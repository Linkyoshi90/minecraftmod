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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The plant family (flora_field_guide.html): alraune, matango, ent, cactus, mandragora. Rooted: standing still on soil she
 * heals and the crops around her grow faster (much faster once she is devoted). They want bone meal and water. The
 * alraune dusts attackers with pollen, the matango sinks into mycelium and comes up elsewhere (her spores sicken
 * attackers), the ent plants saplings and hits very hard, the cactus pricks whatever hits her, and the mandragora hides
 * in the ground until someone pulls her out, when she screams.
 */
public class Flora extends AbstractMonsterGirl {
    /** Nectar can be gathered once every half day. */
    public static final int NECTAR_COOLDOWN = 12000;
    private long nextNectar;

    /** Whether a bee could gather nectar from her right now (a grown flower that has nectar to spare). */
    public boolean hasNectar() {
        return !this.isBaby() && this.level().getGameTime() >= this.nextNectar;
    }

    /** Herself, curled up as a bloom to plant elsewhere (AlrauneBloomBlock lets her out again). */
    public ItemStack asBloom() {
        CompoundTag girl = new CompoundTag();
        this.saveWithoutId(girl);
        girl.remove("UUID");                                                // a fresh one when she unfolds (a copied bloom never clashes)
        CompoundTag data = new CompoundTag();
        data.put("Girl", girl);
        ItemStack bloom = new ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.ALRAUNE_BLOOM.get());
        net.minecraft.world.item.BlockItem.setBlockEntityData(bloom, com.linkyoshi90.monstergirls.registry.ModBlockEntities.ALRAUNE_BLOOM.get(), data);
        return bloom;
    }

    /** A bee girl took her nectar. */
    public void giveNectar() {
        this.nextNectar = this.level().getGameTime() + NECTAR_COOLDOWN;
    }

    private int stillTicks;
    private int sunk;
    private long nextPlant;

    public Flora(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(Items.BONE_MEAL), false));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.6) {
            @Override
            public boolean canUse() {
                return !Flora.this.isHidden() && super.canUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case ALRAUNE_FLORA -> girlAttributes(30, 0.1, 3);
            case MATANGO_FLORA -> girlAttributes(20, 0.2, 2);
            case ENT_FLORA -> girlAttributes(80, 0.2, 14).add(Attributes.KNOCKBACK_RESISTANCE, 0.9).add(Attributes.ATTACK_KNOCKBACK, 2.0);
            case CACTUS_FLORA -> girlAttributes(28, 0.18, 4).add(Attributes.ARMOR, 4);
            default -> girlAttributes(10, 0.24, 1);
        };
    }

    public boolean isHidden() {
        return this.hasFlag(FLAG_HIDDEN);
    }

    public void setHidden(boolean on) {
        this.setFlag(FLAG_HIDDEN, on);
        if (on) this.getNavigation().stop();
    }

    // --- food, presents ------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BONE_MEAL) || stack.is(Items.POTION);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        if (stack.is(Items.POTION) && !player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
        return stack.is(Items.BONE_MEAL) || this.species == GirlSpecies.CACTUS_FLORA ? 2 : 1;
    }

    /** Pull a buried mandragora out of the ground: she screams. */
    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (this.species == GirlSpecies.ALRAUNE_FLORA && stack.is(net.minecraft.tags.ItemTags.SHOVELS) && player.isShiftKeyDown() && !this.isBaby()) {
            if (this.getAffection(player) < BED_AFFECTION) return InteractionResult.PASS;   // only for someone she trusts
            if (!this.level().isClientSide) {
                ItemStack bloom = this.asBloom();
                if (!player.getInventory().add(bloom)) player.drop(bloom, false);
                stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                this.playSound(net.minecraft.sounds.SoundEvents.ROOTED_DIRT_BREAK, 1.0F, 1.0F);
                this.discard();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (stack.is(Items.GLASS_BOTTLE) && !this.isBaby()) {                // nectar from her blossoms
            if (this.getAffection(player) < BED_AFFECTION || this.level().getGameTime() < this.nextNectar) return InteractionResult.PASS;
            if (this.level() instanceof net.minecraft.server.level.ServerLevel server) {
                this.nextNectar = server.getGameTime() + NECTAR_COOLDOWN;
                stack.consume(1, player);
                ItemStack nectar = new ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.NECTAR_BOTTLE.get());
                if (!player.getInventory().add(nectar)) player.drop(nectar, false);
                this.playSound(net.minecraft.sounds.SoundEvents.BOTTLE_FILL, 1.0F, 1.2F);
                this.hearts(server, 2);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.species == GirlSpecies.MANDRAGORA_FLORA && this.isHidden()) {
            if (this.level() instanceof ServerLevel server) {
                this.setHidden(false);
                this.scream(server, player);
                this.addAffection(player, 1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return null;
    }

    /** Everyone around but the one who pulled her up is stunned. */
    public void scream(ServerLevel level, @Nullable Player puller) {
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8), e -> e != this && e != puller)) {
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
            e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
        }
        this.startTimedAction(Action.SCREAM, 24);
        this.playSound(SoundEvents.GHAST_SCREAM, 1.2F, 1.8F);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case ENT_FLORA -> new ItemStack(r < 50 ? Items.OAK_SAPLING : r < 75 ? Items.APPLE : Items.BIRCH_SAPLING, 2);
            case MATANGO_FLORA -> new ItemStack(r < 50 ? Items.RED_MUSHROOM : Items.BROWN_MUSHROOM, 2);
            case CACTUS_FLORA -> new ItemStack(r < 60 ? Items.CACTUS : Items.GREEN_DYE, 2);
            case MANDRAGORA_FLORA -> new ItemStack(r < 50 ? Items.POTATO : Items.CARROT, 3);
            default -> new ItemStack(r < 40 ? Items.WHEAT_SEEDS : r < 80 ? Items.PINK_TULIP : Items.HONEY_BOTTLE, r < 40 ? 4 : 1);
        };
    }

    // --- combat --------------------------------------------------------------------------------------------------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) amount *= 2.0F;          // plants burn
        boolean hurt = super.hurt(source, amount);
        if (!hurt || !(this.level() instanceof ServerLevel server)) return hurt;
        if (source.getEntity() instanceof LivingEntity a && a.distanceToSqr(this) < 4 * 4 && !source.is(DamageTypes.THORNS)) {
            switch (this.species) {
                case ALRAUNE_FLORA -> {
                    a.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
                    a.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                    server.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY() + 1.5, this.getZ(), 30, 1, 0.6, 1, 0.01);
                    this.startTimedAction(Action.PUFF, 20);
                }
                case MATANGO_FLORA -> {
                    a.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
                    a.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
                    server.sendParticles(ParticleTypes.MYCELIUM, this.getX(), this.getY() + 1, this.getZ(), 40, 1, 0.6, 1, 0.01);
                    this.sinkAway(server);
                }
                case CACTUS_FLORA -> a.hurt(this.damageSources().thorns(this), 2.0F);
                case MANDRAGORA_FLORA -> this.scream(server, null);
                default -> {
                }
            }
        }
        return hurt;
    }

    /** The matango sinks into the mycelium and comes up somewhere else nearby. */
    public void sinkAway(ServerLevel level) {
        this.setHidden(true);
        this.sunk = 100;
        BlockPos o = this.blockPosition();
        for (int i = 0; i < 20; i++) {
            BlockPos p = o.offset(this.random.nextInt(17) - 8, this.random.nextInt(5) - 2, this.random.nextInt(17) - 8);
            BlockState s = level.getBlockState(p.below());
            if ((s.is(Blocks.MYCELIUM) || s.is(Blocks.PODZOL) || s.is(BlockTags.DIRT)) && level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()) {
                this.teleportTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
                return;
            }
        }
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    /** On soil and standing still. */
    public boolean isRooted() {
        BlockState under = this.level().getBlockState(this.blockPosition().below());
        return this.stillTicks > 40 && (under.is(BlockTags.DIRT) || under.is(Blocks.FARMLAND) || under.is(BlockTags.SAND) || under.is(Blocks.MYCELIUM));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        boolean still = this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4 && this.getTarget() == null;
        this.stillTicks = still ? this.stillTicks + 1 : 0;
        if (this.sunk > 0 && --this.sunk == 0) this.setHidden(false);
        if (this.species == GirlSpecies.MANDRAGORA_FLORA && !this.isBaby() && this.stillTicks > 200 && !this.isHidden() && this.bondedPlayer(10, 16) == null) {
            this.setHidden(true);                                            // she burrows back in, leaves showing
        }
        if (this.isHidden()) this.getNavigation().stop();
        if (this.tickCount % 40 == 0 && this.isRooted()) {
            if (this.getHealth() < this.getMaxHealth()) this.heal(1.0F);
            int tries = this.bondedPlayer(60, 32) != null ? 6 : 2;           // devoted: the farm fairly leaps up
            for (int i = 0; i < tries; i++) {
                BlockPos p = this.blockPosition().offset(this.random.nextInt(13) - 6, this.random.nextInt(3) - 1, this.random.nextInt(13) - 6);
                BlockState s = server.getBlockState(p);
                if (s.getBlock() instanceof CropBlock crop && !crop.isMaxAge(s)) {
                    server.setBlock(p, crop.getStateForAge(crop.getAge(s) + 1), 2);
                    server.levelEvent(2005, p, 0);
                }
            }
        }
        if (server.getGameTime() >= this.nextPlant && this.tickCount % 41 == 0) {
            if (this.species == GirlSpecies.ENT_FLORA) this.plantSapling(server);
            if (this.species == GirlSpecies.ALRAUNE_FLORA) this.plant(server, Blocks.PINK_TULIP);
        }
    }

    private void plantSapling(ServerLevel level) {
        Block sapling = switch (this.getVariantName()) {
            case "birch" -> Blocks.BIRCH_SAPLING;
            case "spruce" -> Blocks.SPRUCE_SAPLING;
            case "cherry" -> Blocks.CHERRY_SAPLING;
            case "autumn" -> Blocks.DARK_OAK_SAPLING;
            default -> Blocks.OAK_SAPLING;
        };
        if (this.plant(level, sapling)) this.startTimedAction(Action.PLANT, 30);
    }

    private boolean plant(ServerLevel level, Block block) {
        this.nextPlant = level.getGameTime() + 2400 + this.random.nextInt(2400);
        for (int i = 0; i < 12; i++) {
            BlockPos p = this.blockPosition().offset(this.random.nextInt(9) - 4, 0, this.random.nextInt(9) - 4);
            if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(p, block.defaultBlockState(), 3);
                return true;
            }
        }
        return false;
    }

    public void setNextPlant(long t) {
        this.nextPlant = t;
    }

    // --- young ---------------------------------------------------------------------------------------------------

    /** A seed in the soil (a clutch block) that sprouts into a seedling. */
    @Override
    protected boolean giveBirth(ServerLevel level, @Nullable UUID father) {
        BlockPos o = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-3, -1, -3), o.offset(3, 1, 3))) {
            if (!canHoldBlock(level, p) || !level.getBlockState(p.below()).is(BlockTags.DIRT)) continue;
            level.setBlock(p, ModBlocks.GIRL_EGG.get().defaultBlockState().setValue(GirlEggBlock.KIND, GirlEggBlock.Kind.LEATHERY), 3);
            if (level.getBlockEntity(p) instanceof GirlEggBlockEntity be) be.setup(this.species.id(), 1, father);
            return true;
        }
        return super.giveBirth(level, father);
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isHidden() ? null : SoundEvents.AZALEA_LEAVES_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.AZALEA_LEAVES_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.AZALEA_BREAK;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.BONE_MEAL_USE;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("NextPlant", this.nextPlant);
        tag.putBoolean("Buried", this.isHidden());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.nextPlant = tag.getLong("NextPlant");
        this.setFlag(FLAG_HIDDEN, tag.getBoolean("Buried"));
    }

    @Override
    public boolean isHealer() {
        return true;
    }
}
