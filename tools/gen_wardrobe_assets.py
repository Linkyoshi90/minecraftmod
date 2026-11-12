#!/usr/bin/env python3
"""Assets for the wardrobe (2026-11-07, entity/Wardrobe): the outfit texture grid for client/OutfitLayer, the four clothing item
icons, their item models and recipes.

    uv run --no-project --with pillow python tools/gen_wardrobe_assets.py [--preview previews/wardrobe_assets.png]

textures/entity/outfits.png is 64x64, a 4x4 grid of 16px cells: one row per outfit (raincoat, winter coat, yukata, maid apron, in
Wardrobe.Outfit order), columns main cloth, trim, band, head piece.
"""
import json
import os
import random
import sys

from PIL import Image

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(REPO, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "harmontide")
DATA = os.path.join(RES, "data", "harmontide")


def rgb(h, a=255):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4)) + (a,)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (c[3],)


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def cloth(px, ox, oy, base, rnd, pattern=None):
    for y in range(16):
        for x in range(16):
            c = shade(base, 0.92 + rnd.random() * 0.12)
            if pattern:
                c = pattern(x, y, c)
            px[ox + x, oy + y] = c


def grid():
    rnd = random.Random(11)
    img = Image.new("RGBA", (64, 64))
    px = img.load()
    yellow, navy = rgb("#F2C230"), rgb("#2A3A6A")
    cloth(px, 0, 0, yellow, rnd, lambda x, y, c: shade(c, 0.85) if x % 8 == 0 else c)                       # raincoat: seams
    cloth(px, 16, 0, rgb("#3A3A3A"), rnd, lambda x, y, c: rgb("#D8D8D0") if (x % 5, y % 5) == (2, 2) else c)  # buttons
    cloth(px, 32, 0, rgb("#C89A20"), rnd)
    cloth(px, 48, 0, yellow, rnd, lambda x, y, c: shade(c, 0.8) if y > 13 else c)                            # hood with a rim
    red, fur = rgb("#8A2A2A"), rgb("#F2EEE4")
    cloth(px, 0, 16, red, rnd, lambda x, y, c: shade(c, 0.8) if (x + y) % 6 == 0 else c)                      # winter coat: weave
    cloth(px, 16, 16, fur, rnd, lambda x, y, c: shade(c, 0.85 + 0.1 * ((x * 7 + y * 3) % 3)))                 # fur trim
    cloth(px, 32, 16, rgb("#4A2A1A"), rnd)
    cloth(px, 48, 16, rgb("#C8D8E8"), rnd, lambda x, y, c: rgb("#FFFFFF") if y < 3 else (rgb("#3A5A8A") if y % 4 == 1 else c))  # knit hat
    ink = rgb("#2A3A7A")
    def flowers(x, y, c):
        return rgb("#F2F2F8") if (x % 6, y % 6) in ((1, 2), (2, 1), (2, 3), (3, 2)) else rgb("#E8A8C8") if (x % 6, y % 6) == (2, 2) else c
    cloth(px, 0, 32, ink, rnd, flowers)                                                                      # yukata: flowers on indigo
    cloth(px, 16, 32, rgb("#D83A4A"), rnd)                                                                   # the bow
    cloth(px, 32, 32, rgb("#D83A4A"), rnd, lambda x, y, c: rgb("#F2C14E") if y in (4, 11) else c)            # obi with gold lines
    cloth(px, 48, 32, rgb("#F2A8C8"), rnd, lambda x, y, c: rgb("#FFF0A0") if 6 <= x <= 9 and 6 <= y <= 9 else c)  # hair flower
    white = rgb("#F8F6F0")
    cloth(px, 0, 48, white, rnd, lambda x, y, c: rgb("#D8D0C8") if y in (0, 15) or (y > 12 and x % 3 == 0) else c)  # apron with frill
    cloth(px, 16, 48, rgb("#1E1E24"), rnd)
    cloth(px, 32, 48, white, rnd)
    cloth(px, 48, 48, white, rnd, lambda x, y, c: rgb("#1E1E24") if y < 2 else (shade(c, 0.85) if x % 2 else c))  # frilled headband
    path = os.path.join(ASSETS, "textures", "entity", "outfits.png")
    img.save(path)
    return img


SHAPES = {
    "raincoat": (["....hhhhhh......", "...hhhhhhhh.....", "...hh....hh.....", "..ccccccccccc...", ".cccccbcccccc...", ".ccc.cbcc.ccc...",
                  ".ccc.cccc.ccc...", ".cc..cbcc..cc...", "....ccccc.......", "....cbccc.......", "....ccccc.......", "....ccccc.......",
                  "....cc.cc.......", "................", "................", "................"],
                 dict(h="#E8B828", c="#F2C230", b="#3A3A3A")),
    "winter_coat": (["....ffffff......", "...ffffffff.....", "..ccccccccccc...", ".cccccbcccccc...", ".ccc.cccc.ccc...", ".ccc.cbcc.ccc...",
                     ".cc..cccc..cc...", ".ff..cbcc..ff...", "....ccccc.......", "....ccccc.......", "...ccccccc......", "...fffffff......",
                     "................", "................", "................", "................"],
                    dict(f="#F2EEE4", c="#8A2A2A", b="#F2C14E")),
    "yukata": (["................", "....kkkkkk......", "...kkwkkkkk.....", "..kkkkkkwkkk....", ".kkk.kwkk.kkk...", ".kk..kkkk..kk...",
                "....rrrrrr......", "....rgrrgr......", "....kkkwkk......", "....kkkkkk......", "....kwkkkk......", "....kkkkwk......",
                "....kkkkkk......", "................", "................", "................"],
               dict(k="#2A3A7A", w="#F2F2F8", r="#D83A4A", g="#F2C14E")),
    "maid_apron": (["....d....d......", "....w....w......", "....wwwwww......", "...wwwwwwww.....", "...wwwwwwww.....", "..ddddddddddd...",
                    "...wwwwwwww.....", "...wwwwwwww.....", "...wwwwwwww.....", "...wwwwwwww.....", "...wwwwwwww.....", "...wfwfwfwf.....",
                    "................", "................", "................", "................"],
                   dict(w="#F8F6F0", d="#1E1E24", f="#C8C0B8")),
}


def icons():
    out = []
    for name, (rows, pal) in SHAPES.items():
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        px = img.load()
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch in pal:
                    px[x + 1, y + 1] = rgb(pal[ch])
        path = os.path.join(ASSETS, "textures", "item", f"{name}.png")
        img.save(path)
        dump(os.path.join(ASSETS, "models", "item", f"{name}.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"harmontide:item/{name}"}})
        out.append(img)
    return out


RECIPES = {
    "raincoat": ["minecraft:leather", "minecraft:leather", "minecraft:yellow_dye", "minecraft:slime_ball"],
    "winter_coat": ["minecraft:leather", "minecraft:leather", "minecraft:white_wool", "minecraft:white_wool", "minecraft:rabbit_hide"],
    "yukata": ["minecraft:blue_wool", "minecraft:blue_wool", "minecraft:red_wool", "minecraft:string"],
    "maid_apron": ["minecraft:white_wool", "minecraft:white_wool", "minecraft:black_wool", "minecraft:string"],
}


def recipes():
    for name, items in RECIPES.items():
        dump(os.path.join(DATA, "recipe", f"{name}.json"), {"type": "minecraft:crafting_shapeless", "category": "misc",
                                                            "ingredients": [{"item": i} for i in items], "result": {"id": f"harmontide:{name}", "count": 1}})


def main():
    g = grid()
    ic = icons()
    recipes()
    if "--preview" in sys.argv:
        sheet = Image.new("RGBA", (256 + 20 + 4 * 100, 276), (40, 40, 48, 255))
        big = g.resize((256, 256), Image.NEAREST)
        sheet.paste(big, (10, 10), big)
        for i, im in enumerate(ic):
            b = im.resize((96, 96), Image.NEAREST)
            sheet.paste(b, (276 + i * 100, 10), b)
        path = sys.argv[sys.argv.index("--preview") + 1]
        os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
        sheet.save(path)


if __name__ == "__main__":
    main()
