package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.entity.Orders;
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
        if (s == GirlSpecies.FROND_BAT) return girlAttributes(14, 0.3, 2);
        return girlAttributes(s == GirlSpecies.DATE_BAT ? 12 : 14, 0.3, 2);
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
        if (this.species == GirlSpecies.VETALA_BAT && this.level() instanceof net.minecraft.server.level.ServerLevel ground && !this.isBaby() && this.tickCount % 10 == 0) this.possess(ground);
        if (this.species == GirlSpecies.CAMAZOTZ_BAT && this.level() instanceof ServerLevel dark && !this.isBaby() && this.tickCount % 40 == 0) this.nightBite(dark, !dark.isDay());
        if (this.species == GirlSpecies.FROND_BAT && this.level() instanceof ServerLevel canopy && !this.isBaby() && this.tickCount % 600 == 0) this.shakeLeaves(canopy);
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.isRoosting()) {
            if (this.roost == null) this.setAction(Action.NONE);
            else this.setPos(this.roost.getX() + 0.5, this.roost.getY() - this.getBbHeight(), this.roost.getZ() + 0.5);
        }
        if (this.species == GirlSpecies.DATE_BAT && !this.isBaby() && !this.isRoosting() && !server.isDay() && this.tickCount % 100 == 0
                && this.bondedPlayer(Orders.AFFECTION, 32) != null) this.pollinate(server);
        if (this.tickCount % PING == 0 && !this.isRoosting() && !this.isBaby()) {
            Player friend = this.bondedPlayer(Orders.AFFECTION, 32);
            if (friend != null) this.ping(server, friend);
        }
    }

    /** The datebat at night: the crops within 6 blocks of her grow a stage. Returns how many grew. */
    public int pollinate(ServerLevel level) {
        int n = 0;
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(this.blockPosition().offset(-6, -5, -6), this.blockPosition().offset(6, 2, 6))) {
            var s = level.getBlockState(p);
            if (s.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop && !crop.isMaxAge(s)) {
                level.setBlock(p, crop.getStateForAge(crop.getAge(s) + 1), 2);
                level.levelEvent(2005, p, 0);
                n++;
            }
        }
        return n;
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
        if (this.species == GirlSpecies.DATE_BAT) return new ItemStack(r < 40 ? Items.SWEET_BERRIES : r < 75 ? Items.APPLE : r < 95 ? Items.HONEYCOMB : Items.GOLDEN_APPLE, r < 95 ? 3 : 1);
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

    // --- the frondbat --------------------------------------------------------------------------------------------

    /** She shakes a leaf block within 6 blocks of her friend: what falls is what the leaves drop when broken (a sapling,
     *  sticks, now and then an apple), or a stick at least. What fell, or null without a friend or leaves. */
    @javax.annotation.Nullable
    public java.util.List<ItemStack> shakeLeaves(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 16);
        if (friend == null) return null;
        java.util.List<net.minecraft.core.BlockPos> leaves = new java.util.ArrayList<>();
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(friend.blockPosition().offset(-6, -2, -6), friend.blockPosition().offset(6, 6, 6))) {
            if (level.getBlockState(p).is(net.minecraft.tags.BlockTags.LEAVES)) leaves.add(p.immutable());
        }
        if (leaves.isEmpty()) return null;
        net.minecraft.core.BlockPos at = leaves.get(this.random.nextInt(leaves.size()));
        var state = level.getBlockState(at);
        java.util.List<ItemStack> drops = new java.util.ArrayList<>();
        for (int i = 0; i < 8 && drops.isEmpty(); i++) drops.addAll(net.minecraft.world.level.block.Block.getDrops(state, level, at, null));
        if (drops.isEmpty()) drops.add(new ItemStack(Items.STICK));
        if (this.random.nextInt(4) == 0) drops.add(new ItemStack(Items.APPLE));
        for (ItemStack st : drops) net.minecraft.world.level.block.Block.popResource(level, at.below(), st);
        level.levelEvent(2001, at, net.minecraft.world.level.block.Block.getId(state));
        return drops;
    }

    // --- Camazotz (nineteenth wave) ---------------------------------------------------------------------------------

    /** At night she bites the nearest monster within 8 blocks of her friend for two points, and the stolen strength heals her
     *  friend by as much. 1 if she bit, 0 if not (day, or none near), -1 without a friend. */
    public int nightBite(ServerLevel level, boolean night) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        if (!night) return 0;
        net.minecraft.world.entity.monster.Monster m = level.getNearestEntity(net.minecraft.world.entity.monster.Monster.class, net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat(), friend,
                friend.getX(), friend.getY(), friend.getZ(), friend.getBoundingBox().inflate(8));
        if (m == null) return 0;
        m.hurt(level.damageSources().mobAttack(this), 2.0F);
        friend.heal(2.0F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR, m.getX(), m.getY(0.7), m.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
        return 1;
    }

    /** The zombie the vetala rides, and until when. */
    @javax.annotation.Nullable
    private java.util.UUID ridden;
    private long riddenUntil;

    /**
     * The vetala's possession (twenty-third wave): she rides the nearest zombie within 8 of her friend for 30 seconds; it turns on
     * the other monsters near her friend (and never on her friend). 1 while she rides one, 0 with no zombie near, -1 without a friend.
     */
    public int possess(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        net.minecraft.world.entity.monster.Zombie z = this.ridden == null ? null
                : level.getEntity(this.ridden) instanceof net.minecraft.world.entity.monster.Zombie r && r.isAlive() && level.getGameTime() < this.riddenUntil ? r : null;
        if (z == null) {
            this.ridden = null;
            z = level.getNearestEntity(net.minecraft.world.entity.monster.Zombie.class, net.minecraft.world.entity.ai.targeting.TargetingConditions.forNonCombat(),
                    friend, friend.getX(), friend.getY(), friend.getZ(), friend.getBoundingBox().inflate(8));
            if (z == null) return 0;
            this.ridden = z.getUUID();
            this.riddenUntil = level.getGameTime() + 600;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL, z.getX(), z.getY() + 1.5, z.getZ(), 16, 0.3, 0.5, 0.3, 0.02);
            this.playSound(net.minecraft.sounds.SoundEvents.SOUL_ESCAPE.value(), 1.0F, 0.8F);
        }
        final net.minecraft.world.entity.monster.Zombie host = z;
        net.minecraft.world.entity.monster.Monster prey = level.getNearestEntity(net.minecraft.world.entity.monster.Monster.class, net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat(), host, host.getX(), host.getY(), host.getZ(),
                friend.getBoundingBox().inflate(12));
        host.setTarget(prey != null && prey != host ? prey : null);
        return 1;
    }
}
