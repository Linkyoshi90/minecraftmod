package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A legend's relic (entity/LegendTrials), given to whoever passes her trial. Each works while it is carried; two also have a use.
 * <ul>
 *   <li>SKY_PLUME: falling, you drift down (Slow Falling).</li>
 *   <li>TIDE_PEARL: in water you breathe and swim like a dolphin.</li>
 *   <li>SERPENT_EYE: use it on a creature you look at within {@link #GAZE} blocks: it is turned to near-stone for a while
 *   (heavy Slowness and Weakness). Never on your friends. Cooldown {@link #GAZE_COOLDOWN}.</li>
 *   <li>DRAGON_SCALE: on fire or in lava, the fire can't hurt you (Fire Resistance).</li>
 *   <li>MOON_MIRROR: at night you see in the dark; use it and every creature within {@link #REVEAL} blocks glows for a while.
 *   Cooldown {@link #REVEAL_COOLDOWN}.</li>
 * </ul>
 */
public class RelicItem extends Item {
    public enum Relic { SKY_PLUME, TIDE_PEARL, SERPENT_EYE, DRAGON_SCALE, MOON_MIRROR }

    public static final int GAZE = 16, GAZE_TICKS = 100, GAZE_COOLDOWN = 600, REVEAL = 32, REVEAL_TICKS = 200, REVEAL_COOLDOWN = 1200;

    private final Relic relic;

    public RelicItem(Relic relic, Properties properties) {
        super(properties);
        this.relic = relic;
    }

    public Relic relic() {
        return this.relic;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player p) || p.tickCount % 20 != 0) return;
        carried(this.relic, level, p);
    }

    /** What a carried relic does for its bearer, once a second. Whether it did anything just now. */
    public static boolean carried(Relic relic, Level level, Player p) {
        switch (relic) {
            case SKY_PLUME -> {
                if (p.onGround() || p.isInWater() || p.getDeltaMovement().y >= -0.2 || p.getAbilities().flying) return false;
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, true, false, true));
                return true;
            }
            case TIDE_PEARL -> {
                if (!p.isInWater()) return false;
                p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 220, 0, true, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, true, false, true));
                return true;
            }
            case DRAGON_SCALE -> {
                if (!p.isOnFire() && !p.isInLava()) return false;
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, true, false, true));
                return true;
            }
            case MOON_MIRROR -> {
                if (level.isDay()) return false;
                MobEffectInstance nv = p.getEffect(MobEffects.NIGHT_VISION);
                if (nv == null || nv.getDuration() < 240) p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false, true));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.relic != Relic.SERPENT_EYE && this.relic != Relic.MOON_MIRROR) return InteractionResultHolder.pass(stack);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        boolean done = this.relic == Relic.SERPENT_EYE ? gaze(server, player) != null : reveal(server, player) > 0;
        if (!done) return InteractionResultHolder.fail(stack);
        player.getCooldowns().addCooldown(this, this.relic == Relic.SERPENT_EYE ? GAZE_COOLDOWN : REVEAL_COOLDOWN);
        return InteractionResultHolder.consume(stack);
    }

    /** The serpent's gaze on what the player looks at: who was turned, or null. */
    @Nullable
    public static Mob gaze(ServerLevel level, Player player) {
        Vec3 eye = player.getEyePosition(), look = player.getViewVector(1.0F);
        Mob best = null;
        double bestDot = 0.95;
        for (Mob m : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(GAZE), Mob::isAlive)) {
            if (m instanceof Bonded b && b.getAffection(player) >= Orders.AFFECTION) continue;
            Vec3 to = m.getBoundingBox().getCenter().subtract(eye);
            if (to.length() > GAZE) continue;
            double dot = to.normalize().dot(look);
            if (dot > bestDot && player.hasLineOfSight(m)) {
                bestDot = dot;
                best = m;
            }
        }
        if (best == null) return null;
        best.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, GAZE_TICKS, 6));
        best.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, GAZE_TICKS, 2));
        best.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, GAZE_TICKS, 3));
        level.sendParticles(ParticleTypes.ASH, best.getX(), best.getY() + best.getBbHeight() * 0.6, best.getZ(), 30, 0.4, 0.6, 0.4, 0.0);
        level.playSound(null, best.blockPosition(), SoundEvents.STONE_PLACE, SoundSource.PLAYERS, 1.0F, 0.6F);
        return best;
    }

    /** The moon mirror's light: everything living within REVEAL glows. How many. */
    public static int reveal(ServerLevel level, Player player) {
        List<LivingEntity> seen = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(REVEAL), e -> e != player && e.isAlive());
        for (LivingEntity e : seen) e.addEffect(new MobEffectInstance(MobEffects.GLOWING, REVEAL_TICKS, 0, false, false));
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
        return seen.size();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.monstergirls." + this.relic.name().toLowerCase(java.util.Locale.ROOT) + ".tip")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
