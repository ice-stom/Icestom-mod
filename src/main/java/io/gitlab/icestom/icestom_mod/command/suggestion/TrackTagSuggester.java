package io.gitlab.icestom.icestom_mod.command.suggestion;

import io.gitlab.icestom.icestom_mod.IceStomModClient;
import io.gitlab.icestom.icestom_mod.track.Track;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.*;

public class TrackTagSuggester extends AbstractTagSuggester {
    private static final Set<String> KNOWN = new HashSet<>(List.of(
            "icestom.author",
            "icestom.license",
            "icestom.contributers"
    ));

    public TrackTagSuggester() {
        super(KNOWN);
    }

    @Override
    protected Collection<String> getAdditional() {
        Track track = IceStomModClient.getInstance().trackHandler.getSelectedTrack();

        if (track != null) return track.getTags().keySet();

        return Collections.emptyList();
    }
}
