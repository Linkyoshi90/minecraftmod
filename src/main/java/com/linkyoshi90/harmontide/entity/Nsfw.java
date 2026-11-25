package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Private-server adult content. Off unless the nsfw option is on, and then only for a grown partner who can already
 * share a bed: never the young, never anyone of childlike height, never the men, and never her own father.
 * In the bed a biped kneels astride him and bounces ({@link #rock}); anyone without that pair of legs still rocks along the mattress.
 * A bucket opens {@link HandMilking} instead of filling at once.
 */
public final class Nsfw {
    /** How far she rocks along the bed, in blocks, when she has no legs to kneel on. */
    public static final double REACH = 0.18;
    private static final double SPEED = 0.5;
    /** Bounce of a kneel, in radians per tick, and how far it lifts her. */
    private static final double BOUNCE = 0.38;
    private static final double LIFT = 0.09;
    /** Models already checked for a pair of legs and shins. */
    private static final Map<String, Boolean> LEGS = new ConcurrentHashMap<>();

    public enum Kind {
        FLIRT("nsfw_flirt", 6, true),
        WIFE("nsfw_wife", 6, true),
        TUB("nsfw_tub", 3, true),
        BED("nsfw_bed", 4, false),
        MORNING("nsfw_morning", 4, false),
        MILK("nsfw_milk", 2, true);

        final String key;
        final int lines;
        /** Action bar, as a tease is. The bed and the morning stay in chat, where a longer line can be read. */
        final boolean bar;

        Kind(String key, int lines, boolean bar) {
            this.key = key;
            this.lines = lines;
            this.bar = bar;
        }
    }

    private Nsfw() {}

    public static boolean on() {
        return HarmontideConfig.nsfw();
    }

    /** The line for calling out from bed. The blunt one only while {@link #on()}. */
    public static String callKey() {
        return on() ? "message.harmontide.call_friends_nsfw" : "message.harmontide.call_friends";
    }

    /** Grown enough for the bed, and not with her own father. The option itself is separate. */
    public static boolean adult(Mob mob, Player player) {
        if (!(mob instanceof Bonded bonded) || !bonded.canShareBed()) return false;
        UUID father = bonded.getFather();
        return father == null || !father.equals(player.getUUID());
    }

    /** A pat, the tub or a milking: she already likes them, or she is their wife. */
    public static boolean close(Mob mob, Player player) {
        if (!on() || !adult(mob, player)) return false;
        if (Marriage.married(mob, player)) return true;
        return ((Bonded) mob).getAffection(player) >= Bonded.BED_AFFECTION;
    }

    /** The line she would say, or null if this moment stays as it is. */
    @Nullable
    public static String line(Mob mob, Player player, Kind kind) {
        boolean allowed = kind == Kind.BED || kind == Kind.MORNING ? on() && adult(mob, player) : close(mob, player);
        if (!allowed) return null;
        return "message.harmontide." + kind.key + "." + mob.getRandom().nextInt(kind.lines);
    }

    /** How far along the bed the rock sits at this tick (0 at tick 0). */
    public static double along(int tick) {
        return Math.sin(tick * SPEED) * REACH;
    }

    /** Where she is in the kneel bounce, -1 at the bottom and 1 at the top. */
    public static double bounce(double tick) {
        return Math.sin(tick * BOUNCE);
    }

    /** Says the line, with hearts. True if she did. */
    public static boolean say(ServerLevel level, Mob mob, Player player, Kind kind) {
        String key = line(mob, player, kind);
        if (key == null) return false;
        player.displayClientMessage(Component.translatable(key, mob.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), kind.bar);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 4, 0.25, 0.15, 0.25, 0.0);
        if (mob instanceof Bonded bonded) bonded.blushFor(200);
        return true;
    }

    /**
     * In the bed, with the option on. A biped kneels astride the sleeper and bounces; anyone else rocks along the
     * mattress. True if she did, and the caller should not also pin her to the still spot. {@code spot} and {@code yaw}
     * are the lying place (towards the foot). {@code bed} is the head of the bed, when she is really in one.
     */
    public static boolean rock(ServerLevel level, Mob mob, Player player, Vec3 spot, float yaw) {
        return rock(level, mob, player, spot, yaw, null);
    }

    public static boolean rock(ServerLevel level, Mob mob, Player player, Vec3 spot, float yaw, @Nullable BlockPos bed) {
        if (!on() || !adult(mob, player)) return false;
        if (bipedal(mob)) kneel(level, mob, spot, yaw, bed);
        else slide(level, mob, spot, yaw);
        return true;
    }

    /** She is kneeling astride someone, so the renderer folds her legs and lets her bounce. */
    public static boolean kneeling(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) return girl.getAction() == AbstractMonsterGirl.Action.KNEEL;
        if (mob instanceof AbstractHarpy harpy) return harpy.getAction() == AbstractHarpy.Action.KNEEL;
        if (mob instanceof AbstractAnuran anuran) return anuran.getAction() == AbstractAnuran.Action.KNEEL;
        return false;
    }

    /** Back to however her family lies in a bed. Used when the kneel should stop. */
    public static void lieDown(Mob mob) {
        if (!kneeling(mob)) return;
        if (mob instanceof Bonded bonded) bonded.settleInBed();
    }

    /**
     * Two legs and two shins, and a body that can fold them. A taur, a serpent, a spider, a mermaid and a slime
     * keep the old rock even when a leftover bone happens to be named like a leg.
     */
    public static boolean bipedal(Mob mob) {
        if (mob.isBaby()) return false;
        if (mob instanceof AbstractHarpy || mob instanceof AbstractAnuran) return true;
        if (!(mob instanceof AbstractMonsterGirl girl)) return false;
        GirlSpecies.Family family = girl.getSpecies().family();
        if (family == GirlSpecies.Family.LAMIA || family == GirlSpecies.Family.ARACHNE || family == GirlSpecies.Family.CENTAUR
                || family == GirlSpecies.Family.MERFOLK || family == GirlSpecies.Family.SLIME) return false;
        return legs(girl.getSpecies().id());
    }

    private static boolean legs(String model) {
        return LEGS.computeIfAbsent(model, Nsfw::readLegs);
    }

    private static boolean readLegs(String model) {
        try (InputStream in = Nsfw.class.getResourceAsStream("/assets/harmontide/geo/entity/" + model + ".geo.json")) {
            if (in == null) return false;
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return json.contains("\"name\": \"leg_l\"") && json.contains("\"name\": \"leg_r\"")
                    && json.contains("\"name\": \"shin_l\"") && json.contains("\"name\": \"shin_r\"");
        } catch (Exception e) {
            return false;
        }
    }

    /** Upright over his hips, facing him, rising and falling. */
    private static void kneel(ServerLevel level, Mob mob, Vec3 spot, float yaw, @Nullable BlockPos bed) {
        double wave = bounce(mob.tickCount);
        double x = spot.x;
        double z = spot.z;
        double y = spot.y + wave * LIFT;
        float face = yaw;
        if (bed != null && mob.level().getBlockState(bed).getBlock() instanceof BedBlock) {
            BlockState state = mob.level().getBlockState(bed);
            Direction facing = state.hasProperty(BedBlock.FACING) ? state.getValue(BedBlock.FACING) : Direction.NORTH;
            Direction foot = facing.getOpposite();
            Vec3 head = Vec3.atBottomCenterOf(bed);
            x = head.x + foot.getStepX() * 0.58;
            z = head.z + foot.getStepZ() * 0.58;
            y = head.y + 0.50 + wave * LIFT;
            face = facing.toYRot();
        }
        mob.setDeltaMovement(Vec3.ZERO);
        mob.moveTo(x, y, z, face, 0.0F);
        mob.setYBodyRot(face);
        mob.setYHeadRot(face);
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(AbstractMonsterGirl.Action.KNEEL);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(AbstractHarpy.Action.KNEEL);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(AbstractAnuran.Action.KNEEL);
        if (mob.tickCount % 40 == 0) {
            level.sendParticles(ParticleTypes.HEART, x, y + 0.9, z, 2, 0.15, 0.1, 0.15, 0.0);
            ((Bonded) mob).blushFor(80);
        }
    }

    /** The old motion: a short slide along the mattress, for a body that cannot kneel. */
    private static void slide(ServerLevel level, Mob mob, Vec3 spot, float yaw) {
        lieDown(mob);
        double shift = along(mob.tickCount);
        double rad = Math.toRadians(yaw);
        double x = spot.x - Math.sin(rad) * shift;
        double z = spot.z + Math.cos(rad) * shift;
        double y = spot.y + Math.abs(Math.sin(mob.tickCount * SPEED)) * 0.05;
        mob.setDeltaMovement(Vec3.ZERO);
        mob.moveTo(x, y, z, yaw, 0.0F);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        if (mob.tickCount % 40 == 0) {
            level.sendParticles(ParticleTypes.HEART, x, y + 0.6, z, 2, 0.15, 0.1, 0.15, 0.0);
            ((Bonded) mob).blushFor(80);
        }
    }
}
