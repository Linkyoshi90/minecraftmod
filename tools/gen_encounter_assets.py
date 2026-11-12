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
R.advancement("every_rescue", "minecraft:lead", "goal", ["webbed", "chased", "lost"], "harmontide:first_rescue")

M.add_lang({
    "message.harmontide.encounter.webbed": "%s is caught in a web! Cut her free.",
    "message.harmontide.encounter.chased": "%s is running from zombies, straight towards you!",
    "message.harmontide.encounter.lost": "%s is lost in the rain. Lead her somewhere with a roof.",
    "message.harmontide.encounter.thanks": "%s won't forget that you helped her.",
    "advancement.harmontide.first_rescue": "In the Nick of Time", "advancement.harmontide.first_rescue.desc": "Help a creature in trouble",
    "advancement.harmontide.every_rescue": "Knight Errant",
    "advancement.harmontide.every_rescue.desc": "Cut one free of a web, see off her chasers, and lead one in from the rain",
    "journal.harmontide.guide.encounters.title": "Encounters",
    "journal.harmontide.guide.encounters": "Now and then you meet one in trouble: caught in a web, chased by zombies, or lost in the rain. "
                                             "Cut the web, fight off the chasers, or lead her under a roof, and she won't forget it. "
                                             "One found hurt mends with a healing balm.",
    "harmontide.configuration.encounterRate": "Encounter rate",
    "harmontide.configuration.missingCountsOffline": "Missing counts offline days",
    "harmontide.configuration.spawnConditions": "Spawn conditions",
    "journal.harmontide.when.full_moon": "Comes out under a full moon",
    "journal.harmontide.when.night": "Comes out at night",
    "journal.harmontide.when.storm": "Comes out in stormy weather",
    "journal.harmontide.when.rain": "Comes out in the rain",
})
print("encounter assets written")
