package io.gitlab.icestom.icestom_mod.track.feature.trigger;

import io.gitlab.icestom.icestom_mod.track.feature.generic.Line;

import java.util.Set;

public class LineTrigger extends Line implements TrackTrigger {

    private Set<String> tags;

    public LineTrigger(double x1, double z1, double x2, double z2, double y, double height, Set<String> tags) {
        super(x1, z1, x2, z2, y, height);
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }
}
