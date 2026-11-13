package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Whose footprints these are (her species' id) and when they were made (block/FootprintBlock, entity/Signs). */
public class FootprintBlockEntity extends BlockEntity {
    private String species = "";
    private long made;

    public FootprintBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOOTPRINT.get(), pos, state);
    }

    public String species() {
        return this.species;
    }

    public long made() {
        return this.made;
    }

    public void set(String species, long made) {
        this.species = species;
        this.made = made;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        tag.putString("Species", this.species);
        tag.putLong("Made", this.made);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        this.species = tag.getString("Species");
        this.made = tag.getLong("Made");
    }
}
