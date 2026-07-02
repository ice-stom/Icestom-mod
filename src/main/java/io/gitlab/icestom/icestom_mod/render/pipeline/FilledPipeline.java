package io.gitlab.icestom.icestom_mod.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import io.gitlab.icestom.icestom_mod.IceStomMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderPipelines;

@Environment(EnvType.CLIENT)
public abstract class FilledPipeline<RenderState> extends FilledThroughWallsPipeline<RenderState> {
   private static final RenderPipeline FILLED = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
           .withLocation(IceStomMod.id("pipeline/debug_filled_box_through_walls"))
           .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
           .build());

   RenderPipeline getPipeline() {
      return FILLED;
   }
}
