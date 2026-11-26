package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.girl.Lamia;
import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.UUID;

/** What a {@link GirlEggBlock} clutch will hatch into: the species id, how many, who fathered them, and how far along they are. */
public class GirlEggBlockEntity extends BlockEntity {
    @Nullable
    private String species;
    private int count = 1;
    @Nullable
    private UUID father;
    private int cracks;

    public GirlEggBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GIRL_EGG.get(), pos, state);
    }

    private int paint;

    public GirlEggBlockEntity setup(String species, int count, @Nullable UUID father) {
        this.species = species;
        this.count = count;
        this.father = father;
        this.setChanged();
        return this;
    }

    /** The mother's Harmontide paint, which the young inherit. */
    public void withPaint(int paint) {
        this.paint = paint;
        this.setChanged();
    }

    public int getPaint() {
        return this.paint;
    }

    @Nullable
    public String getSpecies() {
        return this.species;
    }

    public int getCount() {
        return this.count;
    }

    /** One more egg has been pushed out. The clutch block is still the one block; the count is what hatches. */
    public void setCount(int count) {
        this.count = Math.max(1, count);
        this.setChanged();
    }

    @Nullable
    public UUID getFather() {
        return this.father;
    }

    public int crack() {
        this.cracks++;
        this.setChanged();
        return this.cracks;
    }

    /** A python lies coiled around the clutch. */
    public boolean isBrooded(ServerLevel level) {
        return !level.getEntitiesOfClass(Lamia.class, new AABB(this.worldPosition).inflate(2.0), Lamia::isBrooding).isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.species != null) tag.putString("Species", this.species);
        tag.putInt("Count", this.count);
        if (this.father != null) tag.putUUID("Father", this.father);
        tag.putInt("Cracks", this.cracks);
        tag.putInt("Paint", this.paint);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.species = tag.contains("Species") ? tag.getString("Species") : null;
        this.count = tag.getInt("Count");
        this.father = tag.hasUUID("Father") ? tag.getUUID("Father") : null;
        this.cracks = tag.getInt("Cracks");
        this.paint = tag.getInt("Paint");
    }
}
