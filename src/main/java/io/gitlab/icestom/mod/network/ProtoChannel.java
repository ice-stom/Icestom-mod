package io.gitlab.icestom.mod.network;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import io.gitlab.icestom.protocol.ISChannel;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtoChannel {

    private static final Logger log = LoggerFactory.getLogger(ProtoChannel.class);

    public record BytePayload(byte[] data, CustomPacketPayload.Type<BytePayload> channelType)
            implements CustomPacketPayload {

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return channelType;
        }

        public <T extends MessageLite> T parse(Parser<T> parser) throws InvalidProtocolBufferException {
            return parser.parseFrom(data);
        }
    }

    public record ServerContext(MinecraftServer server, ServerCommonPacketListenerImpl listener, @Nullable ServerPlayer player, PacketSender responseSender) {}
    public record ClientContext(Minecraft client, PacketSender responseSender) {}

    @FunctionalInterface
    public interface ServerHandler {
        void handleServerbound(BytePayload payload, ServerContext context);
    }

    @FunctionalInterface
    public interface ClientHandler {
        void handleClientbound(BytePayload payload, ClientContext context);
    }

    protected final CustomPacketPayload.Type<BytePayload> id;
    public final StreamCodec<FriendlyByteBuf, BytePayload> codec;
    private final ISChannel.Phase phase;

    private final ServerHandler serverHandler;
    private final ClientHandler clientHandler;

    public <T extends ServerHandler & ClientHandler> ProtoChannel(ISChannel channel, T handler) {
        this(channel, handler, handler);
    }

    public ProtoChannel(ISChannel channel, ServerHandler serverHandler, ClientHandler clientHandler) {
        this.id = new CustomPacketPayload.Type<>(Identifier.parse(channel.getIdentifier()));
        this.phase = channel.getPhase();

        this.serverHandler = serverHandler;
        this.clientHandler = clientHandler;

        this.codec = StreamCodec.of(
                (buf, payload) -> buf.writeBytes(payload.data()),
                buf -> {
                    byte[] data = new byte[buf.readableBytes()];
                    buf.readBytes(data);
                    return new BytePayload(data, id);
                }
        );
    }

    public CustomPacketPayload.Type<BytePayload> id() {
        return id;
    }

    private BytePayload wrap(MessageLite message) {
        return new BytePayload(message.toByteArray(), id);
    }

    public void registerCodec() {
        switch (phase) {
            case CONFIGURATION -> {
                PayloadTypeRegistry.configurationS2C().register(id, codec);
                PayloadTypeRegistry.configurationC2S().register(id, codec);
            }
            case PLAY -> {
                PayloadTypeRegistry.playS2C().register(id, codec);
                PayloadTypeRegistry.playC2S().register(id, codec);
            }
        }
    }

    public void registerServerHandler() {
        switch (phase) {
            case CONFIGURATION -> ServerConfigurationNetworking.registerGlobalReceiver(id, (payload, ctx) ->
                    serverHandler.handleServerbound(payload, new ServerContext(
                            ctx.server(), ctx.networkHandler(), null, ctx.responseSender())));
            case PLAY -> ServerPlayNetworking.registerGlobalReceiver(id, (payload, ctx) ->
                    serverHandler.handleServerbound(payload, new ServerContext(
                            ctx.server(), ctx.player().connection, ctx.player(), ctx.responseSender())));
        }
    }

    public void registerClientHandler() {
        switch (phase) {
            case CONFIGURATION -> ClientConfigurationNetworking.registerGlobalReceiver(id, (payload, ctx) -> {
                clientHandler.handleClientbound(payload, new ClientContext(ctx.client(), ctx.responseSender()));});
            case PLAY -> ClientPlayNetworking.registerGlobalReceiver(id, (payload, ctx) ->
                    clientHandler.handleClientbound(payload, new ClientContext(ctx.client(), ctx.responseSender())));
        }
    }

    public void sendPacketC2S(MessageLite message) {
        switch (phase) {
            case CONFIGURATION -> ClientConfigurationNetworking.send(wrap(message));
            case PLAY -> ClientPlayNetworking.send(wrap(message));
        }
    }

    public void sendPacketS2C(ServerCommonPacketListenerImpl listener, MessageLite message) {
        switch (phase) {
            case CONFIGURATION -> {
                if (!(listener instanceof ServerConfigurationPacketListenerImpl config)) throw new IllegalStateException();
                ServerConfigurationNetworking.send(config, wrap(message));
            }
            case PLAY -> {
                if (!(listener instanceof ServerGamePacketListenerImpl game)) throw new IllegalStateException();
                sendPacketS2C(game.player, message);
            }
        }
    }

    public void sendPacketS2C(ServerPlayer player, MessageLite message) {
        if (phase != ISChannel.Phase.PLAY) throw new IllegalStateException();

        ServerPlayNetworking.send(player, wrap(message));
    }
}