package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * Wandering merchants (2026-11-11, after Grimoire of Gaia's traders). One wild grown girl in {@link #ODDS} (wanderingMerchants)
 * comes as a merchant, her pack full of the wares of her branch of the tree of life (Clade): dairy and wool from the cattle, silk
 * from the spinners, realm fruit from the devils, saplings from the flora and so on. An empty hand opens her trades (a real
 * trading screen); a trade is a little friendship. She never picks a fight while she is trading. After {@link #STAY} ticks she
 * moves on, unless someone has made a friend of her ({@link Orders#AFFECTION}): then she puts her pack down and stays, like any
 * girl. Not the merfolk (they have their sea merchant), not the elves (they trade with their friends already), not the legends.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class WanderingMerchants {
    public static final int ODDS = 25, STAY = 2 * 24000, WARES = 4, USES = 6;
    private static final String UNTIL = "MerchantUntil", OFFERS = "MerchantOffers", GREETED = "MerchantGreeted";

    /** One ware: so many of it for so many emeralds. */
    public record Ware(Supplier<? extends ItemLike> item, int count, int emeralds) {
        static Ware of(ItemLike item, int count, int emeralds) {
            return new Ware(() -> item, count, emeralds);
        }
    }

    public static final Map<Clade, List<Ware>> WARES_OF = new EnumMap<>(Clade.class);

    private static void put(List<Ware> wares, Clade... clades) {
        for (Clade c : clades) WARES_OF.put(c, wares);
    }

    static {
        put(List.of(Ware.of(Items.COD, 8, 1), Ware.of(Items.SALMON, 6, 1), Ware.of(Items.STRING, 8, 1), Ware.of(Items.RABBIT_HIDE, 4, 1),
                Ware.of(Items.FEATHER, 8, 1)), Clade.FELIFORMIA);
        put(List.of(Ware.of(Items.BONE, 8, 1), Ware.of(Items.LEATHER, 4, 1), Ware.of(Items.SWEET_BERRIES, 12, 1), Ware.of(Items.HONEYCOMB, 3, 1),
                Ware.of(Items.SALMON, 6, 1)), Clade.CANIDAE, Clade.ARCTOIDEA);
        put(List.of(Ware.of(Items.WHEAT_SEEDS, 16, 1), Ware.of(Items.CARROT, 8, 1), Ware.of(Items.GOLDEN_CARROT, 3, 2), Ware.of(Items.APPLE, 6, 1),
                Ware.of(Items.MELON_SEEDS, 8, 1), Ware.of(Items.COCOA_BEANS, 8, 1)), Clade.RODENTIA, Clade.LAGOMORPHA, Clade.PRIMATES, Clade.XENARTHRA,
                Clade.MARSUPIALIA, Clade.CHIROPTERA);
        put(List.of(Ware.of(Items.MILK_BUCKET, 1, 1), Ware.of(Items.LEATHER, 4, 1), Ware.of(Items.HAY_BLOCK, 2, 1), Ware.of(Items.WHITE_WOOL, 6, 1),
                Ware.of(Items.SADDLE, 1, 6), Ware.of(Items.WHEAT, 16, 1)), Clade.BOVIDAE, Clade.CERVIDAE, Clade.CAMELIDAE, Clade.SUINA,
                Clade.PERISSODACTYLA, Clade.PAENUNGULATA, Clade.WHIPPOMORPHA);
        put(List.of(Ware.of(Items.FEATHER, 8, 1), Ware.of(Items.EGG, 8, 1), Ware.of(Items.ARROW, 16, 1), Ware.of(Items.PUMPKIN_SEEDS, 12, 1)),
                Clade.AVES, Clade.ARCHOSAURIA);
        put(List.of(new Ware(ModItems.SHED_SKIN, 2, 2), Ware.of(Items.SLIME_BALL, 4, 2), Ware.of(Items.TURTLE_SCUTE, 1, 6), Ware.of(Items.LILY_PAD, 4, 1),
                Ware.of(Items.SEA_PICKLE, 4, 1)), Clade.TESTUDINES, Clade.SQUAMATA, Clade.AMPHIBIA);
        put(List.of(new Ware(ModItems.SPIDER_SILK, 4, 2), Ware.of(Items.HONEY_BOTTLE, 2, 1), Ware.of(Items.HONEYCOMB, 3, 1), Ware.of(Items.COBWEB, 2, 2),
                new Ware(ModItems.EXOSKELETON, 1, 3)), Clade.CHELICERATA, Clade.HEXAPODA);
        put(List.of(Ware.of(Items.PRISMARINE_SHARD, 6, 2), Ware.of(Items.INK_SAC, 4, 1), Ware.of(Items.NAUTILUS_SHELL, 1, 5), Ware.of(Items.KELP, 16, 1),
                Ware.of(Items.TROPICAL_FISH, 4, 1)), Clade.MALACOSTRACA, Clade.MOLLUSCA, Clade.ACTINOPTERYGII);
        put(List.of(Ware.of(Items.BONE_MEAL, 16, 1), Ware.of(Items.OAK_SAPLING, 4, 1), Ware.of(Items.CHERRY_SAPLING, 2, 2), Ware.of(Items.GLOW_BERRIES, 8, 2),
                Ware.of(Items.MOSS_BLOCK, 8, 1), Ware.of(Items.BROWN_MUSHROOM, 8, 1)), Clade.FLORA);
        put(List.of(Ware.of(Items.AMETHYST_SHARD, 4, 2), Ware.of(Items.BOOK, 2, 1), Ware.of(Items.LAPIS_LAZULI, 8, 2), Ware.of(Items.GLOW_BERRIES, 8, 2),
                Ware.of(Items.CANDLE, 4, 1)), Clade.FAE);
        put(List.of(Ware.of(Items.GOLD_INGOT, 2, 3), Ware.of(Items.DIAMOND, 1, 12), Ware.of(Items.BLAZE_ROD, 2, 3), Ware.of(Items.GOLD_NUGGET, 12, 1)), Clade.DRAGON);
        put(List.of(Ware.of(Items.BONE, 12, 1), Ware.of(Items.CANDLE, 4, 1), Ware.of(Items.PHANTOM_MEMBRANE, 1, 3), Ware.of(Items.SOUL_SAND, 8, 1),
                Ware.of(Items.SKELETON_SKULL, 1, 10)), Clade.UNDEAD);
        put(List.of(new Ware(ModItems.HEARTFRUIT, 2, 3), new Ware(ModItems.WEEPCAP, 2, 2), Ware.of(Items.BLAZE_POWDER, 4, 2), Ware.of(Items.NETHER_WART, 8, 2)),
                Clade.DEVIL);
        put(List.of(Ware.of(Items.SLIME_BALL, 8, 2), Ware.of(Items.MAGMA_CREAM, 2, 2), Ware.of(Items.HONEY_BLOCK, 1, 2), Ware.of(Items.CLAY_BALL, 12, 1)), Clade.SLIME);
        put(List.of(Ware.of(Items.IRON_INGOT, 4, 3), Ware.of(Items.COPPER_INGOT, 8, 2), Ware.of(Items.REDSTONE, 12, 2), Ware.of(Items.LANTERN, 2, 2)),
                Clade.CONSTRUCT, Clade.RIDER);
    }

    private WanderingMerchants() {}

    public static boolean isMerchant(Mob mob) {
        return mob.getPersistentData().contains(UNTIL);
    }

    /** Whether a girl of this kind may come as a wandering merchant. */
    public static boolean canTrade(GirlSpecies s) {
        return s.family() != GirlSpecies.Family.MERFOLK && s.family() != GirlSpecies.Family.ELF && !Rarity.LEGENDARY.contains(s.id());
    }

    /** She comes as a merchant: her pack is packed, and she is here to trade, not to fight. */
    public static void makeMerchant(ServerLevel level, AbstractMonsterGirl girl) {
        girl.getPersistentData().putLong(UNTIL, level.getGameTime() + STAY);
        girl.setFlag(AbstractMonsterGirl.FLAG_CALM, true);
        girl.setPersistenceRequired();
        save(girl, makeOffers(girl.getSpecies().clade(), girl.getRandom()));
    }

    /** WARES of her clade's wares, and one thing she buys back. */
    public static MerchantOffers makeOffers(Clade clade, RandomSource random) {
        List<Ware> all = new ArrayList<>(WARES_OF.getOrDefault(clade, WARES_OF.get(Clade.FAE)));
        MerchantOffers o = new MerchantOffers();
        for (int i = 0; i < WARES && !all.isEmpty(); i++) {
            Ware w = all.remove(random.nextInt(all.size()));
            o.add(new MerchantOffer(new ItemCost(Items.EMERALD, w.emeralds()), new ItemStack(w.item().get(), w.count()), USES, 2, 0.05F));
        }
        o.add(new MerchantOffer(new ItemCost(Items.BREAD, 6), new ItemStack(Items.EMERALD), 12, 1, 0.05F));   // food for the road
        return o;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.isSpawnCancelled() || !(event.getEntity() instanceof AbstractMonsterGirl girl) || girl.isBaby() || !(girl.level() instanceof ServerLevel level)) return;
        if (event.getSpawnType() != MobSpawnType.NATURAL && event.getSpawnType() != MobSpawnType.CHUNK_GENERATION) return;
        if (net.neoforged.neoforge.gametest.GameTestHooks.isGametestServer() || !MonsterGirlsConfig.wanderingMerchants() || !canTrade(girl.getSpecies())) return;
        if (girl.getRandom().nextInt(ODDS) == 0) makeMerchant(level, girl);
    }

    // --- her offers, kept with her ----------------------------------------------------------------------------------

    private static final Map<Mob, Stall> STALLS = new WeakHashMap<>();

    public static MerchantOffers offers(Mob mob) {
        if (!mob.getPersistentData().contains(OFFERS) || !(mob.level() instanceof ServerLevel level)) return new MerchantOffers();
        return MerchantOffers.CODEC.parse(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), mob.getPersistentData().get(OFFERS))
                .resultOrPartial(e -> {}).orElseGet(MerchantOffers::new);
    }

    static void save(Mob mob, MerchantOffers offers) {
        mob.getPersistentData().put(OFFERS, MerchantOffers.CODEC.encodeStart(mob.registryAccess().createSerializationContext(NbtOps.INSTANCE), offers).getOrThrow());
    }

    /** Her side of the trading screen (the screen's merchant is this, not the girl herself). */
    public static final class Stall implements Merchant {
        private final AbstractMonsterGirl girl;
        @Nullable
        private Player trading;
        private MerchantOffers offers;

        Stall(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.offers = WanderingMerchants.offers(girl);
        }

        @Override
        public void setTradingPlayer(@Nullable Player player) {
            this.trading = player;
            if (player == null) save(this.girl, this.offers);
        }

        @Nullable
        @Override
        public Player getTradingPlayer() {
            return this.trading;
        }

        @Override
        public MerchantOffers getOffers() {
            return this.offers;
        }

        @Override
        public void overrideOffers(MerchantOffers offers) {
            this.offers = offers;
        }

        @Override
        public void notifyTrade(MerchantOffer offer) {
            offer.increaseUses();
            if (this.trading != null) this.girl.addAffection(this.trading, 1);
            this.girl.playSound(SoundEvents.VILLAGER_YES, 0.6F, 1.4F);
            save(this.girl, this.offers);
        }

        @Override
        public void notifyTradeUpdated(ItemStack stack) {
        }

        @Override
        public int getVillagerXp() {
            return 0;
        }

        @Override
        public void overrideXp(int xp) {
        }

        @Override
        public boolean showProgressBar() {
            return false;
        }

        @Override
        public SoundEvent getNotifyTradeSound() {
            return SoundEvents.VILLAGER_YES;
        }

        @Override
        public boolean isClientSide() {
            return this.girl.level().isClientSide;
        }
    }

    /** Her stall for this player: the trading screen opens on it. Null when she isn't trading (or is busy with someone else). */
    @Nullable
    public static Stall open(AbstractMonsterGirl girl, Player player) {
        if (!isMerchant(girl) || girl.isBaby()) return null;
        Stall stall = STALLS.computeIfAbsent(girl, m -> new Stall(girl));
        if (stall.getTradingPlayer() != null && stall.getTradingPlayer() != player) return null;
        stall.setTradingPlayer(player);
        return stall;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Player p = event.getEntity();
        if (!(event.getTarget() instanceof AbstractMonsterGirl girl) || event.getHand() != InteractionHand.MAIN_HAND || !isMerchant(girl)
                || !event.getItemStack().isEmpty() || p.isShiftKeyDown()) return;
        if (!girl.level().isClientSide) {
            Stall stall = open(girl, p);
            if (stall != null) stall.openTradingScreen(p, Component.translatable("entity.monstergirls.wandering_merchant", girl.getDisplayName()), 1);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(girl.level().isClientSide));
    }

    // --- greeting, moving on ----------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % 100 != 23 || !(event.getEntity() instanceof AbstractMonsterGirl girl) || !(girl.level() instanceof ServerLevel level)
                || !isMerchant(girl)) return;
        update(level, girl);
    }

    /** Every five seconds: she greets whoever comes near (once each), and when her time is up she moves on, or stays with a friend. Whether she is still trading. */
    public static boolean update(ServerLevel level, AbstractMonsterGirl girl) {
        if (level.getGameTime() > girl.getPersistentData().getLong(UNTIL)) {
            girl.getPersistentData().remove(UNTIL);
            girl.getPersistentData().remove(OFFERS);
            girl.getPersistentData().remove(GREETED);
            STALLS.remove(girl);
            if (girl.bestAffection() >= Orders.AFFECTION || girl.hasCustomName()) {      // a friend: she stays
                level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getY() + girl.getBbHeight(), girl.getZ(), 6, 0.4, 0.2, 0.4, 0.0);
            } else {                                                                // off down the road
                level.sendParticles(ParticleTypes.POOF, girl.getX(), girl.getY() + girl.getBbHeight() * 0.5, girl.getZ(), 16, 0.4, 0.6, 0.4, 0.02);
                girl.discard();
            }
            return false;
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, girl.getX(), girl.getY() + girl.getBbHeight() + 0.2, girl.getZ(), 2, 0.3, 0.1, 0.3, 0.0);
        var greeted = girl.getPersistentData().getCompound(GREETED);
        for (Player p : level.getEntitiesOfClass(Player.class, girl.getBoundingBox().inflate(8))) {
            if (greeted.contains(p.getStringUUID())) continue;
            greeted.putBoolean(p.getStringUUID(), true);
            p.displayClientMessage(Component.translatable("message.monstergirls.wandering_merchant.greet", girl.getDisplayName()).withStyle(ChatFormatting.GREEN), false);
        }
        girl.getPersistentData().put(GREETED, greeted);
        return true;
    }

    /** For tests: her time is up now. */
    public static void endNow(Mob mob) {
        mob.getPersistentData().putLong(UNTIL, 0);
    }
}
