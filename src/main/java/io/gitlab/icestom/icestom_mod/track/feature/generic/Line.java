package io.gitlab.icestom.icestom_mod.track.feature.generic;

import io.gitlab.icestom.icestom_mod.GizmoHandler;
import io.gitlab.icestom.icestom_mod.render.renderers.FeatureLabelRenderer;
import io.gitlab.icestom.icestom_mod.render.renderers.generic.LineFeatureRenderer;
import io.gitlab.icestom.icestom_mod.track.feature.TrackFeature;
import net.minecraft.world.phys.Vec3;

public abstract class Line<RenderState extends LineFeatureRenderer.LineRenderable & FeatureLabelRenderer.FeatureRenderable> extends TrackFeature<RenderState> {
    private final GizmoHandler.Gizmo left;
    private final GizmoHandler.Gizmo right;
    private final GizmoHandler.Gizmo height;

    protected double x1;
    protected double z1;
    protected double x2;
    protected double z2;
    protected double y;

    protected double halfHeight;

    public Line(double x1, double z1, double x2, double z2, double y, double height) {
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
        this.y = y;
        this.halfHeight = height / 2;
        this.left = this.newGizmo(x1, y, z1, 1, 512);
        this.right = this.newGizmo(x2, y, z2, 1, 512);
        this.height = this.newGizmo((x1 + x2) / 2, y + this.halfHeight, (z1 + z2) / 2, 1, 512);
    }

    public void onGizmoMoved(GizmoHandler.Gizmo gizmo) {
        if (gizmo == this.left) {
            this.y = gizmo.getY();
            this.right.setY(this.y);
            this.height.setY(this.y + this.halfHeight);
            this.height.setX((this.x1 + this.x2) / 2);
            this.height.setZ((this.z1 + this.z2) / 2);
        } else if (gizmo == this.right) {
            this.y = gizmo.getY();
            this.left.setY(this.y);
            this.height.setY(this.y + this.halfHeight);
            this.height.setX((this.x1 + this.x2) / 2);
            this.height.setZ((this.z1 + this.z2) / 2);
        } else if (gizmo == this.height) {
            double dx = (this.x2 - this.x1) / 2;
            double dz = (this.z2 - this.z1) / 2;
            this.left.setX(this.height.getX() - dx);
            this.left.setZ(this.height.getZ() - dz);
            this.right.setX(this.height.getX() + dx);
            this.right.setZ(this.height.getZ() + dz);
            this.height.setY(Math.max(this.y, gizmo.getY()));
        }

        this.updateState();
    }

    private void updateState() {
        this.x1 = this.left.getX();
        this.z1 = this.left.getZ();
        this.x2 = this.right.getX();
        this.z2 = this.right.getZ();
        this.y = Math.min(this.left.getY(), this.right.getY());
        this.halfHeight = this.height.getY() - this.y;
    }

    @Override
    public Vec3 getRootPosition() {
        return new Vec3((x1 + x2) / 2, y, (z1 + z2) / 2);
    }

    public interface LineRenderState extends LineFeatureRenderer.LineRenderable, FeatureLabelRenderer.FeatureRenderable {

        double z1();
        double x1();
        double x2();
        double z2();

        @Override
        default double x() {
            return (x1() + x2()) / 2;
        }

        @Override
        default double z() {
            return (z1() + z2()) / 2;
        }

        @Override
        default float labelHeight() {
            return (float) (height() / 2) + 1;
        }
    }
}