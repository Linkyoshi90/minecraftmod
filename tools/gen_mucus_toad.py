#!/usr/bin/env python3
"""Mucus Toad - species definition (the toad lineage). Shared engine: anuran_lib.py on top of harpy_lib.py.

    python tools/gen_mucus_toad.py                    # write geo / animation / texture
    python tools/gen_mucus_toad.py --preview x.png    # also render x.png, x_face.png, x_body.png, x_texture.png

Concept (field guide: "Mucus Toad - placid, slow, stubborn, sleepy; damp woodland, caves and farm edges; slippery skin and a
bitter secretion"): a short, stocky toad herbalist, about 1.5 blocks (1.7 with the hat). Warty olive-brown skin under a
glossy film of mucus, big tan parotoid glands behind the eyes that drip, heavy sleepy copper eyes, a placid flat mouth.
Outfit: a toadstool-cap hat on a cream stem, a damp moss cloak over a shoulder mantle, a belt of salve jars (her own
mucus, bottled), a little satchel and a leather apron. Moss-green hair, long and clumped with damp.
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
NAME = "mucus_toad" if HUMANOID else "mucus_toad_frogface"

SPEC = SimpleNamespace(legx=3.0, eye_x=3.0, eye_out=0.25, eye_rise=1.0, head_sink=1.0, head_fwd=0.25, chest_fwd=0.0,
                       mouth_y=1.0, throat_y=0.5, tymp_y=1.6, tymp_z=3.4)
SIZES = {
    "head": (10, 6, 8), "eye": (3.25, 3, 3), "eyelid": (3.25, 3, .125), "tymp": (.25, 2.5, 2.5), "mouth": (7.5, 1.75, .125),
    "throat": (5, 2.5, 4), "hips": (10, 3, 6.5), "mid": (7.5, 3, 5.5), "chest": (9, 4.5, 6), "belly_f": (7, 3.5, 3.5),
    "belly_s": (3.5, 3, 5), "arm_up": (2.5, 3, 2.5), "forearm": (2.25, 3, 2.25), "palm": (2.75, 1.25, 1.75),
    "fing_a": (.75, 1, .75), "fing_b": (.75, 1.25, .75), "pad_h": (1, .5, 1),
    "thigh": (4.5, 4.5, 4.5), "shin": (3, 3.5, 3), "heel": (4, 1, 3), "toe_a": (1, .75, 2), "toe_b": (1, .75, 3),
    "toe_c": (1, .75, 3.25), "toe_d": (1, .75, 2.5), "pad_t": (1.25, .75, 1.25), "web_f": (4, .25, 2),
    "strand_s": (2, 5, 1), "strand_m": (2, 8, 1), "strand_l": (2, 11, 1),
}


if HUMANOID:
    SIZES.update(head=(9, 7, 8), eyelid=(3, 3, .125), mouth=(3, 1.5, .125))
    SPEC.hum = dict(sep=2.75, y=3.6, a=5.2, b=5.4, mw=1.6)
    SPEC.mouth_y = 0.75


def declare_parts():
    A.declare_base(SIZES)
    for args in [
        ("gland", 3.25, 2.25, 3.5), ("drip", .75, 2.5, .75), ("wart", 1, .75, 1),
        ("hat_stem", 6, 1.75, 5), ("hat_dome", 8.5, 2.5, 7.5), ("hat_brim", 13, .75, 11),
        ("cloak", 10.5, 8, .75), ("mantle", 11.5, 1.75, 7), ("belt", 10.5, 1, 7), ("apron", 6, 4.5, .5), ("stitch", 6.5, .5, .5),
        ("jar", 1.5, 2, 1.5), ("cork", 1.5, .5, 1.5), ("bag", 3, 3, 1.5), ("bag_flap", 3.25, 1.25, 1.75), ("strap", .75, 7, .5),
    ]:
        H.part(*args)


PAL = A.new_pal(
    BACK=rg([(44, 36, 22), (78, 62, 34), (116, 94, 50), (156, 130, 72), (200, 172, 108)], nodither=True),
    BELLY=rg([(150, 138, 98), (190, 178, 128), (220, 208, 156), (240, 230, 184)], nodither=True),
    spots=dict(seed=33, cell=6, rmin=1.4, rmax=3.0, depth=0.34),
    warts=dict(seed=41, cell=5, density=0.55, ramp=rg([(90, 60, 30), (140, 96, 48), (190, 140, 76), (226, 184, 112)])),
    mucus=(H.rgb(176, 224, 176), 0.035), side_split=0.50, gloss=2, lid=0.38, pupil=(0.62, 0.20),
    EYE=rg([(80, 40, 10), (130, 70, 18), (184, 110, 30), (224, 152, 50), (246, 190, 90)]),
)
PAL.EYE_OUT = H.rgb(36, 26, 14)
PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE, PAL.NOSTRIL = H.rgb(44, 34, 18), H.rgb(120, 46, 50), H.rgb(214, 108, 112), H.rgb(30, 24, 14)
PAL.TONGUE_R = rg([(150, 60, 70), (196, 88, 96), (226, 120, 128), (246, 160, 166)])
PAL.BLUSH, PAL.BLUSH_L = H.rgb(196, 116, 90), H.rgb(220, 150, 116)
PAL.DRUM = rg([(60, 48, 28), (100, 80, 44), (140, 116, 64), (176, 150, 90)])
PAL.SAC = rg([(160, 120, 90), (204, 164, 120), (232, 200, 156), (248, 226, 186)])
PAL.PAD = rg([(120, 96, 56), (170, 140, 84), (204, 176, 112), (230, 206, 144)])
PAL.HAIR = rg([(24, 30, 18), (46, 58, 28), (76, 92, 40), (112, 128, 60), (150, 164, 92), (190, 200, 130)])
PAL._mouth_y, PAL.nostril_dx, PAL.nostril_up = SPEC.mouth_y, 3.2, 3

GLAND = rg([(110, 70, 30), (160, 104, 44), (206, 146, 66), (240, 190, 110)])
MUCUS = rg([(120, 180, 140), (170, 220, 170), (214, 246, 206)])
CAP = rg([(84, 28, 22), (132, 44, 32), (176, 66, 44), (208, 98, 66)])
CREAM = rg([(184, 170, 132), (224, 212, 172), (248, 240, 206)])
MOSS = rg([(22, 34, 20), (38, 56, 28), (62, 86, 40), (94, 120, 58), (134, 156, 88)])
LEATHER = rg([(44, 30, 20), (78, 54, 34), (112, 82, 52), (148, 112, 74)])
GLASS = rg([(90, 140, 110), (140, 190, 150), (196, 232, 200)])


def paint():
    pt = A.paint_base(PAL, curve=-1.2, fringe=4 if HUMANOID else 0, hum=getattr(SPEC, "hum", None))

    def gland(f, x, y, w, h):
        e = (noise(x, y, 51) - 0.5) * 0.10
        t = 0.62 + e + (0.25 if f in ("up", "north", "west", "east") and y < 3 else 0) - (0.12 if f in ("south", "down") else 0)
        if noise(x // 2, y // 2, 53) > 0.80:
            return pick(GLAND, 0.15, x, y)                               # dark pores
        if (x * 3 + y * 5) % 11 == 0:
            return pick(MUCUS, 0.7, x, y)                                # a wet shine
        return pick(GLAND, t, x, y)
    pt("gland", "all", gland)
    pt("drip", "all", lambda f, x, y, w, h: pick(MUCUS, 0.62 + (0.3 if (x < 1 and y > 1) else 0) - (0.3 if y > h - 3 else 0), x, y))

    def wart(f, x, y, w, h):
        return pick(PAL.warts["ramp"], 0.9 if (f == "up" or (x < 2 and y < 2)) else 0.45 - 0.2 * (y > h - 2), x, y)
    pt("wart", "all", wart)

    def hat_top(f, x, y, w, h, spots=True):
        t = 0.56 + (noise(x, y, 61) - 0.5) * 0.10
        if spots:
            b = A.blob_t(x, y, 63, 7, 1.2, 2.6)
            if b > 0.35:
                return pick(CREAM, 0.7 + 0.2 * b, x, y)
        return pick(CAP, t, x, y)
    pt("hat_dome", "all", lambda f, x, y, w, h: hat_top(f, x, y, w, h) if f != "down" else CREAM[1])
    pt("hat_brim", ("up", "north", "south", "east", "west"), lambda f, x, y, w, h: hat_top(f, x, y, w, h) if f == "up" else CAP[1])

    def gills(f, x, y, w, h):
        ang = math.atan2(x + 0.5 - w / 2.0, y + 0.5 - h / 2.0)
        r = math.hypot((x + 0.5 - w / 2.0) / w, (y + 0.5 - h / 2.0) / h)
        return pick(CREAM, 0.30 + 0.30 * (int((ang + math.pi) / (2 * math.pi) * 40) % 2) + (0.15 if r > 0.45 else 0), x, y)
    pt("hat_brim", ("down",), gills)
    pt("hat_stem", "all", lambda f, x, y, w, h: pick(CREAM, 0.55 + 0.2 * (x % 4 == 0) + (noise(x, y, 67) - 0.5) * 0.1, x, y))

    def cloak(f, x, y, w, h):
        if f not in BROAD:
            return MOSS[1]
        if y > h - 5 and (noise(x, y, 71) > 0.40 + 0.5 * (h - 1 - y) / 5.0 or (x % 5 == 2 and y > h - 3)):
            return CLEAR                                                     # ragged hem
        t = 0.42 + (noise(x, y, 73) - 0.5) * 0.14 + 0.25 * (A.blob_t(x, y, 75, 6, 1.5, 3.2) > 0.4) - 0.1 * (y / h)
        return pick(MOSS, t, x, y)
    pt("cloak", "all", cloak)
    pt("mantle", "all", lambda f, x, y, w, h: pick(MOSS, 0.50 + 0.22 * (A.blob_t(x, y, 77, 5, 1.2, 2.6) > 0.4) + (noise(x, y, 79) - 0.5) * 0.12 + (0.12 if f == "up" else 0), x, y))

    def belt(f, x, y, w, h):
        return LEATHER[0] if (y < 1 or y >= h - 1) else pick(LEATHER, 0.55 + (noise(x, y, 81) - 0.5) * 0.12 + 0.1 * (x % 6 == 0), x, y)
    pt("belt", "all", belt)
    pt("apron", "all", lambda f, x, y, w, h: pick(LEATHER, 0.50 + (noise(x, y, 83) - 0.5) * 0.14 + 0.18 * (A.blob_t(x, y, 85, 6, 1.5, 3) > 0.4), x, y) if not (f in BROAD and y > h - 3 and x % 4 == 1) else CLEAR)
    pt("stitch", "all", lambda f, x, y, w, h: CREAM[1] if x % 3 else LEATHER[0])

    def jar(f, x, y, w, h):
        if f in BROAD + ("east", "west"):
            if y > h - 3:
                return pick(MUCUS, 0.45, x, y)                            # the salve inside
            if y > 1:
                return pick(GLASS, 0.55 + 0.35 * (x < 2) - 0.2 * (x > w - 3), x, y)
        return pick(MUCUS, 0.4, x, y)
    pt("jar", "all", jar)
    pt("cork", "all", lambda f, x, y, w, h: pick(LEATHER, 0.78 + (noise(x, y, 87) - 0.5) * 0.2, x, y))
    pt("bag", "all", lambda f, x, y, w, h: pick(LEATHER, 0.50 + (noise(x, y, 89) - 0.5) * 0.14, x, y))
    pt("bag_flap", "all", lambda f, x, y, w, h: LEATHER[0] if y >= h - 1 else pick(LEATHER, 0.62 + (noise(x, y, 91) - 0.5) * 0.14, x, y))
    pt("strap", "all", lambda f, x, y, w, h: pick(LEATHER, 0.45, x, y))
    return pt.T


def extras(r, g):
    HG = H.hang
    top = g.head_top
    hw = A.S("head")[0]
    z0 = g.head_z0

    # parotoid glands behind the eyes, each with a hanging drip of mucus
    gl = cube("gland", [-hw / 2 - 0.25, top - 1.5, z0 + 3.0])
    r.put("head", gl, mirror_cube(gl))
    r.add("drip_r", "head", [-hw / 2, top - 1.5, z0 + 6.3], [cube("drip", [-hw / 2 - 0.5, top - 5.2, z0 + 5.5])], mirror=True)

    # hat: cream stem, cap and a wide gilled brim, set back so the eyes stay clear
    r.put("head", cube("hat_stem", [-3.0, top, z0 + 3.5]), cube("hat_brim", [-6.5, top + 1.75, z0 + 0.25]),
          cube("hat_dome", [-4.25, top + 2.5, z0 + 1.75]))

    # hair: a bang between the eyes, long wet locks over the mantle, damp hair down the back
    r.put("head", HG("bang_a", -1.0, top + 0.3, z0 - 0.5, 2, -4))
    side = [("strand_l", -6.3, top - 1.6, -1.0, -4, 0), ("strand_m", -6.3, top - 1.6, 1.4, -5, 2), ("strand_l", -6.3, top - 1.6, 3.2, -7, 4)]
    r.add("hair_side_r", "head", [-5.0, top - 1.5, 0], [HG(*s) for s in side], mirror=True)
    back = [HG(n, x, top - 1.6, 4.1 + 0.25 * (i % 3), x * 1.0, 4 + 2 * (i % 3)) for i, (n, x) in
            enumerate(zip(("strand_m", "strand_l", "strand_m", "strand_l", "strand_m", "strand_l"), (-4.5, -2.7, -0.9, 0.9, 2.7, 4.5)))]
    r.add("hair_back", "head", [0, top - 1.6, 4.0], back)

    # mantle and a hanging moss cloak
    r.put("body", cube("mantle", [-5.75, g.chest_top - 1.75, -3.5]))
    r.add("cloak", "body", [0, g.chest_top - 0.5, 3.4], [HG("cloak", -5.25, g.chest_top - 0.5, 3.4, 0, 3)])

    # belt, apron, salve jars, satchel
    r.put("body", cube("belt", [-5.25, g.hips_y0 + 2.0, -3.5]))
    r.add("apron", "body", [0, g.hips_y0 + 2.4, -3.6], [HG("apron", -3.0, g.hips_y0 + 2.4, -3.85, 0, -4), HG("stitch", -3.25, g.hips_y0 + 2.5, -3.9, 0, -3)])
    jars = []
    for x, z, rz in ((-4.6, -1.2, -6), (4.4, -1.6, 7), (-4.7, 1.4, -5)):
        jars.append(HG("jar", x - 0.75, g.hips_y0 + 1.8, z - 0.75 + (0 if z < 0 else 0.0), rz, 0))
        jars.append(HG("cork", x - 0.75, g.hips_y0 + 2.35, z - 0.75, rz, 0))
    r.add("jars", "body", [0, g.hips_y0 + 1.8, 0], jars)
    bag = [HG("bag", 4.2, g.hips_y0 + 0.5, 0.2, 2, 0), HG("bag_flap", 4.1, g.hips_y0 + 0.9, 0.1, 2, 0)]
    r.add("satchel", "body", [4.2, g.hips_y0 + 1.5, 0.5], bag)
    r.put("body", cube("strap", [-0.4, g.chest_y0 - 1.0, g.chest_front - 0.3], pivot=[0, g.chest_y0 + 3.0, 0], rotation=[0, 0, 38]))

    # warts on the limbs and shoulders (leg/arm bones mirror, so they land on both sides)
    cx = -SPEC.legx
    tw = A.S("thigh")[0]
    warts = [(cx - tw / 2 - 0.5, g.knee_y + 1.5, 0.5), (cx - tw / 2 - 0.5, g.knee_y + 3.5, -1.2), (cx - tw / 2 - 0.5, g.knee_y + 2.6, 1.6)]
    for x, y, z in warts:
        r.put("leg_r", cube("wart", [x, y, z]))
    r.put("leg_r", cube("wart", [cx - 0.5, g.knee_y + 4.0, -2.5]), cube("wart", [cx + 0.9, g.knee_y + 3.4, -2.4]))
    r.put("shin_r", cube("wart", [cx - 1.9, g.ankle_y + 1.3, 0.4]), cube("wart", [cx - 1.9, g.ankle_y + 2.6, 1.2]))
    r.put("arm_r", cube("wart", [-g.shoulder_x - 1.75, g.shoulder_y - 2.0, -0.4]))
    r.put("body", cube("wart", [-3.0, g.hips_y0 + 1.2, 3.2]), cube("wart", [1.4, g.hips_y0 + 2.0, 3.2]), cube("wart", [-1.0, g.mid_y0 + 0.5, 2.7]))
    r.put("head", cube("wart", [-1.0, top - 0.3, z0 + 1.0]), cube("wart", [1.6, top - 0.3, z0 + 1.8]))


def build_model():
    m, g = A.build(SPEC, extras, NAME)
    build_model.g = g
    return m


TUNE = SimpleNamespace(crouch=8, step=14, hop_lift=3.0, throat_out=(0, -1.5, -2.2), puff_f=1.4, croak_crouch=18, sit_crouch=58,
                       tongue_len=5.8, arm_rest=(3, 0, 5), extra={})


def animations(an):
    g = build_model.g
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    OUT, REST = H.OUT_, H.REST
    sway = lambda amp, per=4.0: T((0, (0, 0, 0)), (per / 4, (amp, 0, 0)), (per / 2, (0, 0, 0)), (3 * per / 4, (-amp, 0, 0)), (per, (0, 0, 0)))
    drip = lambda per, ph=0.0: T((0, (0, 0, 0)), (per * 0.7, (0, -1.6, 0)), (per * 0.72, (0, -3.4, 0)), (per * 0.74, (0, 0, 0)), (per, (0, 0, 0)))
    TUNE.extra = {
        "idle": {"cloak": {"rotation": sway(1.5)}, "jars": {"rotation": sway(2.5)}, "drip_r": {"position": drip(4.0)}, "drip_l": {"position": drip(3.0)},
                 "satchel": {"rotation": sway(1.5)}},
        "walk": {"cloak": {"rotation": T((0, (4, 0, 2)), (0.5, (4, 0, -2)), (1, (4, 0, 2)))}, "jars": {"rotation": T((0, (0, 0, 5)), (0.5, (0, 0, -5)), (1, (0, 0, 5)))},
                 "satchel": {"rotation": T((0, (0, 0, 4)), (0.5, (0, 0, -4)), (1, (0, 0, 4)))}},
        "hop": {"cloak": {"rotation": T((0, (0, 0, 0)), (0.3, (-22, 0, 0)), (0.74, (12, 0, 0)), (0.9, (0, 0, 0)))}},
        "swim": {"cloak": {"rotation": hold(-60, 0, 0)}, "jars": {"rotation": hold(-30, 0, 0)}},
        "croak": {"cloak": {"rotation": sway(2, 3.0)}, "drip_r": {"position": drip(3.0)}},
        "sit": {"cloak": {"rotation": hold(8, 0, 0)}, "drip_r": {"position": drip(4.0)}},
    }
    A.common_anims(an, g, TUNE)

    # puff: the defence - she straightens up and swells, eyes screwed shut, glands bulging, mucus running
    stand = A.gait(g.T, g.Sh, 2.0, [(0, (-2, 6, 0, 0)), (2, (-2, 6, 0, 0))], [(0, (-2, 6, 0, 0)), (2, (-2, 6, 0, 0))])
    lg, by = stand
    for s_ in ("r", "l"):
        lg["leg_" + s_]["position"] = by
    lg["body"] = {"position": by}
    an.add("puff", 2.0, True, merge(
        lg,
        {"belly": {"position": T((0, REST), (0.35, (0, 0, -1.7)), (1.6, (0, 0, -1.7)), (2, REST))},
         "belly_r": {"position": T((0, REST), (0.35, (-1.5, 0, 0)), (1.6, (-1.5, 0, 0)), (2, REST))},
         "belly_l": {"position": T((0, REST), (0.35, (1.5, 0, 0)), (1.6, (1.5, 0, 0)), (2, REST))},
         "throat": {"position": T((0, REST), (0.35, (0, -1.2, -1.6)), (1.6, (0, -1.2, -1.6)), (2, REST))},
         "eyelids": {"position": T((0, REST), (0.3, OUT), (1.7, OUT), (2, REST))},
         "head": {"rotation": T((0, (0, 0, 0)), (0.35, (8, 0, 0)), (1.0, (8, -4, 0)), (1.6, (8, 4, 0)), (2, (0, 0, 0)))},
         "cloak": {"rotation": T((0, (0, 0, 0)), (0.35, (14, 0, 0)), (1.6, (14, 0, 0)), (2, (0, 0, 0)))},
         "jars": {"rotation": T((0, (0, 0, 0)), (0.35, (0, 0, 10)), (0.5, (0, 0, 4)), (0.65, (0, 0, 10)), (1.6, (0, 0, 8)), (2, (0, 0, 0)))},
         "drip_r": {"position": T((0, (0, 0, 0)), (0.7, (0, -1.6, 0)), (0.74, (0, -3.4, 0)), (0.78, (0, 0, 0)), (1.2, (0, -1.6, 0)), (1.25, (0, -3.4, 0)), (1.3, (0, 0, 0)), (2, (0, 0, 0)))},
         "drip_l": {"position": T((0, (0, 0, 0)), (0.5, (0, -1.6, 0)), (0.54, (0, -3.4, 0)), (0.58, (0, 0, 0)), (1.4, (0, -1.6, 0)), (1.45, (0, -3.4, 0)), (1.5, (0, 0, 0)), (2, (0, 0, 0)))}},
        sym("arm", T((0, (3, 0, 5)), (0.35, (6, 0, 30)), (1.6, (6, 0, 30)), (2, (3, 0, 5)))), sym("forearm", hold(-18, 0, 0))))

    # dig: the forepaws scoop and the body sinks a little
    c = 26
    pose = (-c, 2 * c, 0, 0)
    lg, by = A.gait(g.T, g.Sh, 0.8, [(0, pose), (0.8, pose)], [(0, pose), (0.8, pose)])
    for s_ in ("r", "l"):
        lg["leg_" + s_]["position"] = by
    lg["body"] = {"position": by, "rotation": T((0, (22, 0, 0)), (0.4, (26, 0, 0)), (0.8, (22, 0, 0)))}
    an.add("dig", 0.8, True, merge(
        lg,
        {"head": {"rotation": hold(-18, 0, 0)}, "cloak": {"rotation": hold(-30, 0, 0)},
         "arm_r": {"rotation": T((0, (-70, 0, 0)), (0.2, (-10, 0, 0)), (0.4, (-70, 0, 0)), (0.8, (-70, 0, 0)))},
         "arm_l": {"rotation": T((0, (-10, 0, 0)), (0.4, (-70, 0, 0)), (0.6, (-10, 0, 0)), (0.8, (-10, 0, 0)))},
         "forearm_r": {"rotation": hold(-20, 0, 0)}, "forearm_l": {"rotation": hold(-20, 0, 0)}},
        H.blink(0.3)))


def main():
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="croak", head_center=(0, 20.5), body_center=(0, 14.0), scale=15.0,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("walk", 0.25, -35, 12), ("puff", 1.0, -28, 8), ("dig", 0.2, -35, 12), ("sit", 1.0, -30, 10)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("puff", 1.0, 0, 0), ("tongue", 0.3, 0, 0), ("idle", 0, -30, 8)])


if __name__ == "__main__":
    main()
