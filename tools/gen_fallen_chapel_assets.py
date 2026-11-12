#!/usr/bin/env python3
"""Fallen chapels (2026-11-08, entity/order/FallenChapels): an advancement, lang, a sentence for the Order guide.

    uv run --no-project --with pillow python tools/gen_fallen_chapel_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402


def main():
    dump(os.path.join(DATA, "advancement", "fallen_chapel.json"), {
        "parent": "harmontide:order_marked",
        "display": {"icon": {"id": "harmontide:realm_censer"}, "title": {"translate": "advancement.harmontide.fallen_chapel"},
                    "description": {"translate": "advancement.harmontide.fallen_chapel.desc"},
                    "frame": "challenge", "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
    add_lang({
        "advancement.harmontide.fallen_chapel": "Sabbath at the Altar",
        "advancement.harmontide.fallen_chapel.desc": "Win a sabbath raid and see an Order chapel fall to the realm",
        "message.harmontide.fallen_chapel.begin": "The heartfruit splits on the font and the bell begins to toll. %s sisters stand to defend their chapel!",
        "message.harmontide.fallen_chapel.won": "The last sister kneels. The chapel falls to the realm.",
        "message.harmontide.fallen_chapel.lost": "The sabbath raid fails: the sisters keep their chapel.",
        "message.harmontide.fallen_chapel.day": "Not by daylight: a sabbath needs the night.",
        "message.harmontide.fallen_chapel.alone": "You need at least two of the realm's own at your side.",
        "message.harmontide.fallen_chapel.not_chapel": "This font isn't part of an Order chapel.",
    })
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.order"] = (
        " The realm can strike back: at night, with two of its own among your friends at your side, offer a heartfruit at a chapel's "
        "holy font. The bell tolls and sisters come to defend it; make every one of them kneel, fall or die before dawn and the chapel "
        "falls to the realm, its font a realm censer.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
