#!/usr/bin/env python3
"""Crustaceans (the crab family, see crustacean_field_guide.html): crab, hermit crab.

    python tools/gen_crustacean.py [species ...] [--previews]

girl_family bipeds with scaled legs, two big pincers on the hands (`pincer_r` / `pincer_l`, the moving finger
`snip_*` snaps shut in "pinch") and eye stalks. The crab carries a wide carapace on her back (`carapace`; the
decorator variant dresses it in kelp and coral), the hermit crab a borrowed spiral shell (`hshell`, its colours from
the variant: whelk, nautilus, shulker, turban, coral). Their "walk" and "run" are sideways: the whole body turns 90
degrees and the legs step out to the side.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin  # noqa: E402

SPECIES = {
    "crab_crustacean": dict(
        build="athletic", outfit=dict(top=["bandeau"], bottom="shorts", extras=["armlets"], colors=dict(cloth2="#3a4a6a")),
        hair="bob", bangs="jagged", ears=None, legs="scales", foot="claw", carapace=True,
        pal=dict(SKIN=skin("fair"), HAIR="#c83a2a", FUR="#d84a2a", LIGHT="#f8c8a8", MARK="#8a1e14", EYE="#2a1a14", CLOTH="#f0e8d8", TRIM="#e8c060",
                 pattern="solid", extra=dict(shell="#c83a24", claw="#e8603a", claw_tip="#2a1a14")),
        variants={"blue": dict(FUR="#3a6aa8", HAIR="#2a5a98", MARK="#1e3a6a", extra=dict(shell="#5a7a9a", claw="#3a7ad8", claw_tip="#e85a3a")),
                  "ghost": dict(FUR="#e8dcc0", HAIR="#f0e8d4", LIGHT="#fffaf0", MARK="#b8a888", EYE="#1a1a1a",
                                extra=dict(shell="#e0d4b8", claw="#ece2c8", claw_tip="#8a7a5a")),
                  "fiddler": dict(FUR="#e8803a", HAIR="#6a3a8a", MARK="#4a2a6a", CLOTH="#e8d040",
                                  extra=dict(shell="#5a3a7a", claw="#f0903a", claw_tip="#f8e8c8")),
                  "decorator": dict(FUR="#a85a3a", HAIR="#7a3a2a", extra=dict(shell="#8a4a32", claw="#c0683a", claw_tip="#3a2014", decorated=True))}),
    "hermit_crab_crustacean": dict(
        build="petite", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["pearls"], colors=dict(cloth2="#5a7aa8")),
        hair="odango", bangs="blunt", ears=None, legs="scales", foot="claw", hermit=True,
        pal=dict(SKIN=skin("peach"), HAIR="#e8783a", FUR="#e8884a", LIGHT="#f8d0b0", MARK="#a84a24", EYE="#2a1a14", CLOTH="#f0e0c8", TRIM="#5a7aa8",
                 pattern="solid", extra=dict(claw="#f09050", claw_tip="#3a2014", shell="#e8d8b8", shell_band="#a87a4a")),
        variants={"nautilus": dict(extra=dict(claw="#f09050", claw_tip="#3a2014", shell="#f4eee4", shell_band="#c8602a")),
                  "shulker": dict(extra=dict(claw="#f09050", claw_tip="#3a2014", shell="#9a6a9e", shell_band="#5a3a5e")),
                  "turban": dict(extra=dict(claw="#f09050", claw_tip="#3a2014", shell="#5a8a4a", shell_band="#2e4a28")),
                  "coral": dict(HAIR="#f08aa0", extra=dict(claw="#f09050", claw_tip="#3a2014", shell="#f4a0b0", shell_band="#c86a7a"))}),
}


def declare_hook(sp, d):
    out = [("pincer", 2.5, 2.5, 3), ("snip", 1, 1, 3), ("stalk", .5, 2, .5), ("stalk_eye", 1, 1, 1)]
    if d.get("carapace"):
        out += [("carapace", 8, 7, 1.5), ("carapace_rim", 9, 1.5, 2)]
    if d.get("hermit"):
        out += [("hshell_a", 6, 6, 4), ("hshell_b", 4.5, 4.5, 3), ("hshell_c", 3, 3, 2)]
    return out


def paint_hook(pt, pal, d):
    e = pal.extra
    shell, claw, tip = G.shades(e.get("shell", "#c83a24")), G.shades(e.get("claw", "#e8603a")), G.shades(e.get("claw_tip", "#2a1a14"))
    kelp, coral = G.shades("#4a8a3a"), G.shades("#f07a8a")

    def carapace(f, x, y, w, h):
        if e.get("decorated") and f in ("south", "up", "east", "west") and noise(x // 2, y // 2, 13) > 0.62:
            return pick(kelp if (x + y) % 5 else coral, 0.45 + 0.2 * (noise(x, y, 5) > 0.5), x, y)   # kelp and coral, like a spider crab
        return pick(shell, 0.42 + 0.2 * (noise(x // 2, y // 2, 4) > 0.55) + 0.12 * (y == 0), x, y)
    pt("carapace", "all", carapace)
    pt("carapace_rim", "all", carapace)
    pt("pincer", "all", lambda f, x, y, w, h: pick(claw, 0.45 + 0.25 * (f == "up") + (noise(x, y, 6) - 0.5) * 0.1, x, y))
    pt("snip", "all", lambda f, x, y, w, h: pick(tip if f == "north" or y == 0 else claw, 0.5, x, y))
    pt("stalk", "all", lambda f, x, y, w, h: pick(claw, 0.5, x, y))
    pt("stalk_eye", "all", lambda f, x, y, w, h: pick(tip, 0.35 + 0.4 * (f == "north" and x == 0 and y == 0), x, y))
    band = G.shades(e.get("shell_band", "#a87a4a"))
    for p in ("hshell_a", "hshell_b", "hshell_c"):
        pt(p, "all", lambda f, x, y, w, h: pick(band if (x + y) % 5 == 0 or y % 6 == 0 else shell, 0.45 + 0.2 * (noise(x // 2, y // 2, 8) > 0.6), x, y))


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    px, py, pz = r.b["hand_r"]["pivot"]                   # pincers on the hands (mirrored to the left), the moving finger its own bone
    r.add("pincer_r", "hand_r", [px, py, pz], [cube("pincer", [px - 1.25, py - 3.0, pz - 2.75])], mirror=True)
    r.add("snip_r", "pincer_r", [px, py - 3.0, pz - 1.0], [cube("snip", [px - 0.75, py - 4.0, pz - 2.75])], mirror=True)
    stalks = []
    for sg in (-1, 1):                                     # eye stalks above the brow
        stalks += [cube("stalk", [sg * 2.0 - 0.25, hy + 8.0, -3.0 + z]), cube("stalk_eye", [sg * 2.0 - 0.5, hy + 10.0, -3.25 + z])]
    r.add("stalks", "head", [0, hy + 8, -3 + z], stalks)
    cd = G.S("chest")[2]
    if d.get("carapace"):
        r.add("carapace", "body", [0, 17, cd / 2 + z], [cube("carapace", [-4, 12.5, cd / 2 + 0.1 + z]), cube("carapace_rim", [-4.5, 18.25, cd / 2 - 0.2 + z])])
    if d.get("hermit"):
        r.add("hshell", "body", [0, 17, 2.0 + z], [cube("hshell_a", [-3, 12.5, cd / 2 + z]), cube("hshell_b", [-2.25, 13.5, cd / 2 + 3.5 + z]),
                                                    cube("hshell_c", [-1.5, 14.5, cd / 2 + 6.0 + z])])


def anim_extra(an, sp, d):
    """the pinch, and a sideways scuttle for walk and run"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    snap = Tk((0, (0, 0, 0)), (0.15, (0, 25, 0)), (0.3, (0, 0, 0)))
    an.add("pinch", 0.6, False, merge({"snip_r": {"rotation": snap}, "snip_l": {"rotation": snap},
                                       "body": {"rotation": Tk((0, (0, 0, 0)), (0.2, (-10, 0, 0)), (0.6, (0, 0, 0)))}},
                                      sym("arm", Tk((0, (0, 0, 0)), (0.2, (-80, 0, 15)), (0.6, (0, 0, 0))))))
    for key, per in (("walk", 0.6), ("run", 0.35)):
        h = per / 2
        F._inject(an, key, {"root": {"rotation": hold(0, 90, 0)},
                            "leg_r": {"rotation": Tk((0, (0, 0, -18)), (h, (0, 0, 4)), (per, (0, 0, -18)))},
                            "leg_l": {"rotation": Tk((0, (0, 0, 4)), (h, (0, 0, 18)), (per, (0, 0, 4)))},
                            "arm_r": {"rotation": hold(-40, 0, -20)}, "arm_l": {"rotation": hold(-40, 0, 20)}})


if __name__ == "__main__":
    F.run_family("crustacean", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
