package io.gitlab.icestom.mod.track.feature.location;

import io.gitlab.icestom.mod.GizmoHandler;
import io.gitlab.icestom.mod.render.renderers.FeatureLabelRenderer;
import io.gitlab.icestom.mod.render.renderers.generic.BoatLocationRenderer;
import io.gitlab.icestom.mod.track.feature.TrackFeature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public abstract class BoatLocation<RenderState extends BoatLocationRenderer.BoatLocationRenderable & FeatureLabelRenderer.FeatureRenderable> extends TrackFeature<RenderState> {
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

    public void snapToPlayer(Player player) {
        double rx = player.getX();
        double ry = player.getY();
        double rz = player.getZ();
        float rr = player.getYRot();

        main.setX(Math.round(rx * 2.0) / 2.0);
        main.setY(Math.round(ry * 2.0) / 2.0);
        main.setZ(Math.round(rz * 2.0) / 2.0);

        rr = Math.round(rr / 15.0f) * 15.0f;

        if (rr >= 180.0f) rr -= 360.0f;
        if (rr < -180.0f) rr += 360.0f;

        this.r = (float) Math.toRadians(rr);

        onGizmoMoved(main);
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

    @Override
    public Vec3 getRootPosition() {
        return new Vec3(x, y, z);
    }

    public interface BoatLocationRenderState extends BoatLocationRenderer.BoatLocationRenderable, FeatureLabelRenderer.FeatureRenderable {
        @Override
        default float labelHeight() {
            return 2f;
        }
    }
}