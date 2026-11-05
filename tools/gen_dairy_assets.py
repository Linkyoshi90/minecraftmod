#!/usr/bin/env python3
"""Assets for dairy (entity/Dairy.java): the butter churn, cheese shelf, milk tank and fridge (textures, models, blockstates, loot,
recipes, tool tags), butter and ice cream, the five dairy advancements, the open umbrella's 3D model, and lang entries.

    python tools/gen_dairy_assets.py

Can be run twice.
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
FACINGS = [("north", 0), ("east", 90), ("south", 180), ("west", 270)]


def rgb(h, n=0):
    h = h.lstrip("#")
    return tuple(min(255, max(0, int(h[i:i + 2], 16) + n)) for i in (0, 2, 4)) + (255,)


def speck(col, spread=8):
    return rgb(col, random.randint(-spread, spread))


def tile(name, fn, folder="block", size=16):
    random.seed(sum(map(ord, name)))
    H.write_png(os.path.join(ASSETS, "textures", folder, name + ".png"), [[fn(x, y) for x in range(size)] for y in range(size)])


def dump(path, doc):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="") as fh:
        json.dump(doc, fh, indent=2)
        fh.write("\n")


def cube(frm, to, tex, faces=("down", "up", "north", "south", "west", "east"), **per_face):
    f = {k: {"texture": "#" + per_face.get(k, tex)} for k in faces}
    return {"from": frm, "to": to, "faces": f}


def model(name, textures, elements, parent="minecraft:block/block", render=None, folder="block", display=None):
    doc = {"parent": parent, "textures": textures, "elements": elements}
    if parent is None:                     # a held 3D item: a parent ending in item/generated would draw only (missing) layers
        del doc["parent"]
    if render:
        doc["render_type"] = render
    if display:
        doc["display"] = display
    dump(os.path.join(ASSETS, "models", folder, name + ".json"), doc)


def state(name, doc):
    dump(os.path.join(ASSETS, "blockstates", name + ".json"), doc)


def block_item(name, mod=None):
    dump(os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": f"monstergirls:block/{mod or name}"})


def plain_item(name):
    dump(os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"monstergirls:item/{name}"}})


def loot(name):
    dump(os.path.join(DATA, "loot_table", "blocks", name + ".json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"monstergirls:{name}"}],
         "conditions": [{"condition": "minecraft:survives_explosion"}]}]})


def shaped(name, pattern, key, count=1):
    dump(os.path.join(DATA, "recipe", name + ".json"), {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
         "key": {k: {"tag": v[1:]} if v.startswith("#") else {"item": v} for k, v in key.items()}, "result": {"id": f"monstergirls:{name}", "count": count}})


def tag_add(path, names):
    if not os.path.exists(path):
        dump(path, {"replace": False, "values": []})
    with open(path, encoding="utf-8") as fh:
        doc = json.load(fh)
    for n in names:
        if n not in doc["values"]:
            doc["values"].append(n)
    dump(path, doc)


def icon(name, mask, cols):
    tile(name, lambda x, y: CLEAR if mask[y][x] == "." else speck(cols[mask[y][x]], 5), "item")


def advancement(name, icon_id, frame="task"):
    dump(os.path.join(DATA, "advancement", name + ".json"), {
        "parent": "minecraft:husbandry/root",
        "display": {"icon": {"id": icon_id}, "title": {"translate": f"advancement.monstergirls.{name}"},
                    "description": {"translate": f"advancement.monstergirls.{name}.desc"}, "frame": frame,
                    "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})


# --- textures ---------------------------------------------------------------------------------------------------------
tile("milk", lambda x, y: speck("#f6f2e6" if (x * 3 + y * 5) % 11 else "#ece6d6", 3))
tile("cheese_wheel_side", lambda x, y: speck("#c88a2a", 6) if y in (0, 15) else speck("#f0c050" if (x + y * 7) % 13 else "#d8a838", 6))
tile("cheese_wheel_top", lambda x, y: speck("#e8b848" if max(abs(x - 7.5), abs(y - 7.5)) < 6 else "#c88a2a", 6))
tile("cheese_button_red", lambda x, y: speck("#c83a3a", 8))
tile("cheese_button_green", lambda x, y: speck("#4ab84a", 8))
tile("fridge_side", lambda x, y: speck("#eef0f2" if y % 8 else "#d8dce0", 3))
tile("fridge_front", lambda x, y: speck("#9aa0a8", 4) if x in (2, 3) and 3 <= y <= 12 else speck("#c8ccd2", 3) if x in (0, 15) or y in (0, 15) else speck("#f2f4f6", 3))
tile("fridge_inside", lambda x, y: speck("#c8d8e8", 4) if y in (4, 5, 10, 11) else speck("#e4ecf4", 4) if 1 <= x <= 14 and 1 <= y <= 14 else speck("#c8ccd2", 3))
tile("churn_wood", lambda x, y: speck("#9a6e42" if x % 4 else "#7a5232", 8))
tile("churn_top", lambda x, y: speck("#8a5e36" if max(abs(x - 7.5), abs(y - 7.5)) > 4.5 else "#a87a4a", 8))
tile("umbrella_canopy", lambda x, y: speck("#d85a8a" if (x // 4) % 2 else "#f4e4ec", 4))
tile("umbrella_handle", lambda x, y: speck("#5a3a22", 6))

icon("butter", [
    "................", "................", "................", "................", "....yyyyyyyy....", "...yYYYYYYYYy...", "..yYYYYYYYYYYy..", "..yyyyyyyyyyyyw.",
    "..yyyyyyyyyyyyw.", "..yyyyyyyyyyyyw.", "..ddddddddddddw.", "..wwwwwwwwwwwww.", "...wwwwwwwwwww..", "................", "................", "................"],
     dict(y="#f4dc78", Y="#fbec9c", d="#d8bc58", w="#e8e4dc"))
icon("ice_cream", [
    "................", "......pppp......", ".....pPPPPp.....", "....pPPPPPPp....", "...wwwppppwww...", "..wWWWWwwWWWWw..", "..wWWWWWWWWWWw..", ".bbbbbbbbbbbbbb.",
    ".bBBBBBBBBBBBBb.", "..bBBBBBBBBBBb..", "...bBBBBBBBBb...", "....bbbbbbbb....", "................", "................", "................", "................"],
     dict(p="#e88aa8", P="#f8b8cc", w="#e8e0d0", W="#fbf6ea", b="#7a4a24", B="#a87a4a"))

# --- butter churn -------------------------------------------------------------------------------------------------------
churn = {"particle": "monstergirls:block/churn_wood", "wood": "monstergirls:block/churn_wood", "top": "monstergirls:block/churn_top",
         "band": "minecraft:block/iron_block", "stick": "minecraft:block/stripped_oak_log"}
body = cube([3, 0, 3], [13, 12, 13], "wood", up="top")
bands = [cube([2.9, 2, 2.9], [13.1, 3, 13.1], "band"), cube([2.9, 9, 2.9], [13.1, 10, 13.1], "band")]
lid = cube([4, 12, 4], [12, 13, 12], "top")
for name, top in (("butter_churn", 20), ("butter_churn_stroke", 16)):
    model(name, churn, [body] + bands + [lid, cube([7.5, 13, 7.5], [8.5, top, 8.5], "stick"), cube([5, top - 1, 7.5], [11, top, 8.5], "stick")])
state("butter_churn", {"variants": {f"powered={p},stroke={s}": {"model": "monstergirls:block/butter_churn" + ("_stroke" if s == "true" else "")}
                                    for p in ("false", "true") for s in ("false", "true")}})

# --- cheese shelf: a little two-board shelf, four wheels, the filter button on its right-hand (west, for north) side --------------
shelf = {"particle": "monstergirls:block/churn_wood", "wood": "monstergirls:block/churn_wood"}
model("cheese_shelf", shelf, [cube([0, 0, 0], [16, 1, 16], "wood"), cube([0, 7, 0], [16, 8, 16], "wood"), cube([0, 1, 14], [16, 14, 16], "wood"),
                              cube([0, 0, 0], [1, 14, 2], "wood"), cube([15, 0, 0], [16, 14, 2], "wood")])
wheel_tex = {"particle": "monstergirls:block/cheese_wheel_side", "side": "monstergirls:block/cheese_wheel_side", "top": "monstergirls:block/cheese_wheel_top"}
WHEELS = [([9, 1, 4], [14, 5, 11]), ([2, 1, 4], [7, 5, 11]), ([9, 8, 4], [14, 12, 11]), ([2, 8, 4], [7, 12, 11])]
for i, (a, b) in enumerate(WHEELS, 1):
    model(f"cheese_shelf_wheel_{i}", wheel_tex, [cube(a, b, "side", up="top", down="top")], parent="minecraft:block/block")
for colour in ("red", "green"):
    model(f"cheese_shelf_button_{colour}", {"particle": f"monstergirls:block/cheese_button_{colour}", "b": f"monstergirls:block/cheese_button_{colour}"},
          [cube([-1, 3, 6], [0, 6, 10], "b")])
parts = []
for facing, rot in FACINGS:
    parts.append({"when": {"facing": facing}, "apply": {"model": "monstergirls:block/cheese_shelf", "y": rot}})
    for i in range(1, 5):
        parts.append({"when": {"facing": facing, "cheeses": "|".join(str(n) for n in range(i, 5))}, "apply": {"model": f"monstergirls:block/cheese_shelf_wheel_{i}", "y": rot}})
    for hard, colour in (("true", "red"), ("false", "green")):
        parts.append({"when": {"facing": facing, "pull_hard": hard}, "apply": {"model": f"monstergirls:block/cheese_shelf_button_{colour}", "y": rot}})
state("cheese_shelf", {"multipart": parts})
model("cheese_shelf_item", {**shelf, **wheel_tex, "b": "monstergirls:block/cheese_button_red"},
      [cube([0, 0, 0], [16, 1, 16], "wood"), cube([0, 7, 0], [16, 8, 16], "wood"), cube([0, 1, 14], [16, 14, 16], "wood"), cube([0, 0, 0], [1, 14, 2], "wood"),
       cube([15, 0, 0], [16, 14, 2], "wood"), cube([9, 1, 4], [14, 5, 11], "side", up="top", down="top"), cube([2, 8, 4], [7, 12, 11], "side", up="top", down="top"),
       cube([-1, 3, 6], [0, 6, 10], "b")])

# --- milk tank: an iron frame round glass, the milk rising in eighths ------------------------------------------------------------
tank = {"particle": "minecraft:block/iron_block", "iron": "minecraft:block/iron_block", "glass": "minecraft:block/glass", "milk": "monstergirls:block/milk"}
frame = [cube([0, 0, 0], [16, 1, 16], "iron"), cube([0, 15, 0], [16, 16, 16], "iron")] + [cube([x, 1, z], [x + 2, 15, z + 2], "iron") for x in (0, 14) for z in (0, 14)]
for level in range(9):
    els = list(frame) + [cube([1, 1, 1], [15, 15, 15], "glass", faces=("north", "south", "west", "east"))]
    if level:
        els.append(cube([2, 1, 2], [14, 1 + 14 * level / 8, 14], "milk"))
    model(f"milk_tank_{level}", tank, els, render="minecraft:cutout")
state("milk_tank", {"variants": {f"level={n}": {"model": f"monstergirls:block/milk_tank_{n}"} for n in range(9)}})

# --- fridge: white box, its door on the front; open, the door stands out to the left (as seen from the front) -------------------
fr = {"particle": "monstergirls:block/fridge_side", "side": "monstergirls:block/fridge_side", "front": "monstergirls:block/fridge_front", "inside": "monstergirls:block/fridge_inside"}
model("fridge", fr, [cube([0, 0, 1], [16, 16, 16], "side"), cube([0, 0, 0], [16, 16, 1], "side", north="front")])
model("fridge_open", fr, [cube([0, 0, 1], [16, 16, 16], "side", north="inside"), cube([15, 0, -15], [16, 16, 0], "side", west="front")])
state("fridge", {"variants": {f"facing={f},open={o}": {"model": "monstergirls:block/fridge" + ("_open" if o == "true" else ""), "y": r}
                              for f, r in FACINGS for o in ("false", "true")}})

for name, item_model in (("butter_churn", None), ("cheese_shelf", "cheese_shelf_item"), ("milk_tank", "milk_tank_4"), ("fridge", None)):
    block_item(name, item_model)
    loot(name)
for name in ("butter", "ice_cream"):
    plain_item(name)

# --- the umbrella: a 2D icon in the inventory, open over its holder's head in hand ----------------------------------------------
um = {"particle": "monstergirls:block/umbrella_canopy", "canopy": "monstergirls:block/umbrella_canopy", "handle": "monstergirls:block/umbrella_handle"}
canopy = [cube([-16, 26, -16], [32, 27, 32], "canopy"), cube([-11, 27, -11], [27, 29, 27], "canopy"), cube([-5, 29, -5], [21, 31, 21], "canopy"),
          cube([4, 31, 4], [12, 32, 12], "canopy")]
shaft = [cube([7.5, -8, 7.5], [8.5, 31, 8.5], "handle"), cube([7.5, -10, 7.5], [11.5, -8, 8.5], "handle"), cube([10.5, -8, 7.5], [11.5, -6, 8.5], "handle")]
held = {"rotation": [0, 0, 0], "translation": [0, 3, 1], "scale": [1, 1, 1]}
model("umbrella_open", um, canopy + shaft, parent=None, folder="item",
      display={"thirdperson_righthand": held, "thirdperson_lefthand": held,
               "firstperson_righthand": {"rotation": [0, 0, 0], "translation": [0, 6, 0], "scale": [0.68, 0.68, 0.68]},
               "firstperson_lefthand": {"rotation": [0, 0, 0], "translation": [0, 6, 0], "scale": [0.68, 0.68, 0.68]}})
dump(os.path.join(ASSETS, "models", "item", "umbrella_icon.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:item/umbrella"}})
icon_ref = {"parent": "monstergirls:item/umbrella_icon"}
dump(os.path.join(ASSETS, "models", "item", "umbrella.json"), {"loader": "neoforge:separate_transforms", "base": {"parent": "monstergirls:item/umbrella_open"},
     "perspectives": {"gui": icon_ref, "ground": icon_ref, "fixed": icon_ref}})

# --- recipes, tags, advancements ---------------------------------------------------------------------------------------------
shaped("butter_churn", [" S ", "PIP", "PPP"], dict(S="minecraft:stick", P="#minecraft:planks", I="minecraft:iron_ingot"))
shaped("cheese_shelf", ["WWW", "S S", "WBW"], dict(W="#minecraft:wooden_slabs", S="minecraft:stick", B="#minecraft:buttons"))
shaped("milk_tank", ["IGI", "GBG", "IGI"], dict(I="minecraft:iron_ingot", G="minecraft:glass", B="minecraft:bucket"))
shaped("fridge", ["III", "ICI", "IPI"], dict(I="minecraft:iron_ingot", C="#c:chests", P="minecraft:packed_ice"))
dump(os.path.join(DATA, "recipe", "ice_cream.json"), {"type": "monstergirls:ice_cream", "category": "misc"})
tag_add(os.path.join(MC, "axe.json"), ["monstergirls:butter_churn", "monstergirls:cheese_shelf"])
tag_add(os.path.join(MC, "pickaxe.json"), ["monstergirls:milk_tank", "monstergirls:fridge"])
CTAGS = os.path.join(I.RES, "data", "c", "tags", "item")
for t in ("foods/butter", "butter", "butters"):                   # other mods' recipes that take any butter or cheese take ours
    tag_add(os.path.join(CTAGS, t + ".json"), ["monstergirls:butter"])
for t in ("foods/cheese", "cheese", "cheeses"):
    tag_add(os.path.join(CTAGS, t + ".json"), ["monstergirls:cheese"])
advancement("first_milk", "monstergirls:bovid_milk")
advancement("churned", "monstergirls:butter")
advancement("hard_cheese", "monstergirls:cheese", "goal")
advancement("full_tank", "monstergirls:milk_tank", "goal")
advancement("fridge", "monstergirls:fridge")

lang = {
    "block.monstergirls.butter_churn": "Butter Churn", "block.monstergirls.cheese_shelf": "Cheese Shelf", "block.monstergirls.milk_tank": "Milk Tank",
    "block.monstergirls.fridge": "Fridge", "item.monstergirls.butter": "Butter", "item.monstergirls.butter.family": "%s Butter",
    "item.monstergirls.ice_cream": "Ice Cream", "item.monstergirls.ice_cream.family": "%s Ice Cream", "item.monstergirls.cheese.family": "%s Cheese",
    "item.monstergirls.cheese.soft": "Soft Cheese", "item.monstergirls.cheese.firm": "Firm Cheese", "item.monstergirls.cheese.hard": "Hard Cheese",
    "item.monstergirls.cheese.soft.family": "Soft %s Cheese", "item.monstergirls.cheese.firm.family": "Firm %s Cheese", "item.monstergirls.cheese.hard.family": "Hard %s Cheese",
    "item.monstergirls.cheese.aging": "Ages on a cheese shelf underground", "item.monstergirls.homemade_meal.family": "Home-Cooked Meal with %s Dairy",
    "item.monstergirls.dairy.effect": "%s (%s:%s)", "item.monstergirls.dairy.diet": ", from her diet", "item.monstergirls.milk.flavour": "Flavoured: %s",
    "message.monstergirls.churn": "Milk in the churn: %s of %s", "message.monstergirls.churn_full": "The churn is full, or holds another milk",
    "message.monstergirls.churn_empty": "Nothing in the churn", "message.monstergirls.cheese_set": "The cheese is set to ripen",
    "message.monstergirls.cheese_sky": "Too bright here: cheese only ripens underground", "message.monstergirls.cheese_full": "The shelf is full",
    "message.monstergirls.cheese_empty": "No cheese on the shelf yet", "message.monstergirls.cheese_filter_hard": "Hoppers take only hard cheese",
    "message.monstergirls.cheese_filter_all": "Hoppers take any cheese", "message.monstergirls.tank": "%s/%s buckets of %s, %s empty buckets",
    "message.monstergirls.tank_nothing": "nothing", "message.monstergirls.tank_full": "The tank is full", "message.monstergirls.tank_other": "The tank holds another milk",
    "message.monstergirls.milk_full": "%s is uncomfortably full: she needs a milking machine", "message.monstergirls.milk_relief": "%s sighs with relief",
    "monstergirls.configuration.milkDiscomfort": "Milk discomfort", "monstergirls.configuration.recipeFlavours": "Flavours from recipes",
    "message.monstergirls.weaned": "%s eats her first solid food: %s weans her", "message.monstergirls.her_mother": "her mother",
    "jade.monstergirls.nursing": "Nursing her baby", "jade.monstergirls.keeps_milk": "Keeps her milk (never weaned)",
    "jade.monstergirls.milk_full": "Uncomfortably full: needs a milking machine",
    "message.monstergirls.rescue_offer": "%s hurries over with a cup of her milk. Right-click her with an empty hand to drink it",
    "message.monstergirls.rescue_drink": "You drink %s's milk: the poison fades",
    "journal.monstergirls.guide.nursing.title": "Nursing",
    "journal.monstergirls.guide.nursing": "A mother with milk nurses her baby every few minutes. Feed the baby by hand to wean her; never weaned, her mother "
                                          "keeps her milk for good. Mothers foster orphans of any kind. Poisoned or withering under a roof? A friend with milk brings you a cup.",
    "journal.monstergirls.guide.dairy.title": "Dairy",
    "journal.monstergirls.guide.dairy": "Pour milk into a butter churn and work it, by hand or with redstone. Set it on a cheese shelf underground: it firms in a day and hardens in three, stronger each time. "
                                         "A fridge feeds the household; berries, honey, fish and the like flavour a friend's milk for a day, and so does food from your hand.",
    "journal.monstergirls.milk.title": "Milk Given", "journal.monstergirls.milk.total": "%s buckets in all", "journal.monstergirls.milk.line": "%s: %s",
    "advancement.monstergirls.first_milk": "Fresh from the Source", "advancement.monstergirls.first_milk.desc": "Get a bucket of milk from a friend",
    "advancement.monstergirls.churned": "Elbow Grease", "advancement.monstergirls.churned.desc": "Churn milk into butter",
    "advancement.monstergirls.hard_cheese": "Worth the Wait", "advancement.monstergirls.hard_cheese.desc": "Age a cheese until it is hard",
    "advancement.monstergirls.full_tank": "Dairy Farmer", "advancement.monstergirls.full_tank.desc": "Fill a milk tank to the top",
    "advancement.monstergirls.fridge": "Help Yourself", "advancement.monstergirls.fridge.desc": "Have a friend eat from your fridge",
}
import taxonomy  # noqa: E402  the clade names on dairy ("Firm Feliform Cheese")
lang.update({k: v for k, v in taxonomy.lang_entries().items() if k.startswith("family.")})
M.add_lang(lang)
print("dairy assets written")
