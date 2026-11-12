#!/usr/bin/env python3
"""The realm censer (block/RealmCenserBlock, 2026-11-08): a lantern of dark plum iron burning a heartfruit's violet flame.

    uv run --no-project --with pillow python tools/gen_realm_censer.py [--preview previews/realm_censer.png]

Textures are the vanilla lantern recoloured: the iron frame to dark plum, the flame to violet (lit) or a dead ember (unlit).
Writes the block and item models (vanilla lantern templates), blockstate, loot table, recipe and pickaxe tag.
"""
import colorsys
import json
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS, DATA, MC_DATA, dump, rgb, save  # noqa: E402
from gen_realm_cuisine import _img  # noqa: E402
from gen_paints import _vanilla_rgba  # noqa: E402

PLUM = [rgb("#1E0E22"), rgb("#2E1634"), rgb("#42224A"), rgb("#5A3264"), rgb("#74447E"), rgb("#8E5A98")]
FLAME = [rgb("#7A1E9E"), rgb("#A43ACA"), rgb("#C860E8"), rgb("#E08AF8"), rgb("#F0B8FF"), rgb("#FFE8FF")]
EMBER = [rgb("#1A0E1E"), rgb("#2A1630"), rgb("#3A1E42"), rgb("#4A2852"), rgb("#5A3262"), rgb("#6A3C72")]


def _recolour(img, flame):
    """warm pixels (the flame) go on `flame`; the grey frame on PLUM; both by lightness"""
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            h, l, s = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
            ramp = flame if (0.0 <= h <= 0.2 and s > 0.4) else PLUM
            px[x, y] = ramp[min(len(ramp) - 1, int(l * len(ramp)))]
    return img


def textures():
    lit = save(_recolour(_img(_vanilla_rgba("block/lantern")), FLAME), "block", "realm_censer.png")
    unlit = save(_recolour(_img(_vanilla_rgba("block/lantern")), EMBER), "block", "realm_censer_unlit.png")
    item = save(_recolour(_img(_vanilla_rgba("item/lantern")), FLAME), "item", "realm_censer.png")
    return [lit, unlit, item]


def data():
    m = os.path.join(ASSETS, "models")
    for lit in (True, False):
        tex = "harmontide:block/realm_censer" + ("" if lit else "_unlit")
        for hanging in (False, True):
            name = "realm_censer" + ("_hanging" if hanging else "") + ("" if lit else "_unlit")
            dump(os.path.join(m, "block", name + ".json"),
                 {"parent": "minecraft:block/template_" + ("hanging_" if hanging else "") + "lantern", "render_type": "minecraft:cutout",
                  "textures": {"lantern": tex}})
    dump(os.path.join(ASSETS, "blockstates", "realm_censer.json"), {"variants": {
        f"hanging={str(h).lower()},lit={str(l).lower()}": {"model": "harmontide:block/realm_censer" + ("_hanging" if h else "") + ("" if l else "_unlit")}
        for h in (False, True) for l in (False, True)}})
    dump(os.path.join(m, "item", "realm_censer.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": "harmontide:item/realm_censer"}})
    dump(os.path.join(DATA, "loot_table", "blocks", "realm_censer.json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": "harmontide:realm_censer"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    dump(os.path.join(DATA, "recipe", "realm_censer.json"), {
        "type": "minecraft:crafting_shaped", "category": "decorations", "pattern": [" C ", "IHI", " A "],
        "key": {"C": {"item": "minecraft:chain"}, "I": {"item": "minecraft:iron_ingot"}, "H": {"item": "harmontide:heartfruit"},
                "A": {"item": "minecraft:amethyst_shard"}},
        "result": {"id": "harmontide:realm_censer", "count": 1}})
    path = os.path.join(MC_DATA, "tags", "block", "mineable", "pickaxe.json")
    old = json.load(open(path, encoding="utf-8")) if os.path.exists(path) else {"replace": False, "values": []}
    if "harmontide:realm_censer" not in old["values"]:
        old["values"].append("harmontide:realm_censer")
    dump(path, old)


def main():
    tiles = textures()
    data()
    if "--preview" in sys.argv:
        path = sys.argv[sys.argv.index("--preview") + 1]
        sheet = Image.new("RGBA", (len(tiles) * 108, 108), (40, 40, 48, 255))
        for i, t in enumerate(tiles):
            big = t.resize((96, 96), Image.NEAREST)
            sheet.paste(big, (i * 108 + 6, 6), big)
        sheet.save(path)


if __name__ == "__main__":
    main()
