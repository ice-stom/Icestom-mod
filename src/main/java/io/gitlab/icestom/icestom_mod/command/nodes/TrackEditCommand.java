package io.gitlab.icestom.icestom_mod.command.nodes;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.gitlab.icestom.icestom_mod.IceStomModClient;
import io.gitlab.icestom.icestom_mod.command.ArgumentFetcher;
import io.gitlab.icestom.icestom_mod.command.BoatUtilsNodeGenerator;
import io.gitlab.icestom.icestom_mod.command.suggestion.TrackSuggestor;
import io.gitlab.icestom.icestom_mod.command.argument.FlagArgumentType;
import io.gitlab.icestom.icestom_mod.command.suggestion.TrackTagSuggester;
import io.gitlab.icestom.icestom_mod.track.Track;
import io.gitlab.icestom.icestom_mod.track.TrackHandler;
import io.gitlab.icestom.icestom_mod.track.feature.*;
import io.gitlab.icestom.icestom_mod.track.feature.checkpoint.CuboidCheckpoint;
import io.gitlab.icestom.icestom_mod.track.feature.checkpoint.LineCheckpoint;
import io.gitlab.icestom.icestom_mod.track.feature.location.GridLocation;
import io.gitlab.icestom.icestom_mod.track.feature.location.SpawnLocation;
import io.gitlab.icestom.stomtrack.shadow.io.github.openboatutils.protocol.channels.OBUSettingsPacket;
import io.gitlab.icestom.stomtrack.type.Location;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class TrackEditCommand implements CommandNodeProvider {

    public WeakReference<TrackFeature> lastTouchedFeature = new WeakReference<>(null);

    private static final SimpleCommandExceptionType NO_TRACK_SELECTED =
            new SimpleCommandExceptionType(new LiteralMessage("No track selected."));

    private static final SimpleCommandExceptionType NO_TRACK_TAG =
            new SimpleCommandExceptionType(new LiteralMessage("Track doesn't have this tag."));

    private LiteralArgumentBuilder<FabricClientCommandSource> genericIndexedTree(String literal, Command<FabricClientCommandSource> command) {
        return literal(literal)
                .executes(command)
                .then(argument("index", IntegerArgumentType.integer(0))
                        .suggests((_, builder) -> {
                            if (builder.getRemainingLowerCase().isEmpty()) builder.suggest(0);
                            return builder.buildFuture();
                        })
                        .executes(command))
                .then(argument("flags", FlagArgumentType.flags(CheckpointFlags.class))
                        .executes(command)
                        .then(argument("index", IntegerArgumentType.integer(0))
                                .executes(command)));
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> genericTaggedTree(String literal, Command<FabricClientCommandSource> command) {
        return literal(literal)
                .executes(command)
                .then(argument("tag", StringArgumentType.word())
                        .suggests((_, builder) -> {
                            if (builder.getRemainingLowerCase().isEmpty()) builder.suggest("<tag>");
                            return builder.buildFuture();
                        })
                        .executes(command))
                .then(argument("flags", FlagArgumentType.flags(RegionFlags.class))
                        .executes(command)
                        .then(argument("tag", StringArgumentType.word())
                                .executes(command)));
    }

    public LiteralArgumentBuilder<FabricClientCommandSource> command() {

        return literal("te").requires(source -> Minecraft.getInstance().hasSingleplayerServer())
                .then(literal("create").then(argument("id", StringArgumentType.word()).suggests(new TrackSuggestor())
                        .executes(this::doCreate)))
                .then(literal("select").then(argument("id", StringArgumentType.word()).suggests(new TrackSuggestor())
                        .executes(this::doSelect)))
                .then(literal("info")
                        .executes(this::doTrackInfo))
                .then(literal("tag")
                        .then(literal("set").then(argument("tag", StringArgumentType.word())
                                .suggests(new TrackTagSuggester())
                                .then(argument("value", StringArgumentType.greedyString())
                                        .executes(this::doSetTag))))
                        .then(literal("remove").then(argument("tag", StringArgumentType.word())
                                .suggests(new TrackTagSuggester())
                                .executes(this::doRemoveTag))))
                .then(literal("obu").executes(this::doOBUList).then(literal("list").executes(this::doOBUList))
                        .then(BoatUtilsNodeGenerator.getBoatUtilsNode(literal("add"), this::doOBUAdd)))
                .then(literal("checkpoint")
                        .then(genericIndexedTree("line", this::doCheckpointLine))
                        .then(genericIndexedTree("cuboid", this::doCheckpointCuboid)))
                .then(genericIndexedTree("grid", this::doCreateGrid))
                .then(literal("spawn")
                        .executes(this::doSpawnLocation))
                .then(literal("region")
                        .then(genericTaggedTree("cuboid", this::doRegionCuboid)));
//                .then(checkpoint_update(literal("last"), context -> {
//                    if (lastTouchedFeature.get() instanceof TrackCheckpoint trackCheckpoint) {
//                        return trackCheckpoint;
//                    }
//
//                    return null;
//                }));
    }

    private int doRegionCuboid(CommandContext<FabricClientCommandSource> context) {
        return 0;
    }

    private int doSetTag(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Track track = activeTrack();

        String tag = StringArgumentType.getString(context, "tag");
        String value = StringArgumentType.getString(context, "value");

        track.getTags().put(tag, value);
        context.getSource().sendFeedback(Component.literal(String.format("Set %s to %s", tag, value)));

        return Command.SINGLE_SUCCESS;
    }

    private int doRemoveTag(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Track track = activeTrack();

        String tag = StringArgumentType.getString(context, "tag");

        if (!track.getTags().containsKey(tag)) throw NO_TRACK_TAG.create();

        track.getTags().remove(tag);

        return Command.SINGLE_SUCCESS;
    }

    private int doTrackInfo(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Track track = activeTrack();

        FabricClientCommandSource source = context.getSource();
        List<TrackFeature> features = track.getFeatures();

        source.sendFeedback(Component.literal("Track: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(track.getId()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
        source.sendFeedback(Component.literal("Looped: " + track.isLooped() + "  Features: " + features.size())
                .withStyle(ChatFormatting.GRAY));

        Map<String, List<MutableComponent>> sections = new LinkedHashMap<>();
        sections.put("Spawn Location", new ArrayList<>());
        sections.put("Grid Locations", new ArrayList<>());
        sections.put("Line Checkpoints", new ArrayList<>());
        sections.put("Cuboid Checkpoints", new ArrayList<>());

        for (int internalId = 0; internalId < features.size(); internalId++) {
            TrackFeature feature = features.get(internalId);
            String title = switch (feature) {
                case SpawnLocation _ -> "Spawn Location";
                case GridLocation _ -> "Grid Locations";
                case LineCheckpoint _ -> "Line Checkpoints";
                case CuboidCheckpoint _ -> "Cuboid Checkpoints";
            };

            int finalInternalId = internalId;
            sections.get(title).add((switch (feature) {
                case SpawnLocation spawn -> Component.literal(String.format(
                                " - x=%.2f y=%.2f z=%.2f yaw=%.1f°",
                                spawn.getX(), spawn.getY(), spawn.getZ(), Math.toDegrees(spawn.getR())))
                        .withStyle(ChatFormatting.WHITE);

                case GridLocation grid -> Component.literal(String.format(
                                " - #%d  x=%.2f y=%.2f z=%.2f yaw=%.1f°",
                                grid.getIndex(), grid.getX(), grid.getY(), grid.getZ(), Math.toDegrees(grid.getR())))
                        .withStyle(ChatFormatting.AQUA);

                case LineCheckpoint checkpoint -> Component.literal(String.format(
                                " - #%d  (%.2f, %.2f) -> (%.2f, %.2f)  y=%.2f h=%.2f",
                                checkpoint.getIndex(), checkpoint.getX1(), checkpoint.getZ1(),
                                checkpoint.getX2(), checkpoint.getZ2(), checkpoint.getY(), checkpoint.getHalfHeight() * 2))
                        .withStyle(ChatFormatting.GREEN);

                case CuboidCheckpoint checkpoint -> Component.literal(String.format(
                                " - #%d  min=(%.2f, %.2f, %.2f)  max=(%.2f, %.2f, %.2f)",
                                checkpoint.getIndex(), checkpoint.getX1(), checkpoint.getY1(), checkpoint.getZ1(),
                                checkpoint.getX2(), checkpoint.getY2(), checkpoint.getZ2()))
                        .withStyle(ChatFormatting.LIGHT_PURPLE);
            }).withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(
                    Component.literal("ID: " + finalInternalId).withStyle(ChatFormatting.DARK_GRAY))
            )));
        }

        for (Map.Entry<String, List<MutableComponent>> section : sections.entrySet()) {
            List<MutableComponent> lines = section.getValue();
            if (lines.isEmpty()) continue;

            source.sendFeedback(Component.literal(""));
            source.sendFeedback(Component.literal(section.getKey() + " (" + lines.size() + ")")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));

            for (MutableComponent line : lines) {
                source.sendFeedback(line);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private int doSpawnLocation(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Track track = activeTrack();
        Player player = player();

        track.getFeatures().stream().filter(trackFeature -> trackFeature instanceof SpawnLocation).toList().forEach(track::removeFeature);

        SpawnLocation spawnLocation = new SpawnLocation(player.getX(), player.getY(), player.getZ(), (float) Math.toRadians(player.getYRot()));
        track.addFeature(spawnLocation);

        context.getSource().sendFeedback(Component.literal(String.format("Moved spawn to [%.2f,%.2f,%.2f] %.2fdeg", spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), Math.toDegrees(spawnLocation.getR()))));

        lastTouchedFeature = new WeakReference<>(spawnLocation);
        return Command.SINGLE_SUCCESS;
    }

    private int doOBUAdd(CommandContext<FabricClientCommandSource> context, OBUSettingsPacket packet) throws CommandSyntaxException {
        Track track = activeTrack();

        track.getOBUSettingsPackets().add(packet);

        context.getSource().sendFeedback(Component.literal("Added " + packet.getClass().getSimpleName() + "."));

        return Command.SINGLE_SUCCESS;
    }

    private int doOBUList(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Track track = activeTrack();

        List<OBUSettingsPacket> packets = track.getOBUSettingsPackets();
        if (packets.isEmpty()) {
            context.getSource().sendFeedback(
                    Component.literal("No OBU settings packets on this track.").withStyle(ChatFormatting.GRAY));
            return Command.SINGLE_SUCCESS;
        }

        context.getSource()
                .sendFeedback(Component.literal("OBU Settings ")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withBold(true))
                        .append(Component.literal("(" + packets.size() + ")")
                                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withBold(false))));

        int index = 1;
        for (OBUSettingsPacket obuSettingsPacket : packets) {
            Class<?> clazz = obuSettingsPacket.getClass();
            MutableComponent line = Component.literal("#" + index++ + " ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(clazz.getSimpleName()).withStyle(ChatFormatting.WHITE));

            Field[] fields = clazz.getDeclaredFields();
            if (fields.length == 0) {
                line.append(Component.literal(" (no fields)").withStyle(ChatFormatting.GRAY));
            }

            for (Field field : fields) {
                field.setAccessible(true);
                String valueStr;
                try {
                    Object value = field.get(obuSettingsPacket);
                    if (value == null) {
                        valueStr = "null";
                    } else if (value instanceof List<?> list) {
                        valueStr = list.isEmpty()
                                ? "[]"
                                : list.stream().map(String::valueOf).collect(Collectors.joining(", "));
                    } else if (value.getClass().isArray()) {
                        int length = java.lang.reflect.Array.getLength(value);
                        List<String> parts = new ArrayList<>(length);
                        for (int i = 0; i < length; i++) {
                            parts.add(String.valueOf(java.lang.reflect.Array.get(value, i)));
                        }
                        valueStr = parts.isEmpty() ? "[]" : String.join(", ", parts);
                    } else {
                        valueStr = String.valueOf(value);
                    }
                } catch (IllegalAccessException e) {
                    valueStr = "<error>";
                }

                line.append(Component.literal("\n  " + field.getName() + ": ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(valueStr).withStyle(ChatFormatting.WHITE));
            }

            context.getSource().sendFeedback(line);
        }

        return Command.SINGLE_SUCCESS;
    }

    private int doCreateGrid(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Track track = activeTrack();
        Player player = player();

        EnumSet<GridFlags> flags = ArgumentFetcher.getOptionalArgument(
                context, "flags", FlagArgumentType::getFlags, EnumSet.noneOf(GridFlags.class));
        int index = ArgumentFetcher.getOptionalArgument(
                context, "index", IntegerArgumentType::getInteger, 0);

        if (flags.contains(GridFlags.REMOVE)) {
            track.getFeatures()
                    .stream()
                    .filter(trackFeature -> trackFeature instanceof GridLocation)
                    .filter(trackFeature -> ((GridLocation) trackFeature).getIndex() == index)
                    .toList()
                    .forEach(track::removeFeature);
        } else {
            Vec3 position = player.position();
            double yaw = player.getYRot();

            GridLocation gridLocation = new GridLocation(position.x, position.y, position.z, (float) Math.toRadians(yaw), index);

            track.addFeature(gridLocation);
            track.updateRenderState();

            lastTouchedFeature = new WeakReference<>(gridLocation);
            return Command.SINGLE_SUCCESS;
        }

        track.updateRenderState();

        return Command.SINGLE_SUCCESS;
    }

    private int doCheckpointCuboid(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Track track = activeTrack();
        Player player = player();

        EnumSet<CheckpointFlags> flags = ArgumentFetcher.getOptionalArgument(
                context, "flags", FlagArgumentType::getFlags, EnumSet.noneOf(CheckpointFlags.class));
        int index = ArgumentFetcher.getOptionalArgument(
                context, "index", IntegerArgumentType::getInteger, 0);

        if (flags.contains(CheckpointFlags.REMOVE) || !flags.contains(CheckpointFlags.OVERRIDE)) {
            track.getFeatures()
                    .stream()
                    .filter(trackFeature -> trackFeature instanceof CuboidCheckpoint)
                    .filter(trackFeature -> ((CuboidCheckpoint) trackFeature).getIndex() == index)
                    .toList()
                    .forEach(track::removeFeature);
        }

        if (!flags.contains(CheckpointFlags.REMOVE)) {
            Vec3 position = player.position();

            CuboidCheckpoint cuboidCheckpoint = new CuboidCheckpoint(
                    position.x - 2.0F, position.y - 2.0F, position.z - 2.0F,
                    position.x + 2.0F, position.y + 2.0F, position.z + 2.0F,
                    index
            );

            track.addFeature(cuboidCheckpoint);

            lastTouchedFeature = new WeakReference<>(cuboidCheckpoint);
        }

        track.updateRenderState();

        return Command.SINGLE_SUCCESS;
    }

    private int doCheckpointLine(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Track track = activeTrack();
        Player player = player();

        EnumSet<CheckpointFlags> flags = ArgumentFetcher.getOptionalArgument(
                context, "flags", FlagArgumentType::getFlags, EnumSet.noneOf(CheckpointFlags.class));
        int index = ArgumentFetcher.getOptionalArgument(
                context, "index", IntegerArgumentType::getInteger, 0);

        if (flags.contains(CheckpointFlags.REMOVE) || !flags.contains(CheckpointFlags.OVERRIDE)) {
            track.getFeatures()
                    .stream()
                    .filter(trackFeature -> trackFeature instanceof LineCheckpoint)
                    .filter(trackFeature -> ((LineCheckpoint) trackFeature).getIndex() == index)
                    .toList()
                    .forEach(track::removeFeature);
        }

        if (!flags.contains(CheckpointFlags.REMOVE)) {
            Vec3 position = player.position();

            LineCheckpoint lineCheckpoint = new LineCheckpoint(
                    position.x - 2.0F, position.z - 2.0F,
                    position.x + 2.0F, position.z + (double) 2.0F,
                    position.y + 2.0F, 2.0F,
                    index
            );

            track.addFeature(lineCheckpoint);

            lastTouchedFeature = new WeakReference<>(lineCheckpoint);
        }

        track.updateRenderState();

        return Command.SINGLE_SUCCESS;
    }


    private int doSelect(CommandContext<FabricClientCommandSource> context) {
        String id = StringArgumentType.getString(context, "id");
        TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;
        Track track = trackHandler.getTrack(id);

        if (track != null) {
            context.getSource().sendFeedback(Component.literal("Selected " + track.getId()));
            trackHandler.setSelectedTrack(track);
            return Command.SINGLE_SUCCESS;
        }

        context.getSource().sendError(Component.literal("No track with id '" + id + "'."));
        return 0;
    }

    private int doCreate(CommandContext<FabricClientCommandSource> context) {
        String id = StringArgumentType.getString(context, "id");

        TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;

        Track existing = trackHandler.getTrack(id);

        if (existing != null) {
            context.getSource().sendError(Component.literal("Track '" + id + "' already exists."));
            return 0;
        }

        Vec3 position = Minecraft.getInstance().player.position();
        float yaw = Minecraft.getInstance().player.getYRot();
        Track track = new Track(id, new Location(position.x, position.y, position.z, yaw, 0.0F), true, Map.of(
                "icestom.author", Minecraft.getInstance().player.getPlainTextName(),
                "icestom.license", "ISTT1"
        ), List.of());

        trackHandler.loadTrack(track);
        trackHandler.setSelectedTrack(track);

        context.getSource().sendFeedback(Component.literal("Selected " + id));
        return Command.SINGLE_SUCCESS;
    }

    private static @NotNull Track activeTrack() throws CommandSyntaxException {
        Track track = IceStomModClient.instance.trackHandler.getSelectedTrack();

        if (track == null) throw NO_TRACK_SELECTED.create();

        return track;
    }

    private static @NotNull Player player() {
        Player player = Minecraft.getInstance().player;

        if (player == null) throw new RuntimeException("Player missing.");

        return player;
    }

    public enum CheckpointFlags {
        REMOVE,
        OVERRIDE
    }

    public enum GridFlags {
        REMOVE
    }

    public enum RegionFlags {
        REMOVE
    }
}
