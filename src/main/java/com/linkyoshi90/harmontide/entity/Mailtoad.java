package com.linkyoshi90.harmontide.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The mailtoad (HARMONTIDE_CONCEPTS.md, eleventh wave; model tools/gen_mailtoad.py, the cane toad's rig): a toad
 * postwoman. A friend hands her an item (her parcel), then a paper renamed in an anvil to a player's name (the address):
 * she carries the parcel to that player. Close by, she walks it over; further off in the same dimension she hops to
 * them; if they are offline or elsewhere she waits with it. A crouching empty hand from the sender takes it back.
 */
public class Mailtoad extends MucusToad {
    /** Within this, she walks; beyond it she hops straight to the addressee. */
    public static final double WALK_RANGE = 48.0;

    private ItemStack parcel = ItemStack.EMPTY;
    @Nullable
    private UUID sender;
    @Nullable
    private String addressee;
    private long nextNotice;

    public Mailtoad(EntityType<? extends Mailtoad> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return toadAttributes(16.0, 0.22);
    }

    @Override
    protected String species() {
        return "mailtoad";
    }

    public ItemStack getParcel() {
        return this.parcel;
    }

    @Nullable
    public String getAddressee() {
        return this.addressee;
    }

    /** Takes a parcel from the player's hand. False if she already carries one or does not know them well enough. */
    public boolean takeParcel(Player player, ItemStack stack) {
        if (!this.parcel.isEmpty() || stack.isEmpty() || this.getAffection(player) < Orders.AFFECTION) return false;
        this.parcel = stack.copyAndClear();
        this.sender = player.getUUID();
        this.addressee = null;
        player.displayClientMessage(Component.translatable("message.harmontide.mail_parcel", this.getDisplayName(), this.parcel.getHoverName()), true);
        return true;
    }

    /** A paper renamed to a player's name: where the parcel goes. */
    public boolean address(Player player, ItemStack paper) {
        if (this.parcel.isEmpty() || !player.getUUID().equals(this.sender) || !paper.is(Items.PAPER) || !paper.has(DataComponents.CUSTOM_NAME)) return false;
        this.addressee = paper.getHoverName().getString().trim();
        paper.consume(1, player);
        player.displayClientMessage(Component.translatable("message.harmontide.mail_addressed", this.getDisplayName(), this.addressee), true);
        this.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
        return true;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND && !this.isBaby()) {
            boolean client = this.level().isClientSide;
            if (stack.isEmpty() && player.isShiftKeyDown() && !this.parcel.isEmpty() && player.getUUID().equals(this.sender)) {
                if (!client) this.returnParcel(player);
                return InteractionResult.sidedSuccess(client);
            }
            if (stack.is(Items.PAPER) && stack.has(DataComponents.CUSTOM_NAME) && !this.parcel.isEmpty()) {
                if (!client) this.address(player, stack);
                return InteractionResult.sidedSuccess(client);
            }
            if (!stack.isEmpty() && !this.isFood(stack) && this.parcel.isEmpty() && this.getAffection(player) >= Orders.AFFECTION) {
                if (!client) this.takeParcel(player, stack);
                return InteractionResult.sidedSuccess(client);
            }
        }
        return super.mobInteract(player, hand);
    }

    private void returnParcel(Player player) {
        if (!player.getInventory().add(this.parcel)) player.drop(this.parcel, false);
        this.parcel = ItemStack.EMPTY;
        this.sender = null;
        this.addressee = null;
    }

    /** Hands the parcel over (into their inventory, or at their feet) and tells both players. */
    public void deliver(ServerLevel level, Player to) {
        ItemStack p = this.parcel;
        this.parcel = ItemStack.EMPTY;
        if (!to.getInventory().add(p)) to.drop(p, false);
        to.displayClientMessage(Component.translatable("message.harmontide.mail_delivered", this.getDisplayName()), false);
        Player from = this.sender == null ? null : level.getServer().getPlayerList().getPlayer(this.sender);
        if (from != null && from != to) from.displayClientMessage(Component.translatable("message.harmontide.mail_done", this.getDisplayName(), to.getDisplayName()), false);
        this.sender = null;
        this.addressee = null;
        this.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 0.8F);
    }

    /** On her round: to the addressee if they are online in this dimension, else she waits. */
    private void tickRound(ServerLevel level) {
        if (this.parcel.isEmpty() || this.addressee == null) return;
        Player to = level.getServer().getPlayerList().getPlayerByName(this.addressee);
        if (to == null || to.level() != level) {
            if (level.getGameTime() >= this.nextNotice && this.sender != null) {
                this.nextNotice = level.getGameTime() + 6000;
                Player from = level.getServer().getPlayerList().getPlayer(this.sender);
                if (from != null) from.displayClientMessage(Component.translatable("message.harmontide.mail_waiting", this.getDisplayName(), this.addressee), true);
            }
            return;
        }
        double d = this.distanceToSqr(to);
        if (d < 2.5 * 2.5) {
            this.deliver(level, to);
        } else if (d > WALK_RANGE * WALK_RANGE) {                       // a long hop: she turns up beside them
            this.teleportTo(to.getX() + this.random.nextGaussian(), to.getY(), to.getZ() + this.random.nextGaussian());
            this.getNavigation().stop();
        } else {
            this.getNavigation().moveTo(to, 1.2);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && this.tickCount % 10 == 0) this.tickRound(level);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (!this.parcel.isEmpty()) this.spawnAtLocation(this.parcel);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return this.parcel.isEmpty() && super.removeWhenFarAway(distance);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!this.parcel.isEmpty()) tag.put("Parcel", this.parcel.save(this.registryAccess()));
        if (this.sender != null) tag.putUUID("Sender", this.sender);
        if (this.addressee != null) tag.putString("Addressee", this.addressee);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.parcel = tag.contains("Parcel") ? ItemStack.parseOptional(this.registryAccess(), tag.getCompound("Parcel")) : ItemStack.EMPTY;
        this.sender = tag.hasUUID("Sender") ? tag.getUUID("Sender") : null;
        this.addressee = tag.contains("Addressee") ? tag.getString("Addressee") : null;
    }
}
