#!/usr/bin/env python3
"""Nekomata (the cat family, see nekomata_field_guide.html): serval, tiger, lioness, snow leopard, cheetah, lynx.

    python tools/gen_nekomata.py [species ...] [--previews]

One model per species on the shared girl_lib rig ("legs" lower body + a three-segment cat tail, and a hidden second
tail tail2_* that the game shows for a true nekomata), five colours each (<species>_<variant>.png; the first variant
is the default texture). Writes previews/<species>_preview*.png and previews/nekomata_roster.png with --previews.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_lib as G  # noqa: E402
import family_concepts as C  # noqa: E402
from girl_lib import H, cube, pick, noise, CLEAR, BROAD, shades, skin  # noqa: E402

FAMILY = C.family("nekomata")

# per species: body knobs, then the default palette and the four other colours as palette overrides
SPECIES = {
    "serval_nekomata": dict(
        build="slim", outfit=dict(top=["halter"], bottom="sarong", extras=["belt", "pouch"]),
        ears="big", hair="bob", tail=((1.25, 1.25, 3.5), (1.25, 1.25, 3.5), (1.25, 1.25, 2.5)), tail_pattern="bars",
        pal=dict(SKIN=skin("tan"), HAIR="#c98d3e", FUR="#d6a052", LIGHT="#f3e6c8", MARK="#2a1c12", EYE="#c8a83a", CLOTH="#5e7a3a", TRIM="#e0c070",
                 pattern="spots", facemark="tear+whisker", extra=dict(dark_tips=True)),
        variants={"servaline": dict(pattern="smallspots", FUR="#dcae62"),
                  "melanistic": dict(FUR="#2e2620", LIGHT="#5a4e44", MARK="#121010", HAIR="#3a3028", EYE="#d9c050", CLOTH="#7a3a3a"),
                  "pale": dict(FUR="#ead2a0", LIGHT="#faf2e2", MARK="#7a5a3a", HAIR="#e2c48e", CLOTH="#a08a5a"),
                  "king": dict(pattern="blotches", FUR="#d09a48")}),
    "tiger_nekomata": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps"], rope_belt=True),
        ears="round", hair="ponytail", tail=((1.75, 1.75, 4), (1.75, 1.75, 4), (1.75, 1.75, 3)), tail_pattern="bars",
        pal=dict(SKIN=skin("peach"), HAIR="#d8742a", FUR="#e0772a", LIGHT="#f6eee0", MARK="#1c1410", EYE="#d8b030", CLOTH="#2e4a2e", TRIM="#c8a050",
                 pattern="stripes", facemark="stripe", extra=dict(dark_tips=True)),
        variants={"siberian": dict(FUR="#e2a060", HAIR="#dca066", pattern="finestripes", CLOTH="#4a5a6a"),
                  "white": dict(FUR="#f2efe8", LIGHT="#ffffff", MARK="#3a2a24", HAIR="#ece6dc", EYE="#5aa0d8", CLOTH="#3a4a6a"),
                  "golden": dict(FUR="#f0b860", LIGHT="#fbf2dc", MARK="#a8642a", HAIR="#f2c070", CLOTH="#6a4a2a"),
                  "black": dict(MARK="#100c0a", pattern_scale=0.7, HAIR="#3a2418")}),
    "lioness_nekomata": dict(
        build="curvy", outfit=dict(top=["bandeau", "mantle"], bottom="strips", extras=["armlets", "belt"]),
        ears="round", hair="mane", tail=((1.25, 1.25, 4), (1.25, 1.25, 4), (1.75, 1.75, 2)), tail_pattern="solid", tuft=True,
        pal=dict(SKIN=skin("olive"), HAIR="#c49a5a", FUR="#c89a5c", LIGHT="#efe2c8", MARK="#3a2a1c", EYE="#c89838", CLOTH="#8a5a2e", TRIM="#e8d088",
                 pattern="solid", facemark="whisker", extra=dict(dark_tips=True)),
        variants={"desert": dict(FUR="#dcc08c", HAIR="#d8be90", LIGHT="#fbf4e6", CLOTH="#b0884a"),
                  "white": dict(FUR="#efe8da", HAIR="#f0eadc", LIGHT="#ffffff", EYE="#8ab8d8", MARK="#8a7a6a", CLOTH="#6a7a8a"),
                  "cave": dict(FUR="#8e8070", HAIR="#7e7060", MARK="#5a4e42", pattern="finestripes", pattern_scale=1.6, CLOTH="#4a4a3a"),
                  "atlas": dict(FUR="#9a6a3c", HAIR="#7a4e2a", MARK="#2a1c12", CLOTH="#5a2e1e")}),
    "snow_leopard_nekomata": dict(
        build="petite", outfit=dict(top=["sweater"], bottom="wrap_skirt", extras=["scarf"]),
        ears="round", hair="braid", tail=((2.25, 2.25, 4), (2.5, 2.5, 4.5), (2.75, 2.75, 3)), tail_pattern="rosettes",
        pal=dict(SKIN=skin("pale"), HAIR="#d8dadc", FUR="#c8c6c0", LIGHT="#f6f6f2", MARK="#3a3a3c", EYE="#8ab89a", CLOTH="#4a5a7a", TRIM="#d8e0e8",
                 pattern="rosettes", facemark="whisker", extra=dict(scarf="#c4473f")),
        variants={"cream": dict(FUR="#e2d2b2", HAIR="#ead8b8", MARK="#5a4430", CLOTH="#7a5a4a"),
                  "smoky": dict(FUR="#7a7a7e", HAIR="#8a8a90", LIGHT="#c8c8cc", MARK="#1e1e22", CLOTH="#2a3040"),
                  "silver": dict(FUR="#e8eaee", HAIR="#f2f4f8", MARK="#8a8c94", EYE="#7ab0d0"),
                  "golden": dict(FUR="#dcc898", HAIR="#e4cfa0", MARK="#5a5248", CLOTH="#6a5a3a")}),
    "cheetah_nekomata": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts"),
        ears="round", hair="pixie", tail=((1.25, 1.25, 4), (1.25, 1.25, 4), (1.25, 1.25, 3)), tail_pattern="spots",
        pal=dict(SKIN=skin("tan"), HAIR="#e0b260", FUR="#e2b25c", LIGHT="#f8f0dc", MARK="#1a1410", EYE="#b88a2a", CLOTH="#6a2e2e", TRIM="#e8c870",
                 pattern="spots", facemark="tear", extra=dict(dark_tips=True)),
        variants={"king": dict(pattern="blotches", HAIR="#d8a650"),
                  "woolly": dict(FUR="#efdcb0", HAIR="#f2e2bc", LIGHT="#fffaf0", MARK="#8a7458", pattern="smallspots", CLOTH="#8a8a6a"),
                  "desert": dict(FUR="#ecd2a0", HAIR="#e8cc98", pattern="smallspots", MARK="#4a3a2a"),
                  "red": dict(FUR="#e0a070", HAIR="#d88a5a", MARK="#8a3a22", CLOTH="#4a3a6a")}),
    "lynx_nekomata": dict(
        build="standard", outfit=dict(top=["bandeau", "capelet"], bottom="shorts", extras=["bell_collar"]),
        ears="tuft", hair="twintails", tail=((1.75, 1.75, 2.5), (1.75, 1.75, 0.75), (1.75, 1.75, 0.75)), tail_pattern="solid", bobtail=True,
        pal=dict(SKIN=skin("fair"), HAIR="#a07a5a", FUR="#a88468", LIGHT="#ece4d8", MARK="#2a2420", EYE="#b8a83a", CLOTH="#3a5a4a", TRIM="#c8b48a",
                 pattern="smallspots", facemark="whisker", extra=dict(dark_tips=True, cloth2="#6a4a2e", ribbon="#b03030")),
        variants={"canada": dict(FUR="#a8a6a4", HAIR="#a6a4a2", pattern="speckle", CLOTH="#4a4a5a"),
                  "iberian": dict(FUR="#c8a060", HAIR="#b88c50", pattern="spots", CLOTH="#6a3a2a"),
                  "bobcat": dict(FUR="#b07850", HAIR="#9a6440", pattern="spots", pattern_scale=0.8),
                  "silver": dict(FUR="#d4d6da", HAIR="#dcdee4", LIGHT="#ffffff", MARK="#4a4c52", CLOTH="#3a4a6a")}),
    # twelfth wave: the cheshire cat, ringed in two colours, with a huge grin that glows (glow mask) and stays when she fades
    "cheshire_nekomata": dict(grin=True, glow=["grin"],
        build="slim", outfit=dict(top=["halter", "vest"], bottom="shorts", extras=["scarf"], colors=dict(cloth2="#3a1e4a", scarf="#f8e070")),
        ears="cat", hair="shaggy", bangs="messy", tail=((1.5, 1.5, 4), (1.5, 1.5, 4), (1.5, 1.5, 3.5)), tail_pattern="bars",
        pal=dict(SKIN=skin("fair"), HAIR="#e86aa8", FUR="#f088b8", LIGHT="#fcd8e8", MARK="#7a3a9a", EYE="#e8e040", CLOTH="#4a2a6a", TRIM="#f8e070",
                 INNER="#f8b0c8", pattern="bars"),
        variants={"purple": dict(HAIR="#8a4ac8", FUR="#9a5ad8", LIGHT="#e0c8f8", MARK="#2a9a9a", EYE="#f8d040", CLOTH="#1e4a4a"),
                  "blue": dict(HAIR="#4a7ad8", FUR="#5a8ae8", LIGHT="#f0f4ff", MARK="#f4f6ff", EYE="#f8e040", CLOTH="#1e2a5a"),
                  "gold": dict(HAIR="#d8a030", FUR="#e8b040", LIGHT="#f8e8b8", MARK="#6a3a14", EYE="#3ac87a", CLOTH="#5a2a14"),
                  "black": dict(HAIR="#2a2230", FUR="#2e2634", LIGHT="#6a5a78", MARK="#8a4ad8", EYE="#e8e040", CLOTH="#1a1420")}),
    # eleventh wave: the waterlil, a big cat's head on a tiny body, riding her own lily pad, a small bloom on top
    # (lily pads grow under her in the game)
    "waterlil_nekomata": dict(chibi=True,
        build="petite", outfit=dict(top=["halter"], bottom="shorts", extras=["pearls"], colors=dict(cloth2="#2a5a3a")),
        ears="cat", hair="bob", tail=((1.25, 1.25, 3.5), (1.25, 1.25, 3.5), (1.25, 1.25, 2.5)), tail_pattern="solid", lotus=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1a1a1e", FUR="#1e1e22", LIGHT="#4a4a50", MARK="#0e0e10", EYE="#4ad8d0", CLOTH="#3a8a4a", TRIM="#f8c0d8",
                 INNER="#f0a8b8", pattern="solid", extra=dict(lotus="#f8b0cc", lotus_in="#fff0a0")),
        variants={"white": dict(FUR="#f4f2ee", HAIR="#fbf8f4", LIGHT="#ffffff", MARK="#c8c4be", EYE="#6aa8f0", extra=dict(lotus="#f8f8f0", lotus_in="#f8e070")),
                  "calico": dict(FUR="#f4f0e8", HAIR="#c8783a", MARK="#2a2420", pattern="patches", EYE="#d8a030",
                                 extra=dict(lotus="#f8d850", lotus_in="#f88a2a")),
                  "grey": dict(FUR="#7a7a80", HAIR="#5a5a62", LIGHT="#b8b8be", EYE="#e8c050", extra=dict(lotus="#8ab0f0", lotus_in="#f8f0a0")),
                  "ginger": dict(FUR="#d8843a", HAIR="#b8602a", LIGHT="#f4d8b0", MARK="#8a4418", EYE="#4aa86a", extra=dict(lotus="#e84a4a", lotus_in="#f8d050"))}),
    # tenth wave: the blaze cat of the Nether, black with flames for paws and a burning tail tip (glow masks)
    "blaze_cat_nekomata": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], bottom="shorts", extras=["armlets"], colors=dict(cloth2="#1e1a1e")),
        ears="cat", hair="shaggy", tail=((1.25, 1.25, 3.5), (1.25, 1.25, 3.5), (1.5, 1.5, 2.5)), tail_pattern="solid", flames=True,
        glow=["flame", "flame_hand", "tail_c"],
        pal=dict(SKIN=skin("fair"), HAIR="#1a1618", FUR="#1e1a1c", LIGHT="#4a4246", MARK="#0e0c0e", EYE="#f0a020", CLOTH="#1e1a1e", TRIM="#f07a20",
                 pattern="solid", extra=dict(flame="#f8901e", flame_hot="#ffe070")),
        variants={"soul": dict(EYE="#4ae8f0", TRIM="#4ac8e8", extra=dict(flame="#2ab8e8", flame_hot="#b0f8ff")),
                  "ash": dict(FUR="#5a5658", HAIR="#3a3638", LIGHT="#8a8688", EYE="#f0c040", extra=dict(flame="#f8901e", flame_hot="#ffe070")),
                  "cinder": dict(FUR="#3a1a14", HAIR="#2a0e0a", LIGHT="#6a3a2a", EYE="#f84a2a", TRIM="#e83a1a", extra=dict(flame="#e83a1a", flame_hot="#f8a040")),
                  "white": dict(FUR="#f4f0ea", HAIR="#ffffff", LIGHT="#ffffff", MARK="#c8c0b4", EYE="#d8a020", CLOTH="#f4f0ea", TRIM="#e8b830",
                                extra=dict(flame="#f0c030", flame_hot="#fff8c0"))}),
    "ocelot_nekomata": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], bottom="shorts", extras=["armlets"]),
        ears="round", hair="sidetail", tail=((1.25, 1.25, 3.5), (1.25, 1.25, 3.5), (1.25, 1.25, 2.5)), tail_pattern="bars",
        pal=dict(SKIN=skin("tan"), HAIR="#8a5e34", FUR="#d8a868", LIGHT="#f4ead8", MARK="#2a1c14", EYE="#a8782a", CLOTH="#2e5a3a", TRIM="#d8b860",
                 pattern="rosettes", facemark="tear+whisker", extra=dict(dark_tips=True)),
        variants={"grey": dict(FUR="#b8ac98", HAIR="#7a7064", LIGHT="#ece8e0", CLOTH="#4a5a4a"),
                  "pale": dict(FUR="#ead6aa", HAIR="#b8946a", MARK="#6a4e34", CLOTH="#7a6a3a"),
                  "melanistic": dict(FUR="#2a2420", LIGHT="#4e443c", MARK="#100e0c", HAIR="#2e2622", EYE="#c8a83a", CLOTH="#6a2e2e"),
                  "golden": dict(FUR="#e8a050", HAIR="#a86a30", MARK="#3a2414", CLOTH="#2a4a6a"),
                  # homage to Gregory Horror Show (2026-10-25): Neko Zombie, sewn together, a purple patch over half her head
                  "stitched": dict(SKIN=skin("#ccc8b4"), FUR="#bcb8a4", HAIR="#aca898", LIGHT="#d8d4c4", MARK="#7a5a7a", EYE="#d82a2a", CLOTH="#c81e2a",
                                   TRIM="#1e4a9a", pattern="solid", extra=dict(stitched=True, cloth2="#8a7e60"))}),
}

LEG_PARTS = [("thigh", 2.25, 4.75, 2.25), ("shin", 1.75, 6.75, 1.75), ("paw", 2.25, 1.25, 3)]


def leg_parts(build):
    """the legs follow the body build (thicker thighs and calves for the strong and curvy ones)"""
    t = G.OF.thigh_width(build, 2.25)
    return [("thigh", t, 4.75, t), ("shin", t - 0.5, 6.75, t - 0.5), ("paw", max(2.25, t), 1.25, 3)]


def declare(sp):
    d = SPECIES[sp]
    (a, b, c) = d["tail"]
    extra = [("nz_muzzle", 3.5, 2.25, 2.5)] if "stitched" in d["variants"] else []   # the stitched cat's muzzle (drawn only on her)
    if d.get("flames"):
        extra += [("flame", 2.75, 2.25, 3.5), ("flame_hand", 2.25, 2.5, 2)]
    if d.get("lotus"):
        extra += [("lotus_petal", 2.25, .5, 3), ("lotus_heart", 1.5, 1.25, 1.5)]
    if d.get("chibi"):
        extra += [("lily_pad", 16, .5, 16)]
    if d.get("grin"):
        extra += [("grin", 4.5, 1.5, .25)]
    G.declare(leg_parts(d.get("build")) + [("tail_a",) + a, ("tail_b",) + b, ("tail_c",) + c] + extra, ears=d["ears"], hair=d["hair"],
              build=d.get("build"), outfit=d.get("outfit"))


def pal_for(sp, key=None):
    d = SPECIES[sp]
    kw = dict(d["pal"])
    if key:
        kw.update(d["variants"][key])
    p = G.palette(**{k: v for k, v in kw.items() if k not in ("pattern", "facemark", "extra", "pattern_scale")})
    p.pattern = kw.get("pattern", "solid")
    p.pattern_scale = kw.get("pattern_scale", 1.0)
    p.facemark = kw.get("facemark")
    p.extra = kw.get("extra", {})
    p.pupil = (0.9, 3.4)                     # slit pupils
    return p


def paint(sp, pal):
    d = SPECIES[sp]
    pt = H.Painter()
    G.paint_upper(pt, pal, top="wrap", top_pattern="weave" if sp.startswith("lion") else None, outfit=d.get("outfit"),
                  build=d.get("build"))
    fur = G.fur_fn(pal, light_faces=("down",), light_below=0.7)
    G.paint_ears(pt, pal, G.fur_fn(pal, pattern="solid"))
    if pal.extra.get("dark_tips"):                       # dark backs of the ears, the cat's "eye spots"
        pt("ear_a", ("south",), lambda f, x, y, w, h: pick(pal.MARK, 0.35, x, y) if y < h * 0.7 else pick(pal.LIGHT, 0.7, x, y))
    skin_t = H.skin_fn(pal, 0.68)

    def thigh(f, x, y, w, h):                            # skin on top, the coat begins half way down the thigh
        edge = h * 0.42 + (noise(x, 0, 5) - 0.5) * 3
        return skin_t(f, x, y, w, h) if y < edge else fur(f, x, y, w, h)
    pt("thigh", "all", thigh)
    pt("shin", "all", fur)

    def paw(f, x, y, w, h):
        if f == "down":
            for bx, by in ((w * 0.25, h * 0.3), (w * 0.5, h * 0.22), (w * 0.75, h * 0.3), (w * 0.5, h * 0.66)):
                if (x + 0.5 - bx) ** 2 + (y + 0.5 - by) ** 2 < 2.4:
                    return pick(pal.INNER, 0.5, x, y)
            return pick(pal.LIGHT, 0.4, x, y)
        if f == "north" and x % 3 == 0 and y >= 1:
            return pick(pal.FUR, 0.25, x, y)
        return pick(pal.LIGHT if f in ("north", "up") else pal.FUR, 0.6 + (noise(x, y, 7) - 0.5) * 0.14, x, y)
    pt("paw", "all", paw)
    tf = G.fur_fn(pal, light_faces=("down",), pattern=d["tail_pattern"], seed=21)
    pt("tail_a", "all", tf)
    pt("tail_b", "all", tf)
    if d.get("tuft"):
        pt("tail_c", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.4 + (noise(x, y, 9) - 0.5) * 0.3, x, y))
    else:
        tip = pal.MARK if d["tail_pattern"] != "solid" or pal.extra.get("dark_tips") else pal.FUR
        pt("tail_c", "all", lambda f, x, y, w, h: pick(tip, 0.38 + (noise(x, y, 9) - 0.5) * 0.2, x, y))
    if d.get("flames"):                                  # flickering flames: hot at the root, ragged at the top
        fl, hot = G.shades(pal.extra.get("flame", "#f8901e")), G.shades(pal.extra.get("flame_hot", "#ffe070"))

        def flame(f, x, y, w, h):
            if f == "down":
                return CLEAR
            if y < h * 0.45 and (x * 5 + y * 3) % 4 == 0:
                return CLEAR
            return pick(hot if y > h * 0.6 else fl, 0.55 + 0.25 * noise(x, y, 59), x, y)
        pt("flame", "all", flame)
        pt("flame_hand", "all", flame)
        pt("tail_c", "all", lambda f, x, y, w, h: pick(hot if y > h * 0.5 else fl, 0.6 + 0.2 * noise(x, y, 61), x, y))
    if d.get("lotus"):
        lp, li = G.shades(pal.extra.get("lotus", "#f8b0cc")), G.shades(pal.extra.get("lotus_in", "#fff0a0"))
        pt("lotus_petal", "all", lambda f, x, y, w, h: pick(lp, 0.55 + 0.2 * (f == "up") - 0.15 * (x == 0 or x == w - 1), x, y))
        pt("lotus_heart", "all", lambda f, x, y, w, h: pick(li, 0.65, x, y))
    if d.get("grin"):                                    # a crescent of white teeth, outlined: all of her that stays when she fades
        teeth, line = G.shades("#fbf8f0"), G.shades("#2a1420")

        def grin(f, x, y, w, h):                          # a smile: straight along the top, curving up to the corners below
            if f != "north":
                return CLEAR
            e = (x + 0.5 - w / 2) / (w / 2)
            bot = (h - 1) * (1 - 0.9 * e * e)
            if y > bot + 0.5:
                return CLEAR
            if y == 0 or y > bot - 0.5 or abs(e) > 0.93:
                return pick(line, 0.3, x, y)
            return pick(line, 0.4, x, y) if x % 2 == 0 and y < bot - 1 else pick(teeth, 0.78, x, y)
        pt("grin", "all", grin)
    if d.get("chibi"):
        pad = G.shades("#4a8a3a")

        def lily(f, x, y, w, h):                         # round, with the notch at the back and veins from the middle
            if f in ("up", "down"):
                dx, dy = x + 0.5 - w / 2, y + 0.5 - h / 2
                if dx * dx + dy * dy > (w / 2) ** 2 or (dy > 0 and abs(dx) < dy * 0.3):
                    return CLEAR
                vein = f == "up" and (abs(dx) < 0.6 or abs(dy) < 0.6 or abs(abs(dx) - abs(dy)) < 0.7)
                return pick(pad, 0.42 + 0.18 * (f == "up") + 0.12 * vein + (noise(x, y, 81) - 0.5) * 0.08, x, y)
            return pick(pad, 0.35, x, y) if abs(x + 0.5 - w / 2) < w * 0.22 else CLEAR
        pt("lily_pad", "all", lily)
    zombie = pal.extra.get("stitched")
    thread, hole, purple = G.shades("#e0dccc"), G.shades("#141414"), G.shades("#7a4a7a")
    ink = G.shades("#141414")
    pt("nz_muzzle", "all", lambda f, x, y, w, h: CLEAR if not zombie
       else pick(ink, 0.2, x, y) if f == "north" and y >= 1 and abs(x + 0.5 - w / 2) < (h - y) * 1.3 and y < h - 2      # a gaping mouth
       else pick(thread, 0.7, x, y) if f == "north" and y == h - 1 and x % 3 == 1
       else pick(pal.FUR, 0.5 + 0.1 * (f == "up"), x, y))
    if zombie:
        def seam(f, x, y, w, h, horizontal=True):         # a sewn seam: white loops with black stitch holes
            k = y if horizontal else x
            m = x if horizontal else y
            return (pick(thread, 0.65, x, y) if m % 3 == 1 else pick(hole, 0.2, x, y) if m % 3 == 0 else None) if k in (h // 2 - 1, h // 2) else None

        def head(f, x, y, w, h):                          # the purple patch over half her head, sewn on
            patch = (f == "north" and x >= w * 0.52 and (y < h * 0.34 or x >= w * 0.88)) or f == "east" or (f == "up" and x >= w * 0.5)
            edge = f == "north" and (x == int(w * 0.52) and y < h * 0.34 or y == int(h * 0.34) and x >= w * 0.52)
            if edge:
                return pick(thread, 0.7, x, y) if (x + y) % 2 == 0 else pick(hole, 0.2, x, y)
            return pick(purple, 0.5 + 0.1 * (f == "up"), x, y) if patch else None
        pt("head", "all", head)
        pt("hair_cap", ("north", "south", "east", "west", "up"), lambda f, x, y, w, h: pick(purple, 0.45 + 0.1 * (x % 4 == 0), x, y) if x >= w * 0.5 else None)
        pt("ear_a", "all", lambda f, x, y, w, h: pick(purple, 0.4 + 0.15 * (f == "north"), x, y))
        for p in ("thigh", "arm_up", "forearm", "tail_a", "tail_b"):
            pt(p, ("north", "east", "west", "south"), seam)
        stripe = lambda f, x, y, w, h: pick(pal.CLOTH if (y // 3) % 2 == 0 else pal.TRIM, 0.5, x, y)   # a red and blue striped top
        for p in ("chest", "bust", "cape_t", "cape_b", "neckband"):
            pt(p, ("north", "south", "east", "west"), stripe)
        for p in ("sh_hips", "short_leg"):                # stitched-up khaki shorts
            pt(p, ("north",), lambda f, x, y, w, h: pick(hole, 0.35, x, y) if abs(x - w * 0.3 - y * 0.6) < 0.6 and y % 2 == 0 else None)
    return pt.T


def build(sp):
    d = SPECIES[sp]
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    g = G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), ears=d["ears"], skirt=True, build=d.get("build"), outfit=d.get("outfit")))
    G.legs_biped(r, g, d)
    G.OF.dress_legs(r, d.get("outfit"))
    G.cat_tail(r, d, "tail")
    G.cat_tail(r, d, "tail2", x=0.6)                     # the true nekomata's second tail (hidden by the renderer)
    if d.get("lotus"):                                   # a lotus flower: behind her ear, or on top of a chibi head
        hy, z = g.hy, g.z
        cx, cy, cz = (1.1, hy + 9.3, -0.8 + z) if d.get("chibi") else (2.4, hy + 8.8, -1.4 + z)
        petals = [G.rot_cube("lotus_petal", [cx - 1.125, cy, cz - 0.2], [cx, cy, cz], -40, a, 0) for a in (0, 72, 144, 216, 288)]
        r.add("lotus", "head", [cx, cy, cz], petals + [cube("lotus_heart", [cx - 0.75, cy, cz - 0.75])])
    if d.get("flames"):                                  # flames round her feet and hands
        cx = r.b["foot_r"]["pivot"][0]
        r.put("foot_r", cube("flame", [cx - 1.375, 0.1, -2.0]))
        hx, hyy, hz = r.b["hand_r"]["pivot"]
        r.put("hand_r", cube("flame_hand", [hx - 1.125, hyy - 2.6, hz - 1.0]))
    if d.get("grin"):                                    # the grin, wide across her lower face
        r.put("head", cube("grin", [-2.25, g.hy + 0.6, -4.2 + g.z]))
    if "stitched" in d["variants"]:                      # Neko Zombie's muzzle (drawn only on that variant)
        r.put("head", cube("nz_muzzle", [-1.75, g.hy + 0.4, -6.3 + g.z]))
    if d.get("chibi"):
        chibify(r, g.hy)
        r.b["pad"] = dict(parent=None, pivot=[0, 0, 0], cubes=[cube("lily_pad", [-8, 0, -8])], mirror=False)
        r.order.insert(0, "pad")                         # its own bone: it stays flat whatever she does
        r.b["head"]["parent"] = "pad"                    # and her head rests on it, whatever her body does under it
    return r.model(sp)


def chibify(r, neck, body=0.5, head=1.3, lift=0.5):
    """Only a big head shows: the head (and all on it) grows about the neck and rests `lift` up, on her lily pad; the
    small body hangs under the pad, in the water. Faces keep their UV rectangles, so the texture just stretches."""
    def under_head(n):
        while n:
            if n == "head":
                return True
            n = r.b[n]["parent"]
        return False
    old_neck = [0, neck, 0]
    new_neck = [0, lift, 0]
    done = set()
    for n in r.order:
        if n == "root":
            continue
        k = head if under_head(n) else body
        move = (lambda p: [nn + (v - o) * head for v, o, nn in zip(p, old_neck, new_neck)]) if under_head(n) \
            else (lambda p: [p[0] * body, (p[1] - neck) * body, p[2] * body])
        b = r.b[n]
        b["pivot"] = move(b["pivot"])
        for c in b["cubes"]:
            if id(c) in done:
                continue
            done.add(id(c))
            c["origin"] = [round(v, 3) for v in move(c["origin"])]
            c["size"] = [round(v * k, 3) for v in c["size"]]
            if "pivot" in c:
                c["pivot"] = [round(v, 3) for v in move(c["pivot"])]
            if "inflate" in c:
                c["inflate"] = round(c["inflate"] * k, 3)


def anims(an):
    G.biped_anims(an, ["tail", "tail_mid", "tail_tip"], ["tail2", "tail2_mid", "tail2_tip"])


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.25, -30, 8), ("run", 0.15, -60, 5), ("sit", 0, -30, 10), ("pounce", 0.5, -70, 5)]
FACE = [("idle", 0, 0, 0, 24), ("idle", 2.75, 0, 0, 24), ("hiss", 0.5, 0, 0, 24), ("idle", 0, -30, 8, 24)]


def run(sp, previews):
    declare(sp)
    keys = list(SPECIES[sp]["variants"])
    tex = paint(sp, pal_for(sp))
    geo, adoc, texes = G.write(sp, tex, build(sp), anims, keys, lambda k: paint(sp, pal_for(sp, k)))
    if SPECIES[sp].get("glow"):
        import girl_family as F
        for k, t in texes.items():
            F.write_glowmask(sp + ("_" + k if k else ""), t, SPECIES[sp]["glow"])
    height = G.model_height(geo)
    if previews:
        G.previews(sp, geo, adoc, texes, VIEWS, FACE, height)
    return geo, adoc, tex, height


if __name__ == "__main__":
    want = [a for a in sys.argv[1:] if not a.startswith("--")]
    prev = "--previews" in sys.argv
    entries = []
    for s in FAMILY["species"]:
        if want and s["id"] not in want:
            continue
        geo, adoc, tex, height = run(s["id"], prev)
        entries.append((geo, tex, adoc["animations"][f"animation.{s['id']}.idle"], s["height"], height))
    if prev and not want:
        G.roster(os.path.join(G.PREVIEWS, "nekomata_roster.png"), entries)
