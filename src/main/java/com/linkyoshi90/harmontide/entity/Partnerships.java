package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Partnerships between kinds (2026-11-13), as in the wild. Every {@link #PULSE} ticks each girl looks for her partners near her; wild or
 * befriended alike (a garden's ant and its alraune work as well as wild ones), except the trailing scavengers, who are wild only.
 * <ul>
 * <li>{@link Pair#CLEANER}: a small crustacean grooms a big sea girl (merfolk or mollusk, {@link #BIG} tall or more), both in the water:
 * the big one is soothed (Regeneration), the small one fed.</li>
 * <li>{@link Pair#ANEMONE}: a hermit crab sheltering by a sea anemone is hard to hurt; the anemone shares her meals.</li>
 * <li>{@link Pair#TICK_PICKER}: a small rodent or rabbit kind riding close by a big grazer picks her clean (Regeneration) and is safe by her (Resistance).</li>
 * <li>{@link Pair#ANT_GUARD}: ants guard a plant girl, going for any monster near her, and are fed for it.</li>
 * <li>{@link Pair#POLLINATOR}: a pollinator at a plant girl's blossoms is fed, and the plant girl thrives (Regeneration).</li>
 * <li>{@link TrailGoal}: a wild scavenger trails a wild hunter of another kind at a distance ({@link #TRAIL_NEAR} to {@link #TRAIL_FAR}),
 * to be near when she makes her kill.</li>
 * <li>Mixed herds: the alarm of one kind's herd reaches the grazers of every other kind near it (entity/Herds#alarm).</li>
 * </ul>
 * Config {@code partnerships}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Partnerships {
    public static final int PULSE = 100, BUFF = 140;
    public static final float BIG = 2.0F, SMALL = 1.6F;
    public static final double CLOSE = 4, GUARD = 8, TRAIL_NEAR = 8, TRAIL_FAR = 16, TRAIL_SEEK = 40;

    private Partnerships() {}

    public enum Pair { CLEANER, ANEMONE, TICK_PICKER, ANT_GUARD, POLLINATOR }

    private static GirlSpecies.Family family(AbstractMonsterGirl g) {
        return g.getSpecies().family();
    }

    public static boolean ant(AbstractMonsterGirl g) {
        return g.getSpecies() == GirlSpecies.ANT_BUG || g.getSpecies() == GirlSpecies.QUEEN_ANT_BUG;
    }

    /** Which partnership this girl offers (as the one who goes to her partner), or null. */
    @Nullable
    public static Pair offers(AbstractMonsterGirl g) {
        if (g.isBaby()) return null;
        if (g.getSpecies() == GirlSpecies.HERMIT_CRAB_CRUSTACEAN) return Pair.ANEMONE;
        if (family(g) == GirlSpecies.Family.CRUSTACEAN && g.getBbHeight() < SMALL) return Pair.CLEANER;
        if (ant(g)) return Pair.ANT_GUARD;
        if (Pollination.pollinator(g)) return Pair.POLLINATOR;
        if ((family(g) == GirlSpecies.Family.RODENT || family(g) == GirlSpecies.Family.JACKALOPE) && g.getBbHeight() < SMALL) return Pair.TICK_PICKER;
        return null;
    }

    /** Who she partners with. */
    public static Predicate<AbstractMonsterGirl> partner(AbstractMonsterGirl g, Pair p) {
        return o -> o != g && o.isAlive() && !o.isBaby() && switch (p) {
            case CLEANER -> (o.getSpecies().family() == GirlSpecies.Family.MERFOLK || o.getSpecies().family() == GirlSpecies.Family.MOLLUSK) && o.getBbHeight() >= BIG
                    && o.isInWater() && g.isInWater();
            case ANEMONE -> o.getSpecies() == GirlSpecies.ANEMONE_MOLLUSK;
            case TICK_PICKER -> FoodWeb.role(o) == FoodWeb.Role.GRAZER && o.getBbHeight() >= BIG;
            case ANT_GUARD, POLLINATOR -> o.getSpecies().family() == GirlSpecies.Family.FLORA;
        };
    }

    /** One round of her partnership: what it does for both of them. How many partners she helped. */
    public static int pulse(ServerLevel level, AbstractMonsterGirl g) {
        Pair p = offers(g);
        if (p == null || !HarmontideConfig.partnerships()) return 0;
        double range = p == Pair.ANT_GUARD ? GUARD : CLOSE;
        List<AbstractMonsterGirl> partners = level.getEntitiesOfClass(AbstractMonsterGirl.class, g.getBoundingBox().inflate(range), partner(g, p));
        if (partners.isEmpty()) return 0;
        for (AbstractMonsterGirl o : partners) {
            switch (p) {
                case CLEANER, TICK_PICKER, POLLINATOR -> o.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BUFF, 0, true, false, true));
                case ANEMONE -> o.heal(2.0F);
                case ANT_GUARD -> guard(level, g, o);
            }
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, (g.getX() + o.getX()) / 2, (g.getEyeY() + o.getEyeY()) / 2, (g.getZ() + o.getZ()) / 2, 3, 0.3, 0.3, 0.3, 0.0);
        }
        switch (p) {
            case ANEMONE, TICK_PICKER -> g.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BUFF, 0, true, false, true));
            default -> {}
        }
        if (p != Pair.TICK_PICKER && p != Pair.ANEMONE && !g.hasAnyAffection()) FoodWeb.fed(level, g);     // a living from it
        return partners.size();
    }

    /** The ant goes for a monster near the plant girl she guards. True if there was one. */
    public static boolean guard(ServerLevel level, AbstractMonsterGirl ant, AbstractMonsterGirl plant) {
        if (ant.getTarget() != null) return false;
        for (Mob m : level.getEntitiesOfClass(Mob.class, plant.getBoundingBox().inflate(GUARD), m -> m instanceof Enemy && !(m instanceof Bonded) && m.isAlive())) {
            ant.setTarget(m);
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof AbstractMonsterGirl g && g.tickCount % PULSE == 47 && g.level() instanceof ServerLevel level) pulse(level, g);
    }

    // --- scavengers trailing hunters --------------------------------------------------------------------------------------

    /** The wild hunter of another kind she trails, or null. */
    @Nullable
    public static AbstractMonsterGirl trails(AbstractMonsterGirl g) {
        if (!HarmontideConfig.partnerships() || !FoodWeb.forages(g) || FoodWeb.role(g) != FoodWeb.Role.SCAVENGER && g.getSpecies() != GirlSpecies.HYENA_CANID) return null;
        AbstractMonsterGirl best = null;
        for (AbstractMonsterGirl h : g.level().getEntitiesOfClass(AbstractMonsterGirl.class, g.getBoundingBox().inflate(TRAIL_SEEK),
                h -> h.getSpecies() != g.getSpecies() && FoodWeb.role(h) == FoodWeb.Role.HUNTER && FoodWeb.forages(h))) {
            if (best == null || h.distanceToSqr(g) < best.distanceToSqr(g)) best = h;
        }
        return best;
    }

    /** Where she goes to keep her distance behind the hunter, or null if she is where she wants to be. */
    @Nullable
    public static Vec3 trailSpot(AbstractMonsterGirl g, AbstractMonsterGirl hunter) {
        double d = g.distanceTo(hunter);
        if (d >= TRAIL_NEAR && d <= TRAIL_FAR) return null;
        Vec3 from = hunter.position(), dir = g.position().subtract(from);
        if (dir.lengthSqr() < 0.01) dir = new Vec3(1, 0, 0);
        return from.add(dir.normalize().scale((TRAIL_NEAR + TRAIL_FAR) / 2));
    }

    public static final class TrailGoal extends Goal {
        private final AbstractMonsterGirl girl;
        private int cooldown;

        public TrailGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0) return false;
            this.cooldown = 60;
            AbstractMonsterGirl h = trails(this.girl);
            Vec3 to = h == null ? null : trailSpot(this.girl, h);
            if (to == null) return false;
            this.girl.getNavigation().moveTo(to.x, to.y, to.z, 1.0);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.girl.getNavigation().isDone();
        }
    }
}
