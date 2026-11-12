package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A heartfruit bush: grows only on Demon Realm grass. Ripe (age 2 and 3) it gives heart-shaped fruit when picked, as a berry bush
 * does. The fruit is food, and fed to a close friend it leaves her lovestruck (entity/DemonRealm).
 */
public class HeartfruitBushBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<HeartfruitBushBlock> CODEC = simpleCodec(HeartfruitBushBlock::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    /** One random tick of realm grass in this many sprouts a bush, if none grows within four blocks. */
    public static final int SPROUT = 120;
    private static final VoxelShape YOUNG = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0), GROWN = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public HeartfruitBushBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AGE) == 0 ? YOUNG : GROWN;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return grows(state);
    }

    /** Heartfruit grows on realm grass and realm sand; the frost and the bog are too cold and too wet. */
    public static boolean grows(BlockState ground) {
        return ground.is(ModBlocks.REALM_GRASS.get()) || ground.is(ModBlocks.REALM_SAND.get());
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.HEARTFRUIT.get());
    }

    /** A young bush on this realm grass, unless one already grows within four blocks. True if it sprouted. */
    public static boolean sprout(net.minecraft.world.level.LevelAccessor level, BlockPos ground) {
        BlockPos at = ground.above();
        if (!grows(level.getBlockState(ground)) || !level.isEmptyBlock(at)) return false;
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-4, -2, -4), at.offset(4, 2, 4))) {
            if (level.getBlockState(p).is(ModBlocks.HEARTFRUIT_BUSH.get())) return false;
        }
        level.setBlock(at, ModBlocks.HEARTFRUIT_BUSH.get().defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < 3;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(AGE) < 3 && random.nextInt(5) == 0) level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), Block.UPDATE_CLIENTS);
    }

    /** Picking a ripe bush: one or two fruit (two or three when fully ripe), and it starts again. How many, or 0. */
    public static int pick(Level level, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < 2) return 0;
        int count = 1 + level.random.nextInt(2) + (age == 3 ? 1 : 0);
        popResource(level, pos, new ItemStack(ModItems.HEARTFRUIT.get(), count));
        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.2F);
        level.setBlock(pos, state.setValue(AGE, 1), Block.UPDATE_CLIENTS);
        return count;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) < 2) return super.useWithoutItem(state, level, pos, player, hit);
        if (!level.isClientSide) pick(level, pos, state);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, Math.min(3, state.getValue(AGE) + 1)), Block.UPDATE_CLIENTS);
    }
}
