"""Harmontide species added to existing families (HARMONTIDE_CONCEPTS.md, first batch 2026-10-21).

Imported at the end of family_concepts.py: each entry is appended to its family's species list, and its column is
appended to every row of that family's comparison table (`split`) so the field guides stay aligned.
"""
import family_concepts as C

ADD = {
    "nekomata": [(
        dict(id="ocelot_nekomata", name="Ocelot", group="Jungle & bamboo", tag="Nocturnal · climber", height=1.65,
             biomes=["jungle", "sparse_jungle", "bamboo_jungle"], temperament="Secretive, nocturnal, fiercely private",
             ecology="Leopardus pardalis (Felidae). A small spotted cat of the jungle floor who hunts at dusk and dawn and climbs well. She is cousin to the "
                     "vanilla ocelots and just as shy.",
             interaction="Come at dusk, sneak, and bring fish. Never chase her into the trees.",
             signature=("Tree climber", "She climbs straight up tree trunks and walls like the gray fox and spends the day high in the canopy. Her presents are jungle "
                        "finds: cocoa beans, feathers, the odd melon slice."),
             danger="Low.",
             variants=[("jungle", "Jungle (default)", "Gold coat with chain-like rosettes"), ("grey", "Grey", "Grey-buff coat (a real colour morph)"),
                       ("pale", "Pale", "Cream coat with faded rosettes"), ("melanistic", "Melanistic", "Black on black"),
                       ("golden", "Golden", "Rich orange-gold")]),
        {"Habitat": "Jungle, bamboo", "Social": "Solitary", "Water": "Swims if she must", "Signature": "Climbs trees, jungle presents", "Danger": "Low"})],
    "centaur": [(
        dict(id="tapir_centaur", name="Tapir", group="Jungle & swamp", tag="Gentle · dream-eater", height=2.1,
             biomes=["jungle", "sparse_jungle", "bamboo_jungle", "swamp", "mangrove_swamp"], temperament="Gentle, sleepy, quietly protective",
             ecology="Tapirus (Perissodactyla: Tapiridae), the same order as horses, so she belongs with the centaurs. A shy browser of the jungle and swamp "
                     "who eats fruit and leaves at night. In Japan the tapir became the baku, the eater of nightmares.",
             interaction="Bring melons and apples. Brush her.",
             signature=("Baku", "Phantoms never spawn for a player sleeping within 32 blocks of a tapir, and the phantom girl stays away too. She eats the "
                        "bad dreams."),
             danger="Low.",
             variants=[("malayan", "Malayan (default)", "Black with a white saddle"), ("lowland", "Lowland", "Even dark brown"),
                       ("mountain", "Mountain", "Woolly black-brown"), ("bairds", "Baird's", "Grey-brown"),
                       ("calf", "Calf coat", "Brown with pale stripes and spots, like a baby tapir")]),
        {"Habitat": "Jungle, swamp", "Ridden": "Yes: slow and steady", "Signature": "Baku: no phantoms", "Temperament": "Gentle, sleepy", "Danger": "Low"})],
    "lizardfolk": [(
        dict(id="snapping_turtle_lizardfolk", name="Alligator Snapping Turtle", group="Swamps & mangroves", tag="Patient · ancient", height=1.8,
             biomes=["swamp", "mangrove_swamp"], temperament="Patient, grumpy, very old",
             ecology="Macrochelys temminckii (Testudines: Chelydridae), the largest freshwater turtle in North America. She lies still on the riverbed "
                     "with her mouth open and wiggles a pink, worm-like lure on her tongue until a fish swims in.",
             interaction="Leave her be in the water and she will feed you. Bring her fish.",
             signature=("Tongue lure", "While she sits still in water she catches a fish now and then and drops it at her feet (cod or salmon, at most "
                        "three lying around at a time). Hit her and she hides in her shell. Her bite is the hardest in the family after the crocodile's."),
             danger="Moderate if provoked (a hard bite).",
             variants=[("common", "Common (default)", "Dark brown shell, olive skin"), ("mossy", "Mossy", "Algae on her shell and hair"),
                       ("dark", "Dark", "Almost black"), ("suwannee", "Suwannee", "The Suwannee River species, paler and greyer"),
                       ("leucistic", "Leucistic", "Pale cream, very rare")]),
        {"Habitat": "Swamps, mangroves", "Signature": "Tongue lure, shell", "Danger": "Moderate if provoked"})],
    "slime": [(
        dict(id="mixed_slime", name="Mixed Slime", group="Made, not found", tag="Two in one · marbled", height=1.6,
             biomes=[], temperament="Two moods at once",
             ecology="Two slime girls of different kinds who trust the same player can flow together into one marbled slime. "
                     "She is both of them: a killing blow does not split her into halves, it separates her back into the two she was.",
             interaction="Hold a slime ball and right-click one of two slimes (of different kinds, both at high friendship with you) standing "
                         "together. Sneak and do it again to separate them.",
             signature=("Two in one", "One slime carrying both girls, their bonds and their belongings. Never lost: hurt badly, she comes apart into the two again."),
             danger="Low.",
             variants=[("verdant_magma", "Verdant and magma (default)", "Marbled verdant and magma jelly"),
                       ("verdant_honey", "Verdant and honey", "Marbled verdant and honey jelly"),
                       ("verdant_frost", "Verdant and frost", "Marbled verdant and frost jelly"),
                       ("verdant_amethyst", "Verdant and amethyst", "Marbled verdant and amethyst jelly"),
                       ("magma_honey", "Magma and honey", "Marbled magma and honey jelly"),
                       ("magma_frost", "Magma and frost", "Marbled magma and frost jelly"),
                       ("magma_amethyst", "Magma and amethyst", "Marbled magma and amethyst jelly"),
                       ("honey_frost", "Honey and frost", "Marbled honey and frost jelly"),
                       ("honey_amethyst", "Honey and amethyst", "Marbled honey and amethyst jelly"),
                       ("frost_amethyst", "Frost and amethyst", "Marbled frost and amethyst jelly")]),
        {"Habitat": "Wherever two slimes meet", "Favourite food": "Whatever her halves like", "Element": "Both", "Weakness": "Being separated", "Danger": "Low"})],
    "undead": [(
        dict(id="banshee_undead", name="Banshee", group="Birch forests, at night", tag="Mournful · watchful", height=1.75,
             biomes=["birch_forest", "old_growth_birch_forest", "meadow"], temperament="Mournful, gentle, protective",
             ecology="The bean sí, the woman of the fairy mound in Irish folklore. Her keening warns of a coming death. Here it is a warning you can act on, "
                     "not a curse. She combs her long hair with a silver comb.",
             interaction="Pumpkin pie, and listen when she cries.",
             signature=("Keening", "When a player within 24 blocks drops to three hearts or fewer, she keens. Every hostile mob within 16 blocks of that player "
                        "glows for ten seconds, as a bell shows raiders. She keens at most once a minute for each player."),
             danger="None.",
             variants=[("keening", "Keening (default)", "Grey cloak, silver hair"), ("green", "Green", "Green dress, auburn hair"),
                       ("red", "Red", "Red-haired, dark cloak"), ("mist", "Mist", "All pale, like fog"), ("raven", "Raven", "Black hair and cloak")]),
        {"Haunts": "Birch forests, at night", "Signature": "Keening reveals danger", "Danger": "None"})],
}

for fid, entries in ADD.items():
    fam = C.family(fid)
    for species, cols in entries:
        if any(s["id"] == species["id"] for s in fam["species"]):
            continue
        fam["species"].append(species)
        for label, values in fam.get("split", []):
            values.append(cols.get(label, "-"))

# answers of 2026-10-22: red kitsune also live in cherry groves, where one in twenty is a nine-tailed elder (Kitsune.finalizeSpawn)
for _s in C.family("kitsune")["species"]:
    if _s["id"] == "red_kitsune" and "cherry_grove" not in _s["biomes"]:
        _s["biomes"].append("cherry_grove")


# second batch (2026-10-23): Kappa, Naga, Dullahan
SECOND = {
    "lizardfolk": [(
        dict(id="kappa_lizardfolk", name="Kappa", group="Rivers & swamps", tag="Polite · mischievous", height=1.5,
             biomes=["river", "swamp"], temperament="Mischievous, proud, very polite",
             ecology="A river yokai of Japan: a turtle's shell on her back and a dish of water (the sara) on her head. Folklore says it comes "
                     "from the softshell turtle (Pelodiscus sinensis, Testudines: Trionychidae) and the Japanese giant salamander.",
             interaction="Bow to her (crouch while facing her): she bows back. Melon slices stand in for her beloved cucumbers.",
             signature=("Her dish", "Wet (water, rain) she is strong. Two minutes dry and her dish has run empty: she is weak and slow. Bow to her and "
                        "she bows back, spilling it: a kappa who was after you stops at once."),
             danger="Moderate in the water, low out of it.",
             variants=[("river", "River (default)", "Green shell and hair"), ("pond", "Pond", "Teal"), ("red", "Red", "A red kappa"),
                       ("mountain", "Mountain", "Brown"), ("white", "White", "Rare and pale")]),
        {"Habitat": "Rivers, swamps", "Signature": "Water dish, bowing", "Danger": "Moderate in water"})],
    "lamia": [(
        dict(id="naga_lamia", name="Naga", group="Jungle temples", tag="Regal · watchful", height=2.3,
             biomes=["jungle", "sparse_jungle", "bamboo_jungle"], temperament="Regal, watchful, generous to friends",
             ecology="The serpent guardians of South and Southeast Asian myth, who keep watch over water and treasure. Her serpent half is a "
                     "king cobra (Ophiophagus hannah, Squamata: Elapidae): a hood, and a crown of her own.",
             interaction="Earn her trust before you go for the temple's treasure.",
             signature=("Guardian", "Nobody she is not fond of opens a treasure chest (an unopened structure chest) within 10 blocks of her: she "
                        "spreads her hood and warns them off. Your own chests are never hers to guard."),
             danger="Moderate.",
             variants=[("king", "King cobra (default)", "Olive with pale bands"), ("black", "Black", "Glossy black"), ("golden", "Golden", "Gold and cream"),
                       ("jade", "Jade", "Green"), ("crimson", "Crimson", "Deep red")]),
        {"Habitat": "Jungle temples", "Young": "Live young", "Defence": "Hood, venom", "Signature": "Guards treasure", "Danger": "Moderate"})],
    "undead": [(
        dict(id="dullahan_undead", name="Dullahan", group="Plains & meadows, at night", tag="Headless · dignified", height=1.85,
             biomes=["plains", "meadow", "sunflower_plains"], temperament="Dignified, reserved, kind underneath",
             ecology="The headless rider of Irish folklore. She carries her head under her arm; a cold blue spirit flame burns where her neck ends. "
                     "In the old stories the dullahan fears gold.",
             interaction="Pumpkin pie, and never hold gold at her.",
             signature=("Headless rider", "She often rides a skeleton horse through the night. Anyone holding gold makes her back away."),
             danger="Low unless provoked.",
             variants=[("knight", "Knight (default)", "Dark armour, silver hair"), ("crimson", "Crimson", "Red cloak"), ("ash", "Ash", "Grey"),
                       ("gold", "Gilded", "Gold trim"), ("pale", "Pale", "White hair, pale cloak")]),
        {"Haunts": "Plains and meadows, at night", "Signature": "Head under her arm, rides at night, fears gold", "Danger": "Low unless provoked"})],
}
for _fid, _entries in SECOND.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))


# fourth wave (2026-10-24): Camel, Dromedary, Kelpie, Wadjet, Hornet, Dugong (HARMONTIDE_CONCEPTS.md "Fourth wave")
FOURTH = {
    "centaur": [(
        dict(id="camel_centaur", name="Camel", group="Badlands & high savanna", tag="Steady · unbothered", height=2.4,
             biomes=["savanna_plateau", "windswept_savanna", "badlands"], temperament="Unflappable, patient, generous",
             ecology="Camelus bactrianus (Artiodactyla: Camelidae), the two-humped camel of cold deserts and steppe. Her humps store fat, not water, "
                     "and her thick winter coat falls out in clumps every spring. Cousin of the vanilla camels.",
             interaction="Climb on: she carries anyone. Cactus is her treat, and a bucket brings back camel milk.",
             signature=("Caravan", "Anyone may ride her, friend or stranger. Feed her cactus beside a vanilla camel fed cactus and the two raise a calf. "
                        "A bucket milks her (once every five minutes)."),
             danger="None.",
             variants=[("bactrian", "Bactrian (default)", "Warm brown, two humps"), ("wild", "Wild", "Lean and sandy grey"),
                       ("white", "White", "Cream white"), ("dark", "Dark", "Deep brown"), ("winter", "Winter coat", "Shaggy and pale")]),
        {"Habitat": "Badlands, high savanna", "Ridden": "Yes: by anyone", "Signature": "Milk, breeds with camels", "Temperament": "Unflappable", "Danger": "None"}),
        (dict(id="dromedary_centaur", name="Dromedary", group="Desert", tag="Hardy · good-humoured", height=2.35,
              biomes=["desert"], temperament="Hardy, good-humoured, a little smug",
              ecology="Camelus dromedarius (Artiodactyla: Camelidae), the one-humped camel of hot deserts. She can go a week without water and "
                      "drink a bathtub's worth when she finds it. Cousin of the vanilla camels.",
              interaction="Climb on: she carries anyone. Cactus is her treat, and a bucket brings back camel milk.",
              signature=("Caravan", "Anyone may ride her, friend or stranger. Feed her cactus beside a vanilla camel fed cactus and the two raise a "
                         "calf. A bucket milks her (once every five minutes)."),
              danger="None.",
              variants=[("desert", "Desert (default)", "Sandy fawn, one hump"), ("white", "White", "Cream white"), ("black", "Black", "Dark brown-black"),
                        ("red", "Red", "Rusty red"), ("piebald", "Piebald", "Fawn with white patches")]),
         {"Habitat": "Desert", "Ridden": "Yes: by anyone", "Signature": "Milk, breeds with camels", "Temperament": "Hardy", "Danger": "None"}),
        (dict(id="kelpie_centaur", name="Kelpie", group="Rivers, at night", tag="Wild · beguiling", height=2.3,
              biomes=["river", "frozen_river"], temperament="Charming by day, dangerous by night",
              ecology="The water horse of Scottish lochs and rivers: a dark mare with weed in her mane who invites travellers to ride, then "
                      "carries them into the deep. Here the dive is a prank and the danger is only at night.",
              interaction="Make friends by day (kelp, apples), then saddle her. Never stand on her bank at night as a stranger.",
              signature=("Water horse", "Saddled, she carries you over water faster than any boat, and on land like a horse. By day she is "
                         "calm. At night she attacks any player she is not fond of."),
              danger="High at night, none by day.",
              variants=[("loch", "Loch (default)", "Black-green coat, kelp in her mane"), ("grey", "Grey", "Grey mare"),
                        ("white", "White", "Pale as river mist"), ("peat", "Peat", "Peat-brown"), ("sea", "Sea", "Blue-green")]),
         {"Habitat": "Rivers", "Ridden": "With a saddle: faster than a boat", "Signature": "Hunts at night", "Temperament": "Two-faced", "Danger": "High at night"})],
    "lamia": [(
        dict(id="wadjet_lamia", name="Wadjet", group="Desert", tag="Protective · fierce", height=1.85,
             biomes=["desert"], temperament="Protective, proud, merciless to monsters",
             ecology="The cobra goddess of Lower Egypt, the uraeus on the pharaoh's crown, here in a two-legged form. Her hood is the Egyptian "
                     "cobra's (Naja haje, Squamata: Elapidae). Her sister Nekhbet, the vulture, guards Upper Egypt.",
             interaction="Walk past her: she blesses you. Stay and be kind, and she pays in emeralds.",
             signature=("Blessing", "Every player who passes within 8 blocks, and the girls following them, gets Regeneration and Resistance. She "
                        "hunts every hostile mob in sight. Name her Nekhbet and the blessing and her bite grow stronger."),
             danger="Deadly to monsters, none to players.",
             variants=[("egyptian", "Egyptian (default)", "Olive-brown scales, gold"), ("green", "Green", "The green one: jade scales"),
                       ("black", "Black", "Black-necked"), ("gold", "Gold", "Gold and cream"), ("red", "Red", "Red spitting cobra")]),
        {"Habitat": "Desert", "Young": "Live young", "Defence": "Hood, venom", "Signature": "Blessing, hunts monsters", "Danger": "None to players"})],
    "bug": [(
        dict(id="hornet_bug", name="Hornet", group="Forests", tag="Fierce · territorial", height=1.6,
             biomes=["forest", "birch_forest", "dark_forest", "flower_forest"], temperament="Fierce, territorial, fiercely loyal once won",
             ecology="Vespa (Hymenoptera: Vespidae), the largest of the social wasps. She chews wood into paper for her nest and hunts other "
                     "insects, bees above all.",
             interaction="Keep your distance until she knows you. She eats meat and fruit. A hornet with a broken wing can't fly: mend it with a "
                         "healing balm and she is yours for life.",
             signature=("Territorial", "She stings players who come too close and hunts bees and bee girls until someone befriends her. On patrol "
                        "she tears into hostile mobs. Friends get paper and clay."),
             danger="High up close.",
             variants=[("european", "European (default)", "Brown-red and yellow"), ("giant", "Giant", "Orange head, dark bands"),
                       ("baldfaced", "Bald-faced", "Black and white"), ("yellowlegged", "Yellow-legged", "Dark with an orange band"),
                       ("oriental", "Oriental", "Red-brown with a yellow band")]),
        {"Habitat": "Forests", "Signature": "Territorial, hunts bees", "Danger": "High up close"})],
    "merfolk": [(
        dict(id="dugong_mermaid", name="Dugong", group="Warm shallow seas", tag="Gentle · inseparable", height=2.0,
             biomes=["warm_ocean", "lukewarm_ocean"], temperament="Gentle, slow, devoted to her partner",
             ecology="Dugong dugon (Sirenia: Dugongidae), the sea cow of warm seagrass meadows, and the likely source of many mermaid stories. "
                     "She grazes all day and is almost never seen alone.",
             interaction="Bring seagrass. Swim slowly.",
             signature=("Sea cow", "Almost always found in pairs. Seagrass wins her over, and friends get sea treasure."),
             danger="None.",
             variants=[("grey", "Grey (default)", "Soft grey"), ("brown", "Brown", "Brown-grey"), ("pale", "Pale", "Pale cream"),
                       ("slate", "Slate", "Dark slate"), ("sandy", "Sandy", "Sandy")]),
        {"Habitat": "Warm shallows", "Young": "One calf", "On land": "Slow", "Signature": "Pairs, seagrass", "Danger": "None"})],
}
for _fid, _entries in FOURTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))


# fifth wave (2026-10-24): a new family, the canid girls (with the bears), and Pegasus, Lich, Mimic in existing ones
if not any(f["id"] == "canid" for f in C.FAMILIES):
    C.FAMILIES.append(dict(
        id="canid", volume="XX", title="Canids", subtitle="the canid family (and the bears)", food="Meat; the wolf loves bones, the panda bamboo",
        theme=dict(bg="#141210", panel="#1e1a17", panel2="#26211d", ink="#f2ebe2", muted="#c2b4a4", accent="#d89a4a",
                   line="#3e342c", green="#9ad06a", grad="#2e2216"),
        lede="Four canid and bear monster girls: the wolf and her pack, the great white polar bear, the bamboo-loving panda and Anubis, "
             "jackal of the desert tombs. Loyal, strong, and kin to the beasts of the vanilla world.",
        hero=dict(eyebrow="The central similarity", title="Kin to the beasts.",
                  text="Every one of them has vanilla kin, except Anubis, who keeps the dead: wolves, polar bears and pandas follow her "
                       "and trust whoever she trusts.",
                  bullets=[("Pack", "The wolf runs with a pack led by its alpha."), ("Hunt", "The wolf and the polar bear hunt while wild."),
                           ("Nature", "The panda is born with a personality, like vanilla pandas."),
                           ("Judgement", "Anubis weighs your heart against the feather.")]),
        lifecycle=[("1. Courtship", "At high friendship she may share your bed."), ("2. Pups", "A day later she bears a litter of pups or cubs."),
                   ("3. Young", "Her young stay by their mother."), ("4. Grown", "Grown in five days, devoted to their father.")],
        systems=[("Pack", "Wolves spawn in packs. The first is the alpha and the others follow her; wild, they hunt sheep and rabbits."),
                 ("Wolf kin", "A befriended wolf girl's tamed wolves join her fights."),
                 ("Polar hunt", "A wild polar bear hunts seal mermaids and penguin harpies, fishes at open water and guards her cubs."),
                 ("Personality", "Pandas are lazy, worried, playful, aggressive, weak or normal; a playful one rolls, a weak one sneezes."),
                 ("The weighing", "Anubis weighs every player who comes near: those who killed villagers or iron golems are condemned and hunted; "
                                  "light hearts may open the tomb chests she guards.")],
        friendship=dict(builds=["Meat (+2); bones for the wolf (+3), bamboo for the panda (+2), fish for the polar bear (+2)", "Petting"],
                        costs=["Hitting her", "For Anubis: killing villagers or iron golems"],
                        devoted="Her kin are yours: wolves, polar bears and pandas near her trust you."),
        mating=dict(form="Live young", young="Pups or cubs", grows="5 days", where="Beside her", note="Young use the adult model at 45%."),
        split=[("Habitat", []), ("Kin", []), ("Signature", []), ("Danger", [])],
        species=[],
        decisions=["Bears share the canid family (2026-10-24). The wolf is her own species, not a kobold."],
        questions=["Kobolds: a sixth canid later?"],
    ))

FIFTH = {
    "canid": [(
        dict(id="wolf_canid", name="Wolf", group="Taiga & forests", tag="Loyal · pack-minded", height=1.8,
             biomes=["taiga", "old_growth_pine_taiga", "old_growth_spruce_taiga", "snowy_taiga", "forest", "grove"], temperament="Loyal, proud, pack-minded",
             ecology="Canis lupus (Carnivora: Canidae). She runs with a pack led by its alpha, and kin of the vanilla wolves.",
             interaction="Bones. Never come between her and her pack.",
             signature=("Pack hunter", "Wild, her pack hunts sheep and rabbits. Befriended, your tamed wolves join her fights. She howls at night."),
             danger="Moderate in a pack.",
             variants=[("pale", "Pale (default)", "Pale grey-white"), ("woods", "Woods", "Brown"), ("snowy", "Snowy", "White"),
                       ("ashen", "Ashen", "Grey"), ("rusty", "Rusty", "Rust red")]),
        {"Habitat": "Taiga, forests", "Kin": "Wolves", "Signature": "Pack, leads your wolves", "Danger": "Moderate in a pack"}),
        (dict(id="polar_bear_canid", name="Polar Bear", group="Snow & ice", tag="Mighty · protective", height=2.2,
              biomes=["snowy_plains", "ice_spikes", "snowy_beach", "frozen_ocean"], temperament="Calm, mighty, fiercely protective",
              ecology="Ursus maritimus (Carnivora: Ursidae). The great white bear of the sea ice, who hunts seals.",
              interaction="Fish. Keep clear of her cubs.",
              signature=("Ice hunter", "Wild, she hunts seal mermaids and penguin harpies. She fishes at open water, and attacks anyone near her cubs."),
              danger="High near her cubs.",
              variants=[("white", "White (default)", "White"), ("cream", "Cream", "Cream"), ("grey", "Grey", "Grey"),
                        ("pizzly", "Pizzly", "Brown and white"), ("ice", "Ice", "Pale blue")]),
         {"Habitat": "Snow, ice", "Kin": "Polar bears", "Signature": "Hunts seals and penguins, fishes", "Danger": "High near cubs"}),
        (dict(id="panda_canid", name="Panda", group="Bamboo jungles", tag="Lazy · lovable", height=1.75,
              biomes=["bamboo_jungle"], temperament="Born with a personality",
              ecology="Ailuropoda melanoleuca (Carnivora: Ursidae). A bear who eats almost nothing but bamboo, and sleeps a lot.",
              interaction="Bamboo, and cake.",
              signature=("Personality", "Lazy (lies about), worried (fears strangers and storms), playful (rolls), aggressive (hits back hard, "
                                        "guards her friends), weak (sneezes often) or normal. She sneezes up slimeballs now and then."),
              danger="Low (aggressive ones: moderate).",
              variants=[("black", "Black (default)", "Black and white"), ("brown", "Brown", "Brown and white"), ("cream", "Cream", "Cream and white"),
                        ("grey", "Grey", "Grey and white"), ("red", "Red", "Rusty red and white")]),
         {"Habitat": "Bamboo jungles", "Kin": "Pandas", "Signature": "Personalities, sneezes", "Danger": "Low"}),
        (dict(id="anubis_canid", name="Anubis", group="Deserts, at night", tag="Solemn · just", height=1.95,
              biomes=["desert"], temperament="Solemn, just, merciless to the guilty",
              ecology="The jackal-headed guardian of Egyptian tombs, who weighs the hearts of the dead against the feather of Ma'at. "
                      "Her jackal is the golden jackal (Canis aureus).",
              interaction="Come with a clean conscience. Bread and honey as offerings.",
              signature=("The weighing", "She weighs every player who comes near. Anyone who killed villagers or iron golems is condemned "
                                         "and hunted. Others may open the tomb chests she guards."),
              danger="Deadly to the condemned, none to others.",
              variants=[("black", "Black (default)", "Black jackal, gold"), ("gold", "Gold", "Golden jackal"), ("silver", "Silver", "Grey"),
                        ("sand", "Sand", "Sandy"), ("night", "Night", "Blue-black")]),
         {"Habitat": "Deserts, at night", "Kin": "-", "Signature": "Weighs your heart, guards tombs", "Danger": "Only to the guilty"})],
    "centaur": [(
        dict(id="pegasus_centaur", name="Pegasus", group="Meadows near mountains", tag="Noble · proud", height=2.4,
             biomes=["meadow", "cherry_grove"], temperament="Proud, gentle, free",
             ecology="The winged horse of Greek myth, tamed by Bellerophon with a golden bridle.",
             interaction="Golden apples, and a golden bridle (gold horse armour) once she trusts you.",
             signature=("Winged", "Ridden, she leaps high into the air (hold jump) and glides down slowly. No true flight."),
             danger="Low.",
             variants=[("white", "White (default)", "White"), ("grey", "Grey", "Dapple grey"), ("palomino", "Palomino", "Gold"),
                       ("black", "Black", "Black"), ("dawn", "Dawn", "Rose and gold")]),
        {"Habitat": "Meadows", "Ridden": "With a golden bridle: leaps and glides", "Signature": "Wings", "Temperament": "Proud", "Danger": "Low"})],
    "undead": [(
        dict(id="lich_undead", name="Lich", group="The deep dark", tag="Ancient · scholarly", height=1.9,
             biomes=["deep_dark"], temperament="Cold, ancient, curious once calmed",
             ecology="A sorcerer who put her soul into a phylactery to cheat death.",
             interaction="Find and break her phylactery first. Then books.",
             signature=("Phylactery", "She casts slowness and weakness. While her phylactery stands she cannot die: she reforms beside it. "
                                      "Break it and she is peaceful. Befriended, she trades experience for books."),
             danger="High until her phylactery breaks.",
             variants=[("soul", "Soul (default)", "Pale blue soul fire"), ("bone", "Bone", "Ivory"), ("crimson", "Crimson", "Red"),
                       ("verdant", "Verdant", "Green"), ("void", "Void", "Black and violet")]),
        {"Haunts": "The deep dark", "Signature": "Phylactery, curses, books for XP", "Danger": "High until the phylactery breaks"})],
    "slime": [(
        dict(id="mimic_slime", name="Mimic", group="Unopened loot chests", tag="Greedy · cuddly", height=1.5,
             biomes=[], temperament="Greedy, then devoted",
             ecology="A slime who lives in a treasure chest and keeps its loot inside her. She waits in dungeons and mineshafts.",
             interaction="Open a loot chest at your own risk. Feed her gold.",
             signature=("Treasure chest", "One unopened loot chest in eight is a mimic: she bites once and keeps the loot. Gold wins her "
                                          "over; then she follows you and her loot is yours (crouch and right-click to open her)."),
             danger="One bite.",
             variants=[("oak", "Oak (default)", "An oak chest"), ("spruce", "Spruce", "A dark chest"), ("gilded", "Gilded", "A gold-trimmed chest"),
                       ("ender", "Ender", "An ender chest"), ("christmas", "Christmas", "A festive chest")]),
        {"Habitat": "Loot chests", "Favourite food": "Gold", "Element": "Greed", "Weakness": "Being opened", "Danger": "One bite"})],
}
for _fid, _entries in FIFTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))


# sixth wave (2026-10-24): a new family, the rodents (capybara, Ratatoskr), and kobold, grizzly, leshy, Xuanwu, La Llorona
if not any(f["id"] == "rodent" for f in C.FAMILIES):
    C.FAMILIES.append(dict(
        id="rodent", volume="XXI", title="Rodents", subtitle="the rodent family", food="Seeds, nuts and greens; the capybara loves melon",
        theme=dict(bg="#15130f", panel="#1f1c16", panel2="#27231b", ink="#f2ece0", muted="#c4b8a2", accent="#d8884a",
                   line="#40382c", green="#9ad06a", grad="#2e2414"),
        lede="Two rodent monster girls: the capybara, calmest animal alive, and Ratatoskr, the squirrel who runs up and down the "
             "world tree carrying messages. Small, busy, and good company.",
        hero=dict(eyebrow="The central similarity", title="Gnawers and gatherers.",
                  text="Rodents gather: the capybara gathers friends around her, Ratatoskr gathers seeds and saplings (and forgets half of them).",
                  bullets=[("Calm", "No predator hunts near a capybara."), ("Caches", "Ratatoskr's forgotten caches grow into trees."),
                           ("Messages", "Ratatoskr carries items between friends."), ("Company", "Idle girls like to sit beside a capybara.")]),
        lifecycle=[("1. Courtship", "At high friendship she may share your bed."), ("2. Pups", "A day later she bears a litter."),
                   ("3. Young", "Her young stay by their mother."), ("4. Grown", "Grown in five days, devoted to their father.")],
        systems=[("Calm", "Wolves, foxes, cats and ocelots hunt nothing within 8 blocks of a capybara; idle girls nearby sit beside her."),
                 ("Scatter-hoarding", "Ratatoskr picks up seeds and saplings and plants the ones she forgets."),
                 ("Messenger", "Hand Ratatoskr an item: she carries it to the nearest other player she is fond of (now and then swapped as a prank).")],
        friendship=dict(builds=["Food (+1), melon for the capybara (+2), seeds and berries for Ratatoskr (+2)", "Petting"],
                        costs=["Hitting her"], devoted="She shares her calm: her devoted friends heal beside her (Regeneration)."),
        mating=dict(form="Live young", young="Pups", grows="5 days", where="Beside her", note="Young use the adult model at 45%."),
        split=[("Habitat", []), ("Signature", []), ("Danger", [])],
        species=[],
        decisions=["The rodents start with two (2026-10-24)."],
        questions=["More rodents later: a hamster, a beaver?"],
    ))

SIXTH = {
    "canid": [(
        dict(id="kobold_canid", name="Kobold", group="Caves & mountains", tag="Eager · nose for ore", height=1.35,
             biomes=["dripstone_caves", "lush_caves", "windswept_hills", "stony_peaks"], temperament="Eager, nosy, loyal",
             ecology="The mine spirit of German folklore, who gave cobalt its name, here a little dog-girl miner in a helmet.",
             interaction="Bring her meat or bones and take her mining.",
             signature=("Nose for ore", "Befriended, she sniffs out the nearest diamond, gold or emerald ore and points the way. She yelps when "
                                        "the block you look at holds up sand or gravel, and she smells mimics: a mimic chest near her rattles."),
             danger="None.",
             variants=[("tan", "Tan (default)", "Tan fur"), ("cobalt", "Cobalt", "Cobalt blue"), ("black", "Black", "Black"),
                       ("cream", "Cream", "Cream"), ("red", "Red", "Rust red")]),
        {"Habitat": "Caves, mountains", "Kin": "Wolves", "Signature": "Finds ore, warns of cave-ins", "Danger": "None"}),
        (dict(id="grizzly_canid", name="Grizzly", group="Taiga", tag="Hearty · hungry", height=2.25,
              biomes=["taiga", "old_growth_pine_taiga", "old_growth_spruce_taiga", "windswept_forest"], temperament="Hearty, hungry, sleepy in winter",
              ecology="Ursus arctos horribilis (Carnivora: Ursidae), the brown bear of the northern forests.",
              interaction="Salmon and honey.",
              signature=("Forager", "She fishes salmon from rivers, raids full beehives for honey (the bees sting her for it) and hibernates "
                                    "while it snows."),
              danger="High if provoked.",
              variants=[("brown", "Brown (default)", "Brown"), ("cinnamon", "Cinnamon", "Red-brown"), ("silvertip", "Silvertip", "Grizzled"),
                        ("dark", "Dark", "Near black"), ("blond", "Blond", "Pale gold")]),
         {"Habitat": "Taiga", "Kin": "Polar bears", "Signature": "Salmon, honey raids, hibernation", "Danger": "High if provoked"})],
    "rodent": [(
        dict(id="capybara_rodent", name="Capybara", group="Rivers & swamps", tag="Calm · beloved", height=1.6,
             biomes=["swamp", "mangrove_swamp", "river"], temperament="Unflappable",
             ecology="Hydrochoerus hydrochaeris (Rodentia: Caviidae), the largest rodent alive, famous for letting every other animal "
                     "sit on or beside her.",
             interaction="Melon, sugar cane and apples. Just sit with her.",
             signature=("Calm", "No wolf, fox, cat or ocelot hunts within 8 blocks of her. Idle girls nearby sit down beside her. "
                                "She soaks in water with a yuzu on her head."),
             danger="None.",
             variants=[("brown", "Brown (default)", "Brown"), ("sandy", "Sandy", "Sandy"), ("dark", "Dark", "Dark brown"),
                       ("ginger", "Ginger", "Ginger"), ("grey", "Grey", "Grey")]),
        {"Habitat": "Rivers, swamps", "Signature": "Calm aura, girls sit with her", "Danger": "None"}),
        (dict(id="ratatoskr_rodent", name="Ratatoskr", group="Forests", tag="Quick · mischievous", height=1.4,
              biomes=["forest", "birch_forest", "dark_forest", "old_growth_birch_forest", "taiga"], temperament="Quick, chatty, a prankster",
              ecology="The squirrel who runs up and down Yggdrasil carrying insults between the eagle and the serpent. Her red squirrel is "
                      "Sciurus vulgaris (Rodentia: Sciuridae).",
              interaction="Seeds and berries. Hand her something to deliver.",
              signature=("Messenger", "She carries an item you give her to the nearest other player she is fond of (now and then she swaps it "
                                      "for something else). She hoards seeds and saplings, and the ones she forgets grow into trees."),
              danger="None.",
              variants=[("red", "Red (default)", "Red squirrel"), ("grey", "Grey", "Grey squirrel"), ("black", "Black", "Black"),
                        ("fox", "Fox", "Fox squirrel"), ("abert", "Abert's", "Tassel-eared grey")]),
         {"Habitat": "Forests", "Signature": "Messenger, plants trees", "Danger": "None"})],
    "flora": [(
        dict(id="leshy_flora", name="Leshy", group="Old-growth taiga", tag="Ancient · territorial", height=2.3,
             biomes=["old_growth_pine_taiga", "old_growth_spruce_taiga"], temperament="Ancient, jealous of his trees",
             ecology="The lord of the Slavic forest, antlered and bearded with moss, who leads woodcutters astray. The Vodyanoy's rival.",
             interaction="Plant saplings near him. Never clear-cut his forest.",
             signature=("Forest lord", "He counts the logs you cut and the saplings you plant near him. Planters get saplings and apples; "
                                       "clear-cutters get lost in his fog. Meeting a vodyanoy, he bellows at him."),
             danger="Low (he misleads, he does not fight).",
             variants=[("oak", "Oak (default)", "Oak bark, green moss"), ("birch", "Birch", "Pale birch"), ("spruce", "Spruce", "Dark spruce"),
                       ("autumn", "Autumn", "Autumn moss"), ("frost", "Frost", "Frosted")]),
        {"Habitat": "Old-growth taiga", "Signature": "Counts your trees, fog", "Danger": "Low"})],
    "lizardfolk": [(
        dict(id="black_tortoise_lizardfolk", name="Black Tortoise", group="Frozen rivers", tag="Steadfast · cold", height=1.85,
             biomes=["frozen_river", "snowy_beach", "frozen_ocean"], temperament="Steadfast, patient, cold",
             ecology="Xuanwu, the north of the Chinese Four Symbols: winter and water, a tortoise with a serpent coiled round her.",
             interaction="Fish and patience.",
             signature=("Shell", "Arrows glance off her shell. The north of the Four Symbols."),
             danger="Low.",
             variants=[("dark", "Dark (default)", "Blue-black"), ("jade", "Jade", "Jade"), ("bronze", "Bronze", "Bronze"),
                       ("ice", "Ice", "Pale ice"), ("black", "Black", "Black and red")]),
        {"Habitat": "Frozen rivers", "Signature": "Arrows glance off, Four Symbols", "Danger": "Low"})],
    "undead": [(
        dict(id="llorona_undead", name="La Llorona", group="Rivers, at night", tag="Grieving · kind", height=1.8,
             biomes=["river", "swamp"], temperament="Grieving, gentle",
             ecology="The weeping woman of Mexican folklore, who searches the rivers at night. Here she brings the lost home.",
             interaction="Pumpkin pie. Help her bring a lost one home.",
             signature=("Bringing them home", "At night she finds lost young animals and village children and leads them home to their "
                                              "own kind. Bring one home with her and she stops weeping for the night."),
             danger="None.",
             variants=[("white", "White (default)", "A white gown"), ("mourning", "Mourning", "Black"), ("rose", "Rose", "Rose"),
                       ("river", "River", "River blue"), ("marigold", "Marigold", "Marigold, for the Day of the Dead")]),
        {"Haunts": "Rivers, at night", "Signature": "Leads the lost home", "Danger": "None"})],
}
for _fid, _entries in SIXTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))


# turtles (2026-10-25): the red-eared slider, the Galapagos tortoise, the box turtle, the softshell and the matamata
TURTLES = [
    (dict(id="red_eared_slider_lizardfolk", name="Red-Eared Slider", group="Ponds & rivers", tag="Sociable · skittish", height=1.4,
          biomes=["river", "swamp", "forest", "flower_forest"], temperament="Sociable, easily startled",
          ecology="Trachemys scripta elegans (Testudines: Emydidae), the pond turtle with the red streak behind each eye, named for "
                  "sliding off her log into the water at the first sign of trouble.",
          interaction="Fish and kelp. Walk up slowly.",
          signature=("Sliding off", "Sliders bask together by the water. When a stranger comes within 6 blocks, every slider nearby slides "
                                    "into the nearest water at once. Every food is worth double to her."),
          danger="None.",
          variants=[("slider", "Red-eared (default)", "Green with red streaks"), ("yellow_bellied", "Yellow-bellied", "Yellow streaks"),
                    ("cumberland", "Cumberland", "Orange streaks"), ("pastel", "Pastel", "Pale pastel"), ("albino", "Albino", "Cream and yellow")]),
     {"Habitat": "Ponds, rivers", "Signature": "Slides into the water together", "Danger": "None"}),
    (dict(id="galapagos_tortoise_lizardfolk", name="Galapagos Tortoise", group="Islands & beaches", tag="Ancient · unhurried", height=1.95,
          biomes=["beach", "stony_shore", "savanna"], temperament="Ancient, patient, unhurried",
          ecology="Chelonoidis niger (Testudines: Testudinidae), the giant tortoise of the Galapagos, who lives well over a century.",
          interaction="Cactus and melon. Climb on with an empty hand once she is fond of you (crouch to pat her).",
          signature=("A ride on her shell", "The slowest girl there is, and the toughest. A friend can sit on her shell: she keeps "
                                            "going where she was going. Cactus does not prick her."),
          danger="None.",
          variants=[("galapagos", "Galapagos (default)", "Grey-brown"), ("santa_cruz", "Santa Cruz", "Dark"), ("pinta", "Pinta", "Pale grey, for Lonesome George"),
                    ("espanola", "Espanola", "Brown"), ("aldabra", "Aldabra", "Near black")]),
     {"Habitat": "Islands, beaches, savanna", "Signature": "Carries a friend on her shell", "Danger": "None"}),
    (dict(id="box_turtle_lizardfolk", name="Box Turtle", group="Forests", tag="Homebody · cautious", height=1.4,
          biomes=["forest", "birch_forest", "dark_forest", "flower_forest"], temperament="A cautious homebody",
          ecology="Terrapene carolina (Testudines: Emydidae), the box turtle, whose hinged shell closes completely, and who spends her whole "
                  "life within a few hundred metres of where she hatched.",
          interaction="Mushrooms and berries.",
          signature=("Shut tight", "Hidden in her shell she takes no damage at all. A stranger sprinting near her makes her shut. She never "
                                   "strays more than 16 blocks from home."),
          danger="None.",
          variants=[("eastern", "Eastern (default)", "Brown with yellow"), ("ornate", "Ornate", "Bright yellow rays"),
                    ("three_toed", "Three-toed", "Olive"), ("gulf_coast", "Gulf Coast", "Dark"), ("desert", "Desert", "Sandy")]),
     {"Habitat": "Forests", "Signature": "Shuts tight (no damage), stays home", "Danger": "None"}),
    (dict(id="softshell_turtle_lizardfolk", name="Softshell Turtle", group="Rivers", tag="Quick · nippy", height=1.55,
          biomes=["river", "swamp"], temperament="Quick, nippy",
          ecology="Apalone spinifera (Testudines: Trionychidae), the pancake-flat river turtle with a leathery shell and a snorkel nose, "
                  "who lies buried in the riverbed and bites hard.",
          interaction="Fish. Do not tread on her.",
          signature=("Buried", "Still on a sandy, muddy or gravel riverbed she is buried out of sight and breathes underwater. A stranger who "
                               "comes within a block and a half gets bitten. No armour, but quick on land and in water."),
          danger="Low (if stepped on).",
          variants=[("softshell", "Spiny (default)", "Olive, dark spots"), ("spiny", "Dark-spotted", "Dark spots"), ("smooth", "Smooth", "Plain tan"),
                    ("chinese", "Chinese", "Green-grey"), ("florida", "Florida", "Dark brown")]),
     {"Habitat": "Rivers", "Signature": "Buried in the riverbed, bites", "Danger": "Low"}),
    (dict(id="matamata_lizardfolk", name="Matamata", group="Swamps", tag="Still · strange", height=1.5,
          biomes=["swamp", "mangrove_swamp"], temperament="Very still, very patient",
          ecology="Chelus fimbriata (Testudines: Chelidae), the South American turtle who looks like a pile of dead leaves and swallows "
                  "fish with a sudden gulp.",
          interaction="Fish.",
          signature=("Leaf litter", "Still in the water she looks like dead leaves (invisible). Fish that swim within 4 blocks are sucked "
                                    "into her mouth; her friend gets the fish."),
          danger="None.",
          variants=[("matamata", "Matamata (default)", "Leaf brown"), ("mossy", "Mossy", "Mossy green"), ("dark", "Dark", "Dark brown"),
                    ("orinoco", "Orinoco", "Red-brown"), ("pale", "Pale", "Pale")]),
     {"Habitat": "Swamps", "Signature": "Looks like leaves, gulps fish", "Danger": "None"}),
]
_fam = C.family("lizardfolk")
for _species, _cols in TURTLES:
    if any(x["id"] == _species["id"] for x in _fam["species"]):
        continue
    _fam["species"].append(_species)
    for _label, _values in _fam.get("split", []):
        _values.append(_cols.get(_label, "-"))


# seventh wave (2026-10-25): crustaceans, marsupials, the armoured and the constructs as new families, the jellyfish
# (with the mollusks) and the ahuizotl (with the merfolk), plus homage variants of existing species


def _family(**kw):
    if not any(f["id"] == kw["id"] for f in C.FAMILIES):
        C.FAMILIES.append(dict(kw))


_LIFE = [("1. Courtship", "At high friendship she may share your bed."), ("2. Young", "A day later she bears young."),
         ("3. Growing", "Her young stay by their mother."), ("4. Grown", "Grown in five days, devoted to their father.")]
_family(id="crustacean", volume="XXII", title="Crustaceans", subtitle="the crab family", food="Fish and kelp",
        theme=dict(bg="#140f0f", panel="#1f1716", panel2="#281e1c", ink="#f4e8e2", muted="#c8b0a6", accent="#e0603a",
                   line="#442e2a", green="#7ac8b0", grad="#3a1a14"),
        lede="Beach-combing crab girls with a hard shell and harder claws. They mind their own business, sideways, until someone hits them.",
        hero=dict(eyebrow="The central similarity", title="Armour on the outside.",
                  text="A crab carries her skeleton outside: a carapace on her back and two big claws. The hermit carries a borrowed shell instead.",
                  bullets=[("Sideways", "They walk sideways."), ("Pinch", "Hit one and she pinches back."),
                           ("Shells", "The hermit crab swaps shells: give her a nautilus or shulker shell."), ("Beaches", "Sand, shores and warm shallows.")]),
        lifecycle=[("1. Courtship", "At high friendship she may share your bed."), ("2. Eggs", "She lays a clutch in wet sand."),
                   ("3. Young", "Little crabs hatch and follow her."), ("4. Grown", "Grown in five days, devoted to their father.")],
        systems=[("Pinch", "Neutral: hit one and she pinches back (Slowness for a moment)."),
                 ("Sideways", "They scuttle sideways."), ("Shell swap", "Hand the hermit crab a nautilus or shulker shell and she moves in (you get her old one back).")],
        friendship=dict(builds=["Fish (+1), kelp and sea pickles (+2)", "Petting"], costs=["Hitting her"], devoted="She pinches anything that hurts you."),
        mating=dict(form="Eggs", young="Little crabs", grows="5 days", where="Wet sand", note="Young use the adult model at 45%."),
        split=[("Habitat", []), ("Signature", []), ("Danger", [])], species=[],
        decisions=["Crabs and hermit crabs first (2026-10-25). The spider crab is a variant (a decorator crab)."],
        questions=["A coconut crab who climbs palms?"])
_family(id="marsupial", volume="XXIII", title="Marsupials", subtitle="the pouch family", food="Grass, leaves and fruit",
        theme=dict(bg="#15120e", panel="#201b15", panel2="#29221a", ink="#f4ece0", muted="#c8b89e", accent="#d8783a",
                   line="#42382a", green="#9ac86a", grad="#3a2a14"),
        lede="The Australian family. The kangaroo carries her young in her pouch and boxes anything that bothers them.",
        hero=dict(eyebrow="The central similarity", title="Young in the pouch.",
                  text="Their young ride in the mother's pouch until they are grown.",
                  bullets=[("Pouch", "Her joey rides in her pouch."), ("Kick", "Big knockback kicks."),
                           ("Mobs", "Kangaroos live in mobs."), ("Outback", "Savanna and badlands.")]),
        lifecycle=[("1. Courtship", "At high friendship she may share your bed."), ("2. Joey", "A day later a joey is born."),
                   ("3. In the pouch", "The joey rides in her pouch while it grows."), ("4. Grown", "Grown in five days, devoted to their father.")],
        systems=[("Pouch", "Her joey rides in her pouch until it is grown."), ("Kick", "She fights with kicks that knock back hard.")],
        friendship=dict(builds=["Grass, wheat and apples (+1), sweet berries (+2)", "Petting"], costs=["Hitting her"],
                        devoted="She kicks whatever goes for you."),
        mating=dict(form="Live young", young="Joeys", grows="5 days", where="In her pouch", note="Young use the adult model at 45%."),
        split=[("Habitat", []), ("Signature", []), ("Danger", [])], species=[],
        decisions=["The kangaroo first (2026-10-25); the koala comes in the next wave."], questions=["A wombat? A Tasmanian devil?"])
_family(id="armoured", volume="XXIV", title="Armoured", subtitle="the armadillo family", food="Insects and spider eyes; she likes beetroot",
        theme=dict(bg="#15120f", panel="#201b16", panel2="#29231c", ink="#f2eadf", muted="#c6b6a0", accent="#c8884a",
                   line="#443a2c", green="#a8c86a", grad="#3a2a18"),
        lede="The armadillo girls: banded armour on their backs, and when they are scared they roll up into a ball.",
        hero=dict(eyebrow="The central similarity", title="Roll up.",
                  text="Frightened, she rolls up into a ball, and nothing gets through. Befriended, she rolls into whatever goes for you.",
                  bullets=[("Ball", "No damage while rolled up."), ("Rolling charge", "Befriended, she rolls into monsters."),
                           ("Scutes", "She sheds armadillo scutes as presents."), ("Kin", "Vanilla armadillos.")]),
        lifecycle=_LIFE, systems=[("Rolling up", "A hit or a sprinting stranger rolls her up for a few seconds: she takes no damage."),
                                   ("Rolling charge", "Befriended, she curls up and rolls into a monster near her friend, knocking it back.")],
        friendship=dict(builds=["Spider eyes (+2), beetroot and sweet berries (+1)", "Petting"], costs=["Hitting her"],
                        devoted="She rolls into anything that hurts you."),
        mating=dict(form="Live young", young="Pups", grows="5 days", where="Beside her", note="Young use the adult model at 45%."),
        split=[("Habitat", []), ("Signature", []), ("Danger", [])], species=[],
        decisions=["One species for now (2026-10-25), with a ranger variant (a cowboy hat and scarf)."], questions=["A pangolin?"])
_family(id="construct", volume="XXV", title="Constructs", subtitle="the golem family", food="Clay (she patches herself)",
        theme=dict(bg="#13110f", panel="#1d1a17", panel2="#26221e", ink="#f0e8e0", muted="#c0b2a4", accent="#b8784a",
                   line="#3e3630", green="#8ab08a", grad="#2e2018"),
        lede="Girls shaped by hand. The clay golem of Jewish folklore, here a village guardian who leads the iron golems.",
        hero=dict(eyebrow="The central similarity", title="Made, not born.",
                  text="A construct is shaped, not born: she has no young, and when she falls she breaks apart into clay.",
                  bullets=[("Toughest", "More health than an iron golem."), ("Guardian", "She fights monsters near villagers."),
                           ("Kin", "Iron golems follow her."), ("Breaks", "She breaks into pieces when she falls.")]),
        lifecycle=[("1. Shaped", "Golems are made, not born."), ("2. -", "No young."), ("3. -", "-"), ("4. -", "-")],
        systems=[("Guardian", "She attacks monsters within 16 blocks of a villager."), ("Patched", "Clay heals her."),
                 ("Breaking", "When she falls she breaks into pieces and leaves clay behind.")],
        friendship=dict(builds=["Clay balls (+1), clay blocks (+3)", "Petting"], costs=["Hitting her"], devoted="She guards you like a village."),
        mating=dict(form="None (she is made)", young="-", grows="-", where="-", note="A golem cannot have young."),
        split=[("Habitat", []), ("Signature", []), ("Danger", [])], species=[],
        decisions=["The clay golem first (2026-10-25). Iron golems are now her kin, not Frankenstein's monster's."],
        questions=["A copper golem when the game has one?"])

SEVENTH = {
    "crustacean": [
        (dict(id="crab_crustacean", name="Crab", group="Beaches", tag="Sideways · snappy", height=1.5, biomes=["beach", "mangrove_swamp", "warm_ocean"],
              temperament="Busy, defensive",
              ecology="True crabs (Decapoda: Brachyura): a hard carapace, two claws, and a sideways walk.",
              interaction="Fish and kelp. Do not hit her.",
              signature=("Pinch", "Neutral: she pinches whoever hits her first (Slowness). The decorator variant dresses her shell in kelp and coral, "
                                  "like the spider crab's relatives."),
              danger="Low (if hit).",
              variants=[("red", "Red (default)", "Red"), ("blue", "Blue crab", "Blue claws"), ("ghost", "Ghost crab", "Sand-pale"),
                        ("fiddler", "Fiddler", "Orange and purple"), ("decorator", "Decorator", "Kelp and coral on her shell")]),
         {"Habitat": "Beaches", "Signature": "Pinches back, walks sideways", "Danger": "Low"}),
        (dict(id="hermit_crab_crustacean", name="Hermit Crab", group="Beaches", tag="Shy · house-proud", height=1.35, biomes=["beach", "warm_ocean"],
              temperament="Shy, house-proud",
              ecology="Hermit crabs (Decapoda: Paguroidea) live in borrowed shells and move house as they grow.",
              interaction="Fish. Offer her a better shell.",
              signature=("Shell swap", "Give her a nautilus shell or a shulker shell and she moves in; you get her old one back. "
                                       "She pinches whoever hits her first."),
              danger="Low (if hit).",
              variants=[("whelk", "Whelk (default)", "A whelk shell"), ("nautilus", "Nautilus", "A nautilus shell"), ("shulker", "Shulker", "A shulker shell"),
                        ("turban", "Turban", "A green turban shell"), ("coral", "Coral", "A pink shell")]),
         {"Habitat": "Beaches", "Signature": "Swaps shells, pinches back", "Danger": "Low"})],
    "marsupial": [
        (dict(id="kangaroo_marsupial", name="Kangaroo", group="Savanna", tag="Bouncy · scrappy", height=1.9, biomes=["savanna", "savanna_plateau", "badlands"],
              temperament="Bouncy, scrappy",
              ecology="Red and grey kangaroos (Diprotodontia: Macropodidae), who box and kick to settle things.",
              interaction="Grass and berries.",
              signature=("Pouch and kick", "Her joey rides in her pouch until it is grown. She fights with kicks that knock back hard. "
                                           "The outback variant wears a red hoodie and sunglasses."),
              danger="Medium (if provoked).",
              variants=[("red", "Red (default)", "Red-brown"), ("grey", "Eastern grey", "Grey"), ("wallaroo", "Wallaroo", "Dark"),
                        ("albino", "Albino", "White"), ("outback", "Outback", "A red hoodie and sunglasses")]),
         {"Habitat": "Savanna, badlands", "Signature": "Joey in the pouch, big kicks", "Danger": "Medium"})],
    "armoured": [
        (dict(id="armadillo_armoured", name="Armadillo", group="Savanna & badlands", tag="Timid · tough", height=1.45, biomes=["savanna", "badlands", "wooded_badlands"],
              temperament="Timid, then very tough",
              ecology="Armadillos (Cingulata: Dasypodidae): banded armour of bony scutes. The three-banded one rolls into a perfect ball.",
              interaction="Spider eyes, beetroot and berries.",
              signature=("Roll up", "A hit or a sprinting stranger rolls her into a ball: she takes no damage. Befriended, she rolls into monsters near "
                                    "her friend. The ranger variant wears a cowboy hat and scarf, a rolling frontier guard."),
              danger="None.",
              variants=[("nine_banded", "Nine-banded (default)", "Grey-brown"), ("three_banded", "Three-banded", "Tan"),
                        ("pink_fairy", "Pink fairy", "Pink and white"), ("giant", "Giant", "Dark"), ("ranger", "Ranger", "Rust-orange, cowboy hat and scarf")]),
         {"Habitat": "Savanna, badlands", "Signature": "Rolls up, rolling charge", "Danger": "None"})],
    "construct": [
        (dict(id="golem_construct", name="Golem", group="Villages", tag="Steadfast · silent", height=2.4, biomes=["plains", "meadow", "savanna"],
              temperament="Steadfast, quiet",
              ecology="The golem of Jewish folklore: a figure shaped from clay and brought to life by a word on her brow.",
              interaction="Clay. Defend the village with her.",
              signature=("Village guardian", "120 health, more than an iron golem. She fights monsters near villagers, and iron golems follow her. "
                                             "When she falls she breaks into pieces."),
              danger="High for monsters.",
              variants=[("clay", "Clay (default)", "Grey clay"), ("terracotta", "Terracotta", "Fired orange"), ("red_clay", "Red clay", "Red"),
                        ("kaolin", "Kaolin", "White clay"), ("mud", "River mud", "Dark brown")]),
         {"Habitat": "Near villages", "Signature": "Guards villagers, breaks apart", "Danger": "High for monsters"})],
    "mollusk": [
        (dict(id="jellyfish_mollusk", name="Jellyfish", group="Warm ocean", tag="Drifting · glowing", height=1.6, biomes=["warm_ocean", "lukewarm_ocean", "deep_lukewarm_ocean"],
              temperament="Dreamy, drifting",
              ecology="Jellyfish (Cnidaria: Scyphozoa). Not a mollusk at all, but she drifts with them.",
              interaction="Fish.",
              signature=("Sting and glow", "She glows at night and stings whatever bumps into her (Poison). Friends are never stung, "
                                           "and swimming near her they see in the dark (Night Vision)."),
              danger="Low (stings).",
              variants=[("moon", "Moon (default)", "Pale blue"), ("pink", "Pink", "Pink"), ("box", "Box", "Clear"),
                        ("lion", "Lion's mane", "Orange"), ("purple", "Purple", "Purple-striped")]),
         {"Habitat": "Warm ocean", "Body": "Bell and tentacle skirt", "Signature": "Stings, glows", "Danger": "Low"})],
    "merfolk": [
        (dict(id="ahuizotl_mermaid", name="Ahuizotl", group="Rivers", tag="Thieving · playful", height=1.5, biomes=["river", "jungle", "mangrove_swamp"],
              temperament="A playful thief",
              ecology="The Aztec water creature with a hand at the end of her tail, perhaps inspired by the neotropical otter.",
              interaction="Fish. Keep a lantern in your hand.",
              signature=("Fish thief", "A wild ahuizotl steals fish off your fishing line with the hand on her tail. A lantern in your hand keeps her "
                                       "away. Befriended, she fishes for you instead."),
              danger="None.",
              variants=[("otter", "Otter (default)", "Brown"), ("dark", "Dark", "Near black"), ("golden", "Golden", "Golden brown"),
                        ("grey", "Grey", "Grey"), ("jade", "Jade", "Green-tinged")]),
         {"Habitat": "Rivers", "Young": "Live pups", "On land": "Slow", "Signature": "Steals fish off your line", "Danger": "None"})],
}
for _fid, _entries in SEVENTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# homage variants for existing species (2026-10-25): generic names, the look of a favourite character
for _sid, _variant in [("cactus_flora", ("gunman", "Gunman", "A straw sombrero, a big black mustache, a poncho and a bandolier")),
                       ("cactus_flora", ("blossom", "Blossom", "A straw sombrero, braids with red bows, a cow-print top and a blue poncho, a lasso")),
                       ("living_doll_undead", ("lost", "Lost", "Red curls with purple bows, a patched green dress, a second face on the back of her head")),
                       ("ocelot_nekomata", ("stitched", "Stitched", "Sewn together, a purple patch over half her head, a striped top")),
                       ("salamander_lizardfolk", ("nurse", "Nurse", "Pink and spotted, a nurse's dress and cap, a huge syringe in hand")),
                       ("wolf_canid", ("mummy", "Mummy", "A bandaged head, red shirt and blue overalls, a cleaver in the head")),
                       ("anubis_canid", ("mummy", "Mummy", "A bandaged head, a grey suit, a machete in the head"))]:
    for _fam in C.FAMILIES:
        for _s in _fam["species"]:
            if _s["id"] == _sid and all(v[0] != _variant[0] for v in _s["variants"]):
                _s["variants"].append(_variant)


# eighth wave (2026-10-25): unicorn, black bear, koala, gecko, chupacabra, firefly, mummy (the flamingo is a harpy: her own script)
EIGHTH = {
    "centaur": [
        (dict(id="unicorn_centaur", name="Unicorn", group="Flower forests", tag="Pure · healing", height=2.2,
              biomes=["flower_forest", "cherry_grove", "meadow"], temperament="Gentle, wary of the impure",
              ecology="The unicorn of European legend: a white horse with a single spiralled horn, who only lets the pure of heart come near.",
              interaction="Golden carrots and sugar. Come with clean hands.",
              signature=("Pure heart, healing horn", "A player who has killed villagers or iron golems can never befriend her, and she keeps away "
                                                     "from them. Her horn clears poison and wither from friends near her and speeds up the cure of "
                                                     "zombie villagers. Friends may ride her."),
              danger="None.",
              variants=[("white", "White (default)", "White, golden horn"), ("silver", "Silver", "Silver-grey"), ("rose", "Rose", "Rose pink"),
                        ("midnight", "Midnight", "Deep blue"), ("rainbow", "Rainbow", "A rainbow mane")]),
         {"Habitat": "Flower forests, cherry groves", "Ridden": "By friends", "Signature": "Heals, pure hearts only", "Temperament": "Gentle",
          "Danger": "None"})],
    "canid": [
        (dict(id="black_bear_canid", name="Black Bear", group="Forests", tag="Timid · greedy for berries", height=2.0,
              biomes=["forest", "birch_forest", "taiga", "old_growth_birch_forest"], temperament="Timid, hungry",
              ecology="Ursus americanus (Carnivora: Ursidae), the forest bear who climbs trees and raids berry patches.",
              interaction="Sweet berries and honey. Move slowly.",
              signature=("Scares easily", "She climbs trees and eats berry bushes bare. A sprinting stranger, an explosion, a bell or a goat horn "
                                          "sends her running, dropping what she holds; befriended, only explosions still do."),
              danger="Low.",
              variants=[("black", "Black (default)", "Black"), ("cinnamon", "Cinnamon", "Red-brown"), ("glacier", "Glacier", "Blue-grey"),
                        ("kermode", "Kermode", "White, the spirit bear"), ("brown", "Brown", "Brown")]),
         {"Habitat": "Forests", "Kin": "Polar bears", "Signature": "Climbs trees, raids berries, scares easily", "Danger": "Low"})],
    "marsupial": [
        (dict(id="koala_marsupial", name="Koala", group="Forests", tag="Sleepy · clingy", height=1.4,
              biomes=["forest", "birch_forest", "savanna_plateau"], temperament="Sleepy",
              ecology="Phascolarctos cinereus (Diprotodontia: Phascolarctidae), who eats only leaves and sleeps most of the day.",
              interaction="Leaves. Do not wake her.",
              signature=("Asleep", "Awake only at dawn and dusk; the rest of the time she sleeps clinging to a tree trunk. Her joey rides on her "
                                   "back. The rare drop bear drops onto whoever walks under her tree (a hug, once she likes you)."),
              danger="None (the drop bear: a fright).",
              variants=[("grey", "Grey (default)", "Grey"), ("brown", "Brown", "Brown-grey"), ("pale", "Pale", "Pale grey"),
                        ("dark", "Dark", "Dark grey"), ("drop_bear", "Drop bear", "Red eyes, sharp claws (rare)")]),
         {"Habitat": "Forests", "Signature": "Sleeps on trees, the drop bear", "Danger": "None"})],
    "lizardfolk": [
        (dict(id="gecko_lizardfolk", name="Gecko", group="Jungles & deserts", tag="Quick · curious", height=1.35,
              biomes=["jungle", "sparse_jungle", "desert", "savanna"], temperament="Quick, curious",
              ecology="Geckos (Squamata: Gekkota), who climb sheer walls on sticky toe pads and lick their own eyes clean.",
              interaction="Fruit.",
              signature=("Climber", "Fast, and she climbs walls. Curious: she trails you and goes to look at anything you drop. She sheds her "
                                    "tail when hit, and it grows back."),
              danger="None.",
              variants=[("leopard", "Leopard (default)", "Yellow with spots"), ("crested", "Crested", "Orange, a crest"),
                        ("tokay", "Tokay", "Blue-grey, orange spots"), ("day", "Day gecko", "Bright green"), ("gargoyle", "Gargoyle", "Mottled grey")]),
         {"Habitat": "Jungles, deserts", "Signature": "Climbs walls, sheds her tail", "Danger": "None"}),
        (dict(id="chupacabra_lizardfolk", name="Chupacabra", group="Mountain edges", tag="Shy · unruly", height=1.6,
              biomes=["windswept_hills", "jagged_peaks", "meadow", "badlands"], temperament="Shy, ungovernable",
              ecology="The goat-sucker of 1990s Puerto Rico: a spiny reptilian cryptid. (The later 'canine chupacabras' were coyotes with mange.)",
              interaction="Mutton. Do not stare.",
              signature=("Goat hunter", "She hunts wild goats only, never tamed, named, leashed or fenced ones, and hides when you look straight at "
                                        "her. Fed, she lingers near you but takes no orders; at high friendship she leaves mutton."),
              danger="None (to players).",
              variants=[("grey", "Grey (default)", "Grey-green, red spines"), ("dark", "Dark", "Charcoal"), ("sand", "Sand", "Sandy"),
                        ("blue", "Blue", "Blue-grey"), ("red", "Red", "Rust red")]),
         {"Habitat": "Mountain edges", "Signature": "Hunts wild goats, hides when watched", "Danger": "None"})],
    "bug": [
        (dict(id="firefly_bug", name="Firefly", group="Meadows at night", tag="Twinkling · tiny", height=0.9,
              biomes=["meadow", "plains", "swamp", "mangrove_swamp"], temperament="Shy, twinkling",
              ecology="Fireflies (Coleoptera: Lampyridae), whose glowing bellies blink, in some places all in time.",
              interaction="Sugar and honey. A glass bottle catches her (why?).",
              signature=("Blinking", "She glows at night, and the fireflies around her blink in time. A glass bottle catches her (or a fairy) in "
                                     "a jar, a light block; break it to let her out."),
              danger="None.",
              variants=[("amber", "Amber (default)", "Amber glow"), ("green", "Green", "Green glow"), ("blue", "Blue", "Blue ghost"),
                        ("pink", "Pink", "Pink glow"), ("white", "White", "White glow")]),
         {"Habitat": "Meadows, swamps at night", "Signature": "Glows, blinks in time, jars", "Danger": "None"})],
    "undead": [
        (dict(id="mummy_undead", name="Mummy", group="Desert pyramids", tag="Bashful · ancient", height=1.8,
              biomes=["desert"], temperament="Ancient, bashful",
              ecology="The wrapped dead of Egyptian tombs, here a bashful guardian of the pyramid's loot.",
              interaction="Paper and gold. Pull gently.",
              signature=("Unravelling", "Hits loosen her bandages (string and paper). Befriended, a crouching pull with an empty hand unravels her "
                                        "a layer at a time, down to skimpy wraps; then she hurries, spins and wraps herself up again."),
              danger="Low.",
              variants=[("linen", "Linen (default)", "Pale linen"), ("aged", "Aged", "Yellowed"), ("royal", "Royal", "Gold and lapis"),
                        ("sand", "Sand", "Sandy"), ("night", "Night", "Grey, glowing eyes")]),
         {"Haunts": "Desert pyramids", "Signature": "Unravels and rewraps", "Danger": "Low"})],
}
for _fid, _entries in EIGHTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))


# ninth wave (2026-10-25): sheep, highland cow, yak, the bats (a new family), the dark elf, medusa, oni, mantis, ender
# slime, wolpertinger, venus flytrap, siren
_family(id="bat", volume="XXVI", title="Bats", subtitle="the bat family", food="Fruit (apples, melon, berries)",
        theme=dict(bg="#110f14", panel="#1a1720", panel2="#221e2a", ink="#ece6f4", muted="#b4a8c4", accent="#9a6ad8",
                   line="#3a3046", green="#8ac8a0", grad="#2a1a3a"),
        lede="Night fliers on leathery wings. By day they hang upside down from cave ceilings; by night they hear the whole world around them.",
        hero=dict(eyebrow="The central similarity", title="Hanging upside down.",
                  text="A bat rests hanging from a ceiling by her feet, wings wrapped round her, and flies at night.",
                  bullets=[("Roost", "She hangs from cave ceilings to rest."), ("Echolocation", "Befriended, she shows you the mobs near you, through walls."),
                           ("Night", "Awake at night."), ("Kin", "Vanilla bats.")]),
        lifecycle=_LIFE,
        systems=[("Roosting", "By day she hangs upside down under a ceiling (a cave roof, an overhang) and sleeps."),
                 ("Echolocation", "Befriended, she pings every few seconds: monsters near her friend glow for a moment, through walls."),
                 ("Flight", "She flies, and never takes fall damage.")],
        friendship=dict(builds=["Apples, melon slices and sweet berries (+1), glow berries (+2)", "Petting"], costs=["Hitting her", "Waking her"],
                        devoted="Her pings never stop while you are near."),
        mating=dict(form="Live young", young="Pups", grows="5 days", where="Beside her", note="Young use the adult model at 45%."),
        split=[("Habitat", []), ("Signature", []), ("Danger", [])], species=[],
        decisions=["One species first (2026-10-25), the fruit bat as a variant (a fox face). Vampires keep bats too."],
        questions=["A vampire bat who drinks from sleeping mobs?"])

NINTH = {
    "minotaur": [
        (dict(id="sheep_minotaur", name="Sheep", group="Meadows", tag="Woolly · gentle", height=1.6, biomes=["meadow", "plains", "windswept_hills"],
              temperament="Gentle, flock-minded",
              ecology="Domestic sheep (Bovidae: Ovis aries), whose wool never stops growing. The family's mildest member.",
              interaction="Wheat and grass. Shears.",
              signature=("Wool", "Shears take her wool (it regrows after she grazes). Dye her like a sheep and her wool takes the colour. "
                                 "Vanilla sheep follow her as their shepherd."),
              danger="None.",
              variants=[("white", "White (default)", "White wool"), ("black", "Black", "Black wool"), ("brown", "Brown", "Brown wool"),
                        ("jacob", "Jacob", "Black-spotted wool"), ("merino", "Merino", "Cream, extra-fine wool")]),
         {"Habitat": "Meadows, plains", "Temper": "Gentle", "Signature": "Shear and dye her, sheep follow her", "Danger": "None"}),
        (dict(id="highland_minotaur", name="Highland", group="Snowy hills", tag="Shaggy · unbothered", height=1.8,
              biomes=["snowy_slopes", "grove", "windswept_hills", "snowy_plains"], temperament="Calm, shaggy",
              ecology="Highland cattle (Bovidae: Bos taurus), the long-haired cattle of the Scottish Highlands, with a fringe over the eyes.",
              interaction="Wheat.",
              signature=("Never cold", "Her shaggy coat keeps out any cold: she never freezes, and walks on powder snow."),
              danger="Low (if provoked).",
              variants=[("red", "Red (default)", "Ginger"), ("black", "Black", "Black"), ("dun", "Dun", "Pale gold"),
                        ("white", "White", "Cream"), ("brindle", "Brindle", "Striped brown")]),
         {"Habitat": "Snowy hills", "Temper": "Calm", "Signature": "Never freezes", "Danger": "Low"}),
        (dict(id="yak_minotaur", name="Yak", group="Mountains", tag="Sturdy · loyal", height=1.9,
              biomes=["jagged_peaks", "frozen_peaks", "stony_peaks", "snowy_slopes"], temperament="Sturdy, patient",
              ecology="The yak (Bovidae: Bos grunniens) of the Himalaya, a pack animal at home far above the tree line.",
              interaction="Wheat. Give her a chest.",
              signature=("Pack yak", "Give her a chest and she carries two rows of cargo (crouch and use her to open it), following you over "
                                     "the mountains. She never freezes either."),
              danger="Low (if provoked).",
              variants=[("brown", "Brown (default)", "Dark brown"), ("black", "Black", "Black"), ("white", "White", "White"),
                        ("golden", "Golden", "Golden"), ("pied", "Pied", "Black and white")]),
         {"Habitat": "High mountains", "Temper": "Patient", "Signature": "Carries a chest of cargo", "Danger": "Low"})],
    "bat": [
        (dict(id="cave_bat", name="Bat", group="Caves & forests", tag="Nocturnal · keen-eared", height=1.4,
              biomes=["dripstone_caves", "lush_caves", "dark_forest", "jungle"], temperament="Shy by day, lively by night",
              ecology="Bats (Chiroptera), the only mammals that truly fly, who find their way in the dark by echolocation.",
              interaction="Fruit. Let her sleep.",
              signature=("Roost and ping", "She hangs upside down from ceilings to rest. Befriended, she pings every few seconds and monsters "
                                           "near her friend glow for a moment, through walls. The fruit bat has a fox face."),
              danger="None.",
              variants=[("brown", "Brown (default)", "Brown"), ("fruit", "Fruit bat", "Fox-faced, golden mantle"), ("vampire", "Vampire", "Dark grey, red eyes"),
                        ("ghost", "Ghost", "White"), ("hoary", "Hoary", "Frosted brown")]),
         {"Habitat": "Caves, dark forests", "Signature": "Roosts upside down, echolocation", "Danger": "None"})],
    "elf": [
        (dict(id="dark_elf", name="Dark Elf", group="The deep dark", tag="Silent · watchful", height=1.75, biomes=["deep_dark"],
              temperament="Silent, watchful",
              ecology="Elves who went down into the deep dark and learned to live beside the Warden.",
              interaction="Echo shards and sculk. Move quietly.",
              signature=("Warden's kin", "She walks silently and never sets off sculk sensors or shriekers. While she likes you, a Warden near "
                                         "her lets you be. Her gifts are echo shards and sculk."),
              danger="Low (the Warden is not).",
              variants=[("sculk", "Sculk (default)", "Grey-blue skin, teal glow"), ("ash", "Ash", "Ash grey"), ("abyss", "Abyss", "Near black"),
                        ("echo", "Echo", "Teal"), ("pale", "Pale", "White hair, pale skin")]),
         {"Habitat": "The deep dark", "Trades": "Echo shards, sculk", "Signature": "Silent, calms the Warden", "Danger": "Low"})],
    "lamia": [
        (dict(id="medusa_lamia", name="Medusa", group="Stony shores", tag="Stony-eyed · lonely", height=1.8, biomes=["stony_shore", "stony_peaks", "desert"],
              temperament="Lonely, guarded",
              ecology="The gorgon of Greek myth, with living snakes for hair, whose gaze turns onlookers to stone.",
              interaction="Rabbit and eggs. Do not meet her eyes.",
              signature=("Petrifying gaze", "Look a stranger medusa in the eyes and you are slowed nearly to a stop; monsters that meet her gaze "
                                            "freeze for a few seconds. Befriended, she wears a blindfold so it is safe to look at her."),
              danger="Medium (her gaze).",
              variants=[("emerald", "Emerald (default)", "Green snakes"), ("bronze", "Bronze", "Bronze snakes"), ("onyx", "Onyx", "Black snakes"),
                        ("marble", "Marble", "White, stone-pale"), ("coral", "Coral", "Red-banded snakes")]),
         {"Habitat": "Stony shores and peaks", "Young": "Eggs", "Defence": "Her gaze", "Signature": "Petrifying gaze, blindfold for friends",
          "Danger": "Medium"})],
    "devil": [
        (dict(id="oni_devil", name="Oni", group="Mountains at night", tag="Mighty · boisterous", height=2.2,
              biomes=["windswept_hills", "cherry_grove", "taiga", "stony_peaks"], temperament="Boisterous, fond of a drink",
              ecology="The ogres of Japanese folklore: horned, red or blue, with an iron club, driven off every spring by thrown beans.",
              interaction="Honey bottles. Cocoa beans drive her off.",
              signature=("Iron club", "Big and hard-hitting. Thrown beans (cocoa beans) drive her away, as at Setsubun. Befriended, she guards "
                                      "your door at night."),
              danger="High (if provoked).",
              variants=[("red", "Red (default)", "Red skin"), ("blue", "Blue", "Blue skin"), ("yellow", "Yellow", "Yellow skin"),
                        ("green", "Green", "Green skin"), ("black", "Black", "Grey-black skin")]),
         {"Habitat": "Mountains at night", "Signature": "Iron club, fears beans, guards your door", "Danger": "High"})],
    "bug": [
        (dict(id="mantis_bug", name="Mantis", group="Flower forests", tag="Still · lightning-fast", height=1.7,
              biomes=["flower_forest", "jungle", "meadow", "savanna"], temperament="Patient, then sudden",
              ecology="Praying mantises (Mantodea), ambush hunters who wait folded in prayer and strike faster than the eye can follow.",
              interaction="Spider eyes. Keep still.",
              signature=("Ambush", "She holds a still prayer pose, then strikes in a flash at silverfish, endermites and spiders. The orchid mantis "
                                   "cannot be seen among flowers."),
              danger="Low (to players).",
              variants=[("green", "Green (default)", "Green"), ("brown", "Brown", "Bark brown"), ("orchid", "Orchid", "Pink and white"),
                        ("leaf", "Leaf", "Leaf green, a leaf cape"), ("ghost", "Ghost", "Pale")]),
         {"Habitat": "Flower forests, jungles", "Signature": "Prays, strikes arthropods", "Danger": "Low"})],
    "slime": [
        (dict(id="ender_slime", name="Ender Slime", group="The End", tag="Flickering · light-fingered", height=1.7,
              biomes=["end_highlands", "end_midlands", "warped_forest"], temperament="Shy, light-fingered",
              ecology="A slime that ate an ender pearl: she flickers from place to place and moves the world around a block at a time.",
              interaction="Chorus fruit and ender pearls.",
              signature=("Teleporting", "She teleports away when hit, like an enderman, picks up loose blocks and puts them down somewhere else. "
                                        "Endermites are her kin."),
              danger="Low.",
              variants=[("void", "Void (default)", "Black and violet"), ("chorus", "Chorus", "Pale purple"), ("pearl", "Pearl", "Teal"),
                        ("end_stone", "End stone", "Pale yellow"), ("shulker", "Shulker", "Purple and gold")]),
         {"Habitat": "The End, warped forests", "Favourite food": "Chorus fruit", "Element": "Ender", "Weakness": "Water",
          "Danger": "Low"})],
    "jackalope": [
        (dict(id="wolpertinger_jackalope", name="Wolpertinger", group="Bavarian forests", tag="Prankster · springy", height=1.5,
              biomes=["forest", "taiga", "old_growth_spruce_taiga", "windswept_forest"], temperament="Mischievous",
              ecology="The horned, winged rabbit of Bavarian folklore, said to show herself only at dusk to the right company.",
              interaction="Carrots. Mind your boots.",
              signature=("Prankster", "She double-jumps and glides down from heights on little wings. Now and then she swaps your boots for "
                                      "leather ones; feed her to get yours back."),
              danger="None.",
              variants=[("brown", "Brown (default)", "Brown, pheasant wings"), ("grey", "Grey", "Grey, duck wings"), ("white", "Snow", "White"),
                        ("red", "Red", "Fox-red"), ("black", "Black", "Black, raven wings")]),
         {"Habitat": "Forests", "Social": "Pranks", "Signature": "Double jump, glides, swaps your boots", "Danger": "None"})],
    "flora": [
        (dict(id="venus_flytrap_flora", name="Venus Flytrap", group="Swamps", tag="Hungry · snappy", height=1.6, biomes=["swamp", "mangrove_swamp"],
              temperament="Hungry, quick",
              ecology="Dionaea muscipula, the carnivorous plant of the Carolina bogs, whose leaves snap shut when touched twice.",
              interaction="Meat. Keep a step back.",
              signature=("Snap trap", "Rooted like the alraune. Her trap snaps shut on any mob or stranger that steps into reach, holding and "
                                      "biting. Befriended, she only snaps at monsters: a garden guard."),
              danger="Medium (in reach).",
              variants=[("green", "Green (default)", "Green, red trap"), ("red", "Red", "All red"), ("purple", "Purple", "Purple"),
                        ("yellow", "Yellow", "Yellow-green"), ("dark", "Dark", "Dark green")]),
         {"Habitat": "Swamps", "Signature": "Snaps and holds, guards gardens", "Danger": "Medium"})],
    "merfolk": [
        (dict(id="siren_mermaid", name="Siren", group="Rocky coasts", tag="Enchanting · dangerous", height=1.7,
              biomes=["stony_shore", "ocean", "deep_ocean", "cold_ocean"], temperament="Enchanting",
              ecology="The singers of the rocks, here the mermaid kind of the sailors' tales, whose song draws ships onto the reef.",
              interaction="Fish.",
              signature=("Luring song", "Her song draws strangers toward the water. Sung for a friend, it gives Conduit Power while they swim."),
              danger="Medium (her song).",
              variants=[("teal", "Teal (default)", "Teal"), ("pearl", "Pearl", "White and silver"), ("coral", "Coral", "Coral pink"),
                        ("abyss", "Abyss", "Deep blue"), ("storm", "Storm", "Grey")]),
         {"Habitat": "Rocky coasts", "Young": "Roe", "On land": "Slow", "Signature": "Lures strangers, Conduit Power for friends",
          "Danger": "Medium"})],
}
for _fid, _entries in NINTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))


# tenth wave (2026-10-25): Harmontide's own creatures from the reference art (the coin magpie and the trickster raven
# are harpies: their own scripts)
TENTH = {
    "centaur": [
        (dict(id="tusktaur_centaur", name="Tusktaur", group="Snowy plains", tag="Huge · motherly", height=2.6,
              biomes=["snowy_plains", "snowy_taiga", "frozen_river", "ice_spikes"], temperament="Patient, protective",
              ecology="A Harmontide mammoth-taur: the woolly mammoth's body, shaggy and tusked, below a broad-shouldered girl.",
              interaction="Wheat and hay. Ride her through the deep snow.",
              signature=("Tusk sweep", "A sweep of her tusks knocks back everything in front of her. Friends ride her, and she never sinks in "
                                       "powder snow or freezes."),
              danger="High (if provoked).",
              variants=[("woolly", "Woolly (default)", "Red-brown wool"), ("black", "Black", "Black wool"), ("snow", "Snow", "White wool"),
                        ("steppe", "Steppe", "Pale gold"), ("ember", "Ember", "Dark, gilded tusks")]),
         {"Habitat": "Snowy plains", "Ridden": "By friends", "Signature": "Tusk sweep, never sinks in snow", "Temperament": "Protective",
          "Danger": "High"}),
        (dict(id="aurorantler_centaur", name="Aurorantler", group="Snowy taiga", tag="Luminous · aloof", height=2.2,
              biomes=["snowy_taiga", "grove", "snowy_slopes"], temperament="Aloof, gentle",
              ecology="A Harmontide deer-taur whose antlers catch the northern lights.",
              interaction="Sweet berries and glow berries.",
              signature=("Aurora antlers", "Her antlers glow at night and light the snow around her. Her friend sees in the dark beside her "
                                           "(Night Vision)."),
              danger="None.",
              variants=[("aurora", "Aurora (default)", "Green-blue glow"), ("violet", "Violet", "Violet glow"), ("dawn", "Dawn", "Pink glow"),
                        ("frost", "Frost", "White glow"), ("ember", "Ember", "Amber glow")]),
         {"Habitat": "Snowy taiga", "Ridden": "By friends", "Signature": "Glowing antlers, Night Vision", "Temperament": "Aloof",
          "Danger": "None"})],
    "minotaur": [
        (dict(id="rhiwool_minotaur", name="Rhiwool", group="Tundra", tag="Shaggy · headstrong", height=2.0,
              biomes=["snowy_plains", "frozen_peaks", "windswept_gravelly_hills"], temperament="Headstrong",
              ecology="A Harmontide woolly rhino: a great horn on her brow and wool to the ground.",
              interaction="Wheat. Shears.",
              signature=("Rhino charge", "She charges in a straight line and sends whatever she hits flying. Shears take her wool, which "
                                         "grows back once she grazes."),
              danger="High (if provoked).",
              variants=[("black", "Black (default)", "Black wool"), ("brown", "Brown", "Brown wool"), ("grey", "Grey", "Grey wool"),
                        ("white", "White", "White wool"), ("red", "Red", "Red wool")]),
         {"Habitat": "Tundra", "Temper": "Headstrong", "Signature": "Straight-line charge, shearable", "Danger": "High"})],
    "canid": [
        (dict(id="meloncollie_canid", name="Meloncollie", group="Farmland", tag="Cheerful · bossy", height=1.5,
              biomes=["plains", "sunflower_plains", "savanna"], temperament="Cheerful, bossy",
              ecology="A Harmontide herding dog with a watermelon rind for a coat.",
              interaction="Melon slices and bones.",
              signature=("Herding", "Befriended, she rounds up stray cows, sheep, pigs and chickens and herds them back to the nearest "
                                    "fenced pen. She plants melon seeds."),
              danger="None.",
              variants=[("melon", "Melon (default)", "Green rind, brown and white"), ("golden", "Golden", "A yellow melon"),
                        ("black", "Black", "A black-rind melon"), ("tricolour", "Tricolour", "Black, tan and white"), ("seedless", "Seedless", "Pale rind")]),
         {"Habitat": "Farmland", "Kin": "Wolves", "Signature": "Herds animals into pens", "Danger": "None"})],
    "rodent": [
        (dict(id="needlehog_rodent", name="Needlehog", group="Forests", tag="Prickly · studious", height=1.3,
              biomes=["forest", "birch_forest", "dark_forest", "meadow"], temperament="Shy, studious",
              ecology="A Harmontide hedgehog with quills long enough to pin notes to.",
              interaction="Spider eyes and berries.",
              signature=("Quills", "Frightened, she rolls into a ball of quills that pricks whatever touches her. She hunts silverfish and "
                                   "picks up paper and books she finds."),
              danger="Low (her quills).",
              variants=[("brown", "Brown (default)", "Brown quills"), ("blue", "Blue", "Blue-grey quills"), ("albino", "Albino", "White quills"),
                        ("cinnamon", "Cinnamon", "Ginger quills"), ("dark", "Dark", "Dark quills")]),
         {"Habitat": "Forests", "Signature": "Quill ball, hunts silverfish", "Danger": "Low"})],
    "jackalope": [
        (dict(id="clockhare_jackalope", name="Clockhare", group="Villages", tag="Punctual · fussy", height=1.45,
              biomes=["plains", "meadow", "flower_forest"], temperament="Punctual, fussy",
              ecology="A Harmontide hare in a waistcoat and top hat, never far from her pocket watch.",
              interaction="Carrots. Be on time.",
              signature=("Pocket watch", "Her watch chimes at dawn and at dusk, and warns her friend when phantoms are due. Her friend works "
                                         "faster beside her (Haste)."),
              danger="None.",
              variants=[("black", "Black (default)", "Black fur, brass watch"), ("white", "White", "White fur, silver watch"),
                        ("brown", "Brown", "Brown fur, copper watch"), ("grey", "Grey", "Grey fur, gold watch"), ("spotted", "Spotted", "Dutch markings")]),
         {"Habitat": "Plains", "Social": "Keeps time", "Signature": "Chimes, Haste", "Danger": "None"})],
    "merfolk": [
        (dict(id="axolume_mermaid", name="Axolume", group="Lush caves", tag="Glowing · gentle", height=1.4, biomes=["lush_caves"],
              temperament="Gentle",
              ecology="A Harmontide axolotl whose frills and spots glow in the dark water of the lush caves.",
              interaction="Tropical fish.",
              signature=("Glow", "She lights the water around her, and her friend heals beside her (Regeneration)."),
              danger="None.",
              variants=[("azure", "Azure (default)", "Blue glow"), ("rose", "Rose", "Pink glow"), ("gold", "Gold", "Gold glow"),
                        ("jade", "Jade", "Green glow"), ("void", "Void", "Black, violet glow")]),
         {"Habitat": "Lush caves", "Young": "Roe", "On land": "Slow", "Signature": "Glows, Regeneration", "Danger": "None"})],
    "armoured": [
        (dict(id="aegis_boar_armoured", name="Aegis Boar", group="Forests", tag="Stalwart · stubborn", height=1.9,
              biomes=["forest", "dark_forest", "old_growth_spruce_taiga"], temperament="Stalwart",
              ecology="A Harmontide boar in plate armour, tusked and broad, who stands in front of whoever she guards.",
              interaction="Carrots, potatoes and beetroot.",
              signature=("Shield", "Befriended, she takes a third of every hit meant for her friend within 8 blocks, and charges what struck "
                                   "them."),
              danger="Medium (if provoked).",
              variants=[("bronze", "Bronze (default)", "Bronze plate"), ("iron", "Iron", "Iron plate"), ("gold", "Gold", "Gold plate"),
                        ("blue", "Azure", "Blue-enamelled plate"), ("black", "Black", "Blackened plate")]),
         {"Habitat": "Forests", "Signature": "Takes hits for her friend", "Danger": "Medium"})],
    "nekomata": [
        (dict(id="blaze_cat_nekomata", name="Blaze Cat", group="The Nether", tag="Fiery · haughty", height=1.6,
              biomes=["nether_wastes", "crimson_forest", "basalt_deltas"], temperament="Haughty",
              ecology="A Harmontide black cat of the Nether with flames for paws.",
              interaction="Cooked fish and blaze powder.",
              signature=("Burning paws", "Fireproof. Her blows set things alight, and she lights campfires and candles near her. Blazes "
                                         "are her kin; befriended, her friend takes no harm from fire beside her."),
              danger="Medium.",
              variants=[("black", "Black (default)", "Black, orange flames"), ("soul", "Soul", "Black, blue flames"), ("ash", "Ash", "Grey"),
                        ("cinder", "Cinder", "Red-black"), ("white", "White", "White, gold flames")]),
         {"Habitat": "The Nether", "Size": "Medium", "Signature": "Fire paws, Fire Resistance", "Danger": "Medium"})],
    "construct": [
        (dict(id="gargoyle_construct", name="Gargoyle", group="Rooftops", tag="Still · watchful", height=1.8,
              biomes=["plains", "meadow", "windswept_hills"], temperament="Silent by day, watchful by night",
              ecology="The carved guardian of cathedral roofs: stone by day, awake at night.",
              interaction="Stone and cobblestone. Leave her a high perch.",
              signature=("Stone by day", "By day she turns to stone: nothing hurts her. At night she wakes, flies, and fights monsters near "
                                         "her friend."),
              danger="Medium (at night).",
              variants=[("granite", "Granite (default)", "Grey stone"), ("sandstone", "Sandstone", "Sandstone"), ("blackstone", "Blackstone", "Blackstone"),
                        ("moss", "Mossy", "Mossy stone"), ("marble", "Marble", "White stone")]),
         {"Habitat": "Rooftops", "Signature": "Stone by day, flies at night", "Danger": "Medium"})],
}
for _fid, _entries in TENTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))


# eleventh wave (2026-10-26): more Harmontide creatures (the rookmourn and the cassowrath are harpies, the mailtoad an
# anuran: their own scripts)
ELEVENTH = {
    "lizardfolk": [
        (dict(id="anvil_tortoise_lizardfolk", name="Anvil Tortoise", group="Mountains & villages", tag="Steady · handy", height=1.5,
              biomes=["stony_peaks", "windswept_hills", "plains"], temperament="Steady, patient",
              ecology="A Harmontide tortoise whose shell is a smith's anvil.",
              interaction="Iron nuggets and melons. Bring her what is broken.",
              signature=("Anvil shell", "Hand her a damaged tool or armour piece with one of its repair materials and she mends it, for no "
                                        "experience. Too heavy to push or knock back."),
              danger="None.",
              variants=[("iron", "Iron (default)", "An iron anvil"), ("copper", "Copper", "A copper anvil"), ("gold", "Gold", "A gold anvil"),
                        ("blackstone", "Blackstone", "A dark anvil"), ("chipped", "Chipped", "A worn, chipped anvil")]),
         {"Habitat": "Mountains, villages", "Signature": "Repairs tools for free", "Danger": "None"})],
    "marsupial": [
        (dict(id="gliderune_marsupial", name="Gliderune", group="Forests", tag="Tiny · clingy", height=1.2,
              biomes=["forest", "birch_forest", "jungle", "dark_forest"], temperament="Playful, clingy",
              ecology="A Harmontide sugar glider who sails from tree to tree on the skin between her arms and legs.",
              interaction="Sweet berries and honey.",
              signature=("Glider", "She glides down from any height. Befriended, she rides on your shoulder and you fall slowly while she "
                                   "is there (Slow Falling)."),
              danger="None.",
              variants=[("grey", "Grey (default)", "Grey, a dark stripe"), ("blue", "Blue", "Blue-grey"), ("leucistic", "Leucistic", "White"),
                        ("mosaic", "Mosaic", "White with grey patches"), ("cinnamon", "Cinnamon", "Ginger")]),
         {"Habitat": "Forests", "Signature": "Rides your shoulder, Slow Falling", "Danger": "None"})],
    "armoured": [
        (dict(id="slothren_armoured", name="Slothren", group="Jungles", tag="Slow · mossy", height=1.6,
              biomes=["jungle", "sparse_jungle", "bamboo_jungle", "lush_caves"], temperament="Slow, serene",
              ecology="A Harmontide sloth, a cousin of the armadillos, so slow that moss and flowers grow in her fur.",
              interaction="Leaves and glow berries. Do not hurry her.",
              signature=("Mossy", "She hangs from branches by day and barely moves. Moss and flowers spread around where she rests; "
                                  "befriended, crops and saplings near her grow as if bone-mealed."),
              danger="None.",
              variants=[("moss", "Mossy (default)", "Brown, green moss"), ("pale", "Pale", "Pale, white moss"), ("dark", "Dark", "Dark brown"),
                        ("blossom", "Blossom", "Flowers in her fur"), ("fungal", "Fungal", "Mushrooms in her fur")]),
         {"Habitat": "Jungles", "Signature": "Hangs from branches, spreads moss and growth", "Danger": "None"})],
    "rodent": [
        (dict(id="coatimano_rodent", name="Coatimano", group="Deserts & jungles", tag="Nosy · busy", height=1.35,
              biomes=["desert", "jungle", "badlands", "savanna"], temperament="Nosy, busy",
              ecology="A Harmontide coati, ring-tailed and long-nosed, who digs in everything.",
              interaction="Fruit and eggs.",
              signature=("Digger", "She sniffs out suspicious sand and gravel within 12 blocks and brushes it out; befriended, she brings "
                                   "you what she finds."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, ringed tail"), ("brown", "Brown", "Brown"), ("ring", "Ringed", "Grey, bold rings"),
                        ("red", "Red", "Rust red"), ("white", "White", "Pale")]),
         {"Habitat": "Deserts, jungles", "Signature": "Digs up archaeology", "Danger": "None"})],
    "nekomata": [
        (dict(id="waterlil_nekomata", name="Waterlil", group="Swamps", tag="Dainty · dry-footed", height=0.6,
              biomes=["swamp", "mangrove_swamp", "river"], temperament="Dainty",
              ecology="A Harmontide cat who will not get her paws wet: a big cat's head resting on her own lily pad, a small bloom "
                      "on top, her little body hidden in the water under it. Lily pads grow under her feet wherever she walks on water.",
              interaction="Fish.",
              signature=("Lily pads", "Lily pads grow under her as she walks on water, and fade again behind her; befriended, they grow "
                                      "under her friend too while they are near her."),
              danger="None.",
              variants=[("black", "Black (default)", "Black, a pink lotus"), ("white", "White", "White, a white lotus"),
                        ("calico", "Calico", "Calico, a yellow lotus"), ("grey", "Grey", "Grey, a blue lotus"), ("ginger", "Ginger", "Ginger, a red lotus")]),
         {"Habitat": "Swamps", "Social": "Alone", "Water": "Walks on lily pads", "Signature": "Lily pads for her friend", "Danger": "None"})],
    "construct": [
        (dict(id="talosling_construct", name="Talosling", group="Ruins", tag="Loyal · wound up", height=1.5,
              biomes=["desert", "badlands", "stony_peaks"], temperament="Loyal, clockwork",
              ecology="A Harmontide bronze automaton hound, a little Talos, that runs on a wound spring.",
              interaction="Redstone winds her up.",
              signature=("Clockwork", "Redstone dust winds her spring (five minutes a dust, up to twenty): while wound she guards her "
                                      "friend; run down, she sits still until wound again."),
              danger="Medium (wound up).",
              variants=[("bronze", "Bronze (default)", "Bronze and blue enamel"), ("copper", "Copper", "Copper"), ("gold", "Gold", "Gilded"),
                        ("iron", "Iron", "Iron"), ("verdigris", "Verdigris", "Green with age")]),
         {"Habitat": "Ruins", "Signature": "Runs on redstone, guards", "Danger": "Medium"})],
    "merfolk": [
        (dict(id="mantarrow_mermaid", name="Mantarrow", group="Warm ocean", tag="Graceful · fast", height=1.6,
              biomes=["warm_ocean", "lukewarm_ocean", "deep_lukewarm_ocean"], temperament="Graceful",
              ecology="A Harmontide manta ray, her fins spread wide like wings.",
              interaction="Fish.",
              signature=("Manta ride", "A friend can ride her across the water, faster than a boat; now and then she leaps clear of the waves."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, a white belly"), ("black", "Black", "Black"), ("spotted", "Spotted", "Black with white spots"),
                        ("teal", "Teal", "Teal"), ("violet", "Violet", "Violet")]),
         {"Habitat": "Warm ocean", "Young": "Live pups", "On land": "Slow", "Signature": "Carries a rider fast", "Danger": "None"})],
    "centaur": [
        (dict(id="sphinx_centaur", name="Sphinx", group="Desert temples", tag="Riddling · proud", height=2.2, biomes=["desert"],
              temperament="Proud, curious",
              ecology="The riddling guardian of Greek and Egyptian legend: a lioness's body, a woman's head and great wings.",
              interaction="Answer her riddle with the right item.",
              signature=("Riddles", "She asks each newcomer a riddle; answer by handing her the item it means. A right answer earns a "
                                    "reward, a wrong one a pounce."),
              danger="Medium (a wrong answer).",
              variants=[("sand", "Sand (default)", "Tawny, gold headdress"), ("night", "Night", "Black, silver"), ("red", "Red", "Red sandstone"),
                        ("white", "White", "White, lapis"), ("gilded", "Gilded", "Golden")]),
         {"Habitat": "Desert temples", "Ridden": "Never", "Signature": "Riddles for rewards", "Temperament": "Proud", "Danger": "Medium"})],
    "elf": [
        (dict(id="brownie_elf", name="Brownie", group="Villages", tag="Tiny · tidy", height=0.9, biomes=["plains", "meadow", "forest"],
              temperament="Shy, house-proud",
              ecology="The small house spirit of Scottish folklore who does the chores at night, for a bowl of milk or bread.",
              interaction="Leave her milk or bread. Never thank her too much.",
              signature=("Chores", "At night she tidies the chests near her friend's bed (merging stacks) and gathers loose items lying "
                                   "nearby into a chest."),
              danger="None.",
              variants=[("brown", "Brown (default)", "Brown rags"), ("green", "Green", "Green rags"), ("grey", "Grey", "Grey rags"),
                        ("red", "Red", "A red cap"), ("patched", "Patched", "Patchwork")]),
         {"Habitat": "Villages", "Trades": "Chores for milk", "Signature": "Tidies chests at night", "Danger": "None"})],
}
for _fid, _entries in ELEVENTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))


# --- the twelfth wave (2026-10-26): more of the Harmontide reference art ---------------------------------------------
TWELFTH = {
    "nekomata": [
        (dict(id="cheshire_nekomata", name="Cheshire", group="Flower forests", tag="Grinning · elusive", height=1.5,
              biomes=["flower_forest", "dark_forest", "birch_forest"], temperament="Mischievous, curious",
              ecology="The grinning cat of Wonderland, striped and smug, who fades away until only her grin is left.",
              interaction="Fish, and a straight look.",
              signature=("The grin", "Unless you look straight at her she fades away, all but her glowing grin; befriended, she blinks "
                                     "back to her friend's side whenever she is left behind."),
              danger="None.",
              variants=[("pink", "Pink (default)", "Pink and purple stripes"), ("purple", "Purple", "Purple and teal"), ("blue", "Blue", "Blue and white"),
                        ("gold", "Gold", "Gold and brown"), ("black", "Black", "Black and violet")]),
         {"Habitat": "Flower forests", "Social": "Alone", "Water": "Avoids it", "Signature": "Fades to a grin", "Danger": "None"})],
    "flora": [
        (dict(id="kesaran_pasaran_flora", name="Kesaran Pasaran", group="Meadows", tag="Fluffy · drifting", height=0.7,
              biomes=["meadow", "plains", "sunflower_plains", "flower_forest"], temperament="Gentle, carefree",
              ecology="The lucky puff of Japanese folklore, here a little dandelion seed of a girl hanging under her fluffy puff.",
              interaction="Sugar.",
              signature=("On the wind", "She never touches the ground: the wind carries her along, all of them drifting the same way; "
                                        "befriended, she drifts after her friend and brings them Luck. Fed sugar, she splits in two "
                                        "(up to three of her)."),
              danger="None.",
              variants=[("white", "White (default)", "A white puff"), ("yellow", "Yellow", "A dandelion-yellow puff"), ("blue", "Blue", "A blue puff"),
                        ("pink", "Pink", "A pink puff"), ("black", "Black", "A black puff")]),
         {"Habitat": "Meadows", "Signature": "Drifts on the wind, brings Luck", "Danger": "None"})],
    "lamia": [
        (dict(id="tsuchinoko_lamia", name="Tsuchinoko", group="Mountain forests", tag="Stubby · roly-poly", height=1.0,
              biomes=["forest", "cherry_grove", "windswept_forest"], temperament="Lazy, greedy",
              ecology="The fat little snake of Japanese legend that rolls down hills like a wheel and loves a drink.",
              interaction="Honey.",
              signature=("Rolling", "Downhill she curls up and rolls, faster than you can run. Her rare presents are golden."),
              danger="None.",
              variants=[("brown", "Brown (default)", "Brown, a pale belly"), ("grey", "Grey", "Grey"), ("green", "Green", "Moss green"),
                        ("gold", "Gold", "Golden"), ("black", "Black", "Black")]),
         {"Habitat": "Mountain forests", "Young": "Eggs", "Defence": "Rolls away", "Signature": "Rolls downhill", "Danger": "None"})],
    "merfolk": [
        (dict(id="platypus_mermaid", name="Platypus", group="Rivers", tag="Odd · clever", height=1.35, biomes=["river", "swamp"],
              temperament="Shy, clever",
              ecology="A Harmontide platypus: a duck-billed cap, a flat beaver tail, and a sixth sense for anything that moves.",
              interaction="Fish.",
              signature=("Electrosense", "Underwater she senses every monster within 16 blocks of her friend and makes them glow. "
                                         "Strangers who hit her are poisoned by her spur."),
              danger="Low (her spur).",
              variants=[("brown", "Brown (default)", "Brown"), ("dark", "Dark", "Dark brown"), ("golden", "Golden", "Golden"),
                        ("grey", "Grey", "Grey"), ("pale", "Pale", "Pale")]),
         {"Habitat": "Rivers", "Young": "Eggs", "On land": "Waddles", "Signature": "Senses hidden monsters", "Danger": "Low"}),
        (dict(id="moonseal_mermaid", name="Moonseal", group="Frozen seas", tag="Dreamy · moonlit", height=1.55,
              biomes=["frozen_ocean", "cold_ocean", "snowy_beach", "frozen_river"], temperament="Calm, dreamy",
              ecology="A Harmontide seal of the frozen seas, blue as the night, with crescent moons on her fur and little antlers.",
              interaction="Fish.",
              signature=("Moon song", "Her crescents glow at night; on a full moon she sings, and her friend near her regenerates. "
                                      "Befriended, she gives Dolphin's Grace to her friend swimming beside her."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Night blue, white crescents"), ("purple", "Purple", "Purple"), ("green", "Green", "Sea green"),
                        ("silver", "Silver", "Silver"), ("night", "Night", "Black, gold crescents")]),
         {"Habitat": "Frozen seas", "Young": "Pups", "On land": "Flops", "Signature": "Sings at the full moon", "Danger": "None"})],
    "rodent": [
        (dict(id="marmotaur_rodent", name="Marmotaur", group="Mountain meadows", tag="Watchful · stout", height=1.3,
              biomes=["meadow", "windswept_hills", "windswept_gravelly_hills", "grove"], temperament="Watchful, bold",
              ecology="A Harmontide marmot with golden ram's horns and an amber pendant, the lookout of the high meadows.",
              interaction="Grass and berries.",
              signature=("Lookout", "When monsters come within 16 blocks of her friend she whistles a warning and they glow. She "
                                    "headbutts hard."),
              danger="Low (her headbutt).",
              variants=[("tawny", "Tawny (default)", "Tawny, golden horns"), ("grey", "Grey", "Grey, bronze horns"), ("black", "Black", "Black, silver horns"),
                        ("red", "Red", "Red, golden horns"), ("white", "White", "White, golden horns")]),
         {"Habitat": "Mountain meadows", "Signature": "Whistles at monsters", "Danger": "Low"}),
        (dict(id="bambadger_rodent", name="Bambadger", group="Bamboo jungles", tag="Cheerful · musical", height=1.2,
              biomes=["bamboo_jungle", "jungle"], temperament="Cheerful",
              ecology="A Harmontide badger striped like green bamboo, who plays a bamboo flute on a cord round her neck.",
              interaction="Bamboo and sweet berries.",
              signature=("Flute", "Befriended, every half minute she plays her flute, and the bamboo and sugar cane around her grow."),
              danger="None.",
              variants=[("green", "Green (default)", "Bamboo green"), ("golden", "Golden", "Golden bamboo"), ("black", "Black", "Black bamboo"),
                        ("red", "Red", "Red bamboo"), ("pale", "Pale", "Pale")]),
         {"Habitat": "Bamboo jungles", "Signature": "Her flute grows bamboo", "Danger": "None"})],
    "jackalope": [
        (dict(id="boghare_jackalope", name="Boghare", group="Swamps", tag="Bouncy · soggy", height=1.35,
              biomes=["swamp", "mangrove_swamp"], temperament="Bouncy, friendly",
              ecology="A Harmontide swamp hare, mossy green with a lily flower on her head and long ears tipped in pink.",
              interaction="Carrots and lily pads.",
              signature=("Leaps", "She leaps huge distances; her friend near her gets Jump Boost. Frogs are her kin."),
              danger="None.",
              variants=[("green", "Green (default)", "Moss green, a red flower"), ("blue", "Blue", "Blue"), ("purple", "Purple", "Purple"),
                        ("red", "Red", "Red"), ("yellow", "Yellow", "Yellow")]),
         {"Habitat": "Swamps", "Social": "Alone", "Signature": "Jump Boost for her friend", "Danger": "None"})],
    "devil": [
        (dict(id="gremlin_devil", name="Gremlin", group="Near villages", tag="Mischievous · handy", height=1.0,
              biomes=["plains", "forest", "savanna"], temperament="Mischievous",
              ecology="A little green tinkerer with big ears, who meddles with machines.",
              interaction="Cookies.",
              signature=("Tinkering", "At night a wild one pranks what people built: she flips levers and opens doors. A cookie "
                                      "stops her. Befriended, she tinkers with furnaces near her friend so they cook twice as fast."),
              danger="None (only pranks).",
              variants=[("moss", "Moss (default)", "Green skin, blue hair"), ("blue", "Blue", "Blue"), ("yellow", "Yellow", "Yellow"),
                        ("grey", "Grey", "Grey"), ("purple", "Purple", "Purple")]),
         {"Habitat": "Near villages", "Signature": "Pranks machines, speeds furnaces", "Danger": "None"}),
        (dict(id="karakasa_obake_devil", name="Karakasa Obake", group="Cherry groves", tag="Hopping · loyal", height=1.3,
              biomes=["cherry_grove", "bamboo_jungle", "swamp"], temperament="Playful, loyal",
              ecology="The umbrella spirit of Japanese folklore, here a girl under an umbrella hat who hops on one leg.",
              interaction="Paper and string.",
              signature=("Umbrella", "Befriended, her umbrella catches the arrows and other projectiles aimed at her friend near her."),
              danger="None.",
              variants=[("blue", "Blue (default)", "A blue umbrella"), ("red", "Red", "A red umbrella"), ("purple", "Purple", "A purple umbrella"),
                        ("green", "Green", "A green umbrella"), ("white", "White", "A white umbrella")]),
         {"Habitat": "Cherry groves", "Signature": "Catches projectiles", "Danger": "None"})],
    "dragon": [
        (dict(id="alebrije_dragon", name="Alebrije", group="Badlands", tag="Bright · joyful", height=1.7,
              biomes=["savanna", "badlands", "wooded_badlands"], temperament="Joyful",
              ecology="A spirit of colour from Mexican folk art: a winged chimera painted in bright patterns, every one her own.",
              interaction="Fruit and dyes.",
              signature=("Colours", "She flies. Befriended, now and then she dyes a sheep near her in a bright colour, and her "
                                    "presents are dyes."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, pink and yellow"), ("green", "Green", "Green, orange and pink"),
                        ("pink", "Pink", "Pink, teal and yellow"), ("orange", "Orange", "Orange, purple and green"),
                        ("purple", "Purple", "Purple, lime and red")]),
         {"Habitat": "Badlands", "Moves by": "Flying", "Signature": "Dyes sheep", "Danger": "None"})],
}
for _fid, _entries in TWELFTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))


# --- the thirteenth wave (2026-10-26): more of the Harmontide reference art, and four Japanese spirits -----------------
THIRTEENTH = {
    "merfolk": [
        (dict(id="rust_otter_mermaid", name="Rust Otter", group="Rivers", tag="Handy · tinkering", height=1.4, biomes=["river", "swamp"],
              temperament="Busy, practical",
              ecology="A Harmontide river otter, dark and sleek, hung with copper charms she polishes endlessly.",
              interaction="Fish.",
              signature=("Copper", "Befriended, she scrapes the green off weathered copper blocks near her friend, one stage at a time. "
                                   "She dives up raw copper and nuggets as presents."),
              danger="None.",
              variants=[("black", "Black (default)", "Black, copper charms"), ("brown", "Brown", "Brown"), ("grey", "Grey", "Grey, verdigris charms"),
                        ("copper", "Copper", "Rust red"), ("pale", "Pale", "Pale")]),
         {"Habitat": "Rivers", "Young": "Pups", "On land": "Lopes", "Signature": "Keeps copper shiny", "Danger": "None"})],
    "construct": [
        (dict(id="tin_squirrel_construct", name="Tin Squirrel", group="Villages", tag="Thrifty · clockwork", height=1.2,
              biomes=["plains", "forest", "birch_forest"], temperament="Thrifty, fussy",
              ecology="A Harmontide clockwork squirrel of riveted tin, who hoards every scrap of metal she finds.",
              interaction="Iron and gold nuggets.",
              signature=("Scrap", "Hand her a worn-out iron or gold tool or armour piece and she breaks it down into nuggets. She "
                                  "picks up nuggets lying near her."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue tin"), ("silver", "Silver", "Bright tin"), ("copper", "Copper", "Copper"),
                        ("gold", "Gold", "Gilded"), ("verdigris", "Verdigris", "Green with age")]),
         {"Habitat": "Villages", "Signature": "Scraps worn gear into nuggets", "Danger": "None"})],
    "kitsune": [
        (dict(id="wattlefox_kitsune", name="Wattlefox", group="Savannas", tag="Bright · brave", height=1.45,
              biomes=["savanna", "savanna_plateau", "windswept_savanna"], temperament="Bright, brave",
              ecology="A Harmontide fox of the dry country, blue as dusk, wreathed in golden wattle that blooms after fire.",
              interaction="Sweet berries and chicken.",
              signature=("After the fire", "She cannot burn. Befriended, she puts out fires near her friend and plants acacia "
                                           "saplings on bare ground."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, golden wattle"), ("teal", "Teal", "Teal"), ("violet", "Violet", "Violet"),
                        ("golden", "Golden", "Gold"), ("grey", "Grey", "Grey")]),
         {"Habitat": "Savannas", "Tails": "One to three", "Signature": "Puts out fires", "Danger": "None"})],
    "bug": [
        (dict(id="scarabronze_bug", name="Scarabronze", group="Deserts", tag="Steadfast · shining", height=1.5, biomes=["desert", "badlands"],
              temperament="Steadfast",
              ecology="A Harmontide scarab cased in bronze and gold, who rolls the sun up the sky each morning.",
              interaction="Rotten flesh, which she rolls away.",
              signature=("The roller", "Hand her rotten flesh and she rolls it into bone meal. At sunrise her friend gets Haste for "
                                       "the morning. Her shell is armour."),
              danger="None.",
              variants=[("bronze", "Bronze (default)", "Bronze, lapis cloth"), ("blue", "Blue", "Lapis"), ("green", "Green", "Malachite"),
                        ("gold", "Gold", "Gold"), ("black", "Black", "Obsidian")]),
         {"Habitat": "Deserts", "Signature": "Rolls rotten flesh into bone meal", "Danger": "None"})],
    "bat": [
        (dict(id="date_bat", name="Datebat", group="Oases", tag="Sweet · nocturnal", height=1.4, biomes=["desert", "savanna", "jungle"],
              temperament="Sweet, sleepy",
              ecology="A Harmontide fruit bat of the oasis, with date-palm fronds in her hair and a cluster of dates by her ear.",
              interaction="Sweet berries, apples and melon.",
              signature=("Pollinator", "She roosts by day. At night she flies from flower to flower, and the crops near her friend "
                                       "grow."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, gold cloth"), ("sand", "Sand", "Sandy"), ("brown", "Brown", "Brown"),
                        ("black", "Black", "Black"), ("gold", "Gold", "Golden")]),
         {"Habitat": "Oases", "Signature": "Crops grow at night", "Danger": "None"})],
    "nekomata": [
        (dict(id="snowloper_nekomata", name="Snowloper", group="Snowy slopes", tag="Playful · fluffy", height=1.3,
              biomes=["snowy_slopes", "grove", "snowy_taiga", "frozen_peaks"], temperament="Playful",
              ecology="A Harmontide snow-leopard cub, white with blue snowflake rosettes, who bounds across deep snow.",
              interaction="Fish.",
              signature=("Snow strider", "She walks on powder snow and never freezes; her friend gets Speed while on snow."),
              danger="None.",
              variants=[("white", "White (default)", "White, blue rosettes"), ("grey", "Grey", "Grey"), ("cream", "Cream", "Cream"),
                        ("blue", "Blue", "Ice blue"), ("black", "Black", "Black")]),
         {"Habitat": "Snowy slopes", "Social": "Alone", "Water": "Avoids it", "Signature": "Speed on snow", "Danger": "None"}),
        (dict(id="jaglare_nekomata", name="Jaglare", group="Jungles", tag="Proud · ambusher", height=1.6, biomes=["jungle", "sparse_jungle"],
              temperament="Proud, patient",
              ecology="A Harmontide jaguar, blue-patterned, with a lion's mane: she lurks in the trees.",
              interaction="Fish and meat.",
              signature=("Ambush", "She lurks in the jungle trees and drops on what passes below. Her roar weakens monsters near her "
                                   "friend."),
              danger="Medium (her ambush).",
              variants=[("blue", "Blue (default)", "Blue rosettes"), ("gold", "Gold", "A golden jaguar"), ("black", "Black", "A black panther"),
                        ("white", "White", "White"), ("violet", "Violet", "Violet")]),
         {"Habitat": "Jungles", "Social": "Alone", "Water": "Swims", "Signature": "Drops from the trees", "Danger": "Medium"})],
    "rodent": [
        (dict(id="squirricerat_rodent", name="Squirricerat", group="Forests", tag="Bold · busy", height=1.35,
              biomes=["forest", "dark_forest", "old_growth_birch_forest"], temperament="Bold",
              ecology="A Harmontide squirrel with a triceratops' frill and three horns, and a great spiral tail.",
              interaction="Sweet berries and seeds.",
              signature=("Horns and acorns", "She charges horns-first at monsters. She buries saplings, which grow into trees."),
              danger="Low (her charge).",
              variants=[("blue", "Blue (default)", "Blue"), ("green", "Green", "Green"), ("red", "Red", "Red"), ("brown", "Brown", "Brown"),
                        ("grey", "Grey", "Grey")]),
         {"Habitat": "Forests", "Signature": "Plants trees, charges monsters", "Danger": "Low"}),
        (dict(id="kamaitachi_rodent", name="Kamaitachi", group="Mountains", tag="Swift · sharp", height=1.35,
              biomes=["windswept_hills", "windswept_forest", "meadow", "cherry_grove"], temperament="Swift, mischievous",
              ecology="The sickle weasel of Japanese folklore, who rides the whirlwind with a blade on each forearm.",
              interaction="Chicken and eggs.",
              signature=("Whirlwind", "She is fast. Befriended, her gusts cut the tall grass, leaves and cobwebs around her friend. "
                                      "Her slashes knock back."),
              danger="Low (her slashes).",
              variants=[("fawn", "Fawn (default)", "Fawn"), ("white", "White", "An ermine"), ("brown", "Brown", "Brown"), ("grey", "Grey", "Grey"),
                        ("black", "Black", "Black")]),
         {"Habitat": "Mountains", "Signature": "Cuts grass, leaves and cobwebs", "Danger": "Low"})],
    "centaur": [
        (dict(id="momoshika_centaur", name="Momoshika", group="Cherry groves", tag="Gentle · blessed", height=2.0,
              biomes=["cherry_grove", "flower_forest", "meadow"], temperament="Gentle",
              ecology="A Harmontide deer taur, white with blue spots, with peach blossoms caught in her antlers.",
              interaction="Apples and sweet berries.",
              signature=("Peach blossom", "By day her friend regenerates beside her; now and then her present is a golden apple."),
              danger="None.",
              variants=[("blue", "Blue (default)", "White, blue spots"), ("pink", "Pink", "Pink"), ("gold", "Gold", "Gold"), ("green", "Green", "Green"),
                        ("white", "White", "White")]),
         {"Habitat": "Cherry groves", "Ridden": "By friends", "Signature": "Regeneration by day", "Temperament": "Gentle", "Danger": "None"})],
    "devil": [
        (dict(id="chochin_obake_devil", name="Chochin-obake", group="Villages at night", tag="Spooky · loyal", height=1.6,
              biomes=["cherry_grove", "bamboo_jungle", "forest"], temperament="Spooky, loyal",
              ecology="The paper-lantern spirit of Japanese folklore, here a girl under a living lantern with one great eye and a lolling "
                      "tongue.",
              interaction="Paper and candles.",
              signature=("Lantern", "Her lantern glows. Befriended, at night she keeps beside her friend and lights the way where "
                                    "they go."),
              danger="None.",
              variants=[("red", "Red (default)", "A red-banded lantern"), ("white", "White", "A white lantern"), ("blue", "Blue", "A blue lantern"),
                        ("purple", "Purple", "A purple lantern"), ("gold", "Gold", "A golden lantern")]),
         {"Habitat": "Villages at night", "Signature": "A light that follows her friend", "Danger": "None"})],
}
for _fid, _entries in THIRTEENTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))


# --- the fourteenth wave (2026-10-27): more of the Harmontide reference art, and five spirits of folklore ---------------
FOURTEENTH = {
    "armoured": [
        (dict(id="gribboar_armoured", name="Gribboar", group="Forests", tag="Snuffling · stubborn", height=1.4,
              biomes=["forest", "dark_forest", "old_growth_spruce_taiga"], temperament="Stubborn, greedy",
              ecology="A Harmontide boar with a shaggy blue mane, who roots through the forest floor for treasure.",
              interaction="Carrots, potatoes and mushrooms.",
              signature=("Truffle hunter", "Every half minute she roots in the dirt she stands on and digs something up: mushrooms, "
                                           "potatoes, carrots, now and then a gold nugget."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue mane"), ("brown", "Brown", "A wild boar"), ("black", "Black", "Black"),
                        ("red", "Red", "Red"), ("grey", "Grey", "Grey")]),
         {"Habitat": "Forests", "Signature": "Digs up truffle finds", "Danger": "None"})],
    "rodent": [
        (dict(id="silkcoon_rodent", name="Silkcoon", group="Rivers", tag="Fussy · clean", height=1.3, biomes=["forest", "swamp", "river"],
              temperament="Fussy, curious",
              ecology="A Harmontide raccoon of silky fur swirled in blue, who washes everything she touches.",
              interaction="Fish, eggs and berries.",
              signature=("Washer", "By water, hand her dyed wool, carpet, a bed, terracotta, glass or leather armour and she washes the "
                                   "dye out."),
              danger="None.",
              variants=[("blue", "Blue (default)", "White, blue swirls"), ("grey", "Grey", "A classic raccoon"), ("brown", "Brown", "Brown"),
                        ("white", "White", "White"), ("black", "Black", "Black")]),
         {"Habitat": "Rivers", "Signature": "Washes the dye out", "Danger": "None"})],
    "minotaur": [
        (dict(id="bronzejaw_minotaur", name="Bronzejaw Ox", group="Farmland", tag="Steady · strong", height=1.9, biomes=["plains", "savanna"],
              temperament="Steady, patient",
              ecology="A Harmontide ox in brass, with a bronze guard over her jaw and a yoke on her shoulders.",
              interaction="Wheat and hay.",
              signature=("Plough", "Led on a lead, she ploughs as she walks: the grass and dirt she passes over turns to farmland."),
              danger="Low (her horns).",
              variants=[("blue", "Blue (default)", "Blue, brass"), ("brown", "Brown", "Brown"), ("black", "Black", "Black"), ("grey", "Grey", "Grey"),
                        ("red", "Red", "Red")]),
         {"Habitat": "Farmland", "Bulls": "One in three", "Signature": "Ploughs as she walks", "Danger": "Low"})],
    "lizardfolk": [
        (dict(id="cocoskink_lizardfolk", name="Cocoskink", group="Jungles", tag="Jingling · sunny", height=1.45, biomes=["jungle", "sparse_jungle", "beach"],
              temperament="Sunny, chatty",
              ecology="A Harmontide skink with a crest of leaves and little bells on her tail, who loves the cocoa trees.",
              interaction="Melon and cocoa beans.",
              signature=("Cocoa", "Befriended, she grows cocoa pods on the jungle logs near her friend."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, leafy crest"), ("green", "Green", "Green"), ("gold", "Gold", "Gold"),
                        ("orange", "Orange", "Orange"), ("black", "Black", "Black")]),
         {"Habitat": "Jungles", "Signature": "Grows cocoa pods", "Danger": "None"})],
    "centaur": [
        (dict(id="prairhorn_centaur", name="Prairhorn", group="Plains", tag="Swift · skittish", height=2.0, biomes=["plains", "sunflower_plains", "savanna"],
              temperament="Swift, skittish",
              ecology="A Harmontide pronghorn taur, banded tan and white, the fastest runner of the plains.",
              interaction="Wheat and apples.",
              signature=("Swift", "The fastest ride there is: friends ride her faster than any horse can gallop."),
              danger="None.",
              variants=[("tan", "Tan (default)", "Tan and white bands"), ("red", "Red", "Red"), ("dark", "Dark", "Dark"), ("pale", "Pale", "Pale"),
                        ("blue", "Blue", "Blue")]),
         {"Habitat": "Plains", "Ridden": "By friends", "Signature": "The fastest ride", "Temperament": "Skittish", "Danger": "None"}),
        (dict(id="halberelk_centaur", name="Halberelk", group="Taiga", tag="Sturdy · handy", height=2.0,
              biomes=["taiga", "old_growth_pine_taiga", "snowy_taiga"], temperament="Sturdy, helpful",
              ecology="A Harmontide elk taur whose antlers end in blades like a halberd's.",
              interaction="Apples and sweet berries.",
              signature=("Woodcutter", "Befriended, when her friend chops a log of a tree within 8 blocks of her, she fells the whole "
                                       "tree (crouch to cut one log)."),
              danger="Low (her antlers).",
              variants=[("blue", "Blue (default)", "Blue, steel blades"), ("brown", "Brown", "Brown"), ("grey", "Grey", "Grey"),
                        ("white", "White", "White"), ("black", "Black", "Black")]),
         {"Habitat": "Taiga", "Ridden": "By friends", "Signature": "Fells whole trees", "Temperament": "Helpful", "Danger": "Low"})],
    "kitsune": [
        (dict(id="vulphea_kitsune", name="Vulphea", group="Badlands", tag="Fiery · undying", height=1.4, biomes=["badlands", "savanna_plateau", "desert"],
              temperament="Fiery, proud",
              ecology="A Harmontide fire fox, all flame-coloured fur, who rises again from her own ashes.",
              interaction="Chicken and blaze powder.",
              signature=("Rebirth", "She cannot burn and her bite sets things alight. Once a day, a killing blow leaves only ashes, and a "
                                    "minute later she rises from them."),
              danger="Low (her bite burns).",
              variants=[("red", "Red (default)", "Red flame"), ("gold", "Gold", "Golden flame"), ("blue", "Blue", "Blue flame"),
                        ("violet", "Violet", "Violet flame"), ("white", "White", "White flame")]),
         {"Habitat": "Badlands", "Tails": "One to three", "Signature": "Rises from her ashes", "Danger": "Low"})],
    "nekomata": [
        (dict(id="cait_sith_nekomata", name="Cait Sìth", group="Highlands", tag="Aloof · uncanny", height=1.45,
              biomes=["windswept_hills", "taiga", "dark_forest"], temperament="Aloof, uncanny",
              ecology="The fairy cat of the Scottish Highlands, black as night with a white star on her chest.",
              interaction="Fish, and a saucer of milk.",
              signature=("Fairy cat", "Phantoms keep away from her friend. A saucer of milk (a milk bucket) brings her friend Luck "
                                      "for five minutes."),
              danger="None.",
              variants=[("black", "Black (default)", "Black, a white star"), ("smoke", "Smoke", "Smoky grey"), ("grey", "Grey", "Grey"),
                        ("tabby", "Tabby", "Dark tabby"), ("midnight", "Midnight", "Blue-black")]),
         {"Habitat": "Highlands", "Social": "Alone", "Water": "Avoids it", "Signature": "Keeps phantoms away", "Danger": "None"})],
    "canid": [
        (dict(id="cu_sith_canid", name="Cù-sìth", group="Highlands", tag="Silent · fearsome", height=1.7, biomes=["windswept_hills", "meadow", "taiga"],
              temperament="Silent, watchful",
              ecology="The great green fairy hound of the Scottish Highlands, who barks three times.",
              interaction="Meat.",
              signature=("Three barks", "When monsters come near her friend she barks; at her third bark they flee."),
              danger="Low.",
              variants=[("green", "Green (default)", "Dark green"), ("moss", "Moss", "Moss green"), ("dark", "Dark", "Near black"),
                        ("pale", "Pale", "Pale green"), ("teal", "Teal", "Teal")]),
         {"Habitat": "Highlands", "Social": "Alone", "Signature": "Her third bark scatters monsters", "Danger": "Low"}),
        (dict(id="yeti_canid", name="Yeti", group="Peaks", tag="Shy · huge", height=2.1, biomes=["snowy_slopes", "frozen_peaks", "jagged_peaks", "snowy_taiga"],
              temperament="Shy, gentle",
              ecology="A huge shaggy snow woman of the high peaks, gentle with friends and a fine shot with a snowball.",
              interaction="Sweet berries and fish.",
              signature=("Snowballs", "She throws snowballs at the monsters near her friend, and now and then builds a snow golem. "
                                      "She walks on powder snow and never freezes."),
              danger="None.",
              variants=[("white", "White (default)", "White fur"), ("grey", "Grey", "Grey"), ("blue", "Blue", "Ice blue"), ("golden", "Golden", "Golden"),
                        ("brown", "Brown", "Brown")]),
         {"Habitat": "Peaks", "Social": "Alone", "Signature": "Snowballs and snow golems", "Danger": "None"})],
    "slime": [
        (dict(id="undine_slime", name="Undine", group="Rivers", tag="Flowing · nurturing", height=1.6, biomes=["river", "swamp", "mangrove_swamp"],
              temperament="Calm, nurturing",
              ecology="A water elemental of European legend, a girl of living water.",
              interaction="Water bottles and kelp.",
              signature=("Living water", "Befriended, she keeps the farmland near her watered, fills the cauldrons near her, and gives "
                                         "her friend Water Breathing in the water."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Clear blue"), ("teal", "Teal", "Teal"), ("clear", "Clear", "Nearly clear"), ("deep", "Deep", "Deep blue"),
                        ("sea", "Sea", "Sea green")]),
         {"Habitat": "Rivers", "Signature": "Waters farms, fills cauldrons", "Danger": "None"})],
    "devil": [
        (dict(id="ittan_momen_devil", name="Ittan-momen", group="Night skies", tag="Fluttering · free", height=1.6,
              biomes=["cherry_grove", "plains", "forest"], temperament="Playful, free",
              ecology="The flying strip of cotton of Japanese folklore, here a girl trailing a long cotton ribbon in place of legs.",
              interaction="String and wool.",
              signature=("Flying cloth", "She flies. Befriended, her friend climbs on with an empty hand and rides her through the air, "
                                         "steering where they look."),
              danger="None.",
              variants=[("white", "White (default)", "White cotton"), ("indigo", "Indigo", "Indigo-dyed"), ("red", "Red", "Red"), ("gold", "Gold", "Gold"),
                        ("black", "Black", "Black")]),
         {"Habitat": "Night skies", "Signature": "A flying ride", "Danger": "None"})],
}
for _fid, _entries in FOURTEENTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# --- the fifteenth wave (2026-10-27): more of the Harmontide reference art, and four spirits of folklore -----------------
FIFTEENTH = {
    "lizardfolk": [
        (dict(id="crocora_lizardfolk", name="Crocora", group="Rivers", tag="Snappy · loyal", height=1.3, biomes=["river", "swamp", "mangrove_swamp"],
              temperament="Snappy, loyal",
              ecology="A Harmontide crocodile in gold-and-blue river finery: small, round and fiercely protective.",
              interaction="Fish and chicken.",
              signature=("Lifeguard", "Befriended, if her friend runs out of air near her, she hauls them up to the surface and their air "
                                      "fills back up."),
              danger="Low (her bite).",
              variants=[("blue", "Blue (default)", "Blue, a cream belly"), ("green", "Green", "Green"), ("olive", "Olive", "Olive, like the Nile"),
                        ("gold", "Gold", "Gold"), ("black", "Black", "Black")]),
         {"Habitat": "Rivers", "Signature": "Saves her friend from drowning", "Danger": "Low"}),
        (dict(id="iron_kobold_lizardfolk", name="Iron Kobold", group="Mountains", tag="Gruff · handy", height=1.1,
              biomes=["windswept_hills", "windswept_gravelly_hills", "stony_peaks"], temperament="Gruff, hard-working",
              ecology="A Harmontide kobold smith in plated armour, who carries a hammer as big as her head.",
              interaction="Cooked beef and mutton.",
              signature=("Smith", "Hand her a damaged iron or chainmail tool or piece of armour and she mends a quarter of it, for one "
                                  "iron ingot from your pack."),
              danger="Low (her hammer).",
              variants=[("blue", "Blue (default)", "Blue scales, iron plate"), ("copper", "Copper", "Copper plate"), ("red", "Red", "Red"),
                        ("green", "Green", "Green"), ("black", "Black", "Black")]),
         {"Habitat": "Mountains", "Signature": "Mends iron gear", "Danger": "Low"})],
    "minotaur": [
        (dict(id="mooskuh_minotaur", name="Mooskuh", group="Lush caves", tag="Soft · sleepy", height=1.4, biomes=["lush_caves", "swamp", "dark_forest"],
              temperament="Sleepy, gentle",
              ecology="A Harmontide calf grown all over with soft green moss.",
              interaction="Wheat and glow berries.",
              signature=("Moss", "The stone she rests on slowly grows into moss. You can milk her with a bucket."),
              danger="None.",
              variants=[("green", "Green (default)", "Moss green"), ("lime", "Lime", "Bright moss"), ("russet", "Russet", "Russet moss"),
                        ("lichen", "Lichen", "Grey-green lichen"), ("blue", "Blue", "Blue moss")]),
         {"Habitat": "Lush caves", "Temper": "Gentle", "Signature": "Grows moss", "Danger": "None"})],
    "centaur": [
        (dict(id="bellmare_centaur", name="Bellmare", group="Meadows", tag="Jingling · bossy", height=1.9, biomes=["meadow", "plains", "sunflower_plains"],
              temperament="Bossy, cheerful",
              ecology="A Harmontide donkey with little goat's horns, hung all over with bells, who leads the herd.",
              interaction="Wheat and carrots.",
              signature=("Bellwether", "Befriended, the cows, sheep, goats and pigs within 12 blocks follow her while she follows her friend."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, silver bells"), ("grey", "Grey", "A grey donkey"), ("brown", "Brown", "Brown"),
                        ("white", "White", "White"), ("black", "Black", "Black")]),
         {"Habitat": "Meadows", "Ridden": "By friends", "Signature": "Leads the herd", "Temperament": "Bossy", "Danger": "None"}),
        (dict(id="vicunero_centaur", name="Vicunero", group="Highlands", tag="Proud · prickly", height=2.0,
              biomes=["windswept_hills", "meadow", "savanna_plateau"], temperament="Proud, prickly",
              ecology="A Harmontide vicuña in a woven saddle blanket, with the finest fleece in the hills.",
              interaction="Wheat and hay bales.",
              signature=("Spitter", "She spits at the monsters near her friend, like a llama. Friends can ride her."),
              danger="Low (she spits).",
              variants=[("blue", "Blue (default)", "Blue, a white chest"), ("tan", "Tan", "A wild vicuña"), ("cream", "Cream", "Cream"),
                        ("brown", "Brown", "Brown"), ("grey", "Grey", "Grey")]),
         {"Habitat": "Highlands", "Ridden": "By friends", "Signature": "Spits at monsters", "Temperament": "Prickly", "Danger": "Low"})],
    "rodent": [
        (dict(id="rockhyrax_rodent", name="Rockhyrax", group="Stony hills", tag="Watchful · sunny", height=1.2,
              biomes=["stony_shore", "windswept_hills", "savanna_plateau"], temperament="Watchful, sociable",
              ecology="A Harmontide rock hyrax with a back of blue scales, who loves nothing more than a sun-warmed stone.",
              interaction="Apples and sweet berries.",
              signature=("Sentinel", "Befriended, she whistles when a creeper comes within 12 blocks of her friend, and the creeper glows."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue scales"), ("brown", "Brown", "Brown"), ("grey", "Grey", "Grey"), ("sand", "Sand", "Sand"),
                        ("dark", "Dark", "Dark")]),
         {"Habitat": "Stony hills", "Signature": "Warns of creepers", "Danger": "None"})],
    "armoured": [
        (dict(id="riverdillo_armoured", name="Riverdillo", group="Rivers", tag="Busy · cheerful", height=1.3, biomes=["river", "swamp", "mangrove_swamp"],
              temperament="Busy, cheerful",
              ecology="A Harmontide armadillo of the rivers, with a long snout and a fish's tail, banded in blue and gold.",
              interaction="Fish and spider eyes.",
              signature=("Angler", "In the water near her friend she catches fish and brings them: cod and salmon, now and then a "
                                   "tropical fish."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, gold bands"), ("green", "Green", "Green"), ("brown", "Brown", "Brown"),
                        ("teal", "Teal", "Teal"), ("grey", "Grey", "Grey")]),
         {"Habitat": "Rivers", "Signature": "Catches fish", "Danger": "None"})],
    "merfolk": [
        (dict(id="reeffin_mermaid", name="Reeffin", group="Warm seas", tag="Gentle · bright", height=1.65, biomes=["warm_ocean", "lukewarm_ocean", "beach"],
              temperament="Gentle, house-proud",
              ecology="A Harmontide reef mermaid crowned with coral, who tends the reef like a garden.",
              interaction="Tropical fish and kelp.",
              signature=("Reef keeper", "Underwater she grows coral, coral fans and sea pickles on the sand around her."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, a coral crown"), ("pink", "Pink", "Pink"), ("orange", "Orange", "Orange"),
                        ("purple", "Purple", "Purple"), ("green", "Green", "Green")]),
         {"Habitat": "Warm seas", "Young": "Live young", "On land": "Slow", "Signature": "Grows coral", "Danger": "None"})],
    "lamia": [
        (dict(id="caraviper_lamia", name="Caraviper", group="Deserts", tag="Haughty · fierce", height=1.6, biomes=["desert", "badlands"],
              temperament="Haughty, protective",
              ecology="A Harmontide cobra with a wide hood, patterned with glyphs in gold.",
              interaction="Eggs and rabbit.",
              signature=("Spitting cobra", "Befriended, she spits venom at the monsters near her friend: they are blinded and poisoned."),
              danger="Low (her venom).",
              variants=[("blue", "Blue (default)", "Blue, gold glyphs"), ("sand", "Sand", "Sand"), ("black", "Black", "Black"), ("red", "Red", "Red"),
                        ("green", "Green", "Green")]),
         {"Habitat": "Deserts", "Young": "Eggs", "Defence": "Spits venom", "Signature": "Blinds monsters", "Danger": "Low"})],
    "nekomata": [
        (dict(id="bakeneko_nekomata", name="Bakeneko", group="Villages at night", tag="Mischievous · spooky", height=1.45,
              biomes=["plains", "forest", "cherry_grove"], temperament="Mischievous, playful",
              ecology="The shapeshifting ghost cat of Japanese folklore, with will-o'-wisps dancing round her.",
              interaction="Fish.",
              signature=("Ghost lights", "At night her wisps light up the ground around her friend."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, pale wisps"), ("calico", "Calico", "Calico"), ("black", "Black", "Black"),
                        ("white", "White", "White"), ("ginger", "Ginger", "Ginger")]),
         {"Habitat": "Villages at night", "Social": "Alone", "Water": "Avoids it", "Signature": "Lights the night", "Danger": "None"})],
    "devil": [
        (dict(id="rokurokubi_devil", name="Rokurokubi", group="Villages at night", tag="Shy · nosy", height=1.6, biomes=["cherry_grove", "plains", "forest"],
              temperament="Shy, nosy",
              ecology="The yōkai of Japanese folklore who seems an ordinary girl by day, but at night her neck stretches out long.",
              interaction="Sweet berries and honey.",
              signature=("Night watch", "After dark her neck rises high, and the monsters within 24 blocks of her friend glow."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue kimono"), ("red", "Red", "Red"), ("green", "Green", "Green"), ("white", "White", "White"),
                        ("black", "Black", "Black")]),
         {"Habitat": "Villages at night", "Signature": "Spots monsters at night", "Danger": "None"})],
    "kitsune": [
        (dict(id="huli_jing_kitsune", name="Huli Jing", group="Bamboo forests", tag="Charming · clever", height=1.55,
              biomes=["bamboo_jungle", "cherry_grove", "forest"], temperament="Clever, charming",
              ecology="The fox spirit of Chinese legend, who has lived a thousand years and has nine tails to show for it.",
              interaction="Chicken and sweet berries.",
              signature=("Charm", "In a village with her friend the villagers trade cheaper: her friend has Hero of the Village while "
                                  "near her."),
              danger="None.",
              variants=[("silver", "Silver (default)", "Silver-blue, black hair"), ("red", "Red", "Red"), ("white", "White", "White"),
                        ("gold", "Gold", "Gold"), ("black", "Black", "Black")]),
         {"Habitat": "Bamboo forests", "Size": "Nine tails", "Signature": "Cheaper trades", "Temperament": "Clever", "Danger": "None"})],
}
for _fid, _entries in FIFTEENTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# --- the sixteenth wave (2026-10-27): more of the Harmontide reference art, and four creatures of folklore ---------------
SIXTEENTH = {
    "arachne": [
        (dict(id="jorogumo_arachne", name="Jorogumo", group="Bamboo forests at night", tag="Sweet · patient", height=1.6,
              biomes=["bamboo_jungle", "cherry_grove", "dark_forest"], temperament="Sweet, patient",
              ecology="The “binding bride” of Japanese folklore: a spider who walks as a girl in a web-patterned kimono, with four "
                      "red eyes and eight long legs spread behind her back.",
              interaction="Chicken, rabbit and string.",
              signature=("Binding web", "At night, a monster that comes within 8 blocks of her friend is caught in a cobweb at its feet. "
                                        "The web melts away after 10 seconds."),
              danger="Low (her bite).",
              variants=[("violet", "Violet (default)", "Violet kimono, red obi"), ("crimson", "Crimson", "Crimson"), ("indigo", "Indigo", "Night blue"),
                        ("white", "White", "White, a black obi"), ("black", "Black", "Black, gold legs")]),
         {"Habitat": "Bamboo forests", "Active": "Night", "Silk use": "Snares monsters", "Signature": "Binds monsters at night", "Danger": "Low"})],
    "kitsune": [
        (dict(id="dunefennec_kitsune", name="Dunefennec", group="Deserts", tag="Bright · restless", height=1.3, biomes=["desert", "badlands"],
              temperament="Restless, cheerful",
              ecology="A night-blue Harmontide fennec with ears bigger than her head, gold crescents on her fur, a purple saddle-cloth and a bell.",
              interaction="Rabbit and sweet berries.",
              signature=("Desert guide", "Her friend near her runs fast on sand (Speed II), and husks near her lose interest in her friend."),
              danger="None.",
              variants=[("night", "Night (default)", "Night blue, gold crescents"), ("sand", "Sand", "Pale sand"), ("red", "Red sand", "Red, like the badlands"),
                        ("cream", "Cream", "Cream"), ("dusk", "Dusk", "Dusky violet")]),
         {"Habitat": "Deserts", "Size": "One tail", "Signature": "Fast on sand", "Temperament": "Restless", "Danger": "None"})],
    "crustacean": [
        (dict(id="floodcrab_crustacean", name="Floodcrab", group="Riverbanks", tag="Busy · muddy", height=1.3, biomes=["river", "swamp", "mangrove_swamp"],
              temperament="Busy, cheerful",
              ecology="A Harmontide river crab under a ridged stone fan of a shell, grown with moss and barnacles and hung with brass bells.",
              interaction="Cod, salmon and kelp.",
              signature=("Puddler", "Befriended, every 20 seconds she turns a block of dirt beside the water near her into mud."),
              danger="Low (her pinch).",
              variants=[("blue", "Blue (default)", "Blue-grey shell"), ("brown", "Brown", "Brown"), ("green", "Green", "Olive"),
                        ("red", "Red", "Red"), ("black", "Black", "Black")]),
         {"Habitat": "Riverbanks", "Signature": "Makes mud", "Danger": "Low"})],
    "bug": [
        (dict(id="temple_mantis_bug", name="Temple Mantis", group="Jungle temples", tag="Solemn · watchful", height=1.6,
              biomes=["jungle", "sparse_jungle", "desert"], temperament="Solemn, watchful",
              ecology="A Harmontide mantis who keeps the old temples, lapis blue worked with gold, a jewelled crown and a gold ring arch behind her head.",
              interaction="Spider eyes and rotten flesh.",
              signature=("Trapbreaker", "Befriended, she cuts the tripwires of old temples and other structures within 8 blocks of her "
                                     "friend before anyone can trip them. Wires a player strung up are left alone."),
              danger="Low (her scythes).",
              variants=[("lapis", "Lapis (default)", "Lapis blue and gold"), ("jade", "Jade", "Jade and gold"), ("orchid", "Orchid", "Orchid pink"),
                        ("sand", "Sand", "Sand, for the pyramids"), ("ruby", "Ruby", "Ruby and gold")]),
         {"Habitat": "Jungle temples", "Signature": "Cuts temple tripwires", "Danger": "Low"})],
    "merfolk": [
        (dict(id="sword_koi_mermaid", name="Sword Koi", group="Warm ocean", tag="Bold · proud", height=1.6, biomes=["warm_ocean", "lukewarm_ocean", "deep_lukewarm_ocean"],
              temperament="Proud, bold",
              ecology="A lapis-blue Harmontide koi patched with white, in gold-trimmed armour with a katana across her back, who duels the drowned for the sea's treasures.",
              interaction="Cod, salmon and kelp.",
              signature=("Duellist", "Befriended, she hunts the drowned near her friend, and a drowned she defeats always drops what it held "
                                     "(a trident, a nautilus shell, a fishing rod)."),
              danger="Low, unless you are drowned.",
              variants=[("lapis", "Lapis (default)", "Blue and white"), ("kohaku", "Kohaku", "White and red"), ("gold", "Gold", "Gold"),
                        ("black", "Black", "Black and red"), ("silver", "Silver", "Silver")]),
         {"Habitat": "Warm ocean", "Young": "Eggs", "On land": "Flops", "Signature": "Drowned drop their gear", "Danger": "Low"})],
    "mollusk": [
        (dict(id="squidkin_mollusk", name="Squidkin", group="Ocean shores", tag="Shy · inky", height=1.4, biomes=["ocean", "beach", "deep_ocean"],
              temperament="Shy, protective",
              ecology="A sky-blue Harmontide squid girl, her long hair curling into tentacles and a skirt of eight more, who hides behind clouds of ink.",
              interaction="Cod and kelp.",
              signature=("Ink cloud", "Befriended, when her friend is hurt near her, she squirts ink: the monsters around go blind and "
                                      "her friend turns invisible for a moment."),
              danger="None.",
              variants=[("sky", "Sky (default)", "Sky blue"), ("violet", "Violet", "Violet"), ("red", "Red", "Red"), ("glow", "Glow", "Glow-squid teal"),
                        ("white", "White", "White")]),
         {"Habitat": "Ocean shores", "Body": "Two legs, tentacle skirt", "Signature": "Ink cloud", "Danger": "None"})],
    "minotaur": [
        (dict(id="reedbull_minotaur", name="Reedbull", group="River marshes", tag="Calm · steady", height=1.9, biomes=["river", "swamp", "mangrove_swamp"],
              temperament="Calm, steady",
              ecology="A shaggy lapis-blue Harmontide marsh bull: gold-banded horns hung with tassels, a white rune on her brow, a woven blanket over her back.",
              interaction="Wheat and sugar cane.",
              signature=("Reed planter", "Befriended, every half minute she plants sugar cane on bare sand or dirt beside the water near her friend."),
              danger="Low.",
              variants=[("lapis", "Lapis (default)", "Lapis blue, a blue blanket"), ("marsh", "Marsh", "Mud brown, a green blanket"), ("grey", "Grey", "Grey, a red blanket"),
                        ("black", "Black", "Black, gold runes"), ("autumn", "Autumn", "Russet, a rust blanket")]),
         {"Habitat": "River marshes", "Temper": "Calm", "Signature": "Plants sugar cane", "Danger": "Low"})],
    "devil": [
        (dict(id="kikimora_devil", name="Kikimora", group="Villages at night", tag="Fussy · house-proud", height=1.4, biomes=["taiga", "old_growth_spruce_taiga", "plains"],
              temperament="Fussy, house-proud",
              ecology="The house spirit of Slavic folklore, a little woman in a headscarf who spins by night and scolds the careless.",
              interaction="Bread and milk.",
              signature=("Night latch", "At night, befriended, she shuts the wooden doors, trapdoors and fence gates left open near her friend."),
              danger="None.",
              variants=[("red", "Red (default)", "Red sarafan"), ("blue", "Blue", "Blue"), ("green", "Green", "Green"),
                        ("black", "Black", "Black"), ("white", "White", "White")]),
         {"Habitat": "Villages", "Signature": "Shuts doors at night", "Danger": "None"})],
    "dragon": [
        (dict(id="cockatrice_dragon", name="Cockatrice", group="Badlands", tag="Haughty · fierce", height=1.6, biomes=["badlands", "wooded_badlands", "savanna"],
              temperament="Haughty, fierce",
              ecology="The rooster-dragon of European legend, with a red comb, feathered wings and a scaly tail, whose glare stops "
                      "creatures in their tracks.",
              interaction="Seeds and chicken.",
              signature=("Stony glare", "Befriended, every 3 seconds she glares at a monster near her friend and it is slowed almost to a stop."),
              danger="Moderate (her glare and claws).",
              variants=[("red", "Red (default)", "Red comb, bronze feathers"), ("white", "White", "White"), ("black", "Black", "Black"),
                        ("gold", "Gold", "Gold"), ("green", "Green", "Green")]),
         {"Habitat": "Badlands", "Moves by": "Wings and legs", "Signature": "Slowing glare", "Danger": "Moderate"})],
    "elf": [
        (dict(id="valkyrie_elf", name="Valkyrie", group="Mountain peaks", tag="Stern · faithful", height=1.8,
              biomes=["jagged_peaks", "frozen_peaks", "snowy_slopes"], temperament="Stern, faithful",
              ecology="The chooser of the slain of Norse myth, armoured and winged, who carries the fallen home.",
              interaction="Cooked beef and honey bottles.",
              signature=("Chooser of the slain", "Befriended, when her friend dies within 32 blocks of her, she gathers everything they drop "
                                                 "and gives it back when they come within 6 blocks of her again."),
              danger="Low (her spear).",
              variants=[("silver", "Silver (default)", "Silver mail, white wings"), ("gold", "Gold", "Gold"), ("raven", "Raven", "Black wings"),
                        ("red", "Red", "Red"), ("blue", "Blue", "Blue")]),
         {"Habitat": "Mountain peaks", "Trades": "No", "Signature": "Keeps a fallen friend's things", "Danger": "Low"})],
}
for _fid, _entries in SIXTEENTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# seventeenth wave (2026-10-27): eight Harmontide originals from the reference art (their blue looks) and four of folklore
SEVENTEENTH = {
    "nekomata": [
        (dict(id="censer_lion_nekomata", name="Censer Lion", group="Savanna", tag="Stately · serene", height=1.8, biomes=["savanna", "savanna_plateau", "desert"],
              temperament="Stately, serene",
              ecology="A Harmontide lion of lapis and gold filigree who carries a smoking censer, its incense sweet and drowsy.",
              interaction="Cooked beef and cooked mutton.",
              signature=("Incense", "Befriended, her friend near her sleeps soundly in her incense, so no phantoms come for them, and "
                                   "phantoms already hunting her friend lose interest."),
              danger="Moderate (her claws).",
              variants=[("lapis", "Lapis (default)", "Lapis and gold"), ("desert", "Desert", "Sand, a red mantle"), ("white", "White", "White lion"),
                        ("jade", "Jade", "Jade"), ("ember", "Ember", "Ember red")]),
         {"Habitat": "Savanna", "Social": "Alone", "Water": "Avoids", "Signature": "Keeps phantoms off", "Danger": "Moderate"})],
    "lizardfolk": [
        (dict(id="conch_turtle_lizardfolk", name="Conch Mail Turtle", group="Warm beaches", tag="Gentle · steady", height=1.2, biomes=["beach", "warm_ocean"],
              temperament="Gentle, steady",
              ecology="A little Harmontide sea turtle in a scaled blue shell with a conch's spire rising from it, strung with pearls.",
              interaction="Seagrass and kelp.",
              signature=("Conch call", "Befriended, her friend underwater near her has Conduit Power: they breathe, see and dig as if beside a conduit."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue shell, cream conch"), ("coral", "Coral", "Coral pink"), ("kelp", "Kelp", "Kelp green"),
                        ("sand", "Sand", "Sandy"), ("abyss", "Abyss", "Deep blue, glowing bands")]),
         {"Habitat": "Warm beaches", "Signature": "Conduit Power underwater", "Danger": "None"})],
    "bug": [
        (dict(id="date_beetle_bug", name="Date Palm Beetle", group="Oases", tag="Sweet · busy", height=1.1, biomes=["desert", "savanna"],
              temperament="Sweet, busy",
              ecology="A round little Harmontide beetle of the oasis palms, who always carries a cluster of dates in her hair.",
              interaction="Sweet berries and apples.",
              signature=("Date share", "Befriended, when her friend near her is hungry she shares a date with them (three hunger points)."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue shell, violet wings"), ("green", "Green", "Green"), ("amber", "Amber", "Amber"),
                        ("plum", "Plum", "Plum"), ("black", "Black", "Black")]),
         {"Habitat": "Oases", "Signature": "Feeds her friend", "Danger": "None"})],
    "minotaur": [
        (dict(id="dustbison_minotaur", name="Dustbison", group="Badlands and plains", tag="Playful · sturdy", height=1.4, biomes=["badlands", "savanna", "plains"],
              temperament="Playful, sturdy",
              ecology="A fluffy Harmontide bison calf with cream-tipped fur and gold spots, who loves nothing more than a roll in the dust.",
              interaction="Wheat and hay.",
              signature=("Dust bath", "Befriended, when her friend near her catches fire she rolls them in the dust: the fire goes out and they "
                                      "are fireproof for a few seconds."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Blue, cream tips, gold spots"), ("plains", "Plains", "Brown bison"), ("white", "White", "White"),
                        ("rose", "Rose", "Rose"), ("dusk", "Dusk", "Dusk violet")]),
         {"Habitat": "Badlands and plains", "Temper": "Playful", "Signature": "Puts out fire", "Danger": "Low"})],
    "bat": [
        (dict(id="frond_bat", name="Frondbat", group="Jungle canopy", tag="Merry · flighty", height=1.5, biomes=["jungle", "sparse_jungle", "bamboo_jungle"],
              temperament="Merry, flighty",
              ecology="A blue Harmontide bat of the canopy with leafy wings and a crown of white flowers, who naps hanging in the leaves.",
              interaction="Apples, melon and sweet berries.",
              signature=("Leaf shaker", "Befriended, every half minute she shakes the leaves near her friend: saplings, sticks and apples fall."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, teal wings"), ("green", "Green", "Green"), ("brown", "Brown", "Brown"),
                        ("autumn", "Autumn", "Autumn orange"), ("black", "Black", "Black")]),
         {"Habitat": "Jungle canopy", "Signature": "Shakes down saplings and apples", "Danger": "None"})],
    "merfolk": [
        (dict(id="sawfin_mermaid", name="Sawfin", group="Warm seas and mangroves", tag="Earnest · brave", height=1.7,
              biomes=["warm_ocean", "lukewarm_ocean", "mangrove_swamp"], temperament="Earnest, brave",
              ecology="A Harmontide sawfish in blue barding with a fleur-de-lis shield, her long toothed saw of a snout out in front.",
              interaction="Cod, salmon and tropical fish.",
              signature=("Undersea sapper", "Befriended, her friend underwater near her digs fast (Haste II)."),
              danger="Low (her saw).",
              variants=[("blue", "Blue (default)", "Blue, white belly"), ("grey", "Grey", "Grey, a red shield"), ("green", "Green", "Green"),
                        ("sand", "Sand", "Sand"), ("royal", "Royal", "Violet, a gold shield")]),
         {"Habitat": "Warm seas", "Young": "Live young", "On land": "Flops", "Signature": "Haste underwater", "Danger": "Low"})],
    "centaur": [
        (dict(id="stagel_centaur", name="Stagel", group="Flower forests", tag="Shy · springy", height=1.5, biomes=["flower_forest", "forest", "birch_forest"],
              temperament="Shy, springy",
              ecology="A Harmontide fawn taur, cream dappled with blue, with budding antlers and a bound in her step.",
              interaction="Apples and sweet berries.",
              signature=("Springing step", "Befriended, her friend near her jumps high (Jump Boost II)."),
              danger="None.",
              variants=[("cream", "Cream (default)", "Cream, blue dapples"), ("brown", "Brown", "Brown fawn"), ("white", "White", "White"),
                        ("rose", "Rose", "Rose"), ("moss", "Moss", "Moss green")]),
         {"Habitat": "Flower forests", "Ridden": "No: too small", "Signature": "Jump Boost", "Temperament": "Shy", "Danger": "None"}),
        (dict(id="lamassu_centaur", name="Lamassu", group="Deserts and badlands", tag="Solemn · steadfast", height=2.2,
              biomes=["desert", "badlands", "savanna_plateau"], temperament="Solemn, steadfast",
              ecology="The winged, horned, lion-bodied guardian of the gates of Assyria, with long grey curls falling down her back.",
              interaction="Cooked beef and golden carrots.",
              signature=("Gate guardian", "Befriended, her friend near her shrugs off blows (Resistance I)."),
              danger="Moderate (her claws and horns).",
              variants=[("grey", "Grey (default)", "Grey stone"), ("gold", "Gold", "Lion gold"), ("alabaster", "Alabaster", "White"),
                        ("lapis", "Lapis", "Lapis and gold"), ("basalt", "Basalt", "Black")]),
         {"Habitat": "Deserts", "Ridden": "Yes: by her friend", "Signature": "Resistance", "Temperament": "Steadfast", "Danger": "Moderate"})],
    "slime": [
        (dict(id="klabuster_slime", name="Klabuster", group="Pastures and swamps", tag="Cheerful · shameless", height=1.2,
              biomes=["plains", "meadow", "swamp"], temperament="Cheerful, shameless",
              ecology="A Harmontide jest: a glossy little swirl of a slime, named for the German “Klabusterbeere”, who dangles on a "
                      "single strand from a tuft of hair and is very pleased with herself.",
              interaction="Wheat, carrots and beetroot.",
              signature=("Manure", "Befriended, every half minute the crops near her friend grow a stage, as if given bone meal."),
              danger="None.",
              variants=[("brown", "Brown (default)", "Glossy brown"), ("blue", "Blue", "Blue"), ("green", "Green", "Green"),
                        ("purple", "Purple", "Purple"), ("gold", "Gold", "Gold")]),
         {"Habitat": "Pastures", "Favourite food": "Wheat", "Element": "Earth", "Weakness": "Soap", "Danger": "None"})],
    "flora": [
        (dict(id="dryad_flora", name="Dryad", group="Forests", tag="Kind · rooted", height=1.7, biomes=["forest", "flower_forest", "old_growth_birch_forest"],
              temperament="Kind, rooted",
              ecology="The tree nymph of Greek myth, bound to her tree: a girl of smooth pale wood, with leafy branches for antlers.",
              interaction="Bone meal and apples.",
              signature=("Tree mother", "Befriended, every half minute the saplings near her friend grow as if given bone meal."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Pale blue wood"), ("oak", "Oak", "Oak"), ("birch", "Birch", "Birch"),
                        ("cherry", "Cherry", "Cherry"), ("autumn", "Autumn", "Autumn")]),
         {"Habitat": "Forests", "Signature": "Grows saplings", "Danger": "None"})],
    "canid": [
        (dict(id="amarok_canid", name="Amarok", group="Snowy taiga", tag="Lone · fierce", height=2.0, biomes=["snowy_taiga", "snowy_plains", "grove"],
              temperament="Solitary, fierce",
              ecology="The giant lone wolf of Inuit legend, slate grey and shaggy, who hunts alone and fears nothing.",
              interaction="Beef, mutton and bones.",
              signature=("Pack mother", "Befriended, her friend's tamed wolves near her fight harder (Strength I)."),
              danger="High (her jaws).",
              variants=[("slate", "Slate (default)", "Slate grey, pale ruff"), ("white", "White", "White"), ("black", "Black", "Black"),
                        ("storm", "Storm", "Storm blue"), ("tundra", "Tundra", "Tundra brown")]),
         {"Habitat": "Snowy taiga", "Kin": "Wolves", "Signature": "Strength for tamed wolves", "Danger": "High"})],
    "undead": [
        (dict(id="yurei_undead", name="Yurei", group="Cherry groves at night", tag="Wistful · faithful", height=1.6, biomes=["cherry_grove", "dark_forest"],
              temperament="Wistful, faithful",
              ecology="The ghost of Japanese folklore: a girl in a school uniform with long black hair, her legs fading into a white wisp.",
              interaction="Bread and flowers.",
              signature=("Haunting", "At night, befriended, she haunts the monsters near her friend: they grow weak (Weakness I)."),
              danger="Low.",
              variants=[("red", "Red (default)", "Red skirt"), ("navy", "Navy", "Navy skirt"), ("white", "White", "All white"),
                        ("pale", "Pale", "Pale teal"), ("plum", "Plum", "Plum")]),
         {"Haunts": "Cherry groves", "Signature": "Weakens monsters at night", "Danger": "Low"})],
}
for _fid, _entries in SEVENTEENTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# eighteenth wave (2026-10-28): eight Harmontide originals from the reference art (their blue looks; the mirror crane, a
# harpy, lives in the harpy tools) and four of folklore
EIGHTEENTH = {
    "arachne": [
        (dict(id="spoodr_arachne", name="Spoodr", group="Old houses and caves", tag="Cuddly · curious", height=1.1,
              biomes=["forest", "dark_forest", "dripstone_caves"], temperament="Cuddly, curious",
              ecology="A round, fluffy little Harmontide spider, navy with pale blue tips, all big shining eyes, who tidies up old webs.",
              interaction="Spider eyes and sweet berries.",
              signature=("Web sweeper", "Befriended, every few seconds she clears a cobweb near her friend and keeps the string for them."),
              danger="None.",
              variants=[("navy", "Navy (default)", "Navy, pale blue tips"), ("green", "Green", "Green"), ("purple", "Purple", "Purple"),
                        ("red", "Red", "Red"), ("yellow", "Yellow", "Yellow")]),
         {"Habitat": "Old houses and caves", "Signature": "Clears cobwebs", "Danger": "None"}),
        (dict(id="ushi_oni_arachne", name="Ushi-oni", group="Rocky shores", tag="Fearsome · loyal", height=2.0,
              biomes=["stony_shore", "beach", "windswept_hills"], temperament="Fearsome, loyal",
              ecology="The ox-headed spider demon of Japanese coasts: a great black and red spider body, an ox's horns and a bellow "
                      "that carries over the waves.",
              interaction="Beef and cod.",
              signature=("Bellow", "Befriended, when monsters come near her friend she bellows: they flee, slowed."),
              danger="High (her fangs and horns).",
              variants=[("black", "Black (default)", "Black and red"), ("crimson", "Crimson", "Red and black"), ("indigo", "Indigo", "Indigo and gold"),
                        ("ash", "Ash", "Ash grey"), ("ochre", "Ochre", "Ochre")]),
         {"Habitat": "Rocky shores", "Signature": "Monsters flee", "Danger": "High"})],
    "lamia": [
        (dict(id="urchskin_lamia", name="Urchskin", group="Reefs and warm shores", tag="Prickly · protective", height=1.7,
              biomes=["warm_ocean", "beach", "lukewarm_ocean"], temperament="Prickly, protective",
              ecology="A Harmontide sea serpent, teal with a cream belly, bristling with purple urchin spines from her crest to her tail tip.",
              interaction="Cod, salmon and kelp.",
              signature=("Urchin spines", "Befriended, monsters that hurt her friend near her are pricked by her spines in return."),
              danger="Moderate (her spines).",
              variants=[("teal", "Teal (default)", "Teal, purple spines"), ("black", "Black", "Black"), ("green", "Green", "Green"),
                        ("red", "Red", "Red"), ("yellow", "Yellow", "Yellow")]),
         {"Habitat": "Reefs", "Signature": "Thorns for her friend", "Danger": "Moderate"})],
    "rodent": [
        (dict(id="pineskrell_rodent", name="Pineskrell", group="Taiga", tag="Bright · nosy", height=1.3, biomes=["taiga", "old_growth_pine_taiga", "snowy_taiga"],
              temperament="Bright, nosy",
              ecology="A blue Harmontide squirrel of the pine woods, with a huge frosted tail and glowing crystal pinecones she gathers "
                      "from deep under the roots.",
              interaction="Sweet berries and seeds.",
              signature=("Ore sense", "Befriended, every few seconds the ores near her friend sparkle."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, glowing cones"), ("brown", "Brown", "Brown"), ("green", "Green", "Green"),
                        ("purple", "Purple", "Purple"), ("red", "Red", "Red")]),
         {"Habitat": "Taiga", "Signature": "Shows ores", "Danger": "None"})],
    "lizardfolk": [
        (dict(id="turtfolk_lizardfolk", name="Turtfolk", group="Beaches and rivers", tag="Bold · warm", height=1.75, biomes=["beach", "river", "swamp"],
              temperament="Bold, warm",
              ecology="A tall Harmontide turtle warrior, blue-scaled, in bronze, with a great brown shell, long brown hair and a "
                      "yellow flower behind her ear.",
              interaction="Seagrass and melon.",
              signature=("Shell guard", "Befriended, every half minute she shields her friend near her: two hearts of absorption."),
              danger="Moderate.",
              variants=[("blue", "Blue (default)", "Blue, brown shell"), ("black", "Black", "Black"), ("green", "Green", "Green"),
                        ("purple", "Purple", "Purple"), ("red", "Red", "Red")]),
         {"Habitat": "Beaches and rivers", "Signature": "Absorption hearts", "Danger": "Moderate"})],
    "canid": [
        (dict(id="tarnhelm_canid", name="Tarnhelm", group="Snowy taiga and groves", tag="Watchful · stern", height=1.85,
              biomes=["snowy_taiga", "grove", "taiga"], temperament="Watchful, stern",
              ecology="A Harmontide war wolf in blue enamelled plate and a silver helm, its gem shining like the northern lights.",
              interaction="Beef, mutton and bones.",
              signature=("Sentinel", "Befriended, raiders and illagers within 48 blocks of her friend glow through walls."),
              danger="High (her jaws).",
              variants=[("blue", "Blue (default)", "Blue plate, silver fur"), ("silver", "Silver", "Silver"), ("black", "Black", "Black"),
                        ("gold", "Gold", "Gold"), ("storm", "Storm", "Storm grey")]),
         {"Habitat": "Snowy taiga", "Kin": "Wolves", "Signature": "Raiders glow", "Danger": "High"})],
    "centaur": [
        (dict(id="palmyr_centaur", name="Palmyr", group="Flower forests and meadows", tag="Graceful · quick", height=1.9,
              biomes=["flower_forest", "meadow", "cherry_grove"], temperament="Graceful, quick",
              ecology="A blue Harmontide deer taur patterned in white diamonds, with feathery frond antlers and a fan of a tail.",
              interaction="Apples and sweet berries.",
              signature=("Fleet foot", "Befriended, her friend near her runs faster while sprinting (Speed I)."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Blue, white diamonds"), ("white", "White", "White"), ("teal", "Teal", "Teal"),
                        ("violet", "Violet", "Violet"), ("dusk", "Dusk", "Dusk")]),
         {"Habitat": "Flower forests", "Ridden": "Yes: by her friend", "Signature": "Speed while sprinting", "Temperament": "Graceful", "Danger": "Low"})],
    "minotaur": [
        (dict(id="skjoldram_minotaur", name="Skjoldram", group="Windswept hills", tag="Stubborn · staunch", height=1.7,
              biomes=["windswept_hills", "meadow", "snowy_slopes"], temperament="Stubborn, staunch",
              ecology="A shaggy teal Harmontide ram in a steel brow plate, her curling horns red as rust, who charges anything that hisses.",
              interaction="Wheat and hay.",
              signature=("Ram charge", "Befriended, she charges creepers that come near her friend and knocks them away before they blow."),
              danger="Moderate (her horns).",
              variants=[("teal", "Teal (default)", "Teal wool, red horns"), ("black", "Black", "Black"), ("brown", "Brown", "Brown"),
                        ("purple", "Purple", "Purple"), ("red", "Red", "Red")]),
         {"Habitat": "Windswept hills", "Temper": "Stubborn", "Signature": "Knocks creepers away", "Danger": "Moderate"})],
    "nekomata": [
        (dict(id="bastet_nekomata", name="Bastet", group="Deserts", tag="Regal · aloof", height=1.75, biomes=["desert", "badlands"],
              temperament="Regal, aloof",
              ecology="The cat goddess of Egypt, guardian of the home: a black cat girl in gold, with a goddess's green eyes.",
              interaction="Cod and salmon.",
              signature=("Home guardian", "Befriended, creepers will not hunt her friend near her: they lose interest and slink away."),
              danger="Low.",
              variants=[("black", "Black (default)", "Black, gold"), ("sand", "Sand", "Sand"), ("silver", "Silver", "Silver"),
                        ("night", "Night", "Night blue"), ("white", "White", "White")]),
         {"Habitat": "Desert", "Social": "Alone", "Water": "Avoids", "Signature": "Creepers keep away", "Danger": "Low"})],
    "merfolk": [
        (dict(id="rusalka_mermaid", name="Rusalka", group="Rivers and lakes", tag="Wistful · playful", height=1.7, biomes=["river", "swamp", "birch_forest"],
              temperament="Wistful, playful",
              ecology="The water spirit of Slavic rivers, pale, with long loose green hair and a white shift, who sings to fishers.",
              interaction="Cod, salmon and flowers.",
              signature=("River's luck", "Befriended, her friend fishing near her has the Luck of the Sea."),
              danger="Low.",
              variants=[("river", "River (default)", "Green hair, a grey-green tail"), ("moonlit", "Moonlit", "Silver"), ("reed", "Reed", "Reed gold"),
                        ("ash", "Ash", "Ash"), ("lily", "Lily", "Lily pink")]),
         {"Habitat": "Rivers", "Young": "Live young", "On land": "Flops", "Signature": "Fishing luck", "Danger": "Low"})],
    "elf": [
        (dict(id="domovoi_elf", name="Domovoi", group="Villages", tag="Homely · fussy", height=1.2, biomes=["plains", "taiga", "meadow"],
              temperament="Homely, fussy",
              ecology="The house spirit of Slavic folklore, who lives behind the stove: a little straw-haired girl in an embroidered "
                      "smock and a red kerchief, a smudge of soot on her nose.",
              interaction="Bread and milk.",
              signature=("Hearth keeper", "Befriended, the lit furnaces, smokers and blast furnaces near her friend smelt faster."),
              danger="None.",
              variants=[("red", "Red (default)", "Red kerchief, white smock"), ("blue", "Blue", "Blue kerchief"), ("green", "Green", "Green kerchief"),
                        ("soot", "Soot", "Sooty"), ("gold", "Gold", "Gold kerchief")]),
         {"Habitat": "Villages", "Signature": "Faster furnaces", "Danger": "None"})],
}
for _fid, _entries in EIGHTEENTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# nineteenth wave (2026-10-28): eight Harmontide originals from the reference art (their blue looks) and four creatures of
# folklore (Camazotz, the akaname, the hoop snake and the draugr)
NINETEENTH = {
    "construct": [
        (dict(id="tin_jack_construct", name="Tin Jack", group="Fields and pumpkin patches", tag="Watchful · spooky", height=1.8,
              biomes=["plains", "sunflower_plains", "meadow"], temperament="Watchful, a little spooky",
              ecology="A Harmontide scarecrow of riveted blue tin in a tattered cloak and a pointed hat, a carved grin for a face "
                      "and a lantern at her hip. Crows perch on her arm; nothing else dares.",
              interaction="Pumpkin pie and wheat.",
              signature=("Scarecrow", "Befriended, phantoms that come near her friend lose heart: weakened, they wheel away."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Blue tin, a navy hat"), ("silver", "Silver", "Bright tin"), ("copper", "Copper", "Copper, a green hat"),
                        ("green", "Green", "Green-painted tin"), ("purple", "Purple", "Purple, an orange hat")]),
         {"Habitat": "Fields", "Signature": "Phantoms flee", "Danger": "Low"})],
    "lamia": [
        (dict(id="turquoise_serpent_lamia", name="Turquoise Serpent", group="Jungle rivers", tag="Proud · bejewelled", height=1.8,
              biomes=["jungle", "sparse_jungle", "bamboo_jungle"], temperament="Proud, generous",
              ecology="A Harmontide river serpent in royal blue with a turquoise belly, a feathered crest and coils hung with gold and "
                      "turquoise. Her scales are as hard as the stones she wears.",
              interaction="Gold nuggets, cod and melon.",
              signature=("Scale ward", "Befriended, her friend within 10 blocks of her has Resistance while they are badly hurt."),
              danger="Moderate.",
              variants=[("blue", "Blue (default)", "Blue, a turquoise belly"), ("jade", "Jade", "Jade green"), ("gold", "Gold", "Gold"),
                        ("coral", "Coral", "Coral red"), ("obsidian", "Obsidian", "Black")]),
         {"Habitat": "Jungle rivers", "Signature": "Resistance when hurt", "Danger": "Moderate"}),
        (dict(id="hoop_snake_lamia", name="Hoop Snake", group="Hills and badlands", tag="Bouncy · prickly", height=1.5,
              biomes=["badlands", "wooded_badlands", "windswept_hills", "savanna"], temperament="Bouncy, quick to sting",
              ecology="The hoop snake of American tall tales, who takes her tail in her mouth and rolls downhill like a wheel. Her tail "
                      "ends in a sting so strong a tree it pricks withers.",
              interaction="Rabbit and eggs.",
              signature=("Tail sting", "Befriended, every few seconds she stings a monster near her friend: poisoned."),
              danger="Moderate (her sting).",
              variants=[("sky", "Sky (default)", "Sky blue, a cream belly"), ("green", "Green", "Green"), ("red", "Red", "Red"),
                        ("black", "Black", "Black"), ("yellow", "Yellow", "Yellow")]),
         {"Habitat": "Badlands, hills", "Signature": "Stings monsters", "Danger": "Moderate"})],
    "merfolk": [
        (dict(id="moopie_mermaid", name="Moopie", group="Warm shallows", tag="Placid · nurturing", height=1.7,
              biomes=["warm_ocean", "lukewarm_ocean", "beach"], temperament="Placid, nurturing",
              ecology="A Harmontide sea cow: a cream and brown spotted tail, little horns and cow's ears in a mane of curling sea-green "
                      "hair. She grazes the seagrass and her milk is said to cure anything.",
              interaction="Kelp, seagrass and wheat.",
              signature=("Sea milk", "Befriended, every few seconds she soothes her friend near her: poison, hunger and nausea wear off."),
              danger="None.",
              variants=[("green", "Green (default)", "Sea-green hair, cream and brown"), ("brown", "Brown", "Brown"),
                        ("black", "Black", "Black and white"), ("pink", "Pink", "Pink"), ("blue", "Blue", "Blue")]),
         {"Habitat": "Warm shallows", "Signature": "Cures poison and hunger", "Danger": "None"})],
    "minotaur": [
        (dict(id="tuskin_minotaur", name="Tuskin", group="Snowy plains", tag="Gentle · unstoppable", height=2.3,
              biomes=["snowy_plains", "ice_spikes", "snowy_slopes"], temperament="Gentle, unstoppable",
              ecology="A shaggy Harmontide mammoth girl, deep blue wool, white cheeks and great curving tusks bound in iron bands. "
                      "She ploughs through snowdrifts as if they were not there.",
              interaction="Wheat, hay and apples.",
              signature=("Snowplough", "Befriended, the powder snow round her friend packs into snow blocks, and the cold cannot "
                                       "freeze them."),
              danger="Moderate (her tusks).",
              variants=[("blue", "Blue (default)", "Blue wool"), ("brown", "Brown", "Brown"), ("grey", "Grey", "Grey"),
                        ("white", "White", "White"), ("red", "Red", "Red")]),
         {"Habitat": "Snowy plains", "Signature": "Packs powder snow", "Danger": "Moderate"})],
    "nekomata": [
        (dict(id="sabre_nekomata", name="Sabre", group="Cold forests", tag="Fierce · loyal", height=1.9,
              biomes=["snowy_taiga", "old_growth_spruce_taiga", "grove"], temperament="Fierce, loyal",
              ecology="A Harmontide sabre-tooth: shaggy blue fur, white ruff and paws, two long fangs and black leather armour. She "
                      "hunts the wounded and never lets one get away.",
              interaction="Raw mutton and beef.",
              signature=("Finishing bite", "Befriended, she finishes off a badly wounded monster near her friend."),
              danger="High (her fangs).",
              variants=[("blue", "Blue (default)", "Blue, white ruff"), ("tawny", "Tawny", "Tawny"), ("snow", "Snow", "White"),
                        ("black", "Black", "Black"), ("dun", "Dun", "Dun, spotted")]),
         {"Habitat": "Cold forests", "Signature": "Finishes wounded monsters", "Danger": "High"})],
    "canid": [
        (dict(id="obsidian_hound_canid", name="Obsidian Hound", group="Badlands and deserts", tag="Stern · watchful", height=1.9,
              biomes=["desert", "badlands", "eroded_badlands"], temperament="Stern, watchful",
              ecology="The obsidian macuahuitl hound: a Harmontide temple dog in navy and gold, with a wooden war club edged in "
                      "obsidian slung across her back. Lava cools under her paws.",
              interaction="Cooked meat and gold.",
              signature=("Lava walker", "Befriended, still lava near her friend's feet cools to obsidian."),
              danger="High (her club).",
              variants=[("navy", "Navy (default)", "Navy and gold"), ("black", "Black", "Black"), ("jade", "Jade", "Jade"),
                        ("red", "Red", "Red"), ("gold", "Gold", "Gold")]),
         {"Habitat": "Badlands", "Signature": "Cools lava", "Danger": "High"})],
    "undead": [
        (dict(id="kasurenijimi_undead", name="Kasurenijimi", group="Dark forests", tag="Shy · unseen", height=1.6,
              biomes=["dark_forest", "birch_forest", "old_growth_birch_forest"], temperament="Shy, hard to look at",
              ecology="A Harmontide ghost in a school uniform with long blue-black hair, whose face nobody can see clearly: it is smeared "
                      "and scratched out like an old photograph.",
              interaction="Cookies and sweet berries.",
              signature=("Smudged", "Befriended, while her friend sneaks near her they blur out of sight."),
              danger="None.",
              variants=[("navy", "Navy (default)", "Navy collar, red bow"), ("black", "Black", "Black uniform"), ("grey", "Grey", "Grey"),
                        ("green", "Green", "Green"), ("red", "Red", "Red")]),
         {"Habitat": "Dark forests", "Signature": "Hides her sneaking friend", "Danger": "None"}),
        (dict(id="draugr_undead", name="Draugr", group="Barrows and cold hills", tag="Grim · loyal", height=1.7,
              biomes=["windswept_hills", "snowy_slopes", "taiga", "stony_peaks"], temperament="Grim, fiercely loyal",
              ecology="The barrow-dweller of Norse sagas, grey as a corpse with pale glowing eyes, who guards her grave goods. The other "
                      "dead do as she says.",
              interaction="Rotten flesh, bread and gold.",
              signature=("Lord of the barrow", "Befriended, the undead near her friend forget them."),
              danger="Moderate.",
              variants=[("grey", "Grey (default)", "Corpse grey"), ("blue", "Hel-blue", "Hel-blue"), ("black", "Black", "Black"),
                        ("bronze", "Bronze", "Barrow gold"), ("frost", "Frost", "Frost")]),
         {"Habitat": "Barrows", "Signature": "The undead ignore her friend", "Danger": "Moderate"})],
    "bat": [
        (dict(id="camazotz_bat", name="Camazotz", group="Jungle caves", tag="Dread · devoted", height=1.6,
              biomes=["jungle", "bamboo_jungle", "lush_caves"], temperament="Dread, devoted",
              ecology="The death bat of the Maya, a leaf-nosed bat with great pink ears and jade at her ears and arms, who flies at night "
                      "and drinks the strength of her prey.",
              interaction="Melon, berries and raw chicken.",
              signature=("Night bite", "Befriended, at night she bites a monster near her friend and the stolen strength heals them."),
              danger="Moderate.",
              variants=[("blue", "Blue (default)", "Blue-grey, pink ears"), ("black", "Black", "Black"), ("brown", "Brown", "Brown"),
                        ("red", "Red", "Red"), ("jade", "Jade", "Jade")]),
         {"Habitat": "Jungle caves", "Signature": "Night bite heals her friend", "Danger": "Moderate"})],
    "devil": [
        (dict(id="akaname_devil", name="Akaname", group="Villages and old bathhouses", tag="Cheerful · tidy", height=1.5,
              biomes=["plains", "swamp", "cherry_grove"], temperament="Cheerful, a little gross",
              ecology="The filth-licker of Japanese tales, a yokai who slips into bathhouses at night to lick the grime away with her "
                      "long tongue.",
              interaction="Sweet berries and honey.",
              signature=("Scrub", "Befriended, she scrubs the moss off the mossy stone and cobblestone near her friend."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue hair and dress"), ("red", "Red", "Red skin"), ("green", "Green", "Green"),
                        ("purple", "Purple", "Purple"), ("pink", "Pink", "Pink")]),
         {"Habitat": "Villages", "Signature": "Cleans mossy stone", "Danger": "None"})],
}

for _fid, _entries in NINETEENTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# twentieth wave (2026-10-28): eight Harmontide originals from the reference art (their blue looks), four creatures of folklore
# (the ghoul, the manticore, the Mothman and the basilisk) and the house mouse, for the Gregory Horror Show homages
TWENTIETH = {
    "minotaur": [
        (dict(id="brasshorn_minotaur", name="Brasshorn Ram", group="Mountains", tag="Proud · surefooted", height=2.2,
              biomes=["windswept_hills", "stony_peaks", "meadow", "jagged_peaks"], temperament="Proud, surefooted",
              ecology="A Harmontide mountain ram girl, shaggy and blue, her great curling horns grown into brass. Brass bands her brow "
                      "and hooves, a white rune shines between her eyes and a woven blanket hangs over her back. She leaps from crag to crag.",
              interaction="Wheat and sweet berries.",
              signature=("Mountain footing", "Befriended, her friend near her jumps higher (Jump Boost)."),
              danger="Moderate (her horns).",
              variants=[("blue", "Blue (default)", "Blue wool, brass horns"), ("grey", "Grey", "Grey, a red blanket"), ("brown", "Brown", "Brown"),
                        ("white", "White", "White, silver horns"), ("black", "Black", "Black, a red blanket")]),
         {"Habitat": "Mountains", "Signature": "Jump Boost", "Danger": "Moderate"})],
    "centaur": [
        (dict(id="ceibler_centaur", name="Ceibler", group="Meadows and groves", tag="Gentle · festive", height=2.2,
              biomes=["meadow", "cherry_grove", "grove"], temperament="Gentle, festive",
              ecology="A slender Harmontide llama-deer, pale blue with blue diamonds over her coat, long ears and little horns. She "
                      "wears a fringed blanket and a harness hung with tassels and bells, and knows every trail through the hills.",
              interaction="Wheat, apples and flowers.",
              signature=("Trail guide", "Befriended, her friend near her runs faster on grass, dirt and paths (Speed)."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Pale blue, blue diamonds"), ("rose", "Rose", "Rose"), ("fawn", "Fawn", "Fawn, a red blanket"),
                        ("jade", "Jade", "Jade"), ("dusk", "Dusk", "Lavender")]),
         {"Habitat": "Meadows", "Signature": "Speed on grass", "Danger": "Low"})],
    "rodent": [
        (dict(id="sobolnik_rodent", name="Sobolnik", group="Cold forests", tag="Curious · secretive", height=1.5,
              biomes=["taiga", "snowy_taiga", "old_growth_pine_taiga"], temperament="Curious, secretive",
              ecology="A Harmontide sable in blue, big dark eyes in a white face, frosty swirls in her fur, a bushy tail and a little blue "
                      "gem at her throat. She knows every burrow and tunnel under the snow.",
              interaction="Sweet berries, eggs and fish.",
              signature=("Burrow eyes", "Befriended, her friend near her sees in the dark when out of the sky's sight (Night Vision)."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Blue, a white face"), ("sable", "Sable", "Dark brown, an amber gem"), ("silver", "Silver", "Silver"),
                        ("green", "Green", "Green"), ("red", "Red", "Red")]),
         {"Habitat": "Cold forests", "Signature": "Night Vision underground", "Danger": "Low"}),
        (dict(id="house_mouse_rodent", name="House Mouse", group="Villages and fields", tag="Timid · thrifty", height=1.4,
              biomes=["plains", "meadow", "forest", "sunflower_plains"], temperament="Timid, thrifty",
              ecology="A little mouse girl with big round ears, a twitchy pink nose and a long bare tail. She lives in the walls of "
                      "farmhouses and never lets a crumb go to waste.",
              interaction="Bread, wheat and seeds.",
              signature=("Crumbs", "Befriended, now and then she shares a crumb with her hungry friend (food)."),
              danger="None.",
              variants=[("grey", "Grey (default)", "Grey, a tan vest"), ("brown", "Brown", "Brown"), ("white", "White", "White, red eyes"),
                        ("black", "Black", "Black"), ("fawn", "Fawn", "Fawn")]),
         {"Habitat": "Villages", "Signature": "Shares food", "Danger": "None"})],
    "lizardfolk": [
        (dict(id="turtworld_lizardfolk", name="Turtworld", group="Rivers and coasts", tag="Ancient · serene", height=2.0,
              biomes=["river", "stony_shore", "beach", "mangrove_swamp"], temperament="Ancient, serene",
              ecology="A great blue Harmontide tortoise who has carried a little world on her shell for so long it has its own turf, two "
                      "snowy peaks and a waterfall. Things grow wherever she rests.",
              interaction="Melon, seagrass and saplings.",
              signature=("Living island", "Befriended, the saplings and crops near her friend grow."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Blue, a green island"), ("green", "Green", "Green, a brown shell"), ("sand", "Sand", "Sandy"),
                        ("frost", "Frost", "Frosted"), ("ember", "Ember", "Black, a smouldering island")]),
         {"Habitat": "Rivers, coasts", "Signature": "Grows plants", "Danger": "Low"}),
        (dict(id="basilisk_lizardfolk", name="Basilisk", group="Badlands and deserts", tag="Regal · stony", height=1.5,
              biomes=["badlands", "eroded_badlands", "desert"], temperament="Regal, cold",
              ecology="The king of serpents of European bestiaries, here a little blue lizard girl with a crest of spines down her back "
                      "and a gold crown. A look from her turns living things to stone.",
              interaction="Eggs and spider eyes.",
              signature=("Stone gaze", "Befriended, she stares down a monster near her friend: it can barely move (Slowness)."),
              danger="Moderate (her gaze).",
              variants=[("blue", "Blue (default)", "Blue, a gold crown"), ("green", "Green", "Green"), ("stone", "Stone", "Stone grey"),
                        ("black", "Black", "Black"), ("gold", "Gold", "Gold, a silver crown")]),
         {"Habitat": "Badlands", "Signature": "Slows monsters", "Danger": "Moderate"})],
    "dragon": [
        (dict(id="moo_dragon", name="Moo", group="Plains and savannas", tag="Stubborn · sweet", height=1.4,
              biomes=["plains", "savanna", "sunflower_plains"], temperament="Stubborn, sweet",
              ecology="A round little Harmontide triceratops in spotted blue, with a frill rimmed in cream knobs, two brow horns and a "
                      "nose horn. She charges anything that frightens her, then hides behind her friend.",
              interaction="Leaves, ferns and wheat.",
              signature=("Herd stand", "Befriended, when monsters gang up on her friend she stands with them: her friend is stronger (Strength)."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Spotted blue"), ("green", "Green", "Green"), ("orange", "Orange", "Orange"),
                        ("pink", "Pink", "Pink"), ("purple", "Purple", "Purple")]),
         {"Habitat": "Plains", "Signature": "Strength against a crowd", "Danger": "Low"}),
        (dict(id="hairypterix_dragon", name="Hairypterix", group="Cliffs and coasts", tag="Loud · daring", height=1.8,
              biomes=["stony_shore", "windswept_hills", "jagged_peaks", "stony_peaks"], temperament="Loud, daring",
              ecology="A shaggy blue Harmontide pterosaur with great membrane wings, a long yellow crest swept back from her head and "
                      "yellow claws. She dives off sea cliffs and rides the wind back up.",
              interaction="Cod, salmon and tropical fish.",
              signature=("Updraft", "Befriended, her friend falling near her drifts down gently (Slow Falling)."),
              danger="Moderate.",
              variants=[("blue", "Blue (default)", "Blue, a yellow crest"), ("rust", "Rust", "Rust"), ("grey", "Grey", "Grey, a red crest"),
                        ("green", "Green", "Green"), ("black", "Black", "Black")]),
         {"Habitat": "Cliffs", "Signature": "Slow Falling", "Danger": "Moderate"})],
    "merfolk": [
        (dict(id="doggo_mermaid", name="Doggo", group="Cold coasts", tag="Eager · loyal", height=1.5,
              biomes=["cold_ocean", "ocean", "beach", "stony_shore"], temperament="Eager, loyal",
              ecology="A Harmontide sea dog: a shaggy blue pup with floppy ears and a white chest above, a sparkling fish's tail below. "
                      "She paddles out to anyone in the water and will not let them go under.",
              interaction="Cod, salmon and cooked meat.",
              signature=("Lifeguard", "Befriended, when her friend near her runs short of air underwater she brings them up a breath."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Shaggy blue"), ("golden", "Golden", "Golden"), ("black", "Black", "Black"),
                        ("white", "White", "White"), ("pink", "Pink", "Pink")]),
         {"Habitat": "Cold coasts", "Signature": "Refills air", "Danger": "None"})],
    "undead": [
        (dict(id="ghoul_undead", name="Ghoul", group="Deserts and graveyards", tag="Hungry · sly", height=1.7,
              biomes=["desert", "badlands", "dark_forest"], temperament="Hungry, sly",
              ecology="The ghul of Arabian tales, who haunts graveyards and empty places: blue-grey cracked skin, wild teal hair, claws "
                      "and a torn shift. She eats what the dead leave behind.",
              interaction="Rotten flesh and bones.",
              signature=("Carrion", "Befriended, she eats the rotten flesh lying near her friend, and it heals her."),
              danger="Moderate.",
              variants=[("blue", "Blue (default)", "Blue-grey, teal hair"), ("grey", "Grey", "Grey"), ("sand", "Sand", "Sandy"),
                        ("green", "Green", "Green"), ("violet", "Violet", "Violet")]),
         {"Habitat": "Deserts", "Signature": "Eats rotten flesh", "Danger": "Moderate"})],
    "nekomata": [
        (dict(id="manticore_nekomata", name="Manticore", group="Savannas and deserts", tag="Fierce · vain", height=1.9,
              biomes=["savanna", "windswept_savanna", "desert"], temperament="Fierce, vain",
              ecology="The man-eater of Persian legend, here a pale blue lion girl with a great mane, her tail ending in a scorpion's "
                      "sting that she flicks at anything she dislikes.",
              interaction="Raw beef and mutton.",
              signature=("Tail spike", "Befriended, every few seconds she stings a monster near her friend: hurt and slowed."),
              danger="High (her sting).",
              variants=[("blue", "Blue (default)", "Pale blue, a dark sting"), ("sand", "Sand", "Sandy, a red mane"), ("red", "Red", "Red"),
                        ("black", "Black", "Black, a red sting"), ("white", "White", "White")]),
         {"Habitat": "Savannas", "Signature": "Stings monsters", "Danger": "High"})],
    "bug": [
        (dict(id="mothman_bug", name="Mothman", group="Dark forests", tag="Ominous · gentle", height=1.8,
              biomes=["dark_forest", "taiga", "old_growth_birch_forest"], temperament="Ominous, gentle",
              ecology="The Mothman of Point Pleasant, here a soft blue moth girl with feathery antennae, a furry ruff and spotted wings, "
                      "her red eyes shining in the dark. Where she appears, trouble is coming.",
              interaction="Honey and glow berries.",
              signature=("Omen", "Befriended, at night the monsters near her friend glow, seen through walls."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, red eyes"), ("grey", "Grey", "Grey"), ("black", "Black", "Black"),
                        ("brown", "Brown", "Brown"), ("pink", "Pink", "Pink")]),
         {"Habitat": "Dark forests", "Signature": "Monsters glow at night", "Danger": "None"})],
}

for _fid, _entries in TWENTIETH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# twenty-first wave (2026-10-28): eight Harmontide creatures from the reference art (their blue looks) and four creatures of
# folklore (the serpopard, Urcuchillay, the rakshasa and the gashadokuro)
TWENTY_FIRST = {
    "dragon": [
        (dict(id="nenekea_dragon", name="Nenekea", group="Warm oceans and reefs", tag="Graceful · generous", height=1.8,
              biomes=["warm_ocean", "lukewarm_ocean", "beach"], temperament="Graceful, generous",
              ecology="A Harmontide sea dragon who walks on land as gladly as she swims: blue scales, fins for ears, a crest of fins on "
                      "her head and a long tail ending in a great fan of a fin. She hangs herself with beads and pearls from the reef.",
              interaction="Fish and prismarine crystals.",
              signature=("Tidal balm", "Befriended, her hurt friend near her heals while they stand in water (Regeneration)."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Blue, white fins, gold beads"), ("teal", "Teal", "Teal"), ("coral", "Coral", "Coral pink"),
                        ("gold", "Gold", "Gold"), ("violet", "Violet", "Violet")]),
         {"Habitat": "Warm oceans", "Signature": "Heals in water", "Danger": "Low"})],
    "canid": [
        (dict(id="rheintalensis_canid", name="Rheintalensis", group="Caves and old forests", tag="Loud · fearless", height=2.1,
              biomes=["old_growth_spruce_taiga", "taiga", "windswept_forest", "dripstone_caves"], temperament="Loud, fearless",
              ecology="A Harmontide cave bear girl, broad and shaggy, wild blue hair, pelts and a necklace of teeth. She carries a club "
                      "as long as her arm and laughs as she swings it.",
              interaction="Salmon, honey and sweet berries.",
              signature=("Brute swing", "Befriended, she clubs a monster that comes close to her friend and knocks it away."),
              danger="High (her club).",
              variants=[("blue", "Blue (default)", "Blue fur, a stone-grey club"), ("brown", "Brown", "Brown"), ("grey", "Grey", "Grey"),
                        ("black", "Black", "Black"), ("red", "Red", "Red-brown")]),
         {"Habitat": "Caves", "Signature": "Clubs and knocks back", "Danger": "High"})],
    "armoured": [
        (dict(id="daeodon_armoured", name="Daeodon", group="Plains and savannas", tag="Hungry · cheerful", height=1.9,
              biomes=["plains", "savanna", "sunflower_plains"], temperament="Hungry, cheerful",
              ecology="A Harmontide 'hell pig', a great shaggy pale-blue boar girl with a huge mouth, round ears and pink cheeks. She "
                      "roots through any soil for something to eat, and shares.",
              interaction="Potatoes, carrots and beetroots.",
              signature=("Rooting", "Befriended, now and then she roots up a potato, a carrot or a beetroot at her friend's feet (rarely a golden carrot)."),
              danger="Moderate (her bite).",
              variants=[("blue", "Blue (default)", "Pale blue"), ("brown", "Brown", "Brown"), ("grey", "Grey", "Grey"),
                        ("black", "Black", "Black"), ("ginger", "Ginger", "Ginger")]),
         {"Habitat": "Plains", "Signature": "Roots up food", "Danger": "Moderate"}),
        (dict(id="anteater_armoured", name="Anteater", group="Savannas and jungles", tag="Slow · tidy", height=1.6,
              biomes=["savanna", "jungle", "sparse_jungle"], temperament="Slow, tidy",
              ecology="A Harmontide anteater girl in soft blue, a long tubular snout with a pink tongue always peeking out, round ears "
                      "and a great bushy tail. Nothing small escapes her tongue.",
              interaction="Sugar, honey and sweet berries.",
              signature=("Long tongue", "Befriended, she licks up the loose items near her friend and brings them to their feet."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue, a cream face"), ("grey", "Grey", "Grey, a black stripe"),
                        ("tamandua", "Tamandua", "Gold with a black vest"), ("silky", "Silky", "Silky gold"), ("brown", "Brown", "Brown")]),
         {"Habitat": "Savannas", "Signature": "Gathers loose items", "Danger": "None"})],
    "minotaur": [
        (dict(id="oryx_minotaur", name="Oryx", group="Deserts and badlands", tag="Calm · enduring", height=2.0,
              biomes=["desert", "badlands", "savanna_plateau"], temperament="Calm, enduring",
              ecology="A Harmontide oryx girl, pale blue with a cream crescent moon on her brow, two long horns swept back and a tufted "
                      "tail. She crosses the desert for days on a single drink.",
              interaction="Wheat and cactus fruit (sweet berries).",
              signature=("Desert stride", "Befriended, her friend near her in the heat of deserts, badlands and savannas doesn't tire (no hunger from exertion)."),
              danger="Moderate (her horns).",
              variants=[("blue", "Blue (default)", "Pale blue, a cream crescent"), ("white", "White", "Arabian white"),
                        ("gemsbok", "Gemsbok", "Grey with black marks"), ("scimitar", "Scimitar", "White and chestnut"), ("sand", "Sand", "Sand")]),
         {"Habitat": "Deserts", "Signature": "No hunger in the heat", "Danger": "Moderate"})],
    "nekomata": [
        (dict(id="caracal_nekomata", name="Caracal", group="Savannas and scrub", tag="Quick · proud", height=1.6,
              biomes=["savanna", "desert", "badlands"], temperament="Quick, proud",
              ecology="A Harmontide caracal girl, blue-grey with long black ear tufts and a pale chest. Famous for leaping straight up "
                      "to swat birds from the air.",
              interaction="Chicken and rabbit.",
              signature=("Bird catcher", "Befriended, she leaps at a flying monster near her friend (a phantom, a vex, a blaze, a ghast)."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Blue-grey, black tufts"), ("sand", "Sand", "Sandy"), ("red", "Red", "Red"),
                        ("grey", "Grey", "Grey"), ("black", "Black", "Black")]),
         {"Habitat": "Savannas", "Signature": "Strikes flying monsters", "Danger": "Low"}),
        (dict(id="rakshasa_nekomata", name="Rakshasa", group="Jungles at night", tag="Cunning · courtly", height=1.9,
              biomes=["jungle", "bamboo_jungle", "savanna"], temperament="Cunning, courtly",
              ecology="The shape-shifting tiger demon of Hindu legend, striped orange and black, long fangs, a robe and gold jewellery. "
                      "She weaves glamours that turn eyes aside.",
              interaction="Gold nuggets and cooked meat.",
              signature=("Glamour", "Befriended, the monsters hunting her friend lose sight of them."),
              danger="Moderate.",
              variants=[("orange", "Orange (default)", "Orange stripes, a purple robe"), ("blue", "Blue", "Blue"), ("white", "White", "White"),
                        ("black", "Black", "Black"), ("crimson", "Crimson", "Crimson")]),
         {"Habitat": "Jungles", "Signature": "Monsters lose their target", "Danger": "Moderate"})],
    "merfolk": [
        (dict(id="walrus_mermaid", name="Walrus", group="Frozen oceans and shores", tag="Lazy · warm", height=1.8,
              biomes=["frozen_ocean", "cold_ocean", "snowy_beach"], temperament="Lazy, warm",
              ecology="A Harmontide walrus girl, round and blue, long pale hair, two white tusks and a broad flipper tail. Her blubber "
                      "keeps anyone who cuddles up to her warm.",
              interaction="Fish and kelp.",
              signature=("Blubber", "Befriended, her friend near her never freezes."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Blue, white tusks"), ("brown", "Brown", "Brown"), ("pink", "Pink", "Sun-pink"),
                        ("grey", "Grey", "Grey"), ("white", "White", "White")]),
         {"Habitat": "Frozen oceans", "Signature": "Friend never freezes", "Danger": "Low"}),
        (dict(id="anomalocaris_mermaid", name="Anomalocaris", group="Deep oceans", tag="Curious · ancient", height=1.7,
              biomes=["deep_ocean", "deep_lukewarm_ocean", "ocean"], temperament="Curious, ancient",
              ecology="A Harmontide anomalocaris, a pale blue swimmer from the oldest seas: big round eyes, two spiny grasping arms curling "
                      "from her brow, flaps along her sides and a fan of a tail. She was the first great hunter of the seas.",
              interaction="Fish and kelp.",
              signature=("Raptor grasp", "Befriended, she seizes a monster swimming near her friend, drags it to her and bites."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Pale blue"), ("red", "Red", "Red"), ("purple", "Purple", "Purple"),
                        ("green", "Green", "Green"), ("gold", "Gold", "Gold")]),
         {"Habitat": "Deep oceans", "Signature": "Grapples swimming monsters", "Danger": "Low"})],
    "lamia": [
        (dict(id="serpopard_lamia", name="Serpopard", group="Deserts and river banks", tag="Watchful · fierce", height=2.1,
              biomes=["desert", "river", "savanna"], temperament="Watchful, fierce",
              ecology="The serpent-leopard of ancient Egyptian palettes, a long-necked spotted serpent with a leopard's ears and face. "
                      "Pairs of them guarded order against chaos.",
              interaction="Rabbit and chicken.",
              signature=("Venom bite", "Befriended, she bites a monster near her friend: Poison."),
              danger="Moderate (her venom).",
              variants=[("gold", "Gold (default)", "Gold with rosettes"), ("blue", "Blue", "Blue"), ("snow", "Snow", "Snow"),
                        ("black", "Black", "Black"), ("sand", "Sand", "Sand")]),
         {"Habitat": "Deserts", "Signature": "Poisons monsters", "Danger": "Moderate"})],
    "centaur": [
        (dict(id="urcuchillay_centaur", name="Urcuchillay", group="Mountain meadows", tag="Kind · watchful", height=2.2,
              biomes=["meadow", "windswept_hills", "grove"], temperament="Kind, watchful",
              ecology="The Inca god who watches over the herds, a llama of many colours: rainbow hair and fleece, a blue dress and a "
                      "headband with a blue stone. Animals grow strong in her care.",
              interaction="Wheat and hay.",
              signature=("Herd keeper", "Befriended, the young animals near her friend grow up faster."),
              danger="None.",
              variants=[("rainbow", "Rainbow (default)", "Rainbow hair and fleece"), ("white", "White", "White"), ("brown", "Brown", "Brown"),
                        ("grey", "Grey", "Grey"), ("black", "Black", "Black")]),
         {"Habitat": "Meadows", "Signature": "Young animals grow", "Danger": "None"})],
    "undead": [
        (dict(id="gashadokuro_undead", name="Gashadokuro", group="Old battlefields at night", tag="Hungry · lonely", height=2.6,
              biomes=["plains", "dark_forest", "taiga"], temperament="Hungry, lonely",
              ecology="The 'starving skeleton' of Japanese folklore, a towering skeleton of the bones of the unburied dead, dark hollows "
                      "round her glowing eyes, her teeth chattering. She seizes whatever crosses her path.",
              interaction="Bones and bread.",
              signature=("Giant's grasp", "Befriended, she seizes a monster near her friend and lifts it into the air (Levitation)."),
              danger="High.",
              variants=[("bone", "Bone (default)", "Bone white, red eyes"), ("ash", "Ash", "Ash grey"), ("black", "Black", "Black bones"),
                        ("blood", "Blood", "Red-stained"), ("jade", "Jade", "Ghostly jade")]),
         {"Habitat": "Battlefields", "Signature": "Lifts monsters", "Danger": "High"})],
}

for _fid, _entries in TWENTY_FIRST.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# twenty-second wave (2026-10-29): eight Harmontide creatures from the reference art (their blue looks) and four creatures of
# folklore (Xiangliu, the cyclops, the genie and Ammit)
TWENTY_SECOND = {
    "dragon": [
        (dict(id="macawtl_dragon", name="Macawtl", group="Jungles", tag="Proud · bright", height=1.9,
              biomes=["jungle", "bamboo_jungle", "sparse_jungle"], temperament="Proud, bright",
              ecology="A Harmontide feathered serpent in deep macaw blue: a crest of plumes, a hooked beak of a mask, diamond-patterned feathers and a long tail of plumes hung with silver beads. She rides the winds above the canopy.",
              interaction="Melon, cocoa beans and seeds.",
              signature=("Tailwind", "Befriended, out under the open sky her friend near her runs faster (Speed)."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Deep blue, silver beads"), ("scarlet", "Scarlet", "Scarlet macaw red"), ("green", "Green", "Military macaw green"), ("gold", "Gold", "Blue and gold"), ("hyacinth", "Hyacinth", "Violet blue")]),
         {"Habitat": "Jungles", "Signature": "Speed under the sky", "Danger": "Low"})],
    "merfolk": [
        (dict(id="piranha_mermaid", name="Piranha", group="Jungle rivers", tag="Hungry · fierce", height=1.5,
              biomes=["river", "jungle", "mangrove_swamp", "swamp"], temperament="Hungry, fierce",
              ecology="A round little Harmontide piranha girl, blue and spotted, with a jutting jaw full of teeth and a lure of a curl on her head. Where there is blood in the water, she is there first.",
              interaction="Any raw meat or fish.",
              signature=("Frenzy", "Befriended, in water she bites every monster near her friend that is already badly hurt."),
              danger="Moderate (her teeth).",
              variants=[("blue", "Blue (default)", "Blue, darker spots"), ("red", "Red", "Red-bellied"), ("silver", "Silver", "Silver"), ("black", "Black", "Black"), ("gold", "Gold", "Gold")]),
         {"Habitat": "Jungle rivers", "Signature": "Bites hurt monsters", "Danger": "Moderate"}),
        (dict(id="mosasaurus_mermaid", name="Mosasaurus", group="Deep oceans", tag="Huge · playful", height=2.4,
              biomes=["deep_ocean", "deep_cold_ocean", "deep_lukewarm_ocean", "ocean"], temperament="Huge, playful",
              ecology="The great sea lizard of the Cretaceous as a Harmontide mermaid: grey-blue scales, a long toothy jaw, flippers for hands and a crescent fluke. She loves to push boats along.",
              interaction="Fish and kelp.",
              signature=("Tow", "Befriended, her friend's boat near her surges forward as she tows it."),
              danger="High (if provoked).",
              variants=[("blue", "Blue (default)", "Grey-blue"), ("green", "Green", "Sea green"), ("grey", "Grey", "Grey"), ("black", "Black", "Black"), ("white", "White", "White")]),
         {"Habitat": "Deep oceans", "Signature": "Tows boats", "Danger": "High"}),
        (dict(id="fishman_mermaid", name="Fishman", group="Oceans and rivers", tag="Grumpy · brave", height=1.6,
              biomes=["ocean", "lukewarm_ocean", "river", "beach"], temperament="Grumpy, brave",
              ecology="A light blue Harmontide fish girl with a fin of a ponytail, fins at her elbows, freckle spots and sharp teeth always ready to scowl. She cannot stand the drowned.",
              interaction="Fish and kelp.",
              signature=("Disarm", "Befriended, she wrenches the trident from a drowned near her friend."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Light blue"), ("green", "Green", "Green"), ("pink", "Pink", "Pink"), ("grey", "Grey", "Grey"), ("gold", "Gold", "Gold")]),
         {"Habitat": "Oceans, rivers", "Signature": "Disarms drowned", "Danger": "Low"})],
    "rodent": [
        (dict(id="gibbon_rodent", name="Gibbon", group="Jungles", tag="Cheerful · agile", height=1.5,
              biomes=["jungle", "bamboo_jungle", "sparse_jungle"], temperament="Cheerful, agile",
              ecology="A fluffy Harmontide gibbon girl, pale blue-grey round a cream face, green eyes, long arms and a curling tail. She swings through the canopy faster than anyone walks.",
              interaction="Apples, melon and sweet berries.",
              signature=("Brachiation", "Befriended, her friend near her climbs ladders, vines and scaffolding twice as fast."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Pale blue-grey"), ("white", "White", "White-handed"), ("black", "Black", "Black"), ("gold", "Gold", "Golden"), ("brown", "Brown", "Brown")]),
         {"Habitat": "Jungles", "Signature": "Faster climbing", "Danger": "None"})],
    "canid": [
        (dict(id="gigantopithecus_canid", name="Gigantopithecus", group="Bamboo forests", tag="Mighty · gentle", height=2.3,
              biomes=["bamboo_jungle", "jungle", "old_growth_pine_taiga"], temperament="Mighty, gentle",
              ecology="The giant ape of ancient China as a Harmontide girl: huge and shaggy, deep blue fur, a red flower in her hair, a fur top and a stone belt. Gentle until her friend is cornered.",
              interaction="Bamboo, apples and melon.",
              signature=("Ground pound", "Befriended, when monsters crowd her friend she slams the ground: they are all knocked back and slowed."),
              danger="High (if provoked).",
              variants=[("blue", "Blue (default)", "Deep blue, a red flower"), ("brown", "Brown", "Brown"), ("black", "Black", "Black"), ("grey", "Grey", "Silver-grey"), ("red", "Red", "Red-brown")]),
         {"Habitat": "Bamboo forests", "Signature": "Ground pound", "Danger": "High"})],
    "lamia": [
        (dict(id="sandworm_lamia", name="Sandworm", group="Deserts", tag="Hidden · hungry", height=2.0,
              biomes=["desert", "badlands", "beach"], temperament="Hidden, hungry",
              ecology="A Harmontide sandworm girl, blue and segmented with a cream belly and a scaly hood over her head. She swims through sand as fish swim through water.",
              interaction="Rotten flesh and spider eyes.",
              signature=("Swallow", "Befriended, she swallows a monster near her friend and spits it out far away."),
              danger="Moderate.",
              variants=[("blue", "Blue (default)", "Blue, a cream belly"), ("sand", "Sand", "Sand"), ("red", "Red", "Red sand"), ("grey", "Grey", "Grey"), ("black", "Black", "Black")]),
         {"Habitat": "Deserts", "Signature": "Spits monsters away", "Danger": "Moderate"}),
        (dict(id="xiangliu_lamia", name="Xiangliu", group="Swamps", tag="Chatty · venomous", height=2.0,
              biomes=["swamp", "mangrove_swamp", "river"], temperament="Chatty, venomous",
              ecology="The nine-headed serpent of Chinese myth, whose venom turned the land to swamp: a teal serpent girl with little heads of her own gathered round her head, all of them talking at once.",
              interaction="Fish and spider eyes.",
              signature=("Nine heads", "Befriended, her heads bite up to three monsters near her friend at once."),
              danger="High.",
              variants=[("teal", "Teal (default)", "Teal"), ("green", "Green", "Swamp green"), ("black", "Black", "Black"), ("gold", "Gold", "Gold"), ("red", "Red", "Red")]),
         {"Habitat": "Swamps", "Signature": "Bites three at once", "Danger": "High"})],
    "lizardfolk": [
        (dict(id="archelon_lizardfolk", name="Archelon", group="Warm oceans and beaches", tag="Steady · protective", height=1.9,
              biomes=["warm_ocean", "lukewarm_ocean", "beach"], temperament="Steady, protective",
              ecology="The greatest sea turtle that ever lived as a Harmontide girl: crystal-blue scales and a great ridged shell like a shield, flippers at her wrists.",
              interaction="Seagrass, kelp and fish.",
              signature=("Shell shield", "Befriended, she knocks away the arrows flying at her friend near her."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Crystal blue"), ("green", "Green", "Green"), ("brown", "Brown", "Brown"), ("black", "Black", "Black"), ("white", "White", "White")]),
         {"Habitat": "Warm oceans", "Signature": "Knocks arrows away", "Danger": "None"})],
    "devil": [
        (dict(id="cyclops_devil", name="Cyclops", group="Mountains", tag="Shy · strong", height=2.2,
              biomes=["windswept_hills", "stony_peaks", "meadow"], temperament="Shy, strong",
              ecology="The one-eyed giants of Greek myth, smiths and shepherds: a big shy girl with a single great eye, pointed ears and a blue bob. She throws rocks as far as others throw pebbles.",
              interaction="Mutton, milk and bread.",
              signature=("Boulder", "Befriended, she hurls a boulder at a monster far from her friend."),
              danger="High (her boulders).",
              variants=[("blue", "Blue (default)", "Blue hair, a teal tunic"), ("brown", "Brown", "Brown"), ("red", "Red", "Red"), ("grey", "Grey", "Grey"), ("green", "Green", "Green")]),
         {"Habitat": "Mountains", "Signature": "Hurls boulders", "Danger": "High"}),
        (dict(id="genie_devil", name="Genie", group="Deserts", tag="Playful · generous", height=1.8,
              biomes=["desert", "badlands"], temperament="Playful, generous",
              ecology="A spirit of smoke and fire from Arabian tales, bound to a brass lamp: a smoke tail for legs, a high ponytail in a gold band, a bandeau and bangles.",
              interaction="Gold nuggets and sweet berries.",
              signature=("Three wishes", "Befriended, three times a day (dawn, noon and dusk) she grants her friend a wish: a random boon for five minutes."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Blue smoke, gold"), ("purple", "Purple", "Purple"), ("green", "Green", "Green"), ("red", "Red", "Red"), ("gold", "Gold", "Gold")]),
         {"Habitat": "Deserts", "Signature": "Three wishes a day", "Danger": "Low"})],
    "nekomata": [
        (dict(id="ammit_nekomata", name="Ammit", group="Deserts by the river", tag="Stern · fair", height=1.8,
              biomes=["desert", "river", "savanna"], temperament="Stern, fair",
              ecology="The devourer of the dead of Egyptian myth, who ate the hearts that failed the weighing: a lion's mane and a lion's coat over all of her, face included, a crocodile's snout, a tufted tail.",
              interaction="Raw meat and fish.",
              signature=("Devourer", "Befriended, when a monster dies near her friend she eats its heart and her friend heals."),
              danger="Moderate.",
              variants=[("blue", "Blue (default)", "Pale blue"), ("sand", "Sand", "Sand"), ("black", "Black", "Black"), ("gold", "Gold", "Gold"), ("green", "Green", "Green")]),
         {"Habitat": "Deserts", "Signature": "Heals on monster deaths", "Danger": "Moderate"})]
}

for _fid, _entries in TWENTY_SECOND.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# twenty-third wave (2026-10-30): eight Harmontide creatures from the reference art (their blue looks; the spewslug's only look is
# red) and three of folklore (Scylla, the taotie, the vetala) and Sun Wukong; the ironbeak is a harpy (her own class)
TWENTY_THIRD = {
    "construct": [
        (dict(id="automaton_construct", name="Automaton", group="Villages and ruins", tag="Dutiful · curious", height=1.7,
              biomes=["plains", "meadow", "savanna"], temperament="Dutiful, curious",
              ecology="A Harmontide clockwork girl in pale-blue armour plates: a round helmet with a disc over each ear, rivets down every plate and a wind-up key she never lets anyone turn but a friend. Nobody remembers who built the first of them.",
              interaction="Redstone dust and copper ingots.",
              signature=("Lamplighter", "Befriended, she sets torches (her friend's own) on the dark ground near her friend."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Pale-blue plates"), ("silver", "Silver", "Silver"), ("red", "Red", "Red and gold"), ("green", "Green", "Green"), ("gold", "Gold", "Gold")]),
         {"Habitat": "Villages and ruins", "Signature": "Lights the dark", "Danger": "Low"})],
    "mollusk": [
        (dict(id="spewslug_mollusk", name="Spewslug", group="Swamps and caves", tag="Gross · loyal", height=1.6,
              biomes=["swamp", "mangrove_swamp", "dripstone_caves", "lush_caves"], temperament="Gross, loyal",
              ecology="A glistening crimson Harmontide slug girl: eye-stalk antennae, a slug's mantle humped over her back and a wide maw full of teeth, purple bile always dribbling from it. She means well.",
              interaction="Mushrooms, rotten flesh and spider eyes.",
              signature=("Bile", "Befriended, she spews bile over a monster that hits her friend: it is weakened and starving."),
              danger="Moderate (her bile).",
              variants=[("red", "Red (default)", "Crimson, purple bile"), ("orange", "Orange", "Orange, green bile"), ("black", "Black", "Black, teal bile"), ("yellow", "Yellow", "Yellow, pink bile"), ("blue", "Blue", "Blue, yellow bile")]),
         {"Habitat": "Swamps and caves", "Signature": "Weakening bile", "Danger": "Moderate"}),
        (dict(id="scylla_mollusk", name="Scylla", group="Rocky coasts", tag="Fierce · watchful", height=2.0,
              biomes=["stony_shore", "ocean", "deep_ocean"], temperament="Fierce, watchful",
              ecology="The sea monster of the Odyssey who waited across the strait from Charybdis: a sea-green girl over a skirt of tentacles, with six dog heads snarling out from her waist. They snatch whatever sails too close.",
              interaction="Fish and raw meat.",
              signature=("Six hounds", "Befriended, her dog heads tear the armour and the weapons off the monsters near her friend."),
              danger="High.",
              variants=[("teal", "Teal (default)", "Sea green, grey hounds"), ("blue", "Blue", "Blue"), ("purple", "Purple", "Purple"), ("red", "Red", "Red"), ("pale", "Pale", "Pale")]),
         {"Habitat": "Rocky coasts", "Signature": "Strips monsters", "Danger": "High"})],
    "devil": [
        (dict(id="ogre_devil", name="Ogre", group="Forests and hills", tag="Big · good-natured", height=2.3,
              biomes=["forest", "dark_forest", "windswept_forest"], temperament="Big, good-natured",
              ecology="A big green Harmontide ogre girl with long pointed ears, two little horns and a mop of light-blue hair, in a simple blue dress. She carries a club, and she is gentle with everything except whatever threatens her friends.",
              interaction="Mutton, potatoes and bread.",
              signature=("Bonk", "Befriended, her club stuns a monster that hits her friend: it can hardly move for three seconds."),
              danger="High (her club).",
              variants=[("green", "Green (default)", "Green skin, blue hair"), ("grey", "Grey", "Grey"), ("brown", "Brown", "Brown"), ("blue", "Blue", "Blue skin"), ("red", "Red", "Red skin")]),
         {"Habitat": "Forests and hills", "Signature": "Stuns attackers", "Danger": "High"}),
        (dict(id="taotie_devil", name="Taotie", group="Badlands", tag="Greedy · cheerful", height=1.9,
              biomes=["badlands", "wooded_badlands", "savanna_plateau"], temperament="Greedy, cheerful",
              ecology="The glutton of Chinese myth, whose face is cast on the ancient bronzes: curled horns, bronze meanders running over her arms and legs and a huge grin. She will eat anything, and she is always hungry.",
              interaction="Any food at all.",
              signature=("Gluttony", "Befriended, she eats the junk the monsters drop near her friend (rotten flesh, bones, string, spider eyes) and turns it into experience for them."),
              danger="Moderate.",
              variants=[("teal", "Teal (default)", "Teal, jade glyphs"), ("bronze", "Bronze", "Bronze"), ("red", "Red", "Red"), ("black", "Black", "Black"), ("gold", "Gold", "Gold")]),
         {"Habitat": "Badlands", "Signature": "Junk into experience", "Danger": "Moderate"})],
    "canid": [
        (dict(id="hellhound_canid", name="Hellhound", group="The Nether", tag="Fierce · devoted", height=1.8,
              biomes=["soul_sand_valley", "basalt_deltas", "nether_wastes"], temperament="Fierce, devoted",
              ecology="A hound of the underworld: charcoal skin, heavy black fur on her arms and legs, spiky dark-red fur at the shoulders, wrists and ankles, flames streaming from her eyes, great claws, a skull at her collar and ember cracks that glow. She grins with every tooth. She never forgets a scent.",
              interaction="Raw meat and bones.",
              signature=("Hunted down", "Befriended, she runs down whatever hurt her friend, wherever it is within 24 blocks, and sets it on fire."),
              danger="High.",
              variants=[("ember", "Ember (default)", "Charcoal, red-orange flames"), ("black", "Black", "Near black, amber flames"), ("blue", "Blue", "Charcoal, blue flames"), ("green", "Green", "Charcoal, green flames"), ("purple", "Purple", "Charcoal, purple flames")]),
         {"Habitat": "The Nether", "Signature": "Avenges her friend", "Danger": "High"})],
    "lizardfolk": [
        (dict(id="argentinosaurus_lizardfolk", name="Argentinosaurus", group="Savannas and plains", tag="Gentle · tall", height=2.8,
              biomes=["savanna", "plains", "sunflower_plains"], temperament="Gentle, tall",
              ecology="The greatest of the long-necked dinosaurs as a Harmontide girl: pale blue with orange stripes and a cream belly, a long neck that lifts her head far above everyone else's and a long heavy tail.",
              interaction="Leaves, wheat and apples.",
              signature=("Lookout", "Befriended, from up on her long neck she spots the monsters 24 to 40 blocks from her friend, and they glow."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Pale blue, orange stripes"), ("green", "Green", "Green"), ("sand", "Sand", "Sand"), ("grey", "Grey", "Grey"), ("pink", "Pink", "Pink")]),
         {"Habitat": "Savannas", "Signature": "Spots far monsters", "Danger": "None"})],
    "lamia": [
        (dict(id="titanoboa_lamia", name="Titanoboa", group="Jungles and swamps", tag="Huge · lazy", height=3.9,
              biomes=["jungle", "swamp", "mangrove_swamp"], temperament="Huge, lazy",
              ecology="The greatest snake that ever lived: twice the size of any boa, olive-brown with dark blotches and a cream belly. She sleeps for days after a meal, coiled in the warm shallows.",
              interaction="Raw meat and fish.",
              signature=("Constrict", "Befriended, she coils round the biggest monster near her friend, holding it fast and squeezing it for five seconds."),
              danger="High.",
              variants=[("olive", "Olive (default)", "Olive-brown blotches"), ("green", "Green", "Green"), ("black", "Black", "Black"), ("red", "Red", "Red"), ("blue", "Blue", "Blue")]),
         {"Habitat": "Jungles", "Signature": "Holds monsters fast", "Danger": "High"})],
    "rodent": [
        (dict(id="sasquatch_rodent", name="Sasquatch", group="Old forests", tag="Shy · gentle", height=2.2,
              biomes=["old_growth_spruce_taiga", "old_growth_pine_taiga", "taiga"], temperament="Shy, gentle",
              ecology="A big fluffy Harmontide sasquatch girl, pale blue all over, with a shaggy mane falling round a pink face. Everyone has heard of her, and hardly anyone has seen her: she hides very well.",
              interaction="Berries, salmon and honey.",
              signature=("Unseen", "Befriended, her friend near her turns invisible while sneaking in a forest."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Pale blue"), ("brown", "Brown", "Brown"), ("white", "White", "White"), ("red", "Red", "Red"), ("black", "Black", "Black")]),
         {"Habitat": "Old forests", "Signature": "Invisible in forests", "Danger": "Low"}),
        (dict(id="sun_wukong_rodent", name="Sun Wukong", group="Mountains", tag="Mischievous · bold", height=1.7,
              biomes=["jagged_peaks", "cherry_grove", "stony_peaks"], temperament="Mischievous, bold",
              ecology="The Monkey King of Journey to the West, born from a stone and the terror of Heaven: golden fur, a gold circlet on her brow, red and gold armour, a long curling tail and her staff. She rides a cloud that crosses the world in one somersault.",
              interaction="Peaches, apples and melon.",
              signature=("Somersault cloud", "Befriended, her friend near her takes no fall damage: her cloud catches them."),
              danger="High (her staff).",
              variants=[("gold", "Gold (default)", "Golden fur, red armour"), ("stone", "Stone", "Grey, blue armour"), ("white", "White", "White"), ("black", "Black", "Black, gold armour"), ("red", "Red", "Red fur")]),
         {"Habitat": "Mountains", "Signature": "No fall damage", "Danger": "High"})],
    "bat": [
        (dict(id="vetala_bat", name="Vetala", group="Old forests at night", tag="Eerie · clever", height=1.7,
              biomes=["dark_forest", "swamp", "old_growth_birch_forest"], temperament="Eerie, clever",
              ecology="The spirit of the Indian tales who hangs upside down from the trees by the burning grounds and rides the bodies of the dead, and who told a king twenty-five riddles: ash-pale, ragged, red-eyed, with tattered wings.",
              interaction="Rotten flesh and sweet berries.",
              signature=("Possession", "Befriended, she rides a zombie near her friend, and it fights for them for 30 seconds."),
              danger="Moderate.",
              variants=[("pale", "Pale (default)", "Ash-pale, white hair"), ("ash", "Ash", "Dark ash"), ("blue", "Blue", "Blue"), ("green", "Green", "Green"), ("red", "Red", "Red")]),
         {"Habitat": "Dark forests", "Signature": "Possesses zombies", "Danger": "Moderate"})],
}

for _fid, _entries in TWENTY_THIRD.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(x["id"] == _species["id"] for x in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# twenty-fourth wave (2026-10-30): six from the reference art (their blue looks) and the Quetzalcoatl on the rearing serpent's rig, the
# Leviathan on the fish body, the Girtablilu on the scorpion's; Cerberus, the jiangshi, the baku and the qilin of folklore; the ibiset
# and the stigowl are harpies (their own classes)
TWENTY_FOURTH = {
    "dragon": [
        (dict(id="quetzalcoatl_dragon", name="Quetzalcoatl", group="Jungles", tag="Gentle · radiant", height=1.9,
              biomes=["jungle", "sparse_jungle", "savanna"], temperament="Gentle, radiant",
              ecology="The feathered serpent of Mesoamerica: a teal snake from the bust down, a crest of green leaves and feathers round her head, a woven orange headband and pale wings. She flies at dawn.",
              interaction="Melon, cocoa beans and maize-yellow carrots.",
              signature=("Morning star", "Befriended, at dawn she ripens the crops near her friend."),
              danger="Low.",
              variants=[("teal", "Teal (default)", "Teal, orange band"), ("green", "Green", "Green"), ("gold", "Gold", "Gold"), ("rose", "Rose", "Rose"), ("violet", "Violet", "Violet")]),
         {"Habitat": "Jungles", "Signature": "Ripens crops at dawn", "Danger": "Low"})],
    "merfolk": [
        (dict(id="leviathan_mermaid", name="Leviathan", group="Deep oceans", tag="Vast · calm", height=3.0,
              biomes=["deep_ocean", "deep_cold_ocean", "deep_lukewarm_ocean"], temperament="Vast, calm",
              ecology="The sea monster of the Book of Job, as a great pale-blue fish with a girl's chest, curled horns and fin ears. The tide moves when she does.",
              interaction="Fish and prismarine crystals.",
              signature=("Tide call", "Befriended, her friend swims with Dolphin's Grace near her, and the tide drives monsters away from them."),
              danger="High.",
              variants=[("blue", "Blue (default)", "Pale blue"), ("deep", "Deep", "Deep blue"), ("green", "Green", "Sea green"), ("pearl", "Pearl", "Pearl white"), ("black", "Black", "Black")]),
         {"Habitat": "Deep oceans", "Signature": "Grace, drives monsters off", "Danger": "High"})],
    "bug": [
        (dict(id="girtablilu_bug", name="Girtablilu", group="Deserts", tag="Stern · loyal", height=1.8,
              biomes=["desert", "badlands"], temperament="Stern, loyal",
              ecology="The scorpion folk of Mesopotamia, who guard the gates of the sun: a veiled girl on a scorpion's legs, pincers in place of hands and a raised tail, spiral tattoos on her arms.",
              interaction="Spider eyes and gold.",
              signature=("Gate guard", "Befriended, at night she stings the monsters that come near her friend."),
              danger="Moderate (her sting).",
              variants=[("blue", "Blue (default)", "Blue"), ("sand", "Sand", "Sand"), ("black", "Black", "Black"), ("red", "Red", "Red"), ("green", "Green", "Green")]),
         {"Habitat": "Deserts", "Signature": "Stings at night", "Danger": "Moderate"})],
    "lizardfolk": [
        (dict(id="cipactli_lizardfolk", name="Cipactli", group="Rivers and swamps", tag="Hungry · playful", height=1.5,
              biomes=["swamp", "mangrove_swamp", "river"], temperament="Hungry, playful",
              ecology="The first monster of the Aztec creation, half crocodile: a small spiky teal girl with a cream belly, a toothy jaw and a heavy tail.",
              interaction="Raw fish and meat.",
              signature=("Pulled under", "Befriended, she snaps at monsters at the water's edge near her friend and drags them in."),
              danger="Moderate.",
              variants=[("teal", "Teal (default)", "Teal"), ("green", "Green", "Green"), ("olive", "Olive", "Olive"), ("red", "Red", "Red"), ("black", "Black", "Black")]),
         {"Habitat": "Rivers and swamps", "Signature": "Drags monsters under", "Danger": "Moderate"})],
    "devil": [
        (dict(id="behemoth_devil", name="Behemoth", group="Mountains", tag="Immense · steady", height=3.0,
              biomes=["windswept_hills", "stony_peaks", "jagged_peaks"], temperament="Immense, steady",
              ecology="The land beast of the Book of Job, the Leviathan's match: huge and dark blue, great horns, a wild mane and a thick tail.",
              interaction="Hay bales and any meat.",
              signature=("Immovable", "Befriended, her friend cannot be knocked back near her."),
              danger="High.",
              variants=[("blue", "Blue (default)", "Dark blue"), ("grey", "Grey", "Grey"), ("brown", "Brown", "Brown"), ("green", "Green", "Green"), ("red", "Red", "Red")]),
         {"Habitat": "Mountains", "Signature": "No knockback", "Danger": "High"})],
    "undead": [
        (dict(id="nasnas_undead", name="Nasnas", group="Deserts by night", tag="Quiet · faithful", height=1.6,
              biomes=["desert", "badlands", "savanna"], temperament="Quiet, faithful",
              ecology="The half-creature of Arabian tales, here half a ghost: pale, eyes closed, her body fading into a wisp where her legs should be.",
              interaction="Dates of any kind: sweet berries and bread.",
              signature=("Half a life", "Befriended, once a day she takes a killing blow in her friend's place."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Pale blue"), ("grey", "Grey", "Grey"), ("gold", "Gold", "Gold"), ("rose", "Rose", "Rose"), ("green", "Green", "Green")]),
         {"Habitat": "Deserts by night", "Signature": "Takes a killing blow", "Danger": "Low"}),
        (dict(id="jiangshi_undead", name="Jiangshi", group="Bamboo and old forests", tag="Stiff · dutiful", height=1.7,
              biomes=["bamboo_jungle", "dark_forest", "old_growth_pine_taiga"], temperament="Stiff, dutiful",
              ecology="The hopping corpse of China: an official's robe and hat, pale skin, a paper talisman on her brow, arms stretched out in front of her. She cannot bend, so she hops.",
              interaction="Sticky rice: bread and sweet berries.",
              signature=("Stiff hop", "Befriended, she hops at the monsters that come near her and stuns them."),
              danger="Moderate.",
              variants=[("indigo", "Indigo (default)", "Indigo robe"), ("red", "Red", "Red robe"), ("green", "Green", "Green robe"), ("black", "Black", "Black robe"), ("gold", "Gold", "Gold robe")]),
         {"Habitat": "Bamboo and old forests", "Signature": "Stuns monsters", "Danger": "Moderate"})],
    "canid": [
        (dict(id="cerberus_canid", name="Cerberus", group="The Nether", tag="Watchful · three times as loyal", height=1.8,
              biomes=["soul_sand_valley", "crimson_forest"], temperament="Watchful, loyal",
              ecology="The hound at the gates of the underworld: a dark-furred girl with two more little heads on her shoulders, each with its own idea about you.",
              interaction="Raw meat and honey cakes: honey bottles.",
              signature=("Three watches", "Befriended, nothing sneaks up on her friend: monsters coming from behind them are slowed and creepers lose their nerve."),
              danger="High.",
              variants=[("black", "Black (default)", "Black, red eyes"), ("brown", "Brown", "Brown"), ("grey", "Grey", "Grey"), ("white", "White", "White"), ("red", "Red", "Red")]),
         {"Habitat": "The Nether", "Signature": "Guards her friend's back", "Danger": "High"})],
    "nekomata": [
        (dict(id="baku_nekomata", name="Baku", group="Meadows at night", tag="Sleepy · kind", height=1.6,
              biomes=["meadow", "flower_forest", "cherry_grove", "end_midlands"], temperament="Sleepy, kind",
              ecology="The dream-eater of Japan: a tapir's short trunk, round ears and a two-tone coat. She eats nightmares and is always a little full.",
              interaction="Sweet berries and glow berries.",
              signature=("Dream eater", "Befriended, she eats her friend's nightmares: they never grow tired enough for phantoms to come."),
              danger="Low.",
              variants=[("black", "Black (default)", "Black and white"), ("lilac", "Lilac", "Lilac"), ("blue", "Blue", "Blue"), ("rose", "Rose", "Rose"), ("grey", "Grey", "Grey")]),
         {"Habitat": "Meadows", "Signature": "No phantoms", "Danger": "Low"})],
    "centaur": [
        (dict(id="qilin_centaur", name="Qilin", group="Bamboo and meadows", tag="Gentle · auspicious", height=2.0,
              biomes=["bamboo_jungle", "meadow", "cherry_grove"], temperament="Gentle, auspicious",
              ecology="The auspicious beast of China: a scaled deer who treads so lightly she bends no grass, with soft antlers and a golden coat.",
              interaction="Apples and golden carrots.",
              signature=("Gentle tread", "Befriended, she heals the animals near her friend, and she never tramples farmland."),
              danger="Low.",
              variants=[("gold", "Gold (default)", "Gold, teal mane"), ("jade", "Jade", "Jade"), ("white", "White", "White"), ("red", "Red", "Red"), ("blue", "Blue", "Blue")]),
         {"Habitat": "Bamboo and meadows", "Signature": "Heals animals", "Danger": "Low"})],
}

for _fid, _entries in TWENTY_FOURTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(s["id"] == _species["id"] for s in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# theropods (2026-10-29): a family of their own, built like a 1920s museum sketch of a T. rex but human: hunched forward over a
# long balancing tail, human legs, arms, skin and face, and a sharp-toothed grin. The fernstalker moved here from the lizardfolk.
_family(id="theropod", volume="XXVII", title="Theropods", subtitle="the raptor family", food="Raw meat (and eggs)",
        theme=dict(bg="#10140f", panel="#191f17", panel2="#20281d", ink="#eef2e6", muted="#b6c2a8", accent="#d86a3a",
                   line="#34402c", green="#8ac85a", grad="#2a3a1a"),
        lede="Hunters who walk like the old museum sketches of a T. rex: leaning forward over a long tail held out for balance, "
             "with a grin full of little sharp teeth. Human in every other way.",
        hero=dict(eyebrow="The central similarity", title="Lean in, tail out.",
                  text="A theropod girl stands hunched forward, her heavy tail held level behind her to balance. Under it she is human: "
                       "human legs, arms, skin and face. Each kind keeps a little of her dinosaur: a sail, claws, feathers.",
                  bullets=[("Posture", "Hunched forward, tail out behind for balance."), ("Teeth", "A grin full of small pointed teeth."),
                           ("Meat", "The family food: raw meat, and eggs."), ("Cousins", "Like the lizardfolk they bask and lay leathery eggs.")]),
        lifecycle=[("1. Courtship", "At high friendship she may share your bed."), ("2. Eggs", "She lays a leathery clutch of two to four eggs."),
                   ("3. Hatching", "After a day the hatchlings break out, each a random colour of her species."), ("4. Grown", "Grown in five days.")],
        systems=[("Cold-blooded", "Like the lizardfolk: quick and healing in sunlight or by a fire, slow in the cold."),
                 ("Hunters", "Each of them helps her friend hunt in her own way: an ambush, a pack hunt, the rivers.")],
        friendship=dict(builds=["Raw meat (+1), eggs (+1)", "A warm spot she can bask by"], costs=["Hitting her"],
                        devoted="She hunts at your side."),
        mating=dict(form="A leathery clutch", young="2-4 hatchlings", grows="5 days", where="Warm sand or dirt",
                    note="Hatchlings use the adult model at 45%."),
        split=[("Habitat", []), ("Signature", []), ("Danger", [])], species=[],
        decisions=["Built as their own family on 2026-10-29: the fernstalker moved over from the lizardfolk, the spinosaur and the "
                   "australovenator came with it (planned for the twenty-second wave)."],
        questions=["More theropods: a T. rex, a dilophosaurus, a velociraptor?"])

THEROPODS = [
        (dict(id="fernstalker_theropod", name="Fernstalker", group="Jungles and lush forests", tag="Patient · deadly", height=1.9,
              biomes=["jungle", "sparse_jungle", "old_growth_spruce_taiga", "lush_caves"], temperament="Patient, deadly",
              ecology="A navy Harmontide raptor, hunched forward over her long tail, feathered white at the breast, with fern fronds fanned up from her back. She waits among "
                      "the ferns without a sound, then pounces.",
              interaction="Raw chicken, mutton and rabbit.",
              signature=("Ambush", "Befriended, every few seconds she pounces on a monster near her friend."),
              danger="High (her claws).",
              variants=[("navy", "Navy (default)", "Navy, a white breast"), ("green", "Green", "Green"), ("rust", "Rust", "Rust red"),
                        ("ash", "Ash", "Ash grey"), ("violet", "Violet", "Violet")]),
         {"Habitat": "Jungles", "Signature": "Pounces on monsters", "Danger": "High"}),
        (dict(id="spinosaur_theropod", name="Spinosaur", group="Rivers and swamps", tag="Bold · watery", height=2.0,
              biomes=["river", "swamp", "mangrove_swamp", "jungle"], temperament="Bold, playful in water",
              ecology="A teal Harmontide spinosaur, the river hunter of the Cretaceous: a low ribbed sail down her spine and along her "
                      "tail, a short crocodile's snout, a tall paddle of a tail. She wades the rivers for fish.",
              interaction="Raw fish and meat.",
              signature=("River wader", "Befriended, her friend near her in water swims like a dolphin (Dolphin's Grace)."),
              danger="Moderate.",
              variants=[("teal", "Teal (default)", "Teal, a teal sail"), ("green", "Green", "Swamp green"), ("sand", "Sand", "Sandy, a red sail"),
                        ("black", "Black", "Black, a blue sail"), ("red", "Red", "Rust red")]),
         {"Habitat": "Rivers, swamps", "Signature": "Dolphin's Grace in water", "Danger": "Moderate"}),
        (dict(id="australovenator_theropod", name="Australovenator", group="Savannas and badlands", tag="Quick · loyal", height=1.8,
              biomes=["savanna", "savanna_plateau", "badlands", "wooded_badlands"], temperament="Quick, loyal",
              ecology="A pale blue Harmontide australovenator, the 'southern hunter' of Australia's dinosaurs: light, striped, with "
                      "long hooked claws on her hands. She hunts with her pack.",
              interaction="Raw meat.",
              signature=("Pack hunt", "Befriended, when her friend strikes a mob she leaps on it too."),
              danger="Moderate (her claws).",
              variants=[("blue", "Blue (default)", "Pale blue, darker stripes"), ("tan", "Tan", "Tan, brown stripes"), ("grey", "Grey", "Grey"),
                        ("green", "Green", "Green"), ("rust", "Rust", "Rust red")]),
         {"Habitat": "Savannas, badlands", "Signature": "Joins her friend's attacks", "Danger": "Moderate"}),
]
_fam = C.family("theropod")
for _species, _cols in THEROPODS:
    if any(x["id"] == _species["id"] for x in _fam["species"]):
        continue
    _fam["species"].append(_species)
    for _label, _values in _fam.get("split", []):
        _values.append(_cols.get(_label, "-"))

# the Four Riders (2026-10-30, twenty-fifth wave): a family of their own, each with her own look; Pestilence rides a real horse
_family(id="rider", volume="XXVIII", title="The Four Riders", subtitle="the riders of the end", food="Whatever suits her",
        theme=dict(bg="#120f14", panel="#1c1820", panel2="#241f2a", ink="#f0ecf2", muted="#bcb2c4", accent="#3ae8c8",
                   line="#38303e", green="#c8a040", grad="#2a1a3a"),
        lede="War, Famine, Pestilence and Death: four girls who go where the end of things goes, and who turn out to be very good friends to have.",
        hero=dict(eyebrow="The central similarity", title="Four girls, four ends.",
                  text="No two of them look alike and none of them is in a hurry. Each is rare, walks alone and takes an interest in exactly one kind of trouble. Befriended, that trouble becomes your enemies' problem.",
                  bullets=[("War", "Stronger when you are outnumbered."), ("Famine", "Feeds you and starves them."),
                           ("Pestilence", "Rides a black horse under a teal cloud."), ("Death", "Finishes what you start.")]),
        lifecycle=[("1. Courtship", "At high friendship she may share your bed."), ("2. Young", "She bears one daughter, as a mammal does."),
                   ("3. Grown", "Grown in five days.")],
        systems=[("Rare", "They are few and wander alone, and never in a group."), ("Steed", "Pestilence rides a real black horse; if it dies she walks.")],
        friendship=dict(builds=["The food she likes (+1)"], costs=["Hitting her"], devoted="She rides at your side."),
        mating=dict(form="Live birth", young="1", grows="5 days", where="Anywhere", note="Daughters use the adult model at 45%."),
        split=[("Habitat", []), ("Signature", []), ("Danger", [])], species=[],
        decisions=["Built as their own family on 2026-10-30, with their own looks, at the user's request."],
        questions=["More riders?"])
RIDERS = [
    (dict(id="war_rider", name="War", group="Plains and old battlefields", tag="Loud · fearless", height=1.9,
          biomes=["plains", "windswept_savanna", "savanna_plateau"], temperament="Loud, fearless",
          ecology="The first rider: a wild blonde mane, blue and silver armour, and a grin that says she has been looking forward to this.",
          interaction="Cooked beef and iron ingots.",
          signature=("Battle cry", "Befriended, when several monsters close in on her friend she gives them Strength."),
          danger="High.",
          variants=[("blue", "Blue (default)", "Blonde, blue and silver"), ("crimson", "Crimson", "Red hair, red armour"), ("black", "Black", "Black armour"), ("gold", "Gold", "Gold armour"), ("white", "White", "White armour")]),
     {"Habitat": "Plains", "Signature": "Strength when outnumbered", "Danger": "High"}),
    (dict(id="famine_rider", name="Famine", group="Badlands and dead lands", tag="Quiet · hollow", height=1.9,
          biomes=["badlands", "eroded_badlands", "desert"], temperament="Quiet, hollow",
          ecology="The second rider: pale and thin in a huge white feather cloak and a dark horned hood. Food tastes of nothing to her, so she gives it away.",
          interaction="Bread, and she only ever nibbles.",
          signature=("Hollow hunger", "Befriended, she keeps her friend fed in a fight and leaves the monsters near them weak with hunger."),
          danger="Moderate.",
          variants=[("white", "White (default)", "White cloak, dark hood"), ("ash", "Ash", "Grey cloak"), ("gold", "Gold", "Wheat gold"), ("bone", "Bone", "Bone"), ("void", "Void", "Black and violet")]),
     {"Habitat": "Badlands", "Signature": "Feeds, weakens monsters", "Danger": "Moderate"}),
    (dict(id="pestilence_rider", name="Pestilence", group="Swamps and dark forests", tag="Cheerful · catching", height=1.5,
          biomes=["swamp", "mangrove_swamp", "dark_forest"], temperament="Cheerful, catching",
          ecology="The third rider: a tiny lilac-haired girl on a black horse, a thorned staff with teal flame at its tip. She is having a lovely time.",
          interaction="Glow berries and fermented spider eyes.",
          signature=("Teal cloud", "Befriended, a teal cloud falls on the monsters near her friend, hurting and slowing them. She rides a real horse."),
          danger="High.",
          variants=[("teal", "Teal (default)", "Lilac hair, teal flame"), ("green", "Green", "Green"), ("rust", "Rust", "Rust"), ("ash", "Ash", "Ash"), ("rose", "Rose", "Rose")]),
     {"Habitat": "Swamps", "Signature": "A cloud on monsters", "Danger": "High"}),
    (dict(id="death_rider", name="Death", group="Old graveyards and snow", tag="Gentle · punctual", height=1.6,
          biomes=["snowy_taiga", "old_growth_pine_taiga", "dark_forest"], temperament="Gentle, punctual",
          ecology="The last rider: a small hooded girl with a very large scythe and a crow on her hand. She is kind about it.",
          interaction="Bones and sweet berries.",
          signature=("Last call", "Befriended, she finishes off the monsters near her friend that are nearly dead."),
          danger="High.",
          variants=[("brown", "Brown (default)", "Brown cloak"), ("black", "Black", "Black cloak"), ("grey", "Grey", "Grey cloak"), ("white", "White", "White cloak"), ("violet", "Violet", "Violet cloak")]),
     {"Habitat": "Old forests", "Signature": "Finishes monsters", "Danger": "High"}),
]
_fam = C.family("rider")
for _species, _cols in RIDERS:
    if any(x["id"] == _species["id"] for x in _fam["species"]):
        continue
    _fam["species"].append(_species)
    for _label, _values in _fam.get("split", []):
        _values.append(_cols.get(_label, "-"))

# twenty-fifth wave (2026-10-30): the two giants, Charybdis, the Mad Hatter, the Ifrit, Pazuzu and the Angel; the yatagarasu is a harpy
TWENTY_FIFTH = {
    "devil": [
        (dict(id="fire_giant_devil", name="Fire Giant", group="The Nether", tag="Warm · booming", height=4.0,
              biomes=["basalt_deltas", "nether_wastes", "crimson_forest"], temperament="Warm, booming",
              ecology="A giant of the forge-worlds, four blocks tall: dark skin veined with glowing lava, a crown of flame and long black hair.",
              interaction="Blaze powder and cooked beef.",
              signature=("Hearthfire", "Befriended, she gives her friend Fire Resistance and lights the ground she walks on."),
              danger="High.",
              variants=[("ember", "Ember (default)", "Dark skin, orange lava"), ("gold", "Gold", "Bronze skin, gold lava"), ("blue", "Blue", "Blue flame"), ("green", "Green", "Green flame"), ("ash", "Ash", "Grey skin, red lava")]),
         {"Habitat": "The Nether", "Signature": "Fire Resistance, light", "Danger": "High"}),
        (dict(id="frost_giant_devil", name="Frost Giant", group="Frozen peaks", tag="Serene · huge", height=4.0,
              biomes=["frozen_peaks", "ice_spikes", "snowy_slopes"], temperament="Serene, huge",
              ecology="A giant of the far north, four blocks tall: long white hair, a crown and shoulders of ice shards, and a smile that frosts the air.",
              interaction="Salmon and snowballs.",
              signature=("Ice road", "Befriended, water freezes under her friend's feet, so they can walk across it."),
              danger="High.",
              variants=[("blue", "Blue (default)", "Pale blue, white hair"), ("white", "White", "White"), ("aqua", "Aqua", "Aqua"), ("violet", "Violet", "Violet"), ("grey", "Grey", "Grey")]),
         {"Habitat": "Frozen peaks", "Signature": "Walks on water", "Danger": "High"}),
        (dict(id="ifrit_devil", name="Ifrit", group="Deserts and the Nether", tag="Proud · smouldering", height=1.8,
              biomes=["desert", "nether_wastes", "basalt_deltas"], temperament="Proud, smouldering",
              ecology="The fire spirit of Arabian tales: horns, small wings, a tail of smoke in place of legs, and blue flames floating at her sides.",
              interaction="Blaze powder and coal.",
              signature=("Smelter", "Befriended, once a day she smelts a stack of raw ore in her friend's pack."),
              danger="Moderate.",
              variants=[("blue", "Blue (default)", "Blue flame"), ("orange", "Orange", "Orange flame"), ("green", "Green", "Green flame"), ("violet", "Violet", "Violet flame"), ("black", "Black", "Black smoke")]),
         {"Habitat": "Deserts", "Signature": "Smelts ore", "Danger": "Moderate"}),
        (dict(id="pazuzu_devil", name="Pazuzu", group="Windswept hills", tag="Sharp · restless", height=1.8,
              biomes=["windswept_hills", "windswept_savanna", "desert"], temperament="Sharp, restless",
              ecology="The Mesopotamian king of the wind demons: horns, feathered wings, taloned feet and a snake for a tail. She drives other demons away.",
              interaction="Rabbit and feathers.",
              signature=("South wind", "Befriended, a gust throws back the monsters that crowd her friend."),
              danger="Moderate.",
              variants=[("blue", "Blue (default)", "Blue"), ("sand", "Sand", "Sand"), ("grey", "Grey", "Grey"), ("red", "Red", "Red"), ("green", "Green", "Green")]),
         {"Habitat": "Windswept hills", "Signature": "Blows monsters back", "Danger": "Moderate"})],
    "mollusk": [
        (dict(id="charybdis_mollusk", name="Charybdis", group="Rocky coasts", tag="Bubbly · bottomless", height=1.8,
              biomes=["stony_shore", "ocean", "warm_ocean"], temperament="Bubbly, bottomless",
              ecology="The whirlpool across the strait from Scylla: curly pale-teal hair, a shell top, a skirt of tentacles and an appetite for the sea itself.",
              interaction="Fish and kelp.",
              signature=("Whirlpool", "Befriended, she drags the monsters in the water near her down towards her."),
              danger="High.",
              variants=[("teal", "Teal (default)", "Pale teal"), ("blue", "Blue", "Blue"), ("green", "Green", "Green"), ("rose", "Rose", "Rose"), ("grey", "Grey", "Grey")]),
         {"Habitat": "Rocky coasts", "Signature": "Pulls monsters in", "Danger": "High"})],
    "elf": [
        (dict(id="mad_hatter_elf", name="Mad Hatter", group="Flower forests", tag="Merry · unhinged", height=1.8,
              biomes=["flower_forest", "cherry_grove", "meadow"], temperament="Merry, unhinged",
              ecology="Of Wonderland, like the Cheshire and the clockhare: purple hair, a blue tailcoat, a huge hat and a teacup she never puts down.",
              interaction="Cookies and cake.",
              signature=("Tea time", "Befriended, once a day she pours her friend a cup: one good effect, chosen entirely at random."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Blue coat, purple hair"), ("green", "Green", "Green coat"), ("plum", "Plum", "Plum coat"), ("red", "Red", "Red coat"), ("gold", "Gold", "Gold coat")]),
         {"Habitat": "Flower forests", "Signature": "Random daily tea", "Danger": "Low"}),
        (dict(id="angel_elf", name="Angel", group="Mountain meadows", tag="Kind · shining", height=1.7,
              biomes=["meadow", "cherry_grove", "snowy_slopes"], temperament="Kind, shining",
              ecology="A small winged girl with a glowing halo and a plain dress. She flies, and stays close to anyone who is hurt.",
              interaction="Golden apples and honey bottles.",
              signature=("Mercy", "Befriended, she heals her friend when their health runs low."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Pale blue"), ("white", "White", "White"), ("gold", "Gold", "Gold"), ("rose", "Rose", "Rose"), ("grey", "Grey", "Grey")]),
         {"Habitat": "Mountain meadows", "Signature": "Heals at low health", "Danger": "Low"})],
}

for _fid, _entries in TWENTY_FIFTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(s["id"] == _species["id"] for s in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# twenty-sixth wave (2026-10-31): the smilodon, the caribou, Lilith, Wrath, the Min Min light and the Hand; the other eight are harpies
TWENTY_SIXTH = {
    "nekomata": [
        (dict(id="smilodon_nekomata", name="Smilodon", group="Cold plains", tag="Heavy · patient", height=2.0,
              biomes=["snowy_plains", "snowy_taiga", "savanna"], temperament="Heavy, patient",
              ecology="The great sabre-tooth of the Ice Age, furred all over in shaggy gold with a thick mane and two long fangs. She waits, then springs.",
              interaction="Raw beef and mutton.",
              signature=("Ambush", "Befriended, she springs on the nearest monster near her friend with a heavy blow, and the rest flinch back."),
              danger="High.",
              variants=[("gold", "Gold (default)", "Shaggy gold"), ("dun", "Dun", "Dusty brown"), ("snow", "Snow", "White"), ("dark", "Dark", "Dark brown"), ("red", "Red", "Red-gold")]),
         {"Habitat": "Cold plains", "Signature": "Springs on monsters", "Danger": "High"})],
    "centaur": [
        (dict(id="caribou_centaur", name="Caribou", group="The tundra", tag="Hardy · wandering", height=2.3,
              biomes=["snowy_plains", "snowy_taiga", "frozen_river"], temperament="Hardy, wandering",
              ecology="The wild cousin of the reindeer: a blue-grey deer taur with broad antlers and wide hooves, who walks a thousand miles a year.",
              interaction="Apples and sweet berries.",
              signature=("Snow stride", "Carrying her friend over snow or ice she only gets faster, and she walks on powder snow."),
              danger="Low.",
              variants=[("blue", "Blue (default)", "Blue-grey"), ("barren", "Barren-ground", "Brown and cream"), ("woodland", "Woodland", "Dark brown"), ("peary", "Peary", "Nearly white"), ("dusk", "Dusk", "Violet")]),
         {"Habitat": "The tundra", "Signature": "Fast over snow", "Danger": "Low"})],
    "devil": [
        (dict(id="lilith_devil", name="Lilith", group="Dark forests", tag="Proud · unbowed", height=1.8,
              biomes=["dark_forest", "swamp", "old_growth_spruce_taiga"], temperament="Proud, unbowed",
              ecology="The night demon of Jewish legend, who would bow to no one: blue hair, ram's horns, great bat wings. She flies, and she comes out after dark.",
              interaction="Pomegranate-red things: sweet berries and beetroot.",
              signature=("Nightfall", "Befriended, at night the monsters near her friend are weakened."),
              danger="Moderate.",
              variants=[("blue", "Blue (default)", "Blue"), ("black", "Black", "Black and red"), ("red", "Red", "Red"), ("violet", "Violet", "Violet"), ("white", "White", "White")]),
         {"Habitat": "Dark forests", "Signature": "Weakens monsters at night", "Danger": "Moderate"}),
        (dict(id="wrath_devil", name="Wrath", group="Volcanic wastes", tag="Furious · burning", height=2.1,
              biomes=["basalt_deltas", "crimson_forest", "badlands"], temperament="Furious, burning",
              ecology="Anger with nothing left to hold it: a walking fire, only vaguely in the shape of an oni, with two horns of flame and a burning club. Rain hurts her.",
              interaction="Blaze powder and fire charges.",
              signature=("Rage", "Every blow she takes makes her next ones harder. Befriended, she goes after whoever hurts her friend."),
              danger="Very high.",
              variants=[("red", "Red (default)", "Red and gold fire"), ("blue", "Blue", "Blue fire"), ("green", "Green", "Green fire"), ("violet", "Violet", "Violet fire"), ("white", "White", "White fire")]),
         {"Habitat": "Volcanic wastes", "Signature": "Hits harder when hit", "Danger": "Very high"})],
    "mollusk": [
        (dict(id="min_min_mollusk", name="Min Min Light", group="The outback", tag="Shy · drifting", height=1.5,
              biomes=["savanna", "badlands", "desert"], temperament="Shy, drifting",
              ecology="The light that hangs over the Queensland outback at night and keeps its distance: a small girl under a glowing cap, pale tentacles for legs. She floats.",
              interaction="Glow berries and glowstone dust.",
              signature=("Lead the way", "Befriended, at night she drifts ahead of her friend and lights the ground."),
              danger="None.",
              variants=[("yellow", "Yellow (default)", "Glowing yellow"), ("white", "White", "White"), ("green", "Green", "Green"), ("blue", "Blue", "Blue"), ("red", "Red", "Red")]),
         {"Habitat": "The outback", "Signature": "Lights the way at night", "Danger": "None"})],
    "undead": [
        (dict(id="hand_undead", name="Hand", group="Graveyards and cold ground", tag="Busy · silent", height=0.6,
              biomes=["swamp", "dark_forest", "deep_dark"], temperament="Busy, silent",
              ecology="Not a girl at all: an ordinary severed hand with a bone sticking out of the wrist. It runs about on its fingertips like a spider, climbs walls, and drums its fingers while it waits.",
              interaction="Rotten flesh and rings of gold nuggets.",
              signature=("Firm grip", "Befriended, it grabs the monsters near its friend by the ankle and holds them back."),
              danger="Low.",
              variants=[("fair", "Fair (default)", "Fair skin"), ("pale", "Pale", "Pale skin"), ("tan", "Tan", "Tan skin"), ("dark", "Dark", "Dark skin"), ("zombie", "Zombie", "Grey-green skin")]),
         {"Habitat": "Dark ground", "Signature": "Holds monsters back", "Danger": "Low"})],
}

for _fid, _entries in TWENTY_SIXTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(s["id"] == _species["id"] for s in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# twenty-seventh wave (2026-10-31): Bahamut, Cetus, the demon, the Fury, the Charro Negro, Koschei, La Mano Peluda, the Gaitress, the bloodhound, the shedim, Anzu and the kitsune obaa
TWENTY_SEVENTH = {
    "dragon": [
        (dict(id="bahamut_dragon", name="Bahamut", group="High peaks", tag="Regal · gentle", height=2.2,
              biomes=["jagged_peaks", "frozen_peaks", "stony_peaks"], temperament="Regal, gentle",
              ecology="The king of dragons, far softer than the stories say: pale blue and round, curled horns, fins for ears. She flies.",
              interaction="Prismarine crystals and cooked salmon.",
              signature=("Judgement", "Befriended, once a day a beam of light strikes down on every monster around her friend."),
              danger="High.",
              variants=[("blue", "Blue (default)", "Pale blue"), ("gold", "Gold", "Gold"), ("black", "Black", "Black"), ("green", "Green", "Green"), ("red", "Red", "Red")]),
         {"Habitat": "High peaks", "Signature": "A daily beam of light", "Danger": "High"})],
    "merfolk": [
        (dict(id="cetus_mermaid", name="Cetus", group="Deep oceans", tag="Huge · hungry", height=3.0,
              biomes=["deep_ocean", "deep_cold_ocean", "deep_lukewarm_ocean"], temperament="Huge, hungry",
              ecology="The sea monster of the Greek myths: a vast, soft, pale whale of a girl with a great flat fluke. Whatever she opens her mouth for is gone.",
              interaction="Cod, salmon and tropical fish.",
              signature=("Whole", "Befriended, she swallows drowned and guardians near her friend whole, and coughs up what they carried."),
              danger="High.",
              variants=[("blue", "Blue (default)", "Pale blue"), ("bw", "Black and white", "Black and white"), ("grey", "Grey", "Grey"), ("white", "White", "White"), ("deep", "Deep", "Deep blue")]),
         {"Habitat": "Deep oceans", "Signature": "Swallows sea monsters", "Danger": "High"})],
    "devil": [
        (dict(id="demon_devil", name="Demon", group="Dark forests and the Nether", tag="Sly · obliging", height=1.75,
              biomes=["dark_forest", "crimson_forest", "soul_sand_valley"], temperament="Sly, obliging",
              ecology="The plain, old-fashioned kind: small horns, furred legs, little wings and a spade tail. She is always ready to make a deal.",
              interaction="Nether wart and gold nuggets.",
              signature=("Pact", "Befriended, crouch and offer an empty hand: she takes 3 hearts and gives you half a minute of great strength."),
              danger="Moderate.",
              variants=[("blue", "Blue (default)", "Blue-grey"), ("red", "Red", "Red"), ("black", "Black", "Black"), ("green", "Green", "Green"), ("purple", "Purple", "Purple")]),
         {"Habitat": "Dark forests", "Signature": "Trades hearts for strength", "Danger": "Moderate"}),
        (dict(id="fury_devil", name="Fury", group="Ruined places", tag="Relentless · just", height=1.9,
              biomes=["soul_sand_valley", "basalt_deltas", "dark_forest"], temperament="Relentless, just",
              ecology="The Erinys of Greek myth, who hounds the guilty to the end of the earth: ink-dark clothes, wild hair, great wings of cold violet flame. She flies.",
              interaction="Ghast tears and blaze powder.",
              signature=("Hounded", "Whoever kills a villager or someone's pet near her is marked, and she does not stop until they are dead. She spares nobody, not even her friend."),
              danger="Very high.",
              variants=[("blue", "Blue (default)", "Blue and violet flame"), ("red", "Red", "Red flame"), ("black", "Black", "White flame"), ("green", "Green", "Green flame"), ("white", "White", "Pale flame")]),
         {"Habitat": "Ruined places", "Signature": "Hounds the guilty", "Danger": "Very high"})],
    "rider": [
        (dict(id="charro_negro_rider", name="Charro Negro", group="Lonely roads", tag="Courteous · patient", height=1.8,
              biomes=["desert", "badlands", "wooded_badlands", "savanna"], temperament="Courteous, patient",
              ecology="The horsewoman in black of Mexican legend, who rides the empty roads at night offering money to anyone desperate enough. It must always be paid back.",
              interaction="Emeralds, and anything cooked.",
              signature=("The loan", "Befriended, crouch with an empty hand: she lends you 8 emeralds. Bring back 16 within three days, or she comes to collect."),
              danger="High.",
              variants=[("black", "Black (default)", "Black and gold"), ("silver", "Silver", "Black and silver"), ("red", "Red", "Deep red"), ("bone", "Bone", "A pale face"), ("green", "Green", "Dark green")]),
         {"Habitat": "Lonely roads", "Signature": "Lends emeralds at a price", "Danger": "High"})],
    "undead": [
        (dict(id="koschei_undead", name="Koschei", group="Cold forests", tag="Patient · deathless", height=1.95,
              biomes=["old_growth_spruce_taiga", "snowy_taiga", "taiga"], temperament="Patient, deathless",
              ecology="Koschei the Deathless of the Russian tales: a bone beak under a tall kokoshnik, long blue robes, chains hung all about her and a lantern at her hand.",
              interaction="Eggs, and rotten flesh.",
              signature=("Chains", "Befriended, she chains the monsters near her friend to the spot: they can go no further than a few steps."),
              danger="High.",
              variants=[("blue", "Blue (default)", "Blue robes"), ("black", "Black", "Black robes"), ("gold", "Gold", "Gold robes"), ("green", "Green", "Green robes"), ("red", "Red", "Red robes")]),
         {"Habitat": "Cold forests", "Signature": "Chains monsters in place", "Danger": "High"}),
        (dict(id="mano_peluda_undead", name="La Mano Peluda", group="Old houses and dark ground", tag="Vicious · quick", height=0.6,
              biomes=["dark_forest", "swamp", "desert"], temperament="Vicious, quick",
              ecology="The hairy hand of Mexican legend, from under the bed: a pale fist under a shaggy pelt with a bone sticking out of the wrist. It runs on its fingers and climbs walls.",
              interaction="Rotten flesh and bones.",
              signature=("Lunge", "Befriended, it leaps on the face of a monster near its friend, which forgets what it was doing."),
              danger="Moderate.",
              variants=[("blue", "Blue (default)", "Blue-grey fur"), ("black", "Black", "Black fur"), ("green", "Green", "Green fur"), ("purple", "Purple", "Purple fur"), ("red", "Red", "Red fur")]),
         {"Habitat": "Dark ground", "Signature": "Leaps at faces", "Danger": "Moderate"}),
        (dict(id="gaitress_undead", name="Gaitress", group="Plains and roads", tag="Tireless · silent", height=2.6,
              biomes=["plains", "sunflower_plains", "savanna"], temperament="Tireless, silent",
              ecology="Four long legs and a swimsuit, and nothing else: two to walk on, two crossed up in the air where a head would be. Nobody knows who she is walking to.",
              interaction="Sugar and rabbit's feet.",
              signature=("Stride", "Very fast, with a kick that throws things. Befriended, her friend runs faster beside her."),
              danger="Moderate.",
              variants=[("blue", "Blue (default)", "A blue swimsuit"), ("black", "Black", "A black swimsuit"), ("green", "Green", "A green swimsuit"), ("purple", "Purple", "A purple swimsuit"), ("red", "Red", "A red swimsuit"), ("white", "White", "A white swimsuit")]),
         {"Habitat": "Plains and roads", "Signature": "Runs, and kicks", "Danger": "Moderate"})],
    "canid": [
        (dict(id="bloodhound_canid", name="Bloodhound", group="Dark forests", tag="Grinning · dogged", height=1.9,
              biomes=["dark_forest", "swamp", "deep_dark"], temperament="Grinning, dogged",
              ecology="Matted fur sewn up with black stitches, long black hair, and a wide human grin with blood on its chin. She has never yet lost a scent.",
              interaction="Raw meat, the rawer the better.",
              signature=("The scent", "Befriended, she tracks whatever last hurt her friend, however far it ran, or else the nearest thing hiding from them, and shows them where it went."),
              danger="High.",
              variants=[("brown", "Brown (default)", "Matted brown"), ("grey", "Grey", "Grey"), ("black", "Black", "Black"), ("red", "Red", "Red"), ("pale", "Pale", "Pale")]),
         {"Habitat": "Dark forests", "Signature": "Tracks what hurt her friend", "Danger": "High"})],
    "centaur": [
        (dict(id="shedim_centaur", name="Shedim", group="Lonely places", tag="Timid · uncanny", height=1.6,
              biomes=["desert", "meadow", "old_growth_birch_forest"], temperament="Timid, uncanny",
              ecology="A spirit of Jewish lore, drawn as a soft deer-fox: long ears, little antlers, a bushy tail. By night you cannot see her at all.",
              interaction="Sweet berries and honey.",
              signature=("Elsewhere", "Unseen at night. Befriended, when her friend is badly hurt with a monster close, she takes them away from it in a blink."),
              danger="None.",
              variants=[("blue", "Blue (default)", "Blue"), ("gold", "Gold", "Golden yellow"), ("red", "Red", "Red"), ("violet", "Violet", "Violet"), ("white", "White", "White")]),
         {"Habitat": "Lonely places", "Signature": "Blinks her friend to safety", "Danger": "None"}),
        (dict(id="anzu_centaur", name="Anzu", group="Mountain crags", tag="Thieving · stormy", height=2.2,
              biomes=["windswept_hills", "stony_peaks", "savanna_plateau"], temperament="Thieving, stormy",
              ecology="The storm-bird of Sumerian myth, who stole the Tablet of Destinies from the gods: a blue lioness with feathered wings, long blue hair and gold at her brow.",
              interaction="Gold nuggets and raw beef.",
              signature=("Snatch", "Befriended, she snatches the weapon out of a monster's hand and steals its blessings for her friend."),
              danger="High.",
              variants=[("blue", "Blue (default)", "Blue"), ("yellow", "Yellow", "Yellow"), ("red", "Red", "Red"), ("black", "Black", "Black"), ("white", "White", "White")]),
         {"Habitat": "Mountain crags", "Signature": "Steals weapons and blessings", "Danger": "High"})],
    "kitsune": [
        (dict(id="obaa_kitsune", name="Kitsune Obaa", group="Old forests", tag="Knowing · amused", height=1.7,
              biomes=["cherry_grove", "old_growth_birch_forest", "taiga"], temperament="Knowing, amused",
              ecology="The old fox-wife of the tales, not nearly as old as she should look: white hair, nine white tails, red berries in her hair. Nothing fools her, and she fools everything.",
              interaction="Fried tofu has not reached this world: cooked chicken and sweet berries.",
              signature=("Fox-glamour", "Befriended, once a day, when monsters close in on her friend, she hides them in a glamour and the monsters lose interest."),
              danger="Low.",
              variants=[("white", "White (default)", "White"), ("gold", "Gold", "Gold"), ("black", "Black", "Black"), ("red", "Red", "Red"), ("silver", "Silver", "Silver")]),
         {"Habitat": "Old forests", "Signature": "Hides her friend once a day", "Danger": "Low"})],
}

for _fid, _entries in TWENTY_SEVENTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(s["id"] == _species["id"] for s in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# twenty-eighth wave (2026-11-01): the last of the Harmontide art: the Gandharva, the Apsara, the pixi, the rlff, the Ark Seraphim (the seven birds are harpies, with their own gen scripts)
TWENTY_EIGHTH = {
    "elf": [
        (dict(id="gandharva_elf", name="Gandharva", group="Cherry groves", tag="Musical · courteous", height=1.75,
              biomes=['cherry_grove', 'meadow'], temperament="Courteous, dreamy",
              ecology="The celestial musician of Hindu and Buddhist lore: a youth in a long robe, small blue wings, a lute on his back.",
              interaction="Sweet berries and honey.",
              signature=("Lute", "He truly plays: a melody of notes on his lute. With a jukebox or a note block nearby he joins in, and his friends heal to the music."),
              danger="Low.",
              variants=[('blue', 'Blue (default)', 'Blue'), ('saffron', 'Saffron', 'Saffron'), ('rose', 'Rose', 'Rose'), ('green', 'Green', 'Green'), ('white', 'White', 'White')]),
         {"Habitat": "Cherry groves", "Signature": "Plays his lute", "Danger": "Low."}),
        (dict(id="apsara_elf", name="Apsara", group="Rivers by the blossom", tag="Graceful · playful", height=1.8,
              biomes=['river', 'cherry_grove'], temperament="Graceful, playful",
              ecology="The celestial dancer and water nymph of Hindu and Buddhist lore, water-blue, in a dancer's full costume.",
              interaction="Cod, salmon and honey.",
              signature=("Dance", "To any music she dances, and her friend moves quicker. With a Gandharva playing, her friend heals as well."),
              danger="Low.",
              variants=[('blue', 'Blue (default)', 'Blue'), ('lotus', 'Lotus', 'Lotus'), ('jade', 'Jade', 'Jade'), ('gold', 'Gold', 'Gold'), ('night', 'Night', 'Night')]),
         {"Habitat": "Rivers by the blossom", "Signature": "Dances to music", "Danger": "Low."}),
    ],
    "bug": [
        (dict(id="pixi_bug", name="Pixi", group="Mushroom fields", tag="Tiny · mischievous", height=0.7,
              biomes=['mushroom_fields', 'flower_forest'], temperament="Mischievous",
              ecology="A tiny blue fairy in a plain sundress, no bigger than a cat.",
              interaction="Sugar and sweet berries.",
              signature=("Shrink", "Crouch and ask her, empty-handed, and she shrinks her friend for half a minute, small enough for a one-block gap."),
              danger="Low.",
              variants=[('blue', 'Blue (default)', 'Blue'), ('candy', 'Candy', 'Candy'), ('mint', 'Mint', 'Mint'), ('lemon', 'Lemon', 'Lemon'), ('dusk', 'Dusk', 'Dusk')]),
         {"Habitat": "Mushroom fields", "Signature": "Shrinks her friend", "Danger": "Low."}),
        (dict(id="rlff_bug", name="Rlff", group="Forest streams", tag="Serene · ancient", height=1.9,
              biomes=['lush_caves', 'river'], temperament="Serene",
              ecology="A tall golden spirit of forest streams, in a long gown, on wide pale wings.",
              interaction="Glow berries and honey.",
              signature=("Healing water", "While her friend stands in water near her, the water heals them."),
              danger="Low.",
              variants=[('gold', 'Gold (default)', 'Gold'), ('moss', 'Moss', 'Moss'), ('brook', 'Brook', 'Brook'), ('autumn', 'Autumn', 'Autumn'), ('pale', 'Pale', 'Pale')]),
         {"Habitat": "Forest streams", "Signature": "Her water heals", "Danger": "Low."}),
    ],
    "centaur": [
        (dict(id="ark_seraphim_centaur", name="Ark Seraphim", group="End highlands", tag="Watchful · solemn", height=2.4,
              biomes=['end_highlands', 'end_midlands'], temperament="Solemn, watchful",
              ecology="A winged blue lioness in jewelled trappings who bears a small gold ark on her back across the outer islands.",
              interaction="Golden apples and raw beef.",
              signature=("Blinding light", "Once a day, when her friend is badly hurt, a blast of light burns the monsters around them and shields her friend."),
              danger="High.",
              variants=[('blue', 'Blue (default)', 'Blue'), ('white', 'White', 'White'), ('gold', 'Gold', 'Gold'), ('void', 'Void', 'Void'), ('rose', 'Rose', 'Rose')]),
         {"Habitat": "End highlands", "Signature": "Burns and shields", "Danger": "High."}),
    ],
}
for _fid, _entries in TWENTY_EIGHTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(s["id"] == _species["id"] for s in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# twenty-ninth wave (2026-11-01): new designs, for the mushroom fields, the warped forest and the soul sand valley
TWENTY_NINTH = {
    "flora": [
        (dict(id="shiitake_flora", name="Shiitake", group="Mushroom fields", tag="Plural · patient", height=1.8,
              biomes=['mushroom_fields'], temperament="Patient, talkative",
              ecology="A brown-capped mushroom woman with two small offshoots of herself growing from her, all three of one mind.",
              interaction="Brown mushrooms and bone meal.",
              signature=("Offshoots", "Her offshoots throw themselves in front of a blow meant for her friend, then regrow over a day."),
              danger="Low.",
              variants=[('brown', 'Brown (default)', 'Brown'), ('chestnut', 'Chestnut', 'Chestnut'), ('pale', 'Pale', 'Pale'), ('ink', 'Ink', 'Ink'), ('violet', 'Violet', 'Violet')]),
         {"Habitat": "Mushroom fields", "Signature": "Takes blows for a friend", "Danger": "Low."}),
    ],
    "minotaur": [
        (dict(id="shroomotaur_minotaur", name="Shroomotaur", group="Mushroom fields", tag="Placid · generous", height=2.1,
              biomes=['mushroom_fields'], temperament="Placid",
              ecology="A mooshroom cow, red and white, with mushrooms growing from her shoulders.",
              interaction="Wheat and mushrooms.",
              signature=("Stew", "Hold out a bowl and she fills it with mushroom stew."),
              danger="Low.",
              variants=[('red', 'Red (default)', 'Red'), ('brown', 'Brown', 'Brown'), ('glow', 'Glow', 'Glow'), ('purple', 'Purple', 'Purple'), ('pale', 'Pale', 'Pale')]),
         {"Habitat": "Mushroom fields", "Signature": "Fills bowls with stew", "Danger": "Low."}),
    ],
    "centaur": [
        (dict(id="censhroom_centaur", name="Censhroom", group="Mushroom fields", tag="Calm · sturdy", height=2.2,
              biomes=['mushroom_fields', 'dark_forest'], temperament="Calm",
              ecology="A pale taur whose back has grown a shelf of fungus, flat enough to ride on.",
              interaction="Mushrooms and wheat.",
              signature=("Fungal saddle", "She needs no saddle, and no poison can touch her rider."),
              danger="Low.",
              variants=[('brown', 'Brown (default)', 'Brown'), ('red', 'Red', 'Red'), ('glow', 'Glow', 'Glow'), ('dark', 'Dark', 'Dark'), ('violet', 'Violet', 'Violet')]),
         {"Habitat": "Mushroom fields", "Signature": "Rider is poison-proof", "Danger": "Low."}),
        (dict(id="zoglintaur_centaur", name="Zoglintaur", group="Warped and crimson forests", tag="Ferocious · loyal", height=2.4,
              biomes=['warped_forest', 'crimson_forest'], temperament="Ferocious",
              ecology="A rotting hoglin taur, tusked and raw. Hostile until calmed with raw meat.",
              interaction="Raw meat.",
              signature=("Goring charge", "Befriended, she tosses the monsters near her friend into the air."),
              danger="High.",
              variants=[('pink', 'Pink (default)', 'Pink'), ('ashen', 'Ashen', 'Ashen'), ('crimson', 'Crimson', 'Crimson'), ('warped', 'Warped', 'Warped'), ('bone', 'Bone', 'Bone')]),
         {"Habitat": "Warped and crimson forests", "Signature": "Tosses monsters", "Danger": "High."}),
    ],
    "armoured": [
        (dict(id="piglette_armoured", name="Piglette", group="Nether forests", tag="Greedy · brave", height=1.9,
              biomes=['crimson_forest', 'warped_forest', 'nether_wastes'], temperament="Greedy, brave",
              ecology="A piglin woman in gold trim and a padded tunic.",
              interaction="Pork and gold.",
              signature=("Barter", "Hand her gold and she pays you in Nether goods. She can carry a crossbow or a gun."),
              danger="Medium.",
              variants=[('gold', 'Gold (default)', 'Gold'), ('rose', 'Rose', 'Rose'), ('brown', 'Brown', 'Brown'), ('ashen', 'Ashen', 'Ashen'), ('warped', 'Warped', 'Warped')]),
         {"Habitat": "Nether forests", "Signature": "Barters for gold", "Danger": "Medium."}),
    ],
    "devil": [
        (dict(id="firesprite_devil", name="Firesprite", group="Warped forest", tag="Flickering · warm", height=1.8,
              biomes=['warped_forest', 'basalt_deltas'], temperament="Flickering",
              ecology="A fire elemental in a woman's shape, burning a warped forest's teal.",
              interaction="Blaze powder and coal.",
              signature=("Kindle", "Befriended, she sets fire to the monsters near her friend and keeps her friend from burning."),
              danger="Medium.",
              variants=[('teal', 'Teal (default)', 'Teal'), ('crimson', 'Crimson', 'Crimson'), ('violet', 'Violet', 'Violet'), ('gold', 'Gold', 'Gold'), ('white', 'White', 'White')]),
         {"Habitat": "Warped forest", "Signature": "Ignites monsters", "Danger": "Medium."}),
    ],
    "undead": [
        (dict(id="cordyceps_undead", name="Cordyceps Infected", group="Mushroom fields", tag="Hungry · lost", height=1.8,
              biomes=['mushroom_fields', 'lush_caves'], temperament="Hostile until fed",
              ecology="A zombie with fungus fruiting from her head and shoulder.",
              interaction="Rotten flesh.",
              signature=("Staved off", "Five feedings of rotten flesh hold the fungus back: she is calm, a friend, and well again."),
              danger="Medium.",
              variants=[('infected', 'Infected (default)', 'Infected'), ('cured', 'Cured', 'Cured'), ('ashen', 'Ashen', 'Ashen'), ('violet', 'Violet', 'Violet'), ('scarlet', 'Scarlet', 'Scarlet')]),
         {"Habitat": "Mushroom fields", "Signature": "Cured by feeding", "Danger": "Medium."}),
        (dict(id="lost_soul_undead", name="Lost Soul", group="Soul sand valley", tag="Wistful · faint", height=1.7,
              biomes=['soul_sand_valley'], temperament="Wistful",
              ecology="A pale legless soul who knows the way back to where things ended.",
              interaction="Bones and soul sand.",
              signature=("Way back", "Ask her, empty-handed, and she points you to where you last died."),
              danger="Low.",
              variants=[('blue', 'Blue (default)', 'Blue'), ('pale', 'Pale', 'Pale'), ('green', 'Green', 'Green'), ('amber', 'Amber', 'Amber'), ('dusk', 'Dusk', 'Dusk')]),
         {"Habitat": "Soul sand valley", "Signature": "Finds your last death", "Danger": "Low."}),
        (dict(id="soul_cluster_undead", name="Soul Cluster", group="Soul sand valley", tag="Crowded · stubborn", height=1.9,
              biomes=['soul_sand_valley'], temperament="Stubborn",
              ecology="Several pale faces gathered into one body.",
              interaction="Bones and soul sand.",
              signature=("Three lives", "A killing blow only scatters her: she re-forms three times before she can die, and regains those over a day."),
              danger="Medium.",
              variants=[('blue', 'Blue (default)', 'Blue'), ('pale', 'Pale', 'Pale'), ('green', 'Green', 'Green'), ('amber', 'Amber', 'Amber'), ('dusk', 'Dusk', 'Dusk')]),
         {"Habitat": "Soul sand valley", "Signature": "Re-forms three times", "Danger": "Medium."}),
        (dict(id="soul_leech_undead", name="Soul Leech", group="Soul sand valley", tag="Patient · thirsty", height=1.7,
              biomes=['soul_sand_valley'], temperament="Patient",
              ecology="A dark ribbed soul with a round sucking mouth.",
              interaction="Bones and soul sand.",
              signature=("Drain", "Befriended, she drains the monsters near her friend and passes the health on."),
              danger="Medium.",
              variants=[('slate', 'Slate (default)', 'Slate'), ('oil', 'Oil', 'Oil'), ('blood', 'Blood', 'Blood'), ('ash', 'Ash', 'Ash'), ('void', 'Void', 'Void')]),
         {"Habitat": "Soul sand valley", "Signature": "Drains monsters", "Danger": "Medium."}),
    ],
}
for _fid, _entries in TWENTY_NINTH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(s["id"] == _species["id"] for s in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# thirtieth wave (2026-11-01): new designs for the cold seas, the dripstone caves and the End
THIRTIETH = {
    "merfolk": [
        (dict(id="beluga_mermaid", name="Beluga", group="Frozen seas", tag="Chatty · gentle", height=1.9,
              biomes=['frozen_ocean', 'deep_frozen_ocean', 'cold_ocean'], temperament="Chatty, gentle",
              ecology="A soft white whale with a round brow, the canary of the sea.",
              interaction="Cod and salmon.",
              signature=("Sonar", "Befriended, she sings the monsters in the water near her friend into view, and carries her friend along."),
              danger="Low.",
              variants=[('white', 'White (default)', 'White'), ('grey', 'Grey', 'Grey'), ('cream', 'Cream', 'Cream'), ('blush', 'Blush', 'Blush'), ('ice', 'Ice', 'Ice')]),
         {"Habitat": "Frozen seas", "Signature": "Reveals swimmers", "Danger": "Low."}),
        (dict(id="olm_mermaid", name="Olm", group="Dripstone caves", tag="Blind · patient", height=1.6,
              biomes=['dripstone_caves'], temperament="Patient",
              ecology="The blind cave salamander, pale pink, no eyes at all. She has never needed them.",
              interaction="Cod and glow berries.",
              signature=("Lightless", "Befriended, her friend near her sees in the dark, and nothing can blind them."),
              danger="Low.",
              variants=[('pink', 'Pink (default)', 'Pink'), ('white', 'White', 'White'), ('black', 'Black', 'Black'), ('peach', 'Peach', 'Peach'), ('lilac', 'Lilac', 'Lilac')]),
         {"Habitat": "Dripstone caves", "Signature": "Lifts blindness", "Danger": "Low."}),
    ],
    "crustacean": [
        (dict(id="spider_crab_crustacean", name="Spider Crab", group="Cold deeps", tag="Slow · relentless", height=2.4,
              biomes=['deep_cold_ocean', 'deep_frozen_ocean'], temperament="Relentless",
              ecology="The giant crab of the cold deep, tall and long-limbed.",
              interaction="Cod and kelp.",
              signature=("Pin", "Befriended, she pins a monster near her friend in her long claws."),
              danger="Medium.",
              variants=[('orange', 'Orange (default)', 'Orange'), ('pale', 'Pale', 'Pale'), ('red', 'Red', 'Red'), ('blue', 'Blue', 'Blue'), ('black', 'Black', 'Black')]),
         {"Habitat": "Cold deeps", "Signature": "Pins monsters", "Danger": "Medium."}),
    ],
    "mollusk": [
        (dict(id="giant_kraken_mollusk", name="Giant Kraken", group="Frozen deeps", tag="Vast · brooding", height=5.0,
              biomes=['deep_frozen_ocean', 'deep_cold_ocean'], temperament="Brooding",
              ecology="The kraken of the sailors' worst stories, five blocks tall. Attacks swimmers until fed.",
              interaction="Cod and salmon.",
              signature=("Crush", "Befriended, she crushes every monster in the water near her friend."),
              danger="Very high.",
              variants=[('abyss', 'Abyss (default)', 'Abyss'), ('crimson', 'Crimson', 'Crimson'), ('pallid', 'Pallid', 'Pallid'), ('violet', 'Violet', 'Violet'), ('ink', 'Ink', 'Ink')]),
         {"Habitat": "Frozen deeps", "Signature": "Crushes the water", "Danger": "Very high."}),
    ],
    "lizardfolk": [
        (dict(id="cave_gekko_lizardfolk", name="Cave Gekko", group="Dripstone caves", tag="Sticky · watchful", height=1.5,
              biomes=['dripstone_caves', 'lush_caves'], temperament="Watchful",
              ecology="A pale gecko with huge dark eyes, who walks up walls.",
              interaction="Spider eyes and glow berries.",
              signature=("Sticky hold", "She climbs walls, and a friend near her who falls drifts down."),
              danger="Low.",
              variants=[('pale', 'Pale (default)', 'Pale'), ('ochre', 'Ochre', 'Ochre'), ('slate', 'Slate', 'Slate'), ('moss', 'Moss', 'Moss'), ('rust', 'Rust', 'Rust')]),
         {"Habitat": "Dripstone caves", "Signature": "Softens falls", "Danger": "Low."}),
    ],
    "undead": [
        (dict(id="drowned_undead", name="Drowned", group="Cold seas", tag="Hungry · cold", height=1.8,
              biomes=['cold_ocean', 'deep_cold_ocean', 'frozen_ocean', 'deep_frozen_ocean'], temperament="Hostile until fed",
              ecology="A zombie of the cold sea, blue-green, walking the sea floor.",
              interaction="Rotten flesh.",
              signature=("Deep breath", "Calmed and befriended, her friend under water near her doesn't run out of air."),
              danger="Medium.",
              variants=[('drowned', 'Drowned (default)', 'Drowned'), ('frozen', 'Frozen', 'Frozen'), ('deep', 'Deep', 'Deep'), ('kelp', 'Kelp', 'Kelp'), ('pale', 'Pale', 'Pale')]),
         {"Habitat": "Cold seas", "Signature": "Gives breath", "Danger": "Medium."}),
        (dict(id="famished_undead", name="Famished", group="Dripstone caves", tag="Starving · dangerous", height=2.0,
              biomes=['dripstone_caves', 'deep_dark'], temperament="Hostile until fed",
              ecology="Something starved in the dark, gaunt, long claws and a wide mouth. Drop food near her and she eases.",
              interaction="Any food, dropped.",
              signature=("Sated", "Drop food near her and she eats, turns friendly, and looks far less monstrous."),
              danger="High.",
              variants=[('famished', 'Famished (default)', 'Famished'), ('sated', 'Sated', 'Sated'), ('pale', 'Pale', 'Pale'), ('ember', 'Ember', 'Ember'), ('ash', 'Ash', 'Ash')]),
         {"Habitat": "Dripstone caves", "Signature": "Drop food to befriend", "Danger": "High."}),
    ],
    "jackalope": [
        (dict(id="moon_rabbit_jackalope", name="Moon Rabbit", group="End highlands", tag="Busy · kind", height=1.6,
              biomes=['end_highlands'], temperament="Busy, kind",
              ecology="The jade rabbit of Chinese legend, who pounds the elixir of life on the moon.",
              interaction="Chorus fruit and carrots.",
              signature=("Elixir", "Hand her chorus fruit and she pounds it into a healing potion."),
              danger="Low.",
              variants=[('white', 'White (default)', 'White'), ('jade', 'Jade', 'Jade'), ('gold', 'Gold', 'Gold'), ('dusk', 'Dusk', 'Dusk'), ('ash', 'Ash', 'Ash')]),
         {"Habitat": "End highlands", "Signature": "Makes healing potions", "Danger": "Low."}),
    ],
    "dragon": [
        (dict(id="kun_dragon", name="Kun", group="End midlands", tag="Vast · serene", height=3.0,
              biomes=['end_midlands'], temperament="Serene",
              ecology="The great fish of the Zhuangzi that became a bird, swimming the sky between the islands.",
              interaction="Cod and chorus fruit.",
              signature=("Over the void", "Once a day she catches a friend falling into the void."),
              danger="High.",
              variants=[('sky', 'Sky (default)', 'Sky'), ('void', 'Void', 'Void'), ('dawn', 'Dawn', 'Dawn'), ('pale', 'Pale', 'Pale'), ('jade', 'Jade', 'Jade')]),
         {"Habitat": "End midlands", "Signature": "Catches you from the void", "Danger": "High."}),
    ],
    "elf": [
        (dict(id="knocker_elf", name="Knocker", group="Dripstone caves", tag="Gruff · helpful", height=1.4,
              biomes=['dripstone_caves'], temperament="Gruff",
              ecology="The Cornish mine spirit, who knocks on the rock to warn of a cave-in.",
              interaction="Bread and cooked pork.",
              signature=("Knocking", "She knocks when loose rock hangs over her friend, and in the direction of ore."),
              danger="Low.",
              variants=[('brown', 'Brown (default)', 'Brown'), ('copper', 'Copper', 'Copper'), ('soot', 'Soot', 'Soot'), ('tin', 'Tin', 'Tin'), ('ochre', 'Ochre', 'Ochre')]),
         {"Habitat": "Dripstone caves", "Signature": "Warns of cave-ins, finds ore", "Danger": "Low."}),
        (dict(id="kaguya_elf", name="Kaguya", group="End highlands", tag="Aloof · homesick", height=1.75,
              biomes=['end_highlands'], temperament="Aloof",
              ecology="The moon's exiled princess of the Japanese tale.",
              interaction="Sweet berries and honey.",
              signature=("Moon's weight", "Befriended, her friend near her falls softly and jumps high."),
              danger="Low.",
              variants=[('night', 'Night (default)', 'Night'), ('silver', 'Silver', 'Silver'), ('bamboo', 'Bamboo', 'Bamboo'), ('rose', 'Rose', 'Rose'), ('gold', 'Gold', 'Gold')]),
         {"Habitat": "End highlands", "Signature": "Low gravity", "Danger": "Low."}),
        (dict(id="ophanim_elf", name="Ophanim", group="End highlands", tag="Unblinking · solemn", height=1.9,
              biomes=['end_highlands'], temperament="Solemn",
              ecology="The biblical wheel covered in eyes, a pale woman inside a ring of them.",
              interaction="Golden carrots and honey.",
              signature=("All-seeing", "Befriended, every monster around her friend shows through the walls."),
              danger="Medium.",
              variants=[('gold', 'Gold (default)', 'Gold'), ('silver', 'Silver', 'Silver'), ('ember', 'Ember', 'Ember'), ('azure', 'Azure', 'Azure'), ('pale', 'Pale', 'Pale')]),
         {"Habitat": "End highlands", "Signature": "Reveals all monsters", "Danger": "Medium."}),
        (dict(id="norn_elf", name="Norn", group="End midlands", tag="Remote · exact", height=1.8,
              biomes=['end_midlands'], temperament="Remote",
              ecology="A Norse weaver of fate, in undyed wool.",
              interaction="Bread and honey.",
              signature=("Foretold", "Befriended, she warns her friend just before something comes for them, and steadies them."),
              danger="Low.",
              variants=[('wool', 'Wool (default)', 'Wool'), ('past', 'Past', 'Past'), ('present', 'Present', 'Present'), ('future', 'Future', 'Future'), ('red', 'Red', 'Red')]),
         {"Habitat": "End midlands", "Signature": "Warns of attacks", "Danger": "Low."}),
    ],
}
for _fid, _entries in THIRTIETH.items():
    _fam = C.family(_fid)
    for _species, _cols in _entries:
        if any(s["id"] == _species["id"] for s in _fam["species"]):
            continue
        _fam["species"].append(_species)
        for _label, _values in _fam.get("split", []):
            _values.append(_cols.get(_label, "-"))

# homage variants of 2026-10-28 (POP_CULTURE_VARIANTS.md): appended after every wave, so every species exists by now
for _sid, _variant in [("cave_bat", ("jewel_thief", "Jewel Thief", "White hair, a black bodysuit with a pink heart, white gloves and boots")),
                       ("ika_mollusk", ("ink_kid", "Ink Kid", "Bright orange tentacle hair, a white tee and black shorts")),
                       ("cattle_minotaur", ("pink_dairy", "Pink Dairy", "Pink with a cream belly and blue eyes")),
                       ("wolf_canid", ("sun_goddess", "Sun Goddess", "White with red markings")),
                       # nineteenth wave
                       ("octopus_mollusk", ("octo_agent", "Octo Agent", "Magenta tentacle hair, a black bodysuit with teal trim")),
                       ("chameleon_lizardfolk", ("ninja_chameleon", "Ninja Chameleon", "Purple scales, a black wrap, yellow trim")),
                       ("turtle_lizardfolk", ("shell_trooper", "Shell Trooper", "Yellow skin, a green shell with a white rim")),
                       ("kobold_canid", ("town_clerk", "Town Clerk", "Golden fur, a green vest, a red bell")),
                       ("mantis_bug", ("scythe_green", "Scythe Green", "Bright green, cream belly, pale blades")),
                       ("mandragora_flora", ("leaf_sprout", "Leaf Sprout", "Blue skin, a leaf on a stalk")),
                       ("axolotl_mermaid", ("mud_fin", "Mud Fin", "Blue with orange cheek gills")),
                       ("shark_mermaid", ("trident_hood", "Trident Hood", "White hair, a blue hoodie")),
                       # twentieth wave
                       ("house_mouse_rodent", ("innkeeper", "Innkeeper", "Grey-brown, red eyes, a worn suit, a ring of keys")),
                       ("house_mouse_rodent", ("prankster", "Prankster", "Light fur, a striped shirt, a slingshot")),
                       ("house_mouse_rodent", ("soul_keeper", "Soul Keeper", "White hair, a dark robe, soul bottles")),
                       ("high_elf", ("sage_princess", "Sage Princess", "Golden-brown hair, a white and lilac gown")),
                       ("wood_elf", ("green_hero", "Green Hero", "Blonde, a green tunic and long green cap")),
                       ("ent_flora", ("tree_guard", "Tree Guard", "Brown bark, a few leaves, amber eyes")),
                       ("yuki_onna_undead", ("snow_wraith", "Snow Wraith", "White, a red-purple sash, purple eyes")),
                       ("dullahan_undead", ("headless_rider", "Headless Rider", "A black rider's suit with yellow trim")),
                       ("dullahan_undead", ("death_envoy", "Death Envoy", "Silver hair, red eyes, a black and red gothic dress")),
                       ("jorogumo_arachne", ("bake_sale", "Bake Sale", "Lilac skin, five eyes, black pigtails with red bows, a teacup")),
                       # twenty-third wave
                       ("fennec_kitsune", ("twin_tails", "Twin Tails", "Orange and white, blue eyes, two tails, goggles")),
                       ("salamander_lizardfolk", ("ember_tail", "Ember Tail", "Orange, a cream belly, a flame at her tail tip")),
                       ("snapping_turtle_lizardfolk", ("spike_king", "Spike King", "Yellow skin, red hair, a green spiked shell")),
                       ("zombie_undead", ("lawn_zombie", "Lawn Zombie", "A brown suit, a red tie, a traffic cone")),
                       ("undine_slime", ("drop_smile", "Drop Smile", "Clear blue, a drop's point on her head, a wide smile")),
                       ("amethyst_slime", ("puffball", "Puffball", "Pink, rosy cheeks, red crystals")),
                       ("ratatoskr_rodent", ("hoodie_squirrel", "Hoodie Squirrel", "Red-orange fur, a blue hoodie")),
                       ("bee_bug", ("flight_cap", "Flight Cap", "An orange vest, an aviator's cap and goggles")),
                       # twenty-fourth wave
                       ("cyclops_devil", ("tactical_eye", "Tactical Eye", "Purple hair, a yellow and black combat suit, a long rifle")),
                       ("genie_devil", ("lamp_friend", "Lamp Friend", "Bright blue, a black top-knot and goatee, gold cuffs, a red sash")),
                       ("jumping_spider_arachne", ("tiny_jumper", "Tiny Jumper", "Round, black and gold")),
                       ("moth_bug", ("kaiju_moth", "Kaiju Moth", "Gold and orange wings, a white ruff")),
                       ("golem_construct", ("pocket_bot", "Pocket Bot", "Silver with green eyes")),
                       ("red_kitsune", ("nine_cream", "Nine Cream", "Cream fur and hair, red eyes, nine tails")),
                       ("silkcoon_rodent", ("shopkeeper", "Shopkeeper", "Brown and tan, a green apron")),
                       ("will_o_wisp_undead", ("shy_ghost", "Shy Ghost", "All white, dark eyes")),
                       ("vampire_undead", ("night_heir", "Night Heir", "Silver hair, a black and gold coat")),
                       # twenty-fifth wave
                       ("croc_lizardfolk", ("detective_croc", "Detective Croc", "Green scales, gold trim")),
                       ("kangaroo_marsupial", ("bandicoot", "Bandicoot", "Orange fur, red hair, blue jeans")),
                       ("cactus_flora", ("needle_runner", "Needle Runner", "Bright green, dark eyes")),
                       ("pegasus_centaur", ("angel_guard", "Angel Guard", "White and gold, dark hair")),
                       ("living_doll_undead", ("music_box", "Music Box", "A white face, a black body, purple eyes")),
                       ("dolphin_mermaid", ("star_song", "Star Song", "Grey-blue, a pale belly")),
                       # twenty-seventh wave
                       ("needlehog_rodent", ("blue_blur", "Blue Blur", "Blue quills, a tan belly, red clothes")),
                       ("blaze_cat_nekomata", ("royal_flame", "Royal Flame", "Lilac fur, a white coat, pink flames")),
                       ("grizzly_canid", ("backpack_bear", "Backpack Bear", "Warm brown, yellow shorts, a blue pack")),
                       ("wolf_canid", ("aura_guardian", "Aura Guardian", "Blue and black, a cream chest")),
                       ("red_kitsune", ("gold_ninja", "Gold Ninja", "Golden fur, a white front, purple clothes")),
                       ("succubus_devil", ("night_heiress", "Night Heiress", "Green hair, black, purple wings and stockings")),
                       ("undine_slime", ("copy_blob", "Copy Blob", "Soft pink, dot eyes")),
                       # twenty-eighth wave
                       ("house_mouse_rodent", ("spark_mouse", "Spark Mouse", "All yellow, black-tipped ears, red at the cheeks")),
                       ("gecko_lizardfolk", ("green_dino", "Green Dino", "Bright green, a white front, a red saddle")),
                       ("reindeer_centaur", ("doctor_deer", "Doctor Deer", "Warm brown fur, pink clothes")),
                       ("wyrm_dragon", ("purple_flame", "Purple Flame", "Purple scales, gold horns and wing webs")),
                       ("gryphon_dragon", ("night_wing", "Night Wing", "Black all over, bright green eyes")),
                       ("panda_canid", ("forest_keeper", "Forest Keeper", "Soft grey, a cream belly")),
                       ("grizzly_canid", ("sleepy_giant", "Sleepy Giant", "Blue-green fur, a cream belly")),
                       ("fennec_kitsune", ("first_fox", "First Fox", "Warm brown fur, a big cream ruff")),
                       # twenty-ninth wave
                       ("hornet_bug", ("needle_knight", "Needle Knight", "A white mask of a face, a red cloak")),
                       ("matango_flora", ("spore_retainer", "Spore Retainer", "A white cap with red spots, a blue vest")),
                       ("caracal_nekomata", ("lasagna_cat", "Lasagna Cat", "Orange with black stripes")),
                       ("gray_kitsune", ("sly_fox", "Sly Fox", "Orange fur, a green shirt")),
                       ("boghare_jackalope", ("carrot_bunny", "Carrot Bunny", "Grey and white")),
                       ("jumping_spider_arachne", ("web_hero", "Web Hero", "Red and blue")),
                       ("kobold_canid", ("blue_heeler", "Blue Heeler", "Blue with darker patches")),
                       ("ogre_devil", ("swamp_king", "Swamp King", "Green skin, a rough tunic")),
                       # thirtieth wave
                       ("octopus_mollusk", ("sea_witch", "Sea Witch", "Lilac skin, white hair, dark tentacles")),
                       ("crab_crustacean", ("calypso_crab", "Calypso Crab", "All bright red")),
                       ("inaba_jackalope", ("moon_warrior", "Moon Warrior", "Golden hair, a navy and red sailor suit")),
                       ("yuki_onna_undead", ("frozen_queen", "Frozen Queen", "A white-blonde braid, an ice-blue gown")),
                       ("lioness_nekomata", ("pride_queen", "Pride Queen", "Golden tan, blue eyes")),
                       ("long_dragon", ("wish_dragon", "Wish Dragon", "Green scales, red eyes")),
                       ("night_elf", ("night_warden", "Night Warden", "Lilac skin, teal hair, dark armour")),
                       ("arctic_kitsune", ("ice_fox", "Ice Fox", "Pale blue fur, curled tails"))]:
    for _fam in C.FAMILIES:
        for _s in _fam["species"]:
            if _s["id"] == _sid and all(v[0] != _variant[0] for v in _s["variants"]):
                _s["variants"].append(_variant)
