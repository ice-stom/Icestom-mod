package io.gitlab.icestom.icestom_mod.client;

import com.moandjiezana.toml.Toml;
import io.gitlab.icestom.icestom.track.format.TrackData;
import io.gitlab.icestom.icestom_mod.command.TrackEdit;
import io.gitlab.icestom.icestom_mod.render.pipeline.IcestomRenderPipeline;
import io.gitlab.icestom.icestom_mod.render.renderes.CuboidCheckpointRenderer;
import io.gitlab.icestom.icestom_mod.render.renderes.CuboidCheckpointTextRenderer;
import io.gitlab.icestom.icestom_mod.render.renderes.GizmoRenderer;
import io.gitlab.icestom.icestom_mod.render.renderes.GridLocationRenderer;
import io.gitlab.icestom.icestom_mod.render.renderes.GridLocationTextRenderer;
import io.gitlab.icestom.icestom_mod.render.renderes.LineCheckpointRenderer;
import io.gitlab.icestom.icestom_mod.render.renderes.LineCheckpointTextRenderer;
import io.gitlab.icestom.icestom_mod.track.EnvironmentFile;
import io.gitlab.icestom.icestom_mod.track.Track;
import io.gitlab.icestom.icestom_mod.track.TrackHandler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.class_1132;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5218;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class IceStomModClient implements ClientModInitializer {
   private static final Logger log = LoggerFactory.getLogger(IceStomModClient.class);
   private final List pipelines = new ArrayList();
   public final GizmoRenderer gizmoRenderer = (GizmoRenderer)this.registerPipeline(new GizmoRenderer());
   public final LineCheckpointRenderer lineCheckpointRenderer = (LineCheckpointRenderer)this.registerPipeline(new LineCheckpointRenderer());
   public final LineCheckpointTextRenderer lineCheckpointTextRenderer = (LineCheckpointTextRenderer)this.registerPipeline(new LineCheckpointTextRenderer());
   public final CuboidCheckpointRenderer cuboidCheckpointRenderer = (CuboidCheckpointRenderer)this.registerPipeline(new CuboidCheckpointRenderer());
   public final CuboidCheckpointTextRenderer cuboidCheckpointTextRenderer = (CuboidCheckpointTextRenderer)this.registerPipeline(new CuboidCheckpointTextRenderer());
   public final GridLocationRenderer gridRenderer = (GridLocationRenderer)this.registerPipeline(new GridLocationRenderer());
   public final GridLocationTextRenderer gridTextRenderer = (GridLocationTextRenderer)this.registerPipeline(new GridLocationTextRenderer());
   public final GizmoHandler gizmoHandler = new GizmoHandler();
   public final TrackHandler trackHandler = new TrackHandler();
   public static IceStomModClient instance;

   public IceStomModClient() {
      instance = this;
   }

   private IcestomRenderPipeline registerPipeline(IcestomRenderPipeline pipeline) {
      this.pipelines.add(pipeline);
      return pipeline;
   }

   public void onInitializeClient() {
      this.pipelines.forEach((pipeline) -> {
         Event var10000 = WorldRenderEvents.END_MAIN;
         Objects.requireNonNull(pipeline);
         var10000.register(pipeline::extractAndDraw);
      });
      WorldRenderEvents.START_MAIN.register((WorldRenderEvents.StartMain)(context) -> this.gizmoHandler.tick(context.gameRenderer().method_19418().method_55437()));
      ClientPlayConnectionEvents.JOIN.register((ClientPlayConnectionEvents.Join)(handler, sender, client) -> {
         if (client.method_1496()) {
            class_1132 integratedServer = client.method_1576();
            if (integratedServer != null) {
               Path path = integratedServer.method_27050(class_5218.field_24188).resolve("icestom");
               boolean ignored = path.toFile().mkdirs();

               try {
                  EnvironmentFile env = new EnvironmentFile(path.resolve("world.environment").toFile());
                  env.setMinY(integratedServer.method_30002().method_31607());
                  env.setHeight(integratedServer.method_30002().method_31605());
                  env.save();
               } catch (IOException var13) {
                  class_310.method_1551().method_18859(() -> class_310.method_1551().field_1705.method_1743().method_1812(class_2561.method_43470("Failed to load environment data!")));
               }

               for(File file : (File[])Objects.requireNonNull(path.toFile().listFiles())) {
                  if (file.getName().toLowerCase().endsWith(".toml")) {
                     log.info("Loading {}", file.getName());

                     try {
                        TrackData trackData = TrackData.deserialize((new Toml()).read(file));
                        this.trackHandler.loadTrack(Track.fromTrackData(trackData));
                     } catch (TrackData.TrackDeserializationException e) {
                        throw new RuntimeException(e);
                     }
                  }
               }

            }
         }
      });
      ClientPlayConnectionEvents.DISCONNECT.register((ClientPlayConnectionEvents.Disconnect)(handler, client) -> {
         if (client.method_1496()) {
            class_1132 integratedServer = client.method_1576();
            if (integratedServer != null) {
               Path path = integratedServer.method_27050(class_5218.field_24188).resolve("icestom");
               this.trackHandler.unloadAllTracks(path);
            }
         }
      });
      ClientCommandRegistrationCallback.EVENT.register((ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> dispatcher.register((new TrackEdit()).command()));
   }

   public void closePipelines() {
      this.pipelines.forEach(IcestomRenderPipeline::close);
   }

   public GizmoHandler getGizmoHandler() {
      return this.gizmoHandler;
   }

   public static IceStomModClient getInstance() {
      return instance;
   }
}
