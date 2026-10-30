#!/usr/bin/env python3
"""Centaurs (the hoofed family, see centaur_field_guide.html): horse, zebra, reindeer, mountain goat, alpaca.

    python tools/gen_centaur.py [species ...] [--previews]

Shared girl_lib upper body (waist at 19.5) on a four-legged lower body: barrel, leg_f_r/f_l/b_r/b_l with shin_* and
hooves, tail. Species extras: reindeer antlers (bone `antlers`), goat horns (`horns`), alpaca fleece (inflated
barrel). Horse-like ears. Five colours per species. `armor_torso` is an invisible torso-sized box the game fits a
worn chestplate to (GeckoLib's vanilla armour layer).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_lib as G  # noqa: E402
import girl_parts as P  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, shades, CLEAR  # noqa: E402

SPECIES = {
    # twenty-first wave (folklore): Urcuchillay, the Inca god who watches over the herds: a llama of many colours, rainbow hair and
    # fleece, a blue dress and a headband with a blue stone
    "urcuchillay_centaur": dict(
        build="standard", outfit=dict(top=["halter"], extras=["armlets", "scarf"], colors=dict(scarf="#3a7ad8", cloth2="#3a7ad8")),
        hair="flowing", bangs="wispy", tail=(2.5, 4, 1.75), fleece=True,
        pal=dict(SKIN=skin("fair"), HAIR="#f8d8e8", FUR="#f4ece4", LIGHT="#fffaf4", MARK="#c8a888", EYE="#6a3a1e", CLOTH="#3a7ad8", TRIM="#e8f0fc",
                 pattern="solid", extra=dict(rainbow=True)),
        variants={"white": dict(HAIR="#f4f0ec", FUR="#f4f0ea", MARK="#a89888", CLOTH="#c84a3a", EYE="#3a2a1e"),
                  "brown": dict(HAIR="#7a5236", FUR="#8a6040", LIGHT="#d8b896", MARK="#3a2414", CLOTH="#2a8a6a", EYE="#2a1a10"),
                  "grey": dict(HAIR="#9a9aa0", FUR="#a8a8ae", LIGHT="#e8e8ea", MARK="#4a4a50", CLOTH="#e8a030", EYE="#2a2a30"),
                  "black": dict(HAIR="#1e1c20", FUR="#2a282c", LIGHT="#6a6870", MARK="#0e0c10", CLOTH="#e83a6a", EYE="#c8902a")}),
    # twentieth wave: the ceibler, a slender pale-blue llama-deer, long ears and little horns, blue diamonds over her coat, a fringed
    # blanket and harness hung with tassels and bells
    "ceibler_centaur": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], extras=["armlets"], colors=dict(cloth2="#2a48b0")),
        hair="flowing", bangs="wispy", tail=(2.5, 4, 1.5), horns=True, fleece=True, blanket=True, bells=True,
        pal=dict(SKIN=skin("fair"), HAIR="#e8f0fc", FUR="#c8d8f4", LIGHT="#f4f8ff", MARK="#2a48b0", EYE="#2a3a9a", CLOTH="#2a48b0", TRIM="#e8f0fc",
                 pattern="diamonds", pattern_scale=2.4, extra=dict(horn="#3a5ac8", bell="#e8f0fc", blanket="#2a48b0", blanket2="#f4f8ff", blanket3="#5ac8f0")),
        variants={"rose": dict(FUR="#f4d8e4", HAIR="#fff4f8", MARK="#c84a7a", CLOTH="#c84a7a", EYE="#8a2a5a",
                               extra=dict(horn="#c84a7a", bell="#fff4f8", blanket="#c84a7a", blanket2="#fff4f8", blanket3="#f8c040")),
                  "fawn": dict(FUR="#e0c8a0", HAIR="#f8f0e0", MARK="#8a5a2a", CLOTH="#8a5a2a", EYE="#3a2412",
                               extra=dict(horn="#5a3a1e", bell="#e8c050", blanket="#a83a2a", blanket2="#f8f0e0", blanket3="#2a6a5a")),
                  "jade": dict(FUR="#c8ecdc", HAIR="#f0fcf6", MARK="#2a8a5a", CLOTH="#2a8a5a", EYE="#1e5a3a",
                               extra=dict(horn="#2a8a5a", bell="#f0fcf6", blanket="#2a8a5a", blanket2="#f0fcf6", blanket3="#e8c040")),
                  "dusk": dict(FUR="#a8a0d8", HAIR="#e8e0fc", MARK="#3a2a7a", CLOTH="#3a2a7a", EYE="#e8a040",
                               extra=dict(horn="#3a2a7a", bell="#e8c050", blanket="#3a2a7a", blanket2="#e8e0fc", blanket3="#f87a8a"))}),
    # eighteenth wave: the palmyr, a blue deer taur in white diamonds with feathery frond antlers and a fan of a tail
    "palmyr_centaur": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], extras=["pearls", "armlets"], colors=dict(cloth2="#c8d8f8")),
        hair="flowing", bangs="wispy", tail=(4, 7, 1.25), antlers=True, fronds=True, ears="round",
        pal=dict(SKIN=skin("fair"), HAIR="#a8c0f0", FUR="#3a5ac8", LIGHT="#d8e4fc", MARK="#f0f4ff", EYE="#2a3a9a", CLOTH="#2a48b0", TRIM="#e8f0ff",
                 pattern="diamonds", pattern_scale=2.4, extra=dict(antler="#c8d8f8", antler2="#3a5ac8", frond="#8aa8f0", frond_edge="#f0f4ff")),
        variants={"white": dict(FUR="#eef2fa", HAIR="#ffffff", LIGHT="#ffffff", MARK="#8aa8e0", CLOTH="#8aa8e0",
                                extra=dict(antler="#f4f6fc", antler2="#a8b8e0", frond="#dce4f8", frond_edge="#8aa8e0")),
                  "teal": dict(FUR="#2a8a9a", HAIR="#a8e8e8", LIGHT="#d8f4f4", MARK="#e8fcfc", CLOTH="#1e6a7a",
                               extra=dict(antler="#c8f0f0", antler2="#2a8a9a", frond="#7ad0d8", frond_edge="#e8fcfc")),
                  "violet": dict(FUR="#6a4ab8", HAIR="#d0b8f8", LIGHT="#ece0fc", MARK="#f4ecff", CLOTH="#5a3aa8",
                                 extra=dict(antler="#e0d0f8", antler2="#6a4ab8", frond="#b898f0", frond_edge="#f4ecff")),
                  "dusk": dict(FUR="#3a2a5a", HAIR="#f0a8c0", LIGHT="#c8a8d8", MARK="#f8c0a0", CLOTH="#a83a5a", EYE="#f8a040",
                               extra=dict(antler="#f8d0b8", antler2="#a83a5a", frond="#e87a8a", frond_edge="#f8d0a0"))}),
    # seventeenth wave: the stagel, a fawn taur, cream and blue-dappled, with budding antlers
    "stagel_centaur": dict(
        build="petite", outfit=dict(top=["halter", "capelet"], extras=["pearls"], colors=dict(cloth2="#a8c0f0")),
        hair="wavy", tail=(2, 3, 1.5), antlers=True, ears="round",
        pal=dict(SKIN=skin("fair"), HAIR="#e8d8c8", FUR="#e0cdb8", LIGHT="#ffffff", MARK="#4a7ad8", EYE="#2a5ab0", CLOTH="#4a7ad8", TRIM="#f8f4ec",
                 pattern="spots", extra=dict(antler="#e8d0a8", antler2="#c8a878")),
        variants={"brown": dict(FUR="#a8784a", HAIR="#7a5432", MARK="#f8f0e4", CLOTH="#3a6a3a", EYE="#2a1a10", extra=dict(antler="#e8d8b8", antler2="#a8885a")),
                  "white": dict(FUR="#f8f8fa", HAIR="#ffffff", MARK="#c8d0e0", CLOTH="#8ab0e8", extra=dict(antler="#f0e8d8", antler2="#d8c8b0")),
                  "rose": dict(FUR="#f0d0d8", HAIR="#f8e0e8", MARK="#d86a8a", CLOTH="#d86a8a", extra=dict(antler="#f0e0d0", antler2="#d8b8a8")),
                  "moss": dict(FUR="#c8d0b0", HAIR="#a8b88a", MARK="#4a7a3a", CLOTH="#4a7a3a", extra=dict(antler="#d8c8a0", antler2="#8a9a5a"))}),
    # seventeenth wave: the lamassu, the winged, horned lion-bodied guardian of Assyrian gates, long grey curls down her back
    "lamassu_centaur": dict(
        build="athletic", outfit=dict(top=["bandeau"], extras=["usekh", "armlets", "belt"], colors=dict(usekh2="#3a4a6a")),
        hair="wavy", tail=(1, 9, 1), wings=True, horns=True, ears="round",
        pal=dict(SKIN=skin("fair"), HAIR="#5a6474", FUR="#7a8494", LIGHT="#c8ccd4", MARK="#3a424c", EYE="#3a6a9a", CLOTH="#c8a050", TRIM="#3a4a6a",
                 pattern="solid", extra=dict(wing="#a8acb4", wing_tip="#4a525c", horn="#5a4a3a")),
        variants={"gold": dict(FUR="#c8964e", LIGHT="#ecd2a0", MARK="#6a4422", HAIR="#3a2a1a", CLOTH="#2a4a9a",
                               extra=dict(wing="#e8c870", wing_tip="#8a6418", horn="#3a2a1a")),
                  "alabaster": dict(FUR="#ece4d4", LIGHT="#ffffff", MARK="#b8ac98", HAIR="#d8d0c0", extra=dict(wing="#f4f0e8", wing_tip="#b8ac98", horn="#8a7a6a")),
                  "lapis": dict(FUR="#2a40a0", LIGHT="#6a80d0", MARK="#141e5a", HAIR="#141e4a", CLOTH="#e8c050",
                                extra=dict(wing="#3a5ab8", wing_tip="#e8c050", horn="#e8c050")),
                  "basalt": dict(FUR="#2a2628", LIGHT="#5a5458", MARK="#141214", HAIR="#141214", EYE="#e8c050",
                                 extra=dict(wing="#3a3438", wing_tip="#141214", horn="#c8a050"))}),
    "horse_centaur": dict(
        build="athletic", outfit=dict(top=["bandeau", "harness"], extras=["bracers", "belt"]),
        hair="flowing", tail=(2, 9, 2),
        pal=dict(SKIN=skin("fair"), HAIR="#2a1c16", FUR="#7a4426", LIGHT="#a8704a", MARK="#1e1612", EYE="#5a3a22", CLOTH="#2a4a6a", TRIM="#d8b860",
                 pattern="solid", extra=dict(socks=True)),
        variants={"chestnut": dict(FUR="#a85a2a", HAIR="#8a4a22", MARK="#7a3e1a", CLOTH="#3a5a3a"),
                  "palomino": dict(FUR="#d8b060", HAIR="#f2ead0", MARK="#c8a050", LIGHT="#f0dca8", CLOTH="#7a3a3a"),
                  "grey": dict(FUR="#a8a8a8", HAIR="#d8d8d8", MARK="#6a6a6a", pattern="dapple", LIGHT="#dcdcdc", CLOTH="#4a3a6a"),
                  "black": dict(FUR="#1e1c1e", HAIR="#141214", MARK="#0e0c0e", LIGHT="#3a3638", CLOTH="#8a2a2a")}),
    "zebra_centaur": dict(
        build="strong", outfit=dict(top=["sports"], extras=["pauldron", "arm_wraps", "belt"]),
        hair="pixie", tail=(1.5, 8, 1.5),
        pal=dict(SKIN=skin("brown"), HAIR="#141414", FUR="#f0eee8", LIGHT="#f8f6f2", MARK="#141414", EYE="#3a2418", CLOTH="#c8702a", TRIM="#e8d8a0",
                 pattern="stripes"),
        variants={"grevy": dict(pattern="finestripes"),
                  "mountain": dict(pattern="stripes", pattern_scale=1.3, FUR="#e8e0d0"),
                  "golden": dict(FUR="#f6ecd2", MARK="#c8a060", HAIR="#c8a060", CLOTH="#4a6a8a"),
                  "quagga": dict(FUR="#a07850", MARK="#3a2a1e", LIGHT="#ece4d6", HAIR="#3a2a1e", pattern="finestripes", pattern_scale=1.5)}),
    # twenty-fourth wave: the qilin, a scaled golden deer with soft antlers and a teal mane
    "qilin_centaur": dict(
        build="standard", outfit=dict(top=["bandeau"], extras=["pearls", "armlets"], colors=dict()),
        hair="flowing", tail=(2, 3, 1.5), antlers=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2aa8a0", FUR="#e0b040", LIGHT="#f8ecc0", MARK="#a87a1e", EYE="#c8501e", CLOTH="#2aa8a0", TRIM="#f8e080",
                 pattern="spots", extra=dict(antler="#f8e8b0")),
        variants={"jade": dict(FUR="#4ab888", LIGHT="#d8f4e4", MARK="#2a7a58", HAIR="#e8c040", CLOTH="#e8c040", extra=dict(antler="#e8f4d8")),
                  "white": dict(FUR="#f0ece4", LIGHT="#ffffff", MARK="#c8c0a8", HAIR="#e8a0b0", CLOTH="#e8a0b0", extra=dict(antler="#f8e8d8")),
                  "red": dict(FUR="#c84a2a", LIGHT="#f8d0a8", MARK="#7a2a14", HAIR="#f0c040", CLOTH="#f0c040", extra=dict(antler="#f8e0a0")),
                  "blue": dict(FUR="#4a7ac8", LIGHT="#d8e8f8", MARK="#2a4a8a", HAIR="#f0e8d0", CLOTH="#f0e8d0", extra=dict(antler="#e8f0f8"))}),
    "reindeer_centaur": dict(
        build="standard", outfit=dict(top=["sweater"], extras=["scarf"], colors=dict(scarf="#2e7a4a")),
        hair="braid", tail=(2, 3, 1.5), antlers=True,
        pal=dict(SKIN=skin("pale"), HAIR="#e8e2d4", FUR="#7a6048", LIGHT="#ece4d6", MARK="#4a3a2a", EYE="#4a3020", CLOTH="#9a2a2a", TRIM="#f0e8d0",
                 pattern="solid", extra=dict(antler="#c8b090")),
        variants={"woodland": dict(FUR="#4a3626", HAIR="#c8b8a0", CLOTH="#2a4a2a"),
                  "svalbard": dict(FUR="#c8beb0", HAIR="#f4f0e8", CLOTH="#3a5a7a"),
                  "white": dict(FUR="#f0eee8", HAIR="#ffffff", LIGHT="#ffffff", extra=dict(antler="#e8dcc0"), CLOTH="#6a3a8a"),
                  "piebald": dict(pattern="patches", MARK="#f2f0ea", HAIR="#d8d0c0")}),
    "goat_centaur": dict(
        build="slim", outfit=dict(top=["halter", "vest"], extras=["satchel"]),
        hair="sidetail", tail=(1.5, 2.5, 1.5), horns=True,
        pal=dict(SKIN=skin("rosy"), HAIR="#f4f2ee", FUR="#efede6", LIGHT="#ffffff", MARK="#3a3632", EYE="#c8a040", CLOTH="#5a7a3a", TRIM="#d0c8b0",
                 pattern="solid", pupil=(3.0, 0.9), extra=dict(horn="#2a2826")),
        variants={"ibex": dict(FUR="#8a7a62", HAIR="#6a5a46", LIGHT="#cfc4b2", extra=dict(horn="#6a5e4e"), CLOTH="#3a4a5a"),
                  "chamois": dict(FUR="#a0643a", HAIR="#3a2a20", MARK="#1e1612", pattern="cross", extra=dict(horn="#141210"), CLOTH="#6a4a2a"),
                  "grey": dict(FUR="#7a7a7e", HAIR="#9a9a9e", LIGHT="#bcbcc0", CLOTH="#4a3a5a"),
                  "cream": dict(FUR="#e8dcc0", HAIR="#f0e6cc", extra=dict(horn="#5a4a3a"))}),
    "alpaca_centaur": dict(
        build="curvy", outfit=dict(top=["bandeau", "capelet"], extras=["pearls"], colors=dict(cloth2="#c8463a")),
        hair="mane", tail=(2, 2.5, 2), fleece=True,
        pal=dict(SKIN=skin("peach"), HAIR="#f4f0e8", FUR="#f2ede2", LIGHT="#ffffff", MARK="#d8d0c0", EYE="#2a1e16", CLOTH="#c84a6a", TRIM="#f0c840",
                 pattern="solid"),
        variants={"fawn": dict(FUR="#d8b888", HAIR="#e2c49a", MARK="#c8a070", CLOTH="#3a7a8a"),
                  "brown": dict(FUR="#7a5236", HAIR="#8a6044", MARK="#5a3a24", CLOTH="#e0a030"),
                  "black": dict(FUR="#2a2624", HAIR="#3a3430", MARK="#1a1614", LIGHT="#5a5450", CLOTH="#d0d0d0"),
                  "grey": dict(FUR="#a8a6a4", HAIR="#c0bebc", MARK="#8a8886", CLOTH="#6a3a8a")}),
    # tenth wave: the tusktaur (a woolly mammoth taur: a shaggy fleece, tusks curving up from the front of her barrel)
    "tusktaur_centaur": dict(
        build="strong", outfit=dict(top=["sarashi", "mantle"], extras=["bracers"], colors=dict(cloth2="#6a3a22")),
        hair="shaggy", tail=(1.5, 4, 1.5), fleece=True, tusks=True,
        pal=dict(SKIN=skin("tan"), HAIR="#2a1a12", FUR="#6a3a22", LIGHT="#9a6a4a", MARK="#3a2012", EYE="#4a2a14", CLOTH="#c8a060", TRIM="#e8d8b0",
                 pattern="solid", extra=dict(tusk="#f0e6cc")),
        variants={"black": dict(FUR="#262020", HAIR="#141010", LIGHT="#4a4040", MARK="#0e0a0a", CLOTH="#a83a2a"),
                  "snow": dict(FUR="#ece6da", HAIR="#f6f2ea", LIGHT="#ffffff", MARK="#c8bca8", CLOTH="#3a6aa8"),
                  "steppe": dict(FUR="#b08a52", HAIR="#7a5a32", LIGHT="#d8b880", MARK="#6a4a24", CLOTH="#4a7a4a"),
                  "ember": dict(FUR="#3a2a24", HAIR="#1e1410", LIGHT="#6a5040", MARK="#140e0a", CLOTH="#e8a830", extra=dict(tusk="#e8c050"))}),
    # tenth wave: the aurorantler, a deer taur whose antlers glow (glow masks) in the colours of the northern lights
    "aurorantler_centaur": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], extras=["pearls"], colors=dict(cloth2="#2a3a6a")),
        hair="flowing", tail=(2, 3, 1.5), antlers=True, glow=["antler_a", "antler_b", "antler_c"],
        pal=dict(SKIN=skin("pale"), HAIR="#e8f4ff", FUR="#4a5a7a", LIGHT="#c8d8f0", MARK="#2a3450", EYE="#6af0d0", CLOTH="#2a3a6a", TRIM="#a8f0e0",
                 pattern="solid", extra=dict(antler="#6af0d0", antler2="#8a6af0")),
        variants={"violet": dict(FUR="#4a3a6a", MARK="#2a1e40", EYE="#c88af8", extra=dict(antler="#c88af8", antler2="#6a8af0")),
                  "dawn": dict(FUR="#6a4a5a", MARK="#3a2432", HAIR="#ffe8f0", EYE="#f8a0c0", extra=dict(antler="#f8a0c0", antler2="#f8d080")),
                  "frost": dict(FUR="#a8b8c8", MARK="#6a7a8a", LIGHT="#ffffff", EYE="#e8f8ff", extra=dict(antler="#ffffff", antler2="#a8e8ff")),
                  "ember": dict(FUR="#5a3a2a", MARK="#2a1a12", HAIR="#ffe0b0", EYE="#f8c050", extra=dict(antler="#f8c050", antler2="#f86a3a"))}),
    # fifteenth wave: the bellmare, a blue donkey taur with little goat's horns, hung with silver bells
    "bellmare_centaur": dict(
        build="curvy", outfit=dict(top=["halter", "harness"], extras=["bell_collar", "armlets"], colors=dict(cloth2="#2a3a7a")),
        hair="braid", tail=(1.5, 5, 1.5), horns=True, bells=True,
        pal=dict(SKIN=skin("fair"), HAIR="#e8ecf4", FUR="#8aa0d8", LIGHT="#e8eef8", MARK="#3a4a8a", EYE="#3a4a8a", CLOTH="#3a4a9a", TRIM="#d8dce8",
                 pattern="solid", extra=dict(horn="#e8dcc0", bell="#d8dce8")),
        variants={"grey": dict(FUR="#8a8a8e", HAIR="#d8d8dc", MARK="#3a3a3e", LIGHT="#e0e0e4", extra=dict(horn="#5a5048", bell="#e8c050")),
                  "brown": dict(FUR="#7a5a3e", HAIR="#3a2a1e", MARK="#2a1e14", LIGHT="#e0d0bc", extra=dict(horn="#e0d0b0", bell="#e8c050")),
                  "white": dict(FUR="#f0eee8", HAIR="#ffffff", MARK="#a8a49c", LIGHT="#ffffff", EYE="#5a8ac8", extra=dict(horn="#c8b898", bell="#e8c050")),
                  "black": dict(FUR="#2a282c", HAIR="#141216", MARK="#0e0c10", LIGHT="#6a686e", EYE="#e8c040", extra=dict(horn="#e8dcc0", bell="#d8dce8"))}),
    # fifteenth wave: the vicunero, a slim blue vicuña taur with a fine fleece and a woven saddle blanket
    "vicunero_centaur": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], extras=["armlets"], colors=dict(cloth2="#2a3a8a")),
        hair="ponytail", tail=(1.5, 3, 1.5), fleece=True, blanket=True,
        pal=dict(SKIN=skin("fair"), HAIR="#f4f2ee", FUR="#5a7ac8", LIGHT="#f4f4f8", MARK="#2a3a7a", EYE="#2a2a4a", CLOTH="#2a3a8a", TRIM="#e8e8f0",
                 pattern="solid", extra=dict(blanket="#2a3a8a", blanket2="#e8e8f0", blanket3="#e8b040")),
        variants={"tan": dict(FUR="#c8925a", HAIR="#f4ece0", MARK="#8a5a2a", extra=dict(blanket="#a83a2a", blanket2="#f4ece0", blanket3="#2a6a5a")),
                  "cream": dict(FUR="#ece0c8", HAIR="#ffffff", MARK="#b8a888", extra=dict(blanket="#6a3a8a", blanket2="#f4ece0", blanket3="#e8b040")),
                  "brown": dict(FUR="#6a4a30", HAIR="#e8dcc8", MARK="#3a2818", extra=dict(blanket="#2a6a4a", blanket2="#f4ece0", blanket3="#e8b040")),
                  "grey": dict(FUR="#8a8a90", HAIR="#e8e8ec", MARK="#4a4a50", extra=dict(blanket="#a8282a", blanket2="#f4f4f4", blanket3="#2a2a30"))}),
    # fourteenth wave: the prairhorn, a pronghorn taur banded tan and white, with short pronged horns
    "prairhorn_centaur": dict(
        build="athletic", outfit=dict(top=["sports"], extras=["armlets"], colors=dict(cloth2="#5a3a22")),
        hair="ponytail", tail=(2, 2.5, 1.5), horns=True, ears="round",
        pal=dict(SKIN=skin("tan"), HAIR="#5a3a22", FUR="#c8884a", LIGHT="#f8f0e4", MARK="#3a2414", EYE="#3a2412", CLOTH="#3a6a8a", TRIM="#f0e8d8",
                 pattern="solid", extra=dict(horn="#2a2420")),
        variants={"red": dict(FUR="#b8582a", HAIR="#7a3418", MARK="#3a1408"),
                  "dark": dict(FUR="#6a4a30", HAIR="#3a2614", MARK="#1e140a"),
                  "pale": dict(FUR="#e8d0a8", HAIR="#a88a64", MARK="#6a5434"),
                  "blue": dict(FUR="#5a7ac8", HAIR="#2a3a7a", MARK="#141e4a", CLOTH="#c8a050")}),
    # fourteenth wave: the halberelk, an elk taur whose antler tines end in steel-bright blades
    "halberelk_centaur": dict(
        build="strong", outfit=dict(top=["sarashi", "vest"], extras=["bracers", "belt"], colors=dict(cloth2="#3a2a1e", leather="#5a3a22")),
        hair="mane", tail=(2, 2.5, 1.5), antlers=True, blades=True, ears="round",
        pal=dict(SKIN=skin("fair"), HAIR="#e8dcc0", FUR="#3a5ab0", LIGHT="#e8dcc0", MARK="#1e2e6a", EYE="#e8a030", CLOTH="#c8a060", TRIM="#e8dcc0",
                 pattern="solid", extra=dict(antler="#e8dcc0", antler2="#c8b898", blade="#c8d0dc")),
        variants={"brown": dict(FUR="#6a4a2e", MARK="#3a2414", HAIR="#d8c8a0"),
                  "grey": dict(FUR="#7a7a82", MARK="#3a3a42", HAIR="#e0e0e4"),
                  "white": dict(FUR="#ece8e0", MARK="#a8a49c", HAIR="#ffffff", EYE="#4a8ac8"),
                  "black": dict(FUR="#26242a", MARK="#0e0c10", HAIR="#c8c0b0", EYE="#e8d040")}),
    # thirteenth wave: the momoshika, a white-and-blue deer taur with peach blossoms in her antlers
    "momoshika_centaur": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], extras=["pearls"], colors=dict(cloth2="#f4c0d0")),
        hair="flowing", tail=(2, 3, 1.5), antlers=True, blossoms=True, ears="round",
        pal=dict(SKIN=skin("pale"), HAIR="#f4f0f8", FUR="#d8e4f4", LIGHT="#ffffff", MARK="#3a5ab0", EYE="#e86aa0", CLOTH="#3a5ab0", TRIM="#f8b8d0",
                 pattern="spots", extra=dict(antler="#f0e8e0", antler2="#e8d8d0", bloom="#f8a8c8", bloom_in="#f8e070")),
        variants={"pink": dict(FUR="#f4d8e4", MARK="#c8507a", HAIR="#fbe8f0", CLOTH="#c8507a",
                               extra=dict(antler="#f0e8e0", antler2="#e8d8d0", bloom="#ffffff", bloom_in="#f8a8c8")),
                  "gold": dict(FUR="#f0dca8", MARK="#a87a2a", HAIR="#fff4d8", CLOTH="#a87a2a", EYE="#3a8ac8",
                               extra=dict(antler="#f0e0b0", antler2="#e8c880", bloom="#f8a8c8", bloom_in="#f8e070")),
                  "green": dict(FUR="#d8ecd8", MARK="#3a8a5a", HAIR="#f0f8f0", CLOTH="#3a8a5a",
                                extra=dict(antler="#e8e0d0", antler2="#d8d0c0", bloom="#f8a8c8", bloom_in="#f8e070")),
                  "white": dict(FUR="#f8f8fa", MARK="#b8bcc8", HAIR="#ffffff", CLOTH="#8a8ac8", EYE="#8a6ad8",
                                extra=dict(antler="#f8f4f0", antler2="#ece4dc", bloom="#f8a8c8", bloom_in="#f8e070"))}),
    # eleventh wave: the sphinx, a winged lioness taur in a striped nemes headcloth
    "sphinx_centaur": dict(
        build="athletic", outfit=dict(top=["bandeau"], extras=["usekh", "armlets", "belt"], colors=dict(usekh2="#2a4a9a")),
        hair="hime", tail=(1, 9, 1), wings=True, nemes=True, ears="round",
        pal=dict(SKIN=skin("tan"), HAIR="#2a1e14", FUR="#c8964e", LIGHT="#ecd2a0", MARK="#6a4422", EYE="#3a8ac8", CLOTH="#e8c050", TRIM="#2a4a9a",
                 pattern="solid", extra=dict(wing="#d8b070", wing_tip="#6a4422", nemes="#e8c050", nemes_band="#2a4a9a")),
        variants={"night": dict(FUR="#2a2628", LIGHT="#5a5458", MARK="#141214", HAIR="#141214", EYE="#e8c050", CLOTH="#c8ccd4",
                                extra=dict(wing="#3a3438", wing_tip="#141214", nemes="#c8ccd4", nemes_band="#2a2a3a")),
                  "red": dict(FUR="#a8582e", LIGHT="#e0a070", MARK="#5a2a12", extra=dict(wing="#c87a4a", wing_tip="#5a2a12", nemes="#e8c050", nemes_band="#a82a2a")),
                  "white": dict(FUR="#ece4d4", LIGHT="#ffffff", MARK="#b8ac98", HAIR="#3a3a42", CLOTH="#2a4a9a",
                                extra=dict(wing="#f4f0e8", wing_tip="#b8ac98", nemes="#f4f0e8", nemes_band="#2a4a9a")),
                  "gilded": dict(FUR="#d8a840", LIGHT="#f8e090", MARK="#8a6418", extra=dict(wing="#f0c850", wing_tip="#8a6418", nemes="#2a4a9a", nemes_band="#f0c850"))}),
    "tapir_centaur": dict(
        build="strong", outfit=dict(top=["kimono"], colors=dict(kimono="#3a3a52", kimono_trim="#e8e0d0", obi="#c8a050")),
        hair="bob", tail=(1.5, 1.5, 1.5),
        pal=dict(SKIN=skin("tan"), HAIR="#1e1c20", FUR="#24222a", LIGHT="#eceae4", MARK="#141216", EYE="#3a2a1e", CLOTH="#3a3a52", TRIM="#c8a050",
                 pattern="solid", extra=dict(saddle=True)),
        variants={"lowland": dict(FUR="#4a3a2e", HAIR="#2a1e16", LIGHT="#8a7a6a", MARK="#2a2018", extra=dict()),
                  "mountain": dict(FUR="#2e2622", HAIR="#1a1614", LIGHT="#d8d0c8", MARK="#1a1614", pattern="speckle", extra=dict()),
                  "bairds": dict(FUR="#6a625a", HAIR="#3a342e", LIGHT="#c8c0b0", MARK="#3a342e", extra=dict()),
                  "calf": dict(FUR="#5a3e2a", HAIR="#2a1e16", MARK="#e8dcc0", LIGHT="#e8dcc0", pattern="stripes", extra=dict())}),
    "camel_centaur": dict(
        build="curvy", outfit=dict(top=["bandeau", "vest"], extras=["scarf", "satchel"], colors=dict(scarf="#c8582a")),
        hair="braid", tail=(1.5, 6, 1.5), humps=2,
        pal=dict(SKIN=skin("tan"), HAIR="#5a3a22", FUR="#9a7046", LIGHT="#d8bc90", MARK="#6a4a2c", EYE="#3a2414", CLOTH="#8a3a2a", TRIM="#e8c060",
                 pattern="solid"),
        variants={"wild": dict(FUR="#a89a80", HAIR="#7a6a54", LIGHT="#d8d0bc", MARK="#7a6e5a"),
                  "white": dict(FUR="#ece4d4", HAIR="#f4ece0", LIGHT="#ffffff", MARK="#c8bca8", CLOTH="#2a5a7a"),
                  "dark": dict(FUR="#5a3a24", HAIR="#2e1e14", MARK="#3a2414", LIGHT="#8a6a4a", CLOTH="#c8a040"),
                  "winter": dict(FUR="#c8a878", HAIR="#e0c8a0", LIGHT="#f0e0c0", MARK="#a08a64", CLOTH="#4a3a6a")}),
    "dromedary_centaur": dict(
        build="athletic", outfit=dict(top=["halter", "capelet"], extras=["armlets", "pearls"]),
        hair="flowing", tail=(1.5, 6, 1.5), humps=1,
        pal=dict(SKIN=skin("brown"), HAIR="#2a1e16", FUR="#c8a06a", LIGHT="#ecd8b0", MARK="#a07a4a", EYE="#3a2414", CLOTH="#2a5a8a", TRIM="#e8c060",
                 pattern="solid"),
        variants={"white": dict(FUR="#efe6d6", LIGHT="#ffffff", MARK="#d8ccb8", CLOTH="#8a2a3a"),
                  "black": dict(FUR="#2e2420", MARK="#1a1410", LIGHT="#5a4a40", CLOTH="#c8a040"),
                  "red": dict(FUR="#a8582e", MARK="#7a3a1e", LIGHT="#d89a6a", CLOTH="#2a6a5a"),
                  "piebald": dict(pattern="patches", MARK="#f2ece0")}),
    "kelpie_centaur": dict(
        build="slim", outfit=dict(top=["bandeau"], extras=["pearls"]),
        hair="wavy", tail=(2, 9, 2), kelp=True,
        pal=dict(SKIN=skin("pale"), HAIR="#1e3a2e", FUR="#1e2a26", LIGHT="#3a4a44", MARK="#141c1a", EYE="#5ac8a0", CLOTH="#2a4a3a", TRIM="#8ab8a8",
                 pattern="solid", extra=dict(kelp="#4a8a3a")),
        variants={"grey": dict(FUR="#8a9294", HAIR="#c8ccc8", LIGHT="#b8c0c0", MARK="#5a6264"),
                  "white": dict(FUR="#e8eeee", HAIR="#f4f8f8", LIGHT="#ffffff", MARK="#c0c8c8", EYE="#3a8ab8"),
                  "peat": dict(FUR="#4a3424", HAIR="#2a1e14", LIGHT="#6a5444", MARK="#2a1c12"),
                  "sea": dict(FUR="#2a5a64", HAIR="#2a7a7a", LIGHT="#5a8a94", MARK="#1a3a44")}),
    "unicorn_centaur": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], extras=["armlets", "pearls"], colors=dict(cloth2="#e8c050")),
        hair="flowing", tail=(2, 9, 2), uhorn=True,
        pal=dict(SKIN=skin("fair"), HAIR="#f8f4ff", FUR="#f6f4f8", LIGHT="#ffffff", MARK="#d8d0e0", EYE="#a86ad8", CLOTH="#c8a8e8", TRIM="#e8c050",
                 pattern="solid", extra=dict(horn="#f0c850")),
        variants={"silver": dict(FUR="#c0c4cc", HAIR="#e8ecf4", MARK="#8a90a0", CLOTH="#5a6a9a", extra=dict(horn="#e8e8f0")),
                  "rose": dict(FUR="#f8d8e4", HAIR="#f8b8d0", MARK="#e0a0b8", CLOTH="#e86a9a", extra=dict(horn="#f8e0a0")),
                  "midnight": dict(FUR="#2a2e5a", HAIR="#c8d0ff", MARK="#1a1e3a", LIGHT="#4a4e7a", EYE="#e8c050", CLOTH="#e8c050", extra=dict(horn="#c8d8ff")),
                  "rainbow": dict(HAIR="#f8a8c8", extra=dict(horn="#f0c850", rainbow=True))}),
    "pegasus_centaur": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], extras=["armlets", "pearls"], colors=dict(cloth2="#e8c050")),
        hair="flowing", tail=(2, 9, 2), wings=True,
        pal=dict(SKIN=skin("fair"), HAIR="#f4f0e8", FUR="#f2f0ec", LIGHT="#ffffff", MARK="#c8c4bc", EYE="#4a8ac8", CLOTH="#3a6aa8", TRIM="#e8c050",
                 pattern="solid", extra=dict(wing="#f6f4f0", wing_tip="#c8c4bc")),
        variants={"grey": dict(FUR="#b0b0b4", HAIR="#e0e0e4", MARK="#7a7a80", pattern="dapple", extra=dict(wing="#e8e8ec", wing_tip="#8a8a90")),
                  "palomino": dict(FUR="#e0bc70", HAIR="#f8f0d8", MARK="#c89a50", CLOTH="#8a2a3a", extra=dict(wing="#f8ecc8", wing_tip="#d8b060")),
                  "black": dict(FUR="#1e1c22", HAIR="#141218", MARK="#0e0c10", LIGHT="#3a3640", CLOTH="#c8a040", extra=dict(wing="#2a2830", wing_tip="#0e0c12")),
                  "dawn": dict(FUR="#f4dcd8", HAIR="#f8c8a0", MARK="#e0a890", CLOTH="#c84a6a", extra=dict(wing="#f8e0d0", wing_tip="#e8a060")),
                  # homage (2026-10-30): Pit: white chiton, a gold wreath's colour in her trim, dark hair
                  "angel_guard": dict(HAIR="#3a2a1e", FUR="#f8f8f4", LIGHT="#ffffff", MARK="#c8c0a8", EYE="#2a5ab0", CLOTH="#f8f8f4", TRIM="#e8c040")}),
}


def declare(sp):
    d = SPECIES[sp]
    parts = [("barrel", 7, 7, 15), ("withers", 5, 2.5, 5), ("uleg_f", 2.25, 6.5, 2.75), ("lleg_f", 1.75, 5.75, 1.75),
             ("uleg_b", 2.5, 6.5, 3.25), ("lleg_b", 1.75, 5.75, 1.75), ("hoof", 2.25, 1.25, 2.25), ("htail",) + d["tail"],
             ("armor_ref", 6, 8, 4)]
    if d.get("antlers"):
        parts += [("antler_a", .75, 5, .75), ("antler_b", .75, 2.5, .75), ("antler_c", .5, 2, .5)]
    if d.get("blossoms"):
        parts += [("blossom", 1.5, 1.5, 1.5), ("blossom_in", .75, .75, .75)]
    if d.get("fronds"):
        parts += [("frond_leaf", .25, 4, 2)]
    if d.get("blades"):
        parts += [("antler_blade", 2.5, 2, .25)]
    if d.get("bells"):
        parts += [("cbell", 1.25, 1.25, 1.25), ("bell_strap", 7.5, .5, .75)]
    if d.get("blanket"):
        parts += [("blanket", 7.75, 3.5, 6)]
    if d.get("horns"):
        parts += [("horn_a", 1, 3, 1), ("horn_b", .75, 2.5, .75)]
    if d.get("uhorn"):                                   # the unicorn's single spiral horn
        parts += [("uhorn_a", 1.5, 6, 1.5), ("uhorn_b", 1, 2.5, 1)]
    if d.get("fleece"):
        parts += [("fleece", 7.5, 4, 14)]
    if d.get("tusks"):
        parts += [("tusk_a", 1.25, 1.25, 5), ("tusk_b", 1, 1, 3.5), ("woolskirt", 7.75, 3, 14.5)]
    if d.get("humps"):
        parts += [("hump", 5, 3, 4.5), ("hump_top", 3.5, 1.5, 3)]
    if d.get("kelp"):
        parts += [("frond", .25, 6, 1.25)]
    if d.get("wings"):
        parts += P.wing_parts("feather", 1.4)
    if d.get("nemes"):                                   # the striped headcloth: a cap and two lappets over her shoulders
        parts += [("nemes_cap", 9, 3, 9), ("nemes_lappet", 2.5, 6, 1)]
    G.declare(parts, ears=d.get("ears", "horse"), hair=SPECIES[sp]["hair"],
              build=SPECIES[sp].get("build"), outfit=SPECIES[sp].get("outfit"))


def paint(sp, key):
    d = SPECIES[sp]
    pal = G.pal_from(d["pal"], d["variants"].get(key) if key else None)
    pt = H.Painter()
    G.paint_upper(pt, pal, top="wrap", top_pattern="check" if sp.startswith("horse") else "weave", outfit=d.get("outfit"), build=d.get("build"))
    coat = G.fur_fn(pal, light_faces=("down",), grain=0.10, seed=31)
    G.paint_ears(pt, pal, G.fur_fn(pal, pattern="solid"))
    if pal.extra.get("saddle"):                          # the Malayan tapir's white saddle over a black coat
        pt("barrel", "all", lambda f, x, y, w, h: coat(f, x, y, w, h) if f == "north" else pick(pal.LIGHT, 0.55 + (noise(x, y, 31) - 0.5) * 0.1, x, y))
    else:
        pt("barrel", "all", coat)
    pt("withers", "all", lambda f, x, y, w, h: H.skin_fn(pal, 0.66)(f, x, y, w, h) if f in ("north", "up") and y < h * 0.5 else coat(f, x, y, w, h))
    pt("uleg_f", "all", coat)
    pt("uleg_b", "all", coat)
    socks = pal.extra.get("socks")
    lower = lambda f, x, y, w, h: (pick(pal.LIGHT if socks else pal.MARK, 0.7, x, y) if y > h * 0.6 and socks else coat(f, x, y, w, h))
    pt("lleg_f", "all", lower)
    pt("lleg_b", "all", lower)
    pt("hoof", "all", lambda f, x, y, w, h: pick(shades("#3a302a"), 0.45 + 0.2 * (y == 0) + (noise(x, y, 3) - 0.5) * 0.1, x, y))
    bloom, bloom_in = shades(pal.extra.get("bloom", "#f8a8c8")), shades(pal.extra.get("bloom_in", "#f8e070"))
    pt("blossom", "all", lambda f, x, y, w, h: pick(bloom, 0.55 + 0.25 * ((x + y) % 2), x, y))
    pt("blossom_in", "all", lambda f, x, y, w, h: pick(bloom_in, 0.7, x, y))
    bellc = shades(pal.extra.get("bell", "#d8dce8"))
    pt("cbell", "all", lambda f, x, y, w, h: pick(bellc, 0.5 + 0.3 * (f == "up") - 0.2 * (y == h - 1), x, y))
    pt("bell_strap", "all", lambda f, x, y, w, h: pick(pal.CLOTH, 0.45, x, y))
    b1, b2, b3 = (shades(pal.extra.get(k, c)) for k, c in (("blanket", "#2a3a8a"), ("blanket2", "#e8e8f0"), ("blanket3", "#e8b040")))

    def weave(f, x, y, w, h):                            # a woven blanket: bands of stepped diamonds, a fringe at the bottom
        if f == "down":
            return CLEAR
        if f in ("east", "west") and y == h - 1:
            return pick(b2, 0.6, x, y) if x % 2 else CLEAR
        band = (y if f in ("east", "west", "north", "south") else x) % 6
        dx = abs((x % 6) - 2.5)
        if band in (0, 5):
            return pick(b3, 0.55, x, y)
        return pick(b2 if dx + abs(band - 2.5) < 2.2 else b1, 0.5 + 0.1 * ((x + y) % 2), x, y)
    pt("blanket", "all", weave)
    steel = shades(pal.extra.get("blade", "#c8d0dc"))
    frond, fedge = shades(pal.extra.get("frond", "#8aa8f0")), shades(pal.extra.get("frond_edge", "#f0f4ff"))

    def leaf(f, x, y, w, h):                             # a feather-leaf: tapering to its tip, a pale rib and pale barbs
        half = (w if f in ("north", "south") else 2) / 2
        if f in ("east", "west"):
            if abs(x + 0.5 - w / 2) > (w / 2) * (1 - y / h * 0.0) * (0.3 + 0.7 * (1 - y / h)):
                return CLEAR
            return pick(fedge if abs(x + 0.5 - w / 2) < 0.6 or (x + y) % 3 == 0 else frond, 0.5 + 0.15 * (y % 2), x, y)
        return pick(frond, 0.45, x, y)
    pt("frond_leaf", "all", leaf)
    pt("antler_blade", "all", lambda f, x, y, w, h: pick(steel, 0.55 + 0.3 * (y == 0), x, y) if y < h - (abs(x + 0.5 - w / 2) * 1.2) else CLEAR)
    kelp = shades(pal.extra.get("kelp", "#4a8a3a"))
    pt("htail", "all", lambda f, x, y, w, h: pick(kelp, 0.45 + (noise(x, y, 6) - 0.5) * 0.3, x, y) if d.get("kelp") and (x + y // 2) % 3 == 0
       else pick(pal.HAIR, 0.45 + 0.25 * ((x + y // 3) % 3 == 0) + (noise(x, y, 5) - 0.5) * 0.15, x, y)
       if sp != "zebra_centaur" or y > h * 0.6 else pick(pal.FUR, 0.5, x, y))
    pt("hump_top", "all", lambda f, x, y, w, h: pick(pal.FUR, 0.62 + (noise(x, y, 13) - 0.5) * 0.12, x, y))
    pt("hump", "all", lambda f, x, y, w, h: pick(pal.FUR, 0.55 + 0.12 * (f == "up") + (noise(x, y, 12) - 0.5) * 0.12, x, y))
    pt("frond", "all", lambda f, x, y, w, h: pick(kelp, 0.5 + 0.15 * (y % 2) - 0.2 * (y == h - 1), x, y))
    if d.get("wings"):
        P.paint_feather_wings(pt, pal.extra.get("wing", "#f6f4f0"), pal.extra.get("wing_tip", "#c8c4bc"))
    ant = shades(pal.extra.get("antler", "#c8b090"))
    ant2 = shades(pal.extra.get("antler2", pal.extra.get("antler", "#c8b090")))
    for p in ("antler_a", "antler_b", "antler_c"):                # the aurorantler's shade from one end to the other
        pt(p, "all", lambda f, x, y, w, h: pick(ant2 if pal.extra.get("antler2") and y < h * 0.4 else ant,
                                                0.5 + (noise(x, y, 7) - 0.5) * 0.2 + (0.2 if y == 0 else 0), x, y))
    nm, nb = shades(pal.extra.get("nemes", "#e8c050")), shades(pal.extra.get("nemes_band", "#2a4a9a"))
    stripes = lambda f, x, y, w, h: pick(nb if (y // 2) % 2 else nm, 0.55, x, y)
    pt("nemes_cap", "all", stripes)
    pt("nemes_lappet", "all", stripes)
    tusk = shades(pal.extra.get("tusk", "#f0e6cc"))
    for p in ("tusk_a", "tusk_b"):
        pt(p, "all", lambda f, x, y, w, h: pick(tusk, 0.55 + 0.15 * (f == "up") - 0.1 * (f == "down"), x, y))
    pt("woolskirt", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") or (y == h - 1 and x % 2) else
       pick(pal.FUR, 0.5 + 0.2 * (x % 2) + (noise(x, y, 19) - 0.5) * 0.15, x, y))
    horn = shades(pal.extra.get("horn", "#2a2826"))
    for p in ("horn_a", "horn_b"):
        pt(p, "all", lambda f, x, y, w, h: pick(horn, 0.45 + 0.25 * (y % 3 == 0), x, y))
    pt("fleece", "all", lambda f, x, y, w, h: pick(pal.FUR, 0.62 + 0.18 * (noise(x // 2, y // 2, 9) > 0.5) + (noise(x, y, 8) - 0.5) * 0.1, x, y))
    if pal.extra.get("rainbow") and d.get("fleece"):    # Urcuchillay's fleece of many colours: soft rainbow bands along her back
        import colorsys

        def rainbow_fleece(f, x, y, w, h):
            r_, g_, b_ = colorsys.hsv_to_rgb(((x + y * 0.5) / 18.0 + noise(x // 2, y // 2, 9) * 0.05) % 1.0, 0.38, 0.97)
            k = 0.9 + 0.1 * (noise(x // 2, y // 2, 9) > 0.5)
            return (int(r_ * 255 * k), int(g_ * 255 * k), int(b_ * 255 * k), 255)
        pt("fleece", "all", rainbow_fleece)
    gold = shades(pal.extra.get("horn", "#f0c850"))
    for p in ("uhorn_a", "uhorn_b"):                      # a spiral: a stripe winding round the horn
        pt(p, "all", lambda f, x, y, w, h: pick(gold, 0.45 + 0.35 * ((x + y) % 4 == 0), x, y))
    if pal.extra.get("rainbow"):                          # a rainbow mane and tail
        import colorsys
        hairy = [p for p in H.PARTS if any(t in p for t in ("hair", "strand", "bang", "frame", "htail", "lock", "pony", "wave"))]

        def bow_for(p):                                   # recolour only what the hair already covers, so the cap's open face stays open
            def bow(f, x, y, w, h):
                x0, y0 = H.RECT[(p, f)][:2]
                if pt.T[y0 + y][x0 + x][3] == 0:
                    return None
                r_, g_, b_ = colorsys.hsv_to_rgb((y / max(1, h) * 0.85 + x * 0.01) % 1.0, 0.45, 0.98)
                return (int(r_ * 255), int(g_ * 255), int(b_ * 255), 255)
            return bow
        for p in hairy:
            pt(p, "all", bow_for(p))
    pt("armor_ref", "all", lambda f, x, y, w, h: G.CLEAR)
    return pt.T


def build(sp):
    d = SPECIES[sp]
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    g = G.upper(r, 19.5, dict(hair=d["hair"], bangs=d.get("bangs"), build=d.get("build"), outfit=d.get("outfit"), ears=d.get("ears", "horse"),
                              skirt=False), z0=-2.25)
    G.taur(r, None, d)
    hy, z = g.hy, g.z
    r.add("armor_torso", "body", [0, 19.5, z], [cube("armor_ref", [-3, 19.25, z - 2])])
    if d.get("antlers"):
        cs = []
        for sg in (-1, 1):
            x = sg * 2.6
            base = [x - 0.375, hy + 8.4, -0.4 + z]
            cs.append(G.rot_cube("antler_a", base, [x, hy + 8.4, z], -20, 0, 22 * sg))
            cs.append(G.rot_cube("antler_b", [x + sg * 1.8 - 0.375, hy + 11.0, -1.4 + z], [x + sg * 1.8, hy + 11.0, -1.0 + z], -45, 0, -10 * sg))
            cs.append(G.rot_cube("antler_c", [x + sg * 2.6 - 0.25, hy + 12.4, 0.8 + z], [x + sg * 2.6, hy + 12.4, 1.0 + z], 10, 0, 30 * sg))
            cs.append(G.rot_cube("antler_b", [x + sg * 0.6 - 0.375, hy + 9.6, -1.9 + z], [x + sg * 0.6, hy + 9.6, -1.6 + z], -60, 0, 0))
        if d.get("blades"):                              # a halberd's blade on the top tine of each antler
            for sg in (-1, 1):
                cs.append(G.rot_cube("antler_blade", [sg * 5.2 - 1.25, hy + 13.4, 0.9 + z], [sg * 5.2, hy + 13.4, 1.0 + z], 0, 0, 25 * sg))
        if d.get("fronds"):                              # feathery fronds sprouting from the tines, fanned outwards
            for sg in (-1, 1):
                for bx, by, bz, tilt in ((2.6 + 1.8, 11.0, -1.4, 25), (2.6 + 2.6, 12.6, 1.0, 40), (2.6 + 0.6, 9.8, -1.8, 10)):
                    cs.append(G.rot_cube("frond_leaf", [sg * bx - 0.125, hy + by, bz - 1 + z], [sg * bx, hy + by, bz + z], 0, 0, -tilt * sg))
        if d.get("blossoms"):                            # peach blossoms caught in the tines
            for sg in (-1, 1):
                for bx, by, bz in ((2.6 + 1.8, 11.4, -1.6), (2.6 + 2.6, 13.6, 1.2), (2.6 + 0.6, 10.6, -2.4), (2.6 + 1.2, 9.4, 0.2)):
                    cs.append(cube("blossom", [sg * bx - 0.75, hy + by - 0.75, bz - 0.75 + z]))
                    cs.append(cube("blossom_in", [sg * bx - 0.375, hy + by - 0.375, bz - 1.0 + z]))
        r.add("antlers", "head", [0, hy + 8.4, z], cs)
    if d.get("horns"):
        cs = []
        for sg in (-1, 1):
            x = sg * 1.6
            cs.append(G.rot_cube("horn_a", [x - 0.5, hy + 8.4, -0.8 + z], [x, hy + 8.4, -0.3 + z], -28, 0, 6 * sg))
            cs.append(G.rot_cube("horn_b", [x - 0.375, hy + 10.9, 0.6 + z], [x, hy + 10.9, 0.9 + z], -62, 0, 6 * sg))
        r.add("horns", "head", [0, hy + 8.4, z], cs)
    if d.get("uhorn"):
        r.add("uhorn", "head", [0, hy + 8, -3 + z], [G.rot_cube("uhorn_a", [-0.75, hy + 8, -3.75 + z], [0, hy + 8, -3 + z], -25, 0, 0),
                                                    G.rot_cube("uhorn_b", [-0.5, hy + 13.9, -3.5 + z], [0, hy + 8, -3 + z], -25, 0, 0)])
    if d.get("fleece"):
        r.put("barrel", cube("fleece", [-3.75, 13.25, -4.0]))
    if d.get("blanket"):                                # the saddle blanket over the middle of her back, hanging down her flanks
        r.put("barrel", cube("blanket", [-3.875, 14.0, 0.0], inflate=0.35))
    if d.get("bells"):                                  # a strap of bells across her chest, more bells hung on her flanks
        r.put("barrel", cube("bell_strap", [-3.75, 15.5, -4.4]),
              *[cube("cbell", [-3.0 + 2.5 * i, 14.2, -4.9]) for i in range(3)],
              *[cube("cbell", [sg * 3.75 - 0.625, 12.0, zz]) for sg in (-1, 1) for zz in (1.0, 6.0)])
    if d.get("nemes"):
        hy, z = g.hy, g.z
        r.add("nemes", "head", [0, hy + 4, z], [cube("nemes_cap", [-4.5, hy + 6.0, -4.3 + z], inflate=0.6),
                                                cube("nemes_lappet", [-5.3, hy - 2.5, -2.0 + z]), cube("nemes_lappet", [2.8, hy - 2.5, -2.0 + z])])
    if d.get("tusks"):                                  # long hair hanging round the barrel; tusks sweeping forward and up
        r.put("barrel", cube("woolskirt", [-3.875, 7.5, -4.25]))
        cs = []
        for sg in (-1, 1):
            x = sg * 2.2
            cs.append(G.rot_cube("tusk_a", [x - 0.625, 11.0, -9.0], [x, 11.6, -4.0], 28, 0, -8 * sg))
            cs.append(G.rot_cube("tusk_b", [x - 0.5 + sg * 0.5, 8.8, -11.9], [x + sg * 0.5, 9.3, -8.4], -60, 0, -8 * sg))
        r.add("tusks", "barrel", [0, 11.6, -4.0], cs)
    if d.get("humps"):                                  # the Bactrian's two humps, the dromedary's one, behind the rider's seat
        for zc in ((2.25, 7.0) if d["humps"] == 2 else (5.0,)):
            r.put("barrel", cube("hump", [-2.5, 18.75, zc - 2.25]), cube("hump_top", [-1.75, 21.5, zc - 1.5]))
    if d.get("kelp"):                                   # the kelpie's weed, hanging off her flanks
        r.put("barrel", *[cube("frond", [sg * 3.6 - 0.125, 11.0, zz]) for sg in (-1, 1) for zz in (0.5, 5.5)])
    if d.get("wings"):                                  # the pegasus' wings rise from her withers, at the front of the horse body
        P.wings(r, "feather", "barrel", y=19.0, z=-1.0, root_x=3.0)
    return r.model(sp)


def anims(an):
    G.taur_anims(an)
    import girl_family as F
    names = ["wing_r", "wing_l"]                         # only the pegasus has them; the others ignore these tracks
    for key in ("idle", "walk", "sit", "lie", "graze", "rear", "kick"):
        F._inject(an, key, P.folded(names, fold=82, droop=-22))
    F._inject(an, "run", P.flap(names, 0.6, 35, 25))


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.3, -45, 8), ("run", 0.15, -70, 5), ("sit", 0, -35, 10), ("rear", 0.8, -60, 5)]
FACE = [("idle", 0, 0, 0, 31.5), ("idle", 2.75, 0, 0, 31.5), ("rear", 0.8, 0, 0, 31.5), ("idle", 0, -30, 8, 31.5)]

if __name__ == "__main__":
    G.run_family("centaur", SPECIES, declare, paint, build, anims, VIEWS, FACE)
    import girl_family as F
    want = [a for a in sys.argv[1:] if not a.startswith("--")]
    for sp, d in SPECIES.items():                        # glow masks: the aurorantler's antlers
        if d.get("glow") and (not want or sp in want):
            declare(sp)
            for k in [None] + list(d["variants"]):
                F.write_glowmask(sp + ("_" + k if k else ""), paint(sp, k), d["glow"])
