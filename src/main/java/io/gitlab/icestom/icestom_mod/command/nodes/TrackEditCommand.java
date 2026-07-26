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
import io.gitlab.icestom.icestom_mod.command.argument.NoLeadingDashArgumentType;
import io.gitlab.icestom.icestom_mod.command.argument.StringSetArgumentType;
import io.gitlab.icestom.icestom_mod.command.suggestion.TrackSuggestor;
import io.gitlab.icestom.icestom_mod.command.argument.FlagArgumentType;
import io.gitlab.icestom.icestom_mod.command.suggestion.TrackTagSuggester;
import io.gitlab.icestom.icestom_mod.track.Track;
import io.gitlab.icestom.icestom_mod.track.TrackHandler;
import io.gitlab.icestom.icestom_mod.track.feature.*;
import io.gitlab.icestom.icestom_mod.track.feature.checkpoint.CuboidCheckpoint;
import io.gitlab.icestom.icestom_mod.track.feature.checkpoint.LineCheckpoint;
import io.gitlab.icestom.icestom_mod.track.feature.checkpoint.TrackCheckpoint;
import io.gitlab.icestom.icestom_mod.track.feature.location.BoatLocation;
import io.gitlab.icestom.icestom_mod.track.feature.location.GridLocation;
import io.gitlab.icestom.icestom_mod.track.feature.location.SpawnLocation;
import io.gitlab.icestom.icestom_mod.track.feature.location.TrackLocation;
import io.gitlab.icestom.icestom_mod.track.feature.region.CuboidRegion;
import io.gitlab.icestom.icestom_mod.track.feature.region.TrackRegion;
import io.gitlab.icestom.icestom_mod.track.feature.trigger.LineTrigger;
import io.gitlab.icestom.stomtrack.shadow.io.github.openboatutils.protocol.channels.OBUSettingsPacket;
import io.gitlab.icestom.stomtrack.type.Location;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class TrackEditCommand implements CommandNodeProvider {

    public WeakReference<TrackFeature<?>> lastTouchedFeature = new WeakReference<>(null);

    private static final SimpleCommandExceptionType NO_TRACK_SELECTED =
            new SimpleCommandExceptionType(new LiteralMessage("No track selected."));

    private static final SimpleCommandExceptionType NO_TRACK_TAG =
            new SimpleCommandExceptionType(new LiteralMessage("Track doesn't have this tag."));

    private <F extends Enum<F>> LiteralArgumentBuilder<FabricClientCommandSource> genericIndexedTree(String literal, Class<F> flags, Command<FabricClientCommandSource> command) {
        return literal(literal)
                .executes(command)
                .then(argument("index", IntegerArgumentType.integer(0))
                        .suggests((_, builder) -> {
                            if (builder.getRemainingLowerCase().isEmpty()) builder.suggest(0);
                            return builder.buildFuture();
                        })
                        .executes(command))
                .then(argument("flags", FlagArgumentType.flags(flags))
                        .executes(command)
                        .then(argument("index", IntegerArgumentType.integer(0))
                                .executes(command)));
    }

    private <F extends Enum<F>> LiteralArgumentBuilder<FabricClientCommandSource> genericTaggedTree(String literal, Class<F> flags, Command<FabricClientCommandSource> command) {
        return literal(literal)
//                .executes(command)
                .then(argument("tags", NoLeadingDashArgumentType.wrap(StringSetArgumentType.stringSet(1)))
                        .suggests((_, builder) -> {
                            if (builder.getRemainingLowerCase().isEmpty()) builder.suggest("<tag>");
                            return builder.buildFuture();
                        })
                        .executes(command))
                .then(argument("flags", FlagArgumentType.flags(flags))
//                        .executes(command)
                        .then(argument("tags", NoLeadingDashArgumentType.wrap(StringSetArgumentType.stringSet(1)))
                                .executes(command)));
    }

    private <F extends Enum<F>> LiteralArgumentBuilder<FabricClientCommandSource> genericRequiredTagTree(String literal, Class<F> flags, Command<FabricClientCommandSource> command) {
        return literal(literal)
                .then(argument("tag", NoLeadingDashArgumentType.wrap(StringArgumentType.greedyString()))
                        .suggests((_, builder) -> {
                            if (builder.getRemainingLowerCase().isEmpty()) builder.suggest("<tag>");
                            return builder.buildFuture();
                        })
                        .executes(command))
                .then(argument("flags", FlagArgumentType.flags(flags))
                        .then(argument("tag", NoLeadingDashArgumentType.wrap(StringArgumentType.greedyString()))
                                .executes(command)));
    }

    private Command<FabricClientCommandSource> makeDoCheckpoint(DefaultTrackFeatureConstructor<TrackCheckpoint<?>, Integer> defaultConstructor, Class<?> type) {
        return context -> {
            Track track = activeTrack();
            Player player = player();

            EnumSet<CheckpointFlags> flags = ArgumentFetcher.getOptionalArgument(
                    context, "flags", FlagArgumentType::getFlags, EnumSet.noneOf(CheckpointFlags.class));
            int index = ArgumentFetcher.getOptionalArgument(
                    context, "index", IntegerArgumentType::getInteger, track.getMaxIndexOfFeature(TrackCheckpoint.class, -1) + 1);


            if (flags.contains(CheckpointFlags.DELETE) || !flags.contains(CheckpointFlags.OVERRIDE)) {
                track.getFeatures()
                        .stream()
                        .filter(trackFeature -> type.isAssignableFrom(trackFeature.getClass()))
                        .filter(trackFeature -> ((TrackCheckpoint<?>) trackFeature).getIndex() == index)
                        .toList()
                        .forEach(track::removeFeature);
            }

            if (!flags.contains(CheckpointFlags.DELETE)) {
                TrackFeature<?> trackCheckpoint = (TrackFeature<?>) defaultConstructor.construct(player, index);

                track.addFeature(trackCheckpoint);

                lastTouchedFeature = new WeakReference<>(trackCheckpoint);
            }

            track.updateRenderState();

            return Command.SINGLE_SUCCESS;
        };
    }

    private Command<FabricClientCommandSource> makeDoTaggedThing(DefaultTrackFeatureConstructor<MultipleTaggedFeature, Set<String>> defaultConstructor, Class<?> type) {
        return context -> {
            Track track = activeTrack();
            Player player = player();

            EnumSet<RegionFlags> flags = ArgumentFetcher.getOptionalArgument(
                    context, "flags", FlagArgumentType::getFlags, EnumSet.noneOf(RegionFlags.class));
            Set<String> tags = StringSetArgumentType.getUniqueStrings(context, "tags");

            if (flags.contains(RegionFlags.DELETE) || flags.contains(RegionFlags.REPLACE)) {
                track.getFeatures()
                        .stream()
                        .filter(trackFeature -> type.isAssignableFrom(trackFeature.getClass()))
                        .filter(trackFeature -> {
                            Set<String> intersection = new HashSet<>(((TrackRegion) trackFeature).getTags());

                            intersection.retainAll(tags);

                            return !intersection.isEmpty();
                        })
                        .toList()
                        .forEach(track::removeFeature);
            }

            if (!flags.contains(RegionFlags.DELETE)) {
                TrackFeature<?> trackRegion = (TrackFeature<?>) defaultConstructor.construct(player, tags);

                track.addFeature(trackRegion);

                lastTouchedFeature = new WeakReference<>(trackRegion);
            }

            track.updateRenderState();

            return Command.SINGLE_SUCCESS;
        };
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
                        .then(genericIndexedTree("line", CheckpointFlags.class, makeDoCheckpoint((player, index) -> new LineCheckpoint(
                                player.getX() - 4,
                                player.getZ() - 4,
                                player.getX() + 4,
                                player.getZ() + 4,
                                player.getY(),
                                2f,
                                index
                        ), TrackCheckpoint.class)))
                        .then(genericIndexedTree("cuboid", CheckpointFlags.class, makeDoCheckpoint((player, index) -> new CuboidCheckpoint(
                                player.getX() - 4,
                                player.getY() - 4,
                                player.getZ() - 4,
                                player.getX() + 4,
                                player.getY() + 4,
                                player.getZ() + 4,
                                index
                        ), TrackCheckpoint.class))))
                .then(genericIndexedTree("grid", GridFlags.class, this::doCreateGrid))
                .then(literal("spawn")
                        .executes(this::doSpawnLocation)
                        .then(argument("flags", FlagArgumentType.flags(SpawnLocationFlags.class))
                                .executes(this::doSpawnLocation)))
                .then(literal("region")
                        .then(genericTaggedTree("cuboid", RegionFlags.class, makeDoTaggedThing((player, tags) -> new CuboidRegion(
                                player.getX() - 4,
                                player.getY() - 4,
                                player.getZ() - 4,
                                player.getX() + 4,
                                player.getY() + 4,
                                player.getZ() + 4,
                                tags
                        ), TrackRegion.class))))
                .then(literal("trigger")
                        .then(genericTaggedTree("line", TriggerFlags.class, makeDoTaggedThing((player, tags) -> new LineTrigger(
                                player.getX() - 4,
                                player.getZ() - 4,
                                player.getX() + 4,
                                player.getZ() + 4,
                                player.getY(),
                                2f,
                                tags
                        ), TrackRegion.class))))
                .then(genericRequiredTagTree("location", LocationFlags.class, this::doLocation));
//                .then(checkpoint_update(literal("last"), context -> {
//                    if (lastTouchedFeature.get() instanceof TrackCheckpoint trackCheckpoint) {
//                        return trackCheckpoint;
//                    }
//
//                    return null;
//                }));
    }

    private int doLocation(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Track track = activeTrack();
        Player player = player();

        EnumSet<LocationFlags> flags = ArgumentFetcher.getOptionalArgument(
                context, "flags", FlagArgumentType::getFlags, EnumSet.noneOf(LocationFlags.class));
        String tag = StringArgumentType.getString(context, "tag");

        track.getFeatures()
                .stream()
                .filter(trackFeature -> trackFeature instanceof TrackLocation)
                .filter(trackFeature -> ((TrackLocation) trackFeature).getTag().equals(tag))
                .toList()
                .forEach(track::removeFeature);

        if (!flags.contains(LocationFlags.DELETE)) {
            BoatLocation<TrackLocation.TrackLocationRenderState> trackLocation = new TrackLocation(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    (float) Math.toRadians(player.getYRot()),
                    tag
            );

            if (flags.contains(LocationFlags.SNAP)) {
                trackLocation.snapToPlayer(player);
            }

            track.addFeature(trackLocation);

            lastTouchedFeature = new WeakReference<>(trackLocation);
        }

        track.updateRenderState();

        return Command.SINGLE_SUCCESS;
    }

    private int doTrackInfo(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("i can't be asked to implement this yet"));
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

    private int doSpawnLocation(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        Track track = activeTrack();
        Player player = player();

        EnumSet<SpawnLocationFlags> spawnLocationFlags = ArgumentFetcher.getOptionalArgument(context, "flags", FlagArgumentType::getFlags, EnumSet.noneOf(SpawnLocationFlags.class));

        track.getFeatures().stream().filter(trackFeature -> trackFeature instanceof SpawnLocation).toList().forEach(track::removeFeature);

        SpawnLocation spawnLocation = new SpawnLocation(
                player.getX(),
                player.getY(),
                player.getZ(),
                (float) Math.toRadians(player.getYRot())
        );

        if (spawnLocationFlags.contains(SpawnLocationFlags.SNAP)) {
            spawnLocation.snapToPlayer(player);
        }

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
                        int length = Array.getLength(value);
                        List<String> parts = new ArrayList<>(length);
                        for (int i = 0; i < length; i++) {
                            parts.add(String.valueOf(Array.get(value, i)));
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

        if (flags.contains(GridFlags.DELETE)) {
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
        DELETE,
        OVERRIDE;
    }

    public enum GridFlags implements FlagArgumentType.DescribedFlag {
        DELETE,
        SNAP;

        @Override
        public String getDescription() {
            if (this == SNAP) return "Snap to nearest block centre";

            return null;
        }
    }

    public enum RegionFlags {
        DELETE,
        REPLACE
    }

    public enum TriggerFlags {
        DELETE,
        REPLACE
    }

    public enum LocationFlags implements FlagArgumentType.DescribedFlag {
        DELETE,
        SNAP;

        @Override
        public String getDescription() {
            if (this == SNAP) return "Snap to nearest block centre";

            return null;
        }
    }

    public enum SpawnLocationFlags implements FlagArgumentType.DescribedFlag {
        SNAP;

        @Override
        public String getDescription() {
            if (this == SNAP) return "Snap to nearest block centre";

            return null;
        }
    }
}
