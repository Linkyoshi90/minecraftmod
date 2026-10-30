#!/usr/bin/env python3
"""Undead (the restless family, see undead_field_guide.html): living doll, will-o'-wisp, yuki-onna, phantom, vampire,
zombie, Frankenstein's monster.

    python tools/gen_undead.py [species ...] [--previews]

girl_family bipeds, except the will-o'-wisp, who floats on a tail of cold flame (bone `flame`, glowing:
<texture>_glowmask.png). The doll has ball joints painted at knees and elbows, the zombie and the monster stitches
(and the monster neck bolts), the yuki-onna an ice comb, the phantom membrane wings, the vampire small bat wings.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

SPECIES = {
    # twenty-first wave (folklore): the gashadokuro, the towering 'starving skeleton' of Japanese folklore: bone-white, dark hollows
    # round her glowing eyes, long black hair, a tattered robe, the lines of her bones showing through
    "gashadokuro_undead": dict(
        build="strong", outfit=dict(top=["mantle"], bottom="hakama", extras=["arm_wraps"], colors=dict(cloth2="#2a2a30", bandage="#c8c0a8")),
        hair="flowing", bangs="wispy", ears=None, legs="bare", foot="claw", tattered=True, bones=True,
        pal=dict(SKIN="#e8e0cc", HAIR="#1e1a20", FUR="#e8e0cc", LIGHT="#f8f4ec", MARK="#2a2220", EYE="#e8302a", CLOTH="#3a3a44", TRIM="#a8a090",
                 facemark="panda", extra=dict(claw="#e8e0cc")),
        variants={"ash": dict(SKIN="#a8a8a4", FUR="#a8a8a4", LIGHT="#d8d8d4", HAIR="#3a3a3e", EYE="#f8a030", CLOTH="#2a2a2e"),
                  "black": dict(SKIN="#4a4448", FUR="#4a4448", LIGHT="#7a7478", MARK="#0e0a0c", HAIR="#e8e4ec", EYE="#f8f040", CLOTH="#5a1e24"),
                  "blood": dict(SKIN="#e0c8b8", FUR="#e0c8b8", LIGHT="#f4e4d8", MARK="#5a0e10", HAIR="#3a0a0c", EYE="#f8e040", CLOTH="#6a1418"),
                  "jade": dict(SKIN="#b8e0cc", FUR="#b8e0cc", LIGHT="#e0f8ec", MARK="#1e4a3a", HAIR="#14302a", EYE="#5af0c8", CLOTH="#1e3a34")}),
    # twentieth wave (folklore): the ghoul, the grave-haunting ghul of Arabian tales: blue-grey cracked skin, wild teal hair, clawed
    # hands and feet, a torn shift
    "ghoul_undead": dict(
        build="slim", outfit=dict(top=["sports"], bottom="wrap_skirt", extras=["arm_wraps"], colors=dict(cloth2="#2a5a7a", bandage="#7a8a9a")),
        hair="wavy", bangs="messy", ears=None, legs="bare", foot="claw", tattered=True,
        pal=dict(SKIN="#8ab0c8", HAIR="#3a8aa8", FUR="#8ab0c8", LIGHT="#c8e0ec", MARK="#2a4a6a", EYE="#e8f0f8", CLOTH="#2a6a8a", TRIM="#1e3a5a",
                 extra=dict(claw="#2a2a30")),
        variants={"grey": dict(SKIN="#9a9a94", FUR="#9a9a94", HAIR="#4a4a44", LIGHT="#d0d0c8", MARK="#3a3a34", EYE="#e8d040", CLOTH="#5a5a50"),
                  "sand": dict(SKIN="#c8b08a", FUR="#c8b08a", HAIR="#6a4a2a", LIGHT="#ecdcbc", MARK="#6a4a2a", EYE="#e83a2a", CLOTH="#8a6a3a"),
                  "green": dict(SKIN="#8aa88a", FUR="#8aa88a", HAIR="#2a5a3a", LIGHT="#c8e0c8", MARK="#2a4a2a", EYE="#e8e83a", CLOTH="#3a5a3a"),
                  "violet": dict(SKIN="#a89ac8", FUR="#a89ac8", HAIR="#5a3a8a", LIGHT="#e0d8f0", MARK="#3a2a5a", EYE="#f8a8e8", CLOTH="#4a2a6a")}),
    # nineteenth wave: the kasurenijimi, a ghost in a school uniform whose face is smeared out like an old photograph
    "kasurenijimi_undead": dict(
        build="slim", outfit=dict(top=["sweater", "capelet"], bottom="wrap_skirt", extras=["satchel", "scarf"],
                                  colors=dict(cloth2="#1e2a5a", leather="#6a4228", scarf="#c82a2a")),
        hair="flowing", bangs="blunt", ears=None, legs="stockings", foot="shoe", smudge=True,
        pal=dict(SKIN="#f4e8e4", HAIR="#1a1e3a", FUR="#f4e8e4", LIGHT="#ffffff", MARK="#c8ccd4", EYE="#3a4a8a", CLOTH="#f4f4f8", TRIM="#1e2a5a",
                 extra=dict(stocking="#141418", boot="#2a1e18")),
        variants={"black": dict(CLOTH="#1e1e24", TRIM="#e8e8ec", extra=dict(stocking="#141418", boot="#2a1e18", cloth2="#1e1e24")),
                  "grey": dict(CLOTH="#e8e8ec", TRIM="#6a6a74", HAIR="#3a3a44", extra=dict(stocking="#4a4a52", boot="#2a2a30", cloth2="#6a6a74")),
                  "green": dict(TRIM="#2a5a3a", HAIR="#1a2a1e", extra=dict(stocking="#141418", boot="#2a1e18", cloth2="#2a5a3a", scarf="#e8c040")),
                  "red": dict(TRIM="#8a1e24", HAIR="#2a1414", extra=dict(stocking="#141418", boot="#2a1e18", cloth2="#8a1e24", scarf="#1e2a5a"))}),
    # nineteenth wave: the draugr, the barrow-dweller of the sagas, corpse grey with pale glowing eyes, in a tattered dress
    "draugr_undead": dict(
        build="slim", outfit=dict(top=["sweater"], bottom="wrap_skirt", extras=["belt", "arm_wraps"], colors=dict(cloth2="#4a5a7a", bandage="#8a8478")),
        hair="shaggy", bangs="messy", ears=None, legs="bare", foot="shoe", tattered=True,
        pal=dict(SKIN="#a8aca0", HAIR="#8a7e70", FUR="#a8aca0", LIGHT="#d0d4c8", MARK="#5a5e54", EYE="#a8e8f8", CLOTH="#5a6a8a", TRIM="#3a4a6a",
                 extra=dict(boot="#3a3430")),
        variants={"blue": dict(SKIN="#7a8aa8", FUR="#7a8aa8", HAIR="#3a3e4a", EYE="#e8f8ff", CLOTH="#4a4a5a"),
                  "black": dict(SKIN="#5a5a5e", FUR="#5a5a5e", HAIR="#1e1e22", EYE="#e83a3a", CLOTH="#2a2a30", TRIM="#141418"),
                  "bronze": dict(SKIN="#a8a08a", HAIR="#c8a060", EYE="#f8d040", CLOTH="#6a4a2a", TRIM="#c8902a",
                                 extra=dict(boot="#4a3420", cloth2="#c8902a")),
                  "frost": dict(SKIN="#c8d8e4", FUR="#c8d8e4", HAIR="#e8f0f8", EYE="#5ad8f8", CLOTH="#8aa0b8", TRIM="#e8f0f8",
                                extra=dict(boot="#5a6a7a", cloth2="#a8b8c8"))}),
    # seventeenth wave: the yurei, a Japanese ghost in a school uniform, long black hair, her legs fading into a white wisp
    # twenty-fourth wave: the nasnas, half a ghost: pale, her body fading into a wisp where her legs should be
    "nasnas_undead": dict(
        build="curvy", outfit=dict(top=["bandeau"], colors=dict(cloth2="#a8c8e0")),
        lower="none", hair="wavy", bangs="swept", ears=None, wisp=True,
        pal=dict(SKIN="#d8ecf4", HAIR="#a8d0e4", FUR="#c8e4f0", LIGHT="#f0f8fc", MARK="#7ab0cc", EYE="#5a9ac0", CLOTH="#a8c8e0", TRIM="#f0f8fc", extra=dict(flame="#c8e4f0")),
        variants={"grey": dict(SKIN="#e0e0e4", HAIR="#a8a8b0", FUR="#c8c8d0", MARK="#7a7a84", EYE="#5a5a64", CLOTH="#a8a8b0", extra=dict(flame="#d8d8e0")),
                  "gold": dict(SKIN="#f4e8c8", HAIR="#e0c070", FUR="#f0dca0", MARK="#a88a3a", EYE="#a87a1e", CLOTH="#e0c070", extra=dict(flame="#f8ecc0")),
                  "rose": dict(SKIN="#f8e0e4", HAIR="#f0b0c0", FUR="#f8ccd8", MARK="#c87a90", EYE="#c84a6a", CLOTH="#f0b0c0", extra=dict(flame="#fcdce4")),
                  "green": dict(SKIN="#dcf0e0", HAIR="#a8d8b4", FUR="#c8e8d0", MARK="#6aa87a", EYE="#3a8a5a", CLOTH="#a8d8b4", extra=dict(flame="#d0f0d8"))}),
    # twenty-fourth wave: the jiangshi, a hopping corpse in an official's robe and hat, a paper talisman on her brow, her arms held out
    "jiangshi_undead": dict(
        build="slim", outfit=dict(top=["kimono"], colors=dict(obi="#c8a040", kimono="#2a3a7a")),
        hair="bob", bangs="blunt", ears=None, legs="bare", foot="shoe", jiangshi=True,
        pal=dict(SKIN="#d0dcd8", HAIR="#141420", FUR="#2a3a7a", LIGHT="#c8d0d8", MARK="#14204a", EYE="#3a3a4a", CLOTH="#2a3a7a", TRIM="#c8a040",
                 extra=dict(boot="#141420", hat="#141420", paper="#f0d860", ink="#c82a1e")),
        variants={"red": dict(CLOTH="#8a1e1e", extra=dict(boot="#141420", hat="#141420", paper="#f0d860", ink="#2a2a2a", kimono="#8a1e1e")),
                  "green": dict(CLOTH="#2a6a4a", extra=dict(boot="#141420", hat="#141420", paper="#f0d860", ink="#c82a1e", kimono="#2a6a4a")),
                  "black": dict(CLOTH="#1e1e24", TRIM="#a8a8b0", extra=dict(boot="#141420", hat="#2a2a30", paper="#f0d860", ink="#c82a1e", kimono="#1e1e24")),
                  "gold": dict(CLOTH="#a87a1e", TRIM="#2a3a7a", extra=dict(boot="#141420", hat="#141420", paper="#f4f0e0", ink="#c82a1e", kimono="#a87a1e"))}),
    "yurei_undead": dict(
        build="slim", outfit=dict(top=["sweater"], bottom="wrap_skirt", extras=["satchel"], colors=dict(cloth2="#c82a2a", leather="#7a4a2a")),
        lower="none", hair="hime", bangs="blunt", ears=None, wisp=True,
        pal=dict(SKIN="#f4e8e4", HAIR="#141218", FUR="#f4f4f6", LIGHT="#ffffff", MARK="#c8ccd4", EYE="#c82a2a", CLOTH="#f4f4f6", TRIM="#c82a2a",
                 extra=dict(flame="#f0f2f6")),
        variants={"navy": dict(TRIM="#2a3a6a", extra=dict(flame="#f0f2f6", cloth2="#2a3a6a")),
                  "white": dict(CLOTH="#f8f8f8", TRIM="#e8e8ec", EYE="#5a6a8a", extra=dict(flame="#ffffff", cloth2="#f0f0f4")),
                  "pale": dict(SKIN="#e0ecf0", HAIR="#3a3a48", EYE="#3ac8c8", extra=dict(flame="#c8f0f0", cloth2="#2a6a6a")),
                  "plum": dict(HAIR="#2a1a2a", EYE="#a83ac8", TRIM="#6a2a8a", extra=dict(flame="#e8d8f4", cloth2="#6a2a8a"))}),
    "living_doll_undead": dict(
        build="petite", outfit=dict(top=["corset", "capelet"], bottom="wrap_skirt", extras=["pearls"], colors=dict(cloth2="#1e1a22")),
        hair="drills", bangs="blunt", ears=None, legs="stockings", foot="shoe", doll=True,
        pal=dict(SKIN="#f8eeea", HAIR="#e8c870", FUR="#f8eeea", LIGHT="#ffffff", MARK="#b8a8a0", EYE="#4a7ac8", CLOTH="#1e1a22", TRIM="#e8e0f0",
                 extra=dict(stocking="#f0eaf0", boot="#1e1a22")),
        variants={"gothic": dict(HAIR="#1e1a22", EYE="#a82a3a", CLOTH="#2a1a2a", TRIM="#a82a3a"),
                  "rose": dict(HAIR="#f0c8a0", CLOTH="#e88aa8", TRIM="#fff0f4", EYE="#6a4a8a", extra=dict(stocking="#ffffff", boot="#e88aa8")),
                  "blue": dict(HAIR="#f4e090", CLOTH="#4a7ad8", TRIM="#ffffff", extra=dict(stocking="#ffffff", boot="#2a3a6a")),
                  "antique": dict(SKIN="#ecd8c0", HAIR="#a8804a", CLOTH="#8a6a4a", TRIM="#e8d8b8", EYE="#5a4a3a"),
                  # homage to Gregory Horror Show (2026-10-25): the Lost Doll, red braids and a patched green dress, Katie's face on the back
                  "lost": dict(SKIN="#f0b0a8", HAIR="#e0503a", CLOTH="#2e7a4a", TRIM="#3a9a5a", EYE="#7a3a9a",
                               extra=dict(stocking="#f0b0a8", boot="#e05040", cloth2="#2e7a4a", lost=True))}),
    "mummy_undead": dict(
        build="slim", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["usekh"], colors=dict(bandage="#ece4cc", cloth2="#ece4cc")),
        hair="hime", bangs="blunt", ears=None, legs="bare", foot="shoe", mummy=True,
        pal=dict(SKIN=skin("tan"), HAIR="#1a1614", FUR="#ece4cc", LIGHT="#fff8e8", MARK="#b8a888", EYE="#5ab0e8", CLOTH="#ece4cc", TRIM="#e8c050",
                 extra=dict(boot="#ece4cc", wrap="#ece4cc")),
        variants={"aged": dict(extra=dict(boot="#d8c89a", wrap="#d8c89a", bandage="#d8c89a", cloth2="#d8c89a")),
                  "royal": dict(TRIM="#3a5ac8", EYE="#e8c050", extra=dict(boot="#f4ecd8", wrap="#f4ecd8", gilded=True)),
                  "sand": dict(SKIN=skin("brown"), extra=dict(boot="#c8b088", wrap="#c8b088", bandage="#c8b088", cloth2="#c8b088")),
                  "night": dict(SKIN=skin("grey"), EYE="#7af0ff", extra=dict(boot="#a8a8b0", wrap="#a8a8b0", bandage="#a8a8b0", cloth2="#a8a8b0"))}),
    "will_o_wisp_undead": dict(
        build="slim", outfit=dict(top=["halter"], extras=["armlets"]), lower="none",
        hair="flowing", bangs="wispy", ears=None, wisp=True, glow=["flame_a", "flame_b", "flame_c", "flame_tip", "hair_cap", "strand_l1"],
        pal=dict(SKIN="#c8e8f8", HAIR="#6ac8f8", FUR="#4aa8f0", LIGHT="#e8f8ff", MARK="#2a6ad8", EYE="#e8f8ff", CLOTH="#2a4a8a", TRIM="#a8e8ff",
                 extra=dict(flame="#5ab8f8")),
        variants={"green": dict(SKIN="#c8f0d0", HAIR="#6af0a0", CLOTH="#1e5a3a", TRIM="#a8f8c8", extra=dict(flame="#5af090")),
                  "violet": dict(SKIN="#e0d0f8", HAIR="#b88af8", CLOTH="#4a2a8a", TRIM="#e0c8ff", extra=dict(flame="#b07af8")),
                  "amber": dict(SKIN="#f8e8c8", HAIR="#f8b04a", CLOTH="#8a4a1a", TRIM="#ffe0a0", extra=dict(flame="#f8a83a")),
                  "soul": dict(SKIN="#c8f8f8", HAIR="#4af0f0", CLOTH="#1a4a5a", TRIM="#a8ffff", extra=dict(flame="#3ae8f0")),
                  # homage (2026-10-30): Boo: all white, dark eyes, a pink tongue's colour in her trim
                  "shy_ghost": dict(SKIN="#fbfbfb", HAIR="#f8f8f8", FUR="#f0f0f4", LIGHT="#ffffff", MARK="#c8c8d0", EYE="#1e1e24", CLOTH="#f4f4f8", TRIM="#f08ab0", extra=dict(flame="#f8f8fc"))}),
    "yuki_onna_undead": dict(
        build="slim", outfit=dict(top=["kimono"], bottom="hakama", colors=dict(kimono="#f4f8fc", kimono_trim="#a8c8e8", obi="#a8c8e8", cloth2="#eaf2fa")),
        hair="hime", bangs="blunt", ears=None, legs="bare", foot="shoe", comb=True,
        pal=dict(SKIN="#eef4fa", HAIR="#1a1c24", FUR="#eef4fa", LIGHT="#ffffff", MARK="#a8c8e8", EYE="#8ab8e8", CLOTH="#f4f8fc", TRIM="#a8c8e8",
                 extra=dict(boot="#e8eef4")),
        variants={"frost": dict(HAIR="#a8d0f0", extra=dict(kimono="#dceefa", obi="#6aa0d8")),
                  "lilac": dict(HAIR="#3a2a4a", extra=dict(kimono="#e8dcf4", obi="#9a7ac8", kimono_trim="#9a7ac8")),
                  "silver": dict(HAIR="#d8dce4", EYE="#c8d8f0"),
                  "crimson": dict(HAIR="#1a1416", extra=dict(obi="#c8283a", kimono_trim="#c8283a"), EYE="#c8283a"),
                  # homage (2026-10-28): Froslass (Pokemon): white snow kimono, a red-purple sash, a pale blue face, purple eyes
                  "snow_wraith": dict(SKIN="#cfe0f4", FUR="#cfe0f4", HAIR="#eef4fc", EYE="#8a3ad8", MARK="#5a7ab8",
                                      extra=dict(kimono="#f8fbff", kimono_trim="#6a8ac8", obi="#a8286a", cloth2="#eef4fc", boot="#d8e4f4"))}),
    "phantom_undead": dict(
        build="slim", outfit=dict(top=["sports"], bottom="shorts", extras=["arm_wraps"], colors=dict(bandage="#4a5468")),
        hair="shaggy", bangs="messy", ears=None, legs="bare", foot="shoe", wings="bat", wing_scale=1.15, flies=True,
        pal=dict(SKIN="#8a9ab8", HAIR="#3a4a6a", FUR="#4a5a7a", LIGHT="#b8c8e0", MARK="#2a3448", EYE="#9af05a", CLOTH="#2a3448", TRIM="#9af05a",
                 extra=dict(wing="#4a5a8a", wing_bone="#2a3448", boot="#2a3448")),
        variants={"grey": dict(SKIN="#a0a4ac", HAIR="#4a4c52", extra=dict(wing="#6a6c74", wing_bone="#3a3c42", boot="#3a3c42")),
                  "violet": dict(SKIN="#a090c0", HAIR="#4a3a6a", extra=dict(wing="#6a4a9a", wing_bone="#3a2a52", boot="#3a2a52")),
                  "teal": dict(SKIN="#80aab0", HAIR="#2a5a5a", extra=dict(wing="#3a7a7a", wing_bone="#1e4040", boot="#1e4040")),
                  "crimson": dict(SKIN="#b09098", HAIR="#5a1a2a", EYE="#f0d040", extra=dict(wing="#8a2a3a", wing_bone="#4a1420", boot="#4a1420"))}),
    "vampire_undead": dict(
        build="curvy", outfit=dict(top=["corset", "capelet"], bottom="wrap_skirt", extras=["pearls"], colors=dict(cloth2="#1a1418")),
        hair="wavy", bangs="parted", ears="elf", legs="stockings", foot="shoe", wings="devil_small", fangs=True,
        pal=dict(SKIN="#f4e8ea", HAIR="#1a1418", FUR="#1a1418", LIGHT="#ffffff", MARK="#8a1a2a", EYE="#c81a2a", CLOTH="#8a1a2a", TRIM="#e8c060",
                 extra=dict(wing="#3a1420", wing_bone="#1a0a10", stocking="#1a1418", boot="#1a1418")),
        variants={"noble": dict(CLOTH="#4a2a6a", TRIM="#e8c060", HAIR="#2a1a2a", extra=dict(wing="#2a1a3a", wing_bone="#140a1a")),
                  "silver": dict(HAIR="#d8dce4", CLOTH="#2a2a3a", EYE="#e83a4a"),
                  "raven": dict(CLOTH="#141216", TRIM="#8a8a90", HAIR="#0e0c10"),
                  "pale": dict(CLOTH="#f0e8ea", TRIM="#c81a2a", HAIR="#f4f0f2", extra=dict(stocking="#ffffff", boot="#c81a2a")),
                  # homage (2026-10-30): Alucard: silver hair, a black and gold coat
                  "night_heir": dict(HAIR="#e8e8ec", CLOTH="#141216", TRIM="#e8c060", EYE="#e8c040")}),
    "zombie_undead": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts", extras=["arm_wraps"], colors=dict(bandage="#c8c0a8")),
        hair="shaggy", bangs="messy", ears=None, legs="bare", foot="shoe", stitches=True, cone_kit=True,
        pal=dict(SKIN="#a8c0a0", HAIR="#3a4a3a", FUR="#a8c0a0", LIGHT="#d8e8d0", MARK="#4a2a3a", EYE="#e8302a", CLOTH="#3a6a8a", TRIM="#c8c0a8",
                 extra=dict(boot="#3a3028")),
        variants={"husk": dict(SKIN="#c8b088", HAIR="#6a5038", CLOTH="#8a6a4a"),
                  "drowned": dict(SKIN="#7ab0b0", HAIR="#2a5a5a", CLOTH="#4a7a6a", EYE="#5af0f0"),
                  "pale": dict(SKIN="#d0d0d4", HAIR="#2a2a2e", CLOTH="#5a3a5a"),
                  "patchy": dict(SKIN="#b8b098", HAIR="#8a3a2a", CLOTH="#3a5a3a"),
                  # homage (2026-10-30): the Plants vs. Zombies zombie: grey-green, a brown suit, a red tie, a traffic cone on her head
                  "lawn_zombie": dict(SKIN="#9ab08a", HAIR="#4a3e30", CLOTH="#6a4a2a", TRIM="#c8281e", EYE="#f0e8d0", extra=dict(boot="#2a2018", cone=True))}),
    "frankenstein_undead": dict(
        build="strong", outfit=dict(top=["sweater"], bottom="wrap_skirt", extras=["belt"], colors=dict(cloth2="#2a2a32")),
        hair="bob", bangs="blunt", ears=None, legs="bare", foot="shoe", stitches=True, bolts=True,
        pal=dict(SKIN="#a8c8a0", HAIR="#1a1c1e", FUR="#a8c8a0", LIGHT="#d0e8c8", MARK="#3a3a3a", EYE="#e8d040", CLOTH="#5a4a6a", TRIM="#8a8a8a",
                 extra=dict(boot="#2a2a2e")),
        variants={"grey": dict(SKIN="#b0b4b0", CLOTH="#4a4a52"),
                  "blue": dict(SKIN="#a8b8d0", CLOTH="#3a3a5a"),
                  "pale": dict(SKIN="#e0dcd0", CLOTH="#6a4a3a"),
                  "patchwork": dict(SKIN="#b8c0a0", CLOTH="#7a5a4a", HAIR="#d8d0c0")}),
    "banshee_undead": dict(
        build="slim", outfit=dict(top=["sweater", "corset", "capelet"], bottom="hakama", colors=dict(cloth2="#5a6a5e")),
        hair="flowing", bangs="wispy", ears=None, legs="bare", foot="shoe", comb=True,
        pal=dict(SKIN="#e8ecee", HAIR="#d8dce4", FUR="#e8ecee", LIGHT="#ffffff", MARK="#8a9098", EYE="#c83a3a", CLOTH="#6a6e78", TRIM="#c8ccd4",
                 extra=dict(boot="#3a3c42", comb="#d8dce4")),
        variants={"green": dict(HAIR="#a8542a", CLOTH="#3a6a4a", extra=dict(boot="#2a3a2a", comb="#d8dce4", cloth2="#2e4a36")),
                  "red": dict(HAIR="#c83a2a", CLOTH="#2a2a32", extra=dict(boot="#1a1a1e", comb="#d8dce4", cloth2="#3a3a46")),
                  "mist": dict(SKIN="#f2f4f6", HAIR="#f4f6f8", CLOTH="#c8ccd4", EYE="#8aa8c8", extra=dict(boot="#a8acb4", comb="#ffffff", cloth2="#dce0e6")),
                  "raven": dict(HAIR="#141218", CLOTH="#1e1c24", extra=dict(boot="#141218", comb="#d8dce4", cloth2="#2a2834"))}),
    "dullahan_undead": dict(
        build="athletic", outfit=dict(top=["corset", "capelet"], bottom="hakama", extras=["pauldron", "bracers", "belt"], colors=dict(cloth2="#2a2a32")),
        hair="ponytail", bangs="parted", ears=None, legs="bare", foot="shoe", headless=True, glow=["stump"], envoy_kit=True,
        pal=dict(SKIN="#e8e4e8", HAIR="#d8dce4", FUR="#e8e4e8", LIGHT="#ffffff", MARK="#5a5a68", EYE="#5ac8f0", CLOTH="#2a2a36", TRIM="#a8b0c0",
                 extra=dict(boot="#1e1e24", flame="#5ac8f8")),
        variants={"crimson": dict(CLOTH="#5a141e", TRIM="#c8a050", extra=dict(boot="#1e0e10", flame="#5ac8f8", cloth2="#3a0e14")),
                  "ash": dict(HAIR="#8a8a90", CLOTH="#4a4a52", TRIM="#8a8a90", extra=dict(boot="#2a2a2e", flame="#a8e0ff", cloth2="#3a3a40")),
                  "gold": dict(HAIR="#e8d090", CLOTH="#1e1e2a", TRIM="#e8c050", extra=dict(boot="#1a1a1e", flame="#f8d070", cloth2="#2a2a36")),
                  "pale": dict(HAIR="#f4f4f8", CLOTH="#c8ccd4", TRIM="#5a6a8a", EYE="#8a6ad8", extra=dict(boot="#8a8e98", flame="#b08af8", cloth2="#a8acb4")),
                  # homage (2026-10-28): Celty Sturluson (Durarara!!): a black rider's suit with yellow trim, black shadow at the neck
                  "headless_rider": dict(HAIR="#2a2a30", CLOTH="#16161a", TRIM="#e8c020", EYE="#e8c020",
                                         extra=dict(boot="#16161a", flame="#3a3a44", cloth2="#16161a", rider=True)),
                  # homage (2026-10-28): Lala (Monster Musume): silver-white hair, red eyes, a black and red gothic dress, blue flame at the neck
                  "death_envoy": dict(HAIR="#e8e8f0", CLOTH="#1a1418", TRIM="#a8182a", EYE="#d8182a",
                                      extra=dict(boot="#1a1418", flame="#4a9af8", cloth2="#5a0e18", scythe=True))}),
    "llorona_undead": dict(
        build="slim", outfit=dict(top=["corset", "capelet"], bottom="hakama", extras=["pearls"], colors=dict(cloth2="#e8e0d8", leather="#d8d0c8")),
        hair="flowing", bangs="wispy", ears=None, legs="bare", foot="shoe",
        pal=dict(SKIN="#e8dcd8", HAIR="#141214", FUR="#e8dcd8", LIGHT="#ffffff", MARK="#7a9ac8", EYE="#5a8ac8", CLOTH="#f4f0ec", TRIM="#c8c0b8",
                 facemark="tear", extra=dict(boot="#c8c0b8")),
        variants={"mourning": dict(CLOTH="#1e1a22", TRIM="#5a5060", extra=dict(boot="#141216", cloth2="#2a2430", leather="#2a2430")),
                  "rose": dict(CLOTH="#e8b8c0", TRIM="#a85a6a", extra=dict(boot="#a85a6a", cloth2="#f0c8d0", leather="#c88a98")),
                  "river": dict(CLOTH="#9ab0c0", TRIM="#4a6a7a", HAIR="#2a3a44", extra=dict(boot="#4a6a7a", cloth2="#b8c8d4", leather="#6a8494")),
                  "marigold": dict(CLOTH="#f0a030", TRIM="#7a2a5a", HAIR="#1a1014", extra=dict(boot="#7a2a5a", cloth2="#f8c050", leather="#c86a20"))}),
    "lich_undead": dict(
        build="slim", outfit=dict(top=["corset", "mantle"], bottom="hakama", extras=["pearls", "bracers"], colors=dict(cloth2="#1a1628", leather="#3a2a5a")),
        hair="flowing", bangs="wispy", ears=None, legs="bare", foot="shoe", lich=True,
        pal=dict(SKIN="#c8d0d8", HAIR="#e8eef4", FUR="#c8d0d8", LIGHT="#ffffff", MARK="#4a5a6a", EYE="#5af0f8", CLOTH="#2a2440", TRIM="#8af0f8",
                 extra=dict(boot="#1a1628", crown="#c8a040", gem="#5af0f8")),
        variants={"bone": dict(SKIN="#e8dcc8", HAIR="#f4ecdc", EYE="#f8d050", CLOTH="#3a3020", TRIM="#f8d050", extra=dict(boot="#2a2418", crown="#a88a50", gem="#f8d050")),
                  "crimson": dict(CLOTH="#5a1420", TRIM="#f83a3a", EYE="#f83a3a", extra=dict(boot="#2a0a10", crown="#c8a040", gem="#f83a3a")),
                  "verdant": dict(CLOTH="#1e3a24", TRIM="#5af07a", EYE="#5af07a", extra=dict(boot="#0e1e12", crown="#8a9a5a", gem="#5af07a")),
                  "void": dict(SKIN="#b8b0c8", CLOTH="#14101e", TRIM="#b05af8", EYE="#b05af8", HAIR="#d0c8e8", extra=dict(boot="#0a0810", crown="#4a4060", gem="#b05af8"))}),
}


def declare_hook(sp, d):
    out = []
    if d.get("jiangshi"):                                # the official's hat and the paper talisman
        out += [("jhat", 9.5, 2, 9.5), ("jhat_top", 6, 1.75, 6), ("jtalisman", 2.5, 5.5, .25)]
    if d.get("cone_kit"):                                # the lawn zombie's traffic cone: base, two tiers, the tip
        out += [("cone_base", 7, 1, 7), ("cone_a", 5, 3, 5), ("cone_b", 3.5, 3, 3.5), ("cone_tip", 2, 2, 2)]
    if d.get("wisp"):
        out += [("flame_a", 6, 4, 4.5), ("flame_b", 4.5, 4, 3.5), ("flame_c", 3, 3.5, 2.5), ("flame_tip", 1.5, 3, 1.5), ("lick", 1, 3.5, 1)]
    if d.get("comb"):
        out += [("comb", 3, 1.5, .5), ("crystal", 1, 1.5, 1)]
    if d.get("bolts"):
        out += [("bolt", 1.25, .75, .75)]
    if d.get("headless"):
        out += [("stump", 3.5, 1.5, 3)]
    if d.get("envoy_kit"):                               # the homage props, drawn only on their own looks: Celty's cat-eared helmet over the
        out += [("rhelm", 9, 9, 9), ("rhelm_ear", 2, 2.5, 1.25),         # head she carries, Lala's scythe in her free hand
                ("sc_shaft", .75, 20, .75), ("sc_blade", .5, 1.75, 6.5), ("sc_tip", .5, 1.25, 2.5)]
    if d.get("lich"):
        out += [("crown", 4.5, 1, 4.5), ("crown_spike", .75, 2, .5), ("crown_gem", 1, 1, .25)]
    if d.get("doll"):
        out += [("katie", 8, 8, .25), ("doll_bow", 2.5, 2, .5)]
    if d.get("mummy"):                                   # the bandage layers she unravels: arms and shins (outer), torso and thighs
        a = G.OF.BUILDS[d.get("build", "slim")].get("arm", 1.5)
        t = G.OF.thigh_width(d.get("build"), 2.25)
        out += [("wrap_arm", a + .5, 3.5, a + .5), ("wrap_fore", a + .5, 3, a + .5), ("wrap_shin", t, 6.5, t), ("wrap_thigh", t + .5, 4.5, t + .5),
                ("wrap_torso", 5.5, 6.5, 4)]
    return out


def paint_hook(pt, pal, d):
    if d.get("jiangshi"):
        hat, paper, ink = G.shades(pal.extra.get("hat", "#141420")), G.shades(pal.extra.get("paper", "#f0d860")), G.shades(pal.extra.get("ink", "#c82a1e"))
        pt("jhat", "all", lambda f, x, y, w, h: CLEAR if f == "down" else pick(hat, 0.45 + 0.2 * (y == 0), x, y))
        pt("jhat_top", "all", lambda f, x, y, w, h: pick(hat, 0.4 + 0.15 * (x % 3 == 0), x, y))
        pt("jtalisman", "all", lambda f, x, y, w, h: pick(ink, 0.5, x, y) if 3 <= x < w - 3 and y % 4 < 3 and y > 1 else pick(paper, 0.7, x, y))
    cone = pal.extra.get("cone")
    orange = G.shades("#f0701e")
    for p in ("cone_base", "cone_a", "cone_b", "cone_tip"):
        pt(p, "all", lambda f, x, y, w, h, p=p: H.CLEAR if not cone else pick(G.shades("#f4f4f0"), 0.7, x, y) if p == "cone_b" and f != "up" and y in (1, 2)
           else pick(orange, 0.5 + 0.15 * (f == "up") - 0.1 * (f == "south"), x, y))
    rider, scythe = pal.extra.get("rider"), pal.extra.get("scythe")
    yel, black = G.shades("#e8c020"), G.shades("#16161a")

    def helm(f, x, y, w, h):                             # yellow, a black visor across the front
        if not rider:
            return CLEAR
        if f == "north" and 10 <= y <= 24 and 4 <= x <= w - 5:
            return pick(black, 0.4 + 0.25 * (y == 11 and x > w // 2), x, y)
        return pick(yel, 0.5 + 0.2 * (f == "up") - 0.12 * (f == "down"), x, y)
    pt("rhelm", "all", helm)
    pt("rhelm_ear", "all", lambda f, x, y, w, h: CLEAR if not rider else pick(yel, 0.55 + 0.15 * (f == "north"), x, y))
    steel, wood = G.shades("#c8ccd4"), G.shades("#2a1e22")
    pt("sc_shaft", "all", lambda f, x, y, w, h: CLEAR if not scythe else pick(wood, 0.45 + 0.15 * (y % 6 == 0), x, y))
    pt("sc_blade", "all", lambda f, x, y, w, h: CLEAR if not scythe else
       pick(G.shades("#a8182a"), 0.5, x, y) if y == h - 1 and f in ("east", "west") else pick(steel, 0.55 + 0.2 * (f == "up"), x, y))
    pt("sc_tip", "all", lambda f, x, y, w, h: CLEAR if not scythe else pick(steel, 0.6, x, y))
    if d.get("bones"):                                   # the gashadokuro's bones showing: a groove down each limb, knuckled joints
        groove = lambda f, x, y, w, h: pick(pal.MARK, 0.45, x, y) if f in ("north", "south") and x == w // 2 and y % 4 != 3 else None
        for p in ("arm_up", "forearm", "thigh", "shin"):
            pt(p, ("north", "south"), groove)
    if d.get("smudge"):                                  # her face smeared and scratched out, like an old photograph
        smear = G.shades("#e8e8ec")
        pt("head", ("north",), lambda f, x, y, w, h: G.with_alpha(pick(smear, 0.55 + 0.3 * noise(x, y, 141), x, y), 215)
           if h * 0.38 <= y <= h * 0.82 and w * 0.12 <= x <= w * 0.88 and ((y * 7 + x // 3) % 5 < 2 or noise(x // 2, y, 142) > 0.72) else None)
    if d.get("tattered"):                                # the draugr's grave clothes, torn at the hem
        for p in ("skirt_f", "skirt_b", "skirt_l", "skirt_r", "wrap_f", "wrap_b"):
            pt(p, "all", lambda f, x, y, w, h: CLEAR if y >= h - 2 and noise(x, 0, 143) > 0.45 else None)
    if d.get("wisp"):
        fl = G.shades(pal.extra.get("flame", "#5ab8f8"))

        def flame(f, x, y, w, h):
            t = 0.55 + 0.35 * (y / max(1, h - 1)) + (0.15 if noise(x, y // 2, 11) > 0.6 else 0) - 0.1 * (f in ("east", "west"))
            return G.with_alpha(pick(fl, t, x, y), 210 if y > h * 0.6 else 255)
        for p in ("flame_a", "flame_b", "flame_c", "flame_tip", "lick"):
            pt(p, "all", flame)
    fl = G.shades(pal.extra.get("flame", "#5ac8f8"))                 # the dullahan's neck: a cold spirit flame
    pt("stump", "all", lambda f, x, y, w, h: G.with_alpha(pick(fl, 0.6 + 0.3 * ((x + y) % 2 == 0), x, y), 220))
    ice = G.shades("#a8d8f8")
    comb = G.shades(pal.extra["comb"]) if "comb" in pal.extra else ice   # the banshee's comb is silver
    pt("comb", "all", lambda f, x, y, w, h: pick(comb, 0.6 + 0.3 * (x % 2), x, y))
    pt("crystal", "all", lambda f, x, y, w, h: pick(ice, 0.85, x, y))
    pt("bolt", "all", lambda f, x, y, w, h: pick(G.shades("#8a8c90"), 0.45 + 0.3 * (x == 0), x, y))
    crown = G.shades(pal.extra.get("crown", "#c8a040"))              # the lich's crown: tarnished gold, a soul gem
    pt("crown", "all", lambda f, x, y, w, h: pick(crown, 0.5 + 0.3 * (y == 0), x, y))
    pt("crown_spike", "all", lambda f, x, y, w, h: pick(crown, 0.6 - 0.2 * (x == 0), x, y))
    pt("crown_gem", "all", lambda f, x, y, w, h: pick(G.shades(pal.extra.get("gem", "#5af0f8")), 0.8, x, y))
    if d.get("doll"):                                   # porcelain ball joints at the elbows and knees
        joint = G.shades("#d8c8c0")
        for p in ("forearm", "shin"):
            pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(joint, 0.45, x, y) if y == 0 else None)
    if d.get("stitches"):                               # stitched seams across the skin
        def stitch(f, x, y, w, h):
            if f in ("north", "east", "west") and (y == h // 2) and x % 2 == 0:
                return pick(G.shades("#2a2a2a"), 0.3, x, y)
            if f in ("north", "east", "west") and y in (h // 2 - 1, h // 2 + 1) and x % 2 == 0 and x % 4 == 0:
                return pick(G.shades("#2a2a2a"), 0.35, x, y)
            return None
        for p in ("arm_up", "thigh", "midriff"):
            pt(p, ("north", "east", "west"), stitch)
        pt("head", ("north",), lambda f, x, y, w, h: pick(G.shades("#3a3a3a"), 0.3, x, y) if (x == 3 or x == 4) and 6 <= y <= 13 and y % 2 == 0 else None)
    if d.get("doll"):                                   # the Lost Doll's things, drawn only on her
        lost = pal.extra.get("lost")
        blue, white, gold, red, ink = G.shades("#2ea8d8"), G.shades("#f8f8f0"), G.shades("#f0c030"), G.shades("#d83a2a"), G.shades("#1a1a1a")

        def katie(f, x, y, w, h):                       # her other face, on the back of her head
            if not lost:
                return CLEAR
            if f != "south":
                return pick(blue, 0.45, x, y)
            for cx in (w * 0.28, w * 0.72):             # big round eyes
                d2 = (x + 0.5 - cx) ** 2 + (y + 0.5 - h * 0.38) ** 2
                if d2 < (w * 0.17) ** 2:
                    return pick(red if d2 < (w * 0.05) ** 2 else gold if d2 < (w * 0.1) ** 2 else white, 0.55, x, y)
            if h * 0.66 <= y < h * 0.82 and w * 0.22 <= x < w * 0.78:   # a row of zigzag teeth, white on red
                k = (y - int(h * 0.66)) * 2
                return pick(white if abs(x % 6 - 3) * 1.6 >= k else red, 0.55, x, y)
            return pick(blue, 0.5 + 0.12 * ((x * 3 + y) % 7 == 0), x, y)
        pt("katie", "all", katie)
        pt("doll_bow", "all", lambda f, x, y, w, h: pick(G.shades("#5a4aa8"), 0.5 + 0.2 * (x in (0, w - 1)), x, y) if lost else CLEAR)
        if lost:
            freck, jaw = G.shades("#e07a90"), G.shades("#c87a72")
            pt("head", ("north",), lambda f, x, y, w, h: pick(freck, 0.5, x, y) if (x, y) in {(13, 17), (15, 16), (17, 17), (19, 16), (14, 19), (18, 19)}
               else pick(jaw, 0.4, x, y) if ((x in (int(w * 0.3), int(w * 0.7)) and y >= int(h * 0.64)) or (y == int(h * 0.64) and int(w * 0.3) <= x <= int(w * 0.7)))
               else None)                               # freckles, and the line of her puppet jaw
            yellow, mint, thread = G.shades("#f4c86a"), G.shades("#7ec06a"), G.shades("#14503a")

            def patched(f, x, y, w, h):                 # patches sewn onto her dress
                if f != "north":
                    return None
                for x0, x1, y0, y1, col in ((0.12, 0.42, 0.15, 0.55, mint), (0.58, 0.9, 0.35, 0.8, yellow)):
                    if w * x0 <= x < w * x1 and h * y0 <= y < h * y1:
                        edge = x in (int(w * x0), int(w * x1) - 1) or y in (int(h * y0), int(h * y1) - 1)
                        return pick(thread, 0.4, x, y) if edge and (x + y) % 2 == 0 else pick(col, 0.55, x, y)
                return None
            for p in ("corset", "panel_f", "cape_b", "chest"):
                pt(p, ("north",), patched)
    if d.get("mummy"):
        wrap = G.shades(pal.extra.get("wrap", "#ece4cc"))
        gilt = G.shades("#e8c050")

        def bandage(f, x, y, w, h):
            if f in ("up", "down"):
                return CLEAR
            if pal.extra.get("gilded") and y % 9 == 4:
                return pick(gilt, 0.6, x, y)
            return pick(wrap, 0.25 if (2 * x + 5 * y) % 11 == 0 else 0.6 + 0.12 * ((y // 3) % 2), x, y)
        for p in ("wrap_arm", "wrap_fore", "wrap_shin", "wrap_thigh", "wrap_torso"):
            pt(p, "all", bandage)
        pt("head", ("south", "east", "west", "up"), lambda f, x, y, w, h: bandage(f if f != "up" else "north", x, y, w, h) if y > h * 0.55 or f == "up" else None)
        for p in ("thigh", "shin", "arm_up", "forearm"):          # thin wraps on her skin under the layers
            pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(wrap, 0.55, x, y) if (x + 2 * y) % 6 < 2 else None)
    if d.get("fangs"):
        pt("mouth", ("north",), lambda f, x, y, w, h: pal.WHITE if y == 0 and x in (1, w - 2) else None)


def build_hook(r, g, d):
    if d.get("jiangshi"):                                # the hat on her head, the talisman hanging from its brim over her face
        r.add("hair_jhat", "head", [0, g.hy + 8, g.z], [cube("jhat", [-4.75, g.hy + 7.25, -4.75 + g.z]), cube("jhat_top", [-3, g.hy + 9.0, -3 + g.z]),
                                                        cube("jtalisman", [-1.25, g.hy + 2.75, -4.6 + g.z])])
    hy, z = g.hy, g.z
    if d.get("cone_kit"):                                # on top of her head, a white band round the middle
        r.add("hair_cone", "head", [0, hy + 8, z], [cube("cone_base", [-3.5, hy + 7.8, -3.5 + z]), cube("cone_a", [-2.5, hy + 8.8, -2.5 + z]),
                                                    cube("cone_b", [-1.75, hy + 11.8, -1.75 + z]), cube("cone_tip", [-1, hy + 14.8, -1 + z])])
    if d.get("envoy_kit"):                               # the helmet over her (carried) head; the scythe upright in her left fist
        r.add("rider_helm", "head", [0, hy + 4, z], [cube("rhelm", [-4.5, hy - 0.5, -4.5 + z]),
                                                    G.rot_cube("rhelm_ear", [-4.0, hy + 8.2, -0.6 + z], [-3.0, hy + 8.4, z], 0, 0, 15),
                                                    G.rot_cube("rhelm_ear", [2.0, hy + 8.2, -0.6 + z], [3.0, hy + 8.4, z], 0, 0, -15)])
        px, py, pz = r.b["hand_r"]["pivot"]
        sx, sz = -px + 0.375 - 0.375, pz - 1.0
        top = py - 6.0 + 20
        r.add("scythe", "hand_l", [-px, py - 1.5, pz], [cube("sc_shaft", [sx - 0.375, py - 6.0, sz - 0.375]),
                                                        cube("sc_blade", [sx - 0.25, top - 1.75, sz + 0.25]),
                                                        G.rot_cube("sc_tip", [sx - 0.25, top - 2.9, sz + 5.6], [sx, top - 1.75, sz + 6.5], 35, 0, 0)])
    if d.get("mummy"):                                  # each layer is its own bone, so the game can hide them one at a time
        a = G.OF.BUILDS[d.get("build", "slim")].get("arm", 1.5)
        t = G.OF.thigh_width(d.get("build"), 2.25)
        ax, ay, az = r.b["arm_r"]["pivot"]
        fx, fy, fz = r.b["forearm_r"]["pivot"]
        r.add("wrap_arm_r", "arm_r", [ax, ay, az], [cube("wrap_arm", [ax - (a + .5) / 2, ay - 3.5, az - (a + .5) / 2])], mirror=True)
        r.add("wrap_fore_r", "forearm_r", [fx, fy, fz], [cube("wrap_fore", [fx - (a + .5) / 2, fy - 3, fz - (a + .5) / 2])], mirror=True)
        lx, ly, lz = r.b["leg_r"]["pivot"]
        sx, sy, sz = r.b["shin_r"]["pivot"]
        r.add("wrap_shin_r", "shin_r", [sx, sy, sz], [cube("wrap_shin", [sx - t / 2, sy - 6.5, sz - t / 2])], mirror=True)
        r.add("wrap_thigh_r", "leg_r", [lx, ly, lz], [cube("wrap_thigh", [lx - (t + .5) / 2, ly - 4.5, lz - (t + .5) / 2])], mirror=True)
        r.add("wrap_torso", "body", [0, 16, z], [cube("wrap_torso", [-2.75, 12.6, -2 + z])])
    if d.get("doll"):                                   # Katie on the back of her head (riding on her hair), bows on her curls
        r.put("hair_back", cube("katie", [-4, hy + 0.2, 7.6 + z]))
        r.put("hair_side_r", cube("doll_bow", [-8.4, hy + 5.4, -1.25 + z]))
    if d.get("headless"):                               # blue flame where her neck ends (the head itself is carried, see anim_extra)
        r.put("body", cube("stump", [-1.75, hy - 0.5, -1.5 + z]))
    if d.get("wisp"):
        aw, ah, ad = G.S("flame_a")
        cs = [cube("flame_a", [-aw / 2, 8.0, -ad / 2]), cube("flame_b", [-G.S("flame_b")[0] / 2, 4.25, -G.S("flame_b")[2] / 2 + 0.5]),
              cube("flame_c", [-G.S("flame_c")[0] / 2, 1.0, -G.S("flame_c")[2] / 2 + 1.0]), cube("flame_tip", [-0.75, -1.8, 0.8])]
        for x, zz, rz in ((-2.6, -1.0, 18), (2.6, -0.5, -20), (-1.2, 1.8, 10), (1.8, 1.5, -12)):
            cs.append(G.rot_cube("lick", [x - 0.5, 7.0, zz - 0.5], [x, 10.0, zz], 0, 0, rz))
        r.add("flame", "root", [0, 12, 0], cs)
    if d.get("comb"):
        r.put("head", G.rot_cube("comb", [1.0, hy + 7.4, 1.0 + z], [2.5, hy + 7.4, 1.0 + z], 0, 0, -20), cube("crystal", [3.4, hy + 8.0, 0.75 + z]))
    if d.get("bolts"):
        b = cube("bolt", [-3.4, hy - 0.6, -0.4 + z])
        r.put("body", b, G.mirror_cube(b))
    if d.get("lich"):                                   # a tall, spiked crown with a soul gem at the front
        cs = [cube("crown", [-2.25, hy + 8.3, -2.25 + z])]
        for x, hgt in ((-1.9, 0.0), (-0.95, 0.6), (0, 1.0), (0.95, 0.6), (1.9, 0.0)):
            cs.append(cube("crown_spike", [x - 0.375, hy + 9.3 + hgt, -2.4 + z]))
        cs.append(cube("crown_gem", [-0.5, hy + 8.3, -2.55 + z]))
        r.add("crown", "head", [0, hy + 8.3, z], cs)


def anim_extra(an, sp, d):
    """the wisp's flicker, the jiang shi-like zombie shamble ("shamble") and the vampire's bite ("drink")"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    if d.get("jiangshi"):                                # stiff as a board: arms straight out, and she can only hop
        arms = merge(sym("arm", hold(-88, 0, 0)), sym("forearm", hold(0, 0, 0)), {"hair_back": {"rotation": hold(0, 0, 0)}})
        hop = lambda p, up: {"root": {"position": Tk((0, (0, 0, 0)), (p * 0.15, (0, 0, 0)), (p * 0.55, (0, up, 0)), (p * 0.9, (0, 0, 0)), (p, (0, 0, 0)))},
                             "leg_r": {"rotation": hold(0, 0, 0)}, "leg_l": {"rotation": hold(0, 0, 0)}, "shin_r": {"rotation": hold(0, 0, 0)}, "shin_l": {"rotation": hold(0, 0, 0)}}
        an.add("idle", 3.0, True, merge(arms, hop(3.0, 0.5)))
        an.add("walk", 0.7, True, merge(arms, hop(0.7, 4)))
        an.add("run", 0.5, True, merge(arms, hop(0.5, 5)))
    if d.get("mummy"):                                  # flustered, she spins round and round, wrapping herself up again
        an.add("spin", 1.0, True, {"root": {"rotation": H.track((0, (0, 0, 0)), (0.5, (0, 180, 0)), (1.0, (0, 360, 0)))},
                                   "arm_r": {"rotation": H.hold(-20, 0, 60)}, "arm_l": {"rotation": H.hold(-20, 0, -60)}})
    if d.get("wisp"):
        flick = {"flame": {"rotation": Tk((0, (0, 0, 3)), (0.5, (4, 0, -3)), (1.0, (0, 0, 3))),
                           "scale": Tk((0, (1.0, 1.0, 1.0)), (0.25, (1.06, 0.94, 1.06)), (0.5, (0.96, 1.08, 0.96)), (1.0, (1.0, 1.0, 1.0)))},
                 "root": {"position": Tk((0, (0, 2.0, 0)), (1.0, (0, 2.6, 0)), (2.0, (0, 2.0, 0)))}}
        for key in ("idle", "walk", "run", "sit", "lie", "groom", "hiss", "stalk", "pounce"):
            F._inject(an, key, flick)
    if d.get("headless"):                              # the dullahan carries her head in the crook of her right arm, in every pose
        carry = merge({"head": {"position": hold(-8.0, -12.5, -2.0)}}, {"arm_r": {"rotation": hold(-15, 0, -20)}, "forearm_r": {"rotation": hold(-60, 0, 0)}})
        for k in list(an.A):
            F._inject(an, k.split(".")[-1], carry)
    if d.get("lich"):                                   # cast: both hands thrust forward, then up
        an.add("cast", 1.0, False, merge({"body": {"rotation": Tk((0, (0, 0, 0)), (0.4, (-8, 0, 0)), (1.0, (0, 0, 0)))},
                                          "head": {"rotation": hold(-10, 0, 0)}},
                                         sym("arm", Tk((0, (0, 0, 0)), (0.4, (-100, 0, 15)), (0.7, (-150, 0, 25)), (1.0, (-30, 0, 10))))))
    an.add("drink", 2.0, True, merge({"head": {"rotation": hold(18, 24, 0)}, "body": {"rotation": hold(14, 0, 0)},
                                      "mouth": {"position": hold(*H.OUT_)}, "eyelids": {"position": hold(*H.OUT_)}},
                                     sym("arm", hold(-70, 0, -24)), sym("forearm", hold(-40, 0, 0))))
    an.add("shamble", 1.4, True, merge({"body": {"rotation": Tk((0, (6, 0, 6)), (0.7, (6, 0, -6)), (1.4, (6, 0, 6)))},
                                        "head": {"rotation": hold(10, 0, 14)},
                                        "leg_r": {"rotation": Tk((0, (16, 0, 0)), (0.7, (-16, 0, 0)), (1.4, (16, 0, 0)))},
                                        "leg_l": {"rotation": Tk((0, (-16, 0, 0)), (0.7, (16, 0, 0)), (1.4, (-16, 0, 0)))}},
                                       sym("arm", hold(-85, 0, 0))))
    if d.get("headless"):
        for k in list(an.A):
            F._inject(an, k.split(".")[-1], carry)


if __name__ == "__main__":
    F.run_family("undead", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
