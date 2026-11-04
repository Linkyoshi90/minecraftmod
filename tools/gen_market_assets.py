#!/usr/bin/env python3
"""Assets for market day (entity/girl/Market.java) and rivalries (entity/girl/Rivalries.java): the advancement, the messages,
the journal pages and the config labels.

    python tools/gen_market_assets.py

Can be run twice.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402
import gen_rarity_assets as R  # noqa: E402  (writes its own files again, harmlessly)

R.advancement("market_day", "minecraft:emerald", "task", ["done"])

M.add_lang({
    "message.monstergirls.market.arrives": "A travelling %s has set up by your hearth. She trades the finds of her country for emeralds.",
    "message.monstergirls.market.sold_out": "%s has nothing left to sell",
    "advancement.monstergirls.market_day": "Market Day", "advancement.monstergirls.market_day.desc": "Buy from a travelling girl at your hearth",
    "journal.monstergirls.guide.market.title": "Market Day",
    "journal.monstergirls.guide.market": "Every 6 days (the 4th, 10th, 16th and 22nd of the 24), if you are home that morning, a travelling girl "
                                         "from a family that doesn't live at your hearth sets up beside it. Hand her an emerald for a bundle "
                                         "of her country's finds; she has four to sell. She moves on the next day, unless you have made a friend of her.",
    "journal.monstergirls.guide.rivals.title": "Rivals",
    "journal.monstergirls.guide.rivals": "Some wild kinds can't stand each other: cats and mice, snakes and mice, wolves and rabbits, foxes and "
                                         "rabbits, spiders and bugs, bats and bugs. When they meet, one glowers and the other runs. Nobody gets "
                                         "hurt, and once a girl is your friend she has better things to do.",
    "monstergirls.configuration.travellingMarket": "Travelling market",
    "monstergirls.configuration.rivalries": "Rivalries",
})
print("market assets written")
