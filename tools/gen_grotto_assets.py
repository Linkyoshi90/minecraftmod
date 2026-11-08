#!/usr/bin/env python3
"""The merfolk grotto (2026-11-08, world/MerfolkGrottoStructure): worldgen data, the sunken chest, advancements, lang, and a
sentence for the encounters guide.

    uv run --no-project --with pillow python tools/gen_grotto_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
from gen_spire_assets import item  # noqa: E402
import gen_manor_assets  # noqa: E402


def main():
    dump(os.path.join(DATA, "worldgen", "structure", "merfolk_grotto.json"), {
        "type": "monstergirls:merfolk_grotto", "biomes": "#monstergirls:has_structure/merfolk_grotto",
        "step": "surface_structures", "spawn_overrides": {}, "terrain_adaptation": "none"})
    dump(os.path.join(DATA, "worldgen", "structure_set", "merfolk_grotto.json"), {
        "structures": [{"structure": "monstergirls:merfolk_grotto", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 40, "separation": 14, "salt": 73192548}})
    dump(os.path.join(DATA, "tags", "worldgen", "biome", "has_structure", "merfolk_grotto.json"),
         {"values": ["minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean"]})
    dump(os.path.join(DATA, "loot_table", "chests", "merfolk_grotto.json"), {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 4, "max": 7}, "entries": [
            item("minecraft:prismarine_crystals", 5, 2, 6), item("minecraft:prismarine_shard", 5, 2, 6), item("minecraft:nautilus_shell", 3, 1, 2),
            item("minecraft:gold_ingot", 4, 1, 4), item("minecraft:emerald", 3, 1, 3), item("minecraft:tropical_fish", 4, 1, 3),
            item("minecraft:sea_pickle", 3, 2, 4), item("minecraft:turtle_scute", 1), item("minecraft:trident", 1),
            item("minecraft:book", 2, enchant=True)]},
        {"rolls": 1, "entries": [{"type": "minecraft:empty", "weight": 4}, item("minecraft:heart_of_the_sea", 1)]}]})
    lang = {
        "entity.monstergirls.grotto_queen": "%s Queen",
        "message.monstergirls.grotto.welcome": "%s welcomes you to her court: %s more of the reef's girls count you a friend.",
        "commands.monstergirls.locate.grotto": "The nearest merfolk grotto is at %s (%s blocks away).",
        "commands.monstergirls.locate.grotto.none": "No merfolk grotto within reach (only warm seas generated after this update have them).",
    }
    advs = {"merfolk_grotto_found": ("stargazer", "minecraft:prismarine", "task", "Under the Sea", "Swim into a merfolk grotto"),
            "merfolk_grotto": ("merfolk_grotto_found", "minecraft:heart_of_the_sea", "goal", "The Queen's Court", "Befriend a grotto's queen, and her court with her")}
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
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.encounters"] = (
        " On the floor of warm seas lie merfolk grottoes: domes of prismarine and coral round a sunken chest, where a mermaid queen "
        "holds court with three of the reef's girls. Befriend the queen and her court takes you in. (/monstergirls locate grotto)")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
