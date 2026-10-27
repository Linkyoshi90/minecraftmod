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
 * fires, amethyst rings near ore. Hits are hard on her trust: one halves it. The ender slime teleports away from blows
 * and water like an enderman, and while she is wild she picks up loose blocks and sets them down somewhere else.
 */
public class SlimeGirl extends AbstractMonsterGirl {
    private static final long SPLIT_COOLDOWN = 24000L;

    private final SimpleContainer belly = new SimpleContainer(9);
    /** The mimic's hoard: the loot of the chest she was. */
    private final SimpleContainer hoard = new SimpleContainer(27);
    /** One unopened loot chest in this many is a mimic. */
    public static final int MIMIC_ODDS = 8;
    private long lastSplit = -SPLIT_COOLDOWN;
    @Nullable
    private UUID partner;
    private int halfAge;
    private int stillTicks;
    private long nextHoney;
    private long nextChill;
    private int resonance;
    @Nullable
    private BlockState carried;
    private long nextMove;

    public SlimeGirl(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.setCanPickUpLoot(true);
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(favourites(species).toArray(new net.minecraft.world.level.ItemLike[0])), false));
        this.goalSelector.addGoal(5, new FollowEntityGoal(this, this::partnerEntity, 1.2, 1.0F, 0.5F));
        this.goalSelector.addGoal(6, new BounceGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        if (species == GirlSpecies.MIMIC_SLIME) {                         // a walking chest: she keeps close to her friend
            this.goalSelector.addGoal(4, new FollowEntityGoal(this, () -> this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24), 1.2, 4.0F, 2.0F));
        }
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        if (s == GirlSpecies.KLABUSTER_SLIME) return girlAttributes(16, 0.24, 1);
        if (s == GirlSpecies.ENDER_SLIME) return girlAttributes(20, 0.26, 3);
        return girlAttributes(s == GirlSpecies.MAGMA_SLIME ? 24 : 18, 0.24, s == GirlSpecies.MAGMA_SLIME ? 4 : 2);
    }

    /** Each species' favourite food (it also tempts her). */
    public static List<net.minecraft.world.item.Item> favourites(GirlSpecies s) {
        return switch (s) {
            case MAGMA_SLIME -> List.of(Items.MAGMA_CREAM, Items.BLAZE_POWDER, Items.COAL);
            case HONEY_SLIME -> List.of(Items.HONEY_BOTTLE, Items.HONEYCOMB, Items.SWEET_BERRIES);
            case FROST_SLIME -> List.of(Items.ICE, Items.SNOWBALL, Items.PACKED_ICE);
            case AMETHYST_SLIME -> List.of(Items.AMETHYST_SHARD, Items.QUARTZ);
            case MIMIC_SLIME -> List.of(Items.GOLD_INGOT, Items.GOLD_NUGGET, Items.GOLD_BLOCK);
            case ENDER_SLIME -> List.of(Items.CHORUS_FRUIT, Items.POPPED_CHORUS_FRUIT, Items.ENDER_PEARL);
            case UNDINE_SLIME -> List.of(Items.POTION, Items.KELP, Items.DRIED_KELP);
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
            case ENDER_SLIME -> List.of(DyeColor.BLACK, DyeColor.MAGENTA, DyeColor.CYAN, DyeColor.YELLOW, DyeColor.PURPLE);
            case UNDINE_SLIME -> List.of(DyeColor.LIGHT_BLUE, DyeColor.CYAN, DyeColor.WHITE, DyeColor.BLUE, DyeColor.GREEN);
            case MIXED_SLIME, MIMIC_SLIME -> List.of();                     // her colours are her two halves; the mimic's, her chest
            default -> List.of(DyeColor.LIME, DyeColor.WHITE, DyeColor.BROWN, DyeColor.CYAN, DyeColor.YELLOW);
        };
    }

    @Override
    public boolean isFood(ItemStack stack) {
        if (this.isMixed()) return this.kinds().stream().anyMatch(k -> favourites(k).stream().anyMatch(stack::is));
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
        if (stack.is(Items.SLIME_BALL) && !this.isBaby() && !this.isHalf() && this.getAffection(player) >= BED_AFFECTION) {
            if (this.isMixed() && player.isShiftKeyDown()) {                // sneak: let the two of them go their own ways
                if (!client) this.separate((ServerLevel) this.level());
                return InteractionResult.sidedSuccess(client);
            }
            SlimeGirl other = this.isMixed() ? null : this.nearest(SlimeGirl.class, 3.0, o -> canMerge(this, o, player));
            if (other != null) {
                if (!client) merge((ServerLevel) this.level(), this, other, player);
                stack.consume(1, player);
                return InteractionResult.sidedSuccess(client);
            }
        }
        if (stack.is(Items.SLIME_BALL) && !this.isMixed() && !this.isBaby() && !this.isHalf()) {        // why not: say so
            SlimeGirl near = this.nearest(SlimeGirl.class, 3.0, o -> o != this && !o.isMixed() && o.species != this.species && !o.isBaby() && !o.isHalf());
            if (near != null) {
                if (!client) player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.merge_trust", this.getDisplayName(), near.getDisplayName()), true);
                return InteractionResult.sidedSuccess(client);
            }
        }
        if (stack.is(Items.POTION) && this.hasCustomColour()
                && stack.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS, net.minecraft.world.item.alchemy.PotionContents.EMPTY)
                .is(net.minecraft.world.item.alchemy.Potions.WATER)) {            // a water bottle washes the dye out of her
            if (!client) {
                this.setColour(-1);
                stack.consume(1, player);
                if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
                this.playSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.4F);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (stack.getItem() instanceof DyeItem dye) {                       // the dye's colour mixes into hers (any colour there is)
            if (!client) {
                this.setColour(blend(this.colour(), dye.getDyeColor().getTextureDiffuseColor()));
                stack.consume(1, player);
                this.startTimedAction(Action.ABSORB, 24);
                this.playSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.2F);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (this.species == GirlSpecies.MIMIC_SLIME && stack.isEmpty() && player.isShiftKeyDown()
                && this.getAffection(player) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION) {   // the mimic opens up for a friend
            if (!client) {
                player.openMenu(new SimpleMenuProvider((id, inv, p) -> ChestMenu.threeRows(id, inv, this.hoard), this.getDisplayName()));
                this.playSound(SoundEvents.CHEST_OPEN, 0.8F, 1.1F);
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
        for (int i = 0; i < this.hoard.getContainerSize(); i++) if (!this.hoard.getItem(i).isEmpty()) this.spawnAtLocation(this.hoard.getItem(i));
        if (this.carried != null) this.spawnAtLocation(this.carried.getBlock());       // the ender slime's block
    }

    // --- the mimic -----------------------------------------------------------------------------------------------

    public SimpleContainer getHoard() {
        return this.hoard;
    }

    /** Whether this unopened loot chest is a mimic: one in MIMIC_ODDS, fixed by the world seed so a kobold can smell it first. */
    public static boolean isMimicChest(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest && chest.getLootTable() != null
                && Math.floorMod(net.minecraft.util.Mth.getSeed(pos) ^ level.getSeed(), MIMIC_ODDS) == 0;
    }

    /**
     * The chest a player just tried to open was a mimic: only an unopened loot chest (one that still has its loot
     * table), never a chest anyone placed. She takes the chest's place and its loot, bites once and settles. Returns
     * the mimic, or null when the chest is a plain chest.
     */
    @Nullable
    public static SlimeGirl springMimic(ServerLevel level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) || chest.getLootTable() == null) return null;
        if (!(com.linkyoshi90.monstergirls.registry.ModGirls.type(GirlSpecies.MIMIC_SLIME).create(level) instanceof SlimeGirl mimic)) return null;
        chest.unpackLootTable(player);
        for (int i = 0; i < chest.getContainerSize(); i++) mimic.hoard.addItem(chest.removeItemNoUpdate(i));
        level.removeBlock(pos, false);
        mimic.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot() + 180.0F, 0.0F);
        mimic.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), net.minecraft.world.entity.MobSpawnType.EVENT, null);
        mimic.setPersistenceRequired();
        level.addFreshEntity(mimic);
        mimic.getLookControl().setLookAt(player);
        mimic.startTimedAction(Action.BITE, 12);
        player.hurt(mimic.damageSources().mobAttack(mimic), 4.0F);       // one bite, then she settles
        level.playSound(null, pos, SoundEvents.CHEST_CLOSE, net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.6F);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.mimic"), true);
        return mimic;
    }

    // --- splitting -----------------------------------------------------------------------------------------------

    /** A killing blow splits her in two small halves (once a day) that find each other again and merge. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && amount >= this.getHealth() && this.isMixed() && !this.parts.isEmpty()
                && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.separate((ServerLevel) this.level());                       // never lost: she comes apart into the two again
            return false;
        }
        if (!this.level().isClientSide && amount >= this.getHealth() && !this.hasFlag(FLAG_HALF) && !this.isBaby()
                && this.level().getGameTime() - this.lastSplit >= SPLIT_COOLDOWN && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.split((ServerLevel) this.level());
            if (source.getEntity() instanceof Player p) this.onHurtByPlayer(p);
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.species == GirlSpecies.ENDER_SLIME && this.isAlive() && this.level() instanceof ServerLevel server) this.blink(server);
        return hurt;
    }

    /** The ender slime flickers away: somewhere within 16 blocks she can stand. */
    public boolean blink(ServerLevel level) {
        for (int i = 0; i < 16; i++) {
            double x = this.getX() + (this.random.nextDouble() - 0.5) * 32, z = this.getZ() + (this.random.nextDouble() - 0.5) * 32;
            double y = Math.min(level.getMaxBuildHeight() - 1, this.getY() + this.random.nextInt(16) - 8);
            net.minecraft.world.phys.Vec3 from = this.position();
            if (this.randomTeleport(x, y, z, true)) {
                level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.TELEPORT, from, net.minecraft.world.level.gameevent.GameEvent.Context.of(this));
                level.playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.2F);
                this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.2F);
                return true;
            }
        }
        return false;
    }

    /** While she is wild she moves the world around: up with a loose block, and down with it again somewhere else. */
    private void tickEnder(ServerLevel level) {
        if (this.isInWaterRainOrBubble() && this.tickCount % 20 == 0) {             // water stings her, and she flees it
            this.hurt(this.damageSources().drown(), 1.0F);
            return;
        }
        if (this.tickCount % 20 != 0 || level.getGameTime() < this.nextMove || this.isBaby()
                || !net.neoforged.neoforge.event.EventHooks.canEntityGrief(level, this)) return;
        this.nextMove = level.getGameTime() + 600 + this.random.nextInt(1200);
        BlockPos o = this.blockPosition();
        if (this.carried == null) {
            if (this.bestAffection() >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION) return;      // a friend's slime leaves things be
            BlockPos p = o.offset(this.random.nextInt(5) - 2, this.random.nextInt(3) - 1, this.random.nextInt(5) - 2);
            BlockState s = level.getBlockState(p);
            if (!s.is(net.minecraft.tags.BlockTags.ENDERMAN_HOLDABLE)) return;
            level.removeBlock(p, false);
            this.setCarried(s);
        } else {
            for (int i = 0; i < 8; i++) {
                BlockPos p = o.offset(this.random.nextInt(9) - 4, this.random.nextInt(3) - 1, this.random.nextInt(9) - 4);
                if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isCollisionShapeFullBlock(level, p.below())
                        && this.carried.canSurvive(level, p)) {
                    level.setBlock(p, this.carried, 3);
                    this.setCarried(null);
                    return;
                }
            }
        }
    }

    @Nullable
    public BlockState getCarried() {
        return this.carried;
    }

    /** What she holds shows in her hand. */
    public void setCarried(@Nullable BlockState state) {
        this.carried = state;
        this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, state == null ? ItemStack.EMPTY : new ItemStack(state.getBlock()));
        this.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0F);
    }

    private void split(ServerLevel level) {
        this.lastSplit = level.getGameTime();
        for (ItemStack s : this.belly.removeAllItems()) this.spawnAtLocation(s);   // the blow knocks everything out of her
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
        if (this.species == GirlSpecies.KLABUSTER_SLIME && this.level() instanceof ServerLevel drip && !this.isBaby() && this.tickCount % 40 == 0) this.drip(drip);
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
            case ENDER_SLIME -> this.tickEnder(server);
            case UNDINE_SLIME -> {
                if (this.tickCount % 100 == 0 && !this.isBaby()) this.water(server);
            }
            default -> {
            }
        }
    }

    /**
     * The undine, befriended: the farmland within 6 blocks of her stays wet, the cauldrons fill, and her friend in the
     * water breathes it. Returns how many blocks she watered or filled.
     */
    public int water(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        if (friend == null) return 0;
        if (friend.isInWater() && friend.distanceToSqr(this) < 8 * 8) {
            friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WATER_BREATHING, 200, 0, true, false));
        }
        int n = 0;
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(this.blockPosition().offset(-6, -2, -6), this.blockPosition().offset(6, 2, 6))) {
            var s = level.getBlockState(p);
            if (s.is(net.minecraft.world.level.block.Blocks.FARMLAND) && s.getValue(net.minecraft.world.level.block.FarmBlock.MOISTURE) < 7) {
                level.setBlock(p, s.setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 7), 2);
                n++;
            } else if (s.is(net.minecraft.world.level.block.Blocks.CAULDRON) || s.is(net.minecraft.world.level.block.Blocks.WATER_CAULDRON)
                    && s.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL) < 3) {
                level.setBlock(p, net.minecraft.world.level.block.Blocks.WATER_CAULDRON.defaultBlockState().setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3), 3);
                n++;
            }
        }
        if (n > 0) level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 1, this.getZ(), 12, 1.0, 0.5, 1.0, 0.1);
        return n;
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
            case MIMIC_SLIME -> new ItemStack(r < 70 ? Items.GOLD_NUGGET : Items.EMERALD, r < 70 ? 4 : 1);
            case UNDINE_SLIME -> new ItemStack(r < 45 ? Items.KELP : r < 80 ? Items.PRISMARINE_SHARD : r < 97 ? Items.NAUTILUS_SHELL : Items.HEART_OF_THE_SEA,
                    r < 80 ? 3 : 1);
            case ENDER_SLIME -> new ItemStack(r < 55 ? Items.CHORUS_FRUIT : r < 85 ? Items.END_STONE : Items.ENDER_PEARL, r < 85 ? 3 : 1);
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
        if (this.hasCustomColour()) tag.putInt("Colour", this.colour());
        tag.put("Hoard", this.hoard.createTag(this.registryAccess()));
        if (!this.parts.isEmpty()) {
            net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
            list.addAll(this.parts);
            tag.put("Parts", list);
        }
        tag.put("Belly", this.belly.createTag(this.registryAccess()));
        tag.putLong("LastSplit", this.lastSplit);
        if (this.carried != null) tag.put("Carried", net.minecraft.nbt.NbtUtils.writeBlockState(this.carried));
        if (this.partner != null) tag.putUUID("Partner", this.partner);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setColour(tag.contains("Colour") ? tag.getInt("Colour") : -1);
        this.parts.clear();
        for (var t : tag.getList("Parts", net.minecraft.nbt.Tag.TAG_COMPOUND)) this.parts.add((CompoundTag) t);
        this.belly.fromTag(tag.getList("Belly", 10), this.registryAccess());
        this.hoard.fromTag(tag.getList("Hoard", 10), this.registryAccess());
        if (tag.contains("LastSplit")) this.lastSplit = tag.getLong("LastSplit");
        if (tag.contains("Carried")) this.setCarried(net.minecraft.nbt.NbtUtils.readBlockState(this.level().holderLookup(net.minecraft.core.registries.Registries.BLOCK),
                tag.getCompound("Carried")));
        this.partner = tag.hasUUID("Partner") ? tag.getUUID("Partner") : null;
    }

    // --- mixed slimes ---------------------------------------------------------------------------------------------

    private static final List<GirlSpecies> KINDS = List.of(GirlSpecies.VERDANT_SLIME, GirlSpecies.MAGMA_SLIME, GirlSpecies.HONEY_SLIME,
            GirlSpecies.FROST_SLIME, GirlSpecies.AMETHYST_SLIME);
    /** The two girls she is made of (their saved data), to give back when she separates. */
    private final List<CompoundTag> parts = new java.util.ArrayList<>();

    public boolean isMixed() {
        return this.species == GirlSpecies.MIXED_SLIME;
    }

    /** The two kinds a mixed slime is made of (read off her colour variant, e.g. "honey_frost"). */
    public List<GirlSpecies> kinds() {
        if (!this.isMixed()) return List.of(this.species);
        String[] k = this.getVariantName().split("_");
        return List.of(GirlSpecies.byId(k[0] + "_slime"), GirlSpecies.byId(k[1] + "_slime"));
    }

    public int partCount() {
        return this.parts.size();
    }

    /** Two grown, whole slimes of different kinds, both at high friendship with the same player. */
    public static boolean canMerge(SlimeGirl a, SlimeGirl b, Player player) {
        return a != b && b.isAlive() && !a.isMixed() && !b.isMixed() && a.species != b.species && !a.isBaby() && !b.isBaby()
                && !a.isHalf() && !b.isHalf() && b.getAffection(player) >= BED_AFFECTION && a.getAffection(player) >= BED_AFFECTION;
    }

    private static CompoundTag saved(SlimeGirl girl) {
        CompoundTag tag = new CompoundTag();
        girl.saveWithoutId(tag);
        tag.remove("UUID");
        tag.putString("PartSpecies", girl.species.id());
        return tag;
    }

    /** The two flow together into one marbled slime that carries both of them (bonds, belongings and all). */
    public static SlimeGirl merge(ServerLevel level, SlimeGirl a, SlimeGirl b, Player player) {
        if (!(com.linkyoshi90.monstergirls.registry.ModGirls.type(GirlSpecies.MIXED_SLIME).create(level) instanceof SlimeGirl mixed)) return null;
        int ia = KINDS.indexOf(a.species), ib = KINDS.indexOf(b.species);
        String key = KINDS.get(Math.min(ia, ib)).id().replace("_slime", "") + "_" + KINDS.get(Math.max(ia, ib)).id().replace("_slime", "");
        mixed.setVariant(key);
        mixed.setColour(blend(a.colour(), b.colour()));                       // the two colours flow together
        mixed.parts.add(saved(a));
        mixed.parts.add(saved(b));
        mixed.setAffection(player.getUUID(), Math.max(a.getAffection(player), b.getAffection(player)));
        mixed.moveTo((a.getX() + b.getX()) / 2, a.getY(), (a.getZ() + b.getZ()) / 2, a.getYRot(), 0.0F);
        mixed.setPersistenceRequired();
        level.addFreshEntity(mixed);
        level.sendParticles(ParticleTypes.ITEM_SLIME, mixed.getX(), mixed.getY() + 1, mixed.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
        mixed.playSound(SoundEvents.SLIME_SQUISH, 1.0F, 0.7F);
        a.discard();
        b.discard();
        return mixed;
    }

    /** She comes apart into the two she was (each a little worn), with whatever she was carrying at her feet. */
    public void separate(ServerLevel level) {
        if (this.parts.isEmpty()) return;
        for (CompoundTag tag : this.parts) {
            GirlSpecies s = GirlSpecies.byId(tag.getString("PartSpecies"));
            net.minecraft.world.entity.Entity e = s == null ? null : com.linkyoshi90.monstergirls.registry.ModGirls.type(s).create(level);
            if (!(e instanceof SlimeGirl girl)) continue;
            girl.load(tag);
            girl.moveTo(this.getX() + this.random.nextGaussian() * 0.6, this.getY(), this.getZ() + this.random.nextGaussian() * 0.6, this.getYRot(), 0.0F);
            girl.setHealth(Math.max(1.0F, girl.getMaxHealth() * 0.5F));
            level.addFreshEntity(girl);
        }
        this.parts.clear();
        for (ItemStack s : this.belly.removeAllItems()) this.spawnAtLocation(s);
        level.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 1, this.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
        this.playSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.3F);
        this.discard();
    }

    // --- colour: anywhere on the RGB spectrum ---------------------------------------------------------------------

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_COLOUR =
            net.minecraft.network.syncher.SynchedEntityData.defineId(SlimeGirl.class, net.minecraft.network.syncher.EntityDataSerializers.INT);

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COLOUR, -1);
    }

    /** Whether dye (or a merge) has given her a colour of her own (she is then drawn tinted, see GirlRenderer). */
    public boolean hasCustomColour() {
        return this.entityData.get(DATA_COLOUR) >= 0;
    }

    /** Her colour as 0xRRGGBB: her own, or her kind's natural one. */
    public int colour() {
        int c = this.entityData.get(DATA_COLOUR);
        return c >= 0 ? c : this.species.eggColor() & 0xFFFFFF;
    }

    /** -1 goes back to her natural colours. */
    public void setColour(int rgb) {
        this.entityData.set(DATA_COLOUR, rgb < 0 ? -1 : rgb & 0xFFFFFF);
    }

    /** RGB to hue, saturation, value (all 0..1). */
    static float[] hsv(int rgb) {
        float r = (rgb >> 16 & 255) / 255.0F, g = (rgb >> 8 & 255) / 255.0F, b = (rgb & 255) / 255.0F;
        float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b)), d = max - min;
        float h = d == 0 ? 0 : max == r ? ((g - b) / d) % 6 : max == g ? (b - r) / d + 2 : (r - g) / d + 4;
        h /= 6.0F;
        if (h < 0) h += 1.0F;
        return new float[]{h, max == 0 ? 0 : d / max, max};
    }

    /**
     * Two colours flowing together, like mixing light without washing out: the hue is the average round the colour
     * wheel (red and blue make magenta, red and green yellow), the brightness is the brighter one's, and most of the
     * saturation is kept. It only fades towards white or grey when the two pull in opposite directions (complementary
     * colours) or one of them was pale already, and even then only part of the way, so repeated merges never end up
     * plain white.
     */
    public static int blend(int a, int b) {
        float[] x = hsv(a), y = hsv(b);
        double ax = Math.cos(x[0] * 2 * Math.PI) * x[1], ay = Math.sin(x[0] * 2 * Math.PI) * x[1];
        double bx = Math.cos(y[0] * 2 * Math.PI) * y[1], by = Math.sin(y[0] * 2 * Math.PI) * y[1];
        double mx = (ax + bx) / 2, my = (ay + by) / 2, pull = Math.hypot(mx, my);
        float hue = pull < 1.0E-4 ? x[0] : (float) (Math.atan2(my, mx) / (2 * Math.PI));
        if (hue < 0) hue += 1.0F;
        float maxSat = Math.max(x[1], y[1]);
        double meanSat = (x[1] + y[1]) / 2;
        float sat = meanSat < 1.0E-4 ? 0.0F : (float) (maxSat * (0.6 + 0.4 * Math.min(1.0, pull / meanSat)) * Math.sqrt(meanSat / maxSat));
        return net.minecraft.util.Mth.hsvToRgb(hue, Math.min(1.0F, sat), Math.max(x[2], y[2])) & 0xFFFFFF;
    }

    @Override
    public boolean isHealer() {
        return this.species == GirlSpecies.VERDANT_SLIME || this.species == GirlSpecies.HONEY_SLIME;
    }

    // --- the klabuster -------------------------------------------------------------------------------------------

    /** Within 6 blocks of her friend she puts out the fires and fills the cauldrons with water. How many blocks she changed, or -1 without a friend. */
    public int drip(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        int n = 0;
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(friend.blockPosition().offset(-6, -3, -6), friend.blockPosition().offset(6, 3, 6))) {
            var st = level.getBlockState(p);
            if (st.is(net.minecraft.tags.BlockTags.FIRE)) {
                level.removeBlock(p, false);
                level.levelEvent(1009, p, 0);                                      // the fizz
                n++;
            } else if (st.is(net.minecraft.world.level.block.Blocks.CAULDRON) || st.is(net.minecraft.world.level.block.Blocks.WATER_CAULDRON)
                    && st.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL) < 3) {
                level.setBlockAndUpdate(p, net.minecraft.world.level.block.Blocks.WATER_CAULDRON.defaultBlockState().setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3));
                level.playSound(null, p, net.minecraft.sounds.SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                n++;
            }
        }
        return n;
    }
}
