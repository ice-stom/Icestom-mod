package io.gitlab.icestom.icestom_mod.render.renderes;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.gitlab.icestom.icestom_mod.render.RenderUtil;
import io.gitlab.icestom.icestom_mod.render.pipeline.FilledPipeline;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_4587;
import org.joml.Matrix4fc;

@Environment(EnvType.CLIENT)
public class LineCheckpointRenderer extends FilledPipeline {
   protected boolean extract(WorldRenderContext context, RenderPipeline pipeline) {
      List<LineCheckpointRenderState> checkpoints = (List)this.getRenderState();
      if (checkpoints != null && checkpoints.isEmpty()) {
         return false;
      } else {
         class_287 buffer = this.getBuffer(pipeline);
         class_4587 matrices = context.matrices();
         class_243 camera = context.worldState().field_63082.field_63078;

         for(LineCheckpointRenderState checkpoint : checkpoints) {
            matrices.method_22903();
            matrices.method_22904(checkpoint.x1 - camera.field_1352, checkpoint.y - camera.field_1351, checkpoint.z1 - camera.field_1350);
            Matrix4fc matrix = matrices.method_23760().method_23761();
            float thickness = 0.01F;
            float heightd2 = (float)(checkpoint.height / (double)2.0F);
            float dx = (float)(checkpoint.x2 - checkpoint.x1);
            float dz = (float)(checkpoint.z2 - checkpoint.z1);
            RenderUtil.renderFilledBox(matrix, buffer, -0.01F, -heightd2, -0.01F, 0.01F, heightd2, 0.01F, 1.0F, 1.0F, 1.0F, 1.0F);
            RenderUtil.renderFilledBox(matrix, buffer, dx - 0.01F, -heightd2, dz - 0.01F, dx + 0.01F, heightd2, dz + 0.01F, 1.0F, 1.0F, 1.0F, 1.0F);
            RenderUtil.renderFilledQuad(matrix, buffer, 0.0F, -heightd2, 0.0F, dx, heightd2, dz, 1.0F, 1.0F, 1.0F, 0.1F);
            matrices.method_22909();
         }

         return true;
      }
   }

   @Environment(EnvType.CLIENT)
   public static record LineCheckpointRenderState(double x1, double z1, double x2, double z2, double y, double height, int n) {
   }
}
