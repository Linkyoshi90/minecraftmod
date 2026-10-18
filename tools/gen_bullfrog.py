#!/usr/bin/env python3
"""Bullfrog - species definition (Bullywug family). Shared engine: anuran_lib.py on top of harpy_lib.py.

    python tools/gen_bullfrog.py                    # write geo / animation / texture
    python tools/gen_bullfrog.py --preview x.png    # also render x.png, x_face.png, x_body.png, x_texture.png

Concept: the pond boss. The heaviest, loudest Bullywug at about 2 blocks: broad shoulders, a big flat head with huge
tympanum discs (a bullfrog's ear is bigger than its eye), muddy olive-and-brown mottled skin, a cream belly and a
swollen cream-yellow throat sac. A stern lowered brow; a dark-brown mohawk. Outfit: gold hoops on the ear discs, a pelt mantle with
lily-pad pauldrons, a trophy belt (fish skull, snail shells, pike teeth) with a big brass buckle, a rush kilt and leather
bracers. Signature moves: the deep bellow, the chest pound and the shove. Humanoid face.
"""
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import anuran_lib as A
from anuran_lib import rg, cube, mirror_cube, pick, noise, CLEAR, BROAD

NAME = "bullfrog"

SPEC = SimpleNamespace(legx=2.75, eye_x=3.0, eye_out=0.25, eye_rise=1.0, head_sink=1.0, head_fwd=0.25, chest_fwd=0.0,
                       mouth_y=1.25, throat_y=0.5, tymp_y=2.0, tymp_z=4.0,
                       hum=dict(sep=3.0, y=4.2, a=6.0, b=6.4, mw=2.0))
SIZES = {
    "head": (10, 8, 8.5), "eyelid": (3.5, 3.5, .125), "mouth": (3.5, 1.75, .125), "tymp": (.25, 3.5, 3.5),
    "tongue_a": (1.5, .5, 7), "tongue_b": (2.5, .75, 1.75), "throat": (6, 3.5, 4.5),
    "strand_s": (1.75, 6, 1), "strand_m": (1.75, 9, 1), "strand_l": (1.75, 12, 1),
    "hips": (10, 3, 6.5), "mid": (8, 3.5, 5.5), "chest": (11, 5, 6.5), "belly_f": (7, 3.5, 3.5), "belly_s": (3.5, 3.5, 5),
    "arm_up": (3, 4.5, 3), "forearm": (2.5, 4, 2.5), "palm": (3, 1.5, 2),
    "fing_a": (.5, 1.5, .5), "fing_b": (.5, 1.75, .5), "pad_h": (.75, .75, .75),
    "thigh": (5, 7, 5), "shin": (3.5, 7, 3.5), "heel": (4.5, 1.25, 3.5),
    "toe_a": (.75, 1, 3.5), "toe_b": (.75, 1, 5), "toe_c": (.75, 1, 5.5), "toe_d": (.75, 1, 4), "pad_t": (1, .75, 1), "web_f": (4, .25, 4),
}


def declare_parts():
    A.declare_base(SIZES)
    for args in [
        ("spike_a", 1.5, 3, 1.5), ("spike_b", 1.5, 4, 1.5), ("spike_c", 1.5, 5, 1.5), ("ring", .5, 4.5, 4.5),
        ("mantle", 13, 2.5, 8.5), ("cape", 10, 7, .75), ("pauldron", 5, .5, 5),
        ("belt", 11.5, 1.5, 7.5), ("buckle", 2.5, 2, .5), ("skull", 1.5, 1.5, 1.5), ("shell", 1.5, 1.5, 1.5), ("tooth", .75, 2, .5),
        ("kilt", 3.5, 5, .5), ("kilt_s", .5, 5, 3.5), ("bracer", 3, 2, 3),
    ]:
        H.part(*args)


PAL = A.new_pal(
    BACK=rg([(28, 34, 14), (56, 70, 26), (94, 112, 40), (136, 150, 62), (180, 190, 98)], nodither=True),
    BELLY=rg([(120, 118, 90), (176, 172, 128), (214, 208, 160), (238, 232, 188)], nodither=True),
    spots=dict(seed=91, cell=8, rmin=2.0, rmax=4.4, depth=0.40), belly_spots=True, side_split=0.55, gloss=1, lid=0.12,
    stripe=rg([(150, 140, 70), (196, 186, 100), (226, 218, 136)], nodither=True),
    EYE=rg([(50, 28, 6), (100, 60, 12), (160, 104, 24), (204, 152, 48), (236, 196, 96)]),
)
PAL.EYE_OUT, PAL.PUPIL, PAL.hpupil = H.rgb(24, 20, 8), H.rgb(10, 8, 6), (0.55, 0.55)
PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE, PAL.NOSTRIL = H.rgb(30, 34, 14), H.rgb(120, 38, 46), H.rgb(224, 100, 108), H.rgb(30, 34, 14)
PAL.TONGUE_R = rg([(150, 56, 66), (196, 84, 94), (228, 116, 124), (248, 158, 164)])
PAL.BLUSH, PAL.BLUSH_L = H.rgb(168, 124, 54), H.rgb(196, 156, 84)
PAL.DRUM = rg([(60, 56, 26), (110, 104, 48), (160, 152, 80), (198, 190, 116)])
PAL.SAC = rg([(150, 120, 40), (200, 170, 64), (232, 208, 104), (250, 236, 154)])
PAL.PAD = rg([(96, 100, 50), (144, 150, 80), (190, 196, 112), (226, 230, 152)])
PAL.HAIR = rg([(30, 22, 12), (54, 40, 20), (84, 64, 32), (120, 94, 50), (160, 130, 76), (200, 172, 112)])
PAL._mouth_y = SPEC.mouth_y

PELT = rg([(22, 14, 10), (46, 30, 20), (74, 52, 34), (110, 84, 56), (150, 120, 84)])
GOLD = rg([(110, 74, 10), (180, 130, 20), (236, 190, 50), (255, 232, 130)])
LEATHER = rg([(36, 24, 16), (66, 46, 30), (100, 72, 46), (136, 102, 68)])
BONE = rg([(150, 140, 118), (204, 196, 172), (240, 234, 212)])
LILY = rg([(26, 74, 46), (40, 112, 62), (66, 156, 82), (108, 196, 108)])
RUSH = rg([(96, 76, 36), (140, 112, 54), (184, 154, 84), (220, 194, 122)])
SHELLC = rg([(110, 80, 56), (166, 126, 86), (214, 180, 130), (240, 222, 178)])


def paint():
    pt = A.paint_base(PAL, curve=-1.0, fringe=3, hum=SPEC.hum)

    def spike(f, x, y, w, h):
        if f in BROAD:
            r = y / max(1, h - 1)
            half = (w / 2.0) * (0.25 + 0.75 * r)                                    # a tapering spike, point up
            if abs(x + 0.5 - w / 2.0) > half:
                return CLEAR
        return A.hair_px(PAL, x, y, h, False)
    for p in ("spike_a", "spike_b", "spike_c"):
        pt(p, "all", spike)

    def ring(f, x, y, w, h):                                                        # a gold hoop around the ear disc
        if f not in ("east", "west"):
            return GOLD[1]
        d = max(abs((x + 0.5 - w / 2.0) / (w / 2.0)), abs((y + 0.5 - h / 2.0) / (h / 2.0)))
        r = (((x + 0.5 - w / 2.0) / (w / 2.0)) ** 2 + ((y + 0.5 - h / 2.0) / (h / 2.0)) ** 2) ** 0.5
        if r > 1.0 or r < 0.74:
            return CLEAR
        return pick(GOLD, 0.85 - 0.5 * (r > 0.9) + (0.15 if (x + y) % 5 == 0 else 0), x, y)
    pt("ring", "all", ring)

    def fur(f, x, y, w, h):
        t = 0.50 + (noise(x, y, 5) - 0.5) * 0.28 + 0.12 * (noise(x // 2, y // 3, 7) > 0.7)
        if y >= h - 2 and f in BROAD and noise(x, y, 9) > 0.45:
            return CLEAR
        return pick(PELT, t + (0.1 if f == "up" else 0), x, y)
    pt("mantle", "all", fur)
    pt("cape", "all", fur)

    def pauldron(f, x, y, w, h):
        if f not in ("up", "down"):
            return CLEAR
        dx, dy = (x + 0.5 - w / 2.0) / (w / 2.0), (y + 0.5 - h / 2.0) / (h / 2.0)
        if dx * dx + dy * dy > 1.0:
            return CLEAR
        if f == "down":
            return pick(LILY, 0.25, x, y)
        ang = (abs(dx) + abs(dy))
        return pick(LILY, 0.58 + 0.2 * ((int(ang * 7) % 2) == 0) + (noise(x, y, 11) - 0.5) * 0.1 - 0.15 * (dx * dx + dy * dy > 0.8), x, y)
    pt("pauldron", "all", pauldron)

    pt("belt", "all", lambda f, x, y, w, h: LEATHER[0] if (y < 1 or y >= h - 1) else pick(LEATHER, 0.55 + (noise(x, y, 13) - 0.5) * 0.12 + 0.12 * (x % 8 == 0), x, y))
    pt("buckle", "all", lambda f, x, y, w, h: pick(GOLD, 0.88 - 0.55 * (x < 2 or x > w - 3 or y < 2 or y > h - 3) + (0.1 if (x, y) == (3, 3) else 0), x, y))
    pt("skull", "all", lambda f, x, y, w, h: pick(BONE, 0.7 - 0.5 * ((f == "north") and ((x in (1, 2) and y in (1, 2)) or (x in (w - 3, w - 2) and y in (1, 2)))), x, y))
    pt("shell", "all", lambda f, x, y, w, h: pick(SHELLC, 0.35 + 0.35 * (((x + y) // 2) % 2) + (noise(x, y, 15) - 0.5) * 0.1, x, y))
    pt("tooth", "all", lambda f, x, y, w, h: pick(BONE, 0.9 - 0.4 * (y < 2), x, y))

    def kilt(f, x, y, w, h):
        if f in ("up", "down"):
            return RUSH[1]
        if y > h - 3 and noise(x, y, 17) > 0.5:
            return CLEAR
        return pick(RUSH, 0.50 + 0.2 * ((x // 2) % 2) + (noise(x, y, 19) - 0.5) * 0.10, x, y)
    pt("kilt", "all", kilt)
    pt("kilt_s", "all", kilt)
    pt("bracer", "all", lambda f, x, y, w, h: LEATHER[0] if (y < 1 or y >= h - 1) else (pick(GOLD, 0.7, x, y) if y == h // 2 and x % 3 == 0 else pick(LEATHER, 0.55 + (noise(x, y, 21) - 0.5) * 0.12, x, y)))
    return pt.T


def extras(r, g):
    HG = H.hang
    top, z0 = g.head_top, g.head_z0
    hw = A.S("head")[0]

    # mohawk: five spikes leaning back along the centre line of the head; a short fringe under it
    for p, zz, tilt in (("spike_a", z0 + 0.5, -8), ("spike_b", z0 + 2.2, -12), ("spike_c", z0 + 4.0, -16), ("spike_b", z0 + 5.8, -22), ("spike_a", z0 + 7.2, -28)):
        pw, ph, pd = A.S(p)
        r.put("head", cube(p, [-pw / 2, top + 0.2, zz], pivot=[0, top + 0.2, zz + pd / 2], rotation=[tilt, 0, 0]))
    back = [HG(n, x, top - 1.0, 4.9 + 0.3 * (i % 2), x * 1.4, 6 + 3 * (i % 3)) for i, (n, x) in
            enumerate(zip(("strand_m", "strand_s", "strand_l", "strand_s", "strand_m"), (-3.6, -1.8, 0.0, 1.8, 3.6)))]
    r.add("hair_back", "head", [0, top - 1.0, 4.6], back)

    # gold hoops hung on the ear discs
    ring = cube("ring", [-hw / 2 - 0.55, g.head_y0 + SPEC.tymp_y - 0.5, z0 + SPEC.tymp_z - 0.5])
    r.put("head", ring, mirror_cube(ring))

    # pelt mantle round the neck and shoulders, a pelt cape, lily pauldrons
    r.put("body", cube("mantle", [-6.5, g.chest_top - 2.4, g.chest_front - 1.0]))
    r.add("cape", "body", [0, g.chest_top - 0.5, 3.8], [HG("cape", -5.0, g.chest_top - 0.5, 3.7, 0, 3)])
    r.put("arm_r", cube("pauldron", [-g.shoulder_x - 2.5, g.shoulder_y + 0.55, -2.5]))

    # trophy belt with a brass buckle, a rush kilt, bracers
    r.put("body", cube("belt", [-5.75, g.hips_y0 + 1.55, -3.75]), cube("buckle", [-1.25, g.hips_y0 + 1.4, -4.25]))
    tro = [HG("skull", -4.9, g.hips_y0 + 1.55, -4.3, 4, -2), HG("shell", -3.2, g.hips_y0 + 1.55, -4.2, 3, -2), HG("tooth", -2.6, g.hips_y0 + 1.55, -4.2, 2, -5),
           HG("tooth", 2.0, g.hips_y0 + 1.55, -4.2, -2, -5), HG("shell", 2.6, g.hips_y0 + 1.55, -4.2, -3, -2), HG("tooth", 4.6, g.hips_y0 + 1.55, -4.3, -4, -3)]
    r.add("trophies", "body", [0, g.hips_y0 + 1.55, -4.0], tro)
    ky = g.hips_y0 + 1.5
    kilt = [HG("kilt", -3.5, ky, -3.9, 3, -5), HG("kilt", 0.0, ky, -3.9, -3, -5), HG("kilt", -3.5, ky, 3.4, 3, 5), HG("kilt", 0.0, ky, 3.4, -3, 5),
            HG("kilt_s", -5.8, ky, -1.75, -5, 0), HG("kilt_s", 5.3, ky, -1.75, 5, 0)]
    r.add("kilt", "body", [0, ky, 0], kilt)
    r.put("forearm_r", cube("bracer", [-g.shoulder_x - 1.5, g.shoulder_y - 4.5 + 0.6 - 4 + 0.7, -1.5]))


def build_model():
    m, g = A.build(SPEC, extras, NAME)
    build_model.g = g
    return m


TUNE = SimpleNamespace(crouch=10, step=15, hop_lift=5.5, throat_out=(0, -2.4, -3.4), puff_f=1.8, croak_crouch=22, sit_crouch=58,
                       tongue_len=6.5, arm_rest=(3, 0, 6), extra={})


def animations(an):
    g = build_model.g
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    OUT, REST = H.OUT_, H.REST
    sway = lambda amp, per=4.0: T((0, (0, 0, 0)), (per / 4, (amp, 0, 0)), (per / 2, (0, 0, 0)), (3 * per / 4, (-amp, 0, 0)), (per, (0, 0, 0)))
    TUNE.extra = {
        "idle": {"kilt": {"rotation": sway(2)}, "cape": {"rotation": sway(1.5)}, "trophies": {"rotation": sway(3, 3.0)}},
        "walk": {"kilt": {"rotation": T((0, (0, 0, 4)), (0.5, (0, 0, -4)), (1, (0, 0, 4)))}, "trophies": {"rotation": T((0, (0, 0, 5)), (0.5, (0, 0, -5)), (1, (0, 0, 5)))}, "cape": {"rotation": T((0, (4, 0, 2)), (0.5, (4, 0, -2)), (1, (4, 0, 2)))}},
        "hop": {"kilt": {"rotation": T((0, (0, 0, 0)), (0.3, (-22, 0, 0)), (0.74, (12, 0, 0)), (0.9, (0, 0, 0)))}, "cape": {"rotation": T((0, (0, 0, 0)), (0.3, (-30, 0, 0)), (0.74, (14, 0, 0)), (0.9, (0, 0, 0)))}, "trophies": {"rotation": sway(10, 0.9)}},
        "swim": {"kilt": {"rotation": hold(-30, 0, 0)}, "cape": {"rotation": hold(-60, 0, 0)}, "trophies": {"rotation": hold(-30, 0, 0)}},
        "croak": {"trophies": {"rotation": sway(3, 3.0)}},
        "sit": {"kilt": {"rotation": hold(6, 0, 0)}, "cape": {"rotation": hold(-6, 0, 0)}},
    }
    A.common_anims(an, g, TUNE)
    pose = lambda deg: (-deg, 2 * deg, 0, 0)

    def stand(period, deg=6):
        lg, by = A.gait(g.T, g.Sh, period, [(0, pose(deg)), (period, pose(deg))], [(0, pose(deg)), (period, pose(deg))])
        for s_ in ("r", "l"):
            lg["leg_" + s_]["position"] = by
        lg["body"] = {"position": by}
        return lg

    # bellow: the deep pond-boss roar - chest out, head back, sac fully swollen, mouth wide, fists out
    sac = (0, -2.9, -3.9)
    wob = [(0, REST), (0.25, sac), (0.55, (0, -2.4, -3.4)), (0.85, sac), (1.15, (0, -2.4, -3.4)), (1.45, sac), (1.75, (0, -2.4, -3.4)), (2.05, sac), (2.4, sac), (2.8, REST), (2.9, REST)]
    lg = stand(2.9)
    lg["body"]["rotation"] = T((0, (0, 0, 0)), (0.35, (-14, 0, 0)), (2.4, (-14, 0, 0)), (2.9, (0, 0, 0)))
    an.add("bellow", 2.9, True, merge(
        lg,
        {"throat": {"position": T(*wob)},
         "mouth": {"position": T((0, REST), (0.35, OUT), (2.4, OUT), (2.8, REST), (2.9, REST))},
         "belly": {"position": T((0, REST), (0.35, (0, 0, -1.8)), (2.4, (0, 0, -1.8)), (2.9, REST))},
         "head": {"rotation": T((0, (0, 0, 0)), (0.35, (-34, 0, 0)), (2.4, (-34, 0, 0)), (2.9, (0, 0, 0)))},
         "eyelids": {"position": T((0, REST), (0.35, OUT), (2.4, OUT), (2.9, REST))},
         "kilt": {"rotation": T((0, (0, 0, 0)), (0.35, (-12, 0, 0)), (2.4, (-12, 0, 0)), (2.9, (0, 0, 0)))}},
        sym("arm", T((0, (3, 0, 6)), (0.35, (-14, 0, 40)), (2.4, (-14, 0, 40)), (2.9, (3, 0, 6)))), sym("forearm", hold(-40, 0, 0))))

    # pound: beating her chest, alternating fists, head back, a short roar on each hit
    lg = stand(1.6)
    lg["body"]["rotation"] = T((0, (-6, 0, 0)), (0.4, (-8, 0, 2)), (0.8, (-6, 0, 0)), (1.2, (-8, 0, -2)), (1.6, (-6, 0, 0)))
    an.add("pound", 1.6, True, merge(
        lg,
        {"head": {"rotation": hold(-20, 0, 0)}, "mouth": {"position": T((0, REST), (0.15, OUT), (0.4, OUT), (0.5, REST), (0.95, REST), (1.0, OUT), (1.25, OUT), (1.35, REST), (1.6, REST))},
         "belly": {"position": T((0, REST), (0.2, (0, 0, -0.6)), (0.5, REST), (1.0, (0, 0, -0.6)), (1.3, REST), (1.6, REST))},
         "throat": {"position": T((0, REST), (0.2, (0, -1.2, -1.6)), (0.5, REST), (1.0, (0, -1.2, -1.6)), (1.3, REST), (1.6, REST))},
         "trophies": {"rotation": sway(8, 0.8)}},
        {"arm_r": {"rotation": T((0, (-40, 0, -14)), (0.2, (-70, 0, -30)), (0.4, (-40, 0, -14)), (1.6, (-40, 0, -14)))},
         "arm_l": {"rotation": T((0, (-40, 0, 14)), (0.8, (-40, 0, 14)), (1.0, (-70, 0, 30)), (1.2, (-40, 0, 14)), (1.6, (-40, 0, 14)))},
         "forearm_r": {"rotation": hold(-100, 0, 0)}, "forearm_l": {"rotation": hold(-100, 0, 0)}}))

    # shove: both fists driven forward, shoulders behind it
    lg = A.gait(g.T, g.Sh, 1.2, [(0, pose(14)), (0.3, pose(24)), (0.5, pose(2)), (1.0, pose(14)), (1.2, pose(14))],
                [(0, pose(14)), (0.3, pose(24)), (0.5, pose(2)), (1.0, pose(14)), (1.2, pose(14))])
    legs, by = lg
    for s_ in ("r", "l"):
        legs["leg_" + s_]["position"] = by
    legs["body"] = {"position": by, "rotation": T((0, (2, 0, 0)), (0.3, (-8, 0, 0)), (0.5, (24, 0, 0)), (1.0, (2, 0, 0)), (1.2, (2, 0, 0)))}
    an.add("shove", 1.2, True, merge(
        legs,
        {"head": {"rotation": T((0, (0, 0, 0)), (0.5, (-18, 0, 0)), (1.0, (0, 0, 0)), (1.2, (0, 0, 0)))},
         "mouth": {"position": T((0, REST), (0.45, OUT), (0.8, OUT), (0.9, REST), (1.2, REST))},
         "kilt": {"rotation": T((0, (0, 0, 0)), (0.5, (-20, 0, 0)), (1.0, (0, 0, 0)), (1.2, (0, 0, 0)))}},
        sym("arm", T((0, (3, 0, 6)), (0.3, (-30, 0, 20)), (0.5, (-92, 0, 8)), (1.0, (3, 0, 6)), (1.2, (3, 0, 6)))),
        sym("forearm", T((0, (-8, 0, 0)), (0.3, (-90, 0, 0)), (0.5, (-6, 0, 0)), (1.0, (-8, 0, 0)), (1.2, (-8, 0, 0))))))


def main():
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="bellow", head_center=(0, 28.0), body_center=(0, 19.5), scale=11.5,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("bellow", 1.0, -28, 8), ("pound", 0.2, -30, 8), ("shove", 0.5, -40, 8), ("hop", 0.4, -30, 10)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("bellow", 1.0, 0, 0), ("tongue", 0.3, 0, 0), ("idle", 0, -30, 8)])


if __name__ == "__main__":
    main()
