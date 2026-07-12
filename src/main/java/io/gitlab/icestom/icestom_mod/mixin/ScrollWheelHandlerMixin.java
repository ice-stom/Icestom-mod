package io.gitlab.icestom.icestom_mod.mixin;

import io.gitlab.icestom.icestom_mod.IceStomModClient;
import io.gitlab.icestom.icestom_mod.render.hud.editor.TrackEditor;
import net.minecraft.client.ScrollWheelHandler;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScrollWheelHandler.class)
public class ScrollWheelHandlerMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(double x, double y, CallbackInfoReturnable<Vector2i> cir) {
        if (IceStomModClient.EDITOR_SPECIAL.isDown()) {
            TrackEditor editor = IceStomModClient.getInstance().trackEditor;

            if (editor.isFocused()) {
                editor.editorModeRelative(y < 0 ? -1 : 1);
                cir.setReturnValue(new Vector2i(0, 0));
            }
        }
    }
}
