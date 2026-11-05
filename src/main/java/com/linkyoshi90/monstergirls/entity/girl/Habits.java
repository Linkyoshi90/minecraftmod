package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.entity.Clade;
import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Habits of a branch of the tree of life (entity.Clade), part of the comfort setting:
 * <ul>
 * <li>Canids howl at the full moon, once a night, and every canid in earshot answers; a friend out under the moon with one
 * of them is a little closer for it.</li>
 * <li>Primates groom: a friend beside you picks out one harmful effect (poison, hunger, slowness and the like), every two minutes.</li>
 * </ul>
 */
public final class Habits {
    public static final String HOWLED = "HowledNight", GROOM_AT = "GroomAt";
    public static final double EARSHOT = 48.0, MOON_FRIEND = 16.0, GROOM_REACH = 3.0;
    public static final int GROOM_GAP = 2400;

    private Habits() {
    }

    // --- the full-moon howl -----------------------------------------------------------------------------------------

    public static boolean fullMoonNight(ServerLevel level) {
        long time = level.getDayTime() % 24000L;
        return level.getMoonPhase() == 0 && time > 13000 && time < 23000 && !level.isRaining();
    }

    private static long night(ServerLevel level) {
        return (level.getDayTime() + 12000L) / 24000L;                 // one number for the whole night, either side of midnight
    }

    /** Every ten seconds for a grown canid: on a full-moon night under open sky she may lead a howl. How many joined in (her included), 0 if she didn't. */
    public static int tick(ServerLevel level, AbstractMonsterGirl girl) {
        if (!MonsterGirlsConfig.comfort() || girl.isBaby() || girl.getSpecies().clade() != Clade.CANIDAE || !fullMoonNight(level)
                || !level.canSeeSky(girl.blockPosition().above()) || girl.getTarget() != null || girl.getRandom().nextInt(4) != 0) return 0;
        return howl(level, girl);
    }

    /** She howls at the moon, and every canid in earshot who hasn't tonight answers. How many howled. */
    public static int howl(ServerLevel level, AbstractMonsterGirl lead) {
        long night = night(level);
        if (lead.getPersistentData().getLong(HOWLED) == night + 1) return 0;
        int n = 0;
        for (AbstractMonsterGirl g : level.getEntitiesOfClass(AbstractMonsterGirl.class, lead.getBoundingBox().inflate(EARSHOT),
                g -> g.getSpecies().clade() == Clade.CANIDAE && !g.isBaby() && g.getPersistentData().getLong(HOWLED) != night + 1)) {
            g.getPersistentData().putLong(HOWLED, night + 1);
            if (g.getSpecies().family() == GirlSpecies.Family.CANID) g.startTimedAction(AbstractMonsterGirl.Action.HOWL, 40);
            g.playSound(SoundEvents.WOLF_HOWL, 2.0F, 0.9F + g.getRandom().nextFloat() * 0.3F);
            n++;
            Player friend = g.bondedPlayer(Orders.AFFECTION, MOON_FRIEND);
            if (friend != null && level.canSeeSky(friend.blockPosition().above())) {
                g.addAffection(friend, 2);
                if (friend instanceof ServerPlayer sp) sp.displayClientMessage(Component.translatable("message.monstergirls.moon_howl", g.getDisplayName())
                        .withStyle(net.minecraft.ChatFormatting.DARK_AQUA), true);
            }
        }
        return n;
    }

    // --- grooming ---------------------------------------------------------------------------------------------------

    /** Every five seconds for a primate: a friend right beside her with something harmful on them gets one effect picked out. The effect, or null. */
    @Nullable
    public static Holder<MobEffect> groom(ServerLevel level, AbstractMonsterGirl girl) {
        if (!MonsterGirlsConfig.comfort() || girl.isBaby() || girl.getSpecies().clade() != Clade.PRIMATES
                || level.getGameTime() < girl.getPersistentData().getLong(GROOM_AT)) return null;
        Player friend = girl.bondedPlayer(Orders.AFFECTION, GROOM_REACH);
        if (friend == null) return null;
        for (MobEffectInstance e : friend.getActiveEffects()) {
            if (e.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) continue;
            Holder<MobEffect> effect = e.getEffect();
            friend.removeEffect(effect);
            girl.getPersistentData().putLong(GROOM_AT, level.getGameTime() + GROOM_GAP);
            girl.getLookControl().setLookAt(friend);
            girl.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            girl.addAffection(friend, 1);
            level.sendParticles(ParticleTypes.HEART, friend.getX(), friend.getEyeY() + 0.4, friend.getZ(), 1, 0.2, 0.1, 0.2, 0);
            if (friend instanceof ServerPlayer sp) sp.displayClientMessage(Component.translatable("message.monstergirls.groomed", girl.getDisplayName(),
                    effect.value().getDisplayName()).withStyle(net.minecraft.ChatFormatting.GREEN), true);
            return effect;
        }
        return null;
    }
}
