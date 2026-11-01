package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.registry.ModItems;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * The karasu-tengu, the crow tengu of the mountains (HARMONTIDE_CONCEPTS.md, fourth wave). Proud and neutral: she
 * leaves you be unless you hurt her. She lives so high in the sky that you mostly meet her when she comes down to
 * drink at water. Her feather fan does little harm but throws whatever it hits far away, out of her territory. Told
 * to patrol, she scans the ground for hostile mobs (creepers and endermen too) and blows them off her ground.
 */
public class TenguHarpy extends AbstractHarpy {
    /** Affection at which she counts you a friend. */
    public static final int FRIEND = com.linkyoshi90.monstergirls.entity.Orders.AFFECTION;
    /** How far above the ground she soars. */
    public static final int SKY_HEIGHT = 28;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(TenguHarpy.class, EntityDataSerializers.BYTE);

    /** Texture variants (tools/gen_tengu_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        KARASU("karasu"), KONOHA("konoha"), SHIRO("shiro"), AKA("aka"), KURAMA("kurama"), SKY_ACE("sky_ace");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return KARASU;
        }
    }

    public TenguHarpy(EntityType<? extends TenguHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.7)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    /** Mountain tops: any solid ground in daylight (the biome modifier picks the peaks). */
    public static boolean checkTenguHarpySpawnRules(EntityType<TenguHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
                                                    RandomSource random) {
        return level.getBlockState(pos.below()).isSolid() && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, (byte) 0);
    }

    public Variant getVariant() {
        return Variant.values()[this.entityData.get(DATA_VARIANT)];
    }

    public void setVariant(Variant variant) {
        this.entityData.set(DATA_VARIANT, (byte) variant.ordinal());
    }

    public String getVariantSuffix() {
        return this.getVariant().ordinal() == 0 ? "" : "_" + this.getVariant().getSerializedName();
    }

    @Override
    public void plainVariant() {
        this.setVariant(Variant.values()[0]);
    }

    @Override
    public void randomizeVariant() {
        this.setVariant(Variant.values()[this.random.nextInt(Variant.KURAMA.ordinal() + 1)]);   // the homage look is never rolled in the wild
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                                                   MobSpawnType spawnType, @Nullable net.minecraft.world.entity.SpawnGroupData group) {
        if (!this.hasVariantFromData()) this.setVariant(this.random.nextInt(3) == 0 ? Variant.values()[1 + this.random.nextInt(4)] : Variant.KARASU);
        return super.finalizeSpawn(level, difficulty, spawnType, group);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Variant", this.getVariant().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(Variant.byName(tag.getString("Variant")));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.4, true) {
            @Override
            protected void checkAndPerformAttack(LivingEntity target) {
                if (this.canPerformAttack(target)) TenguHarpy.this.startTimedAction(Action.STRIKING, 10);
                super.checkAndPerformAttack(target);
            }
        });
        this.goalSelector.addGoal(6, new SkyGoal());
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 10, false, false, this::trespasses));
    }

    // --- her territory ---------------------------------------------------------------------------------------------

    public boolean isPatrolling() {
        return this.getOrders().command(this) == com.linkyoshi90.monstergirls.entity.Orders.Command.PATROL;
    }

    /** The middle of what she guards: her patrol spot, or wherever she is. */
    public Vec3 territoryCentre() {
        BlockPos anchor = this.getOrders().anchor();
        return this.isPatrolling() && anchor != null ? Vec3.atBottomCenterOf(anchor) : this.position();
    }

    /** On patrol, any hostile mob (creepers and endermen included) on her ground or near its edge. */
    public boolean trespasses(LivingEntity mob) {
        if (!this.isPatrolling()) return false;
        double r = com.linkyoshi90.monstergirls.entity.Orders.PATROL_RADIUS + 8;
        return mob.distanceToSqr(this.territoryCentre()) < r * r;
    }

    /** The fan: a little damage and a great gust that throws the target away from the middle of her territory. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) this.gust(target);
        return hit;
    }

    public void gust(Entity target) {
        Vec3 away = target.position().subtract(this.territoryCentre()).multiply(1, 0, 1);
        if (away.lengthSqr() < 0.01) away = target.position().subtract(this.position()).multiply(1, 0, 1);
        if (away.lengthSqr() < 0.01) away = new Vec3(1, 0, 0);
        away = away.normalize().scale(1.8);
        target.push(away.x, 0.55, away.z);
        target.hurtMarked = true;
        this.playSound(SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), 1.0F, 1.2F);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.GUST, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 1, 0, 0, 0, 0);
        }
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return false;
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.monstergirls.tengu." + (a >= FRIEND ? "friend" : a > 0 ? "respects" : "wary"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 45 ? new ItemStack(Items.FEATHER, 3) : r < 75 ? new ItemStack(Items.PINK_PETALS, 2) : r < 95 ? new ItemStack(Items.FLINT, 2)
                : new ItemStack(Items.EMERALD);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.TENGU_EGG.get());
    }

    @Override
    protected String species() {
        return "tengu_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case STRIKING -> "strike";
            case DRINKING -> "drink";
            case SINGING -> "sing";
            default -> null;
        };
    }

    @Override
    protected boolean actionShowsInFlight(Action action) {
        return action == Action.STRIKING || super.actionShowsInFlight(action);
    }

    // --- sounds (the crow's voice, a little deeper) ----------------------------------------------------------------

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CROW_CAW.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CROW_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CROW_DEATH.get();
    }

    @Override
    protected SoundEvent getEatSound() {
        return ModSounds.CROW_EAT.get();
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.8F;
    }

    // --- the sky -----------------------------------------------------------------------------------------------------

    /**
     * Her day when nobody gives her orders: soar in slow circles far above the ground, now and then glide down to the
     * nearest water, kneel at its edge and drink, then climb back up. The drink is when you meet her.
     */
    class SkyGoal extends Goal {
        private enum Phase { SOARING, DESCENDING, DRINKING }

        private Phase phase = Phase.SOARING;
        private Vec3 centre = Vec3.ZERO;
        private double angle;
        private int ticks;
        private int length;
        @Nullable
        private BlockPos bank;

        SkyGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            TenguHarpy t = TenguHarpy.this;
            return !t.isBaby() && t.getTarget() == null && !t.isOrderedToSit()
                    && t.getOrders().command(t) == com.linkyoshi90.monstergirls.entity.Orders.Command.FREE_ROAM;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            this.soar();
        }

        @Override
        public void stop() {
            if (TenguHarpy.this.getAction() == Action.DRINKING) TenguHarpy.this.setAction(Action.NONE);
            this.bank = null;
        }

        private void soar() {
            this.phase = Phase.SOARING;
            this.ticks = 0;
            this.length = 1200 + TenguHarpy.this.random.nextInt(2400);
            this.centre = TenguHarpy.this.position();
            this.angle = TenguHarpy.this.random.nextDouble() * Math.PI * 2;
        }

        @Override
        public void tick() {
            TenguHarpy t = TenguHarpy.this;
            this.ticks++;
            switch (this.phase) {
                case SOARING -> {
                    this.angle += 0.025;
                    double ground = t.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(this.centre)).getY();
                    t.getMoveControl().setWantedPosition(this.centre.x + Math.cos(this.angle) * 20, ground + SKY_HEIGHT,
                            this.centre.z + Math.sin(this.angle) * 20, 1.0);
                    if (this.ticks > this.length && this.ticks % 40 == 0) {
                        this.bank = findWaterEdge(t.level(), t.blockPosition(), t.random);
                        if (this.bank != null) {
                            this.phase = Phase.DESCENDING;
                            this.ticks = 0;
                        }
                    }
                }
                case DESCENDING -> {
                    Vec3 spot = Vec3.atBottomCenterOf(this.bank);
                    if (t.onGround() && t.distanceToSqr(spot) < 2.5 * 2.5) {
                        this.phase = Phase.DRINKING;
                        this.ticks = 0;
                        t.getNavigation().stop();
                        t.setAction(Action.DRINKING);
                    } else if (this.ticks > 900) {
                        this.soar();                                      // could not get down there
                    } else {
                        t.getMoveControl().setWantedPosition(spot.x, spot.y + Math.min(8.0, Math.sqrt(t.distanceToSqr(spot.x, t.getY(), spot.z)) * 0.4),
                                spot.z, 1.0);
                    }
                }
                case DRINKING -> {
                    t.getLookControl().setLookAt(spot(this.bank));
                    if (this.ticks > 200 + t.random.nextInt(200)) {
                        t.setAction(Action.NONE);
                        this.soar();
                    }
                }
            }
        }

        private Vec3 spot(BlockPos bank) {
            for (BlockPos p : BlockPos.betweenClosed(bank.offset(-1, -1, -1), bank.offset(1, 0, 1))) {
                if (TenguHarpy.this.level().getFluidState(p).is(FluidTags.WATER)) return Vec3.atCenterOf(p);
            }
            return Vec3.atCenterOf(bank);
        }

        public boolean isDrinking() {
            return this.phase == Phase.DRINKING;
        }
    }

    /** A dry spot to stand on right beside open water (water at the surface), within 48 blocks, or null. */
    @Nullable
    public static BlockPos findWaterEdge(Level level, BlockPos from, RandomSource random) {
        for (int i = 0; i < 24; i++) {
            BlockPos column = from.offset(random.nextInt(97) - 48, 0, random.nextInt(97) - 48);
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column);
            if (!level.getFluidState(top.below()).is(FluidTags.WATER)) continue;
            for (BlockPos p : new BlockPos[]{top.north(), top.south(), top.east(), top.west()}) {
                BlockPos stand = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p);
                if (Math.abs(stand.getY() - top.getY()) <= 1 && level.getFluidState(stand.below()).isEmpty()
                        && level.getBlockState(stand.below()).isSolid()) return stand;
            }
        }
        return null;
    }
}
