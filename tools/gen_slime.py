#!/usr/bin/env python3
"""Slimes (the jelly family, see slime_field_guide.html): verdant, magma, honey, frost, amethyst.

    python tools/gen_slime.py [species ...] [--previews]

Shared girl_lib upper body made of jelly, on a dripping jelly gown (bone `gown`) with a bright core in her chest
(bone `core`). The texture is semi-transparent (alpha 200) except the core: the game renders slimes with a
translucent render type. Element extras: amethyst crystals, frost spikes, a verdant sprout, honey drips. The magma
slime also gets glow masks (<species>[_<variant>]_glowmask.png). Five colours per species.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, shades, CLEAR  # noqa: E402

ALPHA = 200

SPECIES = {
    "verdant_slime": dict(
        build="curvy", outfit=dict(top=[]),
        extra="sprout", hair="bob",
        pal=dict(FUR="#6ad04a", LIGHT="#c8f8a0", MARK="#3a8a2a", HAIR="#8ae060", EYE="#1e5a1e", PUPIL="#0e3a0e", extra=dict(core="#2a8a2a")),
        variants={"mint": dict(FUR="#a8f0c8", HAIR="#c0f8d8", MARK="#5ab088", EYE="#1e6a4a", extra=dict(core="#3aa070")),
                  "olive": dict(FUR="#8a9a3a", HAIR="#a0b048", MARK="#5a6a1e", EYE="#3a4a10", extra=dict(core="#5a6a1e")),
                  "teal": dict(FUR="#3ab8a0", HAIR="#5ad0b8", MARK="#1e7a6a", EYE="#0e4a4a", extra=dict(core="#1e7a6a")),
                  "moss": dict(FUR="#a8c83a", HAIR="#c0d850", MARK="#6a8a1e", EYE="#4a5a10", extra=dict(core="#e8c040"))}),
    "magma_slime": dict(
        build="strong", outfit=dict(top=[], extras=["armlets"]),
        extra="crust", hair="shaggy", glow=True,
        pal=dict(FUR="#e8602a", LIGHT="#ffd060", MARK="#5a1a0a", HAIR="#ff8a3a", EYE="#ffe080", PUPIL="#6a1a0a", pattern="net", extra=dict(core="#fff0a0")),
        variants={"cinder": dict(FUR="#3a2a26", MARK="#ff7a2a", HAIR="#5a3a2a", extra=dict(core="#ff9a40")),
                  "blue": dict(FUR="#3a7ae8", LIGHT="#a8e8ff", MARK="#0e1a5a", HAIR="#5aa0ff", EYE="#d0f4ff", PUPIL="#0e1a5a", extra=dict(core="#e0faff")),
                  "white": dict(FUR="#ffe8b0", LIGHT="#ffffff", MARK="#c87a2a", HAIR="#fff0c8", PUPIL="#8a4a10", extra=dict(core="#ffffff")),
                  "crimson": dict(FUR="#a8141e", LIGHT="#ff7a4a", MARK="#3a0408", HAIR="#c8202a", extra=dict(core="#ffb080"))}),
    "honey_slime": dict(
        build="petite", outfit=dict(top=[], extras=["bell_collar"], colors=dict(ribbon="#c8402a")),
        extra="drip", hair="drills",
        pal=dict(FUR="#f0b030", LIGHT="#ffe890", MARK="#c87a10", HAIR="#f8c850", EYE="#8a4a10", PUPIL="#4a2208", extra=dict(core="#ffe070")),
        variants={"amber": dict(FUR="#c8781a", HAIR="#d88a2a", MARK="#8a4a08", EYE="#5a2a08", extra=dict(core="#f0a030")),
                  "clover": dict(FUR="#f8e8a0", HAIR="#fff2c0", MARK="#d8b860", EYE="#8a6a20", extra=dict(core="#fff8d0")),
                  "buckwheat": dict(FUR="#6a3a14", HAIR="#8a4e1e", MARK="#3a1e08", LIGHT="#c8884a", EYE="#f0c060", PUPIL="#3a1e08", extra=dict(core="#c8884a")),
                  "blossom": dict(FUR="#f4a8c0", HAIR="#f8c0d4", MARK="#d07898", EYE="#8a2a5a", extra=dict(core="#ffe0ea"))}),
    "frost_slime": dict(
        build="slim", outfit=dict(top=[], extras=["scarf"], colors=dict(scarf="#3a6ad0")),
        extra="spikes", hair="hime",
        pal=dict(FUR="#8ad0f0", LIGHT="#e8f8ff", MARK="#ffffff", HAIR="#b8e8ff", EYE="#2a6aa8", PUPIL="#0e2a5a", pattern="speckle", extra=dict(core="#ffffff", crystal="#e0f6ff")),
        variants={"glacier": dict(FUR="#d8eef8", HAIR="#eef8ff", EYE="#3a7ab8", extra=dict(core="#ffffff", crystal="#ffffff")),
                  "aurora": dict(FUR="#7ae8b0", MARK="#b07ae8", HAIR="#a0f0d0", pattern="patches", EYE="#5a2a9a", extra=dict(core="#e0c8ff", crystal="#c8a8ff")),
                  "deep": dict(FUR="#2a5ab8", HAIR="#4a7ad0", LIGHT="#8ab8f0", EYE="#d0eaff", PUPIL="#0e1a4a", extra=dict(core="#a8d8ff", crystal="#8ac0ff")),
                  "snow": dict(FUR="#f8d0dc", HAIR="#fde0ea", EYE="#a83a6a", extra=dict(core="#ffffff", crystal="#ffe8f0"))}),
    "amethyst_slime": dict(
        build="athletic", outfit=dict(top=[], extras=["usekh"], colors=dict(usekh2="#8a4ad0")),
        extra="crystals", hair="sidetail",
        pal=dict(FUR="#a070d8", LIGHT="#e0c8ff", MARK="#6a3aa8", HAIR="#c098f0", EYE="#4a1a8a", PUPIL="#20083a", extra=dict(core="#f0e0ff", crystal="#c8a0ff")),
        variants={"rose": dict(FUR="#f0a8c0", HAIR="#f8c4d6", MARK="#c87090", EYE="#8a2a5a", extra=dict(core="#fff0f4", crystal="#ffc8da")),
                  "citrine": dict(FUR="#f0c860", HAIR="#f8d880", MARK="#c08a20", EYE="#7a4a10", extra=dict(core="#fff4c8", crystal="#ffe08a")),
                  "smoky": dict(FUR="#8a7a70", HAIR="#a09088", MARK="#4a3e38", EYE="#2a2420", extra=dict(core="#e0d4cc", crystal="#b0a094")),
                  "emerald": dict(FUR="#40c070", HAIR="#60d88a", MARK="#1e7a40", EYE="#0e4a24", extra=dict(core="#d8ffe8", crystal="#80f0a8"))}),
}

# a mixed slime: two kinds of slime flowed into one (variants: every pair, in this order; the first is the default)
KINDS = ["verdant", "magma", "honey", "frost", "amethyst"]
PAIRS = [f"{a}_{b}" for i, a in enumerate(KINDS) for b in KINDS[i + 1:]]
SPECIES["mixed_slime"] = dict(build="standard", outfit=dict(top=[]), extra="none", hair="wavy",
                              pal=SPECIES["verdant_slime"]["pal"], variants={k: {} for k in PAIRS[1:]})


def smooth_noise(x, y, seed, cell=6):
    """value noise, bilinear between a grid of random values (for the marbling)"""
    import random as _r

    def v(ix, iy):
        return _r.Random(ix * 73856093 ^ iy * 19349663 ^ seed).random()
    gx, gy = x / cell, y / cell
    ix, iy = int(gx), int(gy)
    fx, fy = gx - ix, gy - iy
    a = v(ix, iy) * (1 - fx) + v(ix + 1, iy) * fx
    b = v(ix, iy + 1) * (1 - fx) + v(ix + 1, iy + 1) * fx
    return a * (1 - fy) + b * fy


def paint_mixed(key):
    """both kinds painted on the same model, then marbled together: swirling bands of one through the other"""
    import math
    a, b = (key or PAIRS[0]).split("_")
    ta = paint("mixed_slime", None, base=SPECIES[a + "_slime"]["pal"])
    tb = paint("mixed_slime", None, base=SPECIES[b + "_slime"]["pal"])
    out = [row[:] for row in ta]
    for y, row in enumerate(out):
        for x in range(len(row)):
            band = math.sin((x * 0.9 + y * 0.55) / 6.0 + 4.0 * smooth_noise(x, y, 7, cell=10))
            if band > 0.15:
                row[x] = tb[y][x]
    return out


def declare(sp):
    d = SPECIES[sp]
    parts = [("gown_a", 10, 2.5, 9), ("gown_b", 8.5, 3, 7.5), ("gown_c", 7, 3.5, 6), ("gown_d", 5.5, 4, 4.5), ("drip", 1, 1.5, 1), ("core", 2, 2, 2)]
    if d["extra"] in ("crystals", "spikes"):
        parts += [("crystal", 1, 2.5, 1), ("crystal_s", .75, 1.5, .75)]
    if d["extra"] == "sprout":
        parts += [("leaf", .25, 2, 1.5)]
    G.declare(parts, ears=None, hair=SPECIES[sp]["hair"],
              build=SPECIES[sp].get("build"), outfit=SPECIES[sp].get("outfit"))


def paint(sp, key, glow=False, base=None):
    if sp == "mixed_slime" and base is None:
        return paint_mixed(key)
    d = SPECIES[sp]
    base = dict(base or d["pal"])
    over = d["variants"].get(key) if key else None
    pal = G.pal_from(base, over)
    pal.SKIN = shades(base["FUR"] if not over or "FUR" not in over else over["FUR"], n=4, dark=0.7, light=0.4, nodither=True)
    pal.BLUSH = pick(pal.LIGHT, 0.9, 0, 0)
    pal.BLUSH_L = pick(pal.LIGHT, 0.7, 0, 0)
    pt = H.Painter()
    G.paint_upper(pt, pal, top="skin", outfit=d.get("outfit"), build=d.get("build"))
    jelly = G.jelly_fn(pal, alpha=255)
    for p in ("gown_a", "gown_b", "gown_c", "gown_d", "drip"):
        pt(p, "all", jelly)
    core = shades(pal.extra.get("core", "#ffffff"))
    pt("core", "all", lambda f, x, y, w, h: pick(core, 0.7 + 0.25 * ((x + y) % 3 == 0), x, y))
    cr = shades(pal.extra.get("crystal", "#ffffff"))
    for p in ("crystal", "crystal_s"):
        pt(p, "all", lambda f, x, y, w, h: pick(cr, 0.55 + 0.35 * (x == 0) - 0.2 * (f in ("south", "east")), x, y))
    pt("leaf", "all", lambda f, x, y, w, h: pick(shades("#4ab82a"), 0.5 + 0.3 * (x == w // 2), x, y))
    T = pt.T
    core_rects = [H.RECT[("core", f)] for f in G.H.FACES]

    def in_core(x, y):
        return any(cx <= x < cx + cw and cy <= y < cy + ch for cx, cy, cw, ch in core_rects)
    if glow:                                   # the magma slime's glow: the bright cracks, the core and her eyes
        out = [[CLEAR] * len(T[0]) for _ in T]
        for y, row in enumerate(T):
            for x, c in enumerate(row):
                if c[3] and (in_core(x, y) or (c[0] + c[1] + c[2]) > 520):
                    out[y][x] = (c[0], c[1], c[2], 255)
        return out
    for y, row in enumerate(T):                # everything but the core is translucent jelly
        for x, c in enumerate(row):
            if c[3] and not in_core(x, y):
                row[x] = (c[0], c[1], c[2], min(c[3], ALPHA))
    return T


def build(sp):
    d = SPECIES[sp]
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), build=d.get("build"), outfit=d.get("outfit"), ears=None, skirt=False, sash=False, pendant=False, cuffs=False))
    G.slime(r, None, d)
    ex = d["extra"]
    if ex in ("crystals", "spikes"):
        cs = []
        for x, z, rz, p in ((-2.6, -1.5, -20, "crystal"), (-1.0, -2.6, 8, "crystal_s"), (1.6, -1.8, 25, "crystal"), (2.8, 0.8, 35, "crystal_s"),
                            (-2.2, 1.6, -30, "crystal_s"), (0.4, 2.2, -6, "crystal")):
            w, h, dd = G.S(p)
            cs.append(G.rot_cube(p, [x - w / 2, 28.4, z - dd / 2], [x, 28.4, z], 0, 0, rz))
        r.add("crystals", "head", [0, 28.4, 0], cs)
        sh = []
        for sg in (-1, 1):
            sh.append(G.rot_cube("crystal_s", [sg * 2.4 - 0.375, 19.6, -0.4], [sg * 2.4, 19.6, 0], 0, 0, -30 * sg))
        r.put("body", *sh)
    if ex == "sprout":
        r.add("sprout", "head", [0, 28.6, 0], [G.rot_cube("leaf", [-0.125, 28.6, -0.75], [0, 28.6, 0], 0, 0, 30),
                                                G.rot_cube("leaf", [-0.125, 28.6, -0.75], [0, 28.6, 0], 0, 0, -30)])
    if ex == "drip":
        for x, y, z in ((-2.0, 24.5, -4.6), (2.4, 23.5, -4.5), (-4.4, 15.5, 0.2), (4.4, 14.5, -0.4)):
            r.put("body" if y < 20 else "head", cube("drip", [x - 0.5, y, z]))
    return r.model(sp)


def anims(an):
    G.slime_anims(an)


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.3, -30, 8), ("absorb", 0.3, -35, 10), ("sit", 0, -30, 10), ("lie", 0, -40, 25)]
FACE = [("idle", 0, 0, 0, 24), ("idle", 2.75, 0, 0, 24), ("absorb", 0.3, 0, 0, 24), ("idle", 0, -30, 8, 24)]


def write_glowmasks(sp):
    if not SPECIES[sp].get("glow"):
        return
    tdir = os.path.join(G.ASSETS, "textures", "entity")
    H.write_png(os.path.join(tdir, sp + "_glowmask.png"), paint(sp, None, glow=True))
    for k in SPECIES[sp]["variants"]:
        H.write_png(os.path.join(tdir, f"{sp}_{k}_glowmask.png"), paint(sp, k, glow=True))
    print(sp, "glow masks written")


def write_tint(sp):
    """<species>_tint.png: the default texture in light greys (alpha kept), for slimes the game tints in any RGB colour"""
    out = []
    for row in paint(sp, None):
        r2 = []
        for c in row:
            if not c[3]:
                r2.append(CLEAR)
                continue
            lum = (0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]) / 255.0
            g = int(120 + 135 * lum)                   # 120..255: dark details stay darker, the tint carries the colour
            r2.append((g, g, g, c[3]))
        out.append(r2)
    H.write_png(os.path.join(G.ASSETS, "textures", "entity", sp + "_tint.png"), out)
    if SPECIES[sp].get("glow"):                        # the glow layer looks for <texture>_glowmask.png, tinted one included
        H.write_png(os.path.join(G.ASSETS, "textures", "entity", sp + "_tint_glowmask.png"), paint(sp, None, glow=True))


if __name__ == "__main__":
    G.run_family("slime", SPECIES, declare, paint, build, anims, VIEWS, FACE)
    for s in SPECIES:
        if s in sys.argv or not [a for a in sys.argv[1:] if not a.startswith("--")]:
            declare(s)
            write_tint(s)
    for s in SPECIES:
        if s in sys.argv or not [a for a in sys.argv[1:] if not a.startswith("--")]:
            declare(s)
            write_glowmasks(s)
