#!/usr/bin/env python3
"""Assets for the nesting box (block/NestingBoxBlock.java): blockstate, the two models (empty, and with eggs in the hay), item
model, loot table, recipe and lines. It uses vanilla textures only (oak planks, hay, and white terracotta for the eggs).

    python tools/gen_nesting_box_assets.py

Can be run twice.
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, "src", "main", "resources", "assets", "harmontide")
DATA = os.path.join(REPO, "src", "main", "resources", "data", "harmontide")


def dump(path, doc):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(doc, fh, indent=2)
        fh.write("\n")


def cube(frm, to, tex, faces=("north", "south", "east", "west", "up", "down")):
    return {"from": frm, "to": to, "faces": {f: {"texture": tex} for f in faces}}


def box(eggs):
    els = [
        cube([1, 0, 1], [15, 1, 15], "#planks"),                    # the floor
        cube([1, 1, 1], [15, 7, 2], "#planks"), cube([1, 1, 14], [15, 7, 15], "#planks"),   # the walls
        cube([1, 1, 2], [2, 7, 14], "#planks"), cube([14, 1, 2], [15, 7, 14], "#planks"),
        cube([2, 1, 2], [14, 4, 14], "#hay"),                        # the hay bedding
    ]
    if eggs:
        els += [cube([5, 4, 6], [8, 7, 9], "#egg"), cube([8, 4, 7], [11, 6, 10], "#egg")]
    return {"parent": "minecraft:block/block", "textures": {"particle": "minecraft:block/oak_planks", "planks": "minecraft:block/oak_planks",
                                                           "hay": "minecraft:block/hay_block_top", "egg": "minecraft:block/white_terracotta"},
            "elements": els}


def main():
    dump(os.path.join(ASSETS, "models", "block", "nesting_box.json"), box(False))
    dump(os.path.join(ASSETS, "models", "block", "nesting_box_eggs.json"), box(True))
    dump(os.path.join(ASSETS, "blockstates", "nesting_box.json"), {"variants": {
        "eggs=false": {"model": "harmontide:block/nesting_box"}, "eggs=true": {"model": "harmontide:block/nesting_box_eggs"}}})
    dump(os.path.join(ASSETS, "models", "item", "nesting_box.json"), {"parent": "harmontide:block/nesting_box_eggs"})
    dump(os.path.join(DATA, "loot_table", "blocks", "nesting_box.json"), {"type": "minecraft:block", "pools": [{
        "rolls": 1, "entries": [{"type": "minecraft:item", "name": "harmontide:nesting_box"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    dump(os.path.join(DATA, "recipe", "nesting_box.json"), {"type": "minecraft:crafting_shaped", "category": "misc",
                                                           "pattern": ["P P", "PHP"], "key": {"P": {"tag": "minecraft:planks"}, "H": {"item": "minecraft:wheat"}},
                                                           "result": {"id": "harmontide:nesting_box", "count": 1}})
    M.add_lang({
        "block.harmontide.nesting_box": "Nesting Box",
        "journal.harmontide.guide.nesting.title": "Nesting boxes",
        "journal.harmontide.guide.nesting": "Build a nesting box (planks around a bit of wheat) and set it down where your hens are. Any egg laid within "
                                              "four blocks of it, by a chicken or a harpy, goes into the hay instead of rolling about. Take the eggs out "
                                              "with an empty hand, or let a hopper do it; a friend on patrol nearby empties it into the fridge.",
    })
    print("nesting box assets written")


if __name__ == "__main__":
    main()
