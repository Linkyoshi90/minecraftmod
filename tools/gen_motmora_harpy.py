#!/usr/bin/env python3
"""Motmora Harpy - the phoenix script's geometry and animations (gen_phoenix_harpy.py), with no fire in her.

    python tools/gen_motmora_harpy.py                 # write the three asset files and the variants
    python tools/gen_motmora_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-eighth wave): a long-tailed jewelled forest bird, blue with woven patterns and beaded tassels. Befriended, her tail display stuns the monsters near her friend. Variants (motmora_harpy_<variant>.png): green, turquoise, rufous, gold. Keep in sync with MotmoraHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
from bird_shades import shade as S  # noqa: E402
import gen_phoenix_harpy as P  # noqa: E402
import variant_tools as VT  # noqa: E402

NAME = "motmora_harpy"
P.NAME = NAME                                      # the phoenix script's model and animations, under her name

NEVER = lambda r, g, b: False                      # no fire in her: nothing glows
GOLDS = [(150, 100, 30), (210, 160, 50), (250, 210, 90), (255, 240, 170)]


def mot(hair, fea, silk, eye="#2a4a9a", leg="#3a3a5a"):
    return P.pvariant(S(hair, 6), S(fea, 7), S(eye), S(leg), S(silk), GOLDS, NEVER, (14, 20, 50), (30, 40, 80))


DEFAULT = mot("#1e4ac8", "#2a5ad8", "#e0ecfc")
P.DEFAULT = DEFAULT

VARIANTS = {
    # green: the rainforest's
    "green": mot("#1e8a5a", "#2aa86a", "#e4f8ec", "#a83a2a"),
    # turquoise: the Yucatan's blue-browed bird
    "turquoise": mot("#1ea8b8", "#2ab8c8", "#e0f8f8", "#2a4a9a"),
    # rufous: a warm red head
    "rufous": mot("#a8502a", "#2a8a7a", "#f8ecdc", "#3a2a1a"),
    # gold: court plumage
    "gold": mot("#d8a02a", "#e8b83a", "#fff4d0", "#7a3a14"),
}


if __name__ == "__main__":
    P.declare_parts()
    H.run(NAME, P.paint, P.build_model, P.animations, speak="strike", head_center=(0, 22.5 + P.L), body_center=(0, 15.5 + P.L),
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("strike", 0.3, -25, 10), ("cry", 0.5, -20, 8), ("walk", 0.25, -35, 12), ("fly", 0.0, -20, 10)],
          face_views=[("idle", 0, 0, 0), ("strike", 0.4, 0, 0), ("cry", 0.5, 0, 0), ("idle", 0, -30, 8)])
    VT.write_variants(NAME, "blue", P.paint, VARIANTS, P.build_model, P.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.0, -20, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + P.L))])
