#!/usr/bin/env python3
"""Assets for the thirty-fourth wave, life at home: block and item textures, block and item models, blockstates, loot tables, recipes,
the pickaxe and axe tags, and lang entries.

    python tools/gen_home_assets.py

The textures are small painted 16x16 tiles with a little noise, the same way gen_harmontide_items.py draws its icons. Can be run twice.
"""
import json
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_harmontide_items as I  # noqa: E402
import gen_milk_assets as M  # noqa: E402
import harpy_lib as H  # noqa: E402

ASSETS, DATA = I.ASSETS, I.DATA
CLEAR = (0, 0, 0, 0)
MC = os.path.join(I.RES, "data", "minecraft", "tags", "block", "mineable")


def rgb(h, n=0):
    h = h.lstrip("#")
    return tuple(min(255, max(0, int(h[i:i + 2], 16) + n)) for i in (0, 2, 4)) + (255,)


def tile(name, fn, folder="block", seed=0):
    random.seed(hash(name) % 1000 + seed if False else sum(map(ord, name)) + seed)
    rows = [[fn(x, y) for x in range(16)] for y in range(16)]
    H.write_png(os.path.join(ASSETS, "textures", folder, name + ".png"), rows)


def speck(col, spread=8):
    return rgb(col, random.randint(-spread, spread))


def dump(path, doc):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="") as fh:
        json.dump(doc, fh, indent=2)
        fh.write("\n")


def cube(frm, to, tex, tint=None):
    faces = {f: {"texture": "#" + tex} for f in ("down", "up", "north", "south", "west", "east")}
    return {"from": frm, "to": to, "faces": faces}


def model(name, textures, elements, parent="minecraft:block/block"):
    dump(os.path.join(ASSETS, "models", "block", name + ".json"), {"parent": parent, "textures": textures, "elements": elements})


def simple_state(name, variants=None):
    dump(os.path.join(ASSETS, "blockstates", name + ".json"), {"variants": variants or {"": {"model": f"monstergirls:block/{name}"}}})


def block_item(name, mod=None):
    dump(os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": f"monstergirls:block/{mod or name}"})


def plain_item(name):
    dump(os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"monstergirls:item/{name}"}})


def loot(name):
    dump(os.path.join(DATA, "loot_table", "blocks", name + ".json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"monstergirls:{name}"}],
         "conditions": [{"condition": "minecraft:survives_explosion"}]}]})


def shaped(name, pattern, key, count=1):
    dump(os.path.join(DATA, "recipe", name + ".json"), {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
         "key": {k: {"item": v} if ":" in v and not v.startswith("#") else {"tag": v[1:]} for k, v in key.items()}, "result": {"id": f"monstergirls:{name}", "count": count}})


def tag_add(path, names):
    with open(path, encoding="utf-8") as fh:
        doc = json.load(fh) if os.path.getsize(path) else {"replace": False, "values": []}
    for n in names:
        if n not in doc["values"]:
            doc["values"].append(n)
    dump(path, doc)


# --- tiles ------------------------------------------------------------------------------------------------------------
def blanket(x, y):
    red, white = "#c83a3a", "#f0e8dc"
    on = (x // 4 + y // 4) % 2 == 0
    return speck(red if on else white, 6)


def pot_side(x, y):
    if y <= 1:
        return speck("#6a6e78", 6)
    return speck("#3a3e46", 6) if x % 5 else speck("#2e3238", 4)


def pot_top(x, y):
    d = max(abs(x - 7.5), abs(y - 7.5))
    return speck("#6a6e78", 6) if d > 5.5 else speck("#8a5a2a", 10)


def wood(x, y):
    return speck("#a87a4a" if (y // 4) % 2 else "#9a6e42", 8)


def band(x, y):
    return speck("#6a6e78", 6) if y in (2, 3, 12, 13) else wood(x, y)


def water(x, y):
    return speck("#4a9ad8", 10)


def glass(x, y):
    base = "#c8dcec" if (x + y) % 9 > 1 else "#f4fafe"
    return speck(base, 4)


def frame(x, y):
    return speck("#7a4a24", 8)


def straw(x, y):
    return speck("#d8b860" if (y + x // 3) % 3 else "#b8944a", 10)


def rope(x, y):
    return speck("#8a6a3e", 8)


for name, fn in [("picnic_blanket", blanket), ("cooking_pot_side", pot_side), ("cooking_pot_top", pot_top), ("soaking_tub_side", band), ("soaking_tub_wood", wood),
                 ("soaking_tub_water", water), ("vanity_mirror_glass", glass), ("vanity_mirror_frame", frame), ("sparring_post_straw", straw), ("sparring_post_wood", rope)]:
    tile(name, fn)


def icon(name, mask, cols):
    def fn(x, y):
        c = mask[y][x]
        return CLEAR if c == "." else speck(cols[c], 5)
    tile(name, fn, "item")


icon("grooming_brush", [
    "................", "................", "..........hhh...", ".........hhhhh..", "........bbbhhh..", ".......bbbbbh...", "......bbbbb.....", ".....wbbbb......",
    "....wwbb........", "...www..........", "..www...........", ".www............", ".ww.............", "................", "................", "................"],
     dict(h="#d8c8a0", b="#7a4a24", w="#a87a4a"))
icon("toy_ball", [
    "................", ".....rrrrrr.....", "...rrrrrrrrrr...", "..rrwwrrrrrrrr..", "..rwwrrrrrrrrr..", ".rrrrrrrrrrrrrr.", ".yyyyyyyyyyyyyy.", ".yyyyyyyyyyyyyy.",
    ".bbbbbbbbbbbbbb.", ".bbbbbbbbbbbbbb.", ".bbbbbbbbbbbbbb.", "..bbbbbbbbbbbb..", "..bbbbbbbbbbbb..", "...bbbbbbbbbb...", ".....bbbbbb.....", "................"],
     dict(r="#d83a3a", w="#f8f4ec", y="#f0c840", b="#3a6ac8"))
icon("scout_pack", [
    "................", ".....ssssss.....", "....s......s....", "...llllllllll...", "..lLLLLLLLLLLl..", "..lLLLLLLLLLLl..", "..lLLLbbbbLLLl..", "..llllbggbllll..",
    "..lLLLbbbbLLLl..", "..lLLLLLLLLLLl..", "..lLLLLLLLLLLl..", "..lLLLLLLLLLLl..", "..lLLLLLLLLLLl..", "...llllllllll...", "................", "................"],
     dict(s="#5a3a1e", l="#6a4426", L="#9a6a3a", b="#5a3a1e", g="#e8c050"))
icon("friend_whistle", [
    "................", "................", "................", "................", "...sssssssss....", "..sggggggggss...", "..sggkkkggggss..", "..sggggggggggs..",
    "...sssssggggs...", ".......sggs.....", "........ss......", "................", "................", "................", "................", "................"],
     dict(s="#8a6a2a", g="#e8c050", k="#2a2020"))
icon("homemade_meal", [
    "................", "................", "......s.s.......", ".......s.s......", "....oooccgoo....", "...occggccgcco..", "..wocgccggcccow.", "..wwoooooooooww.",
    "..wwwwwwwwwwwww.", "...wwwwwwwwwww..", "....wwwwwwwww...", ".....wwwwwww....", "......wwwww.....", "................", "................", "................"],
     dict(s="#d8d8d8", o="#c8903a", c="#e8c060", g="#5a9a3a", w="#8a5a2a"))

# --- models -----------------------------------------------------------------------------------------------------------
model("picnic_blanket", {"particle": "monstergirls:block/picnic_blanket", "all": "monstergirls:block/picnic_blanket"}, [cube([0, 0, 0], [16, 1, 16], "all")])
simple_state("picnic_blanket")
pot = {"particle": "monstergirls:block/cooking_pot_side", "side": "monstergirls:block/cooking_pot_side", "top": "monstergirls:block/cooking_pot_top"}
pe = cube([2, 0, 2], [14, 10, 14], "side")
pe["faces"]["up"] = {"texture": "#top"}
model("cooking_pot", pot, [pe, cube([0, 7, 6], [2, 8, 10], "side"), cube([14, 7, 6], [16, 8, 10], "side")])
simple_state("cooking_pot")
tub = {"particle": "monstergirls:block/soaking_tub_side", "side": "monstergirls:block/soaking_tub_side", "wood": "monstergirls:block/soaking_tub_wood", "water": "monstergirls:block/soaking_tub_water"}
walls = [cube([0, 0, 0], [16, 2, 16], "wood"), cube([0, 0, 0], [16, 8, 2], "side"), cube([0, 0, 14], [16, 8, 16], "side"), cube([0, 0, 2], [2, 8, 14], "side"), cube([14, 0, 2], [16, 8, 14], "side")]
model("soaking_tub", tub, walls)
model("soaking_tub_filled", tub, walls + [cube([2, 2, 2], [14, 7, 14], "water")])
simple_state("soaking_tub", {"filled=false": {"model": "monstergirls:block/soaking_tub"}, "filled=true": {"model": "monstergirls:block/soaking_tub_filled"}})
mirror = {"particle": "monstergirls:block/vanity_mirror_frame", "frame": "monstergirls:block/vanity_mirror_frame", "glass": "monstergirls:block/vanity_mirror_glass"}
model("vanity_mirror", mirror, [cube([1, 0, 6], [15, 2, 10], "frame"), cube([2, 2, 7], [14, 16, 9], "frame"), cube([3, 3, 6.5], [13, 15, 9.5], "glass")])
simple_state("vanity_mirror", {f"facing={f}": {"model": "monstergirls:block/vanity_mirror", "y": r} for f, r in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]})
post = {"particle": "monstergirls:block/sparring_post_straw", "straw": "monstergirls:block/sparring_post_straw", "wood": "monstergirls:block/sparring_post_wood"}
model("sparring_post", post, [cube([6, 0, 6], [10, 16, 10], "wood"), cube([4, 4, 4], [12, 13, 12], "straw"), cube([0, 9, 7], [16, 11, 9], "wood")])
simple_state("sparring_post")
for name in ("picnic_blanket", "cooking_pot", "soaking_tub", "vanity_mirror", "sparring_post"):
    block_item(name)
    loot(name)
for name in ("grooming_brush", "toy_ball", "friend_whistle", "homemade_meal", "scout_pack"):
    plain_item(name)

roll_w = lambda x, y: speck("#7a5a3a" if y % 4 else "#5a3e26", 8)
tile("bedroll", roll_w)
tile("bedroll_top", lambda x, y: speck("#c8a878" if 4 <= x <= 11 and y < 5 else "#3a5a7a" if (y // 3) % 2 else "#2e4a68", 6))
bed = {"particle": "monstergirls:block/bedroll", "top": "monstergirls:block/bedroll_top", "side": "monstergirls:block/bedroll"}
be = cube([0, 0, 1], [16, 3, 15], "side")
be["faces"]["up"] = {"texture": "#top"}
model("bedroll", bed, [be])
simple_state("bedroll", {f"facing={f}": {"model": "monstergirls:block/bedroll", "y": r} for f, r in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]})
block_item("bedroll")
loot("bedroll")
model("figurine", {"particle": "monstergirls:block/soaking_tub_wood", "base": "monstergirls:block/soaking_tub_wood"}, [cube([3, 0, 3], [13, 2, 13], "base")])
simple_state("figurine", {f"facing={f}": {"model": "monstergirls:block/figurine", "y": r} for f, r in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]})
block_item("figurine")
icon("sketchbook", [
    "................", "...bbbbbbbbbb...", "...bpppppppppb..", "...bpppppppppb..", "...bppkkkkpppb..", "...bppkppkpppb..", "...bppkkkkpppb..", "...bpppkppppb...",
    "...bppkkkpppb...", "...bpppppppppb..", "...bpppppppppb..", "...bbbbbbbbbbb..", "................", "................", "................", "................"],
     dict(b="#6a3a22", p="#f4ecd8", k="#3a3a40"))
plain_item("sketchbook")
rows = [[speck("#f4f2ec" if (y // 4) % 2 else "#e8a8b8", 5) for x in range(32)] for y in range(32)]
H.write_png(os.path.join(ASSETS, "textures", "entity", "towel.png"), rows)

tile("gift_box", lambda x, y: speck("#e8c050" if x in (7, 8) or y in (7, 8) else "#c83a4a", 6))
tile("gift_box_top", lambda x, y: speck("#e8c050" if x in (7, 8) or y in (7, 8) else "#c83a4a", 6))
gb = {"particle": "monstergirls:block/gift_box", "side": "monstergirls:block/gift_box", "top": "monstergirls:block/gift_box_top"}
ge = cube([2, 0, 2], [14, 10, 14], "side")
ge["faces"]["up"] = {"texture": "#top"}
model("gift_box", gb, [ge, cube([1, 10, 1], [15, 12, 15], "side")])
simple_state("gift_box")
block_item("gift_box")
loot("gift_box")
icon("friend_compass", [
    "................", ".....gggggg.....", "...gggwwwwggg...", "..ggwwwwwwwwgg..", "..gwwwwrrwwwwg..", ".ggwwwrrrrwwwgg.", ".gwwwwwrrwwwwwg.", ".gwwwwwrrwwwwwg.",
    ".gwwwwwkkwwwwwg.", ".gwwwwwkkwwwwwg.", ".ggwwwkkkkwwwgg.", "..gwwwwkkwwwwg..", "..ggwwwwwwwwgg..", "...gggwwwwggg...", ".....gggggg.....", "................"],
     dict(g="#c8a040", w="#f4ecd8", r="#e8486a", k="#3a3a40"))
plain_item("friend_compass")

# --- recipes ------------------------------------------------------------------------------------------------------------
shaped("picnic_blanket", ["WR", "RW"], dict(W="minecraft:white_wool", R="minecraft:red_wool"), 2)
shaped("cooking_pot", ["I I", "I I", "III"], dict(I="minecraft:iron_ingot"))
shaped("soaking_tub", ["P P", "PIP", "PPP"], dict(P="#minecraft:planks", I="minecraft:iron_ingot"))
shaped("vanity_mirror", ["SGS", "SGS", " S "], dict(S="minecraft:stick", G="minecraft:glass_pane"))
shaped("sparring_post", [" H ", "SFS", " F "], dict(H="minecraft:hay_block", S="minecraft:stick", F="#minecraft:wooden_fences"))
shaped("grooming_brush", ["  W", " S ", "S  "], dict(W="minecraft:wheat", S="minecraft:stick"))
shaped("toy_ball", [" W ", "WSW", " W "], dict(W="#minecraft:wool", S="minecraft:slime_ball"), 2)
shaped("scout_pack", ["LSL", "LCL", "LLL"], dict(L="minecraft:leather", S="minecraft:string", C="#c:chests"))
shaped("bedroll", ["SWW", "SWW"], dict(S="minecraft:string", W="#minecraft:wool"))
shaped("sketchbook", [" C", "BF"], dict(C="minecraft:charcoal", B="minecraft:book", F="minecraft:feather"))
shaped("gift_box", ["PSP", "PCP", "PPP"], dict(P="minecraft:paper", S="minecraft:string", C="#c:chests"))
shaped("friend_compass", ["  ", "CH"], dict(C="minecraft:compass", H="minecraft:honeycomb"))
shaped("friend_whistle", ["  G", "GGG", " G "], dict(G="minecraft:gold_nugget"))
tag_add(os.path.join(MC, "pickaxe.json"), ["monstergirls:cooking_pot"])
axe = os.path.join(MC, "axe.json")
if not os.path.exists(axe):
    dump(axe, {"replace": False, "values": []})
tag_add(axe, ["monstergirls:soaking_tub", "monstergirls:sparring_post", "monstergirls:vanity_mirror"])

M.add_lang({
    "block.monstergirls.picnic_blanket": "Picnic Blanket", "block.monstergirls.vanity_mirror": "Vanity Mirror", "block.monstergirls.cooking_pot": "Cooking Pot",
    "block.monstergirls.soaking_tub": "Soaking Tub", "block.monstergirls.sparring_post": "Sparring Post",
    "item.monstergirls.grooming_brush": "Grooming Brush", "item.monstergirls.toy_ball": "Toy Ball", "item.monstergirls.friend_whistle": "Friend Whistle",
    "item.monstergirls.homemade_meal": "Home-Cooked Meal", "block.monstergirls.gift_box": "Gift Box", "item.monstergirls.friend_compass": "Friend Compass",
    "message.monstergirls.compass": "%s: %s blocks %s", "message.monstergirls.compass_none": "No friends nearby in the loaded world",
    "direction.monstergirls.north": "north", "direction.monstergirls.northeast": "northeast", "direction.monstergirls.east": "east", "direction.monstergirls.southeast": "southeast",
    "direction.monstergirls.south": "south", "direction.monstergirls.southwest": "southwest", "direction.monstergirls.west": "west", "direction.monstergirls.northwest": "northwest", "block.monstergirls.bedroll": "Bedroll", "block.monstergirls.figurine": "Figurine", "item.monstergirls.sketchbook": "Sketchbook",
    "item.monstergirls.figurine.of": "Figurine of %s", "message.monstergirls.camp": "%s friends gather round the camp", "message.monstergirls.sketch_refused": "%s won't sit for you", "item.monstergirls.scout_pack": "Scout Pack",
    "item.monstergirls.scout_pack.empty": "Hand it to a friend: she'll scout her home biome", "item.monstergirls.scout_pack.line": "%s x %s",
    "message.monstergirls.scout_set_off": "%s shoulders the pack and sets off for the %s", "message.monstergirls.scout_back": "%s is back from the %s with a full pack!",
    "message.monstergirls.scout_full": "Empty the pack before you give it to %s", "message.monstergirls.scout_refused": "%s won't go for you",
    "message.monstergirls.groomed": "%s has only just been brushed", "message.monstergirls.full": "%s couldn't manage another bite yet",
    "message.monstergirls.whistle": "%s came running", "message.monstergirls.picnic": "Friends at the picnic: %s", "message.monstergirls.picnic_nobody": "Nobody has come for a picnic",
    "message.monstergirls.mirror_nobody": "No friend near the mirror", "message.monstergirls.pot": "In the pot: %s of %s",
})
print("home assets written")
