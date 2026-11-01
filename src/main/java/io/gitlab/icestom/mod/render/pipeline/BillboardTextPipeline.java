package io.gitlab.icestom.mod.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class BillboardTextPipeline<RenderState extends BillboardTextPipeline.BillboardTextRenderable<RenderState>> extends TextPipeline<RenderState> {

    protected abstract float getTextScale();

    @Override
    protected boolean extract(WorldRenderContext context, RenderPipeline pipeline, @NonNull List<RenderState> renderStates) {
        MultiBufferSource.BufferSource bufferSource = this.getTextBufferSource();
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        for (RenderState state : renderStates) {

            matrices.pushPose();
            matrices.translate(state.x() - camera.x(), state.y() - camera.y(), state.z() - camera.z());

            Quaternionf billboard = new Quaternionf(context.worldState().cameraRenderState.orientation)
                    .mul(Axis.YP.rotationDegrees(180.0F));

            float scale = getTextScale();

            matrices.translate(
                    state.xOffset(),
                    state.yOffset(),
                    state.zOffset()
            );

            matrices.mulPose(billboard);
            matrices.scale(-scale, -scale, scale);



            Matrix4f textMatrix = matrices.last().pose();

            state.draw(state, textMatrix, bufferSource);

            matrices.popPose();
        }

        return true;
    }

    public interface BillboardTextRenderable<T> {
        double x();
        double y();
        double z();

        default float xOffset() { return 0; }
        default float yOffset() { return 0; }
        default float zOffset() { return 0; }

        void draw(T feature, Matrix4f textMatrix, MultiBufferSource.BufferSource bufferSource);

        static void drawCenteredText(String text, int x, int y, int c, Matrix4f textMatrix, MultiBufferSource.BufferSource bufferSource) {
            Font font = Minecraft.getInstance().font;

            int width = font.width(text);
            font.drawInBatch(text, (float) -width / 2 + x, y, -1, false, textMatrix, bufferSource,
                    Font.DisplayMode.SEE_THROUGH, 0, c);
        }

        @SuppressWarnings("unchecked")
        default T cast() {
            return (T) this;
        }
    }
}
