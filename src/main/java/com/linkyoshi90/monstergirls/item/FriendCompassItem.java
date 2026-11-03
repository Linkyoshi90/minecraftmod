package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.entity.HomeLife;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/** Right-click: which way and how far to the next of your friends in the loaded world. */
public class FriendCompassItem extends Item {
    private static final String[] WAYS = {"south", "southwest", "west", "northwest", "north", "northeast", "east", "southeast"};

    public FriendCompassItem(Properties properties) {
        super(properties);
    }

    /** Which way from the player, as a compass point. */
    public static String way(Player player, Mob mob) {
        double angle = Math.toDegrees(Math.atan2(player.getX() - mob.getX(), mob.getZ() - player.getZ()));
        return WAYS[(int) Math.floorMod(Math.round(angle / 45.0), 8L)];
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            List<Mob> friends = HomeLife.allFriends(server, player);
            if (friends.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.monstergirls.compass_none"), true);
            } else {
                int next = stack.getOrDefault(net.minecraft.core.component.DataComponents.REPAIR_COST, 0) + 1;
                stack.set(net.minecraft.core.component.DataComponents.REPAIR_COST, next);
                Mob friend = friends.get(next % friends.size());
                player.displayClientMessage(Component.translatable("message.monstergirls.compass", friend.getDisplayName(), (int) player.distanceTo(friend),
                        Component.translatable("direction.monstergirls." + way(player, friend))), true);
            }
            player.getCooldowns().addCooldown(this, 10);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
