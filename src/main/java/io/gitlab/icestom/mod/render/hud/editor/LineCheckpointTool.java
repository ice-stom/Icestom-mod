package io.gitlab.icestom.mod.render.hud.editor;

import io.gitlab.icestom.mod.IceStomModClient;
import io.gitlab.icestom.mod.track.Track;
import io.gitlab.icestom.mod.track.TrackHandler;
import io.gitlab.icestom.mod.track.feature.checkpoint.LineCheckpoint;
import io.gitlab.icestom.mod.track.feature.checkpoint.TrackCheckpoint;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class LineCheckpointTool implements EditorTool {

    private Vec3 mouseDownPosition;
    private long mouseDownTimestamp = 0;

    @Override
    public String getName() {
        return "Line Checkpoint";
    }

    @Override
    public int getColor() {
        return 0xFF00FF00;
    }

    @Override
    public void selected() {}

    @Override
    public boolean mouseDown() {
        mouseDownPosition = castRay();
        mouseDownTimestamp = Util.getNanos();

        return true;
    }

    @Override
    public boolean mouseUp() {
        if (mouseDownPosition == null) return false;

        Vec3 mousePos = castRay();

        if (mousePos == null) return false;

        TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;
        Track track = trackHandler.getSelectedTrack();

        if (track == null) return false;

        if (Util.getNanos() - mouseDownTimestamp < 100e6) return true;

        var max = track.getFeatures().stream()
                .filter(trackFeature -> trackFeature instanceof TrackCheckpoint)
                .map(trackFeature -> (TrackCheckpoint) trackFeature)
                .max(Comparator.comparingInt(TrackCheckpoint::getIndex));

        track.addFeature(new LineCheckpoint(
                mouseDownPosition.x,
                mouseDownPosition.z,
                mousePos.x,
                mousePos.z,
                mouseDownPosition.y,
                2,
                max.map(TrackCheckpoint::getIndex).orElse(-1) + 1
        ));

        mouseDownPosition = null;

        return true;
    }

    private Vec3 castRay() {
        final Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) return null;
        if (minecraft.level == null) return null;


        Vec3 origin = minecraft.player.getEyePosition(1.0F);
        Vec3 direction = minecraft.player.getViewVector(1.0F);


        BlockHitResult result = Minecraft.getInstance().level.clip(new ClipContext(
                origin,
                origin.add(direction.scale(100)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                minecraft.player
        ));

        return result.getLocation();
    }
}
