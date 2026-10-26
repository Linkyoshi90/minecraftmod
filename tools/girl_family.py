"""A two-legged girl, assembled from a species table: the template most of the families added on 2026-10-20 share
(jackalopes, minotaurs, elves, most undead, lizardfolk, devils, bugs, some dragons and mollusks). A family script
lists its species with these knobs and adds whatever is unique to it through the `hooks`.

Species knobs (besides girl_lib's build / outfit / hair / bangs / ears / pal / variants):
    legs      "fur" | "boots" | "hoof" | "scales" | "bare" | "stockings"    how the legs are painted
    foot      "paw" | "long" (rabbit) | "hoof" | "shoe" | "claw"           the foot's shape
    tail      list of (w, h, d) boxes, back from the lower back (bones tail, tail_2, ...); tail_rest = rest pitch per bone
    tail_look "fur" | "scale" | "puff" | "tuft" | "spade" | "flame"        how the tail is painted
    horns     a girl_parts.HORN_KINDS key (colour pal.extra["horn"])
    wings     a girl_parts.WING_KINDS key (colours pal.extra["wing"], ["wing_vein"]); wing_scale
    flies     True: a "fly" animation (wings beating, legs trailing)
    pupil     (w, h) pupil radii (slits: (0.9, 3.4))

Animations: girl_lib's biped set (idle, walk, run, sit, lie, pounce, groom, hiss, stalk) with the tail, wings and
any `extra` animations the family adds, plus "fly" for fliers.
"""
import os
import sys

import girl_lib as G
import girl_parts as P
import harpy_lib as H
from harpy_lib import cube, pick, noise, CLEAR

FOOT = {"paw": (2.25, 1.25, 3), "long": (2.25, 1.0, 4.5), "hoof": (2.25, 1.5, 2.5), "shoe": (2.0, 1.25, 3.0), "claw": (2.25, 1.0, 3.25)}


def leg_parts(d):
    t = G.OF.thigh_width(d.get("build"), 2.25)
    fw, fh, fd = FOOT[d.get("foot", "shoe")]
    out = [("thigh", t, 4.75, t), ("shin", t - 0.5, 6.75, t - 0.5), ("paw", max(fw, t), fh, fd)]
    if d.get("foot") in ("paw", "claw"):
        out.append(("toe", .5, .75, .75))
    return out


def tail_parts(d):
    return [(f"tail_{'abcdef'[i]}",) + tuple(s) for i, s in enumerate(d.get("tail") or [])]


def declare(sp, d, extra_parts=()):
    parts = (leg_parts(d) if d.get("lower", "biped") == "biped" else []) + tail_parts(d)
    if d.get("horns"):
        parts += P.horn_parts(d["horns"])
    if d.get("wings"):
        parts += P.wing_parts(d["wings"], d.get("wing_scale", 1.0))
    G.declare(parts + list(extra_parts), ears=d.get("ears"), hair=d.get("hair"), build=d.get("build"), outfit=d.get("outfit"))


def pal_for(d, key=None):
    p = G.pal_from(d["pal"], d["variants"].get(key) if key else None)
    if d.get("pupil"):
        p.pupil = d["pupil"]
    return p


def paint(sp, d, pal, hook=None):
    pt = H.Painter()
    G.paint_upper(pt, pal, top="wrap", outfit=d.get("outfit"), build=d.get("build"))
    fur = G.fur_fn(pal, light_faces=("down",), light_below=0.7)
    scale = G.scale_fn(pal)
    skin_t = H.skin_fn(pal, 0.68)
    legs = d.get("legs", "bare")
    if d.get("ears") in ("cat", "big", "fox", "round", "tuft", "horse", "rabbit", "lop", "cow"):
        G.paint_ears(pt, pal, G.fur_fn(pal, pattern="solid"))
    elif d.get("ears") == "elf":
        pt("ear_a", "all", H.skin_fn(pal, 0.62))
        pt("ear_b", "all", H.skin_fn(pal, 0.66))
    elif d.get("ears") in ("antenna", "plume"):
        ant = G.shades(pal.extra.get("antenna", "#2a2420"))
        pt("ear_a", "all", lambda f, x, y, w, h: pick(ant, 0.4 + 0.2 * (y % 2), x, y))
        pt("ear_b", "all", lambda f, x, y, w, h: pick(ant, 0.55, x, y))
        pt("ear_t", "all", lambda f, x, y, w, h: pick(ant, 0.55 + 0.25 * ((x + y) % 2), x, y) if f in ("east", "west") else pick(ant, 0.4, x, y))

    boot = G.shades(pal.extra.get("boot", "#3a2a20"))
    stock = G.shades(pal.extra.get("stocking", "#2a2430"))

    def thigh(f, x, y, w, h):
        if legs in ("fur", "hoof"):
            edge = h * 0.42 + (noise(x, 0, 5) - 0.5) * 3
            return skin_t(f, x, y, w, h) if y < edge else fur(f, x, y, w, h)
        if legs == "scales":
            edge = h * 0.3 + (noise(x, 0, 5) - 0.5) * 2
            return skin_t(f, x, y, w, h) if y < edge else scale(f, x, y, w, h)
        if legs == "stockings":
            return skin_t(f, x, y, w, h) if y < 3 else pick(stock, 0.45 + (0.25 if y == 3 else 0), x, y)
        return skin_t(f, x, y, w, h)

    def shin(f, x, y, w, h):
        if legs in ("fur", "hoof"):
            return fur(f, x, y, w, h)
        if legs == "scales":
            return scale(f, x, y, w, h)
        if legs == "boots":
            return pick(boot, 0.5 + (0.25 if y == 0 else 0) + (noise(x, y, 3) - 0.5) * 0.1, x, y) if y < h - 0 else None
        if legs == "stockings":
            return pick(stock, 0.45 + (noise(x, y, 3) - 0.5) * 0.08, x, y)
        return skin_t(f, x, y, w, h)
    pt("thigh", "all", thigh)
    pt("shin", "all", shin)
    foot = d.get("foot", "shoe")
    hoof = G.shades(pal.extra.get("hoof", "#3a302a"))
    claw = G.shades(pal.extra.get("claw", "#e8e0cc"))

    def paw(f, x, y, w, h):
        if foot == "hoof":
            return pick(hoof, 0.42 + 0.2 * (y == 0) + (0.15 if f == "north" and abs(x + 0.5 - w / 2) < 0.6 else 0), x, y)
        if foot == "shoe":
            return pick(boot, 0.36 + 0.2 * (y == 0) + (noise(x, y, 6) - 0.5) * 0.08, x, y) if f != "down" else pick(boot, 0.2, x, y)
        if foot == "claw":
            return scale(f, x, y, w, h)
        if f == "down":
            return pick(pal.LIGHT, 0.4, x, y)
        return pick(pal.LIGHT if f in ("north", "up") else pal.FUR, 0.6 + (noise(x, y, 7) - 0.5) * 0.14, x, y)
    pt("paw", "all", paw)
    pt("toe", "all", lambda f, x, y, w, h: pick(claw, 0.5, x, y))

    look = d.get("tail_look", "fur")
    tf = {"fur": G.fur_fn(pal, light_faces=("down",), seed=21), "scale": scale}.get(look, G.fur_fn(pal, seed=21))
    tails = [p for p, *_ in tail_parts(d)]
    for i, p in enumerate(tails):
        last = i == len(tails) - 1
        if look == "puff":
            pt(p, "all", lambda f, x, y, w, h: pick(pal.LIGHT, 0.7 + 0.2 * (noise(x // 2, y // 2, 3) > 0.5), x, y))
        elif look == "tuft" and last:
            pt(p, "all", lambda f, x, y, w, h: pick(pal.HAIR, 0.4 + (noise(x, y, 9) - 0.5) * 0.3, x, y))
        elif look == "spade" and last:
            pt(p, "all", lambda f, x, y, w, h: pick(pal.MARK, 0.5 + 0.2 * (y == 0), x, y))
        elif look == "flame" and last:
            fl = G.shades(pal.extra.get("flame", "#f08a28"))
            pt(p, "all", lambda f, x, y, w, h: pick(fl, 0.55 + 0.4 * (noise(x, y, 11) > 0.5), x, y))
        elif look == "spade":
            pt(p, "all", lambda f, x, y, w, h: skin_t(f, x, y, w, h) if not pal.extra.get("tail_col") else pick(G.shades(pal.extra["tail_col"]), 0.5, x, y))
        else:
            pt(p, "all", tf)
    if d.get("horns"):
        P.paint_horns(pt, pal.extra.get("horn", "#e8dcc0"), tip=pal.extra.get("horn_tip"))
    wk = d.get("wings")
    if wk in ("bat", "devil_small", "dragon"):
        P.paint_membrane(pt, pal.extra.get("wing_bone", pal.extra.get("wing", "#3a2030")), pal.extra.get("wing", "#6a2a3a"))
    elif wk == "feather":
        P.paint_feather_wings(pt, pal.extra.get("wing", "#8a6a4a"), pal.extra.get("wing_tip", "#3a2a1e"))
    elif wk:
        P.paint_insect(pt, pal.extra.get("wing", "#e8f0f8"), pal.extra.get("wing_vein", "#3a3a40"), alpha=pal.extra.get("wing_alpha", 150),
                       pattern=pal.extra.get("wing_pattern"), eyespot=pal.extra.get("eyespot"))
    if hook:
        hook(pt, pal, d)
    return pt.T


def build(sp, d, hook=None):
    r = G.Rig()
    r.add("root", None, [0, 0, 0])
    g = G.upper(r, 12, dict(hair=d.get("hair"), bangs=d.get("bangs"), ears=d.get("ears"), skirt=True, build=d.get("build"), outfit=d.get("outfit")))
    if d.get("lower", "biped") == "biped":
        G.legs_biped(r, g, d)
        G.OF.dress_legs(r, d.get("outfit"))
    if d.get("tail"):
        P.tail(r, "tail", "body", (0, 12.6, 1.6), [p for p, *_ in tail_parts(d)])
    if d.get("horns"):
        P.horns(r, d["horns"], g.hy, g.z)
    if d.get("wings"):
        P.wings(r, d["wings"], "body", y=18.6, z=1.4)
    if hook:
        hook(r, g, d)
    return r.model(sp)


def _inject(an, key, bones):
    """add tracks to an animation that already exists (rotation Z is flipped like Anims.add does)"""
    A = an.A.get(f"animation.{an.name}.{key}")
    if A is None:
        return
    for b, ch in bones.items():
        if b not in an.bones:
            continue
        ch = dict(ch)
        if "rotation" in ch:
            ch["rotation"] = {k: [v[0], v[1], -v[2]] for k, v in ch["rotation"].items()}
        A["bones"].setdefault(b, {}).update(ch)


def anims(d, extra=None):
    """the animation function for a species table entry"""
    def fn(an):
        G.biped_anims(an, [], [], pounce=True, fox=False)
        n = len(d.get("tail") or [])
        tails = ["tail"] + [f"tail_{i}" for i in range(2, n + 1)] if n else []
        rest = d.get("tail_rest") or [(-30, 0, 0)] + [(8, 0, 0)] * (n - 1)
        hold = H.hold
        if tails:
            sway_amp = d.get("tail_sway", 8)
            for key, per, amp in (("idle", 4.0, sway_amp), ("walk", 1.0, sway_amp), ("run", 0.6, sway_amp * 0.6), ("sit", 3.0, sway_amp * 0.6),
                                  ("pounce", 1.0, 0), ("groom", 2.0, 4), ("hiss", 1.5, 2), ("stalk", 1.8, 3)):
                _inject(an, key, G.wave(tails, per, [amp * (0.5 + 0.5 * i) for i in range(n)], axis=1, base=rest))
            _inject(an, "lie", {t: {"rotation": hold(*(5, 0, 0))} for t in tails})
        wk = d.get("wings")
        if wk:
            names = ["wing_r", "wing_l"] + (["wing2_r", "wing2_l"] if wk in ("insect", "fairy", "butterfly", "moth") else [])
            insect = wk in ("insect", "fairy", "butterfly", "moth")
            rest_w = (P.flap(names, 0.25, 14, 10, sweep=20) if wk in ("insect", "fairy") else
                      P.folded(names, fold=40 if insect else 62, droop=-6 if insect else -18))
            for key in ("idle", "walk", "sit", "groom", "hiss", "stalk"):
                _inject(an, key, rest_w)
            _inject(an, "lie", P.folded(names, fold=70, droop=-10))
            _inject(an, "run", P.flap(names, 0.3, 30, 20) if d.get("flies") else P.folded(names, 55, -10))
            if d.get("flies"):
                Tk = H.track
                period = 0.25 if insect else 0.7
                fly = H.merge(G.upper_idle(), P.flap(names, period, 40 if not insect else 25, 30 if not insect else 20, sweep=10),
                              {"root": {"rotation": hold(18, 0, 0)}, "leg_r": {"rotation": Tk((0, (14, 0, 0)), (1, (22, 0, 0)), (2, (14, 0, 0)))},
                               "leg_l": {"rotation": Tk((0, (22, 0, 0)), (1, (14, 0, 0)), (2, (22, 0, 0)))},
                               "shin_r": {"rotation": hold(20, 0, 0)}, "shin_l": {"rotation": hold(28, 0, 0)},
                               "hair_back": {"rotation": hold(22, 0, 0)}},
                              H.sym("arm", hold(20, 0, 12)))
                if tails:
                    fly.update(G.wave(tails, 2.0, [4] * n, axis=1, base=[(10, 0, 0)] * n))
                an.add("fly", 2.0, True, fly)
        if extra:
            extra(an)
    return fn


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.25, -30, 8), ("run", 0.15, -60, 5), ("sit", 0, -30, 10), ("pounce", 0.5, -70, 5)]
FACE = [("idle", 0, 0, 0, 24), ("idle", 2.75, 0, 0, 24), ("hiss", 0.5, 0, 0, 24), ("idle", 0, -30, 8, 24)]


def write_glowmask(name, tex, parts, boost=1.0):
    """<name>_glowmask.png: the texture's pixels on the given parts, transparent everywhere else (GeckoLib's
    AutoGlowingGeoLayer draws it full-bright). `boost` > 1 brightens them: mid-tone colours glow dimly."""
    lift = lambda px: px if not px[3] or boost == 1.0 else tuple(min(255, int(v * boost + 40 * (boost - 1))) for v in px[:3]) + (px[3],)
    n = len(tex)
    mask = [[(0, 0, 0, 0)] * n for _ in range(n)]
    for (p, f), (x0, y0, w, h) in H.RECT.items():
        if p not in parts:
            continue
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                if 0 <= y < n and 0 <= x < n:
                    mask[y][x] = lift(tex[y][x])
    H.write_png(os.path.join(G.ASSETS, "textures", "entity", name + "_glowmask.png"), mask)


def run_family(fid, species, declare_hook=None, paint_hook=None, build_hook=None, anim_extra=None, views=None, face=None, only=None):
    """generate every species in `species` (or those named on the command line); --previews renders sheets and a roster"""
    import family_concepts as FC
    fam = FC.family(fid)
    want = [a for a in sys.argv[1:] if not a.startswith("--")]
    prev = "--previews" in sys.argv
    entries = []
    for s in fam["species"]:
        sp = s["id"]
        if sp not in species or (want and sp not in want) or (only and sp not in only):
            continue
        d = species[sp]
        extra_parts = declare_hook(sp, d) if declare_hook else []
        declare(sp, d, extra_parts)
        tex = paint(sp, d, pal_for(d), paint_hook)
        keys = list(d["variants"])
        geo, adoc, texes = G.write(sp, tex, build(sp, d, build_hook), anims(d, (lambda an, sp=sp: anim_extra(an, sp, species[sp])) if anim_extra else None),
                                   keys, lambda k, d=d: paint(sp, d, pal_for(d, k), paint_hook))
        if d.get("glow"):
            for k, t in texes.items():
                write_glowmask(sp + ("_" + k if k else ""), t, d["glow"], d.get("glow_boost", 1.0))
        height = G.model_height(geo)
        if prev:
            G.previews(sp, geo, adoc, texes, views or VIEWS, face or FACE, height)
        entries.append((geo, tex, adoc["animations"][f"animation.{sp}.idle"], s["height"], height))
    if prev and not want and entries:
        G.roster(os.path.join(G.PREVIEWS, f"{fid}_roster.png"), entries)
    return entries
