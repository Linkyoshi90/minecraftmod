#!/usr/bin/env python3
"""Rodents (the rodent family, see rodent_field_guide.html): capybara, Ratatoskr.

    python tools/gen_rodent.py [species ...] [--previews]

girl_family bipeds with fur legs and paws. The capybara is broad and calm, with tiny round ears, hardly any tail and a
yuzu balanced on her head (`yuzu`); Ratatoskr is a red squirrel with tufted ears, a huge bushy tail curled up her back
and a messenger's satchel. Extra animations: "soak" (the capybara up to her chin in water, eyes half shut) and
"scamper" (Ratatoskr's quick hop with her tail flicking).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, skin  # noqa: E402

SPECIES = {
    "capybara_rodent": dict(
        build="curvy", outfit=dict(top=["bandeau"], bottom="sarong", extras=["pearls"], colors=dict(cloth2="#e8a030")),
        hair="bob", bangs="blunt", ears="round", legs="fur", foot="paw", tail=[(1.5, 1, .75)], tail_rest=[(-20, 0, 0)], tail_look="fur", yuzu=True,
        pal=dict(SKIN=skin("tan"), HAIR="#6a4228", FUR="#8a5a3a", LIGHT="#b8875e", MARK="#5a3a24", EYE="#2a1a10", CLOTH="#3a8a7a", TRIM="#e8c060",
                 pattern="solid"),
        variants={"sandy": dict(FUR="#b8946a", HAIR="#8a6a48", LIGHT="#d8bc94"),
                  "dark": dict(FUR="#4a3220", HAIR="#2e1e12", LIGHT="#6a4e36"),
                  "ginger": dict(FUR="#a8582a", HAIR="#7a3e1e", LIGHT="#d0885a", CLOTH="#3a6a9a"),
                  "grey": dict(FUR="#7a726a", HAIR="#5a524a", LIGHT="#a8a098", CLOTH="#9a3a4a")}),
    # tenth wave: the needlehog, a hedgehog with long quills down her back and head (a note pinned among them)
    "needlehog_rodent": dict(
        build="petite", outfit=dict(top=["sweater"], bottom="shorts", extras=["satchel"], colors=dict(cloth2="#5a4a3a", leather="#7a5a3a")),
        hair="bob", bangs="blunt", ears="round", legs="fur", foot="paw", tail=[(1, 1, .75)], tail_rest=[(-20, 0, 0)], tail_look="fur", quills=True,
        pal=dict(SKIN=skin("fair"), HAIR="#6a5040", FUR="#c8b090", LIGHT="#ecdcc4", MARK="#3a2a20", EYE="#1e1410", CLOTH="#8aa0c8", TRIM="#c8a060",
                 pattern="solid", extra=dict(quill="#5a4030", quill_tip="#e8dcc8")),
        variants={"blue": dict(HAIR="#4a5a7a", FUR="#a8b0c0", MARK="#2a3040", CLOTH="#e8c070", extra=dict(quill="#3a4868", quill_tip="#c8d8f0")),
                  "albino": dict(HAIR="#f0ece6", FUR="#f8f4ee", LIGHT="#ffffff", MARK="#c8c0b8", EYE="#d84a5a", CLOTH="#c8a8d8",
                                 extra=dict(quill="#e8e2da", quill_tip="#ffffff")),
                  "cinnamon": dict(HAIR="#a85a2a", FUR="#d8a878", MARK="#6a3418", CLOTH="#5a8a5a", extra=dict(quill="#8a4a22", quill_tip="#f0d0a8")),
                  "dark": dict(HAIR="#2a2220", FUR="#6a5a50", MARK="#141010", CLOTH="#a84a4a", extra=dict(quill="#1e1816", quill_tip="#a8a098"))}),
    # eleventh wave: the coatimano, a coati: a long ringed tail held high and a long nose
    "coatimano_rodent": dict(
        build="slim", outfit=dict(top=["halter", "vest"], bottom="shorts", extras=["satchel"], colors=dict(cloth2="#3a2a1e", leather="#7a5a3a")),
        hair="ponytail", bangs="messy", ears="round", legs="fur", foot="paw",
        tail=[(1.75, 1.75, 4), (1.5, 1.5, 4), (1.25, 1.25, 4), (1, 1, 3)], tail_rest=[(55, 0, 0), (12, 0, 0), (10, 0, 0), (8, 0, 0)],
        tail_look="fur", coati=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a3a7a", FUR="#3a5ab0", LIGHT="#e8eef8", MARK="#14204a", EYE="#1a1a24", CLOTH="#e8c060", TRIM="#e8e8f0",
                 pattern="bars"),
        variants={"brown": dict(HAIR="#5a3a24", FUR="#8a5a34", LIGHT="#e8d0b0", MARK="#3a2414", CLOTH="#4a7a5a"),
                  "ring": dict(HAIR="#4a4a50", FUR="#8a8a90", LIGHT="#f4f4f6", MARK="#1e1e22", CLOTH="#c84a3a"),
                  "red": dict(HAIR="#7a2e1a", FUR="#a8482a", LIGHT="#f0c8a8", MARK="#4a160a", CLOTH="#3a5a8a"),
                  "white": dict(HAIR="#e0dcd4", FUR="#ece8e0", LIGHT="#ffffff", MARK="#9a948a", EYE="#c84a5a", CLOTH="#5a7a3a")}),
    "ratatoskr_rodent": dict(
        build="petite", outfit=dict(top=["bandeau", "vest"], bottom="shorts", extras=["satchel", "scarf"], colors=dict(scarf="#3a7a4a", leather="#5a3a22")),
        hair="ponytail", bangs="messy", ears="tuft", legs="fur", foot="paw",
        tail=[(2.5, 2.5, 4), (3, 3, 4), (2.75, 2.75, 4), (2, 2, 3)], tail_rest=[(-70, 0, 0), (55, 0, 0), (45, 0, 0), (40, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#b8501e", FUR="#c8602a", LIGHT="#f0e0c8", MARK="#7a3414", EYE="#2a1a10", CLOTH="#4a6a3a", TRIM="#c8a050",
                 pattern="solid"),
        variants={"grey": dict(FUR="#8a8a90", HAIR="#6a6a72", LIGHT="#e8e8ec", MARK="#4a4a50"),
                  "black": dict(FUR="#222024", HAIR="#141216", LIGHT="#5a5860", MARK="#0a0a0c"),
                  "fox": dict(FUR="#c8783a", HAIR="#a85a26", LIGHT="#e8b07a", MARK="#5a3418", CLOTH="#3a5a7a"),
                  "abert": dict(FUR="#5a5a62", HAIR="#3a3a40", LIGHT="#f4f4f6", MARK="#7a3a1a", CLOTH="#7a3a3a")}),
}


def declare_hook(sp, d):
    out = [("yuzu", 2, 2, 2), ("yuzu_leaf", 1, .25, 1.5)] if d.get("yuzu") else []
    if d.get("quills"):
        out += [("quill", 1, 1, 5), ("note", 2, 2.5, .25)]
    if d.get("coati"):
        out += [("coati_nose", 2, 1.75, 2.5)]
    return out


def paint_hook(pt, pal, d):
    orange = G.shades("#f0a020")
    pt("yuzu", "all", lambda f, x, y, w, h: pick(orange, 0.55 + 0.25 * (f == "up") - 0.15 * (f == "down"), x, y))
    pt("yuzu_leaf", "all", lambda f, x, y, w, h: pick(G.shades("#4a9a3a"), 0.55, x, y))
    pt("coati_nose", "all", lambda f, x, y, w, h: pick(G.shades("#1e1a1c"), 0.4, x, y) if f == "north" and y == h - 1
       else pick(pal.LIGHT if f == "down" else pal.FUR, 0.55, x, y))
    q, tip = G.shades(pal.extra.get("quill", "#5a4030")), G.shades(pal.extra.get("quill_tip", "#e8dcc8"))
    pt("quill", "all", lambda f, x, y, w, h: pick(tip if y >= h - 1 or f == "south" else q, 0.5 + 0.15 * (x % 2), x, y))
    pt("note", "all", lambda f, x, y, w, h: pick(G.shades("#3a3a4a"), 0.45, x, y) if f == "south" and y % 2 == 1 and 0 < x < w - 1
       else pick(G.shades("#f4ecd8"), 0.65, x, y))


def build_hook(r, g, d):
    if d.get("coati"):                                   # the long nose
        r.put("head", cube("coati_nose", [-1, g.hy + 0.6, -5.9 + g.z]))
    if d.get("quills"):                                 # rows of quills sweeping back over her head and down her back
        hy, z = g.hy, g.z
        hq = [G.rot_cube("quill", [x - 0.5, hy + y, 3.6 + z], [x, hy + y + 0.5, 3.8 + z], 25 + 10 * (y > 5), x * 5, 0)
              for x in (-3.4, -1.7, 0, 1.7, 3.4) for y in (2.5, 5.0, 7.2)]
        r.add("quills_head", "head", [0, hy + 4, 2 + z], hq)
        bq = [G.rot_cube("quill", [x - 0.5, y, 1.5 + z], [x, y + 0.5, 1.7 + z], 40, x * 6, 0) for x in (-2.2, -0.75, 0.75, 2.2) for y in (13.5, 16.0, 18.5)]
        r.add("quills", "body", [0, 16, 1.6 + z], bq + [G.rot_cube("note", [-1.8, 16.0, 3.0 + z], [-0.8, 17.0, 3.0 + z], 10, 0, -12)])
    if d.get("yuzu"):                                   # the capybara's famous yuzu, balanced on her head
        r.add("yuzu", "head", [0, g.hy + 8.5, g.z], [cube("yuzu", [-1, g.hy + 8.6, -1 + g.z]), cube("yuzu_leaf", [-0.5, g.hy + 10.6, -0.5 + g.z])])


def anim_extra(an, sp, d):
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("soak", 4.0, True, merge({"root": {"position": hold(0, -7, 0)}, "head": {"rotation": hold(-6, 0, 0)},
                                     "eyelids": {"position": Tk((0, H.REST), (1.0, H.OUT_), (3.0, H.OUT_), (4.0, H.REST))}},
                                    sym("arm", hold(-20, 0, 25))))
    if d.get("tail"):
        tails = ["tail"] + [f"tail_{i}" for i in range(2, len(d["tail"]) + 1)]
        flick = {t: {"rotation": Tk((0, (0, 0, 0)), (0.1, (-15, 0, 0)), (0.2, (0, 0, 0)), (0.4, (0, 0, 0)))} for t in tails[1:]}
        if d.get("quills"):                             # the needlehog rolled into a ball of quills
            an.add("curl", 2.0, True, merge({"root": {"position": hold(0, -5, 0)}, "body": {"rotation": hold(85, 0, 0), "position": hold(0, -2, 2)},
                                             "head": {"rotation": hold(50, 0, 0)}, "leg_r": {"rotation": hold(-110, 0, 0)}, "leg_l": {"rotation": hold(-110, 0, 0)},
                                             "shin_r": {"rotation": hold(140, 0, 0)}, "shin_l": {"rotation": hold(140, 0, 0)},
                                             "eyelids": {"position": hold(*H.OUT_)}}, sym("arm", hold(-130, 0, -15))))
        an.add("scamper", 0.4, True, merge(flick, {"root": {"position": Tk((0, (0, 0, 0)), (0.1, (0, 1.2, 0)), (0.2, (0, 0, 0)), (0.4, (0, 0, 0)))},
                                                   "body": {"rotation": hold(20, 0, 0)}}, sym("arm", hold(-40, 0, 0))))


if __name__ == "__main__":
    F.run_family("rodent", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
