#!/usr/bin/env python3
"""The realm of the dead (2026-11-13, block/GraveGrassBlock, entity/DeadRealm): grave grass textures (ashen grey-green turf with
pale bone flecks and a few faint soul-blue motes, over vanilla dirt), model, blockstate, loot, tags, advancement, lang.

    uv run --no-project --with pillow python tools/gen_dead_realm_assets.py
"""
import json
import os
import random
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_paints import _vanilla_rgba  # noqa: E402
from gen_realm_assets import ASSETS, DATA, MC_DATA, rgb, shade, dump, save  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402

ASH = [rgb("#4A524C"), rgb("#56605A"), rgb("#646E66"), rgb("#727C72"), rgb("#868E84")]
BONE, SOUL = rgb("#D8D4C4"), rgb("#7AD8E0")


def pick(pal, rnd, mid=2.0, spread=1.1):
    return pal[min(len(pal) - 1, max(0, int(rnd.gauss(mid, spread))))]


def top():
    rnd = random.Random(1311)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = pick(ASH, rnd)
            if rnd.random() < 0.04:
                px[x, y] = BONE                                          # bone flecks
    for _ in range(3):
        x, y = rnd.randrange(1, 15), rnd.randrange(1, 15)
        px[x, y] = SOUL                                                  # a faint soul mote
    return save(img, "block", "grave_grass_top.png")


def side():
    rnd = random.Random(1312)
    base = _vanilla_rgba("dirt")
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            r, g, b = base[y][x][:3]
            px[x, y] = (int(r * 0.8), int(g * 0.8), int(b * 0.82), 255)  # the dirt under the dead is drained a little
    for x in range(16):
        depth = 3 + rnd.choice((0, 0, 1, 1, 2)) - (1 if rnd.random() < 0.25 else 0)
        for y in range(depth):
            px[x, y] = pick(ASH, rnd)
        if rnd.random() < 0.3:
            px[x, depth] = shade(ASH[1], 0.8)
    return save(img, "block", "grave_grass_side.png")


def data():
    m = os.path.join(ASSETS, "models")
    dump(os.path.join(m, "block", "grave_grass.json"), {
        "parent": "minecraft:block/cube_bottom_top",
        "textures": {"top": "harmontide:block/grave_grass_top", "bottom": "minecraft:block/dirt", "side": "harmontide:block/grave_grass_side",
                     "particle": "minecraft:block/dirt"}})
    rot = [{"model": "harmontide:block/grave_grass"}, {"model": "harmontide:block/grave_grass", "y": 90},
           {"model": "harmontide:block/grave_grass", "y": 180}, {"model": "harmontide:block/grave_grass", "y": 270}]
    dump(os.path.join(ASSETS, "blockstates", "grave_grass.json"), {"variants": {"snowy=false": rot, "snowy=true": {"model": "minecraft:block/grass_block_snow"}}})
    dump(os.path.join(m, "item", "grave_grass.json"), {"parent": "harmontide:block/grave_grass"})
    silk = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
        {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
    dump(os.path.join(DATA, "loot_table", "blocks", "grave_grass.json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [
        {"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "conditions": [silk], "name": "harmontide:grave_grass"},
            {"type": "minecraft:item", "conditions": [{"condition": "minecraft:survives_explosion"}], "name": "minecraft:dirt"}]}]}]})
    for tag in ("dirt", "mineable/shovel"):
        path = os.path.join(MC_DATA, "tags", "block", tag + ".json")
        old = json.load(open(path, encoding="utf-8")) if os.path.exists(path) else {"replace": False, "values": []}
        if "harmontide:grave_grass" not in old["values"]:
            old["values"].append("harmontide:grave_grass")
        dump(path, old)
    dump(os.path.join(DATA, "advancement", "dead_realm_entered.json"), {
        "parent": "minecraft:adventure/root",
        "display": {"icon": {"id": "harmontide:grave_grass"}, "title": {"translate": "advancement.harmontide.dead_realm_entered"},
                    "description": {"translate": "advancement.harmontide.dead_realm_entered.desc"},
                    "frame": "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
    add_lang({
        "block.harmontide.grave_grass": "Grave Grass",
        "advancement.harmontide.dead_realm_entered": "Where the Dead Walk",
        "advancement.harmontide.dead_realm_entered.desc": "Set foot on the grave grass of the realm of the dead",
        "journal.harmontide.guide.dead_realm.title": "The realm of the dead",
        "journal.harmontide.guide.dead_realm": "Where a wild lich or koschei makes her home, the ground turns to ashen grave grass and a grey mist hangs over it. "
            "It spreads in the dark, under trees and roofs and at night, but the open sun withers it back to its heart by day, and lamplight, holy water "
            "and the Order's sisters cleanse it. Zombies and skeletons that linger on it in the dark rise again as undead girls, and deep in it, at night, "
            "one of the dead sometimes climbs out of the ground. Undead friends standing on it are at their strongest. Befriend its lich, and it stops growing.",
    })


def main():
    top()
    side()
    data()


if __name__ == "__main__":
    main()
