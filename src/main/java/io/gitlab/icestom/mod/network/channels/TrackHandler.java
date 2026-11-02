package io.gitlab.icestom.mod.network.channels;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gitlab.icestom.mod.IceStomModClient;
import io.gitlab.icestom.mod.network.ProtoChannel;
import io.gitlab.icestom.mod.track.Track;
import io.gitlab.icestom.protocol.packets.track.ClientboundTrack;
import io.gitlab.icestom.protocol.packets.track.clientbound.ServerTrack;
import io.gitlab.icestom.stomtrack.TrackFile;
import io.gitlab.icestom.stomtrack.TrackLoader;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TrackHandler implements ProtoChannel.ServerHandler, ProtoChannel.ClientHandler {
    private static final Logger log = LoggerFactory.getLogger(TrackHandler.class);

    @Override
    public void handleClientbound(ProtoChannel.BytePayload payload, ProtoChannel.ClientContext context) {

        ClientboundTrack clientboundTrack;
        try {
            clientboundTrack = ClientboundTrack.parseFrom(payload.data());
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to decode protobuf", e);
            return;
        }

        if (clientboundTrack.hasTrack()) {
            ServerTrack track = clientboundTrack.getTrack();

            IceStomModClient.getInstance().trackHandler.setSelectedTrack(null);

            if (track.hasTrack()) {
                Minecraft.getInstance().doRunTask(() -> {
                    try {
                        TrackFile trackFile = TrackLoader.loadTrack(track.getTrack().newInput());

                        Track track1 = Track.fromTrackData(trackFile);

                        track1.updateRenderState();
                        track1.getRenderState().apply();
                    } catch (IOException e) {
                        log.error("Failed to decode track file from server", e);
                    }
                });
            }
        }
    }

    @Override
    public void handleServerbound(ProtoChannel.BytePayload payload, ProtoChannel.ServerContext context) {

    }
}
