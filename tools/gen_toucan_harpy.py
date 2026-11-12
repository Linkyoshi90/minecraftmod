#!/usr/bin/env python3
"""Toucan Harpy - species definition. The shared engine lives in harpy_lib.py; the geometry is the seagull's
(gen_seagull_harpy.py), a touch taller.

    python tools/gen_toucan_harpy.py                 # write the three asset files
    python tools/gen_toucan_harpy.py --preview x.png # also render preview sheets
    python tools/gen_toucan_harpy.py --lineup x.png  # also render all variants side by side

Variants (VARIANTS below) only get their own texture, toucan_harpy_<variant>.png; the default (toco) stays
toucan_harpy.png. Keep in sync with ToucanHarpy.Variant.

Field guide: a sociable fruit-eater of the jungle canopy. She talks by clacking her big beak, and toucans who meet
hold a clack-off for the fun of it.
Design: glossy black hair and wings, a white bib, and her famous beak worn as a big
headpiece over her brow (the lower mandible is its own bone, `beak_jaw`, so it can clack); a leafy jungle skirt.
"""
import copy
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

NAME = "toucan_harpy"
L = 0.5                                          # a touch taller than the sparrow

# ----------------------------------------------------------------------------- palette
PAL = H.Pal()
PAL.HAIR = ramp([rgb(12, 12, 14), rgb(24, 24, 28), rgb(40, 40, 46), rgb(62, 62, 70), rgb(96, 96, 106), rgb(140, 140, 150)])
PAL.SKIN = ramp([rgb(196, 150, 132), rgb(222, 182, 162), rgb(240, 210, 192), rgb(250, 230, 214)], nodither=True)
PAL.FEA = ramp([rgb(8, 8, 10), rgb(18, 18, 22), rgb(32, 32, 38), rgb(52, 52, 60), rgb(84, 84, 94), rgb(128, 128, 140), rgb(180, 180, 190)])
PAL.EYE = ramp([rgb(16, 60, 110), rgb(30, 100, 170), rgb(60, 150, 220), rgb(120, 200, 245), rgb(190, 235, 255)])
PAL.LEG = ramp([rgb(40, 60, 110), rgb(60, 86, 150), rgb(86, 116, 186), rgb(120, 150, 214), rgb(160, 186, 236)])
PAL.hair_gain, PAL.hair_off = 0.62, -0.02
PAL.TALON = [rgb(30, 30, 40), rgb(70, 70, 86)]
PAL.EYE_OUT, PAL.PUPIL = rgb(10, 40, 90), rgb(8, 10, 20)
PAL.BROW = PAL.HAIR[1]
PAL.eye_a, PAL.eye_b, PAL.eye_slant = 4.4, 5.2, 0.0           # round, cheerful eyes
PAL.pupil = (1.8, 2.8)
PAL.wing_base_t, PAL.tail_fluff_t, PAL.fluff_t = (0.05, 0.18), 0.30, 0.22

BIB = ramp([rgb(206, 206, 200), rgb(232, 232, 226), rgb(246, 246, 242), rgb(255, 255, 255)])
LEAF = ramp([rgb(20, 70, 30), rgb(34, 104, 44), rgb(56, 140, 60), rgb(96, 180, 84)])
# the beak: (main colour, tip colour)
BEAK = (ramp([rgb(196, 90, 10), rgb(236, 136, 24), rgb(252, 180, 50), rgb(255, 220, 120)]), ramp([rgb(10, 10, 12), rgb(30, 30, 34), rgb(54, 54, 60)]))
RING = ramp([rgb(40, 90, 170), rgb(70, 130, 210), rgb(120, 180, 240)])

WINGS = {
    "sec_a": (1, 2, 3, 0), "sec_b": (0, 2, 3, 1), "prim_a": (0, 1, 2, 0), "prim_b": (1, 2, 3, 1),
    "tail_f": (1, 2, 3, 0), "tail_g": (0, 1, 3, 1),
    "cov_a": (0, 0, 1, None, True), "cov_b": (0, 1, 2, None, True), "cov_c": (1, 2, 3, None, True),
}


# ----------------------------------------------------------------------------- parts
def declare_parts():
    H.standard_parts()
    for args in [("beak_u", 3.5, 2.5, 7.5), ("beak_tip", 3, 2, 1.5), ("beak_lo", 3, 1.25, 7)]:
        H.part(*args)


# ----------------------------------------------------------------------------- texture
def paint(pal=PAL, bib=BIB, beak=BEAK):
    pt = H.Painter()
    FEA, SKIN = pal.FEA, pal.SKIN
    D = H.D
    H.paint_head(pt, pal)
    H.paint_hair(pt, pal)
    H.paint_wings_tail(pt, pal, WINGS)
    H.paint_body_skin(pt, pal)
    H.paint_legs(pt, pal)
    for p, spec in (("tassel", (1, 2, 4)), ("cuff_f", (1, 2, 3)), ("trim", (0, 1, 3)), ("hfeather", (0, 2, 4))):
        pt(p, "all", H.feather_fn(pal, *spec, None, False, False))
    pt("back_cover", "all", lambda f, x, y, w, h: pick(FEA, 0.12 + 0.2 * y / h + (noise(x, y, 19) - 0.5) * 0.08, x, y))

    # a white bib (chest and bust), the top edge scalloped into her black neck feathers
    def bib_fn(f, x, y, w, h):
        if f == "up":
            return pick(SKIN, 0.66, x, y)
        if f == "south":
            return pick(FEA, 0.15, x, y)
        if f == "north" and y < 2 and (x + y) % 3 == 0:
            return pick(FEA, 0.2, x, y)
        return pick(bib, 0.6 - (0.15 if f != "north" else 0) + (noise(x, y, 31) - 0.5) * 0.1, x, y)
    pt("chest", "all", bib_fn)
    pt("bust", "all", bib_fn)
    base, tip = beak

    def beak_fn(f, x, y, w, h):                          # bright along the ridge, darker at the very tip
        if f == "north":
            return pick(tip, 0.5, x, y)
        return pick(base, 0.55 + 0.25 * (f == "up") + (noise(x, y, 51) - 0.5) * 0.12 - 0.15 * (y == h - 1), x, y)
    pt("beak_u", "all", beak_fn)
    pt("beak_lo", "all", lambda f, x, y, w, h: pick(base, 0.4 + (noise(x, y, 52) - 0.5) * 0.1, x, y))
    pt("beak_tip", "all", lambda f, x, y, w, h: pick(tip, 0.45 + 0.2 * (f == "up"), x, y))

    pt("sash", "all", lambda f, x, y, w, h: pick(base, 0.5 + 0.2 * (y == 0), x, y))
    pt("pendant", "all", lambda f, x, y, w, h: pick(RING, 0.7, x, y))
    pt("cord", "all", lambda f, x, y, w, h: LEAF[1])

    def leaf_fn(broad):
        def fn(f, x, y, w, h):
            if f not in broad:
                return LEAF[0]
            r = h - 1 - y
            tl = 2.0 * D
            if r < tl:                                          # pointed leaf tips
                half = (w / 2.0) * (min(1.0, (r + 1) / tl) ** 0.8)
                if abs(x + 0.5 - w / 2.0) > half:
                    return CLEAR
            vein = abs(x + 0.5 - w / 2.0) < 0.6
            return pick(LEAF, (0.75 if vein else 0.45) + (noise(x, y, 43) - 0.5) * 0.2, x, y)
        return fn
    for p in ("leaf_s", "leaf_m", "leaf_l"):
        pt(p, "all", leaf_fn(BROAD))
    pt("leaf_side", "all", leaf_fn(("east", "west")))
    pt("bracer", "all", lambda f, x, y, w, h: pick(RING, 0.5 - 0.2 * (y in (0, h - 1)), x, y))
    return pt.T


# ----------------------------------------------------------------------------- variants
def variant_pal(hair, fea, eye, gain=0.62):
    p = copy.copy(PAL)
    p.HAIR, p.FEA, p.EYE = ramp([rgb(*c) for c in hair]), ramp([rgb(*c) for c in fea]), ramp([rgb(*c) for c in eye])
    p.BROW = p.HAIR[1]
    p.hair_gain = gain
    return p


R = lambda *c: ramp([rgb(*x) for x in c])
BLACK_HAIR = [(12, 12, 14), (24, 24, 28), (40, 40, 46), (62, 62, 70), (96, 96, 106), (140, 140, 150)]
BLACK_FEA = [(8, 8, 10), (18, 18, 22), (32, 32, 38), (52, 52, 60), (84, 84, 94), (128, 128, 140), (180, 180, 190)]
BLUE_EYES = [(16, 60, 110), (30, 100, 170), (60, 150, 220), (120, 200, 245), (190, 235, 255)]
VARIANTS = {
    # keel-billed: the "rainbow" beak (green, orange, red tip), a lemon bib
    "keel_billed": (variant_pal(BLACK_HAIR, BLACK_FEA, [(40, 90, 20), (70, 140, 30), (120, 190, 50), (180, 230, 100), (220, 250, 170)]),
                    R((200, 170, 20), (236, 206, 40), (250, 230, 90), (255, 246, 160)),
                    (R((60, 130, 40), (100, 170, 50), (236, 140, 30), (250, 190, 70)), R((150, 20, 20), (200, 40, 30), (236, 80, 60)))),
    # channel-billed: a black beak with a yellow base, a yellow-and-white bib
    "channel_billed": (variant_pal(BLACK_HAIR, BLACK_FEA, BLUE_EYES), R((220, 190, 60), (240, 214, 100), (250, 236, 170), (255, 252, 230)),
                       (R((210, 170, 20), (240, 200, 40), (250, 226, 90), (255, 240, 150)), R((10, 10, 12), (26, 26, 30), (46, 46, 52)))),
    # aracari: greenish-black, a yellow front, a pale beak with a black tip
    "aracari": (variant_pal([(16, 24, 16), (30, 44, 28), (48, 68, 44), (70, 96, 64), (100, 130, 92), (146, 176, 136)],
                            [(12, 20, 12), (24, 38, 22), (40, 60, 36), (60, 86, 54), (90, 120, 80), (130, 164, 116), (180, 206, 166)],
                            [(140, 100, 10), (200, 150, 20), (240, 196, 50), (255, 226, 110), (255, 244, 180)]),
                R((220, 170, 20), (240, 200, 40), (250, 224, 90), (255, 240, 160)),
                (R((214, 200, 170), (234, 222, 196), (246, 238, 220), (255, 252, 240)), R((10, 10, 12), (30, 30, 34), (54, 54, 60)))),
    # emerald toucanet: green all over, a yellow-and-black beak
    "emerald": (variant_pal([(16, 60, 24), (28, 92, 36), (44, 126, 52), (70, 160, 76), (110, 196, 110), (164, 226, 160)],
                            [(12, 50, 20), (22, 80, 30), (36, 112, 44), (58, 146, 64), (92, 180, 94), (140, 214, 140), (196, 240, 196)],
                            [(70, 40, 10), (120, 76, 20), (176, 124, 40), (220, 176, 80), (248, 220, 140)]),
                R((120, 190, 120), (150, 214, 150), (190, 236, 190), (226, 250, 226)),
                (R((210, 170, 20), (240, 200, 40), (250, 226, 90), (255, 240, 150)), R((10, 10, 12), (30, 30, 34), (54, 54, 60)))),
}


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide", "textures", "entity")
    texes = {"toco": paint()}
    for v, (pal, bib, beak) in VARIANTS.items():
        texes[v] = paint(pal, bib, beak)
        H.write_png(os.path.join(tex_dir, f"{NAME}_{v}.png"), texes[v])
    print(f"{NAME}: {len(VARIANTS)} variants ({', '.join(VARIANTS)})")
    if "--lineup" in sys.argv:
        lineup(sys.argv[sys.argv.index("--lineup") + 1], texes)


def lineup(path, texes):
    model = build_model().doc()
    an = H.Anims(NAME, [b["name"] for b in model["minecraft:geometry"][0]["bones"]])
    animations(an)
    A = an.doc()["animations"]
    g = lambda n: A[f"animation.{NAME}.{n}"]
    bg, W = (44, 48, 44, 255), 330
    rows = [(g("idle"), 0, -20, 6, 10, None), (g("fly"), 0.4, -30, 10, 10, None), (g("idle"), 0, -40, 0, 26, (0, 23.5 + L))]
    sheet = [[bg] * (W * len(texes)) for _ in range(W * len(rows))]
    for c, tex in enumerate(texes.values()):
        for r, (anim, tm, yaw, pitch, scale, center) in enumerate(rows):
            im = H.render(model, tex, anim, tm, yaw, pitch, W, W, scale, bg, center=center)
            for y in range(W):
                sheet[r * W + y][c * W:(c + 1) * W] = im[y]
    H.write_png(path, sheet)
    print("lineup ->", path)


# ----------------------------------------------------------------------------- geometry
def C(p, origin, inflate=None, pivot=None, rotation=None):
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
        C("pendant", [-0.5, 12.8, -2.3]),
    ]
    bust = C("bust", [-2.25, 16.9, -2.375])
    body += [bust, mirror_cube(bust)]
    bone("body", "root", [0, 12 + L, 0], body)

    sk = []
    front = [("leaf_m", -3.0, 8), ("leaf_l", -1.5, -4), ("leaf_m", -0.2, 3), ("leaf_l", 1.0, -3), ("leaf_m", 2.0, 8)]
    for i, (p, x, rz) in enumerate(front):
        sk.append(HG(p, x, 13.7, -2.7 - 0.15 * (i % 2), rz, -9))
    for i, (p, x, rz) in enumerate((("leaf_m", -3.0, 3), ("leaf_l", -1.0, -2), ("leaf_l", 0.8, 3), ("leaf_m", 2.0, -3))):
        sk.append(HG(p, x, 13.7, 2.2 + 0.15 * (i % 2), rz, 9))
    for z in (-2.2, 0.0):
        sk.append(C("leaf_side", [-3.4, 8.2, z], pivot=[-3.15, 13.7, z + 1.25], rotation=[0, 0, -9]))
        sk.append(mirror_cube(sk[-1]))
    bone("skirt", "body", [0, 13.7 + L, 0], sk)

    head = [C("head", [-4, 20, -4]), C("hair_cap", [-4, 20, -4], inflate=0.75)]
    ear_r = C("ear", [-5.0, 21.7, -1.2])
    head += [ear_r, mirror_cube(ear_r)]
    for i, (p, x, rz) in enumerate((("bang_e", -5.0, -8), ("bang_a", -3.1, 6), ("bang_a", -1.4, -4), ("bang_c", 0.2, 6),
                                    ("bang_a", 1.7, -5), ("bang_b", 3.2, 9))):
        head.append(HG(p, x, 28.4, -5.35 + 0.1 * (i % 2), rz))
    head.append(HG("frame", -4.9, 28, -5.0, -6))
    head.append(mirror_cube(head[-1]))
    for x, z in ((-2.0, -0.6), (0.6, -1.8), (2.4, 0.2)):
        head.append(C("tuft", [x - 1, 28.2, z - 0.75], pivot=[x, 28.2, z], rotation=[-18, 0, -x * 8]))
    head.append(HG("hfeather", 4.6, 28.0, -2.8, -24))
    bone("head", "body", [0, 20 + L, 0], head)

    bone("eyelids", "head", [0, 23.5 + L, -3.8], [C("eyelids", [-3.25, 21.75, -3.9])])
    bone("mouth", "head", [0, 21 + L, -3.8], [C("mouth", [-0.875, 20.25, -3.9])])
    # her beak, worn as a headpiece over her brow; the lower mandible hinges so it can clack
    bone("beak", "head", [0, 27.6 + L, -4.2], [C("beak_u", [-1.75, 26.4, -11.5], pivot=[0, 27.6, -4.2], rotation=[-8, 0, 0]),
                                               C("beak_tip", [-1.5, 25.9, -12.8], pivot=[0, 27.6, -4.2], rotation=[-8, 0, 0])])
    bone("beak_jaw", "beak", [0, 26.6 + L, -4.4], [C("beak_lo", [-1.5, 25.3, -11.2], pivot=[0, 26.6, -4.4], rotation=[-4, 0, 0])])

    side = [("strand_s1", -6.1, 26.0, -3.4, -8, 0), ("strand_s2", -6.3, 25.0, -2.0, -11, 0),
            ("strand_s1", -6.2, 26.2, 0.8, -9, 3), ("strand_s2", -6.0, 26.2, 2.4, -12, 4)]
    bone("hair_side_r", "head", [-4.7, 27 + L, 0], [HG(*s) for s in side], mirror=True)
    back = []
    names = ["strand_s1", "strand_m1", "strand_s2", "strand_m2", "strand_s1"]
    for i in range(11):
        x = -4.8 + 0.96 * i
        back.append(HG(names[i % 5], x, 26.8, 4.9 + 0.3 * (i % 3), x * 2.0, 8 + 2 * (i % 3)))
    bone("hair_back", "head", [0, 27 + L, 4.5], back)
    bone("ahoge", "head", [0, 28.5 + L, -1], [
        C("ahoge", [-0.5, 28.5, -1.5]),
        C("ahoge", [-0.35, 29.8, -1.5], pivot=[0, 29.8, -1], rotation=[0, 0, -20]),
        C("ahoge_tip", [0.1, 31.0, -1.5], pivot=[0.5, 31.1, -1], rotation=[0, 0, -50]),
    ])

    bone("arm_r", "body", [-3.25, 19.5 + L, 0], [C("arm_up", [-4.0, 16, -0.75])], mirror=True)
    bone("forearm_r", "arm_r", [-3.25, 16 + L, 0], [
        C("forearm", [-4.0, 13, -0.75]), C("bracer", [-4.5, 13.6, -1.25]),
        HG("cuff_f", -4.15, 13.8, -1.45, 4, -6), HG("cuff_f", -3.25, 13.8, -1.45, -4, -6),
    ], mirror=True)
    bone("hand_r", "forearm_r", [-3.25, 13 + L, 0], [
        C("palm", [-4.0, 11.5, -0.5]), C("fingers", [-4.0, 10.5, -0.5]), C("thumb", [-2.5, 11.5, -0.25]),
    ], mirror=True)

    # short, rounded wings (a canopy flier), a long square tail
    w = [C("wing_base", [-7, 10, 2.3])]
    rows = [(19.5, (-7.0, -5.0), ("cov_a", "cov_a")), (16.8, (-8.0, -6.0, -4.5), ("cov_a", "cov_b", "cov_a")),
            (14.1, (-8.5, -6.5, -4.5), ("cov_b", "cov_b", "cov_b")), (11.4, (-7.5, -5.5, -4.5), ("cov_c", "cov_c", "cov_c"))]
    for r, (ytop, xs, ps) in enumerate(rows):
        for x, p in zip(xs, ps):
            w.append(C(p, [x, ytop - 3, 3.3 + 0.2 * r]))
    for i, (x, ang) in enumerate(zip((-8.5, -7.25, -6, -4.75, -3.5), (-8, -6, -4, -2, 0))):
        w.append(HG("sec_a" if i % 2 == 0 else "sec_b", x, 12.5, 2.3 + 0.25 * (i % 2), ang))
    bone("wing_r", "body", [-2.5, 19 + L, 2.3], w, mirror=True)
    pr = []
    for i, (x, ang) in enumerate(zip((-9.0, -7.75, -6.5, -5.25), (-14, -10, -6, -2))):
        pr.append(HG("prim_a" if i < 2 else "prim_b", x, 12.5, 2.8 + 0.25 * (i % 2), ang))
    bone("wing_tip_r", "wing_r", [-7, 12.5 + L, 3.0], pr, mirror=True)

    tail = [C("tail_fluff", [-1.75, 12.2, 2.2])]
    for i, (x, ang) in enumerate(zip((-2.6, -1.4, -0.2, 1.0), (-4, -1.5, 1.5, 4))):
        tail.append(HG("tail_f" if i % 2 == 0 else "tail_g", x, 12.7, 3.4 + 0.25 * (i % 2), ang, 4))
    bone("tail", "body", [0, 13.2 + L, 3], tail)

    # legs: blue-grey, zygodactyl (two toes forward, two back) for gripping branches
    cx = -1.25
    bone("leg_r", "root", [cx, 12 + L, 0], [C("thigh", [cx - 1, 9.5, -1]), C("fluff", [cx - 1.25, 8.5, -1.25])], mirror=True)
    bone("shin_r", "leg_r", [cx, 9 + L, 0], [cube("shin", [cx - .75, 2, -.75]), cube("ankle", [cx - 1, 1, -1])], mirror=True)
    foot = [cube("heel", [cx - 1, 0, -1])]
    for dx, spread in ((-1.0, 10), (0.0, -10)):
        x = cx + dx
        foot += [cube("toe1", [x, 0, -3.5], pivot=[cx, .5, -1], rotation=[0, spread, 0]),
                 cube("talon", [x, 0, -5.0], pivot=[cx, .5, -1], rotation=[0, spread, 0])]
    for dx in (-1.0, 0.0):
        foot += [cube("htoe", [cx + dx, 0, 0.75]), cube("htalon", [cx + dx, 0, 2.0])]
    bone("foot_r", "shin_r", [cx, 1, 0], foot, mirror=True)
    return m


# ----------------------------------------------------------------------------- animations
def animations(an):
    for f in (H.std_idle, H.std_walk, H.std_fly, H.std_sing, H.std_bathe):
        f(an)
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold

    # clack: the beak snaps open and shut, her head bobbing with each clack
    snaps = [(0, (0, 0, 0)), (0.1, (22, 0, 0)), (0.2, (0, 0, 0)), (0.35, (22, 0, 0)), (0.45, (0, 0, 0)), (0.8, (0, 0, 0))]
    an.add("clack", 0.8, True, merge({
        "beak_jaw": {"rotation": T(*snaps)},
        "head": {"rotation": T((0, (0, 0, 0)), (0.1, (-10, 0, 4)), (0.2, (0, 0, 0)), (0.35, (-10, 0, -4)), (0.45, (0, 0, 0)), (0.8, (0, 0, 0)))},
        "tail": {"rotation": T((0, (0, 0, 0)), (0.2, (-20, 0, 0)), (0.45, (0, 0, 0)), (0.8, (0, 0, 0)))},
    }, sym("wing", T((0, (0, 0, 0)), (0.2, (0, 0, -10)), (0.45, (0, 0, 0)), (0.8, (0, 0, 0))))))


if __name__ == "__main__":
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="sing", head_center=(0, 23.5 + L), body_center=(0, 15.5 + L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("clack", 0.1, 0, 0), ("idle", 0, -40, 8)])
    write_variants()
