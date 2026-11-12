#!/usr/bin/env python3
"""Ibisking Harpy - the flamingo's geometry and animations (gen_flamingo_harpy.py), robed, with a long curved bill.

    python tools/gen_ibisking_harpy.py                 # write the three asset files and the variants
    python tools/gen_ibisking_harpy.py --preview x.png # also render preview sheets

Field guide (twenty-eighth wave): the great ibis of the temple scribes, deep blue, in gold and tassels. Befriended, give her paper and she tells you where the nearest treasure lies. Variants (ibisking_harpy_<variant>.png): ivory, ink, bronze, jade. Keep in sync with IbiskingHarpy.Variant.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
from bird_shades import shade as S  # noqa: E402
import gen_flamingo_harpy as Fl  # noqa: E402
from harpy_lib import rgb, ramp  # noqa: E402

NAME = "ibisking_harpy"
Fl.NAME = NAME                                     # the flamingo's model and animations, under her name

R = lambda *c: ramp([rgb(*x) for x in c])


def ibis(hair, fea, flight, bib, skirt, eye="#c8a040"):
    """a palette set: the harpy palette, and the bib, skirt, flight feather and beak ramps the flamingo paints with"""
    p = Fl.variant_pal(S(hair, 6), S(fea, 4), S(eye))
    p.LEG = R(*S("#2a2a34"))
    return dict(pal=p, bib=R(*S(bib, 4)), skirt=R(*S(skirt, 4)), flight=R(*S(flight, 4)), beak=(R(*S("#2a2a34", 4)), R(*S("#14141c", 3))))


DEFAULT = ibis("#1e3a8a", "#2a4a9a", "#14286a", "#e8c050", "#1e3a8a")

VARIANTS = {
    # ivory: the sacred white ibis
    "ivory": ibis("#e8e4d8", "#f0ece4", "#1e1e28", "#e8c050", "#c8b070"),
    # ink: the scribe's black
    "ink": ibis("#1e1e28", "#2a2a34", "#14141c", "#c8c8d0", "#3a3a48"),
    # bronze: the glossy ibis
    "bronze": ibis("#6a3a2a", "#7a4a30", "#3a5a4a", "#e8b050", "#6a3a2a"),
    # jade: temple green
    "jade": ibis("#1e6a5a", "#2a7a68", "#14483e", "#e8c050", "#1e6a5a"),
}


def paint(v=None):
    """the flamingo's painter with her robe, flight feathers and jewellery recoloured (module globals there)"""
    v = v or DEFAULT
    Fl.LEAF, Fl.BLACK = v["skirt"], v["flight"]
    Fl.RING = ramp([rgb(196, 150, 50), rgb(232, 190, 80), rgb(250, 226, 150)])           # gold bracers and pendant
    return Fl.paint(v["pal"], v["bib"], v["beak"])


def write_variants():
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide", "textures", "entity")
    for k, v in VARIANTS.items():
        H.write_png(os.path.join(tex_dir, f"{NAME}_{k}.png"), paint(v))
    print(f"{NAME}: {len(VARIANTS)} variants ({', '.join(VARIANTS)})")


if __name__ == "__main__":
    Fl.declare_parts()
    H.run(NAME, paint, Fl.build_model, Fl.animations, speak="sing", head_center=(0, 23.5 + Fl.L), body_center=(0, 15.5 + Fl.L),
          face_views=[("idle", 0, 0, 0), ("sing", 0.5, 0, 0), ("stand1", 2.0, 0, 0), ("idle", 0, -40, 8)])
    write_variants()
