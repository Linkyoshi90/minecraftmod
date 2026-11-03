package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.entity.Dairy;
import com.linkyoshi90.monstergirls.registry.ModBlockEntities;
import com.linkyoshi90.monstergirls.registry.ModComponents;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Four cheeses on a little shelf. A bucket of milk set on it becomes a soft cheese of that milk (the bucket kept for the hoppers), and
 * underground (no sky above, and dark) every cheese on it ages: firm after a day, hard after three, stronger each time (entity.Dairy).
 * Hoppers put milk in and take buckets and cheese out; the button on its side (CheeseShelfBlock.PULL_HARD) keeps soft ones on the shelf.
 */
public class CheeseShelfBlockEntity extends BlockEntity {
    public static final int CHEESES = 4, AGE_STEP = 100, MAX_SKY = 7;

    private final SimpleContainer store = new SimpleContainer(CHEESES + 1) {
        @Override
        public void setChanged() {
            super.setChanged();
            CheeseShelfBlockEntity.this.changed();
        }
    };

    public CheeseShelfBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEESE_SHELF.get(), pos, state);
    }

    public SimpleContainer store() {
        return this.store;
    }

    private void changed() {
        this.setChanged();
        if (this.level == null || this.level.isClientSide) return;
        BlockState state = this.getBlockState();
        int count = this.count();
        if (state.hasProperty(CheeseShelfBlock.CHEESES) && state.getValue(CheeseShelfBlock.CHEESES) != count) {
            this.level.setBlock(this.worldPosition, state.setValue(CheeseShelfBlock.CHEESES, count), 2);
        }
    }

    public int count() {
        int n = 0;
        for (int i = 0; i < CHEESES; i++) if (!this.store.getItem(i).isEmpty()) n++;
        return n;
    }

    /** Below ground or in a dark cellar: where cheese ripens. */
    public static boolean underground(Level level, BlockPos pos) {
        return !level.canSeeSky(pos) && level.getBrightness(LightLayer.SKY, pos) <= MAX_SKY;
    }

    /** A bucket of milk set down: a soft cheese of it. True if there was room. */
    public boolean addMilk(ItemStack milk) {
        if (!Dairy.isMilk(milk) || this.store.getItem(CHEESES).getCount() >= 16) return false;
        for (int i = 0; i < CHEESES; i++) {
            if (!this.store.getItem(i).isEmpty()) continue;
            ItemStack cheese = Dairy.madeFrom(milk, ModItems.CHEESE.get(), 1);
            cheese.set(ModComponents.CHEESE_AGE.get(), 0);
            this.store.setItem(i, cheese);
            return true;
        }
        return false;
    }

    /** The empty buckets left from the milk. */
    public ItemStack takeBuckets() {
        ItemStack out = this.store.removeItemNoUpdate(CHEESES);
        this.store.setChanged();
        return out;
    }

    /** Adds the bucket the milk came in, for addMilk's callers that keep it here (hoppers). */
    void keepBucket() {
        ItemStack held = this.store.getItem(CHEESES);
        if (held.isEmpty()) this.store.setItem(CHEESES, new ItemStack(Items.BUCKET));
        else held.grow(1);
        this.store.setChanged();
    }

    /** The ripest cheese on the shelf, taken off it; empty if there is none. */
    public ItemStack takeRipest() {
        int best = -1;
        for (int i = 0; i < CHEESES; i++) {
            ItemStack c = this.store.getItem(i);
            if (!c.isEmpty() && (best < 0 || Dairy.cheeseAge(c) > Dairy.cheeseAge(this.store.getItem(best)))) best = i;
        }
        if (best < 0) return ItemStack.EMPTY;
        ItemStack out = this.store.removeItemNoUpdate(best);
        this.store.setChanged();
        return out;
    }

    /** Ages every cheese by this many ticks. */
    public void age(int ticks) {
        for (int i = 0; i < CHEESES; i++) {
            ItemStack c = this.store.getItem(i);
            if (!c.isEmpty()) c.set(ModComponents.CHEESE_AGE.get(), Dairy.cheeseAge(c) + ticks);
        }
        this.setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CheeseShelfBlockEntity shelf) {
        if (level.getGameTime() % AGE_STEP == 0 && shelf.count() > 0 && underground(level, pos)) shelf.age(AGE_STEP);
    }

    /** What hoppers see: milk goes in (and becomes cheese), buckets come out, and cheese does too unless the button says only hard. */
    public net.neoforged.neoforge.items.IItemHandler hopperHandler() {
        return new net.neoforged.neoforge.items.wrapper.InvWrapper(this.store) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (slot >= CHEESES || !Dairy.isMilk(stack) || !this.getStackInSlot(slot).isEmpty() || this.getStackInSlot(CHEESES).getCount() >= 16) return stack;
                if (!simulate) {
                    ItemStack cheese = Dairy.madeFrom(stack, ModItems.CHEESE.get(), 1);
                    cheese.set(ModComponents.CHEESE_AGE.get(), 0);
                    CheeseShelfBlockEntity.this.store.setItem(slot, cheese);
                    CheeseShelfBlockEntity.this.keepBucket();
                }
                return stack.copyWithCount(stack.getCount() - 1);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (slot < CHEESES && CheeseShelfBlockEntity.this.getBlockState().getValue(CheeseShelfBlock.PULL_HARD)
                        && Dairy.hardness(this.getStackInSlot(slot)) < 2) return ItemStack.EMPTY;
                return super.extractItem(slot, amount, simulate);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return slot < CHEESES && Dairy.isMilk(stack);
            }
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.store.getItems(), registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.store.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.store.getItems(), registries);
    }
}
