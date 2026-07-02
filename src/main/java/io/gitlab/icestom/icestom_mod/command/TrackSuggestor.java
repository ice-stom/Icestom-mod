package io.gitlab.icestom.icestom_mod.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.gitlab.icestom.icestom_mod.IceStomModClient;
import io.gitlab.icestom.icestom_mod.track.Track;
import io.gitlab.icestom.icestom_mod.track.TrackHandler;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

@Environment(EnvType.CLIENT)
public class TrackSuggestor implements SuggestionProvider<FabricClientCommandSource> {

   @Override
   public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
      TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;

      for (Track track : trackHandler.getTracks()) {
         if (track.getId().startsWith(builder.getRemainingLowerCase())) {
            builder.suggest(track.getId());
         }
      }

      return builder.buildFuture();
   }
}
