#!/usr/bin/env python3
"""Alkonost Harpy - the crow's geometry and animations (gen_crow_harpy.py), all soft white down.

    python tools/gen_alkonost_harpy.py                 # write the three asset files and the variants
    python tools/gen_alkonost_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-sixth wave): the bird of paradise of Russian tales, whose song makes you forget all sorrow. Fluffy white hair and wings,
a white ruff. Befriended, she sings: her friend mends, and the monsters round them nod off. Variants (alkonost_harpy_<variant>.png): dawn, rose, gold, lilac. Keep in sync with AlkonostHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import variant_tools as VT  # noqa: E402
import gen_crow_harpy as Cr  # noqa: E402

NAME = "alkonost_harpy"
Cr.NAME = NAME                                     # the crow's model and animations, under her name

SKIN = [(214, 176, 158), (236, 204, 188), (248, 226, 212), (254, 242, 232)]
LEG = [(170, 120, 40), (210, 160, 60), (236, 196, 90), (250, 224, 140), (255, 244, 200)]
BROWN = [(50, 34, 26), (84, 60, 44), (120, 90, 66), (156, 124, 94), (196, 168, 136)]
GOLD = [(190, 150, 70), (226, 192, 110), (250, 232, 170)]


def bird(hair, fea, cloth, ruff=None, eye=BROWN):
    return Cr.cvariant(hair=hair, fea=fea, eye=eye, leg=LEG, cloth=cloth, bead=GOLD, ruff=ruff or fea, shift=1, gain=0.8, skin=SKIN)


DEFAULT = bird([(176, 170, 156), (208, 202, 188), (230, 226, 214), (242, 240, 230), (250, 248, 242), (255, 254, 250)],
               [(170, 164, 150), (204, 198, 184), (226, 222, 210), (240, 238, 228), (248, 246, 240), (252, 252, 248), (255, 255, 254)],
               [(200, 194, 180), (222, 218, 206), (238, 236, 226), (248, 246, 240), (254, 252, 248)])

VARIANTS = {
    # dawn: the soft peach of first light
    "dawn": bird([(214, 150, 120), (232, 180, 150), (244, 206, 180), (250, 226, 206), (254, 240, 228), (255, 250, 244)],
                 [(206, 140, 112), (226, 170, 142), (240, 198, 172), (248, 220, 200), (252, 236, 222), (254, 246, 238), (255, 252, 248)],
                 [(216, 150, 120), (234, 182, 152), (246, 210, 186), (252, 230, 214), (255, 244, 234)]),
    # rose: pale pink, red at the tips
    "rose": bird([(206, 120, 140), (226, 154, 170), (240, 186, 198), (248, 212, 220), (252, 232, 236), (255, 246, 248)],
                 [(190, 96, 120), (214, 130, 150), (232, 166, 182), (244, 198, 208), (250, 222, 228), (254, 240, 244), (255, 250, 252)],
                 [(200, 80, 100), (224, 120, 136), (240, 160, 172), (250, 200, 208), (254, 232, 236)]),
    # gold: the paradise bird on the icons
    "gold": bird([(190, 140, 50), (220, 176, 80), (240, 208, 120), (248, 228, 166), (252, 242, 208), (255, 250, 236)],
                 [(176, 126, 40), (208, 162, 66), (232, 194, 104), (244, 218, 150), (250, 234, 194), (254, 246, 226), (255, 252, 242)],
                 [(170, 120, 40), (214, 166, 66), (240, 204, 110), (250, 228, 160), (255, 244, 210)]),
    # lilac: the evening song
    "lilac": bird([(150, 130, 196), (178, 160, 216), (204, 190, 232), (226, 214, 244), (242, 234, 250), (252, 248, 255)],
                  [(136, 116, 186), (166, 148, 208), (194, 178, 226), (216, 206, 240), (234, 226, 248), (246, 242, 252), (254, 252, 255)],
                  [(130, 106, 180), (164, 142, 206), (196, 178, 228), (224, 212, 244), (246, 240, 252)]),
}


def paint(v=None):
    return Cr.paint(v or DEFAULT)


if __name__ == "__main__":
    Cr.declare_parts()
    H.run(NAME, paint, Cr.build_model, Cr.animations, speak="caw", head_center=(0, 22.5 + Cr.L), body_center=(0, 15.5 + Cr.L),
          face_views=[("idle", 0, 0, 0), ("caw", 0.5, 0, 0), ("knock", 0.5, 0, 0), ("idle", 0, -28, 8)])
    VT.write_variants(NAME, "white", paint, VARIANTS, Cr.build_model, Cr.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + Cr.L))])
