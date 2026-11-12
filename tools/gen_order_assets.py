#!/usr/bin/env python3
"""The Order (2026-11-08, entity/order): the sisters' skins, spawn egg models, loot tables, advancements and lang.

Skins are 64x64 in the vanilla player layout (slim arms), painted box by box: a sister-knight (white tabard with a gold cross
over mail, chestnut hair in a braid), an inquisitrix (navy robe and hood, white collar, gold trim) and a captain (blonde,
white and gold with a sky-blue border). Knights and captains wear vanilla iron over theirs.

    uv run --no-project --with pillow python tools/gen_order_assets.py [--preview previews/order_sisters.png]
"""
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_realm_assets import ASSETS, DATA, dump, rgb, save, shade  # noqa: E402
from gen_milk_assets import add_lang  # noqa: E402

# --- the player skin layout ------------------------------------------------------------------------------------------
# box: (u, v, w, h, d). Faces: top, bottom, right, front, left, back.
BOXES = {
    "head": (0, 0, 8, 8, 8), "hat": (32, 0, 8, 8, 8),
    "body": (16, 16, 8, 12, 4), "jacket": (16, 32, 8, 12, 4),
    "rarm": (40, 16, 3, 12, 4), "rsleeve": (40, 32, 3, 12, 4),
    "larm": (32, 48, 3, 12, 4), "lsleeve": (48, 48, 3, 12, 4),
    "rleg": (0, 16, 4, 12, 4), "rpants": (0, 32, 4, 12, 4),
    "lleg": (16, 48, 4, 12, 4), "lpants": (0, 48, 4, 12, 4),
}


def faces(box):
    u, v, w, h, d = BOXES[box]
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
            "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + d + w + d, v + d, w, h)}


class Skin:
    def __init__(self):
        self.img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
        self.px = self.img.load()

    def put(self, x, y, c):
        self.px[x, y] = tuple(c) + ((255,) if len(c) == 3 else ())

    def fill(self, box, c, which=("top", "bottom", "right", "front", "left", "back")):
        for f in which:
            x0, y0, w, h = faces(box)[f]
            for y in range(y0, y0 + h):
                for x in range(x0, x0 + w):
                    self.put(x, y, c)

    def rows(self, box, face, y_from, y_to, c, x_from=0, x_to=None):
        """rows y_from..y_to-1 of a face (face-relative), columns x_from..x_to-1"""
        x0, y0, w, h = faces(box)[face]
        for y in range(y_from, y_to):
            for x in range(x_from, w if x_to is None else x_to):
                self.put(x0 + x, y0 + y, c)

    def ring(self, box, y_from, y_to, c):
        """rows of every side face (front, back, left, right)"""
        for f in ("front", "back", "left", "right"):
            self.rows(box, f, y_from, y_to, c)

    def dot(self, box, face, x, y, c):
        x0, y0, _, _ = faces(box)[face]
        self.put(x0 + x, y0 + y, c)


SKIN, SKIN_SH, BLUSH = rgb("#EEC6A6"), rgb("#D9A887"), rgb("#E8A0A0")
WHITE, WHITE_SH = rgb("#EEEAE0"), rgb("#C9C3B4")
GOLD, GOLD_SH = rgb("#D4A93A"), rgb("#9C7A22")
MAIL, MAIL_SH = rgb("#8C9098"), rgb("#62666E")
SKY, NAVY, NAVY_SH = rgb("#6FB7E0"), rgb("#26315E"), rgb("#18203F")
LEATHER, LEATHER_SH = rgb("#6B4528"), rgb("#4A2E1A")


def head(s, hair, eyes, fringe_rows=2):
    hair_sh = shade(hair, 0.75)
    s.fill("head", SKIN)
    s.fill("head", hair, ("top", "back"))
    s.rows("head", "right", 0, 8, hair)
    s.rows("head", "left", 0, 8, hair)
    s.rows("head", "right", 3, 6, SKIN, 2, 5)                          # ears show through
    s.rows("head", "left", 3, 6, SKIN, 3, 6)
    s.rows("head", "front", 0, fringe_rows, hair)                       # fringe
    s.dot("head", "front", 0, fringe_rows, hair)
    s.dot("head", "front", 7, fringe_rows, hair)
    s.rows("head", "front", fringe_rows, 7, hair, 0, 1)                 # locks framing the face
    s.rows("head", "front", fringe_rows, 7, hair, 7, 8)
    s.dot("head", "front", 3, 1, hair_sh)
    s.dot("head", "front", 5, 1, hair_sh)
    for x in (1, 5):                                                    # eyes: lash above, white and iris
        s.dot("head", "front", x, 3, shade(hair, 0.5))
        s.dot("head", "front", x + 1, 3, shade(hair, 0.5))
    s.dot("head", "front", 1, 4, WHITE)
    s.dot("head", "front", 2, 4, eyes)
    s.dot("head", "front", 5, 4, eyes)
    s.dot("head", "front", 6, 4, WHITE)
    s.dot("head", "front", 1, 5, BLUSH)
    s.dot("head", "front", 6, 5, BLUSH)
    s.dot("head", "front", 4, 5, SKIN_SH)                               # a hint of nose
    s.dot("head", "front", 3, 6, rgb("#C46E6E"))                        # lips
    s.dot("head", "front", 4, 6, rgb("#C46E6E"))
    s.rows("head", "bottom", 0, 8, SKIN_SH)
    for y in range(8):                                                  # the back of the hair, a little texture
        for x in range(8):
            if (x + y) % 3 == 0:
                s.dot("head", "back", x, y, hair_sh)


def hands(s, sleeve_rows):
    for arm in ("rarm", "larm"):
        s.ring(arm, sleeve_rows, 12, SKIN)
        s.rows(arm, "bottom", 0, 4, SKIN_SH)


def tabard(s, cloth, cloth_sh, cross, border=None):
    """a sleeveless tabard on the body: cloth front and back, a cross on the chest, a belt"""
    s.fill("body", cloth)
    s.rows("body", "right", 0, 12, cloth_sh)
    s.rows("body", "left", 0, 12, cloth_sh)
    s.rows("body", "front", 1, 11, cross, 3, 5)                          # the cross
    s.rows("body", "front", 3, 5, cross, 1, 7)
    s.rows("body", "back", 1, 11, cross, 3, 5)
    s.rows("body", "back", 3, 5, cross, 1, 7)
    s.rows("body", "front", 2, 4, shade(cloth, 0.92), 0, 1)              # a little shape at the chest
    s.rows("body", "front", 2, 4, shade(cloth, 0.92), 7, 8)
    if border:
        for f in ("front", "back"):
            s.rows("body", f, 0, 12, border, 0, 1)
            s.rows("body", f, 0, 12, border, 7, 8)
    s.ring("body", 8, 9, LEATHER)                                       # belt
    s.dot("body", "front", 3, 8, GOLD)
    s.dot("body", "front", 4, 8, GOLD)
    # the tabard's skirt hangs over the legs (pants overlay): front and back panels, open at the sides
    for leg in ("rpants", "lpants"):
        s.rows(leg, "front", 0, 6, cloth)
        s.rows(leg, "back", 0, 6, cloth)
        s.rows(leg, "front", 5, 6, border or cross)
        s.rows(leg, "back", 5, 6, border or cross)


def mail_limbs(s):
    for arm in ("rarm", "larm"):
        s.fill(arm, MAIL)
        for y in range(0, 10, 2):
            s.ring(arm, y, y + 1, MAIL_SH)
    for leg in ("rleg", "lleg"):
        s.fill(leg, MAIL)
        for y in range(0, 8, 2):
            s.ring(leg, y, y + 1, MAIL_SH)
        s.ring(leg, 8, 12, LEATHER)                                     # boots
        s.ring(leg, 8, 9, LEATHER_SH)
        s.rows(leg, "bottom", 0, 4, LEATHER_SH)


def knight():
    s = Skin()
    head(s, rgb("#6B3E22"), rgb("#4A7A3A"))
    s.rows("body", "back", 0, 7, rgb("#6B3E22"), 3, 5)                   # her braid down the back
    mail_limbs(s)
    tabard(s, WHITE, WHITE_SH, GOLD)
    hands(s, 10)
    return s.img


def captain():
    s = Skin()
    head(s, rgb("#E2BC5C"), rgb("#3A6EA8"), fringe_rows=1)
    s.rows("body", "back", 0, 9, rgb("#E2BC5C"), 2, 6)                   # long golden hair
    mail_limbs(s)
    tabard(s, WHITE, WHITE_SH, GOLD, border=SKY)
    hands(s, 10)
    return s.img


def inquisitor():
    s = Skin()
    head(s, rgb("#2A1F1A"), rgb("#7A4A9A"), fringe_rows=2)
    # the hood (hat overlay): over the top, sides and back, a frame round the face
    s.fill("hat", NAVY, ("top", "right", "left", "back"))
    s.rows("hat", "front", 0, 1, NAVY)
    s.rows("hat", "front", 0, 8, NAVY, 0, 1)
    s.rows("hat", "front", 0, 8, NAVY, 7, 8)
    s.rows("hat", "front", 0, 1, GOLD, 1, 7)
    s.rows("hat", "right", 7, 8, GOLD)
    s.rows("hat", "left", 7, 8, GOLD)
    # the robe: body, arms and legs alike, a white collar and gold down the front
    for part in ("body", "rarm", "larm", "rleg", "lleg"):
        s.fill(part, NAVY)
    for part in ("rarm", "larm"):
        s.rows(part, "right", 0, 12, NAVY_SH)
    s.rows("body", "right", 0, 12, NAVY_SH)
    s.rows("body", "left", 0, 12, NAVY_SH)
    s.rows("body", "front", 0, 2, WHITE)
    s.rows("body", "back", 0, 2, WHITE)
    s.rows("body", "front", 2, 12, GOLD, 3, 5)
    s.dot("body", "front", 3, 5, WHITE)                                  # a small white cross on the gold
    s.dot("body", "front", 4, 5, WHITE)
    s.ring("body", 7, 8, GOLD_SH)                                        # a cord at the waist
    for leg, side in (("rleg", "left"), ("lleg", "right")):
        s.rows(leg, "front", 0, 12, GOLD, 3 if leg == "rleg" else 0, 4 if leg == "rleg" else 1)
        s.ring(leg, 11, 12, GOLD)                                        # gold hem
    for leg in ("rleg", "lleg"):
        s.rows(leg, "bottom", 0, 4, LEATHER_SH)
    for arm in ("rarm", "larm"):
        s.ring(arm, 9, 10, GOLD)                                         # cuffs
        s.ring(arm, 10, 12, SKIN)
        s.rows(arm, "bottom", 0, 4, SKIN_SH)
    return s.img


def chaplain():
    """a spared sister come back as a chaplain (entity/order/Chaplain): a black habit and veil, a white coif and collar, a gold cross"""
    s = Skin()
    black, black_sh = rgb("#24222A"), rgb("#16151B")
    head(s, rgb("#6B3E22"), rgb("#4A7A3A"))
    s.fill("hat", black, ("top", "right", "left", "back"))                # the veil
    s.rows("hat", "front", 0, 2, WHITE)                                  # the coif's band over the brow
    s.rows("hat", "front", 0, 8, WHITE, 0, 1)                            # and down the sides of her face
    s.rows("hat", "front", 0, 8, WHITE, 7, 8)
    s.rows("hat", "right", 0, 8, WHITE, 6, 8)
    s.rows("hat", "left", 0, 8, WHITE, 0, 2)
    for part in ("body", "rarm", "larm", "rleg", "lleg"):
        s.fill(part, black)
    for part in ("rarm", "larm", "body"):
        s.rows(part, "right", 0, 12, black_sh)
    s.rows("body", "left", 0, 12, black_sh)
    s.rows("body", "front", 0, 2, WHITE)                                 # the collar
    s.rows("body", "back", 0, 3, black_sh)                               # the veil falls over her shoulders
    s.rows("body", "front", 3, 7, GOLD, 3, 5)                            # the cross on its cord
    s.rows("body", "front", 4, 5, GOLD, 2, 6)
    s.ring("body", 8, 9, WHITE_SH)                                       # a white cord at the waist
    for leg in ("rleg", "lleg"):
        s.ring(leg, 11, 12, LEATHER_SH)                                  # shoes under the hem
        s.rows(leg, "bottom", 0, 4, LEATHER_SH)
    for arm in ("rarm", "larm"):
        s.ring(arm, 9, 10, WHITE)                                        # white cuffs, bare hands
        s.ring(arm, 10, 12, SKIN)
        s.rows(arm, "bottom", 0, 4, SKIN_SH)
    return s.img


SKINS = {"order_knight": knight, "order_inquisitor": inquisitor, "order_captain": captain, "order_chaplain": chaplain}


def preview(path):
    """the front of each (head, body, arms, legs) side by side, 8x"""
    out = Image.new("RGBA", (len(SKINS) * 18 * 8, 34 * 8), (40, 40, 48, 255))
    for i, (name, make) in enumerate(SKINS.items()):
        img = make()
        sheet = Image.new("RGBA", (18, 34), (0, 0, 0, 0))

        def paste(box, x, y):
            fx, fy, w, h = faces(box)["front"]
            sheet.alpha_composite(img.crop((fx, fy, fx + w, fy + h)), (x, y))
        paste("head", 5, 1)
        paste("hat", 5, 1)
        paste("body", 5, 9)
        paste("rarm", 2, 9)
        paste("larm", 13, 9)
        paste("rleg", 5, 21)
        paste("rpants", 5, 21)
        paste("lleg", 9, 21)
        paste("lpants", 9, 21)
        out.alpha_composite(sheet.resize((18 * 8, 34 * 8), Image.NEAREST), (i * 18 * 8, 0))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    out.save(path)


ADVANCEMENTS = {
    "order_patrol": ("minecraft:white_banner", "task", "Holy Orders", "See a patrol of the Order march on the Demon Realm"),
    "order_marked": ("minecraft:bell", "task", "Heretic", "Become known to the Order for your ties to the Demon Realm"),
    "order_humbled": ("harmontide:realm_silver_sword", "task", "Crisis of Faith", "Bring a sister of the Order to her knees"),
    "order_spared": ("minecraft:white_banner", "task", "Mercy", "Spare a kneeling sister of the Order"),
    "order_fallen": ("harmontide:heartfruit", "goal", "Fallen", "Feed a kneeling sister a heartfruit"),
    "order_crusade": ("minecraft:shield", "challenge", "Crusade Repelled", "Beat back a crusade of the Order from your hearth"),
    "order_chaplain": ("minecraft:book", "task", "Mercy Remembered", "Have a sister you spared come back to your hearth as a chaplain"),
    "order_blessing": ("minecraft:bell", "goal", "Blessed Union", "Be wed, or renew your vows, with the Order's chaplain's blessing"),
}
PARENTS = {"order_patrol": "harmontide:realm_entered", "order_marked": "harmontide:order_patrol", "order_humbled": "harmontide:order_patrol",
           "order_spared": "harmontide:order_humbled", "order_fallen": "harmontide:order_humbled", "order_crusade": "harmontide:order_marked",
           "order_chaplain": "harmontide:order_spared", "order_blessing": "harmontide:order_chaplain"}

GUIDE = ("The Order is sworn to wipe out the Demon Realm. By day, now and then, a patrol of its sisters (two knights in white and gold, "
         "and an inquisitrix in a navy hood) marches on a realm patch near you to purge it: realm grass turns back where they walk, and "
         "the inquisitrix blesses all of it around her. They free the possessed with a touch, and they put the realm's own to the sword: "
         "devils, and every girl the realm changed. Ordinary girls they leave alone. The Order hears of your ties to the realm: time "
         "spent in its air, devils and changed girls among your friends, heartfruit handed out. Once it has heard enough, you are "
         "marked: sisters fight you and the friends at your side, and patrols come looking for you. When it has heard far too much, a "
         "crusade comes for your hearth one night: a captain under the Order's banner and her band. Beat every one of them and your "
         "household is the fonder of you; if any still stand at dawn, they withdraw, and your household is shaken. A realm silver blade "
         "cuts a sister's faith, not her flesh, and so does the realm's air in the dark: with no faith left she kneels and lays down her "
         "sword. Feed a kneeling sister a heartfruit and she falls to the realm, a succubus (a captain a demon) devoted to you; with an "
         "empty hand you spare her, and she goes home. A sister you spared remembers: a couple of days later, while the Order doesn't have "
         "you marked, she comes back to your hearth unarmed, as a chaplain, and stays three days. Any wedding or vow renewal at a hearth "
         "while she is there gets her blessing. The Order thinks better of mercy and of exorcism. (/harmontide order tells you "
         "how the Order sees you.)")

LANG = {
    "entity.harmontide.order_knight": "Sister-Knight of the Order",
    "entity.harmontide.order_inquisitor": "Inquisitrix of the Order",
    "entity.harmontide.order_captain": "Captain of the Order",
    "item.harmontide.order_knight_spawn_egg": "Sister-Knight Spawn Egg",
    "item.harmontide.order_inquisitor_spawn_egg": "Inquisitrix Spawn Egg",
    "item.harmontide.order_captain_spawn_egg": "Order Captain Spawn Egg",
    "item.harmontide.order_banner": "Banner of the Order",
    "event.harmontide.order_crusade": "Crusade of the Order",
    "harmontide.configuration.orderPatrols": "Order patrols",
    "harmontide.configuration.orderRaids": "Order crusades",
    "message.harmontide.order.patrol": "Bells ring in the distance: a patrol of the Order is marching on the Demon Realm.",
    "message.harmontide.order.marked": "The Order has heard of your ties to the Demon Realm. Its sisters will know you on sight.",
    "message.harmontide.order.raid": "A horn sounds in the dark: a crusade of the Order is coming for your hearth!",
    "message.harmontide.order.victory": "The crusade is broken. Your household gathers round you, shaken but proud.",
    "message.harmontide.order.dawn": "At dawn the crusaders withdraw, blessing the ground as they go. Your household is shaken.",
    "message.harmontide.order.kneels": "%s lays down her sword and kneels, her faith spent.",
    "message.harmontide.order.fallen": "%s tastes the heartfruit, and the realm takes her. She looks up at you with new eyes.",
    "message.harmontide.order.spared": "%s rises, bows to you, and turns for home.",
    "entity.harmontide.order_chaplain.named": "Sister %s, Chaplain",
    "message.harmontide.order.chaplain.came": "Sister %s, whom you spared, has come to your hearth as a chaplain. She will bless any wedding here while she stays.",
    "message.harmontide.order.chaplain.talk": "Sister %s: \"I'll bless any vows spoken at this hearth. %s more days, then I'm needed home.\"",
    "message.harmontide.order.chaplain.farewell": "Sister %s closes her book, blesses the house, and sets out for home.",
    "message.harmontide.order.chaplain.blessed": "Sister %s blesses the union of %s and %s.",
    "journal.harmontide.family.blessed": " (blessed by Sister %s)",
    "commands.harmontide.order.standing.unknown": "The Order has barely heard of you (notoriety %s).",
    "commands.harmontide.order.standing.rumoured": "There are rumours about you in the Order's halls (notoriety %s).",
    "commands.harmontide.order.standing.marked": "The Order has marked you as a friend of the Demon Realm (notoriety %s).",
    "commands.harmontide.order.standing.crusade": "The Order is preaching a crusade against you (notoriety %s): it comes some night you are near your hearth.",
    "commands.harmontide.order.raid.under_way": "A crusade on your hearth at %s: %s of %s still standing.",
    "commands.harmontide.order.patrol": "A patrol of %s set out.",
    "commands.harmontide.order.raid": "A crusade of %s set out for your hearth.",
    "commands.harmontide.order.no_ground": "No open ground nearby for them to come from.",
    "commands.harmontide.order.no_hearth": "You have no hearth within 64 blocks.",
    "journal.harmontide.guide.order.title": "The Order",
    "journal.harmontide.guide.order": GUIDE,
}


def main():
    for name, make in SKINS.items():
        save(make(), "entity", "order", name + ".png")
        if name != "order_chaplain":                                     # a chaplain is a knight come back, not her own entity
            dump(os.path.join(ASSETS, "models", "item", name + "_spawn_egg.json"), {"parent": "minecraft:item/template_spawn_egg"})
    nugget = {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:iron_nugget", "functions": [
        {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 3}},
        {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}]}]}
    bread = {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:bread", "functions": [
        {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}]}]}
    holy = {"rolls": 1, "conditions": [{"condition": "minecraft:killed_by_player"}, {"condition": "minecraft:random_chance", "chance": 0.5}],
            "entries": [{"type": "minecraft:item", "name": "harmontide:holy_water"}]}
    gold = {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:gold_ingot", "functions": [
        {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]}]}
    tables = {"order_knight": [nugget, bread], "order_inquisitor": [holy, bread], "order_captain": [nugget, gold, holy]}
    for name, pools in tables.items():
        dump(os.path.join(DATA, "loot_table", "entities", name + ".json"), {"type": "minecraft:entity", "pools": pools})
    for adv, (icon, frame, title, desc) in ADVANCEMENTS.items():
        dump(os.path.join(DATA, "advancement", adv + ".json"), {
            "parent": PARENTS[adv],
            "display": {"icon": {"id": icon}, "title": {"translate": "advancement.harmontide." + adv},
                        "description": {"translate": "advancement.harmontide." + adv + ".desc"},
                        "frame": frame, "show_toast": True, "announce_to_chat": True, "hidden": False},
            "criteria": {"done": {"trigger": "minecraft:impossible"}}})
        LANG["advancement.harmontide." + adv] = title
        LANG["advancement.harmontide." + adv + ".desc"] = desc
    add_lang(LANG)
    if "--preview" in sys.argv:
        preview(sys.argv[sys.argv.index("--preview") + 1])


def chapel():
    """the wayside chapel (world/OrderChapelStructure): structure, spread, biomes, chest, locate lines"""
    dump(os.path.join(DATA, "worldgen", "structure", "order_chapel.json"), {
        "type": "harmontide:order_chapel", "biomes": "#harmontide:has_structure/order_chapel",
        "step": "surface_structures", "spawn_overrides": {}, "terrain_adaptation": "none"})
    dump(os.path.join(DATA, "worldgen", "structure_set", "order_chapel.json"), {
        "structures": [{"structure": "harmontide:order_chapel", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 36, "separation": 14, "salt": 73019441}})
    dump(os.path.join(DATA, "tags", "worldgen", "biome", "has_structure", "order_chapel.json"), {"values": [
        "minecraft:plains", "minecraft:sunflower_plains", "minecraft:meadow", "minecraft:forest", "minecraft:flower_forest",
        "minecraft:birch_forest", "minecraft:cherry_grove", "minecraft:savanna"]})
    dump(os.path.join(DATA, "loot_table", "chests", "order_chapel.json"), {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 3, "max": 6}, "entries": [
            {"type": "minecraft:item", "name": "harmontide:holy_water", "weight": 4,
             "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]},
            {"type": "minecraft:item", "name": "harmontide:splash_holy_water", "weight": 3,
             "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": "minecraft:bread", "weight": 5,
             "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 4}}]},
            {"type": "minecraft:item", "name": "minecraft:gold_nugget", "weight": 4,
             "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 2, "max": 9}}]},
            {"type": "minecraft:item", "name": "minecraft:candle", "weight": 3,
             "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": "minecraft:book", "weight": 2},
            {"type": "minecraft:item", "name": "minecraft:golden_apple", "weight": 1}]}]})
    add_lang({
        "commands.harmontide.locate.chapel": "The nearest chapel of the Order is at %s (%s blocks away).",
        "commands.harmontide.locate.chapel.none": "No chapel of the Order within reach (only chunks generated after this update have them).",
    })


if __name__ == "__main__":
    main()
    chapel()
