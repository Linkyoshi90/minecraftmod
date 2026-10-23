# Harmontide: species concepts

Rough notes for the next wave of species. Nothing here is built yet. Each entry gives:

- **Home:** the existing family it slots into (reuse that family's model, AI and tests), or a proposed new family.
- **Basis:** the real animal behind it, with its classification where there is one. Myths have none, so for those it gives the origin.
- **Niche:** where it lives and what it does in a vanilla world.
- **Note:** design, tone or open questions.

## Ground rules (all species)

- **Vanilla friendly:** obey `mobGriefing`, take no spawns from vanilla mobs, use vanilla items for every interaction (feeding, taming, trading), and never touch player-placed chests or blocks unless tamed and asked.
- **Believable ecology:** every species gets a biome, a time of day, a food, a predator or rival, and at least one useful link with vanilla mobs or blocks. That link is what keeps them from being plain reskins.
- **Low ecchi:** fully clothed or naturally covered (fur, feathers, scales, shell). No fanservice poses or animations. Adult proportions only.
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
| **Lilith** | `lilith` | Devil | A night demoness of Mesopotamian and Jewish lore | Nocturnal. Keep her low ecchi (no succubus overlap). |
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
