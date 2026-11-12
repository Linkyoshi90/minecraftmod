package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * The butter churn (ButterChurnBlockEntity). A bucket of milk pours in; an empty hand works the dasher (sneaking takes out the butter
 * and the empty buckets); a redstone pulse works it too, so a clock and two hoppers churn on their own.
 */
public class ButterChurnBlock extends BaseEntityBlock {
    public static final MapCodec<ButterChurnBlock> CODEC = simpleCodec(ButterChurnBlock::new);
    /** The dasher pushed down. */
    public static final BooleanProperty STROKE = BooleanProperty.create("stroke");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 15, 13);

    public ButterChurnBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STROKE, false).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STROKE, POWERED);
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ButterChurnBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.BUTTER_CHURN.get(), ButterChurnBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!Dairy.isMilk(stack) || !(level.getBlockEntity(pos) instanceof ButterChurnBlockEntity churn)) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (churn.pour(stack)) {
            player.setItemInHand(hand, net.minecraft.world.item.ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
            player.displayClientMessage(Component.translatable("message.harmontide.churn", churn.store().getItem(ButterChurnBlockEntity.MILK).getCount(), ButterChurnBlockEntity.MAX_MILK), true);
        } else {
            player.displayClientMessage(Component.translatable("message.harmontide.churn_full"), true);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ButterChurnBlockEntity churn)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown() || churn.store().getItem(ButterChurnBlockEntity.MILK).isEmpty()) {
            boolean any = false;
            for (int slot : new int[] {ButterChurnBlockEntity.BUTTER, ButterChurnBlockEntity.BUCKETS}) {
                ItemStack out = churn.store().removeItemNoUpdate(slot);
                if (out.isEmpty()) continue;
                if (slot == ButterChurnBlockEntity.BUTTER) Dairy.award(player, "churned");
                player.getInventory().placeItemBackInInventory(out);
                any = true;
            }
            churn.store().setChanged();
            if (!any) player.displayClientMessage(Component.translatable("message.harmontide.churn_empty"), true);
        } else if (churn.churn(level, pos)) {
            player.causeFoodExhaustion(0.1F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered == state.getValue(POWERED)) return;
        level.setBlock(pos, state.setValue(POWERED, powered), 2);
        if (powered && level.getBlockEntity(pos) instanceof ButterChurnBlockEntity churn) churn.churn(level, pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ButterChurnBlockEntity churn) Containers.dropContents(level, pos, churn.store());
        super.onRemove(state, level, pos, newState, moved);
    }
}
