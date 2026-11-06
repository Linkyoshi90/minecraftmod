#!/usr/bin/env python3
"""Lines for a friend's first name (entity/Names.java). Can be run twice.

    python tools/gen_name_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402

M.add_lang({"message.monstergirls.named": "%s! She loves her new name"})
print("name assets written")
