#!/usr/bin/env python3
"""Biome coverage, run after gen_girl_data.py and add_harpy_extras.py (it finishes the spawn tags they leave, and can be run twice):

  EXTRA     creatures added to the thin vanilla biomes, so that every biome has at least 8 species from 3 families
  FALLBACK  broad tags for the plain biomes: a world generation mod not listed here still gets the creatures of its forests and deserts
  MODDED    each biome of the supported world generation mods, listed under the vanilla biome it is most like: she spawns there too

A modded entry is optional ({"id": ..., "required": false}), so the tags load without those mods. The IDs are from the mod jars:
Biomes O' Plenty 21.1.0.14, Oh The Biomes We've Gone 2.6.3, Regions Unexplored 0.6.3, Terralith 2.6.2.
"""
import glob
import json
import os

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TAGS = os.path.join(REPO, "src", "main", "resources", "data", "harmontide", "tags", "worldgen", "biome")

END = ("end_barrens", "small_end_islands", "the_end")
EXTRA = {
    "ender_slime": END,
    "kun_dragon": ("end_barrens", "small_end_islands", "the_end", "end_highlands"),
    "norn_elf": ("end_barrens", "small_end_islands", "the_end", "end_highlands"),
    "kaguya_elf": ("end_barrens", "small_end_islands", "the_end", "end_midlands"),
    "ophanim_elf": ("end_barrens", "small_end_islands", "the_end", "end_midlands"),
    "ark_seraphim_centaur": ("end_barrens", "the_end"),
    "baku_nekomata": ("end_barrens", "the_end"),
    "moon_rabbit_jackalope": ("small_end_islands", "end_midlands"),
    "reliquary_dove_harpy": ("small_end_islands",),
    "cactus_flora": ("eroded_badlands",),
    "scorpion_bug": ("eroded_badlands",),
    "greater_devil": ("warped_forest",),
    "magma_slime": ("warped_forest",),
    "blaze_cat_nekomata": ("warped_forest",),
    "knocker_elf": ("deep_dark",),
    "beluga_mermaid": ("frozen_river",),
    "frost_slime": ("snowy_beach",),
    "orca_mermaid": ("deep_frozen_ocean",),
    "mandragora_flora": ("mushroom_fields",),
    "kraken_mollusk": ("cold_ocean",),
    "spider_crab_crustacean": ("cold_ocean",),
    "drowned_undead": ("ocean", "deep_ocean"),
}

FALLBACK = {
    "forest": "#minecraft:is_forest", "plains": "#c:is_plains", "desert": "#c:is_desert", "swamp": "#c:is_swamp", "savanna": "#minecraft:is_savanna",
    "jungle": "#minecraft:is_jungle", "taiga": "#minecraft:is_taiga", "badlands": "#minecraft:is_badlands", "beach": "#minecraft:is_beach",
    "river": "#minecraft:is_river", "snowy_plains": "#c:is_snowy_plains", "mushroom_fields": "#c:is_mushroom", "stony_peaks": "#c:is_mountain/peak",
}

B, W, R, T = "biomesoplenty:", "biomeswevegone:", "regions_unexplored:", "terralith:"
MODS = {B: """plains: field forested_field grassland pasture prairie origin_valley
meadow: lavender_field jacaranda_glade auroral_garden
flower_forest: overgrown_greens
forest: seasonal_forest woodland old_growth_woodland orchard maple_woods aspen_glade mediterranean_forest pumpkin_patch
dark_forest: ominous_woods mystic_grove dead_forest old_growth_dead_forest
taiga: coniferous_forest fir_clearing hot_springs
old_growth_spruce_taiga: redwood_forest
snowy_taiga: snowy_coniferous_forest snowy_fir_clearing snowy_maple_woods
snowy_plains: tundra wintry_origin_valley
cherry_grove: snowblossom_grove
swamp: bayou bog marsh wetland floodplain moor muskeg
jungle: rainforest rocky_rainforest tropics fungal_jungle
savanna: lush_savanna scrubland shrubland dryland wasteland_steppe
desert: cold_desert lush_desert
badlands: wasteland volcanic_plains volcano
stony_peaks: crag highland jade_cliffs rocky_shrubland
beach: dune_beach
stony_shore: gravel_beach
lush_caves: glowing_grotto
dripstone_caves: spider_nest
crimson_forest: undergrowth visceral_heap
nether_wastes: crystalline_chasm
soul_sand_valley: withered_abyss
basalt_deltas: erupting_inferno
end_midlands: end_corruption end_wilds
end_highlands: end_reef""",
        W: """plains: prairie allium_shrubland amaranth_grassland rose_fields
meadow: coconino_meadow
flower_forest: crag_gardens skyris_vale
forest: temperate_grove zelkova_forest cika_woods overgrowth_woodlands orchard ironwood_gour pumpkin_valley
dark_forest: black_forest ebony_woods enchanted_tangle forgotten_forest weeping_witch_forest
taiga: coniferous_forest maple_taiga aspen_boreal canadian_shield
old_growth_spruce_taiga: redwood_thicket
snowy_taiga: frosted_taiga frosted_coniferous_forest
snowy_plains: crimson_tundra eroded_borealis
ice_spikes: shattered_glacier
cherry_grove: sakura_grove
swamp: bayou cypress_swamplands cypress_wetlands pale_bog
mangrove_swamp: white_mangrove_marshes
jungle: tropical_rainforest fragment_jungle jacaranda_jungle
savanna: araucaria_savanna baobab_savanna firecracker_chaparral
desert: atacama_outback mojave_desert windswept_desert dead_sea
badlands: red_rock_valley rugged_badlands sierra_badlands
stony_peaks: dacite_ridges howling_peaks red_rock_peaks lush_stacks
beach: rainbow_beach
stony_shore: dacite_shore basalt_barrera""",
        R: """plains: barley_fields clover_plains grassland prairie pumpkin_fields
meadow: flower_fields poppy_fields rocky_meadow highland_fields
flower_forest: wisteria_grove
forest: deciduous_forest old_growth_forest temperate_grove maple_forest autumnal_maple_forest orchard magnolia_woodland willow_forest eucalyptus_forest alpha_grove
birch_forest: silver_birch_forest
dark_forest: ashen_woodland
taiga: boreal_taiga golden_boreal_taiga pine_taiga blackwood_taiga
old_growth_spruce_taiga: redwoods sparse_redwoods old_growth_boreal_taiga old_growth_golden_boreal_taiga
snowy_taiga: cold_boreal_taiga frozen_pine_taiga cold_deciduous_forest
snowy_plains: tundra frozen_tundra
frozen_peaks: icy_heights
swamp: bayou old_growth_bayou marsh fen ancient_delta
mushroom_fields: fungal_fen
jungle: rainforest tropics
sparse_jungle: sparse_rainforest
bamboo_jungle: bamboo_forest
savanna: baobab_savanna dry_bushland shrubland steppe outback
desert: joshua_desert saguaro_desert
stony_peaks: mountains arid_mountains chalk_cliffs mauve_hills pine_slopes spires towering_cliffs
windswept_forest: windswept_maple_forest
beach: grassy_beach
stony_shore: gravel_beach
frozen_river: cold_river
river: muddy_river tropical_river
warm_ocean: rocky_reef
lukewarm_ocean: hyacinth_deeps
lush_caves: bioshroom_caves
dripstone_caves: prismachasm redstone_caves scorching_caves
crimson_forest: infernal_holt
warped_forest: mycotoxic_undergrowth glistering_meadow
nether_wastes: inferno redstone_abyss
basalt_deltas: blackstone_basin""",
        T: """plains: shield_clearing valley_clearing alpha_islands
meadow: blooming_plateau blooming_valley lavender_valley lush_valley mirage_isles
flower_forest: lavender_forest moonlight_grove moonlight_valley
forest: forested_highlands yosemite_lowlands skylands_spring skylands_summer skylands_autumn
taiga: birch_taiga siberian_taiga shield yellowstone
snowy_taiga: snowy_maple_forest siberian_grove wintry_forest alpine_grove snowy_shield skylands_winter
snowy_plains: wintry_lowlands cold_shrubland alpha_islands_winter
ice_spikes: ice_marsh
frozen_peaks: frozen_cliffs glacial_chasm
cherry_grove: sakura_grove sakura_valley snowy_cherry_grove
swamp: orchid_swamp
jungle: amethyst_rainforest jungle_mountains rocky_jungle tropical_jungle cloud_forest
savanna: ashen_savanna fractured_savanna savanna_slopes shrubland hot_shrubland brushland steppe rocky_shrubland
desert: ancient_sands desert_canyon desert_oasis desert_spires lush_desert red_oasis sandstone_valley gravel_desert
badlands: bryce_canyon painted_mountains savanna_badlands white_mesa snowy_badlands warped_mesa caldera volcanic_crater volcanic_peaks basalt_cliffs
stony_peaks: amethyst_canyon emerald_peaks granite_cliffs stony_spires windswept_spires yosemite_cliffs white_cliffs scarlet_mountains haze_mountain
windswept_hills: rocky_mountains highlands alpine_highlands arid_highlands temperate_highlands
stony_shore: gravel_beach
river: warm_river
deep_lukewarm_ocean: deep_warm_ocean
lush_caves: cave/fungal_caves cave/underground_jungle
dripstone_caves: cave/andesite_caves cave/deep_caves cave/diorite_caves cave/frostfire_caves cave/granite_caves cave/infested_caves cave/mantle_caves cave/thermal_caves cave/tuff_caves"""}

MODDED = {}                                                            # vanilla biome -> its modded biomes
for ns, table in MODS.items():
    for line in table.splitlines():
        like, names = line.split(": ")
        MODDED.setdefault(like, []).extend(ns + n for n in names.split())


def main():
    extra = {sp: set(bs) for sp, bs in EXTRA.items()}
    missing = [sp for sp in extra if not os.path.exists(os.path.join(TAGS, f"spawns_{sp}.json"))]
    assert not missing, missing
    for path in glob.glob(os.path.join(TAGS, "spawns_*.json")):
        sp = os.path.basename(path)[len("spawns_"):-len(".json")]
        with open(path, encoding="utf-8") as f:
            values = [v for v in json.load(f)["values"] if isinstance(v, str)]   # what the generators wrote, or a run before this one
        values += [f"minecraft:{b}" for b in sorted(extra.get(sp, ())) if f"minecraft:{b}" not in values]
        plain = [v.split(":")[1] for v in values if not v.startswith("#")]
        optional = [t for b, t in FALLBACK.items() if b in plain and t not in values]
        for b in plain:
            optional += MODDED.get(b, [])
        optional = list(dict.fromkeys(optional))
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            json.dump({"values": values + [{"id": o, "required": False} for o in optional]}, f, indent=2)
            f.write("\n")
    print(f"{sum(len(v) for v in MODDED.values())} modded biomes")


if __name__ == "__main__":
    main()
