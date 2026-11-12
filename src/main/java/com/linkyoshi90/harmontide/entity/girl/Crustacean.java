package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.block.GirlEggBlock;
import com.linkyoshi90.harmontide.block.GirlEggBlockEntity;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The crab family (crustacean_field_guide.html): crab, hermit crab. Neutral: whoever hits one first gets pinched
 * (Slowness for a moment); a devoted crab pinches whatever goes for her friend. The hermit crab moves into a better
 * shell: give her a nautilus shell or a shulker shell and you get her old one back.
 */
public class Crustacean extends AbstractMonsterGirl {
    public Crustacean(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, e -> {   // devoted: her friend's attacker
            Player p = this.bondedPlayer(60, 16);
            return p != null && e != p && p.getLastHurtByMob() == e;
        }));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        if (s == GirlSpecies.FLOODCRAB_CRUSTACEAN) return girlAttributes(20, 0.26, 3).add(Attributes.ARMOR, 5);
        return s == GirlSpecies.CRAB_CRUSTACEAN ? girlAttributes(24, 0.26, 5).add(Attributes.ARMOR, 6)
                : girlAttributes(18, 0.24, 4).add(Attributes.ARMOR, 4);
    }

    /** The pinch: a moment's Slowness on whatever she grabs. */
    @Override
    public boolean doHurtTarget(Entity target) {
        this.startTimedAction(Action.PINCH, 12);
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity l) l.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
        return hit;
    }

    // --- the hermit crab's shells ----------------------------------------------------------------------------------

    /** The shell item for a hermit crab's variant, or empty for the shells no item can stand for. */
    private static ItemStack shellItem(String variant) {
        return switch (variant) {
            case "nautilus" -> new ItemStack(Items.NAUTILUS_SHELL);
            case "shulker" -> new ItemStack(Items.SHULKER_SHELL);
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    @Nullable
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (this.species != GirlSpecies.HERMIT_CRAB_CRUSTACEAN || this.isBaby()) return null;
        String shell = stack.is(Items.NAUTILUS_SHELL) ? "nautilus" : stack.is(Items.SHULKER_SHELL) ? "shulker" : null;
        if (shell == null || shell.equals(this.getVariantName())) return null;
        if (!this.level().isClientSide) this.moveInto(player, stack, shell);
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /** She moves into the new shell; the old one, if it is something you can hold, goes to the player. */
    public void moveInto(Player player, ItemStack stack, String shell) {
        ItemStack old = shellItem(this.getVariantName());
        this.setVariant(shell);
        stack.consume(1, player);
        if (!old.isEmpty() && !player.getInventory().add(old)) player.drop(old, false);
        this.addAffection(player, 3);
        this.playSound(SoundEvents.ARMOR_EQUIP_TURTLE.value(), 1.0F, 1.4F);
        if (this.level() instanceof ServerLevel server) this.hearts(server, 3);
    }

    // --- food, presents, young -------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.FISHES) || stack.is(Items.KELP) || stack.is(Items.SEA_PICKLE);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.KELP) || stack.is(Items.SEA_PICKLE) ? 2 : 1;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 40 ? new ItemStack(Items.KELP, 3) : r < 70 ? new ItemStack(Items.SEA_PICKLE) : r < 95 ? new ItemStack(Items.PRISMARINE_SHARD, 2)
                : new ItemStack(Items.NAUTILUS_SHELL);
    }

    @Override
    protected int litterSize() {
        return 2 + this.random.nextInt(3);
    }

    /** A clutch in sand near the water. */
    @Override
    protected boolean giveBirth(ServerLevel level, @Nullable UUID father) {
        BlockPos o = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-5, -2, -5), o.offset(5, 2, 5))) {
            if (!canHoldBlock(level, p) || !level.getBlockState(p.below()).is(BlockTags.SAND)) continue;
            level.setBlock(p, ModBlocks.GIRL_EGG.get().defaultBlockState().setValue(GirlEggBlock.KIND, GirlEggBlock.Kind.LEATHERY), 3);
            if (level.getBlockEntity(p) instanceof GirlEggBlockEntity be) be.setup(this.species.id(), this.litterSize(), father).withPaint(this.getPaint());
            return true;
        }
        return super.giveBirth(level, father);
    }

    // --- sounds ----------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.TURTLE_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.TURTLE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.TURTLE_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.GENERIC_EAT;
    }

    // --- the floodcrab -------------------------------------------------------------------------------------------

    /** The nearest monster within 8 of her friend takes 4 and can't move for 3 seconds. 1 on a hit, 0 with none near, -1 without a friend. */
    public int pin(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        net.minecraft.world.entity.monster.Monster near = null;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(8), net.minecraft.world.entity.monster.Monster::isAlive)) {
            if (near == null || m.distanceToSqr(friend) < near.distanceToSqr(friend)) near = m;
        }
        if (near == null) return 0;
        near.hurt(this.damageSources().mobAttack(this), 4);
        near.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 6));
        return 1;
    }

    /** The lobster, below half health, sheds her shell once a day: whole again, and an exoskeleton for her friend. True if she moulted. */
    public boolean moult(long day) {
        if (this.getHealth() >= this.getMaxHealth() / 2 || this.getPersistentData().getLong("MoultDay") == day + 1) return false;
        this.getPersistentData().putLong("MoultDay", day + 1);
        this.setHealth(this.getMaxHealth());
        this.spawnAtLocation(new ItemStack(com.linkyoshi90.harmontide.registry.ModItems.EXOSKELETON.get()));
        return true;
    }

    /** The mantis shrimp punches the monster nearest her friend for 12; the shock stuns what stands within 2 of it. 1 on a hit, 0 with none, -1 without a friend. */
    public int punch(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 16);
        if (friend == null) return -1;
        net.minecraft.world.entity.monster.Monster near = null;
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, friend.getBoundingBox().inflate(6), net.minecraft.world.entity.monster.Monster::isAlive)) {
            if (near == null || m.distanceToSqr(friend) < near.distanceToSqr(friend)) near = m;
        }
        if (near == null) return 0;
        near.hurt(this.damageSources().mobAttack(this), 12);
        for (net.minecraft.world.entity.monster.Monster m : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, near.getBoundingBox().inflate(2), net.minecraft.world.entity.monster.Monster::isAlive)) m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 5));
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM, near.getX(), near.getY() + 1, near.getZ(), 1, 0, 0, 0, 0);
        return 1;
    }

    /** The starfish: her friend near her, badly hurt, gets Regeneration II. True if they did. */
    public boolean regrowth() {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 8);
        if (friend == null || friend.getHealth() > friend.getMaxHealth() / 2) return false;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 80, 1, true, true));
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.STARFISH_CRUSTACEAN && !this.level().isClientSide && !this.isBaby() && this.tickCount % 41 == 0) this.regrowth();
        if (this.species == GirlSpecies.MANTIS_SHRIMP_CRUSTACEAN && this.level() instanceof net.minecraft.server.level.ServerLevel reefs && !this.isBaby() && this.tickCount % 81 == 0) this.punch(reefs);
        if (this.species == GirlSpecies.LOBSTER_CRUSTACEAN && !this.level().isClientSide && !this.isBaby() && this.tickCount % 21 == 0) this.moult(this.level().getDayTime() / 24000L);
        if (this.species == GirlSpecies.SPIDER_CRAB_CRUSTACEAN && this.level() instanceof net.minecraft.server.level.ServerLevel clawed && !this.isBaby() && this.tickCount % 60 == 0) this.pin(clawed);
        if (this.species == GirlSpecies.FLOODCRAB_CRUSTACEAN && !this.isBaby() && this.tickCount % 400 == 0 && this.level() instanceof ServerLevel server) this.puddle(server);
    }

    /** Befriended: a block of dirt or grass beside water within 4 blocks of her turns to mud. Where, or null. */
    @Nullable
    public BlockPos puddle(ServerLevel level) {
        if (this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 24) == null || !level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING)) return null;
        java.util.List<BlockPos> spots = new java.util.ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(this.blockPosition().offset(-4, -2, -4), this.blockPosition().offset(4, 1, 4))) {
            var s = level.getBlockState(p);
            if (!(s.is(net.minecraft.world.level.block.Blocks.DIRT) || s.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT) || s.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK))) continue;
            for (net.minecraft.core.Direction d : net.minecraft.core.Direction.values()) {
                if (d != net.minecraft.core.Direction.DOWN && level.getFluidState(p.relative(d)).is(net.minecraft.tags.FluidTags.WATER)) {
                    spots.add(p.immutable());
                    break;
                }
            }
        }
        if (spots.isEmpty()) return null;
        BlockPos at = spots.get(this.random.nextInt(spots.size()));
        level.setBlock(at, net.minecraft.world.level.block.Blocks.MUD.defaultBlockState(), 3);
        level.playSound(null, at, net.minecraft.sounds.SoundEvents.MUD_PLACE, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
        this.startTimedAction(Action.PINCH, 12);
        return at;
    }
}
