#!/usr/bin/env python3
"""Archery matches (2026-11-09, entity/Archery): lang, two advancements, a sentence for the games guide.

    uv run --no-project --with pillow python tools/gen_archery_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_family_assets import adv  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402

LANG = {
    "message.monstergirls.archery.start": "%s grins and strings her bow: \"Three arrows each. Loser says the winner is wonderful\"",
    "message.monstergirls.archery.no_target": "%s looks round: there's no target block nearby to shoot at",
    "message.monstergirls.archery.yours": "Your arrow: %s points (%s in all)",
    "message.monstergirls.archery.hers": "%s's arrow: %s points (%s in all)",
    "message.monstergirls.archery.won": "You beat %s, %s to %s! She pays up, pouting a little",
    "message.monstergirls.archery.lost": "%s wins, %s to %s, and won't stop bouncing about it",
    "message.monstergirls.archery.draw": "A draw with %s, %s to %s! She insists on a rematch soon",
    "advancement.monstergirls.archery": "Bullseye Buddies",
    "advancement.monstergirls.archery.desc": "Shoot an archery match against a friend at a target block",
    "advancement.monstergirls.archery_won": "Sharper Than a Centaur",
    "advancement.monstergirls.archery_won.desc": "Win an archery match against a friend",
}


def main():
    adv("archery", "monstergirls:race", "minecraft:target")
    adv("archery_won", "monstergirls:archery", "minecraft:bow")
    add_lang(LANG)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.games"] = (
        " Crouch and show a friend a bow near a target block for an archery match: three arrows each, closer to the middle scores more.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
