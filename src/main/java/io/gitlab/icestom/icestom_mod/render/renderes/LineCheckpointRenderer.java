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
public class LineCheckpointRenderer extends FilledPipeline<List<LineCheckpointRenderer.LineCheckpointRenderState>> {
   protected boolean extract(WorldRenderContext context, RenderPipeline pipeline) {
      List<LineCheckpointRenderState> checkpoints = this.getRenderState();
      if (checkpoints != null && checkpoints.isEmpty()) {
         return false;
      } else {
         BufferBuilder buffer = this.getBuffer(pipeline);
         PoseStack matrices = context.matrices();
         Vec3 camera = context.worldState().cameraRenderState.pos;

         for(LineCheckpointRenderState checkpoint : checkpoints) {
            matrices.pushPose();
            matrices.translate(checkpoint.x1 - camera.x, checkpoint.y - camera.y, checkpoint.z1 - camera.z);
            Matrix4fc matrix = matrices.last().pose();
            float thickness = 0.01F;
            float heightd2 = (float)(checkpoint.height / (double)2.0F);
            float dx = (float)(checkpoint.x2 - checkpoint.x1);
            float dz = (float)(checkpoint.z2 - checkpoint.z1);
            RenderUtil.renderFilledBox(matrix, buffer, -0.01F, -heightd2, -0.01F, 0.01F, heightd2, 0.01F, 1.0F, 1.0F, 1.0F, 1.0F);
            RenderUtil.renderFilledBox(matrix, buffer, dx - 0.01F, -heightd2, dz - 0.01F, dx + 0.01F, heightd2, dz + 0.01F, 1.0F, 1.0F, 1.0F, 1.0F);
            RenderUtil.renderFilledQuad(matrix, buffer, 0.0F, -heightd2, 0.0F, dx, heightd2, dz, 1.0F, 1.0F, 1.0F, 0.1F);
            matrices.popPose();
         }

         return true;
      }
   }

   @Environment(EnvType.CLIENT)
   public static record LineCheckpointRenderState(double x1, double z1, double x2, double z2, double y, double height, int n) {
   }
}
