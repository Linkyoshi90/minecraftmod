#!/usr/bin/env python3
"""Assets for rarity tiers (entity/Rarity.java): the three legendary advancements (one criterion per legendary kind in "every_legend",
read from Rarity.java), the tier names, the journal's legends page and the config labels.

    python tools/gen_rarity_assets.py

Can be run twice.
"""
import json
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_harmontide_items as I  # noqa: E402
import gen_milk_assets as M  # noqa: E402

SRC = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "java", "com", "linkyoshi90", "harmontide", "entity", "Rarity.java")
src = open(SRC, encoding="utf-8").read()
LEGENDARY = re.findall(r'"([a-z_]+)"', re.search(r"LEGENDARY = List\.of\((.*?)\);", src, re.S).group(1))


def advancement(name, icon_id, frame, criteria, parent="minecraft:adventure/root"):
    path = os.path.join(I.DATA, "advancement", name + ".json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    doc = {"parent": parent,
           "display": {"icon": {"id": icon_id}, "title": {"translate": f"advancement.harmontide.{name}"},
                       "description": {"translate": f"advancement.harmontide.{name}.desc"}, "frame": frame,
                       "show_toast": True, "announce_to_chat": True, "hidden": False},
           "criteria": {c: {"trigger": "minecraft:impossible"} for c in criteria},
           "requirements": [[c] for c in criteria]}
    with open(path, "w", encoding="utf-8") as f:
        json.dump(doc, f, indent=2)


advancement("legendary_encounter", "minecraft:nether_star", "task", ["done"])
advancement("legendary_friend", "minecraft:totem_of_undying", "goal", ["done"], "harmontide:legendary_encounter")
advancement("every_legend", "minecraft:dragon_egg", "challenge", LEGENDARY, "harmontide:legendary_friend")

M.add_lang({
    "rarity.harmontide.common": "Common", "rarity.harmontide.rare": "Rare", "rarity.harmontide.legendary": "Legendary",
    "advancement.harmontide.legendary_encounter": "A Living Legend",
    "advancement.harmontide.legendary_encounter.desc": "Come face to face with a legendary creature",
    "advancement.harmontide.legendary_friend": "Storybook Friend",
    "advancement.harmontide.legendary_friend.desc": "Befriend a legendary creature",
    "advancement.harmontide.every_legend": "Keeper of Legends",
    "advancement.harmontide.every_legend.desc": f"Befriend every one of the {len(LEGENDARY)} legendary creatures",
    "journal.harmontide.guide.legends.title": "Legends",
    "journal.harmontide.guide.legends": "Most kinds are common. The storied ones are rare. The figures of myth are legendary: "
                                          "only one of each comes to a region, she glints now and then, and she stays until befriended.",
    "harmontide.configuration.rareSpawnRate": "Rare spawn rate",
    "harmontide.configuration.legendarySpawnRate": "Legendary spawn rate",
    "harmontide.configuration.legendaryOncePerRegion": "One legend per region",
})
print(f"rarity assets written ({len(LEGENDARY)} legends)")
