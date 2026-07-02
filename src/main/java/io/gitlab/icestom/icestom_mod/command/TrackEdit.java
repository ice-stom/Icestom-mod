package io.gitlab.icestom.icestom_mod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.gitlab.icestom.icestom_mod.IceStomModClient;
import io.gitlab.icestom.icestom_mod.track.Track;
import io.gitlab.icestom.icestom_mod.track.TrackHandler;
import io.gitlab.icestom.icestom_mod.track.feature.CuboidCheckpoint;
import io.gitlab.icestom.icestom_mod.track.feature.GridLocation;
import io.gitlab.icestom.icestom_mod.track.feature.LineCheckpoint;
import io.gitlab.icestom.icestom_mod.track.feature.TrackFeature;
import io.gitlab.icestom.stomtrack.type.Location;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

@Environment(EnvType.CLIENT)
public class TrackEdit implements CommandProvider {
   public LiteralArgumentBuilder<FabricClientCommandSource> command() {
      return literal("te")
              .requires(source -> Minecraft.getInstance().hasSingleplayerServer())
              .then(literal("create")
                      .then(argument("id", StringArgumentType.word())
                              .suggests(new TrackSuggestor())
                              .executes(this::doCreate)
                      )
              )
              .then(literal("select")
                      .then(argument("id", StringArgumentType.word())
                              .suggests(new TrackSuggestor())
                              .executes(this::doSelect)
                      )
              )
              .then(literal("checkpoint")
                      .then(literal("line")
                              .then(argument("index", IntegerArgumentType.integer(0))
                                      .executes(this::doCreateLine))
                      )
                      .then(literal("cuboid")
                              .then(argument("index", IntegerArgumentType.integer(0))
                                      .executes(this::doCreateCuboid))
                      )
              )
             .then(literal("grid")
                     .then(argument("index", IntegerArgumentType.integer(0))
                             .executes(this::doCreateGrid)));
   }

   private int doCreateGrid(CommandContext<FabricClientCommandSource> context) {
      int index = IntegerArgumentType.getInteger(context, "index");
      TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;
      Track track = trackHandler.getSelectedTrack();
      Vec3 position = Minecraft.getInstance().player.position();
      float yaw = Minecraft.getInstance().player.getYRot();
      if (track == null) {
         return 0;
      } else {
         for(TrackFeature feature : track.getFeatures()) {
            if (feature instanceof GridLocation gridLocation) {
               if (gridLocation.getIndex() == index) {
                  track.removeFeature(feature);
                  break;
               }
            }
         }

         track.addFeature(new GridLocation(
                 position.x, position.y, position.z,
                 (float)Math.toRadians(yaw), index)
         );
         track.updateRenderState();
         return Command.SINGLE_SUCCESS;
      }
   }

   private int doCreateCuboid(CommandContext<FabricClientCommandSource> context) {
      int index = IntegerArgumentType.getInteger(context, "index");
      TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;

      @Nullable Track track = trackHandler.getSelectedTrack();

      if (track == null) return 0;

      Vec3 position = Minecraft.getInstance().player.position();

      track.addFeature(new CuboidCheckpoint(
              position.x - 2.0F, position.y - 2.0F, position.z - 2.0F,
              position.x + 2.0F, position.y + 2.0F, position.z + 2.0F,
              index
      ));
      track.updateRenderState();

      return Command.SINGLE_SUCCESS;
   }

   private int doCreateLine(CommandContext<FabricClientCommandSource> context) {
      int index = IntegerArgumentType.getInteger(context, "index");
      TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;
      Track track = trackHandler.getSelectedTrack();
      Vec3 position = Minecraft.getInstance().player.position();

      if (track != null) {
         track.addFeature(new LineCheckpoint(
                 position.x - 2.0F, position.z - 2.0F,
                 position.x() + 2.0F, position.z + (double)2.0F,
                 position.y + 2.0F,
                 2.0F, index));
         track.updateRenderState();
         return Command.SINGLE_SUCCESS;
      }

      return 0;
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
      Track track = new Track(id, new Location(position.x, position.y, position.z, yaw, 0.0F));

      trackHandler.loadTrack(track);
      trackHandler.setSelectedTrack(track);

      context.getSource().sendFeedback(Component.literal("Selected " + id));
      return Command.SINGLE_SUCCESS;
   }
}
