#!/usr/bin/env python3
"""Adds the 2026-11-27 run's config switches to HarmontideConfig.java (after cursed gear). Can be run twice."""
import os

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PATH = os.path.join(REPO, "src", "main", "java", "com", "linkyoshi90", "harmontide", "HarmontideConfig.java")

OPTIONS = [
    ("TRAVERSAL", "traversal", "Getting about together: spiderlings and gliderunes on your shoulder, harpies lifting and catching friends, ducks, swans and mermaids towing swimmers, sea lamias diving with a rider, lamia coils, slime bounces, arachne silk bridges and swings, centaurs pulling minecarts."),
    ("BLOCK_JOBS", "blockJobs", "Work about the home: dragons at furnaces and campfires, liches at brewing stands, cattle minotaurs at cheese shelves, bug girls at beehives, rodents planting saplings, arachne at looms, elves at fletching tables, crabs shedding armour pieces."),
    ("DEAD_COMPANY", "deadCompany", "The company of the dead: while a zombie girl walks with you, the undead and creepers leave you alone (until you strike one)."),
    ("INSTINCTS", "instincts", "Little instincts: a minotaur shown a compass points the way home, cat girls get the zoomies at dusk, owl harpies lend you night vision, penguins slide on ice."),
    ("WORKSHOP", "workshop", "Hands at the workbench: elves at enchanting tables, dwarves at anvils, the dinner bell, winged friends catching a fall, a yuki-onna freezing a path over water."),
]


def repr_java(s):
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'


def main():
    with open(PATH, encoding="utf-8") as fh:
        text = fh.read()
    if "BLOCK_JOBS" in text:
        return
    decl = "".join(f"    public static final ModConfigSpec.BooleanValue {c};\n" for c, _, _ in OPTIONS)
    anchor = "    public static final ModConfigSpec.BooleanValue CURSED_GEAR;\n"
    assert anchor in text
    text = text.replace(anchor, anchor + decl, 1)
    build = "".join(f"\n        {c} = BUILDER\n                .comment({repr_java(desc)})\n                .define(\"{k}\", true);\n" for c, k, desc in OPTIONS)
    anchor2 = '                .define("cursedGear", true);\n'
    assert anchor2 in text
    text = text.replace(anchor2, anchor2 + build, 1)
    getters = "".join(f"\n    public static boolean {k}() {{\n        return SPEC.isLoaded() ? {c}.get() : {c}.getDefault();\n    }}\n" for c, k, _ in OPTIONS)
    anchor3 = "    public static boolean cursedGear() {\n        return SPEC.isLoaded() ? CURSED_GEAR.get() : CURSED_GEAR.getDefault();\n    }\n"
    assert anchor3 in text
    text = text.replace(anchor3, anchor3 + getters, 1)
    with open(PATH, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(text)


if __name__ == "__main__":
    main()
