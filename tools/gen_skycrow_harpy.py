#!/usr/bin/env python3
"""Skycrow Harpy - the crow's geometry and animations (gen_crow_harpy.py), blue-black and crackling.

    python tools/gen_skycrow_harpy.py                 # write the three asset files and the variants
    python tools/gen_skycrow_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-eighth wave): a storm crow in black plate, blue light in the gaps of her feathers. Befriended, lightning finds the monsters near her friend. Variants (skycrow_harpy_<variant>.png): steel, ash, violet, crimson. Keep in sync with SkycrowHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
from bird_shades import shade as S  # noqa: E402
import variant_tools as VT  # noqa: E402
import gen_crow_harpy as Cr  # noqa: E402

NAME = "skycrow_harpy"
Cr.NAME = NAME                                     # the crow's model and animations, under her name

SKIN = [(200, 150, 124), (226, 184, 160), (242, 210, 190), (252, 232, 218)]


def bird(hair, fea, ruff, cloth, eye, leg="#2a3040", bead="#7ab8f8"):
    return Cr.cvariant(hair=S(hair, 6), fea=S(fea, 7), eye=S(eye), leg=S(leg), cloth=S(cloth), bead=S(bead, 3), ruff=S(ruff, 7), shift=1, gain=0.8, skin=SKIN)


DEFAULT = bird("#141a30", "#10142a", "#3a6ac8", "#1e2438", "#5ab8f8")

VARIANTS = {
    # steel: a grey sky
    "steel": bird("#3a4250", "#2e3644", "#a8b4c4", "#4a5464", "#c8e0f8"),
    # ash: burnt out, pale
    "ash": bird("#6a6a70", "#55555c", "#c8c8d0", "#3a3a42", "#f0a040"),
    # violet: the End's own light
    "violet": bird("#241438", "#1c1030", "#8a4ae0", "#2e1a48", "#c88af8"),
    # crimson: a red storm
    "crimson": bird("#2a1014", "#200c10", "#c83a3a", "#3a1418", "#f8704a"),
}


def paint(v=None):
    return Cr.paint(v or DEFAULT)


if __name__ == "__main__":
    Cr.declare_parts()
    H.run(NAME, paint, Cr.build_model, Cr.animations, speak="caw", head_center=(0, 22.5 + Cr.L), body_center=(0, 15.5 + Cr.L),
          face_views=[("idle", 0, 0, 0), ("caw", 0.5, 0, 0), ("knock", 0.5, 0, 0), ("idle", 0, -28, 8)])
    VT.write_variants(NAME, "night", paint, VARIANTS, Cr.build_model, Cr.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + Cr.L))])
