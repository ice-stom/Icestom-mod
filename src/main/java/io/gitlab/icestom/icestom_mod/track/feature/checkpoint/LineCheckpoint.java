package io.gitlab.icestom.icestom_mod.track.feature.checkpoint;

import io.gitlab.icestom.icestom_mod.render.renderers.LineCheckpointRenderer;
import io.gitlab.icestom.icestom_mod.track.feature.generic.Line;

public class LineCheckpoint extends Line implements TrackCheckpoint {

    private int index;

    public LineCheckpoint(double x1, double z1, double x2, double z2, double y, double height, int index) {
        super(x1, z1, x2, x2, y, height);
        this.index = index;
    }

    public LineCheckpointRenderer.LineCheckpointRenderState renderState() {
        return new LineCheckpointRenderer.LineCheckpointRenderState(this.x1, this.z1, this.x2, this.z2, this.y,
                this.halfHeight * (double) 2.0F, this.index);
    }

    @Override public int getIndex() { return this.index; }
    @Override public void setIndex(int index) { this.index = index; }
}
