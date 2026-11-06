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
BOUGHS = {"carnivores": ("minecraft:bone", "Carnivores", "felines, canids and arctoids"),
          "gnawers": ("minecraft:carrot", "Gnawers", "rodents and lagomorphs"),
          "hooves": ("minecraft:wheat", "Hooves", "horses, cattle, deer, camels, pigs and the whales' kin"),
          "reptiles": ("minecraft:turtle_scute", "Reptiles", "crocodiles and dinosaurs, turtles, snakes and lizards"),
          "arthropods": ("minecraft:spider_eye", "Arthropods", "arachnids, insects and crustaceans"),
          "legends": ("minecraft:nether_star", "Legends", "the fae, dragons, undead, devils, slimes, constructs and riders")}
for k, (icon, name, what) in BOUGHS.items():
    R.advancement("bough_" + k, icon, "challenge", ["done"], "monstergirls:whole_branch")
R.advancement("whole_tree", "minecraft:flowering_azalea", "challenge", ["done"], "monstergirls:whole_branch")

M.add_lang({
    "journal.monstergirls.tree.title": "The tree of life",
    "journal.monstergirls.tree.line": "%s %s/%s",
    "journal.monstergirls.milestones": "Milestones",
    "journal.monstergirls.branches": "Whole branches: %s of %s (boughs %s of %s)",
    "jade.monstergirls.branch": "%s · met %s of %s",
    "journal.monstergirls.guest": "A guest here: her true kin are few, so she lives with her nearest relatives",
    "message.monstergirls.word_gets_around": "Word of %s gets around the %s",
    "message.monstergirls.whole_branch": "%s: every kind met, the branch is whole",
    "journal.monstergirls.guide.tree.title": "The tree of life",
    "journal.monstergirls.guide.tree": "Every kind belongs to a branch of the tree of life: cats with hyenas and mongooses, whales with hippos, "
                                       "salamanders with frogs. A body can fool you (the hyena runs like a dog), her branch decides her milk. "
                                       "The pages after the guide keep count of each branch you have started; meet every kind on one branch to complete it and earn its charm (in your offhand it lends that branch's milk's first effect). "
                                       "Your first friend of a kind gets talked about: wild ones of her branch nearby start a little fond of you.",
    **{"bough.monstergirls." + k: n for k, (_, n, _) in BOUGHS.items()},
    **{"advancement.monstergirls.bough_" + k: n + " in Full" for k, (_, n, _) in BOUGHS.items()},
    **{"advancement.monstergirls.bough_" + k + ".desc": "Meet every kind of " + w for k, (_, _, w) in BOUGHS.items()},
    "message.monstergirls.bough": "Every branch of the %s is whole",
    "advancement.monstergirls.whole_tree": "The Whole Tree of Life",
    "advancement.monstergirls.whole_tree.desc": "Meet every kind on every branch of the tree of life",
    "advancement.monstergirls.whole_branch": "A Whole Branch", "advancement.monstergirls.whole_branch.desc": "Meet every kind on one branch of the tree of life",
})
print("tree assets written")
