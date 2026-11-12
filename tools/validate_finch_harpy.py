#!/usr/bin/env python3
"""Structural checks for the generated Finch Harpy assets."""
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "harmontide"
geo_path = ASSETS / "geo" / "entity" / "finch_harpy.geo.json"
anim_path = ASSETS / "animations" / "entity" / "finch_harpy.animation.json"
tex_path = ASSETS / "textures" / "entity" / "finch_harpy.png"
geo = json.loads(geo_path.read_text(encoding="utf-8"))["minecraft:geometry"][0]
anims = json.loads(anim_path.read_text(encoding="utf-8"))["animations"]
bones = geo["bones"]
names = [b["name"] for b in bones]
assert len(names) == len(set(names)), "duplicate bone names"
name_set = set(names)
assert all(b.get("parent") in name_set for b in bones if b.get("parent")), "missing parent"
required = {"root", "body", "head", "eyelids", "mouth", "hair_back", "ahoge", "skirt", "tail"}
for side in ("r", "l"):
    required |= {f"{part}_{side}" for part in ("hair_side", "arm", "forearm", "hand", "wing", "wing_tip", "leg", "shin", "foot")}
assert required <= name_set, f"missing bones: {sorted(required - name_set)}"
for name in names:
    seen = set()
    current = name
    while current:
        assert current not in seen, f"bone cycle at {name}"
        seen.add(current)
        current = next(b.get("parent") for b in bones if b["name"] == current)
width, height = geo["description"]["texture_width"], geo["description"]["texture_height"]
cube_count = 0
for bone in bones:
    for cube in bone.get("cubes", []):
        cube_count += 1
        assert all(v > 0 for v in cube["size"]), "non-positive cube size"
        for face, uv in cube["uv"].items():
            x, y = uv["uv"]
            w, h = uv["uv_size"]
            assert x >= 0 and y >= 0 and w > 0 and h > 0, "invalid UV"
            assert x + w <= width and y + h <= height, f"out-of-bounds UV on {bone['name']}"
expected = {f"animation.finch_harpy.{key}" for key in ("idle", "walk", "fly", "sing", "bathe", "player_sleep_bounce")}
assert set(anims) == expected, "unexpected animation set"
for key, anim in anims.items():
    assert set(anim["bones"]) <= name_set, f"unknown animated bone in {key}"
with Image.open(tex_path) as tex:
    assert tex.size == (width, height), "texture dimensions differ from geometry"
    assert tex.mode == "RGBA", "texture must preserve feather cutout alpha"
    assert tex.getbbox(), "texture is empty"
print(f"OK: {len(bones)} bones, {cube_count} cubes, {len(anims)} animations, {width}x{height} RGBA texture; UVs and bone references valid")
