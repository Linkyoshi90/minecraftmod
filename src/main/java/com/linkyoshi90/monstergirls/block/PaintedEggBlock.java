package com.linkyoshi90.monstergirls.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A painted egg the Easter Bunny hid in the grass (six colours). Breaking it gives the egg and a little treat; bringing
 * the egg back to her makes her day (see Jackalope).
 */
public class PaintedEggBlock extends Block {
    public static final MapCodec<PaintedEggBlock> CODEC = simpleCodec(PaintedEggBlock::new);
    public static final int COLORS = 6;
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, COLORS - 1);
    private static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 8.0, 11.0);

    public PaintedEggBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(COLOR, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    /** What finding one is worth: mostly sweets and dyes, sometimes gold, rarely an emerald. */
    public static ItemStack treat(RandomSource random) {
        int r = random.nextInt(100);
        if (r < 3) return new ItemStack(Items.EMERALD);
        if (r < 15) return new ItemStack(Items.GOLD_NUGGET, 2 + random.nextInt(3));
        if (r < 45) return new ItemStack(Items.COOKIE, 2);
        if (r < 60) return new ItemStack(Items.CARROT, 2);
        var dyes = new net.minecraft.world.item.Item[]{Items.PINK_DYE, Items.LIGHT_BLUE_DYE, Items.YELLOW_DYE, Items.LIME_DYE, Items.MAGENTA_DYE};
        return new ItemStack(dyes[random.nextInt(dyes.length)], 2);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative()) popResource(level, pos, treat(level.random));
        return super.playerWillDestroy(level, pos, state, player);
    }
}
