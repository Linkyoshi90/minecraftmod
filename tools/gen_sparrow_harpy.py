#!/usr/bin/env python3
"""Sparrow Harpy - species definition. The shared engine lives in harpy_lib.py.

    python tools/gen_sparrow_harpy.py                 # write the three asset files
    python tools/gen_sparrow_harpy.py --preview x.png # also render preview sheets (x.png, x_face.png, x_body.png)
    python tools/gen_sparrow_harpy.py --lineup x.png  # also render all colour variants side by side

Colour variants (VARIANTS below) share the geometry and animations and only get their own texture,
sparrow_harpy_<variant>.png. The default palette stays the plain sparrow_harpy.png.

Target look: the concept art in harpy_field_guide.html - chestnut hair, amber eyes, brown-to-cream
wings, olive leaf loincloth, yellow bird legs with black talons.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD, PARTS

NAME = "sparrow_harpy"
PAL = H.Pal()                                   # the library defaults are the sparrow's colours
LEAF = ramp([rgb(36, 46, 28), rgb(56, 66, 38), rgb(86, 98, 54), rgb(120, 132, 74), rgb(156, 164, 98)])
GOLD = ramp([rgb(150, 112, 44), rgb(214, 170, 70), rgb(244, 208, 118)])


# ----------------------------------------------------------------------------- texture
def paint(pal=PAL, LEAF=LEAF, GOLD=GOLD):
    pt = H.Painter()
    FEA, SKIN, EYE = pal.FEA, pal.SKIN, pal.EYE
    H.paint_head(pt, pal)
    H.paint_hair(pt, pal)
    H.paint_wings_tail(pt, pal, {
        "sec_a": (3, 4, 5, 2), "sec_b": (2, 3, 5, 1), "prim_a": (1, 2, 5, 1), "prim_b": (2, 5, 6, 3),
        "tail_f": (3, 4, 5, 2), "tail_g": (1, 3, 5, 2),
        "cov_a": (1, 1, 2, None, True), "cov_b": (1, 2, 3, None, True), "cov_c": (2, 3, 5, None, True),
    })
    H.paint_body_skin(pt, pal)
    H.paint_legs(pt, pal)

    # feathery outfit bits
    pt("tassel", "all", H.feather_fn(pal, 5, 4, 3, None, False, False))
    pt("cuff_f", "all", H.feather_fn(pal, 5, 5, 4, None, False, False))
    pt("trim", "all", H.feather_fn(pal, 6, 5, 4, None, False, False))
    pt("hfeather", "all", H.feather_fn(pal, 6, 6, 5, None, False, False))
    pt("back_cover", "all", lambda f, x, y, w, h: pick(FEA, 0.30 + 0.45 * y / h + (noise(x, y, 19) - 0.5) * 0.16 + (0.18 if (x // 3 + y // 3) % 4 == 0 else 0), x, y))

    # feathered bandeau with a slight bust
    D = H.D

    papi = getattr(pal, "papi", False)
    COTTON = ramp([rgb(178, 182, 190), rgb(214, 218, 224), rgb(236, 238, 242), rgb(248, 249, 251), rgb(255, 255, 255)])

    def shirt(f, x, y, w, h):                                   # Papi's white sleeveless shirt
        if f == "up":
            return pick(SKIN, 0.66, x, y)
        if f == "down":
            return pick(COTTON, 0.5, x, y)
        if y >= h - D // 2:
            return COTTON[1]                                    # a stitched hem
        t = 0.80 + (noise(x, y, 29) - 0.5) * 0.12 - (0.12 if f in ("east", "west", "south") else 0) - (0.08 if (x + y) % 9 == 0 else 0)
        return pick(COTTON, t, x, y)

    def wrap(f, x, y, w, h):
        if papi:
            return shirt(f, x, y, w, h)
        if f == "up":
            return pick(SKIN, 0.66, x, y)
        if f == "down":
            return pick(SKIN, 0.5, x, y)
        if y < D // 2:
            return pick(FEA, 0.62 + (noise(x, y, 29) - 0.5) * 0.2, x, y)       # top edge
        if y >= h - D // 2:
            return FEA[3] if x % 4 < 2 else FEA[2]                               # brown hem
        t = 0.82 + (noise(x, y, 31) - 0.5) * 0.28 - (0.10 if f in ("east", "west", "south") else 0)
        if noise(x // 2, y // 2, 33) > 0.84:
            t = 1.0
        if f == "north" and (x + y * 2) % 11 == 0:
            t -= 0.2
        return pick(FEA, t, x, y)
    pt("chest", "all", wrap)
    pt("bust", "all", wrap)

    def sash(f, x, y, w, h):
        if y < 2 or y >= h - 2:
            return FEA[1]
        if x % (D * 2) == D and y == h // 2:
            return GOLD[2] if f in BROAD else FEA[2]
        return pick(FEA, 0.40 + 0.12 * (((x + y) % 4) < 2) + (noise(x, y, 37) - 0.5) * 0.1, x, y)
    pt("sash", "all", sash)
    pt("pendant", "all", lambda f, x, y, w, h: EYE[2] if (f in BROAD and 1 <= x < w - 1 and 1 <= y < h - 1) else pick(GOLD, 0.5 + 0.5 * (x < w // 2), x, y))
    pt("cord", "all", lambda f, x, y, w, h: FEA[1])

    # loincloth strips: olive leaf, brown belt-trim, frayed hem
    def leaf(broad):
        def fn(f, x, y, w, h):
            if f not in broad:
                return LEAF[1]
            r = h - 1 - y
            tl = 2.2 * D
            if papi:                                            # denim: square-cut, orange stitching, no leaf veins
                if y < D:
                    return LEAF[1] if (x // 2) % 2 else LEAF[0]
                if y in (D, D + 1) or r == 1 or x in (1, w - 2):
                    return GOLD[1] if (x + y) % 2 == 0 else pick(LEAF, 0.6, x, y)
                return pick(LEAF, 0.5 + (noise(x, y, 43) - 0.5) * 0.3 + (0.1 if (x + y) % 3 == 0 else 0), x, y)
            if r < tl:
                half = (w / 2.0) * (min(1.0, (r + 1) / tl) ** 0.8)
                if abs(x + 0.5 - w / 2.0) > half or (r < D // 2 and noise(x, y, 41) > 0.5):
                    return CLEAR
            if y < D:                                           # brown waistband
                return FEA[2] if (x // 2) % 2 else FEA[1]
            if y in (D, D + 1):
                return GOLD[0]
            cx = (w - 1) / 2.0
            t = 0.42 + (noise(x, y, 43) - 0.5) * 0.28
            if abs(x - cx) < 0.8:
                t += 0.28                                       # midrib
            elif (y - D * 2 + abs(x - cx) * 2.2) % 7 < 1.2:
                t += 0.20                                       # side veins
            if x == 0 or x == w - 1:
                t -= 0.2
            return pick(LEAF, t, x, y)
        return fn
    for p in ("leaf_s", "leaf_m", "leaf_l"):
        pt(p, "all", leaf(BROAD))
    pt("leaf_side", "all", leaf(("east", "west")))

    def bracer(f, x, y, w, h):
        if f in ("up", "down"):
            return FEA[2]
        if y >= h - D // 2:
            return FEA[4] if x % 2 else FEA[3]
        t = 0.55 + (noise(x, y, 47) - 0.5) * 0.3 + (0.2 if y < D // 2 else 0)
        if noise(x // 2, y // 2, 49) > 0.8:
            t = 0.95
        return pick(FEA, t, x, y)
    pt("bracer", "all", bracer)
    return pt.T


# ----------------------------------------------------------------------------- colour variants
def variant_pal(hair=None, fea=None, eye=None, leg=None, pupil=None):
    pal = H.Pal()
    if hair:
        pal.HAIR = ramp(hair)
        pal.BROW = pal.HAIR[1]
    if fea:
        pal.FEA = ramp(fea)
    if eye:
        pal.EYE = ramp(eye)
    if leg:
        pal.LEG = ramp(leg)
    if pupil:
        pal.PUPIL = pupil
    return pal


# name -> (palette, loincloth ramp). Keep in sync with SparrowHarpy.Variant.
VARIANTS = {
    # sandy desert sparrow: pale blonde hair, buff feathers, dry palm-straw loincloth
    "sand": (variant_pal(
        hair=[rgb(110, 80, 44), rgb(150, 112, 64), rgb(190, 150, 94), rgb(220, 186, 128), rgb(240, 214, 164), rgb(252, 236, 200)],
        fea=[rgb(84, 62, 40), rgb(122, 92, 60), rgb(164, 128, 86), rgb(198, 164, 116), rgb(226, 200, 156), rgb(242, 228, 200), rgb(252, 248, 236)]),
        ramp([rgb(96, 78, 38), rgb(140, 116, 58), rgb(182, 156, 84), rgb(214, 190, 116), rgb(236, 218, 156)])),
    # slate sparrow of the cold north (dunnock-like): charcoal hair, grey feathers, spruce loincloth, russet eyes
    "slate": (variant_pal(
        hair=[rgb(34, 32, 36), rgb(56, 52, 58), rgb(84, 78, 84), rgb(114, 108, 114), rgb(148, 142, 146), rgb(186, 182, 184)],
        fea=[rgb(30, 28, 32), rgb(52, 48, 54), rgb(80, 74, 80), rgb(112, 106, 110), rgb(150, 146, 148), rgb(204, 202, 204), rgb(244, 244, 246)],
        eye=[rgb(70, 30, 24), rgb(110, 48, 34), rgb(150, 70, 44), rgb(186, 100, 62), rgb(214, 140, 96)]),
        ramp([rgb(22, 36, 30), rgb(34, 54, 42), rgb(52, 78, 58), rgb(74, 104, 78), rgb(102, 132, 100)])),
    # tree sparrow of the woods: rufous red-brown hair and wings, fresh spring-green loincloth
    "tree": (variant_pal(
        hair=[rgb(58, 22, 14), rgb(92, 36, 22), rgb(130, 56, 32), rgb(166, 80, 44), rgb(200, 112, 64), rgb(228, 152, 100)],
        fea=[rgb(40, 22, 16), rgb(70, 38, 24), rgb(110, 60, 34), rgb(152, 88, 50), rgb(200, 140, 96), rgb(236, 214, 186), rgb(250, 244, 234)]),
        ramp([rgb(40, 62, 26), rgb(62, 96, 36), rgb(96, 136, 52), rgb(136, 172, 74), rgb(178, 204, 108)])),
    # rare sky-blue one, a nod to Papi from Monster Musume: messy light blue hair (the ahoge every sparrow has), light
    # blue wings, orange-amber eyes, yellow scaly bird legs, a white sleeveless shirt and denim shorts
    "papi": (variant_pal(
        hair=[rgb(56, 104, 146), rgb(86, 146, 192), rgb(122, 186, 228), rgb(158, 212, 244), rgb(194, 234, 252), rgb(226, 248, 255)],
        fea=[rgb(38, 76, 124), rgb(58, 112, 166), rgb(88, 152, 206), rgb(124, 188, 234), rgb(164, 214, 248), rgb(208, 236, 255), rgb(244, 252, 255)],
        eye=[rgb(120, 50, 14), rgb(176, 84, 22), rgb(220, 124, 36), rgb(244, 164, 64), rgb(255, 206, 120)],
        pupil=rgb(64, 26, 10)),
        ramp([rgb(28, 44, 78), rgb(42, 66, 112), rgb(62, 94, 148), rgb(88, 122, 178), rgb(122, 154, 204)])),
}


VARIANTS["papi"][0].papi = True                 # her shirt and shorts (see paint)


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide", "textures", "entity")
    texes = {"house": paint()}
    for v, (pal, leaf) in VARIANTS.items():
        texes[v] = paint(pal, leaf)
        H.write_png(os.path.join(tex_dir, f"{NAME}_{v}.png"), texes[v])
    print(f"{NAME}: {len(VARIANTS)} colour variants ({', '.join(VARIANTS)})")
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
    rows = [(g("idle"), 0, -20, 6, 10, None), (g("fly"), 0.4, -30, 10, 10, None), (g("idle"), 0, -15, 0, 30, (0, 22.5))]
    sheet = [[bg] * (W * len(texes)) for _ in range(W * len(rows))]
    for c, tex in enumerate(texes.values()):
        for r, (anim, tm, yaw, pitch, scale, center) in enumerate(rows):
            im = H.render(model, tex, anim, tm, yaw, pitch, W, W, scale, bg, center=center)
            for y in range(W):
                sheet[r * W + y][c * W:(c + 1) * W] = im[y]
    H.write_png(path, sheet)
    print("lineup ->", path, "(columns:", ", ".join(texes), ")")


# ----------------------------------------------------------------------------- geometry
def build_model():
    m = H.Model(NAME)
    bone = m.bone
    bone("root", None, [0, 0, 0])

    # ---- torso: slender waist, slight bust under a feathered bandeau, necklace
    body = [
        cube("hips", [-2.5, 12, -1.75]), cube("midriff", [-1.75, 13.25, -1.25]), cube("chest", [-2.5, 16, -1.625]),
        cube("collar", [-1.75, 19, -1.375]), cube("sash", [-2.75, 12.6, -2.0]), cube("back_cover", [-1.75, 15.5, 1.625]),
        cube("pendant", [-0.5, 17.15, -2.7]), cube("cord", [-1.5, 18.55, -2.45]),
    ]
    bust = cube("bust", [-2.25, 16.9, -2.375])
    body += [bust, mirror_cube(bust)]
    for i, x in enumerate((-2.5, -1.5, -0.5, 0.5, 1.5)):
        body.append(hang("trim", x, 16.1, -2.05, 6 if i % 2 else -6))
    bone("body", "root", [0, 12, 0], body)

    # ---- loincloth: ragged strips of different lengths, a few tilted outward
    sk = []
    front = [("leaf_m", -3.0, 5), ("leaf_l", -1.5, -3), ("leaf_s", -0.2, 2), ("leaf_l", 1.0, -2), ("leaf_m", 2.0, 6)]
    for i, (p, x, rz) in enumerate(front):
        sk.append(hang(p, x, 13.7, -2.7 - 0.15 * (i % 2), rz, -9))
    for i, (p, x, rz) in enumerate((("leaf_m", -3.0, 3), ("leaf_s", -1.0, -2), ("leaf_l", 0.8, 3), ("leaf_m", 2.0, -3))):
        sk.append(hang(p, x, 13.7, 2.2 + 0.15 * (i % 2), rz, 9))
    for z in (-2.2, 0.0):
        sk.append(cube("leaf_side", [-3.4, 8.2, z], pivot=[-3.15, 13.7, z + 1.25], rotation=[0, 0, -9]))
        sk.append(mirror_cube(sk[-1]))
    sk.append(hang("tassel", 1.8, 13.4, -3.2, 3, -4))
    sk.append(hang("tassel", -2.7, 13.4, -3.1, -3, -4))
    bone("skirt", "body", [0, 13.7, 0], sk)

    # ---- head: cap, swept bangs (kept clear of the eyes), crown tufts, ears, feather accessory
    head = [cube("head", [-4, 20, -4]), cube("hair_cap", [-4, 20, -4], inflate=0.75)]
    ear_r = cube("ear", [-5.0, 21.7, -1.2])
    head += [ear_r, mirror_cube(ear_r)]
    for i, (p, x, rz) in enumerate((("bang_e", -5.0, -6), ("bang_a", -3.1, 4), ("bang_a", -1.4, -3), ("bang_c", 0.2, 4),
                                    ("bang_a", 1.7, -4), ("bang_b", 3.2, 7))):
        head.append(hang(p, x, 28.4, -5.35 + 0.1 * (i % 2), rz))
    head.append(hang("frame", -4.9, 28, -5.0, -4))
    head.append(mirror_cube(head[-1]))
    for x, z in ((-2.4, -1.2), (0.4, -2.6), (2.6, -0.4), (-1.2, 1.6), (1.6, 2.4)):   # messy crown tufts, leaning outward
        head.append(cube("tuft", [x - 1, 28.2, z - 0.75], pivot=[x, 28.2, z], rotation=[-z * 7, 0, -x * 7]))
    for dx, dy, ang in ((0, 0, -26), (0.8, 0.9, -8), (1.6, 0.3, 12)):
        head.append(hang("hfeather", 4.6 + dx, 28.0 + dy, -2.8, ang))
    bone("head", "body", [0, 20, 0], head)

    # expression parts: inside the face at rest (face plane is z = -4), animated forward by POP
    bone("eyelids", "head", [0, 23.5, -3.8], [cube("eyelids", [-3.25, 21.75, -3.9])])
    bone("mouth", "head", [0, 21, -3.8], [cube("mouth", [-0.875, 20.25, -3.9])])

    side = [("strand_m1", -6.1, 26.2, -3.4, -6, 0), ("strand_s2", -6.3, 25.2, -2.0, -9, 0),
            ("strand_m2", -6.2, 26.2, 0.8, -7, 3), ("strand_m1", -6.0, 26.2, 2.4, -10, 4),
            ("strand_s1", -5.4, 24.6, 3.9, -13, 5)]
    bone("hair_side_r", "head", [-4.7, 27, 0], [hang(*s) for s in side], mirror=True)

    back = []
    names = ["strand_s1", "strand_m1", "strand_s2", "strand_m2", "strand_s1"]
    for i in range(11):
        x = -4.8 + 0.96 * i
        back.append(hang(names[i % 5], x, 26.8, 4.9 + 0.3 * (i % 3), x * 1.8, 5 + 2 * (i % 3)))
    bone("hair_back", "head", [0, 27, 4.5], back)

    bone("ahoge", "head", [0, 28.5, -1], [
        cube("ahoge", [-0.5, 28.5, -1.5]),
        cube("ahoge", [-0.35, 29.8, -1.5], pivot=[0, 29.8, -1], rotation=[0, 0, -20]),
        cube("ahoge_tip", [0.1, 31.0, -1.5], pivot=[0.5, 31.1, -1], rotation=[0, 0, -50]),
    ])

    # ---- arms (right side; left is mirrored)
    bone("arm_r", "body", [-3.25, 19.5, 0], [cube("arm_up", [-4.0, 16, -0.75])], mirror=True)
    bone("forearm_r", "arm_r", [-3.25, 16, 0], [
        cube("forearm", [-4.0, 13, -0.75]), cube("bracer", [-4.5, 13.6, -1.25]),
        hang("cuff_f", -4.15, 13.8, -1.45, 4, -6), hang("cuff_f", -3.25, 13.8, -1.45, -4, -6), hang("cuff_f", -3.65, 13.8, 1.0, 0, 6),
    ], mirror=True)
    bone("hand_r", "forearm_r", [-3.25, 13, 0], [
        cube("palm", [-4.0, 11.5, -0.5]), cube("fingers", [-4.0, 10.5, -0.5]), cube("thumb", [-2.5, 11.5, -0.25]),
    ], mirror=True)

    # ---- wings: scapular base + four rows of coverts + secondaries on the arm bone, primaries on the tip bone
    w = [cube("wing_base", [-7, 10, 2.3])]
    rows = [(19.5, (-7.0, -5.0), ("cov_a", "cov_a")), (16.8, (-8.5, -6.5, -4.5), ("cov_a", "cov_b", "cov_a")),
            (14.1, (-9.5, -7.5, -5.5), ("cov_b", "cov_b", "cov_b")), (11.4, (-8.5, -6.5, -4.5), ("cov_c", "cov_c", "cov_c"))]
    for r, (ytop, xs, ps) in enumerate(rows):
        for x, p in zip(xs, ps):
            w.append(cube(p, [x, ytop - 3, 3.3 + 0.2 * r]))
    for i, (x, ang) in enumerate(zip((-9.5, -8.25, -7, -5.75, -4.5), (-10, -7.5, -5, -2.5, 0))):
        w.append(hang("sec_a" if i % 2 == 0 else "sec_b", x, 12.5, 2.3 + 0.25 * (i % 2), ang))
    bone("wing_r", "body", [-2.5, 19, 2.3], w, mirror=True)
    pr = []
    for i, (x, ang) in enumerate(zip((-10.5, -9, -7.5, -6, -4.5), (-18, -14, -10, -6, -2))):
        pr.append(hang("prim_a" if i < 2 else "prim_b", x, 12.5, 2.8 + 0.25 * (i % 2), ang))
    bone("wing_tip_r", "wing_r", [-8, 12.5, 3.0], pr, mirror=True)

    # ---- tail
    tail = [cube("tail_fluff", [-1.75, 12.2, 2.2])]
    for i, (x, ang) in enumerate(zip((-3.5, -2.1, -0.75, 0.6, 2.0), (-16, -8, 0, 8, 16))):
        tail.append(hang("tail_f" if i % 2 == 0 else "tail_g", x, 12.7, 3.4 + 0.25 * (i % 2), ang, 6))
    bone("tail", "body", [0, 13.2, 3], tail)

    # ---- legs (right; left mirrored)
    cx = -1.25
    bone("leg_r", "root", [cx, 12, 0], [cube("thigh", [cx - 1, 9.5, -1]), cube("fluff", [cx - 1.25, 8.5, -1.25])], mirror=True)
    bone("shin_r", "leg_r", [cx, 9, 0], [cube("shin", [cx - .75, 2, -.75]), cube("ankle", [cx - 1, 1, -1])], mirror=True)
    foot = [cube("heel", [cx - 1, 0, -1])]
    for dx in (-2.0, -0.5, 1.0):
        x = cx + dx
        foot += [cube("toe1", [x, 0, -3.5]), cube("toe2", [x, 0, -5.5]),
                 cube("talon", [x, 0, -7.5], pivot=[x + .5, .5, -5.5], rotation=[22, 0, 0])]
    foot += [cube("htoe", [cx - .5, 0, 1]), cube("htalon", [cx - .5, 0, 2.5], pivot=[cx, .5, 2.5], rotation=[-22, 0, 0])]
    bone("foot_r", "shin_r", [cx, 1, 0], foot, mirror=True)
    return m


def animations(an):
    for f in (H.std_idle, H.std_walk, H.std_fly, H.std_sing, H.std_bathe):
        f(an)


if __name__ == "__main__":
    H.standard_parts()
    H.run(NAME, paint, build_model, animations, speak="sing")
    write_variants()
