#!/usr/bin/env python3
"""Writes the unfertilised harpy eggs that grown harpies lay on a hen's schedule (the sparrow's is
tools/gen_sparrow_egg_item.py, the riflebird's existed before): 16x16 item textures, item models, the item tags
(monstergirls:laid_eggs and the common c:eggs) and one cake and one pumpkin pie recipe that take any of them.
    python tools/gen_laid_eggs.py
"""
import json
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from harpy_lib import write_png  # noqa: E402

RES = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "monstergirls")
DATA = os.path.join(RES, "data", "monstergirls")

# name: (english, shell, shade, outline, flecks, fleck count, radius x, radius y)
EGGS = {
    "gull_egg": ("Gull Egg", (176, 168, 120), (146, 138, 96), (70, 64, 44), [(60, 52, 40), (92, 80, 56)], 22, 4.3, 5.7),
    "crow_egg": ("Crow Egg", (150, 190, 170), (120, 160, 142), (52, 70, 62), [(70, 60, 44), (96, 84, 60)], 18, 4.0, 5.4),
    "owl_egg": ("Owl Egg", (244, 242, 236), (214, 210, 200), (110, 104, 96), [], 0, 4.6, 5.2),
    "albatross_egg": ("Albatross Egg", (240, 236, 224), (206, 200, 186), (104, 98, 86), [(150, 120, 96)], 6, 4.8, 6.4),
    "ostrich_egg": ("Ostrich Egg", (240, 230, 204), (212, 200, 170), (110, 98, 74), [(200, 188, 156)], 30, 5.8, 7.0),
    "ember_egg": ("Ember Egg", (232, 120, 40), (190, 70, 30), (90, 30, 16), [(255, 214, 90), (255, 170, 60)], 20, 4.4, 5.8),
}


def texture(name, shell, shade, outline, flecks, n, rx0, ry):
    rnd = random.Random(name)
    rows = [[(0, 0, 0, 0)] * 16 for _ in range(16)]
    cx, cy = 7.5, 8.4
    for y in range(16):
        for x in range(16):
            rx = rx0 if y >= cy else rx0 - (cy - y) * 0.16      # narrower at the top
            d = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2
            if d <= 1.0:
                c = shade if (x - cx) + (y - cy) * 0.6 > 2.2 else shell
                if (x - cx) + (y - cy) < -4.5 and d < 0.6:
                    c = tuple(min(255, v + 18) for v in shell)       # a highlight
                rows[y][x] = c + (255,)
            elif d <= 1.38:
                rows[y][x] = outline + (255,)
    for _ in range(n):
        x, y = rnd.randrange(3, 13), rnd.randrange(2, 15)
        if rows[y][x][3] and rows[y][x][:3] not in (outline,):
            rows[y][x] = rnd.choice(flecks) + (255,)
    return rows


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def main():
    for name, (eng, *spec) in EGGS.items():
        write_png(os.path.join(ASSETS, "textures", "item", name + ".png"), texture(name, *spec))
        dump(os.path.join(ASSETS, "models", "item", name + ".json"),
             {"parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:item/" + name}})
    laid = ["monstergirls:sparrow_egg", "monstergirls:riflebird_egg"] + ["monstergirls:" + n for n in EGGS]
    dump(os.path.join(DATA, "tags", "item", "laid_eggs.json"), {"values": laid})
    dump(os.path.join(RES, "data", "c", "tags", "item", "eggs.json"), {"replace": False, "values": laid})
    dump(os.path.join(DATA, "recipe", "cake_from_laid_egg.json"), {
        "type": "minecraft:crafting_shaped", "category": "food",
        "pattern": ["AAA", "BEB", "CCC"],
        "key": {"A": {"item": "minecraft:milk_bucket"}, "B": {"item": "minecraft:sugar"},
                "C": {"item": "minecraft:wheat"}, "E": {"tag": "monstergirls:laid_eggs"}},
        "result": {"id": "minecraft:cake", "count": 1},
    })
    dump(os.path.join(DATA, "recipe", "pumpkin_pie_from_laid_egg.json"), {
        "type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [{"item": "minecraft:pumpkin"}, {"item": "minecraft:sugar"}, {"tag": "monstergirls:laid_eggs"}],
        "result": {"id": "minecraft:pumpkin_pie", "count": 1},
    })
    lang_path = os.path.join(ASSETS, "lang", "en_us.json")
    import collections
    lang = json.load(open(lang_path, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
    for name, (eng, *_) in EGGS.items():
        lang["item.monstergirls." + name] = eng
    with open(lang_path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(json.dumps(lang, indent=2, ensure_ascii=False) + "\n")
    print(len(EGGS), "laid eggs written")


if __name__ == "__main__":
    main()
