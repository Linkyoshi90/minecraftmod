package com.linkyoshi90.monstergirls.world;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.order.Order;
import com.linkyoshi90.monstergirls.entity.order.OrderSister;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Optional;

/**
 * A wayside chapel of the Order (2026-11-08): a little white hall of calcite and quartz with sky-blue windows, a bell over the
 * door, the Order's banner behind a holy font (which wards twelve blocks round it from the realm), a chest of the Order's
 * stores, and one sister-knight who keeps it (she stays near; hostile only to those the Order has marked). In plains,
 * meadows and light forests; /locate structure monstergirls:order_chapel or /monstergirls locate chapel finds one.
 * Built in code, facing south (door at -z), 7 wide and 9 long, on a foundation down to the ground.
 */
public class OrderChapelStructure extends Structure {
    public static final MapCodec<OrderChapelStructure> CODEC = simpleCodec(OrderChapelStructure::new);
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, MonsterGirlsMod.id("chests/order_chapel"));
    public static final int HALF_W = 3, HALF_L = 4, FOUNDATION = 6, KEEP = 12;

    public OrderChapelStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
        var gen = context.chunkGenerator();
        int y = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        // not on water, and not on a slope steeper than the foundation can carry
        int lo = y, hi = y;
        for (int dx = -HALF_W; dx <= HALF_W; dx += HALF_W) {
            for (int dz = -HALF_L; dz <= HALF_L; dz += HALF_L) {
                int h = gen.getFirstOccupiedHeight(x + dx, z + dz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                lo = Math.min(lo, h);
                hi = Math.max(hi, h);
            }
        }
        if (hi - lo > 3) return Optional.empty();
        int ocean = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        if (ocean < y) return Optional.empty();
        BlockPos origin = new BlockPos(x, hi, z);
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new Piece(origin, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.ORDER_CHAPEL.get();
    }

    public static class Piece extends StructurePiece {
        private final BlockPos origin;
        private final long seed;

        public Piece(BlockPos origin, long seed) {
            super(ModStructures.ORDER_CHAPEL_PIECE.get(), 0, new BoundingBox(origin.getX() - HALF_W - 1, origin.getY() - FOUNDATION, origin.getZ() - HALF_L - 1,
                    origin.getX() + HALF_W + 1, origin.getY() + 9, origin.getZ() + HALF_L + 1));
            this.origin = origin;
            this.seed = seed;
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructures.ORDER_CHAPEL_PIECE.get(), tag);
            this.origin = BlockPos.of(tag.getLong("Origin"));
            this.seed = tag.getLong("Seed");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            tag.putLong("Origin", this.origin.asLong());
            tag.putLong("Seed", this.seed);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random, BoundingBox box,
                                ChunkPos chunk, BlockPos pivot) {
            if (!MonsterGirlsConfig.orderPatrols() || !box.isInside(this.origin.getX(), box.minY(), this.origin.getZ())) return;
            build(level, this.origin, RandomSource.create(this.seed), true);
        }
    }

    private static void set(WorldGenLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, Block.UPDATE_CLIENTS);
    }

    /** The chapel, its floor at {@code origin}'s height. The holy font's spot. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean keeper) {
        int x0 = origin.getX(), y0 = origin.getY(), z0 = origin.getZ();
        BlockState wall = Blocks.CALCITE.defaultBlockState(), pillar = Blocks.QUARTZ_PILLAR.defaultBlockState(), floor = Blocks.POLISHED_DIORITE.defaultBlockState();
        BlockState window = Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState(), roof = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        for (int dx = -HALF_W; dx <= HALF_W; dx++) {
            for (int dz = -HALF_L; dz <= HALF_L; dz++) {
                for (int dy = -1; dy >= -FOUNDATION; dy--) {                    // foundation down to the ground
                    BlockPos p = new BlockPos(x0 + dx, y0 + dy, z0 + dz);
                    BlockState here = level.getBlockState(p);
                    if (!here.canBeReplaced() && !here.is(net.minecraft.tags.BlockTags.LEAVES) && !here.is(net.minecraft.tags.BlockTags.LOGS)) break;
                    set(level, p, Blocks.STONE_BRICKS.defaultBlockState());
                }
                set(level, new BlockPos(x0 + dx, y0, z0 + dz), (dx + dz) % 2 == 0 ? floor : Blocks.POLISHED_ANDESITE.defaultBlockState());
                boolean edgeX = Math.abs(dx) == HALF_W, edgeZ = Math.abs(dz) == HALF_L;
                for (int dy = 1; dy <= 6; dy++) {
                    BlockPos p = new BlockPos(x0 + dx, y0 + dy, z0 + dz);
                    BlockState s = Blocks.AIR.defaultBlockState();
                    if (dy <= 4 && (edgeX || edgeZ)) {
                        if (edgeX && edgeZ) s = pillar;
                        else if (dy >= 2 && dy <= 3 && (edgeX ? Math.abs(dz) % 2 == 1 : Math.abs(dx) == 2)) s = window;
                        else s = wall;
                    }
                    if (dy == 5) s = roof;                                      // a flat roof, and a low ridge along the middle
                    if (dy == 6 && Math.abs(dx) == 1) s = Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, dx < 0 ? Direction.EAST : Direction.WEST);
                    if (dy == 6 && dx == 0) s = Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState();
                    set(level, p, s);
                }
            }
        }
        for (int dz = -HALF_L; dz <= HALF_L; dz++) {                            // gold trim under the eaves
            set(level, new BlockPos(x0 - HALF_W, y0 + 4, z0 + dz), Blocks.GOLD_BLOCK.defaultBlockState());
            set(level, new BlockPos(x0 + HALF_W, y0 + 4, z0 + dz), Blocks.GOLD_BLOCK.defaultBlockState());
        }
        // the door (an opening, two high) and the bell above it, outside
        set(level, new BlockPos(x0, y0 + 1, z0 - HALF_L), Blocks.AIR.defaultBlockState());
        set(level, new BlockPos(x0, y0 + 2, z0 - HALF_L), Blocks.AIR.defaultBlockState());
        set(level, new BlockPos(x0, y0 + 3, z0 - HALF_L - 1), Blocks.BELL.defaultBlockState()
                .setValue(BellBlock.FACING, Direction.NORTH).setValue(BellBlock.ATTACHMENT, BellAttachType.SINGLE_WALL));
        // inside: two lanterns, pews, the font, the banner, the chest
        set(level, new BlockPos(x0 - 1, y0 + 4, z0), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        set(level, new BlockPos(x0 + 1, y0 + 4, z0), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        for (int dz = -2; dz <= 0; dz += 2) {
            for (int dx : new int[]{-2, -1, 1, 2}) {
                set(level, new BlockPos(x0 + dx, y0 + 1, z0 + dz), Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
            }
        }
        BlockPos font = new BlockPos(x0, y0 + 1, z0 + HALF_L - 1);
        set(level, font, ModBlocks.HOLY_FONT.get().defaultBlockState());
        set(level, new BlockPos(x0 - 1, y0 + 1, z0 + HALF_L - 1), Blocks.CANDLE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CandleBlock.LIT, true).setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 3));
        BlockPos chest = new BlockPos(x0 + 2, y0 + 1, z0 + HALF_L - 1);
        set(level, chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH));
        net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, chest, LOOT);
        BlockPos banner = new BlockPos(x0, y0 + 3, z0 + HALF_L - 1);
        set(level, banner, Blocks.WHITE_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, Direction.NORTH));
        if (level.getBlockEntity(banner) instanceof BannerBlockEntity be) {
            be.applyComponentsFromItemStack(Order.banner(level.registryAccess()));
            be.setChanged();
        }
        if (keeper) {
            OrderSister sister = Order.type(OrderSister.Rank.KNIGHT).create(level.getLevel());
            if (sister != null) {
                sister.moveTo(x0 + 0.5, y0 + 1, z0 + 1.5, 180.0F, 0.0F);
                sister.finalizeSpawn(level, level.getCurrentDifficultyAt(origin), MobSpawnType.STRUCTURE, null);
                sister.keepChapel(font);
                level.addFreshEntityWithPassengers(sister);
            }
        }
        return font;
    }
}
