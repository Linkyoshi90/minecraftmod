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
                  "white": dict(SKIN="#c8d8b8", HAIR="#f0f0e8", MARK="#ffffff", extra=dict(cactus="#a8c098", bloom="#f07aa8"))}),
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
    if k == "flower":
        return [("petal", 3.5, .5, 7), ("petal_in", 3.5, 6, .5), ("bulb", 7, 5, 7), ("leaf", 2.5, .25, 6), ("stem", 2.5, 7, 2.5)]
    if k == "mushroom":
        return [("cap", 13, 3.5, 13), ("cap_top", 8, 2, 8), ("myc_a", 14, 1, 14), ("myc_b", 10, 3, 10), ("myc_c", 6.5, 5, 6.5),
                ("shroom", 2, 2, 2), ("shroom_stalk", 1, 2, 1)]
    if k == "tree":
        return [("branch", 1.25, 6, 1.25), ("twig", .75, 3, .75), ("leafball", 4, 3.5, 4)]
    if k == "cactus":
        return [("column", 5.5, 13, 5.5), ("carm", 2.5, 5, 2.5), ("carm_up", 2.5, 4, 2.5), ("bloom", 2.5, 1.5, 2.5)]
    if k == "root":
        return [("rbulb", 5, 6, 5), ("rtend_a", 1, 4, 1), ("rtend_b", .75, 3, .75), ("leafy", 1.5, 6, .25), ("tuft", 4, 1, 4)]
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
    pt("tuft", "all", lambda f, x, y, w, h: pick(leaf, 0.45, x, y))
    if d["plant"] in ("cactus",):                              # spines on her skin too
        pt("arm_up", ("north", "east", "west"), lambda f, x, y, w, h: pick(G.shades("#f8f0d8"), 0.7, x, y) if (x + 3 * y) % 7 == 0 else None)


def build_hook(r, g, d):
    import math
    k = d["plant"]
    hy, z = g.hy, g.z
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
    if k in ("flower", "mushroom", "cactus", "root"):          # no legs: she sways instead of walking
        sway = {"body": {"rotation": Tk((0, (0, 0, 3)), (1.0, (0, 0, -3)), (2.0, (0, 0, 3)))}}
        for key in ("walk", "run"):
            an.A.pop(f"animation.{sp}.{key}", None)
            an.add(key, 2.0, True, merge(G.upper_idle(), sway))


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.5, -30, 8), ("scream", 0.5, -40, 8), ("sit", 0, -30, 10), ("idle", 0, -60, 40)]

if __name__ == "__main__":
    F.run_family("flora", SPECIES, declare_hook, paint_hook, build_hook, anim_extra, views=VIEWS)
