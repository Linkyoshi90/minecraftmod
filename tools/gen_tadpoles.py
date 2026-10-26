#!/usr/bin/env python3
"""Tadpoles: a tiny swimming tadpole for each frog and toad species (11; the Surinam toad has no tadpole stage).
Shared helpers: chibi_lib.py on top of harpy_lib.py.

    python tools/gen_tadpoles.py                   # write <species>_tadpole geo / animation / texture
    python tools/gen_tadpoles.py --preview x.png   # also render a contact sheet

Concept: a plump little oval body with two big shiny eyes and a tiny smile, feathery gills, and a tail in three
segments with a vertical fin that wiggles when she swims. Species differ in colour and pattern: the bullfrog and the
goliath are big and brown-olive, the poison dart frog is deep blue with cobalt spots, the glass frog is mint with a
see-through belly (a window cut into the texture), the cane toad is black, the fire-bellied toad has the red belly
already, the desert rain frog is a round sand-pink bean. Animations: idle (a lazy tail sway), swim (a quick wiggle).
Used by the game for the first life stage of an anuran (AbstractAnuran#isTadpole); in the water only.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import chibi_lib as C
from harpy_lib import rgb, ramp, pick, noise, cube, track, sym, merge, hold

D = H.D


def R(*cols):
    return ramp([rgb(*c) for c in cols])


SPECIES = {
    "bullywug": dict(k=1.0, back=R((34, 70, 28), (58, 110, 40), (88, 150, 58), (126, 188, 86)), belly=R((190, 190, 120), (222, 222, 150), (244, 240, 180)),
                     fin=R((70, 130, 70), (110, 176, 98)), gill=(210, 90, 80), iris=R((120, 90, 20), (190, 150, 30), (240, 206, 80)), pattern="blotch", pat=(30, 60, 24)),
    "bullfrog": dict(k=1.45, back=R((52, 56, 24), (84, 92, 36), (116, 124, 52), (152, 158, 74)), belly=R((180, 176, 130), (212, 208, 160), (236, 232, 190)),
                     fin=R((90, 100, 50), (130, 140, 76)), gill=(190, 110, 80), iris=R((110, 70, 14), (170, 110, 24), (222, 168, 56)), pattern="blotch", pat=(34, 40, 16)),
    "tree_frog": dict(k=0.9, back=R((50, 120, 40), (84, 170, 56), (120, 210, 84), (170, 238, 130)), belly=R((200, 220, 150), (228, 240, 180), (246, 250, 210)),
                      fin=R((110, 190, 100), (170, 230, 150)), gill=(235, 70, 50), iris=R((150, 20, 20), (210, 40, 30), (250, 100, 70)), pattern="bars", pat=(250, 214, 60)),
    "poison_dart_frog": dict(k=0.9, back=R((6, 20, 80), (16, 56, 160), (30, 96, 214), (74, 150, 250)), belly=R((130, 170, 230), (170, 206, 246), (210, 232, 255)),
                             fin=R((40, 90, 190), (90, 150, 240)), gill=(240, 200, 60), iris=R((10, 14, 40), (24, 40, 90), (60, 90, 170)), pattern="spots", pat=(6, 8, 30)),
    "glass_frog": dict(k=0.85, back=R((120, 180, 140), (156, 214, 172), (190, 238, 200), (222, 252, 230)), belly=None,
                       fin=R((170, 226, 190), (210, 246, 220)), gill=(240, 170, 170), iris=R((150, 120, 20), (210, 176, 50), (246, 224, 120)), pattern="dots", pat=(110, 170, 130), window=True),
    "goliath_frog": dict(k=1.7, back=R((50, 46, 20), (88, 80, 34), (124, 112, 52), (160, 146, 80)), belly=R((170, 160, 110), (204, 194, 140), (230, 222, 170)),
                         fin=R((120, 106, 50), (176, 152, 76)), gill=(180, 100, 70), iris=R((90, 56, 10), (150, 96, 20), (210, 156, 50)), pattern="blotch", pat=(30, 26, 12)),
    "desert_rain_frog": dict(k=0.8, back=R((150, 100, 70), (190, 140, 100), (216, 170, 128), (238, 202, 164)), belly=R((230, 196, 160), (244, 220, 188), (252, 236, 214)),
                             fin=R((210, 160, 120), (236, 196, 156)), gill=(220, 120, 110), iris=R((10, 6, 12), (24, 16, 24), (60, 44, 30)), pattern="freckle", pat=(150, 104, 70)),
    "mucus_toad": dict(k=1.0, back=R((50, 36, 22), (86, 64, 38), (122, 94, 58), (160, 128, 86)), belly=R((176, 150, 108), (206, 184, 138), (230, 212, 168)),
                       fin=R((100, 76, 46), (146, 116, 76)), gill=(190, 110, 90), iris=R((100, 66, 12), (160, 110, 22), (216, 166, 54)), pattern="warts", pat=(40, 28, 16)),
    "fire_bellied_toad": dict(k=0.9, back=R((30, 36, 16), (56, 66, 28), (88, 100, 44), (124, 138, 66)), belly=R((190, 50, 20), (230, 84, 28), (250, 140, 50)),
                              fin=R((70, 82, 40), (110, 124, 64)), gill=(240, 110, 50), iris=R((140, 60, 10), (210, 100, 18), (250, 160, 50)), pattern="blotch", pat=(12, 14, 8), belly_show=True),
    "cane_toad": dict(k=1.25, back=R((12, 10, 8), (28, 22, 16), (46, 36, 26), (70, 56, 40)), belly=R((70, 60, 46), (104, 90, 66), (140, 124, 92)),
                      fin=R((34, 28, 22), (60, 50, 38)), gill=(150, 90, 70), iris=R((90, 60, 10), (150, 104, 20), (214, 166, 50)), pattern="speck", pat=(110, 96, 70)),
    "mailtoad": dict(k=1.1, back=R((10, 20, 40), (20, 36, 70), (34, 56, 104), (56, 84, 140)), belly=R((90, 104, 130), (126, 140, 166), (164, 178, 200)),
                     fin=R((30, 44, 80), (56, 74, 120)), gill=(120, 120, 170), iris=R((90, 60, 10), (150, 104, 20), (214, 166, 50)), pattern="speck", pat=(110, 130, 180)),
    "natterjack": dict(k=0.9, back=R((30, 24, 16), (56, 46, 30), (88, 74, 48), (124, 106, 70)), belly=R((120, 104, 76), (156, 140, 104), (190, 174, 134)),
                       fin=R((70, 58, 38), (108, 92, 60)), gill=(170, 100, 70), iris=R((110, 74, 10), (176, 124, 20), (232, 188, 60)), pattern="gold", pat=(230, 190, 60)),
}

SIZES = {
    "body": (3.5, 3, 4), "eye": (1.25, 1.25, .75), "gill": (.25, 1.25, .75),
    "tail_a": (.5, 2.5, 2.5), "tail_b": (.5, 2.25, 2.5), "tail_c": (.5, 1.75, 2.5), "fin_a": (.25, 3.75, 2.5), "fin_b": (.25, 3.5, 2.5), "fin_c": (.25, 2.75, 2.5),
}


def paint(sp):
    pt = H.Painter()
    back, belly, fin, pat = sp["back"], sp["belly"], sp["fin"], sp["pat"]
    kind = sp["pattern"]

    def skin(f, x, y, w, h):
        t = 0.55 + (noise(x, y, 3) - 0.5) * 0.18
        underside = f == "down" or (f in ("north", "south", "east", "west") and y > h * 0.62)
        if underside and belly is not None:
            return pick(belly, 0.55 + (noise(x, y, 4) - 0.5) * 0.15, x, y)
        if underside and belly is None:
            return (0, 0, 0, 0) if sp.get("window") else pick(back, t, x, y)   # glass frog: see-through belly
        if f == "up":
            t += 0.14
        if kind == "blotch" and noise(x // 3, y // 3, 8) > 0.72:
            return rgb(*pat)
        if kind == "spots" and noise(x // 2, y // 2, 9) > 0.78:
            return rgb(*pat)
        if kind == "bars" and f in ("east", "west") and (x // 2) % 3 == 0:
            return rgb(*pat)
        if kind == "dots" and noise(x, y, 6) > 0.90:
            return rgb(*pat)
        if kind == "freckle" and noise(x, y, 5) > 0.92:
            return rgb(*pat)
        if kind == "warts" and noise(x // 2, y // 2, 12) > 0.80:
            return pick(back, t - 0.3, x, y)
        if kind == "speck" and noise(x, y, 19) > 0.93:
            return rgb(*pat)
        if kind == "gold" and noise(x, y, 23) > 0.9:
            return rgb(*pat)
        if kind == "gold" and f == "up" and abs(x - w // 2) <= 0:
            return rgb(*pat)
        return pick(back, t, x, y)
    pt("body", "all", skin)
    if sp.get("belly_show"):
        pass

    def fin_fn(f, x, y, w, h):
        t = 0.5 + 0.35 * (1 - y / max(1, h - 1)) * 0.5 + (noise(x, y, 14) - 0.5) * 0.2
        edge = x == 0 or y == h - 1
        return pick(fin, 0.3 if edge else t, x, y)
    for p in ("tail_a", "tail_b", "tail_c", "fin_a", "fin_b", "fin_c"):
        pt(p, "all", lambda f, x, y, w, h: pick(back, 0.5 + (noise(x, y, 31) - 0.5) * 0.2, x, y) if p.startswith("tail") else fin_fn(f, x, y, w, h))
    for p in ("fin_a", "fin_b", "fin_c"):
        pt(p, "all", fin_fn)
    gc = rgb(*sp["gill"])
    pt("gill", "all", lambda f, x, y, w, h: gc if (y + x) % 2 == 0 else rgb(max(0, gc[0] - 50), max(0, gc[1] - 40), max(0, gc[2] - 30)))

    # eyes: the big shiny eye on the front of each eye cube
    ex0, ey0, ew, eh = H.RECT[("eye", "north")]
    pt("eye", "all", lambda f, x, y, w, h: pick(sp["iris"], 0.3, x, y) if f != "north" else rgb(24, 18, 20))
    for yy in range(eh):
        for xx in range(ew):
            pt.T[ey0 + yy][ex0 + xx] = rgb(18, 14, 16)
    cx, cy = ew / 2, eh / 2
    C.paint_eye(pt.T, ex0 + cx, ey0 + cy, ew / 2 - 0.2, eh / 2 - 0.2, sp["iris"], rgb(18, 14, 16), glint=False)
    pt.T[ey0 + 1][ex0 + 1] = rgb(255, 255, 255)
    # a tiny smile and blush on the body's front
    bx0, by0, bw, bh = H.RECT[("body", "north")]
    my = int(bh * 0.72)
    if bw >= 8:
        for dx, dy in ((-1, 0), (0, 1), (1, 0)):
            pt.T[by0 + my + dy][bx0 + bw // 2 + dx] = rgb(110, 50, 50)
        for sx in (bw // 2 - 4, bw // 2 + 4):
            pt.T[by0 + my - 1][bx0 + sx] = rgb(238, 130, 130)
    return pt.T


def build(sp, name):
    m = H.Model(name)
    S = H.PARTS
    sz = lambda p: S[p]["size"]
    bw, bh, bd = sz("body")
    ew, eh, ed = sz("eye")
    y0 = 0.25
    m.bone("root", None, [0, 0, 0])
    m.bone("body", "root", [0, y0 + bh / 2, 0], [cube("body", [-bw / 2, y0, -bd / 2])])
    m.bone("eye_r", "body", [-bw / 2 + ew * 0.6, y0 + bh, -bd / 2 + 0.5],
           [cube("eye", [-bw / 2 + 0.1, y0 + bh - 0.45, -bd / 2 - ed * 0.6])], mirror=True)
    gw, gh, gd = sz("gill")
    m.bone("gill_r", "body", [-bw / 2, y0 + bh * 0.6, -bd / 2 + 1.0],
           [cube("gill", [-bw / 2 - gw, y0 + bh * 0.35, -bd / 2 + 0.75]), cube("gill", [-bw / 2 - gw, y0 + bh * 0.62, -bd / 2 + 0.5])], mirror=True)
    z = bd / 2 - 0.25
    ta, tb, tc = sz("tail_a"), sz("tail_b"), sz("tail_c")
    fa, fb, fc = sz("fin_a"), sz("fin_b"), sz("fin_c")
    cy = y0 + bh / 2
    m.bone("tail_a", "body", [0, cy, z], [cube("tail_a", [-ta[0] / 2, cy - ta[1] / 2, z]), cube("fin_a", [-fa[0] / 2, cy - fa[1] / 2, z])])
    z2 = z + ta[2] - 0.1
    m.bone("tail_b", "tail_a", [0, cy, z2], [cube("tail_b", [-tb[0] / 2, cy - tb[1] / 2, z2]), cube("fin_b", [-fb[0] / 2, cy - fb[1] / 2, z2])])
    z3 = z2 + tb[2] - 0.1
    m.bone("tail_c", "tail_b", [0, cy, z3], [cube("tail_c", [-tc[0] / 2, cy - tc[1] / 2, z3]), cube("fin_c", [-fc[0] / 2, cy - fc[1] / 2, z3])])
    if sp.get("window"):
        pass
    return m, y0 + bh + eh * 0.4, z3 + tc[2] + bd / 2


def animations(an):
    def wig(amp, period, lag):
        return track((0, (0, amp, 0)), (period / 2, (0, -amp, 0)), (period, (0, amp, 0)))
    an.add("idle", 3.0, True, {
        "body": {"position": track((0, (0, 0, 0)), (1.5, (0, 0.2, 0)), (3, (0, 0, 0)))},
        "tail_a": {"rotation": track((0, (0, 8, 0)), (1.5, (0, -8, 0)), (3, (0, 8, 0)))},
        "tail_b": {"rotation": track((0, (0, 12, 0)), (1.5, (0, -12, 0)), (3, (0, 12, 0)))},
        "tail_c": {"rotation": track((0, (0, 16, 0)), (1.5, (0, -16, 0)), (3, (0, 16, 0)))},
        "gill_r": {"rotation": track((0, (0, 0, 6)), (1.5, (0, 0, -6)), (3, (0, 0, 6)))},
        "gill_l": {"rotation": track((0, (0, 0, -6)), (1.5, (0, 0, 6)), (3, (0, 0, -6)))},
    })
    an.add("swim", 0.6, True, {
        "body": {"rotation": track((0, (0, 6, 0)), (0.3, (0, -6, 0)), (0.6, (0, 6, 0)))},
        "tail_a": {"rotation": track((0, (0, 22, 0)), (0.3, (0, -22, 0)), (0.6, (0, 22, 0)))},
        "tail_b": {"rotation": track((0, (0, -28, 0)), (0.3, (0, 28, 0)), (0.6, (0, -28, 0)))},
        "tail_c": {"rotation": track((0, (0, 34, 0)), (0.3, (0, -34, 0)), (0.6, (0, 34, 0)))},
        "gill_r": {"rotation": track((0, (0, 0, 14)), (0.3, (0, 0, -4)), (0.6, (0, 0, 14)))},
        "gill_l": {"rotation": track((0, (0, 0, -14)), (0.3, (0, 0, 4)), (0.6, (0, 0, -14)))},
    })


def main():
    entries = []
    for species, sp in SPECIES.items():
        name = species + "_tadpole"
        C.declare(SIZES, sp["k"])
        tex = paint(sp)
        model, height, length = build(sp, name)
        geo, adoc = C.write_model(name, tex, model, animations)
        entries.append((geo, tex, adoc["animations"][f"animation.{name}.swim"], 0.15, max(height, 5)))
    if "--preview" in sys.argv:
        C.lineup(sys.argv[sys.argv.index("--preview") + 1], entries, cols=4, cell=300, views=((-40, 14), (90, 10)), pixels=26)


if __name__ == "__main__":
    main()
