#!/usr/bin/env python3
"""Natterjack Toad - species definition (Mucus Toad family). Shared engine: anuran_lib.py on top of harpy_lib.py.

    python tools/gen_natterjack.py                    # write geo / animation / texture
    python tools/gen_natterjack.py --preview x.png    # also render x.png, x_face.png, x_body.png, x_texture.png

Concept (after the natterjack, a heath-and-dune toad that runs instead of hopping, has a bright yellow stripe down its back
and a famously loud chorus): the dune courier, about 1.25 blocks. Sandy olive skin with red-brown warts and a bright yellow
stripe from the back of her head to her rump, a pale flecked belly, short legs and a huge throat sac. Outfit: a red-orange
scarf with a long tail, a short sand-coloured travel cloak, a satchel and a scroll tube on her back, cloth gaiters and a
shell necklace. Signature moves: the scampering `run`, the rolling `chorus` and `burrow`. Humanoid face.
"""
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import anuran_lib as A
from anuran_lib import rg, cube, mirror_cube, pick, noise, CLEAR, BROAD

NAME = "natterjack"

SPEC = SimpleNamespace(legx=2.25, eye_x=2.5, eye_out=0.25, eye_rise=1.0, head_sink=0.75, head_fwd=0.0, chest_fwd=0.0,
                       mouth_y=0.75, throat_y=0.5, tymp_y=1.8, tymp_z=3.0,
                       hum=dict(sep=2.5, y=3.3, a=5.0, b=5.4, mw=1.4))
SIZES = {
    "head": (8, 6.5, 7), "eyelid": (3, 3, .125), "mouth": (2.75, 1.5, .125), "tymp": (.25, 2, 2),
    "tongue_a": (1, .5, 5.5), "tongue_b": (1.75, .5, 1.5), "throat": (6, 3, 4),
    "bang_a": (1.5, 3, .5), "strand_s": (1.5, 5, .75), "strand_m": (1.5, 7, .75),
    "hips": (7.5, 2.5, 5), "mid": (6, 3, 4.5), "chest": (7.5, 4, 5), "belly_f": (5.5, 3, 3), "belly_s": (3, 3, 4),
    "arm_up": (2, 3, 2), "forearm": (1.75, 3, 1.75), "palm": (2.75, 1.25, 1.5),
    "fing_a": (.5, 1, .5), "fing_b": (.5, 1.25, .5), "pad_h": (.75, .5, .75),
    "thigh": (3.5, 3, 3.5), "shin": (2.25, 3, 2.25), "heel": (3.5, 1, 2.5),
    "toe_a": (.75, .75, 2.25), "toe_b": (.75, .75, 3), "toe_c": (.75, .75, 3.25), "toe_d": (.75, .75, 2.5), "pad_t": (1, .75, 1), "web_f": (3.25, .25, 2),
}


def declare_parts():
    A.declare_base(SIZES)
    for args in [
        ("collar", 8, 1.5, 5.75), ("scarf_tail", 1.5, 6, .5), ("cloak", 3, 4.5, .5), ("shell", .75, .75, .75),
        ("bag", 3.5, 3, 1.5), ("bag_flap", 3.75, 1, 1.75), ("strap", .75, 9, .5), ("tube", 1.25, 6, 1.25), ("tube_cap", 1.5, .5, 1.5),
        ("gaiter", 2.75, 1.5, 2.75), ("wart", 1, .75, 1),
    ]:
        H.part(*args)


PAL = A.new_pal(
    BACK=rg([(34, 44, 16), (70, 90, 30), (110, 134, 48), (156, 176, 80), (200, 216, 124)], nodither=True),
    BELLY=rg([(150, 140, 110), (194, 184, 148), (222, 214, 178), (240, 234, 204)], nodither=True),
    spots=dict(seed=131, cell=6, rmin=1.4, rmax=3.0, depth=0.32), belly_spots=True,
    warts=dict(seed=133, cell=4, density=0.3, ramp=rg([(90, 40, 20), (150, 70, 34), (196, 110, 60), (226, 150, 96)])),
    side_split=0.50, gloss=0, lid=0.10,
    EYE=rg([(40, 50, 6), (90, 110, 12), (150, 170, 24), (200, 220, 50), (236, 246, 120)]),
)
PAL.EYE_OUT, PAL.PUPIL = H.rgb(24, 30, 6), H.rgb(8, 8, 4)
PAL.hpupil_bitmap = ["########", "########", ".######."]                      # horizontal pupils
PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE, PAL.NOSTRIL = H.rgb(54, 46, 24), H.rgb(130, 48, 46), H.rgb(222, 108, 108), H.rgb(44, 38, 18)
PAL.TONGUE_R = rg([(150, 56, 56), (196, 84, 80), (228, 116, 110), (246, 156, 150)])
PAL.BLUSH, PAL.BLUSH_L = H.rgb(214, 132, 90), H.rgb(232, 164, 116)
PAL.DRUM = rg([(80, 66, 30), (124, 108, 54), (168, 152, 88), (208, 192, 128)])
PAL.SAC = rg([(150, 120, 70), (196, 166, 100), (226, 204, 140), (246, 232, 180)])
PAL.PAD = rg([(120, 100, 50), (170, 150, 84), (206, 190, 120), (232, 222, 156)])
PAL.HAIR = rg([(30, 24, 20), (54, 44, 34), (84, 68, 50), (120, 98, 72), (160, 134, 100), (200, 176, 136)])
PAL._mouth_y = SPEC.mouth_y

SUNSET = rg([(120, 36, 20), (180, 64, 30), (224, 104, 50), (244, 150, 84)])
SAND = rg([(120, 100, 66), (170, 146, 100), (210, 188, 136), (236, 220, 170)])
LEATHER = rg([(44, 30, 20), (78, 56, 36), (112, 84, 56), (148, 116, 80)])
STRIPE = rg([(200, 160, 20), (244, 206, 40), (255, 236, 110)])
SHELLC = rg([(190, 150, 140), (232, 200, 190), (252, 238, 228)])


def paint():
    pt = A.paint_base(PAL, curve=3.0, fringe=3, hum=SPEC.hum)

    def stripe(f, x, y, w, h):                                           # the natterjack's yellow back stripe
        if f in ("south", "up") and abs(x + 0.5 - w / 2.0) <= 1.1:
            return pick(STRIPE, 0.55 + 0.35 * (noise(x, y, 3) > 0.5), x, y)
        return None
    for p in ("hips", "mid", "chest"):
        pt(p, ("south", "up"), stripe)

    def cloth(ramp_, seed):
        def fn(f, x, y, w, h):
            if f in ("up", "down"):
                return ramp_[1]
            if y > h - 3 and f in BROAD and noise(x, y, seed) > 0.5:
                return CLEAR
            return pick(ramp_, 0.50 + 0.14 * (((x // 2) + (y // 2)) % 2) + (noise(x, y, seed + 1) - 0.5) * 0.10, x, y)
        return fn
    pt("collar", "all", lambda f, x, y, w, h: pick(SUNSET, 0.55 + 0.18 * (((x // 2) + (y // 2)) % 2), x, y))
    pt("scarf_tail", "all", lambda f, x, y, w, h: CLEAR if (f in BROAD and y >= h - 2 and (x + y) % 3 == 0) else pick(SUNSET, 0.55 + 0.2 * ((y // 2) % 2), x, y))
    pt("cloak", "all", cloth(SAND, 5))
    pt("shell", "all", lambda f, x, y, w, h: pick(SHELLC, 0.45 + 0.35 * (((x + y) // 2) % 2), x, y))
    pt("bag", "all", lambda f, x, y, w, h: pick(LEATHER, 0.55 + (noise(x, y, 9) - 0.5) * 0.14, x, y))
    pt("bag_flap", "all", lambda f, x, y, w, h: LEATHER[0] if y >= h - 1 else pick(LEATHER, 0.65 + (noise(x, y, 11) - 0.5) * 0.12, x, y))
    pt("strap", "all", lambda f, x, y, w, h: pick(LEATHER, 0.5 + 0.15 * (y % 4 == 0), x, y))
    pt("tube", "all", lambda f, x, y, w, h: pick(LEATHER, 0.5 + 0.2 * (y % 8 < 2) + (noise(x, y, 13) - 0.5) * 0.1, x, y))
    pt("tube_cap", "all", lambda f, x, y, w, h: pick(SUNSET, 0.5, x, y))
    pt("gaiter", "all", lambda f, x, y, w, h: pick(SAND, 0.45 + 0.2 * (y % 3 == 0), x, y))
    pt("wart", "all", lambda f, x, y, w, h: pick(PAL.warts["ramp"], 0.9 if (f == "up" or (x < 2 and y < 2)) else 0.4, x, y))
    return pt.T


def extras(r, g):
    HG = H.hang
    top, z0 = g.head_top, g.head_z0

    # windswept sandy hair
    r.put("head", HG("bang_a", -0.75, top + 0.3, z0 - 0.3, 0, -5))
    r.add("hair_side_r", "head", [-4.2, top - 1.2, 0], [HG("strand_s", -4.9, top - 1.1, -0.2, -4, 0), HG("strand_m", -4.9, top - 1.1, 1.6, -6, 2)], mirror=True)
    r.add("hair_back", "head", [0, top - 1.2, 3.3], [HG(n, x, top - 1.1, 3.8 + 0.25 * (i % 2), x * 1.5, 6 + 3 * (i % 3)) for i, (n, x) in enumerate(zip(("strand_s", "strand_m", "strand_s", "strand_m"), (-2.8, -0.9, 0.9, 2.8)))])

    # collar, scarf tail, short travel cloak, shell necklace
    r.put("body", cube("collar", [-4.0, g.chest_top - 1.5, g.chest_front - 0.4]))
    r.add("scarf_tail", "body", [3.0, g.chest_top - 0.8, 2.0], [HG("scarf_tail", 2.25, g.chest_top - 0.8, 2.4, 0, 6)])
    r.add("cloak_r", "body", [-1.5, g.chest_top - 0.8, 3.0], [HG("cloak", -3.25, g.chest_top - 0.8, 2.85, 0, 3)], mirror=True)
    sh = []
    for x in (-2.0, -1.0, 0.0, 1.0, 2.0):
        sh.append(cube("shell", [x - 0.375, g.chest_top - 1.9 - 0.4 * (1 - (x / 2.4) ** 2), g.chest_front - 0.7]))
    r.put("body", *sh)

    # satchel on a strap across the chest, scroll tube on her back
    r.put("body", cube("strap", [-0.4, g.chest_y0 - 1.5, g.chest_front - 0.5], pivot=[0, g.chest_y0 + 3.0, g.chest_front], rotation=[0, 0, 32]))
    r.add("bag", "body", [4.4, g.hips_y0 + 2.0, 0.2], [HG("bag", 2.8, g.hips_y0 + 1.9, -0.6), HG("bag_flap", 2.7, g.hips_y0 + 2.0, -0.7)])
    cy = g.mid_y0 + 0.5
    r.put("body", cube("tube", [-0.625, cy, 2.9], pivot=[0, cy + 3, 3.5], rotation=[0, 0, -30]), cube("tube_cap", [-0.75, cy + 6, 2.8], pivot=[0, cy + 3, 3.5], rotation=[0, 0, -30]))
    r.put("shin_r", cube("gaiter", [-SPEC.legx - 1.375, g.ankle_y + 1.4, 0.5 - 1.375]))
    cx = -SPEC.legx
    r.put("leg_r", cube("wart", [cx - 2.25, g.knee_y + 1.2, 0.3]), cube("wart", [cx - 2.25, g.knee_y + 2.4, -0.9]))
    r.put("arm_r", cube("wart", [-g.shoulder_x - 1.5, g.shoulder_y - 1.8, 0.2]))


def build_model():
    m, g = A.build(SPEC, extras, NAME)
    build_model.g = g
    return m


TUNE = SimpleNamespace(crouch=14, step=18, hop_lift=3.0, throat_out=(0, -2.2, -3.2), puff_f=1.4, croak_crouch=24, sit_crouch=58,
                       tongue_len=5.0, arm_rest=(3, 0, 5), extra={})


def animations(an):
    g = build_model.g
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    OUT, REST = H.OUT_, H.REST
    sway = lambda amp, per=4.0: T((0, (0, 0, 0)), (per / 4, (amp, 0, 0)), (per / 2, (0, 0, 0)), (3 * per / 4, (-amp, 0, 0)), (per, (0, 0, 0)))
    TUNE.extra = {
        "idle": {"scarf_tail": {"rotation": sway(6, 3.0)}, **sym("cloak", sway(2)), "bag": {"rotation": sway(3)}},
        "walk": {"scarf_tail": {"rotation": T((0, (10, 0, 4)), (0.5, (20, 0, -4)), (1, (10, 0, 4)))}, **sym("cloak", T((0, (4, 0, 2)), (0.5, (8, 0, -2)), (1, (4, 0, 2)))), "bag": {"rotation": T((0, (0, 0, 5)), (0.5, (0, 0, -5)), (1, (0, 0, 5)))}},
        "hop": {"scarf_tail": {"rotation": T((0, (0, 0, 0)), (0.3, (-50, 0, 0)), (0.74, (20, 0, 0)), (0.9, (0, 0, 0)))}, **sym("cloak", T((0, (0, 0, 0)), (0.3, (-30, 0, 0)), (0.74, (14, 0, 0)), (0.9, (0, 0, 0))))},
        "swim": {"scarf_tail": {"rotation": hold(-60, 0, 0)}, **sym("cloak", hold(-60, 0, 0))},
        "croak": {"scarf_tail": {"rotation": sway(5, 3.0)}},
        "sit": {**sym("cloak", hold(8, 0, 0))},
    }
    A.common_anims(an, g, TUNE)
    pose = lambda deg: (-deg, 2 * deg, 0, 0)

    # run: a low, quick scamper on short legs instead of hops; scarf and cloak streaming behind
    fr = [(0, (-44, 78, 0, 0)), (0.25, (14, 12, 0.4, 0)), (0.5, (-44, 78, 0, 0))]
    lg, by = A.gait(g.T, g.Sh, 0.5, fr)
    for s_ in ("r", "l"):
        lg["leg_" + s_]["position"] = by
    lg["body"] = {"position": by, "rotation": hold(26, 0, 0)}
    an.add("run", 0.5, True, merge(
        lg,
        {"head": {"rotation": hold(-22, 0, 0)}, "hair_back": {"rotation": hold(30, 0, 0)},
         "scarf_tail": {"rotation": T((0, (-52, 0, 4)), (0.25, (-60, 0, -4)), (0.5, (-52, 0, 4)))}, **sym("cloak", hold(-44, 0, 0)), "bag": {"rotation": T((0, (0, 0, 16)), (0.25, (0, 0, -16)), (0.5, (0, 0, 16)))}},
        {"arm_r": {"rotation": T((0, (40, 0, 0)), (0.25, (-30, 0, 0)), (0.5, (40, 0, 0)))}, "arm_l": {"rotation": T((0, (-30, 0, 0)), (0.25, (40, 0, 0)), (0.5, (-30, 0, 0)))}},
        sym("forearm", hold(-40, 0, 0))))

    # chorus: the huge sac stays swollen and flutters in a rolling trill, eyes shut
    keys = [(0, REST)]
    for i in range(10):
        t0 = 0.3 + i * 0.26
        keys += [(t0, (0, -2.2, -3.2)), (t0 + 0.13, (0, -1.7, -2.5))]
    keys += [(3.1, (0, -2.2, -3.2)), (3.4, REST), (3.5, REST)]
    lg2, by2 = A.gait(g.T, g.Sh, 3.5, [(0, pose(8)), (3.5, pose(8))], [(0, pose(8)), (3.5, pose(8))])
    for s_ in ("r", "l"):
        lg2["leg_" + s_]["position"] = by2
    lg2["body"] = {"position": by2, "rotation": T((0, (0, 0, 0)), (0.3, (-10, 0, 0)), (3.1, (-10, 0, 0)), (3.5, (0, 0, 0)))}
    an.add("chorus", 3.5, True, merge(
        lg2,
        {"throat": {"position": T(*keys)}, "head": {"rotation": T((0, (0, 0, 0)), (0.3, (-30, 0, 0)), (3.1, (-30, 0, 0)), (3.5, (0, 0, 0)))},
         "eyelids": {"position": T((0, REST), (0.3, OUT), (3.1, OUT), (3.5, REST))},
         "belly": {"position": T((0, REST), (0.3, (0, 0, -0.9)), (3.1, (0, 0, -0.9)), (3.5, REST))},
         "scarf_tail": {"rotation": sway(5, 1.0)}, **sym("cloak", sway(2, 1.0))},
        sym("arm", hold(3, 0, 5)), sym("forearm", hold(-8, 0, 0))))

    # burrow: the hind legs shovel and she backs down into the sand until only her eyes show
    c = 30
    lg3, _ = A.gait(g.T, g.Sh, 1.0, [(0, (-c, 2 * c, 0, 0)), (0.5, (-c + 22, 2 * c - 30, 0, 0)), (1.0, (-c, 2 * c, 0, 0))])
    for s_ in ("r", "l"):
        lg3["leg_" + s_].pop("position", None)
    lg3["body"] = {"rotation": hold(-8, 0, 0)}
    sink = T((0, (0, 0, 0)), (0.2, (0, -0.4, 0)), (3.0, (0, -11.0, 0)), (3.4, (0, -11.0, 0)))
    an.add("burrow", 3.4, False, merge(
        {"root": {"position": sink}}, lg3,
        {"head": {"rotation": hold(-6, 0, 0)}, "scarf_tail": {"rotation": hold(-20, 0, 0)}, **sym("cloak", hold(-16, 0, 0))},
        sym("arm", T((0, (-50, 0, 6)), (0.25, (-8, 0, 6)), (0.5, (-50, 0, 6)), (3.4, (-50, 0, 6)))), sym("forearm", hold(-24, 0, 0)), H.blink(1.2, 2.4)))


def main():
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="chorus", head_center=(0, 17.0), body_center=(0, 11.0), scale=16.5,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("run", 0.1, -35, 10), ("chorus", 1.0, -28, 8), ("burrow", 1.6, -30, 10), ("sit", 1.0, -30, 10)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("chorus", 1.0, 0, 0), ("tongue", 0.3, 0, 0), ("idle", 0, -30, 8)])


if __name__ == "__main__":
    main()
