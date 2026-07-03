package io.gitlab.icestom.icestom_mod;

import io.gitlab.icestom.icestom_mod.command.TrackEdit;
import io.gitlab.icestom.icestom_mod.render.pipeline.IcestomRenderPipeline;
import io.gitlab.icestom.icestom_mod.render.renderes.*;
// import io.gitlab.icestom.icestom_mod.track.EnvironmentFile;
import io.gitlab.icestom.icestom_mod.track.Track;
import io.gitlab.icestom.icestom_mod.track.TrackHandler;
import io.gitlab.icestom.stomtrack.TrackFile;
import io.gitlab.icestom.stomtrack.TrackLoader;
import java.io.*;
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
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class IceStomModClient implements ClientModInitializer {
    private static final Logger log = LoggerFactory.getLogger(IceStomModClient.class);

    private final List<IcestomRenderPipeline<?>> pipelines = new ArrayList<>();

    public final GizmoRenderer gizmoRenderer = this.registerPipeline(new GizmoRenderer());
    public final LineCheckpointRenderer lineCheckpointRenderer = this.registerPipeline(new LineCheckpointRenderer());
    public final LineCheckpointTextRenderer lineCheckpointTextRenderer = this
            .registerPipeline(new LineCheckpointTextRenderer());
    public final CuboidCheckpointRenderer cuboidCheckpointRenderer = this
            .registerPipeline(new CuboidCheckpointRenderer());
    public final CuboidCheckpointTextRenderer cuboidCheckpointTextRenderer = this
            .registerPipeline(new CuboidCheckpointTextRenderer());
    public final GridLocationRenderer gridRenderer = this.registerPipeline(new GridLocationRenderer());
    public final GridLocationTextRenderer gridTextRenderer = this.registerPipeline(new GridLocationTextRenderer());
    public final SpawnLocationRenderer spawnLocationRenderer = this.registerPipeline(new SpawnLocationRenderer());

    public final GizmoHandler gizmoHandler = new GizmoHandler();
    public final TrackHandler trackHandler = new TrackHandler();

    public static IceStomModClient instance;

    public IceStomModClient() {
        instance = this;
    }

    private <T extends IcestomRenderPipeline<?>> T registerPipeline(T pipeline) {
        this.pipelines.add(pipeline);
        return pipeline;
    }

    public void onInitializeClient() {
        this.pipelines.forEach((pipeline) -> {
            WorldRenderEvents.END_MAIN.register(pipeline::extractAndDraw);
        });
        WorldRenderEvents.START_MAIN.register(
                (context) -> this.gizmoHandler.tick(context.gameRenderer().getMainCamera().getPartialTickTime()));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.hasSingleplayerServer()) {
                IntegratedServer integratedServer = client.getSingleplayerServer();
                if (integratedServer != null) {
                    Path path = integratedServer.getWorldPath(LevelResource.ROOT).resolve("icestom");
                    boolean ignored = path.toFile().mkdirs();

                    // try {
                    // EnvironmentFile env = new
                    // EnvironmentFile(path.resolve("world.environment").toFile());
                    // env.setMinY(integratedServer.overworld().getMinY());
                    // env.setHeight(integratedServer.overworld().getHeight());
                    // env.save();
                    // } catch (IOException var13) {
                    // Minecraft.getInstance().doRunTask(() -> {
                    //
                    // Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Failed to
                    // load
                    // environment data:" + var13.toString()));
                    // });
                    // }

                    for (File file : Objects.requireNonNull(path.toFile().listFiles())) {
                        if (file.getName().toLowerCase().endsWith(".track.xml")) {
                            log.info("Loading {}", file.getName());

                            try (InputStream is = new FileInputStream(file)) {
                                TrackFile trackFile = TrackLoader.loadTrack(is);
                                this.trackHandler.loadTrack(Track.fromTrackData(trackFile));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (client.hasSingleplayerServer()) {
                IntegratedServer integratedServer = client.getSingleplayerServer();
                if (integratedServer != null) {
                    Path path = integratedServer.getWorldPath(LevelResource.ROOT).resolve("icestom");
                    this.trackHandler.unloadAllTracks(path);
                }
            }
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register((new TrackEdit()).command());
        });
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
