package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** The alraune asleep in her bloom: her saved entity data (tag "Girl"). */
public class AlrauneBloomBlockEntity extends BlockEntity {
    @Nullable
    private CompoundTag girl;

    public AlrauneBloomBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALRAUNE_BLOOM.get(), pos, state);
    }

    @Nullable
    public CompoundTag getGirl() {
        return this.girl;
    }

    public void setGirl(@Nullable CompoundTag girl) {
        this.girl = girl;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.girl != null) tag.put("Girl", this.girl);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.girl = tag.contains("Girl") ? tag.getCompound("Girl") : null;
    }
}
