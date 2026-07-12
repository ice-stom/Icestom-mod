package io.gitlab.icestom.icestom_mod.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public abstract class AbstractTagSuggester implements SuggestionProvider<FabricClientCommandSource> {

    private final Set<String> known;

    protected AbstractTagSuggester(Set<String> known) {
        this.known = known;
    }

    protected Collection<String> getAdditional() {
        return Collections.emptyList();
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        for (String s : Stream.concat(known.stream(), getAdditional().stream()).toList()) {
            if (s.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(s);
            }
        }

        return builder.buildFuture();
    }
}
