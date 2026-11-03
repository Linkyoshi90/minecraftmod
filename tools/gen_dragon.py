#!/usr/bin/env python3
"""Dragons (the dragon family, see dragon_field_guide.html): long, Jormungandr, wyrm, gryphon, Kannae, wyvern, Yamata no
Orochi.

    python tools/gen_dragon.py [species ...] [--previews]

    long, jormungandr   girl_lib upper body on a serpent tail (seg_1..seg_8, like the lamia); the long dragon has antlers,
                        a mane down her back and a "fly" animation that swims through the air
    gryphon             girl_lib upper body on a four-legged body (G.taur): eagle forelegs, lion hind legs, feathered
                        wings (wing_r / wing_l) and a tufted tail
    wyrm, wyvern        girl_family bipeds with great membrane wings, dragon horns and a heavy tail (the wyvern's ends
                        in a stinger); both fly
    kannae              a small girl_family biped with little horns and a fat tail
    orochi              a girl_family biped in an elaborate kimono with seven dragon heads on long necks rising around
                        her own (bones halo_neck1..7, left out of her height)
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
import girl_parts as P  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

DRAGON_TAIL = [(3, 3, 4.5), (2.5, 2.5, 4.5), (2, 2, 4.5), (1.25, 1.25, 4)]
SEGS = [("seg_1", 4.5, 10.5, 4), ("seg_2", 4.25, 4, 5.5), ("seg_3", 4, 3.75, 5.5), ("seg_4", 3.5, 3.25, 5),
        ("seg_5", 3, 2.75, 5), ("seg_6", 2.5, 2.25, 4.5), ("seg_7", 2, 1.75, 4), ("seg_8", 1.25, 1.25, 3.5)]
BIG_SEGS = [(n, round(w * 1.3 * 4) / 4, h if n == "seg_1" else round(h * 1.3 * 4) / 4, round(d * 1.25 * 4) / 4) for n, w, h, d in SEGS]

SPECIES = {
    # thirty-seventh wave: the chimera: a lion's mane, a goat's horns, a snake for a tail, and fire
    "chimera_dragon": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts", extras=["belt"]), hair="mane", bangs="jagged", ears=None, legs="scales", foot="claw",
        tail=DRAGON_TAIL, tail_look="spade", tail_rest=[(-30, 0, 0), (8, 0, 0), (8, 0, 0), (6, 0, 0)], horns="dragon", wings="dragon", wing_scale=1.0,
        flies=True,
        pal=dict(SKIN=skin("tan"), HAIR="#a8602a", FUR="#c89a4a", LIGHT="#f0dca0", MARK="#3a7a3a", EYE="#e8602a", CLOTH="#5a2a22", TRIM="#e8a030",
                 pupil=(0.9, 3.4), extra=dict(horn="#e8dcc0", wing="#a8602a", wing_bone="#5a2a22", tail_col="#3a7a3a")),
        variants={"black": dict(FUR="#3a3028", HAIR="#1e1814", extra=dict(horn="#a89880", wing="#3a3028", wing_bone="#141010", tail_col="#2a3a2a")),
                  "white": dict(FUR="#e8e0d0", HAIR="#c8bca8", extra=dict(horn="#e8c050", wing="#e8e0d0", wing_bone="#a89880", tail_col="#7ab87a")),
                  "red": dict(FUR="#a83a22", HAIR="#6a1e12", extra=dict(horn="#e8dcc0", wing="#a83a22", wing_bone="#3a1008", tail_col="#a8582a")),
                  "ash": dict(FUR="#8a8078", HAIR="#4a4440", extra=dict(horn="#e8dcc0", wing="#8a8078", wing_bone="#3a3430", tail_col="#5a6a5a"))}),
    # thirty-third wave: the amphiptere: a winged serpent with no legs at all, on the rearing rig, green and gold
    "amphiptere_dragon": dict(
        build="slim", outfit=dict(top=["bandeau"], extras=["bracers"], colors=dict(cloth2="#e8c040", leather="#e8c040")),
        hair="flowing", bangs="swept", ears=None, lower="none", wings="dragon", wing_scale=1.15, flies=True, rearing=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e6a3a", FUR="#2a8a4a", LIGHT="#e8e0a0", MARK="#0e3a1e", EYE="#e8c040", CLOTH="#e8c040", TRIM="#1e6a3a",
                 pupil=(0.9, 3.4), extra=dict(wing="#2a8a4a", wing_bone="#e8c040", claw="#e8e0a0")),
        variants={"red": dict(HAIR="#8a1e1e", FUR="#b82a2a", MARK="#4a0e0e", TRIM="#8a1e1e", extra=dict(wing="#b82a2a", wing_bone="#e8c040", claw="#e8e0a0")),
                  "blue": dict(HAIR="#1e4a8a", FUR="#2a6ab8", MARK="#0e2a4a", TRIM="#1e4a8a", extra=dict(wing="#2a6ab8", wing_bone="#e8e0c0", claw="#e8e0a0")),
                  "white": dict(HAIR="#d8d8e0", FUR="#f0f0f4", MARK="#8a8a98", TRIM="#8a8a98", EYE="#3a7ac8", extra=dict(wing="#f0f0f4", wing_bone="#a8a8b8", claw="#ffffff")),
                  "black": dict(HAIR="#1a1a1e", FUR="#2a2a30", MARK="#0a0a0e", TRIM="#e8c040", EYE="#e85a3a", extra=dict(wing="#2a2a30", wing_bone="#e8c040", claw="#8a8a90"))}),
    # thirty-third wave: the Ender Dragon, in a girl's shape: black scales, violet eyes and wing webs, tall, the End's own
    "ender_dragon": dict(
        build="strong", outfit=dict(top=["corset"], bottom="loincloth", extras=["armlets", "bracers"], colors=dict(cloth2="#6a2ab8")), hair="flowing", bangs="jagged", ears=None, legs="scales", foot="claw",
        tail=DRAGON_TAIL, tail_look="spade", tail_rest=[(-30, 0, 0), (8, 0, 0), (8, 0, 0), (6, 0, 0)], horns="dragon", wings="dragon", wing_scale=1.5,
        flies=True,
        pal=dict(SKIN=skin("pale"), HAIR="#141218", FUR="#1e1c24", LIGHT="#4a4458", MARK="#0a080e", EYE="#c858f8", CLOTH="#141218", TRIM="#a858f0",
                 pupil=(0.9, 3.4), extra=dict(horn="#2a2830", wing="#3a1e68", wing_bone="#141218", tail_col="#1e1c24")),
        variants={"violet": dict(FUR="#3a1e68", HAIR="#2a1450", MARK="#1a0a3a", extra=dict(horn="#a858f0", wing="#a858f0", wing_bone="#2a1450", tail_col="#3a1e68")),
                  "pale": dict(FUR="#d8d0e0", HAIR="#b8b0c8", MARK="#5a5068", SKIN=skin("fair"), extra=dict(horn="#5a5068", wing="#8a70b8", wing_bone="#5a5068", tail_col="#d8d0e0")),
                  "end_stone": dict(FUR="#d8d8a0", HAIR="#a8a870", MARK="#5a5a30", extra=dict(horn="#5a5a30", wing="#3a3a20", wing_bone="#5a5a30", tail_col="#d8d8a0")),
                  "void": dict(FUR="#0a0a12", HAIR="#050508", MARK="#2a2a48", EYE="#5ae8e0", TRIM="#5ae8e0", extra=dict(horn="#14141e", wing="#10102a", wing_bone="#5ae8e0", tail_col="#0a0a12"))}),
    # twenty-first wave: the nenekea, a blue sea dragon who walks on land: fins for ears, a crest of fins, gold beads and pearls,
    # a long tail ending in a great fan of a fin
    "nenekea_dragon": dict(
        build="slim", outfit=dict(top=["bandeau"], bottom="wrap_skirt", extras=["pearls", "armlets"], colors=dict(cloth2="#2a4ab0")),
        hair="flowing", bangs="swept", ears="fin", legs="scales", foot="claw", tail=[(2, 2, 4), (1.5, 1.5, 4), (1, 1, 4)],
        tail_look="scale", tail_rest=[(-30, 0, 0), (10, 0, 0), (8, 0, 0)], seafin=True,
        pal=dict(SKIN=skin("fair"), HAIR="#3a6ad8", FUR="#3a7ae0", LIGHT="#e8f4ff", MARK="#1a3a8a", EYE="#2a8ad8", CLOTH="#2a4ab0", TRIM="#e8c050",
                 pupil=(0.9, 3.4), extra=dict(fin="#d8ecff", fin_ray="#5aa8f0")),
        variants={"teal": dict(HAIR="#2a9a9a", FUR="#2aa8a8", MARK="#0e4a4a", CLOTH="#1e6a6a", EYE="#2ad8c8", extra=dict(fin="#d8fcf4", fin_ray="#3ac8b8")),
                  "coral": dict(HAIR="#e86a7a", FUR="#f08a8a", LIGHT="#fff0ec", MARK="#a83a4a", CLOTH="#3a8ac8", EYE="#e85a6a",
                                extra=dict(fin="#fff0ec", fin_ray="#f8a8a0")),
                  "gold": dict(HAIR="#d8a030", FUR="#e8b840", LIGHT="#fff8e0", MARK="#8a5a14", CLOTH="#2a3a8a", EYE="#c8902a",
                               extra=dict(fin="#fff4d0", fin_ray="#f0c860")),
                  "violet": dict(HAIR="#7a4ad8", FUR="#8a5ae0", LIGHT="#f4ecff", MARK="#3a1e7a", CLOTH="#4a2a9a", EYE="#c87af8",
                                 extra=dict(fin="#f0e4ff", fin_ray="#b088f0"))}),
    # twenty-second wave: the macawtl, a feathered serpent in deep macaw blue: a crest of plumes, a hooked beak of a mask on her brow,
    # feathered wings and tail, two long plumes trailing from it
    # twenty-fourth wave: Quetzalcoatl, the feathered serpent on the rearing rig: a crest of leaves and feathers, a woven band, pale wings
    "quetzalcoatl_dragon": dict(
        build="slim", outfit=dict(top=["bandeau"], extras=["usekh", "bracers"], colors=dict(cloth2="#e8903a", leather="#c86a2a")),
        hair="wavy", bangs="swept", ears=None, lower="none", wings="feather", wing_scale=1.1, flies=True, rearing=True, qcrest=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2aa8a0", FUR="#2ab8b0", LIGHT="#c8f4e4", MARK="#167a78", EYE="#2a7ac8", CLOTH="#e8903a", TRIM="#f8d080",
                 extra=dict(wing="#a8f0dc", wing_tip="#3ac8b8", leaf="#3ad8a8", leaf_tip="#c8f8e0", band="#e8903a", band_mark="#7a3a14")),
        variants={"green": dict(HAIR="#2a8a3a", FUR="#3aa84a", LIGHT="#d8f4b8", MARK="#1a5a24", extra=dict(wing="#c8f0a8", wing_tip="#4ab85a", leaf="#6ad84a", leaf_tip="#e0f8b0", band="#e8903a", band_mark="#7a3a14")),
                  "gold": dict(HAIR="#c8901e", FUR="#e0b040", LIGHT="#f8ecb0", MARK="#8a5a10", extra=dict(wing="#f8e8a8", wing_tip="#e0a830", leaf="#f0c840", leaf_tip="#fff0b0", band="#2aa8a0", band_mark="#0e5a58")),
                  "rose": dict(HAIR="#c84a7a", FUR="#e0709a", LIGHT="#fcd8e4", MARK="#8a2a50", extra=dict(wing="#fcd8e8", wing_tip="#e87aa8", leaf="#f08ab0", leaf_tip="#ffe0ec", band="#f0c840", band_mark="#8a5a10")),
                  "violet": dict(HAIR="#6a3ab8", FUR="#8a5ad8", LIGHT="#e4d8f8", MARK="#3a1a7a", extra=dict(wing="#e0d0f8", wing_tip="#8a5ad8", leaf="#a87ae8", leaf_tip="#ece0fc", band="#e8903a", band_mark="#7a3a14"))}),
    "macawtl_dragon": dict(
        build="slim", outfit=dict(top=["bandeau", "harness"], extras=["bracers"], colors=dict(leather="#c8ccd8", cloth2="#1e3a9a")),
        hair="flowing", bangs="swept", ears=None, lower="none", wings="feather", wing_scale=1.1, flies=True, macaw=True, rearing=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e3a9a", FUR="#2a4ab8", LIGHT="#c8d8f4", MARK="#0e1e5a", EYE="#f0c030", CLOTH="#1e3a9a", TRIM="#c8ccd8",
                 pupil=(0.9, 3.4), extra=dict(wing="#2a4ab8", wing_tip="#0e1e5a", plume="#3a6ae0", plume_tip="#c8ccd8", beak="#4a4a56", claw="#c8ccd8")),
        variants={"scarlet": dict(HAIR="#b81e1e", FUR="#d02a2a", LIGHT="#f8e0a0", MARK="#3a5ab8", CLOTH="#b81e1e", EYE="#f0e8d0",
                                  extra=dict(wing="#d02a2a", wing_tip="#2a5ac8", plume="#e8c030", plume_tip="#2a5ac8", beak="#f0e8d8", claw="#3a3a40")),
                  "green": dict(HAIR="#2a6a2a", FUR="#3a8a3a", LIGHT="#d8f0c0", MARK="#1e3a8a", CLOTH="#2a6a2a", EYE="#f0d040",
                                extra=dict(wing="#3a8a3a", wing_tip="#2a4ab8", plume="#c83a2a", plume_tip="#2a4ab8", beak="#2a2a30", claw="#3a3a40")),
                  "gold": dict(HAIR="#2a5ac8", FUR="#3a6ad8", LIGHT="#f8d040", MARK="#0e2a6a", CLOTH="#e8b020", EYE="#f8f0d8",
                               extra=dict(wing="#3a6ad8", wing_tip="#0e2a6a", plume="#f0c020", plume_tip="#3a6ad8", beak="#2a2a30", claw="#3a3a40")),
                  "hyacinth": dict(HAIR="#3a2a8a", FUR="#4a3aa8", LIGHT="#f0d040", MARK="#1e1450", CLOTH="#3a2a8a", EYE="#f0c030",
                                   extra=dict(wing="#4a3aa8", wing_tip="#1e1450", plume="#5a4ac8", plume_tip="#f0d040", beak="#2a2a30", claw="#3a3a40"))}),
    # twentieth wave: the moo, a round little blue triceratops: a frill rimmed with cream knobs, two brow horns and a nose horn, spots
    "moo_dragon": dict(
        build="petite", outfit=dict(top=["bandeau"], bottom="shorts", extras=["belt"], colors=dict(cloth2="#3a6aa8")), hair="bob", bangs="blunt", ears=None,
        legs="scales", foot="claw", tail=[(2.5, 2.5, 4), (2, 2, 4), (1.25, 1.25, 3.5)], tail_look="scale", tail_rest=[(-30, 0, 0), (8, 0, 0), (6, 0, 0)],
        trifrill=True,
        pal=dict(SKIN=skin("fair"), HAIR="#5a8ac8", FUR="#7aa8d8", LIGHT="#d8e8f8", MARK="#3a6aa8", EYE="#2a3a5a", CLOTH="#3a6aa8", TRIM="#f0e0c0",
                 extra=dict(frill="#7aa8d8", frill_edge="#f0e0c0", horn="#f0e0c0")),
        variants={"green": dict(HAIR="#4a8a4a", FUR="#6aa86a", LIGHT="#d8f0d0", MARK="#2a5a2a", CLOTH="#2a5a2a",
                                extra=dict(frill="#6aa86a", frill_edge="#f8e070", horn="#f0e0c0")),
                  "orange": dict(HAIR="#c86a2a", FUR="#e8904a", LIGHT="#f8e0c8", MARK="#8a3a14", CLOTH="#8a3a14",
                                 extra=dict(frill="#e8904a", frill_edge="#f8f0d0", horn="#f8f0d0")),
                  "pink": dict(HAIR="#d870a0", FUR="#f0a0c0", LIGHT="#fce8f0", MARK="#a83a6a", CLOTH="#a83a6a",
                               extra=dict(frill="#f0a0c0", frill_edge="#f8f0d0", horn="#f8f0d0")),
                  "purple": dict(HAIR="#6a4aa8", FUR="#8a6ac8", LIGHT="#e8dcf8", MARK="#3a2a6a", CLOTH="#3a2a6a",
                                 extra=dict(frill="#8a6ac8", frill_edge="#f8e070", horn="#f0e0c0"))}),
    # twenty-seventh wave: Bahamut, the king of dragons, drawn soft and round: pale blue, curled horns, fins for ears, a pale belly, dragon wings; she flies
    # thirtieth wave: Kun, the great fish of the Zhuangzi that becomes a bird: Bahamut's soft build, fin ears, wing-fins, a fish's fluke of a tail, in deep sky blues
    "kun_dragon": dict(
        build="curvy", outfit=dict(top=["bandeau"], bottom="shorts", colors=dict(cloth2="#2a4a7a")), hair="flowing", bangs="swept", ears="fin",
        legs="scales", foot="claw", tail=DRAGON_TAIL, tail_look="spade", tail_rest=[(-30, 0, 0), (8, 0, 0), (8, 0, 0), (6, 0, 0)], wings="dragon", wing_scale=1.3, flies=True,
        pal=dict(SKIN=skin("#c8d8f0"), HAIR="#3a5a9a", FUR="#2a4a8a", LIGHT="#c8e0f8", MARK="#14285a", EYE="#e8c050", CLOTH="#2a4a7a", TRIM="#c8e0f8",
                 extra=dict(wing="#4a70b0", wing_bone="#14285a", tail_col="#2a4a8a")),
        variants={"void": dict(SKIN=skin("#a898c8"), HAIR="#2a1a4a", FUR="#3a2a5a", LIGHT="#8a78b0", MARK="#140a28", EYE="#c88af8", extra=dict(wing="#4a3a7a", wing_bone="#140a28", tail_col="#3a2a5a")),
                  "dawn": dict(SKIN=skin("#f8dcc8"), HAIR="#e88a6a", FUR="#e07a5a", LIGHT="#fce0c8", MARK="#a83a2a", extra=dict(wing="#f0a07a", wing_bone="#a83a2a", tail_col="#e07a5a")),
                  "pale": dict(SKIN=skin("#e8eef4"), HAIR="#d8e4ec", FUR="#c8d8e4", LIGHT="#ffffff", MARK="#7a90a8", extra=dict(wing="#dce8f0", wing_bone="#7a90a8", tail_col="#c8d8e4")),
                  "jade": dict(SKIN=skin("#c8e8d8"), HAIR="#2a8a6a", FUR="#2a7a60", LIGHT="#c8f0dc", MARK="#14483a", extra=dict(wing="#4aa888", wing_bone="#14483a", tail_col="#2a7a60"))}),
    "bahamut_dragon": dict(
        build="curvy", outfit=dict(top=["bandeau"], bottom="shorts", colors=dict(cloth2="#8ab4e0")), hair="wavy", bangs="swept", ears="fin",
        legs="scales", foot="claw", tail=DRAGON_TAIL, tail_look="spade", tail_rest=[(-30, 0, 0), (8, 0, 0), (8, 0, 0), (6, 0, 0)], horns="dragon", wings="dragon", flies=True,
        pal=dict(SKIN=skin("#d8e8f8"), HAIR="#b8d4f0", FUR="#a8c8ec", LIGHT="#f0f6fc", MARK="#5a84b8", EYE="#2a4a8a", CLOTH="#8ab4e0", TRIM="#f0f6fc",
                 extra=dict(horn="#5a84b8", wing="#a8c8ec", wing_bone="#5a84b8", tail_col="#a8c8ec")),
        variants={"gold": dict(SKIN=skin("#f8ecc8"), HAIR="#f0d890", FUR="#e8c870", LIGHT="#fff8e0", MARK="#a87a2a", EYE="#8a4a14", CLOTH="#e8c870", extra=dict(horn="#a87a2a", wing="#e8c870", wing_bone="#a87a2a", tail_col="#e8c870")),
                  "black": dict(SKIN=skin("#7a7a8a"), HAIR="#2a2a34", FUR="#3a3a48", LIGHT="#a8a8b8", MARK="#14141c", EYE="#e8c040", CLOTH="#2a2a34", extra=dict(horn="#14141c", wing="#3a3a48", wing_bone="#14141c", tail_col="#3a3a48")),
                  "green": dict(SKIN=skin("#d8f0dc"), HAIR="#a8dcb4", FUR="#90d0a4", LIGHT="#f0fcf4", MARK="#3a8a5a", EYE="#1e5a3a", CLOTH="#90d0a4", extra=dict(horn="#3a8a5a", wing="#90d0a4", wing_bone="#3a8a5a", tail_col="#90d0a4")),
                  "red": dict(SKIN=skin("#f8d8d0"), HAIR="#e89a8a", FUR="#e0806a", LIGHT="#fcecE8", MARK="#a83a2a", EYE="#7a1e14", CLOTH="#e0806a", extra=dict(horn="#a83a2a", wing="#e0806a", wing_bone="#a83a2a", tail_col="#e0806a"))}),
    # twentieth wave: the hairypterix, a shaggy blue pterosaur: great membrane wings, a long head crest swept back, yellow claws
    "hairypterix_dragon": dict(
        build="slim", outfit=dict(top=["sports"], bottom="shorts", extras=["belt"], colors=dict(cloth2="#1a2a6a")), hair="shaggy", bangs="jagged",
        ears=None, legs="scales", foot="claw", tail=[(1, 1, 3)], tail_look="scale", tail_rest=[(-20, 0, 0)], wings="dragon", wing_scale=1.2,
        flies=True, ptcrest=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a4ab0", FUR="#3a5ac8", LIGHT="#e8eef8", MARK="#1a2a6a", EYE="#c82a2a", CLOTH="#1a2a6a", TRIM="#e8c040",
                 pupil=(0.9, 3.4), extra=dict(wing="#3a5ac8", wing_bone="#1a2a6a", crest="#e8c040", claw="#e8c040")),
        variants={"rust": dict(HAIR="#8a3a1e", FUR="#a84a2a", LIGHT="#f8e0c8", MARK="#4a1a0a", CLOTH="#4a1a0a",
                               extra=dict(wing="#a84a2a", wing_bone="#4a1a0a", crest="#f8d040", claw="#f8d040")),
                  "grey": dict(HAIR="#5a5a64", FUR="#7a7a84", LIGHT="#e8e8ec", MARK="#2a2a32", CLOTH="#2a2a32",
                               extra=dict(wing="#7a7a84", wing_bone="#2a2a32", crest="#c83a3a", claw="#e8c040")),
                  "green": dict(HAIR="#2a6a3a", FUR="#3a8a4a", LIGHT="#e0f8e8", MARK="#14401e", CLOTH="#14401e",
                                extra=dict(wing="#3a8a4a", wing_bone="#14401e", crest="#f8a040", claw="#e8c040")),
                  "black": dict(HAIR="#1e1c24", FUR="#2a2830", LIGHT="#8a8890", MARK="#0e0c12", CLOTH="#5a1e24", EYE="#e8d040",
                                extra=dict(wing="#2a2830", wing_bone="#0e0c12", crest="#c83a3a", claw="#c8ccd4"))}),
    # sixteenth wave: the cockatrice, a rooster-dragon with a red comb, feathered wings and a scaly tail
    "cockatrice_dragon": dict(
        build="athletic", outfit=dict(top=["bandeau", "harness"], bottom="loincloth", extras=["bracers"]), hair="pixie", bangs="jagged", ears=None,
        legs="scales", foot="claw", tail=DRAGON_TAIL, tail_look="scale", tail_rest=[(-30, 0, 0), (8, 0, 0), (8, 0, 0), (6, 0, 0)],
        wings="feather", wing_scale=1.0, flies=True, comb=True,
        pal=dict(SKIN=skin("fair"), HAIR="#9a3a1e", FUR="#a86a2a", LIGHT="#e8c890", MARK="#5a3a14", EYE="#e8c020", CLOTH="#3a2a1e", TRIM="#c8a040",
                 pupil=(0.9, 3.4), extra=dict(wing="#e8c070", wing_tip="#2a4a3a", comb="#d8201e", claw="#e8c060")),
        variants={"white": dict(HAIR="#f4f0e8", FUR="#e8e4dc", LIGHT="#ffffff", MARK="#a8a090", extra=dict(wing="#f4f0e8", wing_tip="#c8c0b0")),
                  "black": dict(HAIR="#1e1c24", FUR="#2a2830", LIGHT="#5a5a6a", MARK="#0e0e12", extra=dict(wing="#2a2a3a", wing_tip="#3a6a5a")),
                  "gold": dict(HAIR="#e8b840", FUR="#d8a030", LIGHT="#f8e8b0", MARK="#7a5010", extra=dict(wing="#e8b040", wing_tip="#a83a1e")),
                  "green": dict(HAIR="#3a7a3a", FUR="#4a8a3a", LIGHT="#c8e8a0", MARK="#1e3a14", extra=dict(wing="#3a7a3a", wing_tip="#d8201e"))}),
    "long_dragon": dict(
        build="slim", outfit=dict(top=["kimono"], extras=["pearls"], colors=dict(kimono="#2a7a5a", kimono_trim="#e8c040", obi="#e8c040")),
        hair="flowing", bangs="parted", ears="fin", body="serpent", segs=SEGS, horns="antlers", mane=True, flies=True,
        pal=dict(SKIN=skin("fair"), HAIR="#e8f0e0", FUR="#3a9a6a", LIGHT="#e8e0a0", MARK="#1e5a3a", EYE="#e8c040", CLOTH="#2a7a5a", TRIM="#e8c040",
                 extra=dict(horn="#e8d8a0")),
        variants={"azure": dict(FUR="#3a6ac8", MARK="#1a3a7a", HAIR="#e0f0ff", extra=dict(kimono="#2a4a9a", horn="#e8d8a0")),
                  "vermilion": dict(FUR="#c83a2a", MARK="#7a1a14", HAIR="#f8e8c0", extra=dict(kimono="#8a1a1a", horn="#f0d080")),
                  "gold": dict(FUR="#d8a830", MARK="#8a5a14", HAIR="#fff4d8", extra=dict(kimono="#8a5a14", horn="#fff0c0")),
                  "pearl": dict(FUR="#f0ece8", MARK="#c8c0d8", HAIR="#ffffff", extra=dict(kimono="#c8c0d8", horn="#e8e0f0"))}),
    "jormungandr_dragon": dict(
        build="strong", outfit=dict(top=["sarashi"], extras=["armlets", "pearls"], colors=dict(bandage="#4a5a4a")),
        hair="mane", bangs="messy", ears=None, body="serpent", segs=BIG_SEGS, horns="dragon",
        pal=dict(SKIN=skin("olive"), HAIR="#2a3a2a", FUR="#5a7a5a", LIGHT="#c8d0a0", MARK="#2a3a2a", EYE="#e8d83a", CLOTH="#3a4a3a", TRIM="#c8a050",
                 pattern="blotches", pupil=(0.9, 3.4), extra=dict(horn="#3a3a30")),
        variants={"ash": dict(FUR="#7a7a78", MARK="#3a3a38", HAIR="#4a4a48"),
                  "venom": dict(FUR="#5a8a2a", MARK="#e8d83a", HAIR="#2a4a14"),
                  "abyss": dict(FUR="#2a3a5a", MARK="#5aa8f0", HAIR="#141e30"),
                  "gold": dict(FUR="#c8a040", MARK="#6a4a14", HAIR="#7a5a20")}),
    "wyrm_dragon": dict(
        build="strong", outfit=dict(top=["corset", "harness"], bottom="loincloth", extras=["pauldron", "bracers"], colors=dict(cloth2="#3a1a14")),
        hair="ponytail", bangs="jagged", ears=None, legs="scales", foot="claw", tail=DRAGON_TAIL, tail_look="scale",
        tail_rest=[(-30, 0, 0), (8, 0, 0), (8, 0, 0), (6, 0, 0)], horns="greater", wings="dragon", flies=True,
        pal=dict(SKIN=skin("fair"), HAIR="#8a1a1a", FUR="#a82a20", LIGHT="#e8b080", MARK="#4a0e0a", EYE="#f8c02a", CLOTH="#2a1a14", TRIM="#e8b040",
                 pupil=(0.9, 3.4), extra=dict(horn="#2a1a14", wing="#8a2a20", wing_bone="#3a0e0a", claw="#1a1010")),
        variants={"black": dict(FUR="#2a2428", HAIR="#141014", MARK="#8a2a3a", extra=dict(horn="#8a8a90", wing="#2a2028", wing_bone="#0e0c10")),
                  "green": dict(FUR="#3a7a3a", HAIR="#1e4a1e", MARK="#142a14", extra=dict(horn="#e8e0c0", wing="#3a6a3a", wing_bone="#142a14")),
                  "white": dict(FUR="#e8eef4", HAIR="#c8d8e8", MARK="#8aa8c8", EYE="#5aa8f0", extra=dict(horn="#a8b8c8", wing="#c8d8e8", wing_bone="#8aa0b8")),
                  "bronze": dict(FUR="#a87a3a", HAIR="#6a4a1e", MARK="#3a2a10", extra=dict(horn="#3a2a10", wing="#a87a3a", wing_bone="#4a3010"))}),
    "gryphon_dragon": dict(
        build="athletic", outfit=dict(top=["bandeau", "harness"], extras=["bracers"]), hair="mane", bangs="swept", ears="tuft", body="taur",
        wings="feather", flies=True,
        pal=dict(SKIN=skin("tan"), HAIR="#f4f0e8", FUR="#d8a850", LIGHT="#f4e0a8", MARK="#6a4a2a", EYE="#e8a82a", CLOTH="#3a5a8a", TRIM="#e8c050",
                 extra=dict(wing="#7a5a3a", wing_tip="#2a1e14", talon="#e8c040")),
        variants={"white": dict(FUR="#f0e8d8", HAIR="#ffffff", extra=dict(wing="#f4f4f0", wing_tip="#a8a8b0", talon="#e8c040")),
                  "black": dict(FUR="#3a3230", HAIR="#1e1a18", extra=dict(wing="#2a2624", wing_tip="#0e0c0a", talon="#c8a030")),
                  "silver": dict(FUR="#a8a8a8", HAIR="#d8d8d8", extra=dict(wing="#8a8a90", wing_tip="#4a4a50", talon="#e8e0c0")),
                  "red": dict(FUR="#a8582a", HAIR="#e8d8c0", extra=dict(wing="#8a3a1a", wing_tip="#3a140a", talon="#e8c040"))}),
    "kannae_dragon": dict(
        build="petite", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["scarf"], colors=dict(scarf="#e8a8c8", cloth2="#f4f0f8")),
        hair="hime", bangs="blunt", ears=None, legs="stockings", foot="shoe", tail=[(3, 3, 3.5), (2.5, 2.5, 3.5), (1.75, 1.75, 3)], tail_look="scale",
        tail_rest=[(-55, 0, 0), (-10, 0, 0), (-5, 0, 0)], horns="kannae",
        pal=dict(SKIN=skin("pale"), HAIR="#f4f0f8", FUR="#e8e0f0", LIGHT="#ffffff", MARK="#b8a0d8", EYE="#8a6ad8", CLOTH="#f4f0f8", TRIM="#b8a0d8",
                 extra=dict(horn="#e8e0f8", horn_tip="#b8a0d8", stocking="#f4f0f8", boot="#b8a0d8")),
        variants={"pink": dict(HAIR="#f8c8d8", FUR="#f8d8e4", MARK="#e8a0b8", EYE="#d86a98"),
                  "lilac": dict(HAIR="#c8b0e8", FUR="#d8c8f0", MARK="#8a6ab8"),
                  "mint": dict(HAIR="#c8f0e0", FUR="#d8f4ea", MARK="#6ab89a", EYE="#3a9a7a"),
                  "black": dict(HAIR="#2a2430", FUR="#3a3440", MARK="#8a7aa8", EYE="#e83a5a")}),
    "wyvern_dragon": dict(
        build="slim", outfit=dict(top=["sports"], bottom="shorts", extras=["belt"]), hair="shaggy", bangs="messy", ears=None, legs="scales", foot="claw",
        tail=DRAGON_TAIL, tail_look="spade", tail_rest=[(-30, 0, 0), (8, 0, 0), (8, 0, 0), (6, 0, 0)], horns="dragon", wings="dragon", wing_scale=1.15,
        flies=True,
        pal=dict(SKIN=skin("tan"), HAIR="#a87a4a", FUR="#c8a06a", LIGHT="#f0dcb0", MARK="#5a3a1e", EYE="#e8d83a", CLOTH="#5a3a1e", TRIM="#c8a050",
                 pupil=(0.9, 3.4), extra=dict(horn="#5a3a1e", wing="#c8905a", wing_bone="#5a3a1e", tail_col="#c8a06a")),
        variants={"rust": dict(FUR="#a85a2a", HAIR="#7a3a1a", extra=dict(wing="#a8582a", wing_bone="#4a1e0a", tail_col="#a85a2a")),
                  "slate": dict(FUR="#6a7a8a", HAIR="#3a4a5a", extra=dict(wing="#5a6a8a", wing_bone="#2a3444", tail_col="#6a7a8a")),
                  "venom": dict(FUR="#6a9a3a", HAIR="#3a5a1e", MARK="#e8d83a", extra=dict(wing="#5a8a2a", wing_bone="#2a4010", tail_col="#6a9a3a")),
                  "dusk": dict(FUR="#7a5a8a", HAIR="#4a2a5a", extra=dict(wing="#6a4a8a", wing_bone="#2a1a3a", tail_col="#7a5a8a"))}),
    # twelfth wave: the alebrije, a winged folk-art chimera painted all over in bright dots and zigzags
    "alebrije_dragon": dict(
        build="slim", outfit=dict(top=["halter"], bottom="shorts", extras=["armlets"], colors=dict(cloth2="#f8e070")), hair="wavy", bangs="swept",
        ears="fox", legs="scales", foot="claw", tail=DRAGON_TAIL, tail_look="scale", tail_rest=[(-30, 0, 0), (8, 0, 0), (8, 0, 0), (6, 0, 0)],
        horns="dragon", wings="dragon", wing_scale=1.0, flies=True, alebrije=True,
        pal=dict(SKIN=skin("tan"), HAIR="#e85aa8", FUR="#3a6ad8", LIGHT="#f8e070", MARK="#e85aa8", EYE="#f8d040", CLOTH="#f8e070", TRIM="#e85aa8",
                 pupil=(0.9, 3.4), extra=dict(horn="#f8e070", wing="#3a6ad8", wing_bone="#e85aa8", tail_col="#3a6ad8", dot="#f8e070", zig="#e85aa8")),
        variants={"green": dict(HAIR="#f08a2a", FUR="#3aa85a", MARK="#f08a2a", extra=dict(horn="#f8a0c8", wing="#3aa85a", wing_bone="#f08a2a",
                                                                                          tail_col="#3aa85a", dot="#f8a0c8", zig="#f08a2a")),
                  "pink": dict(HAIR="#2ac8b8", FUR="#e85aa8", MARK="#2ac8b8", extra=dict(horn="#f8e070", wing="#e85aa8", wing_bone="#2ac8b8",
                                                                                        tail_col="#e85aa8", dot="#f8e070", zig="#2ac8b8")),
                  "orange": dict(HAIR="#8a4ad8", FUR="#f08a2a", MARK="#8a4ad8", extra=dict(horn="#7ad84a", wing="#f08a2a", wing_bone="#8a4ad8",
                                                                                          tail_col="#f08a2a", dot="#7ad84a", zig="#8a4ad8")),
                  "purple": dict(HAIR="#9ae83a", FUR="#7a3ac8", MARK="#9ae83a", extra=dict(horn="#e83a3a", wing="#7a3ac8", wing_bone="#9ae83a",
                                                                                          tail_col="#7a3ac8", dot="#e83a3a", zig="#9ae83a"))}),
    "orochi_dragon": dict(
        build="curvy", outfit=dict(top=["kimono"], bottom="hakama", extras=["pearls"],
                                   colors=dict(kimono="#a81a2a", kimono_trim="#e8c040", obi="#1a1a1a", cloth2="#2a1a1a")),
        hair="hime", bangs="blunt", ears=None, legs="bare", foot="shoe", heads=True, horns="kannae",
        pal=dict(SKIN=skin("pale"), HAIR="#141214", FUR="#3a7a3a", LIGHT="#c8e0a0", MARK="#1a3a1a", EYE="#c81a2a", CLOTH="#a81a2a", TRIM="#e8c040",
                 extra=dict(horn="#e8d8a0", boot="#1a1a1a")),
        variants={"night": dict(extra=dict(kimono="#1a1a2a", obi="#a81a2a", horn="#c8c8d8"), FUR="#2a3a5a"),
                  "sakura": dict(extra=dict(kimono="#f4b8c8", kimono_trim="#ffffff", obi="#a83a5a"), FUR="#5a8a4a"),
                  "gold": dict(extra=dict(kimono="#d8a830", kimono_trim="#1a1a1a", obi="#1a1a1a"), FUR="#8a6a2a"),
                  "storm": dict(extra=dict(kimono="#4a5a6a", kimono_trim="#c8d8e8", obi="#2a3440"), FUR="#4a5a6a", HAIR="#c8d0d8")}),
}

BIPEDS = {k: v for k, v in SPECIES.items() if v.get("body") is None}


import serpent_rig  # noqa: E402


def declare_hook(sp, d):
    out = []
    if d.get("heads"):
        out += [("neck_a", 1.75, 8, 1.75), ("neck_b", 1.5, 8, 1.5), ("dhead", 3.5, 3, 4), ("dsnout", 2.25, 1.75, 2.5)]
    if d.get("mane"):
        out += [("mane", 1, 4, 2.5)]
    if d.get("comb"):                                    # the cockatrice's rooster comb
        out += [("comb", 1, 3.5, 5), ("wattle", 1.5, 1.75, .5)]
    if d.get("trifrill"):                                # the moo's frill and three horns
        out += [("tfrill", 11, 7, .75), ("tfrill_knob", 1, 1, 1), ("brow_horn", 1, 3.5, 1), ("nose_horn", 1, 1.5, 1)]
    if d.get("ptcrest"):                                 # the hairypterix's long head crest
        out += [("ptcrest", 1, 2.5, 6.5)]
    if d.get("seafin"):                                  # the nenekea's tail fan and head crest
        out += [("nfan", .5, 6, 5.5), ("nfin", .5, 3.5, 5)]
    if d.get("macaw"):                                   # the macawtl's crest of plumes, beak mask and trailing tail plumes
        out += [("mcrest", .5, 1.25, 4.5), ("mbeak", 1, 1.25, 1), ("mbeak_tip", .75, .5, .5), ("mplume", 1.25, .25, 7.5)]
    if d.get("rearing"):
        out += serpent_rig.parts()
    if d.get("qcrest"):                                  # Quetzalcoatl's crest of leaves and woven band
        out += [("qleaf", 1.75, 6, .5), ("qband", 9.25, 1.5, 9.25)]
    return out


def paint_hook(pt, pal, d):
    if d.get("rearing"):
        serpent_rig.paint(pt, pal)
    if d.get("qcrest"):
        leaf, tip = G.shades(pal.extra.get("leaf", "#3ad8a8")), G.shades(pal.extra.get("leaf_tip", "#c8f8e0"))
        pt("qleaf", "all", lambda f, x, y, w, h: pick(tip if y < 2 else leaf, 0.5 + 0.2 * (x == w // 2), x, y))
        band, mark = G.shades(pal.extra.get("band", "#e8903a")), G.shades(pal.extra.get("band_mark", "#7a3a14"))
        pt("qband", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else pick(mark if (x + y * 2) % 6 < 2 else band, 0.55, x, y))
    if d.get("seafin"):                                  # see-through fins with rays, painted only on their broad sides
        fin, ray = G.shades(pal.extra.get("fin", "#d8ecff")), G.shades(pal.extra.get("fin_ray", "#5aa8f0"))

        def fan(f, x, y, w, h):
            if f not in ("east", "west"):
                return CLEAR
            if y > h * 0.55 + abs(x + 0.5 - w / 2) * 0.5 and y > 1:     # the fan's notched trailing edge
                return CLEAR
            return pick(ray, 0.5, x, y) if x % 2 == 0 else G.with_alpha(pick(fin, 0.6, x, y), 220)
        pt("nfan", "all", fan)
        pt("nfin", "all", lambda f, x, y, w, h: CLEAR if f not in ("east", "west") or y > h - 1 - x * 0.6
           else pick(ray, 0.5, x, y) if x % 2 == 0 else G.with_alpha(pick(fin, 0.6, x, y), 220))
    plume, ptip = G.shades(pal.extra.get("plume", "#3a6ae0")), G.shades(pal.extra.get("plume_tip", "#c8ccd8"))

    def feather(f, x, y, w, h):                          # a long feather: a pale shaft, barbs, a tip of the second colour
        along = y if f in ("east", "west", "north", "south") else y
        tip = along > h * 0.75
        if f in ("east", "west", "up", "down") and x == w // 2:
            return pick(ptip, 0.75, x, y)
        return pick(ptip if tip else plume, 0.5 + 0.15 * ((x + y) % 3 == 0), x, y)
    for p in ("mcrest", "mplume"):
        pt(p, "all", feather)
    beak = G.shades(pal.extra.get("beak", "#2a2a30"))
    pt("mbeak", "all", lambda f, x, y, w, h: pick(beak, 0.45 + 0.2 * (f == "up") + 0.1 * (x == 0 or x == w - 1), x, y))
    pt("mbeak_tip", "all", lambda f, x, y, w, h: pick(beak, 0.35, x, y))
    comb = G.shades(pal.extra.get("comb", "#d8201e"))
    pt("comb", "all", lambda f, x, y, w, h: CLEAR if f in ("east", "west") and y < 2 and x % 3 == 1 else pick(comb, 0.5 + 0.2 * (f == "up"), x, y))
    pt("wattle", "all", lambda f, x, y, w, h: pick(comb, 0.45, x, y))
    frill, edge = G.shades(pal.extra.get("frill", "#7aa8d8")), G.shades(pal.extra.get("frill_edge", "#f0e0c0"))

    def tfr(f, x, y, w, h):                              # a half-disc fan plate, rimmed, spotted
        if f not in ("north", "south"):
            return CLEAR
        dx = (x + 0.5 - w / 2) / (w / 2)
        top = (h - 1) * dx * dx
        if y < top:
            return CLEAR
        if y < top + 1.5:
            return pick(edge, 0.6, x, y)
        return pick(pal.MARK, 0.45, x, y) if (x * 7 + y * 5) % 11 == 0 else pick(frill, 0.55 + 0.1 * ((x + y) % 4 == 0), x, y)
    pt("tfrill", "all", tfr)
    horn = G.shades(pal.extra.get("horn", "#f0e0c0"))
    for p in ("tfrill_knob", "brow_horn", "nose_horn"):
        pt(p, "all", lambda f, x, y, w, h: pick(horn, 0.55 + 0.15 * (y < 1), x, y))
    crest = G.shades(pal.extra.get("crest", "#e8c040"))
    pt("ptcrest", "all", lambda f, x, y, w, h: pick(crest, 0.5 + 0.2 * (y == h - 1) - 0.15 * (x % 3 == 0 and f in ("east", "west")), x, y))
    sc = G.scale_fn(pal)
    for p in [n for n, *_ in SEGS] + ["neck_a", "neck_b", "dhead", "dsnout"]:
        pt(p, "all", sc)
    pt("seg_1", "all", lambda f, x, y, w, h: H.skin_fn(pal, 0.66)(f, x, y, w, h) if y < 2 and f != "down" else sc(f, x, y, w, h))
    dark = G.shades(G.mix(pal.MARK[3][:3], pal.FUR[3][:3], 0.5))
    pt("dhead", "all", lambda f, x, y, w, h: pick(dark, 0.45 + 0.2 * ((x + y) % 3 == 0), x, y))
    pt("dhead", ("east", "west"), lambda f, x, y, w, h: pick(G.shades("#f8d83a"), 0.7, x, y) if y == 2 and 1 <= x <= 2 else None)
    pt("dsnout", "all", lambda f, x, y, w, h: pick(dark, 0.55, x, y) if f != "down" else pick(G.shades("#c83a3a"), 0.5, x, y))
    for p in ("neck_a", "neck_b"):
        pt(p, ("north",), lambda f, x, y, w, h: pick(pal.LIGHT, 0.55 + 0.2 * (y % 2), x, y))
    pt("mane", "all", lambda f, x, y, w, h: pick(pal.HAIR, 0.5 + 0.25 * (x % 2), x, y))
    tal = G.shades(pal.extra.get("talon", "#e8c040"))
    for p in ("uleg_f", "lleg_f"):
        pt(p, "all", lambda f, x, y, w, h: pick(tal, 0.5 + 0.2 * (y % 2), x, y))
    fur = G.fur_fn(pal, light_faces=("down",), grain=0.1, seed=31)
    for p in ("barrel", "uleg_b", "lleg_b", "withers"):
        pt(p, "all", fur)
    pt("hoof", "all", lambda f, x, y, w, h: pick(G.shades("#2a2420"), 0.4, x, y))
    pt("htail", "all", lambda f, x, y, w, h: pick(pal.MARK if y > h * 0.7 else pal.FUR, 0.5, x, y))
    if pal.extra.get("dot"):                              # the alebrije's folk-art paint: bright dots and zigzags over every scale
        dot, zig = G.shades(pal.extra["dot"]), G.shades(pal.extra["zig"])

        def folk(f, x, y, w, h):
            if (y + abs(x % 6 - 3)) % 7 == 0:
                return pick(zig, 0.6, x, y)
            if (x * 3 + y * 5) % 11 == 0:
                return pick(dot, 0.7, x, y)
            return None
        for p in ["thigh", "shin", "paw", "tail_a", "tail_b", "tail_c", "tail_d"] + [n for n, *_ in P.WING_KINDS["dragon"]]:
            pt(p, ("north", "south", "east", "west", "up"), folk)


def build_hook(r, g, d):
    if d.get("rearing"):
        serpent_rig.build(r)
    hy, z = g.hy, g.z
    if d.get("comb"):
        r.add("hair_comb", "head", [0, hy + 8, z], [cube("comb", [-0.5, hy + 8.0, -3.5 + z])])
    if d.get("trifrill"):                                # the frill behind her head, knobs round its rim, two brow horns and a nose horn
        piv = [0, hy + 6, 4.6 + z]
        knobs = [G.rot_cube("tfrill_knob", [5.5 * math.sin(math.radians(a)) - 0.5, hy + 5.0 + 7.0 * math.cos(math.radians(a)) - 0.5, 4.5 + z], piv, -25, 0, 0)
                 for a in (-80, -45, 0, 45, 80)]
        r.add("hair_frill", "head", piv, [G.rot_cube("tfrill", [-5.5, hy + 5.0, 4.6 + z], piv, -25, 0, 0)] + knobs)
        r.put("head", G.rot_cube("brow_horn", [-2.7, hy + 6.4, -4.4 + z], [-2.2, hy + 6.4, -4.0 + z], -55, 0, 10),
              G.rot_cube("brow_horn", [1.7, hy + 6.4, -4.4 + z], [2.2, hy + 6.4, -4.0 + z], -55, 0, -10),
              G.rot_cube("nose_horn", [-0.5, hy + 2.4, -4.8 + z], [0, hy + 2.4, -4.3 + z], -40, 0, 0))
    if d.get("ptcrest"):                                 # swept straight back from the crown of her head
        r.add("hair_ptcrest", "head", [0, hy + 8.2, z], [G.rot_cube("ptcrest", [-0.5, hy + 7.4, 1.0 + z], [0, hy + 8.2, 1.0 + z], 20, 0, 0)])
    if d.get("seafin") and "tail_3" in r.b:              # a tall fan of a fin on the tip of her tail, a crest of fin on her head
        px, py, pz = r.b["tail_3"]["pivot"]
        r.put("tail_3", cube("nfan", [px - 0.25, py - 3.0, pz + 2.0]))
        r.add("hair_nfin", "head", [0, hy + 8, z], [cube("nfin", [-0.25, hy + 7.4, -1.5 + z])])
    if d.get("qcrest"):                                  # leaves fanned round the back of her head, the woven band over her brow
        cs = [cube("qband", [-4.625, hy + 5.4, -4.625 + z])]
        for i in range(9):
            tilt = -84 + i * 21
            cs.append(G.rot_cube("qleaf", [-0.875, hy + 7.0, 3.0 + z], [0, hy + 4.0, 3.0 + z], -14, 0, tilt))
        r.add("hair_qcrest", "head", [0, hy + 4, 3 + z], cs)
    if d.get("macaw"):                                   # plumes fanned up and back, a hooked beak between her eyes, two plumes off the tail tip
        r.add("hair_mcrest", "head", [0, hy + 8.2, z], [G.rot_cube("mcrest", [x - 0.25, hy + 7.8, -1.5 + z], [x, hy + 8.2, -1.0 + z], 30 + 8 * abs(x), x * 12, 0)
                                                       for x in (-1.6, -0.8, 0, 0.8, 1.6)])
        r.put("head", cube("mbeak", [-0.5, hy + 3.25, -4.9 + z]), cube("mbeak_tip", [-0.375, hy + 2.75, -5.15 + z]))
        last = serpent_rig.tip()
        px, py, pz = r.b[last]["pivot"]
        py -= 4.5                                        # the end of her tail: the plumes carry on down the line of her body
        r.put(last, *[G.rot_cube("mplume", [px + dx - 0.625, py - 0.125, pz], [px + dx, py, pz], 90, 0, dx * 14) for dx in (-0.5, 0.5)])
    if d.get("heads"):                                    # seven dragon heads rising behind and around her own
        for i in range(7):
            ry = -135 + i * 45                              # fanned round the sides and back, none in front of her face
            rx = 138 + (i % 2) * 10
            base = (round(2.6 * math.sin(math.radians(ry)), 2), 18.5, round(1.0 + 2.0 * math.cos(math.radians(ry)), 2))
            names = P.chain(r, f"halo_neck{i + 1}", "body", base, ["neck_a", "neck_b"], rx=rx, ry=ry + 180, bend=8)
            last = names[-1]
            px, py, pz = r.b[last]["pivot"]
            # the head sits at the end of the neck: find it by rotating the neck's end point like the renderer does
            end = H.rot_about((px, py - 7.75, pz), (px, py, pz), H.rot_matrix(rx + 8, ry + 180, 0))
            hx, hyy, hz = end
            r.add(f"halo_head{i + 1}", last, [round(hx, 2), round(hyy, 2), round(hz, 2)],
                  [G.rot_cube("dhead", [hx - 1.75, hyy - 1.5, hz - 2.0], [hx, hyy, hz], 0, ry, 0),
                   G.rot_cube("dsnout", [hx - 1.125, hyy - 1.5, hz - 4.3], [hx, hyy, hz], 0, ry, 0)])


# ----------------------------------------------------------------------------- serpents and the gryphon
def serpent_declare(sp, d):
    parts = list(d["segs"]) + P.horn_parts(d.get("horns")) + declare_hook(sp, d)
    G.declare(parts, ears=d["ears"], hair=d["hair"], build=d["build"], outfit=d["outfit"])


def serpent_build(sp, d):
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    g = G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), build=d["build"], outfit=d["outfit"], ears=d["ears"], skirt=False, cuffs=False))
    G.serpent(r, None, d)
    if d.get("horns"):
        P.horns(r, d["horns"], g.hy, g.z)
    if d.get("mane"):
        for i in range(2, 8):
            n = f"seg_{i}"
            w, h, dd = G.S(n)
            pz = r.b[n]["pivot"][2]
            r.put(n, cube("mane", [-0.5, h - 0.25, pz + 1.0]))
    return r.model(sp)


def serpent_anims(an, flies):
    segs = [f"seg_{i}" for i in range(1, 9)]
    G.serpent_anims(an, segs)
    an.add("call", 1.5, False, H.merge({"head": {"rotation": H.hold(-20, 0, 0)}, "eyelids": {"position": H.hold(*H.OUT_)}},
                                        H.sym("arm", H.track((0, (0, 0, 0)), (0.5, (-170, 0, 10)), (1.2, (-170, 0, 10)), (1.5, (0, 0, 0))))))
    if flies:
        Tk, hold, merge = H.track, H.hold, H.merge
        flat = [(80, 0, 0), (-80, 0, 0)] + [(0, 0, 0)] * 6      # seg_1 swings back; seg_2 turns the rest of the chain level again
        an.add("fly", 3.0, True, merge(G.upper_idle(), {"root": {"position": hold(0, 6, 0)}, "body": {"rotation": hold(-10, 0, 0)}},
                                       G.wave(segs, 3.0, [0, 12, 16, 18, 20, 22, 24, 26], axis=1, phase=0.8, base=flat),
                                       H.sym("arm", hold(-20, 0, 30))))


def gryphon_declare(sp, d):
    parts = [("barrel", 7, 7, 15), ("withers", 5, 2.5, 5), ("uleg_f", 2.25, 6.5, 2.75), ("lleg_f", 1.75, 5.75, 1.75),
             ("uleg_b", 2.5, 6.5, 3.25), ("lleg_b", 1.75, 5.75, 1.75), ("hoof", 2.25, 1.25, 2.25), ("htail", 1.5, 8, 1.5)]
    G.declare(parts + P.wing_parts("feather", 1.2), ears=d["ears"], hair=d["hair"], build=d["build"], outfit=d["outfit"])


def gryphon_build(sp, d):
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    G.upper(r, 19.5, dict(hair=d["hair"], bangs=d.get("bangs"), build=d["build"], outfit=d["outfit"], ears=d["ears"], skirt=False), z0=-2.25)
    G.taur(r, None, d)
    P.wings(r, "feather", "barrel", y=19.0, z=-1.0, root_x=2.5)
    return r.model(sp)


def gryphon_anims(an):
    G.taur_anims(an)
    names = ["wing_r", "wing_l"]
    for key in ("idle", "walk", "sit", "lie", "rear", "kick", "graze"):
        F._inject(an, key, P.folded(names, 70, -10))
    F._inject(an, "run", P.flap(names, 0.6, 30, 20))
    hold, merge = H.hold, H.merge
    an.add("fly", 1.2, True, merge(G.upper_idle(), P.flap(names, 1.2, 45, 35, sweep=5),
                                   {"leg_f_r": {"rotation": hold(-50, 0, 0)}, "leg_f_l": {"rotation": hold(-50, 0, 0)},
                                    "leg_b_r": {"rotation": hold(50, 0, 0)}, "leg_b_l": {"rotation": hold(50, 0, 0)}, "tail": {"rotation": hold(60, 0, 0)}}))


def anim_extra(an, sp, d):
    """fire breath ("breathe"), the sting ("sting"), Kannae's lightning call ("call")"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    if d.get("rearing"):
        serpent_rig.anims(an)
        folded = P.folded(["wing_r", "wing_l"], fold=62, droop=-84)
        for key in ("idle", "walk", "run", "sit", "rear", "coil", "bask", "lie"):
            F._inject(an, key, folded)
        F._inject(an, "fly", merge(serpent_rig.posed(serpent_rig.FLAT), {"root": {"position": hold(0, 10, 14)}}, serpent_rig.slither(1.6, 22)))
    an.add("breathe", 1.5, False, merge({"head": {"rotation": Tk((0, (0, 0, 0)), (0.3, (-25, 0, 0)), (0.6, (10, 0, 0)), (1.5, (10, 0, 0)))},
                                         "body": {"rotation": Tk((0, (0, 0, 0)), (0.3, (-10, 0, 0)), (0.6, (12, 0, 0)), (1.5, (0, 0, 0)))},
                                         "mouth": {"position": Tk((0, H.REST), (0.5, H.OUT_), (1.3, H.OUT_), (1.5, H.REST))}}))
    tails = ["tail", "tail_2", "tail_3", "tail_4"]
    an.add("sting", 0.8, False, merge({t: {"rotation": Tk((0, (0, 0, 0)), (0.3, (-60, 0, 0)), (0.5, (40, 0, 0)), (0.8, (0, 0, 0)))} for t in tails}))
    an.add("call", 1.5, False, merge({"head": {"rotation": hold(-20, 0, 0)}, "eyelids": {"position": hold(*H.OUT_)}},
                                      sym("arm", Tk((0, (0, 0, 0)), (0.5, (-170, 0, 10)), (1.2, (-170, 0, 10)), (1.5, (0, 0, 0))))))


# homage looks of the twenty-eighth wave (POP_CULTURE_VARIANTS.md)
SPECIES["wyrm_dragon"]["variants"]["purple_flame"] = dict(HAIR="#e8b83a", FUR="#8a3ac8", LIGHT="#e8c050", MARK="#5a1e8a", EYE="#c83a8a", CLOTH="#e8b83a", TRIM="#5a1e8a")
SPECIES["gryphon_dragon"]["variants"]["night_wing"] = dict(HAIR="#1a1a22", FUR="#22222c", LIGHT="#3a3a48", MARK="#101018", EYE="#4ae05a", CLOTH="#3a2a22", TRIM="#14141c")


# homage looks of the thirtieth wave (POP_CULTURE_VARIANTS.md)
SPECIES["long_dragon"]["variants"]["wish_dragon"] = dict(HAIR="#1e6a3a", FUR="#2a8a4a", LIGHT="#c8e8a0", MARK="#14482a", EYE="#e82a2a", CLOTH="#e8c050", TRIM="#14482a")


if __name__ == "__main__":
    import family_concepts as FC
    want = [a for a in sys.argv[1:] if not a.startswith("--")]
    prev = "--previews" in sys.argv
    heights = {s["id"]: s["height"] for s in FC.family("dragon")["species"]}
    entries = {}

    def custom(sp, decl, bld, anm):
        d = SPECIES[sp]
        decl(sp, d)
        tex = F.paint(sp, d, F.pal_for(d), paint_hook)
        geo, adoc, texes = G.write(sp, tex, bld(sp, d), anm, list(d["variants"]), lambda k: F.paint(sp, d, F.pal_for(d, k), paint_hook))
        h = G.model_height(geo)
        if prev:
            G.previews(sp, geo, adoc, texes, F.VIEWS, F.FACE, h)
        entries[sp] = (geo, tex, adoc["animations"][f"animation.{sp}.idle"], heights[sp], h)

    for sp in ("long_dragon", "jormungandr_dragon"):
        if not want or sp in want:
            custom(sp, serpent_declare, serpent_build, lambda an, sp=sp: serpent_anims(an, SPECIES[sp].get("flies")))
    if not want or "gryphon_dragon" in want:
        custom("gryphon_dragon", gryphon_declare, gryphon_build, gryphon_anims)
    for e, sp in zip(F.run_family("dragon", BIPEDS, declare_hook, paint_hook, build_hook, anim_extra, only=[k for k in BIPEDS if not want or k in want]),
                     [s["id"] for s in FC.family("dragon")["species"] if s["id"] in BIPEDS and (not want or s["id"] in want)]):
        entries[sp] = e
    if prev and not want:
        G.roster(os.path.join(G.PREVIEWS, "dragon_roster.png"), [entries[s["id"]] for s in FC.family("dragon")["species"]])
