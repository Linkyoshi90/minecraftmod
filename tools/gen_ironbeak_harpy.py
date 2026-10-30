#!/usr/bin/env python3
"""Ironbeak Harpy - the crow's geometry and animations (gen_crow_harpy.py) in a Harmontide courier bird's colours.

    python tools/gen_ironbeak_harpy.py                 # write the three asset files and the variants
    python tools/gen_ironbeak_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-third wave): a bright blue courier bird with silver talons, a blue scarf (the crow's ruff, in blue
cloth) and a satchel of letters at her hip (the crow's belt pouch). Once a day she brings her friend an explorer's map.
Variants (ironbeak_harpy_<variant>.png): slate, cardinal, jade, gold. Keep in sync with IronbeakHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import variant_tools as VT  # noqa: E402
import gen_crow_harpy as Cr  # noqa: E402

NAME = "ironbeak_harpy"
Cr.NAME = NAME                                     # the crow's model and animations, under her name

SKIN = [(190, 146, 132), (216, 178, 162), (236, 206, 190), (248, 226, 212)]
SILVER_LEGS = [(70, 76, 90), (110, 118, 134), (150, 158, 176), (190, 198, 214), (226, 232, 244)]
WARM_EYES = [(60, 30, 10), (110, 60, 20), (170, 100, 30), (220, 150, 60), (246, 200, 120)]
SILVER = [(110, 118, 134), (176, 184, 200), (232, 236, 246)]

DEFAULT = Cr.cvariant(
    hair=[(20, 40, 100), (36, 70, 160), (60, 110, 210), (100, 150, 236), (150, 190, 248), (210, 230, 255)],
    fea=[(14, 26, 70), (26, 48, 120), (44, 80, 180), (70, 120, 224), (110, 160, 244), (170, 206, 252), (226, 240, 255)],
    eye=WARM_EYES, leg=SILVER_LEGS,
    cloth=[(24, 44, 110), (40, 70, 160), (64, 104, 200), (100, 144, 230), (150, 188, 248)],
    bead=SILVER, ruff=[(30, 56, 130), (50, 86, 180), (80, 124, 220), (120, 164, 240), (170, 204, 250), (214, 232, 255), (240, 248, 255)],
    shift=1, gain=0.8, skin=SKIN)

VARIANTS = {
    # slate: the grey-blue of a rock dove carrying the post through the rain
    "slate": Cr.cvariant([(30, 34, 44), (54, 60, 76), (84, 94, 114), (120, 132, 154), (164, 176, 196), (210, 218, 232)],
                         [(20, 24, 32), (40, 46, 60), (66, 76, 96), (98, 110, 134), (136, 150, 174), (184, 196, 216), (230, 236, 246)],
                         WARM_EYES, SILVER_LEGS,
                         cloth=[(60, 20, 30), (100, 34, 46), (140, 54, 64), (180, 84, 90), (214, 130, 132)], bead=SILVER, shift=1, gain=0.8, skin=SKIN),
    # cardinal: scarlet with a black mask of a scarf
    "cardinal": Cr.cvariant([(90, 10, 14), (140, 20, 24), (190, 36, 36), (226, 70, 60), (246, 120, 104), (252, 180, 168)],
                            [(70, 6, 10), (110, 14, 18), (160, 26, 28), (204, 50, 44), (236, 90, 76), (248, 150, 136), (255, 214, 204)],
                            WARM_EYES, SILVER_LEGS,
                            cloth=[(14, 14, 18), (30, 30, 36), (50, 50, 58), (76, 76, 86), (110, 110, 122)], bead=SILVER, shift=1, gain=0.8, skin=SKIN),
    # jade: a green courier of the jungle routes
    "jade": Cr.cvariant([(14, 60, 40), (24, 96, 64), (40, 136, 92), (70, 176, 124), (120, 210, 166), (190, 240, 214)],
                        [(10, 44, 30), (18, 74, 50), (30, 110, 74), (50, 150, 102), (90, 190, 140), (150, 222, 190), (214, 246, 230)],
                        WARM_EYES, SILVER_LEGS,
                        cloth=[(110, 70, 14), (160, 110, 24), (204, 150, 40), (236, 190, 80), (250, 222, 140)], bead=SILVER, shift=1, gain=0.8, skin=SKIN),
    # gold: a golden courier with a royal blue scarf
    "gold": Cr.cvariant([(110, 74, 10), (160, 112, 20), (206, 152, 36), (236, 190, 70), (248, 222, 130), (254, 244, 200)],
                        [(80, 52, 6), (130, 90, 14), (180, 130, 28), (222, 170, 50), (244, 204, 100), (250, 230, 170), (255, 248, 226)],
                        [(20, 40, 100), (36, 70, 160), (60, 110, 210), (100, 150, 236), (170, 200, 250)], SILVER_LEGS,
                        cloth=[(20, 30, 90), (34, 52, 140), (54, 80, 190), (90, 120, 226), (140, 170, 246)], bead=SILVER, shift=1, gain=0.8, skin=SKIN),
}


def paint(v=None):
    return Cr.paint(v or DEFAULT)


if __name__ == "__main__":
    Cr.declare_parts()
    H.run(NAME, paint, Cr.build_model, Cr.animations, speak="caw", head_center=(0, 22.5 + Cr.L), body_center=(0, 15.5 + Cr.L),
          face_views=[("idle", 0, 0, 0), ("caw", 0.5, 0, 0), ("knock", 0.5, 0, 0), ("idle", 0, -28, 8)])
    VT.write_variants(NAME, "blue", paint, VARIANTS, Cr.build_model, Cr.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + Cr.L))])
