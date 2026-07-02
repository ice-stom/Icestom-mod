package io.gitlab.icestom.icestom_mod.track;

import com.moandjiezana.toml.TomlWriter;
import io.gitlab.icestom.icestom.track.format.TrackData;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TrackHandler {
   private static final TomlWriter tomlWriter = (new TomlWriter.Builder()).build();
   private @Nullable Track selectedTrack;
   private final Map tracks = new HashMap();

   public void setSelectedTrack(@Nullable Track selectedTrack) {
      Track old_selected = this.selectedTrack;
      this.selectedTrack = selectedTrack;
      if (old_selected != null) {
         old_selected.updateRenderState();
      }

      if (selectedTrack != null) {
         selectedTrack.updateRenderState();
      }

   }

   public @Nullable Track getSelectedTrack() {
      return this.selectedTrack;
   }

   public List getTracks() {
      return this.tracks.values().stream().toList();
   }

   public void loadTrack(Track track) {
      this.tracks.put(track.getId(), track);
   }

   public @Nullable Track getTrack(String id) {
      return (Track)this.tracks.get(id);
   }

   public void unloadAllTracks(Path data) {
      for(Track track : this.tracks.values()) {
         TrackData trackData = track.toTrackData();

         try {
            Map<String, Object> map = trackData.serialize();
            tomlWriter.write(map, data.resolve(trackData.getId() + ".toml").toFile());
         } catch (IOException var6) {
         }
      }

      this.setSelectedTrack((Track)null);
      this.tracks.clear();
   }
}
