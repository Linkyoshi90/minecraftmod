package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.HearthBlockEntity;
import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.registry.ModGirls;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A hearth that grows into a place (2026-11-08). With {@link #HAMLET} residents it is a hamlet: it takes a name (made from
 * its position, or given with a name tag) and raises its own banner beside the hearth, and anyone who walks in sees the name.
 * With {@link #VILLAGE} it is a village, and word gets around: every {@link #NEWCOMER_EVERY} days, on a morning its owner is
 * home, a wandering girl of a family that doesn't live there yet comes and asks to stay (already their friend enough to
 * settle), until {@link #FULL} live there. Off with the option hamlets.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Hamlet {
    public static final int HAMLET = 3, VILLAGE = 6, FULL = 12, NEWCOMER_EVERY = 5, NEWCOMER_ON = 2, GREET = 32;

    private Hamlet() {}

    public enum Tier { HEARTH, HAMLET, VILLAGE }

    public static Tier tier(int residents) {
        return residents >= VILLAGE ? Tier.VILLAGE : residents >= HAMLET ? Tier.HAMLET : Tier.HEARTH;
    }

    private static final String[] FIRST = {"Willow", "Moss", "Fern", "Honey", "Amber", "Clover", "Thistle", "Bramble", "Heather", "Rowan", "Hazel",
            "Briar", "Lark", "Wren", "Dew", "Rose", "Mist", "Sun", "Moon", "Star", "Ash", "Elder", "Plum", "Bell"};
    private static final String[] LAST = {"mere", "brook", "dale", "hollow", "field", "wick", "ford", "glen", "haven", "stead", "burrow", "croft",
            "combe", "well", "nook", "ridge"};

    /** The name a hearth at this spot takes (the same spot always gives the same name). */
    public static String nameFor(BlockPos pos) {
        RandomSource r = RandomSource.create(pos.asLong() * 31L + 7L);
        return FIRST[r.nextInt(FIRST.length)] + LAST[r.nextInt(LAST.length)];
    }

    /** The banner the place flies: a field and two charges, all from its name. */
    public static ItemStack banner(ServerLevel level, String name) {
        RandomSource r = RandomSource.create(name.hashCode());
        DyeColor[] colours = DyeColor.values();
        DyeColor field = colours[r.nextInt(colours.length)];
        List<ResourceKey<BannerPattern>> charges = List.of(BannerPatterns.FLOWER, BannerPatterns.CIRCLE_MIDDLE, BannerPatterns.RHOMBUS_MIDDLE,
                BannerPatterns.STRIPE_CENTER, BannerPatterns.CROSS, BannerPatterns.TRIANGLE_TOP, BannerPatterns.HALF_HORIZONTAL, BannerPatterns.BORDER,
                BannerPatterns.CURLY_BORDER, BannerPatterns.GRADIENT, BannerPatterns.STRIPE_TOP, BannerPatterns.GLOBE);
        var lookup = level.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
        BannerPatternLayers.Builder b = new BannerPatternLayers.Builder();
        for (int i = 0; i < 2; i++) {
            DyeColor c;
            do c = colours[r.nextInt(colours.length)]; while (c == field);
            Holder<BannerPattern> p = lookup.getOrThrow(charges.get(r.nextInt(charges.size())));
            b.add(p, c);
        }
        ItemStack stack = new ItemStack(BannerBlock.byColor(field));
        stack.set(DataComponents.BANNER_PATTERNS, b.build());
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.harmontide.hamlet_banner", name));
        return stack;
    }

    /** It raises its banner on a spot beside the hearth (open, on solid ground). Where, or null if there's no room. */
    @Nullable
    public static BlockPos raiseBanner(ServerLevel level, HearthBlockEntity hearth) {
        BlockPos h = hearth.getBlockPos();
        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int far = 1; far <= 2; far++) {
                BlockPos p = h.relative(d, far).relative(d.getClockWise());
                if (!level.isEmptyBlock(p) || !level.isEmptyBlock(p.above()) || !level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)) continue;
                ItemStack stack = banner(level, hearth.placeName());
                level.setBlockAndUpdate(p, ((BannerBlock) ((net.minecraft.world.item.BlockItem) stack.getItem()).getBlock()).defaultBlockState()
                        .setValue(BannerBlock.ROTATION, (d.get2DDataValue() * 4) % 16));
                if (level.getBlockEntity(p) instanceof BannerBlockEntity be) {
                    be.applyComponentsFromItemStack(stack);
                    be.setChanged();
                }
                return p;
            }
        }
        return null;
    }

    /** The morning hook (HearthBlockEntity.serverTick): a hamlet names itself and raises its banner; a village may draw a newcomer. */
    public static void morning(ServerLevel level, HearthBlockEntity hearth) {
        if (!HarmontideConfig.hamlets()) return;
        Tier tier = tier(hearth.residentCount());
        if (tier == Tier.HEARTH) return;
        Player owner = hearth.owner() == null ? null : level.getPlayerByUUID(hearth.owner());
        if (!hearth.named()) {
            hearth.setPlaceName(nameFor(hearth.getBlockPos()));
            if (owner != null) {
                owner.displayClientMessage(Component.translatable("message.harmontide.hamlet.named", hearth.placeName()).withStyle(ChatFormatting.GOLD), false);
                Dairy.award(owner, "hamlet");
            }
        }
        if (!hearth.bannerRaised() && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && raiseBanner(level, hearth) != null) hearth.setBannerRaised(true);
        long day = level.getDayTime() / 24000L;
        if (tier == Tier.VILLAGE && Math.floorMod(day, (long) NEWCOMER_EVERY) == NEWCOMER_ON && owner != null
                && owner.distanceToSqr(hearth.getBlockPos().getCenter()) <= 64 * 64) newcomer(level, hearth, owner);
    }

    /** Word has got around: a wandering girl comes to the village and asks to stay. She, or null (full, nobody new to come). */
    @Nullable
    public static AbstractMonsterGirl newcomer(ServerLevel level, HearthBlockEntity hearth, Player owner) {
        if (hearth.residentCount() >= FULL) return null;
        List<GirlSpecies> from = Market.candidates(level, hearth.getBlockPos());
        if (from.isEmpty()) return null;
        AbstractMonsterGirl girl = ModGirls.type(from.get(level.random.nextInt(from.size()))).create(level);
        if (girl == null) return null;
        BlockPos at = Market.spot(level, hearth.getBlockPos(), girl);
        girl.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360F, 0);
        girl.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
        girl.setBaby(false);
        girl.setPersistenceRequired();
        girl.addAffection(owner, Orders.AFFECTION);                        // she has heard of them: friend enough to settle
        if (!level.addFreshEntity(girl)) return null;
        Settlement.settle(level, girl, hearth);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, girl.getX(), girl.getY() + 1, girl.getZ(), 12, 0.5, 0.5, 0.5, 0);
        owner.displayClientMessage(Component.translatable("message.harmontide.hamlet.newcomer", girl.getDisplayName(), hearth.placeName())
                .withStyle(ChatFormatting.GREEN), false);
        Dairy.award(owner, "village");
        return girl;
    }

    /** The place this player stands in last time we looked (by hearth), so the name shows once on the way in. */
    private static final Map<UUID, BlockPos> IN = new HashMap<>();

    /** The named place (hamlet or village) within GREET blocks of this spot, or null. */
    @Nullable
    public static HearthBlockEntity placeAt(ServerLevel level, BlockPos pos) {
        for (BlockPos p : List.copyOf(HearthBlockEntity.loaded(level))) {
            if (p.closerThan(pos, GREET) && level.getBlockEntity(p) instanceof HearthBlockEntity h && h.named() && tier(h.residentCount()) != Tier.HEARTH) return h;
        }
        return null;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (p.tickCount % 20 != 11 || !(p.level() instanceof ServerLevel level) || !HarmontideConfig.hamlets()) return;
        HearthBlockEntity h = placeAt(level, p.blockPosition());
        BlockPos was = IN.get(p.getUUID());
        if (h == null) {
            IN.remove(p.getUUID());
            return;
        }
        if (h.getBlockPos().equals(was)) return;
        IN.put(p.getUUID(), h.getBlockPos());
        String kind = tier(h.residentCount()) == Tier.VILLAGE ? "village" : "hamlet";
        p.displayClientMessage(Component.translatable("message.harmontide.hamlet.enter." + kind, h.placeName(), h.residentCount())
                .withStyle(ChatFormatting.GOLD), true);
    }

    /** A line for the hearth's own message: its name and what it is, or null for a plain hearth. */
    @Nullable
    public static Component describe(HearthBlockEntity h) {
        if (!h.named() || tier(h.residentCount()) == Tier.HEARTH) return null;
        return Component.translatable("message.harmontide.hamlet.enter." + (tier(h.residentCount()) == Tier.VILLAGE ? "village" : "hamlet"),
                h.placeName(), h.residentCount());
    }
}
