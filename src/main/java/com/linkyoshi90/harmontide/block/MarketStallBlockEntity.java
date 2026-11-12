package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Hamlet;
import com.linkyoshi90.harmontide.entity.girl.Roles;
import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A village's market stall (2026-11-08, hamlet trades). Within {@link #NEAR} blocks of a hearth that has grown into a village
 * (Hamlet.VILLAGE residents), every morning the passing trade buys half of what's laid out on it, {@link #PER} items to the
 * emerald (at least one a stack), and then each resident lays out something new from her calling: a farmhand her crops, a
 * cook her baking, a guard arrows, anyone else the kind of present she'd bring her friend. Collect the emeralds from it.
 */
public class MarketStallBlockEntity extends BlockEntity {
    public static final int SLOTS = 9, NEAR = 16, PER = 4, RESIDENTS_REACH = 48;
    private final SimpleContainer store = new SimpleContainer(SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            MarketStallBlockEntity.this.setChanged();
        }
    };
    private long lastDay;

    public MarketStallBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MARKET_STALL.get(), pos, state);
    }

    public SimpleContainer store() {
        return this.store;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MarketStallBlockEntity stall) {
        if (!(level instanceof ServerLevel server) || server.getGameTime() % 200 != 0 || server.getDayTime() % 24000L >= 2000L) return;
        long day = server.getDayTime() / 24000L;
        if (stall.lastDay == day + 1) return;
        stall.lastDay = day + 1;
        stall.setChanged();
        stall.morning(server, pos);
    }

    /** The village this stall serves, or null (no hearth near, or not yet a village). */
    @Nullable
    public static HearthBlockEntity village(ServerLevel level, BlockPos pos) {
        for (BlockPos h : java.util.List.copyOf(HearthBlockEntity.loaded(level))) {
            if (h.closerThan(pos, NEAR) && level.getBlockEntity(h) instanceof HearthBlockEntity hearth && Hamlet.tier(hearth.residentCount()) == Hamlet.Tier.VILLAGE) return hearth;
        }
        return null;
    }

    /** Market morning: yesterday's goods sell, then the residents lay out new ones. Emeralds earned. */
    public int morning(ServerLevel level, BlockPos pos) {
        HearthBlockEntity hearth = village(level, pos);
        if (hearth == null) return 0;
        int earned = this.sell();
        Player owner = hearth.owner() == null ? null : level.getPlayerByUUID(hearth.owner());
        this.stock(level, hearth, owner);
        if (earned > 0) {
            level.playSound(null, pos, SoundEvents.VILLAGER_YES, SoundSource.BLOCKS, 0.8F, 1.0F);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 8, 0.4, 0.2, 0.4, 0.0);
            if (owner != null) Dairy.award(owner, "market_stall");
        }
        return earned;
    }

    /** Half the goods (every other stack, rounding up) go to the passing trade. Emeralds earned. */
    public int sell() {
        int earned = 0, goods = 0;
        for (int i = 0; i < SLOTS; i++) {
            ItemStack s = this.store.getItem(i);
            if (s.isEmpty() || s.is(Items.EMERALD)) continue;
            if (goods++ % 2 != 0) continue;
            earned += Math.max(1, s.getCount() / PER);
            this.store.setItem(i, ItemStack.EMPTY);
        }
        int left = earned;
        while (left > 0) {
            ItemStack em = new ItemStack(Items.EMERALD, Math.min(64, left));
            if (!this.store.canAddItem(em)) break;
            this.store.addItem(em);
            left -= em.getCount();
        }
        return earned - left;
    }

    /** What a resident lays out: her calling's goods, else her kind of present. */
    public static ItemStack goods(AbstractMonsterGirl girl, @Nullable Player owner) {
        Roles.Role role = Roles.of(girl);
        int r = girl.getRandom().nextInt(3);
        if (role == Roles.Role.FARMHAND) return new ItemStack(r == 0 ? Items.WHEAT : r == 1 ? Items.CARROT : Items.POTATO, 6);
        if (role == Roles.Role.COOK) return new ItemStack(r == 0 ? Items.BREAD : r == 1 ? Items.BAKED_POTATO : Items.PUMPKIN_PIE, 3);
        if (role == Roles.Role.GUARD) return r == 0 ? new ItemStack(Items.IRON_NUGGET, 8) : new ItemStack(Items.ARROW, 8);
        return owner == null ? ItemStack.EMPTY : girl.rollGift(owner);
    }

    /** Each resident near the hearth lays something out, while there's room. How many did. */
    public int stock(ServerLevel level, HearthBlockEntity hearth, @Nullable Player owner) {
        int n = 0;
        for (UUID id : hearth.residents()) {
            if (!(level.getEntity(id) instanceof AbstractMonsterGirl girl) || girl.isBaby() || !girl.blockPosition().closerThan(hearth.getBlockPos(), RESIDENTS_REACH)) continue;
            ItemStack g = goods(girl, owner);
            if (g.isEmpty() || !this.store.canAddItem(g)) continue;
            this.store.addItem(g);
            n++;
        }
        return n;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Store", this.store.createTag(registries));
        tag.putLong("LastDay", this.lastDay);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.store.fromTag(tag.getList("Store", 10), registries);
        this.lastDay = tag.getLong("LastDay");
    }
}
