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

/** What travels between client and server: the call from bed ({@link BedCall}), the bucket close-up ({@link com.linkyoshi90.harmontide.entity.HandMilking}), and the question before a feed ({@link com.linkyoshi90.harmontide.entity.Breastfeed}). */
public final class ModPayloads {
    private ModPayloads() {}

    /** Client to server: jump pressed again in mid-air; someone with you may give an air step (entity/Traversal#airStep). */
    public record AirStep() implements CustomPacketPayload {
        public static final Type<AirStep> TYPE = new Type<>(HarmontideMod.id("air_step"));
        public static final StreamCodec<ByteBuf, AirStep> CODEC = StreamCodec.unit(new AirStep());

        @Override
        public Type<AirStep> type() {
            return TYPE;
        }
    }

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

    /** Server to client: open the bucket close-up on this girl. */
    public record OpenMilk(int token, int entityId, int need) implements CustomPacketPayload {
        public static final Type<OpenMilk> TYPE = new Type<>(HarmontideMod.id("open_milk"));
        public static final StreamCodec<ByteBuf, OpenMilk> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, OpenMilk::token,
                ByteBufCodecs.VAR_INT, OpenMilk::entityId, ByteBufCodecs.VAR_INT, OpenMilk::need, OpenMilk::new);

        @Override
        public Type<OpenMilk> type() {
            return TYPE;
        }
    }

    /** Client to server: one back-and-forth pull. */
    public record MilkStroke(int token) implements CustomPacketPayload {
        public static final Type<MilkStroke> TYPE = new Type<>(HarmontideMod.id("milk_stroke"));
        public static final StreamCodec<ByteBuf, MilkStroke> CODEC = ByteBufCodecs.VAR_INT.map(MilkStroke::new, MilkStroke::token);

        @Override
        public Type<MilkStroke> type() {
            return TYPE;
        }
    }

    /** Client to server: the screen closed early. Nothing is given. */
    public record CloseMilk(int token) implements CustomPacketPayload {
        public static final Type<CloseMilk> TYPE = new Type<>(HarmontideMod.id("close_milk"));
        public static final StreamCodec<ByteBuf, CloseMilk> CODEC = ByteBufCodecs.VAR_INT.map(CloseMilk::new, CloseMilk::token);

        @Override
        public Type<CloseMilk> type() {
            return TYPE;
        }
    }

    /** Server to client: she is asking whether a hungry player wants to be fed. */
    public record OpenFeed(int entityId) implements CustomPacketPayload {
        public static final Type<OpenFeed> TYPE = new Type<>(HarmontideMod.id("open_feed"));
        public static final StreamCodec<ByteBuf, OpenFeed> CODEC = ByteBufCodecs.VAR_INT.map(OpenFeed::new, OpenFeed::entityId);

        @Override
        public Type<OpenFeed> type() {
            return TYPE;
        }
    }

    /** Server to client: in her season, she asks to be taken standing. */
    public record OpenStand(int entityId) implements CustomPacketPayload {
        public static final Type<OpenStand> TYPE = new Type<>(HarmontideMod.id("open_stand"));
        public static final StreamCodec<ByteBuf, OpenStand> CODEC = ByteBufCodecs.VAR_INT.map(OpenStand::new, OpenStand::entityId);

        @Override
        public Type<OpenStand> type() {
            return TYPE;
        }
    }

    /** Client to server: yes or no to standing with her. */
    public record StandAnswer(int entityId, boolean yes) implements CustomPacketPayload {
        public static final Type<StandAnswer> TYPE = new Type<>(HarmontideMod.id("stand_answer"));
        public static final StreamCodec<ByteBuf, StandAnswer> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, StandAnswer::entityId,
                ByteBufCodecs.BOOL, StandAnswer::yes, StandAnswer::new);

        @Override
        public Type<StandAnswer> type() {
            return TYPE;
        }
    }

    /** Client to server: yes or no to that question. */
    public record FeedAnswer(int entityId, boolean yes) implements CustomPacketPayload {
        public static final Type<FeedAnswer> TYPE = new Type<>(HarmontideMod.id("feed_answer"));
        public static final StreamCodec<ByteBuf, FeedAnswer> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, FeedAnswer::entityId,
                ByteBufCodecs.BOOL, FeedAnswer::yes, FeedAnswer::new);

        @Override
        public Type<FeedAnswer> type() {
            return TYPE;
        }
    }

    /** Server to client: how many pulls count. need 0 means the session was dropped. */
    public record MilkProgress(int token, int strokes, int need, boolean done) implements CustomPacketPayload {
        public static final Type<MilkProgress> TYPE = new Type<>(HarmontideMod.id("milk_progress"));
        public static final StreamCodec<ByteBuf, MilkProgress> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, MilkProgress::token,
                ByteBufCodecs.VAR_INT, MilkProgress::strokes, ByteBufCodecs.VAR_INT, MilkProgress::need, ByteBufCodecs.BOOL, MilkProgress::done, MilkProgress::new);

        @Override
        public Type<MilkProgress> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(BedCallStage.TYPE, BedCallStage.CODEC, (payload, context) -> BedCallScreen.setStage(payload.stage()));
        registrar.playToClient(Possessed.TYPE, Possessed.CODEC,
                (payload, context) -> com.linkyoshi90.harmontide.client.PossessedEyesLayer.set(payload.entityId(), payload.on()));
        registrar.playToClient(RoseMoonSky.TYPE, RoseMoonSky.CODEC, (payload, context) -> com.linkyoshi90.harmontide.client.RealmAtmosphere.roseMoon(payload.on()));
        registrar.playToClient(OpenMilk.TYPE, OpenMilk.CODEC, (payload, context) -> context.enqueueWork(() ->
                com.linkyoshi90.harmontide.client.MilkingScreen.open(payload.token(), payload.entityId(), payload.need())));
        registrar.playToClient(MilkProgress.TYPE, MilkProgress.CODEC, (payload, context) -> context.enqueueWork(() ->
                com.linkyoshi90.harmontide.client.MilkingScreen.progress(payload.token(), payload.strokes(), payload.need(), payload.done())));
        registrar.playToClient(OpenFeed.TYPE, OpenFeed.CODEC, (payload, context) -> context.enqueueWork(() ->
                com.linkyoshi90.harmontide.client.FeedOfferScreen.open(payload.entityId())));
        registrar.playToClient(OpenStand.TYPE, OpenStand.CODEC, (payload, context) -> context.enqueueWork(() ->
                com.linkyoshi90.harmontide.client.StandOfferScreen.open(payload.entityId())));
        registrar.playToServer(AirStep.TYPE, AirStep.CODEC, (payload, context) -> context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) com.linkyoshi90.harmontide.entity.Traversal.airStep(player);
        }));
        registrar.playToServer(SkipNight.TYPE, SkipNight.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) BedCall.skipNight(player);
        });
        registrar.playToServer(MilkStroke.TYPE, MilkStroke.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) com.linkyoshi90.harmontide.entity.HandMilking.stroke(player, payload.token());
        });
        registrar.playToServer(CloseMilk.TYPE, CloseMilk.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) com.linkyoshi90.harmontide.entity.HandMilking.close(player, payload.token());
        });
        registrar.playToServer(FeedAnswer.TYPE, FeedAnswer.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) {
                if (payload.yes()) com.linkyoshi90.harmontide.entity.Breastfeed.accept(player, payload.entityId());
                else com.linkyoshi90.harmontide.entity.Breastfeed.decline(player, payload.entityId());
            }
        });
        registrar.playToServer(StandAnswer.TYPE, StandAnswer.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) {
                if (payload.yes()) com.linkyoshi90.harmontide.entity.Horniness.accept(player, payload.entityId());
                else com.linkyoshi90.harmontide.entity.Horniness.decline(player, payload.entityId());
            }
        });
    }
}
