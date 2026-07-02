package io.gitlab.icestom.icestom_mod.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface CommandProvider {
   LiteralArgumentBuilder command();
}
