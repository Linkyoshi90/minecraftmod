"""Body builds and outfits for the girl_lib families (until now every girl had the same torso, tube top and strip skirt).

A species opts in with two knobs on girl_lib.upper() (and the same `build` on girl_lib.declare(), which sizes the parts):

    build    standard | slim | petite | athletic | curvy | strong   (chest, waist, hips, bust, arm and thigh sizes)
    outfit   dict(top=[...], bottom=..., extras=[...])

Tops (several can be layered, e.g. ["bandeau", "mantle"]):
    wrap      the old tube top                    bandeau   a band across the bust, shoulders and ribs bare
    halter    tied behind the neck                sports    a racer-back crop top
    sarashi   chest bindings wound round the torso (a fighter's)
    sweater   a knitted crop sweater with long sleeves and a turtleneck
    vest      an open leather vest (wear it over a bandeau)
    mantle    a shaggy fur shawl over the shoulders, with drapes down the front
    capelet   a short shoulder cape with a clasp
    kimono    a wrap top crossed at the front, wide hanging sleeves and an obi with a bow
    shells    two scallop shells (merfolk)        corset    a laced bodice over the waist
    harness   leather straps across the chest, meeting at a ring
Bottoms:
    strips    the old hanging-strip skirt         shorts    shorts with legs on the thighs
    loincloth front and back flaps on a belt      wrap_skirt  a flared panel skirt
    sarong    a long cloth knotted at the hip, cut on the diagonal
    hakama    long pleated trousers-skirt (shrine maiden)
Extras: belt, pouch, bell_collar, arm_wraps, armlets, scarf, pearls, usekh (an Egyptian broad collar), pauldron,
bracers, satchel.

Colours (outfit["colors"] or the palette's extra dict): tops use CLOTH, bottoms CLOTH2 (pal.extra["cloth2"], else a darker CLOTH), leather pal.extra["leather"],
bindings pal.extra["bandage"], metal TRIM. Without the knobs girl_lib builds the old body byte for byte.
"""
import math

import harpy_lib as H
from harpy_lib import cube, hang, mirror_cube, pick, noise, rgb, ramp, CLEAR, BROAD

D = H.D

# ----------------------------------------------------------------------------- builds
BUILDS = {
    "standard": {},
    "slim": dict(chest=(4.5, 3, 3), midriff=(3.25, 2.75, 2.25), hips=(4.5, 1.25, 3.25), bust=(1.75, 1.25, .5), arm=1.25, thigh=2.0),
    "petite": dict(chest=(4.5, 3, 3), midriff=(3.25, 2.75, 2.25), hips=(4.5, 1.25, 3.25), bust=(1.5, 1, .5), arm=1.25, thigh=2.0),
    "athletic": dict(chest=(5.5, 3, 3.25), midriff=(3.75, 2.75, 2.5), hips=(5, 1.25, 3.5), bust=(2, 1.5, .75), arm=1.75, thigh=2.5),
    "curvy": dict(chest=(5, 3, 3.25), midriff=(3.25, 2.75, 2.5), hips=(5.75, 1.25, 4), bust=(2.25, 1.75, 1), arm=1.5, thigh=2.75),
    "strong": dict(chest=(5.75, 3, 3.5), midriff=(4, 2.75, 2.75), hips=(5.25, 1.25, 3.75), bust=(2, 1.5, .75), arm=2.0, thigh=2.75),
    # the newt: a grown woman's frame, long and lean, nothing on the chest to drag along the ground
    "lithe": dict(chest=(5, 3, 3), midriff=(3.5, 2.75, 2.25), hips=(5.25, 1.25, 3.5), bust=(2, 1.25, .25), arm=1.5, thigh=2.5),
    # the minotaur bulls (the one family with men): broad flat chest, thick arms, no bust (two flat pectoral plates)
    "male": dict(chest=(6.5, 3.25, 3.5), midriff=(4.75, 2.75, 3), hips=(5.25, 1.25, 3.5), bust=(2.75, 1.5, .25), arm=2.25, thigh=3.0),
}


def build_parts(build):
    """part re-declarations for a build (declared after the shared upper parts, so they win)"""
    b = BUILDS[build or "standard"]
    out = []
    for p in ("chest", "midriff", "hips", "bust"):
        if p in b:
            out.append((p,) + b[p])
    a = b.get("arm")
    if a:
        out += [("arm_up", a, 3.5, a), ("forearm", a, 3, a), ("cuff", a + .25, 1, a + .25), ("palm", a, 1.5, 1), ("fingers", a, 1, 1)]
    return out


def thigh_width(build, default):
    return BUILDS[build or "standard"].get("thigh", default)


# ----------------------------------------------------------------------------- outfit parts
def S(p):
    return H.PARTS[p]["size"]


def outfit_parts(outfit, build=None):
    """the parts an outfit needs; shells copy the size of the body part they cover (so they follow the build)"""
    if not outfit:
        return []
    b = dict(chest=(5, 3, 3.25), midriff=(3.5, 2.75, 2.5), hips=(5, 1.25, 3.5), bust=(2, 1.5, .75))
    b.update({k: v for k, v in BUILDS[build or "standard"].items() if k in b})
    a = BUILDS[build or "standard"].get("arm", 1.5)
    cw, ch, cd = b["chest"]
    mw, mh, md = b["midriff"]
    hw, hh, hd = b["hips"]
    bw, bh, bd = b["bust"]
    tops = outfit.get("top", [])
    tops = [tops] if isinstance(tops, str) else tops
    bottom = outfit.get("bottom")
    extras = outfit.get("extras", [])
    out = []
    if "sweater" in tops:
        out += [("sh_chest", cw, ch, cd), ("sh_arm", a, 3.5, a), ("sh_fore", a, 3, a), ("turtle", 3.75, 1.25, 3), ("knit_bust", bw, bh, bd)]
    if "vest" in tops:
        out += [("vest_c", cw, ch, cd), ("vest_m", mw, 1.5, md)]
    if "mantle" in tops:
        out += [("mantle", cw + 2 * a + 1.25, 2.25, cd + 1.5), ("mantle_f", 2.25, 4.5, .5)]
    if "capelet" in tops:
        out += [("cape_t", cw + 0.5, 1, cd + 0.75), ("cape_b", cw + 2 * a + 1.0, 2, cd + 1.5), ("clasp", 1, 1, .5)]
    if "halter" in tops:
        out += [("neckband", 3.75, .5, 3)]
    if "kimono" in tops:
        out += [("kimono_c", cw, ch, cd), ("kimono_m", mw, mh, md), ("kimono_b", bw, bh, bd), ("sleeve_w", a + 1.0, 4.5, a + 1.25),
                ("obi", mw, 1.75, md), ("obi_bow", 2.5, 1.5, .75)]
    if "shells" in tops:
        out += [("shell_c", bw + .25, bh + .25, .5)]
    if "corset" in tops:
        out += [("corset", mw, mh, md)]
    if "harness" in tops:
        out += [("ring", 1, 1, .25)]
    if bottom == "hakama":
        out += [("hakama_f", hw + 0.75, 8.5, .25), ("hakama_s", .25, 8.5, hd + 0.5)]
    if "pearls" in extras:
        out += [("pearl", .5, .5, .5)]
    if "usekh" in extras:
        out += [("usekh", 6, .5, 4.5), ("usekh_f", 4.5, 2, .25)]
    if "pauldron" in extras:
        out += [("pauldron", a + 1.0, 1.25, a + 1.0)]
    if "bracers" in extras:
        out += [("bracer", a, 2.25, a)]
    if "satchel" in extras:
        out += [("satchel", 1.25, 2, 2.25), ("strap", .5, 8.5, .25)]
    if bottom == "shorts":
        t = thigh_width(build, 2.25) + 0.3                # just wider than her thigh
        out += [("sh_hips", hw, 2.0, hd), ("short_leg", t, 2.25, t)]
    if bottom == "loincloth":
        out += [("flap_f", 3.25, 6, .25), ("flap_b", 3.5, 5, .25)]
    if bottom == "wrap_skirt":
        out += [("panel_f", hw + 0.25, 5, .25), ("panel_s", .25, 5, hd + 0.25)]
    if bottom == "sarong":
        out += [("sarong", hw + 0.5, 7.5, .25), ("sarong_b", hw + 0.5, 6, .25), ("knot", 1.25, 1.25, .75), ("knot_end", .75, 2.5, .25)]
    if bottom in ("shorts", "loincloth", "wrap_skirt", "sarong", "hakama") or "belt" in extras:
        out += [("belt", hw + 0.4, .75, hd + 0.4), ("buckle", 1, 1, .25)]
    if "pouch" in extras:
        out += [("pouch", 1.5, 1.75, 1)]
    if "bell_collar" in extras:
        out += [("neck_ring", 3.9, .6, 3.15), ("bell", 1, 1, 1)]
    if "arm_wraps" in extras:
        out += [("wrap_fa", a, 2.5, a), ("wrap_palm", a, 1.25, 1)]
    if "armlets" in extras:
        out += [("armlet", a, .6, a)]
    if "scarf" in extras:
        out += [("scarf", 4.25, 1.5, 3.5), ("scarf_tail", 1.25, 4.5, .4)]
    return out


def _tops(outfit):
    t = outfit.get("top", [])
    return [t] if isinstance(t, str) else list(t)


# ----------------------------------------------------------------------------- geometry
def torso(r, s, W, dy, z, parent):
    """body + belly + skirt bone, sized from the declared parts (identical numbers to the old body for 'standard')"""
    outfit = s.get("outfit") or {}
    cw, ch, cd = S("chest")
    mw, mh, md = S("midriff")
    hw, hh, hd = S("hips")
    bw, bh, bd = S("bust")
    body = [cube("midriff", [-mw / 2, 13.25 + dy, -md / 2 + z]), cube("chest", [-cw / 2, 16 + dy, -cd / 2 + z]),
            cube("collar", [-1.75, 19 + dy, -1.375 + z])]
    if s.get("hips", True):
        body.insert(0, cube("hips", [-hw / 2, 12 + dy, -hd / 2 + z]))
    if not outfit:                                    # the old sash and pendant
        if s.get("sash", True):
            body.append(cube("sash", [-2.75, 12.6 + dy, -2.0 + z]))
        if s.get("pendant", True):
            body += [cube("pendant", [-0.5, 17.6 + dy, -2.45 + z]), cube("cord", [-1.5, 18.55 + dy, -2.2 + z])]
    bust = cube("bust", [-0.25 - bw, 16.9 + dy, -cd / 2 - bd + z])
    body += [bust, mirror_cube(bust)]
    sk = []
    tops = _tops(outfit)
    ex = outfit.get("extras", [])
    bottom = outfit.get("bottom", "strips" if s.get("skirt") else None)
    fz = -cd / 2 - bd + z                             # the front of the bust

    # tops
    if "sweater" in tops:
        body.append(cube("sh_chest", [-cw / 2, 16 + dy, -cd / 2 + z], inflate=0.2))
        kb = cube("knit_bust", [-0.25 - bw, 16.9 + dy, -cd / 2 - bd + z], inflate=0.15)
        body += [kb, mirror_cube(kb)]
        body.append(cube("turtle", [-1.875, 19 + dy, -1.5 + z]))
    if "vest" in tops:
        body.append(cube("vest_c", [-cw / 2, 16 + dy, -cd / 2 + z], inflate=0.3))
        body.append(cube("vest_m", [-mw / 2, 14.5 + dy, -md / 2 + z], inflate=0.3))
    if "mantle" in tops:
        mw_, mh_, md_ = S("mantle")
        body.append(cube("mantle", [-mw_ / 2, 18.1 + dy, -md_ / 2 + z]))
        drape = cube("mantle_f", [-cw / 2 - 0.5, 14.2 + dy, fz - 0.6], pivot=[-cw / 2 + 0.6, 18.7 + dy, fz - 0.35], rotation=[-4, 0, 8])
        body += [drape, mirror_cube(drape)]
    if "capelet" in tops:
        tw, th, td = S("cape_t")
        bw2, bh2, bd2 = S("cape_b")
        body.append(cube("cape_t", [-tw / 2, 19 + dy, -td / 2 + z]))
        body.append(cube("cape_b", [-bw2 / 2, 17.1 + dy, -bd2 / 2 + z]))
        body.append(cube("clasp", [-0.5, 18.9 + dy, -td / 2 - 0.45 + z]))
    if "halter" in tops:
        body.append(cube("neckband", [-1.875, 19.55 + dy, -1.5 + z]))
    if "kimono" in tops:
        body.append(cube("kimono_c", [-cw / 2, 16 + dy, -cd / 2 + z], inflate=0.25))
        body.append(cube("kimono_m", [-mw / 2, 13.25 + dy, -md / 2 + z], inflate=0.25))
        kb = cube("kimono_b", [-0.25 - bw, 16.9 + dy, -cd / 2 - bd + z], inflate=0.2)
        body += [kb, mirror_cube(kb)]
        body.append(cube("obi", [-mw / 2, 13.5 + dy, -md / 2 + z], inflate=0.4))
        body.append(cube("obi_bow", [-1.25, 13.6 + dy, md / 2 + 0.4 + z]))
    if "shells" in tops:
        sc = cube("shell_c", [-0.25 - bw - 0.125, 16.9 - 0.125 + dy, fz - 0.35])
        body += [sc, mirror_cube(sc)]
    if "corset" in tops:
        body.append(cube("corset", [-mw / 2, 13.25 + dy, -md / 2 + z], inflate=0.2))
    if "harness" in tops:
        body.append(cube("ring", [-0.5, 16.6 + dy, -cd / 2 - 0.3 + z]))
    if "usekh" in ex:
        body.append(cube("usekh", [-3, 19.0 + dy, -2.25 + z]))
        body.append(cube("usekh_f", [-2.25, 17.4 + dy, fz - 0.3]))
    if "pearls" in ex:
        for i in range(7):
            u = (i - 3) / 3.0
            body.append(cube("pearl", [u * 1.7 - 0.25, 18.35 + 0.55 * u * u + dy, -cd / 2 - 0.45 + z]))
    if "satchel" in ex:
        sw2, sh2, sd2 = S("satchel")
        body.append(cube("satchel", [-hw / 2 - sw2 - 0.1, 11.6 + dy, -sd2 / 2 + z]))
        body.append(hang("strap", 1.9, 19.7 + dy, -cd / 2 - bd - 0.3 + z, -30, 0))
        body.append(hang("strap", 1.9, 19.7 + dy, cd / 2 + 0.1 + z, -30, 0))

    # bottoms (hanging cloth goes on the skirt bone, which the animations sway)
    belt_y = 12.9 + dy
    if bottom in ("shorts", "loincloth", "wrap_skirt", "sarong", "hakama") or "belt" in ex:
        bw3, _, bd3 = S("belt")
        body.append(cube("belt", [-bw3 / 2, belt_y, -bd3 / 2 + z]))
        body.append(cube("buckle", [-0.5, belt_y - 0.125, -bd3 / 2 - 0.2 + z]))
    if bottom == "strips":
        for i, (p, x, rz) in enumerate((("cloth_m", -3.0, 5), ("cloth_l", -1.5, -3), ("cloth_s", -0.2, 2), ("cloth_l", 1.0, -2), ("cloth_m", 2.0, 6))):
            sk.append(hang(p, x, 13.7 + dy, -hd / 2 - 0.65 - 0.15 * (i % 2) + z, rz, -8))
        for i, (p, x, rz) in enumerate((("cloth_m", -3.0, 3), ("cloth_s", -1.0, -2), ("cloth_l", 0.8, 3), ("cloth_m", 2.0, -3))):
            sk.append(hang(p, x, 13.7 + dy, hd / 2 + 0.25 + 0.15 * (i % 2) + z, rz, 8))
        side = cube("cloth_side", [-hw / 2 - 0.9, 9.2 + dy, -1.2 + z], pivot=[-hw / 2 - 0.65, 13.7 + dy, z], rotation=[0, 0, -8])
        sk += [side, mirror_cube(side)]
    elif bottom == "shorts":
        sw, sh, sd = S("sh_hips")
        body.append(cube("sh_hips", [-sw / 2, 11.25 + dy, -sd / 2 + z], inflate=0.2))
    elif bottom == "loincloth":
        fw, fh, _ = S("flap_f")
        sk.append(hang("flap_f", -fw / 2, belt_y + 0.5, -S("belt")[2] / 2 - 0.3 + z, 0, -6))
        bw4, bh4, _ = S("flap_b")
        sk.append(hang("flap_b", -bw4 / 2, belt_y + 0.5, S("belt")[2] / 2 + 0.05 + z, 0, 8))
    elif bottom == "wrap_skirt":
        pw, ph, _ = S("panel_f")
        sk.append(hang("panel_f", -pw / 2, belt_y + 0.4, -hd / 2 - 0.35 + z, 0, -14))
        sk.append(hang("panel_f", -pw / 2, belt_y + 0.4, hd / 2 + 0.1 + z, 0, 14))
        _, _, sd2 = S("panel_s")
        side = cube("panel_s", [-hw / 2 - 0.4, belt_y + 0.4 - 5, -sd2 / 2 + z], pivot=[-hw / 2 - 0.3, belt_y + 0.4, z], rotation=[0, 0, -14])
        sk += [side, mirror_cube(side)]
    elif bottom == "hakama":
        pw, ph, _ = S("hakama_f")
        sk.append(hang("hakama_f", -pw / 2, belt_y + 0.5, -hd / 2 - 0.4 + z, 0, -6))
        sk.append(hang("hakama_f", -pw / 2, belt_y + 0.5, hd / 2 + 0.15 + z, 0, 6))
        _, _, sd2 = S("hakama_s")
        side = cube("hakama_s", [-hw / 2 - 0.55, belt_y + 0.5 - ph, -sd2 / 2 + z], pivot=[-hw / 2 - 0.4, belt_y + 0.5, z], rotation=[0, 0, -7])
        sk += [side, mirror_cube(side)]
    elif bottom == "sarong":
        pw, ph, _ = S("sarong")
        sk.append(hang("sarong", -pw / 2, belt_y + 0.4, -hd / 2 - 0.4 + z, 0, -6))
        sk.append(hang("sarong_b", -pw / 2, belt_y + 0.4, hd / 2 + 0.15 + z, 0, 8))
        kx = hw / 2 - 0.2                             # the knot on her left hip
        sk.append(cube("knot", [kx - 0.6, belt_y - 1.0, -hd / 2 - 0.9 + z]))
        sk.append(hang("knot_end", kx - 0.2, belt_y - 1.0, -hd / 2 - 0.75 + z, 10, -4))
        sk.append(hang("knot_end", kx + 0.3, belt_y - 1.0, -hd / 2 - 0.7 + z, -14, -2))

    # extras on the body
    if "pouch" in ex:
        body.append(cube("pouch", [-S("belt")[0] / 2 + 0.1, belt_y - 1.5, -hd / 2 - 0.6 + z]))
    if "bell_collar" in ex:
        body.append(cube("neck_ring", [-1.95, 19.2 + dy, -1.575 + z]))
        body.append(cube("bell", [-0.5, 18.5 + dy, -2.25 + z]))
    if "scarf" in ex:
        body.append(cube("scarf", [-2.125, 18.9 + dy, -1.75 + z]))
        body.append(hang("scarf_tail", 1.2, 19.5 + dy, -2.2 + z, -10, -12))
    r.add("body", parent, [0, W, z], body)
    r.add("belly", "body", [0, 13.25 + dy + mh / 2, md / 2 + z], [cube("belly_skin", [-mw / 2, 13.25 + dy, -md / 2 + z])])
    if sk:
        r.add("skirt", "body", [0, 13.7 + dy, z], sk)


ARM_GAP = 0.25


def arms(r, s, dy, z):
    outfit = s.get("outfit") or {}
    tops, ex = _tops(outfit), outfit.get("extras", [])
    a = S("arm_up")[0]
    x0 = -(max(S("chest")[0], S("hips")[0]) / 2 + ARM_GAP + a)   # clear of her chest and hips, or the arms melt into her body
    ay = 19.5 + dy
    up = [cube("arm_up", [x0, ay - 3.5, -a / 2 + z])]
    if "sweater" in tops:
        up.append(cube("sh_arm", [x0, ay - 3.5, -a / 2 + z], inflate=0.15))
    if "armlets" in ex:
        up.append(cube("armlet", [x0, ay - 1.6, -a / 2 + z], inflate=0.18))
    if "kimono" in tops:
        sw, sh_, sd = S("sleeve_w")
        up.append(cube("sleeve_w", [x0 - 0.6, ay + 0.35 - sh_, -sd / 2 + z]))
    if "pauldron" in ex:
        pw, ph, pd = S("pauldron")
        up.append(cube("pauldron", [x0 - 0.6, ay - 0.4, -pd / 2 + z], pivot=[x0 + a / 2, ay, z], rotation=[0, 0, -14]))
    r.add("arm_r", "body", [x0 + a / 2, ay, z], up, mirror=True)
    fa = [cube("forearm", [x0, ay - 6.5, -a / 2 + z])]
    if s.get("cuffs", True) and not outfit:
        fa.append(cube("cuff", [x0 - 0.125, ay - 6.4, -a / 2 - 0.125 + z]))
    if "sweater" in tops:
        fa.append(cube("sh_fore", [x0, ay - 6.5, -a / 2 + z], inflate=0.15))
    if "arm_wraps" in ex:
        fa.append(cube("wrap_fa", [x0, ay - 6.5, -a / 2 + z], inflate=0.1))
    if "bracers" in ex:
        fa.append(cube("bracer", [x0, ay - 6.3, -a / 2 + z], inflate=0.2))
    r.add("forearm_r", "arm_r", [x0 + a / 2, ay - 3.5, z], fa, mirror=True)
    hand = [cube("palm", [x0, ay - 8, -0.5 + z]), cube("fingers", [x0, ay - 9, -0.5 + z]), cube("thumb", [x0 + a, ay - 8, -0.25 + z])]
    if "arm_wraps" in ex:
        hand.append(cube("wrap_palm", [x0, ay - 8.1, -0.5 + z], inflate=0.08))
    r.add("hand_r", "forearm_r", [x0 + a / 2, ay - 6.5, z], hand, mirror=True)


def dress_legs(r, outfit, cx=-1.25):
    """the parts that ride on the legs (call after the lower body is built)"""
    if (outfit or {}).get("bottom") == "shorts" and "leg_r" in r.b:
        w, h, d = S("short_leg")
        r.put("leg_r", cube("short_leg", [cx - w / 2, 12 - h, -d / 2], inflate=0.1))


# ----------------------------------------------------------------------------- painting
def _ramp(c):
    from girl_lib import shades
    return shades(c)


def darker(rmp, f=0.72):
    return ramp([rgb(int(c[0] * f), int(c[1] * f), int(c[2] * f)) for c in rmp])


def paint_build(pt, pal, build):
    """muscle definition on the bare midriff of the athletic, strong and male builds"""
    if build not in ("athletic", "strong", "male"):
        return
    deep = build in ("strong", "male")

    def abs_(f, x, y, w, h):
        c = (w - 1) / 2
        if abs(x - c) < 0.6 and 1 <= y <= h - 2:                         # the line down the middle
            return pick(pal.SKIN, 0.32, x, y)
        if deep and y in (h // 3, 2 * h // 3) and 1 <= abs(x - c) <= w / 2 - 2:
            return pick(pal.SKIN, 0.38, x, y)
        if abs(abs(x - c) - (w / 2 - 1.5)) < 0.6 and y >= h // 3:     # the obliques
            return pick(pal.SKIN, 0.40, x, y)
        return None
    pt("midriff", ("north",), abs_)


def _webbed(fn, pal):
    """a cloth painter with a spider-web print and scattered flowers over it, when pal.extra has "kimono_web" (the
    jorogumo's kimono); otherwise the painter unchanged"""
    web = pal.extra.get("kimono_web") if pal.extra else None
    if not web:
        return fn
    W, FL = _ramp(web), _ramp(pal.extra.get("kimono_flower", "#e88aa8"))

    def out(f, x, y, w, h):
        base = fn(f, x, y, w, h)
        if base is None or base == CLEAR or f in ("up", "down"):
            return base
        if noise(x // 3, y // 3, 61) > 0.86 and noise(x, y, 67) > 0.25:                          # flowers
            return pick(FL, 0.5 + 0.25 * ((x + y) % 2), x, y)
        cx, cy = (w - 1) * (0.15 if f in ("north", "east") else 0.85), 1.0
        dx, dy = x - cx, y - cy
        r = math.hypot(dx, dy)
        a = math.atan2(dy, dx) * 6 / math.pi                          # twelve spokes round the centre
        if r > 1.5 and abs(a - round(a)) * r * math.pi / 6 < 0.45 or r > 2 and abs(r % 7 - 3.5) > 3.0:
            return pick(W, 0.55, x, y)
        return base
    return out


def paint(pt, pal, outfit, build=None):
    paint_build(pt, pal, build)
    if not outfit:
        return
    pal.extra = {**outfit.get("colors", {}), **(pal.extra or {})}     # outfit colours (kimono, obi, shell...) unless a variant sets them
    import girl_lib as G
    tops, ex, bottom = _tops(outfit), outfit.get("extras", []), outfit.get("bottom")
    C1 = pal.CLOTH
    C2 = _ramp(pal.extra["cloth2"]) if pal.extra.get("cloth2") else darker(pal.CLOTH)
    LEA = _ramp(pal.extra.get("leather", "#7a5232"))
    BAND = _ramp(pal.extra.get("bandage", "#d8cfbc"))
    skin = H.skin_fn(pal, 0.66)

    def cloth(rmp, base=0.55, hem=True, pattern=None, seed=13):
        def fn(f, x, y, w, h):
            if hem and (y == 0 or y == h - 1) and f not in ("up", "down"):
                return pick(pal.TRIM, 0.55 + (0.2 if x % 3 == 0 else 0), x, y)
            t = base + (noise(x, y, seed) - 0.5) * 0.12 - (0.07 if f in ("east", "west", "south") else 0)
            if pattern == "knit" and (x % 2 == 0):
                t -= 0.10
            if pattern == "rib" and x % 2 == 0:
                t -= 0.14
            return pick(rmp, t, x, y)
        return fn

    def bands(f, x, y, w, h):                          # bindings: overlapping diagonal turns
        k = (y + (x // 3)) % 4
        return pick(BAND, 0.62 - (0.22 if k == 0 else 0) + (noise(x, y, 23) - 0.5) * 0.08, x, y)

    # --- the bare torso first; tops paint over it
    pt("chest", "all", skin)
    pt("bust", "all", H.skin_fn(pal, 0.7))
    if "wrap" in tops:
        pt("chest", "all", G.cloth_fn(pal, 0.55, 1))
        pt("bust", "all", G.cloth_fn(pal, 0.6, 0))
    if "bandeau" in tops:
        c = cloth(C1, 0.56)
        pt("chest", ("north", "south", "east", "west"), lambda f, x, y, w, h: c(f, x, y - 2, w, 8) if 2 <= y <= 9 else None)
        pt("bust", "all", cloth(C1, 0.6, hem=False))
    if "halter" in tops:
        c = cloth(C1, 0.56)

        def halter(f, x, y, w, h):
            if y >= 3:
                return c(f, x, y - 3, w, h - 3)
            if f == "north" and abs(x + 0.5 - w / 2) < 2 + y:           # the V rising to the neck
                return pick(C1, 0.5, x, y)
            return None
        pt("chest", ("north", "south", "east", "west"), halter)
        pt("bust", "all", cloth(C1, 0.6, hem=False))
        pt("neckband", "all", lambda f, x, y, w, h: pick(C1, 0.45, x, y))
    if "sports" in tops:
        c = cloth(C1, 0.52)

        def sports(f, x, y, w, h):
            if f == "south" and y < 4:                 # racer back: a narrow strip up the spine
                return pick(C1, 0.45, x, y) if abs(x + 0.5 - w / 2) < 2 else None
            if f in ("east", "west") and y < 2:
                return None
            if y == h - 2:
                return pick(pal.TRIM, 0.6, x, y)
            return c(f, x, y, w, h) if y >= 1 or f == "north" and (x < 4 or x >= w - 4) else None
        pt("chest", ("north", "south", "east", "west", "up"), sports)
        pt("bust", "all", cloth(C1, 0.56, hem=False))
    if "sarashi" in tops:
        pt("chest", ("north", "south", "east", "west"), lambda f, x, y, w, h: bands(f, x, y, w, h) if y >= 2 else None)
        pt("bust", "all", bands)
    if "sweater" in tops:
        knit = cloth(C1, 0.58, hem=False, pattern="knit")
        rib = cloth(C1, 0.5, hem=False, pattern="rib")
        pt("sh_chest", "all", lambda f, x, y, w, h: rib(f, x, y, w, h) if y >= h - 2 else knit(f, x, y, w, h))
        pt("sh_chest", ("down",), lambda f, x, y, w, h: CLEAR)
        pt("knit_bust", "all", knit)
        pt("turtle", "all", rib)
        pt("turtle", ("up",), lambda f, x, y, w, h: pick(C1, 0.4, x, y))
        pt("sh_arm", "all", knit)
        pt("sh_arm", ("up", "down"), lambda f, x, y, w, h: CLEAR)
        pt("sh_fore", "all", lambda f, x, y, w, h: rib(f, x, y, w, h) if y >= h - 3 else knit(f, x, y, w, h))
        pt("sh_fore", ("up",), lambda f, x, y, w, h: CLEAR)
        pt("chest", "all", knit)                       # under the shell, in case anything peeks
    if "vest" in tops:
        lea = cloth(LEA, 0.5, hem=False)

        def vest(f, x, y, w, h):
            if f == "north" and abs(x + 0.5 - w / 2) < w * 0.32:   # open front
                return CLEAR
            if f in ("up", "down"):
                return CLEAR
            if f in ("east", "west") and y < 3 and 1 < x < w - 2:  # arm holes
                return CLEAR
            t = lea(f, x, y, w, h)
            return pick(LEA, 0.75, x, y) if (f == "north" and abs(abs(x + 0.5 - w / 2) - w * 0.32) < 1) else t
        pt("vest_c", "all", vest)
        pt("vest_m", "all", lambda f, x, y, w, h: vest(f, x, y + 4, w, h + 4) if not (f in ("up", "down")) else CLEAR)
    if "mantle" in tops:
        PELT = _ramp(pal.extra.get("pelt", "#6e4a2c"))
        fur = lambda f, x, y, w, h: pick(PELT, 0.5 + (noise(x, y, 29) - 0.5) * 0.3 - (0.1 if x % 3 == 0 else 0), x, y)

        def mantle(f, x, y, w, h):
            if f == "down":
                return CLEAR
            if f in BROAD + ("east", "west") and y >= h - 3 and noise(x, 0, 41) > 0.45 + 0.15 * (h - 1 - y):
                return CLEAR                         # shaggy hem
            if f == "up":
                return pick(pal.LIGHT, 0.55 + (noise(x, y, 43) - 0.5) * 0.2, x, y)
            return fur(f, x, y, w, h)
        pt("mantle", "all", mantle)
        pt("mantle_f", "all", lambda f, x, y, w, h: CLEAR if (y >= h - 3 and noise(x, 0, 47) > 0.5) else
           (pick(pal.LIGHT, 0.6, x, y) if x in (0, w - 1) else fur(f, x, y, w, h)))
    if "capelet" in tops:
        cape = cloth(C2, 0.52)
        pt("cape_t", "all", lambda f, x, y, w, h: CLEAR if f == "down" else cape(f, x, y, w, h))
        pt("cape_b", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else
           (pick(pal.TRIM, 0.55, x, y) if y == h - 1 else cape(f, x, y, w, h)))
        pt("cape_b", ("up",), lambda f, x, y, w, h: cape(f, x, y, w, h) if (x < 4 or x >= w - 4 or y < 3 or y >= h - 3) else CLEAR)
        pt("clasp", "all", lambda f, x, y, w, h: pick(pal.TRIM, 0.85 if f == "north" else 0.5, x, y))

    if "kimono" in tops:
        K = _ramp(pal.extra.get("kimono")) if pal.extra.get("kimono") else C1
        KT = _ramp(pal.extra.get("kimono_trim")) if pal.extra.get("kimono_trim") else pal.TRIM
        kc = _webbed(cloth(K, 0.6, hem=False), pal)

        def kimono(f, x, y, w, h):
            if f in ("up", "down"):
                return CLEAR if f == "down" else kc(f, x, y, w, h)
            if f == "north":
                c = (w - 1) / 2
                half = max(0.0, w * 0.22 * (1 - y / (h * 0.85)))   # the V where the two sides cross
                d_ = abs(x - c)
                if d_ < half - 1:
                    return None                    # skin shows through (the chest is painted skin)
                if d_ < half + 1:
                    return pick(KT, 0.55, x, y)
            return kc(f, x, y, w, h)
        pt("kimono_c", "all", kimono)
        pt("kimono_m", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else kc(f, x, y, w, h))
        pt("kimono_b", "all", lambda f, x, y, w, h: kc(f, x, y, w, h))
        pt("sleeve_w", "all", lambda f, x, y, w, h: CLEAR if f == "down" else (pick(KT, 0.55, x, y) if y >= h - 2 and f != "up" else kc(f, x, y, w, h)))
        OBI = _ramp(pal.extra.get("obi")) if pal.extra.get("obi") else C2
        pt("obi", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else pick(pal.TRIM if h // 2 - 1 <= y <= h // 2 else OBI, 0.55, x, y))
        pt("obi_bow", "all", lambda f, x, y, w, h: pick(OBI, 0.5 + 0.15 * (abs(x - w / 2) < 1.5), x, y))
    if "shells" in tops:
        SH = _ramp(pal.extra.get("shell", "#f0d8c8"))

        def shell(f, x, y, w, h):
            if f == "north" and y == h - 1 and x % 3 == 2:
                return CLEAR                           # scalloped edge
            if f == "north" and y == 0 and (x < 2 or x >= w - 2):
                return CLEAR
            return pick(SH, 0.5 + 0.28 * (x % 3 == 1) - 0.1 * (y > h * 0.7), x, y)
        pt("shell_c", "all", shell)
        pt("bust", "all", cloth(C1, 0.5, hem=False))
    if "corset" in tops:
        cc = cloth(C2, 0.45)

        def corset(f, x, y, w, h):
            if f in ("up", "down"):
                return CLEAR
            if f == "north":
                c = (w - 1) / 2
                if abs(x - c) < 2.5 and 1 <= y < h - 1:
                    return pick(pal.TRIM, 0.7, x, y) if abs(abs(x - c) - (y % 4) * 0.6) < 0.6 else pick(C2, 0.3, x, y)
            return cc(f, x, y, w, h) if not (f in ("east", "west") and x % 4 == 0) else pick(C2, 0.3, x, y)   # boning
        pt("corset", "all", corset)
    if "harness" in tops:
        def strap(f, x, y, w, h):
            c = (w - 1) / 2
            if f in ("north", "south"):
                k = (c - 0.5) / (h * 0.7)
                if y < h * 0.7 and abs(abs(x - c) - (c - 0.5 - y * k)) < 1.1:
                    return pick(LEA, 0.5 + 0.15 * (y % 3 == 0), x, y)
                if y >= h - 3:
                    return pick(LEA, 0.45, x, y)
            if f in ("east", "west") and y >= h - 3:
                return pick(LEA, 0.4, x, y)
            return None
        pt("chest", ("north", "south", "east", "west"), strap)
        pt("ring", "all", lambda f, x, y, w, h: CLEAR if (f == "north" and 1 <= x < w - 1 and 1 <= y < h - 1) else pick(pal.TRIM, 0.7, x, y))

    # --- bottoms
    if bottom == "hakama":
        HK = C2

        def hakama(f, x, y, w, h):
            if f in BROAD and x % 4 == 0:
                return pick(HK, 0.38, x, y)            # deep pleats
            return pick(HK, 0.55 + (noise(x, y, 63) - 0.5) * 0.1 - (0.06 if f not in BROAD else 0), x, y)
        pt("hakama_f", "all", hakama)
        pt("hakama_s", "all", hakama)
    if bottom == "strips":
        for p in ("cloth_s", "cloth_m", "cloth_l"):
            pt(p, "all", G.strip_fn(pal, C2 if outfit.get("strip_c2") else pal.CLOTH))
        pt("cloth_side", "all", G.strip_fn(pal, pal.CLOTH, fray=False))
    if bottom == "shorts":
        c = cloth(C2, 0.5)
        pt("sh_hips", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else c(f, x, y, w, h))
        pt("short_leg", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else
           (pick(pal.TRIM, 0.55, x, y) if y == h - 1 else pick(C2, 0.5 + (noise(x, y, 51) - 0.5) * 0.12 - (0.06 if f != "north" else 0), x, y)))
    if bottom == "loincloth":
        def flap(f, x, y, w, h):
            if f not in BROAD:
                return pick(C2, 0.4, x, y)
            if y >= h - 2 and (x % 3 == 1):
                return CLEAR                         # fringed end
            if x in (0, w - 1):
                return pick(pal.TRIM, 0.5, x, y)
            return pick(C2, 0.52 + (noise(x, y, 53) - 0.5) * 0.14 - (0.06 if y % 6 == 0 else 0), x, y)
        pt("flap_f", "all", flap)
        pt("flap_b", "all", flap)
    if bottom == "wrap_skirt":
        c = _webbed(cloth(C2, 0.54), pal)

        def panel(f, x, y, w, h):
            if y == h - 1 and f not in ("up", "down"):
                return pick(pal.TRIM, 0.6, x, y)
            t = c(f, x, y, w, h)
            return pick(C2, 0.4, x, y) if (f in BROAD and x % 5 == 0) else t     # pleats
        pt("panel_f", "all", panel)
        pt("panel_s", "all", panel)
    if bottom == "sarong":
        c = cloth(C2, 0.55, hem=False)

        def sarong(f, x, y, w, h):
            cut = h - 1 - (x / max(1, w - 1)) * h * 0.45           # diagonal hem, long on her right
            if f in BROAD and y > cut:
                return CLEAR
            if f in BROAD and y > cut - 1.5:
                return pick(pal.TRIM, 0.6, x, y)
            t = c(f, x, y, w, h)
            return pick(pal.TRIM, 0.7, x, y) if (f in BROAD and (x + y) % 9 == 0 and noise(x, y, 57) > 0.5) else t
        pt("sarong", "all", sarong)
        pt("sarong_b", "all", lambda f, x, y, w, h: CLEAR if (f in BROAD and y > h - 1 - (1 - x / max(1, w - 1)) * h * 0.4) else c(f, x, y, w, h))
        pt("knot", "all", lambda f, x, y, w, h: pick(C2, 0.45 + 0.15 * ((x + y) % 2), x, y))
        pt("knot_end", "all", lambda f, x, y, w, h: CLEAR if (y >= h - 2 and x % 2) else pick(C2, 0.52, x, y))
    if bottom in ("shorts", "loincloth", "wrap_skirt", "sarong", "hakama") or "belt" in ex:
        rope = outfit.get("rope_belt")
        pt("belt", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else
           (pick(BAND, 0.5 + 0.2 * ((x + y) % 3 == 0), x, y) if rope else pick(LEA, 0.45 + (0.12 if y == 0 else 0), x, y)))
        pt("buckle", "all", lambda f, x, y, w, h: (pick(BAND, 0.45, x, y) if rope else
           pick(pal.TRIM, 0.85 if (f == "north" and (x in (0, w - 1) or y in (0, h - 1))) else 0.45, x, y)))

    # --- extras
    pt("pouch", "all", lambda f, x, y, w, h: pick(LEA, 0.7 if (f == "north" and y < 2) else 0.5 + (noise(x, y, 59) - 0.5) * 0.12, x, y))
    pt("neck_ring", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else pick(pal.extra.get("ribbon") and _ramp(pal.extra["ribbon"]) or C1, 0.5, x, y))
    pt("bell", "all", lambda f, x, y, w, h: pick(_ramp("#e8c040"), 0.85 if (x + y) % 3 == 0 else 0.55, x, y) if not (f == "down" and 1 <= x <= 2) else rgb(40, 30, 20))
    pt("wrap_fa", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else bands(f, x, y, w, h))
    pt("wrap_palm", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else bands(f, x, y, w, h))
    pt("armlet", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else pick(pal.TRIM, 0.8 if y == 0 else 0.5, x, y))
    PEARL = _ramp("#f2eee8")
    pt("pearl", "all", lambda f, x, y, w, h: pick(PEARL, 0.9 if (x == 0 and y == 0) else 0.62, x, y))
    U2 = _ramp(pal.extra.get("usekh2", "#2a8aa8"))

    def usekh(f, x, y, w, h):
        if f == "up":
            r_ = int(max(abs(x - (w - 1) / 2) / (w / 2), abs(y - (h - 1) / 2) / (h / 2)) * 6)
            return pick(pal.TRIM if r_ % 2 else U2, 0.6, x, y)
        return pick(pal.TRIM, 0.55, x, y)
    pt("usekh", "all", usekh)
    pt("usekh_f", "all", lambda f, x, y, w, h: CLEAR if (f == "north" and y >= h - 2 and (x < 2 or x >= w - 2)) else
       pick((pal.TRIM, U2, pal.TRIM, C1)[(y // 2) % 4], 0.6 + 0.2 * (x % 4 == 0 and (y // 2) % 4 == 0), x, y))
    pt("pauldron", "all", lambda f, x, y, w, h: pick(pal.TRIM, 0.8, x, y) if (f == "up" and (x + y) % 5 == 0) else
       pick(LEA, 0.5 + (0.15 if f == "up" else 0) + (noise(x, y, 65) - 0.5) * 0.1, x, y))
    pt("bracer", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else
       (pick(pal.TRIM, 0.6, x, y) if (f == "north" and abs(x - (w - 1) / 2) < 0.6 and y % 2) else pick(LEA, 0.48 + 0.1 * (y in (0, h - 1)), x, y)))
    pt("satchel", "all", lambda f, x, y, w, h: pick(LEA, 0.66 if (f == "west" and y < 3) or f == "up" else 0.48 + (noise(x, y, 67) - 0.5) * 0.1, x, y))
    pt("strap", "all", lambda f, x, y, w, h: pick(LEA, 0.45 + 0.1 * (x == 0), x, y))
    if "scarf" in ex:
        SC = _ramp(pal.extra.get("scarf", "#b83a3a"))

        def scarf(f, x, y, w, h):
            stripe = (x // 3) % 2 == 0 if f in ("up", "down") else (y // 2) % 2 == 0
            return pick(SC, 0.58 - (0.12 if stripe else 0) + (noise(x, y, 61) - 0.5) * 0.08, x, y)
        pt("scarf", "all", scarf)
        pt("scarf_tail", "all", lambda f, x, y, w, h: CLEAR if (f in BROAD and y >= h - 2 and x % 2) else scarf(f, x, y, w, h))
