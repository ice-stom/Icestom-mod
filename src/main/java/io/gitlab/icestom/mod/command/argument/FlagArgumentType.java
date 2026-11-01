package io.gitlab.icestom.mod.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.Component;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class FlagArgumentType<T extends Enum<T>> implements ArgumentType<EnumSet<T>> {

    private static final DynamicCommandExceptionType UNKNOWN_FLAG =
            new DynamicCommandExceptionType(flag -> Component.literal("Unknown flag: -" + flag));
    private static final SimpleCommandExceptionType MISSING_DASH =
            new SimpleCommandExceptionType(Component.literal("Expected '-' followed by one or more flag letters"));

    private final Class<T> enumClass;
    private final Map<Character, T> byLetter;

    private FlagArgumentType(Class<T> enumClass) {
        this.enumClass = enumClass;
        this.byLetter = new HashMap<>();

        for (T constant : enumClass.getEnumConstants()) {
            char letter = Character.toLowerCase(constant.name().charAt(0));
            T existing = byLetter.put(letter, constant);
            if (existing != null) {
                throw new IllegalStateException(
                        "Flag letter collision: '" + letter + "' is used by both "
                                + existing + " and " + constant);
            }
        }
    }

    public static <T extends Enum<T>> FlagArgumentType<T> flags(Class<T> enumClass) {
        return new FlagArgumentType<>(enumClass);
    }

    @Override
    public EnumSet<T> parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();

        if (!reader.canRead() || reader.peek() != '-') {
            reader.setCursor(start);
            throw MISSING_DASH.createWithContext(reader);
        }
        reader.skip();

        if (!reader.canRead() || Character.isWhitespace(reader.peek())) {
            reader.setCursor(start);
            throw MISSING_DASH.createWithContext(reader);
        }

        EnumSet<T> result = EnumSet.noneOf(enumClass);
        while (reader.canRead() && !Character.isWhitespace(reader.peek())) {
            char c = Character.toLowerCase(reader.read());
            T flag = byLetter.get(c);
            if (flag == null) {
                throw UNKNOWN_FLAG.create(String.valueOf(c));
            }
            result.add(flag);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();

        if (remaining.isEmpty()) {
            return builder.suggest("-").buildFuture();
        }
        if (remaining.charAt(0) != '-') {
            return builder.buildFuture();
        }

        String typed = remaining.substring(1).toLowerCase();
        for (T constant : enumClass.getEnumConstants()) {
            char letter = Character.toLowerCase(constant.name().charAt(0));
            if (typed.indexOf(letter) < 0) {
                var component = Component.literal(String.valueOf(Character.toUpperCase(letter)))
                        .withColor(0x9CC9FC)
                        .append(Component.literal(constant.name().toLowerCase().substring(1))
                                .withColor(0xFFFFFF));

                if (DescribedFlag.class.isAssignableFrom(enumClass)) {
                    String description = ((DescribedFlag) constant).getDescription();

                    if (description != null) component.append(Component.literal(": " + description)
                            .withColor(0xFFFFFF));
                }

                builder.suggest(remaining + letter, component);
            }
        }
        return builder.buildFuture();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> EnumSet<T> getFlags(CommandContext<?> context, String name) {
        return (EnumSet<T>) context.getArgument(name, EnumSet.class);
    }

    public interface DescribedFlag {
        String getDescription();
    }
}