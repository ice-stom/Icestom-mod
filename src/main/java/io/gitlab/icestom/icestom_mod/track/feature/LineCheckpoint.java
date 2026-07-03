package io.gitlab.icestom.icestom_mod.track.feature;

import io.gitlab.icestom.icestom_mod.GizmoHandler;
import io.gitlab.icestom.icestom_mod.render.renderes.LineCheckpointRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public non-sealed class LineCheckpoint extends TrackFeature {
    private final GizmoHandler.Gizmo left;
    private final GizmoHandler.Gizmo right;
    private final GizmoHandler.Gizmo height;
    private double x1;
    private double z1;
    private double x2;
    private double z2;
    private double y;
    private double halfHeight;
    private final int index;

    public double getX1() {
        return this.x1;
    }

    public double getZ1() {
        return this.z1;
    }

    public double getX2() {
        return this.x2;
    }

    public double getZ2() {
        return this.z2;
    }

    public double getY() {
        return this.y;
    }

    public double getHalfHeight() {
        return this.halfHeight;
    }

    public int getIndex() {
        return this.index;
    }

    public LineCheckpoint(double x1, double z1, double x2, double z2, double y, double height, int index) {
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
        this.y = y;
        this.halfHeight = height / (double) 2.0F;
        this.index = index;
        this.left = this.newGizmo(x1, y, z1, 1.0F, 512.0F);
        this.right = this.newGizmo(x2, y, z2, 1.0F, 512.0F);
        this.height = this.newGizmo((x1 + x2) / (double) 2.0F, y + this.halfHeight, (z1 + z2) / (double) 2.0F, 1.0F,
                512.0F);
    }

    public LineCheckpointRenderer.LineCheckpointRenderState renderState() {
        return new LineCheckpointRenderer.LineCheckpointRenderState(this.x1, this.z1, this.x2, this.z2, this.y,
                this.halfHeight * (double) 2.0F, this.index);
    }

    public void onGizmoMoved(GizmoHandler.Gizmo gizmo) {
        if (gizmo == this.left) {
            this.y = gizmo.getY();
            this.right.setY(this.y);
            this.height.setY(this.y + this.halfHeight);
            this.height.setX((this.x1 + this.x2) / (double) 2.0F);
            this.height.setZ((this.z1 + this.z2) / (double) 2.0F);
        } else if (gizmo == this.right) {
            this.y = gizmo.getY();
            this.left.setY(this.y);
            this.height.setY(this.y + this.halfHeight);
            this.height.setX((this.x1 + this.x2) / (double) 2.0F);
            this.height.setZ((this.z1 + this.z2) / (double) 2.0F);
        } else if (gizmo == this.height) {
            double dx = (this.x2 - this.x1) / (double) 2.0F;
            double dz = (this.z2 - this.z1) / (double) 2.0F;
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
}
