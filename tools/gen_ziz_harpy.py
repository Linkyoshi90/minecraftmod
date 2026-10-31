#!/usr/bin/env python3
"""Ziz Harpy - the phoenix's geometry and animations (gen_phoenix_harpy.py), without the fire: rainbow wings and a long tail.

    python tools/gen_ziz_harpy.py                 # write the three asset files and the variants
    python tools/gen_ziz_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-sixth wave): the great sky-bird of Jewish legend, whose wings can blot out the sun. Blue and white, a golden beak-pale ruff, wings
that run through the rainbow and a long tail. Befriended, once a day she spreads them and clears the rain from the sky. Variants (ziz_harpy_<variant>.png): dawn, gold, white, storm. Keep in sync with ZizHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_phoenix_harpy as P  # noqa: E402
import variant_tools as VT  # noqa: E402

NAME = "ziz_harpy"
P.NAME = NAME                                      # the phoenix script's model and animations, under her name

NEVER = lambda r, g, b: False                      # no fire in her: nothing glows
GOLDS = [(150, 100, 30), (210, 160, 50), (250, 210, 90), (255, 240, 170)]
EYE = [(20, 40, 100), (34, 70, 150), (60, 110, 200), (120, 170, 236), (200, 226, 255)]
LEG = [(150, 90, 20), (200, 130, 30), (236, 170, 50), (250, 206, 100), (255, 232, 160)]
RAINBOW = [(120, 20, 40), (214, 50, 50), (244, 130, 50), (246, 214, 80), (90, 200, 120), (60, 150, 230), (150, 110, 230)]


def ziz(hair, fea=RAINBOW, silk=None, eye=EYE, leg=LEG):
    return P.pvariant(hair, fea, eye, leg, silk or [(230, 234, 244), (240, 244, 250), (248, 250, 254), (252, 252, 255), (255, 255, 255)], GOLDS, NEVER, (14, 20, 50), (30, 40, 80))


DEFAULT = ziz([(20, 70, 150), (36, 110, 200), (60, 150, 230), (110, 190, 244), (176, 224, 252), (232, 246, 255)])
P.DEFAULT = DEFAULT

VARIANTS = {
    # dawn: rose and gold over the same wings
    "dawn": ziz([(160, 60, 80), (206, 90, 100), (236, 130, 120), (248, 176, 150), (252, 216, 190), (255, 240, 226)]),
    # gold: wings of gold from root to tip
    "gold": ziz([(150, 100, 20), (200, 146, 30), (236, 190, 60), (250, 222, 120), (255, 242, 180), (255, 252, 230)],
                [(120, 80, 14), (170, 120, 24), (214, 164, 44), (240, 200, 80), (250, 226, 130), (254, 240, 180), (255, 250, 226)]),
    # white: the bird of the upper air
    "white": ziz([(170, 176, 190), (200, 206, 216), (226, 230, 236), (242, 244, 248), (250, 250, 252), (255, 255, 255)]),
    # storm: the wing that hides the sun
    "storm": ziz([(24, 28, 44), (40, 46, 70), (62, 70, 100), (94, 104, 138), (140, 150, 184), (200, 208, 230)],
                 [(16, 20, 34), (30, 36, 56), (48, 56, 84), (74, 84, 118), (110, 122, 158), (160, 170, 204), (220, 226, 244)],
                 [(50, 56, 80), (80, 88, 116), (120, 130, 158), (170, 178, 204), (220, 226, 240)]),
}


if __name__ == "__main__":
    P.declare_parts()
    H.run(NAME, P.paint, P.build_model, P.animations, speak="strike", head_center=(0, 22.5 + P.L), body_center=(0, 15.5 + P.L),
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("strike", 0.3, -25, 10), ("cry", 0.5, -20, 8), ("walk", 0.25, -35, 12), ("fly", 0.0, -20, 10)],
          face_views=[("idle", 0, 0, 0), ("strike", 0.4, 0, 0), ("cry", 0.5, 0, 0), ("idle", 0, -30, 8)])
    VT.write_variants(NAME, "blue", P.paint, VARIANTS, P.build_model, P.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.0, -20, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + P.L))])
