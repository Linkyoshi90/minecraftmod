#!/usr/bin/env python3
"""Elves (the elven family, see elf_field_guide.html): night elf, high elf, wood elf, blood elf, desert elf.

    python tools/gen_elf.py [species ...] [--previews]

The most human of the families: the girl_family biped with long pointed elf ears, boots and her people's clothes.
Props: the high elf's circlet (on the head), the wood elf's quiver (on her back), the desert elf's wide-brimmed hat
(bone `hat`), the blood elf's sword at her hip. Five skin/hair colourings per species.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin  # noqa: E402

SPECIES = {
    "night_elf": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], bottom="sarong", extras=["armlets"], colors=dict(cloth2="#2a2a4a")),
        hair="flowing", bangs="parted", ears="elf", legs="boots", foot="shoe",
        pal=dict(SKIN="#a088c8", HAIR="#eceaf4", FUR="#a088c8", LIGHT="#d8c8f0", MARK="#5a4a8a", EYE="#e8e0f8", CLOTH="#3a3a6a", TRIM="#c8c8e0",
                 extra=dict(boot="#2a2440")),
        variants={"indigo": dict(SKIN="#6a68b8", HAIR="#c8d0e0", CLOTH="#2a3a5a"),
                  "teal": dict(SKIN="#6aa8a8", HAIR="#a8e8c8", CLOTH="#2a4a4a", EYE="#e8f8a0"),
                  "dusk": dict(SKIN="#8a8aa8", HAIR="#2a2430", CLOTH="#4a2a4a"),
                  "moon": dict(SKIN="#c8b8e0", HAIR="#8ab0f0", CLOTH="#2a2a5a")}),
    "high_elf": dict(
        build="slim", outfit=dict(top=["kimono"], bottom="hakama", extras=["pearls"],
                                  colors=dict(kimono="#f4f2ea", kimono_trim="#d8b040", obi="#d8b040", cloth2="#f0ece0")),
        hair="hime", bangs="blunt", ears="elf", legs="boots", foot="shoe", circlet=True,
        pal=dict(SKIN=skin("pale"), HAIR="#f0d070", FUR="#f0d070", LIGHT="#fff8d8", MARK="#c8a040", EYE="#3a8a5a", CLOTH="#f4f2ea", TRIM="#d8b040",
                 extra=dict(boot="#e8e0d0")),
        variants={"silver": dict(HAIR="#e0e4ec", EYE="#4a6aa8"),
                  "auburn": dict(HAIR="#a84a2a", EYE="#4a8a4a", extra=dict(kimono="#2a6a4a", obi="#d8b040", boot="#5a3a22")),
                  "ivory": dict(HAIR="#fbf8f0", EYE="#8a5ab8"),
                  "sun": dict(HAIR="#f0a868", EYE="#3a7ab0")}),
    "wood_elf": dict(
        build="athletic", outfit=dict(top=["bandeau", "vest"], bottom="shorts", extras=["belt", "pouch", "bracers"], colors=dict(leather="#6a4a2a")),
        hair="braid", bangs="swept", ears="elf", legs="boots", foot="shoe", quiver=True,
        pal=dict(SKIN=skin("tan"), HAIR="#6a4422", FUR="#6a4422", LIGHT="#d8c098", MARK="#3a5a2a", EYE="#4a8a3a", CLOTH="#4a7a3a", TRIM="#a8884a",
                 extra=dict(boot="#4a3420")),
        variants={"autumn": dict(CLOTH="#a8582a", HAIR="#c8783a", TRIM="#d8a840"),
                  "moss": dict(CLOTH="#3a5a2a", HAIR="#1e1a16"),
                  "birch": dict(CLOTH="#a8a89a", HAIR="#e8d8a0", SKIN=skin("fair")),
                  "bark": dict(CLOTH="#4a3424", HAIR="#a8382a")}),
    "blood_elf": dict(
        build="athletic", outfit=dict(top=["corset", "halter"], bottom="loincloth", extras=["pauldron", "bracers", "belt"], colors=dict(cloth2="#5a1a1a")),
        hair="ponytail", bangs="jagged", ears="elf", legs="boots", foot="shoe", sword=True,
        pal=dict(SKIN=skin("fair"), HAIR="#c82a2a", FUR="#c82a2a", LIGHT="#f8c8a8", MARK="#5a1a1a", EYE="#f0c040", CLOTH="#8a1a2a", TRIM="#e8b840",
                 extra=dict(boot="#2a1414")),
        variants={"warped": dict(HAIR="#2ab8a8", CLOTH="#1a3a3a", TRIM="#4ae0c8", EYE="#4ae0c8"),
                  "ember": dict(HAIR="#f07a2a", CLOTH="#2a1a14", TRIM="#f0a040"),
                  "ash": dict(HAIR="#9a9aa0", CLOTH="#4a1a1a", SKIN=skin("grey")),
                  "gilded": dict(HAIR="#f0d070", CLOTH="#f0ece0", TRIM="#e8b840")}),
    "desert_elf": dict(
        build="athletic", outfit=dict(top=["sports", "vest"], bottom="shorts", extras=["scarf", "belt", "satchel"],
                                      colors=dict(scarf="#c83a2a", leather="#8a5a32")),
        hair="sidetail", bangs="messy", ears="elf", legs="boots", foot="shoe", hat=True,
        pal=dict(SKIN=skin("brown"), HAIR="#e8c890", FUR="#e8c890", LIGHT="#f8e8c8", MARK="#8a5a2a", EYE="#5aa8c8", CLOTH="#c8a878", TRIM="#c8a050",
                 extra=dict(boot="#6a4024", hat="#a87a4a")),
        variants={"dusk": dict(extra=dict(scarf="#6a3a8a", hat="#2a2420", boot="#2a2420", leather="#3a3028"), CLOTH="#4a4048"),
                  "sage": dict(extra=dict(scarf="#5a7a5a", hat="#8a8a72", boot="#4a4a3a"), CLOTH="#9aa08a"),
                  "rust": dict(extra=dict(scarf="#a83a1a", hat="#7a3a1a", boot="#4a2010"), CLOTH="#c8784a", HAIR="#7a3a1a"),
                  "bone": dict(extra=dict(scarf="#3a6ab0", hat="#f0e8d8", boot="#c8b8a0"), CLOTH="#f0e8d8", HAIR="#2a2420")}),
}


def declare_hook(sp, d):
    out = []
    if d.get("circlet"):
        out += [("circlet", 8.75, .75, 8.75), ("gem", 1, 1, .5)]
    if d.get("quiver"):
        out += [("quiver", 2, 6, 2), ("fletch", 1.5, 1.5, 1.5)]
    if d.get("hat"):
        out += [("brim", 12, .5, 12), ("crown", 7, 3.5, 7), ("hatband", 7.25, .75, 7.25)]
    if d.get("sword"):
        out += [("scabbard", 1, 7, 1.5), ("hilt", 2.5, .75, .75)]
    return out


def paint_hook(pt, pal, d):
    gold = G.shades("#e8b840")
    pt("circlet", "all", lambda f, x, y, w, h: pick(gold, 0.6 + 0.25 * (x % 3 == 0), x, y))
    pt("gem", "all", lambda f, x, y, w, h: pick(G.shades("#3ad0a0"), 0.7, x, y))
    lea = G.shades("#6a4a2a")
    pt("quiver", "all", lambda f, x, y, w, h: pick(lea, 0.45 + 0.2 * (y % 4 == 0), x, y))
    pt("fletch", "all", lambda f, x, y, w, h: pick(G.shades("#e8e0d0"), 0.55 + 0.25 * (x % 2), x, y))
    hat = G.shades(pal.extra.get("hat", "#a87a4a"))
    pt("brim", "all", lambda f, x, y, w, h: pick(hat, 0.5 + (noise(x, y, 3) - 0.5) * 0.1, x, y))
    pt("crown", "all", lambda f, x, y, w, h: pick(hat, 0.55 + (noise(x, y, 4) - 0.5) * 0.1 - (0.1 if f == "up" and abs(x - w / 2) < 1 else 0), x, y))
    pt("hatband", "all", lambda f, x, y, w, h: pick(G.shades("#3a2a1e"), 0.4, x, y))
    pt("scabbard", "all", lambda f, x, y, w, h: pick(G.shades("#2a1a1a"), 0.45 + 0.3 * (y == 0), x, y))
    pt("hilt", "all", lambda f, x, y, w, h: pick(gold, 0.6, x, y))


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    if d.get("circlet"):
        r.put("head", cube("circlet", [-4.375, hy + 5.6, -4.375 + z], inflate=0.55), cube("gem", [-0.5, hy + 5.5, -5.35 + z]))
    if d.get("quiver"):
        r.put("body", G.rot_cube("quiver", [-1.0, 14.5, 2.0 + z], [0, 17.5, 3.0 + z], 0, 0, 30),
              G.rot_cube("fletch", [-0.75, 20.3, 2.25 + z], [0, 17.5, 3.0 + z], 0, 0, 30))
    if d.get("hat"):
        r.add("hat", "head", [0, hy + 8.5, z], [cube("brim", [-6, hy + 8.3, -6 + z]), cube("crown", [-3.5, hy + 8.7, -3.5 + z]),
                                                cube("hatband", [-3.625, hy + 8.75, -3.625 + z])])
    if d.get("sword"):
        hw = G.S("hips")[0]
        r.put("body", G.rot_cube("scabbard", [-hw / 2 - 1.1, 6.0, -0.75 + z], [-hw / 2 - 0.6, 12.5, z], 20, 0, 0),
              G.rot_cube("hilt", [-hw / 2 - 1.85, 12.6, -0.375 + z], [-hw / 2 - 0.6, 12.5, z], 20, 0, 0))


def anim_extra(an, sp, d):
    """drawing a bow or a crossbow ("shoot"), a little bow of the head for a trade ("trade")"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("shoot", 1.0, False, merge({"body": {"rotation": hold(0, -20, 0)}, "head": {"rotation": hold(0, 20, 0)},
                                       "arm_l": {"rotation": hold(-90, 0, 0)},
                                       "arm_r": {"rotation": Tk((0, (-90, 0, 0)), (0.6, (-90, -40, 0)), (0.8, (-90, 0, 0)), (1, (-90, 0, 0)))}}))
    an.add("trade", 1.5, False, merge({"body": {"rotation": Tk((0, (0, 0, 0)), (0.5, (14, 0, 0)), (1.0, (14, 0, 0)), (1.5, (0, 0, 0)))},
                                       "eyelids": {"position": Tk((0, H.REST), (0.4, H.OUT_), (1.1, H.OUT_), (1.2, H.REST))}},
                                      sym("arm", hold(-20, 0, 0))))


if __name__ == "__main__":
    F.run_family("elf", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
