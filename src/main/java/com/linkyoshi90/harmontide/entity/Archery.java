package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;
import javax.annotation.Nullable;

/**
 * An archery match (2026-11-09), alongside races and treasure hunts. Sneak and show a friend a bow with a target block within
 * {@link #RANGE} of her: three arrows each. She really shoots (her arrows fly at the target, {@link #inaccuracy} by kind: a centaur
 * or an elf hardly misses, a slime hardly hits) and so do you; each arrow scores what the target block's own redstone would say
 * (1 to 15, more for nearer the middle). After her third arrow, and your third or {@link #TIME} ticks, the better total wins: win
 * and she pays up with a present; lose and she's delighted with herself. Either way she's {@link #AFFECTION} closer.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Archery {
    public static final int RANGE = 24, SHOTS = 3, FIRST = 40, BETWEEN = 50, TIME = 1200, AFFECTION = 3;
    static final String TARGET = "ArcheryTarget", WITH = "ArcheryWith", START = "ArcheryStart", HERS = "ArcheryHers", HER_SHOTS = "ArcheryHerShots",
            YOURS = "ArcheryYours", YOUR_SHOTS = "ArcheryYourShots", PLAYER_MATCH = "ArcheryMatch";

    private Archery() {
    }

    public static boolean playing(Mob m) {
        return m.getPersistentData().contains(TARGET);
    }

    /** The nearest target block within RANGE of her, or null. */
    @Nullable
    public static BlockPos target(ServerLevel level, Mob girl) {
        BlockPos at = girl.blockPosition(), best = null;
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-RANGE, -4, -RANGE), at.offset(RANGE, 6, RANGE))) {
            if (level.getBlockState(p).is(Blocks.TARGET) && (best == null || p.distSqr(at) < best.distSqr(at))) best = p.immutable();
        }
        return best;
    }

    /** How far off her arrows fly (vanilla's inaccuracy): the archers of the roster, everyone else, and slimes. */
    public static float inaccuracy(Mob girl) {
        if (!(girl instanceof AbstractMonsterGirl g)) return 4.0F;
        GirlSpecies.Family f = g.getSpecies().family();
        return f == GirlSpecies.Family.CENTAUR || f == GirlSpecies.Family.ELF ? 1.0F : f == GirlSpecies.Family.SLIME ? 9.0F : 4.0F;
    }

    public static boolean start(ServerLevel level, Mob girl, Player p, BlockPos target) {
        if (playing(girl) || !(girl instanceof Bonded b) || girl.isBaby() || b.getAffection(p) < HomeLife.FRIEND) return false;
        CompoundTag t = girl.getPersistentData();
        t.putLong(TARGET, target.asLong());
        t.putUUID(WITH, p.getUUID());
        t.putLong(START, level.getGameTime());
        t.putInt(HERS, 0);
        t.putInt(HER_SHOTS, 0);
        t.putInt(YOURS, 0);
        t.putInt(YOUR_SHOTS, 0);
        p.getPersistentData().putUUID(PLAYER_MATCH, girl.getUUID());
        girl.getNavigation().stop();
        p.displayClientMessage(Component.translatable("message.harmontide.archery.start", girl.getDisplayName()).withStyle(ChatFormatting.YELLOW), false);
        return true;
    }

    /** She looses one arrow at the target. */
    public static Arrow shoot(ServerLevel level, Mob girl, BlockPos target) {
        Arrow arrow = new Arrow(level, girl, new ItemStack(Items.ARROW), null);
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        Vec3 aim = target.getCenter().subtract(arrow.position());
        double flat = Math.sqrt(aim.x * aim.x + aim.z * aim.z);
        arrow.shoot(aim.x, aim.y + flat * 0.2, aim.z, 1.6F, inaccuracy(girl));
        girl.getLookControl().setLookAt(target.getCenter());
        level.addFreshEntity(arrow);
        level.playSound(null, girl.blockPosition(), SoundEvents.SKELETON_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.1F);
        return arrow;
    }

    /** The target block's own score for a hit: 1 at the rim to 15 dead centre. */
    public static int score(BlockHitResult hit) {
        Vec3 loc = hit.getLocation();
        double dx = Math.abs(Mth.frac(loc.x) - 0.5), dy = Math.abs(Mth.frac(loc.y) - 0.5), dz = Math.abs(Mth.frac(loc.z) - 0.5);
        Direction.Axis axis = hit.getDirection().getAxis();
        double off = axis == Direction.Axis.Y ? Math.max(dx, dz) : axis == Direction.Axis.Z ? Math.max(dx, dy) : Math.max(dy, dz);
        return Math.max(1, Mth.ceil(15.0 * Mth.clamp((0.5 - off) / 0.5, 0.0, 1.0)));
    }

    /** An arrow hit a block: if it's the match's target, it scores for whoever shot it. The points, or 0. */
    public static int hit(ServerLevel level, @Nullable Entity owner, BlockHitResult hit) {
        Mob girl = owner instanceof Mob m && playing(m) ? m
                : owner instanceof Player p && p.getPersistentData().hasUUID(PLAYER_MATCH) && level.getEntity(p.getPersistentData().getUUID(PLAYER_MATCH)) instanceof Mob m && playing(m) ? m : null;
        if (girl == null || !hit.getBlockPos().equals(BlockPos.of(girl.getPersistentData().getLong(TARGET)))) return 0;
        boolean hers = owner == girl;
        CompoundTag t = girl.getPersistentData();
        if (!hers && t.getInt(YOUR_SHOTS) > SHOTS) return 0;                           // only their first three count
        int points = score(hit);
        t.putInt(hers ? HERS : YOURS, t.getInt(hers ? HERS : YOURS) + points);
        if (owner instanceof Player p) p.displayClientMessage(Component.translatable("message.harmontide.archery.yours", points, t.getInt(YOURS)), true);
        else if (level.getPlayerByUUID(t.getUUID(WITH)) instanceof Player p) p.displayClientMessage(Component.translatable("message.harmontide.archery.hers", girl.getDisplayName(), points, t.getInt(HERS)), true);
        return points;
    }

    /** The player loosed an arrow (bow or crossbow): one of their three. */
    public static void loosed(Player p) {
        if (!p.getPersistentData().hasUUID(PLAYER_MATCH) || !(p.level() instanceof ServerLevel level)) return;
        if (level.getEntity(p.getPersistentData().getUUID(PLAYER_MATCH)) instanceof Mob m && playing(m)) {
            m.getPersistentData().putInt(YOUR_SHOTS, m.getPersistentData().getInt(YOUR_SHOTS) + 1);
        }
    }

    /** Every few ticks during a match: her next arrow, or the end. 1 the player won, 2 she did, 3 a draw, 0 goes on. */
    public static int tick(ServerLevel level, Mob girl) {
        CompoundTag t = girl.getPersistentData();
        Player p = level.getPlayerByUUID(t.getUUID(WITH));
        BlockPos target = BlockPos.of(t.getLong(TARGET));
        long since = level.getGameTime() - t.getLong(START);
        if (p == null || !level.getBlockState(target).is(Blocks.TARGET)) {
            end(girl, p);
            return 3;
        }
        girl.getNavigation().stop();                                       // she stays at her mark
        int shots = t.getInt(HER_SHOTS);
        if (shots < SHOTS && since >= FIRST + (long) shots * BETWEEN) {
            shoot(level, girl, target);
            t.putInt(HER_SHOTS, shots + 1);
        }
        boolean herDone = shots >= SHOTS && since >= FIRST + (long) SHOTS * BETWEEN;   // her last arrow has had time to land
        if (!herDone || t.getInt(YOUR_SHOTS) < SHOTS && since < TIME) return 0;
        return finish(level, girl, p);
    }

    static int finish(ServerLevel level, Mob girl, Player p) {
        CompoundTag t = girl.getPersistentData();
        int hers = t.getInt(HERS), yours = t.getInt(YOURS);
        end(girl, p);
        ((Bonded) girl).addAffection(p, AFFECTION);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
        int result = yours > hers ? 1 : hers > yours ? 2 : 3;
        if (result == 1) {
            if (girl instanceof GiftGiver giver) {
                ItemStack prize = giver.rollGift(p);
                if (!prize.isEmpty()) girl.spawnAtLocation(prize);
            }
            Dairy.award(p, "archery_won");
        } else if (result == 2) {
            girl.getJumpControl().jump();
        }
        p.displayClientMessage(Component.translatable("message.harmontide.archery." + (result == 1 ? "won" : result == 2 ? "lost" : "draw"),
                girl.getDisplayName(), yours, hers).withStyle(result == 1 ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE), false);
        level.playSound(null, girl.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.7F, result == 1 ? 1.0F : 1.5F);
        Dairy.award(p, "archery");
        return result;
    }

    private static void end(Mob girl, @Nullable Player p) {
        CompoundTag t = girl.getPersistentData();
        for (String k : new String[]{TARGET, WITH, START, HERS, HER_SHOTS, YOURS, YOUR_SHOTS}) t.remove(k);
        if (p != null) p.getPersistentData().remove(PLAYER_MATCH);
    }

    // --- events -------------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob m && m.tickCount % 5 == 3 && playing(m) && m.level() instanceof ServerLevel level) tick(level, m);
    }

    @SubscribeEvent
    public static void onImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof AbstractArrow mine && mine.getOwner() instanceof Mob shooter && playing(shooter)
                && event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult) {
            event.setCanceled(true);                                         // her match arrows are for the target: they pass by anyone in the way
            return;
        }
        if (event.getProjectile() instanceof AbstractArrow arrow && event.getRayTraceResult() instanceof BlockHitResult hit
                && arrow.level() instanceof ServerLevel level && level.getBlockState(hit.getBlockPos()).is(Blocks.TARGET)) {
            hit(level, arrow.getOwner(), hit);
        }
    }

    /** Every arrow a player in a match sends off counts as a shot, from a bow or a crossbow alike (ArrowLooseEvent misses crossbows). */
    @SubscribeEvent
    public static void onArrowAdded(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (!event.loadedFromDisk() && !event.getLevel().isClientSide && event.getEntity() instanceof AbstractArrow arrow && arrow.getOwner() instanceof Player p) loosed(p);
    }

    /** Sneaking with a bow: a match, if there's a target near her (before her weapons code would take the bow). */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Player p = event.getEntity();
        if (!p.isShiftKeyDown() || !(event.getItemStack().getItem() instanceof BowItem) || !(event.getTarget() instanceof Mob m) || !(m instanceof Bonded b)
                || m.isBaby() || b.getAffection(p) < HomeLife.FRIEND) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos target = target(level, m);
        if (target == null) p.displayClientMessage(Component.translatable("message.harmontide.archery.no_target", m.getDisplayName()), true);
        else start(level, m, p, target);
    }
}
