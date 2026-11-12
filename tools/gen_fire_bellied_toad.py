#!/usr/bin/env python3
"""Fire-Bellied Toad - species definition (Mucus Toad family). Shared engine: anuran_lib.py on top of harpy_lib.py.

    python tools/gen_fire_bellied_toad.py                    # write geo / animation / texture
    python tools/gen_fire_bellied_toad.py --preview x.png    # also render x.png, x_face.png, x_body.png, x_texture.png

Concept: a small, wary mountain-pond toad, about 1.35 blocks. Plain mossy olive back with black blotches, but her whole
front is blazing red-orange with black mottling: a warning she hides under a winter vest until threatened, then flashes
in the Unken reflex (arches her back, throws up her hands and feet to show the fire underneath). Gold eyes with heart-shaped
pupils. Outfit: a red-and-cream knit cap with ear flaps and a bobble, a striped scarf, a charcoal vest with fur edging
(open at the front so the belly shows), a wool skirt, and a little iron lantern at her belt. Auburn hair that flares like flame.
Humanoid face (the default for the whole anuran family).
"""
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import anuran_lib as A
from anuran_lib import rg, cube, mirror_cube, pick, noise, CLEAR, BROAD

NAME = "fire_bellied_toad"

SPEC = SimpleNamespace(legx=2.5, eye_x=2.5, eye_out=0.25, eye_rise=1.0, head_sink=0.75, head_fwd=0.0, chest_fwd=0.0,
                       mouth_y=0.75, throat_y=0.5, tymp_y=1.8, tymp_z=3.4,
                       hum=dict(sep=2.5, y=3.6, a=5.2, b=5.4, mw=1.5))
SIZES = {
    "head": (8.5, 7, 7.5), "eyelid": (3, 3, .125), "mouth": (2.75, 1.5, .125), "tymp": (.25, 2.25, 2.25), "throat": (4.5, 2.5, 3.5),
    "hips": (8, 3, 5.5), "mid": (6.5, 3, 5), "chest": (8, 4, 5.5), "belly_f": (6, 3, 3.5), "belly_s": (3.5, 3, 4.5),
    "arm_up": (2, 3, 2), "forearm": (1.75, 3, 1.75), "palm": (2.75, 1.25, 1.5),
    "fing_a": (.5, 1, .5), "fing_b": (.5, 1.25, .5), "pad_h": (.75, .5, .75),
    "thigh": (3.5, 3.5, 3.5), "shin": (2.5, 3, 2.5), "heel": (3.5, 1, 2.5),
    "toe_a": (.75, .75, 2), "toe_b": (.75, .75, 3), "toe_c": (.75, .75, 3.25), "toe_d": (.75, .75, 2.5), "pad_t": (1, .75, 1), "web_f": (3.25, .25, 2.5),
}


def declare_parts():
    A.declare_base(SIZES)
    for args in [
        ("cap", 9.5, 2.25, 8.5), ("flap", 1, 3.5, 2), ("bobble", 2, 2, 2), ("tassel", .5, 1.5, .5),
        ("vest_f", 2.5, 4, .5), ("vest_b", 8.5, 4, .5), ("vest_s", .5, 4, 5.5), ("fur_f", .75, 4, .75),
        ("scarf", 8.5, 1.5, 6), ("scarf_tail", 1.75, 5, .5), ("scarf_tail2", 1.75, 3.5, .5),
        ("belt", 8.5, 1, 6), ("buckle", 1.5, 1, .5), ("panel_f", 3.5, 3.5, .5), ("panel_s", .5, 3.5, 3.5),
        ("lantern", 2.25, 3, 2.25), ("lantern_cap", 2.75, .75, 2.75), ("chain", .5, 2.5, .5), ("wart", 1, .75, 1),
    ]:
        H.part(*args)


PAL = A.new_pal(
    BACK=rg([(10, 16, 10), (30, 50, 26), (66, 96, 40), (110, 140, 62), (160, 184, 100)], nodither=True),
    BELLY=rg([(40, 6, 4), (150, 22, 12), (226, 64, 22), (250, 120, 40)], nodither=True),
    spots=dict(seed=81, cell=8, rmin=2.0, rmax=4.2, depth=0.52), belly_spots=True,
    warts=dict(seed=83, cell=5, density=0.35, ramp=rg([(20, 34, 18), (50, 74, 34), (96, 124, 56), (140, 164, 84)])),
    side_split=0.50, gloss=1, lid=0.14,
    EYE=rg([(40, 30, 6), (90, 70, 12), (150, 120, 20), (210, 176, 40), (240, 220, 100)]),
)
PAL.EYE_OUT, PAL.PUPIL = H.rgb(20, 18, 6), H.rgb(8, 4, 4)
PAL.hpupil_bitmap = [".##.##.", "#######", "#######", ".#####.", "..###..", "...#..."]      # heart-shaped pupils
PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE, PAL.NOSTRIL = H.rgb(30, 22, 12), H.rgb(150, 30, 24), H.rgb(232, 100, 90), H.rgb(24, 24, 10)
PAL.TONGUE_R = rg([(150, 40, 40), (200, 66, 60), (230, 100, 90), (248, 150, 140)])
PAL.BLUSH, PAL.BLUSH_L = H.rgb(222, 110, 70), H.rgb(240, 150, 100)
PAL.DRUM = rg([(24, 36, 20), (60, 84, 36), (104, 130, 60), (146, 170, 90)])
PAL.SAC = rg([(150, 24, 14), (214, 56, 22), (246, 106, 40), (255, 160, 70)])
PAL.PAD = rg([(150, 24, 14), (214, 60, 24), (240, 110, 44), (252, 170, 80)])
PAL.HAIR = rg([(60, 14, 8), (110, 26, 12), (160, 44, 16), (206, 76, 24), (236, 120, 44), (250, 176, 90)])
PAL._mouth_y = SPEC.mouth_y

KNIT_R = rg([(100, 16, 16), (160, 30, 26), (210, 56, 36), (244, 96, 60)])
CREAM = rg([(180, 166, 140), (222, 210, 182), (246, 238, 212)])
CHAR = rg([(24, 22, 26), (44, 40, 46), (70, 64, 72), (100, 92, 100)])
FUR = rg([(150, 138, 120), (196, 186, 164), (232, 224, 204), (252, 248, 236)])
LEATHER = rg([(30, 22, 18), (56, 42, 34), (88, 68, 52), (124, 98, 76)])
IRON = rg([(18, 18, 22), (40, 40, 46), (72, 72, 80)])
GLOW = rg([(196, 90, 16), (250, 150, 36), (255, 214, 96), (255, 246, 190)])


def paint():
    pt = A.paint_base(PAL, curve=0.0, fringe=4, hum=SPEC.hum)

    def knit(f, x, y, w, h):
        if f in ("up",):
            c = 0.55 + 0.25 * ((x + y) % 8 == 0 or (x - y) % 8 == 0)
            return pick(KNIT_R, c, x, y) if (x + y) % 8 else CREAM[1]
        if f == "down":
            return KNIT_R[0]
        if y >= h - 5:
            band = (x // 2 + (0 if y < h - 3 else 1)) % 2
            return pick(CREAM, 0.6, x, y) if band else pick(KNIT_R, 0.45, x, y)
        pat = ((x % 8 == 3 or x % 8 == 4) and y % 6 in (2, 3)) or ((x % 8 in (1, 2, 5, 6)) and y % 6 in (0, 1) and y > 1 and False)
        if pat:
            return pick(CREAM, 0.7, x, y)
        return pick(KNIT_R, 0.52 + (noise(x, y, 5) - 0.5) * 0.10 + 0.08 * (y % 2 == 0), x, y)
    pt("cap", "all", knit)
    pt("flap", "all", lambda f, x, y, w, h: pick(CREAM, 0.6, x, y) if y > h - 3 and (x // 2) % 2 else pick(KNIT_R, 0.5 + 0.1 * (y % 2), x, y))
    pt("bobble", "all", lambda f, x, y, w, h: pick(CREAM, 0.55 + (noise(x, y, 7) - 0.5) * 0.4, x, y))
    pt("tassel", "all", lambda f, x, y, w, h: pick(KNIT_R, 0.6, x, y))

    def vest(f, x, y, w, h):
        if f in ("up", "down"):
            return CHAR[1]
        if y >= h - 1 or (f in ("north", "south") and (x < 1 or x > w - 2)):
            return pick(rg([(200, 90, 30), (240, 140, 50)]), 0.4 + 0.5 * ((x + y) % 4 < 2), x, y)       # orange stitching
        return pick(CHAR, 0.50 + (noise(x, y, 9) - 0.5) * 0.14 + 0.1 * (x % 6 == 0), x, y)
    for p in ("vest_f", "vest_b", "vest_s"):
        pt(p, "all", vest)
    pt("fur_f", "all", lambda f, x, y, w, h: CLEAR if (y < 2 and noise(x, y, 11) > 0.5) else pick(FUR, 0.62 + (noise(x, y, 13) - 0.5) * 0.4, x, y))

    def scarf(f, x, y, w, h):
        k = (x // 3) % 3
        return pick(CREAM, 0.6, x, y) if k == 0 else (pick(KNIT_R, 0.5 + 0.1 * (y % 2), x, y) if k == 1 else pick(CHAR, 0.4, x, y))
    pt("scarf", "all", scarf)

    def tail(f, x, y, w, h):
        if y >= h - 3:
            return FUR[1] if (x % 3 == 0 and f in BROAD) else (CLEAR if (f in BROAD and noise(x, y, 15) > 0.55 and y == h - 1) else pick(KNIT_R, 0.5, x, y))
        k = ((y // 3)) % 3
        return pick(CREAM, 0.6, x, y) if k == 0 else (pick(KNIT_R, 0.5 + 0.1 * (x % 2), x, y) if k == 1 else pick(CHAR, 0.4, x, y))
    pt("scarf_tail", "all", tail)
    pt("scarf_tail2", "all", tail)
    pt("belt", "all", lambda f, x, y, w, h: LEATHER[0] if (y < 1 or y >= h - 1) else pick(LEATHER, 0.55 + (noise(x, y, 17) - 0.5) * 0.12 + 0.1 * (x % 6 == 0), x, y))
    pt("buckle", "all", lambda f, x, y, w, h: pick(GLOW, 0.8 if 2 <= x < w - 2 and 1 <= y < h - 1 else 0.2, x, y))

    def skirt(f, x, y, w, h):
        if f in ("up", "down"):
            return CHAR[1]
        if y >= h - 3:
            zig = abs((x % 6) - 3)
            return pick(KNIT_R, 0.6, x, y) if y - (h - 3) >= zig // 2 else pick(CHAR, 0.5, x, y)
        return pick(CHAR, 0.50 + (noise(x, y, 19) - 0.5) * 0.12 + 0.12 * (x % 5 == 0), x, y)
    pt("panel_f", "all", skirt)
    pt("panel_s", "all", skirt)

    def lantern(f, x, y, w, h):
        if f in ("up", "down"):
            return IRON[1]
        if x < 2 or x > w - 3 or y < 2 or y > h - 3:
            return pick(IRON, 0.6, x, y)                                 # iron frame
        r = abs(x - w / 2.0) / (w / 2.0)
        return pick(GLOW, 0.95 - 0.5 * r - 0.2 * (y / h), x, y)           # the ember glow
    pt("lantern", "all", lantern)
    pt("lantern_cap", "all", lambda f, x, y, w, h: pick(IRON, 0.5 + 0.3 * (f == "up"), x, y))
    pt("chain", "all", lambda f, x, y, w, h: pick(IRON, 0.7 if y % 3 else 0.2, x, y))
    pt("wart", "all", lambda f, x, y, w, h: pick(PAL.warts["ramp"], 0.9 if (f == "up" or (x < 2 and y < 2)) else 0.4, x, y))
    return pt.T


def extras(r, g):
    HG = H.hang
    top, z0 = g.head_top, g.head_z0
    hw = A.S("head")[0]

    # knit cap with ear flaps, bobble and tassels
    r.put("head", cube("cap", [-4.75, top + 0.45, z0 - 0.5]), cube("bobble", [-1.0, top + 2.7, z0 + 3.0]))
    r.add("flap_r", "head", [-4.6, top - 0.3, 0.5], [HG("flap", -5.4, top - 0.2, -0.2, -6, 0), HG("tassel", -5.15, top - 3.7, 0.3, -8, 0)], mirror=True)

    # flame hair: a short fringe, side locks, and back hair that flares out like fire
    r.put("head", HG("bang_a", -0.75, top + 0.3, z0 - 0.3, 0, -4))
    r.add("hair_side_r", "head", [-4.8, top - 1.5, 0], [HG("strand_m", -5.5, top - 1.4, -0.9, -5, 0), HG("strand_s", -5.5, top - 1.4, 1.5, -8, 3)], mirror=True)
    names = ("strand_s", "strand_m", "strand_l", "strand_m", "strand_s")
    back = [HG(n, x, top - 1.6, 4.0 + 0.2 * (i % 2), x * 3.2, 10 + 4 * (i % 3)) for i, (n, x) in enumerate(zip(names, (-3.6, -1.8, 0.0, 1.8, 3.6)))]
    r.add("hair_back", "head", [0, top - 1.6, 4.0], back)

    # striped scarf with two tails, charcoal vest open at the front, fur edging
    r.put("body", cube("scarf", [-4.25, g.chest_top - 1.1, -3.0]))
    r.add("scarf_tails", "body", [-3.0, g.chest_top - 0.7, g.chest_front - 0.6], [HG("scarf_tail", -3.9, g.chest_top - 0.7, g.chest_front - 0.85, 0, -3), HG("scarf_tail2", -2.3, g.chest_top - 0.7, g.chest_front - 0.65, 0, -2)])
    vy = g.chest_y0 - 0.1
    for sx in (-1, 1):
        x0 = -4.4 if sx < 0 else 1.9
        r.put("body", cube("vest_f", [x0, vy, g.chest_front - 0.5]), cube("fur_f", [(-2.65 if sx < 0 else 1.9), vy, g.chest_front - 0.75]))
    r.put("body", cube("vest_b", [-4.25, vy, 2.75]), cube("vest_s", [-4.5, vy, g.chest_front + 0.0]), cube("vest_s", [4.0, vy, g.chest_front + 0.0]))

    # belt, skirt panels, lantern
    r.put("body", cube("belt", [-4.25, g.hips_y0 + 1.9, -3.0]), cube("buckle", [-0.75, g.hips_y0 + 1.9, -3.5]))
    sk = [HG("panel_f", -3.5, g.hips_y0 + 1.9, -3.2, 3, -6), HG("panel_f", 0.0, g.hips_y0 + 1.9, -3.2, -3, -6),
          HG("panel_f", -3.5, g.hips_y0 + 1.9, 2.7, 3, 6), HG("panel_f", 0.0, g.hips_y0 + 1.9, 2.7, -3, 6),
          HG("panel_s", -4.7, g.hips_y0 + 1.9, -1.75, -6, 0), HG("panel_s", 4.2, g.hips_y0 + 1.9, -1.75, 6, 0)]
    r.add("skirt", "body", [0, g.hips_y0 + 1.9, 0], sk)
    lx, ly = -5.0, g.hips_y0 + 1.9
    r.add("lantern", "body", [lx, ly, 0.3], [HG("chain", lx - 0.25, ly, 0.05), cube("lantern_cap", [lx - 1.375, ly - 2.5, -0.85]),
                                              HG("lantern", lx - 1.125, ly - 3.2, -0.82), cube("lantern_cap", [lx - 1.375, ly - 6.2, -0.85])])

    # warts on the back and legs
    r.put("body", cube("wart", [-2.5, g.hips_y0 + 0.9, 2.5]), cube("wart", [1.0, g.mid_y0 + 1.2, 2.6]))
    r.put("leg_r", cube("wart", [-SPEC.legx - 2.25, g.knee_y + 1.5, 0.2]), cube("wart", [-SPEC.legx - 2.25, g.knee_y + 2.8, -1.0]))
    r.put("arm_r", cube("wart", [-g.shoulder_x - 1.5, g.shoulder_y - 2.0, 0.3]))


def build_model():
    m, g = A.build(SPEC, extras, NAME)
    build_model.g = g
    return m


TUNE = SimpleNamespace(crouch=9, step=13, hop_lift=3.5, throat_out=(0, -1.5, -2.2), puff_f=1.3, croak_crouch=20, sit_crouch=56,
                       tongue_len=5.5, arm_rest=(3, 0, 5), extra={})


def animations(an):
    g = build_model.g
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    OUT, REST = H.OUT_, H.REST
    sway = lambda amp, per=4.0: T((0, (0, 0, 0)), (per / 4, (amp, 0, 0)), (per / 2, (0, 0, 0)), (3 * per / 4, (-amp, 0, 0)), (per, (0, 0, 0)))
    swing = lambda amp, per=4.0: T((0, (0, 0, -amp)), (per / 2, (0, 0, amp)), (per, (0, 0, -amp)))
    TUNE.extra = {
        "idle": {"skirt": {"rotation": sway(2)}, "lantern": {"rotation": swing(5, 4.0)}, "scarf_tails": {"rotation": sway(4, 3.0)}},
        "walk": {"skirt": {"rotation": T((0, (0, 0, 4)), (0.5, (0, 0, -4)), (1, (0, 0, 4)))}, "lantern": {"rotation": swing(14, 1.0)}, "scarf_tails": {"rotation": T((0, (6, 0, 0)), (0.5, (14, 0, 0)), (1, (6, 0, 0)))}},
        "hop": {"skirt": {"rotation": T((0, (0, 0, 0)), (0.3, (-22, 0, 0)), (0.74, (12, 0, 0)), (0.9, (0, 0, 0)))}, "lantern": {"rotation": swing(18, 0.9)},
                "scarf_tails": {"rotation": T((0, (0, 0, 0)), (0.3, (-40, 0, 0)), (0.74, (20, 0, 0)), (0.9, (0, 0, 0)))}},
        "swim": {"skirt": {"rotation": hold(-30, 0, 0)}, "scarf_tails": {"rotation": hold(-60, 0, 0)}, "lantern": {"rotation": hold(-40, 0, 0)}},
        "croak": {"scarf_tails": {"rotation": sway(5, 3.0)}},
        "sit": {"skirt": {"rotation": hold(6, 0, 0)}, "lantern": {"rotation": swing(3, 4.0)}},
    }
    A.common_anims(an, g, TUNE)

    # unken: the warning reflex - arches back, throws hands and feet up and shows the fire underneath
    lg, by = A.gait(g.T, g.Sh, 2.4, [(0, (-72, 112, 0, 0)), (2.4, (-72, 112, 0, 0))], [(0, (-72, 112, 0, 0)), (2.4, (-72, 112, 0, 0))])
    shake = T((0, (-34, 0, 3)), (0.3, (-34, 0, -3)), (0.6, (-34, 0, 3)), (2.0, (-34, 0, 3)), (2.4, (-34, 0, 3)))
    for s_ in ("r", "l"):
        lg["leg_" + s_]["position"] = {k: [0, v[1] + 1.2, 0] for k, v in by.items()}
    lg["body"] = {"position": {k: [0, v[1] + 1.0, 0] for k, v in by.items()}, "rotation": T((0, (0, 0, 0)), (0.3, (-34, 0, 2)), (0.5, (-34, 0, -2)), (0.7, (-34, 0, 2)), (2.0, (-34, 0, 0)), (2.4, (0, 0, 0)))}
    an.add("unken", 2.4, True, merge(
        lg,
        {"head": {"rotation": T((0, (0, 0, 0)), (0.3, (26, 0, 0)), (2.0, (26, 0, 0)), (2.4, (0, 0, 0)))},
         "throat": {"position": T((0, REST), (0.3, (0, -0.9, -1.1)), (2.0, (0, -0.9, -1.1)), (2.4, REST))},
         "belly": {"position": T((0, REST), (0.3, (0, 0, -1.2)), (2.0, (0, 0, -1.2)), (2.4, REST))},
         "eyelids": {"position": T((0, REST), (0.3, OUT), (0.4, REST), (2.4, REST))},
         "skirt": {"rotation": T((0, (0, 0, 0)), (0.3, (-40, 0, 0)), (2.0, (-40, 0, 0)), (2.4, (0, 0, 0)))},
         "lantern": {"rotation": swing(26, 0.6)}, "scarf_tails": {"rotation": T((0, (0, 0, 0)), (0.3, (-50, 0, 0)), (2.0, (-50, 0, 0)), (2.4, (0, 0, 0)))},
         "hair_back": {"rotation": T((0, (0, 0, 0)), (0.3, (-16, 0, 0)), (2.4, (0, 0, 0)))}},
        sym("arm", T((0, (3, 0, 5)), (0.3, (-168, 0, 26)), (2.0, (-168, 0, 26)), (2.4, (3, 0, 5)))), sym("forearm", hold(-20, 0, 0)), sym("hand", hold(-30, 0, 0))))

    # trill: a long wavering call, throat pulsing fast
    pulses = [(0, REST)]
    for i in range(8):
        t0 = 0.2 + i * 0.28
        pulses += [(t0, REST), (t0 + 0.1, (0, -1.5, -2.2)), (t0 + 0.2, REST)]
    pulses.append((3.0, REST))
    lg2, by2 = A.gait(g.T, g.Sh, 3.0, [(0, (-20, 40, 0, 0)), (3.0, (-20, 40, 0, 0))], [(0, (-20, 40, 0, 0)), (3.0, (-20, 40, 0, 0))])
    for s_ in ("r", "l"):
        lg2["leg_" + s_]["position"] = by2
    lg2["body"] = {"position": by2}
    an.add("trill", 3.0, True, merge(
        lg2,
        {"throat": {"position": T(*pulses)}, "head": {"rotation": T((0, (-8, 0, 0)), (1.5, (-12, 0, 0)), (3, (-8, 0, 0)))},
         "eyelids": {"position": hold(*OUT)}, "lantern": {"rotation": swing(4, 3.0)}, "scarf_tails": {"rotation": sway(4, 3.0)}},
        sym("arm", hold(3, 0, 5)), sym("forearm", hold(-8, 0, 0))))


def write_glowmask(tex):
    """the lantern glass and the ember buckle glow in the dark (GeckoLib's AutoGlowingGeoLayer reads <texture>_glowmask.png)"""
    keep = set(GLOW)
    out = [[CLEAR] * len(tex) for _ in tex]
    for part in ("lantern", "buckle"):
        for f in H.FACES:
            if f in H.PARTS[part]["skip"]:
                continue
            x0, y0, w, h = H.RECT[(part, f)]
            for y in range(y0, y0 + h):
                for x in range(x0, x0 + w):
                    if tex[y][x] in keep:
                        out[y][x] = tex[y][x]
    path = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide", "textures", "entity", NAME + "_glowmask.png")
    H.write_png(path, out)
    print("glow mask ->", os.path.basename(path))


def main():
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="croak", head_center=(0, 18.5), body_center=(0, 11.5), scale=17.0,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("unken", 0.8, -28, 8), ("walk", 0.25, -35, 12), ("hop", 0.4, -30, 10), ("trill", 0.4, -30, 8)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("unken", 0.8, 0, 0), ("tongue", 0.3, 0, 0), ("idle", 0, -30, 8)])
    write_glowmask(paint())


if __name__ == "__main__":
    main()
