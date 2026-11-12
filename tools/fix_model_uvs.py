#!/usr/bin/env python3
"""Gives explicit UVs to model elements that reach outside the 0..16 block (2026-11-09).

Without a "uv", a face takes its UVs from the element's position; past 0..16 those land outside its sprite and the game draws
whatever sits next to it in the texture atlas (the umbrella showed warped vines and doors). Faces that already have a "uv" are left
alone; the rest get [0, 0, w, h] with w and h the face's size, at most 16. Safe to run again after any model generator.

    python tools/fix_model_uvs.py
"""
import glob
import json
import os

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
AXES = {"down": (0, 2), "up": (0, 2), "north": (0, 1), "south": (0, 1), "west": (2, 1), "east": (2, 1)}


def fix(doc):
    changed = False
    for el in doc.get("elements", []):
        if all(0 <= c <= 16 for c in el["from"] + el["to"]):
            continue
        size = [t - f for f, t in zip(el["from"], el["to"])]
        for face, spec in el["faces"].items():
            if "uv" in spec:
                continue
            u, v = AXES[face]
            spec["uv"] = [0, 0, min(16, size[u]), min(16, size[v])]
            changed = True
    return changed


def main():
    n = 0
    for path in glob.glob(os.path.join(REPO, "src", "main", "resources", "assets", "harmontide", "models", "**", "*.json"), recursive=True):
        with open(path, encoding="utf-8") as fh:
            doc = json.load(fh)
        if fix(doc):
            with open(path, "w", encoding="utf-8", newline="\n") as fh:
                json.dump(doc, fh, indent=2)
                fh.write("\n")
            n += 1
    print(f"uvs fixed in {n} models")


if __name__ == "__main__":
    main()
