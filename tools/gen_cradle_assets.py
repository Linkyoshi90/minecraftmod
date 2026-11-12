#!/usr/bin/env python3
"""The cradle (2026-11-09, block/CradleBlock, entity/girl/Cradle): blockstate (four facings), model, item model, loot,
recipe, lang and a sentence for the nursing guide. Vanilla textures only (spruce planks, stripped spruce, white wool).

    uv run --no-project --with pillow python tools/gen_cradle_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_nesting_box_assets import ASSETS, DATA, dump, cube  # noqa: E402
import gen_milk_assets as M  # noqa: E402
import gen_manor_assets  # noqa: E402


def model():
    els = [
        cube([1, 0, 2], [15, 1, 4], "#rocker"), cube([1, 0, 12], [15, 1, 14], "#rocker"),       # the two rockers
        cube([3, 1, 3], [13, 2, 13], "#planks"),                                                  # the floor of the basket
        cube([2, 1, 3], [3, 8, 13], "#planks"), cube([13, 1, 3], [14, 8, 13], "#planks"),       # ends (the head end taller)
        cube([2, 8, 3], [3, 10, 13], "#planks"),
        cube([3, 2, 3], [13, 6, 4], "#planks"), cube([3, 2, 12], [13, 6, 13], "#planks"),       # the sides
        cube([3, 2, 4], [13, 4, 12], "#wool"),                                                    # the little mattress
        cube([3, 4, 4], [6, 5, 12], "#wool"),                                                     # and the pillow
    ]
    return {"parent": "minecraft:block/block", "textures": {"particle": "minecraft:block/spruce_planks", "planks": "minecraft:block/spruce_planks",
                                                           "rocker": "minecraft:block/stripped_spruce_log", "wool": "minecraft:block/white_wool"},
            "elements": els}


def main():
    dump(os.path.join(ASSETS, "models", "block", "cradle.json"), model())
    rot = {"north": 0, "east": 90, "south": 180, "west": 270}
    dump(os.path.join(ASSETS, "blockstates", "cradle.json"), {"variants": {
        f"facing={f}": ({"model": "harmontide:block/cradle", "y": r} if r else {"model": "harmontide:block/cradle"}) for f, r in rot.items()}})
    dump(os.path.join(ASSETS, "models", "item", "cradle.json"), {"parent": "harmontide:block/cradle"})
    dump(os.path.join(DATA, "loot_table", "blocks", "cradle.json"), {"type": "minecraft:block", "pools": [{
        "rolls": 1, "entries": [{"type": "minecraft:item", "name": "harmontide:cradle"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    dump(os.path.join(DATA, "recipe", "cradle.json"), {"type": "minecraft:crafting_shaped", "category": "misc",
                                                      "pattern": ["P P", "PWP", "S S"],
                                                      "key": {"P": {"tag": "minecraft:planks"}, "W": {"tag": "minecraft:wool"}, "S": {"item": "minecraft:stick"}},
                                                      "result": {"id": "harmontide:cradle", "count": 1}})
    M.add_lang({
        "block.harmontide.cradle": "Cradle",
        "message.harmontide.cradle.rocked": "You rock the cradle and hum a lullaby for %s little ones",
        "message.harmontide.cradle.empty": "You rock the empty cradle. It creaks softly",
    })
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.nursing"] = (
        " A cradle (planks around wool, on two sticks) helps too: at night the little ones nap beside it and grow twice as fast, and rocking it "
        "hums them a lullaby.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
