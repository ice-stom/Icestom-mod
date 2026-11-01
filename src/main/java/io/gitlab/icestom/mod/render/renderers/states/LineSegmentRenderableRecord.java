package io.gitlab.icestom.mod.render.renderers.states;

import io.gitlab.icestom.mod.render.renderers.generic.LineSegmentRenderer;

public record LineSegmentRenderableRecord(
        String label,
        double x1,
        double y1,
        double z1,
        double x2,
        double y2,
        double z2
) implements LineSegmentRenderer.LineSegmentRenderable {}