#!/usr/bin/env python3
"""Hamlets and villages (2026-11-08, entity/girl/Hamlet): advancements, lang, a sentence for the hearth guide.

    uv run --no-project --with pillow python tools/gen_hamlet_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402

LANG = {
    "harmontide.configuration.hamlets": "Hamlets and villages",
    "item.harmontide.hamlet_banner": "Banner of %s",
    "message.harmontide.hamlet.named": "Your hearth has grown into a hamlet. The girls call it %s. (A name tag on the hearth renames it.)",
    "message.harmontide.hamlet.renamed": "From now on this place is called %s.",
    "message.harmontide.hamlet.newcomer": "Word has got around: %s has come to %s and asks to stay.",
    "message.harmontide.hamlet.enter.hamlet": "%s, a hamlet of %s",
    "message.harmontide.hamlet.enter.village": "%s, a village of %s",
}
ADV = {"hamlet": ("harmontide:market_day", "minecraft:white_banner", "task", "A Name on the Map", "See your hearth grow into a hamlet"),
       "village": ("harmontide:hamlet", "minecraft:bell", "goal", "Word Gets Around", "A newcomer asks to stay in your village")}


def main():
    for adv, (parent, icon, frame, title, desc) in ADV.items():
        dump(os.path.join(DATA, "advancement", adv + ".json"), {
            "parent": parent,
            "display": {"icon": {"id": icon}, "title": {"translate": "advancement.harmontide." + adv},
                        "description": {"translate": "advancement.harmontide." + adv + ".desc"},
                        "frame": frame, "show_toast": True, "announce_to_chat": True, "hidden": False},
            "criteria": {"done": {"trigger": "minecraft:impossible"}}})
        LANG["advancement.harmontide." + adv] = title
        LANG["advancement.harmontide." + adv + ".desc"] = desc
    add_lang(LANG)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.hearth"] = (
        " With three living there a hearth becomes a hamlet: it takes a name (a name tag on the hearth changes it) and flies its "
        "own banner. With six it is a village, and now and then a wandering girl of a family new to it comes and asks to stay.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
