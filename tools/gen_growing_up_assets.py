#!/usr/bin/env python3
"""Growing up (2026-11-09, entity/girl/GrowingUp): lang and a sentence for the nursing guide.

    uv run --no-project --with pillow python tools/gen_growing_up_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402

LANG = {
    "message.harmontide.growing_up.word": "%s points at you and says her very first word: \"%s!\"",
    "message.harmontide.growing_up.flower": "%s toddles over with a flower she picked just for you",
    "message.harmontide.growing_up.drawing": "%s proudly hands you a drawing: the two of you, holding hands",
    "message.harmontide.role.coming_of_age": "%s is all grown up! She hugs you tight, thanks you for raising her, and gives you the first fruit of her life as a %s",
    "message.harmontide.thunder.little": "%s runs to you at the thunder and hides her face in your side",
    "harmontide.configuration.nameNewborns": "Name newborns",
    "item.harmontide.drawing": "%s's Drawing",
    "item.harmontide.drawing.lore": "Two stick figures holding hands: her, and %s",
}


def main():
    add_lang(LANG)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.nursing"] = (
        " Be near as she grows: her first word, a flower she picked and a drawing of you both are moments she saves for you.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
