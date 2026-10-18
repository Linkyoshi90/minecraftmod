#!/usr/bin/env python3
"""Writes the riflebird nest block's textures, block models, blockstate and item model.

    python tools/gen_nest_assets.py

The nest is a low woven bowl of twigs with a soft lining; the "eggs" blockstate (0-3) adds that many cream eggs
with brown streaks (like real riflebird eggs) sitting in the lining.
"""
import json
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from harpy_lib import write_png  # noqa: E402

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, "src", "main", "resources", "assets", "harpies")
NAME = "riflebird_nest"


def twigs():
    """Woven twigs: diagonal strands in several browns, with the odd leaf and vine tendril."""
    rnd = random.Random(7)
    browns = [(92, 62, 36), (112, 78, 44), (74, 50, 30), (130, 94, 56), (60, 40, 24)]
    rows = [[(66, 44, 26, 255)] * 16 for _ in range(16)]
    for strand in range(22):
        c = rnd.choice(browns)
        x, y = rnd.randrange(16), rnd.randrange(16)
        dx = rnd.choice((1, -1))
        for _ in range(rnd.randint(5, 10)):
            rows[y % 16][x % 16] = c + (255,)
            x += dx
            if rnd.random() < 0.55:
                y += 1
    for _ in range(9):  # leaves and vine bits woven in
        x, y = rnd.randrange(16), rnd.randrange(16)
        rows[y][x] = rnd.choice([(58, 104, 40), (76, 126, 48), (44, 86, 34)]) + (255,)
    return rows


def lining():
    """Soft lining of fibres, moss and a few dark feathers."""
    rnd = random.Random(11)
    base = [(150, 126, 84), (138, 114, 76), (162, 138, 94), (120, 128, 70)]
    rows = [[rnd.choice(base) + (255,) for _ in range(16)] for _ in range(16)]
    for _ in range(5):
        x, y = rnd.randrange(15), rnd.randrange(16)
        rows[y][x] = (24, 26, 40, 255)
        rows[y][x + 1] = (40, 70, 92, 255)
    return rows


def egg():
    """Cream shell with brown and purple-grey streaks running lengthwise."""
    rnd = random.Random(3)
    rows = [[(236, 222, 190, 255)] * 16 for _ in range(16)]
    for _ in range(14):
        x, y = rnd.randrange(16), rnd.randrange(16)
        c = rnd.choice([(140, 92, 54), (118, 98, 118), (166, 116, 70)])
        for k in range(rnd.randint(1, 3)):
            rows[(y + k) % 16][x] = c + (255,)
    return rows


def box(frm, to, tex, top=None):
    faces = {f: {"texture": tex} for f in ("north", "south", "east", "west", "down")}
    faces["up"] = {"texture": top or tex}
    return {"from": frm, "to": to, "faces": faces}


EGG_SPOTS = [([5, 1, 6], [8, 4, 9]), ([8, 1, 8], [11, 4, 11]), ([8, 1, 4], [11, 4, 7])]


def nest_model(eggs):
    elements = [
        box([1, 0, 1], [15, 1, 15], "#twigs", top="#lining"),
        box([1, 1, 1], [15, 4, 3], "#twigs"),
        box([1, 1, 13], [15, 4, 15], "#twigs"),
        box([1, 1, 3], [3, 4, 13], "#twigs"),
        box([13, 1, 3], [15, 4, 13], "#twigs"),
    ]
    elements += [box(frm, to, "#egg") for frm, to in EGG_SPOTS[:eggs]]
    return {
        "parent": "minecraft:block/block",
        "textures": {"particle": f"harpies:block/{NAME}", "twigs": f"harpies:block/{NAME}",
                     "lining": f"harpies:block/{NAME}_lining", "egg": f"harpies:block/{NAME}_egg"},
        "elements": elements,
    }


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


if __name__ == "__main__":
    write_png(os.path.join(ASSETS, "textures", "block", NAME + ".png"), twigs())
    write_png(os.path.join(ASSETS, "textures", "block", NAME + "_lining.png"), lining())
    write_png(os.path.join(ASSETS, "textures", "block", NAME + "_egg.png"), egg())
    for eggs in range(4):
        dump(os.path.join(ASSETS, "models", "block", f"{NAME}_{eggs}.json"), nest_model(eggs))
    dump(os.path.join(ASSETS, "blockstates", NAME + ".json"),
         {"variants": {f"eggs={e}": {"model": f"harpies:block/{NAME}_{e}"} for e in range(4)}})
    dump(os.path.join(ASSETS, "models", "item", NAME + ".json"), {"parent": f"harpies:block/{NAME}_1"})
    print("nest assets written")
