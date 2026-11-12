#!/usr/bin/env python3
"""Assets for milk tasting (entity/Journal.java taste(), item/FamilyMilkItem.java): the connoisseur advancement and the lines.

    python tools/gen_tasting_assets.py

Can be run twice.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402
import gen_rarity_assets as R  # noqa: E402  (writes its own files again, harmlessly)
import taxonomy as T  # noqa: E402

R.advancement("connoisseur", "harmontide:bovid_milk", "challenge", ["done"], "harmontide:first_milk")

M.add_lang({
    "message.harmontide.tasted": "A first taste of %s (%s of %s milks)",
    "journal.harmontide.tasted": "Milks tasted: %s of %s",
    "jade.harmontide.milk_kind": "Her milk: %s",
    "advancement.harmontide.connoisseur": "Connoisseur",
    "advancement.harmontide.connoisseur.desc": f"Drink a bucket of every one of the {len(T.milks())} kinds of milk",
})
print("tasting assets written")
