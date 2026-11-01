package io.gitlab.icestom.mod.command.nodes;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface DefaultTrackFeatureConstructor<Feature, Extra> {
    Feature construct(Player player, Extra extra);
}
