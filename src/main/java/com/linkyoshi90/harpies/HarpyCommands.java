package com.linkyoshi90.harpies;

import com.linkyoshi90.harpies.entity.AbstractHarpy;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.List;

/**
 * /harpies bonds [radius] : lists the harpies around you (default 32 blocks) and how each one feels about you.
 * Usable by every player.
 */
@EventBusSubscriber(modid = HarpiesMod.MODID)
public final class HarpyCommands {
    private static final int DEFAULT_RADIUS = 32;

    private HarpyCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("harpies")
                .then(Commands.literal("bonds")
                        .executes(ctx -> listBonds(ctx.getSource(), DEFAULT_RADIUS))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 128))
                                .executes(ctx -> listBonds(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius"))))));
    }

    static int listBonds(CommandSourceStack source, int radius) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        List<AbstractHarpy> harpies = player.serverLevel().getEntitiesOfClass(AbstractHarpy.class, player.getBoundingBox().inflate(radius),
                h -> h.isAlive() && h.distanceToSqr(player) <= (double) radius * radius);
        harpies.sort(Comparator.comparingDouble(h -> h.distanceToSqr(player)));
        if (harpies.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.harpies.bonds.none", radius), false);
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.harpies.bonds.header", harpies.size(), radius).withStyle(ChatFormatting.GOLD), false);
        for (AbstractHarpy harpy : harpies) {
            int distance = (int) Math.round(Math.sqrt(harpy.distanceToSqr(player)));
            Component line = Component.translatable("commands.harpies.bonds.entry",
                    harpy.getDisplayName().copy().withStyle(ChatFormatting.AQUA), distance, harpy.describeBond(player));
            source.sendSuccess(() -> line, false);
        }
        return harpies.size();
    }

    /** For tests: the same lines /harpies bonds would print, without needing a command source. */
    public static List<Component> describeAll(ServerPlayer player, int radius) {
        return player.serverLevel().getEntitiesOfClass(AbstractHarpy.class, player.getBoundingBox().inflate(radius)).stream()
                .sorted(Comparator.comparingDouble(h -> h.distanceToSqr(player)))
                .map(h -> h.describeBond(player)).toList();
    }
}
