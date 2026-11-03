package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.entity.Dairy;
import com.linkyoshi90.monstergirls.registry.ModBlockEntities;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Up to four buckets of one milk wait in the churn; every CHURNS strokes (a friend's hand, or a redstone pulse) turn the first of them
 * into BUTTER_PER_BUCKET butter, and the bucket is set aside empty. Hoppers put milk in and take the butter and buckets out.
 */
public class ButterChurnBlockEntity extends BlockEntity {
    public static final int MILK = 0, BUTTER = 1, BUCKETS = 2, MAX_MILK = 4, CHURNS = 8, BUTTER_PER_BUCKET = 2;
    /** How long the dasher takes to come back up. */
    public static final int STROKE_TICKS = 6;

    private final SimpleContainer store = new SimpleContainer(3) {
        @Override
        public void setChanged() {
            super.setChanged();
            ButterChurnBlockEntity.this.setChanged();
        }
    };
    private int strokes, down;

    public ButterChurnBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BUTTER_CHURN.get(), pos, state);
    }

    public SimpleContainer store() {
        return this.store;
    }

    public int strokes() {
        return this.strokes;
    }

    /** One bucket of milk poured in. True if it took it (the same milk as what's there, and room). */
    public boolean pour(ItemStack milk) {
        ItemStack held = this.store.getItem(MILK);
        if (!Dairy.isMilk(milk) || held.getCount() >= MAX_MILK || !held.isEmpty() && !ItemStack.isSameItemSameComponents(held, milk)) return false;
        if (held.isEmpty()) this.store.setItem(MILK, milk.copyWithCount(1));
        else held.grow(1);
        this.store.setChanged();
        return true;
    }

    /** One stroke of the dasher. True if there was milk to churn. */
    public boolean churn(Level level, BlockPos pos) {
        ItemStack milk = this.store.getItem(MILK);
        if (milk.isEmpty() || this.down > 0) return false;
        ItemStack butter = this.store.getItem(BUTTER), made = Dairy.madeFrom(milk, ModItems.BUTTER.get(), BUTTER_PER_BUCKET);
        if (!butter.isEmpty() && (!ItemStack.isSameItemSameComponents(butter, made) || butter.getCount() + BUTTER_PER_BUCKET > butter.getMaxStackSize())) return false;
        if (this.store.getItem(BUCKETS).getCount() >= 16) return false;
        this.down = STROKE_TICKS;
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.35F, 0.6F + level.random.nextFloat() * 0.2F);
        if (level.getBlockState(pos).hasProperty(ButterChurnBlock.STROKE)) level.setBlock(pos, level.getBlockState(pos).setValue(ButterChurnBlock.STROKE, true), 2);
        if (++this.strokes >= CHURNS) {
            this.strokes = 0;
            milk.shrink(1);
            if (butter.isEmpty()) this.store.setItem(BUTTER, made);
            else butter.grow(BUTTER_PER_BUCKET);
            ItemStack buckets = this.store.getItem(BUCKETS);
            if (buckets.isEmpty()) this.store.setItem(BUCKETS, new ItemStack(Items.BUCKET));
            else buckets.grow(1);
            level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.6F, 1.2F);
        }
        this.store.setChanged();
        return true;
    }

    /** What hoppers see: milk goes in, butter and empty buckets come out. */
    public net.neoforged.neoforge.items.IItemHandler hopperHandler() {
        return new net.neoforged.neoforge.items.wrapper.InvWrapper(this.store) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (slot != MILK || stack.isEmpty()) return stack;
                ItemStack held = this.getStackInSlot(MILK);
                if (!Dairy.isMilk(stack) || held.getCount() >= MAX_MILK || !held.isEmpty() && !ItemStack.isSameItemSameComponents(held, stack)) return stack;
                if (!simulate) ButterChurnBlockEntity.this.pour(stack);
                return stack.copyWithCount(stack.getCount() - 1);
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return slot == MILK ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
            }
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ButterChurnBlockEntity churn) {
        if (churn.down > 0 && --churn.down == 0 && state.getValue(ButterChurnBlock.STROKE)) level.setBlock(pos, state.setValue(ButterChurnBlock.STROKE, false), 2);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        net.minecraft.world.ContainerHelper.saveAllItems(tag, this.store.getItems(), registries);   // by slot: milk, butter, buckets
        tag.putInt("Strokes", this.strokes);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.store.getItems().replaceAll(s -> ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(tag, this.store.getItems(), registries);
        this.strokes = tag.getInt("Strokes");
    }
}
