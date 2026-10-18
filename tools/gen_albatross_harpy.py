#!/usr/bin/env python3
"""Albatross Harpy - species definition. The shared engine lives in harpy_lib.py.

    python tools/gen_albatross_harpy.py                 # write the three asset files
    python tools/gen_albatross_harpy.py --preview x.png # also render preview sheets (x.png, x_face.png, x_body.png)

Field guide: "Large flier that needs a big clearing to land, so it avoids hilly terrain. Skittish: vanishes when
approached. Likes to swoop down and steal food items out of the player inventory." Independent, cautious.
Design: tall; white hair and plumage, very long narrow wings that are sooty brown on top, a soft grey smudge
over the eyes, dark eyes; weathered sailcloth wrap with rope lashings and a sea-glass pendant; pale pink legs
with webbed feet. A dedicated "soar" animation holds the long wings out stiffly.
"""
import copy
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import variant_tools as VT
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

NAME = "albatross_harpy"
L = 2.0                                          # tall: everything above the shins sits this much higher
FOLD = 165                                       # degrees the wing tips fold back (around Y) when she is not flying

# ----------------------------------------------------------------------------- palette
PAL = H.Pal()
PAL.HAIR = ramp([rgb(150, 150, 150), rgb(186, 186, 184), rgb(214, 214, 210), rgb(234, 234, 230), rgb(246, 246, 242), rgb(254, 254, 252)])
PAL.FEA = ramp([rgb(30, 28, 26), rgb(56, 52, 48), rgb(88, 82, 76), rgb(130, 124, 116), rgb(180, 176, 170), rgb(228, 226, 222), rgb(252, 252, 250)])
PAL.EYE = ramp([rgb(36, 26, 20), rgb(62, 44, 32), rgb(94, 68, 48), rgb(130, 98, 70), rgb(170, 136, 104)])
PAL.LEG = ramp([rgb(170, 130, 128), rgb(200, 162, 158), rgb(222, 190, 184), rgb(238, 212, 206), rgb(248, 232, 228)])
PAL.TALON = [rgb(110, 96, 96), rgb(150, 136, 134)]
PAL.EYE_OUT, PAL.PUPIL = rgb(26, 20, 16), rgb(10, 8, 6)
PAL.BROW = rgb(120, 120, 124)                                # the soft grey "eyebrow" smudge albatrosses have
PAL.brows = [(8, 9, -5, 4, 0.0, 2), (24, 9, -5, 4, 0.0, 2)]
PAL.wing_base_t, PAL.tail_fluff_t, PAL.fluff_t = (0.10, 0.25), 0.92, 0.92

CANVAS = ramp([rgb(120, 110, 88), rgb(160, 150, 124), rgb(196, 186, 160), rgb(222, 214, 192), rgb(240, 234, 216)])
GLASS = ramp([rgb(40, 110, 110), rgb(80, 170, 160), rgb(170, 230, 214)])
ROPE = ramp([rgb(110, 86, 52), rgb(156, 126, 82), rgb(196, 168, 120)])

# ----------------------------------------------------------------------------- species variants (texture only)
# Wing patterns are feather_fn specs per part (indices into the variant's feather ramp). The default is the
# black-browed albatross. Keep in sync with AlbatrossHarpy.Variant.
WINGS = {
    "sec_a": (2, 1, 1, None), "sec_b": (2, 2, 1, None), "prim_a": (1, 1, 0, None), "prim_b": (2, 1, 0, None),
    "tail_f": (6, 5, 2, None, True), "tail_g": (6, 5, 1, None, True),
    "cov_a": (2, 2, 3, None, True), "cov_b": (2, 3, 2, None, True), "cov_c": (1, 2, 2, None, True),
}
DEFAULT = SimpleNamespace(pal=PAL, wings=WINGS, canvas=CANVAS, glass=GLASS)


def avariant(hair, fea, wings, brow=None, eye=None, leg=None, canvas=None, glass=None):
    rg = lambda colors: ramp([rgb(*c) for c in colors])
    p = copy.copy(PAL)
    p.HAIR, p.FEA = rg(hair), rg(fea)
    if brow:
        p.BROW = rgb(*brow)
    if eye:
        p.EYE = rg(eye)
    if leg:
        p.LEG = rg(leg)
    return SimpleNamespace(pal=p, wings=dict(WINGS, **wings), canvas=rg(canvas) if canvas else CANVAS, glass=rg(glass) if glass else GLASS)


WHITE_FEA = [(36, 34, 32), (70, 66, 62), (110, 106, 100), (160, 156, 150), (206, 204, 198), (238, 236, 232), (254, 254, 252)]
PINK_LEG = [(190, 120, 124), (214, 150, 150), (230, 178, 176), (242, 206, 202), (250, 230, 226)]
VARIANTS = {
    # wandering albatross: almost all white, with black wing tips and black-edged flight feathers; pink legs
    "wandering": avariant([(176, 176, 178), (206, 206, 206), (226, 226, 224), (240, 240, 238), (250, 250, 248), (255, 255, 254)],
                          WHITE_FEA,
                          dict(sec_a=(5, 5, 4, None), sec_b=(5, 4, 4, None), prim_a=(5, 4, 0, None), prim_b=(5, 5, 0, None),
                               cov_a=(5, 5, 6, None, True), cov_b=(5, 6, 5, None, True), cov_c=(6, 5, 5, None, True)),
                          brow=(176, 176, 180), leg=PINK_LEG),
    # laysan albatross: white head and body, near-black back and upper wings, a dusky smudge round the eyes
    "laysan": avariant([(150, 150, 150), (186, 186, 184), (214, 214, 210), (234, 234, 230), (246, 246, 242), (254, 254, 252)],
                       [(16, 14, 14), (34, 30, 28), (54, 48, 44), (88, 80, 74), (150, 142, 134), (216, 212, 206), (250, 250, 248)],
                       dict(sec_a=(1, 1, 0, None), sec_b=(1, 0, 0, None), prim_a=(0, 0, 0, None), prim_b=(1, 0, 0, None),
                            cov_a=(1, 1, 2, None, True), cov_b=(1, 2, 1, None, True), cov_c=(0, 1, 1, None, True)),
                       brow=(70, 62, 62), leg=PINK_LEG),
    # sooty albatross: chocolate-brown everywhere, a paler grey-brown mantle, dark eyes; a dark-blue canvas wrap
    "sooty": avariant([(40, 30, 26), (62, 48, 40), (86, 68, 58), (114, 94, 80), (146, 124, 108), (180, 160, 142)],
                      [(24, 18, 16), (44, 34, 30), (66, 52, 46), (92, 76, 68), (120, 102, 92), (160, 142, 130), (206, 192, 178)],
                      dict(sec_a=(2, 1, 1, None), sec_b=(2, 2, 1, None), prim_a=(1, 1, 0, None), prim_b=(2, 1, 0, None),
                           tail_f=(2, 1, 0, None, True), tail_g=(1, 1, 0, None, True),
                           cov_a=(3, 3, 4, None, True), cov_b=(3, 4, 3, None, True), cov_c=(2, 3, 3, None, True)),
                      brow=(90, 80, 74), leg=[(70, 56, 56), (100, 82, 80), (130, 110, 106), (160, 140, 134), (190, 170, 162)],
                      canvas=[(36, 44, 62), (60, 72, 96), (90, 104, 130), (126, 140, 164), (160, 174, 196)]),
    # short-tailed albatross: golden-cream head, white body, black wings with white shoulder patches, pink legs
    "short_tailed": avariant([(150, 120, 60), (190, 160, 90), (222, 196, 120), (240, 222, 156), (250, 238, 190), (255, 248, 220)],
                             WHITE_FEA,
                             dict(sec_a=(1, 0, 0, None), sec_b=(1, 1, 0, None), prim_a=(0, 0, 0, None), prim_b=(1, 0, 0, None),
                                  cov_a=(5, 5, 6, None, True), cov_b=(5, 4, 1, None, True), cov_c=(1, 1, 0, None, True)),
                             brow=(190, 160, 100), leg=PINK_LEG,
                             canvas=[(140, 110, 56), (180, 150, 86), (216, 188, 122), (238, 214, 156), (250, 236, 196)]),
    # grey-headed albatross: slate-grey head, charcoal wings, a pale-grey smudge, golden-yellow canvas and amber glass
    "grey_headed": avariant([(70, 76, 84), (104, 112, 122), (138, 146, 156), (170, 178, 188), (204, 210, 218), (234, 238, 242)],
                            [(22, 24, 30), (44, 48, 56), (72, 78, 88), (108, 114, 124), (160, 166, 176), (222, 226, 232), (252, 252, 254)],
                            dict(cov_a=(2, 2, 3, None, True), cov_b=(2, 3, 2, None, True)),
                            brow=(150, 156, 164), canvas=[(150, 118, 40), (192, 156, 66), (222, 190, 102), (240, 218, 144), (250, 238, 190)],
                            glass=[(150, 100, 20), (214, 160, 40), (250, 214, 110)]),
}


# ----------------------------------------------------------------------------- parts
def declare_parts():
    H.standard_parts()
    for args in [("knot", 1, 1, .5), ("prim_a", 1.5, 13, .5), ("prim_b", 1.5, 13, .5), ("sec_a", 1.5, 10, .5),
                 ("sec_b", 1.5, 10, .5), ("cov_a", 2.5, 3, .5), ("cov_b", 2.5, 3, .5), ("cov_c", 2.5, 3, .5),
                 ("shin", 1.5, 7, 1.5)]:
        H.part(*args)


# ----------------------------------------------------------------------------- texture
def paint(v=None):
    v = v or DEFAULT
    pt = H.Painter()
    pal, FEA, SKIN = v.pal, v.pal.FEA, v.pal.SKIN
    CANVAS, GLASS = v.canvas, v.glass                      # shadow the module-level defaults
    D = H.D
    H.paint_head(pt, pal)
    H.paint_hair(pt, pal)
    # upper wing per species (the default: sooty brown, darker toward the tips); the tail is white with a dark edge
    H.paint_wings_tail(pt, pal, v.wings)
    H.paint_body_skin(pt, pal)
    H.paint_legs(pt, pal)
    for p, spec in (("tassel", (5, 5, 4)), ("cuff_f", (5, 5, 6)), ("trim", (5, 5, 6)), ("hfeather", (6, 5, 2))):
        pt(p, "all", H.feather_fn(pal, *spec, None, False, False))
    pt("back_cover", "all", lambda f, x, y, w, h: pick(FEA, 0.72 + 0.2 * y / h + (noise(x, y, 19) - 0.5) * 0.08, x, y))

    # weathered sailcloth wrap with darned patches and a rope lashing across it
    def sailcloth(f, x, y, w, h):
        if f == "up":
            return pick(SKIN, 0.66, x, y)
        if f == "down":
            return pick(SKIN, 0.5, x, y)
        if y < D // 2:
            return pick(CANVAS, 0.75, x, y)
        if y >= h - D // 2:
            return ROPE[1] if (x + y) % 3 else ROPE[2]
        t = 0.62 + (noise(x, y, 31) - 0.5) * 0.18 - (0.10 if f in ("east", "west", "south") else 0)
        if noise(x // 3, y // 3, 33) > 0.84:
            t -= 0.22                                                     # patches
        if (x + y) % 9 == 0:
            t -= 0.08                                                     # weave
        return pick(CANVAS, t, x, y)
    pt("chest", "all", sailcloth)
    pt("bust", "all", sailcloth)
    pt("knot", "all", lambda f, x, y, w, h: pick(ROPE, 0.4 + 0.4 * (x == 0), x, y))

    def belt(f, x, y, w, h):
        if y < 2 or y >= h - 2:
            return ROPE[0]
        return pick(ROPE, 0.5 + 0.25 * (((x + y) % 3) == 0) - 0.15 * (((x - y) % 3) == 0), x, y)   # twisted rope
    pt("sash", "all", belt)
    pt("pendant", "all", lambda f, x, y, w, h: pick(GLASS, 0.4 + 0.5 * (x < w // 2) + 0.2 * (y == 0), x, y))
    pt("cord", "all", lambda f, x, y, w, h: ROPE[1])

    def cloth(broad):
        def fn(f, x, y, w, h):
            if f not in broad:
                return CANVAS[1]
            r = h - 1 - y
            tl = 1.5 * D
            if r < tl:
                half = (w / 2.0) * (min(1.0, (r + 1) / tl) ** 0.6)
                if abs(x + 0.5 - w / 2.0) > half:
                    return CLEAR
            if y < D:
                return ROPE[1] if (x + y) % 3 else ROPE[2]
            return pick(CANVAS, 0.5 + (noise(x, y, 43) - 0.5) * 0.24 - (0.16 if x % 5 == 0 else 0), x, y)
        return fn
    for p in ("leaf_s", "leaf_m", "leaf_l"):
        pt(p, "all", cloth(BROAD))
    pt("leaf_side", "all", cloth(("east", "west")))
    pt("bracer", "all", lambda f, x, y, w, h: ROPE[2] if (x + y) % 3 == 0 else pick(ROPE, 0.4, x, y))
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
        C("knot", [-0.5, 18.6, -2.2]), C("pendant", [-0.5, 17.3, -2.6]), C("cord", [-1.5, 18.6, -2.45]),
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

    # ---- wings: very long and narrow; the arm part carries a long row of secondaries, the tip a long hand
    w = [C("wing_base", [-7, 10, 2.3])]
    rows = [(19.5, (-7.5, -5.0), ("cov_a", "cov_a")), (16.8, (-10.0, -7.5, -5.0), ("cov_a", "cov_b", "cov_a")),
            (14.1, (-12.5, -10.0, -7.5, -5.0), ("cov_b", "cov_b", "cov_c", "cov_b")),
            (11.4, (-12.5, -10.0, -7.5, -5.0), ("cov_c", "cov_c", "cov_c", "cov_c"))]
    for r, (ytop, xs, ps) in enumerate(rows):
        for x, p in zip(xs, ps):
            w.append(C(p, [x, ytop - 3, 3.3 + 0.2 * r]))
    for i, x in enumerate((-13.0, -11.75, -10.5, -9.25, -8.0, -6.75, -5.5, -4.25)):
        w.append(HG("sec_a" if i % 2 == 0 else "sec_b", x, 12.5, 2.3 + 0.25 * (i % 2), -i * 1.2))
    bone("wing_r", "body", [-2.5, 19 + L, 2.3], w, mirror=True)
    pr = []
    for i, (x, ang) in enumerate(zip((-17.5, -16.0, -14.5, -13.0, -11.5), (-18, -14, -10, -6, -3))):
        pr.append(HG("prim_a" if i % 2 == 0 else "prim_b", x, 12.5, 2.8 + 0.25 * (i % 2), ang))
    bone("wing_tip_r", "wing_r", [-12, 12.5 + L, 3.0], pr, mirror=True)

    # ---- tail: short and wedge-shaped
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
    for f in (H.std_idle, H.std_walk, H.std_fly, H.std_bathe):
        f(an)
    # On the ground the long hand of the wing folds back along the body instead of hanging out like a cape.
    for key in ("idle", "walk", "bathe"):
        bones = an.A[f"animation.{NAME}.{key}"]["bones"]
        for side, sign in (("r", 1), ("l", -1)):
            bones[f"wing_tip_{side}"] = {"rotation": {"0.0": [0, sign * FOLD, 0]}}
            bones[f"wing_{side}"] = {"rotation": {"0.0": [0, sign * 12, sign * 4]}}
    OUT, REST = H.OUT_, H.REST
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold

    # soar: long wings held out stiff and nearly level, banking gently; legs tucked, body almost horizontal
    bank = T((0, (0, 0, -4)), (2, (0, 0, 4)), (4, (0, 0, -4)))
    an.add("soar", 4.0, True, merge({
        "body": {"rotation": T((0, (30, 0, -4)), (2, (30, 0, 4)), (4, (30, 0, -4))),
                 "position": T((0, (0, 0, 0)), (2, (0, 0.4, 0)), (4, (0, 0, 0)))},
        "head": {"rotation": T((0, (-26, 6, 0)), (2, (-26, -6, 0)), (4, (-26, 6, 0)))},
        "leg_r": {"rotation": hold(55, 0, 0)}, "leg_l": {"rotation": hold(55, 0, 0)},
        "shin_r": {"rotation": hold(35, 0, 0)}, "shin_l": {"rotation": hold(35, 0, 0)},
        "foot_r": {"rotation": hold(-30, 0, 0)}, "foot_l": {"rotation": hold(-30, 0, 0)},
        "hair_back": {"rotation": hold(38, 0, 0)},
        "skirt": {"rotation": hold(-16, 0, 0)},
        "tail": {"rotation": bank},
    },
        sym("hair_side", hold(32, 0, -8)),
        sym("wing", T((0, (0, 0, -92)), (1, (0, 0, -88)), (2, (0, 0, -92)), (3, (0, 0, -88)), (4, (0, 0, -92)))),
        sym("wing_tip", T((0, (0, 0, 4)), (2, (0, 0, -2)), (4, (0, 0, 4)))),
        sym("arm", hold(35, 0, 0)), sym("forearm", hold(25, 0, 0)), sym("hand", hold(15, 0, 0))))

    # dance: the albatross courtship display (to a jukebox's music) - sky-pointing with the wings half spread,
    # a deep bow, head-fencing side to side while clapping her hands like a bill-clap, then a little hop
    an.add("dance", 3.2, True, merge({
        "body": {"rotation": T((0, (0, 0, 0)), (0.3, (-8, 0, 0)), (0.8, (-8, 0, 0)), (1.1, (28, 0, 0)), (1.5, (28, 0, 0)),
                               (1.8, (4, 0, 0)), (2.4, (4, 0, 0)), (2.7, (0, 0, 0)), (3.2, (0, 0, 0))),
                 "position": T((0, (0, 0, 0)), (2.5, (0, 0, 0)), (2.65, (0, 1.2, 0)), (2.8, (0, 0, 0)), (3.2, (0, 0, 0)))},
        "head": {"rotation": T((0, (0, 0, 0)), (0.3, (-42, 0, 0)), (0.8, (-42, 0, 0)), (1.1, (26, 0, 0)), (1.5, (26, 0, 0)),
                               (1.7, (0, 24, 6)), (1.9, (0, -24, -6)), (2.1, (0, 24, 6)), (2.3, (0, -24, -6)), (2.5, (0, 0, 0)), (3.2, (0, 0, 0)))},
        "tail": {"rotation": T((0, (0, 0, 0)), (0.3, (-18, 0, 0)), (0.8, (-18, 0, 0)), (1.1, (10, 0, 0)), (2.4, (10, 0, 0)), (3.2, (0, 0, 0)))},
        "hair_back": {"rotation": T((0, (0, 0, 0)), (0.3, (-10, 0, 0)), (0.8, (-10, 0, 0)), (1.1, (6, 0, 0)), (1.5, (6, 0, 0)), (3.2, (0, 0, 0)))},
        "arm_r": {"rotation": T((0, (0, 0, 0)), (1.6, (0, 0, 0)), (1.7, (-60, 0, -18)), (1.8, (-60, 0, 6)), (1.9, (-60, 0, -18)), (2.0, (-60, 0, 6)),
                                (2.1, (-60, 0, -18)), (2.2, (-60, 0, 6)), (2.4, (0, 0, 0)), (3.2, (0, 0, 0)))},
        "arm_l": {"rotation": T((0, (0, 0, 0)), (1.6, (0, 0, 0)), (1.7, (-60, 0, 18)), (1.8, (-60, 0, -6)), (1.9, (-60, 0, 18)), (2.0, (-60, 0, -6)),
                                (2.1, (-60, 0, 18)), (2.2, (-60, 0, -6)), (2.4, (0, 0, 0)), (3.2, (0, 0, 0)))},
        "forearm_r": {"rotation": T((0, (0, 0, 0)), (1.6, (0, 0, 0)), (1.7, (-30, 0, 0)), (2.2, (-30, 0, 0)), (2.4, (0, 0, 0)), (3.2, (0, 0, 0)))},
        "forearm_l": {"rotation": T((0, (0, 0, 0)), (1.6, (0, 0, 0)), (1.7, (-30, 0, 0)), (2.2, (-30, 0, 0)), (2.4, (0, 0, 0)), (3.2, (0, 0, 0)))},
        "leg_r": {"rotation": T((0, (0, 0, 0)), (2.5, (0, 0, 0)), (2.65, (-24, 0, 0)), (2.8, (0, 0, 0)), (3.2, (0, 0, 0)))},
        "leg_l": {"rotation": T((0, (0, 0, 0)), (2.5, (0, 0, 0)), (2.65, (-24, 0, 0)), (2.8, (0, 0, 0)), (3.2, (0, 0, 0)))},
        "shin_r": {"rotation": T((0, (0, 0, 0)), (2.5, (0, 0, 0)), (2.65, (40, 0, 0)), (2.8, (0, 0, 0)), (3.2, (0, 0, 0)))},
        "shin_l": {"rotation": T((0, (0, 0, 0)), (2.5, (0, 0, 0)), (2.65, (40, 0, 0)), (2.8, (0, 0, 0)), (3.2, (0, 0, 0)))},
        "foot_r": {"rotation": T((0, (0, 0, 0)), (2.5, (0, 0, 0)), (2.65, (-16, 0, 0)), (2.8, (0, 0, 0)), (3.2, (0, 0, 0)))},
        "foot_l": {"rotation": T((0, (0, 0, 0)), (2.5, (0, 0, 0)), (2.65, (-16, 0, 0)), (2.8, (0, 0, 0)), (3.2, (0, 0, 0)))},
    },
        # wings: folded, half spread and raised for the sky-point, swept back for the bow, folded again
        sym("wing", T((0, (0, 12, 4)), (0.3, (0, 0, -55)), (0.8, (0, 0, -55)), (1.1, (0, 22, 10)), (1.5, (0, 22, 10)),
                      (1.8, (0, 12, 4)), (2.6, (0, 6, -14)), (2.8, (0, 12, 4)), (3.2, (0, 12, 4)))),
        sym("wing_tip", T((0, (0, FOLD, 0)), (0.3, (0, 10, 0)), (0.8, (0, 10, 0)), (1.1, (0, FOLD, 0)), (3.2, (0, FOLD, 0))))))


if __name__ == "__main__":
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="idle", head_center=(0, 22.5 + L), body_center=(0, 15.5 + L), scale=11,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("soar", 0.0, -20, 25), ("soar", 1.0, 150, 20), ("walk", 0.25, -35, 12), ("fly", 0.0, -20, 10)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("idle", 0, -30, 8)])
    VT.write_variants(NAME, "black_browed", paint, VARIANTS, build_model, animations, width=420,
                      rows=[("idle", 0, -20, 6, 9, None), ("soar", 0.0, -20, 25, 9, None), ("idle", 0, -15, 0, 28, (0, 22.5 + L))])
