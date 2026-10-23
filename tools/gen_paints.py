#!/usr/bin/env python3
"""Harmontide paints: every girl species' default texture in the seven colour paints of the Harmontide creatures
(blue, red, green, yellow, purple, black, inverted), as textures/entity/<model>_paint_<paint>.png.

    python tools/gen_paints.py

Run after the model scripts (it reads their textures). Like the reference paint jobs, a colour paint turns her skin,
hair and fur into the paint's colour and shifts everything else (clothes, accessories) by the same hue step, so the
colour relations survive; whites, greys and blacks stay as they are. Black is greyscale, inverted flips every colour.
A painted girl is a rare spawn (AbstractMonsterGirl.PAINT_RARITY) and her daughters inherit the paint.
"""
import colorsys
import os
import re
import sys
from collections import Counter

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402

REPO = H.REPO
ENT = os.path.join(REPO, "src", "main", "resources", "assets", "monstergirls", "textures", "entity")
GEO = os.path.join(REPO, "src", "main", "resources", "assets", "monstergirls", "geo", "entity")
JAVA = os.path.join(REPO, "src", "main", "java", "com", "linkyoshi90", "monstergirls", "entity", "girl", "GirlSpecies.java")
HUES = {"blue": 0.58, "red": 0.0, "green": 0.33, "yellow": 0.14, "purple": 0.77}
GLOWING = {"magma_slime", "will_o_wisp_undead", "salamander_lizardfolk", "jack_o_lantern_devil"}


def load(path):
    from PIL import Image
    im = Image.open(path).convert("RGBA")
    return [[im.getpixel((x, y)) for x in range(im.width)] for y in range(im.height)]


def skinish(h, s, v):
    return 0.0 <= h <= 0.12 and 0.12 < s < 0.65 and v > 0.45


def dominant_hue(img):
    """the most common hue among her saturated, non-skin pixels (her fur, hair, scales)"""
    c = Counter()
    for row in img:
        for r, g, b, a in row:
            if not a:
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if s > 0.25 and v > 0.2 and not skinish(h, s, v):
                c[round(h * 36) % 36] += 1
    return (c.most_common(1)[0][0] / 36.0) if c else 0.08


def paint(img, name):
    out = []
    if name == "inverted":
        return [[(255 - r, 255 - g, 255 - b, a) if a else (r, g, b, a) for r, g, b, a in row] for row in img]
    if name == "black":
        for row in img:
            o = []
            for r, g, b, a in row:
                l = int((0.299 * r + 0.587 * g + 0.114 * b) * 0.72)
                o.append((l, l, int(l * 1.04) if l < 245 else l, a))
            out.append(o)
        return out
    target = HUES[name]
    dom = dominant_hue(img)
    shift = target - dom
    for row in img:
        o = []
        for r, g, b, a in row:
            if not a:
                o.append((r, g, b, a))
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if s < 0.12:                                            # whites, greys, blacks: as they are
                o.append((r, g, b, a))
                continue
            if skinish(h, s, v) or abs(((h - dom + 0.5) % 1.0) - 0.5) < 0.08:
                h, s = target, max(s, 0.38)                          # her body: the paint's colour
            else:
                h = (h + shift) % 1.0                                # everything else keeps its relation
            rr, gg, bb = colorsys.hsv_to_rgb(h, min(1.0, s), v)
            o.append((int(rr * 255), int(gg * 255), int(bb * 255), a))
        out.append(o)
    return out


def models():
    ids = re.findall(r'\("([a-z_]+)", Family\.', open(JAVA, encoding="utf-8").read())
    out = []
    for sp in ids:
        for m in (sp, sp + "_male"):
            if os.path.exists(os.path.join(GEO, m + ".geo.json")) and os.path.exists(os.path.join(ENT, m + ".png")):
                out.append(m)
    return out


def main():
    import shutil
    ms = models()
    for m in ms:
        img = load(os.path.join(ENT, m + ".png"))
        for p in ["blue", "red", "green", "yellow", "purple", "black", "inverted"]:
            H.write_png(os.path.join(ENT, f"{m}_paint_{p}.png"), paint(img, p))
            glow = os.path.join(ENT, m + "_glowmask.png")
            if m in GLOWING and os.path.exists(glow):              # the glow layer looks for <texture>_glowmask.png
                shutil.copyfile(glow, os.path.join(ENT, f"{m}_paint_{p}_glowmask.png"))
    print(f"{len(ms)} models x 7 paints")


if __name__ == "__main__":
    main()
