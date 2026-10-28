#!/usr/bin/env python3
"""Minotaurs (the cattle family, see minotaur_field_guide.html): cattle, Minoan, bison, aurochs, water buffalo.

    python tools/gen_minotaur.py [species ...] [--previews]

The girl_family biped with hooves, cow ears, horns (bone `horns`) and a tufted cow tail. The only family with men in
it: every species also gets a bull, <species>_male (the "male" build: broad flat chest, no top, short hair, bigger
horns), with its own textures <species>_male[_<variant>].png. The bull's animation file uses the species' animation
names (animation.<species>.*), so the game plays the same animations on either model. The Minoan carries a labrys.
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, noise, skin, CLEAR  # noqa: E402

COW_TAIL = [(1, 1, 3.5), (.75, .75, 3.5), (1.5, 1.75, 1.75)]
TAIL_REST = [(-62, 0, 0), (-14, 0, 0), (-6, 0, 0)]

SPECIES = {
    # twentieth wave: the brasshorn ram, a shaggy blue mountain ram, great curling horns of brass, brass at her brow and hooves,
    # a white rune on her brow and a woven blanket
    "brasshorn_minotaur": dict(
        build="strong", outfit=dict(top=["halter"], bottom="sarong", extras=["bracers"], colors=dict(cloth2="#2a3a7a")),
        hair="shaggy", bangs="messy", ears="cow", legs="hoof", foot="hoof", horns="ram", tail=[(2, 2, 1.75)], tail_look="puff", tail_rest=[(-20, 0, 0)],
        wool=True, regalia=True, male=dict(hair="shaggy", outfit=dict(bottom="sarong", extras=["bracers"], colors=dict(cloth2="#2a3a7a"))),
        pal=dict(SKIN=skin("fair"), HAIR="#3a5ab8", FUR="#3a5ab8", LIGHT="#a8c0f0", MARK="#1a2a6a", EYE="#c8902a", CLOTH="#2a3a8a", TRIM="#d8b048",
                 INNER="#c8a0b0", pattern="solid", extra=dict(horn="#d8a838", hoof="#c8a040", brass="#d8b048", rune="#e8eef8", blanket="#2a3a8a",
                                                               weave="#d8b048", wool="#5a7ad0")),
        variants={"grey": dict(HAIR="#7a7a84", FUR="#7a7a84", LIGHT="#d0d0d8", MARK="#3a3a42",
                               extra=dict(horn="#c8a040", hoof="#3a3a42", brass="#c8a040", rune="#f0f0f4", blanket="#8a2a2a", weave="#e8d8b0", wool="#9a9aa4")),
                  "brown": dict(HAIR="#6a4a2e", FUR="#7a5636", LIGHT="#d8c0a0", MARK="#3a2414",
                                extra=dict(horn="#d8a838", hoof="#2a1a12", brass="#d8a838", rune="#f0e8d0", blanket="#2a5a3a", weave="#d8a838", wool="#9a7450")),
                  "white": dict(HAIR="#e8ecf4", FUR="#e8ecf4", LIGHT="#ffffff", MARK="#a8acb8", EYE="#3a6ac8",
                                extra=dict(horn="#c8ccd4", hoof="#5a5a64", brass="#c8ccd4", rune="#3a5ab8", blanket="#3a5ab8", weave="#e8ecf4", wool="#f4f6fa")),
                  "black": dict(HAIR="#26242a", FUR="#2a282e", LIGHT="#6a6870", MARK="#0e0c10", EYE="#e8c040",
                                extra=dict(horn="#d8a838", hoof="#141418", brass="#d8a838", rune="#e8c040", blanket="#5a1e24", weave="#d8a838", wool="#3a3840"))}),
    # nineteenth wave: the tuskin, a shaggy blue mammoth girl, white cheeks, great tusks bound with iron
    "tuskin_minotaur": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps", "belt", "bracers"], colors=dict(cloth2="#3a3a4a")),
        hair="shaggy", bangs="messy", ears="cow", legs="fur", foot="hoof", tail=[(1, 1, 3), (1.5, 1.5, 1.5)], tail_look="tuft",
        tail_rest=[(-15, 0, 0), (10, 0, 0)], wool=True, tusks=True,
        male=dict(hair="shaggy", outfit=dict(bottom="loincloth", extras=["arm_wraps", "belt", "bracers"], colors=dict(cloth2="#3a3a4a"))),
        pal=dict(SKIN=skin("fair"), HAIR="#2a4ab8", FUR="#2a48b0", LIGHT="#8aa8f0", MARK="#14246a", EYE="#c83a5a", CLOTH="#3a3a4a", TRIM="#8a8a9a",
                 INNER="#f0b0c0", pattern="solid", extra=dict(tusk="#f4ecd8", tusk_band="#5a5e6a", hoof="#1e1e28", wool="#3a5ac8")),
        variants={"brown": dict(HAIR="#6a4228", FUR="#7a4e2e", LIGHT="#c8a078", MARK="#3a2414", EYE="#2a6a3a",
                                extra=dict(tusk="#f4ecd8", tusk_band="#8a5a2a", hoof="#2a1a12", wool="#8a5a34")),
                  "grey": dict(HAIR="#6a6a74", FUR="#7a7a84", LIGHT="#c8c8d0", MARK="#3a3a42", EYE="#3a6ac8",
                               extra=dict(tusk="#f4ecd8", tusk_band="#3a3a42", hoof="#2a2a30", wool="#8a8a94")),
                  "white": dict(HAIR="#e8ecf4", FUR="#e0e4ec", LIGHT="#ffffff", MARK="#a8acb8", EYE="#3a8ad8",
                                extra=dict(tusk="#e8d8b0", tusk_band="#5a7ac0", hoof="#5a5a64", wool="#f4f6fa")),
                  "red": dict(HAIR="#a8402a", FUR="#b04a30", LIGHT="#f0b090", MARK="#5a1e12", EYE="#e8c040",
                              extra=dict(tusk="#f4ecd8", tusk_band="#2a2a30", hoof="#2a1610", wool="#c85a3a"))}),
    # eighteenth wave: the skjoldram, a shaggy teal ram in a steel brow plate, her curling horns red as rust
    "skjoldram_minotaur": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["pauldron", "arm_wraps", "belt"], colors=dict(cloth2="#5a3a3a")),
        hair="shaggy", bangs="messy", ears="cow", legs="fur", foot="hoof", horns="ram", tail=[(2, 2, 1.75)], tail_look="puff", tail_rest=[(-20, 0, 0)],
        wool=True, browplate=True, male=dict(hair="shaggy", outfit=dict(bottom="loincloth", extras=["pauldron", "arm_wraps", "belt"])),
        pal=dict(SKIN=skin("fair"), HAIR="#7ac8c0", FUR="#4a9a98", LIGHT="#c8f0ec", MARK="#2a6a6a", EYE="#c82a2a", CLOTH="#6a7a8a", TRIM="#a8b4c0",
                 INNER="#e0a8a8", pattern="solid", extra=dict(horn="#a83a32", hoof="#2a2a30", wool="#8ad4cc", plate="#a8b4c4")),
        variants={"black": dict(FUR="#2a2a30", HAIR="#3a3a42", LIGHT="#6a6a74", MARK="#141418", EYE="#e8c040",
                                extra=dict(horn="#8a7a6a", hoof="#141418", wool="#34343c", plate="#6a707c")),
                  "brown": dict(FUR="#7a5232", HAIR="#9a6a42", LIGHT="#d8b890", MARK="#4a2e18", EYE="#3a6a2a",
                                extra=dict(horn="#e8dcc0", hoof="#2a2018", wool="#a87a50", plate="#b88a4a")),
                  "purple": dict(FUR="#5a4a8a", HAIR="#9a84d0", LIGHT="#e0d8f8", MARK="#2e2458", EYE="#e8c040",
                                 extra=dict(horn="#e8c040", hoof="#1e1a2a", wool="#a890e0", plate="#c8ccd8")),
                  "red": dict(FUR="#9a3a2e", HAIR="#c8604a", LIGHT="#f8c8b8", MARK="#5a1a12", EYE="#3a8ad8",
                              extra=dict(horn="#3a6a8a", hoof="#2a1a14", wool="#d8705a", plate="#a8b4c4"))}),
    # seventeenth wave: the dustbison, a fluffy blue bison calf with cream-tipped fur and gold spots, a bell at her neck
    "dustbison_minotaur": dict(
        build="petite", outfit=dict(top=["bandeau", "mantle"], bottom="loincloth", extras=["bell_collar"], colors=dict(cloth2="#e8d0a8")),
        hair="mane", bangs="messy", ears="cow", legs="hoof", foot="hoof", horns="bull", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        male=dict(hair="mane", horns="bull", outfit=dict(top=["mantle"], bottom="loincloth", extras=["bell_collar"])),
        pal=dict(SKIN=skin("fair"), HAIR="#3a5ac8", FUR="#4a6ad8", LIGHT="#f0dcb8", MARK="#e8b878", EYE="#2a5ab0", CLOTH="#3a5ac8", TRIM="#e8c070",
                 INNER="#c8a8b8", pattern="spots", extra=dict(horn="#c8ccd4", hoof="#1e2a5a")),
        variants={"plains": dict(FUR="#5a3e28", HAIR="#3a2618", LIGHT="#c8a880", MARK="#8a6a4a", EYE="#2a1a10", CLOTH="#a87a4a", extra=dict(horn="#2a2420", hoof="#1e1a18")),
                  "white": dict(FUR="#ece6da", HAIR="#f4f0e6", LIGHT="#ffffff", MARK="#c8bca8", CLOTH="#4a6ad8", extra=dict(horn="#8a8478", hoof="#3a3430")),
                  "rose": dict(FUR="#d888a8", HAIR="#b8587a", LIGHT="#f8e0e8", MARK="#f8c8a0", CLOTH="#b8587a", extra=dict(horn="#e8e0d8", hoof="#5a2a3a")),
                  "dusk": dict(FUR="#4a3a6a", HAIR="#2a1e44", LIGHT="#b8a8d0", MARK="#e8b878", CLOTH="#2a1e44", extra=dict(horn="#c8ccd4", hoof="#1a1428"))}),
    # sixteenth wave: the reedbull, a shaggy lapis-blue marsh bull: gold-banded horns hung with tassels, a white rune on her brow,
    # a woven blanket over her back, a pendant and gold anklets
    "reedbull_minotaur": dict(
        build="curvy", outfit=dict(top=["halter"], bottom="sarong", colors=dict(cloth2="#2a3a7a")),
        hair="shaggy", bangs="wispy", ears="cow", legs="hoof", foot="hoof", horns="bull_big", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        regalia=True, male=dict(hair="shaggy", outfit=dict(bottom="sarong", colors=dict(cloth2="#2a3a7a"))),
        pal=dict(SKIN=skin("fair"), HAIR="#2a46b0", FUR="#2e4cc0", LIGHT="#6a88e0", MARK="#14246a", EYE="#3a2a1e", CLOTH="#2a3a8a", TRIM="#d8c08a",
                 INNER="#a8a0c8", extra=dict(horn="#5a8ae8", hoof="#2a3448", brass="#c8a040", rune="#e8eef8", blanket="#2a3a8a", weave="#d8c08a")),
        variants={"marsh": dict(FUR="#6a5038", HAIR="#5a4a30", LIGHT="#a8906a", MARK="#3a2a1a", EYE="#4a7a3a", CLOTH="#c8b880", TRIM="#4a7a3a",
                                extra=dict(horn="#3a3226", hoof="#2a2218", brass="#c8a040", rune="#f0e8d0", blanket="#4a7a3a", weave="#c8b880")),
                  "grey": dict(FUR="#7a7a78", HAIR="#4a4a48", LIGHT="#a8a8a4", MARK="#2a2a28",
                               extra=dict(horn="#d8d0c0", hoof="#2a2a28", brass="#c8a040", rune="#ffffff", blanket="#8a2a2a", weave="#e8d8b0")),
                  "black": dict(FUR="#2a2624", HAIR="#1a1816", LIGHT="#5a5450", MARK="#0a0808",
                                extra=dict(horn="#e8e0cc", hoof="#1a1816", brass="#c8a040", rune="#e8c040", blanket="#2a2a5a", weave="#c8a040")),
                  "autumn": dict(FUR="#8a5a30", HAIR="#a8682a", LIGHT="#d8a870", MARK="#4a2a14",
                                 extra=dict(horn="#3a3226", hoof="#2a2218", brass="#c8a040", rune="#f0e0c0", blanket="#6a2a1a", weave="#e8c040"))}),
    "cattle_minotaur": dict(
        build="curvy", outfit=dict(top=["halter"], bottom="wrap_skirt", extras=["bell_collar"], colors=dict(cloth2="#4a6aa8")),
        hair="wavy", bangs="swept", ears="cow", legs="hoof", foot="hoof", horns="bull", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        male=dict(hair="shaggy", horns="bull_big", outfit=dict(bottom="loincloth", extras=["bell_collar"])),
        pal=dict(SKIN=skin("fair"), HAIR="#f2eee6", FUR="#f4f2ee", LIGHT="#ffffff", MARK="#1e1c1c", EYE="#4a3020", CLOTH="#e8e2d0", TRIM="#e0b040",
                 INNER="#f0b0b0", pattern="patches", extra=dict(horn="#e8e0cc", hoof="#3a3430")),
        variants={"jersey": dict(FUR="#c8925a", HAIR="#a8723a", MARK="#8a5a2a", pattern="solid", LIGHT="#ecd0a8"),
                  "highland": dict(FUR="#c8682a", HAIR="#d07a3a", MARK="#9a4a1e", pattern="solid", LIGHT="#e8a870", extra=dict(horn="#f0e6cc")),
                  "angus": dict(FUR="#1e1c1c", HAIR="#141212", MARK="#0a0808", pattern="solid", LIGHT="#3a3636"),
                  "hereford": dict(FUR="#8a3a22", HAIR="#f2eee6", MARK="#f2eee6", pattern="patches", LIGHT="#f8f4ee"),
                  # homage (2026-10-28): Miltank (Pokemon): pink with a cream belly, blue eyes
                  "pink_dairy": dict(FUR="#f0a0bc", HAIR="#f8c8d8", MARK="#f0a0bc", pattern="solid", LIGHT="#fff0dc", EYE="#3a6ac8", CLOTH="#fff0dc",
                                     INNER="#f8a0b8", extra=dict(horn="#f0e8d8", hoof="#2a2a30"))}),
    "minoan_minotaur": dict(
        build="strong", outfit=dict(top=["sports", "harness"], bottom="loincloth", extras=["pauldron", "bracers", "belt"], colors=dict(leather="#8a5a2a")),
        hair="braid", bangs="blunt", ears="cow", legs="hoof", foot="hoof", horns="bull_big", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        labrys=True, male=dict(hair="pixie", outfit=dict(bottom="loincloth", extras=["pauldron", "bracers", "belt"], colors=dict(leather="#8a5a2a"))),
        pal=dict(SKIN=skin("olive"), HAIR="#2a1a12", FUR="#4a2e1e", LIGHT="#8a6a50", MARK="#2a1a12", EYE="#c8902a", CLOTH="#a8282a", TRIM="#d8a840",
                 INNER="#c88a7a", extra=dict(horn="#e8dcc0", horn_tip="#c8a040", hoof="#2a2420")),
        variants={"crimson": dict(FUR="#6a2e22", HAIR="#3a1a14", CLOTH="#7a1a2a"),
                  "ivory": dict(FUR="#e8e0cc", HAIR="#f2ece0", LIGHT="#fffaf0", MARK="#b8a890", CLOTH="#3a5aa8"),
                  "obsidian": dict(FUR="#1a1618", HAIR="#0e0c0e", LIGHT="#3a3436", MARK="#0a0808", CLOTH="#5a2a6a"),
                  "gilded": dict(FUR="#8a6a3a", HAIR="#c8a050", LIGHT="#e8d0a0", CLOTH="#f0f0f0", TRIM="#f8d860")}),
    "bison_minotaur": dict(
        build="strong", outfit=dict(top=["bandeau", "mantle"], bottom="loincloth", extras=["arm_wraps"], colors=dict(cloth2="#6a4a2a")),
        hair="mane", bangs="messy", ears="cow", legs="hoof", foot="hoof", horns="bull", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        male=dict(hair="mane", horns="bull", outfit=dict(top=["mantle"], bottom="loincloth", extras=["arm_wraps"])),
        pal=dict(SKIN=skin("tan"), HAIR="#3a2618", FUR="#5a3e28", LIGHT="#8a6a4a", MARK="#2a1a10", EYE="#2a1a10", CLOTH="#a87a4a", TRIM="#c89a5a",
                 INNER="#a87a6a", extra=dict(horn="#2a2420", hoof="#1e1a18")),
        variants={"wood": dict(FUR="#3a2618", HAIR="#22160e", LIGHT="#5a4030"),
                  "white": dict(FUR="#ece6da", HAIR="#f4f0e6", LIGHT="#ffffff", MARK="#c8bca8", extra=dict(horn="#5a4a3a")),
                  "dark": dict(FUR="#2a1e16", HAIR="#140e0a", LIGHT="#4a3a2e", MARK="#0a0604"),
                  "golden": dict(FUR="#9a6a3a", HAIR="#6a4422", LIGHT="#c8a070")}),
    "aurochs_minotaur": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps", "belt"]),
        hair="ponytail", bangs="parted", ears="cow", legs="hoof", foot="hoof", horns="bull_big", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        male=dict(hair="shaggy", outfit=dict(bottom="loincloth", extras=["arm_wraps", "belt"])),
        pal=dict(SKIN=skin("peach"), HAIR="#1a1614", FUR="#1e1a18", LIGHT="#c8b8a0", MARK="#0e0c0a", EYE="#3a2a1a", CLOTH="#d8cfbc", TRIM="#a88a5a",
                 INNER="#a88a7a", pattern="solid", extra=dict(horn="#f0e6d0", horn_tip="#2a2420", hoof="#1a1614")),
        variants={"red": dict(FUR="#6a2e1a", HAIR="#4a1e10", LIGHT="#c8987a"),
                  "dun": dict(FUR="#a88a62", HAIR="#7a6040", LIGHT="#e0cca8"),
                  "grey": dict(FUR="#6a6862", HAIR="#4a4844", LIGHT="#c8c4bc"),
                  "primal": dict(FUR="#2a1e16", HAIR="#c88a4a", LIGHT="#d8a870", MARK="#c88a4a")}),
    "water_buffalo_minotaur": dict(
        build="athletic", outfit=dict(top=["bandeau"], bottom="sarong", extras=["satchel"], colors=dict(cloth2="#3a7a6a")),
        hair="sidetail", bangs="wispy", ears="cow", legs="hoof", foot="hoof", horns="buffalo", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        male=dict(hair="pixie", outfit=dict(bottom="sarong", extras=["satchel"], colors=dict(cloth2="#3a7a6a"))),
        pal=dict(SKIN=skin("tan"), HAIR="#2a2a2e", FUR="#4a4a50", LIGHT="#8a8a90", MARK="#2a2a2e", EYE="#2a2018", CLOTH="#e8b040", TRIM="#c87a2a",
                 INNER="#a88a8a", extra=dict(horn="#3a3632", hoof="#2a2624")),
        variants={"black": dict(FUR="#1e1e22", HAIR="#141418", LIGHT="#4a4a50"),
                  "albino": dict(FUR="#f0c8c0", HAIR="#f8e4e0", LIGHT="#fff4f0", MARK="#d8a0a0", extra=dict(horn="#e8d8c8")),
                  "mud": dict(FUR="#6a5640", HAIR="#4a3a28", LIGHT="#9a8668"),
                  "carabao": dict(FUR="#5a5048", HAIR="#3a342e", LIGHT="#8a8278")}),
    # ninth wave (2026-10-25)
    "sheep_minotaur": dict(
        build="petite", outfit=dict(top=["halter"], bottom="shorts", extras=["bell_collar"], colors=dict(cloth2="#7a9ac8")),
        hair="bob", bangs="blunt", ears="cow", legs="hoof", foot="hoof", horns="ram", tail=[(2, 2, 1.75)], tail_look="puff", tail_rest=[(-20, 0, 0)],
        wool=True, male=dict(hair="pixie", outfit=dict(bottom="loincloth", extras=["bell_collar"])),
        pal=dict(SKIN=skin("fair"), HAIR="#f4f0e6", FUR="#ece6da", LIGHT="#f8f4ec", MARK="#d8d0c0", EYE="#6a5a3a", CLOTH="#a8c8e8", TRIM="#e0b040",
                 INNER="#f0c0c0", pattern="solid", extra=dict(horn="#d8ccb0", hoof="#3a3430", wool="#f4f2ea")),
        variants={"black": dict(FUR="#3a3634", HAIR="#2a2624", LIGHT="#5a5450", MARK="#1e1a18", CLOTH="#e8c8a0",
                                extra=dict(horn="#c8bca0", hoof="#2a2420", wool="#2e2a28")),
                  "brown": dict(FUR="#7a5a3e", HAIR="#6a4a30", LIGHT="#a88a6a", MARK="#4a3420", CLOTH="#c8d8a0",
                                extra=dict(horn="#d8ccb0", hoof="#2a2420", wool="#8a6a4a")),
                  "jacob": dict(FUR="#f2eee6", HAIR="#2a2624", MARK="#2a2624", pattern="patches", CLOTH="#c86a5a",
                                extra=dict(horn="#e8e0cc", hoof="#2a2420", wool="#f4f2ea", wool_spots="#2e2a28")),
                  "merino": dict(FUR="#efe4cc", HAIR="#e8dcc0", LIGHT="#fbf4e4", CLOTH="#9a7ac8",
                                 extra=dict(horn="#e8dcbc", hoof="#3a3430", wool="#f0e6cc"))}),
    "highland_minotaur": dict(
        build="curvy", outfit=dict(top=["bandeau", "mantle"], bottom="wrap_skirt", extras=["scarf"], colors=dict(cloth2="#3a6a4a", scarf="#2a5a3a")),
        hair="shaggy", bangs="messy", ears="cow", legs="fur", foot="hoof", horns="bull_big", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        fringe=True, male=dict(hair="shaggy", outfit=dict(top=["mantle"], bottom="loincloth", extras=["scarf"])),
        pal=dict(SKIN=skin("peach"), HAIR="#c8622a", FUR="#c06a34", LIGHT="#e0a070", MARK="#8a4220", EYE="#3a2412", CLOTH="#c8282a", TRIM="#d8c8a0",
                 INNER="#d89a8a", pattern="solid", extra=dict(horn="#f0e6cc", horn_tip="#3a3028", hoof="#2a2420")),
        variants={"black": dict(FUR="#262220", HAIR="#1a1614", LIGHT="#4a4440", MARK="#0e0c0a", CLOTH="#e8c040"),
                  "dun": dict(FUR="#c8a060", HAIR="#d8b070", LIGHT="#ecd4a4", MARK="#9a7840", CLOTH="#3a6aa8"),
                  "white": dict(FUR="#ece4d2", HAIR="#f4ecdc", LIGHT="#fffaf0", MARK="#c8bca4", CLOTH="#8a3a5a"),
                  "brindle": dict(FUR="#6a4428", HAIR="#8a5a34", LIGHT="#a87a54", MARK="#3a2414", pattern="stripes", CLOTH="#4a7a3a")}),
    "yak_minotaur": dict(
        build="strong", outfit=dict(top=["sweater"], bottom="hakama", extras=["satchel"], colors=dict(cloth2="#8a2a2a")),
        hair="mane", bangs="parted", ears="cow", legs="fur", foot="hoof", horns="bull", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        yak=True, male=dict(hair="mane", outfit=dict(top=["mantle"], bottom="loincloth", extras=["satchel"])),
        pal=dict(SKIN=skin("tan"), HAIR="#2e2218", FUR="#3e2e22", LIGHT="#6a5442", MARK="#1e1610", EYE="#2a1a10", CLOTH="#d8a838", TRIM="#c83a2a",
                 INNER="#a88a7a", pattern="solid", extra=dict(horn="#e8dcc0", hoof="#1e1a18")),
        variants={"black": dict(FUR="#1e1c1c", HAIR="#141212", LIGHT="#3a3636", MARK="#0a0808", CLOTH="#3a8a9a"),
                  "white": dict(FUR="#ece8e0", HAIR="#f6f2ea", LIGHT="#ffffff", MARK="#c8c0b4", CLOTH="#c83a4a"),
                  "golden": dict(FUR="#a8783a", HAIR="#c8944a", LIGHT="#e0c08a", MARK="#7a5428", CLOTH="#4a5aa8"),
                  "pied": dict(FUR="#1e1c1c", HAIR="#f2eee6", MARK="#f2eee6", LIGHT="#f8f4ee", pattern="patches", CLOTH="#d8a838")}),
    # fifteenth wave: the mooskuh, a small calf grown all over with soft green moss (the sheep's fleece, in moss)
    "mooskuh_minotaur": dict(
        build="petite", outfit=dict(top=["halter"], bottom="shorts", extras=["bell_collar"], colors=dict(cloth2="#5a7a3a")),
        hair="wavy", bangs="blunt", ears="cow", legs="fur", foot="hoof", horns="bull", tail=[(2, 2, 1.75)], tail_look="puff", tail_rest=[(-20, 0, 0)],
        wool=True, male=dict(hair="shaggy", outfit=dict(bottom="loincloth", extras=["bell_collar"])),
        pal=dict(SKIN=skin("fair"), HAIR="#4a8a3a", FUR="#5a9a44", LIGHT="#c8e0a0", MARK="#2a5a22", EYE="#e8a030", CLOTH="#e8dcb0", TRIM="#e8c050",
                 INNER="#f0c0b0", pattern="solid", extra=dict(horn="#c8d098", hoof="#3a3424", wool="#6aa848")),
        variants={"lime": dict(HAIR="#7ab83a", FUR="#8ac84a", LIGHT="#e0f0b0", MARK="#4a7a22", extra=dict(horn="#e0e0a8", hoof="#3a3424", wool="#9ad84a")),
                  "russet": dict(HAIR="#8a5a2a", FUR="#9a6a34", LIGHT="#e0c8a0", MARK="#5a3a1a", extra=dict(horn="#e0d0a8", hoof="#2a2018", wool="#b07a3a")),
                  "lichen": dict(HAIR="#7a8a72", FUR="#8a9a82", LIGHT="#d8e0d0", MARK="#4a5a44", extra=dict(horn="#e0e0d0", hoof="#2a2a24", wool="#9aaa8a")),
                  "blue": dict(HAIR="#2a6a8a", FUR="#3a7a9a", LIGHT="#b8d8e8", MARK="#1a3a5a", extra=dict(horn="#d8e0e8", hoof="#1a2430", wool="#4a8ab0"))}),
    # fourteenth wave: the bronzejaw ox, a blue ox in brass: a bronze guard over her jaw and a yoke across her shoulders
    "bronzejaw_minotaur": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="hakama", extras=["armlets", "belt"], colors=dict(cloth2="#2a3a6a", leather="#8a6a2a")),
        hair="shaggy", bangs="messy", ears="cow", legs="fur", foot="hoof", horns="bull", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        bronze=True, male=dict(hair="shaggy", outfit=dict(bottom="loincloth", extras=["armlets", "belt"])),
        pal=dict(SKIN=skin("tan"), HAIR="#1e2a5a", FUR="#2a3a7a", LIGHT="#7a8ac8", MARK="#141e4a", EYE="#e8a030", CLOTH="#e8dcc0", TRIM="#c8902a",
                 INNER="#a88a7a", pattern="solid", extra=dict(horn="#e8dcc0", hoof="#1a1614", brass="#c8902a")),
        variants={"brown": dict(HAIR="#3a2416", FUR="#5a3a24", LIGHT="#8a6a50", MARK="#2a1a10", extra=dict(horn="#e8dcc0", hoof="#1a1614", brass="#c8902a")),
                  "black": dict(HAIR="#141212", FUR="#22201e", LIGHT="#4a4644", MARK="#0a0808", extra=dict(horn="#e8dcc0", hoof="#0e0c0c", brass="#d8a840")),
                  "grey": dict(HAIR="#4a4644", FUR="#6a6662", LIGHT="#a8a4a0", MARK="#2a2826", extra=dict(horn="#e8dcc0", hoof="#1a1614", brass="#a8acb4")),
                  "red": dict(HAIR="#5a1e12", FUR="#7a2e1e", LIGHT="#b0604a", MARK="#3a1208", extra=dict(horn="#e8dcc0", hoof="#1a1614", brass="#c8902a"))}),
    # tenth wave: the rhiwool, a woolly rhino (the sheep's shearable fleece in dark wool, a great horn on her brow)
    "rhiwool_minotaur": dict(
        build="strong", outfit=dict(top=["sarashi"], bottom="loincloth", extras=["arm_wraps", "belt"], colors=dict(cloth2="#5a4030")),
        hair="shaggy", bangs="messy", ears="cow", legs="fur", foot="hoof", tail=COW_TAIL, tail_look="tuft", tail_rest=TAIL_REST,
        wool=True, rhorn=True, male=dict(hair="shaggy", outfit=dict(bottom="loincloth", extras=["arm_wraps", "belt"])),
        pal=dict(SKIN=skin("tan"), HAIR="#1e1a18", FUR="#2a2422", LIGHT="#5a4e48", MARK="#120e0c", EYE="#8a2a1a", CLOTH="#c8b8a0", TRIM="#a8885a",
                 INNER="#a88a7a", pattern="solid", extra=dict(horn="#d8ccb0", hoof="#1a1614", wool="#2e2826")),
        variants={"brown": dict(FUR="#5a3a24", HAIR="#3a2416", LIGHT="#8a6a50", MARK="#2a1a10", extra=dict(horn="#d8ccb0", hoof="#1a1614", wool="#6a4a30")),
                  "grey": dict(FUR="#6a6662", HAIR="#4a4644", LIGHT="#a8a4a0", MARK="#2a2826", extra=dict(horn="#e8e0cc", hoof="#1a1614", wool="#7a7672")),
                  "white": dict(FUR="#e8e2d6", HAIR="#f2ece2", LIGHT="#ffffff", MARK="#b8b0a2", EYE="#4a6a8a",
                                extra=dict(horn="#c8b490", hoof="#2a2420", wool="#ece6da")),
                  "red": dict(FUR="#7a2e1e", HAIR="#5a1e12", LIGHT="#b0604a", MARK="#3a1208", extra=dict(horn="#e8dcc0", hoof="#1a1614", wool="#8a3424"))}),
}


def male(sp):
    """the bull's table entry: the cow's with the male build, short hair and his own outfit"""
    d = dict(SPECIES[sp])
    m = d["male"]
    d.update(build="male", hair=m.get("hair", "pixie"), bangs="messy", outfit=m["outfit"], horns=m.get("horns", d.get("horns")))
    d["pal"] = dict(d["pal"], CLOTH=d["pal"].get("extra", {}).get("cloth2", d["pal"]["CLOTH"]))
    return d


def declare_hook(sp, d):
    out = [("haft", .75, 13, .75), ("blade", 3, 3.5, .5)] if d.get("labrys") else []
    if d.get("wool"):                                   # the fleece: a shawl, a fluffy band at the hips and a cap (bones wool, wool_hips, wool_head)
        out += [("wool_c", 9, 2.75, 5), ("wool_s", 7.5, 2.5, 5.25), ("wool_h", 7, 1.75, 6.5)]
    if d.get("browplate"):                              # a steel plate over her brow, riveted, with a ridge down its middle
        out += [("brow_plate", 5, 3, .75), ("brow_ridge", 1, 3.5, 1)]
    if d.get("rhorn"):                                  # the rhiwool's horn: a big one rising from her brow, a little one above it
        out += [("rhorn_a", 2, 4.5, 2), ("rhorn_b", 1.25, 2.5, 1.25)]
    if d.get("fringe"):                                 # the highland's fringe, over her eyes
        out += [("fringe", 7.5, 3.25, .75)]
    if d.get("bronze"):                                 # the bronze jaw guard and the yoke
        out += [("bjaw", 5, 1.25, 1.5), ("yoke", 11, 1.25, 1.5)]
    if d.get("yak"):                                    # a long hair skirt; the pack chest (bone pack, shown once she carries one)
        out += [("yak_skirt", 7, 4.5, 5.25), ("pack", 5, 4, 3.25), ("pack_lid", 5.25, 1.25, 3.5)]
    if d.get("regalia"):                                # the reedbull's horn bands and tassels, brow rune, woven blanket, pendant and anklets
        out += [("hband", 1.75, 1.75, 1.75), ("htassel", .5, 2.5, .5), ("rune", 3, 2.5, .25), ("blanket", 7, 7, .5), ("blanket_s", .5, 4.5, 4),
                ("pendant", 1.75, 2, .25), ("anklet", 3.5, 1, 3.5)]
    if d.get("tusks"):                                  # the tuskin's tusks, from her cheeks, curving forward and up, an iron band on each
        out += [("tusk_a", 1.25, 1.25, 4.5), ("tusk_b", 1, 1, 4), ("tusk_band", 1.5, .5, 1.5)]
    return out


def wool_fn(pal):
    w = G.shades(pal.extra.get("wool", "#f4f2ea"))
    sp_ = G.shades(pal.extra.get("wool_spots", "#2e2a28"))

    def fn(f, x, y, w_, h):
        if pal.extra.get("wool_spots") and noise(x // 3, y // 3, 17) > 0.62:
            return pick(sp_, 0.5 + 0.15 * ((x + y) % 3 == 0), x, y)
        curl = ((x + 2 * (y % 2)) % 3 == 0) + 0.5 * ((x * 3 + y) % 5 == 0)
        return pick(w, 0.5 + 0.18 * curl + (noise(x, y, 23) - 0.5) * 0.12 - (0.14 if f == "down" else 0), x, y)
    return fn


def paint_hook(pt, pal, d):
    tusk, tband = G.shades(pal.extra.get("tusk", "#f4ecd8")), G.shades(pal.extra.get("tusk_band", "#5a5e6a"))
    pt("tusk_a", "all", lambda f, x, y, w, h: pick(tusk, 0.5 + 0.15 * (f == "up") - 0.1 * (f == "down"), x, y))
    pt("tusk_b", "all", lambda f, x, y, w, h: pick(tusk, 0.62 + 0.1 * (f == "up"), x, y))
    pt("tusk_band", "all", lambda f, x, y, w, h: pick(tband, 0.5 + 0.2 * (f == "up"), x, y))
    brass = G.shades(pal.extra.get("brass", "#c8902a"))
    rune, blanket, weave = G.shades(pal.extra.get("rune", "#e8eef8")), G.shades(pal.extra.get("blanket", "#2a3a8a")), G.shades(pal.extra.get("weave", "#d8c08a"))
    pt("hband", "all", lambda f, x, y, w, h: pick(brass, 0.5 + 0.25 * (y % 3 == 0), x, y))
    pt("htassel", "all", lambda f, x, y, w, h: pick(brass, 0.45 + 0.2 * (y == 0), x, y))
    pt("anklet", "all", lambda f, x, y, w, h: pick(brass, 0.5 + 0.25 * (x % 3 == 0), x, y))
    pt("pendant", "all", lambda f, x, y, w, h: pick(brass, 0.4, x, y) if x in (0, w - 1) or y in (0, h - 1) else pick(blanket, 0.6, x, y))

    def chevron(f, x, y, w, h):                         # a white chevron and a line above it
        if f != "north":
            return CLEAR
        c = w / 2 - 0.5
        return pick(rune, 0.8, x, y) if abs(abs(x - c) - (h - 1 - y)) < 0.8 or (y == 0 and abs(x - c) < 1) else CLEAR
    pt("rune", "all", chevron)

    def woven(f, x, y, w, h):                           # woven bands, a stepped diamond border
        band = y % 8 in (0, 1) or x in (0, 1, w - 2, w - 1)
        diamond = abs((x % 8) - 3.5) + abs((y % 8) - 3.5) < 2.2
        return pick(weave if band or diamond else blanket, 0.5 + 0.15 * ((x + y) % 2), x, y)
    pt("blanket", "all", woven)
    pt("blanket_s", "all", woven)
    pt("bjaw", "all", lambda f, x, y, w, h: pick(brass, 0.5 + 0.25 * (y == 0) + 0.15 * (x % 4 == 0), x, y))
    pt("yoke", "all", lambda f, x, y, w, h: pick(G.shades("#6a4a2a"), 0.45 + 0.15 * (x % 6 == 0), x, y) if 2 < x < w - 3
       else pick(brass, 0.6, x, y))
    for p in ("wool_c", "wool_s", "wool_h"):
        pt(p, "all", wool_fn(pal))
    plate = G.shades(pal.extra.get("plate", "#a8b4c4"))
    pt("brow_plate", "all", lambda f, x, y, w, h: pick(plate, 0.3, x, y) if f == "north" and (x, y) in ((0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1))
       else pick(plate, 0.5 + 0.2 * (y == 0) + (noise(x, y, 91) - 0.5) * 0.12, x, y))
    pt("brow_ridge", "all", lambda f, x, y, w, h: pick(plate, 0.65 + 0.15 * (f == "up"), x, y))
    horn = G.shades(pal.extra.get("horn", "#d8ccb0"))
    for p in ("rhorn_a", "rhorn_b"):
        pt(p, "all", lambda f, x, y, w, h: pick(horn, 0.5 + 0.2 * (y == 0) - 0.1 * (y > h * 0.7), x, y))
    hair = pal.HAIR
    pt("fringe", "all", lambda f, x, y, w, h: pick(hair, 0.45 + 0.25 * (x % 2) + (noise(x, y, 29) - 0.5) * 0.2 - 0.1 * (y > h - 2), x, y))
    pt("yak_skirt", "all", lambda f, x, y, w, h: pick(hair, 0.4 + 0.2 * (x % 2) + (noise(x, y // 2, 31) - 0.5) * 0.2, x, y)
       if f != "up" and (y < h - 1 or (x + 1) % 2) else (CLEAR if f != "up" else pick(hair, 0.4, x, y)))
    wood = G.shades("#8a5a2e")
    pt("pack", "all", lambda f, x, y, w, h: pick(G.shades("#c8a040"), 0.6, x, y) if f == "north" and abs(x + 0.5 - w / 2) < 0.6 and y < 1
       else pick(wood, 0.45 + 0.15 * (y % 2 == 0) - 0.15 * (x in (0, w - 1)), x, y))
    pt("pack_lid", "all", lambda f, x, y, w, h: pick(wood, 0.55 - 0.15 * (x in (0, w - 1)), x, y))
    bronze = G.shades("#b8803a")
    pt("haft", "all", lambda f, x, y, w, h: pick(G.shades("#5a3a22"), 0.45 + 0.15 * (y % 4 == 0), x, y))
    pt("blade", "all", lambda f, x, y, w, h: pick(bronze, 0.55 + 0.3 * (x == 0 or x == w - 1) + (noise(x, y, 5) - 0.5) * 0.1, x, y))


def build_hook(r, g, d):
    hy, z = g.hy, g.z
    if d.get("tusks"):
        cs = []
        for sg in (-1, 1):
            x = sg * 3.0
            cs += [G.rot_cube("tusk_a", [x - 0.625, hy - 0.4, -8.6 + z], [x, hy + 0.2, -4.1 + z], 15, -25 * sg, 0),
                   G.rot_cube("tusk_band", [x - 0.75, hy - 0.55, -6.0 + z], [x, hy + 0.2, -4.1 + z], 15, -25 * sg, 0),
                   G.rot_cube("tusk_b", [x - 0.5 + sg * 1.9, hy - 1.5, -11.6 + z], [x + sg * 1.9, hy - 1.0, -7.6 + z], -40, -40 * sg, 0)]
        r.add("tusks", "head", [0, hy + 1, -4 + z], cs)
    if d.get("regalia"):
        cd = G.S("chest")[2]
        r.put("head", cube("rune", [-1.5, hy + 4.6, -4.4 + z]))
        r.add("hair_hbands", "head", [0, hy + 7, z], [cube("hband", [sg * 5.9 - 0.875, hy + 5.9, -1.15 + z]) for sg in (-1, 1)]
              + [cube("htassel", [sg * 5.9 - 0.25, hy + 3.4, -0.5 + z]) for sg in (-1, 1)])
        r.add("blanket", "body", [0, 19, cd / 2 + z], [cube("blanket", [-3.5, 12.0, cd / 2 + 0.05 + z]),
                                                       cube("blanket_s", [-3.9, 14.5, -1.5 + z]), cube("blanket_s", [3.4, 14.5, -1.5 + z])])
        bd = G.S("bust")[2]
        r.put("body", cube("pendant", [-0.875, 15.2, -cd / 2 - bd - 0.3 + z]))
        for leg in ("shin_r", "shin_l"):
            if leg in r.b:
                px, py, pz = r.b[leg]["pivot"]
                r.put(leg, cube("anklet", [px - 1.75, 1.6, pz - 1.75]))
    if d.get("bronze"):
        r.put("head", cube("bjaw", [-2.5, hy - 0.1, -4.9 + z]))
        r.put("body", cube("yoke", [-5.5, 19.4, -0.75 + z]))
    if d.get("wool"):
        r.add("wool", "body", [0, 18, z], [cube("wool_c", [-4.5, 17.25, -2.5 + z])])
        r.add("wool_hips", "body", [0, 12, z], [cube("wool_s", [-3.75, 11.25, -2.625 + z])])
        r.add("wool_head", "head", [0, hy + 8, z], [cube("wool_h", [-3.5, hy + 7.4, -3.0 + z])])
    if d.get("fringe"):
        r.put("head", cube("fringe", [-3.75, hy + 2.6, -4.85 + z]))
    if d.get("browplate"):
        r.add("brow_plate", "head", [0, hy + 6, -4.5 + z], [cube("brow_plate", [-2.5, hy + 5.6, -5.4 + z]), cube("brow_ridge", [-0.5, hy + 5.6, -5.75 + z])])
    if d.get("rhorn"):
        r.add("rhorn", "head", [0, hy + 5.6, -5.0 + z], [G.rot_cube("rhorn_a", [-1, hy + 5.4, -6.2 + z], [0, hy + 5.6, -5.0 + z], -30, 0, 0),
                                                        G.rot_cube("rhorn_b", [-0.625, hy + 7.6, -4.6 + z], [0, hy + 7.8, -4.0 + z], -25, 0, 0)])
    if d.get("yak"):
        r.put("body", cube("yak_skirt", [-3.5, 8.75, -2.625 + z]))
        r.add("pack", "body", [0, 16, 1.6 + z], [cube("pack", [-2.5, 13.75, 1.75 + z]), cube("pack_lid", [-2.625, 17.6, 1.625 + z])])
    if not d.get("labrys"):
        return
    hx = -(G.S("chest")[0] / 2 + G.S("arm_up")[0] / 2)
    y = 19.5 - 9.0
    r.put("hand_r", cube("haft", [hx - 0.375, y - 7, -0.6]), cube("blade", [hx - 3.4, y + 2.5, -0.85]), cube("blade", [hx + 0.4, y + 2.5, -0.85]))


def anim_extra(an, sp, d):
    """the charge: head down, horns first"""
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("charge", 0.6, True, merge({
        "body": {"rotation": hold(28, 0, 0)}, "head": {"rotation": hold(16, 0, 0)},
        "leg_r": {"rotation": Tk((0, (50, 0, 0)), (0.3, (-50, 0, 0)), (0.6, (50, 0, 0)))},
        "leg_l": {"rotation": Tk((0, (-50, 0, 0)), (0.3, (50, 0, 0)), (0.6, (-50, 0, 0)))},
        "shin_r": {"rotation": Tk((0, (10, 0, 0)), (0.15, (60, 0, 0)), (0.3, (10, 0, 0)), (0.6, (10, 0, 0)))},
        "shin_l": {"rotation": Tk((0, (10, 0, 0)), (0.3, (10, 0, 0)), (0.45, (60, 0, 0)), (0.6, (10, 0, 0)))},
        "hair_back": {"rotation": hold(30, 0, 0)},
    }, sym("arm", hold(30, 0, 14)), sym("forearm", hold(-40, 0, 0))))
    an.add("milk", 2.0, True, merge({"body": {"rotation": hold(-4, 0, 0)}, "head": {"rotation": hold(10, 20, 0)}, "eyelids": {"position": hold(*H.OUT_)}},
                                    sym("arm", hold(-20, 0, -14)), sym("forearm", hold(-60, 0, 0))))


VIEWS = [("idle", 0, 0, 0), ("idle", 0, -35, 10), ("idle", 0, 90, 0), ("idle", 0, 180, 0),
         ("walk", 0.25, -30, 8), ("charge", 0.15, -60, 5), ("sit", 0, -30, 10), ("pounce", 0.5, -70, 5)]


def write_males(want, prev):
    """the bulls: same rig and animations, the male build; their animation names are the species' (see the docstring)"""
    entries = []
    for sp in SPECIES:
        if want and sp not in want:
            continue
        d = male(sp)
        name = sp + "_male"
        F.declare(name, d, declare_hook(sp, d))
        tex = F.paint(name, d, F.pal_for(d), paint_hook)
        geo, adoc, texes = G.write(name, tex, F.build(name, d, build_hook), F.anims(d, lambda an, sp=sp, d=d: anim_extra(an, sp, d)),
                                   list(d["variants"]), lambda k, d=d: F.paint(name, d, F.pal_for(d, k), paint_hook))
        path = os.path.join(G.ASSETS, "animations", "entity", name + ".animation.json")
        doc = json.load(open(path, encoding="utf-8"))
        doc["animations"] = {k.replace(f"animation.{name}.", f"animation.{sp}."): v for k, v in doc["animations"].items()}
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            json.dump(doc, fh, indent=2)
            fh.write("\n")
        height = G.model_height(geo)
        if prev:
            G.previews(name, geo, adoc, texes, VIEWS, F.FACE, height)
        entries.append((geo, tex, adoc["animations"][f"animation.{name}.idle"], height))
    return entries


def write_wool(name, d):
    """<name>_wool.png: the fleece alone in white, the rest transparent; the game draws it tinted over a dyed sheep"""
    F.declare(name, d, declare_hook(name, d))
    pal = F.pal_for(d)
    pal.extra = {k: v for k, v in pal.extra.items() if k != "wool_spots"}
    pal.extra["wool"] = "#f6f6f4"
    tex = F.paint(name, d, pal, paint_hook)
    n = len(tex)
    mask = [[(0, 0, 0, 0)] * n for _ in range(n)]
    for (p, f), (x0, y0, w, h) in H.RECT.items():
        if p.startswith("wool_"):
            for y in range(y0, y0 + h):
                for x in range(x0, x0 + w):
                    mask[y][x] = tex[y][x]
    H.write_png(os.path.join(G.ASSETS, "textures", "entity", name + "_wool.png"), mask)


if __name__ == "__main__":
    want = [a for a in sys.argv[1:] if not a.startswith("--")]
    cows = F.run_family("minotaur", SPECIES, declare_hook, paint_hook, build_hook, anim_extra, views=VIEWS)
    bulls = write_males(want, "--previews" in sys.argv)
    for sp, d in SPECIES.items():
        if d.get("wool") and (not want or sp in want):
            write_wool(sp, d)
            write_wool(sp + "_male", male(sp))
    if "--previews" in sys.argv and not want:
        import family_concepts as FC
        hs = {s["id"]: s["height"] for s in FC.family("minotaur")["species"]}
        ent = []
        for (cg, ct, ca, ch, cu), (bg, bt, ba, bu), sp in zip(cows, bulls, SPECIES):
            ent += [(cg, ct, ca, ch, cu), (bg, bt, ba, ch * 1.1, bu)]
        G.roster(os.path.join(G.PREVIEWS, "minotaur_roster.png"), ent)
