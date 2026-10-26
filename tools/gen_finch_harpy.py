#!/usr/bin/env python3
"""Finch Harpy - compact, high-density voxel interpretation of the harpy concept sheet.

    python tools/gen_finch_harpy.py
    python tools/gen_finch_harpy.py --preview previews/finch_harpy_preview.png
    python tools/gen_finch_harpy.py --bounce-preview previews/finch_harpy_sleep_bounce.gif

The model is independent of the sparrow assets but keeps their skeleton, so the standard harpy
animations and expression bones still work. Geometry is deliberately built from small, broad colour-
blocked cuboids rather than noisy textures or a smooth high-poly mesh.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
from harpy_lib import cube, hang, mirror_cube, ramp, rgb, CLEAR, BROAD

NAME = "finch_harpy"
SIDES = ("east", "west")


def palette():
    p = H.Pal()
    p.HAIR = ramp([
        rgb(55, 30, 22), rgb(78, 42, 28), rgb(105, 57, 35),
        rgb(137, 79, 47), rgb(172, 110, 67), rgb(205, 151, 99),
    ], nodither=True)
    p.FEA = ramp([
        rgb(47, 31, 26), rgb(68, 43, 32), rgb(96, 61, 42),
        rgb(132, 91, 62), rgb(177, 137, 98), rgb(222, 202, 169), rgb(247, 237, 216),
    ], nodither=True)
    p.EYE = ramp([
        rgb(105, 57, 15), rgb(151, 91, 20), rgb(202, 139, 35),
        rgb(235, 183, 65), rgb(255, 222, 127),
    ], nodither=True)
    p.LEG = ramp([
        rgb(116, 78, 25), rgb(166, 116, 35), rgb(215, 161, 52),
        rgb(239, 195, 85), rgb(255, 226, 139),
    ], nodither=True)
    p.BROW = p.HAIR[1]
    p.eye_a, p.eye_b = 4.1, 4.5
    p.pupil = (1.75, 2.8)
    p.hl_dy, p.hl2_dy = 3, 2
    p.lid_r = (4.9, 5.2)
    p.brows = [(8, 10, -4, 3, -0.08, 1), (24, 10, -4, 3, 0.08, 1)]
    return p


PAL = palette()
LEAF = ramp([
    rgb(37, 47, 27), rgb(55, 68, 35), rgb(78, 91, 46),
    rgb(108, 119, 61), rgb(148, 151, 86),
], nodither=True)
COTTON = ramp([
    rgb(173, 157, 133), rgb(207, 194, 169), rgb(235, 225, 202), rgb(250, 244, 226),
], nodither=True)
GOLD = ramp([rgb(139, 98, 39), rgb(198, 150, 62), rgb(237, 200, 111)], nodither=True)


def declare_parts():
    H.standard_parts()
    P = H.part
    # Face and compact body.
    P("cheek", .75, 1.5, 1.25)
    P("neck", 2, 2, 2)
    P("arm_up", 1.5, 3.25, 1.5)
    P("forearm", 1.5, 2.75, 1.5)
    P("palm", 1.5, 1.25, 1)
    P("thigh", 2.25, 3, 2.25)
    P("shin", 1.5, 5, 1.5)
    P("shin_lower", 1.25, 2.75, 1.25)
    P("hock", 1.75, 1.25, 1.75)
    P("knee", 2.5, 1.5, 2.5)
    # Chunky, stepped hair forms.
    P("hair_cap", 8.5, 8.5, 8.5, ("down",))
    P("crown_a", 3, 1.5, 3)
    P("crown_b", 2, 1.25, 2.5)
    P("crown_c", 1.5, 1, 2)
    P("hair_puff", 2, 3, 2)
    P("hair_puff_small", 1.5, 2, 1.5)
    P("bang_s", 1.5, 2.5, .75)
    P("bang_m", 2, 3.5, .75)
    P("bang_l", 1.5, 4.5, .75)
    P("bang_tip", 1, 1.5, .75)
    P("side_lock", 2, 5.5, 1.5)
    P("side_tip", 1.5, 3, 1)
    P("back_lock", 2, 7, 1.25)
    P("back_tip", 1.5, 3, 1)
    P("flyaway", 1.5, 3, .75)
    P("clip_feather", 1.25, 4, .5)
    P("clip_bead", .75, .75, .75)
    # Feathered shoulders, arms and clothing.
    P("mantle", 2, 2.5, 1)
    P("sleeve", 1.5, 4, .75)
    P("arm_feather", 1.5, 6, .75)
    P("chest_fluff", 1.5, 1.5, .75)
    P("belt_knot", 1.5, 1.25, .75)
    P("belt_tie", .5, 3, .25)
    P("hip_feather", 1.25, 4, .5)
    # Broad folded wings, with separate covert / secondary / primary layers.
    P("wing_base", 4.5, 7, 1)
    P("covert", 2, 3.5, .75)
    P("covert_small", 1.75, 2.5, .75)
    P("secondary", 2, 7, .75)
    P("primary", 2, 9, .75)
    P("tail_feather", 1.75, 7.5, .75)
    P("tail_covert", 1.75, 3, .75)
    P("tail_fluff", 4, 2, 1.75)
    # Shaggy leg cuffs and two-part talons.
    P("leg_fluff", 1.5, 3, .75)
    P("leg_fluff_side", .75, 3, 1.5)
    P("claw_a", 1, .75, 1.5)
    P("claw_b", .75, .5, 1.25)


# ----------------------------------------------------------------------------- texture
def paint():
    pt = H.Painter()
    H.paint_head(pt, PAL)
    H.paint_body_skin(pt, PAL)
    H.paint_legs(pt, PAL)
    pt("neck", "all", H.skin_fn(PAL, .62, 0))
    pt("cheek", "all", H.skin_fn(PAL, .72, 0))
    pt("shin_lower", "all", lambda f, x, y, w, h: PAL.LEG[1 if y % 4 == 3 else 2])
    pt("hock", "all", lambda f, x, y, w, h: PAL.LEG[1 if f in SIDES else 2])

    def hair(shade=2, taper=False):
        def fn(f, x, y, w, h):
            if f in ("up", "down"):
                return PAL.HAIR[min(5, shade + (1 if f == "up" else -1))]
            if f in SIDES:
                return PAL.HAIR[max(0, shade - 1)]
            if taper and y >= h - 3 and (x + y) % 3 == 0:
                return CLEAR
            # Broad locks and a single restrained highlight, not per-pixel static.
            idx = shade
            if x < max(1, w // 5) or x >= w - max(1, w // 5):
                idx -= 1
            if w >= 6 and w // 3 <= x < w // 3 + max(1, w // 5) and y < h * .72:
                idx += 1
            if y >= h - max(2, H.D // 2):
                idx = min(5, idx + 1)
            return PAL.HAIR[max(0, min(5, idx))]
        return fn

    pt("hair_cap", "all", hair(2))
    # Keep the face clear; the separate bangs provide the hairline.
    pt("hair_cap", ("north",), lambda f, x, y, w, h: hair(2)(f, x, y, w, h) if y < 7 else CLEAR)
    for part in ("crown_a", "crown_b", "crown_c", "hair_puff", "hair_puff_small"):
        pt(part, "all", hair(3))
    for part in ("bang_s", "bang_m", "bang_l", "bang_tip"):
        pt(part, "all", hair(2, True))
    for part in ("side_lock", "side_tip", "back_lock", "back_tip", "flyaway"):
        pt(part, "all", hair(2, True))
    pt("ahoge", "all", hair(3))
    pt("ahoge_tip", "all", hair(4))

    def feather(kind="secondary"):
        def fn(f, x, y, w, h):
            if f not in BROAD:
                return PAL.FEA[2 if f in SIDES else 1]
            t = y / max(1, h - 1)
            edge = x == 0 or x == w - 1
            if kind in ("covert", "covert_small", "mantle", "leg"):
                idx = 3 if t < .58 else (4 if t < .80 else 5)
            elif kind == "clip":
                idx = 5 if t < .78 else 6
            elif kind == "hip":
                idx = 2 if t < .42 else (3 if t < .76 else 5)
            else:
                idx = 1 if t < .25 else (2 if t < .52 else (3 if t < .75 else 5))
                if .60 <= t < .69:
                    idx = 4                         # broad pale finch wing bar
                if .75 <= t < .82:
                    idx = 1                         # dark band before the cream tip
            if abs(x + .5 - w / 2) < .55 and .18 < t < .82:
                idx = min(6, idx + 1)               # quiet central shaft
            if edge:
                idx = max(0, idx - 1)
            # A short stepped taper; most of the silhouette comes from geometry.
            r = h - 1 - y
            tl = 1.7 * H.D if kind not in ("covert", "covert_small") else 1.1 * H.D
            if r < tl:
                half = (w / 2) * (min(1.0, (r + 1) / tl) ** .8)
                if abs(x + .5 - w / 2) > half:
                    return CLEAR
            return PAL.FEA[idx]
        return fn

    for part in ("covert", "covert_small", "mantle"):
        pt(part, "all", feather(part))
    for part in ("secondary", "primary", "tail_feather", "tail_covert", "arm_feather", "sleeve"):
        pt(part, "all", feather(part))
    pt("clip_feather", "all", feather("clip"))
    pt("hip_feather", "all", feather("hip"))
    pt("leg_fluff", "all", feather("leg"))
    pt("leg_fluff_side", "all", feather("leg"))
    pt("wing_base", "all", lambda f, x, y, w, h: PAL.FEA[2 if y < h * .55 else 3])
    pt("tail_fluff", "all", lambda f, x, y, w, h: PAL.FEA[3 if y < h - 2 else 4])
    pt("clip_bead", "all", lambda f, x, y, w, h: GOLD[2] if x + y < w else GOLD[1])

    def cotton(f, x, y, w, h):
        if f in ("up", "down"):
            return COTTON[1]
        if y < 1:
            return PAL.FEA[4]                 # soft feathered upper hem, not a brown cage line
        if y >= h - 1:
            return COTTON[0]
        if f in SIDES:
            return COTTON[1]
        return COTTON[2 if (x // 5 + y // 6) % 4 else 3]
    pt("chest", "all", cotton)
    pt("bust", "all", cotton)
    pt("chest_fluff", "all", lambda f, x, y, w, h: CLEAR if y == h - 1 and x % 3 == 0 else PAL.FEA[5 if y > h // 2 else 4])
    pt("sash", "all", lambda f, x, y, w, h: PAL.FEA[1] if y < 2 or y >= h - 2 else PAL.FEA[2])
    pt("belt_knot", "all", lambda f, x, y, w, h: PAL.FEA[1] if x in (0, w - 1) else PAL.FEA[3])
    pt("belt_tie", "all", lambda f, x, y, w, h: PAL.FEA[2] if y < h - 2 else PAL.FEA[3])
    pt("cord", "all", lambda f, x, y, w, h: PAL.FEA[1])
    pt("pendant", "all", lambda f, x, y, w, h: GOLD[2] if f in BROAD and 1 <= x < w - 1 and 1 <= y < h - 1 else GOLD[1])

    def leaf(f, x, y, w, h):
        if f not in BROAD + SIDES:
            return LEAF[1]
        if y < 3:
            return PAL.FEA[1]
        cx = (w - 1) / 2
        idx = 2
        if abs(x - cx) < .8:
            idx = 3
        elif (y + int(abs(x - cx) * 2)) % 8 < 2:
            idx = 3
        if x in (0, w - 1):
            idx = 1
        r = h - 1 - y
        if r < 2 * H.D and abs(x - cx) > (r + 1) * w / (4 * H.D):
            return CLEAR
        return LEAF[idx]
    for part in ("leaf_s", "leaf_m", "leaf_l", "leaf_side"):
        pt(part, "all", leaf)
    pt("trim", "all", lambda f, x, y, w, h: PAL.FEA[4 if y < h // 2 else 5])
    pt("claw_a", "all", lambda f, x, y, w, h: PAL.TALON[1] if f == "up" else PAL.TALON[0])
    pt("claw_b", "all", lambda f, x, y, w, h: PAL.TALON[0])
    return pt.T


# ----------------------------------------------------------------------------- geometry
def build_model():
    m = H.Model(NAME)
    bone = m.bone
    bone("root", None, [0, 0, 0])

    # Compact torso: eight units from hips to neck, under an eight-unit head.
    body = [
        cube("hips", [-2.5, 9, -1.75]),
        cube("midriff", [-1.75, 10.1, -1.25]),
        cube("collar", [-1.75, 15.9, -1.375]),
        cube("neck", [-1, 16.4, -1]),
        cube("sash", [-2.75, 9.65, -2]),
    ]
    bone("body", "root", [0, 9, 0], body)
    # Separate upper-chest bone for delayed follow-through in the perched bounce.
    chest = [
        cube("chest", [-2.5, 12.9, -1.625]),
        cube("cord", [-1.5, 15.65, -2.15]),
        cube("pendant", [-.5, 14.8, -2.3]),
    ]
    # A single feathered wrap reads more clearly than protruding cuboids at Minecraft scale.
    for x in (-2.4, -1.2, 0, 1.2):
        chest.append(hang("chest_fluff", x - .75, 16.15, -2.25, 0, -5))
    bone("chest", "body", [0, 14, 0], chest)

    # Short leaf skirt with a side knot and feather accents.
    skirt = []
    for part, x, rz in (("leaf_m", -3, -7), ("leaf_l", -1.55, 3), ("leaf_s", -.25, -2),
                        ("leaf_l", 1, 5), ("leaf_m", 2.1, -6)):
        skirt.append(hang(part, x, 10.65, -2.55, rz, -8))
    for part, x, rz in (("leaf_m", -2.8, 4), ("leaf_l", -1.1, -3), ("leaf_m", .8, 3), ("leaf_s", 2.2, -4)):
        skirt.append(hang(part, x, 10.65, 2, rz, 8))
    for z in (-2.1, .1):
        c = cube("leaf_side", [-3.25, 5.9, z], pivot=[-3, 10.65, z + 1.25], rotation=[0, 0, -8])
        skirt += [c, mirror_cube(c)]
    skirt += [cube("belt_knot", [-3.25, 9.8, -2.5]),
              hang("belt_tie", -2.9, 10, -2.55, -8, -5), hang("belt_tie", -2.25, 10, -2.55, 8, -5)]
    for x, z, rz in ((-3.45, -.9, -14), (-3.35, .55, -10)):
        c = hang("hip_feather", x, 10.45, z, rz)
        skirt += [c, mirror_cube(c)]
    bone("skirt", "body", [0, 10.4, 0], skirt)

    # Large rounded-by-steps head, short cheek locks and an asymmetric feather clip.
    head = [cube("head", [-4, 18, -4]), cube("hair_cap", [-4.25, 18, -4.25])]
    ear = cube("ear", [-4.85, 19.8, -1.2])
    cheek = cube("cheek", [-4.35, 19.1, -3.35])
    head += [ear, mirror_cube(ear), cheek, mirror_cube(cheek)]
    # Crown clumps overlap in three directions, breaking up the box silhouette.
    for part, pos, rot in (
        ("crown_a", [-3.6, 25.7, -3.2], [-5, 0, -5]),
        ("crown_a", [-.7, 26.1, -2.5], [4, 0, 4]),
        ("crown_b", [1.6, 25.9, -1.8], [-4, 0, 9]),
        ("crown_b", [-2.8, 25.8, .8], [6, 0, -8]),
        ("crown_c", [.1, 26.3, 1.3], [8, 0, 5]),
        ("crown_c", [-4.1, 24.9, 1.8], [8, 0, -12]),
        ("crown_c", [3, 24.8, 1.5], [5, 0, 12]),
    ):
        head.append(cube(part, pos, pivot=[pos[0] + H.PARTS[part]["size"][0] / 2, pos[1], pos[2] + 1], rotation=rot))
    # Uneven bangs leave both eyes visible and form a stepped hairline.
    for part, x, top, rz in (
        ("bang_m", -4.2, 26.1, -8), ("bang_s", -2.65, 26.4, 5),
        ("bang_s", -1.25, 26.25, -4), ("bang_l", .05, 26.35, 4),
        ("bang_m", 1.45, 26.25, -5), ("bang_s", 3.15, 25.95, 8),
    ):
        head.append(hang(part, x, top, -4.85, rz, -4))
    # Overlapping side puffs round the profile; the lower locks stay separately animated.
    side = [cube("hair_puff", [-4.65, 21.6, -2.25], pivot=[-4.1, 24.5, -1], rotation=[0, 0, -5]),
            cube("hair_puff", [-4.55, 21.9, .25], pivot=[-4.1, 24.5, 1], rotation=[0, 0, -8]),
            cube("hair_puff_small", [-4.75, 23.65, -3.1], pivot=[-4.1, 25, -2], rotation=[0, 0, -10]),
            hang("side_lock", -5.05, 24.9, -2.75, -5, 0),
            hang("side_tip", -5.15, 20.1, -2.55, 7, 0),
            hang("side_lock", -5.1, 24.6, .3, -8, 3)]
    bone("hair_side_r", "head", [-4, 24.5, 0], side, mirror=True)
    head += [hang("flyaway", -5.05, 24.7, -3.25, -72, 0),
             hang("flyaway", 4.25, 25.3, -1.5, 82, 0)]
    head.append(cube("clip_bead", [4.45, 23.5, -2.25]))
    for angle, rx, dz in ((118, -8, 0), (137, 7, .35), (153, 18, .7)):
        head.append(hang("clip_feather", 4.15, 24.1, -2.35 + dz, angle, rx))
    bone("head", "body", [0, 18, 0], head)
    bone("eyelids", "head", [0, 21.5, -3.8], [cube("eyelids", [-3.25, 19.75, -3.9])])
    bone("mouth", "head", [0, 19, -3.8], [cube("mouth", [-.875, 18.25, -3.9])])

    # Back hair is a fan of separated, chunky locks rather than a continuous curtain.
    back = []
    for i, x in enumerate((-4.1, -2.55, -1.0, .55, 2.1)):
        back.append(hang("back_lock", x, 24.8 - .25 * (i % 2), 3.65 + .15 * (i % 2), (x + 1) * 2.2, 7))
        back.append(hang("back_tip", x + .25, 18.25, 4.2, (x + 1) * 3, 9))
    bone("hair_back", "head", [0, 24, 4], back)
    bone("ahoge", "head", [0, 26.3, -1], [
        cube("ahoge", [-.5, 26.2, -1.5]),
        cube("ahoge", [-.35, 27.5, -1.5], pivot=[0, 27.5, -1], rotation=[0, 0, -20]),
        cube("ahoge_tip", [.1, 28.65, -1.5], pivot=[.5, 28.7, -1], rotation=[0, 0, -50]),
    ])

    # Shorter human arms with feathered shoulders and forearm edges.
    arm = [cube("arm_up", [-4, 13.25, -.75])]
    for x, z, rz in ((-4.75, -1, -20), (-4.85, .1, -28), (-4.35, .85, -16)):
        arm.append(hang("mantle", x, 17, z, rz))
    for x, z, rz in ((-4.65, -.6, -12), (-4.8, .4, -18)):
        arm.append(hang("sleeve", x, 15.7, z, rz))
    bone("arm_r", "body", [-3.25, 16.5, 0], arm, mirror=True)
    fore = [cube("forearm", [-4, 10.5, -.75])]
    for x, z, rz in ((-4.75, .1, -14), (-4.55, .8, -8)):
        fore.append(hang("arm_feather", x, 13, z, rz))
    bone("forearm_r", "arm_r", [-3.25, 13.25, 0], fore, mirror=True)
    bone("hand_r", "forearm_r", [-3.25, 10.5, 0], [
        cube("palm", [-4, 9.25, -.5]), cube("fingers", [-4, 8.45, -.5]), cube("thumb", [-2.5, 9.4, -.25]),
    ], mirror=True)

    # Broad folded wings. Coverts make the shoulder round; long feathers form a scalloped fan.
    wing = [cube("wing_base", [-6.75, 9.5, 2.15])]
    for i, x in enumerate((-6.7, -4.8, -2.9)):
        wing.append(hang("covert_small", x, 17.1, 2.85, -8 + i * 6))
    for i, x in enumerate((-7.75, -5.8, -3.85)):
        wing.append(hang("covert", x, 15.25, 3.15, -10 + i * 6))
    for i, x in enumerate((-8.25, -6.35, -4.45, -2.55)):
        wing.append(hang("covert", x, 12.9, 3.35, -12 + i * 6))
    for i, x in enumerate((-8.75, -6.9, -5.05, -3.2)):
        wing.append(hang("secondary", x, 11.25, 2.45 + .15 * (i % 2), -15 + i * 6))
    bone("wing_r", "body", [-2.5, 16.5, 2.2], wing, mirror=True)
    primary = []
    for i, x in enumerate((-9.4, -7.65, -5.9, -4.15)):
        primary.append(hang("primary", x, 10.9, 2.85 + .18 * (i % 2), -21 + i * 8))
    bone("wing_tip_r", "wing_r", [-7.4, 10.8, 2.8], primary, mirror=True)

    # Compact tail fan, visible between the folded wings from behind.
    tail = [cube("tail_fluff", [-2, 9.3, 2.2])]
    for i, x in enumerate((-3.4, -1.85, -.3, 1.25)):
        tail.append(hang("tail_feather", x, 10.2, 3.25 + .15 * (i % 2), -16 + i * 10, 6))
    for x, rz in ((-1.9, -8), (-.15, 8)):
        tail.append(hang("tail_covert", x, 10.8, 3.85, rz, 8))
    bone("tail", "body", [0, 10.4, 2.8], tail)

    # Short digitigrade-looking legs, feather cuffs, spread toes and hooked black talons.
    cx = -1.25
    leg = [cube("thigh", [cx - 1.125, 6, -1.125]), cube("knee", [cx - 1.25, 5.65, -1.25])]
    for x, z, rz, rx in ((cx - 1.35, -1.55, -8, -10), (cx + .05, -1.5, 7, -12),
                         (cx - 1.25, 1.05, -6, 12), (cx + .1, 1, 8, 10)):
        leg.append(hang("leg_fluff", x, 7.8, z, rz, rx))
    bone("leg_r", "root", [cx, 9, 0], leg, mirror=True)
    shin = [cube("shin", [cx - .75, 1, -.75]),
            cube("shin_lower", [cx - .625, .9, -1.25], pivot=[cx, 3.6, -.6], rotation=[-10, 0, 0]),
            cube("hock", [cx - .875, 3.6, -1.05]), cube("ankle", [cx - 1, .75, -1])]
    bone("shin_r", "leg_r", [cx, 6, 0], shin, mirror=True)
    foot = [cube("heel", [cx - 1, 0, -1])]
    for dx, ry in ((-2, 14), (-.5, 0), (1, -14)):
        x = cx + dx
        rot = dict(pivot=[cx, .5, -1], rotation=[0, ry, 0]) if ry else {}
        foot += [cube("toe1", [x, 0, -3.5], **rot), cube("toe2", [x, 0, -5.5], **rot),
                 cube("claw_a", [x, .05, -7], pivot=[x + .5, .55, -5.5], rotation=[18, ry, 0]),
                 cube("claw_b", [x + .125, -.15, -8.25], pivot=[x + .5, .1, -7], rotation=[52, ry, 0])]
    foot += [cube("htoe", [cx - .5, 0, 1]), cube("claw_a", [cx - .5, .05, 2.5], pivot=[cx, .55, 2.5], rotation=[-18, 0, 0])]
    bone("foot_r", "shin_r", [cx, 1, 0], foot, mirror=True)
    return m


def player_sleep_bounce(an):
    """A cheerful perched bounce for a sleeping player's midsection; visual motion only.

    Root motion keeps the behaviour controller free to hold her world position. The crouch,
    counter-tilted head, delayed hair/tail and small wing flare sell the take-off and landing.
    """
    an.add("player_sleep_bounce", 1.4, True, H.merge({
        "root": {"position": H.track(
            (0, (0, 0, 0)), (.18, (0, -.7, 0)), (.32, (0, .6, 0)),
            (.50, (0, 2.6, 0)), (.68, (0, 1.8, 0)), (.86, (0, -.25, 0)),
            (1.0, (0, 0, 0)), (1.4, (0, 0, 0)))},
        "body": {"rotation": H.track(
            (0, (4, 0, 0)), (.18, (9, 0, 0)), (.50, (-4, 0, 0)),
            (.68, (-2, 0, 0)), (.86, (7, 0, 0)), (1.0, (4, 0, 0)), (1.4, (4, 0, 0)))},
        "chest": {"position": H.track(
            (0, (0, 0, 0)), (.18, (0, -.35, 0)), (.32, (0, .65, 0)),
            (.50, (0, -.9, 0)), (.68, (0, .95, 0)), (.86, (0, -.7, 0)),
            (1.0, (0, .35, 0)), (1.18, (0, 0, 0)), (1.4, (0, 0, 0)))},
        "head": {"rotation": H.track(
            (0, (-2, 0, 0)), (.18, (-7, 0, 0)), (.50, (5, 0, 0)),
            (.86, (-5, 0, 0)), (1.0, (-2, 0, 0)), (1.4, (-2, 0, 0)))},
        "hair_back": {"rotation": H.track(
            (0, (0, 0, 0)), (.32, (-8, 0, 0)), (.68, (12, 0, 0)),
            (.86, (-5, 0, 0)), (1.4, (0, 0, 0)))},
        "ahoge": {"rotation": H.track(
            (0, (0, 0, -5)), (.32, (0, 0, 12)), (.68, (0, 0, -14)),
            (1.0, (0, 0, 4)), (1.4, (0, 0, -5)))},
        "tail": {"rotation": H.track(
            (0, (0, 0, 0)), (.32, (-12, 0, 0)), (.68, (16, 0, 0)),
            (1.0, (0, 0, 0)), (1.4, (0, 0, 0)))},
        "skirt": {"rotation": H.track(
            (0, (0, 0, 0)), (.32, (-8, 0, 0)), (.68, (10, 0, 0)),
            (1.0, (0, 0, 0)), (1.4, (0, 0, 0)))},
        "leg_r": {"rotation": H.track(
            (0, (0, 0, 0)), (.18, (18, 0, 0)), (.50, (-8, 0, 0)),
            (.86, (12, 0, 0)), (1.0, (0, 0, 0)), (1.4, (0, 0, 0)))},
        "leg_l": {"rotation": H.track(
            (0, (0, 0, 0)), (.18, (18, 0, 0)), (.50, (-8, 0, 0)),
            (.86, (12, 0, 0)), (1.0, (0, 0, 0)), (1.4, (0, 0, 0)))},
        "shin_r": {"rotation": H.track(
            (0, (0, 0, 0)), (.18, (-30, 0, 0)), (.50, (12, 0, 0)),
            (.86, (-20, 0, 0)), (1.0, (0, 0, 0)), (1.4, (0, 0, 0)))},
        "shin_l": {"rotation": H.track(
            (0, (0, 0, 0)), (.18, (-30, 0, 0)), (.50, (12, 0, 0)),
            (.86, (-20, 0, 0)), (1.0, (0, 0, 0)), (1.4, (0, 0, 0)))},
        "foot_r": {"rotation": H.track(
            (0, (0, 0, 0)), (.18, (12, 0, 0)), (.50, (-5, 0, 0)),
            (.86, (8, 0, 0)), (1.0, (0, 0, 0)), (1.4, (0, 0, 0)))},
        "foot_l": {"rotation": H.track(
            (0, (0, 0, 0)), (.18, (12, 0, 0)), (.50, (-5, 0, 0)),
            (.86, (8, 0, 0)), (1.0, (0, 0, 0)), (1.4, (0, 0, 0)))},
    },
        H.sym("arm", H.track(
            (0, (0, 0, 8)), (.18, (-8, 0, 14)), (.50, (8, 0, -16)),
            (.86, (-5, 0, 10)), (1.0, (0, 0, 8)), (1.4, (0, 0, 8)))),
        H.sym("forearm", H.hold(-10, 0, 0)),
        H.sym("wing", H.track(
            (0, (0, 0, -3)), (.18, (0, 0, 5)), (.50, (0, 0, -20)),
            (.68, (0, 0, -12)), (.86, (0, 0, 3)), (1.0, (0, 0, -3)), (1.4, (0, 0, -3)))),
        H.sym("wing_tip", H.track(
            (0, (0, 0, 0)), (.32, (0, 0, 10)), (.68, (0, 0, -12)),
            (1.0, (0, 0, 0)), (1.4, (0, 0, 0)))),
        H.sym("hair_side", H.track(
            (0, (0, 0, 0)), (.32, (-6, 0, -5)), (.68, (10, 0, 6)),
            (1.0, (0, 0, 0)), (1.4, (0, 0, 0))))))


def animations(an):
    for fn in (H.std_idle, H.std_walk, H.std_fly, H.std_sing, H.std_bathe, player_sleep_bounce):
        fn(an)


def bounce_preview(path, model, tex, anims):
    """Render an animated GIF and a twelve-frame contact sheet without altering the model."""
    from PIL import Image, ImageDraw

    anim = anims["animations"][f"animation.{NAME}.player_sleep_bounce"]
    size, count = 360, 12
    frames = []
    for i in range(count):
        rows = H.render(model, tex, anim, i * 1.4 / count, -25, 8, size, size, 12, (44, 48, 44, 255))
        im = Image.new("RGBA", (size, size))
        im.putdata([px for row in rows for px in row])
        frames.append(im)
    frames[0].save(path, save_all=True, append_images=frames[1:], duration=1400 // count, loop=0, disposal=2)
    thumb = 240
    sheet = Image.new("RGBA", (thumb * 6, (thumb + 24) * 2), (44, 48, 44, 255))
    draw = ImageDraw.Draw(sheet)
    for i, im in enumerate(frames):
        x, y = (i % 6) * thumb, (i // 6) * (thumb + 24)
        sheet.paste(im.resize((thumb, thumb), Image.Resampling.NEAREST), (x, y + 24))
        draw.text((x + 8, y + 6), f"{i * 1.4 / count:.2f} s", fill=(235, 225, 202, 255))
    sheet.save(os.path.splitext(path)[0] + "_frames.png")
    print("bounce preview ->", path)


if __name__ == "__main__":
    declare_parts()
    H.pack()
    model = build_model().doc()
    tex = paint()
    an = H.Anims(NAME, [b["name"] for b in model["minecraft:geometry"][0]["bones"]])
    animations(an)
    anims = an.doc()
    H.run(NAME, paint, build_model, animations, head_center=(0, 21.5), body_center=(0, 12.5), scale=14)
    if "--bounce-preview" in sys.argv:
        bounce_preview(sys.argv[sys.argv.index("--bounce-preview") + 1], model, tex, anims)
