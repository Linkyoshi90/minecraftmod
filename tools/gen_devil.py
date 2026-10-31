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
    # twenty-fifth wave: the Fire Giant, four blocks tall: dark skin veined with glowing lava, a crown of flame, long black hair
    "fire_giant_devil": dict(
        build="curvy", outfit=dict(top=["bandeau"], bottom="sarong", extras=["armlets", "belt"], colors=dict(cloth2="#1e3a8a", leather="#c8901e")),
        hair="flowing", bangs="parted", ears="elf", legs="bare", foot="shoe", lava=True, glow=["lvein", "lcrown"], glow_boost=1.3,
        pal=dict(SKIN=skin("#5a3a2a"), HAIR="#1a1210", FUR="#2a1a14", LIGHT="#7a5a44", MARK="#1a0e0a", EYE="#f8a01e", CLOTH="#1e3a8a", TRIM="#e8b030",
                 extra=dict(boot="#2a1a14", vein="#f8701e", vein_hot="#ffd860")),
        variants={"gold": dict(SKIN=skin("#8a6a3a"), HAIR="#3a2410", EYE="#f8d040", CLOTH="#7a1e1e", extra=dict(boot="#3a2410", vein="#f8c030", vein_hot="#fff4c0")),
                  "blue": dict(EYE="#4ac8f8", CLOTH="#2a2a30", TRIM="#4ac8f8", extra=dict(boot="#2a1a14", vein="#3ab0f8", vein_hot="#e0f8ff")),
                  "green": dict(EYE="#6af84a", CLOTH="#2a2a30", TRIM="#6af84a", extra=dict(boot="#2a1a14", vein="#5af04a", vein_hot="#e8ffd0")),
                  "ash": dict(SKIN=skin("#5a5a60"), HAIR="#141418", EYE="#f83a1e", CLOTH="#2a2a30", extra=dict(boot="#1e1e24", vein="#f83a1e", vein_hot="#ffc080"))}),
    # twenty-fifth wave: the Frost Giant, four blocks tall: long white hair, a crown and shoulders of ice shards
    "frost_giant_devil": dict(
        build="curvy", outfit=dict(top=["bandeau"], bottom="sarong", extras=["armlets"], colors=dict(cloth2="#a8d8f0")),
        hair="flowing", bangs="swept", ears="elf", legs="bare", foot="shoe", ice=True,
        pal=dict(SKIN=skin("#d8ecf8"), HAIR="#f0f4fa", FUR="#f4f8fc", LIGHT="#ffffff", MARK="#a8c8e0", EYE="#3a8ad8", CLOTH="#a8d8f0", TRIM="#e8f4fc",
                 extra=dict(boot="#a8c8e0", ice="#a8e0f8", ice_tip="#ffffff")),
        variants={"white": dict(SKIN=skin("#f0f0f4"), EYE="#6a7a9a", CLOTH="#e8e8f0", extra=dict(boot="#c8c8d8", ice="#e8f0f8", ice_tip="#ffffff")),
                  "aqua": dict(SKIN=skin("#c8f0ec"), HAIR="#d8fcf8", EYE="#2aa8a0", CLOTH="#7ae0d8", extra=dict(boot="#7ac8c0", ice="#8af0e0", ice_tip="#f0fffc")),
                  "violet": dict(SKIN=skin("#e4dcf8"), HAIR="#f0ecfc", EYE="#7a5ad8", CLOTH="#c8b0f0", extra=dict(boot="#a890d8", ice="#c8b8f8", ice_tip="#f8f4ff")),
                  "grey": dict(SKIN=skin("#c8ccd4"), HAIR="#a8acb4", EYE="#3a4a6a", CLOTH="#8a909a", extra=dict(boot="#6a7078", ice="#a8b8c8", ice_tip="#e8f0f8"))}),
    # twenty-fifth wave: the Ifrit, a fire spirit: horns, small wings, a smoke tail for legs, blue flames floating at her sides
    "ifrit_devil": dict(
        build="curvy", outfit=dict(top=["bandeau"], extras=["armlets"], colors=dict(cloth2="#2a5ab8")),
        hair="shaggy", bangs="swept", ears="elf", lower="none", smoke=True, horns="devil", wings="bat", wing_scale=0.7, iflames=True, glow=["iflame"], glow_boost=1.2,
        pal=dict(SKIN=skin("#a8c8f0"), HAIR="#6aa8e8", FUR="#7ab0e8", LIGHT="#d8e8fc", MARK="#2a5ab8", EYE="#2a4a9a", CLOTH="#2a5ab8", TRIM="#a8d8f8",
                 extra=dict(smoke="#7ab0e8", smoke_dark="#2a5ab8", horn="#2a4a9a", wing="#4a7ac8", wing_vein="#2a4a9a", iflame="#4ac8f8", iflame_hot="#e8fcff")),
        variants={"orange": dict(SKIN=skin("#e8a878"), HAIR="#c85a1e", MARK="#7a2a0e", CLOTH="#7a2a0e", EYE="#7a2a0e", extra=dict(smoke="#e88a4a", smoke_dark="#7a2a0e", horn="#3a1a0e", wing="#a84a1e", wing_vein="#5a1e0e", iflame="#f8901e", iflame_hot="#fff0a0")),
                  "green": dict(SKIN=skin("#a8e0b0"), HAIR="#3a9a5a", MARK="#1a5a2e", CLOTH="#1a5a2e", EYE="#1a5a2e", extra=dict(smoke="#6ac88a", smoke_dark="#1a5a2e", horn="#143a1e", wing="#2a7a4a", wing_vein="#143a1e", iflame="#5af04a", iflame_hot="#e8ffd0")),
                  "violet": dict(SKIN=skin("#c8b0f0"), HAIR="#7a4ac8", MARK="#3a1a7a", CLOTH="#3a1a7a", EYE="#3a1a7a", extra=dict(smoke="#a07ae0", smoke_dark="#3a1a7a", horn="#2a1454", wing="#5a2aa8", wing_vein="#2a1454", iflame="#c84af8", iflame_hot="#f8e0ff")),
                  "black": dict(SKIN=skin("#5a5a64"), HAIR="#1e1e24", MARK="#0e0e12", CLOTH="#0e0e12", EYE="#f83a1e", extra=dict(smoke="#3a3a44", smoke_dark="#0e0e12", horn="#0e0e12", wing="#2a2a30", wing_vein="#0e0e12", iflame="#f8501e", iflame_hot="#ffd860"))}),
    # twenty-fifth wave: Pazuzu, the king of the wind demons: horns, feathered wings, talons and a snake for a tail; she flies
    "pazuzu_devil": dict(
        build="athletic", outfit=dict(top=["bandeau"], bottom="loincloth", extras=["armlets", "belt"], colors=dict(cloth2="#c8a040", leather="#c8a040")),
        hair="shaggy", bangs="jagged", ears="elf", legs="scales", foot="claw", horns="devil", wings="feather", wing_scale=1.0, flies=True,
        tail=[(1.5, 1.5, 4), (1.25, 1.25, 4), (1, 1, 3.5)], tail_look="scale", snake_tail=True,
        pal=dict(SKIN=skin("#a8c0e0"), HAIR="#3a5a9a", FUR="#6a8ac0", LIGHT="#d0dcf0", MARK="#2a3a6a", EYE="#e8c040", CLOTH="#c8a040", TRIM="#e8c860",
                 extra=dict(horn="#2a3a6a", boot="#2a3a6a", wing="#7a9ad0", wing_tip="#2a3a6a")),
        variants={"sand": dict(SKIN=skin("#d8b888"), HAIR="#7a5a2e", FUR="#b8945a", LIGHT="#f0e0bc", MARK="#5a3e1e", extra=dict(horn="#3a2410", boot="#3a2410", wing="#b8945a", wing_tip="#5a3e1e")),
                  "grey": dict(SKIN=skin("#b8bcc4"), HAIR="#4a4c54", FUR="#7a7e88", LIGHT="#d8dae0", MARK="#2a2c32", extra=dict(horn="#1e2024", boot="#1e2024", wing="#8a8e98", wing_tip="#2a2c32")),
                  "red": dict(SKIN=skin("#d89a8a"), HAIR="#7a1e1a", FUR="#b84a3a", LIGHT="#f0c8b8", MARK="#5a1410", extra=dict(horn="#2a0e0a", boot="#2a0e0a", wing="#b84a3a", wing_tip="#5a1410")),
                  "green": dict(SKIN=skin("#a8d0a8"), HAIR="#2a6a3a", FUR="#5a9a6a", LIGHT="#d0ecd0", MARK="#1a4a24", extra=dict(horn="#0e2a14", boot="#0e2a14", wing="#5a9a6a", wing_tip="#1a4a24"))}),
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
                  "green": dict(HAIR="#3a7a3a", EYE="#4a9a3a", CLOTH="#a8803a"),
                  "tactical_eye": dict(HAIR="#3a2458", EYE="#4a5ad8", CLOTH="#e8b83a")}),
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


_declare, _paint, _build = declare_hook, paint_hook, build_hook


def declare_hook(sp, d):                                 # twenty-fifth wave: the giants' lava and ice, the ifrit's flames, Pazuzu's snake
    out = list(_declare(sp, d) or [])
    if d.get("lava"):
        out += [("lvein", .5, 3.5, .25), ("lcrown", 1.25, 3.5, 1.25)]
    if d.get("ice"):
        out += [("ishard", 1.25, 4.5, 1.25), ("ishard_s", .75, 2.75, .75)]
    if d.get("iflames"):
        out += [("iflame", 2.25, 3.5, 2.25)]
    if d.get("snake_tail"):
        out += [("shead", 1.75, 1.5, 2.75)]
    return out


def paint_hook(pt, pal, d):
    _paint(pt, pal, d)
    if d.get("lava"):
        v, hot = G.shades(pal.extra["vein"]), G.shades(pal.extra["vein_hot"])
        pt("lvein", "all", lambda f, x, y, w, h: pick(hot if y % 3 == 1 else v, 0.7, x, y))
        pt("lcrown", "all", lambda f, x, y, w, h: pick(hot if y > h * 0.5 else v, 0.65 + 0.2 * (x % 2), x, y))
    if d.get("ice"):
        ic, tip = G.shades(pal.extra["ice"]), G.shades(pal.extra["ice_tip"])
        for p in ("ishard", "ishard_s"):
            pt(p, "all", lambda f, x, y, w, h: pick(tip if y < h * 0.35 or x == 0 else ic, 0.6, x, y))
    if d.get("iflames"):
        fl, hot = G.shades(pal.extra["iflame"]), G.shades(pal.extra["iflame_hot"])
        pt("iflame", "all", lambda f, x, y, w, h: pick(hot if y > h * 0.6 else fl, 0.6 + 0.2 * (x % 2), x, y))
    if d.get("snake_tail"):
        pt("shead", "all", lambda f, x, y, w, h: pick(pal.extra["horn"] and G.shades(pal.FUR and "#2a2a30") if f == "north" and y == 0 and x in (0, w - 1) else pal.FUR, 0.45, x, y))


def build_hook(r, g, d):
    _build(r, g, d)
    hy, z = g.hy, g.z
    if d.get("lava"):                                    # veins of lava over her skin, a crown of flame
        ax, ay, az = r.b["arm_r"]["pivot"]
        r.put("arm_r", G.rot_cube("lvein", [ax - 0.75, ay - 3.5, az - 0.9], [ax, ay - 2, az], 0, 0, 12))
        r.put("forearm_r", G.rot_cube("lvein", [ax - 0.25, ay - 7.0, az - 0.9], [ax, ay - 5, az], 0, 0, -14))
        fz = -G.S("midriff")[2] / 2 - 0.1 + z
        r.put("body", G.rot_cube("lvein", [-1.5, 13.5, fz], [-1.25, 15, fz], 0, 0, 22), G.rot_cube("lvein", [1.0, 13.8, fz], [1.25, 15, fz], 0, 0, -22))
        lx, ly, lz = r.b["leg_r"]["pivot"]
        sx, sy, sz = r.b["shin_r"]["pivot"]
        r.put("leg_r", G.rot_cube("lvein", [lx - 0.25, sy + 0.5, lz - 1.5], [lx, sy + 2, lz - 1.5], 0, 0, 16))
        r.put("shin_r", G.rot_cube("lvein", [sx - 0.25, 2.0, sz - 1.4], [sx, 3.5, sz - 1.4], 0, 0, -14))
        r.add("hair_lcrown", "head", [0, hy + 8, z], [G.rot_cube("lcrown", [x - 0.625, hy + 8.2, -3.6 + z + abs(x) * 0.6], [x, hy + 8.2, -3 + z], 0, 0, -x * 9)
                                                      for x in (-3.4, -1.7, 0, 1.7, 3.4)])
    if d.get("ice"):                                     # a crown of shards, and more standing up from her shoulders
        r.add("hair_icrown", "head", [0, hy + 8, z], [G.rot_cube("ishard", [x - 0.625, hy + 7.8, -2.6 + z], [x, hy + 7.8, -2 + z], 0, 0, -x * 11)
                                                      for x in (-3.4, -1.7, 0, 1.7, 3.4)])
        ax, ay, az = r.b["arm_r"]["pivot"]
        r.put("arm_r", G.rot_cube("ishard_s", [ax - 0.375, ay + 0.2, az - 0.375], [ax, ay, az], 0, 0, 24), G.rot_cube("ishard_s", [ax - 1.0, ay, az + 0.5], [ax, ay, az], 14, 0, 40))
    if d.get("iflames"):                                 # floating free at her sides
        r.add("hair_iflames", "body", [0, 16, z], [cube("iflame", [x - 1.125, y, z - 1.125]) for x, y in ((-8.5, 15.5), (8.5, 15.5), (-7.0, 21.0), (7.0, 21.0))])
    if d.get("snake_tail"):                              # the tip of her tail is a snake's head, looking back at you
        last = [b for b in ("tail_3", "tail_2", "tail") if b in r.b][0]
        px, py, pz = r.b[last]["pivot"]
        r.put(last, cube("shead", [px - 0.875, py - 0.5, pz + 3.0]))


# twenty-sixth wave: Lilith of Jewish legend, night-blue hair, ram's horns, great bat wings, a blue leotard and long gloves; she flies
SPECIES["lilith_devil"] = dict(
    build="curvy", outfit=dict(top=["corset"], bottom="shorts", extras=["armlets"], colors=dict(cloth2="#2a4a9a")),
    hair="wavy", bangs="swept", ears="elf", legs="stockings", foot="shoe", tail=SPADE, tail_look="spade", tail_rest=SPADE_REST, horns="ram", wings="bat", wing_scale=1.5, flies=True,
    pal=dict(SKIN=skin("#cfe0f4"), HAIR="#5a8ad0", FUR="#5a8ad0", LIGHT="#d8e8f8", MARK="#1e3468", EYE="#2a5ac8", CLOTH="#2a4a9a", TRIM="#1e3468",
             pupil=(1.6, 2.4), extra=dict(horn="#1e2a58", wing="#2a3a78", wing_bone="#141e48", tail_col="#1e2a58", stocking="#1e3468", boot="#1e3468")),
    variants={"black": dict(SKIN=skin("pale"), HAIR="#1a1820", FUR="#1a1820", CLOTH="#1a1820", TRIM="#a81e2a", EYE="#c81e2a", extra=dict(horn="#141218", wing="#26222a", wing_bone="#0e0c10", tail_col="#141218", stocking="#141218", boot="#141218")),
              "red": dict(SKIN=skin("fair"), HAIR="#a81e2a", FUR="#a81e2a", CLOTH="#7a141e", TRIM="#2a0e12", EYE="#e8a030", extra=dict(horn="#2a0e12", wing="#7a141e", wing_bone="#2a0e12", tail_col="#7a141e", stocking="#2a0e12", boot="#2a0e12")),
              "violet": dict(SKIN=skin("lilac"), HAIR="#5a2a8a", FUR="#5a2a8a", CLOTH="#3a1e6a", TRIM="#1e0e3a", EYE="#a83ae8", extra=dict(horn="#1e0e3a", wing="#3a1e6a", wing_bone="#1e0e3a", tail_col="#3a1e6a", stocking="#1e0e3a", boot="#1e0e3a")),
              "white": dict(SKIN=skin("pale"), HAIR="#f0f0f8", FUR="#f0f0f8", CLOTH="#e8e8f0", TRIM="#9a9ab0", EYE="#6a4ac8", extra=dict(horn="#c8c8d8", wing="#d8d8e4", wing_bone="#8a8aa0", tail_col="#c8c8d8", stocking="#c8c8d8", boot="#c8c8d8"))})
# twenty-sixth wave: Wrath, a walking fire vaguely in the shape of an oni: broad shoulders, two horns, a kanabo and a tail, and every bit of her is flame
SPECIES["wrath_devil"] = dict(
    build="strong", outfit=dict(top=["bandeau"], bottom="loincloth", colors=dict(cloth2="#c84a1e", leather="#c84a1e")),
    hair="shaggy", bangs="jagged", ears="elf", legs="bare", foot="shoe", horns="greater", club=True, tail=[(1.75, 1.75, 4), (1.25, 1.25, 4), (.75, .75, 3)], grin=True,
    wrath=True, glow="all",
    pal=dict(SKIN="#e8602a", HAIR="#f8a03a", FUR="#e8602a", LIGHT="#f8c860", MARK="#a82a14", EYE="#fff0c0", CLOTH="#c84a1e", TRIM="#f8a03a",
             extra=dict(horn="#f8d060", boot="#a82a14", fire=("#5a0e08", "#c82a0e", "#f8701e", "#fcc83a", "#fff4d0"))),
    variants={"blue": dict(extra=dict(horn="#a8d0f8", boot="#2a3a8a", fire=("#0a1450", "#1a48c8", "#2a90f8", "#80dcfc", "#f0fcff"))),
              "green": dict(extra=dict(horn="#c0f8a0", boot="#1a6a2a", fire=("#06381a", "#0e8a30", "#2ad04a", "#a0f850", "#f4ffd8"))),
              "violet": dict(extra=dict(horn="#e0b0f8", boot="#4a1a8a", fire=("#1e0640", "#5a18b0", "#a03af0", "#e88af8", "#fff0ff"))),
              "white": dict(extra=dict(horn="#ffffff", boot="#8a8a94", fire=("#6a6058", "#c8a878", "#f8e0a0", "#fff8e0", "#ffffff")))})
FLAMES = [("hair_wflame1", "body", -5.5, 21, 0, 16), ("hair_wflame2", "body", 5.5, 21, 0, -16), ("hair_wflame3", "body", 0, 19, 3, 0),
          ("hair_wflame4", "body", -4.0, 12, 0, 22), ("hair_wflame5", "body", 4.0, 12, 0, -22)]
_declare3, _paint3, _build3, _extra3 = declare_hook, paint_hook, build_hook, anim_extra


def _lerp(a, b, t):
    return tuple(int(x + (y - x) * t) for x, y in zip(a, b))


def fire_ramp(hexes):
    return [tuple(int(h[i:i + 2], 16) for i in (1, 3, 5)) for h in hexes]


def set_alight(T, ramp):
    """every painted texel becomes flame: its brightness picks the heat (so her eyes and grin still read), licked with streaks"""
    for y, row in enumerate(T):
        for x, px in enumerate(row):
            if not px[3]:
                continue
            lum = (0.3 * px[0] + 0.59 * px[1] + 0.11 * px[2]) / 255
            t = max(0.0, min(0.999, -0.08 + 0.92 * lum + (noise(x // 2, y // 3, 7) - 0.5) * 0.4)) * (len(ramp) - 1)
            i = int(t)
            row[x] = _lerp(ramp[i], ramp[i + 1], t - i) + (255,)


def declare_hook(sp, d):
    out = list(_declare3(sp, d) or [])
    if d.get("wrath"):
        out += [("wflame", 2.25, 5.5, 2.25), ("wflame_s", 1.25, 3, 1.25)]
    return out


def paint_hook(pt, pal, d):
    _paint3(pt, pal, d)
    if d.get("wrath"):
        pt("wflame", "all", lambda f, x, y, w, h: pick(G.shades("#f8c060"), 0.9 - 0.8 * y / h + 0.2 * (x % 3 == 0), x, y))
        pt("wflame_s", "all", lambda f, x, y, w, h: pick(G.shades("#f8c060"), 0.9 - 0.8 * y / h, x, y))
        set_alight(pt.T, fire_ramp(pal.extra["fire"]))


def build_hook(r, g, d):
    _build3(r, g, d)
    if d.get("wrath"):                                   # tongues of flame licking up off her shoulders, back and hips
        for name, parent, x, y, z, rz in FLAMES:
            piv = [x, y, z + g.z]
            r.add(name, parent, piv, [G.rot_cube("wflame", [x - 1.125, y, z + g.z - 1.125], piv, 0, 0, rz), G.rot_cube("wflame_s", [x - 0.625, y + 5.3, z + g.z - 0.625], piv, 0, 0, rz * 1.6)])


def anim_extra(an, sp, d):
    _extra3(an, sp, d)
    if d.get("wrath"):                                   # she never stops flickering
        Tk = H.track
        flick = lambda p: {"rotation": Tk((0, (0, 0, 0)), (0.2 + p, (9, 0, -7)), (0.45 + p, (-7, 0, 9)), (0.8, (0, 0, 0)))}
        tracks = {n: flick(i * 0.05) for i, (n, *_) in enumerate(FLAMES)}
        for key in [k.split(".")[-1] for k in an.A]:
            F._inject(an, key, tracks)


# homage look with a model of its own (AbstractMonsterGirl.OWN_MODELS), written after its species so its texture is the one the game draws.
# Manako (Monster Musume): a short purple bob and her one eye, a yellow suit with a zip and shoulder plates, black leggings, yellow armoured
# knees, tall navy boots, a red belt with two yellow pouches, a long tan anti-materiel rifle held upright in her right hand
HOMAGE_MODELS = {"cyclops_devil_tactical_eye": "cyclops_devil"}
SPECIES["cyclops_devil_tactical_eye"] = dict(
    build="slim", outfit=dict(top=["sweater"], bottom="shorts", extras=["belt", "pauldron"], colors=dict(cloth2="#1a1a22", leather="#9a2a1e")),
    hair="bob", bangs="blunt", ears=None, legs="boots", foot="shoe", cyclops=True, manako=True,
    pal=dict(SKIN=skin("fair"), HAIR="#3a2458", FUR="#3a2458", LIGHT="#f4f0e8", MARK="#22143a", EYE="#4a5ad8", CLOTH="#e8b83a", TRIM="#e8b83a",
             extra=dict(boot="#2a2c4a", suit="#e8b83a", black="#1a1a22", stripe="#9a2a1e", plate="#b8bcc4", rifle="#c8a858", rifle_dark="#7a6434", glove="#2a2c4a")),
    variants={})
_declare2, _paint2, _build2 = declare_hook, paint_hook, build_hook


def declare_hook(sp, d):
    out = list(_declare2(sp, d) or [])
    if d.get("manako"):
        out += [("mknee", 2.25, 2.5, .75), ("mpouch", 1.25, 2.25, 1.5), ("rf_stock", 1.25, 3.5, 2.25), ("rf_body", 1.5, 10, 2), ("rf_barrel", .75, 12, .75),
                ("rf_brake", 1.25, 2.25, 1.25), ("rf_scope", 1, 5, 1), ("rf_mag", .75, 3, 2.25), ("rf_bipod", .25, 3.5, .25)]
    return out


def paint_hook(pt, pal, d):
    _paint2(pt, pal, d)
    if d.get("manako"):
        paint_manako(pt, pal)


def paint_manako(pt, pal):
    e = pal.extra
    suit, black, red, navy, plate, tan, dark = (G.shades(e[k]) for k in ("suit", "black", "stripe", "glove", "plate", "rifle", "rifle_dark"))
    shiny = lambda f, x, y, w, h: pick(black, 0.35 + 0.3 * (x == w // 3), x, y)            # the sheen of her leggings

    def front(f, x, y, w, h):                                                              # the yellow suit, a zip down the middle
        if f == "north" and abs(x + 0.5 - w / 2) < 1:
            return pick(black, 0.5, x, y)
        return pick(suit, 0.55 + 0.12 * (f == "up") - 0.08 * (f in ("east", "west", "south")), x, y)
    for p in ("sh_chest", "chest", "midriff"):
        pt(p, ("north", "south", "east", "west"), front)
    pt("knit_bust", "all", lambda f, x, y, w, h: pick(suit, 0.6, x, y))
    pt("turtle", "all", lambda f, x, y, w, h: pick(G.shades("#f4f0e8"), 0.65, x, y) if y < h * 0.6 else pick(black, 0.45, x, y))
    pt("turtle", ("up",), lambda f, x, y, w, h: pick(black, 0.4, x, y))
    pt("sh_arm", ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(suit, 0.55, x, y) if y < h * 0.55 else pick(red, 0.5, x, y) if y < h * 0.7 else shiny(f, x, y, w, h))
    pt("sh_fore", ("north", "south", "east", "west"), lambda f, x, y, w, h: shiny(f, x, y, w, h) if y < h * 0.45 else pick(navy, 0.45 + 0.15 * (y % 4 == 0), x, y))
    pt("pauldron", "all", lambda f, x, y, w, h: pick(suit, 0.62 + 0.15 * (f == "up"), x, y))
    for p in ("palm", "thumb"):
        pt(p, "all", lambda f, x, y, w, h: pick(navy, 0.5, x, y))
    pt("fingers", "all", H.skin_fn(pal, 0.66))                                             # fingerless gloves
    pt("sh_hips", "all", shiny)
    pt("short_leg", "all", shiny)
    pt("thigh", "all", lambda f, x, y, w, h: shiny(f, x, y, w, h) if y < h * 0.5 else pick(red, 0.5, x, y) if y < h * 0.62 else pick(suit, 0.55, x, y))
    pt("shin", "all", lambda f, x, y, w, h: pick(suit, 0.55, x, y) if y < h * 0.14 else pick(red, 0.5, x, y) if y < h * 0.2 else
       shiny(f, x, y, w, h) if y < h * 0.3 else pick(navy, 0.42 + 0.22 * (y % 8 == 0) + 0.1 * (y == 0), x, y))
    pt("belt", "all", lambda f, x, y, w, h: pick(red, 0.5, x, y))
    pt("buckle", "all", lambda f, x, y, w, h: pick(plate, 0.7, x, y))
    pt("mknee", "all", lambda f, x, y, w, h: pick(plate, 0.6 + 0.2 * (x < w / 2) - 0.2 * (y == h - 1), x, y))
    pt("mpouch", "all", lambda f, x, y, w, h: pick(suit, 0.55 - 0.2 * (y == 2), x, y))
    pt("rf_stock", "all", lambda f, x, y, w, h: pick(tan, 0.5, x, y))
    pt("rf_body", "all", lambda f, x, y, w, h: pick(tan, 0.6 + 0.12 * (x == 0) - 0.15 * (y % 20 == 0), x, y))
    pt("rf_barrel", "all", lambda f, x, y, w, h: pick(tan, 0.55 + 0.15 * (y % 16 == 0), x, y))
    pt("rf_brake", "all", lambda f, x, y, w, h: pick(dark, 0.5 + 0.3 * (y % 4 < 2), x, y))
    pt("rf_scope", "all", lambda f, x, y, w, h: pick(dark, 0.45, x, y))
    pt("rf_mag", "all", lambda f, x, y, w, h: pick(black, 0.45, x, y))
    pt("rf_bipod", "all", lambda f, x, y, w, h: pick(black, 0.5, x, y))


def build_hook(r, g, d):
    _build2(r, g, d)
    if d.get("manako"):
        cx, ky, kz = r.b["shin_r"]["pivot"]                  # the knee plates
        r.add("kneeplate_r", "shin_r", [cx, ky, kz], [cube("mknee", [cx - 1.125, ky - 1.5, kz - 1.75])], mirror=True)
        hw = G.S("hips")[0]                                  # a pouch on each hip
        r.put("body", cube("mpouch", [-hw / 2 - 1.1, 10.75, -0.75 + g.z]), cube("mpouch", [hw / 2 - 0.15, 10.75, -0.75 + g.z]))
        px, py, pz = r.b["hand_r"]["pivot"]                  # the rifle upright in her right hand, muzzle past her head, leaning away from her
        grip = [px, py - 1.5, pz]
        held = lambda p, o: G.rot_cube(p, o, grip, -6, 0, 13)
        x, z = px - 0.25, pz - 2.0
        r.add("hair_mrifle", "hand_r", grip, [held("rf_stock", [x - 0.375, py - 7.5, z]), held("rf_body", [x - 0.5, py - 4.0, z - 0.25]), held("rf_barrel", [x, py + 6.0, z + 0.5]),
                                              held("rf_brake", [x - 0.25, py + 18.0, z + 0.25]), held("rf_scope", [x, py + 0.5, z + 2.0]), held("rf_mag", [x, py - 3.5, z - 2.5]),
                                              held("rf_bipod", [x - 0.75, py + 8.0, z + 0.75]), held("rf_bipod", [x + 1.0, py + 8.0, z + 0.75])])


if __name__ == "__main__":
    F.run_family("devil", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
    want = [a for a in sys.argv[1:] if not a.startswith("--")]
    for m, base in HOMAGE_MODELS.items():                # after their species: their texture is the one the game draws for that look
        if want and m not in want and base not in want:
            continue
        d = SPECIES[m]
        F.declare(m, d, declare_hook(m, d))
        geo, adoc, texes = G.write(m, F.paint(m, d, F.pal_for(d), paint_hook), F.build(m, d, build_hook), F.anims(d, lambda an: anim_extra(an, m, d)), [], None)
        if "--previews" in sys.argv:
            G.previews(m, geo, adoc, texes, F.VIEWS, F.FACE, G.model_height(geo))
