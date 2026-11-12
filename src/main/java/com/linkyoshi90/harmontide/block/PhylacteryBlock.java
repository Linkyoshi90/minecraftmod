package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.girl.Undead;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A lich's phylactery: the vessel of her soul. While it stands she cannot die (she reforms beside it); however it is
 * removed (broken, blown up), she is freed and becomes peaceful. Placed by the lich herself, never crafted.
 */
public class PhylacteryBlock extends Block {
    public static final MapCodec<PhylacteryBlock> CODEC = simpleCodec(PhylacteryBlock::new);
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0);

    public PhylacteryBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) Undead.phylacteryBroken(server, pos);
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SOUL, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.8, pos.getZ() + 0.3 + random.nextDouble() * 0.4,
                    0.0, 0.03, 0.0);
        }
    }
}
