#!/usr/bin/env python3
"""Passing flocks (2026-11-08, entity/Flocks): lang and a sentence for the encounters guide.

    uv run --no-project --with pillow python tools/gen_flock_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_milk_assets import add_lang  # noqa: E402
import gen_manor_assets  # noqa: E402


def main():
    add_lang({
        "monstergirls.configuration.passingFlocks": "Passing flocks",
        "message.monstergirls.flock.north": "A flock of %s comes down nearby to rest on the way north. They'll fly on tomorrow.",
        "message.monstergirls.flock.south": "A flock of %s comes down nearby to rest on the way south. They'll fly on tomorrow.",
    })
    gen_manor_assets.GUIDE_ADD.clear()
    gen_manor_assets.GUIDE_ADD["journal.monstergirls.guide.encounters"] = (
        " Every eight days, in the morning, a flock of wild harpies passing through comes down near you to rest: waterfowl by "
        "water, songbirds inland. They fly on the next day, all but any who has made a friend.")
    gen_manor_assets.append_guides()


if __name__ == "__main__":
    main()
