package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * Holds up to CAPACITY buckets of one milk (the same family and flavours), shown rising behind its glass (MilkTankBlock.LEVEL), and up to
 * 16 empty buckets to fill. Hoppers from above or the sides put in milk (its bucket stays in the tank) and empty buckets; a hopper
 * below takes milk out, filling the tank's own empty buckets, and gets nothing while it has none.
 */
public class MilkTankBlockEntity extends BlockEntity {
    public static final int CAPACITY = 32, MAX_BUCKETS = 16;

    private ItemStack kind = ItemStack.EMPTY;
    private int amount, buckets;

    public MilkTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MILK_TANK.get(), pos, state);
    }

    public int amount() {
        return this.amount;
    }

    public int buckets() {
        return this.buckets;
    }

    /** One bucket of what it holds, or empty. */
    public ItemStack kind() {
        return this.kind;
    }

    private void changed() {
        if (this.amount == 0) this.kind = ItemStack.EMPTY;
        this.setChanged();
        if (this.level == null || this.level.isClientSide) return;
        BlockState state = this.getBlockState();
        int level = this.amount == 0 ? 0 : Math.max(1, Math.round(this.amount * 8.0F / CAPACITY));
        if (state.hasProperty(MilkTankBlock.LEVEL) && state.getValue(MilkTankBlock.LEVEL) != level) this.level.setBlock(this.worldPosition, state.setValue(MilkTankBlock.LEVEL, level), 3);
    }

    public boolean accepts(ItemStack milk) {
        return Dairy.isMilk(milk) && this.amount < CAPACITY && (this.amount == 0 || ItemStack.isSameItemSameComponents(this.kind, milk));
    }

    /** A bucket of milk poured in. True if it took it; the empty bucket is the caller's. */
    public boolean pour(ItemStack milk, @Nullable Player who) {
        if (!this.accepts(milk)) return false;
        if (this.amount == 0) this.kind = milk.copyWithCount(1);
        this.amount++;
        this.changed();
        if (this.amount == CAPACITY) {
            if (who == null && this.level != null) who = this.level.getNearestPlayer(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, 8, false);
            Dairy.award(who, "full_tank");
        }
        return true;
    }

    /** A bucket filled from it, or empty if it holds no milk. */
    public ItemStack draw() {
        if (this.amount == 0) return ItemStack.EMPTY;
        ItemStack out = this.kind.copyWithCount(1);
        this.amount--;
        this.changed();
        return out;
    }

    public int comparator() {
        return this.amount == 0 ? 0 : 1 + this.amount * 14 / CAPACITY;
    }

    /** Below: milk in its own buckets. Above and the sides: milk and empty buckets in. */
    public IItemHandler hopperHandler(@Nullable Direction side) {
        return side == Direction.DOWN ? new Tap() : new Inlet();
    }

    /** The one slot a hopper below sees: as many filled buckets as there are milk and empty buckets for. */
    private class Tap implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            MilkTankBlockEntity t = MilkTankBlockEntity.this;
            int n = Math.min(Math.min(t.amount, t.buckets), 16);
            return n == 0 ? ItemStack.EMPTY : t.kind.copyWithCount(n);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int count, boolean simulate) {
            MilkTankBlockEntity t = MilkTankBlockEntity.this;
            int n = Math.min(count, Math.min(t.amount, t.buckets));
            if (n <= 0) return ItemStack.EMPTY;
            ItemStack out = t.kind.copyWithCount(n);
            if (!simulate) {
                t.amount -= n;
                t.buckets -= n;
                t.changed();
            }
            return out;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 16;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    }

    /** Slot 0 takes empty buckets, slot 1 milk. Nothing comes out this way. */
    private class Inlet implements IItemHandler {
        @Override
        public int getSlots() {
            return 2;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            MilkTankBlockEntity t = MilkTankBlockEntity.this;
            return slot == 0 && t.buckets > 0 ? new ItemStack(Items.BUCKET, t.buckets) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            MilkTankBlockEntity t = MilkTankBlockEntity.this;
            if (stack.isEmpty()) return stack;
            if (slot == 0) {
                if (!stack.is(Items.BUCKET)) return stack;
                int n = Math.min(stack.getCount(), MAX_BUCKETS - t.buckets);
                if (n <= 0) return stack;
                if (!simulate) {
                    t.buckets += n;
                    t.setChanged();
                }
                return stack.copyWithCount(stack.getCount() - n);
            }
            if (!t.accepts(stack) || t.buckets >= MAX_BUCKETS) return stack;   // its bucket has to fit too
            if (!simulate) {
                t.pour(stack, null);
                t.buckets++;
            }
            return stack.copyWithCount(stack.getCount() - 1);
        }

        @Override
        public ItemStack extractItem(int slot, int count, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? MAX_BUCKETS : 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 ? stack.is(Items.BUCKET) : Dairy.isMilk(stack);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.kind.isEmpty()) tag.put("Kind", this.kind.save(registries));
        tag.putInt("Amount", this.amount);
        tag.putInt("Buckets", this.buckets);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.kind = tag.contains("Kind") ? ItemStack.parseOptional(registries, tag.getCompound("Kind")) : ItemStack.EMPTY;
        this.amount = tag.getInt("Amount");
        this.buckets = tag.getInt("Buckets");
    }
}
