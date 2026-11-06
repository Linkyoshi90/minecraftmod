package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * A name of her own: the first time a friend gives a grown friend a name (a named name tag), she is delighted with it: hearts, a
 * line, and a little closer ({@link #CLOSER}). Renaming her later is just a name tag. Strangers get the name the vanilla way.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Names {
    public static final String NAMED = "NamedByFriend";
    public static final int CLOSER = 3;

    private Names() {
    }

    @SubscribeEvent
    public static void onNameTag(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (event.getLevel().isClientSide || !stack.is(Items.NAME_TAG) || !stack.has(DataComponents.CUSTOM_NAME)) return;
        if (event.getTarget() instanceof LivingEntity target) named(event.getEntity(), target, stack.get(DataComponents.CUSTOM_NAME));
    }

    /** A friend names her. Whether this was her first name from a friend (and so a moment). */
    public static boolean named(Player player, LivingEntity girl, Component name) {
        if (!(girl instanceof Bonded b) || girl.isBaby() || b.getAffection(player) < Orders.AFFECTION || girl.getPersistentData().getBoolean(NAMED)) return false;
        girl.getPersistentData().putBoolean(NAMED, true);
        b.addAffection(player, CLOSER);
        if (girl.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.4, girl.getZ(), 4, 0.3, 0.2, 0.3, 0);
        if (player instanceof ServerPlayer sp) sp.displayClientMessage(Component.translatable("message.monstergirls.named", name).withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return true;
    }
}
