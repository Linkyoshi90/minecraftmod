#!/usr/bin/env python3
"""Flora (the plant family, see flora_field_guide.html): alraune, matango, ent, cactus, mandragora.

    python tools/gen_flora.py [species ...] [--previews]

girl_family upper bodies over plant lower bodies (lower="none" plus a bone of her own):
    alraune     rises from a great flower (bone `flower`: eight outer petals flat on the ground, cupped inner petals)
    matango     a mushroom cap for a hat and a skirt of mycelium spreading over the ground (`mycelium`); `disguise` is a
                cluster of mushrooms, shown while she has sunk into the ground
    ent         a big bark-skinned biped with a crown of leaves and branches growing from her shoulders
    cactus      a cactus column for a lower body with two cactus arms, a flower in her hair
    mandragora  a little root girl on a root bulb with trailing root tendrils, a tuft of leaves on her head; `disguise`
                is the leaf tuft alone, shown while she is buried
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
import girl_parts as P  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

SPECIES = {
    # seventeenth wave: the dryad, a tree nymph of Greek myth: a girl of pale blue wood with leafy branches for antlers
    "dryad_flora": dict(
        build="slim", outfit=dict(top=[]), hair="wavy", bangs="swept", ears="elf", legs="bare", foot="claw", plant="dryad", horns="antlers",
        pal=dict(SKIN="#8ab0e0", HAIR="#6a98d8", FUR="#7aa0d8", LIGHT="#c8dcf8", MARK="#4a6aa8", EYE="#1e3a7a", CLOTH="#5a8ad0", TRIM="#4a6aa8",
                 extra=dict(horn="#6a88b8", leaf="#6ac0a0", claw="#6a88b8")),
        variants={"oak": dict(SKIN="#a8805a", HAIR="#5a8a3a", FUR="#8a6a4a", LIGHT="#d8c0a0", MARK="#5a3e26", EYE="#3a5a1e", extra=dict(horn="#6a4a2e", leaf="#5aa84a", claw="#6a4a2e")),
                  "birch": dict(SKIN="#ece8dc", HAIR="#9ac87a", FUR="#e0dccc", LIGHT="#ffffff", MARK="#3a3a3a", EYE="#3a6a2a", extra=dict(horn="#e8e4d8", leaf="#8ac85a", claw="#e8e4d8")),
                  "cherry": dict(SKIN="#c88a8a", HAIR="#f4b8c8", FUR="#a86a6a", LIGHT="#f8d8e0", MARK="#6a3a3a", EYE="#6a2a3a", extra=dict(horn="#5a2e2e", leaf="#f8c0d0", claw="#5a2e2e")),
                  "autumn": dict(SKIN="#b88a5a", HAIR="#e07a2a", FUR="#9a6a3a", LIGHT="#f0d0a8", MARK="#5a3418", EYE="#6a2a10", extra=dict(horn="#6a4a2e", leaf="#e8902a", claw="#6a4a2e"))}),
    # twelfth wave: the kesaran pasaran, a little dandelion-seed girl hanging under her fluffy puff, legs dangling ("drift")
    "kesaran_pasaran_flora": dict(
        build="petite", outfit=dict(top=["halter"], bottom="wrap_skirt", colors=dict(cloth2="#f0ece4")), hair="bob", bangs="blunt", ears=None,
        legs="bare", foot="shoe", plant="puff",
        pal=dict(SKIN=skin("fair"), HAIR="#f4f2ee", FUR="#f4f2ee", LIGHT="#ffffff", MARK="#c8c4bc", EYE="#5a8ad8", CLOTH="#f8f6f0", TRIM="#c8b890",
                 extra=dict(puff="#fbfaf6", seed="#8a6a3a", boot="#c8b890")),
        variants={"yellow": dict(HAIR="#f8e070", EYE="#d88a2a", CLOTH="#f8e8a0", extra=dict(puff="#f8e470", seed="#8a6a3a", boot="#c8a040")),
                  "blue": dict(HAIR="#a8c8f0", EYE="#3a5ab8", CLOTH="#d8e8f8", extra=dict(puff="#b8d4f4", seed="#4a5a8a", boot="#7a9ac8")),
                  "pink": dict(HAIR="#f8b8d0", EYE="#c84a7a", CLOTH="#f8dce8", extra=dict(puff="#f8c8dc", seed="#8a4a5a", boot="#d888a8")),
                  "black": dict(HAIR="#2a2830", EYE="#e8c040", CLOTH="#3a3840", TRIM="#8a8890", extra=dict(puff="#3a3842", seed="#1e1c20", boot="#5a5860"))}),
    # ninth wave: the venus flytrap rises from a rosette of leaves between two great trap lobes (bones jaw_r/jaw_l) that snap shut ("snap")
    "venus_flytrap_flora": dict(
        build="slim", outfit=dict(top=["bandeau"], extras=["armlets"]), hair="sidetail", bangs="jagged", ears=None, lower="none", plant="trap",
        pal=dict(SKIN="#a8d890", HAIR="#c82a3a", FUR="#c82a3a", LIGHT="#e8f8d0", MARK="#6a1a22", EYE="#e8d040", CLOTH="#5aa83a", TRIM="#c82a3a",
                 extra=dict(leaf="#5aa83a", trap="#d8303a", cilia="#e8f0a0")),
        variants={"red": dict(SKIN="#e8b0a8", HAIR="#a81a2a", CLOTH="#a81a2a", TRIM="#e8d040", extra=dict(leaf="#a8303a", trap="#7a1222", cilia="#f8c8a0")),
                  "purple": dict(SKIN="#c8b0d8", HAIR="#6a2a8a", CLOTH="#5a3a7a", extra=dict(leaf="#6a5a8a", trap="#8a2aa8", cilia="#e8c8f8")),
                  "yellow": dict(SKIN="#d8e8a0", HAIR="#d8a82a", CLOTH="#a8c83a", extra=dict(leaf="#a8c83a", trap="#e8a82a", cilia="#f8f0c0")),
                  "dark": dict(SKIN="#7aa86a", HAIR="#3a0e14", CLOTH="#2a5a1e", extra=dict(leaf="#2a5a1e", trap="#6a1a1e", cilia="#c8d8a0"))}),
    "alraune_flora": dict(
        build="curvy", outfit=dict(top=["bandeau"], extras=["pearls"]), hair="wavy", bangs="parted", ears=None, lower="none", plant="flower",
        pal=dict(SKIN="#b8e0a0", HAIR="#f088b0", FUR="#f088b0", LIGHT="#fcd8e8", MARK="#c84a7a", EYE="#e8c040", CLOTH="#f088b0", TRIM="#7ab85a",
                 extra=dict(petal="#f08ab0", petal_in="#f8c0d8", leaf="#5aa84a")),
        variants={"violet": dict(HAIR="#a888e8", extra=dict(petal="#9a78e0", petal_in="#d8c8f8", leaf="#5aa84a"), CLOTH="#9a78e0"),
                  "sun": dict(HAIR="#f8d040", extra=dict(petal="#f8c830", petal_in="#7a4a1a", leaf="#4a9a3a"), CLOTH="#f8c830"),
                  "white": dict(HAIR="#f8f4ec", extra=dict(petal="#f8f8f0", petal_in="#f8e8a0", leaf="#5aa84a"), CLOTH="#f0f0e8"),
                  "crimson": dict(HAIR="#c82a3a", extra=dict(petal="#c8283a", petal_in="#f8a0a8", leaf="#3a7a2a"), CLOTH="#c8283a")}),
    "matango_flora": dict(
        build="petite", outfit=dict(top=["capelet"], colors=dict()), hair="bob", bangs="blunt", ears=None, lower="none", plant="mushroom",
        pal=dict(SKIN="#f4e8dc", HAIR="#c83a2a", FUR="#c83a2a", LIGHT="#ffffff", MARK="#f8f4ec", EYE="#6a4a8a", CLOTH="#e8dcc8", TRIM="#c83a2a",
                 extra=dict(cap="#c8302a", spot="#f8f4ec", stalk="#ece0cc", mycelium="#8a7a8a")),
        variants={"brown": dict(HAIR="#7a5a3a", extra=dict(cap="#7a5432", spot="#c8a880", stalk="#e8dcc8", mycelium="#8a7a8a")),
                  "glow": dict(HAIR="#3ac8e8", extra=dict(cap="#2a6ab8", spot="#8af0ff", stalk="#d8e8f0", mycelium="#5a6a8a")),
                  "purple": dict(HAIR="#8a3ab8", extra=dict(cap="#7a2aa8", spot="#e8c8f8", stalk="#ece0f0", mycelium="#6a5a7a")),
                  "white": dict(HAIR="#f0ece4", extra=dict(cap="#f4f0e8", spot="#c8c0b0", stalk="#f8f4ec", mycelium="#9a8a9a"))}),
    "leshy_flora": dict(
        build="male", outfit=dict(top=["mantle"], bottom="loincloth", extras=["belt"], colors=dict(cloth2="#3a5a2a")), hair="shaggy", bangs="messy",
        ears=None, legs="fur", foot="hoof", plant="leshy", horns="antlers",
        pal=dict(SKIN="#8a6a48", HAIR="#4a7a3a", FUR="#5a4a32", LIGHT="#8a7a5a", MARK="#3a2a1a", EYE="#7ae860", CLOTH="#3a5a2a", TRIM="#8a6a3a",
                 extra=dict(horn="#a8906a", moss="#4a7a3a", hoof="#3a2a1a")),
        variants={"birch": dict(SKIN="#d8d0c0", HAIR="#7ab85a", FUR="#c8c0b0", extra=dict(horn="#e8e0d0", moss="#7ab85a", hoof="#2a2a2a")),
                  "spruce": dict(SKIN="#5a3e26", HAIR="#2a5a3a", FUR="#4a3220", extra=dict(horn="#8a7050", moss="#2a5a3a", hoof="#2a1a10")),
                  "autumn": dict(HAIR="#c8682a", extra=dict(horn="#a8906a", moss="#c8682a", hoof="#3a2a1a")),
                  "frost": dict(SKIN="#a8b0b0", HAIR="#d8e8e8", FUR="#8a9090", EYE="#5ab0e8", extra=dict(horn="#e8f0f0", moss="#c8dcdc", hoof="#3a3a40"))}),
    "ent_flora": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", colors=dict(bandage="#7a9a4a", cloth2="#5a7a3a")), hair="mane", bangs="messy",
        ears=None, legs="bare", foot="claw", plant="tree",
        pal=dict(SKIN="#8a6a4a", HAIR="#4a8a3a", FUR="#6a4a2e", LIGHT="#a8885a", MARK="#3a2a1a", EYE="#c8e05a", CLOTH="#5a7a3a", TRIM="#3a2a1a",
                 pattern="stripes", extra=dict(bark="#6a4a2e", leaf="#4a8a3a", claw="#3a2a1a")),
        variants={"birch": dict(SKIN="#e8e4d8", FUR="#e8e4d8", MARK="#2a2a2a", HAIR="#7ab85a", extra=dict(bark="#e8e4d8", leaf="#7ab85a", claw="#2a2a2a")),
                  "spruce": dict(SKIN="#5a3e26", FUR="#4a3220", HAIR="#2a5a3a", extra=dict(bark="#4a3220", leaf="#2a5a3a", claw="#2a1a10")),
                  "cherry": dict(SKIN="#6a3a3a", FUR="#5a2e2e", HAIR="#f4b8c8", extra=dict(bark="#5a2e2e", leaf="#f4b8c8", claw="#3a1a1a")),
                  "autumn": dict(HAIR="#e0782a", extra=dict(bark="#6a4a2e", leaf="#e0782a", claw="#3a2a1a"))}),
    "cactus_flora": dict(
        build="slim", outfit=dict(top=["halter"], extras=["armlets"]), hair="ponytail", bangs="swept", ears=None, lower="none", plant="cactus",
        pal=dict(SKIN="#7ab86a", HAIR="#3a8a3a", FUR="#4a9a3a", LIGHT="#a8d88a", MARK="#f8f0d8", EYE="#2a2a2a", CLOTH="#f0a0c0", TRIM="#e8c040",
                 extra=dict(cactus="#4a9a3a", bloom="#f07aa8")),
        variants={"blue": dict(SKIN="#7ab8a8", HAIR="#3a7a7a", extra=dict(cactus="#4a8a8a", bloom="#f8d040"), CLOTH="#f8d040"),
                  "golden": dict(SKIN="#c8c06a", HAIR="#9a8a2a", extra=dict(cactus="#a8a03a", bloom="#f05a3a"), CLOTH="#f05a3a"),
                  "purple": dict(SKIN="#8a8aa8", HAIR="#5a4a7a", extra=dict(cactus="#6a5a8a", bloom="#f8f0a0"), CLOTH="#f8f0a0"),
                  "white": dict(SKIN="#c8d8b8", HAIR="#f0f0e8", MARK="#ffffff", extra=dict(cactus="#a8c098", bloom="#f07aa8")),
                  # homages to Gregory Horror Show (2026-10-25): Cactus Girl and Cactus Gunman
                  "gunman": dict(SKIN="#2e8a46", HAIR="#141414", EYE="#3a2a9a", CLOTH="#e8dcc8", TRIM="#8a2e4a",
                                 extra=dict(cactus="#1e7a3e", bloom="#d83a2a", gunman=True, hat="#f4c018", hat_band="#1e7a3e", bandana="#d81e1e",
                                            poncho="#e8dcc8", poncho_band="#8a2e4a", ribs="#1a6a34")),
                  "blossom": dict(SKIN="#62b434", HAIR="#14603a", EYE="#e0301e", CLOTH="#2a5aa0", TRIM="#f8c830",
                                  extra=dict(cactus="#4aa02e", bloom="#f86aa8", blossom=True, hat="#d8782a", hat_band="#6a3a18", bandana="#f02a10",
                                             poncho="#1e4e98", poncho_band="#f0a030", ribs="#2e8a2a"))}),
    "mandragora_flora": dict(
        build="petite", outfit=dict(top=["halter"], colors=dict()), hair="pixie", bangs="messy", ears=None, lower="none", plant="root",
        pal=dict(SKIN="#e8d0b0", HAIR="#5aa84a", FUR="#d8b890", LIGHT="#f8ecd8", MARK="#8a6a4a", EYE="#3a2a1a", CLOTH="#7ab85a", TRIM="#3a7a2a",
                 extra=dict(root="#d8b890", leaf="#5aa84a")),
        variants={"red": dict(SKIN="#e8a090", extra=dict(root="#c86a5a", leaf="#4a9a3a")),
                  "purple": dict(SKIN="#c8a8d8", extra=dict(root="#9a6ab8", leaf="#4a9a3a")),
                  "golden": dict(SKIN="#f0d890", extra=dict(root="#d8b050", leaf="#7ab84a")),
                  "dark": dict(SKIN="#a8886a", extra=dict(root="#5a3e2a", leaf="#2a6a2a"))}),
}


def declare_hook(sp, d):
    k = d["plant"]
    if k == "trap":
        return [("lobe", .75, 9, 7.5), ("cilia", .5, 2.25, .5), ("bulb", 7, 5, 7), ("leaf", 2.5, .25, 6), ("stem", 2.5, 7, 2.5)]
    if k == "flower":
        return [("petal", 3.5, .5, 7), ("petal_in", 3.5, 6, .5), ("bulb", 7, 5, 7), ("leaf", 2.5, .25, 6), ("stem", 2.5, 7, 2.5)]
    if k == "mushroom":
        return [("cap", 13, 3.5, 13), ("cap_top", 8, 2, 8), ("myc_a", 14, 1, 14), ("myc_b", 10, 3, 10), ("myc_c", 6.5, 5, 6.5),
                ("shroom", 2, 2, 2), ("shroom_stalk", 1, 2, 1)]
    if k == "tree":
        return [("branch", 1.25, 6, 1.25), ("twig", .75, 3, .75), ("leafball", 4, 3.5, 4)]
    if k == "cactus":
        return [("column", 5.5, 13, 5.5), ("carm", 2.5, 5, 2.5), ("carm_up", 2.5, 4, 2.5), ("bloom", 2.5, 1.5, 2.5),
                ("sombrero_brim", 17, .75, 17), ("sombrero_crown", 6, 4, 6), ("sombrero_top", 4.5, 2, 4.5), ("bandana", 5.5, 1.75, 4.25),
                ("poncho", 7.5, 5.5, 5.25), ("braid", 1.5, 6, 1.5), ("braid_bow", 3, 1.5, .5), ("lasso", 4, 4, .25), ("mustache", 7, 2, .25),
                ("bandolier", 1.5, 12, .5), ("sleeve_puff", 2.25, 2, 2.25)]
    if k == "root":
        return [("rbulb", 5, 6, 5), ("rtend_a", 1, 4, 1), ("rtend_b", .75, 3, .75), ("leafy", 1.5, 6, .25), ("tuft", 4, 1, 4)]
    if k == "leshy":
        return [("beard", 1.5, 7, .75), ("beard_s", 1.5, 5, .75), ("moss", 5.5, 1.5, 3.5)]
    if k == "dryad":
        return [("dleaf", 1.75, .25, 2.5), ("dleaf_s", 1.25, .25, 1.75)]
    if k == "puff":
        return [("seed_stalk", .5, 5, .5), ("puff_ball", 4.5, 3, 4.5), ("puff_ray", .5, .5, 8), ("puff_tip", 1.5, 1.5, 1.5)]
    return []


def paint_hook(pt, pal, d):
    e = pal.extra
    petal, inner = G.shades(e.get("petal", "#f08ab0")), G.shades(e.get("petal_in", "#f8c0d8"))
    leaf = G.shades(e.get("leaf", "#5aa84a"))
    pt("petal", "all", lambda f, x, y, w, h: pick(petal, 0.45 + 0.35 * (y / max(1, h - 1)) + 0.1 * (x == w // 2), x, y))
    pt("petal_in", "all", lambda f, x, y, w, h: pick(inner, 0.5 + 0.3 * (1 - y / max(1, h - 1)), x, y))
    pt("bulb", "all", lambda f, x, y, w, h: pick(leaf, 0.45 + 0.2 * ((x + y) % 3 == 0), x, y))
    pt("leaf", "all", lambda f, x, y, w, h: pick(leaf, 0.5 + 0.2 * (x == w // 2), x, y))
    pt("stem", "all", lambda f, x, y, w, h: pick(leaf, 0.4 + 0.15 * (y % 3 == 0), x, y))
    trap = G.shades(e.get("trap", "#d8303a"))
    pt("lobe", "all", lambda f, x, y, w, h: pick(leaf, 0.5 + 0.12 * (y % 3 == 0), x, y) if f in ("up", "down")
       else pick(trap if abs(x + 0.5 - w / 2) < w * 0.36 and y > 0 else leaf, 0.45 + 0.25 * (y / max(1, h - 1)) + (noise(x, y, 43) - 0.5) * 0.1, x, y))
    pt("cilia", "all", lambda f, x, y, w, h: pick(G.shades(e.get("cilia", "#e8f0a0")), 0.5 + 0.2 * (y == 0), x, y))
    puff, seed = G.shades(e.get("puff", "#fbfaf6")), G.shades(e.get("seed", "#8a6a3a"))
    pt("seed_stalk", "all", lambda f, x, y, w, h: pick(seed, 0.5 + 0.1 * (y % 2), x, y))
    pt("puff_ball", "all", lambda f, x, y, w, h: pick(puff, 0.6 + 0.25 * (noise(x, y, 87) > 0.5), x, y))
    pt("puff_ray", "all", lambda f, x, y, w, h: pick(puff, 0.7 + 0.15 * (x % 2), x, y))
    pt("puff_tip", "all", lambda f, x, y, w, h: G.with_alpha(pick(puff, 0.75 + 0.2 * ((x + y) % 2), x, y), 230))
    cap, spot = G.shades(e.get("cap", "#c8302a")), G.shades(e.get("spot", "#f8f4ec"))
    spotted = lambda f, x, y, w, h: pick(spot, 0.7, x, y) if (x * 7 + y * 11) % 13 < 2 and f != "down" else pick(cap if f != "down" else spot, 0.5, x, y)
    pt("cap", "all", spotted)
    pt("cap_top", "all", spotted)
    pt("shroom", "all", spotted)
    pt("shroom_stalk", "all", lambda f, x, y, w, h: pick(G.shades(e.get("stalk", "#ece0cc")), 0.6, x, y))
    myc = G.shades(e.get("mycelium", "#8a7a8a"))
    for p in ("myc_a", "myc_b", "myc_c"):
        pt(p, "all", lambda f, x, y, w, h: pick(myc, 0.4 + 0.3 * (noise(x // 2, y // 2, 6) > 0.6) + (noise(x, y, 7) - 0.5) * 0.1, x, y))
    bark = G.shades(e.get("bark", "#6a4a2e"))
    pt("branch", "all", lambda f, x, y, w, h: pick(bark, 0.45 + 0.15 * (x % 2), x, y))
    pt("twig", "all", lambda f, x, y, w, h: pick(bark, 0.5, x, y))
    pt("leafball", "all", lambda f, x, y, w, h: pick(leaf, 0.4 + 0.35 * (noise(x, y, 3) > 0.5), x, y))
    cac = G.shades(e.get("cactus", "#4a9a3a"))
    spines = lambda f, x, y, w, h: pick(G.shades("#f8f0d8"), 0.7, x, y) if (x + 2 * y) % 5 == 0 else pick(cac, 0.45 + 0.2 * (x % 3 == 0), x, y)
    for p in ("column", "carm", "carm_up"):
        pt(p, "all", spines)
    pt("bloom", "all", lambda f, x, y, w, h: pick(G.shades(e.get("bloom", "#f07aa8")), 0.6 + 0.2 * ((x + y) % 2), x, y))
    root = G.shades(e.get("root", "#d8b890"))
    for p in ("rbulb", "rtend_a", "rtend_b"):
        pt(p, "all", lambda f, x, y, w, h: pick(root, 0.45 + 0.2 * (y % 3 == 0) + (noise(x, y, 4) - 0.5) * 0.1, x, y))
    pt("leafy", "all", lambda f, x, y, w, h: pick(leaf, 0.5 + 0.25 * (x == w // 2), x, y))
    for p in ("dleaf", "dleaf_s"):
        pt(p, "all", lambda f, x, y, w, h: pick(leaf, 0.45 + 0.25 * (x == w // 2) + 0.1 * (y % 2), x, y))
    pt("tuft", "all", lambda f, x, y, w, h: pick(leaf, 0.45, x, y))
    moss = G.shades(e.get("moss", "#4a7a3a"))                         # the leshy's beard and shoulders of moss
    for p in ("beard", "beard_s", "moss"):
        pt(p, "all", lambda f, x, y, w, h: pick(moss, 0.4 + 0.3 * ((x + y // 2) % 3 == 0) + (noise(x, y, 8) - 0.5) * 0.15, x, y))
    if d["plant"] == "cactus":                                 # the homage variants (Cactus Girl, Cactus Gunman): drawn only on them
        girl, gun = e.get("blossom"), e.get("gunman")
        any_ = girl or gun
        show = lambda on, fn: (lambda f, x, y, w, h: fn(f, x, y, w, h) if on else CLEAR)
        hat, band = G.shades(e.get("hat", "#d8782a")), G.shades(e.get("hat_band", "#6a3a18"))

        def weave(f, x, y, w, h, band_rows=0):                  # straw, in rows of little scallops
            if band_rows and y >= h - band_rows and f not in ("up", "down"):
                return pick(band, 0.45, x, y)
            arc = (y % 3 == 1 and (x + (y // 3) * 2) % 5 in (1, 2, 3)) or (y % 3 == 2 and (x + (y // 3) * 2) % 5 in (0, 4))
            return pick(hat, 0.5 + 0.22 * arc - 0.1 * (f == "down"), x, y)
        pt("sombrero_brim", "all", show(any_, weave))
        pt("sombrero_crown", "all", show(any_, lambda f, x, y, w, h: weave(f, x, y, w, h, band_rows=3)))
        pt("sombrero_top", "all", show(any_, weave))
        red, gold = G.shades(e.get("bandana", "#f02a10")), G.shades("#f8c830")
        pt("bandana", "all", show(any_, lambda f, x, y, w, h: pick(gold if girl and y == h - 1 else red, 0.5 + 0.15 * ((x + y) % 5 == 0), x, y)))
        pon, pband = G.shades(e.get("poncho", "#1e4e98")), G.shades(e.get("poncho_band", "#f0a030"))
        ink, cream = G.shades("#1e1a1a"), G.shades("#f4ecdc")

        def poncho(f, x, y, w, h):
            if f in ("up", "down"):
                return CLEAR if f == "down" else pick(pon if gun else cream, 0.5, x, y)
            if y >= h - 2:                                      # the fringe
                return pick(pband, 0.55, x, y) if x % 2 == 0 else CLEAR
            if girl:
                if y < h * 0.55:                                # a cow-print top
                    return pick(ink, 0.3, x, y) if H.noise(x // 3, y // 3, 41) > 0.68 else pick(cream, 0.6, x, y)
                zig = abs((x % 6) - 3) == (y - int(h * 0.55)) % 4    # a zigzag band with diamonds under it
                dia = (x + 3) % 6 == 0 and y == h - 4
                return pick(pband if zig else ink if dia else pon, 0.5 + 0.1 * zig, x, y)
            zig = abs((x % 6) - 3) == (y // 2) % 4                   # the gunman's: cream and maroon zigzags, bullet holes
            if (x * 7 + y * 5) % 37 == 0:
                return pick(ink, 0.2, x, y)
            return pick(pband if zig else pon, 0.45 + 0.15 * (y % 4 == 0), x, y)
        pt("poncho", "all", show(any_, poncho))
        hair_c = G.shades(e.get("braid", "#14603a"))
        pt("braid", "all", show(girl, lambda f, x, y, w, h: pick(pal.HAIR, 0.45 + 0.25 * ((y // 3) % 2 == 0), x, y)))
        pt("braid_bow", "all", show(girl, lambda f, x, y, w, h: pick(red, 0.55 + 0.2 * (x in (0, w - 1)), x, y)))
        rope = G.shades("#c08a3a")
        pt("lasso", "all", show(girl, lambda f, x, y, w, h: CLEAR if (2 <= x < w - 2 and 2 <= y < h - 2) else pick(rope, 0.5 + 0.2 * ((x + y) % 2), x, y)))
        pt("mustache", "all", show(gun, lambda f, x, y, w, h: CLEAR if (f == "north" and (y < h * 0.4 and abs(x + 0.5 - w / 2) > w * 0.12
                                                                                           or y >= h * 0.7 and abs(x + 0.5 - w / 2) < w * 0.3))
                                     else pick(ink, 0.25, x, y)))
        leather, brass = G.shades("#6a3a24"), G.shades("#b88a3a")
        pt("bandolier", "all", show(gun, lambda f, x, y, w, h: pick(brass if f == "north" and y % 3 != 0 and 1 <= x < w - 1 else leather, 0.5, x, y)))
        pt("sleeve_puff", "all", show(girl, lambda f, x, y, w, h: pick(pon, 0.55 + 0.2 * (y == h - 1), x, y)))
        ribs = G.shades(e.get("ribs", "#2e8a2a"))
        if any_:                                                # cactus ribs down her skin
            for p in ("head", "arm_up", "forearm", "column", "carm", "carm_up"):
                pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(ribs, 0.4, x, y) if x % 5 == 2 else None)
            if girl:                                            # freckles
                pt("head", ("north",), lambda f, x, y, w, h: pick(ribs, 0.25, x, y) if (x, y) in {(13, 17), (15, 16), (17, 17), (14, 19), (16, 19)} else None)
    if d["plant"] in ("cactus",):                              # spines on her skin too
        pt("arm_up", ("north", "east", "west"), lambda f, x, y, w, h: pick(G.shades("#f8f0d8"), 0.7, x, y) if (x + 3 * y) % 7 == 0 else None)


def build_hook(r, g, d):
    import math
    k = d["plant"]
    hy, z = g.hy, g.z
    if k == "dryad":                                           # leaves sprouting at the tips and forks of her antler branches
        cs = []
        for sg in (-1, 1):
            for x, y, zz, p, a in ((4.6, 12.8, 1.0, "dleaf", 20), (3.8, 11.2, -0.2, "dleaf_s", -30), (5.4, 11.0, 2.0, "dleaf_s", 60)):
                w = G.S(p)[0]
                cs.append(G.rot_cube(p, [sg * x - w / 2, hy + y, zz - 1.0 + z], [sg * x, hy + y, zz + z], -25, a * sg, 0))
        r.add("hair_leaves", "head", [0, hy + 8.0, z], cs)
    if k == "puff":                                            # the seed stalk rising from her head into the puff, rays all round
        top = hy + 13.0
        cs = [cube("seed_stalk", [-0.25, hy + 8.0, -0.25 + z]), cube("puff_ball", [-2.25, top - 1.0, -2.25 + z])]
        for ring, tilt in ((0, -35), (1, -62), (2, -85)):       # a fan of fluff opening upward, like a dandelion seed's
            for i in range(10 - 3 * ring):
                a = i * 360 / (10 - 3 * ring) + ring * 18
                cs.append(G.rot_cube("puff_ray", [-0.25, top, z], [0, top + 0.25, z], tilt, a, 0))
                tip = H.rot_about((0, top + 0.25, z + 8), (0, top + 0.25, z), H.rot_matrix(tilt, a, 0))
                cs.append(cube("puff_tip", [tip[0] - 0.75, tip[1] - 0.75, tip[2] - 0.75]))
        r.add("puff", "head", [0, hy + 8.0, z], cs)
    if k == "trap":                                            # a rosette of leaves; a trap lobe each side, hinged low, open wide
        cs = [cube("bulb", [-3.5, 7.0, -3.5]), cube("stem", [-1.25, 0, -1.25])]
        for i in range(6):
            cs.append(G.rot_cube("leaf", [-1.25, 0.1, 5.0], [0, 0.2, 0], -4, i * 60 + 15, 0))
        r.add("trap", "root", [0, 0, 0], cs)
        teeth = [cube("cilia", [-4.6, 15.9, -3.6 + 1.0 * i]) for i in range(8)]
        r.add("jaw_r", "trap", [-3.5, 8.0, 0], [cube("lobe", [-4.6, 7.0, -3.75])] + teeth, mirror=True)
    if k == "flower":                                          # her waist rises from the flower's heart
        cs = [cube("bulb", [-3.5, 7.0, -3.5]), cube("stem", [-1.25, 0, -1.25])]
        for i in range(8):                                    # the great outer petals droop from under the bulb
            a = i * 45 + 22.5
            cs.append(G.rot_cube("petal", [-1.75, 6.6, 2.5], [0, 7.0, 0], 28, a, 0))
        for i in range(6):
            a = i * 60
            cs.append(G.rot_cube("petal_in", [-1.75, 7.0, 2.6], [0, 7.0, 0], -25, a, 0))
        for i in range(4):
            cs.append(G.rot_cube("leaf", [-1.25, 0.1, 5.5], [0, 0.2, 0], -4, i * 90 + 22, 0))
        r.add("flower", "root", [0, 0, 0], cs)
    if k == "mushroom":
        r.add("cap_hat", "head", [0, hy + 8, z], [cube("cap", [-6.5, hy + 7.5, -6.5 + z]), cube("cap_top", [-4, hy + 11, -4 + z])])
        r.add("mycelium", "root", [0, 0, 0], [cube("myc_a", [-7, 0, -7]), cube("myc_b", [-5, 1, -5]), cube("myc_c", [-3.25, 4, -3.25])])
        dis = []
        for x, zz, s_ in ((-1.5, -1, 1.0), (1.5, 0.5, 0.8), (0, 2, 0.9)):
            dis += [cube("shroom_stalk", [x - 0.5, 0, zz - 0.5]), cube("shroom", [x - 1, 2, zz - 1])]
        r.add("disguise", "root", [0, 0, 0], dis)
    if k == "tree":
        for sg in (-1, 1):
            r.put("body", G.rot_cube("branch", [sg * 3.2 - 0.625, 19.5, -0.625 + z], [sg * 3.2, 19.5, z], -10, 0, -35 * sg),
                  G.rot_cube("twig", [sg * 6.0 - 0.375, 23.0, -0.375 + z], [sg * 6.0, 23.0, z], 0, 0, 20 * sg),
                  cube("leafball", [sg * 6.4 - 2, 24.5, -2 + z]))
    if k == "cactus":
        r.add("cactus", "root", [0, 0, 0], [cube("column", [-2.75, 0, -2.75]),
                                           cube("carm", [-5.25, 5, -1.25]), cube("carm_up", [-5.25, 7.5, -1.25]),
                                           cube("carm", [2.75, 3.5, -1.25]), cube("carm_up", [2.75, 6.0, -1.25])])
        r.put("head", cube("bloom", [2.0, hy + 7.6, -1.0 + z]))
        r.add("sombrero", "head", [0, hy + 7, z], [cube("sombrero_brim", [-8.5, hy + 6.8, -8.5 + z]), cube("sombrero_crown", [-3, hy + 7.5, -3 + z]),
                                                   cube("sombrero_top", [-2.25, hy + 11.5, -2.25 + z])])
        r.add("braids", "head", [0, hy + 2, z], [cube("braid", [-6.4, hy - 4.0, -1.75 + z]), cube("braid", [4.9, hy - 4.0, -1.75 + z]),
                                                 cube("braid_bow", [-7.15, hy - 3.2, -2.0 + z]), cube("braid_bow", [4.15, hy - 3.2, -2.0 + z])])
        r.put("head", cube("mustache", [-3.5, hy + 0.2, -4.35 + z]))
        r.add("poncho", "body", [0, 16, z], [cube("poncho", [-3.75, 13.4, -2.625 + z]), cube("bandana", [-2.75, 18.6, -2.125 + z]),
                                             G.rot_cube("bandolier", [-0.75, 10.6, -2.95 + z], [0, 16.6, -2.7 + z], 0, 0, 48),
                                             cube("lasso", [-4.6, 11.0, -2.9 + z])])
        r.put("arm_r", cube("sleeve_puff", [r.b["arm_r"]["pivot"][0] - 1.125, r.b["arm_r"]["pivot"][1] - 2.2, -1.125 + z]))
    if k == "leshy":                                           # a long beard of moss, moss on his shoulders
        r.add("beard", "head", [0, hy + 0.5, z - 4], [G.hang("beard", -0.75, hy + 0.6, z - 4.6, 0, -6), G.hang("beard_s", -2.25, hy + 0.8, z - 4.4, 6, -5),
                                                     G.hang("beard_s", 0.75, hy + 0.8, z - 4.4, -6, -5)])
        r.put("body", cube("moss", [-2.75, 19.0, -1.75 + z]))
    if k == "root":
        r.add("rootbody", "root", [0, 0, 0], [cube("rbulb", [-2.5, 6.0, -2.5])])
        for i in range(5):
            a = i * 72 + 15
            P.chain(r, f"rtend{i + 1}", "rootbody", (round(1.6 * math.sin(math.radians(a)), 2), 6.5, round(1.6 * math.cos(math.radians(a)), 2)),
                    ["rtend_a", "rtend_b"], rx=-35, ry=a + 180, bend=-25)
        leaves = [G.rot_cube("leafy", [-0.75, hy + 8.3, -0.1 + z], [0, hy + 8.3, z], -15 + 30 * (i % 2), i * 60, 25) for i in range(6)]
        r.add("sprout", "head", [0, hy + 8.3, z], leaves)
        r.add("disguise", "root", [0, 0, 0], [G.rot_cube("leafy", [-0.75, 0, -0.1], [0, 0, 0], -15 + 30 * (i % 2), i * 60, 25) for i in range(6)]
              + [cube("tuft", [-2, 0, -2])])


def anim_extra(an, sp, d):
    """the matango sinking into her mycelium ("sink"), the mandragora's scream, the alraune's pollen puff ("puff"), the
    ent planting a sapling ("plant"); the disguise bones stay shrunk away unless the game shows them"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    k = d["plant"]
    an.add("scream", 1.2, False, merge({"head": {"rotation": hold(-20, 0, 0)}, "mouth": {"position": hold(*H.OUT_)},
                                        "body": {"position": Tk((0, (0, 0, 0)), (0.1, (0, 0.6, 0)), (0.2, (0, 0, 0)), (0.3, (0, 0.6, 0)), (1.2, (0, 0, 0)))}},
                                       sym("arm", hold(-160, 0, 30))))
    an.add("puff", 1.0, False, merge({"body": {"rotation": Tk((0, (0, 0, 0)), (0.3, (-10, 0, 0)), (0.6, (8, 0, 0)), (1, (0, 0, 0)))},
                                      "mouth": {"position": Tk((0, H.REST), (0.3, H.OUT_), (0.7, H.OUT_), (0.8, H.REST))}},
                                     sym("arm", Tk((0, (0, 0, 0)), (0.4, (-60, 0, 40)), (1, (0, 0, 0))))))
    an.add("plant", 1.5, False, merge({"body": {"rotation": Tk((0, (0, 0, 0)), (0.5, (50, 0, 0)), (1.0, (50, 0, 0)), (1.5, (0, 0, 0)))}},
                                      sym("arm", Tk((0, (0, 0, 0)), (0.5, (-90, 0, 0)), (1.0, (-90, 0, 0)), (1.5, (0, 0, 0))))))
    if k in ("mushroom", "root"):
        for key in [kk.split(".")[-1] for kk in an.A]:
            F._inject(an, key, {"disguise": {"scale": hold(0, 0, 0)}})
    if k == "trap":                                            # the lobes stand open; "snap" swings them shut over whatever is in front
        for key in [kk.split(".")[-1] for kk in an.A]:
            F._inject(an, key, {"jaw_r": {"rotation": hold(0, 30, 35)}, "jaw_l": {"rotation": hold(0, -30, -35)}})
        an.add("snap", 0.6, False, merge({"jaw_r": {"rotation": Tk((0, (0, 30, 35)), (0.08, (0, 60, -20)), (0.45, (0, 60, -20)), (0.6, (0, 30, 35)))},
                                          "jaw_l": {"rotation": Tk((0, (0, -30, -35)), (0.08, (0, -60, 20)), (0.45, (0, -60, 20)), (0.6, (0, -30, -35)))},
                                          "body": {"rotation": Tk((0, (0, 0, 0)), (0.08, (16, 0, 0)), (0.6, (0, 0, 0)))}},
                                         sym("arm", Tk((0, (0, 0, 0)), (0.08, (-70, 0, 0)), (0.6, (0, 0, 0))))))
    if k == "trap":                                            # asleep: sunk into her rosette, upright, the lobes closed over her like a bud
        an.A.pop(f"animation.{sp}.lie", None)
        an.add("lie", 3.0, True, merge({"body": {"position": hold(0, -4.5, 0), "rotation": hold(8, 0, 0)}, "head": {"rotation": hold(22, 0, 6)},
                                        "eyelids": {"position": hold(*H.OUT_)},
                                        "jaw_r": {"rotation": hold(0, 60, -22)}, "jaw_l": {"rotation": hold(0, -60, 22)}},
                                       sym("arm", hold(-30, 0, -20)), sym("forearm", hold(-60, 0, 0))))
    if k == "puff":                                            # she never walks: she drifts, legs dangling, the puff turning
        drift = merge({"body": {"rotation": Tk((0, (4, 0, 3)), (1.5, (4, 0, -3)), (3.0, (4, 0, 3)))},
                       "puff": {"rotation": Tk((0, (0, 0, 0)), (3.0, (0, 40, 0)))},
                       "leg_r": {"rotation": Tk((0, (10, 0, 0)), (1.5, (-6, 0, 0)), (3.0, (10, 0, 0)))},
                       "leg_l": {"rotation": Tk((0, (-6, 0, 0)), (1.5, (10, 0, 0)), (3.0, (-6, 0, 0)))},
                       "shin_r": {"rotation": hold(18, 0, 0)}, "shin_l": {"rotation": hold(14, 0, 0)}}, G.upper_idle())
        for key in ("walk", "run"):
            an.A.pop(f"animation.{sp}.{key}", None)
            an.add(key, 3.0, True, drift)
        an.add("drift", 3.0, True, drift)
    if k in ("flower", "mushroom", "cactus", "root", "trap"):  # no legs: she sways instead of walking
        sway = {"body": {"rotation": Tk((0, (0, 0, 3)), (1.0, (0, 0, -3)), (2.0, (0, 0, 3)))}}
        for key in ("walk", "run"):
            an.A.pop(f"animation.{sp}.{key}", None)
            an.add(key, 2.0, True, merge(G.upper_idle(), sway, {"jaw_r": {"rotation": hold(0, 30, 35)}, "jaw_l": {"rotation": hold(0, -30, -35)}} if k == "trap" else {}))


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.5, -30, 8), ("scream", 0.5, -40, 8), ("sit", 0, -30, 10), ("idle", 0, -60, 40)]

if __name__ == "__main__":
    F.run_family("flora", SPECIES, declare_hook, paint_hook, build_hook, anim_extra, views=VIEWS)
