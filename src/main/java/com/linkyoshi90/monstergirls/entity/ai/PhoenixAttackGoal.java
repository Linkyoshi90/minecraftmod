package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.PhoenixHarpy;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Her fighting style: keep circling at a few blocks' distance throwing fire bolts (like a blaze's, three in a
 * burst), and dive in for a raking talon strike whenever the target gets close.
 */
public class PhoenixAttackGoal extends Goal {
    private final PhoenixHarpy phoenix;
    private int cooldown;
    private int burst;
    private double orbitAngle;

    public PhoenixAttackGoal(PhoenixHarpy phoenix) {
        this.phoenix = phoenix;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.phoenix.getTarget();
        return target != null && target.isAlive() && !this.phoenix.isHatchling();
    }

    @Override
    public void start() {
        this.cooldown = 20;
        this.burst = 0;
        this.orbitAngle = this.phoenix.getRandom().nextDouble() * Math.PI * 2;
        this.phoenix.setAggressive(true);
    }

    @Override
    public void stop() {
        this.phoenix.setAggressive(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.phoenix.getTarget();
        if (target == null) return;
        this.phoenix.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double dist = Math.sqrt(this.phoenix.distanceToSqr(target));
        this.cooldown--;

        if (dist < 2.5) {
            // Talons.
            this.phoenix.getMoveControl().setWantedPosition(target.getX(), target.getY() + 0.5, target.getZ(), 1.2);
            if (this.cooldown <= 0) {
                this.cooldown = 20;
                this.phoenix.startTimedAction(AbstractHarpy.Action.STRIKING, 16);
                this.phoenix.doHurtTarget(target);
                target.igniteForSeconds(3.0F);
            }
            return;
        }
        // Circle at 6-8 blocks, a little above the target.
        this.orbitAngle += 0.04;
        Vec3 spot = target.position().add(Math.cos(this.orbitAngle) * 7, 3, Math.sin(this.orbitAngle) * 7);
        this.phoenix.getMoveControl().setWantedPosition(spot.x, spot.y, spot.z, 1.0);
        if (dist < 16 && this.cooldown <= 0 && this.phoenix.hasLineOfSight(target)) {
            Vec3 dir = target.getEyePosition().subtract(this.phoenix.getEyePosition());
            SmallFireball bolt = new SmallFireball(this.phoenix.level(), this.phoenix, dir.normalize());
            bolt.setPos(this.phoenix.getX(), this.phoenix.getEyeY() - 0.2, this.phoenix.getZ());
            this.phoenix.level().addFreshEntity(bolt);
            this.phoenix.playSound(ModSounds.PHOENIX_SHOOT.get(), 1.0F, 1.0F);
            // Three bolts a burst, then a breather.
            this.cooldown = ++this.burst % 3 == 0 ? 60 : 8;
        }
    }
}
