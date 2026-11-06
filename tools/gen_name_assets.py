#!/usr/bin/env python3
"""Lines for a friend's first name (entity/Names.java) and the journal's dearest friends (Journal.devoted). Can be run twice.

    python tools/gen_name_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402

M.add_lang({"message.monstergirls.named": "%s! She loves her new name",
            "message.monstergirls.devoted": "%s is devoted to you: your journal remembers the day",
            "journal.monstergirls.dearest.title": "Dearest friends",
            "journal.monstergirls.dearest.line": "%s, %s (day %s)"})
print("name assets written")
