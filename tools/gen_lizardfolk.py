#!/usr/bin/env python3
"""Lizardfolk (the reptile family, see lizardfolk_field_guide.html): salamander, chameleon, iguana, crocodile, turtle.

    python tools/gen_lizardfolk.py [species ...] [--previews]

girl_family bipeds with scaled legs, clawed feet and a heavy four-part tail (tail .. tail_4). The salamander's tail
ends in a flame (glow mask), the chameleon's curls up, the iguana and the crocodile have spines down the back and tail
(`spines`), the turtle carries a shell (`shell`) and has a stubby tail.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin  # noqa: E402

LIZARD_TAIL = [(2.5, 2.5, 4), (2, 2, 4), (1.5, 1.5, 4), (1, 1, 3.5)]
DROOP = [(-38, 0, 0), (10, 0, 0), (10, 0, 0), (8, 0, 0)]

SPECIES = {
    "salamander_lizardfolk": dict(
        build="athletic", outfit=dict(top=["bandeau"], bottom="loincloth", extras=["armlets", "belt"], colors=dict(cloth2="#3a1a14")),
        hair="ponytail", bangs="jagged", ears=None, legs="scales", foot="claw", tail=LIZARD_TAIL, tail_rest=DROOP, tail_look="flame",
        glow=["tail_d"],
        pal=dict(SKIN=skin("tan"), HAIR="#d84a1a", FUR="#e8702a", LIGHT="#f8c87a", MARK="#f8e04a", EYE="#f8c82a", CLOTH="#2a1a14", TRIM="#f8a83a",
                 pattern="spots", pupil=(0.9, 3.4), extra=dict(flame="#f8a02a", claw="#3a2014")),
        variants={"crimson": dict(FUR="#c82a1a", HAIR="#8a1a10", MARK="#f8a02a", extra=dict(flame="#f8582a", claw="#2a0e08")),
                  "soul": dict(FUR="#2a6a8a", HAIR="#2ac8e8", MARK="#8af0ff", EYE="#8af0ff", extra=dict(flame="#3ae8f8", claw="#14303a")),
                  "obsidian": dict(FUR="#2a1e2a", HAIR="#1a141a", MARK="#f8782a", LIGHT="#5a4a5a", extra=dict(flame="#f8782a", claw="#100c10")),
                  "gold": dict(FUR="#e8b830", HAIR="#c8781a", MARK="#fff0a0", extra=dict(flame="#fff07a", claw="#5a3a10"))}),
    "chameleon_lizardfolk": dict(
        build="slim", outfit=dict(top=["halter"], bottom="sarong", extras=["armlets"], colors=dict(cloth2="#6a3a8a")),
        hair="bun", bangs="swept", ears=None, legs="scales", foot="claw", tail=LIZARD_TAIL, tail_look="scale",
        tail_rest=[(-20, 0, 0), (45, 0, 0), (60, 0, 0), (70, 0, 0)], tail_sway=3,
        pal=dict(SKIN=skin("peach"), HAIR="#5ab84a", FUR="#6ac850", LIGHT="#c8f0a0", MARK="#3a8a2a", EYE="#e8c83a", CLOTH="#e8a83a", TRIM="#c83a6a",
                 pattern="stripes", pupil=(1.4, 1.4)),
        variants={"panther": dict(FUR="#3ac8c0", HAIR="#e84a3a", MARK="#e84a3a", LIGHT="#a8f0f0"),
                  "yellow": dict(FUR="#c8e04a", HAIR="#a8c83a", MARK="#7a9a2a"),
                  "veiled": dict(FUR="#4ab05a", HAIR="#3a8a4a", MARK="#e8c83a", pattern="bands"),
                  "brown": dict(FUR="#8a6a4a", HAIR="#6a4a2a", MARK="#4a3420", LIGHT="#c8a880")}),
    "iguana_lizardfolk": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts", extras=["belt"], colors=dict(cloth2="#3a5a7a")),
        hair="sidetail", bangs="messy", ears=None, legs="scales", foot="claw", tail=LIZARD_TAIL, tail_rest=DROOP, tail_look="scale", spines=True,
        pal=dict(SKIN=skin("tan"), HAIR="#4a8a3a", FUR="#5aa84a", LIGHT="#c8e8a0", MARK="#2a5a2a", EYE="#d89a2a", CLOTH="#e8e0c8", TRIM="#3a5a7a",
                 pattern="bands"),
        variants={"blue": dict(FUR="#4a8ac8", HAIR="#3a6aa8", MARK="#2a4a7a", LIGHT="#b8d8f0"),
                  "orange": dict(FUR="#e8883a", HAIR="#c8682a", MARK="#7a3a1a", LIGHT="#f8c8a0"),
                  "marine": dict(FUR="#4a4a4e", HAIR="#2a2a2e", MARK="#7a7a5a", LIGHT="#8a8a8e"),
                  "rhino": dict(FUR="#8a8a82", HAIR="#5a5a52", MARK="#4a4a44", LIGHT="#c0c0b8")}),
    "croc_lizardfolk": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps", "belt"], colors=dict(bandage="#c8b898")),
        hair="shaggy", bangs="messy", ears=None, legs="scales", foot="claw", tail=[(3.5, 3, 5), (3, 2.5, 5), (2.25, 2, 4.5), (1.5, 1.5, 4)],
        tail_rest=[(-25, 0, 0), (8, 0, 0), (6, 0, 0), (4, 0, 0)], tail_look="scale", spines=True, teeth=True,
        pal=dict(SKIN=skin("olive"), HAIR="#2a3a1e", FUR="#5a6a3a", LIGHT="#d8d0a0", MARK="#2a3a1a", EYE="#d8c83a", CLOTH="#4a3a2a", TRIM="#a8987a",
                 pattern="bands", pupil=(0.9, 3.4)),
        variants={"saltwater": dict(FUR="#4a5030", HAIR="#1e2414", MARK="#1a1e10"),
                  "albino": dict(FUR="#f0e8d8", HAIR="#f8f4ec", MARK="#d8c8b0", LIGHT="#ffffff", EYE="#e83a4a"),
                  "dwarf": dict(FUR="#2a2a24", HAIR="#141410", MARK="#4a4a3a"),
                  "gharial": dict(FUR="#7a8a6a", HAIR="#4a5a3a", MARK="#3a4a2a")}),
    "turtle_lizardfolk": dict(
        build="petite", outfit=dict(top=["sports"], bottom="shorts", extras=["pearls"], colors=dict(cloth2="#3a7a8a")),
        hair="bob", bangs="blunt", ears=None, legs="scales", foot="claw", tail=[(1.5, 1.5, 2), (1, 1, 1.5)], tail_rest=[(-50, 0, 0), (10, 0, 0)],
        tail_look="scale", shell=True,
        pal=dict(SKIN=skin("peach"), HAIR="#3a6a3a", FUR="#7aa85a", LIGHT="#e0e8b0", MARK="#4a6a3a", EYE="#3a2a1a", CLOTH="#e8d8a8", TRIM="#3a7a8a",
                 extra=dict(shell="#7a5a2a", shell_band="#c8a050")),
        variants={"hawksbill": dict(extra=dict(shell="#c88a3a", shell_band="#5a2a10"), HAIR="#6a4a2a"),
                  "leatherback": dict(FUR="#4a4a52", HAIR="#2a2a32", extra=dict(shell="#2a2a32", shell_band="#7a7a8a")),
                  "loggerhead": dict(FUR="#a87a4a", HAIR="#8a4a2a", extra=dict(shell="#8a3a1a", shell_band="#c8784a")),
                  "olive": dict(FUR="#8a8a4a", HAIR="#5a5a2a", extra=dict(shell="#6a6a2a", shell_band="#a8a85a"))}),
    "snapping_turtle_lizardfolk": dict(
        build="strong", outfit=dict(top=["halter", "vest"], bottom="sarong", extras=["belt", "pouch"], colors=dict(cloth2="#3a4a2a", leather="#4a3a2a")),
        hair="shaggy", bangs="messy", ears=None, legs="scales", foot="claw", tail=[(2.5, 2, 4), (2, 1.75, 4), (1.5, 1.25, 3.5), (1, 1, 3)],
        tail_rest=DROOP, tail_look="scale", spines=True, shell=True,
        pal=dict(SKIN=skin("olive"), HAIR="#3a4a22", FUR="#6a5a42", LIGHT="#a89a7a", MARK="#3a3024", EYE="#c8a040", CLOTH="#5a6a4a", TRIM="#a8987a",
                 pattern="bands", pupil=(1.4, 1.4), extra=dict(shell="#3e3424", shell_band="#24201a")),
        variants={"mossy": dict(HAIR="#4a7a2a", extra=dict(shell="#3e4a24", shell_band="#5a7a2a")),
                  "dark": dict(FUR="#3a342a", HAIR="#1e1e16", extra=dict(shell="#1e1a14", shell_band="#3a3428")),
                  "suwannee": dict(FUR="#7a7466", HAIR="#4a4a3a", extra=dict(shell="#5a5448", shell_band="#3a362e")),
                  "leucistic": dict(FUR="#e0d4b8", HAIR="#f0e8d0", LIGHT="#fff8e8", MARK="#c8b898", EYE="#c84a4a",
                                    extra=dict(shell="#d8c8a0", shell_band="#b8a880"))}),
}


def declare_hook(sp, d):
    out = []
    if d.get("spines"):
        out += [("spine", .5, 1.25, 1)]
    if d.get("shell"):
        out += [("carapace", 7, 8, 2.5), ("carapace_top", 5.5, 6, 1.25), ("plastron", 5, 5, .5)]
    if d.get("teeth"):
        out += [("snout", 3.5, 1, 1.5)]
    return out


def paint_hook(pt, pal, d):
    pt("spine", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.45 + 0.2 * (y == 0), x, y))
    shell = G.shades(pal.extra.get("shell", "#7a5a2a"))
    band = G.shades(pal.extra.get("shell_band", "#c8a050"))

    def scutes(f, x, y, w, h):
        edge = x % 6 == 0 or y % 6 == 0
        return pick(band if edge else shell, 0.4 + 0.2 * (noise(x // 3, y // 3, 5) > 0.5), x, y)
    for p in ("carapace", "carapace_top"):
        pt(p, "all", scutes)
    pt("plastron", "all", lambda f, x, y, w, h: pick(G.shades("#e8d8a0"), 0.5 + 0.2 * (y % 3 == 0), x, y))
    pt("snout", "all", lambda f, x, y, w, h: pal.WHITE if f == "north" and y == h - 1 and x % 2 == 0 else H.skin_fn(pal, 0.66)(f, x, y, w, h))


def build_hook(r, g, d):
    z = g.z
    if d.get("spines"):
        cs = [cube("spine", [-0.25, 13 + i * 1.6, 1.4 + z]) for i in range(4)]
        r.put("body", *cs)
        for i, b in enumerate(("tail", "tail_2", "tail_3")):
            if b in r.b:
                piv = r.b[b]["pivot"]
                r.put(b, cube("spine", [-0.25, piv[1] + 0.6, piv[2] + 1.0]), cube("spine", [-0.25, piv[1] + 0.4, piv[2] + 2.6]))
    if d.get("shell"):
        cd = G.S("chest")[2]
        r.add("shell", "body", [0, 16, 1.5 + z], [cube("carapace", [-3.5, 12.5, cd / 2 - 0.2 + z]), cube("carapace_top", [-2.75, 13.5, cd / 2 + 2.2 + z])])
    if d.get("teeth"):
        r.put("head", cube("snout", [-1.75, g.hy + 0.5, -5.2 + z]))


def anim_extra(an, sp, d):
    """basking in the sun ("bask"), the crocodile's death roll ("roll"), the turtle hiding in her shell ("retract")"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("bask", 4.0, True, merge({"root": {"rotation": hold(-90, 0, 0), "position": hold(0, 2.5, 0)}, "head": {"rotation": hold(-30, 0, 0)},
                                     "eyelids": {"position": hold(*H.OUT_)}}, sym("arm", hold(-170, 0, 20))))
    an.add("roll", 1.0, True, merge({"root": {"rotation": Tk((0, (0, 0, 0)), (0.5, (0, 0, 180)), (1.0, (0, 0, 360))), "position": hold(0, 4, 0)},
                                     "mouth": {"position": hold(*H.OUT_)}}, sym("arm", hold(-90, 0, 0))))
    an.add("retract", 1.0, False, merge({
        "body": {"rotation": Tk((0, (0, 0, 0)), (0.5, (70, 0, 0)), (1, (80, 0, 0))), "position": Tk((0, (0, 0, 0)), (1, (0, -6, 2)))},
        "leg_r": {"position": hold(0, -5, 0), "rotation": hold(-90, 0, 0)}, "leg_l": {"position": hold(0, -5, 0), "rotation": hold(-90, 0, 0)},
        "shin_r": {"rotation": hold(150, 0, 0)}, "shin_l": {"rotation": hold(150, 0, 0)}, "head": {"rotation": hold(40, 0, 0)},
        "eyelids": {"position": hold(*H.OUT_)}}, sym("arm", hold(-120, 0, -20))))


if __name__ == "__main__":
    F.run_family("lizardfolk", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
