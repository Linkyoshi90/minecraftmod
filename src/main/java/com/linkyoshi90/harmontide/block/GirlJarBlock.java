package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModGirls;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * A firefly (or a fairy) caught in a glass bottle: a little jar that lights up the room. Open it (right-click) or break it
 * and she flies out again, everything she was kept in the block entity, and the bottle is left behind.
 */
public class GirlJarBlock extends Block implements EntityBlock {
    public enum Kind implements StringRepresentable {
        FIREFLY("firefly"), FAIRY("fairy");

        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final EnumProperty<Kind> KIND = EnumProperty.create("kind", Kind.class);
    public static final MapCodec<GirlJarBlock> CODEC = simpleCodec(GirlJarBlock::new);
    private static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);

    public GirlJarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(KIND, Kind.FIREFLY));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(KIND);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GirlJarBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) level.removeBlock(pos, false);         // onRemove lets her out
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** However the jar goes, she flies out and the empty bottle is left. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof GirlJarBlockEntity jar) {
            release(server, pos, jar.getSpecies(), jar.getGirl());
            server.addFreshEntity(new ItemEntity(server, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, new ItemStack(Items.GLASS_BOTTLE)));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static void release(ServerLevel level, BlockPos pos, GirlSpecies species, @Nullable CompoundTag girl) {
        Entity e = ModGirls.type(species).create(level);
        if (e == null) return;
        if (girl != null) e.load(girl);
        e.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(e);
        level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.02);
    }
}
