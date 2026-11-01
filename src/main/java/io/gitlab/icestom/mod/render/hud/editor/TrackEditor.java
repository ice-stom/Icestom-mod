package io.gitlab.icestom.mod.render.hud.editor;

import io.gitlab.icestom.mod.IceStomModClient;
import io.gitlab.icestom.mod.track.Track;
import io.gitlab.icestom.mod.track.TrackHandler;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class TrackEditor implements HudElement {

    private final List<EditorTool> tools = List.of(
            new NoneTool(),
            new LineCheckpointTool(),
            new GridTool()
    );

    private EditorTool editorTool = tools.getFirst();
    private boolean focused;

    @Override
    public void render(@NonNull GuiGraphics context, @NonNull DeltaTracker tickCounter) {
        final TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;
        final Font font = Minecraft.getInstance().font;

        Track track = trackHandler.getSelectedTrack();

        focused = track != null;

        if (!focused) return;

        int height = context.guiHeight();

        EditorTool tool = this.editorTool;
        int index = tools.indexOf(tool);

        context.drawString(font, track.getId() + ".track.xml", 1, height - 20, 0xFFFFFFFF);


        String content = "[" + (index + 1) + "/" + tools.size() + "] ";
        context.drawString(font, content, 1, height - 10, 0xFFFFFFFF);
        context.drawString(font, tool.getName(), 1 + font.width(content), height - 10, tool.getColor());
    }

//    public void castCheckpoint(Vec3 origin, Vec3 direction, boolean dir) {
//        final TrackHandler trackHandler = IceStomModClient.getInstance().trackHandler;
//        if (Minecraft.getInstance().player == null) return;
//
//        Track track = trackHandler.getSelectedTrack();
//
//        if (track == null) return;
//
//        BlockHitResult result = Minecraft.getInstance().level.clip(new ClipContext(
//                origin,
//                origin.add(direction.scale(100)),
//                ClipContext.Block.OUTLINE,
//                ClipContext.Fluid.ANY,
//                Minecraft.getInstance().player
//        ));
//
//        Vec3 location = result.getLocation();
//
//        if (other == null && !dir) {
//            other = location;
//            return;
//        }
//
//        if (!dir) {
//            other = null;
//            return;
//        };
//
//        var highest_checkpoint = track.getFeatures().stream().filter(trackFeature -> trackFeature instanceof LineCheckpoint)
//                .max(Comparator.comparingInt(trackFeature -> ((LineCheckpoint) trackFeature).getIndex()));
//
//        track.addFeature(new LineCheckpoint(
//                other.x,
//                other.z,
//                location.x,
//                location.z,
//                location.y,
//                2,
//                highest_checkpoint.isEmpty() ? 0 : ((LineCheckpoint) highest_checkpoint.get()).getIndex()
//        ));
//        other = null;
//    }

    public boolean isFocused() {
        return focused;
    }

    public EditorTool getEditorTool() {
        return editorTool;
    }

    public void editorModeRelative(int n) {
        int current = tools.indexOf(this.editorTool);
        int next = (current + tools.size() + n) % tools.size();

        setEditorTool(tools.get(next));
    }

    public void setEditorTool(EditorTool editorTool) {
        this.editorTool = editorTool;

        if (editorTool != null) {
            editorTool.selected();
        }
    }
}
