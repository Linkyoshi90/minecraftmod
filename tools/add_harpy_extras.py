"""Adds what the relationship features need to every adult harpy model, without touching the gen scripts:

* a "belly" bone (child of body): a copy of the midriff cube, pivoted at its back face. It is hidden in game
  until she is expecting; then the renderer scales it outward (forward) as the gestation goes on.
* a "sit" animation (legs folded under her, like the ostrich's brood pose): ordered to sit with a stick.
* a "lie" animation (on her back, eyes closed): sharing a bed.

Idempotent: re-run it after regenerating any harpy model (gen_<species>_harpy.py overwrites these files).
    python tools/add_harpy_extras.py
"""
import glob
import json
import os

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "harmontide")
MIDRIFF = [3.5, 2.75, 2.5]


def load(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def save(path, doc):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(doc, f, indent=2)
        f.write("\n")


def add_belly(geo_path):
    doc = load(geo_path)
    geo = doc["minecraft:geometry"][0]
    bones = geo["bones"]
    if any(b["name"] == "belly" for b in bones):
        return "has belly"
    body = next(b for b in bones if b["name"] == "body")
    cubes = body.get("cubes", [])
    mid = next((c for c in cubes if [float(v) for v in c["size"]] == MIDRIFF), None)
    if mid is None:   # a species with its own torso proportions: the second-lowest unrotated cube is the midriff
        plain = sorted((c for c in cubes if "rotation" not in c), key=lambda c: c["origin"][1])
        mid = plain[1]
    ox, oy, oz = mid["origin"]
    sx, sy, sz = mid["size"]
    cube = {k: v for k, v in mid.items() if k not in ("pivot", "rotation")}
    belly = {"name": "belly", "parent": "body", "pivot": [ox + sx / 2, oy + sy / 2, oz + sz], "cubes": [cube]}
    bones.insert(bones.index(body) + 1, belly)
    save(geo_path, doc)
    return "belly added"


def hold(v):
    return {"0.0": v, "1.0": v}


def leg_pivot_y(species):
    geo = load(os.path.join(ROOT, "geo", "entity", species + ".geo.json"))["minecraft:geometry"][0]
    return next(b for b in geo["bones"] if b["name"] == "leg_r")["pivot"][1]


def sit_anim(drop):
    return {
        "loop": True,
        "animation_length": 3.0,
        "bones": {
            "body": {"position": hold([0, -drop, 0]), "rotation": hold([4, 0, 0])},
            "leg_r": {"position": hold([0, -drop, 0]), "rotation": hold([-84, 0, 0])},
            "leg_l": {"position": hold([0, -drop, 0]), "rotation": hold([-84, 0, 0])},
            "shin_r": {"rotation": hold([150, 0, 0])},
            "shin_l": {"rotation": hold([150, 0, 0])},
            "foot_r": {"rotation": hold([-66, 0, 0])},
            "foot_l": {"rotation": hold([-66, 0, 0])},
            "head": {"rotation": {"0.0": [0, 0, 0], "1.0": [-3, -18, 0], "2.0": [-3, 18, 0], "3.0": [0, 0, 0]}},
            "arm_r": {"rotation": hold([10, 0, 4])},
            "arm_l": {"rotation": hold([10, 0, -4])},
            "eyelids": {"position": {"0.0": [0, 0, 0], "1.4": [0, 0, 0], "1.44": [0, 0, -0.4], "1.52": [0, 0, -0.4], "1.56": [0, 0, 0]}},
        },
    }


def lie_anim():
    # rotating the root -90 about X lays her on her back with her head towards +Z (behind her): the entity faces
    # the foot of the bed, so her head rests on the pillow
    return {
        "loop": True,
        "animation_length": 4.0,
        "bones": {
            "root": {"rotation": hold([-90, 0, 0]), "position": hold([0, 3, 0])},
            "body": {"scale": {"0.0": [1, 1, 1], "2.0": [1.03, 1, 1.03], "4.0": [1, 1, 1]}},
            "head": {"rotation": hold([-8, 0, 0])},
            "eyelids": {"position": hold([0, 0, -0.4])},
            "arm_r": {"rotation": hold([-30, 0, -10])},
            "arm_l": {"rotation": hold([-30, 0, 10])},
            "wing_r": {"rotation": hold([0, 0, 12])},
            "wing_l": {"rotation": hold([0, 0, -12])},
        },
    }


def add_poses(species):
    path = os.path.join(ROOT, "animations", "entity", species + ".animation.json")
    doc = load(path)
    anims = doc["animations"]
    added = []
    key = "animation.%s.sit" % species
    if key not in anims:
        anims[key] = sit_anim(round(leg_pivot_y(species) * 0.53, 2))
        added.append("sit")
    key = "animation.%s.lie" % species
    if key not in anims:
        anims[key] = lie_anim()
        added.append("lie")
    if added:
        save(path, doc)
    return added


def main():
    for geo_path in sorted(glob.glob(os.path.join(ROOT, "geo", "entity", "*_harpy*.geo.json"))):
        name = os.path.basename(geo_path)[: -len(".geo.json")]
        if name.endswith("_hatchling"):
            continue
        print(name, add_belly(geo_path), end=" ")
        if os.path.exists(os.path.join(ROOT, "animations", "entity", name + ".animation.json")):
            print(add_poses(name))
        else:
            print("(shares its animations)")


if __name__ == "__main__":
    main()
