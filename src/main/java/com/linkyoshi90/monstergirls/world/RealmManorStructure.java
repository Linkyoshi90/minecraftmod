package com.linkyoshi90.monstergirls.world;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.RealmGrassBlock;
import com.linkyoshi90.monstergirls.entity.Dairy;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModGirls;
import com.linkyoshi90.monstergirls.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Optional;

/**
 * A Lilith's manor (2026-11-08): the Demon Realm's answer to the Order's chapels. Deep in dark forests a hall of realm wood on
 * polished blackstone, violet windows, purple candles, two unlit realm censers, a throne before a wall of crying obsidian,
 * a bed, a chest of the realm's treasures, realm grass round it. Its lady, a wild grown Lilith, keeps to it (and, wild, grows
 * the realm round her as every wild Lilith does), with a succubus or two. /locate structure monstergirls:realm_manor or
 * /monstergirls locate manor. Built in code, 13 by 11, door at -z, inside one chunk.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public class RealmManorStructure extends Structure {
    public static final MapCodec<RealmManorStructure> CODEC = simpleCodec(RealmManorStructure::new);
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, MonsterGirlsMod.id("chests/realm_manor"));
    public static final int HALF_W = 6, HALF_L = 5, FOUNDATION = 6, KEEP = 14;

    public RealmManorStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
        var gen = context.chunkGenerator();
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (int dx = -HALF_W; dx <= HALF_W; dx += HALF_W) {
            for (int dz = -HALF_L; dz <= HALF_L; dz += HALF_L) {
                int h = gen.getFirstOccupiedHeight(x + dx, z + dz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                lo = Math.min(lo, h);
                hi = Math.max(hi, h);
            }
        }
        if (hi - lo > 4) return Optional.empty();
        int ocean = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int top = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        if (ocean < top) return Optional.empty();
        BlockPos origin = new BlockPos(x, hi, z);
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new Piece(origin, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.REALM_MANOR.get();
    }

    public static class Piece extends StructurePiece {
        private final BlockPos origin;
        private final long seed;

        public Piece(BlockPos origin, long seed) {
            super(ModStructures.REALM_MANOR_PIECE.get(), 0, new BoundingBox(origin.getX() - HALF_W - 1, origin.getY() - FOUNDATION, origin.getZ() - HALF_L - 2,
                    origin.getX() + HALF_W + 1, origin.getY() + 12, origin.getZ() + HALF_L + 2));
            this.origin = origin;
            this.seed = seed;
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructures.REALM_MANOR_PIECE.get(), tag);
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
            if (!MonsterGirlsConfig.demonRealm() || !box.isInside(this.origin.getX(), box.minY(), this.origin.getZ())) return;
            build(level, this.origin, RandomSource.create(this.seed), true);
        }
    }

    private static void set(WorldGenLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, Block.UPDATE_CLIENTS);
    }

    private static BlockState stairs(Direction facing) {
        return ModBlocks.REALM_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING, facing);
    }

    /** The manor, its floor at {@code origin}'s height. The throne's spot. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean residents) {
        int x0 = origin.getX(), y0 = origin.getY(), z0 = origin.getZ();
        BlockState base = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), planks = ModBlocks.REALM_PLANKS.get().defaultBlockState();
        BlockState window = Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState(), pillar = ModBlocks.REALM_LOG.get().defaultBlockState();
        for (int dx = -HALF_W; dx <= HALF_W; dx++) {
            for (int dz = -HALF_L; dz <= HALF_L; dz++) {
                for (int dy = -1; dy >= -FOUNDATION; dy--) {
                    BlockPos p = new BlockPos(x0 + dx, y0 + dy, z0 + dz);
                    BlockState here = level.getBlockState(p);
                    if (!here.canBeReplaced() && !here.is(net.minecraft.tags.BlockTags.LEAVES) && !here.is(net.minecraft.tags.BlockTags.LOGS)) break;
                    set(level, p, base);
                }
                set(level, new BlockPos(x0 + dx, y0, z0 + dz), dx == 0 ? Blocks.PURPLE_WOOL.defaultBlockState() : planks);   // a runner to the throne
                boolean edgeX = Math.abs(dx) == HALF_W, edgeZ = Math.abs(dz) == HALF_L;
                for (int dy = 1; dy <= 9; dy++) {
                    BlockPos p = new BlockPos(x0 + dx, y0 + dy, z0 + dz);
                    BlockState s = Blocks.AIR.defaultBlockState();
                    if (dy <= 5 && (edgeX || edgeZ)) {
                        if (edgeX && edgeZ) s = pillar;
                        else if (dy == 1) s = base;
                        else if (dy >= 2 && dy <= 4 && (edgeX ? Math.abs(dz) % 2 == 0 && Math.abs(dz) < HALF_L : Math.abs(dx) % 3 == 1)) s = window;
                        else s = planks;
                    }
                    if (dy == 6) s = base;                                      // a stepped roof
                    if (dy == 7 && Math.abs(dx) <= HALF_W - 2) s = Math.abs(dx) == HALF_W - 2 ? stairs(dx < 0 ? Direction.EAST : Direction.WEST) : planks;
                    if (dy == 8 && Math.abs(dx) <= HALF_W - 4) s = Math.abs(dx) == HALF_W - 4 ? stairs(dx < 0 ? Direction.EAST : Direction.WEST) : planks;
                    if (dy == 9 && dx == 0) s = ModBlocks.REALM_SLAB.get().defaultBlockState();
                    set(level, p, s);
                }
            }
        }
        // the door
        BlockState door = ModBlocks.REALM_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH);
        set(level, new BlockPos(x0, y0 + 1, z0 - HALF_L), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(level, new BlockPos(x0, y0 + 2, z0 - HALF_L), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        // the throne before crying obsidian, candles either side
        BlockPos throne = new BlockPos(x0, y0 + 1, z0 + HALF_L - 1);
        set(level, throne, stairs(Direction.SOUTH));
        set(level, throne.west(), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
        set(level, throne.east(), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
        for (int dy = 1; dy <= 4; dy++) set(level, new BlockPos(x0, y0 + dy, z0 + HALF_L), Blocks.CRYING_OBSIDIAN.defaultBlockState());
        BlockState candles = Blocks.PURPLE_CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true).setValue(CandleBlock.CANDLES, 2);
        set(level, throne.west().above(), candles);
        set(level, throne.east().above(), candles);
        for (int dz : new int[]{-3, 1}) {                                       // candles along the walls
            set(level, new BlockPos(x0 - HALF_W + 1, y0 + 1, z0 + dz), candles);
            set(level, new BlockPos(x0 + HALF_W - 1, y0 + 1, z0 + dz), candles);
        }
        // two censers (unlit: a heartfruit lights them), a bed, the chest
        set(level, new BlockPos(x0 - 3, y0 + 1, z0 + 3), ModBlocks.REALM_CENSER.get().defaultBlockState());
        set(level, new BlockPos(x0 + 3, y0 + 1, z0 + 3), ModBlocks.REALM_CENSER.get().defaultBlockState());
        BlockState bed = Blocks.PURPLE_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        set(level, new BlockPos(x0 + HALF_W - 1, y0 + 1, z0 - 1), bed.setValue(BedBlock.PART, BedPart.FOOT));
        set(level, new BlockPos(x0 + HALF_W - 1, y0 + 1, z0 - 2), bed.setValue(BedBlock.PART, BedPart.HEAD));
        BlockPos chest = new BlockPos(x0 - HALF_W + 1, y0 + 1, z0 - 1);
        set(level, chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.EAST));
        net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, chest, LOOT);
        // the realm round it: grass at the foot of the walls turns
        for (int dx = -HALF_W - 1; dx <= HALF_W + 1; dx++) {
            for (int dz = -HALF_L - 2; dz <= HALF_L + 2; dz++) {
                if (Math.abs(dx) <= HALF_W && Math.abs(dz) <= HALF_L) continue;
                int x = x0 + dx, z = z0 + dz;
                BlockPos ground = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
                if (level.getBlockState(ground).is(Blocks.GRASS_BLOCK)) RealmGrassBlock.corrupt(level, ground);
            }
        }
        if (residents) {
            settle(level, GirlSpecies.LILITH_DEVIL, throne.north(), origin);
            settle(level, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(x0 - 2, y0 + 1, z0 - 2), origin);
            if (random.nextBoolean()) settle(level, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(x0 + 2, y0 + 1, z0 - 2), origin);
        }
        return throne;
    }

    /** One of the house: wild, grown, kept to the manor. */
    private static void settle(WorldGenLevel level, GirlSpecies kind, BlockPos at, BlockPos home) {
        if (!(ModGirls.type(kind).create(level.getLevel()) instanceof AbstractMonsterGirl girl)) return;
        girl.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 180.0F, 0.0F);
        girl.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.STRUCTURE, null);
        girl.setBaby(false);
        girl.setPersistenceRequired();
        girl.restrictTo(home, KEEP);
        level.addFreshEntityWithPassengers(girl);
    }

    /** A player who steps inside a manor. */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 40 != 23 || !(event.getEntity().level() instanceof ServerLevel level)) return;
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ModStructures.REALM_MANOR_KEY);
        if (structure != null && level.structureManager().getStructureWithPieceAt(event.getEntity().blockPosition(), structure).isValid()) {
            Dairy.award(event.getEntity(), "realm_manor");
        }
    }
}
