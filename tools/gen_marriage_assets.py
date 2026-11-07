#!/usr/bin/env python3
"""Assets for marriage (2026-11-07, entity/Marriage): the wedding ring icon and the sheer veil texture (client/VeilLayer).

    uv run --no-project --with pillow python tools/gen_marriage_assets.py [--preview previews/marriage_assets.png]

The veil texture is used in quarters by MilkingHarnessLayer.Boxes: cell 0 (top left) is the drape, cell 1 (top right) the lace band.
"""
import math
import os
import sys

from PIL import Image

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(REPO, "src", "main", "resources", "assets", "monstergirls", "textures")


def ring():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    gold, gold_light, gold_dark = (242, 193, 78, 255), (255, 232, 150, 255), (168, 118, 30, 255)
    cx, cy = 7.5, 9.5
    for y in range(16):
        for x in range(16):
            dx, dy = (x + 0.5 - cx - 0.5) / 5.2, (y + 0.5 - cy - 0.5) / 4.2
            r = math.hypot(dx, dy)
            if 0.66 <= r <= 1.0:
                ang = math.atan2(dy, dx)
                px[x, y] = gold_light if -2.6 < ang < -1.4 else gold_dark if 0.3 < ang < 2.6 else gold
    for x, y in ((7, 2), (8, 2), (6, 3), (7, 3), (8, 3), (9, 3), (7, 4), (8, 4)):
        px[x, y] = (200, 240, 255, 255)
    for x, y in ((7, 2), (6, 3)):
        px[x, y] = (255, 255, 255, 255)
    for x, y in ((8, 4), (9, 3)):
        px[x, y] = (120, 190, 230, 255)
    for x, y in ((7, 5), (8, 5)):
        px[x, y] = gold_dark                                      # the setting
    os.makedirs(os.path.join(TEX, "item"), exist_ok=True)
    img.save(os.path.join(TEX, "item", "wedding_ring.png"))
    return img


def veil():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    px = img.load()
    for y in range(32):
        for x in range(32):
            if x < 16 and y < 16:                                 # the drape: sheer tulle, a scalloped hem pattern
                lace = (x + y) % 4 == 0 or (x - y) % 4 == 0
                px[x, y] = (255, 255, 255, 190 if lace else 120)
            elif y < 16:                                          # the band: denser lace with little flowers
                flower = (x % 4, y % 4) in ((1, 1), (2, 2))
                px[x, y] = (255, 214, 228, 255) if flower else (252, 250, 246, 230)
            else:
                px[x, y] = (255, 255, 255, 140)
    os.makedirs(os.path.join(TEX, "entity"), exist_ok=True)
    img.save(os.path.join(TEX, "entity", "veil.png"))
    return img


def main():
    r, v = ring(), veil()
    if "--preview" in sys.argv:
        sheet = Image.new("RGBA", (6 * 16 + 12 * 32 + 30, 12 * 32 + 20), (40, 40, 48, 255))
        big = r.resize((96, 96), Image.NEAREST)
        sheet.paste(big, (10, 10), big)
        bv = v.resize((384, 384), Image.NEAREST)
        sheet.paste(bv, (116, 10), bv)
        path = sys.argv[sys.argv.index("--preview") + 1]
        os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
        sheet.save(path)


if __name__ == "__main__":
    main()
