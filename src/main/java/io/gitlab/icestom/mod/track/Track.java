package io.gitlab.icestom.mod.track;

import io.gitlab.icestom.mod.GizmoHandler;
import io.gitlab.icestom.mod.IceStomModClient;
import io.gitlab.icestom.mod.render.renderers.FeatureLabelRenderer;
import io.gitlab.icestom.mod.render.renderers.generic.BoatLocationRenderer;
import io.gitlab.icestom.mod.render.renderers.generic.CuboidFeatureRenderer;
import io.gitlab.icestom.mod.render.renderers.generic.LineFeatureRenderer;
import io.gitlab.icestom.mod.render.renderers.states.LineSegmentRenderableRecord;
import io.gitlab.icestom.mod.track.feature.*;
import io.gitlab.icestom.mod.track.feature.checkpoint.CuboidCheckpoint;
import io.gitlab.icestom.mod.track.feature.checkpoint.LineCheckpoint;
import io.gitlab.icestom.mod.track.feature.checkpoint.TrackCheckpoint;
import io.gitlab.icestom.mod.track.feature.location.GridLocation;
import io.gitlab.icestom.mod.track.feature.location.SpawnLocation;
import io.gitlab.icestom.mod.track.feature.location.TrackLocation;
import io.gitlab.icestom.mod.track.feature.region.CuboidRegion;
import io.gitlab.icestom.mod.track.feature.region.TrackRegion;
import io.gitlab.icestom.mod.track.feature.trigger.LineTrigger;
import io.gitlab.icestom.mod.track.feature.trigger.TrackTrigger;
import io.gitlab.icestom.stomtrack.TrackFile;
import io.gitlab.icestom.stomtrack.shadow.io.github.openboatutils.protocol.channels.OBUSettingsPacket;
import io.gitlab.icestom.stomtrack.shadow.net.kyori.adventure.text.Component;
import io.gitlab.icestom.stomtrack.type.Location;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import io.gitlab.icestom.stomtrack.type.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Track {

    private final TrackRenderStateProvider renderState = new TrackRenderStateProvider();

    private static final Logger log = LoggerFactory.getLogger(Track.class);

    private final String id;
    private boolean looped;
    private final Map<String, String> tags;
    private final List<TrackFeature<?>> features = new ArrayList<>();
    private final List<OBUSettingsPacket> obuSettingsPackets;

    public Track(String id, Location spawnLocation, boolean looped, Map<String, String> tags, List<OBUSettingsPacket> obuSettingsPackets) {
        this.id = id;
        this.looped = looped;
        this.tags = new HashMap<>(tags);
        this.obuSettingsPackets = new ArrayList<>(obuSettingsPackets);

        addFeature(new SpawnLocation(spawnLocation.x(), spawnLocation.y(), spawnLocation.z(),
                (float) Math.toRadians(spawnLocation.yaw())));
    }

    public void setLooped(boolean looped) {
        this.looped = looped;
    }

    public void addFeature(TrackFeature<?> feature) {
        this.features.add(feature);

        feature.onGizmoMoved(this.getGizmos().findFirst().get());

        this.updateRenderState();
    }

    public @Nullable TrackFeature<?> removeFeature(TrackFeature<?> feature) {
        if (feature == null)
            return null;

        feature.close();

        this.features.remove(feature);
        this.updateRenderState();

        return feature;
    }

    public TrackRenderStateProvider getRenderState() {
        return renderState;
    }

    public void updateRenderState() {

        this.getFeatures().forEach((trackFeature) -> {
            for (GizmoHandler.Gizmo gizmo : trackFeature.getGizmos()) {
                trackFeature.onGizmoMoved(gizmo);
            }
        });


        boolean active = this == IceStomModClient.getInstance().trackHandler.getSelectedTrack();

        this.getGizmos().forEach(gizmo -> gizmo.setHidden(!active));

        IceStomModClient.getInstance().gizmoHandler.updateRenderState();

        renderState.apply();
        renderState.clear();

        Map<String, Vec3> locations = new HashMap<>();

        for (TrackFeature<?> feature : this.features) {
            if (feature instanceof TrackLocation location) {
                locations.put(location.getTag(), new Vec3(location.getX(), location.getY(), location.getZ()));
            }
        }

        int index = 0;
        for (TrackFeature<?> feature : this.features) {

            FeatureLabelRenderer.FeatureRenderable renderState = feature.getRenderState(index++);

            if (active) this.renderState.featureRenderable.add(renderState);

            if (feature instanceof TrackCheckpoint<?> checkpoint) {
                SpawnLocation spawnLocation = (SpawnLocation) features.stream().filter(trackFeature -> trackFeature instanceof SpawnLocation).findFirst().get();

                Vec3 reset_pos = new Vec3(
                        spawnLocation.getX(),
                        spawnLocation.getY(),
                        spawnLocation.getZ()
                );

                for (int i = checkpoint.getIndex(); i > 0; i--) {
                    Vec3 location = locations.get("icestom.reset_" + i);

                    if (location != null) {
                        reset_pos = location;

                        break;
                    }
                }

                if (active) this.renderState.tetherRenderables.add(
                        new LineSegmentRenderableRecord(
                                "icestom.reset",
                                renderState.x(),
                                renderState.y(),
                                renderState.z(),
                                reset_pos.x(),
                                reset_pos.y(),
                                reset_pos.z()
                        )
                );
            }

            if (feature instanceof TrackTrigger<?> || feature instanceof TrackRegion<?>) {
                final Map<String, Function<String, List<String>>> tpPrefixes = Map.of(
                        "icestom.reset_", s -> List.of("icestom.reset_" + s),
                        "icestom.teleport_", List::of
                );

                for (String tag : ((MultipleTaggedFeature) feature).getTags()) {
                    for (Map.Entry<String, Function<String, List<String>>> entry : tpPrefixes.entrySet()) {
                        String prefix = entry.getKey();

                        if (!tag.startsWith(prefix)) {
                            continue;
                        }

                        Function<String, List<String>> fn = entry.getValue();
                        List<String> lookFor = fn.apply(tag.substring(prefix.length()));

                        for (String location : lookFor) {
                            locations.computeIfPresent(location, (ignored, vec) -> {
                                if (active) this.renderState.tetherRenderables.add(
                                        new LineSegmentRenderableRecord(
                                                tag,
                                                renderState.x(),
                                                renderState.y(),
                                                renderState.z(),
                                                vec.x(),
                                                vec.y(),
                                                vec.z()
                                        )
                                );

                                return vec;
                            });
                        }

                        break;
                    }
                }
            }

            if (renderState instanceof CuboidFeatureRenderer.CuboidRenderable cuboidRenderable) {
                this.renderState.cuboidRenderables.add(cuboidRenderable);
            }

            if (renderState instanceof LineFeatureRenderer.LineRenderable lineRenderable) {
                this.renderState.lineRenderables.add(lineRenderable);
            }

            if (renderState instanceof BoatLocationRenderer.BoatLocationRenderable boatLocationRenderable) {
                this.renderState.boatLocationRenderables.add(boatLocationRenderable);
            }
        }
    }

    public @Nullable Stream<GizmoHandler.Gizmo> getGizmos() {
        return this.features.stream().flatMap((feature) -> feature.getGizmos().stream());
    }

    public List<TrackFeature<?>> getFeatures() {
        return this.features;
    }

    public List<OBUSettingsPacket> getOBUSettingsPackets() {
        return obuSettingsPackets;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public boolean isLooped() {
        return looped;
    }

    public static Track fromTrackData(TrackFile trackData) {
        Track track = new Track(
                trackData.getId(),
                trackData.getSpawnLocation(),
                trackData.isLooped(),
                trackData.getTags(),
                new ArrayList<>(trackData.getOpenBoatUtils()));
        int gridIndex = 0;

        for (Location gridLocation : trackData.getGrid()) {
            track.addFeature(new GridLocation(gridLocation.x(), gridLocation.y(), gridLocation.z(),
                    (float) (Math.toRadians(gridLocation.yaw())), gridIndex++));
        }

        trackData.getCheckpoints().forEach((trackCheckpoint, index) -> {
            if (trackCheckpoint instanceof io.gitlab.icestom.stomtrack.TrackCheckpoint.LineCheckpoint(io.gitlab.icestom.stomtrack.type.Vec2 a, io.gitlab.icestom.stomtrack.type.Vec2 b, double y, double height)) {
                track.addFeature(new LineCheckpoint(a.x(), a.y(), b.x(), b.y(), y, height, index));
            }
            if (trackCheckpoint instanceof io.gitlab.icestom.stomtrack.TrackCheckpoint.CuboidCheckpoint(io.gitlab.icestom.stomtrack.type.Vec3 min, io.gitlab.icestom.stomtrack.type.Vec3 max)) {
                track.addFeature(new CuboidCheckpoint(min.x(), min.y(), min.z(), max.x(), max.y(), max.z(), index));
            }
        });

        trackData.getTriggers().forEach((trigger, tags) -> {
            if (trigger instanceof io.gitlab.icestom.stomtrack.TrackTrigger.LineTrigger(io.gitlab.icestom.stomtrack.type.Vec2 a, io.gitlab.icestom.stomtrack.type.Vec2 b, double y, double height)) {
                track.addFeature(new LineTrigger(a.x(), a.y(), b.x(), b.y(), y, height, tags));
            }
        });

        trackData.getRegions().forEach((region, tags) -> {
            if (region instanceof io.gitlab.icestom.stomtrack.TrackRegion.CuboidRegion(Vec3 min, Vec3 max)) {
                track.addFeature(new CuboidRegion(min.x(), min.y(), min.z(), max.x(), max.y(), max.z(), tags));
            }
        });

        trackData.getLocations().forEach((tag, location) -> {
            track.addFeature(new TrackLocation(
                    location.x(),
                    location.y(),
                    location.z(),
                    (float) Math.toRadians(location.yaw()),
                    tag
            ));
        });

        track.updateRenderState();
        return track;
    }

    public String getId() {
        return this.id;
    }

    public int getMaxIndexOfFeature(Class<? extends IndexedFeature> superclass, int fallback) {
        return features.stream()
                .filter(trackFeature -> superclass.isAssignableFrom(trackFeature.getClass()))
                .map(trackFeature -> ((IndexedFeature) trackFeature).getIndex())
                .max(Integer::compareTo)
                .orElse(fallback);
    }

    public TrackFile toTrackFile() {
        final Map<Integer, Location> gridLocations = new HashMap<>();
        final Map<io.gitlab.icestom.stomtrack.TrackCheckpoint, Integer> checkpoints = new HashMap<>();

        final Map<io.gitlab.icestom.stomtrack.TrackRegion, Set<String>> regions = new HashMap<>();
        final Map<io.gitlab.icestom.stomtrack.TrackTrigger, Set<String>> triggers = new HashMap<>();
        final Map<String, Location> locations = new HashMap<>();

        Location spawnLocation = new Location(0, 0, 0, 0, 0);

        for (TrackFeature<?> feature : this.features) {
            if (feature instanceof GridLocation gridLocation) {
                gridLocations.put(gridLocation.getIndex(), new Location(gridLocation.getX(), gridLocation.getY(),
                        gridLocation.getZ(), (float) Math.toDegrees(gridLocation.getR()), 0));
            } else if (feature instanceof TrackCheckpoint<?> checkpoint) {
                checkpoints.put(checkpoint.toEncoded(), checkpoint.getIndex());
            } else if (feature instanceof SpawnLocation spawn) {
                spawnLocation = new Location(spawn.getX(), spawn.getY(), spawn.getZ(),
                        (float) Math.toDegrees(spawn.getR()), 0);
            } else if (feature instanceof TrackLocation trackLocation) {
                locations.put(trackLocation.getTag(), new Location(
                        trackLocation.getX(),
                        trackLocation.getY(),
                        trackLocation.getZ(),
                        (float) Math.toDegrees(trackLocation.getR()),
                        0
                ));
            } else if (feature instanceof TrackTrigger<?> trigger) {
                triggers.put(trigger.toEncoded(), trigger.getTags());
            } else if (feature instanceof TrackRegion<?> region) {
                regions.put(region.toEncoded(), region.getTags());
            }
        }

        final Location finalSpawnLocation = spawnLocation;
        return new TrackFile() {

            @Override
            public @NotNull String getId() {
                return id;
            }

            @Override
            public @NonNull Component getName() {
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
            public @NotNull Map<String, String> getTags() { return tags; }

            @Override
            public @NotNull Map<io.gitlab.icestom.stomtrack.TrackCheckpoint, Integer> getCheckpoints() { return checkpoints; }

            @Override
            public @NotNull List<Location> getGrid() {
                return gridLocations.values().stream().toList();
            }

            @Override
            public @NotNull List<OBUSettingsPacket> getOpenBoatUtils() {
                return obuSettingsPackets;
            }

            @Override
            public @NotNull Map<io.gitlab.icestom.stomtrack.TrackRegion, Set<String>> getRegions() { return regions; }

            @Override
            public @NotNull Map<String, Location> getLocations() { return locations; }

            @Override
            public @NotNull Map<io.gitlab.icestom.stomtrack.TrackTrigger, Set<String>> getTriggers() { return triggers; };
        };
    }
}
