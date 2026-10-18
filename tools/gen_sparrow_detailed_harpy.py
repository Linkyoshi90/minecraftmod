#!/usr/bin/env python3
"""Sparrow Harpy, detailed model - a denser alternative to gen_sparrow_harpy.py, closer to the sparrow concept sheet.

    python tools/gen_sparrow_detailed_harpy.py                 # write geometry + textures
    python tools/gen_sparrow_detailed_harpy.py --preview x.png # also render preview sheets (x.png, x_face.png, x_body.png)
    python tools/gen_sparrow_detailed_harpy.py --lineup x.png  # also render all colour variants side by side

Same skeleton (bone names and pivots) as the plain sparrow, so it reuses sparrow_harpy.animation.json and only adds
static detail parented to the animated bones. Only geo/entity/sparrow_harpy_detailed.geo.json and
textures/entity/sparrow_harpy_detailed[_<variant>].png are written; the renderer picks this model when the client
config option `detailedSparrowModel` is on.

What it adds over the plain sparrow:
  * fuller, messier chestnut hair: temple puffs, a crown layer, flyaway locks, a second long back layer, front locks
  * a fan of three cream feathers clipped above the left ear
  * arm-wings: shoulder covert caps, feather sleeves on the upper arm and long feathers along the forearm
  * denser wings and tail with sparrow-patterned feathers (dark centre, chestnut rim, cream tip, pale wing bar)
  * a feathered back patch with a cream heart, a belt knot with ties, feather tassels on the hips
  * shaggy feather cuffs at the knees, scuted shins, spread toes with long two-part black talons
"""
import json
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import gen_sparrow_harpy as S
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

NAME = "sparrow_harpy_detailed"
SIDES = ("east", "west")


def house_pal():
    """the concept sheet's richer, darker chestnut hair; everything else stays the sparrow default"""
    pal = H.Pal()
    pal.HAIR = ramp([rgb(50, 27, 19), rgb(82, 45, 29), rgb(116, 66, 39), rgb(152, 94, 55), rgb(188, 128, 75), rgb(220, 168, 108)])
    pal.BROW = pal.HAIR[1]
    pal.hair_gain = 0.88
    return pal


PAL = house_pal()


def declare_parts():
    H.standard_parts()
    P = H.part
    P("back_cover", 4.5, 5.5, .5)                                  # bigger feathered back patch
    # hair volume
    P("hair_puff", 2, 4.5, 6.5)
    P("bang_f", 1.5, 3.5, .5)
    P("strand_x1", 1.5, 4.5, .5)                                    # flyaway locks
    P("strand_x2", 1, 3, .5)
    P("strand_l3", 1.5, 11.5, 1)
    P("hfeather_l", 1.5, 5.5, .5)
    P("bead", .75, .75, .75)
    # outfit
    P("knot", 1.25, 1.25, .75)
    P("tie", .5, 2.5, .25)
    P("hip_fea", 1, 3.5, .5)
    # arm-wings
    P("shoulder_fea", 1.75, 3, 1.75)
    P("sleeve", 1.5, 6, .5)
    P("arm_prim", 1.5, 9, .5)
    # wings and tail
    P("dcov", 2, 3.5, .5)
    P("dsec", 1.5, 9.5, .5)
    P("dprim", 1.5, 11, .5)
    P("dtail", 1.5, 9.5, .5)
    P("tail_cov", 1.5, 4, .5)
    # legs
    P("knee_fea", 1.25, 3.5, .5)
    P("knee_fea_s", .5, 3.5, 1.25)
    P("knee", 2.25, 1.25, 2.25)
    P("shin_d", 1.75, 6.75, 1.75)
    P("scute", 1, 6, .25)
    P("claw_a", 1, .75, 1.5)
    P("claw_b", .75, .5, 1.25)


# ----------------------------------------------------------------------------- texture
def taper_clear(x, y, w, h, tl):
    """pointed tip over the last tl texel rows"""
    r = h - 1 - y
    if r < tl:
        half = (w / 2.0) * (min(1.0, (r + 1) / tl) ** 0.85)
        if abs(x + 0.5 - w / 2.0) > half:
            return True
    return False


def sp_feather(pal, broad=BROAD, dark=1, rim=3, tip=5, bar=None, tip_frac=0.20):
    """a sparrow feather: dark centre, chestnut rim, cream tip; optional pale bar at the given height (0..1)"""
    FEA = pal.FEA

    def fn(f, x, y, w, h):
        if f not in broad:
            return FEA[rim - 1] if f not in ("up", "down") else FEA[dark]
        if taper_clear(x, y, w, h, 2.2 * H.D):
            return CLEAR
        t = y / max(1, h - 1)
        n = (noise(x, y, 71) - 0.5) * 0.08
        cx = (w - 1) / 2.0
        dx = abs(x - cx) / (w / 2.0)
        if t + n > 1 - tip_frac:
            idx = tip - (1 if (t + n < 1 - tip_frac * 0.55 or dx > 0.75) else 0)
            if x == int(cx + 0.5) and t < 0.9:
                idx -= 1
            return FEA[min(len(FEA) - 1, idx)]
        if bar is not None and abs(t + n - bar) < 0.05:
            return FEA[5]
        if t + n > 1 - tip_frac - 0.06:
            return FEA[rim + 1]                                        # soft step into the tip
        idx = dark if dx < 0.34 - 0.14 * t else (rim - 1 if dx < 0.5 else rim)
        if x == int(cx + 0.5) and dx < 0.45:
            idx = dark + 1                                             # shaft
        elif idx == rim and (y + int(abs(x - cx) * 2)) % 5 == 0:
            idx -= 1                                                   # barbs
        if x == 0 or x == w - 1:
            idx = max(0, idx - 1)
        return FEA[idx]
    return fn


def fluff_chunk(pal, base=0.45, tip=0.82):
    """chunky block of small feathers: scalloped rows, jagged lighter hem"""
    FEA = pal.FEA

    def fn(f, x, y, w, h):
        if f in ("up", "down"):
            return pick(FEA, base - 0.1, x, y)
        if y >= h - 2 and noise(x, y, 75) > 0.5:
            return CLEAR
        row = y // 3
        sx = (x + (row % 2) * 2) % 4
        t = base + 0.25 * (y / h) + (noise(x, y, 77) - 0.5) * 0.12
        if y % 3 == 2 and sx in (1, 2):
            t = tip                                                    # pale feather edge
        elif y % 3 == 0 and sx == 0:
            t -= 0.18
        return pick(FEA, t, x, y)
    return fn


def paint(pal=PAL, LEAF=S.LEAF, GOLD=S.GOLD):
    T = S.paint(pal, LEAF, GOLD)
    pt = H.Painter()
    pt.T = T
    FEA, HAIR, LEG, SKIN = pal.FEA, pal.HAIR, pal.LEG, pal.SKIN

    # ---- hair volume
    def hair_t(x, y, h, seed):
        sid = x // 2
        return (pal.streak[sid % 7] * 0.8 + (y / max(1, h - 1)) * 0.25 + (noise(x, y, seed) - 0.5) * 0.12) * pal.hair_gain + pal.hair_off

    def puff(f, x, y, w, h):
        if f == "up":
            return pick(HAIR, hair_t(x, y, h, 81) + 0.12, x, y)
        if f == "down":
            return HAIR[1]
        if y >= h - 3 and (x // 2 + (y == h - 1)) % 2 == 0:
            return CLEAR                                               # jagged lower edge
        return pick(HAIR, hair_t(x, y, h, 83), x, y)
    pt("hair_puff", "all", puff)

    def top(f, x, y, w, h):
        if f == "up":
            on_part = abs(x - w // 2 - (y - h // 2) // 4) == 0         # parting line
            return HAIR[1] if on_part else pick(HAIR, 0.50 + 0.18 * (noise(x // 2, y, 85) > 0.6) + (noise(x, y, 87) - 0.5) * 0.12, x, y)
        if f == "down":
            return HAIR[1]
        return pick(HAIR, 0.62 + (noise(x, y, 89) - 0.5) * 0.15, x, y)
    pt("hair_top", "all", top)

    pt("hfeather_l", "all", H.feather_fn(pal, 6, 6, 5, None, False, False))
    pt("bead", "all", lambda f, x, y, w, h: GOLD[2] if (x + y) % 3 == 0 else GOLD[1])

    # ---- back patch: fluffy brown with a cream heart between the shoulder blades
    def back(f, x, y, w, h):
        if f != "south":
            return pick(FEA, 0.35, x, y)
        u, v = (x + 0.5 - w / 2) / (w * 0.30), (y + 0.5 - h * 0.42) / (h * 0.24)
        v = -v
        heart = (u * u + v * v - 1) ** 3 - u * u * v * v * v <= 0
        if heart:
            return pick(FEA, 0.86 + (noise(x, y, 91) - 0.5) * 0.1, x, y)
        if y >= h - 2 and noise(x, y, 93) > 0.5:
            return CLEAR
        row = y // 3
        t = 0.30 + 0.25 * y / h + (0.16 if ((x + (row % 2) * 2) % 4 == 1 and y % 3 == 2) else 0) + (noise(x, y, 95) - 0.5) * 0.1
        return pick(FEA, t, x, y)
    pt("back_cover", "all", back)

    # ---- outfit bits
    pt("knot", "all", lambda f, x, y, w, h: FEA[1] if (x == w // 2 or y == h // 2) else FEA[2])
    pt("tie", "all", lambda f, x, y, w, h: CLEAR if (y == h - 1 and x % 2) else (FEA[2] if y % 3 else FEA[1]))
    pt("hip_fea", "all", sp_feather(pal, dark=2, rim=4, tip=5, tip_frac=0.35))

    # ---- arm-wings, wings, tail
    pt("shoulder_fea", "all", fluff_chunk(pal, 0.30, 0.78))
    pt("sleeve", "all", sp_feather(pal, dark=1, rim=3, tip=5, tip_frac=0.22))
    pt("arm_prim", "all", sp_feather(pal, dark=1, rim=3, tip=5, tip_frac=0.20))
    pt("dcov", "all", sp_feather(pal, dark=1, rim=3, tip=5, tip_frac=0.30))
    pt("dsec", "all", sp_feather(pal, dark=1, rim=3, tip=5, tip_frac=0.18))
    pt("dprim", "all", sp_feather(pal, dark=0, rim=2, tip=5, tip_frac=0.14))
    pt("dtail", "all", sp_feather(pal, dark=1, rim=3, tip=4, tip_frac=0.16))
    pt("tail_cov", "all", sp_feather(pal, dark=2, rim=3, tip=5, tip_frac=0.35))

    # ---- legs: knee cuffs, scuted shins, talons
    pt("knee_fea", "all", sp_feather(pal, dark=2, rim=3, tip=5, tip_frac=0.40))
    pt("knee_fea_s", "all", sp_feather(pal, broad=SIDES, dark=2, rim=3, tip=5, tip_frac=0.40))
    pt("knee", "all", lambda f, x, y, w, h: pick(LEG, 0.35 + (noise(x, y, 97) - 0.5) * 0.2, x, y))

    def shin(f, x, y, w, h):
        if f in ("up", "down"):
            return LEG[1]
        row, off = y // 3, (y // 3 % 2) * 1.5
        t = 0.50 + (noise(x, y, 99) - 0.5) * 0.12
        if y % 3 == 2:
            t = 0.18
        elif (x + off) % 3 < 1:
            t = 0.80
        if f in SIDES and (x == 0 or x == w - 1):
            t -= 0.12
        return pick(LEG, t, x, y)
    pt("shin_d", "all", shin)

    def scute(f, x, y, w, h):
        if f != "north":
            return LEG[1]
        if y % 4 == 3:
            return LEG[0]                                              # plate seam
        t = 0.62 + 0.25 * (y % 4 == 0) - (0.15 if x in (0, w - 1) else 0)
        return pick(LEG, t, x, y)
    pt("scute", "all", scute)
    pt("claw_a", "all", lambda f, x, y, w, h: pal.TALON[1] if (f in ("up",) or (f in SIDES and y == 0)) else pal.TALON[0])
    pt("claw_b", "all", lambda f, x, y, w, h: pal.TALON[1] if f == "up" and x == 1 else pal.TALON[0])
    return pt.T


# ----------------------------------------------------------------------------- geometry
def build_model():
    base = S.build_model()                          # the plain sparrow; reused bone by bone
    bones = {b["name"]: b for b in base.bones}
    m = H.Model(NAME)
    bone = m.bone

    def keep(name, drop=(), add=()):
        """copy a plain-sparrow bone (pivot, parent), dropping cubes by UV-identical part and adding new ones"""
        b = bones[name]
        cubes = [c for c in b.get("cubes", []) if not any(c["uv"] == cube(p, [0, 0, 0])["uv"] for p in drop)]
        return b["pivot"], b.get("parent"), cubes + list(add)

    bone("root", None, [0, 0, 0])

    # ---- torso: plain sparrow + bigger back patch + more bandeau trim
    pv, par, body = keep("body", drop=("back_cover", "trim"))
    body.append(cube("back_cover", [-2.25, 14.6, 1.625]))
    for i, x in enumerate((-2.75, -2.0, -1.25, -0.5, 0.25, 1.0, 1.75)):
        body.append(hang("trim", x, 16.15, -2.05 - 0.05 * (i % 2), 8 if i % 2 else -8, -4))
    bone("body", par, pv, body)

    # ---- loincloth: plain strips + belt knot and ties + feather tassels on the hips
    pv, par, sk = keep("skirt", drop=("tassel",))
    sk += [cube("knot", [-2.9, 12.85, -2.55]),
           hang("tie", -2.6, 13.1, -2.6, -6, -6), hang("tie", -2.0, 13.1, -2.6, 9, -6)]
    hips = [(-3.45, -1.6, -16), (-3.55, -0.5, -11), (-3.45, 0.6, -18), (-3.3, 1.5, -9)]
    for x, z, rz in hips:
        c = hang("hip_fea", x, 13.5, z, rz)
        sk += [c, mirror_cube(c)]
    bone("skirt", par, pv, sk)

    # ---- head: plain head + volume, extra bangs, flyaway locks, feather clip
    pv, par, head = keep("head", drop=("hfeather",))
    puff = cube("hair_puff", [-6.0, 23.6, -2.6], pivot=[-4.5, 28, 0], rotation=[0, 0, -6])
    head += [puff, mirror_cube(puff)]
    for x, rz in ((-2.5, -9), (-0.7, 5), (0.9, -6), (2.4, 10)):
        head.append(hang("bang_f", x, 28.7, -5.5, rz, -6))
    # flyaway locks (right side; mirrored with small differences so it does not look stamped)
    locks = [("strand_x1", -5.4, 27.4, -1.2, -104, 0), ("strand_x2", -5.6, 25.9, 1.0, -96, 0),
             ("strand_x2", -5.0, 28.0, 2.6, -122, 10)]
    for i, (p, x, y, z, rz, rx) in enumerate(locks):
        c = hang(p, x, y, z, rz, rx)
        head.append(c)
        if i != 1:
            head.append(mirror_cube(c))
    # feather clip above the left ear (+X): three cream feathers fanning up and back from a gold bead
    head.append(cube("bead", [4.55, 26.3, -2.0]))
    for ang, rx, dz in ((118, -8, 0.0), (134, 6, 0.35), (152, 20, 0.7)):
        head.append(hang("hfeather_l", 4.3, 26.8, -2.0 + dz, ang, rx))
    bone("head", par, pv, head)

    for n in ("eyelids", "mouth"):
        pv, par, c = keep(n)
        bone(n, par, pv, c)

    # side hair: plain + a long lock falling in front of each shoulder
    pv, par, side = keep("hair_side_r", add=[hang("strand_l3", -5.3, 25.6, -2.9, -3, -5)])
    bone("hair_side_r", par, pv, side, mirror=True)

    # back hair: plain layer + a longer, flared layer underneath
    pv, par, back = keep("hair_back")
    for i in range(6):
        x = -3.6 + 1.3 * i - 0.75
        back.append(hang("strand_l3", x, 26.2, 4.55 - 0.15 * (i % 2), (x + 0.75) * 3.2, 14 + 3 * (i % 2)))
    bone("hair_back", par, pv, back)

    pv, par, c = keep("ahoge")
    bone("ahoge", par, pv, c)

    # ---- arm-wings: shoulder covert cap + feather sleeve on the upper arm, long feathers along the forearm
    pv, par, arm = keep("arm_r")
    for x, z, rz, rx in ((-4.6, -1.2, -24, -12), (-4.8, -0.4, -30, 0), (-4.5, 0.4, -22, 12)):
        arm.append(hang("shoulder_fea", x, 20.1, z, rz, rx))
    for x, z, rz in ((-4.9, -0.55, -16), (-4.6, 0.0, -10), (-5.1, 0.5, -20), (-4.3, 0.95, -6)):
        arm.append(hang("sleeve", x, 18.6, z, rz))
    bone("arm_r", par, pv, arm, mirror=True)

    pv, par, fore = keep("forearm_r")
    for x, z, rz in ((-5.0, 0.2, -14), (-4.6, 0.55, -8), (-5.3, 0.9, -19), (-4.2, 1.2, -3)):
        fore.append(hang("arm_prim", x, 15.6, z, rz))
    bone("forearm_r", par, pv, fore, mirror=True)

    pv, par, c = keep("hand_r")
    bone("hand_r", par, pv, c, mirror=True)

    # ---- wings: scapular base, three covert rows with a pale bar, dense secondaries, long primaries
    pv, par, _ = keep("wing_r")
    w = [cube("wing_base", [-7, 10, 2.3])]
    rows = [(19.6, (-7.25, -5.5, -3.75)), (17.0, (-8.75, -7.0, -5.25, -3.5)), (14.4, (-9.75, -8.0, -6.25, -4.5))]
    for r, (ytop, xs) in enumerate(rows):
        for i, x in enumerate(xs):
            w.append(hang("dcov", x, ytop, 3.3 + 0.2 * r + 0.05 * (i % 2), -4 + 2 * i))
    for i in range(7):
        x = -10.0 + 1.05 * i
        w.append(hang("dsec", x, 13.2, 2.35 + 0.2 * (i % 2), -11 + 1.8 * i))
    bone("wing_r", par, pv, w, mirror=True)
    pv, par, _ = keep("wing_tip_r")
    pr = []
    for i in range(7):
        x = -11.0 + 1.1 * i
        pr.append(hang("dprim", x, 12.8, 2.85 + 0.2 * (i % 2), -20 + 3 * i))
    bone("wing_tip_r", par, pv, pr, mirror=True)

    # ---- tail: seven sparrow-patterned feathers under three short coverts
    pv, par, _ = keep("tail")
    tail = [cube("tail_fluff", [-1.75, 12.2, 2.2])]
    for i in range(7):
        x = -4.2 + 1.2 * i
        tail.append(hang("dtail", x, 12.8, 3.4 + 0.2 * (i % 2), -21 + 7 * i, 6))
    for i, x in enumerate((-2.2, -0.75, 0.7)):
        tail.append(hang("tail_cov", x, 13.3, 3.95, -10 + 10 * i, 10))
    bone("tail", par, pv, tail)

    # ---- legs: skin thigh, shaggy feather cuff at the knee, scuted shin, spread toes with long talons
    cx = -1.25
    pv, par, leg = keep("leg_r")
    for x, rz, rx in ((cx - 1.1, -6, -18), (cx - 0.1, 4, -16)):           # front
        leg.append(hang("knee_fea", x, 10.2, -1.55, rz, rx))
    for x, rz, rx in ((cx - 0.9, -5, 18), (cx + 0.1, 6, 20)):             # back
        leg.append(hang("knee_fea", x, 10.2, 1.05, rz, rx))
    for z, rx in ((-1.0, -8), (0.1, 8)):                                  # outer / inner side
        leg.append(hang("knee_fea_s", cx - 1.6, 10.2, z, -18, rx))
        leg.append(hang("knee_fea_s", cx + 1.1, 10.2, z, 14, rx))
    bone("leg_r", par, pv, leg, mirror=True)

    pv, par, _ = keep("shin_r")
    shin = [cube("knee", [cx - 1.125, 8.0, -1.125]), cube("shin_d", [cx - .875, 1.75, -.875]),
            cube("scute", [cx - .5, 2.25, -1.1]), cube("ankle", [cx - 1, 1, -1])]
    bone("shin_r", par, pv, shin, mirror=True)

    pv, par, _ = keep("foot_r")
    foot = [cube("heel", [cx - 1, 0, -1])]
    for dx, ry in ((-2.0, 14), (-0.5, 0), (1.0, -14)):
        x = cx + dx
        rot = dict(pivot=[cx, 0.5, -1], rotation=[0, ry, 0]) if ry else {}
        foot += [cube("toe1", [x, 0, -3.5], **rot), cube("toe2", [x, 0, -5.5], **rot)]
        # two-part talon hooking down from the toe tip; claw_b starts where claw_a's centre line ends
        a_len, a_ang, b_ang = 1.5, 18, 52
        tip_y = 0.55 - a_len * math.sin(math.radians(a_ang))
        tip_z = -5.5 - a_len * math.cos(math.radians(a_ang))
        foot.append(cube("claw_a", [x, 0.175, -7.0], pivot=[x + .5, .55, -5.5], rotation=[a_ang, ry, 0]))
        foot.append(cube("claw_b", [x + .125, tip_y - .25, tip_z - 1.25], pivot=[x + .5, tip_y, tip_z], rotation=[b_ang, ry, 0]))
        if ry:                                   # toes splay about the heel; move the claws with them
            for c in foot[-2:]:
                c["origin"], c["pivot"] = _yaw(c["origin"], c["size"], c["pivot"], ry, (cx, -1))
    foot += [cube("htoe", [cx - .5, 0, 1]),
             cube("claw_a", [cx - .5, 0.175, 2.5], pivot=[cx, .55, 2.5], rotation=[-18, 0, 0])]
    bone("foot_r", par, pv, foot, mirror=True)
    return m


def _yaw(origin, size, pivot, ry, about):
    """shift a cube (origin + own pivot) as if swung by ry degrees around a vertical axis at about=(x, z)"""
    # Blockbench Y sense: the written value is used as is for symmetric Y here, and rot_matrix negates it
    a = math.radians(-ry)
    px, pz = pivot[0] - about[0], pivot[2] - about[1]
    nx, nz = px * math.cos(a) + pz * math.sin(a), -px * math.sin(a) + pz * math.cos(a)
    dx, dz = nx - px, nz - pz
    return ([round(origin[0] + dx, 3), origin[1], round(origin[2] + dz, 3)],
            [round(pivot[0] + dx, 3), pivot[1], round(pivot[2] + dz, 3)])


# ----------------------------------------------------------------------------- output
def anims_doc(model):
    an = H.Anims(S.NAME, [b["name"] for b in model["minecraft:geometry"][0]["bones"]])
    S.animations(an)
    return an.doc()


def main():
    declare_parts()
    H.pack()
    tex_dir = os.path.join(H.REPO, "src", "main", "resources", "assets", "monstergirls", "textures", "entity")
    geo = os.path.join(H.REPO, "src", "main", "resources", "assets", "monstergirls", "geo", "entity", NAME + ".geo.json")
    model = build_model().doc()
    texes = {"house": paint()}
    H.write_png(os.path.join(tex_dir, NAME + ".png"), texes["house"])
    for v, (pal, leaf) in S.VARIANTS.items():
        texes[v] = paint(pal, leaf)
        H.write_png(os.path.join(tex_dir, f"{NAME}_{v}.png"), texes[v])
    with open(geo, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(model, fh, indent=2)
        fh.write("\n")
    bones = model["minecraft:geometry"][0]["bones"]
    used = sum(w * h for (_, _, w, h) in H.RECT.values())
    print(f"{NAME}: texture {H.TEX}x{H.TEX} ({used / H.TEX / H.TEX:.0%} used), {len(bones)} bones / "
          f"{sum(len(b.get('cubes', [])) for b in bones)} cubes, {len(S.VARIANTS)} colour variants; animations: {S.NAME}")
    anims = anims_doc(model)
    if "--preview" in sys.argv:
        out = sys.argv[sys.argv.index("--preview") + 1]
        H.preview(out, S.NAME, model, texes["house"], anims)
        H.write_png(os.path.splitext(out)[0] + "_texture.png", H.upscale(texes["house"], 2))
        print("preview ->", out)
    if "--lineup" in sys.argv:
        lineup(sys.argv[sys.argv.index("--lineup") + 1], model, anims, texes)


def lineup(path, model, anims, texes):
    A = anims["animations"]
    g = lambda n: A[f"animation.{S.NAME}.{n}"]
    bg, W = (44, 48, 44, 255), 360
    rows = [(g("idle"), 0, -20, 6, 10, None), (g("fly"), 0.4, -30, 10, 10, None), (g("idle"), 0, -15, 0, 30, (0, 22.5))]
    sheet = [[bg] * (W * len(texes)) for _ in range(W * len(rows))]
    for c, tex in enumerate(texes.values()):
        for r, (anim, tm, yaw, pitch, scale, center) in enumerate(rows):
            im = H.render(model, tex, anim, tm, yaw, pitch, W, W, scale, bg, center=center)
            for y in range(W):
                sheet[r * W + y][c * W:(c + 1) * W] = im[y]
    H.write_png(path, sheet)
    print("lineup ->", path)


if __name__ == "__main__":
    main()
