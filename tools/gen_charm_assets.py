#!/usr/bin/env python3
"""Assets for the branch charm (item/BranchCharmItem.java): the 16px icon, its model, and the lines.

    uv run --no-project --with pillow python tools/gen_charm_assets.py [--preview previews/branch_charm.png]

A small pendant on a cord: a golden ring holding a green leaf-shaped stone (a twig of the tree of life), with a highlight.
Can be run twice.
"""
import json
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, "src", "main", "resources", "assets", "monstergirls")

CORD = (122, 84, 52, 255)
CORD_D = (84, 56, 34, 255)
GOLD = (232, 186, 74, 255)
GOLD_D = (168, 122, 40, 255)
LEAF = (86, 168, 82, 255)
LEAF_L = (150, 214, 120, 255)
LEAF_D = (44, 110, 58, 255)
VEIN = (36, 86, 48, 255)
SHINE = (236, 252, 220, 255)


def charm():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for x, y in [(4, 1), (5, 1), (10, 1), (11, 1), (3, 2), (12, 2), (3, 3), (12, 3), (4, 4), (11, 4), (5, 5), (10, 5), (6, 6), (9, 6)]:
        px[x, y] = CORD if x < 8 else CORD_D                       # the cord, a loop over the top
    for x, y in [(7, 6), (8, 6), (6, 7), (9, 7), (6, 8), (9, 8)]:
        px[x, y] = GOLD if x < 8 else GOLD_D                        # the gold ring the leaf hangs from
    leaf = {7: (7, 8), 8: (6, 9), 9: (5, 10), 10: (5, 10), 11: (5, 10), 12: (6, 9), 13: (7, 8), 14: (7, 8)}
    for y, (a, b) in leaf.items():
        for x in range(a, b + 1):
            px[x, y] = LEAF_D if x == a or x == b or y == 14 else LEAF
    for y in range(8, 14):
        px[7 if y < 11 else 8, y] = VEIN                            # the midrib
    for x, y in [(6, 9), (6, 10), (7, 9)]:
        px[x, y] = LEAF_L
    px[6, 9] = SHINE
    return img


def main():
    img = charm()
    path = os.path.join(ASSETS, "textures", "item", "branch_charm.png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    model = os.path.join(ASSETS, "models", "item", "branch_charm.json")
    with open(model, "w", encoding="utf-8", newline="\n") as fh:
        json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:item/branch_charm"}}, fh, indent=2)
        fh.write("\n")
    M.add_lang({
        "item.monstergirls.branch_charm": "Branch Charm",
        "item.monstergirls.branch_charm.blank": "Meet every kind on one branch of the tree of life to earn one",
        "item.monstergirls.branch_charm.effect": "In your offhand: %s",
        "item.monstergirls.branch_charm.keepsake": "A keepsake: this branch gives no milk",
        "item.monstergirls.branch_charm.kin": "Wild ones of this branch who see it warm to you",
        "message.monstergirls.charmed": "%s recognises your charm and warms to you",
    })
    if "--preview" in sys.argv:
        out = sys.argv[sys.argv.index("--preview") + 1]
        img.resize((256, 256), Image.NEAREST).save(out)
        print("preview", out)
    print("charm assets written")


if __name__ == "__main__":
    main()
