#!/usr/bin/env python3
"""Assets for chores (entity/girl/Chores.java): the two advancements, the patrol line, the journal page and the config label.

    python tools/gen_chores_assets.py

Can be run twice.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402
import gen_rarity_assets as R  # noqa: E402  (writes its own files again, harmlessly)

R.advancement("first_chore", "minecraft:egg", "task", ["done"])
R.advancement("every_chore", "minecraft:fishing_rod", "goal", ["eggs", "honey", "wool", "fish"], "harmontide:first_chore")

M.add_lang({
    "message.harmontide.order.patrol_chores": "%s patrols here and does the chores",
    "advancement.harmontide.first_chore": "Many Hands", "advancement.harmontide.first_chore.desc": "Have a friend on patrol do a chore",
    "advancement.harmontide.every_chore": "A Working Household",
    "advancement.harmontide.every_chore.desc": "Have your friends gather eggs, take honey, shear sheep and catch fish",
    "journal.harmontide.guide.chores.title": "Chores",
    "journal.harmontide.guide.chores": "A friend you leave on patrol keeps the place: she gathers eggs, feathers and wool, takes the honey "
                                         "from full hives without upsetting the bees (put glass bottles in a chest for honey bottles), shears "
                                         "sheep, picks ripe berries, and fishes from the shore when there is nothing else to do. Food goes in a fridge nearby, "
                                         "the rest in a chest.",
    "harmontide.configuration.chores": "Chores",
})
print("chores assets written")
