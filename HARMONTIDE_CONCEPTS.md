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
| **Klabuster** | Slime | A round little drop of water from the cave ceilings: she puts out fires and fills cauldrons near her friend. |
| **Dryad** | Flora | The Greek tree nymph, a girl of pale blue wood with leafy branches for antlers: the saplings near her friend grow. |
| **Amarok** | Canid | The giant lone wolf of Inuit legend, slate grey and shaggy: her friend's tamed wolves near her fight harder. |
| **Yurei** | Undead | The Japanese ghost in a school uniform, long black hair, legs fading into a white wisp; she floats. At night the monsters near her friend grow weak. |
| **Lamassu** | Centaur | The winged, horned, lion-bodied guardian of Assyria's gates, long grey curls: Resistance I for her friend. |

Paints: polka (white dots on bright colours), circuit (a green circuit board whose traces glow), giraffe (brown patches
in a net of cream lines) and the seasonal watermelon (July only), so 67 in all.

