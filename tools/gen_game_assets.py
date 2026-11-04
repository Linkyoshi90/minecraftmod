#!/usr/bin/env python3
"""Assets for games (entity/girl/HideAndSeek.java, entity/Snowballs.java): the advancements, messages and journal page.

    python tools/gen_game_assets.py

Can be run twice.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402
import gen_rarity_assets as R  # noqa: E402  (writes its own files again, harmlessly)

R.advancement("hide_and_seek", "minecraft:spyglass", "task", ["done"])
R.advancement("snowball_fight", "minecraft:snowball", "task", ["done"])

M.add_lang({
    "message.monstergirls.hide.start": "Count to ten! %s runs off to hide...",
    "message.monstergirls.hide.found": "You found %s!",
    "message.monstergirls.hide.won": "%s wins hide and seek! She pops out of her hiding place, glowing with pride.",
    "advancement.monstergirls.hide_and_seek": "Found You!", "advancement.monstergirls.hide_and_seek.desc": "Find a friend playing hide and seek",
    "advancement.monstergirls.snowball_fight": "Snow Day", "advancement.monstergirls.snowball_fight.desc": "Start a snowball fight with a friend",
    "journal.monstergirls.guide.games.title": "Games",
    "journal.monstergirls.guide.games": "Crouch and touch a friend with an empty hand to play hide and seek: count to ten while she hides, "
                                        "then find her within three minutes. Daughters love it most. Hit a friend with a snowball and she'll "
                                        "throw them right back for a while.",
})
print("game assets written")
