#!/usr/bin/env python3
"""Assets and lang lines for the 2026-11-27 run (getting about together: shoulder perches, silk bridges and swinging, harpies
carrying and catching, ducks and swans swimming and towing, mermaids towing divers, lamia coils, slime bounces; then the
vanilla-block jobs). Adds only missing lang keys; can be run twice.

    uv run --no-project --with pillow python tools/gen_run_2026_11_27.py
"""
import json
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets  # noqa: E402

ASSETS = gen_milk_assets.ASSETS
G = "journal.harmontide.guide."
M = "message.harmontide."


def write_json(rel, data):
    path = os.path.join(ASSETS, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(data, fh, indent=2)
        fh.write("\n")


def silk_bridge():
    """A sheet of silk: a few long strands each way, cross-threads between them, and gaps you can see through."""
    from PIL import Image, ImageDraw
    rnd = random.Random(1127)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for x in (1, 5, 8, 11, 14):                                   # the long lines across the gap
        d.line([(x, 0), (x + rnd.choice((-1, 0, 0, 1)), 15)], fill=(236, 236, 244, 235))
    for y in range(1, 16, 3):                                      # the weave between them
        d.line([(0, y), (15, y + rnd.choice((-1, 0, 1)))], fill=(214, 216, 228, 200))
    for _ in range(14):                                            # a few glints of dew
        img.putpixel((rnd.randrange(16), rnd.randrange(16)), (255, 255, 255, 255))
    path = os.path.join(ASSETS, "textures", "block", "silk_bridge.png")
    img.save(path)
    write_json("models/block/silk_bridge.json", {
        "parent": "minecraft:block/block",
        "render_type": "minecraft:cutout",
        "ambientocclusion": False,
        "textures": {"silk": "harmontide:block/silk_bridge", "particle": "harmontide:block/silk_bridge"},
        "elements": [{
            "from": [0, 0, 0], "to": [16, 1, 16],
            "faces": {
                "up": {"uv": [0, 0, 16, 16], "texture": "#silk"},
                "down": {"uv": [0, 0, 16, 16], "texture": "#silk"},
                "north": {"uv": [0, 15, 16, 16], "texture": "#silk"},
                "south": {"uv": [0, 15, 16, 16], "texture": "#silk"},
                "east": {"uv": [0, 15, 16, 16], "texture": "#silk"},
                "west": {"uv": [0, 15, 16, 16], "texture": "#silk"},
            },
        }],
    })
    write_json("blockstates/silk_bridge.json", {"variants": {"": {"model": "harmontide:block/silk_bridge"}}})


TRAVERSAL = {
    "block.harmontide.silk_bridge": "Silk Bridge",
    "block.harmontide.lava_crust": "Lava Crust",
    M + "safety_line": "%s holds you on her safety line: you float down unhurt.",
    M + "silk_bridge": "%s spins a silk bridge %s blocks long. It will hold for a few minutes.",
    M + "silk_bridge_tired": "%s is still spinning out the last bridge. Give her a moment.",
    M + "silk_bridge_none": "%s looks for a gap in front of you to bridge, and finds none.",
    M + "carry_rest": "%s needs to catch her breath before she can carry you again.",
    M + "carry_tiring": "%s's wings are tiring. She will have to set you down soon.",
    M + "harpy_caught": "%s swoops down and catches you!",
    M + "coiled": "%s coils around you to shield you. Crouch to slip out.",
    G + "traversal.title": "Getting about together",
    G + "traversal": "Many kinds help you get about once they like you enough. "
                     "A young jumping spider you are friends with hops onto your shoulder when you tell her to follow you, or when you crouch and hold out an empty hand. "
                     "While she rides there, falls never hurt you and cobwebs no longer hold you. A gliderune rides there too, and slows your falls. "
                     "Other tiny friends hop up when told to follow you: a fairy mends you a little, a pixie or a kesaran-pasaran brings luck, the waterlil cat lets you breathe under water, and the living doll lends you her night eyes. "
                     "A harpy hatchling who likes you rides your shoulder like a parrot: crouch and hold out an empty hand. "
                     "Crouch for two seconds to set any of them down. She stays down for a minute. "
                     "Crouch and hand an arachne some string, and she spins a silk bridge across the gap in front of you. It lasts a few minutes. "
                     "An arachne following you swings across on a silk line when she cannot walk to you. "
                     "A harpy who trusts you a lot lifts you when you crouch and hold out an empty hand. The big birds, such as rocs, albatrosses, swans and owls, fly you wherever you look for half a minute. "
                     "Most others can only glide you down, and hummingbirds and finches are too small. When she tires she sets you down, and if you let go you drift down slowly. "
                     "She also catches you if you fall past her. "
                     "Ducks and swans float and paddle about ponds. Take hold of one while you both swim and she tows you. "
                     "A mermaid takes your hand in the water and swims you anywhere you look, even far down, and you never run out of air. "
                     "A lamia who trusts you a lot coils around you when you are badly hurt near her: you take less damage and heal, and your attackers turn on her. "
                     "Land on a slime girl and you bounce back up unharmed. Crouch to sink in instead. "
                     "Sit in a minecart on a track while a centaur follows you, and she takes the front and pulls you along. "
                     "In the water, a capybara, turtle, water buffalo, hippo, beaver, polar bear or spinosaur friend lets you climb onto her back, and she swims you wherever you face. "
                     "An octopus, a squid girl, a kraken, a cuttlefish or a nautilus dives with you instead, wherever you look, and shares her air. "
                     "Crouch and hold out an empty hand to a big construct friend, a golem, a talosling, an automaton or a gargoyle: she tosses you up and forward, and you land unhurt. "
                     "Friends following you lend you a little of themselves: with a cat girl at your heels falls hurt half as much, a jackalope or a kangaroo puts a spring in your step, and a wolf or a hellhound sets the pack's pace when you sprint.",
}

JOBS = {
    "item.harmontide.crab_armour_piece": "Crab Armour Piece",
    M + "crab_shed": "%s has shed her shell. She gives you a piece of it.",
    M + "forest_given": "%s tucks away %s %s. She will plant them around here.",
    M + "forest_done": "%s has planted every sapling you gave her.",
    M + "shambler_company": "With %s beside you, the dead take you for one of their own.",
    G + "jobs.title": "Work about the home",
    G + "jobs": "Girls who are fond of you lend a hand with the blocks around a home. "
                "A dragon walks over to a furnace, blast furnace or smoker that has something to cook in it and breathes on it: it burns without fuel and cooks twice as fast while she stays. The water dragons cannot do this. "
                "Cattle minotaurs and holstaurs carry buckets of milk from a chest or barrel to a cheese shelf with room on it. They take the hard cheeses off the shelves and put them in a chest that already holds cheese. "
                "Bug girls take the honey from full hives without angering the bees, and calm bees that are angry. The honey goes into a glass bottle if a chest nearby has one, else it comes as honeycomb. Fairies keep to their fountains instead. "
                "Hand a rodent a stack of saplings and she plants them all around the spot where you gave them to her, a few blocks apart. "
                "The mooskuh fills the bowls in a chest near her with mushroom stew, one every couple of minutes. "
                "A holstaur bakes a cake, with her own milk in it, from the sugar, egg and wheat in a chest beside a crafting table. A brownie or a domovoi bakes bread from the wheat there. "
                "A slime girl fills an empty cauldron near her with what she is made of, once every five minutes: the undine with water, the frost slime with powder snow, the magma slime with lava. "
                "A befriended crab sheds her shell every few days and gives you a piece. At a smithing table, a crab armour piece next to any worn tool, weapon or armour mends half its wear. No template is needed. "
                "While a zombie girl walks with you, the undead and creepers take you for one of their own and leave you alone, until you hit one. "
                "Girls do this work while they are free or on patrol, not while they follow you or sit.",
}

INSTINCTS = {
    M + "labyrinth": "%1$s points %3$s. Home is %2$s blocks that way.",
    M + "fox_veil": "%s wraps you in an illusion. For a moment, nothing can see you.",
    M + "mummy_bandage": "%s binds your wounds.",
    M + "labyrinth_below": "%1$s points %3$s and down. Home is %2$s blocks that way, %4$s below you.",
    M + "labyrinth_above": "%1$s points %3$s and up. Home is %2$s blocks that way, %4$s above you.",
    M + "labyrinth_here": "%1$s stamps a hoof. You are home already.",
    "direction.harmontide.south": "south",
    "direction.harmontide.south_west": "south-west",
    "direction.harmontide.west": "west",
    "direction.harmontide.north_west": "north-west",
    "direction.harmontide.north": "north",
    "direction.harmontide.north_east": "north-east",
    "direction.harmontide.east": "east",
    "direction.harmontide.south_east": "south-east",
    G + "instincts.title": "Little instincts",
    G + "instincts": "Every kind has her own small ways. "
                     "A minotaur never loses her way: show a minotaur friend a compass and she points the way home, to your bed or else the world's spawn, and tells you how far. "
                     "At dusk a cat girl sometimes tears about for a few seconds for no reason at all. "
                     "An owl harpy at your side in the dark lends you her eyes, and you see as if by day. "
                     "On ice a penguin drops onto her belly and slides along three times as fast. "
                     "A gryphon or a wyrm who would let you ride her catches you on her back if you fall past her. "
                     "When you are badly hurt, a kitsune who trusts you a lot wraps you in an illusion: you vanish for a few seconds and whatever was after you loses you. "
                     "A mummy friend binds your wounds when you are down to half your health, and you heal quickly for a while.",
}

WORKSHOP = {
    M + "dwarf_anvil": "%s knows this work: she gives you %s levels back.",
    M + "dinner_bell": "%s friends come running at the bell.",
    M + "winged_catch": "%s catches you before you hit the ground.",
    G + "workshop.title": "Hands at the workbench",
    G + "workshop": "Some kinds know their way round your workshop. "
                    "An elf who is fond of anyone, standing near an enchanting table, knows what the bookshelves would tell it: the table offers enchantments as if fifteen stood around it. "
                    "A smith friend at your side, a dwarf, a cyclops, an iron kobold or an anvil tortoise, gives back half the levels an anvil job cost you. "
                    "A lich, or Koschei, keeps a brewing stand with potions and an ingredient in it going without blaze powder, and it brews twice as fast. "
                    "A dragon lights an unlit campfire as well as tending furnaces. "
                    "Ring a bell and your friends nearby who are free to come hurry over to it. "
                    "A winged friend, such as a valkyrie, an angel, a sylph, a phantom or a moth girl, catches you before you hit the ground. "
                    "A yuki-onna friend near you freezes the water under your feet as you walk across it. "
                    "A fire kind friend near you, a salamander, a blaze cat, a magma slime, an ifrit, a fire giant or a hellhound, cools the lava under your feet into a crust you can walk on. It melts back a few seconds later. "
                    "An arachne weaves the string in a chest beside a loom into white wool, four string to a block. "
                    "A wood or desert elf turns flint, sticks and feathers in a chest beside a fletching table into arrows, four at a time. "
                    "A sea snake or sea serpent lamia keeps you in her coils under the water too: she dives wherever you look, and you never run out of air.",
}

TIPS = {
    "tip.harmontide.string": "Crouch and hand it to an arachne friend: a silk bridge",
    "tip.harmontide.compass": "Show it to a minotaur friend: she points the way home",
    "tip.harmontide.sapling": "Hand a stack to a rodent friend: she plants them",
    "tip.harmontide.crab_armour_piece": "Mends half a worn item's wear at a smithing table, no template",
    "tip.harmontide.bell": "Ring it to call your friends nearby",
}

ADVANCEMENTS = {
    "shoulder_friend": ("minecraft:spider_eye", "A Friend on Your Shoulder", "Have a little friend ride on your shoulder"),
    "carried_aloft": ("minecraft:feather", "Up, Up and Away", "Be carried through the air by a harpy"),
    "caught_falling": ("minecraft:phantom_membrane", "Caught!", "Be caught by a winged friend while falling"),
    "ferried": ("minecraft:oak_boat", "Ferry Crossing", "Be towed or carried across the water by a friend"),
    "silk_bridge": ("minecraft:string", "Bridge of Silk", "Have an arachne spin you a bridge across a gap"),
}
ADV_LANG = {}
for _k, (_icon, _title, _desc) in ADVANCEMENTS.items():
    ADV_LANG["advancement.harmontide." + _k] = _title
    ADV_LANG["advancement.harmontide." + _k + ".desc"] = _desc


def advancements():
    for name, (icon, _t, _d) in ADVANCEMENTS.items():
        path = os.path.join(gen_milk_assets.DATA, "advancement", name + ".json")
        if os.path.exists(path):
            continue
        data = {"parent": "minecraft:adventure/root",
                "display": {"icon": {"id": icon}, "title": {"translate": f"advancement.harmontide.{name}"},
                            "description": {"translate": f"advancement.harmontide.{name}.desc"},
                            "frame": "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
                "criteria": {"done": {"trigger": "minecraft:impossible"}}}
        with open(path, "w", encoding="utf-8", newline=chr(10)) as fh:
            json.dump(data, fh, indent=2)
            fh.write(chr(10))


ALL = {}
for part in (TRAVERSAL, JOBS, INSTINCTS, WORKSHOP, TIPS, ADV_LANG):
    ALL.update(part)


def lava_crust():
    """Black, glassy basalt over lava, with glowing orange cracks."""
    from PIL import Image
    rnd = random.Random(2711)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    for y in range(16):
        for x in range(16):
            v = 28 + rnd.randrange(22)
            img.putpixel((x, y), (v, v - 4, v - 6, 255))
    for _ in range(5):                                              # cracks: short random walks
        x, y = rnd.randrange(16), rnd.randrange(16)
        for _ in range(7):
            img.putpixel((x % 16, y % 16), (250, 120 + rnd.randrange(60), 20, 255))
            x += rnd.choice((-1, 0, 1))
            y += rnd.choice((-1, 0, 1))
    img.save(os.path.join(ASSETS, "textures", "block", "lava_crust.png"))
    write_json("models/block/lava_crust.json", {"parent": "minecraft:block/cube_all", "textures": {"all": "harmontide:block/lava_crust"}})
    write_json("blockstates/lava_crust.json", {"variants": {"": {"model": "harmontide:block/lava_crust"}}})


def crab_armour_piece():
    """A crab's shed carapace plate, seen from above: spiked front edge, the H-shaped grooves, a dark rim and a highlight."""
    from PIL import Image
    shell, rim, light, groove = (214, 88, 48, 255), (128, 40, 26, 255), (246, 160, 112, 255), (170, 62, 36, 255)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))

    def inside(x, y):
        if y < 4 or y > 13:
            return False
        half = 7.0 if y <= 7 else 7.0 - (y - 7) * 0.9
        return abs(x - 7.5) <= half

    for y in range(16):
        for x in range(16):
            if inside(x, y):
                edge = not all(inside(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                img.putpixel((x, y), rim if edge else shell)
    for x in (1, 3, 5, 10, 12, 14):                                 # the spiky front edge
        img.putpixel((x, 3), rim)
    for y in range(6, 12):                                          # the H: a groove down the middle
        img.putpixel((7, y), groove)
        img.putpixel((8, y), groove)
    for x in range(4, 12):                                          # and one across
        img.putpixel((x, 8), groove)
    for (x, y) in ((4, 5), (5, 5), (4, 6), (10, 5), (11, 6)):      # a shine on the upper plates
        img.putpixel((x, y), light)
    img.save(os.path.join(ASSETS, "textures", "item", "crab_armour_piece.png"))
    write_json("models/item/crab_armour_piece.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "harmontide:item/crab_armour_piece"}})
    data = os.path.join(gen_milk_assets.DATA, "recipe", "crab_mend.json")
    with open(data, "w", encoding="utf-8", newline=chr(10)) as fh:
        json.dump({"type": "harmontide:crab_mend"}, fh, indent=2)
        fh.write(chr(10))


def main():
    silk_bridge()
    lava_crust()
    crab_armour_piece()
    advancements()
    gen_milk_assets.add_lang(ALL)


if __name__ == "__main__":
    main()
