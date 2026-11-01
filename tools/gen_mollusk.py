#!/usr/bin/env python3
"""Mollusks (the shell and tentacle family, see mollusk_field_guide.html): kraken, Ika, octopus, snail, ammonite.

    python tools/gen_mollusk.py [species ...] [--previews]

    kraken    girl_lib upper body over eight tentacles (bones tent1..tent8, each with tentN_b / tentN_c joints) and a
              mantle skirt; animations idle, walk, run, swim, sit, lie, grab
    ika       girl_family biped with a squid hat (bone `hat`) and six tentacle locks of hair (hair_tent1..6) that lash
              forward ("lash"); "ink"
    octopus   girl_family biped with a round mantle cap (`hat`) and a skirt of eight short tentacles (tskirt1..8); "ink"
    snail     girl_family biped with a spiral shell on her back (`shell`) and eye-stalk antennae; "retract"
    ammonite  girl_lib upper body rising out of a great coiled shell (`coil`); "retract" pulls her in
    jellyfish girl_family biped under a see-through bell (`bell`, glowing) with ten long tentacles trailing from its rim
              (bell_tent1..10); not a mollusk, but she drifts with them
Five colours per species.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
import girl_parts as P  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

SPECIES = {
    # twenty-third wave: the spewslug, a glistening crimson slug girl with eye-stalk antennae, a slug's mantle over her back, a wide
    # toothy maw and purple bile dribbling from it
    "spewslug_mollusk": dict(
        build="curvy", outfit=dict(top=["halter"], bottom="wrap_skirt", colors=dict(cloth2="#6a1a2a")),
        hair="wavy", bangs="messy", ears="antenna", legs="bare", foot="shoe", body="slug", grin=True,
        pal=dict(SKIN=skin("#d87a7a"), HAIR="#a8202a", FUR="#b82a32", LIGHT="#f0a0a0", MARK="#5a0e14", EYE="#e8d040", CLOTH="#6a1a2a", TRIM="#b84ad8",
                 INNER="#f0a0b0", extra=dict(antenna="#b82a32", mantle="#a8202a", bile="#b84ad8")),
        variants={"orange": dict(SKIN=skin("#e0a070"), HAIR="#c8601e", FUR="#d8702a", MARK="#6a2a0a", extra=dict(antenna="#d8702a", mantle="#c8601e", bile="#a8d83a")),
                  "black": dict(SKIN=skin("#8a7a82"), HAIR="#2a2228", FUR="#3a3038", MARK="#0e0a0e", EYE="#e83a3a", extra=dict(antenna="#3a3038", mantle="#2a2228", bile="#5ad8a8")),
                  "yellow": dict(SKIN=skin("#e8d090"), HAIR="#c8a020", FUR="#d8b830", MARK="#5a4a0e", EYE="#3a2a1a", extra=dict(antenna="#d8b830", mantle="#c8a020", bile="#e85a8a")),
                  "blue": dict(SKIN=skin("#8aa0d8"), HAIR="#2a4aa8", FUR="#3a5ac0", MARK="#0e1a5a", EYE="#f8a030", extra=dict(antenna="#3a5ac0", mantle="#2a4aa8", bile="#f8d040"))}),
    # twenty-third wave (folklore): Scylla, the sea monster of the Odyssey: a sea-green girl over a skirt of tentacles, with six dog
    # heads snarling out from her waist
    # twenty-fifth wave: Charybdis, the whirlpool across the strait from Scylla: curly pale-teal hair, a shell top, a skirt of tentacles
    "charybdis_mollusk": dict(
        build="curvy", outfit=dict(top=["shells"], extras=["pearls"], colors=dict(cloth2="#5ab8b0", shell="#f4f0e4")),
        hair="wavy", bangs="swept", ears="fin", legs="bare", foot="shoe", body="octopus", hat=False,
        pal=dict(SKIN=skin("#d8f4f0"), HAIR="#8ae0d8", FUR="#7ad0c8", LIGHT="#d8f8f4", MARK="#2a8a84", EYE="#2a7ac8", CLOTH="#5ab8b0", TRIM="#f0ecd8", INNER="#f0b8c0", extra=dict(boot="#2a8a84")),
        variants={"blue": dict(HAIR="#6aa8e8", FUR="#5a98e0", LIGHT="#c8dcf8", MARK="#1e3a8a", extra=dict(boot="#1e3a8a")),
                  "green": dict(HAIR="#7ad08a", FUR="#6ac07a", LIGHT="#d0f4d8", MARK="#1e6a34", extra=dict(boot="#1e6a34")),
                  "rose": dict(HAIR="#f0a0b8", FUR="#e88aa8", LIGHT="#fcd8e4", MARK="#8a2a50", extra=dict(boot="#8a2a50")),
                  "grey": dict(HAIR="#b8c0c8", FUR="#a0a8b0", LIGHT="#e0e4e8", MARK="#4a5058", extra=dict(boot="#4a5058"))}),
    "scylla_mollusk": dict(
        build="curvy", outfit=dict(top=["shells", "corset"], extras=["pearls", "armlets"], colors=dict(cloth2="#1e3a4a", shell="#f4c8cc")),
        hair="flowing", bangs="parted", ears="fin", legs="bare", foot="shoe", body="octopus", hat=False, dogs=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e6a6a", FUR="#2a7a7a", LIGHT="#a8e0d8", MARK="#0e3a3a", EYE="#e8c040", CLOTH="#1e3a4a", TRIM="#e8c060",
                 INNER="#f0b8c0", extra=dict(dog="#5a6a74", dog_dark="#2a343a", boot="#0e3a3a")),
        variants={"blue": dict(HAIR="#2a4a9a", FUR="#3a5ab0", LIGHT="#b8c8f0", MARK="#14204a", extra=dict(dog="#4a5068", dog_dark="#1e2232", boot="#14204a")),
                  "purple": dict(HAIR="#5a2a8a", FUR="#6a3a9a", LIGHT="#d8c0f0", MARK="#2a1044", extra=dict(dog="#6a5a6a", dog_dark="#2e242e", boot="#2a1044")),
                  "red": dict(HAIR="#8a1e2a", FUR="#a82a32", LIGHT="#f0b0b0", MARK="#4a0a10", extra=dict(dog="#6a5048", dog_dark="#2e1e1a", boot="#4a0a10")),
                  "pale": dict(HAIR="#d8e0e4", FUR="#c8d4d8", LIGHT="#f4f8fa", MARK="#6a7a80", EYE="#3a8ab0", extra=dict(dog="#a8a8a4", dog_dark="#4a4a48", boot="#6a7a80"))}),
    # sixteenth wave: the squidkin, a sky-blue squid girl: long wavy hair curling into tentacles, a slip dress over a skirt of
    # eight tentacles (the octopus' body without its cap, the ika's tentacle locks)
    "squidkin_mollusk": dict(
        build="slim", outfit=dict(top=["halter"]), hat=False, tent_hair=True,
        hair="wavy", bangs="swept", ears=None, legs="bare", foot="shoe", body="octopus",
        pal=dict(SKIN=skin("fair"), HAIR="#6a94c8", FUR="#6a94c8", LIGHT="#c8dcf0", MARK="#3a5a8a", EYE="#3a5a9a", CLOTH="#6a94c8", TRIM="#c8dcf0",
                 INNER="#f8d0e0", extra=dict(boot="#3a5a8a")),
        variants={"violet": dict(FUR="#7a4ab8", HAIR="#8a5ac8", MARK="#3a1e6a", LIGHT="#e0d0f8", CLOTH="#7a4ab8", EYE="#3a1e6a", extra=dict(boot="#2a1450")),
                  "red": dict(FUR="#c83a3a", HAIR="#e85a4a", MARK="#5a1414", LIGHT="#f8c8c0", CLOTH="#c83a3a", extra=dict(boot="#5a1414")),
                  "glow": dict(FUR="#1e5a6a", HAIR="#3ad8c8", MARK="#8af8e8", LIGHT="#a8f0e8", CLOTH="#1e5a6a", EYE="#3ad8c8", extra=dict(boot="#0e3a44")),
                  "white": dict(FUR="#f0eef4", HAIR="#e8e4f0", MARK="#a8a0c0", LIGHT="#ffffff", CLOTH="#f0eef4", extra=dict(boot="#a8a0c0"))}),
    # thirtieth wave: the giant kraken, the kraken's rig drawn enormous, an abyssal blue
    "giant_kraken_mollusk": dict(
        build="curvy", outfit=dict(top=["corset"], extras=["pearls"], colors=dict(cloth2="#0e1e3a")),
        hair="flowing", bangs="parted", ears="fin", body="kraken",
        pal=dict(SKIN=skin("#c8d8f0"), HAIR="#a8c8e8", FUR="#1e3a6a", LIGHT="#6a90c0", MARK="#0a1830", EYE="#5af0f8", CLOTH="#0e1e3a", TRIM="#5af0f8", INNER="#a8c8e8"),
        variants={"crimson": dict(FUR="#7a1420", LIGHT="#c8606a", MARK="#2e060c", TRIM="#e8c060"), "pallid": dict(FUR="#a8b0b8", LIGHT="#dce0e4", MARK="#4a5058", EYE="#e83a2a"),
                  "violet": dict(FUR="#3a1a6a", LIGHT="#8a68c0", MARK="#160a2e"), "ink": dict(FUR="#14141c", LIGHT="#4a4a58", MARK="#08080c", SKIN=skin("#9098a8"))}),
    "kraken_mollusk": dict(
        build="curvy", outfit=dict(top=["corset"], extras=["pearls"], colors=dict(cloth2="#2a1a3a")),
        hair="wavy", bangs="parted", ears="fin", body="kraken",
        pal=dict(SKIN=skin("lilac"), HAIR="#e8e8f0", FUR="#a8283a", LIGHT="#f0a8b0", MARK="#5a0e1a", EYE="#e8c040", CLOTH="#2a1a3a", TRIM="#e8c060",
                 INNER="#f8c8d0"),
        variants={"violet": dict(FUR="#6a2a8a", LIGHT="#c8a0e0", MARK="#2e0e42", SKIN=skin("lilac")),
                  "abyssal": dict(FUR="#1a1a2a", LIGHT="#3a6aa8", MARK="#5aa8f0", HAIR="#3a3a5a", SKIN=skin("grey")),
                  "pearl": dict(FUR="#f2e8e8", LIGHT="#ffffff", MARK="#e8a8b8", HAIR="#f8f0f0", SKIN=skin("pale"), CLOTH="#4a6a9a"),
                  "seagreen": dict(FUR="#2a7a6a", LIGHT="#8ad8c0", MARK="#0e3a32", HAIR="#a8f0e0", SKIN=skin("blue"))}),
    "ika_mollusk": dict(
        build="petite", outfit=dict(top=["halter"], bottom="wrap_skirt", colors=dict(cloth2="#f4f4f8")),
        hair="bob", bangs="blunt", ears=None, legs="stockings", foot="shoe", body="ika",
        pal=dict(SKIN=skin("pale"), HAIR="#5ab0e8", FUR="#f4f4f8", LIGHT="#ffffff", MARK="#3a7ac8", EYE="#2a6ab8", CLOTH="#f4f4f8", TRIM="#3a7ac8",
                 INNER="#f8d0d8", extra=dict(stocking="#f4f4f8", boot="#3a7ac8")),
        variants={"firefly": dict(FUR="#2a3a6a", HAIR="#3ad0f8", MARK="#8af0ff", LIGHT="#5a6aa8", CLOTH="#2a3a6a"),
                  "red": dict(FUR="#e85a4a", HAIR="#f08a6a", MARK="#a82a2a", LIGHT="#f8b8a8", CLOTH="#f8e8e0"),
                  "pink": dict(FUR="#f8c8d8", HAIR="#f0a0c0", MARK="#d86a98", LIGHT="#fff0f4"),
                  "teal": dict(FUR="#d8f4f0", HAIR="#3ac0b0", MARK="#2a8a80", LIGHT="#f0fffc"),
                  # homage (2026-10-28): an Inkling girl (Splatoon): bright orange tentacle hair, white tee, black shorts
                  "ink_kid": dict(FUR="#f8901e", HAIR="#f8901e", MARK="#1e1e24", LIGHT="#ffd8a0", EYE="#f8901e", CLOTH="#f4f4f8", TRIM="#1e1e24",
                                  extra=dict(cloth2="#24242a", stocking="#24242a", boot="#f8c020"))}),
    "octopus_mollusk": dict(
        build="slim", outfit=dict(top=["sweater"], extras=["satchel"], colors=dict(cloth2="#3a5a7a")),
        hair="ponytail", bangs="swept", ears=None, legs="bare", foot="shoe", body="octopus",
        pal=dict(SKIN=skin("peach"), HAIR="#c8602a", FUR="#d0703a", LIGHT="#f8c8a0", MARK="#8a3a1a", EYE="#e8a030", CLOTH="#e8dcc0", TRIM="#3a5a7a",
                 INNER="#f8d8c0", pupil=(2.4, 0.9)),
        variants={"blueringed": dict(FUR="#e8c840", HAIR="#d8a830", MARK="#2a5ad8", LIGHT="#f8e8a0", pattern="rings"),
                  "mimic": dict(FUR="#8a6a4a", HAIR="#5a3e26", MARK="#f2ece0", LIGHT="#e8dccc"),
                  "red": dict(FUR="#c83a2a", HAIR="#a82a1a", MARK="#6a1a10", LIGHT="#f0a090"),
                  "dumbo": dict(FUR="#f0b0c8", HAIR="#e890b0", MARK="#c8608a", LIGHT="#fce0ea"),
                  # homage (2026-10-28): an Octoling (Splatoon): magenta tentacle hair, a black bodysuit with teal trim
                  "octo_agent": dict(FUR="#e8308a", HAIR="#e8308a", MARK="#7a0e48", LIGHT="#ff8ac0", EYE="#2ae8c8", CLOTH="#1e1e24", TRIM="#2ae8c8", extra=dict(cloth2="#1e1e24"))}),
    "snail_mollusk": dict(
        build="petite", outfit=dict(top=["sweater"], bottom="wrap_skirt", colors=dict(cloth2="#8a9a5a")),
        hair="bun", bangs="wispy", ears="antenna", legs="bare", foot="shoe", body="snail",
        pal=dict(SKIN=skin("grey"), HAIR="#7a6a5a", FUR="#8a6a4a", LIGHT="#d8c8a8", MARK="#4a3426", EYE="#5a4a3a", CLOTH="#d8c8a8", TRIM="#8a9a5a",
                 INNER="#e8c8c0", extra=dict(antenna="#9a8a7a", shell="#9a6a3a", shell_band="#4a3020")),
        variants={"banded": dict(extra=dict(antenna="#9a8a7a", shell="#e8c850", shell_band="#3a2a1a")),
                  "lilac": dict(extra=dict(antenna="#a89ab8", shell="#b8a0d8", shell_band="#6a508a"), HAIR="#8a7a9a"),
                  "mint": dict(extra=dict(antenna="#8aa89a", shell="#a8e0c8", shell_band="#4a8a6a"), HAIR="#6a8a7a"),
                  "amber": dict(extra=dict(antenna="#a8906a", shell="#d8902a", shell_band="#7a4410"), HAIR="#8a5a3a")}),
    # twenty-sixth wave: the Min Min light of the Queensland outback, a girl under a glowing yellow cap who trails pale tentacles in place of legs and floats
    "min_min_mollusk": dict(
        build="petite", outfit=dict(top=["halter"], colors=dict(cloth2="#f4ecc8")), lower="none",
        hair="bob", bangs="wispy", ears=None, legs="bare", foot="shoe", body="jelly", tent_hair=False, glow=["bell_a", "bell_b", "bell_c", "jtent_a", "jtent_b"],
        pal=dict(SKIN=skin("#fff4dc"), HAIR="#f4e8b0", FUR="#f8d850", LIGHT="#fff8d0", MARK="#e8b030", EYE="#4a3a1a", CLOTH="#f4ecc8", TRIM="#f8d850",
                 INNER="#f8c8b0", extra=dict(boot="#f4e8b0")),
        variants={"white": dict(FUR="#f4f4e8", HAIR="#ffffff", MARK="#c8c8b0", LIGHT="#ffffff", EYE="#3a3a4a", CLOTH="#ffffff", TRIM="#f4f4e8"),
                  "green": dict(FUR="#a8f0a0", HAIR="#d8f8d0", MARK="#5ac868", LIGHT="#e8ffe0", EYE="#1e4a2a", CLOTH="#e0f8d8", TRIM="#a8f0a0"),
                  "blue": dict(FUR="#90d0f8", HAIR="#d0ecfc", MARK="#4a9ae0", LIGHT="#e0f4ff", EYE="#1e3a6a", CLOTH="#dcf0fc", TRIM="#90d0f8"),
                  "red": dict(FUR="#f89a70", HAIR="#fcd8b8", MARK="#e0603a", LIGHT="#ffe4d0", EYE="#5a1e14", CLOTH="#fce4d0", TRIM="#f89a70")}),
    "jellyfish_mollusk": dict(
        build="slim", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["pearls"], colors=dict(cloth2="#c8e0f4")),
        hair="flowing", bangs="wispy", ears=None, legs="stockings", foot="shoe", body="jelly", glow=["bell_a", "bell_b", "bell_c", "jtent_a", "jtent_b"],
        pal=dict(SKIN=skin("#e4eef6"), HAIR="#a8d0f0", FUR="#c8e4f8", LIGHT="#f4faff", MARK="#7ab0e0", EYE="#3a7ac8", CLOTH="#e0eef8", TRIM="#a8c8f0",
                 INNER="#f0d0e8", extra=dict(stocking="#d8eaf8", boot="#a8c8f0")),
        variants={"pink": dict(FUR="#f8c8e0", HAIR="#f0a8cc", MARK="#d878a8", EYE="#c84a8a", CLOTH="#f8e0ee", TRIM="#f0a8cc"),
                  "box": dict(FUR="#e8f4f4", HAIR="#d0ecec", MARK="#a8d8d8", EYE="#2a8a8a", CLOTH="#f4fafa", TRIM="#a8d8d8"),
                  "lion": dict(FUR="#f0a050", HAIR="#e8783a", MARK="#c8502a", EYE="#a83a1a", CLOTH="#f8e0c0", TRIM="#e8783a"),
                  "purple": dict(FUR="#c8a8e8", HAIR="#9a78d8", MARK="#6a4ab0", EYE="#6a3ac8", CLOTH="#e8dcf8", TRIM="#9a78d8", pattern="stripes")}),
    "ammonite_mollusk": dict(
        build="slim", outfit=dict(top=["shells"], extras=["pearls"]),
        hair="flowing", bangs="parted", ears="fin", body="ammonite",
        pal=dict(SKIN=skin("pale"), HAIR="#c8d8e0", FUR="#e8dccc", LIGHT="#fffaf0", MARK="#a8907a", EYE="#4a8aa8", CLOTH="#f0e0d0", TRIM="#e8d0a0",
                 INNER="#f0d8d0", extra=dict(shell="#e8dccc", shell_band="#a88a6a")),
        variants={"amber": dict(extra=dict(shell="#d8a050", shell_band="#7a4a1a"), HAIR="#e8c080"),
                  "opal": dict(extra=dict(shell="#c8e0f0", shell_band="#e0a8d8"), HAIR="#e0e8f8"),
                  "ruby": dict(extra=dict(shell="#c83a4a", shell_band="#6a1a2a"), HAIR="#f0a0a8"),
                  "jet": dict(extra=dict(shell="#2a2628", shell_band="#6a6266"), HAIR="#4a4a52")}),
}

TENT = [("tent_a", 2.25, 6, 2.25), ("tent_b", 1.75, 5.5, 1.75), ("tent_c", 1.25, 5, 1.25)]


def declare_hook(sp, d):
    b = d["body"]
    if b == "ika":
        return [("hat_a", 9, 2.5, 9), ("hat_b", 6.5, 3, 6.5), ("hat_c", 3.5, 2, 3.5), ("hat_fin", 4, 3, .5),
                ("htent_a", 1.5, 6, 1.5), ("htent_b", 1.25, 6, 1.25)]
    if b == "octopus":
        out = [("stent_a", 1.5, 4, 1.5), ("stent_b", 1.25, 3.5, 1.25)]
        if d.get("hat", True):
            out += [("hat_a", 9.5, 3, 9.5), ("hat_b", 7, 3, 7)]
        if d.get("tent_hair"):
            out += [("htent_a", 1.5, 6, 1.5), ("htent_b", 1.25, 6, 1.25)]
        if d.get("dogs"):                                  # Scylla's dog heads: a head, a muzzle, two ears
            out += [("dog_head", 2.5, 2.5, 2.5), ("dog_muzzle", 1.5, 1.25, 1.5), ("dog_ear", .75, 1.25, .5)]
        return out
    if b == "snail":
        return [("shell_a", 7, 7, 4), ("shell_b", 5, 5, 3), ("shell_c", 3, 3, 2)]
    if b == "slug":                                        # the slug's mantle on her back, the bile dribbling from her maw
        return [("mantle", 6, 7, 2), ("bile", 1.5, 2.5, .5), ("bile_drip", .5, 2, .5)]
    if b == "jelly":
        return [("bell_a", 10, 3, 10), ("bell_b", 8, 2.5, 8), ("bell_c", 5, 1.5, 5), ("jtent_a", .5, 7, .5), ("jtent_b", .5, 6, .5)]
    return []


def paint_hook(pt, pal, d):
    mant = G.fur_fn(pal, light_faces=("down",), grain=0.06, seed=41)

    def spotted(f, x, y, w, h):
        if pal.pattern == "rings" and ((x // 3 + y // 3) % 3 == 0):
            return pick(pal.MARK, 0.55, x, y) if (x % 3 + y % 3) in (1, 2) else pick(pal.FUR, 0.6, x, y)
        if (x * 7 + y * 13) % 17 == 0:
            return pick(pal.MARK, 0.5, x, y)
        return mant(f, x, y, w, h)
    for p in ("hat_a", "hat_b", "hat_c", "hat_fin", "tent_a", "tent_b", "tent_c", "stent_a", "stent_b", "htent_a", "htent_b"):
        pt(p, "all", spotted)
    # suckers on the underside of the tentacles
    for p in ("tent_a", "tent_b", "tent_c", "stent_a", "stent_b"):
        pt(p, ("north",), lambda f, x, y, w, h: pick(pal.LIGHT, 0.75 if (y % 2 == 0 and x % 2 == 0) else 0.55, x, y))
    for p in ("htent_a", "htent_b"):
        pt(p, "all", lambda f, x, y, w, h: pick(pal.HAIR, 0.45 + 0.2 * (y % 3 == 0) + (noise(x, y, 4) - 0.5) * 0.1, x, y))
    shell = G.shades(pal.extra.get("shell", "#9a6a3a"))
    band = G.shades(pal.extra.get("shell_band", "#4a3020"))
    for p in ("shell_a", "shell_b", "shell_c", "coil_a", "coil_b", "coil_c", "coil_rim"):
        pt(p, "all", lambda f, x, y, w, h: pick(band if (x + y) % 5 == 0 or (y % 6 == 0) else shell, 0.45 + 0.2 * (noise(x // 2, y // 2, 8) > 0.6), x, y))
    pt("skirt_m", "all", spotted)
    mantle = G.shades(pal.extra.get("mantle", "#a8202a"))
    pt("mantle", "all", lambda f, x, y, w, h: pick(mantle, 0.45 + 0.25 * ((x * 3 + y * 5) % 7 == 0) + 0.15 * (y < 2), x, y))
    bile = G.shades(pal.extra.get("bile", "#b84ad8"))
    for p in ("bile", "bile_drip"):
        pt(p, "all", lambda f, x, y, w, h: G.with_alpha(pick(bile, 0.55 + 0.25 * (x == 0), x, y), 215))
    dog, dark = G.shades(pal.extra.get("dog", "#5a6a74")), G.shades(pal.extra.get("dog_dark", "#2a343a"))

    def dog_face(f, x, y, w, h):
        if f == "north" and y == 1 and x in (0, w - 1):
            return pick(G.shades("#e8c040"), 0.7, x, y)                    # her eyes
        return pick(dog, 0.45 + 0.2 * (f == "up") + (noise(x, y, 213) - 0.5) * 0.12, x, y)
    pt("dog_head", "all", dog_face)
    pt("dog_muzzle", "all", lambda f, x, y, w, h: pick(G.shades("#f4f0e8"), 0.7, x, y) if f == "north" and y == h - 1 and x % 2 == 0
       else pick(dark, 0.5 if f == "north" and y == 0 else 0.4, x, y))        # a black nose, white fangs along the jaw
    pt("dog_ear", "all", lambda f, x, y, w, h: pick(dark, 0.4, x, y))
    bell = G.shades(pal.FUR) if isinstance(pal.FUR, str) else pal.FUR             # the jellyfish's bell: see-through, ringed
    for p in ("bell_a", "bell_b", "bell_c"):
        pt(p, "all", lambda f, x, y, w, h: G.with_alpha(pick(pal.MARK if (pal.pattern == "stripes" and x % 4 == 0) or (f == "up" and (x + y) % 7 == 0)
                                                              else bell, 0.55 + 0.25 * (y == 0), x, y), 170))
    for p in ("jtent_a", "jtent_b"):
        pt(p, "all", lambda f, x, y, w, h: G.with_alpha(pick(pal.MARK, 0.5 + 0.3 * (y % 3 == 0), x, y), 200))


def build_hook(r, g, d):
    b = d["body"]
    hy, z = g.hy, g.z
    if b == "ika":
        r.add("hat", "head", [0, hy + 8.5, z], [cube("hat_a", [-4.5, hy + 8.2, -4.5 + z]), cube("hat_b", [-3.25, hy + 10.7, -3.25 + z]),
                                                cube("hat_c", [-1.75, hy + 13.7, -1.75 + z]),
                                                G.rot_cube("hat_fin", [-7.4, hy + 10.5, -0.25 + z], [-4.0, hy + 11.5, z], 0, 0, 20),
                                                G.mirror_cube(G.rot_cube("hat_fin", [-7.4, hy + 10.5, -0.25 + z], [-4.0, hy + 11.5, z], 0, 0, 20))])
        for i, (x, zz, ry) in enumerate(((-3.4, 2.6, 20), (-1.2, 3.6, 8), (1.2, 3.6, -8), (3.4, 2.6, -20), (-4.4, 0.4, 70), (4.4, 0.4, -70))):
            P.chain(r, f"hair_tent{i + 1}", "head", (x, hy + 4.5, zz + z), ["htent_a", "htent_b"], rx=-12, ry=ry, bend=-8)
    if b == "octopus" and d.get("hat", True):
        r.add("hat", "head", [0, hy + 8.5, z], [cube("hat_a", [-4.75, hy + 7.8, -4.5 + z]), cube("hat_b", [-3.5, hy + 10.6, -3.0 + z])])
    if b == "octopus" and d.get("tent_hair"):              # the squidkin's hair curls into tentacles at the ends
        for i, (x, zz, ry) in enumerate(((-3.4, 2.6, 20), (-1.2, 3.6, 8), (1.2, 3.6, -8), (3.4, 2.6, -20), (-4.4, 0.4, 70), (4.4, 0.4, -70))):
            P.chain(r, f"hair_tent{i + 1}", "head", (x, hy + 2.5, zz + z), ["htent_a", "htent_b"], rx=-12, ry=ry, bend=-14)
    if b == "octopus":                                     # the skirt of eight short tentacles (every octopus body)
        for i in range(8):
            ang = 360 * i / 8 + 22.5
            import math
            x, zz = 2.4 * math.sin(math.radians(ang)), 1.8 * math.cos(math.radians(ang))
            P.chain(r, f"tskirt{i + 1}", "body", (round(x, 2), 12.6, round(zz, 2)), ["stent_a", "stent_b"], rx=-16, ry=ang + 180, bend=-14)
    if b == "jelly":
        import math
        r.add("bell", "head", [0, hy + 8.5, z], [cube("bell_a", [-5, hy + 7.4, -5 + z]), cube("bell_b", [-4, hy + 10.4, -4 + z]),
                                                 cube("bell_c", [-2.5, hy + 12.9, -2.5 + z])])
        for i in range(10):
            a = 360 * i / 10
            x, zz = 4.4 * math.sin(math.radians(a)), 4.4 * math.cos(math.radians(a))
            P.chain(r, f"bell_tent{i + 1}", "bell", (round(x, 2), hy + 7.6, round(zz + z, 2)), ["jtent_a", "jtent_b"], rx=8, ry=a, bend=6)
    if b == "slug":                                        # the mantle humped over her back, bile from the corner of her grin
        r.add("mantle", "body", [0, 17, 2.0 + z], [cube("mantle", [-3, 13.5, 1.6 + z])])
        r.put("head", cube("bile", [0.5, hy - 0.6, -4.3 + z]), cube("bile_drip", [1.0, hy - 2.6, -4.3 + z]))
    if d.get("dogs"):                                      # six dog heads round her waist, each looking out, snarling
        for i in range(6):
            ang = 360 * i / 6 + 30
            piv = [0, 12.0, z]
            dog = [G.rot_cube("dog_head", [-1.25, 10.8, -5.0 + z], piv, 0, ang, 0), G.rot_cube("dog_muzzle", [-0.75, 11.0, -6.2 + z], piv, 0, ang, 0),
                   G.rot_cube("dog_ear", [-1.25, 13.3, -3.5 + z], piv, 0, ang, 0), G.rot_cube("dog_ear", [0.5, 13.3, -3.5 + z], piv, 0, ang, 0)]
            r.add(f"dog{i + 1}", "body", piv, dog)
    if b == "snail":
        r.add("shell", "body", [0, 17, 2.0 + z], [cube("shell_a", [-3.5, 13.5, 1.8 + z]), cube("shell_b", [-2.5, 14.5, 5.3 + z]), cube("shell_c", [-1.5, 15.5, 7.8 + z])])


# ----------------------------------------------------------------------------- kraken and ammonite (their own lower bodies)
def kraken_declare(sp, d):
    G.declare(TENT + [("skirt_m", 7.5, 2.5, 6)], ears=d["ears"], hair=d["hair"], build=d["build"], outfit=d["outfit"])


def ammonite_declare(sp, d):
    G.declare([("coil_a", 6, 12, 12), ("coil_b", 6.5, 8, 8), ("coil_c", 7, 4.5, 4.5), ("coil_rim", 5, 7, 2)], ears=d["ears"], hair=d["hair"],
              build=d["build"], outfit=d["outfit"])


def kraken_build(sp, d):
    import math
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    G.upper(r, 14, dict(hair=d["hair"], bangs=d.get("bangs"), build=d["build"], outfit=d["outfit"], ears=d["ears"], skirt=False, cuffs=False))
    r.add("mantle", "root", [0, 14, 0], [cube("skirt_m", [-3.75, 12, -3])])
    for i in range(8):
        ang = 360 * i / 8 + 22.5
        x, zz = 2.6 * math.sin(math.radians(ang)), 2.2 * math.cos(math.radians(ang))
        P.chain(r, f"tent{i + 1}", "mantle", (round(x, 2), 13.0, round(zz, 2)), ["tent_a", "tent_b", "tent_c"], rx=-15, ry=ang + 180, bend=-30)
    return r.model(sp)


def ammonite_build(sp, d):
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), build=d["build"], outfit=d["outfit"], ears=d["ears"], skirt=False, cuffs=False), z0=-2.0)
    # a round shell seen from the side: squares turned about X make a disc, smaller discs inside make the whorl
    cs = []
    for part, cy, cz, n in (("coil_a", 6.5, 3.0, 3), ("coil_b", 7.5, 4.6, 3), ("coil_c", 8.6, 5.6, 2)):
        w, h, dd = G.S(part)
        for k in range(n):
            cs.append(G.rot_cube(part, [-w / 2, cy - h / 2, cz - dd / 2], [0, cy, cz], k * (90 / n), 0, 0))
    cs.append(cube("coil_rim", [-2.5, 6.5, -4.6]))
    r.add("coil", "root", [0, 6.5, 3], cs)
    return r.model(sp)


def tentacle_names():
    return [[f"tent{i}", f"tent{i}_b", f"tent{i}_c"] for i in range(1, 9)]


def kraken_anims(an):
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    T = tentacle_names()
    curl = lambda per, amp, ph: merge(*[G.wave(t, per, [amp, amp * 1.4, amp * 1.8], axis=0, phase=0.9, base=None) for t in T])
    an.add("idle", 4.0, True, merge(G.upper_idle(), *[G.wave(t, 4.0, [3, 5, 8], axis=0, phase=0.6 + i * 0.3) for i, t in enumerate(T)]))
    an.add("walk", 1.2, True, merge(G.upper_idle(), *[G.wave(t, 1.2, [10, 14, 18], axis=0, phase=0.8 + (i % 2) * 3.1) for i, t in enumerate(T)],
                                    {"mantle": {"position": Tk((0, (0, 0, 0)), (0.6, (0, 0.6, 0)), (1.2, (0, 0, 0)))}}))
    swim = {"root": {"rotation": hold(70, 0, 0), "position": hold(0, 8, 0)}, "head": {"rotation": hold(-50, 0, 0)}, "hair_back": {"rotation": hold(40, 0, 0)}}
    trail = merge(*[{t[0]: {"rotation": Tk((0, (30, 0, 0)), (0.5, (10, 0, 0)), (1.0, (30, 0, 0)))}, t[1]: {"rotation": hold(10, 0, 0)},
                     t[2]: {"rotation": hold(10, 0, 0)}} for t in T])
    an.add("swim", 1.0, True, merge(swim, trail, sym("arm", hold(20, 0, 20))))
    an.add("run", 0.7, True, merge(swim, trail, sym("arm", hold(10, 0, 10))))
    sit = {"mantle": {"position": hold(0, -3, 0)}, "body": {"position": hold(0, -3, 0)}}
    an.add("sit", 4.0, True, merge(G.upper_idle(), sit, *[{t[0]: {"rotation": hold(-30, 0, 0)}, t[1]: {"rotation": hold(-30, 0, 0)}} for t in T]))
    an.add("lie", 4.0, True, merge(sit, {"body": {"position": hold(0, -4, 0), "rotation": hold(-60, 0, 0)}, "eyelids": {"position": hold(*H.OUT_)}},
                                   *[{t[0]: {"rotation": hold(-40, 0, 0)}} for t in T], sym("arm", hold(-30, 0, -10))))
    an.add("grab", 1.0, True, merge(G.upper_idle(), {t[0]: {"rotation": Tk((0, (0, 0, 0)), (0.5, (-50, 0, 0)), (1, (0, 0, 0)))} for t in T[:4]},
                                    sym("arm", hold(-80, 0, 20)), {"mouth": {"position": hold(*H.OUT_)}}))


def ammonite_anims(an):
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("idle", 4.0, True, merge(G.upper_idle(), {"coil": {"rotation": Tk((0, (0, 0, 0)), (2, (0, 0, 2)), (4, (0, 0, 0)))}}))
    an.add("walk", 1.6, True, merge(G.upper_idle(), {"coil": {"rotation": Tk((0, (0, 0, -3)), (0.8, (0, 0, 3)), (1.6, (0, 0, -3)))},
                                                     "body": {"rotation": Tk((0, (0, 0, 3)), (0.8, (0, 0, -3)), (1.6, (0, 0, 3)))}}))
    an.add("swim", 1.2, True, merge(G.upper_idle(), {"root": {"rotation": hold(20, 0, 0)}, "coil": {"rotation": Tk((0, (0, 0, -4)), (0.6, (0, 0, 4)), (1.2, (0, 0, -4)))}},
                                    sym("arm", hold(30, 0, 20))))
    an.add("run", 0.8, True, merge({"root": {"rotation": hold(30, 0, 0)}}, sym("arm", hold(40, 0, 10))))
    an.add("sit", 4.0, True, merge(G.upper_idle(), {"body": {"position": hold(0, -2, 0)}}))
    an.add("lie", 4.0, True, merge({"body": {"position": hold(0, -3, 0), "rotation": hold(-50, 0, 0)}, "eyelids": {"position": hold(*H.OUT_)}}, sym("arm", hold(-30, 0, -10))))
    an.add("retract", 1.0, False, {"body": {"position": Tk((0, (0, 0, 0)), (0.5, (0, -9, 2)), (1, (0, -12, 3)))}})


def bip_extra(an, sp, d):
    """Ika's hair lash, the ink squirt, the snail's retreat into her shell"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    b = d["body"]
    if b == "octopus" and d.get("tent_hair"):
        locks = [[f"hair_tent{i}", f"hair_tent{i}_b"] for i in range(1, 7)]
        for key in ("idle", "walk", "sit"):
            F._inject(an, key, merge(*[G.wave(l, 3.0, [4, 6], axis=0, phase=0.7 + i) for i, l in enumerate(locks)]))
    if b == "ika":
        locks = [[f"hair_tent{i}", f"hair_tent{i}_b"] for i in range(1, 7)]
        an.add("lash", 0.8, False, merge({l[0]: {"rotation": Tk((0, (0, 0, 0)), (0.3, (60, 0, 0)), (0.5, (-110, 0, 0)), (0.8, (0, 0, 0)))} for l in locks},
                                         {"head": {"rotation": Tk((0, (0, 0, 0)), (0.3, (-10, 0, 0)), (0.5, (14, 0, 0)), (0.8, (0, 0, 0)))}}))
        for key in ("idle", "walk", "sit"):
            F._inject(an, key, merge(*[G.wave(l, 3.0, [4, 6], axis=0, phase=0.7 + i) for i, l in enumerate(locks)]))
    if b == "octopus":
        T = [[f"tskirt{i}", f"tskirt{i}_b"] for i in range(1, 9)]
        for key, per, amp in (("idle", 3.0, 4), ("walk", 1.0, 10), ("run", 0.6, 14)):
            F._inject(an, key, merge(*[G.wave(t, per, [amp, amp * 1.5], axis=0, phase=0.8 + i * 0.7) for i, t in enumerate(T)]))
    if b == "jelly":
        T = [[f"bell_tent{i}", f"bell_tent{i}_b"] for i in range(1, 11)]
        for key, per, amp in (("idle", 3.0, 5), ("walk", 1.2, 8), ("run", 0.7, 10), ("swim", 1.0, 14)):
            F._inject(an, key, merge(*[G.wave(t, per, [amp, amp * 1.4], axis=0, phase=0.6 + i * 0.6) for i, t in enumerate(T)]))
        pulse = Tk((0, (0, 0, 0)), (0.5, (0, 0.8, 0)), (1.0, (0, 0, 0)))
        F._inject(an, "swim", {"bell": {"position": pulse}})
    if b in ("ika", "octopus"):
        an.add("ink", 0.8, False, merge({"body": {"rotation": Tk((0, (0, 0, 0)), (0.2, (-15, 0, 0)), (0.8, (0, 0, 0)))},
                                         "mouth": {"position": Tk((0, H.REST), (0.15, H.OUT_), (0.5, H.OUT_), (0.6, H.REST))}},
                                        sym("arm", Tk((0, (0, 0, 0)), (0.2, (-40, 0, 30)), (0.8, (0, 0, 0))))))
    if b == "snail":
        an.add("retract", 1.0, False, merge({
            "body": {"rotation": Tk((0, (0, 0, 0)), (0.5, (70, 0, 0)), (1, (80, 0, 0))), "position": Tk((0, (0, 0, 0)), (1, (0, -6, 2)))},
            "leg_r": {"position": hold(0, -5, 0), "rotation": hold(-90, 0, 0)}, "leg_l": {"position": hold(0, -5, 0), "rotation": hold(-90, 0, 0)},
            "shin_r": {"rotation": hold(150, 0, 0)}, "shin_l": {"rotation": hold(150, 0, 0)},
            "head": {"rotation": hold(40, 0, 0)}, "eyelids": {"position": hold(*H.OUT_)}, "ear_r": {"rotation": hold(60, 0, 0)}, "ear_l": {"rotation": hold(60, 0, 0)},
        }, sym("arm", hold(-120, 0, -20))))


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.25, -30, 8), ("swim", 0.3, -60, 10), ("sit", 0, -30, 10), ("idle", 1.0, -150, 15)]


def run_custom(sp, decl, bld, anm, prev):
    d = SPECIES[sp]
    decl(sp, d)
    pal = F.pal_for(d)

    def painter(pal):
        pt_ = H.Painter()
        G.paint_upper(pt_, pal, top="skin", outfit=d.get("outfit"), build=d.get("build"))
        if d["ears"] == "fin":
            pt_("ear_a", "all", lambda f, x, y, w, h: pick(pal.FUR, 0.5 + 0.2 * (x % 3 == 0), x, y))
        paint_hook(pt_, pal, d)
        return pt_.T
    tex = painter(pal)
    geo, adoc, texes = G.write(sp, tex, bld(sp, d), anm, list(d["variants"]), lambda k: painter(F.pal_for(d, k)))
    h = G.model_height(geo)
    if prev:
        G.previews(sp, geo, adoc, texes, VIEWS, F.FACE, h)
    return geo, tex, adoc["animations"][f"animation.{sp}.idle"], h


# homage looks of the thirtieth wave (POP_CULTURE_VARIANTS.md)
SPECIES["octopus_mollusk"]["variants"]["sea_witch"] = dict(HAIR="#f4f4f0", FUR="#3a1e5a", LIGHT="#8a5ab0", MARK="#1a0e2e", EYE="#3a8a5a", CLOTH="#14141c", TRIM="#e8c050", SKIN=skin("lilac"))


if __name__ == "__main__":
    import family_concepts as FC
    want = [a for a in sys.argv[1:] if not a.startswith("--")]
    prev = "--previews" in sys.argv
    heights = {s["id"]: s["height"] for s in FC.family("mollusk")["species"]}
    entries = {}
    for sp, (decl, bld, anm) in {"kraken_mollusk": (kraken_declare, kraken_build, kraken_anims), "giant_kraken_mollusk": (kraken_declare, kraken_build, kraken_anims),
                                  "ammonite_mollusk": (ammonite_declare, ammonite_build, ammonite_anims)}.items():
        if not want or sp in want:
            g, t, a, h = run_custom(sp, decl, bld, anm, prev)
            entries[sp] = (g, t, a, heights[sp], h)
    bip = {k: v for k, v in SPECIES.items() if v["body"] in ("ika", "octopus", "snail", "jelly", "slug")}
    for e, sp in zip(F.run_family("mollusk", bip, declare_hook, paint_hook, build_hook, bip_extra, views=VIEWS, only=[k for k in bip if not want or k in want]),
                     [k for k in [s["id"] for s in FC.family("mollusk")["species"]] if k in bip and (not want or k in want)]):
        entries[sp] = e
    if prev and not want:
        G.roster(os.path.join(G.PREVIEWS, "mollusk_roster.png"), [entries[s["id"]] for s in FC.family("mollusk")["species"] if s["id"] in entries])
