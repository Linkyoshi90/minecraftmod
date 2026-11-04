package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.Letters;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Favours: now and then a grown friend near a player asks them for something she needs (what depends on where her kind comes
 * from: candles, string, ingots, paper). Bring it within three days, all at once or a few at a time, and she is glad of it:
 * {@link #THANKS} friendship and a few emeralds. One open favour per player at a time.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Favours {
    public static final int THANKS = 10, ASK_CHANCE = 6;
    public static final long LASTS = 72000;
    public static final double RANGE = 24;
    public static final String FAVOUR = "Favour", OPEN = "FavourUntil";

    record Ask(Item item, int count) {
    }

    static final Map<String, List<Ask>> ASKS = Map.of(
            "sea", List.of(new Ask(Items.LANTERN, 1), new Ask(Items.GLASS_BOTTLE, 4), new Ask(Items.STRING, 6), new Ask(Items.CANDLE, 2)),
            "dark", List.of(new Ask(Items.CANDLE, 3), new Ask(Items.BONE, 6), new Ask(Items.BOOK, 1), new Ask(Items.INK_SAC, 3)),
            "wood", List.of(new Ask(Items.BONE_MEAL, 8), new Ask(Items.FLOWER_POT, 2), new Ask(Items.BOWL, 4), new Ask(Items.STRING, 4)),
            "desert", List.of(new Ask(Items.TORCH, 16), new Ask(Items.GOLD_INGOT, 1), new Ask(Items.LEATHER, 3), new Ask(Items.CANDLE, 2)),
            "east", List.of(new Ask(Items.PAPER, 6), new Ask(Items.LANTERN, 2), new Ask(Items.STRING, 4), new Ask(Items.INK_SAC, 2)),
            "swamp", List.of(new Ask(Items.GLASS_BOTTLE, 4), new Ask(Items.CLAY_BALL, 8), new Ask(Items.BOWL, 3), new Ask(Items.LANTERN, 1)),
            "ruins", List.of(new Ask(Items.IRON_INGOT, 3), new Ask(Items.REDSTONE, 8), new Ask(Items.COPPER_INGOT, 6), new Ask(Items.BOOK, 1)),
            "caves", List.of(new Ask(Items.TORCH, 16), new Ask(Items.STRING, 6), new Ask(Items.CANDLE, 2), new Ask(Items.LANTERN, 1)),
            "plains", List.of(new Ask(Items.LEATHER, 3), new Ask(Items.IRON_INGOT, 2), new Ask(Items.BOOK, 1), new Ask(Items.TORCH, 12)));

    private Favours() {
    }

    /** What she asked this player for, or null. */
    @Nullable
    public static CompoundTag of(AbstractMonsterGirl girl, Player player) {
        CompoundTag f = girl.getPersistentData().getCompound(FAVOUR);
        if (f.isEmpty() || !f.getString("By").equals(player.getStringUUID())) return null;
        if (girl.level().getGameTime() > f.getLong("Until")) return null;
        return f;
    }

    public static Item item(CompoundTag f) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(f.getString("Item")));
    }

    private static boolean open(ServerLevel level, Player player) {
        return level.getGameTime() < player.getData(ModAttachments.JOURNAL).getLong(OPEN);
    }

    private static void setOpen(Player player, long until) {
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        data.putLong(OPEN, until);
        player.setData(ModAttachments.JOURNAL, data);
    }

    /** She asks a player for something (unless they owe a favour already). Whether she asked. */
    public static boolean ask(ServerLevel level, AbstractMonsterGirl girl, Player player) {
        if (open(level, player) || girl.isBaby() || girl.getAffection(player) < HomeLife.FRIEND) return false;
        List<Ask> pool = ASKS.get(Letters.country(girl.getSpecies().family()));
        Ask a = pool.get(level.random.nextInt(pool.size()));
        long until = level.getGameTime() + LASTS;
        CompoundTag f = new CompoundTag();
        f.putString("By", player.getStringUUID());
        f.putString("Item", BuiltInRegistries.ITEM.getKey(a.item()).toString());
        f.putInt("Count", a.count());
        f.putLong("Until", until);
        girl.getPersistentData().put(FAVOUR, f);
        setOpen(player, until);
        player.displayClientMessage(Component.translatable("message.monstergirls.favour.ask", girl.getDisplayName(), a.count(), a.item().getDescription()).withStyle(ChatFormatting.YELLOW), false);
        return true;
    }

    /** A hand holding what she asked for (AbstractMonsterGirl.mobInteract, server side): she takes what she still needs. */
    @Nullable
    public static InteractionResult interact(AbstractMonsterGirl girl, Player player, ItemStack stack) {
        if (!(girl.level() instanceof ServerLevel level)) return null;
        CompoundTag f = of(girl, player);
        if (f == null || stack.isEmpty() || !stack.is(item(f))) return null;
        int need = f.getInt("Count"), give = Math.min(need, stack.getCount());
        stack.consume(give, player);
        need -= give;
        girl.getLookControl().setLookAt(player);
        if (need > 0) {
            f.putInt("Count", need);
            player.displayClientMessage(Component.translatable("message.monstergirls.favour.more", girl.getDisplayName(), need), true);
            return InteractionResult.SUCCESS;
        }
        girl.getPersistentData().remove(FAVOUR);
        setOpen(player, 0);
        girl.addAffection(player, THANKS);
        girl.blushFor(60);
        ItemStack emeralds = new ItemStack(Items.EMERALD, 2 + level.random.nextInt(3));
        if (!player.addItem(emeralds)) player.drop(emeralds, false);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, girl.getX(), girl.getEyeY() + 0.3, girl.getZ(), 8, 0.4, 0.3, 0.4, 0);
        level.playSound(null, girl.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.4F);
        player.displayClientMessage(Component.translatable("message.monstergirls.favour.done", girl.getDisplayName()).withStyle(ChatFormatting.GREEN), false);
        return InteractionResult.SUCCESS;
    }

    /** Once a minute: maybe a friend near them thinks of something they could bring her. Who asked, or null. */
    @Nullable
    public static AbstractMonsterGirl check(ServerLevel level, ServerPlayer player, boolean force) {
        if (open(level, player) || !force && level.random.nextInt(ASK_CHANCE) != 0) return null;
        List<Mob> friends = HomeLife.friendsOf(level, player, player.getBoundingBox().inflate(RANGE));
        List<AbstractMonsterGirl> girls = friends.stream().filter(m -> m instanceof AbstractMonsterGirl).map(m -> (AbstractMonsterGirl) m).toList();
        if (girls.isEmpty()) return null;
        AbstractMonsterGirl girl = girls.get(level.random.nextInt(girls.size()));
        return ask(level, girl, player) ? girl : null;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer p && p.level() instanceof ServerLevel level && p.tickCount % 1200 == 501 && !p.isSpectator()) check(level, p, false);
    }
}
