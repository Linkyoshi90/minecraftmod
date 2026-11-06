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

    /** A hearth with this many living there keeps hostile mobs from spawning around it (config hearthSafeRadius). */
    public static final int SAFE_RESIDENTS = 2;

    /** Whether a spot is within the safe ring of a lived-in hearth. */
    public static boolean shelters(Level level, net.minecraft.world.phys.Vec3 pos) {
        int r = com.linkyoshi90.monstergirls.MonsterGirlsConfig.hearthSafeRadius();
        if (r <= 0) return false;
        for (BlockPos p : java.util.List.copyOf(loaded(level))) {
            if (p.distToCenterSqr(pos) <= (double) r * r && level.getBlockEntity(p) instanceof HearthBlockEntity h && h.residentCount() >= SAFE_RESIDENTS) return true;
        }
        return false;
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
            h.garden(server);
            h.homely(server);
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

    // --- the garden -------------------------------------------------------------------------------------------------

    public static final int GARDEN_NEAR = 3, GARDEN_FAR = 7, GARDEN_MAX = 16;
    /** The flowers girls plant (no wither roses): each kind has her own, the same everywhere. */
    public static final java.util.List<net.minecraft.world.level.block.Block> GARDEN_FLOWERS = java.util.List.of(
            net.minecraft.world.level.block.Blocks.DANDELION, net.minecraft.world.level.block.Blocks.POPPY, net.minecraft.world.level.block.Blocks.BLUE_ORCHID,
            net.minecraft.world.level.block.Blocks.ALLIUM, net.minecraft.world.level.block.Blocks.AZURE_BLUET, net.minecraft.world.level.block.Blocks.RED_TULIP,
            net.minecraft.world.level.block.Blocks.ORANGE_TULIP, net.minecraft.world.level.block.Blocks.WHITE_TULIP, net.minecraft.world.level.block.Blocks.PINK_TULIP,
            net.minecraft.world.level.block.Blocks.OXEYE_DAISY, net.minecraft.world.level.block.Blocks.CORNFLOWER, net.minecraft.world.level.block.Blocks.LILY_OF_THE_VALLEY,
            net.minecraft.world.level.block.Blocks.TORCHFLOWER);

    /** Her kind's flower. */
    public static net.minecraft.world.level.block.Block flowerOf(AbstractMonsterGirl girl) {
        return GARDEN_FLOWERS.get(Math.floorMod(girl.getSpecies().id().hashCode(), GARDEN_FLOWERS.size()));
    }

    /**
     * Every morning each resident at home plants her kind's flower on the grass between 3 and 7 blocks from the hearth, until
     * {@value #GARDEN_MAX} flowers grow there: a garden that shows who lives here. Mob griefing off: none. How many were planted.
     */
    public int garden(ServerLevel level) {
        if (!level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING) || !com.linkyoshi90.monstergirls.MonsterGirlsConfig.comfort()) return 0;
        java.util.List<BlockPos> spots = new java.util.ArrayList<>();
        int flowers = 0;
        for (BlockPos p : BlockPos.betweenClosed(this.worldPosition.offset(-GARDEN_FAR, -2, -GARDEN_FAR), this.worldPosition.offset(GARDEN_FAR, 2, GARDEN_FAR))) {
            if (level.getBlockState(p).is(net.minecraft.tags.BlockTags.SMALL_FLOWERS)) {
                flowers++;
                continue;
            }
            int d = Math.max(Math.abs(p.getX() - this.worldPosition.getX()), Math.abs(p.getZ() - this.worldPosition.getZ()));
            if (d >= GARDEN_NEAR && level.isEmptyBlock(p) && level.getBlockState(p.below()).is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)) spots.add(p.immutable());
        }
        int n = 0;
        for (UUID id : Set.copyOf(this.residents)) {
            if (flowers + n >= GARDEN_MAX || spots.isEmpty()) break;
            if (!(level.getEntity(id) instanceof AbstractMonsterGirl girl) || !girl.isAlive() || !this.worldPosition.equals(girl.homeHearth())) continue;
            BlockPos at = spots.remove(level.random.nextInt(spots.size()));
            level.setBlock(at, flowerOf(girl).defaultBlockState(), 3);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, at.getX() + 0.5, at.getY() + 0.4, at.getZ() + 0.5, 4, 0.3, 0.2, 0.3, 0.0);
            n++;
        }
        return n;
    }

    // --- the supper bell ---------------------------------------------------------------------------------------------

    public static final int BELL_NEAR = 8, BELL_HEARD = 96, BELL_CALL = 600;

    /**
     * A bell rung within {@value #BELL_NEAR} blocks of a hearth calls its residents home: each one in earshot ({@value #BELL_HEARD}
     * blocks) drops what she is doing (unless she is fighting or under orders) and comes to the hearth, and stays by it for half a
     * minute. How many answered, over every hearth near the bell.
     */
    public static int bell(ServerLevel level, BlockPos bell) {
        int n = 0;
        for (BlockPos at : Set.copyOf(loaded(level))) {
            if (at.distSqr(bell) > BELL_NEAR * BELL_NEAR || !(level.getBlockEntity(at) instanceof HearthBlockEntity h)) continue;
            for (UUID id : Set.copyOf(h.residents)) {
                if (!(level.getEntity(id) instanceof AbstractMonsterGirl girl) || !girl.isAlive() || !at.equals(girl.homeHearth())
                        || girl.distanceToSqr(at.getCenter()) > BELL_HEARD * BELL_HEARD) continue;
                girl.callHome(level.getGameTime() + BELL_CALL);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.NOTE, girl.getX(), girl.getEyeY() + 0.5, girl.getZ(), 1, 0, 0, 0, 0.5);
                n++;
            }
        }
        if (n > 0) for (Player p : level.getEntitiesOfClass(Player.class, new net.minecraft.world.phys.AABB(bell).inflate(16)))
            p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.supper_bell", n), true);
        return n;
    }

    /** Every other morning or so, each resident at home in a finished hut adds something to it (Settlement.homely). How many did. */
    public int homely(ServerLevel level) {
        int n = 0;
        for (UUID id : Set.copyOf(this.residents)) {
            if (!(level.getEntity(id) instanceof AbstractMonsterGirl girl) || !girl.isAlive() || !this.worldPosition.equals(girl.homeHearth())
                    || level.random.nextInt(2) != 0) continue;
            if (com.linkyoshi90.monstergirls.entity.girl.Settlement.makeHomely(level, girl) != null) n++;
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
