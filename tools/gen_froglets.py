#!/usr/bin/env python3
"""Froglets: a tiny froglet / toadlet for each frog and toad species (11; the Surinam toad's young are miniature
adults and keep the adult model). Shared helpers: chibi_lib.py on top of harpy_lib.py.

    python tools/gen_froglets.py                   # write <species>_froglet geo / animation / texture
    python tools/gen_froglets.py --preview x.png   # also render a contact sheet

Concept: a big round head with the humanoid chibi face (huge shiny eyes, cheek blush, a small mouth) under a little
cap of hair in her mother's hair colour, a plump body with a pale belly, stubby arms, webbed feet and a stub of tail
left over from the tadpole. Species keep one signature: the poison dart frog her cobalt and black spots, the tree
frog her orange pads and blue flanks, the glass frog a see-through belly window with a red heart behind it, the
fire-bellied toad a red belly (with a glow mask), the natterjack her yellow back stripe, the cane toad the big
glands behind her eyes, the desert rain frog a round sand-pink grump. Animations: idle, walk, hop, swim.
Used by the game for the second life stage of an anuran (AbstractAnuran#isBaby after the tadpole stage).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H
import chibi_lib as C
from harpy_lib import rgb, ramp, pick, noise, cube, track, sym, merge, hold

D = H.D


def R(*cols):
    return ramp([rgb(*c) for c in cols])


# k scales the base model (about 10.5 units tall)
SPECIES = {
    "bullywug": dict(k=1.1, back=R((36, 84, 34), (60, 124, 48), (92, 166, 66), (132, 200, 96)), belly=R((190, 190, 120), (226, 224, 154), (248, 244, 190)),
                     hair=R((20, 90, 86), (36, 140, 130), (64, 186, 172), (110, 220, 204)), iris=R((120, 90, 20), (190, 150, 30), (240, 206, 80)),
                     cheek=(238, 150, 70), pattern="stripe", pat=(238, 226, 120)),
    "bullfrog": dict(k=1.25, back=R((50, 58, 24), (82, 94, 36), (114, 126, 52), (150, 160, 74)), belly=R((190, 184, 140), (214, 210, 164), (238, 234, 194)),
                     hair=R((40, 26, 14), (66, 46, 24), (96, 70, 38), (130, 98, 56)), iris=R((120, 76, 16), (176, 116, 26), (226, 170, 58)),
                     cheek=(210, 150, 90), pattern="blotch", pat=(30, 40, 16), mohawk=True),
    "tree_frog": dict(k=0.9, back=R((50, 130, 42), (80, 180, 60), (116, 214, 84), (168, 240, 130)), belly=R((210, 226, 160), (232, 242, 190), (248, 252, 216)),
                      hair=R((16, 44, 130), (30, 80, 200), (60, 120, 240), (110, 170, 255)), iris=R((130, 10, 14), (196, 30, 26), (244, 80, 60)),
                      cheek=(248, 150, 60), pattern="flank", pat=(30, 80, 220), pads=(250, 140, 40)),
    "poison_dart_frog": dict(k=1.0, back=R((8, 24, 96), (20, 64, 176), (34, 104, 224), (78, 154, 252)), belly=R((120, 164, 230), (160, 200, 246), (206, 230, 255)),
                             hair=R((8, 10, 30), (16, 22, 56), (30, 40, 90), (56, 70, 130)), iris=R((10, 14, 40), (30, 44, 96), (80, 110, 190)),
                             cheek=(250, 214, 60), pattern="spots", pat=(6, 8, 30)),
    "glass_frog": dict(k=0.95, back=R((120, 184, 140), (158, 218, 174), (192, 240, 204), (226, 254, 232)), belly=None,
                       hair=R((50, 120, 110), (80, 160, 148), (116, 198, 184), (170, 230, 216)), iris=R((150, 120, 20), (214, 178, 50), (248, 228, 124)),
                       cheek=(248, 190, 190), pattern="dots", pat=(110, 170, 130), window=True),
    "goliath_frog": dict(k=1.45, back=R((52, 48, 22), (90, 82, 36), (126, 114, 54), (164, 150, 82)), belly=R((176, 166, 116), (208, 198, 144), (232, 224, 174)),
                         hair=R((36, 26, 16), (62, 46, 28), (92, 70, 44), (126, 98, 64)), iris=R((90, 56, 10), (150, 96, 20), (210, 156, 50)),
                         cheek=(190, 130, 90), pattern="blotch", pat=(30, 26, 12)),
    "desert_rain_frog": dict(k=0.7, back=R((156, 104, 74), (196, 146, 104), (220, 174, 132), (240, 206, 168)), belly=R((234, 200, 164), (246, 224, 192), (254, 240, 218)),
                             hair=R((150, 110, 70), (190, 148, 96), (222, 176, 122), (244, 208, 156)), iris=R((8, 4, 10), (20, 14, 22), (110, 80, 30)),
                             cheek=(238, 130, 130), pattern="freckle", pat=(160, 110, 76), mouth="frown", wide=True),
    "mucus_toad": dict(k=1.0, back=R((54, 40, 24), (90, 68, 40), (126, 98, 60), (164, 132, 90)), belly=R((180, 154, 112), (210, 188, 142), (234, 216, 172)),
                       hair=R((30, 22, 18), (52, 40, 30), (78, 62, 46), (108, 88, 66)), iris=R((100, 66, 12), (160, 110, 22), (216, 166, 54)),
                       cheek=(200, 120, 100), pattern="warts", pat=(40, 28, 16)),
    "fire_bellied_toad": dict(k=0.9, back=R((30, 38, 16), (58, 70, 28), (90, 104, 44), (126, 142, 66)), belly=R((190, 50, 20), (232, 86, 28), (252, 144, 52)),
                              hair=R((26, 30, 14), (44, 52, 24), (70, 82, 38), (100, 116, 56)), iris=R((140, 60, 10), (210, 100, 18), (250, 160, 50)),
                              cheek=(244, 120, 60), pattern="blotch", pat=(12, 14, 8), glow=True),
    "cane_toad": dict(k=1.0, back=R((50, 38, 24), (88, 68, 40), (124, 98, 60), (162, 132, 90)), belly=R((190, 174, 128), (214, 200, 154), (236, 224, 182)),
                      hair=R((40, 30, 20), (68, 52, 34), (98, 78, 54), (130, 106, 76)), iris=R((90, 60, 10), (150, 104, 20), (214, 166, 50)),
                      cheek=(206, 130, 100), pattern="warts", pat=(36, 26, 14), glands=True),
    "natterjack": dict(k=0.8, back=R((70, 58, 36), (104, 88, 56), (140, 120, 80), (176, 156, 108)), belly=R((190, 176, 140), (212, 200, 164), (234, 224, 190)),
                       hair=R((60, 46, 26), (92, 74, 44), (126, 104, 66), (160, 136, 92)), iris=R((110, 74, 10), (176, 124, 20), (232, 188, 60)),
                       cheek=(222, 140, 90), pattern="stripe", pat=(240, 214, 70)),
}

SIZES = {
    "head": (8, 5, 6), "cap": (8, 1.75, 6, ("down",)), "tuft": (1.5, 1.5, 1.5), "mohawk": (1.25, 2, 4.5),
    "eyelids": (6.4, 2.4, .25), "gland": (1, 1.5, 1.5),
    "body": (5.5, 3, 4), "belly": (3.5, 2, .5), "organs": (2.5, 2, 1.5),
    "arm": (1.25, 2.25, 1.25), "hand": (1.75, .5, 1.5),
    "leg": (1.75, 1.75, 2), "foot": (3, .5, 3.5), "tail": (1.5, 1.25, 2.5),
}


def paint(sp):
    pt = H.Painter()
    back, belly, hair = sp["back"], sp["belly"], sp["hair"]
    kind = sp["pattern"]

    def skin(f, x, y, w, h):
        t = 0.55 + (noise(x, y, 3) - 0.5) * 0.18
        if f == "up":
            t += 0.12
        elif f == "down":
            t -= 0.2
        elif f in ("south", "east", "west"):
            t -= 0.05
        if kind == "blotch" and noise(x // 3, y // 3, 8) > 0.74:
            return rgb(*sp["pat"])
        if kind == "spots" and noise(x // 2, y // 2, 9) > 0.76:
            return rgb(*sp["pat"])
        if kind == "freckle" and noise(x, y, 5) > 0.93:
            return rgb(*sp["pat"])
        if kind == "warts" and noise(x // 2, y // 2, 12) > 0.80:
            return pick(back, t - 0.3, x, y)
        if kind == "dots" and noise(x, y, 6) > 0.92:
            return rgb(*sp["pat"])
        if kind == "stripe" and f in ("up", "east", "west", "south"):
            if (f == "up" and abs(x - w // 2) <= 1) or (f in ("east", "west") and abs(y - h * 0.45) < 1.2):
                return rgb(*sp["pat"])
        if kind == "flank" and f in ("east", "west") and y > h * 0.45:
            return pick(ramp([rgb(14, 40, 120), rgb(30, 80, 200), rgb(70, 130, 240)]), 0.5 + (noise(x, y, 2) - 0.5) * 0.3, x, y)
        return pick(back, t, x, y)

    def belly_fn(f, x, y, w, h):
        if belly is None:
            return (0, 0, 0, 0)                     # glass frog: see-through window
        return pick(belly, 0.6 + (noise(x, y, 4) - 0.5) * 0.15, x, y)

    def hair_fn(f, x, y, w, h):
        return pick(hair, 0.55 + (noise(x, y, 8) - 0.5) * 0.3 + (0.12 if f == "up" else 0), x, y)

    for p in ("head", "body", "tail", "gland"):
        pt(p, "all", skin)
    pt("body", ("down",), lambda f, x, y, w, h: pick(belly, 0.5, x, y) if belly is not None else pick(back, 0.4, x, y))
    pt("belly", "all", belly_fn)
    for p in ("cap", "tuft", "mohawk"):
        pt(p, "all", hair_fn)
    # the belly panel is the whole front of the body for toads with a coloured belly
    if sp.get("glow") or belly is not None:
        pt("belly", "all", belly_fn)
    pt("organs", "all", lambda f, x, y, w, h: pick(ramp([rgb(150, 40, 50), rgb(200, 70, 76), rgb(236, 120, 120)]), 0.5 + (noise(x, y, 6) - 0.5) * 0.3, x, y))
    if sp.get("window"):
        x0, y0, w, h = H.RECT[("organs", "north")]
        for yy in range(h):
            for xx in range(w):
                if (xx - w // 2) ** 2 + (yy - h // 3) ** 2 <= 4:
                    pt.T[y0 + yy][x0 + xx] = rgb(228, 56, 64)                 # the little heart

    limb = lambda f, x, y, w, h: pick(back, 0.5 + (noise(x, y, 17) - 0.5) * 0.2 - (0.12 if f in ("down", "south") else 0), x, y)
    pt("arm", "all", limb)
    pt("leg", "all", limb)
    pad = sp.get("pads")
    padfn = (lambda f, x, y, w, h: rgb(*pad)) if pad else (lambda f, x, y, w, h: pick(back, 0.6 + (noise(x, y, 18) - 0.5) * 0.2, x, y))
    pt("hand", "all", padfn)
    pt("foot", "all", padfn)

    cxs, ey, a, b = C.chibi_face(pt, "head", skin, sp["iris"], eye_scale=1.2 if sp.get("wide") else 1.05, eye_y=0.5,
                                 cheek=rgb(*sp["cheek"]), mouth=sp.get("mouth", "smile"))
    C.eyelids_part(pt, "eyelids", H.PARTS["head"]["size"][0], skin, a)

    gmask = None
    if sp.get("glow"):
        gp = H.Painter()
        gp("belly", "all", lambda f, x, y, w, h: rgb(255, 120, 40))
        gp("body", ("north",), lambda f, x, y, w, h: rgb(255, 110, 36) if y > h * 0.5 else None)
        gmask = gp.T
    return pt.T, gmask


def build(sp, name):
    m = H.Model(name)
    S = H.PARTS
    sz = lambda p: S[p]["size"]
    hw, hh, hd = sz("head")
    bw, bh, bd = sz("body")
    aw, ah, ad = sz("arm")
    lw, lh, ld = sz("leg")
    fw, fh, fd = sz("foot")
    wide = 1.2 if sp.get("wide") else 1.0
    y_body = fh + lh
    y_neck = y_body + bh - 0.25
    lx = bw * 0.27

    m.bone("root", None, [0, 0, 0])
    body = [cube("body", [-bw / 2, y_body, -bd / 2]),
            cube("belly", [-sz("belly")[0] / 2, y_body + 0.4, -bd / 2 - sz("belly")[2] + 0.1])]
    if sp.get("window"):
        o = sz("organs")
        body.append(cube("organs", [-o[0] / 2, y_body + 0.6, -bd / 2 + 0.15]))
    m.bone("body", "root", [0, y_body, 0], body)
    m.bone("tail", "body", [0, y_body + 0.75, bd / 2 - 0.25], [cube("tail", [-sz("tail")[0] / 2, y_body + 0.25, bd / 2 - 0.25])])

    head = [cube("head", [-hw / 2, y_neck, -hd / 2]), cube("cap", [-hw / 2, y_neck + hh - sz("cap")[1], -hd / 2], inflate=0.25)]
    top = y_neck + hh
    tw = sz("tuft")[0]
    for x, z, rz in ((-1.6, 0.4, -14), (1.6, 0.4, 14)):
        head.append(cube("tuft", [x - tw / 2, top - 0.25, z - tw / 2], pivot=[x, top - 0.25, z], rotation=[0, 0, rz]))
    if sp.get("mohawk"):
        mw, mh, md = sz("mohawk")
        head.append(cube("mohawk", [-mw / 2, top - 0.25, -md / 2 + 0.5], pivot=[0, top - 0.25, 0], rotation=[-12, 0, 0]))
    if sp.get("glands"):
        gw, gh, gd = sz("gland")
        for sx in (-1, 1):
            head.append(cube("gland", [sx * hw / 2 - (gw if sx > 0 else 0), y_neck + hh * 0.5, hd * 0.05]))
    m.bone("head", "body", [0, y_neck, 0], head)
    ey_c = y_neck + hh * 0.5
    m.bone("eyelids", "head", [0, ey_c, -hd / 2 + 0.2], [cube("eyelids", [-sz("eyelids")[0] / 2, ey_c - sz("eyelids")[1] / 2, -hd / 2 + 0.1])])

    ay = y_body + bh - 0.5
    m.bone("arm_r", "body", [-bw / 2 - aw / 2, ay, 0], [cube("arm", [-bw / 2 - aw, ay - ah, -ad / 2]),
                                                       cube("hand", [-bw / 2 - aw - 0.25, ay - ah - sz("hand")[1], -sz("hand")[2] / 2])], mirror=True)
    m.bone("leg_r", "root", [-lx, y_body, 0], [cube("leg", [-lx - lw / 2, fh, -ld / 2 + 0.25])], mirror=True)
    m.bone("foot_r", "leg_r", [-lx, fh, 0], [cube("foot", [-lx - fw / 2, 0, -fd / 2 - 0.25])], mirror=True)
    return m, top


def animations(an):
    an.add("idle", 3.0, True, merge({
        "body": {"position": track((0, (0, 0, 0)), (1.5, (0, -0.15, 0)), (3, (0, 0, 0)))},
        "head": {"rotation": track((0, (0, 0, 0)), (0.9, (-3, 8, 2)), (1.8, (2, -6, -2)), (3, (0, 0, 0)))},
        "tail": {"rotation": track((0, (0, 0, 0)), (0.75, (0, 10, 0)), (1.5, (0, 0, 0)), (2.25, (0, -10, 0)), (3, (0, 0, 0)))},
    }, H.blink(1.9), sym("arm", track((0, (0, 0, 4)), (1.5, (0, 0, 0)), (3, (0, 0, 4))))))
    swing = track((0, (28, 0, 0)), (0.3, (-28, 0, 0)), (0.6, (28, 0, 0)))
    swing_o = track((0, (-28, 0, 0)), (0.3, (28, 0, 0)), (0.6, (-28, 0, 0)))
    an.add("walk", 0.6, True, merge({
        "leg_r": {"rotation": swing}, "leg_l": {"rotation": swing_o},
        "arm_r": {"rotation": swing_o}, "arm_l": {"rotation": swing},
        "body": {"position": track((0, (0, 0, 0)), (0.15, (0, 0.2, 0)), (0.3, (0, 0, 0)), (0.45, (0, 0.2, 0)), (0.6, (0, 0, 0))),
                 "rotation": track((0, (0, 0, 4)), (0.3, (0, 0, -4)), (0.6, (0, 0, 4)))},
        "head": {"rotation": track((0, (0, 0, -4)), (0.3, (0, 0, 4)), (0.6, (0, 0, -4)))},
        "tail": {"rotation": track((0, (0, 12, 0)), (0.3, (0, -12, 0)), (0.6, (0, 12, 0)))},
    }))
    both = lambda tr: {"leg_r": {"rotation": tr}, "leg_l": {"rotation": tr}}
    an.add("hop", 0.7, True, merge({
        "body": {"position": track((0, (0, 0, 0)), (0.15, (0, -0.6, 0)), (0.35, (0, 1.6, 0)), (0.55, (0, 0.3, 0)), (0.7, (0, 0, 0))),
                 "rotation": track((0, (0, 0, 0)), (0.15, (14, 0, 0)), (0.35, (-14, 0, 0)), (0.55, (4, 0, 0)), (0.7, (0, 0, 0)))},
        "head": {"rotation": track((0, (0, 0, 0)), (0.15, (-10, 0, 0)), (0.35, (8, 0, 0)), (0.7, (0, 0, 0)))},
        "tail": {"rotation": track((0, (0, 0, 0)), (0.35, (-16, 0, 0)), (0.7, (0, 0, 0)))},
    }, both(track((0, (0, 0, 0)), (0.15, (46, 0, 0)), (0.35, (-48, 0, 0)), (0.55, (20, 0, 0)), (0.7, (0, 0, 0)))),
        sym("arm", track((0, (0, 0, 0)), (0.15, (30, 0, 0)), (0.35, (-60, 0, 0)), (0.7, (0, 0, 0)))), {}))
    an.add("swim", 0.8, True, merge({
        "body": {"rotation": track((0, (14, 0, 0)), (0.4, (22, 0, 0)), (0.8, (14, 0, 0)))},
        "leg_r": {"rotation": track((0, (40, 0, 0)), (0.4, (-30, 0, 0)), (0.8, (40, 0, 0)))},
        "leg_l": {"rotation": track((0, (-30, 0, 0)), (0.4, (40, 0, 0)), (0.8, (-30, 0, 0)))},
        "tail": {"rotation": track((0, (0, 16, 0)), (0.4, (0, -16, 0)), (0.8, (0, 16, 0)))},
    }, sym("arm", track((0, (-30, 0, 18)), (0.4, (20, 0, 10)), (0.8, (-30, 0, 18))))))


def main():
    entries = []
    for species, sp in SPECIES.items():
        name = species + "_froglet"
        C.declare(SIZES, sp["k"])
        tex, glow = paint(sp)
        model, height = build(sp, name)
        geo, adoc = C.write_model(name, tex, model, animations, glow)
        entries.append((geo, tex, adoc["animations"][f"animation.{name}.idle"], 0, height))
    if "--preview" in sys.argv:
        C.lineup(sys.argv[sys.argv.index("--preview") + 1], entries, cols=4, cell=320, views=((-30, 12), (150, 10)))


if __name__ == "__main__":
    main()
