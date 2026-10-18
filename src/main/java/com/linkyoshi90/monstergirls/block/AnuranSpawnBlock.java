package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Frog and toad spawn: a film on the surface of a still water source, laid by a mother (never placed by hand).
 * It hatches after a few minutes into a small litter of tadpoles of her species; they swim in the water, grow
 * legs after two and a half days (froglets) and are grown up after five. The Surinam toad does not use it:
 * she carries her young on her back. Spawn laid after sharing a player's bed remembers that player
 * ({@link ParentageBlockEntity}): the tadpoles are devoted to them from the start.
 */
public class AnuranSpawnBlock extends Block implements EntityBlock {
    public static final MapCodec<AnuranSpawnBlock> CODEC = simpleCodec(AnuranSpawnBlock::new);
    public static final EnumProperty<Species> SPECIES = EnumProperty.create("species", Species.class);
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.5, 16.0);
    private static final int MIN_DELAY = 3600;
    private static final int EXTRA_DELAY = 8400;

    public enum Species implements StringRepresentable {
        BULLYWUG("bullywug"), BULLFROG("bullfrog"), TREE_FROG("tree_frog"), POISON_DART_FROG("poison_dart_frog"),
        GLASS_FROG("glass_frog"), GOLIATH_FROG("goliath_frog"), DESERT_RAIN_FROG("desert_rain_frog"),
        MUCUS_TOAD("mucus_toad"), FIRE_BELLIED_TOAD("fire_bellied_toad"), CANE_TOAD("cane_toad"), NATTERJACK("natterjack");

        private final String entityId;

        Species(String entityId) {
            this.entityId = entityId;
        }

        public String entityId() {
            return this.entityId;
        }

        @Override
        public String getSerializedName() {
            return this.entityId;
        }

        @Nullable
        public static Species forEntityId(String id) {
            for (Species s : values()) if (s.entityId.equals(id)) return s;
            return null;
        }
    }

    public AnuranSpawnBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SPECIES, Species.BULLYWUG));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SPECIES);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ParentageBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Sits on top of a water source with air (or nothing) above it. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getFluidState(pos.below()).getType() == Fluids.WATER && level.getFluidState(pos.above()).getType() == Fluids.EMPTY;
    }

    /**
     * A still source block: water that is a source itself, with only sources or solid walls beside it, so no current
     * can carry the spawn away, and open sky or air above where the spawn will float.
     */
    public static boolean isStillWaterSource(LevelReader level, BlockPos waterPos) {
        if (!level.getFluidState(waterPos).isSource() || level.getFluidState(waterPos).getType() != Fluids.WATER) return false;
        if (!level.getBlockState(waterPos.above()).isAir()) return false;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos side = waterPos.relative(dir);
            var fluid = level.getFluidState(side);
            if (fluid.isEmpty()) {
                if (!level.getBlockState(side).isFaceSturdy(level, side, dir.getOpposite())) return false;
            } else if (!fluid.isSource() || fluid.getType() != Fluids.WATER) {
                return false;
            }
        }
        return true;
    }

    /** Any water source with open air above it, where spawn can float (a fallback when no pond is still). */
    public static boolean isOpenWaterSource(LevelReader level, BlockPos waterPos) {
        return level.getFluidState(waterPos).isSource() && level.getFluidState(waterPos).getType() == Fluids.WATER
                && level.getBlockState(waterPos.above()).isAir();
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, MIN_DELAY + level.random.nextInt(EXTRA_DELAY));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, false);
            return;
        }
        hatch(level, pos, state);
    }

    /** Lets two to four hatchlings out into the water. */
    public static void hatch(ServerLevel level, BlockPos pos, BlockState state) {
        level.playSound(null, pos, SoundEvents.FROGSPAWN_HATCH, SoundSource.BLOCKS, 0.5F, 1.0F);
        UUID father = ParentageBlockEntity.fatherAt(level, pos);
        level.removeBlock(pos, false);
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(MonsterGirlsMod.id(state.getValue(SPECIES).entityId()));
        int count = 2 + level.random.nextInt(3);
        for (int i = 0; i < count; i++) {
            if (!(type.create(level) instanceof AbstractAnuran young)) return;
            young.moveTo(pos.getX() + 0.5 + (level.random.nextDouble() - 0.5), pos.getY(), pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5),
                    level.random.nextFloat() * 360.0F, 0.0F);
            young.setTadpole();
            young.rollVariant(level.getBiome(pos)); // her colour is chosen for the pond she hatches in
            level.addFreshEntity(young);
            young.bornTo(father);
            if (father != null) young.setPersistenceRequired();
        }
    }
}
