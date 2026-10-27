#!/usr/bin/env python3
"""HARMONTIDE_ROSTER.md: every creature in the Harmontide reference art (D:\\wamp64\\www\\images\\creatures\\, one file per
creature and look, named <creature>_<f|m>_<look>), and whether it is in the game yet.

    python tools/harmontide_roster.py

IN_GAME maps a reference creature to what plays it in the mod; add to it after each wave and re-run. EXCLUDED holds
what is not a creature to make and the ones set aside on purpose.
"""
import os
import re
import sys

ART = r"D:\wamp64\www\images\creatures"
OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "HARMONTIDE_ROSTER.md")

# reference creature -> what plays it in the game
IN_GAME = {
    "ahuizotl": "ahuizotl_mermaid", "alraune": "alraune_flora", "anubis": "anubis_canid", "arachne": "the arachne family",
    "armadillo": "armadillo_armoured", "azure_dragon": "long_dragon (the Azure Dragon)", "banshee": "banshee_undead", "bee": "bee_bug",
    "black_turtle": "black_tortoise_lizardfolk", "bullywug": "bullywug", "camel": "camel_centaur", "capybara": "capybara_rodent",
    "centaur": "horse_centaur", "chameleon": "chameleon_lizardfolk", "chupacabra": "chupacabra_lizardfolk", "crab_man": "crab_crustacean",
    "crabby": "crab_crustacean", "croc_man": "croc_lizardfolk", "dolphin": "dolphin_mermaid", "dolphin_realistic": "dolphin_mermaid",
    "dragon": "the dragon family", "drop_bear": "koala_marsupial (drop bear)", "dugong": "dugong_mermaid", "dullahan": "dullahan_undead",
    "elf": "the elf family", "fennec": "fennec_kitsune", "golem": "golem_construct", "gorgon": "medusa_lamia", "griffin": "gryphon_dragon",
    "harpy": "the harpies", "inaba": "inaba_jackalope", "jack_o_lantern": "jack_o_lantern_devil", "jackalope": "jack_jackalope",
    "jiang_shi": "jiang_shi_devil", "kangaroo": "kangaroo_marsupial", "kappa": "kappa_lizardfolk", "kelpie": "kelpie_centaur",
    "kitsune": "the kitsune family", "kobold": "kobold_canid", "kraken": "kraken_mollusk", "la_llorona": "llorona_undead", "lamia": "the lamia family",
    "leshy": "leshy_flora", "lich": "lich_undead", "lizardman": "the lizardfolk", "long": "long_dragon", "medusa": "medusa_lamia",
    "mermaid": "the merfolk", "mimic": "mimic_slime", "minotaur": "the minotaurs", "mummy": "mummy_undead", "naga": "naga_lamia",
    "ocelot": "ocelot_nekomata", "papillon": "papillon_bug", "pegasus": "pegasus_centaur", "penguin": "penguin_harpy", "phoenix": "phoenix_harpy",
    "polar_bear": "polar_bear_canid", "ratatoskr": "ratatoskr_rodent", "salamander": "salamander_lizardfolk", "sea_turtle": "turtle_lizardfolk",
    "spider_crab": "crab_crustacean (decorator)", "succubus": "succubus_devil", "sucubus": "succubus_devil", "tapir": "tapir_centaur",
    "tengu": "tengu_harpy", "toucan": "toucan_harpy", "turtle": "turtle_lizardfolk", "unicorn": "unicorn_centaur", "uraeus": "cobra_lamia",
    "vampire": "vampire_undead", "vermillion_bird": "zhuque_harpy", "vodyanoy": "vodyanoy", "wadjet": "wadjet_lamia",
    "white_tiger": "tiger_nekomata (white)", "will_o_wisp": "will_o_wisp_undead", "wyvern": "wyvern_dragon", "yamato_no_orochi": "orochi_dragon",
    "yuki_onna": "yuki_onna_undead", "zombie": "zombie_undead",
    # tenth wave (2026-10-25)
    "tusktaur": "tusktaur_centaur", "aurorantler": "aurorantler_centaur", "rhiwool": "rhiwool_minotaur", "meloncollie": "meloncollie_canid",
    "needlehog": "needlehog_rodent", "clockhare": "clockhare_jackalope", "axolume": "axolume_mermaid", "aegis_boar": "aegis_boar_armoured",
    "blaze_cat": "blaze_cat_nekomata", "gargoyle": "gargoyle_construct", "coin_magpie": "coin_magpie_harpy", "trickster_raven": "trickster_raven_harpy",
    # eleventh wave (2026-10-26)
    "anvil_tortoise": "anvil_tortoise_lizardfolk", "mailtoad": "mailtoad", "gliderune": "gliderune_marsupial", "slothren": "slothren_armoured",
    "sloth": "slothren_armoured", "coatimano": "coatimano_rodent", "waterlil": "waterlil_nekomata", "rookmourn": "rookmourn_harpy",
    "cassowrath": "cassowrath_harpy", "talosling": "talosling_construct", "mantarrow": "mantarrow_mermaid", "sphinx": "sphinx_centaur",
    "brownie": "brownie_elf",
    # twelfth wave (2026-10-26)
    "cheshire": "cheshire_nekomata", "kesaran_pasaran": "kesaran_pasaran_flora", "tsuchinoko": "tsuchinoko_lamia", "platypus": "platypus_mermaid",
    "moonseal": "moonseal_mermaid", "marmotaur": "marmotaur_rodent", "bambadger": "bambadger_rodent", "dodobold": "dodobold_harpy",
    "boghare": "boghare_jackalope", "gremlin": "gremlin_devil", "karakasa_obake": "karakasa_obake_devil", "alebrije": "alebrije_dragon",
    # thirteenth wave (2026-10-26)
    "rust_otter": "rust_otter_mermaid", "tin_squirrel": "tin_squirrel_construct", "peaflare": "peaflare_harpy", "wattlefox": "wattlefox_kitsune",
    "scarabronze": "scarabronze_bug", "datebat": "date_bat", "snowloper": "snowloper_nekomata", "jaglare": "jaglare_nekomata",
    "squirricerat": "squirricerat_rodent", "kamaitachi": "kamaitachi_rodent", "momoshika": "momoshika_centaur", "japanese_lantern": "chochin_obake_devil",
    # fourteenth wave (2026-10-27)
    "gribboar": "gribboar_armoured", "silkcoon": "silkcoon_rodent", "bronzejaw_ox": "bronzejaw_minotaur", "cocoskink": "cocoskink_lizardfolk",
    "prairhorn": "prairhorn_centaur", "halberelk": "halberelk_centaur", "vulphea": "vulphea_kitsune", "cait_sith": "cait_sith_nekomata",
    "cu_sith": "cu_sith_canid", "yeti": "yeti_canid", "undine": "undine_slime", "ittan_momen": "ittan_momen_devil",
    # fifteenth wave (2026-10-27)
    "crocora": "crocora_lizardfolk", "iron_kobold": "iron_kobold_lizardfolk", "mooskuh": "mooskuh_minotaur", "bellmare": "bellmare_centaur",
    "vicunero": "vicunero_centaur", "rockhyrax": "rockhyrax_rodent", "riverdillo": "riverdillo_armoured", "reeffin": "reeffin_mermaid",
    "caraviper": "caraviper_lamia", "bakeneko": "bakeneko_nekomata", "rokurokubi": "rokurokubi_devil", "huli_jing": "huli_jing_kitsune",
    # sixteenth wave (2026-10-27)
    "jorogumo": "jorogumo_arachne", "dunefennec": "dunefennec_kitsune", "floodcrab": "floodcrab_crustacean", "temple_mantis": "temple_mantis_bug",
    "sword_koi": "sword_koi_mermaid", "squidkin": "squidkin_mollusk", "reedbull": "reedbull_minotaur", "kikimora": "kikimora_devil",
    "cockatrice": "cockatrice_dragon", "valkyrie": "valkyrie_elf", "pumpkingull": "pumpkingull_harpy", "condorune": "condorune_harpy",
}
# not a creature to make, and the ones set aside on purpose
EXCLUDED = {
    "human": "not a creature", "mary": "not a creature", "nsfw concepts": "not for the mod", "shibari": "not for the mod",
    "shopclerk": "not a creature", "shopklerk": "not a creature", "smock": "not a creature",
    "kachina": "declined: sacred to living Pueblo religion", "rainbow_serpent": "declined: sacred to living Aboriginal traditions",
    "taniwha": "set aside: living Māori tradition", "nightmarcher": "set aside: living Hawaiian tradition",
    "bunyip": "set aside: living Aboriginal tradition", "tiddalik": "set aside: living Aboriginal tradition",
    "thunderbird": "declined: sacred to living North American nations' traditions",
    "amaru": "set aside: living Andean tradition", "apu": "set aside: living Andean tradition", "supay": "set aside: living Andean tradition",
    "wayob": "set aside: living Maya tradition", "pukwudgie": "set aside: living Wampanoag tradition",
    "mimi_spirit": "set aside: living Aboriginal tradition", "nargun": "set aside: living Aboriginal tradition",
    "yara_ma_yha_who": "set aside: living Aboriginal tradition", "menehune": "set aside: living Hawaiian tradition",
    "akhlut": "set aside: living Inuit tradition", "ijiraq": "set aside: living Inuit tradition", "keelut": "set aside: living Inuit tradition",
    "peacepipe_bison": "set aside: a sacred pipe of living Plains traditions",
    "alux": "set aside: living Maya tradition", "curupira": "set aside: living Indigenous Brazilian tradition",
    "mapinguari": "set aside: living Indigenous Brazilian tradition", "ypupiara": "set aside: living Indigenous Brazilian tradition",
    "horned_serpent_uktena": "set aside: living Cherokee tradition", "terniq": "set aside: an Inuit name",
}


def creature(filename):
    """the creature a reference file shows: the name before _f_ / _m_, a few stray spellings folded in"""
    base = os.path.splitext(filename)[0].lower().replace(".webp", "")
    m = re.match(r"(.+?)_(f|m)_", base)
    name = m.group(1) if m else base
    if name.startswith("arachne_long"):
        return "arachne"
    return re.sub(r"_idle$", "", name)


def main():
    if not os.path.isdir(ART):
        sys.exit(f"no reference art at {ART}")
    names = sorted({creature(f) for f in os.listdir(ART)})
    done = [n for n in names if n in IN_GAME]
    out = [n for n in names if n in EXCLUDED]
    left = [n for n in names if n not in IN_GAME and n not in EXCLUDED]
    lines = ["# Harmontide roster", "",
             "Every creature in the Harmontide reference art and whether it is in the game. Generated by `tools/harmontide_roster.py`",
             "(update its `IN_GAME` table after each wave and re-run).", "",
             f"**{len(done)} in the game, {len(left)} left** ({len(out)} excluded) of {len(names)}.", "",
             f"## Left ({len(left)})", ""]
    by_letter = {}
    for n in left:
        by_letter.setdefault(n[0].upper(), []).append(n.replace("_", " "))
    for k in sorted(by_letter):
        lines.append(f"- **{k}:** " + ", ".join(by_letter[k]))
    lines += ["", f"## In the game ({len(done)})", "", "| Reference | In the game as |", "|---|---|"]
    lines += [f"| {n.replace('_', ' ')} | {IN_GAME[n]} |" for n in done]
    lines += ["", f"## Excluded ({len(out)})", "", "| Reference | Why |", "|---|---|"]
    lines += [f"| {n} | {EXCLUDED[n]} |" for n in out]
    with open(OUT, "w", encoding="utf-8", newline="\n") as fh:
        fh.write("\n".join(lines) + "\n")
    print(f"{len(done)} in the game, {len(left)} left, {len(out)} excluded -> {OUT}")


if __name__ == "__main__":
    main()
