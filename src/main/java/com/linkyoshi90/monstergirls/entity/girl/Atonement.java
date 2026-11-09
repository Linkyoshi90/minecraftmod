package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Atonement before Anubis (2026-11-09; the fifth wave left it open). A heart grows heavy with every villager and iron golem a player has
 * killed (Canid.heartWeight, from their lifetime stats, which never go down); each good deed lightens it by one: a zombie villager
 * they cured (golden apple to a weakened one), or an iron golem they built. Once the deeds balance the deaths, show the Anubis who
 * condemned you a feather (the feather of Ma'at) and she weighs you again: lighter than the feather, you are free.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Atonement {
    public static final String DEEDS = "Atonement";
    static final String CURED_BY = "CuredBy";
    public static final double BUILDER = 8;

    private Atonement() {
    }

    public static int deeds(Player player) {
        return player.getData(ModAttachments.JOURNAL).getInt(DEEDS);
    }

    /** One more good deed: the heart a little lighter. */
    public static void deed(Player player, String key) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        journal.putInt(DEEDS, journal.getInt(DEEDS) + 1);
        player.setData(ModAttachments.JOURNAL, journal);
        if (Canid.heartWeight((ServerPlayer) player) > 0) player.displayClientMessage(Component.translatable("message.monstergirls.atonement." + key), true);
    }

    /** What still weighs on their heart: the deaths less the deeds, never below zero. */
    public static int netWeight(ServerPlayer player) {
        return Math.max(0, Canid.heartWeight(player) - deeds(player));
    }

    /** A golden apple to a zombie villager: remember who began the cure, so the deed is theirs when it takes. */
    @SubscribeEvent
    public static void onCureStarted(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide && event.getTarget() instanceof ZombieVillager zv && event.getItemStack().is(Items.GOLDEN_APPLE)) {
            zv.getPersistentData().putUUID(CURED_BY, event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onCured(LivingConversionEvent.Post event) {
        if (event.getEntity() instanceof ZombieVillager zv && event.getOutcome() instanceof Villager && zv.getPersistentData().hasUUID(CURED_BY)
                && zv.level() instanceof ServerLevel level && level.getPlayerByUUID(zv.getPersistentData().getUUID(CURED_BY)) instanceof ServerPlayer p) {
            deed(p, "cured");
        }
    }

    /** An iron golem built (player-made) beside a player: theirs. */
    @SubscribeEvent
    public static void onGolemBuilt(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getEntity() instanceof IronGolem golem) || !golem.isPlayerCreated() || !(event.getLevel() instanceof ServerLevel level)) return;
        if (level.getNearestPlayer(golem, BUILDER) instanceof ServerPlayer p) deed(p, "golem");
    }

    /** A feather shown to an Anubis who condemned them: she weighs them again. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFeather(PlayerInteractEvent.EntityInteract event) {
        if (!event.getItemStack().is(Items.FEATHER) || !(event.getTarget() instanceof Canid anubis) || anubis.getSpecies() != GirlSpecies.ANUBIS_CANID
                || !anubis.isCondemned(event.getEntity())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getEntity() instanceof ServerPlayer p) anubis.reweigh(p);
    }
}
