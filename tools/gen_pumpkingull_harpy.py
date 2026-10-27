#!/usr/bin/env python3
"""Pumpkingull Harpy - the seagull harpy's rig and animations (gen_seagull_harpy.py) in autumn colours, with a carved
pumpkin worn as a hat (bone `pumpkin_hat`).

    python tools/gen_pumpkingull_harpy.py              # write the three asset files and the variants
    python tools/gen_pumpkingull_harpy.py --preview x  # also render preview sheets

Field guide (sixteenth wave): a Harmontide gull of the autumn shores, orange and black. While she wears her pumpkin the
endermen near her friend take no offence at being looked at. Variants (pumpkingull_harpy_<variant>.png): ghost, black,
harvest, cinder. Keep in sync with PumpkingullHarpy.Variant.
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

DEFAULT = (S.variant_pal(ORANGE_HAIR, ORANGE_FEA, AMBER_EYES, BLACK_LEGS), INK_TIPS,
           ramp([rgb(30, 22, 30), rgb(60, 44, 62), rgb(96, 70, 100)]))
VARIANTS = {
    # ghost: pale grey-white, a pale pumpkin
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
    for args in [("pumpkin", 6, 4.5, 6), ("pumpkin_face", 6, 4.5, .25), ("pstem", 1, 1.5, 1)]:
        H.part(*args)


def paint(v=None):
    pal, wings, kerchief = v or DEFAULT
    pt = H.Painter()
    pt.T = _paint(pal, wings, kerchief)
    pt("pumpkin", "all", lambda f, x, y, w, h: pick(PUMPKIN, 0.5 + 0.2 * (f == "up") - 0.18 * (x % 4 == 0 and f != "up"), x, y))

    def face(f, x, y, w, h):                       # a carved grin, lit from inside
        if f != "north":
            return None
        eyes = y in (3, 4, 5) and (abs(x - 6) <= 5 - y or abs(x - 17) <= 5 - y)
        grin = 10 <= y <= 13 and 4 <= x <= 19 and (y < 12 or x % 4 != 0) and abs(x - 11.5) < 8 - (13 - y)
        return pick(GLOW, 0.6 + 0.3 * noise(x, y, 7), x, y) if eyes or grin else CLEAR_
    pt("pumpkin_face", "all", face)
    pt("pstem", "all", lambda f, x, y, w, h: pick(STEM, 0.5, x, y))
    return pt.T


CLEAR_ = H.CLEAR


def build_model():
    m = _build()
    y = 28.3 + L                                    # just above her hair, tipped back a little
    m.bone("pumpkin_hat", "head", [0, y, 0], [S.C("pumpkin", [-3, y, -3.2], pivot=[0, y, 0], rotation=[-8, 0, 0]),
                                             S.C("pumpkin_face", [-3, y, -3.45], pivot=[0, y, 0], rotation=[-8, 0, 0]),
                                             S.C("pstem", [-0.5, y + 4.5, -0.7], pivot=[0, y, 0], rotation=[-8, 0, 0])])
    return m


if __name__ == "__main__":
    declare_parts()
    H.run(NAME, paint, build_model, S.animations, speak="sing", head_center=(0, 22.5 + L), body_center=(0, 15.5 + L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("beg", 0.2, 0, 0), ("beg", 0.2, -30, 8)])
    VT.write_variants(NAME, "pumpkin", paint, VARIANTS, build_model, S.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + L))])
