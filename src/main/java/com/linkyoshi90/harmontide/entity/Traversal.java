package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Arachne;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Lamia;
import com.linkyoshi90.harmontide.entity.girl.SlimeGirl;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Getting about together: what each family's body lets her do for the player she likes (and for herself).
 *
 * <ul>
 *   <li><b>Shoulder perches.</b> The small ones ride on the player's left shoulder: a gliderune, a jumping-spider youngling,
 *   a fairy or pixie, the waterlil cat, a kesaran-pasaran, the living doll. A crouching empty hand lifts a spiderling or
 *   gliderune up; any of them told to follow hops up by herself. Two seconds of crouching sets her down (for a minute).
 *   A spiderling is a safety line (no fall damage) and parts any cobweb; a gliderune slows falls; a fairy mends a little;
 *   a pixie or a kesaran-pasaran brings luck; the waterlil lets you breathe under water; the doll lends her night eyes.</li>
 *   <li><b>A lift.</b> A bonded harpy (25+) given a crouching empty hand takes the player up hanging from her talons. The
 *   big birds fly ({@link AbstractHarpy.Lift#STRONG}, half a minute, longer for a devoted one), most only glide them down
 *   ({@link AbstractHarpy.Lift#GLIDE}); the hummingbird and finch are too small. Out of breath she sinks and sets them
 *   down; letting go (crouching) floats them down slowly.</li>
 *   <li><b>A catch.</b> A bonded harpy within {@link #CATCH_REACH} swoops on a friend falling past and carries them.</li>
 *   <li><b>A tow.</b> Ducks and swans float, and a swimming friend (10+) who takes hold of one is paddled along. A mermaid
 *   takes a swimmer by the hand and dives with them wherever they look, her breath theirs.</li>
 *   <li><b>Coils.</b> A bonded lamia (25+) within {@link #COIL_REACH} of a friend struck down to {@link #COIL_HEALTH} of
 *   their health lunges and wraps them in her coils: Resistance and Regeneration for a while, and whatever was after
 *   them turns on her instead. Once a minute.</li>
 *   <li><b>A bounce.</b> Landing on or right beside a slime girl bounces a player back up (unless they crouch), no damage.</li>
 *   <li><b>A ferry.</b> A capybara, turtle, snapping turtle, water buffalo, hippo, polar bear or spinosaur friend (10+) lets a
 *   swimming player climb onto her back and swims them where they face; ashore they slide off. The octopus, Ika, the
 *   squidkin, the krakens, the cuttlefish and the nautilus dive with them instead, sharing their air.</li>
 *   <li><b>A golem's toss.</b> Crouch and hold out an empty hand to a big construct friend (golem, talosling, automaton,
 *   gargoyle): she throws you up and forward, and you land unhurt.</li>
 *   <li><b>Company on the road.</b> What a friend following you lends you: a cat girl, her landing (falls hurt half as much);
 *   a jackalope or a kangaroo, her spring (Jump Boost); a wolf or a hellhound, the pack's pace when you sprint (Speed).</li>
 *   <li><b>A pull along the rails.</b> A centaur following a player who sits in a minecart takes the front and pulls it along
 *   the track, like a furnace minecart.</li>
 * </ul>
 * The rest of the traversal moves live with their family: harpies carry and catch ({@link AbstractHarpy}), ducks and
 * swans swim and tow, mermaids tow divers, lamia coil round a friend in trouble, slime girls bounce a falling friend
 * back up, arachne swing on silk and spin bridges.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Traversal {
    /** How far out from the neck a shoulder rider sits, and how far below the top of the head (of the player's height). */
    public static final double SHOULDER_OUT = 0.38, SHOULDER_DOWN = 0.24;
    /** Crouching this long sets a shoulder rider down. */
    public static final int CROUCH_OFF = 40;
    /** A spiderling following her player hops up onto the shoulder from this close; once set down she waits this long. */
    public static final double HOP_UP = 4.0;
    public static final int SET_DOWN = 1200;
    public static final String PERCH_REST = "PerchRestUntil";

    /** How far below her feet a carried player hangs (their feet). */
    public static final double HANG = 1.5;
    /** How long a big bird carries someone (a devoted one half again), how long a glider holds on, the first flapping climb of a glide, and the rest after. */
    public static final int STRONG_TICKS = 600, GLIDE_TICKS = 240, TAKEOFF = 30, CARRY_REST = 200;
    /** A fall this long, past a bonded harpy this close, ends in her talons. */
    public static final float CATCH_FALL = 6.0F;
    public static final double CATCH_REACH = 12.0;
    /** The big birds: they fly with someone hanging on. */
    public static final Set<String> STRONG = Set.of("roc_harpy", "ziz_harpy", "garuda_harpy", "condorune_harpy", "albatross_harpy", "pelican_harpy",
            "swan_harpy", "phoenix_harpy", "zhuque_harpy", "bennu_harpy", "tengu_harpy", "yatagarasu_harpy", "mirror_crane_harpy", "ibisking_harpy",
            "owl_harpy", "stigowl_harpy", "ironbeak_harpy", "alkonost_harpy", "argentowlis_harpy");
    /** Too small to carry anyone at all. */
    public static final Set<String> TINY = Set.of("hummingbird_harpy", "finch_harpy");

    /** A lamia this close to a friend hurt down to this share of their health wraps them up; then she rests this long. */
    public static final double COIL_REACH = 6.0;
    public static final float COIL_HEALTH = 0.4F;
    public static final int COIL_REST = 1200, COIL_GUARD = 160;
    /** How close to a slime girl a landing has to be to bounce. */
    public static final double BOUNCE_REACH = 1.3;

    /** The ones who ferry a swimmer across the water on their backs, and how fast. */
    /** The tentacled ones dive with their swimmer instead (wherever they look, under the water too), and share their air. */
    public static final Set<GirlSpecies> DIVERS = Set.of(GirlSpecies.OCTOPUS_MOLLUSK, GirlSpecies.IKA_MOLLUSK, GirlSpecies.SQUIDKIN_MOLLUSK, GirlSpecies.KRAKEN_MOLLUSK,
            GirlSpecies.GIANT_KRAKEN_MOLLUSK, GirlSpecies.CUTTLEFISH_MOLLUSK, GirlSpecies.NAUTILUS_MOLLUSK);
    public static final Set<GirlSpecies> FERRIES = java.util.stream.Stream.concat(DIVERS.stream(), Set.of(GirlSpecies.CAPYBARA_RODENT, GirlSpecies.TURTLE_LIZARDFOLK,
            GirlSpecies.SNAPPING_TURTLE_LIZARDFOLK, GirlSpecies.WATER_BUFFALO_MINOTAUR, GirlSpecies.HIPPO_MINOTAUR, GirlSpecies.POLAR_BEAR_CANID,
            GirlSpecies.SPINOSAUR_THEROPOD, GirlSpecies.BEAVER_RODENT).stream()).collect(java.util.stream.Collectors.toUnmodifiableSet());
    public static final float FERRY_SPEED = 0.3F;

    /** How fast a centaur pulls a minecart, and how close behind her it must be. */
    public static final double PULL = 0.4, PULL_REACH = 8;

    private Traversal() {}

    /** Hands out one of this run's advancements (they have a single "done" criterion). */
    public static void award(Player p, String id) {
        if (!(p instanceof ServerPlayer sp)) return;
        var a = sp.server.getAdvancements().get(HarmontideMod.id(id));
        if (a != null) sp.getAdvancements().award(a, "done");
    }

    /** The whole of it can be switched off (config: traversal). */
    public static boolean on() {
        return com.linkyoshi90.harmontide.HarmontideConfig.traversal();
    }

    // --- company on the road ------------------------------------------------------------------------------------------

    public static final String CAT_GRACE = "CatGraceUntil";
    public static final double COMPANY = 16;

    /** The girls following this player (told to, within {@link #COMPANY}). */
    public static java.util.List<AbstractMonsterGirl> followers(Player p) {
        return p.level().getEntitiesOfClass(AbstractMonsterGirl.class, p.getBoundingBox().inflate(COMPANY), g -> g.isAlive() && !g.isBaby()
                && Orders.current(g) == Orders.Command.FOLLOW && p.getUUID().equals(g.getOrders().commanderId()));
    }

    /** Every second: what the friends following this player lend them. */
    public static void company(ServerPlayer p) {
        if (!on()) return;
        for (AbstractMonsterGirl g : followers(p)) {
            GirlSpecies s = g.getSpecies();
            if (s.family() == GirlSpecies.Family.NEKOMATA) p.getPersistentData().putLong(CAT_GRACE, p.level().getGameTime() + 40);
            if (s.family() == GirlSpecies.Family.JACKALOPE || s == GirlSpecies.KANGAROO_MARSUPIAL) p.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 0, true, false));
            if ((s == GirlSpecies.WOLF_CANID || s == GirlSpecies.HELLHOUND_CANID) && p.isSprinting()) p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, true, false));
        }
    }

    /** A cat girl at their heels: they land like her, and the fall hurts half as much. */
    @SubscribeEvent
    public static void onCatLanding(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && p.level().getGameTime() < p.getPersistentData().getLong(CAT_GRACE)) {
            event.setDamageMultiplier(event.getDamageMultiplier() * 0.5F);
        }
    }

    // --- ferries ------------------------------------------------------------------------------------------------------

    public static boolean ferries(GirlSpecies s) {
        return on() && FERRIES.contains(s);
    }

    /** A swimming friend (10+) with an empty hand climbs onto a ferry's back, when both are in the water. */
    @Nullable
    public static net.minecraft.world.InteractionResult ferryInteract(AbstractMonsterGirl g, Player player, net.minecraft.world.item.ItemStack stack) {
        if (!stack.isEmpty() || player.isShiftKeyDown() || !g.ferries() || !player.isInWater() || !g.isInWater() || g.isVehicle() || player.isPassenger()
                || g.getAffection(player) < Orders.AFFECTION) return null;
        if (!g.level().isClientSide) {
            g.getNavigation().stop();
            g.setOrderedToSit(false);
            if (player.startRiding(g, true)) award(player, "ferried");
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(g.level().isClientSide);
    }

    // --- a golem's toss -----------------------------------------------------------------------------------------------

    public static final Set<GirlSpecies> TOSSERS = Set.of(GirlSpecies.GOLEM_CONSTRUCT, GirlSpecies.TALOSLING_CONSTRUCT, GirlSpecies.AUTOMATON_CONSTRUCT,
            GirlSpecies.GARGOYLE_CONSTRUCT);
    public static final String TOSSED = "TossedUntil";

    /** A crouching empty hand to a big construct friend (10+): up and forward you go. */
    @Nullable
    public static net.minecraft.world.InteractionResult toss(AbstractMonsterGirl g, Player p, net.minecraft.world.item.ItemStack stack) {
        if (!on() || !TOSSERS.contains(g.getSpecies()) || g.isBaby() || !stack.isEmpty() || !p.isShiftKeyDown() || p.isPassenger()
                || g.getAffection(p) < Orders.AFFECTION) return null;
        if (!g.level().isClientSide) {
            Vec3 look = p.getLookAngle().multiply(1, 0, 1).normalize();
            p.setDeltaMovement(look.x * 0.55, 1.15, look.z * 0.55);
            p.hurtMarked = true;
            p.getPersistentData().putLong(TOSSED, g.level().getGameTime() + 100);
            g.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            g.playSound(net.minecraft.sounds.SoundEvents.IRON_GOLEM_ATTACK, 0.8F, 1.2F);
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(g.level().isClientSide);
    }

    /** A tossed player lands unhurt. */
    @SubscribeEvent
    public static void onTossLanding(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && p.level().getGameTime() < p.getPersistentData().getLong(TOSSED)) {
            p.getPersistentData().remove(TOSSED);
            event.setDistance(0.0F);
            event.setCanceled(true);
        }
    }

    // --- centaurs pull minecarts -------------------------------------------------------------------------------------

    /** A centaur following this player, close by, or null. */
    @Nullable
    public static AbstractMonsterGirl cartHorse(Player p) {
        for (AbstractMonsterGirl g : p.level().getEntitiesOfClass(AbstractMonsterGirl.class, p.getBoundingBox().inflate(PULL_REACH),
                g -> g.isAlive() && !g.isBaby() && g.getSpecies().family() == GirlSpecies.Family.CENTAUR && !g.isVehicle())) {
            if (Orders.current(g) == Orders.Command.FOLLOW && p.getUUID().equals(g.getOrders().commanderId())) return g;
        }
        return null;
    }

    /** A player sitting in a minecart on rails with a centaur following: she pulls it along the way it rolls (or the way they face). Whether she did. */
    public static boolean pullCart(ServerPlayer p) {
        if (!on() || !(p.getVehicle() instanceof net.minecraft.world.entity.vehicle.AbstractMinecart cart)) return false;
        if (!net.minecraft.world.level.block.BaseRailBlock.isRail(cart.level().getBlockState(cart.blockPosition()))
                && !net.minecraft.world.level.block.BaseRailBlock.isRail(cart.level().getBlockState(cart.blockPosition().below()))) return false;
        AbstractMonsterGirl horse = cartHorse(p);
        if (horse == null) return false;
        Vec3 v = cart.getDeltaMovement();
        Vec3 way = new Vec3(v.x, 0, v.z);
        if (way.lengthSqr() < 1.0E-4) way = Vec3.directionFromRotation(0, p.getYRot());
        way = way.normalize();
        if (v.horizontalDistance() < PULL) cart.setDeltaMovement(way.x * PULL, v.y, way.z * PULL);
        horse.getNavigation().moveTo(cart.getX() + way.x * 2.5, cart.getY(), cart.getZ() + way.z * 2.5, 1.6);   // she leads at the front
        return true;
    }

    // --- lamia coils --------------------------------------------------------------------------------------------------

    /** A friend struck hard near a bonded lamia: she lunges and coils round them. Her, or null. */
    @Nullable
    public static Lamia coil(ServerPlayer p, @Nullable LivingEntity attacker) {
        if (!on() || p.isPassenger() || p.isSleeping() || p.isSpectator() || p.getAbilities().instabuild || p.getHealth() > p.getMaxHealth() * COIL_HEALTH) return null;
        ServerLevel level = p.serverLevel();
        for (Lamia l : level.getEntitiesOfClass(Lamia.class, p.getBoundingBox().inflate(COIL_REACH), l -> l.isAlive() && l.getAffection(p) >= Bonded.BED_AFFECTION)) {
            if (l.isBaby() || l.isVehicle() || l.isPassenger() || l.getSpecies() == GirlSpecies.WADJET_LAMIA) continue;
            if (level.getGameTime() < l.getPersistentData().getLong("CoilRestUntil")) continue;
            if (!level.noCollision(l, l.getBoundingBox().move(p.position().subtract(l.position())))) continue;
            l.setPos(p.position());                                        // the lunge
            l.setOrderedToSit(false);
            l.getNavigation().stop();
            if (!p.startRiding(l, true)) continue;
            l.getPersistentData().putLong("CoilRestUntil", level.getGameTime() + COIL_REST);
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, COIL_GUARD, 1, true, true));
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, COIL_GUARD / 2, 0, true, true));
            for (Mob m : level.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(12), m -> m.getTarget() == p || m == attacker)) m.setTarget(l);
            l.playSound(net.minecraft.sounds.SoundEvents.CAT_HISS, 1.2F, 0.6F);
            p.displayClientMessage(Component.translatable("message.harmontide.coiled", l.getDisplayName()), true);
            return l;
        }
        return null;
    }

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer p && event.getNewDamage() > 0 && p.isAlive() && event.getSource().getEntity() instanceof LivingEntity attacker
                && attacker != p) coil(p, attacker);
    }

    // --- slime bounces ------------------------------------------------------------------------------------------------

    /** The slime girl a landing player came down on (or right beside), or null. */
    @Nullable
    public static SlimeGirl bouncer(Player p) {
        for (SlimeGirl s : p.level().getEntitiesOfClass(SlimeGirl.class, p.getBoundingBox().inflate(BOUNCE_REACH, 1.0, BOUNCE_REACH),
                s -> s.isAlive() && !s.isBaby() && s.getTarget() != p)) return s;
        return null;
    }

    /** How fast a fall of this many blocks sends you back up off her: most of the way, never sky-high. */
    public static double bounceSpeed(float distance) {
        return Math.min(1.4, 0.25 + Math.sqrt(distance) * 0.22);
    }

    // --- harpies: a lift, a catch, a tow -----------------------------------------------------------------------------

    public static AbstractHarpy.Lift liftOf(String species) {
        return TINY.contains(species) ? AbstractHarpy.Lift.NONE : STRONG.contains(species) ? AbstractHarpy.Lift.STRONG : AbstractHarpy.Lift.GLIDE;
    }

    /** How long her wings hold out with someone hanging on. */
    public static int liftTicks(AbstractHarpy.Lift lift, int affection) {
        if (lift == AbstractHarpy.Lift.STRONG) return affection >= Bonded.FULL_AFFECTION ? STRONG_TICKS * 3 / 2 : STRONG_TICKS;
        return lift == AbstractHarpy.Lift.GLIDE ? GLIDE_TICKS : 0;
    }

    /**
     * One tick of flight with a passenger, from the rider's input: forward flies where they look, back brakes. A big bird with
     * breath left climbs and hovers at will; a glider only flaps up for the first moments and then sinks gently; out of breath,
     * anyone sinks.
     */
    public static Vec3 flightStroke(Vec3 v, Player rider, AbstractHarpy.Lift lift, int left) {
        boolean strong = lift == AbstractHarpy.Lift.STRONG && left > 0;
        v = v.scale(0.88);
        if (rider.zza > 0) v = v.add(rider.getLookAngle().scale(strong ? 0.09 : 0.07));
        else if (rider.zza < 0) v = v.scale(0.7);
        if (strong) return rider.zza > 0 ? v : new Vec3(v.x, v.y * 0.8, v.z);    // she holds height while they look about
        boolean flapping = lift == AbstractHarpy.Lift.GLIDE && left > GLIDE_TICKS - TAKEOFF;
        double ceiling = left <= 0 ? -0.18 : flapping ? 0.15 : -0.04;
        return new Vec3(v.x, Math.min(v.y - 0.01, ceiling), v.z);
    }

    /** One stroke of a tow: along the way the swimmer faces when they push forward, bobbing at the surface. */
    public static Vec3 towStroke(Vec3 v, Player rider, float yRot, double depth, float speed) {
        float s = rider.zza > 0 ? (rider.isSprinting() ? speed * 1.35F : speed) : 0.0F;
        Vec3 fwd = Vec3.directionFromRotation(0, yRot).scale(s);
        double up = depth > 0.4 ? 0.04 : depth > 0 ? 0.0 : -0.08;
        return new Vec3(Mth.lerp(0.25, v.x, fwd.x), Mth.clamp(v.y * 0.6 + up, -0.2, 0.12), Mth.lerp(0.25, v.z, fwd.z));
    }

    /** One stroke of a mermaid's tow: wherever the diver looks, up, down or along, quicker when they sprint. */
    public static Vec3 diveStroke(Vec3 v, Player diver, float speed) {
        float s = diver.zza > 0 ? speed * (diver.isSprinting() ? 2.75F : 2.0F) : 0.0F;
        Vec3 want = diver.getLookAngle().scale(s);
        return new Vec3(Mth.lerp(0.3, v.x, want.x), Mth.lerp(0.3, v.y, want.y), Mth.lerp(0.3, v.z, want.z));
    }

    /** A friend falling past a bonded harpy (or a dragon who would carry them): she swoops and catches them. Her, or null. */
    @Nullable
    public static net.minecraft.world.entity.Mob catchFalling(ServerPlayer p) {
        if (!on() || p.fallDistance < CATCH_FALL || p.isPassenger() || p.isFallFlying() || p.getAbilities().flying || p.isInWater() || p.isSpectator()) return null;
        for (AbstractHarpy h : p.level().getEntitiesOfClass(AbstractHarpy.class, p.getBoundingBox().inflate(CATCH_REACH),
                h -> h.getAffection(p) >= Bonded.BED_AFFECTION && h.canCarry())) {
            if (h.pickUp(p, true)) {
                p.displayClientMessage(Component.translatable("message.harmontide.harpy_caught", h.getDisplayName()), true);
                award(p, "caught_falling");
                return h;
            }
        }
        for (com.linkyoshi90.harmontide.entity.girl.Dragon d : p.level().getEntitiesOfClass(com.linkyoshi90.harmontide.entity.girl.Dragon.class,
                p.getBoundingBox().inflate(CATCH_REACH), com.linkyoshi90.harmontide.entity.girl.Dragon::isAlive)) {   // (moved only after the search)
            if (!d.catchFalling(p)) continue;
            p.displayClientMessage(Component.translatable("message.harmontide.harpy_caught", d.getDisplayName()), true);
            award(p, "caught_falling");
            return d;
        }
        return null;
    }

    // --- shoulder perches ---------------------------------------------------------------------------------------------

    /** The tiny ones, grown, who fit on a shoulder. */
    public static final Set<GirlSpecies> TINY_PERCHERS = Set.of(GirlSpecies.GLIDERUNE_MARSUPIAL, GirlSpecies.FAIRY_BUG, GirlSpecies.PIXI_BUG,
            GirlSpecies.WATERLIL_NEKOMATA, GirlSpecies.KESARAN_PASARAN_FLORA, GirlSpecies.LIVING_DOLL_UNDEAD);

    /** Whether she is small enough to ride a shoulder: one of the tiny kinds, or a jumping spider still young. */
    public static boolean perches(AbstractMonsterGirl g) {
        return TINY_PERCHERS.contains(g.getSpecies()) || g.getSpecies() == GirlSpecies.JUMPING_SPIDER_ARACHNE && g.isBaby();
    }

    /** What she does for the player she rides on (every two seconds). */
    public static void perchGift(AbstractMonsterGirl g, Player p) {
        switch (g.getSpecies()) {
            case FAIRY_BUG -> p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 0, true, false));
            case PIXI_BUG, KESARAN_PASARAN_FLORA -> p.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 0, true, false));
            case WATERLIL_NEKOMATA -> {
                if (p.isInWater()) p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, true, false));
            }
            case LIVING_DOLL_UNDEAD -> {
                if (!p.level().isDay() || p.level().getMaxLocalRawBrightness(p.blockPosition()) < 7) p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false));
            }
            default -> {
            }
        }
    }

    /**
     * Where on her the shoulder goes, as {@link Entity#getVehicleAttachmentPoint} wants it: the player puts her at the
     * top of their head, and this moves her down to the left shoulder (turning with the player's body).
     */
    public static Vec3 shoulderSeat(Player p) {
        float body = p.yBodyRot * Mth.DEG_TO_RAD;
        double h = p.getBbHeight();
        return new Vec3(-Mth.cos(body) * SHOULDER_OUT, h * SHOULDER_DOWN, -Mth.sin(body) * SHOULDER_OUT);
    }

    /** Up onto a friend's shoulder, if it is free. Whether she is up there now. */
    public static boolean perchOn(AbstractMonsterGirl g, Player p) {
        if (g.getVehicle() == p) return true;
        if (!on() || !perches(g) || p.isVehicle() || p.isSleeping() || p.isSpectator() || g.isVehicle()) return false;
        if (g.isPassenger()) g.stopRiding();
        boolean up = g.startRiding(p, true);
        if (up) award(p, "shoulder_friend");
        return up;
    }

    /** Every tick on a shoulder (server): facing where her player faces, off again after a long crouch or when she has outgrown it. */
    public static void tickPerch(AbstractMonsterGirl g, Player p, int crouching) {
        g.setYRot(p.yBodyRot);
        g.yBodyRot = p.yBodyRot;
        if (crouching > CROUCH_OFF) g.getPersistentData().putLong(PERCH_REST, g.level().getGameTime() + SET_DOWN);   // set down: she stays down a while
        else if (g.tickCount % 40 == 0) perchGift(g, p);
        if (crouching > CROUCH_OFF || !perches(g) || p.isSleeping() || !p.isAlive() || p.isSpectator()) g.stopRiding();
    }

    /** A small one following her player within {@link #HOP_UP} hops up onto their shoulder (not for a minute after being set down). Whether she did. */
    public static boolean hopUp(AbstractMonsterGirl g) {
        if (!on() || !perches(g) || g.isPassenger() || g.isOrderedToSit()) return false;
        if (g.level().getGameTime() < g.getPersistentData().getLong(PERCH_REST)) return false;
        if (Orders.current(g) != Orders.Command.FOLLOW) return false;
        Player p = g.getOrders().commander(g);
        if (p == null || p.distanceToSqr(g) > HOP_UP * HOP_UP || g.getAffection(p) < Orders.AFFECTION) return false;
        return perchOn(g, p);
    }

    /** The spiderling riding this player's shoulder, or null. */
    @Nullable
    public static Arachne spiderling(Player p) {
        for (Entity e : p.getPassengers()) if (e instanceof Arachne a && a.getSpecies() == GirlSpecies.JUMPING_SPIDER_ARACHNE && a.isBaby()) return a;
        return null;
    }

    /** With a spiderling aboard, a cobweb no longer holds the player. Whether it let go of them. */
    public static boolean partWebs(Player p) {
        if (p.stuckSpeedMultiplier.lengthSqr() < 1.0E-7 || !on() || spiderling(p) == null) return false;
        AABB box = p.getBoundingBox().deflate(1.0E-3);
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            if (p.level().getBlockState(pos).is(Blocks.COBWEB)) {
                p.stuckSpeedMultiplier = Vec3.ZERO;
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        partWebs(event.getEntity());   // both sides: the player's own client moves them
        if (event.getEntity() instanceof ServerPlayer p && p.fallDistance >= CATCH_FALL && p.tickCount % 2 == 0) catchFalling(p);
        if (event.getEntity() instanceof ServerPlayer rider && rider.isPassenger() && rider.tickCount % 5 == 0) pullCart(rider);
        if (event.getEntity() instanceof ServerPlayer walker && walker.tickCount % 20 == 3) company(walker);
    }

    /** Landing on a slime girl: no harm, and up again (crouch to just sink in). Before the soft-landing cushion (entity/Company). */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBounce(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || event.getDistance() <= 2.0F || !on()) return;
        SlimeGirl slime = bouncer(p);
        if (slime == null) return;
        if (!p.isShiftKeyDown()) {
            Vec3 v = p.getDeltaMovement();
            p.setDeltaMovement(v.x, bounceSpeed(event.getDistance()), v.z);
            p.hurtMarked = true;
        }
        slime.startTimedAction(AbstractMonsterGirl.Action.BOUNCE, 10);
        slime.playSound(net.minecraft.sounds.SoundEvents.SLIME_JUMP, 1.0F, 1.1F);
        p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.ITEM_SLIME, p.getX(), p.getY() + 0.1, p.getZ(), 10, 0.4, 0.1, 0.4, 0.1);
        event.setDistance(0.0F);
        event.setCanceled(true);
    }

    /** A spiderling on the shoulder is a safety line: falls never hurt. */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player p && spiderling(p) != null) {
            if (!p.level().isClientSide && event.getDistance() > 4.0F) {
                p.displayClientMessage(Component.translatable("message.harmontide.safety_line", spiderling(p).getDisplayName()), true);
            }
            event.setDistance(0.0F);
            event.setCanceled(true);
        }
    }
}
