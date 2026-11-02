package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.Minotaur;
import com.linkyoshi90.monstergirls.item.FamilyMilk;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import com.linkyoshi90.monstergirls.registry.ModBlockEntities;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Fills the empty buckets in its store from a creature standing by it. She has to be a grown friend of whoever placed
 * it (fond, the same as taking orders) and full size, and either expecting or a minotaur cow. Each one gives a bucket
 * every five minutes, or sooner for a cow whose bell has rung. Those with milk come to it on their own (SeekMilkingMachineGoal), and hoppers
 * can feed it empty buckets and carry off the milk (MilkingMachineBlockEntity.hopperHandler).
 */
public class MilkingMachineBlockEntity extends BlockEntity {
    public static final int SLOTS = 5;
    public static final int RADIUS = 2;
    public static final int INTERVAL = 40;
    /** Small grown species stay out of it, as the young do. */
    public static final float MIN_HEIGHT = 1.25F;
    /** How long it pumps for each bucket, and how long each stroke takes. */
    public static final int PUMP_TICKS = 80, STROKE = 7;

    private final SimpleContainer store = new SimpleContainer(SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            MilkingMachineBlockEntity.this.setChanged();
        }
    };
    @Nullable
    private UUID owner;
    private int pumpTicks;

    public MilkingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MILKING_MACHINE.get(), pos, state);
    }

    public SimpleContainer store() {
        return this.store;
    }

    /** What hoppers and pipes see: empty buckets go in, and only milk comes out. */
    public net.neoforged.neoforge.items.IItemHandler hopperHandler() {
        return new net.neoforged.neoforge.items.wrapper.InvWrapper(this.store) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return stack.is(Items.BUCKET) ? super.insertItem(slot, stack, simulate) : stack;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return this.getStackInSlot(slot).is(Items.BUCKET) ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
            }
        };
    }

    /** Whether this machine would milk her now: she's fit for it, and it has a bucket and room. */
    public boolean wants(LivingEntity creature, ServerLevel level) {
        return this.owner != null && this.freeBucketSlot() >= 0 && this.canMilk(creature, level);
    }

    /** The nearest machine within range that would milk her, or null. */
    @Nullable
    public static BlockPos nearestFor(ServerLevel level, LivingEntity creature, int range) {
        net.minecraft.world.level.ChunkPos here = creature.chunkPosition();
        int chunks = (range >> 4) + 1;
        BlockPos best = null;
        double closest = range * range;
        for (int x = here.x - chunks; x <= here.x + chunks; x++) for (int z = here.z - chunks; z <= here.z + chunks; z++) {
            net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
            if (chunk == null) continue;
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (!(be instanceof MilkingMachineBlockEntity machine)) continue;
                double d = be.getBlockPos().distToCenterSqr(creature.position());
                if (d <= closest && machine.wants(creature, level)) {
                    closest = d;
                    best = be.getBlockPos();
                }
            }
        }
        return best;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MilkingMachineBlockEntity machine) {
        if (level.getGameTime() % INTERVAL == 0 && level instanceof ServerLevel server) {
            boolean harness = com.linkyoshi90.monstergirls.MonsterGirlsConfig.milkHarness();
            if (state.getValue(MilkingMachineBlock.HARNESS) != harness) level.setBlock(pos, state.setValue(MilkingMachineBlock.HARNESS, harness), 2);
            machine.work(server, pos);
        }
        if (machine.pumpTicks > 0 && level instanceof ServerLevel) machine.pump(level, pos, level.getBlockState(pos));
    }

    /** The plunger goes up and down (a second model, swapped by block state). */
    private void pump(Level level, BlockPos pos, BlockState state) {
        boolean stroke = --this.pumpTicks > 0 && (this.pumpTicks / STROKE) % 2 == 0;
        if (state.getValue(MilkingMachineBlock.STROKE) == stroke) return;
        level.setBlock(pos, state.setValue(MilkingMachineBlock.STROKE, stroke), 2);
        if (stroke) level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.12F, 1.4F);
    }

    public boolean isPumping() {
        return this.pumpTicks > 0;
    }

    private void work(ServerLevel level, BlockPos pos) {
        if (this.owner == null) return;
        int bucket = this.freeBucketSlot();
        if (bucket < 0) return;
        for (LivingEntity creature : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(RADIUS), e -> this.canMilk(e, level))) {
            FamilyMilk milk = FamilyMilk.of(creature);
            this.store.getItem(bucket).shrink(1);
            this.put(new ItemStack(ModItems.MILK.get(milk).get()));
            markMilked(creature, level.getGameTime() + Minotaur.MILK_COOLDOWN);
            if (creature instanceof Minotaur cow) cow.milkedByMachine();
            if (creature instanceof com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl girl) girl.blushFor(Minotaur.MACHINE_MILK_TICKS);
            this.pumpTicks = PUMP_TICKS;
            level.playSound(null, pos, SoundEvents.COW_MILK, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }
    }

    /** A slot of empty buckets the filled one has room to go to: its own last bucket, or any bucket with a free slot. */
    private int freeBucketSlot() {
        boolean room = false;
        int bucket = -1;
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = this.store.getItem(i);
            if (stack.isEmpty()) room = true;
            else if (stack.is(Items.BUCKET)) {
                bucket = i;
                if (stack.getCount() == 1) room = true;
            }
        }
        return room ? bucket : -1;
    }

    private void put(ItemStack milk) {
        for (int i = 0; i < SLOTS; i++) {
            ItemStack held = this.store.getItem(i);
            if (ItemStack.isSameItemSameComponents(held, milk) && held.getCount() < held.getMaxStackSize()) {
                held.grow(1);
                this.store.setChanged();
                return;
            }
        }
        for (int i = 0; i < SLOTS; i++) {
            if (this.store.getItem(i).isEmpty()) {
                this.store.setItem(i, milk);
                return;
            }
        }
    }

    private boolean canMilk(LivingEntity creature, ServerLevel level) {
        if (!(creature instanceof Bonded bonded) || creature.isBaby() || creature.getBbHeight() < MIN_HEIGHT) return false;
        if (FamilyMilk.of(creature) == null || !isReady(creature, level.getGameTime())) return false;
        net.minecraft.world.entity.player.Player friend = level.getPlayerByUUID(this.owner);
        if (friend == null || bonded.getAffection(friend) < Orders.AFFECTION) return false;
        return bonded.getGestation().isCarrying() || creature instanceof Minotaur cow && !cow.isMale();
    }

    public static boolean isReady(LivingEntity creature, long now) {
        return creature instanceof Minotaur cow ? cow.isMilkReady() : now >= creature.getData(ModAttachments.NEXT_MILK);
    }

    private static void markMilked(LivingEntity creature, long next) {
        if (creature instanceof Minotaur cow) cow.setNextMilk(next);
        else creature.setData(ModAttachments.NEXT_MILK, next);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Store", this.store.createTag(registries));
        if (this.owner != null) tag.putUUID("Owner", this.owner);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.store.fromTag(tag.getList("Store", 10), registries);
        this.owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
