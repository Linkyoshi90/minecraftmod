#!/usr/bin/env python3
"""Poison Dart Frog - species definition (Bullywug family). Shared engine: anuran_lib.py on top of harpy_lib.py.

    python tools/gen_poison_dart_frog.py                    # write geo / animation / texture
    python tools/gen_poison_dart_frog.py --preview x.png    # also render x.png, x_face.png, x_body.png, x_texture.png

Concept: a slender, quick rainforest frog hunter, about 1.6 blocks. Glossy cobalt-blue skin covered in black spots
(the warning colours are real: she is poisonous, but only after eating the right jungle insects), a pale-blue throat sac,
big round sticky toe pads for climbing, glossy black hair in a high ponytail, yellow-and-black cheek stripes. Outfit: a
leaf bandeau and loincloth with macaw-feather fringe, a bamboo blowpipe slung across her back with a quiver of darts,
a seed-bead necklace. Dart frogs carry their tadpoles on their backs, so three tadpoles cling to hers.
Humanoid face (the default for the whole anuran family).
"""
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import anuran_lib as A
from anuran_lib import rg, cube, mirror_cube, pick, noise, CLEAR, BROAD

NAME = "poison_dart_frog"

SPEC = SimpleNamespace(legx=2.0, eye_x=2.25, eye_out=0.25, eye_rise=1.0, head_sink=0.75, head_fwd=0.0, chest_fwd=0.0,
                       mouth_y=1.0, throat_y=0.5, tymp_y=2.0, tymp_z=3.0,
                       hum=dict(sep=2.25, y=3.7, a=4.9, b=5.6, mw=1.3))
SIZES = {
    "head": (7.5, 7, 7), "eyelid": (2.75, 3, .125), "mouth": (2.25, 1.5, .125), "tymp": (.25, 2, 2), "throat": (4, 2.5, 3.25),
    "bang_a": (1.5, 3, .5), "bang_b": (1.25, 4, .5), "strand_s": (1.25, 5, .75), "strand_m": (1.25, 8, .75), "strand_l": (1.25, 12, .75),
    "hips": (6, 2.5, 4.5), "mid": (4.5, 3, 3.5), "chest": (6, 4, 4.25), "belly_f": (4, 3, 3), "belly_s": (3, 3, 3),
    "arm_up": (1.5, 3.5, 1.5), "forearm": (1.25, 3.5, 1.25), "palm": (2.75, 1, 1.25),
    "fing_a": (.5, 1.25, .5), "fing_b": (.5, 1.5, .5), "pad_h": (.75, .75, .75),
    "thigh": (3, 5, 3), "shin": (2, 6.5, 2), "heel": (3.5, 1, 2.5),
    "toe_a": (.5, .5, 2.5), "toe_b": (.5, .5, 3.5), "toe_c": (.5, .5, 4), "toe_d": (.5, .5, 3), "pad_t": (1, .75, 1), "web_f": (3.25, .25, 2.5),
}


def declare_parts():
    A.declare_base(SIZES)
    for args in [
        ("leaf_top", 6.5, 2.5, 4.75), ("belt", 6.5, 1, 4.75), ("loin", 3, 4, .5), ("feather", .75, 2, .25), ("hfeather", .75, 3, .25),
        ("quiver", 2, 6, 2), ("dart", .25, 3, .25), ("dart_f", .75, 1, .25), ("pipe", .75, 15, .75), ("pipe_band", 1.25, .5, 1.25),
        ("seed", .75, .75, .75), ("tadpole", 1.25, 1, 1.75), ("tad_tail", .25, .75, 1.5), ("armband", 2, .75, 2), ("hair_tie", 1.5, 1, 1.5),
    ]:
        H.part(*args)


PAL = A.new_pal(
    BACK=rg([(2, 6, 28), (6, 28, 100), (20, 84, 214), (74, 150, 250), (170, 216, 255)], nodither=True),
    BELLY=rg([(2, 8, 40), (22, 64, 150), (60, 124, 226), (140, 198, 250)], nodither=True),
    spots=dict(seed=71, cell=5, rmin=1.2, rmax=2.7, depth=0.56), belly_spots=True, side_split=0.55, gloss=2,
    EYE=rg([(10, 6, 6), (30, 14, 10), (70, 36, 20), (130, 80, 36), (190, 130, 60)]),
)
PAL.EYE_OUT, PAL.PUPIL, PAL.hpupil = H.rgb(6, 8, 24), H.rgb(4, 4, 8), (0.72, 0.72)
PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE, PAL.NOSTRIL = H.rgb(4, 10, 44), H.rgb(120, 40, 60), H.rgb(230, 110, 124), H.rgb(4, 8, 36)
PAL.TONGUE_R = rg([(150, 56, 74), (196, 84, 100), (228, 118, 134), (248, 160, 170)])
PAL.DRUM = rg([(4, 14, 60), (10, 40, 130), (30, 90, 200), (70, 140, 240)])
PAL.SAC = rg([(20, 80, 150), (60, 140, 210), (130, 200, 240), (200, 236, 255)])
PAL.PAD = rg([(40, 110, 190), (110, 180, 240), (180, 220, 255), (232, 246, 255)])
PAL.HAIR = rg([(4, 6, 16), (10, 16, 40), (20, 34, 76), (40, 64, 120), (80, 112, 176), (150, 176, 224)])
PAL._mouth_y = SPEC.mouth_y

LEAF = rg([(18, 62, 30), (34, 104, 48), (62, 150, 70), (112, 196, 100)])
CORD = rg([(60, 38, 22), (100, 66, 38), (146, 104, 62)])
FEA_R = rg([(100, 10, 14), (170, 24, 24), (226, 56, 40), (250, 110, 80)])
FEA_Y = rg([(170, 120, 10), (230, 180, 24), (252, 220, 70), (255, 244, 150)])
BAMBOO = rg([(110, 100, 36), (160, 148, 60), (200, 190, 96), (232, 224, 140)])
SEED = rg([(120, 14, 18), (190, 30, 28), (236, 70, 52)])
WEAVE = rg([(120, 66, 30), (170, 106, 48), (214, 150, 80), (240, 196, 120)])


def paint():
    pt = A.paint_base(PAL, curve=3.0, fringe=4, hum=SPEC.hum)

    def paint_face(f, x, y, w, h):                          # yellow-and-black stripes on each cheek
        cx = w / 2.0
        for sg in (-1, 1):
            for k, yy in enumerate((21, 23)):
                x0, x1 = (6, 12 - k) if k == 0 else (7, 12)
                if yy == y and x0 <= abs(x + 0.5 - cx) <= x1 and (x + 0.5 - cx) * sg > 0:
                    return pick(FEA_Y, 0.8, x, y) if (int(x) % 4 != 3) else H.rgb(6, 8, 24)
        return None
    pt("head", ("north",), paint_face)

    def leaf(f, x, y, w, h):
        if f in ("up", "down"):
            return LEAF[1]
        vein = (x % 6 == 3) or abs((y - h * 0.5) - 0.6 * abs(x - w / 2.0) * -1) < 0.6
        return pick(LEAF, 0.50 + 0.22 * vein + (noise(x, y, 5) - 0.5) * 0.10 - (0.15 if y >= h - 2 else 0), x, y)
    pt("leaf_top", "all", leaf)
    pt("belt", "all", lambda f, x, y, w, h: pick(CORD, 0.30 + 0.35 * (((x + y) % 3) == 0), x, y))

    def loin(f, x, y, w, h):
        if f not in BROAD:
            return WEAVE[1]
        if y > h - 3 and noise(x, y, 9) > 0.55 + (h - 1 - y) * 0.15:
            return CLEAR
        zig = abs((x % 6) - 3)
        if y in (3 + zig // 2, 4 + zig // 2):
            return pick(FEA_Y, 0.75, x, y)
        return pick(WEAVE, 0.45 + 0.15 * ((x // 3 + y // 3) % 2) + (noise(x, y, 11) - 0.5) * 0.1, x, y)
    pt("loin", "all", loin)

    def feather(r_, y_):
        def fn(f, x, y, w, h):
            if f not in BROAD:
                return r_[1]
            half = (w / 2.0) * min(1.0, (h - y + 1) / (h * 0.6)) if y > h * 0.4 else w / 2.0
            if abs(x + 0.5 - w / 2.0) > max(0.9, half):
                return CLEAR
            return pick(r_, 0.85 - 0.5 * (y / h) if y_ else 0.5 + 0.4 * (y / h), x, y)
        return fn
    pt("feather", "all", feather(FEA_R, True))
    pt("hfeather", "all", lambda f, x, y, w, h: CLEAR if (f in BROAD and abs(x + 0.5 - w / 2.0) > (w / 2.0) * min(1.0, (h - y) / (h * 0.5))) else pick(FEA_R if y < h * 0.62 else FEA_Y, 0.7 - 0.2 * (x == w // 2), x, y))
    pt("quiver", "all", lambda f, x, y, w, h: pick(BAMBOO, 0.40 + 0.3 * (y % 12 < 2) + (noise(x, y, 13) - 0.5) * 0.12, x, y))
    pt("dart", "all", lambda f, x, y, w, h: pick(BAMBOO, 0.8, x, y))
    pt("dart_f", "all", lambda f, x, y, w, h: pick(FEA_R, 0.7 - 0.3 * (x == w // 2), x, y))
    pt("pipe", "all", lambda f, x, y, w, h: pick(BAMBOO, 0.45 + 0.3 * (y % 14 < 2) + (noise(x, y, 15) - 0.5) * 0.1 + (0.12 if x < 1 else 0), x, y))
    pt("pipe_band", "all", lambda f, x, y, w, h: pick(CORD, 0.6, x, y))

    def seed(f, x, y, w, h):
        e = (x - (w - 1) / 2.0) ** 2 + (y - (h - 1) / 2.0) ** 2
        if f in BROAD + ("east", "west") and e < 1.2:
            return H.rgb(14, 10, 10)                       # the black eye of a rosary pea
        return pick(SEED, 0.7 - 0.25 * (x > w - 2 or y > h - 2), x, y)
    pt("seed", "all", seed)

    def tad(f, x, y, w, h):
        if f == "north" or f == "up":
            e = (x - 1.0) ** 2 + (y - 1.0) ** 2
            if f == "up" and y < 2 and (x < 3 or x > w - 4):
                return H.rgb(250, 250, 240)
        return pick(rg([(14, 20, 24), (36, 50, 56), (74, 96, 100)]), 0.45 + 0.35 * (f == "up") + (noise(x, y, 17) - 0.5) * 0.12, x, y)
    pt("tadpole", "all", tad)
    pt("tad_tail", "all", lambda f, x, y, w, h: pick(rg([(14, 20, 24), (36, 50, 56), (74, 96, 100)]), 0.5, x, y))
    pt("armband", "all", lambda f, x, y, w, h: pick(FEA_Y, 0.8, x, y) if (x // 3) % 2 else H.rgb(10, 10, 14))
    pt("hair_tie", "all", lambda f, x, y, w, h: pick(FEA_R, 0.55 + 0.2 * (x % 2), x, y))
    return pt.T


def extras(r, g):
    HG = H.hang
    top, z0 = g.head_top, g.head_z0

    # hair: bangs over the forehead, two braids over the shoulders, a high ponytail through a red tie, a macaw feather
    r.put("head", HG("bang_a", -0.75, top + 0.3, z0 - 0.4, 0, -4))
    r.add("hair_side_r", "head", [-4.2, top - 1.5, 0], [HG("strand_m", -5.4, top - 1.2, -0.6, -5, 0), HG("strand_s", -5.4, top - 1.2, 1.4, -6, 3)], mirror=True)
    r.put("head", cube("hair_tie", [-0.75, top - 2.2, 3.3]))
    back = [HG(n, x, top - 1.8, 3.9 + 0.3 * (i % 2), x * 2.2, 8 + 3 * (i % 3)) for i, (n, x) in enumerate(zip(("strand_m", "strand_l", "strand_m"), (-1.2, 0.0, 1.2)))]
    r.add("hair_back", "head", [0, top - 1.8, 3.6], back)
    r.put("head", cube("hfeather", [-4.7, top - 1.0, z0 + 3.2], pivot=[-4.3, top - 1.0, z0 + 3.4], rotation=[0, 0, 14]),
          cube("hfeather", [-5.2, top - 2.0, z0 + 4.2], pivot=[-4.8, top - 2.0, z0 + 4.4], rotation=[0, 0, 26]))

    # leaf bandeau, cord belt, loincloth with macaw feathers, seed necklace, armbands
    cw, cd = A.S("chest")[0], A.S("chest")[2]
    r.put("body", cube("leaf_top", [-3.25, g.chest_y0 + 0.25, g.chest_front - 0.25]), cube("belt", [-3.25, g.hips_y0 + 1.75, -2.5]))
    loin = [HG("loin", -1.5, g.hips_y0 + 1.75, -2.8, 0, -4)]
    for i in range(4):
        x = -1.5 + i * 0.75
        loin.append(HG("feather", x, g.hips_y0 - 2.25, -3.0 + 0.1 * (i % 2), 0, -4))
    r.add("loin", "body", [0, g.hips_y0 + 1.75, -2.8], loin)
    beads = []
    for x in (-2.4, -1.6, -0.8, 0.0, 0.8, 1.6, 2.4):
        beads.append(cube("seed", [x - 0.375, g.chest_top - 0.8 - 0.7 * (1 - (x / 2.8) ** 2), g.chest_front - 0.6]))
    for sx in (-1, 1):
        beads.append(cube("seed", [sx * 2.9 - 0.375, g.chest_top - 0.8, g.chest_front + 0.5]))
    r.put("body", *beads)
    r.put("arm_r", cube("armband", [-g.shoulder_x - 1.0, g.shoulder_y - 2.4, -1.0]))

    # blowpipe slung across her back with a quiver of darts; three tadpoles cling on
    gear = []
    cy = g.chest_y0 - 0.5
    gear.append(cube("pipe", [-0.375, cy - 7.5, 2.4], pivot=[0, cy, 2.4], rotation=[0, 0, 32]))
    for dy in (-3.0, 3.0):
        gear.append(cube("pipe_band", [-0.625, cy + dy - 0.25, 2.2], pivot=[0, cy, 2.4], rotation=[0, 0, 32]))
    qx, qy = 1.2, g.mid_y0 - 0.5
    pv = [qx + 1.0, qy, 3.1]
    gear.append(cube("quiver", [qx, qy, 2.1], pivot=pv, rotation=[0, 0, -12]))
    for i, dx in enumerate((0.35, 0.95, 1.55)):
        gear.append(cube("dart", [qx + dx, qy + 4.5, 2.7], pivot=pv, rotation=[0, 0, -12]))
        gear.append(cube("dart_f", [qx + dx - 0.25, qy + 7.0, 2.7], pivot=pv, rotation=[0, 0, -12]))
    r.add("back_gear", "body", [0, cy, 2.4], gear)
    tads = []
    for x, y in ((-2.7, g.mid_y0 + 0.4), (-0.6, g.mid_y0 + 2.0), (-3.0, g.chest_y0 + 0.6)):
        tads.append(cube("tadpole", [x - 0.6, y, 1.8]))
        tads.append(cube("tad_tail", [x - 0.125, y + 0.125, 3.55]))
    r.add("tadpoles", "body", [-2, g.mid_y0 + 1.5, 2], tads)


def build_model():
    m, g = A.build(SPEC, extras, NAME)
    build_model.g = g
    return m


TUNE = SimpleNamespace(crouch=12, step=14, hop_lift=6.0, throat_out=(0, -1.7, -2.3), puff_f=1.2, croak_crouch=24, sit_crouch=58,
                       tongue_len=5.5, arm_rest=(3, 0, 5), extra={})


def animations(an):
    g = build_model.g
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    OUT, REST = H.OUT_, H.REST
    wig = lambda amp, per=2.0: T((0, (0, 0, -amp)), (per / 2, (0, 0, amp)), (per, (0, 0, -amp)))
    TUNE.extra = {
        "idle": {"tadpoles": {"rotation": wig(3, 3.0)}},
        "walk": {"back_gear": {"rotation": T((0, (0, 0, 2)), (0.5, (0, 0, -2)), (1, (0, 0, 2)))}, "loin": {"rotation": T((0, (3, 0, 3)), (0.5, (3, 0, -3)), (1, (3, 0, 3)))}},
        "hop": {"back_gear": {"rotation": T((0, (0, 0, 0)), (0.3, (14, 0, 0)), (0.74, (-8, 0, 0)), (0.9, (0, 0, 0)))}, "tadpoles": {"rotation": wig(8, 0.9)},
                "loin": {"rotation": T((0, (0, 0, 0)), (0.3, (-24, 0, 0)), (0.74, (12, 0, 0)), (0.9, (0, 0, 0)))}},
        "swim": {"loin": {"rotation": hold(-40, 0, 0)}, "back_gear": {"rotation": hold(-60, 0, 0)}},
    }
    A.common_anims(an, g, TUNE)

    # warn: the aposematic display - up on straight legs, chest out, arms away from the body, slowly turning to show every spot
    lg, by = A.gait(g.T, g.Sh, 2.4, [(0, (-3, 6, 0, 0)), (2.4, (-3, 6, 0, 0))], [(0, (-3, 6, 0, 0)), (2.4, (-3, 6, 0, 0))])
    for s_ in ("r", "l"):
        lg["leg_" + s_]["position"] = by
    lg["body"] = {"position": by, "rotation": T((0, (-6, 24, 0)), (0.6, (-6, 0, 0)), (1.2, (-6, -24, 0)), (1.8, (-6, 0, 0)), (2.4, (-6, 24, 0)))}
    an.add("warn", 2.4, True, merge(
        lg,
        {"head": {"rotation": T((0, (-6, -24, 0)), (1.2, (-6, 24, 0)), (2.4, (-6, -24, 0)))},
         "throat": {"position": T((0, REST), (0.4, (0, -0.8, -1.0)), (0.9, REST), (1.6, (0, -0.8, -1.0)), (2.1, REST), (2.4, REST))},
         "back_gear": {"rotation": hold(0, 0, 0)}, "hair_back": {"rotation": T((0, (4, 0, 0)), (1.2, (-2, 0, 0)), (2.4, (4, 0, 0)))}},
        H.blink(1.8), sym("arm", hold(6, 0, 26)), sym("forearm", hold(-24, 0, 0)), sym("hand", hold(-14, 0, 0))))

    # climb: hands and feet take turns reaching up, the sticky pads do the rest
    cl = [(0, (-82, 118, 0, 0)), (0.5, (-38, 52, 0, 0)), (1.0, (-82, 118, 0, 0))]
    lg2, by2 = A.gait(g.T, g.Sh, 1.0, cl)
    for s_ in ("r", "l"):
        lg2["leg_" + s_]["position"] = by2
    lg2["body"] = {"position": by2, "rotation": hold(-10, 0, 0)}
    an.add("climb", 1.0, True, merge(
        lg2,
        {"head": {"rotation": hold(-24, 0, 0)}, "hair_back": {"rotation": hold(14, 0, 0)},
         "arm_r": {"rotation": T((0, (-170, 0, 8)), (0.5, (-110, 0, 8)), (1.0, (-170, 0, 8)))},
         "arm_l": {"rotation": T((0, (-110, 0, 8)), (0.5, (-170, 0, 8)), (1.0, (-110, 0, 8)))},
         "forearm_r": {"rotation": hold(-12, 0, 0)}, "forearm_l": {"rotation": hold(-12, 0, 0)},
         "back_gear": {"rotation": T((0, (0, 0, 2)), (0.5, (0, 0, -2)), (1, (0, 0, 2)))}, "tadpoles": {"rotation": wig(5, 1.0)}}))


def main():
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="croak", head_center=(0, 22.5), body_center=(0, 15.5), scale=14.0,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 150, 8),
                      ("warn", 0.6, -30, 8), ("climb", 0.25, -30, 8), ("hop", 0.4, -30, 10), ("sit", 1.0, -30, 10)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("croak", 0.6, 0, 0), ("tongue", 0.3, 0, 0), ("idle", 0, -30, 8)])


if __name__ == "__main__":
    main()
