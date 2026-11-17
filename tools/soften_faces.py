#!/usr/bin/env python3
"""Soft faces for every model: rounds the head and softens the face of each geo + texture already in the assets.

    python tools/soften_faces.py              # process every model that still has a box head / unprocessed texture
    python tools/soften_faces.py crow_harpy   # just these models
    python tools/soften_faces.py --dry        # report only

RE-RUN IT after any generator writes a model (like gen_fidgets.py): generators still build the old box head, and this
turns it into the soft one. It is idempotent: a converted head has no box left to convert, and a texture is only
reworked while its md5 is not the one recorded in tools/soft_faces_manifest.json.

Geometry (any head: the cube of the bone that holds the eyelids, plus its inflated hair cap twin if it has one):
  * the head box is cut into a core and four steps that narrow into a round chin (8 -> 7.5 -> 6.5 -> 5 -> 3.5 wide on
    the usual 8-wide head); the steps never get narrower than the eyelid and mouth cubes behind the face need
  * the hair cap becomes a dome: its vertical corners are cut back and the top steps in twice
  Every new cube's faces point into the old cube's texture (per-face UV sub-rectangles), so the texture layout, the
  paints and the blush overlays stay valid.

Texture (the 32 x 32 face every girl and harpy shares; frogs and hatchlings with their own faces only get the shading):
  * the face skin is shaded like a curved surface (lighter in the middle, darker under the fringe and along the jaw)
  * the standard eye (a 4 x 6 ellipse) is redrawn rounder (4.9 x 5.5), by resampling her own eye, so custom irises,
    pupils and highlights survive
  * the standard blush becomes a larger, softer oval; the standard straight smile becomes a small curved one; the
    standard brows get a texel shorter at each end
  * the closed-eye sheet gets the same skin, so a blink does not cut the blush
Skin is recognised from the closed-eye sheet (painted only with her skin and the lash line), so species skins work.
"""
import glob
import hashlib
import json
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402

ASSETS = os.path.join(H.REPO, "src", "main", "resources", "assets", "harmontide")
GEO = os.path.join(ASSETS, "geo", "entity")
ENT = os.path.join(ASSETS, "textures", "entity")
MANIFEST = os.path.join(os.path.dirname(os.path.abspath(__file__)), "soft_faces_manifest.json")

# lower face of an 8-tall head: (bottom, top) in units from the head's bottom, half width as a fraction of the head's
JAW = [(1.5, 8.0, 1.0), (1.0, 1.5, 0.9375), (0.5, 1.0, 0.8125), (0.25, 0.5, 0.625), (0.0, 0.25, 0.4375)]
# hair dome in texels of the cap's face: (inset x, inset z, texels below the top, texels above it ends)
DOME = [(0, 2, 3, None), (2, 0, 3, None), (2, 2, 1, 3), (5, 5, 0, 1)]
FACES = ("north", "south", "east", "west", "up", "down")


# ----------------------------------------------------------------------------- geometry
def box(c):
    inf = c.get("inflate", 0) or 0
    lo = [c["origin"][i] - inf for i in range(3)]
    hi = [c["origin"][i] + c["size"][i] + inf for i in range(3)]
    return lo, hi


def sub_uv(c, lo, hi, a, b):
    """per-face UVs of the sub-box a..b (model units) of cube c, pointing into c's own texture"""
    f = [[(a[i] - lo[i]) / (hi[i] - lo[i]), (b[i] - lo[i]) / (hi[i] - lo[i])] for i in range(3)]
    fx, fy, fz = f
    rng = {
        "north": ((fx[0], fx[1]), (1 - fy[1], 1 - fy[0])),
        "south": ((1 - fx[1], 1 - fx[0]), (1 - fy[1], 1 - fy[0])),
        "east": ((1 - fz[1], 1 - fz[0]), (1 - fy[1], 1 - fy[0])),
        "west": ((fz[0], fz[1]), (1 - fy[1], 1 - fy[0])),
        "up": ((fx[0], fx[1]), (1 - fz[1], 1 - fz[0])),
        "down": ((fx[0], fx[1]), (fz[0], fz[1])),
    }
    out = {}
    for face, ((u0, u1), (v0, v1)) in rng.items():
        src = c["uv"][face]
        (u, v), (su, sv) = src["uv"], src["uv_size"]
        if abs(su) <= 1 and abs(sv) <= 1:                  # a skipped face (1 x 1 clear texel): keep as is
            out[face] = {"uv": [u, v], "uv_size": [su, sv]}
            continue
        out[face] = {"uv": [r4(u + u0 * su), r4(v + v0 * sv)], "uv_size": [r4((u1 - u0) * su), r4((v1 - v0) * sv)]}
    return out


def r4(x):
    x = round(x, 4)
    return int(x) if x == int(x) else x


def piece(c, lo, hi, a, b):
    p = {"origin": [r4(v) for v in a], "size": [r4(b[i] - a[i]) for i in range(3)], "uv": sub_uv(c, lo, hi, a, b)}
    if "pivot" in c:
        p["pivot"], p["rotation"] = c["pivot"], c["rotation"]
    return p


def texel(c, lo, hi):
    """model units per texel along x, y, z of a cube's texture"""
    return ((hi[0] - lo[0]) / abs(c["uv"]["north"]["uv_size"][0]), (hi[1] - lo[1]) / abs(c["uv"]["north"]["uv_size"][1]),
            (hi[2] - lo[2]) / abs(c["uv"]["east"]["uv_size"][0]))


def jaw_pieces(c, need):
    """the head cube cut into a core and narrowing steps; need(y0, y1) = the half width the face parts require there"""
    lo, hi = box(c)
    tx, ty, _ = texel(c, lo, hi)
    cx, w, h = (lo[0] + hi[0]) / 2, hi[0] - lo[0], hi[1] - lo[1]
    out, prof = [], []
    for y0u, y1u, frac in JAW:
        y0 = lo[1] + round(y0u / 8 * h / ty) * ty
        y1 = lo[1] + round(y1u / 8 * h / ty) * ty if y1u < 8 else hi[1]
        if y1 - y0 < ty / 2:
            continue
        cut = math.floor((1 - frac) * w / 2 / tx + 1e-6)              # texels taken off each side
        while cut > 0 and w / 2 - cut * tx < need(y0, y1) - 1e-6:
            cut -= 1
        a, b = [lo[0] + cut * tx, y0, lo[2]], [hi[0] - cut * tx, y1, hi[2]]
        out.append(piece(c, lo, hi, a, b))
        prof.append((y0, y1, cut))
    return out, prof


def dome_pieces(c):
    lo, hi = box(c)
    tx, ty, tz = texel(c, lo, hi)
    out = []
    for ix, iz, below, above in DOME:
        a = [lo[0] + ix * tx, lo[1] if above is None else hi[1] - above * ty, lo[2] + iz * tz]
        b = [hi[0] - ix * tx, hi[1] - below * ty, hi[2] - iz * tz]
        out.append(piece(c, lo, hi, a, b))
    return out


def find_heads(g):
    """[(bone, head cube, cap cube or None, [eyelid/mouth cubes])] for every bone holding an eyelids bone"""
    bones = {b["name"]: b for b in g["bones"]}
    kids = {}
    for b in g["bones"]:
        kids.setdefault(b.get("parent"), []).append(b)
    heads = []
    for name, b in bones.items():
        lids = [k for k in kids.get(name, []) if k["name"].startswith("eyelid")]
        if not lids:
            continue
        feat = [cu for k in kids.get(name, []) if k["name"].startswith(("eyelid", "mouth")) for cu in k.get("cubes", [])]
        lc = lids[0].get("cubes", [])
        if not lc:
            continue
        lx0 = min(cu["origin"][0] for cu in lc)
        lx1 = max(cu["origin"][0] + cu["size"][0] for cu in lc)
        lz = min(cu["origin"][2] for cu in lc)
        best = None
        for cu in b.get("cubes", []):
            if cu.get("inflate") or "rotation" in cu and any(cu["rotation"]):
                continue
            lo, hi = box(cu)
            if lo[0] - 0.01 <= lx0 and lx1 <= hi[0] + 0.01 and abs(lz - lo[2]) < 0.8 and cu["size"][1] >= 3:
                # the face plane nearest the eyelids, then the biggest (a rounded head's dome pieces sit further out)
                key = (round(abs(lz - lo[2]), 3), -cu["size"][0] * cu["size"][1] * cu["size"][2])
                if best is None or key < best[0]:
                    best = (key, cu)
        if best is None:
            continue
        head = best[1]
        hlo, hhi = box(head)
        if any(cu is not head and abs(cu["origin"][1] + cu["size"][1] - hlo[1]) < 1e-6 and abs(cu["origin"][2] - hlo[2]) < 0.6
               and hlo[0] - 1e-6 <= cu["origin"][0] and cu["origin"][0] + cu["size"][0] <= hhi[0] + 1e-6 and not cu.get("inflate")
               and cu["size"][1] < head["size"][1]
               for cu in b.get("cubes", [])):
            continue                     # already rounded (by this script, or by hand like sparrow_harpy_soft): a step sits flush under it
        cap = next((cu for cu in b.get("cubes", []) if cu.get("inflate") and cu["origin"] == head["origin"] and cu["size"] == head["size"]), None)
        heads.append((b, head, cap, feat, [cu for k in lids for cu in k.get("cubes", [])]))
    return heads


def soften_geo(doc):
    """converts every box head in a geo doc in place; returns [(head cube, jaw profile, lid cube)] for the texture pass"""
    g = doc["minecraft:geometry"][0]
    done = []
    for b, head, cap, feat, _ in find_heads(g):
        lo, hi = box(head)
        cx = (lo[0] + hi[0]) / 2

        def need(y0, y1):
            n = 0.0
            for cu in feat:
                flo, fhi = box(cu)
                if fhi[1] > y0 + 1e-6 and flo[1] < y1 - 1e-6 and fhi[0] > lo[0] and flo[0] < hi[0]:
                    n = max(n, abs(flo[0] - cx), abs(fhi[0] - cx))
            return n
        jaw, prof = jaw_pieces(head, need)
        cubes = b["cubes"]
        i = cubes.index(head)
        cubes[i:i + 1] = jaw                                   # the core stays where the head cube was (cubes[0])
        if cap is not None:
            j = cubes.index(cap)
            cubes[j:j + 1] = dome_pieces(cap)
        done.append((head, prof, feat))
    return done


# ----------------------------------------------------------------------------- texture
BAYER = ((0.125, 0.625), (0.875, 0.375))


def shade(col, f, x, y):
    """col times f, with f on 2.5 % steps dithered by the 2 x 2 Bayer pattern"""
    q = f / 0.025
    lo = math.floor(q)
    q = lo + (1 if q - lo > BAYER[y & 1][x & 1] else 0)
    k = q * 0.025
    return (min(255, int(col[0] * k + 0.5)), min(255, int(col[1] * k + 0.5)), min(255, int(col[2] * k + 0.5)), col[3])


def curve(x, y, W, Hh):
    """light falloff over a face W x Hh texels (the usual one is 32 x 32)"""
    sx, sy = 32 / W, 32 / Hh
    X, Y = (x + 0.5) * sx, (y + 0.5) * sy
    u, v = (X - 16) / 16, (Y - 17) / 16
    f = 1.03 - 0.09 * (u * u + v * v)
    if Y < 4:
        f -= 0.05 * (4 - Y) / 4
    if Y > 26:
        f -= 0.012 * (Y - 26)
    return f


def opaque(c):
    return c[3] >= 128


def eye_e(x, y, cx, a, b):
    dx, dy = (x + 0.5 - cx) / a, (y + 0.5 - 18) / b
    return dx * dx + dy * dy, dy


def blush_at(x, y, blush):
    for cx in (7.5, 24.5):
        e = ((x + 0.5 - cx) / 4.4) ** 2 + ((y + 0.5 - 25.5) / 2.0) ** 2
        if e <= 0.40:
            return blush[0]
        if e <= 0.80 or (e <= 1.15 and (x + y) % 2 == 0):
            return blush[1]
    return None


def soften_tex(img, glow, rect, lids, prof, head_lo_y, ty, standard):
    """rework one texture (and its glow mask) in place. rect = head north (u, v, w, h); lids = [(u, v, w, h, ox, oy)]"""
    u0, v0, W, Hh = rect
    F = [[img[v0 + y][u0 + x] for x in range(W)] for y in range(Hh)]
    G = [[glow[v0 + y][u0 + x] for x in range(W)] for y in range(Hh)] if glow else None

    # her skin: the colours of the closed-eye sheets, minus the lash line (their darkest colour)
    cols = {}
    for lu, lv, lw, lh, _, _ in lids:
        for y in range(lh):
            for x in range(lw):
                c = img[lv + y][lu + x]
                if opaque(c):
                    cols[c] = cols.get(c, 0) + 1
    if len(cols) < 2:
        return False
    dark = min(cols, key=lambda c: c[0] + c[1] + c[2])
    skin = {c for c in cols if c != dark and cols[c] >= 3}
    if not skin:
        return False
    counts = {}
    for row in F:
        for c in row:
            if c in skin:
                counts[c] = counts.get(c, 0) + 1
    if not counts:
        return False
    base = max(counts, key=counts.get)

    def bare(x, y):
        return shade(base, curve(x, y, W, Hh), x, y)

    out = [row[:] for row in F]
    gout = [row[:] for row in G] if G else None
    skinm = [[F[y][x] in skin for x in range(W)] for y in range(Hh)]      # texels that are (now) bare skin
    done = [[False] * W for _ in range(Hh)]

    def put(x, y, c, g="clear", is_skin=False):
        out[y][x] = c
        done[y][x] = True
        skinm[y][x] = is_skin
        if gout is not None:
            gout[y][x] = (0, 0, 0, 0) if g == "clear" else g

    def to_skin(x, y):
        put(x, y, bare(x, y), is_skin=True)

    blush = None
    if standard:
        # ---- eyes: the 4 x 6 ellipse redrawn as 4.9 x 5.5 from her own eye
        def is_std(cx, sg):
            o = F[12][cx]
            pts_in = [F[12][cx], F[18][cx - 4 if sg < 0 else cx + 3]]
            pts_out = [F[11][cx], F[18][cx - 5 if sg < 0 else cx + 4]]
            return all(p == o for p in pts_in) and o not in skin and all(p in skin for p in pts_out)
        if is_std(8, -1) and is_std(24, 1):
            for cx in (8, 24):
                for y in range(9, 28):
                    for x in range(cx - 7, cx + 7):
                        e_old, dyo = eye_e(x, y, cx, 4.0, 6.0)
                        e_new, dyn = eye_e(x, y, cx, 4.9, 5.5)
                        if e_new <= 1.0 or (e_new < 1.3 and dyn > 0.45):
                            sx = int(math.floor(cx + (x + 0.5 - cx) * 4.0 / 4.9))
                            sy = int(math.floor(18 + (y + 0.5 - 18) * 6.0 / 5.5))
                            src = F[sy][sx]
                            if e_new > 1.0 and src in skin:
                                to_skin(x, y)
                            else:
                                put(x, y, src, G[sy][sx] if G else None)
                        elif e_old <= 1.0 or (e_old < 1.3 and dyo > 0.45 and F[y][x] not in skin):
                            to_skin(x, y)
        # ---- mouth: the straight smile becomes a small curve
        L = F[28][14]
        if (L not in skin and all(F[28][x] == L for x in range(14, 18)) and F[27][13] == L and F[27][18] == L
                and F[29][15] == F[29][16] and F[29][15] != L):
            for x, y in ((13, 27), (18, 27), (14, 28), (17, 28)):
                to_skin(x, y)
            for x, y in ((14, 27), (17, 27)):
                put(x, y, L)
        # ---- brows: a texel shorter at both ends
        Bc = F[10][8]
        if Bc not in skin and all(F[10][x] == Bc for x in range(4, 12)) and all(F[10][x] == Bc for x in range(20, 28))                 and F[10][3] in skin and F[10][28] in skin:
            for x in (4, 11, 20, 27):
                to_skin(x, 10)
        # ---- blush: the old 3.6 x 1.6 ovals give way to larger, softer ones
        B, BL = F[26][7], F[26][4]
        if B not in skin and BL not in skin and B != BL and F[26][25] == B and F[26][28] == BL:
            blush = (B, BL)
            for cx in (7, 25):
                for y in range(23, 30):
                    for x in range(max(0, cx - 5), min(W, cx + 6)):
                        e = ((x + 0.5 - cx) / 3.6) ** 2 + ((y + 0.5 - 26.5) / 1.6) ** 2
                        if e <= 1.0 and F[y][x] in blush and not done[y][x]:
                            put(x, y, bare(x, y), G[y][x] if G else None, is_skin=True)

    # ---- shading of the remaining skin texels; then the blush over the skin
    for y in range(Hh):
        for x in range(W):
            if not skinm[y][x]:
                continue
            if not done[y][x]:
                out[y][x] = shade(F[y][x], curve(x, y, W, Hh), x, y)
            if blush:
                c = blush_at(x, y, blush)
                if c:
                    out[y][x] = c

    # ---- the jaw turns away at its new silhouette
    top = head_lo_y + Hh * ty
    for y0, y1, cut in prof:
        if cut <= 0:
            continue
        r0, r1 = int(round((top - y1) / ty)), int(round((top - y0) / ty))
        for y in range(max(0, r0), min(Hh, r1)):
            for x in (cut, W - 1 - cut):
                if skinm[y][x]:
                    out[y][x] = shade(out[y][x], 0.93, x, y)

    for y in range(Hh):
        img[v0 + y][u0:u0 + W] = out[y]
        if glow:
            glow[v0 + y][u0:u0 + W] = gout[y]

    # ---- closed-eye sheets: the same skin (and blush) as the face behind them
    for lu, lv, lw, lh, ox, oy in lids:
        for y in range(lh):
            for x in range(lw):
                c = img[lv + y][lu + x]
                fx, fy = x + ox, y + oy
                if c in skin and 0 <= fx < W and 0 <= fy < Hh:
                    img[lv + y][lu + x] = (blush and blush_at(fx, fy, blush)) or shade(c, curve(fx, fy, W, Hh), fx, fy)
    return True


# ----------------------------------------------------------------------------- files
def load_png(path):
    from PIL import Image
    im = Image.open(path).convert("RGBA")
    w, h = im.size
    px = list(im.getdata())
    return [px[y * w:(y + 1) * w] for y in range(h)]


def md5(path):
    with open(path, "rb") as fh:
        return hashlib.md5(fh.read()).hexdigest()


def textures_by_model(names):
    """texture files of each model: <name>.png and <name>_<suffix>.png (longest model name wins), not overlays"""
    out = {n: [] for n in names}
    ordered = sorted(names, key=len, reverse=True)
    for p in glob.glob(os.path.join(ENT, "*.png")):
        base = os.path.basename(p)[:-4]
        if base.endswith(("_blush", "_glowmask")):
            continue
        for n in ordered:
            if base == n or base.startswith(n + "_"):
                out[n].append(p)
                break
    return out


def head_info(g):
    """texture facts of each head of a converted or unconverted geo: head north rect, lid rects with face offsets"""
    infos = []
    for b, head, cap, feat, lidc in find_heads(g):
        infos.append(_info(head, lidc))
    return infos


def _info(head, feat):
    lo, hi = box(head)
    n = head["uv"]["north"]
    rect = (int(n["uv"][0]), int(n["uv"][1]), int(abs(n["uv_size"][0])), int(abs(n["uv_size"][1])))
    tx = (hi[0] - lo[0]) / rect[2]
    ty = (hi[1] - lo[1]) / rect[3]
    lids = []
    for cu in feat:
        if cu["size"][2] > 0.5:
            continue
        ln = cu["uv"]["north"]
        lw, lh = int(abs(ln["uv_size"][0])), int(abs(ln["uv_size"][1]))
        ox = int(round((cu["origin"][0] - lo[0]) / tx))
        oy = int(round((hi[1] - (cu["origin"][1] + cu["size"][1])) / ty))
        lids.append((int(ln["uv"][0]), int(ln["uv"][1]), lw, lh, ox, oy, cu))
    return dict(rect=rect, lo_y=lo[1], ty=ty, lids=lids)


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    dry = "--dry" in sys.argv
    manifest = json.load(open(MANIFEST)) if os.path.exists(MANIFEST) else {"heads": {}, "textures": {}}
    geos = sorted(glob.glob(os.path.join(GEO, "*.geo.json")))
    names = [os.path.basename(p)[:-len(".geo.json")] for p in geos]
    if args:
        names = [n for n in names if n in args]
    texmap = textures_by_model([os.path.basename(p)[:-len(".geo.json")] for p in geos])
    n_geo = n_tex = 0
    for name in names:
        path = os.path.join(GEO, name + ".geo.json")
        raw = open(path, "rb").read()
        doc = json.loads(raw)
        g = doc["minecraft:geometry"][0]
        heads = find_heads(g) if manifest.setdefault("geo", {}).get(name) != hashlib.md5(raw).hexdigest() else []
        if heads:
            infos = [_info(h, lidc) for b, h, cap, feat, lidc in heads]
            origs = [dict(head=h["uv"], cap=cap["uv"] if cap else None) for b, h, cap, feat, lidc in heads]
            done = soften_geo(doc)
            if not done:
                continue
            profs = [prof for _, prof, _ in done]
            manifest["heads"][name] = [dict(rect=i["rect"], lo_y=i["lo_y"], ty=i["ty"], prof=p, orig=o,
                                            lids=[l[:6] for l in i["lids"]]) for i, p, o in zip(infos, profs, origs)]
            if not dry:
                nl = "\r\n" if b"\r\n" in raw else "\n"
                text = json.dumps(doc, indent=2) + "\n"
                with open(path, "w", encoding="utf-8", newline="") as fh:
                    fh.write(text.replace("\n", nl))
                manifest["geo"][name] = md5(path)
            n_geo += 1
        heads_info = manifest["heads"].get(name)
        if not heads_info:
            continue
        size = g["description"]["texture_width"]
        for tp in texmap.get(name, []):
            key = os.path.relpath(tp, H.REPO).replace("\\", "/")
            if manifest["textures"].get(key) == md5(tp):
                continue
            img = load_png(tp)
            if len(img[0]) != size:
                continue
            gp = tp[:-4] + "_glowmask.png"
            glow = load_png(gp) if os.path.exists(gp) else None
            changed = False
            for hi_ in heads_info:
                u, v, w, h = hi_["rect"]
                standard = (w, h) == (32, 32)
                changed |= soften_tex(img, glow, (u, v, w, h), [tuple(l) for l in hi_["lids"]], hi_["prof"], hi_["lo_y"], hi_["ty"], standard)
            if not dry:
                if changed:
                    H.write_png(tp, img)
                    if glow:
                        H.write_png(gp, glow)
                manifest["textures"][key] = md5(tp)
            n_tex += changed
    if not dry:
        with open(MANIFEST, "w", encoding="utf-8", newline="\n") as fh:
            json.dump(manifest, fh, indent=1, sort_keys=True)
            fh.write("\n")
    print(f"soften_faces: {n_geo} geo files converted, {n_tex} textures reworked")


if __name__ == "__main__":
    main()
