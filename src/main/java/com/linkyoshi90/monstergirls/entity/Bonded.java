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

    int getAffection(Player player);

    void addAffection(Player player, int amount);

    /** Sets her affection for a player who may not be online (a newborn's father). */
    void setAffection(UUID player, int amount);

    /** One line for /monstergirls bonds. */
    Component describeBond(Player player);

    Gestation getGestation();

    /** Grown up, not already expecting or resting from it, and able to leave her home terrain. */
    boolean canShareBed();

    /** The player who fathered her, if she was born to one. She never shares their bed. */
    @Nullable
    UUID getFather();

    void setFather(@Nullable UUID father);

    /** She has climbed into the bed and settled down for the night. */
    default void settleInBed() {
    }

    /** She gets up again. */
    default void leaveBed() {
    }

    /** Ordered to sit with a stick (and not yet told to get up). */
    boolean isOrderedToSit();

    void setOrderedToSit(boolean sit);

    default boolean trustsEnoughForBed(Player player) {
        return this.getAffection(player) >= BED_AFFECTION && !player.getUUID().equals(this.getFather());
    }

    /**
     * The chance that she joins a trusting player in bed on a given night: one in three at {@link #BED_AFFECTION},
     * rising to certain at {@link #FULL_AFFECTION}.
     */
    static float bedChance(int affection) {
        if (affection < BED_AFFECTION) return 0.0F;
        return Math.min(1.0F, 0.33F + 0.67F * (affection - BED_AFFECTION) / (FULL_AFFECTION - BED_AFFECTION));
    }

    /** A newborn: devoted to her father from the start. */
    default void bornTo(@Nullable UUID father) {
        if (father == null) return;
        this.setFather(father);
        this.setAffection(father, FULL_AFFECTION);
    }
}
