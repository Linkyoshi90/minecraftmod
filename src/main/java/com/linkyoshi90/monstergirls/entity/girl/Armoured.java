package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * The armoured (armoured_field_guide.html): armadillo. A hit or a sprinting stranger rolls her up into a ball for a few
 * seconds, and nothing gets through. Befriended, she rolls into monsters near her friend and bowls them over. She
 * sheds armadillo scutes as presents.
 */
public class Armoured extends AbstractMonsterGirl {
    public static final int CURL_TICKS = 80;
    private int curled;

    public Armoured(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.3) {
            @Override
            public boolean canUse() {
                return !Armoured.this.isBoar() && !Armoured.this.isSloth() && !Armoured.this.isGrib() && !Armoured.this.isCurled() && Armoured.this.bestAffection() < Orders.AFFECTION
                        && super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.6, true) {
            @Override
            public boolean canUse() {
                return !Armoured.this.isCurled() && super.canUse();
            }
        });
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override
            public boolean canUse() {
                return !Armoured.this.isCurled() && super.canUse();
            }
        });
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true, m -> {   // her friend's monsters get bowled over
            Player p = this.bondedPlayer(Orders.AFFECTION, 16);
            return p != null && m.distanceToSqr(p) < 8 * 8;
        }));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        if (s == GirlSpecies.RIVERDILLO_ARMOURED) return girlAttributes(18, 0.28, 2).add(Attributes.ARMOR, 4);
        if (s == GirlSpecies.GRIBBOAR_ARMOURED) return girlAttributes(26, 0.27, 4).add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
        if (s == GirlSpecies.SLOTHREN_ARMOURED) return girlAttributes(30, 0.12, 2).add(Attributes.ARMOR, 2);
        if (s == GirlSpecies.AEGIS_BOAR_ARMOURED) return girlAttributes(50, 0.27, 7).add(Attributes.ARMOR, 10).add(Attributes.ATTACK_KNOCKBACK, 1.5);
        return girlAttributes(22, 0.25, 4).add(Attributes.ARMOR, 8).add(Attributes.ATTACK_KNOCKBACK, 2.0);
    }

    // --- the gribboar ------------------------------------------------------------------------------------------------

    public boolean isGrib() {
        return this.species == GirlSpecies.GRIBBOAR_ARMOURED;
    }

    /** She roots in the earth she stands on and digs something up. The find, or empty if there is nothing to root in. */
    public ItemStack root(ServerLevel level) {
        var under = level.getBlockState(this.blockPosition().below());
        if (!under.is(net.minecraft.tags.BlockTags.DIRT) && !under.is(net.minecraft.world.level.block.Blocks.MYCELIUM)) return ItemStack.EMPTY;
        int r = this.random.nextInt(100);
        ItemStack find = new ItemStack(r < 20 ? Items.BROWN_MUSHROOM : r < 35 ? Items.RED_MUSHROOM : r < 60 ? Items.POTATO : r < 80 ? Items.CARROT
                : r < 92 ? Items.BEETROOT_SEEDS : r < 99 ? Items.GOLD_NUGGET : Items.EMERALD, r < 92 ? 1 + this.random.nextInt(2) : 1);
        this.spawnAtLocation(find);
        this.startTimedAction(Action.DIG, 30);
        level.levelEvent(2001, this.blockPosition().below(), net.minecraft.world.level.block.Block.getId(under));
        this.playSound(SoundEvents.HOGLIN_AMBIENT, 0.6F, 1.4F);
        return find;
    }

    // --- the riverdillo -----------------------------------------------------------------------------------------------

    /** The angler: in the water near her friend she catches a fish and drops it at their feet. The fish, or empty. */
    public ItemStack angle(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null || this.isBaby() || !this.isInWater()) return ItemStack.EMPTY;
        int r = this.random.nextInt(100);
        ItemStack fish = new ItemStack(r < 55 ? Items.COD : r < 90 ? Items.SALMON : Items.TROPICAL_FISH);
        level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level, friend.getX(), friend.getY() + 0.5, friend.getZ(), fish.copy()));
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH, this.getX(), this.getY() + 0.5, this.getZ(), 12, 0.3, 0.2, 0.3, 0.1);
        this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.8F, 1.2F);
        return fish;
    }

    // --- the slothren -------------------------------------------------------------------------------------------------

    @Nullable
    private BlockPos branch;

    public boolean isSloth() {
        return this.species == GirlSpecies.SLOTHREN_ARMOURED;
    }

    public boolean isHanging() {
        return this.getAction() == Action.HANG;
    }

    /** Leaves or a log within 8 blocks above with room under it for her: a branch to hang from. */
    @Nullable
    public BlockPos findBranch() {
        BlockPos o = this.blockPosition(), best = null;
        int room = (int) Math.ceil(this.getBbHeight());
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-6, 1, -6), o.offset(6, 8, 6))) {
            var s = this.level().getBlockState(p);
            if (!s.is(net.minecraft.tags.BlockTags.LEAVES) && !s.is(net.minecraft.tags.BlockTags.LOGS)) continue;
            boolean clear = true;
            for (int i = 1; i <= room && clear; i++) clear = this.level().getBlockState(p.below(i)).isAir();
            if (clear && (best == null || p.distSqr(o) < best.distSqr(o))) best = p.immutable();
        }
        return best;
    }

    /** Hands on the branch, the rest of her hanging below it. */
    public void hangFrom(BlockPos b) {
        this.branch = b;
        this.getNavigation().stop();
        this.setPos(b.getX() + 0.5, b.getY() - this.getBbHeight(), b.getZ() + 0.5);
        this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        this.setNoGravity(true);
        this.setAction(Action.HANG);
    }

    public void letGo() {
        this.branch = null;
        this.setNoGravity(false);
        if (this.isHanging()) this.setAction(Action.NONE);
    }

    /** Flowers on the grass and moss on the stone under where she rests; befriended, the crops and saplings there grow. */
    public int tend(ServerLevel level) {
        BlockPos ground = this.blockPosition();
        for (int i = 0; i < 10 && level.getBlockState(ground.below()).isAir(); i++) ground = ground.below();
        int n = 0;
        BlockPos p = ground.offset(this.random.nextInt(5) - 2, -1, this.random.nextInt(5) - 2);
        var s = level.getBlockState(p);
        if (s.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) && level.getBlockState(p.above()).isAir()) {
            var flowers = java.util.List.of(net.minecraft.world.level.block.Blocks.POPPY, net.minecraft.world.level.block.Blocks.DANDELION,
                    net.minecraft.world.level.block.Blocks.AZURE_BLUET, net.minecraft.world.level.block.Blocks.OXEYE_DAISY);
            level.setBlock(p.above(), flowers.get(this.random.nextInt(flowers.size())).defaultBlockState(), 3);
            n++;
        } else if (s.is(net.minecraft.world.level.block.Blocks.STONE) || s.is(net.minecraft.world.level.block.Blocks.COBBLESTONE) || s.is(net.minecraft.world.level.block.Blocks.DIRT)) {
            level.setBlock(p, net.minecraft.world.level.block.Blocks.MOSS_BLOCK.defaultBlockState(), 3);
            n++;
        }
        if (this.bondedPlayer(Orders.AFFECTION, 32) != null) {
            for (BlockPos q : BlockPos.betweenClosed(ground.offset(-4, -1, -4), ground.offset(4, 1, 4))) {
                var qs = level.getBlockState(q);
                if ((qs.getBlock() instanceof net.minecraft.world.level.block.CropBlock || qs.is(net.minecraft.tags.BlockTags.SAPLINGS))
                        && this.random.nextInt(4) == 0 && net.minecraft.world.item.BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, q.immutable())) {
                    level.levelEvent(1505, q, 0);
                    n++;
                }
            }
        }
        return n;
    }

    private void tickSloth(ServerLevel level) {
        boolean rest = level.isDay() && this.getTarget() == null && level.getGameTime() >= this.getWokenUntil() && !this.isOrderedToSit();
        if (this.isHanging()) {
            if (!rest || this.branch == null || level.getBlockState(this.branch).isAir()) this.letGo();
            else this.setPos(this.branch.getX() + 0.5, this.branch.getY() - this.getBbHeight(), this.branch.getZ() + 0.5);
        } else if (rest && this.tickCount % 40 == 0) {
            BlockPos b = this.findBranch();
            if (b != null) this.hangFrom(b);
        }
        if (this.tickCount % 400 == 0 && !this.isBaby()) this.tend(level);
    }

    // --- the aegis boar -----------------------------------------------------------------------------------------------

    /** The aegis boar never rolls up: she stands in front of her friend. */
    public boolean isBoar() {
        return this.species == GirlSpecies.AEGIS_BOAR_ARMOURED;
    }

    /** The share of a blow meant for her friend that she takes instead. */
    public static final float SHIELD_SHARE = 1.0F / 3.0F;

    /**
     * A player is about to be hurt: the nearest grown aegis boar within 8 blocks who is their friend takes a third of
     * it and turns on whatever struck. Returns what is left for the player.
     */
    public static float shield(Player player, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || amount <= 0) return amount;
        Armoured boar = null;
        for (Armoured a : player.level().getEntitiesOfClass(Armoured.class, player.getBoundingBox().inflate(8),
                a -> a.isBoar() && a.isAlive() && !a.isBaby() && a.getAffection(player) >= Orders.AFFECTION)) {
            if (boar == null || a.distanceToSqr(player) < boar.distanceToSqr(player)) boar = a;
        }
        if (boar == null) return amount;
        float share = amount * SHIELD_SHARE;
        boar.hurt(boar.damageSources().generic(), share);
        if (source.getEntity() instanceof LivingEntity l && l != boar && !(l instanceof Player)) boar.setTarget(l);
        boar.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F);
        return amount - share;
    }

    public boolean isCurled() {
        return this.curled > 0;
    }

    /** Rolled up tight: no damage gets through for a few seconds. */
    public void curlUp() {
        this.curled = CURL_TICKS;
        this.setFlag(FLAG_RETRACTED, true);
        this.getNavigation().stop();
        this.setAction(Action.CURL);
        this.playSound(SoundEvents.ARMADILLO_ROLL, 1.0F, 1.0F);
    }

    @Override
    public float getSpeed() {
        return this.isCurled() ? 0.0F : super.getSpeed();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isCurled() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (!this.level().isClientSide) this.playSound(SoundEvents.ARMADILLO_HURT_REDUCED, 1.0F, 1.0F);
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.isSloth()) this.letGo();
        if (hurt && !this.level().isClientSide && this.getTarget() == null && !this.isBoar() && !this.isSloth() && !this.isGrib()) this.curlUp();   // not while charging a monster
        return hurt;
    }

    /** The rolling charge: she hits as a ball. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) this.playSound(SoundEvents.ARMADILLO_LAND, 1.0F, 0.8F);
        return hit;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.DAEODON_ARMOURED && this.level() instanceof net.minecraft.server.level.ServerLevel soil && !this.isBaby() && this.tickCount % 1200 == 0) this.rooting(soil);
        if (this.species == GirlSpecies.ANTEATER_ARMOURED && this.level() instanceof net.minecraft.server.level.ServerLevel lick && !this.isBaby() && this.tickCount % 20 == 0) this.longTongue(lick);
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.curled > 0 && --this.curled == 0) {
            this.setFlag(FLAG_RETRACTED, false);
            if (this.getAction() == Action.CURL) this.setAction(Action.NONE);
        }
        LivingEntity target = this.getTarget();
        if (this.isSloth()) this.tickSloth(server);
        if (this.isGrib() && !this.isBaby() && this.tickCount % 600 == 0 && this.onGround()) this.root(server);
        if (this.species == GirlSpecies.RIVERDILLO_ARMOURED && this.tickCount % 600 == 0) this.angle(server);
        if (target != null && !this.isCurled() && !this.isBoar() && !this.isSloth() && !this.isGrib()) {     // charging: a rolling ball
            if (this.getAction() != Action.ROLL) this.setAction(Action.ROLL);
            if (this.tickCount % 20 == 0) this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, true, false));
        } else if (this.getAction() == Action.ROLL) {
            this.setAction(Action.NONE);
        }
        if (this.tickCount % 10 == 0 && !this.isCurled() && target == null && !this.isBaby() && !this.isBoar() && !this.isSloth() && !this.isGrib()) {
            for (Player p : server.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(4),
                    p -> p.isSprinting() && !p.isSpectator() && !p.isCreative() && this.getAffection(p) < Orders.AFFECTION)) {
                this.curlUp();
                break;
            }
        }
    }

    // --- food, presents ----------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        if (this.isBoar()) return stack.is(Items.CARROT) || stack.is(Items.POTATO) || stack.is(Items.BEETROOT);
        if (this.isSloth()) return stack.is(net.minecraft.tags.ItemTags.LEAVES) || stack.is(Items.GLOW_BERRIES);
        if (this.species == GirlSpecies.RIVERDILLO_ARMOURED && stack.is(net.minecraft.tags.ItemTags.FISHES)) return true;
        if (this.isGrib()) return stack.is(Items.CARROT) || stack.is(Items.POTATO) || stack.is(Items.BROWN_MUSHROOM) || stack.is(Items.RED_MUSHROOM);
        return stack.is(Items.SPIDER_EYE) || stack.is(Items.BEETROOT) || stack.is(Items.SWEET_BERRIES);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.SPIDER_EYE) ? 2 : 1;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        if (this.species == GirlSpecies.RIVERDILLO_ARMOURED) return new ItemStack(r < 40 ? Items.COD : r < 75 ? Items.SALMON : r < 95 ? Items.TROPICAL_FISH : Items.ARMADILLO_SCUTE, r < 95 ? 2 : 1);
        if (this.isGrib()) return new ItemStack(r < 40 ? Items.BROWN_MUSHROOM : r < 75 ? Items.POTATO : r < 95 ? Items.CARROT : Items.GOLD_NUGGET, r < 95 ? 3 : 2);
        if (this.isSloth()) return new ItemStack(r < 45 ? Items.MOSS_BLOCK : r < 80 ? Items.GLOW_BERRIES : Items.VINE, 2);
        if (this.isBoar()) return r < 50 ? new ItemStack(Items.IRON_NUGGET, 4) : r < 85 ? new ItemStack(Items.BEETROOT, 3) : r < 97 ? new ItemStack(Items.LEATHER, 2)
                : new ItemStack(Items.SHIELD);
        return r < 60 ? new ItemStack(Items.ARMADILLO_SCUTE, r < 20 ? 2 : 1) : r < 85 ? new ItemStack(Items.SPIDER_EYE) : new ItemStack(Items.DEAD_BUSH);
    }

    @Override
    protected int litterSize() {
        return this.random.nextInt(4) == 0 ? 4 : 1;                     // nine-banded armadillos have identical quadruplets
    }

    // --- sounds ----------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBoar() ? SoundEvents.HOGLIN_AMBIENT : SoundEvents.ARMADILLO_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.isBoar() ? SoundEvents.HOGLIN_HURT : SoundEvents.ARMADILLO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isBoar() ? SoundEvents.HOGLIN_DEATH : SoundEvents.ARMADILLO_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return this.isBoar() ? SoundEvents.GENERIC_EAT : SoundEvents.ARMADILLO_EAT;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * (this.isBoar() ? 1.4F : 1.0F);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Curled", this.curled);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.curled = tag.getInt("Curled");
    }

    // --- the daeodon and the anteater (twenty-first wave) -----------------------------------------------------------

    /** Her friend within 10 blocks, standing on grass or dirt, gets what she roots up at their feet: a potato, a carrot or a
     *  beetroot (one time in twenty a golden carrot). True if she found something. */
    public boolean rooting(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 10);
        if (friend == null || !level.getBlockState(friend.blockPosition().below()).is(net.minecraft.tags.BlockTags.DIRT)) return false;
        int roll = this.random.nextInt(20);
        var find = roll == 0 ? net.minecraft.world.item.Items.GOLDEN_CARROT : roll < 7 ? net.minecraft.world.item.Items.POTATO : roll < 14 ? net.minecraft.world.item.Items.CARROT : net.minecraft.world.item.Items.BEETROOT;
        net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(level, friend.getX(), friend.getY() + 0.3, friend.getZ(), new net.minecraft.world.item.ItemStack(find));
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
        level.levelEvent(2001, friend.blockPosition().below(), net.minecraft.world.level.block.Block.getId(level.getBlockState(friend.blockPosition().below())));
        return true;
    }

    /** The loose items within 12 blocks of her friend are licked up and brought to their feet. How many, or -1 without a friend. */
    public int longTongue(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        int n = 0;
        for (net.minecraft.world.entity.item.ItemEntity item : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, friend.getBoundingBox().inflate(12), e -> e.distanceToSqr(friend) > 4.0)) {
            item.teleportTo(friend.getX(), friend.getY() + 0.2, friend.getZ());
            item.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            n++;
        }
        return n;
    }
}
