package io.gitlab.icestom.icestom_mod.track.feature.trigger;

import io.gitlab.icestom.icestom_mod.track.feature.generic.Line;
import io.gitlab.icestom.stomtrack.type.Vec2;

import java.util.Set;

public class LineTrigger extends Line<LineTrigger.LineTriggerRenderState> implements TrackTrigger<io.gitlab.icestom.stomtrack.TrackTrigger.LineTrigger> {

    private Set<String> tags;

    public LineTrigger(double x1, double z1, double x2, double z2, double y, double height, Set<String> tags) {
        super(x1, z1, x2, z2, y, height);
        this.tags = tags;
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }

    @Override
    public LineTriggerRenderState getRenderState(int globalIndex) {
        return new LineTriggerRenderState(
                x1,
                z1,
                x2,
                z2,
                y,
                halfHeight * 2,
                tags,
                globalIndex
        );
    }

    @Override
    public io.gitlab.icestom.stomtrack.TrackTrigger.LineTrigger toEncoded() {
        return new io.gitlab.icestom.stomtrack.TrackTrigger.LineTrigger(
                new Vec2(x1, z1),
                new Vec2(x2, z2),
                y,
                halfHeight * 2
        );
    }

    public record LineTriggerRenderState(
            double x1,
            double z1,
            double x2,
            double z2,
            double y,
            double height,
            Set<String> tags,
            int globalIndex
    ) implements LineRenderState {

        @Override
        public String label() {
            return String.join(", ", tags);
        }

        @Override
        public String type() { return "LineCheckpoint"; }
    }
}
