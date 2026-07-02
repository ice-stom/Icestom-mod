package io.gitlab.icestom.icestom_mod.render.renderes;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.gitlab.icestom.icestom_mod.render.RenderUtil;
import io.gitlab.icestom.icestom_mod.render.pipeline.FilledThroughWallsPipeline;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_4587;
import org.joml.Matrix4fc;

@Environment(EnvType.CLIENT)
public class GizmoRenderer extends FilledThroughWallsPipeline {
   private static final float SHAFT_LENGTH = 0.9F;
   private static final float SHAFT_THICKNESS = 0.04F;
   private static final float HEAD_LENGTH = 0.2F;
   private static final float HEAD_THICKNESS = 0.1F;
   private static final float CENTRE_SIZE = 0.1F;
   private static final float PLANE_SIZE = 0.2F;
   private static final float PLANE_THICKNESS = 0.02F;
   private boolean focusedX = true;
   private boolean focusedY = true;
   private boolean focusedZ = true;
   private boolean focused = false;

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

   protected boolean extract(WorldRenderContext context, RenderPipeline pipeline) {
      List<GizmoRenderState> gizmos = (List)this.getRenderState();
      if (gizmos.isEmpty()) {
         return false;
      } else {
         class_287 buffer = this.getBuffer(pipeline);
         class_4587 matrices = context.matrices();
         class_243 camera = context.worldState().field_63082.field_63078;
         boolean drawn = false;

         for(GizmoRenderState gizmo : gizmos) {
            boolean unfocused = this.focused && !gizmo.selected;
            if (!gizmo.hidden) {
               double dx = gizmo.x() - camera.field_1352;
               double dy = gizmo.y() - camera.field_1351;
               double dz = gizmo.z() - camera.field_1350;
               if (!(dx * dx + dy * dy + dz * dz > (double)(gizmo.draw_distance * gizmo.draw_distance))) {
                  matrices.method_22903();
                  matrices.method_22904(gizmo.x() - camera.field_1352, gizmo.y() - camera.field_1351, gizmo.z() - camera.field_1350);
                  matrices.method_22905(gizmo.scale, gizmo.scale, gizmo.scale);
                  Matrix4fc matrix = matrices.method_23760().method_23761();
                  float alpha = !this.focusedX && !this.focusedY && !this.focusedZ ? 1.0F : 0.3F;
                  if (unfocused) {
                     alpha = 0.15F;
                  }

                  this.renderAxis(matrix, buffer, GizmoRenderer.GizmoAxis.X, 1.0F, 0.2F, 0.2F, this.focusedX && !unfocused ? 1.0F : alpha);
                  this.renderAxis(matrix, buffer, GizmoRenderer.GizmoAxis.Y, 0.2F, 1.0F, 0.2F, this.focusedY && !unfocused ? 1.0F : alpha);
                  this.renderAxis(matrix, buffer, GizmoRenderer.GizmoAxis.Z, 0.2F, 0.2F, 1.0F, this.focusedZ && !unfocused ? 1.0F : alpha);
                  RenderUtil.renderFilledBox(matrix, buffer, -0.1F, -0.1F, -0.1F, 0.1F, 0.1F, 0.1F, 1.0F, 1.0F, 1.0F, unfocused ? 0.15F : 1.0F);
                  drawn = true;
                  matrices.method_22909();
               }
            }
         }

         return drawn;
      }
   }

   private void renderAxis(Matrix4fc positionMatrix, class_287 buffer, GizmoAxis axis, float red, float green, float blue, float alpha) {
      float st = 0.04F;
      float ht = 0.1F;
      switch (axis.ordinal()) {
         case 0:
            RenderUtil.renderFilledBox(positionMatrix, buffer, 0.1F, -st, -st, 0.9F, st, st, red, green, blue, alpha);
            RenderUtil.renderFilledBox(positionMatrix, buffer, 0.9F, -ht, -ht, 1.1F, ht, ht, red, green, blue, alpha);
            RenderUtil.renderFilledBox(positionMatrix, buffer, -0.02F, 0.3F, 0.3F, 0.02F, 0.7F, 0.7F, red, green, blue, alpha);
            if (this.focused) {
               RenderUtil.renderFilledBox(positionMatrix, buffer, -1000.0F, -0.01F, -0.01F, 1000.0F, 0.01F, 0.01F, red, green, blue, alpha);
            }
            break;
         case 1:
            RenderUtil.renderFilledBox(positionMatrix, buffer, -st, 0.1F, -st, st, 0.9F, st, red, green, blue, alpha);
            RenderUtil.renderFilledBox(positionMatrix, buffer, -ht, 0.9F, -ht, ht, 1.1F, ht, red, green, blue, alpha);
            RenderUtil.renderFilledBox(positionMatrix, buffer, 0.3F, -0.02F, 0.3F, 0.7F, 0.02F, 0.7F, red, green, blue, alpha);
            if (this.focused) {
               RenderUtil.renderFilledBox(positionMatrix, buffer, -0.01F, -1000.0F, -0.01F, 0.01F, 1000.0F, 0.01F, red, green, blue, alpha);
            }
            break;
         case 2:
            RenderUtil.renderFilledBox(positionMatrix, buffer, -st, -st, 0.1F, st, st, 0.9F, red, green, blue, alpha);
            RenderUtil.renderFilledBox(positionMatrix, buffer, -ht, -ht, 0.9F, ht, ht, 1.1F, red, green, blue, alpha);
            RenderUtil.renderFilledBox(positionMatrix, buffer, 0.3F, 0.3F, -0.02F, 0.7F, 0.7F, 0.02F, red, green, blue, alpha);
            if (this.focused) {
               RenderUtil.renderFilledBox(positionMatrix, buffer, -0.01F, -0.01F, -1000.0F, 0.01F, 0.01F, 1000.0F, red, green, blue, alpha);
            }
      }

   }

   @Environment(EnvType.CLIENT)
   public static enum GizmoAxis {
      X,
      Y,
      Z;

      // $FF: synthetic method
      private static GizmoAxis[] $values() {
         return new GizmoAxis[]{X, Y, Z};
      }
   }

   @Environment(EnvType.CLIENT)
   public static record GizmoRenderState(double x, double y, double z, float scale, float draw_distance, boolean selected, boolean hidden) {
   }
}
