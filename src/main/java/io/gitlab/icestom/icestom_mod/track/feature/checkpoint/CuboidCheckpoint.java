package io.gitlab.icestom.icestom_mod.track.feature.checkpoint;

import io.gitlab.icestom.icestom_mod.render.renderers.generic.CuboidFeatureRenderer;
import io.gitlab.icestom.icestom_mod.track.feature.generic.Cuboid;
import io.gitlab.icestom.stomtrack.type.Vec3;

public class CuboidCheckpoint extends Cuboid<Cuboid.CuboidRenderState> implements TrackCheckpoint<io.gitlab.icestom.stomtrack.TrackCheckpoint.CuboidCheckpoint> {

    private int index;

    public CuboidCheckpoint(double x1, double y1, double z1, double x2, double y2, double z2, int index) {
        super(x1, y1, z1, x2, y2, z2);
        this.index = index;
    }

    @Override public int getIndex() {
        return this.index;
    }
    @Override public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public io.gitlab.icestom.stomtrack.TrackCheckpoint.CuboidCheckpoint toEncoded() {
        return new io.gitlab.icestom.stomtrack.TrackCheckpoint.CuboidCheckpoint(
                new Vec3(x1, y1, z1),
                new Vec3(x2, y2, z2)
        );
    }

    @Override
    public CuboidRenderState getRenderState(int globalIndex) {
        return new CuboidCheckpointRenderState(
                x1,
                y1,
                z1,
                x2,
                y2,
                z2,
                index,
                globalIndex
        );
    }

    public record CuboidCheckpointRenderState(
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2,
            int index,
            int globalIndex
    ) implements CuboidRenderState {
        @Override
        public String label() {
            return String.valueOf(index);
        }

        @Override
        public String type() { return "CuboidCheckpoint"; }
    }
}
