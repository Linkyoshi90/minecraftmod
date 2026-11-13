package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.registry.ModAttachments;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.UUID;

/**
 * The Sabbath (2026-11-13): the witches' coven of Monster Girl Encyclopedia, the Demon Realm's own answer to the Order. A player's
 * standing with it ({@link #standing}, 0 to {@link #MAX}) rises with every deed the Order frowns on (whatever raises their
 * notoriety raises it too, Order#addNotoriety), with the full-moon sabbath dance ({@link #DANCE} once a night), a realm-changed
 * creature turning nearby ({@link #CHANGED}) and a wedding by a realm censer ({@link #WEDDING}); it falls with the Order's own
 * comforts (tithes, mercy, exorcism: half what they take off notoriety). Its ranks:
 * <ul>
 * <li>{@link Rank#INITIATE}: witches no longer turn on you.</li>
 * <li>{@link Rank#ADEPT}: a witch, offered an empty hand, opens her Sabbath trades (realm goods and brews for emeralds).</li>
 * <li>{@link Rank#KEEPER}: a familiar of your own, a black cat who comes to you once (another at the next full moon if you lose her);
 * at night, near her, you see with her eyes (Night Vision).</li>
 * <li>{@link Rank#SIBLING}: the Sabbath's sibling: the title goes after your name.</li>
 * </ul>
 * Off with the config option {@code sabbath}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Sabbath {
    public static final int MAX = 100, DANCE = 5, CHANGED = 2, WEDDING = 15, FAMILIAR_NEAR = 8;
    private static final String STANDING = "SabbathStanding", DANCED = "SabbathDanced", FAMILIAR = "SabbathFamiliar", FAMILIAR_DAY = "SabbathFamiliarDay";

    private Sabbath() {}

    public enum Rank {
        OUTSIDER(0), INITIATE(10), ADEPT(30), KEEPER(60), SIBLING(100);

        public final int at;

        Rank(int at) {
            this.at = at;
        }

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Rank of(int standing) {
            Rank r = OUTSIDER;
            for (Rank k : values()) if (standing >= k.at) r = k;
            return r;
        }
    }

    private static CompoundTag journal(Player p) {
        return p.getData(ModAttachments.JOURNAL);
    }

    public static int standing(Player p) {
        return journal(p).getInt(STANDING);
    }

    public static Rank rank(Player p) {
        return Rank.of(standing(p));
    }

    public static boolean atLeast(Player p, Rank r) {
        return HarmontideConfig.sabbath() && standing(p) >= r.at;
    }

    /** Sets a player's standing; rank changes are announced and rewarded. */
    public static void setStanding(Player p, int value) {
        if (p.level().isClientSide) return;
        int before = standing(p), after = Math.max(0, Math.min(MAX, value));
        if (before == after) return;
        CompoundTag j = journal(p);
        j.putInt(STANDING, after);
        p.setData(ModAttachments.JOURNAL, j);
        Rank was = Rank.of(before), now = Rank.of(after);
        if (now.ordinal() > was.ordinal() && HarmontideConfig.sabbath()) {
            p.displayClientMessage(Component.translatable("message.harmontide.sabbath.rank." + now.key()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
            Dairy.award(p, "sabbath_" + now.key());
            if (now.ordinal() >= Rank.KEEPER.ordinal() && p.level() instanceof ServerLevel level) familiar(level, p);
        }
        if (was != now) p.refreshDisplayName();
    }

    /** A deed the Sabbath hears of (negative: one the Order would bless). */
    public static void deed(@Nullable Player p, int amount) {
        if (p == null || amount == 0 || !HarmontideConfig.sabbath()) return;
        setStanding(p, standing(p) + amount);
    }

    /** Dancing at the full-moon sabbath: once a night. True if it counted. */
    public static boolean danced(ServerLevel level, Player p) {
        long night = (level.getDayTime() + 12000L) / 24000L;
        CompoundTag j = journal(p);
        if (j.getLong(DANCED) == night + 1) return false;
        j.putLong(DANCED, night + 1);
        p.setData(ModAttachments.JOURNAL, j);
        deed(p, DANCE);
        return true;
    }

    // --- witches -----------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Witch && event.getNewAboutToBeSetTarget() instanceof Player p && atLeast(p, Rank.INITIATE)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Player p = event.getEntity();
        if (!(event.getTarget() instanceof Witch witch) || event.getHand() != InteractionHand.MAIN_HAND || !event.getItemStack().isEmpty()) return;
        if (!atLeast(p, Rank.ADEPT)) return;
        if (!witch.level().isClientSide) {
            Coven coven = new Coven(witch, p);
            coven.setTradingPlayer(p);
            coven.openTradingScreen(p, Component.translatable("entity.harmontide.sabbath_witch"), 1);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(witch.level().isClientSide));
    }

    /** The Sabbath's trades: the realm's goods and a witch's brews. */
    public static MerchantOffers offers() {
        MerchantOffers o = new MerchantOffers();
        o.add(new MerchantOffer(new ItemCost(Items.EMERALD, 3), new ItemStack(ModItems.HEARTFRUIT.get(), 2), 8, 2, 0.05F));
        o.add(new MerchantOffer(new ItemCost(Items.EMERALD, 2), new ItemStack(ModItems.WEEPCAP.get(), 2), 8, 2, 0.05F));
        o.add(new MerchantOffer(new ItemCost(Items.EMERALD, 6), new ItemStack(ModItems.REALM_CENSER.get()), 3, 5, 0.05F));
        o.add(new MerchantOffer(new ItemCost(Items.EMERALD, 4), new ItemStack(ModItems.REALM_GRASS.get(), 4), 6, 3, 0.05F));
        o.add(new MerchantOffer(new ItemCost(Items.EMERALD, 5), PotionContents.createItemStack(Items.SPLASH_POTION, Potions.HEALING), 6, 3, 0.05F));
        o.add(new MerchantOffer(new ItemCost(Items.EMERALD, 5), PotionContents.createItemStack(Items.POTION, Potions.NIGHT_VISION), 6, 3, 0.05F));
        o.add(new MerchantOffer(new ItemCost(Items.EMERALD, 8), PotionContents.createItemStack(Items.POTION, Potions.INVISIBILITY), 4, 5, 0.05F));
        o.add(new MerchantOffer(new ItemCost(Items.GLASS_BOTTLE, 3), new ItemStack(Items.EMERALD), 12, 1, 0.05F));
        return o;
    }

    /** A witch's table, for one player at a time. */
    public static final class Coven implements Merchant {
        private final Witch witch;
        @Nullable
        private Player trading;
        private MerchantOffers offers = offers();

        Coven(Witch witch, Player p) {
            this.witch = witch;
        }

        @Override public void setTradingPlayer(@Nullable Player player) { this.trading = player; }
        @Nullable @Override public Player getTradingPlayer() { return this.trading; }
        @Override public MerchantOffers getOffers() { return this.offers; }
        @Override public void overrideOffers(MerchantOffers offers) { this.offers = offers; }
        @Override public void notifyTrade(MerchantOffer offer) {
            offer.increaseUses();
            this.witch.playSound(SoundEvents.WITCH_CELEBRATE, 0.6F, 1.2F);
        }
        @Override public void notifyTradeUpdated(ItemStack stack) {}
        @Override public int getVillagerXp() { return 0; }
        @Override public void overrideXp(int xp) {}
        @Override public boolean showProgressBar() { return false; }
        @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.WITCH_CELEBRATE; }
        @Override public boolean isClientSide() { return this.witch.level().isClientSide; }
    }

    // --- the familiar -------------------------------------------------------------------------------------------------

    /** The player's familiar, if she lives in this level. */
    @Nullable
    public static Cat familiarOf(ServerLevel level, Player p) {
        CompoundTag j = journal(p);
        return j.hasUUID(FAMILIAR) && level.getEntity(j.getUUID(FAMILIAR)) instanceof Cat cat && cat.isAlive() ? cat : null;
    }

    /** A black cat comes to a keeper of the Sabbath: tame, theirs, named. Null if one is already theirs and alive. */
    @Nullable
    public static Cat familiar(ServerLevel level, Player p) {
        if (familiarOf(level, p) != null) return null;
        Cat cat = EntityType.CAT.create(level);
        if (cat == null) return null;
        cat.moveTo(p.getX() + 1, p.getY(), p.getZ() + 1, p.getYRot(), 0.0F);
        cat.finalizeSpawn(level, level.getCurrentDifficultyAt(p.blockPosition()), MobSpawnType.EVENT, null);
        level.registryAccess().registryOrThrow(Registries.CAT_VARIANT).getHolder(CatVariant.ALL_BLACK).ifPresent(cat::setVariant);
        cat.tame(p);
        cat.setCustomName(Component.translatable("entity.harmontide.sabbath_familiar", p.getName()));
        cat.setPersistenceRequired();
        level.addFreshEntity(cat);
        level.sendParticles(ParticleTypes.WITCH, cat.getX(), cat.getY() + 0.4, cat.getZ(), 20, 0.3, 0.3, 0.3, 0.0);
        CompoundTag j = journal(p);
        j.putUUID(FAMILIAR, cat.getUUID());
        j.putLong(FAMILIAR_DAY, level.getDayTime() / 24000L);
        p.setData(ModAttachments.JOURNAL, j);
        p.displayClientMessage(Component.translatable("message.harmontide.sabbath.familiar"), false);
        return cat;
    }

    public static boolean isFamiliar(Cat cat, Player p) {
        CompoundTag j = journal(p);
        return j.hasUUID(FAMILIAR) && j.getUUID(FAMILIAR).equals(cat.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (p.tickCount % 100 != 13 || !(p.level() instanceof ServerLevel level) || !atLeast(p, Rank.KEEPER)) return;
        Cat cat = familiarOf(level, p);
        if (cat != null) {
            if (!level.isDay() && cat.distanceToSqr(p) <= FAMILIAR_NEAR * FAMILIAR_NEAR) p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 320, 0, true, false, true));
        } else if (BedSharing.fullMoon(level) && !level.isDay() && journal(p).getLong(FAMILIAR_DAY) != level.getDayTime() / 24000L) {
            UUID lost = journal(p).hasUUID(FAMILIAR) ? journal(p).getUUID(FAMILIAR) : null;
            if (lost == null || level.getEntity(lost) == null) familiar(level, p);    // only when she is truly gone, not just far away
        }
    }

    // --- the title ----------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onName(PlayerEvent.NameFormat event) {
        if (atLeast(event.getEntity(), Rank.SIBLING)) {
            event.setDisplayname(event.getDisplayname().copy().append(Component.translatable("title.harmontide.sabbath_sibling").withStyle(ChatFormatting.LIGHT_PURPLE)));
        }
    }
}
