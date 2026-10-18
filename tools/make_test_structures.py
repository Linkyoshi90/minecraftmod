#!/usr/bin/env python3
"""Writes the structure templates used by the mod's GameTests (src/main/resources/data/monstergirls/structure/).

    python tools/make_test_structures.py

platform: 15x8x15, a grass floor at y=0 and air above. Tests build whatever else they need with
helper.setBlock, so this one template covers them all.
"""
import gzip
import os
import struct

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(REPO, "src", "main", "resources", "data", "monstergirls", "structure")
DATA_VERSION = 3955  # Minecraft 1.21.1

TAG_END, TAG_INT, TAG_STRING, TAG_LIST, TAG_COMPOUND = 0, 3, 8, 9, 10


def _name(s):
    b = s.encode("utf-8")
    return struct.pack(">H", len(b)) + b


def _payload(tag, value):
    if tag == TAG_INT:
        return struct.pack(">i", value)
    if tag == TAG_STRING:
        return _name(value)
    if tag == TAG_LIST:
        elem, items = value
        return struct.pack(">bi", elem if items else TAG_END, len(items)) + b"".join(_payload(elem, v) for v in items)
    if tag == TAG_COMPOUND:
        return b"".join(struct.pack(">b", t) + _name(k) + _payload(t, v) for k, (t, v) in value.items()) + b"\x00"
    raise ValueError(tag)


def compound(**kv):
    return (TAG_COMPOUND, kv)


def write_structure(name, size, block_at):
    """block_at(x, y, z) -> block id string"""
    palette, blocks = [], []
    for y in range(size[1]):
        for z in range(size[2]):
            for x in range(size[0]):
                block = block_at(x, y, z)
                if block not in palette:
                    palette.append(block)
                blocks.append({"pos": (TAG_LIST, (TAG_INT, [x, y, z])), "state": (TAG_INT, palette.index(block))})
    root = {
        "DataVersion": (TAG_INT, DATA_VERSION),
        "size": (TAG_LIST, (TAG_INT, list(size))),
        "palette": (TAG_LIST, (TAG_COMPOUND, [{"Name": (TAG_STRING, b)} for b in palette])),
        "blocks": (TAG_LIST, (TAG_COMPOUND, blocks)),
        "entities": (TAG_LIST, (TAG_COMPOUND, [])),
    }
    data = struct.pack(">b", TAG_COMPOUND) + _name("") + _payload(TAG_COMPOUND, root)
    os.makedirs(OUT, exist_ok=True)
    path = os.path.join(OUT, name + ".nbt")
    with gzip.GzipFile(path, "wb", mtime=0) as fh:
        fh.write(data)
    print(name, "->", path)


if __name__ == "__main__":
    write_structure("platform", (15, 8, 15), lambda x, y, z: "minecraft:grass_block" if y == 0 else "minecraft:air")
