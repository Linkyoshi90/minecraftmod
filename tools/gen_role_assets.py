#!/usr/bin/env python3
"""Assets for roles (entity/girl/Roles.java) and teamwork (entity/Teamwork.java): the advancement, the role names and message,
the journal pages and the config label.

    python tools/gen_role_assets.py

Can be run twice.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402
import gen_rarity_assets as R  # noqa: E402  (writes its own files again, harmlessly)

R.advancement("teamwork", "minecraft:iron_sword", "task", ["done"])

M.add_lang({
    "role.harmontide.farmhand": "Farmhand",
    "role.harmontide.cook": "Cook",
    "role.harmontide.guard": "Guard",
    "role.harmontide.scout": "Scout",
    "message.harmontide.role.chosen": "%s has grown up and wants to be a %s.",
    "journal.harmontide.guide.roles.title": "Callings",
    "journal.harmontide.guide.roles": "A daughter who grows up picks a calling from what she grew up around. Farmland makes a farmhand: "
                                        "crops grow faster where she tends them. A kitchen (furnace, smoker, campfire or fridge) makes a cook: "
                                        "now and then she cooks something raw in a fridge. A bell or an iron golem makes a guard, tougher and "
                                        "stronger. Otherwise she becomes a scout: her expeditions take half the time and bring more home.",
    "journal.harmontide.guide.teamwork.title": "Teamwork",
    "journal.harmontide.guide.teamwork": "Two friends fighting the same foe set each other up: a blow within three seconds of a teammate's "
                                           "lands half again as hard. A harpy knocks the foe into the air, and a slime or spider girl slows it down.",
    "advancement.harmontide.teamwork": "Fighting as One", "advancement.harmontide.teamwork.desc": "Have two of your friends land a combined blow",
    "harmontide.configuration.teamwork": "Teamwork",
})
print("role assets written")
