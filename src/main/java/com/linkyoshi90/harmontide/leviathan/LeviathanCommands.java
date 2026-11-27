package com.linkyoshi90.harmontide.leviathan;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Testing help for the Dream-Leviathan (op only), {@code /harmontide leviathan ...}:
 * <ul>
 *   <li>{@code summon}: she flies in and lands near you after a short circle; {@code land}: she lands near you at once,</li>
 *   <li>{@code skip <rooms>}: marks that many rooms of the current visit done (to get to the heart chamber quickly),</li>
 *   <li>{@code heal}, {@code kill}: heals her or lets her die now; {@code rot}: jumps her rotting a day on,</li>
 *   <li>{@code status}: where she is and what she is doing; {@code reset}: forgets everything, as in a new world,</li>
 *   <li>{@code unbless}: takes your blessing (and the record of your egg) away, to try the reward again.</li>
 * </ul>
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class LeviathanCommands {
    private LeviathanCommands() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("harmontide").then(Commands.literal("leviathan").requires(s -> s.hasPermission(2))
                .then(Commands.literal("summon").executes(ctx -> summon(ctx.getSource(), false)))
                .then(Commands.literal("land").executes(ctx -> summon(ctx.getSource(), true)))
                .then(Commands.literal("heal").executes(ctx -> {
                    ServerLevel over = ctx.getSource().getServer().overworld();
                    LeviathanEvent e = LeviathanEvent.get(over);
                    if (e.phase != LeviathanEvent.LANDED) return fail(ctx.getSource());
                    Interior.expelAll(ctx.getSource().getServer(), e.run, "message.harmontide.leviathan.healed_out");
                    e.heal(over);
                    return 1;
                }))
                .then(Commands.literal("kill").executes(ctx -> {
                    ServerLevel over = ctx.getSource().getServer().overworld();
                    LeviathanEvent e = LeviathanEvent.get(over);
                    if (e.phase != LeviathanEvent.LANDED) return fail(ctx.getSource());
                    e.die(over);
                    return 1;
                }))
                .then(Commands.literal("rot").executes(ctx -> {
                    LeviathanEvent e = LeviathanEvent.get(ctx.getSource().getServer().overworld());
                    if (e.phase != LeviathanEvent.DEAD) return fail(ctx.getSource());
                    e.phaseStart -= 24000L;
                    e.setDirty();
                    return 1;
                }))
                .then(Commands.literal("skip").then(Commands.argument("rooms", IntegerArgumentType.integer(1, 12)).executes(ctx -> {
                    LeviathanEvent e = LeviathanEvent.get(ctx.getSource().getServer().overworld());
                    Interior.RunState rs = Interior.get(ctx.getSource().getServer()).run(e.run);
                    if (rs == null) return fail(ctx.getSource());
                    int n = IntegerArgumentType.getInteger(ctx, "rooms");
                    Interior.skip(ctx.getSource().getServer(), e.run, n);
                    return 1;
                })))
                .then(Commands.literal("status").executes(ctx -> {
                    LeviathanEvent e = LeviathanEvent.get(ctx.getSource().getServer().overworld());
                    ctx.getSource().sendSuccess(() -> Component.literal(String.format("phase %d at %.0f %.0f %.0f, run %d, waves beaten %d, appeared %s, healed once %s",
                            e.phase, e.x, e.y, e.z, e.run, e.progress, e.appeared, e.healedOnce)), false);
                    return 1;
                }))
                .then(Commands.literal("reset").executes(ctx -> {
                    LeviathanEvent e = LeviathanEvent.get(ctx.getSource().getServer().overworld());
                    Interior.expelAll(ctx.getSource().getServer(), e.run, "message.harmontide.leviathan.left");
                    e.reset();
                    return 1;
                }))
                .then(Commands.literal("unbless").executes(ctx -> {
                    DreamHeart.unbless(ctx.getSource().getPlayerOrException());
                    return 1;
                }))));
    }

    private static int summon(CommandSourceStack source, boolean land) throws CommandSyntaxException {
        ServerPlayer p = source.getPlayerOrException();
        ServerLevel over = source.getServer().overworld();
        if (p.level() != over) return fail(source);
        LeviathanEvent.get(over).summonNow(over, p, land);
        return 1;
    }

    private static int fail(CommandSourceStack source) {
        source.sendFailure(Component.translatable("message.harmontide.leviathan.command_no"));
        return 0;
    }
}
