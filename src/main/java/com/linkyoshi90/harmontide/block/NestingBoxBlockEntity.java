package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A nesting box's eggs: four stacks of whatever was laid in it (NestingBoxBlock). Hoppers take them out; nothing goes in but a
 * laid egg.
 */
public class NestingBoxBlockEntity extends BlockEntity {
    public static final int SLOTS = 4;

    private final SimpleContainer eggs = new SimpleContainer(SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            NestingBoxBlockEntity.this.changed();
        }
    };

    public NestingBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NESTING_BOX.get(), pos, state);
    }

    public SimpleContainer eggs() {
        return this.eggs;
    }

    private void changed() {
        this.setChanged();
        if (this.level == null || this.level.isClientSide) return;
        BlockState state = this.getBlockState();
        boolean full = !this.eggs.isEmpty();
        if (state.hasProperty(NestingBoxBlock.EGGS) && state.getValue(NestingBoxBlock.EGGS) != full) {
            this.level.setBlock(this.worldPosition, state.setValue(NestingBoxBlock.EGGS, full), 2);
        }
    }

    /** An egg laid into the box: what did not fit. */
    public ItemStack lay(ItemStack egg) {
        return this.eggs.addItem(egg);
    }

    /** Everything in it, taken out. */
    public java.util.List<ItemStack> takeAll() {
        java.util.List<ItemStack> out = this.eggs.removeAllItems();
        this.eggs.setChanged();
        return out;
    }

    /** What hoppers see: they can take eggs out, never put anything in. */
    public net.neoforged.neoforge.items.IItemHandler hopperHandler() {
        return new net.neoforged.neoforge.items.wrapper.InvWrapper(this.eggs) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return stack;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return false;
            }
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.eggs.getItems(), registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.eggs.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.eggs.getItems(), registries);
    }
}
