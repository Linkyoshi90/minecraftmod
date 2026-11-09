#!/usr/bin/env python3
"""The journal's Family guide (2026-11-09): the little ones' features in one place.

    uv run --no-project --with pillow python tools/gen_family_guide.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_milk_assets import add_lang  # noqa: E402

LANG = {
    "journal.monstergirls.guide.family.title": "Family",
    "journal.monstergirls.guide.family": (
        "A girl who is expecting craves one food and tells you; bring it before her time for twins (a harpy lays a full clutch). "
        "When her time comes you hear of it wherever you are, and little ones born to you come with a name. "
        "A cradle (planks around wool, on two sticks) lets them nap at night and grow twice as fast; rock it for a lullaby. "
        "Read them a written book. Give a little one a toy each day: a wooden hoe, a bowl, a wooden sword or a compass tips what she grows up to be. "
        "Be near as she grows: her first word, a flower, a drawing. In a thunderstorm she runs to you. "
        "Grown, she brings you the first thing of her calling. Use a plain painting on your hearth with the family round you for a portrait. "
        "The journal lists them all, and /monstergirls family tells you where everyone is."),
}


def main():
    add_lang(LANG)


if __name__ == "__main__":
    main()
