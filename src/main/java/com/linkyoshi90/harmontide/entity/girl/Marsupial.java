package com.linkyoshi90.harmontide.entity.girl;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
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
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The pouch family (marsupial_field_guide.html): kangaroo, koala. The kangaroo's joey rides in her pouch until it is
 * grown; she fights with kicks that knock back hard (the whole mob joins in) and, once devoted, kicks whatever hurts her
 * friend. The koala is awake only at dawn and dusk and sleeps the rest of the day clinging to a tree trunk; her joey
 * rides on her back. The drop bear drops onto whoever walks under her tree (a hug, once she likes you).
 */
public class Marsupial extends AbstractMonsterGirl {
    public Marsupial(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level, species);
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, this::isFood, false));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, e -> {
            Player p = this.bondedPlayer(60, 16);
            return p != null && e != p && p.getLastHurtByMob() == e;
        }));
    }

    public static AttributeSupplier.Builder createAttributes(GirlSpecies s) {
        if (s == GirlSpecies.GLIDERUNE_MARSUPIAL) return girlAttributes(12, 0.32, 1);
        return s == GirlSpecies.KOALA_MARSUPIAL ? girlAttributes(18, 0.2, 1) : girlAttributes(30, 0.32, 5).add(Attributes.ATTACK_KNOCKBACK, 2.5);
    }

    // --- the koala ---------------------------------------------------------------------------------------------------

    private boolean clinging;
    private long nextDrop;

    public boolean isKoala() {
        return this.species == GirlSpecies.KOALA_MARSUPIAL;
    }

    /** A koala is up only at dawn and dusk. */
    public static boolean koalaAwake(Level level) {
        long t = level.getDayTime() % 24000L;
        return t >= 22500 || t < 1500 || (t >= 11500 && t < 13500);
    }

    public boolean isClinging() {
        return this.clinging;
    }

    /** Off to sleep: up the side of a tree trunk nearby if there is one (three logs tall), else where she is. */
    public void goToSleep(ServerLevel level) {
        this.getNavigation().stop();
        this.setAction(Action.CLING);
        BlockPos o = this.blockPosition();
        for (BlockPos log : BlockPos.betweenClosed(o.offset(-8, -1, -8), o.offset(8, 1, 8))) {
            if (!level.getBlockState(log).is(net.minecraft.tags.BlockTags.LOGS) || !level.getBlockState(log.above(2)).is(net.minecraft.tags.BlockTags.LOGS)) continue;
            for (net.minecraft.core.Direction side : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                BlockPos spot = log.above(2).relative(side);
                if (!level.getBlockState(spot).isAir() || !level.getBlockState(spot.below()).isAir()) continue;
                this.teleportTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5);
                this.setYRot(side.getOpposite().toYRot());
                this.yBodyRot = this.getYRot();
                this.setNoGravity(true);                                     // clinging to the trunk
                this.clinging = true;
                return;
            }
        }
    }

    public void wakeUp() {
        if (this.clinging) this.setNoGravity(false);
        this.clinging = false;
        if (this.getAction() == Action.CLING) this.setAction(Action.NONE);
    }

    /** The drop bear: someone walks under her tree. A stranger gets a fright; a friend gets a hug. */
    private void lurk(ServerLevel level) {
        if (!this.clinging || !"drop_bear".equals(this.getVariantName()) || level.getGameTime() < this.nextDrop) return;
        Player under = this.nearest(Player.class, 6, p -> !p.isSpectator() && p.getY() < this.getY() - 1
                && p.position().subtract(this.position()).horizontalDistanceSqr() < 3.5 * 3.5);
        if (under == null) return;
        this.nextDrop = level.getGameTime() + 1200;
        this.wakeUp();
        this.teleportTo(under.getX(), this.getY(), under.getZ());
        boolean friend = this.getAffection(under) >= com.linkyoshi90.harmontide.entity.Orders.AFFECTION;
        if (friend) {
            this.hearts(level, 5);
        } else {
            under.hurt(this.damageSources().mobAttack(this), 2.0F);
            under.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, 100, 0));
        }
        this.playSound(SoundEvents.PANDA_AGGRESSIVE_AMBIENT, 1.0F, 1.3F);
        under.displayClientMessage(net.minecraft.network.chat.Component.translatable(friend ? "message.harmontide.drop_bear_hug"
                : "message.harmontide.drop_bear", this.getDisplayName()), true);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return !this.isKoala() && !this.isGlider() && super.causeFallDamage(distance, multiplier, source);
    }

    // --- the gliderune ---------------------------------------------------------------------------------------------------

    private int crouchTicks;

    public boolean isGlider() {
        return this.species == GirlSpecies.GLIDERUNE_MARSUPIAL;
    }

    @Override
    protected boolean isGliding() {
        return this.isGlider() && !this.isPassenger() && this.getDeltaMovement().y < -0.1;
    }

    /** A crouching empty hand from a friend: up onto their shoulder. */
    @Nullable
    @Override
    protected net.minecraft.world.InteractionResult familyInteract(Player player, net.minecraft.world.InteractionHand hand, ItemStack stack) {
        if (!this.isGlider() || !stack.isEmpty() || !player.isShiftKeyDown() || this.isBaby() || this.getAffection(player) < com.linkyoshi90.harmontide.entity.Orders.AFFECTION
                || player.isVehicle()) return null;
        if (!this.level().isClientSide) this.startRiding(player, true);
        return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /** On a shoulder: her friend floats down gently; two seconds of crouching sets her down. Off it: she glides. */
    private void tickGlider() {
        if (this.getVehicle() instanceof Player p) {
            if (this.tickCount % 20 == 0) p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 40, 0, true, false));
            this.crouchTicks = p.isShiftKeyDown() ? this.crouchTicks + 1 : 0;
            if (this.crouchTicks > 40) {
                this.stopRiding();
                this.crouchTicks = 0;
            }
        } else if (!this.onGround() && this.getDeltaMovement().y < -0.12 && !this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().x, -0.12, this.getDeltaMovement().z);
            this.resetFallDistance();
        }
    }

    /** A kick: both feet, lots of knockback. */
    @Override
    public boolean doHurtTarget(Entity target) {
        this.startTimedAction(Action.KICK, 16);
        return super.doHurtTarget(target);
    }

    // --- the joey in the pouch -------------------------------------------------------------------------------------

    /** The young one rides in her pouch: in front of her belly. */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        if (passenger instanceof Marsupial) {                          // a kangaroo's joey in her pouch; a koala's on her back
            return this.isKoala() ? new Vec3(0, dimensions.height() * 0.45, -dimensions.width() * 0.5) : new Vec3(0, dimensions.height() * 0.3, dimensions.width() * 0.55);
        }
        return super.getPassengerAttachmentPoint(passenger, dimensions, scale);
    }

    /** Nobody steers a kangaroo, least of all her joey. */
    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        return null;
    }

    /** The opossum: her friend near her, nearly dead, vanishes for three seconds and the monsters after them lose them; once a minute. 1 if so, 0 if not, -1 without a friend. */
    public int playPossum(net.minecraft.server.level.ServerLevel level) {
        Player friend = this.bondedPlayer(com.linkyoshi90.harmontide.entity.Orders.AFFECTION, 12);
        if (friend == null) return -1;
        long now = level.getGameTime();
        if (friend.getHealth() > 8 || now < this.getPersistentData().getLong("PossumAt")) return 0;
        this.getPersistentData().putLong("PossumAt", now + 1200);
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.INVISIBILITY, 60, 0, true, true));
        for (net.minecraft.world.entity.Mob m : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, friend.getBoundingBox().inflate(24), m -> m.getTarget() == friend)) m.setTarget(null);
        return 1;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.species == GirlSpecies.OPOSSUM_MARSUPIAL && this.level() instanceof net.minecraft.server.level.ServerLevel den && !this.isBaby() && this.tickCount % 11 == 0) this.playPossum(den);
        if (this.isGlider() && !this.level().isClientSide) this.tickGlider();
        if (this.isKoala() && !this.isBaby() && this.level() instanceof ServerLevel server) {
            boolean awake = koalaAwake(server) || server.getGameTime() < this.getWokenUntil() || this.getTarget() != null;
            if (awake && (this.clinging || this.getAction() == Action.CLING)) this.wakeUp();
            else if (!awake && this.getAction() != Action.CLING && this.tickCount % 20 == 0) this.goToSleep(server);
            if (this.clinging) {
                this.setDeltaMovement(Vec3.ZERO);
                this.getNavigation().stop();
                if (this.tickCount % 10 == 0) this.lurk(server);
            }
        }
        if (!(this.level() instanceof ServerLevel) || this.tickCount % 20 != 0) return;
        if (this.isBaby() && !this.isPassenger() && this.mother() instanceof Marsupial mom && !mom.isVehicle() && mom.distanceToSqr(this) < 4 * 4) {
            this.startRiding(mom, true);                                // back into the pouch
        } else if (!this.isBaby() && this.getVehicle() instanceof Marsupial) {
            this.stopRiding();                                          // grown: out she hops
        }
    }

    // --- food, presents, young -------------------------------------------------------------------------------------

    @Override
    public boolean isFood(ItemStack stack) {
        if (this.isKoala()) return stack.is(net.minecraft.tags.ItemTags.LEAVES);   // leaves, and nothing else
        if (this.isGlider()) return stack.is(Items.SWEET_BERRIES) || stack.is(Items.HONEY_BOTTLE) || stack.is(Items.APPLE);
        return stack.is(Items.WHEAT) || stack.is(Items.SHORT_GRASS) || stack.is(Items.APPLE) || stack.is(Items.SWEET_BERRIES);
    }

    @Override
    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(Items.SWEET_BERRIES) ? 2 : 1;
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        if (this.isKoala()) return new ItemStack(r < 50 ? Items.OAK_LEAVES : r < 85 ? Items.BIRCH_LEAVES : Items.STICK, 2);
        if (this.isGlider()) return new ItemStack(r < 50 ? Items.SWEET_BERRIES : r < 85 ? Items.HONEYCOMB : Items.FEATHER, 2);
        return r < 40 ? new ItemStack(Items.WHEAT, 3) : r < 75 ? new ItemStack(Items.SWEET_BERRIES, 4) : r < 95 ? new ItemStack(Items.RABBIT_HIDE, 2)
                : new ItemStack(Items.LEATHER);
    }

    @Override
    protected int litterSize() {
        return 1;
    }

    // --- sounds ----------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isKoala()) return this.getAction() == Action.CLING ? SoundEvents.FOX_SLEEP : SoundEvents.PANDA_AMBIENT;
        return SoundEvents.GOAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GOAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GOAT_DEATH;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.GOAT_EAT;
    }
}
