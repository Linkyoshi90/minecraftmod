#!/usr/bin/env python3
"""Glowing violet eyes for creatures the Demon Realm possessed (entity/Monsterization#POSSESSED, client/PossessedEyesLayer).

    uv run --no-project --with pillow python tools/gen_possessed_eyes.py

One texture per vanilla model, the size of its vanilla texture, black and clear everywhere but the eyes (drawn with the
"eyes" render type, which adds light, as a spider's eyes). The eye pixels were read off the 1.21.1 textures: the pig's on
row 11, the villager's on row 14, every cat's on row 6, the chicken's on row 4.
"""
import os

from PIL import Image

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "monstergirls",
                   "textures", "entity", "possessed")
GLOW, PUPIL = (240, 150, 255, 255), (200, 60, 255, 255)

EYES = {
    # name: (size, [(x, y, colour)])
    "pig_eyes": ((64, 32), [(8, 11, PUPIL), (9, 11, GLOW), (14, 11, GLOW), (15, 11, PUPIL)]),
    "villager_eyes": ((64, 64), [(9, 14, GLOW), (10, 14, PUPIL), (13, 14, PUPIL), (14, 14, GLOW)]),
    "cat_eyes": ((64, 32), [(5, 6, GLOW), (6, 6, PUPIL), (8, 6, PUPIL), (9, 6, GLOW)]),
    "chicken_eyes": ((64, 32), [(3, 4, GLOW), (6, 4, GLOW)]),           # the wandering trader wears the villager's
}


def horns():
    """the little horns (client/PossessedEyesLayer draws them on the head): dark plum at the root to violet at the tip, 16x16"""
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    ramp = [(0x2A, 0x14, 0x30), (0x3E, 0x1E, 0x46), (0x56, 0x2A, 0x62), (0x74, 0x3A, 0x84), (0x9A, 0x52, 0xAE), (0xC0, 0x78, 0xD8)]
    for y in range(16):
        for x in range(16):
            c = ramp[min(5, (15 - y) * 6 // 16)] if x < 8 else ramp[min(5, 2 + (15 - y) * 4 // 16)]   # left half the base, right the tip
            shade = 0.85 if (x + y) % 3 == 0 else 1.0
            im.putpixel((x, y), tuple(int(v * shade) for v in c) + (255,))
    im.save(os.path.join(OUT, "horns.png"))
    print("horns (16, 16)")


def main():
    os.makedirs(OUT, exist_ok=True)
    horns()
    for name, (size, px) in EYES.items():
        im = Image.new("RGBA", size, (0, 0, 0, 0))
        for x, y, c in px:
            im.putpixel((x, y), c)
        im.save(os.path.join(OUT, name + ".png"))
        print(name, size)


if __name__ == "__main__":
    main()
