package io.gitlab.icestom.icestom_mod.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import io.gitlab.icestom.icestom_mod.IceStomMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;

@Environment(EnvType.CLIENT)
public abstract class FilledPipeline extends FilledThroughWallsPipeline {
   private static final RenderPipeline FILLED;

   RenderPipeline getPipeline() {
      return FILLED;
   }

   static {
      FILLED = class_10799.method_67887(RenderPipeline.builder(new RenderPipeline.Snippet[]{class_10799.field_56860}).withLocation(IceStomMod.id("pipeline/debug_filled_box_through_walls")).withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST).build());
   }
}
