package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * The bat family (bat_field_guide.html): the bat. She flies, sleeps by day hanging upside down from a ceiling (a cave
 * roof, an overhang: "roost"), and, once befriended, pings every few seconds: the monsters near her friend glow for a
 * moment, through walls. Vanilla bats are her kin; the fruit bat variant has a fox's face.
 */
public class BatGirl extends AbstractMonsterGirl {
    /** How often she pings (ticks). */
    public static final int PING = 60;
    @Nullable
    private BlockPos roost;

    public BatGirl(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(Items.APPLE, Items.MELON_SLICE, Items.SWEET_BERRIES, Items.GLOW_BERRIES), false));
        this.goalSelector.addGoal(6, new RoostGoal());
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return girlAttributes(14, 0.3, 2);
    }

    @Override
    public boolean canFly() {
        return true;
    }

    // --- roosting ----------------------------------------------------------------------------------------------------

    public boolean isRoosting() {
        return this.getAction() == Action.ROOST;
    }

    /** A ceiling within 8 blocks she can hang under: a sturdy underside with room below it for her. */
    @Nullable
    public BlockPos findRoost() {
        BlockPos o = this.blockPosition();
        int room = (int) Math.ceil(this.getBbHeight());
        BlockPos best = null;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-8, 1, -8), o.offset(8, 10, 8))) {
            if (!this.level().getBlockState(p).isFaceSturdy(this.level(), p, Direction.DOWN)) continue;
            boolean clear = true;
            for (int i = 1; i <= room && clear; i++) clear = this.level().getBlockState(p.below(i)).isAir();
            if (clear && (best == null || p.distSqr(o) < best.distSqr(o))) best = p.immutable();
        }
        return best;
    }

    /** Hang from the ceiling block above this spot: her feet against it, head down (the "roost" animation flips her). */
    public void hangFrom(BlockPos ceiling) {
        this.roost = ceiling;
        this.getNavigation().stop();
        this.setPos(ceiling.getX() + 0.5, ceiling.getY() - this.getBbHeight(), ceiling.getZ() + 0.5);
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.setAction(Action.ROOST);
    }

    public void stopRoosting() {
        this.roost = null;
        if (this.isRoosting()) this.setAction(Action.NONE);
    }

    /** By day (her bedtime) she flies to the nearest ceiling and hangs from it until night, a hit or a hand wakes her. */
    class RoostGoal extends Goal {
        private final SleepGoal sleep = new SleepGoal(BatGirl.this);
        @Nullable
        private BlockPos ceiling;

        RoostGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        private boolean sleepy() {
            BatGirl b = BatGirl.this;
            return this.sleep.bedtime() && b.getTarget() == null && !b.isPassenger() && b.level().getGameTime() >= b.getWokenUntil()
                    && b.getOrders().command(b) != Orders.Command.FOLLOW && !b.isOrderedToSit();
        }

        @Override
        public boolean canUse() {
            if (BatGirl.this.tickCount % 20 != 0 || !this.sleepy()) return false;
            this.ceiling = BatGirl.this.findRoost();
            return this.ceiling != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.ceiling != null && this.sleepy() && BatGirl.this.level().getBlockState(this.ceiling).isFaceSturdy(BatGirl.this.level(), this.ceiling, Direction.DOWN);
        }

        @Override
        public void start() {
            BatGirl.this.getNavigation().moveTo(this.ceiling.getX() + 0.5, this.ceiling.getY() - BatGirl.this.getBbHeight(), this.ceiling.getZ() + 0.5, 1.0);
        }

        @Override
        public void tick() {
            BatGirl b = BatGirl.this;
            if (b.isRoosting()) {
                b.setDeltaMovement(Vec3.ZERO);
                b.getNavigation().stop();
                return;
            }
            Vec3 spot = new Vec3(this.ceiling.getX() + 0.5, this.ceiling.getY() - b.getBbHeight(), this.ceiling.getZ() + 0.5);
            if (b.position().distanceToSqr(spot) < 1.5 * 1.5) b.hangFrom(this.ceiling);
            else if (b.getNavigation().isDone()) b.getNavigation().moveTo(spot.x, spot.y, spot.z, 1.0);
        }

        @Override
        public void stop() {
            BatGirl.this.stopRoosting();
        }
    }

    // --- echolocation --------------------------------------------------------------------------------------------------

    /** A ping: every monster within 16 blocks of her friend glows for a moment. */
    public int ping(ServerLevel level, Player friend) {
        int found = 0;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, friend.getBoundingBox().inflate(16), e -> e instanceof Enemy && e.isAlive())) {
            e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, true, false));
            found++;
        }
        this.playSound(SoundEvents.BAT_AMBIENT, 0.3F, 1.8F);
        return found;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.isRoosting()) {
            if (this.roost == null) this.setAction(Action.NONE);
            else this.setPos(this.roost.getX() + 0.5, this.roost.getY() - this.getBbHeight(), this.roost.getZ() + 0.5);
        }
        if (this.tickCount % PING == 0 && !this.isRoosting() && !this.isBaby()) {
            Player friend = this.bondedPlayer(Orders.AFFECTION, 32);
            if (friend != null) this.ping(server, friend);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt) this.stopRoosting();
        return hurt;
    }

    // --- food, presents, sounds ------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.APPLE) || stack.is(Items.MELON_SLICE) || stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.GLOW_BERRIES) ? 2 : 1;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return new ItemStack(r < 45 ? Items.BONE_MEAL : r < 80 ? Items.GLOW_BERRIES : r < 95 ? Items.APPLE : Items.PHANTOM_MEMBRANE, r < 80 ? 3 : 1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isRoosting() ? null : SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.GENERIC_EAT;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 1.3F;
    }
}
