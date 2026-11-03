package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;

/**
 * Meetings by chance: the more species a biome holds, the more of its spawns would be girls, so natural spawns are
 * thinned here for every family at once, however many there are.
 */
public final class SpawnRarity {
    /** The rest of her group: same species, this close. */
    private static final double GROUP = 16.0;

    private SpawnRarity() {
    }

    public static boolean allows(LevelAccessor level, Mob mob, RandomSource random) {
        return random.nextDouble() < MonsterGirlsConfig.wildSpawnChance() * MonsterGirlsConfig.spawnRate(mob) * Rarity.spawnFactor(mob) && spacedOut(level, mob);
    }

    /** No other wild one near (friends stay out of it: they are yours, not the world's). */
    public static boolean spacedOut(LevelAccessor level, Mob mob) {
        int spacing = MonsterGirlsConfig.wildSpacing();
        if (spacing <= 0) return true;
        return level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(spacing), m -> m != mob && m instanceof Bonded && m.isAlive()
                && m.removeWhenFarAway(spacing) && !(m.getType() == mob.getType() && m.distanceToSqr(mob) < GROUP * GROUP)).isEmpty();
    }
}
