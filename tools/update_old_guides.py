#!/usr/bin/env python3
"""One-off (idempotent) update of the two hand-written guides, harpy_field_guide.html and anuran_field_guide.html,
for the 2026-10-19 relationship rework and the new families: a volume bar, a shared relationship section, a
companions section per guide, and the outdated lines fixed. Safe to re-run: every insertion is marked.
    python tools/update_old_guides.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import build_field_guides as B  # noqa: E402

REPO = B.REPO
MARK = "<!-- relationships-2026-10-19 -->"

NAV_CSS = (".volumes{display:flex;flex-wrap:wrap;gap:8px;margin-top:18px}"
           ".volumes a{color:var(--muted);text-decoration:none;border:1px solid var(--line);border-radius:99px;padding:4px 11px;font-size:.82rem}"
           ".volumes a.here{color:var(--bg);background:var(--accent);border-color:var(--accent)}"
           ".table-wrap{overflow-x:auto;border:1px solid var(--line);border-radius:15px;margin:18px 0 10px}"
           "table{border-collapse:collapse;width:100%;min-width:640px;background:var(--panel)}"
           "th,td{text-align:left;vertical-align:top;padding:12px 16px;border-bottom:1px solid var(--line)}"
           "thead th{color:var(--accent);font-size:.8rem;text-transform:uppercase;letter-spacing:.06em;background:var(--panel2)}"
           "tbody th{color:var(--green);font-size:.82rem;text-transform:uppercase;letter-spacing:.05em;font-weight:700;width:16%}"
           "code{font-size:.9em;color:var(--green)}.card ul{padding-left:1.2rem;color:var(--muted)}")


def shared_section():
    rules = "".join(f'<div class="rule"><strong>{B.E(t)}</strong>{d}</div>' for t, d in B.SHARED_CORE)
    return (f'  <section style="margin-top:48px" id="relationships">\n    <h2>Relationships (shared by every family)</h2>\n'
            '    <p class="section-intro">Updated 2026-10-19. Every MonsterGirls family uses the same relationship core; each volume only '
            'changes how trust is won, what she gives back and how she bears young. The new families (volumes III to IX) are in their own guides.</p>\n'
            f'    <div class="rules">{rules}</div>\n    {B.tiers_table()}\n  </section>\n')


HARPY = """  <section style="margin-top:48px" id="companions">
    <h2>Nests, hatchlings &amp; companions</h2>
    <p class="section-intro">Updated 2026-10-19. What a harpy does once you have won her over. All of this is in the game.</p>
    <div class="grid">
      <div class="card"><h3>Mating &amp; the nest</h3><ul>
        <li><b>Bed</b>At high friendship (25) she may hop into your bed and lie down beside you (one night in three at 25, every night at 100). Sleep through the night and she is expecting.</li>
        <li><b>Expecting</b>One in-game day. Her belly rounds out as the day goes on (a belly bone added to every adult model).</li>
        <li><b>Nest &amp; clutch</b>When her time comes she picks a spot (her home if it will do, otherwise a dry, solid spot nearby), weaves a nest for a while, and lays a clutch of two to four fertilised eggs in it. The nest becomes her home, so she broods on it at night.</li>
        <li><b>Hatchlings</b>The eggs crack over about a day and hatch one hatchling each: her species, each in a random colour of it. Hatchlings grow up in five days.</li>
        <li><b>Born devoted</b>Every hatchling is devoted to the player who fathered the clutch: full affection, and her species' own bond on top (a crow treats that player as her companion, a phoenix is imprinted, an ostrich is loyal). A daughter never shares her father's bed.</li>
      </ul></div>
      <div class="card"><h3>Companions</h3><ul>
        <li><b>Sit</b>Right-click any harpy with a stick: she sits down (a new sit pose) until you do it again.</li>
        <li><b>Crow</b>At high friendship she sometimes brings raw iron or raw gold, a diamond in rare cases. With a chest, barrel or hopper nearby that already holds that item, she drops it in there instead of handing it over.</li>
        <li><b>Riflebird</b>Brings seeds straight into your inventory (now and then a torchflower seed or pitcher pod), on top of her unfertilised eggs.</li>
        <li><b>Owl</b>Brings food from the night's hunt: rabbit, chicken, cod, berries, apples.</li>
        <li><b>Seagull &amp; albatross</b>Bring what the sea gave up: fish, kelp, ink sacs, prismarine, turtle scutes, a nautilus shell, very rarely a heart of the sea.</li>
        <li><b>Phoenix</b>Her warmth heals the player she imprinted on: half a heart every two seconds within eight blocks.</li>
        <li><b>Sparrow</b>Roosts on a hopper and lays an unfertilised Sparrow Egg into it every five to ten minutes. Sparrow eggs can be eaten raw and bake into cakes and pumpkin pies like hen eggs (they also count as eggs for other mods: <code>c:eggs</code>).</li>
      </ul></div>
    </div>
  </section>
"""

ANURAN = """  <section style="margin-top:48px" id="companions">
    <h2>Spawn, tadpoles &amp; companions</h2>
    <p class="section-intro">Updated 2026-10-19. What a frog or toad does once you have won her over. All of this is in the game.</p>
    <div class="grid">
      <div class="card"><h3>Mating &amp; spawn</h3><ul>
        <li><b>Bed</b>At high friendship (25) she may hop into your bed and sit snug beside you (one night in three at 25, every night at 100). Sleep through the night and she is expecting.</li>
        <li><b>Expecting</b>One in-game day. Her belly rounds out as the day goes on (her belly bone swells).</li>
        <li><b>Spawn</b>She lays her spawn in the nearest water source: a still pond if there is one, otherwise any source block with open air above.</li>
        <li><b>Tadpoles &amp; froglets</b>The spawn hatches into two to four tadpoles of her species, coloured for the pond they hatch in. They grow legs after two and a half days (froglets) and are grown up after five.</li>
        <li><b>Born devoted</b>Every tadpole is devoted to the player who fathered the spawn (full affection) and never shares that player's bed. The Surinam toad still carries her young on her back instead.</li>
      </ul></div>
      <div class="card"><h3>Companions</h3><ul>
        <li><b>Sit</b>Right-click any frog or toad with a stick: she sits down until you do it again (tadpoles excepted).</li>
        <li><b>Bullywug</b>Every five to ten minutes she sheds a slime ball (only the bullywug herself, not the rest of her family).</li>
      </ul></div>
    </div>
  </section>
"""


def update(fname, companions, fixes):
    path = os.path.join(REPO, fname)
    s = open(path, encoding="utf-8").read()
    if MARK in s:
        print(fname, "already updated")
        return
    s = s.replace("</style>", NAV_CSS + "\n</style>", 1)
    i = s.index("</header>")
    s = s[:i] + "  " + B.volumes_nav(fname) + "\n" + s[i:]
    j = s.index("  <footer>")
    s = s[:j] + MARK + "\n" + shared_section() + companions + s[j:]
    for a, b in fixes:
        assert a in s, (fname, a[:60])
        s = s.replace(a, b, 1)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(s)
    print("updated", fname)


if __name__ == "__main__":
    update("harpy_field_guide.html", HARPY, [
        ("<strong>2. Nests & eggs</strong>Nests are habitat objects. Eggs are usually unfertilized; fertilization can be a special mechanic rather than a default breeding loop.",
         "<strong>2. Nests & eggs</strong>Nests are habitat objects. Eggs are unfertilised by default (riflebird, sparrow). Fertilised eggs come only from sharing a bed with a harpy who trusts you: she builds a nest and lays a clutch (see Nests, hatchlings &amp; companions)."),
        ("a harpy who spent the night beside someone she trusts builds a woven nest with one fertilised egg",
         "a harpy who spent the night beside someone she trusts builds a woven nest with a fertilised egg (since 2026-10-19 a clutch of two to four, each hatchling in a random colour)"),
        # the search only filters the species cards, not every card on the page
        ("const cards = [...document.querySelectorAll('.card')];", "const cards = [...document.querySelectorAll('#grid .card')];"),
    ])
    update("anuran_field_guide.html", ANURAN, [
        ("frogs and toads spawn on a still water source (hatchlings use the adult model at 40% scale, no tadpole stage yet)",
         "frogs and toads spawn on the nearest water source (a still pond first); the spawn hatches into tadpoles (see Spawn, tadpoles &amp; companions)"),
    ])
