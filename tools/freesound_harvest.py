#!/usr/bin/env python3
"""Finds short CC0 clips on Freesound and downloads their HQ previews (no account needed).

    python tools/freesound_harvest.py <out_dir> [--per N]

Every candidate's own sound page is checked for the CC0 licence link before it is kept, so the
search filter alone is never trusted. Writes <out_dir>/<group>/<id>.mp3 plus <out_dir>/manifest.json
(id, title, author, url, licence, duration) which tools/sound_import.py turns into CREDITS.
"""
import json
import os
import re
import sys
import time
import urllib.parse
import urllib.request

UA = {"User-Agent": "Mozilla/5.0 (sound-harvest)"}
CC0 = "creativecommons.org/publicdomain/zero/1.0"

# titles that are rarely a clean single call
SKIP = re.compile(r"alien|synth|ambien|background|\bbg\b|loop|forest|morning|dawn|traffic|warehouse|\bwings?\b|flap|"
                  r"music|remix|robot|toy|cartoon|kazoo|whistle|\bhorns?\b|squeak|door|vocoder|monster|zombie|imitation|"
                  r"rubber|808|guitar|snare|laser|beam|dinosaur|click|button|scary|child|kid|baby|son of|c'mon|"
                  r"gonna|\bman\b|\bmale\b|weird|sexy|sensual|seduc|moan|orgasm|pleasure|kiss|flirt|naughty|erotic|asmr|"
                  r"lewd|\bbed\b|breath", re.I)
FEMALE = re.compile(r"\b(female|woman|women|girl|lady)\b", re.I)


def W(words):
    """Matches any of the words at a word start (so 'hen' is not found in 'then')."""
    return re.compile(r"\b(" + words + r")", re.I)


# the clip's title or tags must name what the group is
MATCH = {
    "songbird": W(r"sparrow|finch|songbird|robin|warbler|wren|canary|chickadee|bunting|chirp|tweet"),
    "hummingbird": W(r"hummingbird|sunbird"),
    "corvid": W(r"crow|raven|caw|magpie|jackdaw|rook\b|jay\b"),
    "owl": W(r"owl|hoot"),
    "raptor": W(r"eagle|hawk|falcon|kite\b|buzzard|osprey|kestrel|raptor"),
    "seabird": W(r"gull|seagull|tern\b|kittiwake|seabird"),
    "penguin": W(r"penguin"),
    "waterfowl": W(r"quack|duck|mallard"),
    "crane": W(r"crane|swan|flamingo|goose|heron|stork"),
    "parrot": W(r"parrot|macaw|cockatoo|galah|parakeet|lorikeet|conure"),
    "peacock": W(r"peacock|peafowl|peahen"),
    "poultry": W(r"chicken|hen\b|cluck|turkey|rooster"),
    "dove": W(r"dove|pigeon|coo\b|cooing"),
    "ratite": W(r"emu|ostrich|cassowary|rhea\b"),
    "girl_hurt": W(r"ouch|pain|hurt|grunt|aie|ow\b|oof|hit\b"),
    "girl_death": W(r"scream|death|dying|die\b|gasp|shriek|cry"),
    "girl_giggle": W(r"giggle|laugh|chuckle|tehe|hehe"),
    "eat": W(r"eat|bite|chew|crunch|munch|nom\b|chomp"),
    # amphibians
    "frog_croak": W(r"frog|ribbit|croak"),
    "frog_chirp": W(r"peeper|tree ?frog|treefrog|frog chirp|coqui"),
    "bullfrog": W(r"bullfrog|bull frog"),
    "toad": W(r"toad"),
    "tongue": W(r"slurp|lick|tongue|squelch|suck"),
    "bubbles": W(r"bubble|blub|gurgle"),
    # mammals
    "cat_meow": W(r"meow|miaow|mew\b|kitten|cat\b|cats\b"),
    "cat_purr": W(r"purr"),
    "big_cat": W(r"lion|tiger|leopard|panther|jaguar|big cat|cougar|puma"),
    "hyena": W(r"hyena"),
    "dog_bark": W(r"bark|woof|dog\b|puppy"),
    "wolf_howl": W(r"howl|wolf"),
    "fox": W(r"fox\b|vixen"),
    "horse": W(r"horse|neigh|whinny|nicker|pony|mare\b"),
    "donkey": W(r"donkey|bray|mule\b"),
    "cow": W(r"\bcow|moo\b|mooing|cattle|bull\b|calf\b"),
    "goat": W(r"goat|bleat"),
    "sheep": W(r"sheep|\bbaa|lamb\b|ewe\b"),
    "deer": W(r"deer|elk\b|stag\b|reindeer|caribou|moose|bugle"),
    "camelid": W(r"alpaca|llama|camel|vicuna"),
    "pig": W(r"\bpig|oink|boar\b|hog\b|swine|piglet"),
    "bear": W(r"bear\b|bears\b|grizzly"),
    "seal": W(r"seal\b|sea lion|walrus"),
    "small_critter": W(r"meerkat|mongoose|squirrel|chipmunk|raccoon|ferret|weasel|chitter|chatter|otter|badger|red panda|skunk|kusimanse"),
    # small mammals
    "rodent": W(r"mouse|mice\b|rat\b|rats\b|hamster|guinea pig|gerbil|capybara|beaver|marmot|rodent|degu|chinchilla"),
    "rabbit": W(r"rabbit|bunny|hare\b|hares\b"),
    "primate": W(r"monkey|chimp|gibbon|lemur|ape\b|apes\b|gorilla|macaque|baboon|orangutan|primate|howler"),
    "bat": W(r"bats?\b|echolocation|chiroptera|flying fox"),
    "marsupial": W(r"koala|kangaroo|possum|opossum|wallaby|tasmanian devil|quokka|wombat|marsupial"),
    "elephant": W(r"elephant|trumpet"),
    "cetacean": W(r"dolphin|whale|orca|beluga|cetacean|porpoise"),
    "hippo": W(r"hippo"),
    # reptiles
    "croc": W(r"crocodile|alligator|caiman|gator\b|croc\b"),
    "snake_hiss": W(r"snake|hiss|serpent|python|cobra|viper"),
    "rattle": W(r"rattlesnake|rattle snake|snake rattle"),
    # fae, undead, devils
    "girl_sigh": W(r"sigh|hum\b|humming|yawn|hmm|mmm"),
    "girl_cackle": W(r"cackle|evil laugh|witch|wicked|maniacal|laugh"),
    "ghost": W(r"ghost|ghostly|wail|spirit|phantom|haunt|spooky|banshee|whisper|eerie"),
    "bones": W(r"bone|bones|skeleton|rattle"),
    "armor": W(r"armou?r|chainmail|chain mail|knight|plate"),
    "blade": W(r"sword|blade|katana|unsheath|sheath|shing"),
    "pages": W(r"page|pages|book"),
    "cloth": W(r"cloth|fabric|flag|sheet|flutter|towel"),
    "fire": W(r"fire|flame|crackle|torch|burn|ignite|whoosh"),
    # fish, bugs, crabs
    "splash": W(r"splash|water|plop|swim"),
    "crab": W(r"crab|claw|pincer|lobster|crayfish"),
    "chitter": W(r"insect|bug|beetle|chitter|click|mandible|cricket|cicada|clicking|spider|scorpion"),
    "girl_sing": W(r"sing|singing|vocal|vocalise|vocalize|hum|humming|soprano|melody|aah|ooh|lullaby"),
    # plants
    "leaves": W(r"leaves|leaf|rustle|bush|foliage|branch|grass"),
    "wood": W(r"creak|wood|wooden|tree|timber|branch"),
}
# clips a person listened to or read and threw out (instruments, jokes, effects, suggestive voices)
REJECT = {556575, 447496, 246318, 529957, 515408, 233123, 233125, 233120, 207387, 660305, 660304, 182017, 241519,
          607902, 443960, 571424, 558488, 251938, 517614, 343978, 220163, 321163, 372182, 233124, 233126, 233121,
          336012, 420466, 448090, 233131, 689407, 839974, 237103, 443771, 693904, 854876, 62460, 351876, 278203, 702399, 457030, 463817,
          712917, 514664, 523773, 381830, 504654, 674253, 573047, 111739, 766542, 851452, 487509, 352898, 39587, 39586,
          39585, 39606, 39592, 39602, 350421, 854137, 72464, 491450, 737276, 321603,
          675802, 709817, 39588, 39589, 39593, 442879, 390595, 502211, 491457, 37460, 452713, 675399, 331816,
          624494, 253772, 856169, 444449, 134025, 321810, 434147, 477182, 724716, 458376, 391551, 155660, 87570, 144159,
          738460, 866472, 339087, 369864, 638738, 88447, 88446, 520284, 534720, 166605}
# clips that are fine but not this group (meows tagged purr, dog barks tagged wolf)
REJECT_IN = {"cat_purr": {333916, 362652, 454805, 629502}, "wolf_howl": {420447, 420450, 420449}, "snake_hiss": {733002}, "chitter": {390733},
             "bones": {392883}}  # a bone-crunch eating sound: fine for eat, gory for bones
# pond and night recordings are many frogs at once, not one girl
SKIP_MORE = re.compile(r"pond|night|chorus|swamp|marsh|lake|river|evening|crickets|insects|many frogs|\brain\b|"
                       r"herd|farm|crowd|kennel|\bzoo\b|pack of|several|group", re.I)
SKIP_MORE_GROUPS = {"frog_croak", "frog_chirp", "bullfrog", "toad", "cat_meow", "big_cat", "hyena", "dog_bark", "wolf_howl",
                    "fox", "horse", "donkey", "cow", "goat", "sheep", "deer", "camelid", "pig", "bear", "seal", "small_critter",
                    "rodent", "rabbit", "primate", "bat", "marsupial", "elephant", "cetacean", "hippo", "croc"}
# tags that mark a designed or played sound rather than the animal
NOT_REAL = re.compile(r"\b(cartoon|fake|imitation|designed|sound-design|brass|sax|saxophone|instrument|bpm|loop|music|punch|bonk|swoosh|kick|beat|turntable|"
                      r"record-scratch|turntablism|glitch|protest|alien|dubstep|genai|ai-generated|politics|evp|gore|neck|processed-white-noise|percussion|intense)\b", re.I)
# words SKIP would throw out that are the very thing this group is after
SKIP_OK = {"rodent": re.compile(r"squeak", re.I), "bat": re.compile(r"squeak", re.I),
           "chitter": re.compile(r"click", re.I), "crab": re.compile(r"click", re.I)}
# baseball bats, cricket bats, rattling things, hissing pipes
NOT_ANIMAL = re.compile(r"baseball|cricket|softball|\bhit\b|swing|pipe|steam|tyre|tire|valve|gas\b|radiator|air hose|"
                        r"kettle|cymbal|maraca|shaker|drum|toy|plastic|metal|chain|window|jar|box\b|computer|keyboard", re.I)
NOT_ANIMAL_GROUPS = {"bat", "snake_hiss", "rattle", "rodent", "chitter", "crab"}
# the girl_* groups need a female tag; these also need one (a witch's cackle, not a man's)
FEMALE_TOO = {"girl_cackle"}

# group -> (queries, max seconds)
GROUPS = {
    "songbird": (["sparrow chirp", "finch song", "songbird chirp", "robin bird song", "bird chirp single",
                  "canary", "wren", "bird tweet"], 3.0),
    "hummingbird": (["hummingbird", "sunbird"], 3.0),
    "corvid": (["crow caw", "raven call", "magpie"], 2.5),
    "owl": (["owl hoot", "barn owl screech", "tawny owl"], 3.0),
    "raptor": (["hawk screech", "eagle cry", "falcon call", "hawk", "eagle", "kestrel", "buzzard bird", "red-tailed hawk",
                "bald eagle", "osprey", "Raptors hawk", "peregrine"], 4.0),
    "seabird": (["seagull", "gull call"], 3.0),
    "penguin": (["penguin"], 5.0),
    "waterfowl": (["duck quack", "goose honk"], 2.5),
    "crane": (["crane bird call", "swan call", "flamingo", "goose", "heron", "crane bird", "Waterfowl goose", "goose honk",
               "whooper swan", "sandhill crane", "stork"], 4.0),
    "parrot": (["parrot squawk", "macaw", "cockatoo"], 2.5),
    "peacock": (["peacock call", "peacock", "peafowl"], 5.0),
    "poultry": (["chicken cluck", "hen cluck"], 2.5),
    "dove": (["dove coo", "pigeon coo", "pigeon", "dove", "cooing"], 5.0),
    "ratite": (["emu", "ostrich", "cassowary"], 5.0),
    "girl_hurt": (["female pain grunt", "woman hurt", "female ouch", "girl hurt", "female grunt", "female pain",
                   "woman pain", "female hit"], 2.0),
    "girl_death": (["female death scream", "woman dying", "female scream short", "female death", "woman scream",
                    "female gasp", "girl scream", "woman gasp", "female groan pain", "female dying"], 3.0),
    "girl_giggle": (["girl giggle", "female giggle", "woman laugh short", "giggle", "female laugh", "woman giggle"], 3.0),
    "eat": (["chewing", "eating crunch", "bite apple", "apple bite", "munch", "eating"], 2.0),
    "frog_croak": (["frog croak", "frog ribbit", "frog", "single frog", "frog call"], 3.0),
    "frog_chirp": (["spring peeper", "tree frog", "treefrog", "frog chirp", "coqui", "Frogs tree frog", "peeper call",
                    "Hyla", "frog squeak", "small frog call", "frog trill"], 3.0),
    "bullfrog": (["bullfrog", "bull frog", "Frogs bullfrog", "American bullfrog", "Lithobates", "deep frog croak"], 5.0),
    "toad": (["toad", "toad call", "toad trill", "natterjack", "cane toad", "Toads", "American toad", "Bufo"], 5.0),
    "tongue": (["slurp", "tongue click", "lick", "wet slurp", "squelch"], 1.5),
    "bubbles": (["bubbles underwater", "bubble pop", "water bubbles", "blub"], 2.0),
    "cat_meow": (["cat meow", "kitten meow", "meow", "cat meowing", "cat mew"], 2.5),
    "cat_purr": (["cat purr", "purring"], 3.0),
    "big_cat": (["lion roar", "tiger growl", "big cat growl", "leopard", "tiger roar", "lion growl", "cougar"], 4.0),
    "hyena": (["hyena laugh", "hyena"], 4.0),
    "dog_bark": (["dog bark", "small dog bark", "dog bark single", "puppy bark", "dog woof"], 2.0),
    "wolf_howl": (["wolf howl", "wolf", "howl"], 5.0),
    "fox": (["fox bark", "fox scream", "fox call", "red fox"], 3.0),
    "horse": (["horse neigh", "horse whinny", "horse snort", "horse nicker", "pony neigh"], 3.0),
    "donkey": (["donkey bray", "donkey"], 4.0),
    "cow": (["cow moo", "cow mooing", "cattle moo", "moo"], 3.5),
    "goat": (["goat bleat", "goat", "goat baa"], 2.5),
    "sheep": (["sheep baa", "sheep bleat", "sheep", "lamb bleat"], 2.5),
    "deer": (["deer call", "elk bugle", "reindeer grunt", "red deer stag", "deer bleat", "moose call", "deer", "roe deer bark",
              "fallow deer", "elk call", "Deer stag", "muntjac"], 4.0),
    "camelid": (["alpaca hum", "llama", "camel grunt", "alpaca", "camel"], 3.0),
    "pig": (["pig grunt", "pig oink", "boar grunt", "pig squeal", "pig snort"], 2.5),
    "bear": (["bear growl", "bear roar", "bear grunt", "grizzly", "bear", "brown bear", "Bears bear", "black bear"], 4.0),
    "seal": (["seal bark", "sea lion", "seal call", "walrus"], 3.0),
    "small_critter": (["meerkat", "mongoose", "squirrel chatter", "raccoon", "ferret dook", "chipmunk chirp", "otter",
                       "sea otter", "badger", "red panda", "skunk", "Small Mammals", "squirrel"], 3.0),
    "rodent": (["mouse squeak", "rat squeak", "guinea pig", "hamster", "rat", "mouse", "guinea pig wheek", "gerbil",
                "capybara", "chinchilla", "rodent"], 2.0),
    "rabbit": (["rabbit", "bunny", "rabbit squeal", "rabbit grunt", "hare", "rabbit thump", "Small Mammals rabbit",
                "rabbit honk", "guinea pig"], 2.5),
    "primate": (["monkey", "monkey call", "chimpanzee", "gibbon", "lemur", "howler monkey", "gorilla", "macaque", "baboon",
                 "ape"], 4.0),
    "bat": (["bat squeak", "bat echolocation", "bats", "bat call", "fruit bat", "flying fox bat", "pipistrelle",
             "Chiroptera", "bat social call", "bat chirp", "bat detector"], 2.5),
    "marsupial": (["koala bellow", "koala", "kangaroo", "possum", "opossum hiss", "tasmanian devil", "wallaby", "wombat",
                   "brushtail possum", "koala grunt"], 4.0),
    "elephant": (["elephant trumpet", "elephant", "elephant rumble", "elephant call"], 4.0),
    "cetacean": (["dolphin", "dolphin whistle", "whale song", "orca", "beluga", "humpback whale", "dolphin click",
                  "whale call", "dolphin chirp", "dolphins", "whale", "porpoise"], 4.0),
    "hippo": (["hippo", "hippopotamus", "hippo grunt", "hippo laugh", "hippos"], 4.0),
    "croc": (["crocodile", "alligator hiss", "alligator", "crocodile growl", "caiman", "alligator bellow",
              "alligator growl", "gator", "crocodile hiss", "Reptiles alligator"], 4.0),
    "snake_hiss": (["snake hiss", "hiss snake", "snake hissing", "python hiss", "cobra hiss", "hiss"], 2.5),
    "rattle": (["rattlesnake", "rattlesnake rattle", "snake rattle"], 3.0),
    "girl_sigh": (["female sigh", "woman sigh", "girl sigh", "female humming", "woman hum", "female yawn", "girl hmm",
                   "woman yawn", "female hmm"], 3.0),
    "girl_cackle": (["witch cackle", "evil laugh female", "female evil laugh", "witch laugh", "woman evil laugh",
                     "female cackle", "girl evil laugh"], 3.0),
    "ghost": (["ghost wail", "ghost whisper", "ghost voice", "ghostly wail", "female ghost", "spooky whisper", "banshee",
               "ghost", "whisper female", "eerie whisper"], 4.0),
    "bones": (["bone rattle", "bones rattle", "skeleton bones", "bones", "bone crack", "skeleton"], 2.0),
    "armor": (["armor clank", "armour movement", "chainmail", "armor walk", "plate armor", "knight armor"], 2.0),
    "blade": (["sword unsheath", "sword ring", "sword draw", "blade ring", "katana", "sword shing"], 2.0),
    "pages": (["page turn", "book page", "pages flip", "turning page", "book flip"], 2.0),
    "cloth": (["cloth flutter", "fabric flap", "flag flutter", "cloth movement", "sheet shake"], 2.0),
    "fire": (["fire crackle", "flame burst", "fire whoosh", "torch ignite", "fireball", "fire ignite"], 2.5),
    "splash": (["small splash", "water splash", "fish splash", "water plop", "splash single", "fish jump water"], 2.0),
    "crab": (["crab claws", "crab", "crab clicking", "crab walking", "lobster", "claw click", "crab scuttle", "crustacean", "shell clatter",
              "crayfish"], 2.0),
    "chitter": (["insect chitter", "beetle click", "insect click", "bug chitter", "cicada", "cricket chirp", "mandible",
                 "insect clicking", "scuttle", "spider scuttle", "beetle", "cockroach",
                 "grasshopper", "insect wings buzz", "bug crawl"], 2.5),
    "girl_sing": (["female vocal note", "woman singing short", "female vocalise", "female humming melody",
                   "soprano note", "girl singing", "female voice aah", "woman hum melody", "female vocal 1 note",
                   "voc snippets", "female choir note", "lullaby female"], 4.0),
    "leaves": (["leaves rustle", "bush rustle", "leaf rustle", "foliage movement", "branch rustle", "rustling leaves"], 2.0),
    "wood": (["wood creak", "tree creak", "creaking wood", "branch creak", "wooden creak"], 3.0),
}


def get(url):
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=30) as r:
        return r.read()


def search(query, max_dur, pages=3):
    found = []
    for page in range(1, pages + 1):
        f = f'license:"Creative Commons 0" duration:[0.2 TO {max_dur}]'
        url = "https://freesound.org/search/?" + urllib.parse.urlencode(
            {"q": query, "f": f, "s": "Downloads (most first)", "page": page})
        html = get(url).decode("utf-8", "replace")
        # each result card: sound link, duration, preview
        for m in re.finditer(r'/people/([^/"]+)/sounds/(\d+)/', html):
            found.append((m.group(1), int(m.group(2))))
        time.sleep(1)
    return list(dict.fromkeys(found))


def details(user, sid):
    html = get(f"https://freesound.org/people/{user}/sounds/{sid}/").decode("utf-8", "replace")
    if CC0 not in html:
        return None
    prev = re.search(r'https://cdn\.freesound\.org/previews/\d+/%d_\d+-hq\.mp3' % sid, html)
    title = re.search(r'<meta property="og:title" content="([^"]*)"', html)
    dur = re.search(r'data-duration="([\d.]+)"', html)
    if not prev:
        return None
    tags = sorted(set(re.findall(r'/browse/tags/([\w-]+)/', html)))
    return {"tags": tags, "id": sid, "author": user, "title": re.sub(r" by %s$" % re.escape(user), "", title.group(1)) if title else str(sid),
            "url": f"https://freesound.org/people/{user}/sounds/{sid}/", "license": "CC0 1.0",
            "preview": prev.group(0), "duration": float(dur.group(1)) if dur else None}


def main():
    out = sys.argv[1]
    per = int(sys.argv[sys.argv.index("--per") + 1]) if "--per" in sys.argv else 6
    only = sys.argv[sys.argv.index("--only") + 1].split(",") if "--only" in sys.argv else None
    mpath = os.path.join(out, "manifest.json")
    manifest = json.load(open(mpath)) if os.path.exists(mpath) else {}
    for group, (queries, max_dur) in GROUPS.items():
        if only and group not in only:
            continue
        have = manifest.setdefault(group, [])
        seen = {h["id"] for h in have}
        for q in queries:
            if len(have) >= per:
                break
            for user, sid in search(q, max_dur):
                if len(have) >= per:
                    break
                if sid in seen or sid in REJECT or sid in REJECT_IN.get(group, ()):
                    continue
                seen.add(sid)
                try:
                    d = details(user, sid)
                except Exception as e:  # noqa: BLE001
                    print("  skip", sid, e)
                    continue
                time.sleep(1)
                if not d:
                    continue
                skip_title = SKIP_OK[group].sub("", d["title"]) if group in SKIP_OK else d["title"]
                if SKIP.search(skip_title) or (group in NOT_ANIMAL_GROUPS and NOT_ANIMAL.search(d["title"])):
                    continue
                text = d["title"] + " " + " ".join(d["tags"])
                if (group in SKIP_MORE_GROUPS and SKIP_MORE.search(d["title"])) or NOT_REAL.search(" ".join(d["tags"])):
                    continue
                if not MATCH[group].search(text) or ((group.startswith("girl_") or group in FEMALE_TOO) and not FEMALE.search(" ".join(d["tags"]))):
                    continue
                os.makedirs(os.path.join(out, group), exist_ok=True)
                path = os.path.join(out, group, f"{sid}.mp3")
                if not os.path.exists(path):
                    open(path, "wb").write(get(d["preview"]))
                d["file"] = f"{group}/{sid}.mp3"
                have.append(d)
                print(f"{group}: {sid} {d['title']} by {d['author']}".encode("ascii", "replace").decode())
                json.dump(manifest, open(mpath, "w"), indent=1)
        json.dump(manifest, open(mpath, "w"), indent=1)
    print("done", {g: len(v) for g, v in manifest.items()})


if __name__ == "__main__":
    main()
