#!/usr/bin/env python3
"""Lang lines for the 2026-11-13 run (blessings, the realm of the dead, the Sabbath, sea weddings, ghosts, cooking, dullahans,
cursed gear, the repeadead). Adds only missing keys; long guides are split by split_guides. Can be run twice."""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_milk_assets  # noqa: E402

G = "journal.harmontide.guide."
M = "message.harmontide."

BLESSINGS = {
    M + "blessing.growth": "Your wife's kind has blessed the home: the crops round your hearth grow faster.",
    M + "blessing.herd": "Your wife's kind has blessed the home: the animals round your hearth grow up and thrive faster.",
    M + "blessing.catch": "Your wife's kind has blessed the home: luck follows you near the hearth (the fish bite better).",
    M + "blessing.ward": "Your wife's kind has blessed the home: monsters that come near your hearth are marked and weakened.",
    M + "blessing.hoard": "Your wife's kind has blessed the home: your hands work quicker near the hearth.",
    M + "blessing.soothe": "Your wife's kind has blessed the home: by the hearth your wounds close gently on their own.",
    G + "blessings.title": "Wives bless the land",
    G + "blessings": "In the old stories a monster girl changes the place she makes her married home. Each wife near your hearth brings it her kind's blessing, and two or three wives of the same blessing make it stronger. "
                     "Plant folk, fae, birds and the small grazers make the crops round the hearth grow. Hoofed and horned kinds make young animals grow up and parents ready sooner. "
                     "Sea, river and shore kinds bring you luck at home, so the fish bite better. Hunters, the made, riders and the dead mark and weaken monsters that come close. "
                     "Dragons, serpents and spinners quicken your hands (Haste). Devils and slimes close your wounds by the fire.",
}

SABBATH = {
    M + "sabbath.rank.initiate": "Word of you has reached the Sabbath. You are its initiate now: witches will no longer turn on you.",
    M + "sabbath.rank.adept": "The Sabbath counts you an adept. Offer a witch an empty hand and she will open her table to you.",
    M + "sabbath.rank.keeper": "The Sabbath trusts you with a familiar of your own.",
    M + "sabbath.rank.sibling": "The Sabbath calls you its sibling. Its title is yours to carry.",
    M + "sabbath.familiar": "A black cat with knowing eyes pads out of the dark and settles at your feet: your familiar.",
    "entity.harmontide.sabbath_witch": "The Sabbath's Table",
    "entity.harmontide.sabbath_familiar": "%s's Familiar",
    "title.harmontide.sabbath_sibling": ", Sibling of the Sabbath",
    G + "sabbath.title": "The Sabbath",
    G + "sabbath": "The witches' Sabbath is the Demon Realm's own coven, and the Order's oldest rival. Whatever the Order holds against you raises your standing with the Sabbath, "
                   "and so does dancing at the full-moon sabbath, seeing a creature turned by the realm, and being wed by a realm censer. Tithes, mercy to the Order and holy water lower it. "
                   "As an initiate, witches leave you alone. As an adept, a witch offered an empty hand opens her table: heartfruit, weepcaps, censers, realm grass and brews. "
                   "A keeper is given a familiar, a black cat whose eyes you share at night; lose her and another comes at the next full moon. "
                   "And the Sabbath's sibling carries its title after their name.",
}
for rank, title, desc in (("initiate", "Initiate of the Sabbath", "Earn the witches' Sabbath's notice"),
                          ("adept", "Adept of the Sabbath", "Be welcome at a witch's table"),
                          ("keeper", "Keeper of a Familiar", "Be trusted with a familiar of your own"),
                          ("sibling", "Sibling of the Sabbath", "Rise as high as the Sabbath goes")):
    SABBATH[f"advancement.harmontide.sabbath_{rank}"] = title
    SABBATH[f"advancement.harmontide.sabbath_{rank}.desc"] = desc

SEA = {
    M + "sea_wedding": "%s officiates as the sea bishop and blesses your marriage to %s: the sea is yours to breathe now, as long as you are wed.",
    M + "mermaid_blood.given": "%s presses a small vial into your hand: a drop of her own blood. Mermaid blood, they say, keeps you strong.",
    M + "mermaid_blood.drunk": "The mermaid blood runs cold, then warm. You feel stronger, for good.",
    M + "mermaid_blood.again": "Mermaid blood only works once in a life.",
    "item.harmontide.mermaid_blood": "Mermaid Blood",
    "advancement.harmontide.sea_wedding": "Married to the Sea",
    "advancement.harmontide.sea_wedding.desc": "Be wed to a sea bride by the water, with a sea bishop officiating",
    "advancement.harmontide.mermaid_blood": "Blood of the Sea",
    "advancement.harmontide.mermaid_blood.desc": "Drink the blood a merfolk friend gave you",
    G + "sea.title": "Sea weddings",
    G + "sea": "Wed a merfolk or mollusk bride by a hearth with water beside it, and if another sea girl stands among the guests she officiates as the sea bishop. "
               "Her blessing lets you breathe the water of the sea, the beaches and the rivers and swim like a dolphin, for as long as you are wed. "
               "And once in your life a merfolk close friend may give you a vial of her blood when you pet her: drink it for a lasting extra heart.",
}

GHOSTS = {
    M + "ghost.in": "%s slips into you with a cold little shiver. The dark goes clear, and the monsters seem to forget you're there.",
    M + "ghost.out": "%s slips back out beside you, looking pleased with herself.",
    M + "ghost.tired": "%s has already been inside you today. She needs to gather herself first.",
    M + "ghost.prank": "Something cold slips into you: %s! You shiver and hiccup.",
    M + "ghost.giggle": "%s pops back out of you, giggling.",
    "advancement.harmontide.ghost_possession": "Two Souls, One Body",
    "advancement.harmontide.ghost_possession.desc": "Let a ghost friend slip into you",
    G + "ghosts.title": "Ghost possession",
    G + "ghosts": "Ghosts (yurei, banshees, poltergeists, lost souls, lloronas) like to be close to the one they love, as close as can be. "
                  "Crouch and offer a ghost friend an empty hand and she slips into you for a minute: you see in the dark, a faint glow hangs about you, "
                  "and monsters lose track of you. Each can do it once a day. Wild ghosts sometimes do it at night as a prank, leaving you shivering and hiccuping.",
}

COOKING = {
    "item.harmontide.dish.masterful": "%s's Famous %s",
    "item.harmontide.dish.good": "%s's Tasty %s",
    "item.harmontide.dish.plain": "%s's %s",
    "item.harmontide.dish.clumsy": "%s's Slightly Burnt %s",
    "item.harmontide.dish.hopeless": "%s's Charred Love %s",
    M + "cooking.masterful": "%s cooks it to perfection, and hands it over with a proud little flourish.",
    M + "cooking.good": "%s cooks it nicely for you.",
    M + "cooking.plain": "%s cooks it for you.",
    M + "cooking.clumsy": "%s cooks it... mostly. There's a lot of smoke.",
    M + "cooking.hopeless": "Something catches fire. %s hands you a smoking black lump, beaming. Made with love!",
    M + "cooking.busy": "%s is still cleaning up from last time.",
    M + "cooking.ate_burnt": "You eat every bite of it. %s looks like she might cry with happiness.",
    G + "cooking.title": "Who can't cook",
    G + "cooking": "Crouch and hand a friend something raw (meat, fish, a potato) and she cooks it for you, now and then. "
                   "Every girl has her own touch in the kitchen and it never changes: a masterful cook's dish fills you up and leaves you strong, "
                   "a good cook's heals a little, and a plain cook's is just dinner. And some can't cook at all: their burnt love food makes you queasy, "
                   "but eat it anyway while she's near and she'll be over the moon.",
}

DULLAHAN = {
    "item.harmontide.dullahan_head": "Dullahan's Head",
    "item.harmontide.dullahan_head.of": "%s's Head",
    M + "dullahan.pop": "%s startles, and her head pops right off and rolls away!",
    M + "dullahan.thanks": "%s takes her head back and settles it on her neck, very red in the face. Thank you...",
    G + "dullahan.title": "A dullahan's head",
    G + "dullahan": "A dullahan's head sits on her neck more by habit than anything. Startle her (a blow, a tease that flusters her, a stumble) and it pops off and rolls away. "
                    "Headless, she stumbles slowly after it. Catch it first and hand it back, and she'll be very grateful. Keep it, and it finds its own way home after a while.",
}

CURSED = {
    "item.harmontide.cursed_blade": "Cursed Blade",
    "item.harmontide.living_armor": "Living Armour",
    "item.harmontide.cursed.fondness": "Fondness: %s / %s",
    M + "cursed.whisper.blade.0": "...who are you? ...your hand is warm...",
    M + "cursed.whisper.blade.1": "...again. Cut again. I like the way you swing me...",
    M + "cursed.whisper.blade.2": "...don't put me down. Don't ever put me down...",
    M + "cursed.whisper.blade.3": "...I'm yours. Only yours. Soon you'll see me...",
    M + "cursed.whisper.armor.0": "...a heartbeat... how long since I held a heartbeat...",
    M + "cursed.whisper.armor.1": "...let them strike. I'll take it. I'll take all of it...",
    M + "cursed.whisper.armor.2": "...stay inside me a little longer. It's so warm...",
    M + "cursed.whisper.armor.3": "...I want to hold you with real arms...",
    M + "cursed.step_out.blade": "The black blade shudders, and %s steps out of it, her own sword in her hand. She doesn't plan to leave your side.",
    M + "cursed.step_out.armor": "The breastplate grows warm, then light, and %s steps out of it, armoured head to foot. She bows to you.",
    M + "repeadead.scream": "%s screams! You can't move!",
    "advancement.harmontide.cursed_gear": "It Loved You Back",
    "advancement.harmontide.cursed_gear.desc": "Use cursed gear until its spirit steps out to meet you",
    "advancement.harmontide.repeadead_scream": "Frozen Stiff",
    "advancement.harmontide.repeadead_scream.desc": "Be frozen by a repeadead's scream",
    G + "cursed.title": "Cursed gear",
    G + "cursed": "Now and then the chests of old places (dungeons, mineshafts, strongholds, temples, mansions, ancient cities) hold cursed gear: a black blade or a living breastplate, each with a spirit in it. "
                  "They work as well as iron, and they grow fond of whoever uses them: the blade with every kill, the armour with every blow it takes for you and every minute you wear it. They whisper to you as they do. "
                  "At full fondness the spirit steps out as a girl, devoted to you from the first: the living armour shrugs off blows and goes for whatever hurts you, and the cursed blade's strikes heal her.",
    G + "repeadead.title": "The repeadead",
    G + "repeadead": "In dark forests, swamps and the deep dark she stands or crouches stock-still, her mourning mask frozen in horror. Walk in front of her and she screams: "
                     "you can't move while she comes for you and clings on, drinking your life. Hit her while she holds you to break free. Befriended, she leaves friendly creatures be "
                     "and turns her scream and her grip on the monsters around you.",
}

FOODWEB = {
    G + "foodweb.title": "The food web",
    G + "foodweb": "Wild girls feed themselves as their diet says. Hunters bring down the wild animals of their size (rabbits and chickens for the small, sheep, pigs and goats for most, cows for the big ones), "
                   "eat the meat where it fell and leave the bones and hide. Fishers take fish and squid. Grazers crop the grass and flowers, and where many graze together the turf wears to bare dirt. "
                   "Scavengers go where something died and eat what was left. Where the land is eaten out they move on, and thinned-out herds come back over time when no hunter is about. "
                   "Your animals are safe: nothing tame, named, leashed or penned, nothing in a village or near a hearth. Befriended girls are fed by you and don't hunt.",
}

ADVANCEMENTS = {"cursed_gear": "harmontide:cursed_blade", "repeadead_scream": "minecraft:skeleton_skull", "ghost_possession": "minecraft:soul_lantern", "sea_wedding": "minecraft:nautilus_shell", "mermaid_blood": "harmontide:mermaid_blood", "sabbath_initiate": "minecraft:glass_bottle", "sabbath_adept": "minecraft:potion", "sabbath_keeper": "minecraft:black_wool",
                "sabbath_sibling": "harmontide:realm_censer"}

ALL = {}
for part in (BLESSINGS, SABBATH, SEA, GHOSTS, COOKING, DULLAHAN, CURSED, FOODWEB):
    ALL.update(part)


def advancements():
    import json
    for name, icon in ADVANCEMENTS.items():
        path = os.path.join(gen_milk_assets.DATA, "advancement", name + ".json")
        if os.path.exists(path):
            continue
        data = {"parent": "minecraft:adventure/root",
                "display": {"icon": {"id": icon}, "title": {"translate": f"advancement.harmontide.{name}"},
                            "description": {"translate": f"advancement.harmontide.{name}.desc"},
                            "frame": "challenge" if name.endswith("sibling") else "task", "show_toast": True, "announce_to_chat": True, "hidden": False},
                "criteria": {"done": {"trigger": "minecraft:impossible"}}}
        with open(path, "w", encoding="utf-8", newline=chr(10)) as fh:
            json.dump(data, fh, indent=2)
            fh.write(chr(10))


def main(only=None):
    gen_milk_assets.add_lang(only if only is not None else ALL)
    advancements()


if __name__ == "__main__":
    main()
