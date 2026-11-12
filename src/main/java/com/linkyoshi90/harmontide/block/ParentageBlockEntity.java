package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Remembers who fathered a nest of harpy eggs or a film of frog spawn, so that whatever hatches from it is devoted
 * to that player from the start. Eggs a player picks up and puts down again forget it (they hatch wild).
 */
public class ParentageBlockEntity extends BlockEntity {
    @Nullable
    private UUID father;

    public ParentageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PARENTAGE.get(), pos, state);
    }

    @Nullable
    public UUID getFather() {
        return this.father;
    }

    public void setFather(@Nullable UUID father) {
        this.father = father;
        this.setChanged();
    }

    /** The father recorded at a position, or null. */
    @Nullable
    public static UUID fatherAt(LevelAccessor level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ParentageBlockEntity be ? be.getFather() : null;
    }

    public static void setFatherAt(LevelAccessor level, BlockPos pos, @Nullable UUID father) {
        if (father != null && level.getBlockEntity(pos) instanceof ParentageBlockEntity be) be.setFather(father);
    }

    /** The mother's Harmontide paint, which the young inherit (0: none). */
    private int paint;

    public static int paintAt(LevelAccessor level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ParentageBlockEntity be ? be.paint : 0;
    }

    public static void setPaintAt(LevelAccessor level, BlockPos pos, int paint) {
        if (paint > 0 && level.getBlockEntity(pos) instanceof ParentageBlockEntity be) {
            be.paint = paint;
            be.setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.father != null) tag.putUUID("Father", this.father);
        if (this.paint > 0) tag.putInt("Paint", this.paint);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.father = tag.hasUUID("Father") ? tag.getUUID("Father") : null;
        this.paint = tag.getInt("Paint");
    }
}
