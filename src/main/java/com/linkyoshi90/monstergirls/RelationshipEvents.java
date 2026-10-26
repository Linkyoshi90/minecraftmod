package com.linkyoshi90.monstergirls;

import com.linkyoshi90.monstergirls.entity.BedSharing;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** The night skipped by sleeping is what makes a monster girl at the bedside expect. */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class RelationshipEvents {
    private RelationshipEvents() {}

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (event.getLevel() instanceof ServerLevel level) BedSharing.onNightSkipped(level);
    }

    /** A full-moon night does not pass until every girl answering the moon has had her turn. */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.isSleeping()) BedSharing.holdNight(player);
    }
}
