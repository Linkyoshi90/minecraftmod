package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * The cheese shelf (CheeseShelfBlockEntity). A bucket of milk sets a cheese on it; an empty hand takes the ripest one off (sneaking: the
 * empty buckets). The button on its right-hand side (seen from the front) switches what hoppers may take: every cheese at once (green),
 * or only hard ones (red).
 */
public class CheeseShelfBlock extends BaseEntityBlock {
    public static final MapCodec<CheeseShelfBlock> CODEC = simpleCodec(CheeseShelfBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty CHEESES = IntegerProperty.create("cheeses", 0, CheeseShelfBlockEntity.CHEESES);
    /** Hoppers leave the soft and firm cheese to ripen. */
    public static final BooleanProperty PULL_HARD = BooleanProperty.create("pull_hard");
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);

    public CheeseShelfBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CHEESES, 0).setValue(PULL_HARD, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CHEESES, PULL_HARD);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** The face the filter button is on: the right-hand side to someone standing in front. */
    public static Direction buttonSide(BlockState state) {
        return state.getValue(FACING).getCounterClockWise();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CheeseShelfBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.CHEESE_SHELF.get(), CheeseShelfBlockEntity::serverTick);
    }

    /** Presses the button: switches the hopper filter. The new setting (true: only hard cheese). */
    public static boolean press(Level level, BlockPos pos, BlockState state) {
        boolean hard = !state.getValue(PULL_HARD);
        level.setBlock(pos, state.setValue(PULL_HARD, hard), 3);
        level.playSound(null, pos, SoundEvents.WOODEN_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6F, hard ? 0.8F : 1.1F);
        return hard;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hit.getDirection() == buttonSide(state)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;   // the button, whatever is in hand
        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!Dairy.isMilk(stack) || !(level.getBlockEntity(pos) instanceof CheeseShelfBlockEntity shelf)) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (shelf.addMilk(stack)) {
            player.setItemInHand(hand, net.minecraft.world.item.ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
            player.displayClientMessage(Component.translatable(CheeseShelfBlockEntity.underground(level, pos) ? "message.harmontide.cheese_set" : "message.harmontide.cheese_sky"), true);
        } else {
            player.displayClientMessage(Component.translatable("message.harmontide.cheese_full"), true);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CheeseShelfBlockEntity shelf)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (hit.getDirection() == buttonSide(state)) {
            boolean hard = press(level, pos, state);
            player.displayClientMessage(Component.translatable(hard ? "message.harmontide.cheese_filter_hard" : "message.harmontide.cheese_filter_all"), true);
            return InteractionResult.SUCCESS;
        }
        ItemStack out = player.isShiftKeyDown() ? shelf.takeBuckets() : shelf.takeRipest();
        if (out.isEmpty()) {
            player.displayClientMessage(Component.translatable(CheeseShelfBlockEntity.underground(level, pos) ? "message.harmontide.cheese_empty" : "message.harmontide.cheese_sky"), true);
        } else {
            if (out.is(com.linkyoshi90.harmontide.registry.ModItems.CHEESE.get()) && Dairy.hardness(out) == 2) Dairy.award(player, "hard_cheese");
            player.getInventory().placeItemBackInInventory(out);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CheeseShelfBlockEntity shelf) Containers.dropContents(level, pos, shelf.store());
        super.onRemove(state, level, pos, newState, moved);
    }

    /** Mobs path round it (or over it) instead of walking into it: a collision shape short of a full cube otherwise reads as open ground to the pathfinder. */
    @Override
    protected boolean isPathfindable(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return type != net.minecraft.world.level.pathfinder.PathComputationType.LAND && super.isPathfindable(state, type);
    }
}
