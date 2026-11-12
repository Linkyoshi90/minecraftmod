#!/usr/bin/env python3
"""Stigowl Harpy - the owl's geometry and animations (gen_owl_harpy.py) in a Harmontide owl's colours.

    python tools/gen_stigowl_harpy.py                 # write the three asset files and the variants
    python tools/gen_stigowl_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-fourth wave): a dark, ornate owl with a pale heart-shaped face and silver-blue barring, who sees through any
darkness. Befriended, she clears Darkness and Blindness from her friend. Variants (stigowl_harpy_<variant>.png): ash, barn, snow,
violet. Keep in sync with StigowlHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_owl_harpy as Ow  # noqa: E402
from harpy_lib import rgb, ramp  # noqa: E402

NAME = "stigowl_harpy"
Ow.NAME = NAME                                     # the owl's model and animations, under her name

R = lambda *c: ramp([rgb(*x) for x in c])
LEG = R((20, 20, 26), (40, 40, 50), (66, 66, 80), (100, 100, 116), (140, 140, 156))
BLUE_EYES = R((20, 50, 110), (34, 80, 160), (60, 120, 210), (120, 176, 240), (200, 226, 255))
AMBER = R((120, 80, 10), (180, 130, 20), (230, 184, 50), (250, 222, 130), (255, 246, 210))


def owl(hair, fea, eye, moss):
    return dict(pal=Ow.variant_pal(R(*hair), R(*fea), eye, LEG), moss=R(*moss))


DEFAULT = owl([(14, 16, 26), (26, 30, 46), (44, 50, 74), (76, 84, 112), (130, 138, 170), (200, 206, 228)],
              [(12, 14, 24), (24, 28, 44), (40, 46, 70), (66, 74, 104), (104, 112, 146), (160, 168, 200), (222, 226, 242)],
              BLUE_EYES, [(20, 24, 40), (34, 40, 64), (60, 70, 104), (96, 108, 146), (150, 160, 196)])

VARIANTS = {
    "ash": owl([(30, 30, 32), (54, 54, 58), (84, 84, 90), (120, 120, 128), (164, 164, 172), (214, 214, 220)],
               [(26, 26, 28), (46, 46, 50), (72, 72, 78), (104, 104, 112), (144, 144, 152), (190, 190, 198), (234, 234, 240)],
               AMBER, [(30, 30, 34), (56, 56, 62), (88, 88, 96), (124, 124, 134), (170, 170, 180)]),
    "barn": owl([(70, 44, 20), (110, 72, 34), (156, 110, 56), (198, 154, 92), (230, 200, 146), (250, 236, 208)],
                [(60, 38, 18), (98, 64, 30), (140, 98, 50), (184, 140, 80), (220, 186, 130), (244, 224, 186), (254, 246, 230)],
                R((16, 10, 8), (30, 20, 14), (46, 30, 22), (66, 44, 30), (92, 64, 44)), [(90, 58, 26), (130, 90, 44), (170, 126, 70), (208, 170, 110), (238, 214, 170)]),
    "snow": owl([(150, 156, 170), (190, 196, 208), (220, 226, 236), (238, 242, 248), (250, 252, 254), (255, 255, 255)],
                [(140, 146, 160), (180, 186, 198), (212, 218, 228), (234, 238, 244), (246, 248, 252), (254, 254, 255), (255, 255, 255)],
                AMBER, [(120, 128, 144), (160, 168, 184), (196, 204, 216), (226, 232, 240), (248, 250, 252)]),
    "violet": owl([(26, 14, 44), (46, 26, 76), (74, 44, 116), (112, 76, 160), (160, 124, 204), (216, 192, 240)],
                  [(22, 12, 38), (40, 22, 68), (64, 38, 104), (96, 64, 146), (136, 104, 186), (184, 160, 222), (232, 222, 248)],
                  BLUE_EYES, [(36, 20, 60), (60, 36, 96), (92, 62, 136), (130, 100, 176), (178, 152, 214)]),
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
