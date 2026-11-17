#!/usr/bin/env python3
"""Sparrow Harpy, soft model - the detailed sparrow with a rounder, softer face.

    python tools/gen_sparrow_soft_harpy.py                 # write geometry + textures
    python tools/gen_sparrow_soft_harpy.py --preview x.png # also render preview sheets (x.png, x_face.png, x_body.png)
    python tools/gen_sparrow_soft_harpy.py --lineup x.png  # also render all colour variants side by side
    python tools/gen_sparrow_soft_harpy.py --compare x.png # detailed vs soft face, side by side

Everything below the neck is the detailed sparrow (gen_sparrow_detailed_harpy.py), and the bones are the same, so it
reuses sparrow_harpy.animation.json too. Only geo/entity/sparrow_harpy_soft.geo.json and
textures/entity/sparrow_harpy_soft[_<variant>].png are written; the renderer picks it when the client config options
`detailedSparrowModel` and `softSparrowFace` are both on.

What changes over the detailed sparrow:
  * the head box loses its square jaw: the lower face steps in (8 -> 7.5 -> 6.5 -> 5 -> 3.5 wide) into a round chin
  * the hair cap is a dome instead of a box: its vertical corners are cut back and two stepped layers round the crown
  * rounder eyes with rounder pupils, softer brows, a small curved smile
  * the face is shaded like a curved surface (lighter in the middle, warmer towards the cheeks and jaw) with a
    larger, softer blush
The eyelid sheet and mouth stay where they were (face plane z = -4), so blinking and singing work unchanged.
"""
import copy
import json
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import gen_sparrow_harpy as S
import gen_sparrow_detailed_harpy as DT
from harpy_lib import cube, pick, noise, CLEAR

NAME = "sparrow_harpy_soft"

# the lower face, as (part, half width, bottom y, top y, depth); the face plane stays at z = -4
JAW = [("head_core", 4.0, 21.5, 28.0, 8.0), ("jaw1", 3.75, 21.0, 21.5, 7.5), ("jaw2", 3.25, 20.5, 21.0, 7.0),
       ("jaw3", 2.5, 20.25, 20.5, 6.5), ("chin", 1.75, 20.0, 20.25, 6.0)]
# the hair dome, as (part, origin, size)
CAP = [("cap_a", [-4.75, 19.25, -4.25], (9.5, 8.5, 8.5)), ("cap_b", [-4.25, 19.25, -4.75], (8.5, 8.5, 9.5)),
       ("cap_top", [-4.25, 27.75, -4.25], (8.5, 0.75, 8.5)), ("cap_crown", [-3.25, 28.5, -3.25], (6.5, 0.5, 6.5))]
CAP_TOP_Y = 27.75                         # top of cap_a / cap_b; the old box cap reached 28.75


def soft_pal(base):
    """a copy of a sparrow palette with the rounder face knobs"""
    pal = copy.copy(base)
    pal.eye_a, pal.eye_b = 4.5, 5.6           # rounder (the plain eye is 4 x 6)
    pal.pupil = (2.1, 2.7)
    pal.hl_dy, pal.hl2_dy = 3, 2
    pal.BROW = pal.HAIR[2]
    pal.brows = [(8, 10, -3, 2, 0, 1), (24, 10, -2, 3, 0, 1)]
    pal.mouth_px = {(14, 27): pal.LIP, (15, 28): pal.LIP, (16, 28): pal.LIP, (17, 27): pal.LIP,
                    (15, 29): pal.SKIN[3], (16, 29): pal.SKIN[3]}
    return pal


def declare_parts():
    DT.declare_parts()
    P = H.part
    # the 32 x 32 face is still painted on `head` by harpy_lib.paint_head, but only as a source the new parts sample
    P("head", 8, 8, 8, skip=("east", "west", "up", "down", "south"))
    P("hair_cap", 1, 1, 1, skip=H.FACES)
    for name, hw, y0, y1, d in JAW:
        P(name, hw * 2, y1 - y0, d)
    for name, _, size in CAP:
        P(name, *size, skip=("down",))


# ----------------------------------------------------------------------------- texture
def soft_skin(pal, x, y):
    """bare skin of the 32 x 32 face at texel (x, y), shaded like a curved surface, with the soft blush"""
    u, v = (x + 0.5 - 16) / 16, (y + 0.5 - 17) / 16
    t = 0.80 - 0.20 * (u * u + v * v) + (noise(x, y, 103) - 0.5) * 0.05
    if y < 4:
        t -= 0.14 * (4 - y) / 4                       # shadow under the fringe
    if y >= 26:
        t -= 0.04 * (y - 25)                          # the jaw turns away
    for cx in (7.5, 24.5):                             # blush: soft oval, dithered rim
        e = ((x + 0.5 - cx) / 4.4) ** 2 + ((y + 0.5 - 25.5) / 2.0) ** 2
        if e <= 0.40:
            return pal.BLUSH
        if e <= 0.80 or (e <= 1.15 and (x + y) % 2 == 0):
            return pal.BLUSH_L
    return pick(pal.SKIN, t, x, y)


def soften_face(F, pal):
    """reshade the skin of the 32 x 32 face so it reads as a curved surface, and widen the blush"""
    keep = set(pal.SKIN) | {pal.BLUSH, pal.BLUSH_L}
    return [[soft_skin(pal, x, y) if F[y][x] in keep else F[y][x] for x in range(32)] for y in range(32)]


def hair_px(pal, x, y, h, gloss=False):
    """harpy_lib.paint_hair's strand colouring (x, y in texels of a face h texels tall)"""
    t = (pal.streak[(x // 2) % 7] * 0.8 + (y / max(1, h - 1)) * 0.32 + (noise(x, y, 7) - 0.5) * 0.10) * pal.hair_gain + pal.hair_off
    if gloss and 0.16 < y / max(1, h - 1) < 0.30:
        t += 0.22
    if y > h - 3:
        t += 0.10
    return pick(pal.HAIR, t, x, y)


def paint(pal=DT.PAL, LEAF=S.LEAF, GOLD=S.GOLD):
    pal = soft_pal(pal)
    T = DT.paint(pal, LEAF, GOLD)
    pt = H.Painter()
    pt.T = T
    D = H.D

    # ---- the face, cut up over the stepped jaw parts
    fx0, fy0, _, _ = H.RECT[("head", "north")]
    F = soften_face([T[fy0 + y][fx0:fx0 + 32] for y in range(32)], pal)
    skin_side = H.skin_fn(pal)
    for name, hw, y0, y1, d in JAW:
        ox, oy = int(round((4 - hw) * D)), int(round((28 - y1) * D))

        def face(f, x, y, w, h, ox=ox, oy=oy):
            c = F[oy + y][ox + x]
            if (x == 0 or x == w - 1) and c in pal.SKIN:
                c = pal.SKIN[max(0, pal.SKIN.index(c) - 1)]      # the cheek turns away at the silhouette
            return c
        pt(name, "all", skin_side)
        pt(name, ("north",), face)

    # ---- the closed-eye sheet gets the same shading, so a blink does not cut the blush
    lx0, ly0, lw, lh = H.RECT[("eyelids", "north")]
    for y in range(lh):
        for x in range(lw):
            if T[ly0 + y][lx0 + x] in pal.SKIN:
                T[ly0 + y][lx0 + x] = soft_skin(pal, x + 3, y + 11)      # the sheet's origin is face texel (3, 11)

    # ---- the hair dome
    def cap(name, origin, size):
        x0, y0, z0 = origin
        w_, h_, d_ = size
        z1 = z0 + d_

        def fn(f, x, y, w, h):
            # y_world of this texel row and how far below the old box cap's top it is (keeps the strands continuous)
            yw = y0 + h_ - (y + 0.5) / D
            yy = int((28.75 - yw) * D)
            if f == "up":
                if name == "cap_crown" and abs(x - w // 2 - (w // 2 - y) // 3) == 0 and y > 2:
                    return pal.HAIR[1]                             # parting line
                return hair_px(pal, x, y, h, True)
            if f == "north":
                return hair_px(pal, x, yy, 38, True) if yy < 5 else CLEAR
            if yw < 22.25 and noise(x, y + int(y0 * D), 9) > 0.45:
                return CLEAR                                       # ragged lower edge
            if f in ("east", "west"):
                z = (z1 - (x + 0.5) / D) if f == "east" else (z0 + (x + 0.5) / D)
                if -2.25 <= z <= 1.5 and 22.25 <= yw <= 25.25:
                    return CLEAR                                   # around the ear
            return hair_px(pal, x, yy, 38, True)
        pt(name, "all", fn)
    for name, origin, size in CAP:
        cap(name, origin, size)
    return pt.T


# ----------------------------------------------------------------------------- geometry
def build_model():
    m = DT.build_model()
    m.name = NAME
    head = next(b for b in m.bones if b["name"] == "head")
    gone = {json.dumps(cube(p, [0, 0, 0])["uv"]) for p in ("head", "hair_cap")}
    rest = [c for c in head["cubes"] if json.dumps(c["uv"]) not in gone]
    new = [cube(name, [-hw, y0, -4]) for name, hw, y0, y1, d in JAW]
    new += [cube(name, origin) for name, origin, _ in CAP]
    head["cubes"] = new + rest                      # head_core stays the bone's first cube
    return m


# ----------------------------------------------------------------------------- output
def compare(path):
    """the detailed face next to the soft one, front and 3/4"""
    bg, W = (44, 48, 44, 255), 420
    shots = []
    for mod in (DT, sys.modules[__name__]):
        H.standard_parts()
        mod.declare_parts()
        H.pack()
        model = mod.build_model().doc()
        tex = mod.paint()
        idle = DT.anims_doc(model)["animations"][f"animation.{S.NAME}.idle"]
        shots.append([H.render(model, tex, idle, 0, yaw, pitch, W, W, 36, bg, center=(0, 23.5)) for yaw, pitch in ((0, 0), (-35, 8), (90, 0))])
    sheet = [[bg] * (W * 3) for _ in range(W * 2)]
    for r, row in enumerate(shots):
        for c, im in enumerate(row):
            for y in range(W):
                sheet[r * W + y][c * W:(c + 1) * W] = im[y]
    H.write_png(path, sheet)
    print("compare ->", path, "(top: detailed, bottom: soft)")


def main():
    H.standard_parts()
    declare_parts()
    H.pack()
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide", "textures", "entity")
    geo = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide", "geo", "entity", NAME + ".geo.json")
    model = build_model().doc()
    texes = {"house": paint()}
    H.write_png(os.path.join(tex_dir, NAME + ".png"), texes["house"])
    for v, (pal, leaf) in S.VARIANTS.items():
        texes[v] = paint(pal, leaf)
        H.write_png(os.path.join(tex_dir, f"{NAME}_{v}.png"), texes[v])
    with open(geo, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(model, fh, indent=2)
        fh.write("\n")
    bones = model["minecraft:geometry"][0]["bones"]
    used = sum(w * h for (_, _, w, h) in H.RECT.values())
    print(f"{NAME}: texture {H.TEX}x{H.TEX} ({used / H.TEX / H.TEX:.0%} used), {len(bones)} bones / "
          f"{sum(len(b.get('cubes', [])) for b in bones)} cubes, {len(S.VARIANTS)} colour variants; animations: {S.NAME}")
    anims = DT.anims_doc(model)
    if "--preview" in sys.argv:
        out = sys.argv[sys.argv.index("--preview") + 1]
        H.preview(out, S.NAME, model, texes["house"], anims)
        H.write_png(os.path.splitext(out)[0] + "_texture.png", H.upscale(texes["house"], 2))
        print("preview ->", out)
    if "--lineup" in sys.argv:
        DT.lineup(sys.argv[sys.argv.index("--lineup") + 1], model, anims, texes)
    if "--compare" in sys.argv:
        compare(sys.argv[sys.argv.index("--compare") + 1])


if __name__ == "__main__":
    main()
