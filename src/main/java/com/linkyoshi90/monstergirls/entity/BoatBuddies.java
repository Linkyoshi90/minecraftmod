package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.vehicle.Boat;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;

/**
 * A friend told to follow you takes the free seat when you get into a boat (within {@link #REACH}), and gets out when you
 * do. Flyers fly alongside instead, mermaids swim, and anyone too broad for a boat stays ashore.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class BoatBuddies {
    public static final double REACH = 6;
    public static final float MAX_WIDTH = 1.0F;
    private static final String KEY = "BoatBuddyOf";

    private BoatBuddies() {}

    /** Whether she can come along in a boat at all. */
    public static boolean fits(Mob m) {
        if (m instanceof AbstractHarpy || m instanceof Merfolk) return false;
        if (m instanceof AbstractMonsterGirl g && g.canFly()) return false;
        return m.getBbWidth() <= MAX_WIDTH;
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer p && p.tickCount % 10 == 0) tick(p);
    }

    /** Fills the free seat of her boat, or lets her buddies out once she has left it. */
    public static void tick(ServerPlayer player) {
        if (player.getVehicle() instanceof Boat boat) {
            if (boat.getPassengers().size() >= 2 || boat.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)) return;
            Mob buddy = nearestFollower(player);
            if (buddy != null && buddy.startRiding(boat)) buddy.getPersistentData().putUUID(KEY, player.getUUID());
            return;
        }
        for (Boat boat : player.level().getEntitiesOfClass(Boat.class, player.getBoundingBox().inflate(16))) {
            for (Entity e : boat.getPassengers()) {
                if (e instanceof Mob m && m.getPersistentData().hasUUID(KEY) && player.getUUID().equals(m.getPersistentData().getUUID(KEY))) {
                    m.getPersistentData().remove(KEY);
                    m.stopRiding();
                    return;                                                    // the passenger list changed: the next tick sees to any other
                }
            }
        }
    }

    @Nullable
    private static Mob nearestFollower(ServerPlayer player) {
        Mob best = null;
        for (Mob m : Travelling.party(player.serverLevel(), player)) {
            if (!fits(m) || m.distanceToSqr(player) > REACH * REACH) continue;
            if (best == null || m.distanceToSqr(player) < best.distanceToSqr(player)) best = m;
        }
        return best;
    }
}
