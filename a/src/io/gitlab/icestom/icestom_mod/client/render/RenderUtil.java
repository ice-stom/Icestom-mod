package io.gitlab.icestom.icestom_mod.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_287;
import org.joml.Matrix4fc;

@Environment(EnvType.CLIENT)
public class RenderUtil {
   public static void renderFilledBox(Matrix4fc positionMatrix, class_287 buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha) {
      buffer.method_22918(positionMatrix, minX, minY, maxZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, maxX, minY, maxZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, maxX, maxY, maxZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, minX, maxY, maxZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, maxX, minY, minZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, minX, minY, minZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, minX, maxY, minZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, maxX, maxY, minZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, minX, minY, minZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, minX, minY, maxZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, minX, maxY, maxZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, minX, maxY, minZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, maxX, minY, maxZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, maxX, minY, minZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, maxX, maxY, minZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, maxX, maxY, maxZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, minX, maxY, maxZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, maxX, maxY, maxZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, maxX, maxY, minZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, minX, maxY, minZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, minX, minY, minZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, maxX, minY, minZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, maxX, minY, maxZ).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, minX, minY, maxZ).method_22915(red, green, blue, alpha);
   }

   public static void renderFilledQuad(Matrix4fc positionMatrix, class_287 buffer, float x1, float minY, float z1, float x2, float maxY, float z2, float red, float green, float blue, float alpha) {
      buffer.method_22918(positionMatrix, x1, minY, z1).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, x2, minY, z2).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, x2, maxY, z2).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, x1, maxY, z1).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, x2, minY, z2).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, x1, minY, z1).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, x1, maxY, z1).method_22915(red, green, blue, alpha);
      buffer.method_22918(positionMatrix, x2, maxY, z2).method_22915(red, green, blue, alpha);
   }

   public static void renderCuboidWireframe(Matrix4fc positionMatrix, class_287 buffer, float x1, float y1, float z1, float x2, float y2, float z2, float thickness, float red, float green, float blue, float alpha) {
      renderFilledBox(positionMatrix, buffer, x1, y1 - thickness, z1 - thickness, x2, y1 + thickness, z1 + thickness, red, green, blue, alpha);
      renderFilledBox(positionMatrix, buffer, x1, y1 - thickness, z2 - thickness, x2, y1 + thickness, z2 + thickness, red, green, blue, alpha);
      renderFilledBox(positionMatrix, buffer, x1, y2 - thickness, z1 - thickness, x2, y2 + thickness, z1 + thickness, red, green, blue, alpha);
      renderFilledBox(positionMatrix, buffer, x1, y2 - thickness, z2 - thickness, x2, y2 + thickness, z2 + thickness, red, green, blue, alpha);
      renderFilledBox(positionMatrix, buffer, x1 - thickness, y1, z1 - thickness, x1 + thickness, y2, z1 + thickness, red, green, blue, alpha);
      renderFilledBox(positionMatrix, buffer, x1 - thickness, y1, z2 - thickness, x1 + thickness, y2, z2 + thickness, red, green, blue, alpha);
      renderFilledBox(positionMatrix, buffer, x2 - thickness, y1, z1 - thickness, x2 + thickness, y2, z1 + thickness, red, green, blue, alpha);
      renderFilledBox(positionMatrix, buffer, x2 - thickness, y1, z2 - thickness, x2 + thickness, y2, z2 + thickness, red, green, blue, alpha);
      renderFilledBox(positionMatrix, buffer, x1 - thickness, y1 - thickness, z1, x1 + thickness, y1 + thickness, z2, red, green, blue, alpha);
      renderFilledBox(positionMatrix, buffer, x1 - thickness, y2 - thickness, z1, x1 + thickness, y2 + thickness, z2, red, green, blue, alpha);
      renderFilledBox(positionMatrix, buffer, x2 - thickness, y1 - thickness, z1, x2 + thickness, y1 + thickness, z2, red, green, blue, alpha);
      renderFilledBox(positionMatrix, buffer, x2 - thickness, y2 - thickness, z1, x2 + thickness, y2 + thickness, z2, red, green, blue, alpha);
   }
}
