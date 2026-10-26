#!/usr/bin/env python3
"""Canids (the canid family, with the bears, see canid_field_guide.html): wolf, polar bear, panda, Anubis.

    python tools/gen_canid.py [species ...] [--previews]

girl_family bipeds with fur legs and paws. The wolf has pointed ears and a bushy tail, the bears round ears and a stub
of a tail, the panda dark patches round her eyes (facemark "panda"), Anubis tall jackal ears and a long thin tail.
Extra animations: "howl" (the wolf), "fish" (the polar bear, crouched at the water), "roll" and "sneeze" (the panda),
"judge" (Anubis holds up the scales).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

SPECIES = {
    "wolf_canid": dict(
        mummy_kit=True,
        build="athletic", outfit=dict(top=["bandeau", "vest"], bottom="shorts", extras=["arm_wraps", "belt"], colors=dict(leather="#5a4a3a")),
        hair="shaggy", bangs="messy", ears="fox", legs="fur", foot="paw", tail=[(2, 2, 4), (2.25, 2.25, 4), (1.75, 1.75, 3)],
        tail_rest=[(-25, 0, 0), (10, 0, 0), (12, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#d8d4cc", FUR="#c8c4bc", LIGHT="#f0eee8", MARK="#6a665e", EYE="#e8b030", CLOTH="#5a4a3a", TRIM="#8a7a6a",
                 pattern="solid", pupil=(1.6, 2.6)),
        variants={"woods": dict(FUR="#7a6248", HAIR="#5a4632", LIGHT="#c8b8a0", MARK="#3a2e22"),
                  "snowy": dict(FUR="#eef0f2", HAIR="#ffffff", LIGHT="#ffffff", MARK="#a8acb4", EYE="#6ab0e8"),
                  "ashen": dict(FUR="#8a8c90", HAIR="#6a6c72", LIGHT="#c0c2c6", MARK="#3a3c40"),
                  "rusty": dict(FUR="#b0663a", HAIR="#8a4a26", LIGHT="#e0b890", MARK="#4a2a18"),
                  # homage to Gregory Horror Show (2026-10-25): Mummy Dog, bandaged head, red shirt, blue overalls, a cleaver in his head
                  "mummy": dict(SKIN=skin("#b8703e"), FUR="#b06e3e", HAIR="#ece6d4", LIGHT="#d8a070", MARK="#5a2e10", EYE="#1e8a4a", CLOTH="#c81e24",
                                extra=dict(wrapped=True, blood=True, blade="cleaver", leather="#1a78b0", cloth2="#1a78b0", shoes="#1a5a8a", legs="#1a78b0"))}),
    # tenth wave: the meloncollie, a collie in watermelon colours (rind-green top, melon-red shorts), half a melon on her back
    "meloncollie_canid": dict(
        build="petite", outfit=dict(top=["halter"], bottom="shorts", extras=["bell_collar"], colors=dict(cloth2="#e84a5a")),
        hair="ponytail", bangs="swept", ears="fox", legs="fur", foot="paw", tail=[(2.25, 2.25, 4), (2.5, 2.5, 4), (2, 2, 3)],
        tail_rest=[(-30, 0, 0), (12, 0, 0), (14, 0, 0)], tail_look="fur", melon=True,
        pal=dict(SKIN=skin("fair"), HAIR="#7a4a2a", FUR="#f4f0e8", LIGHT="#ffffff", MARK="#8a5432", EYE="#3a2a1a", CLOTH="#4a9a3a", TRIM="#f8d040",
                 pattern="patches", pupil=(1.6, 2.6), extra=dict(rind="#4a9a3a", flesh="#e84a5a")),
        variants={"golden": dict(CLOTH="#c8b83a", extra=dict(rind="#a8b83a", flesh="#f8d040", cloth2="#f8d040")),
                  "black": dict(CLOTH="#1e3a1e", extra=dict(rind="#1e3a1e", flesh="#e83a4a", cloth2="#e83a4a")),
                  "tricolour": dict(HAIR="#1e1a18", MARK="#1e1a18", LIGHT="#f0e0c8", extra=dict(rind="#4a9a3a", flesh="#e84a5a", cloth2="#e84a5a")),
                  "seedless": dict(CLOTH="#a8d890", extra=dict(rind="#a8d890", flesh="#f88a9a", cloth2="#f88a9a", seedless=True))}),
    "polar_bear_canid": dict(
        build="strong", outfit=dict(top=["halter", "mantle"], bottom="loincloth", extras=["scarf"], colors=dict(scarf="#3a6a9a")),
        hair="bob", bangs="blunt", ears="round", legs="fur", foot="paw", tail=[(1.75, 1.5, 1.5)], tail_rest=[(-20, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("pale"), HAIR="#f4f2ec", FUR="#f0ece2", LIGHT="#ffffff", MARK="#c8c2b4", EYE="#2a2a32", CLOTH="#3a5a7a", TRIM="#e8e4dc",
                 pattern="solid"),
        variants={"cream": dict(FUR="#efe2c4", HAIR="#f2e6cc", MARK="#c8b894"),
                  "grey": dict(FUR="#c8c8c8", HAIR="#dcdcdc", MARK="#8a8a8a", CLOTH="#5a3a3a"),
                  "pizzly": dict(FUR="#c8a882", HAIR="#e8dcc8", MARK="#8a6a4a", CLOTH="#2a4a3a"),
                  "ice": dict(FUR="#e4f0f8", HAIR="#d0e8f8", MARK="#a8c8e0", EYE="#5ab0e8", CLOTH="#2a4a6a")}),
    "panda_canid": dict(
        build="curvy", outfit=dict(top=["bandeau", "vest"], bottom="sarong", extras=["bell_collar"], colors=dict(cloth2="#c83a3a")),
        hair="bob", bangs="blunt", ears="round", legs="fur", foot="paw", tail=[(1.5, 1.5, 1.25)], tail_rest=[(-20, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#f4f2ee", FUR="#1e1c20", LIGHT="#3a383c", MARK="#1e1c20", EYE="#3a2a1e", CLOTH="#3a8a4a", TRIM="#e8c050",
                 pattern="solid", facemark="panda"),
        variants={"brown": dict(FUR="#5a3e2a", LIGHT="#7a5a42", MARK="#5a3e2a", HAIR="#ece0d0"),
                  "cream": dict(FUR="#a89070", LIGHT="#c8b090", MARK="#a89070", HAIR="#f8f2e6"),
                  "grey": dict(FUR="#4a4a50", LIGHT="#6a6a70", MARK="#4a4a50", HAIR="#e8e8ea", CLOTH="#3a5a8a"),
                  "red": dict(FUR="#8a3a1e", LIGHT="#b05a30", MARK="#8a3a1e", HAIR="#f4e8d8", CLOTH="#2a6a6a")}),
    "anubis_canid": dict(
        mummy_kit=True,
        build="athletic", outfit=dict(top=["bandeau"], bottom="wrap_skirt", extras=["usekh", "armlets", "bracers"], colors=dict(cloth2="#e8c050")),
        hair="hime", bangs="blunt", ears="big", legs="fur", foot="paw", scales=True, tail=[(1.25, 1.25, 4), (1.25, 1.25, 4), (1, 1, 3)],
        tail_rest=[(-50, 0, 0), (8, 0, 0), (8, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("brown"), HAIR="#141214", FUR="#1a181c", LIGHT="#3a363c", MARK="#e8c050", EYE="#e8c030", CLOTH="#f0ece0", TRIM="#e8c050",
                 pattern="solid", pupil=(1.4, 2.8)),
        variants={"gold": dict(FUR="#c8984a", HAIR="#3a2614", LIGHT="#e8c88a", MARK="#7a5a2a", CLOTH="#2a4a8a"),
                  "silver": dict(FUR="#8a8a90", HAIR="#d8d8dc", LIGHT="#c0c0c4", MARK="#e8c050"),
                  "sand": dict(FUR="#c8b088", HAIR="#5a4630", LIGHT="#e8d8b8", MARK="#8a6a40"),
                  "night": dict(FUR="#1a1e30", HAIR="#0e1020", LIGHT="#3a4060", MARK="#5ab0e8", EYE="#5ab0e8", TRIM="#5ab0e8"),
                  # homage to Gregory Horror Show (2026-10-25): Mummy Papa, bandaged head, grey suit, a machete in his head
                  "mummy": dict(SKIN=skin("#9a5e30"), FUR="#9a5e30", HAIR="#ece6d4", LIGHT="#c8905a", MARK="#5a2e10", EYE="#2a6ac8", CLOTH="#8c8c88",
                                TRIM="#6a6a72", extra=dict(wrapped=True, blood=True, blade="machete", cloth2="#4e4448", suit=True, shoes="#141414", legs="#4e4448"))}),
    "kobold_canid": dict(
        build="petite", outfit=dict(top=["sports", "harness"], bottom="shorts", extras=["satchel", "belt"], colors=dict(leather="#5a3a22")),
        hair="twintails", bangs="messy", ears="lop", legs="fur", foot="paw", tail=[(1.5, 1.5, 3), (1.75, 1.75, 3)], tail_rest=[(-30, 0, 0), (15, 0, 0)],
        tail_look="fur", helmet=True,
        pal=dict(SKIN=skin("peach"), HAIR="#8a5a32", FUR="#a8784a", LIGHT="#e8c89a", MARK="#5a3a1e", EYE="#3a7ac8", CLOTH="#4a5a6a", TRIM="#c8a050",
                 pattern="solid"),
        variants={"cobalt": dict(FUR="#3a5aa8", HAIR="#2a3a7a", LIGHT="#a8c0f0", MARK="#1a2a5a", EYE="#e8b030"),
                  "black": dict(FUR="#2a2624", HAIR="#141210", LIGHT="#6a6260", MARK="#0a0808"),
                  "cream": dict(FUR="#e8d8b8", HAIR="#d8c098", LIGHT="#fff8e8", MARK="#a89070"),
                  "red": dict(FUR="#a84a2a", HAIR="#7a2e18", LIGHT="#e8a07a", MARK="#4a1a0a")}),
    # fourteenth wave: the cu sith, the great green fairy hound, her tail curled up over her back
    "cu_sith_canid": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts", extras=["belt", "scarf"], colors=dict(scarf="#e8dcb0", cloth2="#2a3a2a")),
        hair="shaggy", bangs="messy", ears="fox", legs="fur", foot="paw", tail=[(1.75, 1.75, 4), (1.5, 1.5, 4), (1.25, 1.25, 3)],
        tail_rest=[(-70, 0, 0), (45, 0, 0), (40, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#1e4a2a", FUR="#2a5a34", LIGHT="#7aa880", MARK="#0e2a16", EYE="#e8e870", CLOTH="#e8dcb0", TRIM="#1e4a2a",
                 pattern="solid"),
        variants={"moss": dict(HAIR="#4a6a2a", FUR="#5a7a34", LIGHT="#a8c080", MARK="#2a3a14"),
                  "dark": dict(HAIR="#0e1a12", FUR="#16241a", LIGHT="#3a4a3e", MARK="#060c08", EYE="#a8f0a0"),
                  "pale": dict(HAIR="#8ab894", FUR="#9ac8a4", LIGHT="#e0f0e2", MARK="#4a6a50", EYE="#2a4a2a"),
                  "teal": dict(HAIR="#1e5a5a", FUR="#2a6a6a", LIGHT="#80b8b8", MARK="#0e2e2e")}),
    # fourteenth wave: the yeti, a huge shaggy snow woman with a blue-grey face
    "yeti_canid": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps"], colors=dict(bandage="#c8d8e8", cloth2="#7a8a9a")),
        hair="mane", bangs="messy", ears="round", legs="fur", foot="paw", tail=[(1.5, 1.25, 1.25)], tail_rest=[(-20, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("#a8b8cc"), HAIR="#f4f6fa", FUR="#eef2f8", LIGHT="#ffffff", MARK="#a8b4c4", EYE="#3a6ad8", CLOTH="#5a7aa8", TRIM="#c8d8e8",
                 pattern="solid"),
        variants={"grey": dict(HAIR="#a8acb4", FUR="#b8bcc4", LIGHT="#e0e2e8", MARK="#6a6e76"),
                  "blue": dict(HAIR="#c8dcf8", FUR="#b8d0f4", LIGHT="#eef6ff", MARK="#6a8ac8"),
                  "golden": dict(HAIR="#e8d4a8", FUR="#e0c890", LIGHT="#fff4dc", MARK="#a88a50", SKIN=skin("#c8b0a0")),
                  "brown": dict(HAIR="#8a6a4e", FUR="#9a7a5a", LIGHT="#d8c0a4", MARK="#5a4028", SKIN=skin("#8a7064"))}),
    "black_bear_canid": dict(
        build="curvy", outfit=dict(top=["sports"], bottom="shorts", extras=["belt", "scarf"], colors=dict(scarf="#c85a2a", cloth2="#4a3a2a")),
        hair="shaggy", bangs="messy", ears="round", legs="fur", foot="paw", tail=[(1.5, 1.25, 1.25)], tail_rest=[(-20, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("tan"), HAIR="#141212", FUR="#1e1a1a", LIGHT="#a8805a", MARK="#0a0808", EYE="#5a3a1a", CLOTH="#c85a2a", TRIM="#e8c060",
                 pattern="solid"),
        variants={"cinnamon": dict(FUR="#8a4a24", HAIR="#6a3418", LIGHT="#d8a070", MARK="#4a2410"),
                  "glacier": dict(FUR="#6a7480", HAIR="#4a5460", LIGHT="#b8c0c8", MARK="#2a3038", CLOTH="#3a6a9a"),
                  "kermode": dict(FUR="#f0ece0", HAIR="#f8f4ec", LIGHT="#ffffff", MARK="#c8c0b0", CLOTH="#3a7a4a"),
                  "brown": dict(FUR="#5a3e26", HAIR="#3e2a18", LIGHT="#a8805a", MARK="#2a1a0e")}),
    "grizzly_canid": dict(
        build="strong", outfit=dict(top=["sports", "vest"], bottom="loincloth", extras=["satchel", "arm_wraps"], colors=dict(leather="#4a3a2a", bandage="#c8b898")),
        hair="shaggy", bangs="messy", ears="round", legs="fur", foot="paw", tail=[(1.75, 1.5, 1.5)], tail_rest=[(-20, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("tan"), HAIR="#5a3a22", FUR="#6a4a2e", LIGHT="#a8805a", MARK="#3a2414", EYE="#3a2412", CLOTH="#4a5a3a", TRIM="#a8987a",
                 pattern="solid"),
        variants={"cinnamon": dict(FUR="#9a5a2e", HAIR="#7a3e1e", LIGHT="#d0905a"),
                  "silvertip": dict(FUR="#7a6a5a", HAIR="#c8c0b0", LIGHT="#d8d0c0", pattern="speckle"),
                  "dark": dict(FUR="#3a2a1e", HAIR="#1e140c", LIGHT="#5a4636"),
                  "blond": dict(FUR="#b8946a", HAIR="#d8bc8a", LIGHT="#ecdcb8")}),
}


def melon_parts(d):
    return [("melon", 6, 5, 3.5), ("melon_cut", 6, 5, .25), ("mflower", 1.5, 1.5, .5)] if d.get("melon") else []


def paint_melon(pt, pal):
    rind, flesh = G.shades(pal.extra.get("rind", "#4a9a3a")), G.shades(pal.extra.get("flesh", "#e84a5a"))
    pt("melon", "all", lambda f, x, y, w, h: pick(rind, 0.35 + 0.3 * ((x // 2) % 2) + (noise(x, y, 47) - 0.5) * 0.08, x, y))
    pt("melon_cut", "all", lambda f, x, y, w, h: pick(rind, 0.6, x, y) if x in (0, w - 1) or y in (0, h - 1)
       else pick(G.shades("#1e1410"), 0.4, x, y) if (x * 3 + y * 5) % 7 == 0 and not pal.extra.get("seedless") else pick(flesh, 0.55, x, y))
    pt("mflower", "all", lambda f, x, y, w, h: pick(G.shades("#f8a0c0"), 0.6 + 0.2 * ((x + y) % 2), x, y))


def build_melon(r, g):
    hy, z = g.hy, g.z                                    # half a watermelon worn on her back, cut face outward; a flower behind her ear
    r.add("melon", "body", [0, 16, 2 + z], [cube("melon", [-3, 13.5, 1.6 + z]), cube("melon_cut", [-3, 13.5, 5.1 + z])])
    r.put("head", cube("mflower", [2.6, hy + 6.4, -2.2 + z]))


def declare_hook(sp, d):
    if d.get("melon"):
        return melon_parts(d)
    if d.get("mummy_kit"):                              # the mummy variants' muzzle and the blade in the head
        out = [("muzzle", 3.5, 2.25, 3.5), ("machete_blade", 14, 3, .5), ("machete_guard", .75, 3, 2), ("machete_grip", 4, 1.25, 1.25),
               ("cleaver_blade", 5, 4.5, .5), ("cleaver_handle", 1, 1, 7)]
        return out + ([("scale_beam", 5, .5, .5), ("scale_pan", 1.5, .25, 1.5)] if d.get("scales") else [])
    if d.get("helmet"):
        return [("helmet", 5, 2, 5), ("helmet_brim", 6, .5, 6), ("lamp", 1.5, 1.5, .5)]
    return [("scale_beam", 5, .5, .5), ("scale_pan", 1.5, .25, 1.5)] if d.get("scales") else []


def paint_hook(pt, pal, d):
    paint_melon(pt, pal)
    gold = G.shades("#e8c050")
    pt("scale_beam", "all", lambda f, x, y, w, h: pick(gold, 0.6, x, y))
    pt("scale_pan", "all", lambda f, x, y, w, h: pick(gold, 0.7, x, y))
    tin = G.shades("#e8c040")                                          # the kobold's miner's helmet and its lamp
    pt("helmet", "all", lambda f, x, y, w, h: pick(tin, 0.5 + 0.25 * (f == "up"), x, y))
    pt("helmet_brim", "all", lambda f, x, y, w, h: pick(tin, 0.4, x, y))
    pt("lamp", "all", lambda f, x, y, w, h: pick(G.shades("#fff4c0"), 0.85, x, y))
    e = pal.extra
    mummy, blade = e.get("wrapped"), e.get("blade")
    show = lambda on, fn: (lambda f, x, y, w, h: fn(f, x, y, w, h) if on else CLEAR)
    wrap, steel, ink = G.shades("#ece6d4"), G.shades("#b8c8d4"), G.shades("#1a1414")

    def bandage(f, x, y, w, h):                         # bandage turns, with a shadow where one overlaps the next
        return pick(wrap, 0.25 if (2 * x + 5 * y) % 11 == 0 else 0.6 + 0.12 * ((y // 3) % 2), x, y)
    fur = lambda f, x, y, w, h: pick(pal.FUR, 0.5 + 0.1 * (f == "up"), x, y)
    pt("muzzle", "all", show(mummy, lambda f, x, y, w, h: pick(ink, 0.2, x, y) if f in ("north", "east", "west") and y == h - 3
                             else bandage(f, x, y, w, h) if f == "east" and x > w * 0.5 else fur(f, x, y, w, h)))
    pt("machete_blade", "all", show(blade == "machete", lambda f, x, y, w, h: pick(steel, 0.45 if y >= h - 1 - (x // 3) % 2 else 0.7, x, y)))
    pt("machete_guard", "all", show(blade == "machete", lambda f, x, y, w, h: pick(G.shades("#8a8a92"), 0.5, x, y)))
    pt("machete_grip", "all", show(blade == "machete", lambda f, x, y, w, h: pick(G.shades("#7a4a1a"), 0.4 + 0.2 * ((x // 2) % 2), x, y)))
    pt("cleaver_blade", "all", show(blade == "cleaver", lambda f, x, y, w, h: pick(steel, 0.55 + 0.2 * (y == 0) - 0.15 * (y == h - 1), x, y)))
    pt("cleaver_handle", "all", show(blade == "cleaver", lambda f, x, y, w, h: pick(G.shades("#c81e24"), 0.5 + 0.15 * (f == "up"), x, y)))
    if mummy:
        # a bandaged head: only a band of fur shows round the eyes
        pt("head", ("north",), lambda f, x, y, w, h: None if h * 0.34 <= y < h * 0.62 else bandage(f, x, y, w, h))
        pt("head", ("south", "east", "west", "up"), bandage)
        for p in ("tail_a", "tail_b", "tail_c"):
            pt(p, ("north", "south", "east", "west", "up"), bandage)
        legs, shoes = G.shades(e.get("legs", "#4e4448")), G.shades(e.get("shoes", "#141414"))
        for p in ("thigh", "shin"):
            pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(legs, 0.45 + 0.08 * (x % 5 == 0), x, y))
        pt("paw", "all", lambda f, x, y, w, h: pick(shoes, 0.35 + 0.12 * (f == "up"), x, y))
        top = pal.CLOTH
        pt("arm_up", ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(top, 0.5, x, y))          # sleeves
        if e.get("suit"):                               # Papa's grey suit jacket, closed over the chest, lapels and all
            for p in ("chest", "midriff", "bust", "hips"):
                pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(G.shades("#6a6a72"), 0.35, x, y)
                   if f == "north" and abs(abs(x + 0.5 - w / 2) - (h - y) * 0.35) < 0.6 else pick(top, 0.5, x, y))
        else:                                           # the dog's overalls: blue bib and gold buttons over the red shirt
            denim = G.shades(e.get("legs", "#1a78b0"))
            for p in ("midriff", "hips"):
                pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(G.shades("#f0c020"), 0.6, x, y)
                   if f == "north" and y <= 1 and x in (2, w - 3) else pick(denim, 0.5, x, y))

    if e.get("blood"):                                  # the homage's blood: where the blade went in, running down the face
        gore = G.shades("#b0101a")

        def drips(cols, f_ok=("north",)):
            def fn(f, x, y, w, h):
                if f not in f_ok:
                    return None
                for c, length in cols:
                    cx, end = int(w * c), int(h * length)
                    if (x in (cx, cx + 1) and y < end) or (y in (end, end + 1) and cx - 1 <= x <= cx + 2):
                        return pick(gore, 0.45 + 0.15 * (x == cx), x, y)
                return None
            return fn
        pt("head", ("up",), lambda f, x, y, w, h: pick(gore, 0.5, x, y) if abs(y - h // 2) <= 1 and w * 0.2 < x < w * 0.8 else None)
        pt("head", ("north",), drips([(0.36, 0.55), (0.46, 0.85), (0.56, 0.4)]))
        pt("muzzle", ("up", "north"), lambda f, x, y, w, h: pick(gore, 0.5, x, y) if (x, y) in {(3, 2), (4, 2), (9, 4), (10, 4), (10, 5)} else None)
        pt("machete_blade", ("north", "south"), lambda f, x, y, w, h: pick(gore, 0.5, x, y) if (x * 5 + y * 3) % 23 == 0 and x < w * 0.6 else None)
        pt("cleaver_blade", ("north", "south"), lambda f, x, y, w, h: pick(gore, 0.5, x, y) if 2 <= x <= 4 and 3 <= y <= 6 else None)
        if e.get("suit"):                               # Papa: it runs on down his shirt and spatters his suit
            pt("chest", ("north",), drips([(0.48, 1.0)]))
            for p in ("midriff", "hips"):
                pt(p, ("north",), lambda f, x, y, w, h: pick(gore, 0.5, x, y) if (x, y) in {(2, 3), (3, 3), (2, 4), (w - 4, 6), (w - 3, 6)} else None)


def build_hook(r, g, d):
    if d.get("melon"):
        build_melon(r, g)
    if d.get("mummy_kit"):                              # a muzzle; a machete (Papa) or a cleaver (the dog) stuck in the top of the head
        hy, z = g.hy, g.z
        r.put("head", cube("muzzle", [-1.75, hy + 0.3, -7.3 + z]))
        r.add("blade", "head", [0, hy + 8, z], [G.rot_cube("machete_blade", [-9, hy + 7.0, -0.25 + z], [0, hy + 8, z], 0, 0, -12),
                                               G.rot_cube("machete_guard", [4.6, hy + 7.0, -1 + z], [0, hy + 8, z], 0, 0, -12),
                                               G.rot_cube("machete_grip", [5.3, hy + 7.9, -0.625 + z], [0, hy + 8, z], 0, 0, -12),
                                               G.rot_cube("cleaver_blade", [-2.5, hy + 6.5, -0.25 + z], [0, hy + 8, z], 0, 0, 8),
                                               G.rot_cube("cleaver_handle", [1.4, hy + 9.6, -0.5 + z], [0, hy + 8, z], -20, 30, 8)])
    if d.get("helmet"):
        r.add("helmet", "head", [0, g.hy + 8, g.z], [cube("helmet", [-2.5, g.hy + 8.0, -2.5 + g.z]), cube("helmet_brim", [-3, g.hy + 7.8, -3 + g.z]),
                                                    cube("lamp", [-0.75, g.hy + 8.3, -3.2 + g.z])])
    if d.get("scales"):                                                 # Anubis carries the scales of judgement at her hip
        r.add("scales", "body", [0, 13, -2.5], [cube("scale_beam", [-2.5, 12.0, -3.0]), cube("scale_pan", [-2.75, 11.5, -3.5]),
                                                 cube("scale_pan", [1.25, 11.5, -3.5])])


def anim_extra(an, sp, d):
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("howl", 2.0, False, merge({"head": {"rotation": Tk((0, (0, 0, 0)), (0.5, (-55, 0, 0)), (2.0, (-55, 0, 0)))},
                                      "body": {"rotation": hold(-8, 0, 0)}, "mouth": {"position": Tk((0, H.REST), (0.4, H.OUT_), (2.0, H.OUT_))}},
                                     sym("arm", hold(10, 0, 10))))
    an.add("fish", 3.0, True, merge({"root": {"position": hold(0, -4.5, 0)}, "body": {"rotation": hold(40, 0, 0)}, "head": {"rotation": hold(20, 0, 0)},
                                     "leg_r": {"rotation": hold(-80, 0, 0)}, "leg_l": {"rotation": hold(-80, 0, 0)},
                                     "shin_r": {"rotation": hold(110, 0, 0)}, "shin_l": {"rotation": hold(110, 0, 0)},
                                     "arm_r": {"rotation": Tk((0, (-60, 0, 10)), (1.0, (-100, 0, 10)), (1.5, (-30, 0, 10)), (3.0, (-60, 0, 10)))},
                                     "arm_l": {"rotation": hold(-50, 0, -10)}}))
    an.add("roll", 1.0, True, merge({"root": {"rotation": Tk((0, (0, 0, 0)), (0.5, (180, 0, 0)), (1.0, (360, 0, 0))), "position": hold(0, 5, 0)},
                                     "leg_r": {"rotation": hold(-90, 0, 0)}, "leg_l": {"rotation": hold(-90, 0, 0)},
                                     "shin_r": {"rotation": hold(120, 0, 0)}, "shin_l": {"rotation": hold(120, 0, 0)}},
                                    sym("arm", hold(-120, 0, 30))))
    an.add("sneeze", 1.0, False, merge({"head": {"rotation": Tk((0, (0, 0, 0)), (0.4, (-20, 0, 0)), (0.55, (25, 0, 0)), (1.0, (0, 0, 0)))},
                                        "body": {"rotation": Tk((0, (0, 0, 0)), (0.4, (-6, 0, 0)), (0.55, (10, 0, 0)), (1.0, (0, 0, 0)))},
                                        "eyelids": {"position": Tk((0, H.REST), (0.3, H.OUT_), (0.8, H.OUT_), (1.0, H.REST))},
                                        "mouth": {"position": Tk((0, H.REST), (0.45, H.OUT_), (0.65, H.REST))}}))
    an.add("point", 1.5, False, merge({"head": {"rotation": hold(-5, -20, 0)}, "body": {"rotation": hold(0, -15, 0)}},
                                      {"arm_r": {"rotation": Tk((0, (0, 0, 0)), (0.3, (-90, -20, 0)), (1.5, (-90, -20, 0)))}}))
    an.add("judge", 2.0, False, merge({"scales": {"position": Tk((0, (0, 0, 0)), (0.5, (-3, 9, -2)), (2.0, (-3, 9, -2)))},
                                       "head": {"rotation": hold(-5, 0, 0)}},
                                      {"arm_r": {"rotation": Tk((0, (0, 0, 0)), (0.5, (-150, 0, 0)), (2.0, (-150, 0, 0)))}}))


if __name__ == "__main__":
    F.run_family("canid", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
