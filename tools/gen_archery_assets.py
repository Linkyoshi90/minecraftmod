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
    "message.harmontide.archery.start": "%s grins and strings her bow: \"Three arrows each. Loser says the winner is wonderful\"",
    "message.harmontide.archery.no_target": "%s looks round: there's no target block nearby to shoot at",
    "message.harmontide.archery.yours": "Your arrow: %s points (%s in all)",
    "message.harmontide.archery.hers": "%s's arrow: %s points (%s in all)",
    "message.harmontide.archery.won": "You beat %s, %s to %s! She pays up, pouting a little",
    "message.harmontide.archery.lost": "%s wins, %s to %s, and won't stop bouncing about it",
    "message.harmontide.archery.draw": "A draw with %s, %s to %s! She insists on a rematch soon",
    "advancement.harmontide.archery": "Bullseye Buddies",
    "advancement.harmontide.archery.desc": "Shoot an archery match against a friend at a target block",
    "advancement.harmontide.archery_won": "Sharper Than a Centaur",
    "advancement.harmontide.archery_won.desc": "Win an archery match against a friend",
}


def main():
    adv("archery", "harmontide:race", "minecraft:target")
    adv("archery_won", "harmontide:archery", "minecraft:bow")
    add_lang(LANG)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.games"] = (
        " Crouch and show a friend a bow near a target block for an archery match: three arrows each, closer to the middle scores more.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
