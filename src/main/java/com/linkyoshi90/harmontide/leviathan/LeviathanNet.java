package com.linkyoshi90.harmontide.leviathan;

import com.linkyoshi90.harmontide.HarmontideMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** What clients are told about the Dream-Leviathan: where she is in the sky (client/DreamSky) and a player's blessing bar (client/DreamHeartHud). */
public final class LeviathanNet {
    private LeviathanNet() {
    }

    /** Where she is (shown = whether she is in the sky at all), how she flies, and how she looks. */
    public record Sky(boolean shown, float x, float y, float z, float yaw, float pitch, float vx, float vy, float vz, int look)
            implements CustomPacketPayload {
        public static final Type<Sky> TYPE = new Type<>(HarmontideMod.id("leviathan_sky"));
        public static final StreamCodec<ByteBuf, Sky> CODEC = new StreamCodec<>() {
            @Override
            public Sky decode(ByteBuf b) {
                return new Sky(b.readBoolean(), b.readFloat(), b.readFloat(), b.readFloat(), b.readFloat(), b.readFloat(), b.readFloat(),
                        b.readFloat(), b.readFloat(), b.readInt());
            }

            @Override
            public void encode(ByteBuf b, Sky s) {
                b.writeBoolean(s.shown);
                b.writeFloat(s.x);
                b.writeFloat(s.y);
                b.writeFloat(s.z);
                b.writeFloat(s.yaw);
                b.writeFloat(s.pitch);
                b.writeFloat(s.vx);
                b.writeFloat(s.vy);
                b.writeFloat(s.vz);
                b.writeInt(s.look);
            }
        };

        @Override
        public Type<Sky> type() {
            return TYPE;
        }

        /** Body mode (DreamLeviathan.FLYING ...), sickness 0..3 and rot stage 0..2, packed. */
        public int mode() {
            return this.look & 0xF;
        }

        public int sick() {
            return (this.look >> 4) & 0xF;
        }

        public int rot() {
            return (this.look >> 8) & 0xF;
        }
    }

    /** A player's blessing bar: whether they have it and how full. */
    public record Heart(boolean blessed, float pool) implements CustomPacketPayload {
        public static final Type<Heart> TYPE = new Type<>(HarmontideMod.id("dream_heart"));
        public static final StreamCodec<ByteBuf, Heart> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, Heart::blessed, ByteBufCodecs.FLOAT, Heart::pool, Heart::new);

        @Override
        public Type<Heart> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(Sky.TYPE, Sky.CODEC, (payload, context) -> context.enqueueWork(() -> com.linkyoshi90.harmontide.client.DreamSky.update(payload)));
        registrar.playToClient(Heart.TYPE, Heart.CODEC, (payload, context) -> context.enqueueWork(() ->
                com.linkyoshi90.harmontide.client.DreamHeartHud.update(payload.blessed(), payload.pool())));
    }

    public static Sky sky(ServerLevel level, LeviathanEvent e) {
        int rot = e.phase == LeviathanEvent.DEAD ? e.rotStage(level.getGameTime()) : 0;
        int look = e.bodyMode() | (e.sickness() << 4) | (rot << 8);
        return new Sky(e.phase != LeviathanEvent.IDLE, (float) e.x, (float) e.y, (float) e.z, e.yaw, e.pitch, (float) e.vx, (float) e.vy, (float) e.vz, look);
    }

    public static void sendSky(ServerLevel overworld, LeviathanEvent e) {
        Sky s = sky(overworld, e);
        for (ServerPlayer p : overworld.players()) {
            if (p.connection != null && p.connection.hasChannel(Sky.TYPE)) PacketDistributor.sendToPlayer(p, s);
        }
    }

    public static void sendHeart(ServerPlayer p) {
        if (p.connection == null || !p.connection.hasChannel(Heart.TYPE)) return;
        PacketDistributor.sendToPlayer(p, new Heart(DreamHeart.blessed(p), DreamHeart.pool(p)));
    }
}
