#!/usr/bin/env python3
"""Owl Harpy - species definition. The shared engine lives in harpy_lib.py.

    python tools/gen_owl_harpy.py                 # write the three asset files
    python tools/gen_owl_harpy.py --preview x.png # also render preview sheets (x.png, x_face.png, x_body.png)
    python tools/gen_owl_harpy.py --lineup x.png  # also render all species variants side by side

Species variants (VARIANTS below) share the geometry and animations and only get their own texture,
owl_harpy_<variant>.png; the default (great horned owl) stays owl_harpy.png. A variant without ear tufts
simply paints them transparent.

Field guide: "Large nocturnal flier and fierce predator of pigs, sheep, cows and even frogs. Mainly lives in
forested areas. Stalks players but is mostly harmless. Hoots at players and comes out when offered a piece of
meat." Patient, observant, predatory.
Design: tawny brown plumage barred with cream, two tall ear tufts, huge round golden eyes under heavy brows,
soft rounded wings, feathered legs down to dark talons; a moss-green hooded wrap over a barred feather bodice,
a leather belt with a little bone charm. Taller than the crow.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

NAME = "owl_harpy"
L = 1.5                                          # everything above the shins sits this much higher than the sparrow

# ----------------------------------------------------------------------------- palette
PAL = H.Pal()
PAL.HAIR = ramp([rgb(44, 30, 20), rgb(76, 52, 34), rgb(112, 80, 50), rgb(148, 110, 70), rgb(186, 148, 100), rgb(220, 190, 146)])
PAL.SKIN = ramp([rgb(196, 150, 120), rgb(222, 180, 150), rgb(240, 206, 178), rgb(250, 226, 202)], nodither=True)
PAL.FEA = ramp([rgb(40, 28, 20), rgb(70, 48, 32), rgb(108, 76, 48), rgb(146, 108, 70), rgb(186, 150, 106), rgb(222, 198, 160), rgb(244, 232, 208)])
PAL.EYE = ramp([rgb(150, 92, 10), rgb(206, 140, 16), rgb(240, 184, 30), rgb(252, 214, 70), rgb(255, 240, 150)])
PAL.LEG = ramp([rgb(120, 96, 70), rgb(156, 130, 98), rgb(194, 170, 136), rgb(222, 204, 172), rgb(240, 230, 206)])
PAL.TALON = [rgb(26, 22, 20), rgb(60, 52, 46)]
PAL.EYE_OUT, PAL.PUPIL = rgb(40, 24, 12), rgb(14, 8, 4)
PAL.BROW = PAL.HAIR[0]
PAL.eye_a, PAL.eye_b, PAL.eye_slant = 4.6, 5.6, 0.0         # big and round
PAL.pupil, PAL.hl_dy, PAL.hl2_dy, PAL.lid_r = (2.3, 3.0), 3, 2, (5.4, 6.3)
PAL.brows = [(8, 9, -4, 3, 0.0, 2), (24, 9, -4, 3, 0.0, 2)]      # heavy, level brows: the owl stare
PAL.mouth_px = {(x, 28): PAL.LIP for x in range(15, 17)}      # a small, serious mouth
PAL.wing_base_t, PAL.tail_fluff_t, PAL.fluff_t = (0.25, 0.45), 0.72, 0.85

MOSS = ramp([rgb(30, 40, 26), rgb(48, 62, 38), rgb(72, 88, 52), rgb(100, 116, 70), rgb(134, 146, 94)])
LEATHER = ramp([rgb(36, 24, 16), rgb(62, 42, 28), rgb(96, 66, 44), rgb(130, 94, 64)])
BONE = ramp([rgb(170, 160, 134), rgb(212, 204, 180), rgb(240, 236, 220)])


def barred(x, y, t, period=4):
    """owl barring: a darker band every few texels, slightly wavy"""
    return t - (0.22 if (y + (x // 3) % 2) % period == 0 else 0.0)


def spotted(x, y, t, period=4):
    """burrowing-owl spotting: scattered pale dots instead of bars"""
    return t + (0.30 if noise(x // 2, y // 2, 81 + period) > 0.80 else 0.0)


# ----------------------------------------------------------------------------- parts
def declare_parts():
    H.standard_parts()
    for args in [
        ("eartuft", 2, 4.5, 1.25), ("shin", 2, 8, 2), ("tail_f", 1.75, 8, .5), ("tail_g", 1.75, 8, .5),
        ("mantle", 6.5, 3, 4.5, ("up",)), ("charm", .5, 1.5, .5),
        ("prim_a", 1.75, 10, .5), ("prim_b", 1.75, 10, .5), ("sec_a", 1.75, 8.5, .5), ("sec_b", 1.75, 8.5, .5),
    ]:
        H.part(*args)


# ----------------------------------------------------------------------------- texture
def paint(pal=PAL, MOSS=MOSS, tufts=True, pattern="bars"):
    pt = H.Painter()
    FEA, SKIN = pal.FEA, pal.SKIN
    barred_ = barred if pattern == "bars" else spotted
    D = H.D
    H.paint_head(pt, pal)
    H.paint_hair(pt, pal)
    # soft, rounded wing feathers with pale bars near the tips
    H.paint_wings_tail(pt, pal, {
        "sec_a": (3, 4, 5, 2, True), "sec_b": (2, 3, 5, 1, True), "prim_a": (2, 3, 5, 1, True), "prim_b": (3, 4, 5, 2, True),
        "tail_f": (3, 4, 5, 2, True), "tail_g": (2, 3, 5, 1, True),
        "cov_a": (2, 3, 4, None, True), "cov_b": (2, 3, 5, None, True), "cov_c": (3, 4, 5, None, True),
    })
    H.paint_body_skin(pt, pal)
    H.paint_legs(pt, pal)
    # feathered legs: barred buff down to the ankle
    pt("shin", "all", lambda f, x, y, w, h: pick(pal.LEG, barred_(x, y, 0.62 + (noise(x, y, 5) - 0.5) * 0.15, 3), x, y))
    pt("thigh", "all", lambda f, x, y, w, h: pick(FEA, barred_(x, y, 0.62 + (noise(x, y, 6) - 0.5) * 0.14, 3), x, y))

    for p, spec in (("tassel", (2, 3, 4)), ("cuff_f", (3, 4, 5)), ("trim", (3, 4, 5)), ("hfeather", (3, 4, 6))):
        pt(p, "all", H.feather_fn(pal, *spec, None, True, False))
    # ear tufts: hair-coloured feather plumes, darker at the tips
    def eartuft(f, x, y, w, h):
        t = 0.62 - 0.45 * (1 - y / h) + (noise(x, y, 7) - 0.5) * 0.12
        if f in BROAD and x == w // 2:
            t -= 0.12
        if not tufts or (y < 3 and abs(x + 0.5 - w / 2.0) > (y + 1) * w / 8.0):
            return CLEAR                                                  # pointed tip (or no tufts at all)
        return pick(pal.HAIR, t, x, y)
    pt("eartuft", "all", eartuft)
    pt("back_cover", "all", lambda f, x, y, w, h: pick(FEA, barred_(x, y, 0.40 + 0.3 * y / h + (noise(x, y, 19) - 0.5) * 0.12), x, y))

    # barred feather bodice
    def bodice(f, x, y, w, h):
        if f == "up":
            return pick(SKIN, 0.66, x, y)
        if f == "down":
            return pick(SKIN, 0.5, x, y)
        if y >= h - D // 2:
            return LEATHER[1]
        t = 0.70 + (noise(x, y, 31) - 0.5) * 0.18 - (0.08 if f in ("east", "west", "south") else 0)
        return pick(FEA, barred_(x, y, t), x, y)
    pt("chest", "all", bodice)
    pt("bust", "all", bodice)

    # moss-green mantle around the shoulders
    def mossy(f, x, y, w, h):
        t = 0.45 + (noise(x, y, 51) - 0.5) * 0.25
        if f in BROAD and y >= h - 2:
            t -= 0.2                                                      # rolled edge
        if noise(x // 2, y // 2, 53) > 0.82:
            t += 0.25                                                     # lichen flecks
        return pick(MOSS, t, x, y)
    pt("mantle", "all", mossy)

    def belt(f, x, y, w, h):
        if y < 2 or y >= h - 2:
            return LEATHER[0]
        return pick(LEATHER, 0.45 + 0.1 * (((x + y) % 4) < 2) + (noise(x, y, 37) - 0.5) * 0.12, x, y)
    pt("sash", "all", belt)
    pt("charm", "all", lambda f, x, y, w, h: pick(BONE, 0.4 + 0.5 * (y % 3 == 0), x, y))
    pt("pendant", "all", lambda f, x, y, w, h: pick(BONE, 0.3 + 0.6 * (x < w // 2), x, y))
    pt("cord", "all", lambda f, x, y, w, h: LEATHER[1])

    # moss cloth strips
    def cloth(broad):
        def fn(f, x, y, w, h):
            if f not in broad:
                return MOSS[1]
            r = h - 1 - y
            tl = 1.8 * D
            if r < tl:
                half = (w / 2.0) * (min(1.0, (r + 1) / tl) ** 0.6)
                if abs(x + 0.5 - w / 2.0) > half or (r < D // 2 and noise(x, y, 41) > 0.45):
                    return CLEAR
            if y < D:
                return LEATHER[2] if (x // 2) % 2 else LEATHER[1]
            t = 0.42 + (noise(x, y, 43) - 0.5) * 0.24
            if x % 5 == 0:
                t -= 0.14
            return pick(MOSS, t, x, y)
        return fn
    for p in ("leaf_s", "leaf_m", "leaf_l"):
        pt(p, "all", cloth(BROAD))
    pt("leaf_side", "all", cloth(("east", "west")))

    def bracer(f, x, y, w, h):
        if f in ("up", "down"):
            return LEATHER[1]
        return pick(LEATHER, 0.45 + (noise(x, y, 47) - 0.5) * 0.25, x, y)
    pt("bracer", "all", bracer)
    return pt.T


# ----------------------------------------------------------------------------- species variants
def variant_pal(hair, fea, eye, leg, brow_dark=0, pupil=None):
    pal = H.Pal()
    for attr in ("SKIN", "TALON", "EYE_OUT", "PUPIL", "eye_a", "eye_b", "eye_slant", "pupil", "hl_dy", "hl2_dy", "lid_r",
                 "brows", "mouth_px", "wing_base_t", "tail_fluff_t", "fluff_t"):
        setattr(pal, attr, getattr(PAL, attr))
    pal.HAIR, pal.FEA, pal.EYE, pal.LEG = ramp(hair), ramp(fea), ramp(eye), ramp(leg)
    pal.BROW = pal.HAIR[brow_dark]
    if pupil:
        pal.PUPIL = pupil
    return pal


DARK_EYES = [rgb(16, 10, 8), rgb(30, 20, 14), rgb(46, 30, 22), rgb(66, 44, 30), rgb(92, 64, 44)]
YELLOW_EYES = [rgb(170, 130, 10), rgb(220, 180, 20), rgb(250, 220, 40), rgb(255, 238, 100), rgb(255, 250, 180)]

# name -> (palette, cloak ramp, ear tufts, plumage pattern). Keep in sync with OwlHarpy.Variant.
VARIANTS = {
    # barn owl: golden-buff back, white front and face, black eyes, no tufts; a straw-coloured cloak (she lives in barns)
    "barn": (variant_pal(
        hair=[rgb(120, 86, 48), rgb(170, 128, 74), rgb(206, 168, 108), rgb(228, 200, 150), rgb(244, 228, 200), rgb(252, 246, 236)],
        fea=[rgb(96, 76, 56), rgb(150, 120, 84), rgb(196, 160, 108), rgb(222, 196, 150), rgb(238, 224, 200), rgb(248, 242, 232), rgb(255, 252, 246)],
        eye=DARK_EYES, leg=[rgb(170, 150, 140), rgb(206, 190, 180), rgb(232, 222, 214), rgb(246, 240, 234), rgb(255, 252, 248)], pupil=rgb(4, 2, 2)),
        ramp([rgb(110, 86, 40), rgb(150, 122, 60), rgb(190, 160, 86), rgb(216, 192, 118), rgb(236, 218, 156)]), False, "bars"),
    # snowy owl: white with dark bars, yellow eyes, feathered white legs, almost no tufts; an icy blue-grey cloak
    "snowy": (variant_pal(
        hair=[rgb(150, 150, 156), rgb(196, 196, 202), rgb(226, 226, 232), rgb(242, 242, 246), rgb(250, 250, 252), rgb(255, 255, 255)],
        fea=[rgb(40, 40, 46), rgb(80, 80, 88), rgb(150, 150, 158), rgb(206, 206, 212), rgb(234, 234, 238), rgb(248, 248, 250), rgb(255, 255, 255)],
        eye=YELLOW_EYES, leg=[rgb(200, 200, 206), rgb(226, 226, 230), rgb(240, 240, 244), rgb(250, 250, 252), rgb(255, 255, 255)], brow_dark=0),
        ramp([rgb(50, 62, 78), rgb(76, 92, 112), rgb(110, 128, 150), rgb(150, 168, 188), rgb(196, 210, 224)]), False, "bars"),
    # tawny owl: warm rufous brown, dark eyes, no tufts; the usual woodland moss
    "tawny": (variant_pal(
        hair=[rgb(60, 28, 16), rgb(98, 48, 26), rgb(138, 72, 38), rgb(176, 104, 58), rgb(206, 144, 92), rgb(232, 188, 140)],
        fea=[rgb(48, 22, 12), rgb(84, 40, 22), rgb(124, 64, 34), rgb(164, 96, 52), rgb(198, 138, 86), rgb(226, 186, 136), rgb(244, 222, 190)],
        eye=DARK_EYES, leg=[rgb(150, 110, 80), rgb(186, 148, 112), rgb(214, 184, 148), rgb(232, 210, 180), rgb(246, 232, 210)]),
        MOSS, False, "bars"),
    # burrowing owl: sandy brown spotted with white, bright yellow eyes, long pale legs, no tufts; a terracotta wrap
    "burrowing": (variant_pal(
        hair=[rgb(84, 62, 40), rgb(122, 92, 60), rgb(160, 124, 84), rgb(194, 160, 116), rgb(224, 198, 158), rgb(244, 230, 204)],
        fea=[rgb(70, 50, 32), rgb(108, 80, 52), rgb(146, 112, 76), rgb(184, 150, 108), rgb(216, 190, 150), rgb(238, 222, 194), rgb(252, 246, 232)],
        eye=YELLOW_EYES, leg=[rgb(170, 160, 140), rgb(204, 196, 178), rgb(228, 222, 206), rgb(242, 238, 226), rgb(252, 250, 242)]),
        ramp([rgb(96, 44, 26), rgb(136, 66, 38), rgb(174, 96, 58), rgb(204, 132, 86), rgb(228, 172, 128)]), False, "spots"),
}


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "harpies", "textures", "entity")
    texes = {"great_horned": paint()}
    for v, (pal, cloak, tufts, pattern) in VARIANTS.items():
        texes[v] = paint(pal, cloak, tufts, pattern)
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
    bg, W = (44, 48, 44, 255), 360
    rows = [(g("idle"), 0, -20, 6, 9, (0, 17 + L)), (g("fly"), 0.4, -30, 10, 9, (0, 17 + L)), (g("idle"), 0, -15, 0, 28, (0, 22.5 + L))]
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
        C("mantle", [-3.25, 17.4, -2.25]), C("pendant", [-0.5, 16.8, -2.6]), C("cord", [-1.5, 18.2, -2.45]),
        HG("charm", 2.2, 12.6, -2.15, 4, 0),
    ]
    bust = C("bust", [-2.25, 16.9, -2.375])
    body += [bust, mirror_cube(bust)]
    for i, x in enumerate((-2.5, -1.5, -0.5, 0.5, 1.5)):
        body.append(HG("trim", x, 17.5, -2.4, 6 if i % 2 else -6))
    bone("body", "root", [0, 12 + L, 0], body)

    sk = []
    front = [("leaf_l", -3.0, 4), ("leaf_m", -1.5, -3), ("leaf_l", -0.2, 2), ("leaf_m", 1.0, -2), ("leaf_l", 2.0, 5)]
    for i, (p, x, rz) in enumerate(front):
        sk.append(HG(p, x, 13.7, -2.7 - 0.15 * (i % 2), rz, -8))
    for i, (p, x, rz) in enumerate((("leaf_l", -3.0, 3), ("leaf_m", -1.0, -2), ("leaf_l", 0.8, 3), ("leaf_m", 2.0, -3))):
        sk.append(HG(p, x, 13.7, 2.2 + 0.15 * (i % 2), rz, 8))
    for z in (-2.2, 0.0):
        sk.append(C("leaf_side", [-3.4, 8.2, z], pivot=[-3.15, 13.7, z + 1.25], rotation=[0, 0, -9]))
        sk.append(mirror_cube(sk[-1]))
    sk.append(HG("tassel", -2.7, 13.4, -3.1, -3, -4))
    bone("skirt", "body", [0, 13.7 + L, 0], sk)

    # ---- head: two feathery ear tufts rising from the crown
    head = [C("head", [-4, 20, -4]), C("hair_cap", [-4, 20, -4], inflate=0.75)]
    ear_r = C("ear", [-5.0, 21.7, -1.2])
    head += [ear_r, mirror_cube(ear_r)]
    for i, (p, x, rz) in enumerate((("bang_b", -4.6, -5), ("bang_a", -3.0, 4), ("bang_c", -1.6, -3), ("bang_a", -0.1, 5),
                                    ("bang_c", 1.4, -4), ("bang_b", 3.0, 6))):
        head.append(HG(p, x, 28.4, -5.35 + 0.1 * (i % 2), rz))
    head.append(HG("frame", -4.9, 28, -5.0, -4))
    head.append(mirror_cube(head[-1]))
    tuft_r = C("eartuft", [-4.2, 27.4, -2.4], pivot=[-3.2, 27.4, -1.8], rotation=[-10, 0, -24])
    head += [tuft_r, mirror_cube(tuft_r)]
    bone("head", "body", [0, 20 + L, 0], head)

    bone("eyelids", "head", [0, 23.5 + L, -3.8], [C("eyelids", [-3.25, 21.75, -3.9])])
    bone("mouth", "head", [0, 21 + L, -3.8], [C("mouth", [-0.875, 20.25, -3.9])])

    side = [("strand_m1", -6.1, 26.2, -3.4, -6, 0), ("strand_m2", -6.3, 25.2, -2.0, -9, 0),
            ("strand_m1", -6.2, 26.2, 0.8, -7, 3), ("strand_m2", -6.0, 26.2, 2.4, -10, 4)]
    bone("hair_side_r", "head", [-4.7, 27 + L, 0], [HG(*s) for s in side], mirror=True)
    back = []
    names = ["strand_m1", "strand_l1", "strand_m2", "strand_l2", "strand_m1"]
    for i in range(11):
        x = -4.8 + 0.96 * i
        back.append(HG(names[i % 5], x, 26.8, 4.9 + 0.3 * (i % 3), x * 1.6, 5 + 2 * (i % 3)))
    bone("hair_back", "head", [0, 27 + L, 4.5], back)

    bone("arm_r", "body", [-3.25, 19.5 + L, 0], [C("arm_up", [-4.0, 16, -0.75])], mirror=True)
    bone("forearm_r", "arm_r", [-3.25, 16 + L, 0], [
        C("forearm", [-4.0, 13, -0.75]), C("bracer", [-4.5, 13.6, -1.25]),
        HG("cuff_f", -4.15, 13.8, -1.45, 4, -6), HG("cuff_f", -3.25, 13.8, -1.45, -4, -6),
    ], mirror=True)
    bone("hand_r", "forearm_r", [-3.25, 13 + L, 0], [
        C("palm", [-4.0, 11.5, -0.5]), C("fingers", [-4.0, 10.5, -0.5]), C("thumb", [-2.5, 11.5, -0.25]),
    ], mirror=True)

    # ---- wings: broad and rounded
    w = [C("wing_base", [-7, 10, 2.3])]
    rows = [(19.5, (-7.5, -5.5), ("cov_a", "cov_a")), (16.8, (-9.0, -7.0, -5.0), ("cov_a", "cov_b", "cov_a")),
            (14.1, (-10.0, -8.0, -6.0), ("cov_b", "cov_b", "cov_b")), (11.4, (-9.0, -7.0, -5.0), ("cov_c", "cov_c", "cov_c"))]
    for r, (ytop, xs, ps) in enumerate(rows):
        for x, p in zip(xs, ps):
            w.append(C(p, [x, ytop - 3, 3.3 + 0.2 * r]))
    for i, (x, ang) in enumerate(zip((-10.0, -8.6, -7.2, -5.8, -4.4), (-9, -6.5, -4, -2, 0))):
        w.append(HG("sec_a" if i % 2 == 0 else "sec_b", x, 12.5, 2.3 + 0.25 * (i % 2), ang))
    bone("wing_r", "body", [-2.5, 19 + L, 2.3], w, mirror=True)
    pr = []
    for i, (x, ang) in enumerate(zip((-10.8, -9.3, -7.8, -6.3, -4.8), (-15, -11, -8, -5, -2))):
        pr.append(HG("prim_a" if i % 2 == 0 else "prim_b", x, 12.5, 2.8 + 0.25 * (i % 2), ang))
    bone("wing_tip_r", "wing_r", [-8, 12.5 + L, 3.0], pr, mirror=True)

    # ---- tail: short and round
    tail = [C("tail_fluff", [-1.75, 12.2, 2.2])]
    for i, (x, ang) in enumerate(zip((-3.3, -2.0, -0.875, 0.25, 1.5), (-12, -6, 0, 6, 12))):
        tail.append(HG("tail_f" if i % 2 == 0 else "tail_g", x, 12.7, 3.4 + 0.25 * (i % 2), ang, 8))
    bone("tail", "body", [0, 13.2 + L, 3], tail)

    # ---- legs: thick, feathered, big talons
    cx = -1.25
    bone("leg_r", "root", [cx, 12 + L, 0], [C("thigh", [cx - 1, 9.5, -1]), C("fluff", [cx - 1.25, 8.5, -1.25])], mirror=True)
    bone("shin_r", "leg_r", [cx, 9 + L, 0], [cube("shin", [cx - 1, 1.5, -1]), cube("ankle", [cx - 1, 1, -1])], mirror=True)
    foot = [cube("heel", [cx - 1, 0, -1])]
    for dx in (-2.0, -0.5, 1.0):
        x = cx + dx
        foot += [cube("toe1", [x, 0, -3.5]), cube("talon", [x, 0, -5.5], pivot=[x + .5, .5, -3.5], rotation=[28, 0, 0])]
    foot += [cube("htoe", [cx - .5, 0, 1]), cube("htalon", [cx - .5, 0, 2.5], pivot=[cx, .5, 2.5], rotation=[-26, 0, 0])]
    bone("foot_r", "shin_r", [cx, 1, 0], foot, mirror=True)
    return m


# ----------------------------------------------------------------------------- animations
def animations(an):
    for f in (H.std_walk, H.std_fly):
        f(an)
    OUT, REST = H.OUT_, H.REST
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold

    # idle: very still body, the head swivelling far round to look behind her, slow blinks
    an.add("idle", 6.0, True, merge({
        "head": {"rotation": T((0, (0, 0, 0)), (1.0, (0, 0, 0)), (1.3, (0, 70, 0)), (2.6, (0, 70, 0)), (2.9, (0, 0, 0)),
                               (3.8, (0, 0, 0)), (4.0, (0, 0, 14)), (4.6, (0, 0, 14)), (4.8, (0, -55, 0)), (5.6, (0, -55, 0)), (6.0, (0, 0, 0)))},
        "body": {"position": T((0, (0, 0, 0)), (3, (0, -0.2, 0)), (6, (0, 0, 0)))},
        "tail": {"rotation": T((0, (0, 0, 0)), (3, (0, 4, 0)), (6, (0, 0, 0)))},
        "eyelids": {"position": T((0, REST), (3.3, REST), (3.4, OUT), (3.7, OUT), (3.8, REST), (6, REST))},
    },
        sym("wing", hold(0, 0, -1)), sym("arm", hold(0, 0, 2)), sym("forearm", hold(-6, 0, 0)), sym("hand", hold(-8, 0, 0))))

    # hoot: puffs up, head low and forward, two slow round "hoo"s
    hoos = [(0, REST), (0.3, OUT), (0.8, OUT), (0.95, REST), (1.3, REST), (1.45, OUT), (2.0, OUT), (2.15, REST), (3.0, REST)]
    an.add("hoot", 3.0, True, merge({
        "body": {"rotation": T((0, (0, 0, 0)), (0.3, (8, 0, 0)), (2.2, (8, 0, 0)), (3.0, (0, 0, 0)))},
        "head": {"rotation": T((0, (0, 0, 0)), (0.3, (14, 0, 0)), (0.9, (8, 0, 0)), (1.45, (14, 0, 0)), (2.1, (8, 0, 0)), (3.0, (0, 0, 0)))},
        "tail": {"rotation": T((0, (0, 0, 0)), (0.3, (-12, 0, 0)), (2.2, (-12, 0, 0)), (3.0, (0, 0, 0)))},
        "mouth": {"position": T(*hoos)},
    },
        sym("wing", T((0, (0, 0, 0)), (0.3, (0, 0, -10)), (2.2, (0, 0, -10)), (3.0, (0, 0, 0)))),
        sym("arm", hold(0, 0, 4))))

    # sleep: perched and puffed, head sunk into the shoulders, eyes shut
    an.add("sleep", 4.0, True, merge({
        "body": {"position": T((0, (0, -0.6, 0)), (2, (0, -0.8, 0)), (4, (0, -0.6, 0)))},
        "head": {"rotation": T((0, (16, 0, 6)), (2, (18, 0, 6)), (4, (16, 0, 6)))},
        "eyelids": {"position": hold(*OUT)},
        "tail": {"rotation": hold(-6, 0, 0)},
    },
        sym("wing", hold(0, 0, -6)), sym("arm", hold(-20, 0, 6)), sym("forearm", hold(-70, 0, 0))))

    # pounce: legs thrown forward with the talons open, wings raised high
    an.add("pounce", 0.8, True, merge({
        "body": {"rotation": T((0, (-20, 0, 0)), (0.4, (-30, 0, 0)), (0.8, (-20, 0, 0)))},
        "head": {"rotation": hold(20, 0, 0)},
        "leg_r": {"rotation": T((0, (-60, 0, 0)), (0.4, (-80, 0, 0)), (0.8, (-60, 0, 0)))},
        "leg_l": {"rotation": T((0, (-60, 0, 0)), (0.4, (-80, 0, 0)), (0.8, (-60, 0, 0)))},
        "foot_r": {"rotation": hold(-30, 0, 0)}, "foot_l": {"rotation": hold(-30, 0, 0)},
    },
        sym("wing", T((0, (0, 0, -120)), (0.4, (0, 0, -95)), (0.8, (0, 0, -120)))), sym("wing_tip", hold(0, 0, 10))))


if __name__ == "__main__":
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="hoot", head_center=(0, 22.5 + L), body_center=(0, 15.5 + L),
          face_views=[("idle", 0, 0, 0), ("hoot", 0.5, 0, 0), ("sleep", 1.0, 0, 0), ("idle", 3.5, 0, 0)])
    write_variants()
