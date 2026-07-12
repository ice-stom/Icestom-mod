package io.gitlab.icestom.icestom_mod.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.gitlab.icestom.icestom_mod.IceStomModClient;
import io.gitlab.icestom.icestom_mod.track.Track;
import io.gitlab.icestom.icestom_mod.track.TrackHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RegionTagSuggestor extends AbstractTagSuggester {
    private static final Set<String> KNOWN = new HashSet<>(List.of(
            "icestom.pit"
    ));

    public RegionTagSuggestor() {
        super(KNOWN);
    }

    @Override
    protected Collection<String> getAdditional() {
        Track track = IceStomModClient.getInstance().trackHandler.getSelectedTrack();

        if (track != null) return track.

        return Collections.emptyList();
    }
}
