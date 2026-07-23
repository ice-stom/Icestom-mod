package io.gitlab.icestom.icestom_mod.track.feature.checkpoint;

import io.gitlab.icestom.icestom_mod.track.feature.generic.Line;
import io.gitlab.icestom.stomtrack.type.Vec2;

public class LineCheckpoint extends Line<LineCheckpoint.LineCheckpointRenderState> implements TrackCheckpoint<io.gitlab.icestom.stomtrack.TrackCheckpoint.LineCheckpoint> {

    private int index;

    public LineCheckpoint(double x1, double z1, double x2, double z2, double y, double height, int index) {
        super(x1, z1, x2, x2, y, height);
        this.index = index;
    }

    @Override public int getIndex() { return this.index; }
    @Override public void setIndex(int index) { this.index = index; }

    @Override
    public io.gitlab.icestom.stomtrack.TrackCheckpoint.LineCheckpoint toEncoded() {
        return new io.gitlab.icestom.stomtrack.TrackCheckpoint.LineCheckpoint(
                new Vec2(x1, z1),
                new Vec2(x2, z2),
                y,
                halfHeight * 2
        );
    }

    @Override
    public LineCheckpointRenderState getRenderState(int globalIndex) {
        return new LineCheckpointRenderState(
                x1,
                z1,
                x2,
                z2,
                y,
                halfHeight * 2,
                index,
                globalIndex
        );
    }

    public record LineCheckpointRenderState(
            double x1,
            double z1,
            double x2,
            double z2,
            double y,
            double height,
            int index,
            int globalIndex
    ) implements LineRenderState {

        @Override
        public String label() {
            return String.valueOf(index);
        }
    }
}
