"""Hairstyle library for the girl_lib families (the shared upper body used to give every girl the same curtain of hair).

A style is chosen per species with the `hair` knob of girl_lib.upper(); the old values 'short' / 'mid' / 'long' still
build the original curtain, byte for byte, so families that have not picked a style yet are untouched.

    bob        chin-length, blunt cut, rounded; blunt fringe
    pixie      cropped short, spiky crown, swept fringe
    ponytail   high ponytail swinging behind; parted fringe
    twintails  two long tails tied at the sides of the head, with ribbons
    braid      one thick plait over her right shoulder
    bun        neat high bun, face-framing tendrils, wispy fringe
    mane       huge wild volume flaring out in every direction (lionesses)
    hime       very long straight hair, blunt fringe, chin-length side locks
    wavy       long hair whose locks kink out and back in, swept fringe
    odango     two buns on top with little tails under them
    drills     twin ringlet curls ("drills") at the sides, long hair behind
    sidetail   one side ponytail on her left
    shaggy     layered wolf cut, volume at the crown, ends flicking out
    flowing    swept back off the forehead, streaming out behind her

The bangs can be overridden independently with the `bangs` knob: jagged (the original), blunt, swept, parted, wispy,
messy, none. Bones stay the same as before (head, hair_side_r/l, hair_back) so every animation keeps working; the braid
adds hair_braid and spiky crowns add hair_top (children of the head; bones named hair* never count towards the height
the game scales her to). Ties, ribbons and bows are painted in the palette's TRIM colour.
"""
import math

import harpy_lib as H
from harpy_lib import cube, hang, mirror_cube, pick, noise, CLEAR, BROAD

D = H.D
LEGACY = ("short", "mid", "long")
STYLES = ("bob", "pixie", "ponytail", "twintails", "braid", "bun", "mane", "hime", "wavy", "odango", "drills", "sidetail",
          "shaggy", "flowing")
BANGS = ("jagged", "blunt", "swept", "parted", "wispy", "messy", "none")

# extra parts, declared only when a species uses a new style (so the old textures keep their layout)
PARTS = [
    ("blunt_s", 1.5, 6, 1), ("blunt_l", 1.5, 14, 1), ("fringe", 1.25, 3, .5),
    ("lock_xs", 1.5, 3.5, 1), ("lock_xl", 1.5, 16, 1), ("spike", 1.5, 3, 1),
    ("tail_root", 2.5, 2.5, 3), ("tail_lock", 2, 12, 1.75), ("tail_long", 2, 15, 1.75),
    ("tie", 2.75, 1, 2.75), ("bun", 3.5, 3, 3.5), ("bow", 2, 1.5, .5),
    ("braid_seg", 2, 2, 2), ("braid_tip", 1.5, 3, 1), ("wave_a", 1.5, 6, 1), ("wave_b", 1.5, 6, 1),
    ("curl_a", 3, 2.5, 3), ("curl_b", 2.5, 2.25, 2.5), ("curl_c", 2, 2, 2),
]


def parts(style):
    return PARTS if style not in LEGACY else []


# ----------------------------------------------------------------------------- geometry helpers
def rcube(p, origin, pivot, rx=0, ry=0, rz=0):
    return cube(p, origin, pivot=pivot, rotation=[rx, ry, rz])


def up(p, x, ybase, z, rx=0, rz=0):
    """a cube standing UP from (x+w/2, ybase, z+d/2), tilted about that base point (spikes, buns)"""
    w, h, d = H.PARTS[p]["size"]
    return rcube(p, [x, ybase, z], [x + w / 2, ybase, z + d / 2], rx, 0, rz)


def centred(p, cx, cy, cz, rx=0, ry=0, rz=0):
    w, h, d = H.PARTS[p]["size"]
    return rcube(p, [cx - w / 2, cy - h / 2, cz - d / 2], [cx, cy, cz], rx, ry, rz)


# ----------------------------------------------------------------------------- bangs
def bangs(kind, hy, z):
    """the fringe and the two face-framing locks, as cubes on the head bone"""
    out = []
    if kind == "jagged":                                 # the original fringe
        for i, (p, x, rz) in enumerate((("bang_e", -5.0, -6), ("bang_a", -3.1, 4), ("bang_a", -1.4, -3), ("bang_c", 0.2, 4),
                                        ("bang_a", 1.7, -4), ("bang_b", 3.2, 7))):
            out.append(hang(p, x, hy + 8.4, -5.35 + 0.1 * (i % 2) + z, rz))
    elif kind == "blunt":                                # one straight line above the brows
        for i in range(7):
            out.append(hang("fringe", -4.5 + 1.25 * i, hy + 8.5, -5.3 + 0.08 * (i % 2) + z))
    elif kind == "swept":                                # every lock leaning the same way, longer on one side
        for i, (p, x) in enumerate((("bang_e", -4.9), ("bang_b", -3.5), ("bang_c", -2.0), ("bang_a", -0.5), ("bang_a", 1.0), ("bang_a", 2.4))):
            out.append(hang(p, x, hy + 8.5, -5.35 + 0.1 * (i % 2) + z, 20 - 2 * i))
    elif kind == "parted":                               # a centre parting, forehead showing
        for i, (p, x, rz) in enumerate((("bang_c", -4.4, -16), ("bang_a", -3.0, -26), ("bang_a", -1.6, -38))):
            c = hang(p, x, hy + 8.5, -5.35 + 0.1 * (i % 2) + z, rz)
            out += [c, mirror_cube(c)]
    elif kind == "wispy":                                # a few thin strands
        for i, (p, x, rz) in enumerate((("bang_a", -3.4, 8), ("bang_c", -0.2, -6), ("bang_a", 2.4, -10))):
            out.append(hang(p, x, hy + 8.4, -5.3 + z, rz))
    elif kind == "messy":                                # the original, rumpled
        for i, (p, x, rz, rx) in enumerate((("bang_e", -5.1, -14, -8), ("bang_a", -3.3, 12, -14), ("bang_c", -1.6, -9, -6), ("bang_a", -0.1, 16, -16),
                                            ("bang_b", 1.4, -6, -8), ("bang_a", 2.6, 18, -12), ("bang_c", 3.3, 9, -4))):
            out.append(hang(p, x, hy + 8.6, -5.35 + 0.12 * (i % 3) + z, rz, rx))
    if kind != "none":
        out.append(hang("frame", -4.9, hy + 8, -5.0 + z, -4))
        out.append(mirror_cube(out[-1]))
    else:
        out.append(hang("frame", -4.9, hy + 7.5, -4.6 + z, -2))
        out.append(mirror_cube(out[-1]))
    return out


DEFAULT_BANGS = {"bob": "blunt", "pixie": "swept", "ponytail": "parted", "twintails": "jagged", "braid": "swept",
                 "bun": "wispy", "mane": "messy", "hime": "blunt", "wavy": "swept", "odango": "jagged", "drills": "parted",
                 "sidetail": "swept", "shaggy": "messy", "flowing": "none"}


# ----------------------------------------------------------------------------- styles
def nape(hy, z, n=9, rx=10):
    """short locks covering the back of the neck under an updo / crop"""
    out = []
    for i in range(n):
        x = -4.4 + 8.8 * i / (n - 1) - 0.75
        out.append(hang("lock_xs", x, hy + 3.4 + 0.3 * (i % 2), 4.75 + 0.2 * (i % 3) + z, x * 2.0, rx + 3 * (i % 2)))
    return out


def style_bob(hy, z):
    side = [hang("blunt_s", -5.9, hy + 7.2, -3.4 + z + 1.6 * k, 3 + k, 0) for k in range(4)]
    back = []
    for i in range(11):
        x = -4.8 + 0.96 * i
        back.append(hang("blunt_s", x - 0.75, hy + 7.2, 4.85 + 0.15 * (i % 2) + z, x * 0.5, 6))
    return dict(side=side, back=back)


def style_pixie(hy, z):
    side = [hang("lock_xs", -5.6, hy + 6.6, -2.6 + z, -10), hang("lock_xs", -5.7, hy + 6.8, 0.6 + z, -16, 8)]
    head = []
    for x, zz, rz in ((-3.2, -2.0, 24), (-1.0, -2.6, 6), (1.2, -1.8, -18), (2.6, 0.6, -30), (-2.6, 1.6, 26), (0.0, 2.4, -6), (-0.4, -0.2, 10)):
        head.append(up("spike", x - 0.75, hy + 8.2, zz - 0.5 + z, -zz * 11, rz))
    return dict(side=side, back=nape(hy, z, 9, 14), head=head)


def style_ponytail(hy, z):
    side = [hang("lock_xs", -5.4, hy + 6.4, -2.6 + z, -4)]
    back = nape(hy, z, 7, 8)
    ty, tz = hy + 7.0, 4.4 + z                            # where the tail leaves the head
    back.append(rcube("tail_root", [-1.25, ty - 1.25, tz], [0, ty, tz], 20, 0, 0))
    back.append(centred("tie", 0, ty + 0.2, tz + 0.5, 70, 0, 0))
    for x, rx, rz in ((-1.6, 8, -7), (-0.9, 14, 0), (0.0, 10, 6), (-0.6, 4, 2)):
        back.append(hang("tail_lock", x, ty + 1.6, tz + 2.2, rz, rx))
    return dict(side=side, back=back)


def style_twintails(hy, z):
    side = []
    cx, cy, cz = -6.0, hy + 6.6, 1.6 + z                  # the knot on each side, a little behind the ear
    side.append(centred("tail_root", cx + 0.4, cy, cz, 0, 90, 0))
    side.append(centred("tie", cx - 0.7, cy, cz, 0, 0, 90))
    for x, zz, rx, rz in ((-0.4, -0.5, 4, -6), (0.5, 0.4, 10, -12), (-0.1, 0.9, 16, -4)):
        side.append(hang("tail_long", cx - 1.6 + x, cy + 1.0, cz - 0.9 + zz, rz, rx))
    side.append(centred("bow", cx - 0.9, cy + 1.3, cz - 1.0, 0, 0, 35))      # ribbon loops above and below the tie
    side.append(centred("bow", cx - 0.9, cy - 1.0, cz - 1.0, 0, 0, -35))
    side.append(hang("strand_s1", -5.8, hy + 6.8, -3.0 + z, -4))             # a lock in front of the ear
    back = nape(hy, z, 9, 6) + [hang("strand_s1", -4.6 + 2.3 * i, hy + 6.8, 4.95 + z, (-4.6 + 2.3 * i) * 1.2, 6) for i in range(5)]
    return dict(side=side, back=back)


def style_braid(hy, z):
    """one plait on her right (-x) side, over the shoulder; the rest gathered back into it"""
    back = nape(hy, z, 9, 6)
    for i in range(5):                                    # hair swept from the back of the head into the braid
        back.append(hang("blunt_s", -4.4 + 1.6 * i, hy + 7.4, 4.9 + z, -14 - 3 * i, 18))
    side = [hang("strand_s2", -5.7, hy + 6.6, 0.4 + z, 4, 0)]
    braid = [hang("strand_s1", -5.5, hy + 7.6, -0.4 + z, 6, -18)]          # sweeping down over the ear into the plait
    bx, by, bz = -3.6, hy + 1.6, -1.9 + z
    for i in range(7):
        off = 0.3 if i % 2 else -0.3
        braid.append(centred("braid_seg", bx + off, by - 1.55 * i, bz - 0.06 * i, 6, 25 if i % 2 else -25, off * 20))
    tip_y = by - 1.55 * 6 - 1.0
    braid.append(centred("tie", bx, tip_y, bz - 0.4, 0, 0, 0))
    braid.append(hang("braid_tip", bx - 0.75, tip_y - 0.4, bz - 0.9, 6, 4))
    return dict(side=side, back=back, bones=[("hair_braid", "head", [bx, hy + 2, bz], braid, False)])


def style_bun(hy, z):
    side = [hang("strand_m1", -5.4, hy + 6.4, -3.1 + z, -2, -4)]             # face-framing tendrils
    back = nape(hy, z, 9, 4)
    for i in range(5):                                    # hair drawn up to the crown
        back.append(up("blunt_s", -4.0 + 1.75 * i - 0.75, hy + 1.5, 4.8 + z, 0, (-4.0 + 1.75 * i) * 2))
    back.append(up("bun", -1.75, hy + 8.2, 1.4 + z, -12))
    back.append(centred("tie", 0, hy + 8.75, 3.15 + z, -12, 0, 0))
    return dict(side=side, back=back)


def style_mane(hy, z):
    """a lioness's mane: a dense, shoulder-length layer flaring out and back, with a short puff standing out around it"""
    side = []
    for k, (p, zz, rz, rx) in enumerate((("strand_s1", -3.4, -26, -4), ("wave_a", -1.6, -34, 4), ("strand_m1", 0.2, -24, 10),
                                        ("wave_a", 2.0, -36, 14), ("strand_s2", 3.4, -30, 20))):
        side.append(hang(p, -5.8, hy + 7.8 - 0.3 * (k % 2), zz + z, rz, rx))
    side += [hang("lock_xs", -5.4, hy + 8.4, -2.4 + z, -64, -6), hang("lock_xs", -5.4, hy + 8.6, 0.8 + z, -70, 14),
             hang("lock_xs", -5.6, hy + 5.0, -1.0 + z, -58, 8)]
    back = []
    for i in range(13):                                   # the main layer, shoulder length, fanning out
        x = -5.4 + 0.9 * i
        p = ("strand_m1", "wave_a", "strand_s1", "strand_m2")[i % 4]
        back.append(hang(p, x - 0.75, hy + 8.0, 4.8 + 0.3 * (i % 3) + z, x * 3.0, 20 + 6 * (i % 3)))
    for i in range(9):                                    # the puff: short locks standing straight out
        x = -4.6 + 1.15 * i
        back.append(hang("lock_xs", x - 0.75, hy + 9.0, 4.2 + z, x * 9, 62 + 10 * (i % 2)))
        back.append(hang("lock_xs", x - 0.75 + 0.4, hy + 5.0, 4.9 + z, x * 7, 48 + 8 * (i % 2)))
    head = []
    for x, zz, rz in ((-4.0, -2.6, 30), (4.0, -2.4, -32), (-0.6, 1.8, 8), (1.4, 2.6, -14)):
        head.append(up("spike", x - 0.75, hy + 8.1, zz - 0.5 + z, -zz * 13, rz))
    return dict(side=side, back=back, head=head)


def style_hime(hy, z):
    side = [hang("blunt_s", -5.5, hy + 7.4, -3.3 + z, 0), hang("blunt_s", -5.6, hy + 7.4, -1.8 + z, 0),
            hang("blunt_l", -5.9, hy + 7.0, 0.6 + z, -2, 3), hang("blunt_l", -5.8, hy + 7.0, 2.4 + z, -3, 5)]
    back = []
    for i in range(13):
        x = -5.0 + 0.83 * i
        back.append(hang("blunt_l", x - 0.75, hy + 7.4, 4.85 + 0.12 * (i % 2) + z, x * 0.5, 4))
    return dict(side=side, back=back)


def style_wavy(hy, z):
    """long locks in three tiers, each tier kinking the other way (the zigzag reads as waves)"""
    def lock(x, ytop, zz, rz, rx):
        out = []
        for k, (p, dz, dx) in enumerate((("wave_a", rz, rx), ("wave_b", -rz * 0.9, rx - 12), ("wave_a", rz * 0.8, rx + 2))):
            out.append(hang(p, x, ytop, zz, dz, dx))
            L = 5.6                                      # the next tier starts at this one's tip
            x += L * math.sin(math.radians(dz))
            zz += L * math.sin(math.radians(dx))
            ytop -= L * math.cos(math.radians(dz)) * math.cos(math.radians(dx))
        return out
    side = lock(-6.1, hy + 6.8, -3.0 + z, -10, 0) + lock(-6.2, hy + 6.8, 0.8 + z, -14, 6)
    back = []
    for i in range(9):
        x = -4.8 + 1.2 * i
        back += lock(x - 0.75, hy + 7.0, 4.85 + 0.2 * (i % 2) + z, x * 1.6 + (6 if i % 2 else -6), 10)
    return dict(side=side, back=back)


def style_odango(hy, z):
    side = [up("bun", -4.9, hy + 7.4, -0.8 + z, 0, 16), centred("tie", -3.0, hy + 7.7, 0.95 + z, 0, 0, 16),
            hang("lock_xs", -4.7, hy + 7.4, 0.4 + z, -10, 10), hang("strand_s1", -5.2, hy + 7.2, 1.4 + z, -14, 16),
            hang("strand_s1", -5.8, hy + 6.8, -3.0 + z, -4)]
    back = [hang(("strand_m1", "strand_s1", "strand_m2")[i % 3], -4.8 + 0.96 * i - 0.75, hy + 6.8, 4.9 + 0.3 * (i % 3) + z,
                 (-4.8 + 0.96 * i) * 1.6, 6 + 2 * (i % 3)) for i in range(11)]
    return dict(side=side, back=back)


def style_drills(hy, z):
    """big ringlets hanging beside the cheeks, out in front of the rest of the hair"""
    cx, cz = -6.6, -1.2 + z
    side = [hang("strand_s1", -6.0, hy + 7.6, cz - 0.5, -12), centred("tie", cx + 0.3, hy + 5.6, cz, 0, 0, -8)]
    y = hy + 5.1
    for i, (p, h_) in enumerate((("curl_a", 2.5), ("curl_a", 2.5), ("curl_b", 2.25), ("curl_b", 2.25), ("curl_c", 2))):
        side.append(centred(p, cx - 0.12 * i, y - h_ / 2, cz, 0, 45 if i % 2 else 0, -4))
        y -= h_ * 0.85
    side.append(hang("lock_xs", cx - 0.75 - 0.6, y + 0.4, cz - 0.5, -6))
    back = [hang(("strand_m1", "strand_s1", "strand_m2")[i % 3], -4.8 + 0.96 * i - 0.75, hy + 6.8, 4.9 + 0.3 * (i % 3) + z,
                 (-4.8 + 0.96 * i) * 1.2, 4 + 2 * (i % 3)) for i in range(11)]
    return dict(side=side, back=back)


def style_sidetail(hy, z):
    side = [hang("strand_s1", -5.8, hy + 6.8, -3.0 + z, -4)]
    back = nape(hy, z, 9, 6) + [hang("strand_s1", -4.6 + 2.3 * i - 0.75, hy + 6.8, 4.95 + z, (-4.6 + 2.3 * i) * 1.2, 6) for i in range(5)]
    cx, cy, cz = 6.0, hy + 6.4, 1.8 + z                   # on her left (+x), just behind the ear
    tail = [centred("tail_root", cx - 0.4, cy, cz, 0, 90, 0), centred("tie", cx + 0.7, cy, cz, 0, 0, 90),
            centred("bow", cx + 0.9, cy + 1.3, cz - 1.0, 0, 0, -35), centred("bow", cx + 0.9, cy - 1.0, cz - 1.0, 0, 0, 35)]
    for x, zz, rx, rz in ((-0.4, -0.5, 4, 8), (0.5, 0.4, 10, 14), (-0.1, 0.9, 16, 5)):
        tail.append(hang("tail_long", cx + 1.6 + x - 2.0, cy + 1.0, cz - 0.9 + zz, rz, rx))
    return dict(side=side, back=back, bones=[("hair_sidetail", "head", [cx, cy, cz], tail, False)])


def style_shaggy(hy, z):
    side = [hang("lock_xs", -5.7, hy + 7.8, -3.0 + z, -42, -6), hang("lock_xs", -5.7, hy + 7.9, 0.4 + z, -48, 10),
            hang("strand_s1", -5.9, hy + 5.6, -2.6 + z, -14, -4), hang("wave_a", -5.9, hy + 5.4, 0.8 + z, -22, 12),
            hang("lock_xs", -6.6, hy + 0.2, -1.0 + z, -50, 4)]
    back = []
    for i in range(9):                                    # crown layer, standing out
        x = -4.6 + 1.15 * i
        back.append(hang("lock_xs", x - 0.75, hy + 8.6, 4.4 + z, x * 7, 40 + 8 * (i % 2)))
    for i in range(11):                                   # main layer
        x = -4.8 + 0.96 * i
        back.append(hang(("strand_s1", "wave_a", "strand_s2")[i % 3], x - 0.75, hy + 5.8, 4.9 + 0.2 * (i % 2) + z, x * 3, 12 + 4 * (i % 2)))
    for i in range(7):                                    # ends flicking out at the nape
        x = -4.2 + 1.4 * i
        back.append(hang("lock_xs", x - 0.75, hy - 0.2, 5.6 + z, x * 9, 52))
    return dict(side=side, back=back)


def style_flowing(hy, z):
    side = [hang("strand_l1", -5.9, hy + 6.8, -1.6 + z, -8, 22), hang("lock_xl", -5.8, hy + 6.6, 1.0 + z, -12, 32)]
    back = []
    for i in range(13):
        x = -5.0 + 0.83 * i
        back.append(hang(("lock_xl", "strand_l1", "lock_xl", "strand_l2")[i % 4], x - 0.75, hy + 7.6, 4.8 + 0.2 * (i % 2) + z, x * 1.4,
                         30 + 6 * (i % 3)))
    head = []
    for i in range(6):                                    # combed back over the crown
        x = -3.8 + 1.5 * i
        head.append(up("spike", x - 0.75, hy + 8.4, -4.9 + z, -80, -x * 2))
    return dict(side=side, back=back, head=head)


BUILDERS = {n: globals()["style_" + n] for n in STYLES}


def build(r, style, hy, z, head_cubes, bang_kind=None):
    """add the hair of a new style: bangs + extras onto head_cubes (the head bone, not yet added), and return a function
    that adds hair_side_r/l, hair_back and any extra bones once the head bone exists"""
    spec = BUILDERS[style](hy, z)
    head_cubes += bangs(bang_kind or DEFAULT_BANGS[style], hy, z)

    def finish():
        if spec.get("head"):                             # crowns and spikes; a bone of their own so they never count
            r.add("hair_top", "head", [0, hy + 8, z], spec["head"])   # towards her height (gen_girl_data skips hair*)
        r.add("hair_side_r", "head", [-4.7, hy + 7, z], spec.get("side", []), mirror=True)
        r.add("hair_back", "head", [0, hy + 7, 4.5 + z], spec.get("back", []))
        for name, parent, pivot, cubes, mirror in spec.get("bones", []):
            r.add(name, parent, pivot, cubes, mirror)
    return finish


# ----------------------------------------------------------------------------- painting
def paint(pt, pal):
    """paint the new-style parts (harpy_lib.paint_hair already did the cap, bangs, frames and strand_* parts)"""
    HAIR = pal.HAIR

    def hair_px(x, y, h, gloss=False):
        sid = x // 2
        t = (pal.streak[sid % 7] * 0.8 + (y / max(1, h - 1)) * 0.32 + (noise(x, y, 7) - 0.5) * 0.10) * pal.hair_gain + pal.hair_off
        if gloss and 0.16 < y / max(1, h - 1) < 0.30:
            t += 0.22
        return pick(HAIR, t, x, y)

    def tapered(top=False):
        def fn(f, x, y, w, h):
            if f not in BROAD:
                return pick(HAIR, 0.45, x, y)
            r = y if top else h - 1 - y
            taper = 2.5 * D
            if r < taper:
                half = (w / 2.0) * (min(1.0, (r + 1) / taper) ** 0.85)
                if abs(x + 0.5 - w / 2.0) > half:
                    return CLEAR
            return hair_px(x, y, h, (y < 5) != top)
        return fn

    def blunt(f, x, y, w, h):
        if f in ("up", "down"):
            return pick(HAIR, 0.42, x, y)
        t_ = hair_px(x, y, h, y < 5)
        return pick(HAIR, 0.3, x, y) if y == h - 1 else t_      # a crisp, slightly darker cut line

    for p in ("lock_xs", "lock_xl", "tail_lock", "tail_long", "braid_tip", "wave_a", "wave_b"):
        pt(p, "all", tapered())
    pt("spike", "all", tapered(top=True))
    for p in ("blunt_s", "blunt_l", "fringe"):
        pt(p, "all", blunt)

    def rooty(f, x, y, w, h):                             # gathered hair: lengthwise streaks, glossy middle
        t = 0.48 + (noise(x, y, 5) - 0.5) * 0.14 + (0.16 if 0.3 < y / max(1, h - 1) < 0.55 else 0)
        return pick(HAIR, t, x, y)
    pt("tail_root", "all", rooty)

    def bun(f, x, y, w, h):                               # a coil: darker rings spiralling in
        cx, cy = (w - 1) / 2, (h - 1) / 2
        rr = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
        ring = (rr + (0.0 if f in ("up", "down") else y * 0.4)) % 3.0
        t = 0.62 - (0.22 if ring < 0.9 else 0) + (noise(x, y, 13) - 0.5) * 0.1
        return pick(HAIR, t, x, y)
    for p in ("bun", "curl_a", "curl_b", "curl_c"):
        pt(p, "all", bun)

    def plait(f, x, y, w, h):                             # crossing strands: diagonal ribs with dark creases
        k = (x + y) % 8 if (y // 4) % 2 == 0 else (x - y) % 8
        t = 0.66 - 0.06 * abs(k - 3.5) - (0.2 if k == 0 else 0) + (noise(x, y, 17) - 0.5) * 0.08
        return pick(HAIR, t, x, y)
    pt("braid_seg", "all", plait)

    def tie(f, x, y, w, h):
        return pick(pal.TRIM, 0.7 if (f in ("up", "down") or y == 0) else 0.5, x, y)
    pt("tie", "all", tie)

    def bow(f, x, y, w, h):
        edge = x == 0 or x == w - 1 or y == 0 or y == h - 1
        return pick(pal.TRIM, 0.35 if edge else 0.68 + (noise(x, y, 19) - 0.5) * 0.1, x, y)
    pt("bow", "all", bow)
