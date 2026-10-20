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
}


def declare_hook(sp, d):
    return [("basket", 3, 2.25, 2.5), ("handle", .5, 2.5, .5), ("egg", 1, 1.25, 1)] if d.get("basket") else []


def paint_hook(pt, pal, d):
    if not d.get("basket"):
        return
    wicker = G.shades("#b8884a")
    pt("basket", "all", lambda f, x, y, w, h: pick(wicker, 0.45 + 0.25 * ((x + y) % 2) + (0.15 if y == 0 else 0), x, y) if f != "up" else pick(G.shades("#7ab05a"), 0.55, x, y))
    pt("handle", "all", lambda f, x, y, w, h: pick(wicker, 0.55, x, y))
    cols = ["#f4a0b8", "#a0d8f0", "#f8e070", "#b0e8a0"]
    pt("egg", "all", lambda f, x, y, w, h: pick(G.shades(cols[(x // 2 + y) % 4]), 0.6 + 0.2 * (y % 2), x, y))


def build_hook(r, g, d):
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
