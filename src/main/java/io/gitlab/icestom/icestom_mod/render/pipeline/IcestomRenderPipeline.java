package io.gitlab.icestom.icestom_mod.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public abstract class IcestomRenderPipeline<RenderState> {
    private static final ByteBufferBuilder allocator = new ByteBufferBuilder(786432);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    protected MappableRingBuffer vertexBuffer;
    private BufferBuilder buffer;
    private RenderState renderState;

    protected @NotNull BufferBuilder getBuffer(RenderPipeline pipeline) {
        if (this.buffer == null) {
            this.buffer = new BufferBuilder(allocator, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
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
                this.draw(Minecraft.getInstance(), this.getPipeline());
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

    protected abstract void draw(Minecraft minecraft, RenderPipeline pipeline);

    public void setRenderState(RenderState renderState) {
        this.renderState = renderState;
    }

    public @Nullable RenderState getRenderState() {
        return this.renderState;
    }

    protected static void executeDraw(Minecraft client, RenderPipeline pipeline, MeshData builtBuffer,
            MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;
        if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().vertexSorting());
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.indexBuffer());
            indexType = builtBuffer.drawState().indexType();
        } else {
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem
                    .getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.type();
        }

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "icestom_mod example render pipeline rendering",
                client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(),
                client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty());

        try {
            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);
            renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
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
