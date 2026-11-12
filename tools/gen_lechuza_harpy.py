#!/usr/bin/env python3
"""La Lechuza Harpy - the owl's geometry and animations (gen_owl_harpy.py) in a barn-brown scowl.

    python tools/gen_lechuza_harpy.py                 # write the three asset files and the variants
    python tools/gen_lechuza_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-sixth wave): the witch-owl of Mexico and South Texas, whose cry at night warns of what is coming. Brown hair and wings,
blue eyes, a scowl. At night her cry drives monsters back from her friend and warns them. Variants (lechuza_harpy_<variant>.png): ash, white, black, red. Keep in sync with LechuzaHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_owl_harpy as Ow  # noqa: E402
from harpy_lib import rgb, ramp  # noqa: E402

NAME = "lechuza_harpy"
Ow.NAME = NAME                                     # the owl's model and animations, under her name

R = lambda *c: ramp([rgb(*x) for x in c])
LEG = R((80, 50, 26), (120, 80, 40), (166, 116, 62), (204, 156, 96), (232, 196, 140))
BLUE_EYES = R((20, 60, 110), (34, 96, 160), (60, 140, 200), (120, 190, 232), (200, 232, 250))
RED_EYES = R((90, 10, 14), (150, 20, 20), (206, 44, 30), (240, 96, 60), (252, 176, 130))


def owl(hair, fea, eye, moss):
    return dict(pal=Ow.variant_pal(R(*hair), R(*fea), eye, LEG), moss=R(*moss))


DEFAULT = owl([(58, 30, 14), (92, 50, 24), (132, 76, 38), (170, 106, 58), (206, 146, 94), (236, 196, 150)],
              [(46, 24, 12), (76, 42, 20), (112, 64, 32), (150, 92, 48), (188, 130, 80), (222, 178, 132), (246, 226, 196)],
              BLUE_EYES, [(66, 36, 18), (104, 60, 30), (146, 90, 48), (186, 130, 80), (224, 180, 136)])

VARIANTS = {
    "ash": owl([(40, 38, 38), (68, 66, 66), (100, 98, 98), (136, 134, 134), (176, 174, 174), (220, 218, 218)],
               [(32, 30, 30), (56, 54, 54), (86, 84, 84), (118, 116, 116), (156, 154, 154), (198, 196, 196), (238, 236, 236)],
               BLUE_EYES, [(46, 44, 44), (74, 72, 72), (108, 106, 106), (146, 144, 144), (190, 188, 188)]),
    "white": owl([(150, 140, 126), (190, 180, 166), (220, 212, 200), (238, 232, 222), (250, 246, 240), (255, 254, 252)],
                 [(140, 130, 116), (178, 168, 154), (210, 202, 190), (232, 226, 216), (246, 242, 234), (252, 250, 246), (255, 255, 254)],
                 RED_EYES, [(136, 124, 108), (172, 160, 144), (206, 196, 182), (232, 224, 214), (250, 246, 240)]),
    "black": owl([(12, 10, 12), (24, 20, 24), (40, 34, 40), (62, 54, 62), (92, 84, 92), (134, 126, 134)],
                 [(10, 8, 10), (18, 16, 18), (32, 28, 32), (50, 44, 50), (76, 68, 76), (110, 102, 110), (160, 152, 160)],
                 RED_EYES, [(16, 14, 16), (30, 26, 30), (50, 44, 50), (78, 70, 78), (116, 108, 116)]),
    "red": owl([(70, 18, 14), (110, 30, 22), (150, 50, 34), (190, 80, 54), (222, 126, 94), (244, 186, 156)],
               [(56, 14, 12), (90, 24, 18), (130, 40, 28), (170, 66, 46), (204, 106, 80), (232, 162, 134), (250, 214, 194)],
               BLUE_EYES, [(80, 22, 16), (120, 38, 26), (160, 62, 42), (198, 100, 76), (232, 160, 132)]),
}


def paint(v=None):
    v = v or DEFAULT
    return Ow.paint(v["pal"], MOSS=v["moss"])


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide", "textures", "entity")
    for k, v in VARIANTS.items():
        H.write_png(os.path.join(tex_dir, f"{NAME}_{k}.png"), paint(v))
    print(f"{NAME}: {len(VARIANTS)} variants ({', '.join(VARIANTS)})")


if __name__ == "__main__":
    Ow.declare_parts()
    H.run(NAME, paint, Ow.build_model, Ow.animations, speak="hoot", head_center=(0, 22.5 + Ow.L), body_center=(0, 15.5 + Ow.L),
          face_views=[("idle", 0, 0, 0), ("hoot", 0.5, 0, 0), ("sleep", 1.0, 0, 0), ("idle", 3.5, 0, 0)])
    write_variants()
