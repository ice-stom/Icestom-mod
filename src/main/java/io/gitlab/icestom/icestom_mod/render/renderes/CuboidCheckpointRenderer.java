package io.gitlab.icestom.icestom_mod.render.renderes;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import io.gitlab.icestom.icestom_mod.render.RenderUtil;
import io.gitlab.icestom.icestom_mod.render.pipeline.FilledPipeline;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

@Environment(EnvType.CLIENT)
public class CuboidCheckpointRenderer
        extends
            FilledPipeline<List<CuboidCheckpointRenderer.CuboidCheckpointRenderState>> {
    protected boolean extract(WorldRenderContext context, RenderPipeline pipeline) {
        List<CuboidCheckpointRenderState> grids = this.getRenderState();

        if (grids == null)
            return false;
        if (grids.isEmpty())
            return false;

        BufferBuilder buffer = this.getBuffer(pipeline);
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        for (CuboidCheckpointRenderState grid : grids) {
            matrices.pushPose();
            matrices.translate(grid.x1 - camera.x, grid.y1 - camera.y, grid.z1 - camera.z);
            Matrix4fc matrix = matrices.last().pose();
            float thickness = 0.01F;
            RenderUtil.renderCuboidWireframe(matrix, buffer, 0.0F, 0.0F, 0.0F, (float) (grid.x2 - grid.x1),
                    (float) (grid.y2 - grid.y1), (float) (grid.z2 - grid.z1), thickness, 1.0F, 1.0F, 1.0F, 1.0F);
            matrices.popPose();
        }

        return true;
    }

    @Environment(EnvType.CLIENT)
    public record CuboidCheckpointRenderState(double x1, double y1, double z1, double x2, double y2, double z2, int n) {
    }
}
