#!/usr/bin/env python3
"""Messages in bottles (2026-11-08, entity/MessageBottles, item/MessageBottleItem): the item's texture (vanilla's glass bottle
with a rolled letter inside and a cork), its model, an advancement, lang, and a sentence for the encounters guide.

    uv run --no-project --with pillow python tools/gen_bottle_assets.py [--preview out.png]
"""
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_paints import _vanilla_rgba  # noqa: E402
from gen_realm_assets import ASSETS, DATA, dump, save  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402


def bottle():
    src = _vanilla_rgba("item/glass_bottle")
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = tuple(src[y][x])
    paper, shade = (236, 224, 190, 255), (196, 176, 136, 255)
    for y in range(7, 13):                                               # a rolled letter lying in the bottle
        for x in range(6, 10):
            px[x, y] = shade if x in (6, 9) else paper
    for x in range(6, 10):                                               # a red ribbon round it
        px[x, 9] = (200, 50, 60, 255)
    for x in range(6, 10):                                               # the cork
        for y in (2, 3):
            px[x, y] = (150, 104, 60, 255) if y == 2 else (120, 80, 44, 255)
    return save(img, "item", "message_bottle.png")


def main():
    img = bottle()
    dump(os.path.join(ASSETS, "models", "item", "message_bottle.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:item/message_bottle"}})
    dump(os.path.join(DATA, "advancement", "message_bottle.json"), {
        "parent": "monstergirls:pen_pal",
        "display": {"icon": {"id": "monstergirls:message_bottle"}, "title": {"translate": "advancement.monstergirls.message_bottle"},
                    "description": {"translate": "advancement.monstergirls.message_bottle.desc"},
                    "frame": "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
    add_lang({
        "item.monstergirls.message_bottle": "Message in a Bottle",
        "advancement.monstergirls.message_bottle": "Washed Ashore",
        "advancement.monstergirls.message_bottle.desc": "Open a message in a bottle",
        "message.monstergirls.bottle.map": "Inside, rolled tight: %s, in a girl's hand.",
        "message.monstergirls.bottle.treasure": "Inside: a few things from a wreck, and sand.",
        "message.monstergirls.bottle.letter.0": "\"To whoever finds this: the sea is very wide and I am a little lonely.\" Signed with a %s's mark. Someone is waving from the shore...",
        "message.monstergirls.bottle.letter.1": "\"If this reaches anyone kind, look up!\" A %s's doodle of a wave. Someone is waving from the shore...",
        "message.monstergirls.bottle.letter.2": "\"I threw a hundred of these. Did one reach you?\" From a %s. Someone is waving from the shore...",
        "message.monstergirls.bottle.letter.3": "\"Looking for a friend on land. I'll find you.\" A %s's scale tucked in the paper. Someone is waving from the shore...",
    })
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.encounters"] = (
        " Walk the beaches by day and now and then a bottle washes up: a map a girl drew, a little wreck treasure, or a letter from a "
        "lonely girl of the sea, who comes to meet whoever read it.")
    gen_manor_assets.append_guides()
    if "--preview" in sys.argv:
        img.resize((128, 128), Image.NEAREST).save(sys.argv[sys.argv.index("--preview") + 1])


if __name__ == "__main__":
    main()
