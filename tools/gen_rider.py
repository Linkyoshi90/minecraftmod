#!/usr/bin/env python3
"""The Four Riders (rider_field_guide.html): War, Famine, Pestilence and Death, a family of their own.

    python tools/gen_rider.py [species ...] [--previews]

girl_family bipeds, each with her own look: War's armour and mane, Famine's great feather cloak and horned hood, Pestilence's thorned
staff and teal flames, Death's hood, cloak, scythe and crow. Pestilence rides a real Minecraft horse (Rider.java keeps her on it): her
"ride" animation is the seated pose. Five colourings per species.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

SPECIES = {
    # War: a wild blonde mane, blue and silver armour over very little else
    "war_rider": dict(
        build="strong", outfit=dict(top=["bandeau"], bottom="loincloth", extras=["pauldron", "bracers", "belt"], colors=dict(leather="#2a4aa8", cloth2="#c8ccd8")),
        hair="mane", bangs="messy", ears=None, legs="boots", foot="shoe",
        pal=dict(SKIN=skin("tan"), HAIR="#e8c850", FUR="#e8c850", LIGHT="#f8ecb0", MARK="#a87a1e", EYE="#2a5ad8", CLOTH="#c8ccd8", TRIM="#2a4aa8", extra=dict(boot="#2a4aa8")),
        variants={"crimson": dict(HAIR="#c82a1e", MARK="#7a140e", CLOTH="#2a2a30", TRIM="#a81e1e", EYE="#e8c040", extra=dict(boot="#a81e1e", leather="#a81e1e")),
                  "black": dict(HAIR="#1e1c22", MARK="#0e0c10", CLOTH="#3a3a42", TRIM="#141418", EYE="#e83a2a", extra=dict(boot="#141418", leather="#141418")),
                  "gold": dict(HAIR="#f4ecd8", MARK="#c8b080", CLOTH="#e8c040", TRIM="#a87a1e", EYE="#3a8a5a", extra=dict(boot="#a87a1e", leather="#a87a1e")),
                  "white": dict(HAIR="#f4f4f8", MARK="#a8a8b0", CLOTH="#f0f0f4", TRIM="#8a8a94", EYE="#5a8ac8", extra=dict(boot="#8a8a94", leather="#8a8a94"))}),
    # Famine: pale, in a huge white feather cloak and a dark horned hood
    "famine_rider": dict(
        build="slim", outfit=dict(top=["bandeau"], bottom="loincloth", colors=dict(cloth2="#e8e8ec", leather="#2a2a30")),
        hair="hime", bangs="blunt", ears=None, legs="bare", foot="shoe", horns="greater", fcloak=True,
        pal=dict(SKIN=skin("pale"), HAIR="#e8e8ec", FUR="#e8e8ec", LIGHT="#ffffff", MARK="#141418", EYE="#6a6a78", CLOTH="#e8e8ec", TRIM="#141418",
                 extra=dict(horn="#141418", boot="#141418", cloak="#f4f4f8", cloak_tip="#141418", hood="#141418")),
        variants={"ash": dict(HAIR="#8a8a92", CLOTH="#8a8a92", extra=dict(horn="#141418", boot="#141418", cloak="#a8a8b0", cloak_tip="#2a2a30", hood="#2a2a30")),
                  "gold": dict(HAIR="#e8d8a0", CLOTH="#c8a040", extra=dict(horn="#5a3e14", boot="#5a3e14", cloak="#e8d090", cloak_tip="#7a5a1e", hood="#5a3e14")),
                  "bone": dict(HAIR="#f0e4c8", CLOTH="#e8dcc0", extra=dict(horn="#6a5a3e", boot="#6a5a3e", cloak="#f0e4c8", cloak_tip="#8a7450", hood="#6a5a3e")),
                  "void": dict(HAIR="#3a3044", CLOTH="#2a2230", EYE="#c84ae8", extra=dict(horn="#0e0a12", boot="#0e0a12", cloak="#2a2230", cloak_tip="#8a4ac8", hood="#0e0a12"))}),
    # Pestilence: small, lilac hair, dark clothes, a thorned staff with teal flame on it, riding a black horse
    "pestilence_rider": dict(
        build="petite", outfit=dict(top=["sports"], bottom="shorts", extras=["belt"], colors=dict(leather="#2a4a4a", cloth2="#1e1a2a")),
        hair="twintails", bangs="parted", ears=None, legs="boots", foot="shoe", pstaff=True, glow=["pflame"], glow_boost=1.2,
        pal=dict(SKIN=skin("pale"), HAIR="#c8a8e8", FUR="#c8a8e8", LIGHT="#ece0f8", MARK="#7a5aa8", EYE="#8a4ac8", CLOTH="#1e1a2a", TRIM="#3ae8c8",
                 extra=dict(boot="#1e1a2a", staff="#141418", flame="#3ae8c8", flame_core="#d8fff4")),
        variants={"green": dict(HAIR="#a8e8a0", MARK="#4a8a4a", EYE="#3aa84a", TRIM="#8af84a", extra=dict(boot="#1e2a1a", staff="#141418", flame="#8af84a", flame_core="#f0ffd0")),
                  "rust": dict(HAIR="#e8a87a", MARK="#a85a2a", EYE="#c84a1e", TRIM="#e8a03a", extra=dict(boot="#2a1a14", staff="#141418", flame="#e8a03a", flame_core="#fff0c0")),
                  "ash": dict(HAIR="#c8c8d0", MARK="#7a7a84", EYE="#5a5a64", TRIM="#a8e8e8", extra=dict(boot="#1e1e24", staff="#141418", flame="#a8e8e8", flame_core="#ffffff")),
                  "rose": dict(HAIR="#f0a8c8", MARK="#a85a7a", EYE="#c82a6a", TRIM="#f85aa8", extra=dict(boot="#2a1a24", staff="#141418", flame="#f85aa8", flame_core="#ffe0f0"))}),
    # Death: hooded and cloaked in brown, a short top and shorts, thigh boots, a great scythe, a crow on her other hand
    "death_rider": dict(
        build="petite", outfit=dict(top=["bandeau", "harness"], bottom="shorts", extras=["belt"], colors=dict(leather="#2a2020", cloth2="#2a2020")),
        hair="bob", bangs="swept", ears=None, legs="boots", foot="shoe", reaper=True,
        pal=dict(SKIN=skin("fair"), HAIR="#c8b8a0", FUR="#c8b8a0", LIGHT="#e8dcc8", MARK="#7a6a54", EYE="#3a5a9a", CLOTH="#2a2020", TRIM="#5a7ac8",
                 extra=dict(boot="#1e1a1a", cloak="#3a2e2a", cloak_in="#1e2a4a", steel="#a8b0c8", crow="#1e1c22")),
        variants={"black": dict(HAIR="#e8e8ec", EYE="#c82a2a", extra=dict(boot="#141418", cloak="#1e1c22", cloak_in="#5a1414", steel="#c8c8d0", crow="#1e1c22")),
                  "grey": dict(HAIR="#8a8a92", EYE="#5ab0e8", extra=dict(boot="#2a2a30", cloak="#5a5a64", cloak_in="#2a2a30", steel="#c8ccd8", crow="#2a2a30")),
                  "white": dict(HAIR="#f4f0e8", EYE="#e8c040", extra=dict(boot="#c8c0b0", cloak="#e8e0d0", cloak_in="#c8a040", steel="#e8c040", crow="#f4f4f4")),
                  "violet": dict(HAIR="#b8a0d8", EYE="#8a4ac8", extra=dict(boot="#1e1428", cloak="#3a2a5a", cloak_in="#141020", steel="#c8b8e8", crow="#1e1428"))}),
}


def declare_hook(sp, d):
    out = []
    if d.get("fcloak"):
        out += [("fc_back", 14, 20, 1.25), ("fc_side", 1.25, 18, 6), ("fc_hood", 10, 6.5, 10)]
    if d.get("pstaff"):
        out += [("pstaff", 1, 20, 1), ("pthorn", 2.5, .5, .5), ("pflame", 2.25, 3.5, 2.25)]
    if d.get("reaper"):
        out += [("dhood", 9.75, 6, 9.75), ("dcloak", 11, 15, .75), ("scy_pole", 1, 20, 1), ("scy_blade", .75, 2.5, 9), ("scy_tip", .75, 1.5, 3),
                ("rcrow", 2, 2, 3), ("rcrow_head", 1.5, 1.5, 1.5), ("rcrow_beak", .5, .5, 1), ("rcrow_tail", 1.5, .25, 2.5)]
    return out


def paint_hook(pt, pal, d):
    if d.get("fcloak"):
        cl, tip, hood = G.shades(pal.extra["cloak"]), G.shades(pal.extra["cloak_tip"]), G.shades(pal.extra["hood"])

        def feathers(f, x, y, w, h):                     # rows of feathers, dark at the hem, ragged at the very bottom
            if y >= h - 2 and (x + noise(x, 0, 31) * 4) % 3 < 1:
                return CLEAR
            return pick(tip if y > h * 0.78 else cl, 0.5 + 0.25 * ((x + y // 5) % 4 == 0) - 0.12 * (y % 5 == 4), x, y)
        for p in ("fc_back", "fc_side"):
            pt(p, "all", feathers)
        pt("fc_hood", "all", lambda f, x, y, w, h: CLEAR if f in ("down", "north") else pick(hood, 0.4 + 0.15 * (x % 4 == 0), x, y))
    if d.get("pstaff"):
        wood, fl, core = G.shades(pal.extra["staff"]), G.shades(pal.extra["flame"]), G.shades(pal.extra["flame_core"])
        pt("pstaff", "all", lambda f, x, y, w, h: pick(wood, 0.35 + 0.2 * (y % 4 == 0), x, y))
        pt("pthorn", "all", lambda f, x, y, w, h: pick(wood, 0.5, x, y))
        pt("pflame", "all", lambda f, x, y, w, h: pick(core if y > h * 0.6 else fl, 0.6 + 0.2 * (x % 2), x, y))
    if d.get("reaper"):
        cl, cin, steel, crow = G.shades(pal.extra["cloak"]), G.shades(pal.extra["cloak_in"]), G.shades(pal.extra["steel"]), G.shades(pal.extra["crow"])
        wood = G.shades("#3a2a1e")
        pt("dhood", "all", lambda f, x, y, w, h: CLEAR if f in ("down", "north") else pick(cl, 0.45 + 0.12 * (x % 3 == 0), x, y))
        pt("dcloak", "all", lambda f, x, y, w, h: pick(cin if f == "north" else cl, 0.45 + 0.12 * (x % 5 == 0), x, y) if y < h - 1 or x % 3 else CLEAR)
        pt("scy_pole", "all", lambda f, x, y, w, h: pick(wood, 0.4 + 0.2 * (y % 3 == 0), x, y))
        for p in ("scy_blade", "scy_tip"):
            pt(p, "all", lambda f, x, y, w, h: pick(steel, 0.55 + 0.3 * (y == 0), x, y))
        for p in ("rcrow", "rcrow_head", "rcrow_tail"):
            pt(p, "all", lambda f, x, y, w, h: pick(crow, 0.35 + 0.2 * (x % 2), x, y))
        pt("rcrow_beak", "all", lambda f, x, y, w, h: pick(G.shades("#2a2a30"), 0.6, x, y))


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    if d.get("fcloak"):                                  # the cloak hangs from her shoulders to the ground, the hood stands up over her head
        r.put("body", cube("fc_back", [-7, -0.5, 2.0 + z]), cube("fc_side", [-7.5, 1.0, -3.5 + z]), cube("fc_side", [6.25, 1.0, -3.5 + z]))
        r.add("hair_fhood", "head", [0, hy + 7, z], [cube("fc_hood", [-5, hy + 3.5, -5 + z])])
    if d.get("pstaff"):                                  # held out from her right fist, tilted, a teal flame at the top
        px, py, pz = r.b["hand_r"]["pivot"]
        grip = [px, py - 1.5, pz]
        held = lambda p, o: G.rot_cube(p, o, grip, 72, 0, 0)
        cs = [held("pstaff", [px - 0.5, py - 9.5, pz - 0.5]), held("pflame", [px - 1.125, py + 10.5, pz - 1.125])]
        cs += [held("pthorn", [px - 1.25, py + 3 + i * 2.2, pz - 0.25]) for i in range(3)]
        r.add("pstaff_b", "hand_r", grip, cs)
    if d.get("reaper"):                                  # hood and cloak; the scythe out from her right fist; a crow on her left
        r.add("hair_dhood", "head", [0, hy + 7, z], [cube("dhood", [-4.875, hy + 3.5, -4.875 + z])])
        r.put("body", cube("dcloak", [-5.5, 4.0, 2.2 + z]))
        px, py, pz = r.b["hand_r"]["pivot"]
        grip = [px, py - 1.5, pz]
        held = lambda p, o: G.rot_cube(p, o, grip, 55, 0, 0)
        r.add("scythe", "hand_r", grip, [held("scy_pole", [px - 0.5, py - 9.5, pz - 0.5]), held("scy_blade", [px - 0.375, py + 8.0, pz - 8.5]),
                                         held("scy_tip", [px - 0.375, py + 7.0, pz - 11.0])])
        lx, ly, lz = -px, py, pz
        r.add("crow", "hand_l", [lx, ly - 2.5, lz], [cube("rcrow", [lx - 1, ly - 2.0, lz - 1.5]), cube("rcrow_head", [lx - 0.75, ly - 0.75, lz - 2.5]),
                                                     cube("rcrow_beak", [lx - 0.25, ly - 0.25, lz - 3.5]), cube("rcrow_tail", [lx - 0.75, ly - 1.5, lz + 1.25])])


def anim_extra(an, sp, d):
    """"ride": seated on her horse (the game plays it while she has a mount)"""
    hold, sym, merge = H.hold, H.sym, H.merge
    an.add("ride", 4.0, True, merge(G.upper_idle(), {"leg_r": {"rotation": hold(-72, 0, 18)}, "leg_l": {"rotation": hold(-72, 0, -18)},
                                                     "shin_r": {"rotation": hold(50, 0, 0)}, "shin_l": {"rotation": hold(50, 0, 0)}}))


if __name__ == "__main__":
    F.run_family("rider", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
