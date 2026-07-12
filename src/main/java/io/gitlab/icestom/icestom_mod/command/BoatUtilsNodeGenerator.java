package io.gitlab.icestom.icestom_mod.command;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.gitlab.icestom.stomtrack.shadow.io.github.openboatutils.protocol.channels.OBUSettingsPacket;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public interface BoatUtilsNodeGenerator {

    @FunctionalInterface
    interface PacketConsumer {
        int consume(CommandContext<FabricClientCommandSource> context, OBUSettingsPacket packet) throws CommandSyntaxException;
    }

    static LiteralArgumentBuilder<FabricClientCommandSource> getBoatUtilsNode(
            LiteralArgumentBuilder<FabricClientCommandSource> root, PacketConsumer consumer) {
        for (Class<?> subclass : OBUSettingsPacket.class.getPermittedSubclasses()) {

            if (!VALID.contains(subclass))
                continue;

            String name = subclass.getSimpleName().toLowerCase();
            LiteralArgumentBuilder<FabricClientCommandSource> top = literal(name);

            List<Field> fields = new ArrayList<>();
            List<ArgumentType<?>> types = new ArrayList<>();
            for (Field declaredField : subclass.getDeclaredFields()) {
                ArgumentType<?> argumentType = resolveArgumentType(declaredField.getType());
                if (argumentType == null)
                    continue;
                declaredField.setAccessible(true);
                fields.add(declaredField);
                types.add(argumentType);
            }

            if (fields.isEmpty()) {
                top.executes(ctx -> {
                    OBUSettingsPacket packet = buildPacket(subclass, ctx, fields);

                    return consumer.consume(ctx, packet);
                });
            } else {
                RequiredArgumentBuilder<FabricClientCommandSource, ?> chain = argument(
                        fields.get(fields.size() - 1).getName(), types.get(fields.size() - 1));
                chain.executes(ctx -> {
                    OBUSettingsPacket packet = buildPacket(subclass, ctx, fields);
                    return consumer.consume(ctx, packet);
                });

                for (int i = fields.size() - 2; i >= 0; i--) {
                    RequiredArgumentBuilder<FabricClientCommandSource, ?> node = argument(fields.get(i).getName(),
                            types.get(i));
                    node.then(chain);
                    chain = node;
                }

                top.then(chain);
            }

            root.then(top);
        }
        return root;
    }

    private static ArgumentType<?> resolveArgumentType(Class<?> type) {
        if (type.isEnum())
            return StringArgumentType.word();
        return switch (type.getSimpleName()) {
        case "String" -> StringArgumentType.string();
        case "List" -> StringArgumentType.greedyString();
        case "byte", "Byte" -> IntegerArgumentType.integer(Byte.MIN_VALUE, Byte.MAX_VALUE);
        case "int", "Integer" -> IntegerArgumentType.integer();
        case "float", "Float" -> FloatArgumentType.floatArg();
        case "double", "Double" -> DoubleArgumentType.doubleArg();
        case "long", "Long" -> LongArgumentType.longArg();
        case "boolean", "Boolean" -> BoolArgumentType.bool();
        case "short", "Short" -> IntegerArgumentType.integer(Short.MIN_VALUE, Short.MAX_VALUE);
        case "CompoundPacketPayload" -> null;
        default -> throw new IllegalStateException("Unexpected value: " + type.getSimpleName());
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object extractValue(CommandContext<FabricClientCommandSource> ctx, Field field) {
        String argName = field.getName();
        Class<?> t = field.getType();

        if (t.isEnum()) {
            String raw = StringArgumentType.getString(ctx, argName);
            return Enum.valueOf((Class<? extends Enum>) t.asSubclass(Enum.class), raw.toUpperCase());
        }

        return switch (t.getSimpleName()) {
        case "String" -> StringArgumentType.getString(ctx, argName);
        case "List" -> Arrays.asList(StringArgumentType.getString(ctx, argName).split(","));
        case "byte", "Byte" -> (byte) IntegerArgumentType.getInteger(ctx, argName);
        case "int", "Integer" -> IntegerArgumentType.getInteger(ctx, argName);
        case "float", "Float" -> FloatArgumentType.getFloat(ctx, argName);
        case "double", "Double" -> DoubleArgumentType.getDouble(ctx, argName);
        case "long", "Long" -> LongArgumentType.getLong(ctx, argName);
        case "boolean", "Boolean" -> BoolArgumentType.getBool(ctx, argName);
        case "short", "Short" -> (short) IntegerArgumentType.getInteger(ctx, argName);
        default -> throw new IllegalStateException("Unexpected value: " + t.getSimpleName());
        };
    }

    private static OBUSettingsPacket buildPacket(Class<?> subclass, CommandContext<FabricClientCommandSource> ctx,
            List<Field> fields) {
        try {
            Object instance = subclass.getDeclaredConstructor().newInstance();
            for (Field field : fields) {
                Object value = extractValue(ctx, field);
                field.set(instance, value);
            }
            return (OBUSettingsPacket) instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to build packet for " + subclass.getSimpleName(), e);
        }
    }

    Set<Class<? extends OBUSettingsPacket>> VALID = Set.of(OBUSettingsPacket.AirControlPacket.class,
            OBUSettingsPacket.AllowAccelStackingPacket.class, OBUSettingsPacket.BackwardAccelPacket.class,
            OBUSettingsPacket.BlocksSlipperinessPacket.class, OBUSettingsPacket.BoatFallDamagePacket.class,
            OBUSettingsPacket.BoatJumpForcePacket.class, OBUSettingsPacket.BoatWaterElevationPacket.class,
            OBUSettingsPacket.ClearSlipperinessPacket.class, OBUSettingsPacket.CollisionResolutionPacket.class,
            OBUSettingsPacket.CoyoteTimePacket.class, OBUSettingsPacket.DefaultSlipperinessPacket.class,
            OBUSettingsPacket.ExclusiveModePacket.class, OBUSettingsPacket.ExclusiveModeSeriesPacket.class,
            OBUSettingsPacket.ForwardAccelPacket.class, OBUSettingsPacket.GravityPacket.class,
            OBUSettingsPacket.ModePacket.class, OBUSettingsPacket.ModeSeriesPacket.class,
            OBUSettingsPacket.PerBlockPacket.class, OBUSettingsPacket.RemoveBlocksSlipperinessPacket.class,
            OBUSettingsPacket.SetBrakeSlipperiness.class, OBUSettingsPacket.SetFixDoubleWaterElevation.class,
            OBUSettingsPacket.SetHoneyCompatibility.class, OBUSettingsPacket.SetJumps.class,
            OBUSettingsPacket.SetLateralSlipperiness.class, OBUSettingsPacket.SetMaxSpeed.class,
            OBUSettingsPacket.SetMaxSpeedResistance.class, OBUSettingsPacket.SetMultiStepping.class,
            OBUSettingsPacket.SetScale.class, OBUSettingsPacket.SetStepUpSlipperiness.class,
            OBUSettingsPacket.SetWalltapMultiplier.class, OBUSettingsPacket.StepHeightPacket.class,
            OBUSettingsPacket.StepWhileFallingPacket.class, OBUSettingsPacket.SurfaceWaterControlPacket.class,
            OBUSettingsPacket.SwimForcePacket.class, OBUSettingsPacket.TurnAccelPacket.class,
            OBUSettingsPacket.UnderwaterControlPacket.class, OBUSettingsPacket.WaterJumpingPacket.class,
            OBUSettingsPacket.YawAccelPacket.class);
}
