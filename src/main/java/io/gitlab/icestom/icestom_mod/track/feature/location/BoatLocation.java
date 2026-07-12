package io.gitlab.icestom.icestom_mod.track.feature.location;

import io.gitlab.icestom.icestom_mod.GizmoHandler;
import io.gitlab.icestom.icestom_mod.render.renderers.BoatLocationRenderer;
import io.gitlab.icestom.icestom_mod.track.feature.TrackFeature;

public abstract class BoatLocation extends TrackFeature {
    private final GizmoHandler.Gizmo main;
    private final GizmoHandler.Gizmo rotation;
    protected double x;
    protected double y;
    protected double z;
    protected float r;

    public BoatLocation(double x, double y, double z, float r) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;

        this.main = this.newGizmo(this.x, this.y, this.z, 1.0F, 512.0F);
        this.rotation = this.newGizmo(this.x, this.y, this.z, 0.5F, 6.0F);

        this.rotation.setX(this.x - Math.sin(r) * 2.0);
        this.rotation.setZ(this.z + Math.cos(r) * 2.0);
    }

    public BoatLocationRenderer.BoatLocationRenderState renderState() {
        return new BoatLocationRenderer.BoatLocationRenderState(this.x, this.y, this.z, this.r, 0);
    }

    public void onGizmoMoved(GizmoHandler.Gizmo gizmo) {
        if (gizmo == this.main) {
            this.rotation.setY(gizmo.getY());
            this.rotation.setX(gizmo.getX() - Math.sin(this.r) * 2.0);
            this.rotation.setZ(gizmo.getZ() + Math.cos(this.r) * 2.0);
        } else if (gizmo == this.rotation) {
            double dx = this.rotation.getX() - this.main.getX();
            double dz = this.rotation.getZ() - this.main.getZ();
            double len = Math.sqrt(dx * dx + dz * dz);

            this.rotation.setX(this.main.getX() + dx / len * 2.0);
            this.rotation.setZ(this.main.getZ() + dz / len * 2.0);
            this.rotation.setY(this.main.getY());
        }

        this.updateState();
    }

    private void updateState() {
        this.x = this.main.getX();
        this.y = this.main.getY();
        this.z = this.main.getZ();

        double dx = this.rotation.getX() - this.main.getX();
        double dz = this.rotation.getZ() - this.main.getZ();

        this.r = (float) Math.atan2(-dx, dz);
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
}