#!/usr/bin/env python3
"""Builds the field guides for the families in tools/family_concepts.py: <family>_field_guide.html in the repo root,
in the same style as harpy_field_guide.html and anuran_field_guide.html.

Model renders are embedded when they exist (re-run after generating models):
    previews/<family>_roster.png           the family side by side, to scale
    previews/<species>_preview.png         turnaround + signature poses
    previews/<species>_preview_face.png    face close-ups
    previews/<species>_variants.png        the five colours
    python tools/build_field_guides.py [family ...]
"""
import base64
import html
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import family_concepts as C  # noqa: E402

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PREV = os.path.join(REPO, "previews")
VOLUMES = [("Index", "The Tree of Life", "tree_of_life.html"), ("I", "Harpies", "harpy_field_guide.html"), ("II", "Bullywugs & Mucus Toads", "anuran_field_guide.html")] + \
          [(f["volume"], f["title"], f["id"] + "_field_guide.html") for f in C.FAMILIES]

E = html.escape
NL = chr(10)


def img(name, alt, style="width:100%;height:auto;border:1px solid var(--line);border-radius:16px;margin:12px 0"):
    path = os.path.join(PREV, name)
    if not os.path.exists(path):
        return ""
    data = base64.b64encode(open(path, "rb").read()).decode()
    return f'<img src="data:image/png;base64,{data}" alt="{E(alt)}" style="{style}">'


CSS = """
:root{color-scheme:dark;--bg:%(bg)s;--panel:%(panel)s;--panel2:%(panel2)s;--ink:%(ink)s;--muted:%(muted)s;--accent:%(accent)s;--line:%(line)s;--green:%(green)s}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--ink);font:16px/1.55 system-ui,-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif}
header{padding:clamp(28px,6vw,72px) max(20px,calc((100%% - 1180px)/2));background:linear-gradient(120deg,%(grad)s,var(--bg) 75%%);border-bottom:1px solid var(--line)}
.eyebrow{text-transform:uppercase;letter-spacing:.18em;color:var(--accent);font-size:.78rem;font-weight:750}
h1{font-size:clamp(2.3rem,6.5vw,5rem);line-height:.98;letter-spacing:-.05em;margin:.4rem 0 1rem}
.lede{max-width:780px;color:var(--muted);font-size:1.1rem}
main{max-width:1180px;margin:auto;padding:28px 20px 72px}
.hero{display:grid;grid-template-columns:minmax(0,1.15fr) minmax(260px,.85fr);gap:26px;align-items:center;margin-bottom:42px}
.hero img,.hero .stand-in{width:100%%;height:auto;display:block;border:1px solid var(--line);border-radius:16px;background:var(--panel)}
.stand-in{padding:26px;display:grid;grid-template-columns:repeat(2,1fr);gap:10px}
.stand-in div{background:var(--panel2);border:1px solid var(--line);border-radius:12px;padding:14px;color:var(--ink);font-weight:650}
.stand-in small{display:block;color:var(--muted);font-weight:400}
.note{background:var(--panel);padding:24px;border:1px solid var(--line);border-radius:16px}
h2{font-size:clamp(1.5rem,3vw,2.2rem);letter-spacing:-.025em;margin:0 0 12px}
h3{font-size:1.35rem;margin:.45rem 0 .8rem}
section{margin-top:48px}
p{margin:.55rem 0;color:var(--ink);opacity:.92}b{display:block;color:var(--green);font-size:.77rem;letter-spacing:.06em;text-transform:uppercase;margin-bottom:2px}
.note ul,.card ul{padding-left:1.2rem;color:var(--muted)}.note li,.card li{margin:.45rem 0}
.grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px}
.card{background:var(--panel);border:1px solid var(--line);border-radius:15px;padding:22px;min-width:0}
.card-top{display:flex;gap:8px;justify-content:space-between;align-items:center;flex-wrap:wrap}
.group{color:var(--accent);font-size:.8rem;font-weight:700;text-transform:uppercase;letter-spacing:.06em}
.tag{border:1px solid var(--line);border-radius:99px;padding:3px 9px;color:var(--muted);font-size:.76rem}
.card p{margin:.85rem 0}.risk{border-top:1px solid var(--line);padding-top:12px}
.section-intro{color:var(--muted);max-width:780px}
.rules{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin:18px 0 10px}
.rules.four{grid-template-columns:repeat(4,1fr)}
.rule{background:var(--panel2);border-radius:12px;padding:18px;border:1px solid var(--line)}
.rule strong{display:block;color:var(--accent);margin-bottom:5px}
.table-wrap{overflow-x:auto;border:1px solid var(--line);border-radius:15px;margin:18px 0 10px}
table{border-collapse:collapse;width:100%%;min-width:640px;background:var(--panel)}
th,td{text-align:left;vertical-align:top;padding:12px 16px;border-bottom:1px solid var(--line)}
tr:last-child td,tr:last-child th{border-bottom:0}
thead th{color:var(--accent);font-size:.8rem;text-transform:uppercase;letter-spacing:.06em;background:var(--panel2)}
tbody th{color:var(--green);font-size:.82rem;text-transform:uppercase;letter-spacing:.05em;font-weight:700;width:16%%}
td{color:var(--ink);opacity:.92}
.swatches{display:flex;flex-wrap:wrap;gap:6px;margin-top:6px}
.swatch{border:1px solid var(--line);border-radius:8px;padding:4px 8px;font-size:.8rem;color:var(--muted);background:var(--panel2)}
.swatch strong{color:var(--ink);font-weight:650}
.volumes{display:flex;flex-wrap:wrap;gap:8px;margin-top:18px}
.volumes a{color:var(--muted);text-decoration:none;border:1px solid var(--line);border-radius:99px;padding:4px 11px;font-size:.82rem}
.volumes a.here{color:var(--bg);background:var(--accent);border-color:var(--accent)}
code{font-size:.9em;color:var(--green)}
footer{border-top:1px solid var(--line);padding-top:20px;margin-top:40px;color:var(--muted);font-size:.9rem}
@media(max-width:900px){.rules.four{grid-template-columns:repeat(2,1fr)}}
@media(max-width:760px){.hero{grid-template-columns:1fr}.grid{grid-template-columns:1fr}.rules,.rules.four{grid-template-columns:1fr}}
@media print{body{background:white;color:#111}.card,.note,.rule,table,thead th{background:white;border-color:#aaa;color:#111}p,td,.lede,.section-intro,footer{color:#333}}
"""


def volumes_nav(current):
    links = []
    for vol, title, fname in VOLUMES:
        cls = ' class="here"' if fname == current else ""
        links.append(f'<a href="{fname}"{cls}>{vol} · {E(title)}</a>')
    return '<nav class="volumes" aria-label="Field guide volumes">' + "".join(links) + "</nav>"


def tiers_table():
    rows = "".join(f"<tr><th>{E(n)}</th><td>{E(a)}</td><td>{E(d)}</td></tr>" for n, a, d in C.TIERS)
    return ('<div class="table-wrap"><table><thead><tr><th>Bond</th><th>Affection</th><th>What it means</th></tr></thead>'
            f"<tbody>{rows}</tbody></table></div>")


SHARED_CORE = [
    ("Affection", "Each monster girl remembers every player separately. Feeding, petting and her family's own bond action raise it; hurting her lowers it. <code>/monstergirls bonds</code> lists everyone nearby."),
    ("Sit", "Right-click any monster girl with a plain stick to make her sit; again to let her get up."),
    ("Sharing a bed", "At high friendship (25) she may join you in bed: once a night she decides, one in three at 25, certain at 100. Sleep through the night and she is expecting."),
    ("Expecting", "A pregnancy lasts one in-game day. Her belly rounds out as it goes on (the model shows it)."),
    ("Born devoted", "Her young are born devoted to the player who fathered them (full affection) and never share that player's bed."),
    ("Presents", "At high friendship every family brings presents of its own kind."),
]


def build(f):
    fid = f["id"]
    fname = fid + "_field_guide.html"
    out = []
    w = out.append
    w('<!doctype html>\n<html lang="en">\n<head>\n<meta charset="utf-8">\n<meta name="viewport" content="width=device-width, initial-scale=1">')
    w(f"<title>{E(f['title'])} — Harmontide Field Guide</title>\n<style>{CSS % f['theme']}</style>\n</head>\n<body>")
    w("<header>")
    w(f'  <div class="eyebrow">Harmontide · a field guide to an inhabited world · volume {f["volume"]} · {E(f["subtitle"])}</div>')
    w(f"  <h1>{E(f['title'])}</h1>")
    w(f'  <p class="lede">{E(f["lede"])}</p>')
    if f.get("food"):
        w(f'  <p class="lede" style="font-size:.95rem"><b style="color:var(--accent)">Favourite food:</b> {E(f["food"])}</p>')
    w("  " + volumes_nav(fname))
    w("</header>\n<main>")

    # hero
    h = f["hero"]
    roster = img(f"{fid}_roster.png", f"The {f['title']} side by side, to scale", style="")
    if not roster:
        roster = '<div class="stand-in">' + "".join(
            f'<div>{E(s["name"])}<small>{E(s["group"])} · about {s["height"]} blocks</small></div>' for s in f["species"]) + "</div>"
    w('  <section class="hero" style="margin-top:0">')
    w("    " + roster)
    w('    <div class="note">')
    w(f'      <div class="eyebrow">{E(h["eyebrow"])}</div>\n      <h2>{E(h["title"])}</h2>\n      <p>{E(h["text"])}</p>\n      <ul>')
    for t, d in h["bullets"]:
        w(f"        <li><b>{E(t)}</b>{E(d)}</li>")
    w("      </ul>\n    </div>\n  </section>")

    # shared relationship core
    w('  <section id="relationships">\n    <h2>Relationships (shared by every family)</h2>')
    w('    <p class="section-intro">The same relationship core runs through all MonsterGirls families, from the harpies on. '
      'This volume only changes how a ' + E(f["title"].rstrip("s").lower()) + ' builds trust, what she gives back, and how she bears young.</p>')
    w('    <div class="rules">' + "".join(f'<div class="rule"><strong>{E(t)}</strong>{d}</div>' for t, d in SHARED_CORE) + "</div>")
    w("    " + tiers_table())
    w("  </section>")

    # friendship
    fr = f["friendship"]
    w('  <section id="friendship">\n    <h2>Friendship</h2>')
    w('    <div class="grid">')
    w('      <div class="card"><h3>What builds trust</h3><ul>' + "".join(f"<li>{E(x)}</li>" for x in fr["builds"]) + "</ul></div>")
    w('      <div class="card"><h3>What costs trust</h3><ul>' + "".join(f"<li>{E(x)}</li>" for x in fr["costs"]) + "</ul></div>")
    w(f'      <div class="card" style="grid-column:1/-1"><h3>Devoted (60+)</h3><p>{E(fr["devoted"])}</p></div>')
    w("    </div>\n  </section>")

    # mating / life cycle
    m = f["mating"]
    w('  <section id="mating">\n    <h2>Mating &amp; young</h2>')
    w(f'    <p class="section-intro"><b>Form</b>{E(m["form"])}. <b style="margin-top:8px">Young</b>{E(m["young"])}, grown in {E(m["grows"])}. '
      f'<b style="margin-top:8px">Where</b>{E(m["where"])}. {E(m["note"])}</p>')
    w('    <div class="rules four">' + "".join(f'<div class="rule"><strong>{E(t)}</strong>{E(d)}</div>' for t, d in f["lifecycle"]) + "</div>")
    w("  </section>")

    # shared systems
    w('  <section id="systems">\n    <h2>Behaviour &amp; gameplay (shared by the family)</h2>')
    w('    <div class="rules">' + "".join(f'<div class="rule"><strong>{E(t)}</strong>{E(d)}</div>' for t, d in f["systems"]) + "</div>")
    w("  </section>")

    # split table
    w('  <section id="split">\n    <h2>Where the species split</h2>')
    head = "".join(f"<th>{E(s['name'])}</th>" for s in f["species"])
    rows = "".join(f"<tr><th>{E(r)}</th>" + "".join(f"<td>{E(c)}</td>" for c in cells) + "</tr>" for r, cells in f["split"])
    w(f'    <div class="table-wrap"><table><thead><tr><th>Trait</th>{head}</tr></thead><tbody>{rows}</tbody></table></div>')
    w("  </section>")

    # species profiles
    w('  <section id="species">\n    <h2>Species profiles</h2>\n    <div class="grid">')
    for s in f["species"]:
        sig_t, sig_d = s["signature"]
        sw = "".join(f'<span class="swatch"><strong>{E(n)}</strong> · {E(d)}</span>' for _, n, d in s["variants"])
        w(f'      <article class="card" id="{s["id"]}">')
        w(f'        <div class="card-top"><span class="group">{E(s["group"])}</span><span class="tag">{E(s["tag"])}</span></div>')
        w(f'        <h3>{E(s["name"])} <span style="color:var(--muted);font-size:.9rem">(about {s["height"]} blocks)</span></h3>')
        w(f'        <p><b>Temperament</b>{E(s["temperament"])}</p>')
        w(f'        <p><b>Ecology &amp; behaviour</b>{E(s["ecology"])}</p>')
        w(f'        <p><b>Signature: {E(sig_t)}</b>{E(sig_d)}</p>')
        w(f'        <p><b>Player interaction</b>{E(s["interaction"])}</p>')
        w(f'        <p><b>Colour variants</b></p><div class="swatches">{sw}</div>')
        w(f'        <p><b>Spawns in</b><code>{E(", ".join(s["biomes"]))}</code></p>')
        w(f'        <p class="risk"><b>Danger</b>{E(s["danger"])}</p>')
        w("      </article>")
    w("    </div>\n  </section>")

    # concept models
    blocks = []
    for s in f["species"]:
        pics = img(f"{s['id']}_preview.png", f"{s['name']} turnaround and poses") + img(f"{s['id']}_preview_face.png", f"{s['name']} faces") + \
            img(f"{s['id']}_variants.png", f"{s['name']} colour variants: " + ", ".join(n for _, n, _ in s["variants"]))
        if pics:
            blocks.append(f'    <h3 id="{s["id"]}-concept" style="margin-top:34px;font-size:1.7rem">{E(s["name"])} &mdash; concept model</h3>\n    {pics}')
    w('  <section id="concept-models">\n    <h2>Concept models</h2>')
    if blocks:
        w(f'    <p class="section-intro">Generated by <code>tools/gen_{fid}.py</code> on the shared <code>tools/girl_lib.py</code> (same 4-texel detail as the harpies and anurans). '
          "The images are renders of the real GeckoLib files. Colour variants are texture-only.</p>")
        w("\n".join(blocks))
    else:
        w('    <p class="section-intro">Models not generated yet.</p>')
    w("  </section>")

    # in the game
    built, todo = C.STATUS[fid]
    w('  <section id="status">' + NL + '    <h2>In the game</h2>' + NL + '    <div class="grid">')
    w('      <div class="card"><h3>Built (2026-10-19)</h3><ul>' + "".join(f"<li>{E(x)}</li>" for x in built) + "</ul></div>")
    w('      <div class="card"><h3>Not yet</h3><ul>' + "".join(f"<li>{E(x)}</li>" for x in todo) + "</ul></div>")
    w("    </div>" + NL + "  </section>")

    # decisions
    w('  <section id="decisions">\n    <h2>Design decisions log</h2>\n    <div class="grid">')
    w('      <div class="card"><h3>Decided (2026-10-19)</h3><ul>' + "".join(f"<li>{E(x)}</li>" for x in f["decisions"]) + "</ul></div>")
    w('      <div class="card"><h3>Open questions</h3><ul>' + "".join(f"<li>{E(x)}</li>" for x in f["questions"]) + "</ul></div>")
    w("    </div>\n  </section>")
    w('  <footer>Concept document · Minecraft-style fantasy ecology · MonsterGirls mod · the numbers here are the ones the game uses.</footer>')
    w("</main>\n</body>\n</html>\n")
    path = os.path.join(REPO, fname)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write("\n".join(out))
    print("wrote", fname, f"({os.path.getsize(path) // 1024} KB)")


if __name__ == "__main__":
    wanted = sys.argv[1:]
    for fam in C.FAMILIES:
        if not wanted or fam["id"] in wanted:
            build(fam)
