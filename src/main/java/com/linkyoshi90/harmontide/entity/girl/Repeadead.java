package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Bonded;
import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.entity.Orders;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * The repeadead (2026-11-13), the user's idea in an original design: a gaunt woman wound in grave linen under a porcelain mourning mask.
 * <p>
 * <b>Wild.</b> In the dark she stands or crouches stock-still where she is ({@link WaitGoal}) until someone walks into the cone in
 * front of her ({@link #NOTICE} blocks, {@link #CONE} degrees either side, in her sight). Then she screams: everyone not her friend within
 * {@link #SCREAM} blocks is frozen in place for {@link #FREEZE} ticks ({@link #paralyse}: no walking, no jumping), and she comes for the
 * one she saw and clings to them, drinking their life ({@link #DRAIN} a second, healing herself as much) and keeping them held. A blow
 * from the one she holds breaks her grip and staggers her. She screams again only after {@link #SCREAM_AGAIN} ticks.
 * <p>
 * <b>Befriended.</b> Friendly creatures she leaves alone. She turns the scream and the grip on monsters near her or her friend
 * ({@link #GUARD} blocks): the scream freezes them, the grip drains them.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Repeadead {
    public static final int NOTICE = 10, SCREAM = 12, FREEZE = 60, HOLD = 25, SCREAM_AGAIN = 300, GRIP_TICKS = 240, GUARD = 12, STAGGER = 100;
    public static final float DRAIN = 2.0F, DRAIN_MONSTER = 3.0F, CONE = 50.0F;
    public static final double REACH = 1.8;
    public static final ResourceLocation PARALYSED = HarmontideMod.id("grave_scream");
    private static final String GRIP = "RDGrip", GRIP_UNTIL = "RDGripUntil", SCREAMED = "RDScreamed", STAGGERED = "RDStaggered", FROZEN = "ParalysedUntil";

    private Repeadead() {}

    public static boolean is(Entity e) {
        return e instanceof AbstractMonsterGirl g && g.getSpecies() == GirlSpecies.REPEADEAD_UNDEAD && !g.isBaby();
    }

    // --- paralysis -------------------------------------------------------------------------------------------------------

    /** Frozen in place: no walking, no jumping, for so many ticks. */
    public static void paralyse(LivingEntity e, int ticks) {
        for (var attr : List.of(Attributes.MOVEMENT_SPEED, Attributes.JUMP_STRENGTH)) {
            AttributeInstance a = e.getAttribute(attr);
            if (a != null && !a.hasModifier(PARALYSED)) a.addTransientModifier(new AttributeModifier(PARALYSED, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        long until = e.level().getGameTime() + ticks;
        CompoundTag pd = e.getPersistentData();
        if (pd.getLong(FROZEN) < until) pd.putLong(FROZEN, until);
        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 4, false, false, true));
    }

    public static boolean paralysed(LivingEntity e) {
        AttributeInstance a = e.getAttribute(Attributes.MOVEMENT_SPEED);
        return a != null && a.hasModifier(PARALYSED);
    }

    public static void release(LivingEntity e) {
        for (var attr : List.of(Attributes.MOVEMENT_SPEED, Attributes.JUMP_STRENGTH)) {
            AttributeInstance a = e.getAttribute(attr);
            if (a != null) a.removeModifier(PARALYSED);
        }
        e.getPersistentData().remove(FROZEN);
    }

    // --- noticing, screaming, gripping --------------------------------------------------------------------------------

    /** Someone in the cone in front of her, near enough and in her sight. */
    public static boolean inFront(Mob her, LivingEntity who) {
        if (who.distanceToSqr(her) > NOTICE * NOTICE || !her.hasLineOfSight(who)) return false;
        Vec3 look = Vec3.directionFromRotation(0, her.getYHeadRot());
        Vec3 to = new Vec3(who.getX() - her.getX(), 0, who.getZ() - her.getZ()).normalize();
        return Math.toDegrees(Math.acos(Mth.clamp(look.dot(to), -1.0, 1.0))) <= CONE;
    }

    /** The scream: everyone she'd freeze within reach is frozen. How many. */
    public static int scream(ServerLevel level, AbstractMonsterGirl her, boolean befriended) {
        her.getPersistentData().putLong(SCREAMED, level.getGameTime());
        int n = 0;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, her.getBoundingBox().inflate(SCREAM), e -> e != her && e.isAlive())) {
            boolean frozen = befriended ? e instanceof Enemy && !(e instanceof Bonded)
                    : e instanceof Player p && !p.isCreative() && !p.isSpectator() && her.getAffection(p) < Orders.AFFECTION;
            if (!frozen) continue;
            paralyse(e, FREEZE);
            if (e instanceof Player p) {
                p.displayClientMessage(Component.translatable("message.harmontide.repeadead.scream", her.getDisplayName()), true);
                Dairy.award(p, "repeadead_scream");
            }
            n++;
        }
        level.playSound(null, her.blockPosition(), SoundEvents.GHAST_HURT, SoundSource.HOSTILE, 2.0F, 0.5F);
        level.playSound(null, her.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 1.2F, 1.6F);
        level.sendParticles(ParticleTypes.SONIC_BOOM, her.getX(), her.getEyeY(), her.getZ(), 1, 0, 0, 0, 0);
        return n;
    }

    public static boolean canScream(Mob her) {
        return her.level().getGameTime() - her.getPersistentData().getLong(SCREAMED) >= SCREAM_AGAIN || !her.getPersistentData().contains(SCREAMED);
    }

    @Nullable
    public static LivingEntity gripping(ServerLevel level, Mob her) {
        CompoundTag pd = her.getPersistentData();
        if (!pd.hasUUID(GRIP) || level.getGameTime() > pd.getLong(GRIP_UNTIL)) return null;
        return level.getEntity(pd.getUUID(GRIP)) instanceof LivingEntity e && e.isAlive() ? e : null;
    }

    public static void grip(ServerLevel level, Mob her, LivingEntity who) {
        her.getPersistentData().putUUID(GRIP, who.getUUID());
        her.getPersistentData().putLong(GRIP_UNTIL, level.getGameTime() + GRIP_TICKS);
    }

    public static void letGo(Mob her) {
        her.getPersistentData().remove(GRIP);
        her.getPersistentData().remove(GRIP_UNTIL);
    }

    /** One drink: close enough, she drains them and heals as much. True if she drank. */
    public static boolean drain(ServerLevel level, AbstractMonsterGirl her, LivingEntity who, boolean befriended) {
        if (who.distanceToSqr(her) > REACH * REACH) {
            her.getNavigation().moveTo(who, 1.1);
            return false;
        }
        her.getNavigation().stop();
        her.getLookControl().setLookAt(who);
        float amount = befriended ? DRAIN_MONSTER : DRAIN;
        if (!who.hurt(her.damageSources().mobAttack(her), amount)) return false;
        her.heal(amount);
        paralyse(who, HOLD);                                                // held fast
        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, who.getX(), who.getY() + who.getBbHeight() * 0.6, who.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
        level.sendParticles(ParticleTypes.SOUL, her.getX(), her.getEyeY(), her.getZ(), 2, 0.2, 0.2, 0.2, 0.01);
        return true;
    }

    @Nullable
    private static UUID friendOf(AbstractMonsterGirl her) {
        Player p = her.bondedPlayer(Orders.AFFECTION, 32);
        return p == null ? null : p.getUUID();
    }

    /** One step of her mind (every 5 ticks). */
    public static void think(ServerLevel level, AbstractMonsterGirl her) {
        if (level.getGameTime() < her.getPersistentData().getLong(STAGGERED)) return;
        boolean befriended = her.hasAnyAffection() && friendOf(her) != null;
        LivingEntity held = gripping(level, her);
        if (held != null) {
            if (held.distanceToSqr(her) > 16 * 16) letGo(her);
            else if (her.tickCount % 20 < 5) drain(level, her, held, befriended);
            else if (held.distanceToSqr(her) > REACH * REACH) her.getNavigation().moveTo(held, 1.1);
            return;
        }
        if (her.getPersistentData().hasUUID(GRIP)) letGo(her);
        if (befriended) {
            Player friend = her.bondedPlayer(Orders.AFFECTION, 32);
            LivingEntity prey = null;
            for (Mob m : level.getEntitiesOfClass(Mob.class, her.getBoundingBox().inflate(GUARD), m -> m instanceof Enemy && !(m instanceof Bonded) && m.isAlive()
                    && (friend == null || m.distanceToSqr(friend) < GUARD * GUARD * 2))) {
                if (prey == null || m.distanceToSqr(her) < prey.distanceToSqr(her)) prey = m;
            }
            if (prey == null) return;
            if (canScream(her)) scream(level, her, true);
            grip(level, her, prey);
            return;
        }
        if (!waiting(level, her)) return;
        for (Player p : level.players()) {
            if (p.isCreative() || p.isSpectator() || her.getAffection(p) >= Orders.AFFECTION || !inFront(her, p)) continue;
            if (canScream(her)) scream(level, her, false);
            grip(level, her, p);
            return;
        }
    }

    /** Wild, in the dark, holding nobody: she waits, stock-still. */
    public static boolean waiting(ServerLevel level, AbstractMonsterGirl her) {
        return !her.hasAnyAffection() && (!level.isDay() || !level.canSeeSky(her.blockPosition().above())) && gripping(level, her) == null;
    }

    /** Some stand and some crouch, always the same. */
    public static boolean crouches(Mob her) {
        return (her.getUUID().getMostSignificantBits() & 1L) == 0L;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        Entity e = event.getEntity();
        if (e.level().isClientSide) return;
        if (e instanceof LivingEntity l && l.getPersistentData().contains(FROZEN) && e.level().getGameTime() >= l.getPersistentData().getLong(FROZEN)) release(l);
        if (e.tickCount % 5 == 0 && is(e) && e.level() instanceof ServerLevel level) think(level, (AbstractMonsterGirl) e);
    }

    /** A blow from the one she holds breaks her grip and staggers her. */
    @SubscribeEvent
    public static void onHurt(LivingDamageEvent.Post event) {
        if (!is(event.getEntity()) || !(event.getEntity().level() instanceof ServerLevel level)) return;
        AbstractMonsterGirl her = (AbstractMonsterGirl) event.getEntity();
        LivingEntity held = gripping(level, her);
        if (held != null && event.getSource().getEntity() == held) {
            letGo(her);
            release(held);
            her.getPersistentData().putLong(STAGGERED, level.getGameTime() + STAGGER);
        }
    }

    /** Waiting, she doesn't stroll or look about: she stands (or crouches) stock-still. */
    public static final class WaitGoal extends Goal {
        private final AbstractMonsterGirl her;

        public WaitGoal(AbstractMonsterGirl her) {
            this.her = her;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.her.level() instanceof ServerLevel level && !this.her.isBaby() && waiting(level, this.her);
        }

        @Override
        public void start() {
            this.her.getNavigation().stop();
            if (crouches(this.her)) this.her.setAction(AbstractMonsterGirl.Action.SITTING);
        }

        @Override
        public void stop() {
            if (this.her.getAction() == AbstractMonsterGirl.Action.SITTING && !this.her.isOrderedToSit()) this.her.setAction(AbstractMonsterGirl.Action.NONE);
        }

        @Override
        public void tick() {
            this.her.getNavigation().stop();
            this.her.setDeltaMovement(0, this.her.getDeltaMovement().y, 0);
        }
    }
}
