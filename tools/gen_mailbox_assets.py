#!/usr/bin/env python3
"""Courier girls (2026-11-08, block/MailboxBlock): the mailbox's model (a spruce post and a box with a red flag, vanilla
textures), recipe, loot, advancement, lang, and a sentence for the friends guide.

    uv run --no-project --with pillow python tools/gen_mailbox_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS, DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402


def box(frm, to, side, top):
    faces = {f: {"texture": side} for f in ("north", "south", "east", "west")}
    faces["up"] = {"texture": top}
    faces["down"] = {"texture": top}
    return {"from": frm, "to": to, "faces": faces}


def main():
    m = os.path.join(ASSETS, "models")
    dump(os.path.join(m, "block", "mailbox.json"), {
        "parent": "minecraft:block/block", "ambientocclusion": False,
        "textures": {"particle": "minecraft:block/spruce_planks", "post": "minecraft:block/stripped_spruce_log", "post_top": "minecraft:block/stripped_spruce_log_top",
                     "box": "minecraft:block/barrel_side", "lid": "minecraft:block/barrel_top", "flag": "minecraft:block/red_wool"},
        "elements": [box([7, 0, 7], [9, 9, 9], "#post", "#post_top"), box([4, 9, 3], [12, 15, 13], "#box", "#lid"),
                     box([12, 11, 9], [13, 16, 10], "#flag", "#flag"), box([12, 14, 10], [13, 16, 12], "#flag", "#flag")]})
    dump(os.path.join(ASSETS, "blockstates", "mailbox.json"), {"variants": {"": {"model": "harmontide:block/mailbox"}}})
    dump(os.path.join(m, "item", "mailbox.json"), {"parent": "harmontide:block/mailbox"})
    dump(os.path.join(DATA, "loot_table", "blocks", "mailbox.json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": "harmontide:mailbox"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    dump(os.path.join(DATA, "recipe", "mailbox.json"), {
        "type": "minecraft:crafting_shaped", "category": "decorations",
        "pattern": ["PFP", "PCP", " S "],
        "key": {"P": {"item": "minecraft:spruce_planks"}, "F": {"item": "minecraft:feather"}, "C": {"item": "minecraft:chest"}, "S": {"item": "minecraft:stick"}},
        "result": {"id": "harmontide:mailbox", "count": 1}})
    dump(os.path.join(DATA, "advancement", "courier.json"), {
        "parent": "harmontide:pen_pal",
        "display": {"icon": {"id": "harmontide:mailbox"}, "title": {"translate": "advancement.harmontide.courier"},
                    "description": {"translate": "advancement.harmontide.courier.desc"},
                    "frame": "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
    add_lang({
        "block.harmontide.mailbox": "Mailbox",
        "advancement.harmontide.courier": "Special Delivery",
        "advancement.harmontide.courier.desc": "Have a harpy or centaur friend bring you your mail",
        "message.harmontide.mailbox.delivered": "%s brings you your mail.",
        "message.harmontide.mailbox.link_first": "Now use a feather on the mailbox to link it to.",
        "message.harmontide.mailbox.linked": "The mailboxes are linked (%s blocks apart).",
        "message.harmontide.mailbox.unlinked": "This mailbox now sends its mail to you.",
    })
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.friends"] = (
        " A harpy or centaur friend living near a mailbox of yours carries what you put in it: to the mailbox it's linked to "
        "(a feather on one, then on the other), or, unlinked, to you wherever you are.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
