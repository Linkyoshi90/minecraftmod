package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** Where the other end of this rabbit burrow comes up (block/RabbitBurrowBlock), or nothing while it is the first of a pair. */
public class RabbitBurrowBlockEntity extends BlockEntity {
    @Nullable
    private BlockPos link;

    public RabbitBurrowBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RABBIT_BURROW.get(), pos, state);
    }

    @Nullable
    public BlockPos link() {
        return this.link;
    }

    public void setLink(@Nullable BlockPos link) {
        this.link = link == null ? null : link.immutable();
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        if (this.link != null) tag.putLong("Link", this.link.asLong());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        this.link = tag.contains("Link") ? BlockPos.of(tag.getLong("Link")) : null;
    }
}
