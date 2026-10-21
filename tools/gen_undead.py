#!/usr/bin/env python3
"""Undead (the restless family, see undead_field_guide.html): living doll, will-o'-wisp, yuki-onna, phantom, vampire,
zombie, Frankenstein's monster.

    python tools/gen_undead.py [species ...] [--previews]

girl_family bipeds, except the will-o'-wisp, who floats on a tail of cold flame (bone `flame`, glowing:
<texture>_glowmask.png). The doll has ball joints painted at knees and elbows, the zombie and the monster stitches
(and the monster neck bolts), the yuki-onna an ice comb, the phantom membrane wings, the vampire small bat wings.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

SPECIES = {
    "living_doll_undead": dict(
        build="petite", outfit=dict(top=["corset", "capelet"], bottom="wrap_skirt", extras=["pearls"], colors=dict(cloth2="#1e1a22")),
        hair="drills", bangs="blunt", ears=None, legs="stockings", foot="shoe", doll=True,
        pal=dict(SKIN="#f8eeea", HAIR="#e8c870", FUR="#f8eeea", LIGHT="#ffffff", MARK="#b8a8a0", EYE="#4a7ac8", CLOTH="#1e1a22", TRIM="#e8e0f0",
                 extra=dict(stocking="#f0eaf0", boot="#1e1a22")),
        variants={"gothic": dict(HAIR="#1e1a22", EYE="#a82a3a", CLOTH="#2a1a2a", TRIM="#a82a3a"),
                  "rose": dict(HAIR="#f0c8a0", CLOTH="#e88aa8", TRIM="#fff0f4", EYE="#6a4a8a", extra=dict(stocking="#ffffff", boot="#e88aa8")),
                  "blue": dict(HAIR="#f4e090", CLOTH="#4a7ad8", TRIM="#ffffff", extra=dict(stocking="#ffffff", boot="#2a3a6a")),
                  "antique": dict(SKIN="#ecd8c0", HAIR="#a8804a", CLOTH="#8a6a4a", TRIM="#e8d8b8", EYE="#5a4a3a")}),
    "will_o_wisp_undead": dict(
        build="slim", outfit=dict(top=["halter"], extras=["armlets"]), lower="none",
        hair="flowing", bangs="wispy", ears=None, wisp=True, glow=["flame_a", "flame_b", "flame_c", "flame_tip", "hair_cap", "strand_l1"],
        pal=dict(SKIN="#c8e8f8", HAIR="#6ac8f8", FUR="#4aa8f0", LIGHT="#e8f8ff", MARK="#2a6ad8", EYE="#e8f8ff", CLOTH="#2a4a8a", TRIM="#a8e8ff",
                 extra=dict(flame="#5ab8f8")),
        variants={"green": dict(SKIN="#c8f0d0", HAIR="#6af0a0", CLOTH="#1e5a3a", TRIM="#a8f8c8", extra=dict(flame="#5af090")),
                  "violet": dict(SKIN="#e0d0f8", HAIR="#b88af8", CLOTH="#4a2a8a", TRIM="#e0c8ff", extra=dict(flame="#b07af8")),
                  "amber": dict(SKIN="#f8e8c8", HAIR="#f8b04a", CLOTH="#8a4a1a", TRIM="#ffe0a0", extra=dict(flame="#f8a83a")),
                  "soul": dict(SKIN="#c8f8f8", HAIR="#4af0f0", CLOTH="#1a4a5a", TRIM="#a8ffff", extra=dict(flame="#3ae8f0"))}),
    "yuki_onna_undead": dict(
        build="slim", outfit=dict(top=["kimono"], bottom="hakama", colors=dict(kimono="#f4f8fc", kimono_trim="#a8c8e8", obi="#a8c8e8", cloth2="#eaf2fa")),
        hair="hime", bangs="blunt", ears=None, legs="bare", foot="shoe", comb=True,
        pal=dict(SKIN="#eef4fa", HAIR="#1a1c24", FUR="#eef4fa", LIGHT="#ffffff", MARK="#a8c8e8", EYE="#8ab8e8", CLOTH="#f4f8fc", TRIM="#a8c8e8",
                 extra=dict(boot="#e8eef4")),
        variants={"frost": dict(HAIR="#a8d0f0", extra=dict(kimono="#dceefa", obi="#6aa0d8")),
                  "lilac": dict(HAIR="#3a2a4a", extra=dict(kimono="#e8dcf4", obi="#9a7ac8", kimono_trim="#9a7ac8")),
                  "silver": dict(HAIR="#d8dce4", EYE="#c8d8f0"),
                  "crimson": dict(HAIR="#1a1416", extra=dict(obi="#c8283a", kimono_trim="#c8283a"), EYE="#c8283a")}),
    "phantom_undead": dict(
        build="slim", outfit=dict(top=["sports"], bottom="shorts", extras=["arm_wraps"], colors=dict(bandage="#4a5468")),
        hair="shaggy", bangs="messy", ears=None, legs="bare", foot="shoe", wings="bat", wing_scale=1.15, flies=True,
        pal=dict(SKIN="#8a9ab8", HAIR="#3a4a6a", FUR="#4a5a7a", LIGHT="#b8c8e0", MARK="#2a3448", EYE="#9af05a", CLOTH="#2a3448", TRIM="#9af05a",
                 extra=dict(wing="#4a5a8a", wing_bone="#2a3448", boot="#2a3448")),
        variants={"grey": dict(SKIN="#a0a4ac", HAIR="#4a4c52", extra=dict(wing="#6a6c74", wing_bone="#3a3c42", boot="#3a3c42")),
                  "violet": dict(SKIN="#a090c0", HAIR="#4a3a6a", extra=dict(wing="#6a4a9a", wing_bone="#3a2a52", boot="#3a2a52")),
                  "teal": dict(SKIN="#80aab0", HAIR="#2a5a5a", extra=dict(wing="#3a7a7a", wing_bone="#1e4040", boot="#1e4040")),
                  "crimson": dict(SKIN="#b09098", HAIR="#5a1a2a", EYE="#f0d040", extra=dict(wing="#8a2a3a", wing_bone="#4a1420", boot="#4a1420"))}),
    "vampire_undead": dict(
        build="curvy", outfit=dict(top=["corset", "capelet"], bottom="wrap_skirt", extras=["pearls"], colors=dict(cloth2="#1a1418")),
        hair="wavy", bangs="parted", ears="elf", legs="stockings", foot="shoe", wings="devil_small", fangs=True,
        pal=dict(SKIN="#f4e8ea", HAIR="#1a1418", FUR="#1a1418", LIGHT="#ffffff", MARK="#8a1a2a", EYE="#c81a2a", CLOTH="#8a1a2a", TRIM="#e8c060",
                 extra=dict(wing="#3a1420", wing_bone="#1a0a10", stocking="#1a1418", boot="#1a1418")),
        variants={"noble": dict(CLOTH="#4a2a6a", TRIM="#e8c060", HAIR="#2a1a2a", extra=dict(wing="#2a1a3a", wing_bone="#140a1a")),
                  "silver": dict(HAIR="#d8dce4", CLOTH="#2a2a3a", EYE="#e83a4a"),
                  "raven": dict(CLOTH="#141216", TRIM="#8a8a90", HAIR="#0e0c10"),
                  "pale": dict(CLOTH="#f0e8ea", TRIM="#c81a2a", HAIR="#f4f0f2", extra=dict(stocking="#ffffff", boot="#c81a2a"))}),
    "zombie_undead": dict(
        build="athletic", outfit=dict(top=["sports"], bottom="shorts", extras=["arm_wraps"], colors=dict(bandage="#c8c0a8")),
        hair="shaggy", bangs="messy", ears=None, legs="bare", foot="shoe", stitches=True,
        pal=dict(SKIN="#a8c0a0", HAIR="#3a4a3a", FUR="#a8c0a0", LIGHT="#d8e8d0", MARK="#4a2a3a", EYE="#e8302a", CLOTH="#3a6a8a", TRIM="#c8c0a8",
                 extra=dict(boot="#3a3028")),
        variants={"husk": dict(SKIN="#c8b088", HAIR="#6a5038", CLOTH="#8a6a4a"),
                  "drowned": dict(SKIN="#7ab0b0", HAIR="#2a5a5a", CLOTH="#4a7a6a", EYE="#5af0f0"),
                  "pale": dict(SKIN="#d0d0d4", HAIR="#2a2a2e", CLOTH="#5a3a5a"),
                  "patchy": dict(SKIN="#b8b098", HAIR="#8a3a2a", CLOTH="#3a5a3a")}),
    "frankenstein_undead": dict(
        build="strong", outfit=dict(top=["sweater"], bottom="wrap_skirt", extras=["belt"], colors=dict(cloth2="#2a2a32")),
        hair="bob", bangs="blunt", ears=None, legs="bare", foot="shoe", stitches=True, bolts=True,
        pal=dict(SKIN="#a8c8a0", HAIR="#1a1c1e", FUR="#a8c8a0", LIGHT="#d0e8c8", MARK="#3a3a3a", EYE="#e8d040", CLOTH="#5a4a6a", TRIM="#8a8a8a",
                 extra=dict(boot="#2a2a2e")),
        variants={"grey": dict(SKIN="#b0b4b0", CLOTH="#4a4a52"),
                  "blue": dict(SKIN="#a8b8d0", CLOTH="#3a3a5a"),
                  "pale": dict(SKIN="#e0dcd0", CLOTH="#6a4a3a"),
                  "patchwork": dict(SKIN="#b8c0a0", CLOTH="#7a5a4a", HAIR="#d8d0c0")}),
    "banshee_undead": dict(
        build="slim", outfit=dict(top=["sweater", "corset", "capelet"], bottom="hakama", colors=dict(cloth2="#5a6a5e")),
        hair="flowing", bangs="wispy", ears=None, legs="bare", foot="shoe", comb=True,
        pal=dict(SKIN="#e8ecee", HAIR="#d8dce4", FUR="#e8ecee", LIGHT="#ffffff", MARK="#8a9098", EYE="#c83a3a", CLOTH="#6a6e78", TRIM="#c8ccd4",
                 extra=dict(boot="#3a3c42", comb="#d8dce4")),
        variants={"green": dict(HAIR="#a8542a", CLOTH="#3a6a4a", extra=dict(boot="#2a3a2a", comb="#d8dce4", cloth2="#2e4a36")),
                  "red": dict(HAIR="#c83a2a", CLOTH="#2a2a32", extra=dict(boot="#1a1a1e", comb="#d8dce4", cloth2="#3a3a46")),
                  "mist": dict(SKIN="#f2f4f6", HAIR="#f4f6f8", CLOTH="#c8ccd4", EYE="#8aa8c8", extra=dict(boot="#a8acb4", comb="#ffffff", cloth2="#dce0e6")),
                  "raven": dict(HAIR="#141218", CLOTH="#1e1c24", extra=dict(boot="#141218", comb="#d8dce4", cloth2="#2a2834"))}),
}


def declare_hook(sp, d):
    out = []
    if d.get("wisp"):
        out += [("flame_a", 6, 4, 4.5), ("flame_b", 4.5, 4, 3.5), ("flame_c", 3, 3.5, 2.5), ("flame_tip", 1.5, 3, 1.5), ("lick", 1, 3.5, 1)]
    if d.get("comb"):
        out += [("comb", 3, 1.5, .5), ("crystal", 1, 1.5, 1)]
    if d.get("bolts"):
        out += [("bolt", 1.25, .75, .75)]
    return out


def paint_hook(pt, pal, d):
    if d.get("wisp"):
        fl = G.shades(pal.extra.get("flame", "#5ab8f8"))

        def flame(f, x, y, w, h):
            t = 0.55 + 0.35 * (y / max(1, h - 1)) + (0.15 if noise(x, y // 2, 11) > 0.6 else 0) - 0.1 * (f in ("east", "west"))
            return G.with_alpha(pick(fl, t, x, y), 210 if y > h * 0.6 else 255)
        for p in ("flame_a", "flame_b", "flame_c", "flame_tip", "lick"):
            pt(p, "all", flame)
    ice = G.shades("#a8d8f8")
    comb = G.shades(pal.extra["comb"]) if "comb" in pal.extra else ice   # the banshee's comb is silver
    pt("comb", "all", lambda f, x, y, w, h: pick(comb, 0.6 + 0.3 * (x % 2), x, y))
    pt("crystal", "all", lambda f, x, y, w, h: pick(ice, 0.85, x, y))
    pt("bolt", "all", lambda f, x, y, w, h: pick(G.shades("#8a8c90"), 0.45 + 0.3 * (x == 0), x, y))
    if d.get("doll"):                                   # porcelain ball joints at the elbows and knees
        joint = G.shades("#d8c8c0")
        for p in ("forearm", "shin"):
            pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(joint, 0.45, x, y) if y == 0 else None)
    if d.get("stitches"):                               # stitched seams across the skin
        def stitch(f, x, y, w, h):
            if f in ("north", "east", "west") and (y == h // 2) and x % 2 == 0:
                return pick(G.shades("#2a2a2a"), 0.3, x, y)
            if f in ("north", "east", "west") and y in (h // 2 - 1, h // 2 + 1) and x % 2 == 0 and x % 4 == 0:
                return pick(G.shades("#2a2a2a"), 0.35, x, y)
            return None
        for p in ("arm_up", "thigh", "midriff"):
            pt(p, ("north", "east", "west"), stitch)
        pt("head", ("north",), lambda f, x, y, w, h: pick(G.shades("#3a3a3a"), 0.3, x, y) if (x == 3 or x == 4) and 6 <= y <= 13 and y % 2 == 0 else None)
    if d.get("fangs"):
        pt("mouth", ("north",), lambda f, x, y, w, h: pal.WHITE if y == 0 and x in (1, w - 2) else None)


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    if d.get("wisp"):
        aw, ah, ad = G.S("flame_a")
        cs = [cube("flame_a", [-aw / 2, 8.0, -ad / 2]), cube("flame_b", [-G.S("flame_b")[0] / 2, 4.25, -G.S("flame_b")[2] / 2 + 0.5]),
              cube("flame_c", [-G.S("flame_c")[0] / 2, 1.0, -G.S("flame_c")[2] / 2 + 1.0]), cube("flame_tip", [-0.75, -1.8, 0.8])]
        for x, zz, rz in ((-2.6, -1.0, 18), (2.6, -0.5, -20), (-1.2, 1.8, 10), (1.8, 1.5, -12)):
            cs.append(G.rot_cube("lick", [x - 0.5, 7.0, zz - 0.5], [x, 10.0, zz], 0, 0, rz))
        r.add("flame", "root", [0, 12, 0], cs)
    if d.get("comb"):
        r.put("head", G.rot_cube("comb", [1.0, hy + 7.4, 1.0 + z], [2.5, hy + 7.4, 1.0 + z], 0, 0, -20), cube("crystal", [3.4, hy + 8.0, 0.75 + z]))
    if d.get("bolts"):
        b = cube("bolt", [-3.4, hy - 0.6, -0.4 + z])
        r.put("body", b, G.mirror_cube(b))


def anim_extra(an, sp, d):
    """the wisp's flicker, the jiang shi-like zombie shamble ("shamble") and the vampire's bite ("drink")"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    if d.get("wisp"):
        flick = {"flame": {"rotation": Tk((0, (0, 0, 3)), (0.5, (4, 0, -3)), (1.0, (0, 0, 3))),
                           "scale": Tk((0, (1.0, 1.0, 1.0)), (0.25, (1.06, 0.94, 1.06)), (0.5, (0.96, 1.08, 0.96)), (1.0, (1.0, 1.0, 1.0)))},
                 "root": {"position": Tk((0, (0, 2.0, 0)), (1.0, (0, 2.6, 0)), (2.0, (0, 2.0, 0)))}}
        for key in ("idle", "walk", "run", "sit", "lie", "groom", "hiss", "stalk", "pounce"):
            F._inject(an, key, flick)
    an.add("drink", 2.0, True, merge({"head": {"rotation": hold(18, 24, 0)}, "body": {"rotation": hold(14, 0, 0)},
                                      "mouth": {"position": hold(*H.OUT_)}, "eyelids": {"position": hold(*H.OUT_)}},
                                     sym("arm", hold(-70, 0, -24)), sym("forearm", hold(-40, 0, 0))))
    an.add("shamble", 1.4, True, merge({"body": {"rotation": Tk((0, (6, 0, 6)), (0.7, (6, 0, -6)), (1.4, (6, 0, 6)))},
                                        "head": {"rotation": hold(10, 0, 14)},
                                        "leg_r": {"rotation": Tk((0, (16, 0, 0)), (0.7, (-16, 0, 0)), (1.4, (16, 0, 0)))},
                                        "leg_l": {"rotation": Tk((0, (-16, 0, 0)), (0.7, (16, 0, 0)), (1.4, (-16, 0, 0)))}},
                                       sym("arm", hold(-85, 0, 0))))


if __name__ == "__main__":
    F.run_family("undead", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
