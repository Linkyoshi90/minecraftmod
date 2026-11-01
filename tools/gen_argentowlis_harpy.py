#!/usr/bin/env python3
"""Argentowlis Harpy - the owl's geometry and animations (gen_owl_harpy.py), drawn huge (MonsterGirlsClient scales her).

    python tools/gen_argentowlis_harpy.py                 # write the three asset files and the variants
    python tools/gen_argentowlis_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-eighth wave): a giant owl, blue-black with a silver-white chest. Befriended, she marks the monsters hiding near her friend at night. Variants (argentowlis_harpy_<variant>.png): silver, snow, sooty, tawny. Keep in sync with ArgentowlisHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
from bird_shades import shade as S  # noqa: E402
import gen_owl_harpy as Ow  # noqa: E402
from harpy_lib import rgb, ramp  # noqa: E402

NAME = "argentowlis_harpy"
Ow.NAME = NAME                                     # the owl's model and animations, under her name

R = lambda *c: ramp([rgb(*x) for x in c])
LEG = R(*S("#3a3a4a"))
AMBER = R(*S("#d88a3a"))
ROSE = R(*S("#c8506a"))


def owl(hair, fea, eye, moss):
    return dict(pal=Ow.variant_pal(R(*S(hair, 6)), R(*S(fea, 6)), eye, LEG), moss=R(*S(moss)))


DEFAULT = owl("#2a3e7a", "#2e4888", ROSE, "#dce4f4")

VARIANTS = {
    # silver: nearly all white metal
    "silver": owl("#a8b0c0", "#98a0b4", ROSE, "#f0f4f8"),
    # snow: the far north's
    "snow": owl("#e8ecf0", "#dce0e8", AMBER, "#ffffff"),
    # sooty: a shape in the dark
    "sooty": owl("#1a1a22", "#22222c", AMBER, "#6a6a78"),
    # tawny: brown and buff
    "tawny": owl("#6a4a2a", "#7a5834", AMBER, "#e8d4b0"),
}


def paint(v=None):
    v = v or DEFAULT
    return Ow.paint(v["pal"], MOSS=v["moss"])


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "monstergirls", "textures", "entity")
    for k, v in VARIANTS.items():
        H.write_png(os.path.join(tex_dir, f"{NAME}_{k}.png"), paint(v))
    print(f"{NAME}: {len(VARIANTS)} variants ({', '.join(VARIANTS)})")


if __name__ == "__main__":
    Ow.declare_parts()
    H.run(NAME, paint, Ow.build_model, Ow.animations, speak="hoot", head_center=(0, 22.5 + Ow.L), body_center=(0, 15.5 + Ow.L),
          face_views=[("idle", 0, 0, 0), ("hoot", 0.5, 0, 0), ("sleep", 1.0, 0, 0), ("idle", 3.5, 0, 0)])
    write_variants()
