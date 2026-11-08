#!/usr/bin/env python3
"""Realm variants (2026-11-08, block/RealmGrassBlock#variantFor, block/RealmSandBlock): the frost realm (lilac turf rimed with
frost), the bog realm (dark wet violet over mud) and realm sand (rose-violet sand with heart glints). Textures from the realm
grass's own recipe with other palettes, over vanilla dirt / mud / sand read from the 1.21.1 client jar; models, blockstates,
loot, tags, lang.

    uv run --no-project --with pillow python tools/gen_realm_variant_assets.py [--preview out.png]
"""
import json
import os
import random
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_paints import _vanilla_rgba  # noqa: E402
from gen_realm_assets import ASSETS, DATA, MC_DATA, rgb, shade, dump, save, heart  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402

FROST = [rgb("#8E7AB8"), rgb("#A08CC8"), rgb("#B4A2D6"), rgb("#C8B8E4"), rgb("#E4DCF4")]
FROST_HEART, FROST_DARK = rgb("#F0A8D8"), rgb("#B87AB8")
BOG = [rgb("#2A1430"), rgb("#341A3C"), rgb("#40224A"), rgb("#4E2A56"), rgb("#5E3466")]
BOG_HEART, BOG_DARK = rgb("#A8407A"), rgb("#6E2450")
RIME, PUDDLE, GLINT, GLINT_DARK = rgb("#F4F8FC"), rgb("#7A5A8E"), rgb("#FFD0E4"), rgb("#F25C9A")


def pick(pal, rnd, mid=2.0, spread=1.1):
    return pal[min(len(pal) - 1, max(0, int(rnd.gauss(mid, spread))))]


def turf_top(name, pal, hc, hd, seed, rime=False, wet=False):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = pick(pal, rnd)
            if rime and rnd.random() < 0.08:
                px[x, y] = RIME                                         # frost crystals
            if wet and rnd.random() < 0.05:
                px[x, y] = PUDDLE                                       # a glint of standing water
    for ox, oy in ((2, 3), (10, 10)):
        heart(px, ox, oy, colour=hc, dark=hd)
    return save(img, "block", name + "_top.png")


def turf_side(name, pal, under, seed, rime=False):
    rnd = random.Random(seed)
    base = _vanilla_rgba(under)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = tuple(base[y][x][:3]) + (255,)
    for x in range(16):
        depth = 3 + rnd.choice((0, 0, 1, 1, 2)) - (1 if rnd.random() < 0.25 else 0)
        for y in range(depth):
            px[x, y] = pick(pal, rnd)
        if rime and rnd.random() < 0.3:
            px[x, 0] = RIME
        if rnd.random() < 0.3:
            px[x, depth] = shade(pal[1], 0.85)
    return save(img, "block", name + "_side.png")


def realm_sand():
    rnd = random.Random(77)
    src = _vanilla_rgba("sand")
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            r, g, b = src[y][x][:3]
            lum = (r * 0.3 + g * 0.59 + b * 0.11) / 255.0
            px[x, y] = (int(190 * lum + 30), int(100 * lum + 20), int(160 * lum + 40), 255)   # rose-violet, the grain kept
    for _ in range(5):                                                   # heart glints: a pixel or two of pink
        x, y = rnd.randrange(1, 14), rnd.randrange(1, 15)
        px[x, y] = GLINT
        px[x + 1, y] = GLINT_DARK
    return save(img, "block", "realm_sand.png")


def data():
    m = os.path.join(ASSETS, "models")
    for name, bottom in (("realm_frost", "minecraft:block/dirt"), ("realm_bog", "minecraft:block/mud")):
        dump(os.path.join(m, "block", name + ".json"), {
            "parent": "minecraft:block/cube_bottom_top",
            "textures": {"top": f"monstergirls:block/{name}_top", "bottom": bottom, "side": f"monstergirls:block/{name}_side", "particle": bottom}})
        rot = [{"model": f"monstergirls:block/{name}"}, {"model": f"monstergirls:block/{name}", "y": 90},
               {"model": f"monstergirls:block/{name}", "y": 180}, {"model": f"monstergirls:block/{name}", "y": 270}]
        dump(os.path.join(ASSETS, "blockstates", name + ".json"),
             {"variants": {"snowy=false": rot, "snowy=true": {"model": "minecraft:block/grass_block_snow"}}})
        dump(os.path.join(m, "item", name + ".json"), {"parent": f"monstergirls:block/{name}"})
    dump(os.path.join(m, "block", "realm_sand.json"), {"parent": "minecraft:block/cube_all", "textures": {"all": "monstergirls:block/realm_sand"}})
    dump(os.path.join(ASSETS, "blockstates", "realm_sand.json"), {"variants": {"": [
        {"model": "monstergirls:block/realm_sand"}, {"model": "monstergirls:block/realm_sand", "y": 90},
        {"model": "monstergirls:block/realm_sand", "y": 180}, {"model": "monstergirls:block/realm_sand", "y": 270}]}})
    dump(os.path.join(m, "item", "realm_sand.json"), {"parent": "monstergirls:block/realm_sand"})
    silk = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
        {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
    for name, plain in (("realm_frost", "minecraft:dirt"), ("realm_bog", "minecraft:mud"), ("realm_sand", "minecraft:sand")):
        dump(os.path.join(DATA, "loot_table", "blocks", name + ".json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [
            {"type": "minecraft:alternatives", "children": [
                {"type": "minecraft:item", "conditions": [silk], "name": "monstergirls:" + name},
                {"type": "minecraft:item", "conditions": [{"condition": "minecraft:survives_explosion"}], "name": plain}]}]}]})
    for tag, entries in (("dirt", ["monstergirls:realm_frost", "monstergirls:realm_bog"]), ("sand", ["monstergirls:realm_sand"]),
                         ("mineable/shovel", ["monstergirls:realm_frost", "monstergirls:realm_bog", "monstergirls:realm_sand"])):
        path = os.path.join(MC_DATA, "tags", "block", tag + ".json")
        old = json.load(open(path, encoding="utf-8")) if os.path.exists(path) else {"replace": False, "values": []}
        for e in entries:
            if e not in old["values"]:
                old["values"].append(e)
        dump(path, old)
    add_lang({"block.monstergirls.realm_frost": "Frost Realm", "block.monstergirls.realm_bog": "Bog Realm", "block.monstergirls.realm_sand": "Realm Sand"})
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.realm"] = (
        " The realm takes on the land it spreads into: where it is cold enough to snow it is the frost realm, on mud and in swamps "
        "the bog realm (weepcaps thrive there, heartfruit does not), and on sand realm sand (heartfruit, but no weepcaps).")
    gen_manor_assets.append_guides()


def preview(path):
    tiles = [Image.open(os.path.join(ASSETS, "textures", "block", n + ".png")).convert("RGBA") for n in (
        "realm_grass_top", "realm_grass_side", "realm_frost_top", "realm_frost_side", "realm_bog_top", "realm_bog_side", "realm_sand")]
    sheet = Image.new("RGBA", (len(tiles) * 136, 136), (40, 40, 40, 255))
    for i, t in enumerate(tiles):
        sheet.paste(t.resize((128, 128), Image.NEAREST), (i * 136 + 4, 4))
    sheet.save(path)


def main():
    turf_top("realm_frost", FROST, FROST_HEART, FROST_DARK, 51, rime=True)
    turf_side("realm_frost", FROST, "dirt", 52, rime=True)
    turf_top("realm_bog", BOG, BOG_HEART, BOG_DARK, 61, wet=True)
    turf_side("realm_bog", BOG, "mud", 62)
    realm_sand()
    data()
    if "--preview" in sys.argv:
        preview(sys.argv[sys.argv.index("--preview") + 1])


if __name__ == "__main__":
    main()
