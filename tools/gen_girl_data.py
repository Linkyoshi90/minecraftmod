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
import importlib  # noqa: E402

REPO = H.REPO
RES = os.path.join(REPO, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "monstergirls")
DATA = os.path.join(RES, "data", "monstergirls")
JAVA = os.path.join(REPO, "src", "main", "java", "com", "linkyoshi90", "monstergirls", "entity", "girl")

# family -> (Java enum, hitbox width, spawn category, group size, weight)
FAMILY = {
    "nekomata": ("NEKOMATA", 0.6, "creature", (1, 1), 6),
    "kitsune": ("KITSUNE", 0.6, "creature", (1, 2), 7),
    "lamia": ("LAMIA", 0.8, "creature", (1, 1), 5),
    "arachne": ("ARACHNE", 1.2, "creature", (1, 1), 5),
    "centaur": ("CENTAUR", 1.1, "creature", (2, 4), 6),
    "merfolk": ("MERFOLK", 0.8, "water_creature", (1, 2), 4),
    "slime": ("SLIME", 0.8, "creature", (1, 2), 5),
    # added 2026-10-20
    "jackalope": ("JACKALOPE", 0.6, "creature", (1, 2), 6),
    "minotaur": ("MINOTAUR", 0.8, "creature", (2, 4), 6),
    "mollusk": ("MOLLUSK", 0.7, "creature", (1, 1), 5),
    "elf": ("ELF", 0.6, "creature", (1, 3), 5),
    "undead": ("UNDEAD", 0.6, "monster", (1, 1), 6),
    "lizardfolk": ("LIZARDFOLK", 0.7, "creature", (1, 2), 6),
    "dragon": ("DRAGON", 0.9, "creature", (1, 1), 3),
    "devil": ("DEVIL", 0.6, "monster", (1, 1), 5),
    "bug": ("BUG", 0.6, "creature", (1, 2), 6),
    "flora": ("FLORA", 0.7, "creature", (1, 1), 5),
}
GROUP = {"lioness_nekomata": (3, 5), "orca_mermaid": (2, 3), "fennec_kitsune": (2, 3), "corsac_kitsune": (2, 3), "dolphin_mermaid": (2, 3),
         "al_miraj_jackalope": (3, 5), "bison_minotaur": (3, 6), "high_elf": (2, 4), "bee_bug": (2, 4)}
CATEGORY = {"axolotl_mermaid": "underground_water_creature", "amethyst_slime": "monster", "magma_slime": "monster",
            "seal_mermaid": "creature", "kraken_mollusk": "water_creature", "ammonite_mollusk": "water_creature",
            "night_elf": "monster", "blood_elf": "monster", "salamander_lizardfolk": "monster", "turtle_lizardfolk": "creature",
            "will_o_wisp_undead": "monster", "mosquito_bug": "monster", "greater_devil": "monster", "succubus_devil": "monster",
            "jack_o_lantern_devil": "monster", "jiang_shi_devil": "monster", "lesser_devil": "monster"}
# hitbox widths that differ from the family's
WIDTH = {"kraken_mollusk": 1.4, "ammonite_mollusk": 1.2, "ent_flora": 1.4, "alraune_flora": 1.2, "frankenstein_undead": 0.8,
         "gryphon_dragon": 1.3, "long_dragon": 1.0, "jormungandr_dragon": 1.2, "wyvern_dragon": 1.2, "wyrm_dragon": 1.3,
         "fairy_bug": 0.4, "scorpion_bug": 1.2, "moth_larva_bug": 0.6, "papillon_larva_bug": 0.6, "living_doll_undead": 0.4,
         "kannae_dragon": 0.5, "lesser_devil": 0.5, "mandragora_flora": 0.5}
# the name shown in game: the species name plus the family's word, unless NAME says otherwise
SUFFIX = {"nekomata": " Nekomata", "kitsune": " Kitsune", "lamia": " Lamia", "arachne": " Arachne", "centaur": " Centaur", "merfolk": " Mermaid",
          "slime": "", "jackalope": "", "minotaur": " Minotaur", "mollusk": "", "elf": "", "undead": "", "lizardfolk": "", "dragon": "",
          "devil": "", "bug": "", "flora": ""}
NAME = {"jack_jackalope": "Jack Jackalope", "kraken_mollusk": "Kraken", "octopus_mollusk": "Octopus Girl", "snail_mollusk": "Snail Girl",
        "long_dragon": "Long Dragon", "zombie_undead": "Zombie Girl", "phantom_undead": "Phantom Girl", "cactus_flora": "Cactus Girl"}


def gen_module(fid):
    """the family's model script module, or None while it has none yet"""
    try:
        return importlib.import_module("gen_" + fid)
    except ModuleNotFoundError:
        return None


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
        if b["name"].startswith(("tail2", "antlers", "horns", "crystals", "sprout", "ear_", "hair", "wing", "halo", "disguise")):
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
        gen = gen_module(fid)
        if gen is None or sp not in gen.SPECIES or not os.path.exists(os.path.join(ASSETS, "geo", "entity", sp + ".geo.json")):
            continue                                    # not modelled yet: not in the game yet
        gd = gen.SPECIES[sp]
        keys = [k for k, _, _ in s["variants"]]
        assert keys[1:] == list(gd["variants"]), (sp, keys, list(gd["variants"]))
        pal = gd["pal"]
        c1 = pal.get("FUR") or pal.get("HAIR")
        c2 = pal.get("HAIR") if pal.get("HAIR") != c1 else pal.get("MARK", "#ffffff")
        enum, width, cat, grp, weight = FAMILY[fid]
        width = WIDTH.get(sp, width)
        mh = model_height(sp)
        rows.append((sp, enum, s["height"], round(mh, 2), width, hexint(c1), hexint(c2), keys, CATEGORY.get(sp, cat)))
        # spawns (species without biomes only come by other means: the phantom, the caterpillars, the fairy queen)
        tag_path = os.path.join(DATA, "tags", "worldgen", "biome", f"spawns_{sp}.json")
        mod_path = os.path.join(DATA, "neoforge", "biome_modifier", f"{sp}_spawns.json")
        if s["biomes"]:
            dump(tag_path, {"values": ["minecraft:" + b for b in s["biomes"]]})
            lo, hi = GROUP.get(sp, grp)
            dump(mod_path, {"type": "neoforge:add_spawns", "biomes": f"#monstergirls:spawns_{sp}",
                            "spawners": {"type": f"monstergirls:{sp}", "weight": weight, "minCount": lo, "maxCount": hi}})
        else:
            for stale in (tag_path, mod_path):
                if os.path.exists(stale):
                    os.remove(stale)
        dump(os.path.join(ASSETS, "models", "item", f"{sp}_spawn_egg.json"), {"parent": "minecraft:item/template_spawn_egg"})
        lang[f"entity.monstergirls.{sp}"] = NAME.get(sp, s["name"] + SUFFIX[fid])
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

    public enum Family { %s }

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
''' % (",\n".join(lines), ", ".join(dict.fromkeys(r[1] for r in rows)))
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
        "message.monstergirls.keening": "%s keens. Danger is close",
        "itemGroup.monstergirls": "Harmontide",
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
