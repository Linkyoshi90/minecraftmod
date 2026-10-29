#!/usr/bin/env python3
"""Theropods (theropod_field_guide.html): girls built like a 1920s museum sketch of a T. rex, but human.

    python tools/gen_theropod.py [species ...] [--previews]

A girl_family biped with human legs, arms and skin, the upper body hunched forward by HUNCH degrees (the head tilted
back up so she still looks ahead) and a long, thick tail held out behind to balance her. The species' own features (a
sail, hand claws, fern fronds) are kept small. She has a human face with a sharp-toothed grin; only the spinosaur has a
snout, a short crocodile's one, with the teeth along its sides.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
import gen_lizardfolk as LZ  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

HUNCH = 20
TAIL = [(3, 3, 4.5), (2.5, 2.5, 4.5), (2, 2, 4.5), (1.5, 1.5, 4), (1, 1, 3.5)]
TAIL_REST = [(-4 - HUNCH, 0, 0), (2, 0, 0), (2, 0, 0), (2, 0, 0), (2, 0, 0)]

SPECIES = {
    # the spinosaur: teal, a low sail down her spine and along her tail, a short crocodile snout, a tall paddle of a tail
    "spinosaur_theropod": dict(
        build="strong", outfit=dict(top=["halter"], bottom="loincloth", extras=["bracers"], colors=dict(leather="#2a3a3e", cloth2="#2a6a74")),
        hair="pixie", bangs="jagged", ears=None, legs="bare", foot="claw",
        tail=[(2.5, 3.25, 4.5), (2, 3, 4.5), (1.75, 2.75, 4.5), (1.25, 2.5, 4), (.75, 2, 3.5)], tail_look="scale",
        sail=True, croc=True,
        pal=dict(SKIN=skin("tan"), HAIR="#1e5a66", FUR="#3a8a96", LIGHT="#d0ece8", MARK="#1e4a56", EYE="#2a7ab0", CLOTH="#2a6a74", TRIM="#d0ece8",
                 pattern="spots", pupil=(0.9, 3.4), extra=dict(sail="#3a8a96", sail_rib="#1e4a56", claw="#e8e4d4")),
        variants={"green": dict(HAIR="#2a4a22", FUR="#5a7a3a", MARK="#2a3a1a", LIGHT="#d8e4c0", CLOTH="#4a6a2a", EYE="#c8a040",
                                extra=dict(sail="#6a8a3a", sail_rib="#2a3a1a", claw="#e8e4d4")),
                  "sand": dict(HAIR="#8a5a2a", FUR="#c8a06a", MARK="#7a4a22", LIGHT="#f0e0c0", CLOTH="#a84a2a", EYE="#3a6a8a",
                               extra=dict(sail="#c84a2a", sail_rib="#6a2a14", claw="#f0e8d4")),
                  "black": dict(HAIR="#141a20", FUR="#2a3036", MARK="#0a0e12", LIGHT="#a8b4c0", CLOTH="#1e2a3a", EYE="#4ab8e8",
                                extra=dict(sail="#2a6ac8", sail_rib="#142a5a", claw="#d8dce4")),
                  "red": dict(HAIR="#5a1e14", FUR="#a8442a", MARK="#4a1a10", LIGHT="#f0d0b8", CLOTH="#6a2a1a", EYE="#e8b040",
                              extra=dict(sail="#c8682a", sail_rib="#5a1e10", claw="#f0e4d0"))}),
    # the australovenator: pale blue with darker stripes, curved claws on her hands, a striped tail
    "australovenator_theropod": dict(
        build="athletic", outfit=dict(top=["bandeau"], bottom="shorts", extras=["belt"], colors=dict(cloth2="#4a6aa8", leather="#3a3a4a")),
        hair="shaggy", bangs="messy", ears=None, legs="bare", foot="claw", tail=TAIL, tail_look="scale", hand_claws=True,
        pal=dict(SKIN=skin("fair"), HAIR="#8ab0d8", FUR="#9ac0e4", LIGHT="#eef4fc", MARK="#4a6aa8", EYE="#2a3a6a", CLOTH="#4a6aa8", TRIM="#eef4fc",
                 pattern="bands", pupil=(0.9, 3.4), extra=dict(claw="#f0ece0")),
        variants={"tan": dict(HAIR="#a87a4a", FUR="#c8a07a", MARK="#6a4424", LIGHT="#f4e8d4", CLOTH="#6a4424", EYE="#3a2a1a"),
                  "grey": dict(HAIR="#8a8e96", FUR="#a0a4ac", MARK="#4a4e56", LIGHT="#eceef0", CLOTH="#4a4e56", EYE="#c84a3a"),
                  "green": dict(HAIR="#5a8a4a", FUR="#7aa86a", MARK="#2a5a2a", LIGHT="#e4f0dc", CLOTH="#2a5a2a", EYE="#e8b040"),
                  "rust": dict(HAIR="#8a3a1e", FUR="#b8603a", MARK="#5a2410", LIGHT="#f4dcc8", CLOTH="#5a2410", EYE="#2a4a6a")}),
    # the fernstalker: navy and white, a feathered tail, three small fern fronds at her back and a feather crest
    "fernstalker_theropod": dict(
        build="athletic", outfit=dict(top=["bandeau", "harness"], bottom="loincloth", extras=["bracers"], colors=dict(leather="#2a2a3a", cloth2="#2a3a7a")),
        hair="shaggy", bangs="jagged", ears=None, legs="bare", foot="claw", tail=TAIL, tail_look="fur", ferns=True,
        pal=dict(SKIN=skin("fair"), HAIR="#1e2a5a", FUR="#2a3a7a", LIGHT="#e8eef8", MARK="#0e1630", EYE="#c83a2a", CLOTH="#2a3a7a", TRIM="#e8eef8",
                 pupil=(0.9, 3.4), extra=dict(frond="#8ab04a", frond_vein="#4a6a1e", feather="#1e2a5a", feather_tip="#e8eef8", claw="#e8eef8")),
        variants={"green": dict(HAIR="#1e4a2a", FUR="#2a6a3a", MARK="#0e2a14", CLOTH="#2a6a3a", EYE="#e8c040",
                                extra=dict(frond="#a8c860", frond_vein="#5a7a2a", feather="#1e4a2a", feather_tip="#e8f4e0", claw="#e8f4e0")),
                  "rust": dict(HAIR="#6a2a1a", FUR="#8a3a22", MARK="#3a120a", CLOTH="#8a3a22", EYE="#e8c040",
                               extra=dict(frond="#c8a048", frond_vein="#6a4a1e", feather="#6a2a1a", feather_tip="#f8e8d0", claw="#f8e8d0")),
                  "ash": dict(HAIR="#3a3a40", FUR="#5a5a62", MARK="#1e1e22", CLOTH="#3a3a40", EYE="#5ad8f0",
                              extra=dict(frond="#7a9a6a", frond_vein="#3a5a2a", feather="#2a2a30", feather_tip="#e8e8ec", claw="#e8e8ec")),
                  "violet": dict(HAIR="#3a1e5a", FUR="#5a3a8a", MARK="#1e0e30", CLOTH="#5a3a8a", EYE="#f8a040",
                                 extra=dict(frond="#6ab07a", frond_vein="#2a6a3a", feather="#3a1e5a", feather_tip="#f0e8fc", claw="#f0e8fc"))}),
}


def grin(pal):
    """a wide grin full of little pointed teeth (on the 32x32 face: rows 26-29)"""
    px = {(10, 25): pal.LIP_D, (21, 25): pal.LIP_D}
    for x in range(11, 21):
        px[(x, 26)] = pal.LIP_D
        px[(x, 27)] = pal.WHITE
        px[(x, 28)] = pal.WHITE if x % 2 else pal.MOUTH_IN       # upper teeth, points down
    for x in range(12, 20):
        px[(x, 29)] = pal.WHITE if x % 2 == 0 else pal.MOUTH_IN  # lower teeth, points up
    for x in range(13, 19):
        px[(x, 30)] = pal.LIP_D
    return px


def declare_hook(sp, d):
    out = []
    if d.get("sail"):                                    # a low fin down her spine, and its tail
        out += [("sail", .25, 7.5, 3), ("sail_t", .25, 2, 4.5)]
    if d.get("croc"):
        out += [("croc_snout", 3.5, 1.75, 2.5)]
    if d.get("hand_claws"):
        out += [("hclaw", .5, .5, 1.75)]
    if d.get("ferns"):                                   # the fernstalker's fronds, smaller than the lizardfolk's
        out += [("frond", 2.25, .25, 6.5), ("crest_feather", .5, 1.25, 3.5)]
    return out


def paint_hook(pt, pal, d):
    e = pal.extra
    sk = H.skin_fn(pal, 0.68)
    claw = G.shades(e.get("claw", "#e8e0cc"))
    pt("paw", "all", sk)                                 # bare human feet, only the toenails clawed
    pt("toe", "all", lambda f, x, y, w, h: pick(claw, 0.55 + 0.2 * (f == "up"), x, y))
    pt("hclaw", "all", lambda f, x, y, w, h: pick(claw, 0.5 + 0.25 * (f == "up"), x, y))
    sail, rib = G.shades(e.get("sail", "#3a8a96")), G.shades(e.get("sail_rib", "#1e4a56"))

    def fin(f, x, y, w, h):                              # broad faces only: ribs fanning up, the upper corner rounded off
        if f not in ("east", "west"):
            return None
        if y < 2 and (x < 1 or x >= w - 1):
            return None
        return pick(rib if x % 3 == 1 else sail, 0.4 + 0.25 * (y / max(1, h - 1)) + (noise(x, y, 61) - 0.5) * 0.1, x, y)
    pt("sail", "all", fin)
    pt("sail_t", "all", fin)
    scale = G.scale_fn(G.pal_from(d["pal"], dict(FUR=e.get("snout", "#c8a07a"), pattern="solid")))
    teeth = G.shades("#f8f4e8")

    def snout(f, x, y, w, h):                            # scaled on top, skin below, a row of teeth along the jaw line
        if f == "up":
            return pick(pal.SKIN, 0.3, x, y) if y == 1 and x in (2, w - 3) else scale(f, x, y, w, h)
        if f in ("east", "west", "north") and y >= h - 3:
            if y == h - 3:
                return pick(teeth, 0.75, x, y) if x % 2 == 0 else pick(G.shades("#5a1e22"), 0.4, x, y)
            return H.skin_fn(pal, 0.62)(f, x, y, w, h)
        return scale(f, x, y, w, h) if f != "down" else H.skin_fn(pal, 0.6)(f, x, y, w, h)
    pt("croc_snout", "all", snout)
    if d.get("ferns"):
        LZ.paint_hook(pt, pal, dict(ferns=True))


def build_hook(r, g, d):
    z, hy = g.z, g.hy
    if d.get("sail"):
        cd = G.S("chest")[2]
        b = cd / 2 - 0.2 + z
        r.put("body", cube("sail", [-0.125, 12.75, b]))
        r.put("tail", cube("sail_t", [-0.125, 12.6 + 3.25 / 2 - 0.2, r.b["tail"]["pivot"][2] + 0.5]))
        r.put("tail_2", cube("sail_t", [-0.125, 12.6 + 3.0 / 2 - 0.2, r.b["tail_2"]["pivot"][2] - 0.25]))
    if d.get("croc"):                                    # a short snout over her mouth; the grin moves onto its jaw
        r.put("head", cube("croc_snout", [-1.75, hy + 0.1, -6.4 + z]))
    if d.get("hand_claws"):                              # three hooked claws curling forward from each hand (hand_l mirrors hand_r)
        px, py, pz = r.b["hand_r"]["pivot"]
        r.put("hand_r", *[G.rot_cube("hclaw", [px + dx - 0.25, py - 2.6, pz - 1.4], [px + dx, py - 2.1, pz - 0.5], 30, 0, 0)
                          for dx in (-0.45, 0.05)])
    if d.get("ferns"):
        cd = G.S("chest")[2]
        b = cd / 2 - 0.2 + z
        r.add("ferns", "body", [0, 14, b], [G.rot_cube("frond", [-1.125, 14.0, b], [0, 14.0, b], 35, a, 0) for a in (-28, 0, 28)])
        r.add("hair_crest", "head", [0, hy + 8.4, z], [G.rot_cube("crest_feather", [x - 0.25, hy + 8.2, -0.5 + z], [x, hy + 8.4, -0.5 + z], -15, x * 8, 0)
                                                       for x in (-1.0, 0, 1.0)])


def hunch(an):
    """lean every pose's upper body forward by HUNCH degrees, and tilt the head back up by the same"""
    for A in an.A.values():
        bones = A["bones"]
        for bone, deg in (("body", HUNCH), ("head", -HUNCH)):
            ch = bones.setdefault(bone, {})
            rot = ch.get("rotation")
            if rot is None:
                ch["rotation"] = H.hold(deg, 0, 0)
            else:
                ch["rotation"] = {k: [v[0] + deg, v[1], v[2]] for k, v in rot.items()}


def anim_extra(an, sp, d):
    """the lizardfolk's extra poses (basking: theropods are cold-blooded too), then the hunch over every pose"""
    LZ.anim_extra(an, sp, d)
    hunch(an)


_pal_for = F.pal_for


def pal_for(d, key=None):
    """the family palette, with the toothy grin (the spinosaur's teeth are on her snout instead)"""
    pal = _pal_for(d, key)
    pal.mouth_px = {} if d.get("croc") else grin(pal)
    return pal


if __name__ == "__main__":
    F.pal_for = pal_for
    for _d in SPECIES.values():
        _d.setdefault("tail_rest", TAIL_REST[:len(_d["tail"])])
        _d.setdefault("tail_sway", 5)
    F.run_family("theropod", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
