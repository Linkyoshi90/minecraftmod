package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.ai.ShareBedGoal;
import com.linkyoshi90.harmontide.network.ModPayloads;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Calling out from bed (sneak + right click): the player lies awake instead of drifting off, so that friends have
 * time to come over. Who comes and when is still up to {@link ShareBedGoal} and its one roll per night. Once someone
 * is in the bed the client offers to skip the night, which simply lets the vanilla sleep run; leaving the bed cancels.
 */
public final class BedCall {
    /** What the client is told: not calling, waiting for an answer, someone has joined. */
    public static final int NONE = 0;
    public static final int WAITING = 1;
    public static final int JOINED = 2;
    private static final int UNANSWERED_TICKS = 600;

    private static final Set<ServerPlayer> CLICKED = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<ServerPlayer, Call> CALLS = new WeakHashMap<>();

    private BedCall() {}

    private static final class Call {
        private int stage = WAITING;
        private int ticks;
    }

    /** They sneak-clicked a bed. Whether they get in is the vanilla bed's business; {@link #tick} finds out. */
    public static void bedClicked(ServerPlayer player) {
        CLICKED.add(player);
    }

    public static void begin(ServerPlayer player) {
        CALLS.put(player, new Call());
        player.displayClientMessage(Component.translatable(Nsfw.callKey()), false);
        send(player, WAITING);
    }

    public static boolean isCalling(ServerPlayer player) {
        return CALLS.containsKey(player);
    }

    /** Whether someone within earshot of her is calling from bed: her patrol waits until that is over. */
    public static boolean calledNear(Mob mob) {
        for (ServerPlayer player : CALLS.keySet()) {
            if (player.level() == mob.level() && player.distanceToSqr(mob) <= ShareBedGoal.RANGE * ShareBedGoal.RANGE) return true;
        }
        return false;
    }

    public static int stage(ServerPlayer player) {
        Call call = CALLS.get(player);
        return call == null ? NONE : call.stage;
    }

    /** Every tick, for every player: holds the night off while a call is going and keeps the client up to date. */
    public static void tick(ServerPlayer player) {
        if (CLICKED.remove(player) && player.isSleeping()) begin(player);
        Call call = CALLS.get(player);
        if (call == null) return;
        if (!player.isSleeping()) {   // cancelled, attacked or morning
            CALLS.remove(player);
            send(player, NONE);
            return;
        }
        player.sleepCounter = 0;
        // a full-moon turn ends with her out of the bed again, which still counts as an answer
        int stage = inBedWith(player) || call.stage == JOINED && BedSharing.fullMoon(player.level()) ? JOINED : WAITING;
        if (stage != call.stage) {
            call.stage = stage;
            send(player, stage);
        }
        if (stage == WAITING && ++call.ticks == UNANSWERED_TICKS) {
            player.displayClientMessage(Component.translatable("message.harmontide.call_unanswered"), false);
        }
    }

    /** The skip-the-night button: stop holding the night and let the vanilla sleep take over. */
    public static void skipNight(ServerPlayer player) {
        if (stage(player) != JOINED) return;
        CALLS.remove(player);
        send(player, NONE);
    }

    private static boolean inBedWith(ServerPlayer player) {
        UUID sleeper = player.getUUID();
        return !player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(ShareBedGoal.RANGE),
                m -> m instanceof Bonded b && sleeper.equals(b.getGestation().getSharingWith())).isEmpty();
    }

    private static void send(ServerPlayer player, int stage) {
        if (player.connection.hasChannel(ModPayloads.BedCallStage.TYPE)) PacketDistributor.sendToPlayer(player, new ModPayloads.BedCallStage(stage));
    }
}
