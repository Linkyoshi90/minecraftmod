package com.linkyoshi90.harmontide.leviathan;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The Dream-Leviathan's blessing: a second bar of ten starry hearts above your own (client/DreamHeartHud). It takes the hurt before
 * your health does, and fills again slowly on its own ({@link #REFILL_EVERY} ticks a half heart, once nothing has hurt you for a
 * while), never from food. Given once per player, together with her egg (leviathan/DreamEggItem); a second healing gives nothing more
 * to whoever already has both.
 */
public final class DreamHeart {
    /** The bar holds ten hearts. */
    public static final float MAX = 20.0F;
    /** A half heart back every 20 seconds, once 10 seconds have gone by without being hurt. */
    public static final int REFILL_EVERY = 400, CALM_FOR = 200;

    private DreamHeart() {
    }

    private static CompoundTag data(Player p) {
        return p.getData(LeviathanRegistry.BLESSING);
    }

    public static boolean blessed(Player p) {
        return data(p).getBoolean("Blessed");
    }

    public static float pool(Player p) {
        return data(p).getFloat("Pool");
    }

    public static boolean hadEgg(Player p) {
        return data(p).getBoolean("Egg");
    }

    private static void put(Player p, CompoundTag t) {
        p.setData(LeviathanRegistry.BLESSING, t);
        if (p instanceof ServerPlayer sp) LeviathanNet.sendHeart(sp);
    }

    public static void setPool(Player p, float v) {
        CompoundTag t = data(p).copy();
        t.putFloat("Pool", Math.max(0, Math.min(MAX, v)));
        put(p, t);
    }

    /** Her thanks to one player: the bar (full) and her egg, each only if they don't already have it. Whether anything was given. */
    public static boolean bless(ServerPlayer p) {
        CompoundTag t = data(p).copy();
        boolean gave = false;
        if (!t.getBoolean("Blessed")) {
            t.putBoolean("Blessed", true);
            t.putFloat("Pool", MAX);
            gave = true;
            p.displayClientMessage(Component.translatable("message.harmontide.leviathan.blessed").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        if (!t.getBoolean("Egg")) {
            t.putBoolean("Egg", true);
            ItemStack egg = new ItemStack(LeviathanRegistry.DREAM_EGG.get());
            if (!p.getInventory().add(egg)) p.drop(egg, false);
            gave = true;
            p.displayClientMessage(Component.translatable("message.harmontide.leviathan.got_egg").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        if (!gave) p.displayClientMessage(Component.translatable("message.harmontide.leviathan.already_blessed").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        put(p, t);
        if (gave) p.level().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 0.8F);
        return gave;
    }

    /** For tests and the test command: takes the blessing (and the egg's record) away again. */
    public static void unbless(Player p) {
        put(p, new CompoundTag());
    }

    /** Damage about to land on a player: the bar takes what it can. Returns what is left for their health. */
    public static float absorb(Player p, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (amount <= 0 || !blessed(p) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return amount;
        CompoundTag t = data(p).copy();
        float pool = t.getFloat("Pool");
        t.putLong("Hurt", p.level().getGameTime());
        if (pool <= 0) {
            put(p, t);
            return amount;
        }
        float took = Math.min(pool, amount);
        t.putFloat("Pool", pool - took);
        put(p, t);
        return amount - took;
    }

    /** The bar fills again, slowly, once nothing has hurt them for a while. */
    public static void tick(ServerPlayer p) {
        if (p.tickCount % 20 != 0 || !blessed(p)) return;
        CompoundTag t = data(p);
        long now = p.level().getGameTime();
        if (t.getFloat("Pool") >= MAX || now - t.getLong("Hurt") < CALM_FOR || now - t.getLong("Refill") < REFILL_EVERY) return;
        CompoundTag c = t.copy();
        c.putFloat("Pool", Math.min(MAX, c.getFloat("Pool") + 1.0F));
        c.putLong("Refill", now);
        put(p, c);
    }
}
