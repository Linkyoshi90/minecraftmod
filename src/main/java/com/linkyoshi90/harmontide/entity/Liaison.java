package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.registry.ModItems;
import com.linkyoshi90.harmontide.registry.ModVillagers;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The exchange liaison: a villager who takes the exchange desk as her workstation and looks after the monster girls' homestays with
 * the villages. She issues the homestay permit and sells clothes and keepsakes; she buys paper (there is always paperwork).
 * <p>
 * A player carrying a permit vouches for their friends: iron golems leave them be, and villagers within {@link #SEEN} who see the
 * player with a friend nearby think better of them (a little positive gossip every {@link #EVERY} ticks), which lowers their prices
 * and spreads through the village as gossip does.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Liaison {
    public static final double SEEN = 16, VOUCH = 64;
    public static final int EVERY = 600, GOOD_WORD = 5;

    private Liaison() {
    }

    public static boolean hasPermit(Player p) {
        return p.getInventory().hasAnyMatching(s -> s.is(ModItems.HOMESTAY_PERMIT.get()));
    }

    // --- her trades ----------------------------------------------------------------------------------------------------

    private static VillagerTrades.ItemListing sell(int emeralds, Supplier<? extends Item> item, int uses, int xp) {
        return (trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), new ItemStack(item.get()), uses, xp, 0.05F);
    }

    private static VillagerTrades.ItemListing sell(int emeralds, Item extra, Supplier<? extends Item> item, int uses, int xp) {
        return (trader, random) -> new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), Optional.of(new ItemCost(extra, 1)), new ItemStack(item.get()), uses, xp, 0.05F);
    }

    private static VillagerTrades.ItemListing buy(Item item, int count, int uses, int xp) {
        return (trader, random) -> new MerchantOffer(new ItemCost(item, count), new ItemStack(Items.EMERALD), uses, xp, 0.05F);
    }

    @SubscribeEvent
    public static void onTrades(VillagerTradesEvent event) {
        if (event.getType() != ModVillagers.LIAISON.get()) return;
        Int2ObjectMap<List<VillagerTrades.ItemListing>> t = event.getTrades();
        t.get(1).add(sell(1, ModItems.HOMESTAY_PERMIT, 4, 2));
        t.get(1).add(buy(Items.PAPER, 24, 16, 2));
        t.get(2).add(sell(3, ModItems.FRIENDSHIP_BRACELET, 8, 5));
        t.get(2).add(sell(2, ModItems.LOVE_LETTER, 8, 5));
        t.get(2).add((trader, random) -> new net.minecraft.world.item.trading.MerchantOffer(
                new net.minecraft.world.item.trading.ItemCost(Items.EMERALD, 1), new net.minecraft.world.item.ItemStack(ModItems.CONDOM.get(), 8), 16, 4, 0.05F));
        t.get(2).add(buy(Items.SUGAR, 16, 12, 10));
        t.get(3).add(sell(6, ModItems.RAINCOAT, 4, 10));
        t.get(3).add(sell(8, ModItems.WINTER_COAT, 4, 10));
        t.get(3).add(sell(8, ModItems.YUKATA, 4, 10));
        t.get(3).add(sell(8, ModItems.MAID_APRON, 4, 10));
        t.get(4).add(sell(10, ModItems.WHISTLE, 3, 15));
        t.get(4).add(sell(12, ModItems.FRIEND_COMPASS, 3, 15));
        t.get(5).add(sell(20, Items.GOLD_INGOT, ModItems.WEDDING_RING, 2, 30));
    }

    // --- the permit's good word ----------------------------------------------------------------------------------------

    /** Whether a player with a permit nearby vouches for this friend of theirs. */
    public static boolean vouchedFor(ServerLevel level, LivingEntity mob) {
        if (!(mob instanceof Bonded b)) return false;
        for (Player p : level.players()) {
            if (p.distanceToSqr(mob) <= VOUCH * VOUCH && b.getAffection(p) >= HomeLife.FRIEND && hasPermit(p)) return true;
        }
        return false;
    }

    /** Iron golems leave a vouched-for friend alone. */
    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof IronGolem && event.getNewAboutToBeSetTarget() != null && event.getEntity().level() instanceof ServerLevel level
                && vouchedFor(level, event.getNewAboutToBeSetTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % EVERY == 37 && event.getEntity().level() instanceof ServerLevel level) goodWord(level, event.getEntity());
    }

    /** Villagers who see the player, permit in pocket, with a friend nearby think better of them. How many. */
    public static int goodWord(ServerLevel level, Player p) {
        if (!hasPermit(p) || p.isSpectator()) return 0;
        if (HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(SEEN)).isEmpty()) return 0;
        int told = 0;
        for (Villager v : level.getEntitiesOfClass(Villager.class, p.getBoundingBox().inflate(SEEN), Entity::isAlive)) {
            if (v.isBaby()) continue;
            v.getGossips().add(p.getUUID(), GossipType.MINOR_POSITIVE, GOOD_WORD);
            told++;
        }
        return told;
    }

    /** For tests and the journal: whether a mob is a liaison. */
    public static boolean isLiaison(Mob mob) {
        return mob instanceof Villager v && v.getVillagerData().getProfession() == ModVillagers.LIAISON.get();
    }
}
