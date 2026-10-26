#!/usr/bin/env python3
"""The moon dial (item/MoonDialItem.java): one 16x16 icon per moon phase, the item model that picks between them like
vanilla's clock (predicate monstergirls:phase = phase / 8), and its recipe.

    python tools/gen_moon_dial.py

Phases are vanilla's order: 0 full, 1 waning gibbous, 2 last quarter, 3 waning crescent, 4 new, 5 waxing crescent,
6 first quarter, 7 waxing gibbous (lit side as seen from the northern hemisphere).
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
from gen_harmontide_items import ASSETS, DATA, dump, rgb  # noqa: E402

RIM, RIM_DARK, FACE, STAR, LIT, DARK = (rgb(c) for c in ("#D8B24A", "#8A6A1E", "#1B2340", "#9AA6D8", "#F2EED8", "#4C5680"))


def icon(phase):
    img = [[(0, 0, 0, 0)] * 16 for _ in range(16)]
    k = math.cos(phase / 8 * 2 * math.pi)
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            r = math.hypot(dx, dy)
            if r > 7.6:
                continue
            if r > 6.2:                                   # the gold rim, lit from the top left
                img[y][x] = (RIM if dx + dy < 2 else RIM_DARK) + (255,)
                continue
            img[y][x] = FACE + (255,)
            mx, my = dx / 3.8, dy / 3.8                   # the moon, a disc in the middle of the face
            if mx * mx + my * my <= 1:
                w = math.sqrt(1 - my * my)
                lit = mx < k * w if phase <= 4 else mx > -k * w
                img[y][x] = (LIT if lit else DARK) + (255,)
    for x, y in ((4, 4), (11, 3), (3, 10), (12, 11)):     # a few stars around it
        img[y][x] = STAR + (255,)
    return img


def main():
    for p in range(8):
        H.write_png(os.path.join(ASSETS, "textures", "item", f"moon_dial_{p:02d}.png"), icon(p))
        dump(os.path.join(ASSETS, "models", "item", f"moon_dial_{p:02d}.json"),
             {"parent": "minecraft:item/generated", "textures": {"layer0": f"monstergirls:item/moon_dial_{p:02d}"}})
    dump(os.path.join(ASSETS, "models", "item", "moon_dial.json"), {
        "parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:item/moon_dial_00"},
        "overrides": [{"predicate": {"monstergirls:phase": p / 8}, "model": f"monstergirls:item/moon_dial_{p:02d}"} for p in range(8)]})
    dump(os.path.join(DATA, "recipe", "moon_dial.json"), {
        "type": "minecraft:crafting_shaped", "category": "misc", "pattern": [" G ", "GAG", " G "],
        "key": {"G": {"item": "minecraft:gold_ingot"}, "A": {"item": "minecraft:amethyst_shard"}},
        "result": {"id": "monstergirls:moon_dial", "count": 1}})


if __name__ == "__main__":
    main()
