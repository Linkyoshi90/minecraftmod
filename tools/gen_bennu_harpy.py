#!/usr/bin/env python3
"""Bennu Harpy - the flamingo's geometry and animations (gen_flamingo_harpy.py) in a heron's blue.

    python tools/gen_bennu_harpy.py                 # write the three asset files and the variants
    python tools/gen_bennu_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-sixth wave): the sacred heron of Heliopolis in Egypt, who rose with the first sun and rises again each day. Slate-blue
plumage, a white bib, a long yellow-pink beak over her brow. At sunrise she heals her friend, and once a day she comes back from death. Variants (bennu_harpy_<variant>.png): grey, gold, white, ember. Keep in sync with BennuHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_flamingo_harpy as Fl  # noqa: E402
from harpy_lib import rgb, ramp  # noqa: E402

NAME = "bennu_harpy"
Fl.NAME = NAME                                     # the flamingo's model and animations, under her name

R = lambda *c: ramp([rgb(*x) for x in c])


def heron(hair, fea, flight, bib, skirt, eye=None):
    """a palette set: the harpy palette, and the bib, skirt, flight feather and beak ramps the flamingo paints with"""
    p = Fl.variant_pal(hair, fea, eye)
    p.LEG = R(*LEG)
    return dict(pal=p, bib=R(*bib), skirt=R(*skirt), flight=R(*flight), beak=(R(*BEAK), R(*TIP)))


LEG = [(190, 130, 110), (214, 160, 140), (232, 190, 170), (244, 214, 198), (252, 234, 224)]
BEAK = [(216, 150, 70), (236, 184, 100), (248, 212, 140), (254, 234, 184)]
TIP = [(226, 120, 90), (240, 150, 116), (250, 186, 150)]
AMBER = [(120, 80, 10), (180, 130, 20), (230, 184, 50), (250, 222, 130), (255, 246, 210)]
WHITE_BIB = [(216, 224, 236), (232, 238, 246), (244, 248, 252), (255, 255, 255)]
DEFAULT = heron(
    [(30, 60, 110), (50, 90, 150), (76, 124, 186), (116, 164, 216), (170, 206, 240), (226, 240, 252)],
    [(26, 50, 96), (42, 76, 134), (62, 106, 170), (92, 142, 204), (136, 182, 230), (194, 222, 246), (240, 248, 254)],
    [(12, 24, 54), (20, 38, 80), (32, 58, 112), (50, 84, 146), (76, 116, 180), (116, 156, 212), (170, 200, 238)],
    WHITE_BIB, [(30, 60, 110), (76, 124, 186), (186, 212, 240), (244, 248, 252)], eye=AMBER)

VARIANTS = {
    # grey: the grey heron of the Nile's banks
    "grey": heron([(50, 54, 62), (80, 84, 94), (116, 120, 130), (154, 158, 168), (198, 200, 208), (238, 238, 242)],
                  [(44, 48, 56), (72, 76, 86), (106, 110, 120), (142, 146, 156), (182, 186, 194), (218, 220, 226), (246, 246, 248)],
                  [(14, 14, 18), (26, 26, 32), (42, 42, 50), (62, 62, 72), (88, 88, 100), (120, 120, 134), (160, 160, 174)],
                  WHITE_BIB, [(60, 60, 68), (120, 120, 130), (200, 200, 208), (240, 240, 244)], eye=AMBER),
    # gold: the sun's bird on the temple walls
    "gold": heron([(120, 80, 14), (170, 120, 24), (214, 164, 44), (240, 200, 90), (250, 228, 150), (255, 246, 214)],
                  [(100, 66, 10), (150, 104, 20), (196, 146, 36), (230, 186, 70), (246, 216, 124), (252, 236, 186), (255, 250, 232)],
                  [(50, 34, 6), (84, 56, 10), (124, 86, 16), (166, 120, 24), (206, 156, 40), (232, 192, 80), (248, 226, 140)],
                  [(240, 226, 190), (248, 238, 210), (252, 246, 228), (255, 254, 246)], [(170, 120, 36), (220, 176, 80), (246, 226, 170), (255, 250, 236)], eye=AMBER),
    # white: the great egret
    "white": heron([(160, 164, 172), (196, 200, 206), (224, 226, 232), (240, 242, 246), (250, 250, 252), (255, 255, 255)],
                   [(150, 154, 162), (188, 192, 198), (216, 220, 226), (236, 238, 242), (246, 248, 250), (252, 252, 254), (255, 255, 255)],
                   [(120, 124, 134), (150, 154, 164), (180, 184, 194), (206, 210, 218), (226, 228, 234), (242, 244, 248), (252, 252, 254)],
                   WHITE_BIB, [(180, 184, 194), (214, 216, 224), (240, 242, 246), (255, 255, 255)], eye=AMBER),
    # ember: the fire she rises from
    "ember": heron([(120, 26, 14), (170, 48, 20), (214, 84, 30), (240, 130, 50), (250, 186, 100), (255, 232, 170)],
                   [(100, 20, 12), (150, 38, 18), (196, 68, 26), (230, 110, 40), (246, 160, 76), (252, 208, 136), (255, 242, 200)],
                   [(40, 10, 8), (70, 18, 12), (110, 30, 16), (150, 48, 22), (190, 76, 30), (224, 116, 48), (244, 170, 90)],
                   [(250, 216, 150), (252, 230, 180), (254, 242, 212), (255, 252, 240)], [(150, 40, 20), (214, 84, 30), (250, 186, 100), (255, 240, 200)], eye=AMBER),
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
