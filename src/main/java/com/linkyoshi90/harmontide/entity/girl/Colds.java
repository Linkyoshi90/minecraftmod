package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.HarmontideConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Colds: a girl left out in the rain or snow somewhere cold (not sea folk, the undead, constructs or slimes) grows chilled, and after
 * about two minutes of it may catch a cold. For three days she sneezes now and then and is a little slow. A warm soup or a honey
 * bottle from a hand cures it: {@link #CARE} friendship from whoever looked after her.
 */
public final class Colds {
    public static final int CHILLED = 24, CATCH = 3, CARE = 8;
    public static final long LASTS = 72000;
    public static final String CHILL = "Chill", COLD = "ColdUntil";
    static final Set<GirlSpecies.Family> HARDY = Set.of(GirlSpecies.Family.MERFOLK, GirlSpecies.Family.MOLLUSK, GirlSpecies.Family.CRUSTACEAN,
            GirlSpecies.Family.UNDEAD, GirlSpecies.Family.CONSTRUCT, GirlSpecies.Family.SLIME);

    private Colds() {
    }

    public static boolean hasCold(AbstractMonsterGirl girl) {
        return girl.level().getGameTime() < girl.getPersistentData().getLong(COLD);
    }

    public static boolean catches(AbstractMonsterGirl girl) {
        return !HARDY.contains(girl.getSpecies().family());
    }

    /** Wet through somewhere cold. */
    public static boolean chilly(ServerLevel level, AbstractMonsterGirl girl) {
        BlockPos at = girl.blockPosition();
        boolean wet = level.isRainingAt(at.above()) || girl.isInWater() || level.isRaining() && level.canSeeSky(at.above()) && level.getBiome(at).value().coldEnoughToSnow(at);
        return wet && level.getBiome(at).value().getBaseTemperature() < 0.5F && !com.linkyoshi90.harmontide.entity.HotSprings.inSpring(girl);   // never in a hot spring
    }

    public static void catchCold(ServerLevel level, AbstractMonsterGirl girl) {
        girl.getPersistentData().putLong(COLD, level.getGameTime() + LASTS);
        girl.getPersistentData().putInt(CHILL, 0);
        sneeze(level, girl);
    }

    private static void sneeze(ServerLevel level, AbstractMonsterGirl girl) {
        level.playSound(null, girl.blockPosition(), SoundEvents.PANDA_SNEEZE, SoundSource.NEUTRAL, 0.8F, 1.4F);
        level.sendParticles(ParticleTypes.SNEEZE, girl.getX() + girl.getLookAngle().x * 0.4, girl.getEyeY(), girl.getZ() + girl.getLookAngle().z * 0.4, 6, 0.1, 0.1, 0.1, 0.02);
    }

    /** Every five seconds (AbstractMonsterGirl's server tick): chill, a cold coming on, or one running its course. */
    public static void tick(ServerLevel level, AbstractMonsterGirl girl) {
        if (!HarmontideConfig.colds() || !catches(girl)) return;
        if (hasCold(girl)) {
            girl.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 0, false, false));
            if (level.random.nextInt(3) == 0) sneeze(level, girl);
            return;
        }
        int chill = girl.getPersistentData().getInt(CHILL);
        chill = chilly(level, girl) ? chill + 1 : Math.max(0, chill - 2);
        if (chill >= CHILLED) {
            chill = 0;
            if (level.random.nextInt(CATCH) == 0) {
                catchCold(level, girl);
                return;
            }
        }
        girl.getPersistentData().putInt(CHILL, chill);
    }

    public static boolean cures(ItemStack stack) {
        return stack.is(Items.MUSHROOM_STEW) || stack.is(Items.BEETROOT_SOUP) || stack.is(Items.RABBIT_STEW) || stack.is(Items.HONEY_BOTTLE);
    }

    /** Soup for a girl with a cold (AbstractMonsterGirl.mobInteract): she is better. */
    @Nullable
    public static InteractionResult interact(AbstractMonsterGirl girl, Player player, ItemStack stack) {
        if (!(girl.level() instanceof ServerLevel level) || !cures(stack) || !hasCold(girl)) return null;
        ItemStack left = stack.is(Items.HONEY_BOTTLE) ? new ItemStack(Items.GLASS_BOTTLE) : new ItemStack(Items.BOWL);
        stack.consume(1, player);
        if (!player.addItem(left)) player.drop(left, false);
        girl.getPersistentData().remove(COLD);
        girl.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        girl.addAffection(player, CARE);
        girl.blushFor(60);
        girl.playSound(girl.getEatSound(), 1.0F, girl.getVoicePitch());
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.4, girl.getZ(), 4, 0.3, 0.2, 0.3, 0);
        player.displayClientMessage(Component.translatable("message.harmontide.cold.cured", girl.getDisplayName()).withStyle(ChatFormatting.GREEN), true);
        return InteractionResult.SUCCESS;
    }
}
