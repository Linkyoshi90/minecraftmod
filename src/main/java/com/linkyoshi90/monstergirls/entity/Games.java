package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Two more games with a friend (2026-11-08), alongside hide and seek and snowballs.
 * <p>
 * <b>Races.</b> Show a friend a banner and point at somewhere {@link #RACE_MIN} to {@link #RACE_MAX} blocks off: after a
 * three-count she's off for it, and so are you. First within {@link #FINISH} blocks wins. Win and she pays up with a present;
 * lose and she's delighted with herself. Either way she likes you the better for it. A centaur is hard to beat; a slime isn't.
 * <p>
 * <b>Treasure hunts.</b> Hand a friend a shovel and she goes and buries one of her presents within a dozen blocks, under a block
 * of suspicious sand or gravel. Wander about and she calls warmer or colder; brush it out to find it.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Games {
    public static final int RACE_MIN = 8, RACE_MAX = 48, FINISH = 2, COUNTDOWN = 60, RACE_TIME = 1200, RACE_AFFECTION = 3;
    public static final int HUNT_MIN = 6, HUNT_MAX = 12, HUNT_TIME = 6000, HUNT_AFFECTION = 3;
    private static final String RACE_TO = "RaceTo", RACE_WITH = "RaceWith", RACE_GO = "RaceGo", RACE_UNTIL = "RaceUntil";
    private static final String HUNT_AT = "HuntAt", HUNT_FOR = "HuntFor", HUNT_UNTIL = "HuntUntil", HUNT_LAST = "HuntLast";

    private Games() {}

    private static boolean friend(Mob m, Player p) {
        return m instanceof Bonded b && !m.isBaby() && b.getAffection(p) >= HomeLife.FRIEND && !b.isOrderedToSit();
    }

    // --- races --------------------------------------------------------------------------------------------------------

    /** Where the player is pointing, if it's a fair distance off: the finish. */
    @Nullable
    public static BlockPos finishLine(ServerLevel level, Player p) {
        Vec3 eye = p.getEyePosition(), to = eye.add(p.getLookAngle().scale(RACE_MAX));
        BlockHitResult hit = level.clip(new ClipContext(eye, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, p));
        if (hit.getType() != HitResult.Type.BLOCK) return null;
        BlockPos at = hit.getBlockPos().above();
        double d = Math.sqrt(at.distToCenterSqr(p.position()));
        return d < RACE_MIN ? null : at;
    }

    public static boolean racing(Mob m) {
        return m.getPersistentData().contains(RACE_TO);
    }

    /** The race is on (after a three-count). True if it began. */
    public static boolean startRace(ServerLevel level, Mob girl, Player p, BlockPos finish) {
        if (!friend(girl, p) || racing(girl)) return false;
        CompoundTag t = girl.getPersistentData();
        t.putLong(RACE_TO, finish.asLong());
        t.putUUID(RACE_WITH, p.getUUID());
        t.putLong(RACE_GO, level.getGameTime() + COUNTDOWN);
        t.putLong(RACE_UNTIL, level.getGameTime() + COUNTDOWN + RACE_TIME);
        girl.getNavigation().stop();
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, finish.getX() + 0.5, finish.getY() + 0.5, finish.getZ() + 0.5, 20, 0.4, 1.0, 0.4, 0.0);
        p.displayClientMessage(Component.translatable("message.monstergirls.race.ready", girl.getDisplayName()).withStyle(ChatFormatting.YELLOW), true);
        return true;
    }

    private static void endRace(Mob girl) {
        CompoundTag t = girl.getPersistentData();
        t.remove(RACE_TO);
        t.remove(RACE_WITH);
        t.remove(RACE_GO);
        t.remove(RACE_UNTIL);
        girl.getNavigation().stop();
    }

    /** One look at a race. 1 if the player won, 2 if she did, 3 if time ran out, 0 if it goes on. */
    public static int raceTick(ServerLevel level, Mob girl) {
        CompoundTag t = girl.getPersistentData();
        Player p = level.getPlayerByUUID(t.getUUID(RACE_WITH));
        if (p == null) {
            endRace(girl);
            return 3;
        }
        BlockPos finish = BlockPos.of(t.getLong(RACE_TO));
        long now = level.getGameTime(), go = t.getLong(RACE_GO);
        if (now < go) {
            long left = (go - now + 19) / 20;
            p.displayClientMessage(Component.literal(left + "...").withStyle(ChatFormatting.YELLOW), true);
            girl.getNavigation().stop();
            return 0;
        }
        if (now - go < 10) p.displayClientMessage(Component.translatable("message.monstergirls.race.go").withStyle(ChatFormatting.GREEN), true);
        Vec3 f = Vec3.atBottomCenterOf(finish);
        if (p.position().subtract(f).horizontalDistanceSqr() <= FINISH * FINISH) return finishRace(level, girl, p, true);
        if (girl.position().subtract(f).horizontalDistanceSqr() <= FINISH * FINISH) return finishRace(level, girl, p, false);
        if (now > t.getLong(RACE_UNTIL)) {
            endRace(girl);
            p.displayClientMessage(Component.translatable("message.monstergirls.race.timeout", girl.getDisplayName()), true);
            return 3;
        }
        if (girl.getNavigation().isDone() || now % 20 == 0) girl.getNavigation().moveTo(f.x, f.y, f.z, 1.6);
        return 0;
    }

    private static int finishRace(ServerLevel level, Mob girl, Player p, boolean playerWon) {
        endRace(girl);
        Bonded b = (Bonded) girl;
        b.addAffection(p, RACE_AFFECTION);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
        if (playerWon) {
            if (girl instanceof GiftGiver giver) {
                ItemStack prize = giver.rollGift(p);
                if (!prize.isEmpty()) girl.spawnAtLocation(prize);
            }
            p.displayClientMessage(Component.translatable("message.monstergirls.race.won", girl.getDisplayName()).withStyle(ChatFormatting.GOLD), false);
            Dairy.award(p, "race_won");
        } else {
            girl.getJumpControl().jump();
            p.displayClientMessage(Component.translatable("message.monstergirls.race.lost", girl.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        level.playSound(null, girl.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.7F, playerWon ? 1.0F : 1.5F);
        Dairy.award(p, "race");
        return playerWon ? 1 : 2;
    }

    // --- treasure hunts -----------------------------------------------------------------------------------------------

    public static boolean hunting(Mob m) {
        return m.getPersistentData().contains(HUNT_AT);
    }

    /** A spot to bury it: a dry top block within reach, on the ground. */
    @Nullable
    public static BlockPos burySpot(ServerLevel level, BlockPos near) {
        for (int i = 0; i < 24; i++) {
            double a = level.random.nextDouble() * Math.PI * 2, d = HUNT_MIN + level.random.nextDouble() * (HUNT_MAX - HUNT_MIN);
            int x = near.getX() + (int) Math.round(Math.cos(a) * d), z = near.getZ() + (int) Math.round(Math.sin(a) * d);
            if (!level.hasChunkAt(new BlockPos(x, 0, z))) continue;
            BlockPos top = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
            if (Math.abs(top.getY() - near.getY()) > 6) continue;
            var s = level.getBlockState(top);
            if (s.is(Blocks.SAND) || s.is(Blocks.GRAVEL) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.PODZOL)) return top;
        }
        return null;
    }

    /** She buries a present under suspicious sand (in sand) or gravel (anywhere else). */
    public static boolean bury(ServerLevel level, Mob girl, Player p, BlockPos at, ItemStack prize) {
        if (prize.isEmpty()) return false;
        boolean sand = level.getBlockState(at).is(Blocks.SAND);
        level.setBlockAndUpdate(at, (sand ? Blocks.SUSPICIOUS_SAND : Blocks.SUSPICIOUS_GRAVEL).defaultBlockState());
        if (!(level.getBlockEntity(at) instanceof BrushableBlockEntity be)) return false;
        HolderLookup.Provider reg = level.registryAccess();
        CompoundTag tag = new CompoundTag();
        tag.put("item", prize.save(reg));
        be.loadCustomOnly(tag, reg);
        be.setChanged();
        CompoundTag t = girl.getPersistentData();
        t.putLong(HUNT_AT, at.asLong());
        t.putUUID(HUNT_FOR, p.getUUID());
        t.putLong(HUNT_UNTIL, level.getGameTime() + HUNT_TIME);
        t.putDouble(HUNT_LAST, Math.sqrt(at.distToCenterSqr(p.position())));
        return true;
    }

    /** She buries it now (the game begins). True if she did. */
    public static boolean startHunt(ServerLevel level, Mob girl, Player p) {
        if (!friend(girl, p) || hunting(girl)) return false;
        BlockPos at = burySpot(level, girl.blockPosition());
        if (at == null) return false;
        ItemStack prize = girl instanceof GiftGiver giver ? giver.rollGift(p) : ItemStack.EMPTY;
        if (prize.isEmpty() || level.random.nextInt(4) == 0) prize = new ItemStack(Items.EMERALD, 1 + level.random.nextInt(2));
        if (!bury(level, girl, p, at, prize)) return false;
        p.displayClientMessage(Component.translatable("message.monstergirls.hunt.buried", girl.getDisplayName()).withStyle(ChatFormatting.YELLOW), false);
        return true;
    }

    private static void endHunt(Mob girl) {
        CompoundTag t = girl.getPersistentData();
        t.remove(HUNT_AT);
        t.remove(HUNT_FOR);
        t.remove(HUNT_UNTIL);
        t.remove(HUNT_LAST);
    }

    /** One look at a hunt: warmer or colder, found, or given up. 1 found, 2 time up, 0 goes on. */
    public static int huntTick(ServerLevel level, Mob girl) {
        CompoundTag t = girl.getPersistentData();
        BlockPos at = BlockPos.of(t.getLong(HUNT_AT));
        Player p = level.getPlayerByUUID(t.getUUID(HUNT_FOR));
        if (p == null) {
            endHunt(girl);
            return 2;
        }
        if (level.isLoaded(at) && !level.getBlockState(at).is(Blocks.SUSPICIOUS_GRAVEL) && !level.getBlockState(at).is(Blocks.SUSPICIOUS_SAND)) {
            endHunt(girl);
            ((Bonded) girl).addAffection(p, HUNT_AFFECTION);
            level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
            p.displayClientMessage(Component.translatable("message.monstergirls.hunt.found", girl.getDisplayName()).withStyle(ChatFormatting.GOLD), false);
            Dairy.award(p, "treasure_hunt");
            return 1;
        }
        if (level.getGameTime() > t.getLong(HUNT_UNTIL)) {
            endHunt(girl);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, at.getX() + 0.5, at.getY() + 1.2, at.getZ() + 0.5, 20, 0.3, 0.5, 0.3, 0.0);
            p.displayClientMessage(Component.translatable("message.monstergirls.hunt.shown", girl.getDisplayName()), true);
            return 2;
        }
        double d = Math.sqrt(at.distToCenterSqr(p.position())), last = t.getDouble(HUNT_LAST);
        String key = d <= 2.5 ? "hot" : d < last - 0.5 ? "warmer" : d > last + 0.5 ? "colder" : null;
        if (key != null) p.displayClientMessage(Component.translatable("message.monstergirls.hunt." + key, girl.getDisplayName()), true);
        t.putDouble(HUNT_LAST, d);
        return 0;
    }

    // --- events -------------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob m) || !(m.level() instanceof ServerLevel level)) return;
        if (m.tickCount % 5 == 2 && racing(m)) raceTick(level, m);
        if (m.tickCount % 20 == 6 && hunting(m)) huntTick(level, m);
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        Player p = event.getEntity();
        if (!(event.getTarget() instanceof Mob m) || !(m instanceof Bonded) || !(event.getLevel() instanceof ServerLevel level) || !friend(m, p)) return;
        if (stack.is(ItemTags.BANNERS)) {
            BlockPos finish = finishLine(level, p);
            if (finish == null) p.displayClientMessage(Component.translatable("message.monstergirls.race.where"), true);
            else startRace(level, m, p, finish);
        } else if (stack.is(ItemTags.SHOVELS) && !p.isShiftKeyDown()
                && !(m instanceof AbstractMonsterGirl g && g.getSpecies() == GirlSpecies.ALRAUNE_FLORA && p.isShiftKeyDown())) {
            if (!startHunt(level, m, p)) p.displayClientMessage(Component.translatable("message.monstergirls.hunt.nowhere", m.getDisplayName()), true);
        } else {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
