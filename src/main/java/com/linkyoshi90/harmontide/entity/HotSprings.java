package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.GeyseriteBlock;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Colds;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Hot springs (2026-11-08). Water over geyserite (block/GeyseriteBlock) is hot spring water: steaming, never frozen. Soaking in
 * it heals and warms (frost thaws off, a girl's cold goes, no girl catches a chill in it), and a long soak ({@link #WELL_SOAKED}
 * ticks) leaves a player well soaked: Health Boost for a while, once a day. A player's friends within {@link #CALL} blocks come
 * and soak with them, as in the tub; a wild girl soaking in the same spring warms to them a little. Springs form naturally in
 * cold and mountain country (world/HotSpringFeature); lay geyserite under a pool at home for an onsen. Off with the option hotSprings.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class HotSprings {
    public static final int EVERY = 40, CALL = 12, WELL_SOAKED = 1200, BOOST = 6000, NEAR = 4;
    private static final String SOAKED = "SpringSoaked", SOAK_DAY = "SpringSoakDay";

    private HotSprings() {}

    /** In hot spring water: in water, with geyserite just under it. */
    public static boolean inSpring(LivingEntity e) {
        if (!HarmontideConfig.hotSprings() || !e.isInWater() && !e.level().getFluidState(e.blockPosition()).is(net.minecraft.tags.FluidTags.WATER)) return false;
        BlockPos feet = e.blockPosition();
        for (int dy = 0; dy <= GeyseriteBlock.REACH; dy++) {
            if (e.level().getBlockState(feet.below(dy)).is(ModBlocks.GEYSERITE.get())) return true;
        }
        return false;
    }

    /** A soak: healed and warmed. */
    public static void warm(ServerLevel level, LivingEntity e) {
        e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EVERY + 20, 0, true, false));
        e.setTicksFrozen(0);
        if (e instanceof AbstractMonsterGirl g && Colds.hasCold(g)) {
            g.getPersistentData().remove(Colds.COLD);                     // a hot soak sweats it out
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, g.getX(), g.getEyeY(), g.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
        }
    }

    /** One round of a player's soak: warmed, maybe well soaked, friends called over. How many friends shared it. */
    public static int soak(ServerLevel level, Player p) {
        if (!inSpring(p)) {
            p.getPersistentData().putInt(SOAKED, 0);
            return 0;
        }
        warm(level, p);
        Dairy.award(p, "hot_spring");
        int soaked = p.getPersistentData().getInt(SOAKED) + EVERY;
        p.getPersistentData().putInt(SOAKED, soaked);
        long day = level.getDayTime() / 24000L;
        if (soaked >= WELL_SOAKED && p.getPersistentData().getLong(SOAK_DAY) != day + 1) {
            p.getPersistentData().putLong(SOAK_DAY, day + 1);
            p.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, BOOST, 1, true, true));
            p.displayClientMessage(Component.translatable("message.harmontide.hot_spring.soaked").withStyle(ChatFormatting.GOLD), true);
        }
        int shared = 0;
        for (Mob m : HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(CALL))) {
            if (!inSpring(m) || m.distanceToSqr(p) > NEAR * NEAR) {
                if (m.getNavigation().isDone()) m.getNavigation().moveTo(p.getX(), p.getY(), p.getZ(), 1.0);
                continue;
            }
            warm(level, m);
            if (HomeLife.soak(level, m, p)) shared++;
            Bathing.consider(level, m, p);
        }
        for (Mob m : level.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(NEAR), m -> m instanceof Bonded b && !b.hasAnyAffection() && inSpring(m))) {
            warm(level, m);
            if (HomeLife.soak(level, m, p)) shared++;                     // sharing a bath breaks the ice
        }
        return shared;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % EVERY == 17 && event.getEntity().level() instanceof ServerLevel level) soak(level, event.getEntity());
    }
}
