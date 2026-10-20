package com.linkyoshi90.monstergirls.entity.girl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * The jelly family (slime_field_guide.html): verdant, magma, honey, frost, amethyst. You are what you eat: she
 * absorbs what she picks up (a 9-slot living storage once devoted), takes her colour from dyes, splits in two
 * instead of dying (once a day; the halves merge again), bounces without fall damage, and carries her element:
 * verdant grows crops, magma cooks food and glows, honey sticks and fills bottles, frost freezes water and puts out
 * fires, amethyst rings near ore. Hits are hard on her trust: one halves it.
 */
public class SlimeGirl extends AbstractMonsterGirl {
    private static final long SPLIT_COOLDOWN = 24000L;

    private final SimpleContainer belly = new SimpleContainer(9);
    private long lastSplit = -SPLIT_COOLDOWN;
    @Nullable
    private UUID partner;
    private int halfAge;
    private int stillTicks;
    private long nextHoney;
    private long nextChill;
    private int resonance;

    public SlimeGirl(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.setCanPickUpLoot(true);
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(favourites(species).toArray(new net.minecraft.world.level.ItemLike[0])), false));
        this.goalSelector.addGoal(5, new FollowEntityGoal(this, this::partnerEntity, 1.2, 1.0F, 0.5F));
        this.goalSelector.addGoal(6, new BounceGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return girlAttributes(s == GirlSpecies.MAGMA_SLIME ? 24 : 18, 0.24, s == GirlSpecies.MAGMA_SLIME ? 4 : 2);
    }

    /** Each species' favourite food (it also tempts her). */
    public static List<net.minecraft.world.item.Item> favourites(GirlSpecies s) {
        return switch (s) {
            case MAGMA_SLIME -> List.of(Items.MAGMA_CREAM, Items.BLAZE_POWDER, Items.COAL);
            case HONEY_SLIME -> List.of(Items.HONEY_BOTTLE, Items.HONEYCOMB, Items.SWEET_BERRIES);
            case FROST_SLIME -> List.of(Items.ICE, Items.SNOWBALL, Items.PACKED_ICE);
            case AMETHYST_SLIME -> List.of(Items.AMETHYST_SHARD, Items.QUARTZ);
            default -> List.of(Items.SLIME_BALL, Items.MELON_SLICE, Items.APPLE);
        };
    }

    /** The dye that turns her into each of her five colours, in variant order. */
    public static List<DyeColor> dyes(GirlSpecies s) {
        return switch (s) {
            case MAGMA_SLIME -> List.of(DyeColor.ORANGE, DyeColor.BLACK, DyeColor.BLUE, DyeColor.WHITE, DyeColor.RED);
            case HONEY_SLIME -> List.of(DyeColor.YELLOW, DyeColor.ORANGE, DyeColor.WHITE, DyeColor.BROWN, DyeColor.PINK);
            case FROST_SLIME -> List.of(DyeColor.LIGHT_BLUE, DyeColor.WHITE, DyeColor.LIME, DyeColor.BLUE, DyeColor.PINK);
            case AMETHYST_SLIME -> List.of(DyeColor.PURPLE, DyeColor.PINK, DyeColor.YELLOW, DyeColor.GRAY, DyeColor.GREEN);
            default -> List.of(DyeColor.LIME, DyeColor.WHITE, DyeColor.BROWN, DyeColor.CYAN, DyeColor.YELLOW);
        };
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return favourites(this.species).stream().anyMatch(stack::is);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return this.isFood(stack) ? 2 : 1;
    }

    /** A hit halves her trust (and lowers it by at least 3). */
    @Override
    protected void onHurtByPlayer(Player player) {
        int a = this.getAffection(player);
        this.setAffection(player.getUUID(), Math.min(a - 3, a / 2));
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    // --- interaction ---------------------------------------------------------------------------------------------

    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        boolean client = this.level().isClientSide;
        if (stack.getItem() instanceof DyeItem dye) {                       // she takes the colour of the dye
            int i = dyes(this.species).indexOf(dye.getDyeColor());
            if (i < 0) return null;
            if (!client) {
                this.setVariantIndex(i);
                stack.consume(1, player);
                this.startTimedAction(Action.ABSORB, 24);
                this.playSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.2F);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (stack.isEmpty() && player.isShiftKeyDown() && this.getAffection(player) >= 60) {   // her living storage
            if (!client) player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x1, id, inv, this.belly, 1), this.getDisplayName()));
            return InteractionResult.sidedSuccess(client);
        }
        if (this.species == GirlSpecies.MAGMA_SLIME && !stack.isEmpty() && this.level() instanceof ServerLevel server) {   // a living furnace
            var recipe = server.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), server);
            if (recipe.isPresent() && stack.has(net.minecraft.core.component.DataComponents.FOOD)) {
                ItemStack cooked = recipe.get().value().getResultItem(server.registryAccess()).copy();
                stack.consume(1, player);
                if (!player.getInventory().add(cooked)) this.spawnAtLocation(cooked);
                this.playSound(SoundEvents.FURNACE_FIRE_CRACKLE, 1.0F, 1.0F);
                server.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.2, this.getZ(), 6, 0.2, 0.3, 0.2, 0.01);
                return InteractionResult.SUCCESS;
            }
        }
        if (this.species == GirlSpecies.HONEY_SLIME && stack.is(Items.GLASS_BOTTLE) && this.level() instanceof ServerLevel server
                && server.getGameTime() >= this.nextHoney) {
            this.nextHoney = server.getGameTime() + 1000;
            stack.consume(1, player);
            ItemStack honey = new ItemStack(Items.HONEY_BOTTLE);
            if (!player.getInventory().add(honey)) this.spawnAtLocation(honey);
            this.playSound(SoundEvents.BOTTLE_FILL, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    // --- absorbing -----------------------------------------------------------------------------------------------

    private static boolean shiny(ItemStack s) {
        return s.is(Items.GOLD_INGOT) || s.is(Items.GOLD_NUGGET) || s.is(Items.DIAMOND) || s.is(Items.EMERALD) || s.is(Items.AMETHYST_SHARD) || s.is(Items.COPPER_INGOT);
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return !this.hasFlag(FLAG_HALF) && (this.isFood(stack) || shiny(stack) || stack.getItem() instanceof DyeItem) && this.belly.canAddItem(stack);
    }

    @Override
    protected void pickUpItem(ItemEntity item) {
        ItemStack stack = item.getItem();
        if (!this.wantsToPickUp(stack)) return;
        this.onItemPickup(item);
        this.take(item, stack.getCount());
        if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0F * stack.getCount());
            item.discard();
        } else {
            ItemStack rest = this.belly.addItem(stack.copy());
            if (rest.isEmpty()) item.discard();
            else item.setItem(rest);
        }
        this.startTimedAction(Action.ABSORB, 24);
        this.playSound(SoundEvents.SLIME_SQUISH_SMALL, 1.0F, 1.3F);
    }

    public SimpleContainer getBelly() {
        return this.belly;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        for (int i = 0; i < this.belly.getContainerSize(); i++) if (!this.belly.getItem(i).isEmpty()) this.spawnAtLocation(this.belly.getItem(i));
    }

    // --- splitting -----------------------------------------------------------------------------------------------

    /** A killing blow splits her in two small halves (once a day) that find each other again and merge. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && amount >= this.getHealth() && !this.hasFlag(FLAG_HALF) && !this.isBaby()
                && this.level().getGameTime() - this.lastSplit >= SPLIT_COOLDOWN && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.split((ServerLevel) this.level());
            if (source.getEntity() instanceof Player p) this.onHurtByPlayer(p);
            return false;
        }
        return super.hurt(source, amount);
    }

    private void split(ServerLevel level) {
        this.lastSplit = level.getGameTime();
        this.setHealth(this.getMaxHealth() * 0.25F);
        this.startTimedAction(Action.SPLIT, 20);
        this.playSound(SoundEvents.SLIME_SQUISH, 1.0F, 0.8F);
        level.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 1, this.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
        if (!(this.getType().create(level) instanceof SlimeGirl half)) return;
        half.moveTo(this.getX() + this.random.nextGaussian(), this.getY(), this.getZ() + this.random.nextGaussian(), this.getYRot(), 0);
        half.setFlag(FLAG_HALF, true);
        half.setVariantIndex(this.getVariantIndex());
        half.setHealth(half.getMaxHealth() * 0.25F);
        half.partner = this.getUUID();
        half.setPersistenceRequired();
        level.addFreshEntity(half);
    }

    public boolean isHalf() {
        return this.hasFlag(FLAG_HALF);
    }

    @Nullable
    private LivingEntity partnerEntity() {
        return this.partner != null && this.level() instanceof ServerLevel server && server.getEntity(this.partner) instanceof LivingEntity l ? l : null;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !this.isHalf() && super.removeWhenFarAway(distance);
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.isHalf()) {                                             // find the other half and merge
            this.halfAge++;
            LivingEntity other = this.partnerEntity();
            if (other instanceof SlimeGirl whole && whole.isAlive() && this.distanceToSqr(whole) < 2.25) {
                whole.heal(this.getHealth() + 4.0F);
                server.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 0.5, this.getZ(), 15, 0.3, 0.3, 0.3, 0.05);
                this.discard();
                return;
            }
            if (other == null && this.halfAge > 1200) {                  // her other half is gone: she becomes whole
                this.setFlag(FLAG_HALF, false);
                this.partner = null;
            }
        }
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4;
        this.stillTicks = moving ? 0 : this.stillTicks + 1;
        switch (this.species) {
            case VERDANT_SLIME -> this.garden(server);
            case MAGMA_SLIME -> this.glow(server, 12);
            case FROST_SLIME -> this.frost(server);
            case HONEY_SLIME -> this.sticky(server);
            case AMETHYST_SLIME -> this.resonate(server);
            default -> {
            }
        }
    }

    /** Verdant: crops and saplings around where she rests grow faster. */
    private void garden(ServerLevel level) {
        if (this.stillTicks < 100 || this.tickCount % 300 != 0) return;
        BlockPos o = this.blockPosition();
        for (int i = 0; i < 6; i++) {
            BlockPos p = o.offset(this.random.nextInt(9) - 4, this.random.nextInt(3) - 1, this.random.nextInt(9) - 4);
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof CropBlock || s.is(BlockTags.SAPLINGS)) {
                if (BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, p)) {
                    level.levelEvent(1505, p, 15);
                    return;
                }
            }
        }
    }

    /** Frost: water freezes under her; she puts out a burning favourite (and shields them from fire for a moment). */
    private void frost(ServerLevel level) {
        if (this.onGround() || this.isInWater()) {
            // standing on water she freezes it under her; swimming, she freezes the surface around her (never her own block)
            BlockPos o = this.isInWater() ? this.blockPosition() : this.blockPosition().below();
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-2, 0, -2), o.offset(2, 0, 2))) {
                if (p.equals(this.blockPosition())) continue;
                if (level.getBlockState(p).is(Blocks.WATER) && level.getFluidState(p).isSource() && level.getBlockState(p.above()).isAir()) {
                    level.setBlockAndUpdate(p, Blocks.FROSTED_ICE.defaultBlockState());
                    level.scheduleTick(p, Blocks.FROSTED_ICE, net.minecraft.util.Mth.nextInt(this.random, 60, 120));
                }
            }
        }
        if (level.getGameTime() < this.nextChill) return;
        Player p = this.bondedPlayer(BED_AFFECTION, 3);
        if (p != null && p.isOnFire()) {
            this.nextChill = level.getGameTime() + 1200;
            p.clearFire();
            p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
            level.sendParticles(ParticleTypes.SNOWFLAKE, p.getX(), p.getY() + 1, p.getZ(), 20, 0.4, 0.6, 0.4, 0.02);
        }
    }

    /** Honey: whatever bumps into her sticks a little. */
    private void sticky(ServerLevel level) {
        if (this.tickCount % 10 != 0) return;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.4), e -> e != this && !(e instanceof Player p && this.getAffection(p) >= 10))) {
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
        }
    }

    /** Amethyst: she chimes, faster and brighter the more ore is within 8 blocks. */
    private void resonate(ServerLevel level) {
        if (this.tickCount % 40 == 0) {
            int n = 0;
            BlockPos o = this.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-8, -8, -8), o.offset(8, 8, 8))) {
                if (level.getBlockState(p).is(net.neoforged.neoforge.common.Tags.Blocks.ORES)) n++;
            }
            this.resonance = n;
        }
        if (this.resonance > 0 && this.tickCount % Math.max(10, 80 - this.resonance * 6) == 0) {
            this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 0.8F + Math.min(1.0F, this.resonance * 0.05F));
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.4, this.getZ(), Math.min(8, this.resonance), 0.3, 0.3, 0.3, 0.01);
        }
    }

    public int getResonance() {
        return this.resonance;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && this.species == GirlSpecies.MAGMA_SLIME) target.igniteForSeconds(3);
        return hit;
    }

    // --- presents & young ----------------------------------------------------------------------------------------

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case AMETHYST_SLIME -> new ItemStack(r < 3 ? Items.DIAMOND : r < 30 ? Items.QUARTZ : Items.AMETHYST_SHARD, r < 30 ? 1 : 3);
            case HONEY_SLIME -> new ItemStack(r < 50 ? Items.HONEYCOMB : Items.HONEY_BOTTLE);
            case MAGMA_SLIME -> new ItemStack(r < 60 ? Items.MAGMA_CREAM : Items.BLAZE_POWDER);
            case FROST_SLIME -> new ItemStack(r < 60 ? Items.SNOWBALL : Items.BLUE_ICE, r < 60 ? 8 : 1);
            default -> new ItemStack(r < 60 ? Items.SLIME_BALL : Items.BONE_MEAL, 3);
        };
    }

    @Override
    protected int litterSize() {
        return 2 + this.random.nextInt(2);                              // budding
    }

    @Override
    public float sizeScale() {
        return this.isBaby() ? 0.35F : super.sizeScale();
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.species == GirlSpecies.AMETHYST_SLIME ? SoundEvents.AMETHYST_BLOCK_CHIME : SoundEvents.SLIME_SQUISH_SMALL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SLIME_HURT_SMALL;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH_SMALL;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.HONEY_DRINK;
    }

    @Override
    public boolean fireImmune() {
        return this.species == GirlSpecies.MAGMA_SLIME || super.fireImmune();
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Belly", this.belly.createTag(this.registryAccess()));
        tag.putLong("LastSplit", this.lastSplit);
        if (this.partner != null) tag.putUUID("Partner", this.partner);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.belly.fromTag(tag.getList("Belly", 10), this.registryAccess());
        if (tag.contains("LastSplit")) this.lastSplit = tag.getLong("LastSplit");
        this.partner = tag.hasUUID("Partner") ? tag.getUUID("Partner") : null;
    }
}
