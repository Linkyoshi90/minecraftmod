package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.HearthBlockEntity;
import com.linkyoshi90.harmontide.entity.Expeditions;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.Rarity;
import com.linkyoshi90.harmontide.registry.ModGirls;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Market day: every few days, on the morning the hearth's owner is home, a travelling girl of a family that doesn't live
 * there sets up by the hearth. An emerald buys a bundle of her country's finds (Expeditions.land), now and then with one
 * of her presents, while her stock lasts. At dusk the next day she moves on, unless someone has made a friend of her:
 * then she stays, like any girl. The way to meet girls from far away, without anyone wandering off on an expedition.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Market {
    public static final int EVERY = 6, ON = 3, STOCK = 4, PRICE = 1, STAY = 30000;
    public static final String LEAVES = "TraderLeaves", STOCK_KEY = "TraderStock", ADVANCEMENT = "market_day";

    private Market() {
    }

    /** Whether it is market day (by the world's day count). */
    public static boolean marketDay(long dayTime) {
        return Math.floorMod(dayTime / 24000L, (long) EVERY) == ON;
    }

    public static boolean trader(AbstractMonsterGirl girl) {
        return girl.getPersistentData().contains(LEAVES);
    }

    public static int stock(AbstractMonsterGirl girl) {
        return girl.getPersistentData().getInt(STOCK_KEY);
    }

    /** The morning hook (HearthBlockEntity.serverTick): on market day, with the owner home, a trader may come. */
    public static void morning(ServerLevel level, HearthBlockEntity hearth) {
        if (!HarmontideConfig.travellingMarket() || !marketDay(level.getDayTime()) || hearth.owner() == null) return;
        Player owner = level.getPlayerByUUID(hearth.owner());
        if (owner == null || owner.distanceToSqr(hearth.getBlockPos().getCenter()) > 64 * 64 || hearth.residentCount() == 0) return;
        arrive(level, hearth.getBlockPos(), owner);
    }

    /** Who could come: grown girls of a family not living at the hearth, land folk, nothing hostile, nothing legendary. */
    public static List<GirlSpecies> candidates(ServerLevel level, BlockPos hearth) {
        Set<GirlSpecies.Family> home = EnumSet.noneOf(GirlSpecies.Family.class);
        for (AbstractMonsterGirl g : Festivals.guests(level, hearth)) home.add(g.getSpecies().family());
        List<GirlSpecies> from = new ArrayList<>();
        for (GirlSpecies s : GirlSpecies.values()) {
            if (!home.contains(s.family()) && s.family() != GirlSpecies.Family.MERFOLK && s.family() != GirlSpecies.Family.MOLLUSK && s.family() != GirlSpecies.Family.CRUSTACEAN
                    && s.category() == net.minecraft.world.entity.MobCategory.CREATURE
                    && Rarity.of(s.id()) != Rarity.Tier.LEGENDARY) from.add(s);   // nothing hostile, nothing legendary
        }
        return from;
    }

    /** Of these, the kinds the owner has never met (the market is a way to fill in the journal's tree of life). */
    public static List<GirlSpecies> unmet(List<GirlSpecies> from, Player owner) {
        Set<String> met = com.linkyoshi90.harmontide.entity.Journal.met(owner);
        List<GirlSpecies> out = new ArrayList<>();
        for (GirlSpecies s : from) if (!met.contains(s.id())) out.add(s);
        return out;
    }

    /** A trader sets up by the hearth (three times in four a kind the owner has never met, while there are any). She, or null. */
    @Nullable
    public static AbstractMonsterGirl arrive(ServerLevel level, BlockPos hearth, Player owner) {
        List<GirlSpecies> from = candidates(level, hearth);
        if (from.isEmpty()) return null;
        List<GirlSpecies> fresh = unmet(from, owner);
        if (!fresh.isEmpty() && level.random.nextInt(4) != 0) from = fresh;
        AbstractMonsterGirl girl = ModGirls.type(from.get(level.random.nextInt(from.size()))).create(level);
        if (girl == null) return null;
        BlockPos at = spot(level, hearth, girl);
        girl.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360F, 0);
        girl.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
        girl.setBaby(false);
        girl.getPersistentData().putLong(LEAVES, level.getGameTime() + STAY);
        girl.getPersistentData().putInt(STOCK_KEY, STOCK);
        girl.setPersistenceRequired();
        if (!level.addFreshEntity(girl)) return null;
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, girl.getX(), girl.getY() + 1, girl.getZ(), 10, 0.5, 0.5, 0.5, 0);
        owner.displayClientMessage(Component.translatable("message.harmontide.market.arrives", girl.getDisplayName()), false);
        return girl;
    }

    static BlockPos spot(ServerLevel level, BlockPos near, AbstractMonsterGirl girl) {
        for (int i = 0; i < 12; i++) {
            int x = near.getX() + level.random.nextInt(11) - 5, z = near.getZ() + level.random.nextInt(11) - 5;
            BlockPos p = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (Math.abs(p.getY() - near.getY()) <= 4 && level.noCollision(girl.getType().getSpawnAABB(p.getX() + 0.5, p.getY(), p.getZ() + 0.5))) return p;
        }
        return near.above();
    }

    /** What an emerald buys: two or three of her country's finds, and one time in three a present. */
    public static List<ItemStack> bundle(AbstractMonsterGirl girl, Player player) {
        List<ItemStack> out = new ArrayList<>();
        Item[] land = Expeditions.land(girl.getSpecies().family());
        for (int i = 0, n = 2 + girl.getRandom().nextInt(2); i < n; i++) out.add(new ItemStack(land[girl.getRandom().nextInt(land.length)], 1 + girl.getRandom().nextInt(3)));
        if (girl.getRandom().nextInt(3) == 0) {
            ItemStack gift = girl.rollGift(player);
            if (!gift.isEmpty()) out.add(gift);
        }
        return out;
    }

    /** A sale: an emerald from the stack for a bundle. Whether she sold. */
    public static boolean sell(ServerLevel level, AbstractMonsterGirl girl, Player player, ItemStack emeralds) {
        if (!trader(girl) || !emeralds.is(Items.EMERALD) || emeralds.getCount() < PRICE) return false;
        if (stock(girl) <= 0) {
            player.displayClientMessage(Component.translatable("message.harmontide.market.sold_out", girl.getDisplayName()), true);
            return false;
        }
        emeralds.consume(PRICE, player);
        girl.getPersistentData().putInt(STOCK_KEY, stock(girl) - 1);
        for (ItemStack s : bundle(girl, player)) if (!player.addItem(s)) player.drop(s, false);
        girl.addAffection(player, 2);                                              // a good customer is half a friend
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, girl.getX(), girl.getEyeY(), girl.getZ(), 6, 0.4, 0.3, 0.4, 0);
        level.playSound(null, girl.blockPosition(), SoundEvents.VILLAGER_TRADE, SoundSource.NEUTRAL, 0.8F, 1.3F);
        if (player instanceof ServerPlayer sp) {
            AdvancementHolder a = sp.server.getAdvancements().get(HarmontideMod.id(ADVANCEMENT));
            if (a != null) sp.getAdvancements().award(a, "done");
        }
        return true;
    }

    /** At her time she moves on, unless she has a friend now. Whether she left. */
    public static boolean maybeLeave(ServerLevel level, AbstractMonsterGirl girl) {
        if (!trader(girl) || level.getGameTime() < girl.getPersistentData().getLong(LEAVES)) return false;
        girl.getPersistentData().remove(LEAVES);
        girl.getPersistentData().remove(STOCK_KEY);
        for (Player p : level.players()) {
            if (girl.getAffection(p) >= Orders.AFFECTION) return false;                // she stays, for her friend
        }
        level.sendParticles(ParticleTypes.POOF, girl.getX(), girl.getY() + 0.5, girl.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
        girl.discard();
        return true;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof AbstractMonsterGirl girl) || !trader(girl) || !event.getItemStack().is(Items.EMERALD)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getLevel() instanceof ServerLevel level) sell(level, girl, event.getEntity(), event.getItemStack());
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof AbstractMonsterGirl girl && girl.tickCount % 100 == 0 && girl.level() instanceof ServerLevel level) maybeLeave(level, girl);
    }
}
