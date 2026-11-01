package io.gitlab.icestom.mod.render.renderers;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import io.gitlab.icestom.mod.render.RenderUtil;
import io.gitlab.icestom.mod.render.pipeline.FilledCuboidPipeline;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;

/* I almost can't be asked to unmangle this after "the incident" - microwavedram */
public class GizmoRenderer extends FilledCuboidPipeline<GizmoRenderer.GizmoRenderState> {
    private static final float SHAFT_LENGTH = 0.9f;
    private static final float SHAFT_THICKNESS = 0.04f;
    private static final float HEAD_LENGTH = 0.2F;
    private static final float HEAD_THICKNESS = 0.1F;
    private static final float CENTRE_SIZE = 0.1F;
    private static final float PLANE_SIZE = 0.2F;
    private static final float PLANE_THICKNESS = 0.02f;
    
    private boolean focusedX = true;
    private boolean focusedY = true;
    private boolean focusedZ = true;
    private boolean focused = false;

    @Override
    protected boolean depthTest() {
        return true;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public void setFocusedX(boolean focusedX) {
        this.focusedX = focusedX;
    }

    public void setFocusedY(boolean focusedY) {
        this.focusedY = focusedY;
    }

    public void setFocusedZ(boolean focusedZ) {
        this.focusedZ = focusedZ;
    }

    private void renderAxis(Matrix4fc positionMatrix, BufferBuilder buffer, GizmoAxis axis, float red, float green, float blue, float alpha) {
        float st = SHAFT_THICKNESS;
        float ht = HEAD_THICKNESS;
        switch (axis) {
            case X -> {
                RenderUtil.renderFilledBox(positionMatrix, buffer, 0.1F, -st, -st, SHAFT_LENGTH, st, st, red, green, blue, alpha);
                RenderUtil.renderFilledBox(positionMatrix, buffer, SHAFT_LENGTH, -ht, -ht, 1.1F, ht, ht, red, green, blue, alpha);
                RenderUtil.renderFilledBox(positionMatrix, buffer, -PLANE_THICKNESS, 0.3F, 0.3F, PLANE_THICKNESS, 0.7F, 0.7F, red, green, blue, alpha);

                if (this.focused) {
                    RenderUtil.renderFilledBox(positionMatrix, buffer, -1000.0F, -0.01F, -0.01F, 1000.0F, 0.01F, 0.01F, red, green, blue, alpha);
                }
            }
            case Y -> {
                RenderUtil.renderFilledBox(positionMatrix, buffer, -st, 0.1F, -st, st, SHAFT_LENGTH, st, red, green, blue, alpha);
                RenderUtil.renderFilledBox(positionMatrix, buffer, -ht, SHAFT_LENGTH, -ht, ht, 1.1F, ht, red, green, blue, alpha);
                RenderUtil.renderFilledBox(positionMatrix, buffer, 0.3F, -PLANE_THICKNESS, 0.3F, 0.7F, PLANE_THICKNESS, 0.7F, red, green, blue, alpha);

                if (this.focused) {
                    RenderUtil.renderFilledBox(positionMatrix, buffer, -0.01F, -1000.0F, -0.01F, 0.01F, 1000.0F, 0.01F, red, green, blue, alpha);
                }
            }
            case Z -> {
                RenderUtil.renderFilledBox(positionMatrix, buffer, -st, -st, 0.1F, st, st, SHAFT_LENGTH, red, green, blue, alpha);
                RenderUtil.renderFilledBox(positionMatrix, buffer, -ht, -ht, SHAFT_LENGTH, ht, ht, 1.1F, red, green, blue, alpha);
                RenderUtil.renderFilledBox(positionMatrix, buffer, 0.3F, 0.3F, -PLANE_THICKNESS, 0.7F, 0.7F, PLANE_THICKNESS, red, green, blue, alpha);

                if (this.focused) {
                    RenderUtil.renderFilledBox(positionMatrix, buffer, -0.01F, -0.01F, -1000.0F, 0.01F, 0.01F, 1000.0F, red, green, blue, alpha);
                }
            }
        }
    }

    @Override
    protected boolean extract(WorldRenderContext context, RenderPipeline pipeline, @NotNull List<GizmoRenderState> gizmos) {
        BufferBuilder buffer = this.getBuffer(pipeline);
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        boolean draw = false;

        for (GizmoRenderState gizmo : gizmos) {
            boolean unfocused = this.focused && !gizmo.selected;
            if (!gizmo.hidden) {
                double dx = gizmo.x() - camera.x;
                double dy = gizmo.y() - camera.y;
                double dz = gizmo.z() - camera.z;
                if (!(dx * dx + dy * dy + dz * dz > (double) (gizmo.draw_distance * gizmo.draw_distance))) {
                    matrices.pushPose();
                    matrices.translate(gizmo.x() - camera.x, gizmo.y() - camera.y, gizmo.z() - camera.z);
                    matrices.scale(gizmo.scale, gizmo.scale, gizmo.scale);

                    Matrix4fc matrix = matrices.last().pose();

                    float alpha = !this.focusedX && !this.focusedY && !this.focusedZ ? 1.0F : 0.3F;
                    if (unfocused) {
                        alpha = 0.15F;
                    }

                    this.renderAxis(matrix, buffer, GizmoRenderer.GizmoAxis.X, 1.0F, 0.2F, 0.2F, this.focusedX && !unfocused ? 1.0F : alpha);
                    this.renderAxis(matrix, buffer, GizmoRenderer.GizmoAxis.Y, 0.2F, 1.0F, 0.2F, this.focusedY && !unfocused ? 1.0F : alpha);
                    this.renderAxis(matrix, buffer, GizmoRenderer.GizmoAxis.Z, 0.2F, 0.2F, 1.0F, this.focusedZ && !unfocused ? 1.0F : alpha);

                    RenderUtil.renderFilledBox(matrix, buffer, -0.1F, -0.1F, -0.1F, 0.1F, 0.1F, 0.1F, 1.0F, 1.0F,
                            1.0F, unfocused ? 0.15F : 1.0F);
                    matrices.popPose();

                    draw = true;
                }
            }
        }

        return draw;
    }

    public enum GizmoAxis {
        X, Y, Z
    }

    public record GizmoRenderState(double x, double y, double z, float scale, float draw_distance, boolean selected, boolean hidden) {}
}
