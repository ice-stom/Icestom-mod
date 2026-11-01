package io.gitlab.icestom.mod.render.hud.editor;

public interface EditorTool {
    String getName();
    int getColor();

    void selected();

    default boolean mouseDown() { return false; }
    default boolean mouseUp() { return false; }
}
