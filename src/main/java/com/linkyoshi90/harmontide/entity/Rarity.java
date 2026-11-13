package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * How rare a meeting is. Most kinds are common; the storied ones are rare (rareSpawnRate of the usual chance); the one-of-a-kind
 * figures of myth are legendary (legendarySpawnRate), and only one of each comes to a region (REGION blocks square) of a dimension.
 * A wild legendary stays where she is (she doesn't despawn) and sparkles now and then; one who dies unbefriended frees her region.
 * Meeting one and befriending one are advancements, and "every legend" counts each.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Rarity {
    public enum Tier {
        COMMON(ChatFormatting.GRAY), RARE(ChatFormatting.AQUA), LEGENDARY(ChatFormatting.GOLD);

        public final ChatFormatting colour;

        Tier(ChatFormatting colour) {
            this.colour = colour;
        }

        public String key() {
            return "rarity.harmontide." + this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    /** One of each legendary kind to a region this many blocks square. */
    public static final int REGION = 2048;
    /** On a wild legend who took her region. */
    private static final String LEGEND = "Legend";
    public static final String MEET = "legendary_encounter", BEFRIEND = "legendary_friend", EVERY = "every_legend";

    public static final List<String> LEGENDARY = List.of(
            "phoenix_harpy", "zhuque_harpy", "yatagarasu_harpy", "roc_harpy", "ziz_harpy", "bennu_harpy", "garuda_harpy", "alkonost_harpy",
            "bastet_nekomata", "ammit_nekomata", "nue_nekomata",
            "xiangliu_lamia", "echidna_lamia", "medusa_lamia", "wadjet_lamia",
            "sphinx_centaur", "lamassu_centaur", "qilin_centaur", "anzu_centaur", "ark_seraphim_centaur", "urcuchillay_centaur",
            "leviathan_mermaid", "cetus_mermaid",
            "scylla_mollusk", "charybdis_mollusk", "giant_kraken_mollusk",
            "kaguya_elf", "norn_elf", "ophanim_elf",
            "koschei_undead", "gashadokuro_undead",
            "black_tortoise_lizardfolk", "basilisk_lizardfolk", "cipactli_lizardfolk", "turtworld_lizardfolk",
            "long_dragon", "bahamut_dragon", "jormungandr_dragon", "ender_dragon", "quetzalcoatl_dragon", "kun_dragon", "orochi_dragon",
            "lilith_devil", "pazuzu_devil", "behemoth_devil", "taotie_devil",
            "cerberus_canid", "amarok_canid",
            "sun_wukong_rodent", "ratatoskr_rodent",
            "war_rider", "famine_rider", "pestilence_rider", "death_rider", "charro_negro_rider");

    public static final List<String> RARE = List.of(
            "tengu_harpy", "trickster_raven_harpy", "stigowl_harpy", "mirror_crane_harpy", "reliquary_dove_harpy", "ibisking_harpy", "lechuza_harpy", "emberu_harpy",
            "vodyanoy",
            "blaze_cat_nekomata", "cheshire_nekomata", "cait_sith_nekomata", "bakeneko_nekomata", "censer_lion_nekomata", "sabre_nekomata",
            "manticore_nekomata", "rakshasa_nekomata", "baku_nekomata", "smilodon_nekomata",
            "huli_jing_kitsune", "kumiho_kitsune", "obaa_kitsune", "vulphea_kitsune",
            "naga_lamia", "tsuchinoko_lamia", "hoop_snake_lamia", "serpopard_lamia", "sandworm_lamia", "titanoboa_lamia", "sea_serpent_lamia",
            "urchskin_lamia", "turquoise_serpent_lamia",
            "jorogumo_arachne", "ushi_oni_arachne",
            "kelpie_centaur", "pegasus_centaur", "unicorn_centaur", "aurorantler_centaur", "momoshika_centaur", "shedim_centaur", "halberelk_centaur",
            "bellmare_centaur", "stagel_centaur",
            "ahuizotl_mermaid", "siren_mermaid", "axolume_mermaid", "rusalka_mermaid", "mosasaurus_mermaid", "selkie_mermaid", "lorelei_mermaid",
            "anomalocaris_mermaid",
            "mimic_slime", "ender_slime", "undine_slime", "klabuster_slime",
            "al_miraj_jackalope", "wolpertinger_jackalope", "clockhare_jackalope", "moon_rabbit_jackalope", "volcano_jackalope",
            "minoan_minotaur", "aurochs_minotaur", "bronzejaw_minotaur", "brasshorn_minotaur", "skjoldram_minotaur",
            "kraken_mollusk", "ammonite_mollusk", "min_min_mollusk",
            "valkyrie_elf", "angel_elf", "gandharva_elf", "apsara_elf", "domovoi_elf", "mad_hatter_elf", "nymph_elf",
            "repeadead_undead", "dullahan_undead", "lich_undead", "banshee_undead", "llorona_undead", "yurei_undead", "draugr_undead", "jiangshi_undead",
            "mano_peluda_undead", "soul_cluster_undead", "sandman_undead", "poltergeist_undead",
            "kappa_lizardfolk", "chupacabra_lizardfolk", "archelon_lizardfolk", "argentinosaurus_lizardfolk", "iron_kobold_lizardfolk",
            "anvil_tortoise_lizardfolk",
            "wyrm_dragon", "gryphon_dragon", "kannae_dragon", "wyvern_dragon", "alebrije_dragon", "cockatrice_dragon", "amphiptere_dragon", "chimera_dragon",
            "succubus_devil", "greater_devil", "oni_devil", "genie_devil", "cyclops_devil", "ifrit_devil", "fire_giant_devil", "frost_giant_devil",
            "wrath_devil", "fury_devil", "demon_devil",
            "fairy_queen_bug", "queen_bee_bug", "queen_ant_bug", "mothman_bug", "girtablilu_bug", "temple_mantis_bug",
            "ent_flora", "mandragora_flora", "leshy_flora", "kesaran_pasaran_flora", "dryad_flora", "rafflesia_flora",
            "anubis_canid", "cu_sith_canid", "yeti_canid", "hellhound_canid", "gigantopithecus_canid", "obsidian_hound_canid",
            "kamaitachi_rodent", "sasquatch_rodent", "tanuki_rodent", "sobolnik_rodent",
            "gargoyle_construct", "talosling_construct", "automaton_construct",
            "camazotz_bat", "vetala_bat",
            "spinosaur_theropod");

    private static final Map<String, Tier> TIERS = new HashMap<>();

    static {
        for (String id : RARE) TIERS.put(id, Tier.RARE);
        for (String id : LEGENDARY) TIERS.put(id, Tier.LEGENDARY);
    }

    private Rarity() {}

    public static Tier of(EntityType<?> type) {
        var key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (!HarmontideMod.MODID.equals(key.getNamespace())) return Tier.COMMON;
        return of(key.getPath());
    }

    public static Tier of(String id) {
        return TIERS.getOrDefault(id, Tier.COMMON);
    }

    public static Tier of(Entity e) {
        return of(e.getType());
    }

    public static boolean isLegendary(Entity e) {
        return of(e) == Tier.LEGENDARY;
    }

    /** The share of the usual spawn chance her tier keeps (1 for common). */
    public static double spawnFactor(Entity e) {
        return switch (of(e)) {
            case COMMON -> 1.0;
            case RARE -> HarmontideConfig.rareSpawnRate();
            case LEGENDARY -> HarmontideConfig.legendarySpawnRate();
        };
    }

    private static boolean wild(MobSpawnType type) {
        return type == MobSpawnType.NATURAL || type == MobSpawnType.CHUNK_GENERATION;
    }

    /** A wild legendary only where her kind hasn't come yet; she takes the region and stays. */
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!wild(event.getSpawnType()) || !(event.getEntity() instanceof Bonded) || !isLegendary(event.getEntity())) return;
        if (!(event.getLevel().getLevel() instanceof ServerLevel level) || !claim(level, event.getEntity())) event.setSpawnCancelled(true);
    }

    /** Whether her region is free for her kind; if it is, she takes it and won't despawn. */
    public static boolean claim(ServerLevel level, Mob mob) {
        if (!HarmontideConfig.legendaryOncePerRegion()) {
            mob.setPersistenceRequired();
            mob.getPersistentData().putBoolean(LEGEND, true);
            return true;
        }
        Regions regions = Regions.get(level);
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath();
        long region = region(mob);
        if (regions.taken(id, region)) return false;
        regions.take(id, region);
        mob.setPersistenceRequired();
        mob.getPersistentData().putBoolean(LEGEND, true);
        return true;
    }

    public static long region(Entity e) {
        return net.minecraft.world.level.ChunkPos.asLong(Math.floorDiv(e.getBlockX(), REGION), Math.floorDiv(e.getBlockZ(), REGION));
    }

    /** A wild legend who dies without a friend leaves her region open for another of her kind. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        Entity e = event.getEntity();
        if (!(e.level() instanceof ServerLevel level) || !e.getPersistentData().getBoolean(LEGEND)) return;
        if (e instanceof Bonded b && b.hasAnyAffection()) return;
        Regions.get(level).free(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath(), region(e));
    }

    /** Now and then a wild legendary glints, so she can be found. */
    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        Entity e = event.getEntity();
        if (e.tickCount % 40 != 0 || !(e.level() instanceof ServerLevel level) || !e.getPersistentData().getBoolean(LEGEND)) return;
        if (e instanceof Bonded b && b.hasAnyAffection()) return;
        level.sendParticles(ParticleTypes.END_ROD, e.getX(), e.getY() + e.getBbHeight() * 0.6, e.getZ(), 3, e.getBbWidth() * 0.5, e.getBbHeight() * 0.3, e.getBbWidth() * 0.5, 0.01);
    }

    /** From the journal: she was seen, or befriended, for the first time. */
    public static void recorded(Player player, Entity creature, boolean befriended) {
        if (!(player instanceof ServerPlayer sp) || !isLegendary(creature)) return;
        award(sp, MEET, "done");
        if (befriended) {
            award(sp, BEFRIEND, "done");
            award(sp, EVERY, BuiltInRegistries.ENTITY_TYPE.getKey(creature.getType()).getPath());
        }
    }

    private static void award(ServerPlayer sp, String id, String criterion) {
        AdvancementHolder a = sp.server.getAdvancements().get(HarmontideMod.id(id));
        if (a != null) sp.getAdvancements().award(a, criterion);
    }

    /** Which legendary kinds have come to which regions, in one dimension. */
    public static final class Regions extends SavedData {
        private static final String NAME = "harmontide_legends";
        private final Map<String, Set<Long>> taken = new HashMap<>();

        public static Regions get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Regions::new, Regions::load, null), NAME);
        }

        public boolean taken(String id, long region) {
            return this.taken.getOrDefault(id, Set.of()).contains(region);
        }

        public void take(String id, long region) {
            if (this.taken.computeIfAbsent(id, k -> new HashSet<>()).add(region)) this.setDirty();
        }

        public void free(String id, long region) {
            Set<Long> set = this.taken.get(id);
            if (set != null && set.remove(region)) this.setDirty();
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            for (var entry : this.taken.entrySet()) {
                ListTag list = new ListTag();
                for (long r : entry.getValue()) list.add(LongTag.valueOf(r));
                tag.put(entry.getKey(), list);
            }
            return tag;
        }

        private static Regions load(CompoundTag tag, HolderLookup.Provider registries) {
            Regions out = new Regions();
            for (String id : tag.getAllKeys()) {
                Set<Long> set = new HashSet<>();
                for (Tag t : tag.getList(id, Tag.TAG_LONG)) set.add(((LongTag) t).getAsLong());
                out.taken.put(id, set);
            }
            return out;
        }
    }
}
