package com.linkyoshi90.harmontide;

import com.linkyoshi90.harmontide.entity.BedCall;
import com.linkyoshi90.harmontide.entity.BedSharing;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BedBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** The night skipped by sleeping is what makes a monster girl at the bedside expect. */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class RelationshipEvents {
    private RelationshipEvents() {}

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (event.getLevel() instanceof ServerLevel level) BedSharing.onNightSkipped(level);
    }

    /** Sneak + right click on a bed lies down as usual (whatever is in hand) and calls out to friends. */
    @SubscribeEvent
    public static void onBedClicked(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown() || !(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof BedBlock)) return;
        event.setUseBlock(TriState.TRUE);
        if (event.getEntity() instanceof ServerPlayer player) BedCall.bedClicked(player);
    }

    /** A full-moon night does not pass until every girl answering the moon has had her turn; a call from bed holds it off too. */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        BedCall.tick(player);
        if (player.isSleeping()) BedSharing.holdNight(player);
    }
}
