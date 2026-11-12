#!/usr/bin/env python3
"""Builds tree_of_life.html: the index volume of the field guides, every mob by her real clade (tools/taxonomy.py,
TAXONOMY_PLAN.md) instead of by her form. Each name links to her card in the form guide that describes her body and
behaviour (<form>_field_guide.html#<id>; the harpy and anuran guides have no per-species anchors, so those link to the guide).

    python tools/build_tree_guide.py

Re-run after adding a species or a guide.
"""
import collections
import glob
import html
import json
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import build_field_guides as B  # noqa: E402
import taxonomy as T  # noqa: E402

REPO = B.REPO
E = html.escape
THEME = dict(bg="#10140f", panel="#182016", panel2="#1f2a1c", ink="#eef2e6", muted="#a9b59c", accent="#9fd36a",
             line="#2f3d2a", green="#c8e6a0", grad="#24361c")
MYTHIC = {"FAE", "DRAGON", "UNDEAD", "DEVIL", "SLIME", "CONSTRUCT", "RIDER"}
FORM_GUIDE = {"HARPY": "harpy_field_guide.html", "ANURAN": "anuran_field_guide.html"}


def anchors():
    """species id -> the guide file whose card carries her id."""
    ids = {sp for sp, _ in T.species_rows()}
    found = {}
    for f in sorted(glob.glob(os.path.join(REPO, "*_field_guide.html"))):
        text = open(f, encoding="utf-8").read()
        for m in re.finditer(r'<article class="card" id="([a-z0-9_]+)"', text):
            if m.group(1) in ids:
                found.setdefault(m.group(1), os.path.basename(f))
    return found


def main():
    lang = json.load(open(os.path.join(REPO, "src/main/resources/assets/harmontide/lang/en_us.json"), encoding="utf-8"))
    rows = T.species_rows()
    where = anchors()
    by = collections.OrderedDict((k, []) for k in T.FAM)
    for sp, old in rows:
        clade, pos, flag = T.place(sp, old)
        by[clade].append((sp, old, pos, flag))
    out = []
    w = out.append
    w('<!doctype html>\n<html lang="en">\n<head>\n<meta charset="utf-8">\n<meta name="viewport" content="width=device-width, initial-scale=1">')
    w(f"<title>The Tree of Life — Harmontide Field Guide</title>\n<style>{B.CSS % THEME}"
      ".clade-list{columns:3 220px;column-gap:24px;padding-left:1.1rem;color:var(--muted)}.clade-list li{break-inside:avoid;margin:.3rem 0}"
      ".clade-list a{color:var(--ink);text-decoration:none;border-bottom:1px dotted var(--line)}.clade-list a:hover{color:var(--accent)}"
      ".pos{display:block;font-size:.78rem;color:var(--muted)}.flag{font-size:.72rem;color:var(--accent);margin-left:4px}"
      ".toc{display:flex;flex-wrap:wrap;gap:8px;margin:14px 0 0}.toc a{color:var(--muted);text-decoration:none;border:1px solid var(--line);border-radius:99px;padding:3px 10px;font-size:.8rem}"
      "\n</style>\n</head>\n<body>")
    w("<header>")
    w('  <div class="eyebrow">Harmontide · a field guide to an inhabited world · index volume · every kind by her real kin</div>')
    w("  <h1>The Tree of Life</h1>")
    w(f'  <p class="lede">{len(rows)} kinds on {len(T.FAM)} branches: {len(T.FAM) - len(MYTHIC)} real clades and {len(MYTHIC)} groups of legend '
      "that sit outside the tree. A girl's body (her form, the word in her id) decides how she moves, nests and behaves; "
      "her branch decides her milk, the name on her cheese, her spawn-rate setting and the journal's tree of life. "
      "Each name links to her card in the guide for her form.</p>")
    w("  " + B.volumes_nav(""))
    w('  <nav class="toc">' + "".join(f'<a href="#{k.lower()}">{E(T.SHORT[k][0])} · {len(v)}</a>' for k, v in by.items()) + "</nav>")
    w("</header>\n<main>")
    w('  <section class="note" style="margin-top:0"><h2>How to read it</h2><ul>'
      "<li><b>Branch</b>Where she really sits, from class down to family. The body animal decides; horns, wings and tails don't.</li>"
      "<li><b>Guest</b>Her true kin are too few here (fewer than four), so she lives with her nearest relatives. Her true place is still given.</li>"
      "<li><b>Judgement call</b>A placement that could reasonably go elsewhere (TAXONOMY_PLAN.md lists the reasons).</li>"
      "<li><b>Milk</b>What a bucket of that branch's milk does for whoever drinks it. Plants, fungi and constructs give none.</li>"
      "</ul></section>")
    for k, members in by.items():
        name, path, milk, folded = T.FAM[k]
        mythic = k in MYTHIC
        w(f'  <section id="{k.lower()}">')
        w(f'    <div class="eyebrow">{"Outside the tree" if mythic else E(path)}</div>')
        w(f"    <h2>{E(name)} <span style=\"color:var(--muted);font-size:1rem\">({len(members)})</span> "
          f'<a href="{k.lower()}_clade_guide.html" style="font-size:.9rem;color:var(--accent)">the branch\'s guide →</a></h2>')
        bits = [f"<b style=\"display:inline;margin-right:6px\">Milk</b>{E(milk)}"]
        if folded and folded != "new":
            bits.append(f"<b style=\"display:inline;margin:0 6px 0 14px\">Was</b>{E(folded.title())}")
        w('    <p class="section-intro">' + " ".join(bits) + "</p>")
        w('    <ul class="clade-list">')
        for sp, old, pos, flag in sorted(members, key=lambda r: lang.get("entity.harmontide." + r[0], r[0])):
            label = E(lang.get("entity.harmontide." + sp, sp.replace("_", " ").title()))
            href = where.get(sp)
            href = f"{href}#{sp}" if href else FORM_GUIDE.get(old, "")
            link = f'<a href="{href}">{label}</a>' if href else label
            tag = {"guest": "guest", "call": "judgement call"}.get(flag, "")
            w(f'      <li>{link}{f"<span class=flag>{tag}</span>" if tag else ""}<span class="pos">{E(pos)} · {E(old.lower())} form</span></li>')
        w("    </ul>\n  </section>")
    w('  <footer>Generated by <code>tools/build_tree_guide.py</code> from <code>tools/taxonomy.py</code>. '
      "The form guides (the volume bar above) keep the bodies, life cycles and behaviour.</footer>")
    w("</main>\n</body>\n</html>\n")
    path = os.path.join(REPO, "tree_of_life.html")
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write("\n".join(out))
    linked = sum(1 for sp, _ in rows if sp in where)
    print(f"wrote tree_of_life.html: {len(rows)} kinds, {linked} linked to their card, the rest to their form guide")


if __name__ == "__main__":
    main()
