package com.linkyoshi90.harmontide.network;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.client.BedCallScreen;
import com.linkyoshi90.harmontide.entity.BedCall;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** What travels between client and server: so far only the call from bed (see {@link BedCall}). */
public final class ModPayloads {
    private ModPayloads() {}

    /** Server to client: where the player's call stands. */
    public record BedCallStage(int stage) implements CustomPacketPayload {
        public static final Type<BedCallStage> TYPE = new Type<>(HarmontideMod.id("bed_call_stage"));
        public static final StreamCodec<ByteBuf, BedCallStage> CODEC = ByteBufCodecs.VAR_INT.map(BedCallStage::new, BedCallStage::stage);

        @Override
        public Type<BedCallStage> type() {
            return TYPE;
        }
    }

    /** Client to server: the skip-the-night button. */
    public record SkipNight() implements CustomPacketPayload {
        public static final Type<SkipNight> TYPE = new Type<>(HarmontideMod.id("skip_night"));
        public static final StreamCodec<ByteBuf, SkipNight> CODEC = StreamCodec.unit(new SkipNight());

        @Override
        public Type<SkipNight> type() {
            return TYPE;
        }
    }

    /** Server to client: this creature is (or no longer is) possessed by the Demon Realm, so her eyes glow (client/PossessedEyesLayer). */
    public record Possessed(int entityId, boolean on) implements CustomPacketPayload {
        public static final Type<Possessed> TYPE = new Type<>(HarmontideMod.id("possessed"));
        public static final StreamCodec<ByteBuf, Possessed> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Possessed::entityId,
                ByteBufCodecs.BOOL, Possessed::on, Possessed::new);

        @Override
        public Type<Possessed> type() {
            return TYPE;
        }
    }

    /** Server to client: a rose moon has risen (or set), so the sky turns rose (entity/RoseMoon, client/RealmAtmosphere). */
    public record RoseMoonSky(boolean on) implements CustomPacketPayload {
        public static final Type<RoseMoonSky> TYPE = new Type<>(HarmontideMod.id("rose_moon"));
        public static final StreamCodec<ByteBuf, RoseMoonSky> CODEC = ByteBufCodecs.BOOL.map(RoseMoonSky::new, RoseMoonSky::on);

        @Override
        public Type<RoseMoonSky> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(BedCallStage.TYPE, BedCallStage.CODEC, (payload, context) -> BedCallScreen.setStage(payload.stage()));
        registrar.playToClient(Possessed.TYPE, Possessed.CODEC,
                (payload, context) -> com.linkyoshi90.harmontide.client.PossessedEyesLayer.set(payload.entityId(), payload.on()));
        registrar.playToClient(RoseMoonSky.TYPE, RoseMoonSky.CODEC, (payload, context) -> com.linkyoshi90.harmontide.client.RealmAtmosphere.roseMoon(payload.on()));
        registrar.playToServer(SkipNight.TYPE, SkipNight.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) BedCall.skipNight(player);
        });
    }
}
