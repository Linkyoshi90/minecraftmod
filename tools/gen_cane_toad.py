#!/usr/bin/env python3
"""Cane Toad - species definition (Mucus Toad family). Shared engine: anuran_lib.py on top of harpy_lib.py.

    python tools/gen_cane_toad.py                    # write geo / animation / texture
    python tools/gen_cane_toad.py --preview x.png    # also render x.png, x_face.png, x_body.png, x_texture.png

Concept (after the cane toad, a huge, toxic, greedy toad that eats anything that fits in its mouth): the glutton, about
1.55 blocks (1.9 in her hat). Dry brown warty skin, a cream belly, heavy bony brow ridges above half-lidded gold eyes,
and big milky parotoid glands that squirt venom. Her smug, hungry face never changes. She wears a wide straw hat, burlap
overalls with a patched pocket, carries a bundle of sugar cane on her back and a tin lunch pail on her belt. Signature
moves: `gulp` (swallowing something far too big) and `squirt` (spraying venom from her glands). Humanoid face.
"""
import copy
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import anuran_lib as A
from anuran_lib import rg, cube, mirror_cube, pick, noise, CLEAR, BROAD

NAME = "cane_toad"

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
    BACK=rg([(40, 28, 16), (78, 56, 30), (120, 90, 50), (160, 124, 74), (204, 168, 112)], nodither=True),
    BELLY=rg([(150, 130, 86), (196, 176, 120), (224, 208, 150), (242, 230, 180)], nodither=True),
    spots=dict(seed=121, cell=8, rmin=1.8, rmax=3.8, depth=0.30), belly_spots=True,
    warts=dict(seed=123, cell=5, density=0.55, ramp=rg([(70, 46, 22), (120, 86, 44), (170, 128, 70), (214, 172, 110)])),
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
PAL.HAIR = rg([(40, 28, 18), (72, 52, 32), (108, 80, 48), (146, 112, 70), (184, 150, 100), (220, 190, 140)])
PAL._mouth_y = SPEC.mouth_y

MILK = rg([(170, 160, 120), (210, 200, 160), (236, 230, 196), (250, 246, 224)])
PAL.MILK = MILK                                                          # gland colour (a variant swaps it)
STRAW = rg([(130, 100, 40), (180, 146, 60), (216, 186, 90), (240, 220, 130)])
RIBBON = rg([(110, 24, 20), (170, 44, 34), (214, 84, 62)])
BURLAP = rg([(100, 76, 44), (140, 110, 68), (176, 146, 96), (208, 182, 130)])
DENIM = rg([(26, 40, 70), (44, 66, 110), (70, 98, 150)])
ROPE = rg([(70, 50, 28), (112, 84, 48), (156, 122, 74)])
CANE = rg([(40, 90, 40), (80, 140, 56), (130, 184, 84), (176, 214, 120)])
TIN = rg([(80, 86, 92), (120, 128, 134), (170, 176, 182), (210, 216, 220)])


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
    pt("cane", "all", lambda f, x, y, w, h: pick(CANE, 0.45 + 0.4 * (y % 14 < 2) + (noise(x, y, 17) - 0.5) * 0.1 + (0.1 if x < 1 else 0), x, y))
    pt("blade", "all", lambda f, x, y, w, h: pick(CANE, 0.55 + 0.2 * (y < h * 0.5), x, y) if (f not in BROAD or abs(x + 0.5 - w / 2.0) <= (w / 2.0) * (1 - y / h * 0.8)) else CLEAR)
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
    # olive: the greener toad of wet ground
    "olive": variant(
        back=[(24, 32, 12), (50, 64, 22), (86, 104, 36), (124, 142, 56), (168, 184, 92)],
        belly=[(140, 138, 86), (184, 182, 118), (214, 212, 148), (238, 236, 178)],
        wart=[(40, 50, 18), (70, 86, 30), (110, 128, 48), (150, 166, 74)],
        eye=[(60, 60, 6), (110, 110, 12), (164, 160, 24), (206, 200, 50), (238, 232, 110)],
        milk=[(160, 170, 120), (204, 214, 160), (232, 240, 196), (248, 252, 224)],
        hair=[(30, 34, 16), (56, 62, 28), (88, 96, 44), (124, 134, 66), (166, 176, 100), (206, 214, 144)],
        drum=[(40, 50, 20), (70, 86, 34), (106, 124, 54), (146, 162, 82)], eye_out=(28, 34, 8)),
    # rust: a red-orange toad of dry red soils
    "rust": variant(
        back=[(60, 24, 12), (110, 46, 22), (158, 76, 34), (198, 110, 56), (230, 152, 96)],
        belly=[(170, 130, 100), (210, 170, 130), (236, 204, 160), (250, 228, 190)],
        wart=[(90, 36, 16), (140, 66, 28), (190, 100, 48), (226, 140, 84)],
        eye=[(90, 30, 6), (150, 60, 12), (206, 100, 24), (238, 140, 40), (252, 190, 90)],
        milk=[(190, 160, 120), (226, 200, 160), (244, 228, 196), (254, 246, 224)],
        hair=[(60, 20, 10), (100, 36, 16), (146, 60, 26), (188, 92, 40), (222, 134, 70), (244, 184, 116)],
        drum=[(80, 36, 18), (124, 60, 30), (170, 90, 46), (208, 124, 70)], eye_out=(50, 16, 6)),
    # grey: a cool slate-grey toad with a pale belly and silvery eyes
    "grey": variant(
        back=[(30, 32, 34), (62, 66, 70), (100, 106, 110), (140, 146, 148), (184, 188, 188)],
        belly=[(150, 150, 140), (190, 190, 178), (218, 218, 206), (240, 240, 230)],
        wart=[(40, 42, 46), (74, 78, 84), (114, 120, 126), (156, 162, 168)],
        eye=[(60, 56, 30), (110, 104, 56), (160, 152, 84), (206, 196, 116), (240, 232, 160)],
        milk=[(170, 172, 168), (208, 210, 204), (234, 236, 230), (250, 252, 248)],
        hair=[(34, 36, 40), (62, 66, 72), (96, 102, 110), (134, 140, 148), (176, 182, 188), (220, 224, 228)],
        drum=[(50, 54, 58), (84, 90, 96), (122, 128, 134), (160, 166, 170)], eye_out=(30, 28, 20)),
    # sand: a pale yellow-sand toad of dry country
    "sand": variant(
        back=[(110, 86, 40), (160, 130, 60), (204, 176, 90), (230, 208, 130), (248, 234, 174)],
        belly=[(180, 164, 120), (216, 204, 156), (238, 230, 184), (252, 248, 212)],
        wart=[(130, 100, 46), (176, 142, 70), (214, 184, 104), (240, 216, 144)],
        eye=[(60, 70, 10), (110, 126, 20), (160, 176, 36), (206, 220, 60), (238, 246, 120)],
        milk=[(220, 214, 170), (240, 236, 200), (250, 248, 224), (255, 254, 240)],
        hair=[(100, 76, 40), (146, 116, 66), (190, 160, 100), (224, 198, 136), (244, 224, 170), (254, 244, 206)],
        drum=[(120, 96, 50), (166, 138, 78), (206, 182, 116), (236, 218, 156)], eye_out=(60, 56, 16), spot=0.22),
    # black: a rare near-black toad with orange warts, orange eyes and glowing cream glands
    "black": variant(
        back=[(6, 6, 8), (18, 16, 18), (36, 32, 32), (60, 54, 52), (96, 88, 84)],
        belly=[(70, 60, 50), (104, 92, 76), (140, 126, 104), (176, 162, 136)],
        wart=[(110, 50, 14), (170, 84, 24), (220, 124, 44), (250, 170, 84)],
        eye=[(80, 20, 6), (140, 40, 10), (200, 70, 16), (240, 110, 30), (255, 170, 80)],
        milk=[(200, 180, 90), (236, 216, 130), (250, 240, 176), (255, 252, 218)],
        hair=[(70, 70, 76), (110, 110, 118), (150, 150, 158), (190, 190, 198), (224, 224, 230), (248, 248, 252)],
        drum=[(14, 12, 12), (30, 26, 26), (52, 46, 44), (82, 74, 70)], eye_out=(40, 10, 4), spot=0.14),
}


def write_variants():
    """cane_toad_<variant>.png next to the default texture; --lineup x.png also renders them side by side"""
    import variant_tools as VT
    VT.write_variants(NAME, "cane", paint, VARIANTS, build_model, animations,
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
