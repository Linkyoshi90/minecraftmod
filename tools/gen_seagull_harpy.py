#!/usr/bin/env python3
"""Seagull Harpy - species definition. The shared engine lives in harpy_lib.py.

    python tools/gen_seagull_harpy.py                 # write the three asset files
    python tools/gen_seagull_harpy.py --preview x.png # also render preview sheets (x.png, x_face.png, x_body.png)
    python tools/gen_seagull_harpy.py --lineup x.png  # also render all species variants side by side

Species variants (VARIANTS below) share the geometry and animations and only get their own texture,
seagull_harpy_<variant>.png; the default (yellow-legged gull) stays seagull_harpy.png.

Field guide: "Small coast- and sea-borne harpy. Gathers around rock formations on the open sea singing happy,
alluring, siren-like songs that result in shipwrecks. Follows any boat, asks passersby on land for fish, and
sometimes swoops down to steal from players." Bold, noisy, opportunistic.
Design: sparrow-sized; white hair streaked silver, pale grey wings with black tips and white "mirror" spots,
bright yellow eyes and legs, webbed feet; a sailor outfit: navy-and-white striped top, red neckerchief, rope belt,
navy strips with a white stripe.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

NAME = "seagull_harpy"
L = 0.0                                          # sparrow-sized

# ----------------------------------------------------------------------------- palette
PAL = H.Pal()
PAL.HAIR = ramp([rgb(118, 126, 138), rgb(156, 164, 176), rgb(194, 200, 210), rgb(220, 224, 232), rgb(240, 242, 246), rgb(252, 252, 254)])
PAL.FEA = ramp([rgb(22, 22, 26), rgb(62, 64, 70), rgb(112, 118, 128), rgb(150, 158, 170), rgb(190, 196, 206), rgb(228, 232, 238), rgb(252, 252, 254)])
PAL.EYE = ramp([rgb(150, 120, 16), rgb(206, 170, 24), rgb(240, 210, 50), rgb(252, 232, 100), rgb(255, 246, 170)])
PAL.LEG = ramp([rgb(150, 110, 20), rgb(206, 156, 30), rgb(238, 192, 50), rgb(250, 218, 96), rgb(255, 238, 160)])
PAL.TALON = [rgb(60, 50, 40), rgb(110, 96, 80)]
PAL.EYE_OUT, PAL.PUPIL = rgb(60, 50, 30), rgb(20, 16, 10)
PAL.BROW = PAL.HAIR[1]
PAL.pupil = (1.6, 2.8)                                       # small pupils in a bright yellow iris: a bold stare
PAL.wing_base_t, PAL.tail_fluff_t, PAL.fluff_t = (0.62, 0.20), 0.92, 0.92

NAVY = ramp([rgb(16, 22, 44), rgb(28, 38, 72), rgb(42, 56, 104), rgb(66, 82, 140), rgb(98, 114, 176)])
WHITE = ramp([rgb(196, 200, 210), rgb(226, 230, 236), rgb(250, 250, 252)])
RED = ramp([rgb(110, 20, 24), rgb(170, 34, 36), rgb(214, 64, 56)])
BRASS = ramp([rgb(140, 104, 40), rgb(200, 160, 70), rgb(240, 210, 120)])
ROPE = ramp([rgb(110, 86, 52), rgb(156, 126, 82), rgb(196, 168, 120)])

# grey wings with black primaries and a white "mirror" spot near each tip; white tail
WINGS = {
    "sec_a": (4, 3, 5, 6), "sec_b": (4, 3, 5, 6), "prim_a": (3, 1, 0, 6), "prim_b": (3, 2, 0, 6),
    "tail_f": (6, 5, 6, None, True), "tail_g": (5, 5, 6, None, True),
    "cov_a": (4, 4, 5, None, True), "cov_b": (3, 4, 4, None, True), "cov_c": (3, 3, 4, None, True),
}


# ----------------------------------------------------------------------------- parts
def declare_parts():
    H.standard_parts()
    for args in [("kerchief", 3, 2, .5), ("knot", 1, 1, .5)]:
        H.part(*args)


# ----------------------------------------------------------------------------- texture
def paint(pal=PAL, wings=WINGS, RED=RED):
    pt = H.Painter()
    FEA, SKIN = pal.FEA, pal.SKIN
    D = H.D
    H.paint_head(pt, pal)
    H.paint_hair(pt, pal)
    H.paint_wings_tail(pt, pal, wings)
    H.paint_body_skin(pt, pal)
    H.paint_legs(pt, pal)
    for p, spec in (("tassel", (5, 4, 3)), ("cuff_f", (5, 4, 3)), ("trim", (5, 5, 6)), ("hfeather", (6, 5, 1))):
        pt(p, "all", H.feather_fn(pal, *spec, None, False, False))
    pt("back_cover", "all", lambda f, x, y, w, h: pick(FEA, 0.72 + 0.2 * y / h + (noise(x, y, 19) - 0.5) * 0.08, x, y))

    # sailor top: navy-and-white horizontal stripes, white piping
    def sailor(f, x, y, w, h):
        if f == "up":
            return pick(SKIN, 0.66, x, y)
        if f == "down":
            return pick(SKIN, 0.5, x, y)
        if y < D // 2 or y >= h - D // 2:
            return WHITE[2]
        shade = 0.0 if f == "north" else -0.15
        if (y // 2) % 2 == 0:
            return pick(WHITE, 0.6 + shade, x, y)
        return pick(NAVY, 0.5 + shade + (noise(x, y, 31) - 0.5) * 0.1, x, y)
    pt("chest", "all", sailor)
    pt("bust", "all", sailor)

    # red neckerchief: a triangle hanging from a knot
    def kerchief(f, x, y, w, h):
        if f in BROAD and abs(x + 0.5 - w / 2.0) > (h - y) * w / (2.0 * h) + 0.5:
            return CLEAR
        return pick(RED, 0.5 + (noise(x, y, 33) - 0.5) * 0.2 - (0.2 if y == 0 else 0), x, y)
    pt("kerchief", "all", kerchief)
    pt("knot", "all", lambda f, x, y, w, h: pick(RED, 0.3 + 0.4 * (x == 0), x, y))

    def belt(f, x, y, w, h):
        if y < 2 or y >= h - 2:
            return ROPE[0]
        return pick(ROPE, 0.5 + 0.25 * (((x + y) % 3) == 0) - 0.15 * (((x - y) % 3) == 0), x, y)   # twisted rope
    pt("sash", "all", belt)
    pt("pendant", "all", lambda f, x, y, w, h: pick(BRASS, 0.4 + 0.5 * (x < w // 2), x, y))
    pt("cord", "all", lambda f, x, y, w, h: ROPE[1])

    def cloth(broad):
        def fn(f, x, y, w, h):
            if f not in broad:
                return NAVY[1]
            r = h - 1 - y
            tl = 1.5 * D
            if r < tl:
                half = (w / 2.0) * (min(1.0, (r + 1) / tl) ** 0.6)
                if abs(x + 0.5 - w / 2.0) > half:
                    return CLEAR
            if y < D:
                return ROPE[1] if (x + y) % 3 else ROPE[2]
            if y in (D + 2, D + 3):
                return WHITE[2]                                 # white stripe near the top of each strip
            return pick(NAVY, 0.45 + (noise(x, y, 43) - 0.5) * 0.2 - (0.14 if x % 5 == 0 else 0), x, y)
        return fn
    for p in ("leaf_s", "leaf_m", "leaf_l"):
        pt(p, "all", cloth(BROAD))
    pt("leaf_side", "all", cloth(("east", "west")))
    pt("bracer", "all", lambda f, x, y, w, h: WHITE[2] if y in (1, 2) else pick(NAVY, 0.5, x, y))
    return pt.T


# ----------------------------------------------------------------------------- species variants
def variant_pal(hair, fea, eye, leg, pupil=None, eye_out=None):
    pal = H.Pal()
    for attr in ("SKIN", "TALON", "EYE_OUT", "PUPIL", "pupil", "wing_base_t", "tail_fluff_t", "fluff_t"):
        setattr(pal, attr, getattr(PAL, attr))
    pal.HAIR, pal.FEA, pal.EYE, pal.LEG = ramp(hair), ramp(fea), ramp(eye), ramp(leg)
    pal.BROW = pal.HAIR[1]
    if pupil:
        pal.PUPIL = pupil
    if eye_out:
        pal.EYE_OUT = eye_out
    return pal


GULL_FEA = [rgb(22, 22, 26), rgb(62, 64, 70), rgb(112, 118, 128), rgb(150, 158, 170), rgb(190, 196, 206), rgb(228, 232, 238), rgb(252, 252, 254)]
WHITE_HAIR = [rgb(118, 126, 138), rgb(156, 164, 176), rgb(194, 200, 210), rgb(220, 224, 232), rgb(240, 242, 246), rgb(252, 252, 254)]
PALE_EYES = [rgb(150, 130, 70), rgb(200, 186, 120), rgb(230, 222, 170), rgb(244, 240, 210), rgb(252, 250, 236)]
DARK_EYES = [rgb(14, 10, 10), rgb(28, 20, 18), rgb(46, 32, 28), rgb(66, 48, 40), rgb(90, 68, 56)]
PINK_LEGS = [rgb(170, 110, 110), rgb(206, 146, 142), rgb(228, 176, 170), rgb(242, 204, 198), rgb(250, 228, 224)]
RED_LEGS = [rgb(110, 20, 20), rgb(160, 34, 30), rgb(200, 56, 46), rgb(226, 90, 76), rgb(240, 132, 116)]
INK_TIPS = dict(WINGS, prim_a=(3, 0, 0, None), prim_b=(3, 1, 0, None))       # black tips, no white mirrors
# dark-mantled gulls: the whole upper wing from the dark end of the ramp, a white trailing edge on the secondaries
DARK_MANTLE = dict(WINGS, sec_a=(3, 2, 6, None), sec_b=(3, 2, 6, None), cov_a=(3, 3, 2, None, True),
                   cov_b=(2, 3, 3, None, True), cov_c=(2, 2, 3, None, True))

# name -> (palette, wing pattern, neckerchief ramp). Keep in sync with SeagullHarpy.Variant.
VARIANTS = {
    # herring gull: like the default but with pink legs and pale eyes
    "herring": (variant_pal(WHITE_HAIR, GULL_FEA, PALE_EYES, PINK_LEGS), WINGS, RED),
    # black-headed gull: chocolate-brown hood, dark eyes, red legs; a yellow neckerchief
    "black_headed": (variant_pal(
        [rgb(30, 18, 14), rgb(50, 32, 24), rgb(72, 48, 36), rgb(96, 66, 50), rgb(124, 90, 70), rgb(156, 120, 96)],
        GULL_FEA, DARK_EYES, RED_LEGS), INK_TIPS,
        ramp([rgb(150, 110, 10), rgb(214, 170, 24), rgb(248, 214, 70)])),
    # laughing gull: black hood, darker slate wings, dark red legs; a teal neckerchief
    "laughing": (variant_pal(
        [rgb(10, 10, 12), rgb(22, 22, 26), rgb(36, 36, 42), rgb(54, 54, 62), rgb(76, 76, 86), rgb(104, 104, 116)],
        [rgb(16, 16, 20), rgb(40, 42, 48), rgb(70, 74, 84), rgb(98, 104, 116), rgb(170, 176, 188), rgb(228, 232, 238), rgb(252, 252, 254)],
        DARK_EYES, RED_LEGS), dict(DARK_MANTLE, prim_a=(2, 0, 0, None), prim_b=(2, 1, 0, None)),
        ramp([rgb(14, 90, 96), rgb(24, 140, 146), rgb(70, 196, 196)])),
    # kittiwake: soft grey wings with "ink-dipped" all-black tips, dark eyes, black legs
    "kittiwake": (variant_pal(WHITE_HAIR, GULL_FEA, DARK_EYES,
        [rgb(8, 8, 10), rgb(20, 20, 24), rgb(34, 34, 40), rgb(52, 52, 60), rgb(76, 76, 86)]), INK_TIPS, RED),
    # great black-backed gull: near-black slate wings with white mirrors, pink legs, pale eyes; a navy-red kerchief
    "great_black_backed": (variant_pal(WHITE_HAIR,
        [rgb(10, 10, 14), rgb(22, 24, 30), rgb(34, 36, 44), rgb(46, 50, 60), rgb(180, 184, 194), rgb(228, 232, 238), rgb(252, 252, 254)],
        PALE_EYES, PINK_LEGS), dict(DARK_MANTLE, prim_a=(2, 1, 0, 6), prim_b=(2, 0, 0, 6)),
        ramp([rgb(80, 14, 20), rgb(130, 24, 30), rgb(176, 46, 46)])),
}


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "harpies", "textures", "entity")
    texes = {"yellow_legged": paint()}
    for v, (pal, wings, kerchief) in VARIANTS.items():
        texes[v] = paint(pal, wings, kerchief)
        H.write_png(os.path.join(tex_dir, f"{NAME}_{v}.png"), texes[v])
    print(f"{NAME}: {len(VARIANTS)} species variants ({', '.join(VARIANTS)})")
    if "--lineup" in sys.argv:
        lineup(sys.argv[sys.argv.index("--lineup") + 1], texes)


def lineup(path, texes):
    """one column per variant: front idle, 3/4 flying, face close-up"""
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
    print("lineup ->", path, "(columns:", ", ".join(texes), ")")


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
        C("kerchief", [-1.5, 17.6, -2.15], pivot=[0, 19.6, -1.9], rotation=[-12, 0, 0]), C("knot", [-0.5, 19.0, -2.3]),
        C("pendant", [-0.5, 16.6, -2.55]),
    ]
    bust = C("bust", [-2.25, 16.9, -2.375])
    body += [bust, mirror_cube(bust)]
    bone("body", "root", [0, 12 + L, 0], body)

    sk = []
    front = [("leaf_m", -3.0, 5), ("leaf_l", -1.5, -3), ("leaf_s", -0.2, 2), ("leaf_l", 1.0, -2), ("leaf_m", 2.0, 6)]
    for i, (p, x, rz) in enumerate(front):
        sk.append(HG(p, x, 13.7, -2.7 - 0.15 * (i % 2), rz, -9))
    for i, (p, x, rz) in enumerate((("leaf_m", -3.0, 3), ("leaf_s", -1.0, -2), ("leaf_l", 0.8, 3), ("leaf_m", 2.0, -3))):
        sk.append(HG(p, x, 13.7, 2.2 + 0.15 * (i % 2), rz, 9))
    for z in (-2.2, 0.0):
        sk.append(C("leaf_side", [-3.4, 8.2, z], pivot=[-3.15, 13.7, z + 1.25], rotation=[0, 0, -9]))
        sk.append(mirror_cube(sk[-1]))
    bone("skirt", "body", [0, 13.7 + L, 0], sk)

    # ---- head: windswept hair, crown tufts, a jaunty ahoge
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

    # ---- wings: long, narrow and pointed
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

    # ---- tail: short and square
    tail = [C("tail_fluff", [-1.75, 12.2, 2.2])]
    for i, (x, ang) in enumerate(zip((-3.3, -2.0, -0.875, 0.25, 1.5), (-8, -4, 0, 4, 8))):
        tail.append(HG("tail_f" if i % 2 == 0 else "tail_g", x, 12.7, 3.4 + 0.25 * (i % 2), ang, 6))
    bone("tail", "body", [0, 13.2 + L, 3], tail)

    # ---- legs: slim yellow legs, webbed feet (three toes spread like a fan)
    cx = -1.25
    bone("leg_r", "root", [cx, 12 + L, 0], [C("thigh", [cx - 1, 9.5, -1]), C("fluff", [cx - 1.25, 8.5, -1.25])], mirror=True)
    bone("shin_r", "leg_r", [cx, 9 + L, 0], [cube("shin", [cx - .75, 2, -.75]), cube("ankle", [cx - 1, 1, -1])], mirror=True)
    foot = [cube("heel", [cx - 1, 0, -1])]
    for dx, spread in ((-1.5, 14), (-0.5, 0), (0.5, -14)):
        x = cx + dx
        foot += [cube("toe1", [x, 0, -3.5], pivot=[cx, .5, -1], rotation=[0, spread, 0]),
                 cube("toe2", [x, 0, -5.5], pivot=[cx, .5, -1], rotation=[0, spread, 0])]
    bone("foot_r", "shin_r", [cx, 1, 0], foot, mirror=True)
    return m


# ----------------------------------------------------------------------------- animations
def animations(an):
    for f in (H.std_idle, H.std_walk, H.std_fly, H.std_sing, H.std_bathe):
        f(an)
    OUT, REST = H.OUT_, H.REST
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold

    # beg: leaning in, both hands held out cupped, head cocked, bouncing on her toes with a squawk
    squawks = [(0, REST), (0.1, OUT), (0.3, OUT), (0.4, REST), (0.6, REST), (0.7, OUT), (0.9, OUT), (1.0, REST), (1.2, REST)]
    an.add("beg", 1.2, True, merge({
        "body": {"position": T((0, (0, 0, 0)), (0.3, (0, 0.6, 0)), (0.6, (0, 0, 0)), (0.9, (0, 0.6, 0)), (1.2, (0, 0, 0))),
                 "rotation": hold(8, 0, 0)},
        "head": {"rotation": T((0, (-12, 0, 10)), (0.6, (-12, 0, -10)), (1.2, (-12, 0, 10)))},
        "mouth": {"position": T(*squawks)},
        "tail": {"rotation": T((0, (0, 10, 0)), (0.3, (0, -10, 0)), (0.6, (0, 10, 0)), (0.9, (0, -10, 0)), (1.2, (0, 10, 0)))},
    },
        sym("arm", hold(-60, 0, 6)), sym("forearm", hold(-25, 0, 0)), sym("hand", hold(-20, 0, 0)),
        sym("wing", T((0, (0, 0, -6)), (0.3, (0, 0, -18)), (0.6, (0, 0, -6)), (0.9, (0, 0, -18)), (1.2, (0, 0, -6))))))


if __name__ == "__main__":
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="sing", head_center=(0, 22.5 + L), body_center=(0, 15.5 + L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("beg", 0.2, 0, 0), ("beg", 0.2, -30, 8)])
    write_variants()
