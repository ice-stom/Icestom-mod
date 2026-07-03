package io.gitlab.icestom.icestom_mod;

import io.gitlab.icestom.icestom_mod.render.renderes.GizmoRenderer;
import io.gitlab.icestom.icestom_mod.track.Track;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class GizmoHandler {
    private final List<Gizmo> gizmos = new ArrayList<>();
    private final List<GizmoRenderer.GizmoRenderState> renderStates = new ArrayList<>();
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

    private @Nullable DragOperation getBestPotentialDrag(Vec3 origin, Vec3 direction) {
        Track track = IceStomModClient.getInstance().trackHandler.getSelectedTrack();

        if (track == null)
            return null;

        return Objects.requireNonNull(track.getGizmos()).map((gizmo) -> {
            Gizmo.GizmoHandleCandidate hit = gizmo.getHandle(origin, direction);
            return hit == null ? null : Map.entry(gizmo, hit);
        }).filter(Objects::nonNull)
                .min(Comparator.comparingDouble((e) -> e.getValue().distance()))
                .map((e) -> {
                    Gizmo gizmo = e.getKey();
                    Gizmo.GizmoHandle handle = e.getValue().handle();
                    Vec3 normal = handle == GizmoHandler.Gizmo.GizmoHandle.FREE
                            ? null
                            : computeAxisPlaneNormal(handle, origin, gizmo.getPos());
                    double radius = origin.distanceTo(gizmo.getPos());
                    return new DragOperation(gizmo, handle, gizmo.getPos(),
                            computeStartHit(gizmo, handle, origin, direction, normal, radius), normal, radius);
                }).orElse(null);
    }

    public boolean mouseDown(Vec3 origin, Vec3 direction) {
        DragOperation candidate = this.getBestPotentialDrag(origin, direction);
        if (candidate == null) {
            this.setSelectedGizmo(null);
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
        Minecraft minecraft = Minecraft.getInstance();
        if (this.dragOperation != null) {
            assert minecraft.player != null;

            Vec3 origin = minecraft.player.getEyePosition(dt);
            Vec3 direction = minecraft.player.getViewVector(dt);
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

        for (Gizmo gizmo : this.gizmos) {
            this.renderStates.add(gizmo.getRenderState(gizmo == this.selectedGizmo));
        }
    }

    private static Vec3 computeStartHit(Gizmo gizmo, Gizmo.GizmoHandle handle, Vec3 origin, Vec3 dir,
            @Nullable Vec3 axisPlaneNormal, double freeDragRadius) {
        Vec3 var10000;
        switch (handle.ordinal()) {
        case 0 :
        case 1 :
        case 2 :
            if (axisPlaneNormal == null) {
                var10000 = origin;
            } else {
                Vec3 hit = intersectRayPlane(origin, dir, gizmo.getPos(), axisPlaneNormal);
                var10000 = hit == null ? origin : hit;
            }
            break;
        case 3 :
            var10000 = intersectRayPlane(origin, dir, gizmo.getPos(),
                    new Vec3(1.0F, 0.0F, 0.0F));
            break;
        case 4 :
            var10000 = intersectRayPlane(origin, dir, gizmo.getPos(),
                    new Vec3(0.0F, 1.0F, 0.0F));
            break;
        case 5 :
            var10000 = intersectRayPlane(origin, dir, gizmo.getPos(),
                    new Vec3(0.0F, 0.0F, 1.0F));
            break;
        case 6 :
            Vec3 hit = intersectRaySphere(origin, dir, origin, freeDragRadius);
            var10000 = hit == null ? gizmo.getPos() : hit;
            break;
        default :
            throw new MatchException(null, null);
        }

        return var10000;
    }

    private static @Nullable Vec3 computeAxisPlaneNormal(Gizmo.GizmoHandle handle, Vec3 rayOrigin, Vec3 gizmoPos) {
        if (handle == GizmoHandler.Gizmo.GizmoHandle.FREE) return null;

        Vec3 axis = switch (handle) {
            case X -> new Vec3(1.0F, 0.0F, 0.0F);
            case Y -> new Vec3(0.0F, 1.0F, 0.0F);
            case Z -> new Vec3(0.0F, 0.0F, 1.0F);
            default -> null;
        };

        if (axis == null) return null;

        Vec3 toCamera = rayOrigin.subtract(gizmoPos);

        if (toCamera.lengthSqr() < 1.0E-6) return null;

        Vec3 n = axis.cross(toCamera).cross(axis);

        if (n.lengthSqr() < 1.0E-6) {
            Vec3 fallback = Math.abs(axis.y) < 0.9
                    ? new Vec3(0.0F, 1.0F, 0.0F)
                    : new Vec3(1.0F, 0.0F, 0.0F);
            n = axis.cross(fallback);
        }

        return n.normalize();
    }

    private static @Nullable Vec3 intersectRaySphere(Vec3 rayOrigin, Vec3 rayDir, Vec3 sphereCenter, double radius) {
        Vec3 oc = rayOrigin.subtract(sphereCenter);
        double b = oc.dot(rayDir);
        double c = oc.dot(oc) - radius * radius;
        double discriminant = b * b - c;
        if (discriminant < (double) 0.0F) {
            return null;
        } else {
            double sqrtD = Math.sqrt(discriminant);
            double t1 = -b - sqrtD;
            double t2 = -b + sqrtD;
            double t = t1 >= (double) 0.0F ? t1 : t2;
            return t < (double) 0.0F ? null : rayOrigin.add(rayDir.scale(t));
        }
    }

    private static @Nullable Vec3 intersectRayPlane(Vec3 rayOrigin, Vec3 rayDir, Vec3 planePoint, Vec3 planeNormal) {
        double denom = planeNormal.dot(rayDir);
        if (Math.abs(denom) < 1.0E-6) {
            return null;
        } else {
            double t = planePoint.subtract(rayOrigin).dot(planeNormal) / denom;
            return t < (double) 0.0F ? null : rayOrigin.add(rayDir.scale(t));
        }
    }

    private static double distanceFromRayToPoint(Vec3 rayOrigin, Vec3 rayDir, Vec3 point) {
        Vec3 w = point.subtract(rayOrigin);
        double t = w.dot(rayDir);
        if (t < (double) 0.0F) {
            return Double.MAX_VALUE;
        } else {
            Vec3 proj = rayOrigin.add(rayDir.scale(t));
            return proj.distanceTo(point);
        }
    }

    public static class Gizmo {
        private double x;
        private double y;
        private double z;
        private final float scale;
        private final float drawDistance;
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

        public Vec3 getPos() {
            return new Vec3(this.x, this.y, this.z);
        }

        private GizmoRenderer.GizmoRenderState getRenderState(boolean selected) {
            return new GizmoRenderer.GizmoRenderState(this.x, this.y, this.z, this.scale, this.drawDistance, selected,
                    this.hidden);
        }

        public @Nullable GizmoHandleCandidate getHandle(Vec3 origin, Vec3 dir) {
            List<GizmoHandleCandidate> candidates = new ArrayList<>();

            for (GizmoHandle handle : GizmoHandler.Gizmo.GizmoHandle.values()) {
                double distance = GizmoHandler.distanceFromRayToPoint(origin, dir, handle.getHandlePos(this));
                if (distance < 0.3 * (double) this.scale) {
                    candidates.add(new GizmoHandleCandidate(handle, distance));
                }
            }

            return candidates.stream().min(Comparator.comparingDouble(GizmoHandleCandidate::distance)).orElse(null);
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }

        public record GizmoHandleCandidate(GizmoHandle handle, double distance) {}

        public enum GizmoHandle {
            X, Y, Z, X_PLANE, Y_PLANE, Z_PLANE, FREE;

            public Vec3 getHandlePos(Gizmo gizmo) {
                Vec3 var10000;
                switch (this.ordinal()) {
                case 0 -> var10000 = gizmo.getPos().add(gizmo.scale, 0.0F, 0.0F);
                case 1 -> var10000 = gizmo.getPos().add(0.0F, gizmo.scale, 0.0F);
                case 2 -> var10000 = gizmo.getPos().add(0.0F, 0.0F, gizmo.scale);
                case 3 -> var10000 = gizmo.getPos().add(0.0F, (double) 0.5F * (double) gizmo.scale,
                        (double) 0.5F * (double) gizmo.scale);
                case 4 -> var10000 = gizmo.getPos().add((double) 0.5F * (double) gizmo.scale, 0.0F,
                        (double) 0.5F * (double) gizmo.scale);
                case 5 -> var10000 = gizmo.getPos().add((double) 0.5F * (double) gizmo.scale,
                        (double) 0.5F * (double) gizmo.scale, 0.0F);
                case 6 -> var10000 = gizmo.getPos();
                default -> throw new MatchException(null, null);
                }

                return var10000;
            }

            public boolean focusesX() {
                boolean var10000;
                switch (this.ordinal()) {
                case 0 :
                case 4 :
                case 5 :
                case 6 :
                    var10000 = true;
                    break;
                case 1 :
                case 2 :
                case 3 :
                default :
                    var10000 = false;
                }

                return var10000;
            }

            public boolean focusesY() {
                boolean var10000;
                switch (this.ordinal()) {
                case 1 :
                case 3 :
                case 5 :
                case 6 :
                    var10000 = true;
                    break;
                case 2 :
                case 4 :
                default :
                    var10000 = false;
                }

                return var10000;
            }

            public boolean focusesZ() {
                boolean var10000;
                switch (this.ordinal()) {
                case 2 :
                case 3 :
                case 4 :
                case 6 :
                    var10000 = true;
                    break;
                case 5 :
                default :
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
    public record DragOperation(Gizmo gizmo, Gizmo.GizmoHandle handle, Vec3 startGizmoPos, Vec3 startHit,
                                @Nullable Vec3 axisPlaneNormal, double freeDragRadius) {
        public void apply(Vec3 rayOrigin, Vec3 rayDir) {
            switch (this.handle.ordinal()) {
            case 0 -> this.applyAxis(rayOrigin, rayDir, new Vec3(1.0F, 0.0F, 0.0F));
            case 1 -> this.applyAxis(rayOrigin, rayDir, new Vec3(0.0F, 1.0F, 0.0F));
            case 2 -> this.applyAxis(rayOrigin, rayDir, new Vec3(0.0F, 0.0F, 1.0F));
            case 3 -> this.applyPlane(rayOrigin, rayDir, new Vec3(1.0F, 0.0F, 0.0F));
            case 4 -> this.applyPlane(rayOrigin, rayDir, new Vec3(0.0F, 1.0F, 0.0F));
            case 5 -> this.applyPlane(rayOrigin, rayDir, new Vec3(0.0F, 0.0F, 1.0F));
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

        private void applyFree(Vec3 rayOrigin, Vec3 rayDir) {
            Vec3 hit = GizmoHandler.intersectRaySphere(rayOrigin, rayDir, rayOrigin, this.freeDragRadius);
            if (hit != null) {
                Vec3 delta = hit.subtract(this.startHit);
                Vec3 pos = this.startGizmoPos.add(delta);
                this.gizmo.setX(pos.x);
                this.gizmo.setY(pos.y);
                this.gizmo.setZ(pos.z);
            }
        }

        private void applyPlane(Vec3 rayOrigin, Vec3 rayDir, Vec3 normal) {
            Vec3 hit = GizmoHandler.intersectRayPlane(rayOrigin, rayDir, this.startGizmoPos, normal);
            if (hit != null) {
                Vec3 delta = hit.subtract(this.startHit);
                switch (this.handle.ordinal()) {
                case 3 :
                    this.gizmo.setY(this.startGizmoPos.y + delta.y);
                    this.gizmo.setZ(this.startGizmoPos.z + delta.z);
                    break;
                case 4 :
                    this.gizmo.setX(this.startGizmoPos.x + delta.x);
                    this.gizmo.setZ(this.startGizmoPos.z + delta.z);
                    break;
                case 5 :
                    this.gizmo.setX(this.startGizmoPos.x + delta.x);
                    this.gizmo.setY(this.startGizmoPos.y + delta.y);
                }
            }
        }

        private void applyAxis(Vec3 rayOrigin, Vec3 rayDir, Vec3 axis) {
            if (this.axisPlaneNormal != null) {
                Vec3 hit = GizmoHandler.intersectRayPlane(rayOrigin, rayDir, this.startGizmoPos, this.axisPlaneNormal);
                if (hit != null) {
                    double delta = hit.subtract(this.startHit).dot(axis);
                    Vec3 pos = this.startGizmoPos.add(axis.scale(delta));
                    this.gizmo.setX(pos.x);
                    this.gizmo.setY(pos.y);
                    this.gizmo.setZ(pos.z);
                }
            }
        }
    }
}
