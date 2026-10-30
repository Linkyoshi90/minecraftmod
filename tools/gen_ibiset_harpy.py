#!/usr/bin/env python3
"""Ibiset Harpy - the flamingo's geometry and animations (gen_flamingo_harpy.py) in a Harmontide ibis's colours.

    python tools/gen_ibiset_harpy.py                 # write the three asset files and the variants
    python tools/gen_ibiset_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-fourth wave): a slim blue ibis hung with gold, a scribe of the old temples. Once a day she brings her
friend an enchanted book. Variants (ibiset_harpy_<variant>.png): sacred, scarlet, glossy, gold. Keep in sync with IbisetHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_flamingo_harpy as Fl  # noqa: E402
from harpy_lib import rgb, ramp  # noqa: E402

NAME = "ibiset_harpy"
Fl.NAME = NAME                                     # the flamingo's model and animations, under her name

R = lambda *c: ramp([rgb(*x) for x in c])


def ibis(hair, fea, flight, bib, skirt, beak, tip, leg, eye=None):
    """a palette set: the harpy palette, and the bib, skirt, flight feather and beak ramps the flamingo paints with"""
    p = Fl.variant_pal(hair, fea, eye)
    p.LEG = R(*leg)
    return dict(pal=p, bib=R(*bib), skirt=R(*skirt), flight=R(*flight), beak=(R(*beak), R(*tip)))


GOLD_TIP = [(40, 34, 30), (70, 60, 50), (110, 92, 70)]
DARK_LEG = [(30, 32, 40), (50, 54, 64), (74, 78, 90), (100, 104, 118), (130, 134, 148)]
AMBER = [(120, 80, 10), (180, 130, 20), (230, 184, 50), (250, 222, 130), (255, 246, 210)]
BEAK = [(40, 44, 54), (70, 76, 90), (110, 116, 130), (150, 156, 170)]
DEFAULT = ibis(
    [(16, 30, 90), (28, 54, 140), (46, 86, 186), (84, 130, 220), (140, 180, 240), (210, 228, 252)],
    [(14, 28, 80), (24, 48, 130), (40, 78, 176), (70, 120, 214), (120, 166, 236), (190, 216, 248), (240, 246, 255)],
    [(8, 12, 36), (12, 20, 56), (20, 34, 86), (30, 50, 120), (48, 76, 156), (84, 116, 192), (140, 170, 226)],
    [(150, 110, 30), (200, 156, 50), (236, 200, 90), (250, 228, 150)],
    [(30, 60, 150), (70, 110, 200), (230, 190, 90), (250, 228, 150)],
    BEAK, GOLD_TIP, DARK_LEG, eye=AMBER)

VARIANTS = {
    # sacred: the white and black of the sacred ibis of the Nile
    "sacred": ibis([(150, 150, 156), (190, 190, 196), (220, 220, 226), (240, 240, 244), (250, 250, 252), (255, 255, 255)],
                    [(140, 140, 148), (180, 180, 188), (214, 214, 220), (236, 236, 240), (248, 248, 250), (254, 254, 255), (255, 255, 255)],
                    [(8, 8, 10), (16, 16, 20), (26, 26, 32), (40, 40, 48), (60, 60, 70), (90, 90, 100), (130, 130, 140)],
                    [(200, 160, 60), (226, 190, 90), (244, 214, 130), (252, 236, 180)],
                    [(20, 20, 26), (60, 60, 70), (230, 190, 90), (252, 236, 180)], BEAK, GOLD_TIP, DARK_LEG, eye=AMBER),
    # scarlet: the scarlet ibis of the mangroves
    "scarlet": ibis([(110, 10, 16), (160, 24, 26), (206, 44, 40), (236, 84, 70), (248, 134, 116), (254, 196, 180)],
                     [(90, 8, 12), (140, 18, 20), (190, 34, 32), (226, 66, 56), (244, 110, 94), (250, 170, 154), (255, 224, 214)],
                     [(30, 4, 8), (50, 8, 12), (84, 12, 16), (120, 18, 22), (160, 30, 30), (200, 60, 54), (230, 110, 96)],
                     [(200, 160, 60), (226, 190, 90), (244, 214, 130), (252, 236, 180)],
                     [(120, 14, 20), (190, 40, 36), (230, 190, 90), (252, 236, 180)], BEAK, GOLD_TIP, DARK_LEG, eye=AMBER),
    # glossy: the bronze and green sheen of the glossy ibis
    "glossy": ibis([(40, 22, 20), (70, 38, 30), (104, 60, 40), (140, 90, 56), (176, 130, 84), (214, 184, 136)],
                    [(30, 18, 18), (56, 32, 28), (88, 52, 38), (124, 80, 50), (70, 120, 90), (120, 170, 130), (190, 220, 180)],
                    [(10, 16, 14), (16, 28, 24), (24, 44, 36), (36, 66, 52), (54, 96, 74), (84, 130, 100), (130, 170, 140)],
                    [(200, 160, 60), (226, 190, 90), (244, 214, 130), (252, 236, 180)],
                    [(60, 34, 26), (110, 70, 44), (230, 190, 90), (252, 236, 180)], BEAK, GOLD_TIP, DARK_LEG, eye=AMBER),
    # gold: a scribe in the temple's gold
    "gold": ibis([(110, 74, 10), (160, 112, 20), (206, 152, 36), (236, 190, 70), (248, 222, 130), (254, 244, 200)],
                  [(80, 52, 6), (130, 90, 14), (180, 130, 28), (222, 170, 50), (244, 204, 100), (250, 230, 170), (255, 248, 226)],
                  [(50, 34, 6), (84, 56, 10), (124, 86, 16), (166, 120, 24), (206, 156, 40), (232, 192, 80), (248, 226, 140)],
                  [(20, 40, 110), (40, 70, 160), (80, 120, 210), (140, 176, 240)],
                  [(30, 60, 150), (70, 110, 200), (230, 190, 90), (250, 228, 150)], BEAK, GOLD_TIP, DARK_LEG, eye=[(20, 40, 100), (36, 70, 160), (60, 110, 210), (100, 150, 236), (170, 200, 250)]),
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
