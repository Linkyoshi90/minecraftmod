#!/usr/bin/env python3
"""Portrait paintings (2026-11-09): vanilla painting variants of the girls, cut from their model previews (previews/<id>_preview.png,
front view), as 2x2-block busts in a wooden frame on a backdrop of her home's colour. Data-driven painting variants, in the
minecraft:placeable tag, so a painting hung on a wall can come up as one of them.

    uv run --no-project --with pillow python tools/gen_portraits.py [--sheet]
"""
import json
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_milk_assets import add_lang  # noqa: E402

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, "src", "main", "resources", "assets", "monstergirls")
DATA = os.path.join(REPO, "src", "main", "resources", "data", "monstergirls")
BG = (44, 48, 44)

# preview id -> (title, backdrop top colour, backdrop bottom colour)
PORTRAITS = {
    "albatross_harpy": ("The Albatross", (120, 170, 210), (40, 80, 130)),
    "akaname_devil": ("The Bath Spirit", (90, 70, 120), (40, 30, 60)),
    "alraune_flora": ("Alraune in Bloom", (150, 190, 110), (60, 100, 50)),
    "amarok_canid": ("Wolf of the North", (200, 210, 230), (90, 100, 130)),
    "alpaca_centaur": ("The Alpaca", (220, 190, 140), (130, 100, 70)),
    "ahuizotl_mermaid": ("Lady of the Lake", (80, 160, 170), (20, 60, 80)),
    "alebrije_dragon": ("The Alebrije", (230, 140, 90), (120, 50, 90)),
    "al_miraj_jackalope": ("Al-Mi'raj", (230, 210, 160), (150, 120, 80)),
    "crow_harpy": ("The Crow", (150, 140, 170), (50, 45, 70)),
    "owl_harpy": ("Night Watch", (60, 70, 120), (20, 20, 50)),
    "phoenix_harpy": ("Phoenix Rising", (250, 190, 90), (170, 50, 30)),
    "serval_nekomata": ("Serval at Noon", (240, 210, 120), (170, 130, 60)),
    "red_kitsune": ("Fox of the Shrine", (230, 120, 90), (120, 40, 40)),
    "python_lamia": ("The Python", (150, 170, 90), (60, 80, 40)),
    "orb_weaver_arachne": ("The Weaver", (180, 180, 160), (70, 70, 60)),
    "horse_centaur": ("Bay Mare", (170, 200, 120), (80, 110, 60)),
    "holstaur_minotaur": ("Milkmaid", (190, 220, 150), (100, 140, 80)),
    "succubus_devil": ("Succubus", (170, 90, 160), (60, 20, 70)),
    "lilith_devil": ("Lilith", (120, 50, 120), (30, 10, 40)),
    "vampire_undead": ("The Countess", (140, 40, 60), (40, 10, 20)),
    "yurei_undead": ("Yurei", (150, 170, 190), (50, 60, 80)),
    "valkyrie_elf": ("Valkyrie", (200, 210, 230), (110, 120, 150)),
    "wood_elf": ("Elf of the Wood", (110, 160, 90), (40, 70, 40)),
    "honey_slime": ("Honey", (250, 210, 110), (190, 130, 40)),
    "wyvern_dragon": ("Wyvern", (170, 120, 90), (80, 50, 40)),
    "kraken_mollusk": ("The Deep", (60, 100, 140), (10, 30, 50)),
    "yeti_canid": ("Yeti", (220, 235, 245), (130, 160, 190)),
    "cave_bat": ("Cave Bat", (90, 80, 100), (30, 25, 40)),
    "bullywug": ("The Bullywug", (130, 170, 100), (50, 80, 50)),
}
FRAME = [(92, 60, 32), (130, 88, 48), (70, 44, 24)]


def bust(src):
    im = Image.open(src).convert("RGBA")
    w, h = im.size
    panel = im.crop((0, 0, w // 4, h // 2))
    px = panel.load()
    xs, ys = [], []
    for y in range(panel.height):
        for x in range(panel.width):
            r, g, b, a = px[x, y]
            if abs(r - BG[0]) + abs(g - BG[1]) + abs(b - BG[2]) > 12:
                xs.append(x)
                ys.append(y)
    top, bottom = min(ys), max(ys)
    cx = (min(xs) + max(xs)) // 2
    side = int((bottom - top) * 0.55)
    box = (cx - side // 2, top - side // 12, cx + side // 2, top - side // 12 + side)
    cut = panel.crop(box)
    # the preview's dark backdrop becomes transparent, so the painting's own sky shows behind her
    cp = cut.load()
    for y in range(cut.height):
        for x in range(cut.width):
            r, g, b, a = cp[x, y]
            if abs(r - BG[0]) + abs(g - BG[1]) + abs(b - BG[2]) <= 12:
                cp[x, y] = (0, 0, 0, 0)
    return cut


def painting(src, top, bottom, size=32):
    inner = size - 4
    img = Image.new("RGBA", (size, size))
    for y in range(size):
        t = y / (size - 1)
        col = tuple(int(top[i] * (1 - t) + bottom[i] * t) for i in range(3)) + (255,)
        for x in range(size):
            img.putpixel((x, y), col)
    b = bust(src).resize((inner, inner), Image.Resampling.BOX)
    # harden the edge: half-transparent pixels either show or don't (no smeared halo at 16 px a block)
    bp = b.load()
    for y in range(inner):
        for x in range(inner):
            r, g, bb, a = bp[x, y]
            bp[x, y] = (r, g, bb, 255 if a >= 128 else 0)
    img.alpha_composite(b, (2, 2))
    for i in range(size):
        for d, col in ((0, FRAME[2]), (1, FRAME[1])):
            img.putpixel((i, d), col)
            img.putpixel((d, i), col)
            img.putpixel((i, size - 1 - d), FRAME[0] if d == 0 else FRAME[1])
            img.putpixel((size - 1 - d, i), FRAME[0] if d == 0 else FRAME[1])
    return img.convert("RGB")


def dump(path, doc):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(doc, fh, indent=2)
        fh.write("\n")


def main():
    lang, ids = {}, []
    tex_dir = os.path.join(ASSETS, "textures", "painting")
    os.makedirs(tex_dir, exist_ok=True)
    sheet = []
    for kind, (title, top, bottom) in PORTRAITS.items():
        pid = "portrait_" + kind
        img = painting(os.path.join(REPO, "previews", kind + "_preview.png"), top, bottom)
        img.save(os.path.join(tex_dir, pid + ".png"))
        sheet.append(img)
        dump(os.path.join(DATA, "painting_variant", pid + ".json"), {
            "asset_id": f"monstergirls:{pid}", "width": 2, "height": 2})
        lang[f"painting.monstergirls.{pid}.title"] = title
        lang[f"painting.monstergirls.{pid}.author"] = "Harmontide"
        ids.append(f"monstergirls:{pid}")
    dump(os.path.join(REPO, "src", "main", "resources", "data", "minecraft", "tags", "painting_variant", "placeable.json"),
         {"replace": False, "values": ids})
    lang["hobby.monstergirls.painting"] = "painting"
    lang["message.monstergirls.hobby.shared.painting"] = ("%s: She sits you down, sketches you twice, then hands you the brush for the sky. "
                                                          "Whatever she finished is yours.")
    if "--no-lang" not in sys.argv:
        add_lang(lang)
    if "--sheet" in sys.argv:
        out = Image.new("RGB", (8 * 136, ((len(sheet) + 7) // 8) * 136), (30, 30, 30))
        for i, im in enumerate(sheet):
            out.paste(im.resize((128, 128), Image.Resampling.NEAREST), ((i % 8) * 136 + 4, (i // 8) * 136 + 4))
        out.save(os.path.join(REPO, "previews", "portraits_sheet.png"))
    print(f"{len(ids)} portraits")


if __name__ == "__main__":
    main()
