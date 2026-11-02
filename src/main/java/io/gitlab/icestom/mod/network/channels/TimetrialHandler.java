package io.gitlab.icestom.mod.network.channels;

import com.google.protobuf.InvalidProtocolBufferException;
import io.gitlab.icestom.linefold.LZ4SinglePositionReader;
import io.gitlab.icestom.mod.IceStomModClient;
import io.gitlab.icestom.mod.network.ProtoChannel;
import io.gitlab.icestom.mod.render.renderers.generic.PathRenderer;
import io.gitlab.icestom.protocol.packets.timetrial.ClientboundTimetrial;
import io.gitlab.icestom.protocol.packets.timetrial.clientbound.ServerPBLine;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TimetrialHandler implements ProtoChannel.ServerHandler, ProtoChannel.ClientHandler {
    private static final Logger log = LoggerFactory.getLogger(TimetrialHandler.class);

    @Override
    public void handleClientbound(ProtoChannel.BytePayload payload, ProtoChannel.ClientContext context) {

        ClientboundTimetrial clientboundTimetrial;
        try {
            clientboundTimetrial = ClientboundTimetrial.parseFrom(payload.data());
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to decode protobuf", e);
            return;
        }

        if (clientboundTimetrial.hasPbLine()) {
            ServerPBLine pbline = clientboundTimetrial.getPbLine();

            Minecraft.getInstance().doRunTask(() -> {
                ByteArrayInputStream stream = new ByteArrayInputStream(pbline.getLine().toByteArray());

                List<PathRenderer.PathPoint> points = new ArrayList<>();

                try {
                    LZ4SinglePositionReader.readAll(stream, (x, y, z, yaw) -> {
                        points.add(new PathRenderer.PathPoint(
                                x, y, z, 0xFF00FF00
                        ));
                    });
                } catch (IOException e) {
                    log.error("Failed to decode pb line", e);
                }

                IceStomModClient.getInstance().pbRenderer.setRenderState(points);
            });
        }
    }

    @Override
    public void handleServerbound(ProtoChannel.BytePayload payload, ProtoChannel.ServerContext context) {

    }
}
