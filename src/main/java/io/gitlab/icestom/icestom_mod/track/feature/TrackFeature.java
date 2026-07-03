package io.gitlab.icestom.icestom_mod.track.feature;

import io.gitlab.icestom.icestom_mod.GizmoHandler;
import io.gitlab.icestom.icestom_mod.IceStomModClient;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract sealed class TrackFeature implements AutoCloseable
        permits CuboidCheckpoint, GridLocation, LineCheckpoint, SpawnLocation {

    private final GizmoHandler gizmoHandler = IceStomModClient.getInstance().getGizmoHandler();
    private final List<GizmoHandler.Gizmo> gizmos = new ArrayList<>();

    protected GizmoHandler.Gizmo newGizmo(double x, double y, double z, float scale, float drawDistance) {
        GizmoHandler.Gizmo gizmo = this.gizmoHandler.newGizmo(x, y, z, scale, drawDistance);
        this.gizmos.add(gizmo);
        return gizmo;
    }

    public void close() {
        this.gizmos.forEach(this.gizmoHandler::removeGizmo);
        this.gizmos.clear();
    }

    public List<GizmoHandler.Gizmo> getGizmos() {
        return this.gizmos;
    }

    public abstract void onGizmoMoved(GizmoHandler.Gizmo var1);
}
