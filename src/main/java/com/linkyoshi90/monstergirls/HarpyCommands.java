package com.linkyoshi90.monstergirls;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.List;

/**
 * /monstergirls bonds [radius] : lists the monster girls around you (harpies, anurans and every girl family; default 32
 * blocks) and how each one feels about you.
 * /monstergirls friends : every friend in the loaded world (all dimensions), how far and which way, and her orders.
 * Usable by every player.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class HarpyCommands {
    private static final int DEFAULT_RADIUS = 32;

    private HarpyCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("monstergirls")
                .then(Commands.literal("bonds")
                        .executes(ctx -> listBonds(ctx.getSource(), DEFAULT_RADIUS))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 128))
                                .executes(ctx -> listBonds(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius")))))
                .then(Commands.literal("friends").executes(ctx -> listFriends(ctx.getSource()))));
    }

    static int listBonds(CommandSourceStack source, int radius) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        List<Mob> harpies = player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius),
                h -> h instanceof Bonded && h.isAlive() && h.distanceToSqr(player) <= (double) radius * radius);
        harpies.sort(Comparator.comparingDouble(h -> h.distanceToSqr(player)));
        if (harpies.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.monstergirls.bonds.none", radius), false);
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.monstergirls.bonds.header", harpies.size(), radius).withStyle(ChatFormatting.GOLD), false);
        for (Mob harpy : harpies) {
            int distance = (int) Math.round(Math.sqrt(harpy.distanceToSqr(player)));
            Component line = Component.translatable("commands.monstergirls.bonds.entry",
                    harpy.getDisplayName().copy().withStyle(ChatFormatting.AQUA), distance, describe((Bonded) harpy, player));
            source.sendSuccess(() -> line, false);
        }
        return harpies.size();
    }

    /** Every friend in the loaded world, in every dimension: where she is from you and what she was told. */
    static int listFriends(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        List<Component> lines = friendLines(player);
        if (lines.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("message.monstergirls.compass_none"), false);
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.monstergirls.friends.header", lines.size()).withStyle(ChatFormatting.GOLD), false);
        for (Component line : lines.subList(0, Math.min(lines.size(), MAX_FRIEND_LINES))) source.sendSuccess(() -> line, false);
        if (lines.size() > MAX_FRIEND_LINES) source.sendSuccess(() -> Component.translatable("commands.monstergirls.friends.more", lines.size() - MAX_FRIEND_LINES), false);
        return lines.size();
    }

    private static final int MAX_FRIEND_LINES = 40;

    /** One line per friend: here first (nearest first), then the other dimensions. */
    public static List<Component> friendLines(ServerPlayer player) {
        List<Component> out = new java.util.ArrayList<>();
        java.util.List<net.minecraft.server.level.ServerLevel> levels = new java.util.ArrayList<>();
        levels.add(player.serverLevel());
        for (net.minecraft.server.level.ServerLevel l : player.server.getAllLevels()) if (l != player.serverLevel()) levels.add(l);
        for (net.minecraft.server.level.ServerLevel level : levels) {
            for (Mob m : com.linkyoshi90.monstergirls.entity.HomeLife.allFriends(level, player)) {
                Component name = m.getDisplayName().copy().withStyle(ChatFormatting.AQUA);
                Component order = Component.translatable("jade.monstergirls.order."
                        + com.linkyoshi90.monstergirls.entity.Orders.current((Bonded) m).name().toLowerCase(java.util.Locale.ROOT));
                if (level == player.serverLevel()) {
                    out.add(Component.translatable("commands.monstergirls.friends.entry", name, (int) player.distanceTo(m),
                            Component.translatable("direction.monstergirls." + com.linkyoshi90.monstergirls.item.FriendCompassItem.way(player, m)), order));
                } else {
                    out.add(Component.translatable("commands.monstergirls.friends.away", name, level.dimension().location().toString(),
                            m.getBlockX(), m.getBlockY(), m.getBlockZ(), order));
                }
            }
        }
        return out;
    }

    /** For tests: the same lines /monstergirls bonds would print, without needing a command source. */
    public static List<Component> describeAll(ServerPlayer player, int radius) {
        return player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius), m -> m instanceof Bonded).stream()
                .sorted(Comparator.comparingDouble(h -> h.distanceToSqr(player)))
                .map(h -> describe((Bonded) h, player)).toList();
    }

    /** Her feelings, and a note if she is expecting. */
    private static Component describe(Bonded bonded, ServerPlayer player) {
        net.minecraft.network.chat.MutableComponent bond = bonded.describeBond(player).copy();
        Component missed = com.linkyoshi90.monstergirls.entity.Missing.describe((Mob) bonded, player);
        if (missed != null) bond.append(missed);
        return bonded.getGestation().isCarrying() ? bond.append(Component.translatable("bond.monstergirls.expecting")) : bond;
    }
}
