#!/usr/bin/env python3
"""Centaurs (the hoofed family, see centaur_field_guide.html): horse, zebra, reindeer, mountain goat, alpaca.

    python tools/gen_centaur.py [species ...] [--previews]

Shared girl_lib upper body (waist at 19.5) on a four-legged lower body: barrel, leg_f_r/f_l/b_r/b_l with shin_* and
hooves, tail. Species extras: reindeer antlers (bone `antlers`), goat horns (`horns`), alpaca fleece (inflated
barrel). Horse-like ears. Five colours per species. `armor_torso` is an invisible torso-sized box the game fits a
worn chestplate to (GeckoLib's vanilla armour layer).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, shades  # noqa: E402

SPECIES = {
    "horse_centaur": dict(
        build="athletic", outfit=dict(top=["bandeau", "harness"], extras=["bracers", "belt"]),
        hair="flowing", tail=(2, 9, 2),
        pal=dict(SKIN=skin("fair"), HAIR="#2a1c16", FUR="#7a4426", LIGHT="#a8704a", MARK="#1e1612", EYE="#5a3a22", CLOTH="#2a4a6a", TRIM="#d8b860",
                 pattern="solid", extra=dict(socks=True)),
        variants={"chestnut": dict(FUR="#a85a2a", HAIR="#8a4a22", MARK="#7a3e1a", CLOTH="#3a5a3a"),
                  "palomino": dict(FUR="#d8b060", HAIR="#f2ead0", MARK="#c8a050", LIGHT="#f0dca8", CLOTH="#7a3a3a"),
                  "grey": dict(FUR="#a8a8a8", HAIR="#d8d8d8", MARK="#6a6a6a", pattern="dapple", LIGHT="#dcdcdc", CLOTH="#4a3a6a"),
                  "black": dict(FUR="#1e1c1e", HAIR="#141214", MARK="#0e0c0e", LIGHT="#3a3638", CLOTH="#8a2a2a")}),
    "zebra_centaur": dict(
        build="strong", outfit=dict(top=["sports"], extras=["pauldron", "arm_wraps", "belt"]),
        hair="pixie", tail=(1.5, 8, 1.5),
        pal=dict(SKIN=skin("brown"), HAIR="#141414", FUR="#f0eee8", LIGHT="#f8f6f2", MARK="#141414", EYE="#3a2418", CLOTH="#c8702a", TRIM="#e8d8a0",
                 pattern="stripes"),
        variants={"grevy": dict(pattern="finestripes"),
                  "mountain": dict(pattern="stripes", pattern_scale=1.3, FUR="#e8e0d0"),
                  "golden": dict(FUR="#f6ecd2", MARK="#c8a060", HAIR="#c8a060", CLOTH="#4a6a8a"),
                  "quagga": dict(FUR="#a07850", MARK="#3a2a1e", LIGHT="#ece4d6", HAIR="#3a2a1e", pattern="finestripes", pattern_scale=1.5)}),
    "reindeer_centaur": dict(
        build="standard", outfit=dict(top=["sweater"], extras=["scarf"], colors=dict(scarf="#2e7a4a")),
        hair="braid", tail=(2, 3, 1.5), antlers=True,
        pal=dict(SKIN=skin("pale"), HAIR="#e8e2d4", FUR="#7a6048", LIGHT="#ece4d6", MARK="#4a3a2a", EYE="#4a3020", CLOTH="#9a2a2a", TRIM="#f0e8d0",
                 pattern="solid", extra=dict(antler="#c8b090")),
        variants={"woodland": dict(FUR="#4a3626", HAIR="#c8b8a0", CLOTH="#2a4a2a"),
                  "svalbard": dict(FUR="#c8beb0", HAIR="#f4f0e8", CLOTH="#3a5a7a"),
                  "white": dict(FUR="#f0eee8", HAIR="#ffffff", LIGHT="#ffffff", extra=dict(antler="#e8dcc0"), CLOTH="#6a3a8a"),
                  "piebald": dict(pattern="patches", MARK="#f2f0ea", HAIR="#d8d0c0")}),
    "goat_centaur": dict(
        build="slim", outfit=dict(top=["halter", "vest"], extras=["satchel"]),
        hair="sidetail", tail=(1.5, 2.5, 1.5), horns=True,
        pal=dict(SKIN=skin("rosy"), HAIR="#f4f2ee", FUR="#efede6", LIGHT="#ffffff", MARK="#3a3632", EYE="#c8a040", CLOTH="#5a7a3a", TRIM="#d0c8b0",
                 pattern="solid", pupil=(3.0, 0.9), extra=dict(horn="#2a2826")),
        variants={"ibex": dict(FUR="#8a7a62", HAIR="#6a5a46", LIGHT="#cfc4b2", extra=dict(horn="#6a5e4e"), CLOTH="#3a4a5a"),
                  "chamois": dict(FUR="#a0643a", HAIR="#3a2a20", MARK="#1e1612", pattern="cross", extra=dict(horn="#141210"), CLOTH="#6a4a2a"),
                  "grey": dict(FUR="#7a7a7e", HAIR="#9a9a9e", LIGHT="#bcbcc0", CLOTH="#4a3a5a"),
                  "cream": dict(FUR="#e8dcc0", HAIR="#f0e6cc", extra=dict(horn="#5a4a3a"))}),
    "alpaca_centaur": dict(
        build="curvy", outfit=dict(top=["bandeau", "capelet"], extras=["pearls"], colors=dict(cloth2="#c8463a")),
        hair="mane", tail=(2, 2.5, 2), fleece=True,
        pal=dict(SKIN=skin("peach"), HAIR="#f4f0e8", FUR="#f2ede2", LIGHT="#ffffff", MARK="#d8d0c0", EYE="#2a1e16", CLOTH="#c84a6a", TRIM="#f0c840",
                 pattern="solid"),
        variants={"fawn": dict(FUR="#d8b888", HAIR="#e2c49a", MARK="#c8a070", CLOTH="#3a7a8a"),
                  "brown": dict(FUR="#7a5236", HAIR="#8a6044", MARK="#5a3a24", CLOTH="#e0a030"),
                  "black": dict(FUR="#2a2624", HAIR="#3a3430", MARK="#1a1614", LIGHT="#5a5450", CLOTH="#d0d0d0"),
                  "grey": dict(FUR="#a8a6a4", HAIR="#c0bebc", MARK="#8a8886", CLOTH="#6a3a8a")}),
}


def declare(sp):
    d = SPECIES[sp]
    parts = [("barrel", 7, 7, 15), ("withers", 5, 2.5, 5), ("uleg_f", 2.25, 6.5, 2.75), ("lleg_f", 1.75, 5.75, 1.75),
             ("uleg_b", 2.5, 6.5, 3.25), ("lleg_b", 1.75, 5.75, 1.75), ("hoof", 2.25, 1.25, 2.25), ("htail",) + d["tail"],
             ("armor_ref", 6, 8, 4)]
    if d.get("antlers"):
        parts += [("antler_a", .75, 5, .75), ("antler_b", .75, 2.5, .75), ("antler_c", .5, 2, .5)]
    if d.get("horns"):
        parts += [("horn_a", 1, 3, 1), ("horn_b", .75, 2.5, .75)]
    if d.get("fleece"):
        parts += [("fleece", 7.5, 4, 14)]
    G.declare(parts, ears="horse", hair=SPECIES[sp]["hair"],
              build=SPECIES[sp].get("build"), outfit=SPECIES[sp].get("outfit"))


def paint(sp, key):
    d = SPECIES[sp]
    pal = G.pal_from(d["pal"], d["variants"].get(key) if key else None)
    pt = H.Painter()
    G.paint_upper(pt, pal, top="wrap", top_pattern="check" if sp.startswith("horse") else "weave", outfit=d.get("outfit"), build=d.get("build"))
    coat = G.fur_fn(pal, light_faces=("down",), grain=0.10, seed=31)
    G.paint_ears(pt, pal, G.fur_fn(pal, pattern="solid"))
    pt("barrel", "all", coat)
    pt("withers", "all", lambda f, x, y, w, h: H.skin_fn(pal, 0.66)(f, x, y, w, h) if f in ("north", "up") and y < h * 0.5 else coat(f, x, y, w, h))
    pt("uleg_f", "all", coat)
    pt("uleg_b", "all", coat)
    socks = pal.extra.get("socks")
    lower = lambda f, x, y, w, h: (pick(pal.LIGHT if socks else pal.MARK, 0.7, x, y) if y > h * 0.6 and socks else coat(f, x, y, w, h))
    pt("lleg_f", "all", lower)
    pt("lleg_b", "all", lower)
    pt("hoof", "all", lambda f, x, y, w, h: pick(shades("#3a302a"), 0.45 + 0.2 * (y == 0) + (noise(x, y, 3) - 0.5) * 0.1, x, y))
    pt("htail", "all", lambda f, x, y, w, h: pick(pal.HAIR, 0.45 + 0.25 * ((x + y // 3) % 3 == 0) + (noise(x, y, 5) - 0.5) * 0.15, x, y)
       if sp != "zebra_centaur" or y > h * 0.6 else pick(pal.FUR, 0.5, x, y))
    ant = shades(pal.extra.get("antler", "#c8b090"))
    for p in ("antler_a", "antler_b", "antler_c"):
        pt(p, "all", lambda f, x, y, w, h: pick(ant, 0.5 + (noise(x, y, 7) - 0.5) * 0.2 + (0.2 if y == 0 else 0), x, y))
    horn = shades(pal.extra.get("horn", "#2a2826"))
    for p in ("horn_a", "horn_b"):
        pt(p, "all", lambda f, x, y, w, h: pick(horn, 0.45 + 0.25 * (y % 3 == 0), x, y))
    pt("fleece", "all", lambda f, x, y, w, h: pick(pal.FUR, 0.62 + 0.18 * (noise(x // 2, y // 2, 9) > 0.5) + (noise(x, y, 8) - 0.5) * 0.1, x, y))
    pt("armor_ref", "all", lambda f, x, y, w, h: G.CLEAR)
    return pt.T


def build(sp):
    d = SPECIES[sp]
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    g = G.upper(r, 19.5, dict(hair=d["hair"], bangs=d.get("bangs"), build=d.get("build"), outfit=d.get("outfit"), ears="horse", skirt=False), z0=-2.25)
    G.taur(r, None, d)
    hy, z = g.hy, g.z
    r.add("armor_torso", "body", [0, 19.5, z], [cube("armor_ref", [-3, 19.25, z - 2])])
    if d.get("antlers"):
        cs = []
        for sg in (-1, 1):
            x = sg * 2.6
            base = [x - 0.375, hy + 8.4, -0.4 + z]
            cs.append(G.rot_cube("antler_a", base, [x, hy + 8.4, z], -20, 0, 22 * sg))
            cs.append(G.rot_cube("antler_b", [x + sg * 1.8 - 0.375, hy + 11.0, -1.4 + z], [x + sg * 1.8, hy + 11.0, -1.0 + z], -45, 0, -10 * sg))
            cs.append(G.rot_cube("antler_c", [x + sg * 2.6 - 0.25, hy + 12.4, 0.8 + z], [x + sg * 2.6, hy + 12.4, 1.0 + z], 10, 0, 30 * sg))
            cs.append(G.rot_cube("antler_b", [x + sg * 0.6 - 0.375, hy + 9.6, -1.9 + z], [x + sg * 0.6, hy + 9.6, -1.6 + z], -60, 0, 0))
        r.add("antlers", "head", [0, hy + 8.4, z], cs)
    if d.get("horns"):
        cs = []
        for sg in (-1, 1):
            x = sg * 1.6
            cs.append(G.rot_cube("horn_a", [x - 0.5, hy + 8.4, -0.8 + z], [x, hy + 8.4, -0.3 + z], -28, 0, 6 * sg))
            cs.append(G.rot_cube("horn_b", [x - 0.375, hy + 10.9, 0.6 + z], [x, hy + 10.9, 0.9 + z], -62, 0, 6 * sg))
        r.add("horns", "head", [0, hy + 8.4, z], cs)
    if d.get("fleece"):
        r.put("barrel", cube("fleece", [-3.75, 13.25, -4.0]))
    return r.model(sp)


def anims(an):
    G.taur_anims(an)


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.3, -45, 8), ("run", 0.15, -70, 5), ("sit", 0, -35, 10), ("rear", 0.8, -60, 5)]
FACE = [("idle", 0, 0, 0, 31.5), ("idle", 2.75, 0, 0, 31.5), ("rear", 0.8, 0, 0, 31.5), ("idle", 0, -30, 8, 31.5)]

if __name__ == "__main__":
    G.run_family("centaur", SPECIES, declare, paint, build, anims, VIEWS, FACE)
