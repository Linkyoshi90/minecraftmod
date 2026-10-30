#!/usr/bin/env python3
"""Yatagarasu Harpy - the crow's geometry and animations (gen_crow_harpy.py) with a pale blue kimono's colours.

    python tools/gen_yatagarasu_harpy.py                 # write the three asset files and the variants
    python tools/gen_yatagarasu_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-fifth wave): the sun's crow of Japan, who guided the first emperor: black wings and hair, a pale blue kimono, gold
bells. At dawn she gives her friend Regeneration and Speed. Variants (yatagarasu_harpy_<variant>.png): sun, shrine, night, white. Keep in
sync with YatagarasuHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import variant_tools as VT  # noqa: E402
import gen_crow_harpy as Cr  # noqa: E402

NAME = "yatagarasu_harpy"
Cr.NAME = NAME                                     # the crow's model and animations, under her name

SKIN = [(200, 156, 142), (226, 188, 172), (244, 214, 198), (252, 234, 220)]
BLACK_LEGS = [(14, 14, 18), (30, 30, 36), (50, 50, 58), (76, 76, 86), (110, 110, 122)]
SUN_EYES = [(120, 70, 10), (180, 110, 20), (230, 160, 40), (250, 210, 100), (255, 240, 190)]
BLUE_EYES = [(20, 50, 110), (34, 80, 160), (60, 120, 210), (120, 176, 240), (200, 226, 255)]
GOLD = [(150, 110, 30), (210, 166, 60), (244, 210, 120)]
BLACK_HAIR = [(10, 10, 14), (20, 20, 26), (34, 34, 42), (52, 52, 62), (80, 80, 92), (120, 120, 134)]
BLACK_FEA = [(8, 8, 12), (16, 16, 22), (28, 28, 36), (44, 44, 54), (68, 68, 80), (100, 100, 114), (150, 150, 164)]


def bird(cloth, ruff, eye=BLUE_EYES, hair=BLACK_HAIR, fea=BLACK_FEA):
    return Cr.cvariant(hair=hair, fea=fea, eye=eye, leg=BLACK_LEGS, cloth=cloth, bead=GOLD, ruff=ruff, shift=1, gain=0.8, skin=SKIN)


DEFAULT = bird([(120, 160, 210), (150, 186, 228), (180, 210, 240), (210, 230, 250), (236, 244, 254)],
               [(120, 160, 210), (150, 186, 228), (180, 210, 240), (210, 230, 250), (226, 238, 252), (240, 246, 254), (252, 254, 255)])

VARIANTS = {
    # sun: white and gold, the crow of the sun herself
    "sun": bird([(200, 150, 40), (226, 184, 70), (244, 214, 120), (250, 234, 170), (254, 246, 214)],
                [(200, 150, 40), (226, 184, 70), (244, 214, 120), (250, 234, 170), (252, 240, 196), (254, 246, 220), (255, 252, 240)], SUN_EYES),
    # shrine: a shrine maiden's red and white
    "shrine": bird([(150, 20, 24), (190, 36, 36), (220, 60, 54), (240, 100, 90), (250, 150, 140)],
                   [(220, 216, 216), (232, 230, 230), (242, 240, 240), (248, 248, 248), (252, 252, 252), (254, 254, 254), (255, 255, 255)]),
    # night: indigo, starlit
    "night": bird([(20, 24, 70), (34, 40, 100), (54, 62, 140), (84, 94, 180), (130, 140, 220)],
                  [(20, 24, 70), (34, 40, 100), (54, 62, 140), (84, 94, 180), (130, 140, 220), (170, 180, 240), (214, 220, 250)]),
    # white: a pale crow, an omen in itself
    "white": bird([(120, 160, 210), (150, 186, 228), (180, 210, 240), (210, 230, 250), (236, 244, 254)],
                  [(120, 160, 210), (150, 186, 228), (180, 210, 240), (210, 230, 250), (226, 238, 252), (240, 246, 254), (252, 254, 255)], SUN_EYES,
                  [(150, 150, 156), (190, 190, 196), (220, 220, 226), (238, 238, 242), (250, 250, 252), (255, 255, 255)],
                  [(140, 140, 148), (180, 180, 188), (214, 214, 220), (236, 236, 240), (246, 246, 250), (252, 252, 254), (255, 255, 255)]),
}


def paint(v=None):
    return Cr.paint(v or DEFAULT)


if __name__ == "__main__":
    Cr.declare_parts()
    H.run(NAME, paint, Cr.build_model, Cr.animations, speak="caw", head_center=(0, 22.5 + Cr.L), body_center=(0, 15.5 + Cr.L),
          face_views=[("idle", 0, 0, 0), ("caw", 0.5, 0, 0), ("knock", 0.5, 0, 0), ("idle", 0, -28, 8)])
    VT.write_variants(NAME, "black", paint, VARIANTS, Cr.build_model, Cr.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + Cr.L))])
