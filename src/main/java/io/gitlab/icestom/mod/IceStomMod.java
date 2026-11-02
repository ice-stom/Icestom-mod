package io.gitlab.icestom.mod;

import io.gitlab.icestom.mod.network.ProtoChannel;
import io.gitlab.icestom.mod.network.channels.ConfigurationHandler;
import io.gitlab.icestom.mod.network.channels.TimetrialHandler;
import io.gitlab.icestom.mod.network.channels.TrackHandler;
import io.gitlab.icestom.protocol.ISChannel;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IceStomMod implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(IceStomMod.class);

    public static final String MOD_ID = "icestom";

    public static final boolean UNSTABLE = true;

    public static final ProtoChannel CONFIGURATION = new ProtoChannel(ISChannel.CONFIGURATION, new ConfigurationHandler());
    public static final ProtoChannel TIMETRIAL = new ProtoChannel(ISChannel.TIMETRIAL, new TimetrialHandler());
    public static final ProtoChannel TRACK = new ProtoChannel(ISChannel.TRACK, new TrackHandler());

    public static final Version VERSION = FabricLoader.getInstance().getModContainer(IceStomMod.MOD_ID).get().getMetadata().getVersion();

    public void onInitialize() {
        CONFIGURATION.registerCodec();
        TIMETRIAL.registerCodec();
        TRACK.registerCodec();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
