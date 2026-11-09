package com.linkyoshi90.monstergirls.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/** A monster girl who builds a relationship with players: harpies, frogs and toads. */
public interface Bonded {
    /** Affection at which she is willing to spend the night beside you ("high friendship"). */
    int BED_AFFECTION = 25;
    /** Affection at which she starts bringing you little presents. */
    int GIFT_AFFECTION = BED_AFFECTION;
    /** The most there is: what a daughter feels for the player who fathered her. */
    int FULL_AFFECTION = 100;
    /** Anyone shorter than this looks like a child, whatever her age: no bed, no young (a fairy, not her queen). */
    float CHILDLIKE_HEIGHT = 1.25F;

    int getAffection(Player player);

    /** Drying out: she has to reach water, even if she was told to sit. */
    default boolean needsWater() {
        return false;
    }

    /** Pink cheeks for a while, at the milking machine. */
    default void blushFor(int ticks) {
    }

    void addAffection(Player player, int amount);

    /** Whether anyone at all is her friend (affection above nothing). */
    boolean hasAnyAffection();

    /** Sets her affection for a player who may not be online (a newborn's father). */
    void setAffection(UUID player, int amount);

    /** One line for /monstergirls bonds. */
    Component describeBond(Player player);

    Gestation getGestation();

    int getGrowTicks();

    void setGrowTicks(int ticks);

    /** Grown up, not already expecting or resting from it, and able to leave her home terrain. */
    boolean canShareBed();

    /** The player who fathered her, if she was born to one. She never shares their bed. */
    @Nullable
    UUID getFather();

    void setFather(@Nullable UUID father);

    /** Under a full moon: called to the bed of a player she trusts, eyes aglow, until she has had her turn (synced). */
    boolean isMoonstruck();

    void setMoonstruck(boolean moonstruck);

    /** She has climbed into the bed and settled down for the night. */
    default void settleInBed() {
    }

    /** She gets up again. */
    default void leaveBed() {
    }

    /** Ordered to sit with a stick (and not yet told to get up). */
    boolean isOrderedToSit();

    void setOrderedToSit(boolean sit);

    /** Her stick orders: where to be and when to fight (see {@link Orders}). */
    Orders getOrders();

    default boolean trustsEnoughForBed(Player player) {
        if (this instanceof net.minecraft.world.entity.Mob m && Monsterization.monsterized(m)) return !player.getUUID().equals(this.getFather());   // the realm changed her: any sleeper
        return this.getAffection(player) >= BED_AFFECTION && !player.getUUID().equals(this.getFather());
    }

    /**
     * The chance that she joins a trusting player in bed on a given night: one in three at {@link #BED_AFFECTION},
     * rising to certain at {@link #FULL_AFFECTION}.
     */
    /** Tonight's odds for this player (a succubus does not wait for friendship). */
    default float bedChanceFor(Player player) {
        if (this instanceof net.minecraft.world.entity.Mob m && Monsterization.monsterized(m)) return Monsterization.EAGER;
        return bedChance(this.getAffection(player));
    }

    static float bedChance(int affection) {
        if (affection < BED_AFFECTION) return 0.0F;
        return Math.min(1.0F, 0.33F + 0.67F * (affection - BED_AFFECTION) / (FULL_AFFECTION - BED_AFFECTION));
    }

    /** A newborn: devoted to her father from the start. */
    default void bornTo(@Nullable UUID father) {
        if (father == null) return;
        this.setFather(father);
        this.setAffection(father, FULL_AFFECTION);
        if (this instanceof net.minecraft.world.entity.Entity e && e.level() instanceof net.minecraft.server.level.ServerLevel level) {
            Player p = level.getServer().getPlayerList().getPlayer(father);
            if (p != null) Marriage.childBorn(p);                         // the family journal counts the little ones
        }
    }
}
