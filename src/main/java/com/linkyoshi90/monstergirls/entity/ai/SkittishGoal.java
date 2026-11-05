package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AlbatrossHarpy;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Anyone she doesn't trust coming within 12 blocks (6 if they creep up sneaking) sends her climbing away fast.
 * A wild albatross that has never been fed by anyone doesn't come back: once she's well clear, she's gone.
 */
public class SkittishGoal extends Goal {
    private static final int VANISH_AFTER = 100;

    private final AlbatrossHarpy albatross;
    private Player threat;
    private int ticks;

    public SkittishGoal(AlbatrossHarpy albatross) {
        this.albatross = albatross;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean isThreat(Player player) {
        if (player.isSpectator() || this.albatross.isTrusting(player)) return false;
        if (player.getVehicle() instanceof net.minecraft.world.entity.vehicle.Boat) return false;   // someone at sea in a boat is company: she follows ships
        double radius = player.isShiftKeyDown() ? 6.0 : 12.0;
        return this.albatross.distanceToSqr(player) < radius * radius;
    }

    @Override
    public boolean canUse() {
        this.threat = this.albatross.level().getNearestPlayer(this.albatross.getX(), this.albatross.getY(), this.albatross.getZ(), 12.0,
                e -> e instanceof Player p && this.isThreat(p));
        return this.threat != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < VANISH_AFTER + 20;
    }

    @Override
    public void start() {
        this.ticks = 0;
    }

    @Override
    public void stop() {
        this.threat = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        Vec3 away = this.albatross.position().subtract(this.threat.position()).multiply(1, 0, 1);
        away = away.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : away.normalize();
        Vec3 target = this.albatross.position().add(away.scale(12)).add(0, 8, 0);
        this.albatross.getNavigation().stop();
        this.albatross.getMoveControl().setWantedPosition(target.x, target.y, target.z, 1.6);
        if (this.ticks == VANISH_AFTER && !this.albatross.hasAnyAffection() && !this.albatross.isPersistenceRequired()
                && this.albatross.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CLOUD, this.albatross.getX(), this.albatross.getY() + 1, this.albatross.getZ(), 12, 0.6, 0.6, 0.6, 0.02);
            this.albatross.discard();
        }
    }
}
