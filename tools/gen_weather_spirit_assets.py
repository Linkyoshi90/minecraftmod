#!/usr/bin/env python3
"""Weather spirits (2026-11-08, entity/girl/WeatherSpirits): lang for the raiju, the ame-onna and the sylph's behaviour.
The species themselves come from family_concepts_harmontide (FORTIETH) through gen_canid / gen_undead / gen_elf and gen_girl_data.

    uv run --no-project --with pillow python tools/gen_weather_spirit_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_milk_assets import add_lang  # noqa: E402

LANG = {
    "journal.monstergirls.when.thunder": "Comes out only in a thunderstorm",
    "message.monstergirls.ame_onna.shower": "%s lifts her face to the sky, and the rain begins.",
    "message.monstergirls.ame_onna.not_now": "%s shakes her head: not now.",
}

if __name__ == "__main__":
    add_lang(LANG)
