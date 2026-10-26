#!/usr/bin/env python3
"""Rookmourn Harpy - species definition. The shared engine lives in harpy_lib.py / anuran_lib.py.

    python tools/gen_rookmourn_harpy.py   (see the generator this one is copied from for the options)

Field guide (eleventh wave): a Harmontide rook in a plague doctor's wide hat and beaked mask. She cleanses poison,
nausea, hunger and wither from everyone near her, and cures a zombie villager for a golden apple. The crow harpy's rig,
animations and colours (gen_crow_harpy.py) plus the hat (pd_brim, pd_crown, pd_band) and the mask (pd_beak), bone
`pd_hat`. Keep in sync with RookmournHarpy.Variant.
"""
import copy
import math
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import variant_tools as VT
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

NAME = "rookmourn_harpy"
L = 1.0                                          # everything above the shins sits this much higher than the sparrow

# ----------------------------------------------------------------------------- palette
PAL = H.Pal()
PAL.HAIR = ramp([rgb(12, 10, 20), rgb(26, 24, 40), rgb(44, 42, 68), rgb(70, 70, 106), rgb(108, 110, 150), rgb(156, 158, 196)])
PAL.SKIN = ramp([rgb(190, 146, 132), rgb(216, 178, 162), rgb(236, 206, 190), rgb(248, 226, 212)], nodither=True)
PAL.FEA = ramp([rgb(10, 9, 16), rgb(22, 21, 34), rgb(38, 38, 58), rgb(62, 64, 96), rgb(98, 102, 142), rgb(146, 150, 188), rgb(196, 200, 228)])
PAL.EYE = ramp([rgb(30, 20, 70), rgb(52, 36, 110), rgb(80, 58, 150), rgb(120, 96, 190), rgb(176, 156, 230)])
PAL.LEG = ramp([rgb(12, 12, 18), rgb(22, 22, 34), rgb(32, 34, 50), rgb(48, 52, 76), rgb(72, 78, 112)])
PAL.hair_gain, PAL.hair_off = 0.62, -0.02                    # near-black hair, the violet sheen only on the gloss and tips
PAL.TALON = [rgb(12, 10, 16), rgb(44, 42, 60)]
PAL.EYE_OUT, PAL.PUPIL = rgb(14, 10, 24), rgb(8, 6, 16)
PAL.BLUSH, PAL.BLUSH_L = rgb(196, 136, 150), rgb(216, 160, 170)
PAL.LIP, PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE = rgb(150, 80, 104), rgb(112, 52, 76), rgb(66, 20, 40), rgb(200, 96, 120)
PAL.BROW = PAL.HAIR[1]
PAL.eye_a, PAL.eye_b, PAL.eye_slant = 4.0, 5.0, 1.4          # narrower, sly: inner corner sits lower than the outer one
PAL.pupil, PAL.hl_dy, PAL.hl2_dy, PAL.lid_r = (1.8, 2.8), 3, 2, (4.9, 5.6)
PAL.brows = [(8, 10, -4, 3, 0.0, 1), (24, 9, -4, 3, 0.0, 1)]        # calm brows, the right one a touch higher
# default mouth: a closed, slightly upturned smile (the mischief grin is a separate pop-out part)
PAL.mouth_px = {(x, 28): PAL.LIP for x in range(14, 18)}
PAL.mouth_px.update({(13, 27): PAL.LIP, (18, 27): PAL.LIP, (15, 29): PAL.SKIN[3], (16, 29): PAL.SKIN[3]})
PAL.wing_base_t, PAL.tail_fluff_t, PAL.fluff_t = (0.05, 0.18), 0.30, 0.22
PAL.mouth_edge = (rgb(74, 34, 50), 0.80)                      # stylized: the open mouth gets a thin dark outline, no lips

CLOTH = ramp([rgb(18, 16, 28), rgb(34, 30, 50), rgb(54, 48, 76), rgb(82, 74, 108), rgb(116, 108, 146)])
LEATHER = ramp([rgb(20, 14, 12), rgb(44, 30, 24), rgb(78, 54, 40), rgb(110, 80, 58)])
SILVER = ramp([rgb(90, 96, 112), rgb(150, 158, 178), rgb(210, 216, 232)])
GOLD = ramp([rgb(150, 112, 44), rgb(214, 170, 70), rgb(244, 208, 118)])
BEAD = ramp([rgb(60, 90, 130), rgb(110, 160, 210), rgb(190, 230, 255)])

# ----------------------------------------------------------------------------- colour variants (texture only)
# `ruff` is a palette for the neck ruff, tassels, cuffs and back mantle (so a hooded crow can have a grey ruff and
# black wings); `shift` lightens the feather indices used on them. Keep in sync with CrowHarpy.Variant.
DEFAULT = SimpleNamespace(pal=PAL, cloth=CLOTH, bead=BEAD, ruff=PAL, shift=0)


def cvariant(hair, fea, eye, leg, cloth, bead, ruff=None, shift=0, gain=0.62, skin=None, pupil=None):
    rg = lambda colors, **kw: ramp([rgb(*c) for c in colors], **kw)
    p = copy.copy(PAL)
    p.HAIR, p.FEA, p.EYE, p.LEG = rg(hair), rg(fea), rg(eye), rg(leg)
    p.BROW = p.HAIR[1]
    p.hair_gain = gain
    if skin:
        p.SKIN = rg(skin, nodither=True)
    if pupil:
        p.PUPIL = rgb(*pupil)
    r = p
    if ruff:
        r = copy.copy(p)
        r.FEA = rg(ruff)
    return SimpleNamespace(pal=p, cloth=rg(cloth), bead=rg(bead), ruff=r, shift=shift)


NEUTRAL_HAIR = [(14, 14, 18), (30, 30, 36), (50, 50, 60), (76, 76, 88), (110, 112, 124), (150, 152, 164)]
NEUTRAL_FEA = [(8, 8, 10), (18, 18, 22), (34, 34, 40), (54, 56, 64), (84, 88, 98), (130, 134, 146), (176, 180, 192)]
DARK_EYES = [(30, 20, 14), (52, 36, 26), (80, 58, 40), (112, 82, 58), (150, 116, 84)]
BLACK_LEGS = [(12, 12, 18), (22, 22, 34), (32, 34, 50), (48, 52, 76), (72, 78, 112)]
GREY_RUFF = [(40, 42, 48), (76, 80, 88), (112, 118, 128), (148, 154, 164), (184, 190, 198), (214, 218, 224), (238, 240, 244)]
WHITE_RUFF = [(60, 62, 70), (110, 114, 124), (170, 174, 184), (208, 212, 220), (232, 236, 242), (246, 248, 250), (255, 255, 255)]

VARIANTS = {
    # hooded crow: black head and wings, ash-grey ruff, mantle and wrap, dark brown eyes, silver beads
    "hooded": cvariant(NEUTRAL_HAIR, NEUTRAL_FEA, DARK_EYES, BLACK_LEGS,
                       cloth=[(40, 42, 48), (72, 76, 86), (108, 114, 126), (148, 154, 166), (188, 194, 204)],
                       bead=[(90, 96, 112), (150, 158, 178), (210, 216, 232)], ruff=GREY_RUFF, shift=3),
    # jackdaw: silvery-grey hair like its grey nape, glossy blue-black wings, pale silver eyes, slate-blue wrap
    "jackdaw": cvariant([(20, 20, 26), (44, 44, 54), (76, 78, 92), (114, 118, 134), (156, 162, 180), (196, 202, 218)],
                        [(8, 10, 18), (18, 22, 36), (34, 42, 66), (56, 70, 104), (90, 108, 150), (140, 156, 196), (190, 204, 232)],
                        [(110, 120, 130), (160, 170, 180), (200, 210, 218), (226, 234, 240), (246, 250, 252)], BLACK_LEGS,
                        cloth=[(24, 28, 38), (44, 52, 68), (70, 82, 102), (104, 118, 140), (140, 154, 176)],
                        bead=[(100, 110, 124), (170, 182, 198), (228, 236, 246)], gain=0.85, pupil=(20, 24, 30)),
    # magpie: blue-green glossy black wings and hair, white ruff and white wrap, green beads
    "magpie": cvariant([(8, 12, 16), (16, 32, 40), (26, 60, 72), (40, 96, 108), (70, 140, 150), (120, 190, 196)],
                       [(6, 10, 14), (12, 26, 34), (18, 52, 62), (24, 92, 104), (40, 138, 148), (100, 196, 200), (190, 236, 236)],
                       DARK_EYES, BLACK_LEGS,
                       cloth=[(70, 72, 80), (120, 124, 136), (170, 174, 186), (210, 214, 224), (240, 242, 248)],
                       bead=[(20, 90, 70), (40, 160, 120), (150, 230, 200)], ruff=WHITE_RUFF, shift=4, gain=0.6),
    # chough: classic blue-black with the red legs and red-brown cloth the red-billed chough is known for
    "chough": cvariant([(12, 10, 20), (26, 24, 40), (44, 42, 68), (70, 70, 106), (108, 110, 150), (156, 158, 196)],
                       [(10, 9, 16), (22, 21, 34), (38, 38, 58), (62, 64, 96), (98, 102, 142), (146, 150, 188), (196, 200, 228)],
                       DARK_EYES, [(80, 14, 16), (130, 26, 28), (176, 44, 42), (210, 76, 64), (236, 120, 104)],
                       cloth=[(40, 14, 18), (72, 24, 30), (110, 38, 44), (150, 60, 66), (190, 92, 96)],
                       bead=[(120, 30, 34), (190, 60, 56), (240, 130, 110)]),
    # pied crow: black with a bold white collar, chest and wrap
    "pied": cvariant(NEUTRAL_HAIR, NEUTRAL_FEA, DARK_EYES, BLACK_LEGS,
                     cloth=[(60, 56, 52), (108, 100, 92), (160, 150, 138), (204, 194, 180), (236, 228, 216)],
                     bead=[(150, 112, 44), (214, 170, 70), (244, 208, 118)], ruff=WHITE_RUFF, shift=4),
}


# ----------------------------------------------------------------------------- parts
def declare_parts():
    H.standard_parts()
    for args in [
        ("bang_a", 2, 2, .5), ("bang_c", 2, 2.5, .5), ("bang_b", 2, 3.5, .5), ("tuft", 1.5, 3, 1.5),
        ("shin", 1.5, 8, 1.5), ("tail_f", 1.5, 10, .5), ("tail_g", 1.5, 10, .5),
        ("ruff", 1.5, 2.5, .5), ("ruff_side", .5, 2.5, 1.5), ("pouch", 2, 2, 1.5), ("trinket", .5, 1.5, .5),
        ("key", .5, 2, .25), ("buckle", 1, 1, .5), ("clip", 1, 1, .5),
        # expression parts: hidden just inside the face, slid out ~0.4 to sit almost flush with it. They are
        # thin (0.125) so no slab shows from the side; the grin is a wide stylized crescent of teeth (no lips).
        ("grin", 5, 2, .125), ("brows", 6.5, 2, .125), ("eyelids", 6.5, 3.5, .125), ("mouth", 1.75, 1.5, .125),
        ("pd_brim", 11, .5, 11), ("pd_crown", 6, 3.5, 6), ("pd_band", 6.25, .75, 6.25), ("pd_beak", 1.75, 2, 5.5),
    ]:
        H.part(*args)


# ----------------------------------------------------------------------------- texture
def paint(v=None):
    v = v or DEFAULT
    pt = H.Painter()
    pal, FEA, SKIN = v.pal, v.pal.FEA, v.pal.SKIN
    CLOTH, BEAD = v.cloth, v.bead                                           # shadow the module-level defaults
    rpal, sh = v.ruff, (lambda *ix: tuple(min(6, i + v.shift) for i in ix))   # ruff / tassel / cuff / mantle feathers
    D = H.D
    H.paint_head(pt, pal)
    H.paint_hair(pt, pal)
    H.paint_wings_tail(pt, pal, {
        "sec_a": (1, 2, 3, 0), "sec_b": (0, 2, 3, 1), "prim_a": (0, 1, 2, 0), "prim_b": (1, 2, 3, 1),
        "tail_f": (1, 2, 3, 0), "tail_g": (0, 1, 3, 1),
        "cov_a": (0, 0, 1, None, True), "cov_b": (0, 1, 2, None, True), "cov_c": (1, 2, 3, None, True),
    })
    H.paint_body_skin(pt, pal)
    H.paint_legs(pt, pal)

    # black feather bits
    for p, spec in (("tassel", (1, 2, 4)), ("cuff_f", (1, 2, 3)), ("trim", (0, 1, 3)), ("hfeather", (0, 2, 4)),
                    ("ruff", (0, 1, 3)), ("ruff_side", (0, 1, 3))):
        pt(p, "all", H.feather_fn(rpal, *sh(*spec), None, False, False))
    mantle = rpal.FEA
    pt("back_cover", "all", lambda f, x, y, w, h: pick(mantle, min(1.0, 0.12 + 0.34 * y / h + (noise(x, y, 19) - 0.5) * 0.14 + (0.16 if (x // 3 + y // 3) % 4 == 0 else 0) + 0.145 * v.shift), x, y))

    # tattered indigo bandeau with silver stitching
    def wrap(f, x, y, w, h):
        if f == "up":
            return pick(SKIN, 0.66, x, y)
        if f == "down":
            return pick(SKIN, 0.5, x, y)
        if y < D // 2:
            return pick(CLOTH, 0.55 + (noise(x, y, 29) - 0.5) * 0.2, x, y)
        if y >= h - D // 2:
            return SILVER[1] if x % 4 < 2 else CLOTH[1]                           # stitched hem
        t = 0.42 + (noise(x, y, 31) - 0.5) * 0.22 - (0.08 if f in ("east", "west", "south") else 0)
        if f == "north" and x == w // 2:
            t -= 0.2                                                              # centre seam
        if noise(x // 3, y // 3, 33) > 0.86:
            t += 0.25                                                             # worn patches
        return pick(CLOTH, t, x, y)
    pt("chest", "all", wrap)
    pt("bust", "all", wrap)

    def belt(f, x, y, w, h):
        if y < 2 or y >= h - 2:
            return LEATHER[0]
        if x % (D * 2) == D and y == h // 2:
            return SILVER[2]
        return pick(LEATHER, 0.45 + 0.1 * (((x + y) % 4) < 2) + (noise(x, y, 37) - 0.5) * 0.12, x, y)
    pt("sash", "all", belt)
    pt("buckle", "all", lambda f, x, y, w, h: SILVER[2] if (x in (0, w - 1) or y in (0, h - 1)) else pick(SILVER, 0.35, x, y))
    pt("pendant", "all", lambda f, x, y, w, h: BEAD[2] if (f in BROAD and x == 1 and y == 1) else pick(BEAD, 0.45 + 0.4 * (x < w // 2), x, y))
    pt("cord", "all", lambda f, x, y, w, h: LEATHER[1])
    pt("clip", "all", lambda f, x, y, w, h: pick(GOLD, 0.35 + 0.6 * (x < w // 2), x, y))

    # frayed cloth strips
    def cloth(broad):
        def fn(f, x, y, w, h):
            if f not in broad:
                return CLOTH[1]
            r = h - 1 - y
            tl = 1.8 * D
            if r < tl:
                half = (w / 2.0) * (min(1.0, (r + 1) / tl) ** 0.6)
                if abs(x + 0.5 - w / 2.0) > half or (r < D // 2 and noise(x, y, 41) > 0.45):
                    return CLEAR
            if y < D:                                           # leather waistband
                return LEATHER[2] if (x // 2) % 2 else LEATHER[1]
            if y in (D, D + 1):
                return SILVER[0]
            t = 0.40 + (noise(x, y, 43) - 0.5) * 0.24
            if x % 5 == 0:
                t -= 0.14                                       # folds
            if noise(x // 2, y // 3, 45) > 0.88:
                t += 0.26                                       # patch
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
        if x == w // 2 and y == h // 2:
            return SILVER[2]
        return pick(LEATHER, 0.45 + (noise(x, y, 47) - 0.5) * 0.25, x, y)
    pt("bracer", "all", bracer)

    # mischief: a stylized toothy grin - a wide crescent of teeth between two thin lines, two fangs, no lips and no
    # mouth interior. The patch around it repeats the face's skin and blush so the painted smile underneath is hidden.
    GRIN_X0, GRIN_Y0 = 6, 24                                   # where the 20 x 8 texel patch sits on the 32 x 32 face
    LINE = rgb(70, 34, 48)
    TEETH_SH = rgb(224, 220, 236)
    TEETH_GAP = rgb(200, 194, 214)

    def face_skin(fx, fy):
        for cx in (7, 25):
            e = ((fx + 0.5 - cx) / 3.6) ** 2 + ((fy + 0.5 - 26.5) / 1.6) ** 2
            if e <= 1.0:
                return pal.BLUSH if e < 0.45 else pal.BLUSH_L
        if fy == 26 and fx in (15, 16):
            return SKIN[1]
        t = 0.70 + (noise(fx, fy, 3) - 0.5) * 0.08
        if fy > 29:
            t -= 0.10
        return pick(SKIN, t, fx, fy)

    def grin(f, x, y, w, h):
        base = face_skin(x + GRIN_X0, y + GRIN_Y0) if f == "north" else pick(SKIN, 0.62, x, y)
        if f != "north":
            return base
        u = (x + 0.5 - w / 2.0) / (w / 2.0)
        yu = 1.2 + 1.7 * (1 - u * u)                            # upper line: corners high, centre low (a smile)
        yl = yu + 0.8 + 2.6 * (1 - u * u) ** 0.7                # lower line: teeth are tallest in the middle
        yy = y + 0.5
        fang = (x in (4, 5, 14, 15) and abs(u) < 0.75)
        if abs(yy - yu) <= 0.5:
            return LINE
        if abs(u) > 0.86 and abs(yy - (yu - 1)) <= 0.5:
            return LINE                                         # little corner dimples
        if yu + 0.5 < yy < yl - 0.5:
            if x % 3 == 0 and abs(u) < 0.8:
                return TEETH_GAP
            return TEETH_SH if yy > yl - 1.6 else pal.WHITE
        if abs(yy - yl) <= 0.5:
            return pal.WHITE if fang else LINE
        if fang and yl + 0.5 < yy <= yl + 2.0:
            tip = 5 if x in (4, 5) else 14                      # the fangs point to a single pixel at the inner column
            if yy <= yl + 1.0 or x == tip:
                return pal.WHITE
        return base
    pt("grin", "all", grin)

    # ... and sharply angled brows (inner ends dragged down) on an opaque skin strip that hides the calm painted ones
    def brow_hit(x, y):
        for xb in (x, 25 - x):                                  # the strip is 26 px wide; the right brow mirrors the left
            if 1 <= xb <= 9:
                yl = 1 + int(round(0.55 * (xb - 1)))
                if yl <= y <= yl + 1 + (1 if xb < 4 else 0):
                    return True
        return False

    def brows(f, x, y, w, h):
        if f == "north" and brow_hit(x, y):
            return pal.HAIR[1]
        return pick(SKIN, 0.70, x, y)
    pt("brows", "all", brows)

    # the pouch of stolen goods
    def pouch(f, x, y, w, h):
        flap = y < h * 0.45 and f in BROAD + ("east", "west")
        if f in ("up", "down"):
            return LEATHER[1]
        if flap and y == int(h * 0.45) - 1:
            return LEATHER[0]
        if f == "north" and y == int(h * 0.45) and x == w // 2:
            return SILVER[2]                                    # clasp
        return pick(LEATHER, (0.55 if flap else 0.40) + (noise(x, y, 63) - 0.5) * 0.2, x, y)
    pt("pouch", "all", pouch)
    pt("trinket", "all", lambda f, x, y, w, h: GOLD[2] if y % 3 == 0 else GOLD[1])
    pt("key", "all", lambda f, x, y, w, h: SILVER[2] if (f in BROAD and y < 3) else SILVER[1])
    pdl = ramp([rgb(14, 12, 12), rgb(30, 26, 24), rgb(52, 46, 42), rgb(80, 72, 66)])      # the plague doctor's hat and mask
    bone_ = ramp([rgb(150, 136, 110), rgb(196, 182, 152), rgb(226, 216, 190), rgb(246, 240, 222)])
    pt("pd_brim", "all", lambda f, x, y, w, h: pick(pdl, 0.4 + 0.15 * (f == "up"), x, y))
    pt("pd_crown", "all", lambda f, x, y, w, h: pick(pdl, 0.45 + 0.1 * (x % 3 == 0), x, y))
    pt("pd_band", "all", lambda f, x, y, w, h: pick(pdl, 0.75, x, y))
    pt("pd_beak", "all", lambda f, x, y, w, h: pick(bone_, 0.45 + 0.25 * (f == "up") + 0.15 * (y == 0) - 0.15 * (x in (0, w - 1)), x, y))
    return pt.T


# ----------------------------------------------------------------------------- geometry
def C(p, origin, inflate=None, pivot=None, rotation=None):
    """cube lifted by L (origin and pivot)"""
    pv = None if pivot is None else [pivot[0], pivot[1] + L, pivot[2]]
    return cube(p, [origin[0], origin[1] + L, origin[2]], inflate, pv, rotation)


def HG(p, x, ytop, z, rz=0, rx=0):
    return hang(p, x, ytop + L, z, rz, rx)


def build_model():
    m = H.Model(NAME)
    bone = m.bone
    bone("root", None, [0, 0, 0])

    # ---- torso: slender waist, slight bust under a tattered wrap, belt with pouch, necklace bead
    body = [
        C("hips", [-2.5, 12, -1.75]), C("midriff", [-1.75, 13.25, -1.25]), C("chest", [-2.5, 16, -1.625]),
        C("collar", [-1.75, 19, -1.375]), C("sash", [-2.75, 12.6, -2.0]), C("back_cover", [-1.75, 15.5, 1.625]),
        C("pendant", [-0.5, 17.15, -2.7]), C("cord", [-1.5, 18.55, -2.45]), C("buckle", [-0.5, 12.75, -2.4]),
    ]
    bust = C("bust", [-2.25, 16.9, -2.375])
    body += [bust, mirror_cube(bust)]
    for i, x in enumerate((-2.5, -1.5, -0.5, 0.5, 1.5)):
        body.append(HG("trim", x, 16.1, -2.05, 6 if i % 2 else -6))
    # feather ruff around the shoulders: front, back and the two sides, all flaring outward
    for i, x in enumerate((-2.2, -0.75, 0.7, 2.0)):
        body.append(HG("ruff", x, 20.3, -2.2 - 0.1 * (i % 2), (-1) ** i * 4, -28))
        body.append(HG("ruff", x, 20.3, 1.8 + 0.1 * (i % 2), (-1) ** i * 4, 28))
    for z in (-0.9, 0.5):
        r = C("ruff_side", [-3.0, 17.8, z], pivot=[-2.75, 20.3, z + 0.75], rotation=[0, 0, -26])
        body += [r, mirror_cube(r)]
    # pouch of stolen goods on the left hip, with a gold ring and a key swinging from it
    body += [C("pouch", [2.65, 11.2, -1.0]),
             HG("trinket", 3.0, 11.3, -1.35, 4, 0), HG("key", 3.9, 11.4, -0.7, -6, 0)]
    bone("body", "root", [0, 12 + L, 0], body)

    # ---- tattered cloth loincloth: strips of different lengths
    sk = []
    front = [("leaf_l", -3.0, 5), ("leaf_m", -1.5, -3), ("leaf_l", -0.2, 2), ("leaf_s", 1.0, -2), ("leaf_m", 2.0, 6)]
    for i, (p, x, rz) in enumerate(front):
        sk.append(HG(p, x, 13.7, -2.7 - 0.15 * (i % 2), rz, -9))
    for i, (p, x, rz) in enumerate((("leaf_m", -3.0, 3), ("leaf_s", -1.0, -2), ("leaf_l", 0.8, 3), ("leaf_m", 2.0, -3))):
        sk.append(HG(p, x, 13.7, 2.2 + 0.15 * (i % 2), rz, 9))
    for z in (-2.2, 0.0):
        sk.append(C("leaf_side", [-3.4, 8.2, z], pivot=[-3.15, 13.7, z + 1.25], rotation=[0, 0, -9]))
        sk.append(mirror_cube(sk[-1]))
    sk.append(HG("tassel", -2.7, 13.4, -3.1, -3, -4))
    bone("skirt", "body", [0, 13.7 + L, 0], sk)

    # ---- head: sleek cap, short swept bangs, sharp crown tufts, ears, stolen hair clip
    head = [C("head", [-4, 20, -4]), C("hair_cap", [-4, 20, -4], inflate=0.75)]
    ear_r = C("ear", [-5.0, 21.7, -1.2])
    head += [ear_r, mirror_cube(ear_r)]
    for i, (p, x, rz) in enumerate((("bang_b", -4.6, -7), ("bang_a", -3.0, 5), ("bang_c", -1.6, -4), ("bang_a", -0.1, 6),
                                    ("bang_c", 1.4, -5), ("bang_b", 3.0, 8))):
        head.append(HG(p, x, 28.4, -5.35 + 0.1 * (i % 2), rz))
    head.append(HG("frame", -4.9, 28, -5.0, -4))
    head.append(mirror_cube(head[-1]))
    for x, z in ((-1.8, 1.2), (0.0, 1.9), (1.8, 1.2)):             # a small crest at the back of the crown, swept backward
        head.append(C("tuft", [x - 0.75, 28.2, z - 0.75], pivot=[x, 28.2, z], rotation=[-24, 0, -x * 5]))
    head.append(HG("strand_l1", 5.6, 25.4, 0.9, 10, 2))          # one long swept lock on her left
    head.append(C("clip", [4.55, 24.6, -3.0], pivot=[5.05, 25.1, -2.75], rotation=[0, 0, -15]))
    for dx, dy, ang in ((0, 0, -22), (1.0, 0.7, 8)):
        head.append(HG("hfeather", 4.6 + dx, 28.0 + dy, -2.8, ang))
    bone("head", "body", [0, 20 + L, 0], head)
    bone("pd_hat", "head", [0, 28 + L, 0], [C("pd_brim", [-5.5, 28.9, -5.5]), C("pd_crown", [-3, 29.3, -3]), C("pd_band", [-3.125, 29.4, -3.125]),
                                           C("pd_beak", [-0.875, 21.0, -9.3], pivot=[0, 22.5, -4], rotation=[-15, 0, 0])])

    # expression parts: 0.3 inside the face at rest (face plane z = -4); sliding out by POP leaves them 0.1 proud of it
    bone("eyelids", "head", [0, 23.5 + L, -3.65], [C("eyelids", [-3.25, 21.75, -3.7])])
    bone("mouth", "head", [0, 21 + L, -3.65], [C("mouth", [-0.875, 20.25, -3.7])])
    bone("grin", "head", [0, 21 + L, -3.65], [C("grin", [-2.5, 20.0, -3.7])])
    bone("brows", "head", [0, 25.5 + L, -3.65], [C("brows", [-3.25, 24.75, -3.7])])

    side = [("strand_s1", -6.1, 26.0, -3.4, -6, 0), ("strand_s2", -6.3, 25.0, -2.0, -9, 0),
            ("strand_s1", -6.2, 26.2, 0.8, -7, 3), ("strand_s2", -6.0, 26.2, 2.4, -10, 4)]
    bone("hair_side_r", "head", [-4.7, 27 + L, 0], [HG(*s) for s in side], mirror=True)
    back = []
    names = ["strand_s1", "strand_s2", "strand_s1", "strand_s2", "strand_s1"]
    for i in range(11):
        x = -4.8 + 0.96 * i
        back.append(HG(names[i % 5], x, 26.8, 4.9 + 0.3 * (i % 3), x * 2.2, 6 + 2 * (i % 3)))
    bone("hair_back", "head", [0, 27 + L, 4.5], back)
    bone("ahoge", "head", [0, 28.5 + L, -1], [
        C("ahoge", [-0.5, 28.5, -1.5]),
        C("ahoge", [-0.35, 29.8, -1.5], pivot=[0, 29.8, -1], rotation=[0, 0, -20]),
        C("ahoge_tip", [0.1, 31.0, -1.5], pivot=[0.5, 31.1, -1], rotation=[0, 0, -50]),
    ])

    # ---- arms
    bone("arm_r", "body", [-3.25, 19.5 + L, 0], [C("arm_up", [-4.0, 16, -0.75])], mirror=True)
    bone("forearm_r", "arm_r", [-3.25, 16 + L, 0], [
        C("forearm", [-4.0, 13, -0.75]), C("bracer", [-4.5, 13.6, -1.25]),
        HG("cuff_f", -4.15, 13.8, -1.45, 4, -6), HG("cuff_f", -3.25, 13.8, -1.45, -4, -6), HG("cuff_f", -3.65, 13.8, 1.0, 0, 6),
    ], mirror=True)
    bone("hand_r", "forearm_r", [-3.25, 13 + L, 0], [
        C("palm", [-4.0, 11.5, -0.5]), C("fingers", [-4.0, 10.5, -0.5]), C("thumb", [-2.5, 11.5, -0.25]),
    ], mirror=True)

    # ---- wings: broad and dark, with widely fanned "fingered" primaries
    w = [C("wing_base", [-7, 10, 2.3])]
    rows = [(19.5, (-7.0, -5.0), ("cov_a", "cov_a")), (16.8, (-8.5, -6.5, -4.5), ("cov_a", "cov_b", "cov_a")),
            (14.1, (-9.5, -7.5, -5.5), ("cov_b", "cov_b", "cov_b")), (11.4, (-8.5, -6.5, -4.5), ("cov_c", "cov_c", "cov_c"))]
    for r, (ytop, xs, ps) in enumerate(rows):
        for x, p in zip(xs, ps):
            w.append(C(p, [x, ytop - 3, 3.3 + 0.2 * r]))
    for i, (x, ang) in enumerate(zip((-9.5, -8.25, -7, -5.75, -4.5), (-11, -8, -5, -2.5, 0))):
        w.append(HG("sec_a" if i % 2 == 0 else "sec_b", x, 12.5, 2.3 + 0.25 * (i % 2), ang))
    bone("wing_r", "body", [-2.5, 19 + L, 2.3], w, mirror=True)
    pr = []
    for i, (x, ang) in enumerate(zip((-10.5, -9, -7.5, -6, -4.5), (-26, -20, -14, -8, -3))):
        pr.append(HG("prim_a" if i % 2 == 0 else "prim_b", x, 12.5, 2.8 + 0.25 * (i % 2), ang))
    bone("wing_tip_r", "wing_r", [-8, 12.5 + L, 3.0], pr, mirror=True)

    # ---- tail: long wedge
    tail = [C("tail_fluff", [-1.75, 12.2, 2.2])]
    for i, (x, ang) in enumerate(zip((-3.5, -2.1, -0.75, 0.6, 2.0), (-14, -7, 0, 7, 14))):
        tail.append(HG("tail_f" if i % 2 == 0 else "tail_g", x, 12.7, 3.4 + 0.25 * (i % 2), ang, 6))
    bone("tail", "body", [0, 13.2 + L, 3], tail)

    # ---- legs: longer, black scaled shins, black talons
    cx = -1.25
    bone("leg_r", "root", [cx, 12 + L, 0], [C("thigh", [cx - 1, 9.5, -1]), C("fluff", [cx - 1.25, 8.5, -1.25])], mirror=True)
    bone("shin_r", "leg_r", [cx, 9 + L, 0], [cube("shin", [cx - .75, 2, -.75]), cube("ankle", [cx - 1, 1, -1])], mirror=True)
    foot = [cube("heel", [cx - 1, 0, -1])]
    for dx in (-2.0, -0.5, 1.0):
        x = cx + dx
        foot += [cube("toe1", [x, 0, -3.5]), cube("toe2", [x, 0, -5.5]),
                 cube("talon", [x, 0, -7.5], pivot=[x + .5, .5, -5.5], rotation=[22, 0, 0])]
    foot += [cube("htoe", [cx - .5, 0, 1]), cube("htalon", [cx - .5, 0, 2.5], pivot=[cx, .5, 2.5], rotation=[-22, 0, 0])]
    bone("foot_r", "shin_r", [cx, 1, 0], foot, mirror=True)
    return m


# ----------------------------------------------------------------------------- animations
def animations(an):
    for f in (H.std_idle, H.std_walk, H.std_fly, H.std_bathe):
        f(an)
    OUT, REST = H.OUT_, H.REST
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold

    # caw: imitating sounds - a rapid, chatty mouth with head bobs and a flick of the wings and tail
    beats = [(0, REST), (0.12, OUT), (0.35, OUT), (0.5, REST), (0.7, OUT), (0.9, OUT), (1.05, REST),
             (1.25, OUT), (1.7, OUT), (1.85, REST), (2.0, REST)]
    an.add("caw", 2.0, True, merge({
        "head": {"rotation": T((0, (-10, 0, 0)), (0.4, (7, 0, 0)), (0.9, (-10, 0, 0)), (1.4, (7, 0, 0)), (2, (-10, 0, 0)))},
        "body": {"rotation": T((0, (-3, 0, 0)), (0.4, (3, 0, 0)), (0.9, (-3, 0, 0)), (1.4, (3, 0, 0)), (2, (-3, 0, 0)))},
        "tail": {"rotation": T((0, (4, 0, 0)), (0.4, (14, 0, 0)), (0.9, (4, 0, 0)), (1.4, (14, 0, 0)), (2, (4, 0, 0)))},
        "mouth": {"position": T(*beats)},
        "hair_back": {"rotation": T((0, (0, 0, 0)), (0.4, (7, 0, 0)), (0.9, (0, 0, 0)), (1.4, (7, 0, 0)), (2, (0, 0, 0)))},
        **H.blink(1.6),
    },
        sym("wing", T((0, (0, 0, -4)), (0.4, (0, 0, -15)), (0.9, (0, 0, -4)), (1.4, (0, 0, -15)), (2, (0, 0, -4)))),
        sym("arm", hold(-6, 0, 3)), sym("forearm", hold(-12, 0, 0))))

    # knock: right fist raps three times on wood, head cocked to listen, a sly grin
    knocks = T((0, (-60, 0, 0)), (0.2, (-98, 0, 0)), (0.4, (-60, 0, 0)), (0.6, (-98, 0, 0)), (0.8, (-60, 0, 0)),
               (1.0, (-98, 0, 0)), (1.2, (-60, 0, 0)), (1.6, (-60, 0, 0)))
    an.add("knock", 1.6, True, merge({
        "arm_r": {"rotation": hold(-55, 0, 4)},
        "forearm_r": {"rotation": knocks},
        "hand_r": {"rotation": hold(-12, 0, 0)},
        "arm_l": {"rotation": hold(-6, 0, -2)},
        "head": {"rotation": T((0, (-4, -8, 8)), (1.6, (-4, -8, 8)))},
        "body": {"rotation": T((0, (3, 0, 0)), (0.2, (6, 0, 0)), (0.4, (3, 0, 0)), (0.6, (6, 0, 0)), (0.8, (3, 0, 0)), (1.0, (6, 0, 0)), (1.2, (3, 0, 0)), (1.6, (3, 0, 0)))},
        "tail": {"rotation": T((0, (0, 5, 0)), (0.8, (0, -5, 0)), (1.6, (0, 5, 0)))},
        "grin": {"position": hold(*OUT)},
        "brows": {"position": hold(*OUT)},
    },
        sym("wing", hold(0, 0, -2))))

    # prank: sneaking low, glancing around, rummaging and snatching a stolen item, then a giggle -
    # with the mischievous face (toothy grin, pointed brows) switched on for the whole sneak
    def keyed(pairs):
        return T(*pairs)
    mis = [(0, REST), (0.4, REST), (0.5, OUT), (2.8, OUT), (2.95, REST), (3.0, REST)]
    an.add("prank", 3.0, True, merge({
        "body": {"position": keyed([(0, (0, 0, 0)), (0.5, (0, -1.4, 0)), (1.3, (0, -1.4, 0)), (1.4, (0, -1.7, 0)), (2.2, (0, -1.4, 0)),
                                    (2.4, (0, -1.0, 0)), (2.55, (0, -1.4, 0)), (2.7, (0, -1.0, 0)), (3.0, (0, 0, 0))]),
                 "rotation": keyed([(0, (0, 0, 0)), (0.5, (14, 0, 0)), (2.2, (14, 0, 0)), (2.5, (10, 0, 0)), (2.7, (14, 0, 0)), (3.0, (0, 0, 0))])},
        "head": {"rotation": keyed([(0, (0, 0, 0)), (0.5, (-10, -24, 6)), (1.2, (-10, -24, 6)), (1.4, (-6, 20, -4)), (2.1, (-6, 20, -4)),
                                    (2.3, (-12, 0, 0)), (2.45, (-12, 0, 6)), (2.6, (-12, 0, -6)), (2.75, (-12, 0, 6)), (3.0, (0, 0, 0))])},
        "leg_r": {"rotation": keyed([(0, (0, 0, 0)), (0.5, (-22, 0, 0)), (2.5, (-22, 0, 0)), (3.0, (0, 0, 0))])},
        "leg_l": {"rotation": keyed([(0, (0, 0, 0)), (0.5, (-22, 0, 0)), (2.5, (-22, 0, 0)), (3.0, (0, 0, 0))])},
        "shin_r": {"rotation": keyed([(0, (0, 0, 0)), (0.5, (38, 0, 0)), (2.5, (38, 0, 0)), (3.0, (0, 0, 0))])},
        "shin_l": {"rotation": keyed([(0, (0, 0, 0)), (0.5, (38, 0, 0)), (2.5, (38, 0, 0)), (3.0, (0, 0, 0))])},
        "foot_r": {"rotation": keyed([(0, (0, 0, 0)), (0.5, (-16, 0, 0)), (2.5, (-16, 0, 0)), (3.0, (0, 0, 0))])},
        "foot_l": {"rotation": keyed([(0, (0, 0, 0)), (0.5, (-16, 0, 0)), (2.5, (-16, 0, 0)), (3.0, (0, 0, 0))])},
        "arm_r": {"rotation": keyed([(0, (0, 0, 2)), (0.5, (-55, 0, 4)), (1.3, (-55, 0, 4)), (1.45, (-88, 0, 4)), (1.65, (-50, 0, 4)), (2.4, (-50, 0, 4)), (3.0, (0, 0, 2))])},
        "arm_l": {"rotation": keyed([(0, (0, 0, -2)), (0.5, (-50, 0, -4)), (1.4, (-50, 0, -4)), (1.55, (-82, 0, -4)), (1.75, (-46, 0, -4)), (2.4, (-46, 0, -4)), (3.0, (0, 0, -2))])},
        "forearm_r": {"rotation": keyed([(0, (-6, 0, 0)), (0.5, (-30, 0, 0)), (0.8, (-52, 0, 0)), (1.1, (-30, 0, 0)), (1.45, (-70, 0, 0)), (1.65, (-34, 0, 0)), (2.0, (-52, 0, 0)), (2.4, (-34, 0, 0)), (3.0, (-6, 0, 0))])},
        "forearm_l": {"rotation": keyed([(0, (-6, 0, 0)), (0.5, (-34, 0, 0)), (0.95, (-56, 0, 0)), (1.25, (-32, 0, 0)), (1.55, (-72, 0, 0)), (1.75, (-36, 0, 0)), (2.1, (-54, 0, 0)), (2.4, (-34, 0, 0)), (3.0, (-6, 0, 0))])},
        "tail": {"rotation": keyed([(0, (0, 0, 0)), (0.5, (18, 0, 0)), (2.4, (18, 0, 0)), (2.55, (24, 0, 0)), (2.7, (18, 0, 0)), (3.0, (0, 0, 0))])},
        "hair_back": {"rotation": keyed([(0, (0, 0, 0)), (0.5, (10, 0, 0)), (2.4, (10, 0, 0)), (3.0, (0, 0, 0))])},
        "grin": {"position": keyed(mis)},
        "brows": {"position": keyed(mis)},
    },
        sym("wing", keyed([(0, (0, 0, -2)), (0.5, (0, 0, -1)), (1.4, (0, 0, -4)), (1.5, (0, 0, -1)), (3.0, (0, 0, -2))])),
        sym("hand", hold(-14, 0, 0))))

    # cache: crouched over a patch of ground, scratching a hole with alternating hands, tucking the item in,
    # patting the dirt flat and glancing round to check nobody saw - mischief face on the glance
    crouch = [(0, (0, -4.5, 0)), (2.0, (0, -4.5, 0))]
    look = [(0, REST), (1.55, REST), (1.6, OUT), (1.95, OUT), (2.0, REST)]
    an.add("cache", 2.0, True, merge({
        "body": {"position": keyed(crouch),
                 "rotation": keyed([(0, (24, 0, 0)), (1.2, (24, 0, 0)), (1.3, (30, 0, 0)), (1.4, (24, 0, 0)), (1.5, (30, 0, 0)),
                                    (1.6, (18, 0, 0)), (2.0, (24, 0, 0))])},
        "head": {"rotation": keyed([(0, (16, 0, 0)), (0.3, (22, 0, 0)), (0.6, (16, 0, 0)), (0.9, (22, 0, 0)), (1.2, (16, 0, 0)),
                                    (1.6, (-8, -26, 6)), (1.8, (-8, 24, -6)), (2.0, (16, 0, 0))])},
        "leg_r": {"rotation": hold(-62, 0, 6)}, "leg_l": {"rotation": hold(-62, 0, -6)},
        "shin_r": {"rotation": hold(96, 0, 0)}, "shin_l": {"rotation": hold(96, 0, 0)},
        "foot_r": {"rotation": hold(-34, 0, 0)}, "foot_l": {"rotation": hold(-34, 0, 0)},
        "arm_r": {"rotation": keyed([(0, (-22, 0, 6)), (0.15, (-4, 0, 6)), (0.3, (-22, 0, 6)), (0.45, (-4, 0, 6)), (0.6, (-22, 0, 6)),
                                     (0.75, (-4, 0, 6)), (0.9, (-22, 0, 6)), (1.2, (-18, 0, 6)), (1.3, (-6, 0, 6)), (1.4, (-18, 0, 6)),
                                     (1.5, (-6, 0, 6)), (1.6, (8, 0, 4)), (2.0, (-22, 0, 6))])},
        "arm_l": {"rotation": keyed([(0, (-4, 0, -6)), (0.15, (-22, 0, -6)), (0.3, (-4, 0, -6)), (0.45, (-22, 0, -6)), (0.6, (-4, 0, -6)),
                                     (0.75, (-22, 0, -6)), (0.9, (-4, 0, -6)), (1.2, (-18, 0, -6)), (1.3, (-6, 0, -6)), (1.4, (-18, 0, -6)),
                                     (1.5, (-6, 0, -6)), (1.6, (8, 0, -4)), (2.0, (-4, 0, -6))])},
        "forearm_r": {"rotation": keyed([(0, (-10, 0, 0)), (0.15, (-34, 0, 0)), (0.3, (-10, 0, 0)), (0.45, (-34, 0, 0)), (0.6, (-10, 0, 0)),
                                         (0.75, (-34, 0, 0)), (0.9, (-10, 0, 0)), (1.6, (-14, 0, 0)), (2.0, (-10, 0, 0))])},
        "forearm_l": {"rotation": keyed([(0, (-34, 0, 0)), (0.15, (-10, 0, 0)), (0.3, (-34, 0, 0)), (0.45, (-10, 0, 0)), (0.6, (-34, 0, 0)),
                                         (0.75, (-10, 0, 0)), (0.9, (-34, 0, 0)), (1.6, (-14, 0, 0)), (2.0, (-34, 0, 0))])},
        "tail": {"rotation": keyed([(0, (20, 0, 0)), (0.6, (20, 8, 0)), (1.2, (20, -8, 0)), (2.0, (20, 0, 0))])},
        "hair_back": {"rotation": hold(12, 0, 0)},
        "grin": {"position": keyed(look)},
        "brows": {"position": keyed(look)},
    },
        sym("wing", hold(0, 0, -3)),
        sym("hand", hold(-20, 0, 0))))


if __name__ == "__main__":
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="caw", head_center=(0, 22.5 + L), body_center=(0, 15.5 + L),
          # idle, caw, knock (mischief face), prank (mischief face), prank from the side
          face_views=[("idle", 0, 0, 0), ("caw", 0.5, 0, 0), ("knock", 0.5, 0, 0), ("prank", 1.0, 0, 0), ("prank", 1.0, -28, 8)])
    VT.write_variants(NAME, "rook", paint, VARIANTS, build_model, animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + L))])
