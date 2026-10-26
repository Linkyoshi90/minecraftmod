#!/usr/bin/env python3
"""Standalone red Blaze Cat Nekomata concept, based on the supplied chibi reference.

Run from the repository root:
    python tools/gen_blaze_cat_nekomata_sketch.py --previews

This deliberately does not replace or register the existing blaze_cat_nekomata.
The geometry can be imported into Blockbench for further hand editing.
"""
import json
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_lib as G  # noqa: E402
import girl_family as F  # noqa: E402
from girl_lib import H, CLEAR, BROAD, cube, pick, shades  # noqa: E402

NAME = "blaze_cat_nekomata_sketch"
OUTFIT = dict(top=["halter", "corset"], bottom="wrap_skirt",
              colors=dict(cloth2="#8f101d", leather="#32151c"))
GLOW_PARTS = ["ear_a", "ear_b", "ember_crest", "tail_a", "tail_b", "tail_c", "fire_tongue"]


def declare():
    # Re-declaring shared parts changes only this standalone model's texture layout.
    parts = [
        ("ear_a", 3.25, 5.5, .75), ("ear_b", 2.0, 3.0, .75),
        ("thigh", 2.75, 3.25, 2.75), ("shin", 2.25, 4.5, 2.25),
        ("paw", 2.75, 1.25, 3.25), ("toe", .65, .55, .65),
        ("muzzle", 6.0, 2.5, 2.25), ("nose", .75, .6, .4),
        ("paw_hand", 2.5, 2.0, 2.25), ("paw_thumb", .9, 1.1, 1.0),
        ("ember_crest", 2.5, 3.0, .25),
        ("tail_a", 2.0, 2.25, 4.5), ("tail_b", 2.75, 2.75, 4.75),
        ("tail_c", 3.5, 3.25, 4.25), ("fire_tongue", 1.25, 3.5, .5),
        ("back_bow", 2.75, 2.0, .75),
    ]
    G.declare(parts, ears="big", hair="shaggy", build="petite", outfit=OUTFIT)


def palette():
    p = G.palette(
        SKIN=G.skin("#c82835"), HAIR="#c51627", FUR="#d82935",
        LIGHT="#fff4e8", MARK="#55101d", EYE="#a53bd0",
        CLOTH="#c5162d", TRIM="#ffb52e", INNER="#ff805e",
        EYE_OUT="#240d24", PUPIL="#190b20", BLUSH="#f38c96", BLUSH_L="#e76578",
    )
    p.pattern = "solid"
    p.pupil = (.85, 3.6)
    p.eye_a, p.eye_b = 4.8, 5.25
    p.extra = dict(cloth2="#8f101d", leather="#32151c")
    return p


def _taper(f, x, y, w, h, from_top=True, power=.8):
    """Transparent silhouette on the two broad faces, pointed at one end."""
    if f not in BROAD:
        return False
    remaining = y if from_top else h - 1 - y
    length = max(2.0, h * .8)
    if remaining >= length:
        return False
    half = (w / 2) * (min(1.0, (remaining + 1) / length) ** power)
    return abs(x + .5 - w / 2) > half


def paint():
    p = palette()
    pt = H.Painter()
    G.paint_upper(pt, p, top="wrap", outfit=OUTFIT, build="petite")

    red = G.shades("#d82935", nodither=True)
    dark_red = G.shades("#8f101d", nodither=True)
    white = G.shades("#fff4e8", nodither=True)
    orange = G.shades("#ff6b16", nodither=True)
    gold = G.shades("#ffc52e", nodither=True)
    hot = G.shades("#fff39a", nodither=True)
    pink = G.shades("#f05c76", nodither=True)
    black = G.shades("#2a101b", nodither=True)

    def fur(f, x, y, w, h):
        shade = .61 + .10 * (f == "up") - .09 * (f in ("down", "south"))
        return pick(red, shade, x, y)

    # Red fur on the limbs, with a white chest above the dark red dress.
    for part in ("arm_up", "forearm", "thigh", "shin"):
        pt(part, "all", fur)
    pt("collar", "all", lambda f, x, y, w, h: pick(white, .72, x, y))
    pt("chest", ("north",), lambda f, x, y, w, h: pick(white, .72, x, y) if y < h * .45 else None)
    pt("bust", "all", lambda f, x, y, w, h: pick(dark_red, .54 + .12 * (f == "up"), x, y))
    for part in ("panel_f", "panel_s"):
        def skirt(f, x, y, w, h):
            if f in BROAD and (y >= h - 2 or (y >= h - 4 and (x + y) % 4 == 0)):
                return pick(black, .48, x, y)
            if f in BROAD and x % 6 == 0:
                return pick(dark_red, .40, x, y)
            return pick(dark_red, .58 + .08 * (f == "up"), x, y)
        pt(part, "all", skirt)
    pt("corset", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else
       (pick(gold, .65, x, y) if f == "north" and abs(x + .5 - w / 2) < 1 and y % 4 < 2
        else pick(dark_red, .48 + .08 * (x % 5 == 0), x, y)))
    pt("belt", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else pick(black, .48, x, y))
    pt("buckle", "all", lambda f, x, y, w, h: pick(gold, .8 if f == "north" else .5, x, y))

    # Long stepped ears: crimson outline, orange interior and yellow-hot flecks.
    def ear(f, x, y, w, h):
        if _taper(f, x, y, w, h):
            return CLEAR
        edge = min(x, w - 1 - x)
        if f == "north" and edge > 1 and 1 < y < h - 2:
            if (x * 3 + y * 5) % 17 < 2 or (y > h * .62 and abs(x + .5 - w / 2) < 1.4):
                return pick(hot, .8, x, y)
            return pick(orange if y < h * .66 else gold, .64 + .12 * (y / h), x, y)
        return pick(red, .40 if edge < 1 else .59, x, y)
    pt("ear_a", "all", ear)
    pt("ear_b", "all", ear)

    # Broad feline muzzle; its tapered lower corners avoid a rectangular snout.
    def muzzle(f, x, y, w, h):
        if f in BROAD and y >= h - 2 and abs(x + .5 - w / 2) > (w / 2 - (y - h + 3) * 2):
            return CLEAR
        if f == "north" and y == h - 2 and abs(x + .5 - w / 2) < 2.0:
            return pick(pink, .42, x, y)
        return pick(white, .70 + .12 * (f == "up") - .10 * (f == "down"), x, y)
    pt("muzzle", "all", muzzle)
    pt("nose", "all", lambda f, x, y, w, h: pick(black, .65 if f == "up" else .35, x, y))

    def hand_paw(f, x, y, w, h):
        if f == "north":
            # Four pink toe beans and one large central pad on the oversized paw.
            for cx, cy, rr in ((.22, .26, 1.5), (.42, .18, 1.5), (.62, .18, 1.5), (.80, .28, 1.5), (.50, .64, 3.0)):
                if (x + .5 - w * cx) ** 2 + (y + .5 - h * cy) ** 2 < rr:
                    return pick(pink, .62, x, y)
        return pick(white, .70 + .10 * (f == "up") - .08 * (f == "down"), x, y)
    pt("paw_hand", "all", hand_paw)
    pt("paw_thumb", "all", lambda f, x, y, w, h: pick(white, .65, x, y))

    def foot(f, x, y, w, h):
        if f == "north" and y < 2 and x % 3 == 1:
            return pick(pink, .52, x, y)
        if f == "down":
            for cx, cy in ((.25, .28), (.5, .22), (.75, .28), (.5, .65)):
                if (x + .5 - w * cx) ** 2 + (y + .5 - h * cy) ** 2 < 2.5:
                    return pick(pink, .5, x, y)
        return fur(f, x, y, w, h)
    pt("paw", "all", foot)
    pt("toe", "all", lambda f, x, y, w, h: pick(pink if f == "north" else red, .58, x, y))

    # A small gold flame/diamond on the forehead, like the reference's bright marking.
    def crest(f, x, y, w, h):
        if f != "north":
            return CLEAR
        dx = abs(x + .5 - w / 2) / (w / 2)
        dy = abs(y + .5 - h / 2) / (h / 2)
        if dx + dy > 1.0:
            return CLEAR
        return pick(hot if dx + dy < .48 else gold, .82, x, y)
    pt("ember_crest", "all", crest)

    # Each tail segment has a ragged flame silhouette and a red -> orange -> yellow core.
    def flame(f, x, y, w, h):
        if f in ("up", "down"):
            return CLEAR
        if f in BROAD:
            # The far end narrows to several uneven licks rather than a square cap.
            t = x / max(1, w - 1)
            centre = h * (.50 + .10 * math.sin(t * math.pi * 2))
            half = h * (.47 - .25 * t)
            if t > .62:
                half *= max(.12, (1 - t) / .38) ** .55
            if abs(y + .5 - centre) > half + ((x + 2 * y) % 3 == 0):
                return CLEAR
            core = abs(y + .5 - centre) < half * .48 and t > .18
            if core and t > .42:
                return pick(hot, .84, x, y)
            if core or t > .52:
                return pick(gold, .72, x, y)
            if t < .25:
                return pick(red, .55, x, y)
            return pick(orange, .66, x, y)
        return pick(orange, .48, x, y)
    for part in ("tail_a", "tail_b", "tail_c"):
        pt(part, "all", flame)

    def tongue(f, x, y, w, h):
        if _taper(f, x, y, w, h, power=.65):
            return CLEAR
        if f in ("up", "down"):
            return CLEAR
        return pick(hot if abs(x + .5 - w / 2) < w * .22 and y > 2 else gold, .78, x, y)
    pt("fire_tongue", "all", tongue)
    pt("back_bow", "all", lambda f, x, y, w, h: pick(white, .65 + .15 * (f == "up"), x, y))
    return pt.T


def short_legs(r):
    cx = -1.35
    r.add("leg_r", "root", [cx, 9, 0], [cube("thigh", [cx - 1.375, 5.75, -1.375])], mirror=True)
    r.add("shin_r", "leg_r", [cx, 5.75, 0], [cube("shin", [cx - 1.125, 1.25, -1.0])], mirror=True)
    foot = [cube("paw", [cx - 1.375, 0, -2.0])]
    for i in range(3):
        foot.append(cube("toe", [cx - .95 + i * .68, .05, -2.35]))
    r.add("foot_r", "shin_r", [cx, 1.25, 0], foot, mirror=True)


def flame_tail(r, name, x=0.0, side=0):
    y, z = 9.55, 1.55
    a, b, c = G.S("tail_a"), G.S("tail_b"), G.S("tail_c")
    r.add(name, "body", [x, y, z], [cube("tail_a", [x - a[0] / 2, y - a[1] / 2, z])])
    z2 = z + a[2] - .3
    r.add(name + "_mid", name, [x, y, z2], [cube("tail_b", [x - b[0] / 2, y - b[1] / 2, z2])])
    z3 = z2 + b[2] - .35
    tip = [cube("tail_c", [x - c[0] / 2, y - c[1] / 2, z3])]
    # Extra thin tongues break up the silhouette without abandoning the voxel style.
    for i, (dx, dy, rz) in enumerate(((-.65, .2, -18), (.65, .65, 15))):
        origin = [x + dx - .625, y + dy, z3 + c[2] - 1.25]
        pivot = [x + dx, y + dy, z3 + c[2] - 1.0]
        tip.append(G.rot_cube("fire_tongue", origin, pivot, -68, 0, rz + side * 4))
    r.add(name + "_tip", name + "_mid", [x, y, z3], tip)


def build():
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    g = G.upper(r, 9, dict(hair="shaggy", bangs="messy", ears="big", skirt=True,
                           build="petite", outfit=OUTFIT))
    short_legs(r)
    flame_tail(r, "tail", x=-.65, side=-1)
    flame_tail(r, "tail2", x=.65, side=1)

    # Replace the human hands with the reference's large white cat paws.
    for side in ("r", "l"):
        sign = -1 if side == "r" else 1
        x, y, z = r.b["hand_" + side]["pivot"]
        r.b["hand_" + side]["cubes"] = [
            cube("paw_hand", [x - 1.25, y - 1.8, z - 1.2]),
            cube("paw_thumb", [x - sign * 1.35 - (.9 if sign < 0 else 0), y - 1.25, z - .5]),
        ]

    r.put("head", cube("muzzle", [-3.0, g.hy + .25, -6.15 + g.z]))
    r.put("head", cube("nose", [-.375, g.hy + 1.75, -6.55 + g.z]))
    r.put("head", cube("ember_crest", [-1.25, g.hy + 4.9, -5.65 + g.z]))
    # Keep the animated expression on the front of the muzzle, not buried inside the head.
    r.b["mouth"]["pivot"] = [0, g.hy + .75, -6.2 + g.z]
    r.b["mouth"]["cubes"] = [cube("mouth", [-.875, g.hy + .15, -6.48 + g.z])]

    bow = cube("back_bow", [-2.9, 8.7, 1.7], pivot=[0, 9.5, 1.9], rotation=[0, 0, -20])
    r.put("body", bow, G.mirror_cube(bow))
    return r.model(NAME)


def anims(an):
    G.biped_anims(an, ["tail", "tail_mid", "tail_tip"], ["tail2", "tail2_mid", "tail2_tip"])


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", .25, -30, 8), ("run", .15, -60, 5), ("sit", 0, -30, 10), ("pounce", .5, -70, 5)]
FACE = [("idle", 0, 0, 0, 21), ("idle", 2.75, 0, 0, 21), ("hiss", .5, 0, 0, 21), ("idle", 0, -30, 8, 21)]


def validate(geo, adoc, tex):
    desc = geo["minecraft:geometry"][0]["description"]
    bones = geo["minecraft:geometry"][0]["bones"]
    names = [b["name"] for b in bones]
    assert len(names) == len(set(names)), "duplicate bone names"
    known = set(names)
    for b in bones:
        assert not b.get("parent") or b["parent"] in known, "missing parent: " + b["name"]
        for c in b.get("cubes", []):
            assert all(v > 0 for v in c["size"]), "non-positive cube size"
            assert all(math.isfinite(v) for v in c["origin"] + c["size"]), "non-finite cube"
            for face, uv in c["uv"].items():
                x, y = uv["uv"]
                w, h = uv["uv_size"]
                assert x >= 0 and y >= 0 and x + w <= desc["texture_width"] and y + h <= desc["texture_height"], \
                    "out-of-bounds UV: " + b["name"] + "/" + face
    for key, animation in adoc["animations"].items():
        assert set(animation["bones"]) <= known, "unknown animated bone in " + key
    assert len(tex) == desc["texture_height"] and all(len(row) == desc["texture_width"] for row in tex)
    opaque = sum(px[3] > 0 for row in tex for px in row)
    assert opaque > 0 and any(px[3] == 0 for row in tex for px in row), "texture alpha/silhouette missing"
    print(f"validated: {len(names)} unique bones, {len(adoc['animations'])} animations, UVs in bounds, {opaque} opaque texels")


def run(previews=False):
    declare()
    tex = paint()
    geo, adoc, _ = G.write(NAME, tex, build(), anims)
    F.write_glowmask(NAME, tex, GLOW_PARTS)
    validate(geo, adoc, tex)
    if previews:
        G.previews(NAME, geo, adoc, {"": tex}, VIEWS, FACE, 32)
    return geo, adoc, tex


if __name__ == "__main__":
    run("--previews" in sys.argv)
