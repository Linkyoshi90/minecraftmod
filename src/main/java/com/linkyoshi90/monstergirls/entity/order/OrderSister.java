package com.linkyoshi90.monstergirls.entity.order;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.block.RealmGrassBlock;
import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Dairy;
import com.linkyoshi90.monstergirls.entity.DemonRealm;
import com.linkyoshi90.monstergirls.entity.Monsterization;
import com.linkyoshi90.monstergirls.entity.ThrownHolyWater;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.item.HolyWaterItem;
import com.linkyoshi90.monstergirls.registry.ModGirls;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

/**
 * A sister of the Order (entity/order/Order): a sworn woman of the faith who hunts the Demon Realm's own. A knight (sword),
 * an inquisitrix (searing holy water flasks, and she blesses the ground she walks: realm grass turns back under her) or a
 * captain (sword and shield, the Order's banner on her helm).
 * <p>
 * They purge: realm grass they stand on turns back ({@link #bless}), a possessed creature they reach is freed, not hurt
 * ({@link #doHurtTarget}). They hunt devils, girls the realm changed and players the Order has marked (Order#hunts);
 * ordinary wild girls they leave alone.
 * <p>
 * Faith: a realm silver blade ({@link #shake}) and the realm's air in the dark wear it down. With none left she kneels, drops
 * her sword and fights no more. A kneeling sister fed a heartfruit falls: she becomes a succubus (a captain a demon), devoted
 * to whoever fed her. With an empty hand she is spared, and goes home ({@link #spare}).
 */
public class OrderSister extends Monster implements RangedAttackMob {
    public enum Rank {
        KNIGHT(6, GirlSpecies.SUCCUBUS_DEVIL), INQUISITOR(5, GirlSpecies.SUCCUBUS_DEVIL), CAPTAIN(10, GirlSpecies.DEMON_DEVIL);

        public final int faith;
        /** What the realm makes of her when she falls. */
        public final GirlSpecies fallsInto;

        Rank(int faith, GirlSpecies fallsInto) {
            this.faith = faith;
            this.fallsInto = fallsInto;
        }
    }

    private static final EntityDataAccessor<Boolean> KNEELING = SynchedEntityData.defineId(OrderSister.class, EntityDataSerializers.BOOLEAN);
    /** Faith lost to the realm's air: one point every ERODE ticks she stands in it in the dark. */
    public static final int ERODE = 200, BLESS_EVERY = 20, FALLEN_AFFECTION = 60, LEAVE_AFTER = 600;
    /** Kneeling in the realm's dark this many ERODE steps, she falls with nobody's help. */
    public static final int FALL_ALONE = 6;
    private int soaked;

    private final Rank rank;
    private int faith;
    /** Where she is headed: a realm patch to purge, a hearth to take. */
    @Nullable
    private BlockPos march;
    /** The crusade she marches in (Order raids), or 0; and its hearth. */
    private int raid;
    @Nullable
    private BlockPos raidHearth;
    @Nullable
    private UUID raidTarget;
    /** Game time after which she goes home (a patrol's day is over, or she was spared); -1 never. */
    private long leaveAt = -1;
    private boolean leaving;

    public OrderSister(EntityType<? extends OrderSister> type, Level level, Rank rank) {
        super(type, level);
        this.rank = rank;
        this.faith = rank.faith;
        this.xpReward = rank == Rank.CAPTAIN ? 15 : 8;
    }

    public static AttributeSupplier.Builder createAttributes(Rank rank) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, rank == Rank.CAPTAIN ? 40.0 : rank == Rank.INQUISITOR ? 22.0 : 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, rank == Rank.CAPTAIN ? 4.0 : 3.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(KNEELING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new KneelGoal());
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 40, 12.0F) {
            @Override
            public boolean canUse() {
                return OrderSister.this.rank == Rank.INQUISITOR && super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false) {
            @Override
            public boolean canUse() {
                return OrderSister.this.rank != Rank.INQUISITOR && super.canUse();
            }
        });
        this.goalSelector.addGoal(3, new ExorciseGoal());
        this.goalSelector.addGoal(4, new MarchGoal());
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, OrderSister.class) {
            @Override
            public boolean canUse() {
                return !OrderSister.this.isChaplain() && super.canUse();           // a chaplain doesn't strike back
            }
        }.setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, e -> Order.hunts(this, e)));
    }

    // --- what she is ---------------------------------------------------------------------------------------------

    public Rank rank() {
        return this.rank;
    }

    public int faith() {
        return this.faith;
    }

    public boolean kneeling() {
        return this.entityData.get(KNEELING);
    }

    public boolean leaving() {
        return this.leaving;
    }

    /** Still in the fight: alive, on her feet, not on her way home. */
    public boolean standing() {
        return this.isAlive() && !this.kneeling() && !this.leaving;
    }

    @Nullable
    public BlockPos march() {
        return this.march;
    }

    public void marchTo(@Nullable BlockPos to) {
        this.march = to == null ? null : to.immutable();
    }

    public int raid() {
        return this.raid;
    }

    @Nullable
    public BlockPos raidHearth() {
        return this.raidHearth;
    }

    @Nullable
    public UUID raidTarget() {
        return this.raidTarget;
    }

    public void joinRaid(int id, BlockPos hearth, @Nullable UUID player) {
        this.raid = id;
        this.raidHearth = hearth.immutable();
        this.raidTarget = player;
        this.march = hearth.immutable();
        this.setPersistenceRequired();
    }

    public void leaveAt(long time) {
        this.leaveAt = time;
    }

    /** The chapel she keeps (world/OrderChapelStructure), or null: she stays near it and marches nowhere. */
    @Nullable
    private BlockPos chapel;

    public void keepChapel(BlockPos font) {
        this.chapel = font.immutable();
        this.restrictTo(font, com.linkyoshi90.monstergirls.world.OrderChapelStructure.KEEP);
        this.setPersistenceRequired();
    }

    @Nullable
    public BlockPos chapel() {
        return this.chapel;
    }

    /** A spared sister come back as a chaplain (Chaplain): her name, or null. */
    @Nullable
    private String chaplain;

    public boolean isChaplain() {
        return this.chaplain != null;
    }

    public String chaplainName() {
        return this.chaplain == null ? "" : this.chaplain;
    }

    /** She lays her arms aside for good, takes up the book, and keeps to this hearth until {@code until}. */
    public void becomeChaplain(String name, BlockPos hearth, long until) {
        this.chaplain = name;
        for (EquipmentSlot slot : EquipmentSlot.values()) this.setItemSlot(slot, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOOK));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setCustomName(Component.translatable("entity.monstergirls.order_chaplain.named", name));
        this.restrictTo(hearth, Chaplain.KEEP);
        this.march = null;
        this.leaveAt = until;
        this.setTarget(null);
        this.setPersistenceRequired();
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return !this.isChaplain() && super.shouldDespawnInPeaceful();
    }

    // --- equipment ------------------------------------------------------------------------------------------------

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData data) {
        data = super.finalizeSpawn(level, difficulty, reason, data);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        return data;
    }

    @Override
    protected void populateDefaultEquipmentSlots(net.minecraft.util.RandomSource random, DifficultyInstance difficulty) {
        switch (this.rank) {
            case KNIGHT -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            }
            case INQUISITOR -> this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SPLASH_HOLY_WATER.get()));
            case CAPTAIN -> {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
                this.setItemSlot(EquipmentSlot.HEAD, Order.banner(this.registryAccess()));
                this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
                this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
            }
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) this.setDropChance(slot, 0.0F);   // no farming the Order's iron
        if (this.rank == Rank.CAPTAIN) this.setDropChance(EquipmentSlot.HEAD, 1.0F);          // her banner is the prize
        if (this.rank == Rank.INQUISITOR) this.setDropChance(EquipmentSlot.MAINHAND, 0.25F);
    }

    // --- the fight --------------------------------------------------------------------------------------------------

    /** Her blow at a possessed creature frees it instead of wounding it. */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof Mob mob && this.level() instanceof ServerLevel level && Monsterization.exorcise(level, mob)) {
            this.swing(InteractionHand.MAIN_HAND);
            level.sendParticles(ParticleTypes.END_ROD, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
            return true;
        }
        return super.doHurtTarget(target);
    }

    /** An inquisitrix throws a flask of holy water: it sears the realm's own where it breaks (ThrownHolyWater#sear). */
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        ThrownHolyWater flask = new ThrownHolyWater(this.level(), this);
        flask.setItem(new ItemStack(ModItems.SPLASH_HOLY_WATER.get()));
        double dx = target.getX() - this.getX(), dy = target.getY(0.3) - flask.getY(), dz = target.getZ() - this.getZ();
        double flat = Math.sqrt(dx * dx + dz * dz);
        flask.shoot(dx, dy + flat * 0.2, dz, 0.75F, 8.0F);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SPLASH_POTION_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
        this.level().addFreshEntity(flask);
        this.swing(InteractionHand.MAIN_HAND);
    }

    /** A realm silver blade at her: it cuts her faith, not her flesh. True (the blow does nothing else). */
    public static boolean shake(ServerLevel level, Mob mob, @Nullable Player by) {
        if (!(mob instanceof OrderSister sister) || sister.kneeling()) return mob instanceof OrderSister;
        sister.faith--;
        level.sendParticles(RealmGrassBlock.HAZE, sister.getX(), sister.getY() + 1.2, sister.getZ(), 10, 0.3, 0.4, 0.3, 0.0);
        sister.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.8F, 0.9F);
        if (sister.faith <= 0) sister.kneel(by);
        return true;
    }

    /** Her faith gives out: she kneels, lays down her sword and fights no more. */
    public void kneel(@Nullable Player by) {
        if (this.kneeling()) return;
        this.faith = 0;
        this.entityData.set(KNEELING, true);
        this.setTarget(null);
        this.getNavigation().stop();
        this.setPose(Pose.CROUCHING);
        this.setPersistenceRequired();
        ItemStack weapon = this.getMainHandItem();
        if (!weapon.isEmpty() && !weapon.is(ModItems.SPLASH_HOLY_WATER.get())) this.spawnAtLocation(weapon.copy());
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        for (Mob m : this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(32), m -> m.getTarget() == this)) m.setTarget(null);   // the fight is over
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(RealmGrassBlock.HAZE, this.getX(), this.getY() + 0.8, this.getZ(), 30, 0.4, 0.6, 0.4, 0.0);
            level.playSound(null, this.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.HOSTILE, 1.0F, 0.7F);
            for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(24))) {
                p.displayClientMessage(Component.translatable("message.monstergirls.order.kneels", this.getDisplayName()), false);
            }
        }
        Dairy.award(by, "order_humbled");
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isChaplain() && !this.kneeling() && !this.leaving && player.getItemInHand(hand).isEmpty()) {   // she tells you why she's here
            if (this.level() instanceof ServerLevel level) {
                long days = Math.max(1, (this.leaveAt - level.getGameTime() + 23999) / 24000);
                player.displayClientMessage(Component.translatable("message.monstergirls.order.chaplain.talk", this.chaplain, days), false);
                this.getLookControl().setLookAt(player);
            }
            return InteractionResult.SUCCESS;
        }
        if (!this.kneeling() || this.leaving) return super.mobInteract(player, hand);
        ItemStack stack = player.getItemInHand(hand);
        if (!(this.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        if (stack.is(ModItems.HEARTFRUIT.get())) {
            if (fall(level, player) != null && !player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.CONSUME;
        }
        if (stack.isEmpty()) {
            this.spare(level, player);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /** She falls to the realm: a succubus (a captain a demon), devoted to whoever fed her. What she became, or null. */
    @Nullable
    public Mob fall(ServerLevel level, @Nullable Player by) {
        Mob girl = Monsterization.changeInto(level, this, ModGirls.type(this.rank.fallsInto));
        if (girl == null) return null;
        if (by != null && girl instanceof Bonded bonded) {
            bonded.addAffection(by, FALLEN_AFFECTION);
            by.displayClientMessage(Component.translatable("message.monstergirls.order.fallen", girl.getDisplayName()), false);
            Dairy.award(by, "order_fallen");
            Order.addNotoriety(by, Order.FALL_NOTORIETY);
            com.linkyoshi90.monstergirls.entity.RealmTouched.addTouch(by, com.linkyoshi90.monstergirls.entity.RealmTouched.FALLEN);
        }
        return girl;
    }

    /** Spared: she rises, thanks them, and goes home. The Order hears of the mercy. */
    public void spare(ServerLevel level, Player by) {
        this.entityData.set(KNEELING, false);
        this.setPose(Pose.STANDING);
        this.leaving = true;
        this.leaveAt = level.getGameTime() + LEAVE_AFTER;
        this.march = null;
        by.displayClientMessage(Component.translatable("message.monstergirls.order.spared", this.getDisplayName()), false);
        level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.2, this.getZ(), 10, 0.3, 0.4, 0.3, 0.02);
        Order.addNotoriety(by, -Order.MERCY);
        Dairy.award(by, "order_spared");
        if (!this.isChaplain()) Chaplain.promise(level, by, this);                // she'll come back, to bless
    }

    // --- her day ------------------------------------------------------------------------------------------------------

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) this.level();
        if (this.kneeling()) {
            this.setTarget(null);
            this.getNavigation().stop();
            if (this.getPose() != Pose.CROUCHING) this.setPose(Pose.CROUCHING);
            if (this.tickCount % ERODE == 0 && DemonRealm.inRealm(this) && Monsterization.dark(level, this) && MonsterGirlsConfig.monsterization()
                    && ++this.soaked >= FALL_ALONE) {
                this.fall(level, null);                                 // left kneeling in the realm's dark, she falls by herself
                return;
            }
        }
        if (this.tickCount % BLESS_EVERY == 0 && !this.kneeling()) this.bless(level);
        if (this.tickCount % ERODE == 0 && !this.kneeling() && DemonRealm.realmAir(this) && Monsterization.dark(level, this)) {
            this.faith--;                                               // the realm's air wears at her
            level.sendParticles(RealmGrassBlock.HAZE, this.getX(), this.getY() + 1.0, this.getZ(), 6, 0.3, 0.4, 0.3, 0.0);
            if (this.faith <= 0) this.kneel(null);
        }
        if (this.isChaplain() && !this.leaving && this.leaveAt >= 0 && level.getGameTime() >= this.leaveAt) {   // her stay is over: farewell
            this.leaving = true;
            this.leaveAt = level.getGameTime() + LEAVE_AFTER;
            this.clearRestriction();
            for (Player p : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(32))) {
                p.displayClientMessage(Component.translatable("message.monstergirls.order.chaplain.farewell", this.chaplain), false);
            }
        }
        if (this.leaveAt >= 0 && level.getGameTime() >= this.leaveAt
                && (this.leaving || level.getNearestPlayer(this, 24) == null)) {
            level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 1.0, this.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
            this.discard();                                             // gone home
        }
    }

    /**
     * Her blessing: realm grass at her feet (and, for an inquisitrix, all within holy water's reach) turns back. Every
     * BLESS_EVERY ticks; an ordinary sister blesses only where she stands. How many blocks turned.
     */
    public int bless(ServerLevel level) {
        BlockPos feet = this.blockPosition();
        if (this.rank == Rank.INQUISITOR) {
            if (!Order.realmNear(level, feet, HolyWaterItem.RADIUS)) return 0;
            int n = HolyWaterItem.cleanse(level, feet.below());
            if (n > 0) {
                level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.3, this.getZ(), 20, 2.0, 0.2, 2.0, 0.01);
                level.playSound(null, feet, SoundEvents.BELL_RESONATE, SoundSource.HOSTILE, 0.6F, 1.6F);
            }
            return n;
        }
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(feet.offset(-1, -1, -1), feet.offset(1, 0, 1))) {
            if (com.linkyoshi90.monstergirls.block.RealmGrassBlock.realm(level.getBlockState(p))) {
                RealmGrassBlock.purify(level, p.immutable());
                n++;
            }
        }
        return n;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return this.raid == 0 && this.leaveAt < 0 && !this.kneeling() && super.removeWhenFarAway(distance);
    }

    @Override
    public boolean isPersistenceRequired() {
        return super.isPersistenceRequired() || this.raid != 0;
    }

    // --- sounds: human, not monster -------------------------------------------------------------------------------

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    // --- saving ---------------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Faith", this.faith);
        tag.putBoolean("Kneeling", this.kneeling());
        tag.putBoolean("Leaving", this.leaving);
        tag.putLong("LeaveAt", this.leaveAt);
        if (this.march != null) tag.put("March", NbtUtils.writeBlockPos(this.march));
        if (this.chapel != null) tag.put("Chapel", NbtUtils.writeBlockPos(this.chapel));
        if (this.chaplain != null) {
            tag.putString("Chaplain", this.chaplain);
            if (this.hasRestriction()) tag.put("ChaplainHearth", NbtUtils.writeBlockPos(this.getRestrictCenter()));
        }
        if (this.raid != 0 && this.raidHearth != null) {
            tag.putInt("Raid", this.raid);
            tag.put("RaidHearth", NbtUtils.writeBlockPos(this.raidHearth));
            if (this.raidTarget != null) tag.putUUID("RaidTarget", this.raidTarget);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Faith")) this.faith = tag.getInt("Faith");
        this.entityData.set(KNEELING, tag.getBoolean("Kneeling"));
        if (this.kneeling()) this.setPose(Pose.CROUCHING);
        this.leaving = tag.getBoolean("Leaving");
        this.leaveAt = tag.contains("LeaveAt") ? tag.getLong("LeaveAt") : -1;
        this.march = NbtUtils.readBlockPos(tag, "March").orElse(null);
        this.raid = tag.getInt("Raid");
        this.raidHearth = NbtUtils.readBlockPos(tag, "RaidHearth").orElse(null);
        this.raidTarget = tag.hasUUID("RaidTarget") ? tag.getUUID("RaidTarget") : null;
        NbtUtils.readBlockPos(tag, "Chapel").ifPresent(this::keepChapel);
        this.chaplain = tag.contains("Chaplain") ? tag.getString("Chaplain") : null;
        if (this.chaplain != null && !this.leaving) NbtUtils.readBlockPos(tag, "ChaplainHearth").ifPresent(h -> this.restrictTo(h, Chaplain.KEEP));
    }

    // --- goals ----------------------------------------------------------------------------------------------------

    /** Kneeling (or on her way home) she does nothing else. */
    private class KneelGoal extends Goal {
        KneelGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return OrderSister.this.kneeling();
        }
    }

    /** She goes to a possessed creature near her and drives the realm out with a touch. */
    private class ExorciseGoal extends Goal {
        @Nullable
        private Mob mark;

        ExorciseGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (OrderSister.this.getTarget() != null || OrderSister.this.leaving || OrderSister.this.random.nextInt(20) != 0) return false;
            this.mark = OrderSister.this.level().getEntitiesOfClass(Mob.class, OrderSister.this.getBoundingBox().inflate(16), Monsterization::possessed).stream()
                    .min(java.util.Comparator.comparingDouble(OrderSister.this::distanceToSqr)).orElse(null);
            return this.mark != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.mark != null && this.mark.isAlive() && Monsterization.possessed(this.mark) && OrderSister.this.getTarget() == null;
        }

        @Override
        public void tick() {
            if (this.mark == null) return;
            OrderSister.this.getLookControl().setLookAt(this.mark);
            if (OrderSister.this.distanceToSqr(this.mark) <= 2.5 * 2.5) {
                OrderSister.this.doHurtTarget(this.mark);
                this.mark = null;
            } else if (OrderSister.this.tickCount % 10 == 0) {
                OrderSister.this.getNavigation().moveTo(this.mark, 1.0);
            }
        }

        @Override
        public void stop() {
            this.mark = null;
        }
    }

    /** With nothing to fight she marches on (to the realm patch, the hearth), or walks away home if she is leaving. */
    private class MarchGoal extends Goal {
        MarchGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (OrderSister.this.getTarget() != null || OrderSister.this.kneeling()) return false;
            if (OrderSister.this.leaving) return true;
            if (OrderSister.this.isChaplain()) return false;                    // she stays by the hearth
            if (OrderSister.this.march == null && OrderSister.this.chapel == null && OrderSister.this.tickCount % 100 == 0 && OrderSister.this.level() instanceof ServerLevel level) {
                OrderSister.this.march = Order.findRealm(level, OrderSister.this.blockPosition(), 24);   // the realm calls to be purged
            }
            return OrderSister.this.march != null && OrderSister.this.march.distToCenterSqr(OrderSister.this.position()) > 16.0;
        }

        @Override
        public void tick() {
            if (OrderSister.this.tickCount % 20 != 0 || !OrderSister.this.getNavigation().isDone() && OrderSister.this.tickCount % 60 != 0) return;
            if (OrderSister.this.leaving) {
                Player p = OrderSister.this.level().getNearestPlayer(OrderSister.this, 32);
                Vec3 away = p == null ? DefaultRandomPos.getPos(OrderSister.this, 16, 4)
                        : DefaultRandomPos.getPosAway(OrderSister.this, 16, 4, p.position());
                if (away != null) OrderSister.this.getNavigation().moveTo(away.x, away.y, away.z, 1.0);
                return;
            }
            BlockPos to = OrderSister.this.march;
            if (to == null) return;
            Vec3 here = OrderSister.this.position(), there = Vec3.atBottomCenterOf(to);
            double d = here.distanceTo(there);
            if (d > 24) {                                               // far: a leg at a time
                Vec3 step = here.add(there.subtract(here).scale(16.0 / d));
                OrderSister.this.getNavigation().moveTo(step.x, Mth.floor(step.y), step.z, 1.0);
            } else {
                OrderSister.this.getNavigation().moveTo(there.x, there.y, there.z, 1.0);
            }
        }

        @Override
        public void stop() {
            if (!OrderSister.this.leaving && OrderSister.this.march != null && OrderSister.this.raid == 0
                    && OrderSister.this.march.distToCenterSqr(OrderSister.this.position()) <= 16.0) {
                OrderSister.this.march = null;                          // purged here; she'll look for more
            }
        }
    }

    /** For the renderer: which skin. */
    public String skin() {
        if (this.isChaplain()) return "order_chaplain";
        return switch (this.rank) {
            case KNIGHT -> "order_knight";
            case INQUISITOR -> "order_inquisitor";
            case CAPTAIN -> "order_captain";
        };
    }
}
