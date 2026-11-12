#!/usr/bin/env python3
"""The kitsune shrine (2026-11-08, world/KitsuneShrineStructure, block/OfferingBoxBlock): worldgen data, the offering box's
model (a slatted dark-oak box with a red rope on top, vanilla textures), recipe, loot, advancements, lang, a guide sentence.

    uv run --no-project --with pillow python tools/gen_shrine_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS, DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402


def box(frm, to, side, top):
    faces = {f: {"texture": side} for f in ("north", "south", "east", "west")}
    faces["up"] = {"texture": top}
    faces["down"] = {"texture": side}
    return {"from": frm, "to": to, "faces": faces}


def main():
    dump(os.path.join(DATA, "worldgen", "structure", "kitsune_shrine.json"), {
        "type": "harmontide:kitsune_shrine", "biomes": "#harmontide:has_structure/kitsune_shrine",
        "step": "surface_structures", "spawn_overrides": {}, "terrain_adaptation": "none"})
    dump(os.path.join(DATA, "worldgen", "structure_set", "kitsune_shrine.json"), {
        "structures": [{"structure": "harmontide:kitsune_shrine", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 36, "separation": 12, "salt": 40917263}})
    dump(os.path.join(DATA, "tags", "worldgen", "biome", "has_structure", "kitsune_shrine.json"), {"values": [
        "minecraft:cherry_grove", "minecraft:bamboo_jungle", "minecraft:birch_forest", "minecraft:old_growth_birch_forest"]})
    m = os.path.join(ASSETS, "models")
    dump(os.path.join(m, "block", "offering_box.json"), {
        "parent": "minecraft:block/block", "ambientocclusion": False,
        "textures": {"particle": "minecraft:block/dark_oak_planks", "wood": "minecraft:block/dark_oak_planks", "slats": "minecraft:block/dark_oak_trapdoor",
                     "rope": "minecraft:block/red_wool"},
        "elements": [box([1, 0, 3], [15, 8, 13], "#wood", "#slats"), box([1, 8, 7], [15, 10, 9], "#rope", "#rope")]})
    dump(os.path.join(ASSETS, "blockstates", "offering_box.json"), {"variants": {"": {"model": "harmontide:block/offering_box"}}})
    dump(os.path.join(m, "item", "offering_box.json"), {"parent": "harmontide:block/offering_box"})
    dump(os.path.join(DATA, "loot_table", "blocks", "offering_box.json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": "harmontide:offering_box"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    dump(os.path.join(DATA, "recipe", "offering_box.json"), {
        "type": "minecraft:crafting_shaped", "category": "decorations", "pattern": ["RRR", "PTP", "PPP"],
        "key": {"R": {"item": "minecraft:red_wool"}, "P": {"item": "minecraft:dark_oak_planks"}, "T": {"item": "minecraft:dark_oak_trapdoor"}},
        "result": {"id": "harmontide:offering_box", "count": 1}})
    lang = {
        "block.harmontide.offering_box": "Offering Box",
        "item.harmontide.fortune.great_blessing": "Fortune Slip: Great Blessing",
        "item.harmontide.fortune.blessing": "Fortune Slip: Blessing",
        "item.harmontide.fortune.small_blessing": "Fortune Slip: Small Blessing",
        "item.harmontide.fortune.curse": "Fortune Slip: Curse",
        "message.harmontide.fortune.great_blessing": "Great blessing! Luck walks with you today.",
        "message.harmontide.fortune.blessing": "A blessing. Things will go your way.",
        "message.harmontide.fortune.small_blessing": "A small blessing: a light step today.",
        "message.harmontide.fortune.curse": "A curse... Better luck tomorrow. (They say you tie a bad slip to a tree and leave it behind.)",
        "message.harmontide.fortune.again": "You've drawn your fortune for today.",
        "commands.harmontide.locate.shrine": "The nearest kitsune shrine is at %s (%s blocks away).",
        "commands.harmontide.locate.shrine.none": "No kitsune shrine within reach (only cherry groves, bamboo jungles and birch woods generated after this update have them).",
    }
    advs = {"kitsune_shrine": ("stargazer", "harmontide:offering_box", "task", "Fortune Slip", "Leave an offering at a kitsune shrine and draw your fortune"),
            "great_blessing": ("kitsune_shrine", "minecraft:paper", "challenge", "Great Blessing", "Draw a great blessing")}
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
        " In cherry groves, bamboo jungles and birch woods, a path under red torii leads to a kitsune shrine and its shrine maiden. "
        "Leave a little food in the offering box and draw a fortune, once a day. (/harmontide locate shrine)")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
