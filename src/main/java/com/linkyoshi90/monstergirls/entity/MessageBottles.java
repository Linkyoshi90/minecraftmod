package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModGirls;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Messages in bottles (2026-11-08). Now and then (one day in {@link #ODDS}) a bottle washes up on the beach near a player who is
 * by the sea in daylight. Opened (block/item MessageBottleItem), it holds one of three things: a map a girl drew of somewhere
 * she found ({@link ScoutMaps}), a little of a shipwreck's treasure, or a letter from a lonely girl of the sea, who is waiting on
 * the shore the next moment, already fond of whoever read it.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class MessageBottles {
    public static final int ODDS = 3, LOOK = 12, LETTER_AFFECTION = 10, LETTERS = 4;
    public static final List<GirlSpecies> WRITERS = List.of(GirlSpecies.SELKIE_MERMAID, GirlSpecies.DOLPHIN_MERMAID, GirlSpecies.SEAL_MERMAID,
            GirlSpecies.LORELEI_MERMAID, GirlSpecies.CUTTLEFISH_MOLLUSK, GirlSpecies.KOI_MERMAID);
    private static final String DAY = "BottleDay";

    private MessageBottles() {}

    public enum Contents { MAP, TREASURE, LETTER }

    /** A spot at the water's edge near {@code near}: sand or gravel with water beside it, or null. */
    public static BlockPos shore(ServerLevel level, BlockPos near) {
        for (int i = 0; i < 24; i++) {
            int x = near.getX() + level.random.nextInt(LOOK * 2 + 1) - LOOK, z = near.getZ() + level.random.nextInt(LOOK * 2 + 1) - LOOK;
            if (!level.hasChunkAt(new BlockPos(x, 0, z))) continue;
            BlockPos top = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
            if (!level.getBlockState(top.below()).is(Blocks.SAND) && !level.getBlockState(top.below()).is(Blocks.GRAVEL)) continue;
            for (var d : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                if (level.getFluidState(top.below().relative(d)).is(FluidTags.WATER)) return top;
            }
        }
        return null;
    }

    /** A bottle washes up near this player. The bottle, or null if there's no shore. */
    public static ItemEntity washUp(ServerLevel level, Player p) {
        BlockPos at = shore(level, p.blockPosition());
        if (at == null) return null;
        ItemEntity bottle = new ItemEntity(level, at.getX() + 0.5, at.getY() + 0.2, at.getZ() + 0.5, new ItemStack(ModItems.MESSAGE_BOTTLE.get()));
        bottle.setGlowingTag(true);
        level.addFreshEntity(bottle);
        level.sendParticles(ParticleTypes.SPLASH, at.getX() + 0.5, at.getY() + 0.2, at.getZ() + 0.5, 12, 0.4, 0.1, 0.4, 0.0);
        return bottle;
    }

    /** Opens a bottle: what was inside. */
    public static Contents open(ServerLevel level, Player p, Contents what) {
        level.playSound(null, p.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);
        give(p, new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE));
        switch (what) {
            case MAP -> {
                ItemStack map = ScoutMaps.chart(level, p.blockPosition(), ScoutMaps.ALL, level.random.nextInt(ScoutMaps.ALL.size()));
                if (map.isEmpty()) return open(level, p, Contents.TREASURE);
                give(p, map);
                p.displayClientMessage(Component.translatable("message.monstergirls.bottle.map", map.getHoverName()).withStyle(ChatFormatting.AQUA), false);
            }
            case TREASURE -> {
                var table = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.SHIPWRECK_TREASURE);
                LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, p.position()).withLuck(p.getLuck()).create(LootContextParamSets.CHEST);
                List<ItemStack> loot = new ArrayList<>(table.getRandomItems(params));
                if (loot.size() > 3) loot = loot.subList(0, 3);
                loot.forEach(s -> give(p, s));
                p.displayClientMessage(Component.translatable("message.monstergirls.bottle.treasure").withStyle(ChatFormatting.GOLD), false);
            }
            case LETTER -> {
                GirlSpecies kind = WRITERS.get(level.random.nextInt(WRITERS.size()));
                p.displayClientMessage(Component.translatable("message.monstergirls.bottle.letter." + level.random.nextInt(LETTERS),
                        ModGirls.type(kind).getDescription()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
                BlockPos at = shore(level, p.blockPosition());
                if (at != null && ModGirls.type(kind).create(level) instanceof AbstractMonsterGirl girl) {
                    girl.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360F, 0);
                    girl.finalizeSpawn(level, level.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
                    girl.setBaby(false);
                    girl.setPersistenceRequired();
                    girl.addAffection(p, LETTER_AFFECTION);
                    level.addFreshEntity(girl);
                    level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
                }
            }
        }
        Dairy.award(p, "message_bottle");
        return what;
    }

    public static Contents roll(ServerLevel level) {
        int r = level.random.nextInt(10);
        return r < 5 ? Contents.MAP : r < 8 ? Contents.TREASURE : Contents.LETTER;
    }

    private static void give(Player p, ItemStack s) {
        if (!p.getInventory().add(s)) p.drop(s, false);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (p.tickCount % 600 != 211 || !(p.level() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) return;
        long t = level.getDayTime() % 24000L;
        if (t > 12000L || !level.getBiome(p.blockPosition()).is(BiomeTags.IS_BEACH)) return;
        long day = level.getDayTime() / 24000L;
        if (p.getPersistentData().getLong(DAY) == day + 1) return;           // stored +1 so 0 means never
        p.getPersistentData().putLong(DAY, day + 1);
        if (level.random.nextInt(ODDS) == 0) washUp(level, p);
    }
}
