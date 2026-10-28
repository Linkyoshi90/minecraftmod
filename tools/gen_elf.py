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
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

SPECIES = {
    # sixteenth wave: the valkyrie, in mail and a winged helm, with great white wings and a sword at her hip
    "valkyrie_elf": dict(
        build="athletic", outfit=dict(top=["corset", "capelet"], bottom="wrap_skirt", extras=["bracers", "belt"], colors=dict(cloth2="#3a5a8a")),
        hair="braid", bangs="swept", ears=None, legs="boots", foot="shoe", wings="feather", wing_scale=1.15, flies=True, helm=True, sword=True,
        pal=dict(SKIN=skin("fair"), HAIR="#f0d890", FUR="#f0d890", LIGHT="#fff8e0", MARK="#a8a8b0", EYE="#4a7ab8", CLOTH="#b8bcc8", TRIM="#e8c050",
                 extra=dict(boot="#5a4a3a", helm="#c8ccd4", wing="#f4f2ee", wing_tip="#c8c4bc")),
        variants={"gold": dict(CLOTH="#d8b048", extra=dict(helm="#e8c050", cloth2="#7a2a2a")),
                  "raven": dict(HAIR="#1e1c24", EYE="#8a8a9a", extra=dict(wing="#2a2830", wing_tip="#141218", helm="#5a5a64", cloth2="#2a2a32")),
                  "red": dict(HAIR="#c8502a", extra=dict(cloth2="#8a1e1e", wing="#f4ece4", wing_tip="#a83a2a")),
                  "blue": dict(HAIR="#e8e4f0", EYE="#3a5ab8", extra=dict(cloth2="#1e3a7a", helm="#a8b8d8", wing="#e8f0fa", wing_tip="#6a8ab8"))}),
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
                  "sun": dict(HAIR="#f0a868", EYE="#3a7ab0"),
                  # homage (2026-10-28): Princess Zelda (Zelda): golden-brown hair, a white and lilac gown, gold trim
                  "sage_princess": dict(HAIR="#c8944a", EYE="#3a6ac8", TRIM="#e8b840",
                                        extra=dict(kimono="#f8f6fc", kimono_trim="#e8b840", obi="#9a7ad0", cloth2="#d8c8f0", boot="#e8e0d0"))}),
    "wood_elf": dict(
        build="athletic", outfit=dict(top=["bandeau", "vest"], bottom="shorts", extras=["belt", "pouch", "bracers"], colors=dict(leather="#6a4a2a")),
        hair="braid", bangs="swept", ears="elf", legs="boots", foot="shoe", quiver=True, hero_cap=True,
        pal=dict(SKIN=skin("tan"), HAIR="#6a4422", FUR="#6a4422", LIGHT="#d8c098", MARK="#3a5a2a", EYE="#4a8a3a", CLOTH="#4a7a3a", TRIM="#a8884a",
                 extra=dict(boot="#4a3420")),
        variants={"autumn": dict(CLOTH="#a8582a", HAIR="#c8783a", TRIM="#d8a840"),
                  "moss": dict(CLOTH="#3a5a2a", HAIR="#1e1a16"),
                  "birch": dict(CLOTH="#a8a89a", HAIR="#e8d8a0", SKIN=skin("fair")),
                  "bark": dict(CLOTH="#4a3424", HAIR="#a8382a"),
                  # homage (2026-10-28): Link (Zelda): blonde, a green tunic and its long green cap, brown belt and boots
                  "green_hero": dict(CLOTH="#3a8a3a", HAIR="#f0d070", TRIM="#c8a050", SKIN=skin("fair"), EYE="#3a6ac8",
                                     extra=dict(boot="#5a3a1e", leather="#6a4a2a", cap="#3a8a3a"))}),
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
    # eleventh wave: the brownie, a tiny house spirit in ragged clothes (the red variant wears a little cap)
    # eighteenth wave: the domovoi, the Slavic house spirit behind the stove: straw hair, an embroidered smock, a red kerchief
    "domovoi_elf": dict(
        build="petite", outfit=dict(top=["sweater"], bottom="wrap_skirt", extras=["belt"], colors=dict(cloth2="#c82a2a", leather="#7a5a3a")),
        hair="braid", bangs="parted", ears="elf", legs="bare", foot="shoe", kerchief=True,
        pal=dict(SKIN=skin("peach"), HAIR="#d8b868", FUR="#d8b868", LIGHT="#f4e8c0", MARK="#3a2a1e", EYE="#3a6ac8", CLOTH="#f4f0e4", TRIM="#c82a2a",
                 extra=dict(boot="#5a3a22", kerchief="#c82a2a", kerchief_dot="#f4f0e4", soot=True)),
        variants={"blue": dict(TRIM="#2a4ab0", extra=dict(boot="#5a3a22", kerchief="#2a4ab0", kerchief_dot="#f4f0e4", soot=True, cloth2="#2a4ab0")),
                  "green": dict(TRIM="#2a7a3a", HAIR="#a87a3a", extra=dict(boot="#5a3a22", kerchief="#2a7a3a", kerchief_dot="#f8e070", soot=True, cloth2="#2a7a3a")),
                  "soot": dict(CLOTH="#8a8680", HAIR="#5a4a3a", TRIM="#3a3632",
                               extra=dict(boot="#2a2420", kerchief="#3a3632", kerchief_dot="#8a8680", soot=True, cloth2="#3a3632")),
                  "gold": dict(TRIM="#c8902a", HAIR="#f0e0a8", extra=dict(boot="#5a3a22", kerchief="#e8b030", kerchief_dot="#c82a2a", soot=True, cloth2="#c8902a"))}),
    "brownie_elf": dict(
        build="petite", outfit=dict(top=["sweater"], bottom="shorts", extras=["satchel"], colors=dict(cloth2="#4a3424", leather="#5a3a22")),
        hair="shaggy", bangs="messy", ears="elf", legs="bare", foot="shoe", brownie=True,
        pal=dict(SKIN=skin("tan"), HAIR="#5a3a22", FUR="#5a3a22", LIGHT="#c8a880", MARK="#3a2414", EYE="#3a7a3a", CLOTH="#7a5a3a", TRIM="#a88a5a",
                 extra=dict(boot="#3a2414", rags=True)),
        variants={"green": dict(CLOTH="#4a6a3a", HAIR="#3a2a1a"),
                  "grey": dict(CLOTH="#6a6a6e", HAIR="#8a8a8e", EYE="#5a6a8a"),
                  "red": dict(CLOTH="#8a4a2a", HAIR="#c8682a", extra=dict(boot="#3a2414", rags=True, cap="#b82a2a")),
                  "patched": dict(CLOTH="#8a6a4a", HAIR="#4a3424", extra=dict(boot="#3a2414", rags=True, patches=True))}),
    # ninth wave: the deep dark's elf, a sculk circlet and a sculk gem at her collar that glow teal (glow masks)
    "dark_elf": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], bottom="sarong", extras=["bracers"], colors=dict(cloth2="#141c24")),
        hair="hime", bangs="wispy", ears="elf", legs="stockings", foot="shoe", sculk=True, glow=["sculk_band", "sculk_gem"],
        pal=dict(SKIN="#6a7a8a", HAIR="#e8eef4", FUR="#6a7a8a", LIGHT="#a8b8c8", MARK="#0e2a32", EYE="#4ae8e0", CLOTH="#1a2630", TRIM="#2ab8b0",
                 extra=dict(boot="#0e141a", stocking="#141c24", sculk="#0e3a44", sculk_glow="#4ae8e0")),
        variants={"ash": dict(SKIN="#8a8a8e", HAIR="#2a2a2e", CLOTH="#2a2a2e", TRIM="#7ad8e8"),
                  "abyss": dict(SKIN="#3a404a", HAIR="#b8c8d8", CLOTH="#0a0e12", TRIM="#2a8ab0", EYE="#2ab8e8",
                                extra=dict(boot="#06080a", stocking="#0a0e12", sculk="#0a2030", sculk_glow="#2ab8e8")),
                  "echo": dict(SKIN="#5a8a8e", HAIR="#0e3a44", CLOTH="#0e3a44", TRIM="#8af8f0",
                               extra=dict(boot="#06141a", stocking="#0e2a32", sculk="#062a32", sculk_glow="#8af8f0")),
                  "pale": dict(SKIN=skin("pale"), HAIR="#fbfbff", CLOTH="#2a3440", EYE="#8ad8f8")}),
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
    if d.get("sculk"):
        out += [("sculk_band", 8.75, 1, 8.75), ("sculk_gem", 1.5, 1.5, .5)]
    if d.get("kerchief"):
        out += [("kerchief", 8.75, 3.5, 8.75), ("kerchief_knot", 2, 1.5, 1), ("kerchief_tail", 1.5, 2.5, .25)]
    if d.get("brownie"):
        out += [("bcap", 5.5, 3, 5.5), ("bcap_tip", 2, 2, 2)]
    if d.get("helm"):                                    # the valkyrie's winged helm
        out += [("helm", 8.75, 3, 8.75), ("helm_wing", .5, 3.5, 3)]
    if d.get("hero_cap"):                                # the green hero's long cap (drawn only on her)
        out += [("hcap", 8.75, 2.5, 8.75), ("hcap_tail", 3, 2.5, 6)]
    return out


def paint_hook(pt, pal, d):
    helm = G.shades(pal.extra.get("helm", "#c8ccd4"))
    pt("helm", "all", lambda f, x, y, w, h: pick(helm, 0.55 + 0.25 * (f == "up") + 0.15 * (y == h - 1) - 0.12 * (x % 6 == 0), x, y))
    pt("helm_wing", "all", lambda f, x, y, w, h: pick(G.shades(pal.extra.get("wing", "#f4f2ee")), 0.6 + 0.15 * (y % 2), x, y))
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
    kc, kd = G.shades(pal.extra.get("kerchief", "#c82a2a")), G.shades(pal.extra.get("kerchief_dot", "#f4f0e4"))
    dotted = lambda f, x, y, w, h: pick(kd, 0.7, x, y) if (x % 3 == 1 and y % 3 == 1) else pick(kc, 0.5 + 0.12 * (f == "up"), x, y)
    pt("kerchief", "all", lambda f, x, y, w, h: CLEAR if f == "down" else dotted(f, x, y, w, h))
    pt("kerchief_knot", "all", lambda f, x, y, w, h: pick(kc, 0.45, x, y))
    pt("kerchief_tail", "all", dotted)
    if pal.extra.get("soot"):                            # a smudge of soot on her nose and cheek
        pt("head", ("north",), lambda f, x, y, w, h: pick(G.shades("#3a3432"), 0.45, x, y) if (x, y) in ((15, 22), (16, 22), (16, 21), (22, 24), (23, 24)) else None)
    capc = G.shades(pal.extra.get("cap", "#b82a2a"))
    pt("hcap", "all", lambda f, x, y, w, h: CLEAR if f == "down" or not pal.extra.get("cap") else pick(capc, 0.5 + 0.1 * (f == "up"), x, y))
    pt("hcap_tail", "all", lambda f, x, y, w, h: pick(capc, 0.45 + 0.1 * (y % 2), x, y) if pal.extra.get("cap") else CLEAR)
    pt("bcap", "all", lambda f, x, y, w, h: pick(capc, 0.5 + 0.1 * (y == 0), x, y) if pal.extra.get("cap") else CLEAR)
    pt("bcap_tip", "all", lambda f, x, y, w, h: pick(capc, 0.55, x, y) if pal.extra.get("cap") else CLEAR)
    if pal.extra.get("rags"):                            # ragged hems, and patches for the patched one
        for p in [p for p in H.PARTS if p.startswith(("sh_", "knit", "turtle", "short"))]:
            pt(p, "all", lambda f, x, y, w, h: CLEAR if f in ("north", "south", "east", "west") and y == h - 1 and x % 3 == 1
               else pick(G.shades("#c8a050" if (x // 3 + y // 3) % 3 == 0 else "#5a7a4a"), 0.5, x, y) if pal.extra.get("patches") and (x // 3 + y // 2) % 4 == 0
               else None)
    sk = G.shades(pal.extra.get("sculk", "#0e3a44"))
    glow = G.shades(pal.extra.get("sculk_glow", "#4ae8e0"))
    pt("sculk_band", "all", lambda f, x, y, w, h: pick(glow, 0.6, x, y) if (x * 7 + y * 3) % 9 == 0 else pick(sk, 0.4 + (noise(x, y, 41) - 0.5) * 0.2, x, y))
    pt("sculk_gem", "all", lambda f, x, y, w, h: pick(glow, 0.55 + 0.3 * (x == 0 and y == h - 1), x, y))


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    if d.get("helm"):
        r.put("head", cube("helm", [-4.375, hy + 5.9, -4.375 + z], inflate=0.6),
              G.rot_cube("helm_wing", [-5.2, hy + 6.2, -1.0 + z], [-5.0, hy + 6.2, z], 0, 0, 20),
              G.rot_cube("helm_wing", [4.7, hy + 6.2, -1.0 + z], [5.0, hy + 6.2, z], 0, 0, -20))
    if d.get("circlet"):
        r.put("head", cube("circlet", [-4.375, hy + 5.6, -4.375 + z], inflate=0.55), cube("gem", [-0.5, hy + 5.5, -5.35 + z]))
    if d.get("quiver"):
        r.put("body", G.rot_cube("quiver", [-1.0, 14.5, 2.0 + z], [0, 17.5, 3.0 + z], 0, 0, 30),
              G.rot_cube("fletch", [-0.75, 20.3, 2.25 + z], [0, 17.5, 3.0 + z], 0, 0, 30))
    if d.get("hat"):
        r.add("hat", "head", [0, hy + 8.5, z], [cube("brim", [-6, hy + 8.3, -6 + z]), cube("crown", [-3.5, hy + 8.7, -3.5 + z]),
                                                cube("hatband", [-3.625, hy + 8.75, -3.625 + z])])
    if d.get("kerchief"):                                # a dotted kerchief over her hair, knotted at the nape, its ends hanging down
        r.add("kerchief", "head", [0, hy + 6, z], [cube("kerchief", [-4.375, hy + 5.4, -4.375 + z], inflate=0.65),
                                                   cube("kerchief_knot", [-1, hy + 4.6, 4.9 + z]),
                                                   G.rot_cube("kerchief_tail", [-1.6, hy + 2.2, 5.4 + z], [-0.6, hy + 4.6, 5.4 + z], 10, 0, -15),
                                                   G.rot_cube("kerchief_tail", [0.1, hy + 2.2, 5.4 + z], [0.6, hy + 4.6, 5.4 + z], 10, 0, 15)])
    if d.get("hero_cap"):                                # a long cap over her hair, its tail flopping down her back (drawn only on the green hero)
        r.add("hair_hcap", "head", [0, hy + 8, z], [cube("hcap", [-4.375, hy + 6.4, -4.375 + z], inflate=0.7),
                                                     G.rot_cube("hcap_tail", [-1.5, hy + 6.6, 3.6 + z], [0, hy + 8.4, 3.6 + z], 35, 0, 0)])
    if d.get("brownie"):                                 # a little pointed cap (drawn only on the red one)
        r.add("bcap", "head", [0, hy + 8.4, z], [cube("bcap", [-2.75, hy + 8.4, -2.75 + z]), cube("bcap_tip", [-1, hy + 11.2, -0.5 + z])])
    if d.get("sculk"):
        r.put("head", cube("sculk_band", [-4.375, hy + 6.0, -4.375 + z], inflate=0.55))
        r.put("body", cube("sculk_gem", [-0.75, 18.2, -2.4 + z]))
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
