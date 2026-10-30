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
    # twenty-third wave (folklore): the vetala, the spirit of Indian tales that hangs upside down from trees in the burning grounds and
    # rides the bodies of the dead: ash-pale, ragged, red-eyed, with tattered wings
    "vetala_bat": dict(
        build="slim", outfit=dict(top=["halter"], bottom="loincloth", extras=["arm_wraps", "pearls"], colors=dict(cloth2="#4a3a3a", bandage="#a89a8a")),
        hair="flowing", bangs="messy", ears="elf", legs="bare", foot="claw", wings="bat", wing_scale=0.95, flies=True,
        pal=dict(SKIN=skin("#c8ccd0"), HAIR="#e8e8ec", FUR="#a8acb4", LIGHT="#e8eaee", MARK="#4a4e56", EYE="#d82a3a", CLOTH="#6a5a5a", TRIM="#a89a8a",
                 INNER="#a8808a", pattern="solid", extra=dict(wing="#5a5e66", wing_bone="#2a2e34", boot="#2a2e34")),
        variants={"ash": dict(SKIN=skin("#9a9a9e"), HAIR="#3a3a40", FUR="#5a5a62", MARK="#1e1e22", CLOTH="#2a2a2e",
                              extra=dict(wing="#3a3a40", wing_bone="#141418", boot="#141418")),
                  "blue": dict(SKIN=skin("#a8b8d8"), HAIR="#2a3a6a", FUR="#4a5a8a", MARK="#14204a", EYE="#f8d040", CLOTH="#2a3a6a",
                               extra=dict(wing="#2a3a6a", wing_bone="#101a3a", boot="#101a3a")),
                  "green": dict(SKIN=skin("#a8c0a8"), HAIR="#2a4a2e", FUR="#4a6a4e", MARK="#142a16", EYE="#f8e040", CLOTH="#3a4a2a",
                                extra=dict(wing="#2a4a2e", wing_bone="#0e1e10", boot="#0e1e10")),
                  "red": dict(SKIN=skin("#d8b0a8"), HAIR="#5a0e14", FUR="#7a1e24", MARK="#2a0608", EYE="#f8e080", CLOTH="#3a1418",
                              extra=dict(wing="#5a0e14", wing_bone="#2a0608", boot="#2a0608"))}),
    # nineteenth wave: Camazotz, the death bat of the Maya: blue-grey, great pink ears, a leaf nose, jade at her arms
    "camazotz_bat": dict(
        build="curvy", outfit=dict(top=["bandeau"], bottom="loincloth", extras=["armlets", "usekh"], colors=dict(cloth2="#2a8a6a")),
        hair="shaggy", bangs="messy", ears="big", legs="bare", foot="claw", wings="bat", wing_scale=1.05, flies=True, leafnose=True,
        pal=dict(SKIN=skin("#9aaad8"), HAIR="#5a6aa8", FUR="#7a8ac0", LIGHT="#c8d0f0", MARK="#3a4a7a", EYE="#2a2a4a", CLOTH="#2a8a6a", TRIM="#e8c050",
                 INNER="#f0a0a8", pattern="solid", extra=dict(wing="#5a6aa0", wing_bone="#3a4a7a", boot="#3a4a7a", nose="#f0a0a8")),
        variants={"black": dict(SKIN=skin("#5a5a64"), HAIR="#1e1e24", FUR="#2a2a32", LIGHT="#6a6a74", MARK="#0e0e12", EYE="#e83a3a",
                                extra=dict(wing="#2a2a32", wing_bone="#0e0e12", boot="#0e0e12", nose="#c87a80")),
                  "brown": dict(SKIN=skin("#b08a6a"), HAIR="#4a3020", FUR="#6a4a32", LIGHT="#c8a888", MARK="#2a1a10",
                                extra=dict(wing="#4a3424", wing_bone="#2a1a10", boot="#2a1a10", nose="#e8a0a0")),
                  "red": dict(SKIN=skin("#c88a7a"), HAIR="#6a1e1a", FUR="#8a2e24", LIGHT="#e0a090", MARK="#3a0e0a", EYE="#f8d040", CLOTH="#1e1e24",
                              extra=dict(wing="#6a1e1a", wing_bone="#3a0e0a", boot="#3a0e0a", nose="#f0a0a8")),
                  "jade": dict(SKIN=skin("#8ab8a0"), HAIR="#1e5a44", FUR="#3a7a5e", LIGHT="#b8e0c8", MARK="#0e3a2a", CLOTH="#e8c050", TRIM="#2a8a6a",
                               extra=dict(wing="#2a6a54", wing_bone="#0e3a2a", boot="#0e3a2a", nose="#f0b0a8"))}),
    # seventeenth wave: the frondbat, a blue bat with leafy wings and a crown of white flowers, at home in the canopy
    "frond_bat": dict(
        build="slim", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["armlets", "pearls"], colors=dict(cloth2="#3a7a5a")),
        hair="wavy", bangs="parted", ears="big", legs="bare", foot="paw", wings="bat", wing_scale=1.0, flies=True, flowers=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a50b8", FUR="#3a60c8", LIGHT="#c8d8f4", MARK="#14286a", EYE="#3a7ae8", CLOTH="#3a7a5a", TRIM="#e8c060",
                 INNER="#e8c0a0", pattern="solid", extra=dict(wing="#3a8a8a", wing_bone="#1e3a6a", boot="#1e2a5a", bloom="#f8f4ec", bloom_in="#f0c040",
                                                              crown_leaf="#5a9a5a")),
        variants={"green": dict(HAIR="#3a7a3a", FUR="#4a8a4a", LIGHT="#c8e8c0", MARK="#1e3a1e",
                                extra=dict(wing="#5a9a3a", wing_bone="#2a4a1e", boot="#1e3a1e", bloom="#f8a8c8", bloom_in="#f0e070", crown_leaf="#3a6a2a")),
                  "brown": dict(HAIR="#5a3a22", FUR="#6a4a2e", LIGHT="#c8a880", MARK="#2e1e12",
                                extra=dict(wing="#6a7a3a", wing_bone="#2a1a10", boot="#2a1a10", bloom="#f8f4ec", bloom_in="#f0c040", crown_leaf="#5a8a3a")),
                  "autumn": dict(HAIR="#a84a1e", FUR="#b8582a", LIGHT="#f0c8a0", MARK="#4a1e0e",
                                 extra=dict(wing="#d8782a", wing_bone="#5a2a10", boot="#4a1e0e", bloom="#f8e070", bloom_in="#c83a1e", crown_leaf="#c86a2a")),
                  "black": dict(HAIR="#1e1a1e", FUR="#2a2428", LIGHT="#6a6468", MARK="#0e0c0e", EYE="#e8d040",
                                extra=dict(wing="#2a4a3a", wing_bone="#0e0c0e", boot="#0e0c0e", bloom="#f8f4ec", bloom_in="#f0c040", crown_leaf="#3a6a3a"))}),
    # thirteenth wave: the datebat, a fruit bat of the oasis with date-palm fronds in her hair and a cluster of dates
    "date_bat": dict(
        build="slim", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["belt", "armlets"], colors=dict(cloth2="#c89a3a")),
        hair="wavy", bangs="parted", ears="big", legs="bare", foot="paw", wings="bat", wing_scale=0.95, flies=True, palm=True,
        pal=dict(SKIN=skin("tan"), HAIR="#3a5ab0", FUR="#4a6ac0", LIGHT="#c8d8f4", MARK="#1e2e6a", EYE="#e8b040", CLOTH="#e8c060", TRIM="#2a4a9a",
                 INNER="#e8a0a0", pattern="solid", extra=dict(wing="#3a4a90", wing_bone="#1e2a5a", boot="#1e2a5a", frond="#5a9a3a", date="#7a3a1a")),
        variants={"sand": dict(HAIR="#a88a5a", FUR="#c8a870", LIGHT="#f0e0c0", MARK="#6a4a2a", CLOTH="#3a6a8a",
                               extra=dict(wing="#8a6a44", wing_bone="#5a4028", boot="#5a4028", frond="#5a9a3a", date="#7a3a1a")),
                  "brown": dict(HAIR="#5a3a22", FUR="#6a4a2e", LIGHT="#c8a880", MARK="#2e1e12", CLOTH="#c84a3a",
                                extra=dict(wing="#4a3424", wing_bone="#2a1a10", boot="#2a1a10", frond="#5a9a3a", date="#a85a2a")),
                  "black": dict(HAIR="#1e1a1e", FUR="#2a2428", LIGHT="#6a6468", MARK="#0e0c0e", EYE="#e8d040",
                                extra=dict(wing="#2a2428", wing_bone="#0e0c0e", boot="#0e0c0e", frond="#3a7a2a", date="#5a2a10")),
                  "gold": dict(HAIR="#d8a840", FUR="#e0b450", LIGHT="#f8ecc0", MARK="#7a5418", CLOTH="#2a4a8a",
                               extra=dict(wing="#b08a3a", wing_bone="#6a4a14", boot="#6a4a14", frond="#4a8a3a", date="#7a3a1a"))}),
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
                                extra=dict(wing="#5a4438", wing_bone="#2a1e18", stocking="#4a3a30", boot="#2a1e18", frost=True)),
                  # homage (2026-10-28): Rouge the Bat (Sonic): white hair, black bodysuit with a pink heart, white gloves and boots
                  "jewel_thief": dict(SKIN=skin("tan"), HAIR="#f4f4f6", FUR="#f0f0f2", LIGHT="#ffffff", MARK="#9a9aa4", EYE="#2a9a8a", CLOTH="#1e1c22",
                                      TRIM="#f080b0", INNER="#f0b8c8", extra=dict(wing="#2a2a32", wing_bone="#141418", stocking="#f4f4f6", boot="#f4f4f6"))}),
}


def declare_hook(sp, d):
    out = [("snout", 3, 2, 2), ("snout_n", 1.25, .75, .5), ("ruff", 5.5, 1.75, 4.25), ("frond", 1.5, .25, 6.5), ("date", 1, 1.25, 1)]
    if d.get("flowers"):                                 # the frondbat's crown: white blooms between leaves
        out += [("bloom", 1.5, .75, 1.5), ("bloom_in", .75, .5, .75), ("crown_leaf", 1, .25, 2.25)]
    if d.get("leafnose"):                                # Camazotz's nose: a pig-like snout and a leaf standing up over it
        out += [("leafnose", 1.75, 1.25, .75), ("noseleaf", 1, 1.5, .25)]
    return out


def paint_hook(pt, pal, d):
    nose = G.shades(pal.extra.get("nose", "#f0a0a8"))
    pt("leafnose", "all", lambda f, x, y, w, h: pick(G.shades("#5a2a30"), 0.35, x, y) if f == "north" and y == 0 and x in (0, w - 1)
       else pick(nose, 0.55 + 0.1 * (f == "up"), x, y))
    pt("noseleaf", "all", lambda f, x, y, w, h: pick(nose, 0.48 + 0.12 * (x == 0), x, y))
    fox = pal.extra.get("fox")
    fur = G.fur_fn(pal, seed=61)
    pt("snout", "all", lambda f, x, y, w, h: (pick(pal.LIGHT, 0.55, x, y) if f in ("down",) or (f == "north" and y >= h - 1) else fur(f, x, y, w, h))
       if fox else CLEAR)
    pt("snout_n", "all", lambda f, x, y, w, h: pick(G.shades("#1e1412"), 0.4, x, y) if fox else CLEAR)
    gold = G.shades("#e8b050")
    pt("ruff", "all", lambda f, x, y, w, h: pick(gold, 0.5 + 0.25 * ((x + y) % 3 == 0), x, y) if fox else CLEAR)
    frond, date = G.shades(pal.extra.get("frond", "#5a9a3a")), G.shades(pal.extra.get("date", "#7a3a1a"))
    pt("frond", "all", lambda f, x, y, w, h: pick(frond, 0.45 + 0.25 * (x == w // 2) + 0.1 * (y % 2), x, y) if f in ("up", "down") or y == 0
       else pick(frond, 0.4, x, y))
    pt("date", "all", lambda f, x, y, w, h: pick(date, 0.45 + 0.25 * (f == "up") + 0.1 * (x % 2), x, y))
    bloom, bin_, cleaf = G.shades(pal.extra.get("bloom", "#f8f4ec")), G.shades(pal.extra.get("bloom_in", "#f0c040")), G.shades(pal.extra.get("crown_leaf", "#5a9a5a"))
    pt("bloom", "all", lambda f, x, y, w, h: pick(bloom, 0.6 + 0.2 * (f == "up") - 0.15 * (x in (0, w - 1) and y in (0, h - 1)), x, y))
    pt("bloom_in", "all", lambda f, x, y, w, h: pick(bin_, 0.6, x, y))
    pt("crown_leaf", "all", lambda f, x, y, w, h: pick(cleaf, 0.45 + 0.25 * (x == w // 2) + 0.1 * (y % 2), x, y))
    if pal.extra.get("frost"):                           # the hoary bat's frosted tips
        for p in [p for p in H.PARTS if p.startswith(("hair", "strand", "bang")) and p != "hair_cap"]:
            pt(p, "all", lambda f, x, y, w, h: pick(pal.LIGHT, 0.7, x, y) if (x * 5 + y * 3) % 7 == 0 else None)


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    if d.get("leafnose"):
        r.put("head", cube("leafnose", [-0.875, hy + 2.0, -4.6 + z]), cube("noseleaf", [-0.5, hy + 3.2, -4.3 + z]))
    if not d.get("palm") and not d.get("leafnose"):
        r.put("head", cube("snout", [-1.5, hy + 0.4, -6.0 + z]), cube("snout_n", [-0.625, hy + 1.65, -6.4 + z]))
        r.put("body", cube("ruff", [-2.75, 18.4, -2.125 + z]))
    if d.get("palm"):                                    # date-palm fronds fanned on her head, a cluster of dates by her ear
        fr = [G.rot_cube("frond", [-0.625, hy + 9.6, z], [0, hy + 9.7, z], 30 + 15 * (i % 2), i * 60 + 15, 0) for i in range(6)]
        fr += [cube("date", [3.6, hy + 5.2, -1.0 + z]), cube("date", [4.2, hy + 4.2, -0.4 + z]), cube("date", [3.4, hy + 4.0, 0.2 + z])]
        r.add("palm", "head", [0, hy + 8.5, z], fr)
    if d.get("flowers"):                                 # round the crown of her head, a bloom over each temple and one in front
        cs = []
        for x, zz, a in ((-3.6, -2.2, 30), (3.6, -2.2, -30), (0.0, -3.8, 0), (-2.2, -3.4, 15), (2.2, -3.4, -15)):
            cs.append(G.rot_cube("crown_leaf", [x - 0.5, hy + 7.9, zz - 1.1 + z], [x, hy + 8.0, zz + z], 0, a, 0))
        for x, zz in ((-3.0, -2.8), (3.0, -2.8), (0.0, -3.9)):
            cs += [cube("bloom", [x - 0.75, hy + 8.0, zz - 0.75 + z]), cube("bloom_in", [x - 0.375, hy + 8.6, zz - 0.375 + z])]
        r.add("hair_crown", "head", [0, hy + 8.0, z], cs)
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
