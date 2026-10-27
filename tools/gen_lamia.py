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
from girl_lib import H, cube, pick, noise, skin, shades, CLEAR  # noqa: E402

SEGS = [("seg_1", 4.5, 10.5, 4), ("seg_2", 4.25, 4, 5.5), ("seg_3", 4, 3.75, 5.5), ("seg_4", 3.5, 3.25, 5),
        ("seg_5", 3, 2.75, 5), ("seg_6", 2.5, 2.25, 4.5), ("seg_7", 2, 1.75, 4), ("seg_8", 1.25, 1.25, 3.5)]

SPECIES = {
    # twelfth wave: the tsuchinoko, a short fat snake body (thick, stubby) that curls up and rolls ("roll")
    "tsuchinoko_lamia": dict(
        build="petite", outfit=dict(top=["sweater"], extras=["scarf"], colors=dict(scarf="#c83a3a")),
        thick=1.55, stub=0.55, hair="bob", bangs="blunt",
        pal=dict(SKIN=skin("fair"), HAIR="#5a3e24", FUR="#7a5a34", LIGHT="#e8d4a8", MARK="#4a3018", EYE="#c8902a", CLOTH="#e8dcc0", TRIM="#c83a3a",
                 pattern="diamonds", facemark=None),
        variants={"grey": dict(HAIR="#5a5a5e", FUR="#7a7a7e", LIGHT="#d8d8d8", MARK="#3a3a3e", CLOTH="#c8d8e8"),
                  "green": dict(HAIR="#3a5a2a", FUR="#5a7a3a", LIGHT="#d8e8a8", MARK="#2a4a1a", CLOTH="#f0e0b0"),
                  "gold": dict(HAIR="#a8782a", FUR="#d8a840", LIGHT="#f8e8b0", MARK="#8a5a1a", CLOTH="#3a2a4a"),
                  "black": dict(HAIR="#1e1c1e", FUR="#2a282a", LIGHT="#8a8488", MARK="#121012", EYE="#e8c040", CLOTH="#a83a3a")}),
    "python_lamia": dict(
        build="curvy", outfit=dict(top=["halter"], bottom="sarong", extras=["armlets"]),
        thick=1.15, hair="wavy",
        pal=dict(SKIN=skin("tan"), HAIR="#3a2a1e", FUR="#5a3e24", LIGHT="#ece0c4", MARK="#c8a060", EYE="#c89030", CLOTH="#2e5a3a", TRIM="#d8b048",
                 pattern="blotches", facemark="scales"),
        variants={"banana": dict(FUR="#a890b0", MARK="#f0d050", HAIR="#e0c058", CLOTH="#5a3a6a"),
                  "piebald": dict(pattern="patches", MARK="#f4f2ee", HAIR="#2a1e16"),
                  "albino": dict(FUR="#f4ecd8", MARK="#f0d060", EYE="#d83a3a", HAIR="#f2ead8", PUPIL="#7a1010", CLOTH="#c86a6a"),
                  "reticulated": dict(FUR="#8a7a4a", MARK="#1e1a12", pattern="net", HAIR="#4a3a22")}),
    "medusa_lamia": dict(                               # ninth wave: living snakes for hair (bone snakes), a blindfold (shown once befriended)
        build="slim", outfit=dict(top=["halter"], bottom="sarong", extras=["armlets", "pearls"], colors=dict(cloth2="#f0ece0")),
        thick=1.05, hair="bob", bangs="none", medusa=True,
        pal=dict(SKIN=skin("pale"), HAIR="#3a7a3a", FUR="#4a7a4a", LIGHT="#e0ecc8", MARK="#2a4a2a", EYE="#e8d040", CLOTH="#f4f0e6", TRIM="#d8b048",
                 pattern="bands", facemark="scales", extra=dict(snake="#4a9a3a", snake_band="#2a5a24", blindfold="#5a2a6a")),
        variants={"bronze": dict(HAIR="#8a5a2a", FUR="#8a6a3a", MARK="#5a3a1a", EYE="#4ab8a8",
                                 extra=dict(snake="#b0783a", snake_band="#6a4018", blindfold="#2a3a5a")),
                  "onyx": dict(HAIR="#1e1e22", FUR="#2a2a30", MARK="#141418", LIGHT="#8a8a90", EYE="#c83a3a",
                               extra=dict(snake="#2a2a30", snake_band="#5a5a64", blindfold="#a8a8b0")),
                  "marble": dict(SKIN="#e8e4dc", HAIR="#d8d4cc", FUR="#c8c4bc", MARK="#9a968e", LIGHT="#f8f6f2", EYE="#8a8a8e",
                                 extra=dict(snake="#d8d4cc", snake_band="#9a968e", blindfold="#3a3a40")),
                  "coral": dict(HAIR="#c83a2a", FUR="#c84a3a", MARK="#1e1a18", LIGHT="#f8e0c0", EYE="#f8d040",
                                extra=dict(snake="#d8402a", snake_band="#1e1a18", snake_band2="#f0d040", blindfold="#1e1a18"))}),
    # fifteenth wave: the caraviper, a blue cobra with a wide hood, banded and patterned with gold glyphs
    "caraviper_lamia": dict(
        build="slim", outfit=dict(top=["bandeau"], bottom="loincloth", extras=["usekh", "armlets"], colors=dict(cloth2="#e8c050")),
        thick=1.0, hair="bob", hood=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e2a6a", FUR="#2a4ab0", LIGHT="#e8dcb0", MARK="#e8b840", EYE="#f8c030", CLOTH="#1e2a6a", TRIM="#e8c050",
                 pattern="bands", facemark="scales"),
        variants={"sand": dict(FUR="#c8a46a", MARK="#5a3a1a", LIGHT="#f4e8c8", HAIR="#6a4a22", CLOTH="#2a5a7a"),
                  "black": dict(FUR="#1e1e22", MARK="#e8b840", LIGHT="#d8d0b8", HAIR="#141416", CLOTH="#3a2a5a"),
                  "red": dict(FUR="#a82a2a", MARK="#f0d040", LIGHT="#f4d8c0", HAIR="#5a1410", CLOTH="#1e1a1a"),
                  "green": dict(FUR="#2a7a4a", MARK="#e8c050", LIGHT="#d8f0c8", HAIR="#14402a", CLOTH="#6a2a2a")}),
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
    "naga_lamia": dict(
        build="athletic", outfit=dict(top=["bandeau", "capelet"], bottom="loincloth", extras=["usekh", "armlets", "bracers"], colors=dict(cloth2="#e8c050")),
        thick=1.15, hair="flowing", hood=True, crown=True,
        pal=dict(SKIN=skin("tan"), HAIR="#1e1a14", FUR="#5a5030", LIGHT="#e8dcb0", MARK="#d8c890", EYE="#e8b020", CLOTH="#2a5a3a", TRIM="#e8c050",
                 pattern="bars", facemark="scales"),
        variants={"black": dict(FUR="#1a1a1c", MARK="#4a4a4c", LIGHT="#c8c0a8", HAIR="#101012", CLOTH="#3a2a4a"),
                  "golden": dict(FUR="#c8a050", MARK="#f0e0a0", HAIR="#5a3a14", CLOTH="#6a1e1e"),
                  "jade": dict(FUR="#2a6a3a", MARK="#a8d888", HAIR="#14301e", CLOTH="#e8e0c8"),
                  "crimson": dict(FUR="#7a1e1e", MARK="#d8a060", HAIR="#2a0e0e", CLOTH="#1e2a4a")}),
    "wadjet_lamia": dict(                               # on two legs (girl_family): built by legged_* below
        biped=True, build="athletic", outfit=dict(top=["bandeau"], bottom="wrap_skirt", extras=["usekh", "armlets", "bracers"], colors=dict(cloth2="#e8c050")),
        hair="bob", bangs="blunt", ears=None, legs="bare", foot="shoe", tail=[(1.5, 1.5, 4), (1.25, 1.25, 4), (1, 1, 3.5)], tail_look="scale",
        tail_rest=[(-35, 0, 0), (12, 0, 0), (10, 0, 0)], pupil=(0.9, 3.6),
        pal=dict(SKIN=skin("brown"), HAIR="#141210", FUR="#7a6a3a", LIGHT="#e8dcb0", MARK="#3a2e1a", EYE="#e8b020", CLOTH="#f0ece0", TRIM="#e8c050",
                 pattern="bars", extra=dict(boot="#c8a050")),
        variants={"green": dict(FUR="#2a8a4a", MARK="#145a2a", LIGHT="#b8e8a0"),
                  "black": dict(FUR="#1e1c1a", MARK="#4a4440", LIGHT="#c8c0a8", CLOTH="#e8e0d0"),
                  "gold": dict(FUR="#c8a050", MARK="#f0e0a0", LIGHT="#fff4d0", CLOTH="#2a4a8a"),
                  "red": dict(FUR="#a83a2a", MARK="#5a1a10", LIGHT="#e8b8a0", CLOTH="#e8e0d0")}),
}


def declare(sp):
    d = SPECIES[sp]
    k = d["thick"]
    st = d.get("stub", 1.0)                             # a stubby tail: the same girth, much shorter
    parts = [(n, round(w * k * 4) / 4, round(h * k * 4) / 4 if i else h, round(dd * (st if i else 1) * 4) / 4) for i, (n, w, h, dd) in enumerate(SEGS)]
    extra = []
    if d.get("hood"):
        extra.append(("hood_p", 5.5, 6.5, .5))
    if d.get("crown"):
        extra += [("crown", 4.5, 1, 4.5), ("crown_spike", .75, 1.5, .5), ("crown_gem", 1, 1, .25)]
    if d.get("rattle"):
        extra.append(("rattle", 1.25, 1, .75))
    if d.get("paddle"):
        extra.append(("paddle", .5, 3.5, 4))
    if d.get("medusa"):
        extra += [("snake", 1, 5.5, 1), ("snake_h", 1.5, 1, 2), ("blindfold", 8.5, 1.75, 8.5), ("blind_tail", 1, 3, .25)]
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
    gold = shades("#e8c050")
    pt("crown", "all", lambda f, x, y, w, h: pick(gold, 0.55 + 0.3 * (y == 0), x, y))
    pt("crown_spike", "all", lambda f, x, y, w, h: pick(gold, 0.7 - 0.2 * (x == 0), x, y))
    pt("crown_gem", "all", lambda f, x, y, w, h: pick(shades("#3ac87a"), 0.7, x, y))
    pt("rattle", "all", lambda f, x, y, w, h: pick(shades("#c8b896"), 0.55 - (0.25 if y == h - 1 else 0), x, y))
    pt("paddle", "all", lambda f, x, y, w, h: pick(pal.FUR, 0.5, x, y) if (y // 3) % 2 else pick(pal.MARK, 0.4, x, y))
    sn, band = shades(pal.extra.get("snake", "#4a9a3a")), shades(pal.extra.get("snake_band", "#2a5a24"))
    band2 = shades(pal.extra.get("snake_band2", pal.extra.get("snake_band", "#2a5a24")))
    pt("snake", "all", lambda f, x, y, w, h: pick(band if y % 4 == 0 else band2 if y % 4 == 2 and pal.extra.get("snake_band2") else sn, 0.5 + 0.15 * (f == "north"), x, y))
    pt("snake_h", "all", lambda f, x, y, w, h: pick(shades("#f8d040"), 0.7, x, y) if f == "north" and y == 0 and x in (0, w - 1)
       else pick(sn, 0.45 + 0.1 * (f == "up"), x, y))
    bf = shades(pal.extra.get("blindfold", "#5a2a6a"))
    pt("blindfold", "all", lambda f, x, y, w, h: pick(bf, 0.45 + 0.15 * (y == 0) + (noise(x, y, 51) - 0.5) * 0.1, x, y) if f not in ("up", "down") else CLEAR)
    pt("blind_tail", "all", lambda f, x, y, w, h: pick(bf, 0.45, x, y))
    return pt.T


def build(sp):
    d = SPECIES[sp]
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    g = G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), build=d.get("build"), outfit=d.get("outfit"), ears=None, skirt=False))
    end_z = G.serpent(r, None, d)
    if d.get("crown"):                                  # the naga's crown: a gold band, five spikes, a jade gem at the front
        cs = [cube("crown", [-2.25, g.hy + 8.3, -2.25 + g.z])]
        for x in (-1.9, -0.95, 0, 0.95, 1.9):
            cs.append(cube("crown_spike", [x - 0.375, g.hy + 9.3, -2.4 + g.z]))
        cs.append(cube("crown_gem", [-0.5, g.hy + 8.3, -2.55 + g.z]))
        r.add("crown", "head", [0, g.hy + 8.3, g.z], cs)
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
    if d.get("medusa"):                                 # nine snakes rising from her head and curling out, each with its own head
        hy, z = g.hy, g.z
        cs = []
        for i, (x, zz, ry) in enumerate(((-3.2, -2.5, -30), (-1.2, -3.0, -10), (1.2, -3.0, 10), (3.2, -2.5, 30), (-3.4, 0.5, -70),
                                         (3.4, 0.5, 70), (-2.4, 3.0, -140), (0, 3.4, 180), (2.4, 3.0, 140))):
            tilt = 28 + 8 * (i % 3)
            cs.append(G.rot_cube("snake", [x - 0.5, hy + 7.6, zz - 0.5 + z], [x, hy + 7.6, zz + z], -tilt, ry, 0))
            tip = H.rot_about((x, hy + 7.6 + 5.0, zz + z), (x, hy + 7.6, zz + z), H.rot_matrix(-tilt, ry, 0))
            cs.append(G.rot_cube("snake_h", [tip[0] - 0.75, tip[1] - 0.3, tip[2] - 1.6], list(tip), 10, ry, 0))
        r.add("snakes", "head", [0, hy + 7.6, z], cs)
        r.add("blindfold", "head", [0, hy + 3.5, z], [cube("blindfold", [-4.25, hy + 2.6, -4.25 + z], inflate=0.3),
                                                      G.rot_cube("blind_tail", [-0.5, hy + 0.2, 4.5 + z], [0, hy + 3.2, 4.4 + z], 12, 0, 0)])
    return r.model(sp)


SEG_NAMES = [f"seg_{i}" for i in range(1, 9)]


def anims(an):
    G.serpent_anims(an, SEG_NAMES)
    Tk = H.track                                         # curled up, rolling down the hill like a wheel
    an.add("roll", 0.5, True, {"root": {"rotation": Tk((0, (0, 0, 0)), (0.25, (180, 0, 0)), (0.5, (360, 0, 0))), "position": H.hold(0, 5, 0)},
                               **{s: {"rotation": H.hold(-35, 0, 0)} for s in SEG_NAMES[1:]}})


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.3, -45, 20), ("rear", 0.9, -30, 8), ("sit", 0, -40, 25), ("bask", 0, -40, 15)]
FACE = [("idle", 0, 0, 0, 24), ("idle", 2.75, 0, 0, 24), ("hiss", 0.5, 0, 0, 24), ("idle", 0, -30, 8, 24)]


# --- the two-legged ones (the wadjet): girl_family's biped with the lamia hood, and the lamia's bask, coil and rear ---------

def legged_declare(sp, d):
    return [("hood_p", 5.5, 6.5, .5)]


def legged_paint(pt, pal, d):
    sc = G.scale_fn(pal, belly_faces=("down",))
    pt("hood_p", "all", lambda f, x, y, w, h: pick(pal.LIGHT, 0.55, x, y) if f == "north" else sc(f, x, y, w, h))


def legged_build(r, g, d):
    hp = G.S("hood_p")
    y, z = 19.5, 2.6                                    # higher and further back than on a serpent: it frames her head, behind the bob
    r.add("hood", "body", [0, y + 2.5, z], [G.rot_cube("hood_p", [-hp[0], y, z], [0, y + 2.5, z], 0, 0, 18),
                                             G.rot_cube("hood_p", [0, y, z], [0, y + 2.5, z], 0, 0, -18)])


def legged_anims(an, sp, d):
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("bask", 4.0, True, merge({"root": {"rotation": hold(-90, 0, 0), "position": hold(0, 2.5, 0)}, "head": {"rotation": hold(-30, 0, 0)},
                                     "eyelids": {"position": hold(*H.OUT_)}}, sym("arm", hold(-170, 0, 20))))
    an.add("coil", 1.5, True, merge({"body": {"rotation": hold(15, 0, 0)}, "hood": {"scale": hold(1.25, 1.1, 1)}},
                                    sym("arm", Tk((0, (-80, 0, 25)), (0.75, (-90, 0, 35)), (1.5, (-80, 0, 25))))))
    an.add("rear", 1.0, False, merge({"body": {"rotation": Tk((0, (0, 0, 0)), (0.5, (-15, 0, 0)))}, "head": {"rotation": hold(-15, 0, 0)},
                                      "hood": {"scale": Tk((0, (1, 1, 1)), (0.5, (1.35, 1.2, 1)))}}, sym("arm", hold(-30, 0, 30))))


if __name__ == "__main__":
    import girl_family as F
    legged = {k: v for k, v in SPECIES.items() if v.get("biped")}
    F.run_family("lamia", legged, legged_declare, legged_paint, legged_build, legged_anims)
    G.run_family("lamia", {k: v for k, v in SPECIES.items() if k not in legged}, declare, paint, build, anims, VIEWS, FACE)
