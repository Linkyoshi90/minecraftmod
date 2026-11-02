package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.block.GirlEggBlock;
import com.linkyoshi90.monstergirls.block.GirlEggBlockEntity;
import com.linkyoshi90.monstergirls.entity.ai.AmphibiousMoveControl;
import com.linkyoshi90.monstergirls.entity.ai.OpenDoorsGoal;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The shell and tentacle family (mollusk_field_guide.html): kraken, Ika, octopus, snail, ammonite. Each has her own
 * way of staying safe: the kraken grips and squeezes, Ika lashes with her tentacle hair from four blocks away, the
 * octopus inks and flees (and opens every door she meets), the snail and the ammonite pull into their shells and take
 * a fifth of the damage. They eat fish; the snail wants vegetables. The kraken attacks swimmers in the deep until fed.
 */
public class Mollusk extends AbstractMonsterGirl {
    public static final int RETRACT_TICKS = 100;

    private int retracted;

    public Mollusk(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        boolean sea = this.isSeaDweller();
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        if (this.swims()) {                                              // in the water she dives and swims rather than bobbing
            this.moveControl = new AmphibiousMoveControl(this);
            this.goalSelector.removeAllGoals(g -> g instanceof FloatGoal);
            this.goalSelector.addGoal(7, new RandomSwimmingGoal(this, 1.0, 20));
        }
        if (sea) this.goalSelector.addGoal(1, new TryFindWaterGoal(this));
        if (species == GirlSpecies.OCTOPUS_MOLLUSK || species == GirlSpecies.SNAIL_MOLLUSK) {
            this.goalSelector.addGoal(1, new PanicGoal(this, 1.6));
            this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 6.0F, 1.1, 1.5,
                    e -> e instanceof Player p && !p.isShiftKeyDown() && this.getAffection(p) < 10 && !p.isSpectator() && !p.isCreative()));
        } else {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true) {
                @Override
                protected void checkAndPerformAttack(LivingEntity target) {
                    if (this.canPerformAttack(target) && species != GirlSpecies.AMMONITE_MOLLUSK && species != GirlSpecies.JELLYFISH_MOLLUSK) {
                        Mollusk.this.startTimedAction(species == GirlSpecies.IKA_MOLLUSK ? Action.LASH : Action.GRAB, 20);
                    }
                    super.checkAndPerformAttack(target);
                }
            });
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Drowned.class, true));
        }
        if (species == GirlSpecies.OCTOPUS_MOLLUSK) this.goalSelector.addGoal(0, new OpenDoorsGoal(this));
        if (species == GirlSpecies.IKA_MOLLUSK) {                       // her friends' monsters are her monsters
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true,
                    m -> { Player p = this.bondedPlayer(BED_AFFECTION, 16); return p != null && m.distanceToSqr(p) < 8 * 8; }));
        }
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, species == GirlSpecies.SNAIL_MOLLUSK
                ? Ingredient.of(Items.CARROT, Items.POTATO, Items.BEETROOT) : Ingredient.of(ItemTags.FISHES), false));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8) {
            @Override
            public boolean canUse() {
                return !Mollusk.this.isRetracted() && super.canUse();
            }
        });
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case GIANT_KRAKEN_MOLLUSK -> girlAttributes(90, 0.24, 12);
            case KRAKEN_MOLLUSK -> girlAttributes(44, 0.24, 7);
            case IKA_MOLLUSK -> girlAttributes(20, 0.28, 4);
            case OCTOPUS_MOLLUSK -> girlAttributes(16, 0.3, 2);
            case SNAIL_MOLLUSK -> girlAttributes(18, 0.16, 1);
            case JELLYFISH_MOLLUSK -> girlAttributes(18, 0.2, 2);
            case SQUIDKIN_MOLLUSK -> girlAttributes(18, 0.28, 2);
            default -> girlAttributes(26, 0.2, 3);
        };
    }

    /** The Min Min light floats wherever she goes. */
    @Override
    public boolean canFly() {
        return this.species == GirlSpecies.MIN_MIN_MOLLUSK || super.canFly();
    }

    @Override
    protected boolean hasFlyAnimation() {
        return false;
    }

    /** At night the Min Min light keeps a few blocks ahead of her friend and lights the ground. 1 when she does, 0 by day, -1 without a friend. */
    public int leadTheWay(net.minecraft.server.level.ServerLevel level, boolean night) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        if (!night) return 0;
        net.minecraft.world.phys.Vec3 ahead = friend.position().add(friend.getLookAngle().multiply(1, 0, 1).normalize().scale(5)).add(0, 2, 0);
        this.getMoveControl().setWantedPosition(ahead.x, ahead.y, ahead.z, 1.0);
        this.glow(level, 14);
        return 1;
    }

    /** The kraken, the ammonite and the jellyfish live in the sea; the others walk. */
    public boolean isSeaDweller() {
        return this.species == GirlSpecies.KRAKEN_MOLLUSK || this.species == GirlSpecies.GIANT_KRAKEN_MOLLUSK || this.species == GirlSpecies.AMMONITE_MOLLUSK || this.species == GirlSpecies.JELLYFISH_MOLLUSK;
    }

    /** Who swims in the water: the sea dwellers, Ika and the octopus (the snail only floats). */
    public boolean swims() {
        return this.isSeaDweller() || this.species == GirlSpecies.IKA_MOLLUSK || this.species == GirlSpecies.OCTOPUS_MOLLUSK;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new com.linkyoshi90.monstergirls.entity.ai.SwimmerNavigation(this, level);
    }

    @Override
    protected boolean hasSwimAnimation() {
        return this.isSeaDweller() && this.species != GirlSpecies.JELLYFISH_MOLLUSK;   // the jellyfish just drifts
    }

    @Override
    public void travel(Vec3 input) {
        if (this.swims() && this.isControlledByLocalInstance() && this.isInWater()) {
            this.moveRelative(this.getSpeed() * (this.species == GirlSpecies.JELLYFISH_MOLLUSK ? 0.35F : 1.5F), input);   // the jellyfish only drifts
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            return;
        }
        super.travel(input);
    }

    @Override
    public float getSpeed() {
        float s = super.getSpeed();
        if (this.isRetracted()) return 0.0F;
        return this.isSeaDweller() && !this.isInWater() ? s * 0.35F : s;
    }

    // --- the kraken ----------------------------------------------------------------------------------------------

    @Override
    protected boolean wildHostile() {
        return this.species == GirlSpecies.KRAKEN_MOLLUSK || this.species == GirlSpecies.GIANT_KRAKEN_MOLLUSK;
    }

    /** The kraken only goes for swimmers. */
    @Override
    public boolean isHostileTo(Player player) {
        return super.isHostileTo(player) && player.isInWater();
    }

    /** Ika's tentacle hair reaches four blocks. */
    @Override
    protected AABB getAttackBoundingBox() {
        AABB box = super.getAttackBoundingBox();
        return this.species == GirlSpecies.IKA_MOLLUSK ? box.inflate(3.0, 0.5, 3.0) : box;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity l && this.species == GirlSpecies.KRAKEN_MOLLUSK) {
            l.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));      // held fast in her grip
        }
        return hit;
    }

    // --- ink and shells ------------------------------------------------------------------------------------------

    public boolean isRetracted() {
        return this.retracted > 0;
    }

    public void retract() {
        if (this.species != GirlSpecies.SNAIL_MOLLUSK && this.species != GirlSpecies.AMMONITE_MOLLUSK) return;
        this.retracted = RETRACT_TICKS;
        this.setFlag(FLAG_RETRACTED, true);
        this.getNavigation().stop();
        this.setAction(Action.RETRACT);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isRetracted() && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) amount *= 0.2F;
        boolean hurt = super.hurt(source, amount);
        if (!hurt || this.level().isClientSide) return hurt;
        this.retract();
        if ((this.species == GirlSpecies.OCTOPUS_MOLLUSK || this.species == GirlSpecies.IKA_MOLLUSK) && source.getEntity() instanceof LivingEntity attacker) {
            attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            this.startTimedAction(Action.INK, 16);
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.6, 0.6, 0.6, 0.05);
            }
            this.playSound(SoundEvents.SQUID_SQUIRT, 1.0F, 1.2F);
        }
        return hurt;
    }

    // --- food, presents ------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        if (this.species == GirlSpecies.SNAIL_MOLLUSK) return stack.is(Items.CARROT) || stack.is(Items.POTATO) || stack.is(Items.BEETROOT);
        return stack.is(ItemTags.FISHES) || stack.is(Items.KELP) || stack.is(Items.DRIED_KELP);
    }

    /** The snail would rather not be fed meat; fish counts for the rest. */
    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return this.isFood(stack) ? 2 : 1;
    }

    @Override
    protected int petValue(Player player) {
        return this.isRetracted() ? 0 : 1;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case SNAIL_MOLLUSK -> new ItemStack(r < 50 ? Items.BROWN_MUSHROOM : r < 80 ? Items.MOSS_BLOCK : Items.SLIME_BALL);
            case AMMONITE_MOLLUSK -> new ItemStack(r < 50 ? Items.BONE : r < 85 ? Items.NAUTILUS_SHELL : Items.PRISMARINE_CRYSTALS);
            case OCTOPUS_MOLLUSK -> new ItemStack(r < 40 ? Items.INK_SAC : r < 80 ? Items.GOLD_NUGGET : Items.AMETHYST_SHARD, r < 80 ? 2 : 1);
            case JELLYFISH_MOLLUSK -> new ItemStack(r < 40 ? Items.SLIME_BALL : r < 75 ? Items.SEA_PICKLE : r < 95 ? Items.PRISMARINE_CRYSTALS : Items.GLOW_INK_SAC,
                    r < 75 ? 2 : 1);
            default -> new ItemStack(r < 40 ? Items.INK_SAC : r < 75 ? Items.PRISMARINE_SHARD : r < 95 ? Items.COD : Items.NAUTILUS_SHELL);
        };
    }

    // --- young ---------------------------------------------------------------------------------------------------

    @Override
    protected int litterSize() {
        return 2 + this.random.nextInt(3);
    }

    /** Eggs: in water for the sea dwellers (roe), a leathery clutch on land for the snail. */
    @Override
    protected boolean giveBirth(ServerLevel level, @Nullable UUID father) {
        BlockPos o = this.blockPosition();
        boolean water = this.species != GirlSpecies.SNAIL_MOLLUSK;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-5, -3, -5), o.offset(5, 2, 5))) {
            boolean ok = water ? level.getBlockState(p).is(Blocks.WATER) && level.getFluidState(p).isSource()
                    && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP) : canHoldBlock(level, p);
            if (!ok) continue;
            level.setBlock(p, ModBlocks.GIRL_EGG.get().defaultBlockState().setValue(GirlEggBlock.KIND, water ? GirlEggBlock.Kind.ROE : GirlEggBlock.Kind.LEATHERY)
                    .setValue(GirlEggBlock.WATERLOGGED, water), 3);
            if (level.getBlockEntity(p) instanceof GirlEggBlockEntity be) be.setup(this.species.id(), this.litterSize(), father).withPaint(this.getPaint());
            return true;
        }
        return water ? super.giveBirth(level, father) : false;
    }

    // --- the jellyfish ----------------------------------------------------------------------------------------------

    /** Who her sting spares: her friends, other jellyfish, players in creative. */
    private boolean spares(LivingEntity e) {
        if (e instanceof Player p) return p.isCreative() || p.isSpectator() || this.getAffection(p) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION;
        return e instanceof Mollusk m && m.species == GirlSpecies.JELLYFISH_MOLLUSK;
    }

    /** She stings whatever bumps into her. Returns how many she stung. */
    public int sting(ServerLevel level) {
        int n = 0;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.3), e -> e != this && e.isAlive() && !this.spares(e))) {
            e.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), this);
            n++;
        }
        return n;
    }

    /** At night she glows; she stings what touches her; friends swimming near her see in the dark. */
    private void tickJelly(ServerLevel level) {
        this.glow(level, level.isDay() ? 0 : 9);
        if (this.tickCount % 10 == 0) this.sting(level);
        if (this.tickCount % 40 == 0) {
            for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(8), p -> p.isInWater() && this.spares(p) && !p.isSpectator())) {
                p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
            }
        }
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    /** Every monster in the water within 16 of her friend takes 10. How many, -1 without a friend. */
    public int crush(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32);
        if (friend == null) return -1;
        int crushed = 0;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(16), m -> m.isAlive() && m.isInWater())) {
            m.hurt(this.damageSources().mobAttack(this), 10);
            crushed++;
        }
        return crushed;
    }

    private static final String BORROWED = "BorrowedSting", BORROWED_UNTIL = "BorrowedUntil";

    /** The nautilus jets her crouching friend in the water near her upward. True if she did. */
    public boolean jet() {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 8);
        if (friend == null || !friend.isInWater() || !friend.isShiftKeyDown()) return false;
        friend.setDeltaMovement(friend.getDeltaMovement().add(0, 0.5, 0));
        friend.hurtMarked = true;
        return true;
    }

    /** The sea slug takes on a potion: its first effect, shared with her friend near her for three minutes. True if she took it. */
    public boolean borrow(ItemStack potion, long now) {
        net.minecraft.world.item.alchemy.PotionContents contents = potion.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        if (contents == null) return false;
        for (net.minecraft.world.effect.MobEffectInstance e : contents.getAllEffects()) {
            net.minecraft.resources.ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(e.getEffect().value());
            if (id == null) continue;
            this.getPersistentData().putString(BORROWED, id.toString());
            this.getPersistentData().putLong(BORROWED_UNTIL, now + 3600);
            return true;
        }
        return false;
    }

    /** She shares what she borrowed with her friend near her. True if she did. */
    public boolean shareSting(long now) {
        if (now >= this.getPersistentData().getLong(BORROWED_UNTIL)) return false;
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 8);
        if (friend == null) return false;
        net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.tryParse(this.getPersistentData().getString(BORROWED));
        if (id == null) return false;
        var effect = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getHolder(id);
        if (effect.isEmpty()) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect.get(), 80, 0, true, true));
        return true;
    }

    @Override
    protected net.minecraft.world.InteractionResult familyInteract(Player player, net.minecraft.world.InteractionHand hand, ItemStack stack) {
        if (this.species == GirlSpecies.SEA_SLUG_MOLLUSK && stack.is(net.minecraft.world.item.Items.POTION) && this.getAffection(player) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION) {
            if (!this.level().isClientSide && this.borrow(stack, this.level().getGameTime())) {
                stack.consume(1, player);
                player.getInventory().placeItemBackInInventory(new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE));
                this.playSound(net.minecraft.sounds.SoundEvents.GENERIC_DRINK, 1.0F, 1.0F);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.familyInteract(player, hand, stack);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && !this.isBaby()) {
            if (this.species == GirlSpecies.NAUTILUS_MOLLUSK && this.tickCount % 5 == 0) this.jet();
            if (this.species == GirlSpecies.SEA_SLUG_MOLLUSK && this.tickCount % 40 == 0) this.shareSting(this.level().getGameTime());
        }
        if (this.species == GirlSpecies.GIANT_KRAKEN_MOLLUSK && this.level() instanceof net.minecraft.server.level.ServerLevel deep && !this.isBaby() && this.tickCount % 100 == 0) this.crush(deep);
        if (this.species == GirlSpecies.MIN_MIN_MOLLUSK && this.level() instanceof net.minecraft.server.level.ServerLevel plain && !this.isBaby() && this.tickCount % 5 == 0) this.leadTheWay(plain, !plain.isDay());
        if (this.species == GirlSpecies.CHARYBDIS_MOLLUSK && this.level() instanceof net.minecraft.server.level.ServerLevel maw && !this.isBaby() && this.tickCount % 20 == 0) this.whirlpool(maw);
        if (this.species == GirlSpecies.SPEWSLUG_MOLLUSK && this.level() instanceof net.minecraft.server.level.ServerLevel bog && !this.isBaby() && this.tickCount % 10 == 0) this.bile(bog);
        if (this.species == GirlSpecies.SCYLLA_MOLLUSK && this.level() instanceof net.minecraft.server.level.ServerLevel strait && !this.isBaby() && this.tickCount % 60 == 0) this.sixHounds(strait);
        if (this.species == GirlSpecies.KRAKEN_MOLLUSK && this.hasFlag(FLAG_TOWING)) this.tow();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.species == GirlSpecies.SQUIDKIN_MOLLUSK && this.tickCount % 5 == 0) this.inkCloud(server);
        if (this.species == GirlSpecies.KRAKEN_MOLLUSK && this.tickCount % 10 == 0) this.checkTow();
        boolean squid = this.species == GirlSpecies.KRAKEN_MOLLUSK || this.species == GirlSpecies.IKA_MOLLUSK || this.species == GirlSpecies.OCTOPUS_MOLLUSK;
        if (squid && !this.isBaby() && this.random.nextInt(9000) == 0) {           // about once every 7.5 minutes, like a chicken's egg
            boolean glow = this.species == GirlSpecies.IKA_MOLLUSK && "firefly".equals(this.getVariantName());
            this.spawnAtLocation(new ItemStack(glow ? Items.GLOW_INK_SAC : Items.INK_SAC));
        }
        if (this.retracted > 0) {
            this.getNavigation().stop();
            this.setDeltaMovement(this.getDeltaMovement().multiply(0, 1, 0));
            if (--this.retracted == 0) {
                this.setFlag(FLAG_RETRACTED, false);
                if (this.getAction() == Action.RETRACT) this.setAction(Action.NONE);
            }
        }
        // the snail is frightened by anyone rushing at her
        if (this.species == GirlSpecies.SNAIL_MOLLUSK && !this.isRetracted() && this.tickCount % 10 == 0) {
            Player p = this.level().getNearestPlayer(this, 4.0);
            if (p != null && p.isSprinting() && this.getAffection(p) < 25) this.retract();
            if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) {     // her slime trail slows what follows her
                server.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 0.1, this.getZ(), 2, 0.2, 0.0, 0.2, 0.0);
                for (Monster m : server.getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(3))) {
                    m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
                }
            }
        }
        if (this.species == GirlSpecies.JELLYFISH_MOLLUSK) this.tickJelly(server);
        if (this.tickCount % 40 == 0 && !this.isBaby()) {               // a devoted mollusk shares the sea's breath
            Player p = this.bondedPlayer(60, 10);
            if (p != null && p.isInWater()) p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, true, false));
        }
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isRetracted() ? null : this.isInWater() ? SoundEvents.SQUID_AMBIENT : SoundEvents.TURTLE_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SQUID_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.DOLPHIN_EAT;
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
        this.setFlag(FLAG_RETRACTED, this.retracted > 0);
    }

    // --- towing a boat ---------------------------------------------------------------------------------------------

    @Nullable
    private net.minecraft.world.entity.vehicle.Boat towedBoat() {
        var boats = this.level().getEntitiesOfClass(net.minecraft.world.entity.vehicle.Boat.class, this.getBoundingBox().inflate(12),
                b -> b.getControllingPassenger() instanceof Player && b.isInWater());
        return boats.isEmpty() ? null : boats.get(0);
    }

    /** Server: the kraken takes the boat of the player she is fond of (high friendship) whenever she is in the water near it. */
    private void checkTow() {
        Player p = this.bondedPlayer(BED_AFFECTION, 12);
        boolean towing = p != null && p.getVehicle() instanceof net.minecraft.world.entity.vehicle.Boat b && b.isInWater() && this.isInWater() && !this.isBaby();
        if (towing != this.hasFlag(FLAG_TOWING)) this.setFlag(FLAG_TOWING, towing);
        if (towing) {
            Vec3 ahead = p.getVehicle().position().add(p.getViewVector(1.0F).multiply(1, 0, 1).normalize().scale(3.0));
            this.getNavigation().moveTo(ahead.x, p.getVehicle().getY() - 1.0, ahead.z, 1.6);
        }
    }

    /** Both sides (the boat moves where its rider's game runs): a strong pull the way the rider is looking. */
    private void tow() {
        var boat = this.towedBoat();
        if (boat == null || !(boat.getControllingPassenger() instanceof Player rider)) return;
        Vec3 look = rider.getViewVector(1.0F).multiply(1, 0, 1).normalize();
        Vec3 v = boat.getDeltaMovement().add(look.scale(0.04));
        double h = v.horizontalDistance();
        if (h > 1.1) v = new Vec3(v.x / h * 1.1, v.y, v.z / h * 1.1);
        boat.setDeltaMovement(v);
    }

    @Override
    public boolean isHealer() {
        return this.species == GirlSpecies.SNAIL_MOLLUSK;
    }

    // --- the squidkin --------------------------------------------------------------------------------------------

    private long nextInk;

    /** Her friend hurt within 12 blocks of her: ink. Monsters within 6 of her friend go blind, her friend turns invisible for 3 s. How many monsters, or -1. */
    public int inkCloud(ServerLevel level) {
        if (this.isBaby() || level.getGameTime() < this.nextInk) return -1;
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 12);
        if (friend == null || friend.hurtTime <= 0) return -1;
        java.util.List<Monster> near = level.getEntitiesOfClass(Monster.class, friend.getBoundingBox().inflate(6));
        for (Monster m : near) {
            m.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0), this);
            if (m.getTarget() == friend) m.setTarget(null);
        }
        friend.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false, true));
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SQUID_INK, friend.getX(), friend.getY() + 1, friend.getZ(), 40, 1.2, 0.8, 1.2, 0.05);
        this.playSound(net.minecraft.sounds.SoundEvents.SQUID_SQUIRT, 1.0F, 1.0F);
        this.startTimedAction(Action.INK, 20);
        this.nextInk = level.getGameTime() + 200;
        return near.size();
    }

    /** The last hit on her friend she has answered (the friend's last-hurt-by timestamp), so each hit is answered once. */
    private int lastBile = Integer.MIN_VALUE;

    /**
     * The spewslug's bile (twenty-third wave): the monster that last hit her friend (within the last five seconds, within 12 of
     * them) is spewed on: Weakness II and Hunger for ten seconds. 1 when she spewed, 0 with nothing to answer, -1 without a friend.
     */
    public int bile(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        net.minecraft.world.entity.LivingEntity attacker = friend.getLastHurtByMob();
        int when = friend.getLastHurtByMobTimestamp();
        if (!(attacker instanceof net.minecraft.world.entity.monster.Monster m) || !m.isAlive() || when == this.lastBile || friend.tickCount - when > 100 || m.distanceTo(friend) > 12) return 0;
        this.lastBile = when;
        m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 200, 1), this);
        m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.HUNGER, 200, 0), this);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH, m.getX(), m.getY() + 1, m.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
        this.getLookControl().setLookAt(m);
        this.playSound(net.minecraft.sounds.SoundEvents.LLAMA_SPIT, 1.0F, 0.7F);
        return 1;
    }

    /**
     * Scylla's six hounds (twenty-third wave): up to two monsters within 6 of her friend have their armour and whatever they hold
     * torn off and dropped. How many she stripped, or -1 without a friend.
     */
    public int sixHounds(net.minecraft.server.level.ServerLevel level) {
        net.minecraft.world.entity.player.Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24);
        if (friend == null) return -1;
        int n = 0;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(6), net.minecraft.world.entity.LivingEntity::isAlive)) {
            boolean any = false;
            for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                net.minecraft.world.item.ItemStack held = m.getItemBySlot(slot);
                if (held.isEmpty() || slot == net.minecraft.world.entity.EquipmentSlot.BODY) continue;
                m.spawnAtLocation(held.copy());
                m.setItemSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
                any = true;
            }
            if (!any) continue;
            this.playSound(net.minecraft.sounds.SoundEvents.WOLF_GROWL, 1.0F, 1.0F);
            if (++n == 2) break;
        }
        return n;
    }

    /** Charybdis's whirlpool (twenty-fifth wave): the monsters in the water within 10 of her are pulled towards her. The number pulled, -1 without a friend near. */
    public int whirlpool(net.minecraft.server.level.ServerLevel level) {
        if (this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32) == null) return -1;
        int pulled = 0;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, this.getBoundingBox().inflate(10), m -> m.isAlive() && m.isInWater())) {
            net.minecraft.world.phys.Vec3 in = this.position().subtract(m.position()).normalize().scale(0.8);
            m.setDeltaMovement(in.x, in.y - 0.1, in.z);
            m.hurtMarked = true;
            pulled++;
        }
        return pulled;
    }
}
