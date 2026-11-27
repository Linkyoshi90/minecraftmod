package com.linkyoshi90.harmontide;

import com.linkyoshi90.harmontide.entity.Bonded;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import javax.annotation.Nullable;

/**
 * Testing help for the 2026-11-27 abilities (SPECIES_ABILITIES_TESTING.md): {@code /harmontide befriend [amount]} (op) sets the girl
 * (or harpy, anuran...) you are looking at to that friendship with you, 25 when no amount is given.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class TraversalCommands {
    private TraversalCommands() {}

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("harmontide").then(Commands.literal("befriend").requires(s -> s.hasPermission(2))
                .executes(ctx -> befriend(ctx.getSource(), Bonded.BED_AFFECTION))
                .then(Commands.argument("amount", IntegerArgumentType.integer(0, Bonded.FULL_AFFECTION))
                        .executes(ctx -> befriend(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "amount"))))));
    }

    /** The bonded creature the player is looking at within 16 blocks (the one nearest the middle of their view), or null. */
    @Nullable
    public static Mob lookedAt(ServerPlayer p) {
        Vec3 eye = p.getEyePosition(), look = p.getLookAngle();
        Mob best = null;
        double bestDot = 0.97;
        for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(16), m -> m instanceof Bonded && m.isAlive())) {
            Vec3 to = m.getBoundingBox().getCenter().subtract(eye).normalize();
            double dot = to.dot(look);
            if (dot > bestDot && p.hasLineOfSight(m)) {
                bestDot = dot;
                best = m;
            }
        }
        return best;
    }

    private static int befriend(CommandSourceStack source, int amount) {
        if (!(source.getEntity() instanceof ServerPlayer p)) return 0;
        Mob m = lookedAt(p);
        if (!(m instanceof Bonded b)) {
            source.sendFailure(Component.literal("Look at a girl first."));
            return 0;
        }
        b.setAffection(p.getUUID(), amount);
        source.sendSuccess(() -> Component.literal(m.getDisplayName().getString() + ": friendship " + amount), false);
        return 1;
    }
}
