package io.gitlab.icestom.icestom_mod.track.feature;

import io.gitlab.icestom.icestom_mod.client.GizmoHandler;
import io.gitlab.icestom.icestom_mod.client.IceStomModClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract sealed class TrackFeature implements AutoCloseable permits GridLocation, LineCheckpoint, CuboidCheckpoint {
   private final GizmoHandler gizmoHandler = IceStomModClient.getInstance().getGizmoHandler();
   private final List gizmos = new ArrayList();

   protected GizmoHandler.Gizmo newGizmo(double x, double y, double z, float scale, float drawDistance) {
      GizmoHandler.Gizmo gizmo = this.gizmoHandler.newGizmo(x, y, z, scale, drawDistance);
      this.gizmos.add(gizmo);
      return gizmo;
   }

   public void close() {
      List var10000 = this.gizmos;
      GizmoHandler var10001 = this.gizmoHandler;
      Objects.requireNonNull(var10001);
      var10000.forEach(var10001::removeGizmo);
      this.gizmos.clear();
   }

   public List getGizmos() {
      return this.gizmos;
   }

   public abstract void onGizmoMoved(GizmoHandler.Gizmo var1);
}
