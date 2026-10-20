#!/usr/bin/env python3
"""Writes everything the game needs for the families in tools/family_concepts.py that is pure data:

    src/main/java/.../entity/girl/GirlSpecies.java      the species table (ids, family, sizes, render scale, colours, variants)
    data/monstergirls/tags/worldgen/biome/spawns_<id>.json and neoforge/biome_modifier/<id>_spawns.json
    assets/monstergirls/models/item/<id>_spawn_egg.json
    data/minecraft/tags/entity_type/powder_snow_walkable_mobs.json  (cats, foxes and hooves of the snow)
    the girl egg block assets (leathery lamia eggs, silk egg sacs, merfolk roe)
    lang entries (merged into en_us.json)

Run after the model scripts (it reads the model heights from the geo files):
    python tools/gen_girl_data.py
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import family_concepts as C  # noqa: E402
import harpy_lib as H  # noqa: E402
import gen_nekomata, gen_kitsune, gen_lamia, gen_arachne, gen_centaur, gen_merfolk, gen_slime  # noqa: E402,E401

REPO = H.REPO
RES = os.path.join(REPO, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "monstergirls")
DATA = os.path.join(RES, "data", "monstergirls")
JAVA = os.path.join(REPO, "src", "main", "java", "com", "linkyoshi90", "monstergirls", "entity", "girl")
GEN = {"nekomata": gen_nekomata, "kitsune": gen_kitsune, "lamia": gen_lamia, "arachne": gen_arachne, "centaur": gen_centaur,
       "merfolk": gen_merfolk, "slime": gen_slime}

# family -> (Java enum, hitbox width, spawn category, group size, weight)
FAMILY = {
    "nekomata": ("NEKOMATA", 0.6, "creature", (1, 1), 6),
    "kitsune": ("KITSUNE", 0.6, "creature", (1, 2), 7),
    "lamia": ("LAMIA", 0.8, "creature", (1, 1), 5),
    "arachne": ("ARACHNE", 1.2, "creature", (1, 1), 5),
    "centaur": ("CENTAUR", 1.1, "creature", (2, 4), 6),
    "merfolk": ("MERFOLK", 0.8, "water_creature", (1, 2), 4),
    "slime": ("SLIME", 0.8, "creature", (1, 2), 5),
}
GROUP = {"lioness_nekomata": (3, 5), "orca_mermaid": (2, 3), "fennec_kitsune": (2, 3), "corsac_kitsune": (2, 3)}
CATEGORY = {"axolotl_mermaid": "underground_water_creature", "amethyst_slime": "monster", "magma_slime": "monster",
            "seal_mermaid": "creature"}


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def hexint(c):
    c = c.lstrip("#") if isinstance(c, str) else "%02x%02x%02x" % c[:3]
    return "0x" + c.upper()


def model_height(sp):
    geo = json.load(open(os.path.join(ASSETS, "geo", "entity", sp + ".geo.json")))
    top = 0.0
    for b in geo["minecraft:geometry"][0]["bones"]:
        if b["name"].startswith(("tail2", "antlers", "horns", "crystals", "sprout", "ear_", "hair")):
            continue
        for c in b.get("cubes", []):
            top = max(top, c["origin"][1] + c["size"][1])
    return top


def main():
    rows = []
    lang = {}
    snow_walkers = []
    for fam, s in C.all_species():
        fid = fam["id"]
        sp = s["id"]
        gen = GEN[fid]
        gd = gen.SPECIES[sp]
        keys = [k for k, _, _ in s["variants"]]
        assert keys[1:] == list(gd["variants"]), (sp, keys, list(gd["variants"]))
        pal = gd["pal"]
        c1 = pal.get("FUR") or pal.get("HAIR")
        c2 = pal.get("HAIR") if pal.get("HAIR") != c1 else pal.get("MARK", "#ffffff")
        enum, width, cat, grp, weight = FAMILY[fid]
        mh = model_height(sp)
        rows.append((sp, enum, s["height"], round(mh, 2), width, hexint(c1), hexint(c2), keys, CATEGORY.get(sp, cat)))
        # spawns
        dump(os.path.join(DATA, "tags", "worldgen", "biome", f"spawns_{sp}.json"), {"values": ["minecraft:" + b for b in s["biomes"]]})
        lo, hi = GROUP.get(sp, grp)
        dump(os.path.join(DATA, "neoforge", "biome_modifier", f"{sp}_spawns.json"), {
            "type": "neoforge:add_spawns", "biomes": f"#monstergirls:spawns_{sp}",
            "spawners": {"type": f"monstergirls:{sp}", "weight": weight, "minCount": lo, "maxCount": hi}})
        dump(os.path.join(ASSETS, "models", "item", f"{sp}_spawn_egg.json"), {"parent": "minecraft:item/template_spawn_egg"})
        lang[f"entity.monstergirls.{sp}"] = s["name"] + ("" if fid in ("slime",) else {"nekomata": " Nekomata", "kitsune": " Kitsune",
                                                                                     "lamia": " Lamia", "arachne": " Arachne",
                                                                                     "centaur": " Centaur", "merfolk": " Mermaid"}[fid])
        lang[f"item.monstergirls.{sp}_spawn_egg"] = lang[f"entity.monstergirls.{sp}"] + " Spawn Egg"
        if sp in ("snow_leopard_nekomata", "lynx_nekomata", "arctic_kitsune", "reindeer_centaur"):
            snow_walkers.append("monstergirls:" + sp)
    dump(os.path.join(RES, "data", "minecraft", "tags", "entity_type", "powder_snow_walkable_mobs.json"), {"replace": False, "values": snow_walkers})

    # the Java species table
    lines = []
    for sp, enum, height, mh, width, c1, c2, keys, cat in rows:
        ks = ", ".join(f'"{k}"' for k in keys)
        lines.append(f'    {sp.upper()}("{sp}", Family.{enum}, {height}F, {mh}F, {width}F, MobCategory.{cat.upper()}, {c1}, {c2}, {ks})')
    java = '''package com.linkyoshi90.monstergirls.entity.girl;

import net.minecraft.world.entity.MobCategory;

import java.util.List;

/**
 * Every species of the families after the harpies and anurans. GENERATED by tools/gen_girl_data.py from
 * tools/family_concepts.py and the model files; do not edit by hand (re-run the script instead).
 * The first variant is the default colours (the plain texture); the others are textures/entity/&lt;id&gt;_&lt;variant&gt;.png.
 */
public enum GirlSpecies {
%s;

    public enum Family { NEKOMATA, KITSUNE, LAMIA, ARACHNE, CENTAUR, MERFOLK, SLIME }

    private final String id;
    private final Family family;
    private final float height;
    private final float modelHeight;
    private final float width;
    private final MobCategory category;
    private final int eggColor;
    private final int eggSpots;
    private final List<String> variants;

    GirlSpecies(String id, Family family, float height, float modelHeight, float width, MobCategory category, int eggColor, int eggSpots, String... variants) {
        this.id = id;
        this.family = family;
        this.height = height;
        this.modelHeight = modelHeight;
        this.width = width;
        this.category = category;
        this.eggColor = eggColor;
        this.eggSpots = eggSpots;
        this.variants = List.of(variants);
    }

    /** The registry id, e.g. "serval_nekomata". */
    public String id() {
        return this.id;
    }

    public Family family() {
        return this.family;
    }

    /** Standing height in blocks (the hitbox). */
    public float height() {
        return this.height;
    }

    public float width() {
        return this.width;
    }

    /** How much the renderer scales the model (which is modelHeight units tall) to reach her height. */
    public float renderScale() {
        return this.height * 16.0F / this.modelHeight;
    }

    public MobCategory category() {
        return this.category;
    }

    public int eggColor() {
        return this.eggColor;
    }

    public int eggSpots() {
        return this.eggSpots;
    }

    /** Colour variant keys, the default first. */
    public List<String> variants() {
        return this.variants;
    }

    public static GirlSpecies byId(String id) {
        for (GirlSpecies s : values()) if (s.id.equals(id)) return s;
        return null;
    }
}
''' % ",\n".join(lines)
    os.makedirs(JAVA, exist_ok=True)
    with open(os.path.join(JAVA, "GirlSpecies.java"), "w", encoding="utf-8", newline="\n") as fh:
        fh.write(java)

    # girl eggs: leathery (lamia), silk egg sac (arachne), roe (merfolk)
    eggs_block()
    lang.update({
        "block.monstergirls.girl_egg": "Clutch", "block.monstergirls.girl_egg.leathery": "Lamia Eggs",
        "item.monstergirls.shed_skin": "Shed Skin", "item.monstergirls.exoskeleton": "Moulted Exoskeleton",
        "bond.monstergirls.girl.wild": "keeps her distance (%s)", "bond.monstergirls.girl.curious": "is curious about you (%s)",
        "bond.monstergirls.girl.fond": "is fond of you (%s)", "bond.monstergirls.girl.high": "is very close to you (%s)",
        "bond.monstergirls.girl.devoted": "is devoted to you (%s)", "bond.monstergirls.girl.full": "would do anything for you (%s)",
        "message.monstergirls.true_form": "%s has awakened her true form",
        "message.monstergirls.swat": "%s has had enough petting for now",
        "message.monstergirls.stash_found": "You found %s's stash. She laughs and runs circles around you",
        "message.monstergirls.cold": "%s is too cold to move",
        "message.monstergirls.ride_refused": "%s does not let you ride her (yet)",
        "itemGroup.monstergirls": "MonsterGirls",
    })
    path = os.path.join(ASSETS, "lang", "en_us.json")
    import collections
    cur = json.load(open(path, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
    cur.update(lang)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(json.dumps(cur, indent=2, ensure_ascii=False) + "\n")
    print(f"{len(rows)} species: GirlSpecies.java, spawns, item models, lang ({len(lang)} keys)")


def eggs_block():
    import random
    tex = {"leathery": ((236, 228, 206), (206, 196, 170)), "silk": ((244, 244, 240), (214, 212, 204)), "roe": ((246, 186, 140), (232, 140, 96))}
    variants = {}
    for kind, (a, b) in tex.items():
        rnd = random.Random(kind)
        rows = []
        for y in range(16):
            row = []
            for x in range(16):
                c = a if rnd.random() > 0.25 else b
                if kind == "silk" and (x + y) % 5 == 0:
                    c = (226, 226, 220)
                if kind == "roe" and (x % 4 == 1 and y % 4 == 1):
                    c = (60, 40, 30)
                row.append(c + (255,))
            rows.append(row)
        H.write_png(os.path.join(ASSETS, "textures", "block", f"girl_egg_{kind}.png"), rows)
        if kind == "silk":
            elements = [{"from": [3, 0, 3], "to": [13, 9, 13], "faces": {f: {"texture": "#egg"} for f in ("north", "south", "east", "west", "up", "down")}},
                        {"from": [5, 9, 5], "to": [11, 12, 11], "faces": {f: {"texture": "#egg"} for f in ("north", "south", "east", "west", "up", "down")}}]
        else:
            elements = []
            for x, z in ((3, 3), (8, 4), (4, 8), (9, 9), (6, 6)):
                h = 5 if kind == "leathery" else 2
                w = 4 if kind == "leathery" else 3
                elements.append({"from": [x, 0, z], "to": [x + w, h, z + w - (1 if kind == "leathery" else 0)],
                                 "faces": {f: {"texture": "#egg"} for f in ("north", "south", "east", "west", "up", "down")}})
        dump(os.path.join(ASSETS, "models", "block", f"girl_egg_{kind}.json"),
             {"parent": "minecraft:block/block", "textures": {"particle": f"monstergirls:block/girl_egg_{kind}", "egg": f"monstergirls:block/girl_egg_{kind}"},
              "elements": elements})
        variants[f"kind={kind}"] = {"model": f"monstergirls:block/girl_egg_{kind}"}
    dump(os.path.join(ASSETS, "blockstates", "girl_egg.json"), {"variants": variants})
    dump(os.path.join(ASSETS, "models", "item", "girl_egg.json"), {"parent": "monstergirls:block/girl_egg_leathery"})
    dump(os.path.join(DATA, "loot_table", "blocks", "girl_egg.json"), {"type": "minecraft:block", "pools": []})
    # shed skin / exoskeleton item textures: a pale curl of skin, a hollow amber shell
    for name, base, mark in (("shed_skin", (226, 214, 180), (176, 160, 120)), ("exoskeleton", (186, 132, 82), (120, 80, 48))):
        rnd = random.Random(name)
        rows = [[(0, 0, 0, 0)] * 16 for _ in range(16)]
        for y in range(2, 14):
            for x in range(16):
                cx = 8 + 3 * __import__("math").sin(y * 0.6)
                if abs(x - cx) < 3.2:
                    c = mark if (x + y) % 3 == 0 or rnd.random() < 0.15 else base
                    rows[y][x] = c + (255,)
        H.write_png(os.path.join(ASSETS, "textures", "item", name + ".png"), rows)
        dump(os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"monstergirls:item/{name}"}})


if __name__ == "__main__":
    main()
