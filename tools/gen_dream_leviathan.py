#!/usr/bin/env python3
"""Assets, data and lang for the Dream-Leviathan event (leviathan/*, client/DreamLeviathanModel and friends).

    uv run --no-project --with pillow python tools/gen_dream_leviathan.py

Paints: the whale's skins on client/DreamLeviathanModel's box layout (keep BOXES in step with it) - healthy, three stages of
sickness, dead, rotting, bones, her calf, and the glow layers (stars, galaxies, constellations, runes, eye); the blocks of her
insides; the horn, scale, antibody and egg; the parasites' skins on the vanilla zombie, skeleton and creeper layouts plus their
growths; the blessing's heart sprites. Writes the interior dimension, biome, damage type, recipe and advancements, and adds lang
lines (only missing keys). Look reference: previews/leviathan-whale.webp. Can be run twice.
"""
import json
import math
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets  # noqa: E402
from PIL import Image  # noqa: E402

ASSETS = gen_milk_assets.ASSETS
DATA = gen_milk_assets.DATA
M = "message.harmontide.leviathan."
G = "journal.harmontide.guide."


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def save(img, *rel):
    path = os.path.join(ASSETS, "textures", *rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def clamp(v):
    return max(0, min(255, int(v)))


def mix(a, b, t):
    return tuple(clamp(a[i] + (b[i] - a[i]) * t) for i in range(3))


def hnoise(x, y, seed):
    """A hash noise in 0..1."""
    n = (x * 374761393 + y * 668265263 + seed * 982451653) & 0xFFFFFFFF
    n = (n ^ (n >> 13)) * 1274126177 & 0xFFFFFFFF
    return ((n ^ (n >> 16)) & 0xFFFF) / 65535.0


def vnoise(x, y, scale, seed):
    """Smooth value noise in 0..1."""
    fx, fy = x / scale, y / scale
    ix, iy = math.floor(fx), math.floor(fy)
    tx, ty = fx - ix, fy - iy
    tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
    a, b = hnoise(ix, iy, seed), hnoise(ix + 1, iy, seed)
    c, d = hnoise(ix, iy + 1, seed), hnoise(ix + 1, iy + 1, seed)
    return a + (b - a) * tx + (c - a) * ty + (a - b - c + d) * tx * ty


# --- the whale -----------------------------------------------------------------------------------------------------------------

# name: (u, v, w, h, d) as in client/DreamLeviathanModel.layer()
BOXES = {
    "body": (0, 0, 24, 20, 40),
    "head": (128, 0, 22, 16, 30),
    "jaw": (0, 60, 20, 6, 28),
    "tail1": (96, 60, 18, 15, 22),
    "tail2": (176, 60, 12, 10, 20),
    "tail3": (0, 97, 7, 6, 16),
    "fluke": (46, 97, 40, 2, 14),
    "fin_l": (0, 120, 30, 2, 9),
    "fin_r": (80, 120, 30, 2, 9),
    "dorsal": (160, 120, 3, 5, 6),
}


def faces(u, v, w, h, d):
    """Box UV faces (as ModelPart lays them out): name -> (x, y, width, height). 'top' is the face seen from above in game."""
    return {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "west": (u, v + d, d, h), "front": (u + d, v + d, w, h), "east": (u + d + w, v + d, d, h), "back": (u + d + w + d, v + d, w, h),
    }


BACK = [(20, 12, 44), (30, 20, 62), (42, 28, 86), (58, 40, 112), (78, 56, 142)]
BELLY = [(84, 72, 128), (112, 100, 160), (140, 128, 188), (170, 160, 210)]
VIOLET, CYAN, PINK, GOLD, STAR = (160, 96, 230), (110, 200, 255), (232, 140, 220), (255, 222, 150), (236, 240, 255)


def ramp(cols, t):
    t = max(0.0, min(0.999, t))
    f = t * (len(cols) - 1)
    i = int(f)
    return mix(cols[i], cols[i + 1], f - i)


def whale(calf=False):
    """Paints the healthy skin and its glow layer. Returns (skin, glow)."""
    rnd = random.Random(77 if not calf else 78)
    skin = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    glow = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    sp, gp = skin.load(), glow.load()
    back = BACK if not calf else [mix(c, (120, 100, 200), 0.3) for c in BACK]
    for part, (u, v, w, h, d) in BOXES.items():
        for fname, (fx, fy, fw, fh) in faces(u, v, w, h, d).items():
            for y in range(fh):
                for x in range(fw):
                    X, Y = fx + x, fy + y
                    n = vnoise(X, Y, 6, 3) * 0.5 + vnoise(X, Y, 2.5, 4) * 0.25
                    if fname == "top":
                        c = ramp(back, 0.25 + n * 0.4)
                    elif fname == "bottom":
                        groove = part in ("jaw", "head", "body") and (x % 3 == 0)
                        c = ramp(BELLY, 0.35 + n * 0.3 - (0.25 if groove else 0))
                    else:                                                           # sides: dark back fading to the pale belly
                        t = y / max(1, fh - 1)
                        if part in ("fin_l", "fin_r", "fluke", "dorsal"):
                            c = ramp(back, 0.35 + n * 0.4)
                        elif t < 0.62:
                            c = ramp(back, 0.2 + n * 0.45 + t * 0.2)
                        else:
                            groove = part in ("jaw", "head") and (y % 2 == 0) and t > 0.7
                            c = mix(ramp(back, 0.5 + n * 0.3), ramp(BELLY, 0.3 + n * 0.4 - (0.2 if groove else 0)), min(1, (t - 0.62) * 3))
                    if part in ("fin_l", "fin_r", "fluke"):                          # nebula colour toward the tips and edges
                        tip = x / max(1, fw - 1) if part == "fin_l" else 1 - x / max(1, fw - 1) if part == "fin_r" else abs(x / max(1, fw - 1) - 0.5) * 2
                        neb = ramp([VIOLET, CYAN, PINK], vnoise(X, Y, 5, 9))
                        c = mix(c, neb, max(0, tip - 0.35) * 0.9)
                        if fname in ("top", "bottom") and (y == 0 or y == fh - 1 or tip > 0.93):
                            gp[X, Y] = neb + (200,)
                    sp[X, Y] = c + (255,)
                    if hnoise(X, Y, 11) > (0.965 if not calf else 0.955):             # stars
                        bright = hnoise(X, Y, 12)
                        sp[X, Y] = mix(c, STAR, 0.5) + (255,)
                        gp[X, Y] = mix(STAR, CYAN if bright > 0.7 else GOLD if bright < 0.2 else STAR, 0.5) + (int(140 + 115 * bright),)
            if part in ("body", "tail1", "head") and fname in ("west", "east", "top"):
                spirals = 1 if part != "body" else 2
                for k in range(spirals):
                    cx = fx + fw * (0.3 + 0.4 * rnd.random())
                    cy = fy + fh * (0.25 + 0.3 * rnd.random())
                    rad = min(fw, fh) * (0.22 + 0.1 * rnd.random())
                    galaxy(sp, gp, cx, cy, rad, (fx, fy, fw, fh), rnd)
            if fname in ("west", "east", "top") and part in ("body", "tail1", "tail2", "head"):
                constellation(gp, (fx, fy, fw, fh), rnd, 2 if part == "body" else 1)
            if part == "head" and fname in ("west", "east"):
                runes(gp, (fx, fy, fw, fh), rnd)
                # the eye: low on the side, toward the front (west runs back->front, east front->back)
                ex = fx + (fw - 6 if fname == "west" else 5)
                ey = fy + fh - 7
                for dy in range(-1, 2):
                    for dx in range(-2, 2):
                        sp[ex + dx, ey + dy] = (18, 14, 34, 255)
                for dx in range(-1, 1):
                    sp[ex + dx, ey] = (150, 210, 255, 255)
                    gp[ex + dx, ey] = (170, 225, 255, 255)
                gp[ex - 1, ey - 1] = (230, 245, 255, 255)
    return skin, glow


def galaxy(sp, gp, cx, cy, rad, box, rnd):
    fx, fy, fw, fh = box
    arms = rnd.choice([2, 3])
    twist = 0.9 + rnd.random() * 0.5
    col = rnd.choice([VIOLET, CYAN, PINK])
    for y in range(int(cy - rad - 1), int(cy + rad + 2)):
        for x in range(int(cx - rad - 1), int(cx + rad + 2)):
            if not (fx <= x < fx + fw and fy <= y < fy + fh):
                continue
            dx, dy = x - cx, y - cy
            r = math.hypot(dx, dy) / rad
            if r > 1:
                continue
            th = math.atan2(dy, dx)
            arm = math.cos(arms * th - r * twist * 6)
            b = max(0, arm) ** 3 * (1 - r) + max(0, 0.35 - r) * 2.2
            if b < 0.08:
                continue
            base = sp[x, y][:3]
            c = mix(col, STAR, max(0, 0.6 - r))
            sp[x, y] = mix(base, c, min(1, b * 0.9)) + (255,)
            gp[x, y] = c + (clamp(255 * min(1, b * 1.1)),)


def line(gp, a, b, col):
    (x0, y0), (x1, y1) = a, b
    n = max(abs(x1 - x0), abs(y1 - y0))
    for i in range(n + 1):
        x = round(x0 + (x1 - x0) * i / max(1, n))
        y = round(y0 + (y1 - y0) * i / max(1, n))
        if gp[x, y][3] < 100:
            gp[x, y] = col + (110,)


def constellation(gp, box, rnd, count):
    fx, fy, fw, fh = box
    for _ in range(count):
        pts = []
        x, y = fx + rnd.randrange(2, max(3, fw - 2)), fy + rnd.randrange(1, max(2, int(fh * 0.6)))
        for _ in range(rnd.randrange(4, 7)):
            pts.append((x, y))
            x = max(fx + 1, min(fx + fw - 2, x + rnd.randrange(-7, 8)))
            y = max(fy + 1, min(fy + fh - 2, y + rnd.randrange(-4, 5)))
        for a, b in zip(pts, pts[1:]):
            line(gp, a, b, mix(GOLD, STAR, 0.4))
        for p in pts:
            gp[p] = STAR + (255,)


def runes(gp, box, rnd):
    fx, fy, fw, fh = box
    for _ in range(3):
        cx, cy = fx + rnd.randrange(4, fw - 4), fy + rnd.randrange(2, max(3, fh // 2))
        r = rnd.choice([2, 3])
        for a in range(0, 360, 20):
            x, y = round(cx + math.cos(math.radians(a)) * r), round(cy + math.sin(math.radians(a)) * r * 0.8)
            if fx <= x < fx + fw and fy <= y < fy + fh:
                gp[x, y] = GOLD + (150,)
        gp[cx, cy] = GOLD + (220,)


def sicken(skin, glow, level):
    """A sick copy: duller, greyer, with blotches of infection and pustules; the glow dims and loses stars."""
    s, g = skin.copy(), glow.copy()
    sp, gp = s.load(), g.load()
    amount = {1: 0.25, 2: 0.5, 3: 0.8}[level]
    for y in range(256):
        for x in range(256):
            r, gg, b, a = sp[x, y]
            if a == 0:
                continue
            grey = (r + gg + b) / 3
            c = mix((r, gg, b), (grey * 0.9, grey * 0.95, grey * 0.8), amount * 0.7)
            blot = vnoise(x, y, 7, 21) + vnoise(x, y, 3, 22) * 0.4
            if blot > 1.15 - amount * 0.45:                                          # infection patches
                c = mix(c, (96, 110, 62), 0.6)
                if hnoise(x, y, 23) > 0.93:
                    c = (190, 200, 90)
                elif vnoise(x, y, 2, 24) > 0.7:
                    c = mix(c, (60, 40, 30), 0.5)
            sp[x, y] = c + (255,)
            ga = gp[x, y][3]
            if ga:
                keep = hnoise(x, y, 25) > amount * 0.7
                gp[x, y] = gp[x, y][:3] + ((clamp(ga * (1 - amount * 0.5)) if keep else 0),)
    return s, g


def dead_skin(skin):
    s = skin.copy()
    sp = s.load()
    for y in range(256):
        for x in range(256):
            r, g, b, a = sp[x, y]
            if a == 0:
                continue
            grey = (r + g + b) / 3
            c = mix((r, g, b), (grey * 0.85 + 20, grey * 0.8 + 18, grey * 0.9 + 26), 0.85)
            if vnoise(x, y, 6, 31) > 0.72:
                c = mix(c, (70, 74, 60), 0.5)
            sp[x, y] = c + (255,)
    return s


def rot_skin(dead):
    s = dead.copy()
    sp = s.load()
    for y in range(256):
        for x in range(256):
            r, g, b, a = sp[x, y]
            if a == 0:
                continue
            n = vnoise(x, y, 5, 41) + vnoise(x, y, 2, 42) * 0.3
            c = (r, g, b)
            if n > 1.0:
                c = mix((150, 50, 60), (90, 20, 30), vnoise(x, y, 2, 43))           # torn open, raw flesh
            elif n > 0.92:
                c = (226, 214, 186)                                                  # bone showing through
            elif n > 0.85:
                c = mix(c, (40, 30, 26), 0.7)
            sp[x, y] = c + (255,)
    return s


def bones_skin():
    """Only the bones left: ribs and spine on the body and tail, the skull, finger bones in the fins; the rest see-through."""
    s = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    sp = s.load()
    bone = lambda x, y: mix((200, 190, 160), (236, 228, 204), vnoise(x, y, 3, 51)) + (255,)
    for part, (u, v, w, h, d) in BOXES.items():
        for fname, (fx, fy, fw, fh) in faces(u, v, w, h, d).items():
            for y in range(fh):
                for x in range(fw):
                    X, Y = fx + x, fy + y
                    keep = False
                    if part in ("head", "jaw"):
                        keep = vnoise(X, Y, 3, 52) > 0.3
                    elif part in ("body", "tail1", "tail2", "tail3"):
                        if fname in ("west", "east"):
                            keep = (x % 5) < 2 or y < 2
                        elif fname in ("top", "bottom"):
                            keep = abs(x - fw / 2) < 2 or (y % 5) < 2 and fname == "top"
                        else:
                            keep = abs(x - fw / 2) < 2 and abs(y - fh / 2) < 2
                    elif part in ("fin_l", "fin_r", "fluke"):
                        keep = fname in ("top", "bottom") and (y % 3 == 1)
                    if keep:
                        sp[X, Y] = bone(X, Y)
    return s


def whale_textures():
    skin, glow = whale()
    save(skin, "entity", "dream_leviathan", "healthy.png")
    save(glow, "entity", "dream_leviathan", "healthy_glow.png")
    s1, _ = sicken(skin, glow, 1)
    s2, _ = sicken(skin, glow, 2)
    s3, g3 = sicken(skin, glow, 3)
    save(s1, "entity", "dream_leviathan", "sick_1.png")
    save(s2, "entity", "dream_leviathan", "sick_2.png")
    save(s3, "entity", "dream_leviathan", "sick_3.png")
    save(g3, "entity", "dream_leviathan", "sick_glow.png")
    dead = dead_skin(s3)
    save(dead, "entity", "dream_leviathan", "dead.png")
    save(rot_skin(dead), "entity", "dream_leviathan", "rot.png")
    save(bones_skin(), "entity", "dream_leviathan", "bones.png")
    calf, calf_glow = whale(calf=True)
    save(calf, "entity", "dream_leviathan", "calf.png")
    save(calf_glow, "entity", "dream_leviathan", "calf_glow.png")


# --- blocks of her insides --------------------------------------------------------------------------------------------------------

FLESH = [(96, 22, 36), (130, 36, 50), (164, 58, 70), (196, 92, 100), (222, 140, 140)]
DENSE = [(58, 12, 24), (82, 20, 34), (108, 30, 46), (136, 46, 60)]


def tile(fn, seed=0, size=16):
    img = Image.new("RGBA", (size, size))
    p = img.load()
    for y in range(size):
        for x in range(size):
            p[x, y] = fn(x, y)
    return img


def flesh_px(x, y, seed=1):
    n = vnoise(x, y, 4, seed) * 0.6 + vnoise(x, y, 1.7, seed + 1) * 0.4
    c = ramp(FLESH, 0.15 + n * 0.75)
    if vnoise(x, y, 3, seed + 2) > 0.78:
        c = mix(c, (250, 190, 190), 0.35)                                            # a wet sheen
    if abs(vnoise(x + 3, y, 5, seed + 3) - 0.5) < 0.04:
        c = mix(c, (70, 14, 28), 0.6)                                                # a fold
    return c + (255,)


def dense_px(x, y):
    n = vnoise(x, y * 0.35, 2.5, 7) * 0.7 + vnoise(x, y, 1.5, 8) * 0.3
    return ramp(DENSE, n) + (255,)


def vein_px(x, y):
    c = ramp(DENSE, vnoise(x, y, 3, 9) * 0.7)
    v = abs(math.sin(x * 0.7 + math.sin(y * 0.5) * 2.2)) < 0.22 or abs(math.sin(y * 0.6 + math.cos(x * 0.45) * 2)) < 0.16
    if v:
        c = mix((120, 70, 220), (190, 150, 255), vnoise(x, y, 2, 10))
    return c + (255,)


def baleen_px(x, y):
    if x % 4 == 3:
        return (52, 40, 40, 255)
    return mix((200, 186, 150), (238, 226, 196), vnoise(x, y * 0.25, 2, 11) * 0.8 + (0.2 if x % 4 == 0 else 0)) + (255,)


def membrane_px(open_):
    def px(x, y):
        dx, dy = x - 7.5, y - 7.5
        r = math.hypot(dx, dy)
        th = math.atan2(dy, dx)
        fold = math.cos(th * 7 + r * 0.4)
        if open_:
            if r < 6.0:
                return (0, 0, 0, 0)
            return mix((170, 70, 100), (230, 140, 170), (fold + 1) / 2) + (150,)
        c = mix((150, 50, 80), (240, 150, 180), (fold + 1) / 2 * (1 - r / 11))
        if r < 1.2:
            c = (70, 16, 34)
        return c + (220,)
    return px


def nerve_px(lit):
    def px(x, y):
        dx, dy = x - 7.5, y - 7.5
        r = math.hypot(dx, dy)
        base = flesh_px(x, y, 13)[:3]
        fibre = abs(math.sin(math.atan2(dy, dx) * 5 + r * 0.6)) < 0.18 and r < 7.5
        if r < 4.2:
            c = mix((255, 250, 200), (255, 210, 90), r / 4.2) if lit else mix((170, 150, 100), (120, 100, 70), r / 4.2)
        elif fibre:
            c = (255, 230, 140) if lit else (150, 130, 90)
        else:
            c = base
        return c + (255,)
    return px


def acid_px(x, y):
    n = vnoise(x, y, 4, 14)
    c = mix((120, 160, 40), (210, 230, 90), n)
    if hnoise(x, y, 15) > 0.9:
        c = (236, 250, 170)
    return c + (200,)


def cilia_px(down):
    def px(x, y):
        if down:
            return (ramp(FLESH, 0.5 + 0.3 * vnoise(x, y, 2, 16)) + (255,)) if (x + y) % 3 else (90, 20, 34, 255)
        strand = (x * 3 + (y // 2)) % 4 != 0
        c = mix((210, 120, 150), (250, 190, 210), y / 15) if strand else (120, 40, 64)
        return c + (255,)
    return px


def pulse_px(open_):
    def px(x, y):
        if open_:
            if 2 <= x <= 13 and 2 <= y <= 13:
                return (0, 0, 0, 0)
            return mix((130, 30, 40), (190, 70, 80), vnoise(x, y, 2, 17)) + (140,)
        stripe = (y + (x // 4)) % 3 == 0
        return (mix((150, 30, 44), (200, 64, 74), vnoise(x * 0.3, y, 2, 18)) if not stripe else (100, 18, 30)) + (255,)
    return px


def valve_px(x, y):
    r = math.hypot(x - 7.5, y - 7.5)
    if r < 2.5:
        return (250, 120, 220, 255)
    if r < 4.5:
        return mix((140, 40, 120), (200, 80, 170), (math.sin(math.atan2(y - 7.5, x - 7.5) * 6) + 1) / 2) + (255,)
    if r < 5.5:
        return (80, 16, 50, 255)
    return flesh_px(x, y, 19)


def cyst_px(cured):
    def px(x, y):
        n = vnoise(x, y, 3, 20)
        lump = vnoise(x, y, 2.2, 21) > 0.55
        if cured:
            c = mix((214, 150, 150), (240, 200, 196), n)
        else:
            c = mix((90, 130, 40), (170, 210, 70), n)
            if lump and hnoise(x, y, 22) > 0.6:
                c = (226, 240, 130)
        if not lump:
            c = mix(c, (60, 20, 30), 0.35)
        return c + (255,)
    return px


def gland_px(x, y):
    r = math.hypot(x - 7.5, y - 7.5)
    c = mix((140, 210, 255), (60, 110, 200), min(1, r / 8))
    if hnoise(x, y, 23) > 0.92:
        c = (236, 250, 255)
    return c + (255,)


def heart_px(x, y):
    n = vnoise(x, y, 3, 24)
    c = mix((120, 20, 90), (220, 70, 170), n)
    if abs(math.sin(x * 0.8 + math.sin(y * 0.7) * 2)) < 0.15:
        c = (250, 170, 240)
    if hnoise(x, y, 25) > 0.95:
        c = (255, 240, 255)
    return c + (255,)


def exit_px(x, y):
    r = math.hypot(x - 7.5, y - 7.5)
    if r < 3.5:
        return (24, 6, 14, 255)
    if r < 5.5:
        return mix((210, 110, 140), (150, 50, 80), (r - 3.5) / 2) + (255,)
    return flesh_px(x, y, 26)


BLOCKS = {}


def block(name, px, render=None):
    BLOCKS[name] = (px, render)


def blocks():
    block("leviathan_flesh", flesh_px)
    block("leviathan_dense_flesh", dense_px)
    block("leviathan_vein", vein_px)
    block("leviathan_baleen", baleen_px)
    block("leviathan_valve", valve_px)
    block("leviathan_gland", gland_px)
    block("leviathan_heart", heart_px)
    block("leviathan_exit", exit_px)
    for name, (px, render) in BLOCKS.items():
        save(tile(px), "block", name + ".png")
        model = {"parent": "minecraft:block/cube_all", "textures": {"all": "harmontide:block/" + name}}
        if render:
            model["render_type"] = render
        write_json(os.path.join(ASSETS, "models", "block", name + ".json"), model)
        write_json(os.path.join(ASSETS, "blockstates", name + ".json"), {"variants": {"": {"model": "harmontide:block/" + name}}})
    two_state("leviathan_membrane", "open", membrane_px(False), membrane_px(True), "minecraft:translucent")
    two_state("leviathan_nerve_node", "lit", nerve_px(False), nerve_px(True), None)
    two_state("leviathan_cyst", "cured", cyst_px(False), cyst_px(True), None)
    two_state("leviathan_pulse_wall", "open", pulse_px(False), pulse_px(True), "minecraft:translucent")
    # stomach acid: one translucent texture
    save(tile(acid_px), "block", "leviathan_acid.png")
    write_json(os.path.join(ASSETS, "models", "block", "leviathan_acid.json"),
               {"parent": "minecraft:block/cube_all", "render_type": "minecraft:translucent", "textures": {"all": "harmontide:block/leviathan_acid"}})
    write_json(os.path.join(ASSETS, "blockstates", "leviathan_acid.json"), {"variants": {"": {"model": "harmontide:block/leviathan_acid"}}})
    # cilia: standing (a cube of hair) or folded (a low mat)
    save(tile(cilia_px(False)), "block", "leviathan_cilia.png")
    save(tile(cilia_px(True)), "block", "leviathan_cilia_folded.png")
    write_json(os.path.join(ASSETS, "models", "block", "leviathan_cilia.json"),
               {"parent": "minecraft:block/cube_all", "render_type": "minecraft:cutout", "textures": {"all": "harmontide:block/leviathan_cilia"}})
    write_json(os.path.join(ASSETS, "models", "block", "leviathan_cilia_folded.json"), {
        "parent": "minecraft:block/block", "textures": {"all": "harmontide:block/leviathan_cilia_folded", "particle": "harmontide:block/leviathan_cilia_folded"},
        "elements": [{"from": [0, 0, 0], "to": [16, 2, 16], "faces": {
            f: {"texture": "#all", "cullface": f} if f == "down" else {"texture": "#all"} for f in ("down", "up", "north", "south", "west", "east")}}]})
    write_json(os.path.join(ASSETS, "blockstates", "leviathan_cilia.json"), {"variants": {
        "retracted=false": {"model": "harmontide:block/leviathan_cilia"}, "retracted=true": {"model": "harmontide:block/leviathan_cilia_folded"}}})


def two_state(name, prop, off, on, render):
    save(tile(off), "block", name + ".png")
    save(tile(on), "block", name + "_" + prop + ".png")
    for suffix in ("", "_" + prop):
        model = {"parent": "minecraft:block/cube_all", "textures": {"all": "harmontide:block/" + name + suffix}}
        if render:
            model["render_type"] = render
        write_json(os.path.join(ASSETS, "models", "block", name + suffix + ".json"), model)
    write_json(os.path.join(ASSETS, "blockstates", name + ".json"), {"variants": {
        prop + "=false": {"model": "harmontide:block/" + name}, prop + "=true": {"model": "harmontide:block/" + name + "_" + prop}}})


# --- items -----------------------------------------------------------------------------------------------------------------

def item_img(fn):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    fn(img.load())
    return img


def horn(p):
    """A goat horn curling up to the right, banded with starry violet."""
    for i in range(26):
        t = i / 25
        x = 2 + t * 11
        y = 13 - math.sin(t * 2.2) * 9
        w = 2.6 - t * 1.8
        for oy in range(-2, 3):
            for ox in range(-2, 3):
                if ox * ox + oy * oy <= w * w:
                    X, Y = round(x + ox), round(y + oy)
                    if 0 <= X < 16 and 0 <= Y < 16:
                        band = int(t * 8) % 2 == 0
                        p[X, Y] = (mix((70, 50, 120), (150, 110, 220), t) if band else mix((190, 180, 160), (220, 210, 190), t)) + (255,)
    for X, Y in ((5, 11), (9, 7), (12, 4)):
        p[X, Y] = (240, 240, 255, 255)
    p[1, 14] = p[2, 14] = (40, 30, 70, 255)


def scale_item(p):
    for y in range(16):
        for x in range(16):
            dx, dy = (x - 7.5) / 6.5, (y - 8.5) / 7
            if dx * dx + dy * dy <= 1 and y >= 2:
                t = (x + y) / 30
                c = ramp([(60, 40, 120), VIOLET, CYAN, PINK], t)
                if abs(dx) < 0.12:
                    c = mix(c, (255, 255, 255), 0.3)
                if (x + y) % 7 == 0:
                    c = mix(c, STAR, 0.5)
                p[x, y] = c + (255,)


def antibody(p):
    for y in range(16):
        for x in range(16):
            dx, dy = (x - 7.5) / 4.5, (y - 9.0) / 5.5
            d = dx * dx + dy * dy
            if d <= 1 or (abs(x - 7.5) < 1.6 and 2 <= y <= 4):
                c = mix((200, 240, 255), (70, 140, 230), d)
                if x == 6 and y in (7, 8):
                    c = (250, 255, 255)
                p[x, y] = c + (255,)


def egg(p):
    for y in range(16):
        for x in range(16):
            dx, dy = (x - 7.5) / 5.5, (y - 8.5) / 7 + (0.12 if y < 8 else 0)
            if dx * dx + dy * dy <= 1:
                c = ramp(BACK[1:], 0.3 + vnoise(x, y, 3, 61) * 0.5 - dy * 0.2)
                if hnoise(x, y, 62) > 0.86:
                    c = STAR
                elif abs(math.sin(math.atan2(y - 8, x - 7.5) * 2 - math.hypot(x - 7.5, y - 8) * 0.9)) < 0.2:
                    c = mix(c, VIOLET, 0.6)
                p[x, y] = c + (255,)


def items():
    for name, fn in (("dream_horn", horn), ("dream_scale", scale_item), ("antibody", antibody), ("dream_leviathan_egg", egg)):
        save(item_img(fn), "item", name + ".png")
        write_json(os.path.join(ASSETS, "models", "item", name + ".json"),
                   {"parent": "minecraft:item/generated", "textures": {"layer0": "harmontide:item/" + name}})
    for name in ("infested_zombie", "infested_skeleton", "infested_creeper"):
        write_json(os.path.join(ASSETS, "models", "item", name + "_spawn_egg.json"), {"parent": "minecraft:item/template_spawn_egg"})


# --- the parasites -------------------------------------------------------------------------------------------------------------

def box_faces(u, v, w, h, d):
    return faces(u, v, w, h, d)


def paint_box(p, box, fn):
    for fname, (fx, fy, fw, fh) in box_faces(*box).items():
        for y in range(fh):
            for x in range(fw):
                c = fn(fname, x, y, fw, fh, fx + x, fy + y)
                if c is not None:
                    p[fx + x, fy + y] = c


ROT_SKIN = [(52, 70, 50), (74, 96, 66), (96, 120, 84), (120, 140, 104)]
INFECT = [(70, 16, 40), (120, 30, 70), (170, 60, 120), (210, 110, 190)]


def infection(X, Y, seed, c, strength=0.5):
    """Mottles a colour with the parasite's purple-red flesh, pustules and dark sores."""
    n = vnoise(X, Y, 3, seed) + vnoise(X, Y, 1.5, seed + 1) * 0.4
    if n > 1.25 - strength * 0.4:
        c = ramp(INFECT, vnoise(X, Y, 2, seed + 2))
        if hnoise(X, Y, seed + 3) > 0.86:
            c = (220, 230, 120)                                                     # a pustule
    elif n > 1.15 - strength * 0.4:
        c = mix(c, (40, 20, 24), 0.5)
    return c


def face_glow(gp, fx, fy, eyes, mouth=None):
    for (x, y) in eyes:
        gp[fx + x, fy + y] = (200, 120, 255, 255)
    if mouth:
        for (x, y) in mouth:
            gp[fx + x, fy + y] = (150, 70, 200, 160)


def zombie():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    glow = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    p, gp = img.load(), glow.load()

    def skin(f, x, y, w, h, X, Y):
        c = ramp(ROT_SKIN, vnoise(X, Y, 2.5, 71) * 0.8 + 0.1)
        return infection(X, Y, 72, c, 0.6) + (255,)

    def shirt(f, x, y, w, h, X, Y):
        if y >= h - 3 and f != "top":
            return (40, 50, 60, 255)                                                 # belt and trousers top
        torn = vnoise(X, Y, 2, 73) > 0.68
        c = skin(f, x, y, w, h, X, Y)[:3] if torn else mix((30, 70, 80), (50, 100, 110), vnoise(X, Y, 2, 74))
        if f == "front" and 2 <= x <= 5 and 4 <= y <= 7:
            c = mix((150, 30, 70), (220, 90, 160), vnoise(X, Y, 1.2, 75))          # the chest split open
            if (x + y) % 3 == 0:
                c = (236, 220, 190)                                                  # ribs
        return c + (255,)

    def trousers(f, x, y, w, h, X, Y):
        c = mix((40, 30, 80), (64, 50, 110), vnoise(X, Y, 2, 76))
        if y > h - 3:
            c = (36, 30, 30)
        return infection(X, Y, 77, c, 0.35) + (255,)

    paint_box(p, (0, 0, 8, 8, 8), skin)
    paint_box(p, (16, 16, 8, 12, 4), shirt)
    paint_box(p, (40, 16, 4, 12, 4), lambda f, x, y, w, h, X, Y: shirt(f, x, y, w, h, X, Y) if y < 4 and f != "bottom" else skin(f, x, y, w, h, X, Y))
    paint_box(p, (0, 16, 4, 12, 4), trousers)
    # the face (front of the head at 8,8): one eye gone to a socket, three violet parasite eyes, a split jaw
    fx, fy = 8, 8
    for (x, y) in ((1, 3), (2, 3)):
        p[fx + x, fy + y] = (20, 10, 14, 255)
    for (x, y) in ((5, 3), (6, 2), (4, 1)):
        p[fx + x, fy + y] = (190, 110, 250, 255)
    for x in range(1, 7):
        p[fx + x, fy + 6] = (40, 10, 20, 255)
    for x in (2, 4, 6):
        p[fx + x, fy + 6] = (220, 210, 180, 255)
    p[fx + 3, fy + 7] = (120, 20, 50, 255)
    face_glow(gp, fx, fy, ((5, 3), (6, 2), (4, 1)), ((1, 6), (3, 6), (5, 6)))
    for X, Y in ((22, 24), (25, 22), (34, 26), (2, 12)):
        gp[X, Y] = (160, 90, 230, 200)
    return img, glow


def skeleton():
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    glow = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    p, gp = img.load(), glow.load()
    BONE = [(150, 140, 118), (186, 176, 150), (214, 204, 178)]

    def bone(f, x, y, w, h, X, Y):
        c = ramp(BONE, vnoise(X, Y, 2.5, 81) * 0.9)
        if abs(vnoise(X, Y, 4, 82) - 0.5) < 0.05:
            c = mix(c, (140, 30, 60), 0.7)                                           # flesh strands
        return infection(X, Y, 83, c, 0.45) + (255,)

    def ribs(f, x, y, w, h, X, Y):
        if f in ("front", "back"):
            if y in (1, 3, 5, 7) or x in (3, 4):
                return bone(f, x, y, w, h, X, Y)
            if y > 8:
                return bone(f, x, y, w, h, X, Y) if x in (3, 4) else None
            return (mix((120, 30, 70), (180, 70, 140), vnoise(X, Y, 1.5, 84)) + (255,)) if vnoise(X, Y, 2, 85) > 0.45 else None
        return bone(f, x, y, w, h, X, Y)

    paint_box(p, (0, 0, 8, 8, 8), bone)
    paint_box(p, (16, 16, 8, 12, 4), ribs)
    paint_box(p, (40, 16, 2, 12, 2), bone)
    paint_box(p, (0, 16, 2, 12, 2), bone)
    fx, fy = 8, 8
    for (x, y) in ((1, 3), (2, 3), (1, 4), (2, 4), (5, 3), (6, 3), (5, 4), (6, 4)):
        p[fx + x, fy + y] = (16, 10, 16, 255)
    for x in range(1, 7):
        p[fx + x, fy + 6] = (30, 20, 24, 255) if x % 2 else (210, 200, 170, 255)
    face_glow(gp, fx, fy, ((2, 3), (5, 4), (6, 3)))
    for X, Y in ((21, 22), (27, 23), (34, 22)):
        gp[X, Y] = (160, 90, 230, 200)
    return img, glow


def creeper():
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    glow = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    p, gp = img.load(), glow.load()
    GREEN = [(40, 90, 40), (70, 130, 60), (100, 170, 80), (150, 200, 120)]

    def skin(f, x, y, w, h, X, Y):
        c = ramp(GREEN, hnoise(X, Y, 91) * 0.6 + vnoise(X, Y, 2, 92) * 0.3)
        return infection(X, Y, 93, c, 0.7) + (255,)

    paint_box(p, (0, 0, 8, 8, 8), skin)
    paint_box(p, (16, 16, 8, 12, 4), skin)
    paint_box(p, (0, 16, 4, 6, 4), skin)
    fx, fy = 8, 8                                                                    # a lopsided face: three eyes, a torn mouth
    for (x, y) in ((1, 1), (2, 1), (1, 2), (2, 2), (5, 2), (6, 2), (5, 3), (6, 3), (4, 0)):
        p[fx + x, fy + y] = (16, 12, 18, 255)
    for (x, y) in ((3, 4), (4, 4), (2, 5), (3, 5), (4, 5), (5, 5), (2, 6), (5, 6), (3, 7)):
        p[fx + x, fy + y] = (40, 8, 22, 255)
    face_glow(gp, fx, fy, ((1, 2), (6, 3), (4, 0)), ((3, 5), (4, 5)))
    for X, Y in ((21, 25), (26, 21), (30, 27), (3, 22)):
        gp[X, Y] = (160, 90, 230, 200)
    return img, glow


def growth():
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    glow = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    p, gp = img.load(), glow.load()
    for y in range(32):
        for x in range(64):
            c = ramp(INFECT, vnoise(x, y, 2.5, 101) * 0.8 + 0.1)
            if abs(math.sin(x * 0.9 + math.sin(y * 0.6) * 2)) < 0.12:
                c = mix(c, (120, 70, 220), 0.7)                                      # veins
            if hnoise(x, y, 102) > 0.9:
                c = (220, 230, 120)
            p[x, y] = c + (255,)
            if hnoise(x, y, 103) > 0.94:
                gp[x, y] = (190, 120, 255, 230)
    return img, glow


def parasites():
    for name, fn in (("infested_zombie", zombie), ("infested_skeleton", skeleton), ("infested_creeper", creeper), ("growth", growth)):
        img, glow = fn()
        save(img, "entity", "parasite", name + ".png")
        save(glow, "entity", "parasite", name + "_glow.png")


# --- the blessing's hearts -------------------------------------------------------------------------------------------------------

HEART = [
    ".XX...XX.",
    "XXXX.XXXX",
    "XXXXXXXXX",
    "XXXXXXXXX",
    ".XXXXXXX.",
    "..XXXXX..",
    "...XXX...",
    "....X....",
    ".........",
]


def hearts():
    def cell(x, y):
        return 0 <= y < 9 and 0 <= x < 9 and HEART[y][x] == "X"

    for kind in ("container", "full", "half"):
        img = Image.new("RGBA", (9, 9), (0, 0, 0, 0))
        p = img.load()
        for y in range(9):
            for x in range(9):
                if not cell(x, y):
                    continue
                edge = not (cell(x - 1, y) and cell(x + 1, y) and cell(x, y - 1) and cell(x, y + 1))
                if kind == "container":
                    p[x, y] = (16, 8, 30, 255) if edge else (44, 28, 70, 255)
                elif edge:
                    continue
                elif kind == "half" and x > 4:
                    continue
                else:
                    c = mix((210, 150, 255), (110, 60, 200), y / 7)
                    if (x, y) in ((2, 2), (6, 3)):
                        c = (250, 245, 255)
                    p[x, y] = c + (255,)
        save(img, "gui", "sprites", "hud", "dream_heart_" + kind + ".png")


# --- data ----------------------------------------------------------------------------------------------------------------------------

def data():
    write_json(os.path.join(DATA, "dimension_type", "leviathan_interior.json"), {
        "ultrawarm": False, "natural": False, "coordinate_scale": 1.0, "has_skylight": False, "has_ceiling": True, "ambient_light": 0.12,
        "fixed_time": 18000, "monster_spawn_light_level": 0, "monster_spawn_block_light_limit": 0, "piglin_safe": False, "bed_works": False,
        "respawn_anchor_works": False, "has_raids": False, "logical_height": 256, "min_y": 0, "height": 256,
        "infiniburn": "#minecraft:infiniburn_overworld", "effects": "minecraft:the_nether"})
    write_json(os.path.join(DATA, "dimension", "leviathan_interior.json"), {
        "type": "harmontide:leviathan_interior",
        "generator": {"type": "minecraft:flat", "settings": {"biome": "harmontide:leviathan_interior", "layers": [{"block": "minecraft:air", "height": 1}],
                                                             "lakes": False, "features": False, "structure_overrides": []}}})
    write_json(os.path.join(DATA, "worldgen", "biome", "leviathan_interior.json"), {
        "has_precipitation": False, "temperature": 0.8, "downfall": 0.0,
        "effects": {"fog_color": 0x3A0E14, "sky_color": 0, "water_color": 0x3F76E4, "water_fog_color": 0x050533,
                    "ambient_sound": "minecraft:ambient.crimson_forest.loop",
                    "mood_sound": {"sound": "minecraft:ambient.cave", "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0},
                    "particle": {"options": {"type": "minecraft:crimson_spore"}, "probability": 0.008}},
        "spawners": {}, "spawn_costs": {}, "carvers": {}, "features": []})
    write_json(os.path.join(DATA, "damage_type", "stomach_acid.json"), {"message_id": "harmontide.stomach_acid", "scaling": "never", "exhaustion": 0.1})
    write_json(os.path.join(DATA, "recipe", "dream_horn.json"), {
        "type": "minecraft:crafting_shaped", "category": "misc", "pattern": [" S ", "SHS", " S "],
        "key": {"S": {"item": "harmontide:dream_scale"}, "H": {"item": "minecraft:goat_horn"}},
        "result": {"id": "harmontide:dream_horn", "count": 1}})
    for name, icon in ADVANCEMENTS.items():
        write_json(os.path.join(DATA, "advancement", name + ".json"), {
            "parent": "minecraft:adventure/root",
            "display": {"icon": {"id": icon}, "title": {"translate": "advancement.harmontide." + name},
                        "description": {"translate": "advancement.harmontide." + name + ".desc"},
                        "frame": "challenge" if name == "dream_blessing" else "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
            "criteria": {"done": {"trigger": "minecraft:impossible"}}})


ADVANCEMENTS = {"inside_the_dream": "harmontide:dream_scale", "dream_blessing": "harmontide:dream_leviathan_egg"}

LANG = {
    "entity.harmontide.dream_leviathan": "Dream-Leviathan",
    "entity.harmontide.dream_calf": "Dream-Leviathan Calf",
    "entity.harmontide.infested_zombie": "Infested Zombie",
    "entity.harmontide.infested_skeleton": "Infested Skeleton",
    "entity.harmontide.infested_creeper": "Infested Creeper",
    "entity.harmontide.bloated_host": "Bloated Host",
    "item.harmontide.dream_horn": "Dream Horn",
    "item.harmontide.dream_scale": "Dream Scale",
    "item.harmontide.antibody": "Antibody",
    "item.harmontide.dream_leviathan_egg": "Dream-Leviathan Egg",
    "item.harmontide.infested_zombie_spawn_egg": "Infested Zombie Spawn Egg",
    "item.harmontide.infested_skeleton_spawn_egg": "Infested Skeleton Spawn Egg",
    "item.harmontide.infested_creeper_spawn_egg": "Infested Creeper Spawn Egg",
    "block.harmontide.leviathan_flesh": "Leviathan Flesh",
    "block.harmontide.leviathan_dense_flesh": "Dense Leviathan Flesh",
    "block.harmontide.leviathan_vein": "Leviathan Vein",
    "block.harmontide.leviathan_baleen": "Baleen",
    "block.harmontide.leviathan_membrane": "Membrane",
    "block.harmontide.leviathan_nerve_node": "Nerve Node",
    "block.harmontide.leviathan_acid": "Stomach Acid",
    "block.harmontide.leviathan_cilia": "Cilia",
    "block.harmontide.leviathan_pulse_wall": "Pulsing Muscle",
    "block.harmontide.leviathan_valve": "Pressure Valve",
    "block.harmontide.leviathan_cyst": "Infected Cyst",
    "block.harmontide.leviathan_gland": "Antibody Gland",
    "block.harmontide.leviathan_heart": "Dream-Leviathan's Heart",
    "block.harmontide.leviathan_exit": "Way Out",
    "biome.harmontide.leviathan_interior": "Inside the Dream-Leviathan",
    "boss.harmontide.leviathan_wave": "Wave %s of %s: the infection",
    "death.attack.harmontide.stomach_acid": "%1$s was digested",
    "death.attack.harmontide.stomach_acid.player": "%1$s was digested while fighting %2$s",
    "tip.harmontide.dream_horn": "Blow it at night under the open sky to call the Dream-Leviathan back",
    "tip.harmontide.dream_leviathan_egg": "Set it down and her calf hatches, yours",
    "advancement.harmontide.inside_the_dream": "Inside the Dream",
    "advancement.harmontide.inside_the_dream.desc": "Climb inside the sick Dream-Leviathan",
    "advancement.harmontide.dream_blessing": "The Dream-Leviathan's Blessing",
    "advancement.harmontide.dream_blessing.desc": "Cure the Dream-Leviathan and touch her heart",
    "message.harmontide.dream_calf.stay": "Your calf floats where she is.",
    "message.harmontide.dream_calf.follow": "Your calf swims after you again.",
    "message.harmontide.dream_calf.hatched": "A little sky whale wriggles out of the egg and nuzzles you. Stay near her and she keeps you safe.",
    M + "landed": "The Dream-Leviathan has landed at %s, %s, %s. Seems like she's not doing well...",
    M + "weaker": "The Dream-Leviathan at %s, %s, %s is growing weaker. She has about a day left.",
    M + "fading": "The Dream-Leviathan at %s, %s, %s is fading fast. Hurry!",
    M + "died": "The Dream-Leviathan has died at %s, %s, %s. Her body will slowly rot away.",
    M + "healed": "The Dream-Leviathan rises from %s, %s, %s, healed and shining. She sings as she goes.",
    M + "crouch_mouth": "Her mouth hangs open. Crouch here to climb inside.",
    M + "crouch_rear": "Her rear is within reach. Crouch here to climb inside.",
    M + "go_to_mouth": "Her mouth is at the front. Go round to it and crouch to climb in.",
    M + "go_to_rear": "Her rear is at the back, under her tail. Go round and crouch to climb in.",
    M + "entered_mouth": "You climb past the baleen into the dark. Her insides pulse around you. Something in here is making her sick.",
    M + "entered_rear": "You squeeze in through her rear into warm darkness. Her insides pulse around you. Something in here is making her sick.",
    M + "left": "You make your way back out of the Dream-Leviathan.",
    M + "expelled": "The Dream-Leviathan heaves you back outside, bruised but whole. Everything you carried is still with you.",
    M + "expelled_dying": "The Dream-Leviathan shudders one last time and pushes you out.",
    M + "healed_out": "Her heart glows under your hand, and she carries you gently back outside.",
    M + "membrane_shut": "The membrane is clenched shut. Clear this room first.",
    M + "cyst_needs_antibody": "The cyst throbs. An antibody from the gland would cure it.",
    M + "gland_has": "You already carry an antibody.",
    M + "wave_start": "Wave %s of %s: the infection comes for you!",
    M + "wave_done": "Wave %s of %s beaten. She breathes a little easier, and the way on opens.",
    M + "solved": "Something deep inside her loosens, and the way on opens.",
    M + "cysts_hint": "Infected cysts breed parasites here. Squeeze an antibody from the blue gland and press it into each cyst.",
    M + "acid_hint": "Stomach acid fills this chamber. Squeeze the valves to raise flesh out of it, and cross before it sinks.",
    M + "heartbeat_hint": "Walls of muscle clench with her heartbeat. Slip through while they are open.",
    M + "cilia_hint": "Cilia stand up over a pit of acid. They fold away under your feet, so keep moving.",
    M + "heart_hint": "Her heart hangs in the middle of the chamber. Touch it.",
    M + "heart_not_yet": "Her heart is still sick. Clear the way here first.",
    M + "valve": "The valve squeezes, and flesh rises out of the acid. It won't stay up for long.",
    M + "heaved": "Her muscles heave you back up out of the pit.",
    M + "blessed": "The Dream-Leviathan blesses you: a second bar of hearts shines above your own.",
    M + "got_egg": "She gives you one of her eggs.",
    M + "already_blessed": "She hums warmly at you. You already carry her blessing and her egg.",
    M + "no_scales": "There are no more dream scales to pull from her.",
    M + "horn_answered": "Far away, something enormous answers the horn. She is coming, and she does not sound well.",
    M + "horn_off": "The horn's call fades. Nothing answers.",
    M + "horn_busy": "The Dream-Leviathan is already here somewhere.",
    M + "horn_dimension": "The horn's call can't reach her from here.",
    M + "horn_day": "The horn's call is lost in the daylight. Try again at night.",
    M + "horn_sky": "The horn's call needs the open sky.",
    M + "command_no": "The Dream-Leviathan isn't in a state for that.",
    G + "leviathan.title": "The Dream-Leviathan",
    G + "leviathan": "Once in a world's life, a great whale of night sky flies over, and something is wrong with her. "
                     "She circles for a while, then lands near someone, and word goes round where she lies. "
                     "Crouch at her open mouth to climb inside. Everyone arrives at the same spot, and the way back out is in the wall there. "
                     "Inside are rooms of her flesh: five puzzles, each followed by a wave of the infection making her sick. "
                     "The waves are zombies, skeletons and creepers, some of them overgrown by the parasite. "
                     "A membrane keeps each room's way on shut until the puzzle is solved or the wave beaten. "
                     "Strike the nerve that lights up. Slip through the muscle walls between heartbeats. "
                     "Squeeze the valves to cross the acid. Cure the cysts with antibodies from the gland. Run over the cilia before they fold. "
                     "Die in there and she simply pushes you back out, everything you carried still with you. "
                     "At the end, in a round chamber, hangs her heart. Touch it and she is cured. "
                     "Whoever is there is blessed with a second bar of hearts that fills slowly on its own, and one of her eggs. "
                     "Her calf hatches from the egg, grows to the size of a horse, and wraps you in Absorption while you are near. "
                     "Leave her too long, three days, and she dies where she lies and rots away. "
                     "Dream scales pulled from her body, or dropped by her parasites, make a dream horn with a goat horn. "
                     "Blown at night under the open sky, it calls her back for another visit.",
}


def main():
    whale_textures()
    blocks()
    items()
    parasites()
    hearts()
    data()
    lang = dict(LANG)
    for name in ADVANCEMENTS:
        pass
    gen_milk_assets.add_lang(lang)


if __name__ == "__main__":
    main()
