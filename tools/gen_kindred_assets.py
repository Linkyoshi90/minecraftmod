#!/usr/bin/env python3
"""Realm-touched (2026-11-08, entity/RealmTouched): advancements, lang and the journal guide "kindred".

    uv run --no-project python tools/gen_kindred_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402

GUIDE = ("Keep company with the Demon Realm long enough and it takes to you. Time in its air, heartfruit you eat, nights beside "
         "one of its own and sisters of the Order you bring to fall all leave its touch on you. Touched, you see in the realm's "
         "dark and its haze rises from you. Kindred, its own know you: wild devils and realm-changed girls let you be unless you "
         "strike first, they are a little likelier to share your bed, and the Order counts it against you. Realm-born, the realm "
         "feeds you while you stand in its air, and its night makes you strong. Splash holy water washes much of it out, and time "
         "near a holy font a little. (/harmontide realm touch tells you how far it has gone.)")

LANG = {
    "harmontide.configuration.realmTouched": "Realm-touched players",
    "message.harmontide.realm_touch.up.1": "The realm's dark no longer seems so dark to you. Its haze clings to your clothes.",
    "message.harmontide.realm_touch.up.2": "The realm knows you now. Its own look at you as one of their kin.",
    "message.harmontide.realm_touch.up.3": "You are the realm's own. Its air fills you like a meal, and its nights make you strong.",
    "message.harmontide.realm_touch.down.0": "The last of the realm's touch has washed out of you.",
    "message.harmontide.realm_touch.down.1": "The realm's kin no longer know you as one of theirs.",
    "message.harmontide.realm_touch.down.2": "The realm's hold on you loosens.",
    "commands.harmontide.realm.touch.0": "The realm has barely touched you (%s of 100).",
    "commands.harmontide.realm.touch.1": "Realm-touched: you see in its dark (%s of 100).",
    "commands.harmontide.realm.touch.2": "Kindred of the realm: its own let you be (%s of 100).",
    "commands.harmontide.realm.touch.3": "Realm-born: the realm feeds you and its night makes you strong (%s of 100).",
    "journal.harmontide.guide.kindred.title": "Kindred of the Realm",
    "journal.harmontide.guide.kindred": GUIDE,
}
ADV = {"realm_kindred": ("harmontide:weepcap", "task", "Kindred", "Become kin to the Demon Realm"),
       "realm_born_player": ("harmontide:heartfruit", "challenge", "Of the Realm", "Let the Demon Realm make you its own")}


def main():
    parent = "harmontide:realm_entered"
    for adv, (icon, frame, title, desc) in ADV.items():
        dump(os.path.join(DATA, "advancement", adv + ".json"), {
            "parent": parent,
            "display": {"icon": {"id": icon}, "title": {"translate": "advancement.harmontide." + adv},
                        "description": {"translate": "advancement.harmontide." + adv + ".desc"},
                        "frame": frame, "show_toast": True, "announce_to_chat": True, "hidden": False},
            "criteria": {"done": {"trigger": "minecraft:impossible"}}})
        parent = "harmontide:" + adv
        LANG["advancement.harmontide." + adv] = title
        LANG["advancement.harmontide." + adv + ".desc"] = desc
    add_lang(LANG)


if __name__ == "__main__":
    main()
