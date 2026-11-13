package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.Signs;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Locale;

/**
 * Footprints (entity/Signs): a trail of prints pressed into sand, snow or mud by a girl passing by, pointing the way she went
 * ({@link #FACING}), their shape her kind's ({@link #KIND}: hooves, paws, claws, bare feet, or the long sweep of a tail). They fade
 * after a day. Nothing to stand on or bump into; build over them as over air. Use an empty hand on them to read them
 * (FootprintBlockEntity knows whose they are and when they were made).
 */
public class FootprintBlock extends BaseEntityBlock {
    public static final MapCodec<FootprintBlock> CODEC = simpleCodec(FootprintBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Kind> KIND = EnumProperty.create("kind", Kind.class);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 0.5, 16);

    public enum Kind implements StringRepresentable {
        PAW, HOOF, CLAW, FOOT, SLITHER;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public FootprintBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(KIND, Kind.PAW));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, KIND);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FootprintBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canBeReplaced(BlockState state, net.minecraft.world.item.context.BlockPlaceContext context) {
        return true;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Signs.takesPrints(level.getBlockState(pos.below()));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbour, net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return dir == Direction.DOWN && !this.canSurvive(state, level, pos) ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState() : state;
    }

    /** They fade after a day. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof FootprintBlockEntity f && level.getGameTime() - f.made() >= Signs.FADE) level.removeBlock(pos, false);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel server) Signs.read(server, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
