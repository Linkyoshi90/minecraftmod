#!/usr/bin/env python3
"""Devils (the infernal family, see devil_field_guide.html): lesser devil, succubus, greater devil, jack o'lantern, jiang shi.

    python tools/gen_devil.py [species ...] [--previews]

girl_family bipeds. The three devils have horns, membrane wings and a spade-tipped tail and fly. The jack o'lantern wears
a carved pumpkin as a hat (its face glows: glow mask) and has a `disguise` bone, a whole pumpkin on the ground that the
game shows instead of her while she hides. The jiang shi wears a Qing official's hat with a paper talisman over her face
and holds her arms out in front of her ("hop").
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

SPADE = [(.75, .75, 4), (.5, .5, 4), (2, 2, .5)]
SPADE_REST = [(-20, 0, 0), (25, 0, 0), (10, 0, 0)]

SPECIES = {
    "lesser_devil": dict(
        build="petite", outfit=dict(top=["sports"], bottom="shorts", extras=["belt"]), hair="twintails", bangs="messy", ears="elf",
        legs="bare", foot="shoe", tail=SPADE, tail_look="spade", tail_rest=SPADE_REST, horns="devil", wings="devil_small", flies=True,
        pal=dict(SKIN="#e87a6a", HAIR="#2a1418", FUR="#e87a6a", LIGHT="#f8c0b0", MARK="#3a0e14", EYE="#f8d83a", CLOTH="#2a1418", TRIM="#f8d83a",
                 pupil=(0.9, 3.4), extra=dict(horn="#2a1418", wing="#8a1a2a", wing_bone="#3a0e14", tail_col="#e87a6a", boot="#2a1418")),
        variants={"purple": dict(SKIN="#a87ad8", FUR="#a87ad8", extra=dict(horn="#2a1430", wing="#5a2a8a", wing_bone="#2a1440", tail_col="#a87ad8")),
                  "blue": dict(SKIN="#6a9ae8", FUR="#6a9ae8", extra=dict(horn="#141e3a", wing="#2a3a8a", wing_bone="#141e3a", tail_col="#6a9ae8")),
                  "pink": dict(SKIN="#f4a8c8", FUR="#f4a8c8", HAIR="#f8f0f4", extra=dict(horn="#a83a6a", wing="#d85a8a", wing_bone="#7a1a4a", tail_col="#f4a8c8")),
                  "black": dict(SKIN="#4a4248", FUR="#4a4248", HAIR="#c82a3a", extra=dict(horn="#c82a3a", wing="#2a2428", wing_bone="#0e0c0e", tail_col="#4a4248"))}),
    "succubus_devil": dict(
        build="curvy", outfit=dict(top=["corset", "halter"], bottom="wrap_skirt", extras=["pearls"], colors=dict(cloth2="#1a0e14")),
        hair="wavy", bangs="swept", ears="elf", legs="stockings", foot="shoe", tail=SPADE, tail_look="spade", tail_rest=SPADE_REST,
        horns="ram", wings="bat", flies=True,
        pal=dict(SKIN=skin("pale"), HAIR="#8a1a3a", FUR="#8a1a3a", LIGHT="#f8d0d8", MARK="#3a0a18", EYE="#e83a7a", CLOTH="#1a0e14", TRIM="#e83a7a",
                 pupil=(1.6, 2.4), extra=dict(horn="#1a0e14", wing="#8a1a3a", wing_bone="#2a0a14", tail_col="#1a0e14", stocking="#1a0e14", boot="#1a0e14")),
        variants={"violet": dict(SKIN=skin("lilac"), HAIR="#4a2a6a", extra=dict(horn="#1a0e24", wing="#5a2a8a", wing_bone="#1a0e24", tail_col="#1a0e24", stocking="#1a0e24", boot="#1a0e24")),
                  "rose": dict(HAIR="#f0a0b8", CLOTH="#c83a6a", extra=dict(horn="#f8e0e8", wing="#e87aa0", wing_bone="#8a2a4a", tail_col="#c83a6a", stocking="#c83a6a", boot="#c83a6a")),
                  "night": dict(HAIR="#e8e8f0", CLOTH="#141418", TRIM="#c8c8d8", extra=dict(horn="#c8c8d8", wing="#2a2a34", wing_bone="#0e0e12", tail_col="#141418", stocking="#141418", boot="#141418")),
                  "azure": dict(SKIN=skin("blue"), HAIR="#1a2a5a", extra=dict(horn="#0e1430", wing="#2a4a9a", wing_bone="#0e1430", tail_col="#0e1430", stocking="#0e1430", boot="#0e1430"))}),
    "greater_devil": dict(
        build="strong", outfit=dict(top=["harness", "bandeau"], bottom="loincloth", extras=["pauldron", "bracers"], colors=dict(cloth2="#1a0a0a")),
        hair="mane", bangs="jagged", ears="elf", legs="scales", foot="hoof", tail=[(1, 1, 5), (.75, .75, 5), (2.5, 2.5, .5)], tail_look="spade",
        tail_rest=SPADE_REST, horns="greater", wings="bat", wing_scale=1.45, flies=True,
        pal=dict(SKIN="#c84a3a", HAIR="#1a0e0e", FUR="#3a1a14", LIGHT="#e88a6a", MARK="#1a0a0a", EYE="#f8e83a", CLOTH="#1a0a0a", TRIM="#e8b040",
                 pupil=(0.9, 3.4), extra=dict(horn="#1a1010", horn_tip="#4a3a3a", wing="#5a1a1a", wing_bone="#1a0a0a", tail_col="#c84a3a", hoof="#1a1010")),
        variants={"obsidian": dict(SKIN="#3a3238", FUR="#1a161a", extra=dict(horn="#8a2a8a", wing="#2a1a2a", wing_bone="#0e0a0e", tail_col="#3a3238", hoof="#0e0a0e")),
                  "ash": dict(SKIN="#8a8682", FUR="#4a4644", extra=dict(horn="#1a1818", wing="#3a3634", wing_bone="#1a1818", tail_col="#8a8682", hoof="#1a1818")),
                  "gold": dict(extra=dict(horn="#e8b040", horn_tip="#fff0a0", wing="#5a1a1a", wing_bone="#1a0a0a", tail_col="#c84a3a", hoof="#1a1010")),
                  "violet": dict(SKIN="#7a4aa8", FUR="#2a1a3a", extra=dict(horn="#1a0e24", wing="#3a1a5a", wing_bone="#14081e", tail_col="#7a4aa8", hoof="#14081e"))}),
    "jack_o_lantern_devil": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], bottom="wrap_skirt", colors=dict(cloth2="#1e1a1e")),
        hair="bob", bangs="blunt", ears=None, legs="stockings", foot="shoe", pumpkin=True, glow=["pumpkin_f"],
        pal=dict(SKIN=skin("fair"), HAIR="#e8782a", FUR="#e8782a", LIGHT="#f8c87a", MARK="#2a1e14", EYE="#f8a82a", CLOTH="#e8782a", TRIM="#2a1e1e",
                 extra=dict(pumpkin="#e8822a", candle="#f8d83a", stocking="#e8782a", boot="#1e1a1e")),
        variants={"white": dict(extra=dict(pumpkin="#ece4d8", candle="#f8d83a", stocking="#ece4d8", boot="#1e1a1e"), HAIR="#f4ece0", CLOTH="#d8d0c4"),
                  "green": dict(extra=dict(pumpkin="#6a8a3a", candle="#c8f04a", stocking="#6a8a3a", boot="#1e1a1e"), HAIR="#4a6a2a", CLOTH="#4a6a2a"),
                  "blue": dict(extra=dict(pumpkin="#e8822a", candle="#4ae8f8", stocking="#2a8aa8", boot="#1e1a1e"), EYE="#4ae8f8", CLOTH="#2a5a7a"),
                  "black": dict(extra=dict(pumpkin="#2a2628", candle="#f8582a", stocking="#2a2628", boot="#0e0c0e"), HAIR="#1e1a1e", CLOTH="#2a2628")}),
    "jiang_shi_devil": dict(
        build="slim", outfit=dict(top=["kimono"], bottom="hakama", colors=dict(kimono="#2a4a8a", kimono_trim="#e8c040", obi="#e8c040", cloth2="#2a3a6a")),
        hair="odango", bangs="blunt", ears=None, legs="bare", foot="shoe", qing=True,
        pal=dict(SKIN="#c8d8e0", HAIR="#1a1a22", FUR="#c8d8e0", LIGHT="#ffffff", MARK="#8aa0b0", EYE="#c83a3a", CLOTH="#2a4a8a", TRIM="#e8c040",
                 extra=dict(boot="#1a1a22", hat="#1a1a22")),
        variants={"red": dict(extra=dict(kimono="#a81a2a", obi="#e8c040", kimono_trim="#e8c040", cloth2="#7a1a2a", hat="#1a1a22")),
                  "green": dict(extra=dict(kimono="#2a6a4a", obi="#e8c040", kimono_trim="#e8c040", cloth2="#1e4a34", hat="#1a1a22")),
                  "black": dict(extra=dict(kimono="#1a1a22", obi="#a81a2a", kimono_trim="#a81a2a", cloth2="#141418", hat="#1a1a22")),
                  "gold": dict(extra=dict(kimono="#c8a030", obi="#a81a2a", kimono_trim="#a81a2a", cloth2="#8a6a1a", hat="#1a1a22"))}),
    # fourteenth wave: the ittan-momen, a girl who ends at the waist in a long ribbon of cotton cloth (no legs); she flies
    "ittan_momen_devil": dict(
        build="petite", outfit=dict(top=["kimono"], extras=[], colors=dict(kimono="#f4f2ec", kimono_trim="#c8c4bc", obi="#3a5ab0")),
        hair="flowing", bangs="swept", ears=None, lower="none", ribbon=True,
        pal=dict(SKIN=skin("pale"), HAIR="#f0eee8", FUR="#f0eee8", LIGHT="#ffffff", MARK="#b8b4ac", EYE="#3a5ab0", CLOTH="#f4f2ec", TRIM="#3a5ab0",
                 extra=dict(cotton="#f4f2ec", fold="#c8c4bc")),
        variants={"indigo": dict(HAIR="#2a3a7a", extra=dict(cotton="#2a3a8a", fold="#1a2458", kimono="#2a3a8a", kimono_trim="#e8e8f0", obi="#f4f2ec")),
                  "red": dict(HAIR="#a82a2a", extra=dict(cotton="#c83a3a", fold="#7a1a1a", kimono="#c83a3a", kimono_trim="#f4f2ec", obi="#2a2a32")),
                  "gold": dict(HAIR="#d8a840", extra=dict(cotton="#e8c060", fold="#a87a2a", kimono="#e8c060", kimono_trim="#7a5410", obi="#2a3a8a")),
                  "black": dict(HAIR="#1e1c22", EYE="#e83a3a", extra=dict(cotton="#2a2830", fold="#141218", kimono="#2a2830", kimono_trim="#8a8890", obi="#c83a3a"))}),
    # thirteenth wave: the chochin-obake, a girl under a living paper lantern (it glows) with one great eye and a lolling tongue
    "chochin_obake_devil": dict(
        build="petite", outfit=dict(top=["kimono"], bottom="hakama", extras=[],
                                    colors=dict(kimono="#f4f0e8", kimono_trim="#c83a3a", obi="#c83a3a", cloth2="#2a2a32")),
        hair="hime", bangs="blunt", ears=None, legs="bare", foot="shoe", lantern=True, glow=["lantern"], glow_boost=1.6,
        pal=dict(SKIN=skin("pale"), HAIR="#1e1a22", FUR="#1e1a22", LIGHT="#f4f0e8", MARK="#c83a3a", EYE="#e8a030", CLOTH="#f4f0e8", TRIM="#c83a3a",
                 extra=dict(paper="#f8e8c0", band="#c83a3a", rim="#1e1a1e", boot="#2a2a32")),
        variants={"white": dict(extra=dict(paper="#fbf8f0", band="#2a2a32", rim="#1e1a1e", boot="#2a2a32")),
                  "blue": dict(HAIR="#1e2a4a", extra=dict(paper="#c8dcf8", band="#2a4a9a", rim="#14182a", boot="#2a2a32", kimono="#2a4a9a", obi="#f8e070")),
                  "purple": dict(HAIR="#2a1e3a", extra=dict(paper="#e8d0f4", band="#6a2a9a", rim="#1e1428", boot="#2a2a32", kimono="#6a2a9a", obi="#f8e070")),
                  "gold": dict(HAIR="#3a2a14", extra=dict(paper="#fff0b0", band="#d8902a", rim="#3a2410", boot="#2a2a32", kimono="#d8902a", obi="#2a2a32"))}),
    # twelfth wave: the gremlin, a little green tinkerer with huge ears and a spanner
    "gremlin_devil": dict(
        build="petite", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["belt"], colors=dict(cloth2="#3a4a2a", leather="#5a3a22")),
        hair="wavy", bangs="parted", ears="big", legs="bare", foot="shoe", spanner=True,
        pal=dict(SKIN="#b8c890", HAIR="#5a8ad8", FUR="#b8c890", LIGHT="#e0e8c0", MARK="#7a8a5a", EYE="#3a8a4a", CLOTH="#7a9a5a", TRIM="#c8a040",
                 INNER="#e8b0a0", extra=dict(boot="#5a3a22")),
        variants={"blue": dict(SKIN="#98b8d8", FUR="#98b8d8", LIGHT="#d0e0f0", MARK="#5a7a9a", HAIR="#2a4a8a", CLOTH="#4a6a9a"),
                  "yellow": dict(SKIN="#e0d090", FUR="#e0d090", LIGHT="#f4ecc0", MARK="#a8984a", HAIR="#d86a2a", CLOTH="#c8902a"),
                  "grey": dict(SKIN="#a8a8a8", FUR="#a8a8a8", LIGHT="#d8d8d8", MARK="#6a6a6a", HAIR="#3a3a3e", CLOTH="#5a5a62"),
                  "purple": dict(SKIN="#c0a8d0", FUR="#c0a8d0", LIGHT="#e4d8ec", MARK="#7a5a8a", HAIR="#5a2a7a", CLOTH="#7a4a9a")}),
    # twelfth wave: the karakasa obake, a girl under an umbrella hat, a thin tail curled like its handle, hopping on one leg ("onehop")
    "karakasa_obake_devil": dict(
        build="petite", outfit=dict(top=["kimono"], bottom="hakama", extras=[],
                                    colors=dict(kimono="#3a6ac8", kimono_trim="#f4f0e8", obi="#c83a3a", cloth2="#2a4a8a")),
        hair="bob", bangs="blunt", ears=None, legs="bare", foot="shoe", tail=[(.75, .75, 4), (.75, .75, 4), (.75, .75, 3)],
        tail_rest=[(-20, 0, 0), (-40, 0, 0), (-70, 0, 0)], tail_look="fur", umbrella=True,
        pal=dict(SKIN=skin("fair"), HAIR="#3a6ac8", FUR="#3a6ac8", LIGHT="#a8c8f0", MARK="#2a4a8a", EYE="#3a5ab8", CLOTH="#3a6ac8", TRIM="#f4f0e8",
                 extra=dict(canopy="#4a7ad8", rib="#2a3a6a", boot="#3a2a20")),
        variants={"red": dict(HAIR="#c83a3a", FUR="#c83a3a", MARK="#7a1a1a", EYE="#c83a3a",
                              extra=dict(canopy="#d84a3a", rib="#5a1a14", kimono="#c83a3a", obi="#2a2a2a", cloth2="#8a2a2a", boot="#3a2a20")),
                  "purple": dict(HAIR="#7a4ab8", FUR="#7a4ab8", MARK="#4a2a7a", EYE="#8a4ac8",
                                 extra=dict(canopy="#8a5ac8", rib="#3a1e5a", kimono="#7a4ab8", obi="#f8e070", cloth2="#4a2a7a", boot="#3a2a20")),
                  "green": dict(HAIR="#3a8a5a", FUR="#3a8a5a", MARK="#1e5a3a", EYE="#3a8a4a",
                                extra=dict(canopy="#4a9a6a", rib="#1e4a2e", kimono="#3a8a5a", obi="#c83a3a", cloth2="#1e5a3a", boot="#3a2a20")),
                  "white": dict(HAIR="#f0eef0", FUR="#f0eef0", MARK="#a8a4b0", EYE="#c84a5a",
                                extra=dict(canopy="#f4f2f0", rib="#8a8490", kimono="#f4f2f0", obi="#c83a3a", cloth2="#c8c4d0", boot="#3a2a20"))}),
    # ninth wave: the oni, big and horned, a tiger-skin loincloth and an iron club (kanabo) in her right hand
    "oni_devil": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps", "belt"], colors=dict(cloth2="#e8a830")),
        hair="shaggy", bangs="messy", ears="elf", legs="bare", foot="shoe", horns="devil", club=True,
        pal=dict(SKIN="#d8584a", HAIR="#1e1a1e", FUR="#d8584a", LIGHT="#f0a090", MARK="#8a2a22", EYE="#f8d040", CLOTH="#f4f0e6", TRIM="#2a2420",
                 extra=dict(horn="#f4ecd8", boot="#3a2a20", tiger=True)),
        variants={"blue": dict(SKIN="#5a7ad0", FUR="#5a7ad0", LIGHT="#a0b8f0", MARK="#2a3a8a", HAIR="#f4f0e6"),
                  "yellow": dict(SKIN="#e8c05a", FUR="#e8c05a", LIGHT="#f8e0a0", MARK="#a87a2a", HAIR="#3a2a1a", EYE="#d83a2a"),
                  "green": dict(SKIN="#6aa85a", FUR="#6aa85a", LIGHT="#a8d898", MARK="#2a5a2a", HAIR="#e8e0c8"),
                  "black": dict(SKIN="#4a4448", FUR="#4a4448", LIGHT="#8a8488", MARK="#1e1a1e", HAIR="#e8e4e0", EYE="#f84a2a")}),
}


def declare_hook(sp, d):
    out = []
    if d.get("pumpkin"):
        out += [("pumpkin", 9.5, 5, 9.5), ("pumpkin_f", 9.5, 3.5, .25), ("stem", 1.5, 2, 1.5), ("whole", 12, 11, 12), ("whole_f", 12, 11, .25)]
    if d.get("club"):                                    # the kanabo: a long iron club with studs, and tiger stripes on her loincloth
        out += [("club", 2.25, 11, 2.25), ("grip", 1.25, 3.5, 1.25), ("stud", .5, .5, .5)]
    if d.get("qing"):
        out += [("cap", 9, 3, 9), ("cap_brim", 10.5, .75, 10.5), ("button", 1.5, 1.5, 1.5), ("talisman", 3, 6, .25)]
    if d.get("spanner"):
        out += [("spanner", .75, 4, .75), ("spanner_head", 2, 1, .75)]
    if d.get("ribbon"):
        out += [("cloth_a", 6, 6, .5), ("cloth_b", 5.5, 6, .5), ("cloth_c", 5, 6, .5), ("cloth_d", 4.5, 5, .5)]
    if d.get("lantern"):
        out += [("lantern", 8.5, 8, 8.5), ("lantern_rim", 6.5, 1, 6.5), ("tongue", 2, .5, 3.5)]
    if d.get("umbrella"):
        out += [("canopy", 14, 1, 14), ("canopy_b", 10.5, 1.25, 10.5), ("canopy_c", 7, 1.25, 7), ("canopy_top", 3.5, 1, 3.5), ("umb_tip", 1, 2, 1)]
    return out


def paint_hook(pt, pal, d):
    pk = G.shades(pal.extra.get("pumpkin", "#e8822a"))
    candle = G.shades(pal.extra.get("candle", "#f8d83a"))
    ribs = lambda f, x, y, w, h: pick(pk, 0.5 + 0.18 * (x % 4 == 0) + (noise(x, y, 3) - 0.5) * 0.08, x, y)
    pt("pumpkin", "all", ribs)
    pt("whole", "all", ribs)
    pt("stem", "all", lambda f, x, y, w, h: pick(G.shades("#4a6a2a"), 0.5, x, y))

    def face(f, x, y, w, h, eyes_y=0.25, mouth_y=0.7):
        if f not in ("north",):
            return ribs(f, x, y, w, h)
        cx = w / 2
        eye = abs(y - h * eyes_y) < h * 0.12 and (abs(x - w * 0.3) < w * 0.08 + (h * eyes_y - y) * 0.3 or abs(x - w * 0.7) < w * 0.08 + (h * eyes_y - y) * 0.3)
        mouth = abs(y - h * mouth_y) < h * 0.08 and abs(x - cx) < w * 0.3 and not (x % 5 == 0 and y < h * mouth_y)
        return pick(candle, 0.8, x, y) if eye or mouth else CLEAR
    pt("pumpkin_f", "all", lambda f, x, y, w, h: face(f, x, y, w, h, 0.3, 0.75))
    pt("whole_f", "all", lambda f, x, y, w, h: face(f, x, y, w, h))
    iron = G.shades("#4a4a52")
    pt("club", "all", lambda f, x, y, w, h: pick(iron, 0.4 + 0.2 * (x == 0) + (noise(x, y, 13) - 0.5) * 0.1, x, y))
    pt("grip", "all", lambda f, x, y, w, h: pick(G.shades("#6a2a1a"), 0.45 + 0.2 * (y % 2), x, y))
    pt("stud", "all", lambda f, x, y, w, h: pick(G.shades("#9a9aa2"), 0.7, x, y))
    if pal.extra.get("tiger"):                           # the tiger-skin loincloth
        black = G.shades("#1e1a14")
        for p in ("flap_f", "flap_b"):
            pt(p, "all", lambda f, x, y, w, h: pick(black, 0.4, x, y) if (y + x // 2) % 4 == 0 else None)
    paper, band, rim = G.shades(pal.extra.get("paper", "#f8e8c0")), G.shades(pal.extra.get("band", "#c83a3a")), G.shades(pal.extra.get("rim", "#1e1a1e"))

    def lantern(f, x, y, w, h):                           # ribbed paper; on the front one great eye and a wide mouth
        if f in ("up", "down"):
            return pick(rim, 0.4, x, y)
        if f == "north":
            ex, ey = w * 0.5, h * 0.4
            d2 = ((x + 0.5 - ex) / (w * 0.22)) ** 2 + ((y + 0.5 - ey) / (h * 0.17)) ** 2
            if d2 < 1.0:
                return pick(rim, 0.15, x, y) if d2 < 0.22 else pick(G.shades("#ffffff"), 0.9, x, y) if d2 < 0.75 else pick(rim, 0.25, x, y)
            if abs(y + 0.5 - h * 0.74) < h * 0.05 and abs(x + 0.5 - w / 2) < w * 0.34:
                return pick(G.shades("#7a1420"), 0.35, x, y)
        if y % 4 == 0:
            return pick(rim, 0.35, x, y)                  # the bamboo ribs
        return pick(band if (y // 4) % 3 == 1 else paper, 0.55 + 0.15 * noise(x, y, 31), x, y)
    pt("lantern", "all", lantern)
    pt("lantern_rim", "all", lambda f, x, y, w, h: pick(rim, 0.45 + 0.15 * (f == "up"), x, y))
    pt("tongue", "all", lambda f, x, y, w, h: pick(G.shades("#e85a6a"), 0.55 + 0.2 * (f == "up"), x, y))
    cotton, fold = G.shades(pal.extra.get("cotton", "#f4f2ec")), G.shades(pal.extra.get("fold", "#c8c4bc"))
    for p in ("cloth_a", "cloth_b", "cloth_c", "cloth_d"):
        pt(p, "all", lambda f, x, y, w, h: pick(fold if x % 5 == 2 or y % 6 == 5 else cotton, 0.55 + 0.1 * (noise(x, y, 41) - 0.5), x, y))
    steel = G.shades("#9a9aa6")
    pt("spanner", "all", lambda f, x, y, w, h: pick(G.shades("#c83a3a") if y >= h - 5 else steel, 0.55, x, y))
    pt("spanner_head", "all", lambda f, x, y, w, h: CLEAR if f in ("north", "south") and x in (w // 2 - 1, w // 2) and y < h // 2 else pick(steel, 0.6, x, y))
    can, rib = G.shades(pal.extra.get("canopy", "#4a7ad8")), G.shades(pal.extra.get("rib", "#2a3a6a"))

    def canopy(f, x, y, w, h):                            # oiled paper panels between bamboo ribs; darker underneath
        if f == "down":
            return pick(can, 0.3, x, y) if (x - y) % 13 and (x + y) % 13 else pick(rib, 0.4, x, y)
        if f == "up":
            dx, dy = x + 0.5 - w / 2, y + 0.5 - h / 2
            return pick(rib, 0.45, x, y) if abs(dx) < 0.6 or abs(dy) < 0.6 or abs(abs(dx) - abs(dy)) < 0.7 else pick(can, 0.55 + 0.1 * (abs(dx) + abs(dy) < w * 0.3), x, y)
        return pick(rib, 0.35, x, y) if y == h - 1 or x % 13 == 0 else pick(can, 0.5, x, y)
    pt("canopy", "all", canopy)
    pt("canopy_b", "all", canopy)
    pt("canopy_c", "all", canopy)
    pt("canopy_top", "all", lambda f, x, y, w, h: pick(can, 0.62, x, y))
    pt("umb_tip", "all", lambda f, x, y, w, h: pick(rib, 0.5, x, y))
    hat = G.shades(pal.extra.get("hat", "#1a1a22"))
    pt("cap", "all", lambda f, x, y, w, h: pick(hat, 0.45 + 0.1 * (x % 3 == 0), x, y))
    pt("cap_brim", "all", lambda f, x, y, w, h: pick(hat, 0.35, x, y))
    pt("button", "all", lambda f, x, y, w, h: pick(G.shades("#c83a3a"), 0.6, x, y))
    paper = G.shades("#f0d870")

    def talisman(f, x, y, w, h):
        if f not in ("north", "south"):
            return pick(paper, 0.6, x, y)
        if abs(x - w / 2) < 0.6 and 2 <= y <= h - 3 or (y in (3, 7, 11) and abs(x - w / 2) < w * 0.3):
            return pick(G.shades("#b81a1a"), 0.5, x, y)
        return pick(paper, 0.7 + (noise(x, y, 9) - 0.5) * 0.1, x, y)
    pt("talisman", "all", talisman)


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    if d.get("pumpkin"):                                 # a carved pumpkin worn as a hat, the candle shining through
        r.add("pumpkin_hat", "head", [0, hy + 8, z], [cube("pumpkin", [-4.75, hy + 7.4, -4.75 + z]), cube("pumpkin_f", [-4.75, hy + 8.2, -4.85 + z]),
                                                    cube("stem", [-0.75, hy + 12.3, -0.75 + z])])
        r.add("disguise", "root", [0, 0, 0], [cube("whole", [-6, 0, -6]), cube("whole_f", [-6, 0, -6.1]), cube("stem", [-0.75, 11, -0.75])])
    if d.get("club"):                                    # held upright in her right hand, resting against her shoulder
        px, py, pz = r.b["hand_r"]["pivot"]
        cz = pz - 2.4
        studs = [cube("stud", [px - 1.45 + 2.4 * (i % 2), py + 1.5 + 2.2 * (i // 2), cz - 0.25]) for i in range(8)]
        studs += [cube("stud", [px - 0.25, py + 2.6 + 2.2 * i, cz - 1.45]) for i in range(3)]
        r.add("club", "hand_r", [px, py - 1.5, pz], [cube("grip", [px - 0.625, py - 2.5, cz - 0.625]), cube("club", [px - 1.125, py + 1.0, cz - 1.125])] + studs)
    if d.get("ribbon"):                                  # the long cotton ribbon she ends in, in four bones so it can ripple
        z0 = z
        r.add("ribbon_1", "body", [0, 12.5, z0], [cube("cloth_a", [-3, 6.5, -0.25 + z0])])
        r.add("ribbon_2", "ribbon_1", [0, 6.5, z0], [cube("cloth_b", [-2.75, 0.5, -0.25 + z0])])
        r.add("ribbon_3", "ribbon_2", [0, 0.5, z0], [cube("cloth_c", [-2.5, -5.5, -0.25 + z0])])
        r.add("ribbon_4", "ribbon_3", [0, -5.5, z0], [cube("cloth_d", [-2.25, -10.5, -0.25 + z0])])
    if d.get("lantern"):                                 # the lantern rests on her head like a tall hat; the tongue hangs from its mouth
        r.add("lantern", "head", [0, hy + 8, z], [cube("lantern", [-4.25, hy + 8.6, -4.25 + z]), cube("lantern_rim", [-3.25, hy + 8.0, -3.25 + z]),
                                                    cube("lantern_rim", [-3.25, hy + 16.6, -3.25 + z]),
                                                    G.rot_cube("tongue", [-1, hy + 9.6, -6.6 + z], [0, hy + 10.6, -4.4 + z], 35, 0, 0)])
    if d.get("spanner"):                                 # a spanner in her right hand
        px, py, pz = r.b["hand_r"]["pivot"]
        r.add("spanner", "hand_r", [px, py - 1.5, pz], [cube("spanner", [px - 0.375, py - 4.5, pz - 2.2]), cube("spanner_head", [px - 1.0, py - 5.4, pz - 2.2])])
    if d.get("umbrella"):                                # the umbrella hat, wide and peaked
        r.add("umbrella", "head", [0, hy + 9, z], [cube("canopy", [-7, hy + 8.4, -7 + z]), cube("canopy_b", [-5.25, hy + 9.4, -5.25 + z]),
                                                    cube("canopy_c", [-3.5, hy + 10.65, -3.5 + z]), cube("canopy_top", [-1.75, hy + 11.9, -1.75 + z]),
                                                    cube("umb_tip", [-0.5, hy + 12.9, -0.5 + z])])
    if d.get("qing"):
        r.add("qing_hat", "head", [0, hy + 8, z], [cube("cap", [-4.5, hy + 7.6, -4.5 + z]), cube("cap_brim", [-5.25, hy + 7.4, -5.25 + z]),
                                                 cube("button", [-0.75, hy + 10.6, -0.75 + z]), cube("talisman", [-1.5, hy + 2.2, -5.6 + z])])


def anim_extra(an, sp, d):
    """the jiang shi's hop (arms out, both feet together), the lesser devil's prank giggle ("laugh"), the succubus' charm ("charm")"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("hop", 0.8, True, merge({"root": {"position": Tk((0, (0, 0, 0)), (0.2, (0, -0.5, 0)), (0.4, (0, 3, 0)), (0.7, (0, 0, 0)), (0.8, (0, 0, 0)))},
                                    "leg_r": {"rotation": hold(0, 0, 0)}, "leg_l": {"rotation": hold(0, 0, 0)}}, sym("arm", hold(-90, 0, 0)), sym("forearm", hold(0, 0, 0))))
    if d.get("qing"):                                    # arms always out in front of her
        for key in ("idle", "walk", "run", "stalk"):
            F._inject(an, key, merge(sym("arm", hold(-90, 0, 0)), sym("forearm", hold(0, 0, 0))))
    an.add("laugh", 1.2, True, merge({"body": {"position": Tk((0, (0, 0, 0)), (0.15, (0, 0.5, 0)), (0.3, (0, 0, 0)), (0.45, (0, 0.5, 0)), (0.6, (0, 0, 0)), (1.2, (0, 0, 0)))},
                                      "head": {"rotation": hold(-14, 0, 0)}, "eyelids": {"position": hold(*H.OUT_)}, "mouth": {"position": hold(*H.OUT_)}},
                                     sym("arm", hold(-30, 0, 20)), sym("forearm", hold(-90, 0, 0))))
    if d.get("pumpkin"):                                 # the disguise stays shrunk away unless the game shows it (see GirlRenderer)
        for key in [k.split(".")[-1] for k in an.A]:
            F._inject(an, key, {"disguise": {"scale": hold(0, 0, 0)}})
    if d.get("ribbon"):                                  # the ribbon trails out behind her, rippling; flying, it streams straight back
        wave = lambda a, ph: Tk((0, (a, 0, 0)), (1.0, (a + 12 * ph, 0, 4)), (2.0, (a, 0, 0)))
        rest = {"ribbon_1": {"rotation": wave(40, 1)}, "ribbon_2": {"rotation": wave(25, -1)}, "ribbon_3": {"rotation": wave(20, 1)},
                "ribbon_4": {"rotation": wave(15, -1)}, "root": {"position": Tk((0, (0, 1.5, 0)), (1.0, (0, 2.2, 0)), (2.0, (0, 1.5, 0)))}}
        for key in [k.split(".")[-1] for k in an.A]:
            F._inject(an, key, rest)
        an.add("fly", 1.0, True, merge(G.upper_idle(), {"body": {"rotation": hold(20, 0, 0)},
                                                        "ribbon_1": {"rotation": Tk((0, (60, 0, 0)), (0.5, (70, 0, 0)), (1.0, (60, 0, 0)))},
                                                        "ribbon_2": {"rotation": Tk((0, (10, 0, 0)), (0.5, (-10, 0, 0)), (1.0, (10, 0, 0)))},
                                                        "ribbon_3": {"rotation": Tk((0, (-10, 0, 0)), (0.5, (10, 0, 0)), (1.0, (-10, 0, 0)))},
                                                        "ribbon_4": {"rotation": Tk((0, (10, 0, 0)), (0.5, (-15, 0, 0)), (1.0, (10, 0, 0)))}},
                                       sym("arm", hold(-20, 0, 25))))
    if d.get("spanner"):                                 # the gremlin's big ears stick out sideways
        for key in [k.split(".")[-1] for k in an.A]:
            F._inject(an, key, {"ear_r": {"rotation": hold(0, 0, 65)}, "ear_l": {"rotation": hold(0, 0, -65)}})
    an.add("onehop", 0.7, True, merge({"root": {"position": Tk((0, (0, 0, 0)), (0.15, (0, -0.5, 0)), (0.35, (0, 2.5, 0)), (0.6, (0, 0, 0)), (0.7, (0, 0, 0)))},
                                       "leg_r": {"rotation": hold(0, 0, 0)}, "leg_l": {"rotation": hold(-40, 0, 0)}, "shin_l": {"rotation": hold(90, 0, 0)}},
                                      sym("arm", hold(-20, 0, 35))))
    if d.get("umbrella"):                                # the karakasa obake goes everywhere on one leg
        for key in ("walk", "run"):
            an.A[f"animation.{sp}.{key}"] = an.A[f"animation.{sp}.onehop"]
    an.add("charm", 2.0, True, merge({"head": {"rotation": hold(8, 14, 6)}, "body": {"rotation": hold(0, 10, 4)}},
                                     {"arm_r": {"rotation": hold(-150, 0, 20)}, "forearm_r": {"rotation": hold(-60, 0, 0)}}))


if __name__ == "__main__":
    F.run_family("devil", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
