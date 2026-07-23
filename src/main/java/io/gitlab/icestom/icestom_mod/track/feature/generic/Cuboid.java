package io.gitlab.icestom.icestom_mod.track.feature.generic;

import io.gitlab.icestom.icestom_mod.GizmoHandler;
import io.gitlab.icestom.icestom_mod.render.renderers.FeatureLabelRenderer;
import io.gitlab.icestom.icestom_mod.render.renderers.generic.CuboidFeatureRenderer;
import io.gitlab.icestom.icestom_mod.track.feature.TrackFeature;
import net.minecraft.world.phys.Vec3;

public abstract class Cuboid<RenderState extends CuboidFeatureRenderer.CuboidRenderable & FeatureLabelRenderer.FeatureRenderable> extends TrackFeature<RenderState> {
    private final GizmoHandler.Gizmo min;
    private final GizmoHandler.Gizmo max;
    private final GizmoHandler.Gizmo mid;

    protected double x1;
    protected double y1;
    protected double z1;
    protected double x2;
    protected double y2;
    protected double z2;

    public Cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.min = this.newGizmo(x1, y1, z1, 1.0F, 512.0F);
        this.max = this.newGizmo(x2, y2, z2, 1.0F, 512.0F);
        this.mid = this.newGizmo((x1 + x2) / (double) 2.0F, (y1 + y2) / (double) 2.0F, (z1 + z2) / (double) 2.0F, 1.0F,
                512.0F);
    }

    public void onGizmoMoved(GizmoHandler.Gizmo gizmo) {
        if (gizmo == this.mid) {
            double oldMidX = (this.x1 + this.x2) / (double) 2.0F;
            double oldMidY = (this.y1 + this.y2) / (double) 2.0F;
            double oldMidZ = (this.z1 + this.z2) / (double) 2.0F;
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
        this.mid.setX((this.x1 + this.x2) / (double) 2.0F);
        this.mid.setY((this.y1 + this.y2) / (double) 2.0F);
        this.mid.setZ((this.z1 + this.z2) / (double) 2.0F);
    }

    private void updateState() {
        this.x1 = Math.min(this.min.getX(), this.max.getX());
        this.y1 = Math.min(this.min.getY(), this.max.getY());
        this.z1 = Math.min(this.min.getZ(), this.max.getZ());
        this.x2 = Math.max(this.min.getX(), this.max.getX());
        this.y2 = Math.max(this.min.getY(), this.max.getY());
        this.z2 = Math.max(this.min.getZ(), this.max.getZ());
    }

    @Override
    public Vec3 getRootPosition() {
        return new Vec3((x1 + x2) / 2, (y1 + y2) / 2, (z1 + z2) / 2);
    }

    public interface CuboidRenderState extends CuboidFeatureRenderer.CuboidRenderable, FeatureLabelRenderer.FeatureRenderable {
        double x1();
        double y1();
        double z1();
        double x2();
        double y2();
        double z2();

        @Override
        default double x() {
            return (x1() + x2()) / 2;
        }
        @Override
        default double y() {
            return (y1() + y2()) / 2;
        };
        @Override
        default double z() {
            return (z1() + z2()) / 2;
        };

        @Override
        default float labelHeight() {
            return (float) (y2() - y1()) / 2 + 1;
        }
    }
}
