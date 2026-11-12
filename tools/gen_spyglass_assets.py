#!/usr/bin/env python3
"""Studying through a spyglass (2026-11-09, Journal.spyglass): a sentence for the bestiary guide.

    uv run --no-project --with pillow python tools/gen_spyglass_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_manor_assets  # noqa: E402


def main():
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.harmontide.guide.bestiary"] = (
        " A spyglass helps: whoever is in its crosshair, up to 64 blocks off, is studied twice as fast.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
