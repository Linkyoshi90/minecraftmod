package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.entity.Clade;
import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Basking, by branch of the tree of life (entity.Clade): snakes, lizards, turtles and crocodiles are cold-blooded. On a clear
 * morning one free to roam settles in the open sun for half a minute (once a day), and gets up warmed through: quicker on her
 * feet for five minutes. A friend who keeps her company in the sun is a little closer for it. Part of the comfort setting.
 */
public final class Basking {
    public static final String DAY = "BaskedDay";
    /** How long she lies in the sun, how long the warmth lasts, and the morning (day time) she basks in. */
    public static final int BASK = 600, WARM = 6000, MORNING_END = 5000;
    public static final double COMPANY = 5.0;

    private Basking() {
    }

    public static boolean coldBlooded(AbstractMonsterGirl girl) {
        Clade c = girl.getSpecies().clade();
        return (c == Clade.SQUAMATA || c == Clade.TESTUDINES || c == Clade.ARCHOSAURIA) && !girl.needsWater();
    }

    /** A clear morning, and open sky over her. */
    public static boolean sunny(ServerLevel level, AbstractMonsterGirl girl) {
        long time = level.getDayTime() % 24000L;
        return level.isDay() && time < MORNING_END && !level.isRaining() && level.canSeeSky(girl.blockPosition().above());
    }

    /** Every five seconds: a cold-blooded girl on a sunny morning may lie down to bask (once a day). Whether she did. */
    public static boolean tick(ServerLevel level, AbstractMonsterGirl girl) {
        if (!MonsterGirlsConfig.comfort() || !coldBlooded(girl) || !girl.onGround() || !RainMoods.free(girl) || !sunny(level, girl)) return false;
        long day = level.getDayTime() / 24000L;
        if (girl.getPersistentData().getLong(DAY) == day + 1 || girl.getAction() == AbstractMonsterGirl.Action.SITTING || girl.getRandom().nextInt(3) != 0) return false;
        return bask(level, girl);
    }

    /** She lies down in the sun and comes up warm. */
    public static boolean bask(ServerLevel level, AbstractMonsterGirl girl) {
        girl.getPersistentData().putLong(DAY, level.getDayTime() / 24000L + 1);
        girl.getNavigation().stop();
        girl.startTimedAction(AbstractMonsterGirl.Action.SITTING, BASK);
        girl.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BASK + WARM, 0, true, false));
        level.sendParticles(ParticleTypes.WAX_ON, girl.getX(), girl.getY() + 0.6, girl.getZ(), 6, 0.4, 0.3, 0.4, 0);
        company(level, girl);
        return true;
    }

    /** A friend in the sun beside her: a little closer (the day's bask). The friend, or null. */
    @Nullable
    public static Player company(ServerLevel level, AbstractMonsterGirl girl) {
        Player friend = girl.bondedPlayer(Orders.AFFECTION, COMPANY);
        if (friend == null || !level.canSeeSky(friend.blockPosition().above())) return null;
        girl.addAffection(friend, 1);
        if (friend instanceof net.minecraft.server.level.ServerPlayer sp) {
            sp.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.basking", girl.getDisplayName())
                    .withStyle(net.minecraft.ChatFormatting.GOLD), true);
        }
        return friend;
    }
}
