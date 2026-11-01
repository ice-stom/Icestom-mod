package io.gitlab.icestom.mod.mixin.openboatutils;

import dev.o7moon.openboatutils.ISettingContext;
import dev.o7moon.openboatutils.OpenBoatUtils;
import io.gitlab.icestom.mod.IceStomModClient;
import io.gitlab.icestom.mod.compat.TrackSettingContext;
import io.gitlab.icestom.mod.track.Track;
import io.gitlab.icestom.stomtrack.shadow.io.github.openboatutils.protocol.channels.OBUSettingsPacket;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.WeakHashMap;

@Mixin(OpenBoatUtils.class)
public class OpenBoatUtilsCompat {

    @Unique private final WeakHashMap<Track, TrackSettingContext> icestom$cache = new WeakHashMap<>();
    @Unique private final WeakHashMap<Track, Integer> icestom$obuHashes = new WeakHashMap<>();

    @Inject(method = "getActiveContext", at = @At("HEAD"), cancellable = true)
    private void hookGetActiveContext(CallbackInfoReturnable<ISettingContext> cir) {
        if (!Minecraft.getInstance().hasSingleplayerServer()) return;

        Track track = IceStomModClient.getInstance()
                .trackHandler
                .getSelectedTrack();

        if (track == null) return;

        List<OBUSettingsPacket> packets = track.getOBUSettingsPackets();
        int hash = packets.hashCode();

        TrackSettingContext context = icestom$cache.get(track);

        if (context == null) {
            context = new TrackSettingContext();
            context.apply(packets);

            icestom$cache.put(track, context);
            icestom$obuHashes.put(track, hash);
        } else if (icestom$obuHashes.get(track) != hash) {
            context.apply(packets);
            icestom$obuHashes.put(track, hash);
        }

        cir.setReturnValue(context);
        cir.cancel();
    }
}
