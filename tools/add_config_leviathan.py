#!/usr/bin/env python3
"""Adds the Dream-Leviathan's config switches to HarmontideConfig.java (after the 2026-11-27 workshop switch). Can be run twice."""
import os

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PATH = os.path.join(REPO, "src", "main", "java", "com", "linkyoshi90", "harmontide", "HarmontideConfig.java")


def main():
    with open(PATH, encoding="utf-8") as fh:
        text = fh.read()
    if "DREAM_LEVIATHAN" in text:
        return
    anchor = "    public static final ModConfigSpec.BooleanValue WORKSHOP;\n"
    assert anchor in text
    text = text.replace(anchor, anchor + "    public static final ModConfigSpec.BooleanValue DREAM_LEVIATHAN;\n"
                        "    public static final ModConfigSpec.IntValue LEVIATHAN_FIRST_DAY;\n"
                        "    public static final ModConfigSpec.IntValue LEVIATHAN_DAYS;\n", 1)
    anchor2 = '                .define("workshop", true);\n'
    assert anchor2 in text
    build = '''
        DREAM_LEVIATHAN = BUILDER
                .comment("The Dream-Leviathan: once per world a sick sky whale lands near a player; inside her are puzzle rooms and five waves of infection, and touching her heart blesses you with a second heart bar and her egg. A dream horn calls her back. Off: she never comes (a horn does nothing).")
                .define("dreamLeviathan", true);

        LEVIATHAN_FIRST_DAY = BUILDER
                .comment("The day (counted from the world's start) from which the Dream-Leviathan may make her first visit.")
                .defineInRange("leviathanFirstDay", 3, 0, 1000);

        LEVIATHAN_DAYS = BUILDER
                .comment("How many days the landed Dream-Leviathan lasts before she dies, if nobody heals her.")
                .defineInRange("leviathanDays", 3, 1, 30);
'''
    text = text.replace(anchor2, anchor2 + build, 1)
    anchor3 = "    public static boolean workshop() {\n        return SPEC.isLoaded() ? WORKSHOP.get() : WORKSHOP.getDefault();\n    }\n"
    assert anchor3 in text
    getters = '''
    public static boolean dreamLeviathan() {
        return SPEC.isLoaded() ? DREAM_LEVIATHAN.get() : DREAM_LEVIATHAN.getDefault();
    }

    public static int leviathanFirstDay() {
        return SPEC.isLoaded() ? LEVIATHAN_FIRST_DAY.get() : LEVIATHAN_FIRST_DAY.getDefault();
    }

    public static int leviathanDays() {
        return SPEC.isLoaded() ? LEVIATHAN_DAYS.get() : LEVIATHAN_DAYS.getDefault();
    }
'''
    text = text.replace(anchor3, anchor3 + getters, 1)
    with open(PATH, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(text)


if __name__ == "__main__":
    main()
