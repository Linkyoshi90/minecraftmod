#!/usr/bin/env python3
"""Lines for cheers (entity/Cheers.java): friends cheer when you earn an advancement. Can be run twice.

    python tools/gen_cheer_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402

M.add_lang({
    "message.harmontide.cheer": "%s cheers for you!",
    "message.harmontide.cheer.many": "%s and %s more cheer for you!",
})
print("cheer assets written")
