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
    /** An aegis boar takes a share of every blow meant for her friend (Armoured.shield). */
    /** A befriended halberelk near a player who cuts a log fells the rest of the tree. */
    @SubscribeEvent
    public static void onLogCut(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level && !event.isCanceled()) {
            com.linkyoshi90.monstergirls.entity.girl.Centaur.fell(level, event.getPlayer(), event.getPos(), event.getState());
        }
    }

    /** The valkyrie: a befriended one within 32 blocks gathers up what her fallen friend drops, to give back later. */
    @SubscribeEvent
    public static void onPlayerDrops(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer p) || event.getDrops().isEmpty()) return;
        for (var v : p.serverLevel().getEntitiesOfClass(com.linkyoshi90.monstergirls.entity.girl.Elf.class, p.getBoundingBox().inflate(32),
                e -> e.getSpecies() == com.linkyoshi90.monstergirls.entity.girl.GirlSpecies.VALKYRIE_ELF && e.isAlive())) {
            if (v.gatherFallen(p, event.getDrops())) return;
        }
    }

    /** Like a tamed wolf's owner, everyone who befriended a girl, harpy or frog hears how she died. */
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
    public static void onFriendDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof com.linkyoshi90.monstergirls.entity.Bonded bonded)
                || !(event.getEntity().level() instanceof net.minecraft.server.level.ServerLevel level)
                || !level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_SHOWDEATHMESSAGES)) return;
        net.minecraft.network.chat.Component message = event.getEntity().getCombatTracker().getDeathMessage();
        for (net.minecraft.server.level.ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            if (bonded.getAffection(p) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION) p.sendSystemMessage(message);
        }
    }

    /** A befriended Behemoth near a player: they cannot be knocked back. */
    @SubscribeEvent
    public static void onKnockback(net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player p && !p.level().isClientSide
                && com.linkyoshi90.monstergirls.entity.girl.Devil.anchored(p)) event.setCanceled(true);
    }

    /** The qilin treads so lightly that farmland never notices. */
    @SubscribeEvent
    public static void onTrample(net.neoforged.neoforge.event.level.BlockEvent.FarmlandTrampleEvent event) {
        if (event.getEntity() instanceof com.linkyoshi90.monstergirls.entity.girl.Centaur c
                && c.getSpecies() == com.linkyoshi90.monstergirls.entity.girl.GirlSpecies.QILIN_CENTAUR) event.setCanceled(true);
    }

    /** A befriended karakasa obake near a player catches the projectiles about to hit them. */
    @SubscribeEvent
    public static void onProjectileImpact(net.neoforged.neoforge.event.entity.ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult hit && hit.getEntity() instanceof net.minecraft.world.entity.player.Player p
                && com.linkyoshi90.monstergirls.entity.girl.Devil.shelter(p, event.getProjectile())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player p && !p.level().isClientSide) {
            event.setAmount(com.linkyoshi90.monstergirls.entity.girl.Armoured.shield(p, event.getSource(), event.getAmount()));
            if (com.linkyoshi90.monstergirls.entity.girl.Undead.takesTheBlow(p, event.getAmount())) event.setAmount(0.0F);
        }
    }

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

    /**
     * A naga keeps strangers from the treasure she guards (opening or breaking an unopened structure chest near her),
     * Anubis everyone she has not weighed light, and one unopened loot chest in eight is a mimic.
     */
    @SubscribeEvent
    public static void onOpenChest(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        com.linkyoshi90.monstergirls.entity.girl.Lamia naga = com.linkyoshi90.monstergirls.entity.girl.Lamia.guardOf(event.getLevel(), event.getPos(), event.getEntity());
        if (naga != null) {
            naga.warnOff(event.getEntity());
            event.setCanceled(true);
            return;
        }
        com.linkyoshi90.monstergirls.entity.girl.Canid anubis = com.linkyoshi90.monstergirls.entity.girl.Canid.tombGuardOf(event.getLevel(), event.getPos(), event.getEntity());
        if (anubis != null) {                                               // Anubis lets only light hearts into the tomb
            anubis.warnOff(event.getEntity());
            event.setCanceled(true);
            return;
        }
        com.linkyoshi90.monstergirls.entity.girl.Undead mummy = com.linkyoshi90.monstergirls.entity.girl.Undead.mummyGuardOf(event.getLevel(), event.getPos(), event.getEntity());
        if (mummy != null) {                                                // and a mummy keeps strangers from it too
            mummy.warnOffTomb(event.getEntity());
            event.setCanceled(true);
            return;
        }
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level && !event.getEntity().isCreative()
                && com.linkyoshi90.monstergirls.entity.girl.SlimeGirl.isMimicChest(level, event.getPos())
                && com.linkyoshi90.monstergirls.entity.girl.SlimeGirl.springMimic(level, event.getPos(), event.getEntity()) != null) {
            event.setCanceled(true);                                        // it was a mimic
        }
    }

    @SubscribeEvent
    public static void onBreakChest(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof net.minecraft.world.level.Level level) || level.isClientSide()) return;
        com.linkyoshi90.monstergirls.entity.girl.Lamia naga = com.linkyoshi90.monstergirls.entity.girl.Lamia.guardOf(level, event.getPos(), event.getPlayer());
        if (naga != null) {
            naga.warnOff(event.getPlayer());
            event.setCanceled(true);
            return;
        }
        com.linkyoshi90.monstergirls.entity.girl.Canid anubis = com.linkyoshi90.monstergirls.entity.girl.Canid.tombGuardOf(level, event.getPos(), event.getPlayer());
        if (anubis != null) {
            anubis.warnOff(event.getPlayer());
            event.setCanceled(true);
            return;
        }
        if (event.getState().is(net.minecraft.tags.BlockTags.LOGS) && level instanceof net.minecraft.server.level.ServerLevel server) {   // the leshy counts every tree
            com.linkyoshi90.monstergirls.entity.girl.Flora leshy = com.linkyoshi90.monstergirls.entity.girl.Flora.leshyNear(level, event.getPos());
            if (leshy != null) leshy.onLogCut(server, event.getPlayer());
        }
    }

    /** A sapling planted near a leshy counts in the planter's favour. */
    @SubscribeEvent
    public static void onPlaceSapling(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel server) || !(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)
                || !event.getPlacedBlock().is(net.minecraft.tags.BlockTags.SAPLINGS)) return;
        com.linkyoshi90.monstergirls.entity.girl.Flora leshy = com.linkyoshi90.monstergirls.entity.girl.Flora.leshyNear(server, event.getPos());
        if (leshy != null) leshy.onSaplingPlanted(server, player);
    }

    /** Loud noises startle black bears: explosions, bells, goat horns. */
    @SubscribeEvent
    public static void onLoudNoise(net.neoforged.neoforge.event.VanillaGameEvent event) {
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) return;
        var e = event.getVanillaEvent();
        boolean boom = e.is(net.minecraft.world.level.gameevent.GameEvent.EXPLODE);
        boolean horn = e.is(net.minecraft.world.level.gameevent.GameEvent.INSTRUMENT_PLAY);
        boolean bell = e.is(net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE)
                && level.getBlockState(net.minecraft.core.BlockPos.containing(event.getEventPosition())).getBlock() instanceof net.minecraft.world.level.block.BellBlock;
        if (boom || horn || bell) com.linkyoshi90.monstergirls.entity.girl.Canid.startle(level, event.getEventPosition(), boom);
    }

    /** A wild ahuizotl near the hook steals the catch off your line. */
    @SubscribeEvent
    public static void onFished(net.neoforged.neoforge.event.entity.player.ItemFishedEvent event) {
        if (!(event.getEntity().level() instanceof net.minecraft.server.level.ServerLevel level)) return;
        com.linkyoshi90.monstergirls.entity.girl.Merfolk thief = com.linkyoshi90.monstergirls.entity.girl.Merfolk.thiefNear(event.getHookEntity(), event.getEntity());
        if (thief == null) return;
        thief.stealCatch(level, event.getEntity());
        event.setCanceled(true);
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
