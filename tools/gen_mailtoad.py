#!/usr/bin/env python3
"""Mailtoad - species definition. The shared engine lives in anuran_lib.py.

    python tools/gen_mailtoad.py   (see the generator this one is copied from for the options)

Field guide (eleventh wave): a Harmontide toad postwoman in a blue cap and uniform, a leather mail satchel at her hip.
Hand her an item, then a paper renamed (in an anvil) to a player's name, and she carries it to them. The cane toad's
rig and animations (gen_cane_toad.py) in blue: the straw hat is her cap, the overalls her uniform, the pail her satchel,
and the sugar cane bundle is left off. Variants: post (default), teal, green, grey, gold; keep in sync with
AnuranVariants.MAILTOAD.
"""
import copy
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import anuran_lib as A
from anuran_lib import rg, cube, mirror_cube, pick, noise, CLEAR, BROAD

NAME = "mailtoad"

SPEC = SimpleNamespace(legx=2.75, eye_x=3.0, eye_out=0.25, eye_rise=1.0, head_sink=1.0, head_fwd=0.25, chest_fwd=0.0,
                       mouth_y=1.0, throat_y=0.5, tymp_y=2.0, tymp_z=3.8,
                       hum=dict(sep=3.0, y=3.7, a=5.6, b=5.8, mw=2.0))
SIZES = {
    "head": (10, 7.5, 8.5), "eyelid": (3.25, 3.25, .125), "mouth": (3.5, 1.75, .125), "tymp": (.25, 2.75, 2.75),
    "tongue_a": (1.5, .5, 7), "tongue_b": (2.5, .75, 1.75), "throat": (5.5, 3, 4.5),
    "hips": (10, 3, 6.5), "mid": (8, 3.5, 5.5), "chest": (11, 5, 6.5), "belly_f": (7, 3.5, 3.5), "belly_s": (3.5, 3.5, 5),
    "arm_up": (3, 3.5, 3), "forearm": (2.5, 3.5, 2.5), "palm": (3, 1.5, 2),
    "fing_a": (.5, 1.25, .5), "fing_b": (.5, 1.5, .5), "pad_h": (.75, .5, .75),
    "thigh": (4.5, 4.5, 4.5), "shin": (3, 3.5, 3), "heel": (4, 1, 3),
    "toe_a": (.75, .75, 2.5), "toe_b": (.75, .75, 3.5), "toe_c": (.75, .75, 3.75), "toe_d": (.75, .75, 3), "pad_t": (1, .75, 1), "web_f": (3.5, .25, 3),
}


def declare_parts():
    A.declare_base(SIZES)
    for args in [
        ("gland", 4, 3, 4.5), ("ridge", 4.25, 1, 2.5), ("drop", 1, 1, 1), ("wart", 1, .75, 1),
        ("brim", 13, .75, 12), ("crown", 7.5, 3, 7), ("band", 8, .75, 7.5),
        ("shorts", 10.5, 3.5, 7), ("bib", 8, 5, .5), ("pocket", 2.5, 2, .5), ("o_strap", .75, 5, .5),
        ("cane", 1, 14, 1), ("blade", .25, 7, 2), ("pail", 2.5, 2.5, 2.5), ("pail_lid", 2.75, .5, 2.75), ("handle", .25, 2.5, .25),
    ]:
        H.part(*args)


PAL = A.new_pal(
    BACK=rg([(16, 34, 80), (30, 60, 130), (50, 96, 180), (90, 140, 216), (150, 190, 240)], nodither=True),
    BELLY=rg([(150, 170, 200), (190, 206, 228), (220, 230, 244), (240, 246, 252)], nodither=True),
    spots=dict(seed=121, cell=8, rmin=1.8, rmax=3.8, depth=0.30), belly_spots=True,
    warts=dict(seed=123, cell=5, density=0.35, ramp=rg([(20, 50, 110), (40, 80, 160), (80, 130, 210), (140, 184, 240)])),
    side_split=0.50, gloss=0, lid=0.28,
    EYE=rg([(70, 46, 6), (130, 90, 12), (190, 140, 24), (228, 184, 50), (250, 224, 110)]),
)
PAL.EYE_OUT, PAL.PUPIL, PAL.hpupil = H.rgb(40, 28, 8), H.rgb(10, 8, 6), (0.6, 0.5)
PAL.LIP_D, PAL.MOUTH_IN, PAL.TONGUE, PAL.NOSTRIL = H.rgb(50, 34, 18), H.rgb(130, 46, 44), H.rgb(220, 104, 104), H.rgb(40, 28, 16)
PAL.TONGUE_R = rg([(150, 56, 56), (196, 84, 80), (228, 116, 110), (246, 156, 150)])
PAL.BLUSH, PAL.BLUSH_L = None, None
PAL.DRUM = rg([(70, 48, 24), (112, 82, 44), (156, 122, 72), (196, 164, 108)])
PAL.SAC = rg([(150, 130, 86), (196, 176, 120), (224, 208, 150), (242, 230, 180)])
PAL.PAD = rg([(120, 96, 56), (170, 140, 86), (206, 180, 120), (232, 210, 154)])
PAL.HAIR = rg([(14, 20, 40), (26, 36, 70), (40, 56, 104), (62, 82, 140), (96, 118, 176), (140, 160, 210)])
PAL.DRUM = rg([(20, 44, 96), (36, 70, 140), (60, 104, 184), (100, 146, 220)])
PAL.SAC = rg([(150, 170, 200), (190, 206, 228), (220, 230, 244), (240, 246, 252)])
PAL.PAD = rg([(110, 130, 170), (150, 170, 206), (190, 206, 232), (220, 230, 246)])
PAL._mouth_y = SPEC.mouth_y

MILK = rg([(170, 160, 120), (210, 200, 160), (236, 230, 196), (250, 246, 224)])
PAL.MILK = MILK                                                          # gland colour (a variant swaps it)
STRAW = rg([(16, 24, 50), (28, 40, 80), (44, 60, 112), (64, 84, 144)])          # her postal cap (the hat's geometry)
RIBBON = rg([(150, 112, 30), (210, 166, 50), (244, 210, 110)])                    # its gold band
BURLAP = rg([(20, 30, 60), (34, 50, 96), (52, 74, 130), (76, 100, 164)])         # the uniform (the overalls)
DENIM = rg([(150, 112, 30), (210, 166, 50), (244, 210, 110)])                     # brass buttons and pocket
ROPE = rg([(70, 50, 28), (112, 84, 48), (156, 122, 74)])
CANE = rg([(40, 90, 40), (80, 140, 56), (130, 184, 84), (176, 214, 120)])
TIN = rg([(70, 44, 24), (110, 72, 40), (150, 104, 62), (190, 142, 92)])          # the leather mail satchel (the pail)


def _paint():
    pt = A.paint_base(PAL, curve=-0.8, fringe=3, hum=SPEC.hum)

    def gland(f, x, y, w, h):
        if noise(x // 2, y // 2, 5) > 0.82:
            return pick(PAL.MILK, 0.15, x, y)                                      # dark pores
        return pick(PAL.MILK, 0.60 + (noise(x, y, 7) - 0.5) * 0.14 + (0.2 if (x, y) == (2, 2) else 0), x, y)
    pt("gland", "all", gland)
    pt("ridge", "all", A.skin_fn(PAL, "limb", base=0.40))
    pt("drop", "all", lambda f, x, y, w, h: pick(PAL.MILK, 0.85 - 0.3 * (x > w - 2 or y > h - 2), x, y))
    pt("wart", "all", lambda f, x, y, w, h: pick(PAL.warts["ramp"], 0.9 if (f == "up" or (x < 2 and y < 2)) else 0.4, x, y))

    def weave(f, x, y, w, h):
        if f in ("up",):
            return pick(STRAW, 0.55 + 0.2 * (((x // 2) + (y // 2)) % 2) + (noise(x, y, 9) - 0.5) * 0.1, x, y)
        if f == "down":
            return STRAW[0]
        return pick(STRAW, 0.50 + 0.2 * (((x // 2) + (y // 2)) % 2) + (noise(x, y, 11) - 0.5) * 0.1, x, y)
    pt("brim", "all", weave)
    pt("crown", "all", weave)
    pt("band", "all", lambda f, x, y, w, h: pick(RIBBON, 0.55 + 0.2 * (y % 2 == 0), x, y))

    def burlap(f, x, y, w, h):
        if f in ("up", "down"):
            return BURLAP[1]
        weave_ = 0.12 * (((x + y) % 2) == 0)
        if f in BROAD and 3 <= x <= w - 4 and 3 <= y <= h - 4 and noise(x // 5, y // 5, 13) > 0.78:
            return pick(DENIM, 0.55 + weave_, x, y)                           # a denim patch
        return pick(BURLAP, 0.45 + weave_ + (noise(x, y, 15) - 0.5) * 0.1, x, y)
    pt("shorts", "all", burlap)
    pt("bib", "all", burlap)
    pt("pocket", "all", lambda f, x, y, w, h: pick(DENIM, 0.5 + 0.2 * (y == 1 or x == 0 or x == w - 1), x, y))
    pt("o_strap", "all", lambda f, x, y, w, h: pick(BURLAP, 0.3 + 0.2 * (y % 4 == 0), x, y))
    pt("cane", "all", lambda f, x, y, w, h: CLEAR)                                 # no cane bundle
    pt("blade", "all", lambda f, x, y, w, h: CLEAR)
    pt("pail", "all", lambda f, x, y, w, h: pick(TIN, 0.55 + 0.25 * (x < 3) - 0.2 * (y % 8 == 0) + (noise(x, y, 19) - 0.5) * 0.1, x, y))
    pt("pail_lid", "all", lambda f, x, y, w, h: pick(TIN, 0.65, x, y))
    pt("handle", "all", lambda f, x, y, w, h: pick(TIN, 0.3, x, y))
    return pt.T


def paint(v=None):
    """v = a variant palette (see VARIANTS); None = the default colours"""
    global PAL
    base = PAL
    PAL = v or base
    try:
        return _paint()
    finally:
        PAL = base


# ----------------------------------------------------------------------------- colour variants (texture only)
# A variant swaps the skin, wart, eye, hair and gland colours (hat, overalls and cane stay the same).
def variant(back, belly, wart, eye, milk, hair, drum, eye_out, spot=0.30, pupil=None):
    p = copy.copy(PAL)
    p.BACK, p.BELLY = rg(back, nodither=True), rg(belly, nodither=True)
    p.warts = dict(PAL.warts, ramp=rg(wart))
    p.EYE, p.MILK, p.HAIR, p.DRUM = rg(eye), rg(milk), rg(hair), rg(drum)
    p.SAC, p.PAD = rg(belly), rg(belly)
    p.EYE_OUT = H.rgb(*eye_out)
    p.spots = dict(PAL.spots, depth=spot)
    if pupil:
        p.PUPIL = H.rgb(*pupil)
    return p


VARIANTS = {
    "teal": variant(back=[(10, 50, 60), (20, 90, 100), (40, 140, 150), (90, 190, 196), (160, 230, 230)],
                    belly=[(150, 190, 190), (190, 220, 218), (220, 240, 236), (240, 250, 248)],
                    wart=[(16, 70, 80), (30, 110, 120), (70, 160, 168), (130, 210, 214)],
                    eye=[(70, 46, 6), (130, 90, 12), (190, 140, 24), (228, 184, 50), (250, 224, 110)],
                    milk=[(170, 200, 200), (210, 230, 230), (236, 246, 246), (250, 254, 254)],
                    hair=[(10, 30, 36), (20, 50, 60), (34, 76, 90), (56, 110, 126), (90, 150, 166), (140, 196, 206)],
                    drum=[(16, 70, 80), (30, 110, 120), (60, 150, 160), (110, 196, 204)], eye_out=(40, 28, 8)),
    "green": variant(back=[(20, 50, 20), (40, 90, 36), (70, 140, 56), (120, 186, 96), (180, 226, 150)],
                     belly=[(170, 190, 140), (206, 222, 176), (230, 240, 206), (246, 250, 230)],
                     wart=[(30, 70, 26), (54, 110, 44), (96, 160, 76), (150, 206, 124)],
                     eye=[(70, 46, 6), (130, 90, 12), (190, 140, 24), (228, 184, 50), (250, 224, 110)],
                     milk=[(190, 200, 160), (220, 230, 196), (240, 246, 224), (252, 254, 242)],
                     hair=[(16, 30, 14), (30, 52, 26), (48, 80, 40), (74, 114, 60), (110, 154, 90), (156, 200, 132)],
                     drum=[(30, 70, 26), (54, 110, 44), (90, 154, 70), (140, 200, 116)], eye_out=(40, 28, 8)),
    "grey": variant(back=[(30, 32, 36), (60, 64, 70), (98, 104, 110), (140, 146, 150), (186, 190, 192)],
                    belly=[(160, 162, 160), (196, 198, 196), (224, 226, 224), (244, 246, 244)],
                    wart=[(40, 44, 50), (74, 80, 86), (114, 120, 128), (158, 164, 170)],
                    eye=[(60, 56, 30), (110, 104, 56), (160, 152, 84), (206, 196, 116), (240, 232, 160)],
                    milk=[(180, 182, 178), (214, 216, 210), (238, 240, 234), (252, 254, 250)],
                    hair=[(34, 36, 40), (62, 66, 72), (96, 102, 110), (134, 140, 148), (176, 182, 188), (220, 224, 228)],
                    drum=[(50, 54, 58), (84, 90, 96), (122, 128, 134), (160, 166, 170)], eye_out=(30, 28, 20)),
    "gold": variant(back=[(90, 60, 10), (150, 104, 20), (206, 156, 40), (236, 200, 90), (252, 236, 160)],
                    belly=[(200, 184, 140), (230, 216, 176), (246, 236, 206), (254, 250, 232)],
                    wart=[(130, 90, 16), (184, 134, 30), (226, 180, 64), (248, 220, 120)],
                    eye=[(40, 70, 120), (60, 110, 170), (90, 150, 210), (140, 196, 240), (200, 230, 252)],
                    milk=[(230, 220, 180), (246, 238, 206), (252, 248, 228), (255, 254, 244)],
                    hair=[(80, 50, 10), (126, 84, 18), (176, 124, 34), (214, 166, 64), (240, 206, 110), (252, 236, 170)],
                    drum=[(130, 90, 16), (184, 134, 30), (224, 178, 60), (246, 216, 110)], eye_out=(20, 40, 70), spot=0.2),
}


def write_variants():
    """cane_toad_<variant>.png next to the default texture; --lineup x.png also renders them side by side"""
    import variant_tools as VT
    VT.write_variants(NAME, "post", paint, VARIANTS, build_model, animations,
                      rows=[("idle", 0, -20, 6, 12.0, None), ("gulp", 0.35, -25, 8, 12.0, None), ("idle", 0, -15, 0, 24, (0, 19.0))])


def extras(r, g):
    HG = H.hang
    top, z0 = g.head_top, g.head_z0
    hw = A.S("head")[0]

    # bony brow ridges over the eyes and big milky glands behind them (the glands are bones so they can swell and squirt)
    ridge = cube("ridge", [-3.0 - 2.125, top - 1.7, z0 - 0.75])
    r.put("head", ridge, mirror_cube(ridge))
    r.add("gland_r", "head", [-hw / 2 + 1.5, top - 1.0, z0 + 5.5], [cube("gland", [-hw / 2 - 0.5, top - 2.6, z0 + 3.6])], mirror=True)
    for i, dx in enumerate((-0.8, 0.0, 0.8)):
        r.add("drop_" + "abc"[i], "head", [0, g.head_y0 + 3.5, z0 + 3.5], [cube("drop", [dx - 0.5, g.head_y0 + 3.3, z0 + 3.2])])

    # short scraggly hair under the hat
    r.add("hair_back", "head", [0, top - 1.2, 4.5], [HG(n, x, top - 1.2, 4.9 + 0.25 * (i % 2), x * 1.4, 4 + 2 * (i % 3)) for i, (n, x) in enumerate(zip(("strand_s", "strand_m", "strand_s", "strand_m", "strand_s"), (-3.6, -1.8, 0.0, 1.8, 3.6)))])

    # straw hat with a red band
    hy = top + 0.5
    zc = z0 + 4.25
    r.put("head", cube("brim", [-6.5, hy, zc - 6.0]), cube("crown", [-3.75, hy + 0.75, zc - 3.5]), cube("band", [-4.0, hy + 0.75, zc - 3.75]))

    # burlap overalls with a patched pocket
    r.put("body", cube("shorts", [-5.25, g.hips_y0 - 0.25, -3.5]), cube("bib", [-4.0, g.mid_y0 + 0.6, -3.75]), cube("pocket", [-1.25, g.mid_y0 + 1.2, -4.25]))
    for x in (-3.4, 2.65):
        r.put("body", cube("o_strap", [x, g.chest_y0 + 2.2, g.chest_front - 0.5]), cube("o_strap", [x, g.chest_y0 + 2.2, 3.3]))

    # sugar cane bundle on her back, lunch pail at her belt
    cn = []
    by = g.mid_y0 - 1.0
    for x, rz, rx in ((-1.8, 9, -7), (0.0, -2, -10), (1.8, -11, -6)):
        cn.append(cube("cane", [x - 0.5, by, 3.6], pivot=[x, by, 4.1], rotation=[rx, 0, rz]))
        cn.append(cube("blade", [x - 0.125, by + 11.0, 3.4], pivot=[x, by, 4.1], rotation=[rx - 14, 0, rz]))
        cn.append(cube("blade", [x - 0.125, by + 8.0, 3.2], pivot=[x, by, 4.1], rotation=[rx - 22, 90, rz]))
    r.add("cane", "body", [0, by, 4.1], cn)
    px, py = 5.9, g.hips_y0 + 1.0
    r.add("pail", "body", [px, g.hips_y0 + 2.8, 0], [HG("handle", px - 0.125, g.hips_y0 + 2.8, 0.0), cube("pail_lid", [px - 1.375, py + 1.9, -1.4]), cube("pail", [px - 1.25, py - 0.6, -1.25])])

    # warts on the limbs and back
    cx = -SPEC.legx
    r.put("leg_r", cube("wart", [cx - 2.75, g.knee_y + 1.2, 0.3]), cube("wart", [cx - 2.75, g.knee_y + 2.6, -1.0]))
    r.put("shin_r", cube("wart", [cx - 2.0, g.ankle_y + 1.3, 0.8]))
    r.put("arm_r", cube("wart", [-g.shoulder_x - 2.0, g.shoulder_y - 2.0, 0.2]))


def build_model():
    m, g = A.build(SPEC, extras, NAME)
    build_model.g = g
    return m


TUNE = SimpleNamespace(crouch=10, step=14, hop_lift=3.0, throat_out=(0, -2.0, -2.8), puff_f=1.6, croak_crouch=20, sit_crouch=58,
                       tongue_len=6.5, arm_rest=(3, 0, 6), extra={})


def animations(an):
    g = build_model.g
    T, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    OUT, REST = H.OUT_, H.REST
    sway = lambda amp, per=4.0: T((0, (0, 0, 0)), (per / 4, (amp, 0, 0)), (per / 2, (0, 0, 0)), (3 * per / 4, (-amp, 0, 0)), (per, (0, 0, 0)))
    swing = lambda amp, per: T((0, (0, 0, -amp)), (per / 2, (0, 0, amp)), (per, (0, 0, -amp)))
    TUNE.extra = {
        "idle": {"cane": {"rotation": sway(1.5)}, "pail": {"rotation": swing(4, 4.0)}},
        "walk": {"cane": {"rotation": T((0, (0, 0, 3)), (0.5, (0, 0, -3)), (1, (0, 0, 3)))}, "pail": {"rotation": swing(14, 1.0)}},
        "hop": {"cane": {"rotation": T((0, (0, 0, 0)), (0.3, (12, 0, 0)), (0.74, (-8, 0, 0)), (0.9, (0, 0, 0)))}, "pail": {"rotation": swing(20, 0.9)}},
        "swim": {"cane": {"rotation": hold(-60, 0, 0)}, "pail": {"rotation": hold(-40, 0, 0)}},
        "croak": {"pail": {"rotation": swing(3, 3.0)}},
        "sit": {"cane": {"rotation": hold(-6, 0, 0)}},
    }
    A.common_anims(an, g, TUNE)
    pose = lambda deg: (-deg, 2 * deg, 0, 0)

    def stand(period, deg):
        lg, by = A.gait(g.T, g.Sh, period, [(0, pose(deg)), (period, pose(deg))], [(0, pose(deg)), (period, pose(deg))])
        for s_ in ("r", "l"):
            lg["leg_" + s_]["position"] = by
        lg["body"] = {"position": by}
        return lg

    # gulp: the head snaps forward, the tongue grabs something far too big, and the whole body swallows it
    lg = stand(1.9, 12)
    lg["body"]["rotation"] = T((0, (0, 0, 0)), (0.2, (8, 0, 0)), (0.9, (-6, 0, 0)), (1.4, (0, 0, 0)), (1.9, (0, 0, 0)))
    an.add("gulp", 1.9, True, merge(
        lg,
        {"mouth": {"position": T((0, REST), (0.12, REST), (0.18, OUT), (0.75, OUT), (0.85, REST), (1.9, REST))},
         "tongue": {"position": T((0, REST), (0.18, REST), (0.26, (0, 0, -TUNE.tongue_len)), (0.4, (0, 0, -TUNE.tongue_len)), (0.55, REST), (1.9, REST))},
         "head": {"rotation": T((0, (0, 0, 0)), (0.18, (14, 0, 0)), (0.5, (14, 0, 0)), (0.9, (-18, 0, 0)), (1.4, (0, 0, 0)), (1.9, (0, 0, 0)))},
         "eyelids": {"position": T((0, REST), (0.7, REST), (0.74, OUT), (1.2, OUT), (1.26, REST), (1.9, REST))},
         "throat": {"position": T((0, REST), (0.7, REST), (0.85, (0, -1.4, -1.8)), (1.1, (0, -0.8, -1.0)), (1.35, REST), (1.9, REST))},
         "belly": {"position": T((0, REST), (0.9, REST), (1.2, (0, 0, -1.4)), (1.8, (0, 0, -1.4)), (1.9, REST))},
         "belly_r": {"position": T((0, REST), (0.9, REST), (1.2, (-1.2, 0, 0)), (1.8, (-1.2, 0, 0)), (1.9, REST))},
         "belly_l": {"position": T((0, REST), (0.9, REST), (1.2, (1.2, 0, 0)), (1.8, (1.2, 0, 0)), (1.9, REST))},
         "pail": {"rotation": swing(8, 1.9)}},
        sym("arm", hold(3, 0, 6)), sym("forearm", hold(-8, 0, 0))))

    # squirt: head thrown back, glands swell and spray milky venom in an arc
    def drops(delay, dx):
        keys = [(0, (0, 0, 0)), (delay, (0, 0, 0))]
        for k in range(3):
            t0 = delay + 0.55 + k * 0.5
            keys += [(t0, (0, 0, 0)), (t0 + 0.4, (dx, -3.0, -16.0)), (t0 + 0.41, (0, 0, 0))]
        keys.append((2.6, (0, 0, 0)))
        return T(*keys)
    lg = stand(2.6, 14)
    lg["body"]["rotation"] = T((0, (0, 0, 0)), (0.4, (-10, 0, 0)), (2.0, (-10, 0, 0)), (2.6, (0, 0, 0)))
    gl_out = T((0, (0, 0, 0)), (0.4, (-0.9, 0.5, 0)), (0.5, (-0.4, 0.2, 0)), (0.6, (-0.9, 0.5, 0)), (0.7, (-0.4, 0.2, 0)), (2.0, (-0.9, 0.5, 0)), (2.6, (0, 0, 0)))
    gl_out_l = {k: [-v[0], v[1], v[2]] for k, v in gl_out.items()}
    an.add("squirt", 2.6, True, merge(
        lg,
        {"head": {"rotation": T((0, (0, 0, 0)), (0.4, (-26, 0, 0)), (2.0, (-26, 0, 0)), (2.6, (0, 0, 0)))},
         "mouth": {"position": T((0, REST), (0.4, OUT), (2.0, OUT), (2.4, REST), (2.6, REST))},
         "gland_r": {"position": gl_out}, "gland_l": {"position": gl_out_l},
         "throat": {"position": T((0, REST), (0.4, (0, -1.0, -1.4)), (2.0, (0, -1.0, -1.4)), (2.6, REST))},
         "eyelids": {"position": T((0, REST), (0.4, REST), (0.44, OUT), (0.6, REST), (2.6, REST))},
         "drop_a": {"position": drops(0.0, 0.0)}, "drop_b": {"position": drops(0.18, 1.4)}, "drop_c": {"position": drops(0.36, -1.4)},
         "pail": {"rotation": swing(6, 2.6)}, "cane": {"rotation": T((0, (0, 0, 0)), (0.4, (-8, 0, 0)), (2.0, (-8, 0, 0)), (2.6, (0, 0, 0)))}},
        sym("arm", T((0, (3, 0, 6)), (0.4, (-10, 0, 30)), (2.0, (-10, 0, 30)), (2.6, (3, 0, 6)))), sym("forearm", hold(-24, 0, 0))))


def main():
    declare_parts()
    H.run(NAME, paint, build_model, animations, speak="gulp", head_center=(0, 21.0), body_center=(0, 14.0), scale=12.5,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("gulp", 0.35, -30, 8), ("squirt", 1.0, -28, 8), ("walk", 0.25, -35, 12), ("hop", 0.4, -30, 10)],
          face_views=[("idle", 0, 0, 0), ("idle", 2.8, 0, 0), ("gulp", 0.35, 0, 0), ("squirt", 1.0, 0, 0), ("idle", 0, -30, 8)])
    write_variants()


if __name__ == "__main__":
    main()
