#!/usr/bin/env python3
"""Lines for the branch habits (entity/girl/Habits.java): the full-moon howl and grooming. Can be run twice.

    python tools/gen_habit_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402

M.add_lang({
    "message.harmontide.moon_howl": "%s howls at the full moon, and you feel it in your bones",
    "message.harmontide.groomed": "%s grooms you and picks out the %s",
    "message.harmontide.doused": "%s sprays you with her trunk and puts you out",
    "journal.harmontide.guide.habits.title": "Habits",
    "journal.harmontide.guide.habits": "Each branch has its habits. Wolves, dogs and foxes howl at the full moon, and every canid in earshot answers; "
                                         "stand under the moon with a friend among them and you are closer for it. Monkeys and apes groom a friend "
                                         "beside them, picking out poison, hunger or the like now and then. Elephants and their kin spray a burning friend out. "
                                         "Walk beside a hoofed friend (cattle, goats, deer, camels, horses) and you find your footing the way she does, "
                                         "stepping up whole blocks without a jump.",
    "message.harmontide.beachcombed": "%s digs in the sand at your feet and turns up %s",
    "message.harmontide.fairy_ring": "You stand in a fairy ring, and luck is with you till morning",
    "message.harmontide.sure_footed": "Beside %s you find your footing: you step up whole blocks",
    "message.harmontide.thumped": "%s thumps the ground: %s, coming from the %s",
    "message.harmontide.pressed": "You press the %s between the pages (%s of %s)",
    "message.harmontide.pressed_already": "Your journal already holds a pressed %s",
    "journal.harmontide.pressed.title": "Pressed Flowers",
    "journal.harmontide.pressed.count": "%s of %s kinds",
    "journal.harmontide.pressed.line": "%s, with %s",
    "journal.harmontide.pressed.alone": "%s",
    "message.harmontide.cushioned": "%s catches you: the landing is soft",
    "message.harmontide.steady_anvil": "%s steadies the anvil: it doesn't wear",
    "advancement.harmontide.herbarium": "Herbarium",
    "advancement.harmontide.herbarium.desc": "Press every kind of small flower in your journal",
    "message.harmontide.supper_bell": "The bell calls them home: %s coming",
    "message.harmontide.supper": "%s shares supper with you by the fire",
    "advancement.harmontide.well_taught": "Well Taught",
    "advancement.harmontide.well_taught.desc": "Learn a lesson from every branch of the tree of life",
})
# the habits and keepsakes guides have since been edited by hand in en_us.json (thumping, pressed flowers): edit them there
print("habit assets written")
