#!/usr/bin/env python3
"""Lamia (the serpent family, see lamia_field_guide.html): python, king cobra, sea snake, rattlesnake, boa.

    python tools/gen_lamia.py [species ...] [--previews]

Shared girl_lib upper body on a serpent tail of eight segments (seg_1 down from the waist, seg_2..seg_8 along the
ground). Species extras: the cobra's hood (bone `hood`), the rattlesnake's rattle, the sea snake's paddle tail.
Jewellery top and arm bands; slit pupils. Five colours per species.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, shades, CLEAR  # noqa: E402

SEGS = [("seg_1", 4.5, 10.5, 4), ("seg_2", 4.25, 4, 5.5), ("seg_3", 4, 3.75, 5.5), ("seg_4", 3.5, 3.25, 5),
        ("seg_5", 3, 2.75, 5), ("seg_6", 2.5, 2.25, 4.5), ("seg_7", 2, 1.75, 4), ("seg_8", 1.25, 1.25, 3.5)]

SPECIES = {
    # thirty-second wave: the anaconda, the heaviest snake there is and proud of it: a full figure on a very thick olive tail with
    # black saddles, long dark waves, gold hoops of armlets. Her own woman, not anyone's portrait.
    "anaconda_lamia": dict(
        build="curvy", outfit=dict(top=["halter"], extras=["armlets", "pearls"], colors=dict(cloth2="#e8c050")),
        thick=1.3, hair="wavy", bangs="parted",
        pal=dict(SKIN=skin("tan"), HAIR="#1e1a14", FUR="#6a7a3a", LIGHT="#e8e0a8", MARK="#14140e", EYE="#d8a030", CLOTH="#e8c050", TRIM="#1e1a14",
                 pattern="blotches", facemark="scales"),
        variants={"yellow": dict(FUR="#b8a83a", MARK="#2a2410", LIGHT="#f8f0c0", HAIR="#3a2e14"),
                  "dark": dict(FUR="#3a4428", MARK="#0e100a", LIGHT="#b8b890", HAIR="#141410"),
                  "green": dict(FUR="#3a8a4a", MARK="#10301a", LIGHT="#d8f0b0", HAIR="#1e3a22"),
                  "brown": dict(FUR="#7a5a34", MARK="#2a1a0e", LIGHT="#e8d8b0", HAIR="#3a2414")}),
    # thirty-second wave: the sea serpent of the old charts: long, barred blue and white, a fin down her back, lives on the open sea
    "sea_serpent_lamia": dict(
        build="athletic", outfit=dict(top=["shells" if False else "bandeau"], extras=["armlets"]),
        thick=1.35, hair="flowing", bangs="wispy", paddle=True,
        pal=dict(SKIN="#bcd8dc", HAIR="#1e4a6a", FUR="#2a6a8a", LIGHT="#e0f0f0", MARK="#123044", EYE="#e8c040", CLOTH="#123044", TRIM="#e0f0f0",
                 pattern="bands", facemark="scales"),
        variants={"green": dict(FUR="#2a7a5a", HAIR="#1e4a3a", MARK="#10301e", CLOTH="#10301e"),
                  "black": dict(FUR="#1e2228", HAIR="#141418", MARK="#0a0a0e", LIGHT="#a8b0b8", EYE="#e85a3a"),
                  "red": dict(FUR="#a83a34", HAIR="#6a1e1e", MARK="#3a0e10", LIGHT="#f0d8c8"),
                  "white": dict(FUR="#d8e4e8", HAIR="#a8c0c8", MARK="#5a7a88", LIGHT="#ffffff", EYE="#3a6a9a")}),
    # twenty-third wave: the titanoboa, the greatest snake that ever lived: huge, olive-brown with dark blotches, a cream belly
    "titanoboa_lamia": dict(
        build="strong", outfit=dict(top=["sports"], extras=["armlets"], colors=dict(cloth2="#5a4a2a")),
        thick=1.45, hair="flowing", bangs="parted",
        pal=dict(SKIN=skin("fair"), HAIR="#4a3a1e", FUR="#9a8a4a", LIGHT="#f0e4b8", MARK="#3a2e14", EYE="#c89a2a", CLOTH="#5a4a2a", TRIM="#e8d8a0",
                 pattern="blotches", facemark="scales"),
        variants={"green": dict(HAIR="#2a4a1e", FUR="#6a8a3a", MARK="#1e3010", CLOTH="#3a4a1e", EYE="#d8b030"),
                  "black": dict(HAIR="#1a1814", FUR="#3a3630", MARK="#0e0c0a", LIGHT="#d8ccb0", CLOTH="#2a2620", EYE="#e85a2a"),
                  "red": dict(HAIR="#5a1e10", FUR="#a8582a", MARK="#4a1a0a", CLOTH="#4a2a14", EYE="#e8c040"),
                  "blue": dict(HAIR="#1e3a5a", FUR="#5a7a9a", MARK="#14243a", LIGHT="#e0e8f0", CLOTH="#1e3a5a", EYE="#e8c040")}),
    # twenty-first wave (folklore): the serpopard of ancient Egyptian palettes, a long-necked serpent with a leopard's spots, ears
    # twenty-second wave: the sandworm, blue and segmented with a cream belly and a scaly hood over her head
    "sandworm_lamia": dict(
        build="curvy", outfit=dict(top=["bandeau"], extras=["armlets"], colors=dict(cloth2="#e8c050")),
        thick=1.2, hair="bob", bangs="blunt", whood=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a4a8a", FUR="#4a7ac8", LIGHT="#f4e4c4", MARK="#2a4a8a", EYE="#2a4a9a", CLOTH="#2a4a8a", TRIM="#f4e4c4",
                 pattern="bands"),
        variants={"sand": dict(HAIR="#8a6a3a", FUR="#d8b878", LIGHT="#f8f0dc", MARK="#9a7440", CLOTH="#9a7440", EYE="#3a6a8a"),
                  "red": dict(HAIR="#7a2a1a", FUR="#c8603a", LIGHT="#f8e0c8", MARK="#7a2a1a", CLOTH="#7a2a1a", EYE="#e8c040"),
                  "grey": dict(HAIR="#4a4e56", FUR="#8a8e96", LIGHT="#ecece8", MARK="#4a4e56", CLOTH="#4a4e56", EYE="#c83a3a"),
                  "black": dict(HAIR="#141218", FUR="#2a2830", LIGHT="#c8b890", MARK="#0a080c", CLOTH="#5a1e24", EYE="#e8c040")}),
    # twenty-second wave (folklore): Xiangliu, the nine-headed serpent of Chinese myth: a teal serpent girl with six little heads of
    # her own round her head on short necks, all of them talking at once
    "xiangliu_lamia": dict(
        build="slim", outfit=dict(top=["kimono"], colors=dict(kimono="#2a6a6a", kimono_trim="#e8d8a8", obi="#a8402a")),
        thick=1.1, hair="hime", bangs="blunt", xheads=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a5a5a", FUR="#3a8a86", LIGHT="#c8e8d8", MARK="#1e4a4a", EYE="#3a6a8a", CLOTH="#2a6a6a", TRIM="#e8d8a8",
                 pattern="solid"),
        variants={"green": dict(HAIR="#2a4a22", FUR="#4a7a3a", LIGHT="#d8e8c0", MARK="#1e3a14", EYE="#c8a040"),
                  "black": dict(HAIR="#141418", FUR="#2a2a30", LIGHT="#8a8a90", MARK="#0a0a0e", EYE="#c83a3a"),
                  "gold": dict(HAIR="#5a3a14", FUR="#c8a040", LIGHT="#f8ecc0", MARK="#7a5a1e", EYE="#3a2a1a"),
                  "red": dict(HAIR="#3a1410", FUR="#a8402a", LIGHT="#f0d0b8", MARK="#5a1e14", EYE="#e8c040")}),
    # and face: gold with dark rosettes, a lapis and gold collar
    "serpopard_lamia": dict(
        build="slim", outfit=dict(top=["bandeau"], extras=["usekh", "armlets"], colors=dict(cloth2="#e8c050")),
        thick=1.05, hair="bob", bangs="blunt", ears="cat",
        pal=dict(SKIN=skin("fair"), HAIR="#2a1e14", FUR="#d8a840", LIGHT="#f8ecc8", MARK="#3a2410", EYE="#c8d040", CLOTH="#2a4ab0", TRIM="#e8c050",
                 INNER="#e8a0a0", pattern="rosettes", facemark="whisker"),
        variants={"blue": dict(FUR="#4a7ad8", LIGHT="#e0ecfc", MARK="#14204a", HAIR="#14204a", EYE="#f8d040", CLOTH="#e8c050", TRIM="#2a4ab0"),
                  "snow": dict(FUR="#e8e8ec", LIGHT="#ffffff", MARK="#5a5a64", HAIR="#3a3a44", EYE="#5ab8e8"),
                  "black": dict(FUR="#2a2830", LIGHT="#5a5860", MARK="#0e0c10", HAIR="#0e0c10", EYE="#f8d040", CLOTH="#a81a2a"),
                  "sand": dict(FUR="#e0c890", LIGHT="#fcf4e0", MARK="#8a6a3a", HAIR="#5a3a1e", EYE="#3a8a3a", CLOTH="#2a7a6a")}),
    # nineteenth wave: the turquoise serpent, royal blue with a turquoise belly, a feathered crest, gold and turquoise jewellery
    "turquoise_serpent_lamia": dict(
        build="curvy", outfit=dict(top=["bandeau"], extras=["usekh", "armlets", "bracers"], colors=dict(cloth2="#e8c050")),
        thick=1.1, hair="mane", bangs="jagged", rearing=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a9aa8", FUR="#2a4ab0", LIGHT="#5ad8c8", MARK="#1a2a6a", EYE="#e8c040", CLOTH="#e8c050", TRIM="#3ad8c8",
                 pattern="solid", facemark="scales"),
        variants={"jade": dict(HAIR="#5a3a8a", FUR="#2a7a4a", LIGHT="#a8e8a0", MARK="#14402a", EYE="#e8c040"),
                  "gold": dict(HAIR="#2a9aa8", FUR="#c8962a", LIGHT="#fff0b0", MARK="#6a4a10", EYE="#3ad8c8", CLOTH="#3ad8c8", TRIM="#e8c050"),
                  "coral": dict(HAIR="#3ad8c8", FUR="#d84a3a", LIGHT="#f8c8a0", MARK="#6a1a14", EYE="#3ad8c8"),
                  "obsidian": dict(HAIR="#7a3ab8", FUR="#1e1e28", LIGHT="#5a5a7a", MARK="#0a0a10", EYE="#e83a3a")}),
    # nineteenth wave: the hoop snake of the tall tales, sky blue with a cream belly
    "hoop_snake_lamia": dict(
        build="petite", outfit=dict(top=["halter"], extras=["armlets"], colors=dict(cloth2="#4a8ac8")),
        thick=0.9, hair="bob", bangs="blunt",
        pal=dict(SKIN=skin("fair"), HAIR="#7ab8e8", FUR="#8ac8f0", LIGHT="#f4ecd8", MARK="#4a8ac8", EYE="#2a5aa8", CLOTH="#4a8ac8", TRIM="#f4ecd8",
                 pattern="solid"),
        variants={"green": dict(HAIR="#6ab85a", FUR="#7ac86a", MARK="#3a7a3a", EYE="#3a5a2a", CLOTH="#3a7a3a"),
                  "red": dict(HAIR="#d86a5a", FUR="#e87a6a", MARK="#9a3a2e", EYE="#5a1a14", CLOTH="#9a3a2e"),
                  "black": dict(HAIR="#3a3a44", FUR="#3a3a46", LIGHT="#c8c0b0", MARK="#14141a", EYE="#e8c040", CLOTH="#2a2a34"),
                  "yellow": dict(HAIR="#e8c050", FUR="#f0d060", MARK="#a8862a", EYE="#5a3a10", CLOTH="#a8862a")}),
    # eighteenth wave: the urchskin, a teal sea serpent with a cream belly, purple urchin spines along her back and a spiny crest
    "urchskin_lamia": dict(
        build="slim", outfit=dict(top=["shells"], extras=["pearls", "armlets"], colors=dict(shell="#c8a0e0")),
        thick=1.0, hair="mane", bangs="jagged", spines=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a6a8a", FUR="#2a7a8a", LIGHT="#e8dcb0", MARK="#1a4a6a", EYE="#d83a8a", CLOTH="#3a8a9a", TRIM="#c8a0e0",
                 pattern="speckle", facemark="scales", extra=dict(spine="#6a2a7a", spine_tip="#e878c8")),
        variants={"black": dict(HAIR="#1e1e2a", FUR="#24242e", LIGHT="#8a8a9a", MARK="#0e0e14", EYE="#e8c040", CLOTH="#2a2a3a",
                                extra=dict(spine="#141418", spine_tip="#e8c040")),
                  "green": dict(HAIR="#2a6a3a", FUR="#3a8a4a", LIGHT="#e8f0c0", MARK="#1e4a2a", EYE="#e8703a", CLOTH="#3a7a4a",
                                extra=dict(spine="#5a2a1e", spine_tip="#f0a03a")),
                  "red": dict(HAIR="#8a2a2a", FUR="#a83a32", LIGHT="#f8dcc0", MARK="#5a1a14", EYE="#f8d040", CLOTH="#8a2a2a",
                              extra=dict(spine="#2a1418", spine_tip="#f8d040")),
                  "yellow": dict(HAIR="#c8962a", FUR="#d8a83a", LIGHT="#fff0c8", MARK="#8a5a14", EYE="#3a8ad8", CLOTH="#c8902a",
                                 extra=dict(spine="#2a3a6a", spine_tip="#6ab0f0"))}),
    # twelfth wave: the tsuchinoko, a short fat snake body (thick, stubby) that curls up and rolls ("roll")
    "tsuchinoko_lamia": dict(
        build="petite", outfit=dict(top=["sweater"], extras=["scarf"], colors=dict(scarf="#c83a3a")),
        thick=1.55, stub=0.55, hair="bob", bangs="blunt",
        pal=dict(SKIN=skin("fair"), HAIR="#5a3e24", FUR="#7a5a34", LIGHT="#e8d4a8", MARK="#4a3018", EYE="#c8902a", CLOTH="#e8dcc0", TRIM="#c83a3a",
                 pattern="diamonds", facemark=None),
        variants={"grey": dict(HAIR="#5a5a5e", FUR="#7a7a7e", LIGHT="#d8d8d8", MARK="#3a3a3e", CLOTH="#c8d8e8"),
                  "green": dict(HAIR="#3a5a2a", FUR="#5a7a3a", LIGHT="#d8e8a8", MARK="#2a4a1a", CLOTH="#f0e0b0"),
                  "gold": dict(HAIR="#a8782a", FUR="#d8a840", LIGHT="#f8e8b0", MARK="#8a5a1a", CLOTH="#3a2a4a"),
                  "black": dict(HAIR="#1e1c1e", FUR="#2a282a", LIGHT="#8a8488", MARK="#121012", EYE="#e8c040", CLOTH="#a83a3a")}),
    "python_lamia": dict(
        build="curvy", outfit=dict(top=["halter"], bottom="sarong", extras=["armlets"]),
        thick=1.15, hair="wavy",
        pal=dict(SKIN=skin("tan"), HAIR="#3a2a1e", FUR="#5a3e24", LIGHT="#ece0c4", MARK="#c8a060", EYE="#c89030", CLOTH="#2e5a3a", TRIM="#d8b048",
                 pattern="blotches", facemark="scales"),
        variants={"banana": dict(FUR="#a890b0", MARK="#f0d050", HAIR="#e0c058", CLOTH="#5a3a6a"),
                  "piebald": dict(pattern="patches", MARK="#f4f2ee", HAIR="#2a1e16"),
                  "albino": dict(FUR="#f4ecd8", MARK="#f0d060", EYE="#d83a3a", HAIR="#f2ead8", PUPIL="#7a1010", CLOTH="#c86a6a"),
                  "reticulated": dict(FUR="#8a7a4a", MARK="#1e1a12", pattern="net", HAIR="#4a3a22")}),
    "medusa_lamia": dict(                               # ninth wave: living snakes for hair (bone snakes), a blindfold (shown once befriended)
        build="slim", outfit=dict(top=["halter"], bottom="sarong", extras=["armlets", "pearls"], colors=dict(cloth2="#f0ece0")),
        thick=1.05, hair="bob", bangs="none", medusa=True,
        pal=dict(SKIN=skin("pale"), HAIR="#3a7a3a", FUR="#4a7a4a", LIGHT="#e0ecc8", MARK="#2a4a2a", EYE="#e8d040", CLOTH="#f4f0e6", TRIM="#d8b048",
                 pattern="bands", facemark="scales", extra=dict(snake="#4a9a3a", snake_band="#2a5a24", blindfold="#5a2a6a")),
        variants={"bronze": dict(HAIR="#8a5a2a", FUR="#8a6a3a", MARK="#5a3a1a", EYE="#4ab8a8",
                                 extra=dict(snake="#b0783a", snake_band="#6a4018", blindfold="#2a3a5a")),
                  "onyx": dict(HAIR="#1e1e22", FUR="#2a2a30", MARK="#141418", LIGHT="#8a8a90", EYE="#c83a3a",
                               extra=dict(snake="#2a2a30", snake_band="#5a5a64", blindfold="#a8a8b0")),
                  "marble": dict(SKIN="#e8e4dc", HAIR="#d8d4cc", FUR="#c8c4bc", MARK="#9a968e", LIGHT="#f8f6f2", EYE="#8a8a8e",
                                 extra=dict(snake="#d8d4cc", snake_band="#9a968e", blindfold="#3a3a40")),
                  "coral": dict(HAIR="#c83a2a", FUR="#c84a3a", MARK="#1e1a18", LIGHT="#f8e0c0", EYE="#f8d040",
                                extra=dict(snake="#d8402a", snake_band="#1e1a18", snake_band2="#f0d040", blindfold="#1e1a18"))}),
    # fifteenth wave: the caraviper, a blue cobra with a wide hood, banded and patterned with gold glyphs
    "caraviper_lamia": dict(
        build="slim", outfit=dict(top=["bandeau"], bottom="loincloth", extras=["usekh", "armlets"], colors=dict(cloth2="#e8c050")),
        thick=1.0, hair="bob", hood=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e2a6a", FUR="#2a4ab0", LIGHT="#e8dcb0", MARK="#e8b840", EYE="#f8c030", CLOTH="#1e2a6a", TRIM="#e8c050",
                 pattern="bands", facemark="scales"),
        variants={"sand": dict(FUR="#c8a46a", MARK="#5a3a1a", LIGHT="#f4e8c8", HAIR="#6a4a22", CLOTH="#2a5a7a"),
                  "black": dict(FUR="#1e1e22", MARK="#e8b840", LIGHT="#d8d0b8", HAIR="#141416", CLOTH="#3a2a5a"),
                  "red": dict(FUR="#a82a2a", MARK="#f0d040", LIGHT="#f4d8c0", HAIR="#5a1410", CLOTH="#1e1a1a"),
                  "green": dict(FUR="#2a7a4a", MARK="#e8c050", LIGHT="#d8f0c8", HAIR="#14402a", CLOTH="#6a2a2a")}),
    "cobra_lamia": dict(
        build="slim", outfit=dict(top=["bandeau"], bottom="loincloth", extras=["usekh", "armlets"], colors=dict(cloth2="#f0ead8")),
        thick=1.0, hair="bob", hood=True,
        pal=dict(SKIN=skin("olive"), HAIR="#2a2a1c", FUR="#6a6438", LIGHT="#efe8c8", MARK="#d8d0a0", EYE="#c8a030", CLOTH="#6a2a2a", TRIM="#e0c050",
                 pattern="bands", facemark="scales"),
        variants={"black": dict(FUR="#1e1e20", MARK="#3a3a3c", LIGHT="#d8d0b8", HAIR="#141416", CLOTH="#3a3a5a"),
                  "banded": dict(FUR="#18181a", MARK="#f0d040", pattern="bars", HAIR="#1a1a1c"),
                  "golden": dict(FUR="#b08848", MARK="#5a4020", HAIR="#6a4a22", CLOTH="#2a4a6a"),
                  "spectacled": dict(FUR="#b09878", MARK="#3a2a1c", HAIR="#4a3a2a", CLOTH="#4a6a3a")}),
    "sea_snake_lamia": dict(
        build="petite", outfit=dict(top=["shells"], extras=["pearls", "belt"], colors=dict(shell="#cfe4ec")),
        thick=0.9, hair="flowing", paddle=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e2a3a", FUR="#b0c4d4", LIGHT="#e4ecf0", MARK="#121418", EYE="#d8c040", CLOTH="#2a6a8a", TRIM="#e8e0c8",
                 pattern="bars", facemark="scales"),
        variants={"yellow": dict(FUR="#1a1a1e", LIGHT="#f0d040", MARK="#f0d040", pattern="speckle", HAIR="#1a1a22", CLOTH="#c8a020"),
                  "olive": dict(FUR="#7a7448", MARK="#4a4430", pattern="smallspots", HAIR="#3a3a22"),
                  "turtlehead": dict(FUR="#4a4a50", MARK="#2a2a2e", HAIR="#2a2a30", CLOTH="#3a3a4a"),
                  "pale": dict(FUR="#d8d8d4", MARK="#5a5a58", pattern_scale=0.6, HAIR="#8a8a90")}),
    "rattlesnake_lamia": dict(
        build="athletic", outfit=dict(top=["bandeau", "vest"], extras=["belt", "pouch", "bracers"]),
        thick=0.95, hair="braid", rattle=True,
        pal=dict(SKIN=skin("peach"), HAIR="#6a5040", FUR="#a08a68", LIGHT="#e8dcc4", MARK="#5a4430", EYE="#c8a038", CLOTH="#8a4a2a", TRIM="#d8c090",
                 pattern="diamonds", facemark="scales"),
        variants={"timber": dict(FUR="#c8b080", MARK="#3a2a1e", pattern="bands", HAIR="#3a2a1e"),
                  "sidewinder": dict(FUR="#e2d2a8", MARK="#a08060", pattern="speckle", HAIR="#c8b088", CLOTH="#a06a3a"),
                  "mojave": dict(FUR="#8a9060", MARK="#3a3a2a", HAIR="#5a5a3a", CLOTH="#4a5a3a"),
                  "prairie": dict(FUR="#c0a878", MARK="#6a5038", pattern="blotches", HAIR="#7a5a40")}),
    "boa_lamia": dict(
        build="strong", outfit=dict(top=["sports", "harness"], bottom="wrap_skirt", extras=["pauldron"]),
        thick=1.05, hair="ponytail", redtail=True,
        pal=dict(SKIN=skin("fair"), HAIR="#4a2a20", FUR="#d0b898", LIGHT="#f0e8d8", MARK="#6a3a2a", EYE="#c89040", CLOTH="#2a4a2a", TRIM="#d8a850",
                 pattern="blotches", facemark="scales", extra=dict(tail="#a8302a")),
        variants={"rainbow": dict(FUR="#b0683a", MARK="#3a1a12", pattern="rosettes", HAIR="#7a3a22", extra=dict(tail="#c06030")),
                  "emerald": dict(FUR="#2a9a4a", MARK="#f0f8f0", LIGHT="#e8f0a0", pattern="speckle", HAIR="#1e5a2a", EYE="#d8c040", extra=dict(tail="#2a8a42")),
                  "sand": dict(FUR="#d8c08a", MARK="#6a4a2a", pattern="spots", HAIR="#8a6a40", extra=dict(tail="#b89060")),
                  "rosy": dict(FUR="#c8c0bc", MARK="#c08070", pattern="finestripes", HAIR="#8a5a5a", extra=dict(tail="#c08070"))}),
    "naga_lamia": dict(
        build="athletic", outfit=dict(top=["bandeau", "capelet"], bottom="loincloth", extras=["usekh", "armlets", "bracers"], colors=dict(cloth2="#e8c050")),
        thick=1.15, hair="flowing", hood=True, crown=True,
        pal=dict(SKIN=skin("tan"), HAIR="#1e1a14", FUR="#5a5030", LIGHT="#e8dcb0", MARK="#d8c890", EYE="#e8b020", CLOTH="#2a5a3a", TRIM="#e8c050",
                 pattern="bars", facemark="scales"),
        variants={"black": dict(FUR="#1a1a1c", MARK="#4a4a4c", LIGHT="#c8c0a8", HAIR="#101012", CLOTH="#3a2a4a"),
                  "golden": dict(FUR="#c8a050", MARK="#f0e0a0", HAIR="#5a3a14", CLOTH="#6a1e1e"),
                  "jade": dict(FUR="#2a6a3a", MARK="#a8d888", HAIR="#14301e", CLOTH="#e8e0c8"),
                  "crimson": dict(FUR="#7a1e1e", MARK="#d8a060", HAIR="#2a0e0e", CLOTH="#1e2a4a")}),
    "wadjet_lamia": dict(                               # on two legs (girl_family): built by legged_* below
        biped=True, build="athletic", outfit=dict(top=["bandeau"], bottom="wrap_skirt", extras=["usekh", "armlets", "bracers"], colors=dict(cloth2="#e8c050")),
        hair="bob", bangs="blunt", ears=None, legs="bare", foot="shoe", tail=[(1.5, 1.5, 4), (1.25, 1.25, 4), (1, 1, 3.5)], tail_look="scale",
        tail_rest=[(-35, 0, 0), (12, 0, 0), (10, 0, 0)], pupil=(0.9, 3.6),
        pal=dict(SKIN=skin("brown"), HAIR="#141210", FUR="#7a6a3a", LIGHT="#e8dcb0", MARK="#3a2e1a", EYE="#e8b020", CLOTH="#f0ece0", TRIM="#e8c050",
                 pattern="bars", extra=dict(boot="#c8a050")),
        variants={"green": dict(FUR="#2a8a4a", MARK="#145a2a", LIGHT="#b8e8a0"),
                  "black": dict(FUR="#1e1c1a", MARK="#4a4440", LIGHT="#c8c0a8", CLOTH="#e8e0d0"),
                  "gold": dict(FUR="#c8a050", MARK="#f0e0a0", LIGHT="#fff4d0", CLOTH="#2a4a8a"),
                  "red": dict(FUR="#a83a2a", MARK="#5a1a10", LIGHT="#e8b8a0", CLOTH="#e8e0d0")}),
}


import serpent_rig  # noqa: E402


def declare(sp):
    d = SPECIES[sp]
    k = d["thick"]
    st = d.get("stub", 1.0)                             # a stubby tail: the same girth, much shorter
    parts = [(n, round(w * k * 4) / 4, round(h * k * 4) / 4 if i else h, round(dd * (st if i else 1) * 4) / 4) for i, (n, w, h, dd) in enumerate(SEGS)]
    extra = serpent_rig.parts() if d.get("rearing") else []
    if d.get("hood"):
        extra.append(("hood_p", 5.5, 6.5, .5))
    if d.get("crown"):
        extra += [("crown", 4.5, 1, 4.5), ("crown_spike", .75, 1.5, .5), ("crown_gem", 1, 1, .25)]
    if d.get("rattle"):
        extra.append(("rattle", 1.25, 1, .75))
    if d.get("paddle"):
        extra.append(("paddle", .5, 3.5, 4))
    if d.get("spines"):
        extra += [("spine", .5, 3.5, .5), ("spine_s", .5, 2, .5)]
    if d.get("whood"):                                   # the sandworm's scaly hood: a cap over her hair, ridged
        extra += [("whood", 9.25, 3.5, 9.25), ("whood_back", 9.25, 4, 1)]
    if d.get("xheads"):                                  # Xiangliu's little heads: a neck, a head, a cap of hair with a fringe
        extra += [("xneck", 1.25, 6, 1.25), ("xhead", 3.5, 3.5, 3.5), ("xhair", 4, 1.5, 4), ("xhair_back", 4, 2.5, .75)]
    if d.get("medusa"):
        extra += [("snake", 1, 5.5, 1), ("snake_h", 1.5, 1, 2), ("blindfold", 8.5, 1.75, 8.5), ("blind_tail", 1, 3, .25)]
    G.declare(parts + extra, ears=d.get("ears"), hair=SPECIES[sp]["hair"],
              build=SPECIES[sp].get("build"), outfit=SPECIES[sp].get("outfit"))


def paint(sp, key):
    d = SPECIES[sp]
    pal = G.pal_from(d["pal"], d["variants"].get(key) if key else None, pupil=(0.9, 3.6))
    pt = H.Painter()
    G.paint_upper(pt, pal, top="wrap", top_pattern="dots", outfit=d.get("outfit"), build=d.get("build"))
    if d.get("ears"):                                    # the serpopard's leopard ears
        G.paint_ears(pt, pal)
    sc = G.scale_fn(pal, belly_faces=("down",))
    belly = G.scale_fn(pal, belly_faces=("down", "north"))
    skin_t = H.skin_fn(pal, 0.66)

    def hips(f, x, y, w, h):                       # the scales begin at the hips
        return skin_t(f, x, y, w, h) if y < h * 0.35 else sc(f, x, y, w, h)
    pt("hips", "all", hips)
    if d.get("rearing"):
        serpent_rig.paint(pt, pal)
    pt("seg_1", "all", lambda f, x, y, w, h: skin_t(f, x, y, w, h) if y < 2 and f != "down" else belly(f, x, y, w, h))
    tail_col = pal.extra.get("tail")
    for i in range(2, 9):
        if tail_col and i >= 6:
            red = G.palette(FUR=tail_col, LIGHT=pal.LIGHT, MARK=pal.MARK)
            red.pattern = pal.pattern
            red.pattern_scale = pal.pattern_scale
            pt(f"seg_{i}", "all", G.scale_fn(red))
        else:
            pt(f"seg_{i}", "all", sc)
    pt("hood_p", "all", lambda f, x, y, w, h: pick(pal.LIGHT, 0.55, x, y) if f == "north" else sc(f, x, y, w, h))
    gold = shades("#e8c050")
    pt("crown", "all", lambda f, x, y, w, h: pick(gold, 0.55 + 0.3 * (y == 0), x, y))
    pt("crown_spike", "all", lambda f, x, y, w, h: pick(gold, 0.7 - 0.2 * (x == 0), x, y))
    pt("crown_gem", "all", lambda f, x, y, w, h: pick(shades("#3ac87a"), 0.7, x, y))
    pt("rattle", "all", lambda f, x, y, w, h: pick(shades("#c8b896"), 0.55 - (0.25 if y == h - 1 else 0), x, y))
    pt("paddle", "all", lambda f, x, y, w, h: pick(pal.FUR, 0.5, x, y) if (y // 3) % 2 else pick(pal.MARK, 0.4, x, y))
    spine, spine_tip = shades(pal.extra.get("spine", "#6a2a7a")), shades(pal.extra.get("spine_tip", "#e878c8"))
    for p in ("spine", "spine_s"):
        pt(p, "all", lambda f, x, y, w, h: pick(spine_tip if y >= h - 1 else spine, 0.5 + 0.15 * (f == "north"), x, y))
    sn, band = shades(pal.extra.get("snake", "#4a9a3a")), shades(pal.extra.get("snake_band", "#2a5a24"))
    band2 = shades(pal.extra.get("snake_band2", pal.extra.get("snake_band", "#2a5a24")))
    pt("snake", "all", lambda f, x, y, w, h: pick(band if y % 4 == 0 else band2 if y % 4 == 2 and pal.extra.get("snake_band2") else sn, 0.5 + 0.15 * (f == "north"), x, y))
    pt("snake_h", "all", lambda f, x, y, w, h: pick(shades("#f8d040"), 0.7, x, y) if f == "north" and y == 0 and x in (0, w - 1)
       else pick(sn, 0.45 + 0.1 * (f == "up"), x, y))
    ring = lambda f, x, y, w, h: pick(pal.MARK, 0.45, x, y) if (y % 4 == 3 and f in ("north", "south", "east", "west")) or (x % 4 == 3 and f == "up")         else sc(f, x, y, w, h)
    pt("whood", "all", ring)
    pt("whood_back", "all", ring)

    def little_face(f, x, y, w, h):                      # two big dark eyes with a glint, a small smile
        if f != "north":
            return skin_t(f, x, y, w, h)
        if y in (5, 6, 7) and x in (3, 4, 9, 10):
            return pal.WHITE if (y == 5 and x in (3, 9)) else pal.EYE[1]
        if y == 10 and 6 <= x <= 7:
            return pal.LIP
        if y == 9 and x in (2, 11):
            return pal.BLUSH
        return skin_t(f, x, y, w, h)
    pt("xhead", "all", little_face)
    hair = lambda f, x, y, w, h: pick(pal.HAIR, 0.45 + 0.2 * (x % 3 == 0) + (noise(x, y, 81) - 0.5) * 0.1, x, y)
    pt("xhair", "all", hair)
    pt("xhair_back", "all", hair)
    pt("xneck", "all", sc)
    bf = shades(pal.extra.get("blindfold", "#5a2a6a"))
    pt("blindfold", "all", lambda f, x, y, w, h: pick(bf, 0.45 + 0.15 * (y == 0) + (noise(x, y, 51) - 0.5) * 0.1, x, y) if f not in ("up", "down") else CLEAR)
    pt("blind_tail", "all", lambda f, x, y, w, h: pick(bf, 0.45, x, y))
    return pt.T


def build(sp):
    d = SPECIES[sp]
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    g = G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), build=d.get("build"), outfit=d.get("outfit"), ears=d.get("ears"), skirt=False))
    if d.get("rearing"):
        serpent_rig.build(r)
    else:
        end_z = G.serpent(r, None, d)
    if d.get("crown"):                                  # the naga's crown: a gold band, five spikes, a jade gem at the front
        cs = [cube("crown", [-2.25, g.hy + 8.3, -2.25 + g.z])]
        for x in (-1.9, -0.95, 0, 0.95, 1.9):
            cs.append(cube("crown_spike", [x - 0.375, g.hy + 9.3, -2.4 + g.z]))
        cs.append(cube("crown_gem", [-0.5, g.hy + 8.3, -2.55 + g.z]))
        r.add("crown", "head", [0, g.hy + 8.3, g.z], cs)
    if d.get("hood"):
        hp = G.S("hood_p")
        r.add("hood", "body", [0, 19, 1.4], [G.rot_cube("hood_p", [-hp[0], 16.5, 1.4], [0, 19, 1.4], 0, 0, 22),
                                              G.rot_cube("hood_p", [0, 16.5, 1.4], [0, 19, 1.4], 0, 0, -22)])
    if d.get("rattle"):
        s8 = G.S("seg_8")
        for i in range(3):
            r.put("seg_8", cube("rattle", [-0.625, 0.1, end_z - 0.25 + i * 0.7]))
    if d.get("paddle"):
        r.put("seg_8", cube("paddle", [-0.25, 0, end_z - 1.5]))
    if d.get("spines"):                                 # urchin spines fanning up from each segment, a crest of them over her head
        z = G.S("seg_1")[2] / 2 - 1.0
        for i in range(2, 9):
            w, h, dd = G.S(f"seg_{i}")
            part = "spine" if i < 7 else "spine_s"
            for k, zz in enumerate((z + dd * 0.3, z + dd * 0.75)):
                for sg in (-1, 1):
                    x = sg * w * 0.25
                    r.put(f"seg_{i}", G.rot_cube(part, [x - 0.25, h - 0.3, zz - 0.25], [x, h - 0.3, zz], 25, 0, sg * (18 + 10 * k)))
            z += dd - 0.25
        hy, gz = g.hy, g.z
        r.add("hair_spines", "head", [0, hy + 8, gz], [G.rot_cube("spine", [x - 0.25, hy + 7.6, zz - 0.25 + gz], [x, hy + 7.6, zz + gz], 35, 0, -x * 6)
                                                        for x, zz in ((-2.2, 0.5), (0, -0.5), (2.2, 0.5), (-1.1, 2.4), (1.1, 2.4))])
    if d.get("whood"):                                  # the hood over her crown and down the back of her head
        hy, z = g.hy, g.z
        r.add("hair_whood", "head", [0, hy + 8, z], [cube("whood", [-4.625, hy + 6.0, -4.75 + z]), cube("whood_back", [-4.625, hy + 2.0, 4.0 + z])])
    if d.get("xheads"):                                 # six little heads on necks rising from her shoulders and back, round her own
        hy, z = g.hy, g.z
        cs = []
        for x, dy, zz in ((-6.0, 1.5, 1.0), (6.0, 1.5, 1.0), (-4.2, 5.5, 3.0), (4.2, 5.5, 3.0), (-7.5, -2.0, 2.0), (7.5, -2.0, 2.0)):
            top = hy + dy
            cs += [cube("xneck", [x - 0.625, top - 6.0, zz - 0.625 + z]), cube("xhead", [x - 1.75, top, zz - 1.75 + z]),
                   cube("xhair", [x - 2.0, top + 3.0, zz - 2.0 + z]), cube("xhair_back", [x - 2.0, top + 1.0, zz + 1.75 + z])]
        r.add("hair_xheads", "body", [0, hy, z], cs)
    if d.get("medusa"):                                 # nine snakes rising from her head and curling out, each with its own head
        hy, z = g.hy, g.z
        cs = []
        for i, (x, zz, ry) in enumerate(((-3.2, -2.5, -30), (-1.2, -3.0, -10), (1.2, -3.0, 10), (3.2, -2.5, 30), (-3.4, 0.5, -70),
                                         (3.4, 0.5, 70), (-2.4, 3.0, -140), (0, 3.4, 180), (2.4, 3.0, 140))):
            tilt = 28 + 8 * (i % 3)
            cs.append(G.rot_cube("snake", [x - 0.5, hy + 7.6, zz - 0.5 + z], [x, hy + 7.6, zz + z], -tilt, ry, 0))
            tip = H.rot_about((x, hy + 7.6 + 5.0, zz + z), (x, hy + 7.6, zz + z), H.rot_matrix(-tilt, ry, 0))
            cs.append(G.rot_cube("snake_h", [tip[0] - 0.75, tip[1] - 0.3, tip[2] - 1.6], list(tip), 10, ry, 0))
        r.add("snakes", "head", [0, hy + 7.6, z], cs)
        r.add("blindfold", "head", [0, hy + 3.5, z], [cube("blindfold", [-4.25, hy + 2.6, -4.25 + z], inflate=0.3),
                                                      G.rot_cube("blind_tail", [-0.5, hy + 0.2, 4.5 + z], [0, hy + 3.2, 4.4 + z], 12, 0, 0)])
    return r.model(sp)


SEG_NAMES = [f"seg_{i}" for i in range(1, 9)]


def anims(an):
    G.serpent_anims(an, SEG_NAMES)
    Tk = H.track                                         # curled up, rolling down the hill like a wheel
    an.add("roll", 0.5, True, {"root": {"rotation": Tk((0, (0, 0, 0)), (0.25, (180, 0, 0)), (0.5, (360, 0, 0))), "position": H.hold(0, 5, 0)},
                               **{s: {"rotation": H.hold(-35, 0, 0)} for s in SEG_NAMES[1:]}})
    if SPECIES.get(an.name, {}).get("rearing"):
        serpent_rig.anims(an)


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.3, -45, 20), ("rear", 0.9, -30, 8), ("sit", 0, -40, 25), ("bask", 0, -40, 15)]
FACE = [("idle", 0, 0, 0, 24), ("idle", 2.75, 0, 0, 24), ("hiss", 0.5, 0, 0, 24), ("idle", 0, -30, 8, 24)]


# --- the two-legged ones (the wadjet): girl_family's biped with the lamia hood, and the lamia's bask, coil and rear ---------

def legged_declare(sp, d):
    return [("hood_p", 5.5, 6.5, .5)]


def legged_paint(pt, pal, d):
    sc = G.scale_fn(pal, belly_faces=("down",))
    pt("hood_p", "all", lambda f, x, y, w, h: pick(pal.LIGHT, 0.55, x, y) if f == "north" else sc(f, x, y, w, h))


def legged_build(r, g, d):
    hp = G.S("hood_p")
    y, z = 19.5, 2.6                                    # higher and further back than on a serpent: it frames her head, behind the bob
    r.add("hood", "body", [0, y + 2.5, z], [G.rot_cube("hood_p", [-hp[0], y, z], [0, y + 2.5, z], 0, 0, 18),
                                             G.rot_cube("hood_p", [0, y, z], [0, y + 2.5, z], 0, 0, -18)])


def legged_anims(an, sp, d):
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("bask", 4.0, True, merge({"root": {"rotation": hold(-90, 0, 0), "position": hold(0, 2.5, 0)}, "head": {"rotation": hold(-30, 0, 0)},
                                     "eyelids": {"position": hold(*H.OUT_)}}, sym("arm", hold(-170, 0, 20))))
    an.add("coil", 1.5, True, merge({"body": {"rotation": hold(15, 0, 0)}, "hood": {"scale": hold(1.25, 1.1, 1)}},
                                    sym("arm", Tk((0, (-80, 0, 25)), (0.75, (-90, 0, 35)), (1.5, (-80, 0, 25))))))
    an.add("rear", 1.0, False, merge({"body": {"rotation": Tk((0, (0, 0, 0)), (0.5, (-15, 0, 0)))}, "head": {"rotation": hold(-15, 0, 0)},
                                      "hood": {"scale": Tk((0, (1, 1, 1)), (0.5, (1.35, 1.2, 1)))}}, sym("arm", hold(-30, 0, 30))))


if __name__ == "__main__":
    import girl_family as F
    legged = {k: v for k, v in SPECIES.items() if v.get("biped")}
    F.run_family("lamia", legged, legged_declare, legged_paint, legged_build, legged_anims)
    G.run_family("lamia", {k: v for k, v in SPECIES.items() if k not in legged}, declare, paint, build, anims, VIEWS, FACE)
