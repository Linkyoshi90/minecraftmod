#!/usr/bin/env python3
"""Bugs (the insect family, see bug_field_guide.html): mosquito, moth (and her caterpillar), fairy (and the Fairy Queen),
scorpion, bee (and the Queen Bee), papillon (and her caterpillar).

    python tools/gen_bug.py [species ...] [--previews]

    winged bipeds    girl_family with insect wings (wing_r/l, wing2_r/l: see-through, drawn translucent), antennae; the
                     mosquito has a proboscis, the moth a fur collar, the bee and the queen a striped abdomen behind
                     (`gaster`), the queen four arms (arm2_r/l) and a crown, the Fairy Queen great butterfly wings
    caterpillars     a small girl on a short caterpillar body (seg_1..seg_8, the lamia's rig), striped or spotted
    scorpion         girl_lib upper body on the arachne's thorax and legs, with pincers and a jointed tail curling up over
                     her back (tail_seg, tail_seg_b.. and the stinger); the brood bone shows young riding on her back
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
import girl_parts as P  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

LARVA_SEGS = [("seg_1", 4, 6.5, 3.5), ("seg_2", 4.25, 3.5, 3.5), ("seg_3", 4, 3.25, 3.5), ("seg_4", 3.75, 3, 3.5),
              ("seg_5", 3.5, 2.75, 3), ("seg_6", 3, 2.5, 3), ("seg_7", 2.5, 2.25, 2.5), ("seg_8", 2, 2, 2)]

SPECIES = {
    # twentieth wave (folklore): the Mothman of Point Pleasant, a soft blue moth girl with feathery antennae, a furry ruff, spotted
    # wings and red eyes that shine in the dark
    "mothman_bug": dict(
        build="standard", outfit=dict(top=["sweater"], bottom="wrap_skirt", colors=dict(cloth2="#3a5ab8")), hair="wavy", bangs="wispy", ears="plume",
        legs="fur", foot="paw", wings="moth", flies=True, collar=True,
        pal=dict(SKIN="#a8bcec", HAIR="#5a7ad0", FUR="#6a88d8", LIGHT="#c8d8f8", MARK="#2a3a8a", EYE="#e82a2a", CLOTH="#4a6ac8", TRIM="#2a3a8a",
                 extra=dict(antenna="#2a3a8a", wing="#5a7ad0", wing_vein="#2a3a8a", wing_alpha=240, wing_pattern="eyespot", eyespot="#2a3a8a")),
        variants={"grey": dict(SKIN="#b8b8c0", HAIR="#5a5a62", FUR="#6a6a74", LIGHT="#c8c8d0", MARK="#2a2a30", CLOTH="#4a4a54",
                               extra=dict(antenna="#2a2a30", wing="#6a6a74", wing_vein="#2a2a30", wing_alpha=240, wing_pattern="eyespot", eyespot="#c82a2a")),
                  "black": dict(SKIN="#8a8a98", HAIR="#1e1e24", FUR="#2a2a30", LIGHT="#5a5a64", MARK="#0e0e12", CLOTH="#1e1e24",
                                extra=dict(antenna="#0e0e12", wing="#2a2a30", wing_vein="#0e0e12", wing_alpha=245, wing_pattern="eyespot", eyespot="#e82a2a")),
                  "brown": dict(SKIN="#d8c0a8", HAIR="#6a4a2e", FUR="#7a5a3e", LIGHT="#c8a888", MARK="#3a2414", CLOTH="#5a3a22",
                                extra=dict(antenna="#3a2414", wing="#7a5a3e", wing_vein="#3a2414", wing_alpha=240, wing_pattern="bands")),
                  "pink": dict(SKIN="#f0c8d8", HAIR="#d870a0", FUR="#e890b8", LIGHT="#fce0ec", MARK="#8a2a5a", CLOTH="#c8508a",
                               extra=dict(antenna="#8a2a5a", wing="#e890b8", wing_vein="#8a2a5a", wing_alpha=240, wing_pattern="eyespot", eyespot="#f8e070"))}),
    # seventeenth wave: the date palm beetle, a round little blue beetle with a cluster of dates and palm leaves in her hair
    "date_beetle_bug": dict(
        build="petite", outfit=dict(top=["halter", "capelet"], bottom="wrap_skirt", extras=["belt"], colors=dict(cloth2="#6a8a4a")),
        hair="bob", bangs="blunt", ears="antenna", legs="stockings", foot="shoe", elytra=True, wings="insect", dates=True,
        pal=dict(SKIN=skin("fair"), HAIR="#4a6ac8", FUR="#5a7ad0", LIGHT="#b8c8f0", MARK="#2a3a8a", EYE="#8a5a2a", CLOTH="#6a8a4a", TRIM="#d8b040",
                 extra=dict(antenna="#2a3a8a", stocking="#4a6ac8", boot="#2a3a8a", shell="#4a6ac8", shell2="#a8c0f0", wing="#e8d8f8", wing_vein="#a890c8",
                            wing_alpha=190, date="#6a1e1a", dleaf="#6a8a4a")),
        variants={"green": dict(HAIR="#3a8a4a", FUR="#4a9a5a", LIGHT="#b8e8c0", MARK="#1e4a2a",
                                extra=dict(antenna="#1e4a2a", stocking="#3a8a4a", boot="#1e4a2a", shell="#3a8a4a", shell2="#a8e0b0", wing="#e8f0d8",
                                           wing_vein="#8ab07a", wing_alpha=190, date="#6a1e1a", dleaf="#6a8a4a")),
                  "amber": dict(HAIR="#c8802a", FUR="#d8903a", LIGHT="#f8d8a0", MARK="#6a3a10", CLOTH="#4a6a3a",
                                extra=dict(antenna="#6a3a10", stocking="#c8802a", boot="#6a3a10", shell="#c8802a", shell2="#f8d090", wing="#f8ecd8",
                                           wing_vein="#c89a6a", wing_alpha=190, date="#4a1410", dleaf="#5a7a3a")),
                  "plum": dict(HAIR="#6a3a8a", FUR="#7a4a9a", LIGHT="#d8b8e8", MARK="#3a1a4a",
                               extra=dict(antenna="#3a1a4a", stocking="#6a3a8a", boot="#3a1a4a", shell="#6a3a8a", shell2="#d0a8e8", wing="#f0e0f8",
                                          wing_vein="#b08ac8", wing_alpha=190, date="#6a1e1a", dleaf="#6a8a4a")),
                  "black": dict(HAIR="#2a2630", FUR="#3a3440", LIGHT="#8a8494", MARK="#141018", EYE="#e8b040",
                                extra=dict(antenna="#141018", stocking="#2a2630", boot="#141018", shell="#2a2630", shell2="#6a8ac8", wing="#e0e0ec",
                                           wing_vein="#8a8a9a", wing_alpha=190, date="#6a1e1a", dleaf="#6a8a4a"))}),
    # sixteenth wave: the temple mantis, lapis blue worked with gold filigree, a jewelled crown and a gold ring arch behind her head
    "temple_mantis_bug": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], bottom="sarong", extras=["bracers", "usekh"], colors=dict(cloth2="#f0e4c0")),
        hair="bob", bangs="blunt", ears="antenna", legs="stockings", foot="shoe", wings="insect", scythes=True, crown=True, halo=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e3a9a", FUR="#2a4ab8", LIGHT="#c8d8f8", MARK="#14246a", EYE="#3a8ae8", CLOTH="#2a44a8", TRIM="#e8c040",
                 extra=dict(wing="#f0e4c0", wing_vein="#c8a040", wing_alpha=200, antenna="#14246a", stocking="#2a4ab8", boot="#c8a040",
                            scythe="#3a5ac8", jewel="#3a6ae8")),
        variants={"jade": dict(HAIR="#2a7a5a", FUR="#3a9a6a", LIGHT="#c8f0d8", MARK="#1e4a3a", EYE="#e8c040", CLOTH="#2a7a5a",
                               extra=dict(wing="#e8f0d8", wing_vein="#c8a040", wing_alpha=210, antenna="#1e4a3a", stocking="#3a9a6a", boot="#c8a040",
                                          scythe="#3a9a6a", jewel="#3ae8a0")),
                  "orchid": dict(SKIN=skin("pale"), HAIR="#f0a8c8", FUR="#f8d0e0", LIGHT="#ffffff", MARK="#d870a0", EYE="#a83a7a", CLOTH="#f8e8f0",
                                 extra=dict(wing="#fbe8f0", wing_vein="#e8a8c8", wing_alpha=200, antenna="#d870a0", stocking="#f8d0e0", boot="#d870a0",
                                            scythe="#f8d0e0", jewel="#e83a8a")),
                  "sand": dict(HAIR="#a8885a", FUR="#c8a870", LIGHT="#f0e0c0", MARK="#7a5a30", EYE="#3a8ab8", CLOTH="#f0e8d0",
                               extra=dict(wing="#e8d8b0", wing_vein="#7a5a30", wing_alpha=220, antenna="#7a5a30", stocking="#c8a870", boot="#5a3a1a",
                                          scythe="#c8a870", jewel="#3a8ab8")),
                  "ruby": dict(HAIR="#8a1e2a", FUR="#a82a3a", LIGHT="#f8c8d0", MARK="#4a0e14", EYE="#e8c040", CLOTH="#8a1e2a",
                               extra=dict(wing="#f0e0c8", wing_vein="#c8a040", wing_alpha=210, antenna="#4a0e14", stocking="#a82a3a", boot="#c8a040",
                                          scythe="#a82a3a", jewel="#e83a4a"))}),
    "mosquito_bug": dict(
        build="slim", outfit=dict(top=["sports"], bottom="shorts", colors=dict(cloth2="#2a2a2e")), hair="shaggy", bangs="messy", ears="antenna",
        legs="stockings", foot="shoe", wings="insect", flies=True, proboscis=True,
        pal=dict(SKIN="#c8c4c0", HAIR="#3a3836", FUR="#5a5652", LIGHT="#a8a4a0", MARK="#1e1c1a", EYE="#c83a2a", CLOTH="#2a2a2e", TRIM="#c83a2a",
                 extra=dict(antenna="#2a2826", wing="#e8eef4", wing_vein="#4a4a52", stocking="#2a2a2e", boot="#1a1a1c")),
        variants={"tiger": dict(HAIR="#141414", CLOTH="#141414", TRIM="#f0f0f0", extra=dict(antenna="#141414", wing="#e8eef4", wing_vein="#141414", stocking="#f0f0f0", boot="#141414")),
                  "red": dict(SKIN="#d8b0a0", HAIR="#7a2a1a", CLOTH="#5a1a14", extra=dict(antenna="#3a1a14", wing="#f4e8e4", wing_vein="#7a2a1a", stocking="#5a1a14", boot="#2a0e0a")),
                  "swamp": dict(SKIN="#b8c4a8", HAIR="#3a4a2a", CLOTH="#3a4a2a", extra=dict(antenna="#2a3a1e", wing="#e8f4e0", wing_vein="#3a4a2a", stocking="#3a4a2a", boot="#1e2a14")),
                  "gold": dict(SKIN="#e0d0b0", HAIR="#a87a2a", CLOTH="#8a6a2a", extra=dict(antenna="#5a3a10", wing="#f8f0d8", wing_vein="#a87a2a", stocking="#8a6a2a", boot="#3a2a10"))}),
    "moth_bug": dict(
        build="standard", outfit=dict(top=["sweater"], bottom="wrap_skirt", colors=dict(cloth2="#c8d8a8")), hair="wavy", bangs="wispy", ears="plume",
        legs="fur", foot="paw", wings="moth", flies=True, collar=True,
        pal=dict(SKIN=skin("fair"), HAIR="#e8f0d8", FUR="#e0eccc", LIGHT="#f8fcf0", MARK="#8aa86a", EYE="#3a3a3a", CLOTH="#b8d8a0", TRIM="#e8d8a0",
                 extra=dict(antenna="#c8b88a", wing="#b8e0a0", wing_vein="#6a8a4a", wing_alpha=235, wing_pattern="eyespot", eyespot="#d8a040")),
        variants={"atlas": dict(HAIR="#c88a5a", FUR="#b8784a", MARK="#5a2a14", CLOTH="#a85a3a", extra=dict(antenna="#7a4a2a", wing="#b8683a", wing_vein="#4a2014", wing_alpha=240, wing_pattern="border", eyespot="#f8f0d8")),
                  "rosy": dict(HAIR="#f8d0e0", FUR="#f8e0a0", MARK="#e888b0", CLOTH="#f0a8c8", extra=dict(antenna="#e8c070", wing="#f0a0c0", wing_vein="#f0d070", wing_alpha=240, wing_pattern="bands")),
                  "silk": dict(HAIR="#f8f4ec", FUR="#f4f0e4", MARK="#c8c0b0", CLOTH="#e8e0d0", extra=dict(antenna="#c8c0b0", wing="#f8f4ec", wing_vein="#c8c0b0", wing_alpha=230)),
                  "hawk": dict(HAIR="#8a7a6a", FUR="#7a6a5a", MARK="#3a3028", CLOTH="#6a5a4a", extra=dict(antenna="#4a3a2a", wing="#8a7a6a", wing_vein="#3a3028", wing_alpha=240, wing_pattern="bands")),
                  # homage (2026-10-30): Mothra: gold and orange wings, a white ruff
                  "kaiju_moth": dict(HAIR="#f4f0e0", FUR="#f8e8a0", MARK="#c8501e", CLOTH="#e8902a", TRIM="#f8f4e8", extra=dict(antenna="#7a4a2a", wing="#e8902a", wing_vein="#5a2014", wing_alpha=245, wing_pattern="bands"))}),
    "firefly_bug": dict(
        build="petite", outfit=dict(top=["halter"], bottom="wrap_skirt", colors=dict(cloth2="#3a2a24")), hair="bob", bangs="blunt", ears="antenna",
        legs="bare", foot="shoe", wings="insect", flies=True, lantern=True, glow=["lantern"],
        pal=dict(SKIN=skin("fair"), HAIR="#2a2420", FUR="#3a2a24", LIGHT="#f8e8a0", MARK="#e85a2a", EYE="#3a2a20", CLOTH="#e85a2a", TRIM="#f8d040",
                 extra=dict(wing="#4a3a30", wing_vein="#2a1e18", wing_alpha=230, antenna="#2a2420", boot="#2a2420", lantern="#f8e070")),
        variants={"green": dict(extra=dict(wing="#3a4a30", wing_vein="#1e2a18", wing_alpha=230, antenna="#2a2420", boot="#2a2420", lantern="#b8f870")),
                  "blue": dict(HAIR="#1e2a3a", CLOTH="#3a5aa8", extra=dict(wing="#2a3448", wing_vein="#141a28", wing_alpha=230, antenna="#1e2a3a",
                                                                           boot="#1e2a3a", lantern="#80d8f8")),
                  "pink": dict(CLOTH="#e86aa8", extra=dict(wing="#4a3040", wing_vein="#2a1a24", wing_alpha=230, antenna="#2a2420", boot="#2a2420",
                                                           lantern="#f8a0d8")),
                  "white": dict(HAIR="#e8e4d8", CLOTH="#a8a8b0", extra=dict(wing="#d8d4c8", wing_vein="#8a8478", wing_alpha=220, antenna="#8a8478",
                                                                            boot="#8a8478", lantern="#ffffff"))}),
    # thirteenth wave: the scarabronze, a bronze scarab: a gilded crown, a domed shell of two wing-cases on her back
    "scarabronze_bug": dict(
        build="athletic", outfit=dict(top=["bandeau"], bottom="wrap_skirt", extras=["usekh", "armlets", "belt"], colors=dict(cloth2="#2a3a8a", usekh2="#2a3a8a")),
        hair="hime", bangs="blunt", ears="antenna", legs="stockings", foot="shoe", crown=True, elytra=True,
        pal=dict(SKIN=skin("tan"), HAIR="#1e1a2e", FUR="#a8702a", LIGHT="#e8b860", MARK="#4a2a10", EYE="#e8c040", CLOTH="#2a3a8a", TRIM="#e8c040",
                 extra=dict(antenna="#4a2a10", stocking="#7a4a1e", boot="#4a2a10", shell="#b07a30", shell2="#e8c060")),
        variants={"blue": dict(FUR="#2a3a8a", LIGHT="#6a8ad8", MARK="#141e4a", CLOTH="#a8702a",
                               extra=dict(antenna="#141e4a", stocking="#2a3a8a", boot="#141e4a", shell="#2a4aa8", shell2="#e8c060")),
                  "green": dict(FUR="#2a7a4a", LIGHT="#6ac890", MARK="#14422a",
                                extra=dict(antenna="#14422a", stocking="#2a6a3a", boot="#14422a", shell="#2a8a5a", shell2="#a8f0c0")),
                  "gold": dict(FUR="#d8a840", LIGHT="#f8e090", MARK="#7a5410", CLOTH="#5a1e2a",
                               extra=dict(antenna="#7a5410", stocking="#a87a2a", boot="#7a5410", shell="#e8b840", shell2="#fff0b0")),
                  "black": dict(FUR="#2a2628", LIGHT="#6a6468", MARK="#0e0c0e", CLOTH="#a82a3a",
                                extra=dict(antenna="#0e0c0e", stocking="#2a2628", boot="#0e0c0e", shell="#2a2a34", shell2="#8a8ac8"))}),
    "mantis_bug": dict(                                  # ninth wave: scythe forearms, long folded wings, the prayer pose
        build="slim", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["bracers"], colors=dict(cloth2="#4a7a2a")), hair="ponytail", bangs="parted",
        ears="antenna", legs="stockings", foot="shoe", wings="insect", scythes=True,
        pal=dict(SKIN=skin("fair"), HAIR="#7ab84a", FUR="#8ac85a", LIGHT="#d8f0b0", MARK="#4a7a2a", EYE="#5aa82a", CLOTH="#6aa83a", TRIM="#e8d870",
                 extra=dict(wing="#b8e0a0", wing_vein="#4a7a2a", wing_alpha=210, antenna="#5a8a3a", stocking="#6aa83a", boot="#3a5a24", scythe="#8ac85a")),
        variants={"brown": dict(HAIR="#8a6a42", FUR="#9a7a52", LIGHT="#d8c0a0", MARK="#5a4028", EYE="#a87a3a", CLOTH="#7a5a3a",
                                extra=dict(wing="#c8b090", wing_vein="#5a4028", wing_alpha=220, antenna="#5a4028", stocking="#7a5a3a", boot="#3a2a1a", scythe="#9a7a52")),
                  "orchid": dict(SKIN=skin("pale"), HAIR="#f8d0e4", FUR="#f8e0ec", LIGHT="#ffffff", MARK="#e888b8", EYE="#d84a8a", CLOTH="#f8c0d8", TRIM="#f8e8a0",
                                 extra=dict(wing="#fbe8f0", wing_vein="#e8a8c8", wing_alpha=200, antenna="#e8a8c8", stocking="#f8e0ec", boot="#e888b8",
                                            scythe="#f8e0ec", petals=True)),
                  "leaf": dict(HAIR="#5a9a2a", FUR="#6aaa3a", MARK="#3a6a1e", CLOTH="#4a8a2a",
                               extra=dict(wing="#8ac85a", wing_vein="#3a6a1e", wing_alpha=240, antenna="#3a6a1e", stocking="#4a8a2a", boot="#2a4a14",
                                          scythe="#6aaa3a", leafcape=True)),
                  "ghost": dict(SKIN=skin("pale"), HAIR="#e8e4d4", FUR="#ece8dc", LIGHT="#ffffff", MARK="#a8a490", EYE="#8a8a7a", CLOTH="#c8c4b0",
                                extra=dict(wing="#f4f2ea", wing_vein="#a8a490", wing_alpha=190, antenna="#a8a490", stocking="#d8d4c4", boot="#8a8678",
                                           scythe="#ece8dc")),
                  # homage (2026-10-28): Scyther (Pokemon): bright green, a cream belly, pale blades and wings
                  "scythe_green": dict(HAIR="#5ad84a", FUR="#6ae05a", LIGHT="#f8f0c0", MARK="#2a8a2a", EYE="#1e1e1e", CLOTH="#f0e8b0", TRIM="#2a8a2a", extra=dict(wing="#f8f8ff", wing_vein="#c8c8d0", wing_alpha=200, antenna="#2a8a2a", stocking="#6ae05a", boot="#2a8a2a", scythe="#f0f0e8"))}),
    "fairy_bug": dict(
        build="petite", outfit=dict(top=["halter"], bottom="wrap_skirt", colors=dict(cloth2="#f8c8e0")), hair="twintails", bangs="swept", ears="elf",
        legs="bare", foot="shoe", wings="fairy", flies=True, flower=True,
        pal=dict(SKIN=skin("pale"), HAIR="#f8b8d8", FUR="#f8b8d8", LIGHT="#ffffff", MARK="#e870a8", EYE="#5aa8e8", CLOTH="#f8d8e8", TRIM="#f8e070",
                 extra=dict(wing="#f8d0f0", wing_vein="#e8a0d8", wing_alpha=140, boot="#f8a8c8")),
        variants={"blue": dict(HAIR="#a8d0f8", CLOTH="#d8e8f8", extra=dict(wing="#d0e8f8", wing_vein="#8ab8e8", wing_alpha=140, boot="#8ab8e8")),
                  "green": dict(HAIR="#a8e8b8", CLOTH="#d8f4dc", extra=dict(wing="#d0f4d8", wing_vein="#7ad89a", wing_alpha=140, boot="#7ad89a")),
                  "gold": dict(HAIR="#f8e090", CLOTH="#f8f0c8", extra=dict(wing="#f8f0c0", wing_vein="#e8c050", wing_alpha=150, boot="#e8c050")),
                  "violet": dict(HAIR="#c8a8f0", CLOTH="#e8d8f8", extra=dict(wing="#e0d0f8", wing_vein="#a888e0", wing_alpha=140, boot="#a888e0"))}),
    "fairy_queen_bug": dict(
        build="curvy", outfit=dict(top=["kimono"], bottom="hakama", extras=["pearls"], colors=dict(kimono="#f8e0ec", kimono_trim="#e8c040", obi="#e8c040", cloth2="#f8e8f0")),
        hair="flowing", bangs="parted", ears="elf", legs="bare", foot="shoe", wings="butterfly", wing_scale=1.35, flies=True, crown=True,
        pal=dict(SKIN=skin("pale"), HAIR="#f8c0d8", FUR="#f8c0d8", LIGHT="#ffffff", MARK="#e870a8", EYE="#e8a030", CLOTH="#f8e0ec", TRIM="#e8c040",
                 extra=dict(wing="#f8b0d8", wing_vein="#e8c040", wing_alpha=170, wing_pattern="border", boot="#e8c040")),
        variants={"azure": dict(HAIR="#a8d8f8", extra=dict(wing="#a8d0f8", wing_vein="#e8c040", wing_alpha=170, wing_pattern="border", kimono="#e0f0f8", boot="#e8c040")),
                  "emerald": dict(HAIR="#a8f0c8", extra=dict(wing="#a8f0c8", wing_vein="#e8c040", wing_alpha=170, wing_pattern="border", kimono="#e0f8ec", boot="#e8c040")),
                  "sun": dict(HAIR="#f8e090", extra=dict(wing="#f8e090", wing_vein="#e87a2a", wing_alpha=180, wing_pattern="border", kimono="#fff4d8", boot="#e8c040")),
                  "amethyst": dict(HAIR="#d8b0f8", extra=dict(wing="#d0a8f8", wing_vein="#e8c040", wing_alpha=170, wing_pattern="border", kimono="#f0e4fc", boot="#e8c040"))}),
    # twenty-fourth wave: the girtablilu, the scorpion folk of Mesopotamia: hooded and veiled, spiral tattoos, on the scorpion's rig
    "girtablilu_bug": dict(
        build="athletic", outfit=dict(top=["bandeau"], extras=["armlets"]), hair="wavy", bangs="parted", ears=None, body="scorpion", veil=True,
        pal=dict(SKIN=skin("tan"), HAIR="#2a3a7a", FUR="#3a5ab0", LIGHT="#7a9ad8", MARK="#1a2a6a", EYE="#2a2a4a", CLOTH="#5a9ad8", TRIM="#e8c050", extra=dict(veil="#5a9ad8")),
        variants={"sand": dict(FUR="#c8a050", LIGHT="#e8d090", MARK="#6a4a1e", HAIR="#3a2a1a", CLOTH="#a87a3a", extra=dict(veil="#a87a3a")),
                  "black": dict(FUR="#2a2628", LIGHT="#5a5458", MARK="#0e0c0e", HAIR="#141214", CLOTH="#3a3438", extra=dict(veil="#3a3438")),
                  "red": dict(FUR="#a83a1a", LIGHT="#e88a5a", MARK="#5a1a0a", HAIR="#5a1a0a", CLOTH="#c85a2a", extra=dict(veil="#c85a2a")),
                  "green": dict(FUR="#3a8a4a", LIGHT="#8ad08a", MARK="#1a4a24", HAIR="#1a3a24", CLOTH="#3a8a4a", extra=dict(veil="#3a8a4a"))}),
    "scorpion_bug": dict(
        build="athletic", outfit=dict(top=["bandeau", "harness"], extras=["armlets"]), hair="braid", bangs="jagged", ears=None, body="scorpion",
        pal=dict(SKIN=skin("tan"), HAIR="#3a2a1a", FUR="#c8a050", LIGHT="#e8d090", MARK="#6a4a1e", EYE="#2a1a10", CLOTH="#6a3a1a", TRIM="#e8c050"),
        variants={"black": dict(FUR="#2a2628", LIGHT="#5a5458", MARK="#0e0c0e", HAIR="#141214"),
                  "red": dict(FUR="#a83a1a", LIGHT="#e88a5a", MARK="#5a1a0a", HAIR="#5a1a0a"),
                  "emperor": dict(FUR="#2a3044", LIGHT="#5a6a8a", MARK="#0e1220", HAIR="#141824"),
                  "bark": dict(FUR="#7a5a3a", LIGHT="#a8885a", MARK="#3a2814", HAIR="#3a2814")}),
    "bee_bug": dict(
        build="petite", outfit=dict(top=["sweater"], bottom="shorts", colors=dict(cloth2="#2a2420")), hair="bob", bangs="blunt", ears="antenna",
        legs="stockings", foot="shoe", wings="insect", flies=True, gaster=True, collar=True, cap_kit=True,
        pal=dict(SKIN=skin("peach"), HAIR="#f0b830", FUR="#f8d040", LIGHT="#fff0a8", MARK="#2a2420", EYE="#2a2018", CLOTH="#f8c830", TRIM="#2a2420",
                 extra=dict(antenna="#2a2420", wing="#f0f4f8", wing_vein="#8a8a90", stocking="#2a2420", boot="#2a2420")),
        variants={"bumble": dict(HAIR="#f8d850", FUR="#f8d850", MARK="#141210", LIGHT="#ffffff", CLOTH="#141210"),
                  "carpenter": dict(HAIR="#2a2a32", FUR="#2a2a32", MARK="#141418", LIGHT="#5a5a6a", CLOTH="#2a2a32", extra=dict(antenna="#141418", wing="#b8a8e8", wing_vein="#3a2a6a", stocking="#141418", boot="#141418")),
                  "orchid": dict(HAIR="#3ab878", FUR="#3ab878", MARK="#1a5a3a", LIGHT="#a8f0c8", CLOTH="#2a8a5a"),
                  "red": dict(HAIR="#2a2420", FUR="#2a2420", MARK="#d84a2a", LIGHT="#f8a080", CLOTH="#d84a2a"),
                  # homage (2026-10-30): Charmy Bee (Sonic): yellow and black, an orange vest, an aviator's cap and goggles
                  "flight_cap": dict(HAIR="#f8c830", FUR="#f8c830", MARK="#1e1a18", LIGHT="#fff0a8", CLOTH="#e8782a", TRIM="#1e1a18",
                                     extra=dict(antenna="#1e1a18", wing="#f0f4f8", wing_vein="#8a8a90", stocking="#1e1a18", boot="#d82a2a", cap=True))}),
    "hornet_bug": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts", extras=["belt"], colors=dict(cloth2="#5a2a14")), hair="ponytail", bangs="jagged",
        ears="antenna", legs="stockings", foot="shoe", wings="insect", flies=True, gaster=True, collar=True,
        pal=dict(SKIN=skin("tan"), HAIR="#8a3a1e", FUR="#e8b830", LIGHT="#f8e08a", MARK="#5a2a14", EYE="#3a1a0e", CLOTH="#8a3a1e", TRIM="#e8b830",
                 extra=dict(antenna="#5a2a14", wing="#e8d8b0", wing_vein="#8a5a2a", stocking="#5a2a14", boot="#3a1a0e")),
        variants={"giant": dict(HAIR="#e8902a", FUR="#e8a030", MARK="#2a1a10", CLOTH="#2a1a10", TRIM="#e8902a"),
                  "baldfaced": dict(HAIR="#f0f0f0", FUR="#1a1a1e", MARK="#f0f0f0", LIGHT="#5a5a6a", CLOTH="#1a1a1e", TRIM="#f0f0f0",
                                    extra=dict(antenna="#1a1a1e", wing="#d8dce8", wing_vein="#3a3a40", stocking="#1a1a1e", boot="#1a1a1e")),
                  "yellowlegged": dict(HAIR="#2a2018", FUR="#2a2018", MARK="#e8902a", LIGHT="#6a5a4a", CLOTH="#2a2018", TRIM="#e8902a",
                                       extra=dict(antenna="#2a2018", wing="#c8b8a8", wing_vein="#3a2a1e", stocking="#e8c040", boot="#e8c040")),
                  "oriental": dict(HAIR="#8a3a1e", FUR="#8a3a1e", MARK="#f0c830", LIGHT="#c8784a", CLOTH="#8a3a1e", TRIM="#f0c830")}),
    "queen_bee_bug": dict(
        build="curvy", outfit=dict(top=["corset", "capelet"], bottom="wrap_skirt", extras=["pearls"], colors=dict(cloth2="#2a2420")), hair="drills", bangs="parted",
        ears="antenna", legs="stockings", foot="shoe", wings="insect", wing_scale=1.3, flies=True, gaster=True, collar=True, crown=True, four_arms=True,
        pal=dict(SKIN=skin("peach"), HAIR="#e8a820", FUR="#f8c830", LIGHT="#fff0a8", MARK="#2a2420", EYE="#c87a1a", CLOTH="#f8c830", TRIM="#2a2420",
                 extra=dict(antenna="#2a2420", wing="#f0f4f8", wing_vein="#8a8a90", stocking="#2a2420", boot="#2a2420")),
        variants={"amber": dict(HAIR="#c86a1a", FUR="#e8902a", MARK="#3a1a0a", CLOTH="#e8902a"),
                  "ebony": dict(HAIR="#2a2a32", FUR="#3a3a44", MARK="#e8c040", LIGHT="#6a6a7a", CLOTH="#2a2a32"),
                  "orchid": dict(HAIR="#2a9a6a", FUR="#3ab878", MARK="#1a5a3a", CLOTH="#2a8a5a"),
                  "crimson": dict(HAIR="#a81a2a", FUR="#c83a3a", MARK="#2a0a0e", CLOTH="#a81a2a")}),
    "papillon_bug": dict(
        build="slim", outfit=dict(top=["halter"], bottom="sarong", extras=["armlets"], colors=dict(cloth2="#2a2420")), hair="flowing", bangs="swept", ears="antenna",
        legs="bare", foot="shoe", wings="butterfly", flies=True,
        pal=dict(SKIN=skin("fair"), HAIR="#e8782a", FUR="#e8782a", LIGHT="#f8c88a", MARK="#1e1a18", EYE="#2a1e14", CLOTH="#f8a830", TRIM="#1e1a18",
                 extra=dict(antenna="#1e1a18", wing="#f08a2a", wing_vein="#1e1a18", wing_alpha=245, wing_pattern="border", boot="#1e1a18")),
        variants={"morpho": dict(HAIR="#3a6ae8", CLOTH="#2a4ab8", extra=dict(antenna="#141418", wing="#3a8af8", wing_vein="#141418", wing_alpha=245, wing_pattern="border", boot="#141418")),
                  "swallowtail": dict(HAIR="#f0d850", CLOTH="#f0d850", extra=dict(antenna="#141418", wing="#f8e060", wing_vein="#141418", wing_alpha=245, wing_pattern="bands", boot="#141418")),
                  "cabbage": dict(HAIR="#f4f2ea", CLOTH="#f4f2ea", extra=dict(antenna="#3a3a3a", wing="#f8f8f0", wing_vein="#3a3a3a", wing_alpha=240, wing_pattern="eyespot", eyespot="#2a2a2a", boot="#3a3a3a")),
                  "emerald": dict(HAIR="#3ab86a", CLOTH="#2a8a5a", extra=dict(antenna="#141418", wing="#4ad88a", wing_vein="#141418", wing_alpha=245, wing_pattern="border", boot="#141418"))}),
}
LARVAE = {
    "moth_larva_bug": dict(
        build="petite", outfit=dict(top=["sweater"], colors=dict()), hair="bob", bangs="blunt", ears="antenna", body="larva", fuzzy=True,
        pal=dict(SKIN=skin("fair"), HAIR="#a8d878", FUR="#98c860", LIGHT="#d8f0a8", MARK="#4a7a2a", EYE="#2a2a2a", CLOTH="#c8e898", TRIM="#e8d870",
                 extra=dict(antenna="#4a6a2a")),
        variants={"brown": dict(FUR="#8a6a4a", HAIR="#a8885a", MARK="#4a3420", LIGHT="#c8a880", CLOTH="#c8a880"),
                  "pale": dict(FUR="#d0e8b8", HAIR="#e0f0c8", MARK="#8aa86a", CLOTH="#f0f8e0"),
                  "spotted": dict(FUR="#98c860", MARK="#f0d040", pattern="spots"),
                  "orange": dict(FUR="#e8902a", HAIR="#f0a84a", MARK="#7a3a10", LIGHT="#f8d0a0", CLOTH="#f8c890")}),
    "papillon_larva_bug": dict(
        build="petite", outfit=dict(top=["sweater"], colors=dict()), hair="bob", bangs="blunt", ears="antenna", body="larva",
        pal=dict(SKIN=skin("fair"), HAIR="#f0e040", FUR="#f0e040", LIGHT="#ffffff", MARK="#141414", EYE="#2a2a2a", CLOTH="#f8f0a0", TRIM="#141414",
                 pattern="stripes", extra=dict(antenna="#141414")),
        variants={"green": dict(FUR="#7ac850", HAIR="#9ad870", MARK="#3a6a2a", CLOTH="#c8f0a8"),
                  "swallowtail": dict(FUR="#5ab84a", HAIR="#7ac860", MARK="#f08a2a", pattern="spots", CLOTH="#d8f0c0"),
                  "blue": dict(FUR="#7a9ab8", HAIR="#9ab8d8", MARK="#2a3a5a", CLOTH="#d0e0f0"),
                  "red": dict(FUR="#c84a3a", HAIR="#e86a5a", MARK="#3a0e0a", CLOTH="#f8c0b8")}),
}
SPECIES.update(LARVAE)
ALL = SPECIES
BIPEDS = {k: v for k, v in SPECIES.items() if v.get("body") is None}  # noqa: E305


def declare_hook(sp, d):
    out = []
    if d.get("cap_kit"):                                 # Charmy's aviator cap, its ear flaps, the goggles on its brow
        out += [("acap", 9.25, 3, 9.25), ("acap_flap", .75, 3, 2.5)] + [("goggle_band", 9.25, 1, 9.25), ("goggle_lens", 2.25, 2, .75)]
    if d.get("proboscis"):
        out += [("proboscis", .5, .5, 4)]
    if d.get("collar"):
        out += [("ruff", 5.5, 1.75, 4.25)]
    if d.get("gaster"):
        out += [("gaster", 5, 5, 6.5), ("stinger", .5, .5, 1.5)]
    if d.get("crown"):
        out += [("crown", 4, 1.5, 4), ("jewel", 1, 1, .5)]
    if d.get("dates"):                                   # a cluster of dates and two palm leaflets by her ear
        out += [("date", 1, 1.25, 1), ("dleaf", 1, .25, 3.5)]
    if d.get("halo"):                                    # the temple mantis' gold ring arch, hung with jewelled tassels
        out += [("arch_bar", 13, .75, .5), ("arch_post", .75, 14, .5), ("arch_ring", 6, 6, .25), ("drop", .5, 2.5, .5)]
    if d.get("flower"):
        out += [("petal", 2, 2, .5)]
    if d.get("lantern"):                                 # the firefly's glowing abdomen
        out += [("lantern", 3.5, 3, 4)]
    if d.get("elytra"):                                  # the scarab's wing-cases
        out += [("elytra", 3.5, 9, 1.5)]
    if d.get("scythes"):                                 # the mantis: a toothed blade along each forearm; the orchid's petal hips; the leaf's cape
        out += [("scythe", .75, 4.5, 1.5), ("petal_hip", 3.25, 2.5, .5), ("leafcape", 6, 9, .5)]
    return out


def paint_hook(pt, pal, d):
    cap = pal.extra.get("cap")
    brown = G.shades("#6a4a2a")
    pt("acap", "all", lambda f, x, y, w, h: H.CLEAR if not cap or f == "down" else pick(brown, 0.45 + 0.15 * (f == "up") + 0.1 * (y == h - 1), x, y))
    pt("acap_flap", "all", lambda f, x, y, w, h: H.CLEAR if not cap else pick(brown, 0.4, x, y))
    pt("goggle_band", "all", lambda f, x, y, w, h: H.CLEAR if not cap or f in ("up", "down") else pick(brown, 0.3, x, y))
    pt("goggle_lens", "all", lambda f, x, y, w, h: H.CLEAR if not cap else pick(G.shades("#5ac8f0"), 0.7, x, y)
       if f == "north" and 0 < x < w - 1 and 0 < y < h - 1 else pick(G.shades("#c8c8cc"), 0.6, x, y))
    pt("proboscis", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.4, x, y))
    pt("ruff", "all", lambda f, x, y, w, h: pick(pal.LIGHT, 0.6 + 0.25 * (noise(x // 2, y, 4) > 0.5), x, y))
    pt("gaster", "all", lambda f, x, y, w, h: pick(pal.FUR, 0.55, x, y))
    pt("gaster", ("east", "west", "up", "down"), lambda f, x, y, w, h: pick(pal.MARK if (x // 3) % 2 == 0 else pal.FUR, 0.5, x, y))
    pt("stinger", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.3, x, y))
    gold = G.shades("#e8c040")
    pt("crown", "all", lambda f, x, y, w, h: pick(gold, 0.55 + 0.3 * (x % 2 == 0 and y == h - 1), x, y))
    date, dleaf = G.shades(pal.extra.get("date", "#6a1e1a")), G.shades(pal.extra.get("dleaf", "#6a8a4a"))
    pt("date", "all", lambda f, x, y, w, h: pick(date, 0.45 + 0.3 * (f == "up" or (x == 0 and y == 0)), x, y))
    pt("dleaf", "all", lambda f, x, y, w, h: pick(dleaf, 0.45 + 0.25 * (x == w // 2) + 0.1 * (y % 2), x, y))
    jewel = G.shades(pal.extra.get("jewel", "#e83a6a"))
    pt("jewel", "all", lambda f, x, y, w, h: pick(jewel, 0.7, x, y))
    for p in ("arch_bar", "arch_post"):
        pt(p, "all", lambda f, x, y, w, h: pick(gold, 0.5 + 0.3 * ((x + y) % 3 == 0), x, y))
    pt("arch_ring", "all", lambda f, x, y, w, h: (pick(gold, 0.6 + 0.2 * ((x + y) % 4 == 0), x, y) if 0.32 * w < ((x + 0.5 - w / 2) ** 2 + (y + 0.5 - h / 2) ** 2) ** 0.5 < 0.5 * w
                                                  else pick(jewel, 0.75, x, y) if ((x + 0.5 - w / 2) ** 2 + (y + 0.5 - h / 2) ** 2) ** 0.5 < 0.14 * w
                                                  else CLEAR))
    pt("drop", "all", lambda f, x, y, w, h: pick(jewel if y < 2 else gold, 0.6, x, y))
    pt("petal", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.6 + 0.2 * ((x + y) % 2), x, y))
    blade = G.shades(pal.extra.get("scythe", "#8ac85a"))
    pt("scythe", "all", lambda f, x, y, w, h: pick(blade, 0.45 + 0.3 * (f == "north" and y % 2 == 0), x, y))
    pt("petal_hip", "all", lambda f, x, y, w, h: pick(pal.MARK if y < 1 else pal.FUR, 0.6 + 0.15 * ((x + y) % 2), x, y)
       if pal.extra.get("petals") else CLEAR)
    leaf = G.shades("#4a8a2a")
    pt("leafcape", "all", lambda f, x, y, w, h: (pick(leaf, 0.5 + 0.25 * (x == w // 2) + 0.12 * ((x - w // 2) == (y // 2) % 3), x, y)
                                                 if abs(x + 0.5 - w / 2) < w / 2 * (1 - (y / h) ** 2) + 0.6 else CLEAR)
       if pal.extra.get("leafcape") else CLEAR)
    shell, shell2 = G.shades(pal.extra.get("shell", "#b07a30")), G.shades(pal.extra.get("shell2", "#e8c060"))
    pt("elytra", "all", lambda f, x, y, w, h: pick(shell2, 0.55, x, y) if (f in ("south", "east", "west") and (y % 5 == 0 or x == 0))
       else pick(shell, 0.4 + 0.25 * (f == "south") + (noise(x, y, 99) - 0.5) * 0.1, x, y))
    lamp = G.shades(pal.extra.get("lantern", "#f8e070"))
    pt("lantern", "all", lambda f, x, y, w, h: pick(lamp, 0.65 + 0.25 * ((x + y) % 3 == 0), x, y) if y >= h * 0.35 or f == "down"
       else pick(pal.FUR, 0.45, x, y))
    # caterpillars and the scorpion
    sc = G.chitin_fn(pal, base=0.5)
    for p in [n for n, *_ in LARVA_SEGS]:
        if d.get("body") == "larva":
            pt(p, "all", lambda f, x, y, w, h: pick(pal.MARK if (y // 2 + x // 4) % 3 == 0 and pal.pattern in ("stripes",) else pal.FUR,
                                                    0.5 + 0.2 * (f == "down") + (0.15 if d.get("fuzzy") and (x + y) % 3 == 0 else 0), x, y))
    for p in ("thorax", "abdomen", "femur", "tibia", "tarsus", "tail_s", "pincer"):
        pt(p, "all", sc)
    pt("sting", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.25 + 0.2 * (y == 0), x, y))
    veil = G.shades(pal.extra.get("veil", "#5a9ad8"))
    pt("ghood", "all", lambda f, x, y, w, h: CLEAR if f in ("down", "north") else pick(veil, 0.5 + 0.15 * (x % 4 == 0), x, y))
    pt("gveil", "all", lambda f, x, y, w, h: pick(veil, 0.55, x, y))


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    if d.get("cap_kit"):                                 # the cap snug over the top of her head, flaps over her ears, goggles on its brow
        r.add("hair_acap", "head", [0, hy + 7, z], [cube("acap", [-4.625, hy + 6.2, -4.625 + z]), cube("goggle_band", [-4.75, hy + 6.0, -4.75 + z]),
                                                    cube("goggle_lens", [-3.4, hy + 5.6, -5.3 + z]), cube("goggle_lens", [1.15, hy + 5.6, -5.3 + z]),
                                                    cube("acap_flap", [-5.0, hy + 3.4, -1.25 + z]), G.mirror_cube(cube("acap_flap", [-5.0, hy + 3.4, -1.25 + z]))])
    if d.get("proboscis"):
        r.put("head", G.rot_cube("proboscis", [-0.25, hy + 0.6, -7.8 + z], [0, hy + 0.8, -4 + z], 18, 0, 0))
    if d.get("collar"):
        r.put("body", cube("ruff", [-2.75, 18.4, -2.125 + z]))
    if d.get("gaster"):
        r.add("gaster", "body", [0, 12.5, 1.5 + z], [G.rot_cube("gaster", [-2.5, 9.5, 1.5 + z], [0, 12.5, 1.5 + z], 35, 0, 0),
                                                       G.rot_cube("stinger", [-0.25, 9.2, 7.8 + z], [0, 12.5, 1.5 + z], 35, 0, 0)])
    if d.get("crown"):
        r.put("head", cube("crown", [-2, hy + 8.6, -2 + z]), cube("jewel", [-0.5, hy + 9.0, -2.4 + z]))
    if d.get("dates"):
        cs = [G.rot_cube("dleaf", [2.8, hy + 7.4, -1.6 + z], [3.3, hy + 7.4, -1.0 + z], -20, a, -30) for a in (-20, 35)]
        cs += [cube("date", [3.4, hy + 5.6, -1.4 + z]), cube("date", [4.0, hy + 4.6, -0.9 + z]), cube("date", [3.2, hy + 4.4, -0.3 + z])]
        r.add("dates", "head", [3.3, hy + 7.0, -1.0 + z], cs)
    if d.get("halo"):                                    # behind her head; hair_ so it does not count to her height
        bz = 5.5 + z
        r.add("hair_arch", "head", [0, hy + 4, bz], [cube("arch_post", [-6.5, hy - 1.0, bz]), cube("arch_post", [5.75, hy - 1.0, bz]),
                                                     cube("arch_bar", [-6.5, hy + 13.0, bz]), cube("arch_ring", [-3, hy + 7.0, bz - 0.1]),
                                                     cube("drop", [-6.4, hy - 2.0, bz]), cube("drop", [5.9, hy - 2.0, bz]),
                                                     cube("drop", [-0.25, hy + 10.5, bz + 0.2])])
    if d.get("elytra"):                                  # two domed wing-cases closed over her back
        cd = G.S("chest")[2]
        r.add("elytra", "body", [0, 19.5, cd / 2 + z], [G.rot_cube("elytra", [-3.5, 10.8, cd / 2 + 0.2 + z], [-1.75, 19.5, cd / 2 + z], 8, 0, 4),
                                                        G.rot_cube("elytra", [0, 10.8, cd / 2 + 0.2 + z], [1.75, 19.5, cd / 2 + z], 8, 0, -4)])
    if d.get("lantern"):                                 # her lantern, under her wings at the small of her back
        r.add("lantern", "body", [0, 12.5, 1.5 + z], [G.rot_cube("lantern", [-1.75, 9.5, 1.2 + z], [0, 12.5, 1.5 + z], 30, 0, 0)])
    if d.get("scythes"):
        px, py, pz = r.b["forearm_r"]["pivot"]
        r.put("forearm_r", G.rot_cube("scythe", [px - 0.375, py - 4.0, pz - 1.9], [px, py, pz], -12, 0, 0))
        r.put("body", cube("petal_hip", [-4.6, 10.75, -1.0 + z]), H.mirror_cube(cube("petal_hip", [-4.6, 10.75, -1.0 + z])))
        r.put("body", cube("leafcape", [-3.0, 10.5, 2.2 + z]))
    if d.get("flower"):
        r.put("head", G.rot_cube("petal", [3.0, hy + 6.5, -1.0 + z], [4.0, hy + 7.5, z], 0, 0, 30), G.rot_cube("petal", [3.3, hy + 6.0, -0.5 + z], [4.0, hy + 7.0, z], 0, 0, -20))
    if d.get("four_arms"):                               # a second, lower pair of arms
        cw = G.S("chest")[0]
        a = G.S("arm_up")[0]
        x0 = -(max(cw, G.S("hips")[0]) / 2 + 0.25 + a)
        ay = 17.0
        r.add("arm2_r", "body", [x0 + a / 2, ay, z], [cube("arm_up", [x0, ay - 3.5, -a / 2 + z])], mirror=True)
        r.add("forearm2_r", "arm2_r", [x0 + a / 2, ay - 3.5, z], [cube("forearm", [x0, ay - 6.5, -a / 2 + z])], mirror=True)
        r.add("hand2_r", "forearm2_r", [x0 + a / 2, ay - 6.5, z], [cube("palm", [x0, ay - 8, -0.5 + z]), cube("fingers", [x0, ay - 9, -0.5 + z])], mirror=True)


def bip_extra(an, sp, d):
    """drinking blood ("drink"), pollinating ("pollinate"), healing magic ("heal"), the four-armed queen's arms"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("drink", 2.0, True, merge({"head": {"rotation": hold(24, 0, 0)}, "body": {"rotation": hold(20, 0, 0)}}, sym("arm", hold(-60, 0, 10))))
    an.add("pollinate", 2.0, True, merge({"body": {"rotation": hold(30, 0, 0)}, "head": {"rotation": Tk((0, (20, 0, 0)), (1, (30, 0, 0)), (2, (20, 0, 0)))}},
                                         sym("arm", Tk((0, (-60, 0, 10)), (1, (-80, 0, 10)), (2, (-60, 0, 10))))))
    an.add("heal", 1.5, False, merge({"head": {"rotation": hold(-10, 0, 0)}, "eyelids": {"position": hold(*H.OUT_)}},
                                     sym("arm", Tk((0, (0, 0, 0)), (0.5, (-120, 0, 30)), (1.2, (-120, 0, 30)), (1.5, (0, 0, 0))))))
    if d.get("scythes"):                                 # the prayer pose: forearms folded up before her chest, very still
        an.add("pray", 3.0, True, merge({"head": {"rotation": Tk((0, (6, 0, 0)), (1.5, (6, 14, 4)), (3, (6, 0, 0)))}, "body": {"rotation": hold(4, 0, 0)}},
                                        sym("arm", hold(-55, 0, -8)), sym("forearm", hold(-95, 0, 0))))
        an.add("strike", 0.4, False, merge({"body": {"rotation": Tk((0, (4, 0, 0)), (0.12, (24, 0, 0)), (0.4, (4, 0, 0)))}},
                                           sym("arm", Tk((0, (-55, 0, -8)), (0.12, (-110, 0, -8)), (0.4, (-55, 0, -8)))),
                                           sym("forearm", Tk((0, (-95, 0, 0)), (0.12, (-10, 0, 0)), (0.4, (-95, 0, 0))))))
    if d.get("four_arms"):
        for key in ("idle", "walk", "sit", "fly"):
            F._inject(an, key, merge(sym("arm2", hold(-20, 0, 14)), sym("forearm2", hold(-50, 0, 0))))


# ----------------------------------------------------------------------------- caterpillars and the scorpion
def larva_declare(sp, d):
    G.declare(list(LARVA_SEGS), ears=d["ears"], hair=d["hair"], build=d["build"], outfit=d["outfit"])


def larva_build(sp, d):
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    G.upper(r, 6.5, dict(hair=d["hair"], bangs=d.get("bangs"), build=d["build"], outfit=d["outfit"], ears=d["ears"], skirt=False, cuffs=False))
    G.serpent(r, None, dict(d))
    # the serpent rig puts seg_1 from 12 down; lower the whole tail so the waist sits on her short body
    for n in [f"seg_{i}" for i in range(1, 9)]:
        b = r.b[n]
        b["pivot"][1] -= 5.5
        for c in b["cubes"]:
            c["origin"][1] -= 5.5
    return r.model(sp)


def larva_anims(an):
    segs = [f"seg_{i}" for i in range(1, 9)]
    G.serpent_anims(an, segs)
    Tk, hold = H.track, H.hold
    an.add("eat", 1.0, True, H.merge({"head": {"rotation": Tk((0, (20, 0, 0)), (0.5, (30, 0, 0)), (1.0, (20, 0, 0)))},
                                      "mouth": {"position": Tk((0, H.REST), (0.25, H.OUT_), (0.5, H.REST), (0.75, H.OUT_), (1.0, H.REST))}},
                                     H.sym("arm", hold(-50, 0, 0))))


def scorpion_declare(sp, d):
    parts = [("thorax", 6, 4.75, 6), ("abdomen", 7, 5, 8), ("femur", 6, 1.25, 1.25), ("tibia", 1, 10, 1), ("tarsus", .75, 3, .75),
             ("tail_s", 2, 3.5, 2), ("sting", 1.25, 2.5, 1.25), ("pincer", 1.75, 2.5, 2.5), ("young", 2, 2, 2.5)]
    if d.get("veil"):
        parts += [("ghood", 9.5, 4, 9.5), ("gveil", 6.5, 2.5, .25)]
    G.declare(parts, ears=d["ears"], hair=d["hair"], build=d["build"], outfit=d["outfit"])


def scorpion_build(sp, d):
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), build=d["build"], outfit=d["outfit"], ears=None, skirt=False))
    G.spider(r, None, d)
    names = P.chain(r, "tail_seg", "abdomen", (0, 9.5, 10.5), ["tail_s"] * 5, rx=200, ry=0, bend=-32)
    last = names[-1]
    px, py, pz = r.b[last]["pivot"]
    r.add("stinger", last, [px, py, pz], [cube("sting", [px - 0.625, py - 0.5, pz - 2.0])])
    for side in ("r", "l"):
        hb = "hand_" + side
        if hb in r.b:
            hx = r.b[hb]["pivot"][0]
            r.put(hb, cube("pincer", [hx - 0.875, 19.5 - 10.5, -1.6]))
    r.add("brood", "abdomen", [0, 11.5, 6], [cube("young", [x, 11.2, z]) for x, z in ((-2.5, 3.5), (0.6, 5.0), (-1.0, 7.5))])
    if d.get("veil"):                                    # a hood over her hair, a veil over her mouth and nose
        hx, hy, hz = r.b["head"]["pivot"]
        r.add("hair_ghood", "head", [0, hy + 7, hz], [cube("ghood", [-4.75, hy + 5.0, hz - 4.75]), cube("gveil", [-3.25, hy + 0.25, hz - 4.35])])
    return r.model(sp)


def scorpion_anims(an):
    G.spider_anims(an)
    tails = ["tail_seg"] + [f"tail_seg_{c}" for c in "bcde"]
    F._inject(an, "idle", G.wave(tails, 3.0, [2, 3, 3, 4, 4], axis=0))
    Tk = H.track
    an.add("sting", 0.8, False, {t: {"rotation": Tk((0, (0, 0, 0)), (0.3, (-25, 0, 0)), (0.5, (30, 0, 0)), (0.8, (0, 0, 0)))} for t in tails})


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.25, -30, 8), ("fly", 0.3, -60, 5), ("sit", 0, -30, 10), ("idle", 0, -150, 15)]


# twenty-eighth wave: the pixi, a tiny blue fairy in a plain sundress (the fairy's rig; too small to share a bed, like the fairy); the rlff, a tall golden spirit
# of forest streams on the Fairy Queen's rig, in a long gold gown
SPECIES["pixi_bug"] = dict(
    build="petite", outfit=dict(top=["halter"], bottom="wrap_skirt", colors=dict(cloth2="#a8d0f4")), hair="bob", bangs="swept", ears="elf",
    legs="bare", foot="shoe", wings="fairy", flies=True,
    pal=dict(SKIN=skin("pale"), HAIR="#a8d0f4", FUR="#a8d0f4", LIGHT="#ffffff", MARK="#6a9ad8", EYE="#4a7ac8", CLOTH="#a8d0f4", TRIM="#d8ecfc",
             extra=dict(wing="#e0f0fc", wing_vein="#a8d0f4", wing_alpha=120, boot="#a8d0f4")),
    variants={"candy": dict(HAIR="#f8b0d0", CLOTH="#f8c8e0", extra=dict(wing="#fce0ec", wing_vein="#f8a8c8", wing_alpha=120, boot="#f8a8c8")),
              "mint": dict(HAIR="#a8ecc8", CLOTH="#c8f4dc", extra=dict(wing="#dcf8e8", wing_vein="#8ad8a8", wing_alpha=120, boot="#8ad8a8")),
              "lemon": dict(HAIR="#f8e890", CLOTH="#fcf0b0", extra=dict(wing="#fcf8d8", wing_vein="#e8c850", wing_alpha=120, boot="#e8c850")),
              "dusk": dict(HAIR="#c0a8f0", CLOTH="#d0c0f4", extra=dict(wing="#e4dcf8", wing_vein="#a890e0", wing_alpha=120, boot="#a890e0"))})
SPECIES["rlff_bug"] = dict(
    build="curvy", outfit=dict(top=["kimono"], bottom="hakama", extras=["pearls"], colors=dict(kimono="#e8d090", kimono_trim="#f8e8b0", obi="#c8a040", cloth2="#e8d090")),
    hair="flowing", bangs="parted", ears="elf", legs="bare", foot="shoe", wings="butterfly", wing_scale=1.2, flies=True,
    pal=dict(SKIN=skin("fair"), HAIR="#e8c878", FUR="#e8c878", LIGHT="#fff4d0", MARK="#b08a3a", EYE="#6a8a3a", CLOTH="#e8d090", TRIM="#f8e8b0",
             extra=dict(wing="#f4e4b0", wing_vein="#c8a040", wing_alpha=130, wing_pattern="border", boot="#c8a040")),
    variants={"moss": dict(HAIR="#8ac878", extra=dict(wing="#d0ecc0", wing_vein="#5a9a4a", wing_alpha=130, wing_pattern="border", kimono="#c8e4b0", boot="#5a9a4a")),
              "brook": dict(HAIR="#8ac8e8", extra=dict(wing="#c8e8f8", wing_vein="#4a90c0", wing_alpha=130, wing_pattern="border", kimono="#c8e4f4", boot="#4a90c0")),
              "autumn": dict(HAIR="#d8783a", extra=dict(wing="#f4c890", wing_vein="#a84a1e", wing_alpha=130, wing_pattern="border", kimono="#f0c8a0", boot="#a84a1e")),
              "pale": dict(HAIR="#f0f0e8", extra=dict(wing="#ffffff", wing_vein="#c8c8b8", wing_alpha=130, wing_pattern="border", kimono="#f4f4ec", boot="#c8c8b8"))})


BIPEDS.update({k: SPECIES[k] for k in ("pixi_bug", "rlff_bug")})


# homage looks of the twenty-ninth wave (POP_CULTURE_VARIANTS.md)
SPECIES["hornet_bug"]["variants"]["needle_knight"] = dict(HAIR="#e8e4dc", FUR="#c82a2a", LIGHT="#e8e4dc", MARK="#6a1414", EYE="#141418", CLOTH="#c82a2a", TRIM="#e8e4dc")


if __name__ == "__main__":
    import family_concepts as FC
    want = [a for a in sys.argv[1:] if not a.startswith("--")]
    prev = "--previews" in sys.argv
    heights = {s["id"]: s["height"] for s in FC.family("bug")["species"]}
    entries = {}

    def custom(sp, decl, bld, anm):
        d = ALL[sp]
        decl(sp, d)
        tex = F.paint(sp, d, F.pal_for(d), paint_hook)
        geo, adoc, texes = G.write(sp, tex, bld(sp, d), anm, list(d["variants"]), lambda k: F.paint(sp, d, F.pal_for(d, k), paint_hook))
        h = G.model_height(geo)
        if prev:
            G.previews(sp, geo, adoc, texes, F.VIEWS, F.FACE, h)
        entries[sp] = (geo, tex, adoc["animations"][f"animation.{sp}.idle"], heights[sp], h)

    for sp in LARVAE:
        if not want or sp in want:
            custom(sp, larva_declare, larva_build, larva_anims)
    for sp in ("scorpion_bug", "girtablilu_bug"):
        if not want or sp in want:
            custom(sp, scorpion_declare, scorpion_build, scorpion_anims)
    for e, sp in zip(F.run_family("bug", BIPEDS, declare_hook, paint_hook, build_hook, bip_extra, views=VIEWS, only=[k for k in BIPEDS if not want or k in want]),
                     [s["id"] for s in FC.family("bug")["species"] if s["id"] in BIPEDS and (not want or s["id"] in want)]):
        entries[sp] = e
    if prev and not want:
        G.roster(os.path.join(G.PREVIEWS, "bug_roster.png"), [entries[s["id"]] for s in FC.family("bug")["species"]])
