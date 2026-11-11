package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.item.HeadgearItem;
import com.linkyoshi90.monstergirls.item.HeadgearItem.Hat;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Headgear (2026-11-11, after Grimoire of Gaia's wearable trinkets). A grown friend devoted to a player ({@link #DEVOTED}) gives
 * them her kind's piece the first time she is near them ({@link #NEAR}) after that, once per player and piece (whoever gives it):
 * <ul>
 *   <li>a kitsune: the fox mask. Worn: luck (Luck), for loot and fishing.</li>
 *   <li>a jackalope: the bunny ears. Worn: you hear what hunts you, monsters within {@link #HEAR} that have you as their target glow.</li>
 *   <li>a devil: the witch hat. Worn: experience orbs give half again ({@link #XP_BONUS}).</li>
 *   <li>a flora girl: the flower crown. Worn: bees never turn on you, and in the open by day you heal a little
 *   ({@link #SUN_HEAL} every {@link #SUN_EVERY} ticks).</li>
 * </ul>
 * The harpies' feather cap is the older winged cap (crafted). Config "headgear".
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Headgear {
    public static final int DEVOTED = 60, NEAR = 8, HEAR = 16, SUN_EVERY = 200, GIVE_EVERY = 600;
    public static final float XP_BONUS = 0.5F, SUN_HEAL = 1.0F;
    private static final String GIVEN = "HatsGiven";

    private Headgear() {}

    @Nullable
    public static Hat hatOf(GirlSpecies s) {
        return switch (s.family()) {
            case KITSUNE -> Hat.KITSUNE_MASK;
            case JACKALOPE -> Hat.BUNNY_EARS;
            case DEVIL -> Hat.WITCH_HAT;
            case FLORA -> Hat.FLOWER_CROWN;
            default -> null;
        };
    }

    public static ItemStack stack(Hat hat) {
        return new ItemStack(switch (hat) {
            case KITSUNE_MASK -> ModItems.KITSUNE_MASK.get();
            case BUNNY_EARS -> ModItems.BUNNY_EARS.get();
            case WITCH_HAT -> ModItems.WITCH_HAT.get();
            case FLOWER_CROWN -> ModItems.FLOWER_CROWN.get();
        });
    }

    /** The piece on their head, or null. */
    @Nullable
    public static Hat wearing(Player p) {
        return MonsterGirlsConfig.headgear() && p.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof HeadgearItem h ? h.hat() : null;
    }

    public static boolean given(Player p, Hat hat) {
        for (Tag t : p.getData(ModAttachments.JOURNAL).getList(GIVEN, Tag.TAG_STRING)) if (t.getAsString().equals(hat.id())) return true;
        return false;
    }

    // --- giving ---------------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onGirlTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractMonsterGirl girl) || girl.tickCount % GIVE_EVERY != 29 || !(girl.level() instanceof ServerLevel level)) return;
        if (net.neoforged.neoforge.gametest.GameTestHooks.isGametestServer()) return;   // the tests call give themselves
        for (Player p : level.getEntitiesOfClass(Player.class, girl.getBoundingBox().inflate(NEAR))) give(level, girl, p);
    }

    /** She gives this player her kind's piece, if she is devoted to them and they haven't one of it from anyone. Whether she did. */
    public static boolean give(ServerLevel level, AbstractMonsterGirl girl, Player p) {
        Hat hat = hatOf(girl.getSpecies());
        if (hat == null || !MonsterGirlsConfig.headgear() || girl.isBaby() || p.isSpectator() || girl.getAffection(p) < DEVOTED || given(p, hat)) return false;
        var journal = p.getData(ModAttachments.JOURNAL);
        ListTag list = journal.getList(GIVEN, Tag.TAG_STRING);
        list.add(StringTag.valueOf(hat.id()));
        journal.put(GIVEN, list);
        p.setData(ModAttachments.JOURNAL, journal);
        ItemStack gift = stack(hat);
        if (!p.getInventory().add(gift)) p.drop(gift, false);
        girl.getLookControl().setLookAt(p);
        girl.blushFor(60);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getY() + girl.getBbHeight(), girl.getZ(), 5, 0.4, 0.2, 0.4, 0.0);
        p.displayClientMessage(Component.translatable("message.monstergirls.headgear.given", girl.getDisplayName(), stack(hat).getHoverName()), false);
        return true;
    }

    // --- wearing --------------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (p.tickCount % 20 == 9 && p.level() instanceof ServerLevel level) tick(level, p);
    }

    /** Once a second: what the piece on their head does. The monsters the bunny ears heard (empty for the rest). */
    public static List<Mob> tick(ServerLevel level, Player p) {
        Hat hat = wearing(p);
        List<Mob> heard = new ArrayList<>();
        if (hat == null) return heard;
        switch (hat) {
            case KITSUNE_MASK -> p.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 0, true, false, true));
            case BUNNY_EARS -> {
                for (Mob m : level.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(HEAR), m -> m instanceof Enemy && m.getTarget() == p)) {
                    m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false));
                    heard.add(m);
                }
            }
            case FLOWER_CROWN -> {
                if (p.tickCount % SUN_EVERY < 20 && level.isDay() && level.canSeeSky(p.blockPosition().above()) && p.getHealth() < p.getMaxHealth()) p.heal(SUN_HEAL);
            }
            default -> {
            }
        }
        return heard;
    }

    /** The witch hat: half again from every orb. */
    @SubscribeEvent
    public static void onXp(PlayerXpEvent.PickupXp event) {
        if (wearing(event.getEntity()) == Hat.WITCH_HAT) event.getEntity().giveExperiencePoints(bonusXp(event.getOrb().getValue()));
    }

    public static int bonusXp(int value) {
        return (int) Math.ceil(value * XP_BONUS);
    }

    /** The flower crown: bees don't turn on its wearer. */
    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Bee && event.getNewAboutToBeSetTarget() instanceof Player p && !p.level().isClientSide && wearing(p) == Hat.FLOWER_CROWN) {
            event.setCanceled(true);
        }
    }
}
