#!/usr/bin/env python3
"""Assets for the exchange liaison (2026-11-07, entity/Liaison): the exchange desk block, the homestay permit icon, the liaison's
profession clothes for villagers and zombie villagers (vanilla's cleric robe from the local 1.21.1 client jar, re-dyed navy with
a pink sash), models, blockstate, loot table and recipes.

    uv run --no-project --with pillow python tools/gen_liaison_assets.py [--preview previews/liaison_assets.png]
"""
import colorsys
import io
import json
import os
import random
import sys
import zipfile

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_paints import VANILLA_JARS, _vanilla_rgba  # noqa: E402

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(REPO, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "harmontide")
DATA = os.path.join(RES, "data", "harmontide")


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def vanilla_image(path):
    jar = next((j for j in VANILLA_JARS if os.path.exists(j)), None)
    with zipfile.ZipFile(jar) as z:
        return Image.open(io.BytesIO(z.read(f"assets/minecraft/textures/{path}.png"))).convert("RGBA")


def redye(img, sash_rows=None):
    """Navy coat: every coloured pixel gets the navy hue, keeping its lightness; greys (skin is not drawn here) stay."""
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            h, l, s = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
            if s < 0.12:
                continue
            nr, ng, nb = colorsys.hls_to_rgb(0.64, min(0.55, l * 0.8), 0.45)
            px[x, y] = (int(nr * 255), int(ng * 255), int(nb * 255), a)
    return img


def sash(img, body_box):
    """A pink sash corner to corner across the front of the body (body_box = x0, y0, w, h of the body's front face)."""
    px = img.load()
    x0, y0, w, h = body_box
    for i in range(max(w, h)):
        for t in (0, 1):
            x = x0 + min(w - 1, int(i * w / h)) + t
            y = y0 + min(h - 1, i)
            if x0 <= x < x0 + w and px[x, y][3]:
                px[x, y] = (232, 120, 168, 255) if t == 0 else (200, 88, 136, 255)
    return img


def profession():
    out = []
    for kind in ("villager", "zombie_villager"):
        img = redye(vanilla_image(f"entity/{kind}/profession/cleric"))
        sash(img, (6, 44, 8, 18))                        # the robe's front face on the villager atlas (texOffs 0,38)
        path = os.path.join(ASSETS, "textures", "entity", kind, "profession", "liaison.png")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        img.save(path)
        out.append(img)
    return out


def desk():
    rnd = random.Random(5)
    planks = _vanilla_rgba("oak_planks")
    top = Image.new("RGBA", (16, 16))
    side = Image.new("RGBA", (16, 16))
    tp, sp = top.load(), side.load()
    for y in range(16):
        for x in range(16):
            tp[x, y] = tuple(planks[y][x][:3]) + (255,)
            c = planks[y][x]
            sp[x, y] = tuple(int(v * 0.85) for v in c[:3]) + (255,)
    for y in range(3, 12):                                # papers on the desk, one stamped with a heart
        for x in range(2, 10):
            tp[x, y] = (244, 240, 228, 255) if (x + y) % 7 else (220, 214, 200, 255)
    for x, y in ((5, 6), (7, 6), (4, 7), (5, 7), (6, 7), (7, 7), (8, 7), (5, 8), (6, 8), (7, 8), (6, 9)):
        tp[x, y] = (216, 58, 90, 255)
    for y in range(9, 14):                                # an ink pot
        for x in range(11, 14):
            tp[x, y] = (30, 30, 46, 255)
    for x in range(3, 13):                                # a drawer with a brass handle on each side
        sp[x, 5] = sp[x, 10] = (70, 46, 24, 255)
    for y in range(5, 11):
        sp[3, y] = sp[12, y] = (70, 46, 24, 255)
    sp[7, 7] = sp[8, 7] = (242, 193, 78, 255)
    tex = os.path.join(ASSETS, "textures", "block")
    top.save(os.path.join(tex, "exchange_desk_top.png"))
    side.save(os.path.join(tex, "exchange_desk_side.png"))
    dump(os.path.join(ASSETS, "models", "block", "exchange_desk.json"), {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "top": "harmontide:block/exchange_desk_top", "bottom": "minecraft:block/oak_planks", "side": "harmontide:block/exchange_desk_side"}})
    dump(os.path.join(ASSETS, "blockstates", "exchange_desk.json"), {"variants": {"": {"model": "harmontide:block/exchange_desk"}}})
    dump(os.path.join(ASSETS, "models", "item", "exchange_desk.json"), {"parent": "harmontide:block/exchange_desk"})
    dump(os.path.join(DATA, "loot_table", "blocks", "exchange_desk.json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": "harmontide:exchange_desk"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    dump(os.path.join(DATA, "recipe", "exchange_desk.json"), {"type": "minecraft:crafting_shaped", "category": "misc",
                                                               "pattern": ["PBP", "WWW", "W W"],
                                                               "key": {"P": {"item": "minecraft:paper"}, "B": {"item": "minecraft:book"}, "W": {"tag": "minecraft:planks"}},
                                                               "result": {"id": "harmontide:exchange_desk", "count": 1}})
    return top, side


def permit():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(2, 15):
        for x in range(3, 13):
            px[x, y] = (244, 240, 228, 255) if x not in (3, 12) and y not in (2, 14) else (180, 170, 150, 255)
    for y in (4, 6, 8):
        for x in range(5, 11):
            px[x, y] = (110, 110, 130, 255)
    for x, y in ((8, 10), (10, 10), (7, 11), (8, 11), (9, 11), (10, 11), (11, 11), (8, 12), (9, 12), (10, 12), (9, 13)):
        px[x, y] = (216, 58, 90, 255)
    img.save(os.path.join(ASSETS, "textures", "item", "homestay_permit.png"))
    dump(os.path.join(ASSETS, "models", "item", "homestay_permit.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": "harmontide:item/homestay_permit"}})
    return img


def main():
    prof = profession()
    top, side = desk()
    p = permit()
    if "--preview" in sys.argv:
        sheet = Image.new("RGBA", (4 * 136 + 10, 330), (40, 40, 48, 255))
        for i, im in enumerate([prof[0], prof[1]]):
            b = im.resize((im.width * 4, im.height * 4), Image.NEAREST)
            sheet.paste(b, (10 + i * 270, 10), b)
        for i, im in enumerate([top, side, p]):
            b = im.resize((64, 64), Image.NEAREST)
            sheet.paste(b, (10 + i * 74, 260), b)
        path = sys.argv[sys.argv.index("--preview") + 1]
        os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
        sheet.save(path)


if __name__ == "__main__":
    main()
