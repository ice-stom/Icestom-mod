package io.gitlab.icestom.mod.render.renderers.generic;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.gitlab.icestom.mod.render.RenderUtil;

import java.util.List;

import io.gitlab.icestom.mod.render.pipeline.FilledCuboidPipeline;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

public class BoatLocationRenderer<RenderState extends BoatLocationRenderer.BoatLocationRenderable> extends FilledCuboidPipeline<RenderState> {

    @Override
    protected boolean depthTest() {
        return true;
    }

    protected boolean extract(WorldRenderContext context, RenderPipeline pipeline, List<RenderState> renderStates) {
        
        BufferBuilder buffer = this.getBuffer(pipeline);
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        for (RenderState boatLocation : renderStates) {
            matrices.pushPose();
            matrices.translate(boatLocation.x() - camera.x, boatLocation.y() - camera.y, boatLocation.z() - camera.z);

            matrices.pushPose();
            matrices.mulPose(Axis.YN.rotation(boatLocation.r()));
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
    }


    public interface BoatLocationRenderable {
        double x();
        double y();
        double z();
        float r();
    }
}
