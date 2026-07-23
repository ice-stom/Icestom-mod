package io.gitlab.icestom.icestom_mod.track.feature;

import io.gitlab.icestom.icestom_mod.IceStomModClient;
import io.gitlab.icestom.icestom_mod.render.renderers.FeatureLabelRenderer;
import io.gitlab.icestom.icestom_mod.render.renderers.generic.BoatLocationRenderer;
import io.gitlab.icestom.icestom_mod.render.renderers.generic.CuboidFeatureRenderer;
import io.gitlab.icestom.icestom_mod.render.renderers.generic.LineFeatureRenderer;

import java.util.ArrayList;
import java.util.List;

public class TrackRenderStateProvider {
    public final List<FeatureLabelRenderer.FeatureRenderable> featureRenderable = new ArrayList<>();
    public final List<LineFeatureRenderer.LineRenderable> lineRenderables = new ArrayList<>();
    public final List<CuboidFeatureRenderer.CuboidRenderable> cuboidRenderables = new ArrayList<>();
    public final List<BoatLocationRenderer.BoatLocationRenderable> boatLocationRenderables = new ArrayList<>();

    public void apply() {
        IceStomModClient.instance.featureRenderer.setRenderState(featureRenderable);
        IceStomModClient.instance.lineFeatureRenderer.setRenderState(lineRenderables);
        IceStomModClient.instance.cuboidFeatureRenderable.setRenderState(cuboidRenderables);
        IceStomModClient.instance.boatLocationRenderer.setRenderState(boatLocationRenderables);
    }

    public void clear() {
        featureRenderable.clear();
        lineRenderables.clear();
        cuboidRenderables.clear();
        boatLocationRenderables.clear();
    }
}
