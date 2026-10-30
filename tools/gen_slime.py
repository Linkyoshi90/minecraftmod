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
    # seventeenth wave: the klabuster (German "Klabusterbeere", a dingleberry): a glossy brown swirl of a slime, dangling
    # on a single strand from a tuft of hair
    "klabuster_slime": dict(
        build="petite", outfit=dict(top=[]), extra="swirl", hair="bob", bangs="blunt",
        pal=dict(FUR="#5e3818", LIGHT="#a87040", MARK="#2e1a0a", HAIR="#6e4420", EYE="#1e120a", PUPIL="#0e0804",
                 extra=dict(core="#f0c890", tuft="#5a3a22")),
        variants={"blue": dict(FUR="#3a8ae8", LIGHT="#c8e8ff", MARK="#1e5ab8", HAIR="#5aa8f8", EYE="#0e2a6a", PUPIL="#0a1a4a",
                               extra=dict(core="#e8f8ff", tuft="#4a3a2a")),
                  "green": dict(FUR="#6aa82a", LIGHT="#d0f0a0", MARK="#3a6a14", HAIR="#80c03a", EYE="#1a2a0a", PUPIL="#0e1804",
                                extra=dict(core="#f0ffd0", tuft="#4a4a22")),
                  "purple": dict(FUR="#9a3ac8", LIGHT="#e8b0f8", MARK="#5a1a7a", HAIR="#b05ae0", EYE="#2a0a3a", PUPIL="#1a0428",
                                 extra=dict(core="#f8e0ff", tuft="#5a2a6a")),
                  "gold": dict(FUR="#e0a820", LIGHT="#fff0a0", MARK="#8a5a10", HAIR="#f0c040", EYE="#3a2408", PUPIL="#1e1404",
                               extra=dict(core="#fff8d0", tuft="#8a6a2a"))}),
    # fourteenth wave: the undine, a girl of living water, bubbles drifting round her
    "undine_slime": dict(
        build="slim", outfit=dict(top=[], extras=["pearls"]),
        extra="motes", hair="flowing", drop_kit=True,
        pal=dict(FUR="#5ab0f0", LIGHT="#d8f0ff", MARK="#2a6ab8", HAIR="#8ac8f8", EYE="#1e4a8a", PUPIL="#0e2450", extra=dict(core="#ffffff")),
        variants={"teal": dict(FUR="#40c8c0", HAIR="#70dcd4", MARK="#1e7a74", EYE="#0e4a46", extra=dict(core="#e0fffc")),
                  "clear": dict(FUR="#d0ecf8", HAIR="#e4f4fc", MARK="#8ab8d0", EYE="#3a6a8a", extra=dict(core="#ffffff")),
                  "deep": dict(FUR="#2a4ab8", HAIR="#4a6ad0", LIGHT="#8ab0f0", MARK="#14286a", EYE="#c8e0ff", PUPIL="#0e1a4a", extra=dict(core="#a8c8ff")),
                  "sea": dict(FUR="#4ab89a", HAIR="#6ad0b4", MARK="#1e6a54", EYE="#0e3a2e", extra=dict(core="#d8fff0")),
                  # homage (2026-10-30): the Dragon Quest slime: clear blue, a drop's point on top of her head, a wide smile
                  "drop_smile": dict(FUR="#3a8ae8", HAIR="#5aa8f8", LIGHT="#d8ecff", MARK="#1e4aa8", EYE="#1e1e24", PUPIL="#0a0a10",
                                     extra=dict(core="#ffffff", drop=True, smile=True))}),
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
                  "emerald": dict(FUR="#40c070", HAIR="#60d88a", MARK="#1e7a40", EYE="#0e4a24", extra=dict(core="#d8ffe8", crystal="#80f0a8")),
                  # homage (2026-10-30): Kirby: pink all over, rosy cheeks, deep blue eyes, red crystals for her shoes' red
                  "puffball": dict(FUR="#f8a8c8", HAIR="#f8b8d0", LIGHT="#fff0f6", MARK="#d86a98", EYE="#1e2a6a", PUPIL="#0a0e2a",
                                   extra=dict(core="#fff4f8", crystal="#e8283a"))}),
}

SPECIES["ender_slime"] = dict(                         # ninth wave: dark jelly, glowing eyes and core, ender motes floating round her
    build="slim", outfit=dict(top=[], extras=["armlets"]), extra="motes", hair="hime", bangs="blunt", glow=True,
    pal=dict(FUR="#2a1a3e", LIGHT="#6a4a9a", MARK="#0e0618", HAIR="#3a2458", EYE="#f088ff", PUPIL="#5a0a7a", extra=dict(core="#f0b0ff", mote="#e080ff")),
    variants={"chorus": dict(FUR="#9a78b0", LIGHT="#d8c0e8", MARK="#5a3a6a", HAIR="#b898c8", EYE="#f8e0ff", PUPIL="#5a2a6a", extra=dict(core="#fff0ff", mote="#f8d0ff")),
              "pearl": dict(FUR="#1a4a4a", LIGHT="#3a8a80", MARK="#0a2020", HAIR="#2a6a62", EYE="#a0fff0", PUPIL="#0a3a3a", extra=dict(core="#d0fff8", mote="#80f0e0")),
              "end_stone": dict(FUR="#d8d49a", LIGHT="#f4f0c8", MARK="#8a865a", HAIR="#e8e4b0", EYE="#c060e8", PUPIL="#3a0a4a", extra=dict(core="#f0b0ff", mote="#c060e8")),
              "shulker": dict(FUR="#8a5a8a", LIGHT="#c890c0", MARK="#4a2a4a", HAIR="#a070a0", EYE="#f8e060", PUPIL="#5a3a0a", extra=dict(core="#fff0a0", mote="#f8e060"))})

SPECIES["mimic_slime"] = dict(
    build="curvy", outfit=dict(top=[], extras=["pearls"]), extra="chest", hair="wavy",
    pal=dict(FUR="#b05ad0", LIGHT="#f0b0ff", MARK="#6a2a8a", HAIR="#c87ae8", EYE="#f8d040", PUPIL="#3a0a1a",
             extra=dict(core="#ffe060", wood="#8a5a2a", iron="#5a5a60")),
    variants={"spruce": dict(extra=dict(core="#ffe060", wood="#4a3020", iron="#3a3a40")),
              "gilded": dict(extra=dict(core="#fff0a0", wood="#6a4020", iron="#e8c040")),
              "ender": dict(FUR="#2ab890", HAIR="#5ad8b0", MARK="#0e5a4a", LIGHT="#b0f8e0", extra=dict(core="#c8fff0", wood="#1a2a2a", iron="#2a8a7a")),
              "christmas": dict(FUR="#e85a6a", HAIR="#f88a9a", MARK="#8a1a2a", LIGHT="#ffc8d0", extra=dict(core="#ffffff", wood="#c82a2a", iron="#e8e8e8"))})

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
    if d["extra"] == "motes":
        parts += [("mote", .75, .75, .75)]
    if d.get("drop_kit"):                      # the point of a drop on top of her head (the drop_smile look only)
        parts += [("sdrop_a", 5, 2, 5), ("sdrop_b", 3, 2, 3), ("sdrop_c", 1.5, 1.75, 1.5)]
    if d["extra"] == "swirl":                  # the klabuster's swirl, its strand and the tuft of hair it hangs from
        parts += [("swirl_a", 7, 2, 7), ("swirl_b", 5, 2, 5), ("swirl_c", 3, 1.75, 3), ("strand", .5, 6, .5), ("tuft", 4, 1.5, 4), ("tuft_hair", 1, 2, 1)]
    if d["extra"] == "chest":
        parts += [("box", 11, 6, 10), ("lid", 11, 2.5, 10), ("latch", 1.5, 2, .5), ("tooth", .5, 1, .5)]
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
    if pal.extra.get("smile"):                 # a wide open smile, a dark mouth with a pink tongue
        pal.mouth_px = {(x, 26): pal.LIP_D for x in range(11, 21)}
        pal.mouth_px.update({(x, 27): pal.MOUTH_IN for x in range(12, 20)})
        pal.mouth_px.update({(x, 28): pal.TONGUE if 14 <= x <= 17 else pal.MOUTH_IN for x in range(13, 19)})
        pal.mouth_px.update({(x, 29): pal.LIP_D for x in range(14, 18)})
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
    wood, iron = shades(pal.extra.get("wood", "#8a5a2a")), shades(pal.extra.get("iron", "#5a5a60"))

    def planks(f, x, y, w, h):                 # the mimic's chest: planks, an iron band round each face
        if x in (0, w - 1) or y in (0, h - 1):
            return pick(iron, 0.45, x, y)
        return pick(wood, 0.45 + 0.15 * (y % 4 == 0) + (noise(x // 3, y, 41) - 0.5) * 0.15, x, y)
    for p in ("box", "lid"):
        pt(p, "all", planks)
    pt("latch", "all", lambda f, x, y, w, h: pick(iron, 0.7, x, y))
    pt("tooth", "all", lambda f, x, y, w, h: pick(shades("#f4f0e0"), 0.7, x, y))
    pt("mote", "all", lambda f, x, y, w, h: pick(shades(pal.extra.get("mote", "#e080ff")), 0.8, x, y))
    for p in ("sdrop_a", "sdrop_b", "sdrop_c"):
        pt(p, "all", (lambda f, x, y, w, h: CLEAR) if not pal.extra.get("drop") else jelly)
    for p in ("swirl_a", "swirl_b", "swirl_c"):            # glossy coils: a darker groove at the bottom of each, a shine on top
        pt(p, "all", lambda f, x, y, w, h: pick(pal.FUR, 0.3 if y == h - 1 and f != "up" else 0.7 if f == "up" and (x + y) % 5 == 0 else 0.55, x, y))
    pt("strand", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.45, x, y))
    tuft = shades(pal.extra.get("tuft", "#5a3a22"))
    pt("tuft", "all", lambda f, x, y, w, h: pick(tuft, 0.35 + 0.3 * ((x + 2 * y) % 3 == 0), x, y))
    pt("tuft_hair", "all", lambda f, x, y, w, h: pick(tuft, 0.4 + 0.2 * (y % 2), x, y))
    T = pt.T
    core_rects = [H.RECT[(p, f)] for p in ("core", "mote", "box", "lid", "latch", "tooth", "strand", "tuft", "tuft_hair") if (p, "north") in H.RECT for f in G.H.FACES]

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
    if ex == "chest":                          # she rises out of an open treasure chest; the lid (bone `lid`) snaps shut when she bites
        r.put("gown", cube("box", [-5.5, 0, -5.0]))
        teeth = [cube("tooth", [x, 5.0, -5.4]) for x in (-4.5, -2.5, -0.5, 1.5, 3.5)]
        r.add("lid", "root", [0, 6, 5.0], [G.rot_cube("lid", [-5.5, 6, -5.0], [0, 6, 5.0], -110, 0, 0),
                                            G.rot_cube("latch", [-0.75, 6.5, -5.5], [0, 6, 5.0], -110, 0, 0)] + teeth)
    if d.get("drop_kit"):                      # stacked to a point on the crown of her head (hair_: not her height)
        r.add("hair_sdrop", "head", [0, 28.4, 0], [cube("sdrop_a", [-2.5, 28.2, -2.5]), cube("sdrop_b", [-1.5, 30.2, -1.5]),
                                                   cube("sdrop_c", [-0.75, 32.2, -0.75])])
    if ex == "motes":                          # specks of the End drifting round her
        r.add("motes", "body", [0, 16, 0], [cube("mote", [x, y, zz]) for x, y, zz in ((-5.2, 21.5, -1.0), (4.6, 18.0, 1.5), (-4.8, 11.0, 2.4),
                                                                                       (5.0, 25.5, -2.0), (-1.0, 30.5, 2.6), (2.6, 8.5, -3.4))])
    if ex == "swirl":                          # a soft-serve swirl on her head; above it the strand and the tuft (hair_: not her height)
        r.add("hair_swirl", "head", [0, 34.0, 0], [cube("swirl_a", [-3.5, 28.2, -3.5]), cube("swirl_b", [-2.5, 30.2, -2.5]), cube("swirl_c", [-1.5, 32.2, -1.5]),
                                                    cube("strand", [-0.25, 34.0, -0.25]), cube("tuft", [-2, 40.0, -2]),
                                                     G.rot_cube("tuft_hair", [-1.8, 39.0, -1.0], [-1.3, 40.5, -0.5], 0, 0, 25),
                                                     G.rot_cube("tuft_hair", [0.8, 39.0, 0.2], [1.3, 40.5, 0.7], 0, 0, -25)])
    if ex == "drip":
        for x, y, z in ((-2.0, 24.5, -4.6), (2.4, 23.5, -4.5), (-4.4, 15.5, 0.2), (4.4, 14.5, -0.4)):
            r.put("body" if y < 20 else "head", cube("drip", [x - 0.5, y, z]))
    return r.model(sp)


def anims(an):
    G.slime_anims(an)
    if "lid" in an.bones:                      # the mimic's bite: the lid slams down and springs open again
        T = H.track
        an.add("bite", 0.6, False, {"lid": {"rotation": T((0, (0, 0, 0)), (0.15, (100, 0, 0)), (0.35, (100, 0, 0)), (0.6, (0, 0, 0)))},
                                    "body": {"rotation": T((0, (0, 0, 0)), (0.15, (25, 0, 0)), (0.6, (0, 0, 0)))}})


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
