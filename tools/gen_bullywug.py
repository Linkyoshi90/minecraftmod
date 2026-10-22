#!/usr/bin/env python3
"""Bullywug - species definition (the frog lineage). Shared engine: anuran_lib.py on top of harpy_lib.py.

    python tools/gen_bullywug.py                    # write geo / animation / texture
    python tools/gen_bullywug.py --preview x.png    # also render x.png, x_face.png, x_body.png, x_texture.png

Concept (field guide: "Bullywug - loud, boastful, pushy, secretly easy to scare; swamps and rivers"): a tall, long-legged
bullfrog girl, about 1.85 blocks. Smooth glossy leaf-green skin with a pale lateral stripe and dark blotches, a yellow
throat sac that swells when she bellows, big golden bulging eyes with horizontal pupils, a smug wide grin, round
tympanum discs behind the eyes, webbed feet with sticky toe pads. Outfit: a lily-pad skirt, a woven reed wrap, a
necklace of frogspawn beads, three cattails strapped to her back and a pink lotus pinned in her teal bob.
"""
import copy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import anuran_lib as A
from anuran_lib import rg, cube, mirror_cube, pick, noise, CLEAR, BROAD
from types import SimpleNamespace

HUMANOID = "--frog-face" not in sys.argv   # default: human-style face painted on the front of the head; --frog-face builds the old bulging-eye version
NAME = "bullywug" if HUMANOID else "bullywug_frogface"

# ----------------------------------------------------------------------------- layout
SPEC = SimpleNamespace(legx=2.25, eye_x=2.75, eye_out=0.5, eye_rise=1.5, head_sink=0.75, head_fwd=0.0, chest_fwd=0.0,
                       mouth_y=1.0, throat_y=0.5, tymp_y=2.0, tymp_z=3.2)
SIZES = {}          # the shared defaults are already a bullfrog girl


if HUMANOID:
    SIZES.update(head=(8, 7.5, 7.5), eyelid=(3, 3, .125), mouth=(2.5, 1.5, .125))
    SPEC.hum = dict(sep=2.5, y=3.9, a=5.4, b=5.8, mw=1.5)


def declare_parts():
    A.declare_base(SIZES)
    for args in [
        ("lily", 4.5, .25, 4.0), ("wrap", 8, 3, 5.5), ("belt", 8.5, 1, 5.5), ("shell", 1.25, 1, .5),
        ("bead", 1, 1, 1), ("cat_stalk", .5, 10, .5), ("cat_head", 1, 3.5, 1), ("cat_tip", .5, 1, .5),
        ("petal", 1.25, 2.25, .25), ("petal_s", 1, 1.75, .25), ("lotus_c", 1.25, .75, 1.25), ("hair_tie", 1, 1, .5),
    ]:
        H.part(*args)


# ----------------------------------------------------------------------------- palette
PAL = A.new_pal(
    BACK=rg([(30, 84, 54), (46, 124, 66), (72, 164, 84), (112, 200, 108), (168, 230, 150)], nodither=True),
    BELLY=rg([(158, 168, 84), (196, 206, 112), (228, 230, 148), (246, 246, 192)], nodither=True),
    spots=dict(seed=21, cell=7, rmin=1.6, rmax=3.4, depth=0.30),
    stripe=rg([(180, 200, 100), (214, 228, 132), (240, 246, 170)], nodither=True), side_split=0.50, gloss=2,
    EYE=rg([(120, 70, 10), (176, 114, 18), (226, 168, 30), (250, 208, 76), (255, 236, 140)]),
    pupil=(0.70, 0.24),
)
PAL.EYE_OUT = H.rgb(18, 40, 24)
PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE, PAL.NOSTRIL = H.rgb(26, 62, 38), H.rgb(124, 38, 54), H.rgb(232, 104, 120), H.rgb(24, 56, 30)
PAL.TONGUE_R = rg([(150, 56, 72), (196, 84, 98), (228, 118, 132), (248, 160, 168)])
PAL.BLUSH, PAL.BLUSH_L = H.rgb(236, 176, 66), H.rgb(244, 204, 104)
PAL.DRUM = rg([(70, 96, 40), (120, 150, 60), (170, 196, 92), (206, 226, 130)])
PAL.SAC = rg([(170, 100, 20), (214, 140, 28), (244, 184, 50), (255, 218, 110)])
PAL.PAD = rg([(110, 140, 50), (170, 196, 82), (214, 232, 120), (244, 250, 170)])
PAL.HAIR = rg([(14, 40, 52), (24, 64, 78), (38, 96, 112), (60, 132, 146), (96, 166, 176), (150, 204, 208)])
PAL._mouth_y = SPEC.mouth_y
PAL.nostril_dx, PAL.nostril_up = 3.0, 4

REED = rg([(90, 64, 34), (130, 96, 50), (172, 134, 74), (206, 172, 106)])
CORD = rg([(70, 44, 26), (110, 74, 42), (152, 108, 64)])
LILY = rg([(26, 74, 46), (40, 112, 62), (66, 156, 82), (108, 196, 108)])
LILY_UNDER = rg([(110, 84, 120), (150, 118, 150), (190, 160, 184)])
PINK = rg([(176, 58, 100), (224, 100, 142), (248, 156, 184), (255, 214, 226)])
CATT = rg([(40, 24, 16), (68, 42, 26), (100, 64, 38), (136, 92, 54)])
STALK = rg([(40, 76, 36), (66, 114, 50), (104, 152, 70), (150, 190, 100)])
BEAD = rg([(120, 168, 150), (176, 214, 196), (222, 244, 232)])
SHELL = rg([(170, 140, 120), (222, 200, 176), (250, 240, 224)])


# ----------------------------------------------------------------------------- texture
def _paint():
    pt = A.paint_base(PAL, curve=3.2, fringe=4 if HUMANOID else 0, hum=getattr(SPEC, "hum", None))

    def lily(f, x, y, w, h):
        if f in ("up", "down"):
            cx = w / 2.0
            dx, dy = (x + 0.5 - cx) / (w / 2.0), (y + 0.5 - h * 0.25) / (h * 0.75)
            if (dx * dx + dy * dy) > 1.0 or (y < h * 0.42 and abs(x + 0.5 - cx) < (h * 0.42 - y) * 0.30):
                return CLEAR                                  # round pad with a V notch toward the body
            if f == "down":
                return pick(LILY_UNDER, 0.55 + (noise(x, y, 3) - 0.5) * 0.12, x, y)
            ang = math.atan2(x + 0.5 - cx, y + 0.5 - h * 0.12)
            vein = abs(((ang * 7.0 / math.pi) % 1.0) - 0.5) < 0.07
            rim = (dx * dx + dy * dy) > 0.78
            t = 0.55 + (noise(x, y, 5) - 0.5) * 0.10 + (0.16 if vein else 0) - (0.16 if rim else 0)
            return pick(LILY, t, x, y)
        return CLEAR
    pt("lily", "all", lily)

    def weave(f, x, y, w, h):
        if y < 2 or y >= h - 2:
            return CORD[1] if (x // 2) % 2 == 0 else CORD[0]
        base = 0.46 + (0.18 if ((x // 2) + (y // 2)) % 2 else 0) + (noise(x, y, 17) - 0.5) * 0.12
        if f in ("up", "down"):
            return REED[1]
        return pick(REED, base, x, y)
    pt("wrap", "all", weave)
    pt("hips", "all", weave)                                  # woven reed shorts under the skirt (the skirt comes off)

    def rope(f, x, y, w, h):
        t = 0.30 + 0.35 * (((x + y) % 3) == 0) + (noise(x, y, 19) - 0.5) * 0.1
        return pick(CORD, t, x, y)
    pt("belt", "all", rope)
    pt("shell", "all", lambda f, x, y, w, h: pick(SHELL, 0.85 - 0.5 * (y > h - 3) - 0.25 * (x % 3 == 0), x, y))

    def bead(f, x, y, w, h):
        e = (x - (w - 1) / 2.0) ** 2 + (y - (h - 1) / 2.0) ** 2
        if f in BROAD + ("east", "west") and e < 1.3:
            return H.rgb(28, 36, 32)                           # the tiny dark embryo inside each frogspawn bead
        return pick(BEAD, 0.55 + 0.35 * (x < 2 and y < 2) - 0.2 * (x > w - 2 or y > h - 2), x, y)
    pt("bead", "all", bead)

    pt("cat_stalk", "all", lambda f, x, y, w, h: pick(STALK, 0.55 + 0.25 * (y % 12 == 0) + (noise(x, y, 23) - 0.5) * 0.1, x, y))
    pt("cat_head", "all", lambda f, x, y, w, h: pick(CATT, 0.50 + 0.2 * (noise(x // 2, y // 2, 27) > 0.55) + (noise(x, y, 29) - 0.5) * 0.14, x, y))
    pt("cat_tip", "all", lambda f, x, y, w, h: pick(STALK, 0.7, x, y))

    def petal(f, x, y, w, h):
        if f not in BROAD:
            return PINK[1]
        r = y / max(1, h - 1)                                  # 0 = tip, 1 = base
        half = (w / 2.0) * min(1.0, (y + 1.5) / (h * 0.55))
        if abs(x + 0.5 - w / 2.0) > half:
            return CLEAR
        return pick(PINK, 0.95 - 0.75 * r + (noise(x, y, 31) - 0.5) * 0.06, x, y)
    pt("petal", "all", petal)
    pt("petal_s", "all", petal)
    pt("lotus_c", "all", lambda f, x, y, w, h: pick(rg([(200, 150, 30), (244, 204, 70), (255, 238, 140)]), 0.7 + (0.3 if (x + y) % 3 == 0 else 0), x, y))
    pt("hair_tie", "all", lambda f, x, y, w, h: pick(CORD, 0.6, x, y))
    return pt.T


def paint(v=None):
    """v = a variant palette (see VARIANTS); None = the default colours"""
    global PAL
    base = PAL
    PAL = v or base
    try:
        return _paint()
    finally:
        PAL = base


# ----------------------------------------------------------------------------- colour variants (texture only)
# A variant swaps the skin, eye, hair and throat colours (the outfit stays the same). Files: bullywug_<variant>.png;
# the default stays bullywug.png.
def variant(back, belly, stripe, eye, sac, pad, drum, hair, blush, eye_out, lip, spot=0.30, pupil=None, spot_cell=None):
    p = copy.copy(PAL)
    p.BACK, p.BELLY, p.stripe = rg(back, nodither=True), rg(belly, nodither=True), rg(stripe, nodither=True)
    p.EYE, p.SAC, p.PAD, p.DRUM, p.HAIR = rg(eye), rg(sac), rg(pad), rg(drum), rg(hair)
    p.BLUSH, p.BLUSH_L = H.rgb(*blush[0]), H.rgb(*blush[1])
    p.EYE_OUT, p.LIP_D, p.NOSTRIL = H.rgb(*eye_out), H.rgb(*lip), H.rgb(*lip)
    p.spots = dict(PAL.spots, depth=spot, **({"cell": spot_cell[0], "rmin": spot_cell[1], "rmax": spot_cell[2]} if spot_cell else {}))
    if pupil:
        p.PUPIL = H.rgb(*pupil)
    return p


VARIANTS = {
    # golden morph: warm yellow-orange skin, amber eyes, an orange throat, auburn hair
    "golden": variant(
        back=[(120, 84, 10), (170, 124, 20), (214, 170, 36), (240, 204, 70), (252, 230, 130)],
        belly=[(180, 150, 80), (220, 190, 110), (244, 222, 150), (252, 240, 190)],
        stripe=[(200, 170, 60), (236, 206, 90), (250, 230, 140)],
        eye=[(100, 50, 10), (150, 80, 16), (196, 116, 26), (226, 150, 40), (246, 190, 90)],
        sac=[(170, 80, 20), (214, 116, 30), (240, 150, 50), (252, 190, 100)],
        pad=[(150, 120, 40), (196, 164, 70), (226, 200, 110), (248, 232, 160)],
        drum=[(100, 74, 16), (150, 116, 30), (196, 164, 60), (230, 204, 100)],
        hair=[(50, 24, 12), (90, 46, 20), (132, 72, 30), (176, 106, 44), (214, 146, 70), (240, 190, 110)],
        blush=((236, 120, 70), (244, 156, 100)), eye_out=(60, 30, 6), lip=(90, 56, 12), spot=0.26),
    # blue morph (axanthic): the yellow is missing, so the skin turns turquoise; dark eyes, purple hair
    "blue": variant(
        back=[(10, 40, 80), (20, 84, 140), (40, 140, 196), (90, 190, 230), (170, 228, 250)],
        belly=[(120, 170, 200), (170, 210, 230), (210, 236, 248), (236, 250, 255)],
        stripe=[(150, 210, 230), (190, 232, 246), (220, 246, 255)],
        eye=[(30, 20, 10), (60, 40, 16), (110, 74, 24), (176, 130, 40), (230, 190, 80)],
        sac=[(80, 150, 190), (130, 190, 220), (180, 220, 240), (220, 244, 255)],
        pad=[(60, 120, 160), (110, 170, 200), (160, 210, 230), (210, 240, 250)],
        drum=[(10, 50, 90), (24, 90, 140), (50, 140, 190), (100, 190, 224)],
        hair=[(30, 14, 50), (56, 28, 90), (88, 48, 130), (124, 78, 170), (166, 120, 206), (210, 170, 236)],
        blush=((240, 140, 170), (248, 176, 198)), eye_out=(6, 20, 40), lip=(6, 30, 60), spot=0.26),
    # leopard: tan-brown with big dark round spots and pale ridges, a white belly, dark brown hair
    "leopard": variant(
        back=[(60, 40, 20), (110, 80, 40), (158, 124, 66), (196, 164, 100), (228, 200, 140)],
        belly=[(170, 160, 130), (210, 204, 170), (236, 232, 204), (250, 248, 230)],
        stripe=[(210, 190, 140), (236, 222, 176), (250, 242, 206)],
        eye=[(60, 40, 10), (110, 76, 16), (160, 116, 24), (206, 160, 40), (238, 206, 90)],
        sac=[(160, 130, 80), (200, 172, 112), (228, 206, 150), (246, 232, 190)],
        pad=[(120, 100, 56), (170, 148, 96), (210, 190, 140), (240, 228, 188)],
        drum=[(80, 60, 30), (124, 96, 52), (168, 138, 84), (208, 182, 122)],
        hair=[(24, 16, 10), (44, 30, 18), (70, 50, 30), (104, 76, 44), (142, 108, 64), (184, 148, 98)],
        blush=((226, 140, 110), (238, 170, 140)), eye_out=(40, 26, 10), lip=(60, 40, 20), spot=0.52, spot_cell=(7, 2.0, 3.8)),
    # marsh: a dark mossy swamp frog, nearly black-green with a dim stripe, emerald hair
    "marsh": variant(
        back=[(10, 24, 14), (24, 50, 26), (44, 80, 38), (74, 114, 56), (116, 152, 86)],
        belly=[(100, 110, 70), (140, 152, 96), (176, 188, 122), (208, 220, 156)],
        stripe=[(110, 130, 60), (150, 170, 84), (190, 206, 116)],
        eye=[(70, 50, 6), (130, 100, 12), (190, 150, 24), (226, 184, 50), (248, 220, 110)],
        sac=[(100, 100, 40), (150, 148, 60), (196, 194, 90), (230, 226, 130)],
        pad=[(70, 90, 40), (110, 130, 60), (150, 170, 90), (190, 206, 130)],
        drum=[(20, 40, 20), (40, 70, 34), (70, 106, 50), (104, 142, 76)],
        hair=[(6, 22, 16), (12, 40, 28), (22, 66, 44), (40, 98, 64), (70, 132, 92), (120, 176, 134)],
        blush=((190, 120, 60), (210, 150, 90)), eye_out=(10, 24, 14), lip=(10, 24, 14), spot=0.34),
    # albino: pale pink all over, white belly, red eyes
    "albino": variant(
        back=[(190, 120, 120), (224, 160, 156), (244, 196, 188), (252, 222, 214), (255, 240, 232)],
        belly=[(230, 200, 196), (246, 222, 216), (254, 240, 234), (255, 250, 246)],
        stripe=[(250, 230, 220), (255, 244, 236), (255, 252, 248)],
        eye=[(120, 10, 20), (176, 24, 36), (220, 50, 60), (244, 100, 96), (255, 170, 160)],
        sac=[(220, 130, 140), (240, 164, 170), (252, 200, 204), (255, 226, 228)],
        pad=[(220, 150, 150), (240, 184, 180), (252, 212, 206), (255, 236, 230)],
        drum=[(190, 130, 130), (220, 160, 156), (240, 196, 190), (252, 224, 216)],
        hair=[(150, 110, 120), (190, 150, 160), (226, 196, 204), (246, 226, 232), (255, 244, 248), (255, 255, 255)],
        blush=((244, 140, 150), (250, 176, 184)), eye_out=(100, 20, 30), lip=(150, 70, 80), spot=0.10, pupil=(80, 10, 20)),
}


def write_variants():
    """bullywug_<variant>.png next to the default texture; --lineup x.png also renders them side by side"""
    import variant_tools as VT
    VT.write_variants(NAME, "green", paint, VARIANTS, build_model, animations,
                      rows=[("idle", 0, -20, 6, 11.5, None), ("croak", 0.6, -28, 8, 11.5, None), ("idle", 0, -15, 0, 28, (0, 24.5))])


# ----------------------------------------------------------------------------- geometry
def extras(r, g):
    HG, hang = H.hang, H.hang
    ew = A.S("eye")
    top = g.head_top

    # hair: a teal bob that clears the eyes and the tympana, one bang between the eyes
    r.b["head"]["cubes"] += [HG("bang_a", -0.875, top + 0.3, g.head_z0 - 0.75, 3, -4)]
    side = [("strand_s", -5.6, top - 1.0, -2.8, -4, 0), ("strand_s", -5.6, top - 1.2, 2.4, -6, 3), ("strand_m", -5.6, top - 1.2, 3.4, -8, 6)]
    r.add("hair_side_r", "head", [-4.6, top - 1.0, 0], [HG(*s) for s in side], mirror=True)
    back = [HG(n, x, top - 1.0, 4.2 + 0.25 * (i % 3), x * 1.2, 5 + 2 * (i % 3)) for i, (n, x) in
            enumerate(zip(("strand_s", "strand_m", "strand_s", "strand_m", "strand_s", "strand_m", "strand_s"), (-4.5, -3.0, -1.5, 0.0, 1.5, 3.0, 4.5)))]
    r.add("hair_back", "head", [0, top - 1.0, 4.0], back)

    # pink lotus pinned over the right side of the bob
    fx, fy, fz = -3.4, top + 0.4, 1.8
    lot = [cube("lotus_c", [fx - 0.625, fy, fz - 0.625])]
    for ring, (p, tilt, n, off) in enumerate((("petal", 62, 6, 0), ("petal_s", 28, 6, 30))):
        for i in range(n):
            yaw = off + i * 360 / n
            pw, ph_, _ = A.S(p)
            lot.append(cube(p, [fx - pw / 2, fy + 0.2 + 0.2 * ring, fz - 0.4], pivot=[fx, fy + 0.2, fz], rotation=[-tilt, yaw, 0]))
    r.b["head"]["cubes"] += lot
    r.b["head"]["cubes"].append(cube("hair_tie", [fx + 0.4, fy - 0.6, fz + 1.2]))

    # frogspawn bead necklace
    beads = []
    for x in (-3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0):
        y = g.chest_top - 0.9 - 0.9 * (1 - (x / 3.4) ** 2)
        beads.append(cube("bead", [x - 0.5, y, g.chest_front - 0.75]))
    for sx in (-1, 1):
        beads.append(cube("bead", [sx * 3.65 - 0.5, g.chest_top - 1.0, g.chest_front + 0.4]))
        beads.append(cube("bead", [sx * 3.95 - 0.5, g.chest_top - 0.95, g.chest_front + 1.6]))
    r.put("body", *beads)

    # reed wrap and a rope belt with a shell clasp
    r.put("body", cube("wrap", [-4.0, g.chest_y0 - 0.25, g.chest_front - 0.25]), cube("belt", [-4.25, g.hips_y0 + 1.9, -2.75]),
          cube("shell", [-0.625, g.hips_y0 + 1.95, -3.2]))

    # lily-pad skirt: eight pads fanned round the hips, drooping
    pads = []
    for i in range(8):
        pads.append(cube("lily", [-2.25, g.hips_y0 + 1.75, -6.0], pivot=[0, g.hips_y0 + 1.9, 0], rotation=[16 + 5 * (i % 2), i * 45 + 12 * (i % 2), 0]))
    r.add("skirt", "body", [0, g.hips_y0 + 1.9, 0], pads)

    # three cattails strapped to her back
    cats = []
    for x, rz, rx in ((-2.2, 10, 8), (0.4, -3, 12), (2.4, -14, 7)):
        base = [x, g.mid_y0 + 0.5, 3.0]
        cats.append(cube("cat_stalk", [x - 0.25, base[1], 3.0], pivot=base, rotation=[rx, 0, rz]))
        cats.append(cube("cat_head", [x - 0.5, base[1] + 6.5, 2.75], pivot=base, rotation=[rx, 0, rz]))
        cats.append(cube("cat_tip", [x - 0.25, base[1] + 10.0, 3.0], pivot=base, rotation=[rx, 0, rz]))
    r.add("cattails", "body", [0, g.mid_y0 + 0.5, 3.0], cats)


def build_model():
    m, g = A.build(SPEC, extras, NAME)
    build_model.g = g
    return m


# ----------------------------------------------------------------------------- animations
TUNE = SimpleNamespace(crouch=14, step=16, hop_lift=5.0, throat_out=(0, -1.9, -2.6), puff_f=1.4, croak_crouch=26, sit_crouch=62,
                       tongue_len=5.8, arm_rest=(3, 0, 6), extra={})


def animations(an):
    g = build_model.g
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    sway = lambda amp, per=4.0: T((0, (0, 0, 0)), (per / 4, (amp, 0, 0)), (per / 2, (0, 0, 0)), (3 * per / 4, (-amp, 0, 0)), (per, (0, 0, 0)))
    TUNE.extra = {
        "idle": {"skirt": {"rotation": sway(2)}, "cattails": {"rotation": T((0, (0, 0, 0)), (2, (2, 0, 3)), (4, (0, 0, 0)))}},
        "walk": {"skirt": {"rotation": T((0, (0, 0, 4)), (0.5, (0, 0, -4)), (1, (0, 0, 4)))}, "cattails": {"rotation": T((0, (3, 0, 4)), (0.5, (3, 0, -4)), (1, (3, 0, 4)))}},
        "hop": {"skirt": {"rotation": T((0, (0, 0, 0)), (0.3, (-18, 0, 0)), (0.5, (-8, 0, 0)), (0.74, (10, 0, 0)), (0.9, (0, 0, 0)))},
                "cattails": {"rotation": T((0, (0, 0, 0)), (0.3, (16, 0, 0)), (0.74, (-8, 0, 0)), (0.9, (0, 0, 0)))}},
        "swim": {"skirt": {"rotation": hold(-30, 0, 0)}, "cattails": {"rotation": hold(-70, 0, 0)}},
        "croak": {"skirt": {"rotation": sway(3, 3.0)}},
        "sit": {"skirt": {"rotation": hold(6, 0, 0)}, "cattails": {"rotation": hold(-6, 0, 0)}},
    }
    A.common_anims(an, g, TUNE)
    OUT, REST = H.OUT_, H.REST
    pose = lambda deg: (-deg, 2 * deg, 0, 0)

    # bellow: the pond contest - chest out, head back, sac fully swollen, mouth wide, arms flung out
    sac = (0, -2.3, -3.0)
    wob = [(0, REST), (0.2, sac), (0.45, (0, -1.9, -2.6)), (0.7, sac), (0.95, (0, -1.9, -2.6)), (1.2, sac), (1.45, (0, -1.9, -2.6)), (1.7, sac), (2.0, sac), (2.3, REST), (2.4, REST)]
    lg, by = A.gait(g.T, g.Sh, 2.4, [(0, pose(6)), (2.4, pose(6))], [(0, pose(6)), (2.4, pose(6))])
    for s_ in ("r", "l"):
        lg["leg_" + s_]["position"] = by
    lg["body"] = {"position": by, "rotation": T((0, (0, 0, 0)), (0.3, (-12, 0, 0)), (2.0, (-12, 0, 0)), (2.4, (0, 0, 0)))}
    an.add("bellow", 2.4, True, merge(
        lg,
        {"throat": {"position": T(*wob)},
         "mouth": {"position": T((0, REST), (0.3, OUT), (1.9, OUT), (2.3, REST), (2.4, REST))},
         "belly": {"position": T((0, REST), (0.3, (0, 0, -1.4)), (2.0, (0, 0, -1.4)), (2.4, REST))},
         "head": {"rotation": T((0, (0, 0, 0)), (0.3, (-34, 0, 0)), (2.0, (-34, 0, 0)), (2.4, (0, 0, 0)))},
         "eyelids": {"position": T((0, REST), (0.3, OUT), (2.0, OUT), (2.4, REST))},
         "skirt": {"rotation": T((0, (0, 0, 0)), (0.3, (-12, 0, 0)), (2.0, (-12, 0, 0)), (2.4, (0, 0, 0)))},
         "cattails": {"rotation": T((0, (0, 0, 0)), (0.3, (-8, 0, 0)), (2.0, (-8, 0, 0)), (2.4, (0, 0, 0)))},
         "hair_back": {"rotation": T((0, (0, 0, 0)), (0.3, (-10, 0, 0)), (2.4, (0, 0, 0)))}},
        sym("arm", T((0, (3, 0, 6)), (0.3, (-10, 0, 52)), (2.0, (-10, 0, 52)), (2.4, (3, 0, 6)))),
        sym("forearm", hold(-30, 0, 0)), sym("hand", hold(-6, 0, 0))))


def main():
    declare_parts()
    pt_texture = paint
    H.run(NAME, pt_texture, build_model, animations, speak="croak", head_center=(0, 24.5), body_center=(0, 16.5), scale=12.5,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("hop", 0.4, -30, 10), ("swim", 0.3, -40, 18), ("bellow", 1.0, -28, 8), ("tongue", 0.3, -32, 10)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("bellow", 1.0, 0, 0), ("tongue", 0.3, 0, 0), ("idle", 0, -30, 8)])
    if HUMANOID:
        write_variants()


if __name__ == "__main__":
    main()
