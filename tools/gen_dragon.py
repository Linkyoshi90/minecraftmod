#!/usr/bin/env python3
"""Dragons (the dragon family, see dragon_field_guide.html): long, Jormungandr, wyrm, gryphon, Kannae, wyvern, Yamata no
Orochi.

    python tools/gen_dragon.py [species ...] [--previews]

    long, jormungandr   girl_lib upper body on a serpent tail (seg_1..seg_8, like the lamia); the long dragon has antlers,
                        a mane down her back and a "fly" animation that swims through the air
    gryphon             girl_lib upper body on a four-legged body (G.taur): eagle forelegs, lion hind legs, feathered
                        wings (wing_r / wing_l) and a tufted tail
    wyrm, wyvern        girl_family bipeds with great membrane wings, dragon horns and a heavy tail (the wyvern's ends
                        in a stinger); both fly
    kannae              a small girl_family biped with little horns and a fat tail
    orochi              a girl_family biped in an elaborate kimono with seven dragon heads on long necks rising around
                        her own (bones halo_neck1..7, left out of her height)
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
import girl_parts as P  # noqa: E402
from girl_lib import H, cube, pick, noise, skin  # noqa: E402

DRAGON_TAIL = [(3, 3, 4.5), (2.5, 2.5, 4.5), (2, 2, 4.5), (1.25, 1.25, 4)]
SEGS = [("seg_1", 4.5, 10.5, 4), ("seg_2", 4.25, 4, 5.5), ("seg_3", 4, 3.75, 5.5), ("seg_4", 3.5, 3.25, 5),
        ("seg_5", 3, 2.75, 5), ("seg_6", 2.5, 2.25, 4.5), ("seg_7", 2, 1.75, 4), ("seg_8", 1.25, 1.25, 3.5)]
BIG_SEGS = [(n, round(w * 1.3 * 4) / 4, h if n == "seg_1" else round(h * 1.3 * 4) / 4, round(d * 1.25 * 4) / 4) for n, w, h, d in SEGS]

SPECIES = {
    "long_dragon": dict(
        build="slim", outfit=dict(top=["kimono"], extras=["pearls"], colors=dict(kimono="#2a7a5a", kimono_trim="#e8c040", obi="#e8c040")),
        hair="flowing", bangs="parted", ears="fin", body="serpent", segs=SEGS, horns="antlers", mane=True, flies=True,
        pal=dict(SKIN=skin("fair"), HAIR="#e8f0e0", FUR="#3a9a6a", LIGHT="#e8e0a0", MARK="#1e5a3a", EYE="#e8c040", CLOTH="#2a7a5a", TRIM="#e8c040",
                 extra=dict(horn="#e8d8a0")),
        variants={"azure": dict(FUR="#3a6ac8", MARK="#1a3a7a", HAIR="#e0f0ff", extra=dict(kimono="#2a4a9a", horn="#e8d8a0")),
                  "vermilion": dict(FUR="#c83a2a", MARK="#7a1a14", HAIR="#f8e8c0", extra=dict(kimono="#8a1a1a", horn="#f0d080")),
                  "gold": dict(FUR="#d8a830", MARK="#8a5a14", HAIR="#fff4d8", extra=dict(kimono="#8a5a14", horn="#fff0c0")),
                  "pearl": dict(FUR="#f0ece8", MARK="#c8c0d8", HAIR="#ffffff", extra=dict(kimono="#c8c0d8", horn="#e8e0f0"))}),
    "jormungandr_dragon": dict(
        build="strong", outfit=dict(top=["sarashi"], extras=["armlets", "pearls"], colors=dict(bandage="#4a5a4a")),
        hair="mane", bangs="messy", ears=None, body="serpent", segs=BIG_SEGS, horns="dragon",
        pal=dict(SKIN=skin("olive"), HAIR="#2a3a2a", FUR="#5a7a5a", LIGHT="#c8d0a0", MARK="#2a3a2a", EYE="#e8d83a", CLOTH="#3a4a3a", TRIM="#c8a050",
                 pattern="blotches", pupil=(0.9, 3.4), extra=dict(horn="#3a3a30")),
        variants={"ash": dict(FUR="#7a7a78", MARK="#3a3a38", HAIR="#4a4a48"),
                  "venom": dict(FUR="#5a8a2a", MARK="#e8d83a", HAIR="#2a4a14"),
                  "abyss": dict(FUR="#2a3a5a", MARK="#5aa8f0", HAIR="#141e30"),
                  "gold": dict(FUR="#c8a040", MARK="#6a4a14", HAIR="#7a5a20")}),
    "wyrm_dragon": dict(
        build="strong", outfit=dict(top=["corset", "harness"], bottom="loincloth", extras=["pauldron", "bracers"], colors=dict(cloth2="#3a1a14")),
        hair="ponytail", bangs="jagged", ears=None, legs="scales", foot="claw", tail=DRAGON_TAIL, tail_look="scale",
        tail_rest=[(-30, 0, 0), (8, 0, 0), (8, 0, 0), (6, 0, 0)], horns="greater", wings="dragon", flies=True,
        pal=dict(SKIN=skin("fair"), HAIR="#8a1a1a", FUR="#a82a20", LIGHT="#e8b080", MARK="#4a0e0a", EYE="#f8c02a", CLOTH="#2a1a14", TRIM="#e8b040",
                 pupil=(0.9, 3.4), extra=dict(horn="#2a1a14", wing="#8a2a20", wing_bone="#3a0e0a", claw="#1a1010")),
        variants={"black": dict(FUR="#2a2428", HAIR="#141014", MARK="#8a2a3a", extra=dict(horn="#8a8a90", wing="#2a2028", wing_bone="#0e0c10")),
                  "green": dict(FUR="#3a7a3a", HAIR="#1e4a1e", MARK="#142a14", extra=dict(horn="#e8e0c0", wing="#3a6a3a", wing_bone="#142a14")),
                  "white": dict(FUR="#e8eef4", HAIR="#c8d8e8", MARK="#8aa8c8", EYE="#5aa8f0", extra=dict(horn="#a8b8c8", wing="#c8d8e8", wing_bone="#8aa0b8")),
                  "bronze": dict(FUR="#a87a3a", HAIR="#6a4a1e", MARK="#3a2a10", extra=dict(horn="#3a2a10", wing="#a87a3a", wing_bone="#4a3010"))}),
    "gryphon_dragon": dict(
        build="athletic", outfit=dict(top=["bandeau", "harness"], extras=["bracers"]), hair="mane", bangs="swept", ears="tuft", body="taur",
        wings="feather", flies=True,
        pal=dict(SKIN=skin("tan"), HAIR="#f4f0e8", FUR="#d8a850", LIGHT="#f4e0a8", MARK="#6a4a2a", EYE="#e8a82a", CLOTH="#3a5a8a", TRIM="#e8c050",
                 extra=dict(wing="#7a5a3a", wing_tip="#2a1e14", talon="#e8c040")),
        variants={"white": dict(FUR="#f0e8d8", HAIR="#ffffff", extra=dict(wing="#f4f4f0", wing_tip="#a8a8b0", talon="#e8c040")),
                  "black": dict(FUR="#3a3230", HAIR="#1e1a18", extra=dict(wing="#2a2624", wing_tip="#0e0c0a", talon="#c8a030")),
                  "silver": dict(FUR="#a8a8a8", HAIR="#d8d8d8", extra=dict(wing="#8a8a90", wing_tip="#4a4a50", talon="#e8e0c0")),
                  "red": dict(FUR="#a8582a", HAIR="#e8d8c0", extra=dict(wing="#8a3a1a", wing_tip="#3a140a", talon="#e8c040"))}),
    "kannae_dragon": dict(
        build="petite", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["scarf"], colors=dict(scarf="#e8a8c8", cloth2="#f4f0f8")),
        hair="hime", bangs="blunt", ears=None, legs="stockings", foot="shoe", tail=[(3, 3, 3.5), (2.5, 2.5, 3.5), (1.75, 1.75, 3)], tail_look="scale",
        tail_rest=[(-55, 0, 0), (-10, 0, 0), (-5, 0, 0)], horns="kannae",
        pal=dict(SKIN=skin("pale"), HAIR="#f4f0f8", FUR="#e8e0f0", LIGHT="#ffffff", MARK="#b8a0d8", EYE="#8a6ad8", CLOTH="#f4f0f8", TRIM="#b8a0d8",
                 extra=dict(horn="#e8e0f8", horn_tip="#b8a0d8", stocking="#f4f0f8", boot="#b8a0d8")),
        variants={"pink": dict(HAIR="#f8c8d8", FUR="#f8d8e4", MARK="#e8a0b8", EYE="#d86a98"),
                  "lilac": dict(HAIR="#c8b0e8", FUR="#d8c8f0", MARK="#8a6ab8"),
                  "mint": dict(HAIR="#c8f0e0", FUR="#d8f4ea", MARK="#6ab89a", EYE="#3a9a7a"),
                  "black": dict(HAIR="#2a2430", FUR="#3a3440", MARK="#8a7aa8", EYE="#e83a5a")}),
    "wyvern_dragon": dict(
        build="slim", outfit=dict(top=["sports"], bottom="shorts", extras=["belt"]), hair="shaggy", bangs="messy", ears=None, legs="scales", foot="claw",
        tail=DRAGON_TAIL, tail_look="spade", tail_rest=[(-30, 0, 0), (8, 0, 0), (8, 0, 0), (6, 0, 0)], horns="dragon", wings="dragon", wing_scale=1.15,
        flies=True,
        pal=dict(SKIN=skin("tan"), HAIR="#a87a4a", FUR="#c8a06a", LIGHT="#f0dcb0", MARK="#5a3a1e", EYE="#e8d83a", CLOTH="#5a3a1e", TRIM="#c8a050",
                 pupil=(0.9, 3.4), extra=dict(horn="#5a3a1e", wing="#c8905a", wing_bone="#5a3a1e", tail_col="#c8a06a")),
        variants={"rust": dict(FUR="#a85a2a", HAIR="#7a3a1a", extra=dict(wing="#a8582a", wing_bone="#4a1e0a", tail_col="#a85a2a")),
                  "slate": dict(FUR="#6a7a8a", HAIR="#3a4a5a", extra=dict(wing="#5a6a8a", wing_bone="#2a3444", tail_col="#6a7a8a")),
                  "venom": dict(FUR="#6a9a3a", HAIR="#3a5a1e", MARK="#e8d83a", extra=dict(wing="#5a8a2a", wing_bone="#2a4010", tail_col="#6a9a3a")),
                  "dusk": dict(FUR="#7a5a8a", HAIR="#4a2a5a", extra=dict(wing="#6a4a8a", wing_bone="#2a1a3a", tail_col="#7a5a8a"))}),
    "orochi_dragon": dict(
        build="curvy", outfit=dict(top=["kimono"], bottom="hakama", extras=["pearls"],
                                   colors=dict(kimono="#a81a2a", kimono_trim="#e8c040", obi="#1a1a1a", cloth2="#2a1a1a")),
        hair="hime", bangs="blunt", ears=None, legs="bare", foot="shoe", heads=True, horns="kannae",
        pal=dict(SKIN=skin("pale"), HAIR="#141214", FUR="#3a7a3a", LIGHT="#c8e0a0", MARK="#1a3a1a", EYE="#c81a2a", CLOTH="#a81a2a", TRIM="#e8c040",
                 extra=dict(horn="#e8d8a0", boot="#1a1a1a")),
        variants={"night": dict(extra=dict(kimono="#1a1a2a", obi="#a81a2a", horn="#c8c8d8"), FUR="#2a3a5a"),
                  "sakura": dict(extra=dict(kimono="#f4b8c8", kimono_trim="#ffffff", obi="#a83a5a"), FUR="#5a8a4a"),
                  "gold": dict(extra=dict(kimono="#d8a830", kimono_trim="#1a1a1a", obi="#1a1a1a"), FUR="#8a6a2a"),
                  "storm": dict(extra=dict(kimono="#4a5a6a", kimono_trim="#c8d8e8", obi="#2a3440"), FUR="#4a5a6a", HAIR="#c8d0d8")}),
}

BIPEDS = {k: v for k, v in SPECIES.items() if v.get("body") is None}


def declare_hook(sp, d):
    out = []
    if d.get("heads"):
        out += [("neck_a", 1.75, 8, 1.75), ("neck_b", 1.5, 8, 1.5), ("dhead", 3.5, 3, 4), ("dsnout", 2.25, 1.75, 2.5)]
    if d.get("mane"):
        out += [("mane", 1, 4, 2.5)]
    return out


def paint_hook(pt, pal, d):
    sc = G.scale_fn(pal)
    for p in [n for n, *_ in SEGS] + ["neck_a", "neck_b", "dhead", "dsnout"]:
        pt(p, "all", sc)
    pt("seg_1", "all", lambda f, x, y, w, h: H.skin_fn(pal, 0.66)(f, x, y, w, h) if y < 2 and f != "down" else sc(f, x, y, w, h))
    dark = G.shades(G.mix(pal.MARK[3][:3], pal.FUR[3][:3], 0.5))
    pt("dhead", "all", lambda f, x, y, w, h: pick(dark, 0.45 + 0.2 * ((x + y) % 3 == 0), x, y))
    pt("dhead", ("east", "west"), lambda f, x, y, w, h: pick(G.shades("#f8d83a"), 0.7, x, y) if y == 2 and 1 <= x <= 2 else None)
    pt("dsnout", "all", lambda f, x, y, w, h: pick(dark, 0.55, x, y) if f != "down" else pick(G.shades("#c83a3a"), 0.5, x, y))
    for p in ("neck_a", "neck_b"):
        pt(p, ("north",), lambda f, x, y, w, h: pick(pal.LIGHT, 0.55 + 0.2 * (y % 2), x, y))
    pt("mane", "all", lambda f, x, y, w, h: pick(pal.HAIR, 0.5 + 0.25 * (x % 2), x, y))
    tal = G.shades(pal.extra.get("talon", "#e8c040"))
    for p in ("uleg_f", "lleg_f"):
        pt(p, "all", lambda f, x, y, w, h: pick(tal, 0.5 + 0.2 * (y % 2), x, y))
    fur = G.fur_fn(pal, light_faces=("down",), grain=0.1, seed=31)
    for p in ("barrel", "uleg_b", "lleg_b", "withers"):
        pt(p, "all", fur)
    pt("hoof", "all", lambda f, x, y, w, h: pick(G.shades("#2a2420"), 0.4, x, y))
    pt("htail", "all", lambda f, x, y, w, h: pick(pal.MARK if y > h * 0.7 else pal.FUR, 0.5, x, y))


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    if d.get("heads"):                                    # seven dragon heads rising behind and around her own
        for i in range(7):
            ry = -135 + i * 45                              # fanned round the sides and back, none in front of her face
            rx = 138 + (i % 2) * 10
            base = (round(2.6 * math.sin(math.radians(ry)), 2), 18.5, round(1.0 + 2.0 * math.cos(math.radians(ry)), 2))
            names = P.chain(r, f"halo_neck{i + 1}", "body", base, ["neck_a", "neck_b"], rx=rx, ry=ry + 180, bend=8)
            last = names[-1]
            px, py, pz = r.b[last]["pivot"]
            # the head sits at the end of the neck: find it by rotating the neck's end point like the renderer does
            end = H.rot_about((px, py - 7.75, pz), (px, py, pz), H.rot_matrix(rx + 8, ry + 180, 0))
            hx, hyy, hz = end
            r.add(f"halo_head{i + 1}", last, [round(hx, 2), round(hyy, 2), round(hz, 2)],
                  [G.rot_cube("dhead", [hx - 1.75, hyy - 1.5, hz - 2.0], [hx, hyy, hz], 0, ry, 0),
                   G.rot_cube("dsnout", [hx - 1.125, hyy - 1.5, hz - 4.3], [hx, hyy, hz], 0, ry, 0)])


# ----------------------------------------------------------------------------- serpents and the gryphon
def serpent_declare(sp, d):
    parts = list(d["segs"]) + P.horn_parts(d.get("horns")) + declare_hook(sp, d)
    G.declare(parts, ears=d["ears"], hair=d["hair"], build=d["build"], outfit=d["outfit"])


def serpent_build(sp, d):
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    g = G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), build=d["build"], outfit=d["outfit"], ears=d["ears"], skirt=False, cuffs=False))
    G.serpent(r, None, d)
    if d.get("horns"):
        P.horns(r, d["horns"], g.hy, g.z)
    if d.get("mane"):
        for i in range(2, 8):
            n = f"seg_{i}"
            w, h, dd = G.S(n)
            pz = r.b[n]["pivot"][2]
            r.put(n, cube("mane", [-0.5, h - 0.25, pz + 1.0]))
    return r.model(sp)


def serpent_anims(an, flies):
    segs = [f"seg_{i}" for i in range(1, 9)]
    G.serpent_anims(an, segs)
    an.add("call", 1.5, False, H.merge({"head": {"rotation": H.hold(-20, 0, 0)}, "eyelids": {"position": H.hold(*H.OUT_)}},
                                        H.sym("arm", H.track((0, (0, 0, 0)), (0.5, (-170, 0, 10)), (1.2, (-170, 0, 10)), (1.5, (0, 0, 0))))))
    if flies:
        Tk, hold, merge = H.track, H.hold, H.merge
        flat = [(80, 0, 0)] + [(0, 0, 0)] * 7
        an.add("fly", 3.0, True, merge(G.upper_idle(), {"root": {"position": hold(0, 6, 0)}, "body": {"rotation": hold(-10, 0, 0)}},
                                       G.wave(segs, 3.0, [0, 12, 16, 18, 20, 22, 24, 26], axis=1, phase=0.8, base=flat),
                                       H.sym("arm", hold(-20, 0, 30))))


def gryphon_declare(sp, d):
    parts = [("barrel", 7, 7, 15), ("withers", 5, 2.5, 5), ("uleg_f", 2.25, 6.5, 2.75), ("lleg_f", 1.75, 5.75, 1.75),
             ("uleg_b", 2.5, 6.5, 3.25), ("lleg_b", 1.75, 5.75, 1.75), ("hoof", 2.25, 1.25, 2.25), ("htail", 1.5, 8, 1.5)]
    G.declare(parts + P.wing_parts("feather", 1.2), ears=d["ears"], hair=d["hair"], build=d["build"], outfit=d["outfit"])


def gryphon_build(sp, d):
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    G.upper(r, 19.5, dict(hair=d["hair"], bangs=d.get("bangs"), build=d["build"], outfit=d["outfit"], ears=d["ears"], skirt=False), z0=-2.25)
    G.taur(r, None, d)
    P.wings(r, "feather", "barrel", y=19.0, z=-1.0, root_x=2.5)
    return r.model(sp)


def gryphon_anims(an):
    G.taur_anims(an)
    names = ["wing_r", "wing_l"]
    for key in ("idle", "walk", "sit", "lie", "rear", "kick", "graze"):
        F._inject(an, key, P.folded(names, 70, -10))
    F._inject(an, "run", P.flap(names, 0.6, 30, 20))
    hold, merge = H.hold, H.merge
    an.add("fly", 1.2, True, merge(G.upper_idle(), P.flap(names, 1.2, 45, 35, sweep=5),
                                   {"leg_f_r": {"rotation": hold(-50, 0, 0)}, "leg_f_l": {"rotation": hold(-50, 0, 0)},
                                    "leg_b_r": {"rotation": hold(50, 0, 0)}, "leg_b_l": {"rotation": hold(50, 0, 0)}, "tail": {"rotation": hold(60, 0, 0)}}))


def anim_extra(an, sp, d):
    """fire breath ("breathe"), the sting ("sting"), Kannae's lightning call ("call")"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("breathe", 1.5, False, merge({"head": {"rotation": Tk((0, (0, 0, 0)), (0.3, (-25, 0, 0)), (0.6, (10, 0, 0)), (1.5, (10, 0, 0)))},
                                         "body": {"rotation": Tk((0, (0, 0, 0)), (0.3, (-10, 0, 0)), (0.6, (12, 0, 0)), (1.5, (0, 0, 0)))},
                                         "mouth": {"position": Tk((0, H.REST), (0.5, H.OUT_), (1.3, H.OUT_), (1.5, H.REST))}}))
    tails = ["tail", "tail_2", "tail_3", "tail_4"]
    an.add("sting", 0.8, False, merge({t: {"rotation": Tk((0, (0, 0, 0)), (0.3, (-60, 0, 0)), (0.5, (40, 0, 0)), (0.8, (0, 0, 0)))} for t in tails}))
    an.add("call", 1.5, False, merge({"head": {"rotation": hold(-20, 0, 0)}, "eyelids": {"position": hold(*H.OUT_)}},
                                      sym("arm", Tk((0, (0, 0, 0)), (0.5, (-170, 0, 10)), (1.2, (-170, 0, 10)), (1.5, (0, 0, 0))))))


if __name__ == "__main__":
    import family_concepts as FC
    want = [a for a in sys.argv[1:] if not a.startswith("--")]
    prev = "--previews" in sys.argv
    heights = {s["id"]: s["height"] for s in FC.family("dragon")["species"]}
    entries = {}

    def custom(sp, decl, bld, anm):
        d = SPECIES[sp]
        decl(sp, d)
        tex = F.paint(sp, d, F.pal_for(d), paint_hook)
        geo, adoc, texes = G.write(sp, tex, bld(sp, d), anm, list(d["variants"]), lambda k: F.paint(sp, d, F.pal_for(d, k), paint_hook))
        h = G.model_height(geo)
        if prev:
            G.previews(sp, geo, adoc, texes, F.VIEWS, F.FACE, h)
        entries[sp] = (geo, tex, adoc["animations"][f"animation.{sp}.idle"], heights[sp], h)

    for sp in ("long_dragon", "jormungandr_dragon"):
        if not want or sp in want:
            custom(sp, serpent_declare, serpent_build, lambda an, sp=sp: serpent_anims(an, SPECIES[sp].get("flies")))
    if not want or "gryphon_dragon" in want:
        custom("gryphon_dragon", gryphon_declare, gryphon_build, gryphon_anims)
    for e, sp in zip(F.run_family("dragon", BIPEDS, declare_hook, paint_hook, build_hook, anim_extra, only=[k for k in BIPEDS if not want or k in want]),
                     [s["id"] for s in FC.family("dragon")["species"] if s["id"] in BIPEDS and (not want or s["id"] in want)]):
        entries[sp] = e
    if prev and not want:
        G.roster(os.path.join(G.PREVIEWS, "dragon_roster.png"), [entries[s["id"]] for s in FC.family("dragon")["species"]])
