#!/usr/bin/env python3
"""Merfolk (the sea family, see merfolk_field_guide.html): orca, koi, shark, seal, axolotl, dolphin.

    python tools/gen_merfolk.py [species ...] [--previews]

Shared girl_lib upper body on a fish / sea-mammal tail: fin_1 down from the waist, fin_2..fin_4 along the ground and a
fluke (horizontal for the orca, seal and axolotl, a vertical tail fin for the koi and shark). Extras: a dorsal fin
(orca, shark, dolphin, axolotl crest), long trailing fins (koi), fin ears (koi, shark, orca, dolphin), gill fronds
(axolotl). Every model has a `seashells` bone (two scallop shells over the bust) that the game shows once she has found
shells on the sea floor. Five colours per species.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, shades, CLEAR  # noqa: E402

SPECIES = {
    "orca_mermaid": dict(
        build="strong", outfit=dict(top=["sports"], extras=["armlets"]),
        ears="fin", hair="ponytail", fluke=(9, .75, 4), dorsal=(.75, 6, 3.5), size=1.15,
        pal=dict(SKIN=skin("fair"), HAIR="#141418", FUR="#141418", LIGHT="#f4f4f2", MARK="#8a8c90", EYE="#2a3a5a", CLOTH="#e8e8e8", TRIM="#4a6a8a",
                 pattern="solid", extra=dict(belly_white=True)),
        variants={"transient": dict(MARK="#5a5c60", HAIR="#1a1a1e"), "typeb": dict(FUR="#5a5e64", LIGHT="#ece4d0", HAIR="#3a3e44"),
                  "white": dict(FUR="#f2f2f0", HAIR="#f8f8f8", MARK="#c8c8cc", CLOTH="#4a6a8a", TRIM="#e8e8e8"),
                  "typed": dict(FUR="#1e1e24", HAIR="#2a2a30", MARK="#3a3a40")}),
    "koi_mermaid": dict(
        build="slim", outfit=dict(top=["kimono"], colors=dict(kimono="#f4f0ea", kimono_trim="#d8402a", obi="#d8402a")),
        ears="fin", hair="flowing", fluke=(.5, 6, 5), long_fins=True,
        pal=dict(SKIN=skin("pale"), HAIR="#f4f2ee", FUR="#f6f4f0", LIGHT="#ffffff", MARK="#d8402a", EYE="#c83a2a", CLOTH="#d8402a", TRIM="#f0d070",
                 pattern="patches"),
        variants={"showa": dict(FUR="#1a1a1c", MARK="#d8402a", HAIR="#1a1a1c", pattern="patches"),
                  "ogon": dict(FUR="#e8c050", MARK="#f8e090", HAIR="#f0d070", pattern="speckle", CLOTH="#c89a30"),
                  "asagi": dict(FUR="#6a88a8", MARK="#e0603a", HAIR="#8aa4c0", pattern="net", CLOTH="#3a5a8a"),
                  "tancho": dict(pattern="solid", HAIR="#f8f6f2", extra=dict(spot=True))}),
    "shark_mermaid": dict(
        build="athletic", outfit=dict(top=["bandeau", "harness"], extras=["pauldron", "belt"]),
        ears="fin", hair="shaggy", fluke=(.75, 7, 5), dorsal=(.75, 5, 4), size=1.1,
        pal=dict(SKIN=skin("grey"), HAIR="#5a6470", FUR="#7a8490", LIGHT="#f0f0ee", MARK="#3a424c", EYE="#2a2a30", CLOTH="#2a3a4a", TRIM="#e8e0c8",
                 pattern="solid", extra=dict(belly_white=True, gills=True)),
        variants={"tiger": dict(FUR="#8a7a62", MARK="#3a3024", pattern="stripes", HAIR="#6a5a46"),
                  "whale": dict(FUR="#2a3a5a", MARK="#f0f0ea", pattern="spots", HAIR="#3a4a6a", CLOTH="#c8b880"),
                  "mako": dict(FUR="#2a4a8a", HAIR="#3a5a9a", MARK="#1a2a5a"),
                  "lemon": dict(FUR="#b8a868", HAIR="#c8b878", MARK="#8a7a48", CLOTH="#3a5a3a")}),
    "seal_mermaid": dict(
        build="curvy", outfit=dict(top=["bandeau"], extras=["pearls"]),
        ears=None, hair="bob", fluke=(7, .75, 3.5), seal=True,
        pal=dict(SKIN=skin("fair"), HAIR="#6a6a6e", FUR="#8a8a8e", LIGHT="#c8c8c8", MARK="#3a3a3e", EYE="#1e1a18", CLOTH="#e8e4dc", TRIM="#7a9ab0",
                 pattern="smallspots"),
        variants={"grey": dict(FUR="#5a5a5e", MARK="#2a2a2e", pattern="blotches", HAIR="#4a4a4e"),
                  "harp": dict(FUR="#e2e4e6", MARK="#2a2a2e", pattern="cross", HAIR="#f0f0f2", LIGHT="#ffffff"),
                  "leopard": dict(FUR="#6a6e74", MARK="#2a2c30", pattern="spots", HAIR="#5a5e64", CLOTH="#8a3a3a"),
                  "ringed": dict(FUR="#4a4a50", MARK="#a8a8ae", pattern="rosettes", HAIR="#3a3a40")}),
    "axolotl_mermaid": dict(
        build="petite", outfit=dict(top=["shells"], extras=["pearls"]),
        ears="gill", hair="odango", fluke=(4.5, .5, 3), dorsal=(.5, 2.5, 9), axolotl=True,
        pal=dict(SKIN=skin("pale"), HAIR="#f4c4cc", FUR="#f4c4cc", LIGHT="#fadce2", MARK="#d8586a", EYE="#1a1a1e", CLOTH="#e86a8a", TRIM="#f8e0e8",
                 pattern="solid", INNER="#d8405a"),
        variants={"wild": dict(FUR="#6a5a3a", HAIR="#7a6a48", LIGHT="#8a7a58", MARK="#3a2e1e", pattern="speckle", INNER="#8a4a4a", CLOTH="#4a6a3a"),
                  "gold": dict(FUR="#f0c858", HAIR="#f4d070", LIGHT="#f8e4a0", INNER="#e8a040", CLOTH="#c87a30"),
                  "cyan": dict(FUR="#c8eef4", HAIR="#d8f4f8", LIGHT="#eafafc", INNER="#e86a8a", CLOTH="#5ab0c8"),
                  "blue": dict(FUR="#4a5ac8", HAIR="#5a6ad8", LIGHT="#7a88e0", MARK="#2a3a8a", INNER="#8a5ad8", CLOTH="#3a3a8a")}),
    "dolphin_mermaid": dict(
        build="slim", outfit=dict(top=["halter"], extras=["armlets"]),
        ears="fin", hair="twintails", bangs="swept", fluke=(8, .75, 3.5), dorsal=(.75, 4.5, 3.5), size=1.05,
        pal=dict(SKIN=skin("fair"), HAIR="#6a7e92", FUR="#7a90a6", LIGHT="#e8eef2", MARK="#4a5c70", EYE="#2a4a6a", CLOTH="#2aa0a8", TRIM="#f0e8d0",
                 pattern="solid", extra=dict(belly_white=True)),
        variants={"common": dict(FUR="#2e3a4a", MARK="#e8d8a0", LIGHT="#f2ecd8", HAIR="#3a4656", pattern="patches", CLOTH="#e8b040"),
                  "spinner": dict(FUR="#5a6676", MARK="#9aa4b0", HAIR="#6a7686", CLOTH="#8a4ab0"),
                  "rissos": dict(FUR="#a8acb0", MARK="#f0f0ee", HAIR="#c8ccd0", pattern="speckle", CLOTH="#3a5a8a"),
                  "pink": dict(FUR="#e8a8b4", HAIR="#f0bcc6", LIGHT="#f8dce2", MARK="#d08898", CLOTH="#3a8a7a")}),
}


def declare(sp):
    d = SPECIES[sp]
    k = d.get("size", 1.0)
    q = lambda v: round(v * k * 4) / 4
    parts = [("fin_1", q(4.5), 10.5, q(3.75)), ("fin_2", q(4.25), q(3.75), 5), ("fin_3", q(3.5), q(3.25), 5), ("fin_4", q(2.5), q(2.5), 4.5),
             ("fluke",) + d["fluke"]]
    if d.get("dorsal"):
        parts.append(("dorsal",) + d["dorsal"])
    if d.get("long_fins"):
        parts.append(("ribbon", .25, 6, 3))
    if d.get("seal"):
        parts.append(("flipper", 3, .5, 3.5))
    parts.append(("shell", 2, 1.75, .5))
    G.declare(parts, ears=d["ears"], hair=SPECIES[sp]["hair"],
              build=SPECIES[sp].get("build"), outfit=SPECIES[sp].get("outfit"))


def paint(sp, key):
    d = SPECIES[sp]
    pal = G.pal_from(d["pal"], d["variants"].get(key) if key else None)
    pt = H.Painter()
    G.paint_upper(pt, pal, top="skin", outfit=d.get("outfit"), build=d.get("build"))
    shell = shades(pal.extra.get("shell", "#f0d8c8"))
    # a shell top: two scallop shells over the bust, a pearl string as the cord
    if not d.get("outfit"):
        pt("bust", "all", lambda f, x, y, w, h: pick(pal.CLOTH, 0.55 + 0.25 * (x % 3 == 0) - 0.2 * (y == h - 1), x, y))
    pt("cord", "all", lambda f, x, y, w, h: pick(pal.TRIM, 0.85 if x % 2 else 0.6, x, y))
    pt("shell", "all", lambda f, x, y, w, h: pick(shell, 0.55 + 0.3 * (x % 3 == 1), x, y))
    sc = G.scale_fn(pal, belly_faces=("down", "north") if pal.extra.get("belly_white") else ("down",))
    smooth = G.fur_fn(pal, light_faces=("down", "north") if pal.extra.get("belly_white") else ("down",), grain=0.06, seed=41)
    body = sc if sp.startswith("koi") else smooth                 # the koi has scales; the mammals and the shark are smooth
    skin_t = H.skin_fn(pal, 0.66)
    pt("hips", "all", lambda f, x, y, w, h: skin_t(f, x, y, w, h) if y < h * 0.4 else body(f, x, y, w, h))
    for p in ("fin_1", "fin_2", "fin_3", "fin_4"):
        pt(p, "all", body)

    def fin(f, x, y, w, h):
        t = 0.5 + 0.2 * (x % 3 == 0) + (noise(x, y, 6) - 0.5) * 0.1
        col = pick(pal.MARK if sp.startswith("koi") else pal.FUR, t, x, y)
        if pal.extra.get("belly_white") is None and sp.startswith("axolotl"):
            col = pick(pal.LIGHT, t, x, y)
        return col
    pt("fluke", "all", fin)
    pt("dorsal", "all", fin)
    pt("ribbon", "all", lambda f, x, y, w, h: G.with_alpha(pick(pal.MARK, 0.55 + 0.25 * (y % 3 == 0), x, y), 220) if y < h - 1 or x % 2 else G.CLEAR)
    pt("flipper", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.35 + 0.2 * (x % 4 == 0), x, y))
    if pal.extra.get("gills"):
        pt("fin_1", ("east", "west"), lambda f, x, y, w, h: pick(pal.MARK, 0.25, x, y) if (y in (1, 3, 5) and 2 <= x < w - 2) else None)
    if pal.extra.get("spot"):                                     # tancho: one red spot on the head
        pt("hair_cap", ("up",), lambda f, x, y, w, h: pick(shades("#d8402a"), 0.55, x, y) if (x - w / 2) ** 2 + (y - h / 2) ** 2 < 30 else None)
    if d["ears"] == "gill":
        pt("ear_a", "all", lambda f, x, y, w, h: pick(pal.INNER, 0.55 + 0.25 * (y % 2 == 0), x, y))
    elif d["ears"] == "fin":
        pt("ear_a", "all", fin)
    return pt.T


def build(sp):
    d = SPECIES[sp]
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), build=d.get("build"), outfit=d.get("outfit"), ears=d["ears"], skirt=False, cuffs=False))
    if not d.get("outfit"):
        sh = cube("shell", [-2.25, 16.8, -2.6])
        r.put("body", sh, G.mirror_cube(sh))
    # seashells she finds on the sea floor (hidden until the game says she wears them)
    bw, bh, bd = G.S("bust")
    cd = G.S("chest")[2]
    sw, sh_, sdp = G.S("shell")
    fz = -cd / 2 - bd
    left = cube("shell", [-0.25 - bw + (bw - sw) / 2, 16.9 - 0.15, fz - sdp + 0.1], inflate=0.1)
    r.add("seashells", "body", [0, 17.5, fz], [left, G.mirror_cube(left), cube("cord", [-1.5, 18.6, fz - 0.15])])
    G.fish(r, None, d)
    if d.get("dorsal"):
        dw, dh, dd = G.S("dorsal")
        if d.get("axolotl"):                                       # a low frilly crest along the tail
            r.put("fin_2", cube("dorsal", [-dw / 2, G.S("fin_2")[1] - 0.25, 1.5]))
        else:
            r.put("fin_1", G.rot_cube("dorsal", [-dw / 2, 7.5, 1.0], [0, 7.5, 1.6], -35, 0, 0))
    if d.get("long_fins"):
        for sg in (-1, 1):
            c = G.rot_cube("ribbon", [sg * 2.4 - 0.125, 2.0, 2.0], [sg * 2.4, 6, 2.5], 20, 0, -18 * sg)
            r.put("fin_2", c)
    if d.get("seal"):
        f2 = G.S("fin_2")
        for sg in (-1, 1):
            r.put("fin_1", G.rot_cube("flipper", [sg * 2.6 - 1.5, 7.0, -2.2], [sg * 2.6, 7.25, -1.0], 0, 0, -30 * sg))
    return r.model(sp)


def anims(an):
    G.fish_anims(an)


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.4, -45, 15), ("swim", 0.3, -70, 15), ("sit", 0, -40, 25), ("sing", 0.5, -30, 8)]
FACE = [("idle", 0, 0, 0, 24), ("idle", 2.75, 0, 0, 24), ("sing", 0.5, 0, 0, 24), ("idle", 0, -30, 8, 24)]

if __name__ == "__main__":
    G.run_family("merfolk", SPECIES, declare, paint, build, anims, VIEWS, FACE)
