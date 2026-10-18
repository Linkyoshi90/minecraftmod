#!/usr/bin/env python3
"""Tree Frog - species definition (Bullywug family). Shared engine: anuran_lib.py on top of harpy_lib.py.

    python tools/gen_tree_frog.py                    # write geo / animation / texture
    python tools/gen_tree_frog.py --preview x.png    # also render x.png, x_face.png, x_body.png, x_texture.png

Concept (after the red-eyed tree frog): a light, quick canopy scout, about 1.4 blocks. Smooth lime-green skin, blue flanks
with yellow bars, a cream belly, bright orange hands and feet with big sticky pads, and vivid red eyes with vertical slit
pupils. She sleeps by day with her eyes shut and her colours tucked away, then startles awake. Her webbed hands and feet
let her glide between trees, and her leaf cape spreads like wings. Outfit: a leaf-cape, a vine harness with a pouch, a rope
belt, a big leaf worn as a hat with an orange blossom, vine wraps on wrists and ankles; blue pigtails.
Humanoid face.
"""
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import anuran_lib as A
from anuran_lib import rg, cube, mirror_cube, pick, noise, CLEAR, BROAD

NAME = "tree_frog"

SPEC = SimpleNamespace(legx=1.75, eye_x=2.25, eye_out=0.25, eye_rise=1.0, head_sink=0.75, head_fwd=0.0, chest_fwd=0.0,
                       mouth_y=0.75, throat_y=0.5, tymp_y=1.8, tymp_z=3.0,
                       hum=dict(sep=2.25, y=3.5, a=4.9, b=5.4, mw=1.3))
SIZES = {
    "head": (7.5, 6.75, 7), "eyelid": (2.75, 3, .125), "mouth": (2.5, 1.5, .125), "tymp": (.25, 2, 2), "throat": (4, 2.5, 3.25),
    "bang_a": (1.5, 3, .5), "bang_b": (1.25, 4, .5), "strand_s": (1.25, 5, .75), "strand_m": (1.25, 7, .75), "strand_l": (1.25, 9, .75),
    "hips": (5.5, 2.5, 4), "mid": (4.25, 3, 3.5), "chest": (5.5, 3.5, 4), "belly_f": (3.75, 3, 3), "belly_s": (3, 3, 3),
    "arm_up": (1.5, 3, 1.5), "forearm": (1.25, 3, 1.25), "palm": (2.75, 1, 1.25),
    "fing_a": (.5, 1.25, .5), "fing_b": (.5, 1.5, .5), "pad_h": (.75, .75, .75),
    "thigh": (2.75, 4, 2.75), "shin": (1.75, 4.5, 1.75), "heel": (3.25, 1, 2.25),
    "toe_a": (.5, .5, 2.5), "toe_b": (.5, .5, 3.5), "toe_c": (.5, .5, 4), "toe_d": (.5, .5, 3), "pad_t": (1, .75, 1), "web_f": (3, .25, 3),
}


def declare_parts():
    A.declare_base(SIZES)
    for args in [
        ("leaf_cape", 4, 8, .5), ("vine", .75, 7, .5), ("pouch", 2, 2, 1.5), ("pouch_flap", 2.25, 1, 1.75), ("belt", 5.75, .75, 4.25),
        ("leaf_hat", 8, .5, 8), ("petal", 1, 1.75, .25), ("bud_c", 1, .75, 1), ("ankle_wrap", 2.25, .75, 2.25), ("wrist_wrap", 1.75, .75, 1.75), ("hair_tie", 1.25, 1, 1.25),
    ]:
        H.part(*args)


ORANGE = rg([(150, 50, 10), (214, 90, 20), (246, 140, 40), (255, 190, 90)])
PAL = A.new_pal(
    BACK=rg([(14, 70, 24), (34, 120, 40), (70, 180, 64), (120, 224, 96), (190, 250, 160)], nodither=True),
    BELLY=rg([(150, 160, 130), (196, 206, 170), (228, 236, 204), (246, 250, 226)], nodither=True),
    side_split=0.5, gloss=2,
    EYE=rg([(70, 6, 10), (150, 16, 20), (214, 36, 30), (244, 84, 50), (255, 150, 100)]),
)
PAL.EYE_OUT, PAL.PUPIL = H.rgb(40, 4, 6), H.rgb(6, 4, 6)
PAL.hpupil_bitmap = ["##"] * 8                                       # vertical slit pupils
PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE, PAL.NOSTRIL = H.rgb(10, 60, 24), H.rgb(150, 40, 56), H.rgb(236, 110, 120), H.rgb(10, 60, 24)
PAL.TONGUE_R = rg([(150, 50, 66), (200, 76, 90), (232, 110, 122), (250, 156, 164)])
PAL.BLUSH, PAL.BLUSH_L = H.rgb(240, 150, 100), H.rgb(250, 186, 130)
PAL.DRUM = rg([(20, 80, 30), (50, 140, 50), (96, 196, 84), (150, 236, 130)])
PAL.SAC = rg([(150, 160, 110), (196, 206, 150), (226, 236, 186), (248, 252, 222)])
PAL.PAD = rg([(170, 70, 14), (236, 120, 30), (255, 170, 70), (255, 216, 130)])
PAL.HAIR = rg([(6, 30, 60), (12, 60, 110), (24, 100, 170), (50, 150, 220), (110, 200, 250), (190, 236, 255)])
PAL._mouth_y = SPEC.mouth_y

LEAF = rg([(18, 70, 28), (36, 116, 46), (66, 160, 70), (120, 206, 104)])
VINE = rg([(40, 56, 20), (74, 98, 34), (112, 140, 56)])
BROWN = rg([(60, 38, 22), (100, 68, 40), (146, 104, 64)])
PETAL = rg([(170, 50, 10), (226, 96, 20), (252, 150, 50), (255, 214, 120)])
BLUE = rg([(10, 40, 150), (30, 80, 220), (70, 130, 250)])
YELLOW = rg([(210, 160, 20), (250, 212, 50), (255, 240, 130)])


def paint():
    pt = A.paint_base(PAL, curve=3.5, fringe=4, hum=SPEC.hum)

    def flank(f, x, y, w, h):                                    # blue flanks with yellow bars
        if f in ("east", "west") and y / max(1, h - 1) > 0.28:
            if (x % 4) < 2:
                return pick(YELLOW, 0.65 + (noise(x, y, 3) - 0.5) * 0.1, x, y)
            return pick(BLUE, 0.55 + (noise(x, y, 5) - 0.5) * 0.12, x, y)
        return None
    for p in ("hips", "mid", "chest", "thigh"):
        pt(p, ("east", "west"), flank)

    def orange(f, x, y, w, h):
        return pick(ORANGE, 0.58 + (noise(x, y, 7) - 0.5) * 0.12 + (0.1 if f == "up" else 0) - (0.12 if f == "down" else 0), x, y)
    for p in ("palm", "fing_a", "fing_b", "toe_a", "toe_b", "toe_c", "toe_d", "heel"):
        pt(p, "all", orange)
    pt("web_f", "all", lambda f, x, y, w, h: pick(ORANGE, 0.78 - (0.2 if (x % 4 == 3 and f in ("up", "down")) else 0), x, y))

    def leafy(f, x, y, w, h):
        if f in ("up", "down"):
            return LEAF[1]
        t = y / max(1, h - 1)
        half = (w / 2.0) * (1.0 - t ** 2.2) + 0.5
        if abs(x + 0.5 - w / 2.0) > half:
            return CLEAR
        rib = (x == w // 2) or (x == w // 2 - 1 and w % 2 == 0)
        vein = (y + int(abs(x - w / 2.0) * 1.3)) % 6 == 0
        return pick(LEAF, 0.40 + 0.28 * rib + 0.16 * vein + 0.12 * (1 - t) + (noise(x, y, 9) - 0.5) * 0.08, x, y)
    pt("leaf_cape", "all", leafy)

    def hat(f, x, y, w, h):
        if f in ("up", "down"):
            dx, dy = (x + 0.5 - w / 2.0) / (w / 2.0), (y + 0.5 - h / 2.0) / (h / 2.0)
            if dx * dx + dy * dy > 1.0:
                return CLEAR
            if f == "down":
                return pick(LEAF, 0.30, x, y)
            vein = abs(dx) < 0.07 or abs(abs(dx) - abs(dy) * 0.9) < 0.07 and dy > -0.4
            return pick(LEAF, 0.55 + 0.25 * vein + (noise(x, y, 11) - 0.5) * 0.08 - 0.15 * (dx * dx + dy * dy > 0.8), x, y)
        return CLEAR
    pt("leaf_hat", "all", hat)
    pt("vine", "all", lambda f, x, y, w, h: pick(VINE, 0.4 + 0.4 * (((y + x) // 2) % 2), x, y))
    pt("belt", "all", lambda f, x, y, w, h: pick(VINE, 0.35 + 0.4 * (((x + y) // 2) % 2), x, y))
    pt("pouch", "all", lambda f, x, y, w, h: pick(BROWN, 0.55 + (noise(x, y, 13) - 0.5) * 0.14, x, y))
    pt("pouch_flap", "all", lambda f, x, y, w, h: pick(LEAF, 0.6 - 0.2 * (y > h - 2), x, y))
    pt("ankle_wrap", "all", lambda f, x, y, w, h: pick(LEAF, 0.50 + 0.2 * (((x + y) // 2) % 2), x, y))
    pt("wrist_wrap", "all", lambda f, x, y, w, h: pick(LEAF, 0.50 + 0.2 * (((x + y) // 2) % 2), x, y))
    pt("hair_tie", "all", lambda f, x, y, w, h: pick(PETAL, 0.55 + 0.2 * (x % 2), x, y))

    def petal(f, x, y, w, h):
        if f not in BROAD:
            return PETAL[1]
        half = (w / 2.0) * min(1.0, (y + 1.5) / (h * 0.5))
        if abs(x + 0.5 - w / 2.0) > half:
            return CLEAR
        return pick(PETAL, 0.95 - 0.7 * (y / h), x, y)
    pt("petal", "all", petal)
    pt("bud_c", "all", lambda f, x, y, w, h: pick(YELLOW, 0.75, x, y))
    return pt.T


def extras(r, g):
    HG = H.hang
    top, z0 = g.head_top, g.head_z0

    # blue pigtails tied high on both sides, a short fringe
    r.put("head", HG("bang_a", -0.75, top + 0.3, z0 - 0.3, 0, -4))
    tail = [HG("strand_l", -1.9, top - 1.6, 0.5, -12, 2), HG("strand_m", -0.9, top - 1.6, 1.0, -9, 4), HG("strand_m", -2.9, top - 1.6, 0.0, -14, 0)]
    r.add("hair_side_r", "head", [-4.7, top - 1.4, 0.5], [HG("strand_l", -5.6, top - 1.2, 0.2, -6, 0), HG("strand_m", -5.0, top - 1.2, 1.4, -9, 3)], mirror=True)
    r.put("head", cube("hair_tie", [-4.9, top - 1.7, 0.0]), cube("hair_tie", [3.65, top - 1.7, 0.0]))
    back = [HG(n, x, top - 1.4, 3.7 + 0.2 * (i % 2), x * 1.6, 4 + 3 * (i % 2)) for i, (n, x) in enumerate(zip(("strand_s", "strand_m", "strand_s"), (-2.0, 0.0, 2.0)))]
    r.add("hair_back", "head", [0, top - 1.4, 3.5], back)

    # a big leaf as a hat, tipped back, with an orange blossom
    r.put("head", cube("leaf_hat", [-4.0, top + 0.7, z0 - 0.3], pivot=[0, top + 0.7, z0 + 6], rotation=[-12, 0, 0]))
    fx, fy, fz = 1.8, top + 1.55, z0 + 3.2
    r.put("head", cube("bud_c", [fx - 0.5, fy, fz - 0.5]))
    for i in range(6):
        r.put("head", cube("petal", [fx - 0.5, fy + 0.2, fz - 0.45], pivot=[fx, fy + 0.2, fz], rotation=[-60, i * 60, 0]))

    # leaf cape (two leaves that spread like wings), vine harness, pouch, rope belt, vine wraps
    capes = [HG("leaf_cape", -4.0, g.chest_top - 0.6, g.chest_front + 4.3, 0, 3)]
    r.add("cape_r", "body", [-2.0, g.chest_top - 0.6, 2.4], capes, mirror=True)
    vy = g.chest_y0 - 1.0
    r.put("body", cube("vine", [-0.4, vy, g.chest_front - 0.45], pivot=[0, vy + 3.5, g.chest_front], rotation=[0, 0, 34]),
          cube("vine", [-0.4, vy, g.chest_front - 0.7], pivot=[0, vy + 3.5, g.chest_front], rotation=[0, 0, -34]))
    r.put("body", cube("belt", [-2.875, g.hips_y0 + 1.7, -2.4]))
    r.add("pouch", "body", [2.9, g.hips_y0 + 1.7, -0.5], [HG("pouch", 2.6, g.hips_y0 + 1.6, -1.0), HG("pouch_flap", 2.5, g.hips_y0 + 1.7, -1.1)])
    r.put("shin_r", cube("ankle_wrap", [-SPEC.legx - 1.125, g.ankle_y + 0.6, 0.5 - 1.125]))
    r.put("forearm_r", cube("wrist_wrap", [-g.shoulder_x - 0.875, g.shoulder_y - 3.0 - 3.0 + 0.9, -0.875]))


def build_model():
    m, g = A.build(SPEC, extras, NAME)
    build_model.g = g
    return m


TUNE = SimpleNamespace(crouch=12, step=16, hop_lift=7.0, throat_out=(0, -1.6, -2.2), puff_f=1.0, croak_crouch=24, sit_crouch=60,
                       tongue_len=5.5, arm_rest=(3, 0, 5), extra={})


def animations(an):
    g = build_model.g
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    OUT, REST = H.OUT_, H.REST
    sway = lambda amp, per=4.0: T((0, (0, 0, -amp)), (per / 2, (0, 0, amp)), (per, (0, 0, -amp)))
    TUNE.extra = {
        "idle": {**sym("cape", sway(2, 4.0))},
        "walk": {**sym("cape", T((0, (0, 0, 3)), (0.5, (0, 0, -3)), (1, (0, 0, 3))))},
        "hop": {**sym("cape", T((0, (0, 0, 0)), (0.3, (-8, 0, -50)), (0.74, (0, 0, -20)), (0.9, (0, 0, 0))))},
        "swim": {**sym("cape", hold(-30, 0, -10))},
        "sit": {**sym("cape", hold(6, 0, 2))},
    }
    A.common_anims(an, g, TUNE)
    pose = lambda deg: (-deg, 2 * deg, 0, 0)

    # glide: body flat, arms and legs spread so the webbed hands and feet catch the air, the leaf cape spread like wings
    lg, by = A.gait(g.T, g.Sh, 1.6, [(0, (4, 4, 0, 0)), (1.6, (4, 4, 0, 0))], [(0, (4, 4, 0, 0)), (1.6, (4, 4, 0, 0))])
    up = 5.0
    for s_ in ("r", "l"):
        lg["leg_" + s_] = {"rotation": T((0, (46, 0, -26)), (0.8, (52, 0, -32)), (1.6, (46, 0, -26)))}
        if s_ == "l":
            lg["leg_l"] = {"rotation": H.mirror(lg["leg_r"]["rotation"])}
        lg["leg_" + s_]["position"] = hold(0, up, 0)
        lg["shin_" + s_] = {"rotation": hold(6, 0, 0)}
        lg["foot_" + s_] = {"rotation": hold(20, 0, 0)}
    lg["body"] = {"position": hold(0, up, 0), "rotation": T((0, (62, 0, 0)), (0.8, (66, 0, 0)), (1.6, (62, 0, 0)))}
    an.add("glide", 1.6, True, merge(
        lg,
        {"head": {"rotation": hold(-46, 0, 0)}, "hair_back": {"rotation": hold(-30, 0, 0)},
         "throat": {"position": hold(0, -0.2, -0.2)}},
        sym("arm", T((0, (-24, 0, -78)), (0.8, (-30, 0, -84)), (1.6, (-24, 0, -78)))), sym("forearm", hold(-4, 0, 0)), sym("hand", hold(-6, 0, 0)),
        sym("cape", T((0, (-6, 0, -70)), (0.8, (-6, 0, -78)), (1.6, (-6, 0, -70))))))

    # sleep: curled up on a leaf with her eyes shut and her bright colours tucked in
    c = 74
    lg2, by2 = A.gait(g.T, g.Sh, 4.0, [(0, pose(c)), (4, pose(c))], [(0, pose(c)), (4, pose(c))])
    for s_ in ("r", "l"):
        lg2["leg_" + s_]["position"] = by2
    lg2["body"] = {"position": by2, "rotation": T((0, (14, 0, 0)), (2, (16, 0, 0)), (4, (14, 0, 0)))}
    an.add("sleep", 4.0, True, merge(
        lg2,
        {"head": {"rotation": T((0, (22, 0, 4)), (2, (24, 0, 4)), (4, (22, 0, 4)))}, "eyelids": {"position": hold(*OUT)},
         "throat": {"position": T((0, REST), (1, (0, -0.3, -0.35)), (2, REST), (3, (0, -0.3, -0.35)), (4, REST))}},
        sym("arm", hold(-40, 0, 24)), sym("forearm", hold(-100, 0, 0)), sym("hand", hold(-20, 0, 0)), sym("cape", hold(30, 0, -4))))

    # climb: hands and feet take turns reaching up, sticky pads holding on
    cl = [(0, (-82, 118, 0, 0)), (0.5, (-38, 52, 0, 0)), (1.0, (-82, 118, 0, 0))]
    lg3, by3 = A.gait(g.T, g.Sh, 1.0, cl)
    for s_ in ("r", "l"):
        lg3["leg_" + s_]["position"] = by3
    lg3["body"] = {"position": by3, "rotation": hold(-10, 0, 0)}
    an.add("climb", 1.0, True, merge(
        lg3,
        {"head": {"rotation": hold(-24, 0, 0)}, "hair_back": {"rotation": hold(14, 0, 0)},
         "arm_r": {"rotation": T((0, (-170, 0, 8)), (0.5, (-110, 0, 8)), (1.0, (-170, 0, 8)))},
         "arm_l": {"rotation": T((0, (-110, 0, 8)), (0.5, (-170, 0, 8)), (1.0, (-110, 0, 8)))},
         "forearm_r": {"rotation": hold(-12, 0, 0)}, "forearm_l": {"rotation": hold(-12, 0, 0)}},
        sym("cape", hold(10, 0, -3))))


def main():
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="croak", head_center=(0, 19.0), body_center=(0, 12.0), scale=16.5,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("glide", 0.4, -30, 10), ("sleep", 1.0, -30, 8), ("climb", 0.25, -30, 8), ("hop", 0.4, -30, 10)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("croak", 0.6, 0, 0), ("tongue", 0.3, 0, 0), ("idle", 0, -30, 8)])


if __name__ == "__main__":
    main()
