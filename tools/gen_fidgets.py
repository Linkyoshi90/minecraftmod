#!/usr/bin/env python3
"""Idle fidgets (2026-11-13, entity/girl/Fidgets): short clips a girl plays now and then while she stands about, so the kinds stand
apart from one another. For every girl model (GirlSpecies, and the own models of homage looks) this adds to her animation file:

* shared fidgets, by the bones she has: stretch, yawn, look_back, shift, hair_toss, tail_swish, ear_twitch, wing_stretch;
* her family's own one or two (a cat kneads and washes her face, a dog shakes herself off, a centaur stamps...);
* a signature fidget of her own ("fidget_signature"): the motion her concept suggests (a dancer dances, a singer sings, a hunter
  sniffs the air...), with a secondary flourish and timing and size of her own, so no two kinds share one (checked); a few have one
  made by hand (the dullahan settles her head, the bee waggles, the cheshire grins...).

Each clip keeps her idle's other tracks (a tail at rest stays at rest) and ends where it began. Also writes registry/FidgetTable.java:
each species' fidgets with their lengths in ticks. Re-run after regenerating any girl model (girl_lib.write does not call it).

    python tools/gen_fidgets.py
"""
import copy
import hashlib
import json
import math
import os
import random
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import family_concepts as C  # noqa: E402
import family_concepts_harmontide  # noqa: E402,F401

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "harmontide")
SPECIES_JAVA = os.path.join(ROOT, "src", "main", "java", "com", "linkyoshi90", "harmontide", "entity", "girl", "GirlSpecies.java")
OUT = os.path.join(ROOT, "src", "main", "java", "com", "linkyoshi90", "harmontide", "registry", "FidgetTable.java")
Tk = H.track


def r3(v):
    return tuple(round(x, 2) for x in v)


class Rig:
    """what a model has: its bones, and their rest rotations in her idle (authored space)"""

    def __init__(self, name, geo, adoc):
        self.name = name
        self.bones = {b["name"] for b in geo["minecraft:geometry"][0]["bones"]}
        self.idle = adoc["animations"].get(f"animation.{name}.idle", {"bones": {}})

    def has(self, *names):
        return all(n in self.bones for n in names)

    def rest(self, bone):
        ch = self.idle["bones"].get(bone, {}).get("rotation")
        if not ch:
            return (0.0, 0.0, 0.0)
        v = ch[sorted(ch, key=float)[0]] if isinstance(ch, dict) else ch
        return (v[0], v[1], -v[2])                      # file Z -> authored Z

    def tails(self):
        out = []
        if "tail" in self.bones:
            out.append("tail")
            i = 2
            while f"tail_{i}" in self.bones:
                out.append(f"tail_{i}")
                i += 1
        for extra in ("tail_mid", "tail_tip"):
            if extra in self.bones:
                out.append(extra)
        return out

    def segs(self):
        out, i = [], 1
        while f"seg_{i}" in self.bones:
            out.append(f"seg_{i}")
            i += 1
        return out

    def fins(self):
        out, i = [], 1
        while f"fin_{i}" in self.bones:
            out.append(f"fin_{i}")
            i += 1
        return out + (["fluke"] if "fluke" in self.bones else [])


def pulse(bone, rest, peak, length, at=0.3, out=0.7):
    """rest -> peak (by at*length), held, -> rest (from out*length)"""
    return {bone: {"rotation": Tk((0, r3(rest)), (length * at, r3(peak)), (length * out, r3(peak)), (length, r3(rest)))}}


def add(v, d):
    return tuple(a + b for a, b in zip(v, d))


def osc(bone, rest, axis, amp, cycles, length, start=0.1, end=0.9, kind="rotation"):
    """an oscillation about rest on one axis, between start and end of the clip"""
    keys = [(0, r3(rest)), (length * start, r3(rest))]
    n = max(2, int(cycles * 4))
    for k in range(1, n + 1):
        t = length * (start + (end - start) * k / n)
        v = list(rest)
        v[axis] += amp * math.sin(2 * math.pi * cycles * k / n)
        keys.append((t, r3(v)))
    keys.append((length, r3(rest)))
    return {bone: {kind: Tk(*keys)}}


def both(rig, base, fn):
    out = {}
    for side in ("r", "l"):
        b = f"{base}_{side}"
        if b in rig.bones:
            out.update(fn(b, 1 if side == "r" else -1))
    return out


# ----------------------------------------------------------------------------- shared fidgets
def f_stretch(rig, p):
    L = 2.2 * p["s"]
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-165 * p["a"], 0, 14 * s), L, 0.35, 0.65))
    out.update(both(rig, "forearm", lambda b, s: pulse(b, rig.rest(b), (0, 0, 0), L)))
    out.update(pulse("body", rig.rest("body"), (-7, 0, 0), L, 0.35, 0.65))
    out.update(pulse("head", rig.rest("head"), (-14, 0, 0), L, 0.35, 0.65))
    return L, out


def f_yawn(rig, p):
    L = 1.9 * p["s"]
    out = pulse("head", rig.rest("head"), (-18 * p["a"], 0, 4 * p["side"]), L, 0.35, 0.7)
    if rig.has("mouth"):
        out["mouth"] = {"scale": Tk((0, (1, 1, 1)), (L * 0.35, (1, 1.8, 1)), (L * 0.7, (1, 1.8, 1)), (L, (1, 1, 1)))}
    arm = "arm_r" if p["side"] > 0 else "arm_l"
    if arm in rig.bones:
        out.update(pulse(arm, rig.rest(arm), (-115, 0, -28 * p["side"]), L, 0.35, 0.7))
    if rig.has("eyelids"):
        out["eyelids"] = {"position": Tk((0, (0, 0, 0)), (L * 0.3, (0, 0, 0)), (L * 0.35, H.OUT_), (L * 0.7, H.OUT_), (L * 0.75, (0, 0, 0)))}
    return L, out


def f_look_back(rig, p):
    L = 2.4 * p["s"]
    out = pulse("head", rig.rest("head"), (0, 70 * p["side"], 0), L, 0.25, 0.7)
    out.update(pulse("body", rig.rest("body"), (0, 14 * p["side"], 0), L, 0.3, 0.65))
    return L, out


def f_shift(rig, p):
    L = 2.0 * p["s"]
    out = {"body": {"position": Tk((0, (0, 0, 0)), (L * 0.3, (0.6 * p["side"], -0.2, 0)), (L * 0.7, (0.6 * p["side"], -0.2, 0)), (L, (0, 0, 0)))}}
    out.update(pulse("body", rig.rest("body"), (0, 0, 3 * p["side"]), L))
    leg = "leg_r" if p["side"] < 0 else "leg_l"
    if leg in rig.bones:
        out.update(pulse(leg, rig.rest(leg), add(rig.rest(leg), (-10, 0, 0)), L))
    return L, out


def f_hair_toss(rig, p):
    L = 1.3 * p["s"]
    out = {"head": {"rotation": Tk((0, r3(rig.rest("head"))), (L * 0.25, (0, 0, -16 * p["side"])), (L * 0.45, (0, 0, 10 * p["side"])), (L, r3(rig.rest("head"))))}}
    out.update(both(rig, "hair_side", lambda b, s: {b: {"rotation": Tk((0, (0, 0, 0)), (L * 0.3, (0, 0, 22 * p["side"])), (L * 0.6, (0, 0, -8 * p["side"])), (L, (0, 0, 0)))}}))
    if rig.has("hair_back"):
        out.update(pulse("hair_back", rig.rest("hair_back"), (-18, 0, 0), L, 0.3, 0.5))
    return L, out


def f_tail_swish(rig, p):
    L = 1.4 * p["s"]
    out = {}
    for i, t in enumerate(rig.tails()):
        out.update(osc(t, rig.rest(t), 1, 26 * p["a"] * (0.6 + 0.3 * i), 2.5, L, 0.05, 0.95))
    return L, out


def f_ear_twitch(rig, p):
    L = 0.9
    ear = "ear_r" if p["side"] > 0 else "ear_l"
    out = {ear: {"rotation": Tk((0, r3(rig.rest(ear))), (0.1, r3(add(rig.rest(ear), (-15, 0, -22 * p["side"])))), (0.22, r3(rig.rest(ear))),
                                (0.4, r3(add(rig.rest(ear), (-10, 0, -16 * p["side"])))), (0.55, r3(rig.rest(ear))), (L, r3(rig.rest(ear))))}}
    return L, out


def f_wing_stretch(rig, p):
    L = 2.2 * p["s"]
    out = both(rig, "wing", lambda b, s: pulse(b, rig.rest(b), add(rig.rest(b), (-10, 25 * s, -55 * s)), L, 0.35, 0.7))
    out.update(both(rig, "wing_tip", lambda b, s: pulse(b, rig.rest(b), add(rig.rest(b), (0, 0, -25 * s)), L, 0.4, 0.65)))
    return L, out


SHARED = [("stretch", f_stretch, ("arm_r", "arm_l")), ("yawn", f_yawn, ("head",)), ("look_back", f_look_back, ("head",)),
          ("shift", f_shift, ("body",)), ("hair_toss", f_hair_toss, ("hair_side_r", "head")), ("tail_swish", f_tail_swish, ("tail",)),
          ("ear_twitch", f_ear_twitch, ("ear_r", "ear_l")), ("wing_stretch", f_wing_stretch, ("wing_r", "wing_l"))]


# ----------------------------------------------------------------------------- family fidgets
def f_knead(rig, p):
    L = 2.4 * p["s"]
    out = {}
    for side, ph in (("r", 0), ("l", math.pi)):
        b = f"arm_{side}"
        if b in rig.bones:
            keys = [(L * k / 8, r3(add(rig.rest(b), (-35 - 15 * math.sin(2 * math.pi * 2 * k / 8 + ph), 0, 0)))) for k in range(9)]
            keys[0], keys[-1] = (0, r3(rig.rest(b))), (L, r3(rig.rest(b)))
            out[b] = {"rotation": Tk(*keys)}
    out.update(pulse("body", rig.rest("body"), (10, 0, 0), L, 0.15, 0.85))
    out["body"]["position"] = Tk((0, (0, 0, 0)), (L * 0.15, (0, -0.8, 0)), (L * 0.85, (0, -0.8, 0)), (L, (0, 0, 0)))
    return L, out


def f_wash_face(rig, p):
    L = 2.4 * p["s"]
    arm = "arm_r" if p["side"] > 0 else "arm_l"
    out = osc(arm, add(rig.rest(arm), (-125, 0, -30 * p["side"])), 0, 18, 3, L, 0.15, 0.85)
    out[arm]["rotation"][H.t(0)] = list(r3(rig.rest(arm)))
    out[arm]["rotation"][H.t(L)] = list(r3(rig.rest(arm)))
    out.update(pulse("head", rig.rest("head"), (10, 0, -10 * p["side"]), L, 0.15, 0.85))
    return L, out


def f_shake(rig, p):
    L = 1.2 * p["s"]
    out = osc("body", rig.rest("body"), 2, 14 * p["a"], 4, L, 0.05, 0.85)
    out.update(osc("head", rig.rest("head"), 2, 22 * p["a"], 4, L, 0.05, 0.85))
    out.update(both(rig, "ear", lambda b, s: osc(b, rig.rest(b), 2, 25 * s, 4, L, 0.05, 0.85)))
    for i, t in enumerate(rig.tails()):
        out.update(osc(t, rig.rest(t), 1, 20, 4, L, 0.05, 0.85))
    return L, out


def f_tail_fan(rig, p):
    L = 2.4 * p["s"]
    out = {}
    tails = [b for b in sorted(rig.bones) if re.fullmatch(r"tail\d?", b) or re.fullmatch(r"tail_[abc]", b)] or rig.tails()[:1]
    for i, t in enumerate(tails):
        spread = (i - (len(tails) - 1) / 2) * 22 * p["a"]
        out.update(pulse(t, rig.rest(t), add(rig.rest(t), (-25, spread, 0)), L, 0.2 + 0.05 * i, 0.75))
    out.update(pulse("head", rig.rest("head"), (0, -25 * p["side"], 8 * p["side"]), L, 0.3, 0.7))
    return L, out


def f_stamp(rig, p):
    L = 1.6 * p["s"]
    leg = "leg_f_r" if p["side"] > 0 else "leg_f_l"
    if leg not in rig.bones:
        leg = "leg_r" if p["side"] > 0 else "leg_l"
    rest = rig.rest(leg)
    out = {leg: {"rotation": Tk((0, r3(rest)), (L * 0.15, r3(add(rest, (-35, 0, 0)))), (L * 0.3, r3(rest)), (L * 0.45, r3(add(rest, (-35, 0, 0)))), (L * 0.6, r3(rest)), (L, r3(rest)))}}
    out.update(pulse("head", rig.rest("head"), (-8, 0, 0), L, 0.2, 0.7))
    return L, out


def f_snort(rig, p):
    L = 1.6 * p["s"]
    out = pulse("head", rig.rest("head"), (18, 0, 0), L, 0.25, 0.6)
    out["head"]["position"] = Tk((0, (0, 0, 0)), (L * 0.25, (0, -0.3, -0.4)), (L * 0.6, (0, -0.3, -0.4)), (L, (0, 0, 0)))
    out.update(f_stamp(rig, p)[1])
    return L, out


def f_sway(rig, p):
    L = 3.0 * p["s"]
    out = osc("body", rig.rest("body"), 2, 9 * p["a"], 1.5, L, 0.05, 0.95)
    out.update(osc("head", rig.rest("head"), 2, -7 * p["a"], 1.5, L, 0.05, 0.95))
    for i, s in enumerate(rig.segs()[:6]):
        out.update(osc(s, rig.rest(s), 1, 6 + 2 * i, 1.5, L, 0.05, 0.95))
    return L, out


def f_leg_tap(rig, p):
    L = 1.8 * p["s"]
    out = {}
    legs = [f"leg{i}_{s}" for i in range(1, 5) for s in ("r", "l") if f"leg{i}_{s}" in rig.bones]
    for i, b in enumerate(legs):
        t0 = L * (0.05 + 0.08 * i)
        rest = rig.rest(b)
        out[b] = {"rotation": Tk((0, r3(rest)), (t0, r3(rest)), (t0 + 0.08, r3(add(rest, (0, 0, 14)))), (t0 + 0.16, r3(rest)), (L, r3(rest)))}
    if not legs:
        return f_wash_face(rig, p)
    return L, out


def f_fin_flutter(rig, p):
    L = 1.8 * p["s"]
    out = {}
    for i, f in enumerate(rig.fins()):
        out.update(osc(f, rig.rest(f), 1, 14 + 4 * i, 3, L, 0.05, 0.95))
    arm = "arm_r" if p["side"] > 0 else "arm_l"
    if arm in rig.bones:
        out.update(osc(arm, add(rig.rest(arm), (-150, 0, -20 * p["side"])), 2, 10, 2, L, 0.2, 0.8))
        out[arm]["rotation"][H.t(0)] = list(r3(rig.rest(arm)))
        out[arm]["rotation"][H.t(L)] = list(r3(rig.rest(arm)))
    return L, out


def f_wobble(rig, p):
    L = 1.6 * p["s"]
    keys = [(0, (1, 1, 1))]
    for k in range(1, 8):
        a = 0.12 * p["a"] * math.sin(math.pi * k / 2) * (1 - k / 8)
        keys.append((L * k / 8, r3((1 + a, 1 - a, 1 + a))))
    keys.append((L, (1, 1, 1)))
    return L, {"body": {"scale": Tk(*keys)}, "root": {"scale": Tk(*keys)}}


def f_antenna_clean(rig, p):
    L = 2.2 * p["s"]
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-155, 0, -22 * s), L, 0.25, 0.75))
    out.update(both(rig, "forearm", lambda b, s: osc(b, (-40, 0, 0), 0, 18, 3, L, 0.25, 0.75)))
    out.update(pulse("head", rig.rest("head"), (16, 0, 0), L, 0.25, 0.75))
    return L, out


def f_sun_face(rig, p):
    L = 3.0 * p["s"]
    out = pulse("head", rig.rest("head"), (-32, 0, 0), L, 0.3, 0.75)
    out.update(both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-20, 0, 45 * s), L, 0.3, 0.75)))
    if rig.has("eyelids"):
        out["eyelids"] = {"position": Tk((0, (0, 0, 0)), (L * 0.32, H.OUT_), (L * 0.72, H.OUT_), (L * 0.76, (0, 0, 0)))}
    return L, out


def f_creak(rig, p):
    L = 1.8 * p["s"]
    rest = rig.rest("head")
    steps = [(0, rest), (L * 0.15, rest), (L * 0.18, add(rest, (0, 0, 14 * p["side"]))), (L * 0.4, add(rest, (0, 0, 14 * p["side"]))),
             (L * 0.43, add(rest, (0, 0, 30 * p["side"]))), (L * 0.6, add(rest, (0, 0, 30 * p["side"]))), (L * 0.66, rest), (L, rest)]
    out = {"head": {"rotation": Tk(*[(t, r3(v)) for t, v in steps])}}
    out.update(both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), add(rig.rest(b), (6, 0, 4 * s)), L, 0.2, 0.7)))
    return L, out


def f_nails(rig, p):
    L = 2.4 * p["s"]
    arm, fore = ("arm_r", "forearm_r") if p["side"] > 0 else ("arm_l", "forearm_l")
    out = pulse(arm, rig.rest(arm), (-70, 0, -22 * p["side"]), L, 0.2, 0.8)
    if fore in rig.bones:
        out.update(pulse(fore, rig.rest(fore), (-70, 0, 0), L, 0.2, 0.8))
    out.update({"head": {"rotation": Tk((0, r3(rig.rest("head"))), (L * 0.25, (14, -12 * p["side"], 0)), (L * 0.6, (14, -12 * p["side"], 0)),
                                        (L * 0.7, (-10, 0, 0)), (L, r3(rig.rest("head"))))}})
    return L, out


def f_flex(rig, p):
    if rig.has("wing_r"):
        return f_wing_stretch(rig, p)
    L = 2.0 * p["s"]
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-80, 0, 70 * s), L, 0.3, 0.7))
    out.update(both(rig, "forearm", lambda b, s: pulse(b, rig.rest(b), (-90, 0, 0), L, 0.3, 0.7)))
    out.update(pulse("head", rig.rest("head"), (-12, 0, 0), L, 0.3, 0.7))
    return L, out


def f_reboot(rig, p):
    L = 2.2 * p["s"]
    rest = rig.rest("head")
    out = {"head": {"rotation": Tk((0, r3(rest)), (L * 0.15, (25, 0, 0)), (L * 0.6, (25, 0, 0)), (L * 0.63, (-8, 0, 0)), (L * 0.7, (4, 0, 0)), (L * 0.78, r3(rest)), (L, r3(rest)))}}
    out.update(both(rig, "arm", lambda b, s: {b: {"rotation": Tk((0, r3(rig.rest(b))), (L * 0.15, (8, 0, 2 * s)), (L * 0.6, (8, 0, 2 * s)), (L * 0.66, (-20, 0, 10 * s)), (L * 0.8, r3(rig.rest(b))), (L, r3(rig.rest(b))))}}))
    return L, out


def f_nibble(rig, p):
    L = 1.8 * p["s"]
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-100, 0, -24 * s), L, 0.2, 0.8))
    out.update(both(rig, "forearm", lambda b, s: pulse(b, rig.rest(b), (-45, 0, 0), L, 0.2, 0.8)))
    out.update(osc("head", add(rig.rest("head"), (10, 0, 0)), 0, 5, 5, L, 0.25, 0.75))
    out["head"]["rotation"][H.t(0)] = list(r3(rig.rest("head")))
    out["head"]["rotation"][H.t(L)] = list(r3(rig.rest("head")))
    return L, out


def f_curtsy(rig, p):
    L = 2.0 * p["s"]
    out = both(rig, "leg", lambda b, s: pulse(b, rig.rest(b), add(rig.rest(b), (-14 if s > 0 else 6, 0, 0)), L, 0.3, 0.65))
    out.update(both(rig, "shin", lambda b, s: pulse(b, rig.rest(b), add(rig.rest(b), (24, 0, 0)), L, 0.3, 0.65)))
    out.update(pulse("body", rig.rest("body"), (12, 0, 0), L, 0.3, 0.65))
    out["body"]["position"] = Tk((0, (0, 0, 0)), (L * 0.3, (0, -1.0, 0)), (L * 0.65, (0, -1.0, 0)), (L, (0, 0, 0)))
    out.update(both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-10, 0, 28 * s), L, 0.3, 0.65)))
    return L, out


def f_hop(rig, p):
    L = 1.4 * p["s"]
    out = {"root": {"position": Tk((0, (0, 0, 0)), (L * 0.15, (0, 2.2, 0)), (L * 0.3, (0, 0, 0)), (L * 0.45, (0, 1.6, 0)), (L * 0.6, (0, 0, 0)), (L, (0, 0, 0)))}}
    out.update(both(rig, "ear", lambda b, s: osc(b, rig.rest(b), 0, 18, 2, L, 0.05, 0.65)))
    return L, out


def f_pushup(rig, p):
    L = 1.8 * p["s"]
    out = {"body": {"position": Tk(*[(L * k / 6, (0, -0.9 if k % 2 else 0, 0)) for k in range(7)])}}
    out.update(pulse("head", rig.rest("head"), (-14, 0, 0), L, 0.15, 0.85))
    if rig.has("throat"):
        out["throat"] = {"scale": Tk((0, (1, 1, 1)), (L * 0.4, (1.5, 1.5, 1.5)), (L * 0.6, (1, 1, 1)), (L, (1, 1, 1)))}
    return L, out


def f_wrap(rig, p):
    if not rig.has("wing_r"):
        return f_curl(rig, p)
    L = 2.4 * p["s"]
    out = both(rig, "wing", lambda b, s: pulse(b, rig.rest(b), add(rig.rest(b), (0, -60 * s, 20 * s)), L, 0.3, 0.75))
    out.update(pulse("head", rig.rest("head"), (12, 0, 0), L, 0.3, 0.75))
    return L, out


def f_curl(rig, p):
    L = 2.4 * p["s"]
    out = pulse("body", rig.rest("body"), (32, 0, 0), L, 0.3, 0.75)
    out.update(pulse("head", rig.rest("head"), (28, 0, 0), L, 0.3, 0.75))
    out.update(both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-60, 0, -20 * s), L, 0.3, 0.75)))
    return L, out


def f_snip(rig, p):
    claws = [b for b in ("snip_r", "snip_l", "pincer_r", "pincer_l") if b in rig.bones]
    L = 1.6 * p["s"]
    if not claws:
        out = both(rig, "hand", lambda b, s: osc(b, rig.rest(b), 2, 20 * s, 3, L, 0.1, 0.9))
        out.update(both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-70, 0, 20 * s), L, 0.1, 0.9)))
        return L, out
    out = {}
    for b in claws:
        out.update(osc(b, rig.rest(b), 1, 18, 3, L, 0.1, 0.9))
    return L, out


def f_pouch(rig, p):
    L = 2.2 * p["s"]
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-35, 0, -18 * s), L, 0.3, 0.7))
    out.update(pulse("head", rig.rest("head"), (26, 0, 0), L, 0.3, 0.7))
    return L, out


def f_bird_tilt(rig, p):
    L = 1.6 * p["s"]
    rest = rig.rest("head")
    steps = [(0, rest), (L * 0.1, add(rest, (0, 0, 24 * p["side"]))), (L * 0.35, add(rest, (0, 0, 24 * p["side"]))),
             (L * 0.42, add(rest, (0, 0, -18 * p["side"]))), (L * 0.7, add(rest, (0, 0, -18 * p["side"]))), (L * 0.78, rest), (L, rest)]
    return L, {"head": {"rotation": Tk(*[(t, r3(v)) for t, v in steps])}}


def f_hat_tip(rig, p):
    L = 1.6 * p["s"]
    arm = "arm_r"
    out = pulse(arm, rig.rest(arm), (-160, 0, -20), L, 0.25, 0.6)
    out.update(pulse("head", rig.rest("head"), (14, 0, 0), L, 0.3, 0.6))
    return L, out


def f_tentacle(rig, p):
    L = 2.0 * p["s"]
    out = {}
    bones = [b for b in sorted(rig.bones) if b.startswith(("tent", "arm_t", "tentacle"))][:8]
    for i, b in enumerate(bones):
        out.update(osc(b, rig.rest(b), i % 3, 16, 2, L, 0.05 * (i % 4), 0.95))
    if not bones:
        return f_fin_flutter(rig, p)
    return L, out


FAMILY = {
    "NEKOMATA": [("knead", f_knead), ("wash_face", f_wash_face)], "KITSUNE": [("tail_fan", f_tail_fan), ("wash_face", f_wash_face)],
    "CANID": [("shake", f_shake)], "CENTAUR": [("stamp", f_stamp)], "MINOTAUR": [("snort", f_snort)], "LAMIA": [("sway", f_sway)],
    "ARACHNE": [("leg_tap", f_leg_tap)], "MERFOLK": [("fin_flutter", f_fin_flutter)], "SLIME": [("wobble", f_wobble)],
    "BUG": [("antenna_clean", f_antenna_clean)], "FLORA": [("sun_face", f_sun_face)], "UNDEAD": [("creak", f_creak)],
    "DEVIL": [("nails", f_nails)], "DRAGON": [("flex", f_flex)], "CONSTRUCT": [("reboot", f_reboot)], "RODENT": [("nibble", f_nibble)],
    "ELF": [("curtsy", f_curtsy)], "JACKALOPE": [("hop", f_hop)], "LIZARDFOLK": [("pushup", f_pushup)], "BAT": [("wrap", f_wrap)],
    "ARMOURED": [("curl", f_curl)], "MARSUPIAL": [("pouch", f_pouch)], "THEROPOD": [("bird_tilt", f_bird_tilt)], "RIDER": [("hat_tip", f_hat_tip)],
    "MOLLUSK": [("tentacle", f_tentacle)], "CRUSTACEAN": [("snip", f_snip)],
}


# ----------------------------------------------------------------------------- signature motions
def s_dance(rig, p):
    L = 2.6 * p["s"]
    out = {"root": {"rotation": Tk((0, (0, 0, 0)), (L * 0.5, (0, 180 * p["side"], 0)), (L * 0.9, (0, 360 * p["side"], 0)), (L, (0, 360 * p["side"], 0)))}}
    out.update(both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-150, 0, 30 * s), L, 0.2, 0.85)))
    return L, out


def s_sing(rig, p):
    L = 2.6 * p["s"]
    out = pulse("head", rig.rest("head"), (-20, 0, 6 * p["side"]), L, 0.25, 0.8)
    arm = "arm_r" if p["side"] > 0 else "arm_l"
    out.update(pulse(arm, rig.rest(arm), (-60, 0, -40 * p["side"]), L, 0.25, 0.8))
    if rig.has("mouth"):
        out["mouth"] = {"scale": Tk((0, (1, 1, 1)), (L * 0.25, (1, 1.5, 1)), (L * 0.5, (1, 1.2, 1)), (L * 0.65, (1, 1.6, 1)), (L * 0.8, (1, 1, 1)), (L, (1, 1, 1)))}
    return L, out


def s_salute(rig, p):
    L = 1.8 * p["s"]
    out = pulse("arm_r", rig.rest("arm_r"), (-150, 0, -40), L, 0.2, 0.7)
    if rig.has("forearm_r"):
        out.update(pulse("forearm_r", rig.rest("forearm_r"), (-70, 0, 0), L, 0.2, 0.7))
    out.update(pulse("head", rig.rest("head"), (-6, 0, 0), L, 0.2, 0.7))
    return L, out


def s_sniff(rig, p):
    L = 2.0 * p["s"]
    out = osc("head", add(rig.rest("head"), (-18, 12 * p["side"], 0)), 0, 4, 5, L, 0.2, 0.8)
    out["head"]["rotation"][H.t(0)] = list(r3(rig.rest("head")))
    out["head"]["rotation"][H.t(L)] = list(r3(rig.rest("head")))
    out.update(pulse("body", rig.rest("body"), (-5, 0, 0), L, 0.2, 0.8))
    return L, out


def s_gaze_up(rig, p):
    L = 3.0 * p["s"]
    out = pulse("head", rig.rest("head"), (-38, 8 * p["side"], 0), L, 0.3, 0.8)
    out.update(both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (20, 0, -10 * s), L, 0.3, 0.8)))
    return L, out


def s_weave(rig, p):
    L = 2.4 * p["s"]
    out = {}
    for side, ph in (("r", 0), ("l", math.pi)):
        b = f"arm_{side}"
        if b in rig.bones:
            s = 1 if side == "r" else -1
            keys = [(L * k / 8, r3((-70 + 15 * math.sin(2 * math.pi * 2 * k / 8 + ph), 0, -20 * s + 10 * math.cos(2 * math.pi * 2 * k / 8 + ph)))) for k in range(9)]
            keys[0], keys[-1] = (0, r3(rig.rest(b))), (L, r3(rig.rest(b)))
            out[b] = {"rotation": Tk(*keys)}
    out.update(pulse("head", rig.rest("head"), (16, 0, 0), L, 0.2, 0.8))
    return L, out


def s_dig(rig, p):
    L = 2.0 * p["s"]
    out = pulse("body", rig.rest("body"), (28, 0, 0), L, 0.15, 0.85)
    for side, ph in (("r", 0), ("l", math.pi)):
        b = f"arm_{side}"
        if b in rig.bones:
            keys = [(L * k / 8, r3((-60 + 35 * math.sin(2 * math.pi * 2 * k / 8 + ph), 0, 0))) for k in range(9)]
            keys[0], keys[-1] = (0, r3(rig.rest(b))), (L, r3(rig.rest(b)))
            out[b] = {"rotation": Tk(*keys)}
    return L, out


def s_cast(rig, p):
    L = 2.0 * p["s"]
    arm = "arm_r" if p["side"] > 0 else "arm_l"
    out = {arm: {"rotation": Tk((0, r3(rig.rest(arm))), (L * 0.3, (-175, 0, 0)), (L * 0.45, (-60, 0, 0)), (L * 0.75, (-60, 0, 0)), (L, r3(rig.rest(arm))))}}
    out.update(pulse("head", rig.rest("head"), (6, 0, 0), L, 0.4, 0.8))
    return L, out


def s_count(rig, p):
    L = 2.4 * p["s"]
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-60, 0, -25 * s), L, 0.15, 0.85))
    hand = "hand_r" if p["side"] > 0 else "hand_l"
    if hand in rig.bones:
        out.update(osc(hand, rig.rest(hand), 0, 20, 4, L, 0.2, 0.8))
    out.update(pulse("head", rig.rest("head"), (22, 0, 0), L, 0.15, 0.85))
    return L, out


def s_cradle(rig, p):
    L = 3.0 * p["s"]
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-55, 0, -38 * s), L, 0.2, 0.85))
    out.update(both(rig, "forearm", lambda b, s: pulse(b, rig.rest(b), (-40, 0, 0), L, 0.2, 0.85)))
    out.update(osc("body", rig.rest("body"), 2, 5, 1.5, L, 0.2, 0.85))
    out.update(pulse("head", rig.rest("head"), (20, 0, 6 * p["side"]), L, 0.2, 0.85))
    return L, out


def s_strum(rig, p):
    L = 2.4 * p["s"]
    out = pulse("arm_l", rig.rest("arm_l"), (-50, 0, -35), L, 0.15, 0.85) if rig.has("arm_l") else {}
    if rig.has("arm_r"):
        out.update(osc("arm_r", (-40, 0, 20), 2, 14, 4, L, 0.15, 0.85))
        out["arm_r"]["rotation"][H.t(0)] = list(r3(rig.rest("arm_r")))
        out["arm_r"]["rotation"][H.t(L)] = list(r3(rig.rest("arm_r")))
    out.update(osc("head", rig.rest("head"), 2, 6, 2, L, 0.15, 0.85))
    return L, out


def s_admire(rig, p):
    L = 2.6 * p["s"]
    arm = "arm_l" if p["side"] > 0 else "arm_r"
    out = pulse(arm, rig.rest(arm), (-95, 0, 25 * p["side"]), L, 0.2, 0.8)
    out.update({"head": {"rotation": Tk((0, r3(rig.rest("head"))), (L * 0.25, (4, 18 * p["side"], 10 * p["side"])), (L * 0.5, (4, 18 * p["side"], -10 * p["side"])),
                                        (L * 0.8, (4, 18 * p["side"], 10 * p["side"])), (L, r3(rig.rest("head"))))}})
    return L, out


def s_hide_face(rig, p):
    L = 2.2 * p["s"]
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-130, 0, -30 * s), L, 0.2, 0.75))
    out.update(both(rig, "forearm", lambda b, s: pulse(b, rig.rest(b), (-30, 0, 0), L, 0.2, 0.75)))
    out.update(pulse("head", rig.rest("head"), (22, 0, 0), L, 0.2, 0.75))
    return L, out


def s_proud(rig, p):
    L = 2.4 * p["s"]
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (10, 0, 40 * s), L, 0.2, 0.8))
    out.update(both(rig, "forearm", lambda b, s: pulse(b, rig.rest(b), (-80, 0, 0), L, 0.2, 0.8)))
    out.update(pulse("head", rig.rest("head"), (-16, 0, 0), L, 0.2, 0.8))
    out.update(pulse("body", rig.rest("body"), (-6, 0, 0), L, 0.2, 0.8))
    return L, out


def s_shiver(rig, p):
    L = 1.8 * p["s"]
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-40, 0, -45 * s), L, 0.15, 0.85))
    out.update(osc("body", rig.rest("body"), 2, 3, 8, L, 0.15, 0.85))
    out.update(osc("head", rig.rest("head"), 1, 4, 6, L, 0.15, 0.85))
    return L, out


def s_skyward(rig, p):
    L = 1.8 * p["s"]
    arm = "arm_r" if p["side"] > 0 else "arm_l"
    out = {arm: {"rotation": Tk((0, r3(rig.rest(arm))), (L * 0.2, (-178, 0, 0)), (L * 0.55, (-178, 0, 0)), (L * 0.6, (-160, 0, 0)), (L * 0.65, (-178, 0, 0)), (L, r3(rig.rest(arm))))}}
    out.update(pulse("head", rig.rest("head"), (-30, 0, 0), L, 0.2, 0.65))
    return L, out


def s_flame(rig, p):
    L = 2.0 * p["s"]
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-75, 0, 18 * s), L, 0.2, 0.8))
    out.update(both(rig, "hand", lambda b, s: osc(b, rig.rest(b), 2, 18 * s, 3, L, 0.25, 0.8)))
    out.update(pulse("head", rig.rest("head"), (18, 0, 0), L, 0.2, 0.8))
    return L, out


def s_breath(rig, p):
    L = 2.2 * p["s"]
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-105, 0, -28 * s), L, 0.2, 0.8))
    out.update(both(rig, "forearm", lambda b, s: pulse(b, rig.rest(b), (-55, 0, 0), L, 0.2, 0.8)))
    out.update(pulse("head", rig.rest("head"), (8, 0, 0), L, 0.2, 0.8))
    out["body"] = {"position": Tk((0, (0, 0, 0)), (L * 0.4, (0, 0.25, 0)), (L * 0.6, (0, -0.1, 0)), (L, (0, 0, 0)))}
    return L, out


def s_wring(rig, p):
    L = 2.4 * p["s"]
    arm = "arm_r" if p["side"] > 0 else "arm_l"
    out = pulse(arm, rig.rest(arm), (-140, 0, -10 * p["side"]), L, 0.2, 0.8)
    other = "arm_l" if arm == "arm_r" else "arm_r"
    if other in rig.bones:
        out.update(osc(other, (-120, 0, 30 * p["side"]), 1, 20, 3, L, 0.2, 0.8))
        out[other]["rotation"][H.t(0)] = list(r3(rig.rest(other)))
        out[other]["rotation"][H.t(L)] = list(r3(rig.rest(other)))
    out.update(pulse("head", rig.rest("head"), (6, 0, 18 * p["side"]), L, 0.2, 0.8))
    return L, out


def s_bow(rig, p):
    L = 2.2 * p["s"]
    out = pulse("body", rig.rest("body"), (38 * p["a"], 0, 0), L, 0.3, 0.6)
    out.update(pulse("head", rig.rest("head"), (10, 0, 0), L, 0.3, 0.6))
    arm = "arm_r" if p["side"] > 0 else "arm_l"
    if arm in rig.bones:
        out.update(pulse(arm, rig.rest(arm), (-40, 0, -35 * p["side"]), L, 0.3, 0.6))
    return L, out


def s_wave(rig, p):
    L = 2.0 * p["s"]
    arm = "arm_r" if p["side"] > 0 else "arm_l"
    out = osc(arm, (-160, 0, 0), 2, 25 * p["side"], 3, L, 0.15, 0.8)
    out[arm]["rotation"][H.t(0)] = list(r3(rig.rest(arm)))
    out[arm]["rotation"][H.t(L)] = list(r3(rig.rest(arm)))
    out.update(pulse("head", rig.rest("head"), (0, 0, 10 * p["side"]), L, 0.15, 0.8))
    return L, out


def s_point(rig, p):
    L = 2.0 * p["s"]
    arm = "arm_r" if p["side"] > 0 else "arm_l"
    out = pulse(arm, rig.rest(arm), (-92, 18 * p["side"], 0), L, 0.2, 0.75)
    out.update(pulse("head", rig.rest("head"), (0, 18 * p["side"], 0), L, 0.2, 0.75))
    return L, out


def s_crouch_wiggle(rig, p):
    L = 2.0 * p["s"]
    out = osc("body", add(rig.rest("body"), (22, 0, 0)), 1, 9, 3, L, 0.2, 0.8)
    out["body"]["rotation"][H.t(0)] = list(r3(rig.rest("body")))
    out["body"]["rotation"][H.t(L)] = list(r3(rig.rest("body")))
    out["body"]["position"] = Tk((0, (0, 0, 0)), (L * 0.2, (0, -1.2, 0)), (L * 0.8, (0, -1.2, 0)), (L, (0, 0, 0)))
    out.update(pulse("head", rig.rest("head"), (-18, 0, 0), L, 0.2, 0.8))
    return L, out


def s_clap(rig, p):
    L = 1.8 * p["s"]
    out = {}
    for side in ("r", "l"):
        b, s = f"arm_{side}", 1 if side == "r" else -1
        if b in rig.bones:
            keys = [(0, r3(rig.rest(b)))] + [(L * (0.15 + 0.6 * k / 6), r3((-80, 0, (-30 if k % 2 else -10) * s))) for k in range(7)] + [(L, r3(rig.rest(b)))]
            out[b] = {"rotation": Tk(*keys)}
    out.update(pulse("head", rig.rest("head"), (-8, 0, 0), L, 0.15, 0.8))
    return L, out


def s_slump(rig, p):
    L = 3.0 * p["s"]
    out = pulse("body", rig.rest("body"), (14, 0, 4 * p["side"]), L, 0.3, 0.8)
    out.update(pulse("head", rig.rest("head"), (24, 0, 10 * p["side"]), L, 0.3, 0.8))
    out.update(both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (6, 0, 2 * s), L, 0.3, 0.8)))
    if rig.has("eyelids"):
        out["eyelids"] = {"position": Tk((0, (0, 0, 0)), (L * 0.35, H.OUT_), (L * 0.75, H.OUT_), (L * 0.8, (0, 0, 0)))}
    return L, out


def s_guard(rig, p):
    L = 2.2 * p["s"]
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-30, 0, 30 * s), L, 0.2, 0.8))
    out.update(both(rig, "forearm", lambda b, s: pulse(b, rig.rest(b), (-100, 0, 0), L, 0.2, 0.8)))
    out.update({"head": {"rotation": Tk((0, r3(rig.rest("head"))), (L * 0.3, (0, 30, 0)), (L * 0.55, (0, -30, 0)), (L * 0.8, (0, 0, 0)), (L, r3(rig.rest("head"))))}})
    return L, out


def s_listen(rig, p):
    L = 2.2 * p["s"]
    out = pulse("head", rig.rest("head"), (0, 25 * p["side"], 18 * p["side"]), L, 0.25, 0.75)
    hand = "arm_r" if p["side"] > 0 else "arm_l"
    out.update(pulse(hand, rig.rest(hand), (-150, 0, -50 * p["side"]), L, 0.25, 0.75))
    return L, out


SIG = [  # keyword (in her signature, ecology, tag), motion: the first that matches is hers
    (r"danc|twirl|spin", s_dance), (r"\bsing|\bsongs?\b|music|lull|melod|\bvoice|\bcalls?\b", s_sing), (r"lute|harp|drum|flute|strum|play[s ]", s_strum),
    (r"knight|guard|protect|bodyguard|shield|soldier|warrior|duel|sword|spear", s_guard), (r"salute|march|patrol|order", s_salute),
    (r"sniff|scent|smell|nose|track", s_sniff), (r"moon|star|sky|night sky|astro|heaven", s_gaze_up), (r"thunder|lightning|storm|spark", s_skyward),
    (r"fire|flame|ember|burn|lava|heat|smoke", s_flame), (r"ice|snow|frost|cold|freez|winter", s_breath), (r"web|silk|weav|spin|thread|knit", s_weave),
    (r"\bdig|burrow|tunnel|\bmin(e|es|ing)\b|\bores?\b", s_dig), (r"\bfish|\bnets?\b|\blures?\b|\bcasts?\b", s_cast), (r"gold|coin|treasure|hoard|\bgems?\b|\btrades?\b|merchant|shiny", s_count),
    (r"\beggs?\b|\byoung\b|\bcubs?\b|\bpups?\b|\bkits?\b|mother|nurs|cradle|baby", s_cradle), (r"mirror|beaut|vain|admir|preen|pretty", s_admire),
    (r"shy|timid|hide|bashful|flee", s_hide_face), (r"proud|haught|regal|queen|noble|empress|lord", s_proud),
    (r"water|rain|river|swim|wet|splash|bath", s_wring), (r"greet|welcome|wave|friendly|cheer", s_wave), (r"bow|courte|polite|elegan", s_bow),
    (r"point|guide|lead|scout|direction|map", s_point), (r"pounce|stalk|ambush|hunt|prey|spring", s_crouch_wiggle), (r"listen|\bhears?\b|\bears?\b|\bsounds?\b", s_listen),
    (r"clap|cheer|happy|joy|merry|laugh", s_clap), (r"sleep|lazy|nap|drows|dream|slow", s_slump), (r"shiver|tremb|nerv|scare", s_shiver),
]
ACCENTS = ["tail", "ear", "blink", "tilt", "bounce", "hair", "nod"]


def accent(rig, p, name, L):
    """a small second flourish on top of her signature: what makes it hers"""
    out = {}
    t0 = L * p["late"]
    if name == "tail" and rig.tails():
        for i, t in enumerate(rig.tails()[:3]):
            out.update({t: {"rotation": Tk((0, r3(rig.rest(t))), (t0, r3(rig.rest(t))), (t0 + 0.1, r3(add(rig.rest(t), (0, 30 * p["side"], 0)))),
                                         (t0 + 0.25, r3(rig.rest(t))), (L, r3(rig.rest(t))))}})
    elif name == "ear" and rig.has("ear_r", "ear_l"):
        for b, s in (("ear_r", 1), ("ear_l", -1)):
            out[b] = {"rotation": Tk((0, r3(rig.rest(b))), (t0, r3(rig.rest(b))), (t0 + 0.08, r3(add(rig.rest(b), (-12, 0, -14 * s)))), (t0 + 0.2, r3(rig.rest(b))), (L, r3(rig.rest(b))))}
    elif name == "blink" and rig.has("eyelids"):
        out["eyelids"] = {"position": Tk((0, (0, 0, 0)), (t0, (0, 0, 0)), (t0 + 0.04, H.OUT_), (t0 + 0.12, H.OUT_), (t0 + 0.16, (0, 0, 0)), (L, (0, 0, 0)))}
    elif name == "bounce":
        out["root"] = {"position": Tk((0, (0, 0, 0)), (t0, (0, 0, 0)), (t0 + 0.12, (0, 0.8, 0)), (t0 + 0.24, (0, 0, 0)), (L, (0, 0, 0)))}
    elif name == "hair" and rig.has("hair_side_r"):
        out.update(both(rig, "hair_side", lambda b, s: {b: {"rotation": Tk((0, (0, 0, 0)), (t0, (0, 0, 0)), (t0 + 0.15, (0, 0, 14 * p["side"])), (t0 + 0.35, (0, 0, 0)), (L, (0, 0, 0)))}}))
    return out


def merge_into(base, extra):
    """extra's tracks join base's; a bone both move keeps base's (the main motion) unless base doesn't touch that channel"""
    for b, ch in extra.items():
        if b not in base:
            base[b] = ch
        else:
            for k, v in ch.items():
                base[b].setdefault(k, v)
    return base


def params(seed, extra=""):
    rnd = random.Random(int(hashlib.md5((seed + extra).encode()).hexdigest()[:8], 16))
    return {"s": round(rnd.uniform(0.85, 1.25), 2), "a": round(rnd.uniform(0.8, 1.2), 2), "side": rnd.choice((1, -1)),
            "late": round(rnd.uniform(0.55, 0.75), 2), "rnd": rnd}


# hand-made signatures for a few
def h_dullahan(rig, p):
    L = 2.4
    out = both(rig, "arm", lambda b, s: pulse(b, rig.rest(b), (-165, 0, -30 * s), L, 0.2, 0.8))
    out["head"] = {"rotation": Tk((0, r3(rig.rest("head"))), (L * 0.3, (0, 22, 4)), (L * 0.45, (0, -12, -3)), (L * 0.6, (0, 6, 0)), (L * 0.8, (0, 0, 0)), (L, r3(rig.rest("head")))),
                   "position": Tk((0, (0, 0, 0)), (L * 0.25, (0, 0.6, 0)), (L * 0.7, (0, 0.6, 0)), (L * 0.8, (0, 0, 0)), (L, (0, 0, 0)))}
    return L, out


def h_bee(rig, p):
    L = 2.4
    out = {"root": {"position": Tk(*[(L * k / 8, (round(1.2 * math.sin(2 * math.pi * k / 8 * 2), 2), 0, 0)) for k in range(9)])}}
    out.update(osc("body", rig.rest("body"), 2, 14, 6, L, 0.05, 0.95))
    return L, out


def h_cheshire(rig, p):
    L = 3.0
    out = pulse("head", rig.rest("head"), (0, 0, 28), L, 0.35, 0.85)
    for b in ("grin", "mouth"):
        if b in rig.bones:
            out[b] = {"scale": Tk((0, (1, 1, 1)), (L * 0.35, (1.5, 1.3, 1)), (L * 0.85, (1.5, 1.3, 1)), (L, (1, 1, 1)))}
    return L, out


def h_anubis(rig, p):
    L = 3.0
    out = {}
    for side, ph in (("r", 0), ("l", math.pi)):
        b, s = f"arm_{side}", 1 if side == "r" else -1
        if b in rig.bones:
            keys = [(0, r3(rig.rest(b)))] + [(L * (0.15 + 0.7 * k / 6), r3((-80 + 18 * math.sin(math.pi * k / 2 + ph), 0, 30 * s))) for k in range(7)] + [(L, r3(rig.rest(b)))]
            out[b] = {"rotation": Tk(*keys)}
    out.update(pulse("head", rig.rest("head"), (14, 0, 0), L, 0.15, 0.85))
    return L, out


def h_repeadead(rig, p):
    L = 2.0
    rest = rig.rest("head")
    steps = [(0, rest), (0.4, rest), (0.42, (0, 55, 8)), (1.2, (0, 55, 8)), (1.24, (0, -40, -10)), (1.7, (0, -40, -10)), (1.9, rest), (L, rest)]
    return L, {"head": {"rotation": Tk(*[(t, r3(v)) for t, v in steps])}}


def h_koschei(rig, p):
    L = 2.2
    out = pulse("arm_r", rig.rest("arm_r"), (-70, 0, -10), L, 0.2, 0.8)
    out.update(osc("forearm_r", (-30, 0, 0), 0, 12, 3, L, 0.25, 0.8))
    out.update(pulse("head", rig.rest("head"), (16, -20, 0), L, 0.2, 0.8))
    return L, out


HAND = {"dullahan_undead": h_dullahan, "bee_bug": h_bee, "queen_bee_bug": h_bee, "cheshire_nekomata": h_cheshire, "anubis_canid": h_anubis,
        "repeadead_undead": h_repeadead, "koschei_undead": h_koschei}


# ----------------------------------------------------------------------------- per model
def concepts():
    out = {}
    for fam in C.FAMILIES:
        for sp in fam["species"]:
            sig = sp.get("signature") or ("", "")
            out[sp["id"]] = " ".join([str(sig[0]), str(sig[1]), str(sp.get("ecology", "")), str(sp.get("tag", "")), str(sp.get("temperament", ""))]).lower()
    return out


def girls():
    out = {}
    for m in re.finditer(r'^\s+[A-Z0-9_]+\("([a-z0-9_]+)", Family\.([A-Z_]+),', open(SPECIES_JAVA, encoding="utf-8").read(), re.M):
        out[m.group(1)] = m.group(2)
    return out


def clip(rig, key, fn, p):
    """build one fidget: her idle's other tracks, the motion on top (authored -> file space via Anims)"""
    L, bones = fn(rig, p)
    an = H.Anims(rig.name, rig.bones)
    an.add("x", round(L, 2), False, copy.deepcopy(bones))
    moved = an.A[f"animation.{rig.name}.x"]["bones"]
    base = copy.deepcopy(rig.idle.get("bones", {}))
    for b, ch in moved.items():
        base.setdefault(b, {}).update(ch)
    return round(L, 2), {"loop": "hold_on_last_frame", "animation_length": round(L, 2), "bones": base}


def fidgets_for(model, species, family, text, rig, seen):
    out = {}
    for name, fn, need in SHARED:
        if all(n in rig.bones for n in need) and (name != "tail_swish" or rig.tails()):
            out[name] = clip(rig, name, fn, params(model, name))
    for name, fn in FAMILY.get(family, []):
        try:
            out[name] = clip(rig, name, fn, params(model, name))
        except KeyError:
            pass
    # her own: hand-made, or the motion her concept suggests, with a flourish of her own; never the same as another kind's
    p = params(species, "signature")
    if species in HAND:
        main = HAND[species]
    else:
        main = next((fn for pat, fn in SIG if re.search(pat, text)), None)
        if main is None:
            main = [s for _, s in SIG][int(hashlib.md5(species.encode()).hexdigest()[:6], 16) % len(SIG)]
    acc = ACCENTS[int(hashlib.md5((species + "acc").encode()).hexdigest()[:6], 16) % len(ACCENTS)]
    for attempt in range(6):
        def sig(r, pp, main=main, acc=acc):
            L, bones = main(r, pp)
            if acc == "tilt":
                bones.setdefault("head", {}).setdefault("rotation", Tk((0, r3(r.rest("head"))), (L * pp["late"], (0, 0, 12 * pp["side"])), (L, r3(r.rest("head")))))
            elif acc == "nod":
                bones.setdefault("head", {}).setdefault("rotation", Tk((0, r3(r.rest("head"))), (L * pp["late"], (14, 0, 0)), (L * pp["late"] + 0.15, (0, 0, 0)), (L, r3(r.rest("head")))))
            return L, merge_into(bones, accent(r, pp, acc, L))
        L, data = clip(rig, "signature", sig, p)
        key = json.dumps(data["bones"], sort_keys=True)
        if model != species or key not in seen:            # homage models share their species' signature
            break
        p = params(species, f"signature{attempt}")
        acc = ACCENTS[(ACCENTS.index(acc) + 1) % len(ACCENTS)]
    seen[key] = species
    out["signature"] = (L, data)
    return out


def main():
    texts = concepts()
    species = girls()
    seen = {}
    table = {}
    files = sorted(f[:-len(".animation.json")] for f in os.listdir(os.path.join(ASSETS, "animations", "entity")) if f.endswith(".animation.json"))
    for model in files:
        sp = model if model in species else next((s for s in species if model.startswith(s + "_")), None)
        if sp is None:
            continue
        geo_path = os.path.join(ASSETS, "geo", "entity", model + ".geo.json")
        anim_path = os.path.join(ASSETS, "animations", "entity", model + ".animation.json")
        if not os.path.exists(geo_path):
            continue
        geo = json.load(open(geo_path, encoding="utf-8"))
        adoc = json.load(open(anim_path, encoding="utf-8"))
        for k in [k for k in adoc["animations"] if ".fidget_" in k]:
            del adoc["animations"][k]
        rig = Rig(model, geo, adoc)
        made = fidgets_for(model, sp, species[sp], texts.get(sp, ""), rig, seen)
        for name, (L, data) in made.items():
            adoc["animations"][f"animation.{model}.fidget_{name}"] = data
        with open(anim_path, "w", encoding="utf-8", newline="\n") as fh:
            json.dump(adoc, fh, indent=2)
            fh.write("\n")
        if model == sp:
            table[sp] = [(n, int(round(L * 20))) for n, (L, _) in made.items()]
    rows = ",\n".join(f'            Map.entry("{sp}", "{",".join(f"{n}:{t}" for n, t in fs)}")' for sp, fs in sorted(table.items()))
    java = f"""package com.linkyoshi90.harmontide.registry;

import java.util.Map;

/** Generated by tools/gen_fidgets.py: each species' idle fidgets as "name:ticks,..." (entity/girl/Fidgets). Do not edit. */
public final class FidgetTable {{
    public static final Map<String, String> FIDGETS = Map.ofEntries(
{rows});

    private FidgetTable() {{}}
}}
"""
    open(OUT, "w", encoding="utf-8", newline="\n").write(java)
    sigs = len(set(seen))
    print(f"{len(table)} species, {sum(len(v) for v in table.values())} fidgets, {sigs} distinct signatures")


if __name__ == "__main__":
    main()
