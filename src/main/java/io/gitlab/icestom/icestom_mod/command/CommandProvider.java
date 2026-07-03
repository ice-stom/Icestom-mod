package io.gitlab.icestom.icestom_mod.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

@Environment(EnvType.CLIENT)
public interface CommandProvider {
    LiteralArgumentBuilder<FabricClientCommandSource> command();
}
