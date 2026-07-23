package io.gitlab.icestom.icestom_mod;

import com.mojang.blaze3d.platform.InputConstants;
import io.gitlab.icestom.icestom_mod.command.nodes.TrackEditCommand;
import io.gitlab.icestom.icestom_mod.render.hud.editor.TrackEditor;
import io.gitlab.icestom.icestom_mod.render.pipeline.IcestomRenderPipeline;
import io.gitlab.icestom.icestom_mod.render.renderers.*;
// import io.gitlab.icestom.icestom_mod.track.EnvironmentFile;
import io.gitlab.icestom.icestom_mod.render.renderers.generic.BoatLocationRenderer;
import io.gitlab.icestom.icestom_mod.render.renderers.generic.CuboidFeatureRenderer;
import io.gitlab.icestom.icestom_mod.render.renderers.generic.LineFeatureRenderer;
import io.gitlab.icestom.icestom_mod.track.Track;
import io.gitlab.icestom.icestom_mod.track.TrackHandler;
import io.gitlab.icestom.stomtrack.TrackFile;
import io.gitlab.icestom.stomtrack.TrackLoader;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.gitlab.icestom.stomtrack.impl.MutableEnvironmentFile;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class IceStomModClient implements ClientModInitializer {
    private static final Logger log = LoggerFactory.getLogger(IceStomModClient.class);

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(IceStomMod.id("keybinds"));

    public static final Path ROOT_DATA_DIR = FabricLoader.getInstance().getGameDir().resolve("icestom");

    public static final KeyMapping EDITOR_SPECIAL = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key." + IceStomMod.MOD_ID + ".editor_special",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            CATEGORY
    ));

    public static final KeyMapping EDITOR_DETAILS = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key." + IceStomMod.MOD_ID + ".editor_label",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            CATEGORY
    ));

    private final List<IcestomRenderPipeline<?>> pipelines = new ArrayList<>();

    public final GizmoRenderer gizmoRenderer = this.registerPipeline(new GizmoRenderer());

    public final FeatureLabelRenderer featureRenderer = this.registerPipeline(new FeatureLabelRenderer());
    public final LineFeatureRenderer<LineFeatureRenderer.LineRenderable> lineFeatureRenderer = this.registerPipeline(new LineFeatureRenderer<>());
    public final CuboidFeatureRenderer<CuboidFeatureRenderer.CuboidRenderable> cuboidFeatureRenderable = this.registerPipeline(new CuboidFeatureRenderer<>());
    public final BoatLocationRenderer<BoatLocationRenderer.BoatLocationRenderable> boatLocationRenderer = this.registerPipeline(new BoatLocationRenderer<>());


    public final GizmoHandler gizmoHandler = new GizmoHandler();
    public final TrackHandler trackHandler = new TrackHandler();

    public final TrackEditor trackEditor = new TrackEditor();

    public static IceStomModClient instance;

    public IceStomModClient() {
        instance = this;
    }

    private <T extends IcestomRenderPipeline<?>> T registerPipeline(T pipeline) {
        this.pipelines.add(pipeline);
        return pipeline;
    }

    public void onInitializeClient() {
        WorldRenderEvents.END_MAIN.register(context -> {
            FeatureLabelRenderer.setShowDetails(EDITOR_DETAILS.isDown());
        });

        this.pipelines.forEach((pipeline) -> {
            WorldRenderEvents.END_MAIN.register(pipeline::extractAndDraw);
        });

        boolean ignored_ = ROOT_DATA_DIR.toFile().mkdirs();

        WorldRenderEvents.START_MAIN.register(
                (context) -> this.gizmoHandler.tick(context.gameRenderer().getMainCamera().getPartialTickTime()));

       HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, IceStomMod.id("track_editor"), trackEditor);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.hasSingleplayerServer()) {
                IntegratedServer integratedServer = client.getSingleplayerServer();
                if (integratedServer != null) {
                    Path path = integratedServer.getWorldPath(LevelResource.ROOT).resolve("icestom");
                    boolean ignored = path.toFile().mkdirs();

                     try {
                         File file = path.resolve("world.environment.xml").toFile();

                         MutableEnvironmentFile environmentFile;
                         if (file.exists()) {
                             environmentFile = TrackLoader.loadEnvironmentFile(new FileInputStream(file));
                         } else {
                             environmentFile = new MutableEnvironmentFile();
                         }

                         environmentFile.setMinY(integratedServer.overworld().getMinY());
                         environmentFile.setHeight(integratedServer.overworld().getHeight());

                         TrackLoader.saveEnvironmentFile(new FileOutputStream(file), environmentFile);
                     } catch (IOException e) {
                         Minecraft.getInstance().doRunTask(() -> {
                             Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Failed to load environment data:" + e));
                         });
                     }

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
            dispatcher.register((new TrackEditCommand()).command());
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
