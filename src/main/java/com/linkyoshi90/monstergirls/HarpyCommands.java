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
                                .executes(ctx -> listBonds(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius"))))));
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

    /** For tests: the same lines /monstergirls bonds would print, without needing a command source. */
    public static List<Component> describeAll(ServerPlayer player, int radius) {
        return player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius), m -> m instanceof Bonded).stream()
                .sorted(Comparator.comparingDouble(h -> h.distanceToSqr(player)))
                .map(h -> describe((Bonded) h, player)).toList();
    }

    /** Her feelings, and a note if she is expecting. */
    private static Component describe(Bonded bonded, ServerPlayer player) {
        Component bond = bonded.describeBond(player);
        if (!bonded.getGestation().isCarrying()) return bond;
        return bond.copy().append(Component.translatable("bond.monstergirls.expecting"));
    }
}
