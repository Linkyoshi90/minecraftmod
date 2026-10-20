#!/usr/bin/env python3
"""Lamia (the serpent family, see lamia_field_guide.html): python, king cobra, sea snake, rattlesnake, boa.

    python tools/gen_lamia.py [species ...] [--previews]

Shared girl_lib upper body on a serpent tail of eight segments (seg_1 down from the waist, seg_2..seg_8 along the
ground). Species extras: the cobra's hood (bone `hood`), the rattlesnake's rattle, the sea snake's paddle tail.
Jewellery top and arm bands; slit pupils. Five colours per species.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, shades  # noqa: E402

SEGS = [("seg_1", 4.5, 10.5, 4), ("seg_2", 4.25, 4, 5.5), ("seg_3", 4, 3.75, 5.5), ("seg_4", 3.5, 3.25, 5),
        ("seg_5", 3, 2.75, 5), ("seg_6", 2.5, 2.25, 4.5), ("seg_7", 2, 1.75, 4), ("seg_8", 1.25, 1.25, 3.5)]

SPECIES = {
    "python_lamia": dict(
        build="curvy", outfit=dict(top=["halter"], bottom="sarong", extras=["armlets"]),
        thick=1.15, hair="wavy",
        pal=dict(SKIN=skin("tan"), HAIR="#3a2a1e", FUR="#5a3e24", LIGHT="#ece0c4", MARK="#c8a060", EYE="#c89030", CLOTH="#2e5a3a", TRIM="#d8b048",
                 pattern="blotches", facemark="scales"),
        variants={"banana": dict(FUR="#a890b0", MARK="#f0d050", HAIR="#e0c058", CLOTH="#5a3a6a"),
                  "piebald": dict(pattern="patches", MARK="#f4f2ee", HAIR="#2a1e16"),
                  "albino": dict(FUR="#f4ecd8", MARK="#f0d060", EYE="#d83a3a", HAIR="#f2ead8", PUPIL="#7a1010", CLOTH="#c86a6a"),
                  "reticulated": dict(FUR="#8a7a4a", MARK="#1e1a12", pattern="net", HAIR="#4a3a22")}),
    "cobra_lamia": dict(
        build="slim", outfit=dict(top=["bandeau"], bottom="loincloth", extras=["usekh", "armlets"], colors=dict(cloth2="#f0ead8")),
        thick=1.0, hair="bob", hood=True,
        pal=dict(SKIN=skin("olive"), HAIR="#2a2a1c", FUR="#6a6438", LIGHT="#efe8c8", MARK="#d8d0a0", EYE="#c8a030", CLOTH="#6a2a2a", TRIM="#e0c050",
                 pattern="bands", facemark="scales"),
        variants={"black": dict(FUR="#1e1e20", MARK="#3a3a3c", LIGHT="#d8d0b8", HAIR="#141416", CLOTH="#3a3a5a"),
                  "banded": dict(FUR="#18181a", MARK="#f0d040", pattern="bars", HAIR="#1a1a1c"),
                  "golden": dict(FUR="#b08848", MARK="#5a4020", HAIR="#6a4a22", CLOTH="#2a4a6a"),
                  "spectacled": dict(FUR="#b09878", MARK="#3a2a1c", HAIR="#4a3a2a", CLOTH="#4a6a3a")}),
    "sea_snake_lamia": dict(
        build="petite", outfit=dict(top=["shells"], extras=["pearls", "belt"], colors=dict(shell="#cfe4ec")),
        thick=0.9, hair="flowing", paddle=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e2a3a", FUR="#b0c4d4", LIGHT="#e4ecf0", MARK="#121418", EYE="#d8c040", CLOTH="#2a6a8a", TRIM="#e8e0c8",
                 pattern="bars", facemark="scales"),
        variants={"yellow": dict(FUR="#1a1a1e", LIGHT="#f0d040", MARK="#f0d040", pattern="speckle", HAIR="#1a1a22", CLOTH="#c8a020"),
                  "olive": dict(FUR="#7a7448", MARK="#4a4430", pattern="smallspots", HAIR="#3a3a22"),
                  "turtlehead": dict(FUR="#4a4a50", MARK="#2a2a2e", HAIR="#2a2a30", CLOTH="#3a3a4a"),
                  "pale": dict(FUR="#d8d8d4", MARK="#5a5a58", pattern_scale=0.6, HAIR="#8a8a90")}),
    "rattlesnake_lamia": dict(
        build="athletic", outfit=dict(top=["bandeau", "vest"], extras=["belt", "pouch", "bracers"]),
        thick=0.95, hair="braid", rattle=True,
        pal=dict(SKIN=skin("peach"), HAIR="#6a5040", FUR="#a08a68", LIGHT="#e8dcc4", MARK="#5a4430", EYE="#c8a038", CLOTH="#8a4a2a", TRIM="#d8c090",
                 pattern="diamonds", facemark="scales"),
        variants={"timber": dict(FUR="#c8b080", MARK="#3a2a1e", pattern="bands", HAIR="#3a2a1e"),
                  "sidewinder": dict(FUR="#e2d2a8", MARK="#a08060", pattern="speckle", HAIR="#c8b088", CLOTH="#a06a3a"),
                  "mojave": dict(FUR="#8a9060", MARK="#3a3a2a", HAIR="#5a5a3a", CLOTH="#4a5a3a"),
                  "prairie": dict(FUR="#c0a878", MARK="#6a5038", pattern="blotches", HAIR="#7a5a40")}),
    "boa_lamia": dict(
        build="strong", outfit=dict(top=["sports", "harness"], bottom="wrap_skirt", extras=["pauldron"]),
        thick=1.05, hair="ponytail", redtail=True,
        pal=dict(SKIN=skin("fair"), HAIR="#4a2a20", FUR="#d0b898", LIGHT="#f0e8d8", MARK="#6a3a2a", EYE="#c89040", CLOTH="#2a4a2a", TRIM="#d8a850",
                 pattern="blotches", facemark="scales", extra=dict(tail="#a8302a")),
        variants={"rainbow": dict(FUR="#b0683a", MARK="#3a1a12", pattern="rosettes", HAIR="#7a3a22", extra=dict(tail="#c06030")),
                  "emerald": dict(FUR="#2a9a4a", MARK="#f0f8f0", LIGHT="#e8f0a0", pattern="speckle", HAIR="#1e5a2a", EYE="#d8c040", extra=dict(tail="#2a8a42")),
                  "sand": dict(FUR="#d8c08a", MARK="#6a4a2a", pattern="spots", HAIR="#8a6a40", extra=dict(tail="#b89060")),
                  "rosy": dict(FUR="#c8c0bc", MARK="#c08070", pattern="finestripes", HAIR="#8a5a5a", extra=dict(tail="#c08070"))}),
}


def declare(sp):
    d = SPECIES[sp]
    k = d["thick"]
    parts = [(n, round(w * k * 4) / 4, round(h * k * 4) / 4 if i else h, dd) for i, (n, w, h, dd) in enumerate(SEGS)]
    extra = []
    if d.get("hood"):
        extra.append(("hood_p", 5.5, 6.5, .5))
    if d.get("rattle"):
        extra.append(("rattle", 1.25, 1, .75))
    if d.get("paddle"):
        extra.append(("paddle", .5, 3.5, 4))
    G.declare(parts + extra, ears=None, hair=SPECIES[sp]["hair"],
              build=SPECIES[sp].get("build"), outfit=SPECIES[sp].get("outfit"))


def paint(sp, key):
    d = SPECIES[sp]
    pal = G.pal_from(d["pal"], d["variants"].get(key) if key else None, pupil=(0.9, 3.6))
    pt = H.Painter()
    G.paint_upper(pt, pal, top="wrap", top_pattern="dots", outfit=d.get("outfit"), build=d.get("build"))
    sc = G.scale_fn(pal, belly_faces=("down",))
    belly = G.scale_fn(pal, belly_faces=("down", "north"))
    skin_t = H.skin_fn(pal, 0.66)

    def hips(f, x, y, w, h):                       # the scales begin at the hips
        return skin_t(f, x, y, w, h) if y < h * 0.35 else sc(f, x, y, w, h)
    pt("hips", "all", hips)
    pt("seg_1", "all", lambda f, x, y, w, h: skin_t(f, x, y, w, h) if y < 2 and f != "down" else belly(f, x, y, w, h))
    tail_col = pal.extra.get("tail")
    for i in range(2, 9):
        if tail_col and i >= 6:
            red = G.palette(FUR=tail_col, LIGHT=pal.LIGHT, MARK=pal.MARK)
            red.pattern = pal.pattern
            red.pattern_scale = pal.pattern_scale
            pt(f"seg_{i}", "all", G.scale_fn(red))
        else:
            pt(f"seg_{i}", "all", sc)
    pt("hood_p", "all", lambda f, x, y, w, h: pick(pal.LIGHT, 0.55, x, y) if f == "north" else sc(f, x, y, w, h))
    pt("rattle", "all", lambda f, x, y, w, h: pick(shades("#c8b896"), 0.55 - (0.25 if y == h - 1 else 0), x, y))
    pt("paddle", "all", lambda f, x, y, w, h: pick(pal.FUR, 0.5, x, y) if (y // 3) % 2 else pick(pal.MARK, 0.4, x, y))
    return pt.T


def build(sp):
    d = SPECIES[sp]
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), build=d.get("build"), outfit=d.get("outfit"), ears=None, skirt=False))
    end_z = G.serpent(r, None, d)
    if d.get("hood"):
        hp = G.S("hood_p")
        r.add("hood", "body", [0, 19, 1.4], [G.rot_cube("hood_p", [-hp[0], 16.5, 1.4], [0, 19, 1.4], 0, 0, 22),
                                              G.rot_cube("hood_p", [0, 16.5, 1.4], [0, 19, 1.4], 0, 0, -22)])
    if d.get("rattle"):
        s8 = G.S("seg_8")
        for i in range(3):
            r.put("seg_8", cube("rattle", [-0.625, 0.1, end_z - 0.25 + i * 0.7]))
    if d.get("paddle"):
        r.put("seg_8", cube("paddle", [-0.25, 0, end_z - 1.5]))
    return r.model(sp)


SEG_NAMES = [f"seg_{i}" for i in range(1, 9)]


def anims(an):
    G.serpent_anims(an, SEG_NAMES)


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.3, -45, 20), ("rear", 0.9, -30, 8), ("sit", 0, -40, 25), ("bask", 0, -40, 15)]
FACE = [("idle", 0, 0, 0, 24), ("idle", 2.75, 0, 0, 24), ("hiss", 0.5, 0, 0, 24), ("idle", 0, -30, 8, 24)]

if __name__ == "__main__":
    G.run_family("lamia", SPECIES, declare, paint, build, anims, VIEWS, FACE)
