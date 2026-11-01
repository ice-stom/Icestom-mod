package io.gitlab.icestom.mod.track.feature.trigger;

import io.gitlab.icestom.mod.track.feature.MultipleTaggedFeature;

public interface TrackTrigger<EncodedType extends io.gitlab.icestom.stomtrack.TrackTrigger> extends MultipleTaggedFeature {
    EncodedType toEncoded();
}
