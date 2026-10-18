package com.linkyoshi90.harpies.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

final class HarpyTestUtil {
    /**
     * Every test builds on this template: a 15x8x15 grass platform (see tools/make_test_structures.py).
     * The test origin sits one block below the template, so the grass floor is at relative y=1 and mobs stand at y=2.
     */
    static final String PLATFORM = "platform";

    private HarpyTestUtil() {}

    /**
     * A real, survival-mode player standing in the test area. Unlike GameTestHelper's mock players this one is in
     * the level's player list, so mob AI (nearest-player lookups) can see it. Remove it with {@link #remove}.
     */
    static ServerPlayer survivalPlayer(GameTestHelper helper, BlockPos relativePos) {
        MinecraftServer server = helper.getLevel().getServer();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-player"), false);
        ServerPlayer player = new ServerPlayer(server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        server.getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 pos = helper.absoluteVec(Vec3.atBottomCenterOf(relativePos));
        player.teleportTo(helper.getLevel(), pos.x, pos.y, pos.z, 0.0F, 0.0F);
        return player;
    }

    static void remove(ServerPlayer player) {
        player.getServer().getPlayerList().remove(player);
    }

    /** TEMP: prints the mob's state every 100 ticks, tagged with the test name. */
    static void trace(GameTestHelper helper, String name, net.minecraft.world.entity.Mob mob) {
        helper.onEachTick(() -> {
            if (helper.getTick() % 100 != 0) return;
            System.out.println("TRACE " + name + " t=" + helper.getTick() + " pos=" + helper.relativeVec(mob.position()) + " alive=" + mob.isAlive()
                    + " hand=" + mob.getMainHandItem() + " day=" + helper.getLevel().getDayTime() + " isDay=" + helper.getLevel().isDay()
                    + " goals=" + mob.goalSelector.getAvailableGoals().stream().filter(g -> g.isRunning()).map(g -> g.getGoal().getClass().getSimpleName()).toList());
        });
    }

    static void check(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }
}
