#!/usr/bin/env python3
"""Icons and item models for the legends' relics (2026-11-11, entity/LegendTrials, item/RelicItem): sky plume, tide pearl,
serpent's eye, dragon scale, moon mirror. 16x16 pixel art drawn here.

    uv run --no-project --with pillow python tools/gen_relic_assets.py [--preview previews/relics.png]
"""
import json
import math
import os
import sys

from PIL import Image

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, "src", "main", "resources", "assets", "monstergirls")
OUTLINE = (40, 30, 40, 255)


def blank():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def outline(img):
    """A dark rim around every drawn pixel (on empty neighbours)."""
    px = img.load()
    src = img.copy().load()
    for y in range(16):
        for x in range(16):
            if src[x, y][3]:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < 16 and 0 <= ny < 16 and src[nx, ny][3] and src[nx, ny] != OUTLINE:
                    px[x, y] = OUTLINE
                    break
    return img


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (255,)


def sky_plume():
    img = blank()
    px = img.load()
    ax, ay, bx, by = 2.5, 13.5, 13.5, 2.5                 # the shaft, foot to tip
    L = math.hypot(bx - ax, by - ay)
    ux, uy = (bx - ax) / L, (by - ay) / L
    for y in range(16):
        for x in range(16):
            vx, vy = x + 0.5 - ax, y + 0.5 - ay
            t = (vx * ux + vy * uy) / L                    # along the shaft, 0..1
            d = vx * -uy + vy * ux                         # across it
            if not 0.08 <= t <= 1.0:
                continue
            half = 2.8 * math.sin(math.pi * min(1.0, (t - 0.08) / 0.92)) ** 0.8
            if abs(d) <= 0.55:
                px[x, y] = (200, 160, 70, 255)
            elif abs(d) <= half:
                vane = (int(250 - 25 * t), int(248 - 55 * t), int(238 - 150 * t), 255)
                px[x, y] = shade(vane, 0.86 if d > 0 else 1.0)
    for i in range(3):                                     # the bare quill
        px[1 + i, 14 - i] = (200, 160, 70, 255)
    return outline(img)


def disc(img, cx, cy, r, colour_at):
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
            if d <= r:
                px[x, y] = colour_at(x, y, d)


def tide_pearl():
    img = blank()
    disc(img, 8, 8.5, 5.5, lambda x, y, d: shade((150, 225, 220), 1.15 - d / 9 - (y - 8) / 30))
    px = img.load()
    for x, y in ((6, 5), (5, 6), (6, 6), (7, 5)):
        px[x, y] = (255, 255, 255, 255)
    for x in range(3, 13):                                # a wave of foam across its foot
        y = 12 + (1 if x % 3 == 0 else 0)
        if px[x, y][3]:
            px[x, y] = (220, 250, 255, 255)
    return outline(img)


def serpent_eye():
    img = blank()
    disc(img, 8, 8, 6, lambda x, y, d: shade((70, 140, 60), 1.1 - d / 12) if (x + y) % 3 else shade((50, 110, 45), 1.0))
    px = img.load()
    for y in range(4, 12):                                # the eye: amber almond, a black slit
        half = int(3.2 * math.sin(math.pi * (y - 3.5) / 8.5))
        for x in range(8 - half, 8 + half + 1):
            px[x, y] = (240, 190, 40, 255) if abs(x - 8) > 0 else (20, 10, 10, 255)
    px[7, 6] = (255, 240, 170, 255)
    return outline(img)


def dragon_scale():
    img = blank()
    px = img.load()
    for y in range(2, 15):                                # a shield-shaped scale, pointed at the foot
        half = 6 if y < 9 else max(0, 6 - (y - 8))
        for x in range(8 - half, 8 + half):
            f = 1.2 - (y - 2) / 24 - abs(x + 0.5 - 8) / 20
            px[x, y] = shade((205, 55, 30), f)
    for row in (5, 9):                                    # overlapping smaller scales: curved darker seams
        for x in range(3, 13):
            y = row + int(round(abs(x + 0.5 - 8) / 3))
            if px[x, y][3]:
                px[x, y] = (120, 25, 20, 255)
    for x, y in ((5, 3), (6, 3), (5, 4)):
        px[x, y] = (255, 170, 110, 255)                   # a glint
    return outline(img)


def moon_mirror():
    img = blank()
    disc(img, 8, 6.5, 5, lambda x, y, d: (190, 195, 215, 255) if d > 4 else shade((120, 140, 200), 1.2 - d / 10))
    px = img.load()
    disc_moon = [(x, y) for y in range(16) for x in range(16)
                 if math.hypot(x + 0.5 - 7.5, y + 0.5 - 6) <= 2.6 and math.hypot(x + 0.5 - 8.8, y + 0.5 - 5.3) > 2.1]
    for x, y in disc_moon:
        px[x, y] = (250, 245, 210, 255)
    for y in range(11, 15):                               # the handle
        px[7, y] = (170, 175, 195, 255)
        px[8, y] = (130, 135, 160, 255)
    return outline(img)


ICONS = {"sky_plume": sky_plume, "tide_pearl": tide_pearl, "serpent_eye": serpent_eye, "dragon_scale": dragon_scale, "moon_mirror": moon_mirror}


def main():
    made = {}
    for name, draw in ICONS.items():
        img = draw()
        img.save(os.path.join(ASSETS, "textures", "item", name + ".png"))
        with open(os.path.join(ASSETS, "models", "item", name + ".json"), "w", encoding="utf-8", newline="\n") as fh:
            json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:item/" + name}}, fh, indent=2)
            fh.write("\n")
        made[name] = img
    if "--preview" in sys.argv:
        out = sys.argv[sys.argv.index("--preview") + 1]
        sheet = Image.new("RGBA", (len(made) * 136, 136), (60, 60, 70, 255))
        for i, img in enumerate(made.values()):
            sheet.paste(img.resize((128, 128), Image.NEAREST), (i * 136 + 4, 4), img.resize((128, 128), Image.NEAREST))
        os.makedirs(os.path.dirname(os.path.join(REPO, out)), exist_ok=True)
        sheet.save(os.path.join(REPO, out))
    print("relic icons:", ", ".join(made))


if __name__ == "__main__":
    main()
