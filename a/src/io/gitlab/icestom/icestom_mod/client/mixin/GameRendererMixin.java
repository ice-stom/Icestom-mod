package io.gitlab.icestom.icestom_mod.mixin;

import io.gitlab.icestom.icestom_mod.client.IceStomModClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({class_757.class})
public class GameRendererMixin {
   @Inject(
      method = {"close"},
      at = {@At("RETURN")}
   )
   private void onGameRendererClose(CallbackInfo ci) {
      IceStomModClient.getInstance().closePipelines();
   }
}
