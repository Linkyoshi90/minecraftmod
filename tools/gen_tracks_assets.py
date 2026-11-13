#!/usr/bin/env python3
"""Footprints (2026-11-13, block/FootprintBlock, entity/Signs): five print textures (paw, hoof, claw, foot, slither; pointing north, a pair
of prints each, dark and see-through so the ground shows), a flat translucent model per kind, the blockstate (facing x kind), and lang.

    uv run --no-project --with pillow python tools/gen_tracks_assets.py
"""
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS, dump  # noqa: E402

INK = (46, 34, 24, 120)
DEEP = (30, 22, 16, 150)
S = 4  # drawn at 64x64, shrunk to 16x16


def canvas():
    return Image.new("RGBA", (16 * S, 16 * S), (0, 0, 0, 0))


def finish(img, name):
    img = img.resize((16, 16), Image.LANCZOS)
    px = img.load()
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            px[x, y] = (r, g, b, 0) if a < 40 else (INK[0], INK[1], INK[2], min(160, a))
    img.save(os.path.join(ASSETS, "textures", "block", f"footprint_{name}.png"))


def paw(d, cx, cy):
    d.ellipse([cx - 7, cy - 2, cx + 7, cy + 10], fill=DEEP)                     # the pad
    for dx, dy in ((-9, -9), (-3, -13), (3, -13), (9, -9)):                       # four toes ahead of it
        d.ellipse([cx + dx - 3, cy + dy - 3, cx + dx + 3, cy + dy + 3], fill=DEEP)


def hoof(d, cx, cy):
    d.ellipse([cx - 8, cy - 10, cx + 8, cy + 10], fill=DEEP)
    d.ellipse([cx - 4, cy - 2, cx + 4, cy + 12], fill=(0, 0, 0, 0))                # the frog of the hoof, open at the heel
    d.line([cx, cy - 10, cx, cy - 2], fill=(0, 0, 0, 0), width=2)                 # a cloven split


def claw(d, cx, cy):
    for ang in (-28, 0, 28):                                                     # three long toes, a claw mark beyond each
        import math
        a = math.radians(ang - 90)
        x1, y1 = cx + math.cos(a) * 12, cy + math.sin(a) * 12
        d.line([cx, cy, x1, y1], fill=DEEP, width=4)
        d.ellipse([x1 + math.cos(a) * 4 - 2, y1 + math.sin(a) * 4 - 2, x1 + math.cos(a) * 4 + 2, y1 + math.sin(a) * 4 + 2], fill=DEEP)
    d.ellipse([cx - 4, cy - 2, cx + 4, cy + 6], fill=DEEP)


def foot(d, cx, cy):
    d.ellipse([cx - 6, cy - 8, cx + 6, cy + 14], fill=DEEP)                      # sole and heel
    for i, dx in enumerate((-5, -2, 1, 4, 6)):
        r = 3 - i * 0.4
        d.ellipse([cx + dx - r, cy - 14 - r + i, cx + dx + r, cy - 14 + r + i], fill=DEEP)


def main():
    for name, fn, offs in (("paw", paw, ((-12, 18), (12, -10))), ("hoof", hoof, ((-11, 16), (11, -12))),
                           ("claw", claw, ((-12, 18), (12, -8))), ("foot", foot, ((-10, 16), (10, -12)))):
        img = canvas()
        d = ImageDraw.Draw(img)
        for ox, oy in offs:
            fn(d, 32 + ox, 32 + oy)
        finish(img, name)
    img = canvas()                                                               # a tail's sweep: one long S through the sand
    d = ImageDraw.Draw(img)
    import math
    pts = [(32 + math.sin(t / 64 * math.pi * 2) * 14, t) for t in range(0, 65, 2)]
    d.line(pts, fill=DEEP, width=9)
    finish(img, "slither")

    for kind in ("paw", "hoof", "claw", "foot", "slither"):
        dump(os.path.join(ASSETS, "models", "block", f"footprint_{kind}.json"), {
            "render_type": "minecraft:translucent", "ambientocclusion": False,
            "textures": {"print": f"harmontide:block/footprint_{kind}", "particle": f"harmontide:block/footprint_{kind}"},
            "elements": [{"from": [0, 0.05, 0], "to": [16, 0.05, 16],
                          "faces": {"up": {"uv": [0, 0, 16, 16], "texture": "#print"}}}]})
    rot = {"north": 0, "east": 90, "south": 180, "west": 270}
    variants = {}
    for kind in ("paw", "hoof", "claw", "foot", "slither"):
        for facing, y in rot.items():
            v = {"model": f"harmontide:block/footprint_{kind}"}
            if y:
                v["y"] = y
            variants[f"facing={facing},kind={kind}"] = v
    dump(os.path.join(ASSETS, "blockstates", "footprint.json"), {"variants": variants})


if __name__ == "__main__":
    main()
