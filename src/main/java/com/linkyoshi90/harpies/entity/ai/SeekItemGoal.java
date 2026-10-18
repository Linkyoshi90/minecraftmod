package com.linkyoshi90.harpies.entity.ai;

import com.linkyoshi90.harpies.entity.AbstractHarpy;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.event.EventHooks;

import java.util.Comparator;
import java.util.EnumSet;

/** Goes to a nearby dropped item she wants (food or a trinket); picking it up happens on contact. */
public class SeekItemGoal extends Goal {
    private final AbstractHarpy harpy;
    private final double speed;
    private final double radius;
    private ItemEntity target;
    private int ticks;

    public SeekItemGoal(AbstractHarpy harpy, double speed, double radius) {
        this.harpy = harpy;
        this.speed = speed;
        this.radius = radius;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean isWanted(ItemEntity item) {
        return item.isAlive() && !item.hasPickUpDelay() && this.harpy.wantsItem(item.getItem()) && this.harpy.isSafeSpot(item.blockPosition());
    }

    @Override
    public boolean canUse() {
        if (this.harpy.isHoldingSomething() || this.harpy.getRandom().nextInt(reducedTickDelay(10)) != 0) return false;
        if (!EventHooks.canEntityGrief(this.harpy.level(), this.harpy)) return false; // vanilla item pickup obeys mobGriefing too
        this.target = this.harpy.level().getEntitiesOfClass(ItemEntity.class, this.harpy.getBoundingBox().inflate(this.radius, 4, this.radius), this::isWanted)
                .stream().min(Comparator.comparingDouble(this.harpy::distanceToSqr)).orElse(null);
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < 300 && this.isWanted(this.target);
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.harpy.getNavigation().moveTo(this.target, this.speed);
    }

    @Override
    public void stop() {
        this.target = null;
        this.harpy.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.ticks++;
        this.harpy.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (this.ticks % 10 == 0) this.harpy.getNavigation().moveTo(this.target, this.speed);
    }
}
