#!/usr/bin/env python3
"""Duck Harpy - the seagull harpy's rig and animations (gen_seagull_harpy.py) in a duck's colours.

    python tools/gen_duck_harpy.py              # write the three asset files and the variants

Field guide (thirty-third wave): mallard, mandarin, the white farm duck and more. Befriended, she tells her friend rain is coming.
Keep the variants in sync with DuckHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_seagull_harpy as S  # noqa: E402
import variant_tools as VT  # noqa: E402
from harpy_lib import rgb, ramp  # noqa: E402

NAME = "duck_harpy"
S.NAME = NAME                                    # the gull's model and animations, under her name
L = S.L

ORANGE_LEGS = [rgb(150, 80, 10), rgb(206, 120, 20), rgb(236, 160, 40), rgb(248, 196, 80), rgb(252, 226, 150)]
EYES = S.DARK_EYES


def ramp6(a, b):
    """six steps from dark to light"""
    return [rgb(*[int(a[i] + (b[i] - a[i]) * t / 5) for i in range(3)]) for t in range(6)]


def ramp7(a, b):
    return [rgb(*[int(a[i] + (b[i] - a[i]) * t / 6) for i in range(3)]) for t in range(7)]


GREEN_HEAD = ramp6((10, 50, 30), (70, 170, 110))
BROWN = ramp7((30, 22, 16), (214, 196, 172))
KERCHIEF = ramp([rgb(230, 230, 226), rgb(244, 244, 240), rgb(252, 252, 250)])
DEFAULT = (S.variant_pal(GREEN_HEAD, BROWN, EYES, ORANGE_LEGS), S.WINGS, KERCHIEF)
VARIANTS = {
    # mandarin: russet and orange, a purple-blue back
    "mandarin": (S.variant_pal(ramp6((90, 30, 16), (240, 150, 70)), ramp7((20, 26, 60), (240, 220, 190)), EYES, ORANGE_LEGS), S.WINGS, ramp([rgb(150, 40, 30), rgb(200, 70, 40), rgb(236, 120, 60)])),
    # white: the farm duck
    "white": (S.variant_pal(ramp6((190, 186, 170), (255, 255, 250)), ramp7((150, 148, 140), (255, 255, 252)), EYES, ORANGE_LEGS), S.WINGS, ramp([rgb(30, 70, 150), rgb(50, 96, 186), rgb(90, 136, 214)])),
    # teal: a chestnut head, a green eye patch of a palette
    "teal": (S.variant_pal(ramp6((70, 34, 20), (190, 110, 70)), ramp7((30, 44, 40), (210, 214, 200)), EYES, ramp6((60, 60, 60), (170, 170, 170))), S.WINGS, ramp([rgb(14, 90, 76), rgb(24, 140, 120), rgb(70, 196, 170)])),
    # black: the East Indie, glossy green-black
    "black": (S.variant_pal(ramp6((10, 14, 14), (60, 90, 80)), ramp7((8, 10, 12), (90, 110, 104)), EYES, ramp6((10, 10, 12), (70, 70, 76))), S.WINGS, ramp([rgb(120, 90, 20), rgb(190, 150, 40), rgb(240, 200, 80)])),
}


def paint(v=DEFAULT):
    return S.paint(*(v or DEFAULT))


if __name__ == "__main__":
    S.declare_parts()
    H.run(NAME, paint, S.build_model, S.animations, speak="sing", head_center=(0, 22.5 + L), body_center=(0, 15.5 + L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("beg", 0.2, 0, 0), ("beg", 0.2, -30, 8)])
    VT.write_variants(NAME, "mallard", paint, VARIANTS, S.build_model, S.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 30, (0, 22.5 + L))])
