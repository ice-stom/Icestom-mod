package io.gitlab.icestom.mod.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4fc;

@Environment(EnvType.CLIENT)
public class RenderUtil {
    public static void renderFilledBox(Matrix4fc positionMatrix, BufferBuilder buffer, float minX, float minY,
            float minZ, float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha) {
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
    }

    public static void renderFilledQuad(Matrix4fc positionMatrix, BufferBuilder buffer, float x1, float minY, float z1,
            float x2, float maxY, float z2, float red, float green, float blue, float alpha) {
        buffer.addVertex(positionMatrix, x1, minY, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, x2, minY, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, x2, maxY, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, x1, maxY, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, x2, minY, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, x1, minY, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, x1, maxY, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, x2, maxY, z2).setColor(red, green, blue, alpha);
    }

    public static void renderCuboidWireframe(Matrix4fc positionMatrix, BufferBuilder buffer, float x1, float y1,
            float z1, float x2, float y2, float z2, float thickness, float red, float green, float blue, float alpha) {
        renderFilledBox(positionMatrix, buffer, x1, y1 - thickness, z1 - thickness, x2, y1 + thickness, z1 + thickness,
                red, green, blue, alpha);
        renderFilledBox(positionMatrix, buffer, x1, y1 - thickness, z2 - thickness, x2, y1 + thickness, z2 + thickness,
                red, green, blue, alpha);
        renderFilledBox(positionMatrix, buffer, x1, y2 - thickness, z1 - thickness, x2, y2 + thickness, z1 + thickness,
                red, green, blue, alpha);
        renderFilledBox(positionMatrix, buffer, x1, y2 - thickness, z2 - thickness, x2, y2 + thickness, z2 + thickness,
                red, green, blue, alpha);
        renderFilledBox(positionMatrix, buffer, x1 - thickness, y1, z1 - thickness, x1 + thickness, y2, z1 + thickness,
                red, green, blue, alpha);
        renderFilledBox(positionMatrix, buffer, x1 - thickness, y1, z2 - thickness, x1 + thickness, y2, z2 + thickness,
                red, green, blue, alpha);
        renderFilledBox(positionMatrix, buffer, x2 - thickness, y1, z1 - thickness, x2 + thickness, y2, z1 + thickness,
                red, green, blue, alpha);
        renderFilledBox(positionMatrix, buffer, x2 - thickness, y1, z2 - thickness, x2 + thickness, y2, z2 + thickness,
                red, green, blue, alpha);
        renderFilledBox(positionMatrix, buffer, x1 - thickness, y1 - thickness, z1, x1 + thickness, y1 + thickness, z2,
                red, green, blue, alpha);
        renderFilledBox(positionMatrix, buffer, x1 - thickness, y2 - thickness, z1, x1 + thickness, y2 + thickness, z2,
                red, green, blue, alpha);
        renderFilledBox(positionMatrix, buffer, x2 - thickness, y1 - thickness, z1, x2 + thickness, y1 + thickness, z2,
                red, green, blue, alpha);
        renderFilledBox(positionMatrix, buffer, x2 - thickness, y2 - thickness, z1, x2 + thickness, y2 + thickness, z2,
                red, green, blue, alpha);
    }
}
