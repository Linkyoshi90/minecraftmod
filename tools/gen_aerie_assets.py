#!/usr/bin/env python3
"""Harpy sky colonies (2026-11-08, world/HarpyAerieStructure, entity/Colonies, block/HarpyPerchBlock): the aerie's worldgen
data, the perch's model (a spruce post and a hay nest, vanilla textures), recipe and loot, advancements, lang, and a sentence for
the nesting guide.

    uv run --no-project --with pillow python tools/gen_aerie_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS, DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402


def box(frm, to, tex_side, tex_end):
    faces = {}
    for f in ("north", "south", "east", "west"):
        faces[f] = {"texture": tex_side}
    faces["up"] = {"texture": tex_end}
    faces["down"] = {"texture": tex_end}
    return {"from": frm, "to": to, "faces": faces}


def main():
    dump(os.path.join(DATA, "worldgen", "structure", "harpy_aerie.json"), {
        "type": "harmontide:harpy_aerie", "biomes": "#harmontide:has_structure/harpy_aerie",
        "step": "surface_structures", "spawn_overrides": {}, "terrain_adaptation": "none"})
    dump(os.path.join(DATA, "worldgen", "structure_set", "harpy_aerie.json"), {
        "structures": [{"structure": "harmontide:harpy_aerie", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 36, "separation": 12, "salt": 28461937}})
    dump(os.path.join(DATA, "tags", "worldgen", "biome", "has_structure", "harpy_aerie.json"), {"values": [
        "minecraft:windswept_hills", "minecraft:windswept_gravelly_hills", "minecraft:windswept_forest", "minecraft:stony_shore",
        "minecraft:jagged_peaks", "minecraft:stony_peaks", "minecraft:frozen_peaks"]})
    m = os.path.join(ASSETS, "models")
    dump(os.path.join(m, "block", "harpy_perch.json"), {
        "parent": "minecraft:block/block", "ambientocclusion": False,
        "textures": {"particle": "minecraft:block/stripped_spruce_log", "post": "minecraft:block/stripped_spruce_log",
                     "post_top": "minecraft:block/stripped_spruce_log_top", "hay": "minecraft:block/hay_block_side", "hay_top": "minecraft:block/hay_block_top"},
        "elements": [box([6, 0, 6], [10, 13, 10], "#post", "#post_top"), box([2, 13, 2], [14, 16, 14], "#hay", "#hay_top")]})
    dump(os.path.join(ASSETS, "blockstates", "harpy_perch.json"), {"variants": {"": {"model": "harmontide:block/harpy_perch"}}})
    dump(os.path.join(m, "item", "harpy_perch.json"), {"parent": "harmontide:block/harpy_perch"})
    dump(os.path.join(DATA, "loot_table", "blocks", "harpy_perch.json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": "harmontide:harpy_perch"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    dump(os.path.join(DATA, "recipe", "harpy_perch.json"), {
        "type": "minecraft:crafting_shaped", "category": "decorations",
        "pattern": ["FHF", " L ", " L "], "key": {"F": {"item": "minecraft:feather"}, "H": {"item": "minecraft:hay_block"}, "L": {"item": "minecraft:stripped_spruce_log"}},
        "result": {"id": "harmontide:harpy_perch", "count": 1}})
    lang = {
        "block.harmontide.harpy_perch": "Harpy Perch",
        "entity.harmontide.colony_matriarch": "%s Matriarch",
        "message.harmontide.colony.welcome": "%s has accepted you, and her colony with her: %s more harpies count you a friend.",
        "message.harmontide.flock.perch": "A flock of %s comes down at your perch, and settles there to stay.",
        "commands.harmontide.locate.aerie": "The nearest harpy aerie is at %s (%s blocks away).",
        "commands.harmontide.locate.aerie.none": "No harpy aerie within reach (only peaks, windswept hills and stony shores generated after this update have them).",
    }
    advs = {"harpy_colony": ("stargazer", "minecraft:feather", "goal", "Welcome to the Flock", "Befriend a colony's matriarch, and the colony with her"),
            "harpy_perch": ("harpy_colony", "harmontide:harpy_perch", "task", "Come Home to Roost", "A passing flock settles at your perch")}
    for adv, (parent, icon, frame, title, desc) in advs.items():
        dump(os.path.join(DATA, "advancement", adv + ".json"), {
            "parent": "harmontide:" + parent,
            "display": {"icon": {"id": icon}, "title": {"translate": "advancement.harmontide." + adv},
                        "description": {"translate": "advancement.harmontide." + adv + ".desc"},
                        "frame": frame, "show_toast": True, "announce_to_chat": True, "hidden": False},
            "criteria": {"done": {"trigger": "minecraft:impossible"}}})
        lang["advancement.harmontide." + adv] = title
        lang["advancement.harmontide." + adv + ".desc"] = desc
    add_lang(lang)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.nesting"] = (
        " Up on the peaks, the windswept hills and the stony shores harpies keep aeries: crags topped with nests, one kind to a "
        "colony, led by a matriarch. Befriend her and the colony takes you in. At home, a harpy perch (a hay bale and two feathers "
        "on spruce) draws a passing flock to settle round it for good. (/harmontide locate aerie)")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
