#!/usr/bin/env python3
"""Kitsune (the fox family, see kitsune_field_guide.html): red fox, arctic fox, fennec, corsac fox, gray fox.

    python tools/gen_kitsune.py [species ...] [--previews]

Shared girl_lib rig with fur legs and nine fox tails (tail_1..tail_9, fanned out behind her; the game shows as many as
her bond allows: one wild, two at high friendship, three devoted, nine for a kyubi elder). Shrine-maiden outfit: a
white top and a coloured skirt. Five colours per species.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, shades  # noqa: E402

SPECIES = {
    # sixteenth wave: the dunefennec, a Harmontide fennec in night blue with gold crescents, a purple saddle-cloth and a bell
    "dunefennec_kitsune": dict(
        build="petite", outfit=dict(top=["bandeau", "capelet"], bottom="sarong", extras=["usekh", "bell_collar"],
                                    colors=dict(cloth2="#5a3a8a", leather="#b8903a")),
        ears="big", hair="pixie", bangs="messy", stockings=False, tip="light",
        pal=dict(SKIN=skin("fair"), HAIR="#2a4ac8", FUR="#2e50d0", LIGHT="#c8dcff", MARK="#14246a", EYE="#d89a3a", CLOTH="#6a3ea8", TRIM="#e0b84a",
                 facemark="fox"),
        variants={"sand": dict(FUR="#f0d8a8", HAIR="#f4e2bc", LIGHT="#fffaf0", MARK="#8a6a40", EYE="#3a8ab8", CLOTH="#d8b878", TRIM="#3a8ab8",
                               extra=dict(cloth2="#e8d8b0")),
                  "red": dict(FUR="#d8864a", HAIR="#e09058", LIGHT="#fff0e0", MARK="#6a3018", CLOTH="#c8683a", extra=dict(cloth2="#e8c8a0")),
                  "cream": dict(FUR="#fbf2e2", HAIR="#fff8ee", LIGHT="#ffffff", MARK="#b89a78", CLOTH="#2a4ac8", EYE="#3a8ab8"),
                  "dusk": dict(FUR="#5a3a8a", HAIR="#6a4a9a", LIGHT="#e0d0ff", MARK="#2a1a4a", CLOTH="#1e2a5a", extra=dict(cloth2="#1e2a5a"))}),
    # twenty-seventh wave: the kitsune obaa, the old fox-wife of the tales: white hair and white tails, gold eyes, red berries and ribbons, a lilac kimono
    "obaa_kitsune": dict(
        build="curvy", outfit=dict(top=["kimono"], extras=["pearls"], colors=dict(kimono="#c8b8e8", kimono_trim="#f4f0f8", obi="#c82a2a")),
        ears="fox", hair="flowing", stockings=False, tip="light",
        pal=dict(SKIN=skin("fair"), HAIR="#f0eee8", FUR="#ece8e0", LIGHT="#ffffff", MARK="#a8a098", EYE="#e0a030", CLOTH="#c8b8e8", TRIM="#c82a2a",
                 facemark="fox"),
        variants={"gold": dict(FUR="#e8c870", HAIR="#f4e0a0", MARK="#a87a2a", EYE="#c82a2a"),
                  "black": dict(FUR="#2a2830", HAIR="#1a181e", LIGHT="#8a8890", MARK="#0a080e", EYE="#e8c040", CLOTH="#3a2a4a"),
                  "red": dict(FUR="#c8482a", HAIR="#e8d8c8", MARK="#7a2412", EYE="#e8c040", CLOTH="#f4ece0"),
                  "silver": dict(FUR="#a8acb8", HAIR="#d8dce4", MARK="#5a5e6a", EYE="#5a8ac8", CLOTH="#3a4a7a")}),
    # fifteenth wave: the huli jing, a silver-blue fox spirit with long black hair (nine tails, the game shows them all)
    "huli_jing_kitsune": dict(
        build="slim", outfit=dict(top=["kimono"], extras=["armlets"], colors=dict(kimono="#c83a3a", kimono_trim="#e8c050", obi="#1e1c22")),
        ears="fox", hair="hime", stockings=False, tip="light",
        pal=dict(SKIN=skin("fair"), HAIR="#141218", FUR="#8aa0c8", LIGHT="#f0f4fa", MARK="#3a4a6a", EYE="#c83a3a", CLOTH="#c83a3a", TRIM="#e8c050",
                 facemark="fox"),
        variants={"red": dict(FUR="#d8602a", LIGHT="#fff0e0", MARK="#7a2a10", HAIR="#3a1410", EYE="#e8c040"),
                  "white": dict(FUR="#f4f2ee", LIGHT="#ffffff", MARK="#c8c0b4", HAIR="#f8f6f0", EYE="#c83a3a"),
                  "gold": dict(FUR="#e0b040", LIGHT="#fff4c0", MARK="#8a5a10", HAIR="#3a2410", EYE="#c83a3a"),
                  "black": dict(FUR="#2a2830", LIGHT="#8a8890", MARK="#0a080e", HAIR="#0a080e", EYE="#e8c040",
                                extra=dict(kimono="#2a2830", kimono_trim="#c83a3a", obi="#c83a3a"))}),
    # fourteenth wave: the vulphea, a fire fox in flame colours, tails tipped with yellow fire
    "vulphea_kitsune": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], bottom="wrap_skirt", extras=["armlets"], colors=dict(cloth2="#3a1a14")),
        ears="fox", hair="flowing", stockings=False, tip="light",
        pal=dict(SKIN=skin("fair"), HAIR="#f0602a", FUR="#e8502a", LIGHT="#ffd860", MARK="#8a1e10", EYE="#ffd040", CLOTH="#3a1a14", TRIM="#ffb030",
                 facemark="fox"),
        variants={"gold": dict(HAIR="#f8b030", FUR="#f0a020", LIGHT="#fff0a0", MARK="#a85a10"),
                  "blue": dict(HAIR="#3a8af0", FUR="#2a7ae8", LIGHT="#c8f0ff", MARK="#14306a", EYE="#c8f0ff", CLOTH="#14182a"),
                  "violet": dict(HAIR="#a04af0", FUR="#9038e0", LIGHT="#f0c8ff", MARK="#40146a", EYE="#f0c8ff"),
                  "white": dict(HAIR="#fff4e0", FUR="#fff0d8", LIGHT="#ffffff", MARK="#e8a040", EYE="#e85a2a", CLOTH="#a83a2a")}),
    # thirteenth wave: the wattlefox, a blue fox wreathed in golden wattle blossom
    "wattlefox_kitsune": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], bottom="wrap_skirt", extras=["pearls"], colors=dict(cloth2="#e8c040")),
        ears="fox", hair="wavy", stockings=False, tip="light", wattle=True,
        pal=dict(SKIN=skin("fair"), HAIR="#4a7ae8", FUR="#3a6ad8", LIGHT="#e8f0ff", MARK="#1e3a8a", EYE="#e8c040", CLOTH="#2a5a3a", TRIM="#e8c040",
                 facemark="fox", extra=dict(bloom="#f8d030", leaf="#5a8a4a")),
        variants={"teal": dict(HAIR="#2a9a9a", FUR="#2a8a8a", MARK="#145a5a", extra=dict(bloom="#f8d030", leaf="#5a8a4a")),
                  "violet": dict(HAIR="#8a5ad8", FUR="#7a4ac8", MARK="#3a1e6a", extra=dict(bloom="#f8e070", leaf="#5a8a4a")),
                  "golden": dict(HAIR="#e8b040", FUR="#d8a030", MARK="#7a4a10", LIGHT="#fff4d8", EYE="#3a7ac8", CLOTH="#3a5a8a",
                                 extra=dict(bloom="#fff070", leaf="#4a7a3a")),
                  "grey": dict(HAIR="#8a8e98", FUR="#7a7e88", MARK="#3a3e48", extra=dict(bloom="#f8d030", leaf="#5a8a4a"))}),
    "red_kitsune": dict(
        build="standard", outfit=dict(top=["kimono"], bottom="hakama", colors=dict(kimono="#f4f0ea", kimono_trim="#c8352a", cloth2="#b8302a", obi="#b8302a")),
        ears="fox", hair="hime", stockings=True, tip="light",
        pal=dict(SKIN=skin("fair"), HAIR="#c8582a", FUR="#d2642a", LIGHT="#f6f0e8", MARK="#2a1c18", EYE="#d8a030", CLOTH="#b8302a", TRIM="#e8c060",
                 facemark="fox"),
        variants={"silver": dict(FUR="#34343a", HAIR="#c8cad2", LIGHT="#eceef2", MARK="#141418", EYE="#c8b040"),
                  "cross": dict(FUR="#c86a30", HAIR="#b8602e", pattern="cross", MARK="#3a2a22"),
                  "platinum": dict(FUR="#dcdce0", HAIR="#ececf0", LIGHT="#ffffff", MARK="#8a8a92", CLOTH="#6a5aa0"),
                  "amber": dict(FUR="#e0a868", HAIR="#e4b070", MARK="#6a4a30", CLOTH="#a04a6a"),
                  # homage (2026-10-30): Ninetales: cream fur and hair, red eyes, nine tails (Kitsune shows nine)
                  "nine_cream": dict(FUR="#f0e4c0", HAIR="#f4e8c8", LIGHT="#fff8e8", MARK="#e8c888", EYE="#c82a2a", CLOTH="#e8a040", TRIM="#f8e8c0"),
                  # homage (2026-10-31): Renamon (Digimon): golden fur, a white front, long purple gloves' colour on her clothes
                  "gold_ninja": dict(FUR="#e8c030", HAIR="#f0d040", LIGHT="#ffffff", MARK="#8a6a14", EYE="#3a7ac8", CLOTH="#6a3a9a", TRIM="#f4f4f6")}),
    "arctic_kitsune": dict(
        build="petite", outfit=dict(top=["halter", "capelet"], bottom="wrap_skirt", extras=["pearls"], colors=dict(cloth2="#dfe8f0")),
        ears="round", hair="shaggy", stockings=False, tip="light",
        pal=dict(SKIN=skin("pale"), HAIR="#eef0f6", FUR="#f2f4f8", LIGHT="#ffffff", MARK="#c4c8d2", EYE="#5a3e28", CLOTH="#4a78b0", TRIM="#cfe0f0",
                 facemark="fox"),
        variants={"blue": dict(FUR="#5a606e", HAIR="#6a7080", LIGHT="#9aa0ac", MARK="#3a3e48", CLOTH="#2a3a5a"),
                  "summer": dict(FUR="#8a6a4a", HAIR="#9a7a5a", LIGHT="#e8dcc8", MARK="#5a4430", CLOTH="#4a6a3a"),
                  "sable": dict(FUR="#5a3a2a", HAIR="#6a4836", LIGHT="#c8b4a0", MARK="#2a1a12", CLOTH="#7a2a3a"),
                  "pearl": dict(FUR="#ece4d8", HAIR="#f2ebe0", MARK="#a8a4a0", CLOTH="#8a6aa8")}),
    "fennec_kitsune": dict(
        build="slim", outfit=dict(top=["bandeau", "vest"], bottom="shorts", extras=["pouch"], colors=dict(leather="#b07a48")),
        ears="big", hair="twintails", stockings=False, tip="dark", goggles_kit=True,
        pal=dict(SKIN=skin("tan"), HAIR="#f0dcb8", FUR="#ecd2a4", LIGHT="#fbf6ec", MARK="#7a5a3a", EYE="#2a1e18", CLOTH="#d0703a", TRIM="#f0d090",
                 facemark="fox"),
        variants={"sandy": dict(FUR="#e0bc84", HAIR="#e6c48e", MARK="#6a4a2a"),
                  "pale": dict(FUR="#f6eee0", HAIR="#faf4ea", MARK="#a89070", CLOTH="#6a8ab0"),
                  "redsand": dict(FUR="#d88a50", HAIR="#d48450", MARK="#5a3020", CLOTH="#3a6a6a"),
                  "ash": dict(FUR="#cfcac2", HAIR="#d8d4cc", LIGHT="#f2f0ec", MARK="#5a5550", CLOTH="#8a4a4a"),
                  # homage (2026-10-30): Tails (Sonic): orange and white, blue eyes, two tails (Kitsune shows two), goggles on her head
                  "twin_tails": dict(SKIN=skin("fair"), FUR="#f0902a", HAIR="#f0902a", LIGHT="#fff8f0", MARK="#fff4ea", EYE="#2a6ad8", CLOTH="#d82a2a",
                                     TRIM="#f8f8f8", extra=dict(goggles=True))}),
    "corsac_kitsune": dict(
        build="athletic", outfit=dict(top=["bandeau", "harness"], bottom="loincloth", extras=["bracers"]),
        ears="fox", hair="braid", stockings=False, tip="dark",
        pal=dict(SKIN=skin("peach"), HAIR="#b8a890", FUR="#b4a690", LIGHT="#f0ece4", MARK="#5e544a", EYE="#c89a3a", CLOTH="#4a6a8a", TRIM="#d8c8a0",
                 facemark="fox"),
        variants={"winter": dict(FUR="#d8d0c4", HAIR="#e0d8cc", LIGHT="#ffffff"),
                  "rusty": dict(FUR="#b8906a", HAIR="#b0845a", CLOTH="#5a4a7a"),
                  "smoky": dict(FUR="#7e7a76", HAIR="#8a8682", LIGHT="#cfcbc6", MARK="#3a3632", CLOTH="#6a3a3a"),
                  "golden": dict(FUR="#d0b07a", HAIR="#d6b680", CLOTH="#3a5a4a")}),
    "gray_kitsune": dict(
        build="curvy", outfit=dict(top=["halter", "corset"], bottom="sarong", extras=["armlets"], colors=dict(cloth2="#3a3440")),
        ears="fox", hair="sidetail", stockings=False, tip="dark",
        pal=dict(SKIN=skin("fair"), HAIR="#8a8a8e", FUR="#8e8c8c", LIGHT="#f2eee8", MARK="#1e1c1c", EYE="#c89a3a", CLOTH="#7a3a2a", TRIM="#c8a070",
                 pattern="speckle", facemark="fox", extra=dict(rust="#b0583a")),
        variants={"island": dict(FUR="#a07058", HAIR="#9a6a52", extra=dict(rust="#c8683a")),
                  "dark": dict(FUR="#4a4a4e", HAIR="#56565c", LIGHT="#bdb8b2", extra=dict(rust="#7a3a2a"), CLOTH="#3a3a5a"),
                  "pale": dict(FUR="#bcbcbe", HAIR="#c8c8cc", extra=dict(rust="#c88a6a")),
                  "rufous": dict(FUR="#8e8a88", HAIR="#9a6a52", extra=dict(rust="#d0602a"), CLOTH="#2a4a3a")}),
}

LEG_PARTS = [("thigh", 2.25, 4.75, 2.25), ("shin", 1.75, 6.75, 1.75), ("paw", 2.25, 1.25, 3),
             ("ftail_a", 1.5, 1.5, 3), ("ftail_b", 3, 3, 6.5), ("ftail_c", 2.5, 2.5, 2.5)]
WHITE_TOP = shades("#f4f0ea")


def leg_parts(build):
    """the legs follow the body build"""
    t = G.OF.thigh_width(build, 2.25)
    return [("thigh", t, 4.75, t), ("shin", t - 0.5, 6.75, t - 0.5), ("paw", max(2.25, t), 1.25, 3)] + LEG_PARTS[3:]


def declare(sp):
    extra = [("wattle", 1.25, 1.25, 1.25), ("wattle_leaf", .5, .25, 2.5)] if SPECIES[sp].get("wattle") else []
    if SPECIES[sp].get("goggles_kit"):                 # Tails's goggles, on the twin_tails look only
        extra += [("goggle_band", 9.25, 1, 9.25), ("goggle_lens", 2.25, 2, .75)]
    G.declare(leg_parts(SPECIES[sp].get("build")) + extra, ears=SPECIES[sp]["ears"], hair=SPECIES[sp]["hair"],
              build=SPECIES[sp].get("build"), outfit=SPECIES[sp].get("outfit"))


def paint(sp, key):
    d = SPECIES[sp]
    pal = G.pal_from(d["pal"], d["variants"].get(key) if key else None, pupil=(1.0, 3.3))
    pt = H.Painter()
    G.paint_upper(pt, pal, top="wrap", outfit=d.get("outfit"), build=d.get("build"))
    top = G.cloth_fn(G.SimpleNamespace(CLOTH=WHITE_TOP, TRIM=pal.CLOTH), 0.62, 1)   # the shrine maiden's white top, coloured trim
    if not d.get("outfit"):
        pt("chest", "all", top)
        pt("bust", "all", G.cloth_fn(G.SimpleNamespace(CLOTH=WHITE_TOP, TRIM=pal.CLOTH), 0.66, 0))
    G.paint_ears(pt, pal, G.fur_fn(pal, pattern="solid"))
    pt("ear_a", ("south",), lambda f, x, y, w, h: pick(pal.MARK if d["tip"] == "dark" or d["stockings"] else pal.FUR, 0.4, x, y))
    fur = G.fur_fn(pal, light_faces=("down",), light_below=0.7)
    skin_t = H.skin_fn(pal, 0.68)
    rust = pal.extra.get("rust")

    def thigh(f, x, y, w, h):
        edge = h * 0.45 + (noise(x, 0, 5) - 0.5) * 3
        return skin_t(f, x, y, w, h) if y < edge else fur(f, x, y, w, h)
    pt("thigh", "all", thigh)
    if d["stockings"]:                                       # black "stockings", like a real red fox
        pt("shin", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.45 + (noise(x, y, 8) - 0.5) * 0.2, x, y) if y > h * 0.25 else fur(f, x, y, w, h))
    elif rust:
        pt("shin", "all", lambda f, x, y, w, h: pick(G.shades(rust), 0.55, x, y) if f in ("east", "west") and y > h * 0.3 else fur(f, x, y, w, h))
    else:
        pt("shin", "all", fur)
    pt("paw", "all", lambda f, x, y, w, h: pick(pal.MARK if d["stockings"] else pal.FUR, 0.5 + (noise(x, y, 7) - 0.5) * 0.14, x, y)
       if f != "down" else pick(pal.INNER, 0.45, x, y))
    tf = G.fur_fn(pal, light_faces=("down",), seed=21, fluffy=False)
    pt("ftail_a", "all", tf)

    def bushy(f, x, y, w, h):
        c = tf(f, x, y, w, h)
        if rust and f in ("east", "west") and noise(x, y, 4) > 0.3:
            c = pick(G.shades(rust), 0.5, x, y)
        if f == "up" and abs(x - w / 2) < 1.5 and d["tip"] == "dark":
            c = pick(pal.MARK, 0.35, x, y)                       # the gray fox's black stripe along the tail
        return c
    pt("ftail_b", "all", bushy)
    tip = pal.LIGHT if d["tip"] == "light" else pal.MARK
    pt("ftail_c", "all", lambda f, x, y, w, h: pick(tip, 0.6 + (noise(x, y, 9) - 0.5) * 0.2, x, y))
    bloom, leaf = shades(pal.extra.get("bloom", "#f8d030")), shades(pal.extra.get("leaf", "#5a8a4a"))
    pt("wattle", "all", lambda f, x, y, w, h: pick(bloom, 0.55 + 0.3 * ((x + y) % 2), x, y))     # fluffy golden balls of blossom
    pt("wattle_leaf", "all", lambda f, x, y, w, h: pick(leaf, 0.5 + 0.15 * (y % 2), x, y))
    paint_goggles(pt, pal)
    return pt.T


def paint_goggles(pt, pal):
    """flight goggles pushed up on her head (pal.extra goggles), a brown strap and two round glass lenses; clear otherwise"""
    on = pal.extra.get("goggles")
    strap, rim = shades("#6a4a2a"), shades("#c8c8cc")
    pt("goggle_band", "all", lambda f, x, y, w, h: H.CLEAR if not on or f in ("up", "down") else pick(strap, 0.45, x, y))
    pt("goggle_lens", "all", lambda f, x, y, w, h: H.CLEAR if not on else pick(shades("#5ac8f0"), 0.7 + 0.2 * (x + y < 2), x, y)
       if f == "north" and 0 < x < w - 1 and 0 < y < h - 1 else pick(rim, 0.6, x, y))


def build(sp):
    d = SPECIES[sp]
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    g = G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), build=d.get("build"), outfit=d.get("outfit"), ears=d["ears"], skirt=True))
    G.legs_biped(r, g, d)
    G.OF.dress_legs(r, d.get("outfit"))
    G.fox_tails(r, d, 9)
    if d.get("goggles_kit"):                             # the goggles' strap round her head above the brow, the lenses up front
        hy, z = g.hy, g.z
        r.add("hair_goggles", "head", [0, hy + 8, z], [G.rot_cube("goggle_band", [-4.625, hy + 8.0, -4.625 + z], [0, hy + 8.5, z], -12, 0, 0),
                                                      G.rot_cube("goggle_lens", [-3.4, hy + 8.4, -5.6 + z], [0, hy + 8.5, z], -12, 0, 0),
                                                      G.rot_cube("goggle_lens", [1.15, hy + 8.4, -5.6 + z], [0, hy + 8.5, z], -12, 0, 0)])
    if d.get("wattle"):                                  # a wreath of wattle round her head
        hy, z = g.hy, g.z
        cs = []
        for i in range(9):
            a = math.radians(i * 40)
            x, zz = 4.4 * math.sin(a), 4.4 * math.cos(a)
            cs.append(cube("wattle", [x - 0.625, hy + 7.4 + 0.4 * (i % 2), zz - 0.625 + z]))
            if i % 2 == 0:
                cs.append(G.rot_cube("wattle_leaf", [x - 0.25, hy + 7.6, zz + z], [x, hy + 7.6, zz + z], 0, i * 40 + 90, 0))
        r.add("wreath", "head", [0, hy + 7.6, z], cs)
    return r.model(sp)


TAILS = [f"tail_{i}" for i in range(1, 10)]


def anims(an):
    G.biped_anims(an, TAILS, fox=True)


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.25, -30, 8), ("run", 0.15, -60, 5), ("sit", 0, -30, 10), ("dig", 0.2, -60, 8)]
FACE = [("idle", 0, 0, 0, 24), ("idle", 2.75, 0, 0, 24), ("laugh", 0.3, 0, 0, 24), ("idle", 0, -30, 8, 24)]

# homage looks of the twenty-eighth wave (POP_CULTURE_VARIANTS.md)
SPECIES["fennec_kitsune"]["variants"]["first_fox"] = dict(HAIR="#a8693a", FUR="#b8784a", LIGHT="#f8ecd4", MARK="#7a4a24", EYE="#2a1a10", CLOTH="#f4e8d0", TRIM="#a8693a")


if __name__ == "__main__":
    G.run_family("kitsune", SPECIES, declare, paint, build, anims, VIEWS, FACE)
