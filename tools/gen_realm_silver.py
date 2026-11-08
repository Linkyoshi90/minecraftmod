#!/usr/bin/env python3
"""Realm silver (2026-11-08, entity/RealmSilver): the ingot, the sword and the armour, the vanilla iron ones recoloured to a pale lilac
silver with a violet sheen. Writes the textures, item models, recipes and the swords tag (so it takes sword enchantments).

    uv run --no-project --with pillow python tools/gen_realm_silver.py [--preview previews/realm_silver.png]
"""
import json
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS, DATA, MC_DATA, dump, save  # noqa: E402
from gen_realm_cuisine import _img  # noqa: E402
from gen_paints import _vanilla_rgba  # noqa: E402

SILVER = [(0x3A, 0x2E, 0x48), (0x6A, 0x5A, 0x7E), (0x98, 0x88, 0xB0), (0xBC, 0xAE, 0xD4), (0xDA, 0xD0, 0xEC), (0xF4, 0xEE, 0xFC)]


def silver(rows, keep_wood=False):
    """grey pixels go on the silver ramp by lightness; with keep_wood, the brown hilt stays as it is"""
    img = _img(rows)
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if not a:
                continue
            if keep_wood and r > b + 25:                                  # the sword's wooden grip
                continue
            l = (r + g + b) / 765
            px[x, y] = SILVER[min(len(SILVER) - 1, int(l * len(SILVER)))] + (a,)
    return img


def main():
    tiles = [save(silver(_vanilla_rgba("item/iron_ingot")), "item", "realm_silver_ingot.png"),
             save(silver(_vanilla_rgba("item/iron_sword"), keep_wood=True), "item", "realm_silver_sword.png")]
    dump(os.path.join(ASSETS, "models", "item", "realm_silver_ingot.json"),
         {"parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:item/realm_silver_ingot"}})
    dump(os.path.join(ASSETS, "models", "item", "realm_silver_sword.json"),
         {"parent": "minecraft:item/handheld", "textures": {"layer0": "monstergirls:item/realm_silver_sword"}})
    dump(os.path.join(DATA, "recipe", "realm_silver_ingot.json"), {
        "type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["AHA", "AIA"],
        "key": {"A": {"item": "minecraft:amethyst_shard"}, "H": {"item": "monstergirls:heartfruit"}, "I": {"item": "minecraft:iron_ingot"}},
        "result": {"id": "monstergirls:realm_silver_ingot", "count": 1}})
    dump(os.path.join(DATA, "recipe", "realm_silver_sword.json"), {
        "type": "minecraft:crafting_shaped", "category": "equipment", "pattern": ["S", "S", "T"],
        "key": {"S": {"item": "monstergirls:realm_silver_ingot"}, "T": {"item": "minecraft:stick"}},
        "result": {"id": "monstergirls:realm_silver_sword", "count": 1}})
    path = os.path.join(MC_DATA, "tags", "item", "swords.json")
    old = json.load(open(path, encoding="utf-8")) if os.path.exists(path) else {"replace": False, "values": []}
    if "monstergirls:realm_silver_sword" not in old["values"]:
        old["values"].append("monstergirls:realm_silver_sword")
    dump(path, old)
    # the armour: items and the two worn layers (textures/models/armor/realm_silver_layer_1/2.png)
    for piece in ("helmet", "chestplate", "leggings", "boots"):
        tiles.append(save(silver(_vanilla_rgba("item/iron_" + piece)), "item", "realm_silver_" + piece + ".png"))
        dump(os.path.join(ASSETS, "models", "item", "realm_silver_" + piece + ".json"),
             {"parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:item/realm_silver_" + piece}})
    for layer in (1, 2):
        save(silver(_vanilla_rgba(f"models/armor/iron_layer_{layer}")), "models", "armor", f"realm_silver_layer_{layer}.png")
    shapes = {"helmet": ["SSS", "S S"], "chestplate": ["S S", "SSS", "SSS"], "leggings": ["SSS", "S S", "S S"], "boots": ["S S", "S S"]}
    for piece, pattern in shapes.items():
        dump(os.path.join(DATA, "recipe", "realm_silver_" + piece + ".json"), {
            "type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pattern,
            "key": {"S": {"item": "monstergirls:realm_silver_ingot"}}, "result": {"id": "monstergirls:realm_silver_" + piece, "count": 1}})
    tags = {"head_armor": "helmet", "chest_armor": "chestplate", "leg_armor": "leggings", "foot_armor": "boots"}
    for tag, piece in tags.items():
        path = os.path.join(MC_DATA, "tags", "item", tag + ".json")
        old = json.load(open(path, encoding="utf-8")) if os.path.exists(path) else {"replace": False, "values": []}
        if "monstergirls:realm_silver_" + piece not in old["values"]:
            old["values"].append("monstergirls:realm_silver_" + piece)
        dump(path, old)
    if "--preview" in sys.argv:
        p = sys.argv[sys.argv.index("--preview") + 1]
        sheet = Image.new("RGBA", (len(tiles) * 108, 108), (40, 40, 48, 255))
        for i, t in enumerate(tiles):
            big = t.resize((96, 96), Image.NEAREST)
            sheet.paste(big, (i * 108 + 6, 6), big)
        sheet.save(p)


if __name__ == "__main__":
    main()
