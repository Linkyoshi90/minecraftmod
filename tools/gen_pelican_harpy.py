#!/usr/bin/env python3
"""Pelican Harpy - the seagull harpy's rig and animations (gen_seagull_harpy.py) in a pelican's colours.

    python tools/gen_pelican_harpy.py              # write the three asset files and the variants

Field guide (thirty-sixth wave): beaches and estuaries. Keep the variants in sync with PelicanHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_seagull_harpy as S  # noqa: E402
import variant_tools as VT  # noqa: E402
from harpy_lib import rgb, ramp  # noqa: E402

NAME = "pelican_harpy"
S.NAME = NAME                                    # the gull's model and animations, under her name
L = S.L

ORANGE_LEGS = [rgb(150, 80, 10), rgb(206, 120, 20), rgb(236, 160, 40), rgb(248, 196, 80), rgb(252, 226, 150)]
EYES = S.DARK_EYES


def ramp6(a, b):
    """six steps from dark to light"""
    return [rgb(*[int(a[i] + (b[i] - a[i]) * t / 5) for i in range(3)]) for t in range(6)]


def ramp7(a, b):
    return [rgb(*[int(a[i] + (b[i] - a[i]) * t / 6) for i in range(3)]) for t in range(7)]


KERCHIEF = ramp([rgb(230, 170, 60), rgb(244, 200, 90), rgb(252, 226, 140)])
DEFAULT = (S.variant_pal(ramp6((190, 186, 170), (255, 252, 244)), ramp7((60, 56, 54), (250, 246, 238)), EYES, ORANGE_LEGS), S.WINGS, KERCHIEF)
VARIANTS = {
    "brown": (S.variant_pal(ramp6((90, 70, 50), (220, 200, 170)), ramp7((30, 24, 20), (200, 180, 150)), EYES, ramp6((40, 36, 34), (120, 110, 100))), S.WINGS, KERCHIEF),
    "pink": (S.variant_pal(ramp6((210, 150, 150), (255, 230, 226)), ramp7((90, 60, 60), (252, 226, 222)), EYES, ORANGE_LEGS), S.WINGS, KERCHIEF),
    "grey": (S.variant_pal(ramp6((120, 124, 130), (236, 238, 242)), ramp7((40, 42, 48), (232, 234, 238)), EYES, ramp6((60, 60, 66), (150, 150, 156))), S.WINGS, KERCHIEF),
    "golden": (S.variant_pal(ramp6((170, 130, 50), (250, 232, 180)), ramp7((70, 50, 20), (248, 228, 170)), EYES, ORANGE_LEGS), S.WINGS, KERCHIEF),
}


def paint(v=DEFAULT):
    return S.paint(*(v or DEFAULT))


if __name__ == "__main__":
    S.declare_parts()
    H.run(NAME, paint, S.build_model, S.animations, speak="sing", head_center=(0, 22.5 + L), body_center=(0, 15.5 + L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("beg", 0.2, 0, 0), ("beg", 0.2, -30, 8)])
    VT.write_variants(NAME, "white", paint, VARIANTS, S.build_model, S.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 30, (0, 22.5 + L))])
