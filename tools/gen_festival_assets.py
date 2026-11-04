#!/usr/bin/env python3
"""Assets for festivals (entity/girl/Festivals.java): the two advancements, the chat lines, the journal page and the config label.

    python tools/gen_festival_assets.py

Can be run twice.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402
import gen_rarity_assets as R  # noqa: E402  (writes its own files again, harmlessly)

R.advancement("first_festival", "minecraft:cake", "task", ["done"])
R.advancement("every_festival", "minecraft:firework_rocket", "goal", ["dance", "harvest", "gifts"], "monstergirls:first_festival")

M.add_lang({
    "festival.monstergirls.dance": "the Midsummer Dance",
    "festival.monstergirls.harvest": "the Harvest Feast",
    "festival.monstergirls.gifts": "the Gift Exchange",
    "message.monstergirls.festival.today.dance": "Today is the Midsummer Dance! At dusk your friends gather at the hearth. Bring music.",
    "message.monstergirls.festival.today.harvest": "Today is the Harvest Feast! At dusk your friends gather at the hearth. Stock a fridge near it.",
    "message.monstergirls.festival.today.gifts": "Today is the Gift Exchange! At dusk your friends gather at the hearth with presents.",
    "message.monstergirls.festival.wants_music": "The dance needs music: play a jukebox near the hearth",
    "message.monstergirls.festival.wants_food": "The feast needs food: put some in a fridge near the hearth",
    "message.monstergirls.festival.celebrate.dance": "The Midsummer Dance! %s friends dance with you around the hearth.",
    "message.monstergirls.festival.celebrate.harvest": "The Harvest Feast! %s friends share the meal with you.",
    "message.monstergirls.festival.celebrate.gifts": "The Gift Exchange! %s friends bring you presents.",
    "advancement.monstergirls.first_festival": "Festive Spirit", "advancement.monstergirls.first_festival.desc": "Celebrate a festival with your friends at the hearth",
    "advancement.monstergirls.every_festival": "A Year at the Hearth",
    "advancement.monstergirls.every_festival.desc": "Celebrate the Midsummer Dance, the Harvest Feast and the Gift Exchange",
    "journal.monstergirls.guide.festivals.title": "Festivals",
    "journal.monstergirls.guide.festivals": "Every 24 days come three festivals: the Midsummer Dance on the 8th day, the Harvest Feast on the "
                                            "16th and the Gift Exchange on the 24th. At dusk the girls who live at your hearth gather round it. "
                                            "Join them: play a jukebox for the dance, stock a fridge for the feast, and come empty-handed for the presents.",
    "journal.monstergirls.festival.next": "Next: %s, in %s days.",
    "journal.monstergirls.festival.today": "Today: %s!",
    "monstergirls.configuration.festivals": "Festivals",
})
print("festival assets written")
