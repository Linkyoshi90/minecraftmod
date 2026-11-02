package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** A straw-wrapped post. Girls who stand near it with a friend about practise on it (SparringPostBlockEntity, HomeLife.train). */
public class SparringPostBlock extends SimpleTickerBlock {
    public static final MapCodec<SparringPostBlock> CODEC = simpleCodec(SparringPostBlock::new);

    public SparringPostBlock(Properties properties) {
        super(properties, Block.box(4, 0, 4, 12, 16, 12));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SparringPostBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.SPARRING_POST.get(), SparringPostBlockEntity::serverTick);
    }
}
