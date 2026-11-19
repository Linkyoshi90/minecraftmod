#!/usr/bin/env python3
"""Turns harvested CC0 clips into mod sounds and points the sound events of every species mapped below at them.

    uv run --with soundfile --with numpy --with scipy python tools/sound_import.py <harvest_dir>

Clips (from tools/freesound_harvest.py) are cut to the loud part, made mono 44.1 kHz, peak-levelled,
faded out and written as Ogg Vorbis to assets/harmontide/sounds/shared/<group>/<n>.ogg. One shared pool
per voice group; species get their own voice through pitch. Then every mapped species' ambient / hurt / death /
eat event is rewritten (events that do not exist are left alone, and so are the species' special sounds
such as sing or hiss). SOUND_CREDITS.md lists every clip's source. Re-run is safe: it rebuilds from scratch.
"""
import html
import json
import os
import re
import sys

import numpy as np
import soundfile as sf
from scipy.signal import resample_poly

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, "src", "main", "resources", "assets", "harmontide")
SOUNDS_JSON = os.path.join(ASSETS, "sounds.json")
SHARED = os.path.join(ASSETS, "sounds", "shared")
CREDITS = os.path.join(REPO, "SOUND_CREDITS.md")
RATE = 44100

# group -> (max seconds kept, peak level)
SHAPE = {"girl_hurt": (1.0, 0.8), "girl_death": (2.2, 0.8), "girl_giggle": (2.5, 0.7), "eat": (0.9, 0.6)}
DEFAULT_SHAPE = (2.5, 0.75)

# harpies: species -> (call group, call pitch, girl voice pitch). Big birds lower, small birds higher.
HARPIES = {
    "sparrow_harpy": ("songbird", 1.15, 1.15), "finch_harpy": ("songbird", 1.25, 1.2),
    "alkonost_harpy": ("songbird", 0.9, 1.0), "motmora_harpy": ("songbird", 0.8, 1.0),
    "riflebird_harpy": ("songbird", 1.0, 1.05), "hummingbird_harpy": ("songbird", 1.45, 1.25),
    "crow_harpy": ("corvid", 1.0, 1.0), "skycrow_harpy": ("corvid", 1.1, 1.05),
    "rookmourn_harpy": ("corvid", 0.85, 0.9), "trickster_raven_harpy": ("corvid", 0.9, 1.05),
    "yatagarasu_harpy": ("corvid", 0.8, 0.95), "coin_magpie_harpy": ("corvid", 1.2, 1.1),
    "owl_harpy": ("owl", 1.0, 0.95), "lechuza_harpy": ("owl", 1.1, 1.05),
    "stigowl_harpy": ("owl", 0.75, 0.85), "argentowlis_harpy": ("owl", 0.9, 1.0),
    "roc_harpy": ("raptor", 0.7, 0.85), "garuda_harpy": ("raptor", 0.85, 0.95),
    "condorune_harpy": ("raptor", 0.75, 0.9), "ironbeak_harpy": ("raptor", 0.9, 0.95),
    "ziz_harpy": ("raptor", 0.65, 0.85), "landharpy_harpy": ("raptor", 1.0, 1.0),
    "phoenix_harpy": ("raptor", 1.1, 1.0), "bennu_harpy": ("raptor", 0.95, 1.0),
    "zhuque_harpy": ("raptor", 1.15, 1.05), "emberu_harpy": ("poultry", 1.1, 1.1),
    "seagull_harpy": ("seabird", 1.0, 1.05), "albatross_harpy": ("seabird", 0.75, 0.9),
    "pelican_harpy": ("seabird", 0.7, 0.9), "pumpkingull_harpy": ("seabird", 0.9, 1.0),
    "penguin_harpy": ("waterfowl", 0.75, 1.1),
    "duck_harpy": ("waterfowl", 1.1, 1.1), "swan_harpy": ("waterfowl", 0.8, 0.95),
    "mirror_crane_harpy": ("crane", 1.0, 1.0), "flamingo_harpy": ("crane", 1.1, 1.05),
    "ibiset_harpy": ("crane", 1.15, 1.1), "ibisking_harpy": ("crane", 0.9, 0.95),
    "macaw_harpy": ("parrot", 1.0, 1.05), "toucan_harpy": ("parrot", 0.9, 1.05),
    "peaflare_harpy": ("peacock", 1.0, 1.0),
    "poultry_harpy": ("poultry", 1.0, 1.05), "dodobold_harpy": ("poultry", 0.8, 0.95),
    "reliquary_dove_harpy": ("dove", 1.0, 1.0),
    "ostrich_harpy": ("ratite", 1.0, 0.95), "emu_harpy": ("ratite", 1.05, 1.0),
}
# amphibians: species -> (call group, call pitch, girl voice pitch, bellow group, bellow pitch).
# Small bright frogs high, big toads and bullfrogs low; the tongue flick follows the call pitch. CC0 bullfrog and toad
# recordings are scarce, so those pools borrow the plain croaks at a lower pitch.
ANURANS = {
    "bullywug": ("frog_croak", 0.75, 0.95, "bullfrog+frog_croak", 0.8),
    "bullfrog": ("bullfrog+frog_croak", 1.0, 0.95, "bullfrog+frog_croak", 0.8),
    "tree_frog": ("frog_chirp", 1.0, 1.1, "frog_croak", 1.0),
    "poison_dart_frog": ("frog_chirp", 1.2, 1.2, "frog_chirp", 0.9),
    "glass_frog": ("frog_chirp", 1.35, 1.2, "frog_chirp", 1.0),
    "goliath_frog": ("bullfrog+frog_croak", 0.7, 0.85, "bullfrog+frog_croak", 0.55),
    "desert_rain_frog": ("frog_chirp", 1.5, 1.3, "frog_chirp", 1.2),
    "mucus_toad": ("toad+frog_croak", 0.85, 0.95, "toad+frog_croak", 0.65),
    "fire_bellied_toad": ("toad+frog_croak", 1.1, 1.05, "toad+frog_croak", 0.9),
    "cane_toad": ("toad+frog_croak", 0.8, 0.95, "bullfrog+frog_croak", 0.75),
    "natterjack": ("toad+frog_croak", 1.0, 1.0, "toad+frog_croak", 0.8),
    "surinam_toad": ("frog_croak", 0.8, 0.95, "toad+frog_croak", 0.7),
    "vodyanoy": ("bullfrog+frog_croak", 0.85, 0.9, "bullfrog+frog_croak", 0.65),
}
# the salamander side: (ambient group or None, ambient pitch, girl voice pitch)
CAUDATA = {
    "axolotl_mermaid": ("bubbles", 1.2, 1.15), "axolume_mermaid": ("bubbles", 1.0, 1.05),
    "olm_mermaid": ("bubbles", 0.85, 1.0),
    "salamander_lizardfolk": (None, 1.0, 1.0), "newt_lizardfolk": (None, 1.0, 1.1),
}
# mammal girls: species -> (call group, call pitch, girl voice pitch); "a+b" mixes two pools.
MAMMALS = {
    # cats (Feliformia)
    "serval_nekomata": ("cat_meow", 1.0, 1.05), "tiger_nekomata": ("big_cat", 1.0, 0.95),
    "lioness_nekomata": ("big_cat", 1.05, 0.95), "snow_leopard_nekomata": ("big_cat", 1.15, 1.0),
    "cheetah_nekomata": ("cat_meow", 0.85, 1.0), "lynx_nekomata": ("cat_meow", 0.8, 1.0),
    "ocelot_nekomata": ("cat_meow", 1.1, 1.05), "blaze_cat_nekomata": ("cat_meow", 0.95, 1.05),
    "waterlil_nekomata": ("cat_meow", 1.1, 1.05), "cheshire_nekomata": ("cat_purr+cat_meow", 1.0, 1.05),
    "snowloper_nekomata": ("big_cat", 1.1, 1.0), "jaglare_nekomata": ("big_cat", 0.95, 0.95),
    "cait_sith_nekomata": ("cat_meow", 0.9, 1.0), "bakeneko_nekomata": ("cat_meow", 0.85, 1.0),
    "censer_lion_nekomata": ("big_cat", 1.0, 0.95), "bastet_nekomata": ("cat_purr+cat_meow", 0.95, 1.0),
    "sabre_nekomata": ("big_cat", 0.85, 0.9), "manticore_nekomata": ("big_cat", 0.75, 0.9),
    "caracal_nekomata": ("cat_meow", 0.95, 1.05), "rakshasa_nekomata": ("big_cat", 0.9, 0.95),
    "ammit_nekomata": ("big_cat", 0.8, 0.9), "smilodon_nekomata": ("big_cat", 0.8, 0.9),
    "black_panther_nekomata": ("big_cat", 0.95, 0.95), "nue_nekomata": ("big_cat", 0.85, 0.95),
    "sphinx_centaur": ("big_cat", 0.85, 0.9), "lamassu_centaur": ("big_cat", 0.8, 0.9),
    "anzu_centaur": ("raptor+big_cat", 0.8, 0.9), "ark_seraphim_centaur": ("big_cat", 1.0, 0.95),
    "hyena_canid": ("hyena", 1.0, 1.0), "mongoose_rodent": ("small_critter", 1.1, 1.1),
    "meerkat_rodent": ("small_critter", 1.2, 1.15), "pangolin_armoured": ("small_critter", 0.8, 1.05),
    # dogs and foxes (Canidae)
    "red_kitsune": ("fox", 1.0, 1.05), "arctic_kitsune": ("fox", 1.1, 1.1), "fennec_kitsune": ("fox", 1.3, 1.15),
    "corsac_kitsune": ("fox", 1.15, 1.1), "gray_kitsune": ("fox", 0.95, 1.05), "wattlefox_kitsune": ("fox", 1.05, 1.05),
    "vulphea_kitsune": ("fox", 1.0, 1.05), "huli_jing_kitsune": ("fox", 0.95, 1.0),
    "dunefennec_kitsune": ("fox", 1.25, 1.1), "obaa_kitsune": ("fox", 0.85, 0.95), "kumiho_kitsune": ("fox", 0.9, 1.0),
    "bat_eared_kitsune": ("fox", 1.2, 1.1), "doggo_mermaid": ("dog_bark", 1.1, 1.1), "wolf_canid": ("wolf_howl", 1.0, 1.0),
    "anubis_canid": ("dog_bark", 0.85, 0.95), "kobold_canid": ("dog_bark", 1.3, 1.15),
    "meloncollie_canid": ("dog_bark", 1.0, 1.05), "cu_sith_canid": ("dog_bark", 0.8, 0.95),
    "amarok_canid": ("wolf_howl", 0.8, 0.9), "tarnhelm_canid": ("wolf_howl", 0.95, 1.0),
    "obsidian_hound_canid": ("dog_bark", 0.75, 0.9), "hellhound_canid": ("dog_bark", 0.7, 0.9),
    "cerberus_canid": ("dog_bark", 0.65, 0.85), "bloodhound_canid": ("dog_bark", 0.85, 0.95),
    "raiju_canid": ("fox", 1.2, 1.1), "tanuki_rodent": ("fox", 0.9, 1.05),
    # horses, tapirs, rhinos (Perissodactyla)
    "baku_nekomata": ("pig", 0.8, 1.0), "horse_centaur": ("horse", 1.0, 1.0), "zebra_centaur": ("horse", 1.1, 1.0),
    "tapir_centaur": ("pig", 0.85, 1.0), "kelpie_centaur": ("horse", 0.9, 1.0), "pegasus_centaur": ("horse", 1.1, 1.05),
    "unicorn_centaur": ("horse", 1.15, 1.05), "bellmare_centaur": ("horse", 0.95, 1.0),
    "donkey_centaur": ("donkey", 1.0, 1.0), "rhiwool_minotaur": ("pig", 0.7, 0.9), "rhino_minotaur": ("pig", 0.65, 0.9),
    # cattle, goats, sheep (Bovidae)
    "goat_centaur": ("goat", 1.0, 1.05), "cattle_minotaur": ("cow", 1.0, 1.0), "minoan_minotaur": ("cow", 0.9, 0.95),
    "bison_minotaur": ("cow", 0.75, 0.9), "aurochs_minotaur": ("cow", 0.7, 0.9), "water_buffalo_minotaur": ("cow", 0.8, 0.9),
    "sheep_minotaur": ("sheep", 1.0, 1.05), "highland_minotaur": ("cow", 0.9, 0.95), "yak_minotaur": ("cow", 0.8, 0.9),
    "bronzejaw_minotaur": ("cow", 0.85, 0.95), "mooskuh_minotaur": ("cow", 1.05, 1.0), "reedbull_minotaur": ("cow", 0.95, 0.95),
    "dustbison_minotaur": ("cow", 0.75, 0.9), "skjoldram_minotaur": ("sheep", 0.8, 0.95),
    "brasshorn_minotaur": ("goat", 0.85, 0.95), "oryx_minotaur": ("goat", 0.9, 1.0),
    "shroomotaur_minotaur": ("cow", 1.1, 1.0), "holstaur_minotaur": ("cow", 1.0, 1.0),
    # deer (Cervidae), giraffes and okapi with them; few CC0 deer calls, so goat bleats fill in
    "reindeer_centaur": ("deer+goat", 1.0, 1.0), "aurorantler_centaur": ("deer+goat", 1.1, 1.05),
    "momoshika_centaur": ("deer+goat", 1.15, 1.05), "prairhorn_centaur": ("deer+goat", 1.05, 1.0),
    "halberelk_centaur": ("deer+goat", 0.85, 0.95), "stagel_centaur": ("deer+goat", 0.95, 1.0), "palmyr_centaur": ("deer+goat", 1.0, 1.0),
    "qilin_centaur": ("deer+goat", 1.1, 1.0), "caribou_centaur": ("deer+goat", 0.95, 1.0), "giraffe_centaur": ("deer+goat", 0.8, 0.95),
    "okapi_centaur": ("deer+goat", 0.9, 1.0), "moose_centaur": ("deer+goat", 0.75, 0.9),
    # camels and llamas (Camelidae)
    "alpaca_centaur": ("camelid", 1.1, 1.05), "camel_centaur": ("camelid", 0.9, 0.95),
    "dromedary_centaur": ("camelid", 0.85, 0.95), "vicunero_centaur": ("camelid", 1.15, 1.05),
    "ceibler_centaur": ("camelid", 1.0, 1.0), "urcuchillay_centaur": ("camelid", 1.05, 1.0),
    # pigs (Suina)
    "zoglintaur_centaur": ("pig", 0.8, 0.95), "aegis_boar_armoured": ("pig", 0.85, 0.95),
    "gribboar_armoured": ("pig", 0.9, 1.0), "daeodon_armoured": ("pig", 0.7, 0.9), "piglette_armoured": ("pig", 1.2, 1.1),
    # bears, seals, otters, raccoons and kin (Arctoidea)
    "seal_mermaid": ("seal", 1.0, 1.05), "ahuizotl_mermaid": ("small_critter", 1.0, 1.05),
    "moonseal_mermaid": ("seal", 1.1, 1.05), "rust_otter_mermaid": ("small_critter", 1.0, 1.05),
    "walrus_mermaid": ("seal", 0.75, 0.9), "selkie_mermaid": ("seal", 1.05, 1.05),
    "polar_bear_canid": ("bear", 0.95, 0.95), "panda_canid": ("bear", 1.1, 1.05), "grizzly_canid": ("bear", 0.85, 0.9),
    "black_bear_canid": ("bear", 1.0, 1.0), "rheintalensis_canid": ("bear", 0.9, 0.95), "wolverine_canid": ("bear", 1.25, 1.0),
    "coatimano_rodent": ("small_critter", 1.0, 1.05), "bambadger_rodent": ("small_critter", 0.85, 1.0),
    "kamaitachi_rodent": ("small_critter", 1.15, 1.1), "silkcoon_rodent": ("small_critter", 1.05, 1.05),
    "sobolnik_rodent": ("small_critter", 1.1, 1.05), "skunk_rodent": ("small_critter", 1.1, 1.1),
    "raccoon_rodent": ("small_critter", 1.0, 1.05), "red_panda_rodent": ("small_critter", 1.2, 1.1),
}
# small mammals, sea cows, whales and reptiles, same shape as MAMMALS. A call of None keeps her own idle sound (the
# vanilla turtle and armadillo idles suit them better than anything CC0); her voice and eating are still replaced.
SMALL = {
    # rodents (Rodentia)
    "capybara_rodent": ("rodent", 0.7, 1.0), "ratatoskr_rodent": ("small_critter", 1.15, 1.1),
    "marmotaur_rodent": ("rodent", 0.85, 1.05), "squirricerat_rodent": ("small_critter", 1.1, 1.1),
    "pineskrell_rodent": ("small_critter", 1.2, 1.1), "house_mouse_rodent": ("rodent", 1.2, 1.15),
    "beaver_rodent": ("rodent", 0.75, 1.0),
    # hares and rabbits (Lagomorpha): CC0 rabbits are few and quiet, so mouse squeaks fill in a little lower
    "inaba_jackalope": ("rabbit+rodent", 1.0, 1.1), "jack_jackalope": ("rabbit+rodent", 0.9, 1.05),
    "easter_jackalope": ("rabbit+rodent", 1.1, 1.15), "al_miraj_jackalope": ("rabbit+rodent", 0.95, 1.05),
    "volcano_jackalope": ("rabbit+rodent", 0.85, 1.0), "wolpertinger_jackalope": ("rabbit+rodent", 0.9, 1.05),
    "clockhare_jackalope": ("rabbit+rodent", 1.0, 1.05), "boghare_jackalope": ("rabbit+rodent", 0.9, 1.05),
    "moon_rabbit_jackalope": ("rabbit+rodent", 1.15, 1.15),
    # primates: apes low, small monkeys and lemurs high
    "yeti_canid": ("primate", 0.7, 0.9), "gigantopithecus_canid": ("primate", 0.65, 0.9),
    "sasquatch_rodent": ("primate", 0.75, 0.9), "gibbon_rodent": ("primate", 1.1, 1.05),
    "sun_wukong_rodent": ("primate", 1.0, 1.05), "lemur_rodent": ("primate", 1.2, 1.1),
    # bats (Chiroptera), with the hedgehog and mole that live with them
    # no usable CC0 bat calls (search finds baseball bats and protest chants), so the vanilla bat idle stays
    "cave_bat": (None, 1.0, 1.1), "date_bat": (None, 1.05, 1.1), "frond_bat": (None, 1.1, 1.1),
    "camazotz_bat": (None, 0.75, 0.95), "vetala_bat": (None, 0.85, 1.0),
    "needlehog_rodent": ("small_critter", 1.25, 1.1), "mole_rodent": ("rodent", 0.9, 1.05),
    # marsupials and the platypus
    # no CC0 marsupial calls either: kangaroos chitter low, koalas get a high bear grunt (their bellow is close),
    # opossums hiss as real ones do
    "kangaroo_marsupial": ("small_critter", 0.8, 1.0), "koala_marsupial": ("bear", 1.3, 1.05),
    "gliderune_marsupial": ("small_critter", 1.3, 1.1), "opossum_marsupial": ("snake_hiss", 1.2, 1.05),
    "platypus_mermaid": ("small_critter", 0.8, 1.05),
    # armadillos, sloths, anteaters (Xenarthra): the vanilla armadillo idle stays
    "armadillo_armoured": (None, 1.0, 1.05), "slothren_armoured": (None, 1.0, 1.0),
    "riverdillo_armoured": (None, 1.0, 1.05), "anteater_armoured": (None, 1.0, 1.0),
    # elephants, sea cows, hyraxes (Paenungulata); sea cows squeak, so they borrow dolphin whistles high
    "tusktaur_centaur": ("elephant", 0.85, 0.9), "elephant_centaur": ("elephant", 1.0, 0.95),
    "tuskin_minotaur": ("elephant", 0.9, 0.95), "dugong_mermaid": ("cetacean", 1.2, 1.05),
    "manatee_mermaid": ("cetacean", 1.15, 1.0), "moopie_mermaid": ("cetacean+cow", 1.1, 1.05),
    "rockhyrax_rodent": ("rodent", 1.0, 1.1),
    # whales and hippos (Whippomorpha)
    "orca_mermaid": ("cetacean", 0.8, 0.95), "dolphin_mermaid": ("cetacean", 1.0, 1.05),
    "cetus_mermaid": ("cetacean", 0.6, 0.9), "beluga_mermaid": ("cetacean", 1.1, 1.05),
    "narwhal_mermaid": ("cetacean", 0.9, 1.0), "hippo_minotaur": ("hippo+pig", 0.7, 0.9),
    # crocodiles, dinosaurs, pterosaurs (Archosauria); dinosaurs get croc rumbles and ratite booms, their real kin
    "croc_lizardfolk": ("croc", 1.0, 0.95), "crocora_lizardfolk": ("croc", 1.05, 1.0),
    "cipactli_lizardfolk": ("croc", 0.8, 0.9), "argentinosaurus_lizardfolk": ("croc+ratite", 0.6, 0.85),
    "moo_dragon": ("cow+croc", 0.7, 0.9), "hairypterix_dragon": ("seabird+raptor", 0.75, 1.0),
    "fernstalker_theropod": ("croc+ratite", 0.85, 0.95), "spinosaur_theropod": ("croc+ratite", 0.7, 0.9),
    "australovenator_theropod": ("croc+ratite", 0.95, 1.0),
    # turtles and tortoises (Testudines): quiet animals, their own idle stays
    **{sp: (None, 1.0, vp) for sp, vp in {
        "turtle_lizardfolk": 1.0, "snapping_turtle_lizardfolk": 0.95, "kappa_lizardfolk": 1.05,
        "black_tortoise_lizardfolk": 0.9, "red_eared_slider_lizardfolk": 1.05, "galapagos_tortoise_lizardfolk": 0.9,
        "box_turtle_lizardfolk": 1.05, "softshell_turtle_lizardfolk": 1.0, "matamata_lizardfolk": 0.95,
        "anvil_tortoise_lizardfolk": 0.9, "conch_turtle_lizardfolk": 1.0, "turtfolk_lizardfolk": 1.05,
        "turtworld_lizardfolk": 0.85, "archelon_lizardfolk": 0.85}.items()},
    # snakes and lizards (Squamata): lamias hiss, big snakes lower
    "python_lamia": ("snake_hiss", 0.85, 1.0), "cobra_lamia": ("snake_hiss", 1.0, 1.0),
    "sea_snake_lamia": ("snake_hiss", 1.1, 1.05), "rattlesnake_lamia": ("rattle+snake_hiss", 1.0, 1.0),
    "boa_lamia": ("snake_hiss", 0.85, 1.0), "naga_lamia": ("snake_hiss", 0.9, 0.95),
    "wadjet_lamia": ("snake_hiss", 1.0, 1.0), "medusa_lamia": ("snake_hiss", 0.95, 1.0),
    "tsuchinoko_lamia": ("snake_hiss", 1.25, 1.15), "caraviper_lamia": ("snake_hiss", 1.05, 1.0),
    "urchskin_lamia": ("snake_hiss", 1.1, 1.05), "turquoise_serpent_lamia": ("snake_hiss", 1.0, 1.05),
    "hoop_snake_lamia": ("snake_hiss", 1.15, 1.1), "serpopard_lamia": ("snake_hiss+big_cat", 0.95, 1.0),
    "xiangliu_lamia": ("snake_hiss", 0.75, 0.9), "titanoboa_lamia": ("snake_hiss", 0.65, 0.9),
    "anaconda_lamia": ("snake_hiss", 0.75, 0.95), "sea_serpent_lamia": ("snake_hiss", 0.7, 0.9),
    "echidna_lamia": ("snake_hiss", 0.85, 0.95), "jormungandr_dragon": ("snake_hiss", 0.55, 0.85),
    "mosasaurus_mermaid": ("croc+cetacean", 0.65, 0.9),
    **{sp: (None, 1.0, vp) for sp, vp in {
        "chameleon_lizardfolk": 1.05, "iguana_lizardfolk": 1.0, "gecko_lizardfolk": 1.1, "chupacabra_lizardfolk": 0.95,
        "cocoskink_lizardfolk": 1.05, "iron_kobold_lizardfolk": 1.1, "basilisk_lizardfolk": 1.0,
        "cave_gekko_lizardfolk": 1.1, "komodo_lizardfolk": 0.9}.items()},
}
# fae, undead and devils: humanoid girls, so their idles are voices (sighs and hums, ghostly wails, cackles) or the
# thing they are made of (bones, armour, a blade, pages, cloth, fire). Same shape as MAMMALS; None keeps her own idle.
MYTHIC = {
    # fae and folk: elves have no idle; the fairies keep their amethyst chime
    **{sp: (None, 1.0, vp) for sp, vp in {
        "night_elf": 1.0, "high_elf": 1.0, "wood_elf": 1.05, "blood_elf": 1.0, "desert_elf": 1.0, "dark_elf": 0.95,
        "brownie_elf": 1.2, "valkyrie_elf": 0.95, "domovoi_elf": 1.1, "mad_hatter_elf": 1.05, "angel_elf": 1.05,
        "gandharva_elf": 1.0, "apsara_elf": 1.05, "knocker_elf": 1.15, "kaguya_elf": 1.05, "ophanim_elf": 0.95,
        "norn_elf": 0.95, "snow_elf": 1.05, "gnome_elf": 1.2, "dwarf_elf": 0.9, "nymph_elf": 1.1, "sylph_elf": 1.15,
        "doppelganger_elf": 1.0, "fairy_bug": 1.3, "fairy_queen_bug": 1.15, "pixi_bug": 1.35, "rlff_bug": 1.3}.items()},
    # undead
    "living_doll_undead": (None, 1.0, 1.15), "will_o_wisp_undead": (None, 1.0, 1.2), "phantom_undead": (None, 1.0, 0.95),
    "zombie_undead": (None, 1.0, 0.9), "mummy_undead": (None, 1.0, 0.9),
    "yuki_onna_undead": ("ghost", 1.1, 1.05), "banshee_undead": ("ghost", 1.0, 1.0), "llorona_undead": ("ghost", 0.95, 1.0),
    "yurei_undead": ("ghost", 1.05, 1.05), "kasurenijimi_undead": ("ghost", 1.0, 1.0), "lich_undead": ("ghost", 0.75, 0.9),
    "draugr_undead": ("ghost", 0.7, 0.9), "ghoul_undead": ("ghost", 0.8, 0.9), "nasnas_undead": ("ghost", 0.85, 0.95),
    "mano_peluda_undead": ("ghost", 0.75, 0.95), "lost_soul_undead": ("ghost", 1.2, 1.1),
    "soul_cluster_undead": ("ghost", 0.9, 1.0), "soul_leech_undead": ("ghost", 1.1, 1.05),
    "drowned_undead": ("bubbles+ghost", 0.85, 0.95), "poltergeist_undead": ("ghost", 1.15, 1.1),
    "ame_onna_undead": ("ghost", 1.0, 1.0), "repeadead_undead": ("ghost", 0.8, 0.9),
    "vampire_undead": ("girl_cackle+girl_giggle", 1.0, 1.0), "frankenstein_undead": ("girl_sigh", 0.8, 0.9),
    "jiangshi_undead": ("girl_sigh", 0.85, 0.95), "gaitress_undead": ("girl_sigh", 1.0, 1.0),
    "cordyceps_undead": ("girl_sigh", 0.8, 0.95), "sandman_undead": ("girl_sigh", 0.9, 1.0),
    "famished_undead": ("eat", 0.8, 0.95), "candle_spirit_undead": ("fire", 1.2, 1.1),
    "skeleton_undead": ("bones", 1.0, 1.0), "gashadokuro_undead": ("bones", 0.6, 0.85), "hand_undead": ("bones", 1.2, 1.05),
    "koschei_undead": ("bones", 0.8, 0.9), "dullahan_undead": ("armor+blade", 0.9, 0.95),
    "living_armor_undead": ("armor+blade", 1.0, 1.0), "cursed_blade_undead": ("blade", 1.0, 1.0),
    # devils, oni and giants: the big ones laugh low
    "lesser_devil": ("girl_cackle+girl_giggle", 1.15, 1.1), "succubus_devil": ("girl_giggle", 0.95, 1.0),
    "greater_devil": ("girl_cackle+girl_giggle", 0.85, 0.95), "jack_o_lantern_devil": ("girl_cackle+girl_giggle", 1.1, 1.05),
    "jiang_shi_devil": ("girl_sigh", 0.85, 0.95), "oni_devil": ("girl_cackle+girl_giggle", 0.75, 0.9),
    "gremlin_devil": ("girl_giggle", 1.3, 1.2), "karakasa_obake_devil": ("girl_giggle", 1.15, 1.1),
    "chochin_obake_devil": ("fire+girl_giggle", 1.1, 1.1), "ittan_momen_devil": ("cloth", 1.0, 1.05),
    "rokurokubi_devil": ("girl_giggle", 1.0, 1.0), "kikimora_devil": ("girl_cackle+girl_giggle", 1.05, 1.05),
    "akaname_devil": ("tongue", 1.0, 1.05), "cyclops_devil": ("girl_cackle+girl_giggle", 0.7, 0.85),
    "genie_devil": ("girl_giggle", 1.0, 1.0), "ogre_devil": ("girl_cackle+girl_giggle", 0.7, 0.85), "taotie_devil": ("eat", 0.75, 0.9),
    "behemoth_devil": ("girl_cackle+girl_giggle", 0.6, 0.8), "fire_giant_devil": ("fire+girl_cackle+girl_giggle", 0.7, 0.85),
    "frost_giant_devil": ("girl_cackle+girl_giggle", 0.7, 0.85), "ifrit_devil": ("fire", 0.9, 0.95),
    "pazuzu_devil": ("girl_cackle+girl_giggle", 0.8, 0.9), "lilith_devil": ("girl_cackle+girl_giggle", 0.95, 1.0),
    "wrath_devil": ("girl_cackle+girl_giggle", 0.85, 0.95), "demon_devil": ("girl_cackle+girl_giggle", 0.9, 0.95),
    "fury_devil": ("girl_cackle+girl_giggle", 1.05, 1.0), "firesprite_devil": ("fire", 1.3, 1.15),
    "goblin_devil": ("girl_giggle", 1.2, 1.1), "troll_devil": ("girl_cackle+girl_giggle", 0.7, 0.85),
    "book_spirit_devil": ("pages", 1.0, 1.05),
}
# fish, spiders, insects, crabs and molluscs. Fish blow bubbles and splash; the singing sea women sing; crabs click
# their claws; spiders and scorpions chitter; soft-bodied molluscs squelch. Bees, hornets and mosquitoes keep the
# vanilla buzz. Same shape as MAMMALS; None keeps her own idle.
SEA = {
    # fish (Actinopterygii), with the starfish that lives with them
    "koi_mermaid": ("splash+bubbles", 1.1, 1.05), "shark_mermaid": ("bubbles+splash", 0.7, 0.95),
    "siren_mermaid": ("girl_sing", 1.0, 1.0), "lorelei_mermaid": ("girl_sing", 1.05, 1.05),
    "rusalka_mermaid": ("girl_sing", 0.9, 1.0), "mantarrow_mermaid": ("splash+bubbles", 0.85, 1.0),
    "reeffin_mermaid": ("bubbles+splash", 1.15, 1.1), "sword_koi_mermaid": ("splash+bubbles", 1.0, 1.0),
    "sawfin_mermaid": ("bubbles+splash", 0.85, 0.95), "piranha_mermaid": ("eat+bubbles", 1.2, 1.1),
    "fishman_mermaid": ("bubbles+splash", 0.9, 0.95), "leviathan_mermaid": ("bubbles+splash", 0.55, 0.85),
    "pufferfish_mermaid": ("bubbles", 1.3, 1.15), "angler_mermaid": ("bubbles", 0.75, 0.95),
    "seahorse_mermaid": ("bubbles", 1.35, 1.15), "eel_mermaid": ("bubbles+splash", 0.95, 1.0),
    "starfish_crustacean": ("bubbles", 1.2, 1.1),
    # spiders and scorpions (Chelicerata): big hunters low, little jumpers high
    "jumping_spider_arachne": ("chitter", 1.25, 1.15), "orb_weaver_arachne": ("chitter", 1.05, 1.05),
    "tarantula_arachne": ("chitter+snake_hiss", 0.8, 0.95), "trapdoor_spider_arachne": ("chitter", 0.9, 1.0),
    "wolf_spider_arachne": ("chitter", 0.95, 1.0), "jorogumo_arachne": ("chitter+girl_giggle", 0.9, 1.0),
    "spoodr_arachne": ("chitter", 1.15, 1.1), "ushi_oni_arachne": ("chitter", 0.7, 0.85),
    "black_widow_arachne": ("chitter", 1.0, 1.0), "peacock_spider_arachne": ("chitter", 1.3, 1.15),
    "cave_spider_arachne": ("chitter", 1.1, 1.05), "scorpion_bug": (None, 1.0, 1.0), "girtablilu_bug": (None, 1.0, 0.95),
    # insects (Hexapoda) and the centipede; most have no idle, the bees keep their buzz
    "centipede_lamia": ("chitter", 0.85, 1.0),
    **{sp: (None, 1.0, vp) for sp, vp in {
        "mosquito_bug": 1.15, "moth_bug": 1.05, "moth_larva_bug": 1.15, "bee_bug": 1.1, "queen_bee_bug": 1.0,
        "papillon_bug": 1.1, "papillon_larva_bug": 1.2, "hornet_bug": 1.0, "firefly_bug": 1.15, "mantis_bug": 1.0,
        "scarabronze_bug": 0.95, "temple_mantis_bug": 0.95, "date_beetle_bug": 1.0, "mothman_bug": 0.9, "ant_bug": 1.15,
        "queen_ant_bug": 0.95, "spider_wasp_bug": 1.05, "dragonfly_bug": 1.1, "ladybug_bug": 1.2, "cicada_bug": 1.1,
        "antlion_bug": 1.0, "tardigrade_bug": 1.25}.items()},
    # crabs, lobsters, shrimp (Malacostraca)
    "anomalocaris_mermaid": ("crab+chitter+bubbles", 0.8, 0.95), "crab_crustacean": ("crab+chitter", 1.0, 1.05),
    "hermit_crab_crustacean": ("crab+chitter", 1.2, 1.1), "floodcrab_crustacean": ("crab+chitter", 0.9, 1.0),
    "spider_crab_crustacean": ("crab+chitter", 0.8, 0.95), "lobster_crustacean": ("crab+chitter", 0.85, 1.0),
    "mantis_shrimp_crustacean": ("crab+chitter", 1.15, 1.1),
    # octopuses, squid, snails (Mollusca), with the sandworm
    "sandworm_lamia": ("tongue", 0.6, 0.9), "kraken_mollusk": ("bubbles+tongue", 0.7, 0.9),
    "ika_mollusk": ("bubbles+tongue", 1.1, 1.05), "octopus_mollusk": ("bubbles+tongue", 1.0, 1.0),
    "snail_mollusk": ("tongue", 1.1, 1.1), "ammonite_mollusk": ("bubbles", 0.9, 1.0),
    "jellyfish_mollusk": ("bubbles", 1.3, 1.15), "squidkin_mollusk": ("bubbles+tongue", 1.05, 1.05),
    "spewslug_mollusk": ("tongue", 0.9, 1.0), "scylla_mollusk": ("bubbles+tongue", 0.8, 0.9),
    "charybdis_mollusk": ("bubbles", 0.6, 0.9), "min_min_mollusk": ("bubbles", 1.4, 1.15),
    "giant_kraken_mollusk": ("bubbles+tongue", 0.55, 0.85), "nautilus_mollusk": ("bubbles", 1.0, 1.0),
    "sea_slug_mollusk": ("tongue", 1.15, 1.1), "anemone_mollusk": ("bubbles", 1.2, 1.1),
    "cuttlefish_mollusk": ("bubbles+tongue", 1.1, 1.05),
}
# the rest: plants, dragons, slimes, constructs and the riders. Dragons have no real recordings, so their roars are built
# from their real-animal parts (big cat and crocodile; eagle and lion for the gryphon; hen and snake for the cockatrice).
# Slimes keep the vanilla squish, constructs have no idle. Same shape as MAMMALS; None keeps her own idle.
REST = {
    # plants and fungi (Flora)
    "alraune_flora": ("leaves+girl_sing", 1.0, 1.05), "matango_flora": ("girl_sigh", 0.9, 1.0),
    "ent_flora": ("wood", 0.8, 0.9), "cactus_flora": ("leaves", 1.1, 1.05),
    "mandragora_flora": ("girl_giggle", 1.3, 1.15), "venus_flytrap_flora": ("eat+leaves", 0.9, 1.0),
    "kesaran_pasaran_flora": ("cloth", 1.4, 1.2), "dryad_flora": ("leaves+wood", 1.0, 1.05),
    "shiitake_flora": ("girl_sigh", 1.0, 1.05), "rose_flora": ("leaves+girl_sing", 1.1, 1.05),
    "lotus_flora": ("bubbles+girl_sing", 1.05, 1.05), "sunflower_flora": ("leaves+girl_giggle", 1.1, 1.1),
    "rafflesia_flora": ("tongue", 0.7, 0.95), "censhroom_centaur": ("horse", 1.1, 1.0),
    "leshy_flora": ("wood+leaves", 0.85, 0.95),
    # dragons and chimeras
    "shedim_centaur": ("deer+fox", 0.9, 1.0),
    "long_dragon": ("big_cat+cetacean", 0.75, 0.9), "wyrm_dragon": ("big_cat+croc", 0.6, 0.85),
    "gryphon_dragon": ("raptor+big_cat", 0.85, 0.95), "kannae_dragon": (None, 1.0, 0.95),
    "wyvern_dragon": ("big_cat+croc+raptor", 0.75, 0.9), "orochi_dragon": ("snake_hiss+croc", 0.6, 0.85),
    "alebrije_dragon": ("parrot+big_cat", 1.0, 1.05), "cockatrice_dragon": ("poultry+snake_hiss", 1.0, 1.0),
    "nenekea_dragon": ("big_cat+croc", 0.8, 0.95), "macawtl_dragon": ("parrot+snake_hiss", 0.95, 1.05),
    "quetzalcoatl_dragon": ("raptor+snake_hiss", 0.85, 0.95), "bahamut_dragon": ("big_cat+croc", 0.55, 0.85),
    "kun_dragon": ("cetacean+raptor", 0.7, 0.9), "amphiptere_dragon": ("snake_hiss+raptor", 0.9, 1.0),
    "ender_dragon": ("big_cat+croc", 0.65, 0.85), "chimera_dragon": ("big_cat+goat+snake_hiss", 0.85, 0.9),
    # slimes keep their squish
    **{sp: (None, 1.0, vp) for sp, vp in {
        "verdant_slime": 1.15, "magma_slime": 1.0, "honey_slime": 1.15, "frost_slime": 1.1, "amethyst_slime": 1.1,
        "mixed_slime": 1.1, "mimic_slime": 1.0, "ender_slime": 1.0, "undine_slime": 1.1, "klabuster_slime": 1.05,
        "copper_slime": 1.05}.items()},
    # constructs: no idle
    **{sp: (None, 1.0, vp) for sp, vp in {
        "golem_construct": 0.85, "gargoyle_construct": 0.9, "talosling_construct": 1.0, "tin_squirrel_construct": 1.2,
        "tin_jack_construct": 1.05, "automaton_construct": 1.0, "scarecrow_construct": 1.0,
        "snowgirl_construct": 1.15}.items()},
    # the riders: her horse, and her own laugh or wail
    "war_rider": ("horse+girl_cackle", 0.85, 0.9), "famine_rider": ("horse+girl_sigh", 0.85, 0.95),
    "pestilence_rider": ("horse+ghost", 0.85, 0.95), "death_rider": ("horse+ghost", 0.7, 0.85),
    "charro_negro_rider": ("horse+girl_cackle", 0.9, 0.95),
}
# extra one-off events that fit a shared group: event -> (group, pitch)
EXTRA ={"entity.crow_harpy.giggle": ("girl_giggle", 1.0), "entity.crow_harpy.laugh": ("girl_giggle", 0.95)}


def plans():
    """species -> {slot: (group, pitch)} for every species this tool voices."""
    out = {}
    for sp, (call, cp, vp) in {**HARPIES, **MAMMALS, **SMALL, **MYTHIC, **SEA, **REST}.items():
        out[sp] = {"hurt": ("girl_hurt", vp), "death": ("girl_death", vp), "eat": ("eat", vp)}
        if call:
            out[sp]["ambient"] = (call, cp)
    for sp, (call, cp, vp, bellow, bp) in ANURANS.items():
        out[sp] = {"ambient": (call, cp), "bellow": (bellow, bp), "tongue": ("tongue", cp), "hurt": ("girl_hurt", vp),
                   "death": ("girl_death", vp), "eat": ("eat", vp)}
    for sp, (amb, ap, vp) in CAUDATA.items():
        out[sp] = {"hurt": ("girl_hurt", vp), "death": ("girl_death", vp), "eat": ("eat", vp)}
        if amb:
            out[sp]["ambient"] = (amb, ap)
    return out
KEEP_OWN = {"entity.ostrich_harpy.ambient"}  # already a custom file


def process(src, dst, max_s, peak):
    data, sr = sf.read(src, always_2d=True)
    x = data.mean(axis=1)
    if sr != RATE:
        x = resample_poly(x, RATE, sr)
    # keep from the first loud sample, up to max_s, ending where it goes quiet
    env = np.abs(x)
    thresh = env.max() * 0.08
    loud = np.nonzero(env > thresh)[0]
    if len(loud) == 0:
        return False
    start = max(0, loud[0] - int(0.01 * RATE))
    end = min(len(x), loud[-1] + int(0.08 * RATE), start + int(max_s * RATE))
    x = x[start:end]
    x = x - x.mean()
    fade = min(len(x) // 4, int(0.06 * RATE))
    if fade > 0:
        x[-fade:] *= np.linspace(1, 0, fade)
        x[:32] *= np.linspace(0, 1, 32)
    x = x / (np.abs(x).max() or 1) * peak
    sf.write(dst, x.astype(np.float32), RATE, format="OGG", subtype="VORBIS")
    return True


def entries(pool, group, pitch):
    """Every clip of a group ("a+b" joins pools, for groups with only a few clips) at one pitch."""
    return [{"name": f"harmontide:shared/{g}/{n}", "pitch": round(pitch, 2)}
            for g in group.split("+") for n in pool.get(g, [])]


def main():
    harvest = sys.argv[1]
    manifest = json.load(open(os.path.join(harvest, "manifest.json"), encoding="utf-8"))
    # which Freesound clip each file was made from: a file whose source did not change is left as it is (Vorbis
    # output differs on every encode, so rewriting would churn every file in git)
    index_path = os.path.join(SHARED, "sources.json")
    old_index = json.load(open(index_path, encoding="utf-8")) if os.path.exists(index_path) else {}
    index, pool, credits = {}, {}, []
    for group, clips in manifest.items():
        os.makedirs(os.path.join(SHARED, group), exist_ok=True)
        max_s, peak = SHAPE.get(group, DEFAULT_SHAPE)
        for c in clips:
            n = str(len(pool.get(group, [])) + 1)
            key, dst = f"{group}/{n}", os.path.join(SHARED, group, n + ".ogg")
            if (old_index.get(key) == c["id"] and os.path.exists(dst)) \
                    or process(os.path.join(harvest, c["file"]), dst, max_s, peak):
                pool.setdefault(group, []).append(n)
                credits.append((f"shared/{group}/{n}.ogg", c))
                index[key] = c["id"]
    for root, _, names in os.walk(SHARED):                                   # files no longer made from anything
        for name in names:
            rel = os.path.relpath(os.path.join(root, name), SHARED).replace(os.sep, "/")
            if name.endswith(".ogg") and rel[:-4] not in index:
                os.remove(os.path.join(root, name))
    json.dump(index, open(index_path, "w", encoding="utf-8", newline="\n"), indent=1, sort_keys=True)

    data = json.load(open(SOUNDS_JSON, encoding="utf-8"))
    changed = 0
    for sp, plan in plans().items():
        for slot, (group, pitch) in plan.items():
            sounds = entries(pool, group, pitch)
            ev = f"entity.{sp}.{slot}"
            if ev in data and ev not in KEEP_OWN and sounds:
                data[ev]["sounds"] = sounds
                changed += 1
    for ev, (group, p) in EXTRA.items():
        if ev in data and pool.get(group):
            data[ev]["sounds"] = entries(pool, group, p)
            changed += 1
    json.dump(data, open(SOUNDS_JSON, "w", encoding="utf-8", newline="\n"), indent=2, ensure_ascii=False)
    open(SOUNDS_JSON, "a", encoding="utf-8", newline="\n").write("\n")

    with open(CREDITS, "w", encoding="utf-8", newline="\n") as f:
        f.write("# Sound credits\n\nGENERATED by `tools/sound_import.py`. Every clip below is CC0 1.0 (public domain) from "
                "Freesound; credit is not required but given anyway. Clips were trimmed, made mono and levelled.\n\n"
                "| File | Source | Author |\n|---|---|---|\n")
        for path, c in credits:
            title = html.unescape(c["title"]).replace("|", "/")
            f.write(f"| `{path}` | [{title}]({c['url']}) | {c['author']} |\n")
    print("groups", {g: len(v) for g, v in pool.items()}, "events changed", changed)


if __name__ == "__main__":
    main()
