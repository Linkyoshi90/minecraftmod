#!/usr/bin/env python3
"""Emberu Harpy - the ostrich's geometry and animations (gen_ostrich_harpy.py) in a Harmontide emu's colours.

    python tools/gen_emberu_harpy.py                 # write the three asset files and the variants
    python tools/gen_emberu_harpy.py --preview x.png # also render preview sheets

Field guide (nineteenth wave): a tall blue emu of the red plains, her shaggy plumage painted with pale lines and dots,
turquoise beads and charms on cords, a pale blue bare neck. She kicks away whatever comes at her friend. Variants
(emberu_harpy_<variant>.png): ember, ochre, ash, dusk. Keep in sync with EmberuHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_ostrich_harpy as Os  # noqa: E402

NAME = "emberu_harpy"
Os.NAME = NAME                                     # the ostrich's model and animations, under her name

SKIN = [(196, 150, 130), (222, 180, 160), (238, 206, 186), (248, 226, 208)]
BEADS = [(30, 120, 140), (50, 180, 190), (130, 230, 230)]

DEFAULT = Os.variant(
    hair=[(18, 34, 90), (30, 56, 140), (50, 90, 190), (90, 136, 220), (150, 186, 240), (210, 228, 250)],
    skin=SKIN,
    fea=[(10, 20, 60), (20, 40, 100), (34, 64, 150), (56, 96, 196), (100, 140, 224), (170, 200, 244), (230, 240, 255)],
    eye=[(20, 60, 110), (40, 100, 170), (70, 150, 210), (120, 200, 240), (190, 236, 255)],
    leg=[(30, 36, 60), (50, 58, 90), (74, 84, 120), (104, 114, 150), (140, 150, 184)],
    leather=[(30, 24, 40), (56, 46, 70), (90, 76, 104), (130, 116, 146)],
    cloth=[(150, 170, 210), (190, 206, 236), (224, 234, 250), (248, 250, 255)],
    accent=BEADS, shift=2,
    neck=[(80, 120, 190), (120, 160, 220), (160, 196, 240), (200, 224, 252)])

VARIANTS = {
    # ember: red-orange plumage like the desert at sundown, white paint
    "ember": Os.variant(
        hair=[(90, 24, 10), (140, 44, 16), (190, 74, 26), (226, 116, 50), (244, 166, 100), (252, 214, 170)],
        skin=SKIN,
        fea=[(50, 12, 6), (90, 24, 10), (140, 46, 18), (186, 80, 32), (222, 124, 60), (240, 176, 120), (252, 230, 200)],
        eye=[(110, 60, 10), (170, 100, 20), (220, 150, 40), (244, 196, 90), (255, 236, 170)],
        leg=[(50, 34, 30), (78, 56, 48), (110, 82, 70), (146, 114, 98), (182, 150, 132)],
        leather=[(40, 20, 14), (70, 38, 26), (104, 62, 42), (140, 92, 64)],
        cloth=[(200, 190, 176), (226, 218, 206), (242, 236, 228), (252, 250, 246)],
        accent=[(150, 120, 30), (210, 176, 60), (246, 222, 130)], shift=2,
        neck=[(110, 140, 190), (146, 174, 214), (180, 204, 234), (212, 228, 248)]),
    # ochre: the earth pigments, yellow ochre and red
    "ochre": Os.variant(
        hair=[(80, 56, 14), (130, 94, 24), (180, 136, 40), (214, 176, 70), (236, 210, 120), (250, 236, 186)],
        skin=SKIN,
        fea=[(50, 34, 10), (86, 60, 18), (130, 94, 28), (174, 132, 44), (210, 170, 70), (234, 206, 128), (250, 238, 200)],
        eye=[(60, 30, 14), (100, 50, 20), (150, 80, 30), (196, 120, 50), (232, 170, 96)],
        leg=[(44, 36, 30), (70, 58, 48), (100, 86, 72), (136, 120, 102), (174, 158, 138)],
        leather=[(70, 24, 16), (110, 40, 24), (150, 62, 36), (190, 96, 60)],
        cloth=[(130, 40, 26), (170, 60, 36), (204, 90, 56), (230, 130, 90)],
        accent=[(240, 236, 226), (250, 248, 242), (255, 255, 255)], shift=2),
    # ash: the grey of a burnt plain after the fire, charcoal and white
    "ash": Os.variant(
        hair=[(30, 30, 34), (54, 54, 60), (84, 84, 92), (120, 120, 128), (164, 164, 172), (214, 214, 220)],
        skin=SKIN,
        fea=[(16, 16, 18), (34, 34, 38), (58, 58, 64), (90, 90, 98), (130, 130, 138), (184, 184, 190), (236, 236, 240)],
        eye=[(110, 40, 20), (170, 66, 26), (220, 100, 40), (244, 150, 80), (255, 210, 150)],
        leg=[(28, 28, 32), (48, 48, 54), (72, 72, 80), (100, 100, 110), (136, 136, 146)],
        leather=[(20, 20, 22), (40, 40, 44), (66, 66, 72), (100, 100, 108)],
        cloth=[(190, 190, 196), (216, 216, 222), (236, 236, 240), (250, 250, 252)],
        accent=[(170, 40, 30), (220, 70, 46), (246, 130, 96)], shift=2),
    # dusk: violet and night blue, star-white paint
    "dusk": Os.variant(
        hair=[(30, 16, 60), (54, 30, 100), (84, 52, 150), (124, 90, 196), (172, 140, 230), (222, 206, 250)],
        skin=SKIN,
        fea=[(16, 8, 36), (32, 18, 66), (54, 32, 110), (84, 56, 160), (124, 96, 204), (180, 160, 236), (236, 228, 255)],
        eye=[(120, 80, 10), (180, 130, 20), (230, 184, 50), (250, 222, 130), (255, 246, 210)],
        leg=[(26, 20, 44), (44, 36, 70), (66, 56, 100), (94, 84, 134), (128, 118, 170)],
        leather=[(24, 16, 36), (46, 32, 64), (74, 56, 100), (110, 90, 140)],
        cloth=[(190, 190, 220), (216, 216, 240), (236, 236, 250), (250, 250, 255)],
        accent=[(150, 120, 30), (210, 176, 60), (246, 222, 130)], shift=2,
        neck=[(90, 80, 170), (124, 116, 204), (164, 156, 232), (204, 198, 248)]),
}


def paint(v=None):
    return Os.paint(v or DEFAULT)


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "monstergirls", "textures", "entity")
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
