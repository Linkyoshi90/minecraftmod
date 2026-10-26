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
import net.minecraft.world.item.Item;
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
    /** The box turtle's home, which she never strays far from. */
    @Nullable
    private BlockPos home;
    private long nextSlide;
    /** Where the slider is sliding to (she keeps at it for up to 5 s, whatever her other goals want). */
    @Nullable
    private BlockPos slideTo;
    public static final int HOME_RANGE = 16;

    public Lizardfolk(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        if (species == GirlSpecies.SALAMANDER_LIZARDFOLK) {
            this.setPathfindingMalus(PathType.LAVA, 0.0F);
            this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
            this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        }
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        if (species == GirlSpecies.TURTLE_LIZARDFOLK || species == GirlSpecies.CHAMELEON_LIZARDFOLK || species == GirlSpecies.RED_EARED_SLIDER_LIZARDFOLK
                || species == GirlSpecies.BOX_TURTLE_LIZARDFOLK) this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true));
        if (species == GirlSpecies.GECKO_LIZARDFOLK) {                   // curious: she trails people and looks at what they drop; she runs up walls
            this.goalSelector.addGoal(3, new InspectGoal());
            this.goalSelector.addGoal(5, new com.linkyoshi90.monstergirls.entity.ai.ClimbGoal(this, 15, () -> false,
                    s -> s.isSolid() && !s.is(BlockTags.LEAVES), 160));
            this.goalSelector.addGoal(6, new FollowEntityGoal(this, () -> this.level().getNearestPlayer(this, 16), 1.0, 10.0F, 5.0F));
        }
        if (species == GirlSpecies.CHUPACABRA_LIZARDFOLK) {              // wild goats only; fed, she lingers near you
            this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this,
                    net.minecraft.world.entity.animal.goat.Goat.class, 10, true, false, Lizardfolk::wildGoat));
            this.goalSelector.addGoal(6, new FollowEntityGoal(this, () -> this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 32), 1.0, 12.0F, 6.0F));
        }
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
            case KAPPA_LIZARDFOLK -> girlAttributes(22, 0.28, 4).add(Attributes.ARMOR, 4);
            case BLACK_TORTOISE_LIZARDFOLK -> girlAttributes(44, 0.2, 6).add(Attributes.ARMOR, 10).add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
            case RED_EARED_SLIDER_LIZARDFOLK -> girlAttributes(20, 0.26, 2).add(Attributes.ARMOR, 5);
            case GALAPAGOS_TORTOISE_LIZARDFOLK -> girlAttributes(70, 0.12, 4).add(Attributes.ARMOR, 12).add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
            case BOX_TURTLE_LIZARDFOLK -> girlAttributes(20, 0.2, 2).add(Attributes.ARMOR, 8);
            case SOFTSHELL_TURTLE_LIZARDFOLK -> girlAttributes(22, 0.32, 6);
            case MATAMATA_LIZARDFOLK -> girlAttributes(22, 0.18, 4).add(Attributes.ARMOR, 4);
            case GECKO_LIZARDFOLK -> girlAttributes(16, 0.38, 2).add(Attributes.ARMOR, 1);
            case CHUPACABRA_LIZARDFOLK -> girlAttributes(26, 0.31, 5).add(Attributes.ARMOR, 3);
            case ANVIL_TORTOISE_LIZARDFOLK -> girlAttributes(40, 0.16, 3).add(Attributes.ARMOR, 12).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
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
                || (this.species == GirlSpecies.TURTLE_LIZARDFOLK && stack.is(Items.SEAGRASS))
                || (this.species == GirlSpecies.KAPPA_LIZARDFOLK && (stack.is(Items.MELON_SLICE) || stack.is(Items.KELP)))   // no cucumbers here
                || (this.species == GirlSpecies.RED_EARED_SLIDER_LIZARDFOLK && (stack.is(Items.KELP) || stack.is(Items.SEAGRASS)))
                || (this.species == GirlSpecies.GALAPAGOS_TORTOISE_LIZARDFOLK && (stack.is(Items.CACTUS) || stack.is(Items.MELON_SLICE) || stack.is(Items.APPLE)))
                || (this.species == GirlSpecies.BOX_TURTLE_LIZARDFOLK && (stack.is(Items.RED_MUSHROOM) || stack.is(Items.BROWN_MUSHROOM) || stack.is(Items.SWEET_BERRIES)))
                || (this.species == GirlSpecies.GECKO_LIZARDFOLK && (stack.is(Items.APPLE) || stack.is(Items.MELON_SLICE) || stack.is(Items.SWEET_BERRIES)
                    || stack.is(Items.GLOW_BERRIES)))
                || (this.species == GirlSpecies.CHUPACABRA_LIZARDFOLK && (stack.is(Items.MUTTON) || stack.is(Items.COOKED_MUTTON)))
                || (this.species == GirlSpecies.ANVIL_TORTOISE_LIZARDFOLK && (stack.is(Items.MELON_SLICE) || stack.is(Items.IRON_NUGGET)));
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        int value = stack.is(ModTags.LAID_EGGS) || stack.is(Items.TURTLE_EGG) ? 2 : 1;
        return this.species == GirlSpecies.RED_EARED_SLIDER_LIZARDFOLK ? value * 2 : value;   // the friendliest of the turtles
    }

    /** A friend climbs onto the Galapagos tortoise's shell (an empty hand, like a centaur; crouch to pat her); she keeps going where she was going. */
    @Override
    @Nullable
    protected net.minecraft.world.InteractionResult familyInteract(Player player, net.minecraft.world.InteractionHand hand, ItemStack stack) {
        if (this.species == GirlSpecies.ANVIL_TORTOISE_LIZARDFOLK && stack.isDamageableItem() && stack.isDamaged() && !this.isBaby()) {
            if (!this.level().isClientSide) this.repair(player, stack);
            return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.species != GirlSpecies.GALAPAGOS_TORTOISE_LIZARDFOLK || !stack.isEmpty() || player.isShiftKeyDown() || this.isBaby()
                || this.getAffection(player) < com.linkyoshi90.monstergirls.entity.Orders.AFFECTION || this.isVehicle()) return null;
        if (!this.level().isClientSide) player.startRiding(this);
        return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    // --- the anvil tortoise ----------------------------------------------------------------------------------------------

    /** A quarter of the item's durability back for one of its repair materials from the player's inventory, as an anvil does, but for no experience. */
    public boolean repair(Player player, ItemStack damaged) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack m = inv.getItem(i);
            if (m.isEmpty() || m == damaged || !damaged.getItem().isValidRepairItem(damaged, m)) continue;
            if (!player.getAbilities().instabuild) m.shrink(1);
            damaged.setDamageValue(Math.max(0, damaged.getDamageValue() - Math.max(1, damaged.getMaxDamage() / 4)));
            this.playSound(SoundEvents.ANVIL_USE, 0.8F, 1.1F);
            this.startTimedAction(Action.SLAM, 14);
            return true;
        }
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.anvil_no_material", this.getDisplayName()), true);
        return false;
    }

    /** Too heavy to shove. */
    @Override
    public boolean isPushable() {
        return this.species != GirlSpecies.ANVIL_TORTOISE_LIZARDFOLK && super.isPushable();
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        if (this.species == GirlSpecies.ANVIL_TORTOISE_LIZARDFOLK) {
            return new ItemStack(r < 50 ? Items.IRON_NUGGET : r < 80 ? Items.IRON_INGOT : r < 97 ? Items.MELON_SLICE : Items.ANVIL, r < 50 ? 6 : r < 80 ? 1 : r < 97 ? 3 : 1);
        }
        return switch (this.species) {
            case TURTLE_LIZARDFOLK -> new ItemStack(r < 30 ? Items.TURTLE_SCUTE : r < 70 ? Items.SEAGRASS : Items.NAUTILUS_SHELL);
            case SNAPPING_TURTLE_LIZARDFOLK -> new ItemStack(r < 45 ? Items.COD : r < 80 ? Items.SALMON : Items.CLAY_BALL, r < 80 ? 1 : 4);
            case KAPPA_LIZARDFOLK -> new ItemStack(r < 40 ? Items.COD : r < 70 ? Items.KELP : r < 95 ? Items.SEA_PICKLE : Items.NAUTILUS_SHELL, r < 70 ? 2 : 1);
            case SALAMANDER_LIZARDFOLK -> new ItemStack(r < 50 ? Items.MAGMA_CREAM : Items.BLAZE_POWDER);
            case CROC_LIZARDFOLK -> new ItemStack(r < 60 ? Items.BONE : Items.LEATHER, 2);
            case RED_EARED_SLIDER_LIZARDFOLK -> new ItemStack(r < 50 ? Items.LILY_PAD : r < 85 ? Items.SEAGRASS : Items.TURTLE_SCUTE, r < 85 ? 2 : 1);
            case GALAPAGOS_TORTOISE_LIZARDFOLK -> new ItemStack(r < 40 ? Items.CACTUS : r < 75 ? Items.MELON_SLICE : r < 95 ? Items.TURTLE_SCUTE : Items.NAME_TAG, r < 75 ? 2 : 1);
            case BOX_TURTLE_LIZARDFOLK -> new ItemStack(r < 45 ? Items.RED_MUSHROOM : r < 90 ? Items.BROWN_MUSHROOM : Items.TURTLE_SCUTE, r < 90 ? 2 : 1);
            case SOFTSHELL_TURTLE_LIZARDFOLK, MATAMATA_LIZARDFOLK -> new ItemStack(r < 50 ? Items.COD : r < 85 ? Items.SALMON : Items.TROPICAL_FISH, 2);
            case GECKO_LIZARDFOLK -> new ItemStack(r < 40 ? Items.APPLE : r < 75 ? Items.GLOW_BERRIES : r < 95 ? Items.MELON_SLICE : Items.EMERALD, r < 95 ? 2 : 1);
            case CHUPACABRA_LIZARDFOLK -> r < 70 ? new ItemStack(Items.MUTTON, 32) : new ItemStack(r < 90 ? Items.BONE : Items.GOAT_HORN, r < 90 ? 3 : 1);   // half a stack
            case BLACK_TORTOISE_LIZARDFOLK -> new ItemStack(r < 45 ? Items.SALMON : r < 80 ? Items.PRISMARINE_SHARD : r < 95 ? Items.BLUE_ICE : Items.HEART_OF_THE_SEA, r < 80 ? 2 : 1);
            default -> new ItemStack(r < 40 ? Items.SLIME_BALL : r < 80 ? Items.COCOA_BEANS : Items.EMERALD);
        };
    }

    // --- combat --------------------------------------------------------------------------------------------------

    /** The two that carry a shell and hide in it. */
    private boolean shelled() {
        return switch (this.species) {
            case TURTLE_LIZARDFOLK, SNAPPING_TURTLE_LIZARDFOLK, BLACK_TORTOISE_LIZARDFOLK, RED_EARED_SLIDER_LIZARDFOLK, GALAPAGOS_TORTOISE_LIZARDFOLK,
                 BOX_TURTLE_LIZARDFOLK, MATAMATA_LIZARDFOLK -> true;
            default -> false;                                            // the softshell's leathery disc is no hiding place
        };
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
        if (this.species == GirlSpecies.BLACK_TORTOISE_LIZARDFOLK && source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {   // arrows glance off Xuanwu's shell
            if (!this.level().isClientSide) this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F);
            return false;
        }
        if (this.species == GirlSpecies.BOX_TURTLE_LIZARDFOLK && this.isRetracted() && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (!this.level().isClientSide) this.playSound(SoundEvents.SHIELD_BLOCK, 0.8F, 1.4F);   // shut tight: nothing gets in
            return false;
        }
        if (this.species == GirlSpecies.GALAPAGOS_TORTOISE_LIZARDFOLK && source.is(net.minecraft.world.damagesource.DamageTypes.CACTUS)) return false;
        if (this.isRetracted()) amount *= 0.2F;
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.shelled() && !this.level().isClientSide) {
            this.retracted = 100;
            this.setFlag(FLAG_RETRACTED, true);
            this.getNavigation().stop();
            this.setAction(Action.RETRACT);
        }
        if (hurt) this.stillTicks = 0;
        if (hurt && this.species == GirlSpecies.GECKO_LIZARDFOLK && !this.hasFlag(FLAG_TAILLESS) && this.level() instanceof ServerLevel server) this.shedTail(server);
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
            if (level.getBlockEntity(p) instanceof GirlEggBlockEntity be) be.setup(this.species.id(), this.litterSize(), father).withPaint(this.getPaint());
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
        if (this.species == GirlSpecies.KAPPA_LIZARDFOLK) this.tickDish(server);
        if (this.species == GirlSpecies.SOFTSHELL_TURTLE_LIZARDFOLK || this.species == GirlSpecies.MATAMATA_LIZARDFOLK) this.lieInWait(server);
        if (this.tailRegrow > 0 && --this.tailRegrow == 0) this.setFlag(FLAG_TAILLESS, false);
        if (this.species == GirlSpecies.CHUPACABRA_LIZARDFOLK && this.tickCount % 5 == 0) {     // looked at, she freezes and fades from sight
            boolean seen = this.getTarget() == null && this.watched(server);
            if (seen) this.getNavigation().stop();
            if (seen != this.isInvisible()) this.setInvisible(seen);
        }
        if (!this.isBaby() && this.tickCount % 10 == 0) {
            if (this.species == GirlSpecies.RED_EARED_SLIDER_LIZARDFOLK) this.watchForStrangers(server);
            if (this.species == GirlSpecies.BOX_TURTLE_LIZARDFOLK) this.keepHome(server);
        }
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

    // --- the turtles of 2026-10-25 -----------------------------------------------------------------------------

    /** Nobody she is fond of: a stranger. */
    private boolean isStranger(Player p) {
        return !p.isSpectator() && !p.isCreative() && this.getAffection(p) < com.linkyoshi90.monstergirls.entity.Orders.AFFECTION;
    }

    /** The nearest water within ten blocks, or null. */
    @Nullable
    private BlockPos nearestWater(ServerLevel level) {
        BlockPos o = this.blockPosition(), best = null;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-10, -3, -10), o.offset(10, 2, 10))) {
            if (level.getFluidState(p).is(net.minecraft.tags.FluidTags.WATER) && (best == null || p.distSqr(o) < best.distSqr(o))) best = p.immutable();
        }
        return best;
    }

    /** The slider: a stranger close by, and every slider around slides into the water at once. */
    public void watchForStrangers(ServerLevel level) {
        if (this.slideTo != null) {
            if (this.isInWater() || level.getGameTime() >= this.nextSlide - 100) this.slideTo = null;
            else this.getNavigation().moveTo(this.slideTo.getX() + 0.5, this.slideTo.getY(), this.slideTo.getZ() + 0.5, 1.5);
        }
        if (level.getGameTime() < this.nextSlide || this.isInWater()) return;
        if (level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(6), this::isStranger).isEmpty()) return;
        for (Lizardfolk s : level.getEntitiesOfClass(Lizardfolk.class, this.getBoundingBox().inflate(8),
                s -> s.species == GirlSpecies.RED_EARED_SLIDER_LIZARDFOLK && !s.isInWater() && !s.isOrderedToSit())) {
            s.slideIntoWater(level);
        }
    }

    public boolean slideIntoWater(ServerLevel level) {
        this.nextSlide = level.getGameTime() + 200;
        BlockPos water = this.nearestWater(level);
        if (water == null) return false;
        if (this.getAction() == Action.BASK) this.setAction(Action.NONE);
        this.slideTo = water;
        this.getNavigation().moveTo(water.getX() + 0.5, water.getY(), water.getZ() + 0.5, 1.5);
        this.playSound(SoundEvents.TURTLE_SHAMBLE, 1.0F, 1.3F);
        return true;
    }

    @Nullable
    public BlockPos getHome() {
        return this.home;
    }

    // --- the gecko --------------------------------------------------------------------------------------------------

    public static final int TAIL_REGROW = 3600;
    private int tailRegrow;

    /** No fall hurts a gecko. */
    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return this.species != GirlSpecies.GECKO_LIZARDFOLK && super.causeFallDamage(distance, multiplier, source);
    }

    /** She runs up walls (and anything else she bumps into). */
    @Override
    public boolean onClimbable() {
        return this.species == GirlSpecies.GECKO_LIZARDFOLK && this.horizontalCollision || super.onClimbable();
    }

    /** Hit, she sheds her tail (it wriggles a moment where it fell) and grows a new one. */
    public void shedTail(ServerLevel level) {
        this.setFlag(FLAG_TAILLESS, true);
        this.tailRegrow = TAIL_REGROW;
        level.sendParticles(new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM, new ItemStack(Items.SLIME_BALL)),
                this.getX(), this.getY() + 0.6, this.getZ(), 12, 0.3, 0.2, 0.3, 0.05);
        this.playSound(SoundEvents.SLIME_SQUISH_SMALL, 1.0F, 1.4F);
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 2, true, false));     // and bolts
    }

    /** She goes to look at an item lying near her, picks it up, turns it over and puts it back. */
    class InspectGoal extends net.minecraft.world.entity.ai.goal.Goal {
        @Nullable
        private ItemEntity item;
        private int turning;

        InspectGoal() {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Lizardfolk g = Lizardfolk.this;
            if (g.isBaby() || g.getTarget() != null || g.random.nextInt(20) != 0) return false;
            this.item = g.nearest(ItemEntity.class, 8, e -> !e.getPersistentData().getBoolean("monstergirls:inspected"));
            return this.item != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.turning > 0 || (this.item != null && this.item.isAlive());
        }

        @Override
        public void start() {
            this.turning = 0;
        }

        @Override
        public void tick() {
            Lizardfolk g = Lizardfolk.this;
            if (this.turning > 0) {                                  // turning it over in her hands
                g.getNavigation().stop();
                g.getLookControl().setLookAt(g.getX() + g.random.nextGaussian(), g.getEyeY() - 0.5, g.getZ() + g.random.nextGaussian());
                if (--this.turning == 0) {
                    ItemEntity back = g.spawnAtLocation(g.getMainHandItem());
                    if (back != null) back.getPersistentData().putBoolean("monstergirls:inspected", true);
                    g.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                }
                return;
            }
            if (this.item == null || !this.item.isAlive()) return;
            if (g.distanceToSqr(this.item) > 1.5 * 1.5) {
                g.getNavigation().moveTo(this.item, 1.2);
                return;
            }
            g.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, this.item.getItem().copy());
            this.item.discard();
            this.item = null;
            this.turning = 40;
        }

        @Override
        public void stop() {
            Lizardfolk g = Lizardfolk.this;
            if (!g.getMainHandItem().isEmpty()) {
                ItemEntity back = g.spawnAtLocation(g.getMainHandItem());
                if (back != null) back.getPersistentData().putBoolean("monstergirls:inspected", true);
                g.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
            this.item = null;
            this.turning = 0;
        }
    }

    // --- the chupacabra ----------------------------------------------------------------------------------------------

    /** A goat nobody keeps: not named, not leashed, not anyone's friend, and no fence or wall pens it in. */
    public static boolean wildGoat(LivingEntity e) {
        if (e.hasCustomName() || (e instanceof net.minecraft.world.entity.Leashable l && l.isLeashed()) || Kinship.friendOf(e) != null) return false;
        BlockPos o = e.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-2, -1, -2), o.offset(2, 1, 2))) {
            var s = e.level().getBlockState(p);
            if (s.is(BlockTags.FENCES) || s.is(BlockTags.WALLS) || s.is(BlockTags.FENCE_GATES)) return false;
        }
        return true;
    }

    /** Someone is looking straight at her. */
    private boolean watched(ServerLevel level) {
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(24), p -> !p.isSpectator())) {
            Vec3 toHer = this.getEyePosition().subtract(p.getEyePosition()).normalize();
            if (p.getViewVector(1.0F).dot(toHer) > 0.97 && p.hasLineOfSight(this)) return true;
        }
        return false;
    }

    @Override
    protected boolean takesOrders() {
        return this.species != GirlSpecies.CHUPACABRA_LIZARDFOLK;
    }

    /** The box turtle stays within HOME_RANGE of home, and shuts tight when a stranger sprints up to her. */
    private void keepHome(ServerLevel level) {
        if (this.home == null) this.home = this.blockPosition();
        if (!this.hasRestriction() && this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 64) == null) this.restrictTo(this.home, HOME_RANGE);
        if (!this.isRetracted() && !level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(4), p -> p.isSprinting() && this.isStranger(p)).isEmpty()) {
            this.shutShell();
        }
    }

    public void shutShell() {
        this.retracted = 100;
        this.setFlag(FLAG_RETRACTED, true);
        this.getNavigation().stop();
        this.setAction(Action.RETRACT);
    }

    /**
     * Still in the water she vanishes: the softshell buried in a sandy, muddy or gravel riverbed (and she bites a stranger
     * who comes within a block and a half), the matamata looking like dead leaves (and she gulps fish within four blocks).
     */
    private void lieInWait(ServerLevel level) {
        boolean still = this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-3 && this.getTarget() == null && this.isInWater();
        if (this.species == GirlSpecies.SOFTSHELL_TURTLE_LIZARDFOLK) {
            var bed = level.getBlockState(this.blockPosition().below());
            still &= bed.is(BlockTags.SAND) || bed.is(net.minecraft.world.level.block.Blocks.MUD) || bed.is(net.minecraft.world.level.block.Blocks.GRAVEL)
                    || bed.is(BlockTags.DIRT) || bed.is(net.minecraft.world.level.block.Blocks.CLAY);
        }
        this.stillTicks = still ? this.stillTicks + 1 : 0;
        boolean hide = this.stillTicks > 40;
        if (hide != this.isInvisible()) this.setInvisible(hide);
        if (!hide || this.isBaby() || this.tickCount % 10 != 0) return;
        if (this.species == GirlSpecies.SOFTSHELL_TURTLE_LIZARDFOLK) {
            for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(1.5), this::isStranger)) this.biteFromHiding(p);
        } else {
            this.gulp(level);
        }
    }

    /** Trodden on (near enough): the softshell bites and dashes off. */
    public void biteFromHiding(Player p) {
        p.hurt(this.damageSources().mobAttack(this), 6.0F);
        this.stillTicks = 0;
        this.setInvisible(false);
        this.playSound(SoundEvents.TURTLE_HURT, 1.0F, 1.4F);
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 2, true, false));
    }

    /** The matamata's gulp: the nearest fish within four blocks is sucked in. Her friend gets it, otherwise she eats it. */
    public boolean gulp(ServerLevel level) {
        LivingEntity fish = this.nearest(LivingEntity.class, 4, f -> f.isAlive()
                && (f instanceof net.minecraft.world.entity.animal.AbstractFish || com.linkyoshi90.monstergirls.compat.AlexsMobs.isFish(f)));
        if (fish == null) return false;
        ItemStack caughtStack = switch (fish) {
            case net.minecraft.world.entity.animal.Salmon s -> new ItemStack(Items.SALMON);
            case net.minecraft.world.entity.animal.TropicalFish t -> new ItemStack(Items.TROPICAL_FISH);
            case net.minecraft.world.entity.animal.Pufferfish pf -> new ItemStack(Items.PUFFERFISH);
            case net.minecraft.world.entity.animal.AbstractFish other -> new ItemStack(Items.COD);
            default -> com.linkyoshi90.monstergirls.compat.AlexsMobs.catchOf(fish);   // an Alex's Mobs catfish
        };
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.BUBBLE, fish.getX(), fish.getY(), fish.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
        fish.discard();
        this.playSound(SoundEvents.GENERIC_EAT, 0.8F, 0.7F);
        if (this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 24) != null) this.spawnAtLocation(caughtStack);
        else this.heal(3.0F);
        return true;
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
        if (this.home != null) tag.put("Home", net.minecraft.nbt.NbtUtils.writeBlockPos(this.home));
        tag.putInt("TailRegrow", this.tailRegrow);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.retracted = tag.getInt("Retracted");
        this.home = net.minecraft.nbt.NbtUtils.readBlockPos(tag, "Home").orElse(null);
        this.tailRegrow = tag.getInt("TailRegrow");
    }

    // --- the kappa's dish -----------------------------------------------------------------------------------------

    /** Two minutes out of water and rain and the water in her dish is gone. */
    public static final int DISH_DRY = 2400;
    private int dishDry;
    private final java.util.Map<java.util.UUID, Long> bowed = new java.util.HashMap<>();

    public boolean isDishDry() {
        return this.dishDry > DISH_DRY;
    }

    /** Wet she is strong; with her dish dry she is weak and slow. A player who bows to her (crouching, facing her) gets a bow back. */
    private void tickDish(ServerLevel level) {
        this.dishDry = this.isInWaterRainOrBubble() ? 0 : this.dishDry + 1;
        if (this.tickCount % 40 == 0) {
            if (this.dishDry == 0 && this.isInWater()) this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, true, false));
            else if (this.isDishDry()) {
                this.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false));
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, true, false));
            }
        }
        if (this.tickCount % 10 != 0) return;
        for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(4), p -> p.isShiftKeyDown() && !p.isSpectator())) {
            Vec3 toHer = this.position().subtract(p.position()).normalize();
            if (p.getViewVector(1.0F).dot(toHer) > 0.8 && level.getGameTime() >= this.bowed.getOrDefault(p.getUUID(), 0L)) this.bowBack(level, p);
        }
    }

    /** She bows back, as politeness demands, and spills her dish: weak for a while, and done fighting this player. */
    public void bowBack(ServerLevel level, Player p) {
        this.bowed.put(p.getUUID(), level.getGameTime() + 6000);
        this.dishDry = DISH_DRY + 1;
        if (this.getTarget() == p) this.setTarget(null);
        if (this.getLastHurtByMob() == p) this.setLastHurtByMob(null);
        this.getLookControl().setLookAt(p);
        this.addAffection(p, 1);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 16, 0.3, 0.1, 0.3, 0.1);
        this.playSound(SoundEvents.GENERIC_SPLASH, 0.6F, 1.4F);
        p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.kappa_bow", this.getDisplayName()), true);
    }
}
