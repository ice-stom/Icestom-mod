package io.gitlab.icestom.mod.screen;

import io.gitlab.icestom.mod.IceStomMod;
import io.gitlab.icestom.mod.IceStomModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TrackExportScreen extends Screen {

    private final Screen parent;

    private EditBox filenameBox;
    private EditBox environmentNameBox;
    private Button exportButton;

    private final LevelStorageSource.LevelStorageAccess access;

    private volatile String status = "";

    public TrackExportScreen(Screen parent, LevelStorageSource.LevelStorageAccess access) {
        super(Component.literal("Export Track"));
        this.parent = parent;
        this.access = access;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 100;

        Path track_path = access.getLevelPath(LevelResource.ROOT).resolve("icestom");

        String d = "my_amazing_track";

        for (String s : Objects.requireNonNull(track_path.toFile().list())) {
            if (s.endsWith(".track.xml")) {
                d = s.substring(0, s.length() - ".track.xml".length());
                continue;
            }
        }

        filenameBox = new EditBox(this.font, x, 74, 200, 20, Component.literal("Track Filename"));
        filenameBox.setHint(Component.literal(d));
        filenameBox.setValue(d);
        addRenderableWidget(filenameBox);

        environmentNameBox = new EditBox(this.font, x, 144, 200, 20, Component.literal("Environment Name"));
        environmentNameBox.setHint(Component.literal(d));
        environmentNameBox.setValue(d);
        addRenderableWidget(environmentNameBox);

        exportButton = Button.builder(Component.literal("Export"), b -> new Thread(() -> onConvert(b)).start())
                .bounds(x, 170, 200, 20).build();

        addRenderableWidget(
                exportButton
        );
        addRenderableWidget(
                Button.builder(CommonComponents.GUI_CANCEL, b -> {
                            minecraft.setScreen(parent);
                        })
                        .bounds(x, 195, 200, 20).build()
        );
    }

    private void onConvert(Button button) {
        String filename = filenameBox.getValue().toLowerCase().replace("-", "_").replace(" ", "_");
        String environment_name = environmentNameBox.getValue().toLowerCase().replace("-", "_").replace(" ", "_");

        button.active = false;

        Path export_path = IceStomModClient.ROOT_DATA_DIR.resolve(filename + ".stomtrack");

        try {
            Path anvilPath = access.getLevelPath(LevelResource.ROOT);
            Path polarPath = anvilPath.resolve("icestom").resolve(environment_name + ".polar");

            Files.createDirectories(polarPath.getParent());

            convertLevelData(anvilPath, polarPath);

            status = "Compressing into stomtrack";

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(export_path.toFile()))) {
                for (File file : Objects.requireNonNull(anvilPath.resolve("icestom").toFile().listFiles())) {
                    if (file.getName().endsWith(".track.xml")) {
                        ZipEntry manifest = new ZipEntry(file.getName());
                        zos.putNextEntry(manifest);
                        zos.write(Files.readAllBytes(file.toPath()));
                        zos.flush();
                    } else if (file.getName().endsWith(".environment.xml")) {
                        ZipEntry manifest = new ZipEntry(environment_name + ".environment.xml");
                        zos.putNextEntry(manifest);
                        zos.write(Files.readAllBytes(file.toPath()));
                        zos.flush();
                    } else if (file.getName().equals(environment_name + ".polar")) {
                        ZipEntry manifest = new ZipEntry(environment_name + ".polar");
                        zos.putNextEntry(manifest);
                        zos.write(Files.readAllBytes(file.toPath()));
                        zos.flush();
                    }
                }
            }

            Minecraft.getInstance().getToastManager().addToast(new SystemToast(
                    SystemToast.SystemToastId.WORLD_BACKUP,
                    Component.literal("Track export success"),
                    Component.literal("Exported to icestom/" + filename + ".stomtrack")
            ));
        } catch (Exception e) {
            IceStomMod.LOGGER.error("Track export failed", e);
            Minecraft.getInstance().getToastManager().addToast(new SystemToast(
                    SystemToast.SystemToastId.WORLD_BACKUP,
                    Component.literal("Track export failed"),
                    Component.literal("Check latest.log for more information.")
            ));
            button.active = true;
            return;
        }

        Minecraft.getInstance().doRunTask(() -> {
            minecraft.setScreen(parent);
        });
    }

    private void convertLevelData(Path anvilPath, Path polarPath) throws IOException, InterruptedException {
        status = "Extracting export worker..";

        Path workerJar = Files.createTempFile("icestom-export-worker", ".jar");
        try {
            try (InputStream in = getClass().getResourceAsStream("/icestom-export-worker.jar")) {
                if (in == null) {
                    throw new IOException("icestom-export-worker.jar not found on classpath");
                }
                Files.copy(in, workerJar, StandardCopyOption.REPLACE_EXISTING);
            }

            String javaBin = ProcessHandle.current().info().command().orElse("java");

            ProcessBuilder pb = new ProcessBuilder(
                    javaBin,
                    "-jar", workerJar.toAbsolutePath().toString(),
                    anvilPath.toAbsolutePath().toString(),
                    polarPath.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(false);

            status = "Starting export worker..";
            Process process = pb.start();

            StringBuilder stderr = new StringBuilder();

            Thread stderrThread = new Thread(() -> {
                try (BufferedReader errReader = new BufferedReader(
                        new java.io.InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = errReader.readLine()) != null) {
                        stderr.append(line).append('\n');
                    }
                } catch (IOException ignored) {
                }
            }, "icestom-export-stderr");
            stderrThread.setDaemon(true);
            stderrThread.start();

            try (BufferedReader reader = new BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("STATUS:")) {
                        status = line.substring("STATUS:".length());
                    } else if (line.equals("DONE")) {
                        status = "Done!";
                    }
                }
            }

            int exitCode = process.waitFor();
            stderrThread.join();

            if (exitCode != 0) {
                throw new IOException("Export worker exited with code " + exitCode
                        + (stderr.length() > 0 ? ":\n" + stderr : ""));
            }
        } finally {
            Files.deleteIfExists(workerJar);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int x = this.width / 2 - 100;

        context.drawCenteredString(font, title, width / 2, 20, 0xFFFFFFFF);

        context.drawString(font, "Track Filename", x, 42, 0xFFFFFFFF);
        context.drawString(font, "Name used for the file, this doesn't", x, 54, 0xFF808080);
        context.drawString(font, "affect the tracks inside.", x, 64, 0xFF808080);

        context.drawString(font, "Environment Id", x, 102, 0xFFFFFFFF);
        context.drawString(font, "Used by IceStom for world data caching,", x, 114, 0xFF808080);
        context.drawString(font, "for tracks with one timetrial", x, 124, 0xFF808080);
        context.drawString(font, "this should match the track id.", x, 134, 0xFF808080);

        context.drawCenteredString(font, status, width / 2, 220, 0xFFFFFFFF);
    }
}