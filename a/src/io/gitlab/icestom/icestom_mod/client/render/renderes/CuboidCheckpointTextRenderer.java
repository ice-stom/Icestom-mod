package io.gitlab.icestom.icestom_mod.render.renderes;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.gitlab.icestom.icestom_mod.render.pipeline.TextPipeline;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_7833;
import net.minecraft.class_327.class_6415;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class CuboidCheckpointTextRenderer extends TextPipeline {
   private static final double LABEL_HEIGHT = 2.1;
   private static final float LABEL_SCALE = 0.05F;

   protected boolean extract(WorldRenderContext context, RenderPipeline pipeline) {
      List<CuboidCheckpointRenderer.CuboidCheckpointRenderState> checkpoints = (List)this.getRenderState();
      if (checkpoints != null && !checkpoints.isEmpty()) {
         class_327 font = class_310.method_1551().field_1772;
         class_4597.class_4598 bufferSource = this.getTextBufferSource();
         class_4587 matrices = context.matrices();
         class_243 camera = context.worldState().field_63082.field_63078;

         for(CuboidCheckpointRenderer.CuboidCheckpointRenderState checkpoint : checkpoints) {
            matrices.method_22903();
            matrices.method_22904((checkpoint.x1() + checkpoint.x2()) / (double)2.0F - camera.field_1352, (checkpoint.y1() + checkpoint.y2()) / (double)2.0F - camera.field_1351, (checkpoint.z1() + checkpoint.z2()) / (double)2.0F - camera.field_1350);
            matrices.method_22904((double)0.0F, (checkpoint.y2() - checkpoint.y1()) / (double)2.0F + (double)2.0F, (double)0.0F);
            Quaternionf billboard = (new Quaternionf(context.worldState().field_63082.field_63081)).mul(class_7833.field_40716.rotationDegrees(180.0F));
            matrices.method_22907(billboard);
            matrices.method_22905(-0.05F, -0.05F, 0.05F);
            Matrix4f textMatrix = matrices.method_23760().method_23761();
            String text = String.valueOf(checkpoint.n());
            int width = font.method_1727(text);
            font.method_27521(text, (float)(-width) / 2.0F, 0.0F, -1, false, textMatrix, bufferSource, class_6415.field_33994, 0, 15728880);
            matrices.method_22909();
         }

         return true;
      } else {
         return false;
      }
   }
}
