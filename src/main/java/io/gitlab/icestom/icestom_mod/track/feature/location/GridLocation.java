package io.gitlab.icestom.icestom_mod.track.feature.location;

import io.gitlab.icestom.icestom_mod.track.feature.IndexedFeature;

public class GridLocation extends BoatLocation<GridLocation.GridLocationRenderState> implements IndexedFeature {
    private final int index;

    public GridLocation(double x, double y, double z, float r, int index) {
        super(x, y, z, r);
        this.index = index;
    }

    @Override public int getIndex() {
        return this.index;
    }

    @Override
    public GridLocationRenderState getRenderState(int globalIndex) {
        return new GridLocationRenderState(x, y, z, r, index, globalIndex);
    }

    public record GridLocationRenderState(
            double x,
            double y,
            double z,
            float r,
            int index,
            int globalIndex
    ) implements BoatLocationRenderState {
        @Override
        public String label() {
            return String.valueOf(index);
        }
    }
}