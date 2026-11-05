#!/usr/bin/env python3
"""Assets for the journal's tree of life (entity/Journal.java, entity/Clade.java): the branch advancement and the journal lines.
The clade names themselves (clade.monstergirls.<clade>) come from tools/taxonomy.py.

    python tools/gen_tree_assets.py

Can be run twice.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402
import gen_rarity_assets as R  # noqa: E402  (writes its own files again, harmlessly)

R.advancement("whole_branch", "minecraft:oak_sapling", "goal", ["done"])

M.add_lang({
    "journal.monstergirls.tree.title": "The tree of life",
    "journal.monstergirls.tree.line": "%s %s/%s",
    "journal.monstergirls.guest": "A guest here: her true kin are few, so she lives with her nearest relatives",
    "message.monstergirls.word_gets_around": "Word of %s gets around the %s",
    "message.monstergirls.whole_branch": "%s: every kind met, the branch is whole",
    "journal.monstergirls.guide.tree.title": "The tree of life",
    "journal.monstergirls.guide.tree": "Every kind belongs to a branch of the tree of life: cats with hyenas and mongooses, whales with hippos, "
                                       "salamanders with frogs. A body can fool you (the hyena runs like a dog), her branch decides her milk. "
                                       "The pages after the guide keep count of each branch you have started; meet every kind on one branch to complete it. "
                                       "Your first friend of a kind gets talked about: wild ones of her branch nearby start a little fond of you.",
    "advancement.monstergirls.whole_branch": "A Whole Branch", "advancement.monstergirls.whole_branch.desc": "Meet every kind on one branch of the tree of life",
})
print("tree assets written")
