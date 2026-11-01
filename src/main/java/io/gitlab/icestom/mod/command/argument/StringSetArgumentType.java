package io.gitlab.icestom.mod.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.StringReader;
import net.minecraft.network.chat.Component;

import java.util.*;

public class StringSetArgumentType implements ArgumentType<Set<String>> {

    private static final DynamicCommandExceptionType DUPLICATE_EXCEPTION =
            new DynamicCommandExceptionType(arg -> Component.literal("Duplicate argument: " + arg));

    private final int minCount;

    private StringSetArgumentType(int minCount) {
        this.minCount = minCount;
    }

    public static StringSetArgumentType stringSet(int minCount) {
        return new StringSetArgumentType(minCount);
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getUniqueStrings(CommandContext<?> context, String name) {
        return (Set<String>) context.getArgument(name, Set.class);
    }

    @Override
    public Set<String> parse(StringReader reader) throws CommandSyntaxException {
        Set<String> result = new LinkedHashSet<>();

        while (true) {
            reader.skipWhitespace();
            if (!reader.canRead()) {
                break;
            }

            int start = reader.getCursor();
            String word = StringArgumentType.string().parse(reader);

            if (!result.add(word)) {
                reader.setCursor(start);
                throw DUPLICATE_EXCEPTION.createWithContext(reader, word);
            }
        }

        if (result.size() < minCount) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                    .dispatcherUnknownArgument()
                    .createWithContext(reader);
        }

        return result;
    }
}