#!/usr/bin/env python3
"""Hot springs (2026-11-08, block/GeyseriteBlock, entity/HotSprings, world/HotSpringFeature): the geyserite texture (vanilla
calcite, warmed to cream, with rust-orange and teal mineral banding), its block data, the worldgen feature and where it grows,
a recipe, an advancement, lang, and a sentence for the home guide.

    uv run --no-project --with pillow python tools/gen_hot_spring_assets.py [--preview out.png]
"""
import os
import random
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_paints import _vanilla_rgba  # noqa: E402
from gen_realm_assets import ASSETS, DATA, MC_DATA, dump, save  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402
import json  # noqa: E402

BIOMES = ["minecraft:meadow", "minecraft:grove", "minecraft:snowy_slopes", "minecraft:snowy_plains", "minecraft:snowy_taiga",
          "minecraft:windswept_hills", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:cherry_grove",
          "minecraft:old_growth_spruce_taiga", "minecraft:ice_spikes"]


def geyserite():
    rnd = random.Random(91)
    src = _vanilla_rgba("calcite")
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        band = (y + rnd.choice((0, 0, 1))) % 6
        for x in range(16):
            r, g, b = src[y][x][:3]
            r, g, b = min(255, r + 6), min(255, g + 2), max(0, b - 10)         # warm cream
            if band == 0:
                r, g, b = int(r * 0.86 + 32), int(g * 0.72 + 10), int(b * 0.55)   # rust-orange mineral
            elif band == 3 and rnd.random() < 0.6:
                r, g, b = int(r * 0.7), int(g * 0.86 + 12), int(b * 0.86 + 20)   # teal mineral
            px[x, y] = (r, g, b, 255)
    return save(img, "block", "geyserite.png")


def data():
    m = os.path.join(ASSETS, "models")
    dump(os.path.join(m, "block", "geyserite.json"), {"parent": "minecraft:block/cube_all", "textures": {"all": "monstergirls:block/geyserite"}})
    dump(os.path.join(ASSETS, "blockstates", "geyserite.json"), {"variants": {"": {"model": "monstergirls:block/geyserite"}}})
    dump(os.path.join(m, "item", "geyserite.json"), {"parent": "monstergirls:block/geyserite"})
    dump(os.path.join(DATA, "loot_table", "blocks", "geyserite.json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": "monstergirls:geyserite"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    path = os.path.join(MC_DATA, "tags", "block", "mineable", "pickaxe.json")
    old = json.load(open(path, encoding="utf-8")) if os.path.exists(path) else {"replace": False, "values": []}
    if "monstergirls:geyserite" not in old["values"]:
        old["values"].append("monstergirls:geyserite")
    dump(path, old)
    dump(os.path.join(DATA, "recipe", "geyserite.json"), {
        "type": "minecraft:crafting_shapeless", "category": "building",
        "ingredients": [{"item": "minecraft:calcite"}, {"item": "minecraft:calcite"}, {"item": "minecraft:calcite"}, {"item": "minecraft:calcite"},
                        {"item": "minecraft:magma_block"}],
        "result": {"id": "monstergirls:geyserite", "count": 4}})
    dump(os.path.join(DATA, "worldgen", "configured_feature", "hot_spring.json"), {"type": "monstergirls:hot_spring", "config": {}})
    dump(os.path.join(DATA, "worldgen", "placed_feature", "hot_spring.json"), {"feature": "monstergirls:hot_spring", "placement": [
        {"type": "minecraft:rarity_filter", "chance": 32}, {"type": "minecraft:in_square"},
        {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}, {"type": "minecraft:biome"}]})
    dump(os.path.join(DATA, "neoforge", "biome_modifier", "hot_spring.json"), {
        "type": "neoforge:add_features", "biomes": BIOMES, "features": "monstergirls:hot_spring", "step": "lakes"})
    dump(os.path.join(DATA, "advancement", "hot_spring.json"), {
        "parent": "monstergirls:stargazer",
        "display": {"icon": {"id": "monstergirls:geyserite"}, "title": {"translate": "advancement.monstergirls.hot_spring"},
                    "description": {"translate": "advancement.monstergirls.hot_spring.desc"},
                    "frame": "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
    add_lang({
        "block.monstergirls.geyserite": "Geyserite",
        "monstergirls.configuration.hotSprings": "Hot springs",
        "message.monstergirls.hot_spring.soaked": "A long soak in the hot spring: you feel wonderful.",
        "advancement.monstergirls.hot_spring": "Into the Steam",
        "advancement.monstergirls.hot_spring.desc": "Soak in a hot spring",
    })
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.home"] = (
        " Out in cold and mountain country there are hot springs: pools floored with geyserite that steam and never freeze. A soak "
        "heals and warms you (a long one leaves you well soaked), your friends come and join you, and it cures a girl's cold. Lay "
        "geyserite under a pool at home (four calcite and a magma block make four) for an onsen of your own.")
    gen_manor_assets.append_guides()


def main():
    img = geyserite()
    data()
    if "--preview" in sys.argv:
        img.resize((128, 128), Image.NEAREST).save(sys.argv[sys.argv.index("--preview") + 1])


if __name__ == "__main__":
    main()
