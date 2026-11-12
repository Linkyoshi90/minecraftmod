package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * A trunk the Demon Realm has crept up (RealmLeavesBlock#creep): dark bark split by glowing violet veins that now and then bead with sap.
 * It remembers its wood ({@link #KIND}), so light or holy water gives the tree back its own log, axis and all.
 */
public class RealmLogBlock extends RotatedPillarBlock {
    public static final MapCodec<RealmLogBlock> CODEC = simpleCodec(RealmLogBlock::new);

    public enum Kind implements StringRepresentable {
        OAK(() -> Blocks.OAK_LOG), SPRUCE(() -> Blocks.SPRUCE_LOG), BIRCH(() -> Blocks.BIRCH_LOG), JUNGLE(() -> Blocks.JUNGLE_LOG),
        ACACIA(() -> Blocks.ACACIA_LOG), DARK_OAK(() -> Blocks.DARK_OAK_LOG), MANGROVE(() -> Blocks.MANGROVE_LOG), CHERRY(() -> Blocks.CHERRY_LOG);

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

    public RealmLogBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(KIND, Kind.OAK).setValue(AXIS, net.minecraft.core.Direction.Axis.Y));
    }

    @Override
    public MapCodec<? extends RotatedPillarBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(KIND);
    }

    /** A vanilla log of a kind it knows turns, keeping its axis. True if it did. */
    public static boolean corrupt(net.minecraft.world.level.LevelAccessor level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        Kind kind = Kind.of(s);
        if (kind == null || level.getBrightness(LightLayer.BLOCK, pos) >= RealmGrassBlock.LIGHT) return false;
        level.setBlock(pos, ModBlocks.REALM_LOG.get().defaultBlockState().setValue(KIND, kind).setValue(AXIS, s.getValue(AXIS)), Block.UPDATE_ALL);
        return true;
    }

    public static void purify(net.minecraft.world.level.LevelAccessor level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        if (!s.is(ModBlocks.REALM_LOG.get())) return;
        level.setBlock(pos, s.getValue(KIND).block().defaultBlockState().setValue(AXIS, s.getValue(AXIS)), Block.UPDATE_ALL);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBrightness(LightLayer.BLOCK, pos) >= RealmGrassBlock.LIGHT) purify(level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(14) != 0) return;
        net.minecraft.core.Direction side = net.minecraft.core.Direction.Plane.HORIZONTAL.getRandomDirection(random);
        if (!level.isEmptyBlock(pos.relative(side))) return;
        double x = pos.getX() + 0.5 + side.getStepX() * 0.52, z = pos.getZ() + 0.5 + side.getStepZ() * 0.52;
        level.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, x + (side.getStepX() == 0 ? random.nextDouble() - 0.5 : 0) * 0.8, pos.getY() + random.nextDouble() * 0.8,
                z + (side.getStepZ() == 0 ? random.nextDouble() - 0.5 : 0) * 0.8, 0, 0, 0);
    }
}
