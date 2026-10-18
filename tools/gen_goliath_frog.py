#!/usr/bin/env python3
"""Goliath Frog - species definition (Bullywug family). Shared engine: anuran_lib.py on top of harpy_lib.py.

    python tools/gen_goliath_frog.py                    # write geo / animation / texture
    python tools/gen_goliath_frog.py --preview x.png    # also render x.png, x_face.png, x_body.png, x_texture.png

Concept (after the goliath frog of West African rapids, the largest frog alive): the river warden, a silent giant of about
2.3 blocks, the tallest and heaviest anuran. Dark olive granular skin with a warm orange-cream belly, a broad stern face
under heavy brows, kelp-green dreadlocks. She never croaks; she moves stones to build her nest pools. Outfit: stone
pauldrons capped with moss, stone gauntlets, a bandolier of river pebbles, a reed kilt on a rope belt, and a boulder
carried on her back in a rope sling. Signature moves: the earth-shaking stomp and lifting the boulder overhead.
Humanoid face.
"""
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import anuran_lib as A
from anuran_lib import rg, cube, mirror_cube, pick, noise, CLEAR, BROAD

NAME = "goliath_frog"

SPEC = SimpleNamespace(legx=3.25, eye_x=3.25, eye_out=0.25, eye_rise=1.0, head_sink=1.25, head_fwd=0.5, chest_fwd=0.0,
                       mouth_y=1.5, throat_y=0.5, tymp_y=2.5, tymp_z=4.5,
                       hum=dict(sep=3.25, y=5.0, a=6.8, b=7.0, mw=2.4))
SIZES = {
    "head": (11, 9, 9.5), "eyelid": (4, 4, .125), "mouth": (4, 2, .125), "tymp": (.25, 3, 3),
    "tongue_a": (1.75, .5, 7.5), "tongue_b": (3, .75, 2), "throat": (6, 3.5, 5),
    "bang_a": (2, 3, .5), "bang_b": (1.75, 4, .5), "strand_s": (1.75, 6, 1), "strand_m": (1.75, 10, 1), "strand_l": (1.75, 14, 1),
    "hips": (12, 3.5, 7.5), "mid": (9, 4, 6.5), "chest": (13, 6, 7.5), "belly_f": (8, 4, 3.5), "belly_s": (3.5, 4, 6),
    "arm_up": (3.5, 5.5, 3.5), "forearm": (3, 5, 3), "palm": (3.5, 1.75, 2.5),
    "fing_a": (.75, 1.5, .75), "fing_b": (.75, 2, .75), "pad_h": (1, .75, 1),
    "thigh": (6, 8, 6), "shin": (4, 8, 4), "heel": (5, 1.5, 4),
    "toe_a": (.75, 1.25, 4), "toe_b": (.75, 1.25, 5.5), "toe_c": (.75, 1.25, 6), "toe_d": (.75, 1.25, 4.5), "pad_t": (1, .75, 1), "web_f": (4, .25, 4.5),
}


def declare_parts():
    A.declare_base(SIZES)
    for args in [
        ("pauldron", 6, 3.5, 6), ("gauntlet", 4.5, 2.5, 3.5), ("sling", 1, 12, .75), ("pebble", 1.5, 1.25, 1),
        ("kilt", 4, 6, .5), ("kilt_s", .5, 6, 4), ("belt", 12.5, 1.5, 8), ("boulder", 9, 8, 7), ("strap", .75, 10, .5), ("disc", 2, 2, .5),
    ]:
        H.part(*args)


PAL = A.new_pal(
    BACK=rg([(18, 22, 10), (40, 50, 20), (76, 90, 34), (116, 130, 56), (160, 172, 92)], nodither=True),
    BELLY=rg([(110, 70, 26), (160, 112, 44), (200, 150, 70), (226, 184, 100)], nodither=True),
    spots=dict(seed=111, cell=9, rmin=2.5, rmax=5.0, depth=0.30), belly_spots=True,
    warts=dict(seed=113, cell=5, density=0.45, ramp=rg([(30, 40, 16), (60, 76, 28), (100, 118, 44), (146, 164, 70)])),
    side_split=0.55, gloss=1, lid=0.22,
    EYE=rg([(30, 18, 6), (70, 40, 10), (120, 76, 18), (176, 120, 30), (220, 170, 66)]),
)
PAL.EYE_OUT, PAL.PUPIL, PAL.hpupil = H.rgb(18, 14, 4), H.rgb(8, 6, 4), (0.55, 0.55)
PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE, PAL.NOSTRIL = H.rgb(20, 26, 10), H.rgb(110, 40, 40), H.rgb(214, 100, 100), H.rgb(20, 26, 10)
PAL.TONGUE_R = rg([(140, 56, 56), (186, 84, 80), (222, 116, 110), (244, 156, 150)])
PAL.BLUSH, PAL.BLUSH_L = None, None
PAL.DRUM = rg([(30, 36, 16), (60, 70, 28), (100, 112, 48), (140, 154, 72)])
PAL.SAC = rg([(150, 100, 40), (200, 150, 60), (230, 190, 100), (246, 220, 150)])
PAL.PAD = rg([(110, 100, 50), (160, 150, 84), (200, 192, 120), (230, 224, 160)])
PAL.HAIR = rg([(8, 24, 14), (18, 50, 26), (34, 80, 40), (60, 116, 60), (100, 156, 90), (150, 196, 130)])
PAL._mouth_y = SPEC.mouth_y

STONE = rg([(40, 42, 44), (70, 74, 76), (104, 108, 108), (140, 144, 142)])
MOSS = rg([(24, 50, 22), (44, 86, 34), (76, 124, 50), (112, 160, 72)])
RUSH = rg([(96, 76, 36), (140, 112, 54), (184, 154, 84), (220, 194, 122)])
ROPE = rg([(70, 50, 28), (112, 84, 48), (156, 122, 74)])
PEBBLE = rg([(60, 66, 74), (100, 108, 118), (146, 154, 164), (190, 196, 206)])


def stone(f, x, y, w, h, moss=True):
    t = 0.50 + (noise(x, y, 5) - 0.5) * 0.26 + 0.14 * (noise(x // 3, y // 3, 7) > 0.6) - 0.10 * (noise(x // 2, y // 2, 9) > 0.8)
    if moss and (f == "up" or y < 3) and noise(x // 2, y // 2, 11) > 0.35:
        return pick(MOSS, 0.5 + (noise(x, y, 13) - 0.5) * 0.4, x, y)
    return pick(STONE, t, x, y)


def paint():
    pt = A.paint_base(PAL, curve=-1.5, fringe=3, hum=SPEC.hum)
    pt("pauldron", "all", stone)
    pt("boulder", "all", stone)
    pt("gauntlet", "all", lambda f, x, y, w, h: stone(f, x, y, w, h, False) if y > 1 else ROPE[0])
    pt("disc", "all", lambda f, x, y, w, h: pick(STONE, 0.8 - 0.4 * (x < 2 or y < 2), x, y))
    pt("sling", "all", lambda f, x, y, w, h: pick(ROPE, 0.4 + 0.35 * (((x + y) // 2) % 2), x, y))
    pt("strap", "all", lambda f, x, y, w, h: pick(ROPE, 0.4 + 0.35 * (((x + y) // 2) % 2), x, y))
    pt("pebble", "all", lambda f, x, y, w, h: pick(PEBBLE, 0.55 + 0.3 * (x < 3 and y < 3) + (noise(x, y, 15) - 0.5) * 0.2, x, y))
    pt("belt", "all", lambda f, x, y, w, h: ROPE[0] if (y < 1 or y >= h - 1) else pick(ROPE, 0.5 + 0.3 * (((x + y) // 3) % 2) + (noise(x, y, 17) - 0.5) * 0.1, x, y))

    def kilt(f, x, y, w, h):
        if f in ("up", "down"):
            return RUSH[1]
        if y > h - 4 and noise(x, y, 19) > 0.45 + 0.1 * (h - 1 - y):
            return CLEAR
        return pick(RUSH, 0.48 + 0.2 * ((x // 2) % 2) + (noise(x, y, 21) - 0.5) * 0.1, x, y)
    pt("kilt", "all", kilt)
    pt("kilt_s", "all", kilt)
    return pt.T


def extras(r, g):
    HG = H.hang
    top, z0 = g.head_top, g.head_z0

    # kelp dreadlocks: a heavy fringe, long tresses over the chest, and short locks down the back above the boulder
    r.put("head", HG("bang_b", -4.0, top + 0.2, z0 - 0.3, 2, -3), HG("bang_a", -1.0, top + 0.3, z0 - 0.4, 0, -4), HG("bang_b", 2.25, top + 0.2, z0 - 0.3, -2, -3))
    side = [("strand_l", -6.4, top - 1.5, -1.0, -3, 0), ("strand_m", -6.4, top - 1.5, 1.5, -4, 2), ("strand_l", -6.4, top - 1.5, 3.6, -5, 4)]
    r.add("hair_side_r", "head", [-5.5, top - 1.5, 0], [HG(*s) for s in side], mirror=True)
    names = ("strand_s", "strand_m", "strand_s", "strand_m", "strand_s", "strand_m", "strand_s")
    back = [HG(n, x, top - 1.5, 5.4 + 0.3 * (i % 2), x * 1.2, 4 + 2 * (i % 3)) for i, (n, x) in enumerate(zip(names, (-4.8, -3.2, -1.6, 0.0, 1.6, 3.2, 4.8)))]
    r.add("hair_back", "head", [0, top - 1.5, 5.2], back)

    # stone pauldrons, stone gauntlets, a bandolier of river pebbles with a stone disc, a reed kilt on a rope belt
    r.put("arm_r", cube("pauldron", [-g.shoulder_x - 3.0, g.shoulder_y + 0.0, -3.0]))
    r.put("forearm_r", cube("gauntlet", [-g.shoulder_x - 2.25, g.shoulder_y - 5.5 - 5.0 + 1.5, -1.75]))
    sl = [cube("sling", [-0.5, g.chest_y0 - 2.5, g.chest_front - 0.6], pivot=[0, g.chest_y0 + 3.5, g.chest_front], rotation=[0, 0, -38])]
    for i, (dx, dy) in enumerate(((-3.6, -0.5), (-2.2, 1.6), (-0.8, 3.7), (0.6, 5.8))):
        sl.append(cube("pebble", [dx - 0.75, g.chest_y0 - 0.7 + dy - 0.6, g.chest_front - 1.0 - 0.1 * (i % 2)]))
    r.put("body", *sl, cube("disc", [-1.0, g.hips_y0 + 1.6, -4.4]))
    r.put("body", cube("belt", [-6.25, g.hips_y0 + 2.2, -4.0]))
    ky = g.hips_y0 + 2.2
    kilt = [HG("kilt", -4.0, ky, -4.3, 3, -5), HG("kilt", 0.0, ky, -4.3, -3, -5), HG("kilt", -4.0, ky, 3.8, 3, 5), HG("kilt", 0.0, ky, 3.8, -3, 5),
            HG("kilt_s", -6.8, ky, -2.0, -5, 0), HG("kilt_s", 6.3, ky, -2.0, 5, 0)]
    r.add("kilt", "body", [0, ky, 0], kilt)

    # the boulder in a rope sling on her back; two rope straps run over the shoulders
    by = g.mid_y0 - 1.5
    r.add("boulder", "body", [0, by + 4.0, 7.0], [cube("boulder", [-4.5, by, 4.0])])
    r.put("body", cube("strap", [-4.25, g.chest_y0 - 0.5, g.chest_front - 0.6]), cube("strap", [3.5, g.chest_y0 - 0.5, g.chest_front - 0.6]))


def build_model():
    m, g = A.build(SPEC, extras, NAME)
    build_model.g = g
    return m


TUNE = SimpleNamespace(crouch=8, step=12, hop_lift=3.0, throat_out=(0, -2.0, -3.0), puff_f=1.6, croak_crouch=18, sit_crouch=56,
                       tongue_len=7.5, arm_rest=(3, 0, 6), extra={})


def animations(an):
    g = build_model.g
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    OUT, REST = H.OUT_, H.REST
    sway = lambda amp, per=4.0: T((0, (0, 0, 0)), (per / 4, (amp, 0, 0)), (per / 2, (0, 0, 0)), (3 * per / 4, (-amp, 0, 0)), (per, (0, 0, 0)))
    TUNE.extra = {
        "idle": {"kilt": {"rotation": sway(1.5)}, "boulder": {"rotation": T((0, (0, 0, 0)), (2, (1.5, 0, 0)), (4, (0, 0, 0)))}},
        "walk": {"kilt": {"rotation": T((0, (0, 0, 3)), (0.5, (0, 0, -3)), (1, (0, 0, 3)))}, "boulder": {"rotation": T((0, (0, 0, 2)), (0.5, (0, 0, -2)), (1, (0, 0, 2)))}},
        "hop": {"kilt": {"rotation": T((0, (0, 0, 0)), (0.3, (-18, 0, 0)), (0.74, (10, 0, 0)), (0.9, (0, 0, 0)))}, "boulder": {"rotation": T((0, (0, 0, 0)), (0.3, (6, 0, 0)), (0.74, (-4, 0, 0)), (0.9, (0, 0, 0)))}},
        "swim": {"kilt": {"rotation": hold(-30, 0, 0)}, "boulder": {"rotation": hold(-50, 0, 0)}},
        "sit": {"kilt": {"rotation": hold(6, 0, 0)}},
    }
    A.common_anims(an, g, TUNE)
    pose = lambda deg: (-deg, 2 * deg, 0, 0)

    # stomp: lifts a foot high, brings it down so the ground shakes (body dips and rebounds, hair and boulder bounce)
    fr_r = [(0, pose(8)), (0.5, (-74, 96, 0, 0)), (0.75, (-74, 96, 0, 0)), (0.9, pose(2)), (1.0, pose(14)), (1.4, pose(6)), (1.8, pose(8))]
    fr_l = [(0, pose(8)), (1.8, pose(8))]
    lg, by = A.gait(g.T, g.Sh, 1.8, fr_r, fr_l)
    for s_ in ("r", "l"):
        lg["leg_" + s_]["position"] = by
    lg["body"] = {"position": by, "rotation": T((0, (0, 0, 0)), (0.5, (-4, 0, 3)), (0.75, (-6, 0, 3)), (0.9, (8, 0, 0)), (1.0, (10, 0, 0)), (1.4, (2, 0, 0)), (1.8, (0, 0, 0)))}
    an.add("stomp", 1.8, True, merge(
        lg,
        {"head": {"rotation": T((0, (0, 0, 0)), (0.6, (-10, 0, 0)), (0.9, (12, 0, 0)), (1.2, (4, 0, 0)), (1.8, (0, 0, 0)))},
         "mouth": {"position": T((0, REST), (0.85, REST), (0.9, OUT), (1.3, OUT), (1.4, REST), (1.8, REST))},
         "eyelids": {"position": T((0, REST), (0.88, REST), (0.9, OUT), (1.0, OUT), (1.06, REST), (1.8, REST))},
         "kilt": {"rotation": T((0, (0, 0, 0)), (0.5, (-14, 0, 0)), (0.95, (18, 0, 0)), (1.4, (0, 0, 0)), (1.8, (0, 0, 0)))},
         "boulder": {"rotation": T((0, (0, 0, 0)), (0.5, (-6, 0, 0)), (0.95, (8, 0, 0)), (1.2, (-3, 0, 0)), (1.8, (0, 0, 0)))},
         "hair_back": {"rotation": T((0, (0, 0, 0)), (0.5, (-6, 0, 0)), (0.95, (12, 0, 0)), (1.4, (0, 0, 0)), (1.8, (0, 0, 0)))}},
        sym("arm", T((0, (3, 0, 6)), (0.5, (-40, 0, 20)), (0.9, (14, 0, 12)), (1.8, (3, 0, 6)))), sym("forearm", T((0, (-8, 0, 0)), (0.5, (-60, 0, 0)), (0.9, (-14, 0, 0)), (1.8, (-8, 0, 0))))))

    # lift: reaches behind, lifts the boulder over her head, holds it, sets it down again
    lg2, by2 = A.gait(g.T, g.Sh, 3.2, [(0, pose(10)), (0.8, pose(26)), (1.8, pose(4)), (2.4, pose(4)), (3.2, pose(10))], [(0, pose(10)), (0.8, pose(26)), (1.8, pose(4)), (2.4, pose(4)), (3.2, pose(10))])
    for s_ in ("r", "l"):
        lg2["leg_" + s_]["position"] = by2
    lg2["body"] = {"position": by2, "rotation": T((0, (0, 0, 0)), (0.8, (14, 0, 0)), (1.8, (-14, 0, 0)), (2.4, (-14, 0, 0)), (3.2, (0, 0, 0)))}
    up = (0, 17.0, -7.0)
    an.add("lift", 3.2, True, merge(
        lg2,
        {"boulder": {"position": T((0, (0, 0, 0)), (0.8, (0, 0, 0)), (1.8, up), (2.4, up), (3.2, (0, 0, 0))),
                     "rotation": T((0, (0, 0, 0)), (0.8, (0, 0, 0)), (1.8, (-20, 0, 0)), (2.4, (-20, 0, 0)), (3.2, (0, 0, 0)))},
         "head": {"rotation": T((0, (0, 0, 0)), (0.8, (10, 0, 0)), (1.8, (-26, 0, 0)), (2.4, (-26, 0, 0)), (3.2, (0, 0, 0)))},
         "mouth": {"position": T((0, REST), (1.0, REST), (1.2, OUT), (2.3, OUT), (2.5, REST), (3.2, REST))},
         "kilt": {"rotation": T((0, (0, 0, 0)), (1.8, (-16, 0, 0)), (2.4, (-16, 0, 0)), (3.2, (0, 0, 0)))}},
        sym("arm", T((0, (3, 0, 6)), (0.8, (46, 0, 14)), (1.8, (-176, 0, 12)), (2.4, (-176, 0, 12)), (3.2, (3, 0, 6)))),
        sym("forearm", T((0, (-8, 0, 0)), (0.8, (-50, 0, 0)), (1.8, (-16, 0, 0)), (2.4, (-16, 0, 0)), (3.2, (-8, 0, 0)))), sym("hand", hold(-10, 0, 0))))


def main():
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="stomp", head_center=(0, 32.0), body_center=(0, 22.5), scale=9.8,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("stomp", 0.6, -30, 8), ("lift", 2.0, -30, 8), ("walk", 0.25, -35, 12), ("hop", 0.4, -30, 10)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("stomp", 1.0, 0, 0), ("tongue", 0.3, 0, 0), ("idle", 0, -30, 8)])


if __name__ == "__main__":
    main()
