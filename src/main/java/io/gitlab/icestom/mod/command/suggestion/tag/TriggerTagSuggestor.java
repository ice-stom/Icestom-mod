package io.gitlab.icestom.mod.command.suggestion.tag;

import io.gitlab.icestom.mod.IceStomModClient;
import io.gitlab.icestom.mod.WellKnownRegistry;
import io.gitlab.icestom.mod.track.Track;
import io.gitlab.icestom.mod.track.feature.trigger.TrackTrigger;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class TriggerTagSuggestor extends AbstractTagSuggester {

    public TriggerTagSuggestor() {
        super(WellKnownRegistry.KNOWN_TRIGGERS);
    }

    @Override
    protected Collection<String> getAdditional() {
        Track track = IceStomModClient.getInstance().trackHandler.getSelectedTrack();

        if (track != null) return track.getFeatures().stream()
                .filter(trackFeature -> trackFeature instanceof TrackTrigger<?>)
                .map(trackFeature -> ((TrackTrigger<?>) trackFeature).getTags())
                .flatMap(Set::stream)
                .collect(Collectors.toList());

        return Collections.emptyList();
    }
}
