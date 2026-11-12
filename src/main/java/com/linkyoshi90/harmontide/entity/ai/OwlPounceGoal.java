package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.OwlHarpy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/** The hunt: a silent glide onto her prey and a talons-first strike. */
public class OwlPounceGoal extends MeleeAttackGoal {
    private final OwlHarpy owl;

    public OwlPounceGoal(OwlHarpy owl, double speed) {
        super(owl, speed, true);
        this.owl = owl;
    }

    @Override
    public void start() {
        super.start();
        if (this.owl.isSleeping()) this.owl.setAction(AbstractHarpy.Action.NONE);
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (!this.canPerformAttack(target)) return;
        this.resetAttackCooldown();
        this.owl.startTimedAction(AbstractHarpy.Action.POUNCING, 16);
        this.owl.doHurtTarget(target);
    }
}
