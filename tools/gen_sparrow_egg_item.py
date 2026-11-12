#!/usr/bin/env python3
"""Writes the sparrow egg item: its 16x16 texture (a small pale egg flecked with brown, like a house sparrow's), item
model, and the recipes that let it stand in for a hen's egg (cake, pumpkin pie).
    python tools/gen_sparrow_egg_item.py
"""
import json
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from harpy_lib import write_png  # noqa: E402

RES = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "harmontide")
DATA = os.path.join(RES, "data", "harmontide")

SHELL = (232, 226, 206)
SHADE = (196, 188, 166)
OUTLINE = (98, 84, 64)
FLECKS = [(128, 96, 64), (150, 118, 84), (96, 72, 52)]


def texture():
    rnd = random.Random(7)
    rows = [[(0, 0, 0, 0)] * 16 for _ in range(16)]
    cx, cy = 7.5, 8.6
    for y in range(16):
        for x in range(16):
            # an egg: narrower at the top
            ry = 5.6
            rx = 4.2 if y >= cy else 4.2 - (cy - y) * 0.18
            d = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2
            if d <= 1.0:
                c = SHADE if (x - cx) + (y - cy) * 0.6 > 2.2 else SHELL
                rows[y][x] = c + (255,)
            elif d <= 1.35:
                rows[y][x] = OUTLINE + (255,)
    for _ in range(14):
        x, y = rnd.randrange(4, 12), rnd.randrange(4, 14)
        if rows[y][x][:3] in (SHELL, SHADE):
            rows[y][x] = rnd.choice(FLECKS) + (255,)
    return rows


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def main():
    write_png(os.path.join(ASSETS, "textures", "item", "sparrow_egg.png"), texture())
    dump(os.path.join(ASSETS, "models", "item", "sparrow_egg.json"),
         {"parent": "minecraft:item/generated", "textures": {"layer0": "harmontide:item/sparrow_egg"}})
    dump(os.path.join(DATA, "recipe", "cake_from_sparrow_egg.json"), {
        "type": "minecraft:crafting_shaped", "category": "food",
        "pattern": ["AAA", "BEB", "CCC"],
        "key": {"A": {"item": "minecraft:milk_bucket"}, "B": {"item": "minecraft:sugar"},
                "C": {"item": "minecraft:wheat"}, "E": {"item": "harmontide:sparrow_egg"}},
        "result": {"id": "minecraft:cake", "count": 1},
    })
    dump(os.path.join(DATA, "recipe", "pumpkin_pie_from_sparrow_egg.json"), {
        "type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [{"item": "minecraft:pumpkin"}, {"item": "minecraft:sugar"}, {"item": "harmontide:sparrow_egg"}],
        "result": {"id": "minecraft:pumpkin_pie", "count": 1},
    })
    # the common tag other mods' recipes use for "any egg" is written by gen_laid_eggs.py (all the harpy eggs)
    print("sparrow egg item written")


if __name__ == "__main__":
    main()
