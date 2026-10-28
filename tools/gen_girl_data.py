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
    # added 2026-10-24
    "canid": ("CANID", 0.7, "creature", (1, 2), 6),
    "rodent": ("RODENT", 0.7, "creature", (1, 2), 6),
    # added 2026-10-25
    "crustacean": ("CRUSTACEAN", 0.7, "creature", (1, 3), 6),
    "marsupial": ("MARSUPIAL", 0.7, "creature", (2, 4), 6),
    "armoured": ("ARMOURED", 0.7, "creature", (1, 2), 6),
    "construct": ("CONSTRUCT", 0.9, "creature", (1, 1), 2),
    "bat": ("BAT", 0.6, "creature", (2, 3), 6),
}
GROUP = {"lioness_nekomata": (3, 5), "orca_mermaid": (2, 3), "fennec_kitsune": (2, 3), "corsac_kitsune": (2, 3), "dolphin_mermaid": (2, 3),
         "al_miraj_jackalope": (3, 5), "bison_minotaur": (3, 6), "high_elf": (2, 4), "bee_bug": (2, 4),
         "dugong_mermaid": (2, 2), "kelpie_centaur": (1, 1), "wolf_canid": (2, 4), "panda_canid": (1, 3), "anubis_canid": (1, 1),
         "pegasus_centaur": (1, 2), "lich_undead": (1, 1), "capybara_rodent": (2, 4), "leshy_flora": (1, 1), "llorona_undead": (1, 1),
         "black_tortoise_lizardfolk": (1, 1), "grizzly_canid": (1, 1), "red_eared_slider_lizardfolk": (2, 4), "galapagos_tortoise_lizardfolk": (1, 2),
         "kangaroo_marsupial": (3, 5), "golem_construct": (1, 1), "jellyfish_mollusk": (2, 4), "ahuizotl_mermaid": (1, 2),
         "unicorn_centaur": (1, 1), "firefly_bug": (3, 6), "mummy_undead": (1, 2), "chupacabra_lizardfolk": (1, 1),
         "sheep_minotaur": (3, 5), "highland_minotaur": (2, 3), "yak_minotaur": (2, 3), "dark_elf": (1, 2), "medusa_lamia": (1, 1),
         "oni_devil": (1, 1), "mantis_bug": (1, 1), "ender_slime": (1, 2), "venus_flytrap_flora": (1, 1), "siren_mermaid": (1, 1),
         "tusktaur_centaur": (2, 3), "aurorantler_centaur": (1, 2), "rhiwool_minotaur": (1, 2), "meloncollie_canid": (1, 1), "clockhare_jackalope": (1, 1),
         "axolume_mermaid": (1, 2), "aegis_boar_armoured": (1, 1), "blaze_cat_nekomata": (1, 1), "gargoyle_construct": (1, 1),
         "anvil_tortoise_lizardfolk": (1, 1), "gliderune_marsupial": (2, 4), "slothren_armoured": (1, 1), "coatimano_rodent": (2, 4),
         "waterlil_nekomata": (1, 1), "talosling_construct": (1, 1), "mantarrow_mermaid": (1, 2), "sphinx_centaur": (1, 1), "brownie_elf": (1, 1),
         "cheshire_nekomata": (1, 1), "kesaran_pasaran_flora": (1, 3), "tsuchinoko_lamia": (1, 1), "platypus_mermaid": (1, 2),
         "moonseal_mermaid": (1, 3), "marmotaur_rodent": (1, 3), "bambadger_rodent": (1, 2), "boghare_jackalope": (1, 2), "gremlin_devil": (1, 2),
         "karakasa_obake_devil": (1, 1), "alebrije_dragon": (1, 1),
         "rust_otter_mermaid": (1, 2), "tin_squirrel_construct": (1, 1), "wattlefox_kitsune": (1, 2), "scarabronze_bug": (1, 1), "date_bat": (1, 3),
         "snowloper_nekomata": (1, 2), "jaglare_nekomata": (1, 1), "squirricerat_rodent": (1, 2), "kamaitachi_rodent": (1, 2), "momoshika_centaur": (1, 2),
         "chochin_obake_devil": (1, 1),
         "gribboar_armoured": (1, 3), "silkcoon_rodent": (1, 2), "bronzejaw_minotaur": (1, 3), "cocoskink_lizardfolk": (1, 2), "prairhorn_centaur": (2, 4),
         "halberelk_centaur": (1, 2), "vulphea_kitsune": (1, 1), "cait_sith_nekomata": (1, 1), "cu_sith_canid": (1, 1), "yeti_canid": (1, 1),
         "undine_slime": (1, 1), "ittan_momen_devil": (1, 1),
         "crocora_lizardfolk": (1, 2), "iron_kobold_lizardfolk": (1, 3), "mooskuh_minotaur": (1, 3), "bellmare_centaur": (1, 2), "vicunero_centaur": (2, 3),
         "rockhyrax_rodent": (2, 4), "riverdillo_armoured": (1, 2), "reeffin_mermaid": (1, 2), "caraviper_lamia": (1, 1), "bakeneko_nekomata": (1, 1),
         "rokurokubi_devil": (1, 1), "huli_jing_kitsune": (1, 1),
         "jorogumo_arachne": (1, 1), "dunefennec_kitsune": (1, 3), "floodcrab_crustacean": (2, 4), "temple_mantis_bug": (1, 1),
         "sword_koi_mermaid": (1, 2), "squidkin_mollusk": (1, 3), "reedbull_minotaur": (1, 3), "kikimora_devil": (1, 1),
         "cockatrice_dragon": (1, 1), "valkyrie_elf": (1, 1),
         "censer_lion_nekomata": (1, 1), "conch_turtle_lizardfolk": (1, 3), "date_beetle_bug": (1, 3), "dustbison_minotaur": (2, 4),
         "frond_bat": (1, 3), "sawfin_mermaid": (1, 2), "stagel_centaur": (1, 3), "klabuster_slime": (1, 2), "dryad_flora": (1, 1),
         "amarok_canid": (1, 1), "yurei_undead": (1, 1), "lamassu_centaur": (1, 1),
         "spoodr_arachne": (1, 3), "urchskin_lamia": (1, 2), "pineskrell_rodent": (1, 3), "turtfolk_lizardfolk": (1, 2), "tarnhelm_canid": (1, 1),
         "palmyr_centaur": (1, 2), "skjoldram_minotaur": (1, 3), "bastet_nekomata": (1, 1), "rusalka_mermaid": (1, 1), "ushi_oni_arachne": (1, 1),
         "domovoi_elf": (1, 1),
         "tin_jack_construct": (1, 1), "turquoise_serpent_lamia": (1, 1), "moopie_mermaid": (1, 2), "tuskin_minotaur": (1, 3), "sabre_nekomata": (1, 2),
         "obsidian_hound_canid": (1, 1), "kasurenijimi_undead": (1, 1), "camazotz_bat": (1, 2), "akaname_devil": (1, 1), "hoop_snake_lamia": (1, 2),
         "draugr_undead": (1, 2)}
CATEGORY = {"axolotl_mermaid": "underground_water_creature", "amethyst_slime": "monster", "magma_slime": "monster",
            "seal_mermaid": "creature", "kraken_mollusk": "water_creature", "ammonite_mollusk": "water_creature",
            "night_elf": "monster", "blood_elf": "monster", "salamander_lizardfolk": "monster", "turtle_lizardfolk": "creature",
            "will_o_wisp_undead": "monster", "mosquito_bug": "monster", "greater_devil": "monster", "succubus_devil": "monster",
            "jack_o_lantern_devil": "monster", "jiang_shi_devil": "monster", "lesser_devil": "monster", "lich_undead": "monster", "jellyfish_mollusk": "water_creature", "mummy_undead": "monster", "chupacabra_lizardfolk": "monster",
            "dark_elf": "monster", "oni_devil": "monster", "ender_slime": "monster", "siren_mermaid": "water_creature",
            "axolume_mermaid": "underground_water_creature", "blaze_cat_nekomata": "monster", "gargoyle_construct": "monster",
            "mantarrow_mermaid": "water_creature", "moonseal_mermaid": "creature", "gremlin_devil": "creature", "karakasa_obake_devil": "creature",
            "rust_otter_mermaid": "creature", "chochin_obake_devil": "creature", "undine_slime": "creature", "ittan_momen_devil": "creature",
            "rokurokubi_devil": "creature", "jorogumo_arachne": "creature", "sword_koi_mermaid": "water_creature", "kikimora_devil": "creature",
            "cockatrice_dragon": "creature", "sawfin_mermaid": "water_creature", "yurei_undead": "monster",
            "rusalka_mermaid": "water_creature", "ushi_oni_arachne": "creature", "spoodr_arachne": "creature",
            "moopie_mermaid": "water_creature", "kasurenijimi_undead": "creature", "akaname_devil": "creature", "tin_jack_construct": "creature"}
# hitbox widths that differ from the family's
WIDTH = {"kraken_mollusk": 1.4, "ammonite_mollusk": 1.2, "ent_flora": 1.4, "alraune_flora": 1.2, "frankenstein_undead": 0.8,
         "gryphon_dragon": 1.3, "long_dragon": 1.0, "jormungandr_dragon": 1.2, "wyvern_dragon": 1.2, "wyrm_dragon": 1.3,
         "fairy_bug": 0.4, "scorpion_bug": 1.2, "moth_larva_bug": 0.6, "papillon_larva_bug": 0.6, "living_doll_undead": 0.4,
         "kannae_dragon": 0.5, "lesser_devil": 0.5, "mandragora_flora": 0.5, "polar_bear_canid": 0.8, "grizzly_canid": 0.8, "kobold_canid": 0.5,
         "ratatoskr_rodent": 0.5, "galapagos_tortoise_lizardfolk": 0.9, "golem_construct": 1.0, "kangaroo_marsupial": 0.7, "hermit_crab_crustacean": 0.6, "firefly_bug": 0.4, "black_bear_canid": 0.8, "unicorn_centaur": 1.2,
         "yak_minotaur": 1.0, "oni_devil": 0.8, "venus_flytrap_flora": 1.2, "cave_bat": 0.5, "wolpertinger_jackalope": 0.5,
         "tusktaur_centaur": 1.5, "rhiwool_minotaur": 0.9, "needlehog_rodent": 0.6, "aegis_boar_armoured": 0.8, "gargoyle_construct": 0.8,
         "anvil_tortoise_lizardfolk": 0.9, "gliderune_marsupial": 0.5, "sphinx_centaur": 1.3, "brownie_elf": 0.4,
         "kesaran_pasaran_flora": 0.45, "tsuchinoko_lamia": 0.8, "gremlin_devil": 0.5, "tin_squirrel_construct": 0.55, "yeti_canid": 0.9, "iron_kobold_lizardfolk": 0.55,
         "kikimora_devil": 0.5, "dunefennec_kitsune": 0.55,
         "klabuster_slime": 0.6, "date_beetle_bug": 0.5, "conch_turtle_lizardfolk": 0.6, "lamassu_centaur": 1.3, "stagel_centaur": 1.0, "amarok_canid": 0.8,
         "spoodr_arachne": 0.9, "ushi_oni_arachne": 1.5, "domovoi_elf": 0.45, "skjoldram_minotaur": 0.8,
         "tuskin_minotaur": 1.0, "akaname_devil": 0.5}
# the name shown in game: the species name plus the family's word, unless NAME says otherwise
SUFFIX = {"nekomata": " Nekomata", "kitsune": " Kitsune", "lamia": " Lamia", "arachne": " Arachne", "centaur": " Centaur", "merfolk": " Mermaid",
          "slime": "", "jackalope": "", "minotaur": " Minotaur", "mollusk": "", "elf": "", "undead": "", "lizardfolk": "", "dragon": "",
          "devil": "", "bug": "", "flora": "", "canid": "", "rodent": "", "crustacean": " Girl", "marsupial": " Girl",
          "armoured": " Girl", "construct": "", "bat": " Girl"}
NAME = {"jack_jackalope": "Jack Jackalope", "kraken_mollusk": "Kraken", "octopus_mollusk": "Octopus Girl", "snail_mollusk": "Snail Girl",
        "long_dragon": "Long Dragon", "zombie_undead": "Zombie Girl", "phantom_undead": "Phantom Girl", "cactus_flora": "Cactus Girl",
        "wolf_canid": "Wolf Girl", "polar_bear_canid": "Polar Bear Girl", "panda_canid": "Panda Girl", "mimic_slime": "Mimic",
        "grizzly_canid": "Grizzly Girl", "capybara_rodent": "Capybara Girl", "leshy_flora": "Leshy", "llorona_undead": "La Llorona",
        "black_tortoise_lizardfolk": "Black Tortoise", "golem_construct": "Golem", "crab_crustacean": "Crab Girl",
        "hermit_crab_crustacean": "Hermit Crab Girl", "jellyfish_mollusk": "Jellyfish Girl", "ahuizotl_mermaid": "Ahuizotl",
        "unicorn_centaur": "Unicorn", "black_bear_canid": "Black Bear Girl", "koala_marsupial": "Koala Girl", "gecko_lizardfolk": "Gecko Girl",
        "firefly_bug": "Firefly", "mummy_undead": "Mummy", "sheep_minotaur": "Sheep Girl", "highland_minotaur": "Highland Minotaur",
        "cave_bat": "Bat Girl", "dark_elf": "Dark Elf", "medusa_lamia": "Medusa", "oni_devil": "Oni", "mantis_bug": "Mantis Girl",
        "ender_slime": "Ender Slime", "wolpertinger_jackalope": "Wolpertinger", "venus_flytrap_flora": "Venus Flytrap", "siren_mermaid": "Siren",
        "tusktaur_centaur": "Tusktaur", "aurorantler_centaur": "Aurorantler", "rhiwool_minotaur": "Rhiwool", "meloncollie_canid": "Meloncollie",
        "needlehog_rodent": "Needlehog", "clockhare_jackalope": "Clockhare", "axolume_mermaid": "Axolume", "aegis_boar_armoured": "Aegis Boar",
        "blaze_cat_nekomata": "Blaze Cat", "gargoyle_construct": "Gargoyle", "anvil_tortoise_lizardfolk": "Anvil Tortoise",
        "gliderune_marsupial": "Gliderune", "slothren_armoured": "Slothren", "coatimano_rodent": "Coatimano", "waterlil_nekomata": "Waterlil",
        "talosling_construct": "Talosling", "mantarrow_mermaid": "Mantarrow", "sphinx_centaur": "Sphinx", "brownie_elf": "Brownie",
        "cheshire_nekomata": "Cheshire Cat", "kesaran_pasaran_flora": "Kesaran Pasaran", "tsuchinoko_lamia": "Tsuchinoko", "platypus_mermaid": "Platypus",
        "moonseal_mermaid": "Moonseal", "marmotaur_rodent": "Marmotaur", "bambadger_rodent": "Bambadger", "boghare_jackalope": "Boghare",
        "gremlin_devil": "Gremlin", "karakasa_obake_devil": "Karakasa Obake", "alebrije_dragon": "Alebrije",
        "rust_otter_mermaid": "Rust Otter", "tin_squirrel_construct": "Tin Squirrel", "wattlefox_kitsune": "Wattlefox", "scarabronze_bug": "Scarabronze",
        "date_bat": "Datebat", "snowloper_nekomata": "Snowloper", "jaglare_nekomata": "Jaglare", "squirricerat_rodent": "Squirricerat",
        "kamaitachi_rodent": "Kamaitachi", "momoshika_centaur": "Momoshika", "chochin_obake_devil": "Chochin-obake",
        "gribboar_armoured": "Gribboar", "silkcoon_rodent": "Silkcoon", "bronzejaw_minotaur": "Bronzejaw Ox", "cocoskink_lizardfolk": "Cocoskink",
        "prairhorn_centaur": "Prairhorn", "halberelk_centaur": "Halberelk", "vulphea_kitsune": "Vulphea", "cait_sith_nekomata": "Cait Sìth",
        "cu_sith_canid": "Cù-sìth", "yeti_canid": "Yeti", "undine_slime": "Undine", "ittan_momen_devil": "Ittan-momen",
        "crocora_lizardfolk": "Crocora", "iron_kobold_lizardfolk": "Iron Kobold", "mooskuh_minotaur": "Mooskuh", "bellmare_centaur": "Bellmare",
        "vicunero_centaur": "Vicunero", "rockhyrax_rodent": "Rockhyrax", "riverdillo_armoured": "Riverdillo", "reeffin_mermaid": "Reeffin",
        "caraviper_lamia": "Caraviper", "bakeneko_nekomata": "Bakeneko", "rokurokubi_devil": "Rokurokubi", "huli_jing_kitsune": "Huli Jing",
        "jorogumo_arachne": "Jorogumo", "dunefennec_kitsune": "Dunefennec", "floodcrab_crustacean": "Floodcrab", "temple_mantis_bug": "Temple Mantis",
        "sword_koi_mermaid": "Sword Koi", "squidkin_mollusk": "Squidkin", "reedbull_minotaur": "Reedbull", "kikimora_devil": "Kikimora",
        "cockatrice_dragon": "Cockatrice", "valkyrie_elf": "Valkyrie",
        "censer_lion_nekomata": "Censer Lion", "conch_turtle_lizardfolk": "Conch Mail Turtle", "date_beetle_bug": "Date Palm Beetle",
        "dustbison_minotaur": "Dustbison", "frond_bat": "Frondbat", "sawfin_mermaid": "Sawfin", "stagel_centaur": "Stagel", "klabuster_slime": "Klabuster",
        "dryad_flora": "Dryad", "amarok_canid": "Amarok", "yurei_undead": "Yurei", "lamassu_centaur": "Lamassu",
        "spoodr_arachne": "Spoodr", "urchskin_lamia": "Urchskin", "pineskrell_rodent": "Pineskrell", "turtfolk_lizardfolk": "Turtfolk",
        "tarnhelm_canid": "Tarnhelm", "palmyr_centaur": "Palmyr", "skjoldram_minotaur": "Skjoldram", "bastet_nekomata": "Bastet",
        "rusalka_mermaid": "Rusalka", "ushi_oni_arachne": "Ushi-oni", "domovoi_elf": "Domovoi",
        "tin_jack_construct": "Tin Jack", "turquoise_serpent_lamia": "Turquoise Serpent", "moopie_mermaid": "Moopie", "tuskin_minotaur": "Tuskin",
        "sabre_nekomata": "Sabre", "obsidian_hound_canid": "Obsidian Hound", "kasurenijimi_undead": "Kasurenijimi", "camazotz_bat": "Camazotz",
        "akaname_devil": "Akaname", "hoop_snake_lamia": "Hoop Snake", "draugr_undead": "Draugr"}


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
        lang[f"journal.monstergirls.{sp}.home"] = s.get("group", "")                       # her page in the Harmontide journal
        sig = s.get("signature")
        if sig:
            lang[f"journal.monstergirls.{sp}.signature"] = f"{sig[0]}: {sig[1]}"
        if sp in ("snow_leopard_nekomata", "lynx_nekomata", "arctic_kitsune", "reindeer_centaur", "highland_minotaur", "yak_minotaur", "tusktaur_centaur",
                  "aurorantler_centaur", "snowloper_nekomata", "yeti_canid"):
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
        "message.monstergirls.mended": "You mend %s's wound. She won't forget it",
        "message.monstergirls.introduced": "%s introduces you to her %s. It is yours now",
        "message.monstergirls.armed": "%s takes the weapon. She'll use it on your enemies",
        "message.monstergirls.disarmed": "%s hands back her weapon",
        "journal.monstergirls.title": "Harmontide Journal",
        "journal.monstergirls.guide.friends.title": "Making friends",
        "journal.monstergirls.guide.friends": "Feed her what she likes and pat her with an empty hand. At 10 friendship she is your friend: her gift of a behaviour starts, and a stick gives her orders (sneak for her stance).",
        "journal.monstergirls.guide.hearth.title": "A hearth",
        "journal.monstergirls.guide.hearth": "A campfire ringed by eight stone bricks. Your friends roaming free within 16 blocks settle there; a day later each builds a hut nearby. With three or more, every morning they leave presents in the hearth.",
        "journal.monstergirls.guide.expedition.title": "Expeditions",
        "journal.monstergirls.guide.expedition": "Give a grown friend an empty map: she sets off and comes back a day later with finds of her homeland, and now and then a new friend.",
        "journal.monstergirls.guide.weapons.title": "Weapons",
        "journal.monstergirls.guide.weapons": "The girls who fight for you take a bow, crossbow or gun: hand it over or throw it near her, and ammunition too. Sneak with an empty hand to take it back.",
        "journal.monstergirls.girls": "Monster girls met: %s of %s",
        "journal.monstergirls.friends": "Befriended: %s",
        "journal.monstergirls.others": "Other creatures met: %s",
        "journal.monstergirls.empty": "No one yet. Go and meet someone!",
        "journal.monstergirls.home": "Lives: %s",
        "journal.monstergirls.met": "Met",
        "journal.monstergirls.befriended": "Befriended",
        "block.monstergirls.hearth": "Harmontide Hearth",
        "message.monstergirls.hearth": "%s live here",
        "message.monstergirls.settled": "%s settles at your hearth",
        "message.monstergirls.expedition_set_off": "%s sets off with your map. She'll be back in a day",
        "message.monstergirls.expedition_back": "%s is back from her expedition, and brought you something!",
        "message.monstergirls.expedition_companion": "%s brought a friend home: %s",
        "itemGroup.monstergirls": "Harmontide",
    })
    path = os.path.join(ASSETS, "lang", "en_us.json")
    import collections
    cur = json.load(open(path, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
    cur.update(lang)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(json.dumps(cur, indent=2, ensure_ascii=False) + "\n")
    write_water_breathers(rows)
    print(f"{len(rows)} species: GirlSpecies.java, spawns, item models, lang ({len(lang)} keys), water breathers")


# who breathes water (minecraft:can_breathe_under_water): every mermaid and every mollusk but the land snail, plus these
WATER_BREATHERS = ["bullywug", "bullfrog", "tree_frog", "poison_dart_frog", "glass_frog", "goliath_frog", "desert_rain_frog", "surinam_toad",
                   "sea_snake_lamia", "kelpie_centaur", "penguin_harpy", "softshell_turtle_lizardfolk", "matamata_lizardfolk", "urchskin_lamia"]
NOT_WATER_BREATHERS = {"snail_mollusk"}


def write_water_breathers(rows):
    ids = list(WATER_BREATHERS) + [r[0] for r in rows if r[1] in ("MERFOLK", "MOLLUSK") and r[0] not in NOT_WATER_BREATHERS]
    path = os.path.join(RES, "data", "minecraft", "tags", "entity_type", "can_breathe_under_water.json")
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(json.dumps({"values": ["monstergirls:" + i for i in dict.fromkeys(ids)]}, indent=2) + "\n")


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
