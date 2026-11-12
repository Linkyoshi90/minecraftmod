package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.entity.HomeLife;
import com.linkyoshi90.harmontide.entity.Orders;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Calls every friend within 48 to you. Sneaking, it gives everyone within 48 who takes your orders the same one:
 * stay put, or (once they all sit already) follow you.
 */
public class WhistleItem extends Item {
    public static final double SQUAD_REACH = 48;

    public WhistleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            if (player.isShiftKeyDown()) {
                List<Mob> squad = Orders.squad(server, player, SQUAD_REACH);
                Orders.Command order = Orders.nextGroupOrder(squad);
                Orders.orderAll(squad, order, player);
                server.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), player.getSoundSource(), 1.5F,
                        order == Orders.Command.SIT ? 1.0F : 1.5F);
                player.displayClientMessage(Component.translatable(order == Orders.Command.SIT
                        ? "message.harmontide.whistle_stay" : "message.harmontide.whistle_follow", squad.size()), true);
            } else {
                int came = HomeLife.whistle(server, player);
                player.displayClientMessage(Component.translatable("message.harmontide.whistle", came), true);
            }
            player.getCooldowns().addCooldown(this, 60);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.harmontide.friend_whistle.tip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.harmontide.friend_whistle.tip_sneak").withStyle(ChatFormatting.GRAY));
    }
}
