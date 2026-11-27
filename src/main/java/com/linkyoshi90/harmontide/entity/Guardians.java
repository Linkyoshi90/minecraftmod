package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import javax.annotation.Nullable;

/**
 * Each kind's way of looking after a friend in trouble (alongside the lamia's coils, entity/Traversal, and the banshee's keening).
 *
 * <ul>
 *   <li><b>A fox's veil.</b> A kitsune friend (25+) near a friend struck down to {@link #VEIL_HEALTH} of their health wraps them
 *   in an illusion: they vanish for a few seconds, and whatever was after them loses them. Once a minute.</li>
 *   <li><b>A mummy's bandages.</b> A mummy friend (10+) binds the wounds of a friend struck down to half their health:
 *   Regeneration II for a while. Once every two minutes.</li>
 * </ul>
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Guardians {
    public static final double NEAR = 10;
    public static final float VEIL_HEALTH = 0.4F, BANDAGE_HEALTH = 0.5F;
    public static final int VEIL_REST = 1200, BANDAGE_REST = 2400;

    private Guardians() {}

    @Nullable
    static AbstractMonsterGirl helper(ServerPlayer p, java.util.function.Predicate<AbstractMonsterGirl> who, int affection, String rest) {
        long now = p.level().getGameTime();
        for (AbstractMonsterGirl g : p.level().getEntitiesOfClass(AbstractMonsterGirl.class, p.getBoundingBox().inflate(NEAR),
                g -> g.isAlive() && !g.isBaby() && who.test(g) && g.getAffection(p) >= affection && now >= g.getPersistentData().getLong(rest))) return g;
        return null;
    }

    /** A kitsune's veil over a badly hurt friend. Her, or null. */
    @Nullable
    public static AbstractMonsterGirl veil(ServerPlayer p) {
        if (!HarmontideConfig.instincts() || p.getHealth() > p.getMaxHealth() * VEIL_HEALTH) return null;
        AbstractMonsterGirl fox = helper(p, g -> g.getSpecies().family() == GirlSpecies.Family.KITSUNE, Bonded.BED_AFFECTION, "VeilRestUntil");
        if (fox == null) return null;
        fox.getPersistentData().putLong("VeilRestUntil", p.level().getGameTime() + VEIL_REST);
        p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, true, true));
        for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(24), m -> m.getTarget() == p)) m.setTarget(null);
        ServerLevel level = p.serverLevel();
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX(), p.getY() + 1.0, p.getZ(), 16, 0.5, 0.8, 0.5, 0.02);
        fox.playSound(SoundEvents.FOX_SCREECH, 0.8F, 1.3F);
        p.displayClientMessage(Component.translatable("message.harmontide.fox_veil", fox.getDisplayName()), true);
        return fox;
    }

    /** A mummy binds a friend's wounds. Her, or null. */
    @Nullable
    public static AbstractMonsterGirl bandage(ServerPlayer p) {
        if (!HarmontideConfig.instincts() || p.getHealth() > p.getMaxHealth() * BANDAGE_HEALTH) return null;
        AbstractMonsterGirl mummy = helper(p, g -> g.getSpecies() == GirlSpecies.MUMMY_UNDEAD, Orders.AFFECTION, "BandageRestUntil");
        if (mummy == null) return null;
        mummy.getPersistentData().putLong("BandageRestUntil", p.level().getGameTime() + BANDAGE_REST);
        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, true, true));
        mummy.playSound(SoundEvents.WOOL_PLACE, 1.0F, 0.8F);
        p.displayClientMessage(Component.translatable("message.harmontide.mummy_bandage", mummy.getDisplayName()), true);
        return mummy;
    }

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !p.isAlive() || event.getNewDamage() <= 0 || p.isSpectator()) return;
        if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker != p) veil(p);
        bandage(p);
    }
}
