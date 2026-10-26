#!/usr/bin/env python3
"""Penguin Harpy - species definition. The shared engine lives in harpy_lib.py; the upper body is the seagull's
(gen_seagull_harpy.py) on short, stubby legs, with flippers for wings.

    python tools/gen_penguin_harpy.py                 # write the three asset files
    python tools/gen_penguin_harpy.py --preview x.png # also render preview sheets
    python tools/gen_penguin_harpy.py --lineup x.png  # also render all variants side by side

Variants (VARIANTS below) only get their own texture, penguin_harpy_<variant>.png; the default (emperor) stays
penguin_harpy.png. Keep in sync with PenguinHarpy.Variant.

Field guide: a flightless seabird of snow and ice who lives in big colonies, waddles on land and flies underwater.
She hunts fish and shares her catch with friends. Now and then one walks off alone, away from the sea.
Design: black hair and back, a white front (a tuxedo), flippers in place of wings, short legs with pink-orange
webbed feet; a golden patch at the ears (the emperor), a white bonnet (gentoo), a chin strap (chinstrap), yellow
crest plumes (rockhopper). Animations add "waddle" for walking and "swim" (flat out, flippers beating).
"""
import copy
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

NAME = "penguin_harpy"
L = -2.5                                         # short legs: everything above the shins sits lower than the sparrow

# ----------------------------------------------------------------------------- palette
PAL = H.Pal()
PAL.HAIR = ramp([rgb(10, 12, 18), rgb(20, 24, 34), rgb(34, 40, 56), rgb(54, 62, 84), rgb(86, 96, 122), rgb(130, 140, 166)])
PAL.SKIN = ramp([rgb(200, 156, 140), rgb(226, 188, 170), rgb(242, 214, 198), rgb(252, 234, 220)], nodither=True)
PAL.FEA = ramp([rgb(8, 10, 16), rgb(18, 22, 32), rgb(32, 38, 54), rgb(52, 60, 82), rgb(84, 94, 120), rgb(130, 140, 166), rgb(190, 198, 216)])
PAL.EYE = ramp([rgb(60, 30, 14), rgb(100, 54, 24), rgb(150, 90, 40), rgb(200, 140, 70), rgb(236, 196, 130)])
PAL.LEG = ramp([rgb(150, 80, 70), rgb(196, 112, 96), rgb(226, 146, 126), rgb(244, 180, 160), rgb(252, 214, 200)])
PAL.hair_gain, PAL.hair_off = 0.62, -0.02
PAL.TALON = [rgb(40, 30, 30), rgb(80, 66, 62)]
PAL.EYE_OUT, PAL.PUPIL = rgb(30, 16, 10), rgb(10, 6, 4)
PAL.BROW = PAL.HAIR[1]
PAL.eye_a, PAL.eye_b, PAL.eye_slant = 4.4, 5.2, 0.0
PAL.pupil = (2.0, 3.0)                                       # big, round, earnest eyes
PAL.wing_base_t, PAL.tail_fluff_t, PAL.fluff_t = (0.05, 0.18), 0.30, 0.22

WHITE = ramp([rgb(200, 204, 214), rgb(228, 232, 240), rgb(244, 246, 250), rgb(255, 255, 255)])
GOLD = ramp([rgb(200, 120, 20), rgb(236, 160, 40), rgb(250, 200, 80), rgb(255, 230, 150)])


# ----------------------------------------------------------------------------- parts
def declare_parts():
    H.standard_parts()
    for args in [("shin", 1.5, 7 + L, 1.5), ("flipper", 2.5, 8, .5), ("flipper_tip", 2, 3, .5), ("tail_f", 1.5, 3, .5),
                 ("tail_g", 1.5, 2.5, .5), ("patch", 1.5, 2.5, .25), ("strap", 6, .5, .25)]:
        H.part(*args)


# ----------------------------------------------------------------------------- texture
def paint(pal=PAL, patch=GOLD, crest=None, bonnet=False, strap=False):
    pt = H.Painter()
    FEA, SKIN = pal.FEA, pal.SKIN
    D = H.D
    H.paint_head(pt, pal)
    H.paint_hair(pt, pal)
    H.paint_body_skin(pt, pal)
    H.paint_legs(pt, pal)
    for p, spec in (("tassel", (1, 2, 4)), ("cuff_f", (1, 2, 3)), ("trim", (0, 1, 3)), ("hfeather", (0, 2, 4))):
        pt(p, "all", H.feather_fn(pal, *spec, None, False, False))
    pt("back_cover", "all", lambda f, x, y, w, h: pick(FEA, 0.12 + 0.15 * y / h + (noise(x, y, 19) - 0.5) * 0.08, x, y))

    # flippers: black above, white beneath, with a white trailing edge
    def flipper(f, x, y, w, h):
        if f in ("down", "east", "west") or (f in BROAD and x in (0, w - 1)):
            return pick(WHITE, 0.5 + (noise(x, y, 61) - 0.5) * 0.1, x, y)
        return pick(FEA, 0.15 + 0.1 * (y / h) + (noise(x, y, 62) - 0.5) * 0.08, x, y)
    pt("flipper", "all", flipper)
    pt("flipper_tip", "all", flipper)
    pt("wing_base", "all", lambda f, x, y, w, h: pick(FEA, 0.15, x, y))
    pt("tail_fluff", "all", lambda f, x, y, w, h: pick(FEA, 0.2 + (noise(x, y, 23) - 0.5) * 0.2, x, y))
    pt("tail_f", "all", H.feather_fn(pal, 0, 1, 2, None, True))
    pt("tail_g", "all", H.feather_fn(pal, 0, 1, 2, None, True))

    # the tuxedo: a white shirt-front, black at the sides and back
    def tux(f, x, y, w, h):
        if f == "up":
            return pick(SKIN, 0.66, x, y)
        if f in ("south", "east", "west"):
            return pick(FEA, 0.18 + (noise(x, y, 31) - 0.5) * 0.08, x, y)
        if f == "north" and (x < 1 or x >= w - 1):
            return pick(FEA, 0.2, x, y)
        return pick(WHITE, 0.6 + (noise(x, y, 32) - 0.5) * 0.1, x, y)
    pt("chest", "all", tux)
    pt("bust", "all", tux)
    pt("patch", "all", lambda f, x, y, w, h: pick(patch, 0.6 - 0.25 * (y / h), x, y))
    pt("collar", "all", lambda f, x, y, w, h: pick(patch, 0.55 + (noise(x, y, 33) - 0.5) * 0.15, x, y) if f != "south" else pick(FEA, 0.2, x, y))
    pt("strap", "all", lambda f, x, y, w, h: pick(FEA, 0.2, x, y))
    if crest:                                            # the rockhopper's yellow plumes, swept back off her brow
        for p in ("tuft", "ahoge", "ahoge_tip"):
            pt(p, "all", lambda f, x, y, w, h: pick(crest, 0.55 + (noise(x, y, 71) - 0.5) * 0.3, x, y))
    if bonnet:                                           # the gentoo's white band over the crown
        pt("tuft", "all", lambda f, x, y, w, h: pick(WHITE, 0.6, x, y))
    pt("sash", "all", lambda f, x, y, w, h: pick(patch, 0.5 + 0.2 * (y == 0), x, y))
    pt("pendant", "all", lambda f, x, y, w, h: pick(patch, 0.75, x, y))
    pt("cord", "all", lambda f, x, y, w, h: WHITE[1])

    def coat(white):                                      # a short skirt: white apron in front, black tails behind
        def fn(f, x, y, w, h):
            if f not in BROAD:
                return FEA[1]
            r = h - 1 - y
            if r < D and abs(x + 0.5 - w / 2.0) > (w / 2.0) * ((r + 1) / D) ** 0.6:
                return CLEAR
            ramp_ = WHITE if white else FEA
            return pick(ramp_, (0.55 if white else 0.18) + (noise(x, y, 43) - 0.5) * 0.12, x, y)
        return fn
    pt("leaf_s", "all", coat(True))
    pt("leaf_m", "all", coat(True))
    pt("leaf_l", "all", coat(False))
    pt("leaf_side", "all", coat(False))
    pt("bracer", "all", lambda f, x, y, w, h: pick(WHITE, 0.5 - 0.2 * (y in (0, h - 1)), x, y))
    if strap:                                            # the chinstrap's thin black line under her chin
        pass
    return pt.T


# ----------------------------------------------------------------------------- variants
def variant_pal(eye=None):
    p = copy.copy(PAL)
    if eye:
        p.EYE = ramp([rgb(*c) for c in eye])
    return p


R = lambda *c: ramp([rgb(*x) for x in c])
VARIANTS = {
    # adélie: all black and white, no colour at all
    "adelie": (variant_pal(), WHITE, None, False, False),
    # gentoo: a white bonnet over the crown, orange accents
    "gentoo": (variant_pal(), R((200, 80, 20), (236, 110, 30), (250, 150, 60), (255, 196, 120)), None, True, False),
    # chinstrap: a thin black line under the chin (the strap), pale accents
    "chinstrap": (variant_pal(), WHITE, None, False, True),
    # rockhopper: yellow crest plumes, red eyes
    "rockhopper": (variant_pal([(110, 14, 14), (160, 24, 24), (210, 50, 40), (240, 100, 80), (255, 160, 140)]),
                   R((200, 160, 10), (236, 200, 30), (250, 226, 80), (255, 244, 150)),
                   R((200, 160, 10), (236, 200, 30), (250, 226, 80), (255, 244, 150)), False, False),
}


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "monstergirls", "textures", "entity")
    texes = {"emperor": paint()}
    for v, (pal, patch, crest, bonnet, strap) in VARIANTS.items():
        texes[v] = paint(pal, patch, crest, bonnet, strap)
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
    rows = [(g("idle"), 0, -20, 6, 11, None), (g("swim"), 0.4, -60, 20, 11, None), (g("idle"), 0, -15, 0, 30, (0, 22.5 + L))]
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
    for i, (p, x, rz) in enumerate((("leaf_s", -2.6, 4), ("leaf_s", -1.0, -2), ("leaf_s", 0.6, 2), ("leaf_s", 2.0, -4))):
        sk.append(HG(p, x, 13.7, -2.7 - 0.15 * (i % 2), rz, -9))
    for i, (p, x, rz) in enumerate((("leaf_l", -2.0, 3), ("leaf_l", 0.0, -3))):   # the tailcoat's two tails
        sk.append(HG(p, x, 13.7, 2.2, rz, 12))
    for z in (-2.2, 0.0):
        sk.append(C("leaf_side", [-3.4, 8.7, z], pivot=[-3.15, 13.7, z + 1.25], rotation=[0, 0, -9]))
        sk.append(mirror_cube(sk[-1]))
    bone("skirt", "body", [0, 13.7 + L, 0], sk)

    head = [C("head", [-4, 20, -4]), C("hair_cap", [-4, 20, -4], inflate=0.75)]
    ear_r = C("ear", [-5.0, 21.7, -1.2])
    patch_r = C("patch", [-4.95, 20.4, -0.2])                      # the coloured patch behind the ears (emperor, king)
    head += [ear_r, mirror_cube(ear_r), patch_r, mirror_cube(patch_r)]
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
    for i in range(9):                                              # a neat bob
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

    # flippers instead of wings: flat, stiff, held a little out from her sides
    bone("wing_r", "body", [-2.5, 19 + L, 1.8], [C("wing_base", [-3.0, 15, 1.6]),
                                                  C("flipper", [-5.25, 11.0, 1.8], pivot=[-2.5, 19, 1.8], rotation=[0, 0, -12])], mirror=True)
    bone("wing_tip_r", "wing_r", [-4.5, 11.5 + L, 2.0], [C("flipper_tip", [-5.25, 8.2, 1.8], pivot=[-4.5, 11.5, 2.0], rotation=[0, 0, -12])],
         mirror=True)

    tail = [C("tail_fluff", [-1.75, 12.2, 2.2])]
    for i, (x, ang) in enumerate(zip((-2.0, -0.75, 0.5), (-8, 0, 8))):
        tail.append(HG("tail_f" if i % 2 == 0 else "tail_g", x, 12.7, 3.4 + 0.25 * (i % 2), ang, 30))
    bone("tail", "body", [0, 13.2 + L, 3], tail)

    # legs: short and stout, pink-orange webbed feet
    cx = -1.25
    bone("leg_r", "root", [cx, 12 + L, 0], [C("thigh", [cx - 1, 9.5, -1]), C("fluff", [cx - 1.25, 8.5, -1.25])], mirror=True)
    bone("shin_r", "leg_r", [cx, 9 + L, 0], [cube("shin", [cx - .75, 2, -.75]), cube("ankle", [cx - 1, 1, -1])], mirror=True)
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

    # walk: the waddle - short steps, the whole body rocking side to side, flippers held out for balance
    an.add("walk", 0.8, True, merge({
        "leg_r": {"rotation": T((0, (14, 0, 0)), (0.4, (-14, 0, 0)), (0.8, (14, 0, 0)))},
        "leg_l": {"rotation": T((0, (-14, 0, 0)), (0.4, (14, 0, 0)), (0.8, (-14, 0, 0)))},
        "root": {"rotation": T((0, (0, 0, 9)), (0.4, (0, 0, -9)), (0.8, (0, 0, 9)))},
        "body": {"position": T((0, (0, 0, 0)), (0.2, (0, 0.4, 0)), (0.4, (0, 0, 0)), (0.6, (0, 0.4, 0)), (0.8, (0, 0, 0)))},
        "head": {"rotation": T((0, (0, 0, -5)), (0.4, (0, 0, 5)), (0.8, (0, 0, -5)))},
        "tail": {"rotation": T((0, (0, 12, 0)), (0.4, (0, -12, 0)), (0.8, (0, 12, 0)))},
    }, sym("wing", hold(0, 0, -28)), sym("arm", hold(0, 0, 10)), sym("forearm", hold(-10, 0, 0))))

    # swim: flat out, beating her flippers like wings, feet trailing behind as a rudder
    an.add("swim", 0.6, True, merge({
        "root": {"rotation": hold(80, 0, 0), "position": hold(0, 6, 0)},      # face down, like the merfolk's swim
        "head": {"rotation": hold(-60, 0, 0)},
        "leg_r": {"rotation": hold(-10, 0, 0)}, "leg_l": {"rotation": hold(-10, 0, 0)},
        "hair_back": {"rotation": hold(40, 0, 0)},
        "tail": {"rotation": hold(20, 0, 0)},
    }, sym("wing", T((0, (0, 0, -20)), (0.3, (0, 0, -80)), (0.6, (0, 0, -20)))),
        sym("arm", hold(30, 0, 10)), sym("forearm", hold(-10, 0, 0))))
    an.A[f"animation.{NAME}.fly"] = an.A[f"animation.{NAME}.swim"]      # she only flies underwater (the preview sheet wants a "fly")


if __name__ == "__main__":
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="sing", head_center=(0, 22.5 + L), body_center=(0, 15.5 + L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("walk", 0.2, 0, 0), ("idle", 0, -30, 8)])
    write_variants()
