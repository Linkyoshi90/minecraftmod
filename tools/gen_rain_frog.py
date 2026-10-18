#!/usr/bin/env python3
"""Desert Rain Frog - species definition (a small frog of the Bullywug family). Shared engine: anuran_lib.py.

    python tools/gen_rain_frog.py                    # write geo / animation / texture
    python tools/gen_rain_frog.py --preview x.png    # also render x.png, x_face.png, x_body.png, x_texture.png

Concept (after Breviceps macrops, the Namib desert rain frog): the smallest anuran, about 1.1 blocks. A round, squat,
velvety sand-pink body with short stubby limbs, pale toe pads, a huge sparkling black-ringed eye pair and a permanently
grumpy down-turned mouth. It burrows into dunes (sinks out of sight with only the eyes showing), drinks fog off its
woven poncho (dew beads on the fringe), and when frightened it puffs up into a ball and lets out a famous squeak.
Outfit: sand goggles pushed up on the head, a striped woven poncho with dew-bead fringe, a pebble pendant.
"""
import math
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import anuran_lib as A
from anuran_lib import rg, cube, mirror_cube, pick, noise, CLEAR, BROAD

HUMANOID = "--frog-face" not in sys.argv   # default: human-style face painted on the front of the head; --frog-face builds the old bulging-eye version
NAME = "desert_rain_frog" if HUMANOID else "desert_rain_frog_frogface"

SPEC = SimpleNamespace(legx=2.0, eye_x=2.5, eye_out=0.25, eye_rise=1.0, head_sink=1.0, head_fwd=0.25, chest_fwd=0.0,
                       mouth_y=0.75, throat_y=0.5, tymp_y=1.5, tymp_z=3.0)
SIZES = {
    "head": (8, 5, 6.5), "eye": (3.75, 3.5, 3.5), "eyelid": (3.75, 3.5, .125), "tymp": (.25, 1.75, 1.75), "mouth": (6, 1.5, .125),
    "tongue_a": (1, .5, 5), "tongue_b": (1.75, .5, 1.25), "throat": (4, 2.5, 3),
    "bang_a": (1.5, 2, .5), "bang_b": (1.25, 2.5, .5), "strand_s": (1.25, 3, .75), "strand_m": (1.25, 4, .75), "strand_l": (1.25, 5, .75),
    "hips": (7, 2.5, 6), "mid": (7.5, 3, 6), "chest": (7, 3, 5.5), "belly_f": (6.5, 3, 3.5), "belly_s": (3.5, 2.5, 5),
    "arm_up": (1.75, 2, 1.75), "forearm": (1.5, 2, 1.5), "palm": (2, 1, 1.25),
    "fing_a": (.5, .75, .5), "fing_b": (.5, 1, .5), "pad_h": (.75, .5, .75),
    "thigh": (3, 3, 3), "shin": (2, 2.5, 2), "heel": (2.5, .75, 2),
    "toe_a": (.5, .5, 1.25), "toe_b": (.5, .5, 1.75), "toe_c": (.5, .5, 2), "toe_d": (.5, .5, 1.5), "pad_t": (.75, .5, .75), "web_f": (2.5, .25, 1.5),
}


if HUMANOID:
    SIZES.update(head=(7.5, 6.25, 6.5), eyelid=(2.75, 3, .125), mouth=(2.25, 1.5, .125))
    SPEC.hum = dict(sep=2.25, y=3.1, a=4.9, b=5.4, mw=1.2)


def declare_parts():
    A.declare_base(SIZES)
    for args in [
        ("lens", 2.25, .75, 2.25), ("rim", 2.75, .5, 2.75), ("gog_strap", 8.75, .75, .5), ("gog_side", .5, .75, 5),
        ("poncho", 8.5, 5, 7), ("fringe", .75, 2, .25), ("dew", .75, .75, .75), ("pebble", 1.25, 1.25, .5), ("cord", 1.5, .5, .5),
    ]:
        H.part(*args)


PAL = A.new_pal(
    BACK=rg([(120, 84, 62), (166, 120, 88), (206, 160, 120), (232, 194, 150), (248, 220, 180)], nodither=True),
    BELLY=rg([(190, 150, 120), (220, 184, 150), (240, 212, 180), (250, 232, 204)], nodither=True),
    spots=dict(seed=55, cell=5, rmin=1.0, rmax=2.2, depth=0.22), belly_spots=True, side_split=0.52, gloss=1, lid=0.0, pupil=(0.82, 0.82),
    EYE=rg([(20, 12, 10), (40, 24, 14), (90, 56, 24), (160, 110, 40), (220, 170, 70)]),
)
PAL.EYE_OUT = H.rgb(24, 14, 10)
PAL.PUPIL = H.rgb(6, 4, 6)
PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE, PAL.NOSTRIL = H.rgb(78, 46, 36), H.rgb(130, 48, 56), H.rgb(224, 112, 120), H.rgb(70, 42, 34)
PAL.TONGUE_R = rg([(160, 64, 74), (204, 92, 100), (232, 124, 132), (248, 164, 170)])
PAL.BLUSH, PAL.BLUSH_L = H.rgb(236, 142, 130), H.rgb(244, 172, 156)
PAL.DRUM = rg([(120, 88, 66), (168, 128, 94), (206, 170, 128), (228, 196, 154)])
PAL.SAC = rg([(190, 120, 100), (224, 156, 128), (244, 190, 160), (252, 216, 190)])
PAL.PAD = rg([(170, 140, 110), (208, 180, 146), (236, 214, 180), (250, 240, 212)])
PAL.HAIR = rg([(110, 72, 48), (150, 104, 70), (190, 142, 96), (222, 176, 122), (240, 204, 148), (250, 226, 178)])
PAL._mouth_y, PAL.nostril_dx, PAL.nostril_up = SPEC.mouth_y, 2.4, 3
if HUMANOID:
    PAL.hpupil = (0.78, 0.78)

SAND = rg([(120, 92, 60), (170, 136, 92), (208, 176, 126), (232, 208, 160)])
TERRA = rg([(110, 46, 32), (160, 72, 46), (200, 104, 66)])
DEW = rg([(110, 160, 204), (170, 210, 238), (226, 244, 255)])
BRASS = rg([(96, 68, 28), (160, 120, 52), (214, 170, 84), (244, 216, 140)])
GLASS = rg([(60, 90, 100), (110, 160, 168), (190, 226, 224)])
STRAP = rg([(52, 36, 28), (90, 64, 44), (128, 96, 66)])
PEBBLE = rg([(70, 66, 70), (110, 104, 108), (156, 148, 148), (196, 188, 184)])


def paint():
    pt = A.paint_base(PAL, curve=-3.0, fringe=4 if HUMANOID else 0, hum=getattr(SPEC, "hum", None))

    def lens(f, x, y, w, h):
        if f == "up":
            return pick(GLASS, 0.55 + 0.4 * (x < 3 and y < 3) + (noise(x, y, 5) - 0.5) * 0.06, x, y)
        return pick(GLASS, 0.35, x, y)
    pt("lens", "all", lens)
    pt("rim", "all", lambda f, x, y, w, h: pick(BRASS, 0.6 + (noise(x, y, 7) - 0.5) * 0.2 + 0.2 * (f == "up"), x, y))
    pt("gog_strap", "all", lambda f, x, y, w, h: pick(STRAP, 0.5 + 0.15 * (x % 6 == 0), x, y))
    pt("gog_side", "all", lambda f, x, y, w, h: pick(STRAP, 0.5, x, y))

    def poncho(f, x, y, w, h):
        if f in ("up", "down"):
            return SAND[1]
        if y > h - 3:
            return TERRA[1] if (x // 2) % 2 else TERRA[0]                # woven hem band
        if y in (h // 2 - 1, h // 2):
            return TERRA[2] if (x // 3) % 2 else SAND[3]                  # a zig band across the middle
        return pick(SAND, 0.50 + 0.14 * ((x // 2 + y // 2) % 2) + (noise(x, y, 11) - 0.5) * 0.10, x, y)
    pt("poncho", "all", poncho)
    pt("fringe", "all", lambda f, x, y, w, h: pick(SAND, 0.62 - 0.2 * (y > h - 3), x, y))
    pt("dew", "all", lambda f, x, y, w, h: pick(DEW, 0.45 + 0.5 * (x < 2 and y < 2) - 0.15 * (y > h - 2), x, y))
    pt("pebble", "all", lambda f, x, y, w, h: pick(PEBBLE, 0.55 + (0.3 if (x, y) == (1, 1) else 0) + (noise(x, y, 13) - 0.5) * 0.2, x, y))
    pt("cord", "all", lambda f, x, y, w, h: pick(STRAP, 0.6, x, y))
    return pt.T


def extras(r, g):
    HG = H.hang
    top = g.head_top
    z0 = g.head_z0
    hw = A.S("head")[0]

    # short sandy hair: a tuft between the eyes, curls behind
    r.put("head", HG("bang_a", -0.75, top + 0.4, z0 - 0.3, 2, -6))
    r.add("hair_side_r", "head", [-3.5, top - 1.0, 0], [HG("strand_s", -4.7, top - 1.0, 0.0, -6, 0), HG("strand_m", -4.7, top - 1.0, 2.2, -8, 3)], mirror=True)
    back = [HG(n, x, top - 1.0, 3.5 + 0.2 * (i % 2), x * 1.3, 4 + 3 * (i % 2)) for i, (n, x) in
            enumerate(zip(("strand_s", "strand_m", "strand_s", "strand_m", "strand_s"), (-3.2, -1.6, 0.0, 1.6, 3.2)))]
    r.add("hair_back", "head", [0, top - 1.0, 3.2], back)

    # sand goggles pushed up on the head behind the eyes
    gl = []
    for sx in (-1, 1):
        gl.append(cube("rim", [sx * 1.9 - 1.375, top + 0.45, z0 + 3.1]))
        gl.append(cube("lens", [sx * 1.9 - 1.125, top + 0.95, z0 + 3.35]))
    gl.append(cube("gog_strap", [-4.375, top - 1.2, z0 + 6.85]))
    for sx in (-1, 1):
        gl.append(cube("gog_side", [sx * 4.3 - 0.25, top - 1.2, z0 + 1.6]))
    r.put("head", *gl)

    # woven poncho with a dew-bead fringe, and a pebble pendant
    r.put("body", cube("poncho", [-4.25, g.chest_y0 - 2.25, g.chest_front - 0.75]))
    hem = g.chest_y0 - 1.75
    fr = []
    for i in range(7):
        x = -3.6 + i * 1.2
        fr += [HG("fringe", x - 0.375, hem, g.chest_front - 0.7, 0, -2), HG("dew", x - 0.375, hem - 2.0, g.chest_front - 0.8, 0, -2)]
    for i in range(6):
        x = -3.2 + i * 1.3
        fr += [HG("fringe", x - 0.375, hem, 3.2, 0, 2), HG("dew", x - 0.375, hem - 2.0, 3.25, 0, 2)]
    r.add("poncho", "body", [0, hem, 0], fr)
    r.add("pendant", "body", [0, g.chest_top - 1.0, g.chest_front - 0.5], [HG("cord", -0.75, g.chest_top - 0.5, g.chest_front - 0.9), HG("pebble", -0.625, g.chest_top - 1.0, g.chest_front - 1.0)])


def build_model():
    m, g = A.build(SPEC, extras, NAME)
    build_model.g = g
    return m


TUNE = SimpleNamespace(crouch=7, step=11, hop_lift=2.5, throat_out=(0, -1.3, -2.0), puff_f=1.2, croak_crouch=12, sit_crouch=44,
                       tongue_len=5.0, arm_rest=(2, 0, 4), extra={})


def animations(an):
    g = build_model.g
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    OUT, REST = H.OUT_, H.REST
    sway = lambda amp, per=4.0: T((0, (0, 0, 0)), (per / 4, (amp, 0, 0)), (per / 2, (0, 0, 0)), (3 * per / 4, (-amp, 0, 0)), (per, (0, 0, 0)))
    TUNE.extra = {
        "idle": {"poncho": {"rotation": sway(2)}},
        "walk": {"poncho": {"rotation": T((0, (3, 0, 4)), (0.5, (3, 0, -4)), (1, (3, 0, 4)))}},
        "hop": {"poncho": {"rotation": T((0, (0, 0, 0)), (0.3, (-26, 0, 0)), (0.74, (14, 0, 0)), (0.9, (0, 0, 0)))}},
        "swim": {"poncho": {"rotation": hold(-50, 0, 0)}},
        "croak": {"poncho": {"rotation": sway(3, 3.0)}},
        "sit": {"poncho": {"rotation": hold(4, 0, 0)}},
    }
    A.common_anims(an, g, TUNE)

    # scream: the famous squeak - up on straightened legs, puffed into a ball, mouth wide, arms flung out
    lg, by = A.gait(g.T, g.Sh, 1.6, [(0, (-2, 4, 0, 0)), (1.6, (-2, 4, 0, 0))], [(0, (-2, 4, 0, 0)), (1.6, (-2, 4, 0, 0))])
    hop = T((0, (0, 0, 0)), (0.12, (0, 1.2, 0)), (0.24, (0, 0, 0)), (1.6, (0, 0, 0)))
    for s_ in ("r", "l"):
        lg["leg_" + s_]["position"] = by
    lg["body"] = {"position": hop, "rotation": T((0, (0, 0, 0)), (0.3, (-8, 0, 3)), (0.45, (-8, 0, -3)), (0.6, (-8, 0, 3)), (0.75, (-8, 0, -3)), (0.9, (-8, 0, 3)), (1.1, (-8, 0, -3)), (1.6, (0, 0, 0)))}
    an.add("scream", 1.6, True, merge(
        lg,
        {"mouth": {"position": T((0, REST), (0.1, OUT), (1.2, OUT), (1.4, REST), (1.6, REST))},
         "belly": {"position": T((0, REST), (0.15, (0, 0, -1.6)), (1.2, (0, 0, -1.6)), (1.6, REST))},
         "belly_r": {"position": T((0, REST), (0.15, (-1.6, 0, 0)), (1.2, (-1.6, 0, 0)), (1.6, REST))},
         "belly_l": {"position": T((0, REST), (0.15, (1.6, 0, 0)), (1.2, (1.6, 0, 0)), (1.6, REST))},
         "throat": {"position": T((0, REST), (0.15, (0, -1.0, -1.4)), (1.2, (0, -1.0, -1.4)), (1.6, REST))},
         "head": {"rotation": T((0, (0, 0, 0)), (0.15, (-22, 0, 0)), (1.2, (-22, 0, 0)), (1.6, (0, 0, 0)))},
         "poncho": {"rotation": T((0, (0, 0, 0)), (0.15, (-14, 0, 0)), (1.2, (-14, 0, 0)), (1.6, (0, 0, 0)))},
         "hair_back": {"rotation": T((0, (0, 0, 0)), (0.15, (-14, 0, 0)), (1.6, (0, 0, 0)))}},
        sym("arm", T((0, (2, 0, 4)), (0.15, (-30, 0, 64)), (1.2, (-30, 0, 64)), (1.6, (2, 0, 4)))), sym("forearm", hold(-16, 0, 0))))

    # burrow: kick sand with the hind feet and sink until only the eyes show
    c = 30
    lg2, by2 = A.gait(g.T, g.Sh, 1.0, [(0, (-c, 2 * c, 0, 0)), (0.5, (-c + 22, 2 * c - 30, 0, 0)), (1.0, (-c, 2 * c, 0, 0))])
    for s_ in ("r", "l"):
        lg2["leg_" + s_].pop("position", None)
    lg2["body"] = {"rotation": hold(12, 0, 0)}
    sink = T((0, (0, 0, 0)), (0.2, (0, -0.4, 0)), (3.0, (0, -8.5, 0)), (3.4, (0, -8.5, 0)))
    an.add("burrow", 3.4, False, merge(
        {"root": {"position": sink}},
        {k: {kk: vv for kk, vv in v.items()} for k, v in lg2.items()},
        {"head": {"rotation": hold(-10, 0, 0)}, "poncho": {"rotation": hold(-10, 0, 0)}},
        sym("arm", T((0, (-60, 0, 6)), (0.25, (-10, 0, 6)), (0.5, (-60, 0, 6)), (3.4, (-60, 0, 6)))),
        sym("forearm", hold(-24, 0, 0)), H.blink(1.2, 2.4)))

    # sip: tips her head back and licks the dew off the poncho fringe
    lg3, by3 = A.gait(g.T, g.Sh, 3.0, [(0, (-8, 16, 0, 0)), (3, (-8, 16, 0, 0))], [(0, (-8, 16, 0, 0)), (3, (-8, 16, 0, 0))])
    for s_ in ("r", "l"):
        lg3["leg_" + s_]["position"] = by3
    lg3["body"] = {"position": by3}
    lick = T((0, REST), (0.5, REST), (0.6, (0, 0, -2.4)), (0.75, REST), (1.4, REST), (1.5, (0, 0, -2.4)), (1.65, REST), (3.0, REST))
    an.add("sip", 3.0, True, merge(
        lg3,
        {"head": {"rotation": T((0, (0, 0, 0)), (0.4, (-28, 0, 0)), (2.0, (-28, 0, 0)), (2.6, (0, 0, 0)), (3, (0, 0, 0)))},
         "mouth": {"position": T((0, REST), (0.4, OUT), (2.0, OUT), (2.4, REST), (3, REST))},
         "tongue": {"position": lick}, "poncho": {"rotation": sway(2, 3.0)},
         "throat": {"position": T((0, REST), (0.8, (0, -0.7, -0.8)), (1.0, REST), (1.8, (0, -0.7, -0.8)), (2.0, REST), (3, REST))}},
        H.blink(2.6), sym("arm", hold(*TUNE.arm_rest)), sym("forearm", hold(-8, 0, 0))))


def main():
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="scream", head_center=(0, 14.5), body_center=(0, 9.0), scale=24.0,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("scream", 0.6, -28, 8), ("burrow", 1.6, -30, 10), ("hop", 0.4, -30, 10), ("sip", 1.0, -30, 8)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("scream", 0.6, 0, 0), ("sip", 0.7, 0, 0), ("idle", 0, -30, 8)])


if __name__ == "__main__":
    main()
