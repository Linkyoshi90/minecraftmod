package com.linkyoshi90.monstergirls.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** A monster girl who brings presents to her favourite players (see {@code GiftGoal}). */
public interface GiftGiver {
    /** A present for a favourite player, or empty if she has nothing to give. */
    ItemStack rollGift(Player player);

    /** Whether she drops her presents into a chest that holds the same thing, rather than handing them over. */
    default boolean storesGiftsAway() {
        return false;
    }

    boolean isGiftDue();

    void scheduleNextGift();

    void setNextGiftTime(long time);

    /** The little call she makes when she hands a present over. */
    SoundEvent getChirpSound();
}
