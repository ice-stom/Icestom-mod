package io.gitlab.icestom.icestom_mod.track;

import io.gitlab.icestom.icestom.track.checkpoint.Checkpoint;
import io.gitlab.icestom.icestom.track.format.TrackData;
import io.gitlab.icestom.icestom_mod.client.GizmoHandler;
import io.gitlab.icestom.icestom_mod.client.IceStomModClient;
import io.gitlab.icestom.icestom_mod.track.feature.CuboidCheckpoint;
import io.gitlab.icestom.icestom_mod.track.feature.GridLocation;
import io.gitlab.icestom.icestom_mod.track.feature.LineCheckpoint;
import io.gitlab.icestom.icestom_mod.track.feature.TrackFeature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class Track {
   private static final List lineCheckpointRenderState = new ArrayList();
   private static final List cuboidCheckpointRenderState = new ArrayList();
   private static final List gridLocationRenderState = new ArrayList();
   private static final Logger log = LoggerFactory.getLogger(Track.class);
   private String id;
   private boolean looped = true;
   private Pos spawnLocation;
   private final List features = new ArrayList();

   public Track(String id, Pos spawnLocation) {
      this.id = id;
      this.spawnLocation = spawnLocation;
      this.updateRenderState();
   }

   public void addFeature(TrackFeature feature) {
      this.features.add(feature);
      feature.onGizmoMoved((GizmoHandler.Gizmo)this.getGizmos().findFirst().get());
      this.updateRenderState();
   }

   public @Nullable TrackFeature removeFeature(TrackFeature feature) {
      if (feature == null) {
         return null;
      } else {
         feature.close();
         this.features.remove(feature);
         this.updateRenderState();
         return feature;
      }
   }

   public void updateRenderState() {
      IceStomModClient.getInstance().lineCheckpointRenderer.setRenderState(lineCheckpointRenderState);
      IceStomModClient.getInstance().lineCheckpointTextRenderer.setRenderState(lineCheckpointRenderState);
      IceStomModClient.getInstance().cuboidCheckpointRenderer.setRenderState(cuboidCheckpointRenderState);
      IceStomModClient.getInstance().cuboidCheckpointTextRenderer.setRenderState(cuboidCheckpointRenderState);
      IceStomModClient.getInstance().gridRenderer.setRenderState(gridLocationRenderState);
      IceStomModClient.getInstance().gridTextRenderer.setRenderState(gridLocationRenderState);
      this.getFeatures().forEach((trackFeature) -> {
         for(GizmoHandler.Gizmo gizmo : trackFeature.getGizmos()) {
            trackFeature.onGizmoMoved(gizmo);
         }

      });
      lineCheckpointRenderState.clear();
      cuboidCheckpointRenderState.clear();
      gridLocationRenderState.clear();
      boolean active = this == IceStomModClient.getInstance().trackHandler.getSelectedTrack();
      ((Stream)Objects.requireNonNull(this.getGizmos())).forEach((gizmo) -> gizmo.setHidden(!active));
      IceStomModClient.getInstance().gizmoHandler.updateRenderState();

      for(TrackFeature feature : this.features) {
         if (feature instanceof LineCheckpoint lineCheckpoint) {
            lineCheckpointRenderState.add(lineCheckpoint.renderState());
         } else if (feature instanceof CuboidCheckpoint cuboidCheckpoint) {
            cuboidCheckpointRenderState.add(cuboidCheckpoint.renderState());
         } else if (feature instanceof GridLocation gridLocation) {
            gridLocationRenderState.add(gridLocation.renderState());
         }
      }

   }

   public @Nullable Stream getGizmos() {
      return this.features.stream().flatMap((feature) -> feature.getGizmos().stream());
   }

   public List getFeatures() {
      return this.features;
   }

   public static Track fromTrackData(TrackData trackData) {
      Track track = new Track(trackData.getId(), trackData.getSpawnLocation());
      int gridIndex = 0;

      for(Pos gridLocation : trackData.getGridLocations()) {
         track.addFeature(new GridLocation(gridLocation.x(), gridLocation.y(), gridLocation.z(), (float)((int)Math.toRadians((double)gridLocation.yaw())), gridIndex++));
      }

      trackData.getCheckpoints().forEach((checkpoint, index) -> {
         if (checkpoint instanceof io.gitlab.icestom.icestom.track.checkpoint.LineCheckpoint lineCheckpoint) {
            Vec a = lineCheckpoint.getA();
            Vec b = lineCheckpoint.getB();
            track.addFeature(new LineCheckpoint(a.x(), a.z(), b.x(), b.z(), Math.min(a.y(), b.y()), lineCheckpoint.getHeight(), index));
         } else if (checkpoint instanceof io.gitlab.icestom.icestom.track.checkpoint.CuboidCheckpoint cuboidCheckpoint) {
            Vec a = cuboidCheckpoint.getA();
            Vec b = cuboidCheckpoint.getB();
            track.addFeature(new CuboidCheckpoint(a.x(), a.y(), a.z(), b.x(), b.y(), b.z(), index));
         } else {
            log.warn("Unknown checkpoint type {}", checkpoint.getClass().getSimpleName());
         }

      });
      track.updateRenderState();
      return track;
   }

   public String getId() {
      return this.id;
   }

   public TrackData toTrackData() {
      final Map<Integer, Pos> gridLocations = new HashMap();
      final Map<Checkpoint, Integer> checkpoints = new HashMap();

      for(TrackFeature feature : this.features) {
         if (feature instanceof GridLocation gridLocation) {
            gridLocations.put(gridLocation.getIndex(), new Pos(gridLocation.getX(), gridLocation.getY(), gridLocation.getZ(), (float)Math.toDegrees((double)gridLocation.getR()), 0.0F));
         } else if (feature instanceof LineCheckpoint lineCheckpoint) {
            checkpoints.put(new io.gitlab.icestom.icestom.track.checkpoint.LineCheckpoint(new Vec(lineCheckpoint.getX1(), lineCheckpoint.getY(), lineCheckpoint.getZ1()), new Vec(lineCheckpoint.getX2(), lineCheckpoint.getY(), lineCheckpoint.getZ2()), lineCheckpoint.getHalfHeight() * (double)2.0F), lineCheckpoint.getIndex());
         } else if (feature instanceof CuboidCheckpoint cuboidCheckpoint) {
            checkpoints.put(new io.gitlab.icestom.icestom.track.checkpoint.CuboidCheckpoint(new Vec(cuboidCheckpoint.getX1(), cuboidCheckpoint.getY1(), cuboidCheckpoint.getZ1()), new Vec(cuboidCheckpoint.getX2(), cuboidCheckpoint.getY2(), cuboidCheckpoint.getZ2())), cuboidCheckpoint.getIndex());
         }
      }

      return new TrackData() {
         public @NotNull String getId() {
            return Track.this.id;
         }

         public @NotNull Component getName() {
            return Component.text(Track.this.id);
         }

         public boolean isLooped() {
            return Track.this.looped;
         }

         public @NotNull Pos getSpawnLocation() {
            return Track.this.spawnLocation;
         }

         public @NotNull Map getCheckpoints() {
            return checkpoints;
         }

         public @NotNull List getGridLocations() {
            return gridLocations.values().stream().toList();
         }

         public @NotNull List getOpenBoatUtilsPackets() {
            return List.of();
         }
      };
   }
}
