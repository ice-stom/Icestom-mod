package io.gitlab.icestom.icestom_mod.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

@Environment(EnvType.CLIENT)
public abstract class TextPipeline<RenderState> extends IcestomRenderPipeline<RenderState> {
   private MultiBufferSource.BufferSource bufferSource;

   RenderPipeline getPipeline() {
      return FilledThroughWallsPipeline.FILLED_THROUGH_WALLS;
   }

   protected MultiBufferSource.BufferSource getTextBufferSource() {
      if (this.bufferSource == null) {
         this.bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
      }

      return this.bufferSource;
   }

   protected void draw(Minecraft client, RenderPipeline pipeline) {
      if (this.bufferSource != null) {
         this.bufferSource.endBatch();
      }
   }
}
