#!/usr/bin/env python3
"""Landharpy - the ostrich's geometry and animations (gen_ostrich_harpy.py), white-haired, in worn blue work clothes.

    python tools/gen_landharpy_harpy.py                 # write the three asset files and the variants
    python tools/gen_landharpy_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-eighth wave): a harpy who never bothered with the sky, white-haired, grey-tipped wings kept folded, big talons, torn jeans. Befriended, her kick stuns the monsters near her friend. Variants (landharpy_harpy_<variant>.png): brown, ash, red, black. Keep in sync with LandharpyHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
from bird_shades import shade as S  # noqa: E402
import gen_ostrich_harpy as Os  # noqa: E402

NAME = "landharpy_harpy"
Os.NAME = NAME                                     # the ostrich's model and animations, under her name

SKIN = [(200, 150, 124), (226, 184, 160), (242, 210, 190), (252, 232, 218)]


def land(hair, fea, cloth, eye="#7a5a3a", leg="#2a2a30"):
    return Os.variant(hair=S(hair, 6), skin=SKIN, fea=S(fea, 7), eye=S(eye), leg=S(leg), leather=S("#3a2a22", 4), cloth=S(cloth, 4), accent=S("#c8c8d0", 3), shift=2)


DEFAULT = land("#e8e8f0", "#d8d4cc", "#3a5a9a")

VARIANTS = {
    # brown: a dusty road
    "brown": land("#8a6a48", "#9a7a58", "#7a5a3a"),
    # ash: the grey of gravel hills
    "ash": land("#8a8a92", "#9a9aa2", "#3a3a48"),
    # red: desert clay on her feathers
    "red": land("#a84a2a", "#b85a3a", "#3a5a9a"),
    # black: road tar
    "black": land("#24242a", "#2e2e36", "#5a2a2a", "#c8a040"),
}


def paint(v=None):
    return Os.paint(v or DEFAULT)


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide", "textures", "entity")
    for k, v in VARIANTS.items():
        H.write_png(os.path.join(tex_dir, f"{NAME}_{k}.png"), paint(v))
    print(f"{NAME}: {len(VARIANTS)} variants ({', '.join(VARIANTS)})")


if __name__ == "__main__":
    Os.declare_parts()
    H.run(NAME, paint, Os.build_model, Os.animations, speak="display",
          head_center=(0, 22.5 + Os.HEAD), body_center=(0, 15.5 + Os.L), scale=10.2,
          main_views=[("idle", 0, 0, 0), ("idle", 0, -35, 12), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
                      ("run", 0.0, -30, 10), ("display", 0.1, -20, 8), ("kick", 0.55, -50, 6), ("brood", 0.9, -30, 10)],
          face_views=[("idle", 0, 0, 0), ("display", 0.5, 0, 0), ("kick", 0.6, 0, 0), ("idle", 0, -40, 8)])
    write_variants()
