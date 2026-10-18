#!/usr/bin/env python3
"""Surinam Toad - species definition (Mucus Toad family). Shared engine: anuran_lib.py on top of harpy_lib.py.

    python tools/gen_surinam_toad.py                    # write geo / animation / texture
    python tools/gen_surinam_toad.py --preview x.png    # also render x.png, x_face.png, x_body.png, x_texture.png

Concept (after the Surinam toad, a fully aquatic, flat, leaf-brown toad with no tongue, star-tipped fingers and young that
hatch out of pits in the mother's back): the swamp midwife, about 1.25 blocks. Dead-leaf-brown mottled skin, a flat wide head
with small wide-set beady eyes, long star-tipped fingers for feeling through mud, huge webbed feet. Her back is a honeycomb
of pits, some holding eggs and some with tiny toadlets peeking out. Outfit: a leaf-litter poncho, a pointed dead-leaf hat,
river-weed hair and ribbons, a pearl pendant and a net bag with a fish in it. She has no tongue: she feeds by sucking
prey in (the `tongue` animation opens the mouth and gulps instead). She carries up to three young, which develop fully inside the pits (five in-game days) and leave as
miniature Surinam toads (no tadpole stage). Signature moves: `sweep`, `brood`, `birth` and `idle_empty` (the idle without young). Humanoid face.
"""
import math
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import anuran_lib as A
from anuran_lib import rg, cube, mirror_cube, pick, noise, CLEAR, BROAD

NAME = "surinam_toad"

SPEC = SimpleNamespace(legx=2.5, eye_x=3.0, eye_out=0.25, eye_rise=1.0, head_sink=0.5, head_fwd=0.0, chest_fwd=0.0,
                       mouth_y=0.5, throat_y=0.5, tymp_y=1.5, tymp_z=3.0,
                       hum=dict(sep=3.0, y=3.2, a=3.6, b=3.6, mw=2.0))
SIZES = {
    "head": (9, 5.5, 8), "eyelid": (2, 2, .125), "mouth": (4, 1.5, .125), "tymp": (.25, 1.25, 1.25),
    "tongue_a": (1, .5, 5), "tongue_b": (1.75, .5, 1.5), "throat": (4.5, 2, 3.5),
    "bang_a": (1.5, 3, .5), "bang_b": (1.25, 3.5, .5), "strand_s": (1.5, 6, .75), "strand_m": (1.5, 9, .75), "strand_l": (1.5, 12, .75),
    "hips": (9, 2.5, 6), "mid": (7.5, 3, 5.5), "chest": (9, 3.5, 6), "belly_f": (6.5, 3, 3.5), "belly_s": (3.5, 3, 5),
    "arm_up": (2, 3, 2), "forearm": (1.75, 3, 1.75), "palm": (2.75, 1, 1.5),
    "fing_a": (.5, 2, .5), "fing_b": (.5, 2.5, .5), "pad_h": (.75, .75, .75),
    "thigh": (3.75, 3.5, 3.75), "shin": (2.5, 3.5, 2.5), "heel": (4, 1, 3),
    "toe_a": (.75, .75, 3.5), "toe_b": (.75, .75, 5), "toe_c": (.75, .75, 5.5), "toe_d": (.75, .75, 4.25), "pad_t": (1, .75, 1), "web_f": (4, .25, 5),
}


def declare_parts():
    A.declare_base(SIZES)
    for args in [
        ("leaf_hat", 11, .5, 12), ("poncho", 10, 3, 7), ("weed", 1.25, 7, .5), ("pearl", 1, 1, .5),
        ("net", 4, 3.5, 3), ("fish", 2.5, .75, 1), ("hair_clip", 1, 1, .5),
        ("tl_body", 2.25, 1, 2.5), ("tl_head", 1.75, 1, 1.5), ("tl_leg", .75, .5, 1.75), ("tl_arm", .25, .25, 1),
    ]:
        H.part(*args)


PAL = A.new_pal(
    BACK=rg([(30, 22, 14), (62, 46, 28), (98, 76, 46), (138, 110, 70), (180, 150, 104)], nodither=True),
    BELLY=rg([(150, 130, 100), (190, 170, 134), (218, 200, 164), (236, 224, 192)], nodither=True),
    spots=dict(seed=141, cell=7, rmin=1.8, rmax=3.6, depth=0.36), belly_spots=False,
    side_split=0.50, gloss=0, lid=0.0,
    EYE=rg([(50, 40, 10), (100, 80, 20), (160, 130, 30), (210, 180, 60), (240, 220, 120)]),
)
PAL.EYE_OUT, PAL.PUPIL, PAL.hpupil = H.rgb(30, 22, 10), H.rgb(8, 6, 4), (0.62, 0.62)
PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE, PAL.NOSTRIL = H.rgb(40, 28, 16), H.rgb(120, 44, 44), H.rgb(214, 100, 100), H.rgb(30, 22, 12)
PAL.TONGUE_R = rg([(120, 40, 40), (160, 60, 60), (190, 80, 80), (210, 100, 100)])
PAL.BLUSH, PAL.BLUSH_L = None, None
PAL.DRUM = rg([(60, 44, 26), (96, 74, 44), (136, 108, 68), (176, 146, 100)])
PAL.SAC = rg([(150, 130, 100), (190, 170, 134), (218, 200, 164), (236, 224, 192)])
PAL.PAD = rg([(150, 130, 100), (196, 176, 140), (226, 210, 176), (246, 238, 214)])
PAL.HAIR = rg([(10, 28, 16), (24, 56, 30), (44, 88, 44), (76, 124, 64), (120, 164, 96), (170, 204, 140)])
PAL._mouth_y = SPEC.mouth_y

LEAF = rg([(60, 38, 18), (104, 70, 30), (148, 106, 48), (190, 150, 80)])
WEED = rg([(20, 60, 30), (40, 104, 50), (80, 150, 76), (130, 196, 112)])
NET = rg([(120, 96, 60), (170, 142, 90)])
SILVER = rg([(90, 100, 110), (140, 150, 160), (190, 200, 208), (232, 238, 242)])
PEARL = rg([(170, 170, 180), (226, 226, 234), (252, 252, 255)])
EGG = rg([(190, 176, 130), (226, 214, 170), (248, 240, 208)])
TAD = rg([(30, 22, 14), (62, 46, 28), (98, 76, 46)])


def paint():
    pt = A.paint_base(PAL, curve=2.0, fringe=3, hum=SPEC.hum)

    def pits(f, x, y, w, h):                                         # the honeycomb of brood pits on her back
        if f != "south" or w < 8 or h < 8:
            return None
        cx, cy = x % 4, y % 4
        cell = noise(x // 4, y // 4, 5)
        if cx in (1, 2) and cy in (1, 2):
            if cell > 0.75:
                return pick(EGG, 0.55 + 0.4 * (cx == 1 and cy == 1), x, y)           # a pit with an egg in it
            return pick(PAL.BACK, 0.06 + 0.1 * (cx == 2 and cy == 2), x, y)
        if cx == 0 or cy == 0:
            return pick(PAL.BACK, 0.80, x, y)                                       # raised rim
        return None
    for p in ("hips", "mid", "chest"):
        pt(p, ("south",), pits)

    def star(f, x, y, w, h):                                         # star-tipped fingers: a plus-shaped pad
        if f in ("north", "south", "up", "down") and w == 3 and h == 3 and (x, y) in ((0, 0), (2, 0), (0, 2), (2, 2)):
            return CLEAR
        return pick(PAL.PAD, 0.78 - 0.3 * (x == 0 or y == h - 1), x, y)
    pt("pad_h", "all", star)

    def leaf_hat(f, x, y, w, h):
        if f in ("up", "down"):
            t = (y + 0.5) / h
            half = (w / 2.0) * math.sin(math.pi * t ** 0.8)
            if abs(x + 0.5 - w / 2.0) > half:
                return CLEAR
            if noise(x // 2, y // 2, 7) > 0.93:
                return CLEAR                                              # a hole eaten in the leaf
            if f == "down":
                return pick(LEAF, 0.30 + 0.2 * (abs(x - w / 2.0) < 1), x, y)
            rib = abs(x + 0.5 - w / 2.0) < 0.8
            vein = (y + int(abs(x - w / 2.0) * 1.2)) % 6 == 0
            return pick(LEAF, 0.52 + 0.26 * rib + 0.14 * vein + (noise(x, y, 9) - 0.5) * 0.14 - 0.12 * (abs(x + 0.5 - w / 2.0) > half * 0.85), x, y)
        return CLEAR
    pt("leaf_hat", "all", leaf_hat)

    def poncho(f, x, y, w, h):
        if f in ("up", "down"):
            return LEAF[1]
        if y > h - 3 and f in BROAD and noise(x, y, 11) > 0.45:
            return CLEAR
        if noise(x // 2, y // 2, 13) > 0.92:
            return CLEAR
        rib = (x % 6 == 3)
        return pick(LEAF, 0.45 + 0.2 * rib + 0.1 * ((x + y) % 7 == 0) + (noise(x, y, 15) - 0.5) * 0.16, x, y)
    pt("poncho", "all", poncho)
    pt("weed", "all", lambda f, x, y, w, h: pick(WEED, 0.45 + 0.4 * (x == w // 2) + (noise(x, y, 17) - 0.5) * 0.1, x, y) if not (f in BROAD and y > h - 3 and (x + y) % 2) else CLEAR)
    pt("pearl", "all", lambda f, x, y, w, h: pick(PEARL, 0.9 - 0.4 * (x > w - 2 or y > h - 2), x, y))
    pt("net", "all", lambda f, x, y, w, h: pick(NET, 0.7, x, y) if ((x + y) % 3 == 0 or (x - y) % 3 == 0 or x == 0 or y == 0 or x == w - 1 or y == h - 1) else CLEAR)
    pt("fish", "all", lambda f, x, y, w, h: pick(SILVER, 0.7 + 0.25 * (y < 2) - 0.3 * (x < 2) + (0.0), x, y))

    BACK, BELLY = PAL.BACK, PAL.BELLY

    def tl_skin(f, x, y, w, h):                                       # same leaf-brown mottling as the adult
        if f == "down":
            return pick(BELLY, 0.62, x, y)
        return pick(BACK, 0.55 + 0.28 * (noise(x // 2, y // 2, 21) > 0.7) + (0.08 if f == "up" else 0) + (noise(x, y, 23) - 0.5) * 0.08, x, y)

    def tl_head(f, x, y, w, h):
        if f in ("south", "up"):
            eye_row = 0 if f == "south" or True else 0
            if y == 0 and x in (0, w - 1):
                return H.rgb(240, 220, 120)                          # beady gold eyes on the flat head
        return tl_skin(f, x, y, w, h)
    pt("tl_body", "all", tl_skin)
    pt("tl_head", "all", tl_head)
    pt("tl_leg", "all", tl_skin)
    pt("tl_arm", "all", lambda f, x, y, w, h: pick(PAL.PAD, 0.8, x, y) if (f in ("north", "south") and y == h - 1) else tl_skin(f, x, y, w, h))
    pt("hair_clip", "all", lambda f, x, y, w, h: pick(WEED, 0.7, x, y))
    return pt.T


def extras(r, g):
    HG = H.hang
    top, z0 = g.head_top, g.head_z0

    # long river-weed hair, a sparse fringe, a clip
    r.put("head", HG("bang_a", -0.75, top + 0.3, z0 - 0.3, 0, -4), HG("bang_b", 1.4, top + 0.2, z0 - 0.3, -4, -3))
    r.add("hair_side_r", "head", [-4.7, top - 1.2, 0], [HG("strand_m", -5.4, top - 1.0, -0.4, -4, 0), HG("strand_s", -5.4, top - 1.0, 1.6, -6, 3)], mirror=True)
    r.add("hair_back", "head", [0, top - 1.2, 4.0], [HG(n, x, top - 1.1, 4.4 + 0.25 * (i % 2), x * 1.3, 5 + 3 * (i % 3)) for i, (n, x) in enumerate(zip(("strand_s", "strand_s", "strand_s", "strand_s", "strand_s"), (-3.6, -1.8, 0.0, 1.8, 3.6)))])

    # pointed dead-leaf hat tipped back, with a hair clip
    r.put("head", cube("leaf_hat", [-5.5, top + 0.55, z0 - 2.0], pivot=[0, top + 0.55, z0 + 9.0], rotation=[-9, 0, 0]), cube("hair_clip", [3.4, top - 0.2, z0 + 1.0]))

    # leaf-litter poncho, river-weed ribbons, pearl pendant
    r.put("body", cube("poncho", [-5.0, g.chest_top - 3.1, -3.5]), cube("pearl", [-0.5, g.chest_top - 3.0, g.chest_front - 0.8]))
    wd = [HG("weed", -3.2, g.chest_top - 2.8, g.chest_front - 0.95, 2, -2), HG("weed", -0.6, g.chest_top - 3.1, g.chest_front - 1.0, -1, -3), HG("weed", 2.0, g.chest_top - 2.8, g.chest_front - 0.95, -3, -2)]
    r.add("weeds", "body", [0, g.chest_top - 2.8, g.chest_front - 0.9], wd)

    # net bag with a fish, hung at her hip
    nx, ny = 5.2, g.hips_y0 + 2.0
    r.add("net", "body", [nx, ny, 0], [HG("net", nx - 1.0, ny, -1.5), cube("fish", [nx - 1.2, ny - 1.0, -0.6], pivot=[nx, ny - 1.0, 0], rotation=[0, 0, 24])])

    # three toadlets (she carries up to three), each a miniature Surinam toad (body, flat head with beady eyes, splayed hind legs, little arms) that
    # sits in a pit on her back, head outward. Each is its own bone so they can wriggle and leave one at a time.
    pos = ((-2.6, g.mid_y0 + 0.4, 2.75), (2.2, g.mid_y0 + 1.0, 2.75), (-0.2, g.hips_y0 + 0.3, 3.0))
    for k, (x, y, zb) in zip("abc", pos):
        kid = [cube("tl_body", [x - 1.125, y, zb]), cube("tl_head", [x - 0.875, y, zb + 2.5]),
               cube("tl_leg", [x - 1.9, y, zb + 0.4], pivot=[x - 1.1, y, zb + 0.4], rotation=[0, 28, 0]),
               cube("tl_leg", [x + 1.15, y, zb + 0.4], pivot=[x + 1.1, y, zb + 0.4], rotation=[0, -28, 0]),
               cube("tl_arm", [x - 1.1, y + 0.3, zb + 2.7]), cube("tl_arm", [x + 0.85, y + 0.3, zb + 2.7])]
        r.add("brood_" + k, "body", [x, y + 0.5, zb + 1.2], kid)


def build_model():
    m, g = A.build(SPEC, extras, NAME)
    build_model.g = g
    return m


TUNE = SimpleNamespace(crouch=10, step=12, hop_lift=2.0, throat_out=(0, -1.5, -2.2), puff_f=1.0, croak_crouch=20, sit_crouch=56,
                       tongue_len=0.0, arm_rest=(3, 0, 5), extra={})


def animations(an):
    g = build_model.g
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    OUT, REST = H.OUT_, H.REST
    sway = lambda amp, per=4.0: T((0, (0, 0, 0)), (per / 4, (amp, 0, 0)), (per / 2, (0, 0, 0)), (3 * per / 4, (-amp, 0, 0)), (per, (0, 0, 0)))
    wig = lambda amp, per: T((0, (0, 0, -amp)), (per / 2, (0, 0, amp)), (per, (0, 0, -amp)))
    kids = lambda make: {f"brood_{k}": make(i) for i, k in enumerate("abc")}       # the three toadlet bones
    HIDE = (0, 0, -5.2)                                                                # sunk into her back: not carrying
    TUNE.extra = {
        "idle": {"weeds": {"rotation": sway(4, 3.0)}, "net": {"rotation": sway(3)}, **kids(lambda i: {"rotation": wig(2 + (i % 2), 3.0 + 0.3 * i)})},
        "walk": {"weeds": {"rotation": T((0, (6, 0, 3)), (0.5, (12, 0, -3)), (1, (6, 0, 3)))}, "net": {"rotation": wig(8, 1.0)}, **kids(lambda i: {"rotation": wig(3, 1.0)})},
        "hop": {"weeds": {"rotation": T((0, (0, 0, 0)), (0.3, (-30, 0, 0)), (0.74, (14, 0, 0)), (0.9, (0, 0, 0)))}, "net": {"rotation": wig(14, 0.9)}},
        "swim": {"weeds": {"rotation": hold(-60, 0, 0)}, "net": {"rotation": hold(-40, 0, 0)}},
        "sit": {"weeds": {"rotation": hold(4, 0, 0)}},
    }
    A.common_anims(an, g, TUNE)
    pose = lambda deg: (-deg, 2 * deg, 0, 0)

    def stand(period, deg):
        lg, by = A.gait(g.T, g.Sh, period, [(0, pose(deg)), (period, pose(deg))], [(0, pose(deg)), (period, pose(deg))])
        for s_ in ("r", "l"):
            lg["leg_" + s_]["position"] = by
        lg["body"] = {"position": by}
        return lg

    # sweep: bent low over the mud, the star-tipped fingers feeling from side to side, then a suck and a gulp
    lg = stand(2.4, 34)
    lg["body"]["rotation"] = hold(30, 0, 0)
    an.add("sweep", 2.4, True, merge(
        lg,
        {"head": {"rotation": T((0, (-26, -10, 0)), (0.6, (-26, 10, 0)), (1.2, (-26, -10, 0)), (1.8, (-26, 0, 0)), (2.4, (-26, -10, 0)))},
         "mouth": {"position": T((0, REST), (1.6, REST), (1.7, OUT), (1.95, OUT), (2.05, REST), (2.4, REST))},
         "throat": {"position": T((0, REST), (1.7, REST), (1.9, (0, -1.2, -1.6)), (2.2, REST), (2.4, REST))},
         "weeds": {"rotation": T((0, (10, 0, 4)), (1.2, (10, 0, -4)), (2.4, (10, 0, 4)))}, "net": {"rotation": wig(6, 2.4)}},
        {"arm_r": {"rotation": T((0, (-66, 0, -22)), (0.6, (-66, 0, 18)), (1.2, (-66, 0, -22)), (2.4, (-66, 0, -22)))},
         "arm_l": {"rotation": T((0, (-66, 0, -18)), (0.6, (-66, 0, 22)), (1.2, (-66, 0, -18)), (2.4, (-66, 0, -18)))},
         "forearm_r": {"rotation": hold(-14, 0, 0)}, "forearm_l": {"rotation": hold(-14, 0, 0)}},
        H.blink(1.0)))

    # brood: she turns to look over her shoulder as the toadlets wriggle and pop up out of their pits (still carried)
    lg2 = stand(3.0, 12)

    def pops(i):
        keys = [(0, (0, 0, 0))]
        t0 = 0.4 + i * 0.5
        keys += [(t0, (0, 0, 0)), (t0 + 0.15, (0, 0.4, 0.9)), (t0 + 0.5, (0, 0.4, 0.9)), (t0 + 0.65, (0, 0, 0)), (3.0, (0, 0, 0))]
        return {"position": T(*keys), "rotation": wig(7, 0.5)}
    an.add("brood", 3.0, True, merge(
        lg2,
        {"head": {"rotation": T((0, (0, 0, 0)), (0.5, (-6, 52, 0)), (2.5, (-6, 52, 0)), (3.0, (0, 0, 0)))},
         **kids(pops), "weeds": {"rotation": sway(3, 3.0)}, "net": {"rotation": sway(2, 3.0)}},
        sym("arm", hold(3, 0, 5)), sym("forearm", hold(-8, 0, 0)), H.blink(1.4)))

    # birth: one toadlet after another climbs out of its pit, fully formed, and swims off (plays once; the game spawns a
    # baby Surinam toad at each hand-off, and the pits are empty at the end)
    def leave(i):
        t0 = 0.6 + i * 0.7
        pos_ = T((0, (0, 0, 0)), (t0, (0, 0, 0)), (t0 + 0.25, (0, 0.6, 2.2)), (t0 + 0.6, (0, -0.6, 4.4)), (t0 + 1.0, (0, -3.2, 7.0)), (t0 + 1.01, HIDE), (3.6, HIDE))
        rot = T((0, (0, 0, -6)), (t0 * 0.5, (0, 0, 6)), (t0, (0, 0, -8)), (t0 + 0.25, (0, 0, 8)), (t0 + 1.0, (20, 0, 0)), (3.6, (0, 0, 0)))
        return {"position": pos_, "rotation": rot}
    lgb = stand(3.6, 12)
    lgb["body"]["rotation"] = T((0, (0, 0, 0)), (0.5, (-4, 0, 0)), (3.0, (-4, 0, 0)), (3.6, (0, 0, 0)))
    an.add("birth", 3.6, False, merge(
        lgb,
        {"head": {"rotation": T((0, (0, 0, 0)), (0.4, (-8, 50, 0)), (4.0, (-8, 50, 0)), (3.6, (0, 0, 0)))}, **kids(leave),
         "weeds": {"rotation": sway(3, 3.0)}, "net": {"rotation": sway(2, 3.0)}},
        sym("arm", hold(3, 0, 5)), sym("forearm", hold(-8, 0, 0)), H.blink(1.6, 3.0)))

    # idle_empty: the idle for when she is not carrying young - the toadlets are sunk out of sight
    lge = stand(4.0, 10)
    an.add("idle_empty", 4.0, True, merge(
        lge,
        {"head": {"rotation": T((0, (0, 0, 0)), (1.3, (-3, 8, 1)), (2.6, (1, -6, -1)), (4, (0, 0, 0)))},
         **kids(lambda i: {"position": hold(*HIDE)}), "weeds": {"rotation": sway(4, 3.0)}, "net": {"rotation": sway(3)},
         "throat": {"position": T((0, REST), (0.8, (0, -0.3, -0.35)), (1.6, REST), (2.4, (0, -0.3, -0.35)), (3.2, REST), (4, REST))}},
        sym("arm", hold(*TUNE.arm_rest)), sym("forearm", hold(-8, 0, 0)), H.blink(2.7)))


def main():
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="sweep", head_center=(0, 16.5), body_center=(0, 10.5), scale=17.0,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 160, 10),
                      ("sweep", 0.3, -32, 10), ("brood", 1.2, 160, 10), ("birth", 1.8, 160, 10), ("swim", 0.3, -40, 18)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("sweep", 1.8, 0, 0), ("tongue", 0.3, 0, 0), ("idle", 0, -30, 8)])


if __name__ == "__main__":
    main()
