#!/usr/bin/env python3
"""Turns harvested CC0 clips into mod sounds and points the harpy and amphibian sound events at them.

    uv run --with soundfile --with numpy --with scipy python tools/sound_import.py <harvest_dir>

Clips (from tools/freesound_harvest.py) are cut to the loud part, made mono 44.1 kHz, peak-levelled,
faded out and written as Ogg Vorbis to assets/harmontide/sounds/shared/<group>/<n>.ogg. One shared pool
per voice group; species get their own voice through pitch. Then every harpy's and amphibian's ambient / hurt / death /
eat event is rewritten (events that do not exist are left alone, and so are the species' special sounds
such as sing or hiss). SOUND_CREDITS.md lists every clip's source. Re-run is safe: it rebuilds from scratch.
"""
import html
import json
import os
import re
import shutil
import sys

import numpy as np
import soundfile as sf
from scipy.signal import resample_poly

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, "src", "main", "resources", "assets", "harmontide")
SOUNDS_JSON = os.path.join(ASSETS, "sounds.json")
SHARED = os.path.join(ASSETS, "sounds", "shared")
CREDITS = os.path.join(REPO, "SOUND_CREDITS.md")
RATE = 44100

# group -> (max seconds kept, peak level)
SHAPE = {"girl_hurt": (1.0, 0.8), "girl_death": (2.2, 0.8), "girl_giggle": (2.5, 0.7), "eat": (0.9, 0.6)}
DEFAULT_SHAPE = (2.5, 0.75)

# harpies: species -> (call group, call pitch, girl voice pitch). Big birds lower, small birds higher.
HARPIES = {
    "sparrow_harpy": ("songbird", 1.15, 1.15), "finch_harpy": ("songbird", 1.25, 1.2),
    "alkonost_harpy": ("songbird", 0.9, 1.0), "motmora_harpy": ("songbird", 0.8, 1.0),
    "riflebird_harpy": ("songbird", 1.0, 1.05), "hummingbird_harpy": ("songbird", 1.45, 1.25),
    "crow_harpy": ("corvid", 1.0, 1.0), "skycrow_harpy": ("corvid", 1.1, 1.05),
    "rookmourn_harpy": ("corvid", 0.85, 0.9), "trickster_raven_harpy": ("corvid", 0.9, 1.05),
    "yatagarasu_harpy": ("corvid", 0.8, 0.95), "coin_magpie_harpy": ("corvid", 1.2, 1.1),
    "owl_harpy": ("owl", 1.0, 0.95), "lechuza_harpy": ("owl", 1.1, 1.05),
    "stigowl_harpy": ("owl", 0.75, 0.85), "argentowlis_harpy": ("owl", 0.9, 1.0),
    "roc_harpy": ("raptor", 0.7, 0.85), "garuda_harpy": ("raptor", 0.85, 0.95),
    "condorune_harpy": ("raptor", 0.75, 0.9), "ironbeak_harpy": ("raptor", 0.9, 0.95),
    "ziz_harpy": ("raptor", 0.65, 0.85), "landharpy_harpy": ("raptor", 1.0, 1.0),
    "phoenix_harpy": ("raptor", 1.1, 1.0), "bennu_harpy": ("raptor", 0.95, 1.0),
    "zhuque_harpy": ("raptor", 1.15, 1.05), "emberu_harpy": ("poultry", 1.1, 1.1),
    "seagull_harpy": ("seabird", 1.0, 1.05), "albatross_harpy": ("seabird", 0.75, 0.9),
    "pelican_harpy": ("seabird", 0.7, 0.9), "pumpkingull_harpy": ("seabird", 0.9, 1.0),
    "penguin_harpy": ("waterfowl", 0.75, 1.1),
    "duck_harpy": ("waterfowl", 1.1, 1.1), "swan_harpy": ("waterfowl", 0.8, 0.95),
    "mirror_crane_harpy": ("crane", 1.0, 1.0), "flamingo_harpy": ("crane", 1.1, 1.05),
    "ibiset_harpy": ("crane", 1.15, 1.1), "ibisking_harpy": ("crane", 0.9, 0.95),
    "macaw_harpy": ("parrot", 1.0, 1.05), "toucan_harpy": ("parrot", 0.9, 1.05),
    "peaflare_harpy": ("peacock", 1.0, 1.0),
    "poultry_harpy": ("poultry", 1.0, 1.05), "dodobold_harpy": ("poultry", 0.8, 0.95),
    "reliquary_dove_harpy": ("dove", 1.0, 1.0),
    "ostrich_harpy": ("ratite", 1.0, 0.95), "emu_harpy": ("ratite", 1.05, 1.0),
}
# amphibians: species -> (call group, call pitch, girl voice pitch, bellow group, bellow pitch).
# Small bright frogs high, big toads and bullfrogs low; the tongue flick follows the call pitch. CC0 bullfrog and toad
# recordings are scarce, so those pools borrow the plain croaks at a lower pitch.
ANURANS = {
    "bullywug": ("frog_croak", 0.75, 0.95, "bullfrog+frog_croak", 0.8),
    "bullfrog": ("bullfrog+frog_croak", 1.0, 0.95, "bullfrog+frog_croak", 0.8),
    "tree_frog": ("frog_chirp", 1.0, 1.1, "frog_croak", 1.0),
    "poison_dart_frog": ("frog_chirp", 1.2, 1.2, "frog_chirp", 0.9),
    "glass_frog": ("frog_chirp", 1.35, 1.2, "frog_chirp", 1.0),
    "goliath_frog": ("bullfrog+frog_croak", 0.7, 0.85, "bullfrog+frog_croak", 0.55),
    "desert_rain_frog": ("frog_chirp", 1.5, 1.3, "frog_chirp", 1.2),
    "mucus_toad": ("toad+frog_croak", 0.85, 0.95, "toad+frog_croak", 0.65),
    "fire_bellied_toad": ("toad+frog_croak", 1.1, 1.05, "toad+frog_croak", 0.9),
    "cane_toad": ("toad+frog_croak", 0.8, 0.95, "bullfrog+frog_croak", 0.75),
    "natterjack": ("toad+frog_croak", 1.0, 1.0, "toad+frog_croak", 0.8),
    "surinam_toad": ("frog_croak", 0.8, 0.95, "toad+frog_croak", 0.7),
    "vodyanoy": ("bullfrog+frog_croak", 0.85, 0.9, "bullfrog+frog_croak", 0.65),
}
# the salamander side: (ambient group or None, ambient pitch, girl voice pitch)
CAUDATA = {
    "axolotl_mermaid": ("bubbles", 1.2, 1.15), "axolume_mermaid": ("bubbles", 1.0, 1.05),
    "olm_mermaid": ("bubbles", 0.85, 1.0),
    "salamander_lizardfolk": (None, 1.0, 1.0), "newt_lizardfolk": (None, 1.0, 1.1),
}
# extra one-off events that fit a shared group: event -> (group, pitch)
EXTRA = {"entity.crow_harpy.giggle": ("girl_giggle", 1.0), "entity.crow_harpy.laugh": ("girl_giggle", 0.95)}


def plans():
    """species -> {slot: (group, pitch)} for every species this tool voices."""
    out = {}
    for sp, (call, cp, vp) in HARPIES.items():
        out[sp] = {"ambient": (call, cp), "hurt": ("girl_hurt", vp), "death": ("girl_death", vp), "eat": ("eat", vp)}
    for sp, (call, cp, vp, bellow, bp) in ANURANS.items():
        out[sp] = {"ambient": (call, cp), "bellow": (bellow, bp), "tongue": ("tongue", cp), "hurt": ("girl_hurt", vp),
                   "death": ("girl_death", vp), "eat": ("eat", vp)}
    for sp, (amb, ap, vp) in CAUDATA.items():
        out[sp] = {"hurt": ("girl_hurt", vp), "death": ("girl_death", vp), "eat": ("eat", vp)}
        if amb:
            out[sp]["ambient"] = (amb, ap)
    return out
KEEP_OWN = {"entity.ostrich_harpy.ambient"}  # already a custom file


def process(src, dst, max_s, peak):
    data, sr = sf.read(src, always_2d=True)
    x = data.mean(axis=1)
    if sr != RATE:
        x = resample_poly(x, RATE, sr)
    # keep from the first loud sample, up to max_s, ending where it goes quiet
    env = np.abs(x)
    thresh = env.max() * 0.08
    loud = np.nonzero(env > thresh)[0]
    if len(loud) == 0:
        return False
    start = max(0, loud[0] - int(0.01 * RATE))
    end = min(len(x), loud[-1] + int(0.08 * RATE), start + int(max_s * RATE))
    x = x[start:end]
    x = x - x.mean()
    fade = min(len(x) // 4, int(0.06 * RATE))
    if fade > 0:
        x[-fade:] *= np.linspace(1, 0, fade)
        x[:32] *= np.linspace(0, 1, 32)
    x = x / (np.abs(x).max() or 1) * peak
    sf.write(dst, x.astype(np.float32), RATE, format="OGG", subtype="VORBIS")
    return True


def entries(pool, group, pitch):
    """Every clip of a group ("a+b" joins pools, for groups with only a few clips) at one pitch."""
    return [{"name": f"harmontide:shared/{g}/{n}", "pitch": round(pitch, 2)}
            for g in group.split("+") for n in pool.get(g, [])]


def main():
    harvest = sys.argv[1]
    manifest = json.load(open(os.path.join(harvest, "manifest.json"), encoding="utf-8"))
    shutil.rmtree(SHARED, ignore_errors=True)
    pool, credits = {}, []
    for group, clips in manifest.items():
        os.makedirs(os.path.join(SHARED, group), exist_ok=True)
        max_s, peak = SHAPE.get(group, DEFAULT_SHAPE)
        for c in clips:
            n = str(len(pool.get(group, [])) + 1)
            if process(os.path.join(harvest, c["file"]), os.path.join(SHARED, group, n + ".ogg"), max_s, peak):
                pool.setdefault(group, []).append(n)
                credits.append((f"shared/{group}/{n}.ogg", c))

    data = json.load(open(SOUNDS_JSON, encoding="utf-8"))
    changed = 0
    for sp, plan in plans().items():
        for slot, (group, pitch) in plan.items():
            sounds = entries(pool, group, pitch)
            ev = f"entity.{sp}.{slot}"
            if ev in data and ev not in KEEP_OWN and sounds:
                data[ev]["sounds"] = sounds
                changed += 1
    for ev, (group, p) in EXTRA.items():
        if ev in data and pool.get(group):
            data[ev]["sounds"] = entries(pool, group, p)
            changed += 1
    json.dump(data, open(SOUNDS_JSON, "w", encoding="utf-8", newline="\n"), indent=2, ensure_ascii=False)
    open(SOUNDS_JSON, "a", encoding="utf-8", newline="\n").write("\n")

    with open(CREDITS, "w", encoding="utf-8", newline="\n") as f:
        f.write("# Sound credits\n\nGENERATED by `tools/sound_import.py`. Every clip below is CC0 1.0 (public domain) from "
                "Freesound; credit is not required but given anyway. Clips were trimmed, made mono and levelled.\n\n"
                "| File | Source | Author |\n|---|---|---|\n")
        for path, c in credits:
            title = html.unescape(c["title"]).replace("|", "/")
            f.write(f"| `{path}` | [{title}]({c['url']}) | {c['author']} |\n")
    print("groups", {g: len(v) for g, v in pool.items()}, "events changed", changed)


if __name__ == "__main__":
    main()
