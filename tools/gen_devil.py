#!/usr/bin/env python3
"""Devils (the infernal family, see devil_field_guide.html): lesser devil, succubus, greater devil, jack o'lantern, jiang shi.

    python tools/gen_devil.py [species ...] [--previews]

girl_family bipeds. The three devils have horns, membrane wings and a spade-tipped tail and fly. The jack o'lantern wears
a carved pumpkin as a hat (its face glows: glow mask) and has a `disguise` bone, a whole pumpkin on the ground that the
game shows instead of her while she hides. The jiang shi wears a Qing official's hat with a paper talisman over her face
and holds her arms out in front of her ("hop").
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

SPADE = [(.75, .75, 4), (.5, .5, 4), (2, 2, .5)]
SPADE_REST = [(-20, 0, 0), (25, 0, 0), (10, 0, 0)]

CLUB_TILT = 78

SPECIES = {
    # twenty-third wave: the ogre, big and green-skinned with long pointed ears, two little horns and a mop of light-blue hair, a
    # simple blue dress; a club in her right hand
    "ogre_devil": dict(
        build="strong", outfit=dict(top=["halter"], bottom="sarong", extras=["belt", "arm_wraps"], colors=dict(cloth2="#5a8ac8", leather="#6a4a2a")),
        hair="shaggy", bangs="parted", ears="elf", legs="bare", foot="shoe", horns="devil", club=True,
        pal=dict(SKIN="#7ab05a", HAIR="#9ab8e8", FUR="#7ab05a", LIGHT="#b8d8a0", MARK="#3a6a2a", EYE="#2a4a8a", CLOTH="#7aa8e0", TRIM="#e8e0c8",
                 extra=dict(horn="#e8e0c8", boot="#6a4a2a")),
        variants={"grey": dict(SKIN="#8a9a8a", FUR="#8a9a8a", LIGHT="#c0ccc0", MARK="#4a5a4a", HAIR="#3a3a40", CLOTH="#a8803a"),
                  "brown": dict(SKIN="#a8805a", FUR="#a8805a", LIGHT="#d8b890", MARK="#5a3a1e", HAIR="#3a2a1a", CLOTH="#5a7a3a"),
                  "blue": dict(SKIN="#6a8ac0", FUR="#6a8ac0", LIGHT="#a8c0e8", MARK="#2a3a6a", HAIR="#e8e4d8", CLOTH="#c84a3a"),
                  "red": dict(SKIN="#c8705a", FUR="#c8705a", LIGHT="#f0b098", MARK="#6a2a1e", HAIR="#2a2228", CLOTH="#3a5a8a")}),
    # twenty-third wave (folklore): the taotie, the glutton of the Chinese bronzes: curled horns, bronze glyph-work running over
    # her arms and legs, and a huge grin
    # twenty-fourth wave: the Behemoth, huge and dark blue, great horns, a wild mane and a thick tail
    "behemoth_devil": dict(
        build="strong", outfit=dict(top=["bandeau"], bottom="loincloth", colors=dict(cloth2="#1a2a5a", leather="#1a2a5a")),
        hair="mane", bangs="messy", ears="elf", legs="bare", foot="shoe", horns="greater", tail=[(3.5, 3.5, 4), (2.75, 2.75, 4), (2, 2, 3.5)], tail_look="scale",
        pal=dict(SKIN=skin("#3a5aa8"), HAIR="#1a2a6a", FUR="#2a448a", LIGHT="#5a7ac8", MARK="#14204a", EYE="#5ad0d8", CLOTH="#1a2a5a", TRIM="#5a7ac8",
                 extra=dict(horn="#1a2a5a", boot="#14204a")),
        variants={"grey": dict(SKIN=skin("#6a6e78"), HAIR="#2e3036", FUR="#4a4c54", LIGHT="#8a8e98", MARK="#1e2024", CLOTH="#2e3036", extra=dict(horn="#1e2024", boot="#1e2024")),
                  "brown": dict(SKIN=skin("#7a5a3a"), HAIR="#3a2614", FUR="#5a3e24", LIGHT="#a8845a", MARK="#2a1a0e", CLOTH="#3a2614", extra=dict(horn="#2a1a0e", boot="#2a1a0e")),
                  "green": dict(SKIN=skin("#3a7a4a"), HAIR="#143a1e", FUR="#2a5a34", LIGHT="#6aa87a", MARK="#0e2a14", CLOTH="#143a1e", extra=dict(horn="#0e2a14", boot="#0e2a14")),
                  "red": dict(SKIN=skin("#a83a2a"), HAIR="#4a0e0a", FUR="#7a1e14", LIGHT="#d86a4a", MARK="#2a0806", CLOTH="#4a0e0a", extra=dict(horn="#2a0806", boot="#2a0806"))}),
    "taotie_devil": dict(
        build="curvy", outfit=dict(top=["bandeau", "mantle"], bottom="sarong", extras=["armlets", "belt"], colors=dict(cloth2="#c8a040", leather="#6a3a1e")),
        hair="hime", bangs="blunt", ears="elf", legs="bare", foot="shoe", horns="ram", grin=True, glyphs=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e3a3a", FUR="#2a5a54", LIGHT="#a8d8c8", MARK="#0e2a26", EYE="#e8a030", CLOTH="#2a6a5a", TRIM="#c8a040",
                 extra=dict(horn="#c8903a", boot="#2a1e14", glyph="#3a8a7a")),
        variants={"bronze": dict(HAIR="#5a3a1e", CLOTH="#8a5a2a", EYE="#3ad8b8", extra=dict(horn="#e8b860", boot="#2a1e14", glyph="#b8783a")),
                  "red": dict(HAIR="#5a0e14", CLOTH="#a82a2a", extra=dict(horn="#e8c060", boot="#2a1e14", glyph="#c8402a")),
                  "black": dict(HAIR="#141214", CLOTH="#2a2628", EYE="#e83a3a", extra=dict(horn="#a8a094", boot="#141214", glyph="#5a5248")),
                  "gold": dict(HAIR="#3a2a14", CLOTH="#c8a040", TRIM="#2a6a5a", extra=dict(horn="#f0d080", boot="#3a2a14", glyph="#d8a830"))}),
    # twenty-second wave (folklore): the cyclops, the one-eyed giants of Greek myth: a big shy girl with a single great eye, pointed
    # ears, a blue bob and a teal tunic
    "cyclops_devil": dict(
        build="strong", outfit=dict(top=["halter", "mantle"], bottom="sarong", extras=["belt"], colors=dict(cloth2="#3a8a8a", leather="#6a4a2a")),
        hair="bob", bangs="parted", ears="elf", legs="bare", foot="shoe", cyclops=True,
        pal=dict(SKIN=skin("fair"), HAIR="#3a6ac8", FUR="#3a6ac8", LIGHT="#e8f0fc", MARK="#1e3a7a", EYE="#3a7ad8", CLOTH="#3a9a9a", TRIM="#e8e0c8",
                 extra=dict(boot="#6a4a2a")),
        variants={"brown": dict(HAIR="#6a4a2a", EYE="#8a5a2a", CLOTH="#8a6a3a"),
                  "red": dict(HAIR="#a8301e", EYE="#c83a2a", CLOTH="#e8d8b8"),
                  "grey": dict(HAIR="#8a8e96", EYE="#5a6a7a", CLOTH="#4a5a6a"),
                  "green": dict(HAIR="#3a7a3a", EYE="#4a9a3a", CLOTH="#a8803a")}),
    # twenty-second wave (folklore): the genie, a spirit of smoke bound to a brass lamp: a smoke tail for legs, a high ponytail, a
    # bandeau and bangles, the lamp in her hand; she floats
    "genie_devil": dict(
        build="curvy", outfit=dict(top=["bandeau"], extras=["armlets", "pearls"], colors=dict(cloth2="#e8c040")),
        hair="ponytail", bangs="swept", ears="elf", lower="none", smoke=True, lamp=True,
        pal=dict(SKIN=skin("fair"), HAIR="#3a5ab8", FUR="#7a9ae0", LIGHT="#e8f0fc", MARK="#2a4a9a", EYE="#3a5ab8", CLOTH="#4a6ac8", TRIM="#e8c040",
                 extra=dict(smoke="#8aa8e8", smoke_dark="#4a6ac8", brass="#d8a840")),
        variants={"purple": dict(HAIR="#5a2a8a", MARK="#3a1a6a", CLOTH="#6a3aa8", EYE="#8a4ac8", extra=dict(smoke="#b08ae0", smoke_dark="#6a3aa8", brass="#d8a840")),
                  "green": dict(HAIR="#2a6a4a", MARK="#1e4a32", CLOTH="#3a8a5a", EYE="#3a9a5a", extra=dict(smoke="#8ad0a8", smoke_dark="#3a8a5a", brass="#d8a840")),
                  "red": dict(HAIR="#8a1e1e", MARK="#5a1414", CLOTH="#a82a2a", EYE="#e8a030", extra=dict(smoke="#e08a7a", smoke_dark="#a82a2a", brass="#d8a840")),
                  "gold": dict(HAIR="#c8901e", MARK="#7a5410", CLOTH="#e8b030", EYE="#3a2a1a", extra=dict(smoke="#f0d890", smoke_dark="#c89a30", brass="#f0c860")),
                  # homage (2026-10-30): the Genie of the animated Aladdin: blue all over, a black top-knot and goatee, gold cuffs, a red sash
                  "lamp_friend": dict(SKIN=skin("#3a8ae8"), FUR="#3a8ae8", HAIR="#141418", LIGHT="#a8d0f8", MARK="#1e5ab8", EYE="#141418", CLOTH="#c82a2a", TRIM="#e8c040", extra=dict(smoke="#3a8ae8", smoke_dark="#1e5ab8", brass="#d8a840", goatee=True))}),
    # nineteenth wave: the akaname, the filth-licker of the bathhouses: blue hair, a simple dress, a long tongue out
    "akaname_devil": dict(
        build="petite", outfit=dict(top=["sweater"], bottom="wrap_skirt", extras=["belt"], colors=dict(cloth2="#3a5ab8", leather="#2a3a7a")),
        hair="wavy", bangs="parted", ears="elf", legs="bare", foot="shoe", tongue=True,
        pal=dict(SKIN=skin("fair"), HAIR="#3a6ad8", FUR="#3a6ad8", LIGHT="#c8d8f8", MARK="#1e3a8a", EYE="#2a4ab0", CLOTH="#3a5ab8", TRIM="#a8c0f0",
                 extra=dict(boot="#2a3a7a", tongue="#5a8af0")),
        variants={"red": dict(SKIN=skin("#e0786a"), HAIR="#2a1e1e", FUR="#2a1e1e", EYE="#f8d040", CLOTH="#5a3a2a", TRIM="#e8c060",
                              extra=dict(boot="#3a2418", tongue="#d84a5a", cloth2="#5a3a2a")),
                  "green": dict(HAIR="#3a9a5a", FUR="#3a9a5a", EYE="#2a6a3a", CLOTH="#3a8a5a", TRIM="#c8f0c8",
                                extra=dict(boot="#1e4a2a", tongue="#e86a8a", cloth2="#3a8a5a")),
                  "purple": dict(HAIR="#7a4ab8", FUR="#7a4ab8", EYE="#5a2a8a", CLOTH="#6a3aa8", TRIM="#e0c8f8",
                                 extra=dict(boot="#3a1e5a", tongue="#a86af0", cloth2="#6a3aa8")),
                  "pink": dict(HAIR="#f08ab8", FUR="#f08ab8", EYE="#a83a6a", CLOTH="#f0a0c0", TRIM="#fff0f6",
                               extra=dict(boot="#a83a6a", tongue="#e84a7a", cloth2="#f0a0c0"))}),
    # sixteenth wave: the kikimora, a little house spirit in a sarafan and a knotted headscarf
    "kikimora_devil": dict(
        build="petite", outfit=dict(top=["sweater"], bottom="wrap_skirt", extras=["belt"], colors=dict(cloth2="#c83a2a")),
        hair="braid", bangs="parted", ears=None, legs="stockings", foot="shoe", kerchief=True,
        pal=dict(SKIN=skin("fair"), HAIR="#8a6a3a", FUR="#8a6a3a", LIGHT="#f4f0e4", MARK="#3a2a1a", EYE="#4a6a3a", CLOTH="#f4f0e4", TRIM="#e8c040",
                 extra=dict(kerchief="#c83a2a", kerchief_dot="#f4f0e4", stocking="#f4f0e4", boot="#5a3a22")),
        variants={"blue": dict(extra=dict(cloth2="#2a4a8a", kerchief="#2a4a8a")),
                  "green": dict(HAIR="#5a4a2a", extra=dict(cloth2="#3a6a3a", kerchief="#3a6a3a", kerchief_dot="#e8c040")),
                  "black": dict(HAIR="#2a2420", extra=dict(cloth2="#1e1c22", kerchief="#1e1c22", kerchief_dot="#c83a2a")),
                  "white": dict(HAIR="#c8b890", extra=dict(cloth2="#f0ece4", kerchief="#f0ece4", kerchief_dot="#2a4a8a"))}),
    "lesser_devil": dict(
        build="petite", outfit=dict(top=["sports"], bottom="shorts", extras=["belt"]), hair="twintails", bangs="messy", ears="elf",
        legs="bare", foot="shoe", tail=SPADE, tail_look="spade", tail_rest=SPADE_REST, horns="devil", wings="devil_small", flies=True,
        pal=dict(SKIN="#e87a6a", HAIR="#2a1418", FUR="#e87a6a", LIGHT="#f8c0b0", MARK="#3a0e14", EYE="#f8d83a", CLOTH="#2a1418", TRIM="#f8d83a",
                 pupil=(0.9, 3.4), extra=dict(horn="#2a1418", wing="#8a1a2a", wing_bone="#3a0e14", tail_col="#e87a6a", boot="#2a1418")),
        variants={"purple": dict(SKIN="#a87ad8", FUR="#a87ad8", extra=dict(horn="#2a1430", wing="#5a2a8a", wing_bone="#2a1440", tail_col="#a87ad8")),
                  "blue": dict(SKIN="#6a9ae8", FUR="#6a9ae8", extra=dict(horn="#141e3a", wing="#2a3a8a", wing_bone="#141e3a", tail_col="#6a9ae8")),
                  "pink": dict(SKIN="#f4a8c8", FUR="#f4a8c8", HAIR="#f8f0f4", extra=dict(horn="#a83a6a", wing="#d85a8a", wing_bone="#7a1a4a", tail_col="#f4a8c8")),
                  "black": dict(SKIN="#4a4248", FUR="#4a4248", HAIR="#c82a3a", extra=dict(horn="#c82a3a", wing="#2a2428", wing_bone="#0e0c0e", tail_col="#4a4248"))}),
    "succubus_devil": dict(
        build="curvy", outfit=dict(top=["corset", "halter"], bottom="wrap_skirt", extras=["pearls"], colors=dict(cloth2="#1a0e14")),
        hair="wavy", bangs="swept", ears="elf", legs="stockings", foot="shoe", tail=SPADE, tail_look="spade", tail_rest=SPADE_REST,
        horns="ram", wings="bat", flies=True,
        pal=dict(SKIN=skin("pale"), HAIR="#8a1a3a", FUR="#8a1a3a", LIGHT="#f8d0d8", MARK="#3a0a18", EYE="#e83a7a", CLOTH="#1a0e14", TRIM="#e83a7a",
                 pupil=(1.6, 2.4), extra=dict(horn="#1a0e14", wing="#8a1a3a", wing_bone="#2a0a14", tail_col="#1a0e14", stocking="#1a0e14", boot="#1a0e14")),
        variants={"violet": dict(SKIN=skin("lilac"), HAIR="#4a2a6a", extra=dict(horn="#1a0e24", wing="#5a2a8a", wing_bone="#1a0e24", tail_col="#1a0e24", stocking="#1a0e24", boot="#1a0e24")),
                  "rose": dict(HAIR="#f0a0b8", CLOTH="#c83a6a", extra=dict(horn="#f8e0e8", wing="#e87aa0", wing_bone="#8a2a4a", tail_col="#c83a6a", stocking="#c83a6a", boot="#c83a6a")),
                  "night": dict(HAIR="#e8e8f0", CLOTH="#141418", TRIM="#c8c8d8", extra=dict(horn="#c8c8d8", wing="#2a2a34", wing_bone="#0e0e12", tail_col="#141418", stocking="#141418", boot="#141418")),
                  "azure": dict(SKIN=skin("blue"), HAIR="#1a2a5a", extra=dict(horn="#0e1430", wing="#2a4a9a", wing_bone="#0e1430", tail_col="#0e1430", stocking="#0e1430", boot="#0e1430"))}),
    "greater_devil": dict(
        build="strong", outfit=dict(top=["harness", "bandeau"], bottom="loincloth", extras=["pauldron", "bracers"], colors=dict(cloth2="#1a0a0a")),
        hair="mane", bangs="jagged", ears="elf", legs="scales", foot="hoof", tail=[(1, 1, 5), (.75, .75, 5), (2.5, 2.5, .5)], tail_look="spade",
        tail_rest=SPADE_REST, horns="greater", wings="bat", wing_scale=1.45, flies=True,
        pal=dict(SKIN="#c84a3a", HAIR="#1a0e0e", FUR="#3a1a14", LIGHT="#e88a6a", MARK="#1a0a0a", EYE="#f8e83a", CLOTH="#1a0a0a", TRIM="#e8b040",
                 pupil=(0.9, 3.4), extra=dict(horn="#1a1010", horn_tip="#4a3a3a", wing="#5a1a1a", wing_bone="#1a0a0a", tail_col="#c84a3a", hoof="#1a1010")),
        variants={"obsidian": dict(SKIN="#3a3238", FUR="#1a161a", extra=dict(horn="#8a2a8a", wing="#2a1a2a", wing_bone="#0e0a0e", tail_col="#3a3238", hoof="#0e0a0e")),
                  "ash": dict(SKIN="#8a8682", FUR="#4a4644", extra=dict(horn="#1a1818", wing="#3a3634", wing_bone="#1a1818", tail_col="#8a8682", hoof="#1a1818")),
                  "gold": dict(extra=dict(horn="#e8b040", horn_tip="#fff0a0", wing="#5a1a1a", wing_bone="#1a0a0a", tail_col="#c84a3a", hoof="#1a1010")),
                  "violet": dict(SKIN="#7a4aa8", FUR="#2a1a3a", extra=dict(horn="#1a0e24", wing="#3a1a5a", wing_bone="#14081e", tail_col="#7a4aa8", hoof="#14081e"))}),
    "jack_o_lantern_devil": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], bottom="wrap_skirt", colors=dict(cloth2="#1e1a1e")),
        hair="bob", bangs="blunt", ears=None, legs="stockings", foot="shoe", pumpkin=True, glow=["pumpkin_f"],
        pal=dict(SKIN=skin("fair"), HAIR="#e8782a", FUR="#e8782a", LIGHT="#f8c87a", MARK="#2a1e14", EYE="#f8a82a", CLOTH="#e8782a", TRIM="#2a1e1e",
                 extra=dict(pumpkin="#e8822a", candle="#f8d83a", stocking="#e8782a", boot="#1e1a1e")),
        variants={"white": dict(extra=dict(pumpkin="#ece4d8", candle="#f8d83a", stocking="#ece4d8", boot="#1e1a1e"), HAIR="#f4ece0", CLOTH="#d8d0c4"),
                  "green": dict(extra=dict(pumpkin="#6a8a3a", candle="#c8f04a", stocking="#6a8a3a", boot="#1e1a1e"), HAIR="#4a6a2a", CLOTH="#4a6a2a"),
                  "blue": dict(extra=dict(pumpkin="#e8822a", candle="#4ae8f8", stocking="#2a8aa8", boot="#1e1a1e"), EYE="#4ae8f8", CLOTH="#2a5a7a"),
                  "black": dict(extra=dict(pumpkin="#2a2628", candle="#f8582a", stocking="#2a2628", boot="#0e0c0e"), HAIR="#1e1a1e", CLOTH="#2a2628")}),
    "jiang_shi_devil": dict(
        build="slim", outfit=dict(top=["kimono"], bottom="hakama", colors=dict(kimono="#2a4a8a", kimono_trim="#e8c040", obi="#e8c040", cloth2="#2a3a6a")),
        hair="odango", bangs="blunt", ears=None, legs="bare", foot="shoe", qing=True,
        pal=dict(SKIN="#c8d8e0", HAIR="#1a1a22", FUR="#c8d8e0", LIGHT="#ffffff", MARK="#8aa0b0", EYE="#c83a3a", CLOTH="#2a4a8a", TRIM="#e8c040",
                 extra=dict(boot="#1a1a22", hat="#1a1a22")),
        variants={"red": dict(extra=dict(kimono="#a81a2a", obi="#e8c040", kimono_trim="#e8c040", cloth2="#7a1a2a", hat="#1a1a22")),
                  "green": dict(extra=dict(kimono="#2a6a4a", obi="#e8c040", kimono_trim="#e8c040", cloth2="#1e4a34", hat="#1a1a22")),
                  "black": dict(extra=dict(kimono="#1a1a22", obi="#a81a2a", kimono_trim="#a81a2a", cloth2="#141418", hat="#1a1a22")),
                  "gold": dict(extra=dict(kimono="#c8a030", obi="#a81a2a", kimono_trim="#a81a2a", cloth2="#8a6a1a", hat="#1a1a22"))}),
    # fifteenth wave: the rokurokubi, a shy girl in a blue kimono whose neck stretches up long at night ("watch")
    "rokurokubi_devil": dict(
        build="slim", outfit=dict(top=["kimono"], extras=[], colors=dict(kimono="#5a7ac8", kimono_trim="#f4f2ec", obi="#2a3a7a")),
        hair="bob", bangs="blunt", ears=None, legs="bare", foot="shoe", neck=True,
        pal=dict(SKIN=skin("pale"), HAIR="#3a2a22", FUR="#3a2a22", LIGHT="#f4f2ec", MARK="#2a1e18", EYE="#6a4aa8", CLOTH="#5a7ac8", TRIM="#f4f2ec"),
        variants={"red": dict(extra=dict(kimono="#c83a3a", kimono_trim="#f4f2ec", obi="#2a2a32")),
                  "green": dict(HAIR="#1e1a18", extra=dict(kimono="#3a8a5a", kimono_trim="#f4f2ec", obi="#c8a040")),
                  "white": dict(HAIR="#141214", EYE="#3a3a3a", extra=dict(kimono="#f4f2ec", kimono_trim="#c8c4bc", obi="#a83a3a")),
                  "black": dict(HAIR="#141214", EYE="#c83a3a", extra=dict(kimono="#2a2830", kimono_trim="#8a8890", obi="#c83a3a"))}),
    # fourteenth wave: the ittan-momen, a girl who ends at the waist in a long ribbon of cotton cloth (no legs); she flies
    "ittan_momen_devil": dict(
        build="petite", outfit=dict(top=["kimono"], extras=[], colors=dict(kimono="#f4f2ec", kimono_trim="#c8c4bc", obi="#3a5ab0")),
        hair="flowing", bangs="swept", ears=None, lower="none", ribbon=True,
        pal=dict(SKIN=skin("pale"), HAIR="#f0eee8", FUR="#f0eee8", LIGHT="#ffffff", MARK="#b8b4ac", EYE="#3a5ab0", CLOTH="#f4f2ec", TRIM="#3a5ab0",
                 extra=dict(cotton="#f4f2ec", fold="#c8c4bc")),
        variants={"indigo": dict(HAIR="#2a3a7a", extra=dict(cotton="#2a3a8a", fold="#1a2458", kimono="#2a3a8a", kimono_trim="#e8e8f0", obi="#f4f2ec")),
                  "red": dict(HAIR="#a82a2a", extra=dict(cotton="#c83a3a", fold="#7a1a1a", kimono="#c83a3a", kimono_trim="#f4f2ec", obi="#2a2a32")),
                  "gold": dict(HAIR="#d8a840", extra=dict(cotton="#e8c060", fold="#a87a2a", kimono="#e8c060", kimono_trim="#7a5410", obi="#2a3a8a")),
                  "black": dict(HAIR="#1e1c22", EYE="#e83a3a", extra=dict(cotton="#2a2830", fold="#141218", kimono="#2a2830", kimono_trim="#8a8890", obi="#c83a3a"))}),
    # thirteenth wave: the chochin-obake, a girl under a living paper lantern (it glows) with one great eye and a lolling tongue
    "chochin_obake_devil": dict(
        build="petite", outfit=dict(top=["kimono"], bottom="hakama", extras=[],
                                    colors=dict(kimono="#f4f0e8", kimono_trim="#c83a3a", obi="#c83a3a", cloth2="#2a2a32")),
        hair="hime", bangs="blunt", ears=None, legs="bare", foot="shoe", lantern=True, glow=["lantern"], glow_boost=1.6,
        pal=dict(SKIN=skin("pale"), HAIR="#1e1a22", FUR="#1e1a22", LIGHT="#f4f0e8", MARK="#c83a3a", EYE="#e8a030", CLOTH="#f4f0e8", TRIM="#c83a3a",
                 extra=dict(paper="#f8e8c0", band="#c83a3a", rim="#1e1a1e", boot="#2a2a32")),
        variants={"white": dict(extra=dict(paper="#fbf8f0", band="#2a2a32", rim="#1e1a1e", boot="#2a2a32")),
                  "blue": dict(HAIR="#1e2a4a", extra=dict(paper="#c8dcf8", band="#2a4a9a", rim="#14182a", boot="#2a2a32", kimono="#2a4a9a", obi="#f8e070")),
                  "purple": dict(HAIR="#2a1e3a", extra=dict(paper="#e8d0f4", band="#6a2a9a", rim="#1e1428", boot="#2a2a32", kimono="#6a2a9a", obi="#f8e070")),
                  "gold": dict(HAIR="#3a2a14", extra=dict(paper="#fff0b0", band="#d8902a", rim="#3a2410", boot="#2a2a32", kimono="#d8902a", obi="#2a2a32"))}),
    # twelfth wave: the gremlin, a little green tinkerer with huge ears and a spanner
    "gremlin_devil": dict(
        build="petite", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["belt"], colors=dict(cloth2="#3a4a2a", leather="#5a3a22")),
        hair="wavy", bangs="parted", ears="big", legs="bare", foot="shoe", spanner=True,
        pal=dict(SKIN="#b8c890", HAIR="#5a8ad8", FUR="#b8c890", LIGHT="#e0e8c0", MARK="#7a8a5a", EYE="#3a8a4a", CLOTH="#7a9a5a", TRIM="#c8a040",
                 INNER="#e8b0a0", extra=dict(boot="#5a3a22")),
        variants={"blue": dict(SKIN="#98b8d8", FUR="#98b8d8", LIGHT="#d0e0f0", MARK="#5a7a9a", HAIR="#2a4a8a", CLOTH="#4a6a9a"),
                  "yellow": dict(SKIN="#e0d090", FUR="#e0d090", LIGHT="#f4ecc0", MARK="#a8984a", HAIR="#d86a2a", CLOTH="#c8902a"),
                  "grey": dict(SKIN="#a8a8a8", FUR="#a8a8a8", LIGHT="#d8d8d8", MARK="#6a6a6a", HAIR="#3a3a3e", CLOTH="#5a5a62"),
                  "purple": dict(SKIN="#c0a8d0", FUR="#c0a8d0", LIGHT="#e4d8ec", MARK="#7a5a8a", HAIR="#5a2a7a", CLOTH="#7a4a9a")}),
    # twelfth wave: the karakasa obake, a girl under an umbrella hat, a thin tail curled like its handle, hopping on one leg ("onehop")
    "karakasa_obake_devil": dict(
        build="petite", outfit=dict(top=["kimono"], bottom="hakama", extras=[],
                                    colors=dict(kimono="#3a6ac8", kimono_trim="#f4f0e8", obi="#c83a3a", cloth2="#2a4a8a")),
        hair="bob", bangs="blunt", ears=None, legs="bare", foot="shoe", tail=[(.75, .75, 4), (.75, .75, 4), (.75, .75, 3)],
        tail_rest=[(-20, 0, 0), (-40, 0, 0), (-70, 0, 0)], tail_look="fur", umbrella=True,
        pal=dict(SKIN=skin("fair"), HAIR="#3a6ac8", FUR="#3a6ac8", LIGHT="#a8c8f0", MARK="#2a4a8a", EYE="#3a5ab8", CLOTH="#3a6ac8", TRIM="#f4f0e8",
                 extra=dict(canopy="#4a7ad8", rib="#2a3a6a", boot="#3a2a20")),
        variants={"red": dict(HAIR="#c83a3a", FUR="#c83a3a", MARK="#7a1a1a", EYE="#c83a3a",
                              extra=dict(canopy="#d84a3a", rib="#5a1a14", kimono="#c83a3a", obi="#2a2a2a", cloth2="#8a2a2a", boot="#3a2a20")),
                  "purple": dict(HAIR="#7a4ab8", FUR="#7a4ab8", MARK="#4a2a7a", EYE="#8a4ac8",
                                 extra=dict(canopy="#8a5ac8", rib="#3a1e5a", kimono="#7a4ab8", obi="#f8e070", cloth2="#4a2a7a", boot="#3a2a20")),
                  "green": dict(HAIR="#3a8a5a", FUR="#3a8a5a", MARK="#1e5a3a", EYE="#3a8a4a",
                                extra=dict(canopy="#4a9a6a", rib="#1e4a2e", kimono="#3a8a5a", obi="#c83a3a", cloth2="#1e5a3a", boot="#3a2a20")),
                  "white": dict(HAIR="#f0eef0", FUR="#f0eef0", MARK="#a8a4b0", EYE="#c84a5a",
                                extra=dict(canopy="#f4f2f0", rib="#8a8490", kimono="#f4f2f0", obi="#c83a3a", cloth2="#c8c4d0", boot="#3a2a20"))}),
    # ninth wave: the oni, big and horned, a tiger-skin loincloth and an iron club (kanabo) in her right hand
    "oni_devil": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps", "belt"], colors=dict(cloth2="#e8a830")),
        hair="shaggy", bangs="messy", ears="elf", legs="bare", foot="shoe", horns="devil", club=True,
        pal=dict(SKIN="#d8584a", HAIR="#1e1a1e", FUR="#d8584a", LIGHT="#f0a090", MARK="#8a2a22", EYE="#f8d040", CLOTH="#f4f0e6", TRIM="#2a2420",
                 extra=dict(horn="#f4ecd8", boot="#3a2a20", tiger=True)),
        variants={"blue": dict(SKIN="#5a7ad0", FUR="#5a7ad0", LIGHT="#a0b8f0", MARK="#2a3a8a", HAIR="#f4f0e6"),
                  "yellow": dict(SKIN="#e8c05a", FUR="#e8c05a", LIGHT="#f8e0a0", MARK="#a87a2a", HAIR="#3a2a1a", EYE="#d83a2a"),
                  "green": dict(SKIN="#6aa85a", FUR="#6aa85a", LIGHT="#a8d898", MARK="#2a5a2a", HAIR="#e8e0c8"),
                  "black": dict(SKIN="#4a4448", FUR="#4a4448", LIGHT="#8a8488", MARK="#1e1a1e", HAIR="#e8e4e0", EYE="#f84a2a")}),
}


def declare_hook(sp, d):
    out = []
    if d.get("pumpkin"):
        out += [("pumpkin", 9.5, 5, 9.5), ("pumpkin_f", 9.5, 3.5, .25), ("stem", 1.5, 2, 1.5), ("whole", 12, 11, 12), ("whole_f", 12, 11, .25)]
    if d.get("club"):                                    # the kanabo: a long iron club with studs, and tiger stripes on her loincloth
        out += [("club", 2.25, 11, 2.25), ("grip", 1.25, 3.5, 1.25), ("stud", .5, .5, .5)]
    if d.get("qing"):
        out += [("cap", 9, 3, 9), ("cap_brim", 10.5, .75, 10.5), ("button", 1.5, 1.5, 1.5), ("talisman", 3, 6, .25)]
    if d.get("spanner"):
        out += [("spanner", .75, 4, .75), ("spanner_head", 2, 1, .75)]
    if d.get("neck"):
        out += [("neck_long", 2, 12, 2)]
    if d.get("ribbon"):
        out += [("cloth_a", 6, 6, .5), ("cloth_b", 5.5, 6, .5), ("cloth_c", 5, 6, .5), ("cloth_d", 4.5, 5, .5)]
    if d.get("lantern"):
        out += [("lantern", 8.5, 8, 8.5), ("lantern_rim", 6.5, 1, 6.5), ("tongue", 2, .5, 3.5)]
    if d.get("umbrella"):
        out += [("canopy", 14, 1, 14), ("canopy_b", 10.5, 1.25, 10.5), ("canopy_c", 7, 1.25, 7), ("canopy_top", 3.5, 1, 3.5), ("umb_tip", 1, 2, 1)]
    if d.get("kerchief"):                                # the kikimora's headscarf, knotted at the nape
        out += [("kerchief", 8.75, 4.5, 8.75), ("kerchief_knot", 2.5, 1.5, 1), ("kerchief_end", 1.5, 3, .25)]
    if d.get("smoke"):                                   # her goatee, drawn only on the lamp_friend look
        out += [("ggoatee", 1.75, 2, .5)]
    if d.get("smoke"):                                   # the genie's smoke tail, puffing smaller to a wisp
        out += [("smoke_a", 5.5, 4.5, 4.5), ("smoke_b", 4.5, 4.5, 3.75), ("smoke_c", 3.25, 4, 2.75), ("smoke_d", 2, 3.5, 1.75)]
    if d.get("lamp"):                                    # her brass lamp: body, lid, spout and handle
        out += [("lamp_body", 2.5, 1.25, 1.75), ("lamp_lid", 1, .75, 1), ("lamp_spout", .75, .5, 2), ("lamp_handle", .5, 1, 1)]
    if d.get("tongue"):                                  # the akaname's long tongue, lolling out over her chin
        out += [("long_tongue", 1.25, 3, .25)]
    return out


def paint_hook(pt, pal, d):
    if d.get("glyphs"):                                  # the taotie's bronze meanders over her arms and legs
        gl = G.shades(pal.extra.get("glyph", "#3a8a7a"))
        for p in ("thigh", "shin", "arm_up", "forearm"):
            pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(gl, 0.5 + 0.15 * (y % 4 == 0), x, y)
               if (y % 4 == 0 and x % 4 != 3) or (x % 4 == 0 and y % 4 != 1) and y % 8 < 4 else None)
    if d.get("cyclops"):
        paint_cyclops(pt, pal)
    pt("ggoatee", "all", lambda f, x, y, w, h: pick(pal.HAIR, 0.4, x, y) if pal.extra.get("goatee") else CLEAR)
    sm, smd = G.shades(pal.extra.get("smoke", "#8aa8e8")), G.shades(pal.extra.get("smoke_dark", "#4a6ac8"))
    for p in ("smoke_a", "smoke_b", "smoke_c", "smoke_d"):          # soft swirls of smoke, a little see-through
        pt(p, "all", lambda f, x, y, w, h: G.with_alpha(pick(smd if (x + 2 * y) % 7 < 2 else sm, 0.5 + 0.2 * noise(x // 2, y // 2, 91), x, y), 225))
    brass = G.shades(pal.extra.get("brass", "#d8a840"))
    for p in ("lamp_body", "lamp_lid", "lamp_spout", "lamp_handle"):
        pt(p, "all", lambda f, x, y, w, h: pick(brass, 0.5 + 0.25 * (f == "up") + 0.15 * (x == 1 and y == 0), x, y))
    tg = G.shades(pal.extra.get("tongue", "#5a8af0"))
    pt("long_tongue", "all", lambda f, x, y, w, h: pick(tg, 0.55 + 0.15 * (x == w // 2) - 0.1 * (y == h - 1), x, y))
    kc, kd = G.shades(pal.extra.get("kerchief", "#c83a2a")), G.shades(pal.extra.get("kerchief_dot", "#f4f0e4"))
    pt("kerchief", "all", lambda f, x, y, w, h: CLEAR if f == "down" or (f == "north" and y > 1) else
       pick(kd, 0.6, x, y) if (x % 4 == 1 and y % 4 == 1) else pick(kc, 0.5 + 0.15 * (f == "up"), x, y))
    pt("kerchief_knot", "all", lambda f, x, y, w, h: pick(kc, 0.4, x, y))
    pt("kerchief_end", "all", lambda f, x, y, w, h: pick(kc, 0.45 + 0.1 * (y % 2), x, y))
    pk = G.shades(pal.extra.get("pumpkin", "#e8822a"))
    candle = G.shades(pal.extra.get("candle", "#f8d83a"))
    ribs = lambda f, x, y, w, h: pick(pk, 0.5 + 0.18 * (x % 4 == 0) + (noise(x, y, 3) - 0.5) * 0.08, x, y)
    pt("pumpkin", "all", ribs)
    pt("whole", "all", ribs)
    pt("stem", "all", lambda f, x, y, w, h: pick(G.shades("#4a6a2a"), 0.5, x, y))

    def face(f, x, y, w, h, eyes_y=0.25, mouth_y=0.7):
        if f not in ("north",):
            return ribs(f, x, y, w, h)
        cx = w / 2
        eye = abs(y - h * eyes_y) < h * 0.12 and (abs(x - w * 0.3) < w * 0.08 + (h * eyes_y - y) * 0.3 or abs(x - w * 0.7) < w * 0.08 + (h * eyes_y - y) * 0.3)
        mouth = abs(y - h * mouth_y) < h * 0.08 and abs(x - cx) < w * 0.3 and not (x % 5 == 0 and y < h * mouth_y)
        return pick(candle, 0.8, x, y) if eye or mouth else CLEAR
    pt("pumpkin_f", "all", lambda f, x, y, w, h: face(f, x, y, w, h, 0.3, 0.75))
    pt("whole_f", "all", lambda f, x, y, w, h: face(f, x, y, w, h))
    iron = G.shades("#4a4a52")
    pt("club", "all", lambda f, x, y, w, h: pick(iron, 0.4 + 0.2 * (x == 0) + (noise(x, y, 13) - 0.5) * 0.1, x, y))
    pt("grip", "all", lambda f, x, y, w, h: pick(G.shades("#6a2a1a"), 0.45 + 0.2 * (y % 2), x, y))
    pt("stud", "all", lambda f, x, y, w, h: pick(G.shades("#9a9aa2"), 0.7, x, y))
    if pal.extra.get("tiger"):                           # the tiger-skin loincloth
        black = G.shades("#1e1a14")
        for p in ("flap_f", "flap_b"):
            pt(p, "all", lambda f, x, y, w, h: pick(black, 0.4, x, y) if (y + x // 2) % 4 == 0 else None)
    paper, band, rim = G.shades(pal.extra.get("paper", "#f8e8c0")), G.shades(pal.extra.get("band", "#c83a3a")), G.shades(pal.extra.get("rim", "#1e1a1e"))

    def lantern(f, x, y, w, h):                           # ribbed paper; on the front one great eye and a wide mouth
        if f in ("up", "down"):
            return pick(rim, 0.4, x, y)
        if f == "north":
            ex, ey = w * 0.5, h * 0.4
            d2 = ((x + 0.5 - ex) / (w * 0.22)) ** 2 + ((y + 0.5 - ey) / (h * 0.17)) ** 2
            if d2 < 1.0:
                return pick(rim, 0.15, x, y) if d2 < 0.22 else pick(G.shades("#ffffff"), 0.9, x, y) if d2 < 0.75 else pick(rim, 0.25, x, y)
            if abs(y + 0.5 - h * 0.74) < h * 0.05 and abs(x + 0.5 - w / 2) < w * 0.34:
                return pick(G.shades("#7a1420"), 0.35, x, y)
        if y % 4 == 0:
            return pick(rim, 0.35, x, y)                  # the bamboo ribs
        return pick(band if (y // 4) % 3 == 1 else paper, 0.55 + 0.15 * noise(x, y, 31), x, y)
    pt("lantern", "all", lantern)
    pt("lantern_rim", "all", lambda f, x, y, w, h: pick(rim, 0.45 + 0.15 * (f == "up"), x, y))
    pt("tongue", "all", lambda f, x, y, w, h: pick(G.shades("#e85a6a"), 0.55 + 0.2 * (f == "up"), x, y))
    pt("neck_long", "all", H.skin_fn(pal, 0.6))
    cotton, fold = G.shades(pal.extra.get("cotton", "#f4f2ec")), G.shades(pal.extra.get("fold", "#c8c4bc"))
    for p in ("cloth_a", "cloth_b", "cloth_c", "cloth_d"):
        pt(p, "all", lambda f, x, y, w, h: pick(fold if x % 5 == 2 or y % 6 == 5 else cotton, 0.55 + 0.1 * (noise(x, y, 41) - 0.5), x, y))
    steel = G.shades("#9a9aa6")
    pt("spanner", "all", lambda f, x, y, w, h: pick(G.shades("#c83a3a") if y >= h - 5 else steel, 0.55, x, y))
    pt("spanner_head", "all", lambda f, x, y, w, h: CLEAR if f in ("north", "south") and x in (w // 2 - 1, w // 2) and y < h // 2 else pick(steel, 0.6, x, y))
    can, rib = G.shades(pal.extra.get("canopy", "#4a7ad8")), G.shades(pal.extra.get("rib", "#2a3a6a"))

    def canopy(f, x, y, w, h):                            # oiled paper panels between bamboo ribs; darker underneath
        if f == "down":
            return pick(can, 0.3, x, y) if (x - y) % 13 and (x + y) % 13 else pick(rib, 0.4, x, y)
        if f == "up":
            dx, dy = x + 0.5 - w / 2, y + 0.5 - h / 2
            return pick(rib, 0.45, x, y) if abs(dx) < 0.6 or abs(dy) < 0.6 or abs(abs(dx) - abs(dy)) < 0.7 else pick(can, 0.55 + 0.1 * (abs(dx) + abs(dy) < w * 0.3), x, y)
        return pick(rib, 0.35, x, y) if y == h - 1 or x % 13 == 0 else pick(can, 0.5, x, y)
    pt("canopy", "all", canopy)
    pt("canopy_b", "all", canopy)
    pt("canopy_c", "all", canopy)
    pt("canopy_top", "all", lambda f, x, y, w, h: pick(can, 0.62, x, y))
    pt("umb_tip", "all", lambda f, x, y, w, h: pick(rib, 0.5, x, y))
    hat = G.shades(pal.extra.get("hat", "#1a1a22"))
    pt("cap", "all", lambda f, x, y, w, h: pick(hat, 0.45 + 0.1 * (x % 3 == 0), x, y))
    pt("cap_brim", "all", lambda f, x, y, w, h: pick(hat, 0.35, x, y))
    pt("button", "all", lambda f, x, y, w, h: pick(G.shades("#c83a3a"), 0.6, x, y))
    paper = G.shades("#f0d870")

    def talisman(f, x, y, w, h):
        if f not in ("north", "south"):
            return pick(paper, 0.6, x, y)
        if abs(x - w / 2) < 0.6 and 2 <= y <= h - 3 or (y in (3, 7, 11) and abs(x - w / 2) < w * 0.3):
            return pick(G.shades("#b81a1a"), 0.5, x, y)
        return pick(paper, 0.7 + (noise(x, y, 9) - 0.5) * 0.1, x, y)
    pt("talisman", "all", talisman)


def paint_cyclops(pt, pal):
    """one great eye in the middle of her face (over where the two eyes were), a single brow over it; one lid to blink with"""
    sk = H.skin_fn(pal, 0.70)
    cx, cy, a, b = 16.0, 18.0, 6.4, 6.8

    def face(f, x, y, w, h):
        e = ((x + 0.5 - cx) / a) ** 2 + ((y + 0.5 - cy) / b) ** 2
        if e <= 1.0:
            if e > 0.72:
                return pal.EYE_OUT                                   # the outline
            iris = ((x + 0.5 - cx) / (a * 0.62)) ** 2 + ((y + 0.5 - cy - 0.6) / (b * 0.66)) ** 2
            if (x, y) in ((13, 14), (14, 14), (13, 15), (14, 15), (18, 21)):
                return pal.WHITE                                     # glints
            if iris <= 0.35:
                return pal.EYE[0]                                    # the pupil
            if iris <= 1.0:
                return pal.EYE[min(4, max(1, int((y - cy + b) / (2 * b) * 5)))]
            return pal.WHITE
        if y == 9 and 11 <= x <= 20:
            return pal.BROW                                          # one brow
        if 6 <= y <= 25:
            return sk(f, x, y, w, h)                                 # skin over the two eyes and brows that were here
        return None
    pt("head", ("north",), face)

    def lid(f, x, y, w, h):                                          # one lid patch with a closed-eye arc across it
        if f != "north":
            return CLEAR
        lx, ly = 13.0, 7.0
        e = ((x + 0.5 - lx) / 6.9) ** 2 + ((y + 0.5 - ly) / 7.2) ** 2
        if e > 1.0:
            return CLEAR
        arch = ly + 2 - 3.6 * math.sin(math.pi * (x - lx + 6.5) / 13.0)
        return pal.EYE_OUT if abs(y + 0.5 - arch) <= 1.0 else sk(f, x, y, w, h)
    pt("eyelids", "all", lid)


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    if d.get("smoke"):                                   # the goatee at her chin
        r.put("head", cube("ggoatee", [-0.875, g.hy - 1.4, -4.3 + g.z]))
    if d.get("smoke"):                                   # the smoke tail she ends in, in four bones so it can curl
        r.add("smoke_1", "body", [0, 12.5, z], [cube("smoke_a", [-2.75, 8.0, -2.25 + z])])
        r.add("smoke_2", "smoke_1", [0, 8.0, z], [cube("smoke_b", [-2.25, 3.75, -1.9 + z])])
        r.add("smoke_3", "smoke_2", [0, 3.75, z], [cube("smoke_c", [-1.625, 0.0, -1.4 + z])])
        r.add("smoke_4", "smoke_3", [0, 0.0, z], [cube("smoke_d", [-1.0, -3.25, -0.9 + z])])
    if d.get("lamp"):                                    # the brass lamp held in her right hand
        px, py, pz = r.b["hand_r"]["pivot"]
        ly, lz = py - 2.75, pz - 1.4
        r.add("lamp", "hand_r", [px, py - 1.5, pz], [cube("lamp_body", [px - 1.25, ly, lz - 0.6]), cube("lamp_lid", [px - 0.5, ly + 1.25, lz - 0.25]),
                                                    cube("lamp_spout", [px - 0.375, ly + 0.5, lz - 2.4]), cube("lamp_handle", [px - 0.25, ly + 0.1, lz + 1.15])])
    if d.get("tongue"):
        r.put("head", G.rot_cube("long_tongue", [-0.625, hy - 1.6, -4.35 + z], [0, hy + 1.4, -4.35 + z], 18, 0, 0))
    if d.get("kerchief"):
        r.put("head", cube("kerchief", [-4.375, hy + 4.4, -4.375 + z], inflate=0.7), cube("kerchief_knot", [-1.25, hy + 2.3, 4.6 + z]),
              G.rot_cube("kerchief_end", [-1.6, hy - 0.6, 4.9 + z], [-0.85, hy + 2.3, 5.0 + z], 0, 0, 14),
              G.rot_cube("kerchief_end", [0.1, hy - 0.6, 4.9 + z], [0.85, hy + 2.3, 5.0 + z], 0, 0, -14))
    if d.get("pumpkin"):                                 # a carved pumpkin worn as a hat, the candle shining through
        r.add("pumpkin_hat", "head", [0, hy + 8, z], [cube("pumpkin", [-4.75, hy + 7.4, -4.75 + z]), cube("pumpkin_f", [-4.75, hy + 8.2, -4.85 + z]),
                                                    cube("stem", [-0.75, hy + 12.3, -0.75 + z])])
        r.add("disguise", "root", [0, 0, 0], [cube("whole", [-6, 0, -6]), cube("whole_f", [-6, 0, -6.1]), cube("stem", [-0.75, 11, -0.75])])
    if d.get("club"):                                    # gripped in her right fist and sloping up and forward, away from her arm
        px, py, pz = r.b["hand_r"]["pivot"]
        grip = [px, py - 1.5, pz]
        held = lambda p, o: G.rot_cube(p, o, grip, CLUB_TILT)
        studs = [held("stud", [px - 1.45 + 2.4 * (i % 2), py + 1.5 + 2.2 * (i // 2), pz - 0.25]) for i in range(8)]
        studs += [held("stud", [px - 0.25, py + 2.6 + 2.2 * i, pz - 1.45]) for i in range(3)]
        r.add("club", "hand_r", grip, [held("grip", [px - 0.625, py - 3.25, pz - 0.625]), held("club", [px - 1.125, py + 0.25, pz - 1.125])] + studs)
    if d.get("neck"):                                    # the long neck, rising from her collar (shrunk away until she stretches it)
        r.add("neck", "body", [0, hy - 0.5, z], [cube("neck_long", [-1, hy - 0.5, -1 + z])])
    if d.get("ribbon"):                                  # the long cotton ribbon she ends in, in four bones so it can ripple
        z0 = z
        r.add("ribbon_1", "body", [0, 12.5, z0], [cube("cloth_a", [-3, 6.5, -0.25 + z0])])
        r.add("ribbon_2", "ribbon_1", [0, 6.5, z0], [cube("cloth_b", [-2.75, 0.5, -0.25 + z0])])
        r.add("ribbon_3", "ribbon_2", [0, 0.5, z0], [cube("cloth_c", [-2.5, -5.5, -0.25 + z0])])
        r.add("ribbon_4", "ribbon_3", [0, -5.5, z0], [cube("cloth_d", [-2.25, -10.5, -0.25 + z0])])
    if d.get("lantern"):                                 # the lantern rests on her head like a tall hat; the tongue hangs from its mouth
        r.add("lantern", "head", [0, hy + 8, z], [cube("lantern", [-4.25, hy + 8.6, -4.25 + z]), cube("lantern_rim", [-3.25, hy + 8.0, -3.25 + z]),
                                                    cube("lantern_rim", [-3.25, hy + 16.6, -3.25 + z]),
                                                    G.rot_cube("tongue", [-1, hy + 9.6, -6.6 + z], [0, hy + 10.6, -4.4 + z], 35, 0, 0)])
    if d.get("spanner"):                                 # a spanner in her right hand
        px, py, pz = r.b["hand_r"]["pivot"]
        r.add("spanner", "hand_r", [px, py - 1.5, pz], [cube("spanner", [px - 0.375, py - 4.5, pz - 2.2]), cube("spanner_head", [px - 1.0, py - 5.4, pz - 2.2])])
    if d.get("umbrella"):                                # the umbrella hat, wide and peaked
        r.add("umbrella", "head", [0, hy + 9, z], [cube("canopy", [-7, hy + 8.4, -7 + z]), cube("canopy_b", [-5.25, hy + 9.4, -5.25 + z]),
                                                    cube("canopy_c", [-3.5, hy + 10.65, -3.5 + z]), cube("canopy_top", [-1.75, hy + 11.9, -1.75 + z]),
                                                    cube("umb_tip", [-0.5, hy + 12.9, -0.5 + z])])
    if d.get("qing"):
        r.add("qing_hat", "head", [0, hy + 8, z], [cube("cap", [-4.5, hy + 7.6, -4.5 + z]), cube("cap_brim", [-5.25, hy + 7.4, -5.25 + z]),
                                                 cube("button", [-0.75, hy + 10.6, -0.75 + z]), cube("talisman", [-1.5, hy + 2.2, -5.6 + z])])


def anim_extra(an, sp, d):
    """the jiang shi's hop (arms out, both feet together), the lesser devil's prank giggle ("laugh"), the succubus' charm ("charm")"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("hop", 0.8, True, merge({"root": {"position": Tk((0, (0, 0, 0)), (0.2, (0, -0.5, 0)), (0.4, (0, 3, 0)), (0.7, (0, 0, 0)), (0.8, (0, 0, 0)))},
                                    "leg_r": {"rotation": hold(0, 0, 0)}, "leg_l": {"rotation": hold(0, 0, 0)}}, sym("arm", hold(-90, 0, 0)), sym("forearm", hold(0, 0, 0))))
    if d.get("qing"):                                    # arms always out in front of her
        for key in ("idle", "walk", "run", "stalk"):
            F._inject(an, key, merge(sym("arm", hold(-90, 0, 0)), sym("forearm", hold(0, 0, 0))))
    an.add("laugh", 1.2, True, merge({"body": {"position": Tk((0, (0, 0, 0)), (0.15, (0, 0.5, 0)), (0.3, (0, 0, 0)), (0.45, (0, 0.5, 0)), (0.6, (0, 0, 0)), (1.2, (0, 0, 0)))},
                                      "head": {"rotation": hold(-14, 0, 0)}, "eyelids": {"position": hold(*H.OUT_)}, "mouth": {"position": hold(*H.OUT_)}},
                                     sym("arm", hold(-30, 0, 20)), sym("forearm", hold(-90, 0, 0))))
    if d.get("pumpkin"):                                 # the disguise stays shrunk away unless the game shows it (see GirlRenderer)
        for key in [k.split(".")[-1] for k in an.A]:
            F._inject(an, key, {"disguise": {"scale": hold(0, 0, 0)}})
    if d.get("smoke"):                                   # she floats; her smoke tail curls and sways under her
        wave = lambda a, ph: Tk((0, (a, 0, -6 * ph)), (1.0, (a + 8 * ph, 0, 6 * ph)), (2.0, (a, 0, -6 * ph)))
        rest = {"smoke_1": {"rotation": wave(10, 1)}, "smoke_2": {"rotation": wave(20, -1)}, "smoke_3": {"rotation": wave(25, 1)},
                "smoke_4": {"rotation": wave(30, -1)}, "root": {"position": Tk((0, (0, 2.0, 0)), (1.0, (0, 2.8, 0)), (2.0, (0, 2.0, 0)))}}
        for key in [k.split(".")[-1] for k in an.A]:
            F._inject(an, key, rest)
    if d.get("ribbon"):                                  # the ribbon trails out behind her, rippling; flying, it streams straight back
        wave = lambda a, ph: Tk((0, (a, 0, 0)), (1.0, (a + 12 * ph, 0, 4)), (2.0, (a, 0, 0)))
        rest = {"ribbon_1": {"rotation": wave(40, 1)}, "ribbon_2": {"rotation": wave(25, -1)}, "ribbon_3": {"rotation": wave(20, 1)},
                "ribbon_4": {"rotation": wave(15, -1)}, "root": {"position": Tk((0, (0, 1.5, 0)), (1.0, (0, 2.2, 0)), (2.0, (0, 1.5, 0)))}}
        for key in [k.split(".")[-1] for k in an.A]:
            F._inject(an, key, rest)
        an.add("fly", 1.0, True, merge(G.upper_idle(), {"body": {"rotation": hold(20, 0, 0)},
                                                        "ribbon_1": {"rotation": Tk((0, (60, 0, 0)), (0.5, (70, 0, 0)), (1.0, (60, 0, 0)))},
                                                        "ribbon_2": {"rotation": Tk((0, (10, 0, 0)), (0.5, (-10, 0, 0)), (1.0, (10, 0, 0)))},
                                                        "ribbon_3": {"rotation": Tk((0, (-10, 0, 0)), (0.5, (10, 0, 0)), (1.0, (-10, 0, 0)))},
                                                        "ribbon_4": {"rotation": Tk((0, (10, 0, 0)), (0.5, (-15, 0, 0)), (1.0, (10, 0, 0)))}},
                                       sym("arm", hold(-20, 0, 25))))
    if d.get("neck"):                                    # the neck stays shrunk away; "watch" stretches it, her head riding up on top
        for key in [k.split(".")[-1] for k in an.A]:
            F._inject(an, key, {"neck": {"scale": hold(1, 0, 1)}})
        import copy
        watch = copy.deepcopy(an.A[f"animation.{sp}.idle"])
        watch["bones"].setdefault("head", {})["position"] = Tk((0, (0, 0, 0)), (1.0, (0, 11.5, 0)), (4.0, (0, 11.5, 0)))
        watch["bones"]["head"]["rotation"] = Tk((0, (0, 0, 0)), (1.0, (-5, 0, 0)), (2.5, (-5, 25, 0)), (4.0, (-5, -25, 0)))
        watch["bones"]["neck"] = {"scale": Tk((0, (1, 0, 1)), (1.0, (1, 1, 1)), (4.0, (1, 1, 1)))}
        watch["loop"], watch["animation_length"] = False, 4.0
        an.A[f"animation.{sp}.watch"] = watch
    if d.get("spanner"):                                 # the gremlin's big ears stick out sideways
        for key in [k.split(".")[-1] for k in an.A]:
            F._inject(an, key, {"ear_r": {"rotation": hold(0, 0, 65)}, "ear_l": {"rotation": hold(0, 0, -65)}})
    an.add("onehop", 0.7, True, merge({"root": {"position": Tk((0, (0, 0, 0)), (0.15, (0, -0.5, 0)), (0.35, (0, 2.5, 0)), (0.6, (0, 0, 0)), (0.7, (0, 0, 0)))},
                                       "leg_r": {"rotation": hold(0, 0, 0)}, "leg_l": {"rotation": hold(-40, 0, 0)}, "shin_l": {"rotation": hold(90, 0, 0)}},
                                      sym("arm", hold(-20, 0, 35))))
    if d.get("umbrella"):                                # the karakasa obake goes everywhere on one leg
        for key in ("walk", "run"):
            an.A[f"animation.{sp}.{key}"] = an.A[f"animation.{sp}.onehop"]
    an.add("charm", 2.0, True, merge({"head": {"rotation": hold(8, 14, 6)}, "body": {"rotation": hold(0, 10, 4)}},
                                     {"arm_r": {"rotation": hold(-150, 0, 20)}, "forearm_r": {"rotation": hold(-60, 0, 0)}}))


if __name__ == "__main__":
    F.run_family("devil", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
