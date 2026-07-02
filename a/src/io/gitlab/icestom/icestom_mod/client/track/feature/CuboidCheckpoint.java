package io.gitlab.icestom.icestom_mod.track.feature;

import io.gitlab.icestom.icestom_mod.client.GizmoHandler;
import io.gitlab.icestom.icestom_mod.render.renderes.CuboidCheckpointRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public non-sealed class CuboidCheckpoint extends TrackFeature {
   private final GizmoHandler.Gizmo min;
   private final GizmoHandler.Gizmo max;
   private final GizmoHandler.Gizmo mid;
   private double x1;
   private double y1;
   private double z1;
   private double x2;
   private double y2;
   private double z2;
   private int index;

   public double getX1() {
      return this.x1;
   }

   public double getY1() {
      return this.y1;
   }

   public double getZ1() {
      return this.z1;
   }

   public double getX2() {
      return this.x2;
   }

   public double getY2() {
      return this.y2;
   }

   public double getZ2() {
      return this.z2;
   }

   public int getIndex() {
      return this.index;
   }

   public CuboidCheckpoint(double x1, double y1, double z1, double x2, double y2, double z2, int index) {
      this.x1 = x1;
      this.y1 = y1;
      this.z1 = z1;
      this.x2 = x2;
      this.y2 = y2;
      this.z2 = z2;
      this.index = index;
      this.min = this.newGizmo(x1, y1, z1, 1.0F, 512.0F);
      this.max = this.newGizmo(x2, y2, z2, 1.0F, 512.0F);
      this.mid = this.newGizmo((x1 + x2) / (double)2.0F, (y1 + y2) / (double)2.0F, (z1 + z2) / (double)2.0F, 1.0F, 512.0F);
   }

   public CuboidCheckpointRenderer.CuboidCheckpointRenderState renderState() {
      return new CuboidCheckpointRenderer.CuboidCheckpointRenderState(this.x1, this.y1, this.z1, this.x2, this.y2, this.z2, this.index);
   }

   public void onGizmoMoved(GizmoHandler.Gizmo gizmo) {
      if (gizmo == this.mid) {
         double oldMidX = (this.x1 + this.x2) / (double)2.0F;
         double oldMidY = (this.y1 + this.y2) / (double)2.0F;
         double oldMidZ = (this.z1 + this.z2) / (double)2.0F;
         double dx = gizmo.getX() - oldMidX;
         double dy = gizmo.getY() - oldMidY;
         double dz = gizmo.getZ() - oldMidZ;
         this.min.setX(this.min.getX() + dx);
         this.min.setY(this.min.getY() + dy);
         this.min.setZ(this.min.getZ() + dz);
         this.max.setX(this.max.getX() + dx);
         this.max.setY(this.max.getY() + dy);
         this.max.setZ(this.max.getZ() + dz);
      }

      this.updateState();
      this.mid.setX((this.x1 + this.x2) / (double)2.0F);
      this.mid.setY((this.y1 + this.y2) / (double)2.0F);
      this.mid.setZ((this.z1 + this.z2) / (double)2.0F);
   }

   private void updateState() {
      this.x1 = Math.min(this.min.getX(), this.max.getX());
      this.y1 = Math.min(this.min.getY(), this.max.getY());
      this.z1 = Math.min(this.min.getZ(), this.max.getZ());
      this.x2 = Math.max(this.min.getX(), this.max.getX());
      this.y2 = Math.max(this.min.getY(), this.max.getY());
      this.z2 = Math.max(this.min.getZ(), this.max.getZ());
   }
}
