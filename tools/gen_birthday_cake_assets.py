#!/usr/bin/env python3
"""Birthday cakes (2026-11-09, entity/girl/BirthdayCake): lang and a sentence for the treats guide.

    uv run --no-project --with pillow python tools/gen_birthday_cake_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402

LANG = {
    "message.harmontide.birthday.cake.0": "%s takes a deep breath and blows out the candle. She won't tell you what she wished for, but she keeps looking at you",
    "message.harmontide.birthday.cake.1": "%s blows the candle out in one go and cheers: \"Best birthday ever!\"",
    "message.harmontide.birthday.cake.2": "%s closes her eyes, makes a wish and puffs the candle out, then hugs everyone in reach",
}


def main():
    add_lang(LANG)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.treats"] = (
        " On her birthday, light a candle on a cake near her: she comes to blow it out, and everyone there is the closer for her wish.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
