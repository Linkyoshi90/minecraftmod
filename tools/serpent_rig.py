"""The rearing serpent (the Macawtl, the Turquoise Serpent, Quetzalcoatl): a girl only from the bust up, one long snake from just under it. She
is not a lamia standing on her tail: she lies flat and slithers like a snake, and now and then rears up to look around.

The bones stand upright in the model, with the body hanging straight down from her chest (sp_1..sp_10), so her size in the game
is worked out as for any girl. The animations do the rest: flat is the whole root pitched forward, the body already in line
behind her; reared is the root upright and the first few segments curving the body back down to the ground.
"""
import girl_lib as G
from girl_lib import H, cube

COUNT = 10
WIDTHS = (5.75, 5.5, 5.25, 5, 4.5, 4, 3.5, 3, 2.25, 1.5)
LENGTH = 5.5
CHAIN = [f"sp_{i}" for i in range(1, COUNT + 1)]
TOP = 12.0                                               # the first segment's pivot, under the flank that covers her waist
FLAT = dict(root=(84, 0, 0), at=(0, 8, 14), head=(-74, 0, 0), chain=(0,) * COUNT, arm=(-62, 0, -14), forearm=(-30, 0, 0), hair=(40, 0, 0))
REAR = dict(root=(-6, 0, 0), at=(0, 2, 0), head=(6, 0, 0), chain=(24, 28, 24, 14, 0, 0, 0, 0, 0, 0), arm=(0, 0, -8), forearm=(-12, 0, 0), hair=(0, 0, 0))
BEATS = (0, 8, 9.5, 12.5, 14)                            # idle: flat, up, a long look round, down again
POSES = (FLAT, FLAT, REAR, REAR, FLAT)


def parts():
    return [("sp_flank", 6.25, 5.25, 5)] + [(n, w, LENGTH, w) for n, w in zip(CHAIN, WIDTHS)]


def paint(pt, pal):
    """scales on her back and sides, the pale belly down her front"""
    back = G.scale_fn(pal, belly_faces=("down", "north"))
    for p in ["sp_flank"] + CHAIN:
        pt(p, "all", back)


def build(r):
    r.put("body", cube("sp_flank", [-3.125, 11.5, -2.5]))
    parent, y = "body", TOP
    for n, w in zip(CHAIN, WIDTHS):
        r.add(n, parent, [0, y, 0], [cube(n, [-w / 2, y - LENGTH, -w / 2])])
        parent, y = n, y - LENGTH + 0.25


def tip():
    return CHAIN[-1]


def posed(p):
    hold, sym, merge = H.hold, H.sym, H.merge
    return merge({"root": {"rotation": hold(*p["root"]), "position": hold(*p["at"])}, "head": {"rotation": hold(*p["head"])},
                  "hair_back": {"rotation": hold(*p["hair"])}}, {n: {"rotation": hold(a, 0, 0)} for n, a in zip(CHAIN, p["chain"])},
                 sym("arm", hold(*p["arm"])), sym("forearm", hold(*p["forearm"])))


def _idle():
    Tk, sym, merge = H.track, H.sym, H.merge
    keys = lambda f: Tk(*[(t, f(p)) for t, p in zip(BEATS, POSES)])
    bones = {"root": {"rotation": keys(lambda p: p["root"]), "position": keys(lambda p: p["at"])}, "hair_back": {"rotation": keys(lambda p: p["hair"])},
             "head": {"rotation": Tk((0, FLAT["head"]), (8, FLAT["head"]), (9.5, REAR["head"]), (10.4, (2, 38, 0)), (11.6, (2, -38, 0)),
                                     (12.5, REAR["head"]), (14, FLAT["head"]))}}
    for i, n in enumerate(CHAIN):
        bones[n] = {"rotation": keys(lambda p, i=i: (p["chain"][i], 0, 0))}
    bones = merge(bones, sym("arm", keys(lambda p: p["arm"])), sym("forearm", keys(lambda p: p["forearm"])))
    return bones


def slither(period, amp):
    return G.wave(CHAIN, period, [amp * (0.4 + 0.6 * min(1, i / 3)) for i in range(COUNT)], axis=2, phase=1.15, keys=12)


def anims(an):
    """every animation in the serpent's poses"""
    merge = H.merge
    flat, rear = posed(FLAT), posed(REAR)
    an.add("idle", 14.0, True, _idle())
    an.add("walk", 1.2, True, merge(flat, slither(1.2, 30)))
    an.add("run", 0.7, True, merge(flat, slither(0.7, 36)))
    an.add("rear", 2.0, True, rear)
    an.add("sit", 4.0, True, merge(flat, slither(4.0, 8)))
    an.add("lie", 4.0, True, flat)
    an.add("bask", 4.0, True, flat)
    an.add("coil", 2.0, True, merge(flat, {n: {"rotation": H.hold(0, 0, 34)} for n in CHAIN}))
    for key in ("hiss", "pounce", "groom", "stalk"):
        if f"animation.{an.name}.{key}" in an.A:
            an.add(key, 1.5, True, merge(rear, {"mouth": {"position": H.hold(*H.OUT_)}}))
