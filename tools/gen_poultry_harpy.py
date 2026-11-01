#!/usr/bin/env python3
"""Poultry Harpy - the crow's geometry with a hen's figure (build_model below), in a farm girl's dress and apron.

    python tools/gen_poultry_harpy.py                 # write the three asset files and the variants
    python tools/gen_poultry_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-eighth wave): the farmyard's own harpy, round and busy, in an apron over a work dress. Befriended, she keeps your crops growing and lays you eggs. Variants (poultry_harpy_<variant>.png): blue, brown, black, speckled. Keep in sync with PoultryHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
from bird_shades import shade as S  # noqa: E402
import variant_tools as VT  # noqa: E402
import gen_crow_harpy as Cr  # noqa: E402

NAME = "poultry_harpy"
Cr.NAME = NAME                                     # the crow's model and animations, under her name

SKIN = [(200, 150, 124), (226, 184, 160), (242, 210, 190), (252, 232, 218)]


def bird(hair, fea, ruff, cloth, eye, leg="#e0a030", bead="#c83a3a"):
    return Cr.cvariant(hair=S(hair, 6), fea=S(fea, 7), eye=S(eye), leg=S(leg), cloth=S(cloth), bead=S(bead, 3), ruff=S(ruff, 7), shift=1, gain=0.8, skin=SKIN)


DEFAULT = bird("#f0ece4", "#f4f0e8", "#ffffff", "#e8dcc4", "#c83a2a")

VARIANTS = {
    # blue: the blue laced hen
    "blue": bird("#7ab8e8", "#a8d4f4", "#e4f4fc", "#4a7ab8", "#3a5ab0"),
    # brown: a good layer
    "brown": bird("#a86a3a", "#b87a48", "#e8c8a0", "#6a8a4a", "#7a3a1e"),
    # black: the old black hen
    "black": bird("#24242a", "#2e2e36", "#6a6a78", "#8a2a2a", "#e0a030"),
    # speckled: grey and white flecks
    "speckled": bird("#9a9aa0", "#b0b0b8", "#e8e8ec", "#5a6a8a", "#a8542a"),
}


def paint(v=None):
    return Cr.paint(v or DEFAULT)


def build_model():
    """the crow's rig with a hen's figure: fuller at the bust, the hips and the thighs (the same cubes, drawn larger)"""
    m = Cr.build_model()
    full = {(2, 1.5, .75): 0.35, (5, 1.25, 3.5): 0.4, (2, 3.5, 2): 0.45}
    for b in m.bones:
        for c in b.get("cubes", []):
            k = full.get(tuple(c["size"]))
            if k:
                c["inflate"] = k
    return m


if __name__ == "__main__":
    Cr.declare_parts()
    H.run(NAME, paint, build_model, Cr.animations, speak="caw", head_center=(0, 22.5 + Cr.L), body_center=(0, 15.5 + Cr.L),
          face_views=[("idle", 0, 0, 0), ("caw", 0.5, 0, 0), ("knock", 0.5, 0, 0), ("idle", 0, -28, 8)])
    VT.write_variants(NAME, "white", paint, VARIANTS, build_model, Cr.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + Cr.L))])
