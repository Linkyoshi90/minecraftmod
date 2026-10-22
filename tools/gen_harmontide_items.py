#!/usr/bin/env python3
"""Assets for the Harmontide items (answers of 2026-10-22): item icons (16x16), armour layers (64x32), item models,
recipes and lang entries.

    python tools/gen_harmontide_items.py

Icons are drawn from small ASCII masks (one letter per palette colour) or simple shapes, with a little noise so they
sit next to vanilla's. Armour layers paint the standard humanoid armour UVs.
"""
import json
import math
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402

REPO = H.REPO
RES = os.path.join(REPO, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "monstergirls")
DATA = os.path.join(RES, "data", "monstergirls")
CLEAR = (0, 0, 0, 0)


def rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def blank(w=16, h=16):
    return [[CLEAR] * w for _ in range(h)]


def from_mask(rows, pal, seed):
    """rows: 16 strings, '.' clear; each other letter a palette colour (hex), lightly noised and shaded top to bottom"""
    rnd = random.Random(seed)
    img = blank()
    assert len(rows) == 16, (seed, len(rows))
    for y, line in enumerate(rows):
        assert len(line) == 16, (seed, y, line)
        for x, ch in enumerate(line):
            if ch == ".":
                continue
            c = shade(rgb(pal[ch]), 1.08 - 0.16 * y / 15 + (rnd.random() - 0.5) * 0.08)
            img[y][x] = c + (255,)
    return outline(img)


def outline(img, dark=0.45):
    """a darker rim where a pixel borders transparency (the vanilla icon look)"""
    out = [row[:] for row in img]
    for y in range(16):
        for x in range(16):
            if img[y][x][3] == 0:
                continue
            edge = any(not (0 <= x + dx < 16 and 0 <= y + dy < 16) or img[y + dy][x + dx][3] == 0 for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            if edge:
                out[y][x] = shade(img[y][x][:3], dark + 0.2) + (255,)
    return out


def ball(colour, top=None, seed=0):
    """a round mochi: a soft ball, highlight top left, a little topping"""
    rnd = random.Random(seed)
    img = blank()
    c = rgb(colour)
    for y in range(16):
        for x in range(16):
            dx, dy = (x - 7.5) / 6.2, (y - 8.5) / 5.6
            d = dx * dx + dy * dy
            if d <= 1.0:
                light = 1.15 - 0.35 * (dx + dy + 1) / 2 + (rnd.random() - 0.5) * 0.05
                img[y][x] = shade(c, light) + (255,)
    for x, y in ((5, 5), (6, 5), (5, 6)):
        img[y][x] = (255, 255, 255, 255)
    if top:
        t = rgb(top)
        for x, y in ((8, 4), (9, 4), (8, 3), (10, 5)):
            img[y][x] = t + (255,)
    return outline(img)


def disc(colour, seed=0):
    """a rice cake: a flat white disc, its top face lighter than its rim"""
    rnd = random.Random(seed)
    img = blank()
    c = rgb(colour)
    for y in range(16):
        for x in range(16):
            dx = (x - 7.5) / 6.5
            top = ((y - 7.0) / 3.2) ** 2 + dx * dx <= 1.0
            side = dx * dx <= 1.0 and 7.0 <= y <= 10.5 and ((y - 10.5) / 3.2) ** 2 + dx * dx <= 1.0 or (dx * dx <= 1.0 and 7 <= y <= 10)
            if top:
                img[y][x] = shade(c, 1.05 + (rnd.random() - 0.5) * 0.06) + (255,)
                if rnd.random() < 0.12:
                    img[y][x] = shade(c, 0.9) + (255,)                 # the grain of the rice
            elif side:
                img[y][x] = shade(c, 0.8 + (rnd.random() - 0.5) * 0.05) + (255,)
    return outline(img)


ICONS = {
    "cheese": (["................",
                "................",
                "................",
                "..........aa....",
                "........aabba...",
                "......aabbbbba..",
                "....aabbbbbbbba.",
                "..aabbbbbbbbbba.",
                "..accccccccccca.",
                "..acdccccdcccca.",
                "..acccccccccdca.",
                "..accdcccccccca.",
                "..accccccdcccca.",
                "..aaaaaaaaaaaaa.",
                "................",
                "................"], {"a": "#c89a2a", "b": "#f6d870", "c": "#f0c850", "d": "#c89a3a"}),
    "spider_silk": (["................",
                     "................",
                     "....aaaaaa......",
                     "...a......aa....",
                     "..a..bbbb...a...",
                     "..a.b....b...a..",
                     "..a.b.aa..b..a..",
                     "..a.b.a.a.b..a..",
                     "..a..b..a.b..a..",
                     "...a..bb..b.a...",
                     "....a....b.a....",
                     ".....aaaa.a.....",
                     "..........a.....",
                     "...........a....",
                     "................",
                     "................"], {"a": "#f4f4f0", "b": "#d8d8d2"}),
    "merfolk_scale": (["................",
                       "................",
                       "......aaaa......",
                       ".....abbbba.....",
                       "....abccccba....",
                       "....abcddcba....",
                       "...abcddddcba...",
                       "...abcddddcba...",
                       "...abccddccba...",
                       "....abccccba....",
                       "....abbccbba....",
                       ".....abbbba.....",
                       "......abba......",
                       ".......aa.......",
                       "................",
                       "................"], {"a": "#2a6a7a", "b": "#3a9aa8", "c": "#6ac8c8", "d": "#c8f0e8"}),
    "nectar_bottle": (["................",
                       "......aaaa......",
                       "......abba......",
                       ".......cc.......",
                       ".......cc.......",
                       "......c..c......",
                       ".....cnnnnc.....",
                       "....cnnmmnnc....",
                       "....cnmmmmnc....",
                       "....cnmmmmnc....",
                       "....cnnmmnnc....",
                       "....cnnnnnnc....",
                       ".....cnnnnc.....",
                       "......cccc......",
                       "................",
                       "................"], {"a": "#8a6a3a", "b": "#b08a4a", "c": "#c8e0e8", "n": "#f0a8c0", "m": "#f8d070"}),
    "silk_cocoon": (["................",
                     ".......a........",
                     "......aba.......",
                     ".....abbba......",
                     "....abcbbba.....",
                     "....abbcbba.....",
                     "...abbbbcbba....",
                     "...abcbbbcba....",
                     "...abbcbbbba....",
                     "...abbbcbbba....",
                     "....abbbcba.....",
                     "....abcbbba.....",
                     ".....abbba......",
                     "......aaa.......",
                     ".......a........",
                     "................"], {"a": "#c8c8c0", "b": "#f8f8f2", "c": "#dcdcd4"}),
    "merfolk_bucket": (["................",
                        "................",
                        "................",
                        "...aaaaaaaaaa...",
                        "..abwwwwwwwwba..",
                        "..abwwwwtwwwba..",
                        "..abwwwwwttwba..",
                        "...abwwwwwtba...",
                        "...abbbbbbbba...",
                        "...abbbbbbbba...",
                        "....abbbbbba....",
                        "....abbbbbba....",
                        "....aaaaaaaa....",
                        "................",
                        "................",
                        "................"], {"a": "#4a4a50", "b": "#9a9aa2", "w": "#3a6ad8", "t": "#f08a3a"}),
    "nectar_bucket": (["................",
                       "................",
                       "................",
                       "...aaaaaaaaaa...",
                       "..abnnnnnnnnba..",
                       "..abnnmmnnnnba..",
                       "..abnnnnnmmnba..",
                       "...abnnnnnnba...",
                       "...abbbbbbbba...",
                       "...abbbbbbbba...",
                       "....abbbbbba....",
                       "....abbbbbba....",
                       "....aaaaaaaa....",
                       "................",
                       "................",
                       "................"], {"a": "#4a4a50", "b": "#9a9aa2", "n": "#f2b04a", "m": "#f8e0a0"}),
    "healing_balm": (["................",
                      "................",
                      "................",
                      "................",
                      ".....aaaaaa.....",
                      "....abbbbbba....",
                      "...acccccccca...",
                      "...addddddda....",
                      "...adeeeeeeda...",
                      "...adeeffeeda...",
                      "...adeeeeeeda...",
                      "...addddddda....",
                      "....aaaaaaaa....",
                      "................",
                      "................",
                      "................"], {"a": "#6a4a2a", "b": "#c8a060", "c": "#e8d8a0", "d": "#b8d8a0", "e": "#d8f0c0", "f": "#f8a8c0"}),
    "snakeskin_cloak": (["................",
                         "..aa........aa..",
                         ".abba......abba.",
                         ".abbbaaaaaabbba.",
                         ".abcbbcbbcbbcba.",
                         "..abbbbbbbbbba..",
                         "...abcbbcbbca...",
                         "...abbbbbbbba...",
                         "...acbbcbbcba...",
                         "...abbbbbbbba...",
                         "...abcbbcbbca...",
                         "...abbbbbbbba...",
                         "....aaaaaaaa....",
                         "................",
                         "................",
                         "................"], {"a": "#6a5a3a", "b": "#c8b080", "c": "#8a7448"}),
    "silk_trousers": (["................",
                       "...aaaaaaaaaa...",
                       "...abbbbbbbba...",
                       "...abccccccba...",
                       "...abbba.abba...",
                       "...abbba.abba...",
                       "...abcba.abca...",
                       "...abbba.abba...",
                       "...abbba.abba...",
                       "...abcba.abca...",
                       "...abbba.abba...",
                       "...abbba.abba...",
                       "...aaaaa.aaaa...",
                       "................",
                       "................",
                       "................"], {"a": "#b8b8b0", "b": "#f4f4ee", "c": "#dcdcd2"}),
    "silk_shoes": (["................",
                    "................",
                    "................",
                    "................",
                    "................",
                    "..aaaa....aaaa..",
                    "..abba....abba..",
                    "..abba....abba..",
                    "..abca....abca..",
                    ".aabba...aabba..",
                    ".abbbba..abbbba.",
                    ".abccba..abccba.",
                    ".aaaaaa..aaaaaa.",
                    "................",
                    "................",
                    "................"], {"a": "#b8b8b0", "b": "#f4f4ee", "c": "#dcdcd2"}),
    "flippers": (["................",
                  "................",
                  "................",
                  "...aaa....aaa...",
                  "...aba....aba...",
                  "...aba....aba...",
                  "..abba...abba...",
                  "..abcba..abcba..",
                  ".abcccba.abcccba",
                  ".abcdcba.abcdcba",
                  "abccdccbabccdcba",
                  "abcdcdcbabcdcdca",
                  "aaaaaaaa.aaaaaaa",
                  "................",
                  "................",
                  "................"], {"a": "#1e4a5a", "b": "#2a7a8a", "c": "#4aa8b0", "d": "#8ad8d0"}),
    "winged_cap": (["................",
                    "................",
                    "................",
                    "......aaaa......",
                    ".....abbbba.....",
                    "w...abbbbbba...w",
                    "ww..abbbbbba..ww",
                    "wwwabbbbbbbbawww",
                    ".wwacccccccccww.",
                    "..wwaaaaaaaaww..",
                    "...w........w...",
                    "................",
                    "................",
                    "................",
                    "................",
                    "................"], {"a": "#5a3a1e", "b": "#8a5a32", "c": "#c8a060", "w": "#f4f2ea"}),
}

BLOOM = (["................",
          "......aa.aa.....",
          ".....abbabba....",
          "....abbcbcbba...",
          "...aabcdddcbaa..",
          "...abbcdedcbba..",
          "....abcdddcba...",
          ".....abbcbba....",
          "......aabaa.....",
          ".......gg.......",
          "....ll.gg.ll....",
          "...lll.gg.lll...",
          "....llggggll....",
          "......gggg......",
          ".......gg.......",
          ".......gg......."], {"a": "#a83a6a", "b": "#f088b0", "c": "#f8b8d0", "d": "#f8e070", "e": "#fff8c0", "g": "#3a7a2a", "l": "#5aa84a"})

MOCHI = {"apple_mochi": ("#f4c8c0", "#c8302a"), "melon_mochi": ("#c8ecb0", "#e8504a"), "berry_mochi": ("#f0b0c8", "#a8203a"),
         "chocolate_mochi": ("#9a6a48", "#3a2014"), "honey_mochi": ("#f8dc90", "#e8a020"), "pumpkin_mochi": ("#f4bc78", "#4a8a2a")}

# --- armour layers (64x32): the standard humanoid UVs ----------------------------------------------------------------
HEAD_TOP = (8, 0, 16, 8)
HEAD_SIDES = (0, 8, 32, 16)
BODY_TOP = (20, 16, 28, 20)
BODY_SIDES = (16, 20, 40, 32)
ARM_TOP = (44, 16, 48, 20)
ARM_SIDES = (40, 20, 56, 32)
LEG_TOP = (4, 16, 8, 20)
LEG_BOTTOM = (8, 16, 12, 20)
LEG_SIDES = (0, 20, 16, 32)


def paint(img, box, fn, rows=None):
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        if rows and not (rows[0] <= y - y0 < rows[1]):
            continue
        for x in range(x0, x1):
            c = fn(x - x0, y - y0)
            if c:
                img[y][x] = c


def armour_layers():
    out = {}
    rnd = random.Random(7)

    def tex(base, mark, pattern):
        b, m = rgb(base), rgb(mark)

        def f(x, y):
            on = pattern(x, y)
            return shade(m if on else b, 1.0 + (rnd.random() - 0.5) * 0.08) + (255,)
        return f

    scales = lambda x, y: (x + (y // 2) % 2 * 2) % 4 == 0 or y % 2 == 0 and x % 4 == 2
    weave = lambda x, y: (x + y) % 3 == 0
    # snakeskin cloak: body and the upper arms, a scaled hide
    img = [[CLEAR] * 64 for _ in range(32)]
    paint(img, BODY_TOP, tex("#c8b080", "#8a7448", scales))
    paint(img, BODY_SIDES, tex("#c8b080", "#8a7448", scales))
    paint(img, ARM_TOP, tex("#c8b080", "#8a7448", scales))
    paint(img, ARM_SIDES, tex("#c8b080", "#8a7448", scales), rows=(0, 7))
    out["snakeskin_layer_1"] = img
    # spider silk: trousers (layer 2: the waist and legs) and shoes (layer 1: the feet)
    img = [[CLEAR] * 64 for _ in range(32)]
    paint(img, BODY_SIDES, tex("#f4f4ee", "#d8d8d0", weave), rows=(6, 12))
    paint(img, LEG_TOP, tex("#f4f4ee", "#d8d8d0", weave))
    paint(img, LEG_SIDES, tex("#f4f4ee", "#d8d8d0", weave), rows=(0, 10))
    out["spider_silk_layer_2"] = img
    img = [[CLEAR] * 64 for _ in range(32)]
    paint(img, LEG_BOTTOM, tex("#e8e8e0", "#c8c8c0", weave))
    paint(img, LEG_SIDES, tex("#f4f4ee", "#c8c8c0", weave), rows=(8, 12))
    out["spider_silk_layer_1"] = img
    # flippers: teal fins at the feet
    img = [[CLEAR] * 64 for _ in range(32)]
    paint(img, LEG_BOTTOM, tex("#2a7a8a", "#8ad8d0", lambda x, y: x % 2 == 0))
    paint(img, LEG_SIDES, tex("#2a7a8a", "#4aa8b0", lambda x, y: y % 2 == 0), rows=(9, 12))
    out["merfolk_scale_layer_1"] = img
    # winged cap: a leather cap on the crown, a white feather wing over each ear
    img = [[CLEAR] * 64 for _ in range(32)]
    paint(img, HEAD_TOP, tex("#8a5a32", "#5a3a1e", lambda x, y: x in (0, 7) or y in (0, 7)))
    paint(img, HEAD_SIDES, tex("#8a5a32", "#c8a060", lambda x, y: y == 2), rows=(0, 3))

    def wing(x, y):
        # the side faces are x 0..8 (right) and 16..24 (left): feathers fanning back from the front edge
        side = x % 8
        if (x < 8 or 16 <= x < 24) and 1 <= y <= 5 and side >= 2 + (y - 1) // 2:
            return shade((244, 242, 234), 1.0 - 0.06 * (side % 2)) + (255,)
        return None
    paint(img, HEAD_SIDES, wing)
    out["winged_cap_layer_1"] = img
    return out


def winged_cap_model():
    """GeckoLib armour model for the winged cap: a crown and visor on armorHead, a feather wing over each ear"""
    def cube(origin, size, uv, mirror=False):
        c = {"origin": origin, "size": size, "uv": uv}
        if mirror:
            c["mirror"] = True
        return c
    bones = [{"name": n, "pivot": p} for n, p in (("armorHead", [0, 24, 0]), ("armorBody", [0, 24, 0]), ("armorRightArm", [-5, 22, 0]),
                                                ("armorLeftArm", [5, 22, 0]), ("armorRightLeg", [-1.9, 12, 0]), ("armorLeftLeg", [1.9, 12, 0]),
                                                ("armorRightBoot", [-1.9, 12, 0]), ("armorLeftBoot", [1.9, 12, 0]))]
    bones.append({"name": "cap", "parent": "armorHead", "pivot": [0, 24, 0],
                  "cubes": [cube([-4.5, 31, -4.5], [9, 2, 9], [0, 0]), cube([-4.5, 30.5, -6.5], [9, 1, 2], [0, 11])]})
    bones.append({"name": "wing_l", "parent": "armorHead", "pivot": [4.6, 30, 0.5],
                  "cubes": [cube([4.6, 29, 0.5], [1, 3, 6], [0, 16]), cube([4.6, 27, 1.5], [1, 2, 5], [16, 16])]})
    bones.append({"name": "wing_r", "parent": "armorHead", "pivot": [-4.6, 30, 0.5],
                  "cubes": [cube([-5.6, 29, 0.5], [1, 3, 6], [32, 16], True), cube([-5.6, 27, 1.5], [1, 2, 5], [48, 16], True)]})
    geo = {"format_version": "1.12.0", "minecraft:geometry": [{"description": {"identifier": "geometry.winged_cap", "texture_width": 64,
                                                                              "texture_height": 32, "visible_bounds_width": 2, "visible_bounds_height": 3,
                                                                              "visible_bounds_offset": [0, 1.5, 0]}, "bones": bones}]}
    dump(os.path.join(ASSETS, "geo", "item", "armor", "winged_cap.geo.json"), geo)
    flap = {"wing_l": {"rotation": {"0.0": [0, 0, 0], "0.15": [0, -50, 30], "0.3": [0, 0, 0]}},
            "wing_r": {"rotation": {"0.0": [0, 0, 0], "0.15": [0, 50, -30], "0.3": [0, 0, 0]}}}
    rest = {"wing_l": {"rotation": [0, 10, 0]}, "wing_r": {"rotation": [0, -10, 0]}}
    dump(os.path.join(ASSETS, "animations", "item", "armor", "winged_cap.animation.json"),
         {"format_version": "1.8.0", "animations": {"animation.winged_cap.flap": {"loop": True, "animation_length": 0.3, "bones": flap},
                                                    "animation.winged_cap.rest": {"loop": True, "animation_length": 1.0, "bones": rest}}})
    rnd = random.Random(11)
    img = [[CLEAR] * 64 for _ in range(32)]

    def fill(x0, y0, w, h, col, stripe=None):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                c = rgb(stripe) if stripe and (x + y) % 4 == 0 else rgb(col)
                img[y][x] = shade(c, 1.0 + (rnd.random() - 0.5) * 0.1) + (255,)
    fill(0, 0, 36, 11, "#8a5a32", "#6a4424")          # crown
    fill(0, 11, 22, 3, "#5a3a1e")                      # visor
    for u in (0, 16, 32, 48):                          # feathers: white with grey shafts and tips
        fill(u, 16, 16, 11, "#f4f2ea", "#d8d6ce")
    for y in range(16, 27):
        for u in (0, 16, 32, 48):
            img[y][u + 15] = (168, 166, 160, 255)
    H.write_png(os.path.join(ASSETS, "textures", "item", "armor", "winged_cap.png"), img)


def recipes():
    def item(i):
        return {"item": i}

    R = {}
    R["cheese"] = {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [item("minecraft:milk_bucket")],
                   "result": {"id": "monstergirls:cheese", "count": 3}}
    R["rice_cake"] = {"type": "minecraft:crafting_shapeless", "category": "misc",
                      "ingredients": [item("minecraft:wheat"), item("minecraft:wheat"), item("minecraft:sugar")],
                      "result": {"id": "monstergirls:rice_cake", "count": 2}}
    flavour = {"apple_mochi": "minecraft:apple", "melon_mochi": "minecraft:melon_slice", "berry_mochi": "minecraft:sweet_berries",
               "chocolate_mochi": "minecraft:cocoa_beans", "honey_mochi": "minecraft:honey_bottle", "pumpkin_mochi": "minecraft:pumpkin"}
    for name, f in flavour.items():
        R[name] = {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [item("monstergirls:rice_cake"), item(f)],
                   "result": {"id": "monstergirls:" + name, "count": 1}}
    R["nectar_bucket"] = {"type": "minecraft:crafting_shapeless", "category": "misc",
                          "ingredients": [item("minecraft:bucket")] + [item("monstergirls:nectar_bottle")] * 3,
                          "result": {"id": "monstergirls:nectar_bucket", "count": 1}}
    R["healing_balm"] = {"type": "minecraft:crafting_shapeless", "category": "misc",
                         "ingredients": [item("monstergirls:nectar_bottle"), item("minecraft:honey_bottle"), {"tag": "minecraft:small_flowers"}],
                         "result": {"id": "monstergirls:healing_balm", "count": 3}}
    R["string_from_spider_silk"] = {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [item("monstergirls:spider_silk")],
                                    "result": {"id": "minecraft:string", "count": 2}}
    shaped = {
        "snakeskin_cloak": (["S S", "SSS", "SSS"], {"S": "monstergirls:shed_skin"}),
        "silk_trousers": (["SSS", "S S", "S S"], {"S": "monstergirls:spider_silk"}),
        "silk_shoes": (["S S", "S S"], {"S": "monstergirls:spider_silk"}),
        "flippers": (["S S", "S S", "L L"], {"S": "monstergirls:merfolk_scale", "L": "minecraft:leather"}),
        "winged_cap": (["FLF", "L L"], {"F": "minecraft:feather", "L": "minecraft:leather"}),
    }
    for name, (pattern, key) in shaped.items():
        R[name] = {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pattern, "key": {k: item(v) for k, v in key.items()},
                   "result": {"id": "monstergirls:" + name, "count": 1}}
    return R


LANG = {
    "item.monstergirls.cheese": "Cheese", "item.monstergirls.rice_cake": "Rice Cake",
    "item.monstergirls.apple_mochi": "Apple Mochi", "item.monstergirls.melon_mochi": "Melon Mochi", "item.monstergirls.berry_mochi": "Berry Mochi",
    "item.monstergirls.chocolate_mochi": "Chocolate Mochi", "item.monstergirls.honey_mochi": "Honey Mochi", "item.monstergirls.pumpkin_mochi": "Pumpkin Mochi",
    "item.monstergirls.spider_silk": "Spider Silk", "item.monstergirls.merfolk_scale": "Merfolk Scale", "item.monstergirls.nectar_bottle": "Bottle of Nectar",
    "item.monstergirls.silk_cocoon": "Silk Cocoon", "item.monstergirls.silk_cocoon.desc": "Something is wrapped inside. Use it to tear it open.",
    "item.monstergirls.merfolk_bucket": "Bucket of Merfolk", "item.monstergirls.snakeskin_cloak": "Snakeskin Cloak",
    "item.monstergirls.silk_trousers": "Silk Trousers", "item.monstergirls.silk_shoes": "Silk Shoes", "item.monstergirls.flippers": "Flippers",
    "item.monstergirls.winged_cap": "Winged Cap",
    "block.monstergirls.alraune_bloom": "Alraune Bloom",
    "block.monstergirls.nectar": "Nectar", "item.monstergirls.nectar_bucket": "Bucket of Nectar",
    "entity.monstergirls.merfolk_merchant": "%s, merchant of the sea",
    "message.monstergirls.winded": "%s is out of breath and slows to a walk",
    "item.monstergirls.healing_balm": "Healing Balm",
    "message.monstergirls.balm_refused": "%s will not let you near enough (befriend her first)",
    "message.monstergirls.balm_unhurt": "%s is not hurt",
}


def main():
    names = []
    for name, (rows, pal) in ICONS.items():
        H.write_png(os.path.join(ASSETS, "textures", "item", name + ".png"), from_mask(rows, pal, name))
        names.append(name)
    H.write_png(os.path.join(ASSETS, "textures", "item", "rice_cake.png"), disc("#f4f2ea", 3))
    names.append("rice_cake")
    for name, (body, top) in MOCHI.items():
        H.write_png(os.path.join(ASSETS, "textures", "item", name + ".png"), ball(body, top, hash(name) & 255))
        names.append(name)
    for name in names:
        dump(os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:item/" + name}})
    # the alraune bloom: a cross-model flower block and its item
    H.write_png(os.path.join(ASSETS, "textures", "block", "alraune_bloom.png"), from_mask(*BLOOM, "alraune_bloom"))
    dump(os.path.join(ASSETS, "blockstates", "alraune_bloom.json"), {"variants": {"": {"model": "monstergirls:block/alraune_bloom"}}})
    dump(os.path.join(ASSETS, "models", "block", "alraune_bloom.json"),
         {"parent": "minecraft:block/cross", "render_type": "minecraft:cutout", "textures": {"cross": "monstergirls:block/alraune_bloom"}})
    dump(os.path.join(ASSETS, "models", "item", "alraune_bloom.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:block/alraune_bloom"}})
    dump(os.path.join(DATA, "loot_table", "blocks", "alraune_bloom.json"), {"type": "minecraft:block", "pools": []})   # she steps out instead
    winged_cap_model()
    # the nectar fluid's block: rendered by the fluid renderer, the model only gives the breaking particle
    dump(os.path.join(ASSETS, "blockstates", "nectar.json"), {"variants": {"": {"model": "monstergirls:block/nectar"}}})
    dump(os.path.join(ASSETS, "models", "block", "nectar.json"), {"textures": {"particle": "minecraft:block/water_still"}})
    dump(os.path.join(DATA, "loot_table", "blocks", "nectar.json"), {"type": "minecraft:block", "pools": []})
    for name, img in armour_layers().items():
        H.write_png(os.path.join(ASSETS, "textures", "models", "armor", name + ".png"), img)
    for name, r in recipes().items():
        dump(os.path.join(DATA, "recipe", name + ".json"), r)
    import collections
    path = os.path.join(ASSETS, "lang", "en_us.json")
    cur = json.load(open(path, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
    cur.update(LANG)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(json.dumps(cur, indent=2, ensure_ascii=False) + "\n")
    print(f"{len(names)} item icons, {len(armour_layers())} armour layers, {len(recipes())} recipes, {len(LANG)} lang keys")


if __name__ == "__main__":
    main()
