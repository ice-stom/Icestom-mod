package io.gitlab.icestom.mod.command.suggestion.tag;

import io.gitlab.icestom.mod.IceStomModClient;
import io.gitlab.icestom.mod.WellKnownRegistry;
import io.gitlab.icestom.mod.track.Track;
import io.gitlab.icestom.mod.track.feature.region.TrackRegion;

import java.util.*;
import java.util.stream.Collectors;

public class RegionTagSuggestor extends AbstractTagSuggester {

    public RegionTagSuggestor() {
        super(WellKnownRegistry.KNOWN_REGIONS);
    }

    @Override
    protected Collection<String> getAdditional() {
        Track track = IceStomModClient.getInstance().trackHandler.getSelectedTrack();

        if (track != null) return track.getFeatures().stream()
                .filter(trackFeature -> trackFeature instanceof TrackRegion)
                .map(trackFeature -> ((TrackRegion<?>) trackFeature).getTags())
                .flatMap(Set::stream)
                .collect(Collectors.toList());

        return Collections.emptyList();
    }
}
