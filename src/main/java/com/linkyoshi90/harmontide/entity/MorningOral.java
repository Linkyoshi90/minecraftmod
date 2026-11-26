package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * After a night shared, she kneels (or lowers her head, if she cannot kneel) and stays there a short while.
 * The pose is client-side. A daughter is only queued when a condom already allowed the night.
 */
public final class MorningOral {
    public static final int TICKS = 100;
    private static final Map<Mob, Session> SESSIONS = new WeakHashMap<>();
    private static ServerLevel lastLevel;
    private static long lastTime = Long.MIN_VALUE;

    private static final class Session {
        final UUID player;
        int ticks;

        Session(UUID player) {
            this.player = player;
        }
    }

    private MorningOral() {}

    /** She is in the morning scene, so the renderer lowers her head. */
    public static boolean posing(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) return girl.getAction() == AbstractMonsterGirl.Action.ORAL;
        if (mob instanceof AbstractHarpy harpy) return harpy.getAction() == AbstractHarpy.Action.ORAL;
        if (mob instanceof AbstractAnuran anuran) return anuran.getAction() == AbstractAnuran.Action.ORAL;
        return false;
    }

    public static boolean pending(Mob mob) {
        return SESSIONS.containsKey(mob);
    }

    /** Remember her for the wake. Does not move her yet. */
    public static void queue(Mob mob, ServerPlayer player) {
        if (!Nsfw.on() || !Nsfw.adult(mob, player) || !Condom.allows(mob, player) || mob.getTarget() != null) return;
        SESSIONS.put(mob, new Session(player.getUUID()));
    }

    /** Once per game tick, from the player tick. */
    public static void tick(ServerLevel level) {
        long now = level.getGameTime();
        if (level == lastLevel && now == lastTime) return;
        lastLevel = level;
        lastTime = now;
        step(level);
    }

    /** One beat. The game test calls this on its own. */
    public static void step(ServerLevel level) {
        List<Mob> mobs = new ArrayList<>(SESSIONS.keySet());
        for (Mob mob : mobs) {
            Session session = SESSIONS.get(mob);
            if (session == null) continue;
            Player found = level.getPlayerByUUID(session.player);
            if (!(found instanceof ServerPlayer player) || !player.isAlive() || !mob.isAlive()
                    || mob.level() != level || mob.getTarget() != null) {
                end(mob);
                continue;
            }
            if (session.ticks > 0 && player.distanceToSqr(mob) > 12.0 * 12.0) {
                end(mob);
                continue;
            }
            if (session.ticks == 0 && mob instanceof Bonded bonded) bonded.blushFor(TICKS);
            place(mob, player);
            if (session.ticks % 20 == 0) {
                level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY(), mob.getZ(), 2, 0.15, 0.1, 0.15, 0.0);
            }
            if (++session.ticks >= TICKS) end(mob);
        }
    }

    private static void place(Mob mob, ServerPlayer player) {
        mob.getNavigation().stop();
        mob.setDeltaMovement(Vec3.ZERO);
        float yaw = player.getYRot();
        double dist = Nsfw.quadruped(mob) ? 1.2 : 0.75;
        float rad = yaw * Mth.DEG_TO_RAD;
        double x = player.getX() - Mth.sin(rad) * dist;
        double z = player.getZ() + Mth.cos(rad) * dist;
        float face = yaw + 180.0F;
        mob.moveTo(x, player.getY(), z, face, 28.0F);
        mob.setYBodyRot(face);
        mob.setYHeadRot(face);
        mob.fallDistance = 0.0F;
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(AbstractMonsterGirl.Action.ORAL);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(AbstractHarpy.Action.ORAL);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(AbstractAnuran.Action.ORAL);
    }

    private static void end(Mob mob) {
        SESSIONS.remove(mob);
        if (!posing(mob)) return;
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(AbstractMonsterGirl.Action.NONE);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(AbstractHarpy.Action.NONE);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(AbstractAnuran.Action.NONE);
    }
}
