#!/usr/bin/env python3
"""A hero's welcome (2026-11-09, entity/HerosWelcome): lang and a sentence for the hearth guide.

    uv run --no-project --with pillow python tools/gen_hero_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402

LANG = {
    "message.monstergirls.hero.dragon": "Word got home: you slew the Ender Dragon! %s friends burst out cheering and won't let you through the door without a hug",
    "message.monstergirls.hero.wither_summoned": "Word got home that you called up the Wither. %s friends fuss over you, half proud and half horrified",
    "message.monstergirls.hero.hunter": "Word got home that you've faced every monster there is. %s friends want to hear every story",
    "message.monstergirls.hero.village": "Word got home: you saved a village from the raiders! %s friends throw you a hero's welcome",
    "message.monstergirls.hero.cured": "Word got home that you cured a zombie villager. %s friends say they always knew you were kind",
    "message.monstergirls.hero.beacon": "Word got home of your full beacon. %s friends insist on a party under its light",
    "message.monstergirls.hero.survived": "Word got home that you cheated death itself. %s friends hold on to you a little too long",
    "message.monstergirls.hero.elytra": "Word got home that you can fly now. %s friends (the harpies loudest) are thrilled",
    "message.monstergirls.hero.traveller": "Word got home that you've seen every land there is. %s friends gather round to hear about it",
}


def main():
    add_lang(LANG)
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.hearth"] = (
        " Word of your great deeds (the dragon slain, a village saved, a villager cured...) reaches home: come back to your hearth and your friends throw you a party.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
