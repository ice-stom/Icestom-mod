package io.gitlab.icestom.icestom_mod.mixin;

import io.gitlab.icestom.icestom_mod.client.IceStomModClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11910;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({class_312.class})
public abstract class MouseMixin {
   @Shadow
   @Final
   private class_310 field_1779;

   @Inject(
      method = {"method_1601"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onMouseButton(long window, class_11910 input, int action, CallbackInfo ci) {
      if (this.field_1779.field_1755 == null) {
         assert this.field_1779.field_1724 != null;

         class_243 origin = this.field_1779.field_1724.method_5836(1.0F);
         class_243 direction = this.field_1779.field_1724.method_5828(1.0F);
         int gizmoButton = 0;
         if (input.comp_4801() == gizmoButton) {
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
}
