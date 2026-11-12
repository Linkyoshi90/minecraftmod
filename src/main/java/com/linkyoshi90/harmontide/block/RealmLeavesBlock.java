package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Leaves the Demon Realm has crept up into (entity/DemonRealm): a tree over realm grass turns dusky purple and weeps violet sap from its
 * underside. They remember what they were ({@link #KIND}), so torchlight ({@link RealmGrassBlock#LIGHT}) or holy water turns them back into
 * the very leaves they were; otherwise they live and decay as leaves do.
 */
public class RealmLeavesBlock extends LeavesBlock {
    public static final MapCodec<RealmLeavesBlock> CODEC = simpleCodec(RealmLeavesBlock::new);

    /** The leaves she was before. */
    public enum Kind implements StringRepresentable {
        OAK(() -> Blocks.OAK_LEAVES), SPRUCE(() -> Blocks.SPRUCE_LEAVES), BIRCH(() -> Blocks.BIRCH_LEAVES), JUNGLE(() -> Blocks.JUNGLE_LEAVES),
        ACACIA(() -> Blocks.ACACIA_LEAVES), DARK_OAK(() -> Blocks.DARK_OAK_LEAVES), MANGROVE(() -> Blocks.MANGROVE_LEAVES),
        CHERRY(() -> Blocks.CHERRY_LEAVES), AZALEA(() -> Blocks.AZALEA_LEAVES), FLOWERING_AZALEA(() -> Blocks.FLOWERING_AZALEA_LEAVES);

        private final Supplier<Block> block;

        Kind(Supplier<Block> block) {
            this.block = block;
        }

        public Block block() {
            return this.block.get();
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(java.util.Locale.ROOT);
        }

        @Nullable
        public static Kind of(BlockState state) {
            for (Kind k : values()) if (state.is(k.block())) return k;
            return null;
        }
    }

    public static final EnumProperty<Kind> KIND = EnumProperty.create("kind", Kind.class);

    public RealmLeavesBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(KIND, Kind.OAK));
    }

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(KIND);
    }

    /** Vanilla leaves of a kind it knows become realm leaves, keeping their distance, persistence and water. True if they did. */
    public static boolean corrupt(net.minecraft.world.level.LevelAccessor level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        Kind kind = Kind.of(s);
        if (kind == null || level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos) >= RealmGrassBlock.LIGHT) return false;
        level.setBlock(pos, ModBlocks.REALM_LEAVES.get().defaultBlockState().setValue(KIND, kind)
                .setValue(DISTANCE, s.getValue(DISTANCE)).setValue(PERSISTENT, s.getValue(PERSISTENT)).setValue(WATERLOGGED, s.getValue(WATERLOGGED)), Block.UPDATE_ALL);
        return true;
    }

    /** Back to the leaves they were; the weeping vines hanging from them wither. */
    public static void purify(net.minecraft.world.level.LevelAccessor level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        if (!s.is(ModBlocks.REALM_LEAVES.get())) return;
        for (BlockPos p = pos.below(); level.getBlockState(p).is(Blocks.WEEPING_VINES) || level.getBlockState(p).is(Blocks.WEEPING_VINES_PLANT); p = p.below()) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        level.setBlock(pos, s.getValue(KIND).block().defaultBlockState()
                .setValue(DISTANCE, s.getValue(DISTANCE)).setValue(PERSISTENT, s.getValue(PERSISTENT)).setValue(WATERLOGGED, s.getValue(WATERLOGGED)), Block.UPDATE_ALL);
    }

    /** The realm reaching up from a patch of ground: the first log or leaves above a spot near it. Where, or null. */
    @Nullable
    public static BlockPos creep(net.minecraft.world.level.LevelAccessor level, BlockPos ground, RandomSource random) {
        int dx = random.nextInt(5) - 2, dz = random.nextInt(5) - 2;
        for (int dy = 1; dy <= 20; dy++) {
            BlockPos p = ground.offset(dx, dy, dz);
            BlockState s = level.getBlockState(p);
            if (s.is(ModBlocks.REALM_LEAVES.get()) || s.is(ModBlocks.REALM_LOG.get())) continue;
            if (RealmLogBlock.Kind.of(s) != null) return RealmLogBlock.corrupt(level, p) ? p : null;   // up the trunk first
            if (s.is(BlockTags.LEAVES)) return corrupt(level, p) ? p : null;
            if (s.isSolidRender(level, p) && !s.is(BlockTags.LOGS)) return null;          // a roof between: the realm doesn't reach
        }
        return null;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (!level.getBlockState(pos).is(this)) return;
        if (level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos) >= RealmGrassBlock.LIGHT) purify(level, pos);
        else if (random.nextInt(VINES) == 0) hangVine(level, pos);
    }

    /** One random tick in this many hangs a short weeping vine under leaves with open air below. */
    public static final int VINES = 80;

    /** A crimson weeping vine under these leaves, short (it can only grow a few blocks more). True if one was hung. */
    public static boolean hangVine(net.minecraft.world.level.LevelAccessor level, BlockPos pos) {
        BlockPos below = pos.below();
        if (!level.isEmptyBlock(below) || !level.isEmptyBlock(below.below())) return false;
        int near = 0;                                                         // a few strands, not a curtain
        for (BlockPos p : BlockPos.betweenClosed(below.offset(-2, -1, -2), below.offset(2, 1, 2))) {
            if (level.getBlockState(p).is(Blocks.WEEPING_VINES) && ++near >= 2) return false;
        }
        level.setBlock(below, Blocks.WEEPING_VINES.defaultBlockState().setValue(net.minecraft.world.level.block.GrowingPlantHeadBlock.AGE, 22), Block.UPDATE_ALL);
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        BlockPos below = pos.below();
        if (random.nextInt(6) == 0 && !level.getBlockState(below).isFaceSturdy(level, below, net.minecraft.core.Direction.UP)
                && level.getBlockState(below).getFluidState().isEmpty()) {
            level.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, pos.getX() + 0.15 + random.nextDouble() * 0.7, pos.getY() - 0.05,
                    pos.getZ() + 0.15 + random.nextDouble() * 0.7, 0.0, 0.0, 0.0);
        }
        if (random.nextInt(40) == 0) {
            level.addParticle(RealmGrassBlock.HAZE, pos.getX() + random.nextDouble(), pos.getY() - 0.2, pos.getZ() + random.nextDouble(), 0.0, -0.01, 0.0);
        }
    }
}
