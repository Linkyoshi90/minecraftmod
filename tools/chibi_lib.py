"""Small helpers for the young-stage models (harpy hatchlings, tadpoles, froglets), on top of harpy_lib.

Each young model is its own little geometry with its own texture and animation file, written next to the adults:

    geo/entity/<name>.geo.json, animations/entity/<name>.animation.json, textures/entity/<name>.png

Sizes are authored at a base scale and multiplied by a per-species factor `k`; every part size is snapped to
quarter units so all faces land on whole texels (D = 4 texels per unit), and positions are laid out from the
snapped sizes so nothing ends up with a hairline gap.

Pure standard library.
"""
import json
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
from harpy_lib import rgb, ramp, pick, noise, CLEAR, FACES

ASSETS = os.path.join(H.REPO, "src", "main", "resources", "assets", "monstergirls")


def q(v):
    """snap to a quarter unit (never below one quarter)"""
    return max(0.25, round(v * 4) / 4)


def declare(sizes, k):
    """sizes: {name: (w, h, d[, skipped faces])}; everything multiplied by k and snapped"""
    H.PARTS.clear()
    H.RECT.clear()
    for name, spec in sizes.items():
        w, h, d = spec[:3]
        skip = spec[3] if len(spec) > 3 else ()
        H.part(name, q(w * k), q(h * k), q(d * k), skip)
    H.pack()


def size(name):
    return H.PARTS[name]["size"]


# ----------------------------------------------------------------------------- painting helpers
def lerp_c(a, b, f):
    return tuple(int(round(a[i] + (b[i] - a[i]) * f)) for i in range(4))


def shade(c, d):
    """lighten (d > 0) or darken (d < 0) an rgba colour"""
    return (max(0, min(255, c[0] + d)), max(0, min(255, c[1] + d)), max(0, min(255, c[2] + d)), c[3])


def surface(rmp, seed, base=0.6, grain=0.30, deco=None):
    """a fluffy / skin surface from a dark->light ramp: noise grain, top lighter, underside darker, optional decorator"""
    def fn(f, x, y, w, h):
        t = base + (noise(x, y, seed) - 0.5) * grain
        if f == "up":
            t += 0.10
        elif f == "down":
            t -= 0.22
        elif f in ("east", "west"):
            t -= 0.06
        elif f == "south":
            t -= 0.10
        if deco is not None:
            r = deco(f, x, y, w, h, t)
            if r is not None:
                return r
        return pick(rmp, t, x, y)
    return fn


def ellipse(x, y, cx, cy, a, b):
    return ((x - cx) / a) ** 2 + ((y - cy) / b) ** 2


def paint_eye(T, cx, cy, a, b, iris, out, pupil=None, glint=True):
    """a big shiny chibi eye drawn straight into the texture grid T (list of rows) around (cx, cy)"""
    pupil = pupil or rgb(20, 14, 18)
    for y in range(int(cy - b - 1), int(cy + b + 2)):
        for x in range(int(cx - a - 1), int(cx + a + 2)):
            if y < 0 or x < 0 or y >= len(T) or x >= len(T[0]):
                continue
            e = ellipse(x + 0.5, y + 0.5, cx, cy, a, b)
            if e > 1.0:
                continue
            ty = (y + 0.5 - (cy - b)) / (2 * b)
            if e > 0.72:
                c = out
            else:
                c = pick(iris, 0.15 + 0.85 * ty, x, y)
                if ellipse(x + 0.5, y + 0.5, cx, cy + b * 0.12, a * 0.55, b * 0.62) < 1.0:
                    c = pupil
            T[y][x] = c
    if glint:
        gx, gy = int(cx - a * 0.4), int(cy - b * 0.55)
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
            T[gy + dy][gx + dx] = rgb(255, 255, 255)
        T[int(cy + b * 0.45)][int(cx + a * 0.4)] = rgb(236, 244, 255)


def blush(T, cx, cy, a, b, col):
    for y in range(int(cy - b), int(cy + b) + 1):
        for x in range(int(cx - a), int(cx + a) + 1):
            if ellipse(x + 0.5, y + 0.5, cx, cy, a, b) <= 1.0 and 0 <= y < len(T) and 0 <= x < len(T[0]):
                T[y][x] = lerp_c(T[y][x], col, 0.55)


# ----------------------------------------------------------------------------- output
def write_model(name, tex, model, anims, glow=None):
    """write geo / animation / texture (and an optional glow mask texture) for one young-stage model"""
    geo = model.doc()
    names = [b["name"] for b in geo["minecraft:geometry"][0]["bones"]]
    an = H.Anims(name, names)
    anims(an)
    adoc = an.doc()
    H.write_png(os.path.join(ASSETS, "textures", "entity", name + ".png"), tex)
    if glow is not None:
        H.write_png(os.path.join(ASSETS, "textures", "entity", name + "_glowmask.png"), glow)
    for sub, ext, data in (("geo", ".geo.json", geo), ("animations", ".animation.json", adoc)):
        path = os.path.join(ASSETS, sub, "entity", name + ext)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            json.dump(data, fh, indent=2)
            fh.write("\n")
    n_cubes = sum(len(b.get("cubes", [])) for b in geo["minecraft:geometry"][0]["bones"])
    print(f"{name}: texture {H.TEX}x{H.TEX}, {len(names)} bones / {n_cubes} cubes, {len(adoc['animations'])} animations")
    return geo, adoc


def lineup(path, entries, cols=4, cell=300, views=((-30, 12),), pixels=None):
    """contact sheet: entries are (geo, texture, animation dict or None, animation time, height in units)"""
    bg = (44, 48, 44, 255)
    rows = (len(entries) * len(views) + cols - 1) // cols
    sheet = [[bg] * (cell * cols) for _ in range(cell * rows)]
    i = 0
    for geo, tex, anim, tm, height in entries:
        for yaw, pitch in views:
            scale = pixels or (cell * 0.8 / max(4.0, height))
            im = H.render(geo, tex, anim, tm, yaw, pitch, cell, cell, scale, bg, center=(0, height * 0.5))
            ox, oy = (i % cols) * cell, (i // cols) * cell
            for y in range(cell):
                sheet[oy + y][ox:ox + cell] = im[y]
            i += 1
    H.write_png(path, sheet)
    print("lineup ->", path)


def chibi_face(pt, part, skin_fn, iris, eye_scale=1.0, eye_y=0.50, outline=None, cheek=None, mouth="smile", rim=None):
    """Paint the chibi face on `part`'s north face: skin patch (skin_fn gives the colour), two huge shiny eyes,
    cheek blush and a small mouth. Returns the (cx list, cy, a, b) of the eyes in texels relative to the face."""
    x0, y0, w, h = H.RECT[(part, "north")]
    T = pt.T
    for yy in range(h):
        for xx in range(w):
            T[y0 + yy][x0 + xx] = skin_fn("north", xx, yy, w, h)
    a, b = 3.3 * eye_scale, 4.4 * eye_scale
    ey = h * eye_y
    cxs = (w * 0.28, w * 0.72)
    for cx in cxs:
        paint_eye(T, x0 + cx, y0 + ey, a, b, iris, outline or rgb(30, 22, 22), glint=True)
        if cheek:
            blush(T, x0 + cx + (-1 if cx < w / 2 else 1), y0 + ey + b + 2.3, 2.6, 1.5, cheek)
    my = int(h * 0.86)
    mx = int(w / 2)
    if mouth == "smile":
        for dx, dy in ((-2, 0), (-1, 1), (0, 1), (1, 1), (2, 0)):
            T[y0 + my + dy][x0 + mx + dx] = rgb(120, 60, 60)
    elif mouth == "frown":
        for dx, dy in ((-2, 1), (-1, 0), (0, 0), (1, 0), (2, 1)):
            T[y0 + my + dy][x0 + mx + dx] = rgb(110, 52, 52)
    return cxs, ey, a, b


def eyelids_part(pt, part, head_w_units, skin_fn, a, hlines=(-0.22, 0.22)):
    """skin-coloured eyelids with a closed-eye curve at each eye position (hlines: eye centres as fractions of head width)"""
    lx0, ly0, lw, lh = H.RECT[(part, "north")]
    T = pt.T
    elw = H.PARTS[part]["size"][0]
    for yy in range(lh):
        for xx in range(lw):
            T[ly0 + yy][lx0 + xx] = skin_fn("north", xx, yy, lw, lh)
    for fr in hlines:
        ecx = (fr * head_w_units + elw / 2) * H.D
        for dx in range(-int(a), int(a) + 1):
            dy = int(round(1.2 * (1 - (dx / max(1, a)) ** 2)))
            T[ly0 + int(lh / 2) - dy][lx0 + int(ecx) + dx] = rgb(60, 34, 34)
    for f in ("east", "west", "south", "up", "down"):
        px0, py0, pw, ph = H.RECT[(part, f)]
        for yy in range(ph):
            for xx in range(pw):
                T[py0 + yy][px0 + xx] = skin_fn(f, xx, yy, pw, ph)
