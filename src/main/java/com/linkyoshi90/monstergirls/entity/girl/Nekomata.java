package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The cat family (nekomata_field_guide.html): serval, tiger, lioness, snow leopard, cheetah, lynx. A cat chooses
 * you: patience (sneaking or standing still near her) is the bond action, chasing her sets you back, and she only
 * tolerates so much petting. Cats nap on warm blocks by day, stalk small prey, leave presents, and after ten days at
 * full devotion become a true nekomata: a second tail, spirit fire and a spare life.
 */
public class Nekomata extends AbstractMonsterGirl {
    private static final ResourceLocation BURST = MonsterGirlsMod.id("cheetah_burst");
    private static final int PATIENCE_COOLDOWN = 1200;
    private static final long SPARE_LIFE_COOLDOWN = 5L * 24000L;

    private final Map<UUID, Long> patience = new HashMap<>();
    private final Map<UUID, Integer> strokes = new HashMap<>();
    private long strokeWindow;
    private long lastRevive = -SPARE_LIFE_COOLDOWN;
    private int burstTicks;
    private int tiredTicks;
    private int watchedTicks;

    public Nekomata(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        if (species == GirlSpecies.OCELOT_NEKOMATA) this.navigation = this.createNavigation(level);   // the first one was made before species was set
        boolean tiger = species == GirlSpecies.TIGER_NEKOMATA;
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.0, 1.4,
                p -> p instanceof Player pl && pl.isSprinting() && this.isCalm() && this.getAffection(pl) < 10 && !pl.isSpectator()));
        // she picks her prey and creeps up on it, belly low, before the pounce
        this.goalSelector.addGoal(1, new StalkGoal(this, 0.55, 3.5, false, t -> !this.isAggressiveSprint()));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true) {
            @Override
            protected void checkAndPerformAttack(LivingEntity target) {
                if (this.canPerformAttack(target)) Nekomata.this.startTimedAction(Action.POUNCE, 16);
                super.checkAndPerformAttack(target);
            }
        });
        this.goalSelector.addGoal(4, new PatienceGoal());
        this.goalSelector.addGoal(6, new NapGoal());
        this.goalSelector.addGoal(8, tiger ? new RandomStrollGoal(this, 0.9) : new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Rabbit.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Silverfish.class, true));
        if (species != GirlSpecies.LYNX_NEKOMATA) {
            // only a wild cat hunts chickens: one who is fond of someone leaves the farm alone
            this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Chicken.class, true, c -> this.bestAffection() < 10));
        }
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case TIGER_NEKOMATA -> girlAttributes(30, 0.3, 6);
            case LIONESS_NEKOMATA -> girlAttributes(26, 0.3, 5);
            case CHEETAH_NEKOMATA -> girlAttributes(18, 0.34, 3);
            case SNOW_LEOPARD_NEKOMATA -> girlAttributes(22, 0.3, 4).add(Attributes.JUMP_STRENGTH, 0.62);
            case SERVAL_NEKOMATA -> girlAttributes(16, 0.3, 3).add(Attributes.JUMP_STRENGTH, 0.66);
            case OCELOT_NEKOMATA -> girlAttributes(16, 0.32, 3);
            case BLAZE_CAT_NEKOMATA -> girlAttributes(24, 0.32, 5);
            case WATERLIL_NEKOMATA -> girlAttributes(16, 0.3, 2);
            case CHESHIRE_NEKOMATA -> girlAttributes(20, 0.32, 3);
            default -> girlAttributes(18, 0.3, 3);
        };
    }

    /** Wild cats are hunters first: a stranger is prey until she has been given meat. */
    @Override
    protected boolean wildHostile() {
        return true;
    }

    /** One wild cat in ten was born a nekomata: her tail is split in two from the start. */
    @Override
    protected int rollTails(net.minecraft.util.RandomSource random) {
        return random.nextInt(10) == 0 ? 2 : 1;
    }

    @Override
    protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level) {
        return this.species == GirlSpecies.OCELOT_NEKOMATA ? new net.minecraft.world.entity.ai.navigation.WallClimberNavigation(this, level) : super.createNavigation(level);
    }

    /** The ocelot climbs tree trunks (and walls) like the gray fox. */
    @Override
    public boolean onClimbable() {
        return (this.species == GirlSpecies.OCELOT_NEKOMATA && this.horizontalCollision) || super.onClimbable();
    }

    /** The cheetah's sprint burst skips the stalk: she simply runs her prey down. */
    private boolean isAggressiveSprint() {
        return this.burstTicks > 0;
    }

    // --- food, petting, presents ---------------------------------------------------------------------------------

    // --- the cheshire cat (her fading is the renderer's) -----------------------------------------------------------------

    /** Left more than 14 blocks behind, she blinks back to her friend's side. True if she did. */
    public boolean blinkToFriend(ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 96);
        if (friend == null || this.isOrderedToSit() || this.isPassenger() || this.distanceToSqr(friend) < 14 * 14) return false;
        for (int i = 0; i < 12; i++) {
            BlockPos p = friend.blockPosition().offset(this.random.nextInt(5) - 2, this.random.nextInt(3) - 1, this.random.nextInt(5) - 2);
            if (!level.getBlockState(p.below()).isSolid() || !level.getBlockState(p).isAir() || !level.getBlockState(p.above()).isAir()) continue;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH, this.getX(), this.getY() + 1, this.getZ(), 12, 0.3, 0.6, 0.3, 0.0);
            this.teleportTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
            this.getNavigation().stop();
            this.playSound(SoundEvents.CAT_PURR, 1.0F, 1.2F);
            return true;
        }
        return false;
    }

    // --- the waterlil ----------------------------------------------------------------------------------------------------

    /** The lily pads she has grown, and when each fades. */
    private final java.util.Map<BlockPos, Long> pads = new java.util.HashMap<>();

    /** Lily pads on the water under and ahead of this walker; true if one grew. */
    public boolean growPads(ServerLevel level, net.minecraft.world.entity.LivingEntity walker) {
        Vec3 step = walker.getDeltaMovement().multiply(1, 0, 1);
        BlockPos feet = walker.blockPosition();
        boolean grew = false;
        for (BlockPos at : new BlockPos[]{feet, step.lengthSqr() > 1.0E-4 ? BlockPos.containing(walker.position().add(step.normalize())) : feet}) {
            BlockPos water = at.below();
            if (level.getFluidState(water).isSourceOfType(net.minecraft.world.level.material.Fluids.WATER) && level.getBlockState(at).isAir()) {
                level.setBlock(at, net.minecraft.world.level.block.Blocks.LILY_PAD.defaultBlockState(), 3);
                this.pads.put(at.immutable(), level.getGameTime() + 200);
                grew = true;
            }
        }
        return grew;
    }

    private void tickPads(ServerLevel level) {
        this.growPads(level, this);
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 6);
        if (friend != null) this.growPads(level, friend);
        if (this.tickCount % 20 == 0) {                              // the old ones fade, unless someone stands on them
            this.pads.entrySet().removeIf(e -> {
                if (level.getGameTime() < e.getValue()) return false;
                BlockPos p = e.getKey();
                if (!level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, new net.minecraft.world.phys.AABB(p).inflate(0.5)).isEmpty()) return false;
                if (level.getBlockState(p).is(net.minecraft.world.level.block.Blocks.LILY_PAD)) level.removeBlock(p, false);
                return true;
            });
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel level) {
            for (BlockPos p : this.pads.keySet()) if (level.getBlockState(p).is(net.minecraft.world.level.block.Blocks.LILY_PAD)) level.removeBlock(p, false);
            this.pads.clear();
        }
        super.remove(reason);
    }

    // --- the blaze cat --------------------------------------------------------------------------------------------------

    @Override
    public boolean fireImmune() {
        return this.species == GirlSpecies.BLAZE_CAT_NEKOMATA || super.fireImmune();
    }

    /** Her burning paws set whatever she hits alight. */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && this.species == GirlSpecies.BLAZE_CAT_NEKOMATA) target.igniteForSeconds(4);
        return hit;
    }

    /** Campfires and candles near her light up; her friend beside her takes no harm from fire. */
    public int kindle(ServerLevel level) {
        int lit = 0;
        BlockPos o = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-4, -2, -4), o.offset(4, 2, 4))) {
            var s = level.getBlockState(p);
            if (s.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)
                    && (s.getBlock() instanceof net.minecraft.world.level.block.CampfireBlock || s.getBlock() instanceof net.minecraft.world.level.block.AbstractCandleBlock)
                    && !s.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)
                    && !(s.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)
                    && s.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED))) {
                level.setBlock(p, s.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, true), 3);
                lit++;
            }
        }
        Player friend = this.bondedPlayer(com.linkyoshi90.monstergirls.entity.Orders.AFFECTION, 8);
        if (friend != null) friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 100, 0, true, false));
        return lit;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        if (this.species == GirlSpecies.BLAZE_CAT_NEKOMATA && stack.is(Items.BLAZE_POWDER)) return true;
        return stack.is(ItemTags.FISHES) || stack.is(Items.CHICKEN) || stack.is(Items.RABBIT) || stack.is(Items.COOKED_CHICKEN)
                || stack.is(Items.COOKED_RABBIT) || stack.is(Items.MILK_BUCKET);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(ItemTags.FISHES) ? 2 : super.foodValue(player, stack);
    }

    /** Every cat tolerates only so many strokes in a row (more, the fonder she is); past that she swats. */
    @Override
    protected int petValue(Player player) {
        long now = this.level().getGameTime();
        if (now > this.strokeWindow) {
            this.strokes.clear();
            this.strokeWindow = now + 200;
        }
        int n = this.strokes.merge(player.getUUID(), 1, Integer::sum);
        int tolerance = 3 + this.getAffection(player) / 20;
        if (n > tolerance) {
            this.startTimedAction(Action.HISS, 30);
            this.playSound(SoundEvents.CAT_HISS, 1.0F, this.getVoicePitch());
            player.displayClientMessage(Component.translatable("message.monstergirls.swat", this.getDisplayName()), true);
            this.addAffection(player, -1);
            if (this.getAffection(player) < BED_AFFECTION) player.hurt(this.damageSources().mobAttack(this), 0.5F);
            this.strokes.put(player.getUUID(), 0);
            return 0;
        }
        if (this.level() instanceof ServerLevel server && this.getAffection(player) >= 10) {
            server.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + this.getBbHeight() + 0.3, this.getZ(), 1, 0.2, 0.1, 0.2, 0.0);
        }
        return 1;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case TIGER_NEKOMATA -> new ItemStack(this.random.nextBoolean() ? Items.COD : Items.SALMON);
            case CHEETAH_NEKOMATA, LYNX_NEKOMATA -> new ItemStack(r < 50 ? Items.RABBIT : r < 85 ? Items.RABBIT_HIDE : Items.RABBIT_FOOT);
            case OCELOT_NEKOMATA -> new ItemStack(r < 45 ? Items.COCOA_BEANS : r < 80 ? Items.FEATHER : Items.MELON_SLICE, r < 45 ? 3 : 1);
            case BLAZE_CAT_NEKOMATA -> new ItemStack(r < 50 ? Items.BLAZE_POWDER : r < 85 ? Items.MAGMA_CREAM : Items.BLAZE_ROD, r < 50 ? 2 : 1);
            default -> new ItemStack(r < 30 ? Items.FEATHER : r < 50 ? Items.RABBIT_HIDE : r < 70 ? Items.STRING : r < 85 ? Items.CHICKEN : r < 97 ? Items.RABBIT : Items.PHANTOM_MEMBRANE);
        };
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    private boolean big() {
        return this.species == GirlSpecies.TIGER_NEKOMATA || this.species == GirlSpecies.LIONESS_NEKOMATA || this.species == GirlSpecies.SNOW_LEOPARD_NEKOMATA;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.getAction() == Action.LYING ? SoundEvents.CAT_PURR : SoundEvents.CAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAT_DEATH;
    }

    public SoundEvent getEatSound() {
        return SoundEvents.CAT_EAT;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * (this.big() ? 0.75F : 1.0F);
    }

    // --- relationships & young -----------------------------------------------------------------------------------

    @Override
    protected boolean hasTrueForm() {
        return true;
    }

    @Override
    protected int litterSize() {
        return 2 + this.random.nextInt(2);
    }

    /** The litter is born in a den: the darkest roofed nook within reach, or where she is. */
    @Override
    protected boolean giveBirth(ServerLevel level, @Nullable UUID father) {
        BlockPos den = this.blockPosition();
        int best = 99;
        for (BlockPos p : BlockPos.betweenClosed(den.offset(-8, -2, -8), den.offset(8, 2, 8))) {
            if (!canHoldBlock(level, p) || level.canSeeSky(p)) continue;
            int light = level.getMaxLocalRawBrightness(p);
            if (light < best) {
                best = light;
                den = p.immutable();
            }
        }
        for (int i = 0; i < this.litterSize(); i++) this.spawnYoung(level, den, father);
        return true;
    }

    /** Waking up beside her: a purring cat gives Regeneration; the snow leopard's tail also Resistance. */
    @Override
    public void onNightShared(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0));
        if (this.species == GirlSpecies.SNOW_LEOPARD_NEKOMATA) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0));
            player.setTicksFrozen(0);
        }
    }

    /** A true nekomata has a spare life: once every five days a killing blow leaves her at half health. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && this.isTrueForm() && amount >= this.getHealth()
                && this.level().getGameTime() - this.lastRevive >= SPARE_LIFE_COOLDOWN && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.lastRevive = this.level().getGameTime();
            this.setHealth(this.getMaxHealth() / 2);
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1, this.getZ(), 40, 0.5, 0.8, 0.5, 0.05);
            }
            this.playSound(SoundEvents.TOTEM_USE, 0.6F, 1.4F);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return this.species != GirlSpecies.SNOW_LEOPARD_NEKOMATA && super.causeFallDamage(Math.max(0, distance - 3), multiplier, source);
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.BLAZE_CAT_NEKOMATA && this.level() instanceof ServerLevel fire && this.tickCount % 40 == 0 && !this.isBaby()) this.kindle(fire);
        if (this.species == GirlSpecies.WATERLIL_NEKOMATA && this.level() instanceof ServerLevel pond && this.tickCount % 4 == 0) this.tickPads(pond);
        if (this.species == GirlSpecies.CHESHIRE_NEKOMATA && this.level() instanceof ServerLevel wonder && this.tickCount % 20 == 0) this.blinkToFriend(wonder);
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.isTrueForm() && this.tickCount % 20 == 0 && !server.isDay()) {
            server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getRandomX(0.6), this.getY() + this.getBbHeight() * this.random.nextDouble(), this.getRandomZ(0.6), 1, 0, 0.02, 0, 0);
        }
        switch (this.species) {
            case SERVAL_NEKOMATA -> this.earScan(server);
            case TIGER_NEKOMATA -> this.tigerTick(server);
            case SNOW_LEOPARD_NEKOMATA -> this.warmTail();
            case CHEETAH_NEKOMATA -> this.cheetahTick();
            case LYNX_NEKOMATA -> this.lynxTick();
            default -> {
            }
        }
    }

    /** The serval hears what you cannot: she hisses towards the nearest monster within 24 blocks, even through stone. */
    private void earScan(ServerLevel level) {
        if (this.tickCount % 101 != 0 || this.bondedPlayer(BED_AFFECTION, 16) == null) return;
        Monster m = level.getNearestEntity(Monster.class, net.minecraft.world.entity.ai.targeting.TargetingConditions.forNonCombat().ignoreLineOfSight(),
                this, this.getX(), this.getY(), this.getZ(), this.getBoundingBox().inflate(24));
        if (m == null) return;
        this.getLookControl().setLookAt(m);
        this.startTimedAction(Action.HISS, 30);
        this.playSound(SoundEvents.CAT_HISS, 0.8F, 1.3F);
    }

    /** The tiger's roar sends monsters near her (or near her favourite) running for a moment. */
    private void tigerTick(ServerLevel level) {
        if (this.tickCount % 201 != 0) return;
        boolean threat = this.getTarget() instanceof Enemy;
        Player p = this.bondedPlayer(BED_AFFECTION, 16);
        if (!threat && p != null) threat = !level.getEntitiesOfClass(Monster.class, p.getBoundingBox().inflate(8), m -> m.getTarget() == p).isEmpty();
        if (!threat) return;
        this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.5F);
        this.startTimedAction(Action.HISS, 30);
        for (Monster m : level.getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(12))) {
            m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
            m.setTarget(null);
        }
    }

    /** The snow leopard's tail: a favourite close by does not freeze. */
    private void warmTail() {
        if (this.tickCount % 20 != 0) return;
        Player p = this.bondedPlayer(BED_AFFECTION, 4);
        if (p != null) p.setTicksFrozen(0);
    }

    /** Five-second bursts at double speed, then she has to rest; she paces a sprinting favourite (Speed II). */
    private void cheetahTick() {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        boolean chasing = this.getTarget() != null || this.isSprinting();
        if (this.tiredTicks > 0) {
            this.tiredTicks--;
        } else if (chasing && this.burstTicks < 100) {
            this.burstTicks++;
            if (speed != null && !speed.hasModifier(BURST)) speed.addTransientModifier(new AttributeModifier(BURST, 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        } else if (this.burstTicks >= 100) {
            this.burstTicks = 0;
            this.tiredTicks = 100;
        }
        if ((this.tiredTicks > 0 || !chasing) && speed != null && speed.hasModifier(BURST)) speed.removeModifier(BURST);
        if (this.tickCount % 20 == 0) {
            Player p = this.bondedPlayer(BED_AFFECTION, 8);
            if (p != null && p.isSprinting()) p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, true, false));
        }
    }

    public boolean isBursting() {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.hasModifier(BURST);
    }

    /** Watched too long by someone she does not know, the lynx slips away. */
    private void lynxTick() {
        if (this.tickCount % 10 != 0 || this.isOrderedToSit()) return;
        Player watcher = null;
        for (Player p : this.level().players()) {
            if (this.getAffection(p) >= 10 || p.distanceToSqr(this) > 16 * 16 || !p.hasLineOfSight(this)) continue;
            Vec3 look = p.getViewVector(1.0F).normalize();
            Vec3 to = this.position().subtract(p.getEyePosition()).normalize();
            if (look.dot(to) > 0.96) watcher = p;
        }
        this.watchedTicks = watcher == null ? 0 : this.watchedTicks + 10;
        if (watcher != null && this.watchedTicks > 100 && this.getNavigation().isDone()) {
            Vec3 away = this.position().add(this.position().subtract(watcher.position()).normalize().scale(12));
            this.getNavigation().moveTo(away.x, away.y, away.z, 1.3);
            this.watchedTicks = 0;
        }
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("LastRevive", this.lastRevive);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("LastRevive")) this.lastRevive = tag.getLong("LastRevive");
    }

    // --- goals ---------------------------------------------------------------------------------------------------

    /** A cat chooses you: stay still (or sneak) near her and she comes over and headbutts you (+1, once a minute). */
    class PatienceGoal extends Goal {
        private Player player;
        private int ticks;

        PatienceGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (Nekomata.this.tickCount % 23 != 0 || Nekomata.this.getTarget() != null || Nekomata.this.isBaby()) return false;
            long now = Nekomata.this.level().getGameTime();
            for (Player p : Nekomata.this.level().players()) {
                if (p.isSpectator() || p.distanceToSqr(Nekomata.this) > 8 * 8) continue;
                boolean still = p.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4 || p.isShiftKeyDown();
                if (!still || now < Nekomata.this.patience.getOrDefault(p.getUUID(), 0L)) continue;
                this.player = p;
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.player != null && this.player.isAlive() && this.ticks < 200 && Nekomata.this.getTarget() == null;
        }

        @Override
        public void start() {
            this.ticks = 0;
        }

        @Override
        public void tick() {
            this.ticks++;
            Nekomata.this.getLookControl().setLookAt(this.player);
            if (Nekomata.this.distanceToSqr(this.player) > 4.0) {
                if (this.ticks % 10 == 1) Nekomata.this.getNavigation().moveTo(this.player, 0.8);
                return;
            }
            Nekomata.this.getNavigation().stop();
            Nekomata.this.patience.put(this.player.getUUID(), Nekomata.this.level().getGameTime() + PATIENCE_COOLDOWN);
            Nekomata.this.addAffection(this.player, 1);
            Nekomata.this.startTimedAction(Action.GROOM, 40);
            if (Nekomata.this.level() instanceof ServerLevel server) Nekomata.this.hearts(server, 3);
            Nekomata.this.playSound(SoundEvents.CAT_PURREOW, 1.0F, Nekomata.this.getVoicePitch());
            this.ticks = 999;
        }

        @Override
        public void stop() {
            this.player = null;
        }
    }

    /** By day she finds a warm spot (wool, a carpet, a bed, a lit furnace) and naps there; lionesses nap the longest. */
    class NapGoal extends Goal {
        private BlockPos spot;
        private int ticks;
        private int length;

        NapGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            Level level = Nekomata.this.level();
            if (!level.isDay() || Nekomata.this.getTarget() != null || Nekomata.this.tickCount % 41 != 0) return false;
            if (Nekomata.this.random.nextInt(Nekomata.this.species == GirlSpecies.LIONESS_NEKOMATA ? 2 : 6) != 0) return false;
            BlockPos origin = Nekomata.this.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(origin.offset(-8, -2, -8), origin.offset(8, 2, 8))) {
                if (isWarm(level.getBlockState(p)) && level.getBlockState(p.above()).getCollisionShape(level, p.above()).isEmpty()) {
                    this.spot = p.immutable();
                    return true;
                }
            }
            this.spot = Nekomata.this.blockPosition().below();
            return Nekomata.this.random.nextInt(4) == 0;              // no warm spot: a nap wherever she is
        }

        private boolean isWarm(BlockState s) {
            return s.is(BlockTags.WOOL) || s.is(BlockTags.WOOL_CARPETS) || s.is(BlockTags.BEDS)
                    || (s.getBlock() instanceof AbstractFurnaceBlock && s.getValue(AbstractFurnaceBlock.LIT));
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.length = (Nekomata.this.species == GirlSpecies.LIONESS_NEKOMATA ? 1600 : 600) + Nekomata.this.random.nextInt(800);
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks < this.length && Nekomata.this.getTarget() == null && Nekomata.this.level().isDay() && Nekomata.this.hurtTime == 0;
        }

        @Override
        public void tick() {
            this.ticks++;
            Vec3 c = Vec3.atBottomCenterOf(this.spot.above());
            if (Nekomata.this.distanceToSqr(c) > 1.5) {
                if (this.ticks % 20 == 1) Nekomata.this.getNavigation().moveTo(c.x, c.y, c.z, 0.9);
                if (this.ticks > 200) this.spot = Nekomata.this.blockPosition().below();
                return;
            }
            Nekomata.this.getNavigation().stop();
            if (Nekomata.this.getAction() == Action.NONE) Nekomata.this.setAction(Action.LYING);
        }

        @Override
        public void stop() {
            if (Nekomata.this.getAction() == Action.LYING) Nekomata.this.setAction(Action.NONE);
        }
    }

    /** The lioness' pride: every lioness of the pride who loves this player joins in on their fight. */
    public static void rallyPride(Player player, LivingEntity enemy) {
        if (enemy instanceof Nekomata || enemy == null) return;
        for (Nekomata n : player.level().getEntitiesOfClass(Nekomata.class, player.getBoundingBox().inflate(24),
                n -> n.species == GirlSpecies.LIONESS_NEKOMATA && n.getAffection(player) >= BED_AFFECTION && !n.isBaby() && !n.isOrderedToSit())) {
            if (enemy instanceof Player other && n.getAffection(other) >= BED_AFFECTION) continue;
            n.setTarget(enemy);
        }
    }

    public static boolean isNekomata(@Nullable Entity e) {
        return e instanceof Nekomata;
    }
}
