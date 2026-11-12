#!/usr/bin/env python3
"""Atonement before Anubis (2026-11-09, entity/girl/Atonement): lang, and her signature page rewritten to say how.

    uv run --no-project --with pillow python tools/gen_atonement_assets.py
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import split_guides  # noqa: E402

LANG = {
    "message.harmontide.heart_still_heavy": "%s weighs your heart again. Still heavy: %s more good deeds to go",
    "message.harmontide.heart_atoned": "%s weighs your heart again. It is lighter than the feather now: she lowers her spear and lets you pass",
    "message.harmontide.atonement.cured": "A villager cured: your heart feels a little lighter",
    "message.harmontide.atonement.golem": "A golem built to guard others: your heart feels a little lighter",
}
SIGNATURE = ("The weighing: She weighs every player who comes near. Anyone who killed villagers or iron golems is condemned and hunted. "
             "Others may open the tomb chests she guards. Each villager you cure and each iron golem you build lightens a heavy heart by one; "
             "when your deeds balance your deaths, show her a feather to be weighed again.")


def main():
    add_lang(LANG)
    path = os.path.join(ASSETS, "lang", "en_us.json")
    with open(path, encoding="utf-8") as fh:
        text = fh.read()
    key = "journal.harmontide.anubis_canid.signature"
    old = json.loads(text)[key]
    if old != SIGNATURE:
        a = json.dumps(key) + ": " + json.dumps(old, ensure_ascii=False)
        assert text.count(a) == 1
        text = text.replace(a, json.dumps(key) + ": " + json.dumps(SIGNATURE, ensure_ascii=False))
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            fh.write(text)
    split_guides.main()


if __name__ == "__main__":
    main()
