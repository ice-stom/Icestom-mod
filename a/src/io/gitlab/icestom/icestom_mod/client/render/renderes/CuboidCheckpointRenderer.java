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
public class CuboidCheckpointRenderer extends FilledPipeline {
   protected boolean extract(WorldRenderContext context, RenderPipeline pipeline) {
      List<CuboidCheckpointRenderState> grids = (List)this.getRenderState();
      if (grids != null && !grids.isEmpty()) {
         class_287 buffer = this.getBuffer(pipeline);
         class_4587 matrices = context.matrices();
         class_243 camera = context.worldState().field_63082.field_63078;

         for(CuboidCheckpointRenderState grid : grids) {
            matrices.method_22903();
            matrices.method_22904(grid.x1 - camera.field_1352, grid.y1 - camera.field_1351, grid.z1 - camera.field_1350);
            Matrix4fc matrix = matrices.method_23760().method_23761();
            float thickness = 0.01F;
            RenderUtil.renderCuboidWireframe(matrix, buffer, 0.0F, 0.0F, 0.0F, (float)(grid.x2 - grid.x1), (float)(grid.y2 - grid.y1), (float)(grid.z2 - grid.z1), thickness, 1.0F, 1.0F, 1.0F, 1.0F);
            matrices.method_22909();
         }

         return true;
      } else {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   public static record CuboidCheckpointRenderState(double x1, double y1, double z1, double x2, double y2, double z2, int n) {
   }
}
