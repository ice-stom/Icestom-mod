package io.gitlab.icestom.icestom_mod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IceStomMod implements ModInitializer {
    public static final String MOD_ID = "icestom_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger("icestom_mod");

    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("icestom_mod", path);
    }
}
