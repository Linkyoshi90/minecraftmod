package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.entity.CrowCaches;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

/**
 * The fox family (kitsune_field_guide.html): red fox, arctic fox, fennec, corsac, gray fox. Tails are trust: one
 * when wild, two at high friendship, three when devoted, nine for a kyubi (her true form). Tricksters: they play the
 * stash game (steal something plain from your hand and bury it; dig it up and laugh it off, and she loves you for
 * it), carry foxfire at night (real light), pounce, and leave a decoy behind when hurt.
 */
public class Kitsune extends AbstractMonsterGirl {
    private static final EntityDataAccessor<Byte> DATA_TAILS = SynchedEntityData.defineId(Kitsune.class, EntityDataSerializers.BYTE);
    private static final int DECOY_LIFE = 100;

    private long nextStashTime;
    private long nextDecoyTime;
    private long nextDigTime;
    private int decoyAge;

    public Kitsune(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4) {
            @Override
            public boolean canUse() {
                return Kitsune.this.getHealth() < Kitsune.this.getMaxHealth() * 0.4F && super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.45F));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25, true) {
            @Override
            protected void checkAndPerformAttack(LivingEntity target) {
                if (this.canPerformAttack(target)) Kitsune.this.startTimedAction(Action.POUNCE, 16);
                super.checkAndPerformAttack(target);
            }
        });
        this.goalSelector.addGoal(4, new StashGoal());
        if (species == GirlSpecies.CORSAC_KITSUNE) this.goalSelector.addGoal(4, new WayfinderGoal());
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Rabbit.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Chicken.class, true, c -> this.bestAffection() < 10));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        return girlAttributes(s == GirlSpecies.FENNEC_KITSUNE ? 12 : 16, s == GirlSpecies.CORSAC_KITSUNE ? 0.33 : 0.3, 3);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TAILS, (byte) 1);
    }

    /** How many tails she shows: her strongest bond (1 wild, 2 at 25, 3 at 60, 9 as a kyubi). */
    public int visibleTails() {
        return this.entityData.get(DATA_TAILS);
    }

    private int tailsFor(int affection) {
        if (this.isTrueForm()) return 9;
        return Math.max(this.getTails(), affection >= 60 ? 3 : affection >= BED_AFFECTION ? 2 : 1);
    }

    @Override
    protected boolean hasTrueForm() {
        return true;
    }

    /** One wild kitsune in four is an older fox spirit, born with two or three split tails (one in twenty-five: up to five). */
    @Override
    protected int rollTails(net.minecraft.util.RandomSource random) {
        if (random.nextInt(25) == 0) return 4 + random.nextInt(2);
        return random.nextInt(4) == 0 ? 2 + random.nextInt(2) : 1;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return this.species == GirlSpecies.GRAY_KITSUNE ? new WallClimberNavigation(this, level) : super.createNavigation(level);
    }

    /** The gray fox climbs trees like a cat. */
    @Override
    public boolean onClimbable() {
        return (this.species == GirlSpecies.GRAY_KITSUNE && this.horizontalCollision) || super.onClimbable();
    }

    @Override
    public boolean canFreeze() {
        return this.species != GirlSpecies.ARCTIC_KITSUNE && super.canFreeze();
    }

    // --- food, presents ------------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES) || stack.is(Items.CHICKEN) || stack.is(Items.COOKED_CHICKEN) || stack.is(ItemTags.FISHES);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES) || stack.is(Items.COOKED_CHICKEN) ? 2 : super.foodValue(player, stack);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        if (this.species == GirlSpecies.FENNEC_KITSUNE) return this.digFind();
        if (this.species == GirlSpecies.RED_KITSUNE && this.getAffection(player) >= 60) {
            return new ItemStack(r < 10 ? Items.EMERALD : r < 45 ? Items.GOLD_NUGGET : Items.SWEET_BERRIES, r < 45 ? 1 : 3);
        }
        return new ItemStack(r < 40 ? Items.SWEET_BERRIES : r < 60 ? Items.FEATHER : r < 75 ? Items.RABBIT_FOOT : r < 90 ? Items.CHICKEN : Items.GLOW_BERRIES);
    }

    /** What a fennec digs out of the sand. */
    private ItemStack digFind() {
        int r = this.random.nextInt(100);
        if (r < 3) return new ItemStack(Items.EMERALD);
        if (r < 10) return new ItemStack(this.random.nextBoolean() ? Items.ARCHER_POTTERY_SHERD : Items.PRIZE_POTTERY_SHERD);
        if (r < 40) return new ItemStack(Items.GOLD_NUGGET, 1 + this.random.nextInt(3));
        return new ItemStack(Items.BONE);
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return this.level().isDay() ? SoundEvents.FOX_SLEEP : SoundEvents.FOX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.FOX_EAT;
    }

    // --- young ---------------------------------------------------------------------------------------------------

    @Override
    protected int litterSize() {
        return 3 + this.random.nextInt(2);
    }

    @Override
    public void onNightShared(ServerPlayer player) {
        player.setTicksFrozen(0);
        if (this.species == GirlSpecies.ARCTIC_KITSUNE) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
    }

    // --- the decoy -----------------------------------------------------------------------------------------------

    public boolean isDecoy() {
        return this.hasFlag(FLAG_DECOY);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isDecoy()) {                               // an illusion pops into foxfire when touched
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.8, this.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
            }
            this.discard();
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.level() instanceof ServerLevel server && this.getHealth() < this.getMaxHealth() * 0.6F
                && server.getGameTime() >= this.nextDecoyTime && source.getEntity() instanceof LivingEntity attacker) {
            this.nextDecoyTime = server.getGameTime() + 600;
            int n = this.species == GirlSpecies.RED_KITSUNE || this.isTrueForm() ? 2 : 1;
            for (int i = 0; i < n; i++) this.spawnDecoy(server, attacker);
            this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false));
        }
        return hurt;
    }

    private void spawnDecoy(ServerLevel level, LivingEntity attacker) {
        if (!(this.getType().create(level) instanceof Kitsune decoy)) return;
        decoy.moveTo(this.getX() + this.random.nextGaussian() * 0.6, this.getY(), this.getZ() + this.random.nextGaussian() * 0.6, this.getYRot(), 0);
        decoy.setFlag(FLAG_DECOY, true);
        decoy.setVariantIndex(this.getVariantIndex());
        decoy.entityData.set(DATA_TAILS, this.entityData.get(DATA_TAILS));
        decoy.setNoAi(true);
        decoy.setBaby(this.isBaby());
        level.addFreshEntity(decoy);
        if (attacker instanceof net.minecraft.world.entity.Mob mob) mob.setTarget(decoy);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return this.isDecoy() || super.removeWhenFarAway(distance);
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
        if (!this.isDecoy()) super.dropAllDeathLoot(level, source);
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.isDecoy()) {
            if (++this.decoyAge > DECOY_LIFE) this.discard();
            return;
        }
        if (this.tickCount % 20 == 0) this.entityData.set(DATA_TAILS, (byte) this.tailsFor(this.bestAffection()));
        // foxfire: real light at night once she has two tails (a kyubi carries a brighter lantern)
        boolean night = !server.isDay() || server.getBrightness(net.minecraft.world.level.LightLayer.SKY, this.blockPosition()) < 4;
        int tails = this.visibleTails();
        this.glow(server, night && tails >= 2 ? (tails >= 9 ? 12 : 9) : 0);
        if (night && tails >= 2 && this.tickCount % 10 == 0) {
            server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX() + this.random.nextGaussian() * 0.4, this.getY() + 1.0 + this.random.nextDouble(),
                    this.getZ() + this.random.nextGaussian() * 0.4, 1, 0, 0.01, 0, 0);
        }
        // her stash spots flicker with foxfire, so a sharp-eyed player can find them
        if (this.tickCount % 40 == 0) {
            for (CrowCaches.Cache c : CrowCaches.get(server).of(this.getUUID())) {
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.pos().getX() + 0.5, c.pos().getY() + 1.1, c.pos().getZ() + 0.5, 1, 0.2, 0.1, 0.2, 0.0);
            }
        }
        if (this.species == GirlSpecies.FENNEC_KITSUNE && server.getGameTime() >= this.nextDigTime && this.onGround()
                && server.getBlockState(this.blockPosition().below()).is(BlockTags.SAND)) {
            this.nextDigTime = server.getGameTime() + 3600 + this.random.nextInt(2400);
            this.startTimedAction(Action.DIG, 40);
            Player p = this.bondedPlayer(BED_AFFECTION, 12);
            ItemStack find = this.digFind();
            if (p != null) {
                if (!p.getInventory().add(find)) this.spawnAtLocation(find);
            } else {
                this.spawnAtLocation(find);
            }
        }
    }

    /** Someone dug up her stash: if they do not hit her for it, she adores them for playing along (+3). */
    public void onStashFound(Player player) {
        this.addAffection(player, 3);
        this.startTimedAction(Action.LAUGH, 60);
        this.playSound(SoundEvents.FOX_SCREECH, 0.7F, 1.5F);
        player.displayClientMessage(Component.translatable("message.monstergirls.stash_found", this.getDisplayName()), true);
        if (this.level() instanceof ServerLevel server) this.hearts(server, 5);
    }

    // --- goals ---------------------------------------------------------------------------------------------------

    /**
     * The stash game: a wild or curious kitsune sneaks up, takes something plain from a hand (never tools, weapons or
     * armour) and buries it within a few blocks, under a wisp of foxfire.
     */
    class StashGoal extends Goal {
        private Player victim;
        private ItemStack loot = ItemStack.EMPTY;
        private BlockPos spot;
        private int ticks;

        StashGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private boolean plain(ItemStack s) {
            return !s.isEmpty() && !s.isDamageableItem() && !(s.getItem() instanceof ArmorItem) && !s.is(Items.STICK);
        }

        @Override
        public boolean canUse() {
            if (Kitsune.this.tickCount % 47 != 0 || Kitsune.this.isBaby() || Kitsune.this.getTarget() != null
                    || Kitsune.this.level().getGameTime() < Kitsune.this.nextStashTime || Kitsune.this.random.nextInt(4) != 0) return false;
            for (Player p : Kitsune.this.level().players()) {
                if (p.isSpectator() || p.isCreative() || p.distanceToSqr(Kitsune.this) > 10 * 10 || Kitsune.this.getAffection(p) >= BED_AFFECTION) continue;
                if (this.plain(p.getMainHandItem())) {
                    this.victim = p;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks < 600 && Kitsune.this.getTarget() == null && (this.loot.isEmpty() ? this.victim != null && this.victim.isAlive() : true);
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.loot = ItemStack.EMPTY;
            this.spot = null;
        }

        @Override
        public void tick() {
            this.ticks++;
            if (this.loot.isEmpty()) {
                Kitsune.this.getLookControl().setLookAt(this.victim);
                if (Kitsune.this.distanceToSqr(this.victim) > 2.5) {
                    if (this.ticks % 10 == 1) Kitsune.this.getNavigation().moveTo(this.victim, 1.2);
                    return;
                }
                ItemStack hand = this.victim.getMainHandItem();
                if (!this.plain(hand)) {
                    this.ticks = 9999;
                    return;
                }
                this.loot = hand.split(1);
                Kitsune.this.playSound(SoundEvents.FOX_BITE, 1.0F, 1.4F);
                Kitsune.this.startTimedAction(Action.LAUGH, 30);
                this.spot = this.findSpot();
                if (this.spot == null) {
                    Kitsune.this.spawnAtLocation(this.loot);
                    this.loot = ItemStack.EMPTY;
                    this.ticks = 9999;
                }
                return;
            }
            Vec3 c = Vec3.atBottomCenterOf(this.spot.above());
            if (Kitsune.this.distanceToSqr(c) > 2.0) {
                if (this.ticks % 10 == 1) Kitsune.this.getNavigation().moveTo(c.x, c.y, c.z, 1.25);
                return;
            }
            Kitsune.this.getNavigation().stop();
            Kitsune.this.startTimedAction(Action.DIG, 30);
            if (Kitsune.this.level() instanceof ServerLevel server) {
                CrowCaches.get(server).add(new CrowCaches.Cache(this.spot, this.loot, Kitsune.this.getUUID(), Long.MAX_VALUE, false));
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.x, c.y + 0.2, c.z, 8, 0.2, 0.1, 0.2, 0.01);
            }
            this.loot = ItemStack.EMPTY;
            this.ticks = 9999;
        }

        @Nullable
        private BlockPos findSpot() {
            BlockPos o = Kitsune.this.blockPosition();
            for (int i = 0; i < 30; i++) {
                BlockPos p = o.offset(Kitsune.this.random.nextInt(17) - 8, -1, Kitsune.this.random.nextInt(17) - 8);
                for (int dy = 2; dy >= -3; dy--) {
                    BlockPos q = p.above(dy);
                    var s = Kitsune.this.level().getBlockState(q);
                    if ((s.is(BlockTags.DIRT) || s.is(BlockTags.SAND) || s.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK)) && Kitsune.this.level().getBlockState(q.above()).isAir()
                            && CrowCaches.get((ServerLevel) Kitsune.this.level()).at(q) == null) return q.immutable();
                }
            }
            return null;
        }

        @Override
        public void stop() {
            if (!this.loot.isEmpty()) Kitsune.this.spawnAtLocation(this.loot);
            this.loot = ItemStack.EMPTY;
            this.victim = null;
            Kitsune.this.nextStashTime = Kitsune.this.level().getGameTime() + 4800 + Kitsune.this.random.nextInt(4800);
        }
    }

    /** The corsac always knows the way home: at night, far from your bed, she trots ahead towards it. */
    class WayfinderGoal extends Goal {
        private ServerPlayer player;
        private BlockPos home;

        WayfinderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (Kitsune.this.tickCount % 61 != 0 || Kitsune.this.level().isDay()) return false;
            if (!(Kitsune.this.bondedPlayer(BED_AFFECTION, 24) instanceof ServerPlayer p)) return false;
            BlockPos respawn = p.getRespawnPosition();
            if (respawn == null || p.getRespawnDimension() != Kitsune.this.level().dimension() || respawn.distSqr(p.blockPosition()) < 64 * 64) return false;
            this.player = p;
            this.home = respawn;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.player != null && this.player.isAlive() && Kitsune.this.distanceToSqr(this.player) < 32 * 32
                    && this.home.distSqr(Kitsune.this.blockPosition()) > 16 * 16 && !Kitsune.this.level().isDay();
        }

        @Override
        public void tick() {
            if (Kitsune.this.distanceToSqr(this.player) > 14 * 14) {                // wait for them to catch up
                Kitsune.this.getNavigation().stop();
                Kitsune.this.getLookControl().setLookAt(this.player);
                return;
            }
            if (Kitsune.this.getNavigation().isDone()) {
                Vec3 dir = Vec3.atCenterOf(this.home).subtract(Kitsune.this.position()).normalize().scale(12);
                Vec3 t = Kitsune.this.position().add(dir);
                Kitsune.this.getNavigation().moveTo(t.x, t.y, t.z, 1.0);
            }
            if (Kitsune.this.tickCount % 5 == 0 && Kitsune.this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, Kitsune.this.getX(), Kitsune.this.getY() + 0.3, Kitsune.this.getZ(), 1, 0.1, 0, 0.1, 0);
            }
        }

        @Override
        public void stop() {
            this.player = null;
        }
    }

    public static boolean isOwnStash(UUID owner, @Nullable net.minecraft.world.entity.Entity e) {
        return e instanceof Kitsune && e.getUUID().equals(owner);
    }
}
