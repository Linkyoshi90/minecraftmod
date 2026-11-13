package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Pastures (2026-11-13): grazing and regrowth. The land remembers where it was grazed ({@link Patch}): a plant cropped
 * ({@link Kind#CROPPED}), turf worn to bare dirt by a crowd of grazers ({@link Kind#WORN}), and game trails ({@link Kind#TRAIL}): where a
 * herd of three or more keeps walking the same ground, the grass is trodden to a path ({@link #TRAMPLE} passes). Every
 * {@link #EVERY} ticks the land rests where no wild grazer is within {@link #RESTING}: after {@link Kind#rest} ticks a cropped spot grows
 * grass again (now and then a flower), worn dirt greens over, and a trail grows back. Part of the food web (config {@code foodWeb}).
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Pasture extends SavedData {
    public static final int EVERY = 600, TRAMPLE = 6, KEPT = 4096, HERD = 3;
    public static final double RESTING = 12;
    private static final Map<ServerLevel, Map<Long, Integer>> TREAD = new WeakHashMap<>();

    public enum Kind {
        CROPPED(12000), WORN(24000), TRAIL(48000);

        public final int rest;

        Kind(int rest) {
            this.rest = rest;
        }
    }

    public record Patch(BlockPos pos, Kind kind, long time) {}

    private final List<Patch> patches = new ArrayList<>();

    public static Pasture get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Pasture::new, Pasture::load, null), "harmontide_pasture");
    }

    public List<Patch> patches() {
        return this.patches;
    }

    /** The land remembers this spot was grazed. */
    public static void note(ServerLevel level, BlockPos pos, Kind kind) {
        Pasture p = get(level);
        p.patches.removeIf(x -> x.pos().equals(pos) && x.kind() == kind);
        p.patches.add(new Patch(pos.immutable(), kind, level.getGameTime()));
        while (p.patches.size() > KEPT) p.patches.remove(0);
        p.setDirty();
    }

    // --- trails ----------------------------------------------------------------------------------------------------------

    /** A herd member on the move treads the grass under her; trodden often enough it becomes a path. True if it just did. */
    public static boolean tread(ServerLevel level, AbstractMonsterGirl g) {
        if (!g.onGround() || g.getDeltaMovement().horizontalDistanceSqr() < 4.0E-4 || Herds.herd(g).size() < HERD) return false;
        BlockPos ground = g.blockPosition().below();
        if (!level.getBlockState(ground).is(Blocks.GRASS_BLOCK) || !level.getBlockState(ground.above()).isAir()) return false;
        Map<Long, Integer> tread = TREAD.computeIfAbsent(level, l -> new HashMap<>());
        int n = tread.merge(ground.asLong(), 1, Integer::sum);
        if (tread.size() > 8192) tread.clear();
        if (n < TRAMPLE) return false;
        tread.remove(ground.asLong());
        level.setBlockAndUpdate(ground, Blocks.DIRT_PATH.defaultBlockState());
        note(level, ground, Kind.TRAIL);
        return true;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof AbstractMonsterGirl g && g.tickCount % 20 == 9 && g.level() instanceof ServerLevel level
                && HarmontideConfig.foodWeb() && FoodWeb.role(g) == FoodWeb.Role.GRAZER && Herds.herds(g)) tread(level, g);
    }

    // --- regrowth --------------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level && level.getGameTime() % EVERY == 41 && HarmontideConfig.foodWeb()) regrow(level, level.getGameTime());
    }

    /** The land rests: every patch old enough, loaded, and with no grazer near grows back. How many did. */
    public static int regrow(ServerLevel level, long now) {
        Pasture p = get(level);
        if (p.patches.isEmpty()) return 0;
        int n = 0;
        for (Patch x : new ArrayList<>(p.patches)) {
            if (!level.isLoaded(x.pos())) continue;
            if (now - x.time() < x.kind().rest) continue;
            if (!level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(x.pos()).inflate(RESTING), g -> FoodWeb.role(g) == FoodWeb.Role.GRAZER && Herds.herds(g)).isEmpty()) continue;
            if (heal(level, x, level.getRandom())) n++;
            p.patches.remove(x);
        }
        p.setDirty();
        return n;
    }

    /** One patch grows back, if the ground is still as the grazers left it. */
    public static boolean heal(ServerLevel level, Patch x, RandomSource r) {
        BlockPos pos = x.pos();
        BlockState s = level.getBlockState(pos);
        switch (x.kind()) {
            case CROPPED -> {
                if (!s.isAir() || !level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) return false;
                Block plant = r.nextInt(6) == 0 ? (r.nextBoolean() ? Blocks.DANDELION : Blocks.POPPY) : r.nextInt(5) == 0 ? Blocks.FERN : Blocks.SHORT_GRASS;
                level.setBlockAndUpdate(pos, plant.defaultBlockState());
                return true;
            }
            case WORN -> {
                if (!s.is(Blocks.DIRT) || !level.getBlockState(pos.above()).isAir()) return false;
                level.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
                return true;
            }
            case TRAIL -> {
                if (!s.is(Blocks.DIRT_PATH)) return false;
                level.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
                return true;
            }
        }
        return false;
    }

    // --- saving ----------------------------------------------------------------------------------------------------------

    private static Pasture load(CompoundTag tag, HolderLookup.Provider lookup) {
        Pasture p = new Pasture();
        for (Tag t : tag.getList("Patches", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            p.patches.add(new Patch(BlockPos.of(c.getLong("Pos")), Kind.values()[Math.floorMod(c.getInt("Kind"), Kind.values().length)], c.getLong("Time")));
        }
        return p;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        for (Patch x : this.patches) {
            CompoundTag c = new CompoundTag();
            c.putLong("Pos", x.pos().asLong());
            c.putInt("Kind", x.kind().ordinal());
            c.putLong("Time", x.time());
            list.add(c);
        }
        tag.put("Patches", list);
        return tag;
    }
}
