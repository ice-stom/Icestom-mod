package io.gitlab.icestom.mod.compat;

import dev.o7moon.openboatutils.ISettingContext;
import dev.o7moon.openboatutils.MutableContext;
import dev.o7moon.openboatutils.network.ClientboundSettingsPacket;
import io.gitlab.icestom.stomtrack.shadow.io.github.openboatutils.protocol.PacketWriter;
import io.gitlab.icestom.stomtrack.shadow.io.github.openboatutils.protocol.channels.OBUSettingsPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.util.List;

public class TrackSettingContext extends MutableContext {

    public void apply(List<OBUSettingsPacket> packets) {
        FriendlyByteBufWriter writer = new FriendlyByteBufWriter();

        try {
            new OBUSettingsPacket.CompoundPayload(packets).write(writer);
        } catch (IOException ignored) {}

        applyFrom(ISettingContext.VANILLA);
        ClientboundSettingsPacket.handleContextPacketPayload(this, writer.getByteBuf(), ClientboundSettingsPacket.COMPOUND, true);
    }


    @Override
    public void switchTo() {}

    public static class FriendlyByteBufWriter implements PacketWriter {

        FriendlyByteBuf byteBuf = PacketByteBufs.create();

        public FriendlyByteBuf getByteBuf() {
            return byteBuf;
        }

        @Override
        public byte[] toBytes() {
            throw new NotImplementedException();
        }

        @Override
        public void writeFloat(float v) throws IOException {
            byteBuf.writeFloat(v);
        }

        @Override
        public void writeBoolean(boolean b) throws IOException {
            byteBuf.writeBoolean(b);
        }

        @Override
        public void writeDouble(double v) throws IOException {
            byteBuf.writeDouble(v);
        }

        @Override
        public void writeShort(short i) throws IOException {
            byteBuf.writeShort(i);
        }

        @Override
        public void writeInt(int i) throws IOException {
            byteBuf.writeInt(i);
        }

        @Override
        public void writeLong(long l) throws IOException {
            byteBuf.writeLong(l);
        }

        @Override
        public void writeByte(byte b) throws IOException {
            byteBuf.writeByte(b);
        }

        @Override
        public void writeString(String s) throws IOException {
            byteBuf.writeUtf(s);
        }
    }

}
