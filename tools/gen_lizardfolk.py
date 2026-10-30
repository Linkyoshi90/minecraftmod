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
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

PRONE, PRONE_AT = (80, 0, 0), (0, 7, 12)                  # the archelon's belly-down pose
LIZARD_TAIL = [(2.5, 2.5, 4), (2, 2, 4), (1.5, 1.5, 4), (1, 1, 3.5)]
DROOP = [(-38, 0, 0), (10, 0, 0), (10, 0, 0), (8, 0, 0)]

SPECIES = {
    # twenty-third wave: the argentinosaurus, the greatest of the long-necked dinosaurs: tall and gentle, pale blue with orange
    # stripes and a cream belly, a long neck lifting her head high, a long heavy tail
    "argentinosaurus_lizardfolk": dict(
        build="curvy", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["pearls"], colors=dict(cloth2="#e8a060")),
        hair="bob", bangs="swept", ears=None, legs="scales", foot="claw",
        tail=[(3, 3, 5), (2.5, 2.5, 5), (2, 2, 5), (1.5, 1.5, 4.5), (1, 1, 4)], tail_rest=[(-30, 0, 0), (12, 0, 0), (10, 0, 0), (8, 0, 0), (6, 0, 0)],
        tail_look="scale", lneck=9,
        pal=dict(SKIN=skin("fair"), HAIR="#6a98c8", FUR="#9ac0e4", LIGHT="#f4ead4", MARK="#e0904a", EYE="#2a4a7a", CLOTH="#e8a060", TRIM="#f4ead4",
                 pattern="bands"),
        variants={"green": dict(HAIR="#4a7a3a", FUR="#8ab878", MARK="#3a5a2a", CLOTH="#c8a050", EYE="#5a3a1e"),
                  "sand": dict(HAIR="#a87a4a", FUR="#d8b888", MARK="#8a5a2a", CLOTH="#5a8aa8"),
                  "grey": dict(HAIR="#5a5e66", FUR="#a8acb4", MARK="#4a6a8a", CLOTH="#a8483a", EYE="#3a2a1a"),
                  "pink": dict(HAIR="#c86a8a", FUR="#f0b8c8", MARK="#a84a6a", CLOTH="#6a8ac8")}),
    # twenty-second wave: the archelon, the greatest sea turtle that ever lived: crystal-blue scales, a great shell ridged with three
    # keels like a shield, flippers at her wrists
    "archelon_lizardfolk": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts", extras=["pearls"], colors=dict(cloth2="#2a5aa8")),
        hair="pixie", bangs="swept", ears=None, legs="scales", foot="claw", tail=[(1.5, 1.5, 2), (1, 1, 1.5)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", shell="great", keels=True, wflippers=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a6ac0", FUR="#5aa0e8", LIGHT="#e4f0fc", MARK="#2a5aa8", EYE="#2a4a8a", CLOTH="#e4f0fc", TRIM="#2a5aa8",
                 extra=dict(shell="#3a7ad8", shell_band="#a8d8f8", keel="#e4f4ff")),
        variants={"green": dict(HAIR="#2a6a3a", FUR="#5aa86a", MARK="#2a5a3a", TRIM="#2a5a3a", extra=dict(shell="#3a7a4a", shell_band="#a8d8a8", keel="#e4f4e4")),
                  "brown": dict(HAIR="#6a4a2a", FUR="#a87a4a", MARK="#5a3a1e", TRIM="#5a3a1e", extra=dict(shell="#7a5a2a", shell_band="#d8b878", keel="#f4e8d0")),
                  "black": dict(HAIR="#1e2228", FUR="#3a3e46", MARK="#141618", TRIM="#141618", EYE="#5ab8e8",
                                extra=dict(shell="#22262c", shell_band="#5a6470", keel="#a8b4c0")),
                  "white": dict(HAIR="#e8eef4", FUR="#d8e4f0", MARK="#8a9ab0", TRIM="#8a9ab0", EYE="#c83a5a",
                                extra=dict(shell="#c8d8e8", shell_band="#ffffff", keel="#ffffff"))}),
    # twentieth wave: the turtworld, a great blue tortoise who carries a little world on her shell: turf, two snowy peaks and a waterfall
    "turtworld_lizardfolk": dict(
        build="strong", outfit=dict(top=["halter"], bottom="sarong", extras=["belt"], colors=dict(cloth2="#2a4a8a", leather="#5a4a2a")),
        hair="bun", bangs="blunt", ears=None, legs="scales", foot="claw", tail=[(1.75, 1.75, 2), (1.25, 1.25, 1.5)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", shell="dome", island=True,
        pal=dict(SKIN="#7a9ad8", HAIR="#1e3270", FUR="#3a5ab0", LIGHT="#a8c0e8", MARK="#1e3270", EYE="#c83a3a", CLOTH="#2a4a8a", TRIM="#e8e0c8",
                 extra=dict(shell="#2a4a9a", shell_band="#6a8ad8", turf="#5aa04a", rock="#8a8a94", snow="#f4f8ff", water="#6ac8f0")),
        variants={"green": dict(SKIN="#7ab08a", FUR="#4a8a5a", LIGHT="#c8e8c8", MARK="#1e4a2a", HAIR="#3a6a2a",
                                extra=dict(shell="#5a4a2a", shell_band="#a8904a", turf="#6ab84a", rock="#8a8478", snow="#ffffff", water="#5ab8e8")),
                  "sand": dict(SKIN="#d8c098", FUR="#b8945a", LIGHT="#f0e0c0", MARK="#6a4a24", HAIR="#c8a050", EYE="#3a6ac8",
                               extra=dict(shell="#8a6a3a", shell_band="#d8b878", turf="#e8d090", rock="#c8986a", snow="#f8f0e0", water="#3ab8c8")),
                  "frost": dict(SKIN="#c8dcf0", FUR="#8aa8d0", LIGHT="#f0f8ff", MARK="#3a5a8a", HAIR="#e8f4ff", EYE="#3a8ad8",
                                extra=dict(shell="#5a7ab0", shell_band="#c8dcf4", turf="#e8f4ff", rock="#8aa0c0", snow="#ffffff", water="#a8e8ff")),
                  "ember": dict(SKIN="#a87a6a", FUR="#6a3a2e", LIGHT="#d8a890", MARK="#2a1410", HAIR="#3a3a3a", EYE="#f8c040",
                                extra=dict(shell="#2a2224", shell_band="#c84a1e", turf="#4a4044", rock="#2a2426", snow="#f8a040", water="#f86a1e"))}),
    # twentieth wave (folklore): the basilisk, the king of serpents, a little blue lizard with a crest of spines down her back and a gold
    # crown; her gaze turns things to stone
    "basilisk_lizardfolk": dict(
        build="petite", outfit=dict(top=["bandeau", "capelet"], bottom="shorts", extras=["belt"], colors=dict(cloth2="#2a4a8a")),
        hair="ponytail", bangs="swept", ears=None, legs="scales", foot="claw", tail=[(2, 2, 4), (1.5, 1.5, 4), (1, 1, 4)],
        tail_rest=[(-25, 0, 0), (10, 0, 0), (10, 0, 0)], tail_look="scale", spines=True, crown=True,
        pal=dict(SKIN="#a8c8f0", HAIR="#3a6ac8", FUR="#5a8ad8", LIGHT="#d8e8fc", MARK="#2a4a9a", EYE="#e8c030", CLOTH="#2a4a8a", TRIM="#e8c040",
                 pupil=(0.9, 3.4), extra=dict(crown="#e8c040")),
        variants={"green": dict(SKIN="#a8d8a0", HAIR="#3a8a3a", FUR="#5aa85a", LIGHT="#d8f0d0", MARK="#2a5a2a", CLOTH="#2a5a2a", EYE="#e83a2a"),
                  "stone": dict(SKIN="#b8b8b0", HAIR="#6a6a64", FUR="#8a8a84", LIGHT="#d8d8d0", MARK="#4a4a44", CLOTH="#4a4a50", EYE="#e8c030"),
                  "black": dict(SKIN="#6a6a7a", HAIR="#1e1e28", FUR="#2a2a36", LIGHT="#8a8aa0", MARK="#0e0e14", CLOTH="#5a1e24", EYE="#e83a2a"),
                  "gold": dict(SKIN="#f0d8a0", HAIR="#c8902a", FUR="#d8a83a", LIGHT="#f8ecc8", MARK="#8a5a14", CLOTH="#3a2a5a", EYE="#5ad83a",
                               extra=dict(crown="#c8ccd4"))}),
    # eighteenth wave: the turtfolk, a tall blue-scaled turtle warrior in bronze, a big brown shell, long brown hair, a yellow flower
    "turtfolk_lizardfolk": dict(
        build="athletic", outfit=dict(top=["bandeau"], bottom="loincloth", extras=["bracers", "belt"], colors=dict(cloth2="#7a5a2a", leather="#8a6a3a")),
        hair="flowing", bangs="swept", ears=None, legs="scales", foot="claw", tail=[(1.75, 1.75, 2.5), (1.25, 1.25, 2)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", shell=True, flower=True,
        pal=dict(SKIN="#7aa0d8", HAIR="#7a4a22", FUR="#5a88d0", LIGHT="#c8dcf4", MARK="#3a5a9a", EYE="#c83a5a", CLOTH="#a8783a", TRIM="#e8c060",
                 pattern="spots", extra=dict(shell="#7a5228", shell_band="#c89a50", flower="#f8d040", flower_in="#f89a3a")),
        variants={"black": dict(SKIN="#6a6a74", FUR="#4a4a54", LIGHT="#a8a8b4", MARK="#2a2a32", HAIR="#1e1a1a", EYE="#e8c040",
                                extra=dict(shell="#2a2622", shell_band="#7a6a5a", flower="#f4f0f0", flower_in="#e8c040")),
                  "green": dict(SKIN="#7ab88a", FUR="#5aa06a", LIGHT="#d0f0c8", MARK="#2e6a3e", HAIR="#5a3a1e",
                                extra=dict(shell="#5a4a22", shell_band="#a8904a", flower="#f87aa8", flower_in="#f8e070")),
                  "purple": dict(SKIN="#9a84c8", FUR="#7a64b0", LIGHT="#e0d4f4", MARK="#4a3a7a", HAIR="#3a2a4a", EYE="#e8c040",
                                 extra=dict(shell="#5a3a3a", shell_band="#a87a6a", flower="#f8f0a0", flower_in="#f8a03a")),
                  "red": dict(SKIN="#d88a7a", FUR="#c8604a", LIGHT="#f8dccc", MARK="#7a2a1e", HAIR="#3a1e14", EYE="#3a8ad8",
                              extra=dict(shell="#6a3a1e", shell_band="#c8803a", flower="#f8f8f0", flower_in="#f8d040"))}),
    # seventeenth wave: the conch mail turtle, a little sea turtle in a scaled blue shell with a conch's spire rising from it, pearls
    "conch_turtle_lizardfolk": dict(
        build="petite", outfit=dict(top=["bandeau", "capelet"], bottom="shorts", extras=["pearls"], colors=dict(cloth2="#3a8ad8", leather="#e8c060")),
        hair="bob", bangs="jagged", ears=None, legs="scales", foot="claw", tail=[(1.25, 1.25, 1.5), (0.75, 0.75, 1)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", shell="dome", conch=True,
        pal=dict(SKIN=skin("fair"), HAIR="#3a7ad8", FUR="#4a8ae0", LIGHT="#c8e0f8", MARK="#1e4a9a", EYE="#a8682a", CLOTH="#2a5ab0", TRIM="#e8c060",
                 pattern="spots", extra=dict(shell="#2a5ab0", shell_band="#8ac0f0", conch="#f0e4d0", conch_band="#3a7ad8")),
        variants={"coral": dict(HAIR="#e8705a", FUR="#f08a6a", LIGHT="#ffe0d0", MARK="#a83a2a", CLOTH="#c84a3a",
                                extra=dict(shell="#c84a3a", shell_band="#f8c0a8", conch="#fff0e4", conch_band="#e8705a")),
                  "kelp": dict(HAIR="#3a8a5a", FUR="#4a9a6a", LIGHT="#c8f0d0", MARK="#1e5a3a", CLOTH="#2a6a4a",
                               extra=dict(shell="#2a6a4a", shell_band="#a8e0b0", conch="#f0ecd8", conch_band="#3a8a5a")),
                  "sand": dict(HAIR="#c8a060", FUR="#d8b070", LIGHT="#fff4dc", MARK="#8a6a3a", CLOTH="#3a7ad8",
                               extra=dict(shell="#a8803a", shell_band="#f0d890", conch="#fff8ec", conch_band="#e8a060")),
                  "abyss": dict(HAIR="#1e2450", FUR="#2a3060", LIGHT="#6a7ac8", MARK="#0e1230", EYE="#5ae8f0", CLOTH="#14183a",
                                extra=dict(shell="#1e2450", shell_band="#5ae8f0", conch="#d8e0f0", conch_band="#2a3a8a"))}),
    # fifteenth wave: the crocora, a round little blue crocodile in a gold collar, a cream belly
    "crocora_lizardfolk": dict(
        build="petite", outfit=dict(top=["bandeau"], bottom="wrap_skirt", extras=["usekh", "armlets"], colors=dict(cloth2="#2a4a9a")),
        hair="bob", bangs="blunt", ears=None, legs="scales", foot="claw", tail=[(3, 2.5, 4), (2.5, 2, 4), (2, 1.5, 3.5), (1.25, 1.25, 3)],
        tail_rest=[(-25, 0, 0), (8, 0, 0), (6, 0, 0), (4, 0, 0)], tail_look="scale", spines=True, teeth=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a5ad8", FUR="#3a7ae8", LIGHT="#f4ecd0", MARK="#1e3a8a", EYE="#e8a030", CLOTH="#e8c050", TRIM="#2a4a9a",
                 pattern="bands", pupil=(0.9, 3.4)),
        variants={"green": dict(HAIR="#2a7a3a", FUR="#3a9a4a", MARK="#1a4a22"),
                  "olive": dict(HAIR="#4a5a2a", FUR="#6a7a3a", MARK="#2a3a1a", CLOTH="#c8a040"),
                  "gold": dict(HAIR="#c8902a", FUR="#e0b040", MARK="#7a5410", CLOTH="#2a4a9a", TRIM="#e8c050"),
                  "black": dict(HAIR="#1e1c22", FUR="#2a2830", MARK="#0a080e", LIGHT="#a8a4b0", EYE="#e8d040")}),
    # fifteenth wave: the iron kobold, a little blue kobold smith in iron plate, a big hammer in her right hand
    "iron_kobold_lizardfolk": dict(
        build="petite", outfit=dict(top=["sarashi", "vest"], bottom="shorts", extras=["pauldron", "bracers", "belt"],
                                    colors=dict(cloth2="#5a5e6a", leather="#4a3a2a")),
        hair="shaggy", bangs="messy", ears=None, legs="scales", foot="claw", horns="devil", tail=LIZARD_TAIL, tail_rest=DROOP, tail_look="scale",
        spines=True, teeth=True, hammer=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e2e6a", FUR="#3a5ab8", LIGHT="#c8d4f0", MARK="#1e2a5a", EYE="#e8a030", CLOTH="#7a7e8a", TRIM="#c8a050",
                 pattern="bands", pupil=(0.9, 3.4), extra=dict(horn="#e8dcc0", steel="#8a8e9a")),
        variants={"copper": dict(CLOTH="#b8642a", TRIM="#e8b860", extra=dict(horn="#e8dcc0", steel="#c87a3a")),
                  "red": dict(HAIR="#6a1a12", FUR="#b83a2a", LIGHT="#f0c0a8", MARK="#5a1a10"),
                  "green": dict(HAIR="#1e4a2a", FUR="#3a8a4a", LIGHT="#c8e8c0", MARK="#1a3a22"),
                  "black": dict(HAIR="#141218", FUR="#2a2830", LIGHT="#8a8890", MARK="#0a080e", EYE="#e83a2a")}),
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
                                TRIM="#d82a2a", pattern="spots", extra=dict(flame="#f87ab0", claw="#2a1a1a", nurse=True)),
                  # homage (2026-10-30): Charmander (Pokemon): orange, a cream belly, blue eyes, the flame at her tail tip
                  "ember_tail": dict(SKIN=skin("tan"), FUR="#f08a30", HAIR="#f08a30", LIGHT="#f8e0a0", MARK="#c86a1e", EYE="#2a5ad8", CLOTH="#f8e0a0",
                                     TRIM="#c86a1e", pattern="solid", extra=dict(flame="#f8c030", claw="#f8f0e8"))}),
    "chameleon_lizardfolk": dict(
        build="slim", outfit=dict(top=["halter"], bottom="sarong", extras=["armlets"], colors=dict(cloth2="#6a3a8a")),
        hair="bun", bangs="swept", ears=None, legs="scales", foot="claw", tail=LIZARD_TAIL, tail_look="scale",
        tail_rest=[(-20, 0, 0), (45, 0, 0), (60, 0, 0), (70, 0, 0)], tail_sway=3,
        pal=dict(SKIN=skin("peach"), HAIR="#5ab84a", FUR="#6ac850", LIGHT="#c8f0a0", MARK="#3a8a2a", EYE="#e8c83a", CLOTH="#e8a83a", TRIM="#c83a6a",
                 pattern="stripes", pupil=(1.4, 1.4)),
        variants={"panther": dict(FUR="#3ac8c0", HAIR="#e84a3a", MARK="#e84a3a", LIGHT="#a8f0f0"),
                  "yellow": dict(FUR="#c8e04a", HAIR="#a8c83a", MARK="#7a9a2a"),
                  "veiled": dict(FUR="#4ab05a", HAIR="#3a8a4a", MARK="#e8c83a", pattern="bands"),
                  "brown": dict(FUR="#8a6a4a", HAIR="#6a4a2a", MARK="#4a3420", LIGHT="#c8a880"),
                  # homage (2026-10-28): Espio (Sonic): purple scales, a black ninja wrap, yellow trim
                  "ninja_chameleon": dict(FUR="#8a4ac8", HAIR="#7a3ab8", MARK="#4a1e7a", LIGHT="#e0c8f0", EYE="#e8c83a", CLOTH="#1e1a22", TRIM="#e8c83a", pattern="solid", extra=dict(cloth2="#1e1a22"))}),
    "iguana_lizardfolk": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts", extras=["belt"], colors=dict(cloth2="#3a5a7a")),
        hair="sidetail", bangs="messy", ears=None, legs="scales", foot="claw", tail=LIZARD_TAIL, tail_rest=DROOP, tail_look="scale", spines=True,
        pal=dict(SKIN=skin("tan"), HAIR="#4a8a3a", FUR="#5aa84a", LIGHT="#c8e8a0", MARK="#2a5a2a", EYE="#d89a2a", CLOTH="#e8e0c8", TRIM="#3a5a7a",
                 pattern="bands"),
        variants={"blue": dict(FUR="#4a8ac8", HAIR="#3a6aa8", MARK="#2a4a7a", LIGHT="#b8d8f0"),
                  "orange": dict(FUR="#e8883a", HAIR="#c8682a", MARK="#7a3a1a", LIGHT="#f8c8a0"),
                  "marine": dict(FUR="#4a4a4e", HAIR="#2a2a2e", MARK="#7a7a5a", LIGHT="#8a8a8e"),
                  "rhino": dict(FUR="#8a8a82", HAIR="#5a5a52", MARK="#4a4a44", LIGHT="#c0c0b8")}),
    # twenty-fourth wave: Cipactli, the Aztec first monster: a small spiky teal crocodile girl, a cream belly, a heavy tail
    "cipactli_lizardfolk": dict(
        build="petite", outfit=dict(top=["bandeau"], bottom="shorts", colors=dict(cloth2="#1a6a70")),
        hair="pixie", bangs="jagged", ears=None, legs="scales", foot="claw", tail=[(3.5, 3, 4.5), (3, 2.5, 4.5), (2.25, 2, 4), (1.5, 1.5, 3.5)],
        tail_rest=[(-25, 0, 0), (8, 0, 0), (6, 0, 0), (4, 0, 0)], tail_look="scale", spines=True, teeth=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1a7a80", FUR="#3ab0b8", LIGHT="#f0e4b8", MARK="#14606a", EYE="#2a5ab0", CLOTH="#f0e4b8", TRIM="#14606a",
                 pattern="solid", pupil=(0.9, 3.4)),
        variants={"green": dict(HAIR="#2a6a2a", FUR="#4a9a4a", MARK="#1e4a1e", TRIM="#1e4a1e"),
                  "olive": dict(HAIR="#4a4a1e", FUR="#7a7a3a", MARK="#3a3a14", TRIM="#3a3a14"),
                  "red": dict(HAIR="#7a1e14", FUR="#b83a2a", MARK="#5a140e", TRIM="#5a140e"),
                  "black": dict(HAIR="#141418", FUR="#2a2a30", LIGHT="#a89a7a", MARK="#0a0a0c", TRIM="#0a0a0c", EYE="#e8c040")}),
    "croc_lizardfolk": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps", "belt"], colors=dict(bandage="#c8b898")),
        hair="shaggy", bangs="messy", ears=None, legs="scales", foot="claw", tail=[(3.5, 3, 5), (3, 2.5, 5), (2.25, 2, 4.5), (1.5, 1.5, 4)],
        tail_rest=[(-25, 0, 0), (8, 0, 0), (6, 0, 0), (4, 0, 0)], tail_look="scale", spines=True, teeth=True,
        pal=dict(SKIN=skin("olive"), HAIR="#2a3a1e", FUR="#5a6a3a", LIGHT="#d8d0a0", MARK="#2a3a1a", EYE="#d8c83a", CLOTH="#4a3a2a", TRIM="#a8987a",
                 pattern="bands", pupil=(0.9, 3.4)),
        variants={"saltwater": dict(FUR="#4a5030", HAIR="#1e2414", MARK="#1a1e10"),
                  "albino": dict(FUR="#f0e8d8", HAIR="#f8f4ec", MARK="#d8c8b0", LIGHT="#ffffff", EYE="#e83a4a"),
                  "dwarf": dict(FUR="#2a2a24", HAIR="#141410", MARK="#4a4a3a"),
                  "gharial": dict(FUR="#7a8a6a", HAIR="#4a5a3a", MARK="#3a4a2a"),
                  # homage (2026-10-30): Vector: green scales, a gold chain's colour in her trim
                  "detective_croc": dict(SKIN=skin("#7ab04a"), FUR="#5a9a3a", HAIR="#2a5a1e", LIGHT="#e8e0a0", MARK="#2a5a1e", EYE="#e8c040", CLOTH="#1e1e24", TRIM="#e8c040")}),
    "turtle_lizardfolk": dict(
        build="petite", outfit=dict(top=["sports"], bottom="shorts", extras=["pearls"], colors=dict(cloth2="#3a7a8a")),
        hair="bob", bangs="blunt", ears=None, legs="scales", foot="claw", tail=[(1.5, 1.5, 2), (1, 1, 1.5)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", shell=True,
        pal=dict(SKIN=skin("peach"), HAIR="#3a6a3a", FUR="#7aa85a", LIGHT="#e0e8b0", MARK="#4a6a3a", EYE="#3a2a1a", CLOTH="#e8d8a8", TRIM="#3a7a8a",
                 extra=dict(shell="#7a5a2a", shell_band="#c8a050")),
        variants={"hawksbill": dict(extra=dict(shell="#c88a3a", shell_band="#5a2a10"), HAIR="#6a4a2a"),
                  "leatherback": dict(FUR="#4a4a52", HAIR="#2a2a32", extra=dict(shell="#2a2a32", shell_band="#7a7a8a")),
                  "loggerhead": dict(FUR="#a87a4a", HAIR="#8a4a2a", extra=dict(shell="#8a3a1a", shell_band="#c8784a")),
                  "olive": dict(FUR="#8a8a4a", HAIR="#5a5a2a", extra=dict(shell="#6a6a2a", shell_band="#a8a85a")),
                  # homage (2026-10-28): a Koopa (Mario): yellow skin, a green shell with a white rim
                  "shell_trooper": dict(SKIN=skin("#f0d060"), FUR="#f0d060", HAIR="#e8c040", LIGHT="#fff0b0", MARK="#c8a030", EYE="#1e1e1e", CLOTH="#f8f4ec", TRIM="#d82a2a", extra=dict(shell="#3aa83a", shell_band="#f8f8f4", cloth2="#d82a2a"))}),
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
                                    extra=dict(shell="#d8c8a0", shell_band="#b8a880")),
                  # homage (2026-10-30): Bowser (Mario): yellow skin, red hair, a green spiked shell with a white rim, black cuffs
                  "spike_king": dict(SKIN=skin("#e8c860"), FUR="#7ab83a", HAIR="#e83a1e", LIGHT="#f8e8a0", MARK="#f0ecd8", EYE="#d82a1e", CLOTH="#2a2a2a",
                                     TRIM="#f0ecd8", extra=dict(shell="#3a8a2a", shell_band="#f0ecd8"))}),
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
    # fourteenth wave: the cocoskink, a slim blue skink with a crest of leaves and little bells in her hair
    "cocoskink_lizardfolk": dict(
        build="slim", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["armlets"], colors=dict(cloth2="#c8902a")),
        hair="ponytail", bangs="swept", ears=None, legs="scales", foot="claw",
        tail=[(1.5, 1.5, 4), (1.25, 1.25, 4), (1, 1, 4), (.75, .75, 3)], tail_rest=[(-20, 0, 0), (8, 0, 0), (8, 0, 0), (6, 0, 0)], tail_look="scale",
        skink=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a5ac8", FUR="#3a6ad8", LIGHT="#e8eef8", MARK="#1e2a6a", EYE="#e8a030", CLOTH="#5a9a3a", TRIM="#e8c050",
                 pattern="bars", extra=dict(leaf="#5a9a3a", bell="#e8c050")),
        variants={"green": dict(HAIR="#2a8a4a", FUR="#3aa85a", MARK="#145a2a", CLOTH="#c8902a", extra=dict(leaf="#7ac84a", bell="#e8c050")),
                  "gold": dict(HAIR="#c8902a", FUR="#e0b040", MARK="#7a5410", CLOTH="#2a6a3a", extra=dict(leaf="#5a9a3a", bell="#f8f0c0")),
                  "orange": dict(HAIR="#d8601e", FUR="#e8782a", MARK="#7a2a0a", CLOTH="#2a5a7a", extra=dict(leaf="#5a9a3a", bell="#e8c050")),
                  "black": dict(HAIR="#1e1c22", FUR="#2a2830", MARK="#0a080e", LIGHT="#a8a4b0", EYE="#e8d040", extra=dict(leaf="#3a7a2a", bell="#e8c050"))}),
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
    if d.get("lneck"):                                   # the long neck her head rides on
        out += [("lneck", 4, d["lneck"] + 1, 4)]
    if d.get("spines"):
        out += [("spine", .5, 1.25, 1)]
    if d.get("shell") == "flat":                         # the softshell: one thin, wide disc
        out += [("carapace", 8, 8.5, 1), ("plastron", 5, 5, .5)]
    elif d.get("shell") == "great":                      # the archelon's: two and a half times her width, from her neck past her hips
        out += [("carapace", 14, 17, 3), ("carapace_top", 10.5, 13.5, 3.75), ("plastron", 5, 5, .5)]
    elif d.get("shell"):
        out += [("carapace", 7, 8, 2.5), ("carapace_top", 5.5, 6, 1.25), ("plastron", 5, 5, .5)]
    if d.get("shell") == "dome":                         # the tortoises' high dome
        out += [("carapace_dome", 4, 4.5, 1.5)]
    if d.get("flower"):
        out += [("petal", .5, 1.5, 1.5), ("bloom_in", .5, 1, 1)]
    if d.get("conch"):                                   # a conch's spire rising from the top of her shell
        out += [("conch_a", 3.5, 3, 3.5), ("conch_b", 2.5, 2.5, 2.5), ("conch_c", 1.5, 2.25, 1.5), ("conch_d", .75, 1.75, .75)]
    if d.get("teeth"):
        out += [("snout", 3.5, 1, 1.5)]
    elif d.get("snout"):                                 # a snorkel nose (softshell, matamata)
        out += [("snout", 1.5, 1, 2)]
    if d.get("dish"):
        out += [("dish", 4, .5, 4), ("dish_water", 3, .25, 3)]
    if d.get("skink"):
        out += [("skink_leaf", 1.5, .25, 4), ("bell", .75, .75, .75)]
    if d.get("hammer"):                                  # the smith's hammer: haft and head
        out += [("haft", 1, 9, 1), ("hammer_head", 4, 2.5, 2.5)]
    if d.get("anvil"):                                   # the anvil on her back: base, waist, the face and its horn
        out += [("anvil_base", 6, 2, 4.5), ("anvil_waist", 3.5, 2, 3), ("anvil_face", 8, 2.5, 4.5), ("anvil_horn", 2.5, 1.75, 2.5)]
    if d.get("nurse_kit"):
        out += [("nurse_cap", 5, 2, 3.5), ("nurse_snout", 4, 2.5, 3.5), ("dress_top", 6, 5, 5), ("dress_hem", 7.5, 4.5, 6),
                ("syr_barrel", 3, 3, 10), ("syr_plunger", 1, 1, 3), ("syr_thumb", 3.5, 3.5, .5), ("syr_needle", .5, .5, 4.5)]
    if d.get("island"):                                  # the turtworld's little world: turf, two peaks and a waterfall
        out += [("isl_turf", 6.5, 6, 1), ("isl_peak", 2.5, 4.5, 2.5), ("isl_peak2", 1.75, 3, 1.75), ("isl_fall", 1.5, 6, .25)]
    if d.get("ferns"):                                   # fern fronds and a feather crest (the theropod fernstalker uses the painting)
        out += [("frond", 3, .25, 9), ("crest_feather", .5, 1.25, 4.5)]
    if d.get("keels"):                                   # the archelon's three keels running down her shell
        out += [("keel", .5, 11, .75)]
    if d.get("wflippers"):                               # flippers at her wrists
        out += [("wflipper", 1, 9, 4), ("rflipper", 1, 7, 4.5)]
    if d.get("crown"):                                   # the basilisk's little crown
        out += [("bcrown", 4.5, 1, 4.5), ("bcrown_pt", .75, 1.25, .75)]
    return out


def paint_hook(pt, pal, d):
    pt("lneck", "all", lambda f, x, y, w, h: pick(pal.LIGHT, 0.6, x, y) if f == "north" and 0 < x < w - 1 else
       pick(pal.MARK if y % 4 == 1 else pal.FUR, 0.5 + 0.1 * (f == "up"), x, y))
    keel = G.shades(pal.extra.get("keel", "#e4f4ff"))
    pt("rflipper", "all", lambda f, x, y, w, h: G.scale_fn(pal)(f, x, y, w, h))
    pt("keel", "all", lambda f, x, y, w, h: pick(keel, 0.55 + 0.2 * (f == "up") + 0.1 * (y % 3 == 0), x, y))
    pt("wflipper", "all", lambda f, x, y, w, h: G.scale_fn(pal)(f, x, y, w, h) if f in ("east", "west") and y < h - 1 - x * 0.4 else
       (CLEAR if f in ("east", "west") else pick(pal.MARK, 0.5, x, y)))
    fl, fin = G.shades(pal.extra.get("flower", "#f8d040")), G.shades(pal.extra.get("flower_in", "#f89a3a"))
    pt("petal", "all", lambda f, x, y, w, h: pick(fl, 0.6 + 0.2 * (f == "north"), x, y))
    pt("bloom_in", "all", lambda f, x, y, w, h: pick(fin, 0.6, x, y))
    conch, cband = G.shades(pal.extra.get("conch", "#f0e4d0")), G.shades(pal.extra.get("conch_band", "#3a7ad8"))
    for p in ("conch_a", "conch_b", "conch_c", "conch_d"):          # a spiral band winding up the spire
        pt(p, "all", lambda f, x, y, w, h: pick(cband if (x + 2 * y) % 6 in (0, 1) else conch, 0.5 + 0.2 * (f == "up") + (noise(x, y, 51) - 0.5) * 0.1, x, y))
    leafc, bell = G.shades(pal.extra.get("leaf", "#5a9a3a")), G.shades(pal.extra.get("bell", "#e8c050"))
    pt("skink_leaf", "all", lambda f, x, y, w, h: pick(leafc, 0.45 + 0.25 * (x == w // 2) + 0.1 * (y % 2), x, y))
    pt("bell", "all", lambda f, x, y, w, h: pick(bell, 0.55 + 0.3 * (f == "up"), x, y))
    steel = G.shades(pal.extra.get("steel", "#8a8e9a"))
    pt("haft", "all", lambda f, x, y, w, h: pick(G.shades("#5a3a22"), 0.45 + 0.1 * (y % 3 == 0), x, y))
    pt("hammer_head", "all", lambda f, x, y, w, h: pick(steel, 0.45 + 0.25 * (f in ("east", "west")) + 0.1 * (y == 0), x, y))
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
    e = pal.extra
    turf, rock, snow, water = (G.shades(e.get(k, c)) for k, c in (("turf", "#5aa04a"), ("rock", "#8a8a94"), ("snow", "#f4f8ff"), ("water", "#6ac8f0")))
    pt("isl_turf", "all", lambda f, x, y, w, h: pick(water, 0.6, x, y) if f == "south" and abs(x - w * 0.65) < 0.8
       else pick(turf, 0.5 + 0.2 * noise(x, y, 71), x, y) if f in ("south", "up") else pick(G.shades("#6a4a2e"), 0.45, x, y))
    for p in ("isl_peak", "isl_peak2"):
        pt(p, "all", lambda f, x, y, w, h: pick(snow, 0.8, x, y) if y < h * 0.3 or f == "up" else pick(rock, 0.45 + 0.2 * noise(x, y, 72), x, y))
    pt("isl_fall", "all", lambda f, x, y, w, h: G.with_alpha(pick(water, 0.6 + 0.3 * ((y + x) % 3 == 0), x, y), 200))
    frond, vein = G.shades(e.get("frond", "#8ab04a")), G.shades(e.get("frond_vein", "#4a6a1e"))

    def fern(f, x, y, w, h):                            # a frond: a midrib with leaflets off it, narrowing to the tip
        if f not in ("up", "down"):
            return CLEAR
        if x == w // 2:
            return pick(vein, 0.5, x, y)
        if y % 2 == 0 or (x in (0, w - 1) and y > h * 0.7):
            return CLEAR
        return pick(frond, 0.5 + 0.2 * (y % 4 == 1), x, y)
    pt("frond", "all", fern)
    fea, tip = G.shades(e.get("feather", "#1e2a5a")), G.shades(e.get("feather_tip", "#e8eef8"))
    pt("crest_feather", "all", lambda f, x, y, w, h: pick(tip if y >= h - 1 else fea, 0.5, x, y))
    gold = G.shades(e.get("crown", "#e8c040"))
    pt("bcrown", "all", lambda f, x, y, w, h: pick(G.shades("#c83a3a"), 0.6, x, y) if f == "north" and x == w // 2 else pick(gold, 0.55 + 0.2 * (y == 0), x, y))
    pt("bcrown_pt", "all", lambda f, x, y, w, h: pick(gold, 0.65, x, y))


def build_hook(r, g, d):
    z = g.z
    if d.get("lneck"):                                   # her head and everything on it lifted onto the top of the neck
        n = d["lneck"]
        lifted, grew = {"head"}, True
        while grew:
            grew = False
            for name, b in r.b.items():
                if b["parent"] in lifted and name not in lifted:
                    lifted.add(name)
                    grew = True
        for name in lifted:
            r.b[name]["pivot"][1] += n
            for c in r.b[name]["cubes"]:
                c["origin"][1] = round(c["origin"][1] + n, 3)
                if "pivot" in c:
                    c["pivot"][1] = round(c["pivot"][1] + n, 3)
        r.add("neck", "body", [0, g.hy, z], [cube("lneck", [-2, g.hy - 1, -1.5 + z])])
    if d.get("flower"):                                  # a flower behind her left ear: four petals round a heart
        hy, z = g.hy, g.z
        fx, fy, fz = 4.6, hy + 4.5, -0.5 + z
        r.add("flower", "head", [fx, fy, fz], [cube("petal", [fx, fy - 0.75 + dy, fz - 0.75 + dz]) for dy, dz in ((1.0, 0), (-1.0, 0), (0, 1.0), (0, -1.0))]
              + [cube("bloom_in", [fx + 0.1, fy - 0.5, fz - 0.5])])
    if d.get("conch"):                                   # leaning back off the top of the dome
        cd = G.S("chest")[2]
        bz = cd / 2 + 3.6 + z
        rot = dict(pivot=[0, 17.0, bz], rotation=[-35, 0, 0])
        r.add("conch", "body", [0, 17.0, bz], [cube("conch_a", [-1.75, 17.0, bz - 1.75], **rot), cube("conch_b", [-1.25, 19.8, bz - 1.25], **rot),
                                               cube("conch_c", [-0.75, 22.1, bz - 0.75], **rot), cube("conch_d", [-0.375, 24.2, bz - 0.375], **rot)])
    if d.get("skink"):                                   # a crest of leaves fanned on her head, little bells hanging by her ears
        hy = g.hy
        r.add("crest", "head", [0, g.hy + 8.4, z], [G.rot_cube("skink_leaf", [-0.75, hy + 8.6, -1.0 + z], [0, hy + 8.6, -0.5 + z], -30 + 12 * i, a, 0)
                                                    for i, a in enumerate((150, 175, 200, 125, 225))])
        r.put("head", cube("bell", [-4.9, hy + 2.6, -1.0 + z]), cube("bell", [4.15, hy + 2.6, -1.0 + z]), cube("bell", [-4.8, hy + 1.6, 0.2 + z]))
    if d.get("hammer"):                                  # held upright in her right hand, the head above her fist
        px, py, pz = r.b["hand_r"]["pivot"]
        cz = pz - 2.0
        r.add("hammer", "hand_r", [px, py - 1.5, pz], [cube("haft", [px - 0.5, py - 4.5, cz - 0.5]), cube("hammer_head", [px - 2.0, py + 4.0, cz - 1.25])])
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
    elif d.get("shell") == "great":
        b = G.S("chest")[2] / 2 + z
        r.add("shell", "body", [0, 16, 1.5 + z], [cube("carapace", [-7, 5.5, b - 0.2]), cube("carapace_top", [-5.25, 7.25, b + 2.6])])
    elif d.get("shell"):
        cd = G.S("chest")[2]
        cs = [cube("carapace", [-3.5, 12.5, cd / 2 - 0.2 + z]), cube("carapace_top", [-2.75, 13.5, cd / 2 + 2.2 + z])]
        if d["shell"] == "dome":
            cs.append(cube("carapace_dome", [-2, 14.25, cd / 2 + 3.4 + z]))
        r.add("shell", "body", [0, 16, 1.5 + z], cs)
    if d.get("keels") and "shell" in r.b:              # three ridges down the shell, the middle one along the top
        cd = G.S("chest")[2]
        b = cd / 2 + z
        r.put("shell", cube("keel", [-0.25, 8.5, b + 6.2]), cube("keel", [-3.4, 8.5, b + 6.0]), cube("keel", [2.9, 8.5, b + 6.0]))
    if d.get("wflippers"):                               # a flipper swept back from each wrist (forearm_l mirrors forearm_r)
        px, py, pz = r.b["forearm_r"]["pivot"]
        r.put("forearm_r", cube("wflipper", [px - 1.0, py - 8.5, pz - 2.0]))
        sx, sy, sz = r.b["shin_r"]["pivot"]              # and a broad flipper in place of each foot (shin_l mirrors shin_r)
        r.put("shin_r", cube("rflipper", [sx - 0.5, sy - 8.5, sz - 2.25]))
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
    if d.get("island"):                                  # on the back of her dome: turf, the peaks rising from it, the waterfall down her shell
        cd = G.S("chest")[2]
        b = cd / 2 + 4.9 + z
        r.add("island", "body", [0, 16, b], [cube("isl_turf", [-3.25, 13.0, b]), cube("isl_peak", [-2.6, 18.0, b - 0.6]),
                                             cube("isl_peak2", [0.5, 18.0, b - 0.3]), cube("isl_fall", [1.2, 10.5, b + 1.05])])
    if d.get("ferns"):                                   # three fronds fanned up and back from the small of her back, feathers swept back on her head
        cd = G.S("chest")[2]
        b = cd / 2 - 0.2 + z
        r.add("ferns", "body", [0, 13, b], [G.rot_cube("frond", [-1.5, 13.0, b], [0, 13.0, b], 25, a, 0) for a in (-30, 0, 30)])
        hy = g.hy
        r.add("hair_crest", "head", [0, hy + 8.4, z], [G.rot_cube("crest_feather", [x - 0.25, hy + 8.2, -1.0 + z], [x, hy + 8.4, -1.0 + z], -15, x * 8, 0)
                                                       for x in (-1.2, 0, 1.2)])
    if d.get("crown"):                                   # a little gold crown on top of her head, five points
        hy = g.hy
        r.add("hair_crown", "head", [0, hy + 8.6, z], [cube("bcrown", [-2.25, hy + 8.6, -2.25 + z])]
              + [cube("bcrown_pt", [px - 0.375, hy + 9.6, pz - 0.375 + z]) for px, pz in ((-1.9, -1.9), (1.9, -1.9), (-1.9, 1.9), (1.9, 1.9), (0, -1.9))])


def turtle_prone(an):
    """the archelon is a sea turtle, not a girl on two legs: flat on her belly, her head up, her flippers out to the sides"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    flat = {"root": {"rotation": hold(*PRONE), "position": hold(*PRONE_AT)}, "head": {"rotation": hold(-PRONE[0] + 10, 0, 0)},
            "hair_back": {"rotation": hold(30, 0, 0)}, "leg_r": {"rotation": hold(0, 0, 14)}, "leg_l": {"rotation": hold(0, 0, -14)}}

    def stroke(period, amp):                             # both front flippers together, as a sea turtle swims and hauls herself
        return sym("arm", Tk((0, (0, amp, -56)), (period / 2, (0, -amp, -44)), (period, (0, amp, -56))))
    an.add("idle", 4.0, True, merge(flat, sym("arm", Tk((0, (0, 0, -60)), (2, (0, 0, -56)), (4, (0, 0, -60))))))
    bob = lambda period: {"body": {"position": Tk((0, (0, 0, 0)), (period / 2, (0, 1.2, -1.2)), (period, (0, 0, 0)))}}    # each stroke heaves her forward
    kick = lambda period: {"leg_r": {"rotation": Tk((0, (-18, 0, 14)), (period / 2, (18, 0, 14)), (period, (-18, 0, 14)))},
                           "leg_l": {"rotation": Tk((0, (18, 0, -14)), (period / 2, (-18, 0, -14)), (period, (18, 0, -14)))}}
    an.add("walk", 1.2, True, merge(flat, stroke(1.2, 42), bob(1.2), kick(1.2)))
    an.add("run", 0.8, True, merge(flat, stroke(0.8, 50), bob(0.8), kick(0.8)))
    an.add("swim", 1.8, True, merge(flat, stroke(1.8, 55), {"root": {"rotation": hold(*PRONE), "position": hold(0, 10, 12)}}, F_wave_legs()))
    for key in ("sit", "lie", "pounce", "groom", "hiss", "stalk"):
        F._inject(an, key, {"root": flat["root"], "head": flat["head"]})


def F_wave_legs():
    return {"leg_r": {"rotation": H.track((0, (-14, 0, 14)), (0.9, (14, 0, 14)), (1.8, (-14, 0, 14)))},
            "leg_l": {"rotation": H.track((0, (14, 0, -14)), (0.9, (-14, 0, -14)), (1.8, (14, 0, -14)))}}


def anim_extra(an, sp, d):
    """basking in the sun ("bask"), the crocodile's death roll ("roll"), the turtle hiding in her shell ("retract")"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    if d.get("shell") == "great":
        turtle_prone(an)
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
