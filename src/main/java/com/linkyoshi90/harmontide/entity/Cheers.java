package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Cheers: when a player earns an advancement worth announcing (not a recipe, not a hidden root), the grown friends around them
 * cheer: a little burst of joy over each, a chime, and a line naming who cheered. Only a celebration: the deeds behind most of our
 * advancements already bring friends closer, and counting it twice would skew them. Part of the comfort setting.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Cheers {
    public static final double RANGE = 16.0;

    private Cheers() {
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var display = event.getAdvancement().value().display();
        if (display.isEmpty() || !display.get().shouldAnnounceChat() || display.get().isHidden()) return;
        cheer(player);
    }

    /** Every grown friend within RANGE cheers for them. Who cheered. */
    public static List<LivingEntity> cheer(ServerPlayer player) {
        List<LivingEntity> out = new ArrayList<>();
        if (!HarmontideConfig.comfort() || !(player.level() instanceof ServerLevel level)) return out;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RANGE),
                e -> e instanceof Bonded b && e.isAlive() && !e.isBaby() && b.getAffection(player) >= Orders.AFFECTION)) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, e.getX(), e.getEyeY() + 0.5, e.getZ(), 6, 0.4, 0.3, 0.4, 0);
            if (e instanceof net.minecraft.world.entity.Mob m) m.getLookControl().setLookAt(player);
            out.add(e);
        }
        if (out.isEmpty()) return out;
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.4F);
        player.displayClientMessage((out.size() == 1 ? Component.translatable("message.harmontide.cheer", out.get(0).getDisplayName())
                : Component.translatable("message.harmontide.cheer.many", out.get(0).getDisplayName(), out.size() - 1)).withStyle(ChatFormatting.GOLD), true);
        return out;
    }
}
