"""Body parts the families added on 2026-10-20 share (jackalopes to flora), on top of girl_lib. girl_lib stays as it was
(its families still build byte for byte); the new family scripts import this as well.

    tail(r, name, parent, start, parts)          a chain of boxes straight back from `start`; bones name, name_2, ...
    horns(r, kind, hy, z)                         a `horns` bone on the head (bull, buffalo, devil, greater, dragon,
                                                  unicorn, kannae, ram, antlers); declare horn_parts(kind) first
    wings(r, kind, parent, y, z, scale)           wing_r / wing_l (+ wing2_r / wing2_l for insects); declare wing_parts(kind)
    chain(r, name, parent, pivot, parts, rx, ry)  a jointed limb (tentacle, neck) hanging from `pivot`, tilted by rx and
                                                  turned by ry; every joint is its own bone (name, name_b, name_c, ...)
    flap(...), sway(...)                          animation helpers for wings and chains

Bones whose names start with horns / antlers / ear_ / hair are left out of the in-game height (girl_lib.model_height).
"""
import math

import girl_lib as G
import harpy_lib as H
from harpy_lib import cube, pick, noise, CLEAR

S = G.S


# ----------------------------------------------------------------------------- tails
def tail(r, name, parent, start, parts, rx=0.0):
    """boxes straight back (+Z) from start = (x, y, z), each its own bone; the droop and curl come from the animations
    (or from rx, a tilt baked into every box). Returns the bone names."""
    x, y, z = start
    names = []
    p_bone = parent
    for i, p in enumerate(parts):
        w, h, d = S(p)
        bn = name if i == 0 else f"{name}_{i + 1}"
        c = cube(p, [x - w / 2, y - h / 2, z]) if not rx else G.rot_cube(p, [x - w / 2, y - h / 2, z], [x, y, z], rx, 0, 0)
        r.add(bn, p_bone, [x, y, z], [c])
        names.append(bn)
        p_bone = bn
        if rx:
            end = H.rot_about((x, y, z + d - 0.25), (x, y, z), H.rot_matrix(rx, 0, 0))
            x, y, z = round(end[0], 3), round(end[1], 3), round(end[2], 3)
        else:
            z += d - 0.25
    return names


# ----------------------------------------------------------------------------- jointed chains (tentacles, necks)
def chain(r, name, parent, pivot, parts, rx=0.0, ry=0.0, bend=0.0):
    """a limb that hangs down from `pivot`: each part is a box extending along -Y, tilted out by rx (degrees, towards
    -Z at ry=0) and turned by ry about the vertical; every next joint tilts `bend` degrees further. Returns bone names."""
    names = []
    p_bone = parent
    px, py, pz = pivot
    tilt = rx
    for i, p in enumerate(parts):
        w, h, d = S(p)
        bn = name if i == 0 else f"{name}_{'bcdefgh'[i - 1]}"
        piv = [round(px, 3), round(py, 3), round(pz, 3)]
        c = G.rot_cube(p, [px - w / 2, py - h, pz - d / 2], piv, tilt, ry, 0)
        r.add(bn, p_bone, piv, [c])
        names.append(bn)
        end = H.rot_about((px, py - h + 0.25, pz), piv, H.rot_matrix(tilt, ry, 0))
        px, py, pz = end
        p_bone = bn
        tilt += bend
    return names


# ----------------------------------------------------------------------------- horns
HORN_KINDS = {
    "bull": [("horn_a", 3.0, 1.25, 1.25), ("horn_b", 1.0, 2.5, 1.0)],
    "bull_big": [("horn_a", 4.5, 1.5, 1.5), ("horn_b", 1.25, 3.25, 1.25)],
    "buffalo": [("horn_a", 4.0, 1.5, 1.5), ("horn_b", 1.25, 1.25, 3.0)],
    "devil": [("horn_a", 1.0, 2.25, 1.0), ("horn_b", .75, 1.25, .75)],
    "greater": [("horn_a", 1.5, 3.5, 1.5), ("horn_b", 1.25, 1.25, 3.5), ("horn_c", .75, 2.5, .75)],
    "dragon": [("horn_a", 1.25, 1.25, 4.5), ("horn_b", .75, .75, 3.0)],
    "kannae": [("horn_a", 1.0, 1.0, 2.5), ("horn_b", .75, .75, 1.5)],
    "unicorn": [("horn_a", 1.0, 5.5, 1.0), ("horn_b", .5, 1.5, .5)],
    "ram": [("horn_a", 1.25, 2.5, 1.25), ("horn_b", 1.25, 1.25, 2.5), ("horn_c", 1.0, 2.0, 1.0)],
    "antlers": [("antler_a", .75, 5, .75), ("antler_b", .75, 2.5, .75), ("antler_c", .5, 2, .5)],
}


def horn_parts(kind):
    return list(HORN_KINDS.get(kind, []))


def horns(r, kind, hy, z, name="horns", y_off=0.0):
    """a horns bone on the head; the boxes are mirrored left/right"""
    if kind not in HORN_KINDS:
        return
    top = hy + 8.2 + y_off
    cs = []
    for sg in (-1, 1):
        x = sg * 2.6
        if kind in ("bull", "bull_big"):
            aw = S("horn_a")[0]
            ox = -4.2 if sg < 0 else 4.2 - 0.0
            a = cube("horn_a", [ox - aw if sg < 0 else ox, hy + 6.4, -1.0 + z], pivot=[ox, hy + 6.4, z], rotation=[0, 0, 10 * sg])
            bx = ox - aw + 0.1 if sg < 0 else ox + aw - 1.1
            b = cube("horn_b", [bx, hy + 7.2, -0.9 + z], pivot=[bx + 0.5, hy + 7.2, z], rotation=[-10, 0, -18 * sg])
            cs += [a, b]
        elif kind == "buffalo":
            aw = S("horn_a")[0]
            ox = -4.2 if sg < 0 else 4.2
            a = cube("horn_a", [ox - aw if sg < 0 else ox, hy + 6.6, -0.6 + z], pivot=[ox, hy + 6.6, z], rotation=[0, 12 * sg, -6 * sg])
            bx = ox - aw if sg < 0 else ox + aw - 1.25
            b = cube("horn_b", [bx, hy + 6.0, 0.4 + z], pivot=[bx + 0.6, hy + 6.6, 0.6 + z], rotation=[30, 0, 0])
            cs += [a, b]
        elif kind == "devil":
            a = G.rot_cube("horn_a", [x - 0.5, top - 0.6, -2.0 + z], [x, top - 0.6, -1.5 + z], -12, 0, -14 * sg)
            b = G.rot_cube("horn_b", [x + sg * 0.6 - 0.375, top + 1.4, -1.6 + z], [x + sg * 0.6, top + 1.4, -1.2 + z], -30, 0, -20 * sg)
            cs += [a, b]
        elif kind == "greater":
            a = G.rot_cube("horn_a", [x - 0.75, top - 0.8, -1.8 + z], [x, top - 0.8, -1.0 + z], -20, 0, -18 * sg)
            b = G.rot_cube("horn_b", [x + sg * 1.2 - 0.6, top + 2.2, -0.6 + z], [x + sg * 1.2, top + 2.6, -0.4 + z], -25, 0, -10 * sg)
            c = G.rot_cube("horn_c", [x + sg * 1.4 - 0.375, top + 2.2, 2.6 + z], [x + sg * 1.4, top + 2.6, 2.8 + z], 40, 0, -6 * sg)
            cs += [a, b, c]
        elif kind in ("dragon", "kannae"):
            ad = S("horn_a")[2]
            a = G.rot_cube("horn_a", [x - S("horn_a")[0] / 2, top - 0.9, -1.5 + z], [x, top - 0.4, -1.5 + z], 28, 0, -8 * sg)
            end = H.rot_about((x, top - 0.4, -1.5 + ad + z), (x, top - 0.4, -1.5 + z), H.rot_matrix(28, 0, -8 * sg))
            b = G.rot_cube("horn_b", [end[0] - S("horn_b")[0] / 2, end[1] - 0.6, end[2] - 0.4], list(end), 10, 0, -8 * sg)
            cs += [a, b]
        elif kind == "ram":
            a = G.rot_cube("horn_a", [x + sg * 0.6 - 0.625, top - 1.0, -0.8 + z], [x + sg * 0.6, top - 1.0, -0.2 + z], 10, 0, -30 * sg)
            b = G.rot_cube("horn_b", [x + sg * 2.1 - 0.625, top + 0.2, -0.6 + z], [x + sg * 2.1, top + 0.8, -0.4 + z], 0, 0, -20 * sg)
            c = G.rot_cube("horn_c", [x + sg * 2.8 - 0.5, top - 1.4, 1.4 + z], [x + sg * 2.8, top + 0.4, 1.6 + z], 20, 0, 15 * sg)
            cs += [a, b, c]
        elif kind == "antlers":
            base = [x - 0.375, top, -0.4 + z]
            cs.append(G.rot_cube("antler_a", base, [x, top, z], -20, 0, 22 * sg))
            cs.append(G.rot_cube("antler_b", [x + sg * 1.8 - 0.375, top + 2.6, -1.4 + z], [x + sg * 1.8, top + 2.6, -1.0 + z], -45, 0, -10 * sg))
            cs.append(G.rot_cube("antler_c", [x + sg * 2.6 - 0.25, top + 4.0, 0.8 + z], [x + sg * 2.6, top + 4.0, 1.0 + z], 10, 0, 30 * sg))
            cs.append(G.rot_cube("antler_b", [x + sg * 0.6 - 0.375, top + 1.2, -1.9 + z], [x + sg * 0.6, top + 1.2, -1.6 + z], -60, 0, 0))
        if kind == "unicorn":
            break
    if kind == "unicorn":                       # one spiral horn from the forehead
        cs = [G.rot_cube("horn_a", [-0.5, top - 1.2, -3.6 + z], [0, top - 1.2, -3.1 + z], -28, 0, 0),
              G.rot_cube("horn_b", [-0.25, top - 1.2 + 5.5, -3.35 + z], [0, top - 1.2, -3.1 + z], -28, 0, 0)]
    r.add(name, "head", [0, top, z], cs)


def paint_horns(pt, base, ridge=True, tip=None):
    """horn/antler parts: a dark-to-light ramp with ring ridges; `tip` colours the last part"""
    ramp_ = G.shades(base)
    tipr = G.shades(tip) if tip else ramp_
    for p in ("horn_a", "horn_b", "antler_a", "antler_b", "antler_c"):
        pt(p, "all", lambda f, x, y, w, h: pick(ramp_, 0.42 + (0.22 if ridge and (y + x // 2) % 3 == 0 else 0) + (noise(x, y, 4) - 0.5) * 0.1, x, y))
    pt("horn_c", "all", lambda f, x, y, w, h: pick(tipr, 0.5 + 0.2 * (y % 3 == 0), x, y))


# ----------------------------------------------------------------------------- wings
WING_KINDS = {
    "bat": [("wbar", 7, .75, .75), ("memb_a", 7, 6, .25), ("memb_b", 4, 4, .25)],
    "devil_small": [("wbar", 4, .5, .5), ("memb_a", 4, 3.5, .25), ("memb_b", 2.5, 2.25, .25)],
    "dragon": [("wbar", 11, 1, 1), ("memb_a", 11, 9, .25), ("memb_b", 6, 6, .25)],
    "insect": [("iwing_a", 7, 3, .25), ("iwing_b", 5.5, 2.5, .25)],
    "fairy": [("iwing_a", 5, 4.5, .25), ("iwing_b", 4, 3.5, .25)],
    "butterfly": [("iwing_a", 8, 7, .25), ("iwing_b", 6, 6, .25)],
    "moth": [("iwing_a", 8, 6, .25), ("iwing_b", 6.5, 5, .25)],
    "feather": [("fwing_a", 10, 5, .75), ("fwing_b", 7, 3.5, .5)],
}


def wing_parts(kind, scale=1.0):
    q = lambda v: max(.25, round(v * scale * 4) / 4)
    return [(n, q(w), q(h), d) for n, w, h, d in WING_KINDS[kind]]


def wings(r, kind, parent="body", y=18.75, z=1.6, root_x=0.75):
    """wing_r / wing_l from the shoulder blades, spread sideways and swept a little back (the fold/flap comes from the
    animations). Membrane and insect wings are thin plates in the XY plane."""
    if kind in ("bat", "devil_small", "dragon"):
        bw, bh, bd = S("wbar")
        aw, ah, ad = S("memb_a")
        cw, ch_, cd = S("memb_b")
        cs = [cube("wbar", [-root_x - bw, y, z]), cube("memb_a", [-root_x - aw, y - ah + 0.25, z + 0.25]),
              cube("memb_b", [-root_x - bw - cw + 0.5, y - ch_ + 0.25, z + 0.25])]
        r.add("wing_r", parent, [-root_x, y, z], cs, mirror=True)
        return ["wing_r", "wing_l"]
    if kind == "feather":
        aw, ah, ad = S("fwing_a")
        bw, bh, bd = S("fwing_b")
        cs = [cube("fwing_a", [-root_x - aw, y - ah + 1.0, z]), cube("fwing_b", [-root_x - aw - bw + 1.5, y - bh + 0.5, z + 0.1])]
        r.add("wing_r", parent, [-root_x, y, z], cs, mirror=True)
        return ["wing_r", "wing_l"]
    # insects: an upper and a lower pair, each its own bone so they can beat out of step
    aw, ah, ad = S("iwing_a")
    bw, bh, bd = S("iwing_b")
    r.add("wing_r", parent, [-root_x, y, z], [G.rot_cube("iwing_a", [-root_x - aw, y - 0.5, z], [-root_x, y, z], 0, 0, -18)], mirror=True)
    r.add("wing2_r", parent, [-root_x, y - 1.0, z], [G.rot_cube("iwing_b", [-root_x - bw, y - 1.0 - bh + 0.5, z + 0.1], [-root_x, y - 1.0, z], 0, 0, 14)], mirror=True)
    return ["wing_r", "wing_l", "wing2_r", "wing2_l"]


def paint_membrane(pt, bone_col, membrane_col, alpha=235):
    """bat/devil/dragon wings: dark bones, a membrane with finger ribs"""
    bc, mc = G.shades(bone_col), G.shades(membrane_col)
    pt("wbar", "all", lambda f, x, y, w, h: pick(bc, 0.45 + 0.2 * (x % 4 == 0), x, y))

    def memb(f, x, y, w, h):
        if f not in ("north", "south"):
            return pick(bc, 0.4, x, y)
        rib = (x % max(4, w // 4)) == 0
        edge = y >= h - 2 and (x // 3) % 2 == 0          # a scalloped trailing edge
        if edge and y == h - 1:
            return CLEAR
        return G.with_alpha(pick(bc if rib else mc, 0.35 + 0.35 * (1 - y / h) + (noise(x, y, 7) - 0.5) * 0.08, x, y), alpha)
    pt("memb_a", "all", memb)
    pt("memb_b", "all", memb)


def paint_insect(pt, col, vein, alpha=150, pattern=None, eyespot=None):
    """insect wings: veined, see-through (alpha) plates; pattern 'eyespot' / 'border' / 'bands' for moths and butterflies"""
    c, v = G.shades(col), G.shades(vein)
    es = G.shades(eyespot) if eyespot else None

    def fn(f, x, y, w, h):
        if f not in ("north", "south"):
            return G.with_alpha(pick(v, 0.4, x, y), 220)
        vein_ = (x + y // 2) % 6 == 0 or y == 0
        a = alpha
        t = 0.5 + (noise(x // 2, y // 2, 9) - 0.5) * 0.15
        col_ = pick(v if vein_ else c, t, x, y)
        if pattern == "border" and (x <= 1 or y >= h - 2 or x >= w - 2):
            col_, a = pick(v, 0.25, x, y), 255
        if pattern == "bands" and (y // 3) % 2 == 0:
            col_ = pick(v, 0.3, x, y)
        if pattern == "eyespot" and es is not None:
            cx, cy = w * 0.6, h * 0.45
            d = math.hypot(x - cx, (y - cy) * 1.2)
            if d < min(w, h) * 0.22:
                col_, a = pick(es, 0.6 if d > min(w, h) * 0.1 else 0.15, x, y), 255
        return G.with_alpha(col_, a)
    pt("iwing_a", "all", fn)
    pt("iwing_b", "all", fn)


def paint_feather_wings(pt, col, tip):
    c, t = G.shades(col), G.shades(tip)

    def fn(f, x, y, w, h):
        quill = x % 3 == 0
        near_tip = y > h * 0.65
        return pick(t if near_tip else c, 0.42 + 0.25 * quill + (noise(x, y, 5) - 0.5) * 0.1, x, y)
    pt("fwing_a", "all", fn)
    pt("fwing_b", "all", fn)


# ----------------------------------------------------------------------------- animation helpers
def flap(names, period, up, down, sweep=25):
    """wings beating: rotate about Z (up/down strokes) and swept back a little about Y; left mirrors right"""
    Tk = H.track
    out = {}
    for n in names:
        sg = -1 if n.endswith("_l") else 1
        out[n] = {"rotation": Tk((0, (0, sweep * sg, up * sg)), (period / 2, (0, sweep * sg, -down * sg)), (period, (0, sweep * sg, up * sg)))}
    return out


def folded(names, fold=60, droop=-20):
    """wings at rest: folded back along the body"""
    out = {}
    for n in names:
        sg = -1 if n.endswith("_l") else 1
        out[n] = {"rotation": H.hold(0, fold * sg, droop * sg)}
    return out


def sway(names, period, amp, axis=0, phase=0.8, base=None):
    """a travelling wave down a chain (tails, tentacles, necks); base = rest rotation per bone"""
    return G.wave(names, period, amp, axis=axis, phase=phase, base=base)
