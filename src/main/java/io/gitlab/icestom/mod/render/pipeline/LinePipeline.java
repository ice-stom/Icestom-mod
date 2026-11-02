package io.gitlab.icestom.mod.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.gitlab.icestom.mod.IceStomMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import org.lwjgl.system.MemoryUtil;

public abstract class LinePipeline<RenderState> extends IcestomRenderPipeline<RenderState> {

    public static final RenderPipeline LINES_THROUGH_WALLS = RenderPipelines
            .register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withLocation(IceStomMod.id("pipeline/lines_through_walls"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build());

    private static final RenderPipeline LINES = RenderPipelines
            .register(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withLocation(IceStomMod.id("pipeline/lines"))
                    .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST).build());

    protected abstract boolean depthTest();

    protected float lineWidth() {
        return 2.0F;
    }

    @Override
    RenderPipeline getPipeline() {
        return depthTest() ? LINES : LINES_THROUGH_WALLS;
    }

    protected void addLine(BufferBuilder builder, double x1, double y1, double z1,
                           double x2, double y2, double z2, int argb) {
        float dx = (float) (x2 - x1);
        float dy = (float) (y2 - y1);
        float dz = (float) (z2 - z1);
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-6F) {
            return;
        }
        float nx = dx / len, ny = dy / len, nz = dz / len;

        builder.addVertex((float) x1, (float) y1, (float) z1).setColor(argb).setNormal(nx, ny, nz);
        builder.addVertex((float) x2, (float) y2, (float) z2).setColor(argb).setNormal(nx, ny, nz);
    }

    protected void addLine(BufferBuilder builder, double x1, double y1, double z1,
                           double x2, double y2, double z2, int argb, float width) {
        float dx = (float) (x2 - x1), dy = (float) (y2 - y1), dz = (float) (z2 - z1);
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-6F) return;
        float nx = dx / len, ny = dy / len, nz = dz / len;

        builder.addVertex((float) x1, (float) y1, (float) z1)
                .setColor(argb).setNormal(nx, ny, nz).setLineWidth(width);
        builder.addVertex((float) x2, (float) y2, (float) z2)
                .setColor(argb).setNormal(nx, ny, nz).setLineWidth(width);
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

            this.vertexBuffer = new MappableRingBuffer(() -> "icestom_mod line render pipeline", 34,
                    vertexBufferSize);
        }

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(
                this.vertexBuffer.currentBuffer().slice(0L, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return this.vertexBuffer.currentBuffer();
    }
}