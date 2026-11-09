#!/usr/bin/env python3
"""The dragon egg hatches (2026-11-09, entity/girl/DragonEgg): lang and an advancement.

    uv run --no-project --with pillow python tools/gen_dragon_egg_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_family_assets import adv  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402

LANG = {
    "message.monstergirls.dragon_egg.warm": "The egg grows warm under the rod... but it wants a dragon's touch. Bring a dragon friend",
    "message.monstergirls.dragon_egg.hatched": "With %s crooning beside it, the egg cracks open: a little ender dragon blinks up at you",
    "advancement.monstergirls.dragon_egg": "Here Be Little Dragons",
    "advancement.monstergirls.dragon_egg.desc": "Hatch the dragon egg with a dragon friend beside you",
}


def main():
    adv("dragon_egg", "monstergirls:dragon_lair", "minecraft:dragon_egg", "challenge")
    add_lang(LANG)


if __name__ == "__main__":
    main()
