#!/usr/bin/env python3
"""Swan Harpy - the flamingo's geometry and animations (gen_flamingo_harpy.py: the long neck and legs), white, a black and orange beak.

    python tools/gen_swan_harpy.py

Field guide (thirty-sixth wave). Variants (swan_harpy_<variant>.png): keep in sync with SwanHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import gen_flamingo_harpy as Fl  # noqa: E402
from harpy_lib import rgb, ramp  # noqa: E402

NAME = "swan_harpy"
Fl.NAME = NAME                                     # the flamingo's model and animations, under her name
R = lambda *c: ramp([rgb(*x) for x in c])


def swan(hair, fea, flight, bib, skirt, beak, tip, leg):
    p = Fl.variant_pal(hair, fea, None)
    p.LEG = R(*leg)
    return dict(pal=p, bib=R(*bib), skirt=R(*skirt), flight=R(*flight), beak=(R(*beak), R(*tip)))


def white(a, b):
    return [tuple(int(a[i] + (b[i] - a[i]) * t / 5) for i in range(3)) for t in range(6)]


DARK = [(14, 14, 16), (28, 28, 32), (44, 44, 50), (66, 66, 74), (90, 90, 100)]
ORANGE = [(170, 80, 20), (220, 120, 30), (240, 160, 60)]
BLACK = [(20, 20, 24), (36, 36, 42), (56, 56, 64), (80, 80, 90)]
DEFAULT = swan(white((190, 190, 196), (255, 255, 255)), white((200, 200, 206), (255, 255, 255)) + [(255, 255, 255)], white((180, 180, 188), (252, 252, 255)) + [(255, 255, 255)],
               [(236, 236, 240), (246, 246, 248), (252, 252, 252), (255, 255, 255)], [(214, 214, 220), (232, 232, 238), (246, 246, 250), (255, 255, 255)], BLACK, ORANGE, DARK)
VARIANTS = {
    "black": swan(white((14, 14, 18), (80, 80, 90)), white((10, 10, 14), (70, 70, 80)) + [(120, 120, 130)], white((8, 8, 10), (60, 60, 70)) + [(90, 90, 100)],
                  [(30, 30, 36), (46, 46, 54), (60, 60, 70), (80, 80, 90)], [(20, 20, 24), (36, 36, 42), (56, 56, 64), (76, 76, 86)], [(170, 30, 30), (210, 50, 44), (236, 90, 70), (248, 130, 110)], [(236, 236, 236), (248, 248, 248), (255, 255, 255)], DARK),
    "rose": swan(white((210, 160, 176), (255, 236, 242)), white((220, 170, 186), (255, 240, 246)) + [(255, 250, 252)], white((200, 150, 170), (252, 226, 236)) + [(255, 244, 248)],
                 [(246, 226, 234), (250, 236, 242), (254, 244, 248), (255, 252, 254)], [(232, 196, 210), (244, 216, 226), (250, 232, 240), (255, 246, 250)], BLACK, ORANGE, DARK),
    "gold": swan(white((170, 130, 50), (250, 236, 190)), white((180, 140, 60), (252, 240, 200)) + [(255, 250, 230)], white((150, 110, 40), (240, 216, 150)) + [(250, 236, 190)],
                 [(246, 234, 200), (250, 242, 216), (254, 248, 232), (255, 252, 244)], [(214, 180, 100), (232, 204, 140), (246, 226, 180), (252, 240, 210)], BLACK, ORANGE, DARK),
    "blue": swan(white((130, 160, 200), (236, 244, 255)), white((140, 170, 210), (240, 246, 255)) + [(250, 252, 255)], white((110, 140, 190), (226, 236, 252)) + [(244, 248, 255)],
                 [(230, 238, 250), (240, 244, 252), (248, 250, 255), (255, 255, 255)], [(190, 210, 236), (214, 228, 246), (234, 242, 252), (250, 252, 255)], BLACK, ORANGE, DARK),
}


def paint(v=None):
    v = v or DEFAULT
    Fl.LEAF, Fl.BLACK = v["skirt"], v["flight"]
    Fl.RING = ramp([rgb(196, 150, 50), rgb(232, 190, 80), rgb(250, 226, 150)])
    return Fl.paint(v["pal"], v["bib"], v["beak"])


if __name__ == "__main__":
    Fl.declare_parts()
    H.run(NAME, paint, Fl.build_model, Fl.animations, speak="sing", head_center=(0, 23.5 + Fl.L), body_center=(0, 15.5 + Fl.L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("stand1", 2.0, 0, 0), ("idle", 0, -40, 8)])
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide", "textures", "entity")
    for k, v in VARIANTS.items():
        H.write_png(os.path.join(tex_dir, f"{NAME}_{k}.png"), paint(v))
