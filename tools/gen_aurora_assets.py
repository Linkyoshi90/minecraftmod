#!/usr/bin/env python3
"""Aurora nights (2026-11-08, entity/Aurora): the aurora shard's texture (a slim crystal shading green to teal to violet, with a
white glint), its item model, an advancement, lang, and a sentence for the stargazing guide.

    uv run --no-project --with pillow python tools/gen_aurora_assets.py [--preview out.png]
"""
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS, DATA, dump, save  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402

RAMP = [(80, 255, 140), (60, 230, 170), (50, 210, 200), (70, 180, 230), (120, 130, 245), (165, 100, 250)]


def shard():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    # a slim diagonal crystal from the lower left to the upper right, three pixels wide at its widest
    for i in range(12):
        x, y = 2 + i, 13 - i
        width = 1 if i in (0, 11) else 2 if i in (1, 10) else 3
        c = RAMP[min(len(RAMP) - 1, i * len(RAMP) // 12)]
        for w in range(width):
            yy = y + w - width // 2
            if 0 <= yy < 16:
                edge = w == 0 or w == width - 1
                px[x, yy] = tuple(int(v * (0.75 if edge else 1.0)) for v in c) + (255,)
    for x, y in ((8, 7), (9, 6), (5, 10)):
        px[x, y] = (240, 255, 255, 255)                                  # glints
    return save(img, "item", "aurora_shard.png")


def main():
    img = shard()
    dump(os.path.join(ASSETS, "models", "item", "aurora_shard.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": "monstergirls:item/aurora_shard"}})
    dump(os.path.join(DATA, "advancement", "aurora.json"), {
        "parent": "monstergirls:stargazer",
        "display": {"icon": {"id": "monstergirls:aurora_shard"}, "title": {"translate": "advancement.monstergirls.aurora"},
                    "description": {"translate": "advancement.monstergirls.aurora.desc"},
                    "frame": "goal", "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
    add_lang({
        "item.monstergirls.aurora_shard": "Aurora Shard",
        "monstergirls.configuration.auroraNights": "Aurora nights",
        "message.monstergirls.aurora.begin": "The sky lights up in curtains of green and violet: an aurora! Something glitters as it falls to the snow.",
        "advancement.monstergirls.aurora": "Northern Lights",
        "advancement.monstergirls.aurora.desc": "See an aurora over cold country",
    })
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.stargazing"] = (
        " Now and then, on a clear night in cold country, the sky fills with an aurora. A frost-touched girl or two comes out under "
        "it (and goes at dawn, unless she's made a friend), and aurora shards fall to the snow: every girl loves to be given one.")
    gen_manor_assets.append_guides()
    if "--preview" in sys.argv:
        img.resize((128, 128), Image.NEAREST).save(sys.argv[sys.argv.index("--preview") + 1])


if __name__ == "__main__":
    main()
