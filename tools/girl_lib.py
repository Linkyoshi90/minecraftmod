"""Shared engine for the monster-girl families after the harpies and anurans (see tools/family_concepts.py):
nekomata (cats), kitsune (foxes), lamia (snakes), arachne (spiders), centaurs (hoofed), merfolk (fish tails), slimes.

Built on harpy_lib (texture packer, painters, cubes, preview renderer, writers), which stays untouched. Every family
shares the harpies' upper body and face (same head, eyes, hair, arms), so the whole mod keeps one art style, and adds
one of six lower bodies:

    "legs"     fur legs with paws, plus a cat tail (tail, tail_mid, tail_tip; tail2_* is the hidden second tail)
               or up to nine fox tails (tail_1..tail_9; the renderer shows as many as her bond allows)
    "serpent"  a long tail of eight segments seg_1..seg_8 lying along the ground
    "spider"   thorax, abdomen and eight legs leg1_r..leg4_l (each with a knee bone legNb_*)
    "taur"     a four-legged lower body: barrel, leg_f_r/f_l/b_r/b_l with shin_* (hooves on the shins), tail
    "fish"     a fish or sea-mammal tail fin_1..fin_4 and a fluke
    "slime"    a jelly gown (gown) around a glowing core (core)

Shared bones, the same names everywhere so the Java side can treat every family alike: root, body, belly (hidden until
she is expecting, like the harpies'), head, eyelids, mouth, hair_back, hair_side_r/l, ear_r/l (animal ears; human
ones are cubes on the head), arm_r/l, forearm_r/l, hand_r/l.

Animations every family has: idle, walk, run, sit, lie (sharing a bed). Each family adds its own (see FAMILY_ANIMS).
Same format facts and detail level as harpy_lib: 4 texels per unit, 1 unit = 1/16 block, the model faces -Z.
"""
import json
import math
import os
import sys
from types import SimpleNamespace

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import hairstyles as HS  # noqa: E402
import outfits as OF  # noqa: E402
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD  # noqa: E402,F401

D = H.D
ASSETS = os.path.join(H.REPO, "src", "main", "resources", "assets", "monstergirls")
PREVIEWS = os.path.join(H.REPO, "previews")
OUT_, REST = H.OUT_, H.REST


# ----------------------------------------------------------------------------- colours
def hx(s):
    s = s.lstrip("#")
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16))


def mix(a, b, f):
    return tuple(int(round(a[i] + (b[i] - a[i]) * f)) for i in range(3))


def shades(base, n=6, dark=0.42, light=0.55, nodither=False):
    """a dark -> light ramp around a base colour (hex string or rgb tuple); the base sits a little above the middle"""
    c = hx(base) if isinstance(base, str) else base
    out = []
    for i in range(n):
        t = i / (n - 1)
        if t < 0.6:
            f = t / 0.6
            col = mix(tuple(int(v * dark) for v in c), c, f)
        else:
            f = (t - 0.6) / 0.4
            col = mix(c, (255, 255, 255), f * light)
        out.append(rgb(*col))
    return ramp(out, nodither)


def solid(c):
    return rgb(*(hx(c) if isinstance(c, str) else c))


def with_alpha(c, a):
    return (c[0], c[1], c[2], a)


# ----------------------------------------------------------------------------- palette
def palette(**kw):
    """the harpy face palette (H.Pal) plus the family knobs. Colours may be given as hex strings."""
    p = H.Pal()
    p.FUR = shades("#b07a40")
    p.LIGHT = shades("#efe0c4")
    p.MARK = shades("#2a2018")
    p.CLOTH = shades("#6a4a8a")
    p.TRIM = shades("#d8b050")
    p.INNER = shades("#e8a8a0")          # inside the ears, toe beans
    p.CLAW = [rgb(40, 34, 30), rgb(80, 70, 62)]
    p.pattern = "solid"
    p.pattern_scale = 1.0
    p.facemark = None
    p.extra = {}
    p.jelly_alpha = 255
    for k, v in kw.items():
        if k in ("HAIR", "FUR", "LIGHT", "MARK", "CLOTH", "TRIM", "INNER", "EYE", "SKIN"):
            if isinstance(v, (str, tuple)) and not isinstance(v, H.Ramp):
                v = shades(v, nodither=(k == "SKIN"))
            if k == "EYE":
                v = ramp(list(v)[:5]) if len(v) > 5 else v
        if k in ("PUPIL", "BLUSH", "BLUSH_L", "LIP", "LIP_D", "EYE_OUT") and isinstance(v, str):
            v = solid(v)
        setattr(p, k, v)
    p.BROW = p.HAIR[1]
    return p


SKIN_TONES = {
    "fair": "#f2c9a8", "peach": "#eebc98", "tan": "#d9a37a", "olive": "#c99a6c", "brown": "#a8724c", "deep": "#7a4e34",
    "pale": "#f6dccb", "rosy": "#f0bca8", "grey": "#cdbfb8", "blue": "#a8c4dc", "lilac": "#d8bce0",
}


def skin(name):
    return shades(SKIN_TONES.get(name, name), n=4, dark=0.78, light=0.35, nodither=True)


# ----------------------------------------------------------------------------- parts
UPPER = [
    ("head", 8, 8, 8), ("hair_cap", 8, 8, 8, ("down",)), ("ear", 1, 2, 1.5),
    ("eyelids", 6.5, 3.5, .25), ("mouth", 1.75, 1.5, .25),
    ("bang_a", 2, 3, .5), ("bang_b", 2, 4.5, .5), ("bang_c", 2, 3.5, .5), ("bang_e", 2, 5.5, .5), ("frame", 1, 7, .5),
    ("strand_s1", 1.5, 7, 1), ("strand_s2", 1.5, 7, 1), ("strand_m1", 1.5, 10, 1), ("strand_m2", 1.5, 10, 1),
    ("strand_l1", 1.5, 13, 1), ("strand_l2", 1.5, 13, 1),
    ("hips", 5, 1.25, 3.5), ("midriff", 3.5, 2.75, 2.5), ("chest", 5, 3, 3.25), ("bust", 2, 1.5, .75), ("collar", 3.5, 1, 2.75),
    ("sash", 5.5, 1.25, 4), ("pendant", 1, 1, .5), ("cord", 3, .5, .5),
    ("arm_up", 1.5, 3.5, 1.5), ("forearm", 1.5, 3, 1.5), ("cuff", 1.75, 1, 1.75), ("palm", 1.5, 1.5, 1), ("fingers", 1.5, 1, 1), ("thumb", .5, 1, .5),
    ("cloth_s", 2, 3.5, .5), ("cloth_m", 2, 5, .5), ("cloth_l", 2, 6.5, .5), ("cloth_side", .5, 4.5, 2.5),
]

EAR_KINDS = {
    "cat": [("ear_a", 2.25, 2.5, .75), ("ear_b", 1.25, 1.25, .75)],
    "big": [("ear_a", 3, 3.5, .75), ("ear_b", 2, 1.75, .75)],
    "fox": [("ear_a", 2.75, 3, .75), ("ear_b", 1.5, 1.75, .75)],
    "round": [("ear_a", 2.25, 1.75, .75), ("ear_b", 1.25, .75, .75)],
    "tuft": [("ear_a", 2.25, 2.75, .75), ("ear_b", 1.25, 1.25, .75), ("ear_t", .5, 2, .5)],
    "horse": [("ear_a", 1.5, 3, .75), ("ear_b", .75, 1, .75)],
    "fin": [("ear_a", .25, 3, 3.5)],
    "gill": [("ear_a", .75, 3.5, .75)],
    # added 2026-10-20 for the new families
    "rabbit": [("ear_a", 2, 6.5, .75), ("ear_b", 1.25, 1.5, .75)],
    "lop": [("ear_a", 2.25, 6, .75), ("ear_b", 1.5, 1.25, .75)],
    "cow": [("ear_a", 2.75, 1.5, 1), ("ear_b", 1, 1, .75)],
    "elf": [("ear_a", .75, 1.5, 3.5), ("ear_b", .5, .75, 1.5)],
    "antenna": [("ear_a", .5, 5, .5), ("ear_b", 1, 1, 1)],
    "plume": [("ear_a", .5, 5.5, .5), ("ear_t", .25, 4, 2.5)],
    # twentieth wave: the house mouse's big round ears
    "mouse": [("ear_a", 3.75, 3.25, .75), ("ear_b", 2.75, 1.25, .75)],
}


def declare(extra_parts=(), ears=None, hair=None, build=None, outfit=None):
    """register the shared upper-body parts, the ear kind, the hairstyle's parts (see hairstyles.py) and the
    family/species parts; then pack the texture"""
    H.PARTS.clear()
    H.RECT.clear()
    for args in UPPER:
        H.part(*args)
    for args in HS.parts(hair or "mid"):
        H.part(*args)
    for args in OF.build_parts(build) + OF.outfit_parts(outfit, build):      # body build + outfit (see outfits.py)
        H.part(*args)
    for args in EAR_KINDS.get(ears, []):
        H.part(*args)
    for args in extra_parts:
        H.part(*args)
    H.part("belly_skin", *H.PARTS["midriff"]["size"])         # the expecting belly: its own smooth skin, no abs or navel stretched over it
    H.pack()


def S(name):
    return H.PARTS[name]["size"]


def has(name):
    return name in H.PARTS


# ----------------------------------------------------------------------------- rig
class Rig:
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


def rot_cube(p, origin, pivot, rx=0, ry=0, rz=0):
    return cube(p, origin, pivot=pivot, rotation=[rx, ry, rz])


def upper(r, W, s, parent="root", z0=0.0):
    """the harpy-style upper body with its waist at height W (the hips' bottom), centred on z0. `s` knobs:
    hair ('short' | 'mid' | 'long' = the old curtain, or a hairstyles.STYLES name), bangs (hairstyles.BANGS, optional), ears (EAR_KINDS key or None for human ears), hips (bool), skirt (bool)."""
    dy = W - 12
    z = z0
    if s.get("outfit") or s.get("build"):
        OF.torso(r, s, W, dy, z, parent)
    else:
        _old_torso(r, s, W, dy, z, parent)
    return _head(r, s, W, dy, z)


def _old_torso(r, s, W, dy, z, parent):
    body = [cube("midriff", [-1.75, 13.25 + dy, -1.25 + z]), cube("chest", [-2.5, 16 + dy, -1.625 + z]),
            cube("collar", [-1.75, 19 + dy, -1.375 + z])]
    if s.get("hips", True):
        body.insert(0, cube("hips", [-2.5, 12 + dy, -1.75 + z]))
    if s.get("sash", True):
        body.append(cube("sash", [-2.75, 12.6 + dy, -2.0 + z]))
    if s.get("pendant", True):
        body += [cube("pendant", [-0.5, 17.6 + dy, -2.45 + z]), cube("cord", [-1.5, 18.55 + dy, -2.2 + z])]
    bust = cube("bust", [-2.25, 16.9 + dy, -2.375 + z])
    body += [bust, mirror_cube(bust)]
    r.add("body", parent, [0, W, z], body)
    # the belly: a copy of the midriff pivoting at its back face, hidden in game until she is expecting
    r.add("belly", "body", [0, 13.25 + dy + 1.375, 1.25 + z], [cube("belly_skin", [-1.75, 13.25 + dy, -1.25 + z])])
    if s.get("skirt"):
        sk = []
        for i, (p, x, rz) in enumerate((("cloth_m", -3.0, 5), ("cloth_l", -1.5, -3), ("cloth_s", -0.2, 2), ("cloth_l", 1.0, -2), ("cloth_m", 2.0, 6))):
            sk.append(hang(p, x, 13.7 + dy, -2.4 - 0.15 * (i % 2) + z, rz, -8))
        for i, (p, x, rz) in enumerate((("cloth_m", -3.0, 3), ("cloth_s", -1.0, -2), ("cloth_l", 0.8, 3), ("cloth_m", 2.0, -3))):
            sk.append(hang(p, x, 13.7 + dy, 2.0 + 0.15 * (i % 2) + z, rz, 8))
        side = cube("cloth_side", [-3.4, 9.2 + dy, -1.2 + z], pivot=[-3.15, 13.7 + dy, z], rotation=[0, 0, -8])
        sk += [side, mirror_cube(side)]
        r.add("skirt", "body", [0, 13.7 + dy, z], sk)


def _head(r, s, W, dy, z):
    hy = 20 + dy
    head = [cube("head", [-4, hy, -4 + z]), cube("hair_cap", [-4, hy, -4 + z], inflate=0.75)]
    if not s.get("ears"):
        e = cube("ear", [-5.0, hy + 1.7, -1.2 + z])
        head += [e, mirror_cube(e)]
    hl = s.get("hair", "mid")
    if hl not in HS.LEGACY:
        finish = HS.build(r, hl, hy, z, head, s.get("bangs"))
        r.add("head", "body", [0, hy, z], head)
        r.add("eyelids", "head", [0, hy + 3.5, -3.8 + z], [cube("eyelids", [-3.25, hy + 1.75, -3.9 + z])])
        r.add("mouth", "head", [0, hy + 1, -3.8 + z], [cube("mouth", [-0.875, hy + 0.25, -3.9 + z])])
        finish()
        return _after_hair(r, s, hy, z, dy, W)
    for i, (p, x, rz) in enumerate((("bang_e", -5.0, -6), ("bang_a", -3.1, 4), ("bang_a", -1.4, -3), ("bang_c", 0.2, 4),
                                    ("bang_a", 1.7, -4), ("bang_b", 3.2, 7))):
        head.append(hang(p, x, hy + 8.4, -5.35 + 0.1 * (i % 2) + z, rz))
    head.append(hang("frame", -4.9, hy + 8, -5.0 + z, -4))
    head.append(mirror_cube(head[-1]))
    r.add("head", "body", [0, hy, z], head)
    r.add("eyelids", "head", [0, hy + 3.5, -3.8 + z], [cube("eyelids", [-3.25, hy + 1.75, -3.9 + z])])
    r.add("mouth", "head", [0, hy + 1, -3.8 + z], [cube("mouth", [-0.875, hy + 0.25, -3.9 + z])])

    sname = {"short": ("strand_s1", "strand_s2"), "mid": ("strand_m1", "strand_s2"), "long": ("strand_l1", "strand_m2")}[hl]
    side = [(sname[0], -6.1, hy + 6.2, -3.4 + z, -6, 0), (sname[1], -6.3, hy + 5.2, -2.0 + z, -9, 0),
            (sname[0], -6.2, hy + 6.2, 0.8 + z, -7, 3), (sname[1], -5.6, hy + 5.0, 3.0 + z, -12, 5)]
    r.add("hair_side_r", "head", [-4.7, hy + 7, z], [hang(*q) for q in side], mirror=True)
    back = []
    names = {"short": ["strand_s1", "strand_s2"], "mid": ["strand_s1", "strand_m1", "strand_s2", "strand_m2"],
             "long": ["strand_m1", "strand_l1", "strand_m2", "strand_l2"]}[hl]
    for i in range(11):
        x = -4.8 + 0.96 * i
        back.append(hang(names[i % len(names)], x, hy + 6.8, 4.9 + 0.3 * (i % 3) + z, x * 1.8, 5 + 2 * (i % 3)))
    r.add("hair_back", "head", [0, hy + 7, 4.5 + z], back)
    return _after_hair(r, s, hy, z, dy, W)


def _after_hair(r, s, hy, z, dy, W):
    ears = s.get("ears")
    if ears:
        add_ears(r, ears, hy, z)

    if s.get("outfit") or s.get("build"):
        OF.arms(r, s, dy, z)
        return SimpleNamespace(W=W, dy=dy, hy=hy, z=z, top=hy + 8.75)
    ay = 19.5 + dy
    r.add("arm_r", "body", [-3.25, ay, z], [cube("arm_up", [-4.0, ay - 3.5, -0.75 + z])], mirror=True)
    fa = [cube("forearm", [-4.0, ay - 6.5, -0.75 + z])]
    if s.get("cuffs", True):
        fa.append(cube("cuff", [-4.125, ay - 6.4, -0.875 + z]))
    r.add("forearm_r", "arm_r", [-3.25, ay - 3.5, z], fa, mirror=True)
    r.add("hand_r", "forearm_r", [-3.25, ay - 6.5, z], [
        cube("palm", [-4.0, ay - 8, -0.5 + z]), cube("fingers", [-4.0, ay - 9, -0.5 + z]), cube("thumb", [-2.5, ay - 8, -0.25 + z]),
    ], mirror=True)
    return SimpleNamespace(W=W, dy=dy, hy=hy, z=z, top=hy + 8.75)


def add_ears(r, kind, hy, z):
    top = hy + 8.6                                   # above the inflated hair cap
    if kind in ("cat", "big", "fox", "round", "tuft", "mouse"):
        aw, ah, ad = S("ear_a")
        bw, bh, bd = S("ear_b")
        x = {"big": -2.9, "mouse": -3.4}.get(kind, -2.6)
        cubes = [("ear_a", [x - aw / 2, top - 0.25, -1.0 + z]), ("ear_b", [x - bw / 2, top - 0.25 + ah, -1.0 + z])]
        if kind == "tuft":
            cubes.append(("ear_t", [x - 0.25, top - 0.25 + ah + bh, -0.875 + z]))
        tilt = {"cat": 10, "big": 8, "fox": 12, "round": 14, "tuft": 8, "mouse": 22}[kind]
        piv = [x, top - 0.25, -0.6 + z]
        r.add("ear_r", "head", piv, [rot_cube(c_p, c_o, piv, 0, 0, tilt) for c_p, c_o in cubes], mirror=True)
    elif kind == "horse":
        aw, ah, ad = S("ear_a")
        x = -3.4
        cubes = [("ear_a", [x - aw / 2, top - 1.0, -0.4 + z]), ("ear_b", [x - 0.375, top - 1.0 + ah, -0.4 + z])]
        piv = [x, top - 1.0, z]
        r.add("ear_r", "head", piv, [rot_cube(c_p, c_o, piv, 0, 0, 32) for c_p, c_o in cubes], mirror=True)
    elif kind == "fin":
        aw, ah, ad = S("ear_a")
        c = cube("ear_a", [-6.6, hy + 3.0, -2.4 + z], pivot=[-6.4, hy + 4.5, -1.6 + z], rotation=[-20, 0, 24])
        r.add("ear_r", "head", [-6.4, hy + 4.5, z], [c], mirror=True)
    elif kind == "gill":
        cs = []
        for i, (yy, ang) in enumerate(((5.6, 35), (4.0, 55), (2.4, 75))):
            cs.append(cube("ear_a", [-6.45, hy + yy - 3, -2.6 + z + i * 0.2], pivot=[-6.2, hy + yy, -2.5 + z], rotation=[-15, 0, ang]))
        r.add("ear_r", "head", [-6.2, hy + 5, z], cs, mirror=True)
    elif kind == "rabbit":                           # tall and upright, leaning out a little
        aw, ah, ad = S("ear_a")
        bw, bh, bd = S("ear_b")
        x = -2.2
        piv = [x, top - 0.5, -0.4 + z]
        cs = [("ear_a", [x - aw / 2, top - 0.5, -0.8 + z]), ("ear_b", [x - bw / 2, top - 0.5 + ah, -0.8 + z])]
        r.add("ear_r", "head", piv, [rot_cube(c_p, c_o, piv, -6, 0, 12) for c_p, c_o in cs], mirror=True)
    elif kind == "lop":                              # floppy ears hanging down beside the head
        aw, ah, ad = S("ear_a")
        bw, bh, bd = S("ear_b")
        x = -3.6
        piv = [x, top - 1.0, z]
        cs = [("ear_a", [x - aw / 2, top - 1.0, -0.4 + z]), ("ear_b", [x - bw / 2, top - 1.0 + ah, -0.4 + z])]
        r.add("ear_r", "head", piv, [rot_cube(c_p, c_o, piv, 8, 0, 160) for c_p, c_o in cs], mirror=True)
    elif kind == "cow":                              # sideways, sticking out below the horns
        aw, ah, ad = S("ear_a")
        piv = [-4.6, hy + 6.2, z]
        cs = [cube("ear_a", [-4.6 - aw, hy + 5.6, -0.5 + z], pivot=piv, rotation=[0, 0, -18]),
              cube("ear_b", [-4.6 - aw + 0.3, hy + 5.85, -0.6 + z], pivot=piv, rotation=[0, 0, -18])]
        r.add("ear_r", "head", piv, cs, mirror=True)
    elif kind == "elf":                              # long and pointed, sweeping back and up from the side of the head
        aw, ah, ad = S("ear_a")
        piv = [-4.3, hy + 3.4, z + 0.2]
        cs = [cube("ear_a", [-4.6, hy + 2.6, z - 0.4], pivot=piv, rotation=[-28, -18, 0]),
              cube("ear_b", [-4.5, hy + 2.85, z + 2.9], pivot=piv, rotation=[-28, -18, 0])]
        r.add("ear_r", "head", piv, cs, mirror=True)
    elif kind in ("antenna", "plume"):              # an insect's antennae from the forehead (plume: a moth's feathery ones)
        aw, ah, ad = S("ear_a")
        piv = [-1.4, top - 0.6, -2.6 + z]
        cs = [cube("ear_a", [-1.4 - aw / 2, top - 0.6, -2.6 - ad / 2 + z], pivot=piv, rotation=[-24, 0, 22])]
        if kind == "antenna":
            cs.append(cube("ear_b", [-1.4 - 0.5, top - 0.6 + ah - 0.25, -2.6 - 0.5 + z], pivot=piv, rotation=[-24, 0, 22]))
        else:
            tw, th, td = S("ear_t")
            cs.append(cube("ear_t", [-1.4 - tw / 2, top - 0.1, -2.6 - td / 2 + z], pivot=piv, rotation=[-24, 0, 22]))
        r.add("ear_r", "head", piv, cs, mirror=True)


# ----------------------------------------------------------------------------- lower bodies
def legs_biped(r, g, s):
    """fur legs with paws (cats, foxes). Hips at 12."""
    cx = -1.25
    tw, th, td = S("thigh")
    r.add("leg_r", "root", [cx, 12, 0], [cube("thigh", [cx - tw / 2, 12 - th, -td / 2])], mirror=True)
    sw, sh, sd = S("shin")
    knee = 12 - th + 0.5
    r.add("shin_r", "leg_r", [cx, knee, 0], [cube("shin", [cx - sw / 2, 1.25, -sd / 2 + 0.25])], mirror=True)
    pw, ph, pd = S("paw")
    foot = [cube("paw", [cx - pw / 2, 0, -pd + 1.25])]
    if has("toe"):
        for i in range(3):
            foot.append(cube("toe", [cx - pw / 2 + 0.125 + i * (pw - 0.25) / 3, 0, -pd + 0.75]))
    r.add("foot_r", "shin_r", [cx, 1.25, 0], foot, mirror=True)


def cat_tail(r, s, name="tail", x=0.0):
    """three segments straight back from the lower back; the rest pose (down, then curling up) is in the animations"""
    a, b, c = S("tail_a"), S("tail_b"), S("tail_c")
    y, z = 12.6, 1.6
    r.add(name, "body", [x, y, z], [cube("tail_a", [x - a[0] / 2, y - a[1] / 2, z])])
    z2 = z + a[2] - 0.25
    r.add(name + "_mid", name, [x, y, z2], [cube("tail_b", [x - b[0] / 2, y - b[1] / 2, z2])])
    z3 = z2 + b[2] - 0.25
    r.add(name + "_tip", name + "_mid", [x, y, z3], [cube("tail_c", [x - c[0] / 2, y - c[1] / 2, z3])])


def fox_tails(r, s, count=9):
    """tail_1 straight back, the others fanned out to the sides (the fan is baked into the cubes)"""
    a, b, c = S("ftail_a"), S("ftail_b"), S("ftail_c")
    y, z = 12.4, 1.5
    fan = [0, -13, 13, -26, 26, -39, 39, -52, 52]
    for i in range(count):
        ry = fan[i]
        rx = 42 - abs(fan[i]) * 0.25
        piv = [0, y, z]
        cs = [rot_cube("ftail_a", [-a[0] / 2, y - a[1] / 2, z], piv, rx, ry),
              rot_cube("ftail_b", [-b[0] / 2, y - b[1] / 2, z + a[2] - 0.5], piv, rx, ry),
              rot_cube("ftail_c", [-c[0] / 2, y - c[1] / 2, z + a[2] + b[2] - 1.0], piv, rx, ry)]
        r.add(f"tail_{i + 1}", "body", piv, cs)


def serpent(r, g, s):
    """eight segments: the first goes down from the waist to the ground, the rest lie along it behind her"""
    w1 = S("seg_1")
    r.add("seg_1", "root", [0, 12, 0], [cube("seg_1", [-w1[0] / 2, 12 - w1[1], -w1[2] / 2])])
    z = w1[2] / 2 - 1.0
    parent = "seg_1"
    for i in range(2, 9):
        n = f"seg_{i}"
        w, h, d = S(n)
        r.add(n, parent, [0, h / 2, z], [cube(n, [-w / 2, 0, z])])
        parent = n
        z += d - 0.25
    return z


def spider(r, g, s):
    tw, th, td = S("thorax")
    r.add("thorax", "root", [0, 9, 0], [cube("thorax", [-tw / 2, 7.25, -td / 2])])
    aw, ah, ad = S("abdomen")
    ab = [cube("abdomen", [-aw / 2, 6.5, td / 2 - 0.75])]
    if has("spinneret"):
        ab.append(cube("spinneret", [-0.75, 8.5, td / 2 - 0.75 + ad - 0.25]))
    r.add("abdomen", "thorax", [0, 9.5, td / 2 - 0.5], ab)
    fl = S("femur")[0]
    tl = S("tibia")[1]
    for i, (lz, ry) in enumerate(((-2.25, -42), (-0.75, -14), (0.75, 14), (2.25, 42))):
        n = i + 1
        px = -tw / 2 + 0.25
        py = 9.25
        ang = 46
        piv = [px, py, lz]
        fem = rot_cube("femur", [px - fl, py - S("femur")[1] / 2, lz - S("femur")[2] / 2], piv, 0, ry, -ang)
        r.add(f"leg{n}_r", "thorax", piv, [fem], mirror=True)
        # knee: where the femur ends, rotated exactly as the renderer / Blockbench rotates the cube
        end = H.rot_about((px - fl, py, lz), piv, H.rot_matrix(*fem["rotation"]))
        kpiv = [round(end[0], 3), round(end[1], 3), round(end[2], 3)]
        ky = kpiv[1]
        tib = rot_cube("tibia", [kpiv[0] - S("tibia")[0] / 2, ky - tl, kpiv[2] - S("tibia")[2] / 2], kpiv, 0, 0, 14)
        cl = rot_cube("tarsus", [kpiv[0] - S("tarsus")[0] / 2, ky - tl - S("tarsus")[1] + 0.25, kpiv[2] - S("tarsus")[2] / 2], kpiv, 0, 0, 14)
        r.add(f"leg{n}b_r", f"leg{n}_r", kpiv, [tib, cl], mirror=True)


def taur(r, g, s):
    bw, bh, bd = S("barrel")
    r.add("barrel", "root", [0, 13, 7.5], [cube("barrel", [-bw / 2, 12, -4.5]), cube("withers", [-S("withers")[0] / 2, 12 + bh - 1.0, -4.0])])
    for name, x, z, up, lo in (("f_r", -2.0, -2.75, "uleg_f", "lleg_f"), ("b_r", -2.0, 8.0, "uleg_b", "lleg_b")):
        uw, uh, ud = S(up)
        lw, lh, ld = S(lo)
        top = 13.0
        r.add("leg_" + name, "barrel", [x, top, z], [cube(up, [x - uw / 2, top - uh, z - ud / 2])], mirror=True)
        knee = top - uh + 0.5
        r.add("shin_" + name, "leg_" + name, [x, knee, z], [cube(lo, [x - lw / 2, 1.25, z - ld / 2]),
                                                             cube("hoof", [x - S("hoof")[0] / 2, 0, z - S("hoof")[2] / 2])], mirror=True)
    tw, th, td = S("htail")
    r.add("tail", "barrel", [0, 17.5, 10.25], [hang("htail", -tw / 2, 17.75, 10.0, 0, 28)])


def fish(r, g, s):
    w1 = S("fin_1")
    r.add("fin_1", "root", [0, 12, 0], [cube("fin_1", [-w1[0] / 2, 12 - w1[1], -w1[2] / 2])])
    z = w1[2] / 2 - 1.0
    parent = "fin_1"
    for i in range(2, 5):
        n = f"fin_{i}"
        w, h, d = S(n)
        r.add(n, parent, [0, h / 2, z], [cube(n, [-w / 2, 0, z])])
        parent = n
        z += d - 0.25
    fw, fh, fd = S("fluke")
    h4 = S("fin_4")[1]
    if fw > fh:     # horizontal fluke (orca, seal, axolotl)
        r.add("fluke", parent, [0, h4 / 2, z], [cube("fluke", [-fw / 2, h4 / 2 - fh / 2, z - 0.5])])
    else:           # vertical tail fin (koi, shark)
        r.add("fluke", parent, [0, h4 / 2, z], [cube("fluke", [-fw / 2, h4 / 2 - fh / 2, z - 0.5])])


def slime(r, g, s):
    gown = []
    y = 0.0
    for p in ("gown_a", "gown_b", "gown_c", "gown_d"):
        w, h, d = S(p)
        gown.append(cube(p, [-w / 2, y, -d / 2 + 0.25]))
        y += h - 0.25
    if has("drip"):
        for x, zz in ((-3.0, -3.2), (2.2, -3.0), (-1.0, 3.4), (3.4, 1.6)):
            gown.append(cube("drip", [x, 0, zz]))
    r.add("gown", "root", [0, 6, 0], gown)
    r.add("core", "body", [0, 17, 0], [cube("core", [-S("core")[0] / 2, 16.25, -S("core")[2] / 2])])


# ----------------------------------------------------------------------------- painting
def fur_t(pal, pattern, x, y, w, h, seed):
    """0..1 'marking strength' of a coat pattern at a texel (0 = plain fur)"""
    sc = pal.pattern_scale
    if pattern == "solid":
        return 0.0
    if pattern == "spots":
        return 1.0 if blob(x, y, seed, int(5 * sc), 1.1 * sc, 1.8 * sc) > 0.15 else 0.0
    if pattern == "smallspots":
        return 1.0 if blob(x, y, seed, int(4 * sc), 0.7 * sc, 1.2 * sc) > 0.2 else 0.0
    if pattern == "rosettes":
        b = blob(x, y, seed, int(7 * sc), 2.0 * sc, 2.8 * sc)
        return 1.0 if 0.12 < b < 0.55 else 0.0
    if pattern == "stripes":       # vertical stripes across a face (tiger, zebra)
        v = math.sin((x + 2.2 * math.sin(y * 0.35 + seed)) * 0.9 / sc + noise(x // 3, y // 3, seed) * 1.2)
        return 1.0 if v > 0.55 else 0.0
    if pattern == "finestripes":
        v = math.sin((x + 1.5 * math.sin(y * 0.4 + seed)) * 1.6 / sc)
        return 1.0 if v > 0.45 else 0.0
    if pattern == "bars":          # horizontal bands (ringed tails, banded snakes)
        v = math.sin(y * 0.9 / sc + noise(x // 4, y // 4, seed) * 0.6)
        return 1.0 if v > 0.5 else 0.0
    if pattern == "bands":
        return 1.0 if (y // max(1, int(3 * sc))) % 3 == 0 else 0.0
    if pattern == "diamonds":
        u = (x + y) % int(8 * sc)
        v = (x - y) % int(8 * sc)
        return 1.0 if (u in (0, 1) or v in (0, 1)) else (0.5 if (u > 2 and v > 2 and (u + v) % 7 == 0) else 0.0)
    if pattern == "net":
        u, v = (x + y // 2) % int(6 * sc), (y + x // 2) % int(6 * sc)
        return 1.0 if (u == 0 or v == 0) else 0.0
    if pattern == "blotches":
        return 1.0 if blob(x, y, seed, int(8 * sc), 2.2 * sc, 3.4 * sc) > 0.1 else 0.0
    if pattern == "patches":
        return 1.0 if noise(x // int(6 * sc), y // int(6 * sc), seed) > 0.62 else 0.0
    if pattern == "dapple":
        return 0.5 if blob(x, y, seed, int(5 * sc), 1.2, 2.0) > 0.3 else 0.0
    if pattern == "speckle":
        return 1.0 if noise(x, y, seed) > 0.86 else 0.0
    if pattern == "cross":
        return 1.0 if abs(x - w // 2) < 2 or abs(y - h // 3) < 2 else 0.0
    return 0.0


def blob(x, y, seed, cell, rmin, rmax):
    cell = max(2, cell)
    best = 0.0
    c0x, c0y = x // cell, y // cell
    for gy in (-1, 0, 1):
        for gx in (-1, 0, 1):
            ix, iy = c0x + gx, c0y + gy
            if noise(ix, iy, seed) < 0.35:
                continue
            px = (ix + 0.2 + 0.6 * noise(ix, iy, seed + 1)) * cell
            py = (iy + 0.2 + 0.6 * noise(ix, iy, seed + 2)) * cell
            rad = rmin + (rmax - rmin) * noise(ix, iy, seed + 3)
            d = math.hypot(x + 0.5 - px, y + 0.5 - py)
            if d < rad:
                best = max(best, 1 - d / rad)
    return best


def fur_fn(pal, light_faces=("down",), base=0.58, grain=0.16, pattern=None, seed=3, light_below=None, fluffy=False):
    """fur: the coat colour with grain, a lighter underside, and the coat pattern on top"""
    pat = pattern if pattern is not None else pal.pattern

    def fn(f, x, y, w, h):
        light = f in light_faces or (light_below is not None and y / max(1, h - 1) > light_below and f in ("north", "east", "west"))
        rmp = pal.LIGHT if light else pal.FUR
        t = base + (noise(x, y, seed) - 0.5) * grain + (0.08 if light else 0)
        if (x + y * 3) % 5 == 0:
            t += 0.06                                  # fur strands
        if f in ("east", "west"):
            t -= 0.05
        elif f in ("south", "down"):
            t -= 0.08
        if fluffy and y >= h - 2 and noise(x, y, seed + 9) > 0.55:
            return CLEAR
        m = 0.0 if light else fur_t(pal, pat, x, y, w, h, seed)
        if m >= 1.0:
            return pick(pal.MARK, 0.35 + (noise(x, y, seed + 4) - 0.5) * 0.2, x, y)
        if m > 0:
            return pick(pal.LIGHT if pat == "dapple" else pal.MARK, 0.65, x, y)
        return pick(rmp, t, x, y)
    return fn


def scale_fn(pal, base=0.56, pattern=None, seed=5, belly_faces=("down",)):
    """overlapping scales (snakes, fish): rows of cells with a highlight and a dark lower edge"""
    pat = pattern if pattern is not None else pal.pattern

    def fn(f, x, y, w, h):
        light = f in belly_faces
        if light:                                    # broad belly plates
            t = 0.62 + (0.15 if y % 3 else -0.18)
            return pick(pal.LIGHT, t, x, y)
        row = y // 2
        off = (row % 2) * 1.5
        cell_x = (x + off) % 3
        t = base + (noise(x, y, seed) - 0.5) * 0.1
        if y % 2 == 1:
            t -= 0.16
        elif cell_x < 1:
            t += 0.16
        m = fur_t(pal, pat, x, y, w, h, seed)
        if m >= 1.0:
            return pick(pal.MARK, t - 0.1, x, y)
        if m > 0:
            return pick(pal.MARK, t + 0.2, x, y)
        return pick(pal.FUR, t, x, y)
    return fn


def chitin_fn(pal, base=0.5, pattern=None, seed=7, hairy=0.0):
    """glossy segmented chitin with fine hairs (spiders)"""
    pat = pattern if pattern is not None else pal.pattern

    def fn(f, x, y, w, h):
        t = base + (noise(x, y, seed) - 0.5) * 0.12
        if f in ("up", "north") and (x, y) in ((w // 3, h // 4), (w // 3 + 1, h // 4)):
            t = 0.98                                 # gloss
        if hairy and noise(x, y, seed + 3) > 1 - hairy:
            return pick(pal.LIGHT, 0.55, x, y)
        m = fur_t(pal, pat, x, y, w, h, seed)
        if m >= 1.0:
            return pick(pal.MARK, 0.55 + (noise(x, y, seed + 1) - 0.5) * 0.2, x, y)
        if m > 0:
            return pick(pal.MARK, 0.8, x, y)
        if f == "down":
            t -= 0.12
        return pick(pal.FUR, t, x, y)
    return fn


def jelly_fn(pal, base=0.55, alpha=None, bubbles=True, seed=11):
    """translucent jelly: a soft vertical gradient, rim light and little bubbles"""
    a = alpha if alpha is not None else pal.jelly_alpha

    def fn(f, x, y, w, h):
        t = base + 0.25 * (1 - y / max(1, h - 1)) + (noise(x, y, seed) - 0.5) * 0.06
        if x < 1 or x > w - 2:
            t += 0.15                                # rim
        if bubbles and noise(x, y, seed + 2) > 0.94:
            return with_alpha(pick(pal.LIGHT, 0.95, x, y), min(255, a + 40))
        m = fur_t(pal, pal.pattern, x, y, w, h, seed)
        col = pick(pal.MARK if m >= 1.0 else pal.FUR, t, x, y)
        return with_alpha(col, a)
    return fn


def cloth_fn(pal, base=0.55, trim_rows=1, pattern=None, seed=13):
    def fn(f, x, y, w, h):
        if y < trim_rows or y >= h - trim_rows:
            return pick(pal.TRIM, 0.6 + (0.2 if x % 3 == 0 else 0), x, y)
        t = base + (noise(x, y, seed) - 0.5) * 0.14 - (0.08 if f in ("east", "west", "south") else 0)
        if pattern == "check" and ((x // 3) + (y // 3)) % 2 == 0:
            t -= 0.18
        elif pattern == "weave" and (x + y) % 4 == 0:
            t -= 0.12
        elif pattern == "dots" and x % 4 == 1 and y % 4 == 1:
            return pick(pal.TRIM, 0.8, x, y)
        return pick(pal.CLOTH, t, x, y)
    return fn


def strip_fn(pal, ramp_, fray=True, seed=17):
    """hanging cloth/leaf strips with a tapered, frayed end (skirt pieces)"""
    def fn(f, x, y, w, h):
        if f not in BROAD:
            return pick(ramp_, 0.4, x, y)
        r_ = h - 1 - y
        tl = 2.0 * D
        if r_ < tl and fray:
            half = (w / 2.0) * (min(1.0, (r_ + 1) / tl) ** 0.8)
            if abs(x + 0.5 - w / 2.0) > half:
                return CLEAR
        if y < D:
            return pick(pal.TRIM, 0.55, x, y)
        t = 0.5 + (noise(x, y, seed) - 0.5) * 0.2 - (0.12 if x in (0, w - 1) else 0)
        return pick(ramp_, t, x, y)
    return fn


def paint_upper(pt, pal, top="wrap", top_pattern=None, outfit=None, build=None):
    """face, hair, skin, the top and sash, cuffs and pendant, skirt strips, ears"""
    H.paint_head(pt, pal)
    H.paint_hair(pt, pal)
    HS.paint(pt, pal)
    H.paint_body_skin(pt, pal)
    pt("belly_skin", "all", H.skin_fn(pal, 0.66))
    if pal.facemark:
        pt("head", ("north",), facemark_fn(pal))
    if top == "wrap":
        pt("chest", "all", cloth_fn(pal, 0.55, 1, top_pattern))
        pt("bust", "all", cloth_fn(pal, 0.6, 0, top_pattern))
    elif top == "skin":
        pt("chest", "all", H.skin_fn(pal, 0.66))
        pt("bust", "all", H.skin_fn(pal, 0.7))
    pt("sash", "all", cloth_fn(pal, 0.45, 1, "weave"))
    pt("cuff", "all", lambda f, x, y, w, h: pick(pal.TRIM, 0.5 + 0.3 * (y == 0), x, y))
    pt("pendant", "all", lambda f, x, y, w, h: pick(pal.TRIM, 0.85 if (f in BROAD and 1 <= x < w - 1 and 1 <= y < h - 1) else 0.45, x, y))
    pt("cord", "all", lambda f, x, y, w, h: pal.TRIM[1])
    for p in ("cloth_s", "cloth_m", "cloth_l"):
        pt(p, "all", strip_fn(pal, pal.CLOTH))
    pt("cloth_side", "all", strip_fn(pal, pal.CLOTH, fray=False))
    OF.paint(pt, pal, outfit, build)                        # outfits.py repaints whatever the outfit covers


def paint_ears(pt, pal, fur=None):
    furf = fur or fur_fn(pal, pattern="solid")

    def inner(f, x, y, w, h):
        if f == "north" and 1 <= x < w - 1 and y >= 1:
            return pick(pal.INNER, 0.55 + (noise(x, y, 3) - 0.5) * 0.2, x, y)
        return furf(f, x, y, w, h)
    pt("ear_a", "all", inner)
    pt("ear_b", "all", lambda f, x, y, w, h: pick(pal.MARK if pal.extra.get("dark_tips") else pal.FUR, 0.45, x, y))
    pt("ear_t", "all", lambda f, x, y, w, h: pick(pal.MARK, 0.3, x, y))


def facemark_fn(pal):
    """small markings painted on the face where there is skin: 'tear' (cheetah), 'whisker' (cats), 'stripe' (tigers),
    'eyes' (spider: four little eyes on the forehead), 'scales' (temples), 'freckle', 'fox' (cheek tufts)"""
    kind = pal.facemark
    M = pal.MARK

    def fn(f, x, y, w, h):
        if kind in ("tear", "tear+whisker") and y >= 22:
            for cx in (10, 21):
                if abs(x - (cx + (y - 22) * (0.15 if cx < 16 else -0.15))) < 0.8 and y <= 30:
                    return M[1]
        if kind in ("whisker", "tear+whisker", "stripe"):
            for sg, x0 in ((-1, 3), (1, 28)):
                if y in (25, 27) and abs(x - x0) <= 1:
                    return M[1]
        if kind == "stripe":
            if (x <= 2 or x >= 29) and y in (16, 17, 20, 21, 24):
                return M[0]
        if kind == "eyes":
            for ex, ey in ((12, 9), (19, 9), (14, 6), (17, 6)):
                if abs(x - ex) <= 0 and abs(y - ey) <= 0:
                    return pal.WHITE
                if abs(x - ex) <= 1 and abs(y - ey) <= 1:
                    return pal.PUPIL
        if kind == "scales" and (x <= 2 or x >= 29) and 12 <= y <= 22 and (x + y) % 2 == 0:
            return M[2]
        if kind == "freckle" and 22 <= y <= 25 and (x in (5, 8, 23, 26)) and y % 2 == 0:
            return M[2]
        if kind == "fox" and y >= 24 and (x <= 1 or x >= 30):
            return pal.LIGHT[3]
        if kind == "panda":                              # dark patches round the eyes, sloping down and out; the eyes themselves stay
            for cx, sg in ((8, -1), (24, 1)):
                ea, eb = getattr(pal, "eye_a", 4.4), getattr(pal, "eye_b", 5.0)
                eye = ((x + 0.5 - cx) / ea) ** 2 + ((y + 0.5 - 18) / eb) ** 2
                patch = ((x + 0.5 - (cx + sg * 1.2)) / 7.4) ** 2 + ((y + 0.5 - 20.5) / 7.0) ** 2
                if patch <= 1.0 and eye > 1.25:
                    return M[0]
        return None
    return fn


# ----------------------------------------------------------------------------- animations
def upper_idle(extra=None):
    Tk = H.track
    return H.merge({
        "body": {"position": Tk((0, (0, 0, 0)), (2, (0, -0.2, 0)), (4, (0, 0, 0)))},
        "head": {"rotation": Tk((0, (0, 0, 0)), (1.2, (-2, 6, 1)), (2.4, (1, -4, -1)), (4, (0, 0, 0)))},
        "hair_back": {"rotation": Tk((0, (0, 0, 0)), (2, (4, 0, 0)), (4, (0, 0, 0)))},
        "skirt": {"rotation": Tk((0, (0, 0, 0)), (2, (2, 0, 0)), (4, (0, 0, 0)))},
        "ear_r": {"rotation": Tk((0, (0, 0, 0)), (2.9, (0, 0, 0)), (3.0, (0, 0, 12)), (3.15, (0, 0, 0)), (4, (0, 0, 0)))},
    }, H.blink(2.7), H.sym("hair_side", Tk((0, (0, 0, 0)), (2, (0, 0, -3)), (4, (0, 0, 0)))),
        H.sym("arm", Tk((0, (0, 0, 3)), (2, (0, 0, 1)), (4, (0, 0, 3)))), H.sym("forearm", H.hold(-8, 0, 0)), H.sym("hand", H.hold(-6, 0, 0)),
        extra or {})


def wave(names, period, amp, axis=1, phase=0.9, keys=8, base=None):
    """a travelling wave down a chain of bones (tails, snake bodies, fish tails); base = per-bone rest rotations"""
    out = {}
    for i, n in enumerate(names):
        a = amp[i] if isinstance(amp, (list, tuple)) else amp
        b0 = base[i] if base else (0, 0, 0)
        pairs = []
        for k in range(keys + 1):
            v = list(b0)
            v[axis] += a * math.sin(2 * math.pi * k / keys - i * phase)
            pairs.append((period * k / keys, tuple(round(q, 2) for q in v)))
        out[n] = {"rotation": H.track(*pairs)}
    return out


def biped_anims(an, tail_bones, extra_tail_bones=(), pounce=True, fox=False):
    """cats and foxes: idle, walk, run, sit, lie, pounce, groom, hiss (+ dig, laugh for foxes)"""
    Tk, sym, hold, merge = H.track, H.sym, H.hold, H.merge
    tail_rest = [(-70, 0, 0), (25, 0, 0), (35, 0, 0)] if not fox else None

    def tail_sway(period, amp, axis=1):
        if fox:
            out = {}
            for i, n in enumerate(tail_bones):
                ph = i * 0.35
                out[n] = {"rotation": Tk((0, (0, amp * math.sin(ph), 0)), (period / 2, (3, -amp * math.sin(ph + 1), 0)), (period, (0, amp * math.sin(ph), 0)))}
            return out
        return wave(tail_bones, period, [amp * 0.5, amp, amp * 1.2], axis=axis, base=tail_rest)

    def twin(d):
        """the second cat tail copies the first, mirrored a little"""
        out = dict(d)
        for a, b in zip(tail_bones, extra_tail_bones):
            if a in d:
                rot = d[a]["rotation"]
                out[b] = {"rotation": {k: [v[0], -v[1] - (12 if a == tail_bones[0] else 0), -v[2]] for k, v in rot.items()}}
        return out

    an.add("idle", 4.0, True, merge(upper_idle(), twin(tail_sway(4.0, 10))))
    # walk: the harpies' stride with arm swing, the tail held up
    swing = Tk((0, (26, 0, 0)), (0.5, (-26, 0, 0)), (1, (26, 0, 0)))
    swing_o = Tk((0, (-26, 0, 0)), (0.5, (26, 0, 0)), (1, (-26, 0, 0)))
    knees = lambda off: Tk((0, (0, 0, 0)), (0.25 + off, (32, 0, 0)), (0.5 + off, (0, 0, 0)), (1, (0, 0, 0))) if off == 0 else Tk((0, (0, 0, 0)), (0.5, (0, 0, 0)), (0.75, (32, 0, 0)), (1, (0, 0, 0)))
    walk = {"leg_r": {"rotation": swing}, "leg_l": {"rotation": swing_o}, "shin_r": {"rotation": knees(0)}, "shin_l": {"rotation": knees(0.5)},
            "arm_r": {"rotation": Tk((0, (-15, 0, 0)), (0.5, (15, 0, 0)), (1, (-15, 0, 0)))},
            "arm_l": {"rotation": Tk((0, (15, 0, 0)), (0.5, (-15, 0, 0)), (1, (15, 0, 0)))},
            "body": {"position": Tk((0, (0, 0, 0)), (0.25, (0, -0.4, 0)), (0.5, (0, 0, 0)), (0.75, (0, -0.4, 0)), (1, (0, 0, 0))),
                     "rotation": Tk((0, (0, 0, 2)), (0.5, (0, 0, -2)), (1, (0, 0, 2)))},
            "hair_back": {"rotation": Tk((0, (0, 0, 0)), (0.25, (6, 0, 0)), (0.5, (0, 0, 0)), (0.75, (6, 0, 0)), (1, (0, 0, 0)))}}
    an.add("walk", 1.0, True, merge(walk, H.blink(0.6), sym("forearm", hold(-15, 0, 0)), twin(tail_sway(1.0, 8))))
    rs = Tk((0, (50, 0, 0)), (0.3, (-50, 0, 0)), (0.6, (50, 0, 0)))
    rs_o = Tk((0, (-50, 0, 0)), (0.3, (50, 0, 0)), (0.6, (-50, 0, 0)))
    an.add("run", 0.6, True, merge({
        "leg_r": {"rotation": rs}, "leg_l": {"rotation": rs_o},
        "shin_r": {"rotation": Tk((0, (10, 0, 0)), (0.15, (60, 0, 0)), (0.3, (10, 0, 0)), (0.6, (10, 0, 0)))},
        "shin_l": {"rotation": Tk((0, (10, 0, 0)), (0.3, (10, 0, 0)), (0.45, (60, 0, 0)), (0.6, (10, 0, 0)))},
        "body": {"rotation": hold(14, 0, 0), "position": Tk((0, (0, 0, 0)), (0.15, (0, 0.8, 0)), (0.3, (0, 0, 0)), (0.45, (0, 0.8, 0)), (0.6, (0, 0, 0)))},
        "head": {"rotation": hold(-12, 0, 0)}, "hair_back": {"rotation": hold(30, 0, 0)},
        "arm_r": {"rotation": Tk((0, (-40, 0, 0)), (0.3, (40, 0, 0)), (0.6, (-40, 0, 0)))},
        "arm_l": {"rotation": Tk((0, (40, 0, 0)), (0.3, (-40, 0, 0)), (0.6, (40, 0, 0)))},
    }, sym("forearm", hold(-50, 0, 0)), sym("hair_side", hold(20, 0, -6)), twin(tail_sway(0.6, 6)) if not fox else
        {n: {"rotation": hold(-20 + 4 * i, 0, 0)} for i, n in enumerate(tail_bones)}))
    # sit: kneeling with the legs folded under (the harpies' pose)
    drop = 6.4
    sit = {"body": {"position": hold(0, -drop, 0), "rotation": hold(4, 0, 0)},
           "leg_r": {"position": hold(0, -drop, 0), "rotation": hold(-84, 0, 0)}, "leg_l": {"position": hold(0, -drop, 0), "rotation": hold(-84, 0, 0)},
           "shin_r": {"rotation": hold(150, 0, 0)}, "shin_l": {"rotation": hold(150, 0, 0)},
           "foot_r": {"rotation": hold(-66, 0, 0)}, "foot_l": {"rotation": hold(-66, 0, 0)},
           "head": {"rotation": Tk((0, (0, 0, 0)), (1, (-3, -18, 0)), (2, (-3, 18, 0)), (3, (0, 0, 0)))}}
    an.add("sit", 3.0, True, merge(sit, H.blink(1.4), sym("arm", hold(10, 0, 4)), twin(tail_sway(3.0, 6))))
    lie = {"root": {"rotation": hold(-90, 0, 0), "position": hold(0, 3, 0)},
           "body": {"position": Tk((0, (0, 0, 0)), (2, (0, 0.15, 0)), (4, (0, 0, 0)))},
           "head": {"rotation": hold(-8, 0, 0)}, "eyelids": {"position": hold(*OUT_)}}
    an.add("lie", 4.0, True, merge(lie, sym("arm", hold(-30, 0, -10)), {n: {"rotation": hold(5, 0, 0)} for n in tail_bones}))
    if pounce:
        an.add("pounce", 1.0, False, merge({
            "body": {"rotation": Tk((0, (0, 0, 0)), (0.35, (30, 0, 0)), (0.55, (-10, 0, 0)), (1, (20, 0, 0))),
                     "position": Tk((0, (0, 0, 0)), (0.35, (0, -3, 0)), (0.55, (0, 2, 0)), (1, (0, -1, 0)))},
            "leg_r": {"rotation": Tk((0, (0, 0, 0)), (0.35, (-60, 0, 0)), (0.55, (40, 0, 0)), (1, (-20, 0, 0)))},
            "leg_l": {"rotation": Tk((0, (0, 0, 0)), (0.35, (-60, 0, 0)), (0.55, (40, 0, 0)), (1, (-20, 0, 0)))},
            "shin_r": {"rotation": Tk((0, (0, 0, 0)), (0.35, (90, 0, 0)), (0.55, (0, 0, 0)), (1, (30, 0, 0)))},
            "shin_l": {"rotation": Tk((0, (0, 0, 0)), (0.35, (90, 0, 0)), (0.55, (0, 0, 0)), (1, (30, 0, 0)))},
            "head": {"rotation": Tk((0, (0, 0, 0)), (0.35, (-20, 0, 0)), (1, (-10, 0, 0)))},
            "mouth": {"position": Tk((0, REST), (0.5, REST), (0.55, OUT_), (0.9, OUT_), (1, REST))},
        }, sym("arm", Tk((0, (0, 0, 0)), (0.35, (-30, 0, 0)), (0.55, (-150, 0, 10)), (1, (-60, 0, 0)))),
            sym("ear", hold(-25, 0, 0))))
    an.add("groom", 2.0, True, merge({
        "head": {"rotation": Tk((0, (10, 0, 0)), (0.5, (14, -8, 0)), (1, (10, 0, 0)), (1.5, (14, -8, 0)), (2, (10, 0, 0)))},
        "arm_r": {"rotation": hold(-70, 20, 0)}, "forearm_r": {"rotation": Tk((0, (-80, 0, 0)), (0.5, (-95, 0, 0)), (1, (-80, 0, 0)), (1.5, (-95, 0, 0)), (2, (-80, 0, 0)))},
        "eyelids": {"position": hold(*OUT_)}, "mouth": {"position": Tk((0, REST), (0.4, OUT_), (0.6, REST), (1.4, OUT_), (1.6, REST), (2, REST))},
    }, sit))
    an.add("hiss", 1.5, True, merge({
        "body": {"rotation": hold(10, 0, 0)}, "head": {"rotation": hold(-6, 0, 0)},
        "mouth": {"position": hold(*OUT_)},
        "hair_back": {"rotation": hold(-8, 0, 0)},
    }, sym("ear", hold(-30, 0, 0)), sym("arm", hold(-50, 0, 18)), sym("forearm", hold(-40, 0, 0)), sym("hand", hold(-30, 0, 0)),
        {n: {"rotation": hold(40 if i == 0 else 10, 0, 0)} for i, n in enumerate(tail_bones)}))
    # stalk: belly low, slow deliberate steps, the tail held straight out behind
    st = Tk((0, (-6, 0, 0)), (0.9, (-40, 0, 0)), (1.8, (-6, 0, 0)))
    st_o = Tk((0, (-40, 0, 0)), (0.9, (-6, 0, 0)), (1.8, (-40, 0, 0)))
    an.add("stalk", 1.8, True, merge({
        "body": {"rotation": hold(28, 0, 0), "position": hold(0, -3.2, 0)}, "head": {"rotation": hold(-26, 0, 0)},
        "leg_r": {"position": hold(0, -3.2, 0), "rotation": st}, "leg_l": {"position": hold(0, -3.2, 0), "rotation": st_o},
        "shin_r": {"rotation": hold(70, 0, 0)}, "shin_l": {"rotation": hold(70, 0, 0)},
        "foot_r": {"rotation": hold(-30, 0, 0)}, "foot_l": {"rotation": hold(-30, 0, 0)},
        "arm_r": {"rotation": Tk((0, (-50, 0, 0)), (0.9, (-20, 0, 0)), (1.8, (-50, 0, 0)))},
        "arm_l": {"rotation": Tk((0, (-20, 0, 0)), (0.9, (-50, 0, 0)), (1.8, (-20, 0, 0)))},
    }, sym("ear", hold(-10, 0, 0)), sym("forearm", hold(-30, 0, 0)),
        {n: {"rotation": hold(-85 if i == 0 else 4, 0, 0)} for i, n in enumerate(tail_bones)} if not fox else
        {n: {"rotation": hold(-35, 0, 0)} for n in tail_bones}))
    if fox:
        an.add("dig", 0.8, True, merge({
            "body": {"rotation": hold(55, 0, 0), "position": hold(0, -2, 0)},
            "head": {"rotation": hold(-30, 0, 0)},
            "arm_r": {"rotation": Tk((0, (-80, 0, 0)), (0.2, (-20, 0, 0)), (0.4, (-80, 0, 0)), (0.8, (-80, 0, 0)))},
            "arm_l": {"rotation": Tk((0, (-80, 0, 0)), (0.4, (-80, 0, 0)), (0.6, (-20, 0, 0)), (0.8, (-80, 0, 0)))},
        }, {n: {"rotation": hold(-30, 0, 0)} for n in tail_bones}))
        an.add("laugh", 1.2, True, merge({
            "body": {"position": Tk((0, (0, 0, 0)), (0.15, (0, 0.5, 0)), (0.3, (0, 0, 0)), (0.45, (0, 0.5, 0)), (0.6, (0, 0, 0)), (1.2, (0, 0, 0)))},
            "head": {"rotation": hold(-14, 0, 0)}, "eyelids": {"position": hold(*OUT_)}, "mouth": {"position": hold(*OUT_)},
        }, sym("arm", hold(-30, 0, 20)), sym("forearm", hold(-90, 0, 0)), tail_sway(0.6, 22)))


def flat_lie(chain, curl):
    """lie for a tail instead of legs: on her back like everyone else (a bed is flat; the old coiled pose floated), shifted
    toward the foot so her head is on the pillow, the waist carrying on toward the foot and the rest of the tail curled up
    flat on the mattress (towards whoever she shares it with)."""
    lie = {"root": {"rotation": H.hold(-90, 0, 0), "position": H.hold(0, 3, -6)}, chain[1]: {"rotation": H.hold(90, 0, 0)},
           "head": {"rotation": H.hold(-8, 0, 0)}, "eyelids": {"position": H.hold(*OUT_)}}
    lie.update({s_: {"rotation": H.hold(0, curl, 0)} for s_ in chain[2:]})
    return lie


def serpent_anims(an, segs, extra=None):
    """lamia: idle, walk/run (slither), sit (coiled), lie (coiled flat, head down), rear, coil (constrict), hiss, bask"""
    Tk, sym, hold, merge = H.track, H.sym, H.hold, H.merge
    n = len(segs)
    rest = [(0, 0, 0)] * n
    an.add("idle", 4.0, True, merge(upper_idle(), wave(segs[1:], 4.0, [3 + i for i in range(n - 1)], axis=1, phase=0.7)))
    an.add("walk", 1.2, True, merge(upper_idle(), wave(segs[1:], 1.2, [10 + 2 * i for i in range(n - 1)], axis=1, phase=0.9),
                                    {"body": {"rotation": Tk((0, (0, 4, 0)), (0.6, (0, -4, 0)), (1.2, (0, 4, 0)))}}))
    an.add("run", 0.7, True, merge(wave(segs[1:], 0.7, [14 + 3 * i for i in range(n - 1)], axis=1, phase=1.0),
                                   {"body": {"rotation": hold(18, 0, 0)}, "head": {"rotation": hold(-14, 0, 0)}, "hair_back": {"rotation": hold(24, 0, 0)}},
                                   sym("arm", hold(30, 0, 8))))
    coil = {s_: {"rotation": hold(0, -48 if i else 0, 0)} for i, s_ in enumerate(segs)}
    coil[segs[0]] = {"rotation": hold(0, 0, 0)}
    an.add("sit", 4.0, True, merge(upper_idle(), coil, {"body": {"position": hold(0, -1.5, 0)}}))
    an.add("lie", 4.0, True, merge(flat_lie(segs, -40), sym("arm", hold(-30, 0, -10))))
    an.add("rear", 1.5, True, merge({
        "body": {"rotation": hold(-14, 0, 0)}, "head": {"rotation": hold(6, 0, 0)},
        "mouth": {"position": Tk((0, REST), (0.6, REST), (0.7, OUT_), (1.1, OUT_), (1.2, REST), (1.5, REST))},
        segs[0]: {"rotation": hold(-12, 0, 0)}, "hood": {"rotation": hold(0, 0, 0), "position": hold(0, 0, 0.6)},
    }, sym("arm", hold(-40, 0, 25)), sym("forearm", hold(-30, 0, 0)), wave(segs[1:], 1.5, [4] * (n - 1), axis=1)))
    an.add("coil", 1.0, True, merge({s_: {"rotation": Tk((0, (0, -60 if i else 0, 0)), (0.5, (0, -66 if i else 0, 0)), (1, (0, -60 if i else 0, 0)))}
                                     for i, s_ in enumerate(segs)}, sym("arm", hold(-60, 0, -20)), {"body": {"rotation": hold(10, 0, 0)}}))
    an.add("hiss", 1.2, True, merge({"mouth": {"position": hold(*OUT_)}, "head": {"rotation": hold(-10, 0, 0)}, "body": {"position": hold(0, 1.5, 0)}},
                                    sym("arm", hold(-30, 0, 20))))
    an.add("bask", 4.0, True, merge({"body": {"rotation": hold(-18, 0, 0)}, "head": {"rotation": hold(-24, 0, 0)}, "eyelids": {"position": hold(*OUT_)}},
                                    sym("arm", hold(-160, 0, 20)), sym("forearm", hold(-40, 0, 0)), wave(segs[1:], 4.0, [2] * (n - 1), axis=1)))
    if extra:
        extra(an)


def spider_anims(an):
    Tk, sym, hold, merge = H.track, H.sym, H.hold, H.merge
    legs = [f"leg{i}" for i in range(1, 5)]

    def gait(period, amp, lift):
        out = {}
        for i, l in enumerate(legs):
            for side, ph in (("_r", 0.0), ("_l", 0.5)):
                p = (ph + (0.5 if i % 2 else 0)) % 1.0
                k = [(0, ((0, amp, 0) if p < 0.5 else (0, -amp, 0)))]
                ks = []
                for j in range(5):
                    tm = period * j / 4
                    ang = math.sin(2 * math.pi * (j / 4 + p))
                    ks.append((tm, (0, amp * ang, -lift * max(0.0, math.cos(2 * math.pi * (j / 4 + p))))))
                yaw_sign = 1 if side == "_r" else -1
                out[l + side] = {"rotation": Tk(*[(tm, (v[0], v[1] * yaw_sign, v[2] * yaw_sign)) for tm, v in ks])}
        return out
    an.add("idle", 4.0, True, merge(upper_idle(), {"abdomen": {"rotation": Tk((0, (0, 0, 0)), (2, (-3, 0, 0)), (4, (0, 0, 0)))}}))
    an.add("walk", 0.8, True, merge(upper_idle(), gait(0.8, 14, 8)))
    an.add("run", 0.45, True, merge(gait(0.45, 18, 12), {"body": {"rotation": hold(10, 0, 0)}}, sym("arm", hold(20, 0, 10))))
    fold = {}
    for l in legs:
        fold[l + "_r"] = {"rotation": hold(0, 0, -25)}
        fold[l + "_l"] = {"rotation": hold(0, 0, 25)}
        fold[l + "b_r"] = {"rotation": hold(0, 0, -40)}
        fold[l + "b_l"] = {"rotation": hold(0, 0, 40)}
    an.add("sit", 4.0, True, merge(upper_idle(), fold, {"thorax": {"position": hold(0, -4, 0)}, "body": {"position": hold(0, -4, 0)}}))
    an.add("lie", 4.0, True, merge(fold, {"thorax": {"position": hold(0, -4.5, 0)}, "body": {"position": hold(0, -4.5, 0), "rotation": hold(-60, 0, 0)},
                                          "eyelids": {"position": hold(*OUT_)}, "head": {"rotation": hold(10, 0, 0)}}, sym("arm", hold(-40, 0, -10))))
    an.add("leap", 0.8, False, merge({"thorax": {"position": Tk((0, (0, 0, 0)), (0.25, (0, -2.5, 0)), (0.4, (0, 2, 0)), (0.8, (0, 0, 0)))},
                                      "body": {"position": Tk((0, (0, 0, 0)), (0.25, (0, -2.5, 0)), (0.4, (0, 2, 0)), (0.8, (0, 0, 0)))}},
                                     {l + s_: {"rotation": Tk((0, (0, 0, 0)), (0.25, (0, 0, -25 if s_ == "_r" else 25)), (0.4, (0, 0, 20 if s_ == "_r" else -20)), (0.8, (0, 0, 0)))}
                                      for l in legs for s_ in ("_r", "_l")}, sym("arm", Tk((0, (0, 0, 0)), (0.4, (-120, 0, 10)), (0.8, (0, 0, 0))))))
    an.add("spin", 2.0, True, merge({"abdomen": {"rotation": Tk((0, (10, 0, 0)), (0.5, (14, 6, 0)), (1, (10, 0, 0)), (1.5, (14, -6, 0)), (2, (10, 0, 0)))},
                                     "head": {"rotation": hold(14, 0, 0)}},
                                    sym("arm", Tk((0, (-60, 0, 10)), (0.5, (-70, 0, 0)), (1, (-60, 0, 10)), (1.5, (-70, 0, 0)), (2, (-60, 0, 10)))),
                                    sym("forearm", hold(-40, 0, 0))))
    # dig: front legs scraping at the ground, body tipped forward
    an.add("dig", 0.8, True, merge({"thorax": {"rotation": hold(12, 0, 0), "position": hold(0, -1.5, 0)}, "body": {"rotation": hold(30, 0, 0), "position": hold(0, -1.5, 0)},
                                    "leg1_r": {"rotation": Tk((0, (0, -20, -10)), (0.4, (0, -50, 20)), (0.8, (0, -20, -10)))},
                                    "leg1_l": {"rotation": Tk((0, (0, 50, -20)), (0.4, (0, 20, 10)), (0.8, (0, 50, -20)))}},
                                   sym("arm", Tk((0, (-70, 0, 0)), (0.4, (-30, 0, 0)), (0.8, (-70, 0, 0))))))
    an.add("rear", 1.5, True, merge({"thorax": {"rotation": hold(-22, 0, 0)}, "mouth": {"position": hold(*OUT_)},
                                     "leg1_r": {"rotation": hold(0, -20, -45)}, "leg1_l": {"rotation": hold(0, 20, 45)},
                                     "leg2_r": {"rotation": hold(0, -10, -30)}, "leg2_l": {"rotation": hold(0, 10, 30)}},
                                    sym("arm", hold(-110, 0, 30))))


def taur_anims(an):
    Tk, sym, hold, merge = H.track, H.sym, H.hold, H.merge

    def legs4(period, amp, knee):
        out = {}
        phases = {"f_r": 0.0, "b_l": 0.0, "f_l": 0.5, "b_r": 0.5}
        for l, ph in phases.items():
            ks, kk = [], []
            for j in range(9):
                tm = period * j / 8
                a = math.sin(2 * math.pi * (j / 8 + ph))
                ks.append((tm, (round(amp * a, 1), 0, 0)))
                kk.append((tm, (round(knee * max(0.0, math.cos(2 * math.pi * (j / 8 + ph))), 1), 0, 0)))
            out["leg_" + l] = {"rotation": Tk(*ks)}
            out["shin_" + l] = {"rotation": Tk(*kk)}
        return out
    an.add("idle", 4.0, True, merge(upper_idle(), {"tail": {"rotation": Tk((0, (0, 0, 0)), (1, (0, 8, 0)), (2, (0, 0, 0)), (3, (0, -8, 0)), (4, (0, 0, 0)))}}))
    an.add("walk", 1.2, True, merge(upper_idle(), legs4(1.2, 18, 30), {"barrel": {"position": Tk((0, (0, 0, 0)), (0.3, (0, -0.3, 0)), (0.6, (0, 0, 0)), (0.9, (0, -0.3, 0)), (1.2, (0, 0, 0)))}}))
    gal = legs4(0.6, 34, 55)
    an.add("run", 0.6, True, merge(gal, {"barrel": {"rotation": Tk((0, (-4, 0, 0)), (0.3, (4, 0, 0)), (0.6, (-4, 0, 0))),
                                                    "position": Tk((0, (0, 0, 0)), (0.15, (0, 1, 0)), (0.3, (0, 0, 0)), (0.45, (0, 1, 0)), (0.6, (0, 0, 0)))},
                                         "body": {"rotation": hold(10, 0, 0)}, "hair_back": {"rotation": hold(30, 0, 0)}, "tail": {"rotation": hold(50, 0, 0)}},
                                   sym("arm", Tk((0, (-30, 0, 0)), (0.3, (20, 0, 0)), (0.6, (-30, 0, 0)))), sym("forearm", hold(-60, 0, 0))))
    fold = {"barrel": {"position": hold(0, -8.5, 0)}, "body": {"position": hold(0, -8.5, 0)}}
    for l in ("f_r", "f_l"):
        fold["leg_" + l] = {"rotation": hold(-70, 0, 0)}
        fold["shin_" + l] = {"rotation": hold(140, 0, 0)}
    for l in ("b_r", "b_l"):
        fold["leg_" + l] = {"rotation": hold(70, 0, 0)}
        fold["shin_" + l] = {"rotation": hold(-140, 0, 0)}
    an.add("sit", 4.0, True, merge(upper_idle(), fold))
    an.add("lie", 4.0, True, merge(fold, {"barrel": {"position": hold(0, -8.5, 0), "rotation": hold(0, 0, 8)},
                                          "body": {"position": hold(0, -8.5, 0), "rotation": hold(-30, 0, -8)}, "eyelids": {"position": hold(*OUT_)}},
                                   sym("arm", hold(-30, 0, -10))))
    an.add("rear", 1.6, False, merge({"barrel": {"rotation": Tk((0, (0, 0, 0)), (0.5, (-38, 0, 0)), (1.1, (-38, 0, 0)), (1.6, (0, 0, 0))),
                                                 "position": Tk((0, (0, 0, 0)), (0.5, (0, 3, 0)), (1.1, (0, 3, 0)), (1.6, (0, 0, 0)))},
                                      "body": {"rotation": Tk((0, (0, 0, 0)), (0.5, (-38, 0, 0)), (1.1, (-38, 0, 0)), (1.6, (0, 0, 0))),
                                               "position": Tk((0, (0, 0, 0)), (0.5, (0, 3, 0)), (1.1, (0, 3, 0)), (1.6, (0, 0, 0)))},
                                      "leg_f_r": {"rotation": Tk((0, (0, 0, 0)), (0.5, (-50, 0, 0)), (0.8, (-20, 0, 0)), (1.1, (-50, 0, 0)), (1.6, (0, 0, 0)))},
                                      "leg_f_l": {"rotation": Tk((0, (0, 0, 0)), (0.5, (-20, 0, 0)), (0.8, (-50, 0, 0)), (1.1, (-20, 0, 0)), (1.6, (0, 0, 0)))},
                                      "mouth": {"position": Tk((0, REST), (0.4, OUT_), (1.2, OUT_), (1.4, REST))}},
                                     sym("arm", Tk((0, (0, 0, 0)), (0.5, (-150, 0, 20)), (1.1, (-150, 0, 20)), (1.6, (0, 0, 0))))))
    an.add("kick", 0.8, False, merge({"barrel": {"rotation": Tk((0, (0, 0, 0)), (0.3, (14, 0, 0)), (0.5, (14, 0, 0)), (0.8, (0, 0, 0)))},
                                      "leg_b_r": {"rotation": Tk((0, (0, 0, 0)), (0.3, (60, 0, 0)), (0.5, (60, 0, 0)), (0.8, (0, 0, 0)))},
                                      "leg_b_l": {"rotation": Tk((0, (0, 0, 0)), (0.3, (60, 0, 0)), (0.5, (60, 0, 0)), (0.8, (0, 0, 0)))},
                                      "head": {"rotation": Tk((0, (0, 0, 0)), (0.3, (0, 60, 0)), (0.6, (0, 60, 0)), (0.8, (0, 0, 0)))}}))
    an.add("graze", 3.0, True, merge({"body": {"rotation": hold(70, 0, 0)}, "head": {"rotation": Tk((0, (20, 0, 0)), (0.5, (26, 0, 0)), (1, (20, 0, 0)), (3, (20, 0, 0)))},
                                      "mouth": {"position": Tk((0, REST), (0.4, OUT_), (0.6, REST), (1.4, OUT_), (1.6, REST), (3, REST))}},
                                     sym("arm", hold(-40, 0, 0))))


def fish_anims(an):
    Tk, sym, hold, merge = H.track, H.sym, H.hold, H.merge
    fins = ["fin_1", "fin_2", "fin_3", "fin_4", "fluke"]
    an.add("idle", 4.0, True, merge(upper_idle(), wave(fins[1:], 4.0, [3, 4, 5, 6], axis=1, phase=0.6)))
    # on land: a slow crawl on the arms, the tail flopping
    an.add("walk", 1.6, True, merge({"body": {"rotation": hold(40, 0, 0), "position": hold(0, -3, 0)}, "head": {"rotation": hold(-30, 0, 0)},
                                     "arm_r": {"rotation": Tk((0, (-80, 0, 0)), (0.8, (-30, 0, 0)), (1.6, (-80, 0, 0)))},
                                     "arm_l": {"rotation": Tk((0, (-30, 0, 0)), (0.8, (-80, 0, 0)), (1.6, (-30, 0, 0)))}},
                                    wave(fins[1:], 1.6, [8, 10, 12, 14], axis=0, phase=0.8)))
    swim_base = {"root": {"rotation": hold(80, 0, 0), "position": hold(0, 6, 0)}, "head": {"rotation": hold(-60, 0, 0)}, "hair_back": {"rotation": hold(40, 0, 0)}}
    inline = [(0, 0, 0), (-90, 0, 0), (0, 0, 0), (0, 0, 0), (0, 0, 0)]      # fold the tail into one line with the body
    an.add("swim", 1.0, True, merge(swim_base, wave(fins, 1.0, [6, 10, 14, 18, 22], axis=0, phase=0.8, base=inline),
                                    sym("arm", hold(30, 0, 10)), sym("hair_side", hold(30, 0, -6))))
    an.add("run", 0.6, True, merge(swim_base, wave(fins, 0.6, [8, 12, 18, 22, 28], axis=0, phase=0.9, base=inline), sym("arm", hold(10, 0, 4))))
    curl = {f_: {"rotation": hold(0, -50 if i else 0, 0)} for i, f_ in enumerate(fins)}
    an.add("sit", 4.0, True, merge(upper_idle(), curl, {"body": {"position": hold(0, -1.5, 0)}}))
    an.add("lie", 4.0, True, merge(flat_lie(fins, -30), sym("arm", hold(-30, 0, -10))))
    notes = [(0, REST), (0.2, OUT_), (0.9, OUT_), (1.1, REST), (1.5, REST), (1.7, OUT_), (2.2, OUT_), (2.4, REST), (3, REST)]
    an.add("sing", 3.0, True, merge(upper_idle(), {"mouth": {"position": Tk(*notes)}, "eyelids": {"position": hold(*OUT_)},
                                                   "head": {"rotation": Tk((0, (-14, 0, -4)), (1.5, (-12, 0, 4)), (3, (-14, 0, -4)))}},
                                    sym("arm", Tk((0, (-22, 0, 8)), (1.5, (-26, 0, 10)), (3, (-22, 0, 8)))), sym("forearm", hold(-80, 0, 0))))
    # dig: bent over the sea floor, sifting sand for shells
    an.add("dig", 1.0, True, merge({"body": {"rotation": hold(60, 0, 0), "position": hold(0, -3, 0)}, "head": {"rotation": hold(-30, 0, 0)},
                                    "arm_r": {"rotation": Tk((0, (-90, 0, 0)), (0.25, (-40, 0, 10)), (0.5, (-90, 0, 0)), (1, (-90, 0, 0)))},
                                    "arm_l": {"rotation": Tk((0, (-90, 0, 0)), (0.5, (-90, 0, 0)), (0.75, (-40, 0, -10)), (1, (-90, 0, 0)))}},
                                   wave(fins[1:], 1.0, [3, 4, 5, 6], axis=1, phase=0.6)))
    # laugh: a playful roll and a grin (the orcas and dolphins teasing)
    an.add("laugh", 1.2, True, merge({"body": {"rotation": Tk((0, (0, 0, -10)), (0.6, (0, 0, 10)), (1.2, (0, 0, -10)))},
                                      "head": {"rotation": hold(-14, 0, 0)}, "eyelids": {"position": hold(*OUT_)}, "mouth": {"position": hold(*OUT_)}},
                                     sym("arm", hold(-30, 0, 20)), sym("forearm", hold(-90, 0, 0)), wave(fins[1:], 0.6, [10, 14, 18, 22], axis=0, phase=0.9)))


def slime_anims(an):
    Tk, sym, hold, merge = H.track, H.sym, H.hold, H.merge
    wob = lambda p, a: Tk((0, (0, 0, 0)), (p / 4, (0, -a, 0)), (p / 2, (0, 0, 0)), (3 * p / 4, (0, a * 0.6, 0)), (p, (0, 0, 0)))
    an.add("idle", 3.0, True, merge(upper_idle(), {"gown": {"position": wob(3.0, 0.4)}, "body": {"position": wob(3.0, 0.3)},
                                                   "core": {"rotation": Tk((0, (0, 0, 0)), (1.5, (0, 180, 0)), (3, (0, 360, 0)))}}))
    an.add("walk", 0.8, True, merge(upper_idle(), {"gown": {"position": Tk((0, (0, 0, 0)), (0.2, (0, -0.8, 0)), (0.4, (0, 0.6, 0)), (0.8, (0, 0, 0)))},
                                                   "body": {"position": Tk((0, (0, 0, 0)), (0.2, (0, -0.6, 0)), (0.4, (0, 0.8, 0)), (0.8, (0, 0, 0)))}}))
    an.add("run", 0.5, True, merge({"gown": {"position": Tk((0, (0, 0, 0)), (0.12, (0, -1.2, 0)), (0.25, (0, 2, 0)), (0.5, (0, 0, 0)))},
                                    "body": {"position": Tk((0, (0, 0, 0)), (0.12, (0, -1.2, 0)), (0.25, (0, 2, 0)), (0.5, (0, 0, 0))), "rotation": hold(8, 0, 0)}},
                                   sym("arm", hold(20, 0, 20))))
    an.add("sit", 4.0, True, merge(upper_idle(), {"gown": {"position": hold(0, -2.5, 0)}, "body": {"position": hold(0, -4, 0)}}))
    an.add("lie", 4.0, True, merge({"gown": {"position": hold(0, -3, 0)}, "body": {"position": hold(0, -5, 0), "rotation": hold(-75, 0, 0)},
                                    "eyelids": {"position": hold(*OUT_)}}, sym("arm", hold(-30, 0, -10))))
    an.add("absorb", 1.2, False, merge({"body": {"position": Tk((0, (0, 0, 0)), (0.3, (0, -1.5, 0)), (0.6, (0, 0.5, 0)), (1.2, (0, 0, 0)))},
                                        "mouth": {"position": Tk((0, REST), (0.2, OUT_), (0.5, OUT_), (0.6, REST))}},
                                       sym("arm", Tk((0, (0, 0, 0)), (0.3, (-70, 0, 20)), (0.8, (-20, 0, 0)), (1.2, (0, 0, 0))))))
    an.add("split", 1.0, False, {"body": {"position": Tk((0, (0, 0, 0)), (0.5, (0, -3, 0)), (1, (0, -6, 0)))},
                                 "gown": {"position": Tk((0, (0, 0, 0)), (0.5, (0, -2, 0)), (1, (0, -4, 0)))}})
    # bounce: squash on landing, stretch on the way up, arms thrown up in delight
    sq = Tk((0, (1.25, 0.7, 1.25)), (0.15, (0.9, 1.2, 0.9)), (0.45, (1.0, 1.05, 1.0)), (0.6, (1.25, 0.7, 1.25)))
    an.add("bounce", 0.6, True, merge({"gown": {"scale": sq, "position": Tk((0, (0, -1, 0)), (0.15, (0, 0.5, 0)), (0.6, (0, -1, 0)))},
                                       "body": {"position": Tk((0, (0, -2.5, 0)), (0.15, (0, 1.2, 0)), (0.45, (0, 0.4, 0)), (0.6, (0, -2.5, 0)))},
                                       "eyelids": {"position": hold(*OUT_)}, "mouth": {"position": hold(*OUT_)}},
                                      sym("arm", Tk((0, (-40, 0, 30)), (0.15, (-160, 0, 20)), (0.6, (-40, 0, 30))))))


# ----------------------------------------------------------------------------- writing
def write(name, tex, model, anim_fn, variants=None, paint_variant=None):
    """write geo / animation / texture, and one texture per colour variant (<name>_<key>.png)"""
    geo = model.doc()
    names = [b["name"] for b in geo["minecraft:geometry"][0]["bones"]]
    an = H.Anims(name, names)
    anim_fn(an)
    adoc = an.doc()
    H.write_png(os.path.join(ASSETS, "textures", "entity", name + ".png"), tex)
    for sub, ext, data in (("geo", ".geo.json", geo), ("animations", ".animation.json", adoc)):
        path = os.path.join(ASSETS, sub, "entity", name + ext)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            json.dump(data, fh, indent=2)
            fh.write("\n")
    texes = {"": tex}
    for key in (variants or []):
        t = paint_variant(key)
        texes[key] = t
        H.write_png(os.path.join(ASSETS, "textures", "entity", f"{name}_{key}.png"), t)
    n_cubes = sum(len(b.get("cubes", [])) for b in geo["minecraft:geometry"][0]["bones"])
    print(f"{name}: texture {H.TEX}x{H.TEX}, {len(names)} bones / {n_cubes} cubes, {len(adoc['animations'])} animations, {len(texes) - 1} variants")
    return geo, adoc, texes


def model_height(geo):
    """the standing height the game scales by (gen_girl_data.model_height skips the same bones)"""
    top = 0.0
    for b in geo["minecraft:geometry"][0]["bones"]:
        if b["name"].startswith(("tail2", "antlers", "horns", "crystals", "sprout", "ear_", "hair", "wing", "halo", "disguise")):
            continue
        for c in b.get("cubes", []):
            top = max(top, c["origin"][1] + c["size"][1])
    return top


def previews(name, geo, adoc, texes, views, face_views, height, variant_rows=None):
    """turnaround/pose sheet, face sheet and the variant lineup in previews/"""
    A = adoc["animations"]
    g = lambda n: A.get(f"animation.{name}.{n}") or A[f"animation.{name}.idle"]
    bg = (44, 48, 44, 255)
    tex = texes[""]
    W = 420
    scale = W * 0.82 / max(32.0, height + 4)
    cy = height * 0.5
    cols = 4
    rows = (len(views) + cols - 1) // cols
    sheet = [[bg] * (W * cols) for _ in range(W * rows)]
    for i, (an_, tm, yaw, pitch) in enumerate(views):
        im = H.render(geo, tex, g(an_), tm, yaw, pitch, W, W, scale, bg, center=(0, cy))
        ox, oy = (i % cols) * W, (i // cols) * W
        for y in range(W):
            sheet[oy + y][ox:ox + W] = im[y]
    os.makedirs(PREVIEWS, exist_ok=True)
    H.write_png(os.path.join(PREVIEWS, name + "_preview.png"), sheet)
    FW = 360
    face = [[bg] * (FW * len(face_views)) for _ in range(FW)]
    head_c = (0, face_views[0][4] if len(face_views[0]) > 4 else 24)
    for i, fv in enumerate(face_views):
        an_, tm, yaw, pitch = fv[:4]
        im = H.render(geo, tex, g(an_), tm, yaw, pitch, FW, FW, 30, bg, center=head_c)
        for y in range(FW):
            face[y][i * FW:(i + 1) * FW] = im[y]
    H.write_png(os.path.join(PREVIEWS, name + "_preview_face.png"), face)
    if len(texes) > 1:
        VW = 300
        vrows = variant_rows or [("idle", 0, -30, 8, VW * 0.8 / max(32.0, height + 4), (0, cy)), ("idle", 0, -15, 0, 24, head_c)]
        vs = [[bg] * (VW * len(texes)) for _ in range(VW * len(vrows))]
        for c, t in enumerate(texes.values()):
            for r_, (an_, tm, yaw, pitch, sc, cen) in enumerate(vrows):
                im = H.render(geo, t, g(an_), tm, yaw, pitch, VW, VW, sc, bg, center=cen)
                for y in range(VW):
                    vs[r_ * VW + y][c * VW:(c + 1) * VW] = im[y]
        H.write_png(os.path.join(PREVIEWS, name + "_variants.png"), vs)
    print("previews ->", name)


def roster(path, entries, cell=340):
    """the family side by side to scale: entries are (geo, tex, idle anim, in-game height in blocks, model height units)"""
    bg = (44, 48, 44, 255)
    tallest = max(e[3] for e in entries)
    px_per_block = cell * 0.85 / tallest
    sheet = [[bg] * (cell * len(entries)) for _ in range(cell)]
    for i, (geo, tex, anim, blocks, units) in enumerate(entries):
        sc = px_per_block * blocks / units
        im = H.render(geo, tex, anim, 0, -28, 8, cell, cell, sc, bg)
        for y in range(cell):
            sheet[y][i * cell:(i + 1) * cell] = im[y]
    H.write_png(path, sheet)
    print("roster ->", path)


# ----------------------------------------------------------------------------- family runner
PAL_KNOBS = ("pattern", "facemark", "extra", "pattern_scale", "pupil", "jelly_alpha")


def pal_from(base, over=None, pupil=None):
    """a palette from a species' default knobs plus a variant's overrides"""
    kw = dict(base)
    if over:
        kw.update(over)
    p = palette(**{k: v for k, v in kw.items() if k not in PAL_KNOBS})
    p.pattern = kw.get("pattern", "solid")
    p.pattern_scale = kw.get("pattern_scale", 1.0)
    p.facemark = kw.get("facemark")
    p.extra = kw.get("extra", {})
    p.jelly_alpha = kw.get("jelly_alpha", 255)
    if kw.get("pupil") or pupil:
        p.pupil = kw.get("pupil") or pupil
    return p


def run_family(fid, species, declare_fn, paint_fn, build_fn, anim_fn, views, face, variant_rows=None):
    """generate every species of a family (or those named on the command line); --previews renders the sheets and roster"""
    import family_concepts as FC
    fam = FC.family(fid)
    want = [a for a in sys.argv[1:] if not a.startswith("--")]
    prev = "--previews" in sys.argv
    entries = []
    for s in fam["species"]:
        sp = s["id"]
        if (want and sp not in want) or sp not in species:
            continue
        declare_fn(sp)
        tex = paint_fn(sp, None)
        keys = list(species[sp]["variants"])
        geo, adoc, texes = write(sp, tex, build_fn(sp), anim_fn, keys, lambda k, sp=sp: paint_fn(sp, k))
        height = model_height(geo)
        if prev:
            previews(sp, geo, adoc, texes, views, face, height, variant_rows)
        entries.append((geo, tex, adoc["animations"][f"animation.{sp}.idle"], s["height"], height))
    if prev and not want:
        roster(os.path.join(PREVIEWS, f"{fid}_roster.png"), entries)
    return entries
