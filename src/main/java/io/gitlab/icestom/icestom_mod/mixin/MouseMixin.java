package io.gitlab.icestom.icestom_mod.mixin;

import io.gitlab.icestom.icestom_mod.IceStomModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.phys.Vec3;
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

    @Inject(method = "onButton", at = {@At("HEAD")}, cancellable = true)
    private void onMouseButton(long window, MouseButtonInfo input, int action, CallbackInfo ci) {

        if (this.minecraft.player == null)
            return;
        if (this.minecraft.screen != null)
            return;

        Vec3 origin = this.minecraft.player.getEyePosition(1.0F);
        Vec3 direction = this.minecraft.player.getViewVector(1.0F);

        int gizmoButton = 0;
        if (input.button() == gizmoButton) {
            if (action == 1) {
                if (IceStomModClient.getInstance().getGizmoHandler().mouseDown(origin, direction)) {
                    ci.cancel();
                }
            } else if (action == 0 && IceStomModClient.getInstance().getGizmoHandler().mouseUp()) {
                ci.cancel();
            }
        }
    }
}
