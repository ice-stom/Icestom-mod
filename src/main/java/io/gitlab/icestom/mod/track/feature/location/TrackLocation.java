package io.gitlab.icestom.mod.track.feature.location;

import io.gitlab.icestom.mod.track.feature.TaggedFeature;

public class TrackLocation extends BoatLocation<TrackLocation.TrackLocationRenderState> implements TaggedFeature {
    private String tag;

    public TrackLocation(double x, double y, double z, float r, String tag) {
        super(x, y, z, r);
        this.tag = tag;
    }

    @Override public String getTag() { return tag; }
    @Override public void setTag(String tag) { this.tag = tag; }

    @Override
    public TrackLocationRenderState getRenderState(int globalIndex) {
        return new TrackLocationRenderState(
                x,
                y,
                z,
                r,
                tag,
                globalIndex
        );
    }

    public record TrackLocationRenderState(
            double x,
            double y,
            double z,
            float r,
            String tag,
            int globalIndex
    ) implements BoatLocationRenderState {
        @Override
        public String label() {
            return tag;
        }

        @Override
        public String type() { return "Location"; }
    }
}