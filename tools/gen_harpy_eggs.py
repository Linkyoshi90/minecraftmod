#!/usr/bin/env python3
"""Writes the fertilised harpy egg block's assets: a woven twig nest with a clutch of one to four speckled eggs in
it (the `eggs` block state), per species, cracking in four stages (the `hatch` block state, 0-3) until the
hatchlings come out.

    python tools/gen_harpy_eggs.py

The nest bowl is the riflebird nest's (gen_nest_assets.py) so the two look related; the egg is two stacked boxes
(a rounded-ish egg) with the species' own colour and flecks, and cracks that spread each stage. Also writes the
blockstate (species x hatch x eggs) and the item model.
"""
import glob
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from harpy_lib import write_png  # noqa: E402
import gen_nest_assets as N  # noqa: E402

ASSETS = N.ASSETS

# species id -> (shell colour, fleck colour, second fleck colour)
SPECIES = {
    "sparrow_harpy": ((226, 214, 188), (130, 96, 64), (170, 140, 104)),
    "crow_harpy": ((150, 190, 176), (40, 52, 44), (86, 104, 70)),
    "ostrich_harpy": ((238, 228, 200), (196, 176, 136), (214, 198, 160)),
    "owl_harpy": ((246, 242, 232), (216, 208, 190), (232, 226, 210)),
    "seagull_harpy": ((196, 186, 150), (90, 74, 52), (126, 108, 78)),
    "albatross_harpy": ((250, 248, 244), (200, 196, 190), (226, 222, 216)),
    "riflebird_harpy": ((238, 220, 190), (150, 96, 60), (120, 100, 124)),
    "phoenix_harpy": ((255, 196, 96), (214, 70, 24), (255, 232, 140)),
    "tengu_harpy": ((176, 200, 196), (44, 50, 60), (90, 96, 110)),
    "zhuque_harpy": ((250, 170, 90), (190, 40, 24), (255, 220, 120)),
    "toucan_harpy": ((250, 248, 244), (226, 222, 214), (238, 234, 226)),
    "penguin_harpy": ((236, 234, 222), (206, 200, 186), (222, 216, 204)),
    "finch_harpy": ((200, 224, 230), (110, 70, 50), (150, 104, 74)),
    "flamingo_harpy": ((246, 240, 232), (220, 210, 196), (236, 226, 214)),
    "rookmourn_harpy": ((150, 186, 172), (52, 62, 52), (92, 108, 80)),
    "dodobold_harpy": ((236, 228, 206), (200, 186, 150), (226, 214, 184)),
    "peaflare_harpy": ((226, 206, 166), (180, 150, 104), (210, 186, 140)),
    "cassowrath_harpy": ((96, 160, 80), (60, 110, 50), (130, 190, 110)),
    "coin_magpie_harpy": ((176, 200, 188), (100, 84, 60), (140, 120, 90)),
    "trickster_raven_harpy": ((140, 180, 164), (40, 52, 44), (86, 104, 70)),
    "pumpkingull_harpy": ((236, 170, 90), (110, 60, 20), (60, 40, 30)),
    "condorune_harpy": ((234, 232, 226), (180, 176, 168), (140, 136, 130)),
    "mirror_crane_harpy": ((232, 238, 248), (90, 120, 190), (200, 170, 90)),
    "emberu_harpy": ((40, 70, 60), (24, 44, 36), (70, 110, 96)),
    "ironbeak_harpy": ((196, 220, 240), (60, 100, 180), (150, 158, 176)),
    "ibiset_harpy": ((226, 232, 244), (46, 86, 186), (236, 200, 90)),
    "stigowl_harpy": ((60, 66, 90), (24, 28, 44), (160, 168, 200)),
    "yatagarasu_harpy": ((40, 40, 48), (14, 14, 18), (180, 210, 240)),
    "emu_harpy": ((30, 60, 54), (18, 40, 36), (56, 96, 84)),
    "macaw_harpy": ((246, 244, 236), (40, 136, 220), (250, 206, 70)),
    "roc_harpy": ((226, 232, 244), (70, 110, 176), (156, 192, 232)),
    "ziz_harpy": ((226, 240, 252), (214, 50, 50), (60, 150, 230)),
    "bennu_harpy": ((120, 170, 200), (50, 90, 150), (250, 222, 130)),
    "garuda_harpy": ((244, 190, 60), (214, 124, 44), (60, 124, 190)),
    "lechuza_harpy": ((236, 226, 206), (132, 76, 38), (206, 146, 94)),
    "alkonost_harpy": ((252, 250, 244), (226, 222, 210), (244, 214, 130)),
    "poultry_harpy": ((244, 236, 216), (200, 170, 130), (232, 214, 180)),
    "landharpy_harpy": ((226, 226, 232), (120, 120, 130), (180, 180, 190)),
    "argentowlis_harpy": ((236, 240, 248), (42, 62, 122), (150, 170, 210)),
    "skycrow_harpy": ((30, 40, 70), (20, 26, 48), (90, 184, 248)),
    "ibisking_harpy": ((60, 90, 170), (30, 58, 138), (232, 192, 80)),
    "motmora_harpy": ((120, 170, 236), (30, 74, 200), (232, 192, 80)),
    "hummingbird_harpy": ((244, 240, 232), (200, 190, 180), (230, 220, 210)),
    "duck_harpy": ((206, 214, 186), (150, 160, 130), (180, 190, 160)),
    "swan_harpy": ((232, 236, 226), (190, 196, 186), (210, 214, 204)),
    "pelican_harpy": ((226, 216, 196), (170, 160, 140), (200, 190, 170)),
    "reliquary_dove_harpy": ((230, 236, 246), (122, 154, 200), (232, 192, 80)),
}


def egg_texture(base, f1, f2, stage, seed):
    rnd = random.Random(seed)
    rows = [[base + (255,)] * 16 for _ in range(16)]
    for y in range(16):                                   # a soft shade towards the bottom
        for x in range(16):
            if y > 11:
                rows[y][x] = tuple(max(0, c - 14) for c in base) + (255,)
            elif y < 3 and rnd.random() < 0.5:
                rows[y][x] = tuple(min(255, c + 10) for c in base) + (255,)
    for _ in range(22):
        x, y = rnd.randrange(16), rnd.randrange(16)
        rows[y][x] = rnd.choice((f1, f2)) + (255,)
    dark = tuple(max(0, c - 120) for c in base)
    inside = (255, 236, 168) if stage == 3 else (240, 224, 190)
    cracks = {
        0: [],
        1: [(7, 3), (8, 4), (7, 5)],
        2: [(7, 3), (8, 4), (7, 5), (8, 6), (7, 7), (6, 8), (9, 9), (10, 10), (4, 10), (3, 11)],
        3: [(7, 2), (8, 3), (7, 4), (8, 5), (7, 6), (8, 7), (7, 8), (6, 9), (9, 9), (10, 10), (11, 11), (5, 10), (4, 11), (3, 12), (9, 5), (10, 4), (5, 6), (4, 5)],
    }[stage]
    for x, y in cracks:
        rows[y][x] = dark + (255,)
    if stage >= 2:
        for x, y in ((8, 5), (8, 6)) if stage == 2 else ((8, 4), (8, 5), (8, 6), (7, 5), (9, 6), (7, 7), (8, 8)):
            rows[y][x] = inside + (255,)
    return rows


# a single egg sits big in the middle; a clutch of smaller eggs fills the bowl corner by corner
CLUTCH_SPOTS = [(4, 4.5), (8.5, 4), (4.5, 8.5), (8.5, 8.5)]


def eggs_in(count):
    if count == 1:
        return [N.box([5.5, 1, 5.5], [10.5, 6, 10.5], "#egg"), N.box([6.5, 6, 6.5], [9.5, 8, 9.5], "#egg")]
    boxes = []
    for x, z in CLUTCH_SPOTS[:count]:
        boxes.append(N.box([x, 1, z], [x + 3.5, 5, z + 3.5], "#egg"))
        boxes.append(N.box([x + 0.75, 5, z + 0.75], [x + 2.75, 6.5, z + 2.75], "#egg"))
    return boxes


def model(species, stage, count=1):
    elements = [
        N.box([1, 0, 1], [15, 1, 15], "#twigs", top="#lining"),
        N.box([1, 1, 1], [15, 4, 3], "#twigs"),
        N.box([1, 1, 13], [15, 4, 15], "#twigs"),
        N.box([1, 1, 3], [3, 4, 13], "#twigs"),
        N.box([13, 1, 3], [15, 4, 13], "#twigs"),
    ] + eggs_in(count)
    return {
        "parent": "minecraft:block/block",
        "textures": {"particle": "harmontide:block/riflebird_nest", "twigs": "harmontide:block/riflebird_nest",
                     "lining": "harmontide:block/riflebird_nest_lining", "egg": f"harmontide:block/harpy_egg_{species}_{stage}"},
        "elements": elements,
    }


def main():
    for old in glob.glob(os.path.join(ASSETS, "textures", "block", "harpy_egg_*.png")) + glob.glob(os.path.join(ASSETS, "models", "block", "harpy_egg_*.json")):
        os.remove(old)
    variants = {}
    for i, (sp, (base, f1, f2)) in enumerate(SPECIES.items()):
        for stage in range(4):
            write_png(os.path.join(ASSETS, "textures", "block", f"harpy_egg_{sp}_{stage}.png"), egg_texture(base, f1, f2, stage, 100 + i))
            N.dump(os.path.join(ASSETS, "models", "block", f"harpy_egg_{sp}_{stage}.json"), model(sp, stage))
            for count in range(1, 5):
                name = f"harpy_egg_{sp}_{stage}" + ("" if count == 1 else f"_x{count}")
                if count > 1:
                    N.dump(os.path.join(ASSETS, "models", "block", name + ".json"), model(sp, stage, count))
                variants[f"eggs={count},hatch={stage},species={sp}"] = {"model": "harmontide:block/" + name}
    N.dump(os.path.join(ASSETS, "blockstates", "harpy_egg.json"), {"variants": variants})
    N.dump(os.path.join(ASSETS, "models", "item", "harpy_egg.json"), {"parent": "harmontide:block/harpy_egg_sparrow_harpy_0"})
    print(f"harpy egg assets written ({len(SPECIES)} species x 4 stages x 1-4 eggs)")


if __name__ == "__main__":
    main()
