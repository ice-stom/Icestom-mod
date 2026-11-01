package io.gitlab.icestom.mod.track;

import io.gitlab.icestom.mod.track.feature.TrackRenderStateProvider;
import io.gitlab.icestom.stomtrack.TrackFile;
import io.gitlab.icestom.stomtrack.TrackLoader;
import io.gitlab.icestom.stomtrack.impl.MutableTrackFile;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class TrackHandler {
    private static final Logger log = LoggerFactory.getLogger(TrackHandler.class);
    private @Nullable Track selectedTrack;
    private final Map<String, Track> tracks = new HashMap<>();

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

    public List<Track> getTracks() {
        return this.tracks.values().stream().toList();
    }

    public void loadTrack(Track track) {
        this.tracks.put(track.getId(), track);
    }

    public @Nullable Track getTrack(String id) {
        return this.tracks.get(id);
    }

    public void saveAllTracks(Path data) {
        for (Track track : this.tracks.values()) {
            TrackFile trackFile = track.toTrackFile();

            try {
                try (OutputStream stream = new FileOutputStream(
                        data.resolve(trackFile.getId() + ".track.xml").toFile())) {
                    TrackLoader.saveTrack(stream, new MutableTrackFile(trackFile));
                }
            } catch (IOException e) {
                log.warn("Failed to save track", e);
            }
        }
    }

    public void unloadAllTracks(Path data) {
        saveAllTracks(data);

        this.setSelectedTrack(null);
        this.tracks.clear();
        TrackRenderStateProvider.EMPTY.apply();
    }
}
