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
import net.minecraft.world.entity.EquipmentSlot;
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
 * The sea family (merfolk_field_guide.html): orca, koi, shark, seal, axolotl, dolphin, dugong. Half the world is water: fast
 * swimmers who crawl slowly on land and dry out there; they sing at dusk, lead their favourite to treasure, share
 * their breath, hate drowned, and trade sea treasure for shiny things from the land. Each is true to her kind: the
 * shark attacks anything that swims (players too, until fed), the orca and the dolphin tease swimmers and sea life.
 * Now and then any of them finds seashells on the sea floor and wears them. Orca, dolphin, shark and seal bear live
 * young; koi and axolotl lay roe.
 */
public class Merfolk extends AbstractMonsterGirl implements net.minecraft.world.item.trading.Merchant, net.minecraft.world.entity.animal.Bucketable {
    private int dryTicks;
    private long nextTrade;
    private long nextTreasureSearch;
    @Nullable
    private BlockPos treasure;
    private int playDead;
    private long nextShellHunt;
    /** One chance in this many, every couple of seconds over sand or gravel, that she starts rummaging for shells. */
    private int shellChance = 120;

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
            this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, com.linkyoshi90.monstergirls.entity.PenguinHarpy.class, 10, true, false,
                    p -> !((com.linkyoshi90.monstergirls.entity.PenguinHarpy) p).isBefriended()));   // orcas hunt penguins nobody has befriended
            this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.LivingEntity.class, 10, true, false,
                    e -> com.linkyoshi90.monstergirls.compat.AlexsMobs.is(e, "seal")));   // and Alex's Mobs seals
        }
        if (species == GirlSpecies.SHARK_MERMAID) {                   // anything that swims is prey (blood draws her from further)
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, this::isSharkPrey));
        }
        if (species == GirlSpecies.ORCA_MERMAID || species == GirlSpecies.DOLPHIN_MERMAID) this.goalSelector.addGoal(5, new TeaseGoal());
        if (species != GirlSpecies.AXOLOTL_MERMAID) this.goalSelector.addGoal(6, new ShellHuntGoal());
        this.goalSelector.addGoal(4, new FetchGoal());
        this.goalSelector.addGoal(2, new SurfaceGoal());
        if (species == GirlSpecies.AHUIZOTL_MERMAID) {                // a lit lantern in hand keeps a wild ahuizotl away
            this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.AvoidEntityGoal<>(this, Player.class, 8.0F, 1.1, 1.4,
                    e -> e instanceof Player p && holdsLantern(p) && this.getAffection(p) < com.linkyoshi90.monstergirls.entity.Orders.AFFECTION));
        }
    }

    // --- the ahuizotl ----------------------------------------------------------------------------------------------

    public static boolean holdsLantern(Player p) {
        return p.getMainHandItem().is(Items.LANTERN) || p.getOffhandItem().is(Items.LANTERN) || p.getMainHandItem().is(Items.SOUL_LANTERN)
                || p.getOffhandItem().is(Items.SOUL_LANTERN);
    }

    /** A wild ahuizotl near a fishing hook, ready to snatch the catch (none comes near a lantern, nor steals from a friend). */
    @Nullable
    public static Merfolk thiefNear(net.minecraft.world.entity.Entity hook, Player fisher) {
        if (holdsLantern(fisher)) return null;
        for (Merfolk m : hook.level().getEntitiesOfClass(Merfolk.class, hook.getBoundingBox().inflate(12),
                m -> m.isAlive() && !m.isBaby() && m.species == GirlSpecies.AHUIZOTL_MERMAID
                        && m.getAffection(fisher) < com.linkyoshi90.monstergirls.entity.Orders.AFFECTION)) {
            return m;
        }
        return null;
    }

    /** The hand on her tail takes the fish off your line. */
    public void stealCatch(ServerLevel level, Player fisher) {
        this.heal(4.0F);
        this.startTimedAction(Action.LAUGH, 20);
        this.playSound(SoundEvents.FOX_SNIFF, 1.0F, 1.4F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH, this.getX(), this.getY() + 0.5, this.getZ(), 12, 0.4, 0.2, 0.4, 0.1);
        fisher.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.ahuizotl_steal", this.getDisplayName()), true);
    }

    private long nextCatch;

    /** Befriended, she fishes for her friend now and then (in the water, near them). */
    private void fishForFriend(ServerLevel level) {
        if (!this.isInWater() || level.getGameTime() < this.nextCatch) return;
        Player p = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (p == null) return;
        this.nextCatch = level.getGameTime() + 1800 + this.random.nextInt(1800);
        this.spawnAtLocation(new ItemStack(this.random.nextInt(3) == 0 ? Items.SALMON : Items.COD));
        this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.8F, 1.2F);
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case SAWFIN_MERMAID -> girlAttributes(24, 0.25, 5);
            case ORCA_MERMAID -> girlAttributes(36, 0.25, 6);
            case SHARK_MERMAID -> girlAttributes(30, 0.25, 7);
            case SEAL_MERMAID -> girlAttributes(22, 0.22, 2);
            case AXOLOTL_MERMAID -> girlAttributes(18, 0.22, 3);
            case DOLPHIN_MERMAID -> girlAttributes(24, 0.3, 3);
            case DUGONG_MERMAID -> girlAttributes(28, 0.2, 2);
            case AHUIZOTL_MERMAID -> girlAttributes(22, 0.28, 3);
            case SIREN_MERMAID -> girlAttributes(26, 0.26, 4);
            case AXOLUME_MERMAID -> girlAttributes(18, 0.24, 2);
            case MANTARROW_MERMAID -> girlAttributes(24, 0.28, 2);
            case PLATYPUS_MERMAID -> girlAttributes(16, 0.26, 2);
            case MOONSEAL_MERMAID -> girlAttributes(28, 0.24, 2);
            case RUST_OTTER_MERMAID -> girlAttributes(20, 0.27, 3);
            case REEFFIN_MERMAID -> girlAttributes(22, 0.26, 2);
            case SWORD_KOI_MERMAID -> girlAttributes(24, 0.28, 6);
            default -> girlAttributes(20, 0.24, 2);
        };
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new com.linkyoshi90.monstergirls.entity.ai.SwimmerNavigation(this, level);
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
        if (this.isInWater() || this.isLevitating()) return s;
        boolean landy = this.species == GirlSpecies.SEAL_MERMAID || this.species == GirlSpecies.AXOLOTL_MERMAID;
        return s * (landy ? 0.55F : 0.33F);
    }

    // --- the shark -----------------------------------------------------------------------------------------------

    /** A wild shark attacks swimmers on sight; fish or meat calms her (see {@link AbstractMonsterGirl#calmDown}). */
    @Override
    protected boolean wildHostile() {
        return this.species == GirlSpecies.SHARK_MERMAID;
    }

    /** She only goes for players who are in the water with her. A merchant never does: she is here to trade. */
    @Override
    public boolean isHostileTo(Player player) {
        return super.isHostileTo(player) && player.isInWater() && !this.isMerchant();
    }

    /** Anything in the water: fish, squid, turtles, drowned, other swimmers. Never her own kind, never a bonded girl. */
    private boolean isSharkPrey(LivingEntity e) {
        if (!e.isInWater() || e instanceof Merfolk || e instanceof Player) return false;
        if (e instanceof AbstractMonsterGirl girl && (girl.hasAnyAffection() || girl.isBaby())) return false;
        if (e instanceof net.minecraft.world.entity.TamableAnimal t && t.isTame()) return false;
        if (e instanceof net.minecraft.world.entity.animal.Dolphin) return false;   // they mob sharks
        return this.getHealth() < this.getMaxHealth() || e.getHealth() < e.getMaxHealth() || this.distanceToSqr(e) < 12 * 12;
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
        if (this.species == GirlSpecies.DUGONG_MERMAID) return stack.is(Items.SEAGRASS) || stack.is(Items.KELP);   // a grazer: no fish
        return stack.is(ItemTags.FISHES) || ((this.species == GirlSpecies.KOI_MERMAID || this.species == GirlSpecies.AXOLOTL_MERMAID)
                && (stack.is(Items.KELP) || stack.is(Items.DRIED_KELP) || stack.is(Items.BREAD)));
    }

    /** Seagrass is what wins a dugong over. */
    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return this.species == GirlSpecies.DUGONG_MERMAID && stack.is(Items.SEAGRASS) ? 3 : super.foodValue(player, stack);
    }

    private static boolean shiny(ItemStack s) {
        return s.is(Items.GOLD_INGOT) || s.is(Items.COPPER_INGOT) || s.is(Items.IRON_INGOT) || s.is(Items.AMETHYST_SHARD) || s.is(Items.EMERALD) || s.is(Items.GOLD_NUGGET);
    }

    /** A shiny thing from the land, traded for a treasure of the sea. */
    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (this.isManta() && stack.isEmpty() && !player.isShiftKeyDown() && !this.isBaby() && !this.isVehicle()
                && this.getAffection(player) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION) {   // a friend climbs on
            if (!this.level().isClientSide) player.startRiding(this);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.isBaby() && stack.is(Items.WATER_BUCKET)) {                 // the young fit in a bucket
            return net.minecraft.world.entity.animal.Bucketable.bucketMobPickup(player, hand, this).orElse(null);
        }
        if (this.isMerchant() && stack.isEmpty() && !player.isShiftKeyDown() && this.tradingPlayer == null && !this.isBaby()) {
            if (!this.level().isClientSide) {
                this.setTradingPlayer(player);
                this.openTradingScreen(player, net.minecraft.network.chat.Component.translatable("entity.monstergirls.merfolk_merchant", this.getDisplayName()), 1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
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
            case REEFFIN_MERMAID -> new ItemStack(this.random.nextInt(20) == 0 ? Items.NAUTILUS_SHELL : this.random.nextBoolean() ? Items.SEA_PICKLE
                    : this.random.nextBoolean() ? Items.TUBE_CORAL : Items.PRISMARINE_SHARD, 2);
            case RUST_OTTER_MERMAID -> new ItemStack(this.random.nextInt(3) == 0 ? Items.COPPER_INGOT : Items.RAW_COPPER, 2);
            case PLATYPUS_MERMAID -> new ItemStack(this.random.nextBoolean() ? Items.CLAY_BALL : Items.SALMON, 2);
            case MOONSEAL_MERMAID -> new ItemStack(this.random.nextInt(4) == 0 ? Items.PRISMARINE_CRYSTALS : this.random.nextBoolean() ? Items.COD : Items.SNOWBALL, 2);
            case DOLPHIN_MERMAID -> new ItemStack(this.random.nextInt(4) == 0 ? Items.NAUTILUS_SHELL : Items.TROPICAL_FISH);
            case AXOLOTL_MERMAID -> new ItemStack(this.random.nextBoolean() ? Items.TROPICAL_FISH : Items.CLAY_BALL);
            case AHUIZOTL_MERMAID -> new ItemStack(this.random.nextInt(4) == 0 ? Items.FISHING_ROD : this.random.nextBoolean() ? Items.SALMON : Items.COD,
                    this.random.nextInt(4) == 0 ? 1 : 2);
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
            if (level.getBlockEntity(p) instanceof GirlEggBlockEntity be) be.setup(this.species.id(), this.litterSize(), father).withPaint(this.getPaint());
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
        if (hurt && this.species == GirlSpecies.PLATYPUS_MERMAID && !this.isBaby() && source.getEntity() instanceof LivingEntity a && a.distanceToSqr(this) < 3 * 3
                && !(a instanceof Player p && this.getAffection(p) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION)) {
            a.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));     // her venomous spur
        }
        if (hurt && !this.isBaby() && this.random.nextInt(6) == 0) this.spawnAtLocation(new ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.MERFOLK_SCALE.get()));
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

    private static final java.util.List<net.minecraft.world.level.block.Block> REEF = java.util.List.of(
            net.minecraft.world.level.block.Blocks.TUBE_CORAL, net.minecraft.world.level.block.Blocks.BRAIN_CORAL, net.minecraft.world.level.block.Blocks.BUBBLE_CORAL, net.minecraft.world.level.block.Blocks.FIRE_CORAL, net.minecraft.world.level.block.Blocks.HORN_CORAL,
            net.minecraft.world.level.block.Blocks.TUBE_CORAL_FAN, net.minecraft.world.level.block.Blocks.BRAIN_CORAL_FAN, net.minecraft.world.level.block.Blocks.BUBBLE_CORAL_FAN, net.minecraft.world.level.block.Blocks.FIRE_CORAL_FAN, net.minecraft.world.level.block.Blocks.HORN_CORAL_FAN,
            net.minecraft.world.level.block.Blocks.SEA_PICKLE);

    /** The reef keeper: coral, a coral fan or a sea pickle on a bare bit of sea floor near her (water above sand or gravel). Where, or null. */
    @Nullable
    public net.minecraft.core.BlockPos tendReef(ServerLevel level) {
        if (!this.isInWater()) return null;
        java.util.List<net.minecraft.core.BlockPos> spots = new java.util.ArrayList<>();
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(this.blockPosition().offset(-4, -4, -4), this.blockPosition().offset(4, 1, 4))) {
            var below = level.getBlockState(p.below());
            if (level.getBlockState(p).is(net.minecraft.world.level.block.Blocks.WATER) && level.getFluidState(p).isSource() && level.getFluidState(p.above()).is(net.minecraft.tags.FluidTags.WATER)
                    && (below.is(net.minecraft.tags.BlockTags.SAND) || below.is(net.minecraft.world.level.block.Blocks.GRAVEL))) spots.add(p.immutable());
        }
        if (spots.isEmpty()) return null;
        net.minecraft.core.BlockPos at = spots.get(this.random.nextInt(spots.size()));
        var block = REEF.get(this.random.nextInt(REEF.size()));
        var state = block.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, true);
        level.setBlock(at, state, 3);
        level.levelEvent(2005, at, 0);
        return at;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.MOOPIE_MERMAID && this.level() instanceof ServerLevel shallows && !this.isBaby() && this.tickCount % 100 == 0) this.seaMilk(shallows);
        if (this.species == GirlSpecies.RUSALKA_MERMAID && this.level() instanceof ServerLevel river && !this.isBaby() && this.tickCount % 20 == 0) this.riverLuck(river);
        if (this.species == GirlSpecies.SAWFIN_MERMAID && this.level() instanceof ServerLevel sea && !this.isBaby() && this.tickCount % 20 == 0) this.sapperAid(sea);
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.playDead > 0 && --this.playDead == 0 && this.getAction() == Action.LYING) this.setAction(Action.NONE);
        if (this.tickCount % 10 == 0) this.breathe(server);
        if (this.species == GirlSpecies.AHUIZOTL_MERMAID && !this.isBaby() && this.tickCount % 100 == 0) this.fishForFriend(server);
        if (this.species == GirlSpecies.SIREN_MERMAID && !this.isBaby() && this.tickCount % 10 == 0) this.sirenSong(server);
        if (this.isManta() && !this.isBaby()) this.tickManta();
        if (this.species == GirlSpecies.PLATYPUS_MERMAID && !this.isBaby() && this.tickCount % 20 == 0) this.electrosense(server);
        if (this.species == GirlSpecies.RUST_OTTER_MERMAID && !this.isBaby() && this.tickCount % 1200 == 0) this.scrape(server);
        if (this.species == GirlSpecies.MOONSEAL_MERMAID && !this.isBaby() && this.tickCount % 40 == 0) this.moonSong(server);
        if (this.species == GirlSpecies.REEFFIN_MERMAID && !this.isBaby() && this.tickCount % 200 == 0) this.tendReef(server);
        if (this.species == GirlSpecies.MOONSEAL_MERMAID && this.tickCount % 20 == 0) this.glow(server, server.isDay() ? 0 : 9);   // her crescents light the night
        if (this.species == GirlSpecies.AXOLUME_MERMAID && !this.isBaby()) {        // she lights the water, and her friend heals beside her
            this.glow(server, 10);
            Player healed = this.tickCount % 40 == 0 ? this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 8) : null;
            if (healed != null) healed.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, false));
        }
        this.levitate();
        if (this.needsAir && this.isInWater()) this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.05, 0.0));   // up for air
        if (!this.isBaby() && this.random.nextInt(12000) == 0) this.spawnAtLocation(new ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.MERFOLK_SCALE.get()));
        boolean wet = this.isInWaterRainOrBubble() || (this.species == GirlSpecies.SEAL_MERMAID && this.onBeach());
        this.dryTicks = wet ? 0 : this.dryTicks + 1;
        if (this.dryTicks > this.dryLimit() && this.tickCount % 40 == 0) this.hurt(this.damageSources().dryOut(), 1.0F);
        if (this.tickCount % 40 != 0) return;
        Player p = this.bondedPlayer(BED_AFFECTION, 32);
        if (p == null) return;
        boolean full = this.getAffection(p) >= FULL_AFFECTION;
        if (full && p.isInWater() && p.distanceToSqr(this) < 8 * 8) {   // devoted: conduit power (below that she shares her breath)
            p.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 200, 0, true, false));
        }
        long day = server.getDayTime() % 24000;
        if (day > 12000 && day < 13800 && this.getAction() == Action.NONE && !this.isInWater()) this.startTimedAction(Action.SING, 120);
        if (this.getAction() == Action.SING && p.isInWater() && p.distanceToSqr(this) < 16 * 16) {
            p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100, 0, true, false));
        }
        if (this.species == GirlSpecies.KOI_MERMAID && this.isInWater()) p.addEffect(new MobEffectInstance(MobEffects.LUCK, 200, 0, true, false));
        if (this.species == GirlSpecies.DOLPHIN_MERMAID && this.isInWater() && p.isInWater() && p.distanceToSqr(this) < 10 * 10) {
            p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100, 0, true, false));
        }
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

    // --- the rust otter ------------------------------------------------------------------------------------------------

    /** The most weathered copper block within 6 blocks of her friend loses a stage of its green. True if she scraped one. */
    public boolean scrape(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        if (friend == null) return false;
        net.minecraft.core.BlockPos best = null;
        int age = 0;
        for (net.minecraft.core.BlockPos p : net.minecraft.core.BlockPos.betweenClosed(friend.blockPosition().offset(-6, -3, -6), friend.blockPosition().offset(6, 3, 6))) {
            var s = level.getBlockState(p);
            if (s.getBlock() instanceof net.minecraft.world.level.block.WeatheringCopper w && w.getAge().ordinal() > age) {
                age = w.getAge().ordinal();
                best = p.immutable();
            }
        }
        if (best == null) return false;
        var prev = net.minecraft.world.level.block.WeatheringCopper.getPrevious(level.getBlockState(best));
        if (prev.isEmpty()) return false;
        level.setBlock(best, prev.get(), 3);
        level.levelEvent(3005, best, 0);                                     // the scraping particles
        this.playSound(SoundEvents.AXE_SCRAPE, 1.0F, 1.2F);
        return true;
    }

    // --- the platypus and the moonseal --------------------------------------------------------------------------------

    /** In the water, she senses every monster within 16 blocks of her friend and makes it glow. Returns how many. */
    public int electrosense(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        if (friend == null || !this.isInWater()) return 0;
        int n = 0;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(16))) {
            m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false));
            n++;
        }
        return n;
    }

    /** Dolphin's Grace for her friend swimming beside her; on a full-moon night she sings and they regenerate. */
    public boolean moonSong(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null) return false;
        if (friend.isInWater() && friend.distanceToSqr(this) < 8 * 8) friend.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, true, false));
        if (level.isDay() || level.getMoonPhase() != 0) return false;
        friend.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
        if (this.getAction() == Action.NONE) this.startTimedAction(Action.SING, 40);
        return true;
    }

    // --- the mantarrow: a ride across the sea ---------------------------------------------------------------------------

    public boolean isManta() {
        return this.species == GirlSpecies.MANTARROW_MERMAID;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.isManta() && this.getFirstPassenger() instanceof Player p ? p : super.getControllingPassenger();
    }

    @Override
    protected void tickRidden(Player player, Vec3 travel) {
        super.tickRidden(player, travel);
        this.setRot(player.getYRot(), player.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        if (this.isInWater()) {                                         // faster than a boat; she keeps her rider at the surface
            float speed = player.zza > 0 ? (player.isSprinting() ? 0.6F : 0.48F) : 0.0F;
            Vec3 fwd = Vec3.directionFromRotation(0, this.getYRot()).scale(speed);
            double up = this.getFluidHeight(net.minecraft.tags.FluidTags.WATER) > 0.9 ? 0.08 : this.horizontalCollision ? 0.4 : 0.0;
            this.setDeltaMovement(fwd.x, Math.max(this.getDeltaMovement().y, up), fwd.z);
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, true, false));
        }
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travel) {
        return new Vec3(player.xxa * 0.5F, 0.0, player.zza > 0 ? player.zza : player.zza * 0.25F);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return 0.2F;
    }

    @Override
    public boolean canBeRiddenUnderFluidType(net.neoforged.neoforge.fluids.FluidType type, net.minecraft.world.entity.Entity rider) {
        return this.isManta() || super.canBeRiddenUnderFluidType(type, rider);
    }

    /** Now and then she leaps clear of the waves. */
    private void tickManta() {
        if (this.isVehicle() || !this.isInWater() || this.tickCount % 100 != 0 || this.random.nextInt(3) != 0) return;
        if (this.getFluidHeight(net.minecraft.tags.FluidTags.WATER) > 1.5) return;
        Vec3 fwd = Vec3.directionFromRotation(0, this.getYRot()).scale(0.6);
        this.setDeltaMovement(fwd.x, 0.9, fwd.z);
        this.playSound(SoundEvents.DOLPHIN_JUMP, 1.0F, 0.8F);
    }

    private long nextSongMessage;

    /**
     * The siren sings whenever someone is near: strangers within 24 blocks are drawn toward her, step by step; friends
     * swimming within 16 get Conduit Power.
     */
    public void sirenSong(ServerLevel level) {
        boolean sang = false;
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(24), p -> !p.isSpectator() && !p.isCreative())) {
            boolean friend = this.getAffection(p) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION;
            if (friend) {
                if (!p.isInWater() || p.distanceToSqr(this) > 16 * 16) continue;
                p.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 220, 0, true, true));
            } else if (p.distanceToSqr(this) > 2.5 * 2.5) {
                Vec3 pull = this.position().subtract(p.position()).normalize().scale(0.08);
                p.setDeltaMovement(p.getDeltaMovement().add(pull.x, 0, pull.z));
                p.hurtMarked = true;                                             // send the nudge to the client
                if (level.getGameTime() >= this.nextSongMessage) {
                    this.nextSongMessage = level.getGameTime() + 300;
                    p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.siren_song", this.getDisplayName()), true);
                }
            }
            sang = true;
        }
        if (!sang) return;
        if (this.getAction() == Action.NONE) this.startTimedAction(Action.SING, 40);
        level.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + this.getBbHeight() + 0.3, this.getZ(), 1, 0.4, 0.2, 0.4, this.random.nextDouble());
    }

    /** After an axolotl fights beside her player, the player regenerates (like a vanilla axolotl). */
    public static void onKill(Merfolk killer, LivingEntity victim) {
        if (killer.species != GirlSpecies.AXOLOTL_MERMAID) return;
        Player p = killer.bondedPlayer(BED_AFFECTION, 20);
        if (p != null) p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
    }

    // --- play and shells -----------------------------------------------------------------------------------------

    public boolean hasShells() {
        return this.hasFlag(FLAG_SHELLS);
    }

    public void setShellChance(int chance) {
        this.shellChance = Math.max(1, chance);
    }

    /** Whether she can rummage here: in water, right above sand or gravel. */
    private boolean onSandyBottom() {
        if (!this.isInWater()) return false;
        for (int dy = 1; dy <= 2; dy++) {
            var s = this.level().getBlockState(this.blockPosition().below(dy));
            if (s.is(BlockTags.SAND) || s.is(net.minecraft.world.level.block.Blocks.GRAVEL)) return true;
            if (!s.getFluidState().is(FluidTags.WATER)) return false;
        }
        return false;
    }

    /**
     * Now and then a mermaid without shells sinks to a sandy or gravelly bottom, sifts it for a while and comes up with a
     * matching pair of seashells, which she wears over her chest from then on.
     */
    class ShellHuntGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private int ticks;

        ShellHuntGoal() {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Merfolk m = Merfolk.this;
            if (m.hasShells() || m.isBaby() || m.getTarget() != null || m.tickCount % 37 != 0 || !m.onSandyBottom()) return false;
            return m.level().getGameTime() >= m.nextShellHunt && m.random.nextInt(m.shellChance) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks > 0 && Merfolk.this.getTarget() == null && Merfolk.this.isInWater();
        }

        @Override
        public void start() {
            this.ticks = 100;
            Merfolk.this.getNavigation().stop();
            Merfolk.this.startTimedAction(Action.DIG, 100);
        }

        @Override
        public void tick() {
            Merfolk m = Merfolk.this;
            m.setDeltaMovement(m.getDeltaMovement().add(0, -0.01, 0));
            if (m.level() instanceof ServerLevel server && this.ticks % 10 == 0) {
                server.sendParticles(ParticleTypes.BUBBLE, m.getX(), m.getY(), m.getZ(), 4, 0.4, 0.1, 0.4, 0.02);
            }
            if (--this.ticks == 0) {
                m.nextShellHunt = m.level().getGameTime() + 6000;
                m.setFlag(FLAG_SHELLS, true);
                m.playSound(SoundEvents.BRUSH_SAND_COMPLETED, 1.0F, 1.2F);
                if (m.level() instanceof ServerLevel server) m.hearts(server, 2);
            }
        }
    }

    /**
     * The orca and the dolphin play with whatever swims: she darts up to a swimmer, a boat or a fish, gives it a playful
     * shove (players get a splash and a little push, squid and fish are bumped up out of the water), laughs and darts off.
     * Nobody gets hurt.
     */
    class TeaseGoal extends net.minecraft.world.entity.ai.goal.Goal {
        @Nullable
        private net.minecraft.world.entity.Entity mark;
        private int ticks;
        private long nextTease;

        TeaseGoal() {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Merfolk m = Merfolk.this;
            if (m.isBaby() || m.getTarget() != null || !m.isInWater() || m.tickCount % 29 != 0 || m.level().getGameTime() < this.nextTease) return false;
            if (m.random.nextInt(3) != 0) return false;
            this.mark = m.nearest(net.minecraft.world.entity.Entity.class, 14, e -> e.isAlive() && e.isInWater() && isPlaymate(e));
            return this.mark != null;
        }

        private boolean isPlaymate(net.minecraft.world.entity.Entity e) {
            if (e instanceof Player p) return !p.isSpectator() && !p.isCreative();
            if (e instanceof Boat) return true;
            return e instanceof net.minecraft.world.entity.animal.WaterAnimal && !(e instanceof Merfolk)
                    || e instanceof net.minecraft.world.entity.animal.Turtle || e instanceof net.minecraft.world.entity.animal.axolotl.Axolotl;
        }

        @Override
        public boolean canContinueToUse() {
            return this.mark != null && this.mark.isAlive() && this.ticks < 160 && Merfolk.this.getTarget() == null;
        }

        @Override
        public void start() {
            this.ticks = 0;
        }

        @Override
        public void stop() {
            this.mark = null;
            this.nextTease = Merfolk.this.level().getGameTime() + 400 + Merfolk.this.random.nextInt(600);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            Merfolk m = Merfolk.this;
            this.ticks++;
            m.getLookControl().setLookAt(this.mark, 30.0F, 30.0F);
            if (m.distanceToSqr(this.mark) > 2.5 * 2.5) {
                if (this.ticks % 5 == 1) m.getNavigation().moveTo(this.mark.getX(), this.mark.getY(), this.mark.getZ(), 1.6);
                return;
            }
            Vec3 push = this.mark.position().subtract(m.position()).normalize();
            if (this.mark instanceof Player || this.mark instanceof Boat) {
                this.mark.push(push.x * 0.5, 0.25, push.z * 0.5);
                this.mark.hurtMarked = true;
            } else {
                this.mark.setDeltaMovement(push.x * 0.3, 0.8, push.z * 0.3);      // up and out of the water it goes
            }
            if (m.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SPLASH, this.mark.getX(), this.mark.getY() + 0.5, this.mark.getZ(), 20, 0.5, 0.3, 0.5, 0.1);
            }
            m.playSound(SoundEvents.DOLPHIN_PLAY, 1.2F, m.getVoicePitch());
            m.startTimedAction(Action.LAUGH, 40);
            Vec3 away = m.position().subtract(this.mark.position()).normalize().scale(10).add(m.position());
            m.getNavigation().moveTo(away.x, away.y, away.z, 1.8);
            this.ticks = 999;
        }
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("FromBucket", this.fromBucket);
        if (this.offers != null) tag.put("Offers", net.minecraft.world.item.trading.MerchantOffers.CODEC.encodeStart(
                this.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), this.offers).getOrThrow());
        tag.putInt("DryTicks", this.dryTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.fromBucket = tag.getBoolean("FromBucket");
        if (tag.contains("Offers")) {
            net.minecraft.world.item.trading.MerchantOffers.CODEC.parse(this.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag.get("Offers"))
                    .resultOrPartial(e -> {}).ifPresent(o -> this.offers = o);
        }
        this.dryTicks = tag.getInt("DryTicks");
    }
    // --- the dragon koi floats ----------------------------------------------------------------------------------------

    /** The dragon koi (a koi's true form) needs no water: out of it she floats, slowly, a little above the ground. */
    public boolean isLevitating() {
        return this.species == GirlSpecies.KOI_MERMAID && this.isTrueForm() && !this.isInWater();
    }

    private void levitate() {
        boolean on = this.isLevitating();
        if (on != this.isNoGravity()) this.setNoGravity(on);
        if (!on) return;
        double ground = this.getY() - 8;
        for (int dy = 0; dy <= 8; dy++) {                                  // the first solid block below her
            BlockPos p = BlockPos.containing(this.getX(), this.getY() - dy - 0.01, this.getZ());
            if (!this.level().getBlockState(p).getCollisionShape(this.level(), p).isEmpty()) {
                ground = p.getY() + 1;
                break;
            }
        }
        double target = ground + 1.2 + 0.15 * Math.sin(this.tickCount * 0.08);
        Vec3 v = this.getDeltaMovement();
        this.setDeltaMovement(v.x, Math.max(-0.08, Math.min(0.08, (target - this.getY()) * 0.1)), v.z);
        this.fallDistance = 0;
    }

    // --- coming up for air -------------------------------------------------------------------------------------------

    /** When she next comes up for a breath (-1: not yet decided). */
    private long nextBreath = -1;

    public void setNextBreath(long time) {
        this.nextBreath = time;
    }

    /**
     * She breathes water, but every one to three minutes she rises to the surface, puts her head out and takes a breath
     * for a few seconds anyway, as a seal or a dolphin would: it sells the illusion.
     */
    class SurfaceGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private int ticks;
        private int atSurface;

        SurfaceGoal() {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Merfolk m = Merfolk.this;
            if (m.nextBreath < 0) m.nextBreath = m.level().getGameTime() + 1200 + m.random.nextInt(2400);
            return m.level().getGameTime() >= m.nextBreath && m.getTarget() == null && m.isInWater() && m.isEyeInFluid(FluidTags.WATER);
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks < 300 && this.atSurface < 60 && Merfolk.this.getTarget() == null && Merfolk.this.isInWater();
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.atSurface = 0;
            Merfolk.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            Merfolk m = Merfolk.this;
            m.nextBreath = m.level().getGameTime() + 1200 + m.random.nextInt(2400);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            Merfolk m = Merfolk.this;
            this.ticks++;
            m.setXRot(-30.0F);
            if (m.isEyeInFluid(FluidTags.WATER)) {
                m.setDeltaMovement(m.getDeltaMovement().multiply(0.8, 1.0, 0.8).add(0.0, 0.06, 0.0));   // straight up
                return;
            }
            if (this.atSurface++ == 0 && m.level() instanceof ServerLevel server) {                      // the breath
                server.sendParticles(ParticleTypes.SPLASH, m.getX(), m.getEyeY(), m.getZ(), 12, 0.3, 0.1, 0.3, 0.1);
                m.playSound(SoundEvents.GENERIC_SPLASH, 0.4F, 1.4F);
            }
            if (m.getDeltaMovement().y < 0) m.setDeltaMovement(m.getDeltaMovement().add(0.0, 0.04, 0.0));   // keep her head out
        }
    }

    // --- sharing her breath ----------------------------------------------------------------------------------------

    private boolean needsAir;
    private int airless;
    private long nextShare;

    public boolean needsAir() {
        return this.needsAir;
    }

    /**
     * High friendship: a player near her running out of air gets a full breath from her, once every 30 seconds. Then
     * she has to surface herself, and drowns slowly if she can't within 20 seconds.
     */
    public void breathe(ServerLevel level) {
        if (this.needsAir) {
            if (!this.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)) {
                this.needsAir = false;
                this.airless = 0;
            } else if (++this.airless > 40) {
                this.hurt(this.damageSources().drown(), 2.0F);
            }
            return;
        }
        if (level.getGameTime() < this.nextShare) return;
        Player p = this.bondedPlayer(BED_AFFECTION, 6);
        if (p == null || p.getAirSupply() > p.getMaxAirSupply() / 3
                || !level.getFluidState(BlockPos.containing(p.getEyePosition())).is(net.minecraft.tags.FluidTags.WATER)) return;
        p.setAirSupply(p.getMaxAirSupply());
        this.needsAir = true;
        this.airless = 0;
        this.nextShare = level.getGameTime() + 600;
        level.sendParticles(ParticleTypes.BUBBLE, p.getX(), p.getEyeY(), p.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
    }

    // --- fetching ---------------------------------------------------------------------------------------------------

    /** Something her player dropped into the water: she swims for it and brings it back. */
    class FetchGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private Player owner;
        private net.minecraft.world.entity.item.ItemEntity item;
        private int cooldown;
        private int ticks;

        FetchGoal() {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private boolean holding() {
            return !Merfolk.this.getMainHandItem().isEmpty();
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0 || Merfolk.this.isBaby() || this.holding() || Merfolk.this.getTarget() != null) return false;
            this.cooldown = 20;
            this.owner = Merfolk.this.bondedPlayer(10, 24);
            if (this.owner == null) return false;
            this.item = Merfolk.this.nearest(net.minecraft.world.entity.item.ItemEntity.class, 16.0,
                    e -> e.isAlive() && e.isInWater() && e.getOwner() == this.owner);
            return this.item != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks < 600 && this.owner.isAlive() && (this.holding() || this.item.isAlive());
        }

        @Override
        public void start() {
            this.ticks = 0;
        }

        @Override
        public void tick() {
            this.ticks++;
            var target = this.holding() ? (net.minecraft.world.entity.Entity) this.owner : this.item;
            Merfolk.this.getLookControl().setLookAt(target);
            if (Merfolk.this.distanceToSqr(target) > (this.holding() ? 6.0 : 2.0)) {
                if (this.ticks % 10 == 1) Merfolk.this.getNavigation().moveTo(target, 1.3);
                return;
            }
            if (!this.holding()) {
                Merfolk.this.setItemSlot(EquipmentSlot.MAINHAND, this.item.getItem().copy());
                Merfolk.this.setDropChance(EquipmentSlot.MAINHAND, 2.0F);
                this.item.discard();
                return;
            }
            ItemStack back = Merfolk.this.getMainHandItem();
            Merfolk.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            if (!this.owner.getInventory().add(back)) Merfolk.this.spawnAtLocation(back);
            Merfolk.this.playSound(SoundEvents.ITEM_PICKUP, 0.6F, 1.2F);
        }

        @Override
        public void stop() {
            if (this.holding()) {                                             // could not reach you: she leaves it where she is
                Merfolk.this.spawnAtLocation(Merfolk.this.getMainHandItem());
                Merfolk.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
            this.item = null;
        }
    }

    // --- the merchant -----------------------------------------------------------------------------------------------

    @Nullable
    private Player tradingPlayer;
    @Nullable
    private net.minecraft.world.item.trading.MerchantOffers offers;

    public boolean isMerchant() {
        return this.hasFlag(FLAG_MERCHANT);
    }

    public void setMerchant(boolean merchant) {
        this.setFlag(FLAG_MERCHANT, merchant);
    }

    /** One wild grown merfolk in five is a merchant: neutral, and here to trade. */
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                                                   net.minecraft.world.entity.MobSpawnType type, @Nullable net.minecraft.world.entity.SpawnGroupData group) {
        var data = super.finalizeSpawn(level, difficulty, type, group);
        if (type == net.minecraft.world.entity.MobSpawnType.NATURAL && !this.isBaby() && this.random.nextInt(5) == 0) this.setMerchant(true);
        return data;
    }

    private static final java.util.List<net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable>> SEA_LOOT = java.util.List.of(
            net.minecraft.world.level.storage.loot.BuiltInLootTables.UNDERWATER_RUIN_SMALL, net.minecraft.world.level.storage.loot.BuiltInLootTables.UNDERWATER_RUIN_BIG,
            net.minecraft.world.level.storage.loot.BuiltInLootTables.SHIPWRECK_TREASURE, net.minecraft.world.level.storage.loot.BuiltInLootTables.SHIPWRECK_SUPPLY,
            net.minecraft.world.level.storage.loot.BuiltInLootTables.BURIED_TREASURE);

    /** Emeralds for one of something, by how rare it is. */
    static int price(ItemStack s) {
        int each = switch (s.getRarity()) {
            case COMMON -> 1;
            case UNCOMMON -> 6;
            case RARE -> 14;
            case EPIC -> 28;
        };
        if (s.isEnchanted()) each += 8;
        return Math.max(1, Math.min(64, s.getMaxStackSize() > 1 ? Math.max(1, s.getCount() * each / 4) : each));
    }

    /** Her wares come out of the sea's own loot (ruins, shipwrecks, buried treasure); she buys fish and shiny things. */
    public net.minecraft.world.item.trading.MerchantOffers makeOffers(ServerLevel level) {
        var o = new net.minecraft.world.item.trading.MerchantOffers();
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, this.position())
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
        java.util.Set<net.minecraft.world.item.Item> seen = new java.util.HashSet<>();
        for (int roll = 0; roll < 4 && o.size() < 6; roll++) {
            var table = level.getServer().reloadableRegistries().getLootTable(SEA_LOOT.get(this.random.nextInt(SEA_LOOT.size())));
            for (ItemStack s : table.getRandomItems(params)) {
                if (o.size() >= 6 || s.isEmpty() || s.is(Items.MAP) || s.is(Items.FILLED_MAP) || !seen.add(s.getItem())) continue;
                o.add(new net.minecraft.world.item.trading.MerchantOffer(new net.minecraft.world.item.trading.ItemCost(Items.EMERALD, price(s)), s, 4, 3, 0.05F));
            }
        }
        o.add(new net.minecraft.world.item.trading.MerchantOffer(new net.minecraft.world.item.trading.ItemCost(Items.COD, 12), new ItemStack(Items.EMERALD), 16, 2, 0.05F));
        o.add(new net.minecraft.world.item.trading.MerchantOffer(new net.minecraft.world.item.trading.ItemCost(Items.GOLD_INGOT, 3), new ItemStack(Items.EMERALD, 2), 12, 2, 0.05F));
        return o;
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public net.minecraft.world.item.trading.MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = this.level() instanceof ServerLevel server ? this.makeOffers(server) : new net.minecraft.world.item.trading.MerchantOffers();
        }
        return this.offers;
    }

    @Override
    public void overrideOffers(net.minecraft.world.item.trading.MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(net.minecraft.world.item.trading.MerchantOffer offer) {
        offer.increaseUses();
        if (this.tradingPlayer != null) this.addAffection(this.tradingPlayer, 1);
        this.playSound(SoundEvents.DOLPHIN_PLAY, 0.8F, 1.3F);
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.DOLPHIN_PLAY;
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }

    // --- the bucket (young only) ----------------------------------------------------------------------------------

    private boolean fromBucket;

    @Override
    public boolean fromBucket() {
        return this.fromBucket;
    }

    @Override
    public void setFromBucket(boolean fromBucket) {
        this.fromBucket = fromBucket;
        if (fromBucket) this.setPersistenceRequired();
    }

    /** The whole of her (age, father, bonds, colour) goes into the bucket, and her species with it. */
    @Override
    public void saveToBucketTag(ItemStack stack) {
        net.minecraft.world.entity.animal.Bucketable.saveDefaultDataToBucketTag(this, stack);
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.BUCKET_ENTITY_DATA, stack, tag -> {
            CompoundTag girl = new CompoundTag();
            this.addAdditionalSaveData(girl);
            tag.put("Girl", girl);
            tag.putString("Species", this.species.id());
        });
    }

    @Override
    public void loadFromBucketTag(CompoundTag tag) {
        net.minecraft.world.entity.animal.Bucketable.loadDefaultDataFromBucketTag(this, tag);
        if (tag.contains("Girl")) this.readAdditionalSaveData(tag.getCompound("Girl"));
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.MERFOLK_BUCKET.get());
    }

    @Override
    public SoundEvent getPickupSound() {
        return SoundEvents.BUCKET_FILL_FISH;
    }

    @Override
    public boolean isHealer() {
        return this.species == GirlSpecies.AXOLOTL_MERMAID;
    }

    // --- the sword koi -------------------------------------------------------------------------------------------

    /** Her duel with a drowned: whatever it holds is hers to win, and it falls when the drowned does. */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        if (this.species == GirlSpecies.SWORD_KOI_MERMAID && target instanceof Drowned d) {
            d.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 2.0F);
            d.setDropChance(net.minecraft.world.entity.EquipmentSlot.OFFHAND, 2.0F);
        }
        return super.doHurtTarget(target);
    }

    // --- the sawfin ----------------------------------------------------------------------------------------------

    /** Her friend within 8 blocks, underwater, digs fast (Haste II). Whether she helped. */
    public boolean sapperAid(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 8);
        if (friend == null || !level.getFluidState(net.minecraft.core.BlockPos.containing(friend.getEyePosition())).is(net.minecraft.tags.FluidTags.WATER)) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED, 60, 1, true, true, true));
        return true;
    }

    // --- the rusalka (eighteenth wave) ------------------------------------------------------------------------------

    /** Her friend within 16 blocks, fishing, has Luck. True if she gave it. */
    public boolean riverLuck(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 16);
        if (friend == null || friend.fishing == null) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.LUCK, 60, 0, true, true));
        return true;
    }

    // --- the moopie (nineteenth wave) -------------------------------------------------------------------------------

    /** Poison, hunger and nausea wear off her friend within 8 blocks. How many effects she cleared, or -1 without a friend. */
    public int seaMilk(ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 8);
        if (friend == null) return -1;
        int n = 0;
        for (var effect : java.util.List.of(net.minecraft.world.effect.MobEffects.POISON, net.minecraft.world.effect.MobEffects.HUNGER, net.minecraft.world.effect.MobEffects.CONFUSION)) {
            if (friend.removeEffect(effect)) n++;
        }
        if (n > 0) this.playSound(net.minecraft.sounds.SoundEvents.COW_MILK, 1.0F, 1.2F);
        return n;
    }
}
