package io.gitlab.icestom.icestom_mod.render.hud.editor;

public class NoneTool implements EditorTool {
    @Override
    public String getName() {
        return "None";
    }

    @Override
    public int getColor() {
        return 0xFFFFFFFF;
    }

    @Override
    public void selected() {}
}
