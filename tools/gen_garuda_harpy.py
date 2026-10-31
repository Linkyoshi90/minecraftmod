#!/usr/bin/env python3
"""Garuda Harpy - the crow's geometry and animations (gen_crow_harpy.py) with sun-orange skin, blue wings and gold.

    python tools/gen_garuda_harpy.py                 # write the three asset files and the variants
    python tools/gen_garuda_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-sixth wave): the king of birds of the Hindu and Buddhist epics, enemy of all serpents. Orange skin, blue hair and
wings, a gold dress and crown. No poison holds on her or her friend, and she strikes down serpents on sight. Variants (garuda_harpy_<variant>.png): crimson, emerald, white, night. Keep in sync with GarudaHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import variant_tools as VT  # noqa: E402
import gen_crow_harpy as Cr  # noqa: E402

NAME = "garuda_harpy"
Cr.NAME = NAME                                     # the crow's model and animations, under her name

SUN_SKIN = [(176, 92, 30), (214, 124, 44), (238, 156, 66), (250, 190, 104)]
TALON = [(150, 100, 20), (200, 140, 30), (236, 184, 50), (250, 216, 100), (255, 240, 170)]
AMBER = [(70, 36, 10), (120, 66, 20), (170, 100, 30), (216, 140, 50), (244, 190, 100)]
GOLD_CLOTH = [(170, 110, 20), (216, 150, 30), (244, 190, 60), (250, 216, 110), (255, 238, 170)]
GOLD = [(170, 120, 30), (226, 176, 60), (252, 226, 130)]


def bird(hair, fea, skin=SUN_SKIN, cloth=GOLD_CLOTH, bead=GOLD):
    return Cr.cvariant(hair=hair, fea=fea, eye=AMBER, leg=TALON, cloth=cloth, bead=bead, shift=1, gain=0.8, skin=skin)


DEFAULT = bird([(26, 60, 110), (40, 90, 150), (60, 124, 190), (96, 160, 220), (150, 200, 240), (214, 236, 252)],
               [(20, 46, 90), (34, 72, 130), (52, 104, 170), (80, 140, 206), (120, 176, 232), (176, 214, 246), (232, 244, 254)])

VARIANTS = {
    # crimson: red wings, as the Thai royal emblem paints her
    "crimson": bird([(110, 14, 18), (160, 26, 26), (206, 46, 40), (236, 90, 70), (248, 150, 124), (254, 208, 190)],
                    [(90, 10, 14), (140, 20, 22), (190, 38, 34), (224, 70, 56), (242, 120, 98), (250, 180, 156), (255, 230, 216)]),
    # emerald: green, for the emerald Garuda swallowed and spat out
    "emerald": bird([(10, 70, 50), (20, 110, 76), (36, 150, 104), (70, 190, 136), (130, 224, 180), (206, 246, 226)],
                    [(8, 54, 40), (16, 90, 62), (28, 130, 88), (50, 170, 118), (96, 206, 156), (160, 232, 200), (226, 250, 238)]),
    # white: pale skin and white wings, the temple carving
    "white": bird([(170, 170, 176), (204, 204, 210), (228, 228, 232), (242, 242, 246), (250, 250, 252), (255, 255, 255)],
                  [(150, 150, 158), (190, 190, 198), (218, 218, 224), (236, 236, 240), (246, 246, 250), (252, 252, 254), (255, 255, 255)],
                  skin=[(200, 160, 120), (226, 192, 150), (242, 216, 180), (252, 236, 208)]),
    # night: dark wings, a silver dress
    "night": bird([(14, 16, 40), (24, 28, 66), (40, 46, 100), (66, 74, 140), (110, 118, 184), (170, 178, 224)],
                  [(10, 12, 32), (18, 22, 54), (32, 38, 84), (52, 60, 120), (84, 94, 160), (130, 140, 200), (190, 196, 236)],
                  cloth=[(110, 116, 134), (150, 158, 176), (190, 198, 214), (220, 226, 238), (244, 246, 252)],
                  bead=[(110, 118, 134), (176, 184, 200), (232, 236, 246)]),
}


def paint(v=None):
    return Cr.paint(v or DEFAULT)


if __name__ == "__main__":
    Cr.declare_parts()
    H.run(NAME, paint, Cr.build_model, Cr.animations, speak="caw", head_center=(0, 22.5 + Cr.L), body_center=(0, 15.5 + Cr.L),
          face_views=[("idle", 0, 0, 0), ("caw", 0.5, 0, 0), ("knock", 0.5, 0, 0), ("idle", 0, -28, 8)])
    VT.write_variants(NAME, "sun", paint, VARIANTS, Cr.build_model, Cr.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + Cr.L))])
