package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.entity.ai.ShareBedGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * What the night leaves behind: anyone who shared a sleeping player's bed all the way to morning is now expecting.
 * Under a full moon it goes differently: every girl who trusts the sleeper answers the moon at once, and they take
 * their turns one after another (see {@link ShareBedGoal}); the night only passes once the last of them is done.
 */
public final class BedSharing {
    private static final double RADIUS = 12.0;

    private BedSharing() {}

    /** Called when the night is skipped (everyone sleeping, morning comes). Returns how many became expecting. */
    public static int onNightSkipped(ServerLevel level) {
        int expecting = 0;
        for (ServerPlayer player : level.players()) {
            if (!player.isSleeping()) continue;
            expecting += Monsterization.shareNight(level, player).size();   // possessed villagers slipped into the bed
            for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RADIUS), m -> m instanceof Bonded)) {
                if (player.getUUID().equals(((Bonded) mob).getGestation().getSharingWith()) && nightShared(level, player, mob)) expecting++;
            }
        }
        return expecting;
    }

    /** She shared the player's bed: her bed perk, and she is expecting if she can be. Returns whether she is. */
    public static boolean nightShared(ServerLevel level, ServerPlayer player, Mob mob) {
        Gestation gestation = ((Bonded) mob).getGestation();
        gestation.setSharingWith(null);
        if (mob instanceof com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl girl) girl.onNightShared(player);   // her bed perk
        if (!((Bonded) mob).canShareBed()) return false;
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight() + 0.3, mob.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
        if (!conceives(level, player, mob)) return false;
        gestation.conceive(player.getUUID());
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight() + 0.3, mob.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
        return true;
    }

    /** The moon never fails; otherwise it is the setting's odds, better for one who has missed them (Missing). */
    private static boolean conceives(ServerLevel level, ServerPlayer player, Mob mob) {
        return fullMoon(level) || net.neoforged.neoforge.gametest.GameTestHooks.isGametestServer()
                || mob.getRandom().nextFloat() < Missing.conceptionChance(mob, player);
    }

    /** A full moon in the night sky. */
    public static boolean fullMoon(Level level) {
        return level.getMoonPhase() == 0 && level.getDayTime() % 24000L >= 12000L;
    }

    /** Someone answering the moon has not had her turn with this sleeper yet, so the night must wait. */
    public static boolean nightWaits(Player player) {
        return player.isSleeping() && fullMoon(player.level()) && !player.level().getEntitiesOfClass(Mob.class,
                player.getBoundingBox().inflate(ShareBedGoal.RANGE), m -> m instanceof Bonded b && b.isMoonstruck()).isEmpty();
    }

    /** Keeps the sleeper just short of deep sleep (which is what lets the night pass) while the night waits. */
    public static void holdNight(ServerPlayer player) {
        if (player.sleepCounter >= 99 && nightWaits(player)) player.sleepCounter = 99;
    }
}
