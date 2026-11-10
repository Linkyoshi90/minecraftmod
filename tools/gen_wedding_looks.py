#!/usr/bin/env python3
"""The harpies' and frogs' wedding looks (2026-11-10): textures/entity/wedding_looks.png, a 2x2 grid of 16px cells that
client/WeddingLookLayer fills its boxes from: 0 a white feather, 1 sheer lace, 2 a lily pad, 3 a pink blossom.

    uv run --no-project --with pillow python tools/gen_wedding_looks.py
"""
import os

from PIL import Image

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "monstergirls",
                   "textures", "entity", "wedding_looks.png")


def main():
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    px = img.load()
    for y in range(16):                                   # 0: a feather: white vanes either side of a pale quill
        for x in range(16):
            barb = (x + y) % 3 == 0
            c = (236, 236, 242, 255) if not barb else (214, 216, 228, 255)
            if x in (7, 8):
                c = (200, 196, 186, 255)
            px[x, y] = c
    for y in range(16):                                   # 1: sheer lace, with a dotted pattern
        for x in range(16):
            dot = (x % 4 == 1 and y % 4 == 1) or (x % 4 == 3 and y % 4 == 3)
            px[16 + x, y] = (255, 255, 255, 200) if dot else (250, 248, 255, 120)
            if y in (0, 15):
                px[16 + x, y] = (255, 255, 255, 230)
    for y in range(16):                                   # 2: a lily pad, veined from the middle
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            vein = abs(dx) < 0.6 or abs(dy) < 0.6 or abs(abs(dx) - abs(dy)) < 0.7
            shade = 4 * ((x * 7 + y * 3) % 3)
            px[x, 16 + y] = (70 + shade, 140 + shade, 52, 255) if not vein else (120, 182, 86, 255)
    for y in range(16):                                   # 3: a pink blossom, petals round a yellow heart
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            r = (dx * dx + dy * dy) ** 0.5
            if r < 2.5:
                c = (250, 214, 90, 255)
            elif (x + y) % 5 == 0:
                c = (232, 128, 172, 255)
            else:
                c = (248, 170, 204, 255)
            px[16 + x, 16 + y] = c
    img.save(OUT)
    print("wrote", os.path.normpath(OUT))


if __name__ == "__main__":
    main()
