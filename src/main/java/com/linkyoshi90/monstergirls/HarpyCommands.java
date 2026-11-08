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
                .then(Commands.literal("friends").executes(ctx -> listFriends(ctx.getSource())))
                .then(Commands.literal("locate").then(Commands.literal("realm").executes(ctx -> locateRealm(ctx.getSource())))
                        .then(Commands.literal("chapel").executes(ctx -> locate(ctx.getSource(), com.linkyoshi90.monstergirls.registry.ModStructures.ORDER_CHAPEL_KEY, "chapel")))
                        .then(Commands.literal("manor").executes(ctx -> locate(ctx.getSource(), com.linkyoshi90.monstergirls.registry.ModStructures.REALM_MANOR_KEY, "manor")))
                        .then(Commands.literal("aerie").executes(ctx -> locate(ctx.getSource(), com.linkyoshi90.monstergirls.registry.ModStructures.HARPY_AERIE_KEY, "aerie")))
                        .then(Commands.literal("spire").executes(ctx -> locate(ctx.getSource(), com.linkyoshi90.monstergirls.registry.ModStructures.DEMON_SPIRE_KEY, "spire")))));
    }

    /**
     * The nearest wild patch of the Demon Realm (world/RealmPatchStructure), as /locate structure monstergirls:realm_patch finds
     * it, with the coordinates to click. Realms a Lilith grows herself are not on any map.
     */
    static int locateRealm(CommandSourceStack source) {
        return locate(source, com.linkyoshi90.monstergirls.registry.ModStructures.REALM_PATCH_KEY, "realm");
    }

    /** The nearest structure of ours of this kind (lang commands.monstergirls.locate.<what>[.none]). */
    static int locate(CommandSourceStack source, net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.Structure> key, String what) {
        net.minecraft.server.level.ServerLevel level = source.getLevel();
        var holder = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getHolder(key);
        net.minecraft.core.BlockPos from = net.minecraft.core.BlockPos.containing(source.getPosition());
        var found = holder.isEmpty() ? null : level.getChunkSource().getGenerator().findNearestMapStructure(level,
                net.minecraft.core.HolderSet.direct(holder.get()), from, 100, false);
        if (found == null) {
            source.sendFailure(Component.translatable("commands.monstergirls.locate." + what + ".none"));
            return 0;
        }
        net.minecraft.core.BlockPos at = found.getFirst();
        int distance = net.minecraft.util.Mth.floor(Math.sqrt(from.distSqr(new net.minecraft.core.BlockPos(at.getX(), from.getY(), at.getZ()))));
        Component coords = net.minecraft.network.chat.ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", at.getX(), "~", at.getZ()))
                .withStyle(s -> s.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.SUGGEST_COMMAND,
                                "/tp @s " + at.getX() + " ~ " + at.getZ()))
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("chat.coordinates.tooltip"))));
        source.sendSuccess(() -> Component.translatable("commands.monstergirls.locate." + what, coords, distance), false);
        return distance;
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
            Component sweet = com.linkyoshi90.monstergirls.entity.Sweethearts.describe(harpy);
            if (sweet != null) source.sendSuccess(() -> Component.literal("   ").append(sweet).withStyle(ChatFormatting.LIGHT_PURPLE), false);
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
        java.util.Set<java.util.UUID> listed = new java.util.HashSet<>();
        java.util.List<net.minecraft.server.level.ServerLevel> levels = new java.util.ArrayList<>();
        levels.add(player.serverLevel());
        for (net.minecraft.server.level.ServerLevel l : player.server.getAllLevels()) if (l != player.serverLevel()) levels.add(l);
        for (net.minecraft.server.level.ServerLevel level : levels) {
            for (Mob m : com.linkyoshi90.monstergirls.entity.HomeLife.allFriends(level, player)) {
                listed.add(m.getUUID());
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
        for (com.linkyoshi90.monstergirls.entity.FriendLog.Seen s : com.linkyoshi90.monstergirls.entity.FriendLog.get(player.server).of(player)) {
            if (listed.contains(s.mob())) continue;                              // out of the loaded world: where she was last seen
            out.add(Component.translatable("commands.monstergirls.friends.unloaded", Component.literal(s.name()).withStyle(ChatFormatting.DARK_AQUA),
                    s.pos().getX(), s.pos().getY(), s.pos().getZ(), s.dimension(), com.linkyoshi90.monstergirls.entity.FriendLog.ago(player.server, s)));
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
        Component season = com.linkyoshi90.monstergirls.entity.MatingSeason.describe((Mob) bonded);
        if (season != null) bond.append(season);
        Component wed = com.linkyoshi90.monstergirls.entity.Marriage.describe((Mob) bonded, player);
        if (wed != null) bond.append(wed);
        Component changed = com.linkyoshi90.monstergirls.entity.Monsterization.describe((Mob) bonded);
        if (changed != null) bond.append(changed);
        com.linkyoshi90.monstergirls.entity.Orders.Command order = com.linkyoshi90.monstergirls.entity.Orders.current(bonded);
        if (order != com.linkyoshi90.monstergirls.entity.Orders.Command.FREE_ROAM && bonded.getAffection(player) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION) {
            bond.append(Component.literal(" · ")).append(Component.translatable("jade.monstergirls.order." + order.name().toLowerCase(java.util.Locale.ROOT)).withStyle(ChatFormatting.BLUE));
        }
        return bonded.getGestation().isCarrying() ? bond.append(Component.translatable("bond.monstergirls.expecting")) : bond;
    }
}
