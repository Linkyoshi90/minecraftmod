#!/usr/bin/env python3
"""Item textures and models for the 2026-11-13 run: mermaid blood (vanilla's potion bottle filled with a deep sea-red,
with a pearly shimmer). More are added below as the run goes.

    uv run --no-project --with pillow python tools/gen_run_2026_11_13_items.py
"""
import json
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_paints import _vanilla_rgba  # noqa: E402
from gen_realm_assets import ASSETS, dump  # noqa: E402


def from_rows(rows):
    img = Image.new("RGBA", (len(rows[0]), len(rows)))
    for y, row in enumerate(rows):
        for x, c in enumerate(row):
            img.putpixel((x, y), tuple(c))
    return img


def tint_overlay(name, colour, shimmer=None):
    """vanilla's potion: the bottle as it is, the liquid overlay (grey) tinted."""
    bottle = from_rows(_vanilla_rgba("item/potion"))
    liquid = from_rows(_vanilla_rgba("item/potion_overlay"))
    out = bottle.copy()
    for y in range(liquid.height):
        for x in range(liquid.width):
            r, g, b, a = liquid.getpixel((x, y))
            if a == 0:
                continue
            lum = (r + g + b) / (3 * 255.0)
            c = tuple(int(colour[i] * (0.55 + 0.6 * lum)) for i in range(3))
            if shimmer and (x + y) % 5 == 0 and lum > 0.6:
                c = shimmer
            out.putpixel((x, y), tuple(min(255, v) for v in c) + (255,))
    path = os.path.join(ASSETS, "textures", "item", name + ".png")
    out.save(path)
    dump(os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"harmontide:item/{name}"}})


def dullahan_head():
    """a little round pale face under silver-white hair, two red eyes and a calm mouth, a dark collar ring at the neck."""
    rows = [
        "................",
        ".....hhhhhh.....",
        "...hhHHHHHHhh...",
        "..hHHHHHHHHHHh..",
        "..hHHhhhhhhHHh..",
        "..hHssssssssHh..",
        "..hsssssssssssh.",
        "..hsSrSsssSrSsh.",
        "..hssssssssssh..",
        "..hsssssbssssh..",
        "..hHssmmmmssHh..",
        "...hHssssssHh...",
        "....hsssssssh...",
        ".....kkkkkk.....",
        "....kKKKKKKk....",
        "................"]
    pal = {"h": (150, 150, 168, 255), "H": (226, 226, 236, 255), "s": (236, 226, 222, 255), "S": (40, 30, 40, 255),
           "r": (210, 30, 50, 255), "b": (214, 200, 196, 255), "m": (180, 120, 130, 255), "k": (40, 40, 52, 255), "K": (90, 90, 110, 255)}
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in pal:
                img.putpixel((x, y), pal[ch])
    img.save(os.path.join(ASSETS, "textures", "item", "dullahan_head.png"))
    dump(os.path.join(ASSETS, "models", "item", "dullahan_head.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": "harmontide:item/dullahan_head"}})


def recolour(src, name, dark, light, accent, handheld=False):
    """a vanilla item sprite recoloured by brightness: dark ... light, with its brightest pixels in the accent colour."""
    img = from_rows(_vanilla_rgba(src))
    out = Image.new("RGBA", img.size)
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = img.getpixel((x, y))
            if a == 0:
                continue
            lum = (r * 0.3 + g * 0.59 + b * 0.11) / 255.0
            grey = abs(r - g) < 30 and abs(g - b) < 30
            if grey and lum > 0.82:
                c = accent
            elif grey:
                c = tuple(int(dark[i] + (light[i] - dark[i]) * lum) for i in range(3))
            else:
                c = (r, g, b)
            out.putpixel((x, y), tuple(c) + (a,))
    out.save(os.path.join(ASSETS, "textures", "item", name + ".png"))
    dump(os.path.join(ASSETS, "models", "item", name + ".json"),
         {"parent": "minecraft:item/handheld" if handheld else "minecraft:item/generated", "textures": {"layer0": f"harmontide:item/{name}"}})


def main():
    tint_overlay("mermaid_blood", (150, 20, 60), shimmer=(170, 230, 240))
    dullahan_head()
    recolour("item/iron_sword", "cursed_blade", (14, 10, 18), (70, 56, 84), (200, 80, 240), handheld=True)
    recolour("item/iron_chestplate", "living_armor", (40, 40, 52), (176, 178, 192), (170, 110, 240))


if __name__ == "__main__":
    main()
