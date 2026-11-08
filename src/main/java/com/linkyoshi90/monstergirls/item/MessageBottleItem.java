package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.entity.MessageBottles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A message in a bottle (entity/MessageBottles): use it to pull the cork. */
public class MessageBottleItem extends Item {
    public MessageBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            stack.consume(1, player);
            MessageBottles.open(server, player, MessageBottles.roll(server));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
