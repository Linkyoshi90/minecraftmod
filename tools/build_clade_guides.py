#!/usr/bin/env python3
"""Builds one field guide per clade (TAXONOMY_PLAN.md step 6): <clade>_clade_guide.html in the repo root, e.g.
feliformia_clade_guide.html. Each holds the branch's lede, its milk, which bodies (forms) live on it, and every member's
species card, taken as it is from her form guide, with two boxes added: her true position and her body (a link to the
form guide's life cycle and behaviour, which stands in for the per-form appendix). Harpies and anurans have no cards in
that format, so they get a short card linking to their guide.

    python tools/build_clade_guides.py [clade ...]

Re-run after the form guides or tools/taxonomy.py change. Needs the form guides in the repo root.
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
import build_tree_guide as TG  # noqa: E402
import taxonomy as T  # noqa: E402

REPO = B.REPO
E = html.escape

# The branch's lede: what binds it, and what the bodies hide.
LEDE = {
    "FELIFORMIA": "The cat-shaped half of the carnivores: true cats, the sphinxes and lamassu of legend, and the kin that don't look it. The hyena runs in a dog's body and the mongoose and meerkat in a rodent's, but all three sit on the cat side of the split. The pangolin lives here as a guest.",
    "CANIDAE": 'Wolves, dogs, jackals and every fox, the kitsune included: one family of long-legged runners, with the hellhounds of legend. Foxes were a family of their own in the old sorting; here they rejoin their cousins, and the tanuki, a raccoon dog, comes with them.',
    "ARCTOIDEA": "The other half of the dog-like carnivores: bears, weasels and badgers, raccoons and red pandas, and the seals and walruses "
                 "that took to the sea. Most arrived scattered across other families; they meet here.",
    "RODENTIA": "Gnawers: mice, squirrels, capybaras and their kin, recognised by the teeth that never stop growing.",
    "LAGOMORPHA": "Hares, rabbits and pikas, with the jackalope and her horned cousins. Close to the rodents, but a branch of their own.",
    "PRIMATES": "Monkeys, apes and lemurs, and the legends built on them. Hands that grip, eyes that face forward.",
    "CHIROPTERA": 'Every bat, from fruit bats to vampires: the only mammals that truly fly. The hedgehog and the mole live here as guests, their nearest kin among ours.',
    "MARSUPIALIA": "Pouched mammals: kangaroos, koalas, wombats and opossums. Their young finish growing in the pouch.",
    "XENARTHRA": "Armadillos, sloths and anteaters, an old American branch with strange joints in the spine.",
    "PAENUNGULATA": "Elephants, sea cows and hyraxes: an odd-looking family that is closer kin than it seems.",
    "PERISSODACTYLA": "Odd-toed hoofed beasts: horses, zebras, tapirs and rhinos, and the centaurs, unicorns and pegasi of legend.",
    "BOVIDAE": "Cattle, sheep, goats and antelope: horns that are never shed. Most minotaurs live here.",
    "CERVIDAE": "Deer, elk and reindeer, whose antlers fall each year, with the giraffes kept here as their nearest guests.",
    "CAMELIDAE": "Camels, llamas, alpacas and vicuñas: desert and mountain walkers.",
    "SUINA": "Pigs, boars, warthogs and peccaries.",
    "WHIPPOMORPHA": "Whales, dolphins and the hippos: the whales' closest living relatives walk on land. Orcas and narwhals share this page with the river horse.",
    "AVES": "Every bird, so every harpy: sparrows, crows, owls, ostriches and the phoenixes and garudas of legend. Birds are dinosaurs, but they get a guide of their own.",
    "ARCHOSAURIA": "Crocodiles, dinosaurs and pterosaurs: the ruling reptiles, birds set apart.",
    "TESTUDINES": "Turtles and tortoises, on land and at sea, shell and all.",
    "SQUAMATA": "Snakes and lizards: every lamia, the geckos and iguanas, the monitors and the great sea serpents.",
    "AMPHIBIA": 'Frogs, toads and salamanders: the anurans, the newts and salamanders who used to sit among the lizards, and the axolotls and olms who swam with the merfolk.',
    "ACTINOPTERYGII": "Ray-finned fish, and the mermaids of legend whose tails are a fish's: koi, piranhas, anglers, eels. Sharks, rays and the starfish live here as guests; none of them are ray-finned.",
    "CHELICERATA": "Spiders, scorpions and their kin: eight legs, no antennae. Every arachne, and the scorpion-tailed folk.",
    "HEXAPODA": 'Insects: bees, beetles, moths, mantises and ants. Six legs and a body in three parts. The centipede and the tardigrade are guests.',
    "MALACOSTRACA": 'Crabs, lobsters and shrimp, with the anomalocaris as an ancient guest.',
    "MOLLUSCA": 'Octopuses, squid, snails and nautiluses, and the krakens of legend. The jellyfish and the anemone drift with them as guests, and the sandworm burrows alongside.',
    "FLORA": "Plants and fungi, two kingdoms in one guide: flowers, trees, mushrooms and mosses. They give no milk.",
    "FAE": 'Elves, fairies, gnomes and spirits: people of legend with no one animal behind them. The fairies were bugs in the old sorting, the leshy a plant.',
    "DRAGON": "Dragons and wyrms of every land. A reptile body, a story bigger than any reptile.",
    "UNDEAD": "Ghosts, zombies, mummies and liches: whatever she was, she is this now.",
    "DEVIL": 'Demons, imps and succubi, and the oni, giants, trolls and yokai of many lands: creatures of legend with no one animal behind them.',
    "SLIME": "Slimes and jellies: a body that is all one thing.",
    "CONSTRUCT": "Golems, automatons and dolls: made, not born. They give no milk.",
    "RIDER": "The Four Riders and their like: a rider and her mount, one creature.",
}

MILK_NAME = {k: v[0] for k, v in T.SHORT.items()}
FORM_GUIDE = {}          # old family (form) -> its guide file


def form_guides():
    """old family -> guide file name, from the species each guide's cards carry (harpy and anuran fixed)."""
    out = dict(TG.FORM_GUIDE)
    where = TG.anchors()
    for sp, old in T.species_rows():
        if sp in where and old not in out:
            out[old] = where[sp]
    return out


def cards():
    """species id -> her card's HTML, from whichever form guide has it."""
    out = {}
    for f in sorted(glob.glob(os.path.join(REPO, "*_field_guide.html"))):
        text = open(f, encoding="utf-8").read()
        for m in re.finditer(r'<article class="card" id="([a-z0-9_]+)">.*?</article>', text, re.S):
            out.setdefault(m.group(1), m.group(0))
    return out


def build(k, members, lang, have, guides):
    name, path, milk, folded = T.FAM[k]
    mythic = k in TG.MYTHIC
    fname = k.lower() + "_clade_guide.html"
    forms = collections.Counter(old for _, old, _, _ in members)
    out = []
    w = out.append
    w('<!doctype html>\n<html lang="en">\n<head>\n<meta charset="utf-8">\n<meta name="viewport" content="width=device-width, initial-scale=1">')
    w(f"<title>{E(name)} — Harmontide Field Guide</title>\n<style>{B.CSS % TG.THEME}"
      ".place{border-top:1px solid var(--line);padding-top:10px}.place a{color:var(--accent)}"
      ".toc{display:flex;flex-wrap:wrap;gap:8px;margin:14px 0 0}.toc a{color:var(--muted);text-decoration:none;border:1px solid var(--line);border-radius:99px;padding:3px 10px;font-size:.8rem}"
      "\n</style>\n</head>\n<body>")
    w("<header>")
    w(f'  <div class="eyebrow">Harmontide · a field guide by branch · {"outside the tree" if mythic else E(path)}</div>')
    w(f"  <h1>{E(name)}</h1>")
    w(f'  <p class="lede">{E(LEDE[k])}</p>')
    w(f'  <p class="lede" style="font-size:.95rem"><b style="color:var(--accent)">Milk:</b> {E(milk)}'
      + (f' · <b style="display:inline;color:var(--accent)">Was:</b> {E(folded.title())}' if folded and folded != "new" else "") + "</p>")
    w('  <nav class="toc"><a href="tree_of_life.html">← The Tree of Life</a>'
      + "".join(f'<a href="{c.lower()}_clade_guide.html">{E(T.SHORT[c][0])}</a>' for c in T.FAM if c != k) + "</nav>")
    w("</header>\n<main>")
    w('  <section class="note" style="margin-top:0"><h2>Bodies on this branch</h2>')
    w('    <p class="section-intro">A girl\'s body (her form) decides how she moves, nests, bears young and behaves; that is in the guide for her form. '
      "Her branch decides her milk, the name on her cheese, her spawn-rate setting and where she sits in the journal's tree of life.</p><ul>")
    for old, n in forms.most_common():
        g = guides.get(old)
        label = E(old.title())
        w(f'      <li><b>{label} form · {n}</b>' + (f'<a href="{g}#mating" style="color:var(--accent)">life cycle</a> · '
                                                    f'<a href="{g}#systems" style="color:var(--accent)">behaviour</a>' if g else "") + "</li>")
    w("    </ul></section>")
    w(f'  <section id="species">\n    <h2>Every kind on the branch ({len(members)})</h2>\n    <div class="grid">')
    for sp, old, pos, flag in sorted(members, key=lambda r: lang.get("entity.monstergirls." + r[0], r[0])):
        g = guides.get(old, "")
        tag = {"guest": "A guest here: her true kin are too few, so she lives with her nearest relatives.",
               "call": "A judgement call: TAXONOMY_PLAN.md gives the reasons."}.get(flag, "")
        place = (f'<p class="place"><b>True position</b>{E(pos)}' + (f" {E(tag)}" if tag else "") + "</p>"
                 f'<p><b>Body</b>{E(old.title())} form' + (f' · <a href="{g}#{sp}">her card in that guide</a>' if g and sp in have else
                                                           (f' · <a href="{g}">its guide</a>' if g else "")) + "</p>")
        if sp in have:
            w("      " + have[sp].replace("</article>", "        " + place + "\n      </article>", 1))
        else:
            nm = E(lang.get("entity.monstergirls." + sp, sp.replace("_", " ").title()))
            w(f'      <article class="card" id="{sp}"><div class="card-top"><span class="group">{E(old.title())}</span></div><h3>{nm}</h3>{place}</article>')
    w("    </div>\n  </section>")
    w('  <footer>Generated by <code>tools/build_clade_guides.py</code> from <code>tools/taxonomy.py</code> and the form guides\' cards.</footer>')
    w("</main>\n</body>\n</html>\n")
    with open(os.path.join(REPO, fname), "w", encoding="utf-8", newline="\n") as fh:
        fh.write("\n".join(out))
    return fname


def main():
    assert set(LEDE) == set(T.FAM), set(T.FAM) ^ set(LEDE)
    wanted = {a.upper() for a in sys.argv[1:]}
    lang = json.load(open(os.path.join(REPO, "src/main/resources/assets/monstergirls/lang/en_us.json"), encoding="utf-8"))
    have, guides = cards(), form_guides()
    by = collections.OrderedDict((k, []) for k in T.FAM)
    for sp, old in T.species_rows():
        clade, pos, flag = T.place(sp, old)
        by[clade].append((sp, old, pos, flag))
    total = 0
    for k, members in by.items():
        if wanted and k not in wanted:
            continue
        f = build(k, members, lang, have, guides)
        total += os.path.getsize(os.path.join(REPO, f))
        print("wrote", f, len(members))
    print(f"{total // 1024} KB in all; {sum(1 for sp, _ in T.species_rows() if sp in have)} cards reused")


if __name__ == "__main__":
    main()
