#!/usr/bin/env python3
"""Mirror Crane Harpy - the flamingo's geometry and animations (gen_flamingo_harpy.py) in a Harmontide crane's colours.

    python tools/gen_mirror_crane_harpy.py                 # write the three asset files and the variants
    python tools/gen_mirror_crane_harpy.py --preview x.png # also render preview sheets

Field guide (eighteenth wave): a tall blue and white crane hung with gold and tassels, whose polished feathers turn
arrows aside. Navy flight feathers, a white bib, a steel-blue beak with a gold tip worn over her brow, a skirt of blue
and white feathers, long dark legs. Variants (mirror_crane_harpy_<variant>.png): white, grey, violet, gold. Keep in
sync with MirrorCraneHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_flamingo_harpy as Fl  # noqa: E402
from harpy_lib import rgb, ramp  # noqa: E402

NAME = "mirror_crane_harpy"
Fl.NAME = NAME                                     # the flamingo's model and animations, under her name

R = lambda *c: ramp([rgb(*x) for x in c])


def crane(hair, fea, flight, bib, skirt, beak, tip, leg, eye=None):
    """a palette set: the harpy palette, and the bib, skirt, flight feather and beak ramps the flamingo paints with"""
    p = Fl.variant_pal(hair, fea, eye)
    p.LEG = R(*leg)
    return dict(pal=p, bib=R(*bib), skirt=R(*skirt), flight=R(*flight), beak=(R(*beak), R(*tip)))


GOLD_TIP = [(150, 110, 30), (210, 166, 60), (244, 210, 120)]
DARK_LEG = [(30, 32, 40), (50, 54, 64), (74, 78, 90), (100, 104, 118), (130, 134, 148)]
DEFAULT = crane(
    [(20, 40, 110), (34, 66, 160), (60, 100, 200), (110, 150, 226), (176, 204, 244), (230, 240, 255)],
    [(18, 36, 100), (30, 60, 150), (52, 92, 190), (90, 136, 220), (150, 188, 240), (210, 228, 252), (246, 250, 255)],
    [(8, 14, 40), (14, 24, 64), (22, 38, 96), (34, 56, 130), (56, 84, 166), (96, 126, 200), (150, 176, 230)],
    [(226, 232, 244), (240, 244, 252), (248, 250, 255), (255, 255, 255)],
    [(40, 70, 160), (90, 130, 210), (190, 210, 244), (246, 250, 255)],
    [(90, 110, 140), (130, 150, 180), (170, 188, 214), (206, 220, 240)], GOLD_TIP, DARK_LEG,
    eye=[(120, 80, 10), (180, 130, 20), (230, 184, 50), (250, 222, 130), (255, 246, 210)])

VARIANTS = {
    # white: the red-crowned crane's black and white, a red crown in her hair
    "white": crane(
        [(120, 20, 20), (170, 30, 30), (210, 50, 44), (236, 236, 236), (248, 248, 248), (255, 255, 255)],
        [(200, 200, 204), (222, 222, 226), (236, 236, 240), (246, 246, 248), (252, 252, 252), (255, 255, 255), (255, 255, 255)],
        [(8, 8, 10), (16, 16, 20), (28, 28, 32), (44, 44, 50), (66, 66, 74), (100, 100, 110), (150, 150, 160)],
        [(236, 236, 238), (246, 246, 248), (252, 252, 252), (255, 255, 255)],
        [(20, 20, 24), (90, 90, 96), (230, 230, 232), (255, 255, 255)],
        [(150, 150, 140), (186, 186, 174), (214, 214, 202), (236, 236, 226)], [(20, 20, 24), (44, 44, 50), (70, 70, 78)], DARK_LEG),
    # grey: the common crane's ash grey, a black and white neck
    "grey": crane(
        [(40, 40, 46), (70, 70, 78), (110, 110, 120), (150, 150, 160), (196, 196, 204), (236, 236, 240)],
        [(70, 72, 80), (100, 102, 112), (130, 132, 142), (160, 162, 172), (190, 192, 200), (220, 222, 228), (244, 244, 248)],
        [(14, 14, 18), (26, 26, 32), (42, 42, 50), (62, 62, 72), (88, 88, 100), (120, 120, 134), (160, 160, 174)],
        [(200, 200, 206), (220, 220, 226), (236, 236, 240), (248, 248, 250)],
        [(60, 60, 68), (120, 120, 130), (200, 200, 208), (240, 240, 244)],
        [(110, 110, 104), (150, 150, 142), (186, 186, 176), (220, 220, 210)], GOLD_TIP, DARK_LEG),
    # violet: dusk blue and violet
    "violet": crane(
        [(50, 20, 100), (80, 40, 150), (120, 70, 196), (166, 120, 226), (210, 180, 246), (244, 232, 255)],
        [(46, 18, 90), (74, 36, 140), (110, 64, 186), (150, 108, 220), (194, 164, 240), (230, 214, 252), (250, 244, 255)],
        [(14, 6, 34), (24, 12, 56), (40, 22, 90), (60, 36, 124), (90, 60, 160), (130, 100, 196), (180, 156, 226)],
        [(236, 228, 246), (246, 240, 252), (252, 248, 255), (255, 255, 255)],
        [(80, 40, 150), (140, 100, 210), (210, 190, 244), (250, 246, 255)],
        [(110, 90, 140), (150, 130, 180), (186, 170, 214), (220, 208, 240)], GOLD_TIP, DARK_LEG),
    # gold: a temple crane in cream and gold
    "gold": crane(
        [(140, 96, 20), (190, 140, 40), (226, 184, 80), (244, 214, 140), (252, 236, 196), (255, 250, 236)],
        [(150, 106, 30), (196, 150, 50), (230, 190, 90), (246, 216, 146), (252, 234, 194), (255, 246, 228), (255, 252, 244)],
        [(70, 40, 10), (100, 60, 16), (136, 88, 24), (170, 120, 36), (204, 156, 60), (230, 196, 110), (248, 228, 170)],
        [(246, 238, 220), (250, 244, 232), (254, 250, 242), (255, 255, 250)],
        [(170, 120, 36), (220, 176, 80), (246, 226, 170), (255, 250, 236)],
        [(160, 140, 110), (196, 176, 140), (226, 206, 170), (246, 232, 204)], [(120, 30, 30), (170, 50, 40), (210, 90, 60)], DARK_LEG),
}


def paint(v=None):
    """the flamingo's painter with her skirt, flight feathers and jewellery recoloured (module globals there)"""
    v = v or DEFAULT
    Fl.LEAF, Fl.BLACK = v["skirt"], v["flight"]
    Fl.RING = ramp([rgb(196, 150, 50), rgb(232, 190, 80), rgb(250, 226, 150)])           # gold bracers and pendant
    return Fl.paint(v["pal"], v["bib"], v["beak"])


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "monstergirls", "textures", "entity")
    for k, v in VARIANTS.items():
        H.write_png(os.path.join(tex_dir, f"{NAME}_{k}.png"), paint(v))
    print(f"{NAME}: {len(VARIANTS)} variants ({', '.join(VARIANTS)})")


if __name__ == "__main__":
    Fl.declare_parts()
    H.run(NAME, paint, Fl.build_model, Fl.animations, speak="sing", head_center=(0, 23.5 + Fl.L), body_center=(0, 15.5 + Fl.L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("stand1", 2.0, 0, 0), ("idle", 0, -40, 8)])
    write_variants()
