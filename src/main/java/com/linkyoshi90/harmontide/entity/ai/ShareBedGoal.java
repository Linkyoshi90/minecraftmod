package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.BedCall;
import com.linkyoshi90.harmontide.entity.BedSharing;
import com.linkyoshi90.harmontide.entity.Bonded;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

/**
 * A monster girl who trusts a player enough ({@link Bonded#BED_AFFECTION}) may join them when they lie down in a
 * bed: once a night she makes up her mind ({@link Bonded#bedChance}, better the more she likes them), comes over,
 * hops into the bed beside them and lies down. If the player sleeps right through to morning she is expecting
 * (see {@code BedSharing}). Nothing more than that is shown, unless the nsfw option is on: then she says so, and a
 * biped kneels astride him and bounces while anyone else rocks along the mattress
 * (see {@link com.linkyoshi90.harmontide.entity.Nsfw}). The night still passes as it always does.
 * <p>
 * Under a full moon her hormones answer the moonlight: she comes without fail, eyes aglow ({@link Bonded#isMoonstruck}),
 * and waits by the bed while another has her turn. Once she has had hers she is expecting, her eyes go back to normal
 * and, worn out, she lies down to sleep beside the bed. The night waits until the last of them is done.
 */
public class ShareBedGoal<T extends PathfinderMob & Bonded> extends Goal {
    public static final double RANGE = 24.0;
    /** Close enough to the bed to hop in. */
    private static final double HOP_DISTANCE_SQR = 2.5 * 2.5;
    private static final int GIVE_UP_TICKS = 600;
    /** How long a full-moon turn in the bed lasts. */
    public static final int TURN_TICKS = 200;
    /** Where she waits for her turn. */
    private static final double WAIT_DISTANCE_SQR = 3.0 * 3.0;

    private final T mob;
    private Player partner;
    private BlockPos bed;
    private int scanDelay;
    private int ticks;
    private boolean inBed;
    /** Tonight is a full moon (decided when she sets off). */
    private boolean moon;
    private int turn;
    /** Her turn is over: she sleeps beside the bed until morning. */
    private boolean spent;
    /** The night (day number) she last made up her mind about, and about whom. */
    private long decidedNight = -1;
    @Nullable
    private UUID decidedFor;
    private boolean decidedYes;

    public ShareBedGoal(T mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (--this.scanDelay > 0) return false;
        this.scanDelay = 20;
        if (!this.mob.canShareBed() || this.mob.isOrderedToSit() || this.mob.getTarget() != null || this.mob.isInWater()) return false;
        boolean onPost = com.linkyoshi90.harmontide.entity.Orders.holdsPlace(this.mob);   // a guard only leaves her post when called
        Player best = null;
        for (Player p : this.mob.level().players()) {
            if (!p.isSleeping() || p.isSpectator() || p.distanceToSqr(this.mob) > RANGE * RANGE) continue;
            if (onPost && !(p instanceof ServerPlayer caller && BedCall.isCalling(caller))) continue;
            if (!this.mob.trustsEnoughForBed(p)) {
                if (this.mob.level() instanceof ServerLevel server) com.linkyoshi90.harmontide.entity.Condom.mention(server, this.mob, p);
                continue;
            }
            if (best == null || p.distanceToSqr(this.mob) < best.distanceToSqr(this.mob)) best = p;
        }
        if (best == null || best.getSleepingPos().isEmpty() || !this.wantsTo(best)) return false;
        this.partner = best;
        this.bed = best.getSleepingPos().get();
        return true;
    }

    /** One roll per night and player: tonight she either comes or she doesn't. */
    private boolean wantsTo(Player player) {
        long night = (this.mob.level().getDayTime() + 12000L) / 24000L;   // from noon to noon: a whole night is one roll
        if (night != this.decidedNight || !player.getUUID().equals(this.decidedFor)) {
            this.decidedNight = night;
            this.decidedFor = player.getUUID();
            this.decidedYes = BedSharing.fullMoon(this.mob.level()) || this.mob.getRandom().nextFloat() < this.mob.bedChanceFor(player) + com.linkyoshi90.harmontide.entity.Missing.bedBonus(this.mob, player);
        }
        return this.decidedYes;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.partner == null || !this.partner.isAlive() || !this.partner.isSleeping() || this.mob.getTarget() != null
                || this.mob.isOrderedToSit()) return false;
        return this.spent || this.mob.canShareBed() && (this.inBed || this.ticks < GIVE_UP_TICKS);
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.turn = 0;
        this.inBed = false;
        this.spent = false;
        this.moon = BedSharing.fullMoon(this.mob.level());
        if (this.moon && !BedSharing.nightWaits(this.partner)) {   // the first of them to answer the moon tonight
            this.partner.displayClientMessage(Component.translatable("message.harmontide.full_moon_visitors"), false);
        }
        this.mob.setMoonstruck(this.moon);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * Where she lies: on the mattress beside the sleeper, her feet at the foot end and her head on the pillow
     * (she faces the foot of the bed; the "lie" pose tips her over backwards).
     */
    public static Vec3 spotInBed(Level level, BlockPos head) {
        BlockState state = level.getBlockState(head);
        Direction facing = state.hasProperty(BedBlock.FACING) ? state.getValue(BedBlock.FACING) : Direction.NORTH;
        Direction side = facing.getClockWise();
        Vec3 headCentre = Vec3.atBottomCenterOf(head);
        return headCentre.add(facing.getStepX() * -1.1 + side.getStepX() * 0.3, 0.5625, facing.getStepZ() * -1.1 + side.getStepZ() * 0.3);
    }

    /** The direction she faces while in the bed (towards its foot). */
    public static float yawInBed(Level level, BlockPos head) {
        BlockState state = level.getBlockState(head);
        Direction facing = state.hasProperty(BedBlock.FACING) ? state.getValue(BedBlock.FACING) : Direction.NORTH;
        return facing.getOpposite().toYRot();
    }

    @Override
    public void tick() {
        this.ticks++;
        if (this.spent) {
            this.mob.getNavigation().stop();
            if (this.ticks % 20 == 0) this.mob.settleInBed();   // whatever else she started, she lies back down
            return;
        }
        if (this.inBed) {
            this.stayInBed();
            if (this.moon && ++this.turn >= TURN_TICKS) this.finishTurn();
            return;
        }
        this.mob.getLookControl().setLookAt(this.partner);
        Vec3 spot = spotInBed(this.mob.level(), this.bed);
        if (this.moon && this.bedTaken()) {   // her turn will come: wait close by (the wait does not count against giving up)
            this.ticks = 0;
            if (this.mob.distanceToSqr(spot) <= WAIT_DISTANCE_SQR) this.mob.getNavigation().stop();
            else if (this.mob.tickCount % 10 == 0) this.mob.getNavigation().moveTo(spot.x, spot.y, spot.z, 1.0);
            return;
        }
        if (this.mob.distanceToSqr(spot) > HOP_DISTANCE_SQR) {
            if (this.ticks % 10 == 1) this.mob.getNavigation().moveTo(spot.x, spot.y, spot.z, 1.0);
            return;
        }
        // hop in
        this.mob.getNavigation().stop();
        this.inBed = true;
        this.mob.getGestation().setSharingWith(this.partner.getUUID());
        this.mob.settleInBed();
        if (!(this.mob.level() instanceof ServerLevel server) || !(this.partner instanceof ServerPlayer sleeper)
                || !com.linkyoshi90.harmontide.entity.Nsfw.say(server, this.mob, sleeper, com.linkyoshi90.harmontide.entity.Nsfw.Kind.BED)) {
            this.partner.displayClientMessage(Component.translatable("message.harmontide.into_bed", this.mob.getDisplayName()), false);   // in chat: who it is
        }
        this.mob.level().playSound(null, this.bed, SoundEvents.WOOL_STEP, SoundSource.NEUTRAL, 0.8F, 1.1F);
        this.stayInBed();
    }

    private void stayInBed() {
        Vec3 spot = spotInBed(this.mob.level(), this.bed);
        float yaw = yawInBed(this.mob.level(), this.bed);
        this.mob.getNavigation().stop();
        this.mob.setDeltaMovement(Vec3.ZERO);
        if (!(this.mob.level() instanceof ServerLevel server) || !(this.partner instanceof ServerPlayer sleeper)
                || !com.linkyoshi90.harmontide.entity.Nsfw.rock(server, this.mob, sleeper, spot, yaw, this.bed)) {
            com.linkyoshi90.harmontide.entity.Nsfw.lieDown(this.mob);
            this.mob.moveTo(spot.x, spot.y, spot.z, yaw, 0.0F);
            this.mob.setYBodyRot(yaw);
            this.mob.setYHeadRot(yaw);
        }
        if (this.ticks % 60 == 0 && this.mob.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HEART, this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ(), 1, 0.2, 0.1, 0.2, 0.0);
        }
    }

    /** Someone else is in the bed with the sleeper right now. */
    private boolean bedTaken() {
        UUID sleeper = this.partner.getUUID();
        return !this.mob.level().getEntitiesOfClass(Mob.class, new AABB(this.bed).inflate(3.0),
                m -> m != this.mob && m instanceof Bonded b && sleeper.equals(b.getGestation().getSharingWith())).isEmpty();
    }

    /** Her full-moon turn is over: she is expecting, the moon lets go of her, and she lies down beside the bed to sleep. */
    private void finishTurn() {
        if (this.mob.level() instanceof ServerLevel server && this.partner instanceof ServerPlayer player) BedSharing.nightShared(server, player, this.mob);
        this.mob.setMoonstruck(false);
        this.climbOut();
        this.inBed = false;
        this.spent = true;
        this.mob.settleInBed();
    }

    private void climbOut() {
        this.mob.leaveBed();
        // climb out beside the bed rather than staying perched on the mattress
        BlockPos out = findSpotBeside(this.mob.level(), this.bed);
        if (out != null) this.mob.moveTo(out.getX() + 0.5, out.getY(), out.getZ() + 0.5, this.mob.getYRot(), 0.0F);
    }

    public boolean isInBed() {
        return this.inBed;
    }

    @Override
    public void stop() {
        if (this.inBed) this.climbOut();
        if (this.spent) this.mob.leaveBed();   // morning: she wakes up
        // sharingWith is cleared by BedSharing once the night is over; if she gave up early, forget it now.
        // A full-moon turn cut short frees the bed for the next one (and she is out of it for tonight, so the night can't hang on her).
        if (this.moon || this.partner == null || !this.partner.isSleeping()) this.mob.getGestation().setSharingWith(null);
        if (this.moon) this.decidedYes = false;
        this.mob.setMoonstruck(false);
        this.spent = false;
        this.inBed = false;
        this.partner = null;
        this.bed = null;
    }

    /** A free, walkable block within two blocks of the bed. */
    @Nullable
    public static BlockPos findSpotBeside(Level level, BlockPos bed) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(bed.offset(-2, 0, -2), bed.offset(2, 0, 2))) {
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) continue;
            if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) continue;
            if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) continue;
            double dist = pos.distSqr(bed);
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }
}
