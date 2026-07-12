package io.gitlab.icestom.icestom_mod.render.renderers;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.gitlab.icestom.icestom_mod.render.RenderUtil;
import io.gitlab.icestom.icestom_mod.render.pipeline.FilledPipeline;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

@Environment(EnvType.CLIENT)
public abstract class BoatLocationRenderer extends FilledPipeline<List<BoatLocationRenderer.BoatLocationRenderState>> {
    protected boolean extract(WorldRenderContext context, RenderPipeline pipeline) {
        List<BoatLocationRenderState> grids = this.getRenderState();
        if (grids != null && !grids.isEmpty()) {
            BufferBuilder buffer = this.getBuffer(pipeline);
            PoseStack matrices = context.matrices();
            Vec3 camera = context.worldState().cameraRenderState.pos;

            for (BoatLocationRenderState grid : grids) {
                matrices.pushPose();
                matrices.translate(grid.x - camera.x, grid.y - camera.y, grid.z - camera.z);
                matrices.pushPose();
                matrices.mulPose(Axis.YN.rotation(grid.r));
                Matrix4fc matrix = matrices.last().pose();
                RenderUtil.renderFilledBox(matrix, buffer, -0.025F, -0.025F, 0.0F, 0.025F, 0.025F, 1.8F, 1.0F, 1.0F,
                        1.0F, 1.0F);
                RenderUtil.renderFilledBox(matrix, buffer, -0.05F, -0.05F, 1.8F, 0.05F, 0.05F, 1.9F, 1.0F, 1.0F, 1.0F,
                        1.0F);
                matrices.popPose();
                Matrix4fc matrix2 = matrices.last().pose();
                float boat_size = 0.6875F;
                float thickness = 0.025F;
                RenderUtil.renderFilledBox(matrix2, buffer, -boat_size - thickness, -thickness, -boat_size - thickness,
                        boat_size + thickness, thickness, -boat_size + thickness, 1.0F, 1.0F, 1.0F, 1.0F);
                RenderUtil.renderFilledBox(matrix2, buffer, -boat_size - thickness, -thickness, -boat_size - thickness,
                        -boat_size + thickness, thickness, boat_size + thickness, 1.0F, 1.0F, 1.0F, 1.0F);
                RenderUtil.renderFilledBox(matrix2, buffer, boat_size - thickness, -thickness, -boat_size - thickness,
                        boat_size + thickness, thickness, boat_size + thickness, 1.0F, 1.0F, 1.0F, 1.0F);
                RenderUtil.renderFilledBox(matrix2, buffer, -boat_size - thickness, -thickness, boat_size - thickness,
                        boat_size + thickness, thickness, boat_size + thickness, 1.0F, 1.0F, 1.0F, 1.0F);
                matrices.popPose();
            }

            return true;
        } else {
            return false;
        }
    }

    @Environment(EnvType.CLIENT)
    public record BoatLocationRenderState(double x, double y, double z, float r, int n) {
    }
}
