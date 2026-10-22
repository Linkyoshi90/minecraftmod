package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.block.GirlEggBlock;
import com.linkyoshi90.monstergirls.block.GirlEggBlockEntity;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The reptile family (lizardfolk_field_guide.html): salamander, chameleon, iguana, crocodile, turtle. Cold-blooded:
 * sunlight or a nearby fire makes them quick and heals them, cold slows them; their scales are armour. The salamander
 * is fireproof and burns what she hits, the chameleon vanishes while still and snatches items with her tongue, the
 * iguana is the fastest, the crocodile death-rolls her prey in water (and attacks until fed), the turtle hides in her
 * shell. They eat eggs and fish and lay leathery clutches.
 */
public class Lizardfolk extends AbstractMonsterGirl {
    private int stillTicks;
    private int retracted;

    public Lizardfolk(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        if (species == GirlSpecies.SALAMANDER_LIZARDFOLK) {
            this.setPathfindingMalus(PathType.LAVA, 0.0F);
            this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
            this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        }
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        if (species == GirlSpecies.TURTLE_LIZARDFOLK || species == GirlSpecies.CHAMELEON_LIZARDFOLK) this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(Items.EGG, Items.TURTLE_EGG, Items.COD, Items.SALMON), false));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override
            public boolean canUse() {
                return Lizardfolk.this.retracted == 0 && super.canUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case SALAMANDER_LIZARDFOLK -> girlAttributes(26, 0.3, 5).add(Attributes.ARMOR, 4);
            case CHAMELEON_LIZARDFOLK -> girlAttributes(18, 0.24, 3).add(Attributes.ARMOR, 2);
            case IGUANA_LIZARDFOLK -> girlAttributes(22, 0.4, 3).add(Attributes.ARMOR, 3);
            case CROC_LIZARDFOLK -> girlAttributes(40, 0.28, 9).add(Attributes.ARMOR, 8);
            case SNAPPING_TURTLE_LIZARDFOLK -> girlAttributes(36, 0.18, 7).add(Attributes.ARMOR, 8);
            default -> girlAttributes(26, 0.2, 2).add(Attributes.ARMOR, 6);
        };
    }

    @Override
    protected boolean wildHostile() {
        return this.species == GirlSpecies.CROC_LIZARDFOLK;
    }

    // --- food ----------------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.EGG) || stack.is(Items.TURTLE_EGG) || stack.is(ModTags.LAID_EGGS) || stack.is(ItemTags.FISHES)
                || (this.species == GirlSpecies.TURTLE_LIZARDFOLK && stack.is(Items.SEAGRASS));
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(ModTags.LAID_EGGS) || stack.is(Items.TURTLE_EGG) ? 2 : 1;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case TURTLE_LIZARDFOLK -> new ItemStack(r < 30 ? Items.TURTLE_SCUTE : r < 70 ? Items.SEAGRASS : Items.NAUTILUS_SHELL);
            case SNAPPING_TURTLE_LIZARDFOLK -> new ItemStack(r < 45 ? Items.COD : r < 80 ? Items.SALMON : Items.CLAY_BALL, r < 80 ? 1 : 4);
            case SALAMANDER_LIZARDFOLK -> new ItemStack(r < 50 ? Items.MAGMA_CREAM : Items.BLAZE_POWDER);
            case CROC_LIZARDFOLK -> new ItemStack(r < 60 ? Items.BONE : Items.LEATHER, 2);
            default -> new ItemStack(r < 40 ? Items.SLIME_BALL : r < 80 ? Items.COCOA_BEANS : Items.EMERALD);
        };
    }

    // --- combat --------------------------------------------------------------------------------------------------

    /** The two that carry a shell and hide in it. */
    private boolean shelled() {
        return this.species == GirlSpecies.TURTLE_LIZARDFOLK || this.species == GirlSpecies.SNAPPING_TURTLE_LIZARDFOLK;
    }

    public boolean isRetracted() {
        return this.retracted > 0;
    }

    @Override
    public float getSpeed() {
        return this.isRetracted() ? 0.0F : super.getSpeed();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isRetracted()) amount *= 0.2F;
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.shelled() && !this.level().isClientSide) {
            this.retracted = 100;
            this.setFlag(FLAG_RETRACTED, true);
            this.getNavigation().stop();
            this.setAction(Action.RETRACT);
        }
        if (hurt) this.stillTicks = 0;
        return hurt;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (!hit || !(target instanceof LivingEntity l)) return hit;
        if (this.species == GirlSpecies.SALAMANDER_LIZARDFOLK) l.igniteForSeconds(4);
        if (this.species == GirlSpecies.CROC_LIZARDFOLK && this.isInWater()) {   // the death roll
            l.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
            l.hurt(this.damageSources().mobAttack(this), 4.0F);
            this.startTimedAction(Action.ROLL, 30);
        }
        return hit;
    }

    // --- young ---------------------------------------------------------------------------------------------------

    @Override
    protected int litterSize() {
        return 2 + this.random.nextInt(3);
    }

    /** A leathery clutch in warm sand or dirt (the salamander's in netherrack). */
    @Override
    protected boolean giveBirth(ServerLevel level, @Nullable UUID father) {
        BlockPos o = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-5, -2, -5), o.offset(5, 2, 5))) {
            if (!canHoldBlock(level, p)) continue;
            var under = level.getBlockState(p.below());
            if (!under.is(BlockTags.SAND) && !under.is(BlockTags.DIRT) && !under.is(net.minecraft.world.level.block.Blocks.NETHERRACK)) continue;
            level.setBlock(p, ModBlocks.GIRL_EGG.get().defaultBlockState().setValue(GirlEggBlock.KIND, GirlEggBlock.Kind.LEATHERY), 3);
            if (level.getBlockEntity(p) instanceof GirlEggBlockEntity be) be.setup(this.species.id(), this.litterSize(), father);
            return true;
        }
        return super.giveBirth(level, father);
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    /** Sunlight, or a fire, campfire or lava within four blocks. */
    public boolean isWarm() {
        Level level = this.level();
        if (level.isDay() && level.canSeeSky(this.blockPosition().above())) return true;
        for (BlockPos p : BlockPos.betweenClosed(this.blockPosition().offset(-4, -2, -4), this.blockPosition().offset(4, 2, 4))) {
            var s = level.getBlockState(p);
            if (s.is(BlockTags.FIRE) || s.is(BlockTags.CAMPFIRES) || s.is(net.minecraft.world.level.block.Blocks.LAVA) || s.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.retracted > 0 && --this.retracted == 0) {
            this.setFlag(FLAG_RETRACTED, false);
            if (this.getAction() == Action.RETRACT) this.setAction(Action.NONE);
        }
        if (this.species == GirlSpecies.SALAMANDER_LIZARDFOLK) this.glow(server, 8);
        if (this.species == GirlSpecies.CHAMELEON_LIZARDFOLK && this.tickCount % 100 == 0) this.blend(server);
        if (this.species == GirlSpecies.CHAMELEON_LIZARDFOLK) {               // still and unseen
            boolean still = this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4 && this.getTarget() == null;
            this.stillTicks = still ? this.stillTicks + 1 : 0;
            boolean hide = this.stillTicks > 40;
            if (hide != this.isInvisible()) this.setInvisible(hide);
            if (this.tickCount % 61 == 0) this.snatch(server);
        }
        if (this.species == GirlSpecies.SNAPPING_TURTLE_LIZARDFOLK && this.tickCount % 200 == 0) this.lure(server);
        if (this.tickCount % 40 != 0) return;
        if (this.isWarm()) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false));
            if (this.getHealth() < this.getMaxHealth()) this.heal(1.0F);
            if (this.getTarget() == null && this.getNavigation().isDone() && this.level().isDay() && this.random.nextInt(8) == 0
                    && this.species != GirlSpecies.SALAMANDER_LIZARDFOLK) {
                this.startTimedAction(Action.BASK, 200);
            }
        } else if (server.getBiome(this.blockPosition()).value().coldEnoughToSnow(this.blockPosition())) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, true, false));
        }
        Player p = this.bondedPlayer(60, 12);                                 // devoted: she shares her toughness in a fight
        if (p != null && (this.getTarget() != null || p.getLastHurtByMob() != null)) {
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0, true, false));
        }
    }

    /**
     * The alligator snapping turtle's tongue lure: sitting still in water she now and then catches a fish and drops it
     * at her feet, never more than three lying around (so a lure farm stays small).
     */
    public void lure(ServerLevel level) {
        if (!this.isInWater() || this.getTarget() != null || !this.getNavigation().isDone() || this.isBaby() || this.random.nextInt(6) != 0) return;
        if (level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(4), e -> e.getItem().is(ItemTags.FISHES)).size() >= 3) return;
        this.spawnAtLocation(new ItemStack(this.random.nextInt(3) == 0 ? Items.SALMON : Items.COD));
        this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.6F, 0.8F);
    }

    /** The chameleon's colours follow her surroundings: the jungle's bright panther, desert yellow, woodland brown, veiled in swamps. */
    private void blend(ServerLevel level) {
        var biome = level.getBiome(this.blockPosition());
        String key = biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_JUNGLE) ? "panther"
                : biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_HOT_OVERWORLD) && biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_DRY_OVERWORLD) ? "yellow"
                : biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_SWAMP) ? "veiled"
                : biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_CONIFEROUS_TREE) || biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_DEAD) ? "brown"
                : "green";
        int index = this.species.variants().indexOf(key);
        if (index >= 0 && index != this.getVariantIndex()) this.setVariantIndex(index);
    }

    /** The chameleon's tongue: a dropped item within four blocks shoots over to her feet. */
    private void snatch(ServerLevel level) {
        ItemEntity item = this.nearest(ItemEntity.class, 4.0, e -> e.isAlive() && e.distanceToSqr(this) > 1.5);
        if (item == null) return;
        Vec3 pull = this.position().add(0, 0.5, 0).subtract(item.position()).normalize().scale(0.6);
        item.setDeltaMovement(pull.x, 0.25, pull.z);
        this.getLookControl().setLookAt(item);
        this.startTimedAction(Action.HISS, 10);
        this.playSound(SoundEvents.FROG_TONGUE, 1.0F, 1.2F);
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.shelled() ? SoundEvents.TURTLE_AMBIENT_LAND : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.shelled() ? SoundEvents.TURTLE_HURT : SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.shelled() ? SoundEvents.TURTLE_DEATH : SoundEvents.PLAYER_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.TURTLE_EGG_CRACK;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Retracted", this.retracted);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.retracted = tag.getInt("Retracted");
    }
}
