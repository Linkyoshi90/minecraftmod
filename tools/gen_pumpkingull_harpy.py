#!/usr/bin/env python3
"""Pumpkingull Harpy - the seagull harpy's rig and animations (gen_seagull_harpy.py) in sky blue and white, dressed in
a pumpkin (her top and lobed skirt) with a little pumpkin cap, stem and vine leaf (bone `pumpkin_hat`).

    python tools/gen_pumpkingull_harpy.py              # write the three asset files and the variants
    python tools/gen_pumpkingull_harpy.py --preview x  # also render preview sheets

Field guide (sixteenth wave): a Harmontide gull of the autumn shores, sky blue and white in a pumpkin. While she wears her pumpkin the
endermen near her friend take no offence at being looked at. Variants (pumpkingull_harpy_<variant>.png): ghost, black,
harvest, cinder, orange. Keep in sync with PumpkingullHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_seagull_harpy as S  # noqa: E402
import variant_tools as VT  # noqa: E402
from harpy_lib import pick, noise, rgb, ramp  # noqa: E402

NAME = "pumpkingull_harpy"
S.NAME = NAME                                    # the gull's model and animations, under her name
L = S.L

ORANGE_FEA = [rgb(26, 16, 10), rgb(60, 34, 16), rgb(130, 66, 20), rgb(196, 104, 30), rgb(232, 146, 50), rgb(246, 196, 120), rgb(252, 236, 210)]
ORANGE_HAIR = [rgb(120, 46, 14), rgb(170, 70, 18), rgb(214, 102, 28), rgb(238, 140, 44), rgb(248, 178, 86), rgb(252, 214, 150)]
AMBER_EYES = [rgb(150, 70, 10), rgb(206, 110, 20), rgb(240, 160, 40), rgb(252, 204, 90), rgb(255, 236, 170)]
DARK_EYES = S.DARK_EYES
BLACK_LEGS = [rgb(14, 12, 12), rgb(30, 26, 24), rgb(50, 44, 40), rgb(74, 66, 60), rgb(104, 94, 86)]
PUMPKIN = ramp([rgb(150, 66, 10), rgb(206, 100, 20), rgb(236, 138, 34), rgb(250, 176, 70)])
STEM = ramp([rgb(50, 60, 20), rgb(80, 96, 34), rgb(118, 136, 54)])
GLOW = ramp([rgb(200, 140, 20), rgb(250, 210, 60), rgb(255, 246, 170)])
INK_TIPS = S.INK_TIPS

BLUE_HAIR = [rgb(30, 70, 150), rgb(48, 96, 186), rgb(70, 124, 214), rgb(104, 156, 232), rgb(150, 192, 244), rgb(206, 228, 252)]
BLUE_FEA = [rgb(16, 30, 70), rgb(34, 66, 140), rgb(60, 110, 200), rgb(96, 150, 228), rgb(170, 200, 240), rgb(232, 240, 250), rgb(254, 254, 255)]
BLUE_EYES = [rgb(14, 40, 100), rgb(30, 76, 160), rgb(60, 120, 210), rgb(120, 170, 240), rgb(200, 226, 255)]
ORANGE_LEGS = [rgb(150, 80, 10), rgb(206, 120, 20), rgb(236, 160, 40), rgb(248, 196, 80), rgb(252, 226, 150)]
LEAF = ramp([rgb(60, 90, 30), rgb(100, 136, 50), rgb(150, 176, 80), rgb(196, 210, 120)])
SWIRL = ramp([rgb(240, 196, 130), rgb(252, 226, 180)])

DEFAULT = (S.variant_pal(BLUE_HAIR, BLUE_FEA, BLUE_EYES, ORANGE_LEGS), S.WINGS, LEAF)
VARIANTS = {
    # orange: an orange-and-black autumn gull
    "orange": (S.variant_pal(ORANGE_HAIR, ORANGE_FEA, AMBER_EYES, BLACK_LEGS), INK_TIPS, LEAF),
    # ghost: pale grey-white
    "ghost": (S.variant_pal(S.WHITE_HAIR, S.GULL_FEA, DARK_EYES, S.PINK_LEGS), S.INK_TIPS, ramp([rgb(80, 60, 110), rgb(120, 96, 160), rgb(170, 146, 210)])),
    # black: a black-cat gull, orange eyes
    "black": (S.variant_pal([rgb(10, 10, 12), rgb(22, 22, 26), rgb(36, 36, 42), rgb(54, 54, 62), rgb(76, 76, 86), rgb(104, 104, 116)],
                            [rgb(8, 8, 10), rgb(18, 18, 22), rgb(30, 30, 36), rgb(46, 46, 54), rgb(80, 80, 90), rgb(140, 140, 150), rgb(200, 200, 210)],
                            AMBER_EYES, BLACK_LEGS), S.INK_TIPS, ramp([rgb(150, 66, 10), rgb(206, 100, 20), rgb(236, 138, 34)])),
    # harvest: wheat gold
    "harvest": (S.variant_pal([rgb(140, 100, 30), rgb(186, 140, 46), rgb(220, 178, 70), rgb(240, 208, 110), rgb(250, 230, 160), rgb(255, 246, 210)],
                              [rgb(40, 30, 14), rgb(90, 66, 26), rgb(150, 112, 40), rgb(200, 156, 60), rgb(232, 196, 100), rgb(248, 228, 170), rgb(255, 250, 230)],
                              DARK_EYES, S.RED_LEGS), INK_TIPS, ramp([rgb(110, 20, 24), rgb(170, 34, 36), rgb(214, 64, 56)])),
    # cinder: ash grey with ember-orange tips
    "cinder": (S.variant_pal([rgb(46, 44, 44), rgb(76, 72, 70), rgb(110, 104, 100), rgb(146, 138, 132), rgb(184, 176, 168), rgb(220, 214, 206)],
                             [rgb(16, 14, 14), rgb(40, 36, 34), rgb(76, 70, 66), rgb(112, 104, 98), rgb(214, 104, 30), rgb(240, 160, 70), rgb(252, 220, 170)],
                             AMBER_EYES, BLACK_LEGS), INK_TIPS, ramp([rgb(150, 66, 10), rgb(206, 100, 20), rgb(236, 138, 34)])),
}

_declare, _build, _paint = S.declare_parts, S.build_model, S.paint


def declare_parts():
    _declare()
    for args in [("pumpkin", 6, 2.5, 6), ("pstem", 1, 2, 1), ("pleaf", 2.5, .25, 2.5), ("tendril", .25, 1.5, .25)]:
        H.part(*args)


def paint(v=None):
    pal, wings, _ = v or DEFAULT
    pt = H.Painter()
    pt.T = _paint(pal, wings, LEAF)
    SKIN = pal.SKIN

    def pumpkin(f, x, y, w, h):                    # ribbed orange, cream vine swirls on the front
        if f == "up" and h > 3:
            return pick(SKIN, 0.66, x, y)
        swirl = f == "north" and h > 3 and noise(x, y, 41) > 0.7 and (x + y) % 3 == 0
        if swirl:
            return pick(SWIRL, 0.5, x, y)
        return pick(PUMPKIN, 0.55 + 0.15 * (f == "up") - 0.22 * (x % 3 == 0 and f != "up") + (noise(x, y, 42) - 0.5) * 0.08, x, y)
    for p in ("chest", "bust", "pumpkin"):
        pt(p, "all", pumpkin)
    for p in ("leaf_m", "leaf_l", "leaf_s", "leaf_side"):   # the skirt: pumpkin lobes
        pt(p, "all", lambda f, x, y, w, h: pick(PUMPKIN, 0.6 - 0.25 * (x == 0 or x == w - 1) - 0.2 * y / max(h, 1), x, y))
    pt("pstem", "all", lambda f, x, y, w, h: pick(STEM_BROWN, 0.5 + 0.2 * (f == "up"), x, y))
    pt("pleaf", "all", lambda f, x, y, w, h: pick(LEAF, 0.55 + 0.2 * (x == y) - 0.1 * (x + y > 3), x, y))
    pt("tendril", "all", lambda f, x, y, w, h: pick(LEAF, 0.5, x, y))
    return pt.T


STEM_BROWN = ramp([rgb(70, 46, 20), rgb(110, 76, 36), rgb(150, 110, 56)])


def build_model():
    m = _build()
    y = 28.3 + L                                    # a small pumpkin cap on her crown, its vine leaf over one ear
    rot = dict(pivot=[0, y, 0], rotation=[-6, 0, 0])
    m.bone("pumpkin_hat", "head", [0, y, 0], [S.C("pumpkin", [-3, y - 0.6, -3.0], **rot),
                                             S.C("pstem", [-0.5, y + 1.9, -0.6], pivot=[0, y + 1.9, 0], rotation=[-6, 0, -18]),
                                             S.C("pleaf", [2.4, y + 0.6, -2.5], pivot=[2.4, y + 0.6, -1.0], rotation=[0, 0, -35]),
                                             S.C("tendril", [3.2, y - 0.9, -2.2], **rot)])
    return m


if __name__ == "__main__":
    declare_parts()
    H.run(NAME, paint, build_model, S.animations, speak="sing", head_center=(0, 22.5 + L), body_center=(0, 15.5 + L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("beg", 0.2, 0, 0), ("beg", 0.2, -30, 8)])
    VT.write_variants(NAME, "pumpkin", paint, VARIANTS, build_model, S.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + L))])
