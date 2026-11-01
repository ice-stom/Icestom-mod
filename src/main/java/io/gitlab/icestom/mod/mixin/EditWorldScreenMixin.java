package io.gitlab.icestom.mod.mixin;

import io.gitlab.icestom.mod.screen.TrackExportScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EditWorldScreen.class)
public class EditWorldScreenMixin {
    @Shadow
    @Final
    private LevelStorageSource.LevelStorageAccess levelAccess;

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"
            ),
            index = 0
    )
    private LayoutElement replaceOptimizeButton(LayoutElement element) {
        if (!(element instanceof Button optimize))
            return element;

        if (!optimize.getMessage().equals(
                Component.translatable("selectWorld.edit.optimize")))
            return element;

        LinearLayout row = LinearLayout.horizontal().spacing(4);

        optimize.setWidth(120 - 4);

        row.addChild(optimize);
        row.addChild(Button.builder(Component.literal("Export Track"), b -> {
            Minecraft.getInstance().setScreen(
                    new TrackExportScreen(Minecraft.getInstance().screen, levelAccess)
            );
        }).width(80).build());

        return row;
    }
}
