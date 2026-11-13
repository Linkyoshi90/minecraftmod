package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

/**
 * Ghost possession (2026-11-13), after Monster Girl Encyclopedia's ghosts, who slip into the one they love. Crouch and offer a ghost
 * friend ({@link #GHOSTS}, friendship {@link #FRIEND} or more) an empty hand and she slips into you for {@link #MERGE} ticks: you see
 * in the dark, a faint glow hangs about you, and monsters lose track of you (they drop you as a target and can't take you up again).
 * Then she slips out beside you, glad of it. Once a day each.
 * <p>
 * A wild ghost may do it as a prank: at night, one near a player (within {@link #PRANK_NEAR}) now and then slips in for
 * {@link #PRANK} ticks, gives them a shiver and a hiccup (Nausea, Slowness), and pops out giggling, a little fonder of them.
 * <p>
 * While she is inside she rides along out of sight (invisible, untouchable, keeping to the player); she comes out at once if they
 * leave or die. Off with the config option {@code ghostPossession}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class GhostPossession {
    public static final Set<GirlSpecies> GHOSTS = Set.of(GirlSpecies.YUREI_UNDEAD, GirlSpecies.BANSHEE_UNDEAD, GirlSpecies.POLTERGEIST_UNDEAD,
            GirlSpecies.LOST_SOUL_UNDEAD, GirlSpecies.LLORONA_UNDEAD);
    public static final int MERGE = 1200, PRANK = 200, FRIEND = Orders.AFFECTION, PRANK_NEAR = 6, PRANK_EVERY = 600, PRANK_ODDS = 12, CLEAR = 24;
    public static final double HIDE = 2.0;
    private static final String IN = "PossessingPlayer", UNTIL = "PossessingUntil", PRANKING = "PossessingPrank", DAY = "PossessedDay";

    private GhostPossession() {}

    public static boolean ghost(Entity e) {
        return e instanceof AbstractMonsterGirl g && GHOSTS.contains(g.getSpecies()) && !g.isBaby();
    }

    /** The player she is inside, or null. */
    @Nullable
    public static UUID inside(Mob ghost) {
        CompoundTag pd = ghost.getPersistentData();
        return pd.hasUUID(IN) ? pd.getUUID(IN) : null;
    }

    /** A ghost is inside this player now. */
    public static boolean possessed(ServerLevel level, Player player) {
        for (AbstractMonsterGirl g : level.getEntitiesOfClass(AbstractMonsterGirl.class, player.getBoundingBox().inflate(3), GhostPossession::ghost)) {
            if (player.getUUID().equals(inside(g))) return true;
        }
        return false;
    }

    /** She slips into the player. False if she can't (not a ghost, already in someone, already today as a friend). */
    public static boolean possess(ServerLevel level, AbstractMonsterGirl ghost, Player player, boolean prank) {
        if (!HarmontideConfig.ghostPossession() || !ghost(ghost) || inside(ghost) != null || !ghost.isAlive()) return false;
        long today = level.getDayTime() / 24000L;
        CompoundTag pd = ghost.getPersistentData();
        if (!prank && pd.contains(DAY) && pd.getLong(DAY) == today) {
            player.displayClientMessage(Component.translatable("message.harmontide.ghost.tired", ghost.getDisplayName()), true);
            return false;
        }
        pd.putUUID(IN, player.getUUID());
        pd.putLong(UNTIL, level.getGameTime() + (prank ? PRANK : MERGE));
        pd.putBoolean(PRANKING, prank);
        if (!prank) pd.putLong(DAY, today);
        ghost.setInvisible(true);
        ghost.setInvulnerable(true);
        ghost.setNoAi(true);
        ghost.setNoGravity(true);
        ghost.noPhysics = true;
        ghost.moveTo(player.getX(), player.getY() - HIDE, player.getZ());   // under their feet, out of reach of clicks
        level.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(), 16, 0.3, 0.5, 0.3, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.NEUTRAL, 1.0F, 1.3F);
        if (prank) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, PRANK, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, PRANK / 2, 0, true, false, true));
            player.displayClientMessage(Component.translatable("message.harmontide.ghost.prank", ghost.getDisplayName()), true);
        } else {
            empower(level, player, MERGE);
            player.displayClientMessage(Component.translatable("message.harmontide.ghost.in", ghost.getDisplayName()), false);
            Dairy.award(player, "ghost_possession");
        }
        return true;
    }

    /** Her gifts while she is inside: night eyes, a faint glow, and monsters lose track of them. */
    public static void empower(ServerLevel level, Player player, int ticks) {
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, ticks + 20, 0, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, true, false, false));
        for (Mob m : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(CLEAR), m -> m instanceof Enemy && m.getTarget() == player)) m.setTarget(null);
    }

    /** She slips back out beside the player (or where she is, if they are gone). */
    public static void release(ServerLevel level, AbstractMonsterGirl ghost, @Nullable Player player) {
        CompoundTag pd = ghost.getPersistentData();
        boolean prank = pd.getBoolean(PRANKING);
        pd.remove(IN);
        pd.remove(UNTIL);
        pd.remove(PRANKING);
        ghost.setInvisible(false);
        ghost.setInvulnerable(false);
        ghost.setNoAi(false);
        ghost.setNoGravity(false);
        ghost.noPhysics = false;
        if (player != null) {
            ghost.moveTo(player.getX() + 0.8, player.getY(), player.getZ() + 0.8);
            ghost.addAffection(player, prank ? 1 : 3);
            player.displayClientMessage(Component.translatable(prank ? "message.harmontide.ghost.giggle" : "message.harmontide.ghost.out", ghost.getDisplayName()), true);
            if (!prank) player.removeEffect(MobEffects.GLOWING);
        }
        level.sendParticles(ParticleTypes.SOUL, ghost.getX(), ghost.getY() + 1.0, ghost.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
        ghost.playSound(SoundEvents.ALLAY_ITEM_TAKEN, 0.7F, 0.8F);
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractMonsterGirl ghost) || !(ghost.level() instanceof ServerLevel level) || !ghost(ghost)) return;
        UUID in = inside(ghost);
        if (in != null) {
            Player p = level.getPlayerByUUID(in);
            if (p == null || !p.isAlive() || level.getGameTime() >= ghost.getPersistentData().getLong(UNTIL)) {
                release(level, ghost, p);
                return;
            }
            ghost.moveTo(p.getX(), p.getY() - HIDE, p.getZ());
            ghost.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            return;
        }
        if (ghost.tickCount % PRANK_EVERY == 0 && !ghost.hasAnyAffection() && !level.isDay() && HarmontideConfig.ghostPossession()
                && ghost.getRandom().nextInt(PRANK_ODDS) == 0) {
            Player near = level.getNearestPlayer(ghost, PRANK_NEAR);
            if (near != null && !near.isSpectator() && !near.isCreative()) possess(level, ghost, near, true);
        }
    }

    /** Crouch and offer a ghost friend an empty hand: she slips into you. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Player p = event.getEntity();
        if (!(event.getTarget() instanceof AbstractMonsterGirl ghost) || !ghost(ghost) || event.getHand() != InteractionHand.MAIN_HAND
                || !p.isShiftKeyDown() || !event.getItemStack().isEmpty() || ghost.getAffection(p) < FRIEND) return;
        if (ghost.level() instanceof ServerLevel level) possess(level, ghost, p, false);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(ghost.level().isClientSide));
    }

    /** Monsters can't take up a player a ghost friend is inside. */
    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Enemy && event.getNewAboutToBeSetTarget() instanceof Player p && p.level() instanceof ServerLevel level
                && friendInside(level, p)) event.setCanceled(true);
    }

    private static boolean friendInside(ServerLevel level, Player p) {
        for (AbstractMonsterGirl g : level.getEntitiesOfClass(AbstractMonsterGirl.class, p.getBoundingBox().inflate(3), GhostPossession::ghost)) {
            if (p.getUUID().equals(inside(g)) && !g.getPersistentData().getBoolean(PRANKING)) return true;
        }
        return false;
    }
}
