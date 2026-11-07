#!/usr/bin/env python3
"""Assets for the Demon Realm (2026-11-07, entity/DemonRealm): realm grass, the heartfruit bush (four stages), the heartfruit and
holy water items, their models, blockstates, loot tables, the holy water recipe and the block tags.

    uv run --no-project --with pillow python tools/gen_realm_assets.py [--preview previews/realm_assets.png]

Realm grass is a deep magenta turf with a few tiny pink hearts in it, over vanilla dirt (read from the local 1.21.1 client jar,
as gen_paints does). The heartfruit is a glossy red-pink heart on a little stem.
"""
import json
import os
import random
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_paints import _vanilla_rgba  # noqa: E402

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(REPO, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "monstergirls")
DATA = os.path.join(RES, "data", "monstergirls")
MC_DATA = os.path.join(RES, "data", "minecraft")


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


def save(img, *parts):
    path = os.path.join(ASSETS, "textures", *parts)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    return img


TURF = [rgb("#5A1E5E"), rgb("#6E2A72"), rgb("#82347E"), rgb("#963E8A"), rgb("#A84C98")]
HEART, HEART_DARK, SHINE = rgb("#F25C9A"), rgb("#B8285E"), rgb("#FFD0E4")
HEART_PX = [(1, 0), (3, 0), (0, 1), (1, 1), (2, 1), (3, 1), (4, 1), (1, 2), (2, 2), (3, 2), (2, 3)]   # a 5x4 heart


def turf_pixel(rnd):
    return TURF[min(len(TURF) - 1, max(0, int(rnd.gauss(2, 1.1))))]


def heart(px, ox, oy, colour=HEART, dark=None, shine=None):
    for x, y in HEART_PX:
        c = colour if dark is None or y < 2 or x == 2 else dark
        px[ox + x, oy + y] = c
    if shine:
        px[ox + 1, oy + 1] = shine


def realm_top():
    rnd = random.Random(31)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = turf_pixel(rnd)
    for ox, oy in ((2, 3), (10, 10)):
        heart(px, ox, oy, colour=shade(HEART, 0.92), dark=HEART_DARK)
    return save(img, "block", "realm_grass_top.png")


def realm_side():
    rnd = random.Random(32)
    dirt = _vanilla_rgba("dirt")
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = tuple(dirt[y][x][:3]) + (255,)
    for x in range(16):
        depth = 3 + rnd.choice((0, 0, 1, 1, 2)) - (1 if rnd.random() < 0.25 else 0)
        for y in range(depth):
            px[x, y] = turf_pixel(rnd)
        if rnd.random() < 0.3:
            px[x, depth] = shade(TURF[1], 0.85)           # a strand hanging over the dirt
    return save(img, "block", "realm_grass_side.png")


LEAF = [rgb("#2E3A2A"), rgb("#3E4E34"), rgb("#4E5E3E"), rgb("#6A3A5A")]


def bush(stage):
    rnd = random.Random(40 + stage)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    top = (11, 7, 3, 1)[stage]                             # how high the leaves reach
    for y in range(top, 16):
        half = 2 + (y - top) * (6 if stage else 4) // max(1, 16 - top)
        for x in range(8 - half, 8 + half):
            if rnd.random() < 0.72:
                px[x, y] = LEAF[min(3, int(rnd.random() * (4 if stage >= 2 else 3)))]
    for x in (7, 8):
        for y in range(13, 16):
            px[x, y] = rgb("#3A2418")                      # the stem
    if stage == 2:
        for ox, oy in ((4, 6), (9, 9)):
            for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
                px[ox + dx, oy + dy] = shade(HEART, 0.8)  # buds
    if stage == 3:
        for ox, oy in ((2, 4), (9, 3), (6, 10)):
            heart(px, ox, oy, colour=HEART, dark=HEART_DARK, shine=SHINE)
    return save(img, "block", f"heartfruit_bush_stage{stage}.png")


def heartfruit():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    shape = ["..XXX...XXX..",
             ".XXXXX.XXXXX.",
             "XXXXXXXXXXXXX",
             "XXXXXXXXXXXXX",
             "XXXXXXXXXXXXX",
             ".XXXXXXXXXXX.",
             "..XXXXXXXXX..",
             "...XXXXXXX...",
             "....XXXXX....",
             ".....XXX.....",
             "......X......"]
    ox, oy = 1, 4
    for y, row in enumerate(shape):
        for x, ch in enumerate(row):
            if ch != "X":
                continue
            light = 1.15 - 0.05 * x - 0.03 * y          # lit from the upper left
            px[ox + x, oy + y] = shade(HEART, max(0.62, min(1.12, light)))
    for x, y in ((3, 6), (4, 6), (3, 7), (4, 5)):
        px[x, y] = SHINE
    # the outline, dark
    edge = []
    for y in range(16):
        for x in range(16):
            if px[x, y][3]:
                continue
            if any(0 <= x + dx < 16 and 0 <= y + dy < 16 and px[x + dx, y + dy][3] and px[x + dx, y + dy] != rgb("#5A1030")
                   for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                edge.append((x, y))
    for x, y in edge:
        px[x, y] = rgb("#5A1030")
    for x, y in ((7, 3), (7, 2), (8, 1)):
        px[x, y] = rgb("#3A2418")                      # stem
    for x, y in ((9, 1), (10, 1), (10, 2), (11, 2)):
        px[x, y] = rgb("#4E7A3A")                      # leaf
    return save(img, "item", "heartfruit.png")


def holy_water():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    glass, glass_dark, cork = rgb("#D8E8F0"), rgb("#8AA4B4"), rgb("#9A6A3A")
    water, water_light = rgb("#7AC8F0"), rgb("#C8F0FF")
    for y in range(1, 4):
        for x in range(7, 10):
            px[x, y] = cork
    for y in range(4, 7):
        for x in (6, 10):
            px[x, y] = glass_dark
    for y in range(7, 15):
        width = 4 if y in (7, 14) else 5
        for x in range(8 - width, 9 + width):
            edge = x in (8 - width, 8 + width) or y == 14
            if edge:
                px[x, y] = glass_dark
            else:
                px[x, y] = water if y > 8 else glass
    for x, y in ((5, 10), (5, 11), (6, 9)):
        px[x, y] = water_light
    gold = rgb("#F2C14E")
    for x, y in ((8, 9), (7, 10), (8, 10), (9, 10), (8, 11), (8, 12)):    # a little gold star
        px[x, y] = gold if (x, y) != (8, 12) else shade(gold, 0.8)
    for y in range(4, 7):
        for x in range(7, 10):
            px[x, y] = glass
    return save(img, "item", "holy_water.png")


def data():
    m = os.path.join(ASSETS, "models")
    dump(os.path.join(m, "block", "realm_grass.json"), {
        "parent": "minecraft:block/cube_bottom_top",
        "textures": {"top": "monstergirls:block/realm_grass_top", "bottom": "minecraft:block/dirt",
                     "side": "monstergirls:block/realm_grass_side", "particle": "minecraft:block/dirt"}})
    dump(os.path.join(ASSETS, "blockstates", "realm_grass.json"), {"variants": {
        "snowy=false": [{"model": "monstergirls:block/realm_grass"}, {"model": "monstergirls:block/realm_grass", "y": 90},
                        {"model": "monstergirls:block/realm_grass", "y": 180}, {"model": "monstergirls:block/realm_grass", "y": 270}],
        "snowy=true": {"model": "minecraft:block/grass_block_snow"}}})
    dump(os.path.join(m, "item", "realm_grass.json"), {"parent": "monstergirls:block/realm_grass"})
    for s in range(4):
        dump(os.path.join(m, "block", f"heartfruit_bush_stage{s}.json"), {
            "parent": "minecraft:block/cross", "render_type": "minecraft:cutout",
            "textures": {"cross": f"monstergirls:block/heartfruit_bush_stage{s}"}})
    dump(os.path.join(ASSETS, "blockstates", "heartfruit_bush.json"),
         {"variants": {f"age={s}": {"model": f"monstergirls:block/heartfruit_bush_stage{s}"} for s in range(4)}})
    for item in ("heartfruit", "holy_water"):
        dump(os.path.join(m, "item", f"{item}.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"monstergirls:item/{item}"}})

    silk = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
        {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
    dump(os.path.join(DATA, "loot_table", "blocks", "realm_grass.json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [
        {"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "conditions": [silk], "name": "monstergirls:realm_grass"},
            {"type": "minecraft:item", "conditions": [{"condition": "minecraft:survives_explosion"}], "name": "minecraft:dirt"}]}]}]})
    ripe = lambda age: {"condition": "minecraft:block_state_property", "block": "monstergirls:heartfruit_bush", "properties": {"age": str(age)}}
    dump(os.path.join(DATA, "loot_table", "blocks", "heartfruit_bush.json"), {"type": "minecraft:block", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "monstergirls:heartfruit"}], "conditions": [{"condition": "minecraft:survives_explosion"}]},
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "monstergirls:heartfruit",
                                  "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]}],
         "conditions": [{"condition": "minecraft:any_of", "terms": [ripe(2), ripe(3)]}]}]})
    dump(os.path.join(DATA, "recipe", "holy_water.json"), {
        "type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [{"item": "minecraft:glass_bottle"}, {"item": "minecraft:glass_bottle"}, {"item": "minecraft:glass_bottle"},
                        {"item": "minecraft:water_bucket"}, {"item": "minecraft:gold_nugget"}],
        "result": {"id": "monstergirls:holy_water", "count": 3}})
    for tag, entries in (("dirt", ["monstergirls:realm_grass"]), ("mineable/shovel", ["monstergirls:realm_grass"])):
        path = os.path.join(MC_DATA, "tags", "block", tag + ".json")
        old = json.load(open(path, encoding="utf-8")) if os.path.exists(path) else {"replace": False, "values": []}
        for e in entries:
            if e not in old["values"]:
                old["values"].append(e)
        dump(path, old)


LEAF_TINT = (0.86, 0.42, 0.90)


def realm_leaves():
    """vanilla oak leaves (greyscale, normally tinted green) dyed dusky purple, with a few beads of violet sap"""
    rnd = random.Random(51)
    src = _vanilla_rgba("oak_leaves")
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(16):
        for x in range(16):
            r, g, b, a = src[y][x]
            if a == 0:
                continue
            v = (r + g + b) / 3
            px[x, y] = (int(v * LEAF_TINT[0]), int(v * LEAF_TINT[1]), int(v * LEAF_TINT[2]), 255)
    for _ in range(5):
        x, y = rnd.randrange(16), rnd.randrange(16)
        if px[x, y][3]:
            px[x, y] = (210, 90, 220, 255)
    return save(img, "block", "realm_leaves.png")


def weepcap():
    """a wide drooping bell of a cap on a thin stem, its frilled rim hanging in strands, droplets falling from them"""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    cap, cap_dark, gill, glow, stem, drop = rgb("#CFE6F2"), rgb("#9CC4E0"), rgb("#7FA6C8"), rgb("#F2FCFF"), rgb("#E4EEF2"), rgb("#BFE8FF")
    rows = {2: (5, 10), 3: (3, 12), 4: (2, 13), 5: (1, 14), 6: (1, 14), 7: (1, 14)}
    for y, (a, b) in rows.items():
        for x in range(a, b + 1):
            px[x, y] = cap if x < 9 or y < 4 else cap_dark
    for x in range(2, 14):
        px[x, 8] = gill
    for x in (1, 3, 5, 10, 12, 14):                                      # frilled strands hanging from the rim
        px[x, 8] = cap_dark
        px[x, 9] = cap_dark
    for x, y in ((6, 3), (4, 5), (10, 4), (8, 6)):
        px[x, y] = glow
    for y in range(9, 16):
        px[7, y] = stem
        px[8, y] = shade(stem, 0.85)
    for x, y in ((1, 11), (5, 12), (14, 11), (12, 13), (3, 14)):          # droplets
        px[x, y] = drop
    return save(img, "block", "weepcap.png")


def realm_plant_data():
    m = os.path.join(ASSETS, "models")
    dump(os.path.join(m, "block", "realm_leaves.json"), {"parent": "minecraft:block/cube_all", "render_type": "minecraft:cutout_mipped",
                                                        "textures": {"all": "monstergirls:block/realm_leaves"}})
    dump(os.path.join(ASSETS, "blockstates", "realm_leaves.json"), {"variants": {"": {"model": "monstergirls:block/realm_leaves"}}})
    dump(os.path.join(m, "item", "realm_leaves.json"), {"parent": "monstergirls:block/realm_leaves"})
    dump(os.path.join(m, "block", "weepcap.json"), {"parent": "minecraft:block/cross", "render_type": "minecraft:cutout",
                                                   "textures": {"cross": "monstergirls:block/weepcap"}})
    dump(os.path.join(ASSETS, "blockstates", "weepcap.json"), {"variants": {"": {"model": "monstergirls:block/weepcap"}}})
    dump(os.path.join(m, "item", "weepcap.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:block/weepcap"}})
    silk_or_shears = {"condition": "minecraft:any_of", "terms": [
        {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}},
        {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]}
    dump(os.path.join(DATA, "loot_table", "blocks", "realm_leaves.json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "conditions": [silk_or_shears], "name": "monstergirls:realm_leaves"},
            {"type": "minecraft:item", "conditions": [{"condition": "minecraft:survives_explosion"},
                                                      {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [0.02, 0.022, 0.025, 0.033, 0.1]}],
             "name": "monstergirls:heartfruit"}]}]}]})
    dump(os.path.join(DATA, "loot_table", "blocks", "weepcap.json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": "monstergirls:weepcap"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    for tag, entries in (("leaves", ["monstergirls:realm_leaves"]), ("mineable/hoe", ["monstergirls:realm_leaves"])):
        path = os.path.join(MC_DATA, "tags", "block", tag + ".json")
        old = json.load(open(path, encoding="utf-8")) if os.path.exists(path) else {"replace": False, "values": []}
        for e in entries:
            if e not in old["values"]:
                old["values"].append(e)
        dump(path, old)


def realm_log():
    """vanilla oak bark darkened to near-black plum, split by glowing violet veins; the cut end gets violet rings"""
    rnd = random.Random(61)
    side_src, top_src = _vanilla_rgba("oak_log"), _vanilla_rgba("oak_log_top")
    side, top = Image.new("RGBA", (16, 16)), Image.new("RGBA", (16, 16))
    sp, tp = side.load(), top.load()
    for y in range(16):
        for x in range(16):
            v = sum(side_src[y][x][:3]) / 3
            sp[x, y] = (int(v * 0.42), int(v * 0.24), int(v * 0.40), 255)
            t = sum(top_src[y][x][:3]) / 3
            tp[x, y] = (int(t * 0.55), int(t * 0.30), int(t * 0.52), 255)
    for vx in (3, 9, 13):                                                  # veins running up the bark, wandering a little
        x = vx
        for y in range(16):
            sp[x % 16, y] = (196, 92, 220, 255) if y % 5 else (240, 150, 250, 255)
            if rnd.random() < 0.3:
                x += rnd.choice((-1, 1))
    for y in range(16):
        for x in range(16):
            r = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if 2.5 < r < 3.3 or 5.2 < r < 5.9:
                tp[x, y] = (170, 80, 190, 255)
    save(side, "block", "realm_log.png")
    save(top, "block", "realm_log_top.png")
    m = os.path.join(ASSETS, "models")
    dump(os.path.join(m, "block", "realm_log.json"), {"parent": "minecraft:block/cube_column",
                                                     "textures": {"end": "monstergirls:block/realm_log_top", "side": "monstergirls:block/realm_log"}})
    dump(os.path.join(m, "block", "realm_log_horizontal.json"), {"parent": "minecraft:block/cube_column_horizontal",
                                                                "textures": {"end": "monstergirls:block/realm_log_top", "side": "monstergirls:block/realm_log"}})
    dump(os.path.join(ASSETS, "blockstates", "realm_log.json"), {"variants": {
        "axis=x": {"model": "monstergirls:block/realm_log_horizontal", "x": 90, "y": 90},
        "axis=y": {"model": "monstergirls:block/realm_log"},
        "axis=z": {"model": "monstergirls:block/realm_log_horizontal", "x": 90}}})
    dump(os.path.join(m, "item", "realm_log.json"), {"parent": "monstergirls:block/realm_log"})
    dump(os.path.join(DATA, "loot_table", "blocks", "realm_log.json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": "monstergirls:realm_log"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    for tag in ("logs", "logs_that_burn", "mineable/axe"):
        path = os.path.join(MC_DATA, "tags", "block", tag + ".json")
        old = json.load(open(path, encoding="utf-8")) if os.path.exists(path) else {"replace": False, "values": []}
        if "monstergirls:realm_log" not in old["values"]:
            old["values"].append("monstergirls:realm_log")
        dump(path, old)
    return side, top


def weepcap_stew():
    """a wooden bowl of pale blue broth with a weepcap cap floating in it"""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    wood, wood_dark, broth, cap = rgb("#8A5A30"), rgb("#5A3A1E"), rgb("#A8D8F0"), rgb("#E4F2FA")
    for y in range(8, 14):
        half = 7 - (y - 8) // 2 if y > 9 else 7
        for x in range(8 - half, 8 + half):
            px[x, y] = wood if y > 8 else broth
    for x in range(2, 14):
        px[x, 8] = broth
        px[x, 9] = broth if 3 <= x <= 12 else wood_dark
    for x in range(5, 11):
        px[x, 7] = cap
    for x in (6, 7, 8, 9):
        px[x, 6] = cap
    for x in range(4, 12):
        px[x, 13] = wood_dark
    return save(img, "item", "weepcap_stew.png")


def preview(path):
    tiles = [realm_top(), realm_side(), realm_leaves(), *realm_log(), weepcap(), weepcap_stew()] + [bush(s) for s in range(4)] + [heartfruit(), holy_water()]
    sheet = Image.new("RGBA", (len(tiles) * 18 * 6, 18 * 6), (40, 40, 48, 255))
    for i, t in enumerate(tiles):
        sheet.paste(t.resize((96, 96), Image.NEAREST), (i * 108 + 6, 6), t.resize((96, 96), Image.NEAREST))
    os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
    sheet.save(path)


def main():
    realm_top()
    realm_side()
    for s in range(4):
        bush(s)
    heartfruit()
    holy_water()
    realm_leaves()
    weepcap()
    data()
    realm_plant_data()
    realm_log()
    weepcap_stew()
    if "--preview" in sys.argv:
        preview(sys.argv[sys.argv.index("--preview") + 1])


if __name__ == "__main__":
    main()
