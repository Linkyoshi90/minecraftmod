package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Travelling together: friends told to follow a player (stick or sneaking whistle) who are within {@link #REACH}
 * when that player goes through a portal (or is sent to another dimension) arrive beside them on the other side,
 * instead of being left behind. Config "travelTogether".
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Travelling {
    public static final double REACH = 16;

    /** Who was standing by each player as they left, and the level they left. */
    private record Party(ResourceKey<Level> from, List<UUID> members) {}

    private static final Map<UUID, Party> LEAVING = new HashMap<>();

    private Travelling() {}

    /** The friends who come along: following this player, grown or not, within reach, not riding or carrying anything. */
    public static List<Mob> party(ServerLevel level, Player player) {
        List<Mob> out = new ArrayList<>();
        for (Mob m : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(REACH), Mob::isAlive)) {
            if (!(m instanceof Bonded b) || m.distanceToSqr(player) > REACH * REACH || m.isPassenger() || m.isVehicle()) continue;
            if (Orders.current(b) == Orders.Command.FOLLOW && player.getUUID().equals(b.getOrders().commanderId())) out.add(m);
        }
        return out;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLeave(EntityTravelToDimensionEvent event) {
        if (event.isCanceled() || !HarmontideConfig.travelTogether() || !(event.getEntity() instanceof ServerPlayer player)) return;
        List<UUID> ids = party(player.serverLevel(), player).stream().map(Entity::getUUID).toList();
        if (ids.isEmpty()) LEAVING.remove(player.getUUID());
        else LEAVING.put(player.getUUID(), new Party(player.level().dimension(), ids));
    }

    @SubscribeEvent
    public static void onArrive(PlayerEvent.PlayerChangedDimensionEvent event) {
        Party party = LEAVING.remove(event.getEntity().getUUID());
        if (party == null || !(event.getEntity() instanceof ServerPlayer player) || !party.from().equals(event.getFrom())) return;
        ServerLevel from = player.server.getLevel(party.from());
        if (from == null) return;
        int i = 0;
        for (UUID id : party.members()) {
            if (!(from.getEntity(id) instanceof Mob m) || !m.isAlive()) continue;
            double angle = Math.PI * 2 * i++ / party.members().size();            // a little ring around the player, not all in one spot
            Vec3 at = player.position().add(Math.cos(angle) * 0.8, 0, Math.sin(angle) * 0.8);
            m.getNavigation().stop();
            m.changeDimension(new DimensionTransition(player.serverLevel(), at, Vec3.ZERO, player.getYRot(), 0.0F, DimensionTransition.DO_NOTHING));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerLoggedOutEvent event) {
        LEAVING.remove(event.getEntity().getUUID());
    }
}
