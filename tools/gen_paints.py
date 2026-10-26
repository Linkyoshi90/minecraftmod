#!/usr/bin/env python3
"""Harmontide paints: every girl species' default texture in the Harmontide creatures' paints, as
textures/entity/<model>_paint_<paint>.png. Seven colour paints (blue, red, green, yellow, purple, black, inverted)
and five treat paints that are more than a palette swap: chocolate (a chocolate girl with darker chocolate dripping
down every part), ice cream (strawberry ice cream melting off her hair, with sprinkles), strawberry (strawberry hair
with seeds and a leafy crown), jello (glossy and see-through: the game draws it translucent) and milk (a creamy
porcelain-white girl, no drips). Six more since 2026-10-24: gold (a gilded statue), marble (white stone with grey
veins), galaxy (a night sky with stars), ghost (pale and see-through, drawn translucent like jello), honey (honey
dripping from her hair) and rainbow (bands of colour running down every part). And since 2026-10-25: copper in its
four weathering stages (copper, copper_exposed, copper_weathered, copper_oxidized; the game ages one into the next),
glow (dark, with neon markings that glow in the dark), ink (manga line art), magma (basalt with glowing cracks), and
the seasonal pumpkin and candy_cane. Glow and magma also write <model>_paint_<paint>_glowmask.png. And (2026-10-25,
eighth wave): frost (icy, frosted on every top), sakura (soft pink, petals in her hair), stained_glass (bright panes and
lead lines, drawn translucent) and sepia (an old photograph). And (ninth wave): opal (pearly white with a shifting
rainbow sheen), chrome (mirror metal with a bright reflection band), aurora (a night body with green-to-violet bands
of light) and camo (blotchy woodland camouflage). And (tenth wave, from the reference looks): plush (soft pastel felt
with stitched seams), pixel (an 8-bit palette in chunky pixels), origami (folded paper, light and shade either side of
each fold) and gummy (bright see-through candy, drawn translucent). And (eleventh wave): synthwave (a dark violet body
with a neon sunset gradient and grid lines), cookie (baked dough with chocolate chips and browned edges), cloud (soft
white with sky-blue shade underneath) and the seasonal valentine (pink and red with little hearts). And (twelfth wave):
porcelain (white glaze with blue painted motifs), denim (blue twill with orange stitching), moss (grey stone overgrown with
moss) and the seasonal easter (pastel egg stripes, April only). The drips and
patterns follow the model: they are drawn per cube face from the geo file, running down each side from its top edge.

    python tools/gen_paints.py [paint ...] [model ...]   # everything, or just the paints and/or models named

Run after the model scripts (it reads their textures). Like the reference paint jobs, a colour paint turns her skin,
hair and fur into the paint's colour and shifts everything else (clothes, accessories) by the same hue step, so the
colour relations survive; whites, greys and blacks stay as they are. Black is greyscale, inverted flips every colour.
Harpies and anurans too (their adult and young textures). A painted girl is a rare spawn (AbstractMonsterGirl.PAINT_RARITY) and her daughters inherit the paint.
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
# the harpies and anurans (their own entity classes): the adult texture and the young ones (hatchling, tadpole, froglet)
OTHERS = ["sparrow_harpy", "crow_harpy", "ostrich_harpy", "owl_harpy", "seagull_harpy", "albatross_harpy", "riflebird_harpy", "phoenix_harpy", "tengu_harpy", "toucan_harpy", "penguin_harpy", "vodyanoy", "zhuque_harpy", "finch_harpy", "flamingo_harpy", "coin_magpie_harpy", "trickster_raven_harpy", "rookmourn_harpy", "cassowrath_harpy", "dodobold_harpy",
          "bullywug", "mucus_toad", "bullfrog", "tree_frog", "poison_dart_frog", "glass_frog", "goliath_frog", "desert_rain_frog",
          "fire_bellied_toad", "cane_toad", "natterjack", "surinam_toad", "mailtoad"]


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


# ----------------------------------------------------------------------------- treat paints (need the model's faces)
import json  # noqa: E402
import math  # noqa: E402
import random  # noqa: E402

HAIR_BONES = ("hair", "bang", "strand", "tuft", "ahoge", "mane", "braid", "pony", "drill", "bun", "twin")


def faces(model, width):
    """[(is_hair, is_face, face_name, x, y, w, h)] in texture pixels, from the geo file's per-face UVs"""
    path = os.path.join(GEO, model + ".geo.json")
    if not os.path.exists(path):
        return []
    g = json.load(open(path))["minecraft:geometry"][0]
    k = width / g["description"]["texture_width"]
    out = []
    for b in g["bones"]:
        name = b["name"]
        cubes = b.get("cubes", [])
        head_cube = max(cubes, key=lambda c: c["size"][0] * c["size"][1] * c["size"][2]) if name == "head" and cubes else None
        for c in cubes:
            hair = any(t in name for t in HAIR_BONES) or (name == "head" and c is not head_cube)
            uv = c.get("uv")
            if not isinstance(uv, dict):
                continue
            for fname, f in uv.items():
                (u, v), (w, h) = f["uv"], f["uv_size"]
                x0, y0 = int(round(min(u, u + w) * k)), int(round(min(v, v + h) * k))
                fw, fh = int(round(abs(w) * k)), int(round(abs(h) * k))
                if fw > 0 and fh > 0:
                    out.append((hair, c is head_cube and fname == "north", fname, x0, y0, fw, fh))
    return out


def lum(r, g, b):
    return (0.299 * r + 0.587 * g + 0.114 * b) / 255.0


def ramp_pick(ramp, t):
    t = max(0.0, min(0.999, t))
    i = t * (len(ramp) - 1)
    a, b = ramp[int(i)], ramp[int(i) + 1]
    f = i - int(i)
    return tuple(int(a[j] + (b[j] - a[j]) * f) for j in range(3))


def hexes(*h):
    return [tuple(int(x.lstrip("#")[i:i + 2], 16) for i in (0, 2, 4)) for x in h]


MILK_CHOC = hexes("#3a2214", "#6a3e22", "#94603a", "#b8845a", "#d8aa80")
DARK_CHOC = hexes("#1e0e06", "#3a1e10", "#5a3018", "#7a4826", "#a06a42")
PINK_CREAM = hexes("#c86a88", "#e890a8", "#f6b8c8", "#fcd8e2", "#fff2f6")
BERRY = hexes("#7a0e18", "#b01e28", "#d8323a", "#ec5a5a", "#f89090")
LEAF = hexes("#1e4a1a", "#2e6a26", "#46902e", "#6ab840")
GOLD = hexes("#4a2e08", "#8a5e14", "#c8941e", "#f0c440", "#fff0a0")
MARBLE = hexes("#6a6a6c", "#a6a4a0", "#cac8c2", "#e4e2dc", "#f6f4f0")
NIGHT = hexes("#060818", "#141a40", "#2a2a6a", "#4a3a8a", "#7a5ab0")
HONEY = hexes("#5a2402", "#8a4206", "#c06a0e", "#e8961e", "#fcc860")
SPRINKLES = hexes("#f8e040", "#5ac8f0", "#8ae060", "#f8f8f8", "#b07ae8")


def drip_len(seed, col, h, longer=1.0):
    """how far down a face the coating runs at a column: smooth, with now and then a long drip"""
    rnd = random.Random(seed * 7919 + col // 2)
    base = 0.2 + 0.35 * (0.5 + 0.5 * math.sin(col * 0.9 + seed))
    if rnd.random() < 0.18:
        base += 0.45
    return max(1, int(h * min(0.95, base * longer)))


def treat(img, name, model):
    if name in PATINA_SHARE or name in ("glow", "ink", "magma", "pumpkin", "candy_cane", "frost", "sakura", "stained_glass", "sepia",
                                        "opal", "chrome", "aurora", "camo", "plush", "pixel", "origami", "gummy",
                                        "synthwave", "cookie", "cloud", "valentine", "porcelain", "denim", "moss", "easter"):
        return finish(img, name, model)[0]
    W = len(img[0])
    out = [list(row) for row in img]
    fs = faces(model, W)
    if name in ("gold", "marble", "galaxy", "ghost"):                   # a whole-body finish, eyes and outlines kept
        rnd = random.Random(model + name)
        for y, row in enumerate(out):
            for x, (r, g, b, a) in enumerate(row):
                if not a:
                    continue
                l = lum(r, g, b)
                if name == "ghost":
                    row[x] = (r, g, b, 235) if l < 0.22 else (int(200 + 50 * l), int(228 + 27 * l), 255, 140)
                    continue
                if l < 0.22:
                    continue
                if name == "gold":
                    c = (255, 252, 220) if rnd.random() < 0.01 else ramp_pick(GOLD, l * 1.15)
                elif name == "marble":
                    c = ramp_pick(MARBLE, 0.3 + 0.6 * l)
                else:
                    n = 0.5 + 0.5 * math.sin(x * 0.21 + math.sin(y * 0.13) * 2.5)    # a drifting nebula
                    c = (255, 250, 220) if rnd.random() < 0.015 else ramp_pick(NIGHT, 0.2 + 0.5 * l + 0.3 * n)
                row[x] = c + (a,)
        if name == "marble":                                            # grey veins wandering across each face
            for i, (hair, face, fname, x0, y0, fw, fh) in enumerate(fs):
                if face or fh < 3:
                    continue
                for v, (amp, freq) in enumerate(((0.35, 0.45), (0.25, 0.8))):
                    for xx in range(fw):
                        y = y0 + int(fh * (0.3 + 0.4 * v + amp * math.sin(xx * freq + i * 1.7 + v * 2.1)))
                        if y < len(out) and x0 + xx < W and out[y][x0 + xx][3] and lum(*img[y][x0 + xx][:3]) >= 0.22:
                            out[y][x0 + xx] = (96, 94, 92, out[y][x0 + xx][3])
        return out
    if name == "rainbow":                                               # bands of colour down every face
        for i, (hair, face, fname, x0, y0, fw, fh) in enumerate(fs):
            if face:
                continue                                                # her face stays her own
            for yy in range(fh):
                hue = ((i // 6) * 0.11 + yy / max(1, fh) * 0.12) % 1.0   # each part its own colour, softly graded
                for xx in range(fw):
                    x, y = x0 + xx, y0 + yy
                    if y >= len(out) or x >= W or not img[y][x][3]:
                        continue
                    l = lum(*img[y][x][:3])
                    if l < 0.22:
                        continue
                    rr, gg, bb = colorsys.hsv_to_rgb(hue, 0.6, 0.55 + 0.45 * l)
                    out[y][x] = (int(rr * 255), int(gg * 255), int(bb * 255), img[y][x][3])
        return out
    if name in ("jello", "milk"):
        rnd = random.Random(model)
        for y, row in enumerate(out):
            for x, (r, g, b, a) in enumerate(row):
                if not a:
                    continue
                l = lum(r, g, b)
                if name == "milk":
                    if l < 0.22:
                        continue                                        # eyes, brows, outlines stay
                    t = 0.82 + 0.18 * l
                    row[x] = (int(252 * t), int(247 * t), int(236 * t), a)
                else:
                    r2, g2, b2 = (min(255, int(c * 1.08 + 12)) for c in (r, g, b))
                    sparkle = rnd.random() < 0.012
                    row[x] = (255, 255, 255, 235) if sparkle else (r2, g2, b2, min(a, 175 if l > 0.18 else 230))
        return out
    if name == "chocolate":                                             # a milk-chocolate girl first
        for y, row in enumerate(out):
            for x, (r, g, b, a) in enumerate(row):
                if a:
                    row[x] = ramp_pick(MILK_CHOC, lum(r, g, b) * 1.1) + (a,)
    for i, (hair, face, fname, x0, y0, fw, fh) in enumerate(fs):
        if name == "chocolate":
            coat, longer, where = DARK_CHOC, 1.0, True
        elif name == "icecream":
            coat, longer, where = PINK_CREAM, 0.7, hair                     # short drips: her own hair shows below them
        elif name == "honey":
            coat, longer, where = HONEY, 1.0, hair or fname == "up"         # from her hair, and pooled on every top
        else:                                                           # strawberry
            coat, longer, where = BERRY, 0.0, hair
        if not where:
            continue
        rnd = random.Random(i * 31 + len(model))
        for xx in range(fw):
            x = x0 + xx
            if fname == "up":
                length = fh                                             # a top face is coated all over
            elif fname == "down":
                length = 0
            elif face:
                length = min(2, fh)                                     # never over her eyes: only the brow line
            elif name == "strawberry":
                length = fh
            else:
                length = drip_len(i, xx, fh, longer)
            for yy in range(length):
                y = y0 + yy
                if y >= len(out) or x >= W or not out[y][x][3]:
                    continue
                r, g, b, a = img[y][x]
                if name == "strawberry":
                    if fname == "up" and (xx + yy) % 2 == 0:
                        c = ramp_pick(LEAF, 0.4 + 0.4 * rnd.random())  # the leafy crown on top
                    elif (yy % 4 == 1 and xx % 4 == (yy // 4 % 2) * 2):
                        c = (248, 224, 96)                              # seeds
                    else:
                        c = ramp_pick(coat, 0.35 + 0.6 * lum(r, g, b))
                else:
                    t = 0.45 + 0.3 * lum(r, g, b)
                    if yy == length - 1 and fname not in ("up",):
                        t -= 0.15                                       # the drip's rounded end
                    if xx % 5 == 1 and yy < length - 1:
                        t += 0.25                                       # a glossy streak
                    c = ramp_pick(coat, t)
                    if name == "icecream" and rnd.random() < 0.05:
                        c = SPRINKLES[rnd.randrange(len(SPRINKLES))]
                out[y][x] = c + (max(a, 255),)
    return out


TREATS = ["chocolate", "icecream", "strawberry", "jello", "milk", "gold", "marble", "galaxy", "ghost", "honey", "rainbow",
          "copper", "copper_exposed", "copper_weathered", "copper_oxidized", "glow", "ink", "magma", "pumpkin", "candy_cane",
          "frost", "sakura", "stained_glass", "sepia", "opal", "chrome", "aurora", "camo", "plush", "pixel", "origami", "gummy",
          "synthwave", "cookie", "cloud", "valentine", "porcelain", "denim", "moss", "easter"]
PEARL = hexes("#b8b0c0", "#d8d2e0", "#ece8f2", "#f8f6fc", "#ffffff")
CHROME = hexes("#1e2024", "#4a4e56", "#8a909a", "#c8ccd4", "#ffffff")
AURORA = hexes("#2af08a", "#3ad8c8", "#5aa0f0", "#8a6af0", "#c86ae8")
CAMO = hexes("#2e3a1e", "#5a6a32", "#8a7a4a", "#2a2420")
ICE = hexes("#3a6a9a", "#6a9ac8", "#a0c8e8", "#d0e8f8", "#f4fbff")
BLOSSOM = hexes("#c86a8a", "#e890a8", "#f6b8c8", "#fcd8e2", "#fff2f6")
SEPIA = hexes("#2e2014", "#5a4028", "#8a6a44", "#b8966a", "#e2cca4")
PANES = hexes("#c8202a", "#2a5ac8", "#f0c020", "#2a9a4a", "#8a3ab0", "#e8702a")
COPPER = hexes("#5a2a14", "#8a4426", "#c06a3e", "#e08a5a", "#f8b890")
PATINA = hexes("#24524a", "#357560", "#4a9a7a", "#6ab89a", "#9ad8bc")
PATINA_SHARE = {"copper": 0.0, "copper_exposed": 0.3, "copper_weathered": 0.62, "copper_oxidized": 1.01}
BASALT = hexes("#0e0c0e", "#1e1a1c", "#2e2a2c", "#44403e", "#5a5652")
EMBER = hexes("#a8280a", "#e85a14", "#f8901e", "#ffc84a", "#fff0a0")
NEON = hexes("#1a8a8a", "#2ad0c8", "#5af8f0", "#b0fff8")
NIGHTBODY = hexes("#0a0a14", "#14141e", "#20202e", "#2e2e40", "#40405a")
PUMPKIN = hexes("#7a2e06", "#b04a0e", "#e0701a", "#f89a3a", "#ffc070")
DENIM = hexes("#1e2e5a", "#2e4a82", "#4a6aa8", "#7a96c8")
MOSS = hexes("#2e4a1e", "#4a6e2a", "#6a9a3a", "#9ac85a")
STONE = hexes("#4a4a4e", "#6e6e72", "#909094", "#b8b8bc")
PASTEL = [(240, 150, 186), (140, 196, 240), (244, 214, 110), (150, 214, 150), (196, 160, 236)]
DOUGH = hexes("#7a4a24", "#b07838", "#d6a460", "#ecca8c")
ROSE = hexes("#b02a54", "#e0607e", "#f39ab0", "#ffd2de")
VINE = hexes("#1e3a14", "#2e5a1e", "#467a2a", "#62a03a")


def face_map(fs, W, H):
    """(x, y) -> (xx, yy, w, h, is_hair, is_face, i) for every face pixel of the model"""
    m = {}
    for i, (hair, face, fname, x0, y0, fw, fh) in enumerate(fs):
        for yy in range(fh):
            for xx in range(fw):
                if x0 + xx < W and y0 + yy < H:
                    m[(x0 + xx, y0 + yy)] = (xx, yy, fw, fh, hair, face, i)
    return m


def finish(img, name, model):
    """the 2026-10-25 paints: returns (texture, glowmask or None)"""
    W, Hh = len(img[0]), len(img)
    out = [list(row) for row in img]
    mask = [[(0, 0, 0, 0)] * W for _ in range(Hh)] if name in ("glow", "magma") else None
    fm = face_map(faces(model, W), W, Hh)
    for y, row in enumerate(out):
        for x, (r, g, b, a) in enumerate(row):
            if not a:
                continue
            l = lum(r, g, b)
            xx, yy, fw, fh, hair, face, i = fm.get((x, y), (x, y, 8, 8, False, False, 0))
            edge = xx in (0, fw - 1) or yy in (0, fh - 1)
            if name.startswith("copper"):
                if l < 0.22:
                    continue
                share = PATINA_SHARE[name]
                c = ramp_pick(PATINA if H.noise(x // 3, y // 3, 23) < share else COPPER, 0.15 + 0.8 * l)
            elif name == "glow":
                if l < 0.22 and face:
                    continue                                        # her eyes stay her own
                neon = edge or (not hair and (xx + 2 * yy) % 11 == 0 and fw > 3)
                c = ramp_pick(NEON, 0.6 + 0.3 * l) if neon else ramp_pick(NIGHTBODY, 0.2 + 0.6 * l)
                if neon:
                    mask[y][x] = c + (255,)
            elif name == "magma":
                if l < 0.22 and face:
                    continue
                vein = lambda k: abs(fh * (0.3 + 0.4 * k + 0.25 * math.sin(xx * (0.5 + 0.3 * k) + i * 1.7 + k * 2.3)) - yy) < 0.75
                crack = (fh > 2 and (vein(0) or vein(1))) or (hair and yy == fh - 1)   # glowing cracks wandering across each face
                c = ramp_pick(EMBER, 0.4 + 0.5 * l) if crack else ramp_pick(BASALT, 0.3 + 0.6 * l)
                if crack:
                    mask[y][x] = c + (255,)
            elif name == "ink":
                if l < 0.35 or edge:
                    c = (24, 22, 26)                                # outlines and anything dark: ink
                elif l < 0.6 and (xx + yy) % 3 == 0:
                    c = (120, 118, 124)                             # hatched shading
                else:
                    c = (int(232 + 20 * l), int(230 + 20 * l), int(226 + 20 * l))
            elif name == "pumpkin":
                if l < 0.22 and face:
                    continue
                c = ramp_pick(VINE, 0.3 + 0.5 * l) if hair else ramp_pick(PUMPKIN, 0.25 + 0.6 * l - 0.18 * (xx % 4 == 0 and fw > 3))
            elif name == "frost":
                if l < 0.22 and face:
                    continue
                frosted = yy < 2 or (fm.get((x, y)) is None) or ((xx * 7 + yy * 3) % 29 == 0)
                c = (244, 250, 255) if frosted else ramp_pick(ICE, 0.25 + 0.6 * l)
            elif name == "sakura":
                if l < 0.22 and face:
                    continue
                c = ramp_pick(BLOSSOM, 0.3 + 0.6 * l)
                if hair and ((xx * 5 + yy * 3 + i) % 17 == 0 or (xx * 5 + yy * 3 + i) % 17 == 1 and yy % 2 == 0):
                    c = (255, 248, 250) if (xx + yy) % 2 else (240, 120, 160)        # a petal caught in her hair
            elif name == "stained_glass":
                if l < 0.22 and face:
                    continue
                if xx % 9 == 0 or yy % 9 == 0:
                    out[y][x] = (30, 26, 30, 240)                       # the lead lines between panes
                    continue
                c = ramp_pick([tuple(min(255, int(v * (0.7 + 0.5 * l))) for v in PANES[(xx // 9 + yy // 9 * 2 + i * 3) % len(PANES)])] * 2, 0.5)
                out[y][x] = c + (190,)
                continue
            elif name == "sepia":
                c = ramp_pick(SEPIA, 0.08 + 0.85 * l - 0.04 * (H.noise(x, y, 5) > 0.7))
            elif name == "opal":
                if l < 0.22 and face:
                    continue
                pr, pg, pb = ramp_pick(PEARL, 0.3 + 0.65 * l)
                hr, hg, hb = colorsys.hsv_to_rgb((xx * 0.035 + yy * 0.025 + i * 0.13 + H.noise(x // 4, y // 4, 31) * 0.4) % 1.0, 0.55, 1.0)
                c = (int(pr * 0.72 + hr * 255 * 0.28), int(pg * 0.72 + hg * 255 * 0.28), int(pb * 0.72 + hb * 255 * 0.28))
            elif name == "chrome":
                if l < 0.22 and face:
                    continue
                band = abs(yy - fh * 0.35) < max(0.6, fh * 0.08)            # the bright reflection across every face
                t = 0.1 + 0.8 * (l * l * (3 - 2 * l)) + (0.35 if band else 0) - 0.15 * (yy > fh * 0.75)
                c = ramp_pick(CHROME, min(1.0, t))
            elif name == "aurora":
                if l < 0.22 and face:
                    continue
                wave = fh * (0.45 + 0.2 * math.sin(xx * 0.18 + i * 1.3)) - yy
                if abs(wave) < max(0.7, fh * 0.1) and fh > 2:
                    c = ramp_pick(AURORA, (yy / max(1, fh - 1) + 0.15 * math.sin(i)) % 1.0)
                    c = tuple(int(v * (0.6 + 0.4 * l)) for v in c)
                else:
                    c = ramp_pick(NIGHTBODY, 0.2 + 0.6 * l)
            elif name == "plush":
                if l < 0.22 and face:
                    continue
                c = tuple(int(v + (255 - v) * 0.4) for v in (r, g, b))           # soft pastel felt
                c = tuple(max(0, min(255, int(v * (0.94 + 0.08 * H.noise(x, y, 67))))) for v in c)
                if edge and (xx + yy) % 2 == 0:
                    c = tuple(int(v * 0.6) for v in c)                       # the stitched seam
            elif name == "pixel":
                if l < 0.22 and face:
                    continue
                sx, sy = x - xx % 2, y - yy % 2                              # chunky pixels: every 2x2 takes its top-left colour
                pr, pg, pb, pa = img[sy][sx] if 0 <= sy < Hh and 0 <= sx < W and img[sy][sx][3] else (r, g, b, a)
                c = tuple(min(255, int(round(v / 48.0) * 48)) for v in (pr, pg, pb))   # a few levels per channel, like old consoles
            elif name == "origami":
                if l < 0.22 and face:
                    continue
                hh, ss, vv = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
                base = colorsys.hsv_to_rgb(hh, ss * 0.45, 0.88)
                fold = (xx * fh - yy * fw) > 0                               # the diagonal crease across each face
                k = 1.0 if fold else 0.8
                c = tuple(int(v * 255 * k) for v in base)
                if abs(xx * fh - yy * fw) < max(fw, fh) * 0.6:
                    c = tuple(int(v * 0.85) for v in c)                      # the crease itself
            elif name == "gummy":
                if l < 0.22 and face:
                    continue
                hh, ss, vv = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
                if ss < 0.3:
                    hh = (__import__("zlib").crc32(model.encode()) % 360) / 360.0   # greys and skin: one candy colour per model
                cr, cg, cb = colorsys.hsv_to_rgb(hh, min(1.0, max(ss, 0.3) * 1.4 + 0.25), min(1.0, 0.55 + 0.45 * vv))
                c = (int(cr * 255), int(cg * 255), int(cb * 255))
                if (xx < 2 and yy < 3) or (xx == yy and xx < 4):
                    c = tuple(int(v + (255 - v) * 0.7) for v in c)          # a glossy highlight
                out[y][x] = c + (205,)
                continue
            elif name == "camo":
                if l < 0.22 and face:
                    continue
                k = int(H.noise(x // 4, y // 4, 37) * 4) % 4
                base = CAMO[k]
                c = tuple(int(v * (0.65 + 0.5 * l)) for v in base)
            elif name == "synthwave":
                if l < 0.22 and face:
                    continue
                if fh > 5 and fw > 5 and (xx % 7 == 3 or yy % 5 == 4) and yy > fh * 0.6:
                    c = (60, 230, 255)                                      # the neon grid on the lower half of each face
                else:
                    t = min(1.0, yy / max(1, fh - 1))                       # sunset: pink at the top into violet
                    top, bot = (255, 90, 190), (70, 30, 120)
                    c = tuple(int((a0 * (1 - t) + b0 * t) * (0.55 + 0.45 * l)) for a0, b0 in zip(top, bot))
            elif name == "cookie":
                if l < 0.22 and face:
                    continue
                c = ramp_pick(DOUGH, 0.3 + 0.6 * l)
                if edge:
                    c = tuple(int(v * 0.78) for v in c)                     # browned at the edges
                if not face and fw > 2 and fh > 2 and H.noise(x // 2, y // 2, 71) > 0.9:
                    c = (62, 36, 24)                                        # a chocolate chip
            elif name == "cloud":
                if l < 0.22 and face:
                    continue
                shade = min(1.0, yy / max(1, fh - 1)) * 0.5 + (0.25 - 0.25 * H.noise(x // 3, y // 3, 73))
                c = tuple(int(w * (1 - shade) + s * shade) for w, s in zip((252, 252, 255), (168, 196, 232)))
            elif name == "valentine":
                if l < 0.22 and face:
                    continue
                c = ramp_pick(ROSE, 0.3 + 0.6 * l)
                hx_, hy_ = (xx + 3 * i) % 12, (yy + 2 * i) % 10                  # little hearts scattered over every face
                if fw > 4 and fh > 4 and (hy_, hx_) in ((0, 1), (0, 3), (1, 0), (1, 1), (1, 2), (1, 3), (1, 4), (2, 1), (2, 2), (2, 3), (3, 2)):
                    c = (200, 20, 60) if not hair else (255, 240, 246)
            elif name == "porcelain":
                if l < 0.22 and face:
                    continue
                c = tuple(int(v * (0.86 + 0.14 * l)) for v in (242, 246, 252))
                if fw > 3 and fh > 3 and (edge or (xx * 3 + yy * 2 + i) % 9 == 0 or (yy == fh - 2 and xx % 2 == 0)):
                    c = (44, 76, 168)                                       # a cobalt rim, the blue painted motifs, a band near each hem
            elif name == "denim":
                if l < 0.22 and face:
                    continue
                c = ramp_pick(DENIM, 0.25 + 0.6 * l - 0.08 * ((xx + yy) % 3 == 0))
                if edge and fw > 2 and fh > 2 and (xx + yy) % 2 == 0:
                    c = (222, 150, 62)                                      # the orange stitching along every seam
            elif name == "moss":
                if l < 0.22 and face:
                    continue
                mossy = yy < 2 or H.noise(x // 3, y // 3, 91) > 0.55
                c = ramp_pick(MOSS if mossy else STONE, 0.25 + 0.6 * l)
            elif name == "easter":
                if l < 0.22 and face:
                    continue
                band = (yy + abs(xx % 4 - 2)) // 5                          # wavy pastel bands, like a dyed egg
                if (yy + abs(xx % 4 - 2)) % 5 == 0:
                    c = (250, 250, 246)
                else:
                    pr, pg, pb = PASTEL[(band + i) % len(PASTEL)]
                    c = (int(pr * (0.85 + 0.15 * l)), int(pg * (0.85 + 0.15 * l)), int(pb * (0.85 + 0.15 * l)))
            else:                                                       # candy cane: red and white stripes, round and round
                if l < 0.22 and face:
                    continue
                red = ((xx + yy) // 5) % 2 == 0 and not face
                c = (int(200 + 40 * l), int(30 + 30 * l), int(40 + 30 * l)) if red else (int(236 + 19 * l), int(232 + 19 * l), int(232 + 19 * l))
            out[y][x] = c + (a,)
    return out, mask


def models():
    ids = re.findall(r'\("([a-z_]+)", Family\.', open(JAVA, encoding="utf-8").read())
    out = []
    for sp in ids:
        for m in (sp, sp + "_male"):
            if os.path.exists(os.path.join(GEO, m + ".geo.json")) and os.path.exists(os.path.join(ENT, m + ".png")):
                out.append(m)
    for sp in OTHERS:
        for m in (sp, sp + "_hatchling", sp + "_tadpole", sp + "_froglet", sp + "_detailed"):
            if os.path.exists(os.path.join(ENT, m + ".png")):
                out.append(m)
    return out


def main():
    import shutil
    every = ["blue", "red", "green", "yellow", "purple", "black", "inverted"] + TREATS
    only = [a for a in sys.argv[1:] if a in every]                   # optional: just these paints
    names = [a for a in sys.argv[1:] if a not in every]              # optional: just these models
    ms = [m for m in models() if not names or m in names]
    for m in ms:
        img = load(os.path.join(ENT, m + ".png"))
        for p in [p for p in ["blue", "red", "green", "yellow", "purple", "black", "inverted"] + TREATS if not only or p in only]:
            glow = os.path.join(ENT, m + "_glowmask.png")
            if p in ("glow", "magma"):                             # her own glowing markings (the species' own glow on top)
                tex, mask = finish(img, p, m)
                if os.path.exists(glow):
                    base = load(glow)
                    mask = [[mk if mk[3] else bs for mk, bs in zip(mrow, brow)] for mrow, brow in zip(mask, base)]
                H.write_png(os.path.join(ENT, f"{m}_paint_{p}.png"), tex)
                H.write_png(os.path.join(ENT, f"{m}_paint_{p}_glowmask.png"), mask)
                continue
            H.write_png(os.path.join(ENT, f"{m}_paint_{p}.png"), treat(img, p, m) if p in TREATS else paint(img, p))
            if os.path.exists(glow):                               # the glow layer looks for <texture>_glowmask.png
                shutil.copyfile(glow, os.path.join(ENT, f"{m}_paint_{p}_glowmask.png"))
    print(f"{len(ms)} models x {7 + len(TREATS)} paints")


if __name__ == "__main__":
    main()
