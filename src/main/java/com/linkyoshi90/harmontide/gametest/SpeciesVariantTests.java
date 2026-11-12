package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.AlbatrossHarpy;
import com.linkyoshi90.harmontide.entity.CrowHarpy;
import com.linkyoshi90.harmontide.entity.OwlHarpy;
import com.linkyoshi90.harmontide.entity.PhoenixHarpy;
import com.linkyoshi90.harmontide.entity.RiflebirdHarpy;
import com.linkyoshi90.harmontide.entity.SeagullHarpy;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.EnumSet;
import java.util.Set;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** Owl and gull species variants: saved with the entity, and chosen to suit the biome. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class SpeciesVariantTests {

    private static Holder<Biome> biome(GameTestHelper helper, ResourceKey<Biome> key) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(key);
    }

    /** Every variant this biome produces over many rolls. */
    private static <V extends Enum<V>> Set<V> rolls(Class<V> type, java.util.function.Function<RandomSource, V> pick) {
        Set<V> seen = EnumSet.noneOf(type);
        RandomSource random = RandomSource.create(42);
        for (int i = 0; i < 200; i++) seen.add(pick.apply(random));
        return seen;
    }

    @GameTest(template = PLATFORM, batch = "owlSpeciesSuitTheirBiomes")
    public static void owlSpeciesSuitTheirBiomes(GameTestHelper helper) {
        check(rolls(OwlHarpy.Variant.class, r -> OwlHarpy.Variant.forBiome(biome(helper, Biomes.SNOWY_PLAINS), r)).equals(EnumSet.of(OwlHarpy.Variant.SNOWY)),
                "snowy plains should only have snowy owls");
        check(rolls(OwlHarpy.Variant.class, r -> OwlHarpy.Variant.forBiome(biome(helper, Biomes.SAVANNA), r)).equals(EnumSet.of(OwlHarpy.Variant.BURROWING)),
                "savannas should have burrowing owls");
        check(rolls(OwlHarpy.Variant.class, r -> OwlHarpy.Variant.forBiome(biome(helper, Biomes.PLAINS), r)).equals(EnumSet.of(OwlHarpy.Variant.BARN)),
                "plains should have barn owls");
        check(rolls(OwlHarpy.Variant.class, r -> OwlHarpy.Variant.forBiome(biome(helper, Biomes.DARK_FOREST), r))
                .equals(EnumSet.of(OwlHarpy.Variant.TAWNY, OwlHarpy.Variant.GREAT_HORNED)), "woods should mix tawny and great horned owls");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "gullSpeciesSuitTheirCoasts")
    public static void gullSpeciesSuitTheirCoasts(GameTestHelper helper) {
        check(rolls(SeagullHarpy.Variant.class, r -> SeagullHarpy.Variant.forBiome(biome(helper, Biomes.SNOWY_BEACH), r))
                .equals(EnumSet.of(SeagullHarpy.Variant.KITTIWAKE, SeagullHarpy.Variant.GREAT_BLACK_BACKED)), "cold coasts: kittiwakes and black-backs");
        check(rolls(SeagullHarpy.Variant.class, r -> SeagullHarpy.Variant.forBiome(biome(helper, Biomes.MUSHROOM_FIELDS), r))
                .equals(EnumSet.of(SeagullHarpy.Variant.LAUGHING, SeagullHarpy.Variant.YELLOW_LEGGED)), "warm coasts: laughing and yellow-legged gulls");
        var temperate = rolls(SeagullHarpy.Variant.class, r -> SeagullHarpy.Variant.forBiome(biome(helper, Biomes.BEACH), r));
        check(temperate.contains(SeagullHarpy.Variant.HERRING) && temperate.contains(SeagullHarpy.Variant.BLACK_HEADED)
                && !temperate.contains(SeagullHarpy.Variant.KITTIWAKE), "temperate beaches: a mix of herring, black-headed and others, no kittiwakes");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "speciesVariantsSurviveSaving")
    public static void speciesVariantsSurviveSaving(GameTestHelper helper) {
        OwlHarpy owl = helper.spawn(ModEntities.OWL_HARPY.get(), new BlockPos(4, 2, 4));
        owl.setVariant(OwlHarpy.Variant.BURROWING);
        CompoundTag tag = new CompoundTag();
        owl.saveWithoutId(tag);
        OwlHarpy owlCopy = ModEntities.OWL_HARPY.get().create(helper.getLevel());
        owlCopy.load(tag);
        check(owlCopy.getVariant() == OwlHarpy.Variant.BURROWING && owlCopy.getVariantSuffix().equals("_burrowing"), "owl variant lost");

        SeagullHarpy gull = helper.spawn(ModEntities.SEAGULL_HARPY.get(), new BlockPos(10, 2, 10));
        gull.setVariant(SeagullHarpy.Variant.GREAT_BLACK_BACKED);
        CompoundTag gullTag = new CompoundTag();
        gull.saveWithoutId(gullTag);
        SeagullHarpy gullCopy = ModEntities.SEAGULL_HARPY.get().create(helper.getLevel());
        gullCopy.load(gullTag);
        check(gullCopy.getVariant() == SeagullHarpy.Variant.GREAT_BLACK_BACKED, "gull variant lost");
        check(gullCopy.getVariantSuffix().equals("_great_black_backed"), "wrong texture suffix: " + gullCopy.getVariantSuffix());
        helper.succeed();
    }

    // --- crow, albatross, riflebird, phoenix ----------------------------------------------------------------

    @GameTest(template = PLATFORM, batch = "crowSpeciesSuitTheirHomes")
    public static void crowSpeciesSuitTheirHomes(GameTestHelper helper) {
        var taiga = rolls(CrowHarpy.Variant.class, r -> CrowHarpy.Variant.forBiome(biome(helper, Biomes.TAIGA), r));
        check(taiga.contains(CrowHarpy.Variant.HOODED) && !taiga.contains(CrowHarpy.Variant.JACKDAW) && !taiga.contains(CrowHarpy.Variant.MAGPIE),
                "taiga: hooded crows (plus the odd chough/pied), no jackdaws or magpies: " + taiga);
        var plains = rolls(CrowHarpy.Variant.class, r -> CrowHarpy.Variant.forBiome(biome(helper, Biomes.PLAINS), r));
        check(plains.contains(CrowHarpy.Variant.JACKDAW) && !plains.contains(CrowHarpy.Variant.HOODED), "plains: jackdaws: " + plains);
        var woods = rolls(CrowHarpy.Variant.class, r -> CrowHarpy.Variant.forBiome(biome(helper, Biomes.DARK_FOREST), r));
        check(woods.contains(CrowHarpy.Variant.MAGPIE) && woods.contains(CrowHarpy.Variant.CARRION), "woods: magpies and carrion crows: " + woods);
        var dry = rolls(CrowHarpy.Variant.class, r -> CrowHarpy.Variant.forBiome(biome(helper, Biomes.DESERT), r));
        check(dry.contains(CrowHarpy.Variant.PIED), "desert: pied crows: " + dry);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "albatrossSpeciesSuitTheirCoasts")
    public static void albatrossSpeciesSuitTheirCoasts(GameTestHelper helper) {
        check(rolls(AlbatrossHarpy.Variant.class, r -> AlbatrossHarpy.Variant.forBiome(biome(helper, Biomes.SNOWY_BEACH), r))
                .equals(EnumSet.of(AlbatrossHarpy.Variant.GREY_HEADED)), "snowy shores: grey-headed albatrosses");
        check(rolls(AlbatrossHarpy.Variant.class, r -> AlbatrossHarpy.Variant.forBiome(biome(helper, Biomes.STONY_SHORE), r))
                .equals(EnumSet.of(AlbatrossHarpy.Variant.SOOTY)), "rocky coasts: sooty albatrosses");
        check(rolls(AlbatrossHarpy.Variant.class, r -> AlbatrossHarpy.Variant.forBiome(biome(helper, Biomes.DESERT), r))
                .equals(EnumSet.of(AlbatrossHarpy.Variant.LAYSAN)), "hot coasts: laysan albatrosses");
        var beach = rolls(AlbatrossHarpy.Variant.class, r -> AlbatrossHarpy.Variant.forBiome(biome(helper, Biomes.BEACH), r));
        check(beach.contains(AlbatrossHarpy.Variant.BLACK_BROWED) && beach.contains(AlbatrossHarpy.Variant.WANDERING), "temperate beaches: a mix: " + beach);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "riflebirdFlocksMixLooks")
    public static void riflebirdFlocksMixLooks(GameTestHelper helper) {
        var jungle = rolls(RiflebirdHarpy.Variant.class, r -> RiflebirdHarpy.Variant.forBiome(biome(helper, Biomes.JUNGLE), r));
        check(jungle.equals(EnumSet.allOf(RiflebirdHarpy.Variant.class)), "a jungle should produce every riflebird look: " + jungle);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "phoenixFlamesSuitTheNether")
    public static void phoenixFlamesSuitTheNether(GameTestHelper helper) {
        check(rolls(PhoenixHarpy.Variant.class, r -> PhoenixHarpy.Variant.forBiome(biome(helper, Biomes.SOUL_SAND_VALLEY), r))
                .equals(EnumSet.of(PhoenixHarpy.Variant.AZURE)), "soul sand valley: blue flame");
        check(rolls(PhoenixHarpy.Variant.class, r -> PhoenixHarpy.Variant.forBiome(biome(helper, Biomes.WARPED_FOREST), r))
                .equals(EnumSet.of(PhoenixHarpy.Variant.VERDANT)), "warped forest: green flame");
        check(rolls(PhoenixHarpy.Variant.class, r -> PhoenixHarpy.Variant.forBiome(biome(helper, Biomes.BASALT_DELTAS), r))
                .equals(EnumSet.of(PhoenixHarpy.Variant.EMBER)), "basalt deltas: ember phoenixes");
        check(rolls(PhoenixHarpy.Variant.class, r -> PhoenixHarpy.Variant.forBiome(biome(helper, Biomes.CRIMSON_FOREST), r))
                .equals(EnumSet.of(PhoenixHarpy.Variant.FLAME)), "crimson forest: the classic flame");
        var wastes = rolls(PhoenixHarpy.Variant.class, r -> PhoenixHarpy.Variant.forBiome(biome(helper, Biomes.NETHER_WASTES), r));
        check(wastes.equals(EnumSet.of(PhoenixHarpy.Variant.FLAME, PhoenixHarpy.Variant.SOLAR, PhoenixHarpy.Variant.EMBER)), "nether wastes: flame, solar, ember: " + wastes);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "newSpeciesVariantsSurviveSaving")
    public static void newSpeciesVariantsSurviveSaving(GameTestHelper helper) {
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(3, 2, 3));
        crow.setVariant(CrowHarpy.Variant.JACKDAW);
        CompoundTag crowTag = new CompoundTag();
        crow.saveWithoutId(crowTag);
        CrowHarpy crowCopy = ModEntities.CROW_HARPY.get().create(helper.getLevel());
        crowCopy.load(crowTag);
        check(crowCopy.getVariant() == CrowHarpy.Variant.JACKDAW && crowCopy.getVariantSuffix().equals("_jackdaw"), "crow variant lost");

        AlbatrossHarpy albatross = helper.spawn(ModEntities.ALBATROSS_HARPY.get(), new BlockPos(6, 2, 6));
        albatross.setVariant(AlbatrossHarpy.Variant.SHORT_TAILED);
        CompoundTag albatrossTag = new CompoundTag();
        albatross.saveWithoutId(albatrossTag);
        AlbatrossHarpy albatrossCopy = ModEntities.ALBATROSS_HARPY.get().create(helper.getLevel());
        albatrossCopy.load(albatrossTag);
        check(albatrossCopy.getVariant() == AlbatrossHarpy.Variant.SHORT_TAILED && albatrossCopy.getVariantSuffix().equals("_short_tailed"), "albatross variant lost");

        RiflebirdHarpy bird = helper.spawn(ModEntities.RIFLEBIRD_HARPY.get(), new BlockPos(9, 2, 9));
        bird.setVariant(RiflebirdHarpy.Variant.WILSONS);
        CompoundTag birdTag = new CompoundTag();
        bird.saveWithoutId(birdTag);
        RiflebirdHarpy birdCopy = ModEntities.RIFLEBIRD_HARPY.get().create(helper.getLevel());
        birdCopy.load(birdTag);
        check(birdCopy.getVariant() == RiflebirdHarpy.Variant.WILSONS && birdCopy.getVariantSuffix().equals("_wilsons"), "riflebird variant lost");

        PhoenixHarpy phoenix = helper.spawn(ModEntities.PHOENIX_HARPY.get(), new BlockPos(12, 2, 12));
        phoenix.setVariant(PhoenixHarpy.Variant.AZURE);
        CompoundTag phoenixTag = new CompoundTag();
        phoenix.saveWithoutId(phoenixTag);
        PhoenixHarpy phoenixCopy = ModEntities.PHOENIX_HARPY.get().create(helper.getLevel());
        phoenixCopy.load(phoenixTag);
        check(phoenixCopy.getVariant() == PhoenixHarpy.Variant.AZURE && phoenixCopy.getVariantSuffix().equals("_azure"), "phoenix variant lost");
        helper.succeed();
    }

    private static boolean textureExists(String file) {
        return HarmontideMod.class.getResource("/assets/harmontide/textures/entity/" + file + ".png") != null;
    }

    private static <V extends Enum<V>> void checkTextures(String species, Class<V> type, java.util.function.Function<V, String> serialized, boolean glow) {
        for (V v : type.getEnumConstants()) {
            String suffix = v.ordinal() == 0 ? "" : "_" + serialized.apply(v);
            check(textureExists(species + suffix), "missing texture " + species + suffix + ".png");
            if (glow) check(textureExists(species + suffix + "_glowmask"), "missing glow mask " + species + suffix + "_glowmask.png");
        }
    }

    @GameTest(template = PLATFORM, batch = "everyVariantHasATexture")
    public static void everyVariantHasATexture(GameTestHelper helper) {
        checkTextures("crow_harpy", CrowHarpy.Variant.class, CrowHarpy.Variant::getSerializedName, false);
        checkTextures("albatross_harpy", AlbatrossHarpy.Variant.class, AlbatrossHarpy.Variant::getSerializedName, false);
        checkTextures("riflebird_harpy", RiflebirdHarpy.Variant.class, RiflebirdHarpy.Variant::getSerializedName, false);
        checkTextures("phoenix_harpy", PhoenixHarpy.Variant.class, PhoenixHarpy.Variant::getSerializedName, true);
        helper.succeed();
    }
}
