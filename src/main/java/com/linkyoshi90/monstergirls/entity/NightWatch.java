package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeping watch, and remembering. The night watch: a girl on patrol for a player who is away from her post (further than
 * {@link #AWAY}) calls out when a monster comes within {@link #POST} of it at night, at most every two minutes, so they know home needs
 * them. Remembrance: when a grown friend dies, a poppy with her name falls where she did, and her friends' journals keep her name.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class NightWatch {
    public static final double POST = 16, AWAY = 32, REACH = 160;
    public static final long CALL_WAIT = 2400;
    public static final String CALLED = "WatchCalledAt", REMEMBERED = "Remembered";

    private NightWatch() {
    }

    /** The nearest monster to her post, or null. */
    @Nullable
    public static Mob intruder(ServerLevel level, BlockPos post) {
        Mob best = null;
        double bestDist = Double.MAX_VALUE;
        for (Mob m : level.getEntitiesOfClass(Mob.class, new AABB(post).inflate(POST), m -> m instanceof Enemy && m.isAlive() && !(m instanceof Bonded))) {
            double d = m.distanceToSqr(post.getX() + 0.5, post.getY(), post.getZ() + 0.5);
            if (d < bestDist) {
                bestDist = d;
                best = m;
            }
        }
        return best;
    }

    /** Every five seconds at night: guards of theirs who see trouble at their post call out. How many called. */
    public static int watch(ServerLevel level, ServerPlayer p) {
        if (!MonsterGirlsConfig.comfort() || !level.isNight()) return 0;
        int n = 0;
        for (AbstractMonsterGirl g : level.getEntitiesOfClass(AbstractMonsterGirl.class, p.getBoundingBox().inflate(REACH), AbstractMonsterGirl::isAlive)) {
            if (g.getOrders().command(g) != Orders.Command.PATROL || g.getOrders().anchor() == null || g.getOrders().commander(g) != p || g.isBaby()) continue;
            BlockPos post = g.getOrders().anchor();
            if (p.blockPosition().closerThan(post, AWAY) || level.getGameTime() < g.getPersistentData().getLong(CALLED)) continue;
            Mob foe = intruder(level, post);
            if (foe == null) continue;
            g.getPersistentData().putLong(CALLED, level.getGameTime() + CALL_WAIT);
            p.displayClientMessage(Component.translatable("message.monstergirls.night_watch", g.getDisplayName(), foe.getType().getDescription(),
                    post.getX(), post.getY(), post.getZ()).withStyle(ChatFormatting.RED), false);
            n++;
        }
        return n;
    }

    /** A grown friend gone: the poppy where she fell, and her name in her friends' journals. How many friends she had. */
    public static int remember(ServerLevel level, Mob mob) {
        if (mob.isBaby() || !(mob instanceof Bonded b)) return 0;
        List<ServerPlayer> friends = new ArrayList<>();
        for (ServerPlayer p : level.players()) if (b.getAffection(p) >= HomeLife.FRIEND) friends.add(p);
        if (friends.isEmpty()) return 0;
        Component name = mob.getDisplayName();
        ItemStack poppy = new ItemStack(Items.POPPY);
        poppy.set(DataComponents.CUSTOM_NAME, Component.translatable("item.monstergirls.memorial_poppy", name).withStyle(ChatFormatting.RESET));
        poppy.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.monstergirls.memorial_poppy.lore", mob.getType().getDescription()).withStyle(ChatFormatting.GRAY))));
        level.addFreshEntity(new ItemEntity(level, mob.getX(), mob.getY() + 0.3, mob.getZ(), poppy));
        for (ServerPlayer p : friends) {
            CompoundTag data = p.getData(ModAttachments.JOURNAL);
            ListTag list = data.getList(REMEMBERED, Tag.TAG_STRING);
            list.add(StringTag.valueOf(name.getString()));
            data.put(REMEMBERED, list);
            p.setData(ModAttachments.JOURNAL, data);                                     // (they hear how she died: GirlEvents.onFriendDeath)
        }
        return friends.size();
    }

    public static List<String> remembered(Player p) {
        List<String> out = new ArrayList<>();
        for (Tag t : p.getData(ModAttachments.JOURNAL).getList(REMEMBERED, Tag.TAG_STRING)) out.add(t.getAsString());
        return out;
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)                  // after anything that brings her back
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity e = event.getEntity();
        if (!event.isCanceled() && e instanceof Mob mob && e.level() instanceof ServerLevel level) remember(level, mob);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer p && p.level() instanceof ServerLevel level && p.tickCount % 100 == 29) watch(level, p);
    }
}
