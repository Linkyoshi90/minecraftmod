package com.linkyoshi90.harmontide.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * A rabbit burrow a jackalope dug (entity/Traversal#digBurrow). Two of them dug one after the other are linked: crouch on one and
 * you come up out of the other (RabbitBurrowBlockEntity keeps where the other end is).
 */
public class RabbitBurrowBlock extends BaseEntityBlock {
    public static final MapCodec<RabbitBurrowBlock> CODEC = simpleCodec(RabbitBurrowBlock::new);

    public RabbitBurrowBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RabbitBurrowBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
