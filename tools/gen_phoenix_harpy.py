#!/usr/bin/env python3
"""Phoenix Harpy - species definition. The shared engine lives in harpy_lib.py.

    python tools/gen_phoenix_harpy.py                 # write the asset files (+ the glow mask)
    python tools/gen_phoenix_harpy.py --preview x.png # also render preview sheets (x.png, x_face.png, x_body.png)

Field guide: "Medium-sized, Nether-occupying fierce fighter. Reborn after being slain and burning to ashes; a
reborn phoenix imprints on the first thing it sees, so enemies can become friends. Its magical tears heal players
with the Regeneration effect. If friends with a player, it deflects all ghast fireballs."
Design: crimson-to-gold plumage, hair that burns from deep red at the roots to yellow-white at the tips, golden
eyes, long trailing tail plumes; crimson silk with gold trim and a gold sun pendant, gold leg bands. The bright
flame colours also go into phoenix_harpy_glowmask.png so GeckoLib renders them glowing in the dark.
"""
import copy
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import variant_tools as VT
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

NAME = "phoenix_harpy"
L = 1.0                                          # medium: crow height

# ----------------------------------------------------------------------------- palette
PAL = H.Pal()
PAL.HAIR = ramp([rgb(110, 14, 10), rgb(170, 30, 14), rgb(222, 70, 20), rgb(250, 130, 30), rgb(255, 196, 70), rgb(255, 240, 170)])
PAL.hair_gain, PAL.hair_off = 1.0, 0.08                       # bright: the tips burn yellow-white
PAL.FEA = ramp([rgb(90, 10, 10), rgb(150, 24, 16), rgb(206, 52, 20), rgb(240, 104, 28), rgb(252, 160, 44), rgb(255, 214, 96), rgb(255, 246, 190)])
PAL.EYE = ramp([rgb(170, 90, 10), rgb(220, 140, 20), rgb(250, 190, 40), rgb(255, 224, 110), rgb(255, 248, 200)])
PAL.LEG = ramp([rgb(150, 90, 30), rgb(200, 130, 40), rgb(236, 172, 60), rgb(250, 206, 100), rgb(255, 232, 160)])
PAL.TALON = [rgb(60, 30, 20), rgb(120, 70, 40)]
PAL.EYE_OUT, PAL.PUPIL = rgb(90, 30, 10), rgb(60, 14, 6)
PAL.BROW = PAL.HAIR[1]
PAL.eye_slant = 0.8                                           # a fierce look
PAL.lash_extra = [(3, 7)]
PAL.wing_base_t, PAL.tail_fluff_t, PAL.fluff_t = (0.30, 0.35), 0.60, 0.55

SILK = ramp([rgb(70, 8, 16), rgb(110, 14, 24), rgb(156, 24, 34), rgb(196, 44, 48), rgb(226, 86, 70)])
GOLD = ramp([rgb(150, 100, 30), rgb(210, 160, 50), rgb(250, 210, 90), rgb(255, 240, 170)])

# ----------------------------------------------------------------------------- flame variants (texture + glow mask)
# Each variant is a different flame: its palette, its silk/trim ramps and the colour test that decides which pixels
# glow (the "hot" tips of that flame). Keep in sync with PhoenixHarpy.Variant.
CLASSIC_HOT = lambda r, g, b: r >= 200 and r > b + 90 and (g >= 60 or r >= 230)
DEFAULT = SimpleNamespace(pal=PAL, silk=SILK, gold=GOLD, hot=CLASSIC_HOT)


def pvariant(hair, fea, eye, leg, silk, gold, hot, pupil, eye_out, talon=None):
    rg = lambda colors: ramp([rgb(*c) for c in colors])
    p = copy.copy(PAL)
    p.HAIR, p.FEA, p.EYE, p.LEG = rg(hair), rg(fea), rg(eye), rg(leg)
    p.BROW = p.HAIR[1]
    p.PUPIL, p.EYE_OUT = rgb(*pupil), rgb(*eye_out)
    if talon:
        p.TALON = [rgb(*c) for c in talon]
    return SimpleNamespace(pal=p, silk=rg(silk), gold=rg(gold), hot=hot)


VARIANTS = {
    # azure phoenix: a blue flame, white-blue at the tips; icy eyes, silver trim
    "azure": pvariant(
        [(10, 24, 110), (20, 60, 180), (40, 110, 230), (90, 170, 250), (160, 220, 255), (225, 248, 255)],
        [(8, 20, 90), (16, 50, 150), (30, 100, 210), (60, 150, 246), (110, 200, 255), (180, 236, 255), (240, 252, 255)],
        [(40, 100, 160), (70, 150, 210), (120, 200, 240), (190, 236, 255), (240, 252, 255)],
        [(40, 80, 140), (70, 120, 190), (120, 170, 225), (180, 214, 245), (230, 244, 255)],
        [(8, 16, 70), (14, 30, 110), (24, 52, 156), (44, 84, 200), (80, 130, 236)],
        [(100, 120, 150), (160, 184, 214), (214, 232, 250), (245, 252, 255)],
        lambda r, g, b: b >= 200 and b > r + 70 and g >= 110, (10, 30, 60), (10, 40, 90), talon=[(20, 40, 80), (60, 100, 150)]),
    # verdant phoenix: a green-teal flame like warped fire; lime-gold eyes
    "verdant": pvariant(
        [(4, 60, 40), (10, 110, 70), (24, 170, 110), (70, 220, 150), (150, 250, 200), (220, 255, 240)],
        [(4, 44, 34), (8, 90, 64), (16, 150, 100), (40, 206, 140), (100, 244, 180), (180, 255, 220), (240, 255, 248)],
        [(90, 120, 10), (150, 190, 20), (200, 240, 50), (230, 255, 120), (250, 255, 200)],
        [(20, 90, 70), (40, 140, 100), (80, 190, 140), (140, 230, 190), (200, 250, 225)],
        [(4, 40, 30), (8, 70, 50), (14, 110, 76), (30, 150, 104), (60, 196, 140)],
        [(110, 130, 30), (170, 200, 50), (220, 245, 110), (245, 255, 190)],
        lambda r, g, b: g >= 190 and g > r + 60 and g > b + 10, (10, 50, 20), (10, 70, 40), talon=[(10, 40, 30), (50, 100, 80)]),
    # solar phoenix: a white-gold flame, like a small sun; ivory and gold trim
    "solar": pvariant(
        [(160, 110, 20), (210, 160, 40), (246, 206, 80), (255, 236, 140), (255, 248, 200), (255, 255, 240)],
        [(150, 100, 20), (200, 150, 36), (240, 200, 70), (255, 230, 120), (255, 244, 170), (255, 252, 215), (255, 255, 245)],
        [(200, 150, 20), (240, 200, 50), (255, 230, 110), (255, 244, 180), (255, 252, 225)],
        [(180, 130, 40), (220, 170, 60), (246, 206, 100), (255, 230, 150), (255, 248, 210)],
        [(120, 90, 40), (170, 136, 64), (214, 184, 96), (240, 222, 150), (255, 244, 200)],
        [(190, 140, 40), (240, 200, 80), (255, 236, 150), (255, 250, 220)],
        lambda r, g, b: r >= 225 and g >= 180, (110, 70, 10), (130, 90, 20), talon=[(100, 70, 30), (180, 140, 70)]),
    # ember phoenix: charred ash-grey plumage that only glows orange at the tips and the cracks; copper trim
    "ember": pvariant(
        [(30, 28, 30), (52, 48, 50), (84, 76, 76), (120, 108, 104), (170, 140, 110), (230, 150, 70)],
        [(24, 22, 24), (48, 42, 44), (82, 70, 70), (126, 100, 90), (190, 120, 70), (240, 150, 60), (255, 200, 110)],
        [(120, 30, 10), (180, 60, 14), (230, 100, 24), (255, 150, 50), (255, 200, 110)],
        [(40, 36, 36), (66, 58, 56), (98, 84, 80), (140, 118, 108), (190, 160, 140)],
        [(30, 20, 22), (54, 34, 34), (86, 52, 48), (130, 74, 56), (180, 100, 60)],
        [(90, 50, 20), (140, 84, 36), (200, 130, 60), (250, 190, 100)],
        CLASSIC_HOT, (40, 14, 6), (50, 20, 10)),
}


# ----------------------------------------------------------------------------- parts
def declare_parts():
    H.standard_parts()
    for args in [("sun", 1.5, 1.5, .5), ("legband", 1.75, .75, 1.75), ("plume", 1.5, 15, .5), ("crest", 1, 5, .5),
                 ("prim_a", 1.5, 11, .5), ("prim_b", 1.5, 11, .5)]:
        H.part(*args)


# ----------------------------------------------------------------------------- texture
def paint(v=None):
    v = v or DEFAULT
    pt = H.Painter()
    pal, FEA, SKIN = v.pal, v.pal.FEA, v.pal.SKIN
    SILK, GOLD = v.silk, v.gold                            # shadow the module-level defaults
    D = H.D
    H.paint_head(pt, pal)
    H.paint_hair(pt, pal)
    # wing and tail feathers: crimson at the base, flaring to gold and pale fire at the tips
    H.paint_wings_tail(pt, pal, {
        "sec_a": (1, 3, 5, None), "sec_b": (2, 3, 5, None), "prim_a": (1, 3, 6, None), "prim_b": (2, 4, 6, None),
        "tail_f": (1, 3, 5, None), "tail_g": (2, 4, 5, None),
        "cov_a": (2, 3, 4, None, True), "cov_b": (2, 3, 4, None, True), "cov_c": (3, 4, 5, None, True),
    })
    H.paint_body_skin(pt, pal)
    H.paint_legs(pt, pal)
    for p, spec in (("tassel", (2, 4, 5)), ("cuff_f", (2, 4, 6)), ("trim", (3, 4, 6)), ("hfeather", (3, 5, 6)), ("crest", (2, 4, 6))):
        pt(p, "all", H.feather_fn(pal, *spec, None, False, False))
    pt("plume", "all", H.feather_fn(pal, 1, 3, 6, 5, False, False))
    pt("back_cover", "all", lambda f, x, y, w, h: pick(FEA, 0.30 + 0.35 * y / h + (noise(x, y, 19) - 0.5) * 0.12, x, y))

    # crimson silk wrap with gold trim
    def silk(f, x, y, w, h):
        if f == "up":
            return pick(SKIN, 0.66, x, y)
        if f == "down":
            return pick(SKIN, 0.5, x, y)
        if y < D // 2 or y >= h - D // 2:
            return GOLD[1 + (x % 2)]
        t = 0.5 + 0.25 * ((x + y // 2) % 6 == 0) + (noise(x, y, 31) - 0.5) * 0.15 - (0.12 if f in ("east", "west", "south") else 0)
        return pick(SILK, t, x, y)
    pt("chest", "all", silk)
    pt("bust", "all", silk)
    pt("sash", "all", lambda f, x, y, w, h: GOLD[2] if y in (0, h - 1) else pick(SILK, 0.35 + 0.2 * ((x + y) % 4 == 0), x, y))
    pt("cord", "all", lambda f, x, y, w, h: GOLD[1])
    pt("pendant", "all", lambda f, x, y, w, h: GOLD[3] if (x + y) % 2 == 0 else GOLD[2])
    pt("sun", "all", lambda f, x, y, w, h: GOLD[3] if f in BROAD and abs(x - w / 2.0 + 0.5) + abs(y - h / 2.0 + 0.5) < 2 else GOLD[1])
    pt("legband", "all", lambda f, x, y, w, h: GOLD[2] if y == 1 else GOLD[1])

    def cloth(broad):
        def fn(f, x, y, w, h):
            if f not in broad:
                return SILK[1]
            r = h - 1 - y
            tl = 1.8 * D
            if r < tl:
                half = (w / 2.0) * (min(1.0, (r + 1) / tl) ** 0.5)
                if abs(x + 0.5 - w / 2.0) > half:
                    return CLEAR                                      # tongue-of-flame tips
            if y < D:
                return GOLD[1] if (x // 2) % 2 else GOLD[0]
            t = 0.45 + 0.5 * (y / h) + (noise(x, y, 43) - 0.5) * 0.15
            return pick(FEA, t, x, y) if y > h * 0.6 else pick(SILK, t, x, y)   # the hem burns into flame colours
        return fn
    for p in ("leaf_s", "leaf_m", "leaf_l"):
        pt(p, "all", cloth(BROAD))
    pt("leaf_side", "all", cloth(("east", "west")))
    pt("bracer", "all", lambda f, x, y, w, h: GOLD[2] if y in (0, 1) else pick(GOLD, 0.35 + (noise(x, y, 47) - 0.5) * 0.3, x, y))
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

    body = [
        C("hips", [-2.5, 12, -1.75]), C("midriff", [-1.75, 13.25, -1.25]), C("chest", [-2.5, 16, -1.625]),
        C("collar", [-1.75, 19, -1.375]), C("sash", [-2.75, 12.6, -2.0]), C("back_cover", [-1.75, 15.5, 1.625]),
        C("sun", [-0.75, 16.9, -2.7]), C("cord", [-1.5, 18.55, -2.45]),
    ]
    bust = C("bust", [-2.25, 16.9, -2.375])
    body += [bust, mirror_cube(bust)]
    for i, x in enumerate((-2.5, -1.5, -0.5, 0.5, 1.5)):
        body.append(HG("trim", x, 16.1, -2.05, 6 if i % 2 else -6))
    bone("body", "root", [0, 12 + L, 0], body)

    sk = []
    front = [("leaf_l", -3.0, 6), ("leaf_m", -1.5, -4), ("leaf_l", -0.2, 2), ("leaf_m", 1.0, -3), ("leaf_l", 2.0, 7)]
    for i, (p, x, rz) in enumerate(front):
        sk.append(HG(p, x, 13.7, -2.7 - 0.15 * (i % 2), rz, -9))
    for i, (p, x, rz) in enumerate((("leaf_l", -3.0, 4), ("leaf_m", -1.0, -3), ("leaf_l", 0.8, 4), ("leaf_m", 2.0, -4))):
        sk.append(HG(p, x, 13.7, 2.2 + 0.15 * (i % 2), rz, 9))
    for z in (-2.2, 0.0):
        sk.append(C("leaf_side", [-3.4, 8.2, z], pivot=[-3.15, 13.7, z + 1.25], rotation=[0, 0, -9]))
        sk.append(mirror_cube(sk[-1]))
    bone("skirt", "body", [0, 13.7 + L, 0], sk)

    # ---- head: flame hair swept up and back, a crest of three fire plumes
    head = [C("head", [-4, 20, -4]), C("hair_cap", [-4, 20, -4], inflate=0.75)]
    ear_r = C("ear", [-5.0, 21.7, -1.2])
    head += [ear_r, mirror_cube(ear_r)]
    for i, (p, x, rz) in enumerate((("bang_b", -4.6, -9), ("bang_a", -3.0, 6), ("bang_c", -1.6, -5), ("bang_a", -0.1, 7),
                                    ("bang_c", 1.4, -6), ("bang_b", 3.0, 10))):
        head.append(HG(p, x, 28.4, -5.35 + 0.1 * (i % 2), rz))
    head.append(HG("frame", -4.9, 28, -5.0, -8))
    head.append(mirror_cube(head[-1]))
    for x, ang in ((-1.2, -16), (0.0, 0), (1.2, 16)):
        head.append(C("crest", [x - 0.5, 28.3, -0.5], pivot=[x, 28.3, 0], rotation=[-28, 0, ang]))
    bone("head", "body", [0, 20 + L, 0], head)

    bone("eyelids", "head", [0, 23.5 + L, -3.8], [C("eyelids", [-3.25, 21.75, -3.9])])
    bone("mouth", "head", [0, 21 + L, -3.8], [C("mouth", [-0.875, 20.25, -3.9])])

    side = [("strand_m1", -6.1, 26.2, -3.4, -9, 0), ("strand_m2", -6.3, 25.2, -2.0, -12, 0),
            ("strand_m1", -6.2, 26.2, 0.8, -10, 4), ("strand_m2", -6.0, 26.2, 2.4, -13, 5)]
    bone("hair_side_r", "head", [-4.7, 27 + L, 0], [HG(*s_) for s_ in side], mirror=True)
    back = []
    names = ["strand_m1", "strand_l1", "strand_m2", "strand_l2", "strand_m1"]
    for i in range(11):
        x = -4.8 + 0.96 * i
        back.append(HG(names[i % 5], x, 26.8, 4.9 + 0.3 * (i % 3), x * 2.4, 10 + 3 * (i % 3)))
    bone("hair_back", "head", [0, 27 + L, 4.5], back)

    bone("arm_r", "body", [-3.25, 19.5 + L, 0], [C("arm_up", [-4.0, 16, -0.75])], mirror=True)
    bone("forearm_r", "arm_r", [-3.25, 16 + L, 0], [
        C("forearm", [-4.0, 13, -0.75]), C("bracer", [-4.5, 13.6, -1.25]),
        HG("cuff_f", -4.15, 13.8, -1.45, 4, -6), HG("cuff_f", -3.25, 13.8, -1.45, -4, -6), HG("cuff_f", -3.65, 13.8, 1.0, 0, 6),
    ], mirror=True)
    bone("hand_r", "forearm_r", [-3.25, 13 + L, 0], [
        C("palm", [-4.0, 11.5, -0.5]), C("fingers", [-4.0, 10.5, -0.5]), C("thumb", [-2.5, 11.5, -0.25]),
    ], mirror=True)

    # ---- wings: broad, the primaries flaring wide like flames
    w = [C("wing_base", [-7, 10, 2.3])]
    rows = [(19.5, (-7.0, -5.0), ("cov_a", "cov_a")), (16.8, (-8.5, -6.5, -4.5), ("cov_a", "cov_b", "cov_a")),
            (14.1, (-9.5, -7.5, -5.5), ("cov_b", "cov_b", "cov_b")), (11.4, (-8.5, -6.5, -4.5), ("cov_c", "cov_c", "cov_c"))]
    for r, (ytop, xs, ps) in enumerate(rows):
        for x, p in zip(xs, ps):
            w.append(C(p, [x, ytop - 3, 3.3 + 0.2 * r]))
    for i, (x, ang) in enumerate(zip((-9.5, -8.25, -7, -5.75, -4.5), (-12, -9, -6, -3, 0))):
        w.append(HG("sec_a" if i % 2 == 0 else "sec_b", x, 12.5, 2.3 + 0.25 * (i % 2), ang))
    bone("wing_r", "body", [-2.5, 19 + L, 2.3], w, mirror=True)
    pr = []
    for i, (x, ang) in enumerate(zip((-10.5, -9, -7.5, -6, -4.5), (-28, -21, -14, -8, -3))):
        pr.append(HG("prim_a" if i % 2 == 0 else "prim_b", x, 12.5, 2.8 + 0.25 * (i % 2), ang))
    bone("wing_tip_r", "wing_r", [-8, 12.5 + L, 3.0], pr, mirror=True)

    # ---- tail: a fan of feathers plus three long trailing plumes
    tail = [C("tail_fluff", [-1.75, 12.2, 2.2])]
    for i, (x, ang) in enumerate(zip((-3.5, -2.1, -0.75, 0.6, 2.0), (-18, -9, 0, 9, 18))):
        tail.append(HG("tail_f" if i % 2 == 0 else "tail_g", x, 12.7, 3.4 + 0.25 * (i % 2), ang, 8))
    for x, ang in ((-1.6, -6), (-0.75, 0), (0.1, 6)):
        tail.append(HG("plume", x, 12.7, 3.9, ang, 14))
    bone("tail", "body", [0, 13.2 + L, 3], tail)

    cx = -1.25
    bone("leg_r", "root", [cx, 12 + L, 0], [C("thigh", [cx - 1, 9.5, -1]), C("fluff", [cx - 1.25, 8.5, -1.25])], mirror=True)
    bone("shin_r", "leg_r", [cx, 9 + L, 0], [cube("shin", [cx - .75, 2, -.75]), cube("ankle", [cx - 1, 1, -1]),
                                              cube("legband", [cx - .875, 6.5, -.875])], mirror=True)
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
    for f in (H.std_walk, H.std_fly):
        f(an)
    OUT, REST = H.OUT_, H.REST
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    H.std_idle(an)
    # idle: on top of the standard idle, the hair and plumes flicker like flames
    idle = an.A[f"animation.{NAME}.idle"]["bones"]
    flicker = {"0.0": [4, 0, 0], "0.25": [10, 0, 0], "0.5": [3, 0, 0], "0.75": [9, 0, 0], "1.0": [4, 0, 0],
               "1.25": [11, 0, 0], "1.5": [2, 0, 0], "1.75": [8, 0, 0], "2.0": [4, 0, 0], "4.0": [4, 0, 0]}
    idle["hair_back"] = {"rotation": flicker}

    # strike: wings flared high, a lunge and a raking slash of the talons
    an.add("strike", 0.8, True, merge({
        "body": {"rotation": T((0, (10, 0, 0)), (0.3, (24, 0, 0)), (0.5, (-6, 0, 0)), (0.8, (10, 0, 0))),
                 "position": T((0, (0, 0, 0)), (0.3, (0, 0, -1.5)), (0.5, (0, 0.5, 1.0)), (0.8, (0, 0, 0)))},
        "head": {"rotation": T((0, (-10, 0, 0)), (0.3, (-20, 0, 0)), (0.8, (-10, 0, 0)))},
        "leg_r": {"rotation": T((0, (0, 0, 0)), (0.3, (-50, 0, 0)), (0.5, (30, 0, 0)), (0.8, (0, 0, 0)))},
        "mouth": {"position": T((0, REST), (0.25, OUT), (0.55, OUT), (0.7, REST), (0.8, REST))},
        "tail": {"rotation": hold(-20, 0, 0)},
    },
        sym("wing", T((0, (0, 0, -100)), (0.3, (0, 0, -140)), (0.6, (0, 0, -60)), (0.8, (0, 0, -100)))),
        sym("arm", T((0, (-40, 0, 20)), (0.3, (-90, 0, 30)), (0.5, (-10, 0, 10)), (0.8, (-40, 0, 20))))))

    # cry: hands to her face, eyes shut, shoulders shaking - her healing tears
    an.add("cry", 2.0, True, merge({
        "head": {"rotation": hold(18, 0, 0)},
        "body": {"rotation": hold(8, 0, 0),
                 "position": T((0, (0, 0, 0)), (0.2, (0, -0.2, 0)), (0.4, (0, 0, 0)), (0.6, (0, -0.2, 0)), (0.8, (0, 0, 0)), (2.0, (0, 0, 0)))},
        "eyelids": {"position": hold(*OUT)},
        "tail": {"rotation": hold(14, 0, 0)},
    },
        sym("arm", hold(-55, 0, 20)), sym("forearm", hold(-95, 0, 0)), sym("hand", hold(-20, 0, 0)),
        sym("wing", T((0, (0, 0, -8)), (1, (0, 0, -14)), (2, (0, 0, -8))))))


def write_glowmask(tex, hot=CLASSIC_HOT, suffix=""):
    """keep only the variant's bright flame colours (`hot` decides); everything else transparent.
    GeckoLib's AutoGlowingGeoLayer looks for <texture>_glowmask.png, so each variant needs its own mask."""
    rows = []
    for row in tex:
        out = []
        for (r, g, b, a) in row:
            out.append((r, g, b, 255) if a and hot(r, g, b) else CLEAR)
        rows.append(out)
    path = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide", "textures", "entity", NAME + suffix + "_glowmask.png")
    H.write_png(path, rows)
    print("glow mask ->", os.path.basename(path))


if __name__ == "__main__":
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="strike", head_center=(0, 22.5 + L), body_center=(0, 15.5 + L),
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("strike", 0.3, -25, 10), ("cry", 0.5, -20, 8), ("walk", 0.25, -35, 12), ("fly", 0.0, -20, 10)],
          face_views=[("idle", 0, 0, 0), ("strike", 0.4, 0, 0), ("cry", 0.5, 0, 0), ("idle", 0, -30, 8)])
    VT.write_variants(NAME, "flame", paint, VARIANTS, build_model, animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.0, -20, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + L))],
                      glow=lambda tex, suffix: write_glowmask(tex, VARIANTS[suffix[1:]].hot if suffix else CLASSIC_HOT, suffix))
