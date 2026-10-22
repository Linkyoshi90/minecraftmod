# Harmontide to-do

## 0. Tonight: survival server playtest (2026-10-21)

Write down numbers and coordinates, not impressions. They turn into the next to-dos.

- [ ] Before you join: run `./gradlew build` and start a **dedicated server** once (`runServer`). Client-only classes leaking into server code only crash there.
- [ ] **Generation:** which species did you meet, in which biome, and how many in the first hour of exploring? Note any biome that feels empty or crowded.
- [ ] **Spawning:** do girls crowd out vanilla animals or the hostile mob cap? Compare animal counts with an area that has no girls in it.
- [ ] **Behaviour:** check for griefing (blocks broken, crops trampled, chests touched) with `mobGriefing` both true and false. Do they get stuck in caves or doors, or drown?
- [ ] **Multiplayer:** do the follow, bond, gift and bed-sharing features behave with two players? Does whoever bonded first win?
- [ ] **Performance:** check tick time with a crowd of girls (for example a harpy roost or an anuran pond). Use `/neoforge tps`, or spark if it is installed.
- [ ] **Arrow farm:** the real-client check from section 2.
- [ ] Note every crash or log spam with a timestamp, and copy `logs/latest.log` afterwards.

## 0b. Harmontide rename

- [x] Display name and creative tab now say **Harmontide** (`gradle.properties` `mod_name`, `itemGroup`).
- [ ] Decide whether to rename `mod_id` `monstergirls` to `harmontide` as well. That **breaks existing worlds**: every entity, block and item id changes. Do it once, before any public release, and **not** before tonight's server test. It touches the package `com.linkyoshi90.monstergirls`, the `assets/monstergirls/` and `data/monstergirls/` namespaces, `MonsterGirlsMod.MODID`, the tools scripts and the gametest structures.
- [ ] Retitle the `*_field_guide.html` pages ("A field guide to an inhabited world") under Harmontide and link them from the website.

## 0c. New species concepts

See [HARMONTIDE_CONCEPTS.md](HARMONTIDE_CONCEPTS.md) for the next wave of 39 species: their families, classifications, vanilla niches and a suggested first batch.

- [x] First four are in the game (2026-10-21), in existing families with GameTests (`HarmontideTests`):
  - **Ocelot** nekomata: jungle and bamboo, climbs walls and trunks, brings jungle presents.
  - **Tapir** centaur: jungle and swamp, eats melon. The baku: no vanilla phantoms or phantom girls within 32 blocks.
  - **Alligator snapping turtle** lizardfolk: swamps, tongue lure (fish at her feet, at most 3 at a time), shell retreat.
  - **Banshee** undead: birch forests and meadows at night. Keens when a player nearby drops to 3 hearts, and every hostile mob near that player glows.
- [ ] Playtest them tonight: do they spawn often enough? Is the turtle's fish rate (about 1 a minute) too generous? Is the banshee's ghast-scream keen too loud? Does the ocelot get stuck climbing?
- [ ] Next batch: Kappa (lizardfolk turtle), Naga (lamia), Dullahan (undead, on a skeleton horse), then the separate harpy and anuran systems: Tengu, Thunderbird and Vodyanoy (the first monster boy, which needs male support in the anuran rig).
- [ ] Decide on the sensitive ones: Kachina (recommendation: drop it), Thunderbird, Uktena, Wayob.
- [ ] Decide which new families are worth their own class: Giants, Celestials, Horsemen, Fury, Crustaceans.

## 0d. Kinship (girls and their vanilla counterparts)

In since 2026-10-21: `entity/girl/Kinship.java`, tested in `KinshipTests`.
- A girl draws her vanilla kin to her: they follow her within 16 blocks. While she does not see a player as prey, her kin neither flee from that player nor attack them. So a calmed zombie girl's zombies leave you alone, and an ocelot nekomata's ocelots stop running.
- Feed her and one of her kin her family's food within 30 seconds, close together: she **introduces** you. Cats, horses, donkeys, mules and llamas are tamed, ocelots and foxes trust you, and everything else becomes a persistent friend that follows you and never targets a player.
- Kin: nekomata and cats (ocelot nekomata and ocelots), kitsune and foxes, arachne and spiders, the horse and zebra centaur and horses, donkeys and mules, the alpaca and llamas, the goat centaur and goats, dolphin and orca mermaids and dolphins, the axolotl mermaid and axolotls, slimes and slimes (magma: magma cubes), jackalopes and rabbits, minotaurs and cows and mooshrooms, kraken, ika and octopus and squid, the zombie girl and zombies, husks, drowned and zombie villagers, the phantom girl and phantoms, the turtles and turtles, bees and bees. Everyone else has no kin and bonds and breeds with players only, as before. Minotaur cows already breed with bulls.
- [ ] Playtest: does a befriended zombie still go for villagers? (It does today. Decide whether friends should leave villagers alone too.) Befriended zombies still burn in daylight.
- [ ] Harpies and anurans are separate classes, not covered yet: parrots and chickens, frogs.
- [ ] Slimes and phantoms have no pathfinding, so they do not visibly follow. They only stop attacking.

## 0e. Stick orders and stances

In since 2026-10-21: `entity/Orders.java`, used by harpies, anurans and every girl family. Tested in `OrdersTests`.
- **Right-click with a stick** cycles **Sit → Patrol → Follow → Free roam**. Patrol: she roams within 10 blocks of the spot where you gave the order. Follow: she stays within 7 blocks of you, and catches up at once if more than 40 blocks behind. Anyone can make her sit and get up again; patrol and follow need her to be fond of you (10+).
- **Sneak + right-click with a stick** cycles **Hostile → Neutral → Self-defence → Peaceful** (fond only). Until a stance is set she follows her instincts, as before.
  - Hostile attacks hostile mobs, wild girls that are hostile to you, and anyone who hurts her or you.
  - Neutral defends herself and you.
  - Self-defence only hits back.
  - Peaceful never fights.
- **No friendly fire:** two companions (both fond of the same player) never hurt or target each other, nor each other's befriended vanilla mobs. That is the phoenix and ostrich crossfire fix. Stray shots never hit her player either; deliberate pokes (a swat, a clumsy kick) still do.
- [ ] **Next command:** even a sitting girl should run for water when she is drying out (anurans, merfolk) or on fire. She should get up, save herself and then go back to her spot.
- [ ] Playtest: is patrol at 10 blocks and follow at 7 the right size? Does the follow teleport feel right for flying harpies?
- [ ] Befriended vanilla mobs (Kinship) do not take stick orders yet.
- [ ] `RelationshipTests.eggHatchesIntoAGrowingHatchling` failed once and then passed on the next run. It is flaky and needs looking at.

## 0f. Answers to the field guides' open questions (2026-10-22)

The answers given on 2026-10-22, as a work list. Ticked = in game.

**All families**
- [x] Befriended vanilla mobs (Kinship) take stick orders and stances too.
- [ ] Dev-client test: harpy hatchlings growing up (`eggHatchesIntoAGrowingHatchling` was flaky once).
- [x] Crash fix (2026-10-22, at random tick speed 100): `BuildNestGoal` ticked once more after losing its nest spot. Not growth-related. Regression test: `AnswersTests.nestGoalSurvivesALostSpot`.

**Harpy**
- [x] The female/male colouring split (grey-brown against black-and-white) becomes two variants of the same mob. Already the case: the ostrich's `hen` variant.
- [x] Ostrich kick: 3 hearts on normal (vanilla difficulty scaling for the rest), high knockback. It should hurt.
- [x] Territory-edge offering: any herbivore food (new item tag `monstergirls:herbivore_food`).
- [x] Winged cap (leather + feathers): slows the fall (capped fall speed) and cuts fall damage to a fifth; sneaking folds the wings.
- [x] Winged cap: a GeckoLib armour model (`geo/item/armor/winged_cap.geo.json`) whose wings beat while falling and fold otherwise. Check it in the dev client: the server loads it, but nobody has seen it render yet.

**Anuran**
- [x] Skirt and cattails become a removable outfit (the bullywug). At high friendship shears take them off: you get a lily pad and three sugar cane. A lily pad puts them back. Woven reed shorts are painted under the skirt, so she stays dressed.

**Nekomata**
- [x] A true nekomata's spare life stays hers alone (no player totem). Answer: no.
- [x] Kittens can be put on a lead.

**Kitsune**
- [x] The nine-tailed elder spawns naturally, very rarely, in cherry groves.
- [x] The stash game works on other mobs' drops too.

**Lamia**
- [x] Shed Skin crafts a light armour piece (a snakeskin cloak).
- [x] She carries her player in her coils: a slow mount off sand, fast on sand.

**Arachne**
- [x] The silk cocoon present becomes an item you open.
- [x] The spider truce covers cave spiders (it already did: `GirlEvents.onTarget`).
- [x] Spider-silk trousers and shoes: light, iron-level protection, but they wear out faster.

**Centaur**
- [x] Ridden centaurs have stamina. A 15-second gallop, then a slow walk until a third is back.
- [x] The zebra lets only her father ride her.

**Merfolk**
- [x] Young merfolk can be carried in a water bucket.
- [x] The dragon koi can fly (slow levitation). Out of water she floats about a block above the ground, without gravity.
- [x] All merfolk fetch items the player dropped. Items that fell into the water.
- [x] At high friendship they share air with a drowning player, then surface themselves so they don't drown. Once every 30 seconds. Water breathing at high friendship was replaced by this; devoted still gives conduit power.
- [x] Merfolk merchants: neutral wild merfolk who trade ocean loot (from the loot pools) like villagers. One wild grown merfolk in five. Empty hand opens her trades. Wares are rolled from the ruin, shipwreck and buried treasure loot tables; she buys cod and gold.
- [x] Merfolk scales drop now and then, even from wild ones. They craft into flippers (faster swimming).

**Slime**
- [x] Two slime species can merge into a mixed slime. At high friendship with both, a slime ball on one while a slime of another kind is within 3 blocks: they flow into one marbled slime (ten looks, one per pair). She keeps both girls' data. A killing blow, or sneak + slime ball, separates them again.
- [x] A slime's inventory spills out when she splits.

**Jackalope**
- [x] The Inaba brings rice cakes (a new food) at full devotion.
- [x] Rice cakes craft into mochi with other foods (apple mochi, melon mochi...).
- Not doing: several jackalopes breeding with one sleeping player at once. It turns the abstract bed-sharing into a group scene, and the mod is low ecchi.

**Minotaur**
- [x] Cheese becomes its own item.
- [x] Only cows (female minotaurs) can be milked (already the case).
- Not doing: the embarrassed-but-gives-milk-anyway reaction, and milk dripping from the chest. Both sexualise the milking.

**Mollusk**
- [x] The kraken can pull a boat (a ride). High friendship: in the water next to her player's boat she tows it the way the rider looks.
- [x] Every squid type (kraken, ika, octopus) drops an ink sac now and then.

**Elves**
- [ ] High elf settlements grow beyond one pavilion, but only if worldgen and performance stay unaffected.

**Undead**
- [x] The living doll sits on bookshelves (any block that powers an enchanting table). With nobody to stalk she hops onto a free bookshelf top within 10 blocks and sits there for one to two minutes, watching the room.

**Lizardfolk**
- [x] The chameleon's colours follow the biome.

**Dragon**
- [x] The wyrm's fire breath sets fire on top of blocks (normal fire ticks do the rest, no burnt block variants). Obeys `mobGriefing`.

**Devil**
- [x] The lesser devil steals from chests too.

**Bug**
- [x] The moth eats wool. Wool and carpet lying about (she heals on it). She never eats wool built into something.
- [x] Bee girls collect nectar from flora girls. A bee girl carrying flora nectar near a vanilla flower has a small chance to turn it into a flora girl.

**Flora**
- [x] The alraune can be planted as a flower block. At high friendship, sneak with a shovel: she becomes an Alraune Bloom item. Plant it on dirt; right-click or break it and she steps out with her bonds intact.
- [x] Nectar from flora girls at high friendship, collected with a glass bottle (built as gathering, not "milking").
- [x] Nectar waters and feeds a field: pour a bottle on farmland and it soaks every block within 2 and gives every crop a growth spurt.
- [x] Nectar as a placeable fluid (Bucket of Nectar: three nectar bottles and a bucket). It hydrates farmland like water, and crops within 4 blocks grow on half of their growth ticks. It is slow and honey-like, makes no infinite source, and renders as golden-tinted water.

## 0g. Round of 2026-10-22 (evening)

- [x] **Cowbell:** a minotaur cow's bell rings once (a note over her head) when her milk is ready again. This replaces the milk-drip idea, which stays out (it sexualises her).
- [x] **Orders outrank following:** under sit or patrol a girl no longer follows her player, flocks, flies home, brings presents, begs or joins your bed. Under follow only her own following gives way (presents and bed stay). This covers harpies (leader, flock, home, owl night-stalking), anurans and girls.
- [x] **Healing balm** (nectar bottle, honey bottle and a small flower make 3, or a present from a healer): use it on a girl who is fond of you, or on a befriended vanilla mob, for 10 health and Regeneration II. Healers are the flora, the verdant and honey slimes, the snail, the fairies and the axolotl. At high friendship one present in five is a balm, and every 30 seconds they tend your most-hurt companion nearby (4 health and a little regeneration).
- [x] **Farmhands:** a girl patrolling (stick) for a player she is fond of at high friendship harvests ripe crops in her patrol area, replants them from the harvest, and stores the rest in a chest or barrel there (or drops it). Flora girls also nudge growing crops along. Obeys mobGriefing. Merfolk don't farm.
- [x] **Slimes in any RGB colour:** a dye mixes its colour into hers, a water bottle washes it out, and merging mixes the two colours. Mixing averages the hues round the colour wheel and keeps brightness and most of the saturation (red + blue = magenta, red + green = yellow), so repeated merges never turn white. Dyed slimes use `<species>_tint.png` (light greys) with the colour applied by the renderer.
- [ ] Dev client: how the tint looks on each slime (the core and eyes take the colour too), the cowbell, and how harpies settle on patrol.
- [ ] Harpies and anurans don't farm yet (only the girl families).

## 1. More species ideas

Ideas to pick from. Each new species needs a gen module, a field-guide entry, tests and its own favourite food.

- **Nekomata:** caracal, black panther, ocelot
- **Kitsune:** kumiho (nine tails from birth), bat-eared fox
- **Lamia:** viper, anaconda, medusa (a petrifying gaze)
- **Arachne:** black widow, peacock spider, cave spider
- **Centaur:** donkey, unicorn, camel, elk
- **Merfolk:** pufferfish, angler, manatee, siren (a luring song)
- **Slime:** ender slime (teleports), copper slime (oxidises), crystal slime
- **Harpy:** penguin, flamingo, hummingbird, vulture, parrot
- **Anuran:** axolotl-like newt girls (a salamander sub-family?)
- **Jackalope:** wolpertinger, snowshoe hare
- **Minotaur:** yak, highland cow
- **Mollusk:** jellyfish, nautilus, sea slug (nudibranch)
- **Elf:** snow elf, dark elf of the deep dark
- **Undead:** mummy, banshee, ghoul, skeleton
- **Lizardfolk:** gecko (climbs walls), komodo (venomous bite), axolotl
- **Dragon:** amphiptere, sea serpent, ender dragon girl
- **Devil:** imp, oni, hellhound
- **Bug:** ant with queen, mantis, firefly, beetle, spider-wasp
- **Flora:** mushroom-cap dryad, venus flytrap, rose, lotus (water)
- **Whole new families:** wolf girls, bats, kobolds, golems, cyclops, sheep girls, sphinx, gorgons

## 2. Seagull/sparrow arrow farm

- [x] Gulls shed feathers and sometimes flint while roosting in a hopper; sparrows shed feathers and sticks. Done in the 2026-10-20 batch: `AbstractHarpy.roostDrop`, `RoostInHopperGoal`. Covered by GameTests.
- [ ] Check in a real client that an arrow farm built from it works (hopper → chest, drop rate, roost stays put).
- [ ] Decide whether the flint and stick rates feel right. Should a fletching table nearby give a bonus?

## 3. Custom sounds for every species

At the moment **every** sound is a vanilla sound.
- Harpies and anurans already have their own sound events in `sounds.json`, but those still point at vanilla files. To replace them, drop `.ogg` files into `assets/monstergirls/sounds/` and edit `sounds.json`; no code change is needed.
- The girl families (`entity/girl/*`) call `SoundEvents.*` directly. They first need a sound event per species, either registered in `ModSounds` or generated by a script, and then the `getAmbientSound`/`getHurtSound`/`getDeathSound` and `playSound` calls switched over.
- Base set per species: **ambient, hurt, death, eat**. Below, "+" marks the extra action sounds and "currently" gives the vanilla sound in use.

### Harpies (event exists, file is vanilla)
| Species | Sounds to replace (currently) |
|---|---|
| sparrow | ambient, hurt, death, eat (parrot); + sing (flute note) |
| crow | caw, giggle, hurt, death, eat (parrot); + laugh (witch celebrate) |
| owl | hoot (flute note); hurt, death, eat (parrot) |
| seagull | ambient, hurt, death, eat (parrot); + sing (harp note) |
| albatross | ambient, hurt, death, eat (parrot); + bill clack (wooden button) |
| ostrich | ambient (sniffer), hurt/death/eat (camel); + hiss (goat pre-ram), kick (goat impact), step (camel step) |
| riflebird | ambient, hurt, death, eat (parrot); + sing (pling), bass, hat (note blocks), lay (chicken plop) |
| phoenix | ambient (blaze breathe), hurt (blaze hit), death (parrot); + shoot (blaze shoot), cry (icechime), rebirth (fire charge) |
| all harpies | + egg laid (chicken egg) |

### Anurans (event exists, file is vanilla frog)
All 12 species have **ambient, hurt, death, eat, tongue, bellow**, all on the frog sounds: bullywug, mucus toad, bullfrog, tree frog, poison dart frog, glass frog, goliath frog, desert rain frog, fire-bellied toad, cane toad, natterjack, Surinam toad. Bullywug also has a mucus squish (slime squish small).

### Girl families (need new events)
| Family / species | ambient | hurt | death | extras |
|---|---|---|---|---|
| **Nekomata** (serval, tiger, lioness, snow leopard, cheetah, lynx) | cat ambient / purr | cat hurt | cat death | hiss, roar (ravager roar), purreow, second life (totem) |
| **Kitsune** (red, arctic, fennec, corsac, gray) | fox sleep by day / fox ambient | fox hurt | fox death | screech, bite |
| **Lamia** (python, cobra, sea snake, rattlesnake, boa) | axolotl idle | cat hiss | axolotl death | rattle (chain armour equip), spit (llama spit), hiss |
| **Arachne** (jumping spider, orb weaver, tarantula, trapdoor, wolf spider) | spider ambient | spider hurt | spider death | ambush burst, brushing |
| **Centaur:** horse, reindeer | horse ambient | horse hurt | horse death | step, saddle, chestplate, brush |
| zebra | donkey ambient | horse hurt | horse death | angry bray (donkey angry) |
| goat | goat ambient | goat hurt | horse death | |
| alpaca | llama ambient | llama hurt | horse death | spit, chest (donkey chest), shear |
| **Merfolk:** orca, shark, seal, dolphin | dolphin ambient (water/air) | dolphin hurt | dolphin death | tease/play (dolphin play), shell found (brush sand) |
| koi | fish swim | dolphin hurt | dolphin death | |
| axolotl | axolotl idle (water/air) | axolotl hurt | axolotl death | |
| **Slime:** verdant, magma, honey, frost | slime squish small | slime hurt small | slime death small | bounce (slime jump), split/merge squish, magma crackle (furnace), honey bottle fill |
| amethyst | amethyst chime | slime hurt small | slime death small | resonance chime |
| **Jackalope** (Inaba, Jack, Easter, Al-mi'raj, volcano) | rabbit ambient | rabbit hurt | rabbit death | hop, egg hide (grass place), volcano pant |
| **Minotaur** (cattle, Minoan, bison, aurochs, water buffalo; cow and bull voices) | cow ambient | cow hurt | cow death | milk, bellow (ravager roar), charge hit (ravager attack), plough (hoe till), step, brush |
| **Mollusk:** kraken, Ika, octopus, ammonite | squid ambient (water) | squid hurt | squid death | ink (squid squirt), grab/lash |
| snail | turtle ambient (land) | squid hurt | squid death | retract into shell |
| **Elf** (night, high, wood, blood, desert) | *none* | player hurt | player death | trade yes (villager yes), bow (skeleton shoot), crossbow, pavilion build |
| **Undead:** zombie | zombie ambient | zombie hurt | zombie death | |
| phantom | phantom ambient | player hurt | soul escape | |
| will-o'-wisp | soul escape | player hurt | soul escape | |
| living doll | *none* | player hurt | soul escape | tug (leather armour equip) |
| yuki-onna, vampire, Frankenstein | allay ambient | player hurt | soul escape | vampire drink (honey drink), Frankenstein lightning |
| **Lizardfolk:** salamander, chameleon, iguana, croc | *none* | player hurt | player death | tongue (frog tongue), egg crack, death roll |
| turtle | turtle ambient (land) | turtle hurt | turtle death | shell retreat |
| **Dragon:** long, Jormungandr, wyrm, gryphon, wyvern, Orochi | ender dragon ambient | ender dragon hurt | player death | fire breath (blaze shoot), hoard (item pickup), wing flap, Orochi multi-head bite |
| Kannae | *none* | ender dragon hurt | player death | lightning call |
| **Devil:** lesser devil | vex ambient | player hurt | player death | prank giggle (vex), sweet thank-you (witch celebrate) |
| succubus, greater devil, jack o'lantern, jiang shi | allay ambient (none while disguised) | player hurt | player death | fireball (blaze shoot), jiang shi hop, pumpkin reveal |
| **Bug:** bee, queen bee | bee loop | bee hurt | bee death | hive work, pollinate |
| mosquito | bee loop aggressive | bee hurt | bee death | drink (honey drink) |
| fairy, fairy queen | amethyst chime | bee hurt | bee death | fountain finished (amethyst resonate), heal |
| moth, moth larva, scorpion, papillon, papillon larva | *none* | bee hurt | bee death | sting, larva chew, cocoon/emerge |
| **Flora** (alraune, matango, ent, cactus, mandragora) | azalea leaves step | azalea leaves break | azalea break | mandragora scream (ghast scream), bone meal, pollen/spore puff, matango sink |
| **All girls** | | | | eat (generic eat), chirp/happy, egg laid (chicken egg), pet |

**Full register:** [SOUNDS.md](SOUNDS.md) lists every sound with its file and line (regenerate it with `python tools/sound_register.py`).

### Plan for the sound work
- [ ] Pick a naming scheme: `entity.<species>.<slot>`. Generate the `sounds.json` entries, the `ModSounds` registration and the subtitles from `GirlSpecies`, the same way `gen_girl_data.py` works.
- [ ] As a first step, point every new event at the vanilla sound it replaces, so nothing changes until real files arrive.
- [ ] Switch the family classes from `SoundEvents.*` to the per-species events.
- [ ] Record or source the `.ogg` files (CC0/licensed), one folder per species.
- [ ] Give the girls without an ambient sound one: elves, the living doll, Kannae, most lizardfolk and the moths/scorpions/papillons.
