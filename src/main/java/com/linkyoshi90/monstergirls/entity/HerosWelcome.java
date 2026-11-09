package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A hero's welcome (2026-11-09). When a player earns one of the great vanilla deeds ({@link #DEEDS}: the dragon slain, the Wither
 * summoned or slain, a village saved from a raid, a zombie villager cured...), word reaches home: the next time they come within
 * {@link #HOME} of a hearth of theirs with friends about, the friends there throw a party: fireworks over the hearth, cheers, each one
 * {@link #CHEER} closer, and a present from each. One party per deed.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class HerosWelcome {
    /** The advancements worth a party, by the key the cheer uses (message.monstergirls.hero.DEED). */
    public static final Map<String, String> DEEDS = Map.of(
            "minecraft:end/kill_dragon", "dragon", "minecraft:nether/summon_wither", "wither_summoned", "minecraft:adventure/kill_all_mobs", "hunter",
            "minecraft:adventure/hero_of_the_village", "village", "minecraft:story/cure_zombie_villager", "cured", "minecraft:nether/create_full_beacon", "beacon",
            "minecraft:adventure/totem_of_undying", "survived", "minecraft:end/elytra", "elytra", "minecraft:adventure/adventuring_time", "traveller");
    public static final double HOME = 12;
    public static final int CHEER = 5;
    static final String PENDING = "HeroDeeds";

    private HerosWelcome() {
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        String deed = DEEDS.get(event.getAdvancement().id().toString());
        if (deed != null) earned(event.getEntity(), deed);
    }

    /** Word of a deed waits for them at home. */
    public static void earned(Player player, String deed) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        ListTag pending = journal.getList(PENDING, Tag.TAG_STRING);
        for (Tag t : pending) if (t.getAsString().equals(deed)) return;
        pending.add(StringTag.valueOf(deed));
        journal.put(PENDING, pending);
        player.setData(ModAttachments.JOURNAL, journal);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 40 == 31 && event.getEntity().level() instanceof ServerLevel level) welcome(level, event.getEntity());
    }

    /** Home with word of a deed, and friends about: the party. How many friends cheered (0: no party). */
    public static int welcome(ServerLevel level, Player player) {
        CompoundTag journal = player.getData(ModAttachments.JOURNAL);
        ListTag pending = journal.getList(PENDING, Tag.TAG_STRING);
        if (pending.isEmpty()) return 0;
        BlockPos hearth = hearthOf(level, player);
        if (hearth == null) return 0;
        List<Mob> friends = HomeLife.friendsOf(level, player, new AABB(hearth).inflate(HOME * 2));
        if (friends.isEmpty()) return 0;
        String deed = pending.getString(0);
        pending.remove(0);
        journal.put(PENDING, pending);
        player.setData(ModAttachments.JOURNAL, journal);
        for (Mob m : friends) {
            ((Bonded) m).addAffection(player, CHEER);
            m.getJumpControl().jump();
            m.getNavigation().moveTo(player, 1.1);
            level.sendParticles(ParticleTypes.HEART, m.getX(), m.getEyeY() + 0.3, m.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
            if (m instanceof GiftGiver giver) {
                ItemStack gift = giver.rollGift(player);
                if (!gift.isEmpty()) m.spawnAtLocation(gift);
            }
        }
        level.sendParticles(ParticleTypes.FIREWORK, hearth.getX() + 0.5, hearth.getY() + 3, hearth.getZ() + 0.5, 80, 2.0, 1.0, 2.0, 0.1);
        level.playSound(null, hearth, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.NEUTRAL, 1.5F, 1.0F);
        level.playSound(null, hearth, SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.2F);
        player.displayClientMessage(Component.translatable("message.monstergirls.hero." + deed, friends.size()).withStyle(ChatFormatting.GOLD), false);
        return friends.size();
    }

    /** A loaded hearth of theirs within HOME. */
    @Nullable
    static BlockPos hearthOf(ServerLevel level, Player player) {
        for (BlockPos h : HearthBlockEntity.loaded(level)) {
            if (h.distToCenterSqr(player.position()) <= HOME * HOME && level.getBlockEntity(h) instanceof HearthBlockEntity be && player.getUUID().equals(be.owner())) return h;
        }
        return null;
    }
}
