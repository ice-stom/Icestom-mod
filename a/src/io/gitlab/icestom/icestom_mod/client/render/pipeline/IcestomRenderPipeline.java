package io.gitlab.icestom.icestom_mod.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.class_11285;
import net.minecraft.class_287;
import net.minecraft.class_310;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public abstract class IcestomRenderPipeline {
   private static final class_9799 allocator = new class_9799(786432);
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f();
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   protected class_11285 vertexBuffer;
   private class_287 buffer;
   private Object renderState;

   protected @NotNull class_287 getBuffer(RenderPipeline pipeline) {
      if (this.buffer == null) {
         this.buffer = new class_287(allocator, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
      }

      return this.buffer;
   }

   protected void clearBuffer() {
      this.buffer = null;
   }

   abstract RenderPipeline getPipeline();

   public void extractAndDraw(WorldRenderContext context) {
      if (this.getRenderState() != null) {
         if (this.extract(context, this.getPipeline())) {
            this.draw(class_310.method_1551(), this.getPipeline());
         }

      }
   }

   public void close() {
      allocator.close();
      if (this.vertexBuffer != null) {
         this.vertexBuffer.close();
         this.vertexBuffer = null;
      }

   }

   protected abstract boolean extract(WorldRenderContext var1, RenderPipeline var2);

   protected abstract void draw(class_310 var1, RenderPipeline var2);

   public void setRenderState(Object renderState) {
      this.renderState = renderState;
   }

   public @Nullable Object getRenderState() {
      return this.renderState;
   }

   protected static void executeDraw(class_310 client, RenderPipeline pipeline, class_9801 builtBuffer, class_9801.class_4574 drawParameters, GpuBuffer vertices, VertexFormat format) {
      GpuBuffer indices;
      VertexFormat.class_5595 indexType;
      if (pipeline.getVertexFormatMode() == class_5596.field_27382) {
         builtBuffer.method_60819(allocator, RenderSystem.getProjectionType().method_65045());
         indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.method_60821());
         indexType = builtBuffer.method_60822().comp_753();
      } else {
         RenderSystem.class_5590 shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
         indices = shapeIndexBuffer.method_68274(drawParameters.comp_751());
         indexType = shapeIndexBuffer.method_31924();
      }

      GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "icestom_mod example render pipeline rendering", client.method_1522().method_71639(), OptionalInt.empty(), client.method_1522().method_71640(), OptionalDouble.empty());

      try {
         renderPass.setPipeline(pipeline);
         RenderSystem.bindDefaultUniforms(renderPass);
         renderPass.setUniform("DynamicTransforms", dynamicTransforms);
         renderPass.setVertexBuffer(0, vertices);
         renderPass.setIndexBuffer(indices, indexType);
         renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.comp_751(), 1);
      } catch (Throwable var13) {
         if (renderPass != null) {
            try {
               renderPass.close();
            } catch (Throwable var12) {
               var13.addSuppressed(var12);
            }
         }

         throw var13;
      }

      if (renderPass != null) {
         renderPass.close();
      }

      builtBuffer.close();
   }
}
