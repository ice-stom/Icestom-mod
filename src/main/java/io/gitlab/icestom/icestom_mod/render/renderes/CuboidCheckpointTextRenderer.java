package io.gitlab.icestom.icestom_mod.render.renderes;

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
public class CuboidCheckpointTextRenderer
        extends
            TextPipeline<List<CuboidCheckpointRenderer.CuboidCheckpointRenderState>> {
    protected boolean extract(WorldRenderContext context, RenderPipeline pipeline) {
        List<CuboidCheckpointRenderer.CuboidCheckpointRenderState> checkpoints = this.getRenderState();

        if (checkpoints == null)
            return false;
        if (checkpoints.isEmpty())
            return false;

        Font font = Minecraft.getInstance().font;
        MultiBufferSource.BufferSource bufferSource = this.getTextBufferSource();
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        for (CuboidCheckpointRenderer.CuboidCheckpointRenderState checkpoint : checkpoints) {
            matrices.pushPose();
            matrices.translate((checkpoint.x1() + checkpoint.x2()) / 2 - camera.x,
                    (checkpoint.y1() + checkpoint.y2()) / 2 - camera.y,
                    (checkpoint.z1() + checkpoint.z2()) / 2 - camera.z);

            matrices.translate(0, (checkpoint.y2() - checkpoint.y1()) / 2 + 2, 0);
            Quaternionf billboard = new Quaternionf(context.worldState().cameraRenderState.orientation)
                    .mul(Axis.YP.rotationDegrees(180.0F));
            matrices.mulPose(billboard);
            matrices.scale(-0.05F, -0.05F, 0.05F);
            Matrix4f textMatrix = matrices.last().pose();
            String text = String.valueOf(checkpoint.n());
            int width = font.width(text);
            font.drawInBatch(text, (float) (-width) / 2.0F, 0.0F, -1, false, textMatrix, bufferSource,
                    Font.DisplayMode.SEE_THROUGH, 0, 15728880);
            matrices.popPose();
        }

        return true;
    }
}
