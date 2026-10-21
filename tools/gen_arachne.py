#!/usr/bin/env python3
"""Arachne (the spider family, see arachne_field_guide.html): jumping spider, orb weaver, tarantula, trapdoor spider,
wolf spider.

    python tools/gen_arachne.py [species ...] [--previews]

Shared girl_lib upper body on a spider's thorax with an abdomen behind and eight legs (leg1_r..leg4_l, knees
leg1b_r..). Four small extra eyes on the forehead, big dark eyes. The wolf spider carries a brood of spiderlings on
her abdomen (bone `brood`, shown by the game while she carries young). Five colours per species.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, shades  # noqa: E402

SPECIES = {
    "jumping_spider_arachne": dict(
        build="petite", outfit=dict(top=["sweater"], colors=dict()),
        abdomen=(6.5, 6, 7.5), hair="odango", hairy=0.08,
        pal=dict(SKIN=skin("pale"), HAIR="#1e1e24", FUR="#1a1a1e", LIGHT="#3a3a40", MARK="#f2f2f0", EYE="#2a2a36", CLOTH="#2a8a8a", TRIM="#e8e8e8",
                 pattern="spots", facemark="eyes"),
        variants={"bold": dict(MARK="#e8602a", CLOTH="#c8502a"), "zebra": dict(pattern="finestripes", HAIR="#e8e8e8"),
                  "golden": dict(FUR="#b08a40", MARK="#f0e0b0", pattern="bands", HAIR="#c89a48", CLOTH="#6a4a2a"),
                  "teal": dict(FUR="#1a3a3a", MARK="#3ad0c0", HAIR="#1a4a4a")}),
    "orb_weaver_arachne": dict(
        build="slim", outfit=dict(top=["bandeau", "corset"], extras=["pearls"], colors=dict(cloth2="#2a2420")),
        abdomen=(9, 8.5, 9), hair="drills", hairy=0.0,
        pal=dict(SKIN=skin("fair"), HAIR="#6a4a2a", FUR="#8a6a40", LIGHT="#c8b090", MARK="#f0ead8", EYE="#2a1a12", CLOTH="#4a3a2a", TRIM="#d8c8a0",
                 pattern="cross", facemark="eyes"),
        variants={"goldensilk": dict(FUR="#d8b040", MARK="#3a2a12", pattern="bands", HAIR="#c89a30"),
                  "marbled": dict(FUR="#e09040", MARK="#f4e8c8", pattern="blotches", HAIR="#c87a30", CLOTH="#6a3a1a"),
                  "spiny": dict(FUR="#f4f2ee", MARK="#c8202a", pattern="spots", HAIR="#e8e4e0", CLOTH="#8a1a2a"),
                  "wasp": dict(FUR="#f0d030", MARK="#141414", pattern="bars", HAIR="#f0d84a", CLOTH="#1e1e1e")}),
    "tarantula_arachne": dict(
        build="strong", outfit=dict(top=["bandeau", "vest"], extras=["armlets", "belt"]),
        abdomen=(9, 7.5, 9), hair="shaggy", hairy=0.22, thick=True,
        pal=dict(SKIN=skin("tan"), HAIR="#5a3e3a", FUR="#7a5a54", LIGHT="#d8a8a0", MARK="#d8a8a0", EYE="#2a1a1a", CLOTH="#6a3a4a", TRIM="#e0b8b0",
                 pattern="speckle", facemark="eyes"),
        variants={"redknee": dict(FUR="#1e1a18", MARK="#e0602a", LIGHT="#e0602a", pattern="speckle", HAIR="#2a2220", extra=dict(knees=True)),
                  "cobalt": dict(FUR="#1a2a6a", MARK="#4a6ad8", LIGHT="#6a8ae0", HAIR="#22306a", CLOTH="#3a3a7a"),
                  "pinktoe": dict(FUR="#1e1a1e", MARK="#f0a0c0", LIGHT="#f0a0c0", HAIR="#2a222a", extra=dict(toes=True), CLOTH="#a04a7a"),
                  "goliath": dict(FUR="#6a4a2e", MARK="#a8845a", LIGHT="#c8a070", HAIR="#5a3a22")}),
    "trapdoor_spider_arachne": dict(
        build="standard", outfit=dict(top=["bandeau", "capelet"], extras=["belt", "pouch"]),
        abdomen=(7.5, 6.5, 8), hair="bob", hairy=0.0,
        pal=dict(SKIN=skin("peach"), HAIR="#2a1e16", FUR="#3a2a1e", LIGHT="#6a5040", MARK="#5a4430", EYE="#1e1410", CLOTH="#5a4a3a", TRIM="#a89070",
                 pattern="solid", facemark="eyes"),
        variants={"black": dict(FUR="#151416", LIGHT="#3a3a3e", HAIR="#141414", CLOTH="#3a3a3a"),
                  "redleg": dict(FUR="#151416", LIGHT="#a8282a", MARK="#a8282a", HAIR="#1a1414", extra=dict(redlegs=True), CLOTH="#6a1a1a"),
                  "sandy": dict(FUR="#a08460", LIGHT="#c8b088", HAIR="#8a6a48", CLOTH="#a07a4a"),
                  "moss": dict(MARK="#4a6a2a", pattern="speckle", HAIR="#2a3a1e", CLOTH="#3a5a2a")}),
    "wolf_spider_arachne": dict(
        build="athletic", outfit=dict(top=["sports", "harness"], extras=["bracers", "satchel"]),
        abdomen=(7, 6.5, 9.5), hair="bun", hairy=0.1, brood=True,
        pal=dict(SKIN=skin("olive"), HAIR="#5a4030", FUR="#6a5040", LIGHT="#c8b090", MARK="#c8b090", EYE="#2a1e14", CLOTH="#4a5a3a", TRIM="#c8b890",
                 pattern="finestripes", pattern_scale=1.4, facemark="eyes"),
        variants={"striped": dict(FUR="#7a7672", MARK="#d8d4cc", HAIR="#6a6662", CLOTH="#3a4a5a"),
                  "sand": dict(FUR="#c8b088", MARK="#8a7458", HAIR="#a89068"),
                  "dark": dict(FUR="#2a2420", MARK="#5a4e44", HAIR="#2a2420", CLOTH="#2a2a2a"),
                  "raft": dict(FUR="#2a2420", MARK="#f0ece0", pattern="bars", pattern_scale=2.0, HAIR="#3a3028", CLOTH="#2a4a5a")}),
}


def declare(sp):
    d = SPECIES[sp]
    th = d.get("thick")
    parts = [("thorax", 6, 4.75, 6), ("abdomen",) + d["abdomen"], ("spinneret", 1.5, 1, 1),
             ("femur", 7, 1.5 if th else 1.25, 1.5 if th else 1.25), ("tibia", 1.25 if th else 1, 11.5, 1.25 if th else 1), ("tarsus", .75, 3.5, .75)]
    if d.get("brood"):
        parts.append(("spiderling", 1.5, 1, 1.5))
    G.declare(parts, ears=None, hair=SPECIES[sp]["hair"],
              build=SPECIES[sp].get("build"), outfit=SPECIES[sp].get("outfit"))


def paint(sp, key):
    d = SPECIES[sp]
    pal = G.pal_from(d["pal"], d["variants"].get(key) if key else None)
    pal.pupil = (2.6, 4.2)                                # big dark spider eyes
    pal.EYE = shades("#3a3a4a") if key is None or "EYE" not in d["variants"].get(key, {}) else pal.EYE
    pt = H.Painter()
    G.paint_upper(pt, pal, top="wrap", top_pattern="weave", outfit=d.get("outfit"), build=d.get("build"))
    ch = G.chitin_fn(pal, hairy=d["hairy"])
    plain = G.chitin_fn(pal, pattern="solid", hairy=d["hairy"])
    pt("thorax", "all", plain)
    pt("abdomen", "all", ch)
    pt("spinneret", "all", lambda f, x, y, w, h: pick(pal.FUR, 0.3, x, y))
    ex = pal.extra
    leg_ramp = pal.LIGHT if ex.get("redlegs") else pal.FUR
    pt("femur", "all", lambda f, x, y, w, h: pick(leg_ramp, 0.5 + (noise(x, y, 3) - 0.5) * 0.15, x, y) if not d["hairy"] or noise(x, y, 4) > d["hairy"] else pick(pal.LIGHT, 0.6, x, y))

    def tibia(f, x, y, w, h):
        if ex.get("knees") and y < 3:
            return pick(pal.MARK, 0.55, x, y)                 # the red-knee's orange knees
        if d["pal"]["pattern"] == "finestripes" and y % 4 == 0:
            return pick(pal.MARK, 0.4, x, y)                  # banded legs
        if d["hairy"] and noise(x, y, 6) > 1 - d["hairy"]:
            return pick(pal.LIGHT, 0.6, x, y)
        return pick(leg_ramp, 0.45 + (noise(x, y, 5) - 0.5) * 0.15, x, y)
    pt("tibia", "all", tibia)
    pt("tarsus", "all", lambda f, x, y, w, h: pick(pal.MARK if ex.get("toes") else leg_ramp, 0.35, x, y))
    pt("spiderling", "all", lambda f, x, y, w, h: pick(pal.FUR, 0.6 + 0.3 * ((x + y) % 3 == 0), x, y))
    skin_t = H.skin_fn(pal, 0.66)
    pt("hips", "all", lambda f, x, y, w, h: skin_t(f, x, y, w, h) if y < h * 0.5 else plain(f, x, y, w, h))
    return pt.T


def build(sp):
    d = SPECIES[sp]
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), build=d.get("build"), outfit=d.get("outfit"), ears=None, skirt=False))
    G.spider(r, None, d)
    if d.get("brood"):
        aw, ah, ad = G.S("abdomen")
        top = 6.5 + ah
        cs = [cube("spiderling", [x, top - 0.25, 3.5 + z]) for x, z in ((-2.5, 1.5), (0.8, 3.0), (-1.0, 5.2), (1.6, 6.4))]
        r.add("brood", "abdomen", [0, top, 6], cs)
    return r.model(sp)


def anims(an):
    G.spider_anims(an)


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.2, -30, 12), ("leap", 0.4, -60, 8), ("sit", 0, -30, 12), ("rear", 0.5, -30, 8)]
FACE = [("idle", 0, 0, 0, 24), ("idle", 2.75, 0, 0, 24), ("rear", 0.5, 0, 0, 24), ("idle", 0, -30, 8, 24)]

if __name__ == "__main__":
    G.run_family("arachne", SPECIES, declare, paint, build, anims, VIEWS, FACE)
