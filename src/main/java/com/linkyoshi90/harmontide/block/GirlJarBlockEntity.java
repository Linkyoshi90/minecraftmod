package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** Who is in the jar: her species (tag "Species") and her saved entity data (tag "Girl"). */
public class GirlJarBlockEntity extends BlockEntity {
    private GirlSpecies species = GirlSpecies.FIREFLY_BUG;
    @Nullable
    private CompoundTag girl;

    public GirlJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GIRL_JAR.get(), pos, state);
        if (state.getValue(GirlJarBlock.KIND) == GirlJarBlock.Kind.FAIRY) this.species = GirlSpecies.FAIRY_BUG;
    }

    public GirlSpecies getSpecies() {
        return this.species;
    }

    @Nullable
    public CompoundTag getGirl() {
        return this.girl;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Species", this.species.id());
        if (this.girl != null) tag.put("Girl", this.girl);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        GirlSpecies s = tag.contains("Species") ? GirlSpecies.byId(tag.getString("Species")) : null;
        if (s != null) this.species = s;
        this.girl = tag.contains("Girl") ? tag.getCompound("Girl") : null;
    }
}
