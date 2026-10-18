package com.linkyoshi90.monstergirls.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Everything crows have buried in this dimension, keyed by the block it's buried in. Kept with the world rather
 * than on the crow, so whoever digs up the spot finds the item even when its crow is far away (or long gone).
 */
public final class CrowCaches extends SavedData {
    private static final String NAME = "monstergirls_crow_caches";

    /**
     * One buried item. {@code watched}: someone saw her bury it, so she'll come back and move it once they've left;
     * {@code retrieveAt}: when she digs it up again anyway, to carry it around (and maybe hide it somewhere new).
     */
    public record Cache(BlockPos pos, ItemStack item, UUID crow, long retrieveAt, boolean watched) {}

    private final Map<Long, Cache> caches = new HashMap<>();

    public static CrowCaches get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(CrowCaches::new, CrowCaches::load, null), NAME);
    }

    public void add(Cache cache) {
        this.caches.put(cache.pos().asLong(), cache);
        this.setDirty();
    }

    @Nullable
    public Cache at(BlockPos pos) {
        return this.caches.get(pos.asLong());
    }

    @Nullable
    public Cache remove(BlockPos pos) {
        Cache cache = this.caches.remove(pos.asLong());
        if (cache != null) this.setDirty();
        return cache;
    }

    public List<Cache> of(UUID crow) {
        List<Cache> mine = new ArrayList<>();
        for (Cache cache : this.caches.values()) if (cache.crow().equals(crow)) mine.add(cache);
        return mine;
    }

    public boolean anyNear(BlockPos pos, double radius) {
        for (Cache cache : this.caches.values()) if (cache.pos().distSqr(pos) <= radius * radius) return true;
        return false;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Cache cache : this.caches.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Pos", cache.pos().asLong());
            entry.put("Item", cache.item().save(registries));
            entry.putUUID("Crow", cache.crow());
            entry.putLong("RetrieveAt", cache.retrieveAt());
            entry.putBoolean("Watched", cache.watched());
            list.add(entry);
        }
        tag.put("Caches", list);
        return tag;
    }

    private static CrowCaches load(CompoundTag tag, HolderLookup.Provider registries) {
        CrowCaches data = new CrowCaches();
        for (Tag t : tag.getList("Caches", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            BlockPos pos = BlockPos.of(entry.getLong("Pos"));
            ItemStack.parse(registries, entry.getCompound("Item")).ifPresent(item -> data.caches.put(pos.asLong(),
                    new Cache(pos, item, entry.getUUID("Crow"), entry.getLong("RetrieveAt"), entry.getBoolean("Watched"))));
        }
        return data;
    }
}
