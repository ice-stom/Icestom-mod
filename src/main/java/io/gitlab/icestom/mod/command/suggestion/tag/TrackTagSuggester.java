package io.gitlab.icestom.mod.command.suggestion.tag;

import io.gitlab.icestom.mod.IceStomModClient;
import io.gitlab.icestom.mod.WellKnownRegistry;
import io.gitlab.icestom.mod.track.Track;

import java.util.*;

public class TrackTagSuggester extends AbstractTagSuggester {

    public TrackTagSuggester() {
        super(WellKnownRegistry.KNOWN_TAGS);
    }

    @Override
    protected Collection<String> getAdditional() {
        Track track = IceStomModClient.getInstance().trackHandler.getSelectedTrack();

        if (track != null) return track.getTags().keySet();

        return Collections.emptyList();
    }
}
