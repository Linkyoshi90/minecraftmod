package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.entity.HomeLife;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Calls every friend within 48 to you. */
public class WhistleItem extends Item {
    public WhistleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            int came = HomeLife.whistle(server, player);
            player.displayClientMessage(Component.translatable("message.monstergirls.whistle", came), true);
            player.getCooldowns().addCooldown(this, 60);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
