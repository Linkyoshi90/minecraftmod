#!/usr/bin/env python3
"""Compliments on a new outfit (entity/Compliments) and things a friend notices (entity/Notices), 2026-11-09: lang.

    uv run --no-project --with pillow python tools/gen_compliment_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_milk_assets import add_lang  # noqa: E402

LANG = {
    "message.harmontide.notice.creeper": "%s shrieks: \"Creeper! To the %s!\"",
    "message.harmontide.notice.worn": "%s eyes your %s: \"That's about to fall apart, you know\"",
    "message.harmontide.notice.tired": "%s peers at you: \"When did you last sleep? You look awful. Get some rest before the phantoms come\"",
    "message.harmontide.notice.tired_wife": "%s takes your hand firmly: \"Bed. Now. You haven't slept in days\"",
    "message.harmontide.compliment.0": "%s looks you up and down: \"Ooh, new look? It suits you!\"",
    "message.harmontide.compliment.1": "%s gives you a thumbs up: \"Very adventurer-chic\"",
    "message.harmontide.compliment.2": "%s: \"Did you get new gear? You look ready for anything\"",
    "message.harmontide.compliment.3": "%s circles you once: \"Not bad. Not bad at all\"",
    "message.harmontide.compliment.4": "%s: \"That colour really works on you\"",
    "message.harmontide.compliment.sweet.0": "%s goes a little pink: \"You look... really good in that\"",
    "message.harmontide.compliment.sweet.1": "%s stares a moment too long, then looks away: \"N-nice outfit\"",
    "message.harmontide.compliment.sweet.2": "%s fixes your collar for you and smiles: \"There. Perfect\"",
    "message.harmontide.compliment.sweet.3": "%s whistles: \"Wearing that for anyone special?\"",
    "message.harmontide.compliment.sweet.4": "%s: \"Careful, dressed like that someone might steal you away\"",
}


def main():
    add_lang(LANG)


if __name__ == "__main__":
    main()
