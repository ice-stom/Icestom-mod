package io.gitlab.icestom.icestom_mod.track.feature.checkpoint;

import io.gitlab.icestom.icestom_mod.track.feature.IndexedFeature;

public interface TrackCheckpoint<EncodedType extends io.gitlab.icestom.stomtrack.TrackCheckpoint> extends IndexedFeature {
    void setIndex(int index);
    int getIndex();

    EncodedType toEncoded();
}
