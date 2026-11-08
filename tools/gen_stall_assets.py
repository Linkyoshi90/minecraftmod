#!/usr/bin/env python3
"""Hamlet trades (2026-11-08, block/MarketStallBlock): the market stall's model (a spruce counter, two posts and a red-and-white
awning, all vanilla textures), recipe, loot, advancement, lang, and a sentence for the market guide.

    uv run --no-project --with pillow python tools/gen_stall_assets.py
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
    m = os.path.join(ASSETS, "models")
    dump(os.path.join(m, "block", "market_stall.json"), {
        "parent": "minecraft:block/block", "ambientocclusion": False,
        "textures": {"particle": "minecraft:block/spruce_planks", "planks": "minecraft:block/spruce_planks", "top": "minecraft:block/stripped_spruce_log_top",
                     "post": "minecraft:block/stripped_spruce_log", "red": "minecraft:block/red_wool", "white": "minecraft:block/white_wool"},
        "elements": [
            box([1, 0, 1], [15, 9, 15], "#planks", "#top"),                 # the counter
            box([1, 9, 12], [3, 15, 14], "#post", "#post"), box([13, 9, 12], [15, 15, 14], "#post", "#post"),
            box([0, 15, 4], [4, 16, 16], "#red", "#red"), box([4, 15, 4], [8, 16, 16], "#white", "#white"),
            box([8, 15, 4], [12, 16, 16], "#red", "#red"), box([12, 15, 4], [16, 16, 16], "#white", "#white")]})
    dump(os.path.join(ASSETS, "blockstates", "market_stall.json"), {"variants": {"": {"model": "monstergirls:block/market_stall"}}})
    dump(os.path.join(m, "item", "market_stall.json"), {"parent": "monstergirls:block/market_stall"})
    dump(os.path.join(DATA, "loot_table", "blocks", "market_stall.json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": "monstergirls:market_stall"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    dump(os.path.join(DATA, "recipe", "market_stall.json"), {
        "type": "minecraft:crafting_shaped", "category": "decorations",
        "pattern": ["RWR", "S S", "PCP"],
        "key": {"R": {"item": "minecraft:red_wool"}, "W": {"item": "minecraft:white_wool"}, "S": {"item": "minecraft:stick"},
                "P": {"item": "minecraft:spruce_planks"}, "C": {"item": "minecraft:chest"}},
        "result": {"id": "monstergirls:market_stall", "count": 1}})
    dump(os.path.join(DATA, "advancement", "market_stall.json"), {
        "parent": "monstergirls:village",
        "display": {"icon": {"id": "monstergirls:market_stall"}, "title": {"translate": "advancement.monstergirls.market_stall"},
                    "description": {"translate": "advancement.monstergirls.market_stall.desc"},
                    "frame": "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
    add_lang({
        "block.monstergirls.market_stall": "Market Stall",
        "advancement.monstergirls.market_stall": "Open for Business",
        "advancement.monstergirls.market_stall.desc": "Earn emeralds at your village's market stall",
    })
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.market"] = (
        " Once your hearth is a village, set a market stall within 16 blocks of it (a red and white awning over sticks, planks and a "
        "chest). Every morning the passing trade buys half of what's on it, four items to the emerald, and each resident lays out "
        "something new: a farmhand her crops, a cook her baking, a guard arrows, anyone else the kind of present she'd bring you.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
