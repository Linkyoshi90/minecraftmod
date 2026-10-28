#!/usr/bin/env python3
"""Merfolk (the sea family, see merfolk_field_guide.html): orca, koi, shark, seal, axolotl, dolphin.

    python tools/gen_merfolk.py [species ...] [--previews]

Shared girl_lib upper body on a fish / sea-mammal tail: fin_1 down from the waist, fin_2..fin_4 along the ground and a
fluke (horizontal for the orca, seal and axolotl, a vertical tail fin for the koi and shark). Extras: a dorsal fin
(orca, shark, dolphin, axolotl crest), long trailing fins (koi), fin ears (koi, shark, orca, dolphin), gill fronds
(axolotl). Every model has a `seashells` bone (two scallop shells over the bust) that the game shows once she has found
shells on the sea floor. Five colours per species. The ahuizotl (a river otter) has fur, round ears and a hand at the
end of her tail instead of a fluke (`hand`), for stealing fish off fishing lines.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, shades, CLEAR  # noqa: E402

SPECIES = {
    # twentieth wave: the doggo, a sea dog: a shaggy blue pup's ears and hair, a white chest, a sparkling fish's tail
    "doggo_mermaid": dict(
        build="petite", outfit=dict(top=["bandeau"], extras=["pearls"], colors=dict(cloth2="#3ac8d8")),
        ears="big", hair="bob", bangs="messy", fluke=(7, .75, 3.5), scales=True,
        pal=dict(SKIN=skin("fair"), HAIR="#3ab0d8", FUR="#3aa8d0", LIGHT="#e8f8fc", MARK="#1e6a9a", EYE="#2a2a4a", CLOTH="#3ac8d8", TRIM="#f0f8fc",
                 INNER="#a8d8e8", pattern="solid"),
        variants={"golden": dict(HAIR="#d8a050", FUR="#c8a058", LIGHT="#fcf0dc", MARK="#8a5a24", EYE="#3a2412", CLOTH="#3a8ad8", INNER="#e8c8a0"),
                  "black": dict(HAIR="#2a2a30", FUR="#2a2e3a", LIGHT="#e8e8f0", MARK="#0e1018", EYE="#c88a2a", CLOTH="#c83a3a", INNER="#8a8a98"),
                  "white": dict(HAIR="#f4f6fa", FUR="#e0eef4", LIGHT="#ffffff", MARK="#8ab8d0", EYE="#3a6ac8", CLOTH="#f87a9a", INNER="#f0c8d0"),
                  "pink": dict(HAIR="#f0a0c0", FUR="#e8a8c8", LIGHT="#fcecf4", MARK="#a83a6a", EYE="#5a2a4a", CLOTH="#7ad8c8", INNER="#f8d0e0")}),
    # nineteenth wave: the moopie, a sea cow: cream and brown spots, cow's ears and little horns, a mane of sea-green curls
    "moopie_mermaid": dict(
        build="curvy", outfit=dict(top=["bandeau"], extras=["pearls", "bell_collar"], colors=dict(cloth2="#3ab89a")),
        ears="cow", hair="wavy", bangs="swept", fluke=(7, .75, 3.5), size=1.1, cowhorns=True,
        pal=dict(SKIN=skin("fair"), HAIR="#3ab89a", FUR="#e8dcc4", LIGHT="#f8f0e0", MARK="#8a5a3a", EYE="#7a4ab8", CLOTH="#3ab89a", TRIM="#f0d070",
                 INNER="#f0a8b0", pattern="patches", extra=dict(horn="#e8dcc0")),
        variants={"brown": dict(HAIR="#7a4a2a", FUR="#a8784a", MARK="#5a3a1e", CLOTH="#c87a3a"),
                  "black": dict(HAIR="#2a2a30", FUR="#f4f2ee", MARK="#1e1e22", EYE="#3a6ac8", CLOTH="#2a2a30"),
                  "pink": dict(HAIR="#f0a0c0", FUR="#f8e0e8", MARK="#d87098", EYE="#3a8ad8", CLOTH="#f0a0c0"),
                  "blue": dict(HAIR="#3a6ad8", FUR="#d8e4f8", MARK="#3a5aa8", EYE="#e8a030", CLOTH="#3a6ad8")}),
    # eighteenth wave: the rusalka, the water spirit of Slavic rivers, pale, long loose green hair, a white shift
    "rusalka_mermaid": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], extras=["pearls"], colors=dict(cloth2="#e8ece4")),
        ears="human", hair="flowing", bangs="wispy", fluke=(.5, 5.5, 5), long_fins=True, scales=True,
        pal=dict(SKIN=skin("pale"), HAIR="#5a9a6a", FUR="#7a9a8a", LIGHT="#d8ece0", MARK="#3a5a4a", EYE="#a8e0e8", CLOTH="#f0f2ec", TRIM="#a8c8a0",
                 pattern="solid"),
        variants={"moonlit": dict(HAIR="#d8e0ec", FUR="#a8b4c4", LIGHT="#f4f8ff", MARK="#6a748a", EYE="#c8d8ff", CLOTH="#e8ecf8"),
                  "reed": dict(HAIR="#c8a85a", FUR="#a89a6a", LIGHT="#f0e8c8", MARK="#6a5a2e", EYE="#5a8a4a", CLOTH="#f4ecd8"),
                  "ash": dict(HAIR="#5a5a5e", FUR="#6a6a70", LIGHT="#c8c8cc", MARK="#2a2a2e", EYE="#c8d0d8", CLOTH="#d8d8dc"),
                  "lily": dict(HAIR="#e8a0b8", FUR="#c8a0b0", LIGHT="#fcecf2", MARK="#8a5a6a", EYE="#5ab0a0", CLOTH="#fff4f8")}),
    # seventeenth wave: the sawfin, a sawfish girl in blue barding with a fleur-de-lis shield, her saw of a snout out in front
    "sawfin_mermaid": dict(
        build="athletic", outfit=dict(top=["bandeau", "harness"], extras=["pauldron", "belt"], colors=dict(leather="#3a5ab0")),
        ears="fin", hair="shaggy", bangs="swept", fluke=(.75, 7, 5), dorsal=(.75, 5, 4), saw=True,
        pal=dict(SKIN=skin("fair"), HAIR="#4a6ab8", FUR="#5a7ac8", LIGHT="#f0ece4", MARK="#2a4a9a", EYE="#2a4a9a", CLOTH="#3a5ab0", TRIM="#e8e8f0",
                 pattern="solid", extra=dict(belly_white=True, gills=True, saw="#f0ece4", shield="#3a5ab0", lily="#e8e8f0")),
        variants={"grey": dict(FUR="#7a8490", HAIR="#5a6470", MARK="#3a424c", CLOTH="#2a3a4a",
                               extra=dict(belly_white=True, gills=True, saw="#e8e4dc", shield="#8a2a2a", lily="#e8c040")),
                  "green": dict(FUR="#5a8a6a", HAIR="#3a6a4a", MARK="#1e4a2e", CLOTH="#2a5a3a",
                                extra=dict(belly_white=True, gills=True, saw="#f0ece0", shield="#2a5a3a", lily="#e8c040")),
                  "sand": dict(FUR="#b8a070", HAIR="#8a7048", MARK="#5a4a2a", CLOTH="#5a3a2a",
                               extra=dict(belly_white=True, gills=True, saw="#fff4e0", shield="#5a3a2a", lily="#f0e0b0")),
                  "royal": dict(FUR="#3a2a8a", HAIR="#2a1e6a", MARK="#141040", CLOTH="#e8c040",
                                extra=dict(belly_white=True, gills=True, saw="#f0ece4", shield="#e8c040", lily="#3a2a8a"))}),
    # sixteenth wave: the sword koi, a lapis-blue koi patched with white, gold-trimmed armour and a katana slung across her back
    "sword_koi_mermaid": dict(
        build="athletic", outfit=dict(top=["sarashi"], extras=["pauldron", "arm_wraps"], colors=dict(bandage="#2a4aa8", leather="#d8b040")),
        ears="fin", hair="ponytail", bangs="swept", fluke=(.5, 6.5, 5.5), long_fins=True, dorsal=(.5, 8, 4.5), scales=True, katana=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e3a98", FUR="#2a4ab8", LIGHT="#e8f0fa", MARK="#e4ecf8", EYE="#2a5ac8", CLOTH="#2a4aa8", TRIM="#d8b040",
                 pattern="patches", extra=dict(fin_mark=True, saya="#1e2a6a", tsuka="#e8e0d0")),
        variants={"kohaku": dict(FUR="#f6f4f0", MARK="#d8302a", HAIR="#f4f2ee", LIGHT="#ffffff", EYE="#c82a2a", CLOTH="#f4f0e8", TRIM="#d8302a",
                                 extra=dict(fin_mark=True, saya="#1a1a1c", tsuka="#d8302a")),
                  "gold": dict(FUR="#e8c050", MARK="#f8e090", HAIR="#f0d070", pattern="speckle", extra=dict(fin_mark=True, saya="#5a3a1a", tsuka="#1a1a1c")),
                  "black": dict(FUR="#1a1a1c", MARK="#d8302a", HAIR="#1a1a1c", CLOTH="#2a2a2e", extra=dict(fin_mark=True, saya="#8a1e1e", tsuka="#e8c040")),
                  "silver": dict(FUR="#c8ccd4", MARK="#f4f6f8", HAIR="#d8dce4", pattern="speckle", extra=dict(fin_mark=True, saya="#3a4a6a", tsuka="#f4f6f8"))}),
    "orca_mermaid": dict(
        build="strong", outfit=dict(top=["sports"], extras=["armlets"]),
        ears="fin", hair="ponytail", fluke=(9, .75, 4), dorsal=(.75, 6, 3.5), size=1.15,
        pal=dict(SKIN=skin("fair"), HAIR="#141418", FUR="#141418", LIGHT="#f4f4f2", MARK="#8a8c90", EYE="#2a3a5a", CLOTH="#e8e8e8", TRIM="#4a6a8a",
                 pattern="solid", extra=dict(belly_white=True)),
        variants={"transient": dict(MARK="#5a5c60", HAIR="#1a1a1e"), "typeb": dict(FUR="#5a5e64", LIGHT="#ece4d0", HAIR="#3a3e44"),
                  "white": dict(FUR="#f2f2f0", HAIR="#f8f8f8", MARK="#c8c8cc", CLOTH="#4a6a8a", TRIM="#e8e8e8"),
                  "typed": dict(FUR="#1e1e24", HAIR="#2a2a30", MARK="#3a3a40")}),
    "koi_mermaid": dict(
        build="slim", outfit=dict(top=["kimono"], colors=dict(kimono="#f4f0ea", kimono_trim="#d8402a", obi="#d8402a")),
        ears="fin", hair="flowing", fluke=(.5, 6, 5), long_fins=True,
        pal=dict(SKIN=skin("pale"), HAIR="#f4f2ee", FUR="#f6f4f0", LIGHT="#ffffff", MARK="#d8402a", EYE="#c83a2a", CLOTH="#d8402a", TRIM="#f0d070",
                 pattern="patches"),
        variants={"showa": dict(FUR="#1a1a1c", MARK="#d8402a", HAIR="#1a1a1c", pattern="patches"),
                  "ogon": dict(FUR="#e8c050", MARK="#f8e090", HAIR="#f0d070", pattern="speckle", CLOTH="#c89a30"),
                  "asagi": dict(FUR="#6a88a8", MARK="#e0603a", HAIR="#8aa4c0", pattern="net", CLOTH="#3a5a8a"),
                  "tancho": dict(pattern="solid", HAIR="#f8f6f2", extra=dict(spot=True))}),
    "shark_mermaid": dict(
        build="athletic", outfit=dict(top=["bandeau", "harness"], extras=["pauldron", "belt"]),
        ears="fin", hair="shaggy", fluke=(.75, 7, 5), dorsal=(.75, 5, 4), size=1.1,
        pal=dict(SKIN=skin("grey"), HAIR="#5a6470", FUR="#7a8490", LIGHT="#f0f0ee", MARK="#3a424c", EYE="#2a2a30", CLOTH="#2a3a4a", TRIM="#e8e0c8",
                 pattern="solid", extra=dict(belly_white=True, gills=True)),
        variants={"tiger": dict(FUR="#8a7a62", MARK="#3a3024", pattern="stripes", HAIR="#6a5a46"),
                  "whale": dict(FUR="#2a3a5a", MARK="#f0f0ea", pattern="spots", HAIR="#3a4a6a", CLOTH="#c8b880"),
                  "mako": dict(FUR="#2a4a8a", HAIR="#3a5a9a", MARK="#1a2a5a"),
                  "lemon": dict(FUR="#b8a868", HAIR="#c8b878", MARK="#8a7a48", CLOTH="#3a5a3a"),
                  # homage (2026-10-28): Gawr Gura (VTuber): white hair with blue streaks, a blue hoodie
                  "trident_hood": dict(HAIR="#f4f6fa", FUR="#5a8ad8", MARK="#2a4a9a", LIGHT="#f0f4fa", CLOTH="#5a8ad8", TRIM="#f4f6fa", EYE="#3a8ae8")}),
    "seal_mermaid": dict(
        build="curvy", outfit=dict(top=["bandeau"], extras=["pearls"]),
        ears=None, hair="bob", fluke=(7, .75, 3.5), seal=True,
        pal=dict(SKIN=skin("fair"), HAIR="#6a6a6e", FUR="#8a8a8e", LIGHT="#c8c8c8", MARK="#3a3a3e", EYE="#1e1a18", CLOTH="#e8e4dc", TRIM="#7a9ab0",
                 pattern="smallspots"),
        variants={"grey": dict(FUR="#5a5a5e", MARK="#2a2a2e", pattern="blotches", HAIR="#4a4a4e"),
                  "harp": dict(FUR="#e2e4e6", MARK="#2a2a2e", pattern="cross", HAIR="#f0f0f2", LIGHT="#ffffff"),
                  "leopard": dict(FUR="#6a6e74", MARK="#2a2c30", pattern="spots", HAIR="#5a5e64", CLOTH="#8a3a3a"),
                  "ringed": dict(FUR="#4a4a50", MARK="#a8a8ae", pattern="rosettes", HAIR="#3a3a40")}),
    "axolotl_mermaid": dict(
        build="petite", outfit=dict(top=["shells"], extras=["pearls"]),
        ears="gill", hair="odango", fluke=(4.5, .5, 3), dorsal=(.5, 2.5, 9), axolotl=True,
        pal=dict(SKIN=skin("pale"), HAIR="#f4c4cc", FUR="#f4c4cc", LIGHT="#fadce2", MARK="#d8586a", EYE="#1a1a1e", CLOTH="#e86a8a", TRIM="#f8e0e8",
                 pattern="solid", INNER="#d8405a"),
        variants={"wild": dict(FUR="#6a5a3a", HAIR="#7a6a48", LIGHT="#8a7a58", MARK="#3a2e1e", pattern="speckle", INNER="#8a4a4a", CLOTH="#4a6a3a"),
                  "gold": dict(FUR="#f0c858", HAIR="#f4d070", LIGHT="#f8e4a0", INNER="#e8a040", CLOTH="#c87a30"),
                  "cyan": dict(FUR="#c8eef4", HAIR="#d8f4f8", LIGHT="#eafafc", INNER="#e86a8a", CLOTH="#5ab0c8"),
                  "blue": dict(FUR="#4a5ac8", HAIR="#5a6ad8", LIGHT="#7a88e0", MARK="#2a3a8a", INNER="#8a5ad8", CLOTH="#3a3a8a"),
                  # homage (2026-10-28): Mudkip (Pokemon): blue with orange cheek gills, a pale belly
                  "mud_fin": dict(FUR="#5aa8e8", HAIR="#5aa8e8", LIGHT="#c8e8f8", MARK="#2a6ab8", INNER="#f8902a", CLOTH="#2a6ab8", EYE="#1e1a18")}),
    "dolphin_mermaid": dict(
        build="slim", outfit=dict(top=["halter"], extras=["armlets"]),
        ears="fin", hair="twintails", bangs="swept", fluke=(8, .75, 3.5), dorsal=(.75, 4.5, 3.5), size=1.05,
        pal=dict(SKIN=skin("fair"), HAIR="#6a7e92", FUR="#7a90a6", LIGHT="#e8eef2", MARK="#4a5c70", EYE="#2a4a6a", CLOTH="#2aa0a8", TRIM="#f0e8d0",
                 pattern="solid", extra=dict(belly_white=True)),
        variants={"common": dict(FUR="#2e3a4a", MARK="#e8d8a0", LIGHT="#f2ecd8", HAIR="#3a4656", pattern="patches", CLOTH="#e8b040"),
                  "spinner": dict(FUR="#5a6676", MARK="#9aa4b0", HAIR="#6a7686", CLOTH="#8a4ab0"),
                  "rissos": dict(FUR="#a8acb0", MARK="#f0f0ee", HAIR="#c8ccd0", pattern="speckle", CLOTH="#3a5a8a"),
                  "pink": dict(FUR="#e8a8b4", HAIR="#f0bcc6", LIGHT="#f8dce2", MARK="#d08898", CLOTH="#3a8a7a")}),
    "ahuizotl_mermaid": dict(
        build="athletic", outfit=dict(top=["sports"], extras=["armlets"], colors=dict(cloth2="#2a6a6a")),
        ears="round", hair="shaggy", bangs="messy", fluke=(3, .75, 3.5), hand=True, size=0.95,
        pal=dict(SKIN=skin("tan"), HAIR="#5a3e2a", FUR="#6a4a32", LIGHT="#c8a888", MARK="#3a2818", EYE="#2a1a10", CLOTH="#2a8a8a", TRIM="#e8c060",
                 pattern="solid", extra=dict(belly_white=True)),
        variants={"dark": dict(FUR="#2e221a", HAIR="#1e1610", LIGHT="#7a6450", MARK="#14100c"),
                  "golden": dict(FUR="#a8783a", HAIR="#8a5a2a", LIGHT="#e8c890", MARK="#5a3a1a", CLOTH="#3a5a9a"),
                  "grey": dict(FUR="#6a6460", HAIR="#4a4440", LIGHT="#c8c0b8", MARK="#2e2a28", CLOTH="#9a3a3a"),
                  "jade": dict(FUR="#4a5a3a", HAIR="#2e3a22", LIGHT="#a8b888", MARK="#1e2614", CLOTH="#c8a040")}),
    "dugong_mermaid": dict(
        build="curvy", outfit=dict(top=["bandeau"], extras=["pearls"]),
        ears=None, hair="wavy", bangs="swept", fluke=(9, .75, 3.5), size=1.15,
        pal=dict(SKIN=skin("fair"), HAIR="#5a4a42", FUR="#9a9a9c", LIGHT="#d8d4d0", MARK="#7a7a7e", EYE="#3a3a4a", CLOTH="#3a9a6a", TRIM="#e8e0c8",
                 pattern="solid"),
        variants={"brown": dict(FUR="#8a7a6a", HAIR="#7a6a5a", MARK="#6a5a4a"),
                  "pale": dict(FUR="#e0d8cc", HAIR="#ece4d8", LIGHT="#ffffff", MARK="#c8c0b4"),
                  "slate": dict(FUR="#5a6068", HAIR="#4a5058", MARK="#3a4048", CLOTH="#c8a040"),
                  "sandy": dict(FUR="#c8b490", HAIR="#b8a07a", MARK="#a08a64", CLOTH="#2a7a8a")}),
    # fifteenth wave: the reeffin, a blue reef mermaid crowned with branching coral, long trailing fins
    "reeffin_mermaid": dict(
        build="slim", outfit=dict(top=["halter"], extras=["pearls", "armlets"], colors=dict(cloth2="#e86a5a")),
        ears="fin", hair="flowing", bangs="wispy", fluke=(.5, 6.5, 5.5), long_fins=True, scales=True, coral=True,
        pal=dict(SKIN=skin("#8ab0e0"), HAIR="#2a5ab8", FUR="#3a7ad8", LIGHT="#c8e0f8", MARK="#1e3a8a", EYE="#f8d040", CLOTH="#f07a6a", TRIM="#f8d8a0",
                 pattern="solid", extra=dict(coral="#f86a7a", coral_tip="#f8c8a0")),
        variants={"pink": dict(HAIR="#e86a9a", FUR="#f08ab0", LIGHT="#fcd8e8", MARK="#a83a6a", CLOTH="#3ab8c0", extra=dict(coral="#f8a040", coral_tip="#f8e0a0")),
                  "orange": dict(HAIR="#e8783a", FUR="#f0904a", LIGHT="#fce0c8", MARK="#a84a1a", CLOTH="#3a7ad8", extra=dict(coral="#a84af0", coral_tip="#e8c8ff")),
                  "purple": dict(HAIR="#7a4ac8", FUR="#9060d8", LIGHT="#e8d8f8", MARK="#4a2a8a", CLOTH="#f8d040", extra=dict(coral="#f8d040", coral_tip="#fff4c0")),
                  "green": dict(HAIR="#2a9a7a", FUR="#3ab890", LIGHT="#c8f0e0", MARK="#1a5a4a", CLOTH="#f86a7a", extra=dict(coral="#f86a7a", coral_tip="#f8c8a0"))}),
    # thirteenth wave: the rust otter, a dark otter hung with copper charms
    "rust_otter_mermaid": dict(
        build="athletic", outfit=dict(top=["sports"], extras=["armlets", "belt"], colors=dict(cloth2="#5a3a22", leather="#7a4a24")),
        ears="round", hair="shaggy", bangs="messy", fluke=(3.5, .75, 4), charms=True, size=0.95,
        pal=dict(SKIN=skin("tan"), HAIR="#2e2620", FUR="#3a2e26", LIGHT="#9a8470", MARK="#1e1814", EYE="#d8803a", CLOTH="#b8642a", TRIM="#d8803a",
                 pattern="solid", extra=dict(belly_white=True, copper="#d8803a")),
        variants={"brown": dict(HAIR="#5a3a22", FUR="#6a4a2e", LIGHT="#c8a880", MARK="#3a2414"),
                  "grey": dict(HAIR="#4a4644", FUR="#5a5652", LIGHT="#b8b2aa", MARK="#2a2826", CLOTH="#5aa890", TRIM="#7ac8b0",
                               extra=dict(belly_white=True, copper="#5aa890")),
                  "copper": dict(HAIR="#8a3e1a", FUR="#a8542a", LIGHT="#e8b890", MARK="#5a2410", CLOTH="#3a5a7a"),
                  "pale": dict(HAIR="#c8b8a4", FUR="#d8c8b4", LIGHT="#f4ece0", MARK="#8a7a68", EYE="#3a8a9a")}),
    # twelfth wave: the platypus, fur, a duck-billed cap and a flat beaver tail
    "platypus_mermaid": dict(
        build="petite", outfit=dict(top=["sports"], extras=["armlets"], colors=dict(cloth2="#3a6a8a")),
        ears=None, hair="bob", bangs="blunt", fluke=(5.5, .75, 6), platypus=True, size=0.95,
        pal=dict(SKIN=skin("tan"), HAIR="#5a3a22", FUR="#6a4a2e", LIGHT="#c8a880", MARK="#3a2414", EYE="#1a1410", CLOTH="#3a8aa8", TRIM="#e8c060",
                 pattern="solid", extra=dict(belly_white=True, bill="#3a3438")),
        variants={"dark": dict(HAIR="#2e2014", FUR="#3a2a1c", LIGHT="#8a6a50", MARK="#1e140c"),
                  "golden": dict(HAIR="#a8783a", FUR="#b8884a", LIGHT="#f0d8a8", MARK="#6a4a1e", CLOTH="#3a5a2a"),
                  "grey": dict(HAIR="#5a5652", FUR="#6e6a66", LIGHT="#c8c4c0", MARK="#3a3836", CLOTH="#a84a4a"),
                  "pale": dict(HAIR="#c8b090", FUR="#d8c0a0", LIGHT="#f8ece0", MARK="#9a8064", CLOTH="#5a7ac8", extra=dict(belly_white=True, bill="#8a7a7a"))}),
    # twelfth wave: the moonseal, a night-blue seal with crescent moons on her tail (they glow) and little antlers
    "moonseal_mermaid": dict(
        build="curvy", outfit=dict(top=["bandeau"], extras=["pearls"], colors=dict(cloth2="#e8f0f8")),
        ears=None, hair="wavy", bangs="swept", fluke=(7, .75, 3.5), seal=True, moon=True, glow=["crescent"], glow_boost=1.5,
        pal=dict(SKIN=skin("pale"), HAIR="#e8eef8", FUR="#2a3a8a", LIGHT="#e8eef8", MARK="#14204a", EYE="#5ab8f0", CLOTH="#e8eef8", TRIM="#5ab8f0",
                 pattern="solid", extra=dict(belly_white=True, moon="#f4f8ff", antler="#e8e0d0")),
        variants={"purple": dict(HAIR="#f0e8f8", FUR="#5a3a8a", MARK="#2a1a4a", EYE="#c88af0", TRIM="#c88af0", extra=dict(belly_white=True, moon="#f8f0ff", antler="#e8e0d0")),
                  "green": dict(HAIR="#e8f8f0", FUR="#2a6a6a", MARK="#143a3a", EYE="#5af0c8", TRIM="#5af0c8", extra=dict(belly_white=True, moon="#f0fff8", antler="#e8e0d0")),
                  "silver": dict(HAIR="#f4f4f8", FUR="#8a90a0", MARK="#4a505e", EYE="#5a8ad8", TRIM="#5a8ad8", extra=dict(belly_white=True, moon="#ffffff", antler="#f4f0e8")),
                  "night": dict(HAIR="#1e1e2a", FUR="#1a1a26", MARK="#0a0a12", LIGHT="#5a5a7a", EYE="#f8d040", TRIM="#f8d040", CLOTH="#2a2a3a",
                                extra=dict(belly_white=True, moon="#f8d860", antler="#c8b890"))}),
    # eleventh wave: the mantarrow, a manta ray: wide wing fins from her hips, a thin whip of a tail
    "mantarrow_mermaid": dict(
        build="slim", outfit=dict(top=["halter"], extras=["armlets"], colors=dict(cloth2="#1e2a4a")),
        ears=None, hair="flowing", bangs="parted", fluke=(1, .75, 8), manta=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e2a5a", FUR="#2a3a7a", LIGHT="#e8eef8", MARK="#14204a", EYE="#4ac8f0", CLOTH="#e8eef8", TRIM="#4ac8f0",
                 pattern="solid", extra=dict(belly_white=True)),
        variants={"black": dict(HAIR="#141418", FUR="#1e1e24", MARK="#0a0a0e", LIGHT="#d8d8e0", CLOTH="#3a3a46"),
                  "spotted": dict(HAIR="#141418", FUR="#1e1e24", MARK="#f4f4f6", LIGHT="#f4f4f6", pattern="spots", CLOTH="#e8e8f0"),
                  "teal": dict(HAIR="#1a5a6a", FUR="#2a7a8a", MARK="#0e3a44", CLOTH="#f8e8c0"),
                  "violet": dict(HAIR="#3a2a6a", FUR="#5a3a8a", MARK="#2a1a44", CLOTH="#f0d8f8", EYE="#f8a0e0")}),
    # tenth wave: the axolume, a glowing axolotl of the lush caves: her gills, crest and fluke glow (glow masks)
    "axolume_mermaid": dict(
        build="petite", outfit=dict(top=["halter"], extras=["pearls"], colors=dict(cloth2="#1e2a6a")),
        ears="gill", hair="odango", fluke=(4.5, .5, 3), dorsal=(.5, 2.5, 9), axolotl=True, glow=["ear_a", "dorsal", "fluke"],
        pal=dict(SKIN="#a8c8f0", HAIR="#3a5ad8", FUR="#3a5ad8", LIGHT="#a8d8ff", MARK="#7af0ff", EYE="#1a1a3e", CLOTH="#1e2a6a", TRIM="#7af0ff",
                 pattern="spots", INNER="#7af0ff"),
        variants={"rose": dict(SKIN="#f0c0d8", HAIR="#d84a9a", FUR="#d84a9a", LIGHT="#f8c8e8", MARK="#ffb0f0", INNER="#ffb0f0", TRIM="#ffb0f0"),
                  "gold": dict(SKIN="#f4e0b0", HAIR="#d8a030", FUR="#d8a030", LIGHT="#f8e8a8", MARK="#fff0a0", INNER="#fff0a0", TRIM="#fff0a0"),
                  "jade": dict(SKIN="#b8e8c8", HAIR="#2a9a6a", FUR="#2a9a6a", LIGHT="#a8f0c8", MARK="#9affc8", INNER="#9affc8", TRIM="#9affc8"),
                  "void": dict(SKIN="#8a7aa8", HAIR="#1e1830", FUR="#1e1830", LIGHT="#4a3a6a", MARK="#c88aff", INNER="#c88aff", TRIM="#c88aff",
                               CLOTH="#2a1e3a")}),
    # ninth wave: the siren, a scaled tail with long trailing fins, fin ears and flowing hair
    "siren_mermaid": dict(
        build="curvy", outfit=dict(top=["halter"], extras=["pearls", "armlets"], colors=dict(cloth2="#2a6a7a")),
        ears="fin", hair="flowing", bangs="wispy", fluke=(.5, 6.5, 5.5), long_fins=True, scales=True, size=1.05,
        pal=dict(SKIN=skin("pale"), HAIR="#3ab8b0", FUR="#2a9a98", LIGHT="#b8f0e8", MARK="#1a6a70", EYE="#e8d040", CLOTH="#e8f4f0", TRIM="#e8d8a0",
                 pattern="solid"),
        variants={"pearl": dict(HAIR="#f4f4f8", FUR="#d8dce4", LIGHT="#ffffff", MARK="#a8b0c0", EYE="#8a6ad8", CLOTH="#c8a8e8"),
                  "coral": dict(HAIR="#f87a6a", FUR="#f0907a", LIGHT="#fcd8c8", MARK="#c84a4a", EYE="#3ab8b0", CLOTH="#f8e8c8"),
                  "abyss": dict(SKIN=skin("fair"), HAIR="#1a2a6a", FUR="#1e3a7a", LIGHT="#6a8ad8", MARK="#0e1a4a", EYE="#8af0f8", CLOTH="#2a2a4a"),
                  "storm": dict(HAIR="#8a8e98", FUR="#6a707a", LIGHT="#c8ccd4", MARK="#3a3e48", EYE="#e8e8f8", CLOTH="#3a4a5a")}),
}

# homage looks with a model of their own (2026-10-28): the game draws <species>_<variant> instead of the species' model
# when a girl has that variant (AbstractMonsterGirl.OWN_MODELS); they are written after their species, so their texture
# replaces the species' plain recolour of the same name
HOMAGE_MODELS = {"axolotl_mermaid_mud_fin": "axolotl_mermaid", "shark_mermaid_trident_hood": "shark_mermaid"}
# Mudkip (Pokemon): light blue, a cream belly, a big fan fin on her head, star-shaped orange gills on her cheeks, a broad tail fin
SPECIES["axolotl_mermaid_mud_fin"] = dict(
    build="petite", outfit=dict(top=["shells"], extras=["pearls"]),
    ears=None, hair="bob", bangs="blunt", fluke=(.75, 7, 6), dorsal=(.5, 2.5, 9), axolotl=True, mudkip=True,
    pal=dict(SKIN=skin("pale"), HAIR="#5aa8e8", FUR="#5aa8e8", LIGHT="#eef4fa", MARK="#2a5aa8", EYE="#1e1a18", CLOTH="#f8902a", TRIM="#fce0c0",
             pattern="solid", INNER="#f8902a", extra=dict(belly_white=True, shell="#f8902a", gill="#f8902a", headfin="#5aa8e8", rim="#2a5aa8")),
    variants={})
# Gawr Gura (VTuber): white hair streaked blue in low twintails, an oversized blue hoodie whose hood is a shark's head (snout,
# teeth, eyes, gills and a dorsal fin), white drawstrings, a blue shark's tail, a trident in her right hand
SPECIES["shark_mermaid_trident_hood"] = dict(
    build="petite", outfit=dict(top=["sweater"], colors=dict(cloth2="#5a8ad8")),
    ears=None, hair="twintails", bangs="blunt", fluke=(.75, 7, 5), dorsal=(.75, 5, 4), size=1.0, ghood=True, trident=True,
    pal=dict(SKIN=skin("fair"), HAIR="#f4f6fa", FUR="#5a8ad8", LIGHT="#f0f4fa", MARK="#2a4a9a", EYE="#3a8ae8", CLOTH="#5a8ad8", TRIM="#f4f6fa",
             pattern="solid", extra=dict(belly_white=True, streak="#6aa8f0", hood="#5a8ad8", hood_dark="#2a4a9a", trident="#8ad0f0", trident_dark="#3a6ab0")),
    variants={})


def declare(sp):
    d = SPECIES[sp]
    k = d.get("size", 1.0)
    q = lambda v: round(v * k * 4) / 4
    parts = [("fin_1", q(4.5), 10.5, q(3.75)), ("fin_2", q(4.25), q(3.75), 5), ("fin_3", q(3.5), q(3.25), 5), ("fin_4", q(2.5), q(2.5), 4.5),
             ("fluke",) + d["fluke"]]
    if d.get("dorsal"):
        parts.append(("dorsal",) + d["dorsal"])
    if d.get("long_fins"):
        parts.append(("ribbon", .25, 6, 3))
    if d.get("seal"):
        parts.append(("flipper", 3, .5, 3.5))
    if d.get("manta"):
        parts.append(("manta_fin", 7, 6, .5))
    if d.get("platypus"):
        parts += [("pcap", 9.5, 3, 9.5), ("bill", 6, 1, 4.5)]
    if d.get("charms"):
        parts += [("charm", 1.25, 1.5, .25), ("charm_cord", 4.5, .25, .25)]
    if d.get("moon"):
        parts += [("crescent", 3.5, 3.5, .25), ("antler_a", .5, 3, .5), ("antler_b", .5, 1.5, .5)]
    if d.get("saw"):
        parts += [("saw", 2.25, 1, 7.5), ("saw_tooth", .75, .5, .5), ("shield", 2.25, 2.75, .25)]
    if d.get("katana"):
        parts += [("saya", 1, 10, 1.25), ("tsuba", 2, .5, 2), ("tsuka", .75, 4, .75)]
    if d.get("coral"):
        parts += [("coral_a", .75, 4, .75), ("coral_b", .5, 2.5, .5), ("coral_tip", .75, .75, .75)]
    if d.get("cowhorns"):
        parts += [("mhorn_a", .75, 1.5, .75), ("mhorn_b", .5, 1, .5)]
    if d.get("mudkip"):                                  # the head fin, and the cheek gills: a base and three points each
        parts += [("mk_headfin", .5, 6.5, 7.5), ("mk_gill", .75, 1.5, 1.5), ("mk_spike", 2.25, .75, .75)]
    if d.get("ghood"):                                   # the shark hood: crown, snout, teeth, fin; the hoodie's drawstrings and pocket
        parts += [("ghood", 9.75, 5.25, 9.75), ("ghood_snout", 7, 2, 3), ("ghood_teeth", 6.5, .75, .25), ("ghood_fin", .75, 3.5, 4),
                  ("gstring", .5, 3.5, .5), ("gpocket", 4, 1.75, .25), ("ghoodie", 5.25, 5.5, 3.75)]
    if d.get("trident"):
        parts += [("tri_shaft", .75, 21, .75), ("tri_bar", 4, .75, .75), ("tri_prong", .5, 2.75, .5), ("tri_mid", .5, 3.5, .5)]
    parts.append(("shell", 2, 1.75, .5))
    G.declare(parts, ears=d["ears"], hair=SPECIES[sp]["hair"],
              build=SPECIES[sp].get("build"), outfit=SPECIES[sp].get("outfit"))


def paint(sp, key):
    d = SPECIES[sp]
    pal = G.pal_from(d["pal"], d["variants"].get(key) if key else None)
    pt = H.Painter()
    G.paint_upper(pt, pal, top="skin", outfit=d.get("outfit"), build=d.get("build"))
    shell = shades(pal.extra.get("shell", "#f0d8c8"))
    # a shell top: two scallop shells over the bust, a pearl string as the cord
    if not d.get("outfit"):
        pt("bust", "all", lambda f, x, y, w, h: pick(pal.CLOTH, 0.55 + 0.25 * (x % 3 == 0) - 0.2 * (y == h - 1), x, y))
    pt("cord", "all", lambda f, x, y, w, h: pick(pal.TRIM, 0.85 if x % 2 else 0.6, x, y))
    pt("shell", "all", lambda f, x, y, w, h: pick(shell, 0.55 + 0.3 * (x % 3 == 1), x, y))
    sc = G.scale_fn(pal, belly_faces=("down", "north") if pal.extra.get("belly_white") else ("down",))
    smooth = G.fur_fn(pal, light_faces=("down", "north") if pal.extra.get("belly_white") else ("down",), grain=0.06, seed=41)
    body = sc if sp.startswith("koi") or d.get("scales") else smooth                 # the koi has scales; the mammals and the shark are smooth
    skin_t = H.skin_fn(pal, 0.66)
    pt("hips", "all", lambda f, x, y, w, h: skin_t(f, x, y, w, h) if y < h * 0.4 else body(f, x, y, w, h))
    for p in ("fin_1", "fin_2", "fin_3", "fin_4"):
        pt(p, "all", body)

    def fin(f, x, y, w, h):
        t = 0.5 + 0.2 * (x % 3 == 0) + (noise(x, y, 6) - 0.5) * 0.1
        col = pick(pal.MARK if sp.startswith("koi") or pal.extra.get("fin_mark") else pal.FUR, t, x, y)
        if pal.extra.get("belly_white") is None and sp.startswith("axolotl"):
            col = pick(pal.LIGHT, t, x, y)
        return col
    pt("fluke", "all", fin)
    if d.get("hand"):                                             # the ahuizotl's tail ends in a hand: skin, with fingers
        pt("fluke", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") and y >= h * 0.55 and x % 3 == 2 else skin_t(f, x, y, w, h))
    pt("dorsal", "all", fin)
    pt("manta_fin", "all", lambda f, x, y, w, h: pick(pal.LIGHT if f == "north" else pal.FUR, 0.5 + 0.15 * (y == 0) - 0.1 * (x in (0, w - 1)), x, y)
       if y < h - (abs(x + 0.5 - w / 2) * 2 // 3) else CLEAR)
    pt("ribbon", "all", lambda f, x, y, w, h: G.with_alpha(pick(pal.MARK, 0.55 + 0.25 * (y % 3 == 0), x, y), 220) if y < h - 1 or x % 2 else G.CLEAR)
    pt("flipper", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.35 + 0.2 * (x % 4 == 0), x, y))
    cu = shades(pal.extra.get("copper", "#d8803a"))
    pt("charm", "all", lambda f, x, y, w, h: pick(cu, 0.5 + 0.3 * (x == 1 and y == 1) - 0.2 * (y == h - 1), x, y))
    pt("charm_cord", "all", lambda f, x, y, w, h: pick(shades("#5a3a22"), 0.45, x, y))
    bill = shades(pal.extra.get("bill", "#3a3438"))
    pt("pcap", "all", lambda f, x, y, w, h: CLEAR if f == "down" else pick(pal.MARK, 0.55 + (noise(x, y, 71) - 0.5) * 0.12, x, y))
    pt("bill", "all", lambda f, x, y, w, h: pick(bill, 0.45 + 0.1 * (f == "up") + 0.15 * (f == "up" and y == h - 2 and x in (w // 2 - 2, w // 2 + 1)), x, y))
    moon = shades(pal.extra.get("moon", "#f4f8ff"))

    def crescent(f, x, y, w, h):                                  # a crescent moon: a disc with a bite out of it
        if f not in ("north", "south"):
            return CLEAR
        cx, cy, rr = w / 2, h / 2, w / 2
        inside = (x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2 < rr * rr
        bite = (x + 0.5 - cx - rr * 0.45) ** 2 + (y + 0.5 - cy + rr * 0.2) ** 2 < (rr * 0.8) ** 2
        return pick(moon, 0.75, x, y) if inside and not bite else CLEAR
    pt("crescent", "all", crescent)
    ant = shades(pal.extra.get("antler", "#e8e0d0"))
    pt("antler_a", "all", lambda f, x, y, w, h: pick(ant, 0.55 + 0.1 * (y % 2), x, y))
    pt("antler_b", "all", lambda f, x, y, w, h: pick(ant, 0.6, x, y))
    coral, ctip = shades(pal.extra.get("coral", "#f86a7a")), shades(pal.extra.get("coral_tip", "#f8c8a0"))
    pt("coral_a", "all", lambda f, x, y, w, h: pick(coral, 0.5 + 0.15 * (y % 2), x, y))
    pt("coral_b", "all", lambda f, x, y, w, h: pick(coral, 0.6, x, y))
    pt("coral_tip", "all", lambda f, x, y, w, h: pick(ctip, 0.65, x, y))
    saw, shield, lily = shades(pal.extra.get("saw", "#f0ece4")), shades(pal.extra.get("shield", "#3a5ab0")), shades(pal.extra.get("lily", "#e8e8f0"))
    pt("saw", "all", lambda f, x, y, w, h: pick(saw, 0.55 + 0.15 * (f == "up") - 0.1 * (f == "down"), x, y))
    pt("saw_tooth", "all", lambda f, x, y, w, h: pick(saw, 0.75, x, y))
    e = pal.extra
    if d.get("mudkip"):
        hf, rim, gill = shades(e.get("headfin", "#5aa8e8")), shades(e.get("rim", "#2a5aa8")), shades(e.get("gill", "#f8902a"))

        def headfin(f, x, y, w, h):                       # a half-disc fan standing on her head, ribbed, a dark rim
            if f in ("up", "down", "north", "south"):
                return CLEAR                             # a thin fan: only its two broad sides
            dx = (x + 0.5 - w / 2) / (w / 2)
            top = (h - 1) * dx * dx                      # y counts down from the top edge
            if y < top:
                return CLEAR
            if y < top + 1.2:
                return pick(rim, 0.5, x, y)
            return pick(hf, 0.55 + 0.12 * (x % 2 == 0), x, y)
        pt("mk_headfin", "all", headfin)
        pt("mk_gill", "all", lambda f, x, y, w, h: pick(gill, 0.55 + 0.1 * (f == "up"), x, y))
        pt("mk_spike", "all", lambda f, x, y, w, h: pick(gill, 0.6 - 0.15 * (x == 0), x, y))
        # two orange cheek spots under her eyes, Mudkip's
        pt("head", ("north",), lambda f, x, y, w, h: pick(gill, 0.6, x, y) if y in (26, 27) and x in (6, 7, 24, 25) else None)
    if d.get("ghood"):
        hood, dark, white = shades(e.get("hood", "#5a8ad8")), shades(e.get("hood_dark", "#2a4a9a")), shades("#f4f6fa")
        ink = shades("#141418")

        def crown(f, x, y, w, h):                         # over the top and back of her head; the front left open for her face
            if f == "down":
                return CLEAR
            if f == "north":
                return pick(hood, 0.5, x, y) if y < 2 else CLEAR
            if f in ("east", "west"):
                if y >= h * 0.55 and (x < w * 0.35 if f == "west" else x > w * 0.65):
                    return CLEAR                         # the opening round her cheeks
                if y == h // 2 and (x % 3 == 1) and (w * 0.35 < x < w * 0.75):
                    return pick(dark, 0.35, x, y)        # gill slits
            return pick(hood, 0.5 + 0.08 * ((x + y) % 5 == 0), x, y)
        pt("ghood", "all", crown)

        def snout(f, x, y, w, h):                         # the shark's upper jaw over her brow: blue above, white below, eyes at the sides
            if f == "down":
                return pick(white, 0.75, x, y)
            if f in ("east", "west") and y == 0 and x == 1:
                return pick(ink, 0.3, x, y)              # the hood's eye
            if f == "north" and y == h - 1:
                return pick(white, 0.7, x, y)
            return pick(hood, 0.55, x, y)
        pt("ghood_snout", "all", snout)
        pt("ghood_teeth", "all", lambda f, x, y, w, h: pick(white, 0.85, x, y) if f in ("north", "south") and y <= (x % 2) else CLEAR)

        def dorsal_fin(f, x, y, w, h):                    # a triangle, swept back
            if f in ("east", "west"):
                xb = x if f == "east" else w - 1 - x     # 0 at the front
                return pick(hood, 0.5 + 0.1 * (y == h - 1), x, y) if y >= h - 1 - (h * (xb + 0.5) / w) * 1.2 else CLEAR
            return CLEAR                                 # a thin fin: only its two broad sides
        pt("ghood_fin", "all", dorsal_fin)
        pt("ghoodie", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down")
           else pick(white, 0.6, x, y) if y == h - 1 else pick(hood, 0.5 + 0.06 * ((x * 3 + y) % 7 == 0), x, y))
        pt("gstring", "all", lambda f, x, y, w, h: pick(white, 0.75 - 0.25 * (y == h - 1), x, y))
        pt("gpocket", "all", lambda f, x, y, w, h: pick(dark if y == 0 else hood, 0.45, x, y))
        # white hair with blue streaks
        streak = shades(e.get("streak", "#6aa8f0"))
        for part in [q for q in H.PARTS if q.startswith(("strand_", "lock_", "tail_lock", "tail_long", "bang_", "frame"))]:
            pt(part, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(streak, 0.55, x, y) if (x + y // 6) % 4 == 1 else None)
    if d.get("trident"):
        tr, trd = shades(e.get("trident", "#8ad0f0")), shades(e.get("trident_dark", "#3a6ab0"))
        pt("tri_shaft", "all", lambda f, x, y, w, h: pick(trd if y % 5 == 0 else tr, 0.45 + 0.1 * (f == "north"), x, y))
        pt("tri_bar", "all", lambda f, x, y, w, h: pick(trd, 0.5, x, y))
        pt("tri_prong", "all", lambda f, x, y, w, h: pick(tr, 0.6 + 0.2 * (y == 0), x, y))
        pt("tri_mid", "all", lambda f, x, y, w, h: pick(tr, 0.65 + 0.2 * (y == 0), x, y))

    def fleur(f, x, y, w, h):                                     # a fleur-de-lis on a heater shield
        if f not in ("north",):
            return pick(shield, 0.45, x, y)
        if y > h - 3 and abs(x + 0.5 - w / 2) > (h - y) * w / 6:
            return CLEAR
        c = w / 2 - 0.5
        on = abs(x - c) < 1 and 1 <= y < h - 2 or (y in (3, 4) and 1 <= abs(x - c) < 3) or (y == 2 and abs(x - c) == 2)
        return pick(lily if on else shield, 0.6, x, y)
    pt("shield", "all", fleur)
    saya, tsuka, gold = shades(pal.extra.get("saya", "#1e2a6a")), shades(pal.extra.get("tsuka", "#e8e0d0")), shades("#d8b040")
    pt("saya", "all", lambda f, x, y, w, h: pick(gold if y % 6 == 0 else saya, 0.5 + 0.2 * (f == "north"), x, y))
    pt("tsuba", "all", lambda f, x, y, w, h: pick(gold, 0.6, x, y))
    pt("tsuka", "all", lambda f, x, y, w, h: pick(tsuka if (x + y) % 3 else saya, 0.55, x, y))
    if pal.extra.get("gills"):
        pt("fin_1", ("east", "west"), lambda f, x, y, w, h: pick(pal.MARK, 0.25, x, y) if (y in (1, 3, 5) and 2 <= x < w - 2) else None)
    if pal.extra.get("spot"):                                     # tancho: one red spot on the head
        pt("hair_cap", ("up",), lambda f, x, y, w, h: pick(shades("#d8402a"), 0.55, x, y) if (x - w / 2) ** 2 + (y - h / 2) ** 2 < 30 else None)
    if d["ears"] == "round":
        G.paint_ears(pt, pal, G.fur_fn(pal, pattern="solid"))
    if d["ears"] in ("cow", "big"):
        G.paint_ears(pt, pal, G.fur_fn(pal, pattern="solid"))
    horn = shades(pal.extra.get("horn", "#e8dcc0"))
    pt("mhorn_a", "all", lambda f, x, y, w, h: pick(horn, 0.5 + 0.1 * (f == "up"), x, y))
    pt("mhorn_b", "all", lambda f, x, y, w, h: pick(horn, 0.65, x, y))
    if d["ears"] == "gill":
        pt("ear_a", "all", lambda f, x, y, w, h: pick(pal.INNER, 0.55 + 0.25 * (y % 2 == 0), x, y))
    elif d["ears"] == "fin":
        pt("ear_a", "all", fin)
    return pt.T


def build(sp):
    d = SPECIES[sp]
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), build=d.get("build"), outfit=d.get("outfit"), ears=d["ears"], skirt=False, cuffs=False))
    if not d.get("outfit"):
        sh = cube("shell", [-2.25, 16.8, -2.6])
        r.put("body", sh, G.mirror_cube(sh))
    # seashells she finds on the sea floor (hidden until the game says she wears them)
    bw, bh, bd = G.S("bust")
    cd = G.S("chest")[2]
    sw, sh_, sdp = G.S("shell")
    fz = -cd / 2 - bd
    left = cube("shell", [-0.25 - bw + (bw - sw) / 2, 16.9 - 0.15, fz - sdp + 0.1], inflate=0.1)
    r.add("seashells", "body", [0, 17.5, fz], [left, G.mirror_cube(left), cube("cord", [-1.5, 18.6, fz - 0.15])])
    G.fish(r, None, d)
    if d.get("dorsal"):
        dw, dh, dd = G.S("dorsal")
        if d.get("axolotl"):                                       # a low frilly crest along the tail
            r.put("fin_2", cube("dorsal", [-dw / 2, G.S("fin_2")[1] - 0.25, 1.5]))
        else:
            r.put("fin_1", G.rot_cube("dorsal", [-dw / 2, 7.5, 1.0], [0, 7.5, 1.6], -35, 0, 0))
    if d.get("long_fins"):
        for sg in (-1, 1):
            c = G.rot_cube("ribbon", [sg * 2.4 - 0.125, 2.0, 2.0], [sg * 2.4, 6, 2.5], 20, 0, -18 * sg)
            r.put("fin_2", c)
    if d.get("manta"):                                   # the wing fins, swept out from her hips
        mw = G.S("manta_fin")[0]
        r.add("manta_r", "fin_1", [-2, 9, 0], [G.rot_cube("manta_fin", [-2 - mw, 4.5, -0.25], [-2, 9, 0], 0, 0, -12)], mirror=True)
    if d.get("charms"):                                  # copper charms on a cord round her neck, one at her hip
        fz = -G.S("chest")[2] / 2 - G.S("bust")[2] - 0.4
        r.put("body", cube("charm_cord", [-2.25, 18.6, fz]), cube("charm", [-1.9, 17.0, fz - 0.1]), cube("charm", [-0.625, 16.6, fz - 0.1]),
              cube("charm", [0.65, 17.0, fz - 0.1]), cube("charm", [-3.0, 11.6, -1.6]))
    if d.get("platypus"):                                # a furry cap with a duck bill for a peak
        hy = 12 + 8 + 0.0
        r.add("pcap", "head", [0, 28, 0], [cube("pcap", [-4.75, 26.2, -4.75]), G.rot_cube("bill", [-3, 25.9, -8.9], [0, 26.4, -4.6], 12, 0, 0)])
    if d.get("moon"):                                    # crescents down her tail (they glow), little antlers in her hair
        r.put("fin_1", cube("crescent", [-1.75, 3.6, -2.2]))
        r.put("fin_2", cube("crescent", [-1.75, 0.3, -1.0]))
        r.put("fin_3", cube("crescent", [-1.75, 0.1, 5.4]))
        for sg in (-1, 1):
            r.put("head", G.rot_cube("antler_a", [sg * 2.4 - 0.25, 28.0, -0.5], [sg * 2.4, 28.0, -0.25], -10, 0, -18 * sg),
                  G.rot_cube("antler_b", [sg * 2.9 - 0.25, 29.4, -0.5], [sg * 2.9, 29.4, -0.25], -10, 0, -60 * sg))
    if d.get("saw"):                                     # the rostrum straight out from her nose, teeth along both edges; a shield on her chest
        hy = 20.0
        cs = [cube("saw", [-1.125, hy + 2.0, -11.9])]
        for i in range(5):
            zz = -11.4 + i * 1.5
            cs += [cube("saw_tooth", [-1.875, hy + 2.25, zz]), cube("saw_tooth", [1.125, hy + 2.25, zz])]
        r.add("saw", "head", [0, hy + 1.4, -4.4], cs)
        cd, bd = G.S("chest")[2], G.S("bust")[2]
        r.put("body", cube("shield", [-1.125, 14.4, -cd / 2 - bd - 0.4]))
    if d.get("katana"):                                  # a katana slung across her back, hilt over her right shoulder
        cd = G.S("chest")[2]
        bz = cd / 2 + 0.3
        rot = dict(pivot=[0, 16, bz], rotation=[0, 0, -38])
        r.add("katana", "body", [0, 16, bz], [cube("saya", [-0.5, 10.0, bz], **rot), cube("tsuba", [-1.0, 20.0, bz - 0.375], **rot),
                                              cube("tsuka", [-0.375, 20.5, bz + 0.25], **rot)])
    if d.get("coral"):                                   # a crown of branching coral in her hair
        cs = []
        for sg in (-1, 1):
            for x0, tilt in ((1.2, 10), (2.8, 28)):
                x = sg * x0
                cs.append(G.rot_cube("coral_a", [x - 0.375, 28.0, -1.2], [x, 28.0, -0.8], -8, 0, -tilt * sg))
                tx = x + sg * 4 * __import__("math").sin(__import__("math").radians(tilt))
                cs.append(G.rot_cube("coral_b", [tx - 0.25, 30.6, -1.0], [tx, 30.6, -0.8], -20, 0, -60 * sg))
                cs.append(cube("coral_tip", [tx + sg * 0.3 - 0.375, 31.5, -1.4]))
        r.put("head", *cs)
    if d.get("cowhorns"):                                # two little horns curving out of her curls
        cs = []
        for sg in (-1, 1):
            cs += [G.rot_cube("mhorn_a", [sg * 2.6 - 0.375, 28.0, -1.0], [sg * 2.6, 28.0, -0.6], -10, 0, -35 * sg),
                   G.rot_cube("mhorn_b", [sg * 3.5 - 0.25, 29.2, -1.0], [sg * 3.5, 29.2, -0.75], -10, 0, -70 * sg)]
        r.add("hair_horns", "head", [0, 28, 0], cs)
    if d.get("mudkip"):                                  # the fan fin along the top of her head; star gills on her cheeks
        fw, fh, fd = G.S("mk_headfin")
        r.add("hair_mkfin", "head", [0, 28.4, 0], [cube("mk_headfin", [-fw / 2, 28.0, -fd / 2 + 0.5])])
        cs = []
        for sg in (-1, 1):
            gx = sg * 4.4
            base = cube("mk_gill", [gx - 0.375, 20.8, -2.2])
            cs.append(base)
            for tilt in (-40, 0, 40):                   # three points fanned outward, like a little orange star
                piv = [gx, 21.55, -1.45]
                x0 = gx if sg > 0 else gx - 2.25
                cs.append(G.rot_cube("mk_spike", [x0, 21.175, -1.825], piv, 0, 0, tilt))
        r.add("cheek_gills", "head", [0, 21.5, -1.5], cs)
    if d.get("ghood"):                                   # the hood over her hair, the shark's snout and teeth over her brow, a fin on top
        r.add("hair_hood", "head", [0, 26, 0], [cube("ghood", [-4.875, 24.4, -4.875]), cube("ghood_snout", [-3.5, 27.6, -7.4]),
                                                 cube("ghood_teeth", [-3.25, 26.9, -7.1]),
                                                 G.rot_cube("ghood_fin", [-0.375, 29.4, -0.5], [0, 29.6, 1.0], 0, 0, 0)])
        cd, bd = G.S("chest")[2], G.S("bust")[2]
        fz = -cd / 2 - bd - 0.6
        hw, hh, hd = G.S("ghoodie")                       # the hoodie's long body down over her waist, white at the hem
        r.put("body", cube("ghoodie", [-hw / 2, 11.0, -hd / 2]), cube("gstring", [-1.25, 15.2, fz]), cube("gstring", [0.75, 15.4, fz]),
              cube("gpocket", [-2.0, 12.4, -hd / 2 - 0.25]))
    if d.get("trident"):                                 # upright in her right hand, the three prongs above her fist
        px, py, pz = r.b["hand_r"]["pivot"]
        cx, cz = px - 1.2, pz - 3.6                      # held out in front of her hair
        top = py - 1.5 + 15.5
        r.add("trident", "hand_r", [px, py - 1.5, pz], [cube("tri_shaft", [cx, py - 1.5 - 5.5, cz]), cube("tri_bar", [cx - 1.625, top, cz]),
                                                        cube("tri_prong", [cx - 1.5, top + 0.75, cz + 0.125]), cube("tri_prong", [cx + 1.75, top + 0.75, cz + 0.125]),
                                                        cube("tri_mid", [cx + 0.125, top + 0.75, cz + 0.125])])
    if d.get("seal"):
        f2 = G.S("fin_2")
        for sg in (-1, 1):
            r.put("fin_1", G.rot_cube("flipper", [sg * 2.6 - 1.5, 7.0, -2.2], [sg * 2.6, 7.25, -1.0], 0, 0, -30 * sg))
    return r.model(sp)


def anims(an):
    G.fish_anims(an)


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.4, -45, 15), ("swim", 0.3, -70, 15), ("sit", 0, -40, 25), ("sing", 0.5, -30, 8)]
FACE = [("idle", 0, 0, 0, 24), ("idle", 2.75, 0, 0, 24), ("sing", 0.5, 0, 0, 24), ("idle", 0, -30, 8, 24)]

if __name__ == "__main__":
    G.run_family("merfolk", SPECIES, declare, paint, build, anims, VIEWS, FACE)
    import girl_family as F
    want = [a for a in sys.argv[1:] if not a.startswith("--")]
    for m, base in HOMAGE_MODELS.items():                # after their species: their texture is the one the game draws for that look
        if want and m not in want and base not in want:
            continue
        declare(m)
        geo, adoc, texes = G.write(m, paint(m, None), build(m), anims, [], None)
        if "--previews" in sys.argv:
            G.previews(m, geo, adoc, texes, VIEWS, FACE, G.model_height(geo))
    for sp, d in SPECIES.items():                        # glow masks: the axolume's gills, crest and fluke
        if d.get("glow") and (not want or sp in want):
            declare(sp)
            for k in [None] + list(d["variants"]):
                F.write_glowmask(sp + ("_" + k if k else ""), paint(sp, k), d["glow"], d.get("glow_boost", 1.0))
