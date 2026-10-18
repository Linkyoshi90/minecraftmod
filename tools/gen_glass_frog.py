#!/usr/bin/env python3
"""Glass Frog - species definition (Bullywug family). Shared engine: anuran_lib.py on top of harpy_lib.py.

    python tools/gen_glass_frog.py                    # write geo / animation / texture
    python tools/gen_glass_frog.py --preview x.png    # also render x.png, x_face.png, x_body.png, x_texture.png

Concept: a delicate cloud-forest stream frog, about 1.4 blocks, with see-through skin. Pale mint, dotted back and limbs;
the front of her body has a real window (cut-out pixels in the texture) through which her tiny organs show: a red heart
that beats (an animation on its own bone), pink lungs, a liver and a pale coil of gut. Big forward-facing pale-gold eyes,
long straight seafoam hair. Outfit: a cobweb-silk shawl, a dew-drop necklace, a gauzy leaf skirt, and a leaf parasol on a
stem strapped to her back for the cloud-forest rain. Her signature moves are the high peeping call and guarding an egg
clutch. Humanoid face.
"""
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import anuran_lib as A
from anuran_lib import rg, cube, mirror_cube, pick, noise, CLEAR, BROAD

NAME = "glass_frog"

SPEC = SimpleNamespace(legx=1.5, eye_x=2.0, eye_out=0.25, eye_rise=1.0, head_sink=0.75, head_fwd=0.0, chest_fwd=0.0,
                       mouth_y=0.75, throat_y=0.5, tymp_y=1.8, tymp_z=2.8,
                       hum=dict(sep=2.0, y=3.4, a=5.2, b=5.8, mw=1.2))
SIZES = {
    "head": (7, 6.5, 6.5), "eyelid": (3, 3.25, .125), "mouth": (2.25, 1.5, .125), "tymp": (.25, 1.75, 1.75),
    "tongue_a": (1, .5, 5.5), "tongue_b": (1.75, .5, 1.5), "throat": (3.75, 2.5, 3.25),
    "bang_a": (1.5, 3, .5), "bang_b": (1.25, 4, .5), "strand_s": (1.25, 5, .75), "strand_m": (1.25, 8, .75), "strand_l": (1.25, 11, .75),
    "hips": (5, 2.5, 4), "mid": (4.5, 3, 3.5), "chest": (5.5, 4, 4), "belly_f": (.5, .5, .5), "belly_s": (.5, .5, .5),
    "arm_up": (1.25, 3, 1.25), "forearm": (1, 3, 1), "palm": (2, 1, 1),
    "fing_a": (.5, 1, .5), "fing_b": (.5, 1.25, .5), "pad_h": (.5, .5, .5),
    "thigh": (2.5, 3.75, 2.5), "shin": (1.5, 4.5, 1.5), "heel": (3, 1, 2),
    "toe_a": (.5, .5, 2), "toe_b": (.5, .5, 3), "toe_c": (.5, .5, 3.25), "toe_d": (.5, .5, 2.5), "pad_t": (.75, .5, .75), "web_f": (2.75, .25, 2.5),
}


def declare_parts():
    A.declare_base(SIZES)
    for args in [
        ("heart", 1.5, 1.5, 1), ("lung", 1.25, 1.75, .75), ("liver", 2.25, 1.25, 1), ("gut", 2.25, 1.75, 1),
        ("shawl", 6.25, 1.75, 4.75), ("dew", .75, .75, .75), ("skirt_p", 2.5, 3.5, .5), ("skirt_s", .5, 3.5, 2.5),
        ("stem", .5, 16, .5), ("leaf_um", 10, .5, 9), ("belt", 5.5, .75, 4.25), ("hair_pin", 1, 1.5, .25),
    ]:
        H.part(*args)


PAL = A.new_pal(
    BACK=rg([(60, 110, 84), (110, 170, 130), (166, 220, 176), (210, 246, 214), (240, 255, 236)], nodither=True),
    BELLY=rg([(150, 190, 170), (196, 226, 208), (228, 246, 232), (246, 255, 248)], nodither=True),
    spots=dict(seed=101, cell=4, rmin=0.8, rmax=1.7, depth=0.30), side_split=0.55, gloss=3,
    EYE=rg([(120, 100, 20), (180, 150, 30), (226, 196, 60), (246, 230, 120), (254, 250, 190)]),
)
PAL.EYE_OUT, PAL.PUPIL, PAL.hpupil = H.rgb(60, 54, 20), H.rgb(10, 12, 10), (0.62, 0.62)
PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE, PAL.NOSTRIL = H.rgb(70, 120, 94), H.rgb(150, 70, 80), H.rgb(236, 130, 140), H.rgb(70, 120, 94)
PAL.TONGUE_R = rg([(170, 80, 90), (210, 110, 120), (238, 140, 150), (252, 180, 186)])
PAL.BLUSH, PAL.BLUSH_L = H.rgb(236, 170, 160), H.rgb(246, 196, 184)
PAL.DRUM = rg([(80, 130, 100), (120, 176, 136), (168, 216, 176), (210, 240, 214)])
PAL.SAC = rg([(150, 200, 170), (190, 230, 206), (220, 248, 228), (244, 255, 244)])
PAL.PAD = rg([(150, 190, 150), (196, 226, 190), (226, 246, 216), (248, 255, 240)])
PAL.HAIR = rg([(30, 84, 84), (56, 124, 118), (90, 166, 152), (140, 206, 186), (200, 238, 222), (240, 255, 248)])
PAL._mouth_y = SPEC.mouth_y

HEART = rg([(90, 10, 20), (160, 24, 36), (220, 56, 64), (250, 120, 120)], nodither=True)
LIVER = rg([(70, 28, 22), (116, 52, 40), (160, 84, 64), (196, 118, 94)])
GUT = rg([(190, 150, 150), (224, 186, 184), (246, 220, 216)])
LUNG = rg([(170, 90, 110), (214, 130, 150), (240, 176, 190)])
SILK = rg([(150, 160, 170), (196, 206, 214), (232, 240, 246), (252, 254, 255)])
DEW = rg([(110, 160, 204), (170, 210, 238), (226, 244, 255)])
LEAF = rg([(24, 80, 44), (44, 122, 66), (80, 168, 96), (130, 210, 128)])
VINE = rg([(40, 70, 30), (74, 108, 46), (112, 146, 70)])
HEART_BM = [".##.##.", "#######", "#######", ".#####.", "..###..", "...#..."]


def paint():
    pt = A.paint_base(PAL, curve=3.0, fringe=4, hum=SPEC.hum)

    def window(cx, cy, rx, ry):                                  # a see-through oval in the skin
        def fn(f, x, y, w, h):
            e = ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2
            if e <= 1.0:
                return CLEAR
            if e <= 1.28:
                return pick(PAL.BELLY, 0.95 - 0.5 * (e > 1.15), x, y)
            return None
        return fn
    pt("chest", ("north",), window(11, 9.6, 8.2, 5.4))
    pt("mid", ("north",), window(9, 4.2, 6.2, 4.6))

    def bone(f, x, y, w, h):                                     # a faint paler line down limbs, like bone through glass
        if f in BROAD and x == w // 2:
            return pick(PAL.BACK, 0.92, x, y)
        return None
    for p in ("thigh", "shin", "arm_up", "forearm"):
        pt(p, "all", bone)

    def heart(f, x, y, w, h):
        if f == "north":
            i, j = x - 1, y - 1
            if 0 <= j < len(HEART_BM) and 0 <= i < len(HEART_BM[0]) and HEART_BM[j][i] == "#":
                return pick(HEART, 0.8 - 0.35 * (j > 3) + (0.2 if (i, j) == (1, 1) else 0), x, y)
            return HEART[0]
        return pick(HEART, 0.4, x, y)
    pt("heart", "all", heart)
    pt("lung", "all", lambda f, x, y, w, h: pick(LUNG, 0.55 + 0.3 * (abs(x - w // 2) < 1 and y % 3 != 0) + (noise(x, y, 3) - 0.5) * 0.12, x, y))
    pt("liver", "all", lambda f, x, y, w, h: pick(LIVER, 0.55 + 0.25 * (x < 3 and y < 3) + (noise(x, y, 5) - 0.5) * 0.14 - 0.15 * (x > w - 3), x, y))
    pt("gut", "all", lambda f, x, y, w, h: pick(GUT, 0.35 + 0.5 * (((y + (3 if (x // 3) % 2 else 0)) // 2) % 2) + (noise(x, y, 7) - 0.5) * 0.1, x, y))

    def shawl(f, x, y, w, h):
        if f in ("up", "down"):
            return SILK[1]
        if (x + y) % 5 == 0 or (x - y) % 5 == 0:
            return pick(SILK, 0.9, x, y)
        if noise(x, y, 9) > 0.72 or (y >= h - 2 and noise(x, y, 10) > 0.4):
            return CLEAR                                          # lace holes
        return pick(SILK, 0.50 + (noise(x, y, 11) - 0.5) * 0.12, x, y)
    pt("shawl", "all", shawl)
    pt("dew", "all", lambda f, x, y, w, h: pick(DEW, 0.45 + 0.5 * (x < 2 and y < 2) - 0.15 * (y > h - 2), x, y))

    def skirt(f, x, y, w, h):
        if f in ("up", "down"):
            return LEAF[1]
        if y > h - 3 and noise(x, y, 13) > 0.5:
            return CLEAR
        return pick(LEAF, 0.55 + 0.2 * (x % 5 == 0) + (noise(x, y, 15) - 0.5) * 0.1, x, y)
    pt("skirt_p", "all", skirt)
    pt("skirt_s", "all", skirt)
    pt("stem", "all", lambda f, x, y, w, h: pick(VINE, 0.45 + 0.35 * (y % 12 < 2), x, y))
    pt("belt", "all", lambda f, x, y, w, h: pick(VINE, 0.35 + 0.4 * (((x + y) // 2) % 2), x, y))

    def umbrella(f, x, y, w, h):
        if f in ("up", "down"):
            dx, dy = (x + 0.5 - w / 2.0) / (w / 2.0), (y + 0.5 - h / 2.0) / (h / 2.0)
            r2 = dx * dx + dy * dy
            if r2 > 1.0:
                return CLEAR
            if f == "down":
                return pick(LEAF, 0.30 + 0.2 * (abs(dx) < 0.05), x, y)
            vein = abs(dx) < 0.05 or any(abs(abs(dx) * 0.9 - abs(dy) * k) < 0.05 for k in (0.5, 1.2))
            if noise(x // 2, y // 2, 17) > 0.93:
                return pick(DEW, 0.8, x, y)                       # a raindrop on the leaf
            return pick(LEAF, 0.52 + 0.25 * vein + (noise(x, y, 19) - 0.5) * 0.08 - 0.15 * (r2 > 0.8), x, y)
        return CLEAR
    pt("leaf_um", "all", umbrella)
    pt("hair_pin", "all", lambda f, x, y, w, h: pick(LEAF, 0.6, x, y))
    return pt.T


def extras(r, g):
    HG = H.hang
    top, z0 = g.head_top, g.head_z0

    # the belly plates are not used: the window shows the organs behind it
    r.b["belly"]["cubes"] = []
    r.b["belly_r"]["cubes"] = []
    cy = g.chest_y0
    org = [cube("lung", [-2.4, cy + 1.7, g.chest_front + 0.7]), cube("lung", [1.15, cy + 1.7, g.chest_front + 0.7]),
           cube("liver", [-2.1, g.mid_y0 + 1.5, g.mid_front + 0.45]), cube("gut", [-0.8, g.mid_y0 + 0.1, g.mid_front + 0.45])]
    r.add("organs", "body", [0, cy, g.chest_front], org)
    r.add("heart", "organs", [0.95, cy + 1.4, g.chest_front + 1.0], [cube("heart", [0.2, cy + 0.65, g.chest_front + 0.4])])

    # long straight seafoam hair
    r.put("head", HG("bang_a", -0.75, top + 0.3, z0 - 0.3, 0, -4), HG("hair_pin", 1.9, top + 0.2, z0 - 0.35, -8, -4))
    r.add("hair_side_r", "head", [-4.2, top - 1.5, 0], [HG("strand_l", -4.9, top - 1.2, -0.3, -4, 0), HG("strand_m", -4.9, top - 1.2, 1.5, -5, 2)], mirror=True)
    back = [HG(n, x, top - 1.2, 3.5 + 0.25 * (i % 2), x * 1.2, 3 + 2 * (i % 3)) for i, (n, x) in enumerate(zip(("strand_l", "strand_m", "strand_l", "strand_m", "strand_l"), (-3.0, -1.5, 0.0, 1.5, 3.0)))]
    r.add("hair_back", "head", [0, top - 1.2, 3.3], back)

    # silk shawl, dew necklace, rope belt, gauzy leaf skirt
    r.put("body", cube("shawl", [-3.125, g.chest_top - 1.3, g.chest_front - 0.375]))
    beads = []
    for x in (-2.0, -1.0, 0.0, 1.0, 2.0):
        beads.append(cube("dew", [x - 0.375, g.chest_top - 1.2 - 0.35 * (1 - (x / 2.4) ** 2), g.chest_front - 0.7]))
    r.put("body", *beads, cube("belt", [-2.75, g.hips_y0 + 1.7, -2.4]))
    ky = g.hips_y0 + 1.7
    sk = [HG("skirt_p", -2.5, ky, -2.55, 3, -6), HG("skirt_p", 0.0, ky, -2.55, -3, -6), HG("skirt_p", -2.5, ky, 2.05, 3, 6), HG("skirt_p", 0.0, ky, 2.05, -3, 6),
          HG("skirt_s", -3.25, ky, -1.25, -6, 0), HG("skirt_s", 2.75, ky, -1.25, 6, 0)]
    r.add("skirt", "body", [0, ky, 0], sk)

    # leaf parasol on a stem strapped to her back
    sy = g.chest_y0 - 1.0
    par = [cube("stem", [-0.25, sy, 2.4]), cube("leaf_um", [-5.0, sy + 16.0, -2.0], pivot=[0, sy + 16.0, 2.65], rotation=[-6, 0, 0])]
    for dx in (-3.5, 0.0, 3.5):
        par.append(cube("dew", [dx - 0.375, sy + 15.2, 2.0 + (0.0 if dx else 0.8)]))
    r.add("parasol", "body", [0, sy + 4.0, 2.6], par)


def build_model():
    m, g = A.build(SPEC, extras, NAME)
    build_model.g = g
    return m


TUNE = SimpleNamespace(crouch=10, step=15, hop_lift=6.0, throat_out=(0, -1.4, -2.0), puff_f=0.8, croak_crouch=20, sit_crouch=58,
                       tongue_len=5.0, arm_rest=(3, 0, 4), extra={})


def beats(period):
    keys = [(0, (0, 0, 0))]
    t0 = 0.0
    while t0 < period - 0.5:
        keys += [(t0 + 0.08, (0, 0.35, -0.1)), (t0 + 0.18, (0, 0, 0)), (t0 + 0.28, (0, 0.25, -0.1)), (t0 + 0.42, (0, 0, 0))]
        t0 += 0.8
    keys.append((period, (0, 0, 0)))
    return H.track(*keys)


def animations(an):
    g = build_model.g
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    OUT, REST = H.OUT_, H.REST
    sway = lambda amp, per=4.0: T((0, (0, 0, 0)), (per / 4, (amp, 0, 0)), (per / 2, (0, 0, 0)), (3 * per / 4, (-amp, 0, 0)), (per, (0, 0, 0)))
    TUNE.extra = {
        "idle": {"heart": {"position": beats(4.0)}, "skirt": {"rotation": sway(2)}, "parasol": {"rotation": T((0, (0, 0, 0)), (2, (0, 0, 2)), (4, (0, 0, 0)))}},
        "walk": {"heart": {"position": beats(1.0)}, "skirt": {"rotation": T((0, (0, 0, 4)), (0.5, (0, 0, -4)), (1, (0, 0, 4)))}, "parasol": {"rotation": T((0, (0, 0, 3)), (0.5, (0, 0, -3)), (1, (0, 0, 3)))}},
        "hop": {"heart": {"position": beats(0.9)}, "skirt": {"rotation": T((0, (0, 0, 0)), (0.3, (-24, 0, 0)), (0.74, (12, 0, 0)), (0.9, (0, 0, 0)))}, "parasol": {"rotation": T((0, (0, 0, 0)), (0.3, (10, 0, 0)), (0.74, (-6, 0, 0)), (0.9, (0, 0, 0)))}},
        "swim": {"skirt": {"rotation": hold(-30, 0, 0)}, "parasol": {"rotation": hold(-60, 0, 0)}},
        "croak": {"heart": {"position": beats(3.0)}, "parasol": {"rotation": sway(2, 3.0)}},
        "sit": {"heart": {"position": beats(4.0)}, "skirt": {"rotation": hold(6, 0, 0)}},
        "tongue": {"heart": {"position": beats(1.6)}},
    }
    A.common_anims(an, g, TUNE)
    pose = lambda deg: (-deg, 2 * deg, 0, 0)

    # peep: the high, quick call - tiny throat pulses, head bobbing, mouth half open
    pulses = [(0, REST)]
    for i in range(7):
        t0 = 0.1 + i * 0.26
        pulses += [(t0, REST), (t0 + 0.07, (0, -1.2, -1.7)), (t0 + 0.15, REST)]
    pulses.append((2.0, REST))
    lg, by = A.gait(g.T, g.Sh, 2.0, [(0, pose(16)), (2.0, pose(16))], [(0, pose(16)), (2.0, pose(16))])
    for s_ in ("r", "l"):
        lg["leg_" + s_]["position"] = by
    lg["body"] = {"position": by}
    an.add("peep", 2.0, True, merge(
        lg,
        {"throat": {"position": T(*pulses)}, "head": {"rotation": T((0, (-12, 0, 0)), (0.13, (-6, 0, 0)), (0.26, (-12, 0, 0)), (2.0, (-12, 0, 0)))},
         "mouth": {"position": T((0, REST), (0.1, OUT), (1.9, OUT), (2.0, REST))},
         "heart": {"position": beats(2.0)}, "skirt": {"rotation": sway(3, 2.0)}, "parasol": {"rotation": sway(3, 2.0)}},
        sym("arm", hold(3, 0, 4)), sym("forearm", hold(-8, 0, 0))))

    # guard: low over an egg clutch, arms curved round it, head slowly scanning
    c = 46
    lg2, by2 = A.gait(g.T, g.Sh, 3.0, [(0, pose(c)), (3, pose(c))], [(0, pose(c)), (3, pose(c))])
    for s_ in ("r", "l"):
        lg2["leg_" + s_]["position"] = by2
    lg2["body"] = {"position": by2, "rotation": hold(18, 0, 0)}
    an.add("guard", 3.0, True, merge(
        lg2,
        {"head": {"rotation": T((0, (-6, 0, 0)), (0.9, (-12, -26, 0)), (1.8, (-12, 26, 0)), (2.7, (-6, 0, 0)), (3, (-6, 0, 0)))},
         "heart": {"position": beats(3.0)}, "skirt": {"rotation": hold(8, 0, 0)}, "parasol": {"rotation": hold(-6, 0, 0)}, **H.blink(1.4)},
        sym("arm", hold(-34, 0, 26)), sym("forearm", hold(-52, 0, 0)), sym("hand", hold(-16, 0, 0))))


def main():
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="peep", head_center=(0, 19.0), body_center=(0, 13.0), scale=15.0,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("idle", 0.1, -12, 4), ("peep", 0.3, -30, 8), ("guard", 1.0, -30, 8), ("hop", 0.4, -30, 10)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("peep", 0.3, 0, 0), ("tongue", 0.3, 0, 0), ("idle", 0, -30, 8)])


if __name__ == "__main__":
    main()
