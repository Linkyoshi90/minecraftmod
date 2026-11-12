#!/usr/bin/env python3
"""Scout maps (2026-11-08, entity/ScoutMaps): map names, the message, an advancement, a sentence for the expedition guide.

    uv run --no-project --with pillow python tools/gen_scout_map_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402


def main():
    dump(os.path.join(DATA, "advancement", "scout_map.json"), {
        "parent": "harmontide:market_day",
        "display": {"icon": {"id": "minecraft:filled_map"}, "title": {"translate": "advancement.harmontide.scout_map"},
                    "description": {"translate": "advancement.harmontide.scout_map.desc"},
                    "frame": "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
    add_lang({
        "item.harmontide.scout_map.order_chapel": "Map to an Order Chapel",
        "item.harmontide.scout_map.realm_manor": "Map to a Lilith's Manor",
        "item.harmontide.scout_map.demon_spire": "Map to the Demon's Spire",
        "item.harmontide.scout_map.harpy_aerie": "Map to a Harpy Aerie",
        "item.harmontide.scout_map.merfolk_grotto": "Map to a Merfolk Grotto",
        "message.harmontide.scout_map": "%s drew you something on the way: %s.",
        "advancement.harmontide.scout_map": "X Marks the Spot",
        "advancement.harmontide.scout_map.desc": "A girl back from an expedition brings you a map she drew",
    })
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.expedition"] = (
        " Now and then she brings back a map she drew on the way, a red cross over a place nobody has explored yet: a chapel, a "
        "manor, the spire, an aerie or a grotto (the sea's girls chart grottoes, the realm's own its manors and spires; scouts bring "
        "maps twice as often).")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
