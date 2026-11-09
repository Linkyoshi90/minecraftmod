#!/usr/bin/env python3
"""Assets for the family milks and the milking machine: bucket icons, item and block models, the c:buckets/milk tag,
recipes, loot table and lang entries.

    python tools/gen_milk_assets.py
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_harmontide_items as I  # noqa: E402
import harpy_lib as H  # noqa: E402

RES = I.RES
ASSETS = I.ASSETS
DATA = I.DATA

# milk prefix -> (name, milk colour), one per clade with milk; order matches item/FamilyMilk.java (tools/taxonomy.py)
import taxonomy  # noqa: E402
MILKS = taxonomy.milks()

BUCKET = [
    "................",
    "................",
    "................",
    "....iiiiiiii....",
    "...immmmmmmmi...",
    "...immmmmmmmi...",
    "...ihmmmmmmsi...",
    "...iiiiiiiiii...",
    "...ihiiiiiisi...",
    "...ihiiiiiisi...",
    "....hiiiiis.....",
    "....hiiiiis.....",
    "....hiiiiis.....",
    ".....ssss.......",
    "................",
    "................",
]


def add_lang(entries):
    path = os.path.join(ASSETS, "lang", "en_us.json")
    with open(path, encoding="utf-8") as fh:
        text = fh.read()
    have = json.loads(text)
    new = [f"  {json.dumps(k)}: {json.dumps(v, ensure_ascii=False)}" for k, v in entries.items() if k not in have]
    if new:
        body = text.rstrip()
        assert body.endswith("}")
        body = body[:-1].rstrip() + ",\n" + ",\n".join(new) + "\n}\n"
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            fh.write(body)
        if any(k.startswith("journal.monstergirls.guide.") for k in entries if k not in have):
            import split_guides                                                    # long guides run over pages
            split_guides.main()


def cake(egg):
    return {"type": "minecraft:crafting_shaped", "category": "food", "pattern": ["AAA", "BEB", "CCC"],
            "key": {"A": {"tag": "c:buckets/milk"}, "B": {"item": "minecraft:sugar"},
                    "C": {"item": "minecraft:wheat"}, "E": egg},
            "result": {"id": "minecraft:cake", "count": 1}}


def edit(path, change):
    with open(path, encoding="utf-8") as fh:
        data = json.load(fh)
    change(data)
    I.dump(path, data)


def box(lo, hi, texture):
    """A model element with the same texture on every face."""
    return {"from": lo, "to": hi, "faces": {f: {"texture": texture} for f in ("north", "south", "east", "west", "up", "down")}}


def machine(lift=0):
    """The plain machine: a barrel with a plunger on top, lifted while it pumps."""
    return {"parent": "minecraft:block/block",
            "textures": {"particle": "minecraft:block/barrel_side", "top": "minecraft:block/copper_block", "bottom": "minecraft:block/smooth_stone", "side": "minecraft:block/barrel_side", "pump": "minecraft:block/iron_block"},
            "elements": [{"from": [0, 0, 0], "to": [16, 15, 16], "faces": {"down": {"texture": "#bottom"}, "up": {"texture": "#top"}, "north": {"texture": "#side"}, "south": {"texture": "#side"}, "west": {"texture": "#side"}, "east": {"texture": "#side"}}},
                         box([7, 15, 7], [9, 17 + lift, 9], "#pump"), box([5, 16 + lift, 5], [11, 18 + lift, 11], "#pump")]}


def cart(lift=0):
    """The milkHarness option: a low cart with a copper tank, two pump cylinders and their hoses."""
    return {"parent": "minecraft:block/block",
            "textures": {"particle": "minecraft:block/copper_block", "base": "minecraft:block/smooth_stone", "tank": "minecraft:block/copper_block",
                         "pump": "minecraft:block/iron_block", "hose": "minecraft:block/black_concrete"},
            "elements": [box([0, 0, 0], [16, 3, 16], "#base"), box([3, 3, 3], [13, 11, 13], "#tank"),
                         box([4, 11, 5], [7, 15 + lift, 8], "#pump"), box([9, 11, 5], [12, 15 + lift, 8], "#pump"),
                         box([5, 15, 3], [6, 16, 8], "#hose"), box([10, 15, 3], [11, 16, 8], "#hose"),
                         box([5, 6, 2], [6, 16, 3], "#hose"), box([10, 6, 2], [11, 16, 3], "#hose")]}


def main():
    lang = {"block.monstergirls.milking_machine": "Milking Machine"}
    for family, (name, colour) in MILKS.items():
        item = family + "_milk"
        pal = {"i": "#c4c8cc", "h": "#eef0f2", "s": "#7a7e84", "m": colour}
        H.write_png(os.path.join(ASSETS, "textures", "item", item + ".png"), I.from_mask(BUCKET, pal, item))
        I.dump(os.path.join(ASSETS, "models", "item", item + ".json"),
               {"parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:item/" + item}})
        lang["item.monstergirls." + item] = name + " Milk"
    add_lang(lang)
    I.dump(os.path.join(RES, "data", "c", "tags", "item", "buckets", "milk.json"),
           {"replace": False, "values": ["monstergirls:" + f + "_milk" for f in MILKS]})

    I.dump(os.path.join(ASSETS, "blockstates", "milking_machine.json"),
           {"variants": {"harness=false,stroke=false": {"model": "monstergirls:block/milking_machine"},
                         "harness=false,stroke=true": {"model": "monstergirls:block/milking_machine_stroke"},
                         "harness=true,stroke=false": {"model": "monstergirls:block/milking_machine_cart"},
                         "harness=true,stroke=true": {"model": "monstergirls:block/milking_machine_cart_stroke"}}})
    I.dump(os.path.join(ASSETS, "models", "block", "milking_machine_cart.json"), cart())
    I.dump(os.path.join(ASSETS, "models", "block", "milking_machine_cart_stroke.json"), cart(2))
    I.dump(os.path.join(ASSETS, "models", "block", "milking_machine_stroke.json"), machine(3))
    I.dump(os.path.join(ASSETS, "models", "block", "milking_machine.json"), machine())
    I.dump(os.path.join(ASSETS, "models", "item", "milking_machine.json"), {"parent": "monstergirls:block/milking_machine"})
    I.dump(os.path.join(DATA, "loot_table", "blocks", "milking_machine.json"),
           {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "monstergirls:milking_machine"}],
                                                  "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    I.dump(os.path.join(DATA, "recipe", "milking_machine.json"),
           {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["CCC", "IBI", "IRI"],
            "key": {"C": {"item": "minecraft:copper_ingot"}, "I": {"item": "minecraft:iron_ingot"},
                    "B": {"item": "minecraft:bucket"}, "R": {"item": "minecraft:redstone"}},
            "result": {"id": "monstergirls:milking_machine", "count": 1}})

    def mineable(data):
        if "monstergirls:milking_machine" not in data["values"]:
            data["values"].append("monstergirls:milking_machine")
    edit(os.path.join(RES, "data", "minecraft", "tags", "block", "mineable", "pickaxe.json"), mineable)

    # every milk works where vanilla milk does
    def tagged(data):
        data["key"]["A"] = {"tag": "c:buckets/milk"}
    edit(os.path.join(DATA, "recipe", "cake_from_laid_egg.json"), tagged)
    edit(os.path.join(DATA, "recipe", "cake_from_sparrow_egg.json"), tagged)
    I.dump(os.path.join(DATA, "recipe", "cake_from_family_milk.json"), cake({"item": "minecraft:egg"}))
    I.dump(os.path.join(DATA, "recipe", "cheese.json"),
           {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [{"tag": "c:buckets/milk"}],
            "result": {"id": "monstergirls:cheese", "count": 3}})


if __name__ == "__main__":
    main()
    import fix_model_uvs  # the milking machine's arms reach past the block edge: explicit UVs
    fix_model_uvs.main()
