#!/usr/bin/env python3
"""Lines for the branch habits (entity/girl/Habits.java): the full-moon howl and grooming. Can be run twice.

    python tools/gen_habit_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402

M.add_lang({
    "message.monstergirls.moon_howl": "%s howls at the full moon, and you feel it in your bones",
    "message.monstergirls.groomed": "%s grooms you and picks out the %s",
    "message.monstergirls.doused": "%s sprays you with her trunk and puts you out",
    "journal.monstergirls.guide.habits.title": "Habits",
    "journal.monstergirls.guide.habits": "Each branch has its habits. Wolves, dogs and foxes howl at the full moon, and every canid in earshot answers; "
                                         "stand under the moon with a friend among them and you are closer for it. Monkeys and apes groom a friend "
                                         "beside them, picking out poison, hunger or the like now and then. Elephants and their kin spray a burning friend out. "
                                         "Walk beside a hoofed friend (cattle, goats, deer, camels, horses) and you find your footing the way she does, "
                                         "stepping up whole blocks without a jump.",
    "message.monstergirls.beachcombed": "%s digs in the sand at your feet and turns up %s",
    "message.monstergirls.fairy_ring": "You stand in a fairy ring, and luck is with you till morning",
    "message.monstergirls.sure_footed": "Beside %s you find your footing: you step up whole blocks",
    "message.monstergirls.thumped": "%s thumps the ground: %s, coming from the %s",
    "message.monstergirls.pressed": "You press the %s between the pages (%s of %s)",
    "message.monstergirls.pressed_already": "Your journal already holds a pressed %s",
    "journal.monstergirls.pressed.title": "Pressed Flowers",
    "journal.monstergirls.pressed.count": "%s of %s kinds",
    "journal.monstergirls.pressed.line": "%s, with %s",
    "journal.monstergirls.pressed.alone": "%s",
    "message.monstergirls.cushioned": "%s catches you: the landing is soft",
    "message.monstergirls.steady_anvil": "%s steadies the anvil: it doesn't wear",
    "advancement.monstergirls.herbarium": "Herbarium",
    "advancement.monstergirls.herbarium.desc": "Press every kind of small flower in your journal",
})
# the habits and keepsakes guides have since been edited by hand in en_us.json (thumping, pressed flowers): edit them there
print("habit assets written")
