#!/usr/bin/env python3
"""Merfolk (the sea family, see merfolk_field_guide.html): orca, koi, shark, seal, axolotl, dolphin.

    python tools/gen_merfolk.py [species ...] [--previews]

Shared girl_lib upper body on a fish / sea-mammal tail: fin_1 down from the waist, fin_2..fin_4 along the ground and a
fluke (horizontal for the orca, seal and axolotl, a vertical tail fin for the koi and shark). Extras: a dorsal fin
(orca, shark, dolphin, axolotl crest), long trailing fins (koi), fin ears (koi, shark, orca, dolphin), gill fronds
(axolotl). Every model has a `seashells` bone (two scallop shells over the bust) that the game shows once she has found
shells on the sea floor. Five colours per species. The ahuizotl (a river otter) has fur, round ears and a hand at the
end of her tail instead of a fluke (`hand`), for stealing fish off fishing lines.
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
    "ahuizotl_mermaid": dict(
        build="athletic", outfit=dict(top=["sports"], extras=["armlets"], colors=dict(cloth2="#2a6a6a")),
        ears="round", hair="shaggy", bangs="messy", fluke=(3, .75, 3.5), hand=True, size=0.95,
        pal=dict(SKIN=skin("tan"), HAIR="#5a3e2a", FUR="#6a4a32", LIGHT="#c8a888", MARK="#3a2818", EYE="#2a1a10", CLOTH="#2a8a8a", TRIM="#e8c060",
                 pattern="solid", extra=dict(belly_white=True)),
        variants={"dark": dict(FUR="#2e221a", HAIR="#1e1610", LIGHT="#7a6450", MARK="#14100c"),
                  "golden": dict(FUR="#a8783a", HAIR="#8a5a2a", LIGHT="#e8c890", MARK="#5a3a1a", CLOTH="#3a5a9a"),
                  "grey": dict(FUR="#6a6460", HAIR="#4a4440", LIGHT="#c8c0b8", MARK="#2e2a28", CLOTH="#9a3a3a"),
                  "jade": dict(FUR="#4a5a3a", HAIR="#2e3a22", LIGHT="#a8b888", MARK="#1e2614", CLOTH="#c8a040")}),
    "dugong_mermaid": dict(
        build="curvy", outfit=dict(top=["bandeau"], extras=["pearls"]),
        ears=None, hair="wavy", bangs="swept", fluke=(9, .75, 3.5), size=1.15,
        pal=dict(SKIN=skin("fair"), HAIR="#5a4a42", FUR="#9a9a9c", LIGHT="#d8d4d0", MARK="#7a7a7e", EYE="#3a3a4a", CLOTH="#3a9a6a", TRIM="#e8e0c8",
                 pattern="solid"),
        variants={"brown": dict(FUR="#8a7a6a", HAIR="#7a6a5a", MARK="#6a5a4a"),
                  "pale": dict(FUR="#e0d8cc", HAIR="#ece4d8", LIGHT="#ffffff", MARK="#c8c0b4"),
                  "slate": dict(FUR="#5a6068", HAIR="#4a5058", MARK="#3a4048", CLOTH="#c8a040"),
                  "sandy": dict(FUR="#c8b490", HAIR="#b8a07a", MARK="#a08a64", CLOTH="#2a7a8a")}),
    # thirteenth wave: the rust otter, a dark otter hung with copper charms
    "rust_otter_mermaid": dict(
        build="athletic", outfit=dict(top=["sports"], extras=["armlets", "belt"], colors=dict(cloth2="#5a3a22", leather="#7a4a24")),
        ears="round", hair="shaggy", bangs="messy", fluke=(3.5, .75, 4), charms=True, size=0.95,
        pal=dict(SKIN=skin("tan"), HAIR="#2e2620", FUR="#3a2e26", LIGHT="#9a8470", MARK="#1e1814", EYE="#d8803a", CLOTH="#b8642a", TRIM="#d8803a",
                 pattern="solid", extra=dict(belly_white=True, copper="#d8803a")),
        variants={"brown": dict(HAIR="#5a3a22", FUR="#6a4a2e", LIGHT="#c8a880", MARK="#3a2414"),
                  "grey": dict(HAIR="#4a4644", FUR="#5a5652", LIGHT="#b8b2aa", MARK="#2a2826", CLOTH="#5aa890", TRIM="#7ac8b0",
                               extra=dict(belly_white=True, copper="#5aa890")),
                  "copper": dict(HAIR="#8a3e1a", FUR="#a8542a", LIGHT="#e8b890", MARK="#5a2410", CLOTH="#3a5a7a"),
                  "pale": dict(HAIR="#c8b8a4", FUR="#d8c8b4", LIGHT="#f4ece0", MARK="#8a7a68", EYE="#3a8a9a")}),
    # twelfth wave: the platypus, fur, a duck-billed cap and a flat beaver tail
    "platypus_mermaid": dict(
        build="petite", outfit=dict(top=["sports"], extras=["armlets"], colors=dict(cloth2="#3a6a8a")),
        ears=None, hair="bob", bangs="blunt", fluke=(5.5, .75, 6), platypus=True, size=0.95,
        pal=dict(SKIN=skin("tan"), HAIR="#5a3a22", FUR="#6a4a2e", LIGHT="#c8a880", MARK="#3a2414", EYE="#1a1410", CLOTH="#3a8aa8", TRIM="#e8c060",
                 pattern="solid", extra=dict(belly_white=True, bill="#3a3438")),
        variants={"dark": dict(HAIR="#2e2014", FUR="#3a2a1c", LIGHT="#8a6a50", MARK="#1e140c"),
                  "golden": dict(HAIR="#a8783a", FUR="#b8884a", LIGHT="#f0d8a8", MARK="#6a4a1e", CLOTH="#3a5a2a"),
                  "grey": dict(HAIR="#5a5652", FUR="#6e6a66", LIGHT="#c8c4c0", MARK="#3a3836", CLOTH="#a84a4a"),
                  "pale": dict(HAIR="#c8b090", FUR="#d8c0a0", LIGHT="#f8ece0", MARK="#9a8064", CLOTH="#5a7ac8", extra=dict(belly_white=True, bill="#8a7a7a"))}),
    # twelfth wave: the moonseal, a night-blue seal with crescent moons on her tail (they glow) and little antlers
    "moonseal_mermaid": dict(
        build="curvy", outfit=dict(top=["bandeau"], extras=["pearls"], colors=dict(cloth2="#e8f0f8")),
        ears=None, hair="wavy", bangs="swept", fluke=(7, .75, 3.5), seal=True, moon=True, glow=["crescent"], glow_boost=1.5,
        pal=dict(SKIN=skin("pale"), HAIR="#e8eef8", FUR="#2a3a8a", LIGHT="#e8eef8", MARK="#14204a", EYE="#5ab8f0", CLOTH="#e8eef8", TRIM="#5ab8f0",
                 pattern="solid", extra=dict(belly_white=True, moon="#f4f8ff", antler="#e8e0d0")),
        variants={"purple": dict(HAIR="#f0e8f8", FUR="#5a3a8a", MARK="#2a1a4a", EYE="#c88af0", TRIM="#c88af0", extra=dict(belly_white=True, moon="#f8f0ff", antler="#e8e0d0")),
                  "green": dict(HAIR="#e8f8f0", FUR="#2a6a6a", MARK="#143a3a", EYE="#5af0c8", TRIM="#5af0c8", extra=dict(belly_white=True, moon="#f0fff8", antler="#e8e0d0")),
                  "silver": dict(HAIR="#f4f4f8", FUR="#8a90a0", MARK="#4a505e", EYE="#5a8ad8", TRIM="#5a8ad8", extra=dict(belly_white=True, moon="#ffffff", antler="#f4f0e8")),
                  "night": dict(HAIR="#1e1e2a", FUR="#1a1a26", MARK="#0a0a12", LIGHT="#5a5a7a", EYE="#f8d040", TRIM="#f8d040", CLOTH="#2a2a3a",
                                extra=dict(belly_white=True, moon="#f8d860", antler="#c8b890"))}),
    # eleventh wave: the mantarrow, a manta ray: wide wing fins from her hips, a thin whip of a tail
    "mantarrow_mermaid": dict(
        build="slim", outfit=dict(top=["halter"], extras=["armlets"], colors=dict(cloth2="#1e2a4a")),
        ears=None, hair="flowing", bangs="parted", fluke=(1, .75, 8), manta=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e2a5a", FUR="#2a3a7a", LIGHT="#e8eef8", MARK="#14204a", EYE="#4ac8f0", CLOTH="#e8eef8", TRIM="#4ac8f0",
                 pattern="solid", extra=dict(belly_white=True)),
        variants={"black": dict(HAIR="#141418", FUR="#1e1e24", MARK="#0a0a0e", LIGHT="#d8d8e0", CLOTH="#3a3a46"),
                  "spotted": dict(HAIR="#141418", FUR="#1e1e24", MARK="#f4f4f6", LIGHT="#f4f4f6", pattern="spots", CLOTH="#e8e8f0"),
                  "teal": dict(HAIR="#1a5a6a", FUR="#2a7a8a", MARK="#0e3a44", CLOTH="#f8e8c0"),
                  "violet": dict(HAIR="#3a2a6a", FUR="#5a3a8a", MARK="#2a1a44", CLOTH="#f0d8f8", EYE="#f8a0e0")}),
    # tenth wave: the axolume, a glowing axolotl of the lush caves: her gills, crest and fluke glow (glow masks)
    "axolume_mermaid": dict(
        build="petite", outfit=dict(top=["halter"], extras=["pearls"], colors=dict(cloth2="#1e2a6a")),
        ears="gill", hair="odango", fluke=(4.5, .5, 3), dorsal=(.5, 2.5, 9), axolotl=True, glow=["ear_a", "dorsal", "fluke"],
        pal=dict(SKIN="#a8c8f0", HAIR="#3a5ad8", FUR="#3a5ad8", LIGHT="#a8d8ff", MARK="#7af0ff", EYE="#1a1a3e", CLOTH="#1e2a6a", TRIM="#7af0ff",
                 pattern="spots", INNER="#7af0ff"),
        variants={"rose": dict(SKIN="#f0c0d8", HAIR="#d84a9a", FUR="#d84a9a", LIGHT="#f8c8e8", MARK="#ffb0f0", INNER="#ffb0f0", TRIM="#ffb0f0"),
                  "gold": dict(SKIN="#f4e0b0", HAIR="#d8a030", FUR="#d8a030", LIGHT="#f8e8a8", MARK="#fff0a0", INNER="#fff0a0", TRIM="#fff0a0"),
                  "jade": dict(SKIN="#b8e8c8", HAIR="#2a9a6a", FUR="#2a9a6a", LIGHT="#a8f0c8", MARK="#9affc8", INNER="#9affc8", TRIM="#9affc8"),
                  "void": dict(SKIN="#8a7aa8", HAIR="#1e1830", FUR="#1e1830", LIGHT="#4a3a6a", MARK="#c88aff", INNER="#c88aff", TRIM="#c88aff",
                               CLOTH="#2a1e3a")}),
    # ninth wave: the siren, a scaled tail with long trailing fins, fin ears and flowing hair
    "siren_mermaid": dict(
        build="curvy", outfit=dict(top=["halter"], extras=["pearls", "armlets"], colors=dict(cloth2="#2a6a7a")),
        ears="fin", hair="flowing", bangs="wispy", fluke=(.5, 6.5, 5.5), long_fins=True, scales=True, size=1.05,
        pal=dict(SKIN=skin("pale"), HAIR="#3ab8b0", FUR="#2a9a98", LIGHT="#b8f0e8", MARK="#1a6a70", EYE="#e8d040", CLOTH="#e8f4f0", TRIM="#e8d8a0",
                 pattern="solid"),
        variants={"pearl": dict(HAIR="#f4f4f8", FUR="#d8dce4", LIGHT="#ffffff", MARK="#a8b0c0", EYE="#8a6ad8", CLOTH="#c8a8e8"),
                  "coral": dict(HAIR="#f87a6a", FUR="#f0907a", LIGHT="#fcd8c8", MARK="#c84a4a", EYE="#3ab8b0", CLOTH="#f8e8c8"),
                  "abyss": dict(SKIN=skin("fair"), HAIR="#1a2a6a", FUR="#1e3a7a", LIGHT="#6a8ad8", MARK="#0e1a4a", EYE="#8af0f8", CLOTH="#2a2a4a"),
                  "storm": dict(HAIR="#8a8e98", FUR="#6a707a", LIGHT="#c8ccd4", MARK="#3a3e48", EYE="#e8e8f8", CLOTH="#3a4a5a")}),
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
    if d.get("manta"):
        parts.append(("manta_fin", 7, 6, .5))
    if d.get("platypus"):
        parts += [("pcap", 9.5, 3, 9.5), ("bill", 6, 1, 4.5)]
    if d.get("charms"):
        parts += [("charm", 1.25, 1.5, .25), ("charm_cord", 4.5, .25, .25)]
    if d.get("moon"):
        parts += [("crescent", 3.5, 3.5, .25), ("antler_a", .5, 3, .5), ("antler_b", .5, 1.5, .5)]
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
    body = sc if sp.startswith("koi") or d.get("scales") else smooth                 # the koi has scales; the mammals and the shark are smooth
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
    if d.get("hand"):                                             # the ahuizotl's tail ends in a hand: skin, with fingers
        pt("fluke", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") and y >= h * 0.55 and x % 3 == 2 else skin_t(f, x, y, w, h))
    pt("dorsal", "all", fin)
    pt("manta_fin", "all", lambda f, x, y, w, h: pick(pal.LIGHT if f == "north" else pal.FUR, 0.5 + 0.15 * (y == 0) - 0.1 * (x in (0, w - 1)), x, y)
       if y < h - (abs(x + 0.5 - w / 2) * 2 // 3) else CLEAR)
    pt("ribbon", "all", lambda f, x, y, w, h: G.with_alpha(pick(pal.MARK, 0.55 + 0.25 * (y % 3 == 0), x, y), 220) if y < h - 1 or x % 2 else G.CLEAR)
    pt("flipper", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.35 + 0.2 * (x % 4 == 0), x, y))
    cu = shades(pal.extra.get("copper", "#d8803a"))
    pt("charm", "all", lambda f, x, y, w, h: pick(cu, 0.5 + 0.3 * (x == 1 and y == 1) - 0.2 * (y == h - 1), x, y))
    pt("charm_cord", "all", lambda f, x, y, w, h: pick(shades("#5a3a22"), 0.45, x, y))
    bill = shades(pal.extra.get("bill", "#3a3438"))
    pt("pcap", "all", lambda f, x, y, w, h: CLEAR if f == "down" else pick(pal.MARK, 0.55 + (noise(x, y, 71) - 0.5) * 0.12, x, y))
    pt("bill", "all", lambda f, x, y, w, h: pick(bill, 0.45 + 0.1 * (f == "up") + 0.15 * (f == "up" and y == h - 2 and x in (w // 2 - 2, w // 2 + 1)), x, y))
    moon = shades(pal.extra.get("moon", "#f4f8ff"))

    def crescent(f, x, y, w, h):                                  # a crescent moon: a disc with a bite out of it
        if f not in ("north", "south"):
            return CLEAR
        cx, cy, rr = w / 2, h / 2, w / 2
        inside = (x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2 < rr * rr
        bite = (x + 0.5 - cx - rr * 0.45) ** 2 + (y + 0.5 - cy + rr * 0.2) ** 2 < (rr * 0.8) ** 2
        return pick(moon, 0.75, x, y) if inside and not bite else CLEAR
    pt("crescent", "all", crescent)
    ant = shades(pal.extra.get("antler", "#e8e0d0"))
    pt("antler_a", "all", lambda f, x, y, w, h: pick(ant, 0.55 + 0.1 * (y % 2), x, y))
    pt("antler_b", "all", lambda f, x, y, w, h: pick(ant, 0.6, x, y))
    if pal.extra.get("gills"):
        pt("fin_1", ("east", "west"), lambda f, x, y, w, h: pick(pal.MARK, 0.25, x, y) if (y in (1, 3, 5) and 2 <= x < w - 2) else None)
    if pal.extra.get("spot"):                                     # tancho: one red spot on the head
        pt("hair_cap", ("up",), lambda f, x, y, w, h: pick(shades("#d8402a"), 0.55, x, y) if (x - w / 2) ** 2 + (y - h / 2) ** 2 < 30 else None)
    if d["ears"] == "round":
        G.paint_ears(pt, pal, G.fur_fn(pal, pattern="solid"))
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
    if d.get("manta"):                                   # the wing fins, swept out from her hips
        mw = G.S("manta_fin")[0]
        r.add("manta_r", "fin_1", [-2, 9, 0], [G.rot_cube("manta_fin", [-2 - mw, 4.5, -0.25], [-2, 9, 0], 0, 0, -12)], mirror=True)
    if d.get("charms"):                                  # copper charms on a cord round her neck, one at her hip
        fz = -G.S("chest")[2] / 2 - G.S("bust")[2] - 0.4
        r.put("body", cube("charm_cord", [-2.25, 18.6, fz]), cube("charm", [-1.9, 17.0, fz - 0.1]), cube("charm", [-0.625, 16.6, fz - 0.1]),
              cube("charm", [0.65, 17.0, fz - 0.1]), cube("charm", [-3.0, 11.6, -1.6]))
    if d.get("platypus"):                                # a furry cap with a duck bill for a peak
        hy = 12 + 8 + 0.0
        r.add("pcap", "head", [0, 28, 0], [cube("pcap", [-4.75, 26.2, -4.75]), G.rot_cube("bill", [-3, 25.9, -8.9], [0, 26.4, -4.6], 12, 0, 0)])
    if d.get("moon"):                                    # crescents down her tail (they glow), little antlers in her hair
        r.put("fin_1", cube("crescent", [-1.75, 3.6, -2.2]))
        r.put("fin_2", cube("crescent", [-1.75, 0.3, -1.0]))
        r.put("fin_3", cube("crescent", [-1.75, 0.1, 5.4]))
        for sg in (-1, 1):
            r.put("head", G.rot_cube("antler_a", [sg * 2.4 - 0.25, 28.0, -0.5], [sg * 2.4, 28.0, -0.25], -10, 0, -18 * sg),
                  G.rot_cube("antler_b", [sg * 2.9 - 0.25, 29.4, -0.5], [sg * 2.9, 29.4, -0.25], -10, 0, -60 * sg))
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
    import girl_family as F
    want = [a for a in sys.argv[1:] if not a.startswith("--")]
    for sp, d in SPECIES.items():                        # glow masks: the axolume's gills, crest and fluke
        if d.get("glow") and (not want or sp in want):
            declare(sp)
            for k in [None] + list(d["variants"]):
                F.write_glowmask(sp + ("_" + k if k else ""), paint(sp, k), d["glow"], d.get("glow_boost", 1.0))
