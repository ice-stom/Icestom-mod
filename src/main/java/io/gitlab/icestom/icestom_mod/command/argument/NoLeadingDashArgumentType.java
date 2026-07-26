package io.gitlab.icestom.icestom_mod.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public final class NoLeadingDashArgumentType<T> implements ArgumentType<T> {

    private static final SimpleCommandExceptionType LOOKS_LIKE_FLAG =
            new SimpleCommandExceptionType(Component.literal("Expected a value, but found what looks like a flag (starts with '-')"));

    private final ArgumentType<T> delegate;

    private NoLeadingDashArgumentType(ArgumentType<T> delegate) {
        this.delegate = delegate;
    }

    public static <T> NoLeadingDashArgumentType<T> wrap(ArgumentType<T> delegate) {
        return new NoLeadingDashArgumentType<>(delegate);
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        if (reader.canRead() && reader.peek() == '-') {
            throw LOOKS_LIKE_FLAG.createWithContext(reader);
        }
        return delegate.parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return delegate.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return delegate.getExamples();
    }
}