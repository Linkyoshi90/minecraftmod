#!/usr/bin/env python3
"""Assets for mining trips (entity/girl/Mining.java): the advancement, messages, Jade line and journal page.

    python tools/gen_mining_assets.py

Can be run twice.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402
import gen_rarity_assets as R  # noqa: E402  (writes its own files again, harmlessly)

R.advancement("miner", "minecraft:iron_pickaxe", "task", ["done"])

M.add_lang({
    "message.harmontide.mining.pickaxe": "%s takes the pickaxe. Show her what to dig for.",
    "message.harmontide.mining.no_pickaxe": "%s needs a pickaxe from you for that",
    "message.harmontide.mining.weak": "%s shakes her head: this pickaxe can't mine %s",
    "message.harmontide.mining.worn": "%s's pickaxe is too worn to go digging with",
    "message.harmontide.mining.off": "%s digs in to look for %s. She'll be back in about %s minutes.",
    "message.harmontide.mining.back": "%s climbs out of her mine with %s %s!",
    "message.harmontide.mining.back_broke": "%s's pickaxe broke, so she climbs out with the %s %s she found",
    "message.harmontide.mining.waiting": "%s is done mining and waits underground at %s %s %s until you come by",
    "jade.harmontide.miner": "Miner",
    "advancement.harmontide.miner": "Strike the Earth", "advancement.harmontide.miner.desc": "Have a friend come back from a mining trip",
    "journal.harmontide.guide.mining.title": "Mining Trips",
    "journal.harmontide.guide.mining": "Diggers (dwarves, gnomes, kobolds, moles, ants, constructs and more; look for \"Miner\") will mine for you. "
                                         "Hand one a pickaxe, then show her what to look for: an ore, raw metal, an ingot, a gem, coal or stone. "
                                         "She digs in at her patrol spot (or where she stands) and comes back there with a stack, into a chest beside "
                                         "the spot or into your hands. Rare things take days and wear the pickaxe; Efficiency, Fortune and Unbreaking "
                                         "help. Crouch with an empty hand to take your pickaxe back. She leaves a laddered shaft beside the spot "
                                         "that goes deeper while she's down (natural ground only), and brings up any ore she struck on the way.",
    "harmontide.configuration.minersDigShafts": "Miners dig real shafts",
})
print("mining assets written")
