package io.gitlab.icestom.mod;

import io.gitlab.icestom.mod.render.renderers.GizmoRenderer;
import io.gitlab.icestom.mod.track.Track;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        if (track == null) return null;

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
            setSelectedGizmo(null);
            dragOperation = null;
            updateRenderState();

            return false;
        }

        setSelectedGizmo(candidate.gizmo());
        dragOperation = candidate;
        updateRenderState();

        return true;
    }

    public boolean mouseUp() {
        if (this.dragOperation == null) return false;

        this.dragOperation = null;
        this.updateRenderState();

        return true;
    }

    public void tick(float dt) {
        IceStomModClient.getInstance().gizmoRenderer.setRenderState(this.renderStates);

        Minecraft minecraft = Minecraft.getInstance();

        if (this.dragOperation != null) {
            assert minecraft.player != null;

            Vec3 origin = minecraft.player.getEyePosition(dt);
            Vec3 direction = minecraft.player.getViewVector(dt);

            dragOperation.apply(origin, direction);
            updateRenderState();
        }
    }

    public void setSelectedGizmo(@Nullable Gizmo selectedGizmo) {
        this.selectedGizmo = selectedGizmo;
        updateRenderState();
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
        return switch (handle) {
            case X, Y, Z -> {
                if (axisPlaneNormal == null) yield origin;

                Vec3 hit = intersectRayPlane(origin, dir, gizmo.getPos(), axisPlaneNormal);

                yield hit == null ? origin : hit;
            }
            case X_PLANE -> intersectRayPlane(origin, dir, gizmo.getPos(), new Vec3(1, 0, 0));
            case Y_PLANE -> intersectRayPlane(origin, dir, gizmo.getPos(), new Vec3(0, 1, 0));
            case Z_PLANE -> intersectRayPlane(origin, dir, gizmo.getPos(), new Vec3(0, 0, 1));
            case FREE -> {
                Vec3 hit = intersectRaySphere(origin, dir, origin, freeDragRadius);
                yield hit == null ? gizmo.getPos() : hit;
            }
        };
    }

    private static @Nullable Vec3 computeAxisPlaneNormal(Gizmo.GizmoHandle handle, Vec3 rayOrigin, Vec3 gizmoPos) {
        if (handle == GizmoHandler.Gizmo.GizmoHandle.FREE) return null;

        Vec3 axis = switch (handle) {
            case X -> new Vec3(1, 0, 0);
            case Y -> new Vec3(0, 1, 0);
            case Z -> new Vec3(0, 0, 1);
            default -> null;
        };

        if (axis == null) return null;

        Vec3 toCamera = rayOrigin.subtract(gizmoPos);

        if (toCamera.lengthSqr() < 1.0E-6) return null;

        Vec3 n = axis.cross(toCamera).cross(axis);

        if (n.lengthSqr() < 1.0E-6) {
            Vec3 fallback = Math.abs(axis.y) < 0.9
                    ? new Vec3(0, 1, 0)
                    : new Vec3(1, 0, 0);
            n = axis.cross(fallback);
        }

        return n.normalize();
    }

    private static @Nullable Vec3 intersectRaySphere(Vec3 rayOrigin, Vec3 rayDir, Vec3 sphereCenter, double radius) {
        Vec3 oc = rayOrigin.subtract(sphereCenter);
        double b = oc.dot(rayDir);
        double c = oc.dot(oc) - radius * radius;
        double discriminant = b * b - c;
        if (discriminant < (double) 0) {
            return null;
        } else {
            double sqrtD = Math.sqrt(discriminant);
            double t1 = -b - sqrtD;
            double t2 = -b + sqrtD;
            double t = t1 >= (double) 0 ? t1 : t2;
            return t < (double) 0 ? null : rayOrigin.add(rayDir.scale(t));
        }
    }

    private static @Nullable Vec3 intersectRayPlane(Vec3 rayOrigin, Vec3 rayDir, Vec3 planePoint, Vec3 planeNormal) {
        double denom = planeNormal.dot(rayDir);
        if (Math.abs(denom) < 1.0E-6) {
            return null;
        } else {
            double t = planePoint.subtract(rayOrigin).dot(planeNormal) / denom;
            return t < (double) 0 ? null : rayOrigin.add(rayDir.scale(t));
        }
    }

    private static double distanceFromRayToPoint(Vec3 rayOrigin, Vec3 rayDir, Vec3 point) {
        Vec3 w = point.subtract(rayOrigin);

        double t = w.dot(rayDir);

        if (t < (double) 0) {
            return Double.MAX_VALUE;
        }

        Vec3 proj = rayOrigin.add(rayDir.scale(t));
        return proj.distanceTo(point);
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
            X, Y, Z,
            X_PLANE, Y_PLANE, Z_PLANE,
            FREE;

            public Vec3 getHandlePos(Gizmo gizmo) {
                double half_scale = gizmo.scale / 2;

                return switch (this) {
                    case X -> gizmo.getPos().add(gizmo.scale, 0, 0);
                    case Y -> gizmo.getPos().add(0, gizmo.scale, 0);
                    case Z -> gizmo.getPos().add(0, 0, gizmo.scale);

                    case X_PLANE -> gizmo.getPos().add(0, half_scale, half_scale);
                    case Y_PLANE -> gizmo.getPos().add(half_scale, half_scale, half_scale);
                    case Z_PLANE -> gizmo.getPos().add(0, half_scale, half_scale);

                    case FREE -> gizmo.getPos();
                };
            }

            public boolean focusesX() {
                return switch (this) {
                    case FREE, X, Y_PLANE, Z_PLANE -> true;
                    case Y, Z, X_PLANE -> false;
                };
            }

            public boolean focusesY() {
                return switch (this) {
                    case FREE, Y, X_PLANE, Z_PLANE -> true;
                    case X, Z, Y_PLANE -> false;
                };
            }

            public boolean focusesZ() {
                return switch (this) {
                    case FREE, Z, X_PLANE, Y_PLANE -> true;
                    case X, Y, Z_PLANE -> false;
                };
            }

        }
    }

    @Environment(EnvType.CLIENT)
    public record DragOperation(Gizmo gizmo, Gizmo.GizmoHandle handle, Vec3 startGizmoPos, Vec3 startHit,
                                @Nullable Vec3 axisPlaneNormal, double freeDragRadius) {
        public void apply(Vec3 rayOrigin, Vec3 rayDir) {
            switch (this.handle) {
                case X -> this.applyAxis(rayOrigin, rayDir, new Vec3(1, 0, 0));
                case Y -> this.applyAxis(rayOrigin, rayDir, new Vec3(0, 1, 0));
                case Z -> this.applyAxis(rayOrigin, rayDir, new Vec3(0, 0, 1));
                case X_PLANE -> this.applyPlane(rayOrigin, rayDir, new Vec3(1, 0, 0));
                case Y_PLANE -> this.applyPlane(rayOrigin, rayDir, new Vec3(0, 1, 0));
                case Z_PLANE -> this.applyPlane(rayOrigin, rayDir, new Vec3(0, 0, 1));
                case FREE -> this.applyFree(rayOrigin, rayDir);
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
                switch (this.handle) {
                    case X_PLANE -> {
                        this.gizmo.setY(this.startGizmoPos.y + delta.y);
                        this.gizmo.setZ(this.startGizmoPos.z + delta.z);
                    }
                    case Y_PLANE -> {
                        this.gizmo.setX(this.startGizmoPos.x + delta.x);
                        this.gizmo.setZ(this.startGizmoPos.z + delta.z);
                    }
                    case Z_PLANE -> {
                        this.gizmo.setX(this.startGizmoPos.x + delta.x);
                        this.gizmo.setY(this.startGizmoPos.y + delta.y);
                    }
                }
            }
        }

        private void applyAxis(Vec3 rayOrigin, Vec3 rayDir, Vec3 axis) {
            if (axisPlaneNormal == null) return;

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

    public interface GizmoHolder {
        @NotNull Stream<Gizmo> getGizmos();
    }
}
