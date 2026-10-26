#!/usr/bin/env python3
"""Tengu Harpy - species definition. The shared engine lives in harpy_lib.py; the geometry is the seagull's
(gen_seagull_harpy.py) at crow height.

    python tools/gen_tengu_harpy.py                 # write the three asset files
    python tools/gen_tengu_harpy.py --preview x.png # also render preview sheets
    python tools/gen_tengu_harpy.py --lineup x.png  # also render all variants side by side

Variants (VARIANTS below) only get their own texture, tengu_harpy_<variant>.png; the default (karasu) stays
tengu_harpy.png. Keep in sync with TenguHarpy.Variant.

Field guide: the karasu-tengu, the crow tengu of Japanese mountains. Proud, neutral, a guardian of her peaks: she
soars far above everything and only comes down to drink. Her feather fan throws a gust that hurls enemies away.
Design: crow-black hair and wings with a violet sheen; a yamabushi's cream robe (suzukake) with persimmon pom-poms
(bonbon) on the sash, a small black cap (tokin) on her brow, charcoal hakama strips, and a crow-feather fan
(hauchiwa) with a red handle in her right hand.
"""
import copy
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

NAME = "tengu_harpy"
L = 1.0                                          # crow height

# ----------------------------------------------------------------------------- palette
PAL = H.Pal()
PAL.HAIR = ramp([rgb(12, 10, 20), rgb(26, 24, 40), rgb(44, 42, 68), rgb(70, 70, 106), rgb(108, 110, 150), rgb(156, 158, 196)])
PAL.SKIN = ramp([rgb(196, 150, 132), rgb(222, 182, 162), rgb(240, 210, 192), rgb(250, 230, 214)], nodither=True)
PAL.FEA = ramp([rgb(10, 9, 16), rgb(22, 21, 34), rgb(38, 38, 58), rgb(62, 64, 96), rgb(98, 102, 142), rgb(146, 150, 188), rgb(196, 200, 228)])
PAL.EYE = ramp([rgb(110, 20, 20), rgb(160, 34, 30), rgb(204, 64, 44), rgb(236, 110, 70), rgb(252, 170, 120)])
PAL.LEG = ramp([rgb(12, 12, 18), rgb(22, 22, 34), rgb(32, 34, 50), rgb(48, 52, 76), rgb(72, 78, 112)])
PAL.hair_gain, PAL.hair_off = 0.62, -0.02
PAL.TALON = [rgb(12, 10, 16), rgb(44, 42, 60)]
PAL.EYE_OUT, PAL.PUPIL = rgb(40, 8, 8), rgb(14, 4, 4)
PAL.BROW = PAL.HAIR[1]
PAL.eye_a, PAL.eye_b, PAL.eye_slant = 4.0, 5.0, 1.0           # proud, level eyes
PAL.pupil = (1.6, 2.8)
PAL.wing_base_t, PAL.tail_fluff_t, PAL.fluff_t = (0.05, 0.18), 0.30, 0.22

ROBE = ramp([rgb(196, 186, 160), rgb(222, 214, 190), rgb(240, 234, 214), rgb(252, 248, 236)])
HAKAMA = ramp([rgb(22, 22, 28), rgb(38, 38, 48), rgb(58, 58, 72), rgb(84, 84, 100)])
BONBON = ramp([rgb(150, 54, 20), rgb(206, 92, 34), rgb(240, 136, 60), rgb(255, 184, 110)])
CAP = ramp([rgb(8, 8, 12), rgb(22, 22, 30), rgb(42, 42, 56)])
LACQUER = ramp([rgb(110, 16, 16), rgb(170, 30, 26), rgb(214, 58, 44)])

WINGS = {
    "sec_a": (1, 2, 3, 0), "sec_b": (0, 2, 3, 1), "prim_a": (0, 1, 2, 0), "prim_b": (1, 2, 3, 1),
    "tail_f": (1, 2, 3, 0), "tail_g": (0, 1, 3, 1),
    "cov_a": (0, 0, 1, None, True), "cov_b": (0, 1, 2, None, True), "cov_c": (1, 2, 3, None, True),
}


# ----------------------------------------------------------------------------- parts
def declare_parts():
    H.standard_parts()
    for args in [("tokin", 2, 1.5, 2), ("bonbon", 1, 1, 1), ("fan", 5, 5.5, .25), ("fan_grip", .5, 2.5, .5)]:
        H.part(*args)


# ----------------------------------------------------------------------------- texture
def paint(pal=PAL, robe=ROBE, bonbon=BONBON, hakama=HAKAMA):
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

    # the suzukake: a cream robe crossed left over right, with a sash line
    def suzukake(f, x, y, w, h):
        if f == "up":
            return pick(SKIN, 0.66, x, y)
        if f == "down":
            return pick(robe, 0.3, x, y)
        if f == "north" and abs((x - w / 2) - (y - h / 2) * 0.6) < 0.8:
            return pick(robe, 0.15, x, y)                              # the crossed collar
        return pick(robe, 0.6 - (0.15 if f != "north" else 0) + (noise(x, y, 31) - 0.5) * 0.12, x, y)
    pt("chest", "all", suzukake)
    pt("bust", "all", suzukake)
    pt("bonbon", "all", lambda f, x, y, w, h: pick(bonbon, 0.55 + (noise(x, y, 7) - 0.5) * 0.5, x, y))
    pt("tokin", "all", lambda f, x, y, w, h: pick(CAP, 0.5 + 0.3 * (y == 0) - 0.2 * (x % 3 == 0), x, y))
    pt("sash", "all", lambda f, x, y, w, h: pick(bonbon, 0.4 + 0.2 * (y == 0), x, y))
    pt("pendant", "all", lambda f, x, y, w, h: pick(bonbon, 0.7, x, y))
    pt("cord", "all", lambda f, x, y, w, h: robe[1])

    # the hauchiwa: crow feathers fanned from a red lacquered grip (it hangs down from her hand, so the round edge is at the bottom)
    def fan(f, x, y, w, h):
        cx = w / 2.0
        r = (y + 0.5) / h
        if f in BROAD and abs(x + 0.5 - cx) > cx * (0.45 + 0.55 * min(1.0, r * 1.6)):
            return CLEAR
        if f in BROAD and r > 0.85 and abs(x + 0.5 - cx) > cx * 0.8:
            return CLEAR
        shaft = abs(x + 0.5 - cx) < 0.6 or (x % 4 == 0 and r > 0.2)
        return pick(FEA, (0.35 if shaft else 0.12) + 0.2 * r + (noise(x, y, 41) - 0.5) * 0.1, x, y)
    pt("fan", "all", fan)
    pt("fan_grip", "all", lambda f, x, y, w, h: pick(LACQUER, 0.5 + 0.3 * (x == 0), x, y))

    def hakama_fn(broad):
        def fn(f, x, y, w, h):
            if f not in broad:
                return hakama[1]
            r = h - 1 - y
            tl = 1.5 * D
            if r < tl:
                half = (w / 2.0) * (min(1.0, (r + 1) / tl) ** 0.6)
                if abs(x + 0.5 - w / 2.0) > half:
                    return CLEAR
            if y < D:
                return robe[2]                                          # the white waistband
            return pick(hakama, 0.45 + (noise(x, y, 43) - 0.5) * 0.2 - (0.18 if x % 4 == 0 else 0), x, y)   # pleats
        return fn
    for p in ("leaf_s", "leaf_m", "leaf_l"):
        pt(p, "all", hakama_fn(BROAD))
    pt("leaf_side", "all", hakama_fn(("east", "west")))
    pt("bracer", "all", lambda f, x, y, w, h: pick(robe, 0.55 - 0.25 * (y in (0, h - 1)), x, y))
    return pt.T


# ----------------------------------------------------------------------------- variants
def variant_pal(hair, fea, eye, gain=0.62):
    p = copy.copy(PAL)
    p.HAIR, p.FEA, p.EYE = ramp([rgb(*c) for c in hair]), ramp([rgb(*c) for c in fea]), ramp([rgb(*c) for c in eye])
    p.BROW = p.HAIR[1]
    p.hair_gain = gain
    return p


R = lambda *c: ramp([rgb(*x) for x in c])
VARIANTS = {
    # konoha-tengu: the kite-feathered tengu of the forest; brown feathers, a moss-green robe
    "konoha": (variant_pal([(40, 24, 14), (66, 42, 26), (96, 64, 40), (130, 92, 60), (166, 126, 88), (204, 170, 128)],
                           [(36, 22, 12), (62, 40, 24), (92, 62, 38), (124, 88, 56), (160, 122, 84), (196, 162, 122), (228, 204, 170)],
                           [(70, 40, 10), (120, 76, 20), (176, 124, 40), (220, 176, 80), (248, 220, 140)], gain=0.8),
               R((70, 96, 60), (96, 126, 80), (126, 156, 104), (164, 190, 140)), BONBON, HAKAMA),
    # a white tengu: snowy feathers and hair, a black robe with white pom-poms
    "shiro": (variant_pal([(150, 154, 166), (186, 190, 200), (214, 218, 226), (232, 234, 240), (244, 246, 250), (255, 255, 255)],
                          [(130, 134, 146), (168, 172, 184), (200, 204, 214), (224, 226, 234), (240, 242, 248), (250, 250, 252), (255, 255, 255)],
                          [(30, 50, 110), (50, 80, 160), (80, 120, 210), (130, 170, 240), (190, 220, 255)], gain=0.9),
              R((22, 22, 28), (38, 38, 48), (58, 58, 72), (84, 84, 100)), R((200, 200, 206), (226, 226, 232), (244, 244, 248), (255, 255, 255)),
              R((120, 20, 20), (170, 34, 30), (206, 60, 50), (230, 100, 80))),
    # aka: crimson-sheened feathers, a red-trimmed robe
    "aka": (variant_pal([(28, 6, 8), (54, 12, 14), (88, 22, 22), (128, 36, 32), (176, 60, 50), (220, 110, 90)],
                        [(22, 6, 8), (44, 10, 12), (74, 18, 20), (110, 30, 30), (150, 50, 46), (196, 90, 80), (232, 150, 140)],
                        [(140, 100, 10), (200, 150, 20), (240, 196, 50), (255, 226, 110), (255, 244, 180)]),
            ROBE, R((110, 14, 20), (160, 24, 30), (206, 46, 46), (236, 90, 80)), HAKAMA),
    # kurama: the tengu of Mount Kurama; teal-sheened black, an indigo robe
    "kurama": (variant_pal([(8, 16, 20), (16, 32, 40), (26, 54, 64), (40, 82, 94), (64, 120, 130), (110, 170, 176)],
                           [(6, 14, 18), (14, 28, 36), (24, 48, 58), (38, 74, 86), (60, 108, 120), (98, 156, 166), (160, 210, 214)],
                           [(10, 70, 66), (24, 124, 112), (56, 184, 164), (130, 232, 212), (200, 250, 240)]),
               R((30, 36, 70), (46, 54, 100), (66, 78, 136), (96, 110, 170)), R((200, 200, 210), (226, 226, 234), (244, 244, 250), (255, 255, 255)),
               HAKAMA),
}


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "monstergirls", "textures", "entity")
    texes = {"karasu": paint()}
    for v, (pal, robe, bonbon, hakama) in VARIANTS.items():
        texes[v] = paint(pal, robe, bonbon, hakama)
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
    rows = [(g("idle"), 0, -20, 6, 10, None), (g("fly"), 0.4, -30, 10, 10, None), (g("idle"), 0, -15, 0, 30, (0, 22.5 + L))]
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
    for x, y in ((-2.3, 18.6), (-0.5, 18.9), (1.3, 18.6)):           # the bonbon pom-poms along the kesa
        body.append(C("bonbon", [x, y, -2.45]))
    bone("body", "root", [0, 12 + L, 0], body)

    sk = []
    front = [("leaf_m", -3.0, 5), ("leaf_l", -1.5, -3), ("leaf_l", -0.2, 2), ("leaf_l", 1.0, -2), ("leaf_m", 2.0, 6)]
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
    for i, (p, x, rz) in enumerate((("bang_e", -5.0, -8), ("bang_a", -3.1, 6), ("bang_b", -1.4, -4), ("bang_c", 0.2, 6),
                                    ("bang_a", 1.7, -5), ("bang_e", 3.2, 9))):
        head.append(HG(p, x, 28.4, -5.35 + 0.1 * (i % 2), rz))
    head.append(HG("frame", -4.9, 28, -5.0, -6))
    head.append(mirror_cube(head[-1]))
    for x, z in ((-2.0, -0.6), (0.6, -1.8), (2.4, 0.2)):
        head.append(C("tuft", [x - 1, 28.2, z - 0.75], pivot=[x, 28.2, z], rotation=[-18, 0, -x * 8]))
    head.append(C("tokin", [-1.0, 28.4, -4.3], pivot=[0, 28.4, -3.3], rotation=[-15, 0, 0]))   # the little cap on her brow
    head.append(HG("hfeather", 4.6, 28.0, -2.8, -24))
    bone("head", "body", [0, 20 + L, 0], head)

    bone("eyelids", "head", [0, 23.5 + L, -3.8], [C("eyelids", [-3.25, 21.75, -3.9])])
    bone("mouth", "head", [0, 21 + L, -3.8], [C("mouth", [-0.875, 20.25, -3.9])])

    side = [("strand_m1", -6.1, 26.0, -3.4, -6, 0), ("strand_m2", -6.3, 25.5, -2.0, -9, 0),
            ("strand_s1", -6.2, 26.2, 0.8, -9, 3), ("strand_s2", -6.0, 26.2, 2.4, -12, 4)]
    bone("hair_side_r", "head", [-4.7, 27 + L, 0], [HG(*s) for s in side], mirror=True)
    back = []
    names = ["strand_l1", "strand_m1", "strand_l2", "strand_m2", "strand_l1"]
    for i in range(11):
        x = -4.8 + 0.96 * i
        back.append(HG(names[i % 5], x, 26.8, 4.9 + 0.3 * (i % 3), x * 1.5, 6 + 2 * (i % 3)))
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
    bone("fan", "hand_r", [-3.25, 11.5 + L, 0], [                     # in her right hand only (not mirrored)
        C("fan_grip", [-3.5, 9.0, -0.25]), C("fan", [-5.75, 3.5, -0.125]),
    ])

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
    for i, (x, ang) in enumerate(zip((-10.5, -9, -7.5, -6, -4.5), (-22, -16, -11, -6, -2))):
        pr.append(HG("prim_a" if i < 2 else "prim_b", x, 12.5, 2.8 + 0.25 * (i % 2), ang))
    bone("wing_tip_r", "wing_r", [-8, 12.5 + L, 3.0], pr, mirror=True)

    tail = [C("tail_fluff", [-1.75, 12.2, 2.2])]
    for i, (x, ang) in enumerate(zip((-3.3, -2.0, -0.875, 0.25, 1.5), (-10, -5, 0, 5, 10))):
        tail.append(HG("tail_f" if i % 2 == 0 else "tail_g", x, 12.7, 3.4 + 0.25 * (i % 2), ang, 6))
    bone("tail", "body", [0, 13.2 + L, 3], tail)

    # legs: black, a crow's four toes (three forward, one back) with talons
    cx = -1.25
    bone("leg_r", "root", [cx, 12 + L, 0], [C("thigh", [cx - 1, 9.5, -1]), C("fluff", [cx - 1.25, 8.5, -1.25])], mirror=True)
    bone("shin_r", "leg_r", [cx, 9 + L, 0], [cube("shin", [cx - .75, 2, -.75]), cube("ankle", [cx - 1, 1, -1])], mirror=True)
    foot = [cube("heel", [cx - 1, 0, -1]), cube("htoe", [cx - .5, 0, 0.75]), cube("htalon", [cx - .5, 0, 2.0])]
    for dx, spread in ((-1.5, 14), (-0.5, 0), (0.5, -14)):
        x = cx + dx
        foot += [cube("toe1", [x, 0, -3.5], pivot=[cx, .5, -1], rotation=[0, spread, 0]),
                 cube("talon", [x, 0, -5.0], pivot=[cx, .5, -1], rotation=[0, spread, 0])]
    bone("foot_r", "shin_r", [cx, 1, 0], foot, mirror=True)
    return m


# ----------------------------------------------------------------------------- animations
def animations(an):
    for f in (H.std_idle, H.std_walk, H.std_fly, H.std_sing, H.std_bathe):
        f(an)
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold

    # strike: the fan swept up and across in one gust
    an.add("strike", 0.5, True, merge({
        "arm_r": {"rotation": T((0, (-20, 0, 10)), (0.15, (-150, -30, 30)), (0.3, (-60, 40, -20)), (0.5, (-20, 0, 10)))},
        "forearm_r": {"rotation": T((0, (-10, 0, 0)), (0.15, (-40, 0, 0)), (0.5, (-10, 0, 0)))},
        "body": {"rotation": T((0, (0, 0, 0)), (0.15, (0, -20, 0)), (0.3, (0, 25, 0)), (0.5, (0, 0, 0)))},
        "head": {"rotation": hold(-5, 0, 0)},
    }, sym("wing", T((0, (0, 0, 0)), (0.2, (0, 0, -30)), (0.5, (0, 0, 0))))))

    # drink: kneeling at the water's edge, bent low, cupped hands to her mouth
    an.add("drink", 3.0, True, merge({
        "root": {"position": hold(0, -4.5, 0)},
        "body": {"rotation": hold(35, 0, 0)},
        "head": {"rotation": T((0, (10, 0, 0)), (1.5, (25, 0, 0)), (3, (10, 0, 0)))},
        "leg_r": {"rotation": hold(-80, 0, 0)}, "leg_l": {"rotation": hold(-80, 0, 0)},
        "shin_r": {"rotation": hold(110, 0, 0)}, "shin_l": {"rotation": hold(110, 0, 0)},
    }, sym("arm", T((0, (-70, 0, 15)), (1.5, (-100, 0, 20)), (3, (-70, 0, 15)))), sym("forearm", hold(-50, 0, 0)),
        sym("wing", hold(0, 0, -6))))


if __name__ == "__main__":
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="sing", head_center=(0, 22.5 + L), body_center=(0, 15.5 + L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("strike", 0.15, 0, 0), ("idle", 0, -30, 8)])
    write_variants()
