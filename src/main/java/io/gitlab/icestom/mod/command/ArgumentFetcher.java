package io.gitlab.icestom.mod.command;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

@FunctionalInterface
public interface ArgumentFetcher<T> {
    T fetch(CommandContext<FabricClientCommandSource> context, String argument) throws  IllegalArgumentException;

    static <T> T getOptionalArgument(CommandContext<FabricClientCommandSource> context, String argument, ArgumentFetcher<T> fetcher, T fallback) {
        try {
            return fetcher.fetch(context, argument);
        } catch (IllegalArgumentException unused) {
            return fallback;
        }
    }
}
