package io.gitlab.icestom.icestom_mod.render.renderers.generic;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import io.gitlab.icestom.icestom_mod.render.RenderUtil;
import io.gitlab.icestom.icestom_mod.render.pipeline.FilledPipeline;
import io.gitlab.icestom.icestom_mod.render.renderers.FeatureLabelRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

import java.util.List;

@Environment(EnvType.CLIENT)
public class LineFeatureRenderer<RenderState extends LineFeatureRenderer.LineRenderable> extends FilledPipeline<RenderState> {

    private static final float THICKNESS = 0.05f;

    protected boolean extract(WorldRenderContext context, RenderPipeline pipeline, List<RenderState> renderStates) {
        BufferBuilder buffer = this.getBuffer(pipeline);
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        for (RenderState checkpoint : renderStates) {
            matrices.pushPose();
            matrices.translate(checkpoint.x1() - camera.x, checkpoint.y() - camera.y, checkpoint.z1() - camera.z);
            Matrix4fc matrix = matrices.last().pose();
            
            float half_height = (float) (checkpoint.height() / (double) 2.0F);

            float dx = (float) (checkpoint.x2() - checkpoint.x1());
            float dz = (float) (checkpoint.z2() - checkpoint.z1());
            
            RenderUtil.renderFilledBox(matrix, buffer, -THICKNESS, -half_height, -THICKNESS, THICKNESS, half_height, THICKNESS, 1.0F,
                    1.0F, 1.0F, 1.0F);
            RenderUtil.renderFilledBox(matrix, buffer, dx - THICKNESS, -half_height, dz - THICKNESS, dx + THICKNESS, half_height,
                    dz + THICKNESS, 1.0F, 1.0F, 1.0F, 1.0F);
            RenderUtil.renderFilledQuad(matrix, buffer, 0.0F, -half_height, 0.0F, dx, half_height, dz, 1.0F, 1.0F, 1.0F,
                    0.1F);
            
            matrices.popPose();
        }

        return true;
    }

    public interface LineRenderable extends FeatureLabelRenderer.FeatureRenderable {
        double x1();
        double z1();
        double x2();
        double z2();
        double y();
        double height();
    }
}
