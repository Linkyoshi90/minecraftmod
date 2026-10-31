#!/usr/bin/env python3
"""Roc Harpy - the crow's geometry and animations (gen_crow_harpy.py), drawn four blocks tall (MonsterGirlsClient scales her).

    python tools/gen_roc_harpy.py                 # write the three asset files and the variants
    python tools/gen_roc_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-sixth wave): the great bird of the Arabian sailors' tales, four blocks tall, big enough to carry off an elephant. Storm-blue feathers,
a pale ruff. Befriended, she snatches a monster near her friend, climbs and lets go. Variants (roc_harpy_<variant>.png): tawny, snow, ember, night. Keep in sync with RocHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import variant_tools as VT  # noqa: E402
import gen_crow_harpy as Cr  # noqa: E402

NAME = "roc_harpy"
Cr.NAME = NAME                                     # the crow's model and animations, under her name

SKIN = [(196, 150, 132), (222, 182, 164), (240, 208, 190), (250, 228, 212)]
TALON = [(40, 70, 70), (70, 120, 116), (110, 170, 160), (160, 214, 200), (210, 240, 232)]
AMBER = [(110, 70, 10), (170, 110, 20), (220, 160, 40), (246, 206, 100), (255, 240, 190)]
BLUE = [(30, 60, 150), (50, 100, 200), (80, 140, 230), (120, 180, 246), (190, 226, 255)]
GOLD = [(150, 110, 30), (210, 166, 60), (244, 210, 120)]


def bird(hair, fea, ruff, cloth, eye=BLUE):
    return Cr.cvariant(hair=hair, fea=fea, eye=eye, leg=TALON, cloth=cloth, bead=GOLD, ruff=ruff, shift=1, gain=0.8, skin=SKIN)


DEFAULT = bird([(26, 44, 90), (44, 74, 136), (70, 110, 176), (108, 150, 206), (156, 192, 232), (214, 232, 248)],
               [(20, 34, 76), (36, 60, 118), (58, 94, 160), (90, 132, 196), (130, 170, 220), (180, 208, 238), (232, 242, 252)],
               [(70, 100, 140), (110, 140, 178), (150, 178, 208), (186, 208, 230), (214, 228, 242), (234, 242, 250), (250, 252, 255)],
               [(30, 40, 70), (50, 66, 106), (74, 96, 146), (110, 134, 184), (156, 178, 218)])

VARIANTS = {
    # tawny: the eagle's brown and gold
    "tawny": bird([(70, 42, 16), (110, 70, 28), (156, 106, 44), (198, 150, 76), (230, 196, 128), (248, 232, 196)],
                  [(50, 30, 12), (84, 52, 22), (126, 84, 36), (170, 124, 60), (208, 168, 100), (236, 208, 156), (250, 240, 214)],
                  [(120, 90, 50), (160, 126, 80), (196, 164, 116), (222, 196, 154), (238, 220, 186), (248, 238, 214), (254, 250, 240)],
                  [(60, 30, 20), (90, 50, 30), (130, 74, 44), (170, 106, 66), (206, 146, 100)], AMBER),
    # snow: the white roc of the far mountains
    "snow": bird([(140, 150, 166), (180, 188, 202), (214, 220, 230), (234, 238, 244), (248, 250, 252), (255, 255, 255)],
                 [(120, 130, 148), (160, 170, 186), (196, 204, 216), (224, 230, 238), (242, 244, 248), (252, 252, 254), (255, 255, 255)],
                 [(170, 178, 190), (200, 206, 216), (224, 228, 236), (240, 242, 246), (248, 250, 252), (254, 254, 255), (255, 255, 255)],
                 [(60, 80, 120), (90, 116, 160), (130, 156, 196), (170, 194, 224), (210, 226, 244)]),
    # ember: red as the sunset over the Indian Ocean
    "ember": bird([(90, 20, 14), (140, 36, 20), (190, 64, 30), (226, 110, 50), (246, 170, 100), (252, 222, 170)],
                  [(70, 14, 12), (116, 28, 16), (166, 50, 24), (210, 90, 40), (236, 140, 76), (248, 196, 136), (254, 236, 206)],
                  [(150, 90, 50), (190, 128, 76), (222, 166, 110), (240, 200, 150), (248, 222, 186), (252, 238, 214), (255, 250, 238)],
                  [(50, 20, 20), (80, 34, 30), (120, 54, 44), (160, 84, 66), (200, 124, 100)], AMBER),
    # night: blue-black, with a silver ruff
    "night": bird([(10, 12, 24), (20, 24, 42), (34, 40, 66), (54, 62, 96), (84, 94, 134), (130, 140, 180)],
                  [(8, 10, 20), (16, 20, 36), (28, 34, 56), (44, 52, 82), (70, 80, 116), (110, 120, 156), (170, 178, 208)],
                  [(90, 96, 112), (130, 136, 152), (168, 174, 188), (200, 204, 216), (222, 226, 234), (240, 242, 246), (252, 252, 254)],
                  [(14, 16, 28), (26, 30, 48), (44, 50, 76), (70, 78, 110), (110, 118, 150)], AMBER),
}


def paint(v=None):
    return Cr.paint(v or DEFAULT)


if __name__ == "__main__":
    Cr.declare_parts()
    H.run(NAME, paint, Cr.build_model, Cr.animations, speak="caw", head_center=(0, 22.5 + Cr.L), body_center=(0, 15.5 + Cr.L),
          face_views=[("idle", 0, 0, 0), ("caw", 0.5, 0, 0), ("knock", 0.5, 0, 0), ("idle", 0, -28, 8)])
    VT.write_variants(NAME, "storm", paint, VARIANTS, Cr.build_model, Cr.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + Cr.L))])
