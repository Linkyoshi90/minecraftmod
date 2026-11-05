package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import com.linkyoshi90.monstergirls.registry.ModGirls;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Expeditions: her friend (friendship 10 or more) gives a grown girl an empty map and she sets off. She is gone for a
 * Minecraft day (kept on her friend meanwhile, so nothing can befall her), then comes back to her hearth if she has
 * one (Settlement), or to her friend, with what she found: three of her own presents, a few finds of her kind's
 * country, now and then an emerald or a diamond, and one time in five a new girl of her family who came along with
 * her and is already her friend's friend.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Expeditions {
    public static final long DURATION = 24000L;
    /** One in this many expeditions brings a new girl home. */
    public static final int COMPANION_CHANCE = 5;

    private Expeditions() {
    }

    /** Her friend hands her an empty map: she sets off. Whether she went. */
    public static boolean send(AbstractMonsterGirl girl, Player player, ItemStack map) {
        if (!(girl.level() instanceof ServerLevel level) || girl.isBaby() || girl.getAffection(player) < Orders.AFFECTION || girl.isPassenger() || girl.isLeashed()) return false;
        CompoundTag saved = new CompoundTag();
        if (!girl.saveAsPassenger(saved)) return false;
        CompoundTag e = new CompoundTag();
        e.put("Girl", saved);
        long length = com.linkyoshi90.monstergirls.entity.girl.Roles.expedition(girl, DURATION);   // a scout is quicker
        e.putLong("Back", level.getGameTime() + length);
        e.putLong("Length", length);
        e.putString("Dim", level.dimension().location().toString());
        CompoundTag data = player.getData(ModAttachments.EXPEDITIONS);
        ListTag list = data.getList("Away", Tag.TAG_COMPOUND);
        list.add(e);
        data.put("Away", list);
        player.setData(ModAttachments.EXPEDITIONS, data);
        map.consume(1, player);
        level.playSound(null, girl.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.NEUTRAL, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.monstergirls.expedition_set_off", girl.getDisplayName()), true);
        girl.discard();
        return true;
    }

    /** How many of this player's girls are away. */
    public static int away(Player player) {
        return player.getData(ModAttachments.EXPEDITIONS).getList("Away", Tag.TAG_COMPOUND).size();
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 100 == 0) {
            Letters.writeDue(player);
            returnDue(player, false);
            Scouting.returnDue(player, false);
        }
    }

    /** Brings home every girl whose day is up (or all of them, with {@code now}). The girls who came back. */
    public static List<AbstractMonsterGirl> returnDue(ServerPlayer player, boolean now) {
        List<AbstractMonsterGirl> back = new ArrayList<>();
        CompoundTag data = player.getData(ModAttachments.EXPEDITIONS);
        ListTag list = data.getList("Away", Tag.TAG_COMPOUND);
        if (list.isEmpty()) return back;
        ServerLevel level = player.serverLevel();
        ListTag still = new ListTag();
        for (Tag t : list) {
            CompoundTag e = (CompoundTag) t;
            boolean due = now || level.getGameTime() >= e.getLong("Back");
            AbstractMonsterGirl girl = due && level.dimension().location().toString().equals(e.getString("Dim")) ? comeHome(level, player, e.getCompound("Girl")) : null;
            if (girl != null) back.add(girl);
            else still.add(e);
        }
        data.put("Away", still);
        player.setData(ModAttachments.EXPEDITIONS, data);
        return back;
    }

    @Nullable
    private static AbstractMonsterGirl comeHome(ServerLevel level, Player player, CompoundTag saved) {
        Entity entity = EntityType.loadEntityRecursive(saved, level, x -> x);
        if (!(entity instanceof AbstractMonsterGirl girl)) return null;
        BlockPos at = arrival(level, player, girl);
        girl.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, girl.getRandom().nextFloat() * 360F, 0);
        girl.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        if (!level.addFreshEntity(girl)) return null;
        com.linkyoshi90.monstergirls.entity.Missing.forgive(girl, player);
        for (ItemStack s : finds(girl, player)) girl.spawnAtLocation(s);
        if (girl.getRandom().nextInt(COMPANION_CHANCE) == 0) companion(level, player, girl);
        girl.hearts(level, 5);
        level.playSound(null, at, SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.6F, 1.4F);
        player.displayClientMessage(Component.translatable("message.monstergirls.expedition_back", girl.getDisplayName()), false);
        return girl;
    }

    /** Beside her hearth when it is loaded, else a little way from her friend, on the ground. */
    static BlockPos arrival(ServerLevel level, Player player, net.minecraft.world.entity.Mob girl) {
        BlockPos home = girl instanceof AbstractMonsterGirl g ? g.homeHearth() : null;
        BlockPos near = home != null && level.isLoaded(home) ? home : player.blockPosition();
        RandomSource r = girl.getRandom();
        for (int i = 0; i < 10; i++) {
            int x = near.getX() + r.nextInt(9) - 4, z = near.getZ() + r.nextInt(9) - 4;
            BlockPos p = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (Math.abs(p.getY() - near.getY()) <= 6 && level.noCollision(girl.getType().getSpawnAABB(p.getX() + 0.5, p.getY(), p.getZ() + 0.5))) return p;
        }
        return player.blockPosition();
    }

    /** What she brings back. */
    public static List<ItemStack> finds(AbstractMonsterGirl girl, Player player) {
        RandomSource r = girl.getRandom();
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ItemStack gift = girl.rollGift(player);
            if (!gift.isEmpty()) out.add(gift);
        }
        Item[] land = land(girl.getSpecies().family());
        for (int i = 0, n = 2 + r.nextInt(3); i < n; i++) out.add(new ItemStack(land[r.nextInt(land.length)], 1 + r.nextInt(4)));
        if (com.linkyoshi90.monstergirls.entity.girl.Roles.is(girl, com.linkyoshi90.monstergirls.entity.girl.Roles.Role.SCOUT)) {
            out.add(new ItemStack(land[r.nextInt(land.length)], 1 + r.nextInt(4)));      // a scout finds a little more
        }
        if (r.nextInt(8) == 0) out.add(new ItemStack(Items.EMERALD, 2 + r.nextInt(3)));
        if (r.nextInt(25) == 0) out.add(new ItemStack(Items.DIAMOND));
        return out;
    }

    /** The finds of her kind's country. */
    public static Item[] land(GirlSpecies.Family family) {
        return switch (family) {
            case MERFOLK, MOLLUSK, CRUSTACEAN -> new Item[]{Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS, Items.KELP, Items.SEA_PICKLE, Items.COD, Items.NAUTILUS_SHELL};
            case UNDEAD, DEVIL, RIDER -> new Item[]{Items.BONE, Items.CANDLE, Items.AMETHYST_SHARD, Items.SOUL_TORCH, Items.ROTTEN_FLESH};
            case FLORA, ELF, BUG -> new Item[]{Items.OAK_SAPLING, Items.BIRCH_SAPLING, Items.APPLE, Items.HONEYCOMB, Items.SWEET_BERRIES, Items.GLOW_BERRIES};
            case LIZARDFOLK, LAMIA, DRAGON, THEROPOD -> new Item[]{Items.CACTUS, Items.GOLD_NUGGET, Items.TURTLE_SCUTE, Items.DEAD_BUSH, Items.FIRE_CHARGE};
            case NEKOMATA, KITSUNE -> new Item[]{Items.STRING, Items.SALMON, Items.SWEET_BERRIES, Items.CHERRY_SAPLING, Items.RABBIT_HIDE};
            case SLIME -> new Item[]{Items.SLIME_BALL, Items.CLAY_BALL, Items.MAGMA_CREAM, Items.HONEY_BOTTLE};
            case CONSTRUCT -> new Item[]{Items.IRON_NUGGET, Items.COPPER_INGOT, Items.REDSTONE, Items.FLINT};
            case BAT, ARACHNE -> new Item[]{Items.STRING, Items.SPIDER_EYE, Items.GLOW_BERRIES, Items.COBWEB, Items.GLOW_INK_SAC};
            case CANID, MINOTAUR, CENTAUR, JACKALOPE, RODENT, MARSUPIAL, ARMOURED -> new Item[]{Items.LEATHER, Items.WHEAT, Items.CARROT, Items.RABBIT_HIDE, Items.FEATHER, Items.HAY_BLOCK};
        };
    }

    /** A girl of her clade she met on the way, who came home with her and already likes her friend. */
    @Nullable
    public static AbstractMonsterGirl companion(ServerLevel level, Player player, AbstractMonsterGirl guide) {
        List<GirlSpecies> kin = new ArrayList<>();
        for (GirlSpecies s : GirlSpecies.values()) if (s.clade() == guide.getSpecies().clade()) kin.add(s);
        GirlSpecies pick = kin.get(guide.getRandom().nextInt(kin.size()));
        AbstractMonsterGirl girl = ModGirls.type(pick).create(level);
        if (girl == null) return null;
        girl.moveTo(guide.getX() + 1.0, guide.getY(), guide.getZ() + 1.0, guide.getYRot(), 0);
        girl.finalizeSpawn(level, level.getCurrentDifficultyAt(girl.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        girl.addAffection(player, Orders.AFFECTION);
        girl.setPersistenceRequired();
        level.addFreshEntityWithPassengers(girl);
        player.displayClientMessage(Component.translatable("message.monstergirls.expedition_companion", guide.getDisplayName(), girl.getDisplayName()), false);
        return girl;
    }
}
