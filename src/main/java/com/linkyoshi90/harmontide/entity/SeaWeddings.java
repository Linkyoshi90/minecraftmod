package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModAttachments;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Sea weddings and mermaid blood (2026-11-13), after the sea bishops of Monster Girl Encyclopedia, who wed sailors to the sea's
 * daughters and give them the sea's breath.
 * <p>
 * <b>A sea wedding.</b> A bride of the sea (merfolk or mollusk) wed by a hearth with water beside it ({@link #WATER} blocks within
 * {@link #SHORE}), with another sea girl among the guests to officiate as the sea bishop, leaves her husband blessed: while she
 * lives and they are wed, in water in the sea, on the beach or in a river they can breathe it, and swim with the dolphins' grace.
 * <p>
 * <b>Mermaid blood.</b> Once in a player's life, a merfolk close friend ({@link #BLOOD_AFFECTION}) may give them a drop of her blood
 * when they pet her (one pat in {@link #BLOOD_ODDS}). Drunk, it is a lasting extra heart ({@link #BLOOD_HEALTH}), kept through death.
 * <p>
 * Off with the config option {@code seaWeddings}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class SeaWeddings {
    public static final int WATER = 6, SHORE = 6, OFFICIANT = 16, BLOOD_AFFECTION = 80, BLOOD_ODDS = 40, BLOOD_HEALTH = 2, BREATH = 320;
    public static final ResourceLocation BLOOD = HarmontideMod.id("mermaid_blood");
    private static final String SEA_WIVES = "SeaWives", GIVEN = "MermaidBloodGiven", DRUNK = "MermaidBloodDrunk";

    private SeaWeddings() {}

    public static boolean ofTheSea(Mob mob) {
        return mob instanceof AbstractMonsterGirl g && (g.getSpecies().family() == GirlSpecies.Family.MERFOLK || g.getSpecies().family() == GirlSpecies.Family.MOLLUSK);
    }

    /** Water by the hearth: at least {@link #WATER} water blocks within {@link #SHORE}. */
    public static boolean byTheWater(ServerLevel level, BlockPos hearth) {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(hearth.offset(-SHORE, -3, -SHORE), hearth.offset(SHORE, 2, SHORE))) {
            if (level.getFluidState(p).is(FluidTags.WATER) && ++n >= WATER) return true;
        }
        return false;
    }

    /** Another sea girl among the guests, to officiate: the sea bishop. */
    @Nullable
    public static Mob seaBishop(ServerLevel level, Mob bride, BlockPos hearth) {
        List<Mob> found = level.getEntitiesOfClass(Mob.class, new AABB(hearth).inflate(OFFICIANT), m -> m != bride && m.isAlive() && !m.isBaby() && ofTheSea(m));
        return found.isEmpty() ? null : found.get(0);
    }

    /** Called at each wedding: a sea wedding blesses the husband. True if it was one. */
    public static boolean onWed(ServerLevel level, Mob bride, Player player, BlockPos hearth) {
        if (!HarmontideConfig.seaWeddings() || !ofTheSea(bride) || !byTheWater(level, hearth)) return false;
        Mob bishop = seaBishop(level, bride, hearth);
        if (bishop == null) return false;
        CompoundTag j = player.getData(ModAttachments.JOURNAL);
        ListTag wives = j.getList(SEA_WIVES, Tag.TAG_STRING);
        wives.add(StringTag.valueOf(bride.getUUID().toString()));
        j.put(SEA_WIVES, wives);
        player.setData(ModAttachments.JOURNAL, j);
        level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, hearth.getX() + 0.5, hearth.getY() + 1, hearth.getZ() + 0.5, 60, 1.5, 0.5, 1.5, 0.1);
        level.sendParticles(ParticleTypes.NAUTILUS, player.getX(), player.getEyeY(), player.getZ(), 30, 0.5, 0.5, 0.5, 0.5);
        level.playSound(null, hearth, SoundEvents.CONDUIT_ACTIVATE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.harmontide.sea_wedding", bishop.getDisplayName(), bride.getDisplayName()), false);
        Dairy.award(player, "sea_wedding");
        return true;
    }

    /** Blessed by a sea wedding to a wife they are still wed to. */
    public static boolean blessed(Player player) {
        if (!HarmontideConfig.seaWeddings()) return false;
        ListTag wives = player.getData(ModAttachments.JOURNAL).getList(SEA_WIVES, Tag.TAG_STRING);
        ListTag spouses = player.getData(ModAttachments.JOURNAL).getList(Marriage.SPOUSES_KEY, Tag.TAG_STRING);
        for (Tag w : wives) for (Tag s : spouses) if (w.getAsString().equals(s.getAsString())) return true;
        return false;
    }

    /** Where the sea's breath reaches: the sea, the beach, a river. */
    public static boolean seaward(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN) || biome.is(BiomeTags.IS_BEACH) || biome.is(BiomeTags.IS_RIVER);
    }

    /** The sea's breath for one blessed player in its water. True if given. */
    public static boolean breathe(Player player, boolean inWater) {
        if (!inWater || !blessed(player) || !seaward(player.level().getBiome(player.blockPosition()))) return false;
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, BREATH, 0, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, BREATH, 0, true, false, true));
        return true;
    }

    // --- mermaid blood ------------------------------------------------------------------------------------------------

    /** After a pat: now and then, once in a player's life, a merfolk close friend gives them her blood. The vial, or null. */
    @Nullable
    public static ItemStack onPet(ServerLevel level, Mob mob, Player player, boolean roll) {
        if (!HarmontideConfig.seaWeddings() || !(mob instanceof AbstractMonsterGirl g) || g.getSpecies().family() != GirlSpecies.Family.MERFOLK
                || g.isBaby() || g.getAffection(player) < BLOOD_AFFECTION || !roll) return null;
        CompoundTag j = player.getData(ModAttachments.JOURNAL);
        if (j.getBoolean(GIVEN)) return null;
        j.putBoolean(GIVEN, true);
        player.setData(ModAttachments.JOURNAL, j);
        ItemStack vial = new ItemStack(ModItems.MERMAID_BLOOD.get());
        if (!player.getInventory().add(vial.copy())) player.drop(vial.copy(), false);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.4, mob.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
        player.displayClientMessage(Component.translatable("message.harmontide.mermaid_blood.given", mob.getDisplayName()), false);
        return vial;
    }

    /** They drink it: a lasting extra heart, once. True if it took. */
    public static boolean drink(Player player) {
        CompoundTag j = player.getData(ModAttachments.JOURNAL);
        if (j.getBoolean(DRUNK)) {
            player.displayClientMessage(Component.translatable("message.harmontide.mermaid_blood.again"), true);
            return false;
        }
        j.putBoolean(DRUNK, true);
        player.setData(ModAttachments.JOURNAL, j);
        apply(player);
        player.heal(BLOOD_HEALTH);
        player.displayClientMessage(Component.translatable("message.harmontide.mermaid_blood.drunk"), false);
        Dairy.award(player, "mermaid_blood");
        return true;
    }

    public static boolean drunk(Player player) {
        return player.getData(ModAttachments.JOURNAL).getBoolean(DRUNK);
    }

    /** The extra heart, kept on the player (put back after death). */
    public static void apply(Player player) {
        AttributeInstance max = player.getAttribute(Attributes.MAX_HEALTH);
        if (max != null && drunk(player) && !max.hasModifier(BLOOD)) {
            max.addPermanentModifier(new AttributeModifier(BLOOD, BLOOD_HEALTH, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (p.level().isClientSide || p.tickCount % 100 != 37) return;
        apply(p);
        breathe(p, p.isInWater());
    }
}
