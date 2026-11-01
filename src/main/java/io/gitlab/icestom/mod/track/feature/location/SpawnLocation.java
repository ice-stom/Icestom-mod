package io.gitlab.icestom.mod.track.feature.location;

public class SpawnLocation extends BoatLocation<SpawnLocation.SpawnLocationRenderState> {
    public SpawnLocation(double x, double y, double z, float r) {
        super(x, y, z, r);
    }

    @Override
    public SpawnLocationRenderState getRenderState(int globalIndex) {
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
        public String type() { return "Spawn"; }

        @Override
        public String label() {
            return "Spawn";
        }
    }
}