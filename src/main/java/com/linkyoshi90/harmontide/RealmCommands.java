package com.linkyoshi90.harmontide;

import com.linkyoshi90.harmontide.entity.DemonRealm;
import com.linkyoshi90.harmontide.entity.Monsterization;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;

/**
 * /harmontide realm ...: list the possessed and the changed near you, and (for operators, to try things out) possess, turn
 * or call out of the realm the creature you are looking at.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class RealmCommands {
    private RealmCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("harmontide").then(Commands.literal("realm")
                .then(Commands.literal("list").executes(ctx -> list(ctx.getSource(), 48))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 128)).executes(ctx -> list(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "radius")))))
                .then(Commands.literal("possess").requires(s -> s.hasPermission(2)).executes(ctx -> possess(ctx.getSource())))
                .then(Commands.literal("turn").requires(s -> s.hasPermission(2)).executes(ctx -> turn(ctx.getSource())))
                .then(Commands.literal("summon").requires(s -> s.hasPermission(2)).executes(ctx -> summon(ctx.getSource())))
                .then(Commands.literal("rosemoon").executes(ctx -> nextRoseMoon(ctx.getSource())))
                .then(Commands.literal("touch").executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            ctx.getSource().sendSuccess(() -> com.linkyoshi90.harmontide.entity.RealmTouched.describe(p).copy().withStyle(ChatFormatting.LIGHT_PURPLE), false);
                            return com.linkyoshi90.harmontide.entity.RealmTouched.touch(p);
                        })
                        .then(Commands.literal("set").requires(s -> s.hasPermission(2))
                                .then(Commands.argument("touch", IntegerArgumentType.integer(0, com.linkyoshi90.harmontide.entity.RealmTouched.MAX)).executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    com.linkyoshi90.harmontide.entity.RealmTouched.setTouch(p, IntegerArgumentType.getInteger(ctx, "touch"));
                                    ctx.getSource().sendSuccess(() -> com.linkyoshi90.harmontide.entity.RealmTouched.describe(p), true);
                                    return com.linkyoshi90.harmontide.entity.RealmTouched.touch(p);
                                }))))));
    }

    /** The possessed creatures and the girls the realm changed within the radius, nearest first. */
    static int list(CommandSourceStack source, int radius) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        List<Mob> found = player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius),
                m -> (Monsterization.possessed(m) || Monsterization.monsterized(m)) && m.distanceToSqr(player) <= (double) radius * radius);
        found.sort(Comparator.comparingDouble(m -> m.distanceToSqr(player)));
        if (found.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.harmontide.realm.list.none", radius), false);
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.harmontide.realm.list.header", found.size(), radius).withStyle(ChatFormatting.DARK_PURPLE), false);
        for (Mob m : found) {
            int d = (int) Math.round(Math.sqrt(m.distanceToSqr(player)));
            Component what = Component.translatable(Monsterization.carrying(m) ? "commands.harmontide.realm.list.carrying"
                    : Monsterization.possessed(m) ? "commands.harmontide.realm.list.possessed" : "commands.harmontide.realm.list.changed");
            source.sendSuccess(() -> Component.translatable("commands.harmontide.realm.list.entry", m.getDisplayName().copy().withStyle(ChatFormatting.LIGHT_PURPLE), d, what), false);
        }
        return found.size();
    }

    /** The creature the player looks at, within eight blocks, or null. */
    @Nullable
    static Mob lookedAt(ServerPlayer player) {
        Vec3 eye = player.getEyePosition(), to = eye.add(player.getLookAngle().scale(8));
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, to, player.getBoundingBox().expandTowards(player.getLookAngle().scale(8)).inflate(1),
                e -> e instanceof Mob, 64);
        return hit != null && hit.getEntity() instanceof Mob m ? m : null;
    }

    static int possess(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Mob m = lookedAt(player);
        if (m == null || !Monsterization.POSSESSED.containsKey(m.getType()) || m.isBaby()) {
            source.sendFailure(Component.translatable("commands.harmontide.realm.possess.fail"));
            return 0;
        }
        Monsterization.possess(player.serverLevel(), m);
        source.sendSuccess(() -> Component.translatable("commands.harmontide.realm.possess", m.getDisplayName()), true);
        return 1;
    }

    static int turn(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        Mob m = lookedAt(player);
        var kind = m == null ? null : Monsterization.kindOf(m);
        var other = m == null ? null : Monsterization.otherOf(m);
        Mob girl = kind != null ? Monsterization.change(level, m, kind) : other != null ? Monsterization.changeInto(level, m, other) : null;
        if (girl == null) {
            source.sendFailure(Component.translatable("commands.harmontide.realm.turn.fail"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.harmontide.realm.turn", girl.getDisplayName()), true);
        return 1;
    }

    /** The next night with a rose moon (entity/RoseMoon), with a click to go there. */
    static int nextRoseMoon(CommandSourceStack source) {
        ServerLevel level = source.getServer().overworld();
        long today = level.getDayTime() / 24000L;
        for (long day = today; day < today + 200; day++) {
            if (!com.linkyoshi90.harmontide.entity.RoseMoon.roseDay(level.getSeed(), day)) continue;
            long time = day * 24000L + 13000L;
            long in = day - today;
            Component go = net.minecraft.network.chat.ComponentUtils.wrapInSquareBrackets(Component.literal("/time set " + time)).withStyle(s -> s
                    .withColor(ChatFormatting.LIGHT_PURPLE).withClickEvent(new net.minecraft.network.chat.ClickEvent(
                            net.minecraft.network.chat.ClickEvent.Action.SUGGEST_COMMAND, "/time set " + time)));
            source.sendSuccess(() -> Component.translatable("commands.harmontide.realm.rosemoon", in, go), false);
            return (int) in;
        }
        source.sendFailure(Component.translatable("commands.harmontide.realm.rosemoon.none"));
        return 0;
    }

    static int summon(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        var girl = DemonRealm.summon(player.serverLevel(), player.blockPosition().below(), true);
        if (girl == null) {
            source.sendFailure(Component.translatable("commands.harmontide.realm.summon.fail"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.harmontide.realm.summon", girl.getDisplayName()), true);
        return 1;
    }
}
