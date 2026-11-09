#!/usr/bin/env python3
"""Upbringing (2026-11-09, entity/girl/Upbringing): lang and a sentence for the roles guide.

    uv run --no-project --with pillow python tools/gen_upbringing_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402

LANG = {
    "message.monstergirls.upbringing.farmhand": "%s digs little furrows everywhere with her wooden hoe",
    "message.monstergirls.upbringing.cook": "%s stirs pretend soup in her bowl and offers you a taste",
    "message.monstergirls.upbringing.guard": "%s waves her wooden sword and vows to protect everyone",
    "message.monstergirls.upbringing.scout": "%s follows the compass needle in circles, giggling",
    "message.monstergirls.upbringing.enough": "%s already has plenty of those",
    "message.monstergirls.upbringing.tomorrow": "%s is still busy with today's toy",
}


def main():
    add_lang(LANG)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.roles"] = (
        " Toys tip the scales, one a day: a wooden hoe for a farmhand, a bowl for a cook, a wooden sword for a guard, a compass for a scout.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
