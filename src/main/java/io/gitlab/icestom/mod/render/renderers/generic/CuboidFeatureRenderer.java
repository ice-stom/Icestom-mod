package io.gitlab.icestom.mod.render.renderers.generic;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import io.gitlab.icestom.mod.render.RenderUtil;

import java.util.List;

import io.gitlab.icestom.mod.render.pipeline.FilledCuboidPipeline;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

public class CuboidFeatureRenderer<RenderState extends CuboidFeatureRenderer.CuboidRenderable> extends FilledCuboidPipeline<RenderState> {

    @Override
    protected boolean depthTest() {
        return true;
    }

    protected boolean extract(WorldRenderContext context, RenderPipeline pipeline, List<RenderState> renderStates) {

        BufferBuilder buffer = this.getBuffer(pipeline);
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        for (RenderState state : renderStates) {
            matrices.pushPose();
            matrices.translate(state.x1() - camera.x, state.y1() - camera.y, state.z1() - camera.z);
            Matrix4fc matrix = matrices.last().pose();
            float thickness = 0.01F;
            RenderUtil.renderCuboidWireframe(matrix, buffer, 0.0F, 0.0F, 0.0F, (float) (state.x2() - state.x1()),
                    (float) (state.y2() - state.y1()), (float) (state.z2() - state.z1()), thickness, 1.0F, 1.0F, 1.0F, 1.0F);
            matrices.popPose();
        }

        return true;
    }

    public interface CuboidRenderable {
        double x1();
        double y1();
        double z1();
        double x2();
        double y2();
        double z2();
    }
}
