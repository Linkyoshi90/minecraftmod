#!/usr/bin/env python3
"""Render the three anurans side by side at one scale (front, 3/4) -> previews/anuran_roster.png

    python tools/anuran_roster.py
Run the three gen_*.py scripts first (this reads the files they write)."""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H

ASSETS = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide")
FAMILIES = {
    "frogs": ["desert_rain_frog", "glass_frog", "tree_frog", "poison_dart_frog", "bullywug", "bullfrog", "goliath_frog"],
    "toads": ["natterjack", "surinam_toad", "fire_bellied_toad", "mucus_toad", "cane_toad", "mailtoad"],
}


def load_png(path):
    import zlib
    data = open(path, "rb").read()
    pos, chunks = 8, {}
    idat = b""
    while pos < len(data):
        ln = int.from_bytes(data[pos:pos + 4], "big")
        tag = data[pos + 4:pos + 8]
        body = data[pos + 8:pos + 8 + ln]
        if tag == b"IHDR":
            w, h = int.from_bytes(body[:4], "big"), int.from_bytes(body[4:8], "big")
        elif tag == b"IDAT":
            idat += body
        pos += 12 + ln
    raw = zlib.decompress(idat)
    stride = w * 4 + 1
    rows, prev = [], bytearray(w * 4)
    for y in range(h):
        line = bytearray(raw[y * stride + 1:(y + 1) * stride])          # our writer only uses filter 0
        rows.append([tuple(line[i:i + 4]) for i in range(0, w * 4, 4)])
    return rows


def sheet_for(species, out):
    bg = (44, 48, 44, 255)
    W, Hh, scale = 340, 520, 11.5
    views = [(0, 0), (-35, 10)]
    sheet = [[bg] * (W * len(species)) for _ in range(Hh * len(views))]
    for c, name in enumerate(species):
        model = json.load(open(os.path.join(ASSETS, "geo", "entity", name + ".geo.json")))
        anim = json.load(open(os.path.join(ASSETS, "animations", "entity", name + ".animation.json")))["animations"][f"animation.{name}.idle"]
        tex = load_png(os.path.join(ASSETS, "textures", "entity", name + ".png"))
        for r, (yaw, pitch) in enumerate(views):
            im = H.render(model, tex, anim, 0, yaw, pitch, W, Hh, scale, bg)
            for y in range(Hh):
                sheet[r * Hh + y][c * W:(c + 1) * W] = im[y]
    H.write_png(out, sheet)
    print("roster ->", out)


def main():
    for fam, species in FAMILIES.items():
        sheet_for(species, os.path.join(H.REPO, "previews", f"anuran_roster_{fam}.png"))


if __name__ == "__main__":
    main()
