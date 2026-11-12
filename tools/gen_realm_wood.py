#!/usr/bin/env python3
"""Demon Realm wood (2026-11-08, asked for "in a dark purple"): planks, stairs, slab, fence, fence gate, door, trapdoor,
button and pressure plate, the vanilla dark oak set recoloured to deep plum-violet. Unlike realm logs these never turn back
in the light: they are the realm's timber, cut and kept.

    uv run --no-project --with pillow python tools/gen_realm_wood.py [--preview previews/realm_wood.png]

Writes textures, block/item models, blockstates (vanilla shapes, by the vanilla templates), loot tables, recipes and tags.
"""
import json
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS, DATA, MC_DATA, dump, save  # noqa: E402
from gen_realm_cuisine import _img  # noqa: E402
from gen_paints import _vanilla_rgba  # noqa: E402

RES = os.path.dirname(os.path.dirname(ASSETS))
MOD = "harmontide"
P = "realm"                                     # realm_planks, realm_stairs, ...
# dark purple, by lightness: deep plum shadows to a dusky violet highlight
RAMP = [(0x1C, 0x0C, 0x22), (0x2A, 0x12, 0x32), (0x38, 0x1A, 0x42), (0x46, 0x22, 0x52), (0x56, 0x2C, 0x64), (0x68, 0x38, 0x78), (0x7C, 0x46, 0x8E)]


def purple(rows):
    img = _img(rows)
    px = img.load()
    lum = [sum(px[x, y][:3]) / 3 for y in range(img.height) for x in range(img.width) if px[x, y][3]]
    lo, hi = min(lum), max(lum)
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if not a:
                continue
            t = ((r + g + b) / 3 - lo) / max(1, hi - lo)
            px[x, y] = RAMP[min(len(RAMP) - 1, int(t * len(RAMP)))] + (a,)
    return img


def textures():
    out = {}
    out["planks"] = save(purple(_vanilla_rgba("dark_oak_planks")), "block", "realm_planks.png")
    out["door_top"] = save(purple(_vanilla_rgba("dark_oak_door_top")), "block", "realm_door_top.png")
    out["door_bottom"] = save(purple(_vanilla_rgba("dark_oak_door_bottom")), "block", "realm_door_bottom.png")
    out["trapdoor"] = save(purple(_vanilla_rgba("dark_oak_trapdoor")), "block", "realm_trapdoor.png")
    out["door_item"] = save(purple(_vanilla_rgba("item/dark_oak_door")), "item", "realm_door.png")
    return out


def B(name):
    return f"{MOD}:block/{name}"


def model(name, parent, **tex):
    dump(os.path.join(ASSETS, "models", "block", name + ".json"), {"parent": "minecraft:block/" + parent, "textures": tex})


def item_model(name, parent=None, layer=None):
    data = {"parent": parent} if parent else {"parent": "minecraft:item/generated", "textures": {"layer0": layer}}
    dump(os.path.join(ASSETS, "models", "item", name + ".json"), data)


def state(name, variants=None, multipart=None):
    dump(os.path.join(ASSETS, "blockstates", name + ".json"), {"variants": variants} if variants is not None else {"multipart": multipart})


def models_and_states():
    planks = B("realm_planks")
    model("realm_planks", "cube_all", all=planks)
    state("realm_planks", {"": {"model": B("realm_planks")}})
    item_model("realm_planks", B("realm_planks"))

    # stairs
    for suffix, parent in (("", "stairs"), ("_inner", "inner_stairs"), ("_outer", "outer_stairs")):
        model("realm_stairs" + suffix, parent, bottom=planks, top=planks, side=planks)
    v = {}
    facing_y = {"east": 0, "south": 90, "west": 180, "north": 270}
    for facing, fy in facing_y.items():
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                m = B("realm_stairs" + ("" if shape == "straight" else "_inner" if shape.startswith("inner") else "_outer"))
                y = fy
                if half == "bottom" and shape in ("inner_left", "outer_left"):
                    y -= 90
                if half == "top" and shape in ("inner_right", "outer_right"):
                    y += 90
                e = {"model": m}
                if half == "top":
                    e["x"] = 180
                y %= 360
                if y:
                    e["y"] = y
                if y or half == "top":
                    e["uvlock"] = True
                v[f"facing={facing},half={half},shape={shape}"] = e
    state("realm_stairs", v)
    item_model("realm_stairs", B("realm_stairs"))

    # slab
    model("realm_slab", "slab", bottom=planks, top=planks, side=planks)
    model("realm_slab_top", "slab_top", bottom=planks, top=planks, side=planks)
    state("realm_slab", {"type=bottom": {"model": B("realm_slab")}, "type=top": {"model": B("realm_slab_top")}, "type=double": {"model": B("realm_planks")}})
    item_model("realm_slab", B("realm_slab"))

    # fence
    model("realm_fence_post", "fence_post", texture=planks)
    model("realm_fence_side", "fence_side", texture=planks)
    model("realm_fence_inventory", "fence_inventory", texture=planks)
    mp = [{"apply": {"model": B("realm_fence_post")}}]
    for d, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        a = {"model": B("realm_fence_side"), "uvlock": True}
        if y:
            a["y"] = y
        mp.append({"when": {d: "true"}, "apply": a})
    state("realm_fence", multipart=mp)
    item_model("realm_fence", B("realm_fence_inventory"))

    # fence gate
    for n, parent in (("", "template_fence_gate"), ("_open", "template_fence_gate_open"), ("_wall", "template_fence_gate_wall"),
                      ("_wall_open", "template_fence_gate_wall_open")):
        model("realm_fence_gate" + n, parent, texture=planks)
    v = {}
    for facing, fy in (("south", 0), ("west", 90), ("north", 180), ("east", 270)):
        for in_wall in ("false", "true"):
            for open_ in ("false", "true"):
                n = ("_wall" if in_wall == "true" else "") + ("_open" if open_ == "true" else "")
                e = {"model": B("realm_fence_gate" + n), "uvlock": True}
                if fy:
                    e["y"] = fy
                v[f"facing={facing},in_wall={in_wall},open={open_}"] = e
    state("realm_fence_gate", v)
    item_model("realm_fence_gate", B("realm_fence_gate"))

    # door
    top, bottom = B("realm_door_top"), B("realm_door_bottom")
    for half in ("bottom", "top"):
        for hinge in ("left", "right"):
            for open_ in ("", "_open"):
                name = f"realm_door_{half}_{hinge}{open_}"
                model(name, f"door_{half}_{hinge}{open_}", top=top, bottom=bottom)
    v = {}
    rot = {"east": 0, "south": 90, "west": 180, "north": 270}
    for facing, fy in rot.items():
        for half in ("lower", "upper"):
            for hinge in ("left", "right"):
                for open_ in ("false", "true"):
                    h = "bottom" if half == "lower" else "top"
                    if open_ == "false":
                        m, y = f"realm_door_{h}_{hinge}", fy
                    else:
                        m, y = f"realm_door_{h}_{hinge}_open", fy + (90 if hinge == "left" else 270)
                    e = {"model": B(m)}
                    if y % 360:
                        e["y"] = y % 360
                    v[f"facing={facing},half={half},hinge={hinge},open={open_}"] = e
    state("realm_door", v)
    item_model("realm_door", layer=f"{MOD}:item/realm_door")

    # trapdoor
    t = B("realm_trapdoor")
    model("realm_trapdoor_bottom", "template_orientable_trapdoor_bottom", texture=t)
    model("realm_trapdoor_top", "template_orientable_trapdoor_top", texture=t)
    model("realm_trapdoor_open", "template_orientable_trapdoor_open", texture=t)
    v = {}
    for facing, fy in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for half in ("bottom", "top"):
            for open_ in ("false", "true"):
                if open_ == "true":
                    e = {"model": B("realm_trapdoor_open")}
                    if half == "top":
                        e["x"] = 180
                        y = (fy + 180) % 360
                    else:
                        y = fy
                else:
                    e = {"model": B("realm_trapdoor_" + half)}
                    y = fy
                if y:
                    e["y"] = y
                v[f"facing={facing},half={half},open={open_},powered=false,waterlogged=false"] = e
                v[f"facing={facing},half={half},open={open_},powered=true,waterlogged=false"] = e
                v[f"facing={facing},half={half},open={open_},powered=false,waterlogged=true"] = e
                v[f"facing={facing},half={half},open={open_},powered=true,waterlogged=true"] = e
    state("realm_trapdoor", v)
    item_model("realm_trapdoor", B("realm_trapdoor_bottom"))

    # button
    model("realm_button", "button", texture=planks)
    model("realm_button_pressed", "button_pressed", texture=planks)
    model("realm_button_inventory", "button_inventory", texture=planks)
    v = {}
    for face, x in (("floor", 0), ("wall", 90), ("ceiling", 180)):
        for facing, fy in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
            for powered in ("false", "true"):
                y = fy if face != "ceiling" else (fy + 180) % 360
                e = {"model": B("realm_button_pressed" if powered == "true" else "realm_button")}
                if x:
                    e["x"] = x
                if y:
                    e["y"] = y
                if face == "wall":
                    e["uvlock"] = True
                v[f"face={face},facing={facing},powered={powered}"] = e
    state("realm_button", v)
    item_model("realm_button", B("realm_button_inventory"))

    # pressure plate
    model("realm_pressure_plate", "pressure_plate_up", texture=planks)
    model("realm_pressure_plate_down", "pressure_plate_down", texture=planks)
    state("realm_pressure_plate", {"powered=false": {"model": B("realm_pressure_plate")}, "powered=true": {"model": B("realm_pressure_plate_down")}})
    item_model("realm_pressure_plate", B("realm_pressure_plate"))

    # render types for the see-through door and trapdoor
    for name in [n for n in os.listdir(os.path.join(ASSETS, "models", "block")) if n.startswith(("realm_door_", "realm_trapdoor_"))]:
        path = os.path.join(ASSETS, "models", "block", name)
        data = json.load(open(path, encoding="utf-8"))
        data["render_type"] = "minecraft:cutout"
        dump(path, data)


BLOCKS = ["realm_planks", "realm_stairs", "realm_slab", "realm_fence", "realm_fence_gate", "realm_door", "realm_trapdoor", "realm_button",
          "realm_pressure_plate"]


def loot():
    for b in BLOCKS:
        if b == "realm_door":
            entry = {"type": "minecraft:item", "name": f"{MOD}:{b}", "conditions": [{"condition": "minecraft:block_state_property",
                                                                                       "block": f"{MOD}:{b}", "properties": {"half": "lower"}}]}
        elif b == "realm_slab":
            entry = {"type": "minecraft:item", "name": f"{MOD}:{b}", "functions": [{"function": "minecraft:set_count", "count": 2, "conditions": [
                {"condition": "minecraft:block_state_property", "block": f"{MOD}:{b}", "properties": {"type": "double"}}]}, {"function": "minecraft:explosion_decay"}]}
        else:
            entry = {"type": "minecraft:item", "name": f"{MOD}:{b}"}
        dump(os.path.join(DATA, "loot_table", "blocks", b + ".json"), {"type": "minecraft:block", "pools": [
            {"rolls": 1, "bonus_rolls": 0, "entries": [entry], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})


def recipes():
    R = os.path.join(DATA, "recipe")
    P_ = {"item": f"{MOD}:realm_planks"}
    dump(os.path.join(R, "realm_planks.json"), {"type": "minecraft:crafting_shapeless", "category": "building", "group": "planks",
                                                "ingredients": [{"item": f"{MOD}:realm_log"}], "result": {"id": f"{MOD}:realm_planks", "count": 4}})
    dump(os.path.join(R, "realm_stairs.json"), {"type": "minecraft:crafting_shaped", "category": "building", "group": "wooden_stairs",
                                                "pattern": ["#  ", "## ", "###"], "key": {"#": P_}, "result": {"id": f"{MOD}:realm_stairs", "count": 4}})
    dump(os.path.join(R, "realm_slab.json"), {"type": "minecraft:crafting_shaped", "category": "building", "group": "wooden_slab",
                                              "pattern": ["###"], "key": {"#": P_}, "result": {"id": f"{MOD}:realm_slab", "count": 6}})
    dump(os.path.join(R, "realm_fence.json"), {"type": "minecraft:crafting_shaped", "category": "misc", "group": "wooden_fence",
                                               "pattern": ["W#W", "W#W"], "key": {"W": P_, "#": {"item": "minecraft:stick"}},
                                               "result": {"id": f"{MOD}:realm_fence", "count": 3}})
    dump(os.path.join(R, "realm_fence_gate.json"), {"type": "minecraft:crafting_shaped", "category": "redstone", "group": "wooden_fence_gate",
                                                    "pattern": ["#W#", "#W#"], "key": {"W": P_, "#": {"item": "minecraft:stick"}},
                                                    "result": {"id": f"{MOD}:realm_fence_gate", "count": 1}})
    dump(os.path.join(R, "realm_door.json"), {"type": "minecraft:crafting_shaped", "category": "redstone", "group": "wooden_door",
                                              "pattern": ["##", "##", "##"], "key": {"#": P_}, "result": {"id": f"{MOD}:realm_door", "count": 3}})
    dump(os.path.join(R, "realm_trapdoor.json"), {"type": "minecraft:crafting_shaped", "category": "redstone", "group": "wooden_trapdoor",
                                                  "pattern": ["###", "###"], "key": {"#": P_}, "result": {"id": f"{MOD}:realm_trapdoor", "count": 2}})
    dump(os.path.join(R, "realm_button.json"), {"type": "minecraft:crafting_shapeless", "category": "redstone", "group": "wooden_button",
                                                "ingredients": [P_], "result": {"id": f"{MOD}:realm_button", "count": 1}})
    dump(os.path.join(R, "realm_pressure_plate.json"), {"type": "minecraft:crafting_shaped", "category": "redstone", "group": "wooden_pressure_plate",
                                                        "pattern": ["##"], "key": {"#": P_}, "result": {"id": f"{MOD}:realm_pressure_plate", "count": 1}})


def tags():
    def add(path, values):
        old = json.load(open(path, encoding="utf-8")) if os.path.exists(path) else {"replace": False, "values": []}
        for v in values:
            if v not in old["values"]:
                old["values"].append(v)
        dump(path, old)
    ids = {b: f"{MOD}:{b}" for b in BLOCKS}
    pairs = {"planks": ["realm_planks"], "wooden_stairs": ["realm_stairs"], "wooden_slabs": ["realm_slab"], "wooden_fences": ["realm_fence"],
             "fence_gates": ["realm_fence_gate"], "wooden_doors": ["realm_door"], "wooden_trapdoors": ["realm_trapdoor"],
             "wooden_buttons": ["realm_button"], "wooden_pressure_plates": ["realm_pressure_plate"]}
    for tag, names in pairs.items():
        add(os.path.join(MC_DATA, "tags", "block", tag + ".json"), [ids[n] for n in names])
        if tag != "fence_gates":
            add(os.path.join(MC_DATA, "tags", "item", tag + ".json"), [ids[n] for n in names])
    add(os.path.join(MC_DATA, "tags", "item", "fence_gates.json"), [ids["realm_fence_gate"]])
    add(os.path.join(MC_DATA, "tags", "block", "mineable", "axe.json"), list(ids.values()))
    add(os.path.join(MC_DATA, "tags", "item", "logs_that_burn.json"), [f"{MOD}:realm_log"])
    add(os.path.join(MC_DATA, "tags", "item", "logs.json"), [f"{MOD}:realm_log"])


def main():
    tex = textures()
    models_and_states()
    loot()
    recipes()
    tags()
    if "--preview" in sys.argv:
        path = sys.argv[sys.argv.index("--preview") + 1]
        tiles = list(tex.values())
        sheet = Image.new("RGBA", (len(tiles) * 108, 108), (40, 40, 48, 255))
        for i, t in enumerate(tiles):
            big = t.resize((96, 96), Image.NEAREST)
            sheet.paste(big, (i * 108 + 6, 6), big)
        sheet.save(path)


if __name__ == "__main__":
    main()
