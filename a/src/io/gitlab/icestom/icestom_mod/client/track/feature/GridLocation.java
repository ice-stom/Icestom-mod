package io.gitlab.icestom.icestom_mod.track.feature;

import io.gitlab.icestom.icestom_mod.client.GizmoHandler;
import io.gitlab.icestom.icestom_mod.render.renderes.GridLocationRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public non-sealed class GridLocation extends TrackFeature {
   private final GizmoHandler.Gizmo main;
   private final GizmoHandler.Gizmo rotation;
   private double x;
   private double y;
   private double z;
   private float r;
   private final int index;

   public GridLocation(double x, double y, double z, float r, int index) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.r = r;
      this.index = index;
      this.main = this.newGizmo(this.x, this.y, this.z, 1.0F, 512.0F);
      this.rotation = this.newGizmo(this.x, this.y, this.z, 0.5F, 6.0F);
      this.rotation.setX(this.x + Math.cos((double)r) * (double)2.0F);
      this.rotation.setZ(this.z + Math.sin((double)r) * (double)2.0F);
   }

   public GridLocationRenderer.GridLocationRenderState renderState() {
      return new GridLocationRenderer.GridLocationRenderState(this.x, this.y, this.z, this.r, this.index);
   }

   public void onGizmoMoved(GizmoHandler.Gizmo gizmo) {
      if (gizmo == this.main) {
         this.rotation.setY(gizmo.getY());
         this.rotation.setX(gizmo.getX() + Math.cos((double)this.r) * (double)2.0F);
         this.rotation.setZ(gizmo.getZ() + Math.sin((double)this.r) * (double)2.0F);
      } else if (gizmo == this.rotation) {
         double dx = this.rotation.getX() - this.main.getX();
         double dz = this.rotation.getZ() - this.main.getZ();
         double len = Math.sqrt(dx * dx + dz * dz);
         this.rotation.setX(this.main.getX() + dx / len * (double)2.0F);
         this.rotation.setZ(this.main.getZ() + dz / len * (double)2.0F);
         this.rotation.setY(this.main.getY());
      }

      this.updateState();
   }

   private void updateState() {
      this.x = this.main.getX();
      this.y = this.main.getY();
      this.z = this.main.getZ();
      this.r = (float)Math.atan2(this.rotation.getZ() - this.main.getZ(), this.rotation.getX() - this.main.getX());
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public float getR() {
      return this.r;
   }

   public int getIndex() {
      return this.index;
   }
}
