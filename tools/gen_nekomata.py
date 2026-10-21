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
from girl_lib import H, pick, noise, CLEAR, BROAD, shades, skin  # noqa: E402

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
}

LEG_PARTS = [("thigh", 2.25, 4.75, 2.25), ("shin", 1.75, 6.75, 1.75), ("paw", 2.25, 1.25, 3)]


def leg_parts(build):
    """the legs follow the body build (thicker thighs and calves for the strong and curvy ones)"""
    t = G.OF.thigh_width(build, 2.25)
    return [("thigh", t, 4.75, t), ("shin", t - 0.5, 6.75, t - 0.5), ("paw", max(2.25, t), 1.25, 3)]


def declare(sp):
    d = SPECIES[sp]
    (a, b, c) = d["tail"]
    G.declare(leg_parts(d.get("build")) + [("tail_a",) + a, ("tail_b",) + b, ("tail_c",) + c], ears=d["ears"], hair=d["hair"],
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
    return r.model(sp)


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
