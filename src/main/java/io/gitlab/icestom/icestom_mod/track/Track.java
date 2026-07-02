package io.gitlab.icestom.icestom_mod.track;

import io.gitlab.icestom.icestom_mod.GizmoHandler;
import io.gitlab.icestom.icestom_mod.IceStomModClient;
import io.gitlab.icestom.icestom_mod.render.renderes.BoatLocationRenderer;
import io.gitlab.icestom.icestom_mod.render.renderes.CuboidCheckpointRenderer;
import io.gitlab.icestom.icestom_mod.render.renderes.GridLocationRenderer;
import io.gitlab.icestom.icestom_mod.render.renderes.LineCheckpointRenderer;
import io.gitlab.icestom.icestom_mod.track.feature.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import io.gitlab.icestom.stomtrack.TrackCheckpoint;
import io.gitlab.icestom.stomtrack.TrackFile;
import io.gitlab.icestom.stomtrack.type.Location;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.Line;

public class Track {
   private static final List<LineCheckpointRenderer.LineCheckpointRenderState> lineCheckpointRenderState = new ArrayList<>();
   private static final List<CuboidCheckpointRenderer.CuboidCheckpointRenderState> cuboidCheckpointRenderState = new ArrayList<>();
   private static final List<BoatLocationRenderer.BoatLocationRenderState> gridLocationRenderState = new ArrayList<>();
   private static final List<BoatLocationRenderer.BoatLocationRenderState> spawnLocationRenderState = new ArrayList<>();

   private static final Logger log = LoggerFactory.getLogger(Track.class);

   private String id;
   private boolean looped = true;
   private final List<TrackFeature> features = new ArrayList<>();

   public Track(String id, Location spawnLocation) {
      this.id = id;

      addFeature(new SpawnLocation(
              spawnLocation.x(),
              spawnLocation.y(),
              spawnLocation.z(),
              spawnLocation.yaw()
      ));

      this.updateRenderState();
   }

   public void addFeature(TrackFeature feature) {
      this.features.add(feature);

      feature.onGizmoMoved(this.getGizmos().findFirst().get());

      this.updateRenderState();
   }

   public @Nullable TrackFeature removeFeature(TrackFeature feature) {
      if (feature == null) return null;

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
         for(GizmoHandler.Gizmo gizmo : trackFeature.getGizmos()) {
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

   public static Track fromTrackData(TrackFile trackData) {
      Track track = new Track(trackData.getId(), trackData.getSpawnLocation());
      int gridIndex = 0;

      for(Location gridLocation : trackData.getGrid()) {
         track.addFeature(new GridLocation(
                 gridLocation.x(), gridLocation.y(), gridLocation.z(),
                 (float) ((int) Math.toRadians(gridLocation.yaw())),
                 gridIndex++
        ));
      }

      trackData.getCheckpoints().forEach((trackCheckpoint, index) -> {
         if (trackCheckpoint instanceof TrackCheckpoint.LineCheckpoint(
                 io.gitlab.icestom.stomtrack.type.Vec2 a,
                 io.gitlab.icestom.stomtrack.type.Vec2 b,
                 double y,
                 double height
         )) {
            track.addFeature(new LineCheckpoint(
                    a.x(), a.y(),
                    b.x(), b.y(),
                    y,
                    height,
                    index
            ));
         } if (trackCheckpoint instanceof TrackCheckpoint.CuboidCheckpoint(
                  io.gitlab.icestom.stomtrack.type.Vec3 min,
                  io.gitlab.icestom.stomtrack.type.Vec3 max
          )) {
            track.addFeature(new CuboidCheckpoint(
                    min.x(), min.y(), min.z(),
                    max.x(), max.y(), max.z(),
                    index
            ));
         }
      });
      track.updateRenderState();
      return track;
   }

   public String getId() {
      return this.id;
   }

//   public TrackData toTrackData() {
//      final Map<Integer, Pos> gridLocations = new HashMap();
//      final Map<Checkpoint, Integer> checkpoints = new HashMap();
//
//      for(TrackFeature feature : this.features) {
//         if (feature instanceof GridLocation gridLocation) {
//            gridLocations.put(gridLocation.getIndex(), new Pos(gridLocation.getX(), gridLocation.getY(), gridLocation.getZ(), (float)Math.toDegrees((double)gridLocation.getR()), 0.0F));
//         } else if (feature instanceof LineCheckpoint lineCheckpoint) {
//            checkpoints.put(new io.gitlab.icestom.icestom.track.checkpoint.LineCheckpoint(new Vec(lineCheckpoint.getX1(), lineCheckpoint.getY(), lineCheckpoint.getZ1()), new Vec(lineCheckpoint.getX2(), lineCheckpoint.getY(), lineCheckpoint.getZ2()), lineCheckpoint.getHalfHeight() * (double)2.0F), lineCheckpoint.getIndex());
//         } else if (feature instanceof CuboidCheckpoint cuboidCheckpoint) {
//            checkpoints.put(new io.gitlab.icestom.icestom.track.checkpoint.CuboidCheckpoint(new Vec(cuboidCheckpoint.getX1(), cuboidCheckpoint.getY1(), cuboidCheckpoint.getZ1()), new Vec(cuboidCheckpoint.getX2(), cuboidCheckpoint.getY2(), cuboidCheckpoint.getZ2())), cuboidCheckpoint.getIndex());
//         }
//      }
//
//      return new TrackData() {
//         public @NotNull String getId() {
//            return Track.this.id;
//         }
//
//         public @NotNull Component getName() {
//            return Component.text(Track.this.id);
//         }
//
//         public boolean isLooped() {
//            return Track.this.looped;
//         }
//
//         public @NotNull Pos getSpawnLocation() {
//            return Track.this.spawnLocation;
//         }
//
//         public @NotNull Map getCheckpoints() {
//            return checkpoints;
//         }
//
//         public @NotNull List getGridLocations() {
//            return gridLocations.values().stream().toList();
//         }
//
//         public @NotNull List getOpenBoatUtilsPackets() {
//            return List.of();
//         }
//      };
//   }
}
