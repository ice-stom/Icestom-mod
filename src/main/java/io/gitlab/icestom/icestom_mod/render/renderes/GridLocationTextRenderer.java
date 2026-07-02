package io.gitlab.icestom.icestom_mod.render.renderes;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.gitlab.icestom.icestom_mod.render.pipeline.TextPipeline;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class GridLocationTextRenderer extends TextPipeline<List<BoatLocationRenderer.BoatLocationRenderState>> {
   private static final double LABEL_HEIGHT = 2.1;
   private static final float LABEL_SCALE = 0.05F;

   protected boolean extract(WorldRenderContext context, RenderPipeline pipeline) {
      List<BoatLocationRenderer.BoatLocationRenderState> grids = this.getRenderState();
      if (grids != null && !grids.isEmpty()) {
         Font font = Minecraft.getInstance().font;
         MultiBufferSource.BufferSource bufferSource = this.getTextBufferSource();
         PoseStack matrices = context.matrices();
         Vec3 camera = context.worldState().cameraRenderState.pos;

         for(BoatLocationRenderer.BoatLocationRenderState grid : grids) {
            matrices.pushPose();
            matrices.translate(grid.x() - camera.x, grid.y() - camera.y, grid.z() - camera.z);
            matrices.translate(0, 2.1, 0);
            Quaternionf billboard = (new Quaternionf(context.worldState().cameraRenderState.orientation)).mul(Axis.YP.rotationDegrees(180.0F));
            matrices.mulPose(billboard);
            matrices.scale(-LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);
            Matrix4f textMatrix = matrices.last().pose();
            String text = String.valueOf(grid.n());
            int width = font.width(text);
            font.drawInBatch(text, (float)(-width) / 2.0F, 0.0F, -1, false, textMatrix, bufferSource, Font.DisplayMode.SEE_THROUGH, 0, 15728880);
            matrices.popPose();
         }

         return true;
      } else {
         return false;
      }
   }
}
