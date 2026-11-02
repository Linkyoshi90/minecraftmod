package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.EnumSet;

/** A friend runs after a toy ball on the ground and brings it back to the nearest player, if that's a friend of hers: 1 friendship, a minute apart (the thirty-fourth wave). */
public class FetchBallGoal<T extends PathfinderMob & Bonded> extends Goal {
    private final T mob;
    @Nullable
    private ItemEntity ball;
    @Nullable
    private Player thrower;
    private ItemStack carried = ItemStack.EMPTY;
    private int patience;

    public FetchBallGoal(T mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isBaby() || this.mob.tickCount % 7 != 0) return false;
        for (ItemEntity item : this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(16), i -> i.isAlive() && i.getItem().is(ModItems.TOY_BALL.get()))) {
            Player p = this.mob.level().getNearestPlayer(this.mob, 24);
            if (p == null || this.mob.getAffection(p) < HomeLife.FRIEND) continue;
            this.ball = item;
            this.thrower = p;
            return true;
        }
        return false;
    }

    /** Otherwise tick() only runs on every other tick, and on some mobs the walking orders below would never fire. */
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.patience > 0 && this.thrower != null && this.thrower.isAlive() && (!this.carried.isEmpty() || (this.ball != null && this.ball.isAlive()));
    }

    @Override
    public void start() {
        this.patience = 400;
    }

    @Override
    public void stop() {
        if (!this.carried.isEmpty()) this.mob.spawnAtLocation(this.carried);
        this.carried = ItemStack.EMPTY;
        this.ball = null;
        this.thrower = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.patience--;
        if (this.carried.isEmpty()) {
            this.mob.getLookControl().setLookAt(this.ball);
            if (this.mob.tickCount % 10 == 0) this.mob.getNavigation().moveTo(this.ball, 1.3);
            if (this.mob.distanceToSqr(this.ball) < 4.0) {
                this.carried = this.ball.getItem().copy();
                this.ball.discard();
            }
            return;
        }
        this.mob.getLookControl().setLookAt(this.thrower);
        if (this.mob.tickCount % 10 == 0) this.mob.getNavigation().moveTo(this.thrower, 1.2);
        if (this.mob.distanceToSqr(this.thrower) >= 9.0) return;
        if (!this.thrower.getInventory().add(this.carried)) this.thrower.drop(this.carried, false);
        this.carried = ItemStack.EMPTY;
        long now = this.mob.level().getGameTime();
        if (now >= this.mob.getPersistentData().getLong("NextFetch")) {
            this.mob.getPersistentData().putLong("NextFetch", now + 1200);
            this.mob.addAffection(this.thrower, 1);
        }
        if (this.mob.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.HEART, this.mob.getX(), this.mob.getY() + this.mob.getBbHeight(), this.mob.getZ(), 2, 0.3, 0.2, 0.3, 0.0);
        this.patience = 0;
    }
}
