package com.linkyoshi90.monstergirls.entity.girl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

/**
 * The cattle family (minotaur_field_guide.html): cattle, Minoan, bison, aurochs, water buffalo. The only family with men:
 * about one in three is a bull ({@link #FLAG_MALE}, drawn with the &lt;species&gt;_male model). A cow and a bull fed
 * wheat raise a calf with no player involved, so herds can be farmed. Cows give milk to a bucket. Hurt one and she
 * charges; the Minoan guards her ground, bison stampede, the aurochs cannot be pushed, the water buffalo wallows in water,
 * and a devoted minotaur ploughs your fields.
 */
public class Minotaur extends AbstractMonsterGirl {
    public static final int MILK_COOLDOWN = 6000;
    public static final int LOVE_TICKS = 600;
    public static final int GUARD_RADIUS = 10;

    private long nextMilk;
    private int inLove;
    @Nullable
    private BlockPos post;
    private long warnedAt = -1000;
    private int stampede;
    @Nullable
    private Vec3 stampedeFrom;

    public Minotaur(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(1, new StampedeGoal());
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.5, true) {
            @Override
            public void tick() {
                super.tick();
                if (Minotaur.this.getAction() == Action.NONE) Minotaur.this.startTimedAction(Action.CHARGE, 10);
            }
        });
        this.goalSelector.addGoal(3, new CourtGoal());
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1, Ingredient.of(Items.WHEAT, Items.HAY_BLOCK), false));
        if (species == GirlSpecies.WATER_BUFFALO_MINOTAUR) this.goalSelector.addGoal(5, new WallowGoal());
        this.goalSelector.addGoal(6, new EatBlockGoal(this));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        if (species == GirlSpecies.MINOAN_MINOTAUR) {                    // the labyrinth guard: strangers on her ground
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, p -> this.isTrespasser((Player) p)));
        }
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return switch (s) {
            case MINOAN_MINOTAUR -> girlAttributes(40, 0.27, 8).add(Attributes.ATTACK_KNOCKBACK, 1.5).add(Attributes.ARMOR, 4);
            case BISON_MINOTAUR -> girlAttributes(40, 0.28, 6).add(Attributes.ATTACK_KNOCKBACK, 1.5);
            case AUROCHS_MINOTAUR -> girlAttributes(48, 0.27, 9).add(Attributes.ATTACK_KNOCKBACK, 2.5).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
            case WATER_BUFFALO_MINOTAUR -> girlAttributes(36, 0.25, 6).add(Attributes.ATTACK_KNOCKBACK, 1.2);
            default -> girlAttributes(30, 0.25, 4).add(Attributes.ATTACK_KNOCKBACK, 1.0);
        };
    }

    // --- bulls and cows ------------------------------------------------------------------------------------------

    public boolean isMale() {
        return this.hasFlag(FLAG_MALE);
    }

    public void setMale(boolean male) {
        this.setFlag(FLAG_MALE, male);
        this.refreshDimensions();
    }

    /** Bulls stand a tenth taller. */
    @Override
    public float sizeScale() {
        return super.sizeScale() * (this.isMale() ? 1.1F : 1.0F);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData group) {
        if (type != MobSpawnType.BREEDING && this.random.nextInt(3) == 0) this.setMale(true);
        if (this.post == null) this.post = this.blockPosition();
        return super.finalizeSpawn(level, difficulty, type, group);
    }

    /** Bulls never share a player's bed. */
    @Override
    public boolean canShareBed() {
        return !this.isMale() && super.canShareBed();
    }

    @Override
    public AbstractMonsterGirl spawnYoung(ServerLevel level, BlockPos pos, @Nullable UUID father) {
        AbstractMonsterGirl calf = super.spawnYoung(level, pos, father);
        if (calf instanceof Minotaur m) m.setMale(this.random.nextBoolean());   // half the calves are bulls
        return calf;
    }

    public boolean isInLove() {
        return this.inLove > 0;
    }

    public void setInLove(int ticks) {
        this.inLove = ticks;
    }

    // --- interaction ---------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT) || stack.is(Items.HAY_BLOCK);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.HAY_BLOCK) ? 3 : 1;
    }

    @Nullable
    @Override
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        boolean client = this.level().isClientSide;
        if (stack.is(Items.WHEAT) && !this.isBaby() && !this.isInLove()) {             // wheat puts her in the mood for courting
            if (!client) {
                this.inLove = LOVE_TICKS;
                if (this.level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 3, 0.3, 0.2, 0.3, 0);
            }
            return null;                                                               // and it still counts as food
        }
        if (stack.is(Items.BUCKET) && !this.isMale() && !this.isBaby()) {
            if (this.level() instanceof ServerLevel server) {
                if (server.getGameTime() < this.nextMilk) {
                    player.displayClientMessage(Component.translatable("message.monstergirls.no_milk", this.getDisplayName()), true);
                } else {
                    boolean friend = this.getAffection(player) >= BED_AFFECTION;
                    this.nextMilk = server.getGameTime() + (friend ? MILK_COOLDOWN / 2 : MILK_COOLDOWN);
                    stack.consume(1, player);
                    ItemStack milk = new ItemStack(Items.MILK_BUCKET);
                    if (!player.getInventory().add(milk)) player.drop(milk, false);
                    this.addAffection(player, 1);
                    this.startTimedAction(Action.MILK, 40);
                    this.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
                }
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (stack.is(Items.BRUSH)) {
            if (this.level() instanceof ServerLevel server) {
                this.addAffection(player, 2);
                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                this.playSound(SoundEvents.BRUSH_GENERIC, 1.0F, 1.0F);
                this.hearts(server, 3);
            }
            return InteractionResult.sidedSuccess(client);
        }
        return null;
    }

    /** The milk is ready again: her cowbell rings once (a note over her head), so you know without asking. */
    private void ringBell(ServerLevel level) {
        this.nextMilk = 0;
        this.playSound(SoundEvents.NOTE_BLOCK_COW_BELL.value(), 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + this.getBbHeight() + 0.4, this.getZ(), 0, 0.3, 0.0, 0.0, 1.0);
    }

    public boolean isMilkReady() {
        return this.level().getGameTime() >= this.nextMilk;
    }

    public void setNextMilk(long time) {
        this.nextMilk = time;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return switch (this.species) {
            case CATTLE_MINOTAUR -> r < 35 ? new ItemStack(Items.MILK_BUCKET) : r < 60 ? new ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.CHEESE.get(), 2)
                    : new ItemStack(Items.WHEAT, 4);
            case WATER_BUFFALO_MINOTAUR -> r < 20 ? new ItemStack(Items.CAKE) : r < 60 ? new ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.CHEESE.get(), 2)
                    : new ItemStack(Items.MILK_BUCKET);
            case MINOAN_MINOTAUR -> new ItemStack(r < 10 ? Items.GOLD_INGOT : r < 50 ? Items.COPPER_INGOT : Items.BRICK, r < 50 ? 1 : 3);
            default -> new ItemStack(r < 50 ? Items.LEATHER : Items.WHEAT, 2);
        };
    }

    // --- combat --------------------------------------------------------------------------------------------------

    /** The Minoan's ground: a stranger who comes within ten blocks of her post is warned with a bellow, then charged. */
    private boolean isTrespasser(Player p) {
        if (this.post == null || p.isCreative() || p.isSpectator() || this.getAffection(p) >= 10 || this.isBaby()) return false;
        if (p.distanceToSqr(Vec3.atCenterOf(this.post)) > GUARD_RADIUS * GUARD_RADIUS) return false;
        long now = this.level().getGameTime();
        if (now - this.warnedAt > 400) {
            this.warnedAt = now;
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.3F);
            p.displayClientMessage(Component.translatable("message.monstergirls.trespass", this.getDisplayName()), true);
            return false;
        }
        return now - this.warnedAt > 60;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.species == GirlSpecies.BISON_MINOTAUR && source.getEntity() != null) {   // the herd stampedes
            for (Minotaur m : this.level().getEntitiesOfClass(Minotaur.class, this.getBoundingBox().inflate(16), m -> m.species == GirlSpecies.BISON_MINOTAUR && m != this)) {
                m.startStampede(source.getEntity().position());
            }
        }
        return hurt;
    }

    public void startStampede(Vec3 from) {
        this.stampede = 100;
        this.stampedeFrom = from;
    }

    public boolean isStampeding() {
        return this.stampede > 0;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) this.playSound(SoundEvents.RAVAGER_ATTACK, 0.8F, 1.2F);
        return hit;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.inLove > 0) this.inLove--;
        if (this.stampede > 0) this.stampede--;
        if (this.nextMilk > 0 && server.getGameTime() >= this.nextMilk) this.ringBell(server);
        if (this.tickCount % 40 == 0 && !this.isBaby()) {
            Player p = this.bondedPlayer(60, 16);
            if (p != null) {
                if (this.getTarget() != null || p.getLastHurtByMob() != null) p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0, true, false));
                this.plough(server);
            }
        }
    }

    /** A devoted minotaur tills the grass and dirt she walks on into farmland, if there is farmland nearby already. */
    private void plough(ServerLevel level) {
        BlockPos under = this.blockPosition().below();
        var s = level.getBlockState(under);
        if (!(s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT)) || !level.getBlockState(under.above()).isAir()) return;
        for (BlockPos p : BlockPos.betweenClosed(under.offset(-4, -1, -4), under.offset(4, 1, 4))) {
            if (level.getBlockState(p).is(Blocks.FARMLAND)) {
                level.setBlock(under, Blocks.FARMLAND.defaultBlockState(), 3);
                level.playSound(null, under, SoundEvents.HOE_TILL, net.minecraft.sounds.SoundSource.NEUTRAL, 0.8F, 1.0F);
                return;
            }
        }
    }

    /** A cow in love finds a bull in love (or the other way round) and they raise a calf. */
    class CourtGoal extends Goal {
        @Nullable
        private Minotaur partner;
        private int ticks;

        CourtGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Minotaur m = Minotaur.this;
            if (!m.isInLove() || m.isMale() || m.isBaby() || !m.getGestation().canConceive()) return false;
            this.partner = m.nearest(Minotaur.class, 8, o -> o.getType() == m.getType() && o.isMale() && o.isInLove() && !o.isBaby());
            return this.partner != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.partner != null && this.partner.isAlive() && this.ticks < 120;
        }

        @Override
        public void start() {
            this.ticks = 0;
        }

        @Override
        public void tick() {
            Minotaur m = Minotaur.this;
            this.ticks++;
            m.getLookControl().setLookAt(this.partner);
            this.partner.getLookControl().setLookAt(m);
            if (m.distanceToSqr(this.partner) > 2.5 * 2.5) {
                m.getNavigation().moveTo(this.partner, 1.0);
                return;
            }
            m.getGestation().conceive(this.partner.getUUID());
            m.inLove = 0;
            this.partner.inLove = 0;
            if (m.level() instanceof ServerLevel server) m.hearts(server, 6);
            this.ticks = 999;
        }
    }

    /** Bison run from whatever hurt one of their herd. */
    class StampedeGoal extends Goal {
        StampedeGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return Minotaur.this.isStampeding() && Minotaur.this.stampedeFrom != null;
        }

        @Override
        public void tick() {
            Minotaur m = Minotaur.this;
            if (m.tickCount % 10 != 0) return;
            Vec3 away = m.position().subtract(m.stampedeFrom).normalize().scale(12).add(m.position());
            m.getNavigation().moveTo(away.x, away.y, away.z, 1.7);
            m.setSprinting(true);
        }

        @Override
        public void stop() {
            Minotaur.this.setSprinting(false);
        }
    }

    /** By day the water buffalo goes and stands in water. */
    class WallowGoal extends Goal {
        @Nullable
        private BlockPos pool;

        WallowGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Minotaur m = Minotaur.this;
            if (!m.level().isDay() || m.isInWater() || m.getTarget() != null || m.tickCount % 41 != 0 || m.random.nextInt(3) != 0) return false;
            BlockPos o = m.blockPosition();
            for (BlockPos p : BlockPos.betweenClosed(o.offset(-12, -3, -12), o.offset(12, 3, 12))) {
                if (m.level().getFluidState(p).is(net.minecraft.tags.FluidTags.WATER) && m.level().getBlockState(p.above()).isAir()) {
                    this.pool = p.immutable();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.pool != null && !Minotaur.this.isInWater() && Minotaur.this.getTarget() == null && Minotaur.this.getNavigation().isInProgress();
        }

        @Override
        public void start() {
            Minotaur.this.getNavigation().moveTo(this.pool.getX() + 0.5, this.pool.getY(), this.pool.getZ() + 0.5, 1.0);
        }
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.COW_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.COW_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.COW_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.GENERIC_EAT;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * (this.isMale() ? 0.75F : 1.0F);
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(SoundEvents.COW_STEP, 0.2F, 1.0F);
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    protected boolean isSettled() {
        return this.isBaby() && this.getFather() != null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("NextMilk", this.nextMilk);
        tag.putInt("InLove", this.inLove);
        if (this.post != null) tag.putLong("Post", this.post.asLong());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.nextMilk = tag.getLong("NextMilk");
        this.inLove = tag.getInt("InLove");
        this.post = tag.contains("Post") ? BlockPos.of(tag.getLong("Post")) : null;
        this.refreshDimensions();
    }
}
