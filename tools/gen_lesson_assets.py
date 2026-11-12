#!/usr/bin/env python3
"""Lines for the lessons (entity/Lessons.java): what each branch teaches, in a message and in short for the journal page. Can be run twice.

    python tools/gen_lesson_assets.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402

LESSONS = {  # clade key: (short, what she teaches)
    "feliformia": ("Landing on your feet", "to land on your feet: a fall hurts only from higher up"),
    "canidae": ("A wolf's stride", "a wolf's stride: you walk a little faster"),
    "arctoidea": ("A bear's hardiness", "a bear's hardiness: one more heart"),
    "rodentia": ("Gnawing away", "to gnaw away at things: you break blocks a little faster"),
    "lagomorpha": ("A hare's spring", "a hare's spring: you jump a little higher"),
    "primates": ("A long reach", "to reach: you can place and break blocks a little farther off"),
    "chiroptera": ("Moving quietly", "to move quietly: you sneak faster"),
    "marsupialia": ("A good kick", "a good kick: your blows knock harder"),
    "xenarthra": ("Taking a blow", "to take a blow on the shoulder: a point of armour"),
    "paenungulata": ("Standing your ground", "to stand your ground: you are knocked back less"),
    "perissodactyla": ("Sure going", "sure going: snow, mud and soul sand slow you less"),
    "bovidae": ("A strong arm", "a strong arm: your blows hit harder"),
    "cervidae": ("Light landings", "to land lightly: a fall hurts only from higher up"),
    "camelidae": ("Heat", "to bear the heat: fire burns you for less long"),
    "suina": ("Rooting out luck", "to root out luck: a little luck, always"),
    "whippomorpha": ("A deep breath", "to take a deep breath: you hold it longer under water"),
    "aves": ("Light bones", "to fall like a bird: falls hurt less"),
    "archosauria": ("A thick hide", "a thick hide: your armour holds better"),
    "testudines": ("Tucking in", "to tuck in: blasts throw you less far"),
    "squamata": ("A quick strike", "a quick strike: you swing a little faster"),
    "amphibia": ("A frog's kick", "a frog's kick: you swim faster"),
    "actinopterygii": ("Working under water", "to work under water: you mine faster there"),
    "chelicerata": ("A long reach for prey", "a hunter's reach: you strike from a little farther off"),
    "hexapoda": ("Busy hands", "busy hands: your tools dig faster"),
    "malacostraca": ("A hard shell", "a hard shell: a point of armour"),
    "mollusca": ("Holding your breath", "to hold your breath: you last longer under water"),
    "flora": ("Putting down roots", "to put down roots: you are knocked back less"),
    "fae": ("Fae luck", "fae luck: luck is with you"),
    "dragon": ("A dragon's might", "a dragon's might: your blows hit harder"),
    "undead": ("Needing no breath", "to need less breath: you last longer under water"),
    "devil": ("Hellfire", "to shrug off fire: it burns you for less long"),
    "slime": ("Bouncing", "to bounce: falls hurt less"),
    "construct": ("Bracing", "to brace like a made thing: a point of armour"),
    "rider": ("A rider's swing", "a rider's swing: you swing a little faster"),
}

lang = {
    "message.harmontide.lesson": "%s, devoted to you, teaches you %s",
    "journal.harmontide.lessons.title": "Lessons",
    "journal.harmontide.lessons.count": "%s of %s branches",
    "journal.harmontide.lessons.line": "%s (%s)",
}
for k, (short, full) in LESSONS.items():
    lang[f"lesson.harmontide.{k}"] = full
    lang[f"lesson.harmontide.{k}.short"] = short
M.add_lang(lang)
print("lesson assets written")
