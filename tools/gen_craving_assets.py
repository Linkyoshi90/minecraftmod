#!/usr/bin/env python3
"""Cravings (2026-11-09, entity/girl/Cravings): lang and a sentence for the treats guide.

    uv run --no-project --with pillow python tools/gen_craving_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402

LANG = {
    "message.monstergirls.craving.tell.0": "%s rubs her belly and sighs: \"I could really go for some %s right now...\"",
    "message.monstergirls.craving.tell.1": "%s tugs your sleeve: \"Do we have any %s? I've been dreaming about it all day\"",
    "message.monstergirls.craving.tell.2": "%s whispers, a little embarrassed: \"I'm craving %s. Lots of it\"",
    "message.monstergirls.craving.fed.0": "%s eats it with her eyes closed: \"That's exactly what we needed\"",
    "message.monstergirls.craving.fed.1": "%s squeals and hugs you, crumbs and all",
    "message.monstergirls.craving.fed.2": "%s pats her belly happily: \"Somebody in here says thank you\"",
}


def main():
    add_lang(LANG)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.treats"] = (
        " A girl who is expecting craves one food and tells you when you come near; bring it to her before her time and she has twins.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
