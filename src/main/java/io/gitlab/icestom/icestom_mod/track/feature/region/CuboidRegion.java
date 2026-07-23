package io.gitlab.icestom.icestom_mod.track.feature.region;

import io.gitlab.icestom.icestom_mod.track.feature.generic.Cuboid;

import java.util.Set;

public class CuboidRegion extends Cuboid implements TrackRegion {

    private Set<String> tags;

    public CuboidRegion(double x1, double y1, double z1, double x2, double y2, double z2, Set<String> tags) {
        super(x1, y1, z1, x2, y2, z2);
        this.tags = tags;
    }

    @Override public Set<String> getTags() { return tags; }
}
