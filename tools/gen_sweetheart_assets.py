#!/usr/bin/env python3
"""Villager sweethearts and grandchildren (2026-11-08, entity/Sweethearts): advancements, lang, a sentence for the roles guide.

    uv run --no-project --with pillow python tools/gen_sweetheart_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402

LANG = {
    "monstergirls.configuration.sweethearts": "Villager sweethearts and grandchildren",
    "message.monstergirls.sweetheart.shine": "%s has taken a shine to a villager. She keeps finding reasons to visit.",
    "message.monstergirls.sweetheart.wed": "%s and her sweetheart are wed! The whole village heard the bell.",
    "message.monstergirls.sweetheart.born": "%s and her sweetheart's family has grown: a grandchild for you!",
    "message.monstergirls.sweetheart.widowed": "%s has lost her sweetheart, and grieves.",
    "message.monstergirls.sweetheart.describe.courting": "%s is courting a villager",
    "message.monstergirls.sweetheart.describe.wed": "%s is wed to a villager (little ones: %s)",
}
ADV = {"sweetheart": ("monstergirls:birthday", "minecraft:poppy", "task", "Sweet on Someone", "A grown daughter takes a shine to a villager"),
       "sweethearts_wed": ("monstergirls:sweetheart", "minecraft:bell", "goal", "Wedding Bells", "See your daughter wed her villager sweetheart"),
       "grandchild": ("monstergirls:sweethearts_wed", "minecraft:cake", "challenge", "Grandparent", "Welcome your first grandchild")}


def main():
    for adv, (parent, icon, frame, title, desc) in ADV.items():
        dump(os.path.join(DATA, "advancement", adv + ".json"), {
            "parent": parent,
            "display": {"icon": {"id": icon}, "title": {"translate": "advancement.monstergirls." + adv},
                        "description": {"translate": "advancement.monstergirls." + adv + ".desc"},
                        "frame": frame, "show_toast": True, "announce_to_chat": True, "hidden": False},
            "criteria": {"done": {"trigger": "minecraft:impossible"}}})
        LANG["advancement.monstergirls." + adv] = title
        LANG["advancement.monstergirls." + adv + ".desc"] = desc
    add_lang(LANG)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.roles"] = (
        " A grown daughter may take a shine to a villager (or give the villager a flower while she is near to introduce them). "
        "After three mornings near each other they are wed, and now and then a little one comes along: your grandchild, as devoted "
        "to you as her mother is. Two at most each.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
