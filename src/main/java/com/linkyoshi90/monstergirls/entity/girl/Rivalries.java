package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

import static com.linkyoshi90.monstergirls.entity.girl.GirlSpecies.Family.*;

/**
 * Rivalries: some wild kinds can't stand each other: cats and mice, snakes and mice, wolves and rabbits, foxes and rabbits,
 * spiders and bugs, bats and bugs. Two wild ones of rival families who meet squabble: the hunter glowers and huffs, the
 * hunted one bolts. Nobody is hurt, and a girl who is anyone's friend has better things to do: friends never squabble.
 */
public final class Rivalries {
    public static final double RANGE = 8.0;
    public static final long COOLDOWN = 1200;
    public static final String NEXT = "SquabbleAt", FLED = "FledAt";
    /** Each hunter's family, and the families she can't stand. */
    public static final Map<GirlSpecies.Family, Set<GirlSpecies.Family>> HUNTS = Map.of(
            NEKOMATA, Set.of(RODENT),
            LAMIA, Set.of(RODENT),
            CANID, Set.of(JACKALOPE),
            KITSUNE, Set.of(JACKALOPE),
            ARACHNE, Set.of(BUG),
            BAT, Set.of(BUG));

    private Rivalries() {
    }

    public static boolean hunts(GirlSpecies.Family hunter, GirlSpecies.Family other) {
        return HUNTS.getOrDefault(hunter, Set.of()).contains(other);
    }

    private static boolean free(AbstractMonsterGirl g, long now) {
        return g.isAlive() && !g.isBaby() && !g.hasAnyAffection() && g.getTarget() == null && !g.isPassenger() && !g.isSleeping()
                && now >= g.getPersistentData().getLong(NEXT);
    }

    /** Every five seconds or so for a wild hunter: a squabble with a rival in reach. The one who bolted, or null. */
    @Nullable
    public static AbstractMonsterGirl squabble(ServerLevel level, AbstractMonsterGirl hunter) {
        long now = level.getGameTime();
        if (!MonsterGirlsConfig.rivalries() || !HUNTS.containsKey(hunter.getSpecies().family()) || !free(hunter, now)) return null;
        AbstractMonsterGirl prey = null;
        double best = RANGE * RANGE;
        for (AbstractMonsterGirl g : level.getEntitiesOfClass(AbstractMonsterGirl.class, hunter.getBoundingBox().inflate(RANGE))) {
            if (g == hunter || !hunts(hunter.getSpecies().family(), g.getSpecies().family()) || !free(g, now)) continue;
            double d = g.distanceToSqr(hunter);
            if (d < best) {
                best = d;
                prey = g;
            }
        }
        if (prey == null) return null;
        hunter.getPersistentData().putLong(NEXT, now + COOLDOWN);
        prey.getPersistentData().putLong(NEXT, now + COOLDOWN);
        hunter.getLookControl().setLookAt(prey, 30F, 30F);
        hunter.getNavigation().moveTo(prey, 1.1);
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, hunter.getX(), hunter.getEyeY() + 0.4, hunter.getZ(), 3, 0.3, 0.2, 0.3, 0);
        level.sendParticles(ParticleTypes.SPLASH, prey.getX(), prey.getEyeY() + 0.3, prey.getZ(), 6, 0.2, 0.1, 0.2, 0);   // a startled sweat drop
        level.playSound(null, hunter.blockPosition(), SoundEvents.CAT_HISS, SoundSource.NEUTRAL, 0.7F, 1.2F);
        Vec3 away = DefaultRandomPos.getPosAway(prey, 14, 6, hunter.position());
        if (away == null) away = prey.position().add(prey.position().subtract(hunter.position()).multiply(1, 0, 1).normalize().scale(6));   // straight off
        prey.getNavigation().moveTo(away.x, away.y, away.z, 1.4);
        prey.getPersistentData().putLong(FLED, now);
        return prey;
    }
}
