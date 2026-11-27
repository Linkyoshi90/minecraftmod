package com.linkyoshi90.harmontide.leviathan;

import com.linkyoshi90.harmontide.HarmontideConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Dream-Leviathan's story in a world, kept with the overworld. Once per world (after {@code leviathanFirstDay} days, with someone
 * online) she flies in sick, circles over a player for a while, then lands near them and the server is told where. Inside her
 * (leviathan/Interior) is the infection making her ill. Cure her before {@code leviathanDays} days are up and she rises healed and
 * blesses whoever touched her heart; leave her and she dies there and slowly rots to bones. Afterwards she only passes over now and
 * then, healthy, unless a dream horn (leviathan/DreamHornItem) calls her back sick again for another round.
 * <p>
 * She is not a real entity while far away: this keeps her position, and clients draw her as a shape in the sky (client/DreamSky).
 * Her body (leviathan/DreamLeviathan) is spawned only while a player is within {@link #BODY_RANGE}.
 */
public class LeviathanEvent extends SavedData {
    private static final String NAME = "harmontide_dream_leviathan";
    public static final int IDLE = 0, INBOUND = 1, LANDED = 2, DEAD = 3, RISING = 4, FLYBY = 5;
    /** Her body exists while a player is this close (horizontally). */
    public static final double BODY_RANGE = 200.0;
    /** How long she circles over her chosen player before she comes down. */
    public static final int CIRCLE_TICKS = 2400;
    public static final double CRUISE = 0.7, CIRCLE_RADIUS = 110.0, ALTITUDE = 190.0;
    /** How long her body takes to rot: one day dead, two rotting, three as bones. */
    public static final long DEAD_FOR = 24000L, ROT_FOR = 72000L, GONE_AFTER = 144000L;
    public static final int SCALES = 24;

    public int phase = IDLE;
    /** Whether her first, natural visit has happened in this world, and whether she has ever been healed. */
    public boolean appeared, healedOnce;
    public double x, y, z, vx, vy, vz;
    public float yaw, pitch;
    public long phaseStart, deadline, nextFlyby;
    public int warned;
    @Nullable
    public BlockPos landing;
    @Nullable
    public UUID target;
    public double cx, cz, toX, toZ;
    /** Which visit this is (each landing gets fresh rooms inside her: leviathan/Interior). */
    public int run;
    /** How many of the five waves inside her are beaten (she looks less sick for each). */
    public int progress;
    public int scales;
    /** Ticks she circles before coming down this visit (a horn call is quicker). */
    public int circleFor = CIRCLE_TICKS;
    @Nullable
    private UUID body;
    private final Map<UUID, Long> pulled = new HashMap<>();

    public static LeviathanEvent get(ServerLevel any) {
        ServerLevel level = any.getServer().overworld();
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(LeviathanEvent::new, LeviathanEvent::load, null), NAME);
    }

    public static boolean nsfwEntry() {
        return HarmontideConfig.nsfw();
    }

    // --- every tick ----------------------------------------------------------------------------------------------------------

    public void tick(ServerLevel level) {
        long now = level.getGameTime();
        List<ServerPlayer> players = level.players();
        if (this.phase == IDLE) {
            if (!this.appeared && HarmontideConfig.dreamLeviathan() && now % 600 == 0 && !players.isEmpty()
                    && level.getDayTime() / 24000L >= HarmontideConfig.leviathanFirstDay() && level.isDay() && level.random.nextInt(6) == 0) {
                this.appeared = true;
                this.startInbound(level, players.get(level.random.nextInt(players.size())), CIRCLE_TICKS);
            } else if (this.healedOnce && HarmontideConfig.dreamLeviathan() && now >= this.nextFlyby && !players.isEmpty()) {
                this.startFlyby(level, players.get(level.random.nextInt(players.size())));
            }
        }
        switch (this.phase) {
            case INBOUND -> this.inbound(level, now);
            case LANDED -> this.landed(level, now);
            case DEAD -> this.dead(level, now);
            case RISING -> this.rising(level, now);
            case FLYBY -> this.flyby(level, now);
            default -> {
            }
        }
        this.keepBody(level);
        if (now % 10 == 0) LeviathanNet.sendSky(level, this);
        if (this.phase != IDLE) this.setDirty();
    }

    private void startInbound(ServerLevel level, ServerPlayer near, int circle) {
        double a = level.random.nextDouble() * Math.PI * 2;
        if (this.phase != FLYBY) {
            this.x = near.getX() + Math.cos(a) * 380;
            this.z = near.getZ() + Math.sin(a) * 380;
            this.y = ALTITUDE + 20;
            this.yaw = (float) (Mth.atan2(near.getZ() - this.z, near.getX() - this.x) * Mth.RAD_TO_DEG) - 90.0F;
        }
        this.phase = INBOUND;
        this.phaseStart = level.getGameTime();
        this.target = near.getUUID();
        this.cx = near.getX();
        this.cz = near.getZ();
        this.landing = null;
        this.progress = 0;
        this.warned = 0;
        this.circleFor = circle;
        this.setDirty();
    }

    private void startFlyby(ServerLevel level, ServerPlayer near) {
        double a = level.random.nextDouble() * Math.PI * 2, off = (level.random.nextDouble() - 0.5) * 200;
        double px = -Math.sin(a), pz = Math.cos(a);                                 // across her path, to pass to one side
        this.x = near.getX() + Math.cos(a) * 700 + px * off;
        this.z = near.getZ() + Math.sin(a) * 700 + pz * off;
        this.toX = near.getX() - Math.cos(a) * 700 + px * off;
        this.toZ = near.getZ() - Math.sin(a) * 700 + pz * off;
        this.y = ALTITUDE + 30 + level.random.nextInt(40);
        this.yaw = (float) (Mth.atan2(this.toZ - this.z, this.toX - this.x) * Mth.RAD_TO_DEG) - 90.0F;
        this.phase = FLYBY;
        this.phaseStart = level.getGameTime();
        this.setDirty();
    }

    /** Turns her toward a point (at most {@code turn} degrees a tick) and moves her on at {@code speed}, climbing or sinking toward ty. */
    private void steer(double tx, double ty, double tz, double speed, float turn, double climb) {
        float want = (float) (Mth.atan2(tz - this.z, tx - this.x) * Mth.RAD_TO_DEG) - 90.0F;
        this.yaw += Mth.clamp(Mth.wrapDegrees(want - this.yaw), -turn, turn);
        this.yaw = Mth.wrapDegrees(this.yaw);
        Vec3 fwd = Vec3.directionFromRotation(0, this.yaw);
        this.vx = fwd.x * speed;
        this.vz = fwd.z * speed;
        this.vy = Mth.clamp(ty - this.y, -climb, climb);
        this.x += this.vx;
        this.y += this.vy;
        this.z += this.vz;
        this.pitch = (float) Mth.clamp(-this.vy * 25.0, -12.0, 12.0);
    }

    private void inbound(ServerLevel level, long now) {
        ServerPlayer near = this.target == null ? null : level.getServer().getPlayerList().getPlayer(this.target);
        if (near == null || near.level() != level) {
            List<ServerPlayer> players = level.players();
            if (players.isEmpty()) return;                                            // nobody to come to: she waits up there
            near = players.get(level.random.nextInt(players.size()));
            this.target = near.getUUID();
        }
        if (now - this.phaseStart < this.circleFor) {
            this.cx = Mth.lerp(0.01, this.cx, near.getX());
            this.cz = Mth.lerp(0.01, this.cz, near.getZ());
            double a = Math.atan2(this.z - this.cz, this.x - this.cx) + 0.35;
            this.steer(this.cx + Math.cos(a) * CIRCLE_RADIUS, ALTITUDE, this.cz + Math.sin(a) * CIRCLE_RADIUS, CRUISE, 1.5F, 0.15);
            return;
        }
        if (this.landing == null) {
            this.landing = findLanding(level, near);
            if (this.landing == null) return;
        }
        double dx = this.landing.getX() + 0.5 - this.x, dz = this.landing.getZ() + 0.5 - this.z, flat = Math.sqrt(dx * dx + dz * dz);
        double ty = this.landing.getY() + (flat < 30 ? 0 : (flat - 30) * 0.5);
        double speed = Mth.clamp(flat / 50.0, 0.12, CRUISE);
        if (flat < 1.0) {
            this.x = this.landing.getX() + 0.5;
            this.z = this.landing.getZ() + 0.5;
            this.y = Mth.approach((float) this.y, this.landing.getY(), 0.2F);
            this.vx = this.vz = 0;
            this.pitch = 0;
            if (Math.abs(this.y - this.landing.getY()) < 0.05) this.land(level, now);
            return;
        }
        this.steer(this.landing.getX() + 0.5, ty, this.landing.getZ() + 0.5, speed, flat < 20 ? 4.0F : 2.0F, 0.35);
        if (flat < 3.0) this.pitch = 0;
    }

    private void land(ServerLevel level, long now) {
        this.y = this.landing.getY();
        this.vy = 0;
        this.phase = LANDED;
        this.phaseStart = now;
        this.deadline = now + HarmontideConfig.leviathanDays() * 24000L;
        this.warned = 0;
        this.run++;
        this.progress = 0;
        broadcast(level, "message.harmontide.leviathan.landed", this.landing.getX(), this.landing.getY(), this.landing.getZ());
        level.playSound(null, this.x, this.y, this.z, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.NEUTRAL, 8.0F, 0.4F);
        this.setDirty();
    }

    private void landed(ServerLevel level, long now) {
        this.vx = this.vy = this.vz = 0;
        long left = this.deadline - now;
        if (this.warned == 0 && left <= 24000) {
            this.warned = 1;
            broadcast(level, "message.harmontide.leviathan.weaker", this.landing.getX(), this.landing.getY(), this.landing.getZ());
        } else if (this.warned == 1 && left <= 6000) {
            this.warned = 2;
            broadcast(level, "message.harmontide.leviathan.fading", this.landing.getX(), this.landing.getY(), this.landing.getZ());
        }
        if (left <= 0) {
            this.die(level);
            return;
        }
        if (now % 5 == 0) {                                                            // crouch at her opening to climb in
            Vec3 door = this.entrance();
            for (ServerPlayer p : level.players()) {
                double d = p.position().distanceTo(door);
                if (d < 3.0 && p.isCrouching() && !p.isSpectator()) {
                    Interior.enter(p, this);
                } else if (d < 7.0 && now % 40 == 0) {
                    p.displayClientMessage(Component.translatable(nsfwEntry() ? "message.harmontide.leviathan.crouch_rear"
                            : "message.harmontide.leviathan.crouch_mouth"), true);
                }
            }
        }
    }

    /** Lets her die where she lies: whoever is inside is pushed back out first. */
    public void die(ServerLevel level) {
        Interior.expelAll(level.getServer(), this.run, "message.harmontide.leviathan.expelled_dying");
        this.phase = DEAD;
        this.phaseStart = level.getGameTime();
        this.scales = SCALES;
        this.vx = this.vy = this.vz = 0;
        broadcast(level, "message.harmontide.leviathan.died", (int) this.x, (int) this.y, (int) this.z);
        this.setDirty();
    }

    public int rotStage(long now) {
        long since = now - this.phaseStart;
        return since < DEAD_FOR ? 0 : since < ROT_FOR ? 1 : 2;
    }

    private void dead(ServerLevel level, long now) {
        if (now - this.phaseStart >= GONE_AFTER && level.isLoaded(BlockPos.containing(this.x, this.y, this.z))) {
            this.leaveBones(level);
            this.phase = IDLE;
            this.nextFlyby = now + 72000L;
            this.setDirty();
        }
    }

    /** What is left of her once the body is gone: her ribs and spine in bone blocks, half sunk in the ground. */
    private void leaveBones(ServerLevel level) {
        Vec3 fwd = Vec3.directionFromRotation(0, this.yaw), side = new Vec3(-fwd.z, 0, fwd.x);
        BlockState bone = Blocks.BONE_BLOCK.defaultBlockState();
        for (int i = -14; i <= 18; i++) {
            put(level, BlockPos.containing(this.x + fwd.x * i, this.y + 1, this.z + fwd.z * i), bone);
        }
        for (int r = -10; r <= 12; r += 4) {
            for (int a = 0; a <= 180; a += 12) {
                double rad = Math.toRadians(a), rx = Math.cos(rad) * 5.0, ry = Math.sin(rad) * 4.0;
                put(level, BlockPos.containing(this.x + fwd.x * r + side.x * rx, this.y + 1 + ry, this.z + fwd.z * r + side.z * rx), bone);
            }
        }
    }

    private static void put(ServerLevel level, BlockPos at, BlockState state) {
        if (level.getBlockState(at).canBeReplaced()) level.setBlock(at, state, 3);
    }

    /** Healed from inside: she rises, and drifts away over the horizon. */
    public void heal(ServerLevel level) {
        this.progress = 5;
        this.phase = RISING;
        this.phaseStart = level.getGameTime();
        this.healedOnce = true;
        broadcast(level, "message.harmontide.leviathan.healed", (int) this.x, (int) this.y, (int) this.z);
        level.playSound(null, this.x, this.y, this.z, SoundEvents.ELDER_GUARDIAN_AMBIENT, SoundSource.NEUTRAL, 12.0F, 0.6F);
        this.setDirty();
    }

    private void rising(ServerLevel level, long now) {
        long t = now - this.phaseStart;
        double base = this.landing == null ? 64 : this.landing.getY();
        if (t < 400) {
            this.vx = this.vz = 0;
            this.vy = Mth.clamp((base + 130 - this.y) * 0.02, 0.05, 0.45);
            this.y += this.vy;
            this.pitch = -8.0F;
        } else {
            Vec3 fwd = Vec3.directionFromRotation(0, this.yaw);
            this.steer(this.x + fwd.x * 50, ALTITUDE + 40, this.z + fwd.z * 50, CRUISE, 0.5F, 0.2);
        }
        if (t > 2400) this.endFlight(level, now);
    }

    private void flyby(ServerLevel level, long now) {
        this.steer(this.toX, this.y, this.toZ, CRUISE, 0.5F, 0.1);
        if (Math.hypot(this.toX - this.x, this.toZ - this.z) < 10 || now - this.phaseStart > 4800) this.endFlight(level, now);
    }

    private void endFlight(ServerLevel level, long now) {
        this.phase = IDLE;
        this.nextFlyby = now + 24000L * (3 + level.random.nextInt(4));
        this.setDirty();
    }

    // --- her body ------------------------------------------------------------------------------------------------------------

    public boolean owns(Entity e) {
        return e.getUUID().equals(this.body) && this.phase != IDLE;
    }

    @Nullable
    public DreamLeviathan body(ServerLevel level) {
        return this.body != null && level.getEntity(this.body) instanceof DreamLeviathan d ? d : null;
    }

    private void keepBody(ServerLevel level) {
        DreamLeviathan d = this.body(level);
        boolean wanted = false;
        if (this.phase != IDLE) {
            for (ServerPlayer p : level.players()) {
                if (Math.hypot(p.getX() - this.x, p.getZ() - this.z) < BODY_RANGE) {
                    wanted = true;
                    break;
                }
            }
        }
        if (!wanted || !level.isLoaded(BlockPos.containing(this.x, Mth.clamp(this.y, level.getMinBuildHeight(), level.getMaxBuildHeight() - 1), this.z))) {
            if (d != null) d.discard();
            if (!wanted) this.body = null;
            return;
        }
        if (d == null) {
            d = LeviathanRegistry.DREAM_LEVIATHAN.get().create(level);
            if (d == null) return;
            d.moveTo(this.x, this.y, this.z, this.yaw, this.pitch);
            this.body = d.getUUID();
            this.applyLook(level, d);
            level.addFreshEntity(d);
            return;
        }
        d.setPos(this.x, this.y, this.z);
        d.setYRot(this.yaw);
        d.setXRot(this.pitch);
        this.applyLook(level, d);
    }

    private void applyLook(ServerLevel level, DreamLeviathan d) {
        d.setLook(this.bodyMode(), this.sickness(), this.phase == DEAD ? this.rotStage(level.getGameTime()) : 0);
    }

    public int bodyMode() {
        return switch (this.phase) {
            case LANDED -> DreamLeviathan.LANDED;
            case DEAD -> DreamLeviathan.DEAD;
            case RISING -> DreamLeviathan.RISING;
            default -> DreamLeviathan.FLYING;
        };
    }

    /** 0 healthy .. 3 worst: sick while coming in and lying there, a little better for every wave beaten inside her. */
    public int sickness() {
        if (this.phase == INBOUND || this.phase == DEAD) return 3;
        if (this.phase != LANDED) return 0;
        return this.progress <= 1 ? 3 : this.progress <= 3 ? 2 : 1;
    }

    // --- players and her -------------------------------------------------------------------------------------------------------

    /** Where you climb in: her mouth, or her rear with the nsfw setting on, on the ground beside her. */
    public Vec3 entrance() {
        return DreamLeviathan.opening(new Vec3(this.x, this.y, this.z), this.yaw, nsfwEntry());
    }

    /** Where someone leaving her comes out: by her opening while she lies there, else where she lay, else nowhere known (null). */
    @Nullable
    public Vec3 exitPoint(ServerLevel overworld) {
        if (this.phase == LANDED) {
            Vec3 door = this.entrance();
            Vec3 out = door.add(Vec3.directionFromRotation(0, this.yaw).scale(nsfwEntry() ? -3 : 3));
            int top = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(out.x), Mth.floor(out.z));
            return new Vec3(out.x, Math.max(top, door.y), out.z);
        }
        if (this.landing != null) {
            Vec3 side = Vec3.atBottomCenterOf(this.landing).add(Vec3.directionFromRotation(0, this.yaw + 90).scale(12));
            int top = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(side.x), Mth.floor(side.z));
            return new Vec3(side.x, top, side.z);
        }
        return null;
    }

    /** Climbing in from beside her (used when the player clicks her body near her opening). */
    public boolean tryEnter(ServerLevel level, ServerPlayer p) {
        if (this.phase != LANDED || p.position().distanceTo(this.entrance()) > 6.0) return false;
        Interior.enter(p, this);
        return true;
    }

    /** Pulling a dream scale off her dead body (a bone once she is down to bones). Whether anything came off. */
    public boolean harvest(ServerLevel level, ServerPlayer p) {
        long now = level.getGameTime();
        Long last = this.pulled.get(p.getUUID());
        if (last != null && now - last < 10) return false;
        this.pulled.put(p.getUUID(), now);
        ItemStack got;
        if (this.rotStage(now) >= 2) {
            got = new ItemStack(level.random.nextInt(3) == 0 ? Items.BONE_BLOCK : Items.BONE);
        } else if (this.scales > 0) {
            this.scales--;
            got = new ItemStack(LeviathanRegistry.DREAM_SCALE.get());
            this.setDirty();
        } else {
            p.displayClientMessage(Component.translatable("message.harmontide.leviathan.no_scales"), true);
            return false;
        }
        if (!p.getInventory().add(got)) p.drop(got, false);
        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.8F, 0.6F);
        return true;
    }

    /** Called by a dream horn: brings her back, sick, to land near this player. The message key to show, or null if she comes. */
    @Nullable
    public String summon(ServerLevel level, ServerPlayer p) {
        if (!HarmontideConfig.dreamLeviathan()) return "message.harmontide.leviathan.horn_off";
        if (this.phase != IDLE && this.phase != FLYBY) return "message.harmontide.leviathan.horn_busy";
        if (p.level() != level) return "message.harmontide.leviathan.horn_dimension";
        if (level.isDay()) return "message.harmontide.leviathan.horn_day";
        if (!level.canSeeSky(p.blockPosition().above())) return "message.harmontide.leviathan.horn_sky";
        this.appeared = true;
        this.startInbound(level, p, CIRCLE_TICKS / 2);
        return null;
    }

    /** Testing help (/harmontide leviathan ...): sends her in to land near the player right away. */
    public void summonNow(ServerLevel level, ServerPlayer p, boolean landNow) {
        this.phase = IDLE;
        this.appeared = true;
        this.startInbound(level, p, landNow ? 0 : 200);
        if (landNow) {
            this.landing = findLanding(level, p);
            if (this.landing != null) {
                this.x = this.landing.getX() + 0.5;
                this.z = this.landing.getZ() + 0.5;
                this.land(level, level.getGameTime());
            }
        }
    }

    public void reset() {
        this.phase = IDLE;
        this.appeared = false;
        this.healedOnce = false;
        this.landing = null;
        this.setDirty();
    }

    public void setProgress(int waves) {
        this.progress = Math.max(this.progress, waves);
        this.setDirty();
    }

    /** A spot of open ground 30 to 70 blocks from the player, as flat as can be found, on land if possible. */
    @Nullable
    public static BlockPos findLanding(ServerLevel level, ServerPlayer near) {
        RandomSource r = level.random;
        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int i = 0; i < 32; i++) {
            double a = r.nextDouble() * Math.PI * 2, d = 30 + r.nextDouble() * 40;
            int x = Mth.floor(near.getX() + Math.cos(a) * d), z = Mth.floor(near.getZ() + Math.sin(a) * d);
            if (!level.isLoaded(new BlockPos(x, 64, z))) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            int bumps = 0;
            for (int ox = -10; ox <= 10; ox += 10) {
                for (int oz = -10; oz <= 10; oz += 10) {
                    if (!level.isLoaded(new BlockPos(x + ox, 64, z + oz))) continue;
                    bumps += Math.abs(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x + ox, z + oz) - y);
                }
            }
            if (!level.getFluidState(new BlockPos(x, y - 1, z)).isEmpty()) bumps += 40;
            if (bumps < bestScore) {
                bestScore = bumps;
                best = new BlockPos(x, y, z);
            }
        }
        if (best == null) best = near.blockPosition();
        return best;
    }

    public static void broadcast(ServerLevel level, String key, Object... args) {
        level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(key, args).withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    // --- saving --------------------------------------------------------------------------------------------------------------------

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        tag.putInt("Phase", this.phase);
        tag.putBoolean("Appeared", this.appeared);
        tag.putBoolean("HealedOnce", this.healedOnce);
        tag.putDouble("X", this.x);
        tag.putDouble("Y", this.y);
        tag.putDouble("Z", this.z);
        tag.putFloat("Yaw", this.yaw);
        tag.putLong("PhaseStart", this.phaseStart);
        tag.putLong("Deadline", this.deadline);
        tag.putLong("NextFlyby", this.nextFlyby);
        tag.putInt("Warned", this.warned);
        if (this.landing != null) tag.putLong("Landing", this.landing.asLong());
        if (this.target != null) tag.putUUID("Target", this.target);
        tag.putDouble("CX", this.cx);
        tag.putDouble("CZ", this.cz);
        tag.putDouble("ToX", this.toX);
        tag.putDouble("ToZ", this.toZ);
        tag.putInt("Run", this.run);
        tag.putInt("Progress", this.progress);
        tag.putInt("Scales", this.scales);
        tag.putInt("CircleFor", this.circleFor);
        return tag;
    }

    public static LeviathanEvent load(CompoundTag tag, HolderLookup.Provider lookup) {
        LeviathanEvent e = new LeviathanEvent();
        e.phase = tag.getInt("Phase");
        e.appeared = tag.getBoolean("Appeared");
        e.healedOnce = tag.getBoolean("HealedOnce");
        e.x = tag.getDouble("X");
        e.y = tag.getDouble("Y");
        e.z = tag.getDouble("Z");
        e.yaw = tag.getFloat("Yaw");
        e.phaseStart = tag.getLong("PhaseStart");
        e.deadline = tag.getLong("Deadline");
        e.nextFlyby = tag.getLong("NextFlyby");
        e.warned = tag.getInt("Warned");
        e.landing = tag.contains("Landing") ? BlockPos.of(tag.getLong("Landing")) : null;
        e.target = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
        e.cx = tag.getDouble("CX");
        e.cz = tag.getDouble("CZ");
        e.toX = tag.getDouble("ToX");
        e.toZ = tag.getDouble("ToZ");
        e.run = tag.getInt("Run");
        e.progress = tag.getInt("Progress");
        e.scales = tag.getInt("Scales");
        e.circleFor = tag.contains("CircleFor") ? tag.getInt("CircleFor") : CIRCLE_TICKS;
        return e;
    }

    public static boolean inOverworld(Level level) {
        return level.dimension() == Level.OVERWORLD;
    }
}
