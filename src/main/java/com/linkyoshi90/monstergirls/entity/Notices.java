package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;

/**
 * Things a friend notices (2026-11-09). The tool in your hand is nearly worn through ({@link #WORN} of its durability or less): a friend
 * within {@link #NEAR} says so, once per tool. You haven't slept in {@link #SLEEPLESS} ticks (phantoms come after three nights): a friend
 * near says you look exhausted, at most once a night. A creeper starts to hiss at you: a friend near shouts a warning (once per creeper).
 * Behind the friendNotices option, like the other friend lines.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Notices {
    public static final double NEAR = 8;
    public static final float WORN = 0.05F;
    public static final int SLEEPLESS = 3 * 24000 - 6000;
    static final String WORN_TOLD = "WornToolTold", TIRED_TOLD = "TiredToldDay";

    private Notices() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || p.tickCount % 80 != 53 || !(p.level() instanceof ServerLevel level)) return;
        if (!com.linkyoshi90.monstergirls.MonsterGirlsConfig.friendNotices()) return;
        wornTool(level, p);
        tired(level, p, p.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)));
    }

    static final String WARNED = "CreeperWarned";

    @SubscribeEvent
    public static void onCreeper(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.monster.Creeper c && c.tickCount % 3 == 0 && c.getSwellDir() > 0
                && c.getTarget() instanceof ServerPlayer p && c.level() instanceof ServerLevel level && com.linkyoshi90.monstergirls.MonsterGirlsConfig.friendNotices()) {
            creeper(level, c, p);
        }
    }

    /** A creeper hissing at a player: a friend near them shouts (once per creeper). Who did, or null. */
    @Nullable
    public static Mob creeper(ServerLevel level, net.minecraft.world.entity.monster.Creeper c, Player p) {
        if (c.getPersistentData().getBoolean(WARNED)) return null;
        Mob friend = friendNear(level, p);
        if (friend == null || friend == c) return null;
        c.getPersistentData().putBoolean(WARNED, true);
        friend.getLookControl().setLookAt(c);
        p.displayClientMessage(Component.translatable("message.monstergirls.notice.creeper", friend.getDisplayName(),
                Component.translatable("direction.monstergirls." + com.linkyoshi90.monstergirls.item.FriendCompassItem.way(p, c))).withStyle(ChatFormatting.RED), true);
        return friend;
    }

    @Nullable
    static Mob friendNear(ServerLevel level, Player p) {
        for (Mob m : level.getEntitiesOfClass(Mob.class, new AABB(p.blockPosition()).inflate(NEAR),
                m -> m instanceof Bonded b && m.isAlive() && !m.isBaby() && b.getAffection(p) >= HomeLife.FRIEND)) return m;
        return null;
    }

    /** The tool in hand nearly worn through, and a friend to see it: she says so (once per tool). Who did, or null. */
    @Nullable
    public static Mob wornTool(ServerLevel level, Player p) {
        ItemStack tool = p.getMainHandItem();
        if (!tool.isDamageableItem() || tool.getMaxDamage() - tool.getDamageValue() > Math.max(1, tool.getMaxDamage() * WORN)) {
            p.getPersistentData().remove(WORN_TOLD);                           // a sound tool in hand: the next worn one gets its warning
            return null;
        }
        int id = tool.getItem().hashCode() | 1;
        if (p.getPersistentData().getInt(WORN_TOLD) == id) return null;
        Mob friend = friendNear(level, p);
        if (friend == null) return null;
        p.getPersistentData().putInt(WORN_TOLD, id);
        friend.getLookControl().setLookAt(p);
        p.displayClientMessage(Component.translatable("message.monstergirls.notice.worn", friend.getDisplayName(), tool.getHoverName()).withStyle(ChatFormatting.YELLOW), false);
        return friend;
    }

    /** Too long without sleep, and a friend near: she tells them to rest (once a night). Who did, or null. */
    @Nullable
    public static Mob tired(ServerLevel level, Player p, int sinceRest) {
        long day = level.getDayTime() / 24000L + 1;
        if (sinceRest < SLEEPLESS || p.getPersistentData().getLong(TIRED_TOLD) == day) return null;
        Mob friend = friendNear(level, p);
        if (friend == null) return null;
        p.getPersistentData().putLong(TIRED_TOLD, day);
        p.displayClientMessage(Component.translatable(Marriage.married(friend, p) ? "message.monstergirls.notice.tired_wife" : "message.monstergirls.notice.tired",
                friend.getDisplayName()).withStyle(ChatFormatting.YELLOW), false);
        return friend;
    }
}
