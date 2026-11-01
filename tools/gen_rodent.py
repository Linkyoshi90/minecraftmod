#!/usr/bin/env python3
"""Rodents (the rodent family, see rodent_field_guide.html): capybara, Ratatoskr.

    python tools/gen_rodent.py [species ...] [--previews]

girl_family bipeds with fur legs and paws. The capybara is broad and calm, with tiny round ears, hardly any tail and a
yuzu balanced on her head (`yuzu`); Ratatoskr is a red squirrel with tufted ears, a huge bushy tail curled up her back
and a messenger's satchel. Extra animations: "soak" (the capybara up to her chin in water, eyes half shut) and
"scamper" (Ratatoskr's quick hop with her tail flicking).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import girl_family as F  # noqa: E402
import girl_lib as G  # noqa: E402
from girl_lib import H, cube, pick, skin, CLEAR  # noqa: E402

STAFF_TILT = -82

SPECIES = {
    # twenty-third wave: the sasquatch, big and fluffy, pale blue, a shaggy mane falling round a pink face, a fluffy ruff
    "sasquatch_rodent": dict(
        build="strong", outfit=dict(top=["bandeau"], bottom="loincloth", colors=dict(cloth2="#7a9ac8")),
        hair="shaggy", bangs="messy", ears=None, legs="fur", foot="paw", ruff=True, chest_fur=True,
        pal=dict(SKIN=skin("#f0c8b8"), HAIR="#8ab0e0", FUR="#9ab8e4", LIGHT="#f0c8c0", MARK="#5a7aa8", EYE="#3a4a7a", CLOTH="#7a9ac8", TRIM="#f0c8c0",
                 pattern="solid"),
        variants={"brown": dict(HAIR="#5a3e26", FUR="#6a4a30", LIGHT="#d8b098", MARK="#2e1e12", CLOTH="#5a3e26", EYE="#2a1e14"),
                  "white": dict(HAIR="#e8ecf0", FUR="#f0f2f4", LIGHT="#f8d8d0", MARK="#9aa0a8", CLOTH="#9aa0a8"),
                  "red": dict(HAIR="#8a3a1e", FUR="#a04a28", LIGHT="#e8b0a0", MARK="#4a1a0a", CLOTH="#4a1a0a", EYE="#3a2a1a"),
                  "black": dict(HAIR="#1e1c20", FUR="#2a282c", LIGHT="#c8a8a0", MARK="#0e0c0e", CLOTH="#5a2a2a", EYE="#e8a030")}),
    # twenty-third wave (folklore): Sun Wukong, the Monkey King of Journey to the West: golden fur, a gold circlet on her brow, red
    # and gold armour, a long curling tail and her staff, the Ruyi Jingu Bang
    "sun_wukong_rodent": dict(
        build="athletic", outfit=dict(top=["corset", "capelet"], bottom="shorts", extras=["bracers", "belt", "pauldron"],
                                      colors=dict(cloth2="#e8b030", leather="#8a1e1a")),
        hair="shaggy", bangs="jagged", ears="round", legs="fur", foot="paw",
        tail=[(1, 1, 3.5), (.85, .85, 3.5), (.75, .75, 3.5), (.6, .6, 3), (.5, .5, 2.5)], tail_rest=[(-55, 0, 0), (15, 0, 0), (30, 0, 0), (35, 0, 0), (40, 0, 0)],
        tail_look="fur", circlet=True, staff=True,
        pal=dict(SKIN=skin("fair"), HAIR="#c8901e", FUR="#d8a030", LIGHT="#f8e0a0", MARK="#7a4a10", EYE="#e8c040", CLOTH="#c8281e", TRIM="#f0c840",
                 pattern="solid", extra=dict(gold="#f0c840", staff="#a8281e")),
        variants={"stone": dict(HAIR="#8a8a8a", FUR="#9a9a9a", LIGHT="#d8d8d4", MARK="#4a4a4a", CLOTH="#3a5a8a", EYE="#e8a030",
                                extra=dict(gold="#f0c840", staff="#3a3a3a")),
                  "white": dict(HAIR="#f0ece4", FUR="#f4f0e8", LIGHT="#ffffff", MARK="#a8a090", CLOTH="#c8281e", EYE="#c83a2a",
                                extra=dict(gold="#f0c840", staff="#c8281e")),
                  "black": dict(HAIR="#1e1a1a", FUR="#2a2424", LIGHT="#8a7a6a", MARK="#0e0a0a", CLOTH="#e8b030", TRIM="#c8281e", EYE="#f8d040",
                                extra=dict(gold="#f0c840", staff="#e8b030")),
                  "red": dict(HAIR="#8a2a14", FUR="#a83a1e", LIGHT="#f0b090", MARK="#4a1408", CLOTH="#e8b030", TRIM="#2a2a2a",
                              extra=dict(gold="#f0c840", staff="#2a2a2a"))}),
    # twentieth wave: the house mouse, big round ears and a long bare tail; the Gregory Horror Show homages (POP_CULTURE_VARIANTS.md)
    # twenty-second wave: the gibbon, fluffy and pale blue-grey round a cream face, green eyes, a fluffy ruff framing her face,
    # long curling tail
    "gibbon_rodent": dict(
        build="petite", outfit=dict(top=["halter"], bottom="shorts", colors=dict(cloth2="#6a8ab0")),
        hair="shaggy", bangs="wispy", ears=None, legs="fur", foot="paw",
        tail=[(1, 1, 3.5), (.85, .85, 3.5), (.75, .75, 3.5), (.6, .6, 3), (.5, .5, 2.5)], tail_rest=[(-55, 0, 0), (15, 0, 0), (30, 0, 0), (35, 0, 0), (40, 0, 0)],
        tail_look="fur", ruff=True,
        pal=dict(SKIN=skin("fair"), HAIR="#9ab0c8", FUR="#a8bcd4", LIGHT="#f4ece0", MARK="#6a7e98", EYE="#3a9a5a", CLOTH="#6a8ab0", TRIM="#f4ece0",
                 pattern="solid"),
        variants={"white": dict(HAIR="#e8e4dc", FUR="#f0ece4", LIGHT="#ffffff", MARK="#8a8478", CLOTH="#c8a060", EYE="#5a3a1e"),
                  "black": dict(HAIR="#26242a", FUR="#302e34", LIGHT="#d8ccb8", MARK="#141216", CLOTH="#c8a040", EYE="#c8a040"),
                  "gold": dict(HAIR="#d8a850", FUR="#e4b860", LIGHT="#f8ecd0", MARK="#9a6a24", CLOTH="#4a6a3a", EYE="#3a5a2a"),
                  "brown": dict(HAIR="#7a5a3e", FUR="#8a6a4c", LIGHT="#ecdcc4", MARK="#4a3422", CLOTH="#3a6a8a", EYE="#3a9a5a")}),
    # carry their props by variant (`ghs_kit`): Gregory's ring of keys, James's slingshot, Gregory Mama's soul bottles
    "house_mouse_rodent": dict(
        build="petite", outfit=dict(top=["halter", "vest"], bottom="shorts", extras=["belt"], colors=dict(cloth2="#5a4a3a", leather="#5a3a22")),
        hair="bob", bangs="wispy", ears="mouse", legs="fur", foot="paw",
        tail=[(.75, .75, 4), (.6, .6, 4), (.5, .5, 4), (.4, .4, 3)], tail_rest=[(-10, 0, 0), (15, 0, 0), (15, 0, 0), (10, 0, 0)], tail_look="fur",
        bare_tail=True, whisker_nose=True, ghs_kit=True,
        pal=dict(SKIN=skin("fair"), HAIR="#8a8a92", FUR="#9a9aa2", LIGHT="#e8e4e0", MARK="#5a5a62", EYE="#1a1418", CLOTH="#c8a878", TRIM="#7a5a3a",
                 INNER="#f0a8b0", pattern="solid", extra=dict(tail_col="#e8a8a8")),
        variants={"brown": dict(HAIR="#7a5a3a", FUR="#8a6a48", LIGHT="#e8d4b8", MARK="#4a3420", CLOTH="#5a8a6a"),
                  "white": dict(HAIR="#f4f2ee", FUR="#f6f4f0", LIGHT="#ffffff", MARK="#c8c4c0", EYE="#d83a4a", CLOTH="#a8c0e8"),
                  "black": dict(HAIR="#2a2828", FUR="#343234", LIGHT="#8a8688", MARK="#141214", EYE="#c8a040", CLOTH="#a83a3a",
                                extra=dict(tail_col="#8a6a70")),
                  "fawn": dict(HAIR="#c8945a", FUR="#d8a468", LIGHT="#f8ead4", MARK="#8a5a2a", EYE="#3a1a10", CLOTH="#4a6aa8"),
                  # homage (2026-10-28): Gregory (Gregory Horror Show): grey-brown, small red eyes, a worn hotelier's suit, a ring of keys
                  "innkeeper": dict(HAIR="#6a5e52", FUR="#7a6e62", LIGHT="#b8aa98", MARK="#3a322a", EYE="#d82a2a", CLOTH="#4a5a3a", TRIM="#c8a040",
                                    extra=dict(tail_col="#d89a98", cloth2="#3a3a30", leather="#2a221a", keys=True)),
                  # homage (2026-10-28): James (Gregory's grandson): younger and lighter, a striped shirt, a slingshot at his belt
                  "prankster": dict(HAIR="#a8968a", FUR="#b8a89a", LIGHT="#f0e8e0", MARK="#6a5a4e", EYE="#d82a2a", CLOTH="#d83a3a", TRIM="#f0f0f0",
                                    extra=dict(tail_col="#e8a8a8", cloth2="#3a5a9a", stripes=True, slingshot=True)),
                  # homage (2026-10-28): Gregory Mama: an old mouse, white hair, a long dark robe, glass soul bottles at her belt
                  "soul_keeper": dict(HAIR="#f0eeea", FUR="#8a8078", LIGHT="#d8d0c8", MARK="#4a423a", EYE="#d82a2a", CLOTH="#3a2a4a", TRIM="#8a6aa8",
                                      extra=dict(tail_col="#c8908e", cloth2="#2a1e38", leather="#1e1828", bottles=True))}),
    # twentieth wave: the sobolnik, a blue sable: big dark eyes, a white face and chest, frosty swirls, a bushy tail, a little gem
    "sobolnik_rodent": dict(
        build="slim", outfit=dict(top=["halter", "capelet"], bottom="shorts", extras=["scarf"], colors=dict(cloth2="#2a3a6a", scarf="#e8f0f8")),
        hair="sidetail", bangs="messy", ears="round", legs="fur", foot="paw",
        tail=[(2, 2, 4), (2.5, 2.5, 4), (2.25, 2.25, 3.5)], tail_rest=[(-25, 0, 0), (15, 0, 0), (12, 0, 0)], tail_look="fur", pendant=True,
        pal=dict(SKIN=skin("fair"), HAIR="#3a5a9a", FUR="#4a6aa8", LIGHT="#eef4fc", MARK="#1e2e5a", EYE="#1a1e2e", CLOTH="#5a7ac8", TRIM="#e8f0f8",
                 pattern="dapple", extra=dict(amber="#5ac8f0")),
        variants={"sable": dict(HAIR="#4a3020", FUR="#5a3a26", LIGHT="#e8c8a0", MARK="#2a1a10", CLOTH="#a87a3a", extra=dict(amber="#e8701e")),
                  "silver": dict(HAIR="#8a8a96", FUR="#9a9aa6", LIGHT="#ffffff", MARK="#4a4a56", CLOTH="#4a5a7a", extra=dict(amber="#a8e0f8")),
                  "green": dict(HAIR="#2a6a4a", FUR="#3a7a5a", LIGHT="#e0f8e8", MARK="#1a3e2a", CLOTH="#3a6a8a", extra=dict(amber="#e8c040")),
                  "red": dict(HAIR="#8a3a2a", FUR="#a84a34", LIGHT="#f8e0d0", MARK="#4a1a10", CLOTH="#3a5a8a", extra=dict(amber="#5ae8a8"))}),
    "capybara_rodent": dict(
        build="curvy", outfit=dict(top=["bandeau"], bottom="sarong", extras=["pearls"], colors=dict(cloth2="#e8a030")),
        hair="bob", bangs="blunt", ears="round", legs="fur", foot="paw", tail=[(1.5, 1, .75)], tail_rest=[(-20, 0, 0)], tail_look="fur", yuzu=True,
        pal=dict(SKIN=skin("tan"), HAIR="#6a4228", FUR="#8a5a3a", LIGHT="#b8875e", MARK="#5a3a24", EYE="#2a1a10", CLOTH="#3a8a7a", TRIM="#e8c060",
                 pattern="solid"),
        variants={"sandy": dict(FUR="#b8946a", HAIR="#8a6a48", LIGHT="#d8bc94"),
                  "dark": dict(FUR="#4a3220", HAIR="#2e1e12", LIGHT="#6a4e36"),
                  "ginger": dict(FUR="#a8582a", HAIR="#7a3e1e", LIGHT="#d0885a", CLOTH="#3a6a9a"),
                  "grey": dict(FUR="#7a726a", HAIR="#5a524a", LIGHT="#a8a098", CLOTH="#9a3a4a")}),
    # tenth wave: the needlehog, a hedgehog with long quills down her back and head (a note pinned among them)
    "needlehog_rodent": dict(
        build="petite", outfit=dict(top=["sweater"], bottom="shorts", extras=["satchel"], colors=dict(cloth2="#5a4a3a", leather="#7a5a3a")),
        hair="bob", bangs="blunt", ears="round", legs="fur", foot="paw", tail=[(1, 1, .75)], tail_rest=[(-20, 0, 0)], tail_look="fur", quills=True,
        pal=dict(SKIN=skin("fair"), HAIR="#6a5040", FUR="#c8b090", LIGHT="#ecdcc4", MARK="#3a2a20", EYE="#1e1410", CLOTH="#8aa0c8", TRIM="#c8a060",
                 pattern="solid", extra=dict(quill="#5a4030", quill_tip="#e8dcc8")),
        variants={"blue": dict(HAIR="#4a5a7a", FUR="#a8b0c0", MARK="#2a3040", CLOTH="#e8c070", extra=dict(quill="#3a4868", quill_tip="#c8d8f0")),
                  "albino": dict(HAIR="#f0ece6", FUR="#f8f4ee", LIGHT="#ffffff", MARK="#c8c0b8", EYE="#d84a5a", CLOTH="#c8a8d8",
                                 extra=dict(quill="#e8e2da", quill_tip="#ffffff")),
                  "cinnamon": dict(HAIR="#a85a2a", FUR="#d8a878", MARK="#6a3418", CLOTH="#5a8a5a", extra=dict(quill="#8a4a22", quill_tip="#f0d0a8")),
                  "dark": dict(HAIR="#2a2220", FUR="#6a5a50", MARK="#141010", CLOTH="#a84a4a", extra=dict(quill="#1e1816", quill_tip="#a8a098")),
                  # homage (2026-10-31): Sonic: blue quills and hair, a tan belly, green eyes, red shoes' colour on her clothes
                  "blue_blur": dict(HAIR="#1e4ad8", FUR="#2a5ae8", LIGHT="#f0d8b0", MARK="#142a8a", EYE="#2a9a4a", CLOTH="#d82a2a", TRIM="#f4f4f6", extra=dict(quill="#1e4ad8", quill_tip="#3a6af0"))}),
    # eleventh wave: the coatimano, a coati: a long ringed tail held high and a long nose
    "coatimano_rodent": dict(
        build="slim", outfit=dict(top=["halter", "vest"], bottom="shorts", extras=["satchel"], colors=dict(cloth2="#3a2a1e", leather="#7a5a3a")),
        hair="ponytail", bangs="messy", ears="round", legs="fur", foot="paw",
        tail=[(1.75, 1.75, 4), (1.5, 1.5, 4), (1.25, 1.25, 4), (1, 1, 3)], tail_rest=[(55, 0, 0), (12, 0, 0), (10, 0, 0), (8, 0, 0)],
        tail_look="fur", coati=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a3a7a", FUR="#3a5ab0", LIGHT="#e8eef8", MARK="#14204a", EYE="#1a1a24", CLOTH="#e8c060", TRIM="#e8e8f0",
                 pattern="bars"),
        variants={"brown": dict(HAIR="#5a3a24", FUR="#8a5a34", LIGHT="#e8d0b0", MARK="#3a2414", CLOTH="#4a7a5a"),
                  "ring": dict(HAIR="#4a4a50", FUR="#8a8a90", LIGHT="#f4f4f6", MARK="#1e1e22", CLOTH="#c84a3a"),
                  "red": dict(HAIR="#7a2e1a", FUR="#a8482a", LIGHT="#f0c8a8", MARK="#4a160a", CLOTH="#3a5a8a"),
                  "white": dict(HAIR="#e0dcd4", FUR="#ece8e0", LIGHT="#ffffff", MARK="#9a948a", EYE="#c84a5a", CLOTH="#5a7a3a")}),
    # twelfth wave: the marmotaur, a stout marmot with curled golden ram horns and an amber pendant
    "marmotaur_rodent": dict(
        build="curvy", outfit=dict(top=["sweater"], bottom="shorts", extras=["pearls"], colors=dict(cloth2="#5a4a3a", pearl="#e8c050")),
        hair="shaggy", bangs="messy", ears="round", legs="fur", foot="paw", tail=[(2.25, 2.25, 3), (2.5, 2.5, 3)], tail_rest=[(-35, 0, 0), (20, 0, 0)],
        tail_look="fur", horns="ram", pendant=True,
        pal=dict(SKIN=skin("fair"), HAIR="#8a6a42", FUR="#a88458", LIGHT="#f0e4cc", MARK="#5a4228", EYE="#3a7ac8", CLOTH="#e8dcc4", TRIM="#e8c050",
                 pattern="dapple", extra=dict(horn="#d8a838", amber="#e8701e")),
        variants={"grey": dict(HAIR="#6a6660", FUR="#8a8680", LIGHT="#e8e4e0", MARK="#4a4642", extra=dict(horn="#a8783a", amber="#e8701e")),
                  "black": dict(HAIR="#2a2624", FUR="#3a3432", LIGHT="#8a8480", MARK="#1a1614", EYE="#c8a030", extra=dict(horn="#c8ccd4", amber="#5ab8e8")),
                  "red": dict(HAIR="#8a3e1e", FUR="#a8542a", LIGHT="#f0c8a8", MARK="#5a2410", extra=dict(horn="#d8a838", amber="#e8c03a")),
                  "white": dict(HAIR="#e8e4dc", FUR="#f0ece6", LIGHT="#ffffff", MARK="#b8b0a4", EYE="#c84a5a", extra=dict(horn="#e8c050", amber="#c83a5a"))}),
    # twelfth wave: the bambadger, a badger striped like bamboo, a bamboo flute on a cord
    "bambadger_rodent": dict(
        build="petite", outfit=dict(top=["halter"], bottom="shorts", extras=["scarf"], colors=dict(scarf="#e8dcb0", cloth2="#3a5a2a")),
        hair="bob", bangs="parted", ears="round", legs="fur", foot="paw", tail=[(2, 2, 2.5)], tail_rest=[(-25, 0, 0)], tail_look="fur", flute=True,
        pal=dict(SKIN=skin("fair"), HAIR="#4a7a2e", FUR="#6a9a3a", LIGHT="#f0ecd8", MARK="#3a5a1e", EYE="#3a6a2a", CLOTH="#e8dcb0", TRIM="#8ab84a",
                 pattern="bars", extra=dict(bamboo="#8ab84a")),
        variants={"golden": dict(HAIR="#a8882e", FUR="#c8a840", MARK="#6a5418", CLOTH="#3a5a2a", extra=dict(bamboo="#e8c84a")),
                  "black": dict(HAIR="#2a2a26", FUR="#3a3a34", LIGHT="#e8e4d8", MARK="#141412", EYE="#8ab84a", extra=dict(bamboo="#4a4a3a")),
                  "red": dict(HAIR="#8a3a22", FUR="#a84e2e", MARK="#5a2010", extra=dict(bamboo="#c8683a")),
                  "pale": dict(HAIR="#b8c8a0", FUR="#d0dcb8", LIGHT="#ffffff", MARK="#8a9a70", extra=dict(bamboo="#d8e8b0"))}),
    # thirteenth wave: the squirricerat, a squirrel with a triceratops' frill and three horns, a great spiral tail
    "squirricerat_rodent": dict(
        build="athletic", outfit=dict(top=["halter", "vest"], bottom="shorts", extras=["belt", "pearls"], colors=dict(cloth2="#3a2a1e", leather="#8a6a3a")),
        hair="ponytail", bangs="jagged", ears="tuft", legs="fur", foot="paw",
        tail=[(2.75, 2.75, 4), (3.25, 3.25, 4), (3, 3, 4), (2.25, 2.25, 3)], tail_rest=[(-70, 0, 0), (55, 0, 0), (45, 0, 0), (40, 0, 0)], tail_look="fur",
        frill=True,
        pal=dict(SKIN=skin("fair"), HAIR="#2a4a9a", FUR="#3a5ab0", LIGHT="#d0dcf4", MARK="#1a2a6a", EYE="#e8a030", CLOTH="#d8b060", TRIM="#2a4a9a",
                 pattern="dapple", extra=dict(frill="#a8c4f8", frill_edge="#e8c060", horn="#f0e8d8")),
        variants={"green": dict(HAIR="#2a6a3a", FUR="#3a8a4a", LIGHT="#c8ecc8", MARK="#1a4a24", extra=dict(frill="#5aa86a", frill_edge="#f0d060", horn="#f0e8d8")),
                  "red": dict(HAIR="#8a2a1e", FUR="#b8402a", LIGHT="#f0c8b0", MARK="#5a1a10", CLOTH="#3a5a8a",
                              extra=dict(frill="#d8603a", frill_edge="#f8e070", horn="#f0e8d8")),
                  "brown": dict(HAIR="#5a3a22", FUR="#8a5a34", LIGHT="#e8d0b0", MARK="#3a2414", extra=dict(frill="#a87a4a", frill_edge="#e8c060", horn="#f0e8d8")),
                  "grey": dict(HAIR="#4a4a52", FUR="#7a7a84", LIGHT="#e0e0e6", MARK="#2a2a32", extra=dict(frill="#9a9aa6", frill_edge="#c8d8f0", horn="#f0e8d8"))}),
    # thirteenth wave: the kamaitachi, the sickle weasel of the wind: long and slim, a sickle blade on each forearm
    "kamaitachi_rodent": dict(
        build="slim", outfit=dict(top=["halter"], bottom="hakama", extras=["scarf"], colors=dict(cloth2="#3a5a7a", scarf="#e8f0f8")),
        hair="sidetail", bangs="jagged", ears="round", legs="fur", foot="paw",
        tail=[(1.5, 1.5, 4), (1.5, 1.5, 4), (1.25, 1.25, 4), (1, 1, 3)], tail_rest=[(-15, 0, 0), (12, 0, 0), (10, 0, 0), (8, 0, 0)], tail_look="fur",
        sickles=True,
        pal=dict(SKIN=skin("fair"), HAIR="#c89a5a", FUR="#d8aa6a", LIGHT="#fbf4e8", MARK="#6a4a2a", EYE="#3a8a6a", CLOTH="#3a5a7a", TRIM="#e8f0f8",
                 pattern="solid", extra=dict(blade="#c8d0dc")),
        variants={"white": dict(HAIR="#f4f2ee", FUR="#f6f4f0", LIGHT="#ffffff", MARK="#2a2628", EYE="#c84a5a", CLOTH="#2a2a3a"),
                  "brown": dict(HAIR="#6a4a2e", FUR="#7a5636", LIGHT="#e8d4bc", MARK="#3a2416", CLOTH="#5a3a6a"),
                  "grey": dict(HAIR="#7a7a80", FUR="#8a8a90", LIGHT="#e8e8ec", MARK="#3a3a40", CLOTH="#3a6a5a"),
                  "black": dict(HAIR="#1e1c1e", FUR="#2a282a", LIGHT="#8a8488", MARK="#0e0c0e", EYE="#e8d040", CLOTH="#a83a3a")}),
    # fifteenth wave: the rockhyrax, a stocky rock hyrax with small round ears and a back of blue scales, no tail to speak of
    "rockhyrax_rodent": dict(
        build="curvy", outfit=dict(top=["sweater"], bottom="shorts", extras=["satchel"], colors=dict(cloth2="#3a4a7a", leather="#6a4a2a")),
        hair="bob", bangs="parted", ears="round", legs="fur", foot="paw", tail=[(1, 1, .5)], tail_rest=[(-20, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#2a3a7a", FUR="#4a6ab8", LIGHT="#e8d8b8", MARK="#1e2a5a", EYE="#2a3a8a", CLOTH="#e8c070", TRIM="#2a3a7a",
                 pattern="bars"),
        variants={"brown": dict(HAIR="#4a3424", FUR="#7a5a3e", LIGHT="#d8c0a0", MARK="#3a2818", CLOTH="#3a6a8a"),
                  "grey": dict(HAIR="#4a4a50", FUR="#7a7a82", LIGHT="#d8d8dc", MARK="#2a2a30", CLOTH="#c84a3a"),
                  "sand": dict(HAIR="#a88a5a", FUR="#d0b080", LIGHT="#f4e8d0", MARK="#8a6a3a", CLOTH="#3a6a5a"),
                  "dark": dict(HAIR="#141218", FUR="#2a2830", LIGHT="#8a8890", MARK="#0a080e", EYE="#e8a030", CLOTH="#c8a050")}),
    # fourteenth wave: the silkcoon, a raccoon of silky white fur swirled in blue, a ringed bushy tail
    "silkcoon_rodent": dict(
        build="petite", outfit=dict(top=["halter", "capelet"], bottom="shorts", extras=["pearls"], colors=dict(cloth2="#2a4a9a")),
        hair="bob", bangs="parted", ears="round", legs="fur", foot="paw",
        tail=[(2, 2, 4), (2.25, 2.25, 4), (2, 2, 3)], tail_rest=[(-30, 0, 0), (15, 0, 0), (12, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#e8eef8", FUR="#f0f4fa", LIGHT="#ffffff", MARK="#3a5ab8", EYE="#2a3a8a", CLOTH="#3a5ab8", TRIM="#f0f4fa",
                 pattern="bars"),
        variants={"grey": dict(HAIR="#6a6a72", FUR="#8a8a92", LIGHT="#e0e0e6", MARK="#2a2a30", CLOTH="#4a6a3a"),
                  "brown": dict(HAIR="#6a4a2e", FUR="#8a6644", LIGHT="#e8d4bc", MARK="#3a2414", CLOTH="#3a5a7a"),
                  "white": dict(HAIR="#f4f2ee", FUR="#f6f4f0", LIGHT="#ffffff", MARK="#c8c4be", EYE="#c84a5a", CLOTH="#c8a8e8"),
                  "black": dict(HAIR="#1e1c22", FUR="#2a2830", LIGHT="#6a6870", MARK="#0e0c10", EYE="#e8c040", CLOTH="#a83a4a"),
                  # homage (2026-10-30): Tom Nook: brown and tan, a green apron
                  "shopkeeper": dict(HAIR="#7a5a34", FUR="#8a6644", LIGHT="#e8d4a8", MARK="#4a3420", EYE="#1e1a14", CLOTH="#3a8a5a", TRIM="#e8d870")}),
    # eighteenth wave: the pineskrell, a blue squirrel with a huge frosted tail and glowing crystal pinecones in her hair
    "pineskrell_rodent": dict(
        build="petite", outfit=dict(top=["sweater"], bottom="shorts", extras=["satchel"], colors=dict(cloth2="#2a3a6a", leather="#5a3a5a")),
        hair="ponytail", bangs="messy", ears="tuft", legs="fur", foot="paw", cones=True, glow=["pinecone"],
        tail=[(2.5, 2.5, 4), (3.25, 3.25, 4), (3, 3, 4), (2.25, 2.25, 3)], tail_rest=[(-70, 0, 0), (55, 0, 0), (45, 0, 0), (40, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#3a6ac8", FUR="#3a70c8", LIGHT="#d8ecff", MARK="#1e3a8a", EYE="#a87ae8", CLOTH="#4a8ad8", TRIM="#c8a0f0",
                 pattern="solid", extra=dict(cone="#8ae8ff", cone_scale="#4a8ae8")),
        variants={"brown": dict(FUR="#8a5a32", HAIR="#6a4224", LIGHT="#f0dcc0", MARK="#4a2a14", EYE="#3a8a4a", CLOTH="#4a6a3a",
                                extra=dict(cone="#f8c060", cone_scale="#a8682a")),
                  "green": dict(FUR="#3a8a5a", HAIR="#2a6a42", LIGHT="#d8f8e0", MARK="#1e4a2e", EYE="#e8c040", CLOTH="#3a7a6a",
                                extra=dict(cone="#b8f870", cone_scale="#4a9a3a")),
                  "purple": dict(FUR="#6a4aa8", HAIR="#523a8a", LIGHT="#ecdcff", MARK="#2e1e5a", EYE="#5ae8d0", CLOTH="#7a5ab8",
                                 extra=dict(cone="#f0a0ff", cone_scale="#8a4ab8")),
                  "red": dict(FUR="#b8402a", HAIR="#8a2a1e", LIGHT="#ffe0d0", MARK="#5a140e", EYE="#3a6ac8", CLOTH="#a83a3a",
                              extra=dict(cone="#ffb070", cone_scale="#c8502a"))}),
    "ratatoskr_rodent": dict(
        build="petite", outfit=dict(top=["bandeau", "vest"], bottom="shorts", extras=["satchel", "scarf"], colors=dict(scarf="#3a7a4a", leather="#5a3a22")),
        hair="ponytail", bangs="messy", ears="tuft", legs="fur", foot="paw",
        tail=[(2.5, 2.5, 4), (3, 3, 4), (2.75, 2.75, 4), (2, 2, 3)], tail_rest=[(-70, 0, 0), (55, 0, 0), (45, 0, 0), (40, 0, 0)], tail_look="fur",
        pal=dict(SKIN=skin("fair"), HAIR="#b8501e", FUR="#c8602a", LIGHT="#f0e0c8", MARK="#7a3414", EYE="#2a1a10", CLOTH="#4a6a3a", TRIM="#c8a050",
                 pattern="solid"),
        variants={"grey": dict(FUR="#8a8a90", HAIR="#6a6a72", LIGHT="#e8e8ec", MARK="#4a4a50"),
                  "black": dict(FUR="#222024", HAIR="#141216", LIGHT="#5a5860", MARK="#0a0a0c"),
                  "fox": dict(FUR="#c8783a", HAIR="#a85a26", LIGHT="#e8b07a", MARK="#5a3418", CLOTH="#3a5a7a"),
                  "abert": dict(FUR="#5a5a62", HAIR="#3a3a40", LIGHT="#f4f4f6", MARK="#7a3a1a", CLOTH="#7a3a3a"),
                  # homage (2026-10-30): Conker (Conker's Bad Fur Day): red-orange fur, a cream face, a blue hoodie
                  "hoodie_squirrel": dict(FUR="#d8702a", HAIR="#c8601e", LIGHT="#f8e0b8", MARK="#8a3a10", EYE="#3a2a1a", CLOTH="#3a5ab8", TRIM="#f0f0f0")}),
}


def declare_hook(sp, d):
    out = [("yuzu", 2, 2, 2), ("yuzu_leaf", 1, .25, 1.5)] if d.get("yuzu") else []
    if d.get("circlet"):                                 # Wukong's gold circlet, its band round her brow
        out += [("circlet", 9, 1, 9), ("circlet_curl", .75, 1.5, .5)]
    if d.get("staff"):                                   # the Ruyi Jingu Bang: a red staff with gold caps
        out += [("staff_pole", 1, 16, 1), ("staff_cap", 1.5, 2, 1.5)]
    if d.get("quills"):
        out += [("quill", 1, 1, 5), ("note", 2, 2.5, .25)]
    if d.get("coati"):
        out += [("coati_nose", 2, 1.75, 2.5)]
    if d.get("pendant"):
        out += [("pendant", 1.5, 2, .5)]
    if d.get("flute"):
        out += [("flute", 6, .75, .75), ("cord", .25, 3, .25)]
    if d.get("frill"):
        out += [("frill", 12, 8, .75), ("trihorn", 1, 4, 1), ("nosehorn", .75, 1.25, .75)]
    if d.get("sickles"):
        out += [("sickle", .5, 5, 2.25)]
    if d.get("cones"):
        out += [("pinecone", 1.75, 2.5, 1.75), ("cone_cap", 1, .5, 1)]
    if d.get("whisker_nose"):
        out += [("mouse_nose", 1, .75, .5)]
    if d.get("ruff"):                                    # the gibbon's ruff of fur framing her face
        out += [("ruff_side", 1, 5, 1), ("ruff_chin", 6, 1.25, 1)]
    if d.get("chest_fur"):                               # the sasquatch's shaggy chest
        out += [("chest_tuft", 5.5, 3.5, 1)]
    if d.get("ghs_kit"):
        out += [("keyring", 1.75, 1.75, .25), ("key", .5, 2, .25), ("sling_y", 2, 2.5, .5), ("sling_grip", .75, 2, .75),
                ("soul_bottle", 1.25, 2, 1.25), ("bottle_cork", .75, .5, .75)]
    return out


def paint_hook(pt, pal, d):
    gold = G.shades(pal.extra.get("gold", "#f0c840"))
    pt("circlet", "all", lambda f, x, y, w, h: CLEAR if f in ("up", "down") else pick(gold, 0.55 + 0.2 * (x % 4 == 0), x, y))
    pt("circlet_curl", "all", lambda f, x, y, w, h: pick(gold, 0.65, x, y))
    pt("staff_pole", "all", lambda f, x, y, w, h: pick(G.shades(pal.extra.get("staff", "#a8281e")), 0.5 + 0.15 * (x == 0), x, y))
    pt("staff_cap", "all", lambda f, x, y, w, h: pick(gold, 0.55 + 0.2 * (y % 2 == 0), x, y))
    orange = G.shades("#f0a020")
    pt("yuzu", "all", lambda f, x, y, w, h: pick(orange, 0.55 + 0.25 * (f == "up") - 0.15 * (f == "down"), x, y))
    pt("yuzu_leaf", "all", lambda f, x, y, w, h: pick(G.shades("#4a9a3a"), 0.55, x, y))
    pt("coati_nose", "all", lambda f, x, y, w, h: pick(G.shades("#1e1a1c"), 0.4, x, y) if f == "north" and y == h - 1
       else pick(pal.LIGHT if f == "down" else pal.FUR, 0.55, x, y))
    ruff = G.fur_fn(pal, light_faces=(), grain=0.2, seed=71)
    for p in ("ruff_side", "ruff_chin"):
        pt(p, "all", ruff)
    if d.get("chest_fur"):                               # her chest and belly are shaggy too, the tuft's lower edge ragged
        shag = G.fur_fn(pal, light_faces=(), grain=0.2, pattern="solid", seed=73, fluffy=True)
        pt("chest_tuft", "all", shag)
        for p in ("collar", "midriff"):
            pt(p, "all", G.fur_fn(pal, light_faces=(), grain=0.2, pattern="solid", seed=74))
    frill, edge = G.shades(pal.extra.get("frill", "#5a7ac8")), G.shades(pal.extra.get("frill_edge", "#e8c060"))

    def fr(f, x, y, w, h):                               # the frill: a fan plate, scalloped and spotted along its edge
        if f not in ("north", "south"):
            return CLEAR                                 # a thin plate: no frame round it
        dx = (x + 0.5 - w / 2) / (w / 2)
        top = (h - 1) * dx * dx                          # a half-disc: the corners cut away
        if f in ("north", "south") and y < top:
            return CLEAR
        if f in ("north", "south") and y < top + 1.5:
            return pick(edge, 0.6, x, y)
        if (x * 7 + y * 5) % 13 == 0:
            return pick(pal.MARK, 0.4, x, y)             # spots
        return pick(frill, 0.55 + 0.12 * ((x + y) % 4 == 0), x, y)
    pt("frill", "all", fr)
    horn = G.shades(pal.extra.get("horn", "#f0e8d8"))
    pt("trihorn", "all", lambda f, x, y, w, h: pick(horn, 0.5 + 0.2 * (y < 2), x, y))
    pt("nosehorn", "all", lambda f, x, y, w, h: pick(horn, 0.6, x, y))
    blade = G.shades(pal.extra.get("blade", "#c8d0dc"))
    pt("sickle", "all", lambda f, x, y, w, h: pick(blade, 0.55 + 0.3 * (f in ("north", "east") and x == w - 1), x, y)
       if f not in ("east", "west") or y > h * 0.15 else CLEAR)
    cone, scale = G.shades(pal.extra.get("cone", "#8ae8ff")), G.shades(pal.extra.get("cone_scale", "#4a8ae8"))
    pt("pinecone", "all", lambda f, x, y, w, h: pick(scale if (x + y * 2) % 3 == 0 else cone, 0.55 + 0.25 * ((x + y) % 2), x, y))
    pt("cone_cap", "all", lambda f, x, y, w, h: pick(G.shades("#5a3a22"), 0.45, x, y))
    amber = G.shades(pal.extra.get("amber", "#e8701e"))
    pt("pendant", "all", lambda f, x, y, w, h: pick(amber, 0.55 + 0.3 * (f == "north" and 0 < x < w - 1 and y in (2, 3)), x, y)
       if 0 < y < h - 1 or 0 < x < w - 1 else pick(G.shades("#e8c050"), 0.6, x, y))
    bam = G.shades(pal.extra.get("bamboo", "#8ab84a"))
    pt("flute", "all", lambda f, x, y, w, h: pick(G.shades("#3a2a1e"), 0.3, x, y) if f == "north" and x % 4 == 1
       else pick(bam, 0.45 + 0.2 * (x % 8 == 0), x, y))
    pt("cord", "all", lambda f, x, y, w, h: pick(G.shades("#8a5a2a"), 0.5, x, y))
    if pal.extra.get("bamboo"):                         # the badger's white stripe down her hair
        pt("hair_cap", ("up", "north"), lambda f, x, y, w, h: pick(pal.LIGHT, 0.75, x, y) if abs(x + 0.5 - w / 2) < w * 0.12 else None)
    q, tip = G.shades(pal.extra.get("quill", "#5a4030")), G.shades(pal.extra.get("quill_tip", "#e8dcc8"))
    pt("quill", "all", lambda f, x, y, w, h: pick(tip if y >= h - 1 or f == "south" else q, 0.5 + 0.15 * (x % 2), x, y))
    pt("note", "all", lambda f, x, y, w, h: pick(G.shades("#3a3a4a"), 0.45, x, y) if f == "south" and y % 2 == 1 and 0 < x < w - 1
       else pick(G.shades("#f4ecd8"), 0.65, x, y))
    paint_ghs(pt, pal, d)


def paint_ghs(pt, pal, d):
    """the house mouse's bare tail and pink nose, and the homage props, each painted only on its own variant"""
    e = pal.extra
    show = lambda on, fn: (lambda f, x, y, w, h: fn(f, x, y, w, h) if on else CLEAR)
    if d.get("bare_tail"):
        tail = G.shades(e.get("tail_col", "#e8a8a8"))
        for p in ("tail_a", "tail_b", "tail_c", "tail_d"):
            pt(p, "all", lambda f, x, y, w, h: pick(tail, 0.55 + 0.1 * ((y + x) % 3 == 0), x, y))
    pt("mouse_nose", "all", lambda f, x, y, w, h: pick(G.shades("#e88a98"), 0.55 + 0.15 * (f == "up"), x, y))
    brass, steel = G.shades("#c8a040"), G.shades("#8a8a94")
    pt("keyring", "all", show(e.get("keys"), lambda f, x, y, w, h: pick(brass, 0.6, x, y) if x in (0, w - 1) or y in (0, h - 1) else CLEAR))
    pt("key", "all", show(e.get("keys"), lambda f, x, y, w, h: pick(brass if y % 3 else steel, 0.55, x, y)))
    wood = G.shades("#8a5a2e")

    def sling(f, x, y, w, h):                            # a Y: two prongs over a stem
        if f not in ("north", "south"):
            return pick(wood, 0.45, x, y)
        if y < h * 0.5:
            return pick(wood, 0.5, x, y) if x in (0, w - 1) else CLEAR
        return pick(wood, 0.5, x, y) if abs(x + 0.5 - w / 2) < 0.8 else CLEAR
    pt("sling_y", "all", show(e.get("slingshot"), sling))
    pt("sling_grip", "all", show(e.get("slingshot"), lambda f, x, y, w, h: pick(G.shades("#c83a3a"), 0.45, x, y)))
    glass = G.shades("#8ad8c8")
    pt("soul_bottle", "all", show(e.get("bottles"), lambda f, x, y, w, h: pick(G.shades("#f0f8ff"), 0.9, x, y) if f == "north" and y == 1 and x == 1
                                   else pick(glass, 0.5 + 0.2 * (y < h / 2), x, y)))
    pt("bottle_cork", "all", show(e.get("bottles"), lambda f, x, y, w, h: pick(wood, 0.6, x, y)))
    if e.get("stripes"):                                 # James's striped shirt
        white = G.shades("#f0f0f0")
        for p in ("chest", "midriff"):
            pt(p, ("north", "south", "east", "west"), lambda f, x, y, w, h: pick(white, 0.7, x, y) if y % 2 == 0 else None)


def build_hook(r, g, d):
    if d.get("circlet"):                                 # round her brow, two little curls rising over her forehead
        hy, z = g.hy, g.z
        r.put("head", cube("circlet", [-4.5, hy + 5.6, -4.5 + z]), cube("circlet_curl", [-1.5, hy + 6.5, -4.6 + z]),
              cube("circlet_curl", [0.75, hy + 6.5, -4.6 + z]))
    if d.get("staff"):                                   # through her right fist, level with the ground, a gold cap at each end
        px, py, pz = r.b["hand_r"]["pivot"]
        cx, cz, grip = px - 0.5, pz - 0.5, [px, py - 1.5, pz]
        held = lambda p, o: G.rot_cube(p, o, grip, STAFF_TILT)
        r.add("staff", "hand_r", grip, [held("staff_pole", [cx, py - 9.5, cz]), held("staff_cap", [cx - 0.25, py - 11.5, cz - 0.25]),
                                        held("staff_cap", [cx - 0.25, py + 6.5, cz - 0.25])])
    if d.get("whisker_nose"):                           # a little pink nose
        r.put("head", cube("mouse_nose", [-0.5, g.hy + 2.5, -4.4 + g.z]))
    if d.get("ruff"):                                    # fluffy fur down both sides of her face and under her chin
        hy, z = g.hy, g.z
        r.put("head", cube("ruff_side", [-4.6, hy + 0.5, -4.6 + z]), cube("ruff_side", [3.6, hy + 0.5, -4.6 + z]),
              cube("ruff_chin", [-3.0, hy - 0.6, -4.6 + z]))
    if d.get("chest_fur"):
        fz = -G.S("chest")[2] / 2 - G.S("bust")[2] + g.z
        r.put("body", cube("chest_tuft", [-2.75, 16.75 + g.dy, fz - 0.6]))
    if d.get("ghs_kit"):                                # at her belt: the ring of keys (right), the slingshot (left), three soul bottles (back)
        bz = -G.S("hips")[2] / 2 + g.z
        r.add("ghs_kit", "body", [0, 12, g.z], [
            cube("keyring", [-3.6, 10.6, bz - 0.4]), cube("key", [-3.3, 8.8, bz - 0.5]), cube("key", [-2.6, 9.0, bz - 0.45]),
            G.rot_cube("sling_y", [2.4, 10.0, bz - 0.6], [3.4, 11.0, bz], 0, 0, -10),
            G.rot_cube("sling_grip", [2.9, 8.4, bz - 0.7], [3.4, 11.0, bz], 0, 0, -10),
            cube("soul_bottle", [-2.6, 9.6, -bz + 0.1]), cube("bottle_cork", [-2.35, 11.6, -bz + 0.35]),
            cube("soul_bottle", [-0.6, 9.4, -bz + 0.1]), cube("bottle_cork", [-0.35, 11.4, -bz + 0.35]),
            cube("soul_bottle", [1.4, 9.6, -bz + 0.1]), cube("bottle_cork", [1.65, 11.6, -bz + 0.35])])
    if d.get("frill"):                                   # the frill behind her head, two brow horns and a little nose horn
        hy, z = g.hy, g.z
        r.add("frill", "head", [0, hy + 6, 4.8 + z], [G.rot_cube("frill", [-6, hy + 5.0, 4.8 + z], [0, hy + 6, 4.8 + z], -25, 0, 0)])
        r.put("head", G.rot_cube("trihorn", [-2.7, hy + 8.6, -2.0 + z], [-2.2, hy + 8.6, -1.5 + z], -25, 0, 12),
              G.rot_cube("trihorn", [1.7, hy + 8.6, -2.0 + z], [2.2, hy + 8.6, -1.5 + z], -25, 0, -12),
              G.rot_cube("nosehorn", [-0.375, hy + 2.4, -4.5 + z], [0, hy + 2.4, -4.1 + z], -75, 0, 0))
    if d.get("sickles"):                                 # a sickle blade along each forearm, curving back past the elbow
        px, py, pz = r.b["forearm_r"]["pivot"]
        r.put("forearm_r", G.rot_cube("sickle", [px - 1.2, py - 4.4, pz - 0.6], [px - 1.0, py - 3.5, pz], 15, 0, 8))
    if d.get("pendant"):                                # the amber pendant on her necklace
        r.put("body", cube("pendant", [-0.75, 16.6, -G.S("chest")[2] / 2 - G.S("bust")[2] - 0.6 + g.z]))
    if d.get("flute"):                                  # the flute hangs across her chest from its cord
        fz = -G.S("chest")[2] / 2 - G.S("bust")[2] - 0.8 + g.z
        r.put("body", cube("flute", [-3, 15.2, fz]), cube("cord", [-0.125, 15.9, fz + 0.25]))
    if d.get("coati"):                                   # the long nose
        r.put("head", cube("coati_nose", [-1, g.hy + 0.6, -5.9 + g.z]))
    if d.get("quills"):                                 # rows of quills sweeping back over her head and down her back
        hy, z = g.hy, g.z
        hq = [G.rot_cube("quill", [x - 0.5, hy + y, 3.6 + z], [x, hy + y + 0.5, 3.8 + z], 25 + 10 * (y > 5), x * 5, 0)
              for x in (-3.4, -1.7, 0, 1.7, 3.4) for y in (2.5, 5.0, 7.2)]
        r.add("quills_head", "head", [0, hy + 4, 2 + z], hq)
        bq = [G.rot_cube("quill", [x - 0.5, y, 1.5 + z], [x, y + 0.5, 1.7 + z], 40, x * 6, 0) for x in (-2.2, -0.75, 0.75, 2.2) for y in (13.5, 16.0, 18.5)]
        r.add("quills", "body", [0, 16, 1.6 + z], bq + [G.rot_cube("note", [-1.8, 16.0, 3.0 + z], [-0.8, 17.0, 3.0 + z], 10, 0, -12)])
    if d.get("cones"):                                  # two crystal pinecones hanging by her right ear
        hy, z = g.hy, g.z
        r.add("cones", "head", [-4.2, hy + 5, z], [cube("cone_cap", [-5.5, hy + 5.0, -0.5 + z]), cube("pinecone", [-5.875, hy + 2.5, -0.875 + z]),
                                                    cube("cone_cap", [-5.5, hy + 4.2, 1.6 + z]), cube("pinecone", [-5.875, hy + 1.7, 1.225 + z])])
    if d.get("yuzu"):                                   # the capybara's famous yuzu, balanced on her head
        r.add("yuzu", "head", [0, g.hy + 8.5, g.z], [cube("yuzu", [-1, g.hy + 8.6, -1 + g.z]), cube("yuzu_leaf", [-0.5, g.hy + 10.6, -0.5 + g.z])])


def anim_extra(an, sp, d):
    Tk, hold, sym, merge = H.track, H.hold, H.sym, H.merge
    an.add("soak", 4.0, True, merge({"root": {"position": hold(0, -7, 0)}, "head": {"rotation": hold(-6, 0, 0)},
                                     "eyelids": {"position": Tk((0, H.REST), (1.0, H.OUT_), (3.0, H.OUT_), (4.0, H.REST))}},
                                    sym("arm", hold(-20, 0, 25))))
    if d.get("tail"):
        tails = ["tail"] + [f"tail_{i}" for i in range(2, len(d["tail"]) + 1)]
        flick = {t: {"rotation": Tk((0, (0, 0, 0)), (0.1, (-15, 0, 0)), (0.2, (0, 0, 0)), (0.4, (0, 0, 0)))} for t in tails[1:]}
        if d.get("quills"):                             # the needlehog rolled into a ball of quills
            an.add("curl", 2.0, True, merge({"root": {"position": hold(0, -5, 0)}, "body": {"rotation": hold(85, 0, 0), "position": hold(0, -2, 2)},
                                             "head": {"rotation": hold(50, 0, 0)}, "leg_r": {"rotation": hold(-110, 0, 0)}, "leg_l": {"rotation": hold(-110, 0, 0)},
                                             "shin_r": {"rotation": hold(140, 0, 0)}, "shin_l": {"rotation": hold(140, 0, 0)},
                                             "eyelids": {"position": hold(*H.OUT_)}}, sym("arm", hold(-130, 0, -15))))
        an.add("scamper", 0.4, True, merge(flick, {"root": {"position": Tk((0, (0, 0, 0)), (0.1, (0, 1.2, 0)), (0.2, (0, 0, 0)), (0.4, (0, 0, 0)))},
                                                   "body": {"rotation": hold(20, 0, 0)}}, sym("arm", hold(-40, 0, 0))))


# homage looks of the twenty-eighth wave (POP_CULTURE_VARIANTS.md)
SPECIES["house_mouse_rodent"]["variants"]["spark_mouse"] = dict(HAIR="#f8d830", FUR="#f8d830", LIGHT="#fff0a0", MARK="#3a2a14", EYE="#1e1e24", CLOTH="#c84a2a", TRIM="#3a2a14")


if __name__ == "__main__":
    F.run_family("rodent", SPECIES, declare_hook, paint_hook, build_hook, anim_extra)
