#!/usr/bin/env python3
"""Beauty pass: makes every finished model prettier and more detailed, in place (run after soften_faces.py).

    python tools/beautify_model.py                        # every model with a face (skips ones already done)
    python tools/beautify_model.py duck_harpy holstaur_minotaur
    python tools/beautify_model.py --all --jobs 6         # in parallel worker processes
    python tools/beautify_model.py duck_harpy --compare out.png   # before (from the backup) / after sheet
    python tools/beautify_model.py --dry                  # say what would change, write nothing

Rewrites geo/entity/<name>.geo.json, animations/entity/<name>.animation.json and every colour texture of the model (and its
glow masks). The files as they were are copied to build/beauty_backup/<name>/ first. tools/beauty_manifest.json keeps the
md5 of every geo written (so a second run skips it) and the cubes the body pass split, which gen_paints.py paints as the
old cubes. Regenerating a model with its generator undoes the pass: run this again afterwards.

Passes:
  0. curves   - (CURVY only) wide hips, a fuller stomach and a bust as wide as the hips, thighs and arms moved out to suit
  1. colour   - shading baked from the shape: soft shadows where parts meet (ray-sampled ambient occlusion), every limb and
                body face shaded like a rounded surface, shadows tinted purple and lights warm instead of plain darker/lighter,
                a darker line where clothing meets skin
  2. eyes     - the iris shades from dark at the top to a light crescent at the bottom, a thick top lash with a wing at
                the outer corner (both eyes the same), a faint lower lash; the pupil and highlights move to pieces of their own
  3. hair     - a broken shine band (the "angel ring") round the head, the fringe locks end in points, and the fringe hangs
                from its own bone (fringe) so it can sway
  4. body     - torso, hips, arms, legs and feet lose their square vertical edges (two crossing boxes), shoulders and the
                chest top are rounded, the bust is rounded at the front; every new piece samples the old cube's texture
  5. alive    - the pupils are small pieces in front of the eyes (bones pupil_r / pupil_l) that the renderers slide
                towards the player (client/PupilLook.java); the fringe sways in idle, walk and fly
"""
import colorsys
import copy
import glob
import hashlib
import json
import math
import os
import shutil
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import soften_faces as SF  # noqa: E402

ASSETS = SF.ASSETS
GEO, ENT = SF.GEO, SF.ENT
ANIM = os.path.join(ASSETS, "animations", "entity")
TOOLS = os.path.dirname(os.path.abspath(__file__))
MANIFEST = os.path.join(TOOLS, "beauty_manifest.json")
BACKUP = os.path.join(H.REPO, "build", "beauty_backup")
PARTS = os.path.join(H.REPO, "build", "beauty_parts")
CLEAR = (0, 0, 0, 0)
SHADOW_HUE = (78, 52, 132)          # what shadows lean towards
LIGHT_HUE = (255, 232, 196)         # what lights lean towards
# the curvy ones: wide hips, a fuller stomach, a bust to match the hips (sizes are the holstaur's, times the factor)
CURVY = {"holstaur_minotaur": 1.0, "cattle_minotaur": 0.95}


# ----------------------------------------------------------------------------- small helpers
def lum(c):
    return (0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]) / 255


def mix(a, b, k):
    k = max(0.0, min(1.0, k))
    out = tuple(int(a[i] + (b[i] - a[i]) * k + 0.5) for i in range(3))
    return out + tuple(a[3:4])


def rgba(c):
    return tuple(c[:3]) + (255,)


def dist(a, b):
    return abs(a[0] - b[0]) + abs(a[1] - b[1]) + abs(a[2] - b[2])


def boost(c, v_k=1.9, v_add=0.05, s_k=1.6, s_cap=0.85):
    h, s, v = colorsys.rgb_to_hsv(*(x / 255 for x in c[:3]))
    s2 = max(s, min(s_cap, s * s_k))
    v2 = min(1.0, v * v_k + v_add)
    return tuple(int(x * 255 + 0.5) for x in colorsys.hsv_to_rgb(h, s2, v2))


def darken(c, k):
    return tuple(int(x * k) for x in c[:3])


def transpose(R):
    return tuple(tuple(R[j][i] for j in range(3)) for i in range(3))


def r4(x):
    return SF.r4(x)


def rect_of(face):
    (u, v), (su, sv) = face["uv"], face["uv_size"]
    return u, v, su, sv


def is_skip(face):
    return abs(face["uv_size"][0]) <= 1 and abs(face["uv_size"][1]) <= 1


def texels_of(face):
    """([(i, j, s, t, px, py)], w, h): texel (i, j) of the face, its centre (s, t) in 0..1 across it, its texture pixel"""
    u, v, su, sv = rect_of(face)
    nw, nh = int(round(abs(su))), int(round(abs(sv)))
    out = []
    for j in range(nh):
        for i in range(nw):
            s, t = (i + 0.5) / nw, (j + 0.5) / nh
            out.append((i, j, s, t, int(math.floor(u + s * su)), int(math.floor(v + t * sv))))
    return out, nw, nh


QUAD = {   # corners TL, TR, BL of each face, as harpy_lib.render maps the UV rect (u right, v down)
    "north": lambda x0, y0, z0, x1, y1, z1: ((x0, y1, z0), (x1, y1, z0), (x0, y0, z0)),
    "south": lambda x0, y0, z0, x1, y1, z1: ((x1, y1, z1), (x0, y1, z1), (x1, y0, z1)),
    "east": lambda x0, y0, z0, x1, y1, z1: ((x0, y1, z1), (x0, y1, z0), (x0, y0, z1)),
    "west": lambda x0, y0, z0, x1, y1, z1: ((x1, y1, z0), (x1, y1, z1), (x1, y0, z0)),
    "up": lambda x0, y0, z0, x1, y1, z1: ((x0, y1, z1), (x1, y1, z1), (x0, y1, z0)),
    "down": lambda x0, y0, z0, x1, y1, z1: ((x0, y0, z0), (x1, y0, z0), (x0, y0, z1)),
}
NORMAL = {"north": (0, 0, -1), "south": (0, 0, 1), "east": (-1, 0, 0), "west": (1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}


def cube_R(c):
    return H.rot_matrix(*c["rotation"]) if c.get("rotation") and any(c["rotation"]) else None


def face_points(c, face):
    """the rest-pose world point of every texel centre of a face, the face normal, its edge vectors"""
    lo, hi = SF.box(c)
    tl, tr, bl = QUAD[face](*lo, *hi)
    R = cube_R(c)
    n = NORMAL[face]
    if R:
        tl, tr, bl = (H.rot_about(p, c["pivot"], R) for p in (tl, tr, bl))
        n = tuple(sum(R[i][j] * n[j] for j in range(3)) for i in range(3))
    tx, nw, nh = texels_of(c["uv"][face])
    pts = [(i, j, s, t, px, py, tuple(tl[k] + s * (tr[k] - tl[k]) + t * (bl[k] - tl[k]) for k in range(3)))
           for i, j, s, t, px, py in tx]
    du = tuple((tr[k] - tl[k]) for k in range(3))
    dv = tuple((bl[k] - tl[k]) for k in range(3))
    return pts, n, du, dv, nw, nh


def retry(fn, *args):
    """Windows sometimes holds a just-read file for a moment (scanners, the IDE): try again before giving up"""
    import time
    for i in range(8):
        try:
            return fn(*args)
        except OSError:
            if i == 7:
                raise
            time.sleep(0.5 * (i + 1))


def write_json(path, doc):
    def w():
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            json.dump(doc, fh, indent=2)
            fh.write("\n")
    retry(w)


def md5(path):
    with open(path, "rb") as fh:
        return hashlib.md5(fh.read()).hexdigest()


def load_manifest():
    man = json.load(open(MANIFEST, encoding="utf-8")) if os.path.exists(MANIFEST) else {"models": {}}
    for p in glob.glob(os.path.join(PARTS, "*.json")):                 # results of worker processes not merged yet
        part = json.load(open(p, encoding="utf-8"))
        man["models"].update(part)
    return man


# ----------------------------------------------------------------------------- model facts
class Model:
    def __init__(self, name, doc=None):
        self.name = name
        if doc is None:
            with open(os.path.join(GEO, name + ".geo.json"), encoding="utf-8") as fh:
                doc = json.load(fh)
        self.doc = doc
        self.g = self.doc["minecraft:geometry"][0]
        self.size = self.g["description"]["texture_width"], self.g["description"]["texture_height"]
        self.bones = {b["name"]: b for b in self.g["bones"]}
        lid_bone = next(b for b in self.g["bones"] if b["name"].startswith("eyelid") and b.get("cubes"))
        self.head = self.bones[lid_bone["parent"]]
        soft = json.load(open(SF.MANIFEST, encoding="utf-8"))["heads"].get(name)
        if soft:
            info = soft[0]
            self.face = tuple(info["rect"])
            self.lids = [tuple(lid[:6]) for lid in info["lids"]]
            self.cap_rects = [rect_of(f) for f in info["orig"]["cap"].values() if not is_skip(f)] if info["orig"]["cap"] else []
            self.core = next((c for c in self.head["cubes"] if [int(x) for x in c["uv"]["north"]["uv"]] == list(self.face[:2])
                              and not c.get("inflate")), self.head["cubes"][0])
        else:                                            # never softened (made round by its generator): find the face here
            lc = [c for b in self.g["bones"] if b["name"].startswith("eyelid") for c in b.get("cubes", [])]
            lz = min(c["origin"][2] for c in lc)
            cands = [c for c in self.head["cubes"] if not c.get("inflate") and not cube_R(c) and c["size"][2] >= 6 and c["size"][0] >= 6]
            self.core = min(cands, key=lambda c: (round(abs(c["origin"][2] - lz), 3), -c["size"][0] * c["size"][1] * c["size"][2]))
            n = self.core["uv"]["north"]
            unit = self.core["size"][0] / abs(n["uv_size"][0])
            top = self.core["origin"][1] + self.core["size"][1]
            fw = int(abs(n["uv_size"][0]))
            pseudo = {"origin": [self.core["origin"][0], top - fw * unit, self.core["origin"][2]], "size": [self.core["size"][0], fw * unit, 8],
                      "uv": {"north": {"uv": n["uv"], "uv_size": [fw, fw]}}}
            info = SF._info(pseudo, lc)
            self.face = info["rect"]
            self.lids = [tuple(lid[:6]) for lid in info["lids"]]
            self.cap_rects = []
        clo, chi = SF.box(self.core)
        self.unit = (chi[0] - clo[0]) / self.face[2]
        self.top = chi[1]
        self.front = clo[2]
        self.textures = self._textures()

    def _textures(self):
        from PIL import Image
        out = []
        for p in SF.textures_by_model(model_names()).get(self.name, []):
            if os.path.basename(p)[len(self.name):].startswith(("_beauty", "_hatchling", "_young")):
                continue
            with Image.open(p) as im:
                if im.size == tuple(self.size):
                    out.append(p)
        return sorted(out)

    def chain(self, bone):
        out = []
        while bone:
            out.append(bone)
            bone = self.bones[bone].get("parent")
        return out

    def cubes(self):
        for b in self.g["bones"]:
            for c in b.get("cubes", []):
                yield b["name"], c

    def head_like(self, bone):
        return any(n == self.head["name"] or n.startswith(("hair", "ahoge", "fringe", "eyelid", "mouth")) for n in self.chain(bone))

    def feature(self, bone):
        return bone.startswith(("eyelid", "mouth", "pupil"))


_NAMES = []


def model_names():
    """every geo with a face (an eyelids bone)"""
    if not _NAMES:
        for p in sorted(glob.glob(os.path.join(GEO, "*.geo.json"))):
            n = os.path.basename(p)[:-9]
            if n.endswith("_beauty"):
                continue
            with open(p, encoding="utf-8") as fh:
                txt = fh.read()
            if '"eyelids' in txt:
                _NAMES.append(n)
    return _NAMES


# ----------------------------------------------------------------------------- pass 0: curves
def subtree(g, root):
    kids = {}
    for b in g["bones"]:
        kids.setdefault(b.get("parent"), []).append(b["name"])
    out, todo = [], [root]
    while todo:
        n = todo.pop()
        out.append(n)
        todo += kids.get(n, [])
    return set(out)


def shift(g, names, dx):
    for b in g["bones"]:
        if b["name"] in names:
            b["pivot"] = [r4(b["pivot"][0] + dx)] + b["pivot"][1:]
            for c in b.get("cubes", []):
                c["origin"][0] = r4(c["origin"][0] + dx)
                if "pivot" in c:
                    c["pivot"][0] = r4(c["pivot"][0] + dx)


def set_box(c, x0=None, y0=None, z0=None, w=None, h=None, d=None):
    """move/resize a cube; its texture rects stay (the texels stretch)"""
    old = list(c["origin"])
    for i, (o, s) in enumerate(((x0, w), (y0, h), (z0, d))):
        if o is not None:
            c["origin"][i] = r4(o)
        if s is not None:
            c["size"][i] = r4(s)
    if "pivot" in c:
        c["pivot"] = [r4(c["pivot"][i] + c["origin"][i] - old[i]) for i in range(3)]


def curves(g, k):
    """wide hips, a fuller stomach, a bust as wide as the hips; thighs and arms moved out to suit"""
    bones = {b["name"]: b for b in g["bones"]}
    body = bones["body"]
    by = body["pivot"][1]

    def at(c, dy, hgt=None):
        return abs(c["origin"][1] - (by + dy)) < 0.02 and (hgt is None or abs(c["size"][1] - hgt) < 0.02)
    hips = next(c for c in body["cubes"] if at(c, 0, 1.25) and c["size"][0] > 5)
    hz = hips["origin"][2] + hips["size"][2] / 2
    hw, hd = 8.0 * k, 5.0 * k
    set_box(hips, x0=-hw / 2, y0=by - 0.5, z0=hz - hd / 2, w=hw, h=1.75, d=hd)
    belt = next((c for c in body["cubes"] if at(c, 0.9, 0.75) and c["size"][0] > 5), None)
    if belt:
        set_box(belt, x0=-(hw + 0.4) / 2, z0=hz - (hd + 0.4) / 2, w=hw + 0.4, d=hd + 0.4)
        for c in body["cubes"]:
            if at(c, 0.775) and c["size"][2] <= 0.3:                     # the buckle
                set_box(c, z0=hz - (hd + 0.4) / 2 - 0.2)
    for c in body["cubes"] + bones.get("belly", {}).get("cubes", []):
        if at(c, 1.25, 2.75):                                            # the stomach
            z1 = c["origin"][2] + c["size"][2]
            d = c["size"][2] + 1.25 * k
            set_box(c, x0=-2.75 * k, z0=z1 - d, w=5.5 * k, d=d)
        elif at(c, 4.0, 3.0) and c["size"][0] >= 4.5:                    # the chest
            z1 = c["origin"][2] + c["size"][2]
            d = c["size"][2] + 0.5 * k
            set_box(c, x0=-3.125 * k, z0=z1 - d, w=6.25 * k, d=d)
    busts = [c for c in body["cubes"] if at(c, 4.9) and c["size"][2] <= 1.3]
    for c in busts:
        w, hgt, d = 3.6 * k, 2.6 * k, 2.0 * k
        z1 = c["origin"][2] + c["size"][2]
        x0 = -0.1 - w if c["origin"][0] < 0 else 0.1
        set_box(c, x0=x0, y0=c["origin"][1] - (hgt - c["size"][1]) * 0.6, z0=z1 - d - 0.35 * k, w=w, h=hgt, d=d + 0.35 * k)
    for side, sgn in (("r", -1), ("l", 1)):
        shift(g, subtree(g, "arm_" + side), sgn * 0.65 * k)
        shift(g, subtree(g, "leg_" + side), sgn * 0.45 * k)
        thigh = bones["leg_" + side]["cubes"][0]
        cx = thigh["origin"][0] + thigh["size"][0] / 2
        cz = thigh["origin"][2] + thigh["size"][2] / 2
        set_box(thigh, x0=cx - 1.875 * k, z0=cz - 1.75 * k, w=3.75 * k, d=3.5 * k)
    if "skirt" in bones:
        front, back = hz - hd / 2 - 0.35, hz + hd / 2 + 0.1
        for c in bones["skirt"]["cubes"]:
            w, h_, d = c["size"]
            if d <= 0.3 and w > 3:                                     # front and back panels
                set_box(c, x0=-(hw + 0.25) / 2, z0=front if c["origin"][2] < hz else back, w=hw + 0.25)
            elif w <= 0.3 and d > 3:                                   # side panels
                set_box(c, x0=(-(hw / 2) - 0.4) if c["origin"][0] < 0 else hw / 2 + 0.15, z0=hz - (hd + 0.5) / 2, d=hd + 0.5)


# ----------------------------------------------------------------------------- pass 1: colour
def occlusion(m):
    """{pixel: darkness 0..1} from short rays leaving each face (rest pose) that end inside another cube"""
    clo, chi = SF.box(m.core)
    centre = [(clo[i] + chi[i]) / 2 for i in range(3)]
    occl = []
    for bone, c in m.cubes():
        lo, hi = SF.box(c)
        if bone == m.head["name"] and c is not m.core and all(lo[i] < centre[i] < hi[i] for i in range(3)) and c["size"][1] > 5:
            continue                                     # the hair shell round the head: hollow, mostly see-through
        if m.feature(bone):
            continue
        R = cube_R(c)
        corners = [(x, y, z) for x in (lo[0], hi[0]) for y in (lo[1], hi[1]) for z in (lo[2], hi[2])]
        if R:
            corners = [H.rot_about(p, c["pivot"], R) for p in corners]
        alo = [min(p[i] for p in corners) for i in range(3)]
        ahi = [max(p[i] for p in corners) for i in range(3)]
        occl.append((c, lo, hi, transpose(R) if R else None, alo, ahi))

    def inside(p, o):
        c, lo, hi, Ri, alo, ahi = o
        if not all(alo[i] - 1e-6 <= p[i] <= ahi[i] + 1e-6 for i in range(3)):
            return False
        q = H.rot_about(p, c["pivot"], Ri) if Ri else p
        return all(lo[i] + 0.02 < q[i] < hi[i] - 0.02 for i in range(3))

    acc = {}
    for bone, c in m.cubes():
        if m.feature(bone):
            continue
        for face in QUAD:
            if is_skip(c["uv"][face]):
                continue
            pts, n, du, dv, nw, nh = face_points(c, face)
            lu = math.sqrt(sum(x * x for x in du)) or 1
            lv = math.sqrt(sum(x * x for x in dv)) or 1
            eu = [x / lu for x in du]
            ev = [x / lv for x in dv]
            flo = [min(p[6][i] for p in pts) - 1.3 for i in range(3)]
            fhi = [max(p[6][i] for p in pts) + 1.3 for i in range(3)]
            near = [o for o in occl if o[0] is not c and all(o[4][i] <= fhi[i] and o[5][i] >= flo[i] for i in range(3))]
            if not near:
                continue
            for i, j, s, t, px, py, p in pts:
                hits, total = 0.0, 0.0
                for d, w in ((0.3, 1.0), (0.7, 0.7), (1.2, 0.4)):
                    for a, b in ((0, 0), (0.45, 0), (-0.45, 0), (0, 0.45), (0, -0.45)):
                        q = [p[k] + n[k] * d + eu[k] * a * d + ev[k] * b * d for k in range(3)]
                        total += w
                        if any(inside(q, o) for o in near):
                            hits += w
                a = acc.setdefault((px, py), [0.0, 0])
                a[0] += hits / total
                a[1] += 1
    return {k: v[0] / v[1] for k, v in acc.items() if v[0] > 0}


def roundness(m):
    """{pixel: light factor} shading limb, body and hair faces like rounded surfaces (top a little lighter)"""
    fac = {}
    for bone, c in m.cubes():
        if m.feature(bone) or (bone == m.head["name"] and c["size"][2] >= 6):
            continue                                       # the face pieces and the hair shell are shaded already
        for face in QUAD:
            f = c["uv"][face]
            if is_skip(f):
                continue
            tx, nw, nh = texels_of(f)
            if nw < 4 or nh < 3:
                continue
            for i, j, s, t, px, py in tx:
                k = 1.035 - 0.11 * (2 * s - 1) ** 2
                if face not in ("up", "down"):
                    k += 0.035 * (0.5 - t)
                fac.setdefault((px, py), k)
    return fac


def skin_set(m, tex):
    u, v, w, h = m.lids[0][:4]
    cols = {tex[v + y][u + x][:3] for y in range(h) for x in range(w) if tex[v + y][u + x][3] >= 128}
    if len(cols) > 1:
        cols.discard(min(cols, key=lambda c: sum(c)))          # the lash line
    fu, fv, fw, fh = m.face
    if cols:
        for y in range(fh):                                    # the soft face's extra skin shades
            for x in range(fw):
                c = tex[fv + y][fu + x]
                if c[3] >= 128 and min(dist(c[:3], s) for s in cols) <= 24:
                    cols.add(c[:3])
    return cols


def used_rects(m):
    out = set()
    for bone, c in m.cubes():
        for f in c["uv"].values():
            if not is_skip(f):
                u, v, su, sv = rect_of(f)
                x0, x1 = sorted((u, u + su))
                y0, y1 = sorted((v, v + sv))
                out.add((int(x0), int(y0), int(math.ceil(x1)), int(math.ceil(y1)), bone))
    return out


def colour_pass(m, tex, ao, rnd, protect, skin):
    out = [row[:] for row in tex]
    W, Hh = m.size
    fu, fv, fw, fh = m.face
    # a darker line where clothing or feathers meet bare skin (not on the face)
    for x0, y0, x1, y1, bone in used_rects(m):
        if m.feature(bone) or (fu <= x0 < fu + fw and fv <= y0 < fv + fh):
            continue
        for y in range(y0, min(y1, Hh)):
            for x in range(x0, min(x1, W)):
                c = tex[y][x]
                if c[3] < 128 or c[:3] in skin or (x, y) in protect:
                    continue
                if any(x0 <= xx < x1 and y0 <= yy < y1 and 0 <= xx < W and 0 <= yy < Hh and tex[yy][xx][3] >= 128
                       and tex[yy][xx][:3] in skin for xx, yy in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1))):
                    out[y][x] = mix(SF.shade(c, 0.84, x, y), SHADOW_HUE, 0.10)
    # shading: occlusion x roundness, shadows tinted purple, lights warm
    for y in range(Hh):
        for x in range(W):
            c = out[y][x]
            if c[3] < 128 or (x, y) in protect:
                continue
            f = rnd.get((x, y), 1.0) * (1 - 0.30 * ao.get((x, y), 0.0))
            c2 = SF.shade(c, f, x, y)
            if f < 1:
                c2 = mix(c2, SHADOW_HUE, (1 - f) * 1.5)
            else:
                c2 = mix(c2, LIGHT_HUE, (f - 1) * 1.6)
            L = lum(c2)
            if L < 0.32:                                       # the darkest steps of every ramp lean purple too
                c2 = mix(c2, SHADOW_HUE, (0.32 - L) * 0.30)
            elif L > 0.80:
                c2 = mix(c2, LIGHT_HUE, (L - 0.80) * 0.25)
            out[y][x] = c2
    return out


# ----------------------------------------------------------------------------- pass 2: eyes
def eye_blob(m, tex, skin, cx):
    """the non-skin pixels joined to the eye centre (face texels), or None when it does not look like an eye"""
    fu, fv, fw, fh = m.face
    seen, todo, blob = set(), [(cx, 18), (cx - 1, 18), (cx, 17), (cx - 1, 17)], set()
    while todo:
        x, y = todo.pop()
        if (x, y) in seen or not (cx - 8 <= x < cx + 8 and 10 <= y <= 26):
            continue
        seen.add((x, y))
        c = tex[fv + y][fu + x]
        if c[3] < 128 or c[:3] in skin:
            continue
        blob.add((x, y))
        todo += [(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)]
    if not 16 <= len(blob) <= 130:
        return None
    xs = [x for x, y in blob]
    ys = [y for x, y in blob]
    if not (4 <= max(xs) - min(xs) + 1 <= 13 and 4 <= max(ys) - min(ys) + 1 <= 13):
        return None
    if any(x in (cx - 8, cx + 7) or y in (10, 26) for x, y in blob):
        return None                                        # runs into something else (markings, a mask, the hairline)
    return blob


def redraw_eye(m, tex, out, skin, cx, blob):
    """one eye redrawn in out; returns (changed face texels, pupil overlay (ox, oy, rows) or None)"""
    fu, fv, fw, fh = m.face

    def px(x, y):
        return tex[fv + y][fu + x]
    top, bot = min(y for x, y in blob), max(y for x, y in blob)
    edge = {(x, y) for x, y in blob if any(p not in blob for p in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)))}
    inner = blob - edge
    if len(inner) < 6:
        return set(), None
    lash = darken(min((px(x, y)[:3] for x, y in blob), key=lum), 0.75)
    inner_cols = [px(x, y)[:3] for x, y in inner]
    shine = [c for c in inner_cols if lum(c) >= 0.80]
    rest = sorted((c for c in inner_cols if lum(c) < 0.80), key=lum)
    if not rest:
        return set(), None
    med = rest[len(rest) // 2]
    darkest = rest[0]
    pupil_pts = set()
    if lum(darkest) < 0.6 * lum(med) or lum(darkest) < 0.09:
        cut = max(lum(darkest) + 0.03, min(0.6 * lum(med), lum(darkest) + 0.12))
        pupil_pts = {(x, y) for x, y in inner if lum(px(x, y)) <= cut}
    iris = sorted({c for c in rest if (lum(c) > lum(darkest) + 0.03) or not pupil_pts}, key=lum) or rest
    lo_c = darken(iris[0], 0.62)
    mid_c = iris[len(iris) // 2]
    hi_c = iris[-1]
    glow_c = boost(hi_c)
    i_top, i_bot = min(y for x, y in inner), max(y for x, y in inner)
    changed = set()

    def put(x, y, c):
        out[fv + y][fu + x] = rgba(c)
        changed.add((x, y))
    for x, y in blob:
        t = (y - i_top) / max(1, i_bot - i_top)
        if t < 0.30:
            c = lo_c
        elif t < 0.62:
            c = mix(lo_c, mid_c, (t - 0.30) / 0.32)
        elif y < i_bot:
            c = mix(mid_c, hi_c, (t - 0.62) / 0.38)
        else:
            c = glow_c if abs(x + 0.5 - cx) < 2.6 else mix(hi_c, glow_c, 0.5)
        if (x, y) in edge:
            if y <= top + 1:
                c = lash
            elif lum(px(x, y)) < lum(lo_c) * 1.1 or lum(px(x, y)) < 0.18:
                c = px(x, y)[:3]                                    # a dark outline stays
            else:
                c = darken(c, 0.85)
        elif any(p in edge for p in ((x + 1, y), (x - 1, y))) and y > i_top + 1:
            c = mix(c, lo_c, 0.55)                                  # the iris rim
        put(x, y, c)
    # the thicker top lash and the wing at the outer corner
    for x, y in blob:
        if y == top + 1 and (x, top) not in blob and px(x, top)[:3] in skin:
            put(x, top, lash)
    outer = -1 if cx < 16 else 1
    rows = [x for x, y in blob if y <= top + 2]
    xe = min(rows) if outer < 0 else max(rows)
    for dx, dy in ((outer, 1), (outer, 0), (2 * outer, 0), (2 * outer, -1)):
        x, y = xe + dx, top + dy
        if 0 <= x < fw and 0 <= y < fh and px(x, y)[:3] in skin:
            put(x, y, lash)
    # a faint lower lash at the outer half
    bx = sorted(x for x, y in blob if y == bot)
    for x in (bx[:3] if outer < 0 else bx[-3:]):
        y = bot + 1
        if y < fh and px(x, y)[:3] in skin:
            put(x, y, mix(lash, px(x, y)[:3], 0.55))
    # the pupil and highlights: an overlay 6 x 7 texels centred on the old pupil (or the eye)
    pts = pupil_pts or inner
    pcx = (min(x for x, y in pts) + max(x for x, y in pts) + 1) / 2
    pcy = (min(y for x, y in pts) + max(y for x, y in pts) + 1) / 2
    ox, oy = int(round(pcx - 3)), int(round(pcy - 3.5))
    ov = [[CLEAR] * 6 for _ in range(7)]
    if pupil_pts:
        pc = darken(sorted((px(x, y)[:3] for x, y in pupil_pts), key=lum)[len(pupil_pts) // 2], 0.55)
        pw = max(x for x, y in pupil_pts) - min(x for x, y in pupil_pts) + 1
        ph = max(y for x, y in pupil_pts) - min(y for x, y in pupil_pts) + 1
        if pw <= 2 and ph >= 4:                                   # a slit stays a slit
            cells = [(x, y) for y in range(max(0, 4 - (ph + 1) // 2), min(7, 4 + ph // 2)) for x in ((2, 3) if pw == 2 else (3,))]
        else:
            cells = [(2, 2), (3, 2), (2, 3), (3, 3), (2, 4), (3, 4), (2, 5), (3, 5), (1, 3), (4, 3), (1, 4), (4, 4)]
        for x, y in cells:
            ov[y][x] = rgba(pc)
    sh = max(shine, key=lum) if shine else (255, 252, 244)
    sh = mix(sh, (255, 252, 244), 0.5)
    for x, y in ((3, 1), (4, 1), (3, 2), (4, 2)):
        ov[y][x] = rgba(sh)
    if ov[1][2] != CLEAR or pupil_pts:
        ov[1][2] = rgba(mix(sh, lo_c, 0.35))
    ov[5][1] = rgba(mix(sh, glow_c, 0.3))
    return changed, (ox, oy, ov)


def eyes_pass(m, tex, skin):
    """both eyes redrawn; returns (texture, changed face texels, overlays [(side, ox, oy, rows)])"""
    fu, fv, fw, fh = m.face
    out = [row[:] for row in tex]
    if (fw, fh) != (32, 32):
        return out, set(), []
    blobs = {cx: eye_blob(m, tex, skin, cx) for cx in (8, 24)}
    changed, overlays = {}, {}
    for cx, blob in blobs.items():
        if blob:
            ch, ov = redraw_eye(m, tex, out, skin, cx, blob)
            if ov:
                changed[cx], overlays[cx] = ch, ov
    if len(overlays) == 2:
        # mirror her left eye (texture right) onto her right when the two were meant to be the same: the generators drew a
        # sideways lash wing on one eye only
        mirror = {(fw - 1 - x, y) for x, y in blobs[24]}
        cols = lambda b: {tex[fv + y][fu + x][:3] for x, y in b}     # noqa: E731
        if len(mirror ^ blobs[8]) <= 8 and cols(blobs[8]) == cols(blobs[24]):
            region = {(fw - 1 - x, y) for x, y in changed[24]} | changed[8] | blobs[8]
            for x, y in region:
                out[fv + y][fu + x] = out[fv + y][fu + fw - 1 - x]
            changed[8] = region
            ox, oy, ov = overlays[24]
            overlays[8] = (fw - 6 - ox, oy, ov)
    allc = set().union(*changed.values()) if changed else set()
    return out, allc, [("r" if cx < 16 else "l", *overlays[cx]) for cx in sorted(overlays)]


# ----------------------------------------------------------------------------- pass 3: hair
HAIR_WORDS = ("hair", "bang", "fringe", "ahoge", "braid", "pony", "bun", "twin", "drill", "mane")


def hair_set(m, tex):
    cols = {}
    rects = list(m.cap_rects)
    if not rects:
        for bone, c in m.cubes():
            if any(w in bone for w in HAIR_WORDS):
                rects += [rect_of(f) for f in c["uv"].values() if not is_skip(f)]
    W, Hh = m.size
    for u, v, su, sv in rects:
        x0, x1 = sorted((u, u + su))
        y0, y1 = sorted((v, v + sv))
        for y in range(int(y0), min(Hh, int(math.ceil(y1)))):
            for x in range(int(x0), min(W, int(math.ceil(x1)))):
                if tex[y][x][3] >= 128:
                    cols[tex[y][x][:3]] = cols.get(tex[y][x][:3], 0) + 1
    return {c for c, n in cols.items() if n >= 2}


def fringe_cubes(m, head, tex=None, hair=None):
    """thin hair locks hanging in front of the face from the head bone"""
    out = []
    for c in head["cubes"]:
        if c is m.core or c.get("inflate") or c["size"][2] > 0.6 or c["origin"][2] > m.front - 0.7:
            continue
        if c["origin"][1] + c["size"][1] < m.top - 4.0 or c["size"][1] < 1.5:
            continue
        if tex is not None:
            f = c["uv"]["north"]
            tx, nw, nh = texels_of(f)
            px = [tex[py][px_] for i, j, s, t, px_, py in tx if 0 <= py < len(tex) and 0 <= px_ < len(tex[0])]
            op = [p for p in px if p[3] >= 128]
            if not op or sum(p[:3] in hair for p in op) < 0.6 * len(op):
                continue
        out.append(c)
    return out


def hair_pass(m, tex, hair, protect, fringe):
    """the shine band round the head, and pointed fringe tips"""
    out = [row[:] for row in tex]
    ramp = sorted(hair, key=lum)
    band = set()
    if len(ramp) < 3:
        return out, band
    idx = {c: i for i, c in enumerate(ramp)}
    y_lo, y_hi = m.top - 2.1, m.top - 1.0
    for bone, c in m.cubes():
        if not m.head_like(bone) or m.feature(bone):
            continue
        for face in ("north", "south", "east", "west"):
            if is_skip(c["uv"][face]):
                continue
            pts, n, *_ = face_points(c, face)
            for i, j, s, t, px, py, p in pts:
                col = out[py][px]
                if col[3] < 128 or col[:3] not in idx or (px, py) in protect:
                    continue
                if y_lo <= p[1] <= y_hi:
                    edge = p[1] < y_lo + 0.25 or p[1] > y_hi - 0.25
                    if H.noise(px, py, 41) < (0.45 if edge else 0.18):
                        continue
                    k = idx[col[:3]]
                    lit = ramp[min(len(ramp) - 1, k + (1 if edge else 2) + len(ramp) // 6)]
                    out[py][px] = mix(rgba(lit), LIGHT_HUE, 0.18)
                    band.add((px, py))
    for c in fringe:                                       # pointed tips: the last two texel rows of each lock
        for face in ("north", "south", "east", "west"):
            f = c["uv"][face]
            if is_skip(f):
                continue
            tx, nw, nh = texels_of(f)
            if nh < 8:
                continue
            for i, j, s, t, px, py in tx:
                if j == nh - 1 and (face in ("east", "west") or i % 4 in (0, 3)):
                    out[py][px] = CLEAR
                elif j == nh - 2 and face in ("north", "south") and i % 4 == 0 and nw > 4:
                    out[py][px] = CLEAR
    return out, band


# ----------------------------------------------------------------------------- pass 4: body geometry
def rounded_pieces(c, top=False, bust=False):
    """a cube as two crossing boxes (no square vertical edges); top = one more texel row inset on top (shoulders)"""
    lo, hi = SF.box(c)
    tx, ty, tz = SF.texel(c, lo, hi)
    if bust:                                                 # the front texel layer inset
        return [SF.piece(c, lo, hi, [lo[0], lo[1], lo[2] + tz], hi),
                SF.piece(c, lo, hi, [lo[0] + tx, lo[1] + ty, lo[2]], [hi[0] - tx, hi[1] - ty, lo[2] + tz])]
    yt = hi[1] - ty if top else hi[1]
    out = [SF.piece(c, lo, hi, [lo[0], lo[1], lo[2] + tz], [hi[0], yt, hi[2] - tz]),
           SF.piece(c, lo, hi, [lo[0] + tx, lo[1], lo[2]], [hi[0] - tx, yt, hi[2]])]
    if top:
        out.append(SF.piece(c, lo, hi, [lo[0] + tx, yt, lo[2] + tz], [hi[0] - tx, hi[1], hi[2] - tz]))
    return out


def round_bone(name):
    return name in ("body", "belly", "barrel") or name.startswith(("arm_", "forearm_", "leg_", "leg1", "leg2", "shin_", "foot_", "thigh"))


def body_pass(m, g):
    """returns {bone: [the uv dicts of the cubes it split]}"""
    orig = {}
    for b in g["bones"]:
        if not round_bone(b["name"]):
            continue
        new = []
        front = min((c["origin"][2] for c in b.get("cubes", []) if c["size"][2] >= 2), default=0)
        for c in b.get("cubes", []):
            lo, hi = SF.box(c)
            rot = cube_R(c)
            w, h, d = c["size"]
            sides_ok = all(not is_skip(c["uv"][f]) for f in ("north", "south", "east", "west"))
            tx, ty, tz = SF.texel(c, lo, hi) if sides_ok else (1, 1, 1)
            if (not rot and sides_ok and not c.get("inflate") and w >= 1.5 and d >= 1.5 and h >= 0.75
                    and w >= 4 * tx and d >= 4 * tz):
                top = b["name"].startswith("arm_") or (b["name"] == "body" and w >= 4.5 and h >= 2.5)
                new += rounded_pieces(c, top=top and h >= 4 * ty)
                orig.setdefault(b["name"], []).append(c["uv"])
            elif (not rot and sides_ok and not c.get("inflate") and b["name"] == "body" and d <= 2.2 and h >= 1.25
                  and 1.5 <= w <= 4.2 and lo[2] < front - 0.3 and w >= 4 * tx and h >= 4 * ty):
                new += rounded_pieces(c, bust=True)
                orig.setdefault(b["name"], []).append(c["uv"])
            else:
                new.append(c)
        b["cubes"] = new
    return orig


# ----------------------------------------------------------------------------- pass 5: pupils, fringe bone
def free_spot(occ, texes, W, Hh, w, h):
    for y in range(Hh - h, 0, -1):
        for x in range(W - w, 0, -1):
            if all((xx, yy) not in occ for yy in range(y, y + h) for xx in range(x, x + w)) and \
                    all(t[yy][xx][3] == 0 for t in texes for yy in range(y, y + h) for xx in range(x, x + w)):
                return x, y
    return None


def add_alive(m, g, texes, overlays, fringe):
    """pupil bones + the fringe bone; returns {side: (x, y)}, where each pupil's 6 x 7 texels go in the texture"""
    head = next(b for b in g["bones"] if b["name"] == m.head["name"])
    W, Hh = m.size
    occ = set()
    for x0, y0, x1, y1, _ in used_rects(m):
        for y in range(y0, y1):
            for x in range(x0, x1):
                occ.add((x, y))
    placed = {}
    if overlays:
        dot = free_spot(occ, texes, W, Hh, 2, 2)
        spots = []
        if dot:
            occ |= {(dot[0] + i, dot[1] + j) for i in range(2) for j in range(2)}
            for _ in overlays:
                s = free_spot(occ, texes, W, Hh, 6, 7)
                if not s:
                    break
                occ |= {(s[0] + i, s[1] + j) for i in range(6) for j in range(7)}
                spots.append(s)
        if dot and len(spots) == len(overlays):
            skip = {"uv": [dot[0], dot[1]], "uv_size": [1, 1]}
            clo, chi = SF.box(m.core)
            for (side, ox, oy, ov), (x, y) in zip(overlays, spots):
                placed[side] = (x, y)
                x0 = clo[0] + ox * m.unit
                y1 = m.top - oy * m.unit
                cube = {"origin": [r4(x0), r4(y1 - 7 * m.unit), r4(m.front - 0.05)], "size": [r4(6 * m.unit), r4(7 * m.unit), 0],
                        "uv": {f: dict(skip) for f in QUAD}}
                cube["uv"]["north"] = {"uv": [x, y], "uv_size": [6, 7]}
                g["bones"].append({"name": "pupil_" + side, "parent": head["name"],
                                   "pivot": [r4(x0 + 3 * m.unit), r4(y1 - 3.5 * m.unit), r4(m.front)], "cubes": [cube]})
        else:
            print(f"  {m.name}: no texture space for the pupils, kept them painted")
            overlays.clear()
    if fringe:                                            # the fringe gets its own bone, hung from the hairline, so it can sway
        ids = {id(c) for c in fringe}
        keep = [c for c in head["cubes"] if id(c) not in ids]
        moved = [c for c in head["cubes"] if id(c) in ids]
        head["cubes"] = keep
        g["bones"].append({"name": "fringe", "parent": head["name"], "pivot": [0, r4(m.top), r4(m.front - 0.5)], "cubes": moved})
    return placed


def animations(name, fringe):
    path = os.path.join(ANIM, name + ".animation.json")
    if not fringe or not os.path.exists(path):
        return None
    with open(path, encoding="utf-8") as fh:
        doc = json.load(fh)
    for key, an in doc.get("animations", {}).items():
        state = key.rsplit(".", 1)[1]
        L = float(an.get("animation_length", 4.0) or 4.0)
        bones = an.setdefault("bones", {})
        if "fringe" in bones:
            continue
        if state == "idle":
            bones["fringe"] = {"rotation": {"0.0": [0, 0, 0], str(r4(L * 0.25)): [-2, 0, 1.2], str(r4(L * 0.5)): [0, 0, 0],
                                            str(r4(L * 0.75)): [-1.5, 0, -1.2], str(r4(L)): [0, 0, 0]}}
        elif state == "walk":
            bones["fringe"] = {"rotation": {"0.0": [0, 0, 0], str(r4(L * 0.25)): [-4, 0, 1.5], str(r4(L * 0.5)): [0, 0, 0],
                                            str(r4(L * 0.75)): [-4, 0, -1.5], str(r4(L)): [0, 0, 0]}}
        elif state == "fly":
            bones["fringe"] = {"rotation": {"0.0": [-8, 0, 0], str(r4(L * 0.5)): [-11, 0, 0], str(r4(L)): [-8, 0, 0]}}
    return doc


# ----------------------------------------------------------------------------- glow masks
def glow_pass(m, mask, eyes_changed, overlay_paint, fringe_clear):
    """the glow mask follows: lit eyes stay lit over their new shape (pupil pieces too), cut fringe tips go dark"""
    if mask is None:
        return None
    fu, fv, fw, fh = m.face
    out = [row[:] for row in mask]
    lit_eyes = any(mask[fv + y][fu + x][3] > 0 for x, y in eyes_changed)
    if lit_eyes:
        src = next(mask[fv + y][fu + x] for x, y in eyes_changed if mask[fv + y][fu + x][3] > 0)
        for x, y in eyes_changed:
            if out[fv + y][fu + x][3] == 0:
                out[fv + y][fu + x] = src
    for (x, y), c in overlay_paint.items():
        out[y][x] = (255, 255, 255, 255) if lit_eyes and c[3] else CLEAR
    for x, y in fringe_clear:
        out[y][x] = CLEAR
    return out


# ----------------------------------------------------------------------------- driver
def process(name, dry=False):
    """beautify one model; returns its manifest entry, or None when there is nothing to do"""
    geo_path = os.path.join(GEO, name + ".geo.json")
    man = load_manifest()
    if man["models"].get(name, {}).get("geo") == md5(geo_path):
        return None
    with open(geo_path, encoding="utf-8") as fh:
        doc = json.load(fh)
    if name in CURVY:
        curves(doc["minecraft:geometry"][0], CURVY[name])
    m = Model(name, doc)
    texes = [SF.load_png(p) for p in m.textures]
    if not texes:
        print(f"{name}: no textures, skipped")
        return None
    ao = occlusion(m)
    rnd = roundness(m)
    g = copy.deepcopy(m.g)
    fringe = fringe_cubes(m, m.head, texes[0], hair_set(m, texes[0]))
    fringe_ids = {id(c) for c in fringe}
    results, ovs, overlays, eyes_changed, clear_px = [], [], None, set(), set()
    for tex in texes:
        skin = skin_set(m, tex)
        hair = hair_set(m, tex)
        t2, changed, ov = eyes_pass(m, tex, skin)
        ovs.append(ov)
        if overlays is None:
            overlays, eyes_changed = ov, changed
        fu, fv = m.face[:2]
        protect = {(fu + x, fv + y) for x, y in changed}
        t3, band = hair_pass(m, t2, hair, protect, fringe)
        clear_px |= {(x, y) for y in range(len(t3)) for x in range(len(t3[0])) if t3[y][x][3] == 0 and t2[y][x][3] > 0}
        results.append(colour_pass(m, t3, ao, rnd, protect | band, skin))
    orig = body_pass(m, g)
    # the fringe cubes in the copy are the ones at the same index in the head bone
    head_g = next(b for b in g["bones"] if b["name"] == m.head["name"])
    idxs = [i for i, c in enumerate(m.head["cubes"]) if id(c) in fringe_ids]
    fringe_g = [head_g["cubes"][i] for i in idxs]
    placed = add_alive(m, g, results, overlays, fringe_g)
    paints = []
    for t, ov in zip(results, ovs):                    # each colour gets its own pupils, in the same place
        own = {s: rows for s, ox, oy, rows in ov}
        base = {s: rows for s, ox, oy, rows in overlays}
        paint = {}
        for side, (x, y) in placed.items():
            rows = own.get(side, base[side])
            for j in range(7):
                for i in range(6):
                    paint[(x + i, y + j)] = rows[j][i]
                    t[y + j][x + i] = rows[j][i]
        paints.append(paint)
    if dry:
        print(f"{name}: {len(m.textures)} textures, eyes {len(overlays)}, fringe {len(fringe_g)}, split {sum(map(len, orig.values()))}")
        return None
    # back up, then write in place
    bk = os.path.join(BACKUP, name)
    if not os.path.exists(bk):
        os.makedirs(bk)
        shutil.copy2(geo_path, bk)
        ap = os.path.join(ANIM, name + ".animation.json")
        if os.path.exists(ap):
            shutil.copy2(ap, bk)
        for p in m.textures:
            shutil.copy2(p, bk)
            gp = p[:-4] + "_glowmask.png"
            if os.path.exists(gp):
                shutil.copy2(gp, bk)
    for p, t, paint in zip(m.textures, results, paints):
        retry(H.write_png, p, t)
        gp = p[:-4] + "_glowmask.png"
        if os.path.exists(gp):
            retry(H.write_png, gp, glow_pass(m, SF.load_png(gp), eyes_changed, paint, clear_px))
    an = animations(name, fringe_g)
    if an:
        write_json(os.path.join(ANIM, name + ".animation.json"), an)
    write_json(geo_path, doc | {"minecraft:geometry": [g]})
    entry = {"geo": md5(geo_path), "orig": orig, "pupils": len(overlays), "fringe": len(fringe_g), "curvy": name in CURVY}
    os.makedirs(PARTS, exist_ok=True)
    with open(os.path.join(PARTS, name + ".json"), "w", encoding="utf-8") as fh:
        json.dump({name: entry}, fh)
    print(f"{name}: {len(results)} textures, {len(overlays)} pupils, {len(fringe_g)} fringe locks, "
          f"{sum(map(len, orig.values()))} cubes rounded{', curvy' if name in CURVY else ''}")
    return entry


def merge():
    man = load_manifest()
    with open(MANIFEST, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(man, fh, indent=1, sort_keys=True)
        fh.write("\n")
    for p in glob.glob(os.path.join(PARTS, "*.json")):
        os.remove(p)
    print(f"manifest: {len(man['models'])} models")


# ----------------------------------------------------------------------------- comparison render (lit, like the game)
L0 = (0.2, 1.0, -0.7)
L1 = (-0.2, 1.0, 0.7)


def _norm(v):
    n = math.sqrt(sum(x * x for x in v)) or 1
    return tuple(x / n for x in v)


def render_lit(model, tex, anim, tm, yaw, pitch, W, Hh, scale, bg, center, extra=None):
    """harpy_lib.render with Minecraft-like face lighting (two lights from above) and an optional extra pose"""
    bones = {b["name"]: b for b in model["minecraft:geometry"][0]["bones"]}
    pose = {}
    if anim:
        for name, ch in anim["bones"].items():
            if name in bones:
                pose[name] = {k: H.sample(v, tm) for k, v in ch.items() if isinstance(v, dict)}
    for name, ch in (extra or {}).items():
        pose.setdefault(name, {}).update(ch)

    def bone_pt(name, p):
        while name:
            b = bones[name]
            ps = pose.get(name, {})
            rot = ps.get("rotation", (0, 0, 0))
            if any(rot):
                p = H.rot_about(p, b["pivot"], H.rot_matrix(*rot))
            pos = ps.get("position")
            if pos:
                p = (p[0] + pos[0], p[1] + pos[1], p[2] + pos[2])
            name = b.get("parent")
        return p

    cy, sy = math.cos(math.radians(yaw)), math.sin(math.radians(yaw))
    cp, sp = math.cos(math.radians(pitch)), math.sin(math.radians(pitch))
    l0, l1 = _norm(L0), _norm(L1)

    def project(p):
        x1 = p[0] * cy + p[2] * sy
        z1 = -p[0] * sy + p[2] * cy
        y2 = p[1] * cp + z1 * sp
        z2 = z1 * cp - p[1] * sp
        return (W / 2 + (x1 - center[0]) * scale, Hh / 2 - (y2 - center[1]) * scale, z2)

    img = [[bg] * W for _ in range(Hh)]
    zbuf = [[1e9] * W for _ in range(Hh)]

    def tri(a, b, c, ua, ub, uc, rc, light):
        minx, maxx = int(max(0, math.floor(min(a[0], b[0], c[0])))), int(min(W - 1, math.ceil(max(a[0], b[0], c[0]))))
        miny, maxy = int(max(0, math.floor(min(a[1], b[1], c[1])))), int(min(Hh - 1, math.ceil(max(a[1], b[1], c[1]))))
        den = (b[1] - c[1]) * (a[0] - c[0]) + (c[0] - b[0]) * (a[1] - c[1])
        if abs(den) < 1e-9:
            return
        x_lo, x_hi = sorted((rc[0], rc[0] + rc[2]))
        y_lo, y_hi = sorted((rc[1], rc[1] + rc[3]))
        for y in range(miny, maxy + 1):
            row, zrow = img[y], zbuf[y]
            for x in range(minx, maxx + 1):
                px, py = x + 0.5, y + 0.5
                l1_ = ((b[1] - c[1]) * (px - c[0]) + (c[0] - b[0]) * (py - c[1])) / den
                l2_ = ((c[1] - a[1]) * (px - c[0]) + (a[0] - c[0]) * (py - c[1])) / den
                l3_ = 1 - l1_ - l2_
                if l1_ < -1e-4 or l2_ < -1e-4 or l3_ < -1e-4:
                    continue
                z = l1_ * a[2] + l2_ * b[2] + l3_ * c[2]
                if z >= zrow[x]:
                    continue
                u = l1_ * ua[0] + l2_ * ub[0] + l3_ * uc[0]
                v = l1_ * ua[1] + l2_ * ub[1] + l3_ * uc[1]
                tx = min(max(int(math.floor(u)), int(x_lo)), max(int(x_lo), int(math.ceil(x_hi)) - 1))
                ty = min(max(int(math.floor(v)), int(y_lo)), max(int(y_lo), int(math.ceil(y_hi)) - 1))
                if not (0 <= ty < len(tex) and 0 <= tx < len(tex[0])):
                    continue
                col = tex[ty][tx]
                if col[3] < 128:
                    continue
                zrow[x] = z
                row[x] = (int(col[0] * light), int(col[1] * light), int(col[2] * light), 255)

    for b in bones.values():
        for cu in b.get("cubes", []):
            if not isinstance(cu.get("uv"), dict):
                continue
            inf = cu.get("inflate", 0) or 0
            x0, y0, z0 = (cu["origin"][i] - inf for i in range(3))
            x1, y1, z1 = (cu["origin"][i] + cu["size"][i] + inf for i in range(3))
            quad = {
                "north": ((x0, y1, z0), (x1, y1, z0), (x1, y0, z0), (x0, y0, z0)),
                "south": ((x1, y1, z1), (x0, y1, z1), (x0, y0, z1), (x1, y0, z1)),
                "east": ((x0, y1, z1), (x0, y1, z0), (x0, y0, z0), (x0, y0, z1)),
                "west": ((x1, y1, z0), (x1, y1, z1), (x1, y0, z1), (x1, y0, z0)),
                "up": ((x0, y1, z1), (x1, y1, z1), (x1, y1, z0), (x0, y1, z0)),
                "down": ((x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)),
            }
            cc = ((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2)
            R = H.rot_matrix(*cu["rotation"]) if "rotation" in cu else None
            if R:
                cc = H.rot_about(cc, cu["pivot"], R)
            cc = bone_pt(b["name"], cc)
            for face, pts in quad.items():
                if face not in cu["uv"]:
                    continue
                if R:
                    pts = tuple(H.rot_about(p, cu["pivot"], R) for p in pts)
                wpts = [bone_pt(b["name"], p) for p in pts]
                e1 = [wpts[1][i] - wpts[0][i] for i in range(3)]
                e2 = [wpts[3][i] - wpts[0][i] for i in range(3)]
                nrm = _norm((e1[1] * e2[2] - e1[2] * e2[1], e1[2] * e2[0] - e1[0] * e2[2], e1[0] * e2[1] - e1[1] * e2[0]))
                fc = [sum(p[i] for p in wpts) / 4 for i in range(3)]
                if sum(nrm[i] * (fc[i] - cc[i]) for i in range(3)) < 0:
                    nrm = tuple(-x for x in nrm)
                light = min(1.0, 0.55 + 0.45 * (max(0, sum(nrm[i] * l0[i] for i in range(3))) + max(0, sum(nrm[i] * l1[i] for i in range(3)))))
                pp = [project(p) for p in wpts]
                (ru, rv), (rw, rh) = cu["uv"][face]["uv"], cu["uv"][face]["uv_size"]
                uvs = ((ru, rv), (ru + rw, rv), (ru + rw, rv + rh), (ru, rv + rh))
                tri(pp[0], pp[1], pp[2], uvs[0], uvs[1], uvs[2], (ru, rv, rw, rh), light)
                tri(pp[0], pp[2], pp[3], uvs[0], uvs[2], uvs[3], (ru, rv, rw, rh), light)
    return img


def _idle(path, name):
    if not os.path.exists(path):
        return None
    an = json.load(open(path, encoding="utf-8")).get("animations", {})
    return an.get(f"animation.{name}.idle") or next((v for k, v in an.items() if k.endswith(".idle")), None)


def views_of(name, before=True, W=300, full_only=False):
    """[images]: face, 3/4 face, torso, full front, full back (before = the backup copy)"""
    bk = os.path.join(BACKUP, name)
    gp = os.path.join(bk if before else GEO, name + ".geo.json")
    doc = json.load(open(gp, encoding="utf-8"))
    tex = SF.load_png(os.path.join(bk if before else ENT, name + ".png"))
    anim = _idle(os.path.join(bk if before else ANIM, name + ".animation.json"), name)
    g = doc["minecraft:geometry"][0]
    head = next(b for b in g["bones"] if b["name"] == "head")
    hc = max((c for c in head.get("cubes", []) if not c.get("inflate")), key=lambda c: c["size"][0] * c["size"][2])
    hy = hc["origin"][1] + 4
    lows = [c["origin"][1] for b in g["bones"] for c in b.get("cubes", [])]
    highs = [c["origin"][1] + c["size"][1] for b in g["bones"] for c in b.get("cubes", [])]
    lo, hi = min(lows), max(highs)
    full = W * 0.9 / max(8, hi - lo)
    mid = (0, (lo + hi) / 2)
    bg = (40, 44, 46, 255)
    vs = [(0, 0, full, mid), (150, 6, full, mid)] if full_only else \
        [(0, 0, W / 11, (0, hy)), (-35, 8, W / 11, (0, hy)), (0, 0, W / 13, (0, hy - 8)), (-40, 6, W / 13, (0, hy - 8)),
         (0, 0, full, mid), (150, 6, full, mid)]
    return [render_lit(doc, tex, anim, 0, yaw, pitch, W, W, s, bg, c) for yaw, pitch, s, c in vs]


def compare(names, path, W=300, full_only=False):
    rows = []
    for n in names:
        rows.append(views_of(n, True, W, full_only))
        rows.append(views_of(n, False, W, full_only))
    cols = len(rows[0])
    sheet = [[(40, 44, 46, 255)] * (W * cols) for _ in range(W * len(rows))]
    for r, row in enumerate(rows):
        for c, im in enumerate(row):
            for y in range(W):
                sheet[r * W + y][c * W:(c + 1) * W] = im[y]
    H.write_png(path, sheet)
    print("compare ->", path, "(pairs of rows: before, after)")


# ----------------------------------------------------------------------------- main
def main():
    args = sys.argv[1:]
    dry = "--dry" in args
    jobs = int(args[args.index("--jobs") + 1]) if "--jobs" in args else 1
    cmp = args[args.index("--compare") + 1] if "--compare" in args else None
    skip = {cmp, str(jobs)}
    names = [a for a in args if not a.startswith("--") and a not in skip]
    if "--merge" in args:
        merge()
        return
    if not names and (cmp is None or "--all" in args):
        names = model_names()
    if jobs > 1 and len(names) > 1:
        procs = []
        for i in range(jobs):
            part = names[i::jobs]
            procs.append(subprocess.Popen([sys.executable, os.path.abspath(__file__)] + part + (["--dry"] if dry else [])))
        codes = [p.wait() for p in procs]
        merge()
        if any(codes):
            raise SystemExit(f"worker exit codes {codes}")
        return
    failed = []
    for n in names:
        try:
            process(n, dry)
        except Exception as e:                              # report, keep going: a broken one stays as it was
            import traceback
            traceback.print_exc()
            failed.append(n)
            print(f"{n}: FAILED {e!r}")
    if not dry and jobs == 1 and "--no-merge" not in args and len(names) == len(model_names()):
        merge()
    if failed:
        print("failed:", " ".join(failed))
    if cmp:
        compare([n for n in names if n not in failed][:6], cmp)


if __name__ == "__main__":
    main()
