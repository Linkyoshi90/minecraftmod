package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.block.GirlEggBlock;
import com.linkyoshi90.monstergirls.block.GirlEggBlockEntity;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import com.linkyoshi90.monstergirls.entity.ai.AmphibiousMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The sea family (merfolk_field_guide.html): orca, koi, shark, seal, axolotl. Half the world is water: fast
 * swimmers who crawl slowly on land and dry out there; they sing at dusk, lead their favourite to treasure, share
 * their breath, hate drowned, and trade sea treasure for shiny things from the land. Orca, shark and seal bear live
 * young; koi and axolotl lay roe.
 */
public class Merfolk extends AbstractMonsterGirl {
    private int dryTicks;
    private long nextTrade;
    private long nextTreasureSearch;
    @Nullable
    private BlockPos treasure;
    private int playDead;

    public Merfolk(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.moveControl = new AmphibiousMoveControl(this);
        this.goalSelector.removeAllGoals(g -> g instanceof FloatGoal); // she dives; floating only bobs her on the surface
        this.goalSelector.addGoal(1, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true));
        this.goalSelector.addGoal(7, new RandomSwimmingGoal(this, 1.0, 20));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Drowned.class, true));
        if (species == GirlSpecies.ORCA_MERMAID) {
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Guardian.class, true, g -> this.bondedPlayer(BED_AFFECTION, 24) != null));
        }
        if (species == GirlSpecies.SHARK_MERMAID) {                   // blood scent: anything hurt and hostile in the water
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                    m -> m.isInWater() && m.getHealth() < m.getMaxHealth()));
        }
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case ORCA_MERMAID -> girlAttributes(36, 0.25, 6);
            case SHARK_MERMAID -> girlAttributes(30, 0.25, 7);
            case SEAL_MERMAID -> girlAttributes(22, 0.22, 2);
            case AXOLOTL_MERMAID -> girlAttributes(18, 0.22, 3);
            default -> girlAttributes(20, 0.24, 2);
        };
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    @Override
    protected boolean hasSwimAnimation() {
        return true;
    }

    /** Fast in the water; on land a slow crawl (the seal and axolotl manage better). */
    @Override
    public void travel(Vec3 input) {
        if (this.isControlledByLocalInstance() && this.isInWater()) {
            double y = this.getY();
            this.moveRelative(this.getSpeed() * 1.6F, input);
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 v = this.getDeltaMovement().scale(0.9);
            if (this.horizontalCollision && this.isFree(v.x, v.y + 0.6 - this.getY() + y, v.z)) v = new Vec3(v.x, 0.3, v.z); // up onto the bank
            this.setDeltaMovement(v);
            return;
        }
        super.travel(input);
    }

    @Override
    public float getSpeed() {
        float s = super.getSpeed();
        if (this.isInWater()) return s;
        boolean landy = this.species == GirlSpecies.SEAL_MERMAID || this.species == GirlSpecies.AXOLOTL_MERMAID;
        return s * (landy ? 0.55F : 0.33F);
    }

    // --- drying out ----------------------------------------------------------------------------------------------

    public int dryLimit() {
        return switch (this.species) {
            case SEAL_MERMAID, AXOLOTL_MERMAID -> 6000;
            default -> 2400;
        };
    }

    public int getDryTicks() {
        return this.dryTicks;
    }

    public void setDryTicks(int t) {
        this.dryTicks = t;
    }

    private boolean onBeach() {
        var below = this.level().getBlockState(this.blockPosition().below());
        return below.is(BlockTags.SAND) || below.is(BlockTags.ICE) || below.is(BlockTags.SNOW);
    }

    // --- interaction ---------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.FISHES) || ((this.species == GirlSpecies.KOI_MERMAID || this.species == GirlSpecies.AXOLOTL_MERMAID)
                && (stack.is(Items.KELP) || stack.is(Items.DRIED_KELP) || stack.is(Items.BREAD)));
    }

    private static boolean shiny(ItemStack s) {
        return s.is(Items.GOLD_INGOT) || s.is(Items.COPPER_INGOT) || s.is(Items.IRON_INGOT) || s.is(Items.AMETHYST_SHARD) || s.is(Items.EMERALD) || s.is(Items.GOLD_NUGGET);
    }

    /** A shiny thing from the land, traded for a treasure of the sea. */
    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (!shiny(stack) || this.isBaby()) return null;
        if (this.level() instanceof ServerLevel server && server.getGameTime() >= this.nextTrade) {
            this.nextTrade = server.getGameTime() + 600;
            stack.consume(1, player);
            this.addAffection(player, 2);
            ItemStack back = this.seaTreasure(this.getAffection(player));
            if (!player.getInventory().add(back)) this.spawnAtLocation(back);
            this.startTimedAction(Action.SING, 40);
            this.hearts(server, 3);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private ItemStack seaTreasure(int affection) {
        int r = this.random.nextInt(100);
        if (affection >= 60 && r < 3) return new ItemStack(Items.HEART_OF_THE_SEA);
        if (r < 12) return new ItemStack(Items.NAUTILUS_SHELL);
        if (r < 35) return new ItemStack(Items.PRISMARINE_CRYSTALS, 2);
        if (r < 60) return new ItemStack(Items.PRISMARINE_SHARD, 2);
        if (r < 80) return new ItemStack(Items.INK_SAC);
        return new ItemStack(Items.SEA_PICKLE);
    }

    @Override
    public ItemStack rollGift(Player player) {
        return switch (this.species) {
            case SHARK_MERMAID -> new ItemStack(this.random.nextBoolean() ? Items.BONE : Items.COD);
            case SEAL_MERMAID, ORCA_MERMAID -> new ItemStack(this.random.nextBoolean() ? Items.COD : Items.SALMON, 2);
            case AXOLOTL_MERMAID -> new ItemStack(this.random.nextBoolean() ? Items.TROPICAL_FISH : Items.CLAY_BALL);
            default -> this.seaTreasure(this.getAffection(player));
        };
    }

    // --- young ---------------------------------------------------------------------------------------------------

    private boolean laysRoe() {
        return this.species == GirlSpecies.KOI_MERMAID || this.species == GirlSpecies.AXOLOTL_MERMAID;
    }

    @Override
    protected int litterSize() {
        return switch (this.species) {
            case SHARK_MERMAID -> 2 + this.random.nextInt(2);
            case KOI_MERMAID, AXOLOTL_MERMAID -> 2 + this.random.nextInt(3);
            default -> 1;
        };
    }

    /** Koi and axolotl lay roe on the bottom of the water; the others give birth to a calf or pups. */
    @Override
    protected boolean giveBirth(ServerLevel level, @Nullable UUID father) {
        if (!this.laysRoe()) return super.giveBirth(level, father);
        BlockPos o = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-6, -4, -6), o.offset(6, 2, 6))) {
            if (!level.getFluidState(p).is(FluidTags.WATER) || !level.getFluidState(p).isSource() || !level.getBlockState(p).is(net.minecraft.world.level.block.Blocks.WATER)) continue;
            if (!level.getBlockState(p.below()).isFaceSturdy(level, p.below(), net.minecraft.core.Direction.UP)) continue;
            level.setBlock(p, ModBlocks.GIRL_EGG.get().defaultBlockState().setValue(GirlEggBlock.KIND, GirlEggBlock.Kind.ROE).setValue(GirlEggBlock.WATERLOGGED, true), 3);
            if (level.getBlockEntity(p) instanceof GirlEggBlockEntity be) be.setup(this.species.id(), this.litterSize(), father);
            return true;
        }
        return false;                                                   // no water nearby: she keeps carrying
    }

    @Override
    protected boolean hasTrueForm() {
        return this.species == GirlSpecies.KOI_MERMAID;               // the dragon koi
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (this.species) {
            case AXOLOTL_MERMAID -> this.isInWater() ? SoundEvents.AXOLOTL_IDLE_WATER : SoundEvents.AXOLOTL_IDLE_AIR;
            case KOI_MERMAID -> SoundEvents.FISH_SWIM;
            default -> this.isInWater() ? SoundEvents.DOLPHIN_AMBIENT_WATER : SoundEvents.DOLPHIN_AMBIENT;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.species == GirlSpecies.AXOLOTL_MERMAID ? SoundEvents.AXOLOTL_HURT : SoundEvents.DOLPHIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.species == GirlSpecies.AXOLOTL_MERMAID ? SoundEvents.AXOLOTL_DEATH : SoundEvents.DOLPHIN_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.DOLPHIN_EAT;
    }

    // --- combat --------------------------------------------------------------------------------------------------

    /** The axolotl plays dead when badly hurt: monsters lose interest while she heals. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.playDead > 0 && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.species == GirlSpecies.AXOLOTL_MERMAID && this.getHealth() < this.getMaxHealth() * 0.5F && this.isAlive()) {
            this.playDead = 200;
            this.setAction(Action.LYING);
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            for (Mob m : this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(16), m -> m.getTarget() == this)) m.setTarget(null);
        }
        return hurt;
    }

    public boolean isPlayingDead() {
        return this.playDead > 0;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.playDead > 0 && --this.playDead == 0 && this.getAction() == Action.LYING) this.setAction(Action.NONE);
        boolean wet = this.isInWaterRainOrBubble() || (this.species == GirlSpecies.SEAL_MERMAID && this.onBeach());
        this.dryTicks = wet ? 0 : this.dryTicks + 1;
        if (this.dryTicks > this.dryLimit() && this.tickCount % 40 == 0) this.hurt(this.damageSources().dryOut(), 1.0F);
        if (this.tickCount % 40 != 0) return;
        Player p = this.bondedPlayer(BED_AFFECTION, 32);
        if (p == null) return;
        boolean full = this.getAffection(p) >= FULL_AFFECTION;
        if (p.isInWater() && p.distanceToSqr(this) < 8 * 8) {         // her breath: water breathing (conduit power when devoted)
            p.addEffect(new MobEffectInstance(full ? MobEffects.CONDUIT_POWER : MobEffects.WATER_BREATHING, 200, 0, true, false));
        }
        long day = server.getDayTime() % 24000;
        if (day > 12000 && day < 13800 && this.getAction() == Action.NONE && !this.isInWater()) this.startTimedAction(Action.SING, 120);
        if (this.getAction() == Action.SING && p.isInWater() && p.distanceToSqr(this) < 16 * 16) {
            p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100, 0, true, false));
        }
        if (this.species == GirlSpecies.KOI_MERMAID && this.isInWater()) p.addEffect(new MobEffectInstance(MobEffects.LUCK, 200, 0, true, false));
        if (this.isTrueForm()) server.sendParticles(ParticleTypes.GLOW, this.getX(), this.getY() + 0.8, this.getZ(), 3, 0.4, 0.4, 0.4, 0.0);
        // a guide to treasure while her favourite is at sea
        if ((p.isInWater() || p.getVehicle() instanceof Boat) && p.distanceToSqr(this) < 16 * 16 && this.isInWater()) {
            if (server.getGameTime() >= this.nextTreasureSearch) {
                this.nextTreasureSearch = server.getGameTime() + 600;
                this.treasure = server.findNearestMapStructure(StructureTags.DOLPHIN_LOCATED, this.blockPosition(), 50, false);
            }
            if (this.treasure != null && this.getNavigation().isDone() && this.getTarget() == null) {
                Vec3 dir = Vec3.atCenterOf(this.treasure).subtract(this.position()).normalize().scale(14);
                Vec3 t = this.position().add(dir);
                this.getNavigation().moveTo(t.x, this.getY(), t.z, 1.2);
                server.sendParticles(ParticleTypes.DOLPHIN, this.getX(), this.getY() + 0.5, this.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
            }
        }
    }

    /** After an axolotl fights beside her player, the player regenerates (like a vanilla axolotl). */
    public static void onKill(Merfolk killer, LivingEntity victim) {
        if (killer.species != GirlSpecies.AXOLOTL_MERMAID) return;
        Player p = killer.bondedPlayer(BED_AFFECTION, 20);
        if (p != null) p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DryTicks", this.dryTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.dryTicks = tag.getInt("DryTicks");
    }
}
