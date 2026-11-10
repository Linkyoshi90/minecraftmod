#!/usr/bin/env python3
"""The journal's Family guide (2026-11-09): the little ones' features in one place.

    uv run --no-project --with pillow python tools/gen_family_guide.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_milk_assets import add_lang  # noqa: E402

LANG = {
    "monstergirls.configuration.reunions": "Family reunions",
    "message.monstergirls.reunion.today": "Today is Reunion Day! At dusk, your children who live away come home to your hearth.",
    "message.monstergirls.reunion.together": "A family reunion! %s of your family gather round the hearth. The little ones run to you first",
    "message.monstergirls.reunion.arrived": "More of your family arrive, waving from the path",
    "journal.monstergirls.reunion.next": "Next Reunion Day: in %s days.",
    "journal.monstergirls.reunion.today": "Today is Reunion Day!",
    "advancement.monstergirls.family_reunion": "All Together Again",
    "advancement.monstergirls.family_reunion.desc": "Gather your family at the hearth on Reunion Day",
    "monstergirls.configuration.daughtersMoveOut": "Grown daughters move out",
    "message.monstergirls.moving_out.planned": "%s is moving out! She's building a house of her own, a little way down the path from your hearth",
    "message.monstergirls.moving_out.done": "%s's house is finished. Go and see it!",
    "message.monstergirls.moving_out.welcome": "%s shows you round her new house, beaming. A housewarming!",
    "message.monstergirls.moving_out.moved_in": "%2$s has moved in with %1$s, into the house she built",
    "journal.monstergirls.children.house": " · her own house",
    "advancement.monstergirls.moving_out": "A House of Her Own",
    "advancement.monstergirls.moving_out.desc": "Visit a grown daughter's new house near your hearth",
    "journal.monstergirls.tree.title": "Family Tree",
    "journal.monstergirls.tree.with": "with %s",
    "message.monstergirls.marriage.renew_hint": "Give %s the wedding ring by a hearth today to renew your vows",
    "message.monstergirls.marriage.renew_wait": "Save it for your anniversary with %s, in %s days, to renew your vows",
    "message.monstergirls.marriage.renewed": "%s and %s renew their vows after %s years, with %s friends gathered round!",
    "journal.monstergirls.family.renewed": " (vows renewed %sx)",
    "advancement.monstergirls.vow_renewal": "I Still Do",
    "advancement.monstergirls.vow_renewal.desc": "Renew your vows with a wife on your anniversary, by a hearth",
    "message.monstergirls.honeymoon_trip.wish": "%s has always wanted to see the %s. Take her there for your honeymoon!",
    "message.monstergirls.honeymoon_trip.made": "%s gazes round the %s, hand in yours. A honeymoon to remember! She gives you a souvenir",
    "item.monstergirls.souvenir": "Souvenir of the %s",
    "item.monstergirls.souvenir.lore": "From your honeymoon with %s, day %s",
    "journal.monstergirls.family.trips": " (been to: %s)",
    "journal.monstergirls.family.wish": " (dreams of the %s)",
    "advancement.monstergirls.honeymoon_trip": "Honeymoon Trip",
    "advancement.monstergirls.honeymoon_trip.desc": "Take a wife to the place she has always wanted to see",
    "journal.monstergirls.guide.family.title": "Family",
    "journal.monstergirls.guide.family": (
        "A girl who is expecting craves one food and tells you; bring it before her time for twins (a harpy lays a full clutch). "
        "When her time comes you hear of it wherever you are, and little ones born to you come with a name. "
        "A cradle (planks around wool, on two sticks) lets them nap at night and grow twice as fast; rock it for a lullaby. "
        "Read them a written book. Give a little one a toy each day: a wooden hoe, a bowl, a wooden sword or a compass tips what she grows up to be. "
        "Be near as she grows: her first word, a flower, a drawing. In a thunderstorm she runs to you. "
        "Grown, she brings you the first thing of her calling. Use a plain painting on your hearth with the family round you for a portrait. "
        "On Reunion Day (the 12th of every 24) your grown children who live away come home at dusk with their sweethearts and little ones, "
        "if you are by your hearth, and go back the next morning. "
        "A grown daughter who settles at your hearth builds a house of her own a little way off, with a cradle and a bed for her sweetheart, "
        "who moves in once they are wed; visit it when it's done. "
        "The journal lists them all and draws your family tree, wife by wife and generation by generation, "
        "and /monstergirls family tells you where everyone is."),
}


def main():
    from gen_family_assets import adv
    adv("family_reunion", "monstergirls:first_child", "minecraft:campfire")
    adv("moving_out", "monstergirls:first_child", "minecraft:oak_door")
    adv("vow_renewal", "monstergirls:wedding", "monstergirls:wedding_ring")
    adv("honeymoon_trip", "monstergirls:wedding", "minecraft:filled_map")
    add_lang(LANG)
    rewrite_guide()
    marriage_guide()


MARRIAGE_OLD = ("Every forty days she wears her veil again; give her the ring by a hearth on the day and you renew your vows, "
                "with the friends gathered round, the bouquet and a second honeymoon.")
MARRIAGE_NEW = ("Every forty days she wears her veil again; give her the ring by a hearth on the day and you renew your vows, "
                "with the friends gathered round, the bouquet and a second honeymoon. "
                "At the wedding she names a place she has always wanted to see; take her there for a souvenir and a pack of what you found.")


def marriage_guide():
    """Vow renewal in the marriage guide (once), then the pages split again."""
    import json
    from gen_realm_assets import ASSETS
    import split_guides
    path = os.path.join(ASSETS, "lang", "en_us.json")
    with open(path, encoding="utf-8") as fh:
        text = fh.read()
    key = "journal.monstergirls.guide.marriage"
    old = json.loads(text)[key]
    if MARRIAGE_NEW not in old:
        a = json.dumps(key) + ": " + json.dumps(old, ensure_ascii=False)
        assert text.count(a) == 1 and old.count(MARRIAGE_OLD) == 1, old
        text = text.replace(a, json.dumps(key) + ": " + json.dumps(old.replace(MARRIAGE_OLD, MARRIAGE_NEW), ensure_ascii=False))
        with open(path, "w", encoding="utf-8", newline=chr(10)) as fh:
            fh.write(text)
    split_guides.main()


def rewrite_guide():
    """The guide text changed after it was first written (add_lang never updates): put the new one in, then split it into pages."""
    import json
    from gen_realm_assets import ASSETS
    import split_guides
    path = os.path.join(ASSETS, "lang", "en_us.json")
    with open(path, encoding="utf-8") as fh:
        text = fh.read()
    key = "journal.monstergirls.guide.family"
    old = json.loads(text)[key]
    if old != LANG[key]:
        a = json.dumps(key) + ": " + json.dumps(old, ensure_ascii=False)
        assert text.count(a) == 1
        text = text.replace(a, json.dumps(key) + ": " + json.dumps(LANG[key], ensure_ascii=False))
        with open(path, "w", encoding="utf-8", newline=chr(10)) as fh:
            fh.write(text)
    split_guides.main()


if __name__ == "__main__":
    main()
