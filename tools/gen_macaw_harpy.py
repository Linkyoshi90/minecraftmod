#!/usr/bin/env python3
"""Macaw Harpy - the toucan's geometry and animations (gen_toucan_harpy.py) in a macaw's blue and gold.

    python tools/gen_macaw_harpy.py                 # write the three asset files and the variants
    python tools/gen_macaw_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-sixth wave): a blue and gold macaw of the jungle canopy, a big curved dark beak over her brow, who repeats everything she hears.
Befriended, she calls out in the voice of any monster hiding near her friend, and shows them where it is. Variants (macaw_harpy_<variant>.png): scarlet, green, hyacinth, military. Keep in sync with MacawHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_toucan_harpy as Tc  # noqa: E402
from harpy_lib import rgb, ramp  # noqa: E402

NAME = "macaw_harpy"
Tc.NAME = NAME                                     # the toucan's model and animations, under her name

R = lambda *c: ramp([rgb(*x) for x in c])
DARK_BEAK = (R((30, 30, 36), (54, 54, 62), (84, 84, 94), (120, 120, 132)), R((14, 14, 18), (26, 26, 30), (44, 44, 50)))
PALE_EYE = [(70, 60, 30), (120, 106, 60), (176, 160, 100), (220, 210, 150), (248, 244, 210)]


def macaw(hair, fea, bib, leaf, ring=None, beak=DARK_BEAK):
    return dict(pal=Tc.variant_pal(hair, fea, PALE_EYE), bib=R(*bib), leaf=R(*leaf), ring=R(*(ring or GOLD_RING)), beak=beak)


GOLD_RING = [(170, 120, 20), (226, 176, 40), (250, 220, 110)]
GOLD_BIB = [(226, 160, 20), (244, 190, 40), (252, 216, 90), (255, 238, 160)]
DEFAULT = macaw([(14, 60, 130), (24, 96, 180), (40, 136, 220), (90, 180, 240), (160, 216, 250), (220, 242, 255)],
                [(10, 46, 110), (18, 76, 156), (30, 112, 200), (56, 150, 232), (110, 190, 246), (180, 224, 252), (232, 246, 255)],
                GOLD_BIB, [(170, 120, 20), (226, 170, 30), (250, 206, 70), (255, 232, 140)])

VARIANTS = {
    # scarlet: red, a band of yellow and blue on the wing
    "scarlet": macaw([(110, 12, 18), (164, 24, 26), (214, 46, 36), (240, 90, 66), (250, 150, 124), (255, 210, 196)],
                     [(90, 8, 14), (140, 18, 22), (192, 36, 30), (228, 70, 50), (246, 120, 96), (252, 180, 156), (255, 232, 220)],
                     [(214, 46, 36), (236, 80, 56), (248, 130, 100), (254, 190, 164)], [(20, 60, 170), (40, 100, 210), (240, 200, 50), (255, 234, 130)]),
    # green: the military macaw's cousin, the great green
    "green": macaw([(14, 80, 50), (24, 120, 70), (40, 160, 90), (90, 200, 120), (160, 232, 170), (222, 250, 226)],
                   [(10, 64, 40), (18, 100, 58), (30, 140, 78), (56, 180, 100), (110, 214, 140), (176, 238, 190), (232, 252, 236)],
                   [(40, 160, 90), (90, 200, 120), (160, 232, 170), (222, 250, 226)], [(30, 100, 190), (60, 140, 226), (220, 60, 40), (250, 130, 90)]),
    # hyacinth: deep violet-blue, the biggest of them all
    "hyacinth": macaw([(30, 20, 110), (44, 36, 160), (64, 56, 200), (100, 96, 230), (160, 156, 248), (220, 218, 255)],
                      [(22, 14, 90), (36, 28, 136), (54, 46, 180), (84, 76, 216), (130, 124, 240), (186, 182, 250), (236, 234, 255)],
                      [(64, 56, 200), (100, 96, 230), (160, 156, 248), (220, 218, 255)], [(44, 36, 160), (64, 56, 200), (240, 200, 50), (255, 234, 130)]),
    # military: olive, a red brow
    "military": macaw([(50, 70, 30), (80, 110, 44), (116, 150, 60), (156, 190, 90), (200, 224, 140), (236, 246, 200)],
                      [(40, 56, 24), (66, 92, 36), (98, 130, 52), (134, 170, 76), (174, 206, 116), (214, 234, 170), (244, 250, 222)],
                      [(116, 150, 60), (156, 190, 90), (200, 224, 140), (236, 246, 200)], [(170, 30, 30), (214, 56, 44), (60, 110, 190), (120, 170, 230)]),
}


def paint(v=None):
    """the toucan's painter with her skirt and jewellery recoloured (module globals there)"""
    v = v or DEFAULT
    Tc.LEAF, Tc.RING = v["leaf"], v["ring"]
    return Tc.paint(v["pal"], v["bib"], v["beak"])


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide", "textures", "entity")
    for k, v in VARIANTS.items():
        H.write_png(os.path.join(tex_dir, f"{NAME}_{k}.png"), paint(v))
    print(f"{NAME}: {len(VARIANTS)} variants ({', '.join(VARIANTS)})")


if __name__ == "__main__":
    Tc.declare_parts()
    H.run(NAME, paint, Tc.build_model, Tc.animations, speak="sing", head_center=(0, 22.5 + Tc.L), body_center=(0, 15.5 + Tc.L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("idle", 0, -40, 8)])
    write_variants()
