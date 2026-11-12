#!/usr/bin/env python3
"""Blocks of the families added on 2026-10-20: textures, block/item models, blockstates, loot tables and names.

    python tools/gen_block_assets.py

    painted_egg     the Easter Bunny's hidden eggs, six colours (block state color=0..5), drops itself
"""
import collections
import json
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402

RES = os.path.join(H.REPO, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "harmontide")
DATA = os.path.join(RES, "data", "harmontide")
LANG = {}
FACES = ("north", "south", "east", "west", "up", "down")


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def box(frm, to, tex="#t", uv=None):
    faces = {}
    for f in FACES:
        faces[f] = {"texture": tex}
        if uv:
            faces[f]["uv"] = uv
    return {"from": frm, "to": to, "faces": faces}


def self_drop(name):
    dump(os.path.join(DATA, "loot_table", "blocks", name + ".json"), {
        "type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "harmontide:" + name}],
                                              "conditions": [{"condition": "minecraft:survives_explosion"}]}]})


# ----------------------------------------------------------------------------- painted eggs
EGG_COLOURS = [((244, 160, 184), (250, 236, 140)), ((160, 216, 240), (250, 250, 250)), ((248, 224, 112), (240, 140, 170)),
               ((176, 232, 160), (250, 200, 120)), ((210, 170, 240), (160, 220, 240)), ((250, 186, 120), (170, 210, 120))]


def painted_egg():
    variants = {}
    for i, (base, band) in enumerate(EGG_COLOURS):
        rows = []
        rnd = random.Random(i)
        for y in range(16):
            row = []
            for x in range(16):
                c = band if y in (6, 7, 11) else base
                if y == 9 and x % 4 == 1:
                    c = band
                if rnd.random() < 0.06:
                    c = tuple(min(255, v + 20) for v in c)
                row.append(c + (255,))
            rows.append(row)
        H.write_png(os.path.join(ASSETS, "textures", "block", f"painted_egg_{i}.png"), rows)
        # an egg of three stacked boxes, narrower at the top
        els = [box([5.5, 0, 5.5], [10.5, 3, 10.5]), box([5, 1, 5], [11, 5, 11]), box([5.5, 5, 5.5], [10.5, 7, 10.5]), box([6.5, 7, 6.5], [9.5, 8, 9.5])]
        dump(os.path.join(ASSETS, "models", "block", f"painted_egg_{i}.json"),
             {"parent": "minecraft:block/block", "textures": {"particle": f"harmontide:block/painted_egg_{i}", "t": f"harmontide:block/painted_egg_{i}"},
              "elements": els})
        variants[f"color={i}"] = {"model": f"harmontide:block/painted_egg_{i}"}
    dump(os.path.join(ASSETS, "blockstates", "painted_egg.json"), {"variants": variants})
    dump(os.path.join(ASSETS, "models", "item", "painted_egg.json"), {"parent": "harmontide:block/painted_egg_0"})
    self_drop("painted_egg")
    LANG["block.harmontide.painted_egg"] = "Painted Egg"


def main():
    painted_egg()
    LANG.update({"message.harmontide.caught": "You caught %s!", "message.harmontide.calmed": "%s calms down",
                 "message.harmontide.no_milk": "%s has no milk to give yet", "message.harmontide.trespass": "%s bellows: this is her ground. Leave!",
                 "message.harmontide.elf_warning": "%s: Outsider, this is elven ground. Leave now.",
                 "message.harmontide.vampire_asks": "%s asks, very politely, for a little of your blood (right-click to say yes)",
                 "message.harmontide.vampire_visit": "You wake a little pale. %s visited you in the night",
                 "message.harmontide.prank_steal": "%s snatched your %s! Catch her (or feed her)",
                 "message.harmontide.prank_swap": "%s swapped things around in your hotbar, giggling"})
    path = os.path.join(ASSETS, "lang", "en_us.json")
    cur = json.load(open(path, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
    cur.update(LANG)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(json.dumps(cur, indent=2, ensure_ascii=False) + "\n")
    print("block assets written:", ", ".join(k for k in LANG if k.startswith("block.")))


if __name__ == "__main__":
    main()
