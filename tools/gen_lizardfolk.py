#!/usr/bin/env python3
"""Lizardfolk (the reptile family, see lizardfolk_field_guide.html): salamander, chameleon, iguana, crocodile, turtle.

    python tools/gen_lizardfolk.py [species ...] [--previews]

girl_family bipeds with scaled legs, clawed feet and a heavy four-part tail (tail .. tail_4). The salamander's tail
ends in a flame (glow mask), the chameleon's curls up, the iguana and the crocodile have spines down the back and tail
(`spines`), the turtle carries a shell (`shell`) and has a stubby tail. The turtles of 2026-10-25: the red-eared
slider (a red streak behind each eye), the Galapagos and box turtles (a high domed shell, `shell="dome"`), the
softshell (a flat leathery shell, `shell="flat"`, and a snorkel snout, `snout`) and the matamata (a leaf-litter
shell and a long snout).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin  # noqa: E402

LIZARD_TAIL = [(2.5, 2.5, 4), (2, 2, 4), (1.5, 1.5, 4), (1, 1, 3.5)]
DROOP = [(-38, 0, 0), (10, 0, 0), (10, 0, 0), (8, 0, 0)]

SPECIES = {
    "salamander_lizardfolk": dict(
        build="athletic", outfit=dict(top=["bandeau"], bottom="loincloth", extras=["armlets", "belt"], colors=dict(cloth2="#3a1a14")),
        hair="ponytail", bangs="jagged", ears=None, legs="scales", foot="claw", tail=LIZARD_TAIL, tail_rest=DROOP, tail_look="flame",
        glow=["tail_d"],
        nurse_kit=True,
        pal=dict(SKIN=skin("tan"), HAIR="#d84a1a", FUR="#e8702a", LIGHT="#f8c87a", MARK="#f8e04a", EYE="#f8c82a", CLOTH="#2a1a14", TRIM="#f8a83a",
                 pattern="spots", pupil=(0.9, 3.4), extra=dict(flame="#f8a02a", claw="#3a2014")),
        variants={"crimson": dict(FUR="#c82a1a", HAIR="#8a1a10", MARK="#f8a02a", extra=dict(flame="#f8582a", claw="#2a0e08")),
                  "soul": dict(FUR="#2a6a8a", HAIR="#2ac8e8", MARK="#8af0ff", EYE="#8af0ff", extra=dict(flame="#3ae8f8", claw="#14303a")),
                  "obsidian": dict(FUR="#2a1e2a", HAIR="#1a141a", MARK="#f8782a", LIGHT="#5a4a5a", extra=dict(flame="#f8782a", claw="#100c10")),
                  "gold": dict(FUR="#e8b830", HAIR="#c8781a", MARK="#fff0a0", extra=dict(flame="#fff07a", claw="#5a3a10")),
                  # homage to Gregory Horror Show (2026-10-25): Catherine, the pink lizard nurse with an enormous syringe
                  "nurse": dict(SKIN=skin("#f0a8c8"), FUR="#e890b8", LIGHT="#f8c8dc", MARK="#e0408a", HAIR="#e890b8", EYE="#c02a6a", CLOTH="#f8f8f8",
                                TRIM="#d82a2a", pattern="spots", extra=dict(flame="#f87ab0", claw="#2a1a1a", nurse=True))}),
    "chameleon_lizardfolk": dict(
        build="slim", outfit=dict(top=["halter"], bottom="sarong", extras=["armlets"], colors=dict(cloth2="#6a3a8a")),
        hair="bun", bangs="swept", ears=None, legs="scales", foot="claw", tail=LIZARD_TAIL, tail_look="scale",
        tail_rest=[(-20, 0, 0), (45, 0, 0), (60, 0, 0), (70, 0, 0)], tail_sway=3,
        pal=dict(SKIN=skin("peach"), HAIR="#5ab84a", FUR="#6ac850", LIGHT="#c8f0a0", MARK="#3a8a2a", EYE="#e8c83a", CLOTH="#e8a83a", TRIM="#c83a6a",
                 pattern="stripes", pupil=(1.4, 1.4)),
        variants={"panther": dict(FUR="#3ac8c0", HAIR="#e84a3a", MARK="#e84a3a", LIGHT="#a8f0f0"),
                  "yellow": dict(FUR="#c8e04a", HAIR="#a8c83a", MARK="#7a9a2a"),
                  "veiled": dict(FUR="#4ab05a", HAIR="#3a8a4a", MARK="#e8c83a", pattern="bands"),
                  "brown": dict(FUR="#8a6a4a", HAIR="#6a4a2a", MARK="#4a3420", LIGHT="#c8a880")}),
    "iguana_lizardfolk": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts", extras=["belt"], colors=dict(cloth2="#3a5a7a")),
        hair="sidetail", bangs="messy", ears=None, legs="scales", foot="claw", tail=LIZARD_TAIL, tail_rest=DROOP, tail_look="scale", spines=True,
        pal=dict(SKIN=skin("tan"), HAIR="#4a8a3a", FUR="#5aa84a", LIGHT="#c8e8a0", MARK="#2a5a2a", EYE="#d89a2a", CLOTH="#e8e0c8", TRIM="#3a5a7a",
                 pattern="bands"),
        variants={"blue": dict(FUR="#4a8ac8", HAIR="#3a6aa8", MARK="#2a4a7a", LIGHT="#b8d8f0"),
                  "orange": dict(FUR="#e8883a", HAIR="#c8682a", MARK="#7a3a1a", LIGHT="#f8c8a0"),
                  "marine": dict(FUR="#4a4a4e", HAIR="#2a2a2e", MARK="#7a7a5a", LIGHT="#8a8a8e"),
                  "rhino": dict(FUR="#8a8a82", HAIR="#5a5a52", MARK="#4a4a44", LIGHT="#c0c0b8")}),
    "croc_lizardfolk": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps", "belt"], colors=dict(bandage="#c8b898")),
        hair="shaggy", bangs="messy", ears=None, legs="scales", foot="claw", tail=[(3.5, 3, 5), (3, 2.5, 5), (2.25, 2, 4.5), (1.5, 1.5, 4)],
        tail_rest=[(-25, 0, 0), (8, 0, 0), (6, 0, 0), (4, 0, 0)], tail_look="scale", spines=True, teeth=True,
        pal=dict(SKIN=skin("olive"), HAIR="#2a3a1e", FUR="#5a6a3a", LIGHT="#d8d0a0", MARK="#2a3a1a", EYE="#d8c83a", CLOTH="#4a3a2a", TRIM="#a8987a",
                 pattern="bands", pupil=(0.9, 3.4)),
        variants={"saltwater": dict(FUR="#4a5030", HAIR="#1e2414", MARK="#1a1e10"),
                  "albino": dict(FUR="#f0e8d8", HAIR="#f8f4ec", MARK="#d8c8b0", LIGHT="#ffffff", EYE="#e83a4a"),
                  "dwarf": dict(FUR="#2a2a24", HAIR="#141410", MARK="#4a4a3a"),
                  "gharial": dict(FUR="#7a8a6a", HAIR="#4a5a3a", MARK="#3a4a2a")}),
    "turtle_lizardfolk": dict(
        build="petite", outfit=dict(top=["sports"], bottom="shorts", extras=["pearls"], colors=dict(cloth2="#3a7a8a")),
        hair="bob", bangs="blunt", ears=None, legs="scales", foot="claw", tail=[(1.5, 1.5, 2), (1, 1, 1.5)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", shell=True,
        pal=dict(SKIN=skin("peach"), HAIR="#3a6a3a", FUR="#7aa85a", LIGHT="#e0e8b0", MARK="#4a6a3a", EYE="#3a2a1a", CLOTH="#e8d8a8", TRIM="#3a7a8a",
                 extra=dict(shell="#7a5a2a", shell_band="#c8a050")),
        variants={"hawksbill": dict(extra=dict(shell="#c88a3a", shell_band="#5a2a10"), HAIR="#6a4a2a"),
                  "leatherback": dict(FUR="#4a4a52", HAIR="#2a2a32", extra=dict(shell="#2a2a32", shell_band="#7a7a8a")),
                  "loggerhead": dict(FUR="#a87a4a", HAIR="#8a4a2a", extra=dict(shell="#8a3a1a", shell_band="#c8784a")),
                  "olive": dict(FUR="#8a8a4a", HAIR="#5a5a2a", extra=dict(shell="#6a6a2a", shell_band="#a8a85a"))}),
    "snapping_turtle_lizardfolk": dict(
        build="strong", outfit=dict(top=["halter", "vest"], bottom="sarong", extras=["belt", "pouch"], colors=dict(cloth2="#3a4a2a", leather="#4a3a2a")),
        hair="shaggy", bangs="messy", ears=None, legs="scales", foot="claw", tail=[(2.5, 2, 4), (2, 1.75, 4), (1.5, 1.25, 3.5), (1, 1, 3)],
        tail_rest=DROOP, tail_look="scale", spines=True, shell=True,
        pal=dict(SKIN=skin("olive"), HAIR="#3a4a22", FUR="#6a5a42", LIGHT="#a89a7a", MARK="#3a3024", EYE="#c8a040", CLOTH="#5a6a4a", TRIM="#a8987a",
                 pattern="bands", pupil=(1.4, 1.4), extra=dict(shell="#3e3424", shell_band="#24201a")),
        variants={"mossy": dict(HAIR="#4a7a2a", extra=dict(shell="#3e4a24", shell_band="#5a7a2a")),
                  "dark": dict(FUR="#3a342a", HAIR="#1e1e16", extra=dict(shell="#1e1a14", shell_band="#3a3428")),
                  "suwannee": dict(FUR="#7a7466", HAIR="#4a4a3a", extra=dict(shell="#5a5448", shell_band="#3a362e")),
                  "leucistic": dict(FUR="#e0d4b8", HAIR="#f0e8d0", LIGHT="#fff8e8", MARK="#c8b898", EYE="#c84a4a",
                                    extra=dict(shell="#d8c8a0", shell_band="#b8a880"))}),
    "black_tortoise_lizardfolk": dict(
        build="athletic", outfit=dict(top=["kimono"], colors=dict(kimono="#1e2a3a", kimono_trim="#c8d8e8", obi="#5a8aa8")),
        hair="hime", bangs="blunt", ears=None, legs="scales", foot="claw",
        tail=[(1.75, 1.75, 5), (1.5, 1.5, 5), (1.25, 1.25, 4.5), (1, 1, 4)], tail_rest=[(-20, 0, 0), (25, 30, 0), (20, 40, 0), (15, 40, 0)],
        tail_look="scale", shell=True,
        pal=dict(SKIN=skin("pale"), HAIR="#141a1e", FUR="#2a3a42", LIGHT="#8aa0a8", MARK="#5ae8d8", EYE="#5ae8d8", CLOTH="#1e2a3a", TRIM="#c8d8e8",
                 pattern="bands", pupil=(0.9, 3.4), extra=dict(shell="#1e2a2e", shell_band="#5a7a8a")),
        variants={"jade": dict(FUR="#2a5a4a", HAIR="#14302a", MARK="#a8f0c8", EYE="#a8f0c8", extra=dict(shell="#1e3a30", shell_band="#5aa88a")),
                  "bronze": dict(FUR="#5a4a2a", HAIR="#2a1e10", MARK="#e8c060", EYE="#e8c060", extra=dict(shell="#4a3a1e", shell_band="#c8a050")),
                  "ice": dict(FUR="#8ab0c8", HAIR="#d8eef8", MARK="#ffffff", EYE="#5ab0e8", extra=dict(shell="#6a8aa0", shell_band="#e0f0f8")),
                  "black": dict(FUR="#141418", HAIR="#0a0a0c", MARK="#c83a3a", EYE="#c83a3a", extra=dict(shell="#0e0e10", shell_band="#3a3a40"))}),
    "red_eared_slider_lizardfolk": dict(
        build="petite", outfit=dict(top=["halter"], bottom="shorts", extras=["armlets"], colors=dict(cloth2="#c83a2a")),
        hair="sidetail", bangs="swept", ears=None, legs="scales", foot="claw", tail=[(1.5, 1.5, 2), (1, 1, 1.5)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", shell=True,
        pal=dict(SKIN=skin("peach"), HAIR="#3a6a2a", FUR="#5a8a3a", LIGHT="#e8e07a", MARK="#d83a2a", EYE="#c8a030", CLOTH="#e8d8a8", TRIM="#d83a2a",
                 pattern="stripes", extra=dict(shell="#4a6a2a", shell_band="#c8c050")),
        variants={"yellow_bellied": dict(MARK="#e8d040", TRIM="#e8d040", extra=dict(shell="#3a4a22", shell_band="#e8d040")),
                  "cumberland": dict(MARK="#e8a030", HAIR="#2a4a22", extra=dict(shell="#3a4a2a", shell_band="#a8a050")),
                  "pastel": dict(FUR="#c8d8a0", HAIR="#a8c888", MARK="#f08a7a", LIGHT="#fff8c8", extra=dict(shell="#a8b880", shell_band="#f0e8b0")),
                  "albino": dict(FUR="#f0ecd0", HAIR="#f8f4e0", MARK="#f8c040", LIGHT="#ffffff", EYE="#e84a4a",
                                 extra=dict(shell="#e8e0b0", shell_band="#f8e880"))}),
    "galapagos_tortoise_lizardfolk": dict(
        build="strong", outfit=dict(top=["halter", "vest"], bottom="sarong", extras=["belt"], colors=dict(cloth2="#8a6a3a", leather="#5a4a2a")),
        hair="bun", bangs="blunt", ears=None, legs="scales", foot="claw", tail=[(1.75, 1.75, 2), (1.25, 1.25, 1.5)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", shell="dome",
        pal=dict(SKIN=skin("tan"), HAIR="#6a6a5a", FUR="#6a6a58", LIGHT="#a8a890", MARK="#4a4a3a", EYE="#2a2a1e", CLOTH="#c8b888", TRIM="#6a5a3a",
                 extra=dict(shell="#3a3a30", shell_band="#5e5e4a")),
        variants={"santa_cruz": dict(FUR="#5a5448", extra=dict(shell="#2e2a24", shell_band="#4a463a")),
                  "pinta": dict(FUR="#7a7a62", HAIR="#8a8a72", extra=dict(shell="#4a4a38", shell_band="#7a7a5a")),
                  "espanola": dict(FUR="#7a6a52", HAIR="#5a4a3a", extra=dict(shell="#4a3e2e", shell_band="#7a6a4e")),
                  "aldabra": dict(FUR="#5a4a3a", HAIR="#3a2e22", extra=dict(shell="#2a2018", shell_band="#5a4630"))}),
    # eleventh wave: the anvil tortoise, a smith's anvil for a shell
    "anvil_tortoise_lizardfolk": dict(
        build="curvy", outfit=dict(top=["bandeau"], bottom="shorts", extras=["belt", "bracers"], colors=dict(cloth2="#3a3a46", leather="#5a3a22")),
        hair="bun", bangs="blunt", ears=None, legs="scales", foot="claw", tail=[(1.5, 1.5, 2), (1, 1, 1.5)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", anvil=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a3a6a", FUR="#4a6ab0", LIGHT="#b8c8f0", MARK="#2a3a6a", EYE="#e8b030", CLOTH="#c8a050", TRIM="#e8c060",
                 extra=dict(anvil="#5a5e66")),
        variants={"copper": dict(FUR="#5a8aa8", CLOTH="#3a6a5a", extra=dict(anvil="#b8683a")),
                  "gold": dict(FUR="#5a6aa8", CLOTH="#a83a3a", extra=dict(anvil="#d8b040")),
                  "blackstone": dict(FUR="#3a3a52", HAIR="#141420", EYE="#f86a2a", CLOTH="#6a3a2a", extra=dict(anvil="#2a2830")),
                  "chipped": dict(FUR="#6a7a9a", HAIR="#4a5068", CLOTH="#8a7a5a", extra=dict(anvil="#7a7e86", chipped=True))}),
    "box_turtle_lizardfolk": dict(
        build="petite", outfit=dict(top=["bandeau", "vest"], bottom="shorts", extras=["scarf"], colors=dict(scarf="#c8782a", leather="#4a3a22")),
        hair="bob", bangs="messy", ears=None, legs="scales", foot="claw", tail=[(1.25, 1.25, 1.5), (0.75, 0.75, 1)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", shell="dome",
        pal=dict(SKIN=skin("fair"), HAIR="#4a3418", FUR="#6a4a2a", LIGHT="#e8a830", MARK="#e8a830", EYE="#c83a2a", CLOTH="#4a6a3a", TRIM="#e8a830",
                 pattern="spots", extra=dict(shell="#4a3418", shell_band="#e8a830")),
        variants={"ornate": dict(MARK="#f0c040", extra=dict(shell="#3a2a14", shell_band="#f0c040")),
                  "three_toed": dict(FUR="#7a6a3a", MARK="#c88a3a", EYE="#a85a2a", extra=dict(shell="#6a5a2e", shell_band="#a8883a")),
                  "gulf_coast": dict(FUR="#3a2e22", HAIR="#2a1e14", extra=dict(shell="#2e2418", shell_band="#7a6a3a")),
                  "desert": dict(FUR="#a8885a", HAIR="#8a6a3a", MARK="#f0d890", extra=dict(shell="#8a6a3a", shell_band="#f0d890"))}),
    "softshell_turtle_lizardfolk": dict(
        build="slim", outfit=dict(top=["sports"], bottom="shorts", extras=["belt"], colors=dict(cloth2="#4a6a7a")),
        hair="ponytail", bangs="swept", ears=None, legs="scales", foot="claw", tail=[(1.25, 1.25, 1.5), (0.75, 0.75, 1)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", shell="flat", snout=True,
        pal=dict(SKIN=skin("olive"), HAIR="#5a5a3a", FUR="#8a8a5a", LIGHT="#d8d8b0", MARK="#3a3a22", EYE="#2a2a18", CLOTH="#e8e0c8", TRIM="#4a6a7a",
                 pattern="spots", extra=dict(shell="#7a7a50", shell_band="#5a5a3a")),
        variants={"spiny": dict(FUR="#7a7a52", MARK="#2a2a18", extra=dict(shell="#6a6a44", shell_band="#3a3a26")),
                  "smooth": dict(FUR="#9a8a5a", HAIR="#6a5a3a", extra=dict(shell="#8a7a50", shell_band="#7a6a44")),
                  "chinese": dict(FUR="#5a6a4a", HAIR="#3a4a2a", extra=dict(shell="#4a5a3a", shell_band="#3a4a2e")),
                  "florida": dict(FUR="#5a4a3a", HAIR="#3a2e22", extra=dict(shell="#4a3a2a", shell_band="#2e2418"))}),
    "matamata_lizardfolk": dict(
        build="slim", outfit=dict(top=["halter"], bottom="sarong", extras=["armlets"], colors=dict(cloth2="#5a4a2a")),
        hair="shaggy", bangs="messy", ears=None, legs="scales", foot="claw", tail=[(1.25, 1.25, 1.5), (0.75, 0.75, 1)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", shell=True, snout=True,
        pal=dict(SKIN=skin("olive"), HAIR="#5a4a2a", FUR="#6a5030", LIGHT="#b89a6a", MARK="#3a2a18", EYE="#3a2a14", CLOTH="#4a5a2a", TRIM="#8a6a3a",
                 pattern="bands", pupil=(1.4, 1.4), extra=dict(shell="#4a3a22", shell_band="#6a5a32")),
        variants={"mossy": dict(HAIR="#4a5a2a", FUR="#5a5a32", extra=dict(shell="#3e4a24", shell_band="#5a6a30")),
                  "dark": dict(FUR="#3a2e1e", HAIR="#2a2014", extra=dict(shell="#2a2014", shell_band="#4a3a24")),
                  "orinoco": dict(FUR="#8a5a3a", HAIR="#6a3a22", MARK="#a83a2a", extra=dict(shell="#5a3a22", shell_band="#8a5a32")),
                  "pale": dict(FUR="#a89a7a", HAIR="#8a7a5a", LIGHT="#e0d4b8", extra=dict(shell="#7a6a4e", shell_band="#a89a72"))}),
    "gecko_lizardfolk": dict(
        build="petite", outfit=dict(top=["sports"], bottom="shorts", extras=["armlets"], colors=dict(cloth2="#3a4a5a")),
        hair="bob", bangs="messy", ears=None, legs="scales", foot="claw", tail=[(2, 2, 3), (2.25, 2.25, 3), (1.75, 1.75, 3), (1, 1, 2.5)],
        tail_rest=[(-30, 0, 0), (10, 0, 0), (8, 0, 0), (6, 0, 0)], tail_look="scale",
        pal=dict(SKIN=skin("peach"), HAIR="#e8c050", FUR="#f0d060", LIGHT="#f8f0d8", MARK="#5a3a2a", EYE="#c8a040", CLOTH="#3a8a7a", TRIM="#f0d060",
                 pattern="spots", pupil=(0.9, 3.4)),
        variants={"crested": dict(FUR="#e8803a", HAIR="#c8602a", MARK="#7a3a1a", LIGHT="#f8c8a0", EYE="#a8c040"),
                  "tokay": dict(FUR="#7a8aa8", HAIR="#5a6a88", MARK="#f0803a", LIGHT="#d8e0f0", EYE="#f0b030"),
                  "day": dict(FUR="#4ac83a", HAIR="#2a9a2a", MARK="#e83a2a", LIGHT="#c8f0b0", EYE="#2a2a2a", pattern="spots"),
                  "gargoyle": dict(FUR="#8a8478", HAIR="#6a6458", MARK="#3a3428", LIGHT="#c8c0b0", pattern="blotches")}),
    "chupacabra_lizardfolk": dict(
        build="athletic", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps"], colors=dict(bandage="#8a8478", cloth2="#3a3430")),
        hair="shaggy", bangs="jagged", ears=None, legs="scales", foot="claw", tail=LIZARD_TAIL, tail_rest=DROOP, tail_look="scale", spines=True, teeth=True,
        pal=dict(SKIN=skin("olive"), HAIR="#3a4a3a", FUR="#6a7a6a", LIGHT="#a8b0a0", MARK="#c83a2a", EYE="#e83a2a", CLOTH="#3a3430", TRIM="#8a8478",
                 pattern="bands", pupil=(0.9, 3.4)),
        variants={"dark": dict(FUR="#3a3a3e", HAIR="#1e1e22", MARK="#a82a2a"),
                  "sand": dict(FUR="#b8a07a", HAIR="#8a744e", MARK="#7a3a1a", LIGHT="#e0d0b0"),
                  "blue": dict(FUR="#5a6a8a", HAIR="#3a4a6a", MARK="#e8c040"),
                  "red": dict(FUR="#a8543a", HAIR="#6a2a1a", MARK="#2a1a14", LIGHT="#d8a080")}),
    "kappa_lizardfolk": dict(
        build="petite", outfit=dict(top=["halter", "vest"], bottom="shorts", extras=["belt"], colors=dict(cloth2="#3a5a7a", leather="#4a6a3a")),
        hair="bob", bangs="blunt", ears=None, legs="scales", foot="claw", tail=[(1.5, 1.5, 2), (1, 1, 1.5)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", shell=True, dish=True,
        pal=dict(SKIN=skin("peach"), HAIR="#3a7a3a", FUR="#5aa84a", LIGHT="#c8e8a0", MARK="#2a5a2a", EYE="#e8b030", CLOTH="#e8e0c8", TRIM="#3a5a7a",
                 pupil=(1.4, 1.4), extra=dict(shell="#4a7a2a", shell_band="#2a4a1e", dish="#e8f4f8")),
        variants={"pond": dict(FUR="#3ab0a0", HAIR="#2a7a7a", MARK="#1e5a5a", extra=dict(shell="#2a7a6a", shell_band="#1e4a44", dish="#e8f4f8")),
                  "red": dict(FUR="#c84a3a", HAIR="#8a2a1e", MARK="#5a1a10", extra=dict(shell="#8a3a1a", shell_band="#4a1e10", dish="#f0e8e0")),
                  "mountain": dict(FUR="#8a6a3a", HAIR="#5a4024", MARK="#3a2a14", extra=dict(shell="#6a4a2a", shell_band="#3a2a18", dish="#e8f0f0")),
                  "white": dict(FUR="#e8ece4", HAIR="#d8e0d8", MARK="#a8b0a8", LIGHT="#ffffff", EYE="#c84a4a",
                                extra=dict(shell="#c8ccc0", shell_band="#8a8e84", dish="#f4fcff"))}),
}


def declare_hook(sp, d):
    out = []
    if d.get("spines"):
        out += [("spine", .5, 1.25, 1)]
    if d.get("shell") == "flat":                         # the softshell: one thin, wide disc
        out += [("carapace", 8, 8.5, 1), ("plastron", 5, 5, .5)]
    elif d.get("shell"):
        out += [("carapace", 7, 8, 2.5), ("carapace_top", 5.5, 6, 1.25), ("plastron", 5, 5, .5)]
    if d.get("shell") == "dome":                         # the tortoises' high dome
        out += [("carapace_dome", 4, 4.5, 1.5)]
    if d.get("teeth"):
        out += [("snout", 3.5, 1, 1.5)]
    elif d.get("snout"):                                 # a snorkel nose (softshell, matamata)
        out += [("snout", 1.5, 1, 2)]
    if d.get("dish"):
        out += [("dish", 4, .5, 4), ("dish_water", 3, .25, 3)]
    if d.get("anvil"):                                   # the anvil on her back: base, waist, the face and its horn
        out += [("anvil_base", 6, 2, 4.5), ("anvil_waist", 3.5, 2, 3), ("anvil_face", 8, 2.5, 4.5), ("anvil_horn", 2.5, 1.75, 2.5)]
    if d.get("nurse_kit"):
        out += [("nurse_cap", 5, 2, 3.5), ("nurse_snout", 4, 2.5, 3.5), ("dress_top", 6, 5, 5), ("dress_hem", 7.5, 4.5, 6),
                ("syr_barrel", 3, 3, 10), ("syr_plunger", 1, 1, 3), ("syr_thumb", 3.5, 3.5, .5), ("syr_needle", .5, .5, 4.5)]
    return out


def paint_hook(pt, pal, d):
    iron = G.shades(pal.extra.get("anvil", "#5a5e66"))
    chipped = pal.extra.get("chipped")
    for p in ("anvil_base", "anvil_waist", "anvil_face", "anvil_horn"):
        pt(p, "all", lambda f, x, y, w, h: pick(iron, 0.35 if chipped and (x * 7 + y * 3) % 11 == 0 else
                                                0.45 + 0.25 * (f == "up") + 0.1 * (y == h - 1) + (noise(x, y, 71) - 0.5) * 0.08, x, y))
    pt("spine", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.45 + 0.2 * (y == 0), x, y))
    shell = G.shades(pal.extra.get("shell", "#7a5a2a"))
    band = G.shades(pal.extra.get("shell_band", "#c8a050"))

    def scutes(f, x, y, w, h):
        edge = x % 6 == 0 or y % 6 == 0
        return pick(band if edge else shell, 0.4 + 0.2 * (noise(x // 3, y // 3, 5) > 0.5), x, y)
    for p in ("carapace", "carapace_top", "carapace_dome"):
        pt(p, "all", scutes)
    pt("plastron", "all", lambda f, x, y, w, h: pick(G.shades("#e8d8a0"), 0.5 + 0.2 * (y % 3 == 0), x, y))
    dish = G.shades(pal.extra.get("dish", "#e8f4f8"))
    pt("dish", "all", lambda f, x, y, w, h: pick(dish, 0.55 + 0.25 * (x in (0, w - 1) or y in (0, h - 1)), x, y))
    pt("dish_water", "all", lambda f, x, y, w, h: pick(G.shades("#5ab0e8"), 0.6 + 0.2 * ((x + y) % 3 == 0), x, y))
    nurse = pal.extra.get("nurse")                     # Catherine's things, drawn only on her
    show = lambda fn: (lambda f, x, y, w, h: fn(f, x, y, w, h) if nurse else H.CLEAR)
    white, sky, red = G.shades("#f8f8f8"), G.shades("#c8dcea"), G.shades("#d82a2a")
    steel, ink = G.shades("#8a8ea0"), G.shades("#1e1a1e")

    def cross(x, y, w, h):                              # a red cross in the middle of a face
        cx, cy = x + 0.5 - w / 2, y + 0.5 - h / 2
        return (abs(cx) < w * 0.12 and abs(cy) < h * 0.36) or (abs(cy) < h * 0.12 and abs(cx) < w * 0.3)
    pt("nurse_cap", "all", show(lambda f, x, y, w, h: pick(red, 0.5, x, y) if f == "north" and cross(x, y, w, h)
                                else pick(sky if f in ("east", "west") else white, 0.6, x, y)))
    spots = lambda f, x, y, w, h: pick(pal.MARK, 0.5, x, y) if (x * 7 + y * 11) % 13 < 2 else pick(pal.FUR, 0.55 + 0.15 * (f == "up"), x, y)
    pt("nurse_snout", "all", show(lambda f, x, y, w, h: pick(ink, 0.2, x, y) if f in ("east", "west", "north") and y == h - 3
                                  else pick(pal.MARK, 0.25, x, y) if f == "north" and y == 2 and x in (2, w - 3) else spots(f, x, y, w, h)))

    def dress(f, x, y, w, h, top):
        if f == "down":
            return H.CLEAR
        if top and f == "north" and y in (4, 5, 9, 10) and x in (w // 3, w - 1 - w // 3):
            return pick(sky, 0.55, x, y)                 # two rows of buttons
        return pick(sky if f in ("east", "west", "south") and x % 6 < 2 else white, 0.55 + 0.1 * (y == 0), x, y)
    pt("dress_top", "all", show(lambda f, x, y, w, h: dress(f, x, y, w, h, True)))
    pt("dress_hem", "all", show(lambda f, x, y, w, h: dress(f, x, y, w, h, False)))
    pt("syr_barrel", "all", show(lambda f, x, y, w, h: pick(ink, 0.2, x, y) if f in ("up", "east", "west") and y % 6 == 0 and x < w * 0.6
                                 else pick(red, 0.5, x, y) if f == "north" or (f in ("up", "down", "east", "west") and y < h * 0.55)
                                 else pick(sky, 0.6, x, y)))
    pt("syr_plunger", "all", show(lambda f, x, y, w, h: pick(steel, 0.55, x, y)))
    pt("syr_thumb", "all", show(lambda f, x, y, w, h: pick(sky, 0.6, x, y)))
    pt("syr_needle", "all", show(lambda f, x, y, w, h: pick(steel, 0.7, x, y)))
    if nurse:
        pt("arm_up", ("north", "south", "east", "west"), lambda f, x, y, w, h: (pick(red, 0.5, x, y) if f in ("east", "west") and cross(x, y - 2, w, 8)
                                                                               else pick(white, 0.6, x, y)) if 2 <= y < 10 else None)   # armbands
        pt("paw", "all", lambda f, x, y, w, h: pick(ink, 0.3 + 0.1 * (f == "up"), x, y))            # black shoes
        pt("head", ("north",), lambda f, x, y, w, h: pick(pal.MARK, 0.55, x, y) if (x, y) in {(24, 20), (26, 20), (23, 21), (24, 21), (25, 21), (26, 21),
                                                                                                 (27, 21), (24, 22), (25, 22), (26, 22), (25, 23)} else None)
    teeth = d.get("teeth")
    pt("snout", "all", lambda f, x, y, w, h: pal.WHITE if teeth and f == "north" and y == h - 1 and x % 2 == 0 else H.skin_fn(pal, 0.66)(f, x, y, w, h))


def build_hook(r, g, d):
    z = g.z
    if d.get("anvil"):                                   # lying along her back like a shell, the face outward
        cd = G.S("chest")[2]
        b = cd / 2 - 0.2 + z
        r.add("anvil", "body", [0, 16, b], [cube("anvil_base", [-3, 12.0, b]), cube("anvil_waist", [-1.75, 14.0, b + 0.75]),
                                             cube("anvil_face", [-4, 16.0, b]), cube("anvil_horn", [4, 16.4, b + 1.0])])
    if d.get("spines"):
        cs = [cube("spine", [-0.25, 13 + i * 1.6, 1.4 + z]) for i in range(4)]
        r.put("body", *cs)
        for i, b in enumerate(("tail", "tail_2", "tail_3")):
            if b in r.b:
                piv = r.b[b]["pivot"]
                r.put(b, cube("spine", [-0.25, piv[1] + 0.6, piv[2] + 1.0]), cube("spine", [-0.25, piv[1] + 0.4, piv[2] + 2.6]))
    if d.get("shell") == "flat":
        cd = G.S("chest")[2]
        r.add("shell", "body", [0, 16, 1.5 + z], [cube("carapace", [-4, 12.25, cd / 2 - 0.2 + z])])
    elif d.get("shell"):
        cd = G.S("chest")[2]
        cs = [cube("carapace", [-3.5, 12.5, cd / 2 - 0.2 + z]), cube("carapace_top", [-2.75, 13.5, cd / 2 + 2.2 + z])]
        if d["shell"] == "dome":
            cs.append(cube("carapace_dome", [-2, 14.25, cd / 2 + 3.4 + z]))
        r.add("shell", "body", [0, 16, 1.5 + z], cs)
    if d.get("teeth"):
        r.put("head", cube("snout", [-1.75, g.hy + 0.5, -5.2 + z]))
    elif d.get("snout"):
        r.put("head", cube("snout", [-0.75, g.hy + 1.5, -5.7 + z]))
    if d.get("nurse_kit"):                              # Catherine: cap, snout, nurse's dress, and the syringe in her right hand
        hy = g.hy
        r.put("head", cube("nurse_cap", [-2.5, hy + 7.6, -2.75 + z]), cube("nurse_snout", [-2, hy + 0.2, -7.4 + z]))
        r.add("dress", "body", [0, 15, z], [cube("dress_top", [-3, 14.6, -2.5 + z]), cube("dress_hem", [-3.75, 10.2, -3 + z])])
        px, py, pz = r.b["hand_r"]["pivot"]
        r.add("syringe", "hand_r", [px, py - 1.5, pz], [cube("syr_barrel", [px - 1.5, py - 3.0, pz - 8.5]), cube("syr_plunger", [px - 0.5, py - 2.0, pz + 1.5]),
                                                        cube("syr_thumb", [px - 1.75, py - 3.25, pz + 4.5]), cube("syr_needle", [px - 0.25, py - 1.75, pz - 13.0])])
    if d.get("dish"):                                   # the kappa's dish (sara), set into her hair, with water in it
        r.add("dish", "head", [0, g.hy + 8.6, z], [cube("dish", [-2, g.hy + 8.6, -2 + z]), cube("dish_water", [-1.5, g.hy + 9.0, -1.5 + z])])


def anim_extra(an, sp, d):
    """basking in the sun ("bask"), the crocodile's death roll ("roll"), the turtle hiding in her shell ("retract")"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("bask", 4.0, True, merge({"root": {"rotation": hold(-90, 0, 0), "position": hold(0, 2.5, 0)}, "head": {"rotation": hold(-30, 0, 0)},
                                     "eyelids": {"position": hold(*H.OUT_)}}, sym("arm", hold(-170, 0, 20))))
    an.add("roll", 1.0, True, merge({"root": {"rotation": Tk((0, (0, 0, 0)), (0.5, (0, 0, 180)), (1.0, (0, 0, 360))), "position": hold(0, 4, 0)},
                                     "mouth": {"position": hold(*H.OUT_)}}, sym("arm", hold(-90, 0, 0))))
    an.add("retract", 1.0, False, merge({
        "body": {"rotation": Tk((0, (0, 0, 0)), (0.5, (70, 0, 0)), (1, (80, 0, 0))), "position": Tk((0, (0, 0, 0)), (1, (0, -6, 2)))},
        "leg_r": {"position": hold(0, -5, 0), "rotation": hold(-90, 0, 0)}, "leg_l": {"position": hold(0, -5, 0), "rotation": hold(-90, 0, 0)},
        "shin_r": {"rotation": hold(150, 0, 0)}, "shin_l": {"rotation": hold(150, 0, 0)}, "head": {"rotation": hold(40, 0, 0)},
        "eyelids": {"position": hold(*H.OUT_)}}, sym("arm", hold(-120, 0, -20))))


if __name__ == "__main__":
    F.run_family("lizardfolk", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
