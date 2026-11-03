package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.GiftGiver;
import com.linkyoshi90.monstergirls.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.UUID;

/** Five slots of presents. Each close friend (25+) of whoever placed it within 16 adds one a day, while its owner is online. */
public class GiftBoxBlockEntity extends BlockEntity {
    public static final int SLOTS = 5, RADIUS = 16;
    private static final long DAY = 24000L;
    private final SimpleContainer store = new SimpleContainer(SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            GiftBoxBlockEntity.this.setChanged();
        }
    };
    @Nullable
    private UUID owner;

    public GiftBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GIFT_BOX.get(), pos, state);
    }

    public SimpleContainer store() {
        return this.store;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GiftBoxBlockEntity box) {
        if (level instanceof ServerLevel server && server.getGameTime() % 200 == 0) box.collect(server, pos);
    }

    /** How many presents came in. */
    public int collect(ServerLevel level, BlockPos pos) {
        Player owner = this.owner == null ? null : level.getPlayerByUUID(this.owner);
        if (owner == null) return 0;
        int added = 0;
        for (Mob m : level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(RADIUS), Mob::isAlive)) {
            if (!(m instanceof Bonded b) || !(m instanceof GiftGiver giver) || m.isBaby() || b.getAffection(owner) < Bonded.GIFT_AFFECTION) continue;
            if (level.getGameTime() < m.getPersistentData().getLong("NextBoxGift")) continue;
            ItemStack gift = giver.rollGift(owner);
            if (gift.isEmpty() || !this.store.canAddItem(gift)) continue;
            m.getPersistentData().putLong("NextBoxGift", level.getGameTime() + DAY);
            this.store.addItem(gift);
            added++;
        }
        return added;
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
