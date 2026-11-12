#!/usr/bin/env python3
"""The wayside inn (2026-11-08, world/WaysideInnStructure, entity/girl/Inn): worldgen data, the larder, an advancement, lang,
and a sentence for the market guide.

    uv run --no-project --with pillow python tools/gen_inn_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
from gen_spire_assets import item  # noqa: E402
import gen_manor_assets  # noqa: E402


def main():
    dump(os.path.join(DATA, "worldgen", "structure", "wayside_inn.json"), {
        "type": "harmontide:wayside_inn", "biomes": "#harmontide:has_structure/wayside_inn",
        "step": "surface_structures", "spawn_overrides": {}, "terrain_adaptation": "none"})
    dump(os.path.join(DATA, "worldgen", "structure_set", "wayside_inn.json"), {
        "structures": [{"structure": "harmontide:wayside_inn", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 44, "separation": 16, "salt": 19384756}})
    dump(os.path.join(DATA, "tags", "worldgen", "biome", "has_structure", "wayside_inn.json"), {"values": [
        "minecraft:plains", "minecraft:sunflower_plains", "minecraft:meadow", "minecraft:savanna", "minecraft:flower_forest", "minecraft:forest"]})
    dump(os.path.join(DATA, "loot_table", "chests", "wayside_inn.json"), {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 4, "max": 7}, "entries": [
            item("minecraft:bread", 6, 2, 5), item("minecraft:apple", 5, 2, 5), item("minecraft:cooked_beef", 3, 1, 3), item("minecraft:baked_potato", 4, 2, 4),
            item("minecraft:milk_bucket", 2), item("minecraft:honey_bottle", 2, 1, 2), item("minecraft:cake", 1), item("minecraft:emerald", 3, 1, 4),
            item("minecraft:map", 2)]}]})
    dump(os.path.join(DATA, "advancement", "wayside_inn.json"), {
        "parent": "harmontide:market_day",
        "display": {"icon": {"id": "minecraft:rabbit_stew"}, "title": {"translate": "advancement.harmontide.wayside_inn"},
                    "description": {"translate": "advancement.harmontide.wayside_inn.desc"},
                    "frame": "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
    add_lang({
        "advancement.harmontide.wayside_inn": "A Hot Meal",
        "advancement.harmontide.wayside_inn.desc": "Buy a meal from the innkeeper of a wayside inn",
        "message.harmontide.inn.meal": "%s sets down a steaming bowl and a loaf. On the house? No, that'll be an emerald.",
        "message.harmontide.inn.rumour": "%s leans over the counter: a traveller left this behind. %s.",
        "commands.harmontide.locate.inn": "The nearest wayside inn is at %s (%s blocks away).",
        "commands.harmontide.locate.inn.none": "No wayside inn within reach (only plains, meadows, savannas and forests generated after this update have them).",
    })
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.market"] = (
        " Out on the plains and meadows stand wayside inns, kept by a holstaur. Every morning a traveller or two from far away "
        "stops over for the day: make a friend of one and she stays. An emerald to the innkeeper buys a hot meal, and sometimes a "
        "map a traveller left behind. (/harmontide locate inn)")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
