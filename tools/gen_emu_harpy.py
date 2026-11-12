#!/usr/bin/env python3
"""Emu Harpy - the ostrich's geometry and animations (gen_ostrich_harpy.py) in a real emu's shaggy browns.

    python tools/gen_emu_harpy.py                 # write the three asset files and the variants
    python tools/gen_emu_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-sixth wave): the emu of the Australian plains (the Emberu is her Harmontide cousin): round, shaggy, long-legged, very fast and
very nosy. She lifts food out of open chests, and kicks whatever comes at her friend. Variants (emu_harpy_<variant>.png): grey, pale, red, black. Keep in sync with EmuHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_ostrich_harpy as Os  # noqa: E402

NAME = "emu_harpy"
Os.NAME = NAME                                     # the ostrich's model and animations, under her name

SKIN = [(190, 146, 124), (216, 176, 154), (234, 204, 182), (246, 224, 206)]
LEG = [(36, 34, 36), (60, 56, 58), (88, 84, 84), (120, 116, 114), (156, 152, 148)]
EYE = [(90, 50, 14), (140, 80, 20), (190, 116, 30), (226, 160, 60), (248, 208, 120)]
LEATHER = [(30, 24, 18), (54, 44, 32), (86, 70, 50), (120, 100, 74)]
CLOTH = [(70, 54, 40), (104, 84, 62), (140, 116, 88), (176, 150, 118)]
BEAD = [(150, 120, 60), (200, 170, 100), (236, 214, 150)]
NECK = [(70, 100, 150), (100, 134, 180), (136, 168, 208), (180, 204, 232)]


def emu(hair, fea, eye=EYE, cloth=CLOTH, neck=NECK):
    return Os.variant(hair=hair, skin=SKIN, fea=fea, eye=eye, leg=LEG, leather=LEATHER, cloth=cloth, accent=BEAD, shift=2, neck=neck)


DEFAULT = emu([(40, 30, 22), (70, 54, 40), (104, 82, 60), (140, 114, 86), (178, 152, 120), (216, 196, 168)],
              [(30, 22, 16), (54, 42, 30), (84, 66, 48), (118, 96, 72), (154, 130, 102), (194, 172, 144), (232, 218, 196)])

VARIANTS = {
    # grey: the dusty inland birds
    "grey": emu([(44, 44, 46), (72, 72, 76), (104, 104, 108), (140, 140, 144), (180, 180, 184), (220, 220, 222)],
                [(34, 34, 36), (58, 58, 62), (88, 88, 92), (122, 122, 126), (160, 160, 164), (200, 200, 204), (238, 238, 240)]),
    # pale: a leucistic bird, cream all over
    "pale": emu([(150, 136, 110), (186, 172, 146), (214, 202, 178), (234, 226, 206), (246, 242, 228), (254, 252, 244)],
                [(136, 122, 98), (172, 158, 132), (204, 192, 166), (226, 216, 196), (242, 236, 220), (250, 248, 238), (255, 254, 250)],
                cloth=[(120, 100, 76), (150, 130, 104), (184, 166, 140), (214, 200, 176)]),
    # red: stained by the red earth she dust-bathes in
    "red": emu([(70, 26, 14), (110, 42, 20), (150, 66, 32), (190, 100, 56), (222, 146, 96), (244, 196, 150)],
               [(54, 20, 12), (90, 34, 18), (130, 54, 28), (170, 84, 46), (206, 126, 80), (232, 176, 132), (250, 224, 196)]),
    # black: a dark young bird, nearly black
    "black": emu([(10, 10, 12), (22, 22, 24), (38, 36, 38), (58, 54, 56), (86, 80, 80), (124, 116, 112)],
                 [(8, 8, 10), (16, 16, 18), (30, 28, 30), (46, 44, 46), (70, 66, 66), (104, 98, 96), (150, 142, 136)]),
    # homage (2026-10-31): the Chocobo (Final Fantasy): all bright yellow, an orange beak and legs
    "golden_runner": emu([(150, 100, 10), (196, 140, 20), (232, 180, 40), (246, 206, 70), (252, 226, 120), (255, 244, 180)],
                         [(120, 80, 10), (170, 120, 16), (216, 164, 30), (240, 196, 56), (250, 216, 96), (254, 234, 150), (255, 248, 210)],
                         neck=[(214, 150, 30), (236, 180, 50), (248, 208, 90), (254, 232, 150)]),
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
