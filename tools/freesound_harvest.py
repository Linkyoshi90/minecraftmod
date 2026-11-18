#!/usr/bin/env python3
"""Finds short CC0 clips on Freesound and downloads their HQ previews (no account needed).

    python tools/freesound_harvest.py <out_dir> [--per N]

Every candidate's own sound page is checked for the CC0 licence link before it is kept, so the
search filter alone is never trusted. Writes <out_dir>/<group>/<id>.mp3 plus <out_dir>/manifest.json
(id, title, author, url, licence, duration) which tools/sound_import.py turns into CREDITS.
"""
import json
import os
import re
import sys
import time
import urllib.parse
import urllib.request

UA = {"User-Agent": "Mozilla/5.0 (sound-harvest)"}
CC0 = "creativecommons.org/publicdomain/zero/1.0"

# titles that are rarely a clean single call
SKIP = re.compile(r"alien|synth|ambien|background|\bbg\b|loop|forest|morning|dawn|traffic|warehouse|wing|flap|"
                  r"music|remix|robot|toy|cartoon|kazoo|whistle|horn|squeak|door|vocoder|monster|zombie|imitation|"
                  r"rubber|808|guitar|snare|laser|beam|dinosaur|click|button|scary|child|kid|baby|son of|c'mon|"
                  r"gonna|\bman\b|\bmale\b|weird", re.I)
FEMALE = re.compile(r"\b(female|woman|women|girl|lady)\b", re.I)


def W(words):
    """Matches any of the words at a word start (so 'hen' is not found in 'then')."""
    return re.compile(r"\b(" + words + r")", re.I)


# the clip's title or tags must name what the group is
MATCH = {
    "songbird": W(r"sparrow|finch|songbird|robin|warbler|wren|canary|chickadee|bunting|chirp|tweet"),
    "hummingbird": W(r"hummingbird|sunbird"),
    "corvid": W(r"crow|raven|caw|magpie|jackdaw|rook\b|jay\b"),
    "owl": W(r"owl|hoot"),
    "raptor": W(r"eagle|hawk|falcon|kite\b|buzzard|osprey|kestrel|raptor"),
    "seabird": W(r"gull|seagull|tern\b|kittiwake|seabird"),
    "penguin": W(r"penguin"),
    "waterfowl": W(r"quack|duck|mallard"),
    "crane": W(r"crane|swan|flamingo|goose|heron|stork"),
    "parrot": W(r"parrot|macaw|cockatoo|galah|parakeet|lorikeet|conure"),
    "peacock": W(r"peacock|peafowl|peahen"),
    "poultry": W(r"chicken|hen\b|cluck|turkey|rooster"),
    "dove": W(r"dove|pigeon|coo\b|cooing"),
    "ratite": W(r"emu|ostrich|cassowary|rhea\b"),
    "girl_hurt": W(r"ouch|pain|hurt|grunt|aie|ow\b|oof|hit\b"),
    "girl_death": W(r"scream|death|dying|die\b|gasp|shriek|cry"),
    "girl_giggle": W(r"giggle|laugh|chuckle|tehe|hehe"),
    "eat": W(r"eat|bite|chew|crunch|munch|nom\b|chomp"),
    # amphibians
    "frog_croak": W(r"frog|ribbit|croak"),
    "frog_chirp": W(r"peeper|tree ?frog|treefrog|frog chirp|coqui"),
    "bullfrog": W(r"bullfrog|bull frog"),
    "toad": W(r"toad"),
    "tongue": W(r"slurp|lick|tongue|squelch|suck"),
    "bubbles": W(r"bubble|blub|gurgle"),
}
# clips a person listened to or read and threw out (instruments, jokes, effects, suggestive voices)
REJECT = {556575, 447496, 246318, 529957, 515408, 233123, 233125, 233120, 207387, 660305, 660304, 182017, 241519,
          607902, 443960, 571424, 558488, 251938, 517614, 343978, 220163, 321163, 372182}
# pond and night recordings are many frogs at once, not one girl
SKIP_MORE = re.compile(r"pond|night|chorus|swamp|marsh|lake|river|evening|crickets|insects|many frogs|\brain\b", re.I)
SKIP_MORE_GROUPS = {"frog_croak", "frog_chirp", "bullfrog", "toad"}

# group -> (queries, max seconds)
GROUPS = {
    "songbird": (["sparrow chirp", "finch song", "songbird chirp", "robin bird song", "bird chirp single",
                  "canary", "wren", "bird tweet"], 3.0),
    "hummingbird": (["hummingbird", "sunbird"], 3.0),
    "corvid": (["crow caw", "raven call", "magpie"], 2.5),
    "owl": (["owl hoot", "barn owl screech", "tawny owl"], 3.0),
    "raptor": (["hawk screech", "eagle cry", "falcon call", "hawk", "eagle", "kestrel", "buzzard bird"], 4.0),
    "seabird": (["seagull", "gull call"], 3.0),
    "penguin": (["penguin"], 5.0),
    "waterfowl": (["duck quack", "goose honk"], 2.5),
    "crane": (["crane bird call", "swan call", "flamingo", "goose", "heron", "crane bird"], 4.0),
    "parrot": (["parrot squawk", "macaw", "cockatoo"], 2.5),
    "peacock": (["peacock call", "peacock", "peafowl"], 5.0),
    "poultry": (["chicken cluck", "hen cluck"], 2.5),
    "dove": (["dove coo", "pigeon coo", "pigeon", "dove", "cooing"], 5.0),
    "ratite": (["emu", "ostrich", "cassowary"], 5.0),
    "girl_hurt": (["female pain grunt", "woman hurt", "female ouch", "girl hurt", "female grunt", "female pain",
                   "woman pain", "female hit"], 2.0),
    "girl_death": (["female death scream", "woman dying", "female scream short", "female death", "woman scream",
                    "female gasp", "girl scream"], 3.0),
    "girl_giggle": (["girl giggle", "female giggle", "woman laugh short", "giggle", "female laugh", "woman giggle"], 3.0),
    "eat": (["chewing", "eating crunch", "bite apple", "apple bite", "munch", "eating"], 2.0),
    "frog_croak": (["frog croak", "frog ribbit", "frog", "single frog", "frog call"], 3.0),
    "frog_chirp": (["spring peeper", "tree frog", "treefrog", "frog chirp", "coqui", "Frogs tree frog", "peeper call",
                    "Hyla", "frog squeak", "small frog call", "frog trill"], 3.0),
    "bullfrog": (["bullfrog", "bull frog", "Frogs bullfrog", "American bullfrog", "Lithobates", "deep frog croak"], 5.0),
    "toad": (["toad", "toad call", "toad trill", "natterjack", "cane toad", "Toads", "American toad", "Bufo"], 5.0),
    "tongue": (["slurp", "tongue click", "lick", "wet slurp", "squelch"], 1.5),
    "bubbles": (["bubbles underwater", "bubble pop", "water bubbles", "blub"], 2.0),
}


def get(url):
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=30) as r:
        return r.read()


def search(query, max_dur, pages=3):
    found = []
    for page in range(1, pages + 1):
        f = f'license:"Creative Commons 0" duration:[0.2 TO {max_dur}]'
        url = "https://freesound.org/search/?" + urllib.parse.urlencode(
            {"q": query, "f": f, "s": "Downloads (most first)", "page": page})
        html = get(url).decode("utf-8", "replace")
        # each result card: sound link, duration, preview
        for m in re.finditer(r'/people/([^/"]+)/sounds/(\d+)/', html):
            found.append((m.group(1), int(m.group(2))))
        time.sleep(1)
    return list(dict.fromkeys(found))


def details(user, sid):
    html = get(f"https://freesound.org/people/{user}/sounds/{sid}/").decode("utf-8", "replace")
    if CC0 not in html:
        return None
    prev = re.search(r'https://cdn\.freesound\.org/previews/\d+/%d_\d+-hq\.mp3' % sid, html)
    title = re.search(r'<meta property="og:title" content="([^"]*)"', html)
    dur = re.search(r'data-duration="([\d.]+)"', html)
    if not prev:
        return None
    tags = sorted(set(re.findall(r'/browse/tags/([\w-]+)/', html)))
    return {"tags": tags, "id": sid, "author": user, "title": re.sub(r" by %s$" % re.escape(user), "", title.group(1)) if title else str(sid),
            "url": f"https://freesound.org/people/{user}/sounds/{sid}/", "license": "CC0 1.0",
            "preview": prev.group(0), "duration": float(dur.group(1)) if dur else None}


def main():
    out = sys.argv[1]
    per = int(sys.argv[sys.argv.index("--per") + 1]) if "--per" in sys.argv else 6
    only = sys.argv[sys.argv.index("--only") + 1].split(",") if "--only" in sys.argv else None
    mpath = os.path.join(out, "manifest.json")
    manifest = json.load(open(mpath)) if os.path.exists(mpath) else {}
    for group, (queries, max_dur) in GROUPS.items():
        if only and group not in only:
            continue
        have = manifest.setdefault(group, [])
        seen = {h["id"] for h in have}
        for q in queries:
            if len(have) >= per:
                break
            for user, sid in search(q, max_dur):
                if len(have) >= per:
                    break
                if sid in seen or sid in REJECT:
                    continue
                seen.add(sid)
                try:
                    d = details(user, sid)
                except Exception as e:  # noqa: BLE001
                    print("  skip", sid, e)
                    continue
                time.sleep(1)
                if not d or SKIP.search(d["title"]):
                    continue
                text = d["title"] + " " + " ".join(d["tags"])
                if group in SKIP_MORE_GROUPS and SKIP_MORE.search(d["title"]):
                    continue
                if not MATCH[group].search(text) or (group.startswith("girl_") and not FEMALE.search(" ".join(d["tags"]))):
                    continue
                os.makedirs(os.path.join(out, group), exist_ok=True)
                path = os.path.join(out, group, f"{sid}.mp3")
                if not os.path.exists(path):
                    open(path, "wb").write(get(d["preview"]))
                d["file"] = f"{group}/{sid}.mp3"
                have.append(d)
                print(f"{group}: {sid} {d['title']} by {d['author']}".encode("ascii", "replace").decode())
                json.dump(manifest, open(mpath, "w"), indent=1)
        json.dump(manifest, open(mpath, "w"), indent=1)
    print("done", {g: len(v) for g, v in manifest.items()})


if __name__ == "__main__":
    main()
