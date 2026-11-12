package com.linkyoshi90.harmontide;

import com.linkyoshi90.harmontide.entity.CrowCaches;
import com.linkyoshi90.harmontide.entity.CrowHarpy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/** World events the harpies care about. */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class HarpyEvents {
    private HarpyEvents() {}

    /** Digging up a crow's cache turns up whatever she buried there; if she sees you do it, she won't forget it. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        CrowCaches.Cache cache = CrowCaches.get(level).remove(event.getPos());
        if (cache == null) return;
        Block.popResource(level, event.getPos(), cache.item().copy());
        Player player = event.getPlayer();
        if (level.getEntity(cache.crow()) instanceof com.linkyoshi90.harmontide.entity.girl.Kitsune fox) {   // a kitsune's stash: the game is won
            fox.onStashFound(player);
            return;
        }
        if (level.getEntity(cache.crow()) instanceof CrowHarpy crow && !crow.isOwnedBy(player)
                && crow.distanceToSqr(player) < 24 * 24 && crow.hasLineOfSight(player)) {
            crow.addGrudge(player.getUUID());
            crow.caw(40);
        }
    }
}
