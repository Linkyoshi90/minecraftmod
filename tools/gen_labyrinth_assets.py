#!/usr/bin/env python3
"""The Minoan labyrinth (2026-11-08, world/LabyrinthStructure): worldgen data, the heart's chest, advancements, lang, and a
sentence for the encounters guide.

    uv run --no-project --with pillow python tools/gen_labyrinth_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
from gen_spire_assets import item  # noqa: E402
import gen_manor_assets  # noqa: E402


def main():
    dump(os.path.join(DATA, "worldgen", "structure", "labyrinth.json"), {
        "type": "harmontide:labyrinth", "biomes": "#harmontide:has_structure/labyrinth",
        "step": "underground_structures", "spawn_overrides": {}, "terrain_adaptation": "none"})
    dump(os.path.join(DATA, "worldgen", "structure_set", "labyrinth.json"), {
        "structures": [{"structure": "harmontide:labyrinth", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 48, "separation": 18, "salt": 57290418}})
    dump(os.path.join(DATA, "tags", "worldgen", "biome", "has_structure", "labyrinth.json"), {"values": [
        "minecraft:savanna", "minecraft:savanna_plateau", "minecraft:windswept_savanna", "minecraft:plains", "minecraft:desert"]})
    dump(os.path.join(DATA, "loot_table", "chests", "labyrinth.json"), {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 5, "max": 8}, "entries": [
            item("minecraft:gold_ingot", 6, 2, 7), item("minecraft:iron_ingot", 6, 2, 6), item("minecraft:string", 4, 4, 12),
            item("minecraft:saddle", 2), item("minecraft:golden_horse_armor", 2), item("minecraft:diamond", 2, 1, 2),
            item("minecraft:golden_apple", 2), item("minecraft:book", 3, enchant=True), item("minecraft:wheat", 4, 6, 16),
            item("minecraft:milk_bucket", 2)]}]})
    lang = {
        "commands.harmontide.locate.labyrinth": "The nearest labyrinth is at %s (%s blocks away).",
        "commands.harmontide.locate.labyrinth.none": "No labyrinth within reach (only savannas, plains and deserts generated after this update have them).",
    }
    advs = {"labyrinth": ("stargazer", "minecraft:mossy_stone_bricks", "task", "Into the Maze", "Climb down into a Minoan labyrinth"),
            "labyrinth_heart": ("labyrinth", "minecraft:string", "challenge", "Thread the Maze", "Reach the heart of a labyrinth")}
    for adv, (parent, icon, frame, title, desc) in advs.items():
        dump(os.path.join(DATA, "advancement", adv + ".json"), {
            "parent": "harmontide:" + parent,
            "display": {"icon": {"id": icon}, "title": {"translate": "advancement.harmontide." + adv},
                        "description": {"translate": "advancement.harmontide." + adv + ".desc"},
                        "frame": frame, "show_toast": True, "announce_to_chat": True, "hidden": False},
            "criteria": {"done": {"trigger": "minecraft:impossible"}}})
        lang["advancement.harmontide." + adv] = title
        lang["advancement.harmontide." + adv + ".desc"] = desc
    add_lang(lang)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.encounters"] = (
        " Under savannas, plains and deserts, a ring of mossy stone marks the way down into a Minoan labyrinth: a maze that's never "
        "the same twice, with a treasure at its heart and a minotaur who guards it. (/harmontide locate labyrinth)")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
