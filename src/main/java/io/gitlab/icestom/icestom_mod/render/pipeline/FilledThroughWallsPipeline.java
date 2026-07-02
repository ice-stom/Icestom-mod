package io.gitlab.icestom.icestom_mod.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.gitlab.icestom.icestom_mod.IceStomMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public abstract class FilledThroughWallsPipeline<RenderState> extends IcestomRenderPipeline<RenderState> {
   public static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
           .withLocation(IceStomMod.id("pipeline/debug_filled_box_through_walls"))
           .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
           .build());

   RenderPipeline getPipeline() {
      return FILLED_THROUGH_WALLS;
   }

   @Override
   protected void draw(Minecraft minecraft, RenderPipeline pipeline) {
      BufferBuilder builder = this.getBuffer(pipeline);
      MeshData builtBuffer = builder.buildOrThrow();
      MeshData.DrawState drawParameters = builtBuffer.drawState();
      VertexFormat format = drawParameters.format();
      GpuBuffer vertices = this.upload(drawParameters, format, builtBuffer);
      IcestomRenderPipeline.executeDraw(minecraft, pipeline, builtBuffer, drawParameters, vertices, format);
      this.vertexBuffer.rotate();
      this.clearBuffer();
   }

   private GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
      int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();
      if (this.vertexBuffer == null || this.vertexBuffer.size() < vertexBufferSize) {
         if (this.vertexBuffer != null) {
            this.vertexBuffer.close();
         }

         this.vertexBuffer = new MappableRingBuffer(() -> "icestom_mod example render pipeline", 34, vertexBufferSize);
      }

      CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
      GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(this.vertexBuffer.currentBuffer().slice(0L, builtBuffer.vertexBuffer().remaining()), false, true);

      try {
         MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
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

      return this.vertexBuffer.currentBuffer();
   }
}
