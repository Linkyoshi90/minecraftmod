#!/usr/bin/env python3
"""The armoured (the armadillo family, see armoured_field_guide.html): armadillo.

    python tools/gen_armoured.py [species ...] [--previews]

girl_family bipeds with banded armour down the back (`bands`), an armoured tail and tall ears. The ranger variant
wears a cowboy hat (`hat`, drawn only on her) and a red scarf. Extra animations: "curl" (rolled up in a ball, nothing
showing but the bands) and "roll" (the same ball, rolling).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

SPECIES = {
    "armadillo_armoured": dict(
        build="petite", outfit=dict(top=["bandeau"], bottom="shorts", extras=["scarf", "belt"], colors=dict(scarf="#6a7a5a", cloth2="#6a5a42")),
        hair="bob", bangs="blunt", ears="horse", legs="scales", foot="claw",
        tail=[(1.5, 1.5, 3), (1.25, 1.25, 3), (1, 1, 3)], tail_rest=[(-30, 0, 0), (12, 0, 0), (10, 0, 0)], tail_look="scale",
        pal=dict(SKIN=skin("peach"), HAIR="#8a7a6a", FUR="#a8907a", LIGHT="#e8d8c8", MARK="#6a5a4a", EYE="#2a1e16", CLOTH="#d8c8a8", TRIM="#8a6a3a",
                 pattern="bands", extra=dict(band="#8a7a68", band_edge="#5a4a3e")),
        variants={"three_banded": dict(FUR="#c8a878", HAIR="#a88a5a", extra=dict(band="#b89868", band_edge="#7a6040")),
                  "pink_fairy": dict(FUR="#f4f0ec", HAIR="#fbf8f4", LIGHT="#ffffff", MARK="#e8a8b0", extra=dict(band="#f0a8b4", band_edge="#c87888")),
                  "giant": dict(FUR="#5a4a3a", HAIR="#3a2e22", MARK="#2a2018", extra=dict(band="#4a3c2e", band_edge="#2a2018")),
                  "ranger": dict(FUR="#c8743a", HAIR="#a85a2a", MARK="#7a3a18", CLOTH="#e8d8b8", TRIM="#5a3a20",
                                 extra=dict(band="#c0682e", band_edge="#7a3a18", hat="#7a4a24", hat_band="#2a1a10", scarf="#c8282a"))}),
    # fifteenth wave: the riverdillo, a river armadillo banded in blue and gold, a fish's fin on the end of her tail
    "riverdillo_armoured": dict(
        build="petite", outfit=dict(top=["halter"], bottom="shorts", extras=["armlets", "belt"], colors=dict(cloth2="#2a4a8a")),
        hair="bob", bangs="swept", ears="horse", legs="scales", foot="claw", fin=True,
        tail=[(1.5, 1.5, 3), (1.25, 1.25, 3), (1, 1, 3)], tail_rest=[(-30, 0, 0), (12, 0, 0), (10, 0, 0)], tail_look="scale",
        pal=dict(SKIN=skin("fair"), HAIR="#2a4ab8", FUR="#4a7ad8", LIGHT="#e8eef8", MARK="#1e2e7a", EYE="#e8a030", CLOTH="#e8c050", TRIM="#2a4a8a",
                 pattern="bands", extra=dict(band="#3a5ac8", band_edge="#e0b040", fin="#5a9ae8")),
        variants={"green": dict(HAIR="#2a6a3a", FUR="#4a9a5a", MARK="#1a4a2a", extra=dict(band="#3a7a4a", band_edge="#e0c050", fin="#6ac08a")),
                  "brown": dict(HAIR="#5a3a22", FUR="#8a6444", MARK="#3a2414", extra=dict(band="#7a5434", band_edge="#d8b048", fin="#a8845a")),
                  "teal": dict(HAIR="#1e6a6a", FUR="#3a9a9a", MARK="#144a4a", extra=dict(band="#2a8080", band_edge="#e0c050", fin="#5ac0c0")),
                  "grey": dict(HAIR="#4a4a50", FUR="#7a7a84", MARK="#2a2a30", extra=dict(band="#5a5a64", band_edge="#c8c8d0", fin="#9a9aa8"))}),
    # eleventh wave: the slothren, a sloth with moss (and flowers or mushrooms) in her fur, long hooked claws; no plates
    "slothren_armoured": dict(
        build="curvy", outfit=dict(top=["halter"], bottom="sarong", extras=["pearls"], colors=dict(cloth2="#4a6a3a")),
        hair="shaggy", bangs="messy", ears="round", legs="fur", foot="claw", tail=[(1, 1, 1)], tail_rest=[(-20, 0, 0)], tail_look="fur", sloth=True,
        pal=dict(SKIN=skin("tan"), HAIR="#6a5038", FUR="#8a6a4a", LIGHT="#c8b08a", MARK="#3a2a1e", EYE="#2a1e14", CLOTH="#c8a060", TRIM="#7ab05a",
                 pattern="solid", extra=dict(nobands=True, moss="#5a9a3a")),
        variants={"pale": dict(HAIR="#b8a888", FUR="#d0c0a0", LIGHT="#f0e8d8", MARK="#7a6a50", extra=dict(nobands=True, moss="#e8eee0")),
                  "dark": dict(HAIR="#3a2a1e", FUR="#4a3a2a", LIGHT="#7a6a50", MARK="#1e140c", extra=dict(nobands=True, moss="#3a7a2a")),
                  "blossom": dict(extra=dict(nobands=True, moss="#5a9a3a", bloom="#f8a0c0")),
                  "fungal": dict(HAIR="#5a4636", extra=dict(nobands=True, moss="#7a6a4a", bloom="#c8302a"))}),
    # tenth wave: the aegis boar, plate armour on her back (the family's bands, in metal), pauldrons and bracers, tusks
    # fourteenth wave: the gribboar, a stout boar with a shaggy blue mane and little tusks, a truffle-hunter's satchel
    "gribboar_armoured": dict(
        build="curvy", outfit=dict(top=["sweater"], bottom="shorts", extras=["satchel", "belt"], colors=dict(leather="#6a4a2a", cloth2="#3a2a1e")),
        hair="mane", bangs="messy", ears="fox", legs="fur", foot="hoof", tail=[(1, 1, 2), (.75, .75, 1.5)], tail_rest=[(-40, 0, 0), (60, 0, 0)],
        tail_look="fur", boar=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e2e6a", FUR="#2a4a9a", LIGHT="#8aa0d8", MARK="#141e4a", EYE="#e8a030", CLOTH="#c8a060", TRIM="#7a5a3a",
                 pattern="solid", extra=dict(nobands=True, tusk="#f0e6cc", hoof="#1e1a2a")),
        variants={"brown": dict(HAIR="#3a2a1e", FUR="#5a4232", LIGHT="#a08870", MARK="#2a1e16", extra=dict(nobands=True, tusk="#f0e6cc", hoof="#2a2420")),
                  "black": dict(HAIR="#141210", FUR="#2a2624", LIGHT="#5a5450", MARK="#0a0808", extra=dict(nobands=True, tusk="#f0e6cc", hoof="#141210")),
                  "red": dict(HAIR="#5a1e12", FUR="#8a3a22", LIGHT="#c88a6a", MARK="#3a120a", extra=dict(nobands=True, tusk="#f0e6cc", hoof="#2a1a14")),
                  "grey": dict(HAIR="#4a4a50", FUR="#6a6a72", LIGHT="#b0b0b8", MARK="#2a2a30", extra=dict(nobands=True, tusk="#f0e6cc", hoof="#2a2a2e"))}),
    "aegis_boar_armoured": dict(
        build="strong", outfit=dict(top=["sports", "harness"], bottom="loincloth", extras=["pauldron", "bracers", "belt"],
                                    colors=dict(leather="#5a3a22", cloth2="#3a2a1e")),
        hair="mane", bangs="messy", ears="fox", legs="fur", foot="hoof", tail=[(1, 1, 2), (.75, .75, 1.5)], tail_rest=[(-40, 0, 0), (60, 0, 0)],
        tail_look="fur", boar=True,
        pal=dict(SKIN=skin("tan"), HAIR="#3a2a20", FUR="#5a4232", LIGHT="#8a705a", MARK="#2a1e16", EYE="#c88a2a", CLOTH="#b8783a", TRIM="#d8a050",
                 pattern="solid", extra=dict(band="#b8783a", band_edge="#6a4220", tusk="#f0e6cc", hoof="#2a2420")),
        variants={"iron": dict(CLOTH="#a8acb4", TRIM="#e8e8f0", extra=dict(band="#a8acb4", band_edge="#5a5e66", tusk="#f0e6cc", hoof="#2a2420")),
                  "gold": dict(CLOTH="#e0b040", TRIM="#fff0a0", extra=dict(band="#e0b040", band_edge="#8a6418", tusk="#f8f0dc", hoof="#2a2420")),
                  "blue": dict(CLOTH="#3a5aa8", TRIM="#e0c060", extra=dict(band="#3a5aa8", band_edge="#d8b048", tusk="#f0e6cc", hoof="#2a2420")),
                  "black": dict(HAIR="#141210", FUR="#2a2420", CLOTH="#2a2a30", TRIM="#8a2a2a",
                                extra=dict(band="#2a2a30", band_edge="#141418", tusk="#e8dcc0", hoof="#141210"))}),
}


def declare_hook(sp, d):
    return [("band", 7, 1.5, 3.5), ("band_top", 6, 1.5, 3), ("hat_crown", 5, 2.5, 5), ("hat_brim", 9.5, .5, 9.5), ("btusk", .75, 1.75, .75),
            ("sclaw", .5, 2.5, .5)] + ([("rfin", 4, .5, 3)] if d.get("fin") else [])


def paint_hook(pt, pal, d):
    e = pal.extra
    band, edge = G.shades(e.get("band", "#8a7a68")), G.shades(e.get("band_edge", "#5a4a3e"))
    plates = lambda f, x, y, w, h: pick(edge if y == h - 1 or x % 3 == 0 else band, 0.45 + 0.2 * (y == 0) + (noise(x, y, 4) - 0.5) * 0.1, x, y)
    pt("band", "all", (lambda *a: CLEAR) if e.get("nobands") else plates)
    pt("band_top", "all", (lambda *a: CLEAR) if e.get("nobands") else plates)
    pt("rfin", "all", lambda f, x, y, w, h: pick(G.shades(e.get("fin", "#5a9ae8")), 0.5 + 0.2 * (x % 2 == 0) - 0.1 * (y == h - 1), x, y)
       if y < h - abs(x + 0.5 - w / 2) * 0.6 else CLEAR)
    pt("sclaw", "all", lambda f, x, y, w, h: pick(G.shades("#e8e0c8"), 0.55 - 0.15 * (y == 0), x, y))
    if e.get("moss"):                                    # moss growing in her hair and fur (and a bloom or a mushroom here and there)
        moss, bloom = G.shades(e["moss"]), G.shades(e.get("bloom", e["moss"]))
        for p in [p for p in H.PARTS if p.startswith(("hair", "strand", "bang")) and p != "hair_cap"] + ["thigh", "shin", "tail_a"]:
            pt(p, "all", lambda f, x, y, w, h: pick(bloom, 0.65, x, y) if e.get("bloom") and (x * 11 + y * 7) % 31 == 0
               else pick(moss, 0.5 + 0.15 * ((x + y) % 2), x, y) if noise(x // 2, y // 2, 79) > 0.72 else None)
    if e.get("hat"):
        hat, hb = G.shades(e["hat"]), G.shades(e.get("hat_band", "#2a1a10"))
        pt("hat_crown", "all", lambda f, x, y, w, h: pick(hb if y == h - 1 and f != "up" else hat, 0.5 + (noise(x, y, 3) - 0.5) * 0.1, x, y))
        pt("hat_brim", "all", lambda f, x, y, w, h: pick(hat, 0.42 + 0.1 * (f == "up"), x, y))
    else:
        pt("hat_crown", "all", lambda *a: CLEAR)
        pt("hat_brim", "all", lambda *a: CLEAR)
    pt("btusk", "all", lambda f, x, y, w, h: pick(G.shades(e.get("tusk", "#f0e6cc")), 0.55 + 0.2 * (y == h - 1), x, y))
    if e.get("scarf"):
        pt("scarf", "all", lambda f, x, y, w, h: pick(G.shades(e["scarf"]), 0.5 + 0.15 * ((x + y) % 4 == 0), x, y))


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    cd = G.S("chest")[2]
    bands = [cube("band", [-3.5, 12.6 + i * 1.6, cd / 2 - 0.4 + z]) for i in range(4)] + [cube("band_top", [-3, 19.0, cd / 2 - 0.6 + z])]
    r.add("bands", "body", [0, 16, cd / 2 + z], bands)
    r.add("hat", "head", [0, hy + 9, z], [cube("hat_brim", [-4.75, hy + 8.9, -4.75 + z]), cube("hat_crown", [-2.5, hy + 9.4, -2.5 + z])])
    if d.get("fin") and "tail_3" in r.b:                # a flat fish's fin on the tip of her tail
        px, py, pz = r.b["tail_3"]["pivot"]
        r.put("tail_3", cube("rfin", [px - 2, py - 0.25, pz + 2.5]))
    if d.get("sloth"):                                   # three long hooked claws on each hand
        px, py, pz = r.b["hand_r"]["pivot"]
        r.put("hand_r", *[G.rot_cube("sclaw", [px - 0.75 + 0.5 * i, py - 4.0, pz - 0.6], [px, py - 1.5, pz], 15, 0, 0) for i in range(3)])
    if d.get("boar"):                                    # little tusks curling up from the corners of her mouth
        r.put("head", G.rot_cube("btusk", [-2.2, hy + 0.3, -4.5 + z], [-1.8, hy + 0.3, -4.1 + z], 0, 0, 15),
              G.rot_cube("btusk", [1.45, hy + 0.3, -4.5 + z], [1.8, hy + 0.3, -4.1 + z], 0, 0, -15))


def anim_extra(an, sp, d):
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    ball = merge({"root": {"position": hold(0, -5, 0)},
                  "body": {"rotation": hold(85, 0, 0), "position": hold(0, -2, 2)}, "head": {"rotation": hold(50, 0, 0)},
                  "leg_r": {"rotation": hold(-110, 0, 0), "position": hold(0, 3, -1)}, "leg_l": {"rotation": hold(-110, 0, 0), "position": hold(0, 3, -1)},
                  "shin_r": {"rotation": hold(140, 0, 0)}, "shin_l": {"rotation": hold(140, 0, 0)},
                  "tail": {"rotation": hold(60, 0, 0)}, "eyelids": {"position": hold(*H.OUT_)}}, sym("arm", hold(-130, 0, -15)))
    an.add("curl", 2.0, True, ball)
    roll = dict(ball)
    roll["root"] = {"position": hold(0, -5, 0), "rotation": Tk((0, (0, 0, 0)), (0.25, (-180, 0, 0)), (0.5, (-360, 0, 0)))}
    an.add("roll", 0.5, True, roll)
    # the slothren hanging from a branch by her arms, legs drawn up, asleep
    an.add("hang", 3.0, True, merge({"body": {"rotation": hold(0, 0, 0)}, "head": {"rotation": Tk((0, (14, 0, 6)), (1.5, (14, 0, -6)), (3, (14, 0, 6)))},
                                     "eyelids": {"position": hold(*H.OUT_)},
                                     "leg_r": {"rotation": hold(-70, 0, 10)}, "leg_l": {"rotation": hold(-70, 0, -10)},
                                     "shin_r": {"rotation": hold(80, 0, 0)}, "shin_l": {"rotation": hold(80, 0, 0)}},
                                    sym("arm", hold(-175, 0, -8))))


if __name__ == "__main__":
    F.run_family("armoured", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
