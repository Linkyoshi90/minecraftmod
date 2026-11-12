package com.linkyoshi90.harmontide.item;

import com.linkyoshi90.harmontide.entity.HomeLife;
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

    public static final double GLOW_REACH = 128;

    public FriendCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.translatable("item.harmontide.friend_compass.tip").withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.harmontide.friend_compass.tip_sneak").withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    /** Which way from the player, as a compass point. */
    public static String way(Player player, Mob mob) {
        return way(player, mob.position());
    }

    /** Which way a spot lies from the player. */
    public static String way(Player player, net.minecraft.world.phys.Vec3 at) {
        double angle = Math.toDegrees(Math.atan2(player.getX() - at.x, at.z - player.getZ()));
        return WAYS[(int) Math.floorMod(Math.round(angle / 45.0), 8L)];
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            List<Mob> friends = HomeLife.allFriends(server, player);
            if (player.isShiftKeyDown()) {                                        // everyone within reach glows for ten seconds
                int lit = 0;
                for (Mob m : friends) {
                    if (m.distanceToSqr(player) > GLOW_REACH * GLOW_REACH) break;  // nearest first
                    m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 200, 0, false, false));
                    lit++;
                }
                player.displayClientMessage(lit == 0 ? Component.translatable("message.harmontide.compass_none")
                        : Component.translatable("message.harmontide.compass_glow", lit), true);
                player.getCooldowns().addCooldown(this, 100);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }
            if (friends.isEmpty()) {
                com.linkyoshi90.harmontide.entity.FriendLog.Seen last = com.linkyoshi90.harmontide.entity.FriendLog.get(server.getServer()).of(player).stream()
                        .filter(s -> s.dimension().equals(level.dimension().location().toString())).findFirst().orElse(null);
                player.displayClientMessage(last == null ? Component.translatable("message.harmontide.compass_none")
                        : Component.translatable("message.harmontide.compass_last", last.name(), (int) Math.sqrt(last.pos().distToCenterSqr(player.position())),
                        Component.translatable("direction.harmontide." + way(player, net.minecraft.world.phys.Vec3.atCenterOf(last.pos())))), true);
            } else {
                int next = stack.getOrDefault(net.minecraft.core.component.DataComponents.REPAIR_COST, 0) + 1;
                stack.set(net.minecraft.core.component.DataComponents.REPAIR_COST, next);
                Mob friend = friends.get(next % friends.size());
                player.displayClientMessage(Component.translatable("message.harmontide.compass", friend.getDisplayName(), (int) player.distanceTo(friend),
                        Component.translatable("direction.harmontide." + way(player, friend))), true);
            }
            player.getCooldowns().addCooldown(this, 10);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
