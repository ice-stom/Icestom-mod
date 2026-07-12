package io.gitlab.icestom.icestom_mod.track.feature.location;

import io.gitlab.icestom.icestom_mod.render.renderers.BoatLocationRenderer;
import io.gitlab.icestom.icestom_mod.track.feature.IndexedFeature;

public class GridLocation extends BoatLocation implements IndexedFeature {
    private final int index;

    public GridLocation(double x, double y, double z, float r, int index) {
        super(x, y, z, r);
        this.index = index;
    }

    public BoatLocationRenderer.BoatLocationRenderState renderState() {
        return new BoatLocationRenderer.BoatLocationRenderState(this.x, this.y, this.z, this.r, this.index);
    }

    @Override public int getIndex() {
        return this.index;
    }
}