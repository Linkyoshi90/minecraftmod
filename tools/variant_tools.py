"""Shared helpers for species colour variants: write one texture per variant and render a lineup sheet.

Variants are texture-only: they reuse the species' geometry and animations and only change the palette.
Files are written as <species>_<variant>.png next to the default <species>.png (the Java side picks one per
spawn; see each entity's Variant enum). An optional `glow` callback writes a matching glow mask per variant.
"""
import os
import sys

import harpy_lib as H


def tex_dir():
    return os.path.join(H.REPO, "src", "main", "resources", "assets", "harpies", "textures", "entity")


def write_variants(name, default_key, paint, variants, build_model, animations, rows, glow=None, width=380):
    """paint(v=None) -> texture rows. `variants` maps name -> whatever paint() takes. `rows` is the lineup layout.

    glow(tex, suffix) is called for the default ("") and every variant ("_name") when given.
    """
    texes = {default_key: paint()}
    if glow:
        glow(texes[default_key], "")
    for key, v in variants.items():
        texes[key] = paint(v)
        H.write_png(os.path.join(tex_dir(), f"{name}_{key}.png"), texes[key])
        if glow:
            glow(texes[key], "_" + key)
    print(f"{name}: {len(variants)} colour variants ({', '.join(variants)})")
    if "--lineup" in sys.argv:
        lineup(sys.argv[sys.argv.index("--lineup") + 1], name, texes, build_model, animations, rows, width)


def lineup(path, name, texes, build_model, animations, rows, width=380):
    """one column per variant; `rows` = [(animation, time, yaw, pitch, scale, center_or_None), ...]"""
    model = build_model().doc()
    an = H.Anims(name, [b["name"] for b in model["minecraft:geometry"][0]["bones"]])
    animations(an)
    A = an.doc()["animations"]
    bg = (44, 48, 44, 255)
    sheet = [[bg] * (width * len(texes)) for _ in range(width * len(rows))]
    for c, tex in enumerate(texes.values()):
        for r, (anim, tm, yaw, pitch, scale, center) in enumerate(rows):
            im = H.render(model, tex, A[f"animation.{name}.{anim}"], tm, yaw, pitch, width, width, scale, bg, center=center)
            for y in range(width):
                sheet[r * width + y][c * width:(c + 1) * width] = im[y]
    H.write_png(path, sheet)
    print("lineup ->", path, "(columns:", ", ".join(texes), ")")
