#!/usr/bin/env python3
"""Races and treasure hunts (2026-11-08, entity/Games): advancements, lang, and a sentence for the games guide.

    uv run --no-project --with pillow python tools/gen_game_assets2.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402


def main():
    lang = {
        "message.harmontide.race.ready": "%s grins and crouches at the line. Ready...",
        "message.harmontide.race.go": "Go!",
        "message.harmontide.race.won": "You win! %s pouts, then pays up.",
        "message.harmontide.race.lost": "%s gets there first and does a little victory dance.",
        "message.harmontide.race.timeout": "%s gives up on the race. Maybe another time.",
        "message.harmontide.race.where": "Point at somewhere a little way off to race to.",
        "message.harmontide.hunt.buried": "%s runs off with the shovel, digs somewhere, and comes back looking very pleased with herself. Find it!",
        "message.harmontide.hunt.warmer": "%s: \"Warmer!\"",
        "message.harmontide.hunt.colder": "%s: \"Colder...\"",
        "message.harmontide.hunt.hot": "%s: \"Hot! It's right here!\"",
        "message.harmontide.hunt.found": "You found it! %s claps.",
        "message.harmontide.hunt.shown": "%s gives up waiting and shows you where she buried it.",
        "message.harmontide.hunt.nowhere": "%s looks around: nowhere good to bury anything here.",
    }
    advs = {"race": ("hide_and_seek", "minecraft:white_banner", "task", "On Your Marks", "Race a friend"),
            "race_won": ("race", "minecraft:golden_boots", "goal", "Photo Finish", "Beat a friend in a race"),
            "treasure_hunt": ("hide_and_seek", "minecraft:brush", "task", "Warmer, Warmer...", "Dig up the present a friend buried for you")}
    for adv, (parent, icon, frame, title, desc) in advs.items():
        dump(os.path.join(DATA, "advancement", adv + ".json"), {
            "parent": "harmontide:" + parent,
            "display": {"icon": {"id": icon}, "title": {"translate": "advancement.harmontide." + adv},
                        "description": {"translate": "advancement.harmontide." + adv + ".desc"},
                        "frame": frame, "show_toast": True, "announce_to_chat": True, "hidden": False},
            "criteria": {"done": {"trigger": "minecraft:impossible"}}})
        lang["advancement.harmontide." + adv] = title
        lang["advancement.harmontide." + adv + ".desc"] = desc
    add_lang(lang)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.games"] = (
        " Show a friend a banner while pointing at somewhere a little way off and she'll race you there (win, and she pays up). "
        "Hand her a shovel and she'll bury one of her presents nearby under suspicious sand or gravel: she calls warmer and colder "
        "as you look, and a brush digs it out.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
