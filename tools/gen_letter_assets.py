#!/usr/bin/env python3
"""Assets for letters home (entity/Letters.java): the advancement, the letter lines by country, the messages.

    python tools/gen_letter_assets.py

Can be run twice.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets as M  # noqa: E402
import gen_rarity_assets as R  # noqa: E402  (writes its own files again, harmlessly)

R.advancement("pen_pal", "minecraft:writable_book", "task", ["done"])

COUNTRIES = {
    "sea": ["The water out here is so clear I can see the bottom from the surface.",
            "I followed a pod of dolphins all morning. They're terrible at sharing fish.",
            "There's a sunken ship below me. I'm being brave and only looking."],
    "dark": ["The nights here are long and very quiet. I like it more than I thought.",
             "I found an old graveyard where nobody remembers anybody. I tidied it a little.",
             "A bat followed me for an hour. I think it wanted to be friends."],
    "wood": ["The trees here are older than any I've ever seen. They creak when they talk.",
             "I slept in a hollow log last night and woke up covered in moss. Lovely.",
             "The berries out here are the sweetest I've ever had. I saved you some."],
    "desert": ["The sand is so hot by day that I walk only at dawn and dusk.",
               "I found a temple half-buried in the dunes. I didn't touch the pressure plate.",
               "The stars over the desert are so bright you could read by them."],
    "east": ["The cherry trees are blooming. The petals get everywhere, even in my food.",
             "I stopped at a shrine and left an offering for your good luck.",
             "There's a hot spring up in the hills. I may have stayed in it far too long."],
    "swamp": ["It rained all day. I didn't mind at all.",
              "The frogs here sing all night. I joined in. They didn't mind either.",
              "I found a witch's hut. She wasn't home, so I left a note."],
    "ruins": ["I found old machinery under a hill, still ticking after all these years.",
              "Someone built a great wall here once. Only the copper is left, gone green.",
              "I counted the gears in a broken clock tower. There were four hundred and twelve."],
    "caves": ["The caves go down further than I dared to follow. I could hear water.",
              "There's a cavern full of glowing berries. It looks like the night sky upside down.",
              "I met a spider who was very polite, for a spider."],
    "plains": ["The grass is so tall here that I can hide in it standing up.",
               "I passed a village. The farmers waved, and one of them gave me bread.",
               "A herd of horses ran beside me for a while. I almost kept up."],
}
lang = {
    "letter.harmontide.dear": "Dear %s,",
    "letter.harmontide.enclosed": "I've tucked something from here into the envelope. Don't lose it!",
    "letter.harmontide.signed": "I miss you. Back soon,\n%s",
    "item.harmontide.letter": "A Letter from %s",
    "message.harmontide.letter": "A letter arrives from %s.",
    "journal.harmontide.letters": "Letters received: %s",
    "advancement.harmontide.pen_pal": "Pen Pal", "advancement.harmontide.pen_pal.desc": "Receive a letter from a friend on an expedition",
}
for c, lines in COUNTRIES.items():
    for i, line in enumerate(lines):
        lang[f"letter.harmontide.{c}.{i}"] = line
M.add_lang(lang)
print("letter assets written")
