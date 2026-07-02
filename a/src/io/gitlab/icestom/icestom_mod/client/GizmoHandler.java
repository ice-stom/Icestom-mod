package io.gitlab.icestom.icestom_mod.client;

import io.gitlab.icestom.icestom_mod.render.renderes.GizmoRenderer;
import io.gitlab.icestom.icestom_mod.track.Track;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_310;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class GizmoHandler {
   private final List gizmos = new ArrayList();
   private final List renderStates = new ArrayList();
   private @Nullable Gizmo selectedGizmo;
   private @Nullable DragOperation dragOperation;

   public Gizmo newGizmo(double x, double y, double z, float scale, float drawDistance) {
      Gizmo gizmo = new Gizmo(x, y, z, scale, drawDistance);
      this.gizmos.add(gizmo);
      this.updateRenderState();
      return gizmo;
   }

   public void removeGizmo(Gizmo gizmo) {
      this.gizmos.remove(gizmo);
      if (this.selectedGizmo == gizmo) {
         this.selectedGizmo = null;
      }

      if (this.dragOperation != null && this.dragOperation.gizmo() == gizmo) {
         this.dragOperation = null;
      }

      this.updateRenderState();
   }

   private @Nullable DragOperation getBestPotentialDrag(class_243 origin, class_243 direction) {
      Track track = IceStomModClient.getInstance().trackHandler.getSelectedTrack();
      return track == null ? null : (DragOperation)((Stream)Objects.requireNonNull(track.getGizmos())).map((gizmo) -> {
         Gizmo.GizmoHandleCandidate hit = gizmo.getHandle(origin, direction);
         return hit == null ? null : Map.entry(gizmo, hit);
      }).filter(Objects::nonNull).min(Comparator.comparingDouble((e) -> ((Gizmo.GizmoHandleCandidate)e.getValue()).distance())).map((e) -> {
         Gizmo gizmo = (Gizmo)e.getKey();
         Gizmo.GizmoHandle handle = ((Gizmo.GizmoHandleCandidate)e.getValue()).handle();
         class_243 normal = handle == GizmoHandler.Gizmo.GizmoHandle.FREE ? null : computeAxisPlaneNormal(handle, origin, gizmo.getPos());
         double radius = origin.method_1022(gizmo.getPos());
         return new DragOperation(gizmo, handle, gizmo.getPos(), computeStartHit(gizmo, handle, origin, direction, normal, radius), normal, radius);
      }).orElse((Object)null);
   }

   public boolean mouseDown(class_243 origin, class_243 direction) {
      DragOperation candidate = this.getBestPotentialDrag(origin, direction);
      if (candidate == null) {
         this.setSelectedGizmo((Gizmo)null);
         this.dragOperation = null;
         this.updateRenderState();
         return false;
      } else {
         this.setSelectedGizmo(candidate.gizmo());
         this.dragOperation = candidate;
         this.updateRenderState();
         return true;
      }
   }

   public boolean mouseUp() {
      if (this.dragOperation == null) {
         return false;
      } else {
         this.dragOperation = null;
         this.updateRenderState();
         return true;
      }
   }

   public void tick(float dt) {
      IceStomModClient.getInstance().gizmoRenderer.setRenderState(this.renderStates);
      class_310 minecraft = class_310.method_1551();
      if (this.dragOperation != null) {
         assert minecraft.field_1724 != null;

         class_243 origin = minecraft.field_1724.method_5836(dt);
         class_243 direction = minecraft.field_1724.method_5828(dt);
         this.dragOperation.apply(origin, direction);
         this.updateRenderState();
      }
   }

   public void setSelectedGizmo(@Nullable Gizmo selectedGizmo) {
      this.selectedGizmo = selectedGizmo;
      this.updateRenderState();
   }

   public void updateRenderState() {
      this.renderStates.clear();
      GizmoRenderer gizmoRenderer = IceStomModClient.getInstance().gizmoRenderer;
      gizmoRenderer.setFocusedX(this.dragOperation != null && this.dragOperation.handle.focusesX());
      gizmoRenderer.setFocusedY(this.dragOperation != null && this.dragOperation.handle.focusesY());
      gizmoRenderer.setFocusedZ(this.dragOperation != null && this.dragOperation.handle.focusesZ());
      gizmoRenderer.setFocused(this.dragOperation != null);

      for(Gizmo gizmo : this.gizmos) {
         this.renderStates.add(gizmo.getRenderState(gizmo == this.selectedGizmo));
      }

   }

   private static class_243 computeStartHit(Gizmo gizmo, Gizmo.GizmoHandle handle, class_243 origin, class_243 dir, @Nullable class_243 axisPlaneNormal, double freeDragRadius) {
      class_243 var10000;
      switch (handle.ordinal()) {
         case 0:
         case 1:
         case 2:
            if (axisPlaneNormal == null) {
               var10000 = origin;
            } else {
               class_243 hit = intersectRayPlane(origin, dir, gizmo.getPos(), axisPlaneNormal);
               var10000 = hit == null ? origin : hit;
            }
            break;
         case 3:
            var10000 = intersectRayPlane(origin, dir, gizmo.getPos(), new class_243((double)1.0F, (double)0.0F, (double)0.0F));
            break;
         case 4:
            var10000 = intersectRayPlane(origin, dir, gizmo.getPos(), new class_243((double)0.0F, (double)1.0F, (double)0.0F));
            break;
         case 5:
            var10000 = intersectRayPlane(origin, dir, gizmo.getPos(), new class_243((double)0.0F, (double)0.0F, (double)1.0F));
            break;
         case 6:
            class_243 hit = intersectRaySphere(origin, dir, origin, freeDragRadius);
            var10000 = hit == null ? gizmo.getPos() : hit;
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   private static @Nullable class_243 computeAxisPlaneNormal(Gizmo.GizmoHandle handle, class_243 rayOrigin, class_243 gizmoPos) {
      if (handle == GizmoHandler.Gizmo.GizmoHandle.FREE) {
         return null;
      } else {
         class_243 var10000;
         switch (handle.ordinal()) {
            case 0 -> var10000 = new class_243((double)1.0F, (double)0.0F, (double)0.0F);
            case 1 -> var10000 = new class_243((double)0.0F, (double)1.0F, (double)0.0F);
            case 2 -> var10000 = new class_243((double)0.0F, (double)0.0F, (double)1.0F);
            default -> var10000 = null;
         }

         class_243 axis = var10000;
         if (axis == null) {
            return null;
         } else {
            class_243 toCamera = rayOrigin.method_1020(gizmoPos);
            if (toCamera.method_1027() < 1.0E-6) {
               return null;
            } else {
               class_243 n = axis.method_1036(toCamera).method_1036(axis);
               if (n.method_1027() < 1.0E-6) {
                  class_243 fallback = Math.abs(axis.field_1351) < 0.9 ? new class_243((double)0.0F, (double)1.0F, (double)0.0F) : new class_243((double)1.0F, (double)0.0F, (double)0.0F);
                  n = axis.method_1036(fallback);
               }

               return n.method_1029();
            }
         }
      }
   }

   private static @Nullable class_243 intersectRaySphere(class_243 rayOrigin, class_243 rayDir, class_243 sphereCenter, double radius) {
      class_243 oc = rayOrigin.method_1020(sphereCenter);
      double b = oc.method_1026(rayDir);
      double c = oc.method_1026(oc) - radius * radius;
      double discriminant = b * b - c;
      if (discriminant < (double)0.0F) {
         return null;
      } else {
         double sqrtD = Math.sqrt(discriminant);
         double t1 = -b - sqrtD;
         double t2 = -b + sqrtD;
         double t = t1 >= (double)0.0F ? t1 : t2;
         return t < (double)0.0F ? null : rayOrigin.method_1019(rayDir.method_1021(t));
      }
   }

   private static @Nullable class_243 intersectRayPlane(class_243 rayOrigin, class_243 rayDir, class_243 planePoint, class_243 planeNormal) {
      double denom = planeNormal.method_1026(rayDir);
      if (Math.abs(denom) < 1.0E-6) {
         return null;
      } else {
         double t = planePoint.method_1020(rayOrigin).method_1026(planeNormal) / denom;
         return t < (double)0.0F ? null : rayOrigin.method_1019(rayDir.method_1021(t));
      }
   }

   private static double distanceFromRayToPoint(class_243 rayOrigin, class_243 rayDir, class_243 point) {
      class_243 w = point.method_1020(rayOrigin);
      double t = w.method_1026(rayDir);
      if (t < (double)0.0F) {
         return Double.MAX_VALUE;
      } else {
         class_243 proj = rayOrigin.method_1019(rayDir.method_1021(t));
         return proj.method_1022(point);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Gizmo {
      private double x;
      private double y;
      private double z;
      private float scale;
      private float drawDistance;
      private boolean hidden;

      public Gizmo(double x, double y, double z, float scale, float drawDistance) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.scale = scale;
         this.drawDistance = drawDistance;
      }

      public void setX(double x) {
         this.x = x;
      }

      public void setY(double y) {
         this.y = y;
      }

      public void setZ(double z) {
         this.z = z;
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

      public class_243 getPos() {
         return new class_243(this.x, this.y, this.z);
      }

      private GizmoRenderer.GizmoRenderState getRenderState(boolean selected) {
         return new GizmoRenderer.GizmoRenderState(this.x, this.y, this.z, this.scale, this.drawDistance, selected, this.hidden);
      }

      public @Nullable GizmoHandleCandidate getHandle(class_243 origin, class_243 dir) {
         List<GizmoHandleCandidate> candidates = new ArrayList();

         for(GizmoHandle handle : GizmoHandler.Gizmo.GizmoHandle.values()) {
            double distance = GizmoHandler.distanceFromRayToPoint(origin, dir, handle.getHandlePos(this));
            if (distance < 0.3 * (double)this.scale) {
               candidates.add(new GizmoHandleCandidate(handle, distance));
            }
         }

         return (GizmoHandleCandidate)candidates.stream().min(Comparator.comparingDouble(GizmoHandleCandidate::distance)).orElse((Object)null);
      }

      public void setHidden(boolean hidden) {
         this.hidden = hidden;
      }

      @Environment(EnvType.CLIENT)
      public static record GizmoHandleCandidate(GizmoHandle handle, double distance) {
      }

      @Environment(EnvType.CLIENT)
      public static enum GizmoHandle {
         X,
         Y,
         Z,
         X_PLANE,
         Y_PLANE,
         Z_PLANE,
         FREE;

         public class_243 getHandlePos(Gizmo gizmo) {
            class_243 var10000;
            switch (this.ordinal()) {
               case 0 -> var10000 = gizmo.getPos().method_1031((double)gizmo.scale, (double)0.0F, (double)0.0F);
               case 1 -> var10000 = gizmo.getPos().method_1031((double)0.0F, (double)gizmo.scale, (double)0.0F);
               case 2 -> var10000 = gizmo.getPos().method_1031((double)0.0F, (double)0.0F, (double)gizmo.scale);
               case 3 -> var10000 = gizmo.getPos().method_1031((double)0.0F, (double)0.5F * (double)gizmo.scale, (double)0.5F * (double)gizmo.scale);
               case 4 -> var10000 = gizmo.getPos().method_1031((double)0.5F * (double)gizmo.scale, (double)0.0F, (double)0.5F * (double)gizmo.scale);
               case 5 -> var10000 = gizmo.getPos().method_1031((double)0.5F * (double)gizmo.scale, (double)0.5F * (double)gizmo.scale, (double)0.0F);
               case 6 -> var10000 = gizmo.getPos();
               default -> throw new MatchException((String)null, (Throwable)null);
            }

            return var10000;
         }

         public boolean focusesX() {
            boolean var10000;
            switch (this.ordinal()) {
               case 0:
               case 4:
               case 5:
               case 6:
                  var10000 = true;
                  break;
               case 1:
               case 2:
               case 3:
               default:
                  var10000 = false;
            }

            return var10000;
         }

         public boolean focusesY() {
            boolean var10000;
            switch (this.ordinal()) {
               case 1:
               case 3:
               case 5:
               case 6:
                  var10000 = true;
                  break;
               case 2:
               case 4:
               default:
                  var10000 = false;
            }

            return var10000;
         }

         public boolean focusesZ() {
            boolean var10000;
            switch (this.ordinal()) {
               case 2:
               case 3:
               case 4:
               case 6:
                  var10000 = true;
                  break;
               case 5:
               default:
                  var10000 = false;
            }

            return var10000;
         }

         // $FF: synthetic method
         private static GizmoHandle[] $values() {
            return new GizmoHandle[]{X, Y, Z, X_PLANE, Y_PLANE, Z_PLANE, FREE};
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static record DragOperation(Gizmo gizmo, Gizmo.GizmoHandle handle, class_243 startGizmoPos, class_243 startHit, @Nullable class_243 axisPlaneNormal, double freeDragRadius) {
      public void apply(class_243 rayOrigin, class_243 rayDir) {
         switch (this.handle.ordinal()) {
            case 0 -> this.applyAxis(rayOrigin, rayDir, new class_243((double)1.0F, (double)0.0F, (double)0.0F));
            case 1 -> this.applyAxis(rayOrigin, rayDir, new class_243((double)0.0F, (double)1.0F, (double)0.0F));
            case 2 -> this.applyAxis(rayOrigin, rayDir, new class_243((double)0.0F, (double)0.0F, (double)1.0F));
            case 3 -> this.applyPlane(rayOrigin, rayDir, new class_243((double)1.0F, (double)0.0F, (double)0.0F));
            case 4 -> this.applyPlane(rayOrigin, rayDir, new class_243((double)0.0F, (double)1.0F, (double)0.0F));
            case 5 -> this.applyPlane(rayOrigin, rayDir, new class_243((double)0.0F, (double)0.0F, (double)1.0F));
            case 6 -> this.applyFree(rayOrigin, rayDir);
         }

         Track track = IceStomModClient.getInstance().trackHandler.getSelectedTrack();
         if (track != null) {
            track.getFeatures().forEach((trackFeature) -> {
               if (trackFeature.getGizmos().contains(this.gizmo)) {
                  trackFeature.onGizmoMoved(this.gizmo);
               }

            });
            track.updateRenderState();
         }

      }

      private void applyFree(class_243 rayOrigin, class_243 rayDir) {
         class_243 hit = GizmoHandler.intersectRaySphere(rayOrigin, rayDir, rayOrigin, this.freeDragRadius);
         if (hit != null) {
            class_243 delta = hit.method_1020(this.startHit);
            class_243 pos = this.startGizmoPos.method_1019(delta);
            this.gizmo.setX(pos.field_1352);
            this.gizmo.setY(pos.field_1351);
            this.gizmo.setZ(pos.field_1350);
         }
      }

      private void applyPlane(class_243 rayOrigin, class_243 rayDir, class_243 normal) {
         class_243 hit = GizmoHandler.intersectRayPlane(rayOrigin, rayDir, this.startGizmoPos, normal);
         if (hit != null) {
            class_243 delta = hit.method_1020(this.startHit);
            switch (this.handle.ordinal()) {
               case 3:
                  this.gizmo.setY(this.startGizmoPos.field_1351 + delta.field_1351);
                  this.gizmo.setZ(this.startGizmoPos.field_1350 + delta.field_1350);
                  break;
               case 4:
                  this.gizmo.setX(this.startGizmoPos.field_1352 + delta.field_1352);
                  this.gizmo.setZ(this.startGizmoPos.field_1350 + delta.field_1350);
                  break;
               case 5:
                  this.gizmo.setX(this.startGizmoPos.field_1352 + delta.field_1352);
                  this.gizmo.setY(this.startGizmoPos.field_1351 + delta.field_1351);
            }

         }
      }

      private void applyAxis(class_243 rayOrigin, class_243 rayDir, class_243 axis) {
         if (this.axisPlaneNormal != null) {
            class_243 hit = GizmoHandler.intersectRayPlane(rayOrigin, rayDir, this.startGizmoPos, this.axisPlaneNormal);
            if (hit != null) {
               double delta = hit.method_1020(this.startHit).method_1026(axis);
               class_243 pos = this.startGizmoPos.method_1019(axis.method_1021(delta));
               this.gizmo.setX(pos.field_1352);
               this.gizmo.setY(pos.field_1351);
               this.gizmo.setZ(pos.field_1350);
            }
         }
      }
   }
}
