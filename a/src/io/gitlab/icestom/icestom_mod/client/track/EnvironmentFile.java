package io.gitlab.icestom.icestom_mod.track;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import io.gitlab.icestom.icestom.track.format.TrackEnvironmentData;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EnvironmentFile {
   private static final TomlWriter tomlWriter = (new TomlWriter.Builder()).build();
   private TrackEnvironmentData environmentData;
   private final File file;

   public EnvironmentFile(File file) throws IOException {
      if (!file.exists()) {
         Files.createFile(file.toPath());
      }

      this.environmentData = (TrackEnvironmentData)(new Toml()).read(file).to(TrackEnvironmentData.class);
      this.file = file;
   }

   public void save() throws IOException {
      tomlWriter.write(this.environmentData, this.file);
   }

   public void setMinY(int minY) {
      this.environmentData = new TrackEnvironmentData(this.environmentData.ambient_light(), minY, this.environmentData.height(), this.environmentData.nether_light(), this.environmentData.skybox());
   }

   public void setHeight(int height) {
      this.environmentData = new TrackEnvironmentData(this.environmentData.ambient_light(), this.environmentData.min_y(), height, this.environmentData.nether_light(), this.environmentData.skybox());
   }
}
