package io.gitlab.icestom.icestom_mod.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.gitlab.icestom.icestom_mod.client.IceStomModClient;
import io.gitlab.icestom.icestom_mod.track.Track;
import io.gitlab.icestom.icestom_mod.track.TrackHandler;
import io.gitlab.icestom.icestom_mod.track.feature.CuboidCheckpoint;
import io.gitlab.icestom.icestom_mod.track.feature.GridLocation;
import io.gitlab.icestom.icestom_mod.track.feature.LineCheckpoint;
import io.gitlab.icestom.icestom_mod.track.feature.TrackFeature;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minestom.server.coordinate.Pos;

@Environment(EnvType.CLIENT)
public class TrackEdit implements CommandProvider {
   public LiteralArgumentBuilder command() {
      return (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal("te").requires((source) -> class_310.method_1551().method_1496())).then(ClientCommandManager.literal("create").then(ClientCommandManager.argument("id", StringArgumentType.word()).suggests(new TrackSuggestor()).executes(this::doCreate)))).then(ClientCommandManager.literal("select").then(ClientCommandManager.argument("id", StringArgumentType.word()).suggests(new TrackSuggestor()).executes(this::doSelect)))).then(((LiteralArgumentBuilder)ClientCommandManager.literal("checkpoint").then(ClientCommandManager.literal("line").then(ClientCommandManager.argument("index", IntegerArgumentType.integer(0)).executes(this::doCreateLine)))).then(ClientCommandManager.literal("cuboid").then(ClientCommandManager.argument("index", IntegerArgumentType.integer(0)).executes(this::doCreateCuboid))))).then(ClientCommandManager.literal("grid").then(ClientCommandManager.argument("index", IntegerArgumentType.integer(0)).executes(this::doCreateGrid)));
   }

   private int doCreateGrid(CommandContext context) {
      int index = IntegerArgumentType.getInteger(context, "index");
      TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;
      Track track = trackHandler.getSelectedTrack();
      class_243 position = class_310.method_1551().field_1724.method_73189();
      float yaw = class_310.method_1551().field_1724.method_36454();
      if (track == null) {
         return 0;
      } else {
         for(TrackFeature feature : track.getFeatures()) {
            if (feature instanceof GridLocation) {
               GridLocation gridLocation = (GridLocation)feature;
               if (gridLocation.getIndex() == index) {
                  track.removeFeature(feature);
                  break;
               }
            }
         }

         track.addFeature(new GridLocation(position.field_1352, position.field_1351, position.field_1350, (float)Math.toRadians((double)yaw), index));
         track.updateRenderState();
         return 1;
      }
   }

   private int doCreateCuboid(CommandContext context) {
      int index = IntegerArgumentType.getInteger(context, "index");
      TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;
      Track track = trackHandler.getSelectedTrack();
      class_243 position = class_310.method_1551().field_1724.method_73189();
      if (track != null) {
         track.addFeature(new CuboidCheckpoint(position.method_10216() - (double)2.0F, position.method_10214() - (double)2.0F, position.method_10215() - (double)2.0F, position.method_10216() + (double)2.0F, position.method_10214() + (double)2.0F, position.method_10215() + (double)2.0F, index));
         track.updateRenderState();
         return 1;
      } else {
         return 0;
      }
   }

   private int doCreateLine(CommandContext context) {
      int index = IntegerArgumentType.getInteger(context, "index");
      TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;
      Track track = trackHandler.getSelectedTrack();
      class_243 position = class_310.method_1551().field_1724.method_73189();
      if (track != null) {
         track.addFeature(new LineCheckpoint(position.method_10216() - (double)2.0F, position.method_10215() - (double)2.0F, position.method_10216() + (double)2.0F, position.method_10215() + (double)2.0F, position.method_10214() + (double)2.0F, (double)2.0F, index));
         track.updateRenderState();
         return 1;
      } else {
         return 0;
      }
   }

   private int doSelect(CommandContext context) {
      String id = StringArgumentType.getString(context, "id");
      TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;
      Track track = trackHandler.getTrack(id);
      if (track != null) {
         ((FabricClientCommandSource)context.getSource()).sendFeedback(class_2561.method_43470("Selected " + track.getId()));
         trackHandler.setSelectedTrack(track);
         return 1;
      } else {
         ((FabricClientCommandSource)context.getSource()).sendError(class_2561.method_43470("No track with id '" + id + "'."));
         return 0;
      }
   }

   private int doCreate(CommandContext context) {
      String id = StringArgumentType.getString(context, "id");
      TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;
      Track existing = trackHandler.getTrack(id);
      if (existing != null) {
         ((FabricClientCommandSource)context.getSource()).sendError(class_2561.method_43470("Track '" + id + "' already exists."));
         return 0;
      } else {
         class_243 position = class_310.method_1551().field_1724.method_73189();
         float yaw = class_310.method_1551().field_1724.method_36454();
         Track track = new Track(id, new Pos(position.field_1352, position.field_1351, position.field_1350, yaw, 0.0F));
         trackHandler.loadTrack(track);
         trackHandler.setSelectedTrack(track);
         ((FabricClientCommandSource)context.getSource()).sendFeedback(class_2561.method_43470("Selected " + id));
         return 1;
      }
   }
}
