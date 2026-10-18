#!/usr/bin/env python3
"""Frog faces vs humanoid faces, side by side -> previews/anuran_face_compare.png

    python tools/anuran_face_compare.py
Run each gen_*.py twice first (plain = humanoid face, and with --frog-face); this reads the files they write.
Rows: full body (frog), full body (humanoid), face (frog), face (humanoid). Columns: rain frog, toad, bullywug."""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
from anuran_roster import load_png

ASSETS = os.path.join(H.REPO, "src", "main", "resources", "assets", "monstergirls")
SPECIES = [("desert_rain_frog", 22.0, (0, 14.0)), ("mucus_toad", 15.0, (0, 20.8)), ("bullywug", 12.5, (0, 24.0))]


def load(name):
    model = json.load(open(os.path.join(ASSETS, "geo", "entity", name + ".geo.json")))
    anim = json.load(open(os.path.join(ASSETS, "animations", "entity", name + ".animation.json")))["animations"][f"animation.{name}.idle"]
    return model, anim, load_png(os.path.join(ASSETS, "textures", "entity", name + ".png"))


def main():
    bg = (44, 48, 44, 255)
    W, Hb, Hf = 440, 520, 380
    rows = [("_frogface", False, 0), ("", False, 0), ("_frogface", True, 1), ("", True, 1)]
    heights = [Hb, Hb, Hf, Hf]
    total = sum(heights)
    sheet = [[bg] * (W * len(SPECIES)) for _ in range(total)]
    for c, (name, scale, head_c) in enumerate(SPECIES):
        for r, (suffix, face, _) in enumerate(rows):
            model, anim, tex = load(name + suffix)
            h_ = heights[r]
            if face:
                im = H.render(model, tex, anim, 0, -18, 6, W, h_, {"desert_rain_frog": 34.0, "mucus_toad": 27.0, "bullywug": 30.0}[name], bg, center=(0, head_c[1] + (0 if suffix else 0.6)))
            else:
                im = H.render(model, tex, anim, 0, -25, 8, W, h_, scale, bg)
            oy = sum(heights[:r])
            for y in range(h_):
                sheet[oy + y][c * W:(c + 1) * W] = im[y]
    out = os.path.join(H.REPO, "previews", "anuran_face_compare.png")
    H.write_png(out, sheet)
    print("compare ->", out)


if __name__ == "__main__":
    main()
