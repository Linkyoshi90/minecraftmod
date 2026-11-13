package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

/**
 * Herds and schools (2026-11-13). Wild grazers (FoodWeb role GRAZER) keep together with their own kind on land, and wild fishers do in
 * the water as a school, swimming the same way. A herd is the wild, grown girls of one species within {@link #RANGE} of each other.
 * <ul>
 * <li><b>Keeping together</b> ({@link KeepTogetherGoal}): one who has strayed more than {@link #STRAY} (in water {@link #SCHOOL}) from the
 * herd's middle goes back to it; a school also swims on the way its members are heading. Centaurs follow their lead mare instead.</li>
 * <li><b>The lookout</b>: any member who sees a threat ({@link #threat}: a monster or a wild hunter of another kind within {@link #WATCH},
 * a stranger running up within {@link #WATCH} or walking up within {@link #CLOSE}; never someone sneaking, never a friend) calls the
 * alarm, and every member within {@link #RANGE} hears it.</li>
 * <li><b>The response</b> ({@link #stands}): a big herd ({@link #STAND_HERD} or more) or big kinds ({@link #BIG} blocks tall) bunch up in
 * the middle and face it, hard to hurt for a while; a small herd of small kinds flees together, all the same way, away from it.
 * In water the school always flees.</li>
 * </ul>
 * Off with the config option {@code herds}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Herds {
    public static final double RANGE = 24, STRAY = 9, SCHOOL = 4, WATCH = 12, CLOSE = 5, FLEE = 16;
    public static final int ALARM = 100, ALARM_AGAIN = 200, STAND_HERD = 4, LOOK_EVERY = 10;
    public static final float BIG = 2.2F;
    private static final String UNTIL = "HerdAlarmUntil", FROM_X = "HerdAlarmX", FROM_Z = "HerdAlarmZ", STAND = "HerdStand", NEXT = "HerdAlarmNext";

    private Herds() {}

    /** One who lives in a herd (or a school): wild, grown, a grazer or a fisher. */
    public static boolean herds(AbstractMonsterGirl g) {
        if (!HarmontideConfig.herds() || g.isBaby() || g.hasAnyAffection() || g.isPassenger() || g.isLeashed()) return false;
        FoodWeb.Role r = FoodWeb.role(g);
        return r == FoodWeb.Role.GRAZER || r == FoodWeb.Role.FISHER;
    }

    /** Her herd: the wild ones of her kind near her (herself included). */
    public static List<AbstractMonsterGirl> herd(AbstractMonsterGirl g) {
        return g.level().getEntitiesOfClass(AbstractMonsterGirl.class, g.getBoundingBox().inflate(RANGE), o -> o.getSpecies() == g.getSpecies() && herds(o));
    }

    public static Vec3 middle(List<AbstractMonsterGirl> herd) {
        double x = 0, y = 0, z = 0;
        for (AbstractMonsterGirl o : herd) {
            x += o.getX();
            y += o.getY();
            z += o.getZ();
        }
        return new Vec3(x / herd.size(), y / herd.size(), z / herd.size());
    }

    /** Whether this herd stands its ground (big herd, or big kinds) or flees (and a school always flees). */
    public static boolean stands(AbstractMonsterGirl g, int size) {
        return !g.isInWater() && (size >= STAND_HERD || g.getBbHeight() >= BIG);
    }

    // --- the lookout ---------------------------------------------------------------------------------------------------

    /** What alarms a herd: a monster or another kind's wild hunter near, a stranger rushing up (or walking right up). Sneaking gets you close. */
    public static boolean threat(AbstractMonsterGirl g, Entity e) {
        if (!e.isAlive() || e == g) return false;
        double d = e.distanceTo(g);
        if (e instanceof Player p) {
            if (p.isCreative() || p.isSpectator() || p.isShiftKeyDown() || g.getAffection(p) >= Orders.AFFECTION) return false;
            return p.isSprinting() ? d <= WATCH : d <= CLOSE;
        }
        if (d > WATCH) return false;
        if (e instanceof AbstractMonsterGirl h) return h.getSpecies() != g.getSpecies() && FoodWeb.role(h) == FoodWeb.Role.HUNTER && FoodWeb.forages(h);
        return e instanceof Enemy && !(e instanceof Bonded);
    }

    /** She looks about; at a threat she calls the alarm. What alarmed her, or null. */
    @Nullable
    public static Entity look(ServerLevel level, AbstractMonsterGirl g) {
        CompoundTag pd = g.getPersistentData();
        if (level.getGameTime() < pd.getLong(NEXT)) return null;
        for (Entity e : level.getEntities(g, g.getBoundingBox().inflate(WATCH), e -> e instanceof LivingEntity)) {
            if (threat(g, e) && g.hasLineOfSight(e)) {
                alarm(level, g, e.position());
                return e;
            }
        }
        return null;
    }

    /** The alarm call: every member of her herd within hearing hears it and answers the same way. How many heard. */
    public static int alarm(ServerLevel level, AbstractMonsterGirl lookout, Vec3 from) {
        List<AbstractMonsterGirl> herd = herd(lookout);
        boolean stand = stands(lookout, herd.size());
        long now = level.getGameTime();
        for (AbstractMonsterGirl o : herd) hear(o, now, from, stand);
        if (HarmontideConfig.partnerships()) {                              // mixed herds: the other kinds near take the warning too, each answering its own way
            for (AbstractMonsterGirl o : level.getEntitiesOfClass(AbstractMonsterGirl.class, lookout.getBoundingBox().inflate(RANGE),
                    o -> o.getSpecies() != lookout.getSpecies() && herds(o) && !alarmed(o))) {
                hear(o, now, from, stands(o, herd(o).size()));
            }
        }
        lookout.playAmbientSound();
        level.sendParticles(ParticleTypes.NOTE, lookout.getX(), lookout.getEyeY() + 0.6, lookout.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
        lookout.startTimedAction(AbstractMonsterGirl.Action.CALL, 20);
        return herd.size();
    }

    private static void hear(AbstractMonsterGirl o, long now, Vec3 from, boolean stand) {
        CompoundTag pd = o.getPersistentData();
        pd.putLong(UNTIL, now + ALARM);
        pd.putLong(NEXT, now + ALARM_AGAIN);
        pd.putDouble(FROM_X, from.x);
        pd.putDouble(FROM_Z, from.z);
        pd.putBoolean(STAND, stand);
        if (stand) o.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ALARM, 0, true, false, true));
    }

    public static boolean alarmed(AbstractMonsterGirl g) {
        return g.level().getGameTime() < g.getPersistentData().getLong(UNTIL);
    }

    public static boolean standing(AbstractMonsterGirl g) {
        return g.getPersistentData().getBoolean(STAND);
    }

    /** Where she goes when the alarm sounds: away from it with the herd, or into the middle of it. */
    public static Vec3 answer(AbstractMonsterGirl g) {
        CompoundTag pd = g.getPersistentData();
        List<AbstractMonsterGirl> herd = herd(g);
        Vec3 mid = middle(herd);
        if (pd.getBoolean(STAND)) return mid;
        Vec3 away = new Vec3(mid.x - pd.getDouble(FROM_X), 0, mid.z - pd.getDouble(FROM_Z));
        if (away.lengthSqr() < 0.01) away = new Vec3(1, 0, 0);
        away = away.normalize().scale(FLEE);
        return new Vec3(mid.x + away.x + (g.getX() - mid.x) * 0.5, g.getY(), mid.z + away.z + (g.getZ() - mid.z) * 0.5);   // all the same way, keeping their spacing
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof AbstractMonsterGirl g && g.tickCount % LOOK_EVERY == 3 && g.level() instanceof ServerLevel level && herds(g) && !alarmed(g)) {
            look(level, g);
        }
    }

    // --- goals ---------------------------------------------------------------------------------------------------------

    /** The alarm: flee with the herd, or bunch up and face it. */
    public static final class AlarmGoal extends Goal {
        private final AbstractMonsterGirl girl;

        public AlarmGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return herds(this.girl) && alarmed(this.girl);
        }

        @Override
        public void tick() {
            if (this.girl.tickCount % 10 != 0) return;
            Vec3 to = answer(this.girl);
            CompoundTag pd = this.girl.getPersistentData();
            boolean stand = pd.getBoolean(STAND);
            if (!stand || this.girl.position().distanceToSqr(to) > 4) this.girl.getNavigation().moveTo(to.x, to.y, to.z, stand ? 1.1 : 1.45);
            if (stand) this.girl.getLookControl().setLookAt(pd.getDouble(FROM_X), this.girl.getEyeY(), pd.getDouble(FROM_Z));
        }

        @Override
        public void stop() {
            this.girl.getNavigation().stop();
        }
    }

    /** Keeping with the herd (or the school). */
    public static final class KeepTogetherGoal extends Goal {
        private final AbstractMonsterGirl girl;
        @Nullable private Vec3 to;
        private int cooldown;

        public KeepTogetherGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0 || !herds(this.girl) || this.girl.getSpecies().family() == GirlSpecies.Family.CENTAUR) return false;
            this.cooldown = 40;
            this.to = gather(this.girl);
            return this.to != null;
        }

        @Override
        public void start() {
            this.girl.getNavigation().moveTo(this.to.x, this.to.y, this.to.z, 1.0);
        }

        @Override
        public boolean canContinueToUse() {
            return this.to != null && !this.girl.getNavigation().isDone() && this.girl.position().distanceToSqr(this.to) > 4;
        }
    }

    /** Where a stray goes to rejoin her herd (a school swims on as it's heading), or null if she's with them. */
    @Nullable
    public static Vec3 gather(AbstractMonsterGirl g) {
        List<AbstractMonsterGirl> herd = herd(g);
        if (herd.size() < 2) return null;
        Vec3 mid = middle(herd);
        boolean school = g.isInWater();
        if (g.position().distanceTo(mid) <= (school ? SCHOOL : STRAY)) {
            if (!school) return null;
            Vec3 heading = Vec3.ZERO;                                         // a school swims on together
            for (AbstractMonsterGirl o : herd) heading = heading.add(o.getDeltaMovement());
            if (heading.horizontalDistanceSqr() < 1.0E-4) return null;
            return mid.add(heading.normalize().scale(6));
        }
        return mid;
    }
}
