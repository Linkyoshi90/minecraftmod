#!/usr/bin/env python3
"""Reliquary Dove Harpy - the crow's geometry and animations (gen_crow_harpy.py), soft blue, carrying a sealed scroll.

    python tools/gen_reliquary_dove_harpy.py                 # write the three asset files and the variants
    python tools/gen_reliquary_dove_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-eighth wave): a keeper of small sacred things, blue-grey, gold thread and a red seal. Befriended, she keeps one item safe for you. Variants (reliquary_dove_harpy_<variant>.png): white, rose, ash, gold. Keep in sync with ReliquaryDoveHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
from bird_shades import shade as S  # noqa: E402
import variant_tools as VT  # noqa: E402
import gen_crow_harpy as Cr  # noqa: E402

NAME = "reliquary_dove_harpy"
Cr.NAME = NAME                                     # the crow's model and animations, under her name

SKIN = [(200, 150, 124), (226, 184, 160), (242, 210, 190), (252, 232, 218)]


def bird(hair, fea, ruff, cloth, eye, leg="#b86a6a", bead="#e8c050"):
    return Cr.cvariant(hair=S(hair, 6), fea=S(fea, 7), eye=S(eye), leg=S(leg), cloth=S(cloth), bead=S(bead, 3), ruff=S(ruff, 7), shift=1, gain=0.8, skin=SKIN)


DEFAULT = bird("#6a8ac0", "#7a9ac8", "#dce8f4", "#2a4a9a", "#2a3a6a")

VARIANTS = {
    # white: a temple dove
    "white": bird("#e8e8ec", "#f0f0f4", "#ffffff", "#c8a040", "#3a3a4a"),
    # rose: dusk over the bell tower
    "rose": bird("#c88a9a", "#d8a0a8", "#f4e0e4", "#7a2a4a", "#4a2a3a"),
    # ash: a very old keeper
    "ash": bird("#6a6a74", "#7a7a84", "#c8c8d0", "#3a3a48", "#a84a3a"),
    # gold: gilded, all of her
    "gold": bird("#c8a040", "#d8b050", "#f4e4b0", "#7a4a1a", "#4a2a14"),
}


def paint(v=None):
    return Cr.paint(v or DEFAULT)


if __name__ == "__main__":
    Cr.declare_parts()
    H.run(NAME, paint, Cr.build_model, Cr.animations, speak="caw", head_center=(0, 22.5 + Cr.L), body_center=(0, 15.5 + Cr.L),
          face_views=[("idle", 0, 0, 0), ("caw", 0.5, 0, 0), ("knock", 0.5, 0, 0), ("idle", 0, -28, 8)])
    VT.write_variants(NAME, "blue", paint, VARIANTS, Cr.build_model, Cr.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + Cr.L))])
