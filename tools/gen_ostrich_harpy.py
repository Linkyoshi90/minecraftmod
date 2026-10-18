#!/usr/bin/env python3
"""Ostrich Harpy - species definition. The shared engine lives in harpy_lib.py.

    python tools/gen_ostrich_harpy.py                 # write the three asset files
    python tools/gen_ostrich_harpy.py --preview x.png # also render preview sheets (x.png, x_face.png, x_body.png)

Field guide: "Flightless harpies, fast on land, hurt when angered, extremely territorial, but loyal when
cared for. Wary, proud, territorial. Runs quickly, patrols familiar ground, and uses warning displays
before charging when a nest or territory is threatened."

Design: the tallest harpy (about 2.3 blocks). Very long, powerful legs with two-toed feet and one big claw;
a long neck; small vestigial wings with white plumes that spread wide in the warning display; a big white
tail fan; black feather bodice, tan savanna leather and cloth with bone and turquoise beads; sandy windswept
hair with a few black-and-white plumes; big lashed eyes under haughty brows; a stern default mouth.
Expressions: blink, hiss (open mouth) and angry brows slide out of the face like the crow's.
"""
import copy
import math
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

NAME = "ostrich_harpy"
L = 6.0                  # everything above the shins sits this much higher than the sparrow (legs are far longer)
N = 2.0                  # extra neck height
HEAD = L + N

# ----------------------------------------------------------------------------- palette
PAL = H.Pal()
# ostrich colours: black and white plumage, pinkish-grey bare skin, blue-grey accents, grey-brown downy head feathers
PAL.HAIR = ramp([rgb(50, 44, 40), rgb(84, 74, 66), rgb(122, 106, 92), rgb(160, 142, 124), rgb(196, 178, 156), rgb(226, 212, 190)])
PAL.SKIN = ramp([rgb(156, 112, 106), rgb(188, 144, 134), rgb(214, 172, 158), rgb(230, 194, 178)], nodither=True)
PAL.FEA = ramp([rgb(14, 12, 12), rgb(32, 28, 26), rgb(64, 58, 54), rgb(110, 104, 98), rgb(170, 164, 156), rgb(226, 222, 214), rgb(250, 248, 244)])
PAL.EYE = ramp([rgb(40, 26, 18), rgb(70, 46, 28), rgb(108, 72, 40), rgb(148, 102, 56), rgb(190, 142, 84)])
PAL.LEG = ramp([rgb(112, 84, 84), rgb(150, 118, 116), rgb(186, 152, 148), rgb(214, 184, 178), rgb(236, 212, 206)])
PAL.TALON = [rgb(28, 22, 20), rgb(72, 60, 54)]
PAL.EYE_OUT, PAL.PUPIL = rgb(30, 20, 16), rgb(16, 10, 8)
PAL.BLUSH, PAL.BLUSH_L = rgb(212, 124, 126), rgb(226, 152, 150)
PAL.LIP, PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE = rgb(170, 92, 84), rgb(120, 60, 58), rgb(80, 26, 30), rgb(206, 100, 100)
PAL.mouth_edge = (rgb(70, 32, 34), 0.80)
PAL.BROW = PAL.HAIR[1]
PAL.eye_a, PAL.eye_b, PAL.eye_slant = 4.2, 6.2, 0.0         # big round eyes ...
PAL.pupil, PAL.hl_dy, PAL.hl2_dy, PAL.lid_r = (2.0, 3.4), 4, 3, (5.2, 6.9)
PAL.lash_extra = [(5, 16), (6, 15), (7, 14), (7, 13), (6, 12)]   # ... with long, sweeping lashes
PAL.brows = [(8, 9, -4, 3, 0.0, 2), (24, 9, -4, 3, 0.0, 2)]       # strong, level, haughty
PAL.mouth_px = {(x, 28): PAL.LIP for x in range(13, 19)}          # proud and composed: a flat line
PAL.wing_base_t, PAL.tail_fluff_t = (0.05, 0.20), 0.90

LEATHER = ramp([rgb(30, 24, 22), rgb(58, 48, 42), rgb(92, 76, 64), rgb(130, 112, 96)])     # near-black brown
CLOTH = ramp([rgb(48, 44, 44), rgb(86, 78, 74), rgb(128, 116, 108), rgb(170, 156, 144)])     # dusty grey-brown
BONE = ramp([rgb(176, 166, 148), rgb(220, 212, 194), rgb(250, 246, 236)])
TURQ = ramp([rgb(54, 80, 110), rgb(96, 134, 170), rgb(170, 204, 228)])                       # the ostrich's blue-grey (name kept)

# ----------------------------------------------------------------------------- colour variants (texture only)
# Each variant bundles a palette with outfit ramps. `shift` darkens the wing/tail plumes by that many ramp steps
# (0 = white plumes, 2 = shaggy mid-tone plumes, 4 = black plumes); `neck`/`wattle` recolour the bare neck.
DEFAULT = SimpleNamespace(pal=PAL, leather=LEATHER, cloth=CLOTH, bone=BONE, accent=TURQ, neck=None, wattle=None, shift=0)


def variant(hair, skin, fea, eye, leg, leather, cloth, accent, shift, gain=1.0, off=0.0, neck=None, wattle=None, pupil=None):
    rg = lambda colors, **kw: ramp([rgb(*c) for c in colors], **kw)        # (r, g, b) tuples -> opaque RGBA ramp
    p = copy.copy(PAL)
    p.HAIR, p.SKIN, p.FEA, p.EYE, p.LEG = rg(hair), rg(skin, nodither=True), rg(fea), rg(eye), rg(leg)
    p.BROW = p.HAIR[1]
    p.hair_gain, p.hair_off = gain, off
    if pupil:
        p.PUPIL = rgb(*pupil)
    return SimpleNamespace(pal=p, leather=rg(leather), cloth=rg(cloth), bone=BONE, accent=rg(accent), shift=shift,
                           neck=rg(neck, nodither=True) if neck else None, wattle=rg(wattle) if wattle else None)


VARIANTS = {
    # female common ostrich: grey-brown plumage with pale grey-brown plumes, sand-coloured cloth, rust beads
    "hen": variant(
        hair=[(58, 48, 40), (92, 76, 62), (128, 108, 88), (164, 142, 116), (200, 178, 148), (228, 210, 180)],
        skin=[(150, 110, 96), (182, 142, 124), (208, 170, 150), (226, 192, 170)],
        fea=[(46, 40, 36), (78, 68, 60), (112, 98, 86), (148, 132, 116), (184, 168, 150), (214, 202, 184), (240, 232, 218)],
        eye=[(40, 26, 18), (70, 46, 28), (108, 72, 40), (148, 102, 56), (190, 142, 84)],
        leg=[(110, 92, 86), (146, 126, 118), (182, 160, 150), (210, 190, 180), (232, 216, 208)],
        leather=[(44, 32, 24), (78, 58, 42), (112, 86, 62), (150, 120, 88)],
        cloth=[(70, 60, 50), (108, 94, 78), (148, 132, 112), (188, 172, 150)],
        accent=[(110, 64, 30), (170, 104, 46), (214, 156, 84)], shift=2),
    # emu: shaggy dark-brown plumage, pale blue-grey bare neck skin, orange eyes, dark grey legs, emu-egg green accents
    "emu": variant(
        hair=[(30, 24, 20), (52, 42, 34), (80, 66, 52), (110, 94, 72), (144, 126, 98), (180, 160, 128)],
        skin=[(146, 124, 128), (180, 158, 158), (208, 190, 188), (228, 214, 210)],
        fea=[(30, 24, 20), (58, 46, 38), (92, 74, 60), (126, 104, 84), (160, 136, 112), (196, 172, 144), (224, 206, 178)],
        eye=[(120, 60, 16), (170, 92, 22), (214, 128, 34), (240, 166, 60), (252, 200, 110)],
        leg=[(70, 66, 66), (100, 94, 92), (134, 126, 122), (168, 160, 154), (200, 192, 186)],
        leather=[(30, 24, 20), (56, 46, 38), (88, 74, 60), (124, 106, 88)],
        cloth=[(26, 44, 40), (44, 74, 66), (70, 108, 96), (104, 142, 126)],
        accent=[(30, 70, 56), (54, 114, 90), (120, 190, 160)], shift=2,
        neck=[(110, 130, 150), (146, 166, 184), (176, 196, 210), (204, 220, 230)]),
    # cassowary: glossy blue-black plumage, vivid blue neck with red wattles, red-brown eyes, pale yellow-grey legs
    "cassowary": variant(
        hair=[(10, 10, 16), (24, 24, 38), (42, 44, 68), (66, 70, 104), (100, 106, 148), (150, 156, 196)], gain=0.7, off=-0.02,
        skin=[(150, 100, 100), (184, 136, 128), (212, 170, 156), (232, 196, 182)],
        fea=[(8, 8, 12), (18, 18, 28), (32, 34, 52), (52, 58, 88), (86, 96, 136), (150, 160, 196), (210, 216, 236)],
        eye=[(60, 20, 12), (110, 36, 16), (160, 60, 24), (204, 96, 40), (236, 140, 80)],
        leg=[(96, 88, 66), (136, 126, 92), (176, 164, 120), (206, 194, 150), (230, 220, 180)],
        leather=[(14, 12, 12), (30, 26, 26), (54, 46, 44), (86, 76, 72)],
        cloth=[(14, 16, 30), (28, 34, 64), (46, 58, 100), (76, 92, 140)],
        accent=[(110, 20, 24), (176, 36, 40), (230, 90, 84)], shift=4,
        neck=[(26, 56, 120), (44, 92, 170), (84, 138, 208), (132, 180, 232)],
        wattle=[(100, 14, 20), (160, 26, 32), (212, 52, 50), (240, 100, 86)]),
    # rhea: soft grey plumage, pale grey-brown plumes, pale blue-grey cloth
    "rhea": variant(
        hair=[(60, 56, 52), (96, 90, 84), (132, 124, 116), (166, 158, 148), (200, 192, 180), (228, 222, 208)],
        skin=[(166, 138, 132), (196, 170, 162), (220, 198, 188), (238, 220, 208)],
        fea=[(54, 54, 58), (88, 88, 92), (122, 122, 126), (158, 156, 158), (196, 194, 194), (226, 224, 222), (246, 244, 240)],
        eye=[(52, 40, 28), (88, 66, 40), (126, 96, 56), (166, 130, 80), (204, 170, 112)],
        leg=[(120, 112, 108), (156, 148, 142), (188, 180, 174), (214, 206, 200), (234, 228, 222)],
        leather=[(40, 34, 32), (70, 60, 56), (104, 92, 84), (142, 128, 118)],
        cloth=[(60, 70, 82), (98, 110, 126), (140, 154, 170), (184, 196, 208)],
        accent=[(120, 100, 60), (176, 150, 96), (220, 200, 150)], shift=2),
}


# ----------------------------------------------------------------------------- parts
def declare_parts():
    H.standard_parts()
    for gone in ("fluff", "toe1", "toe2", "talon", "htoe", "htalon", "ahoge", "ahoge_tip"):
        H.PARTS.pop(gone)
    for args in [
        # heavier build: broad ribcage and hips (waist stays slender), big powerful thighs and calves
        ("hips", 6, 1.25, 3.75, ("up", "down")), ("chest", 6, 3.25, 3.75), ("bust", 2.5, 2, 1.25), ("collar", 4.5, 1, 3.25),
        ("sash", 6.5, 1.25, 4.25, ("up", "down")), ("back_cover", 4.5, 4, .5), ("leaf_side", .5, 3.5, 2.5),
        ("thigh", 3.75, 3, 3.75), ("thigh_low", 3.25, 2.5, 3.25), ("calf", 3, 4.5, 3), ("shin", 2.25, 7 + L, 2.25),
        ("ankle", 2.25, 1, 2.25), ("heel", 2.5, 1, 2.5),
        ("arm_up", 1.75, 3.5, 1.75), ("forearm", 1.75, 3, 1.75),
        ("neck", 2.5, 2, 2.5), ("choker", 3, .75, 3),
        ("tuft", 2.25, 3, 1.75), ("hfeather", 1.25, 5.5, .5),
        ("bang_a", 2, 2, .5), ("bang_c", 2, 2.5, .5), ("bang_b", 2, 3.5, .5),     # short, so the brows show
        ("strand_xl1", 1.5, 17, 1), ("strand_xl2", 1.5, 17, 1),                    # hip-length hair
        # small wings
        ("wing_base", 3, 6, .5), ("cov_a", 1.75, 2.5, .5), ("cov_b", 1.75, 2.5, .5), ("cov_c", 1.75, 2.5, .5),
        ("sec_a", 2, 5, .5), ("sec_b", 2, 5, .5), ("prim_a", 2, 6.5, .5), ("prim_b", 2, 6.5, .5),
        ("tail_f", 2, 6.5, .5), ("tail_g", 2, 6.5, .5),
        # two-toed foot
        ("toe_big", 1.5, 1.25, 4), ("claw_big", 1.5, .75, 2.5), ("toe_small", 1, 1, 3), ("claw_small", 1, .5, 1.5),
        # expression parts: hidden just inside the face, slid out ~0.4 to sit almost flush with it
        ("eyelids", 6.5, 3.5, .125), ("mouth", 2.25, 1.75, .125), ("brows", 6.5, 2, .125),
    ]:
        H.part(*args)


# ----------------------------------------------------------------------------- texture
def paint(v=None):
    v = v or DEFAULT
    pt = H.Painter()
    pal, FEA, SKIN = v.pal, v.pal.FEA, v.pal.SKIN
    LEATHER, CLOTH, BONE, TURQ = v.leather, v.cloth, v.bone, v.accent       # shadow the module-level defaults
    D = H.D
    H.paint_head(pt, pal)
    H.paint_hair(pt, pal)
    sh = lambda *ix: tuple(max(0, i - v.shift) for i in ix)                   # darken plumes for the non-white variants
    H.paint_wings_tail(pt, pal, {                       # black coverts, white (or shaggier) plumes
        "sec_a": sh(5, 6, 6), "sec_b": sh(4, 5, 6), "prim_a": sh(5, 6, 6), "prim_b": sh(4, 6, 6),
        "tail_f": sh(5, 6, 6), "tail_g": sh(4, 5, 6),
        "cov_a": (0, 1, 2, None, True), "cov_b": (1, 2, 3, None, True), "cov_c": (2, 3, 4, None, True),
    })
    H.paint_body_skin(pt, pal)
    H.paint_legs(pt, pal)                               # shin / ankle / heel (toes are painted below)

    # white plume details
    for p, spec in (("tassel", (5, 6, 6)), ("cuff_f", (5, 6, 6)), ("trim", (5, 6, 6)), ("hfeather", (1, 3, 6))):
        pt(p, "all", H.feather_fn(pal, *spec, None, False, False))
    pt("back_cover", "all", lambda f, x, y, w, h: pick(FEA, 0.10 + 0.20 * y / h + (noise(x, y, 19) - 0.5) * 0.12 + (0.14 if (x // 3 + y // 3) % 4 == 0 else 0), x, y))

    # black feather bodice with a white fringe
    def bodice(f, x, y, w, h):
        if f == "up":
            return pick(SKIN, 0.66, x, y)
        if f == "down":
            return pick(SKIN, 0.5, x, y)
        if y < D // 2:
            return pick(FEA, 0.30 + (noise(x, y, 29) - 0.5) * 0.2, x, y)            # dark grey top edge
        if y >= h - 2 and (x + y) % 2 == 0:
            return FEA[5]                                                            # a light white-flecked hem
        scallop = 0.10 if ((y // 3) + (x // 3)) % 2 else 0.0
        t = 0.10 + scallop + (noise(x, y, 31) - 0.5) * 0.12 + (0.06 if f in ("north",) and x == w // 2 else 0)
        if noise(x // 2, y // 2, 33) > 0.9:
            t += 0.22                                                                # a few grey barbs
        return pick(FEA, t, x, y)
    pt("chest", "all", bodice)
    pt("bust", "all", bodice)

    # savanna leather belt with bone and turquoise beads
    def belt(f, x, y, w, h):
        if y < 2 or y >= h - 2:
            return LEATHER[0]
        if y == h // 2 and x % (D * 2) == D:
            return TURQ[2] if (x // (D * 2)) % 2 else BONE[2]
        return pick(LEATHER, 0.50 + 0.10 * (((x + y) % 4) < 2) + (noise(x, y, 37) - 0.5) * 0.12, x, y)
    pt("sash", "all", belt)

    # ochre cloth strips with a dark zig-zag border and bead-weighted hem
    def cloth(broad):
        def fn(f, x, y, w, h):
            if f not in broad:
                return CLOTH[1]
            r = h - 1 - y
            tl = 1.4 * D
            if r < tl:
                half = (w / 2.0) * (min(1.0, (r + 1) / tl) ** 0.5)
                if abs(x + 0.5 - w / 2.0) > half or (r < D // 2 and noise(x, y, 41) > 0.55):
                    return CLEAR
            if y < D:
                return LEATHER[2] if (x // 2) % 2 else LEATHER[1]
            zig = abs((x % 6) - 3)
            if D + 2 <= y <= D + 4 and (y - (D + 2)) == zig // 2:
                return BONE[1]                                   # white zig-zag border on the grey cloth
            if y == h - 2 and x % 4 == 1:
                return TURQ[1]                                   # hem beads
            t = 0.55 + (noise(x, y, 43) - 0.5) * 0.22
            if x % 5 == 0:
                t -= 0.10
            return pick(CLOTH, t, x, y)
        return fn
    for p in ("leaf_s", "leaf_m", "leaf_l"):
        pt(p, "all", cloth(BROAD))
    pt("leaf_side", "all", cloth(("east", "west")))

    def bracer(f, x, y, w, h):
        if f in ("up", "down"):
            return LEATHER[1]
        if y >= h - 2:
            return LEATHER[0]
        if y in (h // 2 - 1, h // 2):
            return BONE[1] if (x // 2) % 2 else LEATHER[0]       # white thread stripes
        return pick(LEATHER, 0.55 + (noise(x, y, 47) - 0.5) * 0.25, x, y)
    pt("bracer", "all", bracer)

    pt("pendant", "all", lambda f, x, y, w, h: pick(BONE, 0.9 - 0.5 * (x > w // 2), x, y))
    pt("cord", "all", lambda f, x, y, w, h: LEATHER[0])
    def neck(f, x, y, w, h):
        t = 0.66 - (0.10 if f in ("east", "west", "south") else 0)
        if v.wattle and f in ("north", "east", "west") and y >= h * 0.5:
            return pick(v.wattle, 0.55 + (noise(x, y, 71) - 0.5) * 0.2, x, y)      # cassowary's red wattles
        return pick(v.neck or SKIN, t, x, y)
    pt("neck", "all", neck)
    pt("choker", "all", lambda f, x, y, w, h: TURQ[2] if (f == "north" and w // 2 - 1 <= x <= w // 2 and 1 <= y < h - 1) else pick(LEATHER, 0.45 + (noise(x, y, 65) - 0.5) * 0.2, x, y))

    # angry brows for the display and the kick: inner ends dragged down, on an opaque skin strip that hides the calm ones
    def brow_hit(x, y):
        for xb in (x, 25 - x):
            if 1 <= xb <= 9:
                yl = 1 + int(round(0.7 * (xb - 1)))
                if yl <= y <= yl + 1 + (1 if xb < 5 else 0):
                    return True
        return False
    pt("brows", "all", lambda f, x, y, w, h: pal.HAIR[1] if (f == "north" and brow_hit(x, y)) else pick(SKIN, 0.70, x, y))

    # muscular legs: banded shading gives the thigh and calf a visible muscle shape
    def muscle(base):
        def fn(f, x, y, w, h):
            c = abs((x + 0.5) / w - 0.5) * 2               # 0 centre .. 1 edge
            t = base + 0.16 * (1 - c * c) - 0.10 * (y / h) + (noise(x, y, 67) - 0.5) * 0.05
            if f in ("east", "west"):
                t -= 0.08
            elif f in ("south", "down"):
                t -= 0.12
            return pick(SKIN, t, x, y)
        return fn
    pt("thigh", "all", muscle(0.66))
    pt("thigh_low", "all", muscle(0.60))

    def calf(f, x, y, w, h):
        c = abs((x + 0.5) / w - 0.5) * 2
        t = 0.62 + 0.20 * (1 - c * c) - 0.12 * (y / h) + (noise(x, y, 69) - 0.5) * 0.06
        if f in ("east", "west"):
            t -= 0.08
        elif f in ("south", "down"):
            t -= 0.10
        return pick(pal.LEG, t, x, y)
    pt("calf", "all", calf)

    # two-toed foot: one big toe with a heavy claw, one small outer toe
    def toe(f, x, y, w, h):
        if (f in ("east", "west", "up") and x % 6 == 5) or (f in BROAD and y == h - 1):
            return pick(pal.LEG, 0.20, x, y)
        return pick(pal.LEG, 0.58 + (noise(x, y, 61) - 0.5) * 0.14, x, y)
    pt("toe_big", "all", toe)
    pt("toe_small", "all", toe)
    for p in ("claw_big", "claw_small"):
        pt(p, "all", lambda f, x, y, w, h: pal.TALON[1] if (f in ("up", "east", "west") and y == 0) else pal.TALON[0])
    return pt.T


# ----------------------------------------------------------------------------- geometry
def C(p, origin, inflate=None, pivot=None, rotation=None, lift=L):
    """cube lifted by `lift` (origin and pivot)"""
    pv = None if pivot is None else [pivot[0], pivot[1] + lift, pivot[2]]
    return cube(p, [origin[0], origin[1] + lift, origin[2]], inflate, pv, rotation)


def HG(p, x, ytop, z, rz=0, rx=0, lift=L):
    return hang(p, x, ytop + lift, z, rz, rx)


def CH(*a, **k):                  # head-level cube (above the neck)
    return C(*a, lift=HEAD, **k)


def HGH(*a, **k):
    return HG(*a, lift=HEAD, **k)


def build_model():
    m = H.Model(NAME)
    bone = m.bone
    bone("root", None, [0, 0, 0])

    # ---- torso: slender waist, slight bust under a feather bodice, long neck with a beaded choker, bone pendant
    body = [
        C("hips", [-3.0, 12, -1.875]), C("midriff", [-1.75, 13.25, -1.25]), C("chest", [-3.0, 15.75, -1.875]),
        C("collar", [-2.25, 19, -1.625]), C("neck", [-1.25, 20, -1.25]), C("choker", [-1.5, 20.1, -1.5]),
        C("sash", [-3.25, 12.6, -2.125]), C("back_cover", [-2.25, 15.5, 1.875]),
        C("pendant", [-0.5, 17.0, -3.4]), C("cord", [-1.5, 18.55, -3.0]),
    ]
    bust = C("bust", [-2.75, 16.5, -3.125])
    body += [bust, mirror_cube(bust)]
    for i, x in enumerate((-2.75, -1.75, -0.75, 0.25, 1.25, 2.0)):
        body.append(HG("trim", x, 15.9, -2.75, 6 if i % 2 else -6))
    bone("body", "root", [0, 12 + L, 0], body)

    # ---- loincloth: ochre strips of different lengths with white plume tassels at the hips
    sk = []
    front = [("leaf_m", -2.2, 3), ("leaf_s", -0.7, -2), ("leaf_m", 0.7, 2)]     # a short centre apron: the powerful thighs show at the sides
    for i, (p, x, rz) in enumerate(front):
        sk.append(HG(p, x, 13.7, -2.95 - 0.15 * (i % 2), rz, -9))
    for i, (p, x, rz) in enumerate((("leaf_s", -2.0, 3), ("leaf_m", 0.0, -2), ("leaf_s", 1.6, 3))):
        sk.append(HG(p, x, 13.7, 2.4 + 0.15 * (i % 2), rz, 9))
    sk.append(HG("tassel", 3.0, 13.4, -2.9, 6, -4))
    sk.append(HG("tassel", -4.0, 13.4, -2.8, -6, -4))
    bone("skirt", "body", [0, 13.7 + L, 0], sk)

    # ---- head: windswept sandy hair, big crown fluff, swept bangs clear of the eyes, two plumes
    head = [CH("head", [-4, 20, -4]), CH("hair_cap", [-4, 20, -4], inflate=0.75)]
    ear_r = CH("ear", [-5.0, 21.7, -1.2])
    head += [ear_r, mirror_cube(ear_r)]
    for i, (p, x, rz) in enumerate((("bang_e", -5.0, -6), ("bang_a", -3.1, 4), ("bang_a", -1.4, -3), ("bang_c", 0.2, 4),
                                    ("bang_a", 1.7, -4), ("bang_b", 3.2, 7))):
        head.append(HGH(p, x, 28.4, -5.35 + 0.1 * (i % 2), rz))
    head.append(HGH("frame", -4.9, 28, -5.0, -4))
    head.append(mirror_cube(head[-1]))
    for x, z in ((-2.6, -1.0), (0.2, -2.4), (2.6, -0.6), (-2.2, 1.6), (0.4, 1.4), (2.4, 2.2), (-0.6, 3.0)):   # big fluffy crown
        head.append(CH("tuft", [x - 1.1, 28.2, z - 0.9], pivot=[x, 28.2, z], rotation=[-z * 6, 0, -x * 6]))
    head.append(HGH("hfeather", 4.8, 27.0, -2.6, -14))
    head.append(HGH("hfeather", 5.6, 26.2, -2.3, 10))
    bone("head", "body", [0, 20 + HEAD, 0], head)

    bone("eyelids", "head", [0, 23.5 + HEAD, -3.65], [CH("eyelids", [-3.25, 21.75, -3.7])])
    bone("mouth", "head", [0, 21 + HEAD, -3.65], [CH("mouth", [-1.125, 20.25, -3.7])])
    bone("brows", "head", [0, 25.5 + HEAD, -3.65], [CH("brows", [-3.25, 24.75, -3.7])])

    # land harpies let their hair grow: long locks frame the face, the back hair reaches the hips
    side = [("strand_m1", -6.1, 26.2, -3.4, -6, 0), ("strand_l2", -6.3, 25.2, -2.0, -9, 0),
            ("strand_l1", -6.2, 26.2, 0.8, -7, 3), ("strand_l2", -6.0, 26.2, 2.4, -10, 4)]
    bone("hair_side_r", "head", [-4.7, 27 + HEAD, 0], [HGH(*s) for s in side], mirror=True)
    back = []
    names = ["strand_l1", "strand_xl1", "strand_xl2", "strand_xl1", "strand_l2"]     # longest in the middle
    for i in range(11):
        x = -4.8 + 0.96 * i
        back.append(HGH(names[i % 5], x, 26.8, 5.3 + 0.3 * (i % 3), x * 1.4, 6 + 2 * (i % 3)))
    bone("hair_back", "head", [0, 27 + HEAD, 4.5], back)

    # ---- arms (right side; left is mirrored)
    bone("arm_r", "body", [-3.75, 19.5 + L, 0], [C("arm_up", [-4.625, 16, -0.875])], mirror=True)
    bone("forearm_r", "arm_r", [-3.75, 16 + L, 0], [
        C("forearm", [-4.625, 13, -0.875]), C("bracer", [-5.0, 13.6, -1.25]),
        HG("cuff_f", -4.65, 13.8, -1.45, 4, -6), HG("cuff_f", -3.75, 13.8, -1.45, -4, -6), HG("cuff_f", -4.15, 13.8, 1.0, 0, 6),
    ], mirror=True)
    bone("hand_r", "forearm_r", [-3.75, 13 + L, 0], [
        C("palm", [-4.5, 11.5, -0.5]), C("fingers", [-4.5, 10.5, -0.5]), C("thumb", [-3.0, 11.5, -0.25]),
    ], mirror=True)

    # ---- small vestigial wings: black coverts, white plumes that fan wide in the display
    w = [C("wing_base", [-5.5, 13.5, 2.3])]
    rows = [(19.5, (-5.5, -3.75), ("cov_a", "cov_a")), (17.2, (-6.25, -4.5), ("cov_a", "cov_b")),
            (14.9, (-5.5, -3.75), ("cov_c", "cov_c"))]
    for r, (ytop, xs, ps) in enumerate(rows):
        for x, p in zip(xs, ps):
            w.append(C(p, [x, ytop - 2.5, 3.3 + 0.2 * r]))
    for i, (x, ang) in enumerate(zip((-7.5, -6.0, -4.5, -3.0), (-9, -6, -3, 0))):
        w.append(HG("sec_a" if i % 2 == 0 else "sec_b", x, 15.5, 2.3 + 0.25 * (i % 2), ang))
    bone("wing_r", "body", [-2.5, 19 + L, 2.3], w, mirror=True)
    pr = []
    for i, (x, ang) in enumerate(zip((-9.0, -7.25, -5.5, -3.75), (-20, -14, -8, -2))):
        pr.append(HG("prim_a" if i % 2 == 0 else "prim_b", x, 15.5, 2.8 + 0.25 * (i % 2), ang))
    bone("wing_tip_r", "wing_r", [-6, 15.5 + L, 3.0], pr, mirror=True)

    # ---- tail: a big white plume fan
    tail = [C("tail_fluff", [-1.75, 12.2, 2.2])]
    for i, ang in enumerate((-26, -17, -8, 0, 8, 17, 26)):
        x = -1.0 + 1.3 * (i - 3)
        tail.append(HG("tail_f" if i % 2 == 0 else "tail_g", x, 12.7, 3.4 + 0.25 * (i % 2), ang, 18))
    bone("tail", "body", [0, 13.2 + L, 3], tail)

    # ---- legs: long and powerful, two-toed feet
    cx = -2.0
    bone("leg_r", "root", [cx, 12 + L, 0], [C("thigh", [cx - 1.875, 10.5, -1.875]), C("thigh_low", [cx - 1.625, 8.0, -1.625])], mirror=True)
    bone("shin_r", "leg_r", [cx, 9 + L, 0], [
        cube("shin", [cx - 1.125, 2, -1.125]), cube("calf", [cx - 1.5, 10.0, -1.1]), cube("ankle", [cx - 1.125, 1, -1.125]),
    ], mirror=True)
    foot = [cube("heel", [cx - 1.25, 0, -1.25])]
    foot += [cube("toe_big", [cx - 0.25, 0, -5.25]), cube("claw_big", [cx - 0.25, 0, -7.75], pivot=[cx + .5, .75, -5.25], rotation=[22, 0, 0])]
    foot += [cube("toe_small", [cx - 1.75, 0, -4.25]), cube("claw_small", [cx - 1.75, 0, -5.75], pivot=[cx - 1.25, .5, -4.25], rotation=[18, 0, 0])]
    bone("foot_r", "shin_r", [cx, 1, 0], foot, mirror=True)
    return m


# ----------------------------------------------------------------------------- animations
def animations(an):
    for f in (H.std_idle, H.std_walk):
        f(an)
    OUT, REST = H.OUT_, H.REST
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold

    # run: the fast charge - long strides, body leaning into it, wings out for balance
    stride = T((0, (45, 0, 0)), (0.25, (-45, 0, 0)), (0.5, (45, 0, 0)))
    stride_o = T((0, (-45, 0, 0)), (0.25, (45, 0, 0)), (0.5, (-45, 0, 0)))
    an.add("run", 0.5, True, merge({
        "leg_r": {"rotation": stride}, "leg_l": {"rotation": stride_o},
        "shin_r": {"rotation": T((0, (10, 0, 0)), (0.125, (75, 0, 0)), (0.25, (10, 0, 0)), (0.375, (28, 0, 0)), (0.5, (10, 0, 0)))},
        "shin_l": {"rotation": T((0, (10, 0, 0)), (0.125, (28, 0, 0)), (0.25, (10, 0, 0)), (0.375, (75, 0, 0)), (0.5, (10, 0, 0)))},
        "foot_r": {"rotation": T((0, (-10, 0, 0)), (0.125, (-30, 0, 0)), (0.25, (-5, 0, 0)), (0.5, (-10, 0, 0)))},
        "foot_l": {"rotation": T((0, (-5, 0, 0)), (0.25, (-10, 0, 0)), (0.375, (-30, 0, 0)), (0.5, (-5, 0, 0)))},
        "body": {"position": T((0, (0, 0, 0)), (0.125, (0, 0.9, 0)), (0.25, (0, 0, 0)), (0.375, (0, 0.9, 0)), (0.5, (0, 0, 0))),
                 "rotation": hold(18, 0, 0)},
        "head": {"rotation": T((0, (-16, 0, 0)), (0.25, (-12, 0, 0)), (0.5, (-16, 0, 0)))},
        "arm_r": {"rotation": T((0, (35, 0, 0)), (0.25, (-35, 0, 0)), (0.5, (35, 0, 0)))},
        "arm_l": {"rotation": T((0, (-35, 0, 0)), (0.25, (35, 0, 0)), (0.5, (-35, 0, 0)))},
        "forearm_r": {"rotation": hold(-45, 0, 0)}, "forearm_l": {"rotation": hold(-45, 0, 0)},
        "hair_back": {"rotation": T((0, (42, 0, 0)), (0.25, (36, 0, 0)), (0.5, (42, 0, 0)))},
        "tail": {"rotation": T((0, (-14, 0, 0)), (0.25, (-6, 0, 0)), (0.5, (-14, 0, 0)))},
        "skirt": {"rotation": hold(-10, 0, 0)},
        "brows": {"position": hold(*OUT)},
    },
        sym("hair_side", hold(32, 0, -6)),
        sym("wing", T((0, (0, 0, -30)), (0.125, (0, 0, -38)), (0.25, (0, 0, -30)), (0.375, (0, 0, -38)), (0.5, (0, 0, -30)))),
        sym("wing_tip", hold(0, 0, 12)), sym("hand", hold(-12, 0, 0))))

    # display: the warning - wings thrown wide and quivering, head lowered, hissing, stomping
    quiver = [(0, (0, 0, -72)), (0.1, (0, 0, -80)), (0.2, (0, 0, -72)), (0.3, (0, 0, -80)), (0.4, (0, 0, -72))]
    quiver = quiver + [(0.4 + t_, v) for t_, v in quiver[1:]] + [(0.8 + t_, v) for t_, v in quiver[1:]]
    quiver += [(1.2 + t_, v) for t_, v in quiver[1:5]] + [(1.6 + t_, v) for t_, v in quiver[1:5]]
    quiver = [(round(t_, 3), v) for t_, v in quiver if t_ <= 2.0]
    stomp_r = T((0, (0, 0, 0)), (0.25, (-32, 0, 0)), (0.5, (0, 0, 0)), (2, (0, 0, 0)))
    stomp_l = T((0, (0, 0, 0)), (1.0, (0, 0, 0)), (1.25, (-32, 0, 0)), (1.5, (0, 0, 0)), (2, (0, 0, 0)))
    an.add("display", 2.0, True, merge({
        "body": {"rotation": T((0, (8, 0, -3)), (0.5, (8, 0, 3)), (1.0, (8, 0, -3)), (1.5, (8, 0, 3)), (2.0, (8, 0, -3))),
                 "position": T((0, (0, -0.4, 0)), (1.0, (0, -0.8, 0)), (2.0, (0, -0.4, 0)))},
        "head": {"rotation": T((0, (16, -10, 0)), (0.5, (16, 10, 0)), (1.0, (16, -10, 0)), (1.5, (16, 10, 0)), (2.0, (16, -10, 0)))},
        "leg_r": {"rotation": stomp_r}, "leg_l": {"rotation": stomp_l},
        "shin_r": {"rotation": T((0, (0, 0, 0)), (0.25, (34, 0, 0)), (0.5, (0, 0, 0)), (2, (0, 0, 0)))},
        "shin_l": {"rotation": T((0, (0, 0, 0)), (1.0, (0, 0, 0)), (1.25, (34, 0, 0)), (1.5, (0, 0, 0)), (2, (0, 0, 0)))},
        "tail": {"rotation": T((0, (-32, 0, 0)), (0.2, (-32, 6, 0)), (0.4, (-32, -6, 0)), (0.6, (-32, 6, 0)), (0.8, (-32, -6, 0)), (1.0, (-32, 6, 0)), (2.0, (-32, 0, 0)))},
        "hair_back": {"rotation": hold(-8, 0, 0)},
        "mouth": {"position": hold(*OUT)},
        "brows": {"position": hold(*OUT)},
    },
        {"wing_r": {"rotation": T(*quiver)}, "wing_l": {"rotation": H.mirror(T(*quiver))}},
        sym("wing_tip", T((0, (0, 0, 10)), (1, (0, 0, 16)), (2, (0, 0, 10)))),
        sym("arm", hold(24, 0, 0)), sym("forearm", hold(-20, 0, 0)), sym("hand", hold(-10, 0, 0)),
        sym("hair_side", hold(-10, 0, 8))))

    # kick: wind up, then a powerful straight-legged thrust forward
    an.add("kick", 1.2, True, merge({
        "leg_r": {"rotation": T((0, (0, 0, 0)), (0.25, (36, 0, 0)), (0.4, (-88, 0, 0)), (0.65, (-88, 0, 0)), (0.95, (0, 0, 0)), (1.2, (0, 0, 0)))},
        "shin_r": {"rotation": T((0, (0, 0, 0)), (0.25, (60, 0, 0)), (0.4, (-6, 0, 0)), (0.65, (-6, 0, 0)), (0.95, (0, 0, 0)), (1.2, (0, 0, 0)))},
        "foot_r": {"rotation": T((0, (0, 0, 0)), (0.4, (-24, 0, 0)), (0.65, (-24, 0, 0)), (0.95, (0, 0, 0)), (1.2, (0, 0, 0)))},
        "shin_l": {"rotation": T((0, (0, 0, 0)), (0.25, (12, 0, 0)), (0.65, (12, 0, 0)), (1.0, (0, 0, 0)), (1.2, (0, 0, 0)))},
        "body": {"rotation": T((0, (0, 0, 0)), (0.25, (8, 0, 0)), (0.4, (-16, 0, 0)), (0.65, (-16, 0, 0)), (0.95, (0, 0, 0)), (1.2, (0, 0, 0)))},
        "head": {"rotation": T((0, (0, 0, 0)), (0.4, (10, 0, 0)), (0.65, (10, 0, 0)), (0.95, (0, 0, 0)), (1.2, (0, 0, 0)))},
        "tail": {"rotation": T((0, (0, 0, 0)), (0.4, (-24, 0, 0)), (0.65, (-24, 0, 0)), (0.95, (0, 0, 0)), (1.2, (0, 0, 0)))},
        "brows": {"position": T((0, REST), (0.15, REST), (0.2, OUT), (1.0, OUT), (1.05, REST), (1.2, REST))},
    },
        sym("wing", T((0, (0, 0, -2)), (0.4, (0, 0, -38)), (0.65, (0, 0, -38)), (0.95, (0, 0, -2)), (1.2, (0, 0, -2)))),
        sym("arm", T((0, (0, 0, 2)), (0.4, (28, 0, 0)), (0.65, (28, 0, 0)), (0.95, (0, 0, 2)), (1.2, (0, 0, 2))))))

    # brood: sitting on the nest, legs folded under her, head slowly scanning the territory
    fold = lambda v: T((0, v), (3, v))
    an.add("brood", 3.0, True, merge({
        "body": {"position": T((0, (0, -9.5, 0)), (3, (0, -9.5, 0))), "rotation": hold(4, 0, 0)},
        "leg_r": {"position": fold((0, -9.5, 0)), "rotation": fold((-84, 0, 0))},
        "leg_l": {"position": fold((0, -9.5, 0)), "rotation": fold((-84, 0, 0))},
        "shin_r": {"rotation": fold((150, 0, 0))}, "shin_l": {"rotation": fold((150, 0, 0))},
        "foot_r": {"rotation": fold((-66, 0, 0))}, "foot_l": {"rotation": fold((-66, 0, 0))},
        "head": {"rotation": T((0, (0, 0, 0)), (0.9, (-4, -26, 0)), (1.8, (-4, 26, 0)), (2.7, (0, 0, 0)), (3, (0, 0, 0)))},
        "tail": {"rotation": T((0, (6, 0, 0)), (1.5, (10, 0, 0)), (3, (6, 0, 0)))},
        **H.blink(1.4),
    },
        sym("wing", T((0, (0, 0, 3)), (1.5, (0, 0, 5)), (3, (0, 0, 3)))),
        sym("arm", hold(6, 0, 0)), sym("forearm", hold(-10, 0, 0))))


def main():
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="display",
          head_center=(0, 22.5 + HEAD), body_center=(0, 15.5 + L), scale=10.2,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("run", 0.0, -30, 10), ("display", 0.1, -20, 8), ("kick", 0.55, -50, 6), ("brood", 0.9, -30, 10)],
          # idle, display, kick, blink, display from the side
          face_views=[("idle", 0, 0, 0), ("display", 0.5, 0, 0), ("kick", 0.6, 0, 0), ("idle", 2.8, 0, 0), ("display", 0.5, -28, 8)])
    write_variants()


def write_variants():
    """ostrich_harpy_<variant>.png next to the default texture; --lineup x.png also renders them side by side"""
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "harpies", "textures", "entity")
    texes = {"ostrich": paint()}
    for name, v in VARIANTS.items():
        texes[name] = paint(v)
        H.write_png(os.path.join(tex_dir, f"{NAME}_{name}.png"), texes[name])
    print(f"{NAME}: {len(VARIANTS)} colour variants ({', '.join(VARIANTS)})")
    if "--lineup" in sys.argv:
        lineup(sys.argv[sys.argv.index("--lineup") + 1], texes)


def lineup(path, texes):
    """one column per variant: front, 3/4 running, back (long hair), face close-up"""
    model = build_model().doc()
    an = H.Anims(NAME, [b["name"] for b in model["minecraft:geometry"][0]["bones"]])
    animations(an)
    A = an.doc()["animations"]
    g = lambda n: A[f"animation.{NAME}.{n}"]
    bg, W = (44, 48, 44, 255), 380
    rows = [(g("idle"), 0, -20, 6, 9.0, None), (g("run"), 0.0, -35, 10, 9.0, None), (g("idle"), 0, 180, 0, 9.0, None),
            (g("idle"), 0, -15, 0, 26, (0, 22.5 + HEAD))]
    sheet = [[bg] * (W * len(texes)) for _ in range(W * len(rows))]
    for c, tex in enumerate(texes.values()):
        for r, (anim, tm, yaw, pitch, scale, center) in enumerate(rows):
            im = H.render(model, tex, anim, tm, yaw, pitch, W, W, scale, bg, center=center)
            for y in range(W):
                sheet[r * W + y][c * W:(c + 1) * W] = im[y]
    H.write_png(path, sheet)
    print("lineup ->", path, "(columns:", ", ".join(texes), ")")


if __name__ == "__main__":
    main()
