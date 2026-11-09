package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

/**
 * The cradle (2026-11-09, block/CradleBlock). At night a little one within {@link #FIND} of a cradle toddles over and naps
 * beside it, and every check she spends there ({@link #EVERY} ticks) counts double towards growing up. Rocking it with an empty
 * hand hums a lullaby to the little ones within {@link #ROCK}: hearts and {@link #ROCKED} affection each, once a minute apiece.
 */
public final class Cradle {
    public static final int FIND = 6, ROCK = 3, EVERY = 100, ROCKED = 2, ROCK_COOLDOWN = 1200;
    public static final double BESIDE = 2.0;
    private static final String ROCKED_AT = "CradleRockedAt";

    private Cradle() {
    }

    /** The nearest cradle within FIND, or null. */
    @Nullable
    public static BlockPos near(ServerLevel level, BlockPos from) {
        BlockPos best = null;
        for (BlockPos p : BlockPos.betweenClosed(from.offset(-FIND, -2, -FIND), from.offset(FIND, 2, FIND))) {
            if (level.getBlockState(p).is(ModBlocks.CRADLE.get()) && (best == null || p.distSqr(from) < best.distSqr(from))) best = p.immutable();
        }
        return best;
    }

    /** Every EVERY ticks for a little one: at night she goes to the cradle and naps, growing twice as fast. True while she naps. */
    public static boolean tick(ServerLevel level, AbstractMonsterGirl young, boolean night) {
        if (!young.isBaby() || !night || young.isPassenger()) return false;
        BlockPos cradle = near(level, young.blockPosition());
        if (cradle == null) return false;
        if (young.distanceToSqr(cradle.getCenter()) > BESIDE * BESIDE) {
            young.getNavigation().moveTo(cradle.getX() + 0.5, cradle.getY(), cradle.getZ() + 0.5, 0.8);
            return false;
        }
        young.getNavigation().stop();
        young.startTimedAction(AbstractMonsterGirl.Action.LYING, EVERY + 20);
        young.setGrowTicks(young.getGrowTicks() + EVERY);
        if (young.getRandom().nextInt(4) == 0) level.sendParticles(ParticleTypes.NOTE, young.getX(), young.getEyeY() + 0.4, young.getZ(), 1, 0.2, 0.1, 0.2, 0.5);
        return true;
    }

    /** Rocking the cradle: a lullaby for the little ones near. How many heard it. */
    public static int rock(ServerLevel level, BlockPos cradle, Player player) {
        level.playSound(null, cradle, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.6F, 0.7F);
        int n = 0;
        for (AbstractMonsterGirl young : level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(cradle).inflate(ROCK), AbstractMonsterGirl::isBaby)) {
            n++;
            level.sendParticles(ParticleTypes.HEART, young.getX(), young.getEyeY() + 0.3, young.getZ(), 2, 0.2, 0.1, 0.2, 0.0);
            if (level.getGameTime() < young.getPersistentData().getLong(ROCKED_AT)) continue;
            young.getPersistentData().putLong(ROCKED_AT, level.getGameTime() + ROCK_COOLDOWN);
            young.addAffection(player, ROCKED);
        }
        return n;
    }
}
