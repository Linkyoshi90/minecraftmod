package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.compat.BondInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Friendship milestones: when a gain of affection carries her over one of {@link BondInfo#MILESTONES}, the player hears what
 * it opens up (a chat line and a chime). Losses say nothing.
 */
public final class Milestones {
    private static final String[] KEYS = {"friend", "close", "devoted", "fondest"};

    private Milestones() {}

    /** The milestone a change from before to after crosses on the way up, or -1. */
    public static int crossed(int before, int after) {
        int hit = -1;
        for (int i = 0; i < BondInfo.MILESTONES.length; i++) {
            if (before < BondInfo.MILESTONES[i] && after >= BondInfo.MILESTONES[i]) hit = i;
        }
        return hit;
    }

    /** Tells the player, if this gain crossed a milestone. */
    public static void after(Mob mob, @Nullable Player player, int before, int after) {
        int i = crossed(before, after);
        if (i < 0 || player == null || mob.level().isClientSide) return;
        player.displayClientMessage(Component.translatable("message.monstergirls.milestone." + KEYS[i], mob.getDisplayName())
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        if (player instanceof net.minecraft.server.level.ServerPlayer sp) sp.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.2F);
    }
}
