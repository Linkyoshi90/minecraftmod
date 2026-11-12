package com.linkyoshi90.harmontide;

import com.linkyoshi90.harmontide.entity.order.Order;
import com.linkyoshi90.harmontide.entity.order.OrderSister;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

/**
 * /harmontide order: how the Order sees you. For operators, to try things out: set your notoriety, call a patrol (to the
 * nearest realm patch, else to you), or send a crusade against your nearest hearth now.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class OrderCommands {
    private OrderCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("harmontide").then(Commands.literal("order")
                .executes(ctx -> standing(ctx.getSource()))
                .then(Commands.literal("set").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("notoriety", IntegerArgumentType.integer(0, Order.MAX)).executes(ctx -> set(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "notoriety")))))
                .then(Commands.literal("patrol").requires(s -> s.hasPermission(2)).executes(ctx -> patrol(ctx.getSource())))
                .then(Commands.literal("raid").requires(s -> s.hasPermission(2)).executes(ctx -> raid(ctx.getSource())))));
    }

    static int standing(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        source.sendSuccess(() -> Order.standingOf(player).copy().withStyle(ChatFormatting.GOLD), false);
        for (Order.Raid r : Order.raids(player.serverLevel()).all()) {
            if (r.player().equals(player.getUUID())) {
                int up = Order.standing(player.serverLevel(), r).size();
                source.sendSuccess(() -> Component.translatable("commands.harmontide.order.raid.under_way", r.hearth().toShortString(), up, r.total()), false);
            }
        }
        return Order.notoriety(player);
    }

    static int set(CommandSourceStack source, int value) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Order.setNotoriety(player, value);
        source.sendSuccess(() -> Order.standingOf(player), true);
        return value;
    }

    static int patrol(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        BlockPos target = Order.findRealm(level, player.blockPosition(), Order.PATROL_SEARCH);
        List<OrderSister> band = Order.patrol(level, player.blockPosition(), target != null ? target : player.blockPosition(), 16, 24);
        if (band.isEmpty()) {
            source.sendFailure(Component.translatable("commands.harmontide.order.no_ground"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.harmontide.order.patrol", band.size()), true);
        return band.size();
    }

    static int raid(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        BlockPos hearth = Order.ownHearth(level, player, Order.HEARTH_RANGE);
        if (hearth == null) {
            source.sendFailure(Component.translatable("commands.harmontide.order.no_hearth"));
            return 0;
        }
        List<OrderSister> band = Order.startRaid(level, hearth, player, 28, 36);
        if (band.isEmpty()) {
            source.sendFailure(Component.translatable("commands.harmontide.order.no_ground"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.harmontide.order.raid", band.size()), true);
        return band.size();
    }
}
