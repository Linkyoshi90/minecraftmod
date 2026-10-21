package com.linkyoshi90.monstergirls;

import com.linkyoshi90.monstergirls.entity.girl.Arachne;
import com.linkyoshi90.monstergirls.entity.girl.Lamia;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.Nekomata;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** World rules the later families bring along (see their field guides). */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class GirlEvents {
    private GirlEvents() {}

    /** Beehives placed near a queen bee win her heart. */
    @SubscribeEvent
    public static void onPlace(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player p && event.getPlacedBlock().getBlock() instanceof net.minecraft.world.level.block.BeehiveBlock) {
            com.linkyoshi90.monstergirls.entity.girl.Bug.onHivePlaced(p, event.getPos());
        }
    }

    /** One night in five a vampire comes to the bed of someone sleeping in the overworld, and drinks a little. */
    @SubscribeEvent
    public static void onWake(net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) || !event.updateLevel()) return;
        net.minecraft.server.level.ServerLevel level = player.serverLevel();
        if (level.dimension() != net.minecraft.world.level.Level.OVERWORLD || player.isCreative() || level.random.nextInt(5) != 0) return;
        var type = com.linkyoshi90.monstergirls.registry.ModGirls.TYPES.get(com.linkyoshi90.monstergirls.entity.girl.GirlSpecies.VAMPIRE_UNDEAD);
        if (type == null || !(type.get().create(level) instanceof com.linkyoshi90.monstergirls.entity.girl.Undead vampire)) return;
        vampire.moveTo(player.getX() + 1.0, player.getY(), player.getZ() + 1.0, level.random.nextFloat() * 360.0F, 0.0F);
        vampire.finalizeSpawn(level, level.getCurrentDifficultyAt(player.blockPosition()), net.minecraft.world.entity.MobSpawnType.EVENT, null);
        level.addFreshEntity(vampire);
        com.linkyoshi90.monstergirls.entity.girl.Undead.drinkFrom(player, vampire);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.vampire_visit", vampire.getDisplayName()), false);
    }

    /** The phantom girl comes for players who have not slept in three nights (checked once a minute at night). */
    @SubscribeEvent
    public static void onLevelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) || level.dimension() != net.minecraft.world.level.Level.OVERWORLD) return;
        if (level.getGameTime() % 1200 != 0 || level.isDay() || level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) return;
        var type = com.linkyoshi90.monstergirls.registry.ModGirls.TYPES.get(com.linkyoshi90.monstergirls.entity.girl.GirlSpecies.PHANTOM_UNDEAD);
        if (type == null) return;
        for (net.minecraft.server.level.ServerPlayer p : level.players()) {
            if (p.isSpectator() || p.isCreative() || !com.linkyoshi90.monstergirls.entity.girl.Undead.sleepless(p) || level.random.nextInt(3) != 0) continue;
            if (com.linkyoshi90.monstergirls.entity.girl.Centaur.dreamWard(p)) continue;
            if (!level.getEntities(type.get(), p.getBoundingBox().inflate(48), e -> true).isEmpty()) continue;
            var girl = type.get().create(level);
            if (girl == null) continue;
            girl.moveTo(p.getX() + level.random.nextInt(9) - 4, p.getY() + 16, p.getZ() + level.random.nextInt(9) - 4, 0.0F, 0.0F);
            if (!level.noCollision(girl)) continue;
            girl.finalizeSpawn(level, level.getCurrentDifficultyAt(p.blockPosition()), net.minecraft.world.entity.MobSpawnType.NATURAL, null);
            level.addFreshEntity(girl);
        }
    }

    /** Vanilla phantoms never come for a player with a tapir (the baku) nearby. */
    @SubscribeEvent
    public static void onPhantoms(net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent event) {
        if (com.linkyoshi90.monstergirls.entity.girl.Centaur.dreamWard(event.getEntity())) {
            event.setResult(net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent.Result.DENY);
        }
    }

    /** Creepers keep away from nekomata, as they do from vanilla cats. */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Creeper creeper) {
            creeper.goalSelector.addGoal(3, new AvoidEntityGoal<>(creeper, Nekomata.class, 6.0F, 1.0, 1.2));
        }
    }

    /** The spider truce: spiders leave alone a player an arachne is devoted to. */
    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        if ((event.getEntity() instanceof Spider || event.getEntity() instanceof CaveSpider)
                && event.getNewAboutToBeSetTarget() instanceof Player player && Arachne.truceFor(player)) {
            event.setCanceled(true);
        }
    }

    /**
     * A devoted lamia beside her player takes half of the damage on her coils. A player who strikes something, or is
     * struck, rallies the lioness pride that loves them.
     */
    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;
        if (victim instanceof Player player) {
            Lamia lamia = Lamia.guardianOf(player);
            if (lamia != null && event.getAmount() > 1.0F && !event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                float half = event.getAmount() / 2;
                event.setAmount(half);
                lamia.hurt(lamia.damageSources().generic(), half * 0.5F);   // her scales soak up some of it
            }
            if (event.getSource().getEntity() instanceof LivingEntity attacker) Nekomata.rallyPride(player, attacker);
        } else if (event.getSource().getEntity() instanceof Player player) {
            Nekomata.rallyPride(player, victim);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Merfolk merfolk) Merfolk.onKill(merfolk, event.getEntity());
    }
}
