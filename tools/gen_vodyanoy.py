#!/usr/bin/env python3
"""Vodyanoy - species definition (the first anuran man). Shared engine: anuran_lib.py on top of harpy_lib.py.

    python tools/gen_vodyanoy.py                    # write geo / animation / texture
    python tools/gen_vodyanoy.py --preview x.png    # also render x.png, x_face.png, x_body.png, x_texture.png

Concept (the Slavic river spirit, a frog-faced old man of the mill pools, after the common frog Rana temporaria): a
burly frog-man, about 1.9 blocks. Dark olive skin mottled brown, a muddy belly, a long beard and hair of green pondweed,
bushy brows and a drooping moustache. A fisherman's reed hat, a rope belt with a fish tied to it. Male: no bust, a broad
chest and a round belly. Signature move: `pull` (he reaches up and drags a swimmer under). He is never born, so he
has no tadpole or froglet models.
"""
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import anuran_lib as A
from anuran_lib import rg, cube, mirror_cube, pick, noise, CLEAR, BROAD

NAME = "vodyanoy"

SPEC = SimpleNamespace(legx=2.75, eye_x=3.0, eye_out=0.25, eye_rise=1.0, head_sink=1.0, head_fwd=0.25, chest_fwd=0.0,
                       mouth_y=1.25, throat_y=0.5, tymp_y=2.0, tymp_z=4.0,
                       hum=dict(sep=3.0, y=4.4, a=5.0, b=5.0, mw=2.4))
SIZES = {
    "head": (10, 8, 8.5), "eyelid": (3.5, 3.5, .125), "mouth": (3.5, 1.75, .125), "tymp": (.25, 3.5, 3.5),
    "tongue_a": (1.5, .5, 7), "tongue_b": (2.5, .75, 1.75), "throat": (6, 3.5, 4.5),
    "strand_s": (1.75, 6, 1), "strand_m": (1.75, 9, 1), "strand_l": (1.75, 12, 1),
    "hips": (10, 3, 6.5), "mid": (9, 3.5, 7), "chest": (12, 5, 7), "belly_f": (8, 4, 4), "belly_s": (3.5, 3.5, 5),
    "arm_up": (3, 4.5, 3), "forearm": (2.75, 4, 2.75), "palm": (3, 1.5, 2),
    "fing_a": (.5, 1.5, .5), "fing_b": (.5, 1.75, .5), "pad_h": (.75, .75, .75),
    "thigh": (5, 7, 5), "shin": (3.5, 7, 3.5), "heel": (4.5, 1.25, 3.5),
    "toe_a": (.75, 1, 3.5), "toe_b": (.75, 1, 5), "toe_c": (.75, 1, 5.5), "toe_d": (.75, 1, 4), "pad_t": (1, .75, 1), "web_f": (4, .25, 4),
}


def declare_parts():
    A.declare_base(SIZES)
    for args in [
        ("beard", 1.5, 7, .75), ("beard_s", 1.5, 5, .75), ("stache", 2.5, 1, .5), ("brow", 3, .75, .5),
        ("hat_brim", 12, .5, 11), ("hat_top", 6.5, 2.5, 6.5), ("rope", 10.5, 1, 7.5), ("fish", 1, 3.5, 2), ("tail_fin", .5, 1.5, 2),
    ]:
        H.part(*args)


PAL = A.new_pal(
    BACK=rg([(24, 30, 14), (48, 60, 24), (78, 96, 36), (112, 130, 56), (154, 170, 90)], nodither=True),
    BELLY=rg([(110, 100, 70), (156, 146, 104), (194, 184, 140), (222, 214, 172)], nodither=True),
    spots=dict(seed=171, cell=7, rmin=1.6, rmax=3.6, depth=0.38), belly_spots=True, side_split=0.55, gloss=1, lid=0.18,
    EYE=rg([(70, 40, 6), (120, 76, 14), (176, 124, 30), (214, 170, 60), (240, 210, 120)]),
)
PAL.EYE_OUT, PAL.PUPIL, PAL.hpupil = H.rgb(24, 20, 8), H.rgb(10, 8, 6), (0.55, 0.55)
PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE, PAL.NOSTRIL = H.rgb(30, 34, 14), H.rgb(110, 36, 40), H.rgb(214, 96, 104), H.rgb(30, 34, 14)
PAL.TONGUE_R = rg([(150, 56, 66), (196, 84, 94), (228, 116, 124), (248, 158, 164)])
PAL.BLUSH, PAL.BLUSH_L = H.rgb(120, 110, 60), H.rgb(140, 130, 76)        # weathered cheeks, not a girl's blush
PAL.DRUM = rg([(60, 56, 26), (110, 104, 48), (160, 152, 80), (198, 190, 116)])
PAL.SAC = rg([(130, 120, 70), (176, 166, 100), (210, 200, 136), (236, 228, 176)])
PAL.PAD = rg([(96, 100, 50), (144, 150, 80), (190, 196, 112), (226, 230, 152)])
PAL.HAIR = rg([(18, 40, 18), (30, 64, 28), (46, 92, 38), (70, 124, 52), (104, 160, 70), (150, 196, 100)])   # pondweed
PAL._mouth_y = SPEC.mouth_y

REED = rg([(110, 90, 40), (156, 130, 64), (200, 176, 100), (230, 212, 140)])
ROPE = rg([(90, 70, 40), (130, 104, 64), (170, 142, 96)])
FISH = rg([(70, 84, 96), (120, 136, 150), (180, 194, 204), (226, 234, 240)])


def paint():
    pt = A.paint_base(PAL, curve=3.0, fringe=3, hum=SPEC.hum)
    weed = PAL.HAIR

    def strands(f, x, y, w, h):                                          # pondweed: streaky, ragged at the end
        if f in BROAD and y >= h - 2 and (x + y) % 3 == 0:
            return CLEAR
        return pick(weed, 0.4 + 0.25 * ((x + y // 2) % 3 == 0) + (noise(x, y, 7) - 0.5) * 0.2, x, y)
    for p in ("beard", "beard_s", "stache", "brow"):
        pt(p, "all", strands)

    def reed(f, x, y, w, h):                                             # woven reed: a basket weave
        return pick(REED, 0.45 + 0.2 * (((x // 2) + (y // 2)) % 2) + (noise(x, y, 9) - 0.5) * 0.1, x, y)
    pt("hat_brim", "all", reed)
    pt("hat_top", "all", reed)
    pt("rope", "all", lambda f, x, y, w, h: pick(ROPE, 0.5 + 0.25 * (((x + y) % 3) == 0) - 0.15 * (((x - y) % 3) == 0), x, y))
    pt("fish", "all", lambda f, x, y, w, h: pick(FISH, 0.4 + 0.4 * (y / h) + (noise(x, y, 11) - 0.5) * 0.15, x, y))
    pt("tail_fin", "all", lambda f, x, y, w, h: pick(FISH, 0.35, x, y))
    return pt.T


def extras(r, g):
    HG = H.hang
    top, z0 = g.head_top, g.head_z0
    hy = g.head_y0

    # pondweed hair under a reed hat
    r.add("hair_side_r", "head", [-4.6, top - 1.0, 0], [HG("strand_m", -5.4, top - 0.8, -1.6, -6, 0), HG("strand_s", -5.4, top - 0.8, 1.4, -8, 2)], mirror=True)
    r.add("hair_back", "head", [0, top - 1.0, 3.8], [HG(n, x, top - 0.8, 4.2 + 0.25 * (i % 2), x * 1.5, 6 + 3 * (i % 3))
                                                     for i, (n, x) in enumerate(zip(("strand_m", "strand_l", "strand_m", "strand_l", "strand_m"), (-3.8, -1.9, 0, 1.9, 3.8)))])
    r.add("hat", "head", [0, top, 0], [cube("hat_brim", [-6, top - 0.25, -5.5]), cube("hat_top", [-3.25, top + 0.25, -3.25])])

    # the old man's face: bushy brows, a drooping moustache and a long beard of weed
    brow = cube("brow", [-SPEC.hum["sep"] - 1.5, hy + SPEC.hum["y"] + 2.6, z0 - 0.35], pivot=[-SPEC.hum["sep"], hy + SPEC.hum["y"] + 3.0, z0], rotation=[0, 0, -10])
    r.put("head", brow, mirror_cube(brow))
    st = cube("stache", [-2.6, hy + SPEC.mouth_y + 1.0, z0 - 0.4], pivot=[0, hy + SPEC.mouth_y + 1.5, z0], rotation=[0, 0, 18])
    r.put("head", st, mirror_cube(st))
    beard = [HG("beard", -0.75, hy + 0.5, z0 - 0.6, 0, -8), HG("beard_s", -2.25, hy + 0.75, z0 - 0.4, 6, -6), HG("beard_s", 0.75, hy + 0.75, z0 - 0.4, -6, -6)]
    r.add("beard", "head", [0, hy + 0.5, z0], beard)

    # a rope belt with a fish tied on
    r.put("body", cube("rope", [-5.25, g.hips_y0 + 2.0, -3.75]))
    r.add("fish", "body", [4.6, g.hips_y0 + 2.5, -1.0], [HG("fish", 4.6, g.hips_y0 + 2.5, -2.0, 0, 0), HG("tail_fin", 4.85, g.hips_y0 - 1.0, -2.0, 0, 0)])


def build_model():
    m, g = A.build(SPEC, extras, NAME)
    build_model.g = g
    return m


TUNE = SimpleNamespace(crouch=12, step=16, hop_lift=2.4, throat_out=(0, -2.2, -3.2), puff_f=1.3, croak_crouch=20, sit_crouch=50,
                       tongue_len=6.0, arm_rest=(3, 0, 6), extra={})


def animations(an):
    g = build_model.g
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    sway = lambda amp, per=4.0: T((0, (0, 0, 0)), (per / 4, (amp, 0, 0)), (per / 2, (0, 0, 0)), (3 * per / 4, (-amp, 0, 0)), (per, (0, 0, 0)))
    TUNE.extra = {
        "idle": {"beard": {"rotation": sway(4, 4.0)}, "fish": {"rotation": sway(5, 3.0)}},
        "walk": {"beard": {"rotation": T((0, (4, 0, 3)), (0.5, (8, 0, -3)), (1, (4, 0, 3)))}, "fish": {"rotation": T((0, (0, 0, 12)), (0.5, (0, 0, -12)), (1, (0, 0, 12)))}},
        "swim": {"beard": {"rotation": hold(-50, 0, 0)}, "fish": {"rotation": hold(-40, 0, 0)}},
    }
    A.common_anims(an, g, TUNE)
    # pull: he reaches up out of the water and drags something down
    an.add("pull", 1.2, False, merge(
        {"body": {"rotation": T((0, (0, 0, 0)), (0.3, (-20, 0, 0)), (0.8, (25, 0, 0)), (1.2, (0, 0, 0)))},
         "beard": {"rotation": T((0, (0, 0, 0)), (0.3, (-20, 0, 0)), (0.8, (30, 0, 0)), (1.2, (0, 0, 0)))}},
        sym("arm", T((0, (0, 0, 6)), (0.3, (-170, 0, 10)), (0.8, (-20, 0, 10)), (1.2, (0, 0, 6)))), sym("forearm", hold(-20, 0, 0))))


def main():
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="croak", head_center=(0, 19.0), body_center=(0, 12.0), scale=15.5,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("walk", 0.3, -35, 10), ("swim", 0.5, -40, 10), ("pull", 0.3, -30, 10), ("sit", 1.0, -30, 10)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("croak", 1.0, 0, 0), ("tongue", 0.3, 0, 0), ("idle", 0, -30, 8)])


if __name__ == "__main__":
    main()
