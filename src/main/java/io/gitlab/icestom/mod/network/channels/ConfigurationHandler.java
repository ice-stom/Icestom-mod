package io.gitlab.icestom.mod.network.channels;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gitlab.icestom.mod.IceStomMod;
import io.gitlab.icestom.mod.network.ProtoChannel;
import io.gitlab.icestom.protocol.packets.configuration.ClientboundConfiguration;
import io.gitlab.icestom.protocol.packets.configuration.ServerboundConfiguration;
import io.gitlab.icestom.protocol.packets.configuration.clientbound.ServerHello;
import io.gitlab.icestom.protocol.packets.configuration.serverbound.ClientHello;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationHandler implements ProtoChannel.ServerHandler, ProtoChannel.ClientHandler {

    private static final int SPEC = 1;
    private static final String NAMESPACE = IceStomMod.MOD_ID;

    private static final Logger log = LoggerFactory.getLogger(ConfigurationHandler.class);

    @Override
    public void handleClientbound(ProtoChannel.BytePayload payload, ProtoChannel.ClientContext context) {

        ClientboundConfiguration clientboundConfiguration;
        try {
            clientboundConfiguration = ClientboundConfiguration.parseFrom(payload.data());
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to decode protobuf", e);
            return;
        }

        if (clientboundConfiguration.hasHello()) {
            ServerHello hello = clientboundConfiguration.getHello();

            int spec = hello.getSpec();

            if (spec != SPEC) {
                log.warn("Unexpected spec number {}", spec);
                return;
            }

            IceStomMod.CONFIGURATION.sendPacketC2S(ServerboundConfiguration.newBuilder()
                    .setHello(ClientHello.newBuilder()
                            .setNamespace(NAMESPACE)
                            .setVersion(IceStomMod.VERSION.getFriendlyString())
                            .setUnstable(IceStomMod.UNSTABLE)
                            .addFeatures(ClientHello.Feature.newBuilder()
                                    .setKey(Identifier.fromNamespaceAndPath(IceStomMod.MOD_ID, "timetrial").toString())
                                    .setVersion(1)
                                    .build()))
                    .build());
        }
    }

    @Override
    public void handleServerbound(ProtoChannel.BytePayload payload, ProtoChannel.ServerContext context) {

    }
}
