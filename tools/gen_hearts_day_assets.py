#!/usr/bin/env python3
"""The Day of Hearts (2026-11-09, a fourth festival in entity/girl/Festivals): lang, and the festival guide and advancement text
rewritten for four festivals.

    uv run --no-project --with pillow python tools/gen_hearts_day_assets.py
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import split_guides  # noqa: E402

LANG = {
    "festival.monstergirls.hearts": "the Day of Hearts",
    "message.monstergirls.festival.today.hearts": "Today is the Day of Hearts! At dusk your friends gather at the hearth: set out a cake.",
    "message.monstergirls.festival.celebrate.hearts": "The Day of Hearts! %s friends share the cake and press sweetheart cookies into your hands.",
    "message.monstergirls.festival.wants_cake": "Your friends glance at the table: the Day of Hearts wants a cake near the hearth",
    "item.monstergirls.sweetheart_cookie": "%s's Sweetheart Cookie",
}
REWRITE = {
    "journal.monstergirls.guide.festivals": (
        "Every 24 days come four festivals: the Midsummer Dance on the 8th day, the Harvest Feast on the 16th, the Day of Hearts on the "
        "20th and the Gift Exchange on the 24th. At dusk the girls who live at your hearth gather round it. Join them: play a jukebox "
        "for the dance, stock a fridge for the feast, set out a cake for the Day of Hearts, and come empty-handed for the presents."),
    "advancement.monstergirls.every_festival.desc": "Celebrate the Midsummer Dance, the Harvest Feast, the Day of Hearts and the Gift Exchange",
}


def main():
    add_lang(LANG)
    path = os.path.join(ASSETS, "lang", "en_us.json")
    with open(path, encoding="utf-8") as fh:
        text = fh.read()
    have = json.loads(text)
    for key, new in REWRITE.items():
        a = json.dumps(key) + ": " + json.dumps(have[key], ensure_ascii=False)
        assert text.count(a) == 1, key
        text = text.replace(a, json.dumps(key) + ": " + json.dumps(new, ensure_ascii=False))
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(text)
    split_guides.main()


if __name__ == "__main__":
    main()
