package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Snowball fights: hit a friend (a girl, harpy or frog) with a snowball and she takes up the fight: for half a minute she
 * pelts her friend back, a snowball a second or so, laughing. Snowballs do no harm, so nobody gets hurt.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Snowballs {
    public static final int FIGHT = 600, EVERY = 25, RANGE = 16;
    public static final String BY = "SnowFightWith", UNTIL = "SnowFightUntil", ADVANCEMENT = "snowball_fight";

    private Snowballs() {
    }

    /** A snowball from a player hits her: the fight is on (or goes on). Whether she joined in. */
    public static boolean hit(ServerLevel level, Mob mob, Player player) {
        if (!(mob instanceof Bonded b) || b.getAffection(player) < Orders.AFFECTION || mob.isBaby() && mob.getRandom().nextBoolean()) return false;
        mob.getPersistentData().putUUID(BY, player.getUUID());
        mob.getPersistentData().putLong(UNTIL, level.getGameTime() + FIGHT);
        mob.playSound(SoundEvents.ALLAY_AMBIENT_WITH_ITEM, 1.0F, 1.5F);
        if (player instanceof ServerPlayer sp) {
            AdvancementHolder a = sp.server.getAdvancements().get(HarmontideMod.id(ADVANCEMENT));
            if (a != null) sp.getAdvancements().award(a, "done");
        }
        return true;
    }

    public static boolean fighting(Mob mob) {
        return mob.getPersistentData().contains(UNTIL) && mob.level().getGameTime() < mob.getPersistentData().getLong(UNTIL);
    }

    /** She throws one at her friend. The snowball, or null. */
    public static Snowball throwAt(ServerLevel level, Mob mob, Player target) {
        Snowball ball = new Snowball(level, mob);
        double dx = target.getX() - mob.getX(), dz = target.getZ() - mob.getZ();
        double dy = target.getEyeY() - 1.1 - ball.getY();
        ball.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.2, dz, 1.4F, 6.0F);
        level.addFreshEntity(ball);
        mob.getLookControl().setLookAt(target);
        mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        level.playSound(null, mob.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
        return ball;
    }

    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof Snowball ball && ball.getOwner() instanceof Player p && event.getRayTraceResult() instanceof EntityHitResult hit
                && hit.getEntity() instanceof Mob mob && mob.level() instanceof ServerLevel level) {
            hit(level, mob, p);
        }
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob instanceof Bonded) || mob.tickCount % EVERY != 0 || !(mob.level() instanceof ServerLevel level)) return;
        if (!mob.getPersistentData().contains(UNTIL)) return;
        if (!fighting(mob)) {
            mob.getPersistentData().remove(UNTIL);
            mob.getPersistentData().remove(BY);
            return;
        }
        Player p = level.getPlayerByUUID(mob.getPersistentData().getUUID(BY));
        if (p != null && p.isAlive() && p.distanceToSqr(mob) < RANGE * RANGE && mob.hasLineOfSight(p)) throwAt(level, mob, p);
    }
}
