#!/usr/bin/env python3
"""Arachne (the spider family, see arachne_field_guide.html): jumping spider, orb weaver, tarantula, trapdoor spider,
wolf spider.

    python tools/gen_arachne.py [species ...] [--previews]

Shared girl_lib upper body on a spider's thorax with an abdomen behind and eight legs (leg1_r..leg4_l, knees
leg1b_r..). Four small extra eyes on the forehead, big dark eyes. The wolf spider carries a brood of spiderlings on
her abdomen (bone `brood`, shown by the game while she carries young). Five colours per species.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, shades, CLEAR  # noqa: E402

SPECIES = {
    # eighteenth wave: the spoodr, a round, fluffy little navy spider with pale blue tips and two tufts on her head
    "spoodr_arachne": dict(
        build="petite", outfit=dict(top=["sweater"], extras=["scarf"], colors=dict(scarf="#8ac8f0")),
        abdomen=(10, 9, 10), hair="bob", bangs="blunt", hairy=0.1, tufts=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a3a6a", FUR="#26305a", LIGHT="#8ac8f0", MARK="#6ab0f0", EYE="#2a3a6a", CLOTH="#3a4a8a", TRIM="#8ac8f0",
                 pattern="spots", facemark="eyes", extra=dict(tuft="#8ac8f0")),
        variants={"green": dict(FUR="#2a4a2e", HAIR="#2a4a2e", LIGHT="#a8e08a", MARK="#7ac86a", CLOTH="#3a6a3a", TRIM="#a8e08a", extra=dict(tuft="#a8e08a")),
                  "purple": dict(FUR="#3a2a5a", HAIR="#3a2a5a", LIGHT="#c8a0f0", MARK="#a87ae0", CLOTH="#5a3a8a", TRIM="#c8a0f0", extra=dict(tuft="#c8a0f0")),
                  "red": dict(FUR="#5a1e22", HAIR="#5a1e22", LIGHT="#f0a08a", MARK="#e0604a", CLOTH="#8a2a2a", TRIM="#f0a08a", extra=dict(tuft="#f0a08a")),
                  "yellow": dict(FUR="#5a4a1e", HAIR="#5a4a1e", LIGHT="#f8e08a", MARK="#f0c84a", CLOTH="#8a6a2a", TRIM="#f8e08a", extra=dict(tuft="#f8e08a"))}),
    # eighteenth wave: the ushi-oni, the ox-headed spider demon of Japanese coasts, black and red, with an ox's horns
    "ushi_oni_arachne": dict(
        build="strong", outfit=dict(top=["sarashi"], extras=["arm_wraps", "belt"]),
        abdomen=(9.5, 8, 10), hair="shaggy", bangs="messy", hairy=0.15, thick=True, ox_horns=True,
        pal=dict(SKIN=skin("tan"), HAIR="#1e1a1e", FUR="#1e1a1e", LIGHT="#4a3a3e", MARK="#b82a2a", EYE="#e83a2a", CLOTH="#e8e0d0", TRIM="#b82a2a",
                 pattern="bands", facemark="eyes", extra=dict(horn="#e8dcc0", horn_tip="#2a2420")),
        variants={"crimson": dict(FUR="#7a1a1e", HAIR="#3a0e10", LIGHT="#b84a3a", MARK="#1e1a1e", EYE="#f8d03a"),
                  "indigo": dict(FUR="#1e2a5a", HAIR="#141a3a", LIGHT="#4a5a9a", MARK="#e8c040", EYE="#f8d03a", TRIM="#e8c040"),
                  "ash": dict(FUR="#6a6664", HAIR="#3a3634", LIGHT="#a8a4a0", MARK="#2a2624", EYE="#e83a2a", SKIN=skin("pale")),
                  "ochre": dict(FUR="#8a5a1e", HAIR="#3a2410", LIGHT="#c8904a", MARK="#2a1a10", EYE="#e8e0d0", extra=dict(horn="#f4ecd8", horn_tip="#5a3a1a"))}),
    "jumping_spider_arachne": dict(
        build="petite", outfit=dict(top=["sweater"], colors=dict()),
        abdomen=(6.5, 6, 7.5), hair="odango", hairy=0.08,
        pal=dict(SKIN=skin("pale"), HAIR="#1e1e24", FUR="#1a1a1e", LIGHT="#3a3a40", MARK="#f2f2f0", EYE="#2a2a36", CLOTH="#2a8a8a", TRIM="#e8e8e8",
                 pattern="spots", facemark="eyes"),
        variants={"bold": dict(MARK="#e8602a", CLOTH="#c8502a"), "zebra": dict(pattern="finestripes", HAIR="#e8e8e8"),
                  "golden": dict(FUR="#b08a40", MARK="#f0e0b0", pattern="bands", HAIR="#c89a48", CLOTH="#6a4a2a"),
                  "teal": dict(FUR="#1a3a3a", MARK="#3ad0c0", HAIR="#1a4a4a"),
                  # homage (2026-10-30): the spider of Webbed: round, black and gold, a tiny red bow's colour on her top
                  "tiny_jumper": dict(FUR="#1a1a1e", HAIR="#1a1a1e", LIGHT="#e8b040", MARK="#e8b040", CLOTH="#c82a4a", TRIM="#e8b040", pattern="bands")}),
    "orb_weaver_arachne": dict(
        build="slim", outfit=dict(top=["bandeau", "corset"], extras=["pearls"], colors=dict(cloth2="#2a2420")),
        abdomen=(9, 8.5, 9), hair="drills", hairy=0.0,
        pal=dict(SKIN=skin("fair"), HAIR="#6a4a2a", FUR="#8a6a40", LIGHT="#c8b090", MARK="#f0ead8", EYE="#2a1a12", CLOTH="#4a3a2a", TRIM="#d8c8a0",
                 pattern="cross", facemark="eyes"),
        variants={"goldensilk": dict(FUR="#d8b040", MARK="#3a2a12", pattern="bands", HAIR="#c89a30"),
                  "marbled": dict(FUR="#e09040", MARK="#f4e8c8", pattern="blotches", HAIR="#c87a30", CLOTH="#6a3a1a"),
                  "spiny": dict(FUR="#f4f2ee", MARK="#c8202a", pattern="spots", HAIR="#e8e4e0", CLOTH="#8a1a2a"),
                  "wasp": dict(FUR="#f0d030", MARK="#141414", pattern="bars", HAIR="#f0d84a", CLOTH="#1e1e1e")}),
    "tarantula_arachne": dict(
        build="strong", outfit=dict(top=["bandeau", "vest"], extras=["armlets", "belt"]),
        abdomen=(9, 7.5, 9), hair="shaggy", hairy=0.22, thick=True,
        pal=dict(SKIN=skin("tan"), HAIR="#5a3e3a", FUR="#7a5a54", LIGHT="#d8a8a0", MARK="#d8a8a0", EYE="#2a1a1a", CLOTH="#6a3a4a", TRIM="#e0b8b0",
                 pattern="speckle", facemark="eyes"),
        variants={"redknee": dict(FUR="#1e1a18", MARK="#e0602a", LIGHT="#e0602a", pattern="speckle", HAIR="#2a2220", extra=dict(knees=True)),
                  "cobalt": dict(FUR="#1a2a6a", MARK="#4a6ad8", LIGHT="#6a8ae0", HAIR="#22306a", CLOTH="#3a3a7a"),
                  "pinktoe": dict(FUR="#1e1a1e", MARK="#f0a0c0", LIGHT="#f0a0c0", HAIR="#2a222a", extra=dict(toes=True), CLOTH="#a04a7a"),
                  "goliath": dict(FUR="#6a4a2e", MARK="#a8845a", LIGHT="#c8a070", HAIR="#5a3a22")}),
    "trapdoor_spider_arachne": dict(
        build="standard", outfit=dict(top=["bandeau", "capelet"], extras=["belt", "pouch"]),
        abdomen=(7.5, 6.5, 8), hair="bob", hairy=0.0,
        pal=dict(SKIN=skin("peach"), HAIR="#2a1e16", FUR="#3a2a1e", LIGHT="#6a5040", MARK="#5a4430", EYE="#1e1410", CLOTH="#5a4a3a", TRIM="#a89070",
                 pattern="solid", facemark="eyes"),
        variants={"black": dict(FUR="#151416", LIGHT="#3a3a3e", HAIR="#141414", CLOTH="#3a3a3a"),
                  "redleg": dict(FUR="#151416", LIGHT="#a8282a", MARK="#a8282a", HAIR="#1a1414", extra=dict(redlegs=True), CLOTH="#6a1a1a"),
                  "sandy": dict(FUR="#a08460", LIGHT="#c8b088", HAIR="#8a6a48", CLOTH="#a07a4a"),
                  "moss": dict(MARK="#4a6a2a", pattern="speckle", HAIR="#2a3a1e", CLOTH="#3a5a2a")}),
    "wolf_spider_arachne": dict(
        build="athletic", outfit=dict(top=["sports", "harness"], extras=["bracers", "satchel"]),
        abdomen=(7, 6.5, 9.5), hair="bun", hairy=0.1, brood=True,
        pal=dict(SKIN=skin("olive"), HAIR="#5a4030", FUR="#6a5040", LIGHT="#c8b090", MARK="#c8b090", EYE="#2a1e14", CLOTH="#4a5a3a", TRIM="#c8b890",
                 pattern="finestripes", pattern_scale=1.4, facemark="eyes"),
        variants={"striped": dict(FUR="#7a7672", MARK="#d8d4cc", HAIR="#6a6662", CLOTH="#3a4a5a"),
                  "sand": dict(FUR="#c8b088", MARK="#8a7458", HAIR="#a89068"),
                  "dark": dict(FUR="#2a2420", MARK="#5a4e44", HAIR="#2a2420", CLOTH="#2a2a2a"),
                  "raft": dict(FUR="#2a2420", MARK="#f0ece0", pattern="bars", pattern_scale=2.0, HAIR="#3a3028", CLOTH="#2a4a5a")}),
    # sixteenth wave: the jorogumo, a spider who walks on two legs in a web-printed kimono, her eight long legs spread
    # behind her back like a fan (built by legged_* below, on girl_family's biped)
    "jorogumo_arachne": dict(
        biped=True, build="slim", outfit=dict(top=["kimono"], bottom="wrap_skirt",
                                             colors=dict(kimono="#9a7ad0", kimono_trim="#4a2e78", obi="#d8483a", cloth2="#9a7ad0",
                                                         kimono_web="#3a2458", kimono_flower="#f08aa8")),
        hair="bob", bangs="parted", ears=None, legs="bare", foot="shoe", pupil=(2.2, 3.4), muffet_kit=True,
        pal=dict(SKIN=skin("pale"), HAIR="#6a5ab8", FUR="#1a1820", LIGHT="#3a3644", MARK="#e8c040", EYE="#c81a2a", CLOTH="#9a7ad0", TRIM="#f4f2f0",
                 extra=dict(boot="#a81a22", bow="#c81a22", rose="#f0a0b8", band="#e8c040")),
        variants={"crimson": dict(HAIR="#3a1a2a", extra=dict(kimono="#a8243a", kimono_trim="#5a0e1a", cloth2="#a8243a", obi="#1e1a22",
                                                             kimono_web="#2a0a12", kimono_flower="#f4d0a0", bow="#1e1a22")),
                  "indigo": dict(HAIR="#2a2a5a", extra=dict(kimono="#2a3a7a", kimono_trim="#141a3a", cloth2="#2a3a7a", obi="#e8c040",
                                                            kimono_web="#c8d0f0", kimono_flower="#a8c8f0")),
                  "white": dict(HAIR="#e8e4ec", EYE="#a81a2a", extra=dict(kimono="#f0eef2", kimono_trim="#9a96a8", cloth2="#f0eef2", obi="#1e1a22",
                                                                         kimono_web="#6a6678", kimono_flower="#e8a0b8", bow="#1e1a22")),
                  "black": dict(HAIR="#141218", MARK="#f0d050", extra=dict(kimono="#1e1a24", kimono_trim="#8a6a1e", cloth2="#1e1a24", obi="#c8a030",
                                                                         kimono_web="#c8a030", kimono_flower="#a81a2a", band="#f0d050")),
                  # homage (2026-10-28): Muffet (Undertale): lilac skin, five eyes, black pigtails with red bows, a puffy purple dress, a
                  # teacup; the pigtails, the fifth eye and the cup are drawn only on her
                  "bake_sale": dict(SKIN=skin("#c8a0e0"), HAIR="#1e1a24", FUR="#3a2a5a", EYE="#8a1a5a",
                                    extra=dict(muffet=True, kimono="#6a2a8a", kimono_trim="#3a1458", cloth2="#6a2a8a", obi="#c81a2a",
                                               kimono_web="#9a5ab8", kimono_flower="#f0a0d0", bow="#d81a2a", band="#e870c0", boot="#3a1458",
                                               cup="#f4f0f8", tea="#8a4a2a"))}),
}


def declare(sp):
    d = SPECIES[sp]
    th = d.get("thick")
    parts = [("thorax", 6, 4.75, 6), ("abdomen",) + d["abdomen"], ("spinneret", 1.5, 1, 1),
             ("femur", 7, 1.5 if th else 1.25, 1.5 if th else 1.25), ("tibia", 1.25 if th else 1, 11.5, 1.25 if th else 1), ("tarsus", .75, 3.5, .75)]
    if d.get("brood"):
        parts.append(("spiderling", 1.5, 1, 1.5))
    if d.get("tufts"):
        parts.append(("tuft", 1.5, 2, 1.5))
    if d.get("ox_horns"):
        parts += [("ox_horn", 4, 1.25, 1.25), ("ox_horn_tip", 1, 2.5, 1)]
    G.declare(parts, ears=None, hair=SPECIES[sp]["hair"],
              build=SPECIES[sp].get("build"), outfit=SPECIES[sp].get("outfit"))


def paint(sp, key):
    d = SPECIES[sp]
    pal = G.pal_from(d["pal"], d["variants"].get(key) if key else None)
    pal.pupil = (2.6, 4.2)                                # big dark spider eyes
    pal.EYE = shades("#3a3a4a") if key is None or "EYE" not in d["variants"].get(key, {}) else pal.EYE
    pt = H.Painter()
    G.paint_upper(pt, pal, top="wrap", top_pattern="weave", outfit=d.get("outfit"), build=d.get("build"))
    ch = G.chitin_fn(pal, hairy=d["hairy"])
    plain = G.chitin_fn(pal, pattern="solid", hairy=d["hairy"])
    pt("thorax", "all", plain)
    pt("abdomen", "all", ch)
    pt("spinneret", "all", lambda f, x, y, w, h: pick(pal.FUR, 0.3, x, y))
    ex = pal.extra
    leg_ramp = pal.LIGHT if ex.get("redlegs") else pal.FUR
    pt("femur", "all", lambda f, x, y, w, h: pick(leg_ramp, 0.5 + (noise(x, y, 3) - 0.5) * 0.15, x, y) if not d["hairy"] or noise(x, y, 4) > d["hairy"] else pick(pal.LIGHT, 0.6, x, y))

    def tibia(f, x, y, w, h):
        if ex.get("knees") and y < 3:
            return pick(pal.MARK, 0.55, x, y)                 # the red-knee's orange knees
        if d["pal"]["pattern"] == "finestripes" and y % 4 == 0:
            return pick(pal.MARK, 0.4, x, y)                  # banded legs
        if d["hairy"] and noise(x, y, 6) > 1 - d["hairy"]:
            return pick(pal.LIGHT, 0.6, x, y)
        return pick(leg_ramp, 0.45 + (noise(x, y, 5) - 0.5) * 0.15, x, y)
    pt("tibia", "all", tibia)
    pt("tarsus", "all", lambda f, x, y, w, h: pick(pal.MARK if ex.get("toes") else leg_ramp, 0.35, x, y))
    pt("spiderling", "all", lambda f, x, y, w, h: pick(pal.FUR, 0.6 + 0.3 * ((x + y) % 3 == 0), x, y))
    tuft = shades(ex.get("tuft", pal.extra.get("tuft", "#8ac8f0")))
    pt("tuft", "all", lambda f, x, y, w, h: pick(tuft, 0.5 + 0.25 * (y >= h - 1) + (noise(x, y, 81) - 0.5) * 0.2, x, y))
    horn, horn_tip = shades(ex.get("horn", "#e8dcc0")), shades(ex.get("horn_tip", "#2a2420"))
    pt("ox_horn", "all", lambda f, x, y, w, h: pick(horn, 0.5 + 0.2 * (f == "up") - 0.12 * (x % 3 == 0), x, y))
    pt("ox_horn_tip", "all", lambda f, x, y, w, h: pick(horn_tip if y >= h - 1.5 else horn, 0.5 + 0.2 * (f == "up"), x, y))
    skin_t = H.skin_fn(pal, 0.66)
    pt("hips", "all", lambda f, x, y, w, h: skin_t(f, x, y, w, h) if y < h * 0.5 else plain(f, x, y, w, h))
    return pt.T


def build(sp):
    d = SPECIES[sp]
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    g = G.upper(r, 12, dict(hair=d["hair"], bangs=d.get("bangs"), build=d.get("build"), outfit=d.get("outfit"), ears=None, skirt=False))
    G.spider(r, None, d)
    hy, z = g.hy, g.z
    if d.get("tufts"):                                   # two soft tufts like a jumping spider's, on top of her head
        r.add("hair_tufts", "head", [0, hy + 8.2, z], [G.rot_cube("tuft", [x - 0.75, hy + 8.0, -1.5 + z], [x, hy + 8.0, -0.75 + z], -10, 0, -18 * sg)
                                                        for x, sg in ((-2.4, -1), (2.4, 1))])
    if d.get("ox_horns"):                                # an ox's horns: out to the sides from her temples, then up at the tips
        cs = []
        for sg in (-1, 1):
            base = [sg * 3.9, hy + 7.0, -1.0 + z]
            x0 = base[0] if sg > 0 else base[0] - 4
            cs.append(G.rot_cube("ox_horn", [x0, hy + 6.4, -1.6 + z], base, 0, 0, 12 * sg))
            cs.append(G.rot_cube("ox_horn_tip", [sg * 7.6 - 0.5, hy + 7.2, -1.5 + z], [sg * 7.6, hy + 7.2, -1.0 + z], 0, 0, -20 * sg))
        r.add("hair_horns", "head", [0, hy + 7, z], cs)
    if d.get("brood"):
        aw, ah, ad = G.S("abdomen")
        top = 6.5 + ah
        cs = [cube("spiderling", [x, top - 0.25, 3.5 + z]) for x, z in ((-2.5, 1.5), (0.8, 3.0), (-1.0, 5.2), (1.6, 6.4))]
        r.add("brood", "abdomen", [0, top, 6], cs)
    return r.model(sp)


def anims(an):
    G.spider_anims(an)


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.2, -30, 12), ("leap", 0.4, -60, 8), ("sit", 0, -30, 12), ("rear", 0.5, -30, 8)]
FACE = [("idle", 0, 0, 0, 24), ("idle", 2.75, 0, 0, 24), ("rear", 0.5, 0, 0, 24), ("idle", 0, -30, 8, 24)]

# --- the two-legged ones (the jorogumo): girl_family's biped with eight spider legs fanned out from her back ----------

# per leg (top pair first): how far the femur, tibia and claw lift from level (degrees, negative = down), how far it
# sweeps back from her shoulders, and the height on her back it grows from. These are the right legs; the left mirror them.
BACK_LEGS = [(42, 24, -62, 26, 19.0), (16, -38, -76, 32, 18.0), (-8, -64, -84, 38, 17.0), (-30, -80, -88, 44, 16.0)]
FEMUR, TIBIA, CLAW = (8, 1.25, 1.25), (10, 1, 1), (4.5, .75, .75)


def legged_declare(sp, d):
    out = [("bfemur",) + FEMUR, ("btibia",) + TIBIA, ("bclaw",) + CLAW, ("bow", 3, 1.75, 1), ("bow_knot", 1, 1.25, 1.25), ("rose", 1.5, 1.5, 1)]
    if d.get("muffet_kit"):                              # Muffet's pigtails, their bows, her teacup and saucer (drawn only on her)
        out += [("mpig", 2.5, 7, 2.5), ("mpig_bow", 3, 1.5, 1), ("mcup", 2, 1.75, 2), ("msaucer", 3, .5, 3)]
    return out


def legged_paint(pt, pal, d):
    black, band = pal.FUR, G.shades(pal.extra.get("band", "#e8c040"))

    def chitin(f, x, y, w, h):
        if f in ("east", "west") or x < 2 or x >= w - 2:     # the joints: a gold band at each end
            return pick(band, 0.5 + 0.2 * (f == "up"), x, y)
        return pick(black, 0.35 + 0.25 * (f == "up") + (noise(x, y, 71) - 0.5) * 0.1, x, y)
    pt("bfemur", "all", chitin)
    pt("btibia", "all", chitin)
    pt("bclaw", "all", lambda f, x, y, w, h: pick(black, 0.3 + 0.3 * (f == "up"), x, y))
    bow, rose = G.shades(pal.extra.get("bow", "#c81a22")), G.shades(pal.extra.get("rose", "#f0a0b8"))
    muffet = pal.extra.get("muffet")
    pt("bow", "all", lambda f, x, y, w, h: CLEAR if muffet else pick(bow, 0.5 + 0.2 * (f == "north") - 0.15 * (abs(x - w / 2) < 1.5), x, y))
    pt("bow_knot", "all", lambda f, x, y, w, h: CLEAR if muffet else pick(bow, 0.4, x, y))
    pt("rose", "all", lambda f, x, y, w, h: CLEAR if muffet else pick(rose, 0.45 + 0.3 * ((x + y) % 2), x, y))
    if d.get("muffet_kit"):
        hair, cup, tea = pal.HAIR, G.shades(pal.extra.get("cup", "#f4f0f8")), G.shades(pal.extra.get("tea", "#8a4a2a"))
        pt("mpig", "all", lambda f, x, y, w, h: pick(hair, 0.35 + 0.15 * (x % 2) - 0.1 * (y == h - 1), x, y) if muffet else CLEAR)
        pt("mpig_bow", "all", lambda f, x, y, w, h: pick(bow, 0.5 + 0.2 * (f == "north") - 0.15 * (abs(x - w / 2) < 1), x, y) if muffet else CLEAR)
        pt("mcup", "all", lambda f, x, y, w, h: CLEAR if not muffet or f == "down"
           else pick(tea, 0.4, x, y) if f == "up" else pick(cup, 0.55 - 0.15 * (y == 0) + 0.1 * (y == h - 1), x, y))
        pt("msaucer", "all", lambda f, x, y, w, h: pick(cup, 0.5 + 0.1 * (f == "up"), x, y) if muffet else CLEAR)

    def forehead_eyes(f, x, y, w, h):                        # a second, smaller pair of red eyes above her own (Muffet: a third between them)
        for cx in ((11.5, 16, 20.5) if pal.extra.get("muffet") else (11.5, 20.5)):
            dx, dy = abs(x - cx), abs(y - 7.5)
            if dx < 2 and dy < 2 and dx + dy < 3:
                if x == round(cx - 1) and y == 6:
                    return pal.WHITE
                return pick(pal.EYE, 0.15 if dx + dy >= 2 else 0.45 + 0.15 * (y >= 8), x, y)
        return None
    pt("head", ("north",), forehead_eyes)


def legged_build(r, g, d):
    for i, (lf, lt, lc, back, y) in enumerate(BACK_LEGS):
        n = i + 1
        piv = [-1.5, y, g.z + 1.75]

        def seg(part, size, start, lift):
            """a segment reaching out (-X) from `start`, lifted by `lift` and swept back; and the point where it ends"""
            L = size[0]
            c = G.rot_cube(part, [start[0] - L, start[1] - size[1] / 2, start[2] - size[2] / 2], start, 0, -back, -lift)
            end = H.rot_about((start[0] - L, start[1], start[2]), start, H.rot_matrix(*c["rotation"]))
            return c, [round(v, 3) for v in end]
        fem, knee = seg("bfemur", FEMUR, piv, lf)
        r.add(f"bleg{n}_r", "body", piv, [fem], mirror=True)
        tib, ankle = seg("btibia", TIBIA, knee, lt)
        r.add(f"bleg{n}b_r", f"bleg{n}_r", knee, [tib], mirror=True)
        claw, _ = seg("bclaw", CLAW, ankle, lc)
        r.add(f"bleg{n}c_r", f"bleg{n}b_r", ankle, [claw], mirror=True)
    hy, z = g.hy, g.z                                       # a red bow on top of her head, a pink rose beside it
    r.add("hair_bow", "head", [1.5, hy + 8.5, z], [cube("bow", [0.25, hy + 8.0, -1.5 + z]), cube("bow_knot", [1.25, hy + 7.9, -1.6 + z]),
                                              cube("rose", [2.5, hy + 6.9, -3.0 + z])])
    if d.get("muffet_kit"):                              # pigtails hanging from high on each side of her head, a red bow at each; a teacup in her hand
        for sg in (-1, 1):
            r.add("pigtail" + ("_r" if sg < 0 else "_l"), "head", [sg * 5.6, hy + 5, z + 0.5],
                  [G.rot_cube("mpig", [sg * 6.9 - 1.25, hy - 2.5, z - 0.75], [sg * 5.6, hy + 5, z + 0.5], 0, 0, sg * 8),
                   G.rot_cube("mpig_bow", [sg * 6.9 - 1.5, hy + 4.2, z - 1.25], [sg * 5.6, hy + 5, z + 0.5], 0, 0, sg * 8)])
        px, py, pz = r.b["hand_r"]["pivot"]
        r.add("teacup", "hand_r", [px, py - 2.5, pz], [cube("msaucer", [px - 2.25, py - 3.6, pz - 3.6]), cube("mcup", [px - 1.75, py - 3.1, pz - 3.1])])


def legged_anims(an, sp, d):
    import math
    import girl_family as F
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    legs = [f"bleg{i}" for i in range(1, 5)]
    sides = (("_r", -1), ("_l", 1))

    def sway(period, amp):
        out = {}
        for i, l in enumerate(legs):
            for side, sg in sides:
                ph = i * 0.25 + (0.5 if side == "_l" else 0)
                out[l + side] = {"rotation": Tk(*[(period * k / 4, (0, 0, round(sg * amp * math.sin(2 * math.pi * (k / 4 + ph)), 2))) for k in range(5)])}
        return out
    for key, period, amp in (("idle", 4.0, 3), ("walk", 1.0, 6), ("run", 0.6, 9), ("sit", 4.0, 2), ("groom", 2.0, 3), ("hiss", 1.5, 2), ("stalk", 1.8, 4)):
        F._inject(an, key, sway(period, amp))
    raised = {l + s: {"rotation": hold(0, 0, sg * 22)} for l in legs for s, sg in sides}          # legs raised, spread wide
    an.add("rear", 1.5, True, merge(G.upper_idle(), raised, {"body": {"rotation": hold(-8, 0, 0)}, "mouth": {"position": hold(*H.OUT_)}},
                                    sym("arm", hold(-40, 0, 25))))
    an.add("leap", 0.8, False, merge({"root": {"position": Tk((0, (0, 0, 0)), (0.25, (0, -1.5, 0)), (0.45, (0, 3, -3)), (0.8, (0, 0, 0)))}},
                                     {l + s: {"rotation": Tk((0, (0, 0, 0)), (0.3, (0, 0, sg * 30)), (0.8, (0, 0, 0)))} for l in legs for s, sg in sides},
                                     sym("arm", Tk((0, (0, 0, 0)), (0.35, (-120, 0, 10)), (0.8, (0, 0, 0))))))
    an.add("spin", 2.0, True, merge(G.upper_idle(),
                                    {"bleg1_r": {"rotation": Tk((0, (0, 0, 0)), (0.5, (0, 20, -15)), (1, (0, 0, 0)), (1.5, (0, -20, -15)), (2, (0, 0, 0)))},
                                     "bleg1_l": {"rotation": Tk((0, (0, 0, 0)), (0.5, (0, -20, 15)), (1, (0, 0, 0)), (1.5, (0, 20, 15)), (2, (0, 0, 0)))}},
                                    sym("arm", Tk((0, (-60, 0, 10)), (1, (-70, 0, 25)), (2, (-60, 0, 10))))))
    an.add("dig", 0.8, True, merge({"body": {"rotation": hold(35, 0, 0)}, "head": {"rotation": hold(-20, 0, 0)}},
                                   sym("arm", Tk((0, (-60, 0, 5)), (0.4, (-20, 0, 5)), (0.8, (-60, 0, 5))))))


# homage looks of the twenty-ninth wave (POP_CULTURE_VARIANTS.md)
SPECIES["jumping_spider_arachne"]["variants"]["web_hero"] = dict(HAIR="#3a2418", FUR="#c82a2a", LIGHT="#e8483a", MARK="#1e3ab0", EYE="#f4f4f8", CLOTH="#c82a2a", TRIM="#1e3ab0")


if __name__ == "__main__":
    import girl_family as F
    legged = {k: v for k, v in SPECIES.items() if v.get("biped")}
    F.run_family("arachne", legged, legged_declare, legged_paint, legged_build, legged_anims)
    G.run_family("arachne", {k: v for k, v in SPECIES.items() if k not in legged}, declare, paint, build, anims, VIEWS, FACE)
