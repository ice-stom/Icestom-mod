package io.gitlab.icestom.mod.track.feature.region;

import io.gitlab.icestom.mod.track.feature.generic.Cuboid;
import io.gitlab.icestom.stomtrack.type.Vec3;

import java.util.Set;

public class CuboidRegion extends Cuboid<Cuboid.CuboidRenderState> implements TrackRegion<io.gitlab.icestom.stomtrack.TrackRegion.CuboidRegion> {

    protected Set<String> tags;

    public CuboidRegion(double x1, double y1, double z1, double x2, double y2, double z2, Set<String> tags) {
        super(x1, y1, z1, x2, y2, z2);
        this.tags = tags;
    }

    @Override public Set<String> getTags() { return tags; }

    @Override
    public CuboidRenderState getRenderState(int globalIndex) {
        return new CuboidRegionRenderState(
                x1,
                y1,
                z1,
                x2,
                y2,
                z2,
                tags,
                globalIndex
        );
    }

    @Override
    public io.gitlab.icestom.stomtrack.TrackRegion.CuboidRegion toEncoded() {
        return new io.gitlab.icestom.stomtrack.TrackRegion.CuboidRegion(
                new Vec3(x1, y1, z1),
                new Vec3(x2, y2, z2)
        );
    }

    public record CuboidRegionRenderState(
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2,
            Set<String> tags,
            int globalIndex
    ) implements CuboidRenderState {
        @Override
        public String label() {
            return String.join(", ", tags);
        }

        @Override
        public String type() { return "CuboidRegion"; }
    }
}
