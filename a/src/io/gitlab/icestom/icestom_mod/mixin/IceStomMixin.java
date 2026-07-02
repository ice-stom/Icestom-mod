package io.gitlab.icestom.icestom_mod.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MinecraftServer.class})
public class IceStomMixin {
   @Inject(
      at = {@At("HEAD")},
      method = {"method_3735"}
   )
   private void init(CallbackInfo info) {
   }
}
