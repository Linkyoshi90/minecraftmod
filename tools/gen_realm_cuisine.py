#!/usr/bin/env python3
"""The Demon Realm's kitchen (2026-11-08): holstaur milk, the heartfruit tart, and the splash holy water's bottle.

    uv run --no-project --with pillow python tools/gen_realm_cuisine.py [--preview previews/realm_cuisine.png]

Holstaur milk is the vanilla honey bottle with its honey turned to cream and a little pink heart on the glass; the tart is
the vanilla pumpkin pie with a heartfruit-pink filling. Also writes the item models and the tart's recipe.
"""
import colorsys
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS, DATA, dump, heart, rgb, save, HEART, HEART_DARK, SHINE  # noqa: E402
from gen_paints import _vanilla_rgba  # noqa: E402


def _img(rows):
    h, w = len(rows), len(rows[0])
    img = Image.new("RGBA", (w, h))
    img.putdata([tuple(p) for row in rows for p in row])
    return img


def _recolour(img, warm, ramp):
    """pixels whose hue falls in `warm` (a range of hue, 0..1) are redrawn on `ramp` (dark to light) by their lightness"""
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            h, l, s = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
            if warm[0] <= h <= warm[1] and s > 0.35:
                i = min(len(ramp) - 1, int(l * len(ramp)))
                px[x, y] = ramp[i]
    return img


CREAM = [rgb("#C8B49A"), rgb("#E0D2BE"), rgb("#F0E6D8"), rgb("#FAF4EA"), rgb("#FFFDF8"), rgb("#FFFFFF")]
PINK = [rgb("#7A1E3E"), rgb("#B8285E"), rgb("#D8407A"), rgb("#F25C9A"), rgb("#F890BC"), rgb("#FFD0E4")]


def holstaur_milk():
    img = _recolour(_img(_vanilla_rgba("item/honey_bottle")), (0.02, 0.17), CREAM)
    heart(img.load(), 6, 10, HEART, HEART_DARK, SHINE)
    return save(img, "item", "holstaur_milk.png")


def heartfruit_tart():
    img = _recolour(_img(_vanilla_rgba("item/pumpkin_pie")), (0.02, 0.12), PINK)
    return save(img, "item", "heartfruit_tart.png")


def splash_holy_water():
    """the holy water bottle (gen_realm_assets) with white splash marks either side of the neck"""
    img = Image.open(os.path.join(ASSETS, "textures", "item", "holy_water.png")).convert("RGBA")
    px = img.load()
    for x, y in ((4, 3), (5, 4), (12, 3), (11, 4), (3, 6), (13, 6)):
        px[x, y] = (232, 248, 255, 255)
    return save(img, "item", "splash_holy_water.png")


def data():
    for name in ("holstaur_milk", "heartfruit_tart"):
        dump(os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": "harmontide:item/" + name}})
    dump(os.path.join(DATA, "recipe", "heartfruit_tart.json"), {
        "type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [{"item": "harmontide:heartfruit"}, {"item": "harmontide:heartfruit"}, {"item": "harmontide:holstaur_milk"},
                        {"item": "minecraft:wheat"}, {"item": "minecraft:sugar"}],
        "result": {"id": "harmontide:heartfruit_tart", "count": 1}})


def main():
    tiles = [holstaur_milk(), heartfruit_tart(), splash_holy_water()]
    data()
    if "--preview" in sys.argv:
        path = sys.argv[sys.argv.index("--preview") + 1]
        sheet = Image.new("RGBA", (len(tiles) * 108, 108), (40, 40, 48, 255))
        for i, t in enumerate(tiles):
            big = t.resize((96, 96), Image.NEAREST)
            sheet.paste(big, (i * 108 + 6, 6), big)
        sheet.save(path)


if __name__ == "__main__":
    main()
