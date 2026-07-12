package io.gitlab.icestom.icestom_mod.render.renderers;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.gitlab.icestom.icestom_mod.render.pipeline.TextPipeline;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class LineCheckpointTextRenderer extends TextPipeline<List<LineCheckpointRenderer.LineCheckpointRenderState>> {
    private static final double LABEL_HEIGHT = 2.1;
    private static final float LABEL_SCALE = 0.05F;

    protected boolean extract(WorldRenderContext context, RenderPipeline pipeline) {
        List<LineCheckpointRenderer.LineCheckpointRenderState> grids = this.getRenderState();
        if (grids != null && !grids.isEmpty()) {
            Font font = Minecraft.getInstance().font;
            MultiBufferSource.BufferSource bufferSource = this.getTextBufferSource();
            PoseStack matrices = context.matrices();
            Vec3 camera = context.worldState().cameraRenderState.pos;

            for (LineCheckpointRenderer.LineCheckpointRenderState checkpoint : grids) {
                matrices.pushPose();
                double mx = (checkpoint.x1() + checkpoint.x2()) / (double) 2.0F;
                double my = checkpoint.y();
                double mz = (checkpoint.z1() + checkpoint.z2()) / (double) 2.0F;
                matrices.translate(mx - camera.x, my - camera.y, mz - camera.z);
                matrices.translate(0.0F, checkpoint.height() / (double) 2.0F + (double) 2.0F, 0.0F);
                Quaternionf billboard = (new Quaternionf(context.worldState().cameraRenderState.orientation))
                        .mul(Axis.YP.rotationDegrees(180.0F));
                matrices.mulPose(billboard);
                matrices.scale(-LABEL_SCALE, -LABEL_SCALE, LABEL_SCALE);
                Matrix4f textMatrix = matrices.last().pose();
                String text = String.valueOf(checkpoint.n());
                int width = font.width(text);
                font.drawInBatch(text, (float) (-width) / 2.0F, 0.0F, -1, false, textMatrix, bufferSource,
                        Font.DisplayMode.SEE_THROUGH, 0, 15728880);
                matrices.popPose();
            }

            return true;
        } else {
            return false;
        }
    }
}
