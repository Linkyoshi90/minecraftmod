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
from girl_lib import H, cube, pick, skin, CLEAR  # noqa: E402

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
    # twelfth wave: the marmotaur, a stout marmot with curled golden ram horns and an amber pendant
    "marmotaur_rodent": dict(
        build="curvy", outfit=dict(top=["sweater"], bottom="shorts", extras=["pearls"], colors=dict(cloth2="#5a4a3a", pearl="#e8c050")),
        hair="shaggy", bangs="messy", ears="round", legs="fur", foot="paw", tail=[(2.25, 2.25, 3), (2.5, 2.5, 3)], tail_rest=[(-35, 0, 0), (20, 0, 0)],
        tail_look="fur", horns="ram", pendant=True,
        pal=dict(SKIN=skin("fair"), HAIR="#8a6a42", FUR="#a88458", LIGHT="#f0e4cc", MARK="#5a4228", EYE="#3a7ac8", CLOTH="#e8dcc4", TRIM="#e8c050",
                 pattern="dapple", extra=dict(horn="#d8a838", amber="#e8701e")),
        variants={"grey": dict(HAIR="#6a6660", FUR="#8a8680", LIGHT="#e8e4e0", MARK="#4a4642", extra=dict(horn="#a8783a", amber="#e8701e")),
                  "black": dict(HAIR="#2a2624", FUR="#3a3432", LIGHT="#8a8480", MARK="#1a1614", EYE="#c8a030", extra=dict(horn="#c8ccd4", amber="#5ab8e8")),
                  "red": dict(HAIR="#8a3e1e", FUR="#a8542a", LIGHT="#f0c8a8", MARK="#5a2410", extra=dict(horn="#d8a838", amber="#e8c03a")),
                  "white": dict(HAIR="#e8e4dc", FUR="#f0ece6", LIGHT="#ffffff", MARK="#b8b0a4", EYE="#c84a5a", extra=dict(horn="#e8c050", amber="#c83a5a"))}),
    # twelfth wave: the bambadger, a badger striped like bamboo, a bamboo flute on a cord
    "bambadger_rodent": dict(
        build="petite", outfit=dict(top=["halter"], bottom="shorts", extras=["scarf"], colors=dict(scarf="#e8dcb0", cloth2="#3a5a2a")),
        hair="bob", bangs="parted", ears="round", legs="fur", foot="paw", tail=[(2, 2, 2.5)], tail_rest=[(-25, 0, 0)], tail_look="fur", flute=True,
        pal=dict(SKIN=skin("fair"), HAIR="#4a7a2e", FUR="#6a9a3a", LIGHT="#f0ecd8", MARK="#3a5a1e", EYE="#3a6a2a", CLOTH="#e8dcb0", TRIM="#8ab84a",
                 pattern="bars", extra=dict(bamboo="#8ab84a")),
        variants={"golden": dict(HAIR="#a8882e", FUR="#c8a840", MARK="#6a5418", CLOTH="#3a5a2a", extra=dict(bamboo="#e8c84a")),
                  "black": dict(HAIR="#2a2a26", FUR="#3a3a34", LIGHT="#e8e4d8", MARK="#141412", EYE="#8ab84a", extra=dict(bamboo="#4a4a3a")),
                  "red": dict(HAIR="#8a3a22", FUR="#a84e2e", MARK="#5a2010", extra=dict(bamboo="#c8683a")),
                  "pale": dict(HAIR="#b8c8a0", FUR="#d0dcb8", LIGHT="#ffffff", MARK="#8a9a70", extra=dict(bamboo="#d8e8b0"))}),
    # thirteenth wave: the squirricerat, a squirrel with a triceratops' frill and three horns, a great spiral tail
    "squirricerat_rodent": dict(
        build="athletic", outfit=dict(top=["halter", "vest"], bottom="shorts", extras=["belt", "pearls"], colors=dict(cloth2="#3a2a1e", leather="#8a6a3a")),
        hair="ponytail", bangs="jagged", ears="tuft", legs="fur", foot="paw",
        tail=[(2.75, 2.75, 4), (3.25, 3.25, 4), (3, 3, 4), (2.25, 2.25, 3)], tail_rest=[(-70, 0, 0), (55, 0, 0), (45, 0, 0), (40, 0, 0)], tail_look="fur",
        frill=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a4a9a", FUR="#3a5ab0", LIGHT="#d0dcf4", MARK="#1a2a6a", EYE="#e8a030", CLOTH="#d8b060", TRIM="#2a4a9a",
                 pattern="dapple", extra=dict(frill="#a8c4f8", frill_edge="#e8c060", horn="#f0e8d8")),
        variants={"green": dict(HAIR="#2a6a3a", FUR="#3a8a4a", LIGHT="#c8ecc8", MARK="#1a4a24", extra=dict(frill="#5aa86a", frill_edge="#f0d060", horn="#f0e8d8")),
                  "red": dict(HAIR="#8a2a1e", FUR="#b8402a", LIGHT="#f0c8b0", MARK="#5a1a10", CLOTH="#3a5a8a",
                              extra=dict(frill="#d8603a", frill_edge="#f8e070", horn="#f0e8d8")),
                  "brown": dict(HAIR="#5a3a22", FUR="#8a5a34", LIGHT="#e8d0b0", MARK="#3a2414", extra=dict(frill="#a87a4a", frill_edge="#e8c060", horn="#f0e8d8")),
                  "grey": dict(HAIR="#4a4a52", FUR="#7a7a84", LIGHT="#e0e0e6", MARK="#2a2a32", extra=dict(frill="#9a9aa6", frill_edge="#c8d8f0", horn="#f0e8d8"))}),
    # thirteenth wave: the kamaitachi, the sickle weasel of the wind: long and slim, a sickle blade on each forearm
    "kamaitachi_rodent": dict(
        build="slim", outfit=dict(top=["halter"], bottom="hakama", extras=["scarf"], colors=dict(cloth2="#3a5a7a", scarf="#e8f0f8")),
        hair="sidetail", bangs="jagged", ears="round", legs="fur", foot="paw",
        tail=[(1.5, 1.5, 4), (1.5, 1.5, 4), (1.25, 1.25, 4), (1, 1, 3)], tail_rest=[(-15, 0, 0), (12, 0, 0), (10, 0, 0), (8, 0, 0)], tail_look="fur",
        sickles=True,
        pal=dict(SKIN=skin("fair"), HAIR="#c89a5a", FUR="#d8aa6a", LIGHT="#fbf4e8", MARK="#6a4a2a", EYE="#3a8a6a", CLOTH="#3a5a7a", TRIM="#e8f0f8",
                 pattern="solid", extra=dict(blade="#c8d0dc")),
        variants={"white": dict(HAIR="#f4f2ee", FUR="#f6f4f0", LIGHT="#ffffff", MARK="#2a2628", EYE="#c84a5a", CLOTH="#2a2a3a"),
                  "brown": dict(HAIR="#6a4a2e", FUR="#7a5636", LIGHT="#e8d4bc", MARK="#3a2416", CLOTH="#5a3a6a"),
                  "grey": dict(HAIR="#7a7a80", FUR="#8a8a90", LIGHT="#e8e8ec", MARK="#3a3a40", CLOTH="#3a6a5a"),
                  "black": dict(HAIR="#1e1c1e", FUR="#2a282a", LIGHT="#8a8488", MARK="#0e0c0e", EYE="#e8d040", CLOTH="#a83a3a")}),
    # fourteenth wave: the silkcoon, a raccoon of silky white fur swirled in blue, a ringed bushy tail
    "silkcoon_rodent": dict(
        build="petite", outfit=dict(top=["halter", "capelet"], bottom="shorts", extras=["pearls"], colors=dict(cloth2="#2a4a9a")),
        hair="bob", bangs="parted", ears="round", legs="fur", foot="paw",
        tail=[(2, 2, 4), (2.25, 2.25, 4), (2, 2, 3)], tail_rest=[(-30, 0, 0), (15, 0, 0), (12, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#e8eef8", FUR="#f0f4fa", LIGHT="#ffffff", MARK="#3a5ab8", EYE="#2a3a8a", CLOTH="#3a5ab8", TRIM="#f0f4fa",
                 pattern="bars"),
        variants={"grey": dict(HAIR="#6a6a72", FUR="#8a8a92", LIGHT="#e0e0e6", MARK="#2a2a30", CLOTH="#4a6a3a"),
                  "brown": dict(HAIR="#6a4a2e", FUR="#8a6644", LIGHT="#e8d4bc", MARK="#3a2414", CLOTH="#3a5a7a"),
                  "white": dict(HAIR="#f4f2ee", FUR="#f6f4f0", LIGHT="#ffffff", MARK="#c8c4be", EYE="#c84a5a", CLOTH="#c8a8e8"),
                  "black": dict(HAIR="#1e1c22", FUR="#2a2830", LIGHT="#6a6870", MARK="#0e0c10", EYE="#e8c040", CLOTH="#a83a4a")}),
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
    if d.get("pendant"):
        out += [("pendant", 1.5, 2, .5)]
    if d.get("flute"):
        out += [("flute", 6, .75, .75), ("cord", .25, 3, .25)]
    if d.get("frill"):
        out += [("frill", 12, 8, .75), ("trihorn", 1, 4, 1), ("nosehorn", .75, 1.25, .75)]
    if d.get("sickles"):
        out += [("sickle", .5, 5, 2.25)]
    return out


def paint_hook(pt, pal, d):
    orange = G.shades("#f0a020")
    pt("yuzu", "all", lambda f, x, y, w, h: pick(orange, 0.55 + 0.25 * (f == "up") - 0.15 * (f == "down"), x, y))
    pt("yuzu_leaf", "all", lambda f, x, y, w, h: pick(G.shades("#4a9a3a"), 0.55, x, y))
    pt("coati_nose", "all", lambda f, x, y, w, h: pick(G.shades("#1e1a1c"), 0.4, x, y) if f == "north" and y == h - 1
       else pick(pal.LIGHT if f == "down" else pal.FUR, 0.55, x, y))
    frill, edge = G.shades(pal.extra.get("frill", "#5a7ac8")), G.shades(pal.extra.get("frill_edge", "#e8c060"))

    def fr(f, x, y, w, h):                               # the frill: a fan plate, scalloped and spotted along its edge
        if f not in ("north", "south"):
            return CLEAR                                 # a thin plate: no frame round it
        dx = (x + 0.5 - w / 2) / (w / 2)
        top = (h - 1) * dx * dx                          # a half-disc: the corners cut away
        if f in ("north", "south") and y < top:
            return CLEAR
        if f in ("north", "south") and y < top + 1.5:
            return pick(edge, 0.6, x, y)
        if (x * 7 + y * 5) % 13 == 0:
            return pick(pal.MARK, 0.4, x, y)             # spots
        return pick(frill, 0.55 + 0.12 * ((x + y) % 4 == 0), x, y)
    pt("frill", "all", fr)
    horn = G.shades(pal.extra.get("horn", "#f0e8d8"))
    pt("trihorn", "all", lambda f, x, y, w, h: pick(horn, 0.5 + 0.2 * (y < 2), x, y))
    pt("nosehorn", "all", lambda f, x, y, w, h: pick(horn, 0.6, x, y))
    blade = G.shades(pal.extra.get("blade", "#c8d0dc"))
    pt("sickle", "all", lambda f, x, y, w, h: pick(blade, 0.55 + 0.3 * (f in ("north", "east") and x == w - 1), x, y)
       if f not in ("east", "west") or y > h * 0.15 else CLEAR)
    amber = G.shades(pal.extra.get("amber", "#e8701e"))
    pt("pendant", "all", lambda f, x, y, w, h: pick(amber, 0.55 + 0.3 * (f == "north" and 0 < x < w - 1 and y in (2, 3)), x, y)
       if 0 < y < h - 1 or 0 < x < w - 1 else pick(G.shades("#e8c050"), 0.6, x, y))
    bam = G.shades(pal.extra.get("bamboo", "#8ab84a"))
    pt("flute", "all", lambda f, x, y, w, h: pick(G.shades("#3a2a1e"), 0.3, x, y) if f == "north" and x % 4 == 1
       else pick(bam, 0.45 + 0.2 * (x % 8 == 0), x, y))
    pt("cord", "all", lambda f, x, y, w, h: pick(G.shades("#8a5a2a"), 0.5, x, y))
    if pal.extra.get("bamboo"):                         # the badger's white stripe down her hair
        pt("hair_cap", ("up", "north"), lambda f, x, y, w, h: pick(pal.LIGHT, 0.75, x, y) if abs(x + 0.5 - w / 2) < w * 0.12 else None)
    q, tip = G.shades(pal.extra.get("quill", "#5a4030")), G.shades(pal.extra.get("quill_tip", "#e8dcc8"))
    pt("quill", "all", lambda f, x, y, w, h: pick(tip if y >= h - 1 or f == "south" else q, 0.5 + 0.15 * (x % 2), x, y))
    pt("note", "all", lambda f, x, y, w, h: pick(G.shades("#3a3a4a"), 0.45, x, y) if f == "south" and y % 2 == 1 and 0 < x < w - 1
       else pick(G.shades("#f4ecd8"), 0.65, x, y))


def build_hook(r, g, d):
    if d.get("frill"):                                   # the frill behind her head, two brow horns and a little nose horn
        hy, z = g.hy, g.z
        r.add("frill", "head", [0, hy + 6, 4.8 + z], [G.rot_cube("frill", [-6, hy + 5.0, 4.8 + z], [0, hy + 6, 4.8 + z], -25, 0, 0)])
        r.put("head", G.rot_cube("trihorn", [-2.7, hy + 8.6, -2.0 + z], [-2.2, hy + 8.6, -1.5 + z], -25, 0, 12),
              G.rot_cube("trihorn", [1.7, hy + 8.6, -2.0 + z], [2.2, hy + 8.6, -1.5 + z], -25, 0, -12),
              G.rot_cube("nosehorn", [-0.375, hy + 2.4, -4.5 + z], [0, hy + 2.4, -4.1 + z], -75, 0, 0))
    if d.get("sickles"):                                 # a sickle blade along each forearm, curving back past the elbow
        px, py, pz = r.b["forearm_r"]["pivot"]
        r.put("forearm_r", G.rot_cube("sickle", [px - 1.2, py - 4.4, pz - 0.6], [px - 1.0, py - 3.5, pz], 15, 0, 8))
    if d.get("pendant"):                                # the amber pendant on her necklace
        r.put("body", cube("pendant", [-0.75, 16.6, -G.S("chest")[2] / 2 - G.S("bust")[2] - 0.6 + g.z]))
    if d.get("flute"):                                  # the flute hangs across her chest from its cord
        fz = -G.S("chest")[2] / 2 - G.S("bust")[2] - 0.8 + g.z
        r.put("body", cube("flute", [-3, 15.2, fz]), cube("cord", [-0.125, 15.9, fz + 0.25]))
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
