package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Letters;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

/**
 * Treats: every girl has a favourite food, a treat from her family's country (the letters' countries), the same for her
 * always (by her UUID). She takes it from any hand, even if it isn't what she usually eats, and it means three times as
 * much as ordinary food. The first time a player finds out, their journal remembers. And every girl has a birthday, a day
 * of the festival calendar's 24: the first food a friend brings her that day is worth ten friendship more, with confetti.
 * Her friends hear about it when they are near her that day.
 */
public final class Treats {
    public static final int FAVOURITE_TIMES = 3, BIRTHDAY_BONUS = 10;
    public static final String KNOWN = "Favourites", BIRTHDAY_DONE = "BirthdayDone", BIRTHDAY_TOLD = "BirthdayTold", ADVANCEMENT = "birthday";
    public static final Map<String, Item[]> TREATS = Map.of(
            "sea", new Item[]{Items.COOKED_SALMON, Items.COOKED_COD, Items.DRIED_KELP, Items.TROPICAL_FISH},
            "dark", new Item[]{Items.PUMPKIN_PIE, Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW, Items.CHORUS_FRUIT},
            "wood", new Item[]{Items.SWEET_BERRIES, Items.APPLE, Items.HONEY_BOTTLE, Items.GLOW_BERRIES},
            "desert", new Item[]{Items.BAKED_POTATO, Items.COOKED_RABBIT, Items.MELON_SLICE, Items.GOLDEN_CARROT},
            "east", new Item[]{Items.COOKED_SALMON, Items.MUSHROOM_STEW, Items.SWEET_BERRIES, Items.COOKIE},
            "swamp", new Item[]{Items.HONEY_BOTTLE, Items.MUSHROOM_STEW, Items.DRIED_KELP, Items.MELON_SLICE},
            "ruins", new Item[]{Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.BREAD, Items.COOKIE},
            "caves", new Item[]{Items.GLOW_BERRIES, Items.COOKED_MUTTON, Items.MUSHROOM_STEW, Items.SPIDER_EYE},
            "plains", new Item[]{Items.BREAD, Items.COOKIE, Items.CARROT, Items.COOKED_BEEF});

    private Treats() {
    }

    private static int hash(AbstractMonsterGirl girl, int salt) {
        long h = girl.getUUID().getLeastSignificantBits() ^ (girl.getUUID().getMostSignificantBits() * 31 + salt);
        return (int) Math.floorMod(h ^ (h >>> 29), 1_000_003L);
    }

    public static Item favourite(AbstractMonsterGirl girl) {
        Item[] pool = TREATS.get(Letters.country(girl.getSpecies().family()));
        return pool[hash(girl, 7) % pool.length];
    }

    /** Her birthday: a day of the festival calendar (from 0). */
    public static int birthday(AbstractMonsterGirl girl) {
        return hash(girl, 13) % Festivals.CYCLE;
    }

    public static boolean isBirthday(AbstractMonsterGirl girl, long dayTime) {
        return Math.floorMod(dayTime / 24000L, (long) Festivals.CYCLE) == birthday(girl);
    }

    /** Whether a player has found out her favourite (their journal keeps it by her UUID). */
    public static boolean knows(Player player, AbstractMonsterGirl girl) {
        return player.getData(ModAttachments.JOURNAL).getCompound(KNOWN).contains(girl.getStringUUID());
    }

    /** A player finds out her favourite (kept in their journal) and is told so with the given message (her name, the treat). */
    public static void learn(Player player, AbstractMonsterGirl girl, String message) {
        Item fav = favourite(girl);
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        CompoundTag known = data.getCompound(KNOWN);
        known.putString(girl.getStringUUID(), BuiltInRegistries.ITEM.getKey(fav).toString());
        data.put(KNOWN, known);
        player.setData(ModAttachments.JOURNAL, data);
        player.displayClientMessage(Component.translatable(message, girl.getDisplayName(), fav.getDescription()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    /**
     * Food from a hand (AbstractMonsterGirl.mobInteract): what it is worth to her, with the favourite and birthday counted in.
     * Call when she has accepted it.
     */
    public static int value(ServerLevel level, AbstractMonsterGirl girl, Player player, ItemStack stack, int normal) {
        int value = normal;
        if (stack.is(favourite(girl))) {
            value = normal * FAVOURITE_TIMES;
            level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getY() + girl.getBbHeight() + 0.3, girl.getZ(), 6, 0.4, 0.2, 0.4, 0);
            if (!knows(player, girl)) learn(player, girl, "message.monstergirls.favourite");
        }
        long day = level.getDayTime() / 24000L + 1;
        if (isBirthday(girl, level.getDayTime()) && girl.getAffection(player) >= Orders.AFFECTION && girl.getPersistentData().getLong(BIRTHDAY_DONE) != day) {
            girl.getPersistentData().putLong(BIRTHDAY_DONE, day);
            value += BIRTHDAY_BONUS;
            level.sendParticles(ParticleTypes.FIREWORK, girl.getX(), girl.getY() + girl.getBbHeight() + 0.5, girl.getZ(), 24, 0.5, 0.4, 0.5, 0.08);
            level.playSound(null, girl.blockPosition(), SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.NEUTRAL, 1.0F, 1.2F);
            player.displayClientMessage(Component.translatable("message.monstergirls.birthday.thanks", girl.getDisplayName()).withStyle(ChatFormatting.GOLD), false);
            if (player instanceof ServerPlayer sp) {
                AdvancementHolder a = sp.server.getAdvancements().get(MonsterGirlsMod.id(ADVANCEMENT));
                if (a != null) sp.getAdvancements().award(a, "done");
            }
        }
        return value;
    }

    /** Every five seconds: on her birthday, a friend near her hears of it (once that day). */
    static void tick(ServerLevel level, AbstractMonsterGirl girl) {
        if (girl.isBaby() || !isBirthday(girl, level.getDayTime())) return;
        long day = level.getDayTime() / 24000L + 1;
        if (girl.getPersistentData().getLong(BIRTHDAY_TOLD) == day) return;
        for (Player p : level.players()) {
            if (girl.getAffection(p) >= Orders.AFFECTION && p.distanceToSqr(girl) < 16 * 16) {
                girl.getPersistentData().putLong(BIRTHDAY_TOLD, day);
                p.displayClientMessage(Component.translatable("message.monstergirls.birthday", girl.getDisplayName()).withStyle(ChatFormatting.GOLD), false);
            }
        }
    }
}
