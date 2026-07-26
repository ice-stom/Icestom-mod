package io.gitlab.icestom.icestom_mod.track.feature.region;

import io.gitlab.icestom.icestom_mod.track.feature.generic.Cuboid;
import io.gitlab.icestom.stomtrack.type.Vec3;

import java.util.Set;

public class InvertedCuboidRegion extends Cuboid<Cuboid.CuboidRenderState> implements TrackRegion<io.gitlab.icestom.stomtrack.TrackRegion.InvertedCuboidRegion> {

    protected Set<String> tags;

    public InvertedCuboidRegion(double x1, double y1, double z1, double x2, double y2, double z2, Set<String> tags) {
        super(x1, y1, z1, x2, y2, z2);
        this.tags = tags;
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }

    @Override
    public InvertedCuboidRegionRenderState getRenderState(int globalIndex) {
        return new InvertedCuboidRegionRenderState(
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
    public io.gitlab.icestom.stomtrack.TrackRegion.InvertedCuboidRegion toEncoded() {
        return new io.gitlab.icestom.stomtrack.TrackRegion.InvertedCuboidRegion(
                new Vec3(x1, y1, z1),
                new Vec3(x2, y2, z2)
        );
    }


    public record InvertedCuboidRegionRenderState(
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
        public String type() { return "InvertedCuboidRegion"; }
    }
}
