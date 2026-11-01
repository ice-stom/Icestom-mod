package io.gitlab.icestom.mod.render.hud.editor;

import io.gitlab.icestom.mod.IceStomModClient;
import io.gitlab.icestom.mod.track.Track;
import io.gitlab.icestom.mod.track.TrackHandler;
import io.gitlab.icestom.mod.track.feature.location.GridLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class GridTool implements EditorTool {

    @Override
    public String getName() {
        return "Single Grid";
    }

    @Override
    public int getColor() {
        return 0xFF0000FF;
    }

    @Override
    public void selected() {}

    @Override
    public boolean mouseDown() {
        TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;
        Track track = trackHandler.getSelectedTrack();

        if (track == null) return false;

        Vec3 pos = castRay();

        pos = new Vec3(
                Math.floor(pos.x) + .5,
                pos.y,
                Math.floor(pos.z) + .5
        );

        var max = track.getFeatures().stream()
                .filter(trackFeature -> trackFeature instanceof GridLocation)
                .map(trackFeature -> (GridLocation) trackFeature)
                .max(Comparator.comparingInt(GridLocation::getIndex));

        track.addFeature(new GridLocation(
                pos.x, pos.y, pos.z,
                (float) Math.toRadians(snapAngle(Minecraft.getInstance().player.getYRot(), 90)),
                max.map(GridLocation::getIndex).orElse(-1) + 1
        ));

        return true;
    }

    @Override
    public boolean mouseUp() {
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

    private float snapAngle(float angle, float increment) {
        return Math.round(angle / increment) * increment;
    }
}
