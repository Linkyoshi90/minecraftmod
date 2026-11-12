#!/usr/bin/env python3
"""Lines for rain moods (entity/girl/RainMoods.java). Can be run twice.

    python tools/gen_rain_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402

M.add_lang({
    "message.harmontide.rain_lover": "%s splashes about in the rain with you",
    "journal.harmontide.guide.rain.title": "Sun and rain",
    "journal.harmontide.guide.rain": "Cats of every kind hate getting wet: caught in the rain with nowhere to be, a feliform looks for a roof "
                                       "nearby and waits it out. Frogs, newts and salamanders love it, and a friend of theirs who stands out in "
                                       "the rain with them grows a little closer (once a shower). Friends warn you a minute before it starts. Snakes, lizards, "
                                       "turtles and crocodiles are cold-blooded: on a clear morning they lie in the sun for a while and get up warmed through, "
                                       "and a friend who keeps them company there is a little closer.",
    "message.harmontide.basking": "%s stretches out in the sun beside you",
})
print("rain assets written")
