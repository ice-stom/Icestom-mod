package io.gitlab.icestom.icestom_mod.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.gitlab.icestom.icestom_mod.IceStomMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_11285;
import net.minecraft.class_287;
import net.minecraft.class_310;
import net.minecraft.class_9801;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public abstract class FilledThroughWallsPipeline extends IcestomRenderPipeline {
   public static final RenderPipeline FILLED_THROUGH_WALLS;

   RenderPipeline getPipeline() {
      return FILLED_THROUGH_WALLS;
   }

   protected void draw(class_310 client, RenderPipeline pipeline) {
      class_287 builder = this.getBuffer(pipeline);
      class_9801 builtBuffer = builder.method_60800();
      class_9801.class_4574 drawParameters = builtBuffer.method_60822();
      VertexFormat format = drawParameters.comp_749();
      GpuBuffer vertices = this.upload(drawParameters, format, builtBuffer);
      IcestomRenderPipeline.executeDraw(client, pipeline, builtBuffer, drawParameters, vertices, format);
      this.vertexBuffer.method_71121();
      this.clearBuffer();
   }

   private GpuBuffer upload(class_9801.class_4574 drawParameters, VertexFormat format, class_9801 builtBuffer) {
      int vertexBufferSize = drawParameters.comp_750() * format.getVertexSize();
      if (this.vertexBuffer == null || this.vertexBuffer.method_71312() < vertexBufferSize) {
         if (this.vertexBuffer != null) {
            this.vertexBuffer.close();
         }

         this.vertexBuffer = new class_11285(() -> "icestom_mod example render pipeline", 34, vertexBufferSize);
      }

      CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
      GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(this.vertexBuffer.method_71119().slice(0L, (long)builtBuffer.method_60818().remaining()), false, true);

      try {
         MemoryUtil.memCopy(builtBuffer.method_60818(), mappedView.data());
      } catch (Throwable var10) {
         if (mappedView != null) {
            try {
               mappedView.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (mappedView != null) {
         mappedView.close();
      }

      return this.vertexBuffer.method_71119();
   }

   static {
      FILLED_THROUGH_WALLS = class_10799.method_67887(RenderPipeline.builder(new RenderPipeline.Snippet[]{class_10799.field_56860}).withLocation(IceStomMod.id("pipeline/debug_filled_box_through_walls")).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build());
   }
}
