package io.gitlab.icestom.mod.render.renderers.generic;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import io.gitlab.icestom.mod.render.pipeline.LinePipeline;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PathRenderer<RenderState extends PathRenderer.PathRenderable> extends LinePipeline<RenderState> {

    @Override
    protected boolean depthTest() {
        return false;
    }

    @Override
    protected boolean extract(WorldRenderContext context, RenderPipeline pipeline, @NotNull List<RenderState> renderStates) {
        if (renderStates.size() < 2) return false;

        // World space -> camera space
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().position();

        BufferBuilder buffer = this.getBuffer(pipeline);

        for (int i = 0; i < renderStates.size() - 1; i++) {
            RenderState a = renderStates.get(i);
            RenderState b = renderStates.get(i + 1);

            addLine(buffer,
                    a.x() - cam.x, a.y() - cam.y, a.z() - cam.z,
                    b.x() - cam.x, b.y() - cam.y, b.z() - cam.z,
                    a.argb(), 2.0F);
        }

        return true;
    }

    public interface PathRenderable {
        double x();
        double y();
        double z();

        default int argb() { return 0xFFFFFFFF; }
    }

    public record PathPoint(double x, double y, double z, int argb) implements PathRenderable {}
}