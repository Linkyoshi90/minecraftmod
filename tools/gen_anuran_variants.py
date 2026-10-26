#!/usr/bin/env python3
"""Colour variants for every frog and toad species that had none (the bullywug's and the cane toad's are written by
their own scripts). Texture only, like all variants: each variant re-tints the species' palette ramps (back, belly,
throat sac, eyes, hair...) towards a target colour while keeping each ramp's shading, then repaints the texture with
the species' own painter. Outfits and props keep their colours.

    python tools/gen_anuran_variants.py [species ...] [--lineup]

Writes textures/entity/<species>_<variant>.png (the fire-bellied toad also gets a glow mask per variant), and with
--lineup previews/<species>_variants.png. The Java side lists the same names in entity/AnuranVariants.java.
"""
import colorsys
import os
import shutil
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import harpy_lib as H  # noqa: E402
import variant_tools as VT  # noqa: E402

# species -> (module, default variant name, {variant: {palette attribute: target colour}})
SPECIES = {
    "bullfrog": ("gen_bullfrog", "olive", {
        "golden": dict(BACK="#c89a2a", BELLY="#f2e2a6", SAC="#f2c23e", HAIR="#9a5a1e", DRUM="#a07a22", PAD="#d8b860"),
        "blue": dict(BACK="#3a86b4", BELLY="#cfe2ec", SAC="#a6cce6", HAIR="#34386e", DRUM="#2a5a7a", PAD="#7ab0cc"),
        "dark": dict(BACK="#3a2c1a", BELLY="#b8a684", SAC="#c8b070", HAIR="#1c140c", DRUM="#2c2214"),
        "albino": dict(BACK="#f0c6bc", BELLY="#fff2ee", SAC="#f8dad4", EYE="#d02c3a", HAIR="#f6eeee", DRUM="#e8b4ac", PAD="#f4d2cc")}),
    "tree_frog": ("gen_tree_frog", "green", {
        "grey": dict(BACK="#8a8c86", BELLY="#e8e6dc", HAIR="#5a5e68", SAC="#dcdad0"),
        "lime": dict(BACK="#a8e040", BELLY="#f2f8d0", HAIR="#e0a020", EYE="#f0a020"),
        "blue": dict(BACK="#3a9ad0", BELLY="#e0f0f8", HAIR="#2a3a8a", SAC="#c8e4f4"),
        "brown": dict(BACK="#8a6a40", BELLY="#ece0c8", HAIR="#3a7a3a", EYE="#c8a030")}),
    "poison_dart_frog": ("gen_poison_dart_frog", "azure", {
        "strawberry": dict(BACK="#d82a22", BELLY="#e8483a", SAC="#f0907a", HAIR="#2a3a8a"),
        "golden": dict(BACK="#f0c020", BELLY="#f6d850", SAC="#fae69a", HAIR="#3a2a1a"),
        "green": dict(BACK="#2aa84a", BELLY="#4ac06a", SAC="#9ae0b0", HAIR="#14301e"),
        "orange": dict(BACK="#f07a1a", BELLY="#f69a3a", SAC="#fac090", HAIR="#3a1a10")}),
    "glass_frog": ("gen_glass_frog", "lime", {
        "emerald": dict(BACK="#3ab46a", BELLY="#d8f4e0", HAIR="#1e7a5a"),
        "yellow": dict(BACK="#d8d870", BELLY="#f8f8dc", HAIR="#a0a040", EYE="#c8a0f0"),
        "blue": dict(BACK="#90c8d8", BELLY="#e4f4f8", HAIR="#4a8aa8"),
        "spotted": dict(BACK="#b8d8a0", BELLY="#f2f8ea", HAIR="#7aa060", EYE="#e0e0e0")}),
    "goliath_frog": ("gen_goliath_frog", "olive", {
        "dark": dict(BACK="#2a2e18", BELLY="#a8824a", HAIR="#1e3a1e"),
        "green": dict(BACK="#3a7a2a", BELLY="#d8b060", HAIR="#2a5a2a"),
        "rust": dict(BACK="#7a4a22", BELLY="#d8a050", HAIR="#5a2a14"),
        "grey": dict(BACK="#5a5c56", BELLY="#c0b8a0", HAIR="#3a3c38")}),
    "desert_rain_frog": ("gen_rain_frog", "sand", {
        "pale": dict(BACK="#e8d0b0", BELLY="#fbf0e2", HAIR="#f0d8b0"),
        "grey": dict(BACK="#a8a098", BELLY="#e6e0d8", HAIR="#c8c0b4", SAC="#e0d4cc"),
        "rufous": dict(BACK="#c0743e", BELLY="#f2c8a0", HAIR="#a04a22"),
        "dark": dict(BACK="#6a4a30", BELLY="#d0b090", HAIR="#3a2618")}),
    "mucus_toad": ("gen_mucus_toad", "brown", {
        "olive": dict(BACK="#5a6a2a", BELLY="#d8d4a0", HAIR="#3a4a1e"),
        "red": dict(BACK="#8a3a22", BELLY="#e8c8a8", HAIR="#5a1e14", SAC="#f0a888"),
        "grey": dict(BACK="#6a6862", BELLY="#d8d4cc", HAIR="#4a4a50"),
        "blue": dict(BACK="#3a5a7a", BELLY="#d0dce4", HAIR="#2a3a5a", SAC="#a8c4dc")}),
    "fire_bellied_toad": ("gen_fire_bellied_toad", "oriental", {
        "yellow": dict(BACK="#5a5848", BELLY="#f0c020", SAC="#f6d04a", HAIR="#d8a020"),
        "european": dict(BACK="#3a3a34", BELLY="#e86a18", SAC="#f08a30", HAIR="#c84a18"),
        "giant": dict(BACK="#6a5a2a", BELLY="#d83a24", HAIR="#a02a18"),
        "blue": dict(BACK="#2a5a8a", BELLY="#e84a1e", HAIR="#2a3a6a")}),
    "vodyanoy": ("gen_vodyanoy", "river", {
        "bog": dict(BACK="#4a3a1e", BELLY="#a89070", HAIR="#3a5a1a"),
        "old": dict(BACK="#5a6a5a", BELLY="#c0bcac", HAIR="#a8b098"),
        "lake": dict(BACK="#2a5a5a", BELLY="#a8c0b8", HAIR="#2a7a6a"),
        "pale": dict(BACK="#a8b890", BELLY="#ece8d8", HAIR="#78a860", EYE="#c83a3a")}),
    "natterjack": ("gen_natterjack", "olive", {
        "green": dict(BACK="#3a8a3a", BELLY="#e6e0c0", HAIR="#4a3a2a"),
        "brown": dict(BACK="#7a5a36", BELLY="#e2d4b4", HAIR="#3a2a1a"),
        "red": dict(BACK="#8a4a32", BELLY="#ead8c0", HAIR="#5a2a1a", EYE="#c8b020"),
        "grey": dict(BACK="#6e6e64", BELLY="#e0dcd0", HAIR="#4a4a44")}),
    "surinam_toad": ("gen_surinam_toad", "brown", {
        "dark": dict(BACK="#3a2c1c", BELLY="#b8a888", HAIR="#2a4a28"),
        "olive": dict(BACK="#5a5a2e", BELLY="#d8d0a8", HAIR="#3a5a2a"),
        "pale": dict(BACK="#b0986e", BELLY="#f0e6cc", HAIR="#6a9a5a", SAC="#ece2c8"),
        "rust": dict(BACK="#7a3e22", BELLY="#e0c4a0", HAIR="#3a6a3a")}),
}


def _hls(c):
    return colorsys.rgb_to_hls(c[0] / 255.0, c[1] / 255.0, c[2] / 255.0)


def retint(rmp, target, light_mix=0.6):
    """the same ramp (same number of steps, same shading) moved to the target's hue and saturation; its lightness
    moves `light_mix` of the way to the target's"""
    t = tuple(int(target.lstrip("#")[i:i + 2], 16) for i in (0, 2, 4))
    th, tl, ts = _hls(t)
    mid = _hls(rmp[len(rmp) // 2])
    out = []
    for c in rmp:
        h, l_, s = _hls(c)
        nl = max(0.02, min(0.98, l_ + (tl - mid[1]) * light_mix))
        ns = max(0.0, min(1.0, ts * (s / mid[2] if mid[2] > 0.05 else 1.0)))
        r, g, b = colorsys.hls_to_rgb(th, nl, ns)
        out.append((int(round(r * 255)), int(round(g * 255)), int(round(b * 255))) + tuple(c[3:4] or (255,)))
    return H.ramp(out, getattr(rmp, "nodither", False))


def variant_pal(base, spec):
    import copy
    p = copy.copy(base)
    for attr, target in spec.items():
        cur = getattr(base, attr, None)
        if cur is None:
            continue
        setattr(p, attr, retint(cur, target))
    if "BACK" in spec and getattr(base, "stripe", None) is not None and "stripe" not in spec:
        p.stripe = retint(base.stripe, spec.get("BELLY", spec["BACK"]))
    return p


def run(sp, prev):
    modname, default, variants = SPECIES[sp]
    m = __import__(modname)
    m.declare_parts()
    H.pack()
    base = m.PAL

    def paint(v=None):
        m.PAL = v or base
        try:
            return m.paint()
        finally:
            m.PAL = base

    pals = {k: variant_pal(base, spec) for k, spec in variants.items()}
    build = getattr(m, "build_model", None)
    anims = getattr(m, "animations", None)
    sys.argv = sys.argv[:1] + (["--lineup", os.path.join(H.REPO, "previews", f"{sp}_variants.png")] if prev and build and anims else [])
    glow = None
    if sp == "fire_bellied_toad":
        mask = os.path.join(VT.tex_dir(), sp + "_glowmask.png")
        glow = lambda tex, suffix: suffix and shutil.copyfile(mask, os.path.join(VT.tex_dir(), f"{sp}{suffix}_glowmask.png"))
    VT.write_variants(m.NAME, default, paint, pals, build, anims,
                      rows=[("idle", 0, -20, 6, 11.5, None), ("idle", 0, -15, 0, 26, (0, 22))], glow=glow)
    return m


def main():
    want = [a for a in sys.argv[1:] if not a.startswith("--") and a in SPECIES]
    prev = "--lineup" in sys.argv
    for sp in SPECIES:
        if not want or sp in want:
            run(sp, prev)


if __name__ == "__main__":
    main()
