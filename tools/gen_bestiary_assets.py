#!/usr/bin/env python3
"""Assets for the bestiary (entity/Journal.java: sightings fill in each page): the advancement and the journal lines.

    python tools/gen_bestiary_assets.py

Can be run twice.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402
import gen_rarity_assets as R  # noqa: E402  (writes its own files again, harmlessly)

R.advancement("naturalist", "minecraft:spyglass", "goal", ["done"])

M.add_lang({
    "journal.harmontide.glimpsed": "Only glimpsed so far. Watch her kind a while to learn more.",
    "journal.harmontide.watch_more": "Watched %s of %s times. Keep watching, or make a friend of one, to learn her ways.",
    "journal.harmontide.studied": "Fully studied: %s of %s",
    "message.harmontide.studied": "Your journal page on the %s is complete",
    "journal.harmontide.guide.bestiary.title": "Watching",
    "journal.harmontide.guide.bestiary": "A kind you have only glimpsed gets just her name here. Watch her kind a while and her home and her moment "
                                           "fill in; watch longer, or befriend one, and you learn her ways. Fully study 25 kinds to be a naturalist.",
    "advancement.harmontide.naturalist": "Naturalist", "advancement.harmontide.naturalist.desc": "Fully study 25 kinds in your journal",
})
print("bestiary assets written")
