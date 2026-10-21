"""Shared engine for the harpy model generators (GeckoLib / Bedrock geometry).

A species script (gen_sparrow_harpy.py, gen_crow_harpy.py, ...) declares its parts, paints the
outfit, builds the skeleton and calls `run(...)`. Everything generic lives here: the texture
packer, painters for the shared anatomy (face, hair, feathers, skin, legs), geometry helpers,
animation helpers, a software preview renderer and the file writers.

Pure standard library.

Detail level: D = 4 texels per model unit (a vanilla mob is 1). Cube sizes should be multiples of
0.25 so every face lands on whole texels.

Expressions: the face has two hidden "pop-out" parts - `eyelids` (happy closed eyes / blink) and
`mouth` (open mouth). They sit just inside the face and slide forward POP units through a
position animation.

FORMAT FACTS (verified against Blockbench 5.2.2, not assumed):
  * 1 unit = 1/16 block, Y up, the model faces -Z. The character's right side is -X.
  * Per-face UV, texture (u,v) -> model position, with x0/y0/z0 = min and x1/y1/z1 = max corner:
      north (z0): u -> +x, v -> -y        south (z1): u -> -x, v -> -y
      east  (x0): u -> -z, v -> -y        west  (x1): u -> +z, v -> -y
      up    (y1): u -> +x, v -> -z        down  (y0): u -> +x, v -> +z
    so every side face reads left-to-right as seen from outside.
  * Animation `position` values are plain model-space offsets (+x, +y, +z as above).
  * Rotations: X sense matches the usual reading (positive X leans the top forward).
    The Z sense is mirrored, so rotations are AUTHORED as "positive Z tilts a hanging part's tip
    toward +X" and Z is negated when written out (cubes and animation keys).
    Y is written as is: positive Y turns a part's -Z (front) toward -X, verified against GeckoLib 4.7.7.
"""
import copy
import json
import math
import os
import re
import struct
import sys
import zlib

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
D = 4                      # texels per model unit
POP = 0.4                  # how far expression parts slide out of the face
CLEAR = (0, 0, 0, 0)
FACES = ("north", "south", "east", "west", "up", "down")
BROAD = ("north", "south")


# ----------------------------------------------------------------------------- colour helpers
def rgb(r, g, b):
    return (r, g, b, 255)


class Ramp(list):
    """a dark -> light list of colours; nodither ramps (skin) use clean banding"""
    nodither = False


def ramp(colors, nodither=False):
    r = Ramp(colors)
    r.nodither = nodither
    return r


def noise(x, y, seed=0):
    h = (x * 374761393 + y * 668265263 + seed * 2147483647) & 0xFFFFFFFF
    h = ((h ^ (h >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((h ^ (h >> 16)) & 0xFFFF) / 65535.0


BAYER = ((0.125, 0.625), (0.875, 0.375))


def pick(rmp, t, x=0, y=0):
    """position t in 0..1 along a dark->light ramp, with a soft ordered dither between steps"""
    t = max(0.0, min(1.0, t)) * (len(rmp) - 1)
    if getattr(rmp, "nodither", False):
        return rmp[int(round(t))]
    lo = int(t)
    if lo >= len(rmp) - 1:
        return rmp[-1]
    fr = t - lo
    if fr < 0.25:
        return rmp[lo]
    if fr > 0.75:
        return rmp[lo + 1]
    return rmp[lo + 1] if fr > BAYER[y & 1][x & 1] else rmp[lo]


class Pal:
    """palette + face/hair style knobs for the shared painters (defaults = the sparrow)"""

    def __init__(self):
        self.HAIR = ramp([rgb(60, 36, 26), rgb(90, 55, 36), rgb(128, 82, 50), rgb(166, 112, 68), rgb(204, 152, 94), rgb(232, 190, 130)])
        self.SKIN = ramp([rgb(196, 142, 114), rgb(222, 170, 140), rgb(240, 198, 166), rgb(250, 220, 192)], nodither=True)
        self.FEA = ramp([rgb(48, 31, 24), rgb(74, 47, 32), rgb(108, 70, 45), rgb(148, 102, 68), rgb(194, 148, 106), rgb(228, 208, 178), rgb(248, 240, 228)])
        self.EYE = ramp([rgb(112, 70, 22), rgb(164, 106, 26), rgb(216, 160, 42), rgb(240, 198, 84), rgb(252, 228, 140)])
        self.LEG = ramp([rgb(112, 82, 30), rgb(166, 124, 44), rgb(220, 174, 66), rgb(244, 210, 108), rgb(255, 238, 166)])
        self.TALON = [rgb(30, 24, 22), rgb(58, 48, 44)]
        self.EYE_OUT = rgb(50, 30, 20)
        self.PUPIL = rgb(66, 38, 18)
        self.WHITE = rgb(255, 250, 240)
        self.BLUSH = rgb(238, 150, 142)
        self.BLUSH_L = rgb(244, 178, 164)
        self.LIP = rgb(188, 102, 98)
        self.LIP_D = rgb(150, 76, 78)
        self.MOUTH_IN = rgb(96, 30, 38)
        self.TONGUE = rgb(220, 112, 118)
        self.BROW = self.HAIR[1]
        # eye shape: semi-axes (texels), tilt of the lid line, pupil radii, highlight rows
        self.eye_a, self.eye_b, self.eye_slant = 4.0, 6.0, 0.0
        self.pupil = (1.9, 3.3)
        self.hl_dy, self.hl2_dy = 4, 3
        self.lid_r = (4.9, 6.6)                      # radii of the skin patch that closes each eye
        self.brows = [(8, 10, -4, 3, 0, 1), (24, 10, -4, 3, 0, 1)]   # (centre x, y, x0, x1, slope, thickness)
        self.lash_extra = []                         # extra lash pixels as (dx outward from the eye centre, y); [] = none
        self.mouth_edge = (self.LIP, 0.62)           # (colour, ellipse threshold) of the open mouth's outline
        self.mouth_px = {}                           # filled below
        self.streak = (0.46, 0.60, 0.40, 0.66, 0.50, 0.58, 0.42)
        self.hair_gain, self.hair_off = 1.0, 0.0      # scale / shift where hair sits on its ramp (darker hair: gain < 1)
        self.wing_base_t = (0.18, 0.40)
        self.tail_fluff_t = 0.78
        self.fluff_t = 0.80
        self.mouth_px = {(x, 28): self.LIP for x in range(14, 18)}
        self.mouth_px.update({(13, 27): self.LIP, (18, 27): self.LIP, (15, 29): self.SKIN[3], (16, 29): self.SKIN[3]})


# ----------------------------------------------------------------------------- parts / texture packing
PARTS = {}
RECT = {}      # (part, face) -> (x, y, w, h) in texels
TEX = 0


def part(name, w, h, d, skip=()):
    PARTS[name] = dict(size=(w, h, d), skip=set(skip))


# the anatomy every harpy shares; a species may re-declare a part to change its size or add its own
STANDARD_PARTS = [
    ("head", 8, 8, 8), ("hair_cap", 8, 8, 8, ("down",)), ("ear", 1, 2, 1.5),
    ("eyelids", 6.5, 3.5, .25), ("mouth", 1.75, 1.5, .25),
    ("bang_a", 2, 3, .5), ("bang_b", 2, 4.5, .5), ("bang_c", 2, 3.5, .5), ("bang_d", 2, 4, .5), ("bang_e", 2, 5.5, .5),
    ("frame", 1, 7, .5), ("tuft", 2, 2, 1.5),
    ("strand_s1", 1.5, 7, 1), ("strand_s2", 1.5, 7, 1), ("strand_m1", 1.5, 10, 1), ("strand_m2", 1.5, 10, 1),
    ("strand_l1", 1.5, 13, 1), ("strand_l2", 1.5, 13, 1),
    ("hfeather", 1, 4, .5), ("ahoge", 1, 1.5, 1), ("ahoge_tip", 1, 1, 1),
    ("hips", 5, 1.25, 3.5, ("up", "down")), ("midriff", 3.5, 2.75, 2.5), ("chest", 5, 3, 3.25),
    ("bust", 2, 1.5, .75), ("collar", 3.5, 1, 2.75),
    ("sash", 5.5, 1.25, 4, ("up", "down")), ("back_cover", 3.5, 4, .5), ("trim", 1, 1.5, .5),
    ("pendant", 1, 1, .5), ("cord", 3, .5, .5),
    ("leaf_s", 2, 3.5, .5), ("leaf_m", 2, 5, .5), ("leaf_l", 2, 6.5, .5), ("leaf_side", .5, 5, 2.5), ("tassel", 1, 3, .5),
    ("arm_up", 1.5, 3.5, 1.5), ("forearm", 1.5, 3, 1.5), ("bracer", 2.5, 2, 2.5), ("cuff_f", 1, 2.5, .5),
    ("palm", 1.5, 1.5, 1), ("fingers", 1.5, 1, 1), ("thumb", .5, 1, .5),
    ("wing_base", 4, 9, .5), ("cov_a", 2, 3, .5), ("cov_b", 2, 3, .5), ("cov_c", 2, 3, .5),
    ("sec_a", 1.5, 8.5, .5), ("sec_b", 1.5, 8.5, .5), ("prim_a", 1.5, 10, .5), ("prim_b", 1.5, 10, .5),
    ("tail_fluff", 3.5, 1.5, 1.5), ("tail_f", 1.5, 9, .5), ("tail_g", 1.5, 9, .5),
    ("thigh", 2, 3.5, 2), ("fluff", 2.5, 1.5, 2.5), ("shin", 1.5, 7, 1.5), ("ankle", 2, 1, 2), ("heel", 2, 1, 2),
    ("toe1", 1, 1, 2.5), ("toe2", 1, 1, 2), ("talon", 1, .5, 2), ("htoe", 1, 1, 1.5), ("htalon", 1, .5, 1.5),
]


def standard_parts():
    PARTS.clear()
    RECT.clear()
    for args in STANDARD_PARTS:
        part(*args)


def texels(v):
    return max(1, int(round(v * D)))


def face_dims(p, f):
    w, h, d = PARTS[p]["size"]
    return {"north": (w, h), "south": (w, h), "east": (d, h), "west": (d, h), "up": (w, d), "down": (w, d)}[f]


def pack():
    global TEX
    rects = []
    for p, spec in PARTS.items():
        for f in FACES:
            if f in spec["skip"]:
                rects.append(((p, f), 1, 1))
            else:
                a, b = face_dims(p, f)
                rects.append(((p, f), texels(a), texels(b)))
    for size in (128, 256, 512, 1024):
        x = y = shelf = 0
        pos, ok = {}, True
        for key, w, h in sorted(rects, key=lambda r: (-r[2], -r[1])):
            if w > size:
                ok = False
                break
            if x + w > size:
                x, y, shelf = 0, y + shelf + 1, 0
            pos[key] = (x, y, w, h)
            x += w + 1
            shelf = max(shelf, h)
        if ok and y + shelf <= size:
            RECT.update(pos)
            TEX = size
            return
    raise RuntimeError("parts do not fit in 1024x1024")


# ----------------------------------------------------------------------------- painting
class Painter:
    def __init__(self):
        self.T = [[CLEAR] * TEX for _ in range(TEX)]

    def __call__(self, p, faces, fn):
        if p not in PARTS:
            return
        for f in (FACES if faces == "all" else faces):
            if f in PARTS[p]["skip"]:
                continue
            x0, y0, w, h = RECT[(p, f)]
            for y in range(h):
                for x in range(w):
                    c = fn(f, x, y, w, h)
                    if c is not None:
                        self.T[y0 + y][x0 + x] = c


def skin_fn(pal, base=0.66, grain=0.10):
    def fn(f, x, y, w, h):
        t = base + (noise(x, y, 1) - 0.5) * grain
        if f in ("east", "west"):
            t -= 0.10
        elif f in ("south", "down"):
            t -= 0.14
        edge = min(x, w - 1 - x)
        if edge < max(1, D // 4):
            t -= 0.10
        if y > h - 1 - max(1, D // 4):
            t -= 0.08
        return pick(pal.SKIN, t, x, y)
    return fn


def feather_fn(pal, top, mid, tip, tip_dark=None, rounded=False, edge=True):
    """pal.FEA indices for the top / middle / tip bands; shaft, barbs, outline, pointed (or rounded) tip"""
    FEA = pal.FEA

    def fn(f, x, y, w, h):
        if f not in BROAD:
            return FEA[mid]
        t = y / max(1, h - 1)
        n = (noise(x, y, 13) - 0.5) * 0.06
        band = 0 if t + n < 0.30 else (1 if t + n < 0.62 else 2)
        idx = (top, mid, tip)[band]
        cx = w // 2
        if x == cx:
            idx += 1                                   # shaft
        elif not rounded and (y + abs(x - cx) * 2) % 5 == 0:
            idx -= 1                                   # barbs
        if edge and (x == 0 or x == w - 1) and y > 3:
            idx -= 1                                   # outline
        if tip_dark is not None and h * 0.78 <= y < h * 0.78 + max(2, D // 2) and not rounded:
            idx = tip_dark
        r = h - 1 - y
        tl = 2.5 * D if not rounded else 1.2 * D
        if r < tl:
            half = (w / 2.0) * (min(1.0, (r + 1) / tl) ** (0.85 if not rounded else 0.55))
            if abs(x + 0.5 - w / 2.0) > half:
                return CLEAR
        return FEA[max(0, min(len(FEA) - 1, idx))]
    return fn


def paint_head(pt, pal):
    """face (north side is 8 x 8 units = 32 x 32 texels), ears, and the hidden expression parts"""
    SKIN = pal.SKIN

    def eye_px(x, y, cx, cy, inner):
        a, b = pal.eye_a, pal.eye_b
        cye = cy + pal.eye_slant * inner * (x + 0.5 - cx) / a if pal.eye_slant else cy
        dx, dy = (x + 0.5 - cx) / a, (y + 0.5 - cye) / b
        e = dx * dx + dy * dy
        if e > 1.0:
            if e < 1.3 and dy > 0.45:                  # lower-lid shadow just outside the ellipse
                return SKIN[1]
            return None
        if e > 0.62 or (dy < -0.40 and e > 0.30):
            return pal.EYE_OUT
        col = pick(pal.EYE, (y - (cye - b)) / (2 * b), x, y)
        pdx, pdy = (x + 0.5 - cx) / pal.pupil[0], (y + 0.5 - (cye - 0.5)) / pal.pupil[1]
        if pdx * pdx + pdy * pdy <= 1.0:
            col = pal.PUPIL
        hx = range(cx, cx + 2) if inner > 0 else range(cx - 2, cx)
        if x in hx and (cy - pal.hl_dy) <= y <= (cy - pal.hl_dy + 1):
            col = pal.WHITE
        sx = cx - 2 if inner > 0 else cx + 1
        if x == sx and y == cy + pal.hl2_dy:
            col = pal.EYE[4]
        return col

    def face(f, x, y, w, h):
        for cx, inner in ((8, 1), (24, -1)):
            c = eye_px(x, y, cx, 18, inner)
            if c is not None:
                return c
        for cx, sg in ((8, -1), (24, 1)):               # lash flick at the outer top corner
            if (x, y) in ((cx + sg * 5, 15), (cx + sg * 6, 14), (cx + sg * 5, 14)):
                return pal.EYE_OUT
        for cx, sg in ((8, -1), (24, 1)):
            for dx, yy in pal.lash_extra:
                if (x, y) == (cx + sg * dx, yy):
                    return pal.EYE_OUT
        for cx, by, x0, x1, slope, th in pal.brows:
            yy = int(round(by + slope * (x - cx)))
            if cx + x0 <= x <= cx + x1 and yy <= y < yy + th:
                return pal.BROW
        for cx in (7, 25):                               # blush
            e = ((x + 0.5 - cx) / 3.6) ** 2 + ((y + 0.5 - 26.5) / 1.6) ** 2
            if e <= 1.0:
                return pal.BLUSH if e < 0.45 else pal.BLUSH_L
        if y == 26 and x in (15, 16):
            return SKIN[1]
        m = pal.mouth_px.get((x, y))
        if m is not None:
            return m
        t = 0.70 + (noise(x, y, 3) - 0.5) * 0.08
        if y < 4:
            t -= 0.16                                    # shadow under the fringe
        if y > 29:
            t -= 0.10
        if x < 2 or x > 29:
            t -= 0.12
        return pick(SKIN, t, x, y)
    pt("head", "all", skin_fn(pal))
    pt("head", ("north",), face)
    pt("ear", "all", lambda f, x, y, w, h: pick(SKIN, 0.35, x, y) if (f in BROAD + ("east", "west") and 2 <= x < w - 2 and 3 <= y < h - 3) else pick(SKIN, 0.62, x, y))

    def lids(f, x, y, w, h):
        if f != "north":
            return pick(SKIN, 0.66, x, y)
        for ex in (1, 17):                                # eye boxes inside the 26x14 lid sheet
            cx, cy = ex + 4, 7
            e = ((x + 0.5 - cx) / pal.lid_r[0]) ** 2 + ((y + 0.5 - cy) / pal.lid_r[1]) ** 2
            if e <= 1.0:
                arch = cy + 2 - 3.2 * math.sin(math.pi * (x - ex + 0.5) / 8.0)   # happy closed eye
                if abs(y + 0.5 - arch) <= 1.0:
                    return pal.EYE_OUT
                return pick(SKIN, 0.70 + (noise(x, y, 5) - 0.5) * 0.06, x, y)
        return CLEAR
    pt("eyelids", "all", lids)

    def mouth(f, x, y, w, h):
        if f != "north":
            return pal.LIP_D
        e = ((x + 0.5 - w / 2) / (w / 2)) ** 2 + ((y + 0.5 - h / 2) / (h / 2)) ** 2
        if e > 1.0:
            return CLEAR
        if e > pal.mouth_edge[1]:
            return pal.mouth_edge[0]
        if y >= h - 3 and 2 <= x < w - 2:
            return pal.TONGUE
        if y <= 1 and 2 <= x < w - 2:
            return pal.WHITE
        return pal.MOUTH_IN
    pt("mouth", "all", mouth)


def paint_hair(pt, pal):
    HAIR = pal.HAIR

    def hair_px(x, y, h, gloss=False):
        sid = x // 2
        t = (pal.streak[sid % 7] * 0.8 + (y / max(1, h - 1)) * 0.32 + (noise(x, y, 7) - 0.5) * 0.10) * pal.hair_gain + pal.hair_off
        if gloss and 0.16 < y / max(1, h - 1) < 0.30:
            t += 0.22
        if y > h - 3:
            t += 0.10
        return pick(HAIR, t, x, y)

    pt("hair_cap", "all", lambda f, x, y, w, h: hair_px(x, y, h, True))
    pt("hair_cap", ("north",), lambda f, x, y, w, h: hair_px(x, y, h, True) if y < 5 else CLEAR)

    def cap_side(f, x, y, w, h):
        lo, hi = (13, 22) if f == "east" else (10, 19)
        if lo <= x <= hi and 14 <= y <= 26:
            return CLEAR
        if y >= 26 and noise(x, y, 9) > 0.45:
            return CLEAR
        return None
    pt("hair_cap", ("east", "west"), cap_side)
    pt("hair_cap", ("south",), lambda f, x, y, w, h: CLEAR if y >= 27 and noise(x, y, 9) > 0.45 else None)
    pt("hair_cap", ("up",), lambda f, x, y, w, h: HAIR[1] if abs(x - 15 - (16 - y) // 3) <= 0 and y > 4 else None)

    def strand(f, x, y, w, h):
        if f not in BROAD:
            return pick(HAIR, 0.45, x, y)
        r = h - 1 - y
        taper = 2.5 * D
        if r < taper:
            half = (w / 2.0) * (min(1.0, (r + 1) / taper) ** 0.85)
            if abs(x + 0.5 - w / 2.0) > half:
                return CLEAR
        return hair_px(x, y, h, y < 5)
    for p in [n for n in PARTS if n.startswith(("bang_", "strand_"))] + ["frame"]:   # species may add longer strands
        pt(p, "all", strand)
    pt("tuft", "all", lambda f, x, y, w, h: CLEAR if (f in BROAD + ("east", "west") and y < 2 and noise(x, y, 11) > 0.5) else hair_px(x, y + 4, h + 4, False))
    pt("ahoge", "all", lambda f, x, y, w, h: pick(HAIR, 0.9 if y < 2 else 0.7, x, y))
    pt("ahoge_tip", "all", lambda f, x, y, w, h: pick(HAIR, 0.95, x, y))


def paint_wings_tail(pt, pal, specs):
    """specs: part -> args for feather_fn (see sparrow / crow scripts)"""
    for p, args in specs.items():
        pt(p, "all", feather_fn(pal, *args))
    FEA = pal.FEA
    lo, span = pal.wing_base_t
    pt("wing_base", "all", lambda f, x, y, w, h: pick(FEA, lo + span * y / h + (noise(x, y, 17) - 0.5) * 0.10, x, y))
    pt("tail_fluff", "all", lambda f, x, y, w, h: CLEAR if (y == h - 1 and noise(x, y, 21) > 0.5) else pick(FEA, pal.tail_fluff_t + (noise(x, y, 23) - 0.5) * 0.3, x, y))


def paint_body_skin(pt, pal):
    SKIN = pal.SKIN
    pt("midriff", "all", skin_fn(pal, 0.66))
    pt("midriff", ("north",), lambda f, x, y, w, h: pick(SKIN, 0.2, x, y) if (x in (w // 2 - 1, w // 2) and y in (h // 2 + 1, h // 2 + 2)) else None)
    pt("collar", "all", skin_fn(pal, 0.70))
    pt("collar", ("north",), lambda f, x, y, w, h: pick(SKIN, 0.3, x, y) if (y == h - 3 and (2 <= x <= 4 or w - 5 <= x <= w - 3)) else None)
    pt("hips", "all", skin_fn(pal, 0.55))
    pt("arm_up", "all", skin_fn(pal, 0.68))
    pt("forearm", "all", skin_fn(pal, 0.68))
    pt("palm", "all", skin_fn(pal, 0.68))
    pt("fingers", "all", lambda f, x, y, w, h: pick(SKIN, 0.35, x, y) if (f in BROAD and x in (2, 4)) else skin_fn(pal, 0.68)(f, x, y, w, h))
    pt("thumb", "all", skin_fn(pal, 0.68))
    pt("thigh", "all", skin_fn(pal, 0.68))


def paint_legs(pt, pal):
    FEA, LEG = pal.FEA, pal.LEG
    pt("fluff", "all", lambda f, x, y, w, h: CLEAR if (f not in ("up", "down") and y >= h - 2 and noise(x, y, 51) > 0.45) else pick(FEA, pal.fluff_t + (noise(x, y, 53) - 0.5) * 0.3 - (0.25 if y >= h - 3 else 0), x, y))

    def scales(f, x, y, w, h):
        if f in ("up", "down"):
            return LEG[1]
        row = y // 3
        off = (row % 2) * 1.5
        cell_x = (x + off) % 3
        t = 0.55 + (noise(x, y, 57) - 0.5) * 0.12
        if y % 3 == 2:
            t = 0.20                                            # scale lower edge
        elif cell_x < 1:
            t = 0.85                                            # scale highlight
        if x == 0 and f in BROAD:
            t += 0.12
        return pick(LEG, t, x, y)
    pt("shin", "all", scales)
    pt("ankle", "all", lambda f, x, y, w, h: pick(LEG, 0.25 + (noise(x, y, 59) - 0.5) * 0.15, x, y))
    pt("heel", "all", lambda f, x, y, w, h: pick(LEG, 0.55, x, y))
    for p in ("toe1", "toe2", "htoe"):
        pt(p, "all", lambda f, x, y, w, h: pick(LEG, 0.22, x, y) if ((f in ("east", "west", "up") and (x % 5 == 4)) or (f in BROAD and y == h - 1)) else pick(LEG, 0.58 + (noise(x, y, 61) - 0.5) * 0.14, x, y))
    for p in ("talon", "htalon"):
        pt(p, "all", lambda f, x, y, w, h: pal.TALON[1] if (f in ("up", "east", "west") and y == 0) else pal.TALON[0])


def write_png(path, rows):
    h, w = len(rows), len(rows[0])
    raw = b"".join(b"\x00" + bytes(ch for px in row for ch in px) for row in rows)

    def chunk(tag, data):
        c = struct.pack(">I", len(data)) + tag + data
        return c + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
    png = (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
           + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as fh:
        fh.write(png)


def upscale(tex, k):
    return [[px for px in row for _ in range(k)] for row in tex for _ in range(k)]


# ----------------------------------------------------------------------------- geometry
def cube(p, origin, inflate=None, pivot=None, rotation=None):
    """rotation is authored (see module docstring); Z is negated on the way out."""
    c = {"origin": [round(v, 3) for v in origin], "size": list(PARTS[p]["size"]),
         "uv": {f: {"uv": [RECT[(p, f)][0], RECT[(p, f)][1]], "uv_size": [RECT[(p, f)][2], RECT[(p, f)][3]]} for f in FACES}}
    if inflate:
        c["inflate"] = inflate
    if pivot is not None:
        c["pivot"] = [round(v, 3) for v in pivot]
        c["rotation"] = [rotation[0], rotation[1], -rotation[2]]
    return c


def hang(p, x, ytop, z, rz=0, rx=0):
    """a cube hanging from (x+w/2, ytop, z+d/2), tilted about that top point"""
    w, h, d = PARTS[p]["size"]
    return cube(p, [x, ytop - h, z], pivot=[x + w / 2, ytop, z + d / 2], rotation=[rx, 0, rz])


def mirror_cube(c):
    m = copy.deepcopy(c)
    m["origin"][0] = round(-(c["origin"][0] + c["size"][0]), 3)
    if "pivot" in c:
        m["pivot"][0] = -c["pivot"][0]
        m["rotation"] = [c["rotation"][0], -c["rotation"][1], -c["rotation"][2]]
    return m


class Model:
    def __init__(self, name):
        self.name = name
        self.bones = []

    def bone(self, name, parent, pivot, cubes=None, mirror=False):
        """add a bone; with mirror=True a name ending in _r also gets its _l twin"""
        b = {"name": name}
        if parent:
            b["parent"] = parent
        b["pivot"] = list(pivot)
        if cubes:
            b["cubes"] = cubes
        self.bones.append(b)
        if mirror:
            m = copy.deepcopy(b)
            m["name"] = re.sub(r"_r$", "_l", name)
            if parent:
                m["parent"] = re.sub(r"_r$", "_l", parent)
            m["pivot"][0] = -pivot[0]
            if cubes:
                m["cubes"] = [mirror_cube(c) for c in cubes]
            self.bones.append(m)

    def doc(self):
        return {
            "format_version": "1.12.0",
            "minecraft:geometry": [{
                "description": {
                    "identifier": f"geometry.{self.name}", "texture_width": TEX, "texture_height": TEX,
                    "visible_bounds_width": 5, "visible_bounds_height": 3, "visible_bounds_offset": [0, 1, 0],
                },
                "bones": self.bones,
            }],
        }


# ----------------------------------------------------------------------------- animation helpers
def t(x):
    return repr(float(x))


def track(*pairs):
    return {t(k): list(v) for k, v in pairs}


def mirror(tr):
    """Left-side copy of a right-side rotation track (negate Y and Z)."""
    return {k: [v[0], -v[1], -v[2]] for k, v in tr.items()}


def sym(base, tr, kind="rotation"):
    if kind == "position":
        return {base + "_r": {kind: tr}, base + "_l": {kind: tr}}
    return {base + "_r": {kind: tr}, base + "_l": {kind: mirror(tr)}}


def merge(*dicts):
    out = {}
    for d_ in dicts:
        for k, v in d_.items():
            out.setdefault(k, {}).update(v)
    return out


def hold(*v):
    return track((0, v), (1, v))


OUT_ = (0, 0, -POP)
REST = (0, 0, 0)


def blink(*times):
    """eyelids slide out for ~0.12s at each given time"""
    pairs = [(0, REST)]
    for tm in times:
        pairs += [(tm, REST), (tm + 0.04, OUT_), (tm + 0.12, OUT_), (tm + 0.16, REST)]
    return {"eyelids": {"position": track(*pairs)}}


class Anims:
    def __init__(self, name, bone_names):
        self.name = name
        self.bones = set(bone_names)
        self.A = {}

    def add(self, key, length, loop, bones):
        bones = {k: v for k, v in bones.items() if k in self.bones}
        for ch in bones.values():                       # authored Z -> file Z (see module docstring)
            if "rotation" in ch:
                ch["rotation"] = {k: [v[0], v[1], -v[2]] for k, v in ch["rotation"].items()}
        self.A[f"animation.{self.name}.{key}"] = {"loop": loop, "animation_length": length, "bones": bones}

    def doc(self):
        return {"format_version": "1.8.0", "animations": self.A}


def std_idle(an):
    an.add("idle", 4.0, True, merge({
        "body": {"position": track((0, (0, 0, 0)), (2, (0, -0.25, 0)), (4, (0, 0, 0)))},
        "head": {"rotation": track((0, (0, 0, 0)), (1.2, (-2, 6, 1)), (2.4, (1, -4, -1)), (4, (0, 0, 0)))},
        "hair_back": {"rotation": track((0, (0, 0, 0)), (2, (4, 0, 0)), (4, (0, 0, 0)))},
        "ahoge": {"rotation": track((0, (0, 0, -4)), (1, (0, 0, 6)), (2, (0, 0, -4)), (3, (0, 0, 6)), (4, (0, 0, -4)))},
        "tail": {"rotation": track((0, (0, 0, 0)), (1, (0, 6, 0)), (2, (0, 0, 0)), (3, (0, -6, 0)), (4, (0, 0, 0)))},
        "skirt": {"rotation": track((0, (0, 0, 0)), (2, (2, 0, 0)), (4, (0, 0, 0)))},
    },
        blink(2.7),
        sym("hair_side", track((0, (0, 0, 0)), (2, (0, 0, -3)), (4, (0, 0, 0)))),
        sym("wing", track((0, (0, 0, 0)), (2, (0, 0, -2)), (4, (0, 0, 0)))),
        sym("wing_tip", track((0, (0, 0, 0)), (1.5, (0, 0, 3)), (3, (0, 0, -2)), (4, (0, 0, 0)))),
        sym("arm", track((0, (0, 0, 2)), (2, (0, 0, 0)), (4, (0, 0, 2)))),
        sym("forearm", hold(-6, 0, 0)), sym("hand", hold(-8, 0, 0))))


def std_walk(an):
    swing = track((0, (28, 0, 0)), (0.5, (-28, 0, 0)), (1, (28, 0, 0)))
    swing_o = track((0, (-28, 0, 0)), (0.5, (28, 0, 0)), (1, (-28, 0, 0)))
    an.add("walk", 1.0, True, merge({
        "leg_r": {"rotation": swing}, "leg_l": {"rotation": swing_o},
        "shin_r": {"rotation": track((0, (0, 0, 0)), (0.25, (35, 0, 0)), (0.5, (0, 0, 0)), (1, (0, 0, 0)))},
        "shin_l": {"rotation": track((0, (0, 0, 0)), (0.5, (0, 0, 0)), (0.75, (35, 0, 0)), (1, (0, 0, 0)))},
        "foot_r": {"rotation": track((0, (0, 0, 0)), (0.25, (-20, 0, 0)), (0.5, (8, 0, 0)), (1, (0, 0, 0)))},
        "foot_l": {"rotation": track((0, (0, 0, 0)), (0.5, (0, 0, 0)), (0.75, (-20, 0, 0)), (1, (8, 0, 0)))},
        "arm_r": {"rotation": track((0, (-15, 0, 0)), (0.5, (15, 0, 0)), (1, (-15, 0, 0)))},
        "arm_l": {"rotation": track((0, (15, 0, 0)), (0.5, (-15, 0, 0)), (1, (15, 0, 0)))},
        "body": {"position": track((0, (0, 0, 0)), (0.25, (0, -0.4, 0)), (0.5, (0, 0, 0)), (0.75, (0, -0.4, 0)), (1, (0, 0, 0))),
                 "rotation": track((0, (0, 0, 2.5)), (0.5, (0, 0, -2.5)), (1, (0, 0, 2.5)))},
        "head": {"rotation": track((0, (0, 0, -2.5)), (0.5, (0, 0, 2.5)), (1, (0, 0, -2.5)))},
        "hair_back": {"rotation": track((0, (0, 0, 0)), (0.25, (6, 0, 0)), (0.5, (0, 0, 0)), (0.75, (6, 0, 0)), (1, (0, 0, 0)))},
        "skirt": {"rotation": track((0, (0, 0, 3)), (0.5, (0, 0, -3)), (1, (0, 0, 3)))},
        "tail": {"rotation": track((0, (0, 10, 0)), (0.5, (0, -10, 0)), (1, (0, 10, 0)))},
    },
        sym("hair_side", track((0, (0, 0, -2)), (0.25, (0, 0, -5)), (0.5, (0, 0, -2)), (0.75, (0, 0, -5)), (1, (0, 0, -2)))),
        sym("wing", track((0, (0, 0, 1.5)), (0.5, (0, 0, -1)), (1, (0, 0, 1.5)))),
        sym("forearm", hold(-15, 0, 0))))


def std_fly(an):
    flap = track((0, (0, 0, -115)), (0.4, (0, 0, -40)), (0.8, (0, 0, -115)))
    lag = track((0, (0, 0, 18)), (0.4, (0, 0, -14)), (0.8, (0, 0, 18)))
    an.add("fly", 0.8, True, merge({
        "body": {"position": track((0, (0, -0.8, 0)), (0.4, (0, 0.8, 0)), (0.8, (0, -0.8, 0))),
                 "rotation": track((0, (26, 0, 0)), (0.4, (18, 0, 0)), (0.8, (26, 0, 0)))},
        "head": {"rotation": track((0, (-24, 0, 0)), (0.4, (-20, 0, 0)), (0.8, (-24, 0, 0)))},
        "leg_r": {"rotation": track((0, (40, 0, 0)), (0.4, (46, 0, 0)), (0.8, (40, 0, 0)))},
        "leg_l": {"rotation": track((0, (40, 0, 0)), (0.4, (46, 0, 0)), (0.8, (40, 0, 0)))},
        "shin_r": {"rotation": hold(30, 0, 0)}, "shin_l": {"rotation": hold(30, 0, 0)},
        "foot_r": {"rotation": hold(-25, 0, 0)}, "foot_l": {"rotation": hold(-25, 0, 0)},
        "hair_back": {"rotation": track((0, (35, 0, 0)), (0.4, (28, 0, 0)), (0.8, (35, 0, 0)))},
        "skirt": {"rotation": track((0, (-14, 0, 0)), (0.4, (-4, 0, 0)), (0.8, (-14, 0, 0)))},
        "tail": {"rotation": track((0, (12, 0, 0)), (0.4, (-5, 0, 0)), (0.8, (12, 0, 0)))},
        "ahoge": {"rotation": track((0, (0, 0, -8)), (0.4, (0, 0, 8)), (0.8, (0, 0, -8)))},
    },
        sym("hair_side", track((0, (30, 0, -8)), (0.4, (22, 0, -4)), (0.8, (30, 0, -8)))),
        sym("wing", flap), sym("wing_tip", lag),
        sym("arm", hold(35, 0, 0)), sym("forearm", hold(25, 0, 0)), sym("hand", hold(15, 0, 0))))


def std_sing(an):
    """head up, hands to the chest, eyes happily closed, mouth opening and closing on the notes"""
    notes = [(0, REST), (0.2, OUT_), (0.9, OUT_), (1.1, REST), (1.5, REST), (1.7, OUT_), (2.2, OUT_), (2.4, REST),
             (2.8, REST), (3.0, OUT_), (3.7, OUT_), (3.9, REST), (4, REST)]
    an.add("sing", 4.0, True, merge({
        "head": {"rotation": track((0, (-14, 0, -4)), (1, (-12, 0, 0)), (2, (-14, 0, 4)), (3, (-12, 0, 0)), (4, (-14, 0, -4)))},
        "body": {"rotation": track((0, (0, 0, -2)), (2, (0, 0, 2)), (4, (0, 0, -2)))},
        "tail": {"rotation": track((0, (0, 8, 0)), (2, (0, -8, 0)), (4, (0, 8, 0)))},
        "ahoge": {"rotation": track((0, (0, 0, 8)), (1, (0, 0, -8)), (2, (0, 0, 8)), (3, (0, 0, -8)), (4, (0, 0, 8)))},
        "hair_back": {"rotation": track((0, (0, 0, 0)), (2, (-6, 0, 0)), (4, (0, 0, 0)))},
        "skirt": {"rotation": track((0, (0, 0, 2)), (2, (0, 0, -2)), (4, (0, 0, 2)))},
        "mouth": {"position": track(*notes)},
        "eyelids": {"position": track((0, OUT_), (4, OUT_))},
    },
        sym("hair_side", track((0, (0, 0, -2)), (2, (0, 0, -5)), (4, (0, 0, -2)))),
        sym("wing", track((0, (0, 0, -3)), (0.5, (0, 0, -1)), (1, (0, 0, -3)), (1.5, (0, 0, -1)), (2, (0, 0, -3)), (4, (0, 0, -3)))),
        sym("arm", track((0, (-22, 0, 8)), (2, (-26, 0, 10)), (4, (-22, 0, 8)))),
        sym("forearm", track((0, (-80, 0, 0)), (2, (-86, 0, 0)), (4, (-80, 0, 0)))),
        sym("hand", hold(-10, 0, 0))))


def std_bathe(an):
    shake = track((0, (0, 0, -75)), (0.125, (0, 0, -58)), (0.25, (0, 0, -75)), (0.375, (0, 0, -58)), (0.5, (0, 0, -75)),
                  (1.5, (0, 0, -75)), (1.625, (0, 0, -58)), (1.75, (0, 0, -75)), (2, (0, 0, -75)))
    an.add("bathe", 2.0, True, merge({
        "body": {"position": track((0, (0, -2.5, 0)), (2, (0, -2.5, 0)))},
        "head": {"rotation": track((0, (0, 0, 0)), (0.25, (0, 12, 0)), (0.5, (0, -12, 0)), (0.75, (0, 12, 0)), (1, (0, 0, 0)), (2, (0, 0, 0)))},
        "tail": {"rotation": track((0, (0, 0, 0)), (0.25, (0, 14, 0)), (0.5, (0, -14, 0)), (0.75, (0, 14, 0)), (1, (0, 0, 0)), (2, (0, 0, 0)))},
        "hair_back": {"rotation": track((0, (0, 0, 0)), (0.5, (-8, 0, 0)), (1, (0, 0, 0)), (2, (0, 0, 0)))},
        "skirt": {"rotation": track((0, (0, 0, 0)), (0.25, (0, 0, 4)), (0.5, (0, 0, -4)), (0.75, (0, 0, 4)), (1, (0, 0, 0)), (2, (0, 0, 0)))},
        "eyelids": {"position": track((0, OUT_), (2, OUT_))},
    }, sym("wing", shake),
        sym("hair_side", track((0, (0, 0, -3)), (0.25, (0, 0, -8)), (0.5, (0, 0, -3)), (0.75, (0, 0, -8)), (1, (0, 0, -3)), (2, (0, 0, -3)))),
        sym("wing_tip", track((0, (0, 0, 6)), (0.125, (0, 0, -6)), (0.25, (0, 0, 6)), (2, (0, 0, 6))))))


# ----------------------------------------------------------------------------- preview renderer (mirrors Blockbench)
def rot_matrix(rx, ry, rz):
    """Rotation values as written to the files -> 3x3 matrix in file space, as GeckoLib applies them
    (it negates X and Y and mirrors X, so in file space X and Z come out negated and Y does not)."""
    a, b, c = math.radians(-rx), math.radians(ry), math.radians(-rz)
    ca, sa, cb, sb, cc, sc = math.cos(a), math.sin(a), math.cos(b), math.sin(b), math.cos(c), math.sin(c)
    Rx = ((1, 0, 0), (0, ca, -sa), (0, sa, ca))
    Ry = ((cb, 0, sb), (0, 1, 0), (-sb, 0, cb))
    Rz = ((cc, -sc, 0), (sc, cc, 0), (0, 0, 1))

    def mul(A_, B_):
        return tuple(tuple(sum(A_[i][k] * B_[k][j] for k in range(3)) for j in range(3)) for i in range(3))
    return mul(Rz, mul(Ry, Rx))


def rot_about(p, pivot, R):
    d_ = [p[i] - pivot[i] for i in range(3)]
    return tuple(pivot[i] + sum(R[i][j] * d_[j] for j in range(3)) for i in range(3))


def sample(track_, tm):
    keys = sorted((float(k), v) for k, v in track_.items())
    if tm <= keys[0][0]:
        return keys[0][1]
    for (t0, v0), (t1, v1) in zip(keys, keys[1:]):
        if tm <= t1:
            f = (tm - t0) / (t1 - t0) if t1 > t0 else 0
            return [v0[i] + (v1[i] - v0[i]) * f for i in range(3)]
    return keys[-1][1]


def render(model, tex, anim, tm, yaw, pitch, W, H, scale, bg, center=None):
    bones = {b["name"]: b for b in model["minecraft:geometry"][0]["bones"]}
    pose = {}
    if anim:
        for name, ch in anim["bones"].items():
            pose[name] = {k: sample(v, tm) for k, v in ch.items()}

    def bone_pt(name, p):
        while name:
            b = bones[name]
            ps = pose.get(name, {})
            rot = ps.get("rotation", (0, 0, 0))
            if any(rot):
                p = rot_about(p, b["pivot"], rot_matrix(*rot))
            pos = ps.get("position")
            if pos:
                p = (p[0] + pos[0], p[1] + pos[1], p[2] + pos[2])
            name = b.get("parent")
        return p

    cy, sy = math.cos(math.radians(yaw)), math.sin(math.radians(yaw))
    cp, sp = math.cos(math.radians(pitch)), math.sin(math.radians(pitch))

    def project(p):
        x1 = p[0] * cy + p[2] * sy
        z1 = -p[0] * sy + p[2] * cy
        y2 = p[1] * cp + z1 * sp
        z2 = z1 * cp - p[1] * sp
        if center:
            return (W / 2 + (x1 - center[0]) * scale, H / 2 - (y2 - center[1]) * scale, z2)
        return (W / 2 + x1 * scale, H - 14 - y2 * scale, z2)

    img = [[bg] * W for _ in range(H)]
    zbuf = [[1e9] * W for _ in range(H)]

    def tri(a, b, c, ua, ub, uc, rc):
        minx, maxx = int(max(0, math.floor(min(a[0], b[0], c[0])))), int(min(W - 1, math.ceil(max(a[0], b[0], c[0]))))
        miny, maxy = int(max(0, math.floor(min(a[1], b[1], c[1])))), int(min(H - 1, math.ceil(max(a[1], b[1], c[1]))))
        den = (b[1] - c[1]) * (a[0] - c[0]) + (c[0] - b[0]) * (a[1] - c[1])
        if abs(den) < 1e-9:
            return
        for y in range(miny, maxy + 1):
            row, zrow = img[y], zbuf[y]
            for x in range(minx, maxx + 1):
                px, py = x + 0.5, y + 0.5
                l1 = ((b[1] - c[1]) * (px - c[0]) + (c[0] - b[0]) * (py - c[1])) / den
                l2 = ((c[1] - a[1]) * (px - c[0]) + (a[0] - c[0]) * (py - c[1])) / den
                l3 = 1 - l1 - l2
                if l1 < -1e-4 or l2 < -1e-4 or l3 < -1e-4:
                    continue
                z = l1 * a[2] + l2 * b[2] + l3 * c[2]
                if z >= zrow[x]:
                    continue
                u = l1 * ua[0] + l2 * ub[0] + l3 * uc[0]
                v = l1 * ua[1] + l2 * ub[1] + l3 * uc[1]
                tx = min(max(int(u), rc[0]), rc[0] + rc[2] - 1)
                ty = min(max(int(v), rc[1]), rc[1] + rc[3] - 1)
                col = tex[ty][tx]
                if col[3] < 128:
                    continue
                zrow[x] = z
                row[x] = col

    for b in bones.values():
        for cu in b.get("cubes", []):
            inf = cu.get("inflate", 0)
            x0, y0, z0 = (cu["origin"][i] - inf for i in range(3))
            x1, y1, z1 = (cu["origin"][i] + cu["size"][i] + inf for i in range(3))
            quad = {
                "north": ((x0, y1, z0), (x1, y1, z0), (x1, y0, z0), (x0, y0, z0)),
                "south": ((x1, y1, z1), (x0, y1, z1), (x0, y0, z1), (x1, y0, z1)),
                "east": ((x0, y1, z1), (x0, y1, z0), (x0, y0, z0), (x0, y0, z1)),
                "west": ((x1, y1, z0), (x1, y1, z1), (x1, y0, z1), (x1, y0, z0)),
                "up": ((x0, y1, z1), (x1, y1, z1), (x1, y1, z0), (x0, y1, z0)),
                "down": ((x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)),
            }
            for face, pts in quad.items():
                if "rotation" in cu:
                    R = rot_matrix(*cu["rotation"])
                    pts = tuple(rot_about(p, cu["pivot"], R) for p in pts)
                pts = [project(bone_pt(b["name"], p)) for p in pts]
                (ru, rv), (rw, rh) = cu["uv"][face]["uv"], cu["uv"][face]["uv_size"]
                uvs = ((ru, rv), (ru + rw, rv), (ru + rw, rv + rh), (ru, rv + rh))
                tri(pts[0], pts[1], pts[2], uvs[0], uvs[1], uvs[2], (ru, rv, rw, rh))
                tri(pts[0], pts[2], pts[3], uvs[0], uvs[2], uvs[3], (ru, rv, rw, rh))
    return img


def preview(path, name, model, tex, anims, speak="sing", head_center=(0, 22.5), body_center=(0, 15.5), face_views=None,
            main_views=None, scale=13):
    bg = (44, 48, 44, 255)
    A = anims["animations"]
    g = lambda n: A[f"animation.{name}.{n}"]
    W = H = 460
    if main_views:                       # a species can pass 8 (animation, time, yaw, pitch) and a pixels-per-unit scale
        views = [(g(n), tm, yaw, pitch) for n, tm, yaw, pitch in main_views]
    else:
        views = [
            (g("idle"), 0, 0, 0), (g("idle"), 0, -35, 12), (g("idle"), 0, 90, 0), (g("idle"), 0, 180, 0),
            (g("fly"), 0.4, -20, 10), (g("fly"), 0.0, 150, 10), (g("walk"), 0.25, -35, 12), (g(speak), 1.0, -30, 10),
        ]
    sheet = [[bg] * (W * 4) for _ in range(H * 2)]
    for i, (an, tm, yaw, pitch) in enumerate(views):
        im = render(model, tex, an, tm, yaw, pitch, W, H, scale, bg)
        ox, oy = (i % 4) * W, (i // 4) * H
        for y in range(H):
            sheet[oy + y][ox:ox + W] = im[y]
    write_png(path, sheet)

    # face close-ups: by default idle, blink, speaking note (front), speaking note (3/4);
    # a species can pass its own list of (animation, time, yaw, pitch)
    FW = FH = 440
    if face_views:
        views = [(g(n), tm, yaw, pitch) for n, tm, yaw, pitch in face_views]
    else:
        views = [(g("idle"), 0, 0, 0), (g("idle"), 2.8, 0, 0), (g(speak), 0.5, 0, 0), (g(speak), 0.5, -30, 8)]
    sheet = [[bg] * (FW * len(views)) for _ in range(FH)]
    for i, (an, tm, yaw, pitch) in enumerate(views):
        im = render(model, tex, an, tm, yaw, pitch, FW, FH, 38, bg, center=head_center)
        for y in range(FH):
            sheet[y][i * FW:(i + 1) * FW] = im[y]
    write_png(os.path.splitext(path)[0] + "_face.png", sheet)

    # torso close-ups: front, 3/4, side
    BW = BH = 440
    sheet = [[bg] * (BW * 3) for _ in range(BH)]
    for i, (yaw, pitch) in enumerate(((0, 0), (-40, 6), (90, 0))):
        im = render(model, tex, g("idle"), 0, yaw, pitch, BW, BH, 30, bg, center=body_center)
        for y in range(BH):
            sheet[y][i * BW:(i + 1) * BW] = im[y]
    write_png(os.path.splitext(path)[0] + "_body.png", sheet)


# ----------------------------------------------------------------------------- runner
def run(name, paint_fn, model_fn, anims_fn, speak="sing", modid="monstergirls", **preview_kw):
    """paint_fn() -> texture rows; model_fn() -> Model; anims_fn(Anims) fills the animation set"""
    pack()
    tex = paint_fn()
    model = model_fn().doc()
    names = [b["name"] for b in model["minecraft:geometry"][0]["bones"]]
    an = Anims(name, names)
    anims_fn(an)
    anims = an.doc()
    out_geo = os.path.join(REPO, "src", "main", "resources", "assets", modid, "geo", "entity", name + ".geo.json")
    out_anim = os.path.join(REPO, "src", "main", "resources", "assets", modid, "animations", "entity", name + ".animation.json")
    out_tex = os.path.join(REPO, "src", "main", "resources", "assets", modid, "textures", "entity", name + ".png")
    write_png(out_tex, tex)
    for path, data in ((out_geo, model), (out_anim, anims)):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            json.dump(data, fh, indent=2)
            fh.write("\n")
    bones = model["minecraft:geometry"][0]["bones"]
    n_cubes = sum(len(b.get("cubes", [])) for b in bones)
    used = sum(w * h for (_, _, w, h) in RECT.values())
    print(f"{name}: texture {TEX}x{TEX} ({used / TEX / TEX:.0%} used), {len(bones)} bones / {n_cubes} cubes, {len(anims['animations'])} animations")
    if "--preview" in sys.argv:
        out = sys.argv[sys.argv.index("--preview") + 1]
        preview(out, name, model, tex, anims, speak, **preview_kw)
        write_png(os.path.splitext(out)[0] + "_texture.png", upscale(tex, 2))
        print("preview ->", out)
