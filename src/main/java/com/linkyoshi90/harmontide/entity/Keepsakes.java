package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

/**
 * Small things to give a friend (thirty-ninth wave). A friendship bracelet she wears for good: missing that player costs her
 * half the friendship. A love letter, once a day: she writes back, a letter to keep with her line in it (a sweet one from a
 * grown friend who can flirt, a friendly one from anyone else). The umbrella is item/UmbrellaItem.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Keepsakes {
    public static final int BRACELET = 5, LETTER = 3, LOVE_LINES = 6, FRIEND_LINES = 4;
    private static final String BRACELETS = "Bracelets", LETTERS = "LetterDay";

    private Keepsakes() {}

    public static boolean wearsBracelet(Mob mob, Player player) {
        return mob.getPersistentData().getCompound(BRACELETS).contains(player.getUUID().toString());
    }

    /** She ties it on: a close friend only, once per player. True if she took it. */
    public static boolean giveBracelet(ServerLevel level, Mob mob, Player player) {
        if (!(mob instanceof Bonded b) || mob.isBaby() || b.getAffection(player) < Bonded.BED_AFFECTION || wearsBracelet(mob, player)) return false;
        CompoundTag data = mob.getPersistentData();
        CompoundTag worn = data.getCompound(BRACELETS);
        worn.putBoolean(player.getUUID().toString(), true);
        data.put(BRACELETS, worn);
        b.addAffection(player, BRACELET);
        b.blushFor(60);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
        return true;
    }

    /** She reads it and writes back, once a day: her reply, or empty if she won't. */
    public static ItemStack readLetter(ServerLevel level, Mob mob, Player player) {
        if (!(mob instanceof Bonded b) || mob.isBaby() || b.getAffection(player) < HomeLife.FRIEND) return ItemStack.EMPTY;
        long day = level.getDayTime() / Missing.DAY + 1;
        CompoundTag data = mob.getPersistentData();
        CompoundTag read = data.getCompound(LETTERS);
        String id = player.getUUID().toString();
        if (read.getLong(id) == day) return ItemStack.EMPTY;
        read.putLong(id, day);
        data.put(LETTERS, read);
        b.addAffection(player, LETTER);
        boolean love = Flirting.canFlirt(mob, player);
        String line = love ? "letter.harmontide.love." + mob.getRandom().nextInt(LOVE_LINES) : "letter.harmontide.friend." + mob.getRandom().nextInt(FRIEND_LINES);
        if (love) {
            b.blushFor(100);
            level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 4, 0.25, 0.15, 0.25, 0.0);
        }
        ItemStack reply = new ItemStack(ModItems.LOVE_LETTER.get());
        reply.set(DataComponents.CUSTOM_NAME, Component.translatable("item.harmontide.love_letter.from", mob.getDisplayName()));
        reply.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable(line))));
        return reply;
    }

    /** A letter she wrote: kept, not given back. */
    public static boolean isReply(ItemStack stack) {
        return stack.has(DataComponents.LORE);
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        boolean bracelet = stack.is(ModItems.FRIENDSHIP_BRACELET.get()), letter = stack.is(ModItems.LOVE_LETTER.get()) && !isReply(stack);
        if ((!bracelet && !letter) || !(event.getTarget() instanceof Mob mob) || !(mob instanceof Bonded)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Player player = event.getEntity();
        if (bracelet) {
            if (giveBracelet(level, mob, player)) {
                stack.consume(1, player);
                player.displayClientMessage(Component.translatable("message.harmontide.bracelet", mob.getDisplayName()), true);
            } else {
                player.displayClientMessage(Component.translatable(wearsBracelet(mob, player) ? "message.harmontide.bracelet_has" : "message.harmontide.bracelet_refused", mob.getDisplayName()), true);
            }
            return;
        }
        ItemStack reply = readLetter(level, mob, player);
        if (reply.isEmpty()) {
            boolean known = ((Bonded) mob).getAffection(player) >= HomeLife.FRIEND && !mob.isBaby();
            player.displayClientMessage(Component.translatable(known ? "message.harmontide.letter_today" : "message.harmontide.letter_refused", mob.getDisplayName()), true);
            return;
        }
        stack.consume(1, player);
        player.getInventory().placeItemBackInInventory(reply);
        player.displayClientMessage(Component.translatable("message.harmontide.letter", mob.getDisplayName()), true);
    }
}
