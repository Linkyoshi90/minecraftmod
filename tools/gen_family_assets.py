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
    "message.harmontide.marriage.blessing.0": "%s nods and grins at %s: \"Welcome to the family!\"",
    "message.harmontide.marriage.blessing.1": "%s claps and hugs %s: \"I knew you'd ask her!\"",
    "message.harmontide.marriage.blessing.2": "%s gives %s a wink: \"There's always room for one more\"",
    "message.harmontide.marriage.bridesmaids": "%s of your wives stood by %s as her bridesmaids, scattering petals",
    "message.harmontide.marriage.warmth.0": "%s smiles, watching you with %s",
    "message.harmontide.marriage.warmth.1": "%s leans in: \"Don't forget me!\" and laughs with %s",
    "message.harmontide.marriage.warmth.2": "%s hums happily to see %s so content",
    "message.harmontide.marriage.comfort.one": "%s holds you close. You are not alone",
    "message.harmontide.marriage.comfort.many": "%s and %s more of your family hold you close. You are not alone",
    "message.harmontide.marriage.family_bed.0": "You wake in a warm tangle of %s wives, all still smiling in their sleep",
    "message.harmontide.marriage.family_bed.1": "Morning: %s wives, one blanket, and nobody wants to get up first",
    "message.harmontide.marriage.family_bed.2": "%s wives fuss over who gets to make you breakfast. Everyone wins",
    "message.harmontide.marriage.bouquet": "%s tosses her bouquet over her shoulder, and %s catches it! She peeks at you and turns red",
    "message.harmontide.marriage.bouquet.daughter": "%s tosses her bouquet over her shoulder, and %s catches it! She looks round for a certain someone in the village",
    "message.harmontide.marriage.figurine": "A little wedding figurine of %s in her veil, for the mantelpiece",
    "message.harmontide.marriage.presents": "%s guests left wedding presents by the hearth",
    "message.harmontide.marriage.realm": "Violet smoke curls round you both as %s whispers vows the Demon Realm keeps. You feel it in your blood",
    "advancement.harmontide.realm_wedding": "Wed in Violet Smoke",
    "advancement.harmontide.realm_wedding.desc": "Marry by a lit realm censer instead of a hearth",
    "message.harmontide.birth": "%s's time has come: welcome the newest of your family!",
    "message.harmontide.birth.twins": "%s's time has come, twice over: twins for your family!",
    "message.harmontide.marriage.she_asks": "%s fumbles in her pocket and presses a ring into your hand: \"I... I don't want to wait any more. Will you marry me?\" Give it back to her to say yes",
    "message.harmontide.marriage.lunch.0": "%s presses a wrapped lunch into your hands: \"Don't skip it this time!\"",
    "message.harmontide.marriage.lunch.1": "%s packed you lunch, and drew a little heart on the wrapping",
    "message.harmontide.marriage.lunch.2": "%s catches you at the door with your lunch: \"Come home safe, okay?\"",
    "item.harmontide.packed_lunch": "%s's Packed Lunch",
    "item.harmontide.packed_lunch.lore": "Made with love",
    "jade.harmontide.wife": "♥ Your wife",
    "jade.harmontide.wife.honeymoon": "♥ Your wife (honeymoon)",
    "jade.harmontide.engaged": "♥ Engaged to you",
    "jade.harmontide.craving": "Craving: %s",
    "message.harmontide.marriage.welcome.0": "%s spots you and comes running: \"You're back! I missed you!\"",
    "message.harmontide.marriage.welcome.1": "%s throws her arms round you: \"Took you long enough!\"",
    "message.harmontide.marriage.welcome.2": "%s smiles warmly: \"Welcome home, dear\"",
    "item.harmontide.her_ring": "%s's Ring",
    "message.harmontide.marriage.flourish.sunshower": "Rain falls from a clear sky as %s says her vows: a fox's wedding",
    "message.harmontide.marriage.flourish.feathers": "%s fluffs up so happily that feathers fly everywhere",
    "message.harmontide.marriage.flourish.bubbles": "Bubbles rise round %s like a sea-folk blessing",
    "message.harmontide.marriage.flourish.flame": "%s breathes a plume of joyful flame into the sky",
    "message.harmontide.marriage.flourish.souls": "Soul fire dances round %s: the ancestors approve",
    "message.harmontide.marriage.flourish.splash": "%s is so happy she wobbles all over you",
    "journal.harmontide.children.title": "Our little ones",
    "journal.harmontide.children.line": "%s, %s (born day %s)",
    "journal.harmontide.children.line_mother": "%s, %s (born day %s, to %s)",
    "journal.harmontide.family.next": "♥ %s, %s (wed day %s; anniversary in %s days)",
    "journal.harmontide.family.today": "♥ %s, %s (wed day %s; it's your anniversary today!)",
    "jade.harmontide.due": "Due in about %s min",
    "message.harmontide.marriage.dream": "%s yawns: \"I dreamt of %s, off to the %s... about %s blocks. Can we go?\"",
    "message.harmontide.marriage.dream.place.oasis": "an oasis with date palms",
    "message.harmontide.marriage.dream.place.island": "an island floating in the sky",
    "message.harmontide.marriage.dream.place.shrine": "a fox shrine with red gates",
    "message.harmontide.marriage.dream.place.inn": "a cosy inn by the road",
    "message.harmontide.marriage.dream.place.grotto": "a mermaid grotto",
    "message.harmontide.marriage.dream.place.aerie": "a harpy aerie up high",
    "message.harmontide.marriage.dream.place.camp": "a centaur camp",
    "message.harmontide.marriage.dream.place.labyrinth": "a stone labyrinth",
    "message.harmontide.marriage.dream.place.lair": "a dragon's lair",
    "message.harmontide.marriage.dream.place.den": "a silk-hung den",
    "commands.harmontide.family.none": "No wives or little ones yet.",
    "commands.harmontide.family.header": "Your family: %s wives, %s little ones",
    "commands.harmontide.family.wife": " ♥ %s: %s (anniversary in %s days)",
    "commands.harmontide.family.child": " %s, %s: %s",
    "commands.harmontide.family.near": "%s blocks %s",
    "commands.harmontide.family.elsewhere": "in %s",
    "commands.harmontide.family.far": "not near",
    "commands.harmontide.family.little": "little",
    "commands.harmontide.family.grown": "grown",
    "commands.harmontide.family.unknown": "not seen lately",
    "message.harmontide.happy_news": "%s is glowing this morning: she's expecting!",
    "message.harmontide.marriage.family_painting": "Your wives press a big painting into your arms: \"The Whole Family\". There's room for everyone on the wall",
    "message.harmontide.marriage.group": "A wedding of %s brides at once! The whole hearth is one big celebration",
    "journal.harmontide.notes.title": "Notes from home",
    "journal.harmontide.notes.line": "%s: \"%s\"",
    "journal.harmontide.notes.0": "Don't forget to eat properly!",
    "journal.harmontide.notes.1": "The bed is cold without you. Come home soon",
    "journal.harmontide.notes.2": "I kept your seat by the fire warm",
    "journal.harmontide.notes.3": "I learned a new recipe. You're trying it first!",
    "journal.harmontide.notes.4": "Bring me something pretty back? Just kidding. Just come back",
    "journal.harmontide.notes.5": "We all miss you. Yes, even the chickens",
    "journal.harmontide.notes.6": "I counted the days. Then I stopped counting and started baking",
    "journal.harmontide.notes.7": "Wear something warm, it's cold out there",
    "journal.harmontide.notes.8": "Whatever you're doing, be careful. We need you",
    "journal.harmontide.notes.9": "Welcome home! There's a hug waiting for you",
    "commands.harmontide.family.look": "Look at a monster girl (within eight blocks) first.",
    "commands.harmontide.family.due": "%s's time has come.",
    "commands.harmontide.family.not_little": "Look at a little one first.",
    "commands.harmontide.family.grown_some": "%s is a quarter of the way more grown up.",
    "message.harmontide.marriage.perfect": "And you asked %s right here, at her kind of place. She's never going to forget this",
    "advancement.harmontide.perfect_proposal": "The Perfect Proposal",
    "advancement.harmontide.perfect_proposal.desc": "Propose to a girl at her favourite kind of place",
    "message.harmontide.marriage.daughter_engaged": "%s has asked for your daughter %s's hand, and she said yes!",
    "message.harmontide.marriage.dream_true": "%s gasps and takes your hand: \"It's just like my dream!\" You found %s, together",
    "advancement.harmontide.dream_come_true": "A Dream Come True",
    "advancement.harmontide.dream_come_true.desc": "Go with your wife to the place she dreamt of",
    "journal.harmontide.children.line_grand": "%s, %s (born day %s, your grandchild by %s)",
    "message.harmontide.flirt_wife.0": "%s leans into your hand: \"Mm. My favourite person\"",
    "message.harmontide.flirt_wife.1": "%s steals a quick kiss on your cheek and pretends nothing happened",
    "message.harmontide.flirt_wife.2": "%s twirls her ring: \"Still can't believe you're mine\"",
    "message.harmontide.flirt_wife.3": "%s: \"Careful, dear, the others will want one too\" She giggles",
    "message.harmontide.flirt_wife.4": "%s hums and laces her fingers through yours for a moment",
    "message.harmontide.flirt_wife.5": "%s whispers: \"Come to bed early tonight?\" and goes pink",
    "message.harmontide.marriage.anniversary_today": "Today is your wedding anniversary with %s. Don't keep her waiting!",
    "harmontide.configuration.compliments": "Compliments",
    "harmontide.configuration.herosWelcome": "Hero's welcome",
    "harmontide.configuration.cravings": "Cravings",
    "harmontide.configuration.sharedDreams": "Shared dreams",
    "harmontide.configuration.packedLunch": "Packed lunch",
    "advancement.harmontide.first_child": "A Little One of Your Own",
    "advancement.harmontide.first_child.desc": "Welcome a little one born to you",
    "advancement.harmontide.family_tree": "A Whole Family Tree",
    "advancement.harmontide.family_tree.desc": "Ten little ones born to you",
    "message.harmontide.portrait.gather": "Gather the family by the hearth first (two of them at least) for a portrait",
    "message.harmontide.portrait.done": "Everyone squeezes in by the fire and holds still... a family portrait of %s!",
    "item.harmontide.family_portrait.day": "Painted by the hearth on day %s",
    "message.harmontide.marriage.anniversary_5": "Five years with %s! She gives you two diamonds: \"One for each of us\"",
    "message.harmontide.marriage.anniversary_10": "Ten years with %s. She presses something golden into your hand and won't let go",
    "advancement.harmontide.anniversary_5": "Five Happy Years",
    "advancement.harmontide.anniversary_5.desc": "Celebrate a fifth wedding anniversary",
    "advancement.harmontide.anniversary_10": "Golden Years",
    "advancement.harmontide.anniversary_10.desc": "Celebrate a tenth wedding anniversary",
    "journal.harmontide.family.title": "Family",
    "journal.harmontide.family.children": "Little ones born to you: %s",
    "journal.harmontide.family.line": "♥ %s, %s (wed day %s)",
    "journal.harmontide.family.gone": "✝ %s, %s (wed day %s)",
    "advancement.harmontide.wedding": "Happily Ever After",
    "advancement.harmontide.wedding.desc": "Marry a monster girl at a hearth",
    "advancement.harmontide.big_family": "One Big Happy Family",
    "advancement.harmontide.big_family.desc": "Be married to three monster girls at once",
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
        "display": {"icon": {"id": icon}, "title": {"translate": f"advancement.harmontide.{name}"},
                    "description": {"translate": f"advancement.harmontide.{name}.desc"},
                    "frame": frame, "show_toast": True, "announce_to_chat": True, "hidden": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})


def main():
    adv("wedding", "harmontide:first_date", "harmontide:wedding_ring")
    adv("big_family", "harmontide:wedding", "minecraft:cake", "goal")
    adv("realm_wedding", "harmontide:realm_censer", "harmontide:realm_censer")
    adv("perfect_proposal", "harmontide:first_date", "minecraft:poppy")
    adv("dream_come_true", "harmontide:wedding", "minecraft:filled_map")
    adv("first_child", "harmontide:wedding", "harmontide:cradle")
    adv("anniversary_5", "harmontide:wedding", "minecraft:diamond")
    adv("anniversary_10", "harmontide:anniversary_5", "minecraft:enchanted_golden_apple", "challenge")
    adv("family_tree", "harmontide:first_child", "minecraft:oak_sapling", "goal")
    add_lang(LANG)
    path = os.path.join(ASSETS, "lang", "en_us.json")
    with open(path, encoding="utf-8") as fh:
        text = fh.read()
    key = "journal.harmontide.guide.marriage"
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
