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

import com.linkyoshi90.monstergirls.entity.Clade;

/**
 * Rivalries: some wild kinds can't stand each other, by their branch of the tree of life (entity.Clade), not their body: cats
 * (and hyenas, and mongooses) and snakes with rodents, wolves and foxes with hares, spiders, bats and frogs with insects, seals
 * and bears with fish. The mongoose and the meerkat take on snakes as well (the meerkat scorpions too). Two wild ones of rival clades who meet squabble: the hunter glowers and huffs, the hunted one bolts.
 * Nobody is hurt, and a girl who is anyone's friend has better things to do: friends never squabble.
 */
public final class Rivalries {
    public static final double RANGE = 8.0;
    public static final long COOLDOWN = 1200;
    public static final String NEXT = "SquabbleAt", FLED = "FledAt";
    /** Each hunter's clade, and the clades she can't stand. */
    public static final Map<Clade, Set<Clade>> HUNTS = Map.of(
            Clade.FELIFORMIA, Set.of(Clade.RODENTIA),
            Clade.SQUAMATA, Set.of(Clade.RODENTIA, Clade.AMPHIBIA),
            Clade.CANIDAE, Set.of(Clade.LAGOMORPHA, Clade.RODENTIA),
            Clade.CHELICERATA, Set.of(Clade.HEXAPODA),
            Clade.CHIROPTERA, Set.of(Clade.HEXAPODA),
            Clade.AMPHIBIA, Set.of(Clade.HEXAPODA),
            Clade.ARCTOIDEA, Set.of(Clade.ACTINOPTERYGII));

    /** Hunters of their own on top of their clade's: the mongoose and the meerkat take on snakes (the meerkat scorpions too). */
    public static final Map<GirlSpecies, Set<Clade>> SPECIES_HUNTS = Map.of(
            GirlSpecies.MONGOOSE_RODENT, Set.of(Clade.SQUAMATA),
            GirlSpecies.MEERKAT_RODENT, Set.of(Clade.SQUAMATA, Clade.CHELICERATA));

    private Rivalries() {
    }

    /** Whether this kind can't stand that one: by clade, or by her own species' grudge. */
    public static boolean hunts(GirlSpecies hunter, GirlSpecies other) {
        return hunts(hunter.clade(), other.clade()) || other.clade() != null && SPECIES_HUNTS.getOrDefault(hunter, Set.of()).contains(other.clade());
    }

    private static boolean hunter(GirlSpecies s) {
        return s.clade() != null && HUNTS.containsKey(s.clade()) || SPECIES_HUNTS.containsKey(s);
    }

    public static boolean hunts(@Nullable Clade hunter, @Nullable Clade other) {
        return hunter != null && other != null && HUNTS.getOrDefault(hunter, Set.of()).contains(other);
    }

    private static boolean free(AbstractMonsterGirl g, long now) {
        return g.isAlive() && !g.isBaby() && !g.hasAnyAffection() && g.getTarget() == null && !g.isPassenger() && !g.isSleeping()
                && now >= g.getPersistentData().getLong(NEXT);
    }

    /** Every five seconds or so for a wild hunter: a squabble with a rival in reach. The one who bolted, or null. */
    @Nullable
    public static AbstractMonsterGirl squabble(ServerLevel level, AbstractMonsterGirl hunter) {
        long now = level.getGameTime();
        if (!MonsterGirlsConfig.rivalries() || !hunter(hunter.getSpecies()) || !free(hunter, now)) return null;
        AbstractMonsterGirl prey = null;
        double best = RANGE * RANGE;
        for (AbstractMonsterGirl g : level.getEntitiesOfClass(AbstractMonsterGirl.class, hunter.getBoundingBox().inflate(RANGE))) {
            if (g == hunter || !hunts(hunter.getSpecies(), g.getSpecies()) || !free(g, now)) continue;
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
