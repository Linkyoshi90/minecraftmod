package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.registry.ModBlockEntities;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** Who the figurine shows: a saved copy of her, kept to draw and never spawned. */
public class FigurineBlockEntity extends BlockEntity {
    private CompoundTag mob = new CompoundTag();
    @Nullable
    private Entity shown;
    /** Whether breaking it gives the figurine back (not in creative). */
    boolean keep = true;

    public FigurineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FIGURINE.get(), pos, state);
    }

    /** A figurine item of her, as she is now. */
    public static ItemStack of(Mob mob) {
        CompoundTag saved = new CompoundTag();
        if (!mob.saveAsPassenger(saved)) return ItemStack.EMPTY;
        saved.remove("UUID");
        saved.remove("Passengers");
        ItemStack stack = new ItemStack(ModItems.FIGURINE.get());
        return stamp(stack, saved, mob.getDisplayName());
    }

    private static ItemStack stamp(ItemStack stack, CompoundTag saved, @Nullable Component name) {
        CompoundTag data = new CompoundTag();
        data.put("Mob", saved);
        BlockItem.setBlockEntityData(stack, com.linkyoshi90.harmontide.registry.ModBlockEntities.FIGURINE.get(), data);
        if (name != null) stack.set(DataComponents.CUSTOM_NAME, Component.translatable("item.harmontide.figurine.of", name));
        return stack;
    }

    public ItemStack toItem() {
        ItemStack stack = new ItemStack(ModItems.FIGURINE.get());
        return this.mob.isEmpty() ? stack : stamp(stack, this.mob, null);
    }

    /** The copy to draw (client). */
    @Nullable
    public Entity shown(Level level) {
        if (this.shown == null && !this.mob.isEmpty()) this.shown = EntityType.create(this.mob, level).orElse(null);
        return this.shown;
    }

    public boolean isBlank() {
        return this.mob.isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.mob.isEmpty()) tag.put("Mob", this.mob);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.mob = tag.getCompound("Mob");
        this.shown = null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
