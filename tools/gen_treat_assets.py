#!/usr/bin/env python3
"""Assets for treats and birthdays (entity/girl/Treats.java): the advancement, the messages, Jade lines and the journal page.

    python tools/gen_treat_assets.py

Can be run twice.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402
import gen_rarity_assets as R  # noqa: E402  (writes its own files again, harmlessly)

R.advancement("birthday", "minecraft:cake", "task", ["done"])

M.add_lang({
    "message.harmontide.favourite": "%s loves %s! It's her favourite.",
    "message.harmontide.birthday": "It's %s's birthday today!",
    "message.harmontide.birthday.thanks": "%s is delighted you remembered her birthday!",
    "jade.harmontide.birthday": "Birthday: day %s of 24",
    "jade.harmontide.favourite": "Loves: %s",
    "advancement.harmontide.birthday": "Many Happy Returns", "advancement.harmontide.birthday.desc": "Bring a friend something to eat on her birthday",
    "journal.harmontide.guide.treats.title": "Treats and Birthdays",
    "journal.harmontide.guide.treats": "Every girl has a favourite treat from her homeland. She takes it even if it isn't what she usually eats, "
                                         "and it means three times as much. And every girl has a birthday on one of the 24 days of the calendar: "
                                         "bring a friend something to eat that day and she'll never forget it. Look at a friend to see her birthday.",
})
print("treat assets written")
