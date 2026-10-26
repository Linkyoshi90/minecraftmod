#!/usr/bin/env python3
"""Jackalopes (the rabbit family, see jackalope_field_guide.html): Inaba, Jack, Easter Bunny, Al-mi'raj, volcano rabbit.

    python tools/gen_jackalope.py [species ...] [--previews]

The girl_family biped with long rabbit feet, rabbit ears (lop ears for the Easter Bunny, small round ones for the
volcano rabbit) and a cotton-puff tail. Jack wears antlers (bone `horns`), the Al-mi'raj a single spiral horn; the
Easter Bunny carries a basket of painted eggs (on hand_r). Five colours per species.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin  # noqa: E402

PUFF = [(2.5, 2.5, 2.25)]

SPECIES = {
    "inaba_jackalope": dict(
        build="slim", outfit=dict(top=["kimono"], colors=dict(kimono="#f4f2fa", kimono_trim="#d8a83a", obi="#c83a4a")),
        hair="hime", bangs="blunt", ears="rabbit", legs="fur", foot="long", tail=PUFF, tail_look="puff",
        pal=dict(SKIN=skin("pale"), HAIR="#f2f0f4", FUR="#f6f6f4", LIGHT="#ffffff", MARK="#d8d2e0", EYE="#d8a030", CLOTH="#f0eef8", TRIM="#d8b040",
                 INNER="#f2b4c4"),
        variants={"harvest": dict(FUR="#ecc878", HAIR="#f0d08a", LIGHT="#fbecc0", MARK="#c89a48", EYE="#c86a2a"),
                  "blood": dict(FUR="#e8a0a4", HAIR="#d86a74", LIGHT="#f8d8da", MARK="#b84a54", EYE="#c82a3a", extra=dict(obi="#2a1a2a")),
                  "blue": dict(FUR="#d4e0f2", HAIR="#b8cce8", LIGHT="#f0f4fc", MARK="#8aa0c8", EYE="#4a7ad8"),
                  "eclipse": dict(FUR="#2a2830", HAIR="#1c1a22", LIGHT="#4a4852", MARK="#c8c8d8", EYE="#e8e0a0", INNER="#8a6a7a")}),
    "jack_jackalope": dict(
        build="athletic", outfit=dict(top=["bandeau", "vest"], bottom="shorts", extras=["belt", "bracers"], colors=dict(leather="#7a4a2a")),
        hair="shaggy", bangs="messy", ears="rabbit", legs="fur", foot="long", tail=PUFF, tail_look="puff", horns="antlers",
        pal=dict(SKIN=skin("tan"), HAIR="#6a4a2e", FUR="#8a6a48", LIGHT="#e8dcc6", MARK="#4a3424", EYE="#7a4a1e", CLOTH="#c8b08a", TRIM="#d0a050",
                 INNER="#d8a090", extra=dict(horn="#e2d4b4")),
        variants={"grey": dict(FUR="#8a8886", HAIR="#5a5856", LIGHT="#e0dedc", MARK="#3a3836"),
                  "tan": dict(FUR="#c8a678", HAIR="#a07a4e", LIGHT="#f2e6d0", MARK="#7a5a3a"),
                  "snowshoe": dict(FUR="#f2f2f0", HAIR="#e8e6e2", LIGHT="#ffffff", MARK="#a8a6a2", extra=dict(horn="#b8a080")),
                  "black": dict(FUR="#2a2624", HAIR="#1a1614", LIGHT="#5a5450", MARK="#100c0a", extra=dict(horn="#f0e8d8"))}),
    "easter_jackalope": dict(
        build="petite", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["bell_collar"], colors=dict(cloth2="#f8f0a8")),
        hair="twintails", bangs="swept", ears="lop", legs="fur", foot="long", tail=PUFF, tail_look="puff", basket=True,
        pal=dict(SKIN=skin("peach"), HAIR="#f4c0d0", FUR="#f8d4de", LIGHT="#fff4f6", MARK="#e8a8b8", EYE="#5a9ad8", CLOTH="#a8e0c8", TRIM="#f8d870",
                 INNER="#f0a0b4"),
        variants={"mint": dict(FUR="#cceedd", HAIR="#a8e0c4", LIGHT="#f0fbf6", MARK="#88c8a8", CLOTH="#f4b8c8"),
                  "lavender": dict(FUR="#e0d4f0", HAIR="#c8b4e4", LIGHT="#f6f2fc", MARK="#a890c8", CLOTH="#f8e090"),
                  "butter": dict(FUR="#f8eebc", HAIR="#f2e08a", LIGHT="#fffbe8", MARK="#d8c070", CLOTH="#a8c8f0"),
                  "sky": dict(FUR="#cfe4f6", HAIR="#a8ccee", LIGHT="#f2f8fe", MARK="#88aed8", CLOTH="#f4c0d8")}),
    "al_miraj_jackalope": dict(
        build="athletic", outfit=dict(top=["halter"], bottom="sarong", extras=["usekh", "armlets"], colors=dict(cloth2="#e8d8b0")),
        hair="braid", bangs="parted", ears="rabbit", legs="fur", foot="long", tail=PUFF, tail_look="puff", horns="unicorn",
        pal=dict(SKIN=skin("brown"), HAIR="#c89a48", FUR="#e0b45a", LIGHT="#f8e8c0", MARK="#a8783a", EYE="#3a8a6a", CLOTH="#3a6aa8", TRIM="#e8c050",
                 INNER="#e8a888", extra=dict(horn="#1e1a18", horn_tip="#3a3430")),
        variants={"sand": dict(FUR="#e8d4a8", HAIR="#d8c090", LIGHT="#faf2de", MARK="#b8a070"),
                  "white": dict(FUR="#f6f2ea", HAIR="#f0ece2", LIGHT="#ffffff", MARK="#c8c0b0", extra=dict(horn="#d8a830", horn_tip="#f0d070")),
                  "rufous": dict(FUR="#b8643a", HAIR="#8a4424", LIGHT="#ecc8a8", MARK="#6a3018"),
                  "dusk": dict(FUR="#8a7a8e", HAIR="#5a4a62", LIGHT="#d8d0dc", MARK="#3a2e42", CLOTH="#a8582a")}),
    "volcano_jackalope": dict(
        build="petite", outfit=dict(top=["sports"], bottom="shorts", extras=["scarf"], colors=dict(scarf="#d84a2a")),
        hair="bob", bangs="wispy", ears="round", legs="fur", foot="paw", tail=[(1.75, 1.75, 1.5)], tail_look="puff",
        pal=dict(SKIN=skin("tan"), HAIR="#5a3e2a", FUR="#6e5038", LIGHT="#c8b090", MARK="#3a2a1e", EYE="#2a1a10", CLOTH="#3a3a3e", TRIM="#e87a2a",
                 INNER="#c89080"),
        variants={"dark": dict(FUR="#3e2e24", HAIR="#2a1e16", LIGHT="#8a7464", MARK="#1e140e"),
                  "ash": dict(FUR="#7a7672", HAIR="#5a5652", LIGHT="#c8c4c0", MARK="#3a3632"),
                  "cinder": dict(FUR="#7a3e2a", HAIR="#5a2a1a", LIGHT="#d89070", MARK="#e86a2a"),
                  "golden": dict(FUR="#b8945a", HAIR="#9a7440", LIGHT="#f0dcb0", MARK="#6a4e2a")}),
    # tenth wave: the clockhare, a top hat between her ears, a waistcoat, a brass pocket watch on a chain at her hip
    "clockhare_jackalope": dict(
        build="slim", outfit=dict(top=["sweater", "vest"], bottom="shorts", extras=["belt"], colors=dict(leather="#3a2a3a", cloth2="#2a2430")),
        hair="bob", bangs="blunt", ears="rabbit", legs="fur", foot="long", tail=PUFF, tail_look="puff", clock=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e1a1c", FUR="#2a2628", LIGHT="#f0ece6", MARK="#141214", EYE="#c8902a", CLOTH="#f0ece6", TRIM="#d8a840",
                 INNER="#e0a8a8", extra=dict(hat="#1e1a22", band="#8a2a3a", watch="#d8a840")),
        variants={"white": dict(HAIR="#f4f2ee", FUR="#f6f4f0", LIGHT="#ffffff", MARK="#c8c4be", EYE="#c84a5a", CLOTH="#3a3a5a",
                                extra=dict(hat="#2a2a3a", band="#c8c8d8", watch="#d0d4dc")),
                  "brown": dict(HAIR="#5a3a24", FUR="#7a5236", LIGHT="#e8d4bc", MARK="#3a2416", CLOTH="#e8dcc0", extra=dict(hat="#3a2a1e", band="#2a5a3a", watch="#c87a4a")),
                  "grey": dict(HAIR="#5a5a5e", FUR="#7a7a80", LIGHT="#e0e0e4", MARK="#3a3a3e", CLOTH="#e8e0f0", extra=dict(hat="#2a2430", band="#6a3a8a", watch="#f0d060")),
                  "spotted": dict(HAIR="#1e1a1c", FUR="#f4f2ee", MARK="#1e1a1c", pattern="patches", CLOTH="#c8d8e8",
                                  extra=dict(hat="#1e1a22", band="#3a6aa8", watch="#d8a840"))}),
    # ninth wave: the wolpertinger, antlers and small feathered wings she glides on (the "fly" animation)
    "wolpertinger_jackalope": dict(
        build="petite", outfit=dict(top=["halter", "vest"], bottom="shorts", extras=["belt", "scarf"],
                                    colors=dict(leather="#3a5a2a", cloth2="#3a5a2a", scarf="#c83a2a")),
        hair="twintails", bangs="jagged", ears="rabbit", legs="fur", foot="long", tail=PUFF, tail_look="puff", horns="antlers",
        wings="feather", wing_scale=0.5, flies=True,
        pal=dict(SKIN=skin("fair"), HAIR="#7a5432", FUR="#9a7048", LIGHT="#ecdcc0", MARK="#4a3020", EYE="#3a7a3a", CLOTH="#f0ece0", TRIM="#c8a040",
                 INNER="#e0a8a0", extra=dict(horn="#e8dcc0", wing="#a8683a", wing_tip="#3a2414")),
        variants={"grey": dict(FUR="#8a8682", HAIR="#6a6660", LIGHT="#e4e0dc", MARK="#3a3836", extra=dict(horn="#e8dcc0", wing="#7a7a72", wing_tip="#2a5a4a")),
                  "white": dict(FUR="#f4f2ee", HAIR="#ece8e2", LIGHT="#ffffff", MARK="#b8b4ae", EYE="#c84a5a",
                                extra=dict(horn="#c8b490", wing="#f4f2ee", wing_tip="#a8a49e")),
                  "red": dict(FUR="#c8622e", HAIR="#a84a22", LIGHT="#f0d0b0", MARK="#6a2a12", extra=dict(horn="#e8dcc0", wing="#c8622e", wing_tip="#2a1a10")),
                  "black": dict(FUR="#2a2628", HAIR="#1a1618", LIGHT="#5a5458", MARK="#0e0c0e", EYE="#c8a030",
                                extra=dict(horn="#f0e8d8", wing="#1e1a22", wing_tip="#3a3a5a"))}),
}


def declare_hook(sp, d):
    if d.get("clock"):
        return [("hat_brim", 6, .5, 6), ("hat_crown", 4, 4, 4), ("hat_band", 4.25, .75, 4.25), ("watch", 1.75, 1.75, .5), ("chain", 2.5, .25, .25)]
    return [("basket", 3, 2.25, 2.5), ("handle", .5, 2.5, .5), ("egg", 1, 1.25, 1)] if d.get("basket") else []


def paint_hook(pt, pal, d):
    if d.get("clock"):
        hat, band, brass = G.shades(pal.extra.get("hat", "#1e1a22")), G.shades(pal.extra.get("band", "#8a2a3a")), G.shades(pal.extra.get("watch", "#d8a840"))
        pt("hat_brim", "all", lambda f, x, y, w, h: pick(hat, 0.4, x, y))
        pt("hat_crown", "all", lambda f, x, y, w, h: pick(hat, 0.45 + 0.1 * (f == "up"), x, y))
        pt("hat_band", "all", lambda f, x, y, w, h: pick(band, 0.5, x, y))
        pt("watch", "all", lambda f, x, y, w, h: (pick(G.shades("#f4f0e4"), 0.7, x, y) if 0 < x < w - 1 and 0 < y < h - 1 else pick(brass, 0.6, x, y))
           if f == "north" else pick(brass, 0.5, x, y))
        pt("chain", "all", lambda f, x, y, w, h: pick(brass, 0.6 + 0.2 * (x % 2), x, y))
        return
    if not d.get("basket"):
        return
    wicker = G.shades("#b8884a")
    pt("basket", "all", lambda f, x, y, w, h: pick(wicker, 0.45 + 0.25 * ((x + y) % 2) + (0.15 if y == 0 else 0), x, y) if f != "up" else pick(G.shades("#7ab05a"), 0.55, x, y))
    pt("handle", "all", lambda f, x, y, w, h: pick(wicker, 0.55, x, y))
    cols = ["#f4a0b8", "#a0d8f0", "#f8e070", "#b0e8a0"]
    pt("egg", "all", lambda f, x, y, w, h: pick(G.shades(cols[(x // 2 + y) % 4]), 0.6 + 0.2 * (y % 2), x, y))


def build_hook(r, g, d):
    if d.get("clock"):                                   # a top hat set between her ears; the watch on its chain at her right hip
        hy, z = g.hy, g.z
        r.add("tophat", "head", [0, hy + 9.0, z], [cube("hat_brim", [-3, hy + 9.0, -3 + z]), cube("hat_crown", [-2, hy + 9.5, -2 + z]),
                                                  cube("hat_band", [-2.125, hy + 9.6, -2.125 + z])])
        hw = G.S("hips")[0]
        r.put("body", cube("watch", [-hw / 2 - 0.9, 10.6, -2.2 + z]), cube("chain", [-hw / 2 + 0.6, 12.3, -2.1 + z]))
        return
    if not d.get("basket"):
        return
    hx = -(G.S("chest")[0] / 2 + G.S("arm_up")[0] / 2)
    y = 19.5 - 9.5
    bw, bh, bd = G.S("basket")
    r.put("hand_r", cube("basket", [hx - bw / 2, y - bh, -bd / 2]), cube("handle", [hx - 0.25, y - 0.25, -0.25]),
          cube("egg", [hx - 1.2, y - 0.4, -0.6]), cube("egg", [hx + 0.2, y - 0.3, -0.2]))


def anim_extra(an, sp, d):
    """a happy hop on the spot ("leap"), and the volcano rabbit panting when she runs out of breath ("pant")"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("leap", 0.8, False, merge({
        "root": {"position": Tk((0, (0, 0, 0)), (0.2, (0, -1, 0)), (0.4, (0, 4, 0)), (0.7, (0, 0, 0)), (0.8, (0, 0, 0)))},
        "leg_r": {"rotation": Tk((0, (0, 0, 0)), (0.2, (-40, 0, 0)), (0.4, (30, 0, 0)), (0.8, (0, 0, 0)))},
        "leg_l": {"rotation": Tk((0, (0, 0, 0)), (0.2, (-40, 0, 0)), (0.4, (30, 0, 0)), (0.8, (0, 0, 0)))},
        "ear_r": {"rotation": Tk((0, (0, 0, 0)), (0.4, (30, 0, 0)), (0.8, (0, 0, 0)))}, "ear_l": {"rotation": Tk((0, (0, 0, 0)), (0.4, (30, 0, 0)), (0.8, (0, 0, 0)))},
    }, sym("arm", Tk((0, (0, 0, 0)), (0.4, (-150, 0, 20)), (0.8, (0, 0, 0))))))
    an.add("pant", 1.0, True, merge({
        "body": {"rotation": hold(30, 0, 0), "position": Tk((0, (0, -1, 0)), (0.5, (0, -1.4, 0)), (1, (0, -1, 0)))},
        "head": {"rotation": hold(-20, 0, 0)}, "mouth": {"position": hold(*H.OUT_)},
        "leg_r": {"rotation": hold(-20, 0, 0)}, "leg_l": {"rotation": hold(-20, 0, 0)},
        "shin_r": {"rotation": hold(30, 0, 0)}, "shin_l": {"rotation": hold(30, 0, 0)},
        "ear_r": {"rotation": hold(40, 0, -20)}, "ear_l": {"rotation": hold(40, 0, 20)},
    }, sym("arm", hold(-50, 0, 6)), sym("forearm", hold(-30, 0, 0))))


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.25, -30, 8), ("run", 0.15, -60, 5), ("sit", 0, -30, 10), ("leap", 0.4, -60, 5)]

if __name__ == "__main__":
    F.run_family("jackalope", SPECIES, declare_hook, paint_hook, build_hook, anim_extra, views=VIEWS)
