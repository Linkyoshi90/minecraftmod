package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/** A hearth's owner, its residents and the market's store (see HearthBlock). */
public class HearthBlockEntity extends BlockEntity {
    /** How many residents the market needs. */
    public static final int MARKET_RESIDENTS = 3;
    /** The loaded hearths of each level, for girls looking for a home without scanning the blocks round them. */
    private static final Map<Level, Set<BlockPos>> LOADED = new WeakHashMap<>();

    @Nullable
    private UUID owner;
    private final Set<UUID> residents = new LinkedHashSet<>();
    private final SimpleContainer store = new SimpleContainer(27);
    private long lastMarketDay = -1;

    public HearthBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEARTH.get(), pos, state);
        this.store.addListener(c -> this.setChanged());
    }

    public static Set<BlockPos> loaded(Level level) {
        return LOADED.getOrDefault(level, Set.of());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide) LOADED.computeIfAbsent(this.level, l -> new HashSet<>()).add(this.worldPosition.immutable());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level != null) {
            Set<BlockPos> s = LOADED.get(this.level);
            if (s != null) s.remove(this.worldPosition);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.setRemoved();
    }

    @Nullable
    public UUID owner() {
        return this.owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    public SimpleContainer store() {
        return this.store;
    }

    public int residentCount() {
        return this.residents.size();
    }

    public boolean isResident(UUID girl) {
        return this.residents.contains(girl);
    }

    public void addResident(UUID girl) {
        if (this.residents.add(girl)) this.setChanged();
    }

    public void removeResident(UUID girl) {
        if (this.residents.remove(girl)) this.setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HearthBlockEntity h) {
        if (level.getGameTime() % 100 != 0 || !(level instanceof ServerLevel server)) return;
        long day = server.getDayTime() / 24000L;
        if (day != h.lastMarketDay && server.getDayTime() % 24000L < 6000L) {        // the morning market
            h.lastMarketDay = day;
            h.market(server);
            com.linkyoshi90.monstergirls.entity.girl.Market.morning(server, h);
            h.setChanged();
        }
    }

    /**
     * With three residents or more, each one at home today puts a present into the store (what she would bring her
     * friend). How many presents arrived; nothing while the owner is away.
     */
    public int market(ServerLevel level) {
        Player owner = this.owner == null ? null : level.getPlayerByUUID(this.owner);
        if (owner == null || this.residents.size() < MARKET_RESIDENTS) return 0;
        int n = 0;
        for (UUID id : Set.copyOf(this.residents)) {
            if (!(level.getEntity(id) instanceof AbstractMonsterGirl girl) || !girl.isAlive()) continue;
            if (!this.worldPosition.equals(girl.homeHearth())) {
                this.residents.remove(id);                                           // she has moved on
                continue;
            }
            ItemStack gift = girl.rollGift(owner);
            if (gift.isEmpty()) continue;
            ItemStack rest = this.store.addItem(gift);
            if (rest.getCount() < gift.getCount()) n++;
        }
        return n;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.owner != null) tag.putUUID("Owner", this.owner);
        ListTag r = new ListTag();
        for (UUID id : this.residents) r.add(NbtUtils.createUUID(id));
        tag.put("Residents", r);
        tag.put("Store", this.store.createTag(registries));
        tag.putLong("LastMarketDay", this.lastMarketDay);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        this.residents.clear();
        for (Tag t : tag.getList("Residents", Tag.TAG_INT_ARRAY)) this.residents.add(NbtUtils.loadUUID(t));
        this.store.clearContent();
        this.store.fromTag(tag.getList("Store", Tag.TAG_COMPOUND), registries);
        this.lastMarketDay = tag.getLong("LastMarketDay");
    }
}
