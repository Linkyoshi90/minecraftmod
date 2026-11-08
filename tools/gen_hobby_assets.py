#!/usr/bin/env python3
"""Hobbies (2026-11-08, entity/Hobbies): lang, an advancement, and a sentence for the life-at-home guide.

    uv run --no-project --with pillow python tools/gen_hobby_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402

HOBBIES = {
    "gardening": "She shows you how she pinches out the weak shoots and talks to the seedlings. She presses a handful into your hands.",
    "fishing": "She lets you hold the line until something bites, then cheers louder than you do. You keep the catch.",
    "reading": "She reads you her favourite passage, then explains it twice. You come away knowing a little more.",
    "music": "She teaches you a little tune and you play it together, badly and happily. It stays in your head all afternoon.",
    "baking": "She lets you knead, then fixes it when you look away. You get the first one out of the oven.",
    "collecting": "She shows you her treasures one by one, each with a story, and gives you a favourite.",
    "knitting": "She tries to teach you to knit and ends up knitting with your hands in hers. She gives you what you made together.",
}


def main():
    lang = {
        "jade.monstergirls.hobby": "Hobby: %s",
        "message.monstergirls.hobby.done": "%s finishes her %s for the day and leaves you something.",
        "message.monstergirls.hobby.invite": "%s is busy with her %s. Stay and keep her company?",
        "item.monstergirls.knitted_cap": "Cap knitted by %s",
        "advancement.monstergirls.shared_interests": "Shared Interests",
        "advancement.monstergirls.shared_interests.desc": "Keep a friend company through her hobby",
    }
    for key, line in HOBBIES.items():
        lang["hobby.monstergirls." + key] = key
        lang["message.monstergirls.hobby.shared." + key] = "%s: " + line
    dump(os.path.join(DATA, "advancement", "shared_interests.json"), {
        "parent": "monstergirls:hide_and_seek",
        "display": {"icon": {"id": "minecraft:writable_book"}, "title": {"translate": "advancement.monstergirls.shared_interests"},
                    "description": {"translate": "advancement.monstergirls.shared_interests.desc"},
                    "frame": "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
    add_lang(lang)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.home"] = (
        " Every girl has a hobby of her own (gardening, fishing, reading, music, baking, collecting or knitting; her friends see it) "
        "and an hour of the day for it. If a friend is about she leaves what she made at her feet: a gardener tends the crops round "
        "her, a reader shares what she learned, a musician's tune quickens everyone's work. Keep her company through it and she "
        "shows you how, once a day.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
