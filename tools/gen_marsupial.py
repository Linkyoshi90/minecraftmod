#!/usr/bin/env python3
"""Marsupials (the pouch family, see marsupial_field_guide.html): kangaroo.

    python tools/gen_marsupial.py [species ...] [--previews]

girl_family bipeds. The kangaroo has long feet, tall ears, a thick tail that rests on the ground and a pouch on her
belly (`pouch`; her joey rides there). Every kangaroo wears a cropped sweater; the outback variant's is red and she
wears sunglasses (`shades`, drawn only on her). Extra animation: "kick" (she leans back on her tail and kicks with both
feet).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

SPECIES = {
    "kangaroo_marsupial": dict(
        build="athletic", outfit=dict(top=["sweater"], bottom="shorts", colors=dict(cloth2="#4a4a52")),
        hair="ponytail", bangs="swept", ears="big", legs="fur", foot="long",
        tail=[(3, 3, 4), (2.5, 2.5, 4), (2, 2, 4), (1.5, 1.5, 3.5)], tail_rest=[(-25, 0, 0), (20, 0, 0), (15, 0, 0), (10, 0, 0)],
        pal=dict(SKIN=skin("tan"), HAIR="#a8582a", FUR="#b86a3a", LIGHT="#ecd0b0", MARK="#6a3418", EYE="#2a1a10", CLOTH="#c8b8a0", TRIM="#4a4a52",
                 pattern="solid"),
        variants={"grey": dict(FUR="#8a8078", HAIR="#6a625a", LIGHT="#e0d8d0", MARK="#4a4440", CLOTH="#5a7a9a"),
                  "wallaroo": dict(FUR="#4a3e36", HAIR="#2e2620", LIGHT="#8a7a6a", MARK="#1e1814", CLOTH="#8a6a3a"),
                  "albino": dict(FUR="#f4f0e8", HAIR="#fbf8f2", LIGHT="#ffffff", MARK="#d8d0c4", EYE="#d84a5a", CLOTH="#7aa8c8"),
                  "outback": dict(CLOTH="#c8282a", TRIM="#2a2a30", extra=dict(shades=True))}),
    # eleventh wave: the gliderune, a sugar glider: big eyes and ears, a gliding membrane from wrist to hip, a long fluffy tail
    "gliderune_marsupial": dict(
        build="petite", outfit=dict(top=["halter"], bottom="shorts", extras=["scarf"], colors=dict(cloth2="#2a3a5a", scarf="#e8c060")),
        hair="bob", bangs="wispy", ears="big", legs="fur", foot="paw", tail=[(1.75, 1.75, 4), (2, 2, 4), (1.75, 1.75, 3.5)],
        tail_rest=[(-35, 0, 0), (15, 0, 0), (12, 0, 0)], tail_look="fur", glider=True,
        pal=dict(SKIN=skin("fair"), HAIR="#8a8a96", FUR="#9a9aa8", LIGHT="#f4f2ee", MARK="#2a2a32", EYE="#1a1a24", CLOTH="#3a6ab0", TRIM="#e8c060",
                 pattern="solid"),
        variants={"blue": dict(FUR="#7a8aa8", HAIR="#6a7a98", MARK="#1e2a42", CLOTH="#c8a050"),
                  "leucistic": dict(FUR="#f4f2ee", HAIR="#fbf8f4", LIGHT="#ffffff", MARK="#c8c4be", EYE="#2a2a34", CLOTH="#a87ac8"),
                  "mosaic": dict(FUR="#f0eeea", HAIR="#9a9aa8", MARK="#7a7a88", pattern="patches", CLOTH="#3a8a6a"),
                  "cinnamon": dict(FUR="#b8784a", HAIR="#a8683a", MARK="#5a2e14", LIGHT="#f0dcc8", CLOTH="#2a5a7a")}),
    "koala_marsupial": dict(
        build="petite", outfit=dict(top=["sweater"], bottom="shorts", colors=dict(cloth2="#5a5a62")),
        hair="bob", bangs="blunt", ears="big", legs="fur", foot="paw", koala=True,
        pal=dict(SKIN=skin("fair"), HAIR="#8a8a90", FUR="#9a9aa2", LIGHT="#f0f0f2", MARK="#3a3a40", EYE="#2a2a30", CLOTH="#7aa86a", TRIM="#4a4a52",
                 pattern="solid"),
        variants={"brown": dict(FUR="#8a7e72", HAIR="#7a6e62", LIGHT="#e8e0d8"),
                  "pale": dict(FUR="#c8c8cc", HAIR="#b8b8c0", CLOTH="#e8a0b0"),
                  "dark": dict(FUR="#5a5a62", HAIR="#3a3a42", LIGHT="#c8c8cc", CLOTH="#c8a050"),
                  "drop_bear": dict(FUR="#6a5a52", HAIR="#4a3a32", EYE="#d82a2a", CLOTH="#4a3a2a", extra=dict(claws=True))}),
}


def declare_hook(sp, d):
    out = [("pouch", 4.5, 2.5, .5), ("shades", 6.5, 1.75, .25)]
    if d.get("koala"):
        out += [("knose", 1.75, 1.5, 1), ("kclaw", .5, .5, 1.5)]
    if d.get("glider"):                                  # the patagium: a thin sheet from wrist to hip on each side
        out += [("patagium", .25, 7, 3)]
    return out


def paint_hook(pt, pal, d):
    fur = G.fur_fn(pal, seed=31)
    pt("patagium", "all", lambda f, x, y, w, h: pick(pal.LIGHT if y == h - 1 or x == w - 1 else pal.FUR, 0.55 + (noise(x, y, 73) - 0.5) * 0.1, x, y))
    pt("pouch", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.4, x, y) if f == "north" and y == 0 else fur(f, x, y, w, h))   # fur, with the opening along the top
    pt("knose", "all", lambda f, x, y, w, h: pick(G.shades("#2a2a30"), 0.35 + 0.25 * (f == "up" and x < w / 2), x, y))
    pt("kclaw", "all", lambda f, x, y, w, h: pick(G.shades("#f0ece0"), 0.6, x, y) if pal.extra.get("claws") else CLEAR)
    lens = G.shades("#1a1a22")
    pt("shades", "all", lambda f, x, y, w, h: (pick(lens, 0.3 + 0.5 * (x in (1, 5) and y == 0), x, y)
                                                if pal.extra.get("shades") and not (abs(x + 0.5 - w / 2) < w * 0.08 and y >= h / 3) else CLEAR))


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    if d.get("glider"):                                  # hung from the upper arm so it opens when she spreads her arms
        px, py, pz = r.b["arm_r"]["pivot"]
        r.put("arm_r", cube("patagium", [px - 0.875, py - 7.5, pz - 1.0]))
    r.add("pouch", "body", [0, 13.5, -1.8 + z], [cube("pouch", [-2.25, 12.6, -1.75 + z])])
    r.put("head", cube("shades", [-3.25, hy + 2.6, -4.3 + z]))
    if d.get("koala"):                                   # the big leathery nose; the drop bear's claws on her hands
        r.put("head", cube("knose", [-0.875, hy + 2.0, -4.8 + z]))
        px, py, pz = r.b["hand_r"]["pivot"]
        r.put("hand_r", *[cube("kclaw", [px - 0.75 + 0.5 * i, py - 3.0, pz - 1.5]) for i in range(3)])


def anim_extra(an, sp, d):
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    if d.get("glider"):                                  # gliding: arms and legs spread wide, the membrane taut
        an.add("fly", 1.0, True, merge({"root": {"rotation": hold(70, 0, 0)}, "head": {"rotation": hold(-55, 0, 0)},
                                          "leg_r": {"rotation": hold(10, 0, 30)}, "leg_l": {"rotation": hold(10, 0, -30)}},
                                         sym("arm", hold(0, 0, 80))))
    if d.get("koala"):
        koala_anims(an)
    an.add("kick", 0.8, False, merge({"root": {"rotation": Tk((0, (0, 0, 0)), (0.25, (20, 0, 0)), (0.8, (0, 0, 0)))},
                                      "leg_r": {"rotation": Tk((0, (0, 0, 0)), (0.3, (-85, 0, 0)), (0.8, (0, 0, 0)))},
                                      "leg_l": {"rotation": Tk((0, (0, 0, 0)), (0.3, (-85, 0, 0)), (0.8, (0, 0, 0)))},
                                      "tail": {"rotation": Tk((0, (-25, 0, 0)), (0.3, (10, 0, 0)), (0.8, (-25, 0, 0)))}},
                                     sym("arm", Tk((0, (0, 0, 0)), (0.3, (-50, 0, 10)), (0.8, (0, 0, 0))))))


def koala_anims(an):
    """clinging to a tree trunk, asleep"""
    hold, sym, merge = H.hold, H.sym, H.merge
    an.add("cling", 3.0, True, merge({"eyelids": {"position": hold(*H.OUT_)}, "head": {"rotation": hold(10, 0, 0)},
                                      "leg_r": {"rotation": hold(-60, 0, 20)}, "leg_l": {"rotation": hold(-60, 0, -20)},
                                      "shin_r": {"rotation": hold(70, 0, 0)}, "shin_l": {"rotation": hold(70, 0, 0)}},
                                     sym("arm", hold(-80, 0, -25))))


if __name__ == "__main__":
    F.run_family("marsupial", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
