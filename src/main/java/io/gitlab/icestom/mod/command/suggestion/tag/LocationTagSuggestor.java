package io.gitlab.icestom.mod.command.suggestion.tag;

import io.gitlab.icestom.mod.IceStomModClient;
import io.gitlab.icestom.mod.WellKnownRegistry;
import io.gitlab.icestom.mod.track.Track;
import io.gitlab.icestom.mod.track.feature.location.TrackLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class LocationTagSuggestor extends AbstractTagSuggester {

    public LocationTagSuggestor() {
        super(WellKnownRegistry.KNOWN_LOCATIONS);
    }

    @Override
    protected Collection<String> getAdditional() {
        Track track = IceStomModClient.getInstance().trackHandler.getSelectedTrack();

        if (track != null) return track.getFeatures().stream()
                .filter(trackFeature -> trackFeature instanceof TrackLocation)
                .map(trackFeature -> ((TrackLocation) trackFeature).getTag())
                .collect(Collectors.toList());

        return Collections.emptyList();
    }
}
