#!/usr/bin/env python3
"""Dates (2026-11-08, entity/Dates): an advancement, lang, a sentence for the marriage guide.

    uv run --no-project --with pillow python tools/gen_date_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402

LANG = {
    "monstergirls.configuration.dates": "Dates",
    "message.monstergirls.date.ask.flowers": "%s takes the flower and blushes. \"Somewhere with lots of flowers... with you?\"",
    "message.monstergirls.date.ask.shore": "%s takes the flower and blushes. \"Let's go and sit by the water together.\"",
    "message.monstergirls.date.ask.view": "%s takes the flower and blushes. \"Take me somewhere high, where we can see everything.\"",
    "message.monstergirls.date.ask.stars": "%s takes the flower and blushes. \"Tonight, under the open sky? I want to watch the stars with you.\"",
    "message.monstergirls.date.lovely.flowers": "%s weaves a flower into her hair and leans on your shoulder. A lovely date.",
    "message.monstergirls.date.lovely.shore": "%s dips her toes in the water and laughs. A lovely date.",
    "message.monstergirls.date.lovely.view": "%s points out everywhere you've been together. A lovely date.",
    "message.monstergirls.date.lovely.stars": "%s names a star after you. A lovely date.",
    "message.monstergirls.date.bored": "%s sighs. Maybe another time.",
    "message.monstergirls.date.again": "%s smiles: \"We already went out today. Tomorrow?\"",
    "advancement.monstergirls.first_date": "It's a Date",
    "advancement.monstergirls.first_date.desc": "Take a close friend to her kind of place",
}


def main():
    dump(os.path.join(DATA, "advancement", "first_date.json"), {
        "parent": "monstergirls:stargazer",
        "display": {"icon": {"id": "minecraft:poppy"}, "title": {"translate": "advancement.monstergirls.first_date"},
                    "description": {"translate": "advancement.monstergirls.first_date.desc"},
                    "frame": "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
    add_lang(LANG)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.marriage"] = (
        " Before any ring, ask her out: sneak and give a close friend a flower, and she'll tell you her kind of place (lots of "
        "flowers, the water's edge, somewhere high with a view, or the open night sky). Take her there and stay together half a minute.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
