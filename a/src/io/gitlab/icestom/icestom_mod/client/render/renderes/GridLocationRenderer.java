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
import net.minecraft.class_7833;
import org.joml.Matrix4fc;

@Environment(EnvType.CLIENT)
public class GridLocationRenderer extends FilledPipeline {
   protected boolean extract(WorldRenderContext context, RenderPipeline pipeline) {
      List<GridLocationRenderState> grids = (List)this.getRenderState();
      if (grids != null && !grids.isEmpty()) {
         class_287 buffer = this.getBuffer(pipeline);
         class_4587 matrices = context.matrices();
         class_243 camera = context.worldState().field_63082.field_63078;

         for(GridLocationRenderState grid : grids) {
            matrices.method_22903();
            matrices.method_22904(grid.x - camera.field_1352, grid.y - camera.field_1351, grid.z - camera.field_1350);
            matrices.method_22903();
            matrices.method_22907(class_7833.field_40715.rotation(grid.r - ((float)Math.PI / 2F)));
            Matrix4fc matrix = matrices.method_23760().method_23761();
            RenderUtil.renderFilledBox(matrix, buffer, -0.025F, -0.025F, 0.0F, 0.025F, 0.025F, 1.8F, 1.0F, 1.0F, 1.0F, 1.0F);
            RenderUtil.renderFilledBox(matrix, buffer, -0.05F, -0.05F, 1.8F, 0.05F, 0.05F, 1.9F, 1.0F, 1.0F, 1.0F, 1.0F);
            matrices.method_22909();
            Matrix4fc matrix2 = matrices.method_23760().method_23761();
            float boat_size = 0.6875F;
            float thickness = 0.025F;
            RenderUtil.renderFilledBox(matrix2, buffer, -boat_size - thickness, -thickness, -boat_size - thickness, boat_size + thickness, thickness, -boat_size + thickness, 1.0F, 1.0F, 1.0F, 1.0F);
            RenderUtil.renderFilledBox(matrix2, buffer, -boat_size - thickness, -thickness, -boat_size - thickness, -boat_size + thickness, thickness, boat_size + thickness, 1.0F, 1.0F, 1.0F, 1.0F);
            RenderUtil.renderFilledBox(matrix2, buffer, boat_size - thickness, -thickness, -boat_size - thickness, boat_size + thickness, thickness, boat_size + thickness, 1.0F, 1.0F, 1.0F, 1.0F);
            RenderUtil.renderFilledBox(matrix2, buffer, -boat_size - thickness, -thickness, boat_size - thickness, boat_size + thickness, thickness, boat_size + thickness, 1.0F, 1.0F, 1.0F, 1.0F);
            matrices.method_22909();
         }

         return true;
      } else {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   public static record GridLocationRenderState(double x, double y, double z, float r, int n) {
   }
}
