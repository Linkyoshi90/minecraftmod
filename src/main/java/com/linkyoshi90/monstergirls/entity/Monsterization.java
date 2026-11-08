package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.RealmGrassBlock;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModGirls;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Monsterization, after Monster Girl Encyclopedia (villagers, pigs and cats are possessed instead, keeping their bodies: {@link #POSSESSED}): the Demon Realm's energy turns the creatures that linger in it into monster girls.
 * A grown vanilla animal or monster standing on realm grass in the dark (at night, or out of the sky) soaks it up, one step every
 * {@link #EVERY} ticks; after {@link #STEPS} she changes into the girl of her kind ({@link #KINDS}): a witch into a succubus, a cow
 * into a holstaur, a slime into a slime girl. Only vanilla creatures change: girls of the mod stay who they are.
 * <p>
 * She keeps her name. A tamed pet keeps her owner, now as a devoted friend ({@link #PET_AFFECTION}). The realm's light rules hold:
 * no realm, no change. Behind the monsterization option.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Monsterization {
    public static final int EVERY = 199, STEPS = 12, PET_AFFECTION = 60;
    private static final String SOAKED = "RealmSoaked";
    public static final Map<EntityType<?>, List<GirlSpecies>> KINDS = new HashMap<>();
    /** Creatures the realm possesses instead of changing: what their young are born as. */
    public static final Map<EntityType<?>, List<GirlSpecies>> POSSESSED = new HashMap<>();
    /** Creatures that become a harpy or a frog girl (not species of the girl table): what each one becomes. */
    public static final Map<EntityType<?>, java.util.function.Function<Mob, EntityType<? extends Mob>>> OTHERS = new HashMap<>();
    /** Possessed creatures whose young are harpies (they hatch, not born of the girl table). */
    public static final Map<EntityType<?>, java.util.function.Supplier<EntityType<? extends Mob>>> HATCHES = new HashMap<>();
    /** A possessed creature breeds at most this often (half a day), with one of her own kind within BREED_RANGE, unless CROWD girls already live within 32. */
    public static final int BREED_EVERY = 12000, BREED_RANGE = 8, CROWD = 6;
    private static final String POSSESSED_TAG = "RealmPossessed", NEXT_BREED = "RealmNextBreed";

    static {
        KINDS.put(EntityType.WITCH, List.of(GirlSpecies.SUCCUBUS_DEVIL));
        KINDS.put(EntityType.VEX, List.of(GirlSpecies.LESSER_DEVIL));
        KINDS.put(EntityType.ZOMBIE, List.of(GirlSpecies.ZOMBIE_UNDEAD));
        KINDS.put(EntityType.HUSK, List.of(GirlSpecies.MUMMY_UNDEAD));
        KINDS.put(EntityType.DROWNED, List.of(GirlSpecies.DROWNED_UNDEAD));
        KINDS.put(EntityType.SKELETON, List.of(GirlSpecies.SKELETON_UNDEAD));
        KINDS.put(EntityType.PHANTOM, List.of(GirlSpecies.PHANTOM_UNDEAD));
        KINDS.put(EntityType.SLIME, List.of(GirlSpecies.VERDANT_SLIME));
        KINDS.put(EntityType.MAGMA_CUBE, List.of(GirlSpecies.MAGMA_SLIME));
        KINDS.put(EntityType.SPIDER, List.of(GirlSpecies.TARANTULA_ARACHNE, GirlSpecies.WOLF_SPIDER_ARACHNE));
        KINDS.put(EntityType.CAVE_SPIDER, List.of(GirlSpecies.CAVE_SPIDER_ARACHNE));
        KINDS.put(EntityType.BEE, List.of(GirlSpecies.BEE_BUG));
        KINDS.put(EntityType.ALLAY, List.of(GirlSpecies.FAIRY_BUG));
        KINDS.put(EntityType.BAT, List.of(GirlSpecies.CAVE_BAT));
        KINDS.put(EntityType.OCELOT, List.of(GirlSpecies.OCELOT_NEKOMATA));
        KINDS.put(EntityType.WOLF, List.of(GirlSpecies.WOLF_CANID));
        KINDS.put(EntityType.POLAR_BEAR, List.of(GirlSpecies.POLAR_BEAR_CANID));
        KINDS.put(EntityType.PANDA, List.of(GirlSpecies.PANDA_CANID));
        KINDS.put(EntityType.FOX, List.of(GirlSpecies.RED_KITSUNE));
        KINDS.put(EntityType.RABBIT, List.of(GirlSpecies.JACK_JACKALOPE));
        KINDS.put(EntityType.COW, List.of(GirlSpecies.HOLSTAUR_MINOTAUR));
        KINDS.put(EntityType.MOOSHROOM, List.of(GirlSpecies.SHROOMOTAUR_MINOTAUR));
        KINDS.put(EntityType.SHEEP, List.of(GirlSpecies.SHEEP_MINOTAUR));
        KINDS.put(EntityType.GOAT, List.of(GirlSpecies.GOAT_CENTAUR));
        KINDS.put(EntityType.HORSE, List.of(GirlSpecies.HORSE_CENTAUR));
        KINDS.put(EntityType.DONKEY, List.of(GirlSpecies.DONKEY_CENTAUR));
        KINDS.put(EntityType.LLAMA, List.of(GirlSpecies.VICUNERO_CENTAUR));
        KINDS.put(EntityType.CAMEL, List.of(GirlSpecies.CAMEL_CENTAUR));
        KINDS.put(EntityType.ARMADILLO, List.of(GirlSpecies.ARMADILLO_ARMOURED));
        KINDS.put(EntityType.AXOLOTL, List.of(GirlSpecies.AXOLOTL_MERMAID));
        KINDS.put(EntityType.DOLPHIN, List.of(GirlSpecies.DOLPHIN_MERMAID));
        KINDS.put(EntityType.SQUID, List.of(GirlSpecies.SQUIDKIN_MOLLUSK));
        KINDS.put(EntityType.IRON_GOLEM, List.of(GirlSpecies.GOLEM_CONSTRUCT));
        KINDS.put(EntityType.SNOW_GOLEM, List.of(GirlSpecies.SNOWGIRL_CONSTRUCT));
        // the rest of the vanilla menagerie (2026-11-08)
        KINDS.put(EntityType.MULE, List.of(GirlSpecies.DONKEY_CENTAUR));
        KINDS.put(EntityType.TRADER_LLAMA, List.of(GirlSpecies.VICUNERO_CENTAUR));
        KINDS.put(EntityType.STRAY, List.of(GirlSpecies.SKELETON_UNDEAD));
        KINDS.put(EntityType.ZOMBIE_VILLAGER, List.of(GirlSpecies.ZOMBIE_UNDEAD));
        KINDS.put(EntityType.PIGLIN, List.of(GirlSpecies.PIGLETTE_ARMOURED));
        KINDS.put(EntityType.HOGLIN, List.of(GirlSpecies.ZOGLINTAUR_CENTAUR));
        KINDS.put(EntityType.ZOGLIN, List.of(GirlSpecies.ZOGLINTAUR_CENTAUR));
        KINDS.put(EntityType.ENDERMAN, List.of(GirlSpecies.ENDER_SLIME));
        KINDS.put(EntityType.TURTLE, List.of(GirlSpecies.TURTLE_LIZARDFOLK));
        KINDS.put(EntityType.PUFFERFISH, List.of(GirlSpecies.PUFFERFISH_MERMAID));
        KINDS.put(EntityType.GLOW_SQUID, List.of(GirlSpecies.SQUIDKIN_MOLLUSK));
        // harpies and frogs are entities of their own, not species of the girl table
        OTHERS.put(EntityType.PARROT, mob -> com.linkyoshi90.monstergirls.registry.ModEntities.MACAW_HARPY.get());
        OTHERS.put(EntityType.FROG, mob -> {
            var v = ((net.minecraft.world.entity.animal.frog.Frog) mob).getVariant();
            if (v.is(net.minecraft.world.entity.animal.FrogVariant.WARM)) return com.linkyoshi90.monstergirls.registry.ModEntities.TREE_FROG.get();
            if (v.is(net.minecraft.world.entity.animal.FrogVariant.COLD)) return com.linkyoshi90.monstergirls.registry.ModEntities.BULLFROG.get();
            return com.linkyoshi90.monstergirls.registry.ModEntities.BULLYWUG.get();
        });
        POSSESSED.put(EntityType.WANDERING_TRADER, List.of(GirlSpecies.SUCCUBUS_DEVIL, GirlSpecies.DEMON_DEVIL));
        POSSESSED.put(EntityType.CHICKEN, List.of());                     // her young hatch as harpies instead (HATCHES)
        HATCHES.put(EntityType.CHICKEN, () -> com.linkyoshi90.monstergirls.registry.ModEntities.POULTRY_HARPY.get());
        // possessed: they keep their own bodies, and their young are girls (the list is weighted by repeats)
        POSSESSED.put(EntityType.VILLAGER, List.of(GirlSpecies.SUCCUBUS_DEVIL, GirlSpecies.SUCCUBUS_DEVIL, GirlSpecies.DEMON_DEVIL));
        POSSESSED.put(EntityType.PIG, List.of(GirlSpecies.PIGLETTE_ARMOURED));
        POSSESSED.put(EntityType.CAT, List.of(GirlSpecies.BAKENEKO_NEKOMATA, GirlSpecies.BAKENEKO_NEKOMATA, GirlSpecies.CAIT_SITH_NEKOMATA, GirlSpecies.CHESHIRE_NEKOMATA));
    }

    private Monsterization() {
    }

    /** The girl this creature would become, or null if she can't change (not a vanilla kind on the list, or a baby). */
    @Nullable
    public static GirlSpecies kindOf(Mob mob) {
        List<GirlSpecies> kinds = KINDS.get(mob.getType());
        if (kinds == null || mob.isBaby()) return null;
        if (mob instanceof Fox fox && fox.getVariant() == Fox.Type.SNOW) return GirlSpecies.ARCTIC_KITSUNE;
        return kinds.get(Math.floorMod(mob.getUUID().hashCode(), kinds.size()));
    }

    /** Dark enough for the realm to work: night, or under a roof. */
    public static boolean dark(ServerLevel level, Mob mob) {
        return !level.isDay() || !level.canSeeSky(mob.blockPosition().above());
    }

    public static int soaked(Mob mob) {
        return mob.getPersistentData().getInt(SOAKED);
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        Entity e = event.getEntity();
        if (e.tickCount % EVERY != 0 || !(e instanceof Mob mob) || e instanceof Bonded || !(e.level() instanceof ServerLevel level)) return;
        if (KINDS.containsKey(e.getType())) step(level, mob, dark(level, mob));
        else if (OTHERS.containsKey(e.getType())) stepOther(level, mob, dark(level, mob));
        else if (POSSESSED.containsKey(e.getType())) {
            if (possessed(mob)) {
                murmur(level, mob);
                giftOwner(level, mob, level.getDayTime() % 24000L < 2000L);
                if (mob.getRandom().nextInt(SPREAD / RoseMoon.stir(level)) == 0) spread(level, mob, !level.isDay());
                if (mob.getRandom().nextInt(ROOT) == 0) takeRoot(level, mob, !level.isDay());
                breed(level, mob);
                deliver(level, mob);
            }
            else soak(level, mob, dark(level, mob));
        }
    }

    /** One step in the realm (dark: whether it is dark enough). The girl she became, or null. */
    @Nullable
    public static AbstractMonsterGirl step(ServerLevel level, Mob mob, boolean dark) {
        GirlSpecies kind = kindOf(mob);
        if (kind == null || !MonsterGirlsConfig.monsterization()) return null;
        if (!dark || !(DemonRealm.inRealm(mob) || guardsTheTurned(mob)) || com.linkyoshi90.monstergirls.block.HolyFontBlock.warded(level, mob.blockPosition())) return null;
        int soaked = soaked(mob) + (RoseMoon.tonight(level) ? 2 : 1);   // a rose moon: twice as fast
        mob.getPersistentData().putInt(SOAKED, soaked);
        level.sendParticles(RealmGrassBlock.HAZE, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 3 + soaked / 2, 0.3, 0.3, 0.3, 0.0);
        return soaked >= STEPS ? change(level, mob, kind) : null;
    }

    /** One step in the realm for a creature that becomes a harpy or a frog girl. What she became, or null. */
    @Nullable
    public static Mob stepOther(ServerLevel level, Mob mob, boolean dark) {
        var into = OTHERS.get(mob.getType());
        if (into == null || mob.isBaby() || !MonsterGirlsConfig.monsterization() || !dark || !DemonRealm.inRealm(mob) || com.linkyoshi90.monstergirls.block.HolyFontBlock.warded(level, mob.blockPosition())) return null;
        int soaked = soaked(mob) + (RoseMoon.tonight(level) ? 2 : 1);   // a rose moon: twice as fast
        mob.getPersistentData().putInt(SOAKED, soaked);
        level.sendParticles(RealmGrassBlock.HAZE, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 3 + soaked / 2, 0.3, 0.3, 0.3, 0.0);
        return soaked >= STEPS ? changeInto(level, mob, into.apply(mob)) : null;
    }

    /** What a creature would become if it isn't one of the girl table's kinds (a harpy, a frog girl), or null. */
    @Nullable
    public static EntityType<? extends Mob> otherOf(Mob mob) {
        var into = OTHERS.get(mob.getType());
        return into == null || mob.isBaby() ? null : into.apply(mob);
    }

    /** One step in the realm for a creature that is possessed rather than changed. True once she is possessed. */
    public static boolean soak(ServerLevel level, Mob mob, boolean dark) {
        if (!POSSESSED.containsKey(mob.getType()) || mob.isBaby() || possessed(mob) || !MonsterGirlsConfig.possession()) return possessed(mob);
        if (!dark || !DemonRealm.inRealm(mob) || com.linkyoshi90.monstergirls.block.HolyFontBlock.warded(level, mob.blockPosition())) return false;
        int soaked = soaked(mob) + (RoseMoon.tonight(level) ? 2 : 1);   // a rose moon: twice as fast
        mob.getPersistentData().putInt(SOAKED, soaked);
        level.sendParticles(RealmGrassBlock.HAZE, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 3 + soaked / 2, 0.3, 0.3, 0.3, 0.0);
        if (soaked < STEPS) return false;
        possess(level, mob);
        return true;
    }

    /** She is possessed, here and now: her own body still, the realm in her, and eager. */
    public static void possess(ServerLevel level, Mob mob) {
        mob.getPersistentData().remove(SOAKED);
        mob.getPersistentData().putBoolean(POSSESSED_TAG, true);
        mob.getPersistentData().putLong(NEXT_BREED, level.getGameTime() + 200);
        mob.setPersistenceRequired();
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY(), mob.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
        level.sendParticles(RealmGrassBlock.HAZE, mob.getX(), mob.getY() + 0.5, mob.getZ(), 30, 0.5, 0.5, 0.5, 0.0);
        mob.playSound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 0.7F, 1.6F);
        sync(mob);
        if (mob instanceof net.minecraft.world.entity.npc.AbstractVillager merchant) realmTrades(merchant, true);
        tempt(mob);
    }

    /** A possessed animal follows a heartfruit held out to her (villagers think with their own brains, so not them). */
    public static void tempt(Mob mob) {
        if (mob instanceof net.minecraft.world.entity.animal.Animal animal) {
            animal.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.TemptGoal(animal, 1.1,
                    net.minecraft.world.item.crafting.Ingredient.of(com.linkyoshi90.monstergirls.registry.ModItems.HEARTFRUIT.get()), false));
        }
    }

    /** One wandering trader in this many arrives already possessed, bringing the realm's goods to players who never found it. */
    public static final int TRADER_POSSESSED = 8;

    @SubscribeEvent
    public static void onTraderArrives(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof net.minecraft.world.entity.npc.WanderingTrader trader)
                || !MonsterGirlsConfig.possession() || possessed(trader) || net.neoforged.neoforge.gametest.GameTestHooks.isGametestServer()) return;
        if (trader.getRandom().nextInt(TRADER_POSSESSED) == 0) level.getServer().execute(() -> {
            if (trader.isAlive()) possess(level, trader);                   // once she is in the world, so her trades and eyes reach the players
        });
    }

    /** Loaded again (goals are not saved): a possessed animal is tempted by heartfruit again. */
    @SubscribeEvent
    public static void onJoin(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Mob mob && POSSESSED.containsKey(mob.getType()) && possessed(mob)) tempt(mob);
    }

    /**
     * A heartfruit fed to a possessed creature: she is ready for one of her kind again at once (her half day's wait is over).
     * True if she took it.
     */
    /** A heartfruit given to a possessed villager: tonight she seeks this player's bed, from as far as SEEK_RANGE. True if she took it. */
    public static boolean promise(ServerLevel level, Mob mob, Player player) {
        if (!eagerForBed(mob)) return false;
        mob.getPersistentData().putUUID(SEEKS, player.getUUID());
        mob.getPersistentData().putLong(SEEKS_UNTIL, level.getGameTime() + 24000L - level.getDayTime() % 24000L);   // until this morning ends the night
        mob.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
        player.displayClientMessage(Component.translatable("message.monstergirls.possessed.promise", mob.getDisplayName()), true);
        return true;
    }

    public static final int SEEK_RANGE = 48;
    private static final String SEEKS = "RealmSeeks", SEEKS_UNTIL = "RealmSeeksUntil";

    /** She took this player's heartfruit and the night it promised isn't over. */
    public static boolean seeks(Mob mob, Player player) {
        return mob.getPersistentData().hasUUID(SEEKS) && player.getUUID().equals(mob.getPersistentData().getUUID(SEEKS))
                && mob.level().getGameTime() < mob.getPersistentData().getLong(SEEKS_UNTIL);
    }

    /** The one coming to this player's bed: one who took their heartfruit first (from SEEK_RANGE), else the nearest within range. */
    @Nullable
    private static Mob bedMate(ServerLevel level, Player player, int range) {
        var near = java.util.Comparator.<Mob>comparingDouble(m -> m.distanceToSqr(player));
        Mob promised = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(SEEK_RANGE), m -> eagerForBed(m) && seeks(m, player)).stream()
                .min(near).orElse(null);
        if (promised != null) return promised;
        return level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(range), Monsterization::eagerForBed).stream().min(near).orElse(null);
    }

    public static boolean feed(ServerLevel level, Mob mob) {
        if (mob instanceof net.minecraft.world.entity.npc.AbstractVillager) return false;   // villagers come to a player's bed instead
        if (!possessed(mob) || mob.isBaby() || mob.getPersistentData().getLong(NEXT_BREED) <= level.getGameTime()) return false;
        mob.getPersistentData().putLong(NEXT_BREED, level.getGameTime());
        mob.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
        return true;
    }

    @SubscribeEvent
    public static void onInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (!event.getItemStack().is(com.linkyoshi90.monstergirls.registry.ModItems.HEARTFRUIT.get()) || !(event.getTarget() instanceof Mob mob)
                || mob instanceof Bonded || !POSSESSED.containsKey(mob.getType()) || !possessed(mob)) return;
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getLevel() instanceof ServerLevel level && (feed(level, mob) || promise(level, mob, event.getEntity()))) event.getItemStack().consume(1, event.getEntity());
    }

    /** What a possessed villager or wandering trader deals in besides her trade: the realm's goods (or, freed, she stops). */
    public static void realmTrades(net.minecraft.world.entity.npc.AbstractVillager merchant, boolean add) {
        var offers = merchant.getOffers();
        offers.removeIf(o -> realmGood(o.getResult()) || o.getCostA().is(com.linkyoshi90.monstergirls.registry.ModItems.HEARTFRUIT.get()));
        if (!add) return;
        var emerald = new net.minecraft.world.item.trading.ItemCost(net.minecraft.world.item.Items.EMERALD, 1);
        offers.add(new net.minecraft.world.item.trading.MerchantOffer(emerald, new net.minecraft.world.item.ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.HEARTFRUIT.get(), 3), 12, 3, 0.05F));
        offers.add(new net.minecraft.world.item.trading.MerchantOffer(new net.minecraft.world.item.trading.ItemCost(net.minecraft.world.item.Items.EMERALD, 2),
                new net.minecraft.world.item.ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.WEEPCAP.get(), 2), 8, 5, 0.05F));
        offers.add(new net.minecraft.world.item.trading.MerchantOffer(new net.minecraft.world.item.trading.ItemCost(net.minecraft.world.item.Items.EMERALD, 5),
                java.util.Optional.of(new net.minecraft.world.item.trading.ItemCost(net.minecraft.world.item.Items.AMETHYST_SHARD, 2)),
                new net.minecraft.world.item.ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.REALM_CENSER.get()), 3, 10, 0.05F));
        offers.add(new net.minecraft.world.item.trading.MerchantOffer(new net.minecraft.world.item.trading.ItemCost(com.linkyoshi90.monstergirls.registry.ModItems.HEARTFRUIT.get(), 6),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD), 12, 3, 0.05F));
        if (merchant instanceof net.minecraft.world.entity.npc.WanderingTrader) {     // a travelling one knows the way to a realm
            var map = new net.minecraft.world.entity.npc.VillagerTrades.TreasureMapForEmeralds(8, REALM_PATCHES, REALM_MAP,
                    net.minecraft.world.level.saveddata.maps.MapDecorationTypes.RED_X, 1, 10).getOffer(merchant, merchant.getRandom());
            if (map != null) offers.add(map);
        }
    }

    public static final net.minecraft.tags.TagKey<net.minecraft.world.level.levelgen.structure.Structure> REALM_PATCHES =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.STRUCTURE, MonsterGirlsMod.id("realm_patches"));
    public static final String REALM_MAP = "filled_map.monstergirls.realm";

    private static boolean realmGood(net.minecraft.world.item.ItemStack stack) {
        var name = stack.get(net.minecraft.core.component.DataComponents.ITEM_NAME);
        if (name != null && name.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t && REALM_MAP.equals(t.getKey())) return true;
        return stack.is(com.linkyoshi90.monstergirls.registry.ModItems.HEARTFRUIT.get()) || stack.is(com.linkyoshi90.monstergirls.registry.ModItems.WEEPCAP.get())
                || stack.is(com.linkyoshi90.monstergirls.registry.ModItems.REALM_CENSER.get());
    }

    /** The players who saw something happen here (within 16 blocks) get this advancement. */
    static void witnessed(ServerLevel level, Mob mob, String advancement) {
        for (Player p : level.getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(16))) com.linkyoshi90.monstergirls.entity.Dairy.award(p, advancement);
    }

    /** Tells everyone who can see her whether she is possessed (her eyes glow). */
    public static void sync(Mob mob) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(mob,
                new com.linkyoshi90.monstergirls.network.ModPayloads.Possessed(mob.getId(), possessed(mob)));
    }

    /** A player who comes into sight of a possessed creature learns it too. */
    @SubscribeEvent
    public static void onStartTracking(net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Mob mob && possessed(mob) && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new com.linkyoshi90.monstergirls.network.ModPayloads.Possessed(mob.getId(), true));
        }
    }

    /** Possessed from the start, quietly (a wild realm patch's residents, as the world is made). */
    public static void markPossessed(Mob mob) {
        mob.getPersistentData().putBoolean(POSSESSED_TAG, true);
        mob.setPersistenceRequired();
    }

    public static boolean possessed(Mob mob) {
        return mob.getPersistentData().getBoolean(POSSESSED_TAG);
    }

    /** Now and then a possessed creature calls, low and changed, with the realm's sigh under it. True if she did. */
    public static boolean murmur(ServerLevel level, Mob mob) {
        if (!possessed(mob) || mob.getRandom().nextInt(4) != 0) return false;
        net.minecraft.sounds.SoundEvent call = mob.getType() == EntityType.VILLAGER ? SoundEvents.VILLAGER_AMBIENT
                : mob.getType() == EntityType.WANDERING_TRADER ? SoundEvents.WANDERING_TRADER_AMBIENT
                : mob.getType() == EntityType.PIG ? SoundEvents.PIG_AMBIENT
                : mob.getType() == EntityType.CAT ? SoundEvents.CAT_PURREOW
                : mob.getType() == EntityType.CHICKEN ? SoundEvents.CHICKEN_AMBIENT : null;
        if (call == null) return false;
        mob.playSound(call, 0.8F, 0.6F + mob.getRandom().nextFloat() * 0.1F);
        level.sendParticles(RealmGrassBlock.HAZE, mob.getX(), mob.getEyeY(), mob.getZ(), 4, 0.2, 0.2, 0.2, 0.0);
        return true;
    }

    /**
     * A possessed cat who has an owner brings them a heartfruit each morning, as cats bring presents: once a day, if they
     * are within 16 blocks. True if she did.
     */
    public static boolean giftOwner(ServerLevel level, Mob mob, boolean morning) {
        if (!morning || !possessed(mob) || !(mob instanceof net.minecraft.world.entity.TamableAnimal cat) || cat.getOwner() == null) return false;
        long day = level.getDayTime() / 24000L;
        if (mob.getPersistentData().contains(GIFT_DAY) && mob.getPersistentData().getLong(GIFT_DAY) == day) return false;
        net.minecraft.world.entity.LivingEntity owner = cat.getOwner();
        if (owner.distanceToSqr(mob) > 16 * 16) return false;
        mob.getPersistentData().putLong(GIFT_DAY, day);
        net.minecraft.world.entity.item.ItemEntity gift = new net.minecraft.world.entity.item.ItemEntity(level, owner.getX(), owner.getY() + 0.5, owner.getZ(),
                new net.minecraft.world.item.ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.HEARTFRUIT.get()));
        level.addFreshEntity(gift);
        mob.playSound(SoundEvents.CAT_PURR, 1.0F, 0.8F);
        level.sendParticles(ParticleTypes.HEART, owner.getX(), owner.getY() + 0.8, owner.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
        return true;
    }

    private static final String GIFT_DAY = "RealmGiftDay";

    /** A village's iron golem among this many possessed villagers (within GUARD_RANGE) soaks up the realm as if she stood in it. */
    public static final int TURNED_VILLAGE = 3, GUARD_RANGE = 8;

    /** An iron golem guarding a village where TURNED_VILLAGE villagers near her are possessed. */
    public static boolean guardsTheTurned(Mob mob) {
        return mob.getType() == EntityType.IRON_GOLEM && mob.level().getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(GUARD_RANGE),
                m -> m.getType() == EntityType.VILLAGER && possessed(m)).size() >= TURNED_VILLAGE;
    }

    /** One look round in this many a possessed villager, at night, lets the realm take root under her. */
    public static final int ROOT = 40, ROOT_DEPTH = RealmGrassBlock.MAX_DEPTH - 2;

    /**
     * At night the realm takes root under a possessed villager: the grass she stands on turns, near the realm's edge (it spreads
     * only a couple of blocks), so a turned village grows little patches of its own. Where, or null.
     */
    @Nullable
    public static net.minecraft.core.BlockPos takeRoot(ServerLevel level, Mob mob, boolean night) {
        if (!night || mob.getType() != EntityType.VILLAGER || !possessed(mob) || !MonsterGirlsConfig.demonRealm()) return null;
        net.minecraft.core.BlockPos ground = mob.getOnPos();
        return RealmGrassBlock.corrupt(level, ground, ROOT_DEPTH) ? ground : null;
    }

    /** One look round in this many (each EVERY ticks) a possessed villager passes it on: about one night in four. */
    public static final int SPREAD = 200, SPREAD_RANGE = 4;

    /**
     * At night a possessed villager passes it on to a grown villager standing within SPREAD_RANGE, so a village slowly turns
     * (behind the possessionSpreads option). Who she passed it to, or null.
     */
    @Nullable
    public static Mob spread(ServerLevel level, Mob mob, boolean night) {
        if (!night || mob.getType() != EntityType.VILLAGER || !possessed(mob) || !MonsterGirlsConfig.possession() || !MonsterGirlsConfig.possessionSpreads()) return null;
        Mob next = level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(SPREAD_RANGE),
                m -> m != mob && m.getType() == EntityType.VILLAGER && !m.isBaby() && !possessed(m)).stream().findFirst().orElse(null);
        if (next == null || com.linkyoshi90.monstergirls.block.HolyFontBlock.warded(level, next.blockPosition())) return null;
        possess(level, next);
        return next;
    }

    /** How long a possessed villager carries before her young is born (a day, as a girl does), and how near the bed she must be. */
    public static final int CARRY = 24000, BED_RANGE = 12, BED_CALL = 24, GOSSIP = 20;
    private static final String CARRYING_BY = "RealmCarryingBy", BIRTH_AT = "RealmBirthAt";

    /** Grown, possessed, a villager or wandering trader, and not carrying already: she would share a sleeper's bed tonight. */
    public static boolean eagerForBed(Mob mob) {
        return mob instanceof net.minecraft.world.entity.npc.AbstractVillager && possessed(mob) && !mob.isBaby() && !carrying(mob);
    }

    public static boolean carrying(Mob mob) {
        return mob.getPersistentData().hasUUID(CARRYING_BY);
    }

    /**
     * The night a player slept through: the nearest possessed villager or wandering trader within BED_RANGE of their bed slipped
     * into it, and now carries their young (a little succubus or demon, born a day later and devoted to them, as any girl's
     * young are). Not while CROWD girls already live near. Who, or null.
     */
    @Nullable
    public static Mob shareNight(ServerLevel level, Player player) {
        if (!MonsterGirlsConfig.possession()) return null;
        Mob she = bedMate(level, player, BED_RANGE);
        if (she != null && she.distanceToSqr(player) > BED_RANGE * BED_RANGE) she = null;                 // promised, but she never got there
        if (she == null || level.getEntitiesOfClass(AbstractMonsterGirl.class, she.getBoundingBox().inflate(32)).size() >= CROWD) return null;
        she.getPersistentData().remove(SEEKS);
        she.getPersistentData().putUUID(CARRYING_BY, player.getUUID());
        she.getPersistentData().putLong(BIRTH_AT, level.getGameTime() + CARRY);
        level.sendParticles(ParticleTypes.HEART, she.getX(), she.getEyeY() + 0.3, she.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
        player.displayClientMessage(Component.translatable("message.monstergirls.possessed.bed", she.getDisplayName()), false);
        return she;
    }

    /** Her day is up: her young is born beside her, devoted to the player whose bed she shared. The young, or null. */
    @Nullable
    public static AbstractMonsterGirl deliver(ServerLevel level, Mob mob) {
        if (!carrying(mob) || level.getGameTime() < mob.getPersistentData().getLong(BIRTH_AT)) return null;
        java.util.UUID father = mob.getPersistentData().getUUID(CARRYING_BY);
        mob.getPersistentData().remove(CARRYING_BY);
        mob.getPersistentData().remove(BIRTH_AT);
        List<GirlSpecies> kinds = POSSESSED.get(mob.getType());
        if (kinds == null || kinds.isEmpty()) return null;
        AbstractMonsterGirl young = AbstractMonsterGirl.youngOf(level, ModGirls.type(kinds.get(mob.getRandom().nextInt(kinds.size()))), mob.blockPosition(), father);
        if (mob instanceof net.minecraft.world.entity.npc.Villager v) {
            v.getGossips().add(father, net.minecraft.world.entity.ai.gossip.GossipType.MAJOR_POSITIVE, GOSSIP);   // she speaks well of her young's father: kinder prices
        }
        if (young != null) {
            level.sendParticles(RealmGrassBlock.HAZE, young.getX(), young.getY() + 0.5, young.getZ(), 12, 0.3, 0.3, 0.3, 0.0);
            level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY(), mob.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
            witnessed(level, mob, "realm_born");
        }
        return young;
    }

    /** For tests: her day is up now. */
    public static void dueNow(Mob mob) {
        if (carrying(mob)) mob.getPersistentData().putLong(BIRTH_AT, 0L);
    }

    /**
     * A player lying in bed at night: the nearest eager possessed villager within BED_CALL blocks sets out for their bed (her
     * brain may still take her home to her own; she has to end the night within BED_RANGE of theirs). Who, or null.
     */
    @Nullable
    public static Mob callToBed(ServerLevel level, Player player) {
        if (!player.isSleeping() || !MonsterGirlsConfig.possession()) return null;
        net.minecraft.core.BlockPos bed = player.getSleepingPos().orElse(null);
        if (bed == null) return null;
        Mob she = bedMate(level, player, BED_CALL);
        if (she == null || she.distanceToSqr(player) < 4.0) return she;
        if (player.getSleepTimer() >= SLIP_IN) {                                // still not there: she slips in through the haze
            BlockPos beside = besideBed(level, bed);
            if (beside != null) {
                if (she instanceof net.minecraft.world.entity.npc.Villager v && v.isSleeping()) v.stopSleeping();
                level.sendParticles(RealmGrassBlock.HAZE, she.getX(), she.getY() + 1.0, she.getZ(), 20, 0.3, 0.6, 0.3, 0.0);
                she.teleportTo(beside.getX() + 0.5, beside.getY(), beside.getZ() + 0.5);
                she.getNavigation().stop();
                level.sendParticles(RealmGrassBlock.HAZE, she.getX(), she.getY() + 1.0, she.getZ(), 20, 0.3, 0.6, 0.3, 0.0);
                return she;
            }
        }
        if (she instanceof net.minecraft.world.entity.npc.Villager v) {
            if (v.isSleeping()) v.stopSleeping();
            v.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET,
                    new net.minecraft.world.entity.ai.memory.WalkTarget(bed, 0.6F, 1));
        } else {
            she.getNavigation().moveTo(bed.getX() + 0.5, bed.getY(), bed.getZ() + 0.5, 0.6);
        }
        return she;
    }

    /** Ticks a player must lie in bed before one who hasn't come walking slips in through the haze. */
    public static final int SLIP_IN = 60;

    /** A spot to stand beside a bed: open at her feet and head, solid ground under. */
    @Nullable
    public static BlockPos besideBed(ServerLevel level, BlockPos bed) {
        for (net.minecraft.core.Direction d : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            BlockPos p = bed.relative(d);
            if (level.getBlockState(p).getCollisionShape(level, p).isEmpty() && level.getBlockState(p.above()).getCollisionShape(level, p.above()).isEmpty()
                    && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), net.minecraft.core.Direction.UP)) return p;
        }
        return null;
    }

    @SubscribeEvent
    public static void onSleeperTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 20 == 13 && event.getEntity().level() instanceof ServerLevel level) callToBed(level, event.getEntity());
    }

    /** Holy water drives it out. True if there was something to drive out. */
    public static boolean exorcise(ServerLevel level, Mob mob) {
        if (!possessed(mob)) return false;
        mob.getPersistentData().remove(POSSESSED_TAG);
        mob.getPersistentData().remove(NEXT_BREED);
        level.sendParticles(ParticleTypes.END_ROD, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
        sync(mob);
        if (mob instanceof net.minecraft.world.entity.npc.AbstractVillager merchant) realmTrades(merchant, false);
        return true;
    }

    /** What a possessed creature's young is born as, or null if she isn't one. */
    @Nullable
    public static GirlSpecies offspringOf(Mob mob) {
        List<GirlSpecies> kinds = POSSESSED.get(mob.getType());
        return kinds == null || kinds.isEmpty() || !possessed(mob) ? null : kinds.get(mob.getRandom().nextInt(kinds.size()));
    }

    /**
     * A possessed creature is eager: whenever she can (every BREED_EVERY), she finds a grown one of her own kind near her and
     * their young is a girl. Not while CROWD girls already live within 32 blocks. The young, or null.
     */
    @Nullable
    public static Mob breed(ServerLevel level, Mob mob) {
        if (!possessed(mob) || mob.isBaby() || !MonsterGirlsConfig.possession() || level.getGameTime() < mob.getPersistentData().getLong(NEXT_BREED)) return null;
        if (mob instanceof net.minecraft.world.entity.npc.AbstractVillager) return null;   // a possessed villager's young come of a player's bed (shareNight)
        level.sendParticles(RealmGrassBlock.HAZE, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 3, 0.3, 0.4, 0.3, 0.0);
        Mob partner = level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(BREED_RANGE),
                m -> m != mob && m.getType() == mob.getType() && !m.isBaby() && m.isAlive()).stream().findFirst().orElse(null);
        if (partner == null) return null;
        if (level.getEntitiesOfClass(AbstractMonsterGirl.class, mob.getBoundingBox().inflate(32)).size() >= CROWD) return null;
        GirlSpecies kind = offspringOf(mob);
        var hatch = HATCHES.get(mob.getType());
        if (kind == null && hatch == null) return null;
        mob.getPersistentData().putLong(NEXT_BREED, level.getGameTime() + BREED_EVERY);
        for (Mob m : List.of(mob, partner)) level.sendParticles(ParticleTypes.HEART, m.getX(), m.getY() + m.getBbHeight() + 0.3, m.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
        Mob young = hatch != null ? hatchling(level, hatch.get(), mob) : AbstractMonsterGirl.youngOf(level, ModGirls.type(kind), mob.blockPosition(), null);
        if (young != null) witnessed(level, mob, "realm_born");
        if (young != null) level.sendParticles(RealmGrassBlock.HAZE, young.getX(), young.getY() + 0.5, young.getZ(), 12, 0.3, 0.3, 0.3, 0.0);
        return young;
    }

    /** A harpy chick from a possessed hen: small, a random colour, staying where she hatched. */
    @Nullable
    private static Mob hatchling(ServerLevel level, EntityType<? extends Mob> type, Mob mother) {
        if (!(type.create(level) instanceof com.linkyoshi90.monstergirls.entity.AbstractHarpy chick)) return null;
        chick.moveTo(mother.getX(), mother.getY(), mother.getZ(), level.random.nextFloat() * 360.0F, 0.0F);
        chick.finalizeSpawn(level, level.getCurrentDifficultyAt(mother.blockPosition()), MobSpawnType.BREEDING, null);
        chick.randomizeVariant();
        chick.setBaby(true);
        chick.setPersistenceRequired();
        level.addFreshEntity(chick);
        chick.bornTo(null);
        level.playSound(null, mother.blockPosition(), SoundEvents.TURTLE_EGG_HATCH, net.minecraft.sounds.SoundSource.NEUTRAL, 0.8F, 1.3F);
        return chick;
    }

    /** She changes, here and now. The girl, or null if the change didn't take. */
    @Nullable
    public static AbstractMonsterGirl change(ServerLevel level, Mob mob, GirlSpecies kind) {
        return changeInto(level, mob, ModGirls.type(kind)) instanceof AbstractMonsterGirl girl ? girl : null;
    }

    /** She changes into a creature of this type (a girl, a harpy, a frog girl). What she became, or null. */
    @Nullable
    public static Mob changeInto(ServerLevel level, Mob mob, EntityType<? extends Mob> type) {
        @Nullable UUID owner = mob instanceof OwnableEntity pet ? pet.getOwnerUUID() : null;
        Mob girl = mob.convertTo(type, false);
        if (girl == null) return null;
        witnessed(level, girl, "realm_changed");
        girl.finalizeSpawn(level, level.getCurrentDifficultyAt(girl.blockPosition()), MobSpawnType.CONVERSION, null);
        if (girl instanceof com.linkyoshi90.monstergirls.entity.girl.Minotaur cow) cow.setMale(false);   // the realm turns cows, not bulls
        girl.setPersistenceRequired();                                  // a far player must not despawn the one creature this happened to
        girl.getPersistentData().remove(SOAKED);
        girl.getPersistentData().putBoolean(MONSTERIZED, true);
        level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getEyeY(), girl.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
        level.sendParticles(RealmGrassBlock.HAZE, girl.getX(), girl.getY() + 1.0, girl.getZ(), 30, 0.5, 0.7, 0.5, 0.0);
        girl.playSound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 0.7F, 1.4F);
        if (owner != null && girl instanceof Bonded bonded) {
            Player p = level.getPlayerByUUID(owner);
            if (p != null) {
                bonded.addAffection(p, PET_AFFECTION);
                p.displayClientMessage(Component.translatable("message.monstergirls.monsterized.pet", girl.getDisplayName()), false);
            } else {
                girl.getPersistentData().putUUID(PENDING_OWNER, owner);   // she greets them when they're back
            }
        }
        return girl;
    }

    private static final String PENDING_OWNER = "MonsterizedOwner", MONSTERIZED = "Monsterized";
    /** A changed girl's odds of joining a sleeping player, friend or not; affection only adds to it. */
    public static final float EAGER = 1.0F;

    /** Changed by the realm: she still burns with it, and is eager for any sleeper's bed (as a succubus is), friend or not. */
    public static boolean monsterized(net.minecraft.world.entity.Mob mob) {
        return mob.getPersistentData().getBoolean(MONSTERIZED);
    }

    /** A line for /monstergirls bonds, or null. */
    @Nullable
    public static Component describe(net.minecraft.world.entity.Mob mob) {
        return monsterized(mob) ? Component.translatable("bond.monstergirls.monsterized").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE) : null;
    }

    /** Now and then the realm's haze still rises from her. */
    @SubscribeEvent
    public static void onHaze(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % 61 != 0 || !(event.getEntity() instanceof AbstractMonsterGirl girl) || !(girl.level() instanceof ServerLevel level)
                || !monsterized(girl)) return;
        level.sendParticles(RealmGrassBlock.HAZE, girl.getX(), girl.getY() + girl.getBbHeight() * 0.6, girl.getZ(), 3, 0.3, 0.4, 0.3, 0.0);
    }

    /** Her odds tonight with a player (0 if she isn't one who changed). */
    public static float eagerness(AbstractMonsterGirl girl, Player player) {
        return monsterized(girl) ? Math.min(1.0F, EAGER + girl.getAffection(player) / 400.0F) : 0.0F;
    }

    /** An owner who was away when their pet changed: the first time they come near, she is their devoted friend. */
    @SubscribeEvent
    public static void onGirlTick(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % 41 != 0 || !(event.getEntity() instanceof Mob girl) || !(girl instanceof Bonded bonded) || !(girl.level() instanceof ServerLevel level)
                || !girl.getPersistentData().hasUUID(PENDING_OWNER)) return;
        Player p = level.getPlayerByUUID(girl.getPersistentData().getUUID(PENDING_OWNER));
        if (p == null || p.distanceToSqr(girl) > 32 * 32) return;
        girl.getPersistentData().remove(PENDING_OWNER);
        bonded.addAffection(p, PET_AFFECTION);
        p.displayClientMessage(Component.translatable("message.monstergirls.monsterized.pet", girl.getDisplayName()), false);
    }
}
