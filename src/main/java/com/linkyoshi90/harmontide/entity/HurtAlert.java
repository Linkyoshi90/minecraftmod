package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * When a friend's health drops below {@link #LINE} of her most, her friends within {@link #REACH} hear it: a line over
 * the hotbar (who, and how far) and a low chime. Only once per drop: she has to heal back over the line first.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class HurtAlert {
    public static final float LINE = 0.3F;
    public static final double REACH = 64;

    private HurtAlert() {}

    /** The friends who were warned, if this blow took her under the line. */
    public static List<ServerPlayer> warn(Mob mob, float before) {
        List<ServerPlayer> warned = new ArrayList<>();
        float line = mob.getMaxHealth() * LINE;
        if (!com.linkyoshi90.harmontide.HarmontideConfig.friendNotices() || !(mob instanceof Bonded b) || !(mob.level() instanceof ServerLevel level) || !mob.isAlive() || before < line || mob.getHealth() >= line) return warned;
        for (ServerPlayer p : level.players()) {
            if (b.getAffection(p) < Orders.AFFECTION || p.distanceToSqr(mob) > REACH * REACH) continue;
            p.displayClientMessage(Component.translatable("message.harmontide.friend_hurt", mob.getDisplayName(), (int) p.distanceTo(mob))
                    .withStyle(ChatFormatting.RED), true);
            p.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.NEUTRAL, 0.7F, 0.6F);
            warned.add(p);
        }
        return warned;
    }

    @SubscribeEvent
    public static void onHurt(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide) warn(mob, mob.getHealth() + event.getNewDamage());
    }
}
