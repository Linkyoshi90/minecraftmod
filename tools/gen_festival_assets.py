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
R.advancement("every_festival", "minecraft:firework_rocket", "goal", ["dance", "harvest", "gifts"], "harmontide:first_festival")

M.add_lang({
    "festival.harmontide.dance": "the Midsummer Dance",
    "festival.harmontide.harvest": "the Harvest Feast",
    "festival.harmontide.gifts": "the Gift Exchange",
    "message.harmontide.festival.today.dance": "Today is the Midsummer Dance! At dusk your friends gather at the hearth. Bring music.",
    "message.harmontide.festival.today.harvest": "Today is the Harvest Feast! At dusk your friends gather at the hearth. Stock a fridge near it.",
    "message.harmontide.festival.today.gifts": "Today is the Gift Exchange! At dusk your friends gather at the hearth with presents.",
    "message.harmontide.festival.wants_music": "The dance needs music: play a jukebox near the hearth",
    "message.harmontide.festival.wants_food": "The feast needs food: put some in a fridge near the hearth",
    "message.harmontide.festival.celebrate.dance": "The Midsummer Dance! %s friends dance with you around the hearth.",
    "message.harmontide.festival.celebrate.harvest": "The Harvest Feast! %s friends share the meal with you.",
    "message.harmontide.festival.celebrate.gifts": "The Gift Exchange! %s friends bring you presents.",
    "advancement.harmontide.first_festival": "Festive Spirit", "advancement.harmontide.first_festival.desc": "Celebrate a festival with your friends at the hearth",
    "advancement.harmontide.every_festival": "A Year at the Hearth",
    "advancement.harmontide.every_festival.desc": "Celebrate the Midsummer Dance, the Harvest Feast and the Gift Exchange",
    "journal.harmontide.guide.festivals.title": "Festivals",
    "journal.harmontide.guide.festivals": "Every 24 days come three festivals: the Midsummer Dance on the 8th day, the Harvest Feast on the "
                                            "16th and the Gift Exchange on the 24th. At dusk the girls who live at your hearth gather round it. "
                                            "Join them: play a jukebox for the dance, stock a fridge for the feast, and come empty-handed for the presents.",
    "journal.harmontide.festival.next": "Next: %s, in %s days.",
    "journal.harmontide.festival.today": "Today: %s!",
    "harmontide.configuration.festivals": "Festivals",
})
print("festival assets written")
