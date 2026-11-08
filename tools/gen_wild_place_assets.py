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
}


def main():
    lang = {}
    for kind, (biomes, spacing, sep, salt, step, word, name, title, desc, icon) in PLACES.items():
        dump(os.path.join(DATA, "worldgen", "structure", kind + ".json"), {
            "type": "monstergirls:" + kind, "biomes": "#monstergirls:has_structure/" + kind,
            "step": step, "spawn_overrides": {}, "terrain_adaptation": "none"})
        dump(os.path.join(DATA, "worldgen", "structure_set", kind + ".json"), {
            "structures": [{"structure": "monstergirls:" + kind, "weight": 1}],
            "placement": {"type": "minecraft:random_spread", "spacing": spacing, "separation": sep, "salt": salt}})
        dump(os.path.join(DATA, "tags", "worldgen", "biome", "has_structure", kind + ".json"), {"values": biomes})
        dump(os.path.join(DATA, "advancement", kind + ".json"), {
            "parent": "monstergirls:stargazer",
            "display": {"icon": {"id": icon}, "title": {"translate": "advancement.monstergirls." + kind},
                        "description": {"translate": "advancement.monstergirls." + kind + ".desc"},
                        "frame": "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
            "criteria": {"done": {"trigger": "minecraft:impossible"}}})
        lang["advancement.monstergirls." + kind] = title
        lang["advancement.monstergirls." + kind + ".desc"] = desc
        lang["commands.monstergirls.locate." + word] = "The nearest " + name + " is at %s (%s blocks away)."
        lang["commands.monstergirls.locate." + word + ".none"] = "No " + name + " within reach (only chunks generated after this update have them)."
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
    add_lang(lang)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.encounters"] = (
        " Further afield: an oasis in the desert where a sphinx keeps an old shrine (and her riddles), sky islands drifting high over "
        "the hills where a sylph lives, and in dark forests a jorogumo's silk den, its chest full of cocoons to tear open. "
        "(/monstergirls locate oasis, island, den)")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
