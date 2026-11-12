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
    "message.harmontide.upbringing.farmhand": "%s digs little furrows everywhere with her wooden hoe",
    "message.harmontide.upbringing.cook": "%s stirs pretend soup in her bowl and offers you a taste",
    "message.harmontide.upbringing.guard": "%s waves her wooden sword and vows to protect everyone",
    "message.harmontide.upbringing.scout": "%s follows the compass needle in circles, giggling",
    "message.harmontide.upbringing.enough": "%s already has plenty of those",
    "message.harmontide.story_time": "You read \"%s\" aloud, and %s little ones sit spellbound",
    "message.harmontide.story_time.untitled": "You read aloud, and %s little ones sit spellbound",
    "message.harmontide.upbringing.tomorrow": "%s is still busy with today's toy",
}


def main():
    add_lang(LANG)
    gen_manor_assets.GUIDE_ADD.clear()
    # the toys sentence (" Toys tip the scales, one a day: ...") went in first, then story time after it; append_guides only checks the end
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.roles"] = " Show a little one a written book to read it to all the little ones near."
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
