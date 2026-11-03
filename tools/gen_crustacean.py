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
    # thirty-eighth wave: the starfish: five points of a crown, knobbled and orange, and whatever you cut off grows back
    "starfish_crustacean": dict(
        build="curvy", outfit=dict(top=["shells" if False else "bandeau"], bottom="shorts", extras=["pearls"], colors=dict(cloth2="#a83a2a")),
        hair="odango", bangs="blunt", ears=None, legs="scales", foot="claw", fanshell=True,
        pal=dict(SKIN=skin("fair"), HAIR="#e8703a", FUR="#e8804a", LIGHT="#f8d0a8", MARK="#a83a2a", EYE="#2a1e14", CLOTH="#a83a2a", TRIM="#f8d0a8",
                 pattern="spots", extra=dict(shell="#e8703a", claw="#e8804a", claw_tip="#f4e4d0", brass="#a83a2a")),
        variants={"blue": dict(FUR="#3a7ad8", HAIR="#2a5ab0", MARK="#14285a", extra=dict(shell="#2a5ab0", claw="#3a7ad8", claw_tip="#f4e4d0", brass="#14285a")),
                  "pink": dict(FUR="#f088a8", HAIR="#e8608a", MARK="#a83a5a", extra=dict(shell="#e8608a", claw="#f088a8", claw_tip="#f4e4d0", brass="#a83a5a")),
                  "purple": dict(FUR="#8a4ac8", HAIR="#6a2aa8", MARK="#3a1a6a", extra=dict(shell="#6a2aa8", claw="#8a4ac8", claw_tip="#f4e4d0", brass="#3a1a6a")),
                  "sand": dict(FUR="#d8c090", HAIR="#a8905a", MARK="#7a6034", extra=dict(shell="#a8905a", claw="#d8c090", claw_tip="#f4e4d0", brass="#7a6034"))}),
    # thirty-sixth wave: the mantis shrimp: every colour at once, and the hardest punch in the sea
    "mantis_shrimp_crustacean": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts", extras=["armlets"], colors=dict(cloth2="#c83a4a")),
        hair="sidetail", bangs="jagged", ears=None, legs="scales", foot="claw", carapace=True,
        pal=dict(SKIN=skin("fair"), HAIR="#e8483a", FUR="#2aa86a", LIGHT="#f0d040", MARK="#3a5ad8", EYE="#e8602a", CLOTH="#3a5ad8", TRIM="#f0d040",
                 pattern="bands", extra=dict(shell="#2aa86a", claw="#e8483a", claw_tip="#f0d040", brass="#3a5ad8")),
        variants={"blue": dict(FUR="#3a6ae0", HAIR="#2a3a9a", extra=dict(shell="#3a6ae0", claw="#3ad0c0", claw_tip="#f8f4ec", brass="#e8483a")),
                  "violet": dict(FUR="#8a3ac8", HAIR="#e85a9a", extra=dict(shell="#8a3ac8", claw="#e85a9a", claw_tip="#f0d040", brass="#3ad0c0")),
                  "orange": dict(FUR="#e8802a", HAIR="#c83a2a", extra=dict(shell="#e8802a", claw="#3aa8c8", claw_tip="#f8f4ec", brass="#3aa86a")),
                  "dull": dict(FUR="#7a7a60", HAIR="#4a4a3a", extra=dict(shell="#7a7a60", claw="#a8a884", claw_tip="#e8e0c8", brass="#4a4a3a"))}),
    # thirty-fifth wave: the lobster: deep blue that goes red if you're not careful, one great crusher claw
    "lobster_crustacean": dict(
        build="strong", outfit=dict(top=["bandeau"], bottom="shorts", extras=["armlets"], colors=dict(cloth2="#14285a")),
        hair="ponytail", bangs="jagged", ears=None, legs="scales", foot="claw", carapace=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e3a7a", FUR="#2a4a9a", LIGHT="#a8c0e8", MARK="#14285a", EYE="#2a1e14", CLOTH="#14285a", TRIM="#e8c050",
                 pattern="solid", extra=dict(shell="#1e3a7a", claw="#2a4a9a", claw_tip="#e85a3a", brass="#e8c050")),
        variants={"red": dict(FUR="#c83a2a", HAIR="#8a1e14", MARK="#5a0e0a", extra=dict(shell="#8a1e14", claw="#c83a2a", claw_tip="#f4e4d0", brass="#e8c050")),
                  "yellow": dict(FUR="#e0b83a", HAIR="#a8802a", MARK="#5a4010", extra=dict(shell="#a8802a", claw="#e0b83a", claw_tip="#f4e4d0", brass="#5a4010")),
                  "calico": dict(FUR="#d88a3a", HAIR="#2a2a30", MARK="#2a2a30", extra=dict(shell="#2a2a30", claw="#d88a3a", claw_tip="#f4e4d0", brass="#e8c050")),
                  "cotton": dict(FUR="#d8e0f0", HAIR="#a8b8d8", MARK="#7a8ab0", extra=dict(shell="#a8b8d8", claw="#d8e0f0", claw_tip="#7a8ab0", brass="#e8c050"))}),
    # sixteenth wave: the floodcrab, a blue-grey river crab under a ridged stone fan of a shell, hung with brass bells, moss and barnacles
    "floodcrab_crustacean": dict(
        build="standard", outfit=dict(top=["bandeau"], bottom="sarong", extras=["pouch"], colors=dict(cloth2="#4a5a3a", leather="#5a4a30")),
        hair="bob", bangs="messy", ears=None, legs="scales", foot="claw", fanshell=True,
        pal=dict(SKIN=skin("fair"), HAIR="#3e5a72", FUR="#3e5e7a", LIGHT="#b8c8d4", MARK="#1e2e3e", EYE="#c82a1e", CLOTH="#5a6a48", TRIM="#c8a040",
                 pattern="solid", extra=dict(shell="#8a8a80", claw="#3e6a8a", claw_tip="#e8e0c8", brass="#c8a040")),
        variants={"brown": dict(FUR="#8a6a4a", HAIR="#6a4a2a", MARK="#3a2a1a", extra=dict(shell="#9a8a70", claw="#9a7a5a", claw_tip="#2a1e14", brass="#c8a040")),
                  "green": dict(FUR="#5a7a4a", HAIR="#3a5a2a", MARK="#2a3418", extra=dict(shell="#7a8a6a", claw="#6a8a4a", claw_tip="#e8d8a0", brass="#c8a040")),
                  "red": dict(FUR="#a84a3a", HAIR="#7a2a1e", MARK="#4a140e", EYE="#f0c040",
                              extra=dict(shell="#a89a88", claw="#c8503a", claw_tip="#f0e8d8", brass="#d8b050")),
                  "black": dict(FUR="#2a2e36", HAIR="#1a1e24", MARK="#0a0c10",
                                extra=dict(shell="#4a4a4e", claw="#3a3e48", claw_tip="#c8c0b0", brass="#a88a3a"))}),
    # thirtieth wave: the spider crab of the cold deep: tall, long-limbed, a knobbled orange shell
    "spider_crab_crustacean": dict(
        build="slim", outfit=dict(top=["bandeau"], bottom="sarong", colors=dict(cloth2="#8a3a1e")),
        hair="ponytail", bangs="messy", ears=None, legs="scales", foot="claw", fanshell=True,
        pal=dict(SKIN=skin("fair"), HAIR="#c85a2a", FUR="#d8703a", LIGHT="#f4d0b0", MARK="#7a2e14", EYE="#2a1e14", CLOTH="#8a3a1e", TRIM="#f4d0b0",
                 pattern="solid", extra=dict(shell="#c85a2a", claw="#d8703a", claw_tip="#f4e4d0", brass="#7a2e14")),
        variants={"pale": dict(FUR="#e8c8a8", HAIR="#d8b088", MARK="#a87a58", extra=dict(shell="#d8b088", claw="#e8c8a8", claw_tip="#2a1e14", brass="#a87a58")),
                  "red": dict(FUR="#a82a22", HAIR="#8a1e1a", MARK="#4a0e0c", extra=dict(shell="#8a1e1a", claw="#a82a22", claw_tip="#f4e4d0", brass="#4a0e0c")),
                  "blue": dict(FUR="#3a6a9a", HAIR="#2a5078", MARK="#14283e", extra=dict(shell="#2a5078", claw="#3a6a9a", claw_tip="#f4e4d0", brass="#14283e")),
                  "black": dict(FUR="#2a2e36", HAIR="#1a1e24", MARK="#0e1014", extra=dict(shell="#1a1e24", claw="#2a2e36", claw_tip="#c8c0a8", brass="#0e1014"))}),
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
    if d.get("fanshell"):
        out += [("fan_rib", 2.5, 17, 1), ("fan_base", 7, 2, 2), ("fbell", 1.5, 1.5, 1.5), ("barnacle", 1, 1, 1)]
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
    brass, moss, white = G.shades(e.get("brass", "#c8a040")), G.shades("#5a7a3a"), G.shades("#ece6d8")

    def fan(f, x, y, w, h):                                # a ridged stone plate, moss creeping up from the base
        if y > h - 3 - 2 * noise(x, 0, 21) and noise(x, y, 22) > 0.35:
            return pick(moss, 0.4 + 0.15 * (noise(x, y, 23) > 0.5), x, y)
        if noise(x, y, 24) > 0.93 and y > 3:
            return pick(white, 0.5, x, y)                  # barnacles
        return pick(shell, 0.62 - 0.22 * (x == 0 or x == w - 1) + 0.08 * (x == w // 2) + (noise(x // 2, y // 3, 25) - 0.5) * 0.12, x, y)
    pt("fan_rib", "all", fan)
    pt("fan_base", "all", fan)
    pt("fbell", "all", lambda f, x, y, w, h: pick(brass, 0.25 if f == "down" else 0.5 + 0.25 * (x == 0) + 0.1 * (y == 0), x, y))
    pt("barnacle", "all", lambda f, x, y, w, h: pick(white, 0.45 + 0.2 * (f == "up"), x, y))
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
    if d.get("fanshell"):                                  # a fan of seven stone ribs over her back; named hair_ so it does not count to her height
        bz = cd / 2 + z
        ribs = [cube("fan_base", [-3.5, 13.0, bz])]
        for i, ang in enumerate((-48, -32, -16, 0, 16, 32, 48)):
            dz = bz + 0.6 + 0.15 * abs(i - 3)
            rot = dict(pivot=[0, 14.0, bz + 1], rotation=[-10, 0, ang])
            ribs.append(cube("fan_rib", [-1.25, 14.0, dz], **rot))
            if i in (0, 2, 4, 6):                          # brass bells at the rib tips, barnacles on the others
                ribs.append(cube("fbell", [-0.75, 30.0 - (i in (2, 4)) * 2, dz + 0.9], **rot))
            elif i in (1, 5):
                ribs.append(cube("barnacle", [-0.5, 25.0, dz + 0.9], **rot))
        r.add("hair_fan", "body", [0, 14.0, bz], ribs)
        px, py, pz = r.b["hand_r"]["pivot"]               # a bell tied to each claw
        r.add("cbell_r", "pincer_r", [px, py, pz], [cube("fbell", [px + 1.25, py - 2.5, pz - 1.0])], mirror=True)
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


# homage looks of the thirtieth wave (POP_CULTURE_VARIANTS.md)
SPECIES["crab_crustacean"]["variants"]["calypso_crab"] = dict(FUR="#d82a22", HAIR="#a81e1a", MARK="#5a0e0c", EYE="#2a1e14", CLOTH="#f8f4ec", extra=dict(claw="#d82a22", claw_tip="#f4e4d0"))


if __name__ == "__main__":
    F.run_family("crustacean", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
