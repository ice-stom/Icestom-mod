package io.gitlab.icestom.mod.track.feature.region;

import io.gitlab.icestom.mod.track.feature.MultipleTaggedFeature;

public interface TrackRegion<EncodedType extends io.gitlab.icestom.stomtrack.TrackRegion> extends MultipleTaggedFeature {
    EncodedType toEncoded();
}
