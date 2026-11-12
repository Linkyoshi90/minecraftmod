#!/usr/bin/env python3
"""Wardrobe outfits painted onto each girl's own model (2026-11-10, client/OutfitLayer).

The wardrobe's clothes (entity/Wardrobe: raincoat, winter coat, yukata, maid apron) used to be a few plain boxes around her torso,
which read as a coloured cloth rather than clothing. This tool reads every girl model (geo/entity/<name>.geo.json), finds her body,
sleeves (arm_*, forearm_*), skirt and legs, and paints each outfit onto a transparent texture with her own UV layout:
textures/entity/outfit/<name>_<outfit>.png. OutfitLayer draws the model again with that texture over her, the way the dyed wool and
the paints are drawn, so the clothes follow her shape and every pose. Hems sit by her own landmarks (hip = the body bone's pivot,
knee = the shin's pivot, ankle = the foot's), so a lamia's tail, a centaur's barrel or a mermaid's fins are never dressed.

    uv run --no-project --with pillow python tools/gen_outfit_overlays.py [--sheet name]
"""
import glob
import json
import os
import random
import re
import sys

from PIL import Image

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, "src", "main", "resources", "assets", "harmontide")
GEO = os.path.join(ASSETS, "geo", "entity")
TEX = os.path.join(ASSETS, "textures", "entity")
OUT = os.path.join(TEX, "outfit")
SPECIES = os.path.join(REPO, "src", "main", "java", "com", "linkyoshi90", "harmontide", "entity", "girl", "GirlSpecies.java")
OUTFITS = ("raincoat", "winter_coat", "yukata", "maid_apron")
SIDES = ("north", "south", "east", "west")


def rgb(h):
    return ((h >> 16) & 255, (h >> 8) & 255, h & 255, 255)


def jitter(c, rnd, amount=10):
    d = rnd.randint(-amount, amount)
    return tuple(max(0, min(255, v + d)) for v in c[:3]) + (255,)


def species_ids():
    with open(SPECIES, encoding="utf-8") as fh:
        return set(re.findall(r'^\s+[A-Z_0-9]+\("([a-z_0-9]+)", Family\.', fh.read(), re.M))


def landmarks(bones):
    """Hip, knee, ankle and neck heights in model units, from her own bones (with fallbacks for bodies without legs)."""
    body = bones["body"]
    hip = body["pivot"][1]
    neck = bones["head"]["pivot"][1] if "head" in bones else hip + 8
    knee = bones["shin_r"]["pivot"][1] if "shin_r" in bones else hip - 4.5
    ankle = bones["foot_r"]["pivot"][1] if "foot_r" in bones else knee - 5.5
    wrist = bones["hand_r"]["pivot"][1] if "hand_r" in bones else None
    return dict(hip=hip, neck=neck, knee=knee, ankle=ankle, wrist=wrist)


def faces(cube, scale):
    """(face name, x0, y0, x1, y1) in texture pixels for each face with a per-face UV (box UVs are expanded)."""
    uv = cube.get("uv")
    if uv is None:
        return []
    if isinstance(uv, list):                                    # box UV: the classic unfolded layout
        u, v = uv
        w, h, d = cube["size"]
        rects = {"up": (u + d, v, w, d), "down": (u + d + w, v, w, d), "east": (u, v + d, d, h), "north": (u + d, v + d, w, h),
                 "west": (u + d + w, v + d, d, h), "south": (u + d + w + d, v + d, w, h)}
    else:
        rects = {k: (f["uv"][0], f["uv"][1], f["uv_size"][0], f["uv_size"][1]) for k, f in uv.items() if "uv" in f and "uv_size" in f}
    out = []
    for name, (u, v, w, h) in rects.items():
        x0, x1 = sorted((u * scale, (u + w) * scale))
        y0, y1 = sorted((v * scale, (v + h) * scale))
        out.append((name, int(round(x0)), int(round(y0)), int(round(x1)), int(round(y1)), h < 0))
    return out


class Painter:
    def __init__(self, size, seed):
        self.img = Image.new("RGBA", size, (0, 0, 0, 0))
        self.px = self.img.load()
        self.rnd = random.Random(seed)

    def face(self, rect, top, bottom, colour_at):
        """Paints one face: colour_at(y, col, row, width, height) gives the colour (or None) for the pixel whose model height is y."""
        name, x0, y0, x1, y1, flipped = rect
        w, h = x1 - x0, y1 - y0
        if w <= 0 or h <= 0:
            return
        W, H = self.img.size
        for row in range(h):
            if name in ("up", "down"):
                y = top if name == "up" else bottom
            else:
                t = (row + 0.5) / h
                if flipped:
                    t = 1 - t
                y = top - t * (top - bottom)
            for col in range(w):
                c = colour_at(y, col, row, w, h)
                if c is not None and 0 <= x0 + col < W and 0 <= y0 + row < H:
                    self.px[x0 + col, y0 + row] = c


def paint(name, outfit, geo, size, scale):
    bones = {b["name"]: b for b in geo["bones"]}
    if "body" not in bones:
        return None
    lm = landmarks(bones)
    hip, neck, knee, ankle = lm["hip"], lm["neck"], lm["knee"], lm["ankle"]
    p = Painter(size, hash((name, outfit)) & 0xFFFF)
    rnd = p.rnd
    torso = neck - hip
    if outfit == "raincoat":
        main, hem, sleeves = rgb(0xF2C230), (hip + knee) / 2, ("arm", "forearm")
    elif outfit == "winter_coat":
        main, hem, sleeves = rgb(0x9E2B2B), knee + 0.5, ("arm", "forearm")
    elif outfit == "yukata":
        main, hem, sleeves = rgb(0x26346B), ankle + 0.6, ("arm", "forearm")
    else:
        main, hem, sleeves = rgb(0x24242A), (hip + knee) / 2 + 0.5, ("arm",)
    fur, white, obi, gold, dark = rgb(0xEFEDE6), rgb(0xF4F4F1), rgb(0xD23A3A), rgb(0xE8C04A), rgb(0x3A3A40)
    obi_lo, obi_hi = hip + torso * 0.22, hip + torso * 0.42
    apron_top = hip + torso * 0.55

    def cloth(y, col, row, w, h, part, face, top, bottom):
        if y < hem:
            return None
        if outfit == "raincoat":
            if part == "body" and face == "north" and abs(col - w / 2) < 1 and row % 5 == 2:
                return dark                                            # the buttons down her front
            return jitter(main, rnd, 6)
        if outfit == "winter_coat":
            if y < hem + 0.6 or (part == "body" and y > neck - 0.6):
                return jitter(fur, rnd, 8)                             # fur hem and collar
            if part == "forearm" and y < bottom + 0.5:
                return jitter(fur, rnd, 8)                             # fur cuffs
            return jitter(main, rnd, 8) if (col + row) % 6 else jitter(main, rnd, 3)
        if outfit == "yukata":
            if part == "body" and obi_lo <= y <= obi_hi:
                return gold if abs(y - obi_lo) < 0.2 or abs(y - obi_hi) < 0.2 else jitter(obi, rnd, 6)
            if (col * 7 + row * 3) % 23 == 0:
                return rgb(0xF2B8CF)                                   # little flowers
            return jitter(main, rnd, 5)
        # maid: a black dress, a white apron over the front, collar and cuffs
        if part == "body" and y > neck - 0.5:
            return white
        if part == "arm" and y < bottom + 0.4:
            return white
        if face == "north" and part in ("body", "skirt", "leg") and y <= apron_top and abs(col - w / 2) < w * 0.42:
            return jitter(white, rnd, 4)
        return jitter(main, rnd, 4)

    painted = False
    for bone_name, bone in bones.items():
        part = None
        if bone_name == "body":
            part = "body"
        elif re.fullmatch(r"(arm|forearm)_[lr]", bone_name):
            part = bone_name.split("_")[0]
            if part not in sleeves:
                continue
        elif bone_name == "skirt":
            part = "skirt"
        elif re.fullmatch(r"(leg|shin)_[lr]", bone_name):
            part = "leg"
        if part is None:
            continue
        for cube in bone.get("cubes", []):
            top = cube["origin"][1] + cube["size"][1]
            bottom = cube["origin"][1]
            if top < hem:
                continue
            for rect in faces(cube, scale):
                face = rect[0]
                p.face(rect, top, bottom, lambda y, col, row, w, h, part=part, face=face, top=top, bottom=bottom:
                       cloth(y, col, row, w, h, part, face, top, bottom))
                painted = True
    return p.img if painted else None


def main():
    ids = species_ids()
    os.makedirs(OUT, exist_ok=True)
    done = 0
    sheet = sys.argv[sys.argv.index("--sheet") + 1] if "--sheet" in sys.argv else None
    for path in sorted(glob.glob(os.path.join(GEO, "*.geo.json"))):
        name = os.path.basename(path)[:-len(".geo.json")]
        if name.endswith("_male") or not any(name == s or name.startswith(s + "_") for s in ids):
            continue
        tex = os.path.join(TEX, name + ".png")
        if not os.path.exists(tex):
            continue
        with open(path, encoding="utf-8") as fh:
            geo = json.load(fh)["minecraft:geometry"][0]
        size = Image.open(tex).size
        scale = size[0] / geo["description"]["texture_width"]
        for outfit in OUTFITS:
            img = paint(name, outfit, geo, size, scale)
            if img is None:
                continue
            img.quantize(colors=48, method=Image.Quantize.FASTOCTREE).save(os.path.join(OUT, f"{name}_{outfit}.png"), optimize=True)   # few colours: small files
            done += 1
            if sheet == name:                                          # a quick look: the overlay over her own texture
                base = Image.open(tex).convert("RGBA")
                base.alpha_composite(img)
                base.save(os.path.join(REPO, "previews", f"outfit_{name}_{outfit}.png"))
    print(f"{done} outfit overlays")


if __name__ == "__main__":
    main()
