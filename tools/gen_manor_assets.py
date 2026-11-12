#!/usr/bin/env python3
"""A Lilith's manor and the Order's tithe (2026-11-08, world/RealmManorStructure, block/HolyFontBlock#tithe): worldgen data,
the manor's chest, advancements, lang, and a sentence each for the realm and order guides.

    uv run --no-project --with pillow python tools/gen_manor_assets.py
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS, DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import split_guides  # noqa: E402

GUIDE_ADD = {
    "journal.harmontide.guide.realm": " Deep in a few dark forests a Lilith keeps a manor of realm wood with her succubi, a throne, and the realm's treasures in a chest. (/harmontide locate manor)",
    "journal.harmontide.guide.order": " A gold ingot given at a holy font buys back some of the Order's goodwill.",
}


def append_guides():
    path = os.path.join(ASSETS, "lang", "en_us.json")
    with open(path, encoding="utf-8") as fh:
        text = fh.read()
    have = json.loads(text)
    for key, more in GUIDE_ADD.items():
        old = have[key]
        if old.endswith(more):
            continue
        a, b = json.dumps(key) + ": " + json.dumps(old, ensure_ascii=False), json.dumps(key) + ": " + json.dumps(old + more, ensure_ascii=False)
        assert text.count(a) == 1, key
        text = text.replace(a, b)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(text)
    split_guides.main()


def main():
    dump(os.path.join(DATA, "worldgen", "structure", "realm_manor.json"), {
        "type": "harmontide:realm_manor", "biomes": "#harmontide:has_structure/realm_manor",
        "step": "surface_structures", "spawn_overrides": {}, "terrain_adaptation": "none"})
    dump(os.path.join(DATA, "worldgen", "structure_set", "realm_manor.json"), {
        "structures": [{"structure": "harmontide:realm_manor", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 48, "separation": 18, "salt": 52370913}})
    dump(os.path.join(DATA, "tags", "worldgen", "biome", "has_structure", "realm_manor.json"), {"values": ["minecraft:dark_forest"]})
    dump(os.path.join(DATA, "loot_table", "chests", "realm_manor.json"), {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 4, "max": 7}, "entries": [
            {"type": "minecraft:item", "name": "harmontide:heartfruit", "weight": 5,
             "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 4}}]},
            {"type": "minecraft:item", "name": "harmontide:weepcap", "weight": 3,
             "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": "harmontide:realm_silver_ingot", "weight": 3,
             "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": "minecraft:amethyst_shard", "weight": 4,
             "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 2, "max": 6}}]},
            {"type": "minecraft:item", "name": "minecraft:gold_ingot", "weight": 3,
             "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 4}}]},
            {"type": "minecraft:item", "name": "harmontide:realm_censer", "weight": 1},
            {"type": "minecraft:item", "name": "harmontide:realm_silver_sword", "weight": 1},
            {"type": "minecraft:item", "name": "minecraft:book", "weight": 2,
             "functions": [{"function": "minecraft:enchant_randomly"}]}]}]})
    advs = {"realm_manor": ("realm_entered", "harmontide:realm_planks", "goal", "An Audience", "Step into a Lilith's manor"),
            "order_tithe": ("order_marked", "minecraft:gold_ingot", "task", "Indulgence", "Buy back the Order's goodwill with gold at a holy font")}
    lang = {
        "commands.harmontide.locate.manor": "The nearest Lilith's manor is at %s (%s blocks away).",
        "commands.harmontide.locate.manor.none": "No Lilith's manor within reach (only dark forest chunks generated after this update have them).",
        "message.harmontide.order.tithe": "The font accepts your tithe. The Order thinks a little better of you (notoriety %s).",
        "message.harmontide.order.tithe.none": "The Order has nothing against you.",
    }
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
    append_guides()


if __name__ == "__main__":
    main()
