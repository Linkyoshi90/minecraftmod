package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Whoever stands in the filled tub heals, and their friends nearby come and join in. */
public class SoakingTubBlockEntity extends BlockEntity {
    public SoakingTubBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOAKING_TUB.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SoakingTubBlockEntity tub) {
        if (!(level instanceof ServerLevel server) || server.getGameTime() % 40 != 0 || !state.getValue(SoakingTubBlock.FILLED)) return;
        tick(server, pos);
    }

    /** One round: the bathers healed, the friends called over. How many friends shared it. */
    public static int tick(ServerLevel level, BlockPos pos) {
        int shared = 0;
        AABB tub = new AABB(pos).inflate(0.2, 1.0, 0.2);
        for (Player p : level.getEntitiesOfClass(Player.class, tub, Player::isAlive)) {
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, true));
            level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 1, 0.3, 0.1, 0.3, 0.0);
            for (Mob m : HomeLife.friendsOf(level, p, new AABB(pos).inflate(12))) {
                if (m.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) > 9) m.getNavigation().moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 1.0);
                else if (HomeLife.soak(level, m, p)) shared++;
            }
        }
        return shared;
    }
}
