#!/usr/bin/env python3
"""Hummingbird Harpy - the phoenix script's geometry and animations (gen_phoenix_harpy.py), small, jewel-bright and with no fire in her.

    python tools/gen_hummingbird_harpy.py                 # write the three asset files and the variants

Field guide (thirty-third wave): she hangs in the air at a flower and is gone. Befriended, near flowers her friend moves fast.
Keep the variants in sync with HummingbirdHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
from bird_shades import shade as S  # noqa: E402
import gen_phoenix_harpy as P  # noqa: E402
import variant_tools as VT  # noqa: E402

NAME = "hummingbird_harpy"
P.NAME = NAME                                      # the phoenix script's model and animations, under her name

NEVER = lambda r, g, b: False                      # no fire in her: nothing glows
GOLDS = [(150, 100, 30), (210, 160, 50), (250, 210, 90), (255, 240, 170)]


def hum(hair, fea, silk, eye="#1e1e2a", leg="#2a2a32"):
    return P.pvariant(S(hair, 6), S(fea, 7), S(eye), S(leg), S(silk), GOLDS, NEVER, (14, 20, 30), (30, 40, 50))


DEFAULT = hum("#c81e4a", "#2a9a5a", "#f4e8ec")
P.DEFAULT = DEFAULT

VARIANTS = {
    "emerald": hum("#1ea85a", "#2ab86a", "#e8f8ec"),
    "violet": hum("#7a2ac8", "#3a7ac8", "#f0e8f8"),
    "sapphire": hum("#1e5ad8", "#2a8ad8", "#e4ecf8"),
    "amber": hum("#d8782a", "#a85a2a", "#f8ecdc"),
}


if __name__ == "__main__":
    P.declare_parts()
    H.run(NAME, P.paint, P.build_model, P.animations, speak="strike", head_center=(0, 22.5 + P.L), body_center=(0, 15.5 + P.L),
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("strike", 0.3, -25, 10), ("cry", 0.5, -20, 8), ("walk", 0.25, -35, 12), ("fly", 0.0, -20, 10)],
          face_views=[("idle", 0, 0, 0), ("strike", 0.4, 0, 0), ("cry", 0.5, 0, 0), ("idle", 0, -30, 8)])
    VT.write_variants(NAME, "ruby", P.paint, VARIANTS, P.build_model, P.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.0, -20, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + P.L))])
