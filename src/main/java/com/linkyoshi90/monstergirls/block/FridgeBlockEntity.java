package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Dairy;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A chest for food only (27 slots), and where the household eats: friends of whoever placed it come by now and then (FridgeGoal) and take
 * something, a food that flavours their milk if there is one (entity.Dairy). Nobody starves without it; it is a treat. Hoppers put food
 * in from any side and take it out from below, as with a chest.
 */
public class FridgeBlockEntity extends BlockEntity {
    public static final int SLOTS = 27, OPEN_TICKS = 30;

    private final SimpleContainer store = new SimpleContainer(SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            FridgeBlockEntity.this.setChanged();
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return isFood(stack);
        }

        @Override
        public void startOpen(Player player) {
            FridgeBlockEntity.this.viewers++;
            FridgeBlockEntity.this.door();
        }

        @Override
        public void stopOpen(Player player) {
            FridgeBlockEntity.this.viewers = Math.max(0, FridgeBlockEntity.this.viewers - 1);
            FridgeBlockEntity.this.door();
        }
    };
    @Nullable
    private UUID owner;
    private int viewers, openTicks;

    public FridgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FRIDGE.get(), pos, state);
    }

    public SimpleContainer store() {
        return this.store;
    }

    public static boolean isFood(ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    @Nullable
    public UUID owner() {
        return this.owner;
    }

    /** The door follows whoever has it open, player or girl. */
    private void door() {
        if (this.level == null || this.level.isClientSide) return;
        BlockState state = this.getBlockState();
        boolean open = this.viewers > 0 || this.openTicks > 0;
        if (!state.hasProperty(FridgeBlock.OPEN) || state.getValue(FridgeBlock.OPEN) == open) return;
        this.level.setBlock(this.worldPosition, state.setValue(FridgeBlock.OPEN, open), 3);
        this.level.playSound(null, this.worldPosition, open ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.5F, 1.3F);
    }

    /** Whether she may eat from it: a friend of its owner (or anyone's child, who eats with the family). */
    public boolean feeds(LivingEntity creature, ServerLevel level) {
        if (!(creature instanceof Bonded bonded) || this.owner == null || this.store.isEmpty()) return false;
        if (creature.isBaby()) return true;
        Player friend = level.getPlayerByUUID(this.owner);
        return friend != null && bonded.getAffection(friend) >= HomeLife.FRIEND;
    }

    /** She opens it and eats one thing: a food that flavours her milk if there is one, else any. What she ate, or empty. */
    public ItemStack serve(ServerLevel level, Mob creature) {
        List<Integer> flavours = new ArrayList<>(), any = new ArrayList<>();
        for (int i = 0; i < SLOTS; i++) {
            ItemStack s = this.store.getItem(i);
            if (!isFood(s)) continue;
            any.add(i);
            if (Dairy.flavourOf(level, s) != null) flavours.add(i);
        }
        List<Integer> pick = flavours.isEmpty() ? any : flavours;
        if (pick.isEmpty()) return ItemStack.EMPTY;
        int slot = pick.get(creature.getRandom().nextInt(pick.size()));
        ItemStack food = this.store.removeItem(slot, 1);
        this.openTicks = OPEN_TICKS;
        this.door();
        Dairy.ate(creature, food);
        creature.heal(4.0F);
        creature.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, food), creature.getX(), creature.getEyeY() - 0.2, creature.getZ(), 6, 0.15, 0.1, 0.15, 0.05);
        if (this.owner != null) Dairy.award(level.getPlayerByUUID(this.owner), "fridge");
        return food;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FridgeBlockEntity fridge) {
        if (fridge.openTicks > 0 && --fridge.openTicks == 0) fridge.door();
    }

    /** The nearest fridge within range that would feed her, or null. */
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
                if (!(be instanceof FridgeBlockEntity fridge)) continue;
                double d = be.getBlockPos().distToCenterSqr(creature.position());
                if (d <= closest && fridge.feeds(creature, level)) {
                    closest = d;
                    best = be.getBlockPos();
                }
            }
        }
        return best;
    }

    /** What hoppers see: food in from anywhere, anything out. */
    public net.neoforged.neoforge.items.IItemHandler hopperHandler() {
        return new net.neoforged.neoforge.items.wrapper.InvWrapper(this.store) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return isFood(stack) ? super.insertItem(slot, stack, simulate) : stack;
            }
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.store.getItems(), registries);
        if (this.owner != null) tag.putUUID("Owner", this.owner);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.store.getItems().replaceAll(s -> ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.store.getItems(), registries);
        this.owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
