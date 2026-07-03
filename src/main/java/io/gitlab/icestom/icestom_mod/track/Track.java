package io.gitlab.icestom.icestom_mod.track;

import io.gitlab.icestom.icestom_mod.GizmoHandler;
import io.gitlab.icestom.icestom_mod.IceStomModClient;
import io.gitlab.icestom.icestom_mod.render.renderes.BoatLocationRenderer;
import io.gitlab.icestom.icestom_mod.render.renderes.CuboidCheckpointRenderer;
import io.gitlab.icestom.icestom_mod.render.renderes.LineCheckpointRenderer;
import io.gitlab.icestom.icestom_mod.track.feature.*;
import io.gitlab.icestom.stomtrack.TrackCheckpoint;
import io.gitlab.icestom.stomtrack.TrackFile;
import io.gitlab.icestom.stomtrack.shadow.io.github.openboatutils.protocol.channels.OBUSettingsPacket;
import io.gitlab.icestom.stomtrack.shadow.net.kyori.adventure.text.Component;
import io.gitlab.icestom.stomtrack.type.Location;
import io.gitlab.icestom.stomtrack.type.Vec2;
import io.gitlab.icestom.stomtrack.type.Vec3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Track {
    private static final List<LineCheckpointRenderer.LineCheckpointRenderState> lineCheckpointRenderState = new ArrayList<>();
    private static final List<CuboidCheckpointRenderer.CuboidCheckpointRenderState> cuboidCheckpointRenderState = new ArrayList<>();
    private static final List<BoatLocationRenderer.BoatLocationRenderState> gridLocationRenderState = new ArrayList<>();
    private static final List<BoatLocationRenderer.BoatLocationRenderState> spawnLocationRenderState = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(Track.class);

    private final String id;
    private final boolean looped = true;
    private final List<TrackFeature> features = new ArrayList<>();
    private final List<OBUSettingsPacket> obuSettingsPackets;

    public Track(String id, Location spawnLocation, List<OBUSettingsPacket> obuSettingsPackets) {
        this.id = id;
        this.obuSettingsPackets = new ArrayList<>(obuSettingsPackets);

        addFeature(new SpawnLocation(spawnLocation.x(), spawnLocation.y(), spawnLocation.z(),
                (float) Math.toRadians(spawnLocation.yaw())));

        this.updateRenderState();
    }

    public void addFeature(TrackFeature feature) {
        this.features.add(feature);

        feature.onGizmoMoved(this.getGizmos().findFirst().get());

        this.updateRenderState();
    }

    public @Nullable TrackFeature removeFeature(TrackFeature feature) {
        if (feature == null)
            return null;

        feature.close();

        this.features.remove(feature);
        this.updateRenderState();

        return feature;
    }

    public void updateRenderState() {
        IceStomModClient.getInstance().lineCheckpointRenderer.setRenderState(lineCheckpointRenderState);
        IceStomModClient.getInstance().lineCheckpointTextRenderer.setRenderState(lineCheckpointRenderState);
        IceStomModClient.getInstance().cuboidCheckpointRenderer.setRenderState(cuboidCheckpointRenderState);
        IceStomModClient.getInstance().cuboidCheckpointTextRenderer.setRenderState(cuboidCheckpointRenderState);
        IceStomModClient.getInstance().gridRenderer.setRenderState(gridLocationRenderState);
        IceStomModClient.getInstance().gridTextRenderer.setRenderState(gridLocationRenderState);
        IceStomModClient.getInstance().spawnLocationRenderer.setRenderState(spawnLocationRenderState);

        this.getFeatures().forEach((trackFeature) -> {
            for (GizmoHandler.Gizmo gizmo : trackFeature.getGizmos()) {
                trackFeature.onGizmoMoved(gizmo);
            }
        });

        lineCheckpointRenderState.clear();
        cuboidCheckpointRenderState.clear();
        gridLocationRenderState.clear();
        spawnLocationRenderState.clear();

        boolean active = this == IceStomModClient.getInstance().trackHandler.getSelectedTrack();

        this.getGizmos().forEach(gizmo -> gizmo.setHidden(!active));

        IceStomModClient.getInstance().gizmoHandler.updateRenderState();

        for (TrackFeature feature : this.features) {
            if (feature instanceof LineCheckpoint lineCheckpoint) {
                lineCheckpointRenderState.add(lineCheckpoint.renderState());
            } else if (feature instanceof CuboidCheckpoint cuboidCheckpoint) {
                cuboidCheckpointRenderState.add(cuboidCheckpoint.renderState());
            } else if (feature instanceof GridLocation gridLocation) {
                gridLocationRenderState.add(gridLocation.renderState());
            } else if (feature instanceof SpawnLocation spawn) {
                spawnLocationRenderState.add(spawn.renderState());
            }
        }
    }

    public @Nullable Stream<GizmoHandler.Gizmo> getGizmos() {
        return this.features.stream().flatMap((feature) -> feature.getGizmos().stream());
    }

    public List<TrackFeature> getFeatures() {
        return this.features;
    }

    public List<OBUSettingsPacket> getOBUSettingsPackets() {
        return obuSettingsPackets;
    }

    public static Track fromTrackData(TrackFile trackData) {
        Track track = new Track(trackData.getId(), trackData.getSpawnLocation(),
                new ArrayList<>(trackData.getOpenBoatUtils()));
        int gridIndex = 0;

        for (Location gridLocation : trackData.getGrid()) {
            track.addFeature(new GridLocation(gridLocation.x(), gridLocation.y(), gridLocation.z(),
                    (float) ((int) Math.toRadians(gridLocation.yaw())), gridIndex++));
        }

        trackData.getCheckpoints().forEach((trackCheckpoint, index) -> {
            if (trackCheckpoint instanceof TrackCheckpoint.LineCheckpoint(io.gitlab.icestom.stomtrack.type.Vec2 a, io.gitlab.icestom.stomtrack.type.Vec2 b, double y, double height)) {
                track.addFeature(new LineCheckpoint(a.x(), a.y(), b.x(), b.y(), y, height, index));
            }
            if (trackCheckpoint instanceof TrackCheckpoint.CuboidCheckpoint(io.gitlab.icestom.stomtrack.type.Vec3 min, io.gitlab.icestom.stomtrack.type.Vec3 max)) {
                track.addFeature(new CuboidCheckpoint(min.x(), min.y(), min.z(), max.x(), max.y(), max.z(), index));
            }
        });
        track.updateRenderState();
        return track;
    }

    public String getId() {
        return this.id;
    }

    public TrackFile toTrackFile() {
        final Map<Integer, Location> gridLocations = new HashMap<>();
        final Map<TrackCheckpoint, Integer> checkpoints = new HashMap<>();

        Location spawnLocation = new Location(0, 0, 0, 0, 0);

        for (TrackFeature feature : this.features) {
            if (feature instanceof GridLocation gridLocation) {
                gridLocations.put(gridLocation.getIndex(), new Location(gridLocation.getX(), gridLocation.getY(),
                        gridLocation.getZ(), (float) Math.toDegrees(gridLocation.getR()), 0));
            } else if (feature instanceof LineCheckpoint lineCheckpoint) {
                checkpoints.put(
                        new TrackCheckpoint.LineCheckpoint(new Vec2(lineCheckpoint.getX1(), lineCheckpoint.getZ1()),
                                new Vec2(lineCheckpoint.getX2(), lineCheckpoint.getZ2()), lineCheckpoint.getY(),
                                lineCheckpoint.getHalfHeight() * 2),
                        lineCheckpoint.getIndex());
            } else if (feature instanceof CuboidCheckpoint cuboidCheckpoint) {
                checkpoints.put(
                        new TrackCheckpoint.CuboidCheckpoint(
                                new Vec3(cuboidCheckpoint.getX1(), cuboidCheckpoint.getY1(), cuboidCheckpoint.getZ1()),
                                new Vec3(cuboidCheckpoint.getX2(), cuboidCheckpoint.getY2(), cuboidCheckpoint.getZ2())),
                        cuboidCheckpoint.getIndex());
            } else if (feature instanceof SpawnLocation spawn) {
                spawnLocation = new Location(spawn.getX(), spawn.getY(), spawn.getZ(),
                        (float) Math.toDegrees(spawn.getR()), 0);
            }
        }

        final Location finalSpawnLocation = spawnLocation;
        return new TrackFile() {
            @Override
            public @NotNull String getId() {
                return id;
            }

            @Override
            public Component getName() {
                return Component.text(id);
            }

            @Override
            public boolean isLooped() {
                return looped;
            }

            @Override
            public @NotNull Location getSpawnLocation() {
                return finalSpawnLocation;
            }

            @Override
            public @NotNull Map<TrackCheckpoint, Integer> getCheckpoints() {
                return checkpoints;
            }

            @Override
            public @NotNull List<Location> getGrid() {
                return gridLocations.values().stream().toList();
            }

            @Override
            public @NotNull List<OBUSettingsPacket> getOpenBoatUtils() {
                return List.of();
            }
        };
    }
}
