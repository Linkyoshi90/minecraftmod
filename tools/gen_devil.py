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
}


def declare_hook(sp, d):
    out = []
    if d.get("pumpkin"):
        out += [("pumpkin", 9.5, 5, 9.5), ("pumpkin_f", 9.5, 3.5, .25), ("stem", 1.5, 2, 1.5), ("whole", 12, 11, 12), ("whole_f", 12, 11, .25)]
    if d.get("qing"):
        out += [("cap", 9, 3, 9), ("cap_brim", 10.5, .75, 10.5), ("button", 1.5, 1.5, 1.5), ("talisman", 3, 6, .25)]
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
    an.add("charm", 2.0, True, merge({"head": {"rotation": hold(8, 14, 6)}, "body": {"rotation": hold(0, 10, 4)}},
                                     {"arm_r": {"rotation": hold(-150, 0, 20)}, "forearm_r": {"rotation": hold(-60, 0, 0)}}))


if __name__ == "__main__":
    F.run_family("devil", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
