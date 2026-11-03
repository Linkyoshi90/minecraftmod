#!/usr/bin/env python3
"""Canids (the canid family, with the bears, see canid_field_guide.html): wolf, polar bear, panda, Anubis.

    python tools/gen_canid.py [species ...] [--previews]

girl_family bipeds with fur legs and paws. The wolf has pointed ears and a bushy tail, the bears round ears and a stub
of a tail, the panda dark patches round her eyes (facemark "panda"), Anubis tall jackal ears and a long thin tail.
Extra animations: "howl" (the wolf), "fish" (the polar bear, crouched at the water), "roll" and "sneeze" (the panda),
"judge" (Anubis holds up the scales).
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

SPECIES = {
    # thirty-ninth wave: the wolverine: low, broad and dark, a pale band from shoulder to hip, and she does not back down
    "wolverine_canid": dict(
        build="strong", outfit=dict(top=["sports", "vest"], bottom="loincloth", extras=["arm_wraps"], colors=dict(leather="#3a2a1e", bandage="#c8b898")),
        hair="shaggy", bangs="messy", ears="round", legs="fur", foot="paw", tail=[(2, 2, 2.5)], tail_rest=[(-20, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("tan"), HAIR="#2a1e16", FUR="#3a2a1e", LIGHT="#b8905a", MARK="#1e140c", EYE="#3a2412", CLOTH="#5a4a3a", TRIM="#b8905a",
                 pattern="solid"),
        variants={"pale": dict(FUR="#6a5038", HAIR="#4a3626", LIGHT="#d8b880"), "black": dict(FUR="#1e1a18", HAIR="#141210"), "red": dict(FUR="#5a3020", HAIR="#3a1e12"),
                  "snow": dict(FUR="#5a5048", HAIR="#d8d0c8", LIGHT="#f0ece4")}),
    # thirty-fifth wave: the spotted hyena: broad shoulders, a sloping back, a laugh that carries for miles
    "hyena_canid": dict(
        build="strong", outfit=dict(top=["sports"], bottom="shorts", extras=["belt"], colors=dict(leather="#4a3a2a")),
        hair="shaggy", bangs="messy", ears="round", legs="fur", foot="paw", tail=[(1.5, 1.5, 3), (1.75, 1.75, 3)], tail_rest=[(-40, 0, 0), (10, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("tan"), HAIR="#3a2e20", FUR="#b89a6a", LIGHT="#e0cca0", MARK="#3a2a1a", EYE="#c8902a", CLOTH="#6a3a2a", TRIM="#3a2a1a",
                 pattern="spots", pupil=(1.6, 2.6)),
        variants={"striped": dict(FUR="#a8a090", MARK="#2a2620", pattern="bars"),
                  "brown": dict(FUR="#7a5a3e", HAIR="#2a1e12", MARK="#2a1a10"),
                  "pale": dict(FUR="#d8ccb0", HAIR="#7a6a4a", MARK="#5a4a30"),
                  "dark": dict(FUR="#4a3c30", HAIR="#1e1812", MARK="#14100c")}),
    # twenty-third wave: the hellhound, from the user's reference (the Monster Girl Encyclopedia's): charcoal skin, heavy black fur on her
    # arms and legs, spiky dark-red fur at the shoulders, wrists and ankles, flames streaming from her eyes, great claws, a skull at her
    # collar on a chain, ember cracks that glow, a grin full of teeth; a fur top and shorts in place of the art's bare skin
    "hellhound_canid": dict(
        build="athletic", outfit=dict(top=["bandeau"], bottom="shorts", colors=dict(leather="#141418", cloth2="#141418")),
        hair="mane", bangs="jagged", ears="fox", legs="fur", foot="paw", tail=[(3, 3, 4), (3.25, 3.25, 4), (2.25, 2.25, 3)],
        tail_rest=[(-25, 0, 0), (10, 0, 0), (12, 0, 0)], tail_look="fur", grin=True, flames=True, glow=["efl_a", "efl_b", "efl_c", "ember"], glow_boost=1.3,
        pal=dict(SKIN=skin("#4a4a54"), HAIR="#1a1a1e", FUR="#242428", LIGHT="#3a3a42", MARK="#0e0e12", EYE="#f03a1e", CLOTH="#1a1a1e", TRIM="#7a1e1a", INNER="#4a1e1e",
                 pattern="solid", pupil=(1.6, 2.6), extra=dict(flame="#f8501e", flame_core="#ffd860", tuft="#5a1a18", tuft_tip="#a83a24", claw="#5a2030")),
        variants={"black": dict(SKIN=skin("#2e2e36"), HAIR="#0e0e12", FUR="#141418", LIGHT="#26262c", EYE="#f8a01e", TRIM="#7a4a14",
                                extra=dict(flame="#f8a01e", flame_core="#fff0a0", tuft="#3a2a14", tuft_tip="#a8701e", claw="#3a2a1e")),
                  "blue": dict(EYE="#4ac8f8", TRIM="#1e4a7a", INNER="#1e2a4a", extra=dict(flame="#4ac8f8", flame_core="#e8fcff", tuft="#1a3a5a", tuft_tip="#2a7aa8", claw="#203a5a")),
                  "green": dict(EYE="#6af84a", TRIM="#2a6a2a", INNER="#1e3a1e", extra=dict(flame="#6af84a", flame_core="#e8ffd0", tuft="#1a4a24", tuft_tip="#3a8a3a", claw="#204a2a")),
                  "purple": dict(EYE="#c84af8", TRIM="#5a1e7a", INNER="#2e1a4a", extra=dict(flame="#c84af8", flame_core="#f8e0ff", tuft="#3a1a5a", tuft_tip="#7a3aa8", claw="#3a205a"))}),
    # twenty-seventh wave: the bloodhound, a horror of a hound: matted brown fur sewn up with black stitches, long black hair, a wide human grin with blood down her chin
    "bloodhound_canid": dict(
        build="strong", outfit=dict(top=["bandeau"], bottom="shorts", colors=dict(leather="#2a1a14", cloth2="#2a1a14")),
        hair="flowing", bangs="messy", ears="round", legs="fur", foot="paw", tail=[(2, 2, 4), (1.75, 1.75, 4), (1.25, 1.25, 3)],
        tail_rest=[(-25, 0, 0), (10, 0, 0), (12, 0, 0)], tail_look="fur", grin=True, sewn=True,
        pal=dict(SKIN=skin("#b89a84"), HAIR="#141012", FUR="#4a3024", LIGHT="#8a6a50", MARK="#1e120c", EYE="#e8e0c8", CLOTH="#2a1a14", TRIM="#7a1414", INNER="#7a3a3a",
                 pattern="solid", extra=dict(gore="#8a1414")),
        variants={"grey": dict(FUR="#5a5a5e", HAIR="#1a1a1e", LIGHT="#9a9a9e", MARK="#242428", SKIN=skin("#b0aca8")),
                  "black": dict(FUR="#26221e", HAIR="#0e0c0c", LIGHT="#5a5048", MARK="#0a0808", SKIN=skin("#8a8078")),
                  "red": dict(FUR="#6a241a", HAIR="#2a0a08", LIGHT="#a85a44", MARK="#2a0a08", SKIN=skin("#c8a090")),
                  "pale": dict(FUR="#a89878", HAIR="#3a342a", LIGHT="#d8ccb0", MARK="#5a4e3a", SKIN=skin("#d8d0c0"))}),
    # twenty-fourth wave: Cerberus, dark-furred, with two more little heads on her shoulders, as the Xiangliu has
    "cerberus_canid": dict(
        build="athletic", outfit=dict(top=["bandeau"], bottom="shorts", extras=["belt"], colors=dict(leather="#7a1e1a", cloth2="#1e1a1e")),
        hair="shaggy", bangs="jagged", ears="fox", legs="fur", foot="paw", tail=[(2, 2, 4), (2.25, 2.25, 4), (1.75, 1.75, 3)],
        tail_rest=[(-25, 0, 0), (10, 0, 0), (12, 0, 0)], tail_look="fur", grin=True, cheads=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e1a1e", FUR="#2a2628", LIGHT="#6a6266", MARK="#0e0c0e", EYE="#e83a2a", CLOTH="#1e1a1e", TRIM="#c82a1e", INNER="#7a3a3a", pattern="solid"),
        variants={"brown": dict(HAIR="#3a2614", FUR="#5a3e24", LIGHT="#a8845a", MARK="#2a1a0e", EYE="#e8a030", CLOTH="#3a2614"),
                  "grey": dict(HAIR="#4a4a50", FUR="#6a6a70", LIGHT="#a8a8b0", MARK="#2a2a30", EYE="#5ab0e8", CLOTH="#2a2a30"),
                  "white": dict(HAIR="#e8e4e0", FUR="#f0ece8", LIGHT="#ffffff", MARK="#a8a4a0", EYE="#c83a5a", CLOTH="#a8a4a0"),
                  "red": dict(HAIR="#4a0e0a", FUR="#7a1e14", LIGHT="#c85a3a", MARK="#2a0806", EYE="#f8d040", CLOTH="#2a0806")}),
    # twenty-second wave: the gigantopithecus, the giant ape: huge and shaggy, deep blue fur, a red flower in her hair, a fur top and a
    # stone belt
    "gigantopithecus_canid": dict(
        build="strong", outfit=dict(top=["bandeau"], bottom="loincloth", extras=["belt", "arm_wraps"],
                                    colors=dict(cloth2="#2a3a8a", leather="#6a6e7a", bandage="#3a4a9a")),
        hair="mane", bangs="messy", ears=None, legs="fur", foot="paw", tail=None, hflower=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e2e8a", FUR="#2a3c9a", LIGHT="#8a9ad8", MARK="#101a4a", EYE="#4a6ad8", CLOTH="#2a3c9a", TRIM="#c8ccd8",
                 pattern="solid", extra=dict(flower="#e0281e", flower_in="#f8d040")),
        variants={"brown": dict(HAIR="#4a3020", FUR="#5a3a26", LIGHT="#a88060", MARK="#2a1a10", EYE="#c8902a", CLOTH="#5a3a26"),
                  "black": dict(HAIR="#1a181c", FUR="#242226", LIGHT="#6a6670", MARK="#0a080a", EYE="#c8902a", CLOTH="#242226"),
                  "grey": dict(HAIR="#8a8e96", FUR="#9a9ea6", LIGHT="#d8dade", MARK="#4a4e56", EYE="#3a5a8a", CLOTH="#4a4e56",
                               extra=dict(flower="#f0f0f0", flower_in="#f8d040")),
                  "red": dict(HAIR="#7a321e", FUR="#8a3c24", LIGHT="#d0906a", MARK="#3a140a", EYE="#3a2a1a", CLOTH="#4a2a1a",
                              extra=dict(flower="#f8d040", flower_in="#e0601e"))}),
    # twenty-first wave: the rheintalensis, a broad shaggy cave bear: wild blue hair, pelts, a necklace of teeth, a big club
    "rheintalensis_canid": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps", "pearls"], colors=dict(bandage="#3a5ab0", cloth2="#2a3a7a")),
        hair="mane", bangs="messy", ears="round", legs="fur", foot="paw", tail=[(1.5, 1.25, 1.25)], tail_rest=[(-20, 0, 0)], tail_look="fur",
        bigclub=True,
        pal=dict(SKIN=skin("tan"), HAIR="#2a4ab0", FUR="#2a4a9a", LIGHT="#8aa8e0", MARK="#14204a", EYE="#3a8ae8", CLOTH="#3a5ab0", TRIM="#f0e8d4",
                 pattern="solid", extra=dict(club="#6a7284", club_dark="#3a4050")),
        variants={"brown": dict(HAIR="#4a2e1a", FUR="#5a3a22", LIGHT="#a8805a", MARK="#2a1a0e", EYE="#c8902a", CLOTH="#6a4a2a",
                                extra=dict(club="#7a5a3a", club_dark="#3a2a1a")),
                  "grey": dict(HAIR="#5a5a62", FUR="#6a6a72", LIGHT="#b8b8c0", MARK="#2a2a30", CLOTH="#4a4a54", extra=dict(club="#8a8478", club_dark="#4a463e")),
                  "black": dict(HAIR="#1a181c", FUR="#222024", LIGHT="#5a565e", MARK="#0a080a", EYE="#e8c040", CLOTH="#5a1e24",
                                extra=dict(club="#5a4a3a", club_dark="#2a2018")),
                  "red": dict(HAIR="#6a2a1a", FUR="#7a3a22", LIGHT="#c8805a", MARK="#3a140a", EYE="#e8c040", CLOTH="#2a3a2a",
                              extra=dict(club="#6a5a4a", club_dark="#3a2e24"))}),
    # nineteenth wave: the obsidian macuahuitl hound, a temple dog in navy and gold, an obsidian-edged war club across her back
    "obsidian_hound_canid": dict(
        build="athletic", outfit=dict(top=["bandeau", "harness"], bottom="loincloth", extras=["pauldron", "bracers", "usekh"],
                                      colors=dict(cloth2="#2a3a8a", leather="#c8a040")),
        hair="ponytail", bangs="blunt", ears="fox", legs="fur", foot="paw", club=True,
        tail=[(1.5, 1.5, 4), (1.25, 1.25, 4)], tail_rest=[(-35, 0, 0), (15, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("brown"), HAIR="#1a1e3a", FUR="#1e2448", LIGHT="#4a5a9a", MARK="#0e1024", EYE="#e8c040", CLOTH="#2a3a8a", TRIM="#e8c050",
                 pattern="solid", extra=dict(club="#6a4a2a", glass="#1a1420")),
        variants={"black": dict(HAIR="#141216", FUR="#1a181c", LIGHT="#4a464e", MARK="#060508", CLOTH="#7a1e24", EYE="#e83a3a"),
                  "jade": dict(HAIR="#1e3a2e", FUR="#2a5a44", LIGHT="#6aa888", MARK="#0e2a1e", CLOTH="#e8c050", TRIM="#2a8a6a"),
                  "red": dict(HAIR="#4a1414", FUR="#7a2420", LIGHT="#c8644a", MARK="#2a0808", CLOTH="#1e1e24", EYE="#f8d040"),
                  "gold": dict(HAIR="#7a5418", FUR="#b8862a", LIGHT="#f0d080", MARK="#4a3010", CLOTH="#1e2a6a", EYE="#3ad8c8")}),
    # seventeenth wave: the amarok, the giant lone wolf of Inuit legend: tall, shaggy, slate grey with a pale ruff
    "amarok_canid": dict(
        build="strong", outfit=dict(top=["bandeau", "mantle"], bottom="loincloth", extras=["arm_wraps", "belt"], colors=dict(cloth2="#3a3e48", leather="#3a3a40")),
        hair="mane", bangs="messy", ears="fox", legs="fur", foot="paw", tail=[(2.25, 2.25, 4), (2.5, 2.5, 4), (2, 2, 3)],
        tail_rest=[(-25, 0, 0), (10, 0, 0), (12, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#5a6270", FUR="#6a7280", LIGHT="#d8dce2", MARK="#3a3e48", EYE="#8ab8e8", CLOTH="#3a3e48", TRIM="#c8ccd4",
                 pattern="solid", pupil=(1.6, 2.6)),
        variants={"white": dict(FUR="#eef0f2", HAIR="#ffffff", LIGHT="#ffffff", MARK="#a8acb4", EYE="#6ab0e8"),
                  "black": dict(FUR="#2a2a30", HAIR="#1a1a20", LIGHT="#6a6a72", MARK="#0e0e12", EYE="#e8c040"),
                  "storm": dict(FUR="#4a5a7a", HAIR="#3a4868", LIGHT="#a8b8d8", MARK="#1e2840", EYE="#e8f0ff"),
                  "tundra": dict(FUR="#a89a84", HAIR="#8a7c66", LIGHT="#ece4d4", MARK="#5a4e3e", EYE="#e8b030")}),
    # eighteenth wave: the tarnhelm, a war wolf in blue enamelled plate and a silver helm with an aurora-bright gem
    "tarnhelm_canid": dict(
        build="athletic", outfit=dict(top=["bandeau", "harness"], bottom="wrap_skirt", extras=["pauldron", "bracers", "belt"],
                                      colors=dict(cloth2="#2a3a8a", leather="#a8b4c8")),
        hair="shaggy", bangs="messy", ears="fox", legs="fur", foot="paw", tarnhelm=True, glow=["helm_gem"],
        tail=[(2, 2, 4), (2.5, 2.5, 4), (2, 2, 3)], tail_rest=[(-25, 0, 0), (10, 0, 0), (12, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#c8d0e0", FUR="#4a5a9a", LIGHT="#dce4f4", MARK="#1e2a5a", EYE="#5ae8e0", CLOTH="#2a3a8a", TRIM="#c8d0dc",
                 pattern="solid", pupil=(1.6, 2.6), extra=dict(helm="#5a6aa8", gem="#5ae8c8")),
        variants={"silver": dict(FUR="#9aa0ac", HAIR="#e8ecf4", LIGHT="#f4f6fa", MARK="#5a606c", CLOTH="#4a5068", EYE="#8ab0f8",
                                 extra=dict(helm="#6a7080", gem="#8ab0f8")),
                  "black": dict(FUR="#26262c", HAIR="#3a3a42", LIGHT="#8a8a94", MARK="#0e0e12", CLOTH="#1e1e24", TRIM="#a8323a", EYE="#e83a3a",
                                extra=dict(helm="#4a4a52", gem="#e83a3a")),
                  "gold": dict(FUR="#a87a3a", HAIR="#f0dca8", LIGHT="#f8ecd0", MARK="#5a3a14", CLOTH="#7a2a2a", TRIM="#e8c050", EYE="#f8d040",
                               extra=dict(helm="#e8c050", gem="#e83a3a")),
                  "storm": dict(FUR="#5a6070", HAIR="#a8b0bc", LIGHT="#d0d4dc", MARK="#2a2e38", CLOTH="#3a4a5a", EYE="#c8e8ff",
                                extra=dict(helm="#8a929e", gem="#c8a8ff"))}),
    "wolf_canid": dict(
        mummy_kit=True,
        build="athletic", outfit=dict(top=["bandeau", "vest"], bottom="shorts", extras=["arm_wraps", "belt"], colors=dict(leather="#5a4a3a")),
        hair="shaggy", bangs="messy", ears="fox", legs="fur", foot="paw", tail=[(2, 2, 4), (2.25, 2.25, 4), (1.75, 1.75, 3)],
        tail_rest=[(-25, 0, 0), (10, 0, 0), (12, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#d8d4cc", FUR="#c8c4bc", LIGHT="#f0eee8", MARK="#6a665e", EYE="#e8b030", CLOTH="#5a4a3a", TRIM="#8a7a6a",
                 pattern="solid", pupil=(1.6, 2.6)),
        variants={"woods": dict(FUR="#7a6248", HAIR="#5a4632", LIGHT="#c8b8a0", MARK="#3a2e22"),
                  "snowy": dict(FUR="#eef0f2", HAIR="#ffffff", LIGHT="#ffffff", MARK="#a8acb4", EYE="#6ab0e8"),
                  "ashen": dict(FUR="#8a8c90", HAIR="#6a6c72", LIGHT="#c0c2c6", MARK="#3a3c40"),
                  "rusty": dict(FUR="#b0663a", HAIR="#8a4a26", LIGHT="#e0b890", MARK="#4a2a18"),
                  # homage to Gregory Horror Show (2026-10-25): Mummy Dog, bandaged head, red shirt, blue overalls, a cleaver in his head
                  "mummy": dict(SKIN=skin("#b8703e"), FUR="#b06e3e", HAIR="#ece6d4", LIGHT="#d8a070", MARK="#5a2e10", EYE="#1e8a4a", CLOTH="#c81e24",
                                extra=dict(wrapped=True, blood=True, blade="cleaver", leather="#1a78b0", cloth2="#1a78b0", shoes="#1a5a8a", legs="#1a78b0")),
                  # homage (2026-10-28): Amaterasu (Okami): white with red markings
                  "sun_goddess": dict(FUR="#f8f8f6", HAIR="#ffffff", LIGHT="#ffffff", MARK="#d82a2a", EYE="#c82a2a", CLOTH="#f4f4f6", TRIM="#d82a2a",
                                      pattern="stripes"),
                  # homage (2026-10-31): Lucario (Pokemon): blue and black fur, a cream chest, red eyes
                  "aura_guardian": dict(FUR="#2a6ac8", HAIR="#1e2a4a", LIGHT="#e8d890", MARK="#14142a", EYE="#c82a2a", CLOTH="#14142a", TRIM="#e8e8ec")}),
    # tenth wave: the meloncollie, a collie in watermelon colours (rind-green top, melon-red shorts), half a melon on her back
    "meloncollie_canid": dict(
        build="petite", outfit=dict(top=["halter"], bottom="shorts", extras=["bell_collar"], colors=dict(cloth2="#e84a5a")),
        hair="ponytail", bangs="swept", ears="fox", legs="fur", foot="paw", tail=[(2.25, 2.25, 4), (2.5, 2.5, 4), (2, 2, 3)],
        tail_rest=[(-30, 0, 0), (12, 0, 0), (14, 0, 0)], tail_look="fur", melon=True,
        pal=dict(SKIN=skin("fair"), HAIR="#7a4a2a", FUR="#f4f0e8", LIGHT="#ffffff", MARK="#8a5432", EYE="#3a2a1a", CLOTH="#4a9a3a", TRIM="#f8d040",
                 pattern="patches", pupil=(1.6, 2.6), extra=dict(rind="#4a9a3a", flesh="#e84a5a")),
        variants={"golden": dict(CLOTH="#c8b83a", extra=dict(rind="#a8b83a", flesh="#f8d040", cloth2="#f8d040")),
                  "black": dict(CLOTH="#1e3a1e", extra=dict(rind="#1e3a1e", flesh="#e83a4a", cloth2="#e83a4a")),
                  "tricolour": dict(HAIR="#1e1a18", MARK="#1e1a18", LIGHT="#f0e0c8", extra=dict(rind="#4a9a3a", flesh="#e84a5a", cloth2="#e84a5a")),
                  "seedless": dict(CLOTH="#a8d890", extra=dict(rind="#a8d890", flesh="#f88a9a", cloth2="#f88a9a", seedless=True))}),
    "polar_bear_canid": dict(
        build="strong", outfit=dict(top=["halter", "mantle"], bottom="loincloth", extras=["scarf"], colors=dict(scarf="#3a6a9a")),
        hair="bob", bangs="blunt", ears="round", legs="fur", foot="paw", tail=[(1.75, 1.5, 1.5)], tail_rest=[(-20, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("pale"), HAIR="#f4f2ec", FUR="#f0ece2", LIGHT="#ffffff", MARK="#c8c2b4", EYE="#2a2a32", CLOTH="#3a5a7a", TRIM="#e8e4dc",
                 pattern="solid"),
        variants={"cream": dict(FUR="#efe2c4", HAIR="#f2e6cc", MARK="#c8b894"),
                  "grey": dict(FUR="#c8c8c8", HAIR="#dcdcdc", MARK="#8a8a8a", CLOTH="#5a3a3a"),
                  "pizzly": dict(FUR="#c8a882", HAIR="#e8dcc8", MARK="#8a6a4a", CLOTH="#2a4a3a"),
                  "ice": dict(FUR="#e4f0f8", HAIR="#d0e8f8", MARK="#a8c8e0", EYE="#5ab0e8", CLOTH="#2a4a6a")}),
    "panda_canid": dict(
        build="curvy", outfit=dict(top=["bandeau", "vest"], bottom="sarong", extras=["bell_collar"], colors=dict(cloth2="#c83a3a")),
        hair="bob", bangs="blunt", ears="round", legs="fur", foot="paw", tail=[(1.5, 1.5, 1.25)], tail_rest=[(-20, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#f4f2ee", FUR="#1e1c20", LIGHT="#3a383c", MARK="#1e1c20", EYE="#3a2a1e", CLOTH="#3a8a4a", TRIM="#e8c050",
                 pattern="solid", facemark="panda"),
        variants={"brown": dict(FUR="#5a3e2a", LIGHT="#7a5a42", MARK="#5a3e2a", HAIR="#ece0d0"),
                  "cream": dict(FUR="#a89070", LIGHT="#c8b090", MARK="#a89070", HAIR="#f8f2e6"),
                  "grey": dict(FUR="#4a4a50", LIGHT="#6a6a70", MARK="#4a4a50", HAIR="#e8e8ea", CLOTH="#3a5a8a"),
                  "red": dict(FUR="#8a3a1e", LIGHT="#b05a30", MARK="#8a3a1e", HAIR="#f4e8d8", CLOTH="#2a6a6a")}),
    "anubis_canid": dict(
        mummy_kit=True,
        build="athletic", outfit=dict(top=["bandeau"], bottom="wrap_skirt", extras=["usekh", "armlets", "bracers"], colors=dict(cloth2="#e8c050")),
        hair="hime", bangs="blunt", ears="big", legs="fur", foot="paw", scales=True, tail=[(1.25, 1.25, 4), (1.25, 1.25, 4), (1, 1, 3)],
        tail_rest=[(-50, 0, 0), (8, 0, 0), (8, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("brown"), HAIR="#141214", FUR="#1a181c", LIGHT="#3a363c", MARK="#e8c050", EYE="#e8c030", CLOTH="#f0ece0", TRIM="#e8c050",
                 pattern="solid", pupil=(1.4, 2.8)),
        variants={"gold": dict(FUR="#c8984a", HAIR="#3a2614", LIGHT="#e8c88a", MARK="#7a5a2a", CLOTH="#2a4a8a"),
                  "silver": dict(FUR="#8a8a90", HAIR="#d8d8dc", LIGHT="#c0c0c4", MARK="#e8c050"),
                  "sand": dict(FUR="#c8b088", HAIR="#5a4630", LIGHT="#e8d8b8", MARK="#8a6a40"),
                  "night": dict(FUR="#1a1e30", HAIR="#0e1020", LIGHT="#3a4060", MARK="#5ab0e8", EYE="#5ab0e8", TRIM="#5ab0e8"),
                  # homage to Gregory Horror Show (2026-10-25): Mummy Papa, bandaged head, grey suit, a machete in his head
                  "mummy": dict(SKIN=skin("#9a5e30"), FUR="#9a5e30", HAIR="#ece6d4", LIGHT="#c8905a", MARK="#5a2e10", EYE="#2a6ac8", CLOTH="#8c8c88",
                                TRIM="#6a6a72", extra=dict(wrapped=True, blood=True, blade="machete", cloth2="#4e4448", suit=True, shoes="#141414", legs="#4e4448"))}),
    "kobold_canid": dict(
        build="petite", outfit=dict(top=["sports", "harness"], bottom="shorts", extras=["satchel", "belt"], colors=dict(leather="#5a3a22")),
        hair="twintails", bangs="messy", ears="lop", legs="fur", foot="paw", tail=[(1.5, 1.5, 3), (1.75, 1.75, 3)], tail_rest=[(-30, 0, 0), (15, 0, 0)],
        tail_look="fur", helmet=True,
        pal=dict(SKIN=skin("peach"), HAIR="#8a5a32", FUR="#a8784a", LIGHT="#e8c89a", MARK="#5a3a1e", EYE="#3a7ac8", CLOTH="#4a5a6a", TRIM="#c8a050",
                 pattern="solid"),
        variants={"cobalt": dict(FUR="#3a5aa8", HAIR="#2a3a7a", LIGHT="#a8c0f0", MARK="#1a2a5a", EYE="#e8b030"),
                  "black": dict(FUR="#2a2624", HAIR="#141210", LIGHT="#6a6260", MARK="#0a0808"),
                  "cream": dict(FUR="#e8d8b8", HAIR="#d8c098", LIGHT="#fff8e8", MARK="#a89070"),
                  "red": dict(FUR="#a84a2a", HAIR="#7a2e18", LIGHT="#e8a07a", MARK="#4a1a0a"),
                  # homage (2026-10-28): Isabelle (Animal Crossing): golden fur, a green vest over a white blouse, a red bell
                  "town_clerk": dict(FUR="#f0c860", HAIR="#f0c860", LIGHT="#fff0c0", MARK="#c89a3a", EYE="#1e1a18", CLOTH="#3a8a4a", TRIM="#d82a2a")}),
    # fourteenth wave: the cu sith, the great green fairy hound, her tail curled up over her back
    "cu_sith_canid": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts", extras=["belt", "scarf"], colors=dict(scarf="#e8dcb0", cloth2="#2a3a2a")),
        hair="shaggy", bangs="messy", ears="fox", legs="fur", foot="paw", tail=[(1.75, 1.75, 4), (1.5, 1.5, 4), (1.25, 1.25, 3)],
        tail_rest=[(-70, 0, 0), (45, 0, 0), (40, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#1e4a2a", FUR="#2a5a34", LIGHT="#7aa880", MARK="#0e2a16", EYE="#e8e870", CLOTH="#e8dcb0", TRIM="#1e4a2a",
                 pattern="solid"),
        variants={"moss": dict(HAIR="#4a6a2a", FUR="#5a7a34", LIGHT="#a8c080", MARK="#2a3a14"),
                  "dark": dict(HAIR="#0e1a12", FUR="#16241a", LIGHT="#3a4a3e", MARK="#060c08", EYE="#a8f0a0"),
                  "pale": dict(HAIR="#8ab894", FUR="#9ac8a4", LIGHT="#e0f0e2", MARK="#4a6a50", EYE="#2a4a2a"),
                  "teal": dict(HAIR="#1e5a5a", FUR="#2a6a6a", LIGHT="#80b8b8", MARK="#0e2e2e")}),
    # fourteenth wave: the yeti, a huge shaggy snow woman with a blue-grey face
    "yeti_canid": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps"], colors=dict(bandage="#c8d8e8", cloth2="#7a8a9a")),
        hair="mane", bangs="messy", ears="round", legs="fur", foot="paw", tail=[(1.5, 1.25, 1.25)], tail_rest=[(-20, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("#a8b8cc"), HAIR="#f4f6fa", FUR="#eef2f8", LIGHT="#ffffff", MARK="#a8b4c4", EYE="#3a6ad8", CLOTH="#5a7aa8", TRIM="#c8d8e8",
                 pattern="solid"),
        variants={"grey": dict(HAIR="#a8acb4", FUR="#b8bcc4", LIGHT="#e0e2e8", MARK="#6a6e76"),
                  "blue": dict(HAIR="#c8dcf8", FUR="#b8d0f4", LIGHT="#eef6ff", MARK="#6a8ac8"),
                  "golden": dict(HAIR="#e8d4a8", FUR="#e0c890", LIGHT="#fff4dc", MARK="#a88a50", SKIN=skin("#c8b0a0")),
                  "brown": dict(HAIR="#8a6a4e", FUR="#9a7a5a", LIGHT="#d8c0a4", MARK="#5a4028", SKIN=skin("#8a7064"))}),
    "black_bear_canid": dict(
        build="curvy", outfit=dict(top=["sports"], bottom="shorts", extras=["belt", "scarf"], colors=dict(scarf="#c85a2a", cloth2="#4a3a2a")),
        hair="shaggy", bangs="messy", ears="round", legs="fur", foot="paw", tail=[(1.5, 1.25, 1.25)], tail_rest=[(-20, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("tan"), HAIR="#141212", FUR="#1e1a1a", LIGHT="#a8805a", MARK="#0a0808", EYE="#5a3a1a", CLOTH="#c85a2a", TRIM="#e8c060",
                 pattern="solid"),
        variants={"cinnamon": dict(FUR="#8a4a24", HAIR="#6a3418", LIGHT="#d8a070", MARK="#4a2410"),
                  "glacier": dict(FUR="#6a7480", HAIR="#4a5460", LIGHT="#b8c0c8", MARK="#2a3038", CLOTH="#3a6a9a"),
                  "kermode": dict(FUR="#f0ece0", HAIR="#f8f4ec", LIGHT="#ffffff", MARK="#c8c0b0", CLOTH="#3a7a4a"),
                  "brown": dict(FUR="#5a3e26", HAIR="#3e2a18", LIGHT="#a8805a", MARK="#2a1a0e")}),
    "grizzly_canid": dict(
        build="strong", outfit=dict(top=["sports", "vest"], bottom="loincloth", extras=["satchel", "arm_wraps"], colors=dict(leather="#4a3a2a", bandage="#c8b898")),
        hair="shaggy", bangs="messy", ears="round", legs="fur", foot="paw", tail=[(1.75, 1.5, 1.5)], tail_rest=[(-20, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("tan"), HAIR="#5a3a22", FUR="#6a4a2e", LIGHT="#a8805a", MARK="#3a2414", EYE="#3a2412", CLOTH="#4a5a3a", TRIM="#a8987a",
                 pattern="solid"),
        variants={"cinnamon": dict(FUR="#9a5a2e", HAIR="#7a3e1e", LIGHT="#d0905a"),
                  "silvertip": dict(FUR="#7a6a5a", HAIR="#c8c0b0", LIGHT="#d8d0c0", pattern="speckle"),
                  "dark": dict(FUR="#3a2a1e", HAIR="#1e140c", LIGHT="#5a4636"),
                  "blond": dict(FUR="#b8946a", HAIR="#d8bc8a", LIGHT="#ecdcb8"),
                  # homage (2026-10-31): Banjo (Banjo-Kazooie): warm brown fur, yellow shorts, a blue backpack
                  "backpack_bear": dict(FUR="#8a5a30", HAIR="#6a4220", LIGHT="#d8b080", EYE="#1e140c", CLOTH="#e8c030", TRIM="#2a5ab8", extra=dict(leather="#2a5ab8", cloth2="#e8c030"))}),
}


def melon_parts(d):
    return [("melon", 6, 5, 3.5), ("melon_cut", 6, 5, .25), ("mflower", 1.5, 1.5, .5)] if d.get("melon") else []


def paint_melon(pt, pal):
    rind, flesh = G.shades(pal.extra.get("rind", "#4a9a3a")), G.shades(pal.extra.get("flesh", "#e84a5a"))
    pt("melon", "all", lambda f, x, y, w, h: pick(rind, 0.35 + 0.3 * ((x // 2) % 2) + (noise(x, y, 47) - 0.5) * 0.08, x, y))
    pt("melon_cut", "all", lambda f, x, y, w, h: pick(rind, 0.6, x, y) if x in (0, w - 1) or y in (0, h - 1)
       else pick(G.shades("#1e1410"), 0.4, x, y) if (x * 3 + y * 5) % 7 == 0 and not pal.extra.get("seedless") else pick(flesh, 0.55, x, y))
    pt("mflower", "all", lambda f, x, y, w, h: pick(G.shades("#f8a0c0"), 0.6 + 0.2 * ((x + y) % 2), x, y))


def build_melon(r, g):
    hy, z = g.hy, g.z                                    # half a watermelon worn on her back, cut face outward; a flower behind her ear
    r.add("melon", "body", [0, 16, 2 + z], [cube("melon", [-3, 13.5, 1.6 + z]), cube("melon_cut", [-3, 13.5, 5.1 + z])])
    r.put("head", cube("mflower", [2.6, hy + 6.4, -2.2 + z]))


def declare_hook(sp, d):
    if d.get("cheads"):                                  # Cerberus's two little heads: a neck, a head, a cap of hair, two ears
        return [("cneck", 1.75, 4, 1.75), ("chead", 4, 4, 4), ("chair", 4.5, 1.5, 4.5), ("cear", 1.25, 1.75, .75), ("csnout", 1.75, 1.25, 1)]
    if d.get("flames"):                                  # the hellhound's flame tufts: a flame and its flickering tip
        return [("efl_a", 3, 1.25, .75), ("efl_b", 3, 1, .75), ("efl_c", 2.5, .75, .5), ("spike_a", 1.75, 2.5, 1.75), ("spike_b", 1, 1.75, 1),
                ("fur_up", 2.75, 3.75, 2.75), ("fur_fa", 3.25, 3.25, 3.25), ("fur_thigh", 4.5, 4.5, 4.5), ("fur_shin", 4.25, 5, 4.25),
                ("hclaw", .5, 2, .5), ("fclaw", .75, .75, 3), ("hcollar", 4.5, .75, 4.5), ("hskull", 1.75, 1.75, .75), ("hchain", .5, 2.5, .5), ("ember", 1.75, .5, .25)]
    if d.get("hflower"):                                 # a big flower behind her ear: four petals round a heart
        return [("hpetal", .5, 2, 2), ("hbloom", .5, 1.25, 1.25)]
    if d.get("bigclub"):                                 # the rheintalensis's club: a thick haft, a knotted head
        return [("bclub", 2, 8, 2), ("bclub_head", 3.25, 4.5, 3.25), ("bclub_knob", 1, 1, 1)]
    if d.get("club"):                                    # the macuahuitl: a flat wooden club edged with obsidian blades
        return [("club", 1.75, 11, .75), ("club_edge", .5, 1.25, .5), ("club_grip", 1, 3.5, 1)]
    if d.get("melon"):
        return melon_parts(d)
    if d.get("mummy_kit"):                              # the mummy variants' muzzle and the blade in the head
        out = [("muzzle", 3.5, 2.25, 3.5), ("machete_blade", 14, 3, .5), ("machete_guard", .75, 3, 2), ("machete_grip", 4, 1.25, 1.25),
               ("cleaver_blade", 5, 4.5, .5), ("cleaver_handle", 1, 1, 7)]
        return out + ([("scale_beam", 5, .5, .5), ("scale_pan", 1.5, .25, 1.5)] if d.get("scales") else [])
    if d.get("tarnhelm"):
        return [("helm_cap", 8.75, 3.25, 8.75), ("nasal", .75, 3, .5), ("helm_gem", 1.25, 1.25, .5), ("helm_crest", 1, 1.5, 6)]
    if d.get("helmet"):
        return [("helmet", 5, 2, 5), ("helmet_brim", 6, .5, 6), ("lamp", 1.5, 1.5, .5)]
    return [("scale_beam", 5, .5, .5), ("scale_pan", 1.5, .25, 1.5)] if d.get("scales") else []


def paint_hook(pt, pal, d):
    if d.get("sewn"):                                    # black stitches across her limbs, blood from the corners of her grin
        thread, blood = G.shades("#141012"), G.shades(pal.extra["gore"])
        seam = lambda f, x, y, w, h: pick(thread, 0.3, x, y) if y == h // 2 and x % 2 == 0 or y in (h // 2 - 1, h // 2 + 1) and x % 4 == 0 else None
        for p in ("arm_up", "thigh", "shin", "midriff"):
            pt(p, ("north", "east", "west"), seam)
        pt("head", ("north",), lambda f, x, y, w, h: pick(blood, 0.45 + 0.2 * (x % 3 == 0), x, y) if h * 0.76 <= y <= h * 0.94 and abs(x - w * 0.5) in range(int(w * 0.2), int(w * 0.24)) else None)
    if d.get("cheads"):
        skin_t = H.skin_fn(pal, 0.66)

        def face(f, x, y, w, h):                         # two bright eyes and a little fang
            if f != "north":
                return skin_t(f, x, y, w, h)
            if y in (6, 7, 8) and x in (3, 4, 11, 12):
                return pal.WHITE if y == 6 and x in (3, 11) else pal.EYE[1]
            return skin_t(f, x, y, w, h)
        pt("chead", "all", face)
        coat = G.fur_fn(pal, light_faces=(), pattern="solid", seed=79)
        for p_ in ("cneck", "chair", "cear", "csnout"):
            pt(p_, "all", coat)
    if d.get("flames"):
        fl, core = G.shades(pal.extra.get("flame", "#f8501e")), G.shades(pal.extra.get("flame_core", "#ffd860"))
        for p_ in ("efl_a", "efl_b", "efl_c"):           # hot at the root, by her eye
            pt(p_, "all", lambda f, x, y, w, h: pick(core if x < w * 0.4 else fl, 0.6 + 0.2 * (y % 2), x, y))
        pt("ember", "all", lambda f, x, y, w, h: pick(core if 0 < x < w - 1 else fl, 0.7, x, y))
        tuft, tip = G.shades(pal.extra.get("tuft", "#5a1a18")), G.shades(pal.extra.get("tuft_tip", "#a83a24"))
        pt("spike_a", "all", lambda f, x, y, w, h: pick(tuft, 0.45 + 0.2 * (x % 2), x, y))
        pt("spike_b", "all", lambda f, x, y, w, h: pick(tip, 0.5 + 0.2 * (y % 2), x, y))
        shag = G.fur_fn(pal, light_faces=(), grain=0.22, pattern="solid", seed=77, fluffy=True)
        for p_ in ("fur_up", "fur_fa", "fur_thigh", "fur_shin"):
            pt(p_, "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else shag(f, x, y, w, h))
        coat = G.fur_fn(pal, light_faces=(), grain=0.22, pattern="solid", seed=78)
        for p_ in ("palm", "fingers", "thumb", "paw"):
            pt(p_, "all", coat)
        claw = G.shades(pal.extra.get("claw", "#5a2030"))
        for p_ in ("hclaw", "fclaw"):
            pt(p_, "all", lambda f, x, y, w, h: pick(claw, 0.5 + 0.25 * (f == "up"), x, y))
        pt("hcollar", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.4 + 0.3 * (x % 3 == 0), x, y))
        bone = G.shades("#6a6a72")
        pt("hskull", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.2, x, y) if f == "north" and y == 2 and x in (1, 4) else pick(bone, 0.55, x, y))
        pt("hchain", "all", lambda f, x, y, w, h: pick(bone, 0.3 + 0.3 * (y % 2), x, y))
    if d.get("hflower"):
        fl, fin = G.shades(pal.extra.get("flower", "#e0281e")), G.shades(pal.extra.get("flower_in", "#f8d040"))
        pt("hpetal", "all", lambda f, x, y, w, h: pick(fl, 0.55 + 0.2 * (f in ("east", "west")) - 0.15 * (x == 0 or y == 0), x, y))
        pt("hbloom", "all", lambda f, x, y, w, h: pick(fin, 0.65, x, y))
    if d.get("bigclub"):
        cl, cd = G.shades(pal.extra.get("club", "#6a7284")), G.shades(pal.extra.get("club_dark", "#3a4050"))
        pt("bclub", "all", lambda f, x, y, w, h: pick(cd if y % 3 == 0 and f != "up" else cl, 0.45 + 0.1 * (x % 2), x, y))
        pt("bclub_head", "all", lambda f, x, y, w, h: pick(cd if noise(x, y, 61) > 0.7 else cl, 0.5 + 0.15 * (f == "up") + (noise(x, y, 62) - 0.5) * 0.15, x, y))
        pt("bclub_knob", "all", lambda f, x, y, w, h: pick(cd, 0.45, x, y))
    wood, glass = G.shades(pal.extra.get("club", "#6a4a2a")), G.shades(pal.extra.get("glass", "#1a1420"))
    pt("club", "all", lambda f, x, y, w, h: pick(wood, 0.45 + 0.12 * (x % 2) + 0.15 * (y % 5 == 0), x, y))
    pt("club_edge", "all", lambda f, x, y, w, h: pick(glass, 0.35 + 0.45 * (x == 0 and y == 0), x, y))
    pt("club_grip", "all", lambda f, x, y, w, h: pick(G.shades("#c8a040") if y % 2 else wood, 0.5, x, y))
    paint_melon(pt, pal)
    gold = G.shades("#e8c050")
    pt("scale_beam", "all", lambda f, x, y, w, h: pick(gold, 0.6, x, y))
    pt("scale_pan", "all", lambda f, x, y, w, h: pick(gold, 0.7, x, y))
    helm, gem = G.shades(pal.extra.get("helm", "#b8c4d8")), G.shades(pal.extra.get("gem", "#5ae8c8"))
    pt("helm_cap", "all", lambda f, x, y, w, h: CLEAR if f == "down" else pick(helm, 0.5 + 0.25 * (f == "up") + 0.15 * (y == h - 1) - 0.12 * ((x + y) % 7 == 0), x, y))
    pt("nasal", "all", lambda f, x, y, w, h: pick(helm, 0.6, x, y))
    pt("helm_crest", "all", lambda f, x, y, w, h: pick(helm, 0.45 + 0.2 * (f == "up") + 0.1 * (z := (x + y) % 2), x, y))
    pt("helm_gem", "all", lambda f, x, y, w, h: pick(gem, 0.55 + 0.3 * (x == 0 and y == h - 1), x, y))
    tin = G.shades("#e8c040")                                          # the kobold's miner's helmet and its lamp
    pt("helmet", "all", lambda f, x, y, w, h: pick(tin, 0.5 + 0.25 * (f == "up"), x, y))
    pt("helmet_brim", "all", lambda f, x, y, w, h: pick(tin, 0.4, x, y))
    pt("lamp", "all", lambda f, x, y, w, h: pick(G.shades("#fff4c0"), 0.85, x, y))
    e = pal.extra
    mummy, blade = e.get("wrapped"), e.get("blade")
    show = lambda on, fn: (lambda f, x, y, w, h: fn(f, x, y, w, h) if on else CLEAR)
    wrap, steel, ink = G.shades("#ece6d4"), G.shades("#b8c8d4"), G.shades("#1a1414")

    def bandage(f, x, y, w, h):                         # bandage turns, with a shadow where one overlaps the next
        return pick(wrap, 0.25 if (2 * x + 5 * y) % 11 == 0 else 0.6 + 0.12 * ((y // 3) % 2), x, y)
    fur = lambda f, x, y, w, h: pick(pal.FUR, 0.5 + 0.1 * (f == "up"), x, y)
    pt("muzzle", "all", show(mummy, lambda f, x, y, w, h: pick(ink, 0.2, x, y) if f in ("north", "east", "west") and y == h - 3
                             else bandage(f, x, y, w, h) if f == "east" and x > w * 0.5 else fur(f, x, y, w, h)))
    pt("machete_blade", "all", show(blade == "machete", lambda f, x, y, w, h: pick(steel, 0.45 if y >= h - 1 - (x // 3) % 2 else 0.7, x, y)))
    pt("machete_guard", "all", show(blade == "machete", lambda f, x, y, w, h: pick(G.shades("#8a8a92"), 0.5, x, y)))
    pt("machete_grip", "all", show(blade == "machete", lambda f, x, y, w, h: pick(G.shades("#7a4a1a"), 0.4 + 0.2 * ((x // 2) % 2), x, y)))
    pt("cleaver_blade", "all", show(blade == "cleaver", lambda f, x, y, w, h: pick(steel, 0.55 + 0.2 * (y == 0) - 0.15 * (y == h - 1), x, y)))
    pt("cleaver_handle", "all", show(blade == "cleaver", lambda f, x, y, w, h: pick(G.shades("#c81e24"), 0.5 + 0.15 * (f == "up"), x, y)))
    if mummy:
        # a bandaged head: only a band of fur shows round the eyes
        pt("head", ("north",), lambda f, x, y, w, h: None if h * 0.34 <= y < h * 0.62 else bandage(f, x, y, w, h))
        pt("head", ("south", "east", "west", "up"), bandage)
        for p in ("tail_a", "tail_b", "tail_c"):
            pt(p, ("north", "south", "east", "west", "up"), bandage)
        legs, shoes = G.shades(e.get("legs", "#4e4448")), G.shades(e.get("shoes", "#141414"))
        for p in ("thigh", "shin"):
            pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(legs, 0.45 + 0.08 * (x % 5 == 0), x, y))
        pt("paw", "all", lambda f, x, y, w, h: pick(shoes, 0.35 + 0.12 * (f == "up"), x, y))
        top = pal.CLOTH
        pt("arm_up", ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(top, 0.5, x, y))          # sleeves
        if e.get("suit"):                               # Papa's grey suit jacket, closed over the chest, lapels and all
            for p in ("chest", "midriff", "bust", "hips"):
                pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(G.shades("#6a6a72"), 0.35, x, y)
                   if f == "north" and abs(abs(x + 0.5 - w / 2) - (h - y) * 0.35) < 0.6 else pick(top, 0.5, x, y))
        else:                                           # the dog's overalls: blue bib and gold buttons over the red shirt
            denim = G.shades(e.get("legs", "#1a78b0"))
            for p in ("midriff", "hips"):
                pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(G.shades("#f0c020"), 0.6, x, y)
                   if f == "north" and y <= 1 and x in (2, w - 3) else pick(denim, 0.5, x, y))

    if e.get("blood"):                                  # the homage's blood: where the blade went in, running down the face
        gore = G.shades("#b0101a")

        def drips(cols, f_ok=("north",)):
            def fn(f, x, y, w, h):
                if f not in f_ok:
                    return None
                for c, length in cols:
                    cx, end = int(w * c), int(h * length)
                    if (x in (cx, cx + 1) and y < end) or (y in (end, end + 1) and cx - 1 <= x <= cx + 2):
                        return pick(gore, 0.45 + 0.15 * (x == cx), x, y)
                return None
            return fn
        pt("head", ("up",), lambda f, x, y, w, h: pick(gore, 0.5, x, y) if abs(y - h // 2) <= 1 and w * 0.2 < x < w * 0.8 else None)
        pt("head", ("north",), drips([(0.36, 0.55), (0.46, 0.85), (0.56, 0.4)]))
        pt("muzzle", ("up", "north"), lambda f, x, y, w, h: pick(gore, 0.5, x, y) if (x, y) in {(3, 2), (4, 2), (9, 4), (10, 4), (10, 5)} else None)
        pt("machete_blade", ("north", "south"), lambda f, x, y, w, h: pick(gore, 0.5, x, y) if (x * 5 + y * 3) % 23 == 0 and x < w * 0.6 else None)
        pt("cleaver_blade", ("north", "south"), lambda f, x, y, w, h: pick(gore, 0.5, x, y) if 2 <= x <= 4 and 3 <= y <= 6 else None)
        if e.get("suit"):                               # Papa: it runs on down his shirt and spatters his suit
            pt("chest", ("north",), drips([(0.48, 1.0)]))
            for p in ("midriff", "hips"):
                pt(p, ("north",), lambda f, x, y, w, h: pick(gore, 0.5, x, y) if (x, y) in {(2, 3), (3, 3), (2, 4), (w - 4, 6), (w - 3, 6)} else None)


def spikes(r, bone, cx, cy, cz, rad, tilt, n=7):
    """a ring of dark-red spikes of fur round a limb, each with a brighter tip"""
    cs = []
    for i in range(n):
        a = 2 * math.pi * i / n
        x, z, rx, rz = cx + rad * math.cos(a), cz + rad * math.sin(a), tilt * math.sin(a), -tilt * math.cos(a)
        cs += [G.rot_cube("spike_a", [x - 0.875, cy, z - 0.875], [x, cy, z], rx, 0, rz),
               G.rot_cube("spike_b", [x - 0.5, cy + 2.3, z - 0.5], [x, cy, z], rx, 0, rz)]
    r.put(bone, *cs)


def hound_kit(r, g):
    """her shaggy limbs, the spikes at shoulder, wrist and ankle, the claws, the skull collar, the flames streaming from her eyes"""
    hy, z = g.hy, g.z
    ax, ay, az = r.b["arm_r"]["pivot"]
    fx, fy, fz = r.b["forearm_r"]["pivot"]
    hx, hy_, hz = r.b["hand_r"]["pivot"]
    r.put("arm_r", cube("fur_up", [ax - 1.375, ay - 3.5, az - 1.375]))
    r.put("forearm_r", cube("fur_fa", [fx - 1.625, fy - 3.1, fz - 1.625]))
    spikes(r, "arm_r", ax, ay - 1.0, az, 1.0, 62, 6)
    spikes(r, "forearm_r", hx, hy_ + 1.0, hz, 1.2, 128, 6)
    r.put("hand_r", *[cube("hclaw", [hx - 0.9 + i * 0.65, hy_ - 4.2, hz - 0.25]) for i in range(3)])
    lx, ly, lz = r.b["leg_r"]["pivot"]
    sx, sy, sz = r.b["shin_r"]["pivot"]
    r.put("leg_r", cube("fur_thigh", [lx - 2.25, sy, lz - 2.25]))
    r.put("shin_r", cube("fur_shin", [sx - 2.125, 1.25, sz - 2.125]))
    spikes(r, "shin_r", sx, 3.0, sz, 1.4, 132, 6)
    r.put("foot_r", *[cube("fclaw", [sx - 1.6 + i * 1.2, 0.1, sz - 4.5]) for i in range(3)])
    c = G.S("chest")[2] / 2 + G.S("bust")[2]
    r.put("body", cube("hcollar", [-2.25, 19.2, -2.25 + z]), cube("hskull", [-0.875, 17.9, -c - 0.5 + z]), cube("hchain", [-1.9, 17.2, -c - 0.2 + z]),
          cube("hchain", [1.4, 17.2, -c - 0.2 + z]), G.rot_cube("ember", [-2.3, 14.6, -G.S("midriff")[2] / 2 - 0.15 + z], [-1.4, 14.8, 0], 0, 0, 24),
          G.rot_cube("ember", [0.6, 13.9, -G.S("midriff")[2] / 2 - 0.15 + z], [1.4, 14.1, 0], 0, 0, -24))
    r.put("leg_r", G.rot_cube("ember", [lx - 0.9, sy + 2.0, lz - 2.35], [lx, sy + 2.2, lz], 0, 0, 20))
    cs = []
    for sg in (-1, 1):                                   # a jagged streak from the outer corner of each eye, up and away
        x0, y0, zf = sg * 2.4, hy + 3.0, -4.6 + z
        for part, dx, dy, tilt in (("efl_a", 0, 0, 34), ("efl_b", 2.4, 1.7, -8), ("efl_c", 5.0, 1.6, 46)):
            bx, by = x0 + sg * dx, y0 + dy
            ox = bx if sg > 0 else bx - G.S(part)[0]
            cs.append(G.rot_cube(part, [ox, by, zf], [bx, by, zf], 0, 0, tilt * sg))
    r.add("hair_eflame", "head", [0, hy + 4, zf], cs)


def build_hook(r, g, d):
    if d.get("cheads"):                                  # one on each shoulder, a little lower than her own and looking outward
        hy, z = g.hy, g.z
        cs = []
        for sg in (-1, 1):
            x, top, zz = sg * 9.0, hy - 0.5, -1.0 + z
            yaw = -18 * sg
            p_ = [x, top + 2, zz]
            cs += [G.rot_cube("cneck", [sg * 3.6 - 0.875, hy - 2.6, -0.875 + z], [sg * 3.6, hy - 2.6, z], 0, 0, -62 * sg), G.rot_cube("chead", [x - 2, top, zz - 2], p_, 0, yaw, 0),
                   G.rot_cube("csnout", [x - 0.875, top + 0.25, zz - 3], p_, 0, yaw, 0), G.rot_cube("chair", [x - 2.25, top + 3, zz - 2.25], p_, 0, yaw, 0),
                   G.rot_cube("cear", [x - 2, top + 4.25, zz - 0.5], p_, 0, yaw, 0), G.rot_cube("cear", [x + 0.75, top + 4.25, zz - 0.5], p_, 0, yaw, 0)]
        r.add("hair_cheads", "body", [0, hy, z], cs)
    if d.get("flames"):                                  # the hellhound (see hound_kit)
        hound_kit(r, g)
    if d.get("hflower"):                                 # tucked over her left ear, facing out
        hy, z = g.hy, g.z
        fx, fy, fz = 4.9, hy + 5.0, -1.0 + z
        r.add("hair_flower", "head", [fx, fy, fz], [cube("hpetal", [fx, fy - 1.0 + dy, fz - 1.0 + dz]) for dy, dz in ((1.2, 0), (-1.2, 0), (0, 1.2), (0, -1.2))]
              + [cube("hbloom", [fx + 0.2, fy - 0.625, fz - 0.625])])
    if d.get("bigclub"):                                 # held upright in her right fist, out in front of her arm
        px, py, pz = r.b["hand_r"]["pivot"]
        cx, cz = px - 0.6, pz - 2.4
        r.add("bigclub", "hand_r", [px, py - 1.5, pz], [cube("bclub", [cx - 1, py - 4.0, cz - 1]), cube("bclub_head", [cx - 1.625, py + 3.6, cz - 1.625]),
                                                        cube("bclub_knob", [cx - 2.3, py + 5.4, cz - 0.5]), cube("bclub_knob", [cx + 1.3, py + 6.4, cz - 0.5]),
                                                        cube("bclub_knob", [cx - 0.5, py + 5.0, cz - 2.3])])
    if d.get("club"):                                    # slung across her back, the grip over her right shoulder
        bz = G.S("chest")[2] / 2 + 0.3 + g.z
        rot = dict(pivot=[0, 16, bz], rotation=[0, 0, -38])
        cs = [cube("club", [-0.875, 8.5, bz], **rot), cube("club_grip", [-0.5, 19.5, bz - 0.125], **rot)]
        for k in range(5):
            cs += [cube("club_edge", [-1.375, 9.5 + 2 * k, bz + 0.125], **rot), cube("club_edge", [0.875, 9.5 + 2 * k, bz + 0.125], **rot)]
        r.add("club", "body", [0, 16, bz], cs)
    if d.get("melon"):
        build_melon(r, g)
    if d.get("mummy_kit"):                              # a muzzle; a machete (Papa) or a cleaver (the dog) stuck in the top of the head
        hy, z = g.hy, g.z
        r.put("head", cube("muzzle", [-1.75, hy + 0.3, -7.3 + z]))
        r.add("blade", "head", [0, hy + 8, z], [G.rot_cube("machete_blade", [-9, hy + 7.0, -0.25 + z], [0, hy + 8, z], 0, 0, -12),
                                               G.rot_cube("machete_guard", [4.6, hy + 7.0, -1 + z], [0, hy + 8, z], 0, 0, -12),
                                               G.rot_cube("machete_grip", [5.3, hy + 7.9, -0.625 + z], [0, hy + 8, z], 0, 0, -12),
                                               G.rot_cube("cleaver_blade", [-2.5, hy + 6.5, -0.25 + z], [0, hy + 8, z], 0, 0, 8),
                                               G.rot_cube("cleaver_handle", [1.4, hy + 9.6, -0.5 + z], [0, hy + 8, z], -20, 30, 8)])
    if d.get("tarnhelm"):                               # a silver helm over her hair, a nose guard, a crest, the gem on her brow
        hy, z = g.hy, g.z
        r.add("helm", "head", [0, hy + 6, z], [cube("helm_cap", [-4.375, hy + 5.4, -4.375 + z], inflate=0.6), cube("nasal", [-0.375, hy + 3.0, -5.1 + z]),
                                               cube("helm_gem", [-0.625, hy + 6.3, -5.25 + z]), cube("helm_crest", [-0.5, hy + 9.2, -3 + z])])
    if d.get("helmet"):
        r.add("helmet", "head", [0, g.hy + 8, g.z], [cube("helmet", [-2.5, g.hy + 8.0, -2.5 + g.z]), cube("helmet_brim", [-3, g.hy + 7.8, -3 + g.z]),
                                                    cube("lamp", [-0.75, g.hy + 8.3, -3.2 + g.z])])
    if d.get("scales"):                                                 # Anubis carries the scales of judgement at her hip
        r.add("scales", "body", [0, 13, -2.5], [cube("scale_beam", [-2.5, 12.0, -3.0]), cube("scale_pan", [-2.75, 11.5, -3.5]),
                                                 cube("scale_pan", [1.25, 11.5, -3.5])])


def flicker(an):
    """the flames never hold still: they swell and shrink in every looping animation"""
    for key in ("idle", "walk", "run", "sit", "lie", "stalk", "groom", "hiss"):
        A = an.A.get(f"animation.{an.name}.{key}")
        if A is None:
            continue
        n = max(4, int(A["animation_length"] / 0.15))
        n += n % 2                                       # an even count, so the loop closes on the same size
        step = A["animation_length"] / n
        sizes = [1.0 + 0.16 * math.sin(i * 2.4) * (1 if i % 2 else -0.7) for i in range(n)] + [None]
        sizes[-1] = sizes[0]
        F._inject(an, key, {"hair_eflame": {"scale": H.track(*[(round(i * step, 3), (round(q, 3),) * 3) for i, q in enumerate(sizes)])}})


def anim_extra(an, sp, d):
    if d.get("flames"):
        flicker(an)
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("howl", 2.0, False, merge({"head": {"rotation": Tk((0, (0, 0, 0)), (0.5, (-55, 0, 0)), (2.0, (-55, 0, 0)))},
                                      "body": {"rotation": hold(-8, 0, 0)}, "mouth": {"position": Tk((0, H.REST), (0.4, H.OUT_), (2.0, H.OUT_))}},
                                     sym("arm", hold(10, 0, 10))))
    an.add("fish", 3.0, True, merge({"root": {"position": hold(0, -4.5, 0)}, "body": {"rotation": hold(40, 0, 0)}, "head": {"rotation": hold(20, 0, 0)},
                                     "leg_r": {"rotation": hold(-80, 0, 0)}, "leg_l": {"rotation": hold(-80, 0, 0)},
                                     "shin_r": {"rotation": hold(110, 0, 0)}, "shin_l": {"rotation": hold(110, 0, 0)},
                                     "arm_r": {"rotation": Tk((0, (-60, 0, 10)), (1.0, (-100, 0, 10)), (1.5, (-30, 0, 10)), (3.0, (-60, 0, 10)))},
                                     "arm_l": {"rotation": hold(-50, 0, -10)}}))
    an.add("roll", 1.0, True, merge({"root": {"rotation": Tk((0, (0, 0, 0)), (0.5, (180, 0, 0)), (1.0, (360, 0, 0))), "position": hold(0, 5, 0)},
                                     "leg_r": {"rotation": hold(-90, 0, 0)}, "leg_l": {"rotation": hold(-90, 0, 0)},
                                     "shin_r": {"rotation": hold(120, 0, 0)}, "shin_l": {"rotation": hold(120, 0, 0)}},
                                    sym("arm", hold(-120, 0, 30))))
    an.add("sneeze", 1.0, False, merge({"head": {"rotation": Tk((0, (0, 0, 0)), (0.4, (-20, 0, 0)), (0.55, (25, 0, 0)), (1.0, (0, 0, 0)))},
                                        "body": {"rotation": Tk((0, (0, 0, 0)), (0.4, (-6, 0, 0)), (0.55, (10, 0, 0)), (1.0, (0, 0, 0)))},
                                        "eyelids": {"position": Tk((0, H.REST), (0.3, H.OUT_), (0.8, H.OUT_), (1.0, H.REST))},
                                        "mouth": {"position": Tk((0, H.REST), (0.45, H.OUT_), (0.65, H.REST))}}))
    an.add("point", 1.5, False, merge({"head": {"rotation": hold(-5, -20, 0)}, "body": {"rotation": hold(0, -15, 0)}},
                                      {"arm_r": {"rotation": Tk((0, (0, 0, 0)), (0.3, (-90, -20, 0)), (1.5, (-90, -20, 0)))}}))
    an.add("judge", 2.0, False, merge({"scales": {"position": Tk((0, (0, 0, 0)), (0.5, (-3, 9, -2)), (2.0, (-3, 9, -2)))},
                                       "head": {"rotation": hold(-5, 0, 0)}},
                                      {"arm_r": {"rotation": Tk((0, (0, 0, 0)), (0.5, (-150, 0, 0)), (2.0, (-150, 0, 0)))}}))


# homage looks of the twenty-eighth wave (POP_CULTURE_VARIANTS.md)
SPECIES["panda_canid"]["variants"]["forest_keeper"] = dict(HAIR="#6a7078", FUR="#7a8088", LIGHT="#ece4cc", MARK="#3a3e44", EYE="#1e1e24", CLOTH="#4a6a4a", TRIM="#ece4cc")
SPECIES["grizzly_canid"]["variants"]["sleepy_giant"] = dict(HAIR="#2a5a6a", FUR="#34697a", LIGHT="#f0e4c8", MARK="#1e4250", EYE="#1e1e24", CLOTH="#f0e4c8", TRIM="#2a5a6a")


# homage looks of the twenty-ninth wave (POP_CULTURE_VARIANTS.md)
SPECIES["kobold_canid"]["variants"]["blue_heeler"] = dict(HAIR="#5a7ab8", FUR="#6a8ac8", LIGHT="#c8d8f0", MARK="#2a3e7a", EYE="#2a2a30", CLOTH="#e8a040", TRIM="#2a3e7a")


SPECIES["wolf_canid"]["variants"]["sun_wolf"] = dict(HAIR="#f4f0e8", FUR="#f4f0e8", LIGHT="#ffffff", MARK="#c82a2a", EYE="#c82a2a", CLOTH="#c82a2a", TRIM="#e8c050")   # homage look of the thirty-sixth wave (POP_CULTURE_VARIANTS.md)


SPECIES["panda_canid"]["variants"]["fight_panda"] = dict(CLOTH="#d8702a", TRIM="#e8c050")   # homage look of the thirty-sixth wave (POP_CULTURE_VARIANTS.md)


if __name__ == "__main__":
    F.run_family("canid", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
