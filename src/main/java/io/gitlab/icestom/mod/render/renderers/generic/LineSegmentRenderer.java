package io.gitlab.icestom.mod.render.renderers.generic;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import io.gitlab.icestom.mod.render.pipeline.FilledCuboidPipeline;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.List;

public class LineSegmentRenderer<RenderState extends LineSegmentRenderer.LineSegmentRenderable> extends FilledCuboidPipeline<RenderState> {

    private static final float THICKNESS = 0.02f;
    private static final float HALF = THICKNESS / 2.0f;

    @Override
    protected boolean depthTest() {
        return false;
    }

    @Override
    protected boolean extract(WorldRenderContext context, RenderPipeline pipeline, @NotNull List<RenderState> renderStates) {
        BufferBuilder buffer = this.getBuffer(pipeline);
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        for (RenderState line : renderStates) {
            float dx = (float) (line.x2() - line.x1());
            float dy = (float) (line.y2() - line.y1());
            float dz = (float) (line.z2() - line.z1());

            Vector3f dir = new Vector3f(dx, dy, dz);
            if (dir.lengthSquared() < 1.0E-8f) {
                continue;
            }
            dir.normalize();

            Vector3f ref = Math.abs(dir.y) > 0.99f ? new Vector3f(1, 0, 0) : new Vector3f(0, 1, 0);
            Vector3f u = new Vector3f(dir).cross(ref).normalize().mul(HALF);
            Vector3f v = new Vector3f(dir).cross(u).normalize().mul(HALF);

            matrices.pushPose();
            matrices.translate(line.x1() - camera.x, line.y1() - camera.y, line.z1() - camera.z);
            Matrix4fc m = matrices.last().pose();

            Vector3f[] off = {
                    new Vector3f(u).negate().sub(v),
                    new Vector3f(u).sub(v),
                    new Vector3f(u).add(v),
                    new Vector3f(u).negate().add(v)
            };

            float[][] a = new float[4][3];
            float[][] b = new float[4][3];
            for (int i = 0; i < 4; i++) {
                a[i][0] = off[i].x;
                a[i][1] = off[i].y;
                a[i][2] = off[i].z;
                b[i][0] = dx + off[i].x;
                b[i][1] = dy + off[i].y;
                b[i][2] = dz + off[i].z;
            }

            float r = line.red(), g = line.green(), bl = line.blue(), al = line.alpha();

            for (int i = 0; i < 4; i++) {
                int j = (i + 1) % 4;
                quad(buffer, m, a[i], a[j], b[j], b[i], r, g, bl, al);
            }

            quad(buffer, m, a[0], a[1], a[2], a[3], r, g, bl, al);
            quad(buffer, m, b[3], b[2], b[1], b[0], r, g, bl, al);

            matrices.popPose();
        }

        return true;
    }

    private static void quad(BufferBuilder buffer, Matrix4fc m, float[] p0, float[] p1, float[] p2, float[] p3, float r, float g, float b, float a) {
        vertex(buffer, m, p0, r, g, b, a);
        vertex(buffer, m, p1, r, g, b, a);
        vertex(buffer, m, p2, r, g, b, a);
        vertex(buffer, m, p3, r, g, b, a);
    }

    private static void vertex(BufferBuilder buffer, Matrix4fc m, float[] p, float r, float g, float b, float a) {
        buffer.addVertex(m, p[0], p[1], p[2]).setColor(r, g, b, a);
    }

    public interface LineSegmentRenderable {
        double x1();
        double y1();
        double z1();
        double x2();
        double y2();
        double z2();

        default float red() { return 1.0f; }
        default float green() { return 1.0f; }
        default float blue() { return 1.0f; }
        default float alpha() { return 1.0f; }
    }
}