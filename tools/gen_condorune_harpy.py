#!/usr/bin/env python3
"""Condorune Harpy - the crow harpy's rig and animations (gen_crow_harpy.py) in condor colours: black plumage, a white
ruff round her neck, white-edged wings and a pale, bare-looking face.

    python tools/gen_condorune_harpy.py              # write the three asset files and the variants
    python tools/gen_condorune_harpy.py --preview x  # also render preview sheets

Field guide (sixteenth wave): a Harmontide condor who rides the mountain updrafts. Befriended, she catches her friend
when they fall. Variants (condorune_harpy_<variant>.png): california, king, griffon, bearded. Keep in sync with
CondoruneHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_crow_harpy as CR  # noqa: E402
import variant_tools as VT  # noqa: E402

NAME = "condorune_harpy"
CR.NAME = NAME                                   # the crow's model and animations, under her name
L = CR.L

BLACK_HAIR = [(10, 10, 12), (24, 24, 28), (40, 40, 46), (62, 62, 70), (92, 92, 102), (130, 130, 142)]
BLACK_FEA = [(6, 6, 8), (16, 16, 20), (30, 30, 36), (48, 48, 56), (150, 150, 156), (214, 214, 218), (246, 246, 248)]   # white edges
WHITE_RUFF = CR.WHITE_RUFF
RED_EYES = [(90, 20, 20), (140, 30, 26), (190, 50, 36), (220, 90, 60), (240, 140, 110)]
GREY_LEGS = [(60, 56, 54), (90, 86, 82), (124, 118, 112), (160, 154, 146), (196, 190, 182)]
BONE_CLOTH = [(60, 52, 44), (100, 88, 74), (144, 128, 108), (190, 174, 150), (226, 214, 192)]
TURQUOISE = [(30, 100, 100), (50, 150, 146), (110, 206, 196)]
SKIN = [(196, 150, 140), (222, 182, 168), (240, 208, 194), (250, 228, 216)]

DEFAULT = CR.cvariant(BLACK_HAIR, BLACK_FEA, RED_EYES, GREY_LEGS, BONE_CLOTH, TURQUOISE, ruff=WHITE_RUFF, skin=SKIN)
VARIANTS = {
    # California condor: black with an orange-pink face, grey ruff
    "california": CR.cvariant(BLACK_HAIR, BLACK_FEA, [(110, 40, 14), (170, 70, 20), (220, 110, 40), (240, 150, 80), (250, 200, 150)], GREY_LEGS,
                              BONE_CLOTH, [(150, 66, 20), (206, 100, 30), (240, 150, 70)], ruff=CR.GREY_RUFF, skin=SKIN),
    # king vulture: cream and black, orange-and-purple accents
    "king": CR.cvariant([(200, 190, 170), (220, 210, 192), (236, 228, 212), (246, 240, 228), (252, 248, 240), (255, 254, 250)],
                        [(12, 12, 14), (26, 26, 30), (60, 56, 52), (150, 140, 124), (210, 200, 182), (236, 228, 212), (252, 248, 240)],
                        [(150, 40, 30), (200, 70, 40), (240, 120, 60), (250, 170, 110), (255, 220, 180)], GREY_LEGS,
                        [(70, 30, 80), (110, 50, 120), (156, 86, 166), (196, 136, 204), (226, 190, 232)], [(200, 100, 20), (240, 150, 40), (252, 200, 100)],
                        ruff=CR.GREY_RUFF, skin=SKIN),
    # griffon vulture: sandy brown with a white ruff
    "griffon": CR.cvariant([(90, 66, 44), (124, 94, 64), (160, 126, 90), (192, 160, 120), (220, 194, 160), (240, 224, 200)],
                           [(30, 22, 16), (60, 44, 30), (100, 76, 52), (140, 110, 80), (180, 150, 116), (214, 190, 160), (240, 228, 210)],
                           [(60, 40, 24), (100, 70, 40), (140, 104, 64), (180, 144, 100), (214, 186, 150)], GREY_LEGS, BONE_CLOTH, TURQUOISE,
                           ruff=WHITE_RUFF, skin=SKIN),
    # bearded vulture: rust-orange breast, slate wings
    "bearded": CR.cvariant([(140, 70, 30), (180, 96, 40), (214, 128, 60), (236, 162, 96), (248, 200, 150), (255, 230, 200)],
                           [(20, 22, 28), (40, 44, 54), (64, 70, 84), (92, 100, 118), (130, 138, 156), (176, 182, 198), (220, 224, 234)],
                           [(140, 20, 20), (190, 40, 30), (230, 80, 50), (246, 140, 100), (252, 200, 170)], GREY_LEGS,
                           [(70, 40, 24), (110, 66, 40), (150, 96, 60), (190, 136, 96), (222, 180, 140)], [(150, 66, 20), (206, 100, 30), (240, 150, 70)],
                           ruff=[(120, 60, 24), (160, 86, 36), (200, 118, 56), (228, 152, 90), (244, 188, 134), (252, 220, 180), (255, 242, 222)], skin=SKIN),
}
CR.DEFAULT = DEFAULT

if __name__ == "__main__":
    CR.declare_parts()
    H.run(NAME, CR.paint, CR.build_model, CR.animations, speak="caw", head_center=(0, 22.5 + L), body_center=(0, 15.5 + L),
          face_views=[("idle", 0, 0, 0), ("caw", 0.5, 0, 0), ("idle", 0, -28, 8), ("fly", 0.4, -20, 8)])
    VT.write_variants(NAME, "andean", CR.paint, VARIANTS, CR.build_model, CR.animations,
                      rows=[("idle", 0, -20, 6, 10, None), ("fly", 0.4, -30, 10, 10, None), ("idle", 0, -15, 0, 28, (0, 22.5 + L))])
