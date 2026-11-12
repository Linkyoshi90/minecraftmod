#!/usr/bin/env python3
"""Assets for longing (2026-11-04): the salami item and the tar-shroom block (icons, models, blockstate, loot, recipe,
worldgen patch and its biome modifier).

    uv run --no-project --with pillow python tools/gen_longing_assets.py [--preview previews/longing_assets.png]

The salami follows the user's picture: a red marbled sausage lying corner to corner, gold end caps, a gold-framed dark plaque
in the middle and a gold tassel. At 16 pixels the plaque carries no lettering.
"""
import json
import math
import os
import random
import sys

from PIL import Image

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(REPO, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "harmontide")
DATA = os.path.join(RES, "data", "harmontide")


def rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (c[3],)


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def outline(img, colour):
    w, h = img.size
    px = img.load()
    edge = []
    for y in range(h):
        for x in range(w):
            if px[x, y][3]:
                continue
            if any(0 <= x + dx < w and 0 <= y + dy < h and px[x + dx, y + dy][3] and px[x + dx, y + dy] != colour
                   for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                edge.append((x, y))
    for x, y in edge:
        px[x, y] = colour


def salami():
    rnd = random.Random(7)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    red, fleck, gold, gold_dark, plaque = rgb("#C8323A"), rgb("#F2C4C0"), rgb("#F2C14E"), rgb("#A8761E"), rgb("#2A1A10")
    ax, ay, bx, by = 2.5, 13.0, 13.0, 2.5            # the sausage's axis, bottom left to top right
    lx, ly = bx - ax, by - ay
    length = math.hypot(lx, ly)
    for y in range(16):
        for x in range(16):
            cx, cy = x + 0.5 - ax, y + 0.5 - ay
            t = (cx * lx + cy * ly) / (length * length)
            d = (cx * -ly + cy * lx) / length            # across: negative is the upper left side
            if t < -0.04 or t > 1.04:
                continue
            cap = t < 0.1 or t > 0.9
            radius = 1.3 if cap else 2.7
            if abs(d) > radius:
                continue
            light = 1.15 - 0.12 * (d + radius)           # lit from the upper left
            if cap:
                c = gold if abs(d) < 0.7 else gold_dark
            elif 0.31 < t < 0.69 and abs(d) < 2.1:
                frame = abs(d) > 1.4 or t < 0.36 or t > 0.64
                c = gold if frame else plaque
                if not frame and rnd.random() < 0.15:
                    c = shade(gold, 0.8)                 # a glint on the plaque
            else:
                c = fleck if rnd.random() < 0.22 else red
            px[x, y] = shade(c, light)
    for x, y in ((14, 1), (15, 2), (15, 3), (14, 4)):  # the tassel off the top cap
        px[x, y] = gold if y < 3 else gold_dark
    outline(img, rgb("#3A1614"))
    return img


def tar_shroom():
    rnd = random.Random(11)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    tar, sheen, drip, stem, stem_dark = rgb("#17131C"), rgb("#5B4A72"), rgb("#0C0A10"), rgb("#B9AE97"), rgb("#7D735F")
    # three shrooms on a cross model: a big one, two small ones
    for cx, top, r, h in ((8, 4, 4.6, 9), (3, 10, 2.2, 4), (13, 9, 2.4, 5)):
        for y in range(16):
            for x in range(16):
                dx, dy = x + 0.5 - cx, y + 0.5 - (top + r * 0.6)
                if dy <= 0 and dx * dx / (r * r) + dy * dy / ((r * 0.75) ** 2) <= 1.0:
                    c = tar
                    if dy < -r * 0.35 and -r * 0.6 < dx < -r * 0.05:
                        c = sheen                           # wet highlight
                    elif rnd.random() < 0.12:
                        c = shade(tar, 1.6)
                    px[x, y] = c
        cap_y = int(top + r * 0.6)
        stem_w = max(1, int(r * 0.35))
        for y in range(cap_y, min(16, cap_y + h - int(r))):
            for x in range(cx - stem_w, cx + stem_w):
                if not px[x, y][3]:
                    px[x, y] = stem if x < cx else stem_dark
        for i in range(int(r)):                            # tar dripping off the rim
            x = int(cx - r + 1 + rnd.random() * (2 * r - 2))
            for y in range(cap_y, cap_y + rnd.randint(1, 2)):
                if 0 <= x < 16 and y < 16:
                    px[x, y] = drip
    return img


def write_assets():
    tex = os.path.join(ASSETS, "textures")
    os.makedirs(os.path.join(tex, "item"), exist_ok=True)
    os.makedirs(os.path.join(tex, "block"), exist_ok=True)
    s, t = salami(), tar_shroom()
    s.save(os.path.join(tex, "item", "salami.png"))
    t.save(os.path.join(tex, "block", "tar_shroom.png"))
    dump(os.path.join(ASSETS, "models", "item", "salami.json"),
         {"parent": "minecraft:item/generated", "textures": {"layer0": "harmontide:item/salami"}})
    dump(os.path.join(ASSETS, "models", "item", "tar_shroom.json"),
         {"parent": "minecraft:item/generated", "textures": {"layer0": "harmontide:block/tar_shroom"}})
    dump(os.path.join(ASSETS, "models", "block", "tar_shroom.json"),
         {"parent": "minecraft:block/cross", "render_type": "minecraft:cutout", "textures": {"cross": "harmontide:block/tar_shroom"}})
    dump(os.path.join(ASSETS, "blockstates", "tar_shroom.json"), {"variants": {"": {"model": "harmontide:block/tar_shroom"}}})
    dump(os.path.join(DATA, "loot_table", "blocks", "tar_shroom.json"), {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "harmontide:tar_shroom"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    dump(os.path.join(DATA, "recipe", "salami.json"), {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": [{"item": "minecraft:porkchop"}, {"item": "minecraft:beef"}, {"item": "minecraft:string"},
                        {"item": "minecraft:gold_nugget"}],
        "result": {"id": "harmontide:salami", "count": 2}})
    dump(os.path.join(DATA, "worldgen", "configured_feature", "tar_shroom_patch.json"), {
        "type": "minecraft:random_patch",
        "config": {"tries": 24, "xz_spread": 5, "y_spread": 2, "feature": {
            "feature": {"type": "minecraft:simple_block",
                        "config": {"to_place": {"type": "minecraft:simple_state_provider",
                                                "state": {"Name": "harmontide:tar_shroom"}}}},
            "placement": [{"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": [
                {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
                {"type": "minecraft:would_survive", "state": {"Name": "harmontide:tar_shroom"}}]}}]}}})
    dump(os.path.join(DATA, "worldgen", "placed_feature", "tar_shroom_patch.json"), {
        "feature": "harmontide:tar_shroom_patch",
        "placement": [{"type": "minecraft:rarity_filter", "chance": 6}, {"type": "minecraft:in_square"},
                      {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING"}, {"type": "minecraft:biome"}]})
    dump(os.path.join(DATA, "neoforge", "biome_modifier", "tar_shroom.json"), {
        "type": "neoforge:add_features",
        "biomes": ["minecraft:swamp", "minecraft:mangrove_swamp", "minecraft:dark_forest"],
        "features": "harmontide:tar_shroom_patch",
        "step": "vegetal_decoration"})
    return s, t


def main():
    s, t = write_assets()
    if "--preview" in sys.argv:
        out = sys.argv[sys.argv.index("--preview") + 1]
        sheet = Image.new("RGBA", (16 * 2 + 4, 16), (200, 200, 200, 255))
        sheet.alpha_composite(s, (0, 0))
        sheet.alpha_composite(t, (20, 0))
        sheet.resize((sheet.width * 16, sheet.height * 16), Image.NEAREST).save(out)
    print("longing assets written")


if __name__ == "__main__":
    main()
