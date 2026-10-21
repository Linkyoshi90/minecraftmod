package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Slimes love anything bouncy (block tag monstergirls:bouncy: slime blocks and beds): she hops over to one nearby and
 * bounces on it for a while, squealing happily.
 */
public class BounceGoal extends Goal {
    public static final int RADIUS = 10;

    private final AbstractMonsterGirl girl;
    @Nullable
    private BlockPos spot;
    private int ticks;
    private int length;

    public BounceGoal(AbstractMonsterGirl girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    public static boolean isBouncy(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModTags.BOUNCY) && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && level.getBlockState(pos.above(2)).getCollisionShape(level, pos.above(2)).isEmpty();
    }

    @Nullable
    public static BlockPos findBouncy(AbstractMonsterGirl girl) {
        BlockPos o = girl.blockPosition();
        BlockPos best = null;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-RADIUS, -3, -RADIUS), o.offset(RADIUS, 3, RADIUS))) {
            if (isBouncy(girl.level(), p) && (best == null || p.distSqr(o) < best.distSqr(o))) best = p.immutable();
        }
        return best;
    }

    @Override
    public boolean canUse() {
        if (this.girl.isOrderedToSit() || this.girl.getTarget() != null || this.girl.tickCount % 31 != 0 || this.girl.getRandom().nextInt(3) != 0) return false;
        this.spot = findBouncy(this.girl);
        return this.spot != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.spot != null && this.ticks < this.length && this.girl.getTarget() == null && !this.girl.isOrderedToSit()
                && this.girl.level().getBlockState(this.spot).is(ModTags.BOUNCY);
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.length = 200 + this.girl.getRandom().nextInt(300);
    }

    @Override
    public void stop() {
        if (this.girl.getAction() == AbstractMonsterGirl.Action.BOUNCE) this.girl.setAction(AbstractMonsterGirl.Action.NONE);
        this.spot = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        Vec3 top = Vec3.atBottomCenterOf(this.spot.above());
        double dx = this.girl.getX() - top.x, dz = this.girl.getZ() - top.z;
        double d2 = dx * dx + dz * dz;
        boolean over = d2 < 0.6 * 0.6 || this.girl.blockPosition().below().equals(this.spot);
        if (!over) {
            if (d2 < 2.5 * 2.5) {                                     // the path stops short of a slime block: hop the last bit
                this.girl.getNavigation().stop();
                this.girl.getMoveControl().setWantedPosition(top.x, top.y, top.z, 1.0);
                if (this.girl.onGround() && this.ticks % 10 == 0) this.girl.getJumpControl().jump();
            } else if (this.ticks % 10 == 1) {
                this.girl.getNavigation().moveTo(top.x, top.y, top.z, 1.1);
            }
            if (this.ticks > 200) this.ticks = this.length;          // she cannot get there: give up
            return;
        }
        this.girl.getNavigation().stop();
        if (this.girl.getAction() == AbstractMonsterGirl.Action.NONE) this.girl.setAction(AbstractMonsterGirl.Action.BOUNCE);
        if (this.girl.onGround()) {
            this.girl.setDeltaMovement(-dx * 0.2, 0.62 + this.girl.getRandom().nextDouble() * 0.15, -dz * 0.2);
            this.girl.hasImpulse = true;
            this.girl.playSound(SoundEvents.SLIME_JUMP_SMALL, 0.8F, 1.2F + this.girl.getRandom().nextFloat() * 0.3F);
            if (this.girl.level() instanceof ServerLevel server && this.girl.getRandom().nextInt(4) == 0) this.girl.hearts(server, 1);
        }
    }
}
