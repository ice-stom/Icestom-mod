package io.gitlab.icestom.icestom_mod.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_4597;

@Environment(EnvType.CLIENT)
public abstract class TextPipeline extends IcestomRenderPipeline {
   private class_4597.class_4598 bufferSource;

   RenderPipeline getPipeline() {
      return FilledThroughWallsPipeline.FILLED_THROUGH_WALLS;
   }

   protected class_4597.class_4598 getTextBufferSource() {
      if (this.bufferSource == null) {
         this.bufferSource = class_310.method_1551().method_22940().method_23000();
      }

      return this.bufferSource;
   }

   protected void draw(class_310 client, RenderPipeline pipeline) {
      if (this.bufferSource != null) {
         this.bufferSource.method_22993();
      }

   }
}
