#!/usr/bin/env python3
"""The Demon's spire (2026-11-08, world/DemonSpireStructure): worldgen data, the library's and the treasury's chests,
advancements, lang, and a sentence for the realm guide.

    uv run --no-project --with pillow python tools/gen_spire_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402


def item(name, weight, lo=1, hi=1, enchant=False):
    e = {"type": "minecraft:item", "name": name, "weight": weight}
    f = []
    if hi > 1:
        f.append({"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}})
    if enchant:
        f.append({"function": "minecraft:enchant_randomly"})
    if f:
        e["functions"] = f
    return e


def main():
    dump(os.path.join(DATA, "worldgen", "structure", "demon_spire.json"), {
        "type": "monstergirls:demon_spire", "biomes": "#monstergirls:has_structure/demon_spire",
        "step": "surface_structures", "spawn_overrides": {}, "terrain_adaptation": "none"})
    dump(os.path.join(DATA, "worldgen", "structure_set", "demon_spire.json"), {
        "structures": [{"structure": "monstergirls:demon_spire", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 56, "separation": 20, "salt": 61734029}})
    dump(os.path.join(DATA, "tags", "worldgen", "biome", "has_structure", "demon_spire.json"),
         {"values": ["minecraft:badlands", "minecraft:wooded_badlands", "minecraft:eroded_badlands"]})
    dump(os.path.join(DATA, "loot_table", "chests", "demon_spire_library.json"), {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 3, "max": 6}, "entries": [
            item("minecraft:book", 6, enchant=True), item("minecraft:paper", 4, 2, 8), item("minecraft:experience_bottle", 4, 2, 6),
            item("minecraft:lapis_lazuli", 4, 3, 9), item("minecraft:writable_book", 2), item("monstergirls:heartfruit", 3, 1, 3),
            item("minecraft:amethyst_shard", 3, 2, 5)]}]})
    dump(os.path.join(DATA, "loot_table", "chests", "demon_spire_treasury.json"), {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 5, "max": 8}, "entries": [
            item("minecraft:gold_ingot", 6, 2, 8), item("minecraft:diamond", 3, 1, 3), item("minecraft:emerald", 4, 2, 6),
            item("monstergirls:realm_silver_ingot", 4, 2, 5), item("monstergirls:heartfruit", 4, 2, 5), item("monstergirls:realm_censer", 2),
            item("monstergirls:realm_silver_sword", 2, enchant=True), item("minecraft:golden_apple", 2), item("minecraft:enchanted_golden_apple", 1),
            item("minecraft:netherite_scrap", 1)]}]})
    advs = {"demon_spire": ("realm_manor", "minecraft:polished_blackstone_bricks", "task", "The Spire", "Step into the Demon's spire"),
            "demon_spire_roof": ("demon_spire", "minecraft:crying_obsidian", "challenge", "Before the Throne", "Climb to the roof of the Demon's spire")}
    lang = {
        "commands.monstergirls.locate.spire": "The nearest Demon's spire is at %s (%s blocks away).",
        "commands.monstergirls.locate.spire.none": "No Demon's spire within reach (only badlands chunks generated after this update have them).",
    }
    for adv, (parent, icon, frame, title, desc) in advs.items():
        dump(os.path.join(DATA, "advancement", adv + ".json"), {
            "parent": "monstergirls:" + parent,
            "display": {"icon": {"id": icon}, "title": {"translate": "advancement.monstergirls." + adv},
                        "description": {"translate": "advancement.monstergirls." + adv + ".desc"},
                        "frame": frame, "show_toast": True, "announce_to_chat": True, "hidden": False},
            "criteria": {"done": {"trigger": "minecraft:impossible"}}})
        lang["advancement.monstergirls." + adv] = title
        lang["advancement.monstergirls." + adv + ".desc"] = desc
    add_lang(lang)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.realm"] = (
        " In the badlands stands the Demon's spire: a blackstone tower of four storeys (a hall with succubi on guard, a library, "
        "a bedchamber, a treasury) and on its roof, under the open sky, the Demon on her throne. (/monstergirls locate spire)")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
