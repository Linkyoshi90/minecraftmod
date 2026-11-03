#!/usr/bin/env python3
"""Assets for encounters (entity/Encounters.java): the two rescue advancements, the chat lines, the journal page and the config label.

    python tools/gen_encounter_assets.py

Can be run twice.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402
import gen_rarity_assets as R  # noqa: E402  (writes its own files again, harmlessly)

R.advancement("first_rescue", "minecraft:shears", "task", ["done"])
R.advancement("every_rescue", "minecraft:lead", "goal", ["webbed", "chased", "lost"], "monstergirls:first_rescue")

M.add_lang({
    "message.monstergirls.encounter.webbed": "%s is caught in a web! Cut her free.",
    "message.monstergirls.encounter.chased": "%s is running from zombies, straight towards you!",
    "message.monstergirls.encounter.lost": "%s is lost in the rain. Lead her somewhere with a roof.",
    "message.monstergirls.encounter.thanks": "%s won't forget that you helped her.",
    "advancement.monstergirls.first_rescue": "In the Nick of Time", "advancement.monstergirls.first_rescue.desc": "Help a creature in trouble",
    "advancement.monstergirls.every_rescue": "Knight Errant",
    "advancement.monstergirls.every_rescue.desc": "Cut one free of a web, see off her chasers, and lead one in from the rain",
    "journal.monstergirls.guide.encounters.title": "Encounters",
    "journal.monstergirls.guide.encounters": "Now and then you meet one in trouble: caught in a web, chased by zombies, or lost in the rain. "
                                             "Cut the web, fight off the chasers, or lead her under a roof, and she won't forget it. "
                                             "One found hurt mends with a healing balm.",
    "monstergirls.configuration.encounterRate": "Encounter rate",
})
print("encounter assets written")
