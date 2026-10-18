"""Shared pieces for the anuran (frog / toad) model generators: Bullywug, Mucus Toad, Desert Rain Frog.

Built on harpy_lib (texture packer, painter, cubes, preview renderer, writers); harpy_lib itself is untouched.
A species script gives a size table, builds the shared frog body with `build()`, bolts its own outfit on through
an `extras(rig, g)` hook, paints with `paint_base()` plus its own painters, and gets the shared animation set from
`common_anims()`.

Shared anatomy (every anuran has all of it, so animations and game code can treat them alike):
  * bones: root, body, head, eyelids, mouth, tongue, throat, belly, arm/forearm/hand _r/_l, leg/shin/foot _r/_l
  * bulging eyes on top of the head (horizontal pupils) with pop-out eyelids, a tympanum disc behind each eye,
    a wide mouth that pops out open, a tongue that slides out of it, a vocal sac that slides out under the chin
    and a belly puff (three plates that slide out of the torso), webbed feet with toe pads.
  * Pop-outs work like the harpies' eyelids/mouth: the part hides inside the body and a position animation
    slides it out. Nothing here uses bone scale.

Same format facts and detail level as harpy_lib (4 texels per unit, 1 unit = 1/16 block, model faces -Z).
Pure standard library.
"""
import math
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

D = H.D
OUT = H.OUT_
REST = H.REST


def rg(colors, nodither=False):
    return ramp([rgb(*c) for c in colors], nodither)


# ----------------------------------------------------------------------------- part sizes
DEFAULT_SIZES = {
    "head": (9, 6.5, 7.5), "eye": (3.5, 3.25, 3.25), "eyelid": (3.5, 3.25, .125), "tymp": (.25, 2.25, 2.25),
    "mouth": (6.5, 1.75, .125), "tongue_a": (1.25, .5, 6), "tongue_b": (2, .75, 1.5), "throat": (5, 3, 3.5),
    "bang_a": (1.75, 3, .5), "bang_b": (1.5, 4.5, .5), "strand_s": (1.5, 5, 1), "strand_m": (1.5, 7, 1), "strand_l": (1.5, 9, 1),
    "hips": (8, 2.5, 5), "mid": (5.5, 3, 4), "chest": (7.5, 4.5, 5), "belly_f": (5, 3.5, 3.5), "belly_s": (3.5, 3, 3.5),
    "arm_up": (2, 3.5, 2), "forearm": (1.75, 3.5, 1.75), "palm": (2.25, 1.25, 1.5),
    "fing_a": (.5, 1.25, .5), "fing_b": (.5, 1.75, .5), "pad_h": (.75, .5, .75),
    "thigh": (3.5, 6, 3.5), "shin": (2.5, 7, 2.5), "heel": (3.5, 1, 2.5),
    "toe_a": (.75, .75, 3), "toe_b": (.75, .75, 4.5), "toe_c": (.75, .75, 5), "toe_d": (.75, .75, 3.5),
    "pad_t": (1, .75, 1), "web_f": (3.25, .25, 3.5),
}


def declare_base(sizes=None):
    """register the shared parts (a species overrides sizes through `sizes`); the species adds its own with H.part"""
    H.PARTS.clear()
    H.RECT.clear()
    sz = dict(DEFAULT_SIZES)
    sz.update(sizes or {})
    for name, dims in sz.items():
        H.part(name, *dims)
    hw, hh, hd = sz["head"]
    H.part("hair_cap", hw, hh, hd, ("down",))


def S(name):
    return H.PARTS[name]["size"]


# ----------------------------------------------------------------------------- geometry
class Rig:
    """bones are collected first so a species hook can still add cubes to them before the model is emitted"""

    def __init__(self):
        self.order, self.b = [], {}

    def add(self, name, parent, pivot, cubes=None, mirror=False):
        self.b[name] = dict(parent=parent, pivot=list(pivot), cubes=list(cubes or []), mirror=mirror)
        self.order.append(name)

    def put(self, name, *cubes):
        self.b[name]["cubes"].extend(cubes)

    def model(self, mname):
        m = H.Model(mname)
        for n in self.order:
            d = self.b[n]
            m.bone(n, d["parent"], d["pivot"], d["cubes"] or None, d["mirror"])
        return m


def geometry(s):
    """key heights of the standard body, computed from the declared part sizes"""
    g = SimpleNamespace()
    g.ankle_y = S("heel")[1]
    g.knee_y = g.ankle_y + S("shin")[1]
    g.hip_y = g.knee_y + S("thigh")[1] - 0.75
    g.hips_y0 = g.hip_y - 1.0
    g.mid_y0 = g.hips_y0 + S("hips")[1] - 0.25
    g.chest_y0 = g.mid_y0 + S("mid")[1] - 0.25
    g.chest_top = g.chest_y0 + S("chest")[1]
    g.head_y0 = g.chest_top - s.head_sink
    g.head_top = g.head_y0 + S("head")[1]
    g.head_z0 = -S("head")[2] / 2 - s.head_fwd
    g.shoulder_y = g.chest_top - 1.25
    g.shoulder_x = S("chest")[0] / 2 + S("arm_up")[0] / 2 - 0.25
    g.chest_front = -S("chest")[2] / 2 + s.chest_fwd
    g.mid_front = -S("mid")[2] / 2
    g.eye_z0 = g.head_z0 - s.eye_out
    g.eye_y0 = g.head_top - S("eye")[1] + s.eye_rise
    g.T, g.Sh = S("thigh")[1], S("shin")[1]
    return g


def build(s, extras=None, name="anuran"):
    """the shared frog body. `s`: layout numbers (legx, eye_x, eye_out, eye_rise, head_sink, head_fwd, chest_fwd,
    mouth_y, throat_y, tymp_y, tymp_z). `extras(rig, g)` adds the species' hair, outfit and extra bones."""
    g = geometry(s)
    r = Rig()
    r.add("root", None, [0, 0, 0])

    # ---- torso: hips, waist, chest; the belly plates hide inside and slide out for a puff
    hw, hh_, hd = S("hips")
    mw, mh, md = S("mid")
    cw, ch, cd = S("chest")
    body = [cube("hips", [-hw / 2, g.hips_y0, -hd / 2]), cube("mid", [-mw / 2, g.mid_y0, -md / 2]),
            cube("chest", [-cw / 2, g.chest_y0, g.chest_front])]
    r.add("body", "root", [0, g.hip_y, 0], body)
    bfw = S("belly_f")
    r.add("belly", "body", [0, g.mid_y0 + 1.5, 0], [cube("belly_f", [-bfw[0] / 2, g.mid_y0 + 0.5, g.mid_front + 0.3])])
    bs = S("belly_s")
    side = cube("belly_s", [-mw / 2 + 0.3, g.mid_y0 + 0.25, -bs[2] / 2])
    r.add("belly_r", "belly", [0, g.mid_y0 + 1.5, 0], [side], mirror=True)

    # ---- head: no neck; wide, with eyes on top, tympana behind them
    hw_, hh, hd_ = S("head")
    ew, eh, ed = S("eye")
    head = [cube("head", [-hw_ / 2, g.head_y0, g.head_z0]), cube("hair_cap", [-hw_ / 2, g.head_y0, g.head_z0], inflate=0.5)]
    hum = getattr(s, "hum", None)
    if not hum:                                   # frog eyes: two bulges on top of the head
        eye = cube("eye", [-s.eye_x - ew / 2, g.eye_y0, g.eye_z0])
        head += [eye, mirror_cube(eye)]
    tw = S("tymp")
    tymp = cube("tymp", [-hw_ / 2 - 0.2, g.head_y0 + s.tymp_y, g.head_z0 + s.tymp_z])
    head += [tymp, mirror_cube(tymp)]
    r.add("head", "body", [0, g.head_y0 + 1.0, 0], head)
    if hum:                                       # humanoid face: the eyes are painted on the face, only the lids are parts
        lw, lh, _ = S("eyelid")
        lid = cube("eyelid", [-hum["sep"] - lw / 2, g.head_y0 + hum["y"] - lh / 2, g.head_z0 + 0.3])
        r.add("eyelids", "head", [0, g.head_y0 + hum["y"], g.head_z0], [lid, mirror_cube(lid)])
    else:
        lid = cube("eyelid", [-s.eye_x - ew / 2, g.eye_y0, g.eye_z0 + 0.3])
        r.add("eyelids", "head", [0, g.eye_y0 + eh / 2, g.eye_z0], [lid, mirror_cube(lid)])
    mw_, mh_, _ = S("mouth")
    r.add("mouth", "head", [0, g.head_y0 + s.mouth_y, g.head_z0], [cube("mouth", [-mw_ / 2, g.head_y0 + s.mouth_y, g.head_z0 + 0.3])])
    tz = g.head_z0 + 0.5
    ty = g.head_y0 + s.mouth_y + 0.25
    r.add("tongue", "head", [0, ty, tz + 5], [cube("tongue_a", [-S("tongue_a")[0] / 2, ty, tz]), cube("tongue_b", [-S("tongue_b")[0] / 2, ty - 0.125, tz - 0.25])])
    sw, sh, sd = S("throat")
    r.add("throat", "head", [0, g.head_y0 + 1.5, g.head_z0 + 2], [cube("throat", [-sw / 2, g.head_y0 + s.throat_y, g.head_z0 + 0.75])])

    # ---- arms: webless fingers with soft pads
    aw, ah, ad = S("arm_up")
    fw_, fh, fd = S("forearm")
    pw, ph, pd = S("palm")
    sx, sy = g.shoulder_x, g.shoulder_y
    elbow = sy - ah + 0.5
    wrist = elbow - fh + 0.25
    r.add("arm_r", "body", [-sx, sy, 0], [cube("arm_up", [-sx - aw / 2, elbow, -ad / 2])], mirror=True)
    r.add("forearm_r", "arm_r", [-sx, elbow, 0], [cube("forearm", [-sx - fw_ / 2, wrist, -fd / 2])], mirror=True)
    palm_y = wrist - ph + 0.25
    hand = [cube("palm", [-sx - pw / 2, palm_y, -pd / 2])]
    fa, fb = S("fing_a"), S("fing_b")
    for i, p in enumerate(("fing_a", "fing_b", "fing_b", "fing_a")):
        fx = -sx - pw / 2 + 0.125 + i * (pw - 0.5) / 3
        fy = palm_y - S(p)[1] + 0.25
        hand.append(cube(p, [fx, fy, -pd / 2 + 0.25]))
        hand.append(cube("pad_h", [fx - 0.125, fy - 0.25, -pd / 2 + 0.125]))
    r.add("hand_r", "forearm_r", [-sx, wrist, 0], hand, mirror=True)

    # ---- legs: thigh, shin, long webbed foot
    cx = -s.legx
    tw_, th, td = S("thigh")
    sw_, shh, sd_ = S("shin")
    r.add("leg_r", "root", [cx, g.hip_y, 0], [cube("thigh", [cx - tw_ / 2, g.knee_y - 0.5, -td / 2])], mirror=True)
    r.add("shin_r", "leg_r", [cx, g.knee_y, 0], [cube("shin", [cx - sw_ / 2, g.ankle_y, 0.5 - sd_ / 2])], mirror=True)
    hl_w, hl_h, hl_d = S("heel")
    hz0 = -0.5
    foot = [cube("heel", [cx - hl_w / 2, 0, hz0])]
    xs = [cx - 1.5 + i * 1.0 for i in range(4)] if hl_w >= 3.25 else [cx - 0.9 + i * 0.6 for i in range(4)]
    web_d = S("web_f")[2]
    foot.append(cube("web_f", [cx - S("web_f")[0] / 2, 0.25, hz0 - web_d]))
    for x, p in zip(xs, ("toe_a", "toe_c", "toe_b", "toe_d")):
        tw__, thh, tl = S(p)
        foot.append(cube(p, [x - tw__ / 2, 0, hz0 - tl]))
        foot.append(cube("pad_t", [x - S("pad_t")[0] / 2, 0, hz0 - tl - 0.5]))
    r.add("foot_r", "shin_r", [cx, g.ankle_y, 0.5], foot, mirror=True)

    if extras:
        extras(r, g)
    return r.model(name), g


# ----------------------------------------------------------------------------- painting
def blob_t(x, y, seed, cell, rmin, rmax):
    """0..1 strength of a soft round blob on a jittered grid (0 = none)"""
    best = 0.0
    c0x, c0y = x // cell, y // cell
    for gy in (-1, 0, 1):
        for gx in (-1, 0, 1):
            ix, iy = c0x + gx, c0y + gy
            if noise(ix, iy, seed) < 0.40:
                continue
            px = (ix + 0.2 + 0.6 * noise(ix, iy, seed + 1)) * cell
            py = (iy + 0.2 + 0.6 * noise(ix, iy, seed + 2)) * cell
            rad = rmin + (rmax - rmin) * noise(ix, iy, seed + 3)
            d = math.hypot(x + 0.5 - px, y + 0.5 - py)
            if d < rad:
                best = max(best, 1 - d / rad)
    return best


def torso_tone(pal, f, y, h, x, kind):
    """True where this pixel is belly-coloured"""
    ys = y / max(1, h - 1)
    if kind == "limb":
        return f == "down"
    if f in ("north", "down"):
        return True
    if f in ("south", "up"):
        return False
    return ys > pal.side_split + (noise(x, y, 9) - 0.5) * 0.10


def skin_fn(pal, kind="torso", grain=0.07, base=0.62):
    """two-tone amphibian skin: back colour on top, belly colour below; spots, warts, a pale lateral stripe and wet glints"""
    BACK, BELLY = pal.BACK, pal.BELLY

    def fn(f, x, y, w, h):
        belly = torso_tone(pal, f, y, h, x, kind)
        ramp_ = BELLY if belly else BACK
        t = base + (noise(x, y, 1) - 0.5) * grain + (0.10 if belly else 0)
        if f in ("east", "west"):
            t -= 0.06
        elif f in ("south", "down"):
            t -= 0.10
        edge = min(x, w - 1 - x)
        if edge < 1:
            t -= 0.08
        if y > h - 2:
            t -= 0.06
        if pal.spots and (not belly or pal.belly_spots):
            sp = pal.spots
            b = blob_t(x, y, sp["seed"] + (3 if f in ("east", "west") else 0), sp["cell"], sp["rmin"], sp["rmax"])
            if b > 0:
                t -= sp["depth"] * min(1.0, b * 2.2)
        c = pick(ramp_, t, x, y)
        if pal.warts and not belly and w >= 6 and h >= 6:
            wt = pal.warts
            cell = wt["cell"]
            ix, iy = x // cell, y // cell
            if noise(ix, iy, wt["seed"]) < wt["density"]:
                px = ix * cell + 1 + int(noise(ix, iy, wt["seed"] + 1) * (cell - 2))
                py = iy * cell + 1 + int(noise(ix, iy, wt["seed"] + 2) * (cell - 2))
                if (x, y) == (px, py):
                    c = pick(wt["ramp"], 0.95, x, y)
                elif (x, y) in ((px + 1, py), (px, py + 1), (px + 1, py + 1)):
                    c = pick(wt["ramp"], 0.55 if (x, y) != (px + 1, py + 1) else 0.15, x, y)
        if pal.stripe and f in ("east", "west", "south", "up") and kind == "torso":
            if f in ("east", "west"):
                if abs(y / max(1, h - 1) - pal.side_split) < 0.07 and h >= 6:
                    c = pick(pal.stripe, 0.7 - 0.2 * (noise(x, y, 4) > 0.8), x, y)
            elif h >= 6 and w >= 8 and (x in (w // 5, w // 5 + 1, w - 1 - w // 5, w - 2 - w // 5)):
                c = pick(pal.stripe, 0.65, x, y)
        if pal.mucus and noise(x, y // 2, 77) > 1 - pal.mucus[1] and w >= 6:
            c = pal.mucus[0]
        if pal.gloss and not belly and f in ("north", "east", "west", "up") and w >= 8 and h >= 6:
            gx, gy = int(w * 0.27), int(h * 0.22)
            if (x, y) in ((gx, gy), (gx + 1, gy), (gx - 1, gy + 1)):
                c = pick(BACK, 0.98, x, y)
            elif (x, y) == (gx + 3, gy - 1) and pal.gloss > 1:
                c = pick(BACK, 0.95, x, y)
        return c
    return fn


def paint_eye(pt, pal):
    BACK = pal.BACK
    ew, eh, ed = S("eye")

    def north(f, x, y, w, h):
        cx, cy = w / 2.0, h / 2.0
        dx, dy = (x + 0.5 - cx) / (w / 2.0), (y + 0.5 - cy) / (h / 2.0)
        r2 = dx * dx + dy * dy
        if r2 > 1.0:
            return pick(BACK, 0.5, x, y)
        if y < h * pal.lid:                                   # a permanently drooping upper lid
            return pal.EYE_OUT if y >= h * pal.lid - 1.1 else pick(BACK, 0.45 + 0.2 * (y / h), x, y)
        if r2 > 0.80:
            return pal.EYE_OUT
        t = 0.30 + 0.60 * (dy + 1) / 2 - 0.25 * max(0.0, r2 - 0.45)
        col = pick(pal.EYE, t, x, y)
        pxr, pyr = pal.pupil
        if (dx / pxr) ** 2 + ((dy + 0.04) / pyr) ** 2 <= 1.0:
            col = pal.PUPIL
        gx, gy = int(w * 0.30), int(h * 0.26)
        if gy >= int(h * pal.lid):
            if x in (gx, gx + 1) and y in (gy, gy + 1):
                col = pal.WHITE
            elif (x, y) == (int(w * 0.68), int(h * 0.70)):
                col = pal.EYE[-1]
        return col

    def outward(f, x, y, w, h):
        fx = w - 1 if f == "east" else 0                      # the edge shared with the front face
        d = math.hypot(abs(x - fx) / (w * 0.85), (y + 0.5 - h / 2.0) / (h / 2.0))
        if y < h * pal.lid:
            return pick(BACK, 0.5, x, y)
        if d < 0.88:
            col = pick(pal.EYE, 0.30 + 0.55 * (y / h), x, y)
            if d < 0.40 and abs(y + 0.5 - h / 2.0) < h * 0.14 * pal.pupil[1] * 3:
                col = pal.PUPIL
            return col
        return pick(BACK, 0.45, x, y)
    pt("eye", ("north",), north)
    pt("eye", ("east", "west"), outward)
    pt("eye", ("up",), lambda f, x, y, w, h: pick(BACK, 0.66 + (noise(x, y, 5) - 0.5) * 0.06, x, y))
    pt("eye", ("south", "down"), lambda f, x, y, w, h: pick(BACK, 0.40, x, y))

    def lid(f, x, y, w, h):
        if f != "north":
            return pick(BACK, 0.6, x, y)
        cx, cy = w / 2.0, h / 2.0
        dx, dy = (x + 0.5 - cx) / (w / 2.0), (y + 0.5 - cy) / (h / 2.0)
        if dx * dx + dy * dy > 1.0:
            return CLEAR
        if abs(y + 0.5 - (cy + 1.5 - 1.6 * math.cos(dx * 1.2))) < 0.9:
            return pal.EYE_OUT                                # the closed-eye line
        return pick(BACK, 0.62 + (noise(x, y, 6) - 0.5) * 0.06, x, y)
    pt("eyelid", "all", lid)


def humanoid_face(pal, hum, hh, mouth_row, curve):
    """a human-style face painted on the head's front: big round eyes under brows, a tiny nose, a small mouth.
    hum: dict(sep, y, a, b, mw) = eye centre offset / height above the chin (units), eye semi-axes (texels), mouth half width (units)"""
    BACK, BELLY = pal.BACK, pal.BELLY
    a, b = hum["a"], hum["b"]
    cy = (hh - hum["y"]) * D

    def eye_px(x, y, cx, sg):
        dx, dy = (x + 0.5 - cx) / a, (y + 0.5 - cy) / b
        e = dx * dx + dy * dy
        if e > 1.0:
            return pick(BACK, 0.46, x, y) if (e < 1.3 and dy > 0.45) else None
        top = cy - b + 2 * b * pal.lid
        if y + 0.5 < top:                                         # a drooping upper lid (sleepy faces)
            return pal.EYE_OUT if y + 0.5 >= top - 1.1 else pick(BACK, 0.45, x, y)
        if e > 0.62 or (dy < -0.40 and e > 0.30):
            return pal.EYE_OUT
        col = pick(pal.EYE, (y - (cy - b)) / (2 * b), x, y)
        px, py = pal.hpupil
        if pal.hpupil_bitmap:                                     # a hand-drawn pupil (e.g. a heart), centred on the eye
            bm = pal.hpupil_bitmap
            i, j = int(x - cx + len(bm[0]) / 2.0), int(y - cy + len(bm) / 2.0 + 0.5)
            if 0 <= j < len(bm) and 0 <= i < len(bm[0]) and bm[j][i] == "#":
                col = pal.PUPIL
        elif (dx / px) ** 2 + ((dy + 0.04) / py) ** 2 <= 1.0:
            col = pal.PUPIL
        if x in (int(cx - sg * a * 0.4), int(cx - sg * a * 0.4) + 1) and int(cy - b * 0.45) <= y <= int(cy - b * 0.45) + 1:
            col = pal.WHITE
        if (x, y) == (int(cx + sg * a * 0.35), int(cy + b * 0.42)):
            col = pal.EYE[-1]
        return col

    def face(f, x, y, w, h):
        yy = y + 0.5
        for sg in (-1, 1):
            cx = w / 2.0 + sg * hum["sep"] * D
            c = eye_px(x, y, cx, sg)
            if c is not None:
                return c
            if (x, y) in ((int(cx + sg * (a + 0.5)), int(cy - b * 0.62)), (int(cx + sg * (a + 0.5)), int(cy - b * 0.62) - 1)):
                return pal.EYE_OUT                                # a lash flick at the outer corner
            brow = int(cy - b - 2.5)
            if y == brow and abs(x + 0.5 - cx) <= a * 0.95:
                return pick(BACK, 0.12, x, y)                      # brows in the darkest skin tone
            bx = cx + sg * a * 0.55
            if pal.BLUSH and ((x + 0.5 - bx) / 2.8) ** 2 + ((yy - (cy + b + 3.0)) / 1.3) ** 2 <= 1.0:
                return pal.BLUSH
        mw = hum["mw"] * D
        dx = (x + 0.5 - w / 2.0) / mw
        ly = mouth_row - curve * 0.5 * dx * dx
        if abs(dx) <= 1.0 and abs(yy - ly) <= 0.6:
            return pal.LIP_D
        if abs(dx) <= 0.7 and 0.6 < yy - ly <= 1.6:
            return pick(BELLY, 0.80, x, y)                         # lower lip
        if (x, y) in ((int(w / 2) - 1, int(mouth_row - 4)), (int(w / 2), int(mouth_row - 4))):
            return pal.NOSTRIL
        t = 0.66 + (noise(x, y, 3) - 0.5) * 0.07
        if y < 3:
            t -= 0.12
        if y > h - 4:
            t += 0.06
        if x < 2 or x > w - 3:
            t -= 0.08
        return pick(BACK, t, x, y)
    return face


def paint_head(pt, pal, curve=3.0, nostrils=True, hum=None):
    """face (north), jaw/lip line along the sides, the tympanum discs and the open-mouth plate"""
    BACK, BELLY = pal.BACK, pal.BELLY
    hw, hh, hd = S("head")
    mouth_row = (hh - pal._mouth_y - 0.9) * D            # row of the lip line in the north face

    def lip_y(x, w):
        dx = (x + 0.5 - w / 2.0) / (w / 2.0)
        return mouth_row - curve * (dx * dx)

    def face(f, x, y, w, h):
        ly = lip_y(x, w)
        yy = y + 0.5
        if abs(yy - ly) <= 0.6:
            return pal.LIP_D
        if yy > ly:                                       # the paler jaw under the lip line
            t = 0.60 + (noise(x, y, 2) - 0.5) * 0.06 - (0.10 if y > h - 3 else 0)
            if abs(yy - ly) <= 1.6:
                t += 0.14
            return pick(BELLY, t, x, y)
        if nostrils:
            for sg in (-1, 1):
                nx = int(w / 2 + sg * pal.nostril_dx)
                ny = int(ly - pal.nostril_up)
                if (x, y) in ((nx, ny), (nx + (1 if sg < 0 else -1), ny)):
                    return pal.NOSTRIL
        for sg in (-1, 1):                                # blush under the eyes
            bx = w / 2 + sg * w * 0.36
            e = ((x + 0.5 - bx) / 2.8) ** 2 + ((yy - (ly - 3.4)) / 1.3) ** 2
            if e <= 1.0 and pal.BLUSH:
                return pal.BLUSH if e < 0.5 else pal.BLUSH_L
        t = 0.58 + (noise(x, y, 3) - 0.5) * 0.07
        if y < 3:
            t -= 0.12
        if x < 2 or x > w - 3:
            t -= 0.08
        return pick(BACK, t, x, y)
    pt("head", "all", skin_fn(pal, "torso"))
    pt("head", ("up",), skin_fn(pal, "torso", base=0.60))
    pt("head", ("north",), humanoid_face(pal, hum, hh, mouth_row, curve) if hum else face)

    def side(f, x, y, w, h):
        fx = w - 1 if f == "east" else 0
        ly = mouth_row - curve * 0.2 * ((abs(x - fx) / w) ** 2) * 6
        yy = y + 0.5
        if abs(yy - ly) <= 0.6 and abs(x - fx) < w * 0.72:
            return pal.LIP_D
        if yy > ly:
            return pick(BELLY, 0.60 + (noise(x, y, 2) - 0.5) * 0.06, x, y)
        return None
    pt("head", ("east", "west"), side)

    def drum(f, x, y, w, h):
        if f in ("east", "west"):
            dx, dy = (x + 0.5 - w / 2.0) / (w / 2.0), (y + 0.5 - h / 2.0) / (h / 2.0)
            r2 = dx * dx + dy * dy
            if r2 > 1.0:
                return CLEAR
            if r2 > 0.78:
                return pick(pal.DRUM, 0.15, x, y)
            c = pick(pal.DRUM, 0.45 + 0.4 * (1 - r2) + (noise(x, y, 7) - 0.5) * 0.05, x, y)
            if r2 < 0.05:
                return pick(pal.DRUM, 0.10, x, y)
            return c
        return pick(pal.DRUM, 0.3, x, y)
    pt("tymp", "all", drum)

    def mouth(f, x, y, w, h):
        if f != "north":
            return pal.LIP_D
        e = ((x + 0.5 - w / 2.0) / (w / 2.0)) ** 2 + abs((y + 0.5 - h / 2.0) / (h / 2.0)) ** 1.5
        if e > 1.0:
            return CLEAR
        if e > 0.74:
            return pal.LIP_D
        if y >= h - 3 and 3 <= x < w - 3:
            return pal.TONGUE
        return pal.MOUTH_IN
    pt("mouth", "all", mouth)
    pt("tongue_a", "all", lambda f, x, y, w, h: pick(pal.TONGUE_R, 0.45 + 0.25 * (f == "up") + (noise(x, y, 8) - 0.5) * 0.1, x, y))
    pt("tongue_b", "all", lambda f, x, y, w, h: pick(pal.TONGUE_R, 0.78 - (0.2 if y < 1 or x < 1 or x > w - 2 else 0) + (noise(x, y, 8) - 0.5) * 0.1, x, y))

    def sac(f, x, y, w, h):
        t = 0.62 + (noise(x, y, 11) - 0.5) * 0.1 - (0.12 if f in ("east", "west", "south") else 0)
        if f in ("north", "down") and x % 5 == 2 and noise(x // 5, y, 12) > 0.35:
            return pick(pal.SAC, t - 0.28, x, y)                    # fine veins
        return pick(pal.SAC, t, x, y)
    pt("throat", "all", sac)


def hair_px(pal, x, y, h, gloss=False):
    t = (pal.streak[(x // 2) % 7] * 0.8 + (y / max(1, h - 1)) * 0.32 + (noise(x, y, 7) - 0.5) * 0.10) * pal.hair_gain + pal.hair_off
    if gloss and 0.16 < y / max(1, h - 1) < 0.30:
        t += 0.22
    if y > h - 3:
        t += 0.10
    return pick(pal.HAIR, t, x, y)


def paint_hair(pt, pal, fringe=3, sides_to=10):
    """hair cap over the top and back of the head (face and cheeks stay clear), plus tapered strands and bangs"""
    pt("hair_cap", "all", lambda f, x, y, w, h: hair_px(pal, x, y, h, True))
    pt("hair_cap", ("north",), lambda f, x, y, w, h: hair_px(pal, x, y, h, True) if y < fringe else CLEAR)
    pt("hair_cap", ("east", "west"), lambda f, x, y, w, h: None if y < sides_to else CLEAR)
    pt("hair_cap", ("south",), lambda f, x, y, w, h: CLEAR if y >= h - 6 and noise(x, y, 9) > 0.5 else None)

    def strand(f, x, y, w, h):
        if f not in BROAD:
            return pick(pal.HAIR, 0.45, x, y)
        r = h - 1 - y
        taper = 2.5 * D
        if r < taper:
            half = (w / 2.0) * (min(1.0, (r + 1) / taper) ** 0.85)
            if abs(x + 0.5 - w / 2.0) > half:
                return CLEAR
        return hair_px(pal, x, y, h, y < 5)
    for p in [n for n in H.PARTS if n.startswith(("bang_", "strand_"))]:
        pt(p, "all", strand)


def paint_limbs(pt, pal):
    skin_t, skin_l = skin_fn(pal, "torso"), skin_fn(pal, "limb")
    for p in ("hips", "mid", "chest"):
        pt(p, "all", skin_t)
    for p in ("belly_f", "belly_s"):
        pt(p, "all", skin_fn(pal, "torso", base=0.66))
    for p in ("arm_up", "forearm", "thigh", "shin", "palm"):
        pt(p, "all", skin_l)
    pt("heel", "all", skin_l)
    for p in ("fing_a", "fing_b"):
        pt(p, "all", skin_fn(pal, "limb", base=0.60))
    PAD = pal.PAD

    def pad(f, x, y, w, h):
        if f in ("up", "down", "north", "south", "east", "west") and (x < 1 or x > w - 2 or y > h - 2):
            return pick(PAD, 0.30, x, y)
        return pick(PAD, 0.70 + (0.2 if (x, y) == (1, 1) else 0), x, y)
    pt("pad_h", "all", pad)
    pt("pad_t", "all", pad)

    def toe(f, x, y, w, h):
        if f in BROAD and y == h - 1:
            return pick(pal.BACK, 0.30, x, y)
        if (f in ("east", "west", "up") and x % 5 == 4):
            return pick(pal.BACK, 0.35, x, y)
        return pick(pal.BACK, 0.60 + (noise(x, y, 61) - 0.5) * 0.10, x, y)
    for p in ("toe_a", "toe_b", "toe_c", "toe_d"):
        pt(p, "all", toe)

    def web(f, x, y, w, h):                                # thin skin between the toes, veined and a shade lighter
        if f in ("up", "down"):
            c = 0.70 + (noise(x, y, 41) - 0.5) * 0.08
            if x % 4 == 3 and y < h - 2:
                c -= 0.22
            if y >= h - 2:
                c -= 0.12
            return pick(pal.BELLY, c, x, y)
        return pick(pal.BACK, 0.42, x, y)
    pt("web_f", "all", web)


def paint_base(pal, curve=3.0, fringe=3, hum=None):
    pt = H.Painter()
    paint_limbs(pt, pal)
    paint_head(pt, pal, curve, hum=hum)
    if not hum:
        paint_eye(pt, pal)
    paint_hair(pt, pal, fringe)
    return pt


def new_pal(**kw):
    """defaults for the knobs the shared painters read; a species overrides what it needs"""
    p = SimpleNamespace(
        BACK=None, BELLY=None, belly_spots=False, mucus=None, spots=None, warts=None, stripe=None, gloss=1, side_split=0.55,
        EYE=None, PUPIL=rgb(10, 10, 14), EYE_OUT=rgb(20, 18, 16), WHITE=rgb(255, 252, 244), lid=0.0, pupil=(0.6, 0.28), hpupil=(0.55, 0.55), hpupil_bitmap=None,
        LIP_D=rgb(60, 70, 40), MOUTH_IN=rgb(110, 40, 48), TONGUE=rgb(214, 100, 110), TONGUE_R=None, NOSTRIL=rgb(30, 40, 24),
        nostril_dx=3.0, nostril_up=4, BLUSH=None, BLUSH_L=None, DRUM=None, SAC=None, PAD=None,
        HAIR=None, streak=(0.46, 0.60, 0.40, 0.66, 0.50, 0.58, 0.42), hair_gain=1.0, hair_off=0.0, _mouth_y=1.0)
    for k, v in kw.items():
        setattr(p, k, v)
    return p


# ----------------------------------------------------------------------------- hair helpers
def strands_back(parts_x, ytop, z, names, rx=4, spread=1.2):
    """a row of hanging strands (cubes) across the back of the head"""
    out = []
    for i, x in enumerate(parts_x):
        out.append(H.hang(names[i % len(names)], x, ytop, z + 0.25 * (i % 3), x * spread, rx + 2 * (i % 3)))
    return out


# ----------------------------------------------------------------------------- animations
def interp(frames, t, period):
    """frames: sorted [(time, (values...))], periodic over `period`"""
    ks = list(frames)
    if t < ks[0][0]:
        ks = [(ks[-1][0] - period, ks[-1][1])] + ks
    if t > ks[-1][0]:
        ks = ks + [(ks[0][0] + period, ks[0][1])]
    for (t0, v0), (t1, v1) in zip(ks, ks[1:]):
        if t0 <= t <= t1:
            f = 0.0 if t1 == t0 else (t - t0) / (t1 - t0)
            return tuple(v0[i] + (v1[i] - v0[i]) * f for i in range(len(v0)))
    return ks[-1][1]


def gait(T, Sh, period, fr_r, fr_l=None, foot=1.0):
    """leg tracks from key poses (time, thigh deg, shin deg, lift, foot extra deg); thigh a (negative = forward), shin b
    relative. The body is kept on the ground by the straighter leg. Returns (bone dict, body y per time)."""
    fr_r = sorted(fr_r)
    if fr_l is None:
        fr_l = [(round((t + period / 2) % period, 4), v) for t, v in fr_r]
    fr_l = sorted(fr_l)
    times = sorted({round(t, 4) for t, _ in fr_r} | {round(t, 4) for t, _ in fr_l} | {0.0, round(period, 4)})
    tr = {k: {} for k in ("leg_r", "shin_r", "foot_r", "leg_l", "shin_l", "foot_l")}
    body = {}
    for t in times:
        tt = t % period if t < period else period - 1e-6
        vals = {}
        for side, fr in (("r", fr_r), ("l", fr_l)):
            a, b, lift, fx = interp(fr, tt, period)
            ext = T * math.cos(math.radians(a)) + Sh * math.cos(math.radians(a + b))
            vals[side] = (a, b, lift, fx, ext - (T + Sh))
        by = max(vals["r"][4], vals["l"][4]) + max(vals["r"][2], vals["l"][2]) * 0 + (vals["r"][2] + vals["l"][2]) / 2
        body[t] = by
        for side in ("r", "l"):
            a, b, lift, fx, d = vals[side]
            tr["leg_" + side][t] = [a, 0, 0]
            tr["shin_" + side][t] = [b, 0, 0]
            tr["foot_" + side][t] = [-(a + b) * foot + fx, 0, 0]
    out = {k: {"rotation": {H.t(t): v for t, v in d.items()}} for k, d in tr.items()}
    return out, {H.t(t): [0, by, 0] for t, by in body.items()}


def common_anims(an, g, P):
    """idle, walk, hop, swim, croak (vocal sac), tongue (lash), sit. `P`: SimpleNamespace of tuning numbers
    (crouch, step, hop_lift, throat_out, arm_up_idle ...). Species scripts add their own signature animations on top."""
    T, Sh = g.T, g.Sh
    Tk, merge, sym, hold = H.track, H.merge, H.sym, H.hold
    ARM_REST = P.arm_rest

    def add(key, length, loop, bones):
        an.add(key, length, loop, merge(bones, P.extra.get(key, {})))

    def legs(period, fr_r, fr_l=None, foot=1.0, extra_body=None):
        lg, by = gait(T, Sh, period, fr_r, fr_l, foot)
        for side in ("r", "l"):
            lg["leg_" + side]["position"] = by
        lg["body"] = {"position": by}
        return lg

    c = P.crouch
    pose = lambda deg: (-deg, 2 * deg, 0, 0)

    # ---- idle
    add("idle", 4.0, True, merge(
        legs(4.0, [(0, pose(c)), (2, pose(c + 2)), (4, pose(c))], [(0, pose(c)), (2, pose(c + 2)), (4, pose(c))]),
        {"head": {"rotation": Tk((0, (0, 0, 0)), (1.3, (-3, 8, 1)), (2.6, (1, -6, -1)), (4, (0, 0, 0)))},
         "throat": {"position": Tk((0, REST), (0.8, (0, -0.3, -0.35)), (1.6, REST), (2.4, (0, -0.3, -0.35)), (3.2, REST), (4, REST))},
         "belly": {"position": Tk((0, REST), (2, (0, 0, -0.25)), (4, REST))},
         "hair_back": {"rotation": Tk((0, (0, 0, 0)), (2, (3, 0, 0)), (4, (0, 0, 0)))}},
        H.blink(2.7), sym("arm", hold(*ARM_REST)), sym("forearm", hold(-8, 0, 0)), sym("hand", hold(-6, 0, 0))))

    # ---- walk (a waddle of short strides)
    st = P.step
    fw, bk = -c - st, -c + st * 0.8
    r_frames = [(0, (fw, 2 * c + 8, 0, 0)), (0.25, (-c, 2 * c + 24, 0, 0)), (0.5, (bk, 2 * c, 0, 0)), (0.75, (-c - st * 0.3, 2 * c + 50, 0.6, 12)), (1.0, (fw, 2 * c + 8, 0, 0))]
    add("walk", 1.0, True, merge(
        legs(1.0, r_frames),
        {"body": {"rotation": Tk((0, (3, 0, 3)), (0.5, (3, 0, -3)), (1, (3, 0, 3)))},
         "head": {"rotation": Tk((0, (-3, 0, -3)), (0.5, (-3, 0, 3)), (1, (-3, 0, -3)))},
         "arm_r": {"rotation": Tk((0, (-18, 0, 0)), (0.5, (14, 0, 0)), (1, (-18, 0, 0)))},
         "arm_l": {"rotation": Tk((0, (14, 0, 0)), (0.5, (-18, 0, 0)), (1, (14, 0, 0)))},
         "throat": {"position": Tk((0, REST), (0.5, (0, -0.25, -0.25)), (1, REST))},
         "hair_back": {"rotation": Tk((0, (0, 0, 0)), (0.25, (6, 0, 0)), (0.5, (0, 0, 0)), (0.75, (6, 0, 0)), (1, (0, 0, 0)))}},
        sym("forearm", hold(-14, 0, 0))))

    # ---- hop: coil, launch with the legs straight back, glide, tuck, land
    hl = P.hop_lift
    hop_fr = [(0, (-42, 84, 0, 0)), (0.14, (-58, 116, 0, 0)), (0.28, (34, -2, hl * 0.45, 28)), (0.42, (42, -10, hl, 34)),
              (0.58, (14, 34, hl * 0.6, 8)), (0.74, (-30, 62, 0, 0)), (0.9, (-42, 84, 0, 0))]
    add("hop", 0.9, True, merge(
        legs(0.9, hop_fr, hop_fr),
        {"body": {"rotation": Tk((0, (-6, 0, 0)), (0.14, (-14, 0, 0)), (0.3, (24, 0, 0)), (0.42, (22, 0, 0)), (0.58, (6, 0, 0)), (0.74, (-8, 0, 0)), (0.9, (-6, 0, 0)))},
         "head": {"rotation": Tk((0, (0, 0, 0)), (0.14, (10, 0, 0)), (0.3, (-26, 0, 0)), (0.42, (-22, 0, 0)), (0.9, (0, 0, 0)))},
         "throat": {"position": Tk((0, REST), (0.14, (0, -0.4, -0.5)), (0.3, REST), (0.9, REST))},
         "hair_back": {"rotation": Tk((0, (0, 0, 0)), (0.3, (34, 0, 0)), (0.5, (20, 0, 0)), (0.74, (-6, 0, 0)), (0.9, (0, 0, 0)))}},
        sym("arm", Tk((0, (-10, 0, 0)), (0.14, (30, 0, 0)), (0.3, (-80, 0, 0)), (0.42, (-70, 0, 0)), (0.62, (-20, 0, 0)), (0.9, (-10, 0, 0)))),
        sym("forearm", Tk((0, (-10, 0, 0)), (0.3, (-24, 0, 0)), (0.9, (-10, 0, 0)))),
        sym("hand", hold(-10, 0, 0))))

    # ---- swim: breaststroke, body flat, frog kick
    sw_fr = [(0, (66, 46, -3, 12)), (0.3, (60, 70, -3, 12)), (0.55, (86, 2, -3, 30)), (0.8, (82, 8, -3, 26)), (1.2, (66, 46, -3, 12))]
    lg = legs(1.2, sw_fr, sw_fr)
    lg["body"] = {"position": {k: [0, -g.hip_y * 0.35, 0] for k in ("0.0", "1.2")},
                  "rotation": Tk((0, (74, 0, 0)), (0.6, (78, 0, 0)), (1.2, (74, 0, 0)))}
    for side in ("r", "l"):
        lg["leg_" + side]["position"] = {k: [0, -g.hip_y * 0.35, 0] for k in ("0.0", "1.2")}
    add("swim", 1.2, True, merge(
        lg,
        {"head": {"rotation": Tk((0, (-62, 0, 0)), (0.6, (-66, 0, 0)), (1.2, (-62, 0, 0)))},
         "hair_back": {"rotation": hold(-10, 0, 0)}, "throat": {"position": hold(0, -0.3, -0.3)}},
        sym("arm", Tk((0, (-150, 0, 14)), (0.35, (-60, 0, 46)), (0.7, (-20, 0, 24)), (1.2, (-150, 0, 14)))),
        sym("forearm", Tk((0, (-6, 0, 0)), (0.35, (-30, 0, 0)), (1.2, (-6, 0, 0)))),
        sym("hand", hold(-10, 0, 0))))

    # ---- croak: the vocal sac swells in three pulses
    sac = P.throat_out
    pulses = [(0, REST)]
    for t0 in (0.25, 1.2, 2.15):
        pulses += [(t0, REST), (t0 + 0.18, sac), (t0 + 0.62, sac), (t0 + 0.8, REST)]
    pulses.append((3.0, REST))
    pu = lambda amt: Tk(*[(t, (0, 0, 0)) if v == REST else (t, (0, 0, amt)) for t, v in pulses])
    cr = P.croak_crouch
    add("croak", 3.0, True, merge(
        legs(3.0, [(0, pose(cr)), (3, pose(cr))], [(0, pose(cr)), (3, pose(cr))]),
        {"throat": {"position": Tk(*pulses)},
         "belly": {"position": Tk(*[(t, REST if v == REST else (0, 0, -P.puff_f * 0.6)) for t, v in pulses])},
         "head": {"rotation": Tk((0, (-6, 0, 0)), (1.5, (-12, 0, 0)), (3, (-6, 0, 0)))},
         "eyelids": {"position": Tk(*[(t, REST if v == REST else OUT) for t, v in pulses])},
         "hair_back": {"rotation": Tk((0, (0, 0, 0)), (1.5, (-4, 0, 0)), (3, (0, 0, 0)))}},
        sym("arm", hold(*ARM_REST)), sym("forearm", hold(-8, 0, 0))))

    # ---- tongue: lunge, mouth opens, the tongue snaps out and back, a gulp
    add("tongue", 1.6, True, merge(
        legs(1.6, [(0, pose(c + 6)), (0.5, pose(c + 18)), (1.6, pose(c + 6))], [(0, pose(c + 6)), (0.5, pose(c + 18)), (1.6, pose(c + 6))]),
        {"mouth": {"position": Tk((0, REST), (0.1, REST), (0.16, OUT), (0.5, OUT), (0.58, REST), (1.6, REST))},
         "tongue": {"position": Tk((0, REST), (0.16, REST), (0.24, (0, 0, -P.tongue_len)), (0.36, (0, 0, -P.tongue_len)), (0.5, REST), (1.6, REST))},
         "head": {"rotation": Tk((0, (0, 0, 0)), (0.14, (-10, 0, 0)), (0.3, (12, 0, 0)), (0.6, (0, 0, 0)), (1.6, (0, 0, 0)))},
         "eyelids": {"position": Tk((0, REST), (0.6, REST), (0.64, OUT), (0.82, OUT), (0.88, REST), (1.6, REST))},
         "throat": {"position": Tk((0, REST), (0.6, REST), (0.72, (0, -0.8, -0.9)), (0.95, REST), (1.6, REST))}},
        sym("arm", hold(*ARM_REST)), sym("forearm", hold(-8, 0, 0))))

    # ---- sit: folded legs, hands on the knees
    sc = P.sit_crouch
    add("sit", 4.0, True, merge(
        legs(4.0, [(0, pose(sc)), (4, pose(sc))], [(0, pose(sc)), (4, pose(sc))], foot=1.0),
        {"head": {"rotation": Tk((0, (-6, 0, 0)), (2, (-8, 6, 1)), (4, (-6, 0, 0)))},
         "throat": {"position": Tk((0, REST), (1, (0, -0.35, -0.4)), (2, REST), (3, (0, -0.35, -0.4)), (4, REST))},
         "hair_back": {"rotation": hold(2, 0, 0)}},
        H.blink(1.6, 3.2), sym("arm", hold(-24, 0, 0)), sym("forearm", hold(-30, 0, 0)), sym("hand", hold(-10, 0, 0))))
