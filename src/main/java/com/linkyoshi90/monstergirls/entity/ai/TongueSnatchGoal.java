package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.Bullywug;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Snatches things off the ground with her tongue to show off: food is gulped down, shiny things are flicked up
 * into the air and dropped again (with a short pickup delay) for everyone to admire.
 */
public class TongueSnatchGoal extends Goal {
    private static final double SEARCH_RANGE = 8.0;
    private static final double REACH_SQR = 6.25;
    private static final int TONGUE_TICKS = 12;
    private static final int GIVE_UP_TICKS = 200;

    private final Bullywug mob;
    private ItemEntity target;
    private int cooldown;
    private int tongueTicks;
    private int giveUpTicks;

    public TongueSnatchGoal(Bullywug mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isBaby()) return false;
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if ((this.mob.getAction() != AbstractAnuran.Action.NONE && this.mob.getAction() != AbstractAnuran.Action.SITTING) || this.mob.isInWater() || this.mob.getTarget() != null) return false;
        if (this.mob.getFleeTicks() > 0 || this.mob.getRandom().nextInt(10) != 0) return false;
        this.target = this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(SEARCH_RANGE),
                        item -> item.isAlive() && !item.hasPickUpDelay() && this.mob.wantsToSnatch(item.getItem())).stream()
                .min(Comparator.comparingDouble(this.mob::distanceToSqr)).orElse(null);
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && this.target.isAlive() && this.giveUpTicks > 0 && this.mob.getTarget() == null
                && this.mob.getFleeTicks() <= 0;
    }

    @Override
    public void start() {
        this.tongueTicks = 0;
        this.giveUpTicks = GIVE_UP_TICKS;
    }

    @Override
    public void tick() {
        this.giveUpTicks--;
        this.mob.getLookControl().setLookAt(this.target);
        if (this.mob.distanceToSqr(this.target) > REACH_SQR) {
            this.mob.getNavigation().moveTo(this.target, 1.2);
            return;
        }
        this.mob.getNavigation().stop();
        if (this.tongueTicks == 0) {
            this.mob.setAction(AbstractAnuran.Action.TONGUE);
            this.mob.playSound(this.mob.getTongueSound(), 1.0F, this.mob.getVoicePitch());
        }
        if (++this.tongueTicks >= TONGUE_TICKS) {
            this.snatch(this.target);
            this.target = null;
        }
    }

    private void snatch(ItemEntity item) {
        ItemStack stack = item.getItem();
        if (this.mob.isFood(stack)) {
            stack.shrink(1);
            this.mob.heal(2.0F);
            this.mob.playSound(this.mob.getEatSound(), 1.0F, this.mob.getVoicePitch());
            if (stack.isEmpty()) item.discard();
            else item.setItem(stack);
        } else {
            // Show-off: flick it up in front of her and let it fall again.
            Vec3 look = this.mob.getLookAngle();
            item.setPos(this.mob.getX() + look.x * 0.8, this.mob.getY() + 1.0, this.mob.getZ() + look.z * 0.8);
            item.setDeltaMovement(look.x * 0.1, 0.3, look.z * 0.1);
            item.setPickUpDelay(40);
        }
    }

    @Override
    public void stop() {
        this.cooldown = 100 + this.mob.getRandom().nextInt(100);
        if (this.mob.getAction() == AbstractAnuran.Action.TONGUE) this.mob.setAction(AbstractAnuran.Action.NONE);
        this.target = null;
    }
}
