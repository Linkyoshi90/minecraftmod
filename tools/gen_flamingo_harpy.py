#!/usr/bin/env python3
"""Flamingo Harpy - species definition. The shared engine lives in harpy_lib.py; the geometry is the toucan's
(gen_toucan_harpy.py) on long, thin legs.

    python tools/gen_flamingo_harpy.py                 # write the three asset files
    python tools/gen_flamingo_harpy.py --preview x.png # also render preview sheets
    python tools/gen_flamingo_harpy.py --lineup x.png  # also render all variants side by side

Variants are her three shades, from her diet (FlamingoHarpy): pink (the default, flamingo_harpy.png), pale and white.
Keep in sync with FlamingoHarpy.Variant.

Field guide: she stands on one leg in warm shallows with her flock, and her pink comes from the fish she eats.
Design: pink hair and feathers with black flight feathers, a pale pink bib, a pale beak with a bent black tip worn as
a headpiece over her brow, a skirt of pink feathers, and very long pink legs. Extra animations: "stand1" (on one leg)
and "march" (the flock's display: heads turning together, high steps).
"""
import copy
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

NAME = "flamingo_harpy"
L = 2.5                                          # long legs lift her higher than the sparrow

# ----------------------------------------------------------------------------- palette
PAL = H.Pal()
PAL.HAIR = ramp([rgb(170, 50, 80), rgb(206, 72, 104), rgb(232, 104, 132), rgb(244, 140, 164), rgb(250, 176, 194), rgb(255, 210, 222)])
PAL.SKIN = ramp([rgb(196, 150, 132), rgb(222, 182, 162), rgb(240, 210, 192), rgb(250, 230, 214)], nodither=True)
PAL.FEA = ramp([rgb(176, 60, 86), rgb(210, 84, 112), rgb(232, 112, 138), rgb(244, 146, 168), rgb(250, 180, 198), rgb(255, 210, 222), rgb(255, 234, 240)])
PAL.EYE = ramp([rgb(150, 110, 10), rgb(200, 160, 20), rgb(236, 200, 50), rgb(250, 230, 120), rgb(255, 246, 190)])
PAL.LEG = ramp([rgb(190, 80, 100), rgb(222, 110, 128), rgb(240, 144, 160), rgb(250, 180, 192), rgb(255, 210, 218)])
PAL.hair_gain, PAL.hair_off = 0.62, -0.02
PAL.TALON = [rgb(30, 30, 40), rgb(70, 70, 86)]
PAL.EYE_OUT, PAL.PUPIL = rgb(110, 70, 10), rgb(8, 10, 20)
PAL.BROW = PAL.HAIR[1]
PAL.eye_a, PAL.eye_b, PAL.eye_slant = 4.4, 5.2, 0.0           # round, cheerful eyes
PAL.pupil = (1.8, 2.8)
PAL.wing_base_t, PAL.tail_fluff_t, PAL.fluff_t = (0.05, 0.18), 0.30, 0.22

BIB = ramp([rgb(244, 190, 204), rgb(250, 212, 222), rgb(254, 230, 236), rgb(255, 244, 248)])
LEAF = ramp([rgb(200, 76, 104), rgb(230, 108, 134), rgb(246, 150, 170), rgb(252, 196, 210)])     # her skirt of pink feathers
BLACK = ramp([rgb(10, 10, 12), rgb(20, 20, 24), rgb(34, 34, 40), rgb(52, 52, 60), rgb(80, 80, 90), rgb(120, 120, 130), rgb(170, 170, 180)])
# the beak: (main colour, tip colour)
BEAK = (ramp([rgb(226, 196, 196), rgb(242, 218, 216), rgb(250, 234, 232), rgb(255, 246, 244)]), ramp([rgb(10, 10, 12), rgb(30, 30, 34), rgb(54, 54, 60)]))
RING = ramp([rgb(30, 130, 140), rgb(50, 170, 176), rgb(110, 210, 210)])

WINGS = {
    "sec_a": (1, 2, 3, 0), "sec_b": (0, 2, 3, 1), "prim_a": (0, 1, 2, 0), "prim_b": (1, 2, 3, 1),
    "tail_f": (1, 2, 3, 0), "tail_g": (0, 1, 3, 1),
    "cov_a": (0, 0, 1, None, True), "cov_b": (0, 1, 2, None, True), "cov_c": (1, 2, 3, None, True),
}


# ----------------------------------------------------------------------------- parts
def declare_parts():
    H.standard_parts()
    for args in [("beak_u", 2.5, 2, 5), ("beak_tip", 2.5, 2, 3), ("beak_lo", 2, 1, 4.5), ("fl_shin", 1, 10.5, 1)]:
        H.part(*args)


# ----------------------------------------------------------------------------- texture
def paint(pal=PAL, bib=BIB, beak=BEAK):
    pt = H.Painter()
    FEA, SKIN = pal.FEA, pal.SKIN
    D = H.D
    H.paint_head(pt, pal)
    H.paint_hair(pt, pal)
    H.paint_wings_tail(pt, pal, WINGS)
    black = copy.copy(pal)                                  # black flight feathers
    black.FEA = BLACK
    for p in ("prim_a", "prim_b"):
        pt(p, "all", H.feather_fn(black, *WINGS[p][:3], None, False, True))
    pt("fl_shin", "all", lambda f, x, y, w, h: pick(pal.LEG, 0.5 + 0.2 * (y % 8 == 0), x, y))
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
def variant_pal(hair, fea, eye=None, gain=0.62):
    p = copy.copy(PAL)
    p.HAIR, p.FEA = ramp([rgb(*c) for c in hair]), ramp([rgb(*c) for c in fea])
    if eye:
        p.EYE = ramp([rgb(*c) for c in eye])
    p.BROW = p.HAIR[1]
    p.hair_gain = gain
    return p


R = lambda *c: ramp([rgb(*x) for x in c])
VARIANTS = {
    # pale: a few days without fish
    "pale": (variant_pal([(214, 140, 154), (232, 164, 178), (244, 188, 200), (250, 208, 218), (254, 226, 232), (255, 240, 244)],
                         [(220, 150, 164), (236, 174, 186), (246, 196, 206), (252, 214, 222), (255, 230, 236), (255, 242, 246), (255, 250, 252)]),
             R((250, 222, 228), (253, 234, 238), (255, 244, 246), (255, 250, 252)), BEAK),
    # white: no fish at all for a long while
    "white": (variant_pal([(200, 196, 196), (220, 216, 216), (236, 232, 232), (246, 244, 244), (252, 250, 250), (255, 255, 255)],
                          [(206, 200, 200), (224, 218, 218), (238, 234, 234), (248, 246, 246), (252, 250, 250), (255, 254, 254), (255, 255, 255)]),
              R((240, 238, 238), (248, 246, 246), (252, 252, 252), (255, 255, 255)), BEAK),
}


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide", "textures", "entity")
    texes = {"pink": paint()}
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
    bone("beak", "head", [0, 27.6 + L, -4.2], [C("beak_u", [-1.25, 26.4, -9.0], pivot=[0, 27.6, -4.2], rotation=[-8, 0, 0]),
                                               C("beak_tip", [-1.25, 24.6, -10.4], pivot=[0, 26.6, -9.0], rotation=[45, 0, 0])])
    bone("beak_jaw", "beak", [0, 26.6 + L, -4.4], [C("beak_lo", [-1.0, 25.5, -8.6], pivot=[0, 26.6, -4.4], rotation=[-4, 0, 0])])

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

    # legs: long and pink, the feet webbed
    cx = -1.25
    bone("leg_r", "root", [cx, 12 + L, 0], [C("thigh", [cx - 1, 9.5, -1]), C("fluff", [cx - 1.25, 8.5, -1.25])], mirror=True)
    bone("shin_r", "leg_r", [cx, 9 + L, 0], [cube("fl_shin", [cx - .5, 1, -.5]), cube("ankle", [cx - 1, 1, -1])], mirror=True)
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

    # on one leg: the other tucked up under her, head sunk into her shoulders
    an.add("stand1", 4.0, True, merge({
        "leg_l": {"rotation": hold(-70, 0, -6)}, "shin_l": {"rotation": hold(130, 0, 0)},
        "head": {"rotation": hold(12, 0, 0)}, "eyelids": {"position": T((0, H.REST), (1.5, H.OUT_), (3.5, H.OUT_), (4.0, H.REST))},
    }))
    # the flock's march: high steps, heads flicking side to side together, wings half open
    an.add("march", 1.0, True, merge({
        "leg_r": {"rotation": T((0, (-50, 0, 0)), (0.5, (10, 0, 0)), (1.0, (-50, 0, 0)))},
        "leg_l": {"rotation": T((0, (10, 0, 0)), (0.5, (-50, 0, 0)), (1.0, (10, 0, 0)))},
        "head": {"rotation": T((0, (0, -35, 0)), (0.25, (0, -35, 0)), (0.3, (0, 35, 0)), (0.75, (0, 35, 0)), (0.8, (0, -35, 0)), (1.0, (0, -35, 0)))},
    }, sym("wing", hold(0, 0, -25))))


if __name__ == "__main__":
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="sing", head_center=(0, 23.5 + L), body_center=(0, 15.5 + L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("stand1", 2.0, 0, 0), ("idle", 0, -40, 8)])
    write_variants()
