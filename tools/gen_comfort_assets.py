#!/usr/bin/env python3
"""Assets for comfort and the forecast (entity/Comfort.java) and the safe hearth: messages and config labels.

    python tools/gen_comfort_assets.py

Can be run twice.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402

M.add_lang({
    "message.harmontide.comfort": "%s rushes to your side and steadies you",
    "message.harmontide.forecast": "%s sniffs the air: \"Rain's coming soon.\"",
    "message.harmontide.forecast.frog": "%s croaks loudly: rain is coming!",
    "harmontide.configuration.comfort": "Comfort and forecasts",
    "harmontide.configuration.hearthSafeRadius": "Safe radius around a hearth",
})
print("comfort assets written")
