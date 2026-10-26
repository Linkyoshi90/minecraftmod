#!/usr/bin/env python3
"""Zhuque (the Vermilion Bird) Harpy - the phoenix's geometry and animations (gen_phoenix_harpy.py) in summer's colours.

    python tools/gen_zhuque_harpy.py                 # write the three asset files, the variants and their glow masks
    python tools/gen_zhuque_harpy.py --preview x.png # also render preview sheets

The south of the Chinese Four Symbols: the bird of fire and summer. Vermilion plumage burning gold at the tips, jade
silk with gold trim. Variants (zhuque_harpy_<variant>.png, each with a glow mask): dusk, golden, rose, jade. Keep in
sync with ZhuqueHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_phoenix_harpy as P  # noqa: E402
import variant_tools as VT  # noqa: E402

NAME = "zhuque_harpy"
P.NAME = NAME                                      # the phoenix script's model, animations and glow masks, under her name

HOT = lambda r, g, b: r >= 220 and g >= 120 and b < 140     # the gold tips of her flame glow
JADE = [(10, 60, 40), (20, 96, 64), (36, 136, 90), (70, 176, 124), (120, 214, 168)]
GOLDS = [(150, 100, 30), (210, 160, 50), (250, 210, 90), (255, 240, 170)]
ZHUQUE = P.pvariant(
    [(120, 10, 14), (180, 26, 20), (226, 60, 28), (250, 120, 40), (255, 190, 80), (255, 236, 170)],
    [(100, 8, 12), (160, 20, 18), (214, 48, 24), (244, 96, 32), (252, 156, 52), (255, 210, 110), (255, 244, 200)],
    [(150, 80, 10), (210, 130, 20), (246, 184, 44), (255, 222, 120), (255, 246, 210)],
    [(150, 90, 30), (200, 130, 40), (236, 172, 60), (250, 206, 100), (255, 232, 160)],
    JADE, GOLDS, HOT, (60, 14, 6), (90, 30, 10))
P.DEFAULT = ZHUQUE

VARIANTS = {
    "dusk": P.pvariant(
        [(70, 8, 30), (120, 18, 50), (176, 40, 70), (220, 80, 90), (250, 140, 110), (255, 200, 160)],
        [(60, 6, 26), (110, 14, 44), (164, 32, 64), (210, 70, 84), (240, 120, 100), (252, 180, 140), (255, 230, 200)],
        [(150, 60, 30), (200, 100, 50), (240, 150, 80), (255, 200, 140), (255, 236, 210)],
        [(120, 60, 50), (170, 90, 70), (210, 130, 100), (240, 180, 150), (255, 220, 200)],
        [(30, 20, 60), (50, 36, 96), (76, 56, 136), (110, 90, 176), (160, 140, 214)], GOLDS,
        lambda r, g, b: r >= 230 and g >= 130, (50, 10, 20), (80, 20, 30)),
    "golden": P.pvariant(
        [(160, 100, 10), (210, 150, 20), (244, 196, 50), (255, 226, 110), (255, 244, 180), (255, 252, 230)],
        [(150, 90, 10), (200, 140, 20), (238, 186, 44), (252, 218, 96), (255, 238, 160), (255, 250, 210), (255, 255, 240)],
        [(180, 100, 10), (230, 150, 20), (255, 200, 60), (255, 230, 150), (255, 248, 220)],
        [(160, 110, 30), (210, 150, 40), (240, 190, 70), (252, 220, 130), (255, 240, 190)],
        [(120, 20, 20), (170, 32, 28), (210, 56, 44), (236, 96, 70), (250, 150, 120)], GOLDS,
        lambda r, g, b: r >= 240 and g >= 200, (110, 60, 10), (130, 80, 20)),
    "rose": P.pvariant(
        [(140, 30, 70), (196, 56, 100), (236, 100, 140), (250, 160, 180), (255, 210, 220), (255, 244, 248)],
        [(130, 26, 64), (186, 50, 94), (230, 90, 130), (246, 140, 166), (252, 190, 206), (255, 226, 234), (255, 250, 252)],
        [(170, 60, 90), (220, 100, 130), (250, 150, 170), (255, 210, 220), (255, 244, 248)],
        [(170, 100, 110), (210, 140, 150), (240, 180, 186), (252, 214, 218), (255, 240, 242)],
        JADE, GOLDS, lambda r, g, b: r >= 240 and g >= 150 and b >= 160, (80, 20, 40), (110, 30, 60)),
    "jade": P.pvariant(
        [(10, 70, 50), (20, 120, 80), (40, 170, 110), (90, 214, 150), (170, 244, 200), (230, 255, 240)],
        [(8, 60, 42), (16, 104, 70), (30, 156, 100), (60, 200, 136), (120, 236, 180), (200, 252, 226), (245, 255, 250)],
        [(150, 120, 10), (210, 170, 20), (246, 214, 60), (255, 236, 140), (255, 250, 220)],
        [(60, 110, 70), (90, 150, 100), (130, 190, 140), (180, 226, 190), (226, 250, 232)],
        [(110, 14, 20), (160, 26, 28), (206, 50, 40), (236, 90, 66), (250, 140, 110)], GOLDS,
        lambda r, g, b: g >= 200 and g > r + 40, (10, 50, 30), (20, 70, 40)),
}


if __name__ == "__main__":
    P.declare_parts()
    H.run(NAME, P.paint, P.build_model, P.animations, speak="strike", head_center=(0, 22.5 + P.L), body_center=(0, 15.5 + P.L),
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("strike", 0.3, -25, 10), ("cry", 0.5, -20, 8), ("walk", 0.25, -35, 12), ("fly", 0.0, -20, 10)],
          face_views=[("idle", 0, 0, 0), ("strike", 0.4, 0, 0), ("cry", 0.5, 0, 0), ("idle", 0, -30, 8)])
    VT.write_variants(NAME, "vermilion", P.paint, VARIANTS, P.build_model, P.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.0, -20, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + P.L))],
                      glow=lambda tex, suffix: P.write_glowmask(tex, VARIANTS[suffix[1:]].hot if suffix else HOT, suffix))
