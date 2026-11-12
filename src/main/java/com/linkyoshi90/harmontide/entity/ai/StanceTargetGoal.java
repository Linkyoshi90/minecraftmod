package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.Bonded;
import com.linkyoshi90.harmontide.entity.Orders;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

/** Picks fights by her stance (hostile mobs, whoever hurts her or her player); idle while she follows her instincts. */
public class StanceTargetGoal<T extends PathfinderMob & Bonded> extends NearestAttackableTargetGoal<LivingEntity> {
    private final T owner;

    public StanceTargetGoal(T mob) {
        super(mob, LivingEntity.class, 10, true, false, e -> mob.getOrders().wants(mob, e));
        this.owner = mob;
    }

    @Override
    public boolean canUse() {
        Orders.Stance s = this.owner.getOrders().stance();
        return s != Orders.Stance.NATURAL && s != Orders.Stance.PEACEFUL && super.canUse();
    }
}
