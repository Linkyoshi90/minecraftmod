#!/usr/bin/env python3
"""Bats (the bat family, see bat_field_guide.html): bat.

    python tools/gen_bat.py [species ...] [--previews]

girl_family bipeds with big ears and membrane wings (girl_parts' "bat"); they fly. The fruit bat variant has a fox's
muzzle and a golden ruff (`snout`, `ruff`: drawn only on her). Extra animation: "roost", hanging upside down by her
feet with her wings wrapped round her. The model turns over inside its own height (root rotated 180 and lifted by the
model's height), so the game keeps her hitbox under the ceiling she hangs from.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, skin, CLEAR  # noqa: E402

SPECIES = {
    "cave_bat": dict(
        build="petite", outfit=dict(top=["halter"], bottom="shorts", extras=["belt"], colors=dict(cloth2="#3a2a3a")),
        hair="pixie", bangs="messy", ears="big", legs="stockings", foot="paw", wings="bat", wing_scale=0.85, flies=True,
        pal=dict(SKIN=skin("fair"), HAIR="#5a4232", FUR="#6a4e3a", LIGHT="#a88a70", MARK="#2a1e18", EYE="#2a1a14", CLOTH="#8a3a5a", TRIM="#c8a060",
                 INNER="#d8a0a0", pattern="solid", extra=dict(wing="#4a3430", wing_bone="#2a1e1a", stocking="#3a2a24", boot="#2a1e18")),
        variants={"fruit": dict(HAIR="#3a2a20", FUR="#4a3428", LIGHT="#e8b050", MARK="#1e1410", EYE="#3a2010", CLOTH="#e8a83a", TRIM="#5a3a20",
                                extra=dict(wing="#3a2a24", wing_bone="#1e1410", stocking="#2a1e18", boot="#1e1410", fox=True)),
                  "vampire": dict(SKIN=skin("pale"), HAIR="#2a2628", FUR="#3a3438", LIGHT="#6a646a", MARK="#141214", EYE="#d82a3a", CLOTH="#6a1a2a",
                                  TRIM="#c8c8d0", extra=dict(wing="#2a2428", wing_bone="#141214", stocking="#1e1a1e", boot="#0e0c0e")),
                  "ghost": dict(SKIN=skin("pale"), HAIR="#f4f2ee", FUR="#ece8e2", LIGHT="#ffffff", MARK="#b8b4ae", EYE="#c84a5a", CLOTH="#a8c8e8",
                                TRIM="#e8e8f0", INNER="#f0c8c8", extra=dict(wing="#e0d8d4", wing_bone="#b8aea8", stocking="#f0ece8", boot="#c8c0b8")),
                  "hoary": dict(HAIR="#8a7262", FUR="#7a6252", LIGHT="#e0d4c8", MARK="#3a2a22", CLOTH="#4a6a8a",
                                extra=dict(wing="#5a4438", wing_bone="#2a1e18", stocking="#4a3a30", boot="#2a1e18", frost=True))}),
}


def declare_hook(sp, d):
    return [("snout", 3, 2, 2), ("snout_n", 1.25, .75, .5), ("ruff", 5.5, 1.75, 4.25)]


def paint_hook(pt, pal, d):
    fox = pal.extra.get("fox")
    fur = G.fur_fn(pal, seed=61)
    pt("snout", "all", lambda f, x, y, w, h: (pick(pal.LIGHT, 0.55, x, y) if f in ("down",) or (f == "north" and y >= h - 1) else fur(f, x, y, w, h))
       if fox else CLEAR)
    pt("snout_n", "all", lambda f, x, y, w, h: pick(G.shades("#1e1412"), 0.4, x, y) if fox else CLEAR)
    gold = G.shades("#e8b050")
    pt("ruff", "all", lambda f, x, y, w, h: pick(gold, 0.5 + 0.25 * ((x + y) % 3 == 0), x, y) if fox else CLEAR)
    if pal.extra.get("frost"):                           # the hoary bat's frosted tips
        for p in [p for p in H.PARTS if p.startswith(("hair", "strand", "bang")) and p != "hair_cap"]:
            pt(p, "all", lambda f, x, y, w, h: pick(pal.LIGHT, 0.7, x, y) if (x * 5 + y * 3) % 7 == 0 else None)


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    r.put("head", cube("snout", [-1.5, hy + 0.4, -6.0 + z]), cube("snout_n", [-0.625, hy + 1.65, -6.4 + z]))
    r.put("body", cube("ruff", [-2.75, 18.4, -2.125 + z]))
    d["tall"] = round(G.model_height(r.model("probe").doc()), 2)   # for "roost" (the animations are made after the rig)


def anim_extra(an, sp, d):
    hold, sym, merge = H.hold, H.sym, H.merge
    tall = d["tall"]
    an.add("roost", 3.0, True, merge({"root": {"rotation": hold(180, 0, 0), "position": hold(0, tall, 0)},
                                      "eyelids": {"position": hold(*H.OUT_)}, "head": {"rotation": hold(-10, 0, 0)},
                                      "wing_r": {"rotation": hold(0, 70, 30)}, "wing_l": {"rotation": hold(0, -70, -30)},
                                      "hair_back": {"rotation": hold(-20, 0, 0)}},
                                     sym("arm", hold(0, 0, -6))))


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.25, -30, 8), ("fly", 0.3, -50, 10), ("sit", 0, -30, 10), ("roost", 0, -30, 10)]

if __name__ == "__main__":
    F.run_family("bat", SPECIES, declare_hook, paint_hook, build_hook, anim_extra, views=VIEWS)
