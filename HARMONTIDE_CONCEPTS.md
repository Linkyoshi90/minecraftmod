# Harmontide: species concepts

Rough notes for the next wave of species. Nothing here is built yet. Each entry gives:

- **Home:** the existing family it slots into (reuse that family's model, AI and tests), or a proposed new family.
- **Basis:** the real animal behind it, with its classification where there is one. Myths have none, so for those it gives the origin.
- **Niche:** where it lives and what it does in a vanilla world.
- **Note:** design, tone or open questions.

## Ground rules (all species)

- **Vanilla friendly:** obey `mobGriefing`, take no spawns from vanilla mobs, use vanilla items for every interaction (feeding, taming, trading), and never touch player-placed chests or blocks unless tamed and asked.
- **Believable ecology:** every species gets a biome, a time of day, a food, a predator or rival, and at least one useful link with vanilla mobs or blocks. That link is what keeps them from being plain reskins.
- **High ecchi:** sometimes partially clothed or naturally uncovered (fur, feathers, scales, shell). Fanservice poses and animations. Adult proportions only.
- **Boys too:** where the myth is male (leshy, vodyanoy, gandharva, charro negro, tengu, the giants), make the male the default, or add both, as the mod already does for some species.
- **Living traditions:** Kachina, Thunderbird, Uktena, Wayob and Naga still belong to living religions. Keep them respectful, never sexualised, and check sources before release. Kachina is the most sensitive one (see the Celestials section).

---

## Fits an existing family (cheapest first)

| Species | Home | Basis | Niche | Note |
|---|---|---|---|---|
| **Ocelot** | Nekomata | *Leopardus pardalis*: Mammalia › Carnivora › Felidae | Jungle floor, crepuscular. Trusts you more if you have fed vanilla ocelots. Scares creepers like cats do. | Already on the TODO idea list. The easiest win. |
| **Naga** | Lamia | King cobra, *Ophiophagus hannah*: Reptilia › Squamata › Elapidae | Jungle temples and river banks. Guards the temple loot and lets in only players with good village reputation. | Different enough from the cobra lamia: a crowned hood and a guardian role. South and Southeast Asian myth. |
| **Horned Serpent Uktena** | Lamia (or Dragon) | Cherokee horned serpent with a blazing crystal (*Ulûñsû'tĭ*) on its forehead | Mountain rivers and caves near amethyst geodes. Gives you an amethyst shard if befriended. | Thunderbird's natural rival (see below). Respectful design: no "monster to farm". |
| **Tengu** | Harpy (crow base) | Karasu-tengu, from the large-billed crow *Corvus macrorhynchos*: Aves › Passeriformes › Corvidae | Windswept peaks and cherry groves. Spars with players (non-lethal duels). Its fan gives a gust like a wind charge. | Can reuse the `CrowHarpy` caching. Long-nosed *daitengu* could be a male variant. |
| **Thunderbird** | Harpy (eagle base) | Bald eagle, *Haliaeetus leucocephalus*: Aves › Accipitriformes › Accipitridae | Only seen in thunderstorms over mountains. Can draw lightning onto hostile mobs and never onto players or villagers. | Many North American nations. In myth it fights the horned serpents, which makes a nice ecology pair with Uktena. |
| **Vermilion Bird (Zhuque)** | Harpy (Phoenix variant) | Chinese Four Symbols: the south, fire and summer | Savanna and badlands. A warmer, southern take on the existing phoenix. | See the "Four Symbols" set below. |
| **Pegasus** | Centaur | Winged horse, *Equus ferus caballus* plus wings (Perissodactyla › Equidae) | Meadows near mountains. Glides more than it flies. In myth, Bellerophon tamed it with a **golden bridle**, so use gold horse armour or a golden apple. | |
| **Kelpie** | Centaur (water variant) | Scottish water horse | Rivers and lochs. Invites you to ride, then dives: a harmless prank that drops you in the water. Once bridled (saddle while it is wet) it becomes a strong swimmer mount. | Keep the "drowns riders" myth as a joke, not a death. |
| **Tapir / Baku** | Centaur | *Tapirus bairdii* or *T. indicus*: Perissodactyla › Tapiridae, the same order as horses, so it fits zoologically | Jungle and swamp, nocturnal, a browser. **Baku tie-in:** Japanese folklore turned the tapir into the dream-eater, so a nearby tapir stops **phantoms** spawning for sleeping players. | A strong vanilla hook. A Malayan tapir has a natural black-and-white "saddle" pattern. |
| **Banshee** | Undead | Irish *bean sí*, "woman of the fairy mound", whose keening foretells a death | Old-growth birch and taiga at night. Wails when a nearby player drops below 3 hearts, or when a raid is coming. A warning, not a curse. | Already on the TODO undead list. |
| **Dullahan** | Undead | Irish headless rider | Plains at night, riding a vanilla **skeleton horse**. In folklore it **fears gold**, so holding gold makes it flee. That is the reverse of piglins. | Head under the arm: needs a separate head bone in the model. |
| **Lich** | Undead | Undead sorcerer with a phylactery | Ancient cities and strongholds, rare. Casts slow and weakness. Breaking its phylactery block makes it peaceful instead of killing it. | Mini-boss tier. Good for the deep dark. |
| **La Llorona** | Undead | Mexican "weeping woman" who searches rivers | Rivers at night, weeping. **Redemption angle:** she escorts lost baby animals and baby villagers home to their village instead of harming anyone. | Recast the child-death theme gently. |
| **La Mano Peluda** | Undead (tiny) | Mexican-American "hairy hand" that hides under beds | Hides under beds in villages and steals one small item. Shy and harmless, a joke mob. | Low priority. It might not need a girl form at all. |
| **Vodyanoy** | Anuran | Slavic river spirit: a frog-faced old man. Common frog base, *Rana temporaria*: Amphibia › Anura › Ranidae | Deep river pools near villages, **male by default**. Friendly with fisherman villagers. Gives fish if you have not polluted the pool, and gets grumpy (pulls you under, then lets go) if you have. | The first anuran "boy". In folklore, millers left offerings for him. |
| **Kappa** | Lizardfolk (turtle) or Anuran | Japanese river yokai, said to come from the softshell turtle *Pelodiscus sinensis* (Testudines › Trionychidae) and the Japanese giant salamander | Rivers. Has a water dish (*sara*) on its head and grows weak if the dish dries out, so it is strong in rain or water. **Bow to it** (sneak) and it bows back and spills its dish. Loves melon slices or sea pickles, since Minecraft has no cucumbers. | |
| **Alligator snapping turtle** | Lizardfolk (turtle) | *Macrochelys temminckii*: Reptilia › Testudines › Chelydridae | Swamps and mangroves. It sits still with its mouth open and **lures fish with its worm-like tongue**, so it gives raw fish or salmon over time, like a passive fishing farm. Very long-lived, slow and tanky. | Shares the shell-retreat behaviour with the existing turtle. |
| **Black Tortoise (Xuanwu)** | Lizardfolk (turtle) | Chinese Four Symbols: the north, water and winter. A tortoise entwined with a snake | Frozen rivers and cold ocean. Snake tail plus turtle shell. | See "Four Symbols" below. |
| **Cipactli** | Lizardfolk (croc) | Aztec primordial sea monster, part crocodile, fish and toad. Croc base: *Crocodylus moreletii* (Crocodylia › Crocodylidae) | Warm deep ocean and mangroves, rare and huge, mostly asleep. In myth the gods made the earth from its body, so it could drop mangrove roots and mud. | Tone down "a mouth on every joint". |
| **Quetzalcoatl** | Dragon (feathered) | Mesoamerican feathered serpent of wind and learning. Feathers of the resplendent quetzal, *Pharomachrus mocinno* (Aves › Trogoniformes › Trogonidae), on a serpent body | Jungle peaks. Wind gusts. Teaches: gives XP when you bring it books or enchanted items. | Rare. |
| **Charro Negro** | Devil | Mexican black-clad horseman who offers gold at the cost of a soul | Roads and deserts at night, riding a black horse. Makes **deals**: gold or emeralds now, Bad Omen or a debt later. | **Male by default.** Reuses the devil family's deal and disguise ideas. |
| **Akaname** | Devil (yokai/oni line) or new | Japanese "filth licker" yokai that cleans bathhouses | Villages. **A tidy-up mob:** it eats dropped rotten flesh, spider eyes and junk items, and clears cobwebs. A cleaner, not a creep. | Design it as an adult with a long tongue and nothing weird. Low priority. |
| **Mimic** | Slime (or new) | Fantasy chest monster | **Only** sits in structure chest positions (dungeons, mineshafts), never in player chests. Open it and it bites once, then becomes a portable chest-friend if you feed it gold. | Make sure the gametests cover "never replaces a player chest". |
| **Leshy** | Flora (or Elf) | Slavic forest lord who leads travellers astray and can grow tree-tall or shrink to grass height | Old-growth spruce taiga, **male by default**. Tracks how many logs you cut and how many saplings you plant nearby. Replant and it is friendly and gives you saplings. Clear-cut and your compass spins and you get lost in fog. | In folklore, wearing your clothes inside out breaks his spell. Could map to swapping armour slots. |

## Proposed new families

### Giants (Jötnar)
Big, slow and neutral. Keep the hitbox at 3 blocks or less so they fit through caves and 2×2 tunnels without getting stuck.

| Species | Basis | Niche | Note |
|---|---|---|---|
| **Frost Giant** | Norse *hrímþursar* of Jötunheimr | Frozen and jagged peaks. Leaves powder snow behind it in snowy biomes only. Neutral, and herds goats. | A male and female pair. |
| **Fire Giant** | Norse fire giants of Muspelheim (Surtr) | Basalt deltas in the Nether. Immune to fire, smelts ores you hand it (a slow living furnace). | Rival of the frost giant: they fight if they meet. |

### Celestials
| Species | Basis | Niche | Note |
|---|---|---|---|
| **Angel** | Abrahamic angel | High mountains or the End highlands, very rare. Helps cure zombie villagers faster and heals tamed mobs. | Counterpart to the Devil family. They could be rivals. |
| **Gandharva** | Hindu and Buddhist celestial musician | Cherry groves. **Music:** reacts to jukeboxes and note blocks. Reuse `JukeboxSensing`. | Male by default. |
| **Apsara** | Hindu and Buddhist celestial dancer and water nymph | Rivers and lakes near cherry groves. Dances to music and pairs with the gandharva. | Classical art often sensualises apsaras. **Keep her modest:** a dancer's costume with full coverage. |
| **Kachina** | Hopi and Pueblo *katsinam* (spirit beings) | — | **Recommendation: do not add it as a monster girl.** Katsinam are sacred in a living religion, and Hopi communities have objected to commercial depictions. Swap it for a non-sacred concept or drop it. |

### The Horsemen (endgame, very rare)
Each rides a vanilla-coloured horse (white, red, black, pale). They are not bosses that wreck the world. Each one has a single domain, from Revelation 6.

| Species | Horse | Niche |
|---|---|---|
| **Reaper / Death** | Pale | Soul sand valley. Collects the "souls" of nearby dying mobs as XP. Trades soul lanterns. Calm, polite, inevitable. |
| **War** | Red | Shows up during raids and fights on the side that is winning. A strong reason to defend villages properly. |
| **Pestilence** | White | Swamps. Can **cure** poison and zombie villagers as well as cause them: plague-doctor framing. (In Revelation the white rider is "Conquest". The "Pestilence" name comes from pop culture.) |
| **Famine** | Black (carries scales) | **A grain merchant:** sells bread and wheat at harsh prices. Shows up near villages whose farms have failed or been trampled. |

### Fury (Erinyes)
Greek avengers of crimes against kin and broken oaths. **Hook:** reuse the villager gossip and reputation system. A fury hunts players who killed villagers, iron golems or their own tamed pets, and ignores everyone else. That makes it a moral counterweight that never bothers a peaceful player. There are three sisters, so three variants: Alecto, Megaera and Tisiphone.

### Crustaceans
| Species | Basis | Niche |
|---|---|---|
| **Spider Crab** | Japanese spider crab, *Macrocheira kaempferi*: Arthropoda › Crustacea › Malacostraca › Decapoda › Inachidae (Majoidea). Leg span up to about 3.7 m | Deep cold ocean floor. Its Majoidea relatives are **decorator crabs** that dress their shells, so it "wears" kelp, coral, sea pickles or sponge picked up from the seabed. That gives free cosmetic variety. |

Room in this family for the crab Mojang never added, coconut crabs and hermit crabs (which would swap shells by using shulker shells or nautilus shells).

### Small mammals and odd ones
| Species | Home | Basis | Niche |
|---|---|---|---|
| **Ratatoskr** | New "Rodent" family, or Jackalope | Red squirrel, *Sciurus vulgaris*: Mammalia › Rodentia › Sciuridae. In Norse myth it carries insults up and down Yggdrasil between the eagle and Níðhöggr | Forests. **Scatter-hoards seeds and forgets some, which plants saplings.** Reuse `CrowCaches`. Carries items between two players or villagers (a messenger), and sometimes swaps them for a prank. |
| **Chupacabra** | Lizardfolk (spiny, 1995 Puerto Rico version) or a canid line | The 1990s reptilian cryptid. The later "canine chupacabras" turned out to be coyotes (*Canis latrans*) with sarcoptic mange | Mountain edges at night. Hunts **wild goats only**. Never touches tamed or penned animals. Shy. A fun field-guide note about the mange. |
| **Ahuizotl** | Merfolk (otter line) | Aztec water creature with a hand on the end of its tail. Possibly inspired by the neotropical otter, *Lontra longicaudis* (Carnivora › Mustelidae) | Rivers. **Steals fish off your fishing line** with its tail hand. A prankster, and repellable with a lit lantern. |
| **Wayob** | Devil (disguise mechanic) or new | Maya *way* (plural *wayob*), spirit co-essences, often grotesque on Classic-era ceramics | Disguises itself as a nearby vanilla animal, like the existing succubus disguise. The tell is a wrong colour or the wrong sound. |

## Cross-family sets (optional and fun)

- **Four Symbols:** Azure Dragon (east) is the existing `LONG_DRAGON`, White Tiger (west) is the existing `TIGER_NEKOMATA` (a white variant), the Vermilion Bird (south) is new, and the Black Tortoise (north) is new. Matching biome directions give an achievement for finding all four.
- **Norse:** Ratatoskr, the Frost Giant, the Fire Giant and the existing Jormungandr.
- **Celtic night:** Banshee, Dullahan, Kelpie.
- **Mesoamerica:** Quetzalcoatl, Ahuizotl, Cipactli, Ocelot, Tapir and the Wayob, plus the Mexican folk set: La Llorona, the Charro Negro, La Mano Peluda and the Chupacabra.
- **Rivals:** Thunderbird against Uktena, Frost Giant against Fire Giant, Angel against Devil, Leshy against Vodyanoy (forest against water, a classic Slavic pairing).

## Suggested first batch after the playtest

**In game since 2026-10-21:** Ocelot, Tapir/Baku, alligator snapping turtle, Banshee (see TODO section 0c).

The best ratio of new content to new code, because each reuses a family that already exists:

1. Ocelot (Nekomata)
2. Tapir/Baku (Centaur): the phantom hook is a great vanilla tie-in
3. Kappa and the alligator snapping turtle (Lizardfolk turtle)
4. Vodyanoy (Anuran): the first monster boy
5. Banshee and Dullahan (Undead)
6. Tengu (Harpy crow)


---

## Third wave (2026-10-23)

Reference art: `D:\wamp64\www\images\creatures\<name>_<f|m>_<paint>.webp`. "Ref" gives the name there, or "–" when there is none yet. Rows marked *exists* are already in the game, so their refs only feed paints and touch-ups.

| Creature | Ref | Home | Basis | Niche |
|---|---|---|---|---|
| **Crab girl** | `crab_man`, `crabby`, `spider_crab` | Crustaceans (new) | True crabs, Decapoda › Brachyura | Beaches. Walks sideways and pinches. A hermit variant swaps shells (shulker or nautilus). |
| **Taniwha** | `taniwha` | Dragon (water serpent) | Māori guardian beings of rivers and the sea | Deep river pools and sea caves. A guardian of one place that protects people who respect it. *Living tradition: design with care.* |
| **Moo** | `moo` | Dinosaurs (new) | A sauropod (Sauropoda) with spikes, Māori-influenced | Plains and forests. Huge, slow, gentle, a living landmark. *Māori influence: respectful design.* |
| **Nightmarcher** | `nightmarcher` | Undead | The Hawaiian huakaʻi pō, processions of ancestral spirits | Marches a set path at night. Lying down or looking away lets them pass. *Sacred in a living tradition: no farming them, no jokes.* |
| **Adaro** | `adaro` (m and f) | Merfolk | Solomon Islands sea spirits | Rides waterspouts and throws flying fish. One of the few with a male ref. |
| **Genie** | `genie` | Spirits (new) | The jinn of Arabian folklore (the "genie" of the stories) | Deserts. Bound to a lamp item, grants three small boons a day. *The jinn are part of Islamic belief: keep it storybook.* |
| **Bahamut** | `bahamut` | Dragon (or Merfolk) | The world-fish of Arabian cosmology (the dragon in later games) | The deep ocean. Rare and enormous. |
| **Manticore** | `manticore` | Beasts (new, on the taur rig) | Persian man-eater: a lion's body and a scorpion's tail | Badlands. Shoots tail spines. |
| **Camel** | `camel` | Centaur | Bactrian camel, *Camelus bactrianus* (Artiodactyla › Camelidae) | Cold desert and steppe. Kin of vanilla camels, a long-haul mount. |
| **Dromedary** | (`camel`) | Centaur | *Camelus dromedarius* | Hot desert. Kin of vanilla camels. |
| **Girtablilu** | `girtablilu` | Bug (next to the scorpion) | Mesopotamian scorpion-man gate guardians | Desert ruins. Guards a doorway. |
| **Lamassu** | `lamassu` | Centaur (winged bull) | Assyrian protective deity | Guards village gates and deters raiders. |
| **Oryx** | `oryx` | Centaur | *Oryx gazella* / *O. leucoryx* (Bovidae) | Desert, in herds, with long straight horns. |
| **Dugong** | `dugong` | Merfolk | *Dugong dugon* (Sirenia › Dugongidae) | Warm shallow seagrass. A gentle grazer that eats seagrass. |
| **Golem** | `golem` | Constructs (new) | Golem of Jewish folklore; kin of the vanilla iron golem | Villages. A defender who befriends iron golems. |
| **Dolphin** | `dolphin` | Merfolk, *exists* | `dolphin_mermaid` | Refs feed paints. |
| **Ziz** | `ziz` | Harpy | The giant bird of Jewish legend | Mountain peaks, huge. |
| **Lilith** | `lilith` | Devil | A night demoness of Mesopotamian and Jewish lore | Nocturnal. Keep her High ecchi (no succubus overlap). |
| **Anubis** | `anubis` | Canids (new) | Egyptian god of the dead, a jackal | Desert pyramids. Guards tombs, nocturnal. |
| **Wadjet** | `wadjet` | Lamia (cobra) | Egyptian cobra goddess, protector | Desert rivers. Protects her player. |
| **Spinosaur** | `spinosaur` | Dinosaurs | *Spinosaurus aegyptiacus* (Theropoda › Spinosauridae) | Rivers. A fisher, half aquatic. |
| **Amarok** | `amarok` | Canids | The giant lone wolf of Inuit stories | Snowy plains. Hunts alone at night. |
| **Polar bear** | `polar_bear` | Bears (new) | *Ursus maritimus* (Carnivora › Ursidae) | Snow and ice. Kin of vanilla polar bears, fishes. |
| **Grizzly bear** | – | Bears | *Ursus arctos horribilis* | Taiga. Salmon and berries. |
| **Black bear** | – | Bears | *Ursus americanus* | Forests. Climbs, raids berry bushes. |
| **Panda** | – | Bears | *Ailuropoda melanoleuca* | Bamboo jungle. Kin of vanilla pandas, eats bamboo. |
| **Keelut** | `keelut` | Canids | The Inuit hairless dog spirit | Snowy plains at night. Leaves no tracks. |
| **Penguin** | `penguin` | Harpy (flightless) | Spheniscidae | Snowy beaches. A fast swimmer, walks slowly. |
| **Koala** | – (`drop_bear` for the joke variant) | Marsupials (new) | *Phascolarctos cinereus* (Diprotodontia) | Climbs trees and sleeps 20 hours a day. The "drop bear" is a rare joke variant. |
| **Min Min Lights** | `min_min_lights` | Jellies (new) | Outback mystery lights, as a floating glowing jellyfish | Badlands and savanna at night. Floats and glows, leads you astray. |
| **Jellyfish** | – | Jellies | Scyphozoa (Cnidaria) | Warm ocean. Stings, glows. |
| **Sea turtle** | `sea_turtle` | Lizardfolk, *exists* | `turtle_lizardfolk` | Refs feed paints. |
| **Kangaroo** | `kangaroo` | Marsupials | *Macropus* (Macropodidae) | Savanna in mobs. Kick-boxes, carries her joey in the pouch. |
| **Bunyip** | `bunyip` | Merfolk (or Lizardfolk) | The water monster of Aboriginal stories | Billabongs and swamps at night. Its booming call warns you off. *Living tradition.* |
| **Tiddalik** | `tiddalik` | Anuran | The frog of the Dreaming story who drank all the water | Drinks a pond dry. Made to laugh, she lets the water flow back. *Living tradition.* |
| **Wasp** | – | Bug | Vespidae | Hedges and meadows. Stings, builds paper nests. |
| **Hornet** | – | Bug | *Vespa* | Forests. Bigger, hunts bees. Enemy of the bee girls. |
| **Curupira** | `curupira` | Elf (or Flora) | Brazilian forest protector with red hair and backward feet | Jungle. Protects animals from hunters; her backward footprints confuse trackers. |
| **Capybara** | `capybara` | Rodents (new, with Ratatoskr) | *Hydrochoerus hydrochaeris* (Rodentia › Caviidae) | Rivers and swamps. Every animal likes sitting next to her. |
| **Toucan** | `toucan` | Harpy | Ramphastidae | Jungle canopy. A fruit eater with a big beak. |
| **Armadillo** | `armadillo` | Armoured (new; kin of the vanilla armadillo) | Cingulata | Savanna and badlands. Rolls up when scared and drops scutes. |
| **Fishman** | `fishman` (m and f) | Merfolk (a boy) | Fish folk of many myths | Ocean ruins. A good first merfolk boy. |
| **Argentinosaurus** | `argentinosaurus` | Dinosaurs | *Argentinosaurus huinculensis* (Titanosauria) | Plains. The largest of them all, very rare. |
| **Amaru** | `amaru` | Dragon | The Andean (Inca) serpent-dragon of water and the underworld | Mountains and rivers. Brings rain. |
| **Pishtaco** | `pishtaco` | Undead (or Devil) | Andean bogeyman folklore | Mountains at night. Tone her down to a sinister but non-gory stalker. |

**Decided (2026-10-24):** the Kachina and the Rainbow Serpent stay out. Bears join kobolds and the other canids in one family, the **canid girls** (Anubis, Amarok, Keelut, kobolds, polar, grizzly and black bears, panda).

**New families this wave would need:** Crustaceans, Dinosaurs, Spirits, Beasts (taur-rig chimeras), Constructs, Canid girls (with the bears), Marsupials, Jellies, Rodents, Armoured. The best value first is the ones with vanilla kin, since Kinship works for them at once: Bears (polar bear, panda), Camels, the Golem (iron golem), Armoured (armadillo).

## Harmontide paints (proposal)

Nearly every creature in the references comes in the same seven **colour paints**: blue, red, green, yellow, purple, black and inverted. Plush, realistic and polygon are art styles, not recolours, and don't carry over to Minecraft textures. Proposal:
- A wild girl has a rare chance (1 in 64?) to spawn **painted** in one of the seven, like a shiny. The paint is saved on her and her daughters may inherit it.
- The paints are generated offline: every species' default texture recoloured (hue-shifted, keeping the shading), plus an inverted copy. That is about 7 × 104 textures, roughly 25 MB.
- Open questions: how rare? Inherited or not? A way to repaint (a rare item)?

---

## Fourth wave (2026-10-24)

Picked for reusing families that already exist. The attributes are the design notes of 2026-10-24.

| Species | Home | Design |
|---|---|---|
| **Tengu** | Harpy (crow base) | Neutral. Fights with her fan: wind attacks with high knockback and low damage. On patrol she scans hard for hostile mobs (creepers and endermen too) and pushes them out of her territory. She lives high in the sky like the albatross, so you meet a wild one when she comes down to drink at water. |
| **Penguin** | Harpy (flightless, short legs) | Snowy and icy biomes, in big groups. A good swimmer who hunts fish and shares her catch once befriended. Now and then you meet one alone, walking off by herself (Werner Herzog's penguin). Wild orca mermaids hunt her, and she attacks wild koi mermaids. |
| **Toucan** | Harpy | Jungle. Eats fruit. Clacks her big beak to talk, and toucans that meet hold a clack-off: they clack at each other for fun. |
| **Camel, Dromedary** | Centaur | Bactrian on cold desert and steppe, dromedary on hot desert. Anyone can ride them, with no friendship needed. They can breed with vanilla camels, and a bucket milks them. |
| **Kelpie** | Centaur (water) | Rivers. Ridden with a saddle, and faster than a boat. Neutral by day. At night she attacks players unless she is befriended. |
| **Wadjet** | Lamia (on two legs, nekomata body with a cobra hood) | Desert rivers. Gives buffs to any player passing by, and to the girls following them. Very aggressive towards hostile mobs. Often hands befriended players emeralds. Name her **Nekhbet** and her buffs and her damage against mobs go up. |
| **Hornet** | Bug | Forests. Attacks players who come too close. Unless befriended, she attacks bees and bee girls. On patrol she is a fierce fighter against hostile mobs. Omnivore: she can be befriended with meat or fruit, and gives paper and clay. |
| **Dugong** | Merfolk | Warm shallow seagrass, nearly always in pairs. She is befriended with seagrass and gives sea loot. |

### Encounter: the injured girl
- Now and then a wild girl spawns **injured**: low health, slow, and she cannot fight. An injured flier cannot fly (a hornet with a broken wing).
- She is rare for most species and common for hornets.
- A **healing balm** fixes her even though she is not your friend yet. Friendship jumps up, and a hornet goes straight to full friendship.

---

## Fifth wave (2026-10-24)

Agreed on 2026-10-24. It opens one new family, the **canid girls** (with the bears).

| Species | Home | Design |
|---|---|---|
| **Wolf** | Canids (new) | Taiga and forests, in packs led by an alpha. Wild, the pack hunts sheep and rabbits. Bones win her over. Kin of vanilla wolves: once she is befriended, her player's tamed wolves join her fights. |
| **Polar bear** | Canids | Snow and ice. Hunts seal mermaids and penguin harpies. Fishes at open water. Guards her cubs as fiercely as a vanilla polar bear. Kin of vanilla polar bears. |
| **Panda** | Canids | Bamboo jungles. Born with a personality like vanilla pandas: normal, lazy, worried, playful, aggressive, weak (brown is a colour). Eats bamboo and sneezes. Kin of vanilla pandas. |
| **Anubis** | Canids | Deserts at night. Weighs the heart of every player who comes near against the feather: anyone who killed villagers or iron golems is condemned and hunted. Light hearts may open the tomb chests she guards. |
| **Pegasus** | Centaur | Meadows near mountains. Tamed with a golden bridle (gold horse armour) or golden apples. Ridden, she leaps and glides: no true flight. |
| **Lich** | Undead | Deep dark. Hostile, casts slowness and weakness. While her phylactery stands she cannot die: she reforms beside it. Break it and she becomes peaceful. Befriended, she trades experience for books. |
| **Mimic** | Slime | Waits as an unopened structure chest (never a player's chest). Open her and she bites once, keeping the loot inside her. Gold wins her over, and she becomes a walking chest. |
| **Vodyanoy** | Anuran (male) | Deep river pools near villages, friends with fishermen. Gives fish while his pool stays clean. |

**Also:** anurans count vanilla frogs as kin and can breed with them.

---

## Sixth wave (2026-10-24)

Agreed on 2026-10-24. It opens the **rodent** family and completes the Four Symbols.

| Species | Home | Design |
|---|---|---|
| **Kobold** | Canids | Caves and mountains. A dog-girl miner (cobalt is named after kobolds). Befriended, she sniffs out the nearest diamond, gold or emerald ore and points the way. She yelps when the block you are about to dig holds up sand or gravel, and she smells mimics: a mimic chest near her rattles. |
| **Grizzly bear** | Canids | Taiga. Fishes salmon from rivers, raids full beehives for honey (the bees sting her for it), and hibernates while it snows. Kin of vanilla polar bears, the closest bear Minecraft has. |
| **Capybara** | Rodents (new) | Rivers and swamps. The calmest animal alive: no wolf, fox, cat or ocelot hunts within 8 blocks of her, and idle girls nearby like to sit beside her. |
| **Ratatoskr** | Rodents | Forests. Scatter-hoards seeds and saplings; the ones she forgets sprout into trees. A messenger: hand her an item and she carries it to the nearest other player she is fond of (now and then swapping it as a prank). |
| **Leshy** | Flora (male) | Old-growth taiga. The forest lord, and the Vodyanoy's rival. He counts the logs you cut and the saplings you plant near him: a planter gets saplings and apples, a clear-cutter gets lost in his fog. Meets a vodyanoy, and they bellow at each other. |
| **Black Tortoise (Xuanwu)** | Lizardfolk | Frozen rivers. A tortoise shell with a serpent coiled round it. Arrows glance off her shell. The north of the Four Symbols. |
| **Vermilion Bird (Zhuque)** | Harpy (phoenix line) | Savanna and badlands. Summer's bird: her fire ripens crops near her instead of burning them. The south of the Four Symbols. |
| **La Llorona** | Undead | Rivers, at night, weeping. She escorts lost young animals and village children home to their own kind. Bring a lost one home with her and she stops weeping for a night. |

**Four Symbols advancement:** befriend the long dragon (east), a white tiger nekomata (west), Zhuque (south) and Xuanwu (north).

## Seventh wave (2026-10-25)

Agreed on 2026-10-25. Four new families (**crustaceans, marsupials, the armoured, constructs**), two newcomers to old families, and homage variants.

| Species | Home | Design |
|---|---|---|
| **Crab** | Crustaceans (new) | Beaches. Walks sideways. Neutral: whoever hits her first gets pinched (Slowness). A decorator variant dresses her shell in kelp and coral, like the spider crab's relatives. |
| **Hermit crab** | Crustaceans | Beaches. Lives in a borrowed shell: give her a nautilus or a shulker shell and she moves in (you get the old one back). Pinches whoever hits her first. |
| **Kangaroo** | Marsupials (new) | Savanna and badlands, in mobs. Her joey rides in her pouch until it is grown. Kicks with big knockback. The **outback** variant (a red hoodie and sunglasses) is a nod to Kangaroo Jack. |
| **Armadillo** | Armoured (new) | Savanna and badlands. Rolls up into a ball when scared (no damage gets through); befriended, she rolls into monsters. Kin of vanilla armadillos; sheds scutes. The **ranger** variant (rust-orange, a cowboy hat and a red scarf) is a nod to Dillon's Rolling Western. |
| **Golem** | Constructs (new) | Near villages. A clay golem with more health than an iron golem (120). Guards villagers; iron golems are her kin (no longer Frankenstein's monster's). When she falls she breaks into pieces. |
| **Jellyfish** | Mollusks (not a mollusk, but she drifts with them) | Warm ocean. Glows at night, stings what bumps into her; friends swimming near her see in the dark. |
| **Ahuizotl** | Merfolk (otter line) | Rivers. The hand on her tail steals fish off your line unless you hold a lantern; befriended, she fishes for you. |

**Homage variants** (Gregory Horror Show and friends; generic names, no logos, rolled rarely in the wild):

| Species | Variant | Look |
|---|---|---|
| Cactus girl | `gunman` | Yellow straw sombrero with a green band, big black mustache, red bandana, maroon zigzag poncho with bullet holes, bandolier (Cactus Gunman) |
| Cactus girl | `blossom` | Orange straw sombrero, dark green braids with red bows, red bandana, cow-print top over a blue zigzag poncho, a lasso, puffy blue sleeves, freckles, red eyes (Cactus Girl) |
| Living doll | `lost` | Red curls with purple bows, freckles and a puppet jaw, patched green dress, round red feet, and Katie's blue face with zigzag teeth on the back of her head (Lost Doll) |
| Ocelot nekomata | `stitched` | Grey-beige and sewn together, a purple patch over half her head, purple ears, red eyes, a stitched muzzle, red and blue striped top (no lettering), khaki shorts (Neko Zombie) |
| Salamander | `nurse` | Pink with magenta spots, a lizard snout, white nurse's dress and red-cross cap and armbands, black shoes, a huge syringe in her hand (Catherine) |
| Wolf girl | `mummy` | Bandaged head with brown fur round the eyes, a muzzle, red shirt and blue overalls with gold buttons, blue shoes, a cleaver stuck in the head (Mummy Dog) |
| Anubis | `mummy` | Bandaged head, a muzzle, grey suit and dark trousers, black shoes, a machete stuck in the head (Mummy Papa) |

The mummies have the show's blood (drips from the blade down the face, spatter on the blade and on Papa's suit), on these two variants only. The show's lettering is left off.

**Paints:** copper (weathers stage by stage like copper blocks; honeycomb waxes it, an axe scrapes it back), glow (neon markings that glow in the dark), ink (manga line art), magma (glowing cracks), and the seasonal pumpkin (October) and candy cane (December).

## Eighth wave (2026-10-25, in the game)

| Species | Home | Design (with the user's notes) |
|---|---|---|
| **Unicorn** | Centaur | Kin of horses like the pegasus. Her horn cures poison and wither on friends; only players with no villager kills may come near (Anubis's heart check). |
| **Black bear** | Canids | Forests. Climbs trees and raids sweet berry bushes. **Scares easily.** |
| **Koala** | Marsupials | Sleeps most of the day and only eats leaves. A rare **drop bear** variant drops on you from trees as a joke. |
| **Gecko** | Lizardfolk | Climbs walls. **Fast little buggers, curious, and they like fruit.** Sheds her tail when hit (it grows back). |
| **Flamingo** | Harpy | Stands on one leg in warm shallows. Her pink fades without fish and comes back with it. |
| **Firefly** | Bug | Meadows at night; fireflies blink in sync. A jar of one is a light. **Advancement "Why?"** for catching her or a fairy in a jar. |
| **Mummy** | Undead | Desert pyramids, guards the sarcophagus (with Anubis); hits unravel her bandages (string and paper). **Befriended, she can be unravelled until she is dressed skimpily, then hurries and spins, wrapping herself up again.** |
| **Chupacabra** | Lizardfolk (spiny) | Mountain edges at night; hunts wild goats only. **Fed, she lingers around the player; she can never be given commands; at high friendship she often leaves half a stack of mutton.** |

Also agreed and built: the flamingo as a harpy (her shade follows her diet: pink, pale, white), the four paints frost, sakura, stained glass and sepia, and the "Why?" advancement for jarring a firefly or a fairy.

## Ninth wave (2026-10-25, in the game)

| Species | Home | Design |
|---|---|---|
| **Sheep** | Minotaur | Shears take her wool, which grows back once she grazes; dye her like a sheep. Vanilla sheep follow her. |
| **Highland** | Minotaur | Shaggy, a fringe over her eyes; never freezes, walks on powder snow. |
| **Yak** | Minotaur | High mountains. A friend's chest goes on her back: two rows of cargo. Never freezes. |
| **Bat** | Bats (new family, volume XXVI) | Roosts upside down from ceilings by day; befriended, she pings and monsters near her friend glow through walls. Fruit bat variant with a fox face. Kin of vanilla bats. |
| **Dark elf** | Elf | The deep dark. Sets off no sculk; a Warden near her spares her friends (the Warden is her kin). Trades echo shards and sculk. |
| **Medusa** | Lamia | Her gaze slows strangers and monsters nearly to a stop; befriended, she wears a blindfold. |
| **Oni** | Devil | Night mountains, an iron club, hits hard. Cocoa beans drive her off (Setsubun); a honey bottle calms her; befriended, she guards your door at night. |
| **Mantis** | Bug | Prays still, then lunges at silverfish, endermites and spiders. The orchid mantis vanishes among flowers. |
| **Ender slime** | Slime | The End. Teleports from blows and water, moves loose blocks around. Kin of endermites. |
| **Wolpertinger** | Jackalope | Antlers and little wings: double jumps and glides. Swaps your boots for leather ones (feed her to get them back). |
| **Venus flytrap** | Flora | Swamps, rooted. Snaps shut on whatever steps into reach; befriended, only monsters. |
| **Siren** | Merfolk | Rocky coasts. Her song draws strangers toward her; friends swimming near her get Conduit Power. |

Paints: opal, chrome, aurora and camo (35 in all). The firefly's blink is now a smooth pulse.

## Tenth wave (2026-10-25, in the game): Harmontide's own creatures

From the reference art (`HARMONTIDE_ROSTER.md` tracks which are done).

| Species | Home | Design |
|---|---|---|
| **Tusktaur** | Centaur | A woolly mammoth taur of the snowy plains. Her tusk sweep knocks back everything in front of her; friends ride her; never freezes. |
| **Aurorantler** | Centaur | A deer taur of the snowy taiga whose antlers glow at night; Night Vision for her friend. |
| **Rhiwool** | Minotaur | A woolly rhino: a straight-line charge; shear and dye her like the sheep. |
| **Meloncollie** | Canid | A watermelon herding dog: drives stray farm animals to the nearest fence gate, plants melon seeds. |
| **Needlehog** | Rodent | A hedgehog: rolls into a ball of quills, hunts silverfish, keeps the paper and books she finds. |
| **Clockhare** | Jackalope | A hare in a top hat: her watch chimes at dawn and dusk and warns of phantoms; Haste for her friend. |
| **Axolume** | Merfolk | A glowing axolotl of the lush caves; Regeneration for her friend. |
| **Aegis boar** | Armoured | A boar in plate armour who takes a third of every blow meant for her friend. Hoglins are her kin. |
| **Blaze cat** | Nekomata | A Nether cat with burning paws: fireproof, lights campfires and candles, Fire Resistance for her friend. Blazes are her kin. |
| **Gargoyle** | Construct | Stone by day (nothing hurts her), flies and fights at night. |
| **Coin magpie** | Harpy | Hoards shinies; pay her a gold nugget for something from her hoard. |
| **Trickster raven** | Harpy | Pranks strangers with monster voices; befriended, she lures monsters away from her friend. |

Paints: plush, pixel, origami and gummy (39 in all).

## Eleventh wave (2026-10-26, in the game): Harmontide's helpers

From the reference art. The Thunderbird is left out on purpose (a living tradition, like the Kachina).

| Species | Home | Design |
|---|---|---|
| **Anvil tortoise** | Lizardfolk | An iron-shelled tortoise with an anvil on her back. Mends tools and armour for their repair material, no experience; too heavy to push. |
| **Mailtoad** | Anuran | A postwoman toad. Give her an item, then a paper renamed to a player's name: she delivers it. |
| **Gliderune** | Marsupial | A sugar glider. Glides; befriended, she rides on your shoulder and gives you Slow Falling. |
| **Slothren** | Armoured | A mossy sloth. Hangs from branches by day; spreads moss and flowers; befriended, she grows your crops. |
| **Coatimano** | Rodent | A coati with a brush of a nose. Digs out suspicious sand and gravel and brings you the find. |
| **Waterlil** | Nekomata | A big cat's head resting on her own lily pad, a small bloom on top, her little body hidden under the pad. Lily pads grow under her and her friend on water. |
| **Rookmourn** | Harpy | A rook in a plague doctor's hat. Cures poison, nausea, hunger and wither nearby; a golden apple has her cure a zombie villager. |
| **Cassowrath** | Harpy | A cassowary. Territorial, kicks hard; befriended, a stick's patrol order has her guard a spot. |
| **Talosling** | Construct | A bronze clockwork girl with a wind-up key. Redstone winds her; she guards her friend; run down, she sits still. |
| **Mantarrow** | Merfolk | A manta ray. A friend rides her across the water, fast; she leaps. |
| **Sphinx** | Centaur | A winged lioness. Riddles answered with an item: a prize if right, a pounce if wrong. Never ridden. |
| **Brownie** | Elf | A tiny house spirit. At night she tidies chests and puts loose items away; paid in milk and bread. |

Paints: synthwave, cookie, cloud and the seasonal valentine (February only), so 43 in all.

## Twelfth wave (2026-10-26, in the game)

From the reference art. Set aside as living traditions: Amaru, Apu, Supay, Wayob, Pukwudgie, Mimi spirits, Nargun,
Yara-ma-yha-who, Menehune, Akhlut, Ijiraq, Keelut and the peace-pipe bison.

| Species | Home | Design |
|---|---|---|
| **Cheshire cat** | Nekomata | Ringed in two colours, with a huge glowing grin. Unless you look straight at her, only the grin shows. Befriended, she blinks back to your side. |
| **Kesaran pasaran** | Flora | A little dandelion-seed girl under her puff, drifting on the wind. Luck for her friend; sugar makes her split (up to three). |
| **Tsuchinoko** | Lamia | A stubby, fat snake who rolls downhill like a wheel. Loves honey; golden presents. |
| **Platypus** | Merfolk | A duck-billed cap and a beaver tail. Underwater she makes monsters near her friend glow; her spur poisons strangers. |
| **Moonseal** | Merfolk | A night-blue seal with glowing crescents and little antlers. Dolphin's Grace; on a full moon she sings and her friend regenerates. |
| **Marmotaur** | Rodent | A stout marmot with golden ram's horns and an amber pendant. Whistles when monsters come; they glow. |
| **Bambadger** | Rodent | A bamboo-striped badger with a flute that makes bamboo and sugar cane grow. |
| **Dodobold** | Harpy | A small, fat, flightless dodo in an ornate harness with saddlebags (18 slots) for her friend. |
| **Boghare** | Jackalope | A mossy swamp hare with a lily flower and pink-tipped ears. Jump Boost for her friend; frogs are her kin. |
| **Gremlin** | Devil | A little green tinkerer. Wild, she pranks levers and doors at night (a cookie stops her); befriended, furnaces cook twice as fast. |
| **Karakasa obake** | Devil | A girl under an umbrella hat who hops on one leg; her umbrella catches projectiles aimed at her friend. |
| **Alebrije** | Dragon | A winged folk-art chimera in bright dots and zigzags. Flies; dyes sheep; gives dyes. |

Paints: porcelain, denim, moss and the seasonal easter (April only), so 47 in all. The waterlil is now only a big
cat's head resting on her lily pad, her little body hidden under it.

## Thirteenth wave (2026-10-26, in the game)

Eight Harmontide originals from the reference art and four spirits of Japanese folklore.

| Species | Home | Design |
|---|---|---|
| **Rust otter** | Merfolk | A dark otter hung with copper charms. Scrapes the green off weathered copper near her friend; raw copper presents. |
| **Tin squirrel** | Construct | A clockwork squirrel of riveted tin. Breaks worn iron and gold gear down into nuggets; hoards nuggets. |
| **Peaflare** | Harpy | A peacock: when monsters come near her friend, her train rises into a great eyed fan and dazzles them. |
| **Wattlefox** | Kitsune | A blue fox wreathed in golden wattle. Fireproof; puts out fires near her friend; plants acacia. |
| **Scarabronze** | Bug | An armoured bronze scarab. Rolls rotten flesh into bone meal; Haste for her friend at sunrise. |
| **Datebat** | Bat | A fruit bat with date-palm fronds. Roosts by day; at night the crops near her grow. |
| **Snowloper** | Nekomata | A snow-leopard cub with snowflake rosettes. Walks on powder snow; Speed on snow for her friend. |
| **Jaglare** | Nekomata | A blue jaguar with a lion's mane. Drops on prey from the treetops; her roar weakens monsters. |
| **Squirricerat** | Rodent | A squirrel with a triceratops' frill and horns. Charges monsters; plants trees. |
| **Kamaitachi** | Rodent | The sickle weasel. Fast; her gusts cut grass, leaves and cobwebs round her friend. |
| **Momoshika** | Centaur | A deer taur with peach blossoms in her antlers. Regeneration by day; golden apples now and then. |
| **Chochin-obake** | Devil | A girl under a living paper lantern with one great eye. Her light follows her friend at night. |

Paints: tartan, watercolour, hologram (see-through) and the seasonal fireworks (January only), so 51 in all.

## Fourteenth wave (2026-10-27, in the game)

Seven Harmontide originals from the reference art and five spirits of folklore.

| Species | Home | Design |
|---|---|---|
| **Gribboar** | Armoured | A shaggy blue boar who roots up truffle finds: mushrooms, potatoes, carrots, now and then gold. |
| **Silkcoon** | Rodent | A silky raccoon who washes the dye out of wool, carpets, beds, terracotta, glass and leather by water. |
| **Bronzejaw ox** | Minotaur | A brass-plated ox: led on a lead, she ploughs the ground into farmland. |
| **Cocoskink** | Lizardfolk | A leafy skink with bells: grows cocoa pods on jungle logs near her friend. |
| **Prairhorn** | Centaur | A pronghorn taur: the fastest ride in the mod. |
| **Halberelk** | Centaur | An elk taur with bladed antlers: fells the whole tree when her friend cuts a log. |
| **Vulphea** | Kitsune | A fire fox: fireproof, a burning bite, and once a day she rises from her ashes. |
| **Cait Sìth** | Nekomata | The Highland fairy cat: keeps phantoms away; milk brings her friend Luck. |
| **Cù-sìth** | Canid | The green fairy hound: at her third bark the monsters near her friend flee. |
| **Yeti** | Canid | A huge snow woman: snowballs at monsters, snow golems now and then. |
| **Undine** | Slime | Living water: waters farmland, fills cauldrons, Water Breathing for her friend. |
| **Ittan-momen** | Devil | A girl trailing a cotton ribbon: she flies, and her friend can ride her through the air. |

Paints: mosaic, chalk, leopard and the seasonal autumn (November only), so 55 in all.

## Fifteenth wave (2026-10-27, in the game)

Eight Harmontide originals from the reference art and four spirits of folklore.

| Species | Home | Design |
|---|---|---|
| **Crocora** | Lizardfolk | A round little blue crocodile in a gold collar: she hauls her drowning friend up to the surface. |
| **Iron Kobold** | Lizardfolk | A kobold smith in iron plate with a big hammer: mends iron and chainmail gear for an iron ingot. |
| **Mooskuh** | Minotaur | A calf grown all over with moss: the stone she rests on turns to moss; she gives milk. |
| **Bellmare** | Centaur | A blue donkey taur with goat's horns and bells: the farm animals near her follow her. |
| **Vicunero** | Centaur | A vicuña taur in a woven saddle blanket: she spits at the monsters near her friend. |
| **Rockhyrax** | Rodent | A stocky hyrax with blue scales: she whistles when a creeper creeps up, and it glows. |
| **Riverdillo** | Armoured | A river armadillo with a fish's fin on her tail: she catches fish for her friend. |
| **Reeffin** | Merfolk | A reef mermaid crowned with coral: she grows coral and sea pickles on the sea floor. |
| **Caraviper** | Lamia | A hooded blue cobra with gold glyphs: her venom blinds and poisons monsters. |
| **Bakeneko** | Nekomata | The Japanese ghost cat, will-o'-wisps round her: they light the night. |
| **Rokurokubi** | Devil | The yōkai whose neck stretches at night: from up there she spots every monster near her friend. |
| **Huli Jing** | Kitsune | The Chinese fox spirit with nine tails: villagers trade cheaper with her friend. |

Paints: checkered, sunset, thunder (its lightning bolts glow) and burlap, so 59 in all.

Set aside as living traditions: alux (Maya), curupira, mapinguari and ypupiara (Indigenous Brazil), the horned serpent
uktena (Cherokee), and terniq (an Inuit name).

## Sixteenth wave (2026-10-27, in the game)

Eight Harmontide originals from the reference art and four creatures of folklore. The originals were first built from
their names alone (the art was not reachable), then reworked against `harmontide-creature-images/` (their blue looks).

| Species | Home | Design |
|---|---|---|
| **Jorogumo** | Arachne | The binding bride: a girl in a web-printed kimono with four red eyes and eight long black-and-gold legs fanned out behind her back (her own two-legged rig, from the user's reference). At night her webs bind the monsters near her friend. |
| **Dunefennec** | Kitsune | A night-blue fennec with gold crescents, a purple saddle-cloth, a jewelled collar and a bell: her friend runs fast on sand, and husks near her lose interest in her friend. |
| **Floodcrab** | Crustacean | A blue-grey river crab with red eyes under a ridged stone fan of a shell, grown with moss and barnacles and hung with brass bells: she turns the dirt beside the water into mud. |
| **Temple Mantis** | Bug | Lapis blue and gold, a jewelled crown and a gold ring arch behind her head: she cuts the tripwires of old temples near her friend. Wires a player strung up are left alone. |
| **Sword Koi** | Merfolk | A lapis-blue koi patched with white, gold-trimmed armour and a katana across her back: a drowned she defeats always drops what it held. |
| **Squidkin** | Mollusk | A sky-blue squid girl, long hair curling into tentacles over a skirt of eight more (the octopus' rig, no cap): when her friend is hurt, ink blinds the monsters and hides her friend. |
| **Reedbull** | Minotaur | A shaggy lapis-blue bull: gold-banded horns with tassels, a white brow rune, a woven blanket, a pendant and gold anklets. She plants sugar cane by the water. |
| **Pumpkingull** | Harpy | A sky-blue and white gull dressed in a pumpkin, with a little pumpkin cap and vine leaf: endermen near her friend take no offence at being looked at. |
| **Condorune** | Harpy | A lapis-blue condor on the crow's rig with a cream ruff and gold beadwork: she catches her friend when they fall. |
| **Kikimora** | Devil | The Slavic house spirit in a sarafan and a polka-dot headscarf: at night she shuts the doors left open near her friend. |
| **Cockatrice** | Dragon | The rooster-dragon with a red comb and feathered wings: her glare stops a monster almost dead. |
| **Valkyrie** | Elf | Mail, a winged helm and great white wings: when her friend dies near her she keeps what they dropped and gives it back. |

Paints: neon (its tubes glow), zebra, terrazzo and the seasonal clover (March only), so 63 in all. Porcelain, denim and
sakura were already taken, so the proposed paints changed.

## Seventeenth wave (2026-10-27, in the game)

Eight Harmontide originals from the reference art (their blue looks) and four creatures of folklore (the dryad, amarok,
yurei and lamassu, from their reference looks).

| Species | Home | Design |
|---|---|---|
| **Censer Lion** | Nekomata | A lapis lion in gold filigree with a smoking censer at her hip: her friend near her sleeps soundly, so no phantoms come, and phantoms hunting her friend lose interest. |
| **Conch Mail Turtle** | Lizardfolk | A little sea turtle in a scaled blue shell with a conch's spire rising from it: her friend underwater near her has Conduit Power. |
| **Date Palm Beetle** | Bug | A round blue beetle with dates and palm leaves in her hair: she shares a date with her hungry friend. |
| **Dustbison** | Minotaur | A fluffy blue bison calf with cream tips and gold spots: she rolls her burning friend in the dust, putting the fire out. |
| **Frondbat** | Bat | A blue bat with leafy wings and a crown of white flowers: she shakes saplings, sticks and apples from the leaves near her friend. |
| **Sawfin** | Merfolk | A sawfish girl in blue barding with a fleur-de-lis shield and a toothed saw of a snout: Haste II for her friend underwater. |
| **Stagel** | Centaur | A fawn taur, cream dappled with blue, with budding antlers: Jump Boost II for her friend. |
| **Klabuster** | Slime | The “Klabusterbeere”: a glossy brown swirl of a slime dangling on a strand from a tuft of hair (her other looks: blue, green, purple, gold). The crops near her friend grow. |
| **Dryad** | Flora | The Greek tree nymph, a girl of pale blue wood with leafy branches for antlers: the saplings near her friend grow. |
| **Amarok** | Canid | The giant lone wolf of Inuit legend, slate grey and shaggy: her friend's tamed wolves near her fight harder. |
| **Yurei** | Undead | The Japanese ghost in a school uniform, long black hair, legs fading into a white wisp; she floats. At night the monsters near her friend grow weak. |
| **Lamassu** | Centaur | The winged, horned, lion-bodied guardian of Assyria's gates, long grey curls: Resistance I for her friend. |

Paints: polka (white dots on bright colours), circuit (a green circuit board whose traces glow), giraffe (brown patches
in a net of cream lines) and the seasonal watermelon (July only), so 67 in all.

## Eighteenth wave (2026-10-28, in the game)

Eight Harmontide originals from the reference art (their blue looks) and four creatures of folklore (Bastet, the
rusalka, the ushi-oni and the domovoi).

| Species | Home | Design |
|---|---|---|
| **Spoodr** | Arachne | A round, fluffy little navy spider with pale blue tips, two tufts on her head and big shining eyes: she clears the cobwebs near her friend and gives them the string. |
| **Urchskin** | Lamia | A teal sea serpent with a cream belly, purple urchin spines down her tail and a spiny crest: monsters that hurt her friend near her are pricked in return. |
| **Pineskrell** | Rodent | A blue pine squirrel with a huge frosted tail and two glowing crystal pinecones by her ear: the ores near her friend sparkle. |
| **Turtfolk** | Lizardfolk | A tall blue-scaled turtle warrior in bronze, a big brown shell, long brown hair and a yellow flower: every half minute two hearts of absorption for her friend. |
| **Tarnhelm** | Canid | A war wolf in blue enamelled plate and a steel helm with a glowing gem: raiders within 48 blocks of her friend glow. |
| **Palmyr** | Centaur | A blue deer taur in a lattice of white diamonds, with feathery frond antlers and a fan of a tail: Speed I for her sprinting friend. |
| **Skjoldram** | Minotaur | A shaggy teal ram in a steel brow plate, her curling horns rust red: she butts creepers away from her friend and stamps out their fuses. |
| **Mirror Crane** | Harpy | A tall blue and white crane on the flamingo's long legs, in gold bracers: one arrow in three aimed at her friend glances off her feathers and flies back. |
| **Bastet** | Nekomata | The cat goddess of Egypt, a black cat girl in gold with green eyes: creepers near her friend stop hunting them and slink away. |
| **Rusalka** | Merfolk | The Slavic river spirit, pale, long loose green hair and a white shift: Luck for her friend while they fish near her. |
| **Ushi-oni** | Arachne | The ox-horned spider demon of Japanese coasts, black and red: her bellow sends the monsters near her friend fleeing, slowed. |
| **Domovoi** | Elf | The Slavic house spirit behind the stove: a little straw-haired girl in a smock and a dotted red kerchief: the furnaces near her friend smelt twice as fast. |

Paints: cow (black patches on white), rust (rust eating through riveted steel), lava lamp (warm blobs rising through
violet; the blobs glow) and the seasonal sunflower (August only), so 71 in all.

## Nineteenth wave (2026-10-28, in the game)

Eight Harmontide originals from the reference art (their blue looks) and four creatures of folklore (Camazotz, the
akaname, the hoop snake and the draugr).

| Species | Home | Design |
|---|---|---|
| **Tin Jack** | Construct | A scarecrow of riveted blue tin in a tattered cloak and a pointed hat, a lantern at her hip (it glows): phantoms near her friend lose heart, weakened, and wheel away. |
| **Turquoise Serpent** | Lamia | A royal blue river serpent with a turquoise belly and a feathered crest, hung with gold and turquoise: her friend near her has Resistance while badly hurt. |
| **Moopie** | Merfolk | A sea cow: a cream and brown spotted tail, cow's ears and little horns in a mane of sea-green curls, a bell at her throat: poison, hunger and nausea wear off her friend near her. |
| **Tuskin** | Minotaur | A shaggy blue mammoth girl, white cheeks, great tusks bound with iron sweeping out from her jaw: the powder snow round her friend packs into snow blocks, and the cold lets go of them. |
| **Sabre** | Nekomata | A shaggy blue sabre-tooth in black leather, a white ruff, two long fangs: she finishes off a badly wounded monster near her friend. |
| **Obsidian Hound** | Canid | The obsidian macuahuitl hound: a temple dog in navy and gold, an obsidian-edged war club across her back: still lava near her friend's feet cools to obsidian. |
| **Kasurenijimi** | Undead | A ghost in a school uniform whose face is smeared out like an old photograph: her friend, sneaking near her, blurs out of sight. |
| **Emberu** | Harpy | A tall blue emu on the ostrich's long legs, her plumage painted with pale lines, turquoise beads: she kicks away the monsters that come at her friend. |
| **Camazotz** | Bat | The Maya death bat, blue-grey, great pink ears and a leaf nose, jade at her arms: at night she bites a monster near her friend and the stolen strength heals them. |
| **Akaname** | Devil | The filth-licker of Japanese bathhouses, blue-haired, her long tongue out: she scrubs the moss off the mossy stone near her friend. |
| **Hoop Snake** | Lamia | The hoop snake of American tall tales, sky blue with a cream belly: every few seconds she stings a monster near her friend: poisoned. |
| **Draugr** | Undead | The barrow-dweller of the sagas, corpse grey with pale glowing eyes, in torn grave clothes: the undead hunting her friend give up. |

Paints: comic (flat tones, halftone dots and inked outlines), blueprint (white lines and a grid on blueprint blue), koi
(red-orange patches on pearl white) and the seasonal lemonade (lemon slices on pale yellow, June only), so 75 in all.

Homage variants (POP_CULTURE_VARIANTS.md): octo agent (octopus), ninja chameleon, shell trooper (turtle), town clerk
(kobold), scythe green (mantis), leaf sprout (mandragora), mud fin (axolotl) and trident hood (shark).

## Twentieth wave (2026-10-28, in the game)

Eight Harmontide originals from the reference art (their blue looks), four creatures of folklore (the ghoul, the manticore,
the Mothman and the basilisk) and the house mouse, which carries the Gregory Horror Show homages. Three creatures of the
reference art turned out to be in the game already: Azureus is the poison dart frog, Slidred the red-eared slider and
Alisnapor the snapping turtle.

| Species | Home | Design |
|---|---|---|
| **Brasshorn Ram** | Minotaur | A shaggy blue mountain ram, great curling horns of brass, brass at her brow and hooves, a white rune and a woven blanket (rams too): her friend near her jumps higher. |
| **Ceibler** | Centaur | A slender pale-blue llama-deer, long ears and little horns, blue diamonds over her coat, a fringed blanket hung with bells: her friend near her runs faster on grass, dirt and paths. |
| **Sobolnik** | Rodent | A blue sable, big dark eyes in a white face, frosty swirls, a bushy tail and a little blue gem: her friend near her, out of the sky's sight, sees in the dark. |
| **Turtworld** | Lizardfolk | A great blue tortoise with a little world on her shell: turf, two snowy peaks and a waterfall: the saplings and crops near her friend grow. |
| **Fernstalker** | Lizardfolk | A navy feathered raptor, white at the breast, fern fronds fanned up from her back and a feather crest: she pounces on a monster near her friend. |
| **Moo** | Dragon | A round little spotted blue triceratops, a frill rimmed with cream knobs, two brow horns and a nose horn: when monsters crowd her friend, she stands with them (Strength). |
| **Hairypterix** | Dragon | A shaggy blue pterosaur, great membrane wings, a long yellow crest swept back (she flies): her friend falling near her drifts down gently. |
| **Doggo** | Merfolk | A sea dog: a shaggy blue pup's ears and hair, a white chest, a sparkling fish's tail: her friend near her, out of air underwater, gets a full breath. |
| **Ghoul** | Undead | The ghul of Arabian tales, blue-grey cracked skin, wild teal hair, claws and a torn shift: she eats the rotten flesh lying near her friend, and it heals her. |
| **Manticore** | Nekomata | The man-eater of Persian legend, a pale blue lion girl with a great mane, a scorpion's sting on her tail: she stings a monster near her friend, hurt and slowed. |
| **Mothman** | Bug | The Mothman of Point Pleasant, a soft blue moth girl, feathery antennae, a furry ruff, spotted wings, red eyes: at night the monsters near her friend glow. |
| **Basilisk** | Lizardfolk | The king of serpents, a little blue lizard girl with a crest of spines and a gold crown: her gaze all but turns a monster near her friend to stone (Slowness IV). |
| **House Mouse** | Rodent | A little mouse girl, big round ears, a pink nose and a long bare tail: now and then she shares a crumb with her hungry friend. |

Paints: tie_dye (a swirl of rainbow dye with white crinkle lines), jade (carved green stone with pale veins), lace (ivory
open-work lace) and the seasonal bluebell (little blue bells on green stems over pale periwinkle, May only), so 79 in all.

Homage variants (POP_CULTURE_VARIANTS.md): the house mouse's innkeeper (Gregory, with a ring of keys), prankster (James, a
striped shirt and a slingshot) and soul keeper (Gregory Mama, soul bottles at her belt); the high elf's sage princess
(Zelda), the wood elf's green hero (Link, with the long green cap), the ent's tree guard (Groot), the yuki-onna's snow
wraith (Froslass, purple eyes) and the dullahan's headless rider (Celty) and death envoy (Lala).

## Twenty-first wave (2026-10-28, in the game)

Eight Harmontide creatures from the reference art (their blue looks) and four creatures of folklore that the reference art
also has (the serpopard, Urcuchillay, the rakshasa and the gashadokuro).

| Species | Home | Design |
|---|---|---|
| **Nenekea** | Dragon | A blue sea dragon who walks on land: fins for ears, a crest of fin on her head, beads and pearls, a long tail ending in a tall fan of a fin: her hurt friend near her heals while they stand in water. |
| **Rheintalensis** | Canid | A broad, shaggy cave bear: wild blue hair, pelts, a necklace of teeth and a knotted club in her fist: she clubs a monster that comes close to her friend and knocks it away. |
| **Daeodon** | Armoured | A great shaggy pale-blue "hell pig", round ears, pink cheeks, little tusks: now and then she roots up a potato, a carrot or a beetroot at her friend's feet (rarely a golden carrot). |
| **Anteater** | Armoured | Soft blue with a cream face, a long snout with a pink tongue peeking out, round ears and a great bushy tail: she licks up the loose items near her friend and brings them to their feet. |
| **Oryx** | Minotaur | Pale blue, a cream crescent moon on her brow, two long straight horns swept back, a tufted tail (bulls too): in the heat of deserts, badlands and savannas her friend near her doesn't tire. |
| **Caracal** | Nekomata | Blue-grey with long black ear tufts and a pale chest: she leaps at a flying monster near her friend (a phantom, a vex, a blaze, a ghast). |
| **Walrus** | Merfolk | Round and blue, long pale hair, two white tusks, flippers and a broad tail: her friend near her never freezes. |
| **Anomalocaris** | Merfolk | A pale blue swimmer from the oldest seas: big round eyes, two spiny arms curling from her brow, flaps along her tail, a fan of a fluke: she seizes a monster swimming near her friend, drags it to her and bites. |
| **Serpopard** | Lamia | The serpent-leopard of ancient Egyptian palettes: a gold serpent with dark rosettes, a leopard's ears and whiskers, a lapis and gold collar: she bites a monster near her friend (Poison). |
| **Urcuchillay** | Centaur | The Inca god who watches over the herds, a llama of many colours: rainbow hair and fleece, a blue dress: the young animals near her friend grow up six times as fast. |
| **Rakshasa** | Nekomata | The shape-shifting tiger demon of Hindu legend: orange stripes, long fangs, a purple robe and gold: the monsters hunting her friend lose sight of them. |
| **Gashadokuro** | Undead | The towering "starving skeleton" of Japanese folklore: bone-white, dark hollows round red eyes, long black hair, a torn robe, the lines of her bones showing: she seizes a monster near her friend and lifts it into the air (Levitation). |

Paints: obsidian (black volcanic glass with purple glints), patchwork (quilted squares of cloth, stitched), sandstone (layered
bands of sand and ochre) and the seasonal acorn (little acorns on warm brown, September only), so 83 in all; every month now
has its seasonal paint.

Homage variant (POP_CULTURE_VARIANTS.md): the jorogumo's bake sale (Muffet): lilac skin, a fifth eye, black pigtails with red
bows, a purple dress and a teacup. The pigtails, the eye and the cup are drawn only on her.

## Twenty-second wave (2026-10-29, in the game)

Eight Harmontide creatures from the reference art (their blue looks) and four creatures of folklore (Xiangliu, the cyclops,
the genie and Ammit). The spinosaur and the australovenator, first planned here, went to the new theropod family; the
archelon and the fishman took their places.

| Species | Home | Design |
|---|---|---|
| **Macawtl** | Dragon | A feathered serpent in deep macaw blue: a crest of plumes, a small hooked beak between her eyes, feathered wings and tail, two long plumes trailing from it: out under the open sky her friend near her runs faster. |
| **Piranha** | Merfolk | A round little blue fish girl with darker spots, a jutting jaw of teeth, a lure on a stalk on her head: in water she bites every monster near her friend that is already badly hurt. |
| **Mosasaurus** | Merfolk | The great sea lizard: big, grey-blue, scaled, a toothy grin, a crescent of a tail fin: her friend's boat near her surges forward as she tows it. |
| **Fishman** | Merfolk | A light blue fish girl with a ponytail, fins at her elbows, freckle spots and a scowl of sharp teeth: she wrenches the trident from a drowned near her friend. |
| **Gibbon** | Rodent | Fluffy pale blue-grey round a cream face, green eyes, a ruff framing her face, a long tail curling up: her friend near her climbs ladders, vines and scaffolding twice as fast. |
| **Gigantopithecus** | Canid | The giant ape: huge, shaggy, deep blue, a red flower over her ear, a fur top and stone belt: when monsters crowd her friend she slams the ground and they are knocked back and slowed. |
| **Sandworm** | Lamia | Blue and segmented, a cream belly, a scaly hood over her head: she swallows a monster near her friend and spits it out far away. |
| **Archelon** | Lizardfolk | The greatest sea turtle: crystal-blue, a great shell with three keels, flippers at her wrists: she knocks away the arrows flying at her friend. |
| **Xiangliu** | Lamia | The nine-headed serpent of Chinese myth: a teal serpent girl with six little heads of her own round her head on short necks: her heads bite up to three monsters near her friend at once. |
| **Cyclops** | Devil | The one-eyed giant of Greek myth: a big shy girl with a single great eye (and one lid to blink it), pointed ears, a blue bob: she hurls a boulder at a monster far from her friend. |
| **Genie** | Devil | A spirit of smoke bound to a brass lamp: a smoke tail for legs, a ponytail, a bandeau and bangles, the lamp in her hand; she floats: three times a day (dawn, noon, dusk) she grants her friend a wish, a random boon. |
| **Ammit** | Nekomata | The devourer of the dead of Egyptian myth: a lion's mane and body, a crocodile's snout with teeth along its jaw, a tufted tail: when a monster dies near her friend she eats its heart and her friend heals. |

Paints: prismarine, amethyst, sculk (its bright specks glow) and mushroom, Minecraft's own blocks tiled (like obsidian and
sandstone since 2026-10-29), so 87 in all.

Homage props (POP_CULTURE_VARIANTS.md): the dullahan's headless rider (Celty) has her yellow cat-eared helmet over the head she
carries, the death envoy (Lala) a scythe in her free hand; both are drawn only on their own looks.

## Thirty-ninth wave (2026-11-03, in the game)

| Species | Home | Design |
|---|---|---|
| **Opossum** | Marsupial | Her nearly dead friend vanishes for three seconds and the monsters lose them; once a minute. |
| **Mole** | Rodent | Haste for her friend out of the sky's sight. |
| **Meerkat** | Rodent | When her friend is hit, every friend of theirs within 24 turns on the attacker. |
| **Red Panda** | Rodent | Her friend near her never freezes. |
| **Wolverine** | Canid | Below half health, Strength II and Resistance. |
| **Rafflesia** | Flora | Monsters that come within 5 reel back, weakened. |
| **Candle Spirit** | Undead | Glows. At night the undead near her friend catch fire. |
| **Cuttlefish** | Mollusk | With her friend swimming, monsters in the water near them lose their target and drift. |

Keepsakes (`entity/Keepsakes`, `item/UmbrellaItem`):
- **Friendship Bracelet** (string, a dye, string): a close friend (25+) wears it for good, +5. Missing that player costs her half the friendship.
- **Love Letter** (paper over a poppy): any friend, once a day, +3. She writes back: a letter to keep with her line in it, sweet from a grown friend who can flirt (and she blushes), friendly from anyone else.
- **Umbrella**: held in the rain, the nearest grown friend within 8 comes and shares it; 1 friendship a minute together, and a blush from one who can flirt.

## Thirty-eighth wave (2026-11-03, in the game)

| Species | Home | Design |
|---|---|---|
| **Beaver** | Rodent | Chews logs lying near her friend into planks (always oak). |
| **Starfish** | Crustacean | Her badly hurt friend regenerates. |
| **Anemone** | Mollusk | Luck for her friend in the water. |
| **Sunflower** | Flora | By day, keeps her friend fed. |
| **Scarecrow** | Construct | Monsters near her lose their targets. |
| **Snowgirl** | Construct | Pelts monsters near her friend; hurts blazes. Snow golems are her kin. |
| **Book Spirit** | Devil | Experience for her friend near a bookshelf. |
| **Tardigrade** | Bug | Once a day, nearly dead, she comes back whole. |
| **Sandman** | Undead | At night monsters near her friend go drowsy and blind. |

**Anniversaries** (`Missing.anniversary`): every 30 in-game days since a girl became a close friend (25+), three presents and a line in chat the next time they meet.

## Thirty-seventh wave (2026-11-03, in the game)

| Species | Home | Design |
|---|---|---|
| **Raccoon** | Rodent | Comes up with odds and ends for her friend. |
| **Pangolin** | Armoured | Snaps up silverfish and endermites. |
| **Okapi** | Centaur | With a rider, monsters after them lose track. |
| **Moose** | Centaur | Water doesn't slow her with a rider. |
| **Narwhal** | Merfolk | A spiral tusk (`hair_tusk`). Breaks ice over her friend in the water. |
| **Seahorse** | Merfolk | Shares her air when her friend runs short. |
| **Electric Eel** | Merfolk | Shocks everything hostile in the water near her friend; her tail glows. |
| **Antlion** | Bug | Monsters on sand near her friend sink and slow. |
| **Echidna** | Lamia | Hushes creepers near her friend. Creepers are her kin. |
| **Chimera** | Dragon | Burns the monster nearest her friend, and it festers. |
| **Nymph** | Elf | Flowers come up on the grass near her. |
| **Poltergeist** | Undead | See-through. Things fly at monsters near her friend. |

Features: **Friend Compass** (right-click: which way and how far to the next friend in the loaded world), **dancing** (any girl with a jukebox playing within 16 hops about, 1 friendship every five minutes), **Gift Box** (close friends of its owner within 16 leave a present once a day).

## Thirty-sixth wave (2026-11-03, in the game)

| Species | Home | Design |
|---|---|---|
| **Swan** | Harpy | The flamingo's rig. Whatever hurts her friend takes 6 and is thrown back. |
| **Pelican** | Harpy | The gull's rig. Beside water, fish for her friend every two minutes. |
| **Lemur** | Rodent | At dawn her friend gets extra hearts and Haste. |
| **Mongoose** | Rodent | Immune to poison, and clears it from her friend. |
| **Rhino** | Minotaur | A single horn; charges in a line like the rhiwool. |
| **Mantis Shrimp** | Crustacean | A 12 damage punch; the shock stuns what stands round the target. |
| **Centipede** | Lamia | The rearing rig (lamia, not bug, for that rig). Climbs walls; her bite sickens. |
| **Cicada** | Bug | Song gives her friend Speed and Haste every few minutes. |
| **Gnome** | Elf | A tall pointed hat. Can't move while a player looks straight at her. Grows crops. |
| **Dwarf** | Elf | Carries a real iron pickaxe and mines ore within 3 of her friend (mobGriefing). |
| **Nue** | Nekomata | Her night cry weakens monsters near her friend. |
| **Lorelei** | Merfolk | Her song pulls monsters near her friend towards her. |

Features: **Bedroll** (a one-block bed; using it brings friends within 32 round and counts as seeing them), **Scout party** (crouch as you hand over the pack: up to two more friends within 6 go too, each adds finds, each new family two more), **Towels** (a grown friend sharing the soaking tub wears a bath towel and blushes, `client/TowelLayer`, in place of the planned Wardrobe, since an overlay can't take clothes off), **Sketchbook** (use on a friend for a Figurine of her, `client/FigurineRenderer`, in place of the planned easel; it stays small on a stand).

Paints: **mud_brick**, **deepslate**, **oak** and **gilded**, so 123 in all (PAINTS.md).

Homage looks: Sun Wolf (wolf), Charm Fox (red kitsune), Fight Panda (panda), Storm Tiger (tiger), Boots Cat (lynx), Red Tide (koi), Shadow Ranger (dark elf), Bridge Keeper (troll).

Fixes: riders sit behind the waist (seats were in front of it: +Z is her front), the giraffe's seat is lower, sitting frogs and merfolk get up when drying out, harpies and frogs blush at the machine, the harness pumps work.

## Thirty-fifth wave (2026-11-02, in the game)

New designs, tested in `ThirtyFifthWaveTests`.

| Species | Home | Design |
|---|---|---|
| **Hyena** | Canid | Savanna. Eats rotten flesh near her friend and leaves bone meal. |
| **Tanuki** | Rodent | Once a day a leaf block becomes an emerald. |
| **Skunk** | Rodent | Hit her, or her friend, and everything round her is blinded and sick. |
| **Giraffe** | Centaur | Pulls apples down from the leaves above her. |
| **Elephant** | Centaur | Monsters crowding her friend are thrown back. |
| **Hippo** | Minotaur | While she is wet her friend doesn't burn. |
| **Lobster** | Crustacean | Below half health, once a day, she moults: whole again, and an exoskeleton for you. |
| **Dragonfly** | Bug | Hits phantoms and vexes near her friend. |
| **Ladybug** | Bug | Luck for her friend; ripens berry bushes. |
| **Goblin** | Devil | A gold ingot buys a random item from her pockets. |
| **Troll** | Devil | Stone under the open sky by day: unhurt and not moving. At night she flattens monsters near her friend. |
| **Selkie** | Merfolk | Sings fish up for her friend. |

**Flirting** (`entity/Flirting`, option `flirting`, on by default): one pat in four from a close friend (25+) gets a teasing line and hearts, and sharing the soaking tub has lines of its own. Words only, nothing past a tease. Harpies and frogs too since 2026-11-03. Only grown, full-size ones: anyone under `CHILDLIKE_HEIGHT` stays out, constructs too, and never a girl and her own father.

**Own rigs:** the Elephant (`rig="elephant"`, pillar legs, a great barrel, a swinging trunk, tusks, fanning ears) and the Giraffe (`rig="giraffe"`, long legs, standing on a long spotted neck, ossicones) use `rig_lower` in `gen_centaur.py`: the same bone names as `G.taur`, so they share its gaits.

**The milking machine:** it pumps while it works (a plunger, swapped by the `stroke` block state), and everyone at it blushes (`AbstractMonsterGirl.blushFor`, overlays for every girl from `tools/gen_milking_harness.py`). Harpies and frogs have no overlay. Girls aim for a free spot beside it, hop and try another side when stuck, and step across if that fails.

**Saving herself:** a girl ordered to sit gets up while she is on fire or in lava, and sits again afterwards.

## The scout pack (2026-11-02, in the game)

`entity/Scouting.java`, `item/ScoutPackItem`, tested in `ScoutingTests`. Leather, string and a chest make a Scout Pack. Hand it to a grown friend (friendship 10+) and she leaves for half a day for one of the biomes in her spawn tag, modded ones included; she comes back to her hearth or to you and drops the pack, full of that biome's materials. Right-click unpacks it, and it can go out again.

- Amount: 4 + friendship / 10 finds, so 5 at 10 and 14 at 100, each 1 to 3 items.
- The rare find (ancient debris, diamonds, emeralds, golden apples, by biome): friendship / 400 per find, one in four at 100.
- If she is missing you (the day after meeting again, Missing), half as much again, twice as much if badly. `missingRate` scales the bonus.
- Harpies and frogs go too. No friendship is lost for being away. Expeditions stay as they were: a map, a whole day, family finds, a chance of a new girl.

## Thirty-fourth wave (2026-11-02, in the game): life at home

New ways to spend time with friends, for anything Bonded: harpies and frogs too, unless noted. `entity/HomeLife.java`, assets from `tools/gen_home_assets.py`, tested in `HomeLifeTests`.

| Thing | How it works |
|---|---|
| **Grooming Brush** | Use it on anyone who likes you: 2 friendship, every five minutes. One time in three something comes out of her coat, by family (feather, slime ball, prismarine shard, hide). 64 uses. |
| **Toy Ball** | Right-click to throw, or drop it. A friend within 16 runs after it and brings it back: 1 friendship, a minute apart (girls and harpies). |
| **Friend Whistle** | Every friend within 48 comes running. |
| **Cooking Pot** | On a lit campfire, fire, magma or lava. Three foods go in, 15 seconds later a bowl takes out each of two Home-Cooked Meals. |
| **Home-Cooked Meal** | 10 food for you. Any friend eats it whatever her diet: 5 friendship and 4 hearts of health, every two minutes. |
| **Picnic Blanket** | Use food on it: friends within 20 come over, 2 friendship each, every ten minutes, and you eat well. The food only goes if somebody came. |
| **Vanity Mirror** | Your hand on it: the nearest friend within 6 tries on her next colour (a random one for harpies; frogs have none here). |
| **Soaking Tub** | Fill it with a water bucket, a bucket empties it. Standing in it you regenerate, and friends within 12 come over, regenerate too, 1 friendship every five minutes. |
| **Sparring Post** | With you within 16, friends within 5 practise on it: a round a minute, each one +0.25 attack, up to +5 at 20 rounds. It stays with her. |

## Thirty-third wave (2026-11-02, in the game)

The rest of TODO's list of species ideas.

| Species | Home | Design |
|---|---|---|
| **Pufferfish** | Merfolk | Warm reefs. A hit blows her up half again as big for 6 s, and another hit then poisons the attacker. |
| **Angler** | Merfolk | The deep. A glowing lure. Monsters after her friend turn on her. |
| **Manatee** | Merfolk | Warm rivers. The drowned and guardians near her friend lose interest. |
| **Nautilus** | Mollusk | Warm deeps. Her crouching friend in the water shoots up to the surface. |
| **Sea Slug** | Mollusk | Hand her a potion: she shares its effect with her friend for three minutes. |
| **Amphiptere** | Dragon | Rearing rig, no legs. Her friend's elytra flies faster near her. |
| **Ender Dragon** | Dragon | The End, 120 health. A glass bottle gets dragon's breath every ten minutes. |
| **Ant** | Bug | Walks. Brings the loose items near her friend to them. |
| **Queen Ant** | Bug | Raises an ant who knows her friend, four at most near her. |
| **Spider-Wasp** | Bug | Flies. Paralyses spiders near her friend. |
| **Lotus** | Flora | At dawn her friend loses every ill effect and is fed. |
| **Newt** | Lizardfolk | User design: a grown woman on all fours, long-backed and low with a forward-looking head, flat-chested, keeled tail. The lizardfolk body laid prone (`newt_prone`, build `lithe`), the spine stretched by bone scale. Low hitbox, standing height 1.8. Rears up now and then (`Action.REAR`). Heals in water, with her friend; biters are poisoned. Not a new family: a salamander's neighbour. |
| **Hummingbird** | Harpy | The phoenix's rig, small. Near flowers her friend gets Speed II. |
| **Duck** | Harpy | The gull's rig: mallard, mandarin, white, teal, black. Tells her friend when rain is due. |

## Thirty-second wave (2026-11-02, in the game)

The first half of TODO's list of species ideas.

| Species | Home | Design |
|---|---|---|
| **Black Panther** | Nekomata | Jungle. After dark, her crouching friend goes unseen. |
| **Kumiho** | Kitsune | Nine tails from birth; her bead gives her friend experience. |
| **Bat-Eared Fox** | Kitsune | Tells her friend how far off the nearest monster spawner is. |
| **Anaconda** | Lamia | The heaviest snake: 60 health, curvy, gold jewellery, thick tail. Swallows small monsters, then rests; music near her gives her friend Speed and Strength. An original character: the user asked for a real singer's look and that was declined, because the mod has bed-sharing. |
| **Sea Serpent** | Lamia | Open sea (on the lamia rig so she can swim). In the rain, lightning finds monsters near her friend. |
| **Black Widow** | Arachne | Poisons and slows the monster nearest her friend. |
| **Peacock Spider** | Arachne | Dances near villagers: Hero of the Village for her friend. |
| **Cave Spider** | Arachne | Clears cobwebs near her friend into string. Kin of cave spiders. |
| **Donkey** | Centaur | Can't be shoved. Kicks monsters near farm animals. |
| **Copper Slime** | Slime | Weathers a stage every 40 minutes. Honeycomb waxes, an axe scrapes, lightning polishes her and gives her friend Speed. |
| **Komodo** | Lizardfolk | Hurt monsters near her friend sicken. |
| **Skeleton** | Undead | Hostile until given a bone; bow fighter; gathers the arrows near her friend. |
| **Snow Elf** | Elf | Her friend doesn't freeze and climbs out of powder snow. |
| **Rose** | Flora | Whatever hurts her friend is pricked and slowed. |

Close enough to existing species to leave out: viper (caraviper), crystal slime (amethyst), parrot (macaw), vulture (condorune), elk (halberelk), imp (lesser devil), beetle (scarabronze, date beetle), mushroom dryad (matango).

## Thirty-first wave (2026-11-01, in the game)

No new creatures: this one is about where they spawn (`tools/gen_biome_coverage.py`).

- Every vanilla biome now has at least 8 species from 3 families. The End's outer islands, the warped forest, the frozen seas and the plain oceans gained the most.
- All 297 biomes of Biomes O' Plenty, Oh The Biomes We've Gone, Regions Unexplored and Terralith are listed under the vanilla biome each is most like, and take that biome's creatures. The entries are optional, so nothing is needed without those mods.
- Other world generation mods get creatures through broad tags: the species of the plain forest, plains, desert, swamp, savanna, jungle, taiga, badlands, beach, river, snowy plains, mushroom fields and stony peaks also spawn in whatever those tags hold. This also spreads them a little in vanilla, such as a forest species into the birch and dark forests.

## Thirtieth wave (2026-11-01, in the game)

New designs for the cold seas, the dripstone caves and the End. The Baku also spawns in the End midlands.

| Species | Home | Design |
|---|---|---|
| **Beluga** | Merfolk | Frozen seas. Reveals monsters in the water near her friend, and carries her friend along. Sings. |
| **Spider Crab** | Crustacean | Cold deeps. Pins a monster near her friend. |
| **Giant Kraken** | Mollusk | Frozen deeps. Five blocks tall. Attacks swimmers until fed; befriended, crushes monsters in the water. |
| **Drowned** | Undead | Cold seas. Hostile until fed rotten flesh; keeps her friend's breath under water. |
| **Olm** | Merfolk | Dripstone caves. No eyes. Her friend sees in the dark and can't be blinded. |
| **Cave Gekko** | Lizardfolk | Dripstone and lush caves. Climbs walls; a friend who falls near her drifts down. |
| **Knocker** | Elf | Dripstone caves. Knocks for loose rock overhead, and towards ore. |
| **Famished** | Undead | Dripstone caves, deep dark. Hostile until a player drops food near her; then friendly, her claws gone (`sated`). No antlers or deer skull: she is not the Wendigo. |
| **Moon Rabbit** | Jackalope | End highlands. Pounds chorus fruit into healing potions. |
| **Kaguya** | Elf | End highlands. Her friend jumps high and falls softly. |
| **Ophanim** | Elf | End highlands. A woman inside a ring of eyes; flies; shows every monster around her friend. |
| **Norn** | Elf | End midlands. Warns her friend of an attacker, and steadies them. |
| **Kun** | Dragon | End midlands. Flies; once a day catches her friend from the void. |

Paints: **blue_ice**, **chorus**, **calcite** and **tuff**, so 119 in all (PAINTS.md).

Homage looks: Sea Witch (octopus), Calypso Crab (crab), Moon Warrior (inaba), Frozen Queen (yuki-onna), Pride Queen (lioness), Wish Dragon (long), Night Warden (night elf), Ice Fox (arctic kitsune).

## Twenty-ninth wave (2026-11-01, in the game)

The first wave of new designs, for the thin biomes, and the missing system.

| Species | Home | Design |
|---|---|---|
| **Shiitake** | Flora | Mushroom fields. A brown-capped woman with two small adult-proportioned offshoots. They take a blow meant for her friend, then regrow over a day. Out of the bed system. |
| **Shroomotaur** | Minotaur | Mushroom fields. A mooshroom cow: fills a bowl with stew. |
| **Censhroom** | Centaur | Mushroom fields, dark forest. A fungal shelf for a saddle; no poison touches her rider. |
| **Cordyceps Infected** | Undead | Mushroom fields, lush caves. Hostile; five feedings of rotten flesh cure and befriend her. |
| **Firesprite** | Devil | Warped forest, basalt deltas. Teal flame in a woman's shape: lights the monsters near her friend, keeps her friend from burning. |
| **Zoglintaur** | Centaur | Warped and crimson forests. Hostile until calmed with raw meat; tosses monsters. |
| **Piglette** | Armoured | Nether forests and wastes. Barters gold for Nether goods, carries a crossbow or gun. |
| **Lost Soul** | Undead | Soul sand valley. Points to where you last died. |
| **Soul Cluster** | Undead | Soul sand valley. A killing blow scatters her; she re-forms, three times a day. |
| **Soul Leech** | Undead | Soul sand valley. Drains the monsters near her friend and passes the health on. |

Paints: **soul_sand**, **nether_wart**, **mycelium** and **basalt**, so 115 in all (PAINTS.md).

Homage looks: Needle Knight (hornet), Spore Retainer (matango), Lasagna Cat (caracal), Sly Fox (gray kitsune), Carrot Bunny (boghare), Web Hero (jumping spider), Blue Heeler (kobold), Swamp King (ogre).

**Missing** (`entity/Missing.java`): worked out from the last meeting when a friend (25+) and her player meet again. She starts missing them after 4 days (60+: 3, 100: 2), badly at twice that. For a day after they meet: +25% or +50% on her odds of coming to bed, her gift at once and more often, and a line in the journal and in her bond. She loses 1 friendship a day past the start, never below 25. Conception after a shared night is now 50% (`conceptionChance`), 75% if she missed them, 100% if badly; a full moon is still certain. `missingRate` scales it all. Expeditions don't count.

## Twenty-eighth wave (2026-11-01, in the game)

The last of the Harmontide reference art: nothing is left on the roster.

| Species | Home | Design |
|---|---|---|
| **Gandharva** | Elf | Male by default, a long robe, small wings, a lute on his back. He truly plays: note by note, joining in with jukeboxes and note blocks, and his friend heals to the music. |
| **Apsara** | Elf | Water-blue dancer, full costume. Dances to any music, her friend moves quicker; with a Gandharva playing, her friend heals too. |
| **Pixi** | Bug | A tiny fairy, kept out of the bed system. Crouch and ask, empty-handed: she shrinks her friend to half size for half a minute. |
| **Rlff** | Bug | A tall golden stream spirit on the Fairy Queen's rig. Her friend heals standing in water near her. |
| **Ark Seraphim** | Centaur | A winged blue lioness with a gold ark. End highlands and midlands. Once a day, with her friend badly hurt, light burns the monsters near them and shields her friend. |
| **Poultry** | Harpy | Crow rig with a hen's full figure, a work dress and apron. Grows the crops around her and lays eggs. |
| **Landharpy** | Harpy | Ostrich rig, torn jeans, never flies. Her kick staggers monsters. |
| **Argentowlis** | Harpy | Owl rig, drawn huge. At night she marks the monsters her friend can't see. |
| **Ibisking** | Harpy | Flamingo rig. Give her paper and she tells you where the nearest treasure lies. |
| **Motmora** | Harpy | Phoenix rig. Her tail display dazes two or more monsters. |
| **Reliquary Dove** | Harpy | Crow rig. The End. She keeps one stack for you. |
| **Skycrow** | Harpy | Crow rig. End barrens and small islands. Lightning on the monsters near her friend, harder in rain. |

Paints: **crimson**, **bone**, **bamboo** and **sponge**, so 111 in all (PAINTS.md).

Homage looks: Spark Mouse (house mouse), Green Dino (gecko), Doctor Deer (reindeer), Purple Flame (wyrm), Night Wing (gryphon), Forest Keeper (panda), Sleepy Giant (grizzly), First Fox (fennec).

Also new: wild girls are rarer (`MonsterGirlsConfig`, `SpawnRarity`: a 37.5% chance on top of each species' own rules, none within 128 blocks of another wild one, groups of at most 2); anyone under 1.25 blocks tall stays out of bed (`Bonded.CHILDLIKE_HEIGHT`: fairies, not the Fairy Queen); `tactical_eye` lost her model rifle and carries real weapons, with a TaCZ sniper rifle firing out to 64 blocks.

## Twenty-seventh wave (2026-10-31, in the game)

Twelve from the roster's Left list, the Gaitress on a rig of her own. Set aside: the adaro and the pishtaco (living traditions). Twelve creatures are left for one last wave.

| Species | Home | Design |
|---|---|---|
| **Bahamut** | Dragon | Soft, round and pale blue, curled horns, fins for ears; she flies. Once a day a beam of light strikes every monster around her friend for 12. |
| **Cetus** | Merfolk | 3 blocks of pale whale, a great flat fluke: she swallows drowned and guardians near her friend whole. |
| **Demon** | Devil | The plain kind: horns, furred legs, little wings. A friend crouching with an empty hand trades 3 hearts for half a minute of Strength II. |
| **Fury** | Devil | Ink-dark clothes, wild hair, wings of glowing violet flame; she flies. Whoever kills a villager or a tamed pet within 32 of her is marked and hunted to the death, her own friend included. |
| **Charro Negro** | Rider | In black from sombrero to hem, on a real black horse. She lends a friend 8 emeralds (crouch, empty hand); 16 must come back within three days, or she rides to them wherever they are. The debt dies with the debtor. |
| **Koschei** | Undead | A bone beak under a tall kokoshnik, blue robes, chains, a glowing lantern: she chains the monsters near her friend so they cannot get 3 blocks from where they stood. |
| **La Mano Peluda** | Undead | The Hand's rig under a shaggy pelt, climbs walls: it leaps on a monster near its friend, and the monster goes blind and forgets its target. |
| **Gaitress** | Undead | Her own rig: four long legs and a swimsuit, two to walk on, two crossed up in the air where a head would be. Very fast, a throwing kick, and her friend runs faster beside her. |
| **Bloodhound** | Canid | Stitched brown fur, long black hair, a wide bloody grin: she tracks what last hurt her friend up to 64 blocks, or the nearest hidden monster within 32, and shows the way. |
| **Shedim** | Centaur | A soft deer-fox taur with little antlers, unseen at night: she blinks her badly hurt friend 8 blocks away from a monster. |
| **Anzu** | Centaur | A blue lioness with feathered wings and gold at her brow: she knocks the weapon out of a monster's hand and passes its good effects to her friend. |
| **Kitsune Obaa** | Kitsune | White hair, nine white tails always: once a day, with two or more monsters after her friend, she hides them and the monsters lose interest. |

Paints: **tnt**, **warped**, **dripstone** and **brain coral**, so 107 in all (PAINTS.md).

Homage looks: Blue Blur (needlehog), Royal Flame (blaze cat), Backpack Bear (grizzly), Aura Guardian (wolf), Gold Ninja (red kitsune), Night Heiress (succubus), Copy Blob (undine), Golden Runner (emu).

## Twenty-sixth wave (2026-10-31, in the game)

Eight birds, four more from the roster, and two new looks at old problems: the Hand, which isn't a girl at all, and Wrath, who is nothing but fire.

| Species | Home | Design |
|---|---|---|
| **Emu** | Harpy | Round, shaggy and brown on the ostrich's rig (`EmuHarpy`): very fast; she lifts food out of chests until she is befriended, then kicks whatever comes at her friend. |
| **Macaw** | Harpy | Blue and gold, a big dark beak, on the toucan's rig (`MacawHarpy`): she calls in the voice of any monster hiding near her friend, and it glows. |
| **Roc** | Harpy | Storm-blue, 4 blocks tall, on the crow's rig (`RocHarpy`): she snatches a monster near her friend, climbs with it for two seconds and lets go. |
| **Ziz** | Harpy | Blue and white, rainbow wings, a long tail, on the phoenix's rig (`ZizHarpy`): once a day she clears the rain from the sky. |
| **Bennu** | Harpy | A slate-blue heron on the flamingo's rig (`BennuHarpy`): at sunrise she heals her friend whole, and once a day she gets up from a killing blow. |
| **Garuda** | Harpy | Orange skin, blue wings, gold, on the crow's rig (`GarudaHarpy`): poison does not hold on her or her friend, and she strikes down silverfish, endermites and wild hostile lamias on sight. |
| **La Lechuza** | Harpy | A scowling brown owl (`LechuzaHarpy`): at night her cry shows up the monsters near her friend and drives them back. |
| **Alkonost** | Harpy | All soft white down, on the crow's rig (`AlkonostHarpy`): her song mends her friend and all but stops the monsters round them. |
| **Smilodon** | Nekomata | Furred all over in shaggy gold, a thick mane, two long fangs: she springs on the nearest monster near her friend for 9, and the rest flinch back. |
| **Caribou** | Centaur | A blue-grey deer taur with broad antlers: with a player on her back she gets faster on snow and ice, and she walks on powder snow. |
| **Lilith** | Devil | Blue hair, ram's horns, great bat wings; she flies: at night the monsters near her friend are weakened. |
| **Min Min Light** | Mollusk | A small girl under a glowing yellow cap, pale tentacles in place of legs; she floats: at night she drifts ahead of her friend and lights the ground. |
| **Hand** | Undead | Its own model: an ordinary severed hand, palm down, a bone sticking out of the wrist. It runs on its fingertips like a spider, climbs walls and drums its fingers while it waits; it holds monsters near its friend by the ankle. |
| **Wrath** | Devil | Made entirely of fire, only vaguely an oni: horns, a club and a tail, all flame, tongues of it licking off her. Every blow she takes adds 2 to her next hits for 10 seconds (5 blows at most), her hits set things alight, water hurts her, and she goes after whatever hurts her friend. |

Paints: **redstone** (its bright lines glow), **glowstone** (it glows), **cherry** and **terracotta**, so 103 in all (PAINTS.md).

## Twenty-fifth wave (2026-10-30, in the game)

The Four Riders, a family of their own with their own looks, and eight more. Riflebirds also got a new dance groove: an original, tense
80s pop pattern in a minor key, a nod to the viral videos of birds dancing (`DanceGoal`; not anyone's song).

| Species | Home | Design |
|---|---|---|
| **War** | Rider | A wild blonde mane, blue and silver armour: she gives her friend Strength when three monsters close in. |
| **Famine** | Rider | Pale, in a huge white feather cloak and a dark horned hood: she keeps her friend fed in a fight and weakens the monsters near them. |
| **Pestilence** | Rider | A tiny lilac-haired girl with a thorned staff and teal flame, riding a real black Minecraft horse: a teal cloud hurts and slows the monsters near her friend. |
| **Death** | Rider | Hooded, a big scythe, a crow on her hand: the monsters near her friend at a quarter of their health fall. |
| **Fire Giant** | Devil | 4 blocks tall, dark skin veined with glowing lava, a crown of flame: Fire Resistance for her friend, and real light where she walks. |
| **Frost Giant** | Devil | 4 blocks tall, long white hair, a crown and shoulders of ice shards: water freezes under her friend's feet. |
| **Ifrit** | Devil | Horns, small wings, a smoke tail for legs, blue flames floating at her sides: once a day she smelts a stack of raw ore in her friend's pack. |
| **Pazuzu** | Devil | Horns, feathered wings, a snake for a tail; she flies: a gust throws back the monsters crowding her friend. |
| **Charybdis** | Mollusk | Curly pale-teal hair, a shell top, a skirt of tentacles, across the strait from Scylla: she drags the monsters in the water towards her. |
| **Mad Hatter** | Elf | Purple hair, a blue coat, a huge hat, a teacup: once a day a cup of tea with a random good effect. |
| **Angel** | Elf | A small winged girl, a glowing halo; she flies: she heals her friend when their health runs low. |
| **Yatagarasu** | Harpy | Black wings, a pale blue kimono (`YatagarasuHarpy`, the crow's rig): at dawn she gives her friend Regeneration and Speed. |

Paints: **end_stone**, **hay_bale**, **ancient_debris** and **sea_lantern** (it glows), so 99 in all (PAINTS.md).

Homage looks (POP_CULTURE_VARIANTS.md): the crocodile as Vector (`detective_croc`), the kangaroo as Crash (`bandicoot`), the cactus as the
Cactuar (`needle_runner`), the pegasus as Pit (`angel_guard`), the living doll as the Puppet (`music_box`), the dolphin as Ecco
(`star_song`), the tengu as Falco (`sky_ace`, never rolled in the wild).

## Twenty-fourth wave (2026-10-30, in the game)

Eight Harmontide creatures from the reference art and four of folklore. Three use the rigs of the last round: Quetzalcoatl the
rearing serpent, the Leviathan the fish body, the girtablilu the scorpion's. The ibiset and the stigowl are harpies of their own.

| Species | Home | Design |
|---|---|---|
| **Quetzalcoatl** | Dragon | A teal feathered serpent from the bust down, a crest of leaves round her head, a woven orange band, pale wings; she flies: at dawn she ripens the crops near her friend. |
| **Leviathan** | Merfolk | A great pale-blue fish, 3 blocks long, with a girl's chest, curled horns and fin ears: her friend swims with Dolphin's Grace and the tide drives monsters away from them. |
| **Girtablilu** | Bug | A veiled, hooded girl on a scorpion's legs with pincers and a raised tail: at night she stings the monsters near her friend (Poison and Slowness II). |
| **Cipactli** | Lizardfolk | A small spiky teal crocodile girl, a cream belly, a heavy tail: she bites monsters at the water's edge near her friend and drags them under. |
| **Behemoth** | Devil | 3 blocks tall, dark blue, great horns, a wild mane and a thick tail: her friend cannot be knocked back near her. |
| **Nasnas** | Undead | Pale, her body fading into a wisp: once a day she takes a killing blow in her friend's place. |
| **Ibiset** | Harpy | A slim blue ibis hung with gold (`IbisetHarpy`, the flamingo's rig): once a day she brings her friend an enchanted book. |
| **Stigowl** | Harpy | A dark owl with a pale heart of a face (`StigowlHarpy`, the owl's rig): she clears Darkness and Blindness from her friend. |
| **Cerberus** | Canid | Dark-furred, with two more little heads on her shoulders: monsters coming at her friend from behind are slowed, and creepers lose their nerve. |
| **Jiangshi** | Undead | The hopping corpse: an official's robe and hat, a paper talisman, arms held out; she only hops, and strikes stiff the monsters she reaches. |
| **Baku** | Nekomata | A tapir's short trunk, round ears, a two-tone coat: she eats her friend's nightmares, so phantoms never come. |
| **Qilin** | Centaur | A golden deer with soft antlers: she heals the animals near her friend and never tramples farmland. |

Paints: **netherite**, **purpur**, **crying_obsidian** (its tears glow) and **shroomlight** (it glows), Minecraft's own blocks, so 95
in all (PAINTS.md).

Homage looks (POP_CULTURE_VARIANTS.md): the genie as the Genie (`lamp_friend`, a goatee), the jumping spider as the Webbed spider
(`tiny_jumper`), the moth as Mothra (`kaiju_moth`), the golem as Clank (`pocket_bot`), the red kitsune as Ninetales (`nine_cream`,
always nine tails), the raccoon as Tom Nook (`shopkeeper`), the will-o'-wisp as Boo (`shy_ghost`), the vampire as Alucard (`night_heir`).

## Twenty-third wave (2026-10-30, in the game)

Eight Harmontide creatures from the reference art (their blue looks; the spewslug's only look is red) and four of myth and
folklore: Sun Wukong, Scylla, the taotie and the vetala. The ironbeak is a harpy of her own (`IronbeakHarpy`, the crow's rig).

| Species | Home | Design |
|---|---|---|
| **Automaton** | Construct | A pale-blue clockwork girl in armour plates: a round helmet with a disc over each ear, rivets down every plate: she sets torches (her friend's own) on the dark ground near her friend. |
| **Spewslug** | Mollusk | A glistening crimson slug girl: eye-stalk antennae, a slug's mantle over her back, a toothy maw with purple bile dribbling from it: she spews bile over the monster that hits her friend (Weakness II, Hunger). |
| **Ogre** | Devil | Big and green-skinned, long pointed ears, two little horns, light-blue hair, a blue dress and a club: she clubs the monster that hits her friend, and it can hardly move for three seconds. |
| **Hellhound** | Canid | From the user's reference: charcoal skin, heavy black fur on her arms and legs, spiky dark-red fur at the shoulders, wrists and ankles, flames (glowing) streaming from her eyes, great claws, a skull at her collar, ember cracks, a fur top and shorts, a toothy grin: she runs down whatever hurt her friend, anywhere within 24 blocks, and sets it on fire. |
| **Argentinosaurus** | Lizardfolk | Tall, pale blue with orange stripes and a cream belly; a **long neck** lifts her head high above everyone (her head and hair ride on a 9-pixel neck bone), a long heavy tail: she spots the monsters 24-40 blocks from her friend and they glow. |
| **Titanoboa** | Lamia | The greatest snake: **3.9 blocks tall, twice the boa**, olive-brown with dark blotches: she coils round the biggest monster near her friend and holds it fast for five seconds (Slowness VII, Wither II). |
| **Sasquatch** | Rodent | Big and fluffy, pale blue, a shaggy mane round a pink face, a fluffy ruff: her friend turns invisible while sneaking in a forest near her. |
| **Ironbeak** | Harpy | A blue courier bird with silver talons, a blue scarf and a satchel (the crow's rig): once a day she brings her friend an explorer's map (a woodland mansion, an ocean monument, buried treasure or a trial chamber, in turn). |
| **Sun Wukong** | Rodent | The Monkey King: golden fur, a gold circlet, red and gold armour, a curling tail and her red staff with gold caps: on her somersault cloud her friend never takes fall damage near her. |
| **Scylla** | Mollusk | The sea monster of the Odyssey: a sea-green girl in a clam shell top over a skirt of tentacles, six dog heads snarling from her waist: her hounds tear the armour and weapons off the monsters near her friend. |
| **Taotie** | Devil | The glutton of the Chinese bronzes: curled horns, bronze meanders over her arms and legs, a toothy grin: she eats the junk monsters drop near her friend and turns it into experience for them. |
| **Vetala** | Bat | The corpse-riding spirit of Indian tales: ash-pale, ragged, red-eyed, tattered wings: she rides a zombie near her friend, and it fights the other monsters for 30 seconds. |

Paints: **lapis**, **bookshelf**, **glazed** (blue glazed terracotta) and **redstone_lamp** (its light glows), Minecraft's own
blocks, so 91 in all. And every older paint named after something Minecraft draws now uses its texture too: gold (the gold
block), the four copper stages (the copper blocks), honey (the honey block), magma (the magma block, its cracks glow), pumpkin,
frost (packed ice), sakura (cherry leaves), stained glass (the stained glass blocks), moss (moss block on stone), watermelon
(the melon's rind), cow (the cow's hide) and sunflower (its flower head).

Homage looks (POP_CULTURE_VARIANTS.md): the fennec as Tails (`twin_tails`: two tails always, goggles on her head), the
salamander as Charmander (`ember_tail`), the snapping turtle as Bowser (`spike_king`), the zombie as the Plants vs. Zombies
zombie (`lawn_zombie`, a traffic cone), the undine as the Dragon Quest slime (`drop_smile`: a drop's point on her head, a wide
smile), the amethyst slime as Kirby (`puffball`), the ratatoskr as Conker (`hoodie_squirrel`) and the bee as Charmy Bee
(`flight_cap`: an aviator's cap and goggles). The props are drawn only on their looks.

## Theropods (2026-10-29, in the game)

A family of their own (`theropod`, field guide volume XXVII), built like a 1920s museum sketch of a T. rex but human: the upper
body hunched 20° forward (the head tilted back up to look ahead), a long thick tail held level behind for balance, human legs,
arms, skin and face, bare feet with small claws, and a wide grin of little pointed teeth. Each kind's own features are kept
small. They are cousins of the lizardfolk (the Java class `Theropod` extends `Lizardfolk`): cold-blooded, basking, leathery
clutches; they eat raw meat and eggs.

| Species | Design |
|---|---|
| **Fernstalker** | Moved from the lizardfolk (now `fernstalker_theropod`): navy and white, a feathered tail, three small fern fronds at her back and a feather crest. She pounces on a monster near her friend. |
| **Spinosaur** | Teal, a low ribbed sail down her spine and along her tail, a tall paddle of a tail, and the family's only snout: a short skin-toned crocodile snout with the teeth along its jaw. Her friend near her in water gets Dolphin's Grace. |
| **Australovenator** | Pale blue with darker bands, two small hooked claws on each hand. When her friend strikes a mob she leaps on it too (never a player, a monster girl or a pet). |

The spinosaur and the australovenator were planned for the twenty-second wave (as a dragon and a lizardfolk); they came
with the family instead.
