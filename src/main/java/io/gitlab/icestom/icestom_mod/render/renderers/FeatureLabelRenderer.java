package io.gitlab.icestom.icestom_mod.render.renderers;

import io.gitlab.icestom.icestom_mod.render.pipeline.BillboardTextPipeline;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public class FeatureLabelRenderer extends BillboardTextPipeline<FeatureLabelRenderer.FeatureRenderable> {

    private static boolean showDetails = false;

    @Override
    protected float getTextScale() {
        return 0.025f;
    }

    public interface FeatureRenderable extends BillboardTextRenderable<FeatureRenderable> {

        default String type(){
            return this.getClass().getSimpleName();
        }

        String label();

        int globalIndex();

        double x();
        double y();
        double z();

        @Override
        default void draw(FeatureRenderable feature, Matrix4f textMatrix, MultiBufferSource.BufferSource bufferSource) {

            if (showDetails) {
                BillboardTextRenderable.drawCenteredText(String.valueOf(feature.globalIndex()), 0, -30, 0xFFFFFFFF, textMatrix, bufferSource);
                BillboardTextRenderable.drawCenteredText("[" + feature.type() + "]", 0, -20, 0xFFFFFFFF, textMatrix, bufferSource);
            }

            BillboardTextRenderable.drawCenteredText(feature.label(), 0, -10, 0xFFFFFFFF, textMatrix, bufferSource);
        }
    }

    public static void setShowDetails(boolean showDetails) {
        FeatureLabelRenderer.showDetails = showDetails;
    }
}
