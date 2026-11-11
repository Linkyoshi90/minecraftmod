#!/usr/bin/env python3
"""Headgear friends give (2026-11-11, entity/Headgear, item/HeadgearItem): kitsune mask, bunny ears, witch hat, flower crown.
GeckoLib armour models (cubes on armorHead), their 64x32 textures, an empty animation file each, 16x16 icons and item models,
and a blank vanilla armour layer for the shared material.

    uv run --no-project --with pillow python tools/gen_headgear_assets.py [--preview previews/headgear.png]
"""
import json
import math
import os
import random
import sys

from PIL import Image

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, "src", "main", "resources", "assets", "monstergirls")
OUTLINE = (40, 30, 40, 255)


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


# --- the models: (origin, size, colour, accent) per cube, all on armorHead ------------------------------------------------
# The head runs x -4..4, y 24..32, z -4..4 (front at z -4).
HATS = {
    "kitsune_mask": [                                    # a festival mask worn pushed to the side of the head
        ([-5, 25, -3], [1, 6, 6], "#f4f0e6", "#c8282a"),
        ([-5, 31, -3], [1, 2, 2], "#f4f0e6", "#c8282a"),   # its ears
        ([-5, 31, 1], [1, 2, 2], "#f4f0e6", "#c8282a"),
        ([-4, 30, -1], [8, 1, 2], "#c8282a", None),        # the cord over the head
    ],
    "bunny_ears": [
        ([-4, 32, -1], [8, 1, 2], "#2a2a32", None),        # the band
        ([-3, 33, 0], [2, 7, 1], "#f6f4f0", "#f2a8b8"),    # ears, pink inside
        ([1, 33, 0], [2, 6, 1], "#f6f4f0", "#f2a8b8"),
    ],
    "witch_hat": [
        ([-6, 32, -6], [12, 1, 12], "#2a2238", None),      # brim
        ([-4, 33, -4], [8, 3, 8], "#2a2238", "#8a5ac8"),   # crown with a violet band
        ([-3, 36, -3], [6, 3, 6], "#2a2238", None),
        ([-2, 39, -1], [4, 2, 4], "#2a2238", None),
        ([-1, 41, 1], [2, 2, 2], "#2a2238", None),         # the tip, bent back
    ],
    "flower_crown": [
        ([-5, 31, -5], [10, 1, 1], "#5a9a3a", "#f8d0e0"),  # a ring of leaves with blossoms
        ([-5, 31, 4], [10, 1, 1], "#5a9a3a", "#f8e070"),
        ([-5, 31, -4], [1, 1, 8], "#5a9a3a", "#f8a0c0"),
        ([4, 31, -4], [1, 1, 8], "#5a9a3a", "#c8b0f8"),
        ([-2, 32, -5], [2, 1, 1], "#f8d0e0", None),        # blossoms standing up at the front
        ([1, 32, -5], [2, 1, 1], "#f8e070", None),
    ],
}

BONES = (("armorHead", [0, 24, 0]), ("armorBody", [0, 24, 0]), ("armorRightArm", [-5, 22, 0]), ("armorLeftArm", [5, 22, 0]),
         ("armorRightLeg", [-1.9, 12, 0]), ("armorLeftLeg", [1.9, 12, 0]), ("armorRightBoot", [-1.9, 12, 0]), ("armorLeftBoot", [1.9, 12, 0]))


def build(name, cubes):
    """Lays the cubes' box UVs out in rows on a 64x32 sheet and paints each box; returns (geo, texture)."""
    rnd = random.Random(name)
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    px = img.load()
    u = v = row_h = 0
    geo_cubes = []
    for origin, size, colour, accent in cubes:
        w, h, d = size
        bw, bh = 2 * (d + w), d + h
        if u + bw > 64:
            u, v, row_h = 0, v + row_h, 0
        geo_cubes.append({"origin": origin, "size": size, "uv": [u, v]})
        base = rgb(colour)
        for y in range(v, min(32, v + bh)):
            for x in range(u, u + bw):
                f = 1.0 + (rnd.random() - 0.5) * 0.12
                c = tuple(max(0, min(255, int(ch * f))) for ch in base)
                if accent:
                    a = rgb(accent)
                    ly, lx = y - v, x - u
                    stripe = (name == "kitsune_mask" and ly >= d and (lx + ly) % 4 == 0) or \
                             (name == "bunny_ears" and ly >= d and d <= lx < d + w and 1 <= ly - d < h - 1) or \
                             (name == "witch_hat" and ly == bh - 1) or \
                             (name == "flower_crown" and lx % 3 == 1)
                    if stripe:
                        c = a
                px[x, y] = c + (255,)
        u += bw
        row_h = max(row_h, bh)
    bones = [{"name": n, "pivot": p} for n, p in BONES]
    bones.append({"name": "hat", "parent": "armorHead", "pivot": [0, 24, 0], "cubes": geo_cubes})
    geo = {"format_version": "1.12.0", "minecraft:geometry": [{"description": {"identifier": "geometry." + name, "texture_width": 64,
            "texture_height": 32, "visible_bounds_width": 2, "visible_bounds_height": 3, "visible_bounds_offset": [0, 1.5, 0]}, "bones": bones}]}
    return geo, img


# --- the icons ----------------------------------------------------------------------------------------------------------

def outline(img):
    px = img.load()
    src = img.copy().load()
    for y in range(16):
        for x in range(16):
            if src[x, y][3]:
                continue
            if any(0 <= x + dx < 16 and 0 <= y + dy < 16 and src[x + dx, y + dy][3] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                px[x, y] = OUTLINE
    return img


def icon(name):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    if name == "kitsune_mask":
        for y in range(3, 14):
            for x in range(3, 13):
                if math.hypot((x + 0.5 - 8) / 5, (y + 0.5 - 8.5) / 5.5) <= 1:
                    px[x, y] = (244, 240, 230, 255)
        for x, y in ((3, 2), (4, 1), (4, 2), (12, 2), (11, 1), (11, 2)):
            px[x, y] = (244, 240, 230, 255)
        for x, y in ((5, 7), (6, 7), (9, 7), (10, 7), (7, 11), (8, 11), (8, 4), (7, 5), (8, 5)):
            px[x, y] = (200, 40, 42, 255)
    elif name == "bunny_ears":
        for x in range(2, 14):
            px[x, 12] = (42, 42, 50, 255)
            px[x, 13] = (42, 42, 50, 255)
        for ex in (4, 9):
            for y in range(2, 12):
                for x in range(ex, ex + 3):
                    px[x, y] = (246, 244, 240, 255) if x != ex + 1 or y < 3 or y > 10 else (242, 168, 184, 255)
    elif name == "witch_hat":
        for x in range(1, 15):
            px[x, 12] = (42, 34, 56, 255)
        for y in range(3, 12):
            half = max(1, (y - 2) * 5 // 9)
            for x in range(8 - half, 8 + half):
                px[x, y] = (42, 34, 56, 255) if y != 10 else (138, 90, 200, 255)
        for x, y in ((9, 2), (10, 1), (11, 1)):
            px[x, y] = (42, 34, 56, 255)
    elif name == "flower_crown":
        for a in range(0, 360, 8):
            x = int(round(8 + 6 * math.cos(math.radians(a))))
            y = int(round(9 + 3.5 * math.sin(math.radians(a))))
            px[min(15, x), min(15, y)] = (90, 154, 58, 255)
        for (x, y), c in zip(((3, 7), (8, 5), (13, 7), (5, 12), (11, 12)),
                             ((248, 208, 224), (248, 224, 112), (248, 160, 192), (200, 176, 248), (248, 208, 224))):
            for dx, dy in ((0, 0), (1, 0), (0, 1), (-1, 0), (0, -1)):
                if 0 <= x + dx < 16 and 0 <= y + dy < 16:
                    px[x + dx, y + dy] = c + (255,) if (dx, dy) != (0, 0) else (250, 230, 120, 255)
    return outline(img)


def main():
    icons = {}
    for name, cubes in HATS.items():
        geo, tex = build(name, cubes)
        dump(os.path.join(ASSETS, "geo", "item", "armor", name + ".geo.json"), geo)
        dump(os.path.join(ASSETS, "animations", "item", "armor", name + ".animation.json"), {"format_version": "1.8.0", "animations": {}})
        os.makedirs(os.path.join(ASSETS, "textures", "item", "armor"), exist_ok=True)
        tex.save(os.path.join(ASSETS, "textures", "item", "armor", name + ".png"))
        ic = icon(name)
        ic.save(os.path.join(ASSETS, "textures", "item", name + ".png"))
        dump(os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:item/" + name}})
        icons[name] = (ic, tex)
    os.makedirs(os.path.join(ASSETS, "textures", "models", "armor"), exist_ok=True)
    Image.new("RGBA", (64, 32), (0, 0, 0, 0)).save(os.path.join(ASSETS, "textures", "models", "armor", "headgear_layer_1.png"))
    if "--preview" in sys.argv:
        out = os.path.join(REPO, sys.argv[sys.argv.index("--preview") + 1])
        sheet = Image.new("RGBA", (len(icons) * 264, 264), (60, 60, 70, 255))
        for i, (ic, tex) in enumerate(icons.values()):
            big = ic.resize((128, 128), Image.NEAREST)
            sheet.paste(big, (i * 264 + 4, 4), big)
            t = tex.resize((256, 128), Image.NEAREST)
            sheet.paste(t, (i * 264 + 4, 134), t)
        os.makedirs(os.path.dirname(out), exist_ok=True)
        sheet.save(out)
    print("headgear:", ", ".join(icons))


if __name__ == "__main__":
    main()
