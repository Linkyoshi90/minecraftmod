#!/usr/bin/env python3
"""Adds a catalogue card to harpy_field_guide.html for every harpy in the game that has none yet (the guide's first eight
were written by hand). Each card is built from the harpy's class comment (entity/<Name>Harpy.java), her name in the lang
file and her spawn biome tag. Can be run again: harpies that already have a card are left alone.

    python tools/add_harpy_guide_entries.py
"""
import html
import json
import os
import re

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GUIDE = os.path.join(REPO, "harpy_field_guide.html")
ENTITY = os.path.join(REPO, "src", "main", "java", "com", "linkyoshi90", "monstergirls", "entity")
LANG = os.path.join(REPO, "src", "main", "resources", "assets", "monstergirls", "lang", "en_us.json")
TAGS = os.path.join(REPO, "src", "main", "resources", "data", "monstergirls", "tags", "worldgen", "biome")


def class_comment(path):
    with open(path, encoding="utf-8") as fh:
        text = fh.read()
    m = re.search(r"/\*\*(.*?)\*/\s*public class", text, re.S)
    if not m:
        return ""
    lines = [re.sub(r"^\s*\*\s?", "", line) for line in m.group(1).splitlines()]
    doc = " ".join(x.strip() for x in lines if x.strip())
    doc = re.sub(r"\{@\w+ ([^}]*)\}", r"\1", doc)
    doc = re.sub(r"\([^)]*(HARMONTIDE|tools/|model )[^)]*\)", "", doc)
    doc = re.sub(r"\s+", " ", doc).strip()
    return re.sub(r"\s+([.,:;])", lambda m: m.group(1), doc).replace(":.", ".")


def biomes(sid):
    path = os.path.join(TAGS, f"spawns_{sid}.json")
    if not os.path.exists(path):
        return []
    with open(path, encoding="utf-8") as fh:
        values = json.load(fh).get("values", [])
    out = []
    for v in values:
        v = v["id"] if isinstance(v, dict) else v
        if v.startswith("minecraft:"):
            out.append(v.split(":")[1].replace("_", " "))
    return out


def card(name, sid, doc, where):
    first = doc.split(". ")[0].rstrip(".")
    rest = doc[len(first):].lstrip(". ").strip() or first
    habitat = ", ".join(where[:5]).capitalize() if where else "Wherever the game places her"
    search = html.escape(f"{name.lower()} {doc.lower()} {habitat.lower()}")
    return f"""      <article class="card" data-group="harmontide" data-search="{search}">
        <div class="card-top"><span class="group">Harmontide</span><span class="tag">In the game</span></div>
        <h3>{html.escape(name)} Harpy</h3>
        <p class="habitat"><b>Habitat</b>{html.escape(habitat)}</p>
        <p><b>Ecology & behaviour</b>{html.escape(first)}.</p>
        <p><b>Player interaction</b>{html.escape(rest)}</p>
        <p class="risk"><b>Entity</b><code>monstergirls:{sid}</code></p>
      </article>

"""


def main():
    with open(GUIDE, encoding="utf-8") as fh:
        page = fh.read()
    with open(LANG, encoding="utf-8") as fh:
        lang = json.load(fh)
    have = set(re.findall(r"<h3>([^<]+?) Harpy</h3>", page))
    cards = []
    for f in sorted(os.listdir(ENTITY)):
        if not f.endswith("Harpy.java") or f.startswith("Abstract"):
            continue
        sid = re.sub(r"(?<!^)(?=[A-Z])", "_", f[:-5]).lower()
        name = lang.get(f"entity.monstergirls.{sid}", "").replace(" Harpy", "")
        if not name or name in have:
            continue
        cards.append(card(name, sid, class_comment(os.path.join(ENTITY, f)), biomes(sid)))
    anchor = '    </div>\n    <p class="empty" id="empty">'
    if "\r\n" in page:
        anchor = anchor.replace("\n", "\r\n")
    assert anchor in page
    page = page.replace(anchor, "".join(cards) + anchor, 1)
    with open(GUIDE, "w", encoding="utf-8", newline="") as fh:
        fh.write(page)
    print(f"{len(cards)} harpy cards added")


if __name__ == "__main__":
    main()
