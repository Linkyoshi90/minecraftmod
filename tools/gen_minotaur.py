#!/usr/bin/env python3
"""Minotaurs (the cattle family, see minotaur_field_guide.html): cattle, Minoan, bison, aurochs, water buffalo.

    python tools/gen_minotaur.py [species ...] [--previews]

The girl_family biped with hooves, cow ears, horns (bone `horns`) and a tufted cow tail. The only family with men in
it: every species also gets a bull, <species>_male (the "male" build: broad flat chest, no top, short hair, bigger
horns), with its own textures <species>_male[_<variant>].png. The bull's animation file uses the species' animation
names (animation.<species>.*), so the game plays the same animations on either model. The Minoan carries a labrys.
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin  # noqa: E402

COW_TAIL = [(1, 1, 3.5), (.75, .75, 3.5), (1.5, 1.75, 1.75)]
TAIL_REST = [(-62, 0, 0), (-14, 0, 0), (-6, 0, 0)]

SPECIES = {
    "cattle_minotaur": dict(
        build="curvy", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["bell_collar"], colors=dict(cloth2="#4a6aa8")),
        hair="wavy", bangs="swept", ears="cow", legs="hoof", foot="hoof", horns="bull", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        male=dict(hair="shaggy", horns="bull_big", outfit=dict(bottom="loincloth", extras=["bell_collar"])),
        pal=dict(SKIN=skin("fair"), HAIR="#f2eee6", FUR="#f4f2ee", LIGHT="#ffffff", MARK="#1e1c1c", EYE="#4a3020", CLOTH="#e8e2d0", TRIM="#e0b040",
                 INNER="#f0b0b0", pattern="patches", extra=dict(horn="#e8e0cc", hoof="#3a3430")),
        variants={"jersey": dict(FUR="#c8925a", HAIR="#a8723a", MARK="#8a5a2a", pattern="solid", LIGHT="#ecd0a8"),
                  "highland": dict(FUR="#c8682a", HAIR="#d07a3a", MARK="#9a4a1e", pattern="solid", LIGHT="#e8a870", extra=dict(horn="#f0e6cc")),
                  "angus": dict(FUR="#1e1c1c", HAIR="#141212", MARK="#0a0808", pattern="solid", LIGHT="#3a3636"),
                  "hereford": dict(FUR="#8a3a22", HAIR="#f2eee6", MARK="#f2eee6", pattern="patches", LIGHT="#f8f4ee")}),
    "minoan_minotaur": dict(
        build="strong", outfit=dict(top=["sports", "harness"], bottom="loincloth", extras=["pauldron", "bracers", "belt"], colors=dict(leather="#8a5a2a")),
        hair="braid", bangs="blunt", ears="cow", legs="hoof", foot="hoof", horns="bull_big", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        labrys=True, male=dict(hair="pixie", outfit=dict(bottom="loincloth", extras=["pauldron", "bracers", "belt"], colors=dict(leather="#8a5a2a"))),
        pal=dict(SKIN=skin("olive"), HAIR="#2a1a12", FUR="#4a2e1e", LIGHT="#8a6a50", MARK="#2a1a12", EYE="#c8902a", CLOTH="#a8282a", TRIM="#d8a840",
                 INNER="#c88a7a", extra=dict(horn="#e8dcc0", horn_tip="#c8a040", hoof="#2a2420")),
        variants={"crimson": dict(FUR="#6a2e22", HAIR="#3a1a14", CLOTH="#7a1a2a"),
                  "ivory": dict(FUR="#e8e0cc", HAIR="#f2ece0", LIGHT="#fffaf0", MARK="#b8a890", CLOTH="#3a5aa8"),
                  "obsidian": dict(FUR="#1a1618", HAIR="#0e0c0e", LIGHT="#3a3436", MARK="#0a0808", CLOTH="#5a2a6a"),
                  "gilded": dict(FUR="#8a6a3a", HAIR="#c8a050", LIGHT="#e8d0a0", CLOTH="#f0f0f0", TRIM="#f8d860")}),
    "bison_minotaur": dict(
        build="strong", outfit=dict(top=["bandeau", "mantle"], bottom="loincloth", extras=["arm_wraps"], colors=dict(cloth2="#6a4a2a")),
        hair="mane", bangs="messy", ears="cow", legs="hoof", foot="hoof", horns="bull", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        male=dict(hair="mane", horns="bull", outfit=dict(top=["mantle"], bottom="loincloth", extras=["arm_wraps"])),
        pal=dict(SKIN=skin("tan"), HAIR="#3a2618", FUR="#5a3e28", LIGHT="#8a6a4a", MARK="#2a1a10", EYE="#2a1a10", CLOTH="#a87a4a", TRIM="#c89a5a",
                 INNER="#a87a6a", extra=dict(horn="#2a2420", hoof="#1e1a18")),
        variants={"wood": dict(FUR="#3a2618", HAIR="#22160e", LIGHT="#5a4030"),
                  "white": dict(FUR="#ece6da", HAIR="#f4f0e6", LIGHT="#ffffff", MARK="#c8bca8", extra=dict(horn="#5a4a3a")),
                  "dark": dict(FUR="#2a1e16", HAIR="#140e0a", LIGHT="#4a3a2e", MARK="#0a0604"),
                  "golden": dict(FUR="#9a6a3a", HAIR="#6a4422", LIGHT="#c8a070")}),
    "aurochs_minotaur": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps", "belt"]),
        hair="ponytail", bangs="parted", ears="cow", legs="hoof", foot="hoof", horns="bull_big", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        male=dict(hair="shaggy", outfit=dict(bottom="loincloth", extras=["arm_wraps", "belt"])),
        pal=dict(SKIN=skin("peach"), HAIR="#1a1614", FUR="#1e1a18", LIGHT="#c8b8a0", MARK="#0e0c0a", EYE="#3a2a1a", CLOTH="#d8cfbc", TRIM="#a88a5a",
                 INNER="#a88a7a", pattern="solid", extra=dict(horn="#f0e6d0", horn_tip="#2a2420", hoof="#1a1614")),
        variants={"red": dict(FUR="#6a2e1a", HAIR="#4a1e10", LIGHT="#c8987a"),
                  "dun": dict(FUR="#a88a62", HAIR="#7a6040", LIGHT="#e0cca8"),
                  "grey": dict(FUR="#6a6862", HAIR="#4a4844", LIGHT="#c8c4bc"),
                  "primal": dict(FUR="#2a1e16", HAIR="#c88a4a", LIGHT="#d8a870", MARK="#c88a4a")}),
    "water_buffalo_minotaur": dict(
        build="athletic", outfit=dict(top=["bandeau"], bottom="sarong", extras=["satchel"], colors=dict(cloth2="#3a7a6a")),
        hair="sidetail", bangs="wispy", ears="cow", legs="hoof", foot="hoof", horns="buffalo", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        male=dict(hair="pixie", outfit=dict(bottom="sarong", extras=["satchel"], colors=dict(cloth2="#3a7a6a"))),
        pal=dict(SKIN=skin("tan"), HAIR="#2a2a2e", FUR="#4a4a50", LIGHT="#8a8a90", MARK="#2a2a2e", EYE="#2a2018", CLOTH="#e8b040", TRIM="#c87a2a",
                 INNER="#a88a8a", extra=dict(horn="#3a3632", hoof="#2a2624")),
        variants={"black": dict(FUR="#1e1e22", HAIR="#141418", LIGHT="#4a4a50"),
                  "albino": dict(FUR="#f0c8c0", HAIR="#f8e4e0", LIGHT="#fff4f0", MARK="#d8a0a0", extra=dict(horn="#e8d8c8")),
                  "mud": dict(FUR="#6a5640", HAIR="#4a3a28", LIGHT="#9a8668"),
                  "carabao": dict(FUR="#5a5048", HAIR="#3a342e", LIGHT="#8a8278")}),
}


def male(sp):
    """the bull's table entry: the cow's with the male build, short hair and his own outfit"""
    d = dict(SPECIES[sp])
    m = d["male"]
    d.update(build="male", hair=m.get("hair", "pixie"), bangs="messy", outfit=m["outfit"], horns=m.get("horns", d["horns"]))
    d["pal"] = dict(d["pal"], CLOTH=d["pal"].get("extra", {}).get("cloth2", d["pal"]["CLOTH"]))
    return d


def declare_hook(sp, d):
    return [("haft", .75, 13, .75), ("blade", 3, 3.5, .5)] if d.get("labrys") else []


def paint_hook(pt, pal, d):
    bronze = G.shades("#b8803a")
    pt("haft", "all", lambda f, x, y, w, h: pick(G.shades("#5a3a22"), 0.45 + 0.15 * (y % 4 == 0), x, y))
    pt("blade", "all", lambda f, x, y, w, h: pick(bronze, 0.55 + 0.3 * (x == 0 or x == w - 1) + (noise(x, y, 5) - 0.5) * 0.1, x, y))


def build_hook(r, g, d):
    if not d.get("labrys"):
        return
    hx = -(G.S("chest")[0] / 2 + G.S("arm_up")[0] / 2)
    y = 19.5 - 9.0
    r.put("hand_r", cube("haft", [hx - 0.375, y - 7, -0.6]), cube("blade", [hx - 3.4, y + 2.5, -0.85]), cube("blade", [hx + 0.4, y + 2.5, -0.85]))


def anim_extra(an, sp, d):
    """the charge: head down, horns first"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("charge", 0.6, True, merge({
        "body": {"rotation": hold(28, 0, 0)}, "head": {"rotation": hold(16, 0, 0)},
        "leg_r": {"rotation": Tk((0, (50, 0, 0)), (0.3, (-50, 0, 0)), (0.6, (50, 0, 0)))},
        "leg_l": {"rotation": Tk((0, (-50, 0, 0)), (0.3, (50, 0, 0)), (0.6, (-50, 0, 0)))},
        "shin_r": {"rotation": Tk((0, (10, 0, 0)), (0.15, (60, 0, 0)), (0.3, (10, 0, 0)), (0.6, (10, 0, 0)))},
        "shin_l": {"rotation": Tk((0, (10, 0, 0)), (0.3, (10, 0, 0)), (0.45, (60, 0, 0)), (0.6, (10, 0, 0)))},
        "hair_back": {"rotation": hold(30, 0, 0)},
    }, sym("arm", hold(30, 0, 14)), sym("forearm", hold(-40, 0, 0))))
    an.add("milk", 2.0, True, merge({"body": {"rotation": hold(-4, 0, 0)}, "head": {"rotation": hold(10, 20, 0)}, "eyelids": {"position": hold(*H.OUT_)}},
                                    sym("arm", hold(-20, 0, -14)), sym("forearm", hold(-60, 0, 0))))


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.25, -30, 8), ("charge", 0.15, -60, 5), ("sit", 0, -30, 10), ("pounce", 0.5, -70, 5)]


def write_males(want, prev):
    """the bulls: same rig and animations, the male build; their animation names are the species' (see the docstring)"""
    entries = []
    for sp in SPECIES:
        if want and sp not in want:
            continue
        d = male(sp)
        name = sp + "_male"
        F.declare(name, d, declare_hook(sp, d))
        tex = F.paint(name, d, F.pal_for(d), paint_hook)
        geo, adoc, texes = G.write(name, tex, F.build(name, d, build_hook), F.anims(d, lambda an, sp=sp, d=d: anim_extra(an, sp, d)),
                                   list(d["variants"]), lambda k, d=d: F.paint(name, d, F.pal_for(d, k), paint_hook))
        path = os.path.join(G.ASSETS, "animations", "entity", name + ".animation.json")
        doc = json.load(open(path, encoding="utf-8"))
        doc["animations"] = {k.replace(f"animation.{name}.", f"animation.{sp}."): v for k, v in doc["animations"].items()}
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            json.dump(doc, fh, indent=2)
            fh.write("\n")
        height = G.model_height(geo)
        if prev:
            G.previews(name, geo, adoc, texes, VIEWS, F.FACE, height)
        entries.append((geo, tex, adoc["animations"][f"animation.{name}.idle"], height))
    return entries


if __name__ == "__main__":
    want = [a for a in sys.argv[1:] if not a.startswith("--")]
    cows = F.run_family("minotaur", SPECIES, declare_hook, paint_hook, build_hook, anim_extra, views=VIEWS)
    bulls = write_males(want, "--previews" in sys.argv)
    if "--previews" in sys.argv and not want:
        import family_concepts as FC
        hs = {s["id"]: s["height"] for s in FC.family("minotaur")["species"]}
        ent = []
        for (cg, ct, ca, ch, cu), (bg, bt, ba, bu), sp in zip(cows, bulls, SPECIES):
            ent += [(cg, ct, ca, ch, cu), (bg, bt, ba, ch * 1.1, bu)]
        G.roster(os.path.join(G.PREVIEWS, "minotaur_roster.png"), ent)
