package com.linkyoshi90.monstergirls;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Marriage;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Operator shortcuts for trying the family features (2026-11-09), on the girl you are looking at: /monstergirls family wed (devoted,
 * engaged and wed by the nearest hearth or on the spot), due (expecting and ready now), grow (a quarter of the way to grown).
 * Plain /monstergirls family (HarpyCommands) lists the family.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class FamilyCommands {
    private FamilyCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("monstergirls").then(Commands.literal("family")
                .then(Commands.literal("wed").requires(s -> s.hasPermission(2)).executes(ctx -> wed(ctx.getSource())))
                .then(Commands.literal("due").requires(s -> s.hasPermission(2)).executes(ctx -> due(ctx.getSource())))
                .then(Commands.literal("grow").requires(s -> s.hasPermission(2)).executes(ctx -> grow(ctx.getSource())))));
    }

    private static Mob target(CommandSourceStack source, ServerPlayer p) {
        Mob m = RealmCommands.lookedAt(p);
        if (!(m instanceof Bonded)) {
            source.sendFailure(Component.translatable("commands.monstergirls.family.look"));
            return null;
        }
        return m;
    }

    static int wed(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer p = source.getPlayerOrException();
        Mob m = target(source, p);
        if (m == null) return 0;
        ((Bonded) m).setAffection(p.getUUID(), Bonded.FULL_AFFECTION);
        if (!Marriage.engaged(m, p) && Marriage.propose(p.serverLevel(), m, p) != Marriage.Answer.ENGAGED) return 0;
        if (!Marriage.tryWed(p.serverLevel(), m, p)) Marriage.wed(p.serverLevel(), m, p, m.blockPosition());
        return 1;
    }

    static int due(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer p = source.getPlayerOrException();
        Mob m = target(source, p);
        if (m == null) return 0;
        var g = ((Bonded) m).getGestation();
        if (!g.isCarrying()) g.conceive(p.getUUID());
        g.setTicksLeft(0);
        source.sendSuccess(() -> Component.translatable("commands.monstergirls.family.due", m.getDisplayName()), true);
        return 1;
    }

    static int grow(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer p = source.getPlayerOrException();
        if (!(target(source, p) instanceof AbstractMonsterGirl g) || !g.isBaby()) {
            source.sendFailure(Component.translatable("commands.monstergirls.family.not_little"));
            return 0;
        }
        g.setGrowTicks(g.getGrowTicks() + AbstractMonsterGirl.GROW_TICKS / 4);
        source.sendSuccess(() -> Component.translatable("commands.monstergirls.family.grown_some", g.getDisplayName()), true);
        return 1;
    }
}
