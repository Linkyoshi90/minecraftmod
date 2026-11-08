package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Dairy;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A mailbox (2026-11-08, courier girls). Whatever its owner puts in it, a courier takes: a harpy or a centaur who is their friend
 * ({@link HomeLife#FRIEND}) and lives within {@link #COURIER_REACH} blocks. She carries the parcel to the mailbox it's linked to
 * (a feather on this one, then on the other) or, unlinked, to the owner wherever they are, taking a second for every
 * {@link #SPEED} blocks of the way (at least {@link #MIN_TRIP} seconds). A parcel waits for an unloaded mailbox or an absent owner.
 */
public class MailboxBlockEntity extends BlockEntity {
    public static final int SLOTS = 5, COURIER_REACH = 32, SPEED = 12, MIN_TRIP = 5, EVERY = 100;
    private final SimpleContainer store = new SimpleContainer(SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            MailboxBlockEntity.this.setChanged();
        }
    };
    @Nullable
    private UUID owner;
    @Nullable
    private BlockPos target;
    private final List<Parcel> parcels = new ArrayList<>();

    /** Items on their way, arriving at {@code arrives}, to {@code to} (a mailbox) or, if null, to the owner; carried by {@code courier}. */
    public record Parcel(List<ItemStack> items, long arrives, @Nullable BlockPos to, String courier) {}

    public MailboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAILBOX.get(), pos, state);
    }

    public SimpleContainer store() {
        return this.store;
    }

    public List<Parcel> parcels() {
        return this.parcels;
    }

    @Nullable
    public UUID owner() {
        return this.owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    @Nullable
    public BlockPos target() {
        return this.target;
    }

    public void setTarget(@Nullable BlockPos target) {
        this.target = target == null ? null : target.immutable();
        this.setChanged();
    }

    /** Who can carry the mail: a grown harpy or centaur, the owner's friend, not told to sit. */
    public static boolean courier(Mob m, Player owner) {
        if (m.isBaby() || !(m instanceof Bonded b) || b.isOrderedToSit() || b.getAffection(owner) < HomeLife.FRIEND) return false;
        return m instanceof AbstractHarpy || m instanceof AbstractMonsterGirl g && g.getSpecies().family() == GirlSpecies.Family.CENTAUR;
    }

    @Nullable
    public Mob findCourier(ServerLevel level, Player owner) {
        for (Mob m : level.getEntitiesOfClass(Mob.class, new AABB(this.worldPosition).inflate(COURIER_REACH), Mob::isAlive)) {
            if (courier(m, owner)) return m;
        }
        return null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MailboxBlockEntity box) {
        if (level instanceof ServerLevel server && server.getGameTime() % EVERY == 0) {
            box.send(server);
            box.deliver(server);
        }
    }

    /** A courier takes what's in the box. True if a parcel set off. */
    public boolean send(ServerLevel level) {
        if (this.store.isEmpty() || this.owner == null) return false;
        Player owner = level.getPlayerByUUID(this.owner);
        if (owner == null) return false;
        Mob courier = this.findCourier(level, owner);
        if (courier == null) return false;
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < SLOTS; i++) {
            ItemStack s = this.store.removeItemNoUpdate(i);
            if (!s.isEmpty()) items.add(s);
        }
        this.store.setChanged();
        BlockPos dest = this.target;
        double dist = Math.sqrt(this.worldPosition.distSqr(dest != null ? dest : owner.blockPosition()));
        long trip = 20L * Math.max(MIN_TRIP, (long) (dist / SPEED));
        this.parcels.add(new Parcel(items, level.getGameTime() + trip, dest, courier.getDisplayName().getString()));
        ((Bonded) courier).addAffection(owner, 1);
        level.sendParticles(ParticleTypes.CLOUD, courier.getX(), courier.getY() + 1, courier.getZ(), 8, 0.3, 0.3, 0.3, 0.02);
        level.playSound(null, this.worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
        this.setChanged();
        return true;
    }

    /** Parcels that have arrived are handed over. How many. */
    public int deliver(ServerLevel level) {
        int done = 0;
        for (var it = this.parcels.iterator(); it.hasNext(); ) {
            Parcel p = it.next();
            if (level.getGameTime() < p.arrives()) continue;
            if (p.to() != null) {
                if (!level.isLoaded(p.to()) || !(level.getBlockEntity(p.to()) instanceof MailboxBlockEntity there)) {
                    if (level.isLoaded(p.to())) {                          // the other mailbox is gone: back it comes
                        it.remove();
                        p.items().forEach(s -> net.minecraft.world.Containers.dropItemStack(level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1, this.worldPosition.getZ() + 0.5, s));
                        done++;
                    }
                    continue;
                }
                List<ItemStack> left = new ArrayList<>();
                for (ItemStack s : p.items()) {
                    ItemStack rest = there.store.addItem(s);
                    if (!rest.isEmpty()) left.add(rest);
                }
                left.forEach(s -> net.minecraft.world.Containers.dropItemStack(level, p.to().getX() + 0.5, p.to().getY() + 1, p.to().getZ() + 0.5, s));
                level.sendParticles(ParticleTypes.CLOUD, p.to().getX() + 0.5, p.to().getY() + 1.2, p.to().getZ() + 0.5, 6, 0.3, 0.2, 0.3, 0.02);
            } else {
                Player owner = this.owner == null ? null : level.getPlayerByUUID(this.owner);
                if (owner == null) continue;
                for (ItemStack s : p.items()) if (!owner.getInventory().add(s)) owner.drop(s, false);
                owner.displayClientMessage(Component.translatable("message.monstergirls.mailbox.delivered", p.courier()).withStyle(ChatFormatting.AQUA), true);
                Dairy.award(owner, "courier");
            }
            it.remove();
            done++;
        }
        if (done > 0) this.setChanged();
        return done;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Store", this.store.createTag(registries));
        if (this.owner != null) tag.putUUID("Owner", this.owner);
        if (this.target != null) tag.putLong("Target", this.target.asLong());
        ListTag list = new ListTag();
        for (Parcel p : this.parcels) {
            CompoundTag t = new CompoundTag();
            ListTag items = new ListTag();
            for (ItemStack s : p.items()) items.add(s.save(registries));
            t.put("Items", items);
            t.putLong("Arrives", p.arrives());
            if (p.to() != null) t.putLong("To", p.to().asLong());
            t.putString("Courier", p.courier());
            list.add(t);
        }
        tag.put("Parcels", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.store.fromTag(tag.getList("Store", 10), registries);
        this.owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        this.target = tag.contains("Target") ? BlockPos.of(tag.getLong("Target")) : null;
        this.parcels.clear();
        for (var e : tag.getList("Parcels", 10)) {
            CompoundTag t = (CompoundTag) e;
            List<ItemStack> items = new ArrayList<>();
            for (var i : t.getList("Items", 10)) ItemStack.parse(registries, i).ifPresent(items::add);
            this.parcels.add(new Parcel(items, t.getLong("Arrives"), t.contains("To") ? BlockPos.of(t.getLong("To")) : null, t.getString("Courier")));
        }
    }
}
