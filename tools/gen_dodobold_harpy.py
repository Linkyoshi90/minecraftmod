#!/usr/bin/env python3
"""Dodobold Harpy - species definition (HARMONTIDE_CONCEPTS.md, twelfth wave). The shared engine lives in harpy_lib.py;
the rig is the penguin's (gen_penguin_harpy.py: the seagull's upper body on short legs), made small and fat like a dodo.

    python tools/gen_dodobold_harpy.py                 # write the three asset files
    python tools/gen_dodobold_harpy.py --preview x.png # also render preview sheets

Variants (VARIANTS below) only get their own texture, dodobold_harpy_<variant>.png; the default (blue) stays
dodobold_harpy.png. Keep in sync with DodoboldHarpy.Variant.

Field guide: a flightless Harmontide dodo, round and stubby, in an ornate harness with two saddlebags: a friend can
give her things to carry. Design: a plump ball of blue-grey feathers round her middle, stubby little wings, a curly
tail plume, thick yellow legs with claws, a gilded harness. Animations: the penguin's "waddle" for walking.
"""
import copy
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

NAME = "dodobold_harpy"
L = -3.5                                         # stubby legs: everything above the shins sits lower still than the penguin

# ----------------------------------------------------------------------------- palette
R = lambda *c: ramp([rgb(*x) for x in c])
PAL = H.Pal()
PAL.HAIR = R((16, 22, 44), (28, 40, 74), (44, 62, 108), (66, 90, 146), (98, 126, 184), (140, 166, 214))
PAL.SKIN = ramp([rgb(200, 156, 140), rgb(226, 188, 170), rgb(242, 214, 198), rgb(252, 234, 220)], nodither=True)
PAL.FEA = R((14, 20, 44), (26, 38, 78), (42, 60, 116), (62, 88, 156), (92, 122, 192), (136, 164, 220), (190, 206, 238))
PAL.EYE = R((20, 40, 80), (34, 70, 130), (60, 110, 180), (110, 160, 220), (180, 210, 245))
PAL.LEG = R((150, 110, 20), (196, 150, 40), (226, 186, 70), (244, 214, 120), (252, 234, 176))
PAL.hair_gain, PAL.hair_off = 0.62, -0.02
PAL.TALON = [rgb(50, 40, 30), rgb(90, 76, 62)]
PAL.EYE_OUT, PAL.PUPIL = rgb(20, 16, 30), rgb(8, 6, 12)
PAL.BROW = PAL.HAIR[1]
PAL.eye_a, PAL.eye_b, PAL.eye_slant = 4.4, 5.2, 0.0
PAL.pupil = (2.0, 3.0)                                       # big, round, earnest eyes
PAL.wing_base_t, PAL.tail_fluff_t, PAL.fluff_t = (0.05, 0.18), 0.40, 0.35

CREAM = R((214, 204, 180), (232, 224, 204), (244, 238, 222), (252, 250, 240))
GOLD = R((160, 110, 20), (210, 150, 40), (240, 196, 80), (255, 230, 150))
LEATHER = R((60, 36, 20), (90, 56, 30), (124, 80, 44), (160, 110, 64))


# ----------------------------------------------------------------------------- parts
def declare_parts():
    H.standard_parts()
    for args in [("shin", 2.25, 7 + L, 2.25), ("flipper", 2.5, 4.5, .75), ("flipper_tip", 2, 2, .75), ("plume", 2, 7, .75),
                 ("plump", 7, 5.5, 6), ("plump_b", 8, 3.5, 7), ("bag", 2.25, 3, 2.75), ("harness", 7.2, .75, .25), ("buckle", 1.25, 1.25, .25)]:
        H.part(*args)


# ----------------------------------------------------------------------------- texture
def paint(pal=PAL, front=CREAM):
    pt = H.Painter()
    FEA, SKIN = pal.FEA, pal.SKIN
    H.paint_head(pt, pal)
    H.paint_hair(pt, pal)
    H.paint_body_skin(pt, pal)
    H.paint_legs(pt, pal)
    for p, spec in (("tassel", (1, 2, 4)), ("cuff_f", (1, 2, 3)), ("trim", (0, 1, 3)), ("hfeather", (0, 2, 4))):
        pt(p, "all", H.feather_fn(pal, *spec, None, False, False))
    pt("back_cover", "all", lambda f, x, y, w, h: pick(FEA, 0.3 + 0.15 * y / h + (noise(x, y, 19) - 0.5) * 0.08, x, y))

    def scallop(f, x, y, w, h):                           # rows of round feathers, darker at each feather's edge
        edge = (y % 3 == 2) or ((x + (y // 3) * 2) % 4 == 0 and y % 3 == 1)
        if f == "north" and abs(x + 0.5 - w / 2) < w * 0.28:
            return pick(front, 0.55 - 0.2 * edge + (noise(x, y, 33) - 0.5) * 0.1, x, y)
        return pick(FEA, 0.42 - 0.18 * edge + (noise(x, y, 31) - 0.5) * 0.1, x, y)
    pt("plump", "all", scallop)
    pt("plump_b", "all", scallop)
    stub = H.feather_fn(pal, 2, 3, 4, None, True)
    pt("flipper", "all", stub)
    pt("flipper_tip", "all", stub)
    pt("wing_base", "all", lambda f, x, y, w, h: pick(FEA, 0.35, x, y))
    pt("tail_fluff", "all", lambda f, x, y, w, h: pick(FEA, pal.tail_fluff_t + (noise(x, y, 23) - 0.5) * 0.3, x, y))
    pt("plume", "all", lambda f, x, y, w, h: pick(front if y < 2 else FEA, 0.5 + 0.2 * (x % 2) + (noise(x, y, 77) - 0.5) * 0.1, x, y))

    def top(f, x, y, w, h):                               # a feathered top: cream in front, her colour at the sides and back
        if f == "up":
            return pick(SKIN, 0.66, x, y)
        if f in ("south", "east", "west") or (f == "north" and (x < 1 or x >= w - 1)):
            return pick(FEA, 0.38 + (noise(x, y, 31) - 0.5) * 0.08, x, y)
        return pick(front, 0.6 + (noise(x, y, 32) - 0.5) * 0.1, x, y)
    pt("chest", "all", top)
    pt("bust", "all", top)
    pt("collar", "all", lambda f, x, y, w, h: pick(GOLD, 0.5 + 0.2 * (x % 3 == 0), x, y))
    pt("sash", "all", lambda f, x, y, w, h: pick(GOLD, 0.5 + 0.2 * (y == 0), x, y))
    pt("pendant", "all", lambda f, x, y, w, h: pick(GOLD, 0.75, x, y))
    pt("harness", "all", lambda f, x, y, w, h: pick(GOLD, 0.45 + 0.3 * (x % 4 == 0), x, y))
    pt("buckle", "all", lambda f, x, y, w, h: pick(GOLD, 0.8 - 0.3 * (0 < x < w - 1 and 0 < y < h - 1), x, y))
    pt("bag", "all", lambda f, x, y, w, h: pick(GOLD, 0.7, x, y) if (f in BROAD and y == 1 and x % 3 == 1)
       else pick(LEATHER, 0.45 + 0.2 * (y == 0) + (noise(x, y, 81) - 0.5) * 0.1, x, y))
    pt("bracer", "all", lambda f, x, y, w, h: pick(GOLD, 0.5 - 0.2 * (y in (0, h - 1)), x, y))
    return pt.T


# ----------------------------------------------------------------------------- variants
def variant_pal(fea, hair, eye=None, leg=None):
    p = copy.copy(PAL)
    p.FEA, p.HAIR = R(*fea), R(*hair)
    p.BROW = p.HAIR[1]
    if eye:
        p.EYE = R(*eye)
    if leg:
        p.LEG = R(*leg)
    return p


VARIANTS = {
    # grey: the classic dodo, grey with a pale front
    "grey": (variant_pal(((30, 30, 34), (52, 52, 58), (80, 80, 88), (112, 112, 120), (146, 146, 154), (184, 184, 190), (220, 220, 224)),
                         ((30, 30, 34), (52, 52, 58), (80, 80, 88), (112, 112, 120), (150, 150, 158), (190, 190, 196))), CREAM),
    # brown: warm brown, a tawny front
    "brown": (variant_pal(((40, 24, 12), (68, 42, 22), (100, 66, 36), (136, 94, 56), (172, 128, 84), (206, 168, 124), (232, 208, 176)),
                          ((40, 24, 12), (68, 42, 22), (100, 66, 36), (136, 94, 56), (172, 128, 84), (206, 168, 124))),
              R((200, 170, 120), (222, 196, 150), (238, 218, 180), (250, 238, 212))),
    # white: the white solitaire, all pale, a golden front
    "white": (variant_pal(((150, 146, 140), (180, 176, 170), (206, 202, 196), (226, 222, 216), (240, 238, 234), (250, 248, 246), (255, 255, 255)),
                          ((170, 166, 160), (196, 192, 186), (216, 212, 206), (234, 230, 226), (246, 244, 240), (255, 255, 255)),
                          ((90, 30, 30), (140, 50, 50), (190, 80, 80), (230, 130, 130), (250, 190, 190))),
              R((210, 170, 80), (230, 196, 110), (244, 220, 150), (252, 238, 196))),
    # teal: sea-green feathers
    "teal": (variant_pal(((10, 34, 34), (18, 58, 58), (30, 90, 88), (48, 124, 120), (80, 160, 152), (130, 196, 186), (196, 232, 224)),
                         ((10, 34, 34), (18, 58, 58), (30, 90, 88), (48, 124, 120), (80, 160, 152), (130, 196, 186))), CREAM),
}


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "monstergirls", "textures", "entity")
    for v, (pal, front) in VARIANTS.items():
        H.write_png(os.path.join(tex_dir, f"{NAME}_{v}.png"), paint(pal, front))
    print(f"{NAME}: {len(VARIANTS)} variants ({', '.join(VARIANTS)})")


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
        C("collar", [-1.75, 19, -1.375]), C("back_cover", [-1.75, 15.5, 1.625]),
        C("plump", [-3.5, 10.4, -3.0]), C("plump_b", [-4.0, 11.4, -3.5]),   # the round, fat body of feathers
        C("harness", [-3.6, 14.4, -3.15]), C("buckle", [-0.625, 14.15, -3.3]),
    ]
    bust = C("bust", [-2.25, 16.9, -2.375])
    body += [bust, mirror_cube(bust)]
    bone("body", "root", [0, 12 + L, 0], body)
    bag = C("bag", [-5.6, 10.8, -1.4])                                     # her two saddlebags, one at each hip
    bone("pack", "body", [0, 13 + L, 0], [bag, mirror_cube(bag)])

    head = [C("head", [-4, 20, -4]), C("hair_cap", [-4, 20, -4], inflate=0.75)]
    ear_r = C("ear", [-5.0, 21.7, -1.2])
    head += [ear_r, mirror_cube(ear_r)]
    for i, (p, x, rz) in enumerate((("bang_b", -5.0, -8), ("bang_a", -3.1, 6), ("bang_c", -1.4, -4), ("bang_a", 0.2, 6),
                                    ("bang_c", 1.7, -5), ("bang_b", 3.2, 9))):
        head.append(HG(p, x, 28.4, -5.35 + 0.1 * (i % 2), rz))
    head.append(HG("frame", -4.9, 28, -5.0, -6))
    head.append(mirror_cube(head[-1]))
    for x, z in ((-2.0, -0.6), (0.6, -1.8), (2.4, 0.2)):
        head.append(C("tuft", [x - 1, 28.2, z - 0.75], pivot=[x, 28.2, z], rotation=[-18, 0, -x * 8]))
    bone("head", "body", [0, 20 + L, 0], head)

    bone("eyelids", "head", [0, 23.5 + L, -3.8], [C("eyelids", [-3.25, 21.75, -3.9])])
    bone("mouth", "head", [0, 21 + L, -3.8], [C("mouth", [-0.875, 20.25, -3.9])])

    side = [("strand_s1", -6.1, 26.0, -3.4, -8, 0), ("strand_s2", -6.3, 25.0, -2.0, -11, 0),
            ("strand_s1", -6.2, 26.2, 0.8, -9, 3)]
    bone("hair_side_r", "head", [-4.7, 27 + L, 0], [HG(*s) for s in side], mirror=True)
    back = []
    for i in range(9):                                              # a round bob
        x = -4.8 + 1.2 * i
        back.append(HG("strand_s1" if i % 2 else "strand_s2", x, 26.8, 4.9 + 0.3 * (i % 2), x * 1.5, 6))
    bone("hair_back", "head", [0, 27 + L, 4.5], back)
    bone("ahoge", "head", [0, 28.5 + L, -1], [
        C("ahoge", [-0.5, 28.5, -1.5]),
        C("ahoge", [-0.35, 29.8, -1.5], pivot=[0, 29.8, -1], rotation=[0, 0, -20]),
        C("ahoge_tip", [0.1, 31.0, -1.5], pivot=[0.5, 31.1, -1], rotation=[0, 0, -50]),
    ])

    bone("arm_r", "body", [-3.25, 19.5 + L, 0], [C("arm_up", [-4.0, 16, -0.75])], mirror=True)
    bone("forearm_r", "arm_r", [-3.25, 16 + L, 0], [
        C("forearm", [-4.0, 13, -0.75]), C("bracer", [-4.5, 13.6, -1.25]),
    ], mirror=True)
    bone("hand_r", "forearm_r", [-3.25, 13 + L, 0], [
        C("palm", [-4.0, 11.5, -0.5]), C("fingers", [-4.0, 10.5, -0.5]), C("thumb", [-2.5, 11.5, -0.25]),
    ], mirror=True)

    # stubby little wings, too small to fly with, from her shoulder blades
    bone("wing_r", "body", [-2.5, 19 + L, 1.8], [C("flipper", [-5.0, 14.6, 1.8], pivot=[-2.5, 19, 1.8], rotation=[0, 0, -30])], mirror=True)
    bone("wing_tip_r", "wing_r", [-4.5, 15.0 + L, 2.0], [C("flipper_tip", [-5.5, 13.0, 1.8], pivot=[-4.5, 15.0, 2.0], rotation=[0, 0, -40])],
         mirror=True)

    # a curly tail plume, standing up and curling forward
    tail = [C("tail_fluff", [-1.75, 12.2, 2.8])]
    for i, (x, rz, rx) in enumerate(((-1.6, -14, -30), (-0.75, 0, -45), (0.1, 14, -30))):
        tail.append(C("plume", [x - 0.25, 12.4, 3.6], pivot=[x + 0.75, 12.4, 3.9], rotation=[rx, 0, rz]))
    bone("tail", "body", [0, 13.2 + L, 3], tail)

    # legs: short and thick, yellow, with claws
    cx = -1.4
    bone("leg_r", "root", [cx, 12 + L, 0], [C("thigh", [cx - 1, 9.5, -1]), C("fluff", [cx - 1.25, 8.5, -1.25])], mirror=True)
    bone("shin_r", "leg_r", [cx, 9 + L, 0], [cube("shin", [cx - 1.125, 2, -1.125]), cube("ankle", [cx - 1, 1, -1])], mirror=True)
    foot = [cube("heel", [cx - 1, 0, -1])]
    for dx, spread in ((-1.5, 14), (-0.5, 0), (0.5, -14)):
        x = cx + dx
        foot += [cube("toe1", [x, 0, -3.5], pivot=[cx, .5, -1], rotation=[0, spread, 0]),
                 cube("toe2", [x, 0, -5.0], pivot=[cx, .5, -1], rotation=[0, spread, 0])]
    bone("foot_r", "shin_r", [cx, 1, 0], foot, mirror=True)
    return m


# ----------------------------------------------------------------------------- animations
def animations(an):
    for f in (H.std_idle, H.std_sing, H.std_bathe):
        f(an)
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold

    # walk: the penguin's waddle - short steps, the whole round body rocking side to side, wings flapping for balance
    an.add("walk", 0.7, True, merge({
        "leg_r": {"rotation": T((0, (16, 0, 0)), (0.35, (-16, 0, 0)), (0.7, (16, 0, 0)))},
        "leg_l": {"rotation": T((0, (-16, 0, 0)), (0.35, (16, 0, 0)), (0.7, (-16, 0, 0)))},
        "root": {"rotation": T((0, (0, 0, 10)), (0.35, (0, 0, -10)), (0.7, (0, 0, 10)))},
        "body": {"position": T((0, (0, 0, 0)), (0.17, (0, 0.5, 0)), (0.35, (0, 0, 0)), (0.52, (0, 0.5, 0)), (0.7, (0, 0, 0)))},
        "head": {"rotation": T((0, (0, 0, -5)), (0.35, (0, 0, 5)), (0.7, (0, 0, -5)))},
        "tail": {"rotation": T((0, (0, 14, 0)), (0.35, (0, -14, 0)), (0.7, (0, 14, 0)))},
    }, sym("wing", T((0, (0, 0, -10)), (0.17, (0, 0, -45)), (0.35, (0, 0, -10)), (0.52, (0, 0, -45)), (0.7, (0, 0, -10)))),
        sym("arm", hold(0, 0, 10)), sym("forearm", hold(-10, 0, 0))))
    an.A[f"animation.{NAME}.fly"] = an.A[f"animation.{NAME}.walk"]      # she never flies (the preview sheet wants a "fly")


if __name__ == "__main__":
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="sing", head_center=(0, 22.5 + L), body_center=(0, 15.5 + L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("walk", 0.2, 0, 0), ("idle", 0, -30, 8)])
    write_variants()
