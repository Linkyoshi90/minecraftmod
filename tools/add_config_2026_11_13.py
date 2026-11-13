#!/usr/bin/env python3
"""Adds the 2026-11-13 run's config switches to HarmontideConfig.java (after the squad). Can be run twice."""
import os

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PATH = os.path.join(REPO, "src", "main", "java", "com", "linkyoshi90", "harmontide", "HarmontideConfig.java")

OPTIONS = [
    ("WIFE_BLESSINGS", "wifeBlessings", "Wives bless the land: each wife near her husband's hearth brings the home her kind's blessing (crops grow, herds thrive, fishing luck, monsters weakened, haste, gentle regeneration)."),
    ("DEAD_REALM", "deadRealm", "The realm of the dead: a wild lich or koschei seeds grave-grass that spreads in the dark, keeps twilight mist, and turns zombies and skeletons that linger on it. Sunlight, light and holy water push it back."),
    ("SABBATH", "sabbath", "The Sabbath: realm deeds earn standing with the witches' Sabbath (ranks, a witch familiar, Sabbath trades and a title), the realm-side mirror of the Order's notoriety."),
    ("SEA_WEDDINGS", "seaWeddings", "Sea weddings: a wedding to a sea kind held by the sea, blessed by a sea bishop, gives water breathing near the ocean; a merfolk close friend may once give mermaid blood (a lasting extra heart)."),
    ("GHOST_POSSESSION", "ghostPossession", "Ghost possession: a ghost friend can slip into you for a minute (night vision, monsters lose track of you, a faint glow); wild ghosts do it as a prank."),
    ("COOKING", "cooking", "Every girl has her own cooking skill: a home-cooked meal she makes for you may be a treat with a buff, or burnt love food (mild sickness, but she is happy you ate it)."),
    ("DULLAHAN_HEADS", "dullahanHeads", "A startled dullahan's head pops off: hand it back for her thanks, or she chases it."),
    ("CURSED_GEAR", "cursedGear", "Cursed gear: living armour and cursed blades found in old places work as gear, whisper, grow fond of whoever uses them, and at full fondness step out as a girl."),
]


def main():
    with open(PATH, encoding="utf-8") as fh:
        text = fh.read()
    if "WIFE_BLESSINGS" in text:
        return
    decl = "".join(f"    public static final ModConfigSpec.BooleanValue {c};\n" for c, _, _ in OPTIONS)
    anchor = "    public static final ModConfigSpec.BooleanValue SQUAD;\n"
    text = text.replace(anchor, anchor + decl, 1)
    build = "".join(f"\n        {c} = BUILDER\n                .comment({repr_java(desc)})\n                .define(\"{k}\", true);\n" for c, k, desc in OPTIONS)
    anchor2 = '                .define("squad", true);\n'
    text = text.replace(anchor2, anchor2 + build, 1)
    getters = "".join(f"\n    public static boolean {k}() {{\n        return SPEC.isLoaded() ? {c}.get() : {c}.getDefault();\n    }}\n" for c, k, _ in OPTIONS)
    anchor3 = "    public static boolean squad() {\n        return SPEC.isLoaded() ? SQUAD.get() : SQUAD.getDefault();\n    }\n"
    assert anchor3 in text
    text = text.replace(anchor3, anchor3 + getters, 1)
    with open(PATH, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(text)


def repr_java(s):
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'


if __name__ == "__main__":
    main()
