#!/usr/bin/env python3
"""Yes Steve Model (YSM) player-model pack: a high-detail harpy girl, separate from the mod.

Writes a YSM folder-format pack to ysm/harmontide_harpy/ (plus a .zip of it):
    ysm.json, models/main.json, models/arm.json, textures/<variant>.png,
    animations/main.animation.json, animations/extra.animation.json
Copy the folder (or unzip it) into .minecraft/config/yes_steve_model/custom/ and run /ysm model reload.

Built the YSM way: rounded limbs and torso from stacked 3-cube slices, separate hair locks with
cut-out tips, layered wing feathers (coverts, secondaries, two-part primaries), a pleated skirt over
a lace petticoat, hands with fingers, bird feet with talons, anime eyes with blink lids.
Texture: 8 texels per unit, painted from each texel's 3D position, so seams between cubes match.
YSM rules followed: every face's UV is whole texels and at least 1 texel (YSM 2.6 UV standard),
no semi-transparent pixels, the special group names (Head, AllHead, *HandLocator, ElytraLocator,
BackpackLocator, shoulder/waist locators, ViewLocator, ysmGlow*), arm.json = the two arms at the
root with zero rotation.

Colour variants are YSM textures the player picks in the model screen.

Usage:  python tools/gen_ysm_harpy.py [--preview]   (pure standard library)
"""
import copy
import json
import math
import os
import sys
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import harpy_lib as H  # noqa: E402  (preview renderer, png writer)

REPO = H.REPO
OUT = os.path.join(REPO, "ysm", "harmontide_harpy")
D = 8                      # texels per unit
FACES = ("north", "south", "east", "west", "up", "down")

# ============================================================================ geometry engine
BONES, BONE, CUBES = [], {}, []


def q(v):
    return max(0.125, round(v * 8) / 8)


def bone(name, parent, pivot, rot=None):
    b = dict(name=name, parent=parent, pivot=[float(v) for v in pivot], rot=list(rot) if rot else None)
    BONES.append(b)
    BONE[name] = b
    return name


def box(b, center, size, mat, rot=None, pivot=None, skip=(), **kw):
    """rot is authored [rx, ry, rz]: +rx tips a hanging part back (+z), +rz tips it toward +X; Z is negated on output"""
    size = [q(s) for s in size]
    c = dict(bone=b, origin=[center[i] - size[i] / 2 for i in range(3)], size=size, mat=mat, skip=set(skip), kw=kw)
    if rot and any(rot):
        c["pivot"] = list(pivot if pivot is not None else center)
        c["rot"] = list(rot)
    CUBES.append(c)
    return c


def R(rot):
    """authored rotation -> matrix (as GeckoLib applies it)"""
    return H.rot_matrix(rot[0], rot[1], -rot[2])


def apply(Rm, v):
    return tuple(sum(Rm[i][k] * v[k] for k in range(3)) for i in range(3))


def hang(b, top, size, mat, rot=(0, 0, 0), skip=("down",), **kw):
    """a plate/box hanging from `top` (its top centre), rotated about that point; returns the bottom-centre point"""
    h = q(size[1])
    box(b, (top[0], top[1] - h / 2, top[2]), size, mat, rot=rot, pivot=top, skip=skip, **kw)
    d = apply(R(rot), (0, -h, 0))
    return (top[0] + d[0], top[1] + d[1], top[2] + d[2])


def chain(b, top, segs, mat, ry=0, **kw):
    """segs = [(width, length, thick, rx, rz), ...] hanging one from the other; kw['t0'/'t1'] = position along the whole lock"""
    total = sum(s[1] for s in segs)
    acc = 0
    p = top
    for k, (w, l, d, rx, rz) in enumerate(segs):
        last = k == len(segs) - 1
        p = hang(b, p, (w, l, d), mat, rot=(rx, ry, rz), t0=acc / total, t1=(acc + l) / total, tip=last, **kw)
        acc += l
    return p


def rslice(b, cx, cz, y0, y1, w, d, mat, k=(0.62, 0.86), **kw):
    """one rounded slice: three boxes (wide, deep, corner) -> a stepped rounded section"""
    cy, h = (y0 + y1) / 2, y1 - y0
    box(b, (cx, cy, cz), (w, h, d * k[0]), mat, **kw)
    box(b, (cx, cy, cz), (w * k[0], h, d), mat, **kw)
    box(b, (cx, cy, cz), (w * k[1], h, d * k[1]), mat, **kw)


def blob(b, center, radii, mat, n=4, **kw):
    """an ellipsoid from n rounded slices along Y"""
    cx, cy, cz = center
    rx, ry, rz = radii
    for k in range(n):
        y0, y1 = cy - ry + 2 * ry * k / n, cy - ry + 2 * ry * (k + 1) / n
        m = ((y0 + y1) / 2 - cy) / ry
        f = math.sqrt(max(0.05, 1 - m * m))
        rslice(b, cx, cz, y0, y1, 2 * rx * f, 2 * rz * f, mat, **kw)


def rbox(b, center, size, mat, k=0.86, **kw):
    """a box with chamfered edges (three boxes)"""
    w, h, d = size
    box(b, center, (w, h * k, d * k), mat, **kw)
    box(b, center, (w * k, h, d * k), mat, **kw)
    box(b, center, (w * k, h * k, d), mat, **kw)


def rod(b, p0, p1, thick, mat, **kw):
    """a rounded bar from p0 to p1 (local +x along the bar)"""
    dx, dy, dz = (p1[i] - p0[i] for i in range(3))
    L = math.sqrt(dx * dx + dy * dy + dz * dz)
    ry = math.degrees(math.asin(-dz / L))
    rz = math.degrees(math.atan2(dy, dx))
    c = tuple((p0[i] + p1[i]) / 2 for i in range(3))
    box(b, c, (L, thick, thick * 0.62), mat, rot=(0, ry, rz), pivot=c, **kw)
    box(b, c, (L, thick * 0.62, thick), mat, rot=(0, ry, rz), pivot=c, **kw)


def mirror_all():
    """every *Right* bone gets its *Left* twin; cubes flagged sym=True are mirrored inside their own bone"""
    for b in list(BONES):
        if "Right" in b["name"]:
            nb = copy.deepcopy(b)
            nb["name"] = b["name"].replace("Right", "Left")
            if nb["parent"]:
                nb["parent"] = nb["parent"].replace("Right", "Left")
            nb["pivot"][0] = -nb["pivot"][0]
            if nb["rot"]:
                nb["rot"] = [nb["rot"][0], -nb["rot"][1], -nb["rot"][2]]
            BONES.append(nb)
            BONE[nb["name"]] = nb
    for c in list(CUBES):
        if "Right" in c["bone"] or c["kw"].get("sym"):
            m = copy.deepcopy(c)
            m["bone"] = c["bone"].replace("Right", "Left")
            m["origin"][0] = -(c["origin"][0] + c["size"][0])
            m["kw"]["sym"] = False
            m["kw"]["mirrored"] = True
            if "pivot" in c:
                m["pivot"][0] = -c["pivot"][0]
                m["rot"] = [c["rot"][0], -c["rot"][1], -c["rot"][2]]
            CUBES.append(m)


# ============================================================================ the harpy
POP = 0.3                  # eyelids slide this far out of the face to close the eyes


def build():
    BONES.clear(), BONE.clear(), CUBES.clear()
    bone("Root", None, (0, 0, 0))
    bone("LowerBody", "Root", (0, 12.5, 0.15))
    bone("UpperBody", "LowerBody", (0, 13.75, 0.15))
    bone("Head", "UpperBody", (0, 22.25, 0.1))
    bone("AllHead", "Head", (0, 22.25, 0.1))
    torso()
    head()
    hair()
    arms()
    wings()
    skirt_and_tail()
    legs()
    locators()
    mirror_all()


def torso():
    # hips (LowerBody) and waist/chest (UpperBody): rounded slices
    for y0, y1, w, d, z in ((10.875, 11.625, 5.0, 3.0, 0.15), (11.625, 12.625, 5.875, 3.625, 0.2), (12.625, 13.75, 5.75, 3.5, 0.2)):
        rslice("LowerBody", 0, z, y0, y1, w, d, "torso")
    blob("LowerBody", (-1.35, 12.0, 1.0), (1.55, 1.35, 1.05), "torso", n=3, sym=True)
    for y0, y1, w, d, z in ((13.75, 14.5, 5.0, 3.25, 0.15), (14.5, 15.375, 4.5, 3.0, 0.15), (15.375, 16.25, 4.5, 3.0, 0.15),
                            (16.25, 17.25, 4.875, 3.25, 0.15), (17.25, 18.5, 5.375, 3.5, 0.15), (18.5, 19.75, 5.75, 3.5, 0.15),
                            (19.75, 20.5, 5.625, 3.25, 0.2), (20.5, 21.0, 3.75, 2.5, 0.3)):
        rslice("UpperBody", 0, z, y0, y1, w, d, "torso")
    blob("UpperBody", (-1.2, 18.1, -1.4), (1.45, 1.35, 1.2), "torso", n=5, sym=True)          # bust
    blob("UpperBody", (-2.85, 19.8, 0.2), (1.1, 1.0, 1.1), "torso", n=3, sym=True)            # shoulder caps
    rslice("UpperBody", 0, 0.3, 20.75, 22.625, 1.875, 1.875, "torso")                         # neck
    # frill along the bottom of the top
    for k in range(16):
        th = math.radians(k * 22.5)
        top = (2.6 * math.sin(th), 16.875, 0.15 - 1.72 * math.cos(th))
        hang("UpperBody", top, (1.125, 0.625, 0.125), "frill", rot=(-35, -k * 22.5, 0))
    # bow at the neckline
    box("UpperBody", (0, 19.2, -2.55), (0.5, 0.5, 0.375), "ribbon")
    box("UpperBody", (-0.6, 19.25, -2.5), (1.0, 0.625, 0.25), "ribbon", rot=(0, 0, 18), sym=True)
    hang("UpperBody", (-0.15, 19.0, -2.62), (0.375, 1.0, 0.125), "ribbon", rot=(-8, 0, -14), sym=True)
    # choker + glowing gem
    rslice("UpperBody", 0, 0.3, 21.125, 21.625, 2.125, 2.125, "choker")
    bone("ysmGlowGem", "UpperBody", (0, 20.85, -0.95))
    box("ysmGlowGem", (0, 20.75, -0.95), (0.625, 0.625, 0.25), "gem", rot=(0, 0, 45))
    box("UpperBody", (0, 21.2, -0.9), (0.375, 0.25, 0.25), "gold")


def head():
    rbox("AllHead", (0, 25.75, -0.1), (7.25, 7.0, 6.75), "head")
    # eyelids hide just inside the face; blinking slides them out (the left one comes from mirroring)
    bone("RightEyelid", "AllHead", (-1.6, 26.75, -3.4))
    box("RightEyelid", (-1.6, 25.55, -3.4), (2.875, 2.625, 0.125), "lid", skip=("south", "east", "west", "up", "down"))
    # mouth-open plate, hidden the same way (used by extras)
    bone("Mouth", "AllHead", (0, 23.1, -3.4))
    box("Mouth", (0, 23.05, -3.4), (0.75, 0.5, 0.125), "mouth", skip=("south", "east", "west", "up", "down"))


def hair():
    # cap over the skull and a block behind it
    for y0, y1, w, d in ((26.75, 28.625, 7.875, 7.625), (28.625, 29.5, 7.625, 7.375), (29.5, 30.125, 6.875, 6.625),
                         (30.125, 30.5, 5.25, 5.0)):
        rslice("AllHead", 0, 0.05, y0, y1, w, d, "hair3d", k=(0.7, 0.9))
    # crown: locks radiating from the top of the head and falling over the dome
    for k in range(16):
        th = k * 360 / 16 + 8
        t = math.radians(th)
        top = (1.55 * math.sin(t), 30.62, 0.05 - 1.5 * math.cos(t))
        chain("AllHead", top, [(1.75, 2.5, 0.25, -47, 0), (1.625, 1.5, 0.25, -14, 0)], "lock", ry=-th,
              seed=150 + k, tone=k % 3, crown=True)
    rbox("AllHead", (0, 25.25, 3.45), (7.5, 5.75, 1.25), "hair3d")
    # bangs: pointed locks over the forehead
    xs = (-3.25, -2.45, -1.6, -0.8, 0.0, 0.8, 1.6, 2.45, 3.25)
    ls = (4.0, 3.5, 2.75, 3.25, 3.75, 3.25, 2.75, 3.5, 4.0)
    for k, (x, l) in enumerate(zip(xs, ls)):
        z = -3.82 - 0.06 * (k % 2) + 0.12 * (abs(x) / 3.25) ** 2
        chain("AllHead", (x, 29.7, z), [(1.125 if k % 2 else 1.25, l * 0.55, 0.25, -6, x * 3.5),
                                         (1.0, l * 0.45, 0.25, -2, x * 5.5)], "lock", seed=k, tone=k % 3)
    # side panels over the ears
    for k, z in enumerate((-2.4, -1.2, 0.0, 1.2, 2.4)):
        l = (5.75, 6.25, 5.5, 5.0, 4.5)[k]
        chain("AllHead", (-3.98 - 0.05 * (k % 2), 28.6, z), [(1.375, l * 0.45, 0.25, -16, 0), (1.375, l * 0.3, 0.25, 4, 0),
                                                              (1.25, l * 0.25, 0.25, 12, 0)],
              "lock", ry=90 - 6 * (2 - k), seed=10 + k, tone=(k + 1) % 3, sym=True)
    # cheek locks in front of the face edge, slanting inward -> a narrower, softer jaw line
    chain("AllHead", (-3.05, 27.6, -3.66), [(1.375, 2.75, 0.25, -4, 7), (1.125, 2.25, 0.25, 0, 16)], "lock", seed=30, tone=1, sym=True)
    # side locks framing the face, down to the chest
    bone("RightSideLock", "AllHead", (-3.35, 26.75, -2.55))
    for k, (dx, dz) in enumerate(((0, 0), (0.45, 0.5))):
        chain("RightSideLock", (-3.35 + dx, 26.75, -2.55 + dz),
              [(1.25, 3.0, 0.375, -4, -4), (1.125, 3.0, 0.375, 2, 2), (1.0, 2.5, 0.375, 6, 6)],
              "lock", ry=100 - 25 * k, seed=20 + k, tone=k)
    # long back hair: two layers of locks, flared over the wing roots
    bone("HairBack", "AllHead", (0, 25.5, 3.6))
    for layer, (rad, n, l0, tone) in enumerate(((3.75, 9, 9.25, 1), (4.15, 10, 8.5, 0))):
        for k in range(n):
            th = 110 + 140 * k / (n - 1) + (7 if layer else 0)
            if th > 250:
                continue
            t = math.radians(th)
            top = (rad * math.sin(t), 26.0 - layer * 0.5, 0.15 - rad * 0.92 * math.cos(t))
            l = l0 + (0.75 if k % 2 else 0) - 1.25 * abs(th - 180) / 70
            chain("HairBack", top, [(1.5, l * 0.38, 0.375, -16, 0), (1.375, l * 0.34, 0.375, -4, 0), (1.25, l * 0.28, 0.375, 3, 0)],
                  "lock", ry=-th, seed=40 + layer * 20 + k, tone=(k + tone) % 3)
    # ahoge
    bone("Ahoge", "AllHead", (0.4, 30.0, -0.8))
    p = hang("Ahoge", (0.4, 30.4, -0.8), (1.25, 1.5, 0.25), "lock", rot=(-172, 25, 0), skip=(), t0=0, t1=0.4, seed=90, tone=2)
    p = hang("Ahoge", p, (1.125, 1.25, 0.25), "lock", rot=(-140, 25, 0), skip=(), t0=0.4, t1=0.7, seed=91, tone=2)
    hang("Ahoge", p, (1.0, 1.25, 0.25), "lock", rot=(-95, 25, 0), t0=0.7, t1=1, tip=True, seed=92, tone=2)
    # feather tufts where ears would be (harpy "ears"), pointing back and up
    for k, (l, rx, y) in enumerate(((3.25, 118, 27.6), (2.75, 104, 27.1), (2.25, 92, 26.6))):
        hang("AllHead", (-3.95, y, 0.6), (0.875, l, 0.125), "feather", rot=(rx, 12, -18 + 6 * k), role="crest", seed=k, sym=True,
             skip=())
    # hair clip with a sea-glass bead
    box("AllHead", (-2.85, 28.1, -3.95), (1.0, 0.375, 0.25), "gold", rot=(0, 0, -20))
    box("AllHead", (-2.35, 28.3, -4.05), (0.375, 0.375, 0.25), "gem")


def arms():
    bone("RightArm", "UpperBody", (-3.55, 20.0, 0.2))
    for y0, y1, w in ((18.75, 20.125, 1.875), (17.5, 18.75, 1.75), (16.25, 17.5, 1.625)):
        rslice("RightArm", -3.9, 0.2, y0, y1, w, w, "skin")
    for k in range(4):                    # shoulder mantle feathers
        hang("RightArm", (-3.6 - 0.35 * k, 20.45 - 0.15 * k, -0.6 + 0.4 * k), (1.0, 1.75 - 0.15 * k, 0.125), "feather",
             rot=(10 + 6 * k, 90, -28), role="cov", seed=60 + k, skip=())
    bone("RightForeArm", "RightArm", (-3.9, 16.25, 0.2))
    for y0, y1, w in ((15.0, 16.375, 1.625), (13.75, 15.0, 1.5), (12.875, 13.75, 1.375)):
        rslice("RightForeArm", -3.9, 0.2, y0, y1, w, w, "skin")
    rslice("RightForeArm", -3.9, 0.2, 13.375, 14.0, 1.75, 1.75, "cuff")
    for k in range(5):                    # down feathers along the outside of the forearm
        hang("RightForeArm", (-4.55, 16.0 - 0.55 * k, 0.55 - 0.1 * k), (0.875, 2.25 - 0.15 * k, 0.125), "feather",
             rot=(22, 0, -24 + 2 * k), role="down", seed=70 + k, skip=())
    for k in range(8):                    # fluffy cuff around the wrist
        th = k * 45
        t = math.radians(th)
        hang("RightForeArm", (-3.9 + 0.85 * math.sin(t), 13.5, 0.2 - 0.85 * math.cos(t)), (0.75, 1.0, 0.125), "feather",
             rot=(-30, -th, 0), role="fluff", seed=80 + k)
    bone("RightHand", "RightForeArm", (-3.9, 12.875, 0.2))
    rbox("RightHand", (-3.95, 12.2, 0.2), (0.625, 1.375, 1.375), "skin", k=0.8)
    for k, z in enumerate((-0.32, 0.06, 0.44, 0.78)):
        l = (0.75, 0.875, 0.75, 0.625)[k]
        p = hang("RightHand", (-3.95, 11.6, z), (0.5, l, 0.375), "skin", rot=(0, 0, 8), skip=())
        hang("RightHand", p, (0.375, l * 0.8, 0.375), "skin", rot=(0, 0, 24), skip=(), nail=True)
    p = hang("RightHand", (-3.7, 12.35, -0.45), (0.375, 0.75, 0.375), "skin", rot=(-40, 0, 14), skip=())
    hang("RightHand", p, (0.375, 0.625, 0.375), "skin", rot=(-30, 0, 24), skip=(), nail=True)
    bone("RightHandLocator", "RightHand", (-3.95, 11.25, 0.2))


def wings():
    # the wing lies in a plane behind the back, angled back by the bone's rest yaw
    root = (-1.1, 19.75, 2.4)
    E, W, T = (-5.0, 21.25, 2.4), (-9.0, 20.5, 2.4), (-11.0, 19.75, 2.4)
    bone("RightWing", "UpperBody", (-1.1, 19.75, 1.85), rot=(0, 62, 16))
    bone("RightWingTip", "RightWing", E)
    rod("RightWing", root, E, 0.875, "wingarm")
    rod("RightWingTip", E, W, 0.75, "wingarm")
    rod("RightWingTip", W, T, 0.625, "wingarm")

    def along(a, b, f):
        return tuple(a[i] + (b[i] - a[i]) * f for i in range(3))

    # secondaries (+ two tertials at the body)
    n = 11
    for k in range(n):
        f = k / (n - 1)
        p = along((-1.7, 19.9, 2.4), (-8.7, 20.4, 2.4), f)
        b = "RightWing" if p[0] > -5.0 else "RightWingTip"
        l = 5.5 + 2.0 * min(1, k / 2) + 0.4 * math.sin(k * 1.7)
        z = 2.4 - 0.035 * k
        chain(b, (p[0], p[1] - 0.2, z), [(1.5, l * 0.6, 0.125, 0, 6 - 1.0 * k), (1.5, l * 0.4, 0.125, 4, 5 - 1.0 * k)],
              "feather", role="sec", wing=True, seed=100 + k)
    # primaries: a fan from the wrist to the tip
    lens = (9.0, 9.75, 10.5, 11.0, 11.25, 11.25, 11.0, 10.5, 9.75, 8.75)
    for k, l in enumerate(lens):
        p = along(W, T, k / 9)
        z = 2.05 - 0.04 * k
        chain("RightWingTip", (p[0], p[1] - 0.15, z), [(1.625, l * 0.55, 0.125, 0, -4 - 6.5 * k), (1.5, l * 0.45, 0.125, 6, -8 - 6.5 * k)],
              "feather", role="prim", wing=True, seed=120 + k)
    # coverts: greater, median, marginal rows on the upper (back) side, under-coverts in front
    rows = (("gcov", 3.25, 1.25, 0.22, 11), ("mcov", 2.0, 1.0, 0.36, 10), ("lcov", 1.25, 0.875, 0.5, 13))
    for role, l, w, dz, n in rows:
        for k in range(n):
            f = k / (n - 1)
            p = along(root, T, f) if role == "lcov" else along((-1.5, 19.95, 2.4), (-10.5, 19.95, 2.4), f)
            if role != "lcov":
                p = (p[0], along(root, E, min(1, f * 2.2))[1] if p[0] > -5 else along(E, T, (p[0] + 5) / -6)[1], p[2])
            b = "RightWing" if p[0] > -5.0 else "RightWingTip"
            rz = 4 - 22 * max(0, (-p[0] - 8.5) / 2.5)
            hang(b, (p[0], p[1] - 0.1, p[2] + dz + 0.01 * k), (w, l, 0.125), "feather", rot=(0, 0, rz), role="cov",
                 wing=True, seed=140 + k + int(dz * 100))
        # same row (shorter) on the underside
    for k in range(10):
        p = along(root, T, k / 9)
        b = "RightWing" if p[0] > -5.0 else "RightWingTip"
        hang(b, (p[0], p[1] - 0.15, 2.0), (1.125, 2.75, 0.125), "feather", rot=(0, 0, 2), role="ucov", wing=True, seed=170 + k)
    # alula at the wrist
    for k in range(3):
        hang("RightWingTip", (W[0], W[1], 2.95), (0.75, 2.25 - 0.3 * k, 0.125), "feather", rot=(0, 0, -62 - 10 * k),
             role="cov", wing=True, seed=180 + k)


def skirt_and_tail():
    bone("SkirtFront", "LowerBody", (0, 13.25, 0.15))
    bone("SkirtBack", "LowerBody", (0, 13.25, 0.15))
    rslice("LowerBody", 0, 0.2, 12.75, 13.75, 6.375, 4.25, "belt")
    box("LowerBody", (-1.3, 13.25, -2.17), (1.0, 0.75, 0.25), "gold")
    n = 18
    for layer, (rad, l1, l2, mat) in enumerate(((1.0, 2.0, 2.0, "skirt"), (0.93, 2.0, 2.375, "petti"))):
        for k in range(n):
            th = k * 360 / n + (360 / n / 2 if layer else 0)
            t = math.radians(th)
            top = (3.15 * rad * math.sin(t), 13.3, 0.2 - 2.15 * rad * math.cos(t))
            b = "SkirtFront" if math.cos(t) > 0.01 else "SkirtBack"
            chain(b, top, [(1.625, l1, 0.125, -22, 0), (1.625, l2, 0.125, -32, 0)], mat, ry=-th, shade=k % 2)
    # tail fan, coming out above the waistband at the back
    bone("Tail", "LowerBody", (0, 13.75, 2.1))
    for k in range(9):
        f = (k - 4) / 4
        l = 6.25 + 0.75 * (1 - abs(f))
        chain("Tail", (f * 1.1, 13.9, 2.1 + 0.03 * abs(k - 4)), [(1.375, l * 0.55, 0.125, 40, f * 30), (1.25, l * 0.45, 0.125, 28, f * 36)],
              "feather", role="tail", seed=200 + k)
    for k in range(5):
        f = (k - 2) / 2
        hang("Tail", (f * 0.9, 14.1, 2.3), (1.125, 2.75, 0.125), "feather", rot=(52, 0, f * 26), role="cov", seed=220 + k)


def legs():
    bone("RightLeg", "Root", (-1.6, 12.25, 0.15))
    for y0, y1, w in ((10.75, 12.25, 2.75), (9.5, 10.75, 2.625), (8.25, 9.5, 2.375), (7.25, 8.25, 2.125)):
        rslice("RightLeg", -1.6, 0.15, y0, y1, w, w, "skin")
    for ring, (y, l, r, n) in enumerate(((9.875, 2.75, 1.3, 12), (8.875, 2.0, 1.15, 10))):
        for k in range(n):
            th = k * 360 / n + ring * 15
            t = math.radians(th)
            hang("RightLeg", (-1.6 + r * math.sin(t), y, 0.15 - r * math.cos(t)), (1.0, l, 0.125), "feather",
                 rot=(-24, -th, 0), role="fluff", seed=240 + ring * 20 + k)
    bone("RightShin", "RightLeg", (-1.6, 7.25, 0.15))
    blob("RightShin", (-1.6, 7.0, 0.15), (0.95, 0.75, 0.95), "scales", n=3)
    for y0, y1, w in ((5.5, 6.875, 1.375), (3.25, 5.5, 1.25), (1.0, 3.25, 1.125)):
        rslice("RightShin", -1.6, 0.15, y0, y1, w, w, "scales")
    bone("RightFoot", "RightShin", (-1.6, 1.0, 0.15))
    blob("RightFoot", (-1.6, 0.85, 0.15), (0.75, 0.6, 0.75), "scales", n=2)
    for k, ry in enumerate((24, 0, -24)):
        base = (-1.6, 0.4, -0.15)
        Rm = R((4, ry, 0))
        d = apply(Rm, (0, 0, -1.5))
        box("RightFoot", (base[0] + d[0] / 2, base[1] + d[1] / 2, base[2] + d[2] / 2), (0.625, 0.625, 1.5), "toe",
            rot=(4, ry, 0), pivot=base)
        p = (base[0] + d[0], base[1] + d[1], base[2] + d[2])
        Rm2 = R((10, ry, 0))
        d2 = apply(Rm2, (0, 0, -1.125))
        box("RightFoot", (p[0] + d2[0] / 2, p[1] + d2[1] / 2, p[2] + d2[2] / 2), (0.5, 0.5, 1.125), "toe", rot=(10, ry, 0), pivot=p)
        p2 = (p[0] + d2[0], p[1] + d2[1], p[2] + d2[2])
        d3 = apply(R((40, ry, 0)), (0, 0, -0.75))
        box("RightFoot", (p2[0] + d3[0] / 2, p2[1] + d3[1] / 2, p2[2] + d3[2] / 2), (0.375, 0.375, 0.75), "talon",
            rot=(40, ry, 0), pivot=p2)
    base = (-1.6, 0.4, 0.5)
    box("RightFoot", (-1.6, 0.4, 1.0), (0.5, 0.5, 1.0), "toe", rot=(-6, 0, 0), pivot=base)
    box("RightFoot", (-1.6, 0.2, 1.75), (0.375, 0.375, 0.625), "talon", rot=(-35, 0, 0), pivot=(-1.6, 0.35, 1.45))


def locators():
    bone("ElytraLocator", "UpperBody", (0, 20.5, 1.9))
    bone("BackpackLocator", "UpperBody", (0, 17.5, 2.0))
    bone("RifleLocator", "UpperBody", (0, 18.0, 2.2))
    bone("RightShoulderLocator", "UpperBody", (-3.9, 20.9, 0.2))
    bone("RightWaistLocator", "LowerBody", (-3.2, 12.75, 0.2))
    bone("PistolLocator", "LowerBody", (-3.3, 12.25, 0.2))
    bone("ViewLocator", "Head", (0, 25.5, 0.1))


# ============================================================================ palettes
def hx(s):
    s = s.lstrip("#")
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), 255)


PALETTES = {
    "sparrow": dict(skin="#f6d7c3", hair=["#3b2417", "#5c3a24", "#7d5233", "#a06d45", "#c9976a"], cov="#8a5a36", sec="#6e4529",
                    prim="#4a2f1f", tipc="#dcb88e", under="#eadcc6", tail="#6e4529", fluff="#cfab84", pattern="streak",
                    mark="#33200f", eye=["#2a1408", "#8a4b16", "#e6a64a"], top="#f3ead8", skirt="#7a4a2a", trim="#c8693a",
                    belt="#4a2c1a", petti="#fbf5ea", gem="#ff9a3c", leg="#c99a86", talon="#2e2420"),
    "albatross": dict(skin="#f8e2d6", hair=["#7f8a9a", "#aab5c3", "#d3dbe4", "#eef2f6", "#ffffff"], cov="#3d4047", sec="#2c2f35",
                      prim="#1d1f24", tipc="#5a5e66", under="#f4f6f8", tail="#2c2f35", fluff="#f1f3f5", pattern="plain",
                      mark="#15171b", eye=["#14202e", "#4f7396", "#a9c9e6"], top="#ffffff", skirt="#1f2f57", trim="#c8343c",
                      belt="#1f2f57", petti="#ffffff", gem="#5fd0e8", leg="#e8c4c4", talon="#6a6f78"),
    "crow": dict(skin="#f3e0d8", hair=["#0b0b12", "#151724", "#222538", "#363b5a", "#58608a"], cov="#181a2c", sec="#121424",
                 prim="#0c0d17", tipc="#3a4470", under="#26283c", tail="#121424", fluff="#1d1f31", pattern="sheen",
                 mark="#06060a", eye=["#3a0508", "#a3121c", "#ff6a6a"], top="#1c1622", skirt="#3b1d4a", trim="#b9a3d6",
                 belt="#120e18", petti="#2a2030", gem="#c03cff", leg="#2a2a33", talon="#0a0a0e"),
    "owl": dict(skin="#f5dcc8", hair=["#46331f", "#6b4f35", "#92714c", "#b89870", "#dcc59e"], cov="#8a6a48", sec="#7a5c3e",
                prim="#6a4f35", tipc="#dcc6a0", under="#eadfc8", tail="#7a5c3e", fluff="#dac7a5", pattern="barred",
                mark="#3e2c1b", eye=["#5a2a00", "#e08a10", "#ffd040"], top="#55633e", skirt="#6b4f34", trim="#d8c29a",
                belt="#3a2a1a", petti="#efe4cc", gem="#ffc040", leg="#cdb894", talon="#30302e"),
    "flamingo": dict(skin="#f8dccf", hair=["#a83a58", "#d05a7a", "#ec86a0", "#ffb0c2", "#ffdbe4"], cov="#f49ab0", sec="#ec7f9a",
                     prim="#1d1719", tipc="#ffc4d2", under="#ff9fb6", tail="#f49ab0", fluff="#ffc6d3", pattern="plain",
                     mark="#c25a76", eye=["#5a3a00", "#e0b020", "#fff070"], top="#ffffff", skirt="#ff6f61", trim="#ffd0a0",
                     belt="#c04050", petti="#fff4f6", gem="#ff4f8a", leg="#f08aa2", talon="#3a2a2e"),
    "seagull": dict(skin="#f7e1d4", hair=["#949eaa", "#bfc7d0", "#e1e6ec", "#f5f7f9", "#ffffff"], cov="#a9b3be", sec="#9ca7b3",
                    prim="#1c1d20", tipc="#ffffff", under="#f6f8fa", tail="#fbfcfd", fluff="#f3f5f7", pattern="gull",
                    mark="#1c1d20", eye=["#4a3a00", "#d8b020", "#fff3a0"], top="#eef6ff", skirt="#4a8fd0", trim="#ffd24a",
                    belt="#2a4a70", petti="#ffffff", gem="#4ad0ff", leg="#f2c94c", talon="#3a3a3a"),
}


def prep(p):
    out = {}
    for k, v in p.items():
        if isinstance(v, str) and v.startswith("#"):
            out[k] = hx(v)
        elif isinstance(v, list):
            out[k] = [hx(c) for c in v]
        else:
            out[k] = v
    out["lash"] = shade(out["hair"][0], 0.55)
    out["blush"] = hx("#f48c8c")
    out["lip"] = hx("#c0605a")
    out["white"] = hx("#fdfdff")
    out["gold"] = [hx("#6b4a12"), hx("#b5852a"), hx("#e8c25a"), hx("#fff0b0")]
    return out


# ============================================================================ painting
def clamp(v):
    return max(0, min(255, int(round(v))))


def mix(a, b, k):
    k = max(0.0, min(1.0, k))
    return (clamp(a[0] + (b[0] - a[0]) * k), clamp(a[1] + (b[1] - a[1]) * k), clamp(a[2] + (b[2] - a[2]) * k), 255)


def shade(c, k):
    return (clamp(c[0] * k), clamp(c[1] * k), clamp(c[2] * k), 255)


def nz(a, b, s=0):
    return H.noise(int(a), int(b), s)


def ramp(cols, t):
    t = max(0.0, min(0.999, t)) * (len(cols) - 1)
    i = int(t)
    return mix(cols[i], cols[i + 1], t - i)


def light(N):
    """mild baked light: top a touch brighter, undersides darker (Minecraft adds its own on top)"""
    return 1.0 + 0.05 * N[1] - (0.10 if N[1] < -0.5 else 0)


def m_skin(p, c, f, s, t, i, j, w, h, P, N):
    k = light(N) + (nz(i, j, 3) - 0.5) * 0.03
    col = shade(p["skin"], k)
    if c["kw"].get("nail") and t > 0.55 and f in ("east", "west", "north", "south"):
        col = mix(col, p["white"], 0.35)
    return col


def m_torso(p, c, f, s, t, i, j, w, h, P, N):
    x, y, z = P
    front = z < 0.15
    # the top: a wrap from under the arms to the frill, dipping between the bust at the front
    ytop = 19.6 - (0.55 * max(0, 1 - abs(x) / 1.1) if front else 0)
    if 16.75 <= y <= ytop + 0.02:
        col = shade(p["top"], light(N) - 0.06 * (nz(i // 2, j // 2, 5)))
        if y > ytop - 0.2 or y < 16.95:
            col = mix(col, p["trim"], 0.85)
        elif (int((y - 16.75) * 8) % 6 == 3) and not front:
            col = shade(col, 0.93)
        return col
    if y < 13.0:                          # shorts under the skirt
        return shade(p["petti"], light(N) * 0.92)
    col = m_skin(p, c, f, s, t, i, j, w, h, P, N)
    if front and abs(x) < 0.09 and 14.6 < y < 14.85:
        col = shade(col, 0.8)              # navel
    if front and 14.0 < y < 16.5 and abs(x) < 0.07:
        col = shade(col, 0.96)
    return col


def eye_px(p, x, y):
    side = -1 if x < 0 else 1
    ex = (x - side * 1.6) / 1.15
    ey = (y - 25.45) / 1.25
    ox = ex * side
    if abs(ex) > 1.3 or abs(ey) > 1.35:
        return None
    ytop = 0.80 - 0.30 * ex * ex - 0.07 * ox
    ybot = -0.80 + 0.26 * ex * ex
    if abs(ex) <= 1.0 and ytop <= ey <= ytop + 0.24:
        return p["lash"]
    if 0.88 < ox < 1.24:                  # outer lash flick
        y1 = 0.80 - 0.30 - 0.07 - 0.75 * (ox - 0.88)
        if y1 - 0.05 < ey < y1 + 0.3:
            return p["lash"]
    if abs(ex) < 1.0 and ybot < ey < ytop:
        dx, dy = ex / 0.64, (ey + 0.04) / 0.9
        r = dx * dx + dy * dy
        if r < 1:
            if ((ex + 0.3) / 0.2) ** 2 + ((ey - 0.4) / 0.24) ** 2 < 1:
                return p["white"]
            if ((ex - 0.28) / 0.12) ** 2 + ((ey + 0.42) / 0.12) ** 2 < 1:
                return p["white"]
            if (ex / 0.22) ** 2 + ((ey + 0.02) / 0.4) ** 2 < 1:
                return shade(p["eye"][0], 0.55)
            k = (1 - ey) / 2
            col = ramp([p["eye"][0], p["eye"][1], p["eye"][1], p["eye"][2]], k ** 1.15)
            if r > 0.72:
                col = mix(col, p["eye"][0], 0.65)
            elif ey < -0.3 and r < 0.6:
                col = mix(col, p["white"], 0.18)
            return col
        col = p["white"]
        if ey > ytop - 0.25:
            col = mix(col, hx("#a9b4cc"), 0.55)
        return col
    if ybot - 0.13 < ey <= ybot and 0.2 < ox < 0.98:
        return mix(p["lash"], p["skin"], 0.45)
    return None


def m_head(p, c, f, s, t, i, j, w, h, P, N):
    x, y, z = P
    col = m_skin(p, c, f, s, t, i, j, w, h, P, N)
    if f != "north":
        if y < 22.6:
            col = shade(col, 0.93)
        return col
    for side in (-1, 1):                  # blush
        dx, dy = (x - side * 2.0) / 0.62, (y - 23.9) / 0.27
        r = dx * dx + dy * dy
        if r < 1:
            col = mix(col, p["blush"], 0.18 + 0.32 * (1 - r))
            if (i + j) % 3 == 0 and r < 0.6:
                col = mix(col, shade(p["blush"], 0.85), 0.6)
    e = eye_px(p, x, y) if z < -3.4 and abs(x) < 3.0 else None
    if e:
        return e
    for side in (-1, 1):                  # brows (mostly under the bangs)
        ex = (x - side * 1.55) / 0.95
        if abs(ex) < 1 and abs(y - (27.1 + 0.12 * (1 - ex * ex) - 0.06 * ex * side)) < 0.08:
            return shade(p["hair"][1], 1.0)
    ym = 23.05 + 0.13 * (x / 0.5) ** 2
    if abs(x) < 0.5 and abs(y - ym) < 0.07 and z < -3.4:
        return p["lip"]
    if abs(x) < 0.07 and abs(y - 24.05) < 0.07:
        return shade(p["skin"], 0.86)
    return col


def m_lid(p, c, f, s, t, i, j, w, h, P, N):
    x, y, z = P
    col = m_head(p, c, "east", s, t, i, j, w, h, P, N)
    side = -1 if x < 0 else 1
    ex = (x - side * 1.6) / 1.15
    if abs(ex) < 1.0 and abs(y - (25.05 + 0.3 * (1 - ex * ex))) < 0.1:
        return p["lash"]
    if 0.85 < ex * side < 1.15 and abs(y - (25.05 - 0.6 * (ex * side - 0.85))) < 0.1:
        return p["lash"]
    return col


def m_mouth(p, c, f, s, t, i, j, w, h, P, N):
    x, y, z = P
    if (x / 0.36) ** 2 + ((y - 23.0) / 0.24) ** 2 < 1:
        return hx("#7a2a32") if y > 22.92 else hx("#e87a86")
    return m_head(p, c, "east", s, t, i, j, w, h, P, N)


def m_hair3d(p, c, f, s, t, i, j, w, h, P, N):
    x, y, z = P
    hr = p["hair"]
    if N[1] > 0.5:
        a = math.atan2(x, -z)
        u = a * 9
    else:
        u = x * 2.6 if abs(N[2]) > 0.5 else z * 2.6
    base = 0.55 + 0.12 * (y - 28.0) / 2.0
    line = (int(u * 4 + nz(int(u * 4), 0, 7) * 2) % 3 == 0)
    k = base - (0.12 if line else 0) + (nz(i, j, 9) - 0.5) * 0.04
    if N[1] < 0.5 and abs(y - 28.4) < 0.22 and nz(int(u * 3), 1, 4) > 0.3:     # angel ring
        k = 0.88 - 0.1 * abs(y - 28.4) / 0.22
    if N[1] < -0.5:
        k -= 0.2
    return ramp(hr, k)


def tip_half(t, t0=0.45, shape="point"):
    if t <= t0:
        return 0.5
    u = (t - t0) / (1 - t0)
    if shape == "round":
        return 0.5 * math.sqrt(max(0, 1 - u * u))
    return 0.5 * (1 - u) ** 0.85


def m_lock(p, c, f, s, t, i, j, w, h, P, N):
    kw = c["kw"]
    hr = p["hair"]
    tt = kw.get("t0", 0) + t * (kw.get("t1", 1) - kw.get("t0", 0))
    broad = f in ("north", "south")
    if kw.get("tip") and t > 0.35:
        if f in ("east", "west"):
            return None if t > 0.5 else ramp(hr, 0.3)
        hw = tip_half(t, 0.35)
        if abs(s - 0.5) > hw:
            return None
        edge = abs(s - 0.5) > hw - 1.0 / w
    else:
        edge = i == 0 or i == w - 1
    k = 0.42 + 0.30 * min(1, tt * 2.5) + (0.08 if kw.get("tone") == 2 else -0.08 if kw.get("tone") == 0 else 0)
    if broad and 0.16 < tt < 0.26:
        k += 0.18 * (1 - abs(tt - 0.21) / 0.05)
    if (i + kw.get("seed", 0)) % 3 == 0:
        k -= 0.10
    if f == "south" or f in ("east", "west"):
        k -= 0.10
    if edge:
        k -= 0.16
    k += (nz(i, j, kw.get("seed", 0)) - 0.5) * 0.05
    return ramp(hr, k)


def feather_base(p, role, under):
    if role in ("prim",):
        base = p["prim"] if not under else mix(p["prim"], p["under"], 0.35)
    elif role in ("sec",):
        base = p["sec"] if not under else p["under"]
    elif role == "ucov":
        base = p["under"]
    elif role == "tail":
        base = p["tail"]
    elif role in ("down", "fluff"):
        base = p["fluff"]
    elif role == "crest":
        base = mix(p["hair"][2], p["cov"], 0.4)
    else:
        base = p["cov"] if not under else p["under"]
    return base


def m_feather(p, c, f, s, t, i, j, w, h, P, N):
    kw = c["kw"]
    role = kw.get("role", "cov")
    seed = kw.get("seed", 0)
    tt = kw.get("t0", 0) + t * (kw.get("t1", 1) - kw.get("t0", 0))
    whole_tip = kw.get("tip", True) if "t0" in kw else True
    shape = "round" if role in ("cov", "gcov", "ucov", "sec", "tail", "fluff", "down") else "point"
    t0 = 0.55 if role in ("prim", "sec", "tail") else 0.4
    edge = False
    if f == "up":
        pass
    elif whole_tip and t > t0:
        if f in ("east", "west"):
            return None
        hw = tip_half(t, t0, shape)
        if abs(s - 0.5) > hw:
            return None
        edge = abs(s - 0.5) > hw - 1.2 / w
    else:
        edge = f in ("north", "south") and (i == 0 or i == w - 1)
    under = (f == "north") and kw.get("wing")
    col = feather_base(p, role, under)
    pat = p["pattern"]
    if role in ("prim", "sec", "tail") and not under:
        if pat == "gull":
            if role == "prim" and 0.74 < tt < 0.86 and abs(s - 0.5) < 0.28:
                col = p["white"]
            elif role == "sec" and tt > 0.86:
                col = p["white"]
            elif role == "tail":
                pass
        elif pat == "barred" and int(tt * 9 + 0.3) % 2 == 0:
            col = mix(col, p["mark"], 0.45)
        elif pat == "sheen":
            col = mix(col, p["tipc"], 0.35 * max(0, 1 - abs(s - 0.35) * 3) * (0.5 + 0.5 * tt))
        elif pat != "gull":
            col = mix(col, p["tipc"], 0.45 * max(0, (tt - 0.82) / 0.18))
    if pat == "streak" and role in ("cov", "sec", "tail") and not under and abs(s - 0.5) < 0.18 and tt < 0.85:
        col = mix(col, p["mark"], 0.6)
    if pat == "streak" and role in ("cov", "sec") and not under and edge:
        col = p["tipc"]
        edge = False
    if pat == "barred" and role in ("cov", "fluff", "down") and (nz(i // 2, j // 2, seed) > 0.8):
        col = mix(col, p["mark"], 0.5)
    if abs(s - 0.5) < 0.6 / w and tt < 0.93 and f in ("north", "south"):
        col = shade(col, 1.15) if role not in ("fluff", "down") else col
    if (i + j * 2 + seed) % 4 == 0:
        col = shade(col, 0.95)
    if role in ("fluff", "down"):
        col = shade(col, 0.9 + 0.15 * t)
    if f == "south" and not kw.get("wing"):
        col = shade(col, 0.88)
    if edge:
        col = shade(col, 0.78)
    if f in ("east", "west", "up"):
        col = shade(col, 0.85)
    return col


def m_wingarm(p, c, f, s, t, i, j, w, h, P, N):
    col = mix(p["cov"], p["tipc"], 0.15) if f != "north" else p["under"]
    return shade(col, 0.95 + 0.08 * nz(i, j, 11))


def m_skirt(p, c, f, s, t, i, j, w, h, P, N):
    kw = c["kw"]
    tt = kw.get("t0", 0) + t * (kw.get("t1", 1) - kw.get("t0", 0))
    col = p["skirt"]
    if f in ("north", "south"):
        col = shade(col, 0.82 + 0.22 * (s if f == "north" else 1 - s))
    if kw.get("shade"):
        col = shade(col, 0.94)
    if 0.84 < tt < 0.93:
        col = mix(col, p["trim"], 0.9)
    if 0.955 < tt:
        col = mix(col, p["trim"], 0.9)
    if f == "south":
        col = shade(col, 0.72)
    if f in ("east", "west"):
        col = shade(col, 0.8)
    return col


def m_petti(p, c, f, s, t, i, j, w, h, P, N):
    kw = c["kw"]
    tt = kw.get("t0", 0) + t * (kw.get("t1", 1) - kw.get("t0", 0))
    col = p["petti"]
    if kw.get("tip") and t > 0.86:
        if f in ("east", "west"):
            return None
        u = ((i % 4) + 0.5) / 4
        if (t - 0.86) / 0.14 > math.sqrt(max(0, 1 - (2 * u - 1) ** 2)):
            return None
        if (i % 4 == 1 or i % 4 == 2) and int((t - 0.86) / 0.14 * 4) == 1:
            return None              # lace holes
    if tt > 0.7 and (i + j) % 2 == 0:
        col = shade(col, 0.93)
    if f == "south":
        col = shade(col, 0.8)
    return shade(col, 0.97)


def m_frill(p, c, f, s, t, i, j, w, h, P, N):
    if f in ("north", "south") and t > 0.6:
        u = ((i % 3) + 0.5) / 3
        if (t - 0.6) / 0.4 > math.sqrt(max(0, 1 - (2 * u - 1) ** 2)):
            return None
    if f in ("east", "west") and t > 0.6:
        return None
    col = p["petti"] if p["top"] != p["petti"] else p["trim"]
    return shade(col, 0.95 if f == "north" else 0.8)


def m_flat(key, k=1.0):
    def fn(p, c, f, s, t, i, j, w, h, P, N):
        col = p[key] if not isinstance(p[key], list) else p[key][1]
        return shade(col, (k * light(N)) + (nz(i, j, 13) - 0.5) * 0.04)
    return fn


def m_belt(p, c, f, s, t, i, j, w, h, P, N):
    col = shade(p["belt"], light(N))
    y = P[1]
    if abs(y - 12.85) < 0.07 or abs(y - 13.65) < 0.07:
        col = mix(col, p["trim"], 0.7)
    return col


def m_gold(p, c, f, s, t, i, j, w, h, P, N):
    g = p["gold"]
    k = 0.55 + 0.3 * N[1] + 0.2 * (1 - t) - 0.15 * (f in ("east", "west"))
    return ramp(g, k)


def m_gem(p, c, f, s, t, i, j, w, h, P, N):
    col = p["gem"]
    if i + j < 2:
        return mix(col, p["white"], 0.8)
    if s + t > 1.2:
        return shade(col, 0.7)
    return col


def m_scales(p, c, f, s, t, i, j, w, h, P, N):
    x, y, z = P
    col = shade(p["leg"], light(N))
    if N[2] < -0.5 or (abs(N[2]) < 0.5 and abs(N[0]) < 0.5 and z < 0):
        if (y * 2.75) % 1 < 0.2:
            col = shade(col, 0.78)
    else:
        if nz(int(y * 5), int((x + z) * 5), 17) > 0.6:
            col = shade(col, 0.88)
    return col


def m_toe(p, c, f, s, t, i, j, w, h, P, N):
    col = shade(p["leg"], light(N) * 0.97)
    if f in ("up", "east", "west") and (j if f != "up" else j) % 3 == 0:
        col = shade(col, 0.82)
    return col


def m_talon(p, c, f, s, t, i, j, w, h, P, N):
    col = p["talon"]
    if f == "up":
        col = mix(col, p["white"], 0.25)
    return col


def m_ribbon(p, c, f, s, t, i, j, w, h, P, N):
    col = shade(p["trim"], light(N) - (0.12 if f == "south" else 0))
    if c["kw"].get("tip", False) is True and t > 0.8 and f in ("north", "south") and abs(s - 0.5) < (t - 0.8) * 2.5:
        return None
    return col


def m_choker(p, c, f, s, t, i, j, w, h, P, N):
    return shade(p["belt"] if p["belt"] != p["skirt"] else p["lash"], light(N))


def m_cuff(p, c, f, s, t, i, j, w, h, P, N):
    col = shade(p["trim"], light(N))
    if abs(P[1] - 13.7) < 0.08:
        col = ramp(p["gold"], 0.7)
    return col


MATS = dict(skin=m_skin, torso=m_torso, head=m_head, lid=m_lid, mouth=m_mouth, hair3d=m_hair3d, lock=m_lock,
            feather=m_feather, wingarm=m_wingarm, skirt=m_skirt, petti=m_petti, frill=m_frill, belt=m_belt,
            gold=m_gold, gem=m_gem, scales=m_scales, toe=m_toe, talon=m_talon, ribbon=m_ribbon, choker=m_choker,
            cuff=m_cuff)


# ============================================================================ texture packing + painting
RECT = {}
TEX = 0
CLEAR = (0, 0, 0, 0)


def face_px(c, f):
    w, h, d = c["size"]
    a, b = {"north": (w, h), "south": (w, h), "east": (d, h), "west": (d, h), "up": (w, d), "down": (w, d)}[f]
    return int(round(a * D)), int(round(b * D))


def pack():
    global TEX
    RECT.clear()
    rects = []
    for n, c in enumerate(CUBES):
        for f in FACES:
            if f not in c["skip"]:
                w, h = face_px(c, f)
                rects.append(((n, f), w, h))
    rects.sort(key=lambda r: (-r[2], -r[1]))
    for size in (512, 1024, 2048):
        x = y = shelf = 0
        pos, ok = {}, True
        for key, w, h in rects:
            if x + w > size:
                x, y, shelf = 0, y + shelf + 1, 0
            pos[key] = (x, y, w, h)
            x += w + 1
            shelf = max(shelf, h)
        if y + shelf + 2 <= size:
            RECT.update(pos)
            TEX = size
            RECT["blank"] = (size - 1, size - 1, 1, 1)
            return
    raise RuntimeError("does not fit in 2048")


def texel_point(c, f, s, t):
    x0, y0, z0 = c["origin"]
    w, h, d = c["size"]
    x1, y1, z1 = x0 + w, y0 + h, z0 + d
    if f == "north":
        P, N = (x0 + s * w, y1 - t * h, z0), (0, 0, -1)
    elif f == "south":
        P, N = (x1 - s * w, y1 - t * h, z1), (0, 0, 1)
    elif f == "east":
        P, N = (x0, y1 - t * h, z1 - s * d), (-1, 0, 0)
    elif f == "west":
        P, N = (x1, y1 - t * h, z0 + s * d), (1, 0, 0)
    elif f == "up":
        P, N = (x0 + s * w, y1, z1 - t * d), (0, 1, 0)
    else:
        P, N = (x0 + s * w, y0, z0 + t * d), (0, -1, 0)
    if "rot" in c:
        Rm = R(c["rot"])
        P = H.rot_about(P, c["pivot"], Rm)
        N = apply(Rm, N)
    return P, N


def paint(pal):
    p = prep(pal)
    T = [[CLEAR] * TEX for _ in range(TEX)]
    for n, c in enumerate(CUBES):
        fn = MATS[c["mat"]]
        for f in FACES:
            if (n, f) not in RECT:
                continue
            x0, y0, w, h = RECT[(n, f)]
            for j in range(h):
                t = (j + 0.5) / h
                row = T[y0 + j]
                for i in range(w):
                    s = (i + 0.5) / w
                    P, N = texel_point(c, f, s, t)
                    col = fn(p, c, f, s, t, i, j, w, h, P, N)
                    if col is not None:
                        row[x0 + i] = col
    return T


# ============================================================================ files
def r3(v):
    return round(v, 4)


def cube_json(n, c):
    out = {"origin": [r3(v) for v in c["origin"]], "size": c["size"]}
    if "rot" in c:
        out["pivot"] = [r3(v) for v in c["pivot"]]
        out["rotation"] = [r3(c["rot"][0]), r3(c["rot"][1]), r3(-c["rot"][2])]
    uv = {}
    for f in FACES:
        x, y, w, h = RECT.get((n, f), RECT["blank"])
        uv[f] = {"uv": [x, y], "uv_size": [w, h]}
    out["uv"] = uv
    return out


def geo(ident, bone_list, rest=True):
    bones = []
    for b in bone_list:
        e = {"name": b["name"]}
        if b["parent"]:
            e["parent"] = b["parent"]
        e["pivot"] = [r3(v) for v in b["pivot"]]
        if rest and b["rot"]:
            e["rotation"] = [r3(b["rot"][0]), r3(b["rot"][1]), r3(-b["rot"][2])]
        cubes = [cube_json(n, c) for n, c in enumerate(CUBES) if c["bone"] == b["name"]]
        if cubes:
            e["cubes"] = cubes
        bones.append(e)
    return {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": ident, "texture_width": TEX, "texture_height": TEX,
                        "visible_bounds_width": 4, "visible_bounds_height": 3.5, "visible_bounds_offset": [0, 1.5, 0]},
        "bones": bones}]}


def arm_geo():
    """arm.json: the two arm chains at the root, arms not rotated (YSM wiki recipe)"""
    keep = []
    for b in BONES:
        n, chain_ = b["name"], b["name"]
        while chain_ and chain_ not in ("RightArm", "LeftArm"):
            chain_ = BONE[chain_]["parent"]
        if chain_:
            nb = dict(b)
            if n in ("RightArm", "LeftArm"):
                nb["parent"] = None
                nb["rot"] = None
            keep.append(nb)
    return geo("geometry.harmontide_harpy.arm", keep)


# ============================================================================ animations
def key(v):
    return f"{v:.4f}".rstrip("0").rstrip(".") if "." in f"{v:.4f}" else str(v)


def osc(L, fn, n=8):
    return {(L * k / n): fn(2 * math.pi * k / n) for k in range(n + 1)}


def const(v):
    return {0.0: v}


def mirror_track(tr, kind):
    if kind == "rotation":
        return {t: [v[0], -v[1], -v[2]] for t, v in tr.items()}
    if kind == "position":
        return {t: [-v[0], v[1], v[2]] for t, v in tr.items()}
    return dict(tr)


class Anim:
    def __init__(self, length, loop=True):
        self.L, self.loop, self.b = length, loop, {}

    def set(self, bone_, kind, tr, mirror=True):
        if not isinstance(tr, dict):
            tr = const(list(tr))
        self.b.setdefault(bone_, {})[kind] = {t: list(v) for t, v in tr.items()}
        if mirror and "Right" in bone_:
            lb = bone_.replace("Right", "Left")
            if kind not in self.b.get(lb, {}):
                self.b.setdefault(lb, {})[kind] = mirror_track(tr, kind)
        return self

    def rot(self, bone_, tr, mirror=True):
        return self.set(bone_, "rotation", tr, mirror)

    def pos(self, bone_, tr, mirror=True):
        return self.set(bone_, "position", tr, mirror)

    def json(self):
        out = {"loop": True if self.loop is True else self.loop, "animation_length": self.L, "bones": {}}
        for bn, chans in self.b.items():
            e = {}
            for kind, tr in chans.items():
                vals = {}
                for t, v in sorted(tr.items()):
                    v = [r3(v[0]), r3(v[1]), r3(-v[2] if kind == "rotation" else v[2])]
                    vals[key(t)] = v
                e[kind] = vals
            out["bones"][bn] = e
        return out


EYES_SHUT = [0, 0, -POP]


def closed_eyes(a):
    a.pos("RightEyelid", const(EYES_SHUT))
    return a


def anims():
    A = {}
    # ---------------- idle: breathing, wings settle
    a = Anim(4.0)
    a.rot("UpperBody", osc(4, lambda p: [-1.2 * math.sin(p), 0, 0]))
    a.rot("Head", osc(4, lambda p: [1.0 * math.sin(p), 0, 1.5 * math.sin(p / 1)]), mirror=False)
    a.rot("RightArm", osc(4, lambda p: [0, 0, -3 - 1.2 * math.sin(p)]))
    a.rot("RightForeArm", const([-8, 0, 0]))
    a.rot("RightWing", osc(4, lambda p: [0, 2 * math.sin(p), 2 * math.sin(p)]))
    a.rot("RightWingTip", osc(4, lambda p: [0, 0, 2 * math.sin(p + 0.8)]))
    A["idle"] = a
    # ---------------- walk / run
    for name, L, leg, arm, lean, bob in (("walk", 1.0, 28, 20, 2, 0.35), ("run", 0.6, 42, 38, 9, 0.6)):
        a = Anim(L)
        a.rot("RightLeg", osc(L, lambda p, g=leg: [-g * math.sin(p), 0, 0]), mirror=False)
        a.rot("LeftLeg", osc(L, lambda p, g=leg: [g * math.sin(p), 0, 0]), mirror=False)
        a.rot("RightShin", osc(L, lambda p, g=leg: [8 + g * 0.8 * max(0, math.sin(p + 1.2)), 0, 0]), mirror=False)
        a.rot("LeftShin", osc(L, lambda p, g=leg: [8 + g * 0.8 * max(0, -math.sin(p + 1.2)), 0, 0]), mirror=False)
        a.rot("RightFoot", osc(L, lambda p, g=leg: [-6 + g * 0.4 * math.sin(p), 0, 0]), mirror=False)
        a.rot("LeftFoot", osc(L, lambda p, g=leg: [-6 - g * 0.4 * math.sin(p), 0, 0]), mirror=False)
        a.rot("RightArm", osc(L, lambda p, g=arm: [g * math.sin(p), 0, -5]), mirror=False)
        a.rot("LeftArm", osc(L, lambda p, g=arm: [-g * math.sin(p), 0, 5]), mirror=False)
        a.rot("RightForeArm", const([-14 - lean, 0, 0]))
        a.rot("LowerBody", osc(L, lambda p: [0, 5 * math.sin(p), 0]), mirror=False)
        a.rot("UpperBody", osc(L, lambda p, l=lean: [l, -7 * math.sin(p), 0]), mirror=False)
        a.rot("Head", osc(L, lambda p, l=lean: [-l, 2 * math.sin(p), 0]), mirror=False)
        a.pos("Root", osc(L, lambda p, b=bob: [0, b * abs(math.cos(p)), 0], n=8), mirror=False)
        a.rot("SkirtFront", osc(L, lambda p, g=leg: [-4 - g * 0.3 * abs(math.sin(p)), 0, 0]), mirror=False)
        a.rot("SkirtBack", osc(L, lambda p, g=leg: [4 + g * 0.15 * abs(math.sin(p)), 0, 0]), mirror=False)
        a.rot("RightWing", osc(L, lambda p, l=lean: [0, -l, 3 * math.sin(2 * p)]))
        A[name] = a
    # ---------------- sneak
    for name in ("sneak", "sneaking"):
        L = 1.2
        a = Anim(L)
        mv = 14 if name == "sneak" else 0
        a.rot("LowerBody", const([10, 0, 0]), mirror=False)
        a.rot("UpperBody", const([16, 0, 0]), mirror=False)
        a.rot("Head", const([-24, 0, 0]), mirror=False)
        a.pos("Root", const([0, -1.2, 0]), mirror=False)
        a.rot("RightLeg", osc(L, lambda p: [-22 - mv * math.sin(p), 0, 0]), mirror=False)
        a.rot("LeftLeg", osc(L, lambda p: [-22 + mv * math.sin(p), 0, 0]), mirror=False)
        a.rot("RightShin", const([30, 0, 0]))
        a.rot("RightFoot", const([-8, 0, 0]))
        a.rot("RightArm", const([-18, 0, -8]))
        a.rot("RightForeArm", const([-30, 0, 0]))
        a.rot("RightWing", const([0, 22, -6]))
        a.rot("SkirtFront", const([-14, 0, 0]), mirror=False)
        A[name] = a
    # ---------------- creative flight / jump / elytra
    a = Anim(1.0)
    a.rot("RightWing", osc(1, lambda p: [0, -50, -18 - 30 * math.sin(p)], n=12))
    a.rot("RightWingTip", osc(1, lambda p: [0, 0, -8 - 18 * math.sin(p - 0.9)], n=12))
    a.rot("UpperBody", const([8, 0, 0]), mirror=False)
    a.rot("Head", const([-8, 0, 0]), mirror=False)
    a.rot("RightLeg", const([18, 0, -3]))
    a.rot("RightShin", const([35, 0, 0]))
    a.rot("RightFoot", const([30, 0, 0]))
    a.rot("RightArm", osc(1, lambda p: [8, 0, -14 - 6 * math.sin(p)], n=12))
    a.rot("RightForeArm", const([-20, 0, 0]))
    a.pos("Root", osc(1, lambda p: [0, 0.6 * math.sin(p + 1.5), 0], n=12), mirror=False)
    a.rot("SkirtFront", const([-6, 0, 0]), mirror=False)
    a.rot("SkirtBack", const([12, 0, 0]), mirror=False)
    A["fly"] = a
    a = Anim(0.6)
    a.rot("RightWing", osc(0.6, lambda p: [0, -44, -34 - 14 * math.sin(p)]))
    a.rot("RightWingTip", const([0, 0, -12]))
    a.rot("RightLeg", const([-26, 0, 0]))
    a.rot("RightShin", const([48, 0, 0]))
    a.rot("RightArm", const([-10, 0, -22]))
    a.rot("SkirtFront", const([-16, 0, 0]), mirror=False)
    a.rot("SkirtBack", const([14, 0, 0]), mirror=False)
    A["jump"] = a
    a = Anim(2.0)
    a.rot("RightWing", osc(2, lambda p: [0, -58, -14 - 3 * math.sin(p)]))
    a.rot("RightWingTip", osc(2, lambda p: [0, 0, -4 - 3 * math.sin(p - 0.6)]))
    a.rot("RightArm", const([24, 0, -10]))
    a.rot("RightLeg", const([8, 0, -2]))
    a.rot("RightShin", const([16, 0, 0]))
    a.rot("RightFoot", const([28, 0, 0]))
    a.rot("SkirtBack", const([18, 0, 0]), mirror=False)
    A["elytra_fly"] = a
    # ---------------- swimming
    a = Anim(1.4)
    a.rot("RightArm", osc(1.4, lambda p: [-150 + 25 * math.sin(p), 0, -20 - 35 * max(0, math.sin(p))]))
    a.rot("RightLeg", osc(1.4, lambda p: [14 * math.sin(p * 2), 0, 0]), mirror=False)
    a.rot("LeftLeg", osc(1.4, lambda p: [-14 * math.sin(p * 2), 0, 0]), mirror=False)
    a.rot("RightWing", const([0, 50, 6]))
    a.rot("Head", const([-30, 0, 0]), mirror=False)
    A["swim"] = a
    a = Anim(2.0)
    a.rot("RightArm", osc(2, lambda p: [-20 * math.sin(p), 0, -30 + 8 * math.cos(p)]))
    a.rot("RightLeg", osc(2, lambda p: [-18 * math.sin(p), 0, 0]), mirror=False)
    a.rot("LeftLeg", osc(2, lambda p: [18 * math.sin(p), 0, 0]), mirror=False)
    a.rot("RightShin", const([22, 0, 0]))
    a.rot("RightWing", osc(2, lambda p: [0, 10, -10 - 8 * math.sin(p)]))
    A["swim_stand"] = a
    a = Anim(0.5)
    a.rot("RightArm", const([-170, 0, -6]))
    a.rot("RightWing", const([0, 60, 4]))
    a.rot("RightLeg", const([4, 0, 2]))
    A["riptide"] = a
    # ---------------- sitting / riding
    for name in ("sit", "ride", "ride_pig", "boat"):
        a = Anim(2.0)
        a.rot("RightLeg", const([-82, 8, -6]))
        a.rot("RightShin", const([78, 0, 0]))
        a.rot("RightFoot", const([4, 0, 0]))
        a.rot("SkirtFront", const([-62, 0, 0]), mirror=False)
        a.rot("SkirtBack", const([10, 0, 0]), mirror=False)
        if name == "boat":
            a.rot("RightArm", osc(2, lambda p: [-50 - 30 * math.sin(p), 0, -4]))
        else:
            a.rot("RightArm", const([-24, 0, 8]))
            a.rot("RightForeArm", const([-30, 0, 0]))
        a.rot("RightWing", osc(2, lambda p: [0, 6, 4 + 1.5 * math.sin(p)]))
        A[name] = a
    # ---------------- sleeping
    a = Anim(4.0)
    closed_eyes(a)
    a.rot("RightArm", const([-6, 0, -6]))
    a.rot("RightForeArm", const([-24, 0, 0]))
    a.rot("Head", const([6, 0, 10]), mirror=False)
    a.rot("RightWing", const([0, 30, 14]))
    a.rot("UpperBody", osc(4, lambda p: [-1.5 * math.sin(p), 0, 0]), mirror=False)
    A["sleep"] = a
    # ---------------- climbing / ladders
    for name, mv in (("climb", 1), ("climbing", 0), ("ladder_up", 1), ("ladder_down", -1), ("ladder_stillness", 0)):
        L = 1.0
        a = Anim(L)
        a.rot("RightArm", osc(L, lambda p, m=mv: [-150 + 20 * m * math.sin(p), 0, -6]), mirror=False)
        a.rot("LeftArm", osc(L, lambda p, m=mv: [-150 - 20 * m * math.sin(p), 0, 6]), mirror=False)
        a.rot("RightLeg", osc(L, lambda p, m=mv: [-20 - 22 * m * math.sin(p), 0, 0]), mirror=False)
        a.rot("LeftLeg", osc(L, lambda p, m=mv: [-20 + 22 * m * math.sin(p), 0, 0]), mirror=False)
        a.rot("RightShin", const([30, 0, 0]))
        a.rot("RightWing", const([0, 30, 4]))
        a.rot("Head", const([-12, 0, 0]), mirror=False)
        A[name] = a
    # ---------------- one-shots: hurt, death, swings, use
    a = Anim(0.4, loop=False)
    a.rot("UpperBody", {0: [0, 0, 0], 0.08: [-9, 0, 0], 0.4: [0, 0, 0]}, mirror=False)
    a.rot("Head", {0: [0, 0, 0], 0.08: [8, 0, 4], 0.4: [0, 0, 0]}, mirror=False)
    a.rot("RightWing", {0: [0, 0, 0], 0.08: [0, -14, -22], 0.4: [0, 0, 0]})
    a.pos("RightEyelid", {0: [0, 0, 0], 0.04: EYES_SHUT, 0.3: EYES_SHUT, 0.34: [0, 0, 0]})
    A["attacked"] = a
    a = Anim(1.0, loop="hold_on_last_frame")
    a.pos("RightEyelid", {0: [0, 0, 0], 0.1: EYES_SHUT, 1.0: EYES_SHUT})
    a.rot("UpperBody", {0: [0, 0, 0], 0.6: [24, 0, 0], 1.0: [24, 0, 0]}, mirror=False)
    a.rot("Head", {0: [0, 0, 0], 0.6: [18, 0, 10], 1.0: [18, 0, 10]}, mirror=False)
    a.rot("RightWing", {0: [0, 0, 0], 0.6: [0, 40, 30], 1.0: [0, 40, 30]})
    a.rot("RightArm", {0: [0, 0, 0], 0.6: [10, 0, -12], 1.0: [10, 0, -12]})
    A["death"] = a
    for name, arm in (("swing_hand", "RightArm"), ("swing_offhand", "LeftArm")):
        sgn = 1 if arm == "RightArm" else -1
        a = Anim(0.3, loop=False)
        a.rot(arm, {0: [0, 0, 0], 0.08: [-115, 0, -10 * sgn], 0.2: [18, 0, 4 * sgn], 0.3: [0, 0, 0]}, mirror=False)
        a.rot("UpperBody", {0: [0, 0, 0], 0.08: [0, 12 * sgn, 0], 0.2: [0, -10 * sgn, 0], 0.3: [0, 0, 0]}, mirror=False)
        A[name] = a
    for name, arm, fore in (("use_mainhand", "RightArm", "RightForeArm"), ("use_offhand", "LeftArm", "LeftForeArm")):
        sgn = 1 if arm == "RightArm" else -1
        a = Anim(1.0)
        a.rot(arm, const([-55, 0, 8 * sgn]), mirror=False)
        a.rot(fore, const([-28, 0, 0]), mirror=False)
        A[name] = a
    return A


def parallel(A):
    # parallel0: blinking (only the eyelids, so main animations are never overridden)
    a = Anim(4.4)
    tr = {0: [0, 0, 0], 3.4: [0, 0, 0], 3.45: EYES_SHUT, 3.58: EYES_SHUT, 3.63: [0, 0, 0], 4.4: [0, 0, 0]}
    a.pos("RightEyelid", tr)
    A["parallel0"] = a
    # parallel1: hair, ahoge and tail sway (bones no main animation touches)
    a = Anim(3.0)
    a.rot("HairBack", osc(3, lambda p: [2.5 + 2.0 * math.sin(p), 0, 1.0 * math.sin(p + 1)], n=8), mirror=False)
    a.rot("RightSideLock", osc(3, lambda p: [0, 0, -2 - 2 * math.sin(p + 0.5)]))
    a.rot("Ahoge", osc(3, lambda p: [4 * math.sin(p * 2), 0, 6 * math.sin(p)], n=8), mirror=False)
    a.rot("Tail", osc(3, lambda p: [3 * math.sin(p), 0, 4 * math.sin(p + 0.7)], n=8), mirror=False)
    A["parallel1"] = a


EXTRAS = (("extra0", "Wave"), ("extra1", "Spread wings"), ("extra2", "Dance"), ("extra3", "Shy"), ("extra4", "Preen"),
          ("extra5", "Sit down"))


def extras():
    A = {}
    a = Anim(1.2)
    a.rot("RightArm", osc(1.2, lambda p: [-10, 0, -150 + 4 * math.sin(p)]), mirror=False)
    a.rot("RightForeArm", osc(1.2, lambda p: [0, 0, 22 * math.sin(p)]), mirror=False)
    a.rot("Head", osc(1.2, lambda p: [0, 0, 6]), mirror=False)
    a.pos("Mouth", const([0, 0, -POP]), mirror=False)
    A["extra0"] = a
    a = Anim(3.0, loop="hold_on_last_frame")
    a.rot("RightWing", {0: [0, 0, 0], 1.0: [0, -62, -40], 1.6: [0, -66, -46], 2.2: [0, -62, -40], 3.0: [0, -64, -42]})
    a.rot("RightWingTip", {0: [0, 0, 0], 1.0: [0, 0, -14], 1.6: [0, 0, -20], 3.0: [0, 0, -16]})
    a.rot("RightArm", {0: [0, 0, 0], 1.0: [0, 0, -38], 3.0: [0, 0, -36]})
    a.rot("Head", {0: [0, 0, 0], 1.0: [-12, 0, 0], 3.0: [-10, 0, 0]}, mirror=False)
    a.rot("UpperBody", {0: [0, 0, 0], 1.0: [-6, 0, 0], 3.0: [-5, 0, 0]}, mirror=False)
    A["extra1"] = a
    L = 2.0
    a = Anim(L)
    a.rot("LowerBody", osc(L, lambda p: [0, 0, 7 * math.sin(p)]), mirror=False)
    a.rot("UpperBody", osc(L, lambda p: [0, 8 * math.sin(p), -9 * math.sin(p)]), mirror=False)
    a.rot("Head", osc(L, lambda p: [0, 0, 8 * math.sin(p + 0.6)]), mirror=False)
    a.rot("RightArm", osc(L, lambda p: [-30 - 30 * math.sin(p), 0, -40 - 40 * max(0, math.sin(p))]), mirror=False)
    a.rot("LeftArm", osc(L, lambda p: [-30 + 30 * math.sin(p), 0, 40 + 40 * max(0, -math.sin(p))]), mirror=False)
    a.rot("RightWing", osc(L, lambda p: [0, -36, -22 - 12 * math.sin(4 * p)], n=16))
    a.pos("Root", osc(L, lambda p: [0, 0.5 * abs(math.sin(2 * p)), 0], n=16), mirror=False)
    a.rot("RightLeg", osc(L, lambda p: [-8 * max(0, math.sin(p)), 0, -4 * max(0, math.sin(p))]), mirror=False)
    a.rot("LeftLeg", osc(L, lambda p: [-8 * max(0, -math.sin(p)), 0, 4 * max(0, -math.sin(p))]), mirror=False)
    a.rot("SkirtFront", osc(L, lambda p: [-6 * abs(math.sin(p)), 0, 0]), mirror=False)
    a.pos("Mouth", const([0, 0, -POP]), mirror=False)
    A["extra2"] = a
    a = Anim(3.0)
    a.rot("RightArm", const([-22, 0, 22]))
    a.rot("RightForeArm", const([-46, 0, 0]))
    a.rot("Head", osc(3, lambda p: [12, 0, 9 + 2 * math.sin(p)]), mirror=False)
    a.rot("RightWing", osc(3, lambda p: [0, 34, 2 * math.sin(p)]))
    a.rot("UpperBody", osc(3, lambda p: [4, 4 * math.sin(p), 0]), mirror=False)
    a.rot("RightLeg", const([0, 0, 3]))
    A["extra3"] = a
    a = Anim(3.0)
    a.rot("Head", osc(3, lambda p: [18, -48, 6 + 3 * math.sin(2 * p)]), mirror=False)
    a.rot("LeftWing", osc(3, lambda p: [0, 10, -10]), mirror=False)
    a.rot("RightWing", osc(3, lambda p: [0, -40, -28 - 4 * math.sin(2 * p)]), mirror=False)
    a.rot("RightWingTip", const([0, 0, -10]), mirror=False)
    a.rot("LeftArm", const([-48, 0, -32]), mirror=False)
    a.rot("LeftForeArm", const([-40, 0, 0]), mirror=False)
    a.pos("RightEyelid", const(EYES_SHUT))
    A["extra4"] = a
    a = Anim(3.0)
    a.pos("Root", const([0, -6.3, 0]), mirror=False)
    a.rot("RightLeg", const([-84, 22, -22]))
    a.rot("RightShin", const([118, 0, 0]))
    a.rot("RightFoot", const([-30, 0, 0]))
    a.rot("SkirtFront", const([-66, 0, 0]), mirror=False)
    a.rot("SkirtBack", const([-6, 0, 0]), mirror=False)
    a.rot("RightArm", const([-34, 0, 14]))
    a.rot("RightForeArm", const([-36, 0, 0]))
    a.rot("RightWing", osc(3, lambda p: [0, 10, 12 + 2 * math.sin(p)]))
    a.rot("Head", osc(3, lambda p: [4, 0, 5 * math.sin(p)]), mirror=False)
    A["extra5"] = a
    return A


# ============================================================================ preview
def preview_pose(anim_json):
    """the renderer ignores rest rotations, so fold them into the pose"""
    bones = copy.deepcopy(anim_json["bones"]) if anim_json else {}
    for b in BONES:
        if not b["rot"]:
            continue
        r = [b["rot"][0], b["rot"][1], -b["rot"][2]]
        tr = bones.setdefault(b["name"], {}).get("rotation")
        if tr is None:
            bones[b["name"]]["rotation"] = {"0": r}
        else:
            bones[b["name"]]["rotation"] = {t: [v[i] + r[i] for i in range(3)] for t, v in tr.items()}
    return {"bones": bones}


def merge_anims(*js):
    out = {"bones": {}}
    for j in js:
        if j:
            for bn, ch in j["bones"].items():
                out["bones"].setdefault(bn, {}).update(ch)
    return out


def render_sheet(path, model, tex, views, W=420, Hh=460, scale=12.5, center=None):
    bg = (40, 44, 48, 255)
    cols = min(4, len(views))
    rows = (len(views) + cols - 1) // cols
    sheet = [[bg] * (W * cols) for _ in range(Hh * rows)]
    for n, (an, tm, yaw, pitch) in enumerate(views):
        im = H.render(model, tex, preview_pose(an), tm, yaw, pitch, W, Hh, scale, bg, center=center)
        ox, oy = (n % cols) * W, (n // cols) * Hh
        for y in range(Hh):
            sheet[oy + y][ox:ox + W] = im[y]
    H.write_png(path, sheet)


# ============================================================================ main
def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=1, ensure_ascii=False)
        fh.write("\n")


def main():
    build()
    pack()
    main_geo = geo("geometry.harmontide_harpy", BONES)
    arm = arm_geo()
    A = anims()
    parallel(A)
    X = extras()
    main_anim = {"format_version": "1.8.0", "animations": {n: a.json() for n, a in A.items()}}
    extra_anim = {"format_version": "1.8.0", "animations": {n: a.json() for n, a in X.items()}}
    write_json(os.path.join(OUT, "models", "main.json"), main_geo)
    write_json(os.path.join(OUT, "models", "arm.json"), arm)
    write_json(os.path.join(OUT, "animations", "main.animation.json"), main_anim)
    write_json(os.path.join(OUT, "animations", "extra.animation.json"), extra_anim)
    only = [a for a in sys.argv[1:] if a in PALETTES]
    texs = {}
    for name, pal in PALETTES.items():
        if only and name not in only:
            continue
        texs[name] = paint(pal)
        H.write_png(os.path.join(OUT, "textures", name + ".png"), texs[name])
    ysm = {
        "spec": 2,
        "metadata": {
            "name": "Harmontide Harpy",
            "tips": "A harpy girl from the Harmontide mod. Six plumages in the texture list; wheel animations: "
                    + ", ".join(n for _, n in EXTRAS) + ".",
            "license": {"type": "All Rights Reserved", "desc": "Made for the Harmontide mod."},
            "authors": [{"name": "Linkyoshi90", "role": "model"}],
        },
        "properties": {
            "preview_animation": "idle",
            "default_texture": "sparrow",
            "extra_animation": {k: v for k, v in EXTRAS},
        },
        "files": {
            "player": {
                "model": {"main": "models/main.json", "arm": "models/arm.json"},
                "animation": {"main": "animations/main.animation.json", "extra": "animations/extra.animation.json"},
                "texture": [f"textures/{n}.png" for n in PALETTES],
            }
        },
    }
    write_json(os.path.join(OUT, "ysm.json"), ysm)
    zpath = OUT + ".zip"
    with zipfile.ZipFile(zpath, "w", zipfile.ZIP_DEFLATED) as z:
        for dp, _, fs in os.walk(OUT):
            for fn in fs:
                full = os.path.join(dp, fn)
                z.write(full, os.path.relpath(full, os.path.dirname(OUT)))
    n_cubes = len(CUBES)
    used = sum(w * h for k, (x, y, w, h) in RECT.items() if k != "blank")
    print(f"harmontide_harpy: {len(BONES)} bones / {n_cubes} cubes, texture {TEX}x{TEX} ({used / TEX / TEX:.0%} used), "
          f"{len(A)} main + {len(X)} extra animations -> {OUT}")

    if "--preview" in sys.argv:
        tex = texs.get("sparrow") or next(iter(texs.values()))
        prev = os.path.join(REPO, "previews")
        mj = {n: a.json() for n, a in A.items()}
        xj = {n: a.json() for n, a in X.items()}
        idle = merge_anims(mj["idle"])
        views = [(idle, 0, 0, 0), (idle, 0, -35, 10), (idle, 0, 90, 0), (idle, 0, 180, 0),
                 (mj["walk"], 0.25, -35, 10), (mj["fly"], 0.25, -25, 15), (mj["fly"], 0.75, 160, 10), (xj["extra1"], 3.0, 200, 5)]
        render_sheet(os.path.join(prev, "ysm_harpy_preview.png"), main_geo, tex, views)
        blink = merge_anims(mj["idle"], {"bones": {"RightEyelid": {"position": {"0": EYES_SHUT}},
                                                   "LeftEyelid": {"position": {"0": EYES_SHUT}}}})
        render_sheet(os.path.join(prev, "ysm_harpy_face.png"), main_geo, tex,
                     [(idle, 0, 0, 0), (idle, 0, -30, 6), (blink, 0, 0, 0), (xj["extra0"], 0.3, -20, 4)],
                     W=420, Hh=420, scale=40, center=(0, 25.6))
        render_sheet(os.path.join(prev, "ysm_harpy_extras.png"), main_geo, tex,
                     [(xj["extra0"], 0.3, -25, 8), (xj["extra2"], 0.5, -30, 8), (xj["extra3"], 0.5, -25, 8), (xj["extra5"], 0, -35, 12),
                      (mj["sit"], 0, -60, 8), (mj["sneak"], 0.3, -70, 8), (mj["run"], 0.15, -70, 8), (mj["swing_hand"], 0.08, -40, 8)])
        if len(texs) > 1:
            views = [(idle, 0, -30, 8)] * len(texs)
            bg = (40, 44, 48, 255)
            W, Hh = 300, 440
            sheet = [[bg] * (W * len(texs)) for _ in range(Hh)]
            for n, (name, tx) in enumerate(texs.items()):
                im = H.render(main_geo, tx, preview_pose(idle), 0, -30, 8, W, Hh, 12, bg)
                for y in range(Hh):
                    sheet[y][n * W:(n + 1) * W] = im[y]
            H.write_png(os.path.join(prev, "ysm_harpy_variants.png"), sheet)
        H.write_png(os.path.join(prev, "ysm_harpy_texture.png"), tex)
        print("previews -> previews/ysm_harpy_*.png")


if __name__ == "__main__":
    main()
