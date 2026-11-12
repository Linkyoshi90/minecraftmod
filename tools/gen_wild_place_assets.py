#!/usr/bin/env python3
"""The oasis, the sky island and the silk den (2026-11-08, world/OneChunkStructure and its three): worldgen data, chests,
advancements, lang, and a sentence for the encounters guide.

    uv run --no-project --with pillow python tools/gen_wild_place_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
from gen_spire_assets import item  # noqa: E402
import gen_manor_assets  # noqa: E402

PLACES = {
    # kind: (biomes, spacing, separation, salt, step, locate word, name, advancement title, advancement description, icon)
    "oasis": (["minecraft:desert"], 40, 14, 63018472, "surface_structures", "oasis", "oasis",
              "A Drink in the Desert", "Find an oasis and its sphinx", "minecraft:sugar_cane"),
    "sky_island": (["minecraft:windswept_hills", "minecraft:windswept_forest", "minecraft:meadow", "minecraft:cherry_grove", "minecraft:sunflower_plains"],
                   48, 18, 84173920, "surface_structures", "island", "sky island", "Head in the Clouds", "Set foot on a sky island", "minecraft:white_wool"),
    "silk_den": (["minecraft:dark_forest"], 36, 12, 29384715, "surface_structures", "den", "silk den",
                 "Into the Web", "Find a jorogumo's silk den", "minecraft:cobweb"),
    "dragon_lair": (["minecraft:windswept_hills", "minecraft:windswept_gravelly_hills", "minecraft:stony_peaks", "minecraft:jagged_peaks", "minecraft:grove"],
                    48, 18, 71820394, "surface_structures", "lair", "dragon's lair", "Here Be Dragons", "Find a dragon's lair and its hoard", "minecraft:gold_block"),
    "centaur_camp": (["minecraft:plains", "minecraft:sunflower_plains", "minecraft:savanna", "minecraft:savanna_plateau"], 40, 14, 30918274,
                     "surface_structures", "camp", "centaur camp", "Round the Campfire", "Find a centaur band's camp", "minecraft:campfire"),
    "graveyard": (["minecraft:forest", "minecraft:taiga", "minecraft:plains", "minecraft:old_growth_spruce_taiga", "minecraft:dark_forest"], 40, 14, 55102938,
                  "surface_structures", "graveyard", "old graveyard", "Here Lies...", "Find an old graveyard and its ghost", "minecraft:candle"),
}


def main():
    lang = {}
    for kind, (biomes, spacing, sep, salt, step, word, name, title, desc, icon) in PLACES.items():
        dump(os.path.join(DATA, "worldgen", "structure", kind + ".json"), {
            "type": "harmontide:" + kind, "biomes": "#harmontide:has_structure/" + kind,
            "step": step, "spawn_overrides": {}, "terrain_adaptation": "none"})
        dump(os.path.join(DATA, "worldgen", "structure_set", kind + ".json"), {
            "structures": [{"structure": "harmontide:" + kind, "weight": 1}],
            "placement": {"type": "minecraft:random_spread", "spacing": spacing, "separation": sep, "salt": salt}})
        dump(os.path.join(DATA, "tags", "worldgen", "biome", "has_structure", kind + ".json"), {"values": biomes})
        dump(os.path.join(DATA, "advancement", kind + ".json"), {
            "parent": "harmontide:stargazer",
            "display": {"icon": {"id": icon}, "title": {"translate": "advancement.harmontide." + kind},
                        "description": {"translate": "advancement.harmontide." + kind + ".desc"},
                        "frame": "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
            "criteria": {"done": {"trigger": "minecraft:impossible"}}})
        lang["advancement.harmontide." + kind] = title
        lang["advancement.harmontide." + kind + ".desc"] = desc
        lang["commands.harmontide.locate." + word] = "The nearest " + name + " is at %s (%s blocks away)."
        lang["commands.harmontide.locate." + word + ".none"] = "No " + name + " within reach (only chunks generated after this update have them)."
    dump(os.path.join(DATA, "loot_table", "chests", "oasis.json"), {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 4, "max": 7}, "entries": [
            item("minecraft:gold_ingot", 6, 2, 6), item("minecraft:lapis_lazuli", 5, 3, 9), item("minecraft:emerald", 3, 1, 3),
            item("minecraft:date" if False else "minecraft:melon_slice", 3, 3, 8), item("minecraft:sugar_cane", 3, 2, 6), item("minecraft:diamond", 1),
            item("minecraft:golden_apple", 1), item("minecraft:book", 2, enchant=True), item("minecraft:clock", 1), item("minecraft:compass", 1)]}]})
    dump(os.path.join(DATA, "loot_table", "chests", "sky_island.json"), {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 4, "max": 7}, "entries": [
            item("minecraft:feather", 6, 3, 9), item("minecraft:phantom_membrane", 3, 1, 3), item("minecraft:elytra", 1), item("minecraft:firework_rocket", 3, 2, 6),
            item("minecraft:amethyst_shard", 4, 2, 5), item("minecraft:emerald", 3, 1, 3), item("minecraft:apple", 4, 2, 4),
            item("minecraft:book", 2, enchant=True)]}]})
    dump(os.path.join(DATA, "loot_table", "chests", "dragon_lair.json"), {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 5, "max": 9}, "entries": [
            item("minecraft:gold_ingot", 8, 3, 9), item("minecraft:gold_nugget", 6, 6, 18), item("minecraft:raw_gold", 5, 2, 6), item("minecraft:emerald", 4, 2, 5),
            item("minecraft:diamond", 3, 1, 3), item("minecraft:amethyst_shard", 4, 3, 8), item("minecraft:golden_apple", 2), item("minecraft:enchanted_golden_apple", 1),
            item("minecraft:golden_sword", 2, enchant=True), item("minecraft:golden_helmet", 1, enchant=True)]}]})
    dump(os.path.join(DATA, "loot_table", "chests", "centaur_camp.json"), {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 4, "max": 7}, "entries": [
            item("minecraft:bread", 6, 2, 5), item("minecraft:carrot", 5, 3, 8), item("minecraft:apple", 5, 2, 5), item("minecraft:arrow", 5, 6, 16),
            item("minecraft:bow", 2, enchant=True), item("minecraft:leather", 4, 2, 5), item("minecraft:saddle", 2), item("minecraft:lead", 3, 1, 2),
            item("minecraft:golden_carrot", 2, 1, 3)]}]})
    dump(os.path.join(DATA, "loot_table", "chests", "graveyard.json"), {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 4, "max": 7}, "entries": [
            item("minecraft:bone", 6, 2, 6), item("minecraft:candle", 5, 1, 4), item("minecraft:gold_nugget", 5, 3, 9), item("minecraft:iron_ingot", 3, 1, 3),
            item("minecraft:emerald", 3, 1, 2), item("minecraft:book", 3, enchant=True), item("minecraft:soul_lantern", 2), item("minecraft:music_disc_13", 1),
            item("minecraft:golden_apple", 1)]}]})
    add_lang(lang)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.encounters"] = (
        " Further afield: an oasis in the desert where a sphinx keeps an old shrine (and her riddles), sky islands drifting high over "
        "the hills where a sylph lives, and in dark forests a jorogumo's silk den, its chest full of cocoons to tear open. "
        "(/harmontide locate oasis, island, den)")
    gen_manor_assets.append_guides()
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.encounters"] = (
        " Up in the mountains a dragon sleeps in her lair on a glittering hoard; out on the plains a centaur band keeps camp round a "
        "fire; and in an old graveyard a yurei drifts among the headstones. (/harmontide locate lair, camp, graveyard)")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
