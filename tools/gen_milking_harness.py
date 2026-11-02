#!/usr/bin/env python3
"""Assets for the optional milking features (see MonsterGirlsConfig): the harness texture, each cow's blush overlay,
the milk drip particle, and the names on the settings page.

    python tools/gen_milking_harness.py
"""
import glob
import json
import re
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_harmontide_items as I  # noqa: E402
import gen_milk_assets as M  # noqa: E402
import harpy_lib as H  # noqa: E402

ASSETS = I.ASSETS
# leather, brass, copper, hose: the quarters of the texture, matching MilkingHarnessLayer
CELLS = [("#5a3a24", "#7a5436"), ("#c8a040", "#ecd078"), ("#b8683a", "#e09a68"), ("#2a2624", "#44403c")]
BLUSH = (232, 72, 96)
FAMILIES = ["harpy", "anuran", "nekomata", "kitsune", "lamia", "arachne", "centaur", "merfolk", "slime", "jackalope", "minotaur",
            "mollusk", "elf", "undead", "lizardfolk", "dragon", "devil", "bug", "flora", "canid", "rodent", "crustacean",
            "marsupial", "armoured", "construct", "bat", "theropod", "rider"]


def rgb(colour):
    return tuple(int(colour[i:i + 2], 16) for i in (1, 3, 5)) + (255,)


def harness_texture():
    """Each quarter a little mottled, so large faces do not read as flat colour."""
    rnd = random.Random(7)
    rows = []
    for y in range(32):
        row = []
        for x in range(32):
            dark, light = CELLS[(y // 16) * 2 + x // 16]
            row.append(rgb(light if rnd.random() < 0.18 else dark))
        rows.append(row)
    return rows


def blush_rows(u, v, size=512, s=1.0):
    """Her head's north face is 32 pixels square at (u, v), or s times that: cheeks below the eyes, over the faint pink already painted."""
    rows = [[(0, 0, 0, 0)] * size for _ in range(size)]
    for left in (2, 20):
        for y in range(23, 29):
            edge = y in (23, 28)
            for x in range(left + (1 if edge else 0), left + (9 if edge else 10)):
                strong = 24 <= y <= 27 and left + 2 <= x <= left + 7
                for yy in range(int(y * s), max(int(y * s) + 1, int((y + 1) * s))):
                    for xx in range(int(x * s), max(int(x * s) + 1, int((x + 1) * s))):
                        rows[int(v) + yy][int(u) + xx] = BLUSH + (150 if strong else 90,)
    return rows


def girl_ids():
    with open(os.path.join(H.REPO, "src", "main", "java", "com", "linkyoshi90", "monstergirls", "entity", "girl", "GirlSpecies.java"), encoding="utf-8") as fh:
        return re.findall(r'^\s+[A-Z0-9_]+\("([a-z0-9_]+)", Family', fh.read(), re.M)


def main():
    H.write_png(os.path.join(ASSETS, "textures", "entity", "milking_harness.png"), harness_texture())
    for name in girl_ids():                              # every girl: a species with no face gets an empty overlay
        with open(os.path.join(ASSETS, "geo", "entity", name + ".geo.json"), encoding="utf-8") as fh:
            geo = json.load(fh)["minecraft:geometry"][0]
        size = geo["description"]["texture_width"]
        head = next((b for b in geo["bones"] if b["name"] == "head" and b.get("cubes")), None)
        if head is None:
            rows = [[(0, 0, 0, 0)] * size for _ in range(size)]
        else:
            face = head["cubes"][0]["uv"]["north"]
            rows = blush_rows(face["uv"][0], face["uv"][1], size, abs(face["uv_size"][0]) / 32)
        H.write_png(os.path.join(ASSETS, "textures", "entity", name + "_blush.png"), rows)
    I.dump(os.path.join(ASSETS, "particles", "milk_drip.json"), {"textures": ["minecraft:drip_fall"]})

    prefix = "monstergirls.configuration."
    lang = {prefix + "title": "MonsterGirls Settings",
            prefix + "detailedSparrowModel": "Detailed sparrow model",
            prefix + "wildSpawnChance": "Wild spawn chance",
            prefix + "wildSpacing": "Wild spacing (blocks)",
            prefix + "spawnRate": "Spawn rate (%)",
            prefix + "variantRate": "Variant spawn rate (%)",
            prefix + "paintRate": "Paint spawn rate (%)",
            prefix + "familySpawnRates": "Family spawn rates",
            prefix + "milking": "Milking",
            prefix + "milkDrips": "Milk drips",
            prefix + "milkEmbarrassed": "Embarrassed reactions",
            prefix + "milkHarness": "Milking harness"}
    for family in FAMILIES:
        lang[prefix + family] = family.capitalize() + " (%)"
    M.add_lang(lang)


if __name__ == "__main__":
    main()
