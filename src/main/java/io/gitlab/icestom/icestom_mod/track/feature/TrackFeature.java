package io.gitlab.icestom.icestom_mod.track.feature;

import io.gitlab.icestom.icestom_mod.GizmoHandler;
import io.gitlab.icestom.icestom_mod.IceStomModClient;
import java.util.ArrayList;
import java.util.List;

import io.gitlab.icestom.icestom_mod.render.renderers.FeatureLabelRenderer;
import net.minecraft.world.phys.Vec3;

public abstract class TrackFeature<RenderState extends FeatureLabelRenderer.FeatureRenderable> implements AutoCloseable {

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

    @SuppressWarnings("unchecked")
    public RenderState getRenderState(int globalIndex) {
        Vec3 root = getRootPosition();

        return (RenderState) new FeatureRenderStateImpl(
                globalIndex,
                root.x,
                root.y,
                root.z,
                2f
        );
    }

    public abstract void onGizmoMoved(GizmoHandler.Gizmo var1);

    public abstract Vec3 getRootPosition();

    public interface FeatureRenderState extends FeatureLabelRenderer.FeatureRenderable {}

    public record FeatureRenderStateImpl(
            int globalIndex,
            double x,
            double y,
            double z,
            float labelHeight
    ) implements FeatureRenderState {

        @Override
        public String type() {
            return this.getClass().getSimpleName();
        }

        @Override
        public String label() {
            return "unknown";
        }
    }
}
