#!/usr/bin/env python3
"""Riflebird Harpy - species definition. The shared engine lives in harpy_lib.py.

    python tools/gen_riflebird_harpy.py                 # write the three asset files
    python tools/gen_riflebird_harpy.py --preview x.png # also render preview sheets (x.png, x_face.png, x_body.png)

Field guide: "Small rainforest dweller. Dances to impress mates, sometimes in front of players for affection.
Given diamonds, emeralds or any type of food, it bonds and sometimes lays unfertilized eggs; it does not mind
the player taking them. Its songs sound like pop songs." Showy, selective, curious.
Design: small; velvety black plumage with an iridescent blue-green throat shield (the riflebird's display
gorget), black hair with a blue-green sheen, dark eyes with long lashes; a sleek black outfit with a sequinned
belt, one sparkly white glove and a little black fedora with a white band (a nod to the pop-star reference).
A "dance" animation raises the wings into the riflebird's round display fan and sways side to side.
"""
import copy
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import variant_tools as VT
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

NAME = "riflebird_harpy"
L = 0.5                                          # just a touch taller than the sparrow

# ----------------------------------------------------------------------------- palette
PAL = H.Pal()
PAL.HAIR = ramp([rgb(8, 8, 14), rgb(16, 18, 28), rgb(24, 34, 48), rgb(30, 70, 82), rgb(40, 120, 120), rgb(90, 190, 180)])
PAL.hair_gain, PAL.hair_off = 0.55, -0.02                     # near-black, the blue-green sheen only on the gloss
PAL.FEA = ramp([rgb(6, 6, 10), rgb(14, 14, 22), rgb(24, 24, 36), rgb(38, 40, 58), rgb(60, 64, 88), rgb(96, 104, 132), rgb(150, 160, 190)])
PAL.EYE = ramp([rgb(30, 18, 14), rgb(56, 34, 24), rgb(88, 56, 36), rgb(120, 82, 52), rgb(160, 118, 80)])
PAL.LEG = ramp([rgb(10, 10, 14), rgb(22, 22, 30), rgb(36, 36, 48), rgb(54, 54, 70), rgb(80, 80, 100)])
PAL.TALON = [rgb(8, 8, 10), rgb(40, 40, 50)]
PAL.EYE_OUT, PAL.PUPIL = rgb(14, 8, 8), rgb(6, 4, 4)
PAL.BROW = PAL.HAIR[1]
PAL.lash_extra = [(3, 7), (4, 6)]                               # long lashes: a showgirl
PAL.wing_base_t, PAL.tail_fluff_t, PAL.fluff_t = (0.05, 0.12), 0.20, 0.15

GORGET = ramp([rgb(10, 60, 70), rgb(20, 120, 130), rgb(40, 180, 170), rgb(110, 230, 200), rgb(200, 255, 240)])
SEQUIN = ramp([rgb(150, 150, 170), rgb(210, 214, 230), rgb(255, 255, 255)])
GLOVE = ramp([rgb(200, 200, 210), rgb(232, 232, 240), rgb(255, 255, 255)])
SATIN = ramp([rgb(10, 10, 14), rgb(22, 22, 30), rgb(40, 40, 54), rgb(70, 70, 90)])

# ----------------------------------------------------------------------------- species variants (texture only)
# The default is the magnificent riflebird. `shift` brightens the feather indices of wings, tail and trim (the dark
# default specs would be too dim on a red or brown ramp). Keep in sync with RiflebirdHarpy.Variant.
DEFAULT = SimpleNamespace(pal=PAL, gorget=GORGET, satin=SATIN, sequin=SEQUIN, shift=0)


def rvariant(hair, fea, gorget, satin, leg=None, eye=None, sequin=None, shift=0, gain=0.55):
    rg = lambda colors: ramp([rgb(*c) for c in colors])
    p = copy.copy(PAL)
    p.HAIR, p.FEA = rg(hair), rg(fea)
    p.BROW = p.HAIR[1]
    p.hair_gain = gain
    if leg:
        p.LEG = rg(leg)
    if eye:
        p.EYE = rg(eye)
    return SimpleNamespace(pal=p, gorget=rg(gorget), satin=rg(satin), sequin=rg(sequin) if sequin else SEQUIN, shift=shift)


BLUE_LEGS = [(40, 60, 110), (60, 90, 160), (90, 130, 200), (130, 170, 230), (180, 210, 250)]
VARIANTS = {
    # Victoria's riflebird: black with an oil-slick violet sheen and a blue-violet-to-green breast shield
    "victoria": rvariant([(8, 6, 16), (18, 12, 34), (34, 22, 62), (60, 40, 100), (90, 70, 140), (130, 110, 180)],
                         [(6, 4, 12), (14, 10, 26), (26, 18, 48), (44, 30, 80), (68, 50, 116), (106, 88, 160), (160, 146, 206)],
                         [(20, 24, 90), (40, 70, 170), (60, 150, 200), (100, 210, 200), (200, 250, 240)],
                         [(12, 8, 22), (26, 18, 44), (46, 34, 72), (78, 60, 110)]),
    # paradise riflebird: deep midnight-blue sheen and a bright teal shield
    "paradise": rvariant([(6, 8, 24), (12, 20, 60), (22, 44, 110), (40, 80, 170), (70, 120, 210), (120, 170, 240)],
                         [(4, 8, 22), (10, 20, 50), (18, 38, 92), (30, 64, 140), (50, 96, 190), (96, 150, 230), (160, 200, 250)],
                         [(10, 50, 90), (20, 110, 160), (40, 180, 200), (110, 236, 224), (210, 255, 248)],
                         [(8, 12, 30), (16, 28, 60), (30, 52, 104), (56, 88, 156)]),
    # hen: the female riflebird is a barred buff and brown bird - brown hair, buff bib, tan satin, cream sequins
    "hen": rvariant([(52, 36, 26), (84, 60, 40), (120, 88, 58), (158, 122, 84), (196, 160, 116), (226, 198, 156)],
                    [(40, 28, 20), (66, 48, 34), (98, 74, 52), (134, 106, 76), (176, 146, 108), (216, 190, 150), (246, 230, 196)],
                    [(150, 120, 80), (190, 160, 110), (220, 196, 150), (238, 222, 184), (250, 242, 214)],
                    [(40, 30, 24), (70, 52, 40), (104, 80, 60), (140, 112, 86)],
                    leg=[(70, 60, 56), (100, 88, 82), (130, 116, 108), (160, 146, 136), (190, 176, 166)],
                    sequin=[(180, 160, 120), (226, 208, 166), (255, 246, 220)], shift=2, gain=1.0),
    # king bird-of-paradise cousin: crimson plumage, an emerald breast band, white satin, bright blue legs
    "king": rvariant([(70, 6, 12), (120, 14, 22), (170, 26, 34), (210, 50, 50), (240, 90, 80), (255, 140, 120)],
                     [(60, 6, 10), (110, 14, 22), (168, 26, 32), (210, 50, 44), (236, 92, 76), (250, 170, 150), (255, 235, 225)],
                     [(10, 70, 40), (20, 130, 70), (50, 190, 110), (130, 236, 170), (210, 255, 230)],
                     [(150, 150, 160), (190, 190, 200), (220, 220, 230), (240, 240, 246)], leg=BLUE_LEGS,
                     sequin=[(170, 130, 50), (230, 190, 90), (255, 240, 170)], shift=2, gain=0.9),
    # Wilson's bird-of-paradise cousin: cyan crown, scarlet wings, a golden-yellow cape, black satin, blue legs
    "wilsons": rvariant([(10, 70, 110), (20, 120, 170), (40, 170, 210), (90, 210, 235), (170, 240, 250), (220, 252, 255)],
                        [(80, 8, 10), (140, 18, 20), (200, 36, 30), (236, 70, 50), (250, 120, 70), (255, 210, 90), (255, 245, 170)],
                        [(150, 100, 10), (214, 160, 24), (250, 208, 60), (255, 236, 130), (255, 250, 200)],
                        [(10, 10, 14), (22, 22, 30), (40, 40, 54), (70, 70, 90)], leg=BLUE_LEGS,
                        sequin=[(190, 150, 30), (240, 206, 70), (255, 246, 190)], shift=2, gain=0.9),
}


# ----------------------------------------------------------------------------- parts
def declare_parts():
    H.standard_parts()
    for args in [("gorget", 5, 2.5, .5), ("hat_brim", 7, .5, 6), ("hat_crown", 5, 2.5, 4), ("hat_band", 5.25, .75, 4.25),
                 ("glove", 1.75, 2.75, 1.25), ("tail_f", 1.5, 7, .5), ("tail_g", 1.5, 7, .5)]:
        H.part(*args)


# ----------------------------------------------------------------------------- texture
def paint(v=None):
    v = v or DEFAULT
    pt = H.Painter()
    pal, FEA, SKIN = v.pal, v.pal.FEA, v.pal.SKIN
    GORGET, SATIN, SEQUIN = v.gorget, v.satin, v.sequin      # shadow the module-level defaults
    sh = lambda a, b, c, *rest: (min(6, a + v.shift), min(6, b + v.shift), min(6, c + v.shift)) + rest
    D = H.D
    H.paint_head(pt, pal)
    H.paint_hair(pt, pal)
    H.paint_wings_tail(pt, pal, {
        "sec_a": sh(2, 1, 0, None, True), "sec_b": sh(1, 1, 0, None, True), "prim_a": sh(1, 0, 0, None, True), "prim_b": sh(2, 1, 0, None, True),
        "tail_f": sh(2, 1, 0, None, True), "tail_g": sh(1, 1, 0, None, True),
        "cov_a": sh(2, 2, 1, None, True), "cov_b": sh(2, 1, 1, None, True), "cov_c": sh(1, 1, 0, None, True),
    })
    H.paint_body_skin(pt, pal)
    H.paint_legs(pt, pal)
    for p, spec in (("tassel", (1, 1, 0)), ("cuff_f", (2, 1, 0)), ("trim", (2, 1, 0)), ("hfeather", (1, 3, 5))):
        pt(p, "all", H.feather_fn(pal, *sh(*spec), None, True, False))
    pt("back_cover", "all", lambda f, x, y, w, h: pick(FEA, min(1.0, 0.10 + 0.15 * y / h + (noise(x, y, 19) - 0.5) * 0.08 + 0.145 * v.shift), x, y))

    # iridescent throat shield: blue-green scales, shimmering bands
    def gorget(f, x, y, w, h):
        t = 0.45 + 0.35 * ((x + 2 * y) % 5 == 0) + (noise(x, y, 61) - 0.5) * 0.25 + 0.15 * (y < h // 2)
        if f in BROAD and abs(x + 0.5 - w / 2.0) > (h - y) * w / (2.0 * h) + 1:
            return CLEAR                                                  # tapers to a point
        return pick(GORGET, t, x, y)
    pt("gorget", "all", gorget)

    # sleek black satin top with a sequinned edge
    def top(f, x, y, w, h):
        if f == "up":
            return pick(SKIN, 0.66, x, y)
        if f == "down":
            return pick(SKIN, 0.5, x, y)
        if y >= h - D // 2:
            return SEQUIN[(x + y) % 3]
        t = 0.45 + (noise(x, y, 31) - 0.5) * 0.2 - (0.12 if f in ("east", "west", "south") else 0) + 0.3 * ((x + y) % 7 == 0)
        return pick(SATIN, t, x, y)
    pt("chest", "all", top)
    pt("bust", "all", top)
    pt("sash", "all", lambda f, x, y, w, h: SEQUIN[(x * 3 + y) % 3] if (x + y) % 2 else pick(SATIN, 0.3, x, y))
    pt("pendant", "all", lambda f, x, y, w, h: pick(GORGET, 0.6 + 0.3 * (x < w // 2), x, y))
    pt("cord", "all", lambda f, x, y, w, h: SATIN[1])

    # the hat: black felt, white band
    pt("hat_brim", "all", lambda f, x, y, w, h: pick(SATIN, 0.25 + (noise(x, y, 71) - 0.5) * 0.15 + (0.15 if f == "up" else 0), x, y))
    pt("hat_crown", "all", lambda f, x, y, w, h: pick(SATIN, 0.30 + (noise(x, y, 73) - 0.5) * 0.15 - (0.12 if f == "up" and abs(x - w / 2) < 1.5 else 0), x, y))
    pt("hat_band", "all", lambda f, x, y, w, h: GLOVE[2] if f not in ("up", "down") else CLEAR)
    # the single sparkly glove
    pt("glove", "all", lambda f, x, y, w, h: SEQUIN[2] if noise(x, y, 77) > 0.82 else pick(GLOVE, 0.5 + (noise(x, y, 79) - 0.5) * 0.4, x, y))

    # black satin strips
    def cloth(broad):
        def fn(f, x, y, w, h):
            if f not in broad:
                return SATIN[1]
            r = h - 1 - y
            tl = 1.5 * D
            if r < tl:
                half = (w / 2.0) * (min(1.0, (r + 1) / tl) ** 0.6)
                if abs(x + 0.5 - w / 2.0) > half:
                    return CLEAR
            if y < D:
                return SEQUIN[(x + y) % 3] if (x + y) % 2 else SATIN[0]
            return pick(SATIN, 0.4 + (noise(x, y, 43) - 0.5) * 0.2 + 0.25 * ((x + y) % 9 == 0), x, y)
        return fn
    for p in ("leaf_s", "leaf_m", "leaf_l"):
        pt(p, "all", cloth(BROAD))
    pt("leaf_side", "all", cloth(("east", "west")))
    pt("bracer", "all", lambda f, x, y, w, h: SEQUIN[(x + 2 * y) % 3] if (x + y) % 3 == 0 else pick(SATIN, 0.35, x, y))
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
        C("gorget", [-2.5, 18.6, -1.75], pivot=[0, 20.6, -1.6], rotation=[-8, 0, 0]),
    ]
    bust = C("bust", [-2.25, 16.9, -2.375])
    body += [bust, mirror_cube(bust)]
    bone("body", "root", [0, 12 + L, 0], body)

    sk = []
    front = [("leaf_m", -3.0, 5), ("leaf_l", -1.5, -3), ("leaf_m", -0.2, 2), ("leaf_l", 1.0, -2), ("leaf_m", 2.0, 6)]
    for i, (p, x, rz) in enumerate(front):
        sk.append(HG(p, x, 13.7, -2.7 - 0.15 * (i % 2), rz, -9))
    for i, (p, x, rz) in enumerate((("leaf_m", -3.0, 3), ("leaf_l", -1.0, -2), ("leaf_l", 0.8, 3), ("leaf_m", 2.0, -3))):
        sk.append(HG(p, x, 13.7, 2.2 + 0.15 * (i % 2), rz, 9))
    for z in (-2.2, 0.0):
        sk.append(C("leaf_side", [-3.4, 8.2, z], pivot=[-3.15, 13.7, z + 1.25], rotation=[0, 0, -9]))
        sk.append(mirror_cube(sk[-1]))
    bone("skirt", "body", [0, 13.7 + L, 0], sk)

    # ---- head: sleek black hair, a little fedora tipped forward over one eye
    head = [C("head", [-4, 20, -4]), C("hair_cap", [-4, 20, -4], inflate=0.75)]
    ear_r = C("ear", [-5.0, 21.7, -1.2])
    head += [ear_r, mirror_cube(ear_r)]
    for i, (p, x, rz) in enumerate((("bang_b", -4.6, -4), ("bang_d", -3.0, 3), ("bang_b", -1.6, -2), ("bang_d", -0.1, 3),
                                    ("bang_c", 1.4, -3), ("bang_b", 3.0, 5))):
        head.append(HG(p, x, 28.4, -5.35 + 0.1 * (i % 2), rz))
    head.append(HG("frame", -4.9, 28, -5.0, -3))
    head.append(mirror_cube(head[-1]))
    hat_pivot = [0.5, 28.8, 0]
    head += [C("hat_brim", [-3.0, 28.7, -3.2], pivot=hat_pivot, rotation=[-12, 0, -10]),
             C("hat_crown", [-2.0, 29.2, -2.2], pivot=hat_pivot, rotation=[-12, 0, -10]),
             C("hat_band", [-2.125, 29.2, -2.325], pivot=hat_pivot, rotation=[-12, 0, -10])]
    bone("head", "body", [0, 20 + L, 0], head)

    bone("eyelids", "head", [0, 23.5 + L, -3.8], [C("eyelids", [-3.25, 21.75, -3.9])])
    bone("mouth", "head", [0, 21 + L, -3.8], [C("mouth", [-0.875, 20.25, -3.9])])

    side = [("strand_m1", -6.1, 26.2, -3.4, -4, 0), ("strand_m2", -6.3, 25.2, -2.0, -6, 0),
            ("strand_m1", -6.2, 26.2, 0.8, -5, 3), ("strand_m2", -6.0, 26.2, 2.4, -7, 4)]
    bone("hair_side_r", "head", [-4.7, 27 + L, 0], [HG(*s) for s in side], mirror=True)
    back = []
    names = ["strand_m1", "strand_l1", "strand_m2", "strand_l2", "strand_m1"]
    for i in range(11):
        x = -4.8 + 0.96 * i
        back.append(HG(names[i % 5], x, 26.8, 4.9 + 0.3 * (i % 3), x * 1.2, 4 + 2 * (i % 3)))
    bone("hair_back", "head", [0, 27 + L, 4.5], back)

    bone("arm_r", "body", [-3.25, 19.5 + L, 0], [C("arm_up", [-4.0, 16, -0.75])], mirror=True)
    bone("forearm_r", "arm_r", [-3.25, 16 + L, 0], [
        C("forearm", [-4.0, 13, -0.75]), C("bracer", [-4.5, 13.6, -1.25]),
        HG("cuff_f", -4.15, 13.8, -1.45, 4, -6), HG("cuff_f", -3.25, 13.8, -1.45, -4, -6),
    ], mirror=True)
    # the right hand wears the glove (over the palm), the left is its bare mirror image
    hand = [C("palm", [-4.0, 11.5, -0.5]), C("fingers", [-4.0, 10.5, -0.5]), C("thumb", [-2.5, 11.5, -0.25])]
    bone("hand_r", "forearm_r", [-3.25, 13 + L, 0], hand + [C("glove", [-4.125, 10.375, -0.625])])
    bone("hand_l", "forearm_l", [3.25, 13 + L, 0], [mirror_cube(c) for c in hand])

    w = [C("wing_base", [-7, 10, 2.3])]
    rows = [(19.5, (-7.0, -5.0), ("cov_a", "cov_a")), (16.8, (-8.5, -6.5, -4.5), ("cov_a", "cov_b", "cov_a")),
            (14.1, (-9.5, -7.5, -5.5), ("cov_b", "cov_b", "cov_b")), (11.4, (-8.5, -6.5, -4.5), ("cov_c", "cov_c", "cov_c"))]
    for r, (ytop, xs, ps) in enumerate(rows):
        for x, p in zip(xs, ps):
            w.append(C(p, [x, ytop - 3, 3.3 + 0.2 * r]))
    for i, (x, ang) in enumerate(zip((-9.5, -8.25, -7, -5.75, -4.5), (-10, -7.5, -5, -2.5, 0))):
        w.append(HG("sec_a" if i % 2 == 0 else "sec_b", x, 12.5, 2.3 + 0.25 * (i % 2), ang))
    bone("wing_r", "body", [-2.5, 19 + L, 2.3], w, mirror=True)
    pr = []
    for i, (x, ang) in enumerate(zip((-10.5, -9, -7.5, -6, -4.5), (-16, -12, -8, -5, -2))):
        pr.append(HG("prim_a" if i % 2 == 0 else "prim_b", x, 12.5, 2.8 + 0.25 * (i % 2), ang))
    bone("wing_tip_r", "wing_r", [-8, 12.5 + L, 3.0], pr, mirror=True)

    tail = [C("tail_fluff", [-1.75, 12.2, 2.2])]
    for i, (x, ang) in enumerate(zip((-3.3, -2.0, -0.875, 0.25, 1.5), (-10, -5, 0, 5, 10))):
        tail.append(HG("tail_f" if i % 2 == 0 else "tail_g", x, 12.7, 3.4 + 0.25 * (i % 2), ang, 6))
    bone("tail", "body", [0, 13.2 + L, 3], tail)

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
    for f in (H.std_idle, H.std_walk, H.std_fly, H.std_sing):
        f(an)
    OUT, REST = H.OUT_, H.REST
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold

    # dance: the riflebird display - wings swept up into a round fan above her head, head tucked behind the gorget,
    # swaying side to side on the beat, a toe-tap, and a little spin of the gloved hand
    sway = T((0, (0, 0, -10)), (0.5, (0, 0, 10)), (1.0, (0, 0, -10)), (1.5, (0, 0, 10)), (2.0, (0, 0, -10)))
    an.add("dance", 2.0, True, merge({
        "body": {"rotation": sway,
                 "position": T((0, (0, 0, 0)), (0.25, (0, -0.6, 0)), (0.5, (0, 0, 0)), (0.75, (0, -0.6, 0)), (1.0, (0, 0, 0)),
                               (1.25, (0, -0.6, 0)), (1.5, (0, 0, 0)), (1.75, (0, -0.6, 0)), (2.0, (0, 0, 0)))},
        "head": {"rotation": T((0, (14, 0, 10)), (0.5, (14, 0, -10)), (1.0, (14, 0, 10)), (1.5, (14, 0, -10)), (2.0, (14, 0, 10)))},
        "tail": {"rotation": T((0, (-20, 12, 0)), (0.5, (-20, -12, 0)), (1.0, (-20, 12, 0)), (1.5, (-20, -12, 0)), (2.0, (-20, 12, 0)))},
        "leg_r": {"rotation": T((0, (0, 0, 0)), (0.25, (-18, 0, 0)), (0.5, (0, 0, 0)), (2.0, (0, 0, 0)))},
        "shin_r": {"rotation": T((0, (0, 0, 0)), (0.25, (25, 0, 0)), (0.5, (0, 0, 0)), (2.0, (0, 0, 0)))},
        "arm_r": {"rotation": T((0, (-150, 0, 10)), (1.0, (-160, 0, 20)), (2.0, (-150, 0, 10)))},
        "forearm_r": {"rotation": T((0, (-20, 0, 0)), (0.5, (-40, 40, 0)), (1.0, (-20, 0, 0)), (1.5, (-40, -40, 0)), (2.0, (-20, 0, 0)))},
        "eyelids": {"position": hold(*OUT)},
        "mouth": {"position": T((0, REST), (0.9, REST), (1.0, OUT), (1.2, OUT), (1.3, REST), (2.0, REST))},
    },
        sym("wing", T((0, (0, -20, -150)), (0.5, (0, -26, -160)), (1.0, (0, -20, -150)), (1.5, (0, -26, -160)), (2.0, (0, -20, -150)))),
        sym("wing_tip", hold(0, -30, -30)),
        {"arm_l": {"rotation": hold(-10, 0, -40)}, "forearm_l": {"rotation": hold(-60, 0, 0)}}))


if __name__ == "__main__":
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="sing", head_center=(0, 23.0 + L), body_center=(0, 15.5 + L),
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("dance", 0.25, -20, 10), ("dance", 0.75, 160, 10), ("walk", 0.25, -35, 12), ("fly", 0.0, -20, 10)],
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("dance", 1.1, 0, 0), ("idle", 0, -30, 8)])
    VT.write_variants(NAME, "magnificent", paint, VARIANTS, build_model, animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("dance", 0.25, -20, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 23.0 + L))])
