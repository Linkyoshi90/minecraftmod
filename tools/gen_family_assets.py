#!/usr/bin/env python3
"""Many spouses, one happy family (2026-11-09, entity/Marriage): lang, two advancements, the marriage guide.

    uv run --no-project --with pillow python tools/gen_family_assets.py
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS, DATA, dump  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402
import split_guides  # noqa: E402

LANG = {
    "message.monstergirls.marriage.blessing.0": "%s nods and grins at %s: \"Welcome to the family!\"",
    "message.monstergirls.marriage.blessing.1": "%s claps and hugs %s: \"I knew you'd ask her!\"",
    "message.monstergirls.marriage.blessing.2": "%s gives %s a wink: \"There's always room for one more\"",
    "message.monstergirls.marriage.bridesmaids": "%s of your wives stood by %s as her bridesmaids, scattering petals",
    "message.monstergirls.marriage.warmth.0": "%s smiles, watching you with %s",
    "message.monstergirls.marriage.warmth.1": "%s leans in: \"Don't forget me!\" and laughs with %s",
    "message.monstergirls.marriage.warmth.2": "%s hums happily to see %s so content",
    "message.monstergirls.marriage.comfort.one": "%s holds you close. You are not alone",
    "message.monstergirls.marriage.comfort.many": "%s and %s more of your family hold you close. You are not alone",
    "message.monstergirls.marriage.family_bed.0": "You wake in a warm tangle of %s wives, all still smiling in their sleep",
    "message.monstergirls.marriage.family_bed.1": "Morning: %s wives, one blanket, and nobody wants to get up first",
    "message.monstergirls.marriage.family_bed.2": "%s wives fuss over who gets to make you breakfast. Everyone wins",
    "message.monstergirls.marriage.bouquet": "%s tosses her bouquet over her shoulder, and %s catches it! She peeks at you and turns red",
    "message.monstergirls.marriage.bouquet.daughter": "%s tosses her bouquet over her shoulder, and %s catches it! She looks round for a certain someone in the village",
    "message.monstergirls.marriage.figurine": "A little wedding figurine of %s in her veil, for the mantelpiece",
    "message.monstergirls.marriage.presents": "%s guests left wedding presents by the hearth",
    "message.monstergirls.marriage.realm": "Violet smoke curls round you both as %s whispers vows the Demon Realm keeps. You feel it in your blood",
    "advancement.monstergirls.realm_wedding": "Wed in Violet Smoke",
    "advancement.monstergirls.realm_wedding.desc": "Marry by a lit realm censer instead of a hearth",
    "message.monstergirls.birth": "%s's time has come: welcome the newest of your family!",
    "message.monstergirls.birth.twins": "%s's time has come, twice over: twins for your family!",
    "message.monstergirls.marriage.she_asks": "%s fumbles in her pocket and presses a ring into your hand: \"I... I don't want to wait any more. Will you marry me?\" Give it back to her to say yes",
    "message.monstergirls.marriage.lunch.0": "%s presses a wrapped lunch into your hands: \"Don't skip it this time!\"",
    "message.monstergirls.marriage.lunch.1": "%s packed you lunch, and drew a little heart on the wrapping",
    "message.monstergirls.marriage.lunch.2": "%s catches you at the door with your lunch: \"Come home safe, okay?\"",
    "item.monstergirls.packed_lunch": "%s's Packed Lunch",
    "item.monstergirls.packed_lunch.lore": "Made with love",
    "jade.monstergirls.wife": "♥ Your wife",
    "jade.monstergirls.wife.honeymoon": "♥ Your wife (honeymoon)",
    "jade.monstergirls.engaged": "♥ Engaged to you",
    "jade.monstergirls.craving": "Craving: %s",
    "message.monstergirls.marriage.welcome.0": "%s spots you and comes running: \"You're back! I missed you!\"",
    "message.monstergirls.marriage.welcome.1": "%s throws her arms round you: \"Took you long enough!\"",
    "message.monstergirls.marriage.welcome.2": "%s smiles warmly: \"Welcome home, dear\"",
    "item.monstergirls.her_ring": "%s's Ring",
    "message.monstergirls.marriage.flourish.sunshower": "Rain falls from a clear sky as %s says her vows: a fox's wedding",
    "message.monstergirls.marriage.flourish.feathers": "%s fluffs up so happily that feathers fly everywhere",
    "message.monstergirls.marriage.flourish.bubbles": "Bubbles rise round %s like a sea-folk blessing",
    "message.monstergirls.marriage.flourish.flame": "%s breathes a plume of joyful flame into the sky",
    "message.monstergirls.marriage.flourish.souls": "Soul fire dances round %s: the ancestors approve",
    "message.monstergirls.marriage.flourish.splash": "%s is so happy she wobbles all over you",
    "journal.monstergirls.children.title": "Our little ones",
    "journal.monstergirls.children.line": "%s, %s (born day %s)",
    "journal.monstergirls.children.line_mother": "%s, %s (born day %s, to %s)",
    "journal.monstergirls.family.next": "♥ %s, %s (wed day %s; anniversary in %s days)",
    "journal.monstergirls.family.today": "♥ %s, %s (wed day %s; it's your anniversary today!)",
    "jade.monstergirls.due": "Due in about %s min",
    "message.monstergirls.marriage.dream": "%s yawns: \"I dreamt of %s, off to the %s... about %s blocks. Can we go?\"",
    "message.monstergirls.marriage.dream.place.oasis": "an oasis with date palms",
    "message.monstergirls.marriage.dream.place.island": "an island floating in the sky",
    "message.monstergirls.marriage.dream.place.shrine": "a fox shrine with red gates",
    "message.monstergirls.marriage.dream.place.inn": "a cosy inn by the road",
    "message.monstergirls.marriage.dream.place.grotto": "a mermaid grotto",
    "message.monstergirls.marriage.dream.place.aerie": "a harpy aerie up high",
    "message.monstergirls.marriage.dream.place.camp": "a centaur camp",
    "message.monstergirls.marriage.dream.place.labyrinth": "a stone labyrinth",
    "message.monstergirls.marriage.dream.place.lair": "a dragon's lair",
    "message.monstergirls.marriage.dream.place.den": "a silk-hung den",
    "commands.monstergirls.family.none": "No wives or little ones yet.",
    "commands.monstergirls.family.header": "Your family: %s wives, %s little ones",
    "commands.monstergirls.family.wife": " ♥ %s: %s (anniversary in %s days)",
    "commands.monstergirls.family.child": " %s, %s: %s",
    "commands.monstergirls.family.near": "%s blocks %s",
    "commands.monstergirls.family.elsewhere": "in %s",
    "commands.monstergirls.family.far": "not near",
    "commands.monstergirls.family.little": "little",
    "commands.monstergirls.family.grown": "grown",
    "commands.monstergirls.family.unknown": "not seen lately",
    "message.monstergirls.happy_news": "%s is glowing this morning: she's expecting!",
    "message.monstergirls.marriage.family_painting": "Your wives press a big painting into your arms: \"The Whole Family\". There's room for everyone on the wall",
    "message.monstergirls.marriage.group": "A wedding of %s brides at once! The whole hearth is one big celebration",
    "journal.monstergirls.notes.title": "Notes from home",
    "journal.monstergirls.notes.line": "%s: \"%s\"",
    "journal.monstergirls.notes.0": "Don't forget to eat properly!",
    "journal.monstergirls.notes.1": "The bed is cold without you. Come home soon",
    "journal.monstergirls.notes.2": "I kept your seat by the fire warm",
    "journal.monstergirls.notes.3": "I learned a new recipe. You're trying it first!",
    "journal.monstergirls.notes.4": "Bring me something pretty back? Just kidding. Just come back",
    "journal.monstergirls.notes.5": "We all miss you. Yes, even the chickens",
    "journal.monstergirls.notes.6": "I counted the days. Then I stopped counting and started baking",
    "journal.monstergirls.notes.7": "Wear something warm, it's cold out there",
    "journal.monstergirls.notes.8": "Whatever you're doing, be careful. We need you",
    "journal.monstergirls.notes.9": "Welcome home! There's a hug waiting for you",
    "commands.monstergirls.family.look": "Look at a monster girl (within eight blocks) first.",
    "commands.monstergirls.family.due": "%s's time has come.",
    "commands.monstergirls.family.not_little": "Look at a little one first.",
    "commands.monstergirls.family.grown_some": "%s is a quarter of the way more grown up.",
    "message.monstergirls.marriage.perfect": "And you asked %s right here, at her kind of place. She's never going to forget this",
    "advancement.monstergirls.perfect_proposal": "The Perfect Proposal",
    "advancement.monstergirls.perfect_proposal.desc": "Propose to a girl at her favourite kind of place",
    "message.monstergirls.marriage.daughter_engaged": "%s has asked for your daughter %s's hand, and she said yes!",
    "message.monstergirls.marriage.dream_true": "%s gasps and takes your hand: \"It's just like my dream!\" You found %s, together",
    "advancement.monstergirls.dream_come_true": "A Dream Come True",
    "advancement.monstergirls.dream_come_true.desc": "Go with your wife to the place she dreamt of",
    "journal.monstergirls.children.line_grand": "%s, %s (born day %s, your grandchild by %s)",
    "message.monstergirls.flirt_wife.0": "%s leans into your hand: \"Mm. My favourite person\"",
    "message.monstergirls.flirt_wife.1": "%s steals a quick kiss on your cheek and pretends nothing happened",
    "message.monstergirls.flirt_wife.2": "%s twirls her ring: \"Still can't believe you're mine\"",
    "message.monstergirls.flirt_wife.3": "%s: \"Careful, dear, the others will want one too\" She giggles",
    "message.monstergirls.flirt_wife.4": "%s hums and laces her fingers through yours for a moment",
    "message.monstergirls.flirt_wife.5": "%s whispers: \"Come to bed early tonight?\" and goes pink",
    "message.monstergirls.marriage.anniversary_today": "Today is your wedding anniversary with %s. Don't keep her waiting!",
    "monstergirls.configuration.compliments": "Compliments",
    "monstergirls.configuration.herosWelcome": "Hero's welcome",
    "monstergirls.configuration.cravings": "Cravings",
    "monstergirls.configuration.sharedDreams": "Shared dreams",
    "monstergirls.configuration.packedLunch": "Packed lunch",
    "advancement.monstergirls.first_child": "A Little One of Your Own",
    "advancement.monstergirls.first_child.desc": "Welcome a little one born to you",
    "advancement.monstergirls.family_tree": "A Whole Family Tree",
    "advancement.monstergirls.family_tree.desc": "Ten little ones born to you",
    "message.monstergirls.portrait.gather": "Gather the family by the hearth first (two of them at least) for a portrait",
    "message.monstergirls.portrait.done": "Everyone squeezes in by the fire and holds still... a family portrait of %s!",
    "item.monstergirls.family_portrait.day": "Painted by the hearth on day %s",
    "message.monstergirls.marriage.anniversary_5": "Five years with %s! She gives you two diamonds: \"One for each of us\"",
    "message.monstergirls.marriage.anniversary_10": "Ten years with %s. She presses something golden into your hand and won't let go",
    "advancement.monstergirls.anniversary_5": "Five Happy Years",
    "advancement.monstergirls.anniversary_5.desc": "Celebrate a fifth wedding anniversary",
    "advancement.monstergirls.anniversary_10": "Golden Years",
    "advancement.monstergirls.anniversary_10.desc": "Celebrate a tenth wedding anniversary",
    "journal.monstergirls.family.title": "Family",
    "journal.monstergirls.family.children": "Little ones born to you: %s",
    "journal.monstergirls.family.line": "♥ %s, %s (wed day %s)",
    "journal.monstergirls.family.gone": "✝ %s, %s (wed day %s)",
    "advancement.monstergirls.wedding": "Happily Ever After",
    "advancement.monstergirls.wedding.desc": "Marry a monster girl at a hearth",
    "advancement.monstergirls.big_family": "One Big Happy Family",
    "advancement.monstergirls.big_family.desc": "Be married to three monster girls at once",
}

OLD_HEARTS = "while she is near you have two more hearts."
NEW_HEARTS = ("while she is near you have two more hearts (each spouse near adds two more, up to six). "
              "Many spouses are one happy family: a wife nearby blesses each new proposal, the wives are bridesmaids, "
              "a pat for one warms the others, two or more in your bed make a family bed, and if one is lost the rest hold you close. "
              "Guests bring wedding presents, and the first three days are a honeymoon. "
              "Wed by a lit realm censer instead and the Demon Realm keeps your vows. Propose at her kind of place (as on a date) and it's perfect. Of a morning, a wife you pass packs you lunch, and one who shared your bed may have dreamt of a real place nearby: go there together. Use a plain painting on your hearth with the family gathered round for a portrait with their names. A girl who adores you may not wait to be asked: after a lovely date she might hand you the ring herself.")


def adv(name, parent, icon, frame="task"):
    dump(os.path.join(DATA, "advancement", name + ".json"), {
        "parent": parent,
        "display": {"icon": {"id": icon}, "title": {"translate": f"advancement.monstergirls.{name}"},
                    "description": {"translate": f"advancement.monstergirls.{name}.desc"},
                    "frame": frame, "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})


def main():
    adv("wedding", "monstergirls:first_date", "monstergirls:wedding_ring")
    adv("big_family", "monstergirls:wedding", "minecraft:cake", "goal")
    adv("realm_wedding", "monstergirls:realm_censer", "monstergirls:realm_censer")
    adv("perfect_proposal", "monstergirls:first_date", "minecraft:poppy")
    adv("dream_come_true", "monstergirls:wedding", "minecraft:filled_map")
    adv("first_child", "monstergirls:wedding", "monstergirls:cradle")
    adv("anniversary_5", "monstergirls:wedding", "minecraft:diamond")
    adv("anniversary_10", "monstergirls:anniversary_5", "minecraft:enchanted_golden_apple", "challenge")
    adv("family_tree", "monstergirls:first_child", "minecraft:oak_sapling", "goal")
    add_lang(LANG)
    path = os.path.join(ASSETS, "lang", "en_us.json")
    with open(path, encoding="utf-8") as fh:
        text = fh.read()
    key = "journal.monstergirls.guide.marriage"
    old = json.loads(text)[key]
    start, end = old.index(OLD_HEARTS[:-1]), old.index(" Every forty days")
    new = old[:start] + NEW_HEARTS + old[end:]
    if new != old:
        a = json.dumps(key) + ": " + json.dumps(old, ensure_ascii=False)
        assert text.count(a) == 1
        text = text.replace(a, json.dumps(key) + ": " + json.dumps(new, ensure_ascii=False))
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            fh.write(text)
    split_guides.main()


if __name__ == "__main__":
    main()
