package com.linkyoshi90.harmontide.entity.order;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.RealmGrassBlock;
import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.entity.HomeLife;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModStructures;
import com.linkyoshi90.harmontide.world.OrderChapelStructure;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Fallen chapels (2026-11-08): the realm's answer to the Order's crusades. At night, offer a heartfruit at the holy font of an
 * Order chapel with at least {@link #FOLLOWERS} of the realm's own (the devil family, or a girl the realm changed) among your
 * friends close by, and the sabbath raid begins: the bell tolls, sisters rush to the chapel's defence, and the Order marks you.
 * Make every defender kneel, fall or die before {@link #LENGTH} ticks are out (or dawn) and the chapel falls: its calcite turns
 * to realm wood, its quartz to blackstone, its glass violet, its font a realm censer, and the realm takes the ground round it.
 * Lose, and the sisters keep their chapel. Kept in memory: a raid in progress ends with the server.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class FallenChapels {
    public static final int FOLLOWERS = 2, LENGTH = 3600, DEFENDERS = 2, GROUND = 7, NOTORIETY = 20, LEAVE = 48;

    private FallenChapels() {}

    public record Raid(UUID player, long until, List<UUID> defenders) {}

    private static final Map<BlockPos, Raid> RAIDS = new HashMap<>();

    public static Map<BlockPos, Raid> raids() {
        return RAIDS;
    }

    /** Whether this font belongs to a generated Order chapel (so the chapel round it is the one we know how to turn). */
    public static boolean chapelFont(ServerLevel level, BlockPos font) {
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ModStructures.ORDER_CHAPEL_KEY);
        return structure != null && level.structureManager().getStructureWithPieceAt(font, structure).isValid();
    }

    public static int followers(ServerLevel level, Player p) {
        int n = 0;
        for (Mob m : HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(16))) if (Order.realmsOwn(m)) n++;
        return n;
    }

    public static boolean night(ServerLevel level) {
        long t = level.getDayTime() % 24000L;
        return t >= 13000L && t < 23000L;
    }

    /** A heartfruit offered at the font: the raid begins, if all is ready. True if it began. */
    public static boolean begin(ServerLevel level, BlockPos font, Player p, boolean checkChapel) {
        return begin(level, font, p, checkChapel, night(level));
    }

    /** As above, told whether it's night (tests can't wait for one). */
    public static boolean begin(ServerLevel level, BlockPos font, Player p, boolean checkChapel, boolean night) {
        if (RAIDS.containsKey(font)) return false;
        String refusal = checkChapel && !chapelFont(level, font) ? "not_chapel" : !night ? "day" : followers(level, p) < FOLLOWERS ? "alone" : null;
        if (refusal != null) {
            p.displayClientMessage(Component.translatable("message.harmontide.fallen_chapel." + refusal), true);
            return false;
        }
        List<UUID> defenders = new ArrayList<>();
        for (OrderSister s : level.getEntitiesOfClass(OrderSister.class, new net.minecraft.world.phys.AABB(font).inflate(16), OrderSister::isAlive)) defenders.add(s.getUUID());
        for (int i = 0; i < DEFENDERS; i++) {
            OrderSister s = Order.type(i == 0 ? OrderSister.Rank.INQUISITOR : OrderSister.Rank.KNIGHT).create(level);
            if (s == null) continue;
            BlockPos at = font.offset(i == 0 ? -1 : 1, 0, -2);
            s.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 180F, 0F);
            s.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
            s.keepChapel(font);
            level.addFreshEntity(s);
            defenders.add(s.getUUID());
        }
        Order.addNotoriety(p, Math.max(NOTORIETY, Order.MARKED - Order.notoriety(p)));   // the Order knows who did this
        RAIDS.put(font.immutable(), new Raid(p.getUUID(), level.getGameTime() + LENGTH, defenders));
        level.playSound(null, font, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 0.6F);
        p.displayClientMessage(Component.translatable("message.harmontide.fallen_chapel.begin", defenders.size()).withStyle(ChatFormatting.DARK_PURPLE), false);
        return true;
    }

    /** Defenders still standing (alive, not kneeling, still a sister). */
    public static int standing(ServerLevel level, Raid raid) {
        int n = 0;
        for (UUID id : raid.defenders()) if (level.getEntity(id) instanceof OrderSister s && s.isAlive() && !s.kneeling()) n++;
        return n;
    }

    /** One look at a raid. True if it is over (won or lost). */
    public static boolean tick(ServerLevel level, BlockPos font, Raid raid) {
        return tick(level, font, raid, night(level));
    }

    public static boolean tick(ServerLevel level, BlockPos font, Raid raid, boolean night) {
        Player p = level.getPlayerByUUID(raid.player());
        if (standing(level, raid) == 0) {
            fall(level, font);
            if (p != null) {
                p.displayClientMessage(Component.translatable("message.harmontide.fallen_chapel.won").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                Dairy.award(p, "fallen_chapel");
            }
            return true;
        }
        if (p == null || p.distanceToSqr(font.getCenter()) > LEAVE * LEAVE || level.getGameTime() > raid.until() || !night) {
            if (p != null) p.displayClientMessage(Component.translatable("message.harmontide.fallen_chapel.lost").withStyle(ChatFormatting.GRAY), false);
            return true;
        }
        return false;
    }

    private static BlockState turned(BlockState s) {
        if (s.is(Blocks.CALCITE)) return ModBlocks.REALM_PLANKS.get().defaultBlockState();
        if (s.is(Blocks.QUARTZ_PILLAR)) return ModBlocks.REALM_LOG.get().defaultBlockState();
        if (s.is(Blocks.POLISHED_DIORITE)) return Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        if (s.is(Blocks.POLISHED_ANDESITE)) return Blocks.BLACKSTONE.defaultBlockState();
        if (s.is(Blocks.SMOOTH_QUARTZ)) return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if (s.is(Blocks.SMOOTH_QUARTZ_SLAB)) return ModBlocks.REALM_SLAB.get().defaultBlockState();
        if (s.is(Blocks.SMOOTH_QUARTZ_STAIRS) || s.is(Blocks.SPRUCE_STAIRS)) {
            return ModBlocks.REALM_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING, s.getValue(StairBlock.FACING)).setValue(StairBlock.HALF, s.getValue(StairBlock.HALF));
        }
        if (s.is(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)) return Blocks.PURPLE_STAINED_GLASS_PANE.withPropertiesOf(s);
        if (s.is(Blocks.LANTERN)) return Blocks.SOUL_LANTERN.withPropertiesOf(s);
        if (s.is(Blocks.CANDLE)) return Blocks.PURPLE_CANDLE.withPropertiesOf(s);
        if (s.is(Blocks.WHITE_WALL_BANNER)) return Blocks.PURPLE_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, s.getValue(WallBannerBlock.FACING));
        if (s.is(ModBlocks.HOLY_FONT.get())) return ModBlocks.REALM_CENSER.get().defaultBlockState();
        return null;
    }

    /** The chapel round this font falls to the realm. How many blocks turned. */
    public static int fall(ServerLevel level, BlockPos font) {
        BlockPos origin = font.offset(0, -1, -(OrderChapelStructure.HALF_L - 1));
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-OrderChapelStructure.HALF_W, 0, -OrderChapelStructure.HALF_L - 1),
                origin.offset(OrderChapelStructure.HALF_W, 6, OrderChapelStructure.HALF_L))) {
            BlockState t = turned(level.getBlockState(p));
            if (t == null) continue;
            level.setBlock(p, t, Block.UPDATE_ALL);
            n++;
        }
        for (int dx = -GROUND; dx <= GROUND; dx++) {                          // the realm takes the ground round it
            for (int dz = -GROUND; dz <= GROUND; dz++) {
                int x = origin.getX() + dx, z = origin.getZ() + dz;
                BlockPos ground = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
                if (RealmGrassBlock.corrupt(level, ground, 2)) n++;
            }
        }
        level.playSound(null, font, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 2.0F, 0.5F);
        level.sendParticles(ParticleTypes.WITCH, font.getX() + 0.5, font.getY() + 2, font.getZ() + 0.5, 80, 3, 2, 3, 0.05);
        return n;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % 20 != 9 || RAIDS.isEmpty()) return;
        RAIDS.entrySet().removeIf(e -> level.isLoaded(e.getKey()) && tick(level, e.getKey(), e.getValue()));
    }
}
