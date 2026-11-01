package com.linkyoshi90.monstergirls.network;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.client.BedCallScreen;
import com.linkyoshi90.monstergirls.entity.BedCall;
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
        public static final Type<BedCallStage> TYPE = new Type<>(MonsterGirlsMod.id("bed_call_stage"));
        public static final StreamCodec<ByteBuf, BedCallStage> CODEC = ByteBufCodecs.VAR_INT.map(BedCallStage::new, BedCallStage::stage);

        @Override
        public Type<BedCallStage> type() {
            return TYPE;
        }
    }

    /** Client to server: the skip-the-night button. */
    public record SkipNight() implements CustomPacketPayload {
        public static final Type<SkipNight> TYPE = new Type<>(MonsterGirlsMod.id("skip_night"));
        public static final StreamCodec<ByteBuf, SkipNight> CODEC = StreamCodec.unit(new SkipNight());

        @Override
        public Type<SkipNight> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(BedCallStage.TYPE, BedCallStage.CODEC, (payload, context) -> BedCallScreen.setStage(payload.stage()));
        registrar.playToServer(SkipNight.TYPE, SkipNight.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) BedCall.skipNight(player);
        });
    }
}
