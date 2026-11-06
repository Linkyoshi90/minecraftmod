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
                                         "beside them, picking out poison, hunger or the like now and then. Elephants and their kin spray a burning friend out.",
})
print("habit assets written")
