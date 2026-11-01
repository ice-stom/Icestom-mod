package io.gitlab.icestom.mod.mixin;

import io.gitlab.icestom.mod.IceStomModClient;
import io.gitlab.icestom.mod.render.hud.editor.EditorTool;
import io.gitlab.icestom.mod.render.hud.editor.TrackEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    // TODO: UNFUCK THIS!!!!

    @Inject(method = "onButton", at = {@At("HEAD")}, cancellable = true)
    private void onMouseButton(long window, MouseButtonInfo input, int action, CallbackInfo ci) {

        if (this.minecraft.player == null)
            return;
        if (this.minecraft.screen != null)
            return;

        Vec3 origin = this.minecraft.player.getEyePosition(1.0F);
        Vec3 direction = this.minecraft.player.getViewVector(1.0F);

        TrackEditor trackEditor = IceStomModClient.getInstance().trackEditor;

        int gizmoButton = 0;
        if (input.button() == gizmoButton) {
            if (trackEditor.isFocused()) {
                EditorTool tool = trackEditor.getEditorTool();

                if (action == GLFW.GLFW_PRESS) {
                    if (tool.mouseDown()) {
                        ci.cancel();
                        return;
                    }
                } else if (action == GLFW.GLFW_RELEASE) {
                    if (tool.mouseUp()) {
                        ci.cancel();
                        return;
                    }
                }
            }

            if (action == GLFW.GLFW_PRESS) {
                if (IceStomModClient.getInstance().getGizmoHandler().mouseDown(origin, direction)) ci.cancel();
            } else if (action == GLFW.GLFW_RELEASE) {
                if (IceStomModClient.getInstance().getGizmoHandler().mouseUp()) ci.cancel();
            }
        }
    }
}
