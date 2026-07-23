package io.gitlab.icestom.icestom_mod.track.feature.location;

import io.gitlab.icestom.icestom_mod.render.renderers.FeatureLabelRenderer;

public class SpawnLocation<RenderState extends BoatLocation.BoatLocationRenderState> extends BoatLocation {
    public SpawnLocation(double x, double y, double z, float r) {
        super(x, y, z, r);
    }

    @Override
    public FeatureLabelRenderer.FeatureRenderable getRenderState(int globalIndex) {
        return new SpawnLocationRenderState(
                x,
                y,
                z,
                r,
                globalIndex
        );
    }

    public record SpawnLocationRenderState(
            double x,
            double y,
            double z,
            float r,
            int globalIndex
    ) implements BoatLocationRenderState {
        @Override
        public String label() {
            return "Spawn";
        }
    }
}