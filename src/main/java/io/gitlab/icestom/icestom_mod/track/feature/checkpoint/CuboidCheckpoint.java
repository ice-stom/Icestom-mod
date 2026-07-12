package io.gitlab.icestom.icestom_mod.track.feature.checkpoint;

import io.gitlab.icestom.icestom_mod.GizmoHandler;
import io.gitlab.icestom.icestom_mod.render.renderers.CuboidCheckpointRenderer;
import io.gitlab.icestom.icestom_mod.track.feature.TrackFeature;
import io.gitlab.icestom.icestom_mod.track.feature.generic.Cuboid;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class CuboidCheckpoint extends Cuboid implements TrackCheckpoint {

    private int index;

    public CuboidCheckpoint(double x1, double y1, double z1, double x2, double y2, double z2, int index) {
        super(x1, y1, z1, x2, y2, z2);
        this.index = index;
    }

    public CuboidCheckpointRenderer.CuboidCheckpointRenderState renderState() {
        return new CuboidCheckpointRenderer.CuboidCheckpointRenderState(this.x1, this.y1, this.z1, this.x2, this.y2,
                this.z2, this.index);
    }

    @Override public int getIndex() {
        return this.index;
    }
    @Override public void setIndex(int index) {
        this.index = index;
    }
}
